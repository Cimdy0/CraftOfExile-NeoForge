package com.cimdy.coe.data.codec.skill.phase.action;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * 动作执行上下文，包含技能、目标、位置等所有运行时信息。
 *
 * <p>去重原则：命中相关数据（目标实体/目标方块/投射物降伤倍率）只存于 {@link HitContext} 一处，
 * {@code target()}/{@code targetBlock()}/{@code projectileDamageMultiplier()} 为派生视图，
 * 不作为独立组件重复缓存（否则同一份数据在 record 中存两份，且触发时需多次 with* 拷贝同步）。
 */
public record ActionContext(
        SkillInstance skill,
        Entity caster,
        Optional<HitContext> hitContext,
        Vec3 position,
        Vec3 direction,
        float chargeAmount,
        int tickCount,
        SkillEventBus eventBus,
        UUID phaseId,
        DamageContext damageContext,
        Optional<ExpressionContext> eventContext
) {
    /** 目标实体（派生自命中上下文，单一数据源）。 */
    public Optional<Entity> target() { return hitContext.flatMap(HitContext::entity); }

    /** 目标方块（派生自命中上下文，单一数据源）。 */
    public Optional<BlockPos> targetBlock() { return hitContext.flatMap(HitContext::block); }

    /** 投射物来源降伤倍率（分裂/连锁降伤，派生自命中上下文）。 */
    public Optional<Float> projectileDamageMultiplier() { return hitContext.flatMap(HitContext::sourceDamageMultiplier); }

    /** 携带命中上下文：同时更新位置/方向为命中点（目标/降伤倍率经派生视图自动可见，无需再单独同步）。 */
    public ActionContext withHit(HitContext hit) {
        return new ActionContext(skill, caster, Optional.of(hit),
                hit.position() != null ? hit.position() : position,
                hit.normal() != null ? hit.normal() : direction,
                chargeAmount, tickCount, eventBus, phaseId, damageContext, eventContext);
    }

    public ActionContext withCharge(float charge) {
        return new ActionContext(skill, caster, hitContext, position, direction,
                charge, tickCount, eventBus, phaseId, damageContext, eventContext);
    }

    public ActionContext withPhaseId(UUID phaseId) {
        return new ActionContext(skill, caster, hitContext, position, direction,
                chargeAmount, tickCount, eventBus, phaseId, damageContext, eventContext);
    }

    /**
     * Stat 读点收口：在标签作用域内读取实体某属性，取代各 Action 重复的
     * {@code StatScopes.enter → getAttributeValue → finally close} 样板。
     * {@code sourceEntity} 非 null 时绑定为作用域 source（供 onTarget(...) 类条件在目标身上求值，
     * 死线 #37 同款手法）；{@code tags} 为空则直接读属性（等价无作用域）。
     */
    public static double stat(LivingEntity entity, Holder<Attribute> attr, Set<String> tags, @Nullable LivingEntity sourceEntity) {
        if (entity == null) return 0.0;
        if (tags == null || tags.isEmpty()) return entity.getAttributeValue(attr);
        StatContext ctx = sourceEntity != null
                ? StatContext.of(tags, null).withSourceEntity(sourceEntity)
                : StatContext.of(tags, null);
        AutoCloseable scope = StatScopes.enter(entity, ctx);
        try {
            return entity.getAttributeValue(attr);
        } finally {
            try { scope.close(); } catch (Exception ignored) { }
        }
    }
}
