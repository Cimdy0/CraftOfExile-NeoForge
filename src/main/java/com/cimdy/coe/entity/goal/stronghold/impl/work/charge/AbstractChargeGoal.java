package com.cimdy.coe.entity.goal.stronghold.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.entity.goal.stronghold.goal.TaskGoal;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

/**
 * 方块充能 / 维持类工作行为模板（方案 D 的进一步抽象）。
 * <p>
 * 将「移动到工作方块 → 周期性维持（充能 / 点燃）→ 饥饿耗尽或工作完成则结束」这一高频循环抽取为模板，
 * 末影充能 / FE 充能 / 熔炉加热等「对单个方块持续作用」工种共用此基类。新增同类工种只需提供：
 * <ul>
 *   <li>{@link #taskType()} —— 该行为对应的任务类型；</li>
 *   <li>{@link #getWorkPos()} —— 需要维持的工作方块位置；</li>
 *   <li>{@link #onMaintain(ServerLevel, BlockPos)} —— 每 tick 的维持动作（充能 / 点燃）；</li>
 *   <li>{@link #isWorkDone(ServerLevel, BlockPos)} —— 除饥饿外的「工作已完成」判定（如能量已满 / 已点燃足够久），默认 false。</li>
 * </ul>
 * 从而使新增一个充能工种从「复制约 40 行循环」降为「四个方法 + 一行注册」。
 * <p>
 * 扩展点：{@link #isWorkDone(ServerLevel, BlockPos)} 默认 false（仅由饥饿结束，如末影充能）；
 * 饥饿耗尽由模板统一判定。
 */
public abstract class AbstractChargeGoal extends TaskGoal {
    public AbstractChargeGoal(Mob mob) {
        super(mob);
    }

    /** 该行为对应的任务类型（用于通用 canUse 匹配）。 */
    protected abstract Class<? extends Task> taskType();

    /** 需要维持的工作方块位置。 */
    protected abstract BlockPos getWorkPos();

    /** 扩展点：每 tick 的维持动作（充能 / 点燃等）。 */
    protected abstract void onMaintain(ServerLevel level, BlockPos pos);

    /**
     * 扩展点：除饥饿外的「工作已完成」判定。默认 false（仅由饥饿结束，如末影充能）。
     * 例如 FE 充能可在此判定能量已满，熔炉加热可判定已点燃足够久。
     */
    protected boolean isWorkDone(ServerLevel level, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        return taskType().isInstance(this.firstTask);
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();
        BlockPos pos = getWorkPos();

        // 移动到工作方块旁边
        if (!moveToNearBlock(pos)) {
            return;
        }

        // 饥饿耗尽或工作已完成则结束
        if (StrongholdHelper.getHunger(mob) <= 0 || isWorkDone(level, pos)) {
            setTaskCompleted();
            return;
        }

        onMaintain(level, pos);
    }
}
