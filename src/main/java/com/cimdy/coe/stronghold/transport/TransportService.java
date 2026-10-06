package com.cimdy.coe.stronghold.transport;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.CarryStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * 跨维物流服务（方案 F）。
 * <p>
 * 从 {@code CarryGoal} 的状态机中抽出「路由计算 + 能量预算 + 在途阶段推进」等纯决策逻辑，
 * 使搬运目标只负责移动 / 容器交互，物流决策集中管理，便于未来支持批量运输与路径优化。
 * 当前为无状态纯函数集合，行为与原内联逻辑完全一致。
 */
public final class TransportService {
    private TransportService() {
    }

    /** 单次跨维传送所需能量（双倍，去程 + 回程预算）。 */
    public static double teleportEnergyCost(CarryTask task) {
        return task.getEnergyCost() * 2;
    }

    /** 尝试从源终端扣除跨维传送能量；不足则返回 false（调用方应终止任务）。 */
    public static boolean tryDeductTeleportEnergy(ServerLevel level, CarryTask task) {
        return StrongholdHelper.reduceEnderEnergy(level, task.getSourceTerminal().pos(), teleportEnergyCost(task));
    }

    /** 根据当前阶段与角色，返回实体应前往的终端方块位置。 */
    public static BlockPos terminalToMoveTo(CarryTask task, CarryStage stage, GlobalPos entityTerminal, boolean remotePickup) {
        if (stage == CarryStage.GO_TO_OTHER_TERMINAL) {
            return entityTerminal.pos();
        } else { // RETURN_OTHER_TERMINAL
            if (remotePickup) {
                return task.getSourceTerminal().pos();
            } else {
                return task.getTargetTerminal().pos();
            }
        }
    }

    /** 计算传送目的地。 */
    public static GlobalPos teleportDestination(CarryTask task, CarryStage stage, boolean remotePickup) {
        boolean firstTrip = (stage == CarryStage.GO_TO_OTHER_TERMINAL);
        if (remotePickup) {
            return firstTrip ? task.getSourceTerminal() : task.getTargetTerminal();
        } else {
            return firstTrip ? task.getTargetTerminal() : task.getSourceTerminal();
        }
    }

    /** 传送后的下一阶段；返回 empty 表示任务完成。 */
    public static Optional<CarryStage> stageAfterTeleport(CarryStage stage, boolean remotePickup) {
        if (stage == CarryStage.GO_TO_OTHER_TERMINAL) {
            return Optional.of(remotePickup ? CarryStage.GO_TO_SOURCE : CarryStage.GO_TO_TARGET);
        } else { // RETURN_OTHER_TERMINAL
            if (remotePickup) {
                return Optional.of(CarryStage.GO_TO_TARGET);
            } else {
                return Optional.empty();
            }
        }
    }
}
