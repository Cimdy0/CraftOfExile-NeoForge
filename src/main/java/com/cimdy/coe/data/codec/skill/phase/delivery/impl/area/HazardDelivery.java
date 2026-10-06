package com.cimdy.coe.data.codec.skill.phase.delivery.impl.area;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.area.Shape;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.combat.DamageEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.Set;

/**
 * 危害物投递 — 在目标位置生成<b>驻场危害物</b>（{@code SkillEntity}），
 * 在其存活期内由实体自身周期性触发命中，而非像 {@link AreaDelivery} 那样瞬间结算一次。
 */
public record HazardDelivery(
        Shape shape,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value hitTest,
        EntityRead ref,
        /** 一次执行生成的数量（散布）；&le;1 视为 1。 */
        ExpressionCodec.Value count,
        /** 同时存在的上限（按「同施法者 + 同形态」计）；&le;0 视为不限。 */
        ExpressionCodec.Value maxActive
) implements Delivery {
    /** 默认形态：通用危害物（{@code coe:hazard}）。 */
    public static final HazardDelivery DEFAULT = new HazardDelivery(
            new SphereShape(ExpressionCodec.of(1.5f)), ExpressionCodec.of(100.0), ExpressionCodec.of(1.0),
            EntityRead.of(Identifier.parse("coe:hazard")),
            ExpressionCodec.of(1.0), ExpressionCodec.of(0.0));

    public static final Codec<HazardDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            Shape.CODEC.optionalFieldOf("shape", new SphereShape(ExpressionCodec.of(1.5f))).forGetter(HazardDelivery::shape),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", ExpressionCodec.of(100.0))
                    .forGetter(HazardDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(HazardDelivery::hitTest),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(HazardDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("count", ExpressionCodec.of(1.0))
                    .forGetter(HazardDelivery::count),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_active", ExpressionCodec.of(0.0))
                    .forGetter(HazardDelivery::maxActive)
    ).apply(i, HazardDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.HAZARD; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public HazardDelivery withRender(Identifier r) {
        return new HazardDelivery(shape, lifetime, hitTest, ref.withRender(r), count, maxActive);
    }

    /**
     * 危害物形态：<b>只</b>携带 {@code hazard} 标签。
     *
     * <p><b>为什么不并入 {@code area} / {@code aoe}</b>：{@code INC_HAZARD_AREA_OF_EFFECT} 对应的
     * EFF（{@code EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_HAZARD}，见 StatEffectGen:791）
     * 的标签集合是<b>纯 {@code {hazard}}</b>，危害物范围走的是它自己独立的 Hazard Area 词缀。
     * 若在此额外返回 {@code area}，通用「范围效果扩大」类天赋的 {@code {area}} EFF 也会被并入作用域，
     * 从而错误地放大危害物范围 —— 属于过度授予，故刻意不加。
     *
     * <p><b>本返回值是 6 条既有 hazard 接线的唯一激活入口</b>：标签经两条通道进入 stat 作用域——
     * <ol>
     *   <li>{@code SkillPhaseInstance.fireTrigger} 内 {@code SkillStatScopes.enterAction}：
     *       标签 = {@code StatScopes.current(caster).tags() ∪ delivery.statTags() ∪ action.statTags()}；</li>
     *   <li>{@code DamageEvent.addDeliveryTags}：由 {@code SkillDamageSource} 的 phaseId 反查阶段实例，
     *       再把 {@code delivery.statTags()} 并入命中作用域。</li>
     * </ol>
     * 由此复活：{@code DAMAGE_HAZARD} / {@code AREA_OF_EFFECT_HAZARD} / {@code AILMENT_BUILDUP_HAZARD} /
     * {@code AILMENT_MAGNITUDE_SLOW_HAZARD} 等 EFF；其中 hazard 创造的<b>地面异常持续时间</b>同样统一收拢到
     * {@code GROUND_DURATION}（承伤侧 {@code GROUND_DURATION_TAKEN}），不再有专属的 {@code AILMENT_DURATION_HAZARD_TAKEN} EFF。
     */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.of(GateTag.HAZARD)); }
}
