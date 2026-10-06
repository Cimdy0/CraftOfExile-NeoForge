package com.cimdy.coe.data.codec.stronghold.task.impl;

import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTaskRegistry;
import com.cimdy.coe.data.codec.stronghold.task.helper.state.StrongholdState;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.AreaSite;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class WorkTask extends BasicTask {
    protected final BlockPos workPos;
    protected int pendingWorkers;
    /** 本任务已转化产物数量（影响需求限量进度，随任务序列化持久化，重启后可无缝续作）。 */
    protected int produced = 0;

    protected WorkTask(Optional<Task> next, BlockPos workPos, int maxWorkers) {
        super(next);
        this.workPos = workPos;
        // 0 表示不限制工人，此时用最大整数代表“无限制”
        this.pendingWorkers = (maxWorkers <= 0) ? Integer.MAX_VALUE : maxWorkers;
    }

    public Predicate<BlockState> getPredicate() {
        return state -> true;
    }

    /** 该任务所需的站点类型（对应 WorkGoal 据此严格校验 Site↔Task 对应）。默认 AreaSite。 */
    public Class<? extends Site> requiredSiteType() {
        return AreaSite.class;
    }

    /**
     * 任务能否在该站点执行。默认按 {@link #requiredSiteType()} 单类校验；
     * 转化类任务（如烹饪可发生在营火/烟熏炉/熔炉）覆写为「匹配任一站点类型」，
     * 以支持「一个转化类型对应多个站点类型」的数据驱动声明。
     */
    public boolean matchesSite(Site site) {
        return requiredSiteType().isInstance(site);
    }

    // 根据站点工作方块生成具体的 WorkTask 子类任务（委托给 WorkTaskRegistry，数据驱动、可扩展）
    public static Task generateTask(ServerLevel level, Site site) {
        return WorkTaskRegistry.generate(level, site);
    }

    public abstract WorkTask createSubTask(int workers);

    @Override
    public Task split(int amount) {
        if (amount <= 0 || pendingWorkers <= 0) return this;
        // 无限制时，拆分出一个大小为1的子任务
        if (pendingWorkers == Integer.MAX_VALUE) return createSubTask(1);
        int taken = Math.min(amount, pendingWorkers);
        pendingWorkers -= taken;
        return createSubTask(taken);
    }

    // 分配实体时：注册到站点
    @Override
    public void onWorkerAssigned(ServerLevel level, GlobalPos terminalPos, UUID entityId, UUID taskId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> {
            StrongholdState state = hold.holdState();
            return state.getSite(workPos).map(site -> {
                site.addWorker(entityId);
                return hold.withHoldState(state.withSite(site));
            }).orElse(hold);
        });
    }

    // 任务完成：从站点移除实体
    @Override
    public void setTaskCompletedAtTerminal(ServerLevel level, GlobalPos terminalPos, UUID entityId, UUID taskId) {
        super.setTaskCompletedAtTerminal(level, terminalPos, entityId, taskId);
        removeEntityFromSite(level, terminalPos, entityId);
    }

    public void removeEntityFromSite(ServerLevel level, GlobalPos terminalPos, UUID entityId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> {
            StrongholdState state = hold.holdState();
            return state.getSite(workPos).map(site -> {
                site.removeWorker(entityId);
                return hold.withHoldState(state.withSite(site));
            }).orElse(hold);
        });
    }

    // 管理具体工作位置
    public void occupyWorkPosition(ServerLevel level, GlobalPos terminalPos, UUID entityId, BlockPos workPosition) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> {
            StrongholdState state = hold.holdState();
            return state.getSite(workPos).map(site -> {
                if(!(site instanceof AreaSite areaSite)) return hold;
                areaSite.withEntityWorkPosition(entityId, workPosition);
                return hold.withHoldState(state.withSite(areaSite));
            }).orElse(hold);
        });
    }

    public void releaseWorkPosition(ServerLevel level, GlobalPos terminalPos, UUID entityId) {
        if (terminalPos == null) return;
        StrongholdHelper.updateHoldAtPos(level, terminalPos, hold -> {
            StrongholdState state = hold.holdState();
            return state.getSite(workPos).map(site -> {
                if(!(site instanceof AreaSite areaSite)) return hold;
                areaSite.clearEntityWorkPosition(entityId);
                return hold.withHoldState(state.withSite(areaSite));
            }).orElse(hold);
        });
    }

    // 获取当前工作站点（使用终端数据）
    public Optional<Site> getSite(ServerLevel level, GlobalPos terminalPos) {
        if (terminalPos == null) return Optional.empty();
        BlockEntity be = level.getBlockEntity(terminalPos.pos());
        if (be instanceof TerminalBlockEntity terminal) {
            Stronghold hold = terminal.getData(AttachmentRegistry.STRONGHOLD);
            return hold.getSite(workPos);
        }
        return Optional.empty();
    }

    // 获取当前站点已分配工人数
    public int getCurrentAssignedWorkers(ServerLevel level, GlobalPos terminalPos) {
        return getSite(level, terminalPos).map(Site::getWorkers).map(List::size).orElse(0);
    }

    public int getMaxWorkersFromSite(ServerLevel level, GlobalPos terminalPos) {
        return getSite(level, terminalPos).map(Site::getMaxWorkers).orElse(0);
    }

    @Override
    public boolean validate(ServerLevel level) {
        if (!level.isLoaded(workPos)) return false;
        if (StrongholdHelper.findHoldGroupContainingPos(level, workPos).isEmpty()) return false;
        if (!getPredicate().test(level.getBlockState(workPos))) return false;
        // 物品采集类任务：若站点存储容器已满则失效，避免产物滞留工人背包
        if (needsItemStorage()) {
            Stronghold hold = StrongholdHelper.getPosInHold(level, workPos);
            if (hold != null) {
                Optional<Site> siteOpt = hold.getSite(workPos);
                if (siteOpt.isPresent()) {
                    List<BlockPos> storages = siteOpt.get().getStorages();
                    if (!storages.isEmpty() && !StrongholdHelper.storageHasRoom(level, storages)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 该任务是否依赖「物品存储容器」承接产物。默认 true（采集 / 转化类均产出物品）；
     * 纯能量类任务（如末影充能）覆盖为 false，避免被物品容量门控误杀。
     */
    public boolean needsItemStorage() {
        return true;
    }
    public BlockPos getWorkPos() { return workPos; }
    public int getPendingWorkers() { return pendingWorkers; }
    public void setPendingWorkers(int pendingWorkers) { this.pendingWorkers = Math.max(0, pendingWorkers); }

    public int getProduced() { return produced; }
    public void setProduced(int produced) { this.produced = Math.max(0, produced); }

    @Override public int getVolume() { return pendingWorkers; }
    @Override public boolean isSplittable() { return true; }
    @Override public Optional<?> getGroupingKey() { return Optional.of(workPos); }
    @Override public BlockPos getTargetPositionForEntity(LivingEntity entity) { return workPos; }
}