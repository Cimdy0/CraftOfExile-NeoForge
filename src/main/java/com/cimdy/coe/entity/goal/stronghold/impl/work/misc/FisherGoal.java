package com.cimdy.coe.entity.goal.stronghold.impl.work.misc;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.FisherTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoal;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

/**
 * 钓鱼行为：在工作区域内寻找水源，抛竿垂钓获取鱼类。
 */
public class FisherGoal extends WorkGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, FisherGoal::new);

    private FisherTask fisherTask;

    @Nullable
    private BlockPos waterPos;
    private int fishTimer = 0;
    private static final int FISH_INTERVAL = 100; // 约 5 秒一竿

    public FisherGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return FisherTask.class;
    }

    @Override
    protected void onWorkResolved(WorkTask task, Site site) {
        this.fisherTask = (FisherTask) task;
    }

    @Override
    public void stop() {
        super.stop();
        waterPos = null;
        fishTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();

        // 1. 存款阶段（钓到的鱼存入绑定容器）
        if (isDepositing()) {
            tickPriorityDeposit();
            return;
        }

        // 2. 寻找水源
        if (waterPos == null) {
            Optional<BlockPos> w = findWater(level);
            if (w.isEmpty()) {
                StrongholdEntity data = mob.getData(AttachmentRegistry.ENTITY_DATA);
                if (!data.stacks().isEmpty()) {
                    startPriorityDeposit(site.getStorages());
                } else {
                    setTaskCompleted();
                }
                return;
            }
            waterPos = w.get();
        }

        // 3. 移动到水边站位
        BlockPos stand = adjacentSolid(level, waterPos);
        if (stand == null || !moveToInteractionBlock(stand)) return;

        // 4. 垂钓计时
        fishTimer++;
        if (fishTimer >= FISH_INTERVAL) {
            fishTimer = 0;
            Item fish = pickFish();
            CapacityHelper.addItem(mob, new ItemStack(fish, 1));
            startPriorityDeposit(site.getStorages());
        }
    }

    private Item pickFish() {
        int r = mob.getRandom().nextInt(10);
        if (r < 5) return Items.COD;
        if (r < 8) return Items.SALMON;
        if (r < 9) return Items.TROPICAL_FISH;
        return Items.PUFFERFISH;
    }

    private Optional<BlockPos> findWater(ServerLevel level) {
        return scanWorkArea(
                pos -> level.getBlockState(pos).is(Blocks.WATER),
                Comparator.comparingInt(p -> p.distManhattan(mob.blockPosition())));
    }

    private BlockPos adjacentSolid(ServerLevel level, BlockPos water) {
        for (BlockPos off : new BlockPos[]{
                water.north(), water.south(), water.east(), water.west(), water.above()
        }) {
            BlockState state = level.getBlockState(off);
            if (!state.is(Blocks.WATER) && state.isSolid()) {
                return off;
            }
        }
        return null;
    }
}
