package com.cimdy.coe.entity.goal.stronghold.goal;

import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.entity.goal.stronghold.helper.*;
import com.cimdy.coe.entity.goal.stronghold.helper.interaction.*;
import com.cimdy.coe.entity.goal.stronghold.helper.queue.*;
import com.cimdy.coe.entity.goal.stronghold.helper.work.*;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TaskGoal extends StrongholdGoal implements IGoalContext {
    protected Task firstTask;
    protected UUID taskId;

    // 组件
    private final ContainerInteractionHandler containerInteraction;
    private final SmartDepositHandler smartDeposit;
    private final ToolManager toolManager;
    protected final BreakingHandler breakingHandler;
    private final FetchQueueHandler fetchQueueHandler;
    private final CraftQueueHandler craftQueueHandler;

    public TaskGoal(Mob mob) {
        super(mob);
        containerInteraction = new ContainerInteractionHandler(this);
        smartDeposit = new SmartDepositHandler(this);
        toolManager = new ToolManager(this);
        breakingHandler = new BreakingHandler(this);
        fetchQueueHandler = new FetchQueueHandler(this);
        craftQueueHandler = new CraftQueueHandler(this);
    }

    // ========== IGoalContext 实现 ==========
    @Override public Mob getMob() { return mob; }
    @Override public ServerLevel getLevel() { return (ServerLevel) mob.level(); }
    @Override public GlobalPos getTerminalPos() { return terminalPos; }
    @Override public com.cimdy.coe.data.codec.stronghold.Stronghold getHold() { return hold; }
    @Override
    public void lookAt(BlockPos target) { mob.getLookControl().setLookAt(Vec3.atCenterOf(target)); }
    /** 移动到目标方块,返回 true 表示已到达（距离 ≤ closeDist）。 */
    public boolean moveToInteractionBlock(BlockPos target) {
        return this.moveTo(this.mob, target, this.mob.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
    }

    @Override
    public boolean moveToNearBlock(BlockPos target) {
        return this.moveTo(this.mob, target, 3.0);
    }

    @Override public List<BlockPos> getStorageBlocks() { return List.of(); } // 子类覆盖

    // ========== 基础生命周期 ==========
    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (!this.hold.holdState().activeTasks().containsKey(this.mob.getStringUUID())) return false;
        var tasks = this.hold.holdState().activeTasks().get(this.mob.getStringUUID());
        if (tasks.isEmpty()) return false;
        ActiveTask activeTask = tasks.getFirst();
        if (activeTask.status() != TaskStatus.ASSIGNED) return false;
        this.firstTask = activeTask.task();
        this.taskId = activeTask.taskId();
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        this.containerInteraction.reset();
        this.firstTask = null;
        this.taskId = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.firstTask != null) {
            double cost = this.firstTask.getHungerCostPerTick();
            StrongholdHelper.modifyHunger(this.mob, -cost);
        }
    }

    protected void updateTask() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        this.firstTask.updateTaskAtTerminal(serverLevel,
                terminalPos, this.mob.getUUID(), this.taskId);
    }

    protected void setTaskFailed() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        this.firstTask.setTaskFailedAtTerminal(serverLevel,
                terminalPos, this.mob.getUUID(), this.taskId);
        this.stop();
    }

    protected void setTaskCompleted() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        this.firstTask.setTaskCompletedAtTerminal(serverLevel,
                terminalPos, this.mob.getUUID(), this.taskId);
        this.stop();
    }

    // ========== 单个合成队列 ==========
    protected void startCraftQueue(BlockPos craftSite, Recipe<?> recipe) { this.craftQueueHandler.startCraftQueue(craftSite, recipe); }
    protected Result tickCraftQueue() { return this.craftQueueHandler.tick(); }
    protected boolean isCraftQueueActive() { return this.craftQueueHandler.isActive(); }

    // ========== 容器交互（CarryGoal 用） ==========
    public ContainerInteractionHandler.InteractionState getInteractionState() { return containerInteraction.getInteractionState(); }
    @Override public void startContainerInteraction(BlockPos target, Consumer<ServerLevel> action) { containerInteraction.start(target, action); }
    @Override public boolean tickContainerInteraction(ServerLevel level) { return containerInteraction.tick() == Result.SKIP; }

    // ========== 智能存储 ==========
    protected void startPriorityDeposit(List<BlockPos> candidates) { smartDeposit.start(candidates); }
    protected boolean tickPriorityDeposit() { return smartDeposit.tick(); }
    protected boolean isDepositing() { return smartDeposit.isActive(); }

    // ========== 物品获取队列 ==========
    protected void startFetchQueue(List<BlockPos> storageCandidates, List<ItemCount> items) {this.fetchQueueHandler.startFetchQueue(storageCandidates, items);}
    protected Result tickFetchQueue(ServerLevel level) {return this.fetchQueueHandler.tick();}
    protected boolean isFetchQueueActive() {return this.fetchQueueHandler.isActive();}

    // ========== 工具获取与归还 ==========
    protected Result fetchToolTick(Predicate<ItemStack> isTool) { return toolManager.fetchToolTick(isTool); }
    protected boolean returnToolTick(EquipmentSlot slot) { return toolManager.returnToolTick(slot); }

    // ========== 破坏系统 ==========
    protected boolean hasBreakingQueue() { return breakingHandler.hasActiveQueue(); }
    protected void startBreaking(BlockPos target, ItemStack tool) { breakingHandler.startBreaking(target, tool); }
    protected boolean tickBreaking() { return breakingHandler.tickBreaking(); }
    protected void startBreakingQueue(List<BlockPos> targets, ItemStack tool) { breakingHandler.startBreakingQueue(targets, tool); }
    protected boolean tickBreakingQueue() { return breakingHandler.tickBreakingQueue(); }
    protected void cancelBreakingQueue() { breakingHandler.cancelBreakingQueue(); }
    protected void cancelBreaking() { breakingHandler.cancelBreaking(); }
    protected List<ItemStack> breakBlock(ServerLevel level, BlockPos pos, ItemStack tool) { return breakingHandler.breakBlock(level, pos, tool); }
    protected void breakAndCollect(ServerLevel level, BlockPos pos, ItemStack tool) { breakingHandler.breakAndCollect(level, pos, tool); }
    protected List<ItemStack> breakBlocksAndReplant(ServerLevel level, List<BlockPos> positions, ItemStack tool,
                                                    BlockPos plantPos, Predicate<ItemStack> filter, Supplier<ItemStack> fallback) {
        return breakingHandler.breakBlocksAndReplant(level, positions, tool, plantPos, filter, fallback);
    }
    protected void placeBlock(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
        level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,
                (state.getSoundType().getVolume() + 1.0F) / 2.0F, state.getSoundType().getPitch() * 0.8F);
    }
    protected void destroyBlock(ServerLevel level, BlockPos pos, boolean effect) {
        if (effect) level.destroyBlock(pos, false);
        else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    // 原有的便捷方法
    protected int getCarriedAmount(ItemResource resource) {
        return CapacityHelper.getStacks(mob).stream()
                .filter(resource::matches)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    protected ItemStack consumeOne(List<ItemStack> drops, Predicate<ItemStack> filter) {
        for (Iterator<ItemStack> it = drops.iterator(); it.hasNext();) {
            ItemStack stack = it.next();
            if (filter.test(stack)) {
                ItemStack result = stack.copy();
                result.setCount(1);
                if (stack.getCount() > 1) stack.shrink(1);
                else it.remove();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    /** 拾取实体周围半径内的掉落物到工人背包（剪毛/狩猎等产生 ItemEntity 的工种使用）。
     *  获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描）。 */
    protected void collectNearbyItems(double radius) {
        ServerLevel level = (ServerLevel) mob.level();
        AABB box = new AABB(mob.blockPosition()).inflate(radius);
        for (ItemEntity ie : ExpressionContext.of(level, mob).queryEntities(EntityQuery.box(ItemEntity.class, box))) {
            ItemStack stack = ie.getItem();
            if (stack.isEmpty()) continue;
            int added = CapacityHelper.addItem(mob, stack);
            if (added >= stack.getCount()) {
                ie.discard();
            } else {
                stack.setCount(stack.getCount() - added);
            }
        }
    }
}