package com.cimdy.coe.data.codec.stronghold.task.helper.dispatch;

import com.cimdy.coe.data.codec.stronghold.task.helper.claim.DataUpdate;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandLedger;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdRuntimeState;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class TaskDispatcher {

    public static void dispatchTasks(ServerLevel level, GlobalPos terminalPos, Stronghold hold) {
        if (hold == null) return;
        StrongholdState state = hold.holdState();

        List<ActiveTask> pending = new ArrayList<>(state.pendingTasks());
        Map<UUID, List<ActiveTask>> active = deepCopyMap(state.getActiveTasks());

        // 清理终止的任务，并提取新产生的 pending 任务
        Pair<List<ActiveTask>, Map<UUID, List<ActiveTask>>> cleaned = cleanAndAdvanceTasks(pending, active, level);
        pending = cleaned.getFirst();
        active = cleaned.getSecond();

        // 验证活跃任务
        Pair<List<ActiveTask>, Map<UUID, List<ActiveTask>>> validationResult =
                validateTasks(pending, active, level, terminalPos);
        pending = mergeLists(pending, validationResult.getFirst());
        active = validationResult.getSecond();

        // 合并相同逻辑的待处理任务
        pending = mergePendingTasks(pending);

        // 收集空闲工人
        List<LivingEntity> idleWorkers = collectIdleWorkers(hold, active, level);

        boolean changed = false;
        if (!pending.isEmpty() && !idleWorkers.isEmpty()) {
            TaskAssigner.AssignmentResult result = switch (hold.taskMode()) {
                case SINGLE_CHAIN -> TaskAssigner.assignSingleChain(pending, idleWorkers);
                case MULTI_SUPPLY_CHAIN -> TaskAssigner.assignMultiSupplyChain(pending, idleWorkers);
                case BALANCED -> TaskAssigner.assignBalanced(pending, idleWorkers);
                case NEAREST_FIRST -> TaskAssigner.assignPriorityFirst(pending, idleWorkers);
            };

            List<ActiveTask> newPending = new ArrayList<>();
            Map<UUID, List<ActiveTask>> newAssignments = result.entityAssignments();

            for (ActiveTask task : result.updatedTasks()) {
                if (task.status() == TaskStatus.PENDING) {
                    newPending.add(task);
                }
            }
            pending = mergePendingTasks(newPending);
            active = mergeEntityTasks(active, newAssignments);

            for (Map.Entry<UUID, List<ActiveTask>> entry : newAssignments.entrySet()) {
                UUID entityId = entry.getKey();
                for (ActiveTask assignedTask : entry.getValue()) {
                    assignedTask.task().onWorkerAssigned(level, terminalPos, entityId, assignedTask.taskId());
                }
            }
            changed = true;
        }

        if (changed) {
            // 方案 B：任务分配/推进后标记终端为脏，使下一轮 DataUpdate 及时重算供需。
            DemandLedger.markHoldDirty(terminalPos);
            // 方案 B 延伸：任务变化（工人释放/分配/完成）影响站点工人平衡，标记该终端所有站点为脏。
            for (Site site : hold.holdState().sites()) {
                DemandLedger.markSiteDirty(terminalPos, site.getSitePos());
            }
        }

        if (changed || !pending.equals(state.pendingTasks()) || !active.equals(state.getActiveTasks())) {
            List<ActiveTask> finalPending = pending;
            // StrongholdRuntimeState 的 activeTasks 以 String（UUID.toString）为键，与 StrongholdState 内部布局一致
            Map<String, List<ActiveTask>> finalActive = new HashMap<>();
            for (var entry : active.entrySet()) {
                finalActive.put(entry.getKey().toString(), entry.getValue());
            }
            StrongholdHelper.updateHoldAtPos(level, terminalPos, h -> {
                StrongholdState latest = h.holdState();
                // 方案 E：高频运行时态更新走 withRuntime，保留低频配置态（站点）不变。
                StrongholdState newState = latest.withRuntime(
                        new StrongholdRuntimeState(latest.transferResult(), finalPending, finalActive));
                return h.withHoldState(newState);
            });
        }
    }

    // ---------- 任务链操作 ----------

    /** 验证整条任务链 / 依赖图（方案 C：遍历所有可达节点） */
    private static boolean validateTaskChain(ActiveTask at, ServerLevel level) {
        for (Task t : TaskGraph.collectAll(at.task())) {
            if (!t.validate(level)) {
                return false;
            }
        }
        return true;
    }

    /** 停止整条任务链 / 依赖图（执行 onStop，若为 WorkTask 且提供实体则移除实体） */
    private static void stopTaskChain(ActiveTask at, ServerLevel level, GlobalPos terminalPos, UUID entityId) {
        for (Task t : TaskGraph.collectAll(at.task())) {
            t.onStop(level);
            if (t instanceof WorkTask workTask && entityId != null && terminalPos != null) {
                workTask.removeEntityFromSite(level, terminalPos, entityId);
            }
        }
    }

    /**
     * 剥离链首任务，返回其后继任务（方案 C：支持 DAG 多后继）生成的新 ActiveTask 列表（PENDING 状态）。
     * 对线性链返回至多一个元素，与原行为一致。
     */
    private static List<ActiveTask> advanceTaskChain(ActiveTask completed) {
        List<ActiveTask> successors = new ArrayList<>();
        for (Task next : completed.task().getNextTasks()) {
            if (next != null) {
                successors.add(new ActiveTask(completed.taskId(), next, TaskStatus.PENDING));
            }
        }
        return successors;
    }

    // ---------- 清理与验证 ----------

    /**
     * 清理终止的任务，并对 COMPLETED 任务剥离链首，生成新 pending 任务。
     * 返回新的 pending 列表和 active 映射。
     */
    private static Pair<List<ActiveTask>, Map<UUID, List<ActiveTask>>> cleanAndAdvanceTasks(
            List<ActiveTask> pending,
            Map<UUID, List<ActiveTask>> active,
            ServerLevel level) {

        List<ActiveTask> newPending = new ArrayList<>(pending);
        Map<UUID, List<ActiveTask>> newActive = new HashMap<>();

        // 清理 active 中的 COMPLETED / FAILED
        for (var entry : active.entrySet()) {
            UUID entityId = entry.getKey();
            List<ActiveTask> tasks = entry.getValue();
            List<ActiveTask> kept = new ArrayList<>();
            for (ActiveTask at : tasks) {
                if (at.status() == TaskStatus.COMPLETED) {
                    // 剥离链首，所有后继加入 pending（方案 C：支持 DAG 多后继）
                    newPending.addAll(advanceTaskChain(at));
                    // 注意：链首任务已经完成，不需要再次 stop
                } else if (at.status() == TaskStatus.FAILED) {
                    // 停止整条链
                    stopTaskChain(at, level, null, null);
                } else {
                    kept.add(at);
                }
            }
            if (!kept.isEmpty()) {
                newActive.put(entityId, kept);
            }
        }

        // 清理 pending 中的 COMPLETED / FAILED
        newPending.removeIf(at -> {
            if (at.status() == TaskStatus.COMPLETED) {
                return true; // 移除原任务
            } else if (at.status() == TaskStatus.FAILED) {
                stopTaskChain(at, level, null, null);
                return true;
            }
            return false;
        });

        // 收集上面循环中无法直接添加的 next 任务 (简化起见，重新遍历一次)
        List<ActiveTask> extraPending = new ArrayList<>();
        for (ActiveTask at : pending) {
            if (at.status() == TaskStatus.COMPLETED) {
                extraPending.addAll(advanceTaskChain(at));
            }
        }
        newPending.addAll(extraPending);

        return Pair.of(newPending, newActive);
    }

    /**
     * 验证所有活跃任务，并将无效任务回退到 pending 或标记 FAILED。
     */
    private static Pair<List<ActiveTask>, Map<UUID, List<ActiveTask>>> validateTasks(
            List<ActiveTask> pending,
            Map<UUID, List<ActiveTask>> active,
            ServerLevel level,
            GlobalPos terminalPos) {

        List<ActiveTask> fallback = new ArrayList<>();
        Map<UUID, List<ActiveTask>> newActive = new HashMap<>();

        for (var entry : active.entrySet()) {
            UUID entityId = entry.getKey();
            LivingEntity entity = StrongholdHelper.findLivingEntity(level, entityId);
            if (!canWork(entity)) {
                for (ActiveTask at : entry.getValue()) {
                    handleInvalidTask(at, entityId, level, terminalPos, fallback, newActive);
                }
                continue;
            }
            for (ActiveTask at : entry.getValue()) {
                if (validateTaskChain(at, level)) {
                    newActive.computeIfAbsent(entityId, k -> new ArrayList<>()).add(at);
                } else {
                    handleInvalidTask(at, entityId, level, terminalPos, fallback, newActive);
                }
            }
        }

        // 清理无效的 pending 任务
        Iterator<ActiveTask> pendingIt = pending.iterator();
        while (pendingIt.hasNext()) {
            ActiveTask at = pendingIt.next();
            if (!validateTaskChain(at, level)) {
                stopTaskChain(at, level, null, null);
                pendingIt.remove();
            }
        }

        return Pair.of(fallback, newActive);
    }

    private static void handleInvalidTask(
            ActiveTask at,
            UUID entityId,
            ServerLevel level,
            GlobalPos terminalPos,
            List<ActiveTask> fallback,
            Map<UUID, List<ActiveTask>> newActive) {
        stopTaskChain(at, level, terminalPos, entityId);
        if (at.task().canFallback()) {
            fallback.add(at.updateStatus(TaskStatus.PENDING));
        } else {
            // 不可回退的任务直接标记失败，后续清理时会移除
            ActiveTask failed = at.updateStatus(TaskStatus.FAILED);
            newActive.computeIfAbsent(entityId, k -> new ArrayList<>()).add(failed);
        }
    }

    // ---------- 工人收集 ----------
    private static List<LivingEntity> collectIdleWorkers(Stronghold hold, Map<UUID, List<ActiveTask>> active, ServerLevel level) {
        List<LivingEntity> list = new ArrayList<>();
        for (UUID uuid : hold.entities()) {
            Entity e = level.getEntity(uuid);
            if (canWork(e) && !active.containsKey(e.getUUID())) {
                list.add((LivingEntity) e);
            }
        }
        list.sort(Comparator.comparingInt(CapacityHelper::getRemainingCapacity).reversed());
        return list;
    }

    // ---------- 工具方法 ----------
    private static List<ActiveTask> mergeLists(List<ActiveTask> a, List<ActiveTask> b) {
        List<ActiveTask> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }

    private static Map<UUID, List<ActiveTask>> mergeEntityTasks(
            Map<UUID, List<ActiveTask>> current, Map<UUID, List<ActiveTask>> newAssignments) {
        Map<UUID, List<ActiveTask>> result = new HashMap<>(current);
        for (var entry : newAssignments.entrySet()) {
            result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }
        return result;
    }

    private static Map<UUID, List<ActiveTask>> deepCopyMap(Map<UUID, List<ActiveTask>> source) {
        Map<UUID, List<ActiveTask>> copy = new HashMap<>();
        for (var entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public static List<ActiveTask> mergePendingTasks(List<ActiveTask> tasks) {
        Map<Object, List<ActiveTask>> groups = new HashMap<>();
        List<ActiveTask> nonMerged = new ArrayList<>();

        for (ActiveTask at : tasks) {
            if (at.status() != TaskStatus.PENDING) {
                nonMerged.add(at);
                continue;
            }
            Optional<?> key = at.task().getGroupingKey();
            if (key.isPresent()) {
                groups.computeIfAbsent(key.get(), k -> new ArrayList<>()).add(at);
            } else {
                nonMerged.add(at);
            }
        }

        List<ActiveTask> merged = new ArrayList<>();
        for (var entry : groups.entrySet()) {
            List<ActiveTask> group = entry.getValue();
            int totalVolume = group.stream().mapToInt(t -> t.task().getVolume()).sum();
            ActiveTask template = group.getFirst();
            Task mergedTask;
            if (template.task() instanceof WorkTask workTask) {
                mergedTask = workTask.createSubTask(totalVolume);
            } else {
                mergedTask = template.task().split(totalVolume);
            }
            if (mergedTask == null) mergedTask = template.task();
            merged.add(template.updateTask(mergedTask).updateStatus(TaskStatus.PENDING));
        }
        merged.addAll(nonMerged);
        return merged;
    }

    /**
     * 能否派工。盟友（AdditionData 的 ally 标记，见 {@link AdditionKeys#ALLY}）直接排除 —— 帕鲁式随行伙伴不进据点工作。
     * 返回 false 时 {@link #validateTasks} 会把其进行中的任务经 {@link #handleInvalidTask} 回退 PENDING，
     * 因此「取消盟友工作」不需要额外的撤工代码。
     */
    private static boolean canWork(Entity entity) {
        // 原 AttachmentRegistry.ALLY 已迁入 AdditionData（key=ally）
        return entity != null && entity.isAlive()
                && AdditionData.getOrDefault(entity, AdditionKeys.ALLY, 0.0) == 0.0;
    }
}