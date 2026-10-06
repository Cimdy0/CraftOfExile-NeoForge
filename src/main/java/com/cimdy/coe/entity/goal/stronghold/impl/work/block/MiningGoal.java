package com.cimdy.coe.entity.goal.stronghold.impl.work.block;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.MiningTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 采矿行为：在工作区域内寻找可被镐开采的方块，开采后存入绑定容器。
 * 基于 {@link AbstractBlockWorkGoal}（破坏队列模式）实现，仅提供五个扩展点。
 */
public class MiningGoal extends AbstractBlockWorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, MiningGoal::new);

    public MiningGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return MiningTask.class;
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return s -> s.is(ItemTags.PICKAXES);
    }

    @Override
    protected boolean isTarget(ServerLevel level, BlockPos pos) {
        if (pos.equals(site.getSitePos())) return false;
        if (site.getStorages().contains(pos)) return false;
        return MiningTask.isMineable(level.getBlockState(pos));
    }

    @Override
    protected Comparator<BlockPos> targetComparator() {
        return Comparator.comparingInt(BlockPos::getY);
    }

    @Override
    protected List<BlockPos> collectBlocks(ServerLevel level, BlockPos root) {
        return List.of(root);
    }
}
