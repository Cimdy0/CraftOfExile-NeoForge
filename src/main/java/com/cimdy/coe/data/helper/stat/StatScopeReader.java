package com.cimdy.coe.data.helper.stat;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/**
 * 「作用域内读属性」的集中读值器（原散落在事件类内的私有辅助，铁律 #56 抽出）。
 *
 * <p>连续倍率经 fold 通道在计算期读取，不另建 FIRE 效果；
 * 近距命中击退「几率」已拆至 {@code KNOCKBACK_CHANCE}（由 DamageAction 掷骰），
 * 本处仅承载「距离倍率」，避免 chance/inc 混读同一属性。
 */
public final class StatScopeReader {
    private StatScopeReader() {
    }

    /** 在 {knockback} 作用域内读 KNOCKBACK（纯距离倍率，不含近距几率）。 */
    public static double knockbackMult(LivingEntity entity) {
        if (entity == null) return 1.0;
        try (AutoCloseable scope = StatScopes.enter(entity, StatContext.of(Set.of(GateTag.KNOCKBACK), null))) {
            return entity.getAttributeValue(AttributeRegistry.KNOCKBACK);
        } catch (Exception ignored) {
            return 1.0;
        }
    }
}
