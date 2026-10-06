package com.cimdy.coe.data.helper.combat;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.helper.hub.ResourceEventHelper;

import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import java.util.Set;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.LuckyStatEffect;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 格挡（Block），POE2 机制：格挡成功免除本次伤害，但<b>仍会累积眩晕</b>——
 * 即「挡得住伤害，挡不住晕」，这是格挡流被眩晕打断的核心来源。
 *
 * <p>眩晕累积统一交给 {@link ResourceRegistry}（唯一眩晕条入口，经 {@link ResourceEventHelper} 资源枢纽），
 */
public class BlockHelper {

    /** 被格挡命中转化为眩晕累积的比例（POE2：格挡不免眩晕，但累积量低于直接受击）。 */
    private static final float BLOCKED_STUN_RATIO = 0.1f;

    public static double getBlock(LivingEntity living) {
        // 格挡基础几率（BLOCK_CHANCE，分数）经偏转评级（DEFLECTION_RATING，默认 1.0 乘数）缩放：
        // inc_deflection 经 ADD_MULTIPLIED_BASE 作用于 DEFLECTION_RATING，使格挡几率按百分比提升。
        double block = living.getAttributeValue(AttributeRegistry.BLOCK_CHANCE);
        double maxBlock = living.getAttributeValue(AttributeRegistry.MAX_BLOCK);
        return Math.min(block, maxBlock) * readDeflectionRating(living);
    }

    /**
     * 读 DEFLECTION_RATING：并入 {@code {deflect}} 作用域后求值。
     *
     * <p><b>为什么必须在这里发射 deflect 标签</b>：{@code INC_DEFLECTION_RATING} /
     * {@code TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION} / {@code ..._WHILE_SURROUNDED} /
     * {@code ..._WHILE_MOVING} 四条接线走的是带 {@code {deflect}} 标签的
     * {@code EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT}。
     * 此前 {@link GateTag#DEFLECT} <b>零发射点</b>（全仓除标签定义与效果定义外无任何引用），
     * 按死线谱系 #36 这四条「已接线」词缀恒不匹配、<b>永不生效且无任何报错</b>。
     * 格挡几率计算是 Deflection 的唯一消费点，故标签在此发射即覆盖全部偏转类读取。
     *
     * <p>与 {@code ProjectileDeliveryExecutor#chanceFork} 同构（{@code enterMerged} 并入当前作用域，
     * 条件在承受方侧实时求值）；异常时退化为无作用域读取，不改变原有数值。
     */
    private static double readDeflectionRating(LivingEntity living) {
        try (AutoCloseable ignored = StatScopes.enterMerged(living, StatContext.of(Set.of(GateTag.DEFLECT), null))) {
            return living.getAttributeValue(AttributeRegistry.DEFLECTION_RATING);
        } catch (Exception e) {
            return living.getAttributeValue(AttributeRegistry.DEFLECTION_RATING);
        }
    }

    /**
     * 判定本次攻击是否被格挡；成功格挡时按伤害比例累积眩晕条。
     *
     * @param living 承受方（格挡者）
     * @param target 攻击方
     * @param amount 本次原始伤害量（用于眩晕累积，0 表示未知则退化为不累积）
     */
    public static boolean isAttackBlock(LivingEntity living, LivingEntity target, RandomSource random, float amount) {
        // 已被轻晕/重晕/冻结时无法格挡
        if (StatusEngine.has(living, StatusDefs.STUN)
                || StatusEngine.has(living, StatusDefs.STUN)
                || StatusEngine.has(living, StatusDefs.FREEZE)) {
            return false;
        }
        boolean isBlock = rollBlock(living, random);
        if (isBlock && amount > 0) {
            // 格挡仍累积眩晕：AILMENT_BUILDUP（{ailment,buildup,stun} 作用域）由 StunMeter 内部统一乘算，此处只给原始量
            ResourceRegistry.accumulateStun(living, target, amount * BLOCKED_STUN_RATIO, "block");
        }
        return isBlock;
    }

    /**
     * 纯格挡掷骰（含 DEFLECT 双掷修饰，LuckyStatEffect{DEFLECT}）：无副作用，
     * 供格挡判定本体与 {@code isAttackBlock} 复用——双掷只发生在概率层，
     * 眩晕累积仍只在 {@code isAttackBlock} 命中时发生一次，不会因补掷重复累积。
     */
    public static boolean rollBlock(LivingEntity living, RandomSource random) {
        boolean first = random.nextInt(100) < getBlock(living) * 100;
        LuckyStatEffect.RollMode luck = StatEngine.rollModifier(living, LuckyStatEffect.RollSubject.DEFLECT);
        if (luck == LuckyStatEffect.RollMode.LUCKY) {
            // 双掷取优：第一次已命中即中，未命中补掷一次
            return first || random.nextInt(100) < getBlock(living) * 100;
        }
        if (luck == LuckyStatEffect.RollMode.UNLUCKY) {
            // 双掷取劣：两次都命中才算格挡
            return first && random.nextInt(100) < getBlock(living) * 100;
        }
        return first;
    }
}
