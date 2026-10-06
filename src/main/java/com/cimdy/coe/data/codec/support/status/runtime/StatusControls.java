package com.cimdy.coe.data.codec.support.status.runtime;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.event.combat.status.StatusEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 硬控制（定身）落点：原本由 StunEffect.setNoAi 驱动。
 * 每 tick 由 {@link StatusEvent} 调用，对处于硬控制状态的实体施加 / 解除定身。
 */
public final class StatusControls {

    private StatusControls() {
    }

    /** 实体是否处于硬控制（无法行动）状态。 */
    public static boolean isControlled(LivingEntity e) {
        return StatusEngine.has(e, StatusDefs.STUN)
                || StatusEngine.has(e, StatusDefs.STUN)
                || StatusEngine.has(e, StatusDefs.PETRIFY)
                || StatusEngine.has(e, StatusDefs.PARRIED)
                || StatusEngine.has(e, StatusDefs.ELECTROCUTE)
                || StatusEngine.has(e, StatusDefs.FREEZE);
    }

    /** 每 tick 施加控制：生物（Mob）停止 AI；玩家硬控制由 {@code StunScreen} 另行表现。 */
    public static void apply(LivingEntity e) {
        boolean controlled = isControlled(e);
        if (e instanceof Mob mob) {
            mob.setNoAi(controlled);
        }
    }
}
