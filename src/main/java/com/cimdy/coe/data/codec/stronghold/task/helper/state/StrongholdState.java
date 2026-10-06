package com.cimdy.coe.data.codec.stronghold.task.helper.state;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.MatchResultWithTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.TransferResult;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.FEChargeTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 据点状态根（方案 E：运行时 / 配置态分离）。
 * <p>
 * 内部由 {@link StrongholdRuntimeState}（高频运行时态）与 {@link StrongholdConfigState}（低频配置态）组成，
 * 对外保持原有扁平 API 与序列化布局（match_result / active_tasks / active_tasks_by_entity / sites），
 * 因此所有调用方与既有存档无需改动。新增的 {@link #runtimeState()} / {@link #configState()} 供未来
 * 「内存优先 + 增量同步」优化使用。
 */
public record StrongholdState(
        StrongholdRuntimeState runtime,
        StrongholdConfigState config
) {
    public static final StrongholdState EMPTY = new StrongholdState(StrongholdRuntimeState.EMPTY, StrongholdConfigState.EMPTY);

    /** 兼容旧 4 参构造：将扁平字段归并到运行时 / 配置两态。 */
    public StrongholdState(TransferResult transferResult, List<ActiveTask> pendingTasks,
                     Map<String, List<ActiveTask>> activeTasks, List<Site> sites) {
        this(new StrongholdRuntimeState(transferResult, pendingTasks, activeTasks), new StrongholdConfigState(sites));
    }

    public static final Codec<StrongholdState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TransferResult.CODEC.fieldOf("match_result").forGetter(StrongholdState::transferResult),
            ActiveTask.CODEC.listOf().fieldOf("active_tasks").forGetter(StrongholdState::pendingTasks),
            Codec.unboundedMap(Codec.STRING, ActiveTask.CODEC.listOf())
                    .fieldOf("active_tasks_by_entity").forGetter(StrongholdState::activeTasks),
            Site.CODEC.listOf().fieldOf("sites").forGetter(StrongholdState::sites)
    ).apply(instance, StrongholdState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrongholdState> STREAM_CODEC = StreamCodec.composite(
            TransferResult.STREAM_CODEC, StrongholdState::transferResult,
            ActiveTask.STREAM_CODEC.apply(ByteBufCodecs.list()), StrongholdState::pendingTasks,
            ByteBufCodecs.map(HashMap::new,
                    ByteBufCodecs.STRING_UTF8, ActiveTask.STREAM_CODEC.apply(ByteBufCodecs.list())), StrongholdState::activeTasks,
            Site.STREAM_CODEC.apply(ByteBufCodecs.list()), StrongholdState::sites,
            StrongholdState::new
    );

    // ---------- 扁平访问器（委托到运行时 / 配置两态） ----------

    public TransferResult transferResult() {
        return runtime.transferResult();
    }

    public List<ActiveTask> pendingTasks() {
        return runtime.pendingTasks();
    }

    public Map<String, List<ActiveTask>> activeTasks() {
        return runtime.activeTasks();
    }

    public List<Site> sites() {
        return config.sites();
    }

    /** 直接访问运行时态（方案 E）。 */
    public StrongholdRuntimeState runtimeState() {
        return runtime;
    }

    /** 直接访问配置态（方案 E）。 */
    public StrongholdConfigState configState() {
        return config;
    }

    /** 仅替换运行时态（高频：供需/任务），保留配置态（低频：站点）不变（方案 E 启用）。 */
    public StrongholdState withRuntime(StrongholdRuntimeState runtime) {
        return new StrongholdState(runtime, this.config);
    }

    public static StrongholdState of(
            TransferResult transferResult, List<ActiveTask> pending, Map<UUID, List<ActiveTask>> active, List<Site> sites) {
        Map<String, List<ActiveTask>> updatedActive = new HashMap<>();
        for (Map.Entry<UUID, List<ActiveTask>> entry : active.entrySet()) {
            UUID uuid = entry.getKey();
            List<ActiveTask> activeTaskList = entry.getValue();
            updatedActive.put(uuid.toString(), activeTaskList);
        }
        return new StrongholdState(transferResult, pending, updatedActive, sites);
    }

    public Map<UUID, List<ActiveTask>> getActiveTasks() {
        Map<UUID, List<ActiveTask>> newActiveTasks = new HashMap<>();
        for (Map.Entry<String, List<ActiveTask>> entry : this.activeTasks().entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            List<ActiveTask> activeTaskList = entry.getValue();
            newActiveTasks.put(uuid, activeTaskList);
        }
        return newActiveTasks;
    }

    /** 仅更新供需结果，不影响任务（本地扫描使用） */
    public StrongholdState withMatchResult(TransferResult newResult) {
        return new StrongholdState(newResult, this.pendingTasks(), this.activeTasks(), sites());
    }

    /** 增量追加活跃任务列表 */
    public StrongholdState appendActiveTasks(Map<UUID, List<Task>> newActive) {
        Map<String, List<ActiveTask>> updatedActive = new HashMap<>(this.activeTasks());
        for (Map.Entry<UUID, List<Task>> entry : newActive.entrySet()) {
            UUID uuid = entry.getKey();
            List<ActiveTask> activeTaskList = entry.getValue().stream()
                    .map(task -> ActiveTask.of(task, TaskStatus.ASSIGNED)).toList();
            updatedActive.put(uuid.toString(), activeTaskList);
        }
        return new StrongholdState(this.transferResult(), pendingTasks(), updatedActive, sites());
    }

    /** 增量追加新任务 */
    public StrongholdState appendNewTasks(ServerLevel serverLevel, List<? extends Task> newTasks) {
        List<ActiveTask> updated = new ArrayList<>(this.pendingTasks());
        newTasks.forEach(t -> t.onStart(serverLevel));
        updated.addAll(newTasks.stream().map(ActiveTask::of).toList());
        return new StrongholdState(this.transferResult(), updated, this.activeTasks(), sites());
    }

    public StrongholdState withMatchResult(ServerLevel serverLevel, MatchResultWithTask result) {
        TransferResult r = result.toMatchResult();
        return this.withMatchResult(r).appendNewTasks(serverLevel, result.tasks());
    }

    public StrongholdState updateTask(UUID entityId, UUID taskId, Task task) {
        Map<String, List<ActiveTask>> updatedActive = new HashMap<>(this.activeTasks());
        if(updatedActive.containsKey(entityId.toString())) {
            List<ActiveTask> updated = new ArrayList<>(updatedActive.get(entityId.toString()));
            for(ActiveTask activeTask : updated) {
                if(activeTask.taskId().equals(taskId)) {
                    updated.set(updated.indexOf(activeTask), activeTask.updateTask(task));
                    break;
                }
            }
            updatedActive.put(entityId.toString(), updated);
        }
        return new StrongholdState(this.transferResult(), this.pendingTasks(), updatedActive, sites());
    }

    public StrongholdState setTaskFailed(UUID entityId, UUID taskId) {
        Map<String, List<ActiveTask>> updatedActive = new HashMap<>(this.activeTasks());
        if(updatedActive.containsKey(entityId.toString())) {
            List<ActiveTask> updated = new ArrayList<>(updatedActive.get(entityId.toString()));
            for (ActiveTask activeTask : updated) {
                if (activeTask.taskId().equals(taskId)) {
                    updated.set(updated.indexOf(activeTask), activeTask.updateStatus(TaskStatus.FAILED));
                    break;
                }
            }
            updatedActive.put(entityId.toString(), updated);
        }
        return new StrongholdState(this.transferResult(), this.pendingTasks(), updatedActive, sites());
    }

    public StrongholdState setTaskCompleted(UUID entityId, UUID taskId) {
        Map<String, List<ActiveTask>> updatedActive = new HashMap<>(this.activeTasks());
        if(updatedActive.containsKey(entityId.toString())) {
            List<ActiveTask> updated = new ArrayList<>(updatedActive.get(entityId.toString()));
            for (ActiveTask activeTask : updated) {
                if (activeTask.taskId().equals(taskId)) {
                    updated.set(updated.indexOf(activeTask), activeTask.updateStatus(TaskStatus.COMPLETED));
                    break;
                }
            }
            updatedActive.put(entityId.toString(), updated);
        }
        return new StrongholdState(this.transferResult(), this.pendingTasks(), updatedActive, sites());
    }

    /**
     * 获取指定工作方块的站点（使用 Optional 避免 null）
     */
    public Optional<Site> getSite(BlockPos workBlock) {
        return this.sites().stream()
                .filter(site -> site.getSitePos().equals(workBlock))
                .findFirst();
    }

    /**
     * 添加或更新一个生产站点（如果工作方块已存在则替换，否则新增）
     */
    public StrongholdState withSite(Site site) {
        List<Site> newSites = new ArrayList<>(this.sites());
        newSites.removeIf(s -> s.getSitePos().equals(site.getSitePos()));
        newSites.add(site);
        return new StrongholdState(this.transferResult(), this.pendingTasks(), this.activeTasks(), newSites);
    }

    /**
     * 移除指定工作方块的站点
     */
    public StrongholdState withoutSite(BlockPos workBlock) {
        List<Site> newSites = this.sites().stream()
                .filter(s -> !s.getSitePos().equals(workBlock))
                .toList();
        return new StrongholdState(this.transferResult(), this.pendingTasks(), this.activeTasks(), newSites);
    }

    /**
     * 对指定工作方块的站点进行自定义更新（如果站点不存在，则不做任何事）
     */
    public StrongholdState updateSite(BlockPos workBlock, java.util.function.UnaryOperator<Site> updater) {
        return getSite(workBlock)
                .map(site -> withSite(updater.apply(site)))
                .orElse(this);
    }

    /** 替换指定的 pending 任务（根据 taskId） */
    public StrongholdState withPendingTaskUpdated(UUID taskId, ActiveTask newTask) {
        List<ActiveTask> newPending = new ArrayList<>();
        for (ActiveTask t : this.pendingTasks()) {
            if (t.taskId().equals(taskId)) {
                newPending.add(newTask);
            } else {
                newPending.add(t);
            }
        }
        return new StrongholdState(this.transferResult(), newPending, this.activeTasks(), this.sites());
    }

    /** 移除指定的 pending 任务（根据 taskId） */
    public StrongholdState withoutPendingTask(UUID taskId) {
        List<ActiveTask> newPending = this.pendingTasks().stream()
                .filter(t -> !t.taskId().equals(taskId))
                .collect(Collectors.toList());
        return new StrongholdState(this.transferResult(), newPending, this.activeTasks(), this.sites());
    }

    /** 直接设置整个 pendingTasks 列表 */
    public StrongholdState withPendingTasks(List<ActiveTask> pendingTasks) {
        return new StrongholdState(this.transferResult(), pendingTasks, this.activeTasks(), this.sites());
    }

    /** 将扫描到的 Pos任务 通过坐标去重 追加到 StrongholdState*/
    public StrongholdState appendPosTasks(ServerLevel level, List<Task> allPosTasks) {
        if (allPosTasks.isEmpty()) return this;
        // 收集所有已有任务的工作方块坐标（pending + active）
        Set<BlockPos> existing = new HashSet<>();
        for (ActiveTask t : this.pendingTasks()) {
            if (t.task() instanceof FEChargeTask charge) {
                existing.add(charge.getWorkPos());
            }
        }
        for (List<ActiveTask> tasks : this.activeTasks().values()) {
            for (ActiveTask t : tasks) {
                if (t.task() instanceof BasicTask.Pos pos) {
                    existing.add(pos.getWorkPos());
                }
            }
        }

        // 过滤出尚未存在的任务
        List<Task> newTasks = new ArrayList<>();
        for (Task task : allPosTasks) {
            if (task instanceof BasicTask.Pos pos && !existing.contains(pos.getWorkPos())) {
                newTasks.add(task);
            }
        }

        if (!newTasks.isEmpty()) {
            return this.appendNewTasks(level, newTasks);
        }
        return this;
    }

    public List<ActiveTask> getPendingTasksForSite(BlockPos sitePos) {
        return pendingTasks().stream()
                .filter(at -> at.task() instanceof WorkTask wt && wt.getWorkPos().equals(sitePos))
                .toList();
    }
    public StrongholdState withoutAllPendingTasksForSite(BlockPos sitePos) {
        List<ActiveTask> filtered = pendingTasks().stream()
                .filter(at -> !(at.task() instanceof WorkTask wt && wt.getWorkPos().equals(sitePos)))
                .toList();
        return new StrongholdState(transferResult(), filtered, activeTasks(), sites());
    }
}
