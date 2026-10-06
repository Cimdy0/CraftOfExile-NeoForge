package com.cimdy.coe.entity.goal.stronghold.impl.work.block;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.ForestryTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

/**
 * 林业行为：在工作区域内寻找成熟树木，砍伐原木并处理树叶（补种）。
 * 基于 {@link AbstractBlockWorkGoal}（破坏队列模式）实现，仅提供五个扩展点。
 */
public class ForestryGoal extends AbstractBlockWorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, ForestryGoal::new);

    /** 破坏队列完成后待处理的树叶（在 collectBlocks 时收集）。 */
    private final List<BlockPos> pendingLeaves = new ArrayList<>();

    public ForestryGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return ForestryTask.class;
    }

    @Override
    protected Predicate<ItemStack> toolPredicate() {
        return s -> s.is(ItemTags.AXES);
    }

    @Override
    protected boolean isTarget(ServerLevel level, BlockPos pos) {
        return ForestryTask.isMatureWood(level, pos, level.getBlockState(pos));
    }

    @Override
    protected Comparator<BlockPos> targetComparator() {
        return Comparator.comparingInt(BlockPos::getY);
    }

    @Override
    protected List<BlockPos> collectBlocks(ServerLevel level, BlockPos root) {
        Set<BlockPos> treeBlocks = collectTreeBlocks(level, root);
        List<BlockPos> logs = new ArrayList<>();
        pendingLeaves.clear();
        for (BlockPos pos : treeBlocks) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) || state.is(Blocks.MUSHROOM_STEM)) logs.add(pos);
            else if (state.is(BlockTags.LEAVES)) pendingLeaves.add(pos);
        }
        logs.sort(Comparator.comparingInt(BlockPos::getY));
        return logs;
    }

    @Override
    protected void onBlocksBroken(ServerLevel level, BlockPos root) {
        processLeaves(level, root);
    }

    // ---------- 树叶枯萎与补种 ----------
    private void processLeaves(ServerLevel level, BlockPos root) {
        ItemStack tool = mob.getMainHandItem();
        List<ItemStack> leafDrops = breakBlocksAndReplant(
                level, pendingLeaves, tool,
                root,                       // 在原树根位置补种
                stack -> stack.is(ItemTags.SAPLINGS),
                () -> getFallbackSapling(pendingLeaves.isEmpty() ? null : pendingLeaves.getFirst()));
        for (ItemStack drop : leafDrops) {
            CapacityHelper.addItem(mob, drop.copy());
        }
    }

    private ItemStack getFallbackSapling(BlockPos leafPos) {
        if (leafPos == null) return ItemStack.EMPTY;
        BlockState state = mob.level().getBlockState(leafPos);
        if (state.isAir()) return ItemStack.EMPTY;
        Block block = state.getBlock();
        if (block == Blocks.OAK_LEAVES) return new ItemStack(Blocks.OAK_SAPLING);
        if (block == Blocks.SPRUCE_LEAVES) return new ItemStack(Blocks.SPRUCE_SAPLING);
        if (block == Blocks.BIRCH_LEAVES) return new ItemStack(Blocks.BIRCH_SAPLING);
        if (block == Blocks.JUNGLE_LEAVES) return new ItemStack(Blocks.JUNGLE_SAPLING);
        if (block == Blocks.ACACIA_LEAVES) return new ItemStack(Blocks.ACACIA_SAPLING);
        if (block == Blocks.DARK_OAK_LEAVES) return new ItemStack(Blocks.DARK_OAK_SAPLING);
        if (block == Blocks.MANGROVE_LEAVES) return new ItemStack(Blocks.MANGROVE_PROPAGULE);
        if (block == Blocks.CHERRY_LEAVES) return new ItemStack(Blocks.CHERRY_SAPLING);
        return ItemStack.EMPTY;
    }

    // ---------- 树木收集 ----------
    private Set<BlockPos> collectTreeBlocks(ServerLevel level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        if (!ForestryTask.isMatureWood(level, start, level.getBlockState(start))) return visited;
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (BlockPos neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                if (Math.abs(start.getX() - neighbor.getX()) > 12 ||
                        Math.abs(start.getY() - neighbor.getY()) > 12 ||
                        Math.abs(start.getZ() - neighbor.getZ()) > 12) continue;
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.is(BlockTags.LOGS) || neighborState.is(Blocks.MUSHROOM_STEM) || neighborState.is(BlockTags.LEAVES)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -1; dz <= 1; dz++)
                    if (dx != 0 || dy != 0 || dz != 0)
                        neighbors.add(pos.offset(dx, dy, dz));
        return neighbors;
    }
}
