package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SiteManager {

    /**
     * 根据站点的当前状态（工作方块、容器、工人）自动平衡待处理任务的数量。
     * 返回可能已修改的 Stronghold。
     */
    public static Stronghold balanceWorkers(ServerLevel level, Stronghold hold, Site site) {
        StrongholdState state = hold.holdState();
        BlockPos sitePos = site.getSitePos();
        boolean siteChanged = false;

        // 1. 站点方块已失效 → 移除站点及所有 pending 任务
        if (!SiteTypes.isSiteBlock(level, sitePos)) {
            state = state.withoutSite(sitePos);
            state = state.withoutAllPendingTasksForSite(sitePos);
            return hold.withHoldState(state);
        }

        // 2. 清理无效容器（同时更新 siteChanged）
        List<BlockPos> validStorage = new ArrayList<>();
        for (BlockPos pos : site.getStorages()) {
            if (!level.isLoaded(pos)) {
                validStorage.add(pos);
                continue;
            }
            if (level.getBlockEntity(pos) != null &&
                    level.getCapability(Capabilities.Item.BLOCK, pos, null) != null) {
                validStorage.add(pos);
            } else {
                siteChanged = true;
            }
        }
        if (siteChanged) {
            site.setStorages(validStorage);
        }

        // 3. 清理无效工人（从站点记录中移除，但不操作 activeTasks）
        List<UUID> currentWorkers = new ArrayList<>(site.getWorkers());
        for (UUID entityId : currentWorkers) {
            List<ActiveTask> entityTasks = state.getActiveTasks().getOrDefault(entityId, List.of());
            boolean hasWorkTask = entityTasks.stream().anyMatch(at ->
                    at.task() instanceof WorkTask work && work.getWorkPos().equals(sitePos)
            );
            if (!hasWorkTask) {
                site.removeWorker(entityId);
                siteChanged = true;
            }
        }

        // 将站点数据修改写入 state
        if (siteChanged) {
            state = state.withSite(site);
        }

        // 4. 工人数量平衡（仅操作 pending 任务，不改变 site 对象）
        int maxWorkers = site.getMaxWorkers();
        if (maxWorkers <= 0) {
            return hold.withHoldState(state);
        }

        int assignedCount = site.getWorkers().size();
        List<ActiveTask> pendingTasks = new ArrayList<>(state.getPendingTasksForSite(sitePos));
        int pendingCount = pendingTasks.stream().mapToInt(t ->
                t.task() instanceof WorkTask wt ? wt.getPendingWorkers() : 0
        ).sum();

        if (!site.isAutoAssign()) {
            // 自动分配关闭：移除所有 pending 任务
            for (ActiveTask pt : pendingTasks) {
                state = state.withoutPendingTask(pt.taskId());
            }
        } else {
            // 自动分配开启：调整 pending 数量，使 已分配 + pending = maxWorkers
            int desiredPending = Math.max(0, maxWorkers - assignedCount);
            // 削减多余的 pending
            while (pendingCount > desiredPending && !pendingTasks.isEmpty()) {
                ActiveTask pt = pendingTasks.getLast();
                if (pt.task() instanceof WorkTask wt) {
                    int reduce = Math.min(wt.getPendingWorkers(), pendingCount - desiredPending);
                    wt.setPendingWorkers(wt.getPendingWorkers() - reduce);
                    pendingCount -= reduce;
                    if (wt.getPendingWorkers() <= 0) {
                        state = state.withoutPendingTask(pt.taskId());
                        pendingTasks.remove(pt);
                    } else {
                        state = state.withPendingTaskUpdated(pt.taskId(), pt);
                    }
                }
            }
            // 补充不足的 pending
            if (pendingCount < desiredPending) {
                int needed = desiredPending - pendingCount;
                if (!pendingTasks.isEmpty()) {
                    ActiveTask pt = pendingTasks.getFirst();
                    if (pt.task() instanceof WorkTask wt) {
                        wt.setPendingWorkers(wt.getPendingWorkers() + needed);
                        state = state.withPendingTaskUpdated(pt.taskId(), pt);
                    }
                } else {
                    Task newTask = WorkTask.generateTask(level, site);
                    if (newTask instanceof WorkTask nwt) {
                        // 物品采集类任务在存储容器满时不生成，避免无效派工（纯能量类任务不受影响）
                        if (nwt.needsItemStorage() && !StrongholdHelper.storageHasRoom(level, site.getStorages())) {
                            // 容器满，跳过本轮生成；搬运工搬走后下一轮自动恢复
                        } else {
                            nwt.setPendingWorkers(needed);
                            state = state.appendNewTasks(level, List.of(newTask));
                        }
                    }
                }
            }
        }

        // 注意：pending 调整已直接修改 state，无需再次设置 site
        return hold.withHoldState(state);
    }
}