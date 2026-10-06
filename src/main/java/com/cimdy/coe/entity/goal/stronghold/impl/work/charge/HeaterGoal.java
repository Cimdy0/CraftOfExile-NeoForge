package com.cimdy.coe.entity.goal.stronghold.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.HeaterTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.mixin.AbstractFurnaceBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 加热行为：移动到熔炉旁，保持其燃烧状态，已点燃足够久则结束。
 * 基于 {@link AbstractChargeGoal} 实现，仅提供四个扩展点。
 */
public class HeaterGoal extends AbstractChargeGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, HeaterGoal::new);

    public HeaterGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return HeaterTask.class;
    }

    @Override
    protected BlockPos getWorkPos() {
        return ((HeaterTask) firstTask).getWorkPos();
    }

    @Override
    protected boolean isWorkDone(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) {
            return ((AbstractFurnaceBlockEntityAccessor) furnace).getLitTimeRemaining() > 32767;
        }
        return false;
    }

    @Override
    protected void onMaintain(ServerLevel level, BlockPos pos) {
        if (StrongholdHelper.getHunger(mob) <= 0) {
            setTaskCompleted();
            return;
        }
        if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) {
            AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;
            int time = accessor.getLitTimeRemaining();
            if (time > 32767) {
                setTaskCompleted();
                return;
            }
            accessor.setLitTimeRemaining(time + 20);
            accessor.setLitTotalTime(time + 20);
            if (!furnace.getBlockState().getValue(AbstractFurnaceBlock.LIT)) {
                BlockState state = furnace.getBlockState().setValue(AbstractFurnaceBlock.LIT, true);
                level.setBlock(pos, state, 3);
            }
        }
    }
}
