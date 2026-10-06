package com.cimdy.coe.entity.goal.stronghold.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.FEChargeTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * FE 充能行为：移动到 FE 机器旁，周期性注入 Forge Energy，能量已满则结束。
 * 基于 {@link AbstractChargeGoal} 实现，仅提供四个扩展点。
 */
public class FEChargeGoal extends AbstractChargeGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, FEChargeGoal::new);

    private int tickCounter = 0;
    private static final int CHARGE_AMOUNT = 100;
    private static final int PARTICLE_INTERVAL = 10;

    public FEChargeGoal(Mob mob) {
        super(mob);
    }

    @Override
    protected Class<? extends Task> taskType() {
        return FEChargeTask.class;
    }

    @Override
    protected BlockPos getWorkPos() {
        return ((FEChargeTask) firstTask).getWorkPos();
    }

    @Override
    public void stop() {
        super.stop();
        tickCounter = 0;
    }

    @Override
    protected boolean isWorkDone(ServerLevel level, BlockPos pos) {
        EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
        return energy != null && energy.getAmountAsInt() >= energy.getCapacityAsInt();
    }

    @Override
    protected void onMaintain(ServerLevel level, BlockPos pos) {
        EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
        if (energy == null) {
            setTaskCompleted();
            return;
        }
        tickCounter++;
        if (tickCounter % PARTICLE_INTERVAL == 0) {
            if (!level.isClientSide()) {
                try (Transaction txn = Transaction.openRoot()) {
                    int received = energy.insert(CHARGE_AMOUNT, txn);
                    if (received > 0) {
                        txn.commit();
                    }
                }
            }
            spawnChargeParticles(level, pos);
        }
    }

    private void spawnChargeParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 5; i++) {
            double dx = (level.getRandom().nextDouble() - 0.5) * 0.8;
            double dy = level.getRandom().nextDouble() * 0.6;
            double dz = (level.getRandom().nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, dx, dy, dz, 0.1);
        }
    }
}
