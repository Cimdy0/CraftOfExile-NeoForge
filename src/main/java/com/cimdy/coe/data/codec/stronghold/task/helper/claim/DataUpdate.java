package com.cimdy.coe.data.codec.stronghold.task.helper.claim;

import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandLedger;
import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandSource;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.DistributedResult;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplySource;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.TransferResult;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockRange;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.stream.Collectors;

public class DataUpdate {

    public static void tick(ServerLevel serverLevel) {
        if (serverLevel == null) return;
        if (serverLevel.getGameTime() % 20 != 0) return;
        SaveData saveData = StrongholdHelper.getHoldData(serverLevel);
        boolean changed = false;

        // 方案 B：脏标记调度。每 FULL_SCAN_EVERY tick 触发一次全量兜底校正；
        // 其余 tick 仅处理「有活跃供需」或「被标记为脏」的终端，跳过无变化的终端。
        DemandLedger.advanceTick();
        boolean fullScan = DemandLedger.isFullScanTick();

        // 每个据点组独立处理
        for (StrongholdGroup group : saveData.groups().values()) {
            // 收集该组内所有终端的 StrongholdState
            Map<GlobalPos, StrongholdState> holdStates = new HashMap<>();
            for (Map.Entry<String, GlobalPos> holdEntry : group.holds().entrySet()) {
                GlobalPos hPos = holdEntry.getValue();
                Stronghold hold = StrongholdHelper.getHoldFromBlockEntity(serverLevel, hPos);
                if (hold == null) continue;
                boolean hasActive = !hold.holdState().transferResult().promotableDemands().isEmpty()
                        || !hold.holdState().transferResult().promotableSupplies().isEmpty();
                // 脏标记 + 兜底全量扫描：只处理脏终端或有活跃供需的终端
                if (!fullScan && !hasActive && !DemandLedger.isHoldDirty(hPos)) continue;
                holdStates.put(hPos, hold.holdState());
            }

            if (holdStates.isEmpty()) continue;

            // 维度匹配
            changed |= processScopeMatch(serverLevel, holdStates, StockRange.DIMENSION);
            // 全局匹配
            changed |= processScopeMatch(serverLevel, holdStates, StockRange.GLOBAL);

            // 处理完毕的终端标记为干净（除非仍有活跃供需，下一 tick 仍会处理）
            for (GlobalPos hPos : holdStates.keySet()) {
                DemandLedger.markHoldClean(hPos);
            }
        }

        if (changed) {
            StrongholdHelper.setHoldData(serverLevel, saveData);
        }
    }

    /**
     * 对给定的一组终端状态，执行一次特定范围的供需匹配，并更新终端数据。
     */
    private static boolean processScopeMatch(ServerLevel level,
                                             Map<GlobalPos, StrongholdState> holdStates,
                                             StockRange scope) {
        // 1. 从当前 HoldStates 中提取供需源
        List<DemandSource> demandSources = new ArrayList<>();
        List<SupplySource> supplySources = new ArrayList<>();
        for (Map.Entry<GlobalPos, StrongholdState> entry : holdStates.entrySet()) {
            GlobalPos pos = entry.getKey();
            TransferResult tr = entry.getValue().transferResult();
            for (DemandInfo d : tr.promotableDemands()) {
                demandSources.add(new DemandSource(d, pos));
            }
            for (SupplyInfo s : tr.promotableSupplies()) {
                supplySources.add(new SupplySource(s, pos));
            }
        }

        if (demandSources.isEmpty() || supplySources.isEmpty()) return false;

        // 2. 调用 TaskGenerator 进行配对（结果已按 scope 过滤剩余供需）
        DistributedResult distributed = TaskGenerator.matchAllDistributed(demandSources, supplySources, scope);

        // 3. 能量 / 工人检查 并分配任务
        Map<UUID, List<Task>> assignedTasksByWorker = new HashMap<>();
        for (Map.Entry<GlobalPos, List<CarryTask>> entry : distributed.newTasksByHold().entrySet()) {
            List<CarryTask> tasks = entry.getValue();

            for (CarryTask carryTask : tasks) {
                // 能量检查
                double energyCost = StrongholdHelper.getEnergyCost(
                        carryTask.getSourceTerminal(),
                        carryTask.getTargetTerminal()) * 2;
                if (!simulateEnergyCheck(level, carryTask.getSourceTerminal().pos(), carryTask.getTargetTerminal().pos(), energyCost))
                    continue;

                // 工人检查（至少一方有空闲）
                UUID idleWorker = getIdleWorkerForPair(level, carryTask.getSourceTerminal(), carryTask.getTargetTerminal());
                if (idleWorker == null) continue;

                // 通过检查，分配任务
                assignedTasksByWorker.computeIfAbsent(idleWorker, k -> new ArrayList<>()).add(carryTask);
            }
        }

        // 4. 将分配好的任务写入终端，并更新供需
        return applyResults(level, distributed.newDemands(), distributed.newSupplies(), assignedTasksByWorker, scope);
    }

    private static boolean simulateEnergyCheck(ServerLevel level, BlockPos pos1, BlockPos pos2, double required) {
        Optional<StrongholdGroup> group1 = StrongholdHelper.findHoldGroupContainingPos(level, pos1);
        Optional<StrongholdGroup> group2 = StrongholdHelper.findHoldGroupContainingPos(level, pos2);
        double available = 0;
        if (group1.isPresent()) available += group1.get().enderEnergy();
        if (group2.isPresent()) available += group2.get().enderEnergy();
        return available >= required;
    }

    private static UUID getIdleWorkerForPair(ServerLevel level, GlobalPos demTerminal, GlobalPos supTerminal) {
        UUID worker = getIdleWorkerAtTerminal(level, demTerminal);
        if (worker != null) return worker;
        return getIdleWorkerAtTerminal(level, supTerminal);
    }

    private static UUID getIdleWorkerAtTerminal(ServerLevel level, GlobalPos terminalPos) {
        Stronghold hold = StrongholdHelper.getHoldAt(level, terminalPos.pos());
        if (hold == null) return null;
        for (UUID uuid : hold.entities()) {
            if (!hold.holdState().activeTasks().containsKey(uuid.toString())) {
                if (level.getEntity(uuid) != null) {
                    return uuid;
                }
            }
        }
        return null;
    }

    /**
     * 将匹配结果应用到终端：更新供需列表，并添加新的活跃任务。
     */
    private static boolean applyResults(ServerLevel level,
                                        Map<GlobalPos, List<DemandInfo>> remainingDemands,
                                        Map<GlobalPos, List<SupplyInfo>> remainingSupplies,
                                        Map<UUID, List<Task>> assignedTasksByWorker,
                                        StockRange scope) {
        boolean changed = false;

        // 更新供需：只保留范围大于当前匹配级别的项，合并新剩余
        Set<GlobalPos> allPositions = new HashSet<>();
        allPositions.addAll(remainingDemands.keySet());
        allPositions.addAll(remainingSupplies.keySet());

        for (GlobalPos pos : allPositions) {
            StrongholdHelper.updateHoldAtPos(level, pos, hold -> {
                StrongholdState state = hold.holdState();
                TransferResult old = state.transferResult();

                List<DemandInfo> filteredDem = old.promotableDemands().stream()
                        .filter(d -> d.range().ordinal() > scope.ordinal())
                        .collect(Collectors.toList());
                List<SupplyInfo> filteredSup = old.promotableSupplies().stream()
                        .filter(s -> s.range().ordinal() > scope.ordinal())
                        .collect(Collectors.toList());

                filteredDem.addAll(remainingDemands.getOrDefault(pos, List.of()));
                filteredSup.addAll(remainingSupplies.getOrDefault(pos, List.of()));

                TransferResult newTr = new TransferResult(filteredDem, filteredSup);
                return hold.withHoldState(state.withMatchResult(newTr));
            });
            changed = true;
        }

        // 添加活跃任务
        if (!assignedTasksByWorker.isEmpty()) {
            Map<GlobalPos, Map<UUID, List<Task>>> terminalTaskMap =
                    groupTasksByTerminal(level, assignedTasksByWorker);
            for (var terminalEntry : terminalTaskMap.entrySet()) {
                GlobalPos pos = terminalEntry.getKey();
                Map<UUID, List<Task>> workerTasks = terminalEntry.getValue();
                StrongholdHelper.updateHoldAtPos(level, pos, hold -> {
                    StrongholdState state = hold.holdState();
                    return hold.withHoldState(state.appendActiveTasks(workerTasks));
                });
                changed = true;
            }
        }

        return changed;
    }

    private static Map<GlobalPos, Map<UUID, List<Task>>> groupTasksByTerminal(
            ServerLevel level, Map<UUID, List<Task>> workerTasks) {
        Map<GlobalPos, Map<UUID, List<Task>>> result = new HashMap<>();
        for (var entry : workerTasks.entrySet()) {
            UUID workerId = entry.getKey();
            GlobalPos terminalPos = getWorkerTerminal(level, workerId);
            if (terminalPos != null) {
                result.computeIfAbsent(terminalPos, k -> new HashMap<>())
                        .put(workerId, entry.getValue());
            }
        }
        return result;
    }

    private static GlobalPos getWorkerTerminal(ServerLevel level, UUID workerId) {
        SaveData data = StrongholdHelper.getHoldData(level);
        for (StrongholdGroup group : data.groups().values()) {
            for (GlobalPos pos : group.holds().values()) {
                Stronghold hold = StrongholdHelper.getHoldAt(level, pos.pos());
                if (hold != null && hold.entities().contains(workerId)) {
                    return pos;
                }
            }
        }
        return null;
    }
}