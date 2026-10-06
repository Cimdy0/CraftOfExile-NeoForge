package com.cimdy.coe.entity.goal.stronghold.helper.work;

import com.cimdy.coe.entity.goal.stronghold.impl.work.block.AbstractBlockWorkGoal;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.entity.goal.stronghold.behavior.IGoalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BreakingHandler {
    private final IGoalContext context;
    private BlockPos breakingPos;
    private float breakingProgress;
    private float breakingSpeed;
    private Deque<BlockPos> breakingQueue;
    /** 工作速度倍率（由所属行为每 tick 据实体工种适应度/速度写入，默认 1.0）。 */
    private double workSpeed = 1.0;

    public BreakingHandler(IGoalContext context) {
        this.context = context;
    }

    /** 设置本 tick 的工作速度倍率（由 AbstractBlockWorkGoal 依实体有效工作速度写入）。 */
    public void setWorkSpeed(double workSpeed) {
        this.workSpeed = Math.max(0.01, workSpeed);
    }

    public boolean hasActiveQueue() {
        return breakingQueue != null && !breakingQueue.isEmpty();
    }

    public void startBreaking(BlockPos target, ItemStack tool) {
        cancelBreaking();
        this.breakingPos = target;
        this.breakingProgress = 0f;
        BlockState state = context.getLevel().getBlockState(target);
        this.breakingSpeed = calcBreakSpeed(state, tool);
    }

    public boolean tickBreaking() {
        ServerLevel level = context.getLevel();
        if (breakingPos == null) return false;
        BlockState state = level.getBlockState(breakingPos);
        if (state.isAir()) { cancelBreaking(); return false; }
        context.lookAt(breakingPos);
        context.getMob().swing(InteractionHand.MAIN_HAND);
        breakingProgress += breakingSpeed * (float) workSpeed;
        if (breakingProgress >= 1.0f) {
            ItemStack tool = context.getMob().getMainHandItem();
            breakAndCollect(level, breakingPos, tool);
            cancelBreaking();
            return true;
        }
        return false;
    }

    public void startBreakingQueue(List<BlockPos> targets, ItemStack tool) {
        this.breakingQueue = new ArrayDeque<>(targets);
        if (!targets.isEmpty()) startBreaking(targets.getFirst(), tool);
    }

    public boolean tickBreakingQueue() {
        if (breakingQueue == null || breakingQueue.isEmpty()) { cancelBreaking(); return true; }
        if (breakingPos != null) {
            if (!tickBreaking()) return false;
            breakingQueue.poll();
        }
        if (!breakingQueue.isEmpty()) {
            startBreaking(breakingQueue.peek(), context.getMob().getMainHandItem());
            return false;
        }
        return true;
    }

    public void cancelBreakingQueue() {
        cancelBreaking();
        if (breakingQueue != null) {
            breakingQueue.clear();
            breakingQueue = null;
        }
    }

    public void cancelBreaking() {
        breakingPos = null;
        breakingProgress = 0f;
    }

    public List<ItemStack> breakBlock(ServerLevel level, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return List.of();
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, context.getMob(), tool);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (!tool.isEmpty() && tool.isDamageableItem()) tool.hurtAndBreak(1, context.getMob(), EquipmentSlot.MAINHAND);
        level.levelEvent(2001, pos, Block.getId(state));
        return drops;
    }

    public void breakAndCollect(ServerLevel level, BlockPos pos, ItemStack tool) {
        for (ItemStack drop : breakBlock(level, pos, tool)) {
            CapacityHelper.addItem(context.getMob(), drop.copy());
        }
    }

    public List<ItemStack> breakBlocksAndReplant(ServerLevel level, List<BlockPos> positions, ItemStack tool,
                                                 BlockPos plantPos, Predicate<ItemStack> itemFilter,
                                                 Supplier<ItemStack> fallbackItem) {
        List<ItemStack> allDrops = new ArrayList<>();
        int index = 0;
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            allDrops.addAll(Block.getDrops(state, level, pos, null, context.getMob(), tool));
            if (index % 3 == 0) {
                level.destroyBlock(pos, false);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            index++;
        }
        ItemStack seed = consumeOne(allDrops, itemFilter);
        if (seed.isEmpty()) seed = fallbackItem.get();
        if (!seed.isEmpty()) {
            Block block = Block.byItem(seed.getItem());
            if (block != Blocks.AIR && block.defaultBlockState().canSurvive(level, plantPos)) {
                level.setBlock(plantPos, block.defaultBlockState(), 3);
                level.playSound(null, plantPos, block.defaultBlockState().getSoundType().getPlaceSound(),
                        SoundSource.BLOCKS, 1.0f, 0.8f);
            }
        }
        return allDrops;
    }

    private float calcBreakSpeed(BlockState state, ItemStack tool) {
        float toolSpeed = tool.isEmpty() ? 1.0f : tool.getDestroySpeed(state);
        if (toolSpeed <= 0f) toolSpeed = 1.0f;
        float hardness = state.getBlock().defaultDestroyTime();
        if (hardness <= 0f) hardness = 1.0f;
        int divisor = tool.isCorrectToolForDrops(state) ? 30 : 100;
        return toolSpeed / hardness / divisor;
    }

    private ItemStack consumeOne(List<ItemStack> drops, Predicate<ItemStack> filter) {
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
}