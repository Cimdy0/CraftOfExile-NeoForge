package com.cimdy.coe.entity.goal.stronghold.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.EnderChargeTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

/**
 * 末影充能行为：移动到末影方块旁，周期性注入末影能量，饥饿耗尽则结束。
 * 基于 {@link AbstractChargeGoal} 实现，仅提供四个扩展点。
 */
public class EnderChargeGoal extends AbstractChargeGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, EnderChargeGoal::new);

    private int tickCounter = 0;

    public EnderChargeGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return EnderChargeTask.class;
    }

    @Override
    protected BlockPos getWorkPos() {
        return ((EnderChargeTask) firstTask).getWorkPos();
    }

    @Override
    public void stop() {
        super.stop();
        tickCounter = 0;
    }

    @Override
    protected void onMaintain(ServerLevel level, BlockPos pos) {
        // 每秒发能一次 (20 tick)
        tickCounter++;
        if (tickCounter % 20 == 0) {
            StrongholdHelper.addEnderEnergy(level, pos, 1);

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 10, 0.5, 0.5, 0.5, 0.02);
        }
    }
}
