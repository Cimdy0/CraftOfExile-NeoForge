package com.cimdy.coe.entity.goal.stronghold.impl.work.block;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.FarmerTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 农耕行为：在工作区域内寻找成熟作物，收割后补种并存入绑定容器。
 * 基于 {@link AbstractBlockWorkGoal}（同步处理模式）实现，仅提供五个扩展点。
 */
public class FarmerGoal extends AbstractBlockWorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, FarmerGoal::new);

    public FarmerGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return FarmerTask.class;
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return null; // 农耕无需工具
    }

    @Override
    protected boolean useBreakingQueue() {
        return false; // 同步收割：抵达作物即当场收割并补种
    }

    @Override
    protected boolean isTarget(ServerLevel level, BlockPos pos) {
        return FarmerTask.isMatureCrop(level, pos, level.getBlockState(pos));
    }

    @Override
    protected Comparator<BlockPos> targetComparator() {
        return Comparator.comparingDouble(p -> mob.distanceToSqr(Vec3.atCenterOf(p)));
    }

    @Override
    protected List<BlockPos> collectBlocks(ServerLevel level, BlockPos root) {
        return List.of(root);
    }

    @Override
    protected void processSynchronously(ServerLevel level, BlockPos pos) {
        harvestCrop(level, pos);
    }

    // --------------------------------------------------
    //  收割
    // --------------------------------------------------
    private void harvestCrop(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!FarmerTask.isMatureCrop(level, pos, state)) {
            releaseWorkPosition();
            return;
        }

        // 特殊处理：南瓜和西瓜 仙人掌花 (直接破坏)
        if (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON) || state.is(Blocks.CACTUS_FLOWER)) {
            breakAndCollect(level, pos, ItemStack.EMPTY);
            releaseWorkPosition();
            return;
        }

        // 特殊处理：甘蔗/竹子/仙人掌
        if (isTallPlantTop(state, pos, level)) {
            harvestTallPlant(level, pos);
            releaseWorkPosition();
            return;
        }

        // 常规作物：获取掉落物并消耗种子
        List<ItemStack> drops = breakBlock(level, pos, mob.getMainHandItem());
        consumeOne(drops, stack -> stack.is(net.neoforged.neoforge.common.Tags.Items.SEEDS));

        // 产物放入背包
        for (ItemStack drop : drops) {
            CapacityHelper.addItem(mob, drop.copy());
        }

        // 重置方块状态 (补种)
        resetCropState(level, pos, state);

        releaseWorkPosition();
    }

    private void harvestTallPlant(ServerLevel level, BlockPos startPos) {
        BlockState state = level.getBlockState(startPos);
        Block block = state.getBlock();
        BlockPos current = startPos;
        while (level.isInWorldBounds(current) && level.getBlockState(current).is(block)) {
            lookAt(current);
            breakAndCollect(level, current, mob.getMainHandItem());
            current = current.above();
        }
    }

    private boolean isTallPlantTop(BlockState state, BlockPos pos, ServerLevel level) {
        Block block = state.getBlock();
        if (block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO || block == Blocks.CACTUS) {
            return level.getBlockState(pos.below()).is(block);
        }
        return false;
    }

    private void resetCropState(ServerLevel level, BlockPos pos, BlockState oldState) {
        Block block = oldState.getBlock();
        BlockState newState;
        if (block instanceof NetherWartBlock) {
            newState = oldState.setValue(NetherWartBlock.AGE, 0);
        } else if (block instanceof CocoaBlock) {
            newState = oldState.setValue(CocoaBlock.AGE, 0);
        } else if (block instanceof CaveVinesBlock || block instanceof CaveVinesPlantBlock) {
            newState = oldState.setValue(CaveVinesBlock.BERRIES, false);
        } else {
            newState = block.defaultBlockState();
        }
        placeBlock(level, pos, newState);
    }
}
