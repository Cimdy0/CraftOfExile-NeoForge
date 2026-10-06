package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import net.minecraft.world.entity.LivingEntity;

/**
 * 实体域读值节点类别（节点分类）：作用于当前候选实体（{@link ValueScope#current()}）的读值。
 * <p>
 * 统一收敛「取当前实体 + 判空兜底 0」样板：实现类只写 {@link #evaluate(LivingEntity, ValueScope)}，
 * 判空由本接口 default 方法完成，不再每个节点各写一遍
 * {@code LivingEntity living = scope.current(); if (living == null) return 0D;}。
 * <p>
 * 家族成员：属性 / 药水效果 / 资源 / 状态堆叠 / 技能使用中 / 战斗状态 / 装备族（entity、equipment 子包）。
 */
public interface EntityNode extends ValueExpr {
    @Override
    default double evaluate(ValueScope scope) {
        LivingEntity living = scope.current();
        return living == null ? 0D : evaluate(living, scope);
    }

    /** 在非空当前实体上读值。 */
    double evaluate(LivingEntity current, ValueScope scope);
}
