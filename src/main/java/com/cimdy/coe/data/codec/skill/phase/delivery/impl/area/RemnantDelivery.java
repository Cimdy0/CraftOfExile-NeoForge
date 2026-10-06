package com.cimdy.coe.data.codec.skill.phase.delivery.impl.area;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireMisc;
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
 * 残片投递 —— 在世界中放置一个<b>可拾取的残留物体</b>（{@code SkillEntity}）。
 *
 * <p><b>残片只是形态，不定义任何效果。</b>它等价于投射物 / 光环 / 危害物一级的「投递形态」：
 * 仅负责把残片放到世界里，并约定「由施放者（即残片的 caster）走过时拾取」。
 * 具体效果（伤害 / 状态 / 增益等）须由技能阶段（phase）的 {@code actions} 在数据层单独定义写入——
 * 残片被施放者拾取时经 {@code SkillPhaseInstance.fireTrigger(ON_HIT)} 回调阶段实例，从而点燃那些 actions。
 *
 * <p><b>{@code {remnant}}</b> 标签经两条通道进入 stat 作用域，使 remnant 一族天赋/词缀生效：
 * <ol>
 *   <li>{@code SkillPhaseInstance.fireTrigger} 内 {@code SkillStatScopes.enterAction}：
 *       标签 = {@code StatScopes.current(caster).tags() ∪ delivery.statTags() ∪ action.statTags()}；</li>
 *   <li>{@code DamageEvent.addDeliveryTags}：由 {@code SkillDamageSource} 的 phaseId 反查阶段实例，
 *       再把 {@code delivery.statTags()} 并入命中作用域。</li>
 * </ol>
 * 由此复活：{@code RECOVER_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT} /
 * {@code RECOVER_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT} /
 * {@code REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY} /
 * {@code REMNANTS_YOU_CREATE_HAVE_INC_EFFECT} 等 EFF（见 WireMisc）。
 *
 * <p><b>与 {@code ResourceRegistry} 并存</b>：{@code ResourceRegistry} 是灌入（Infusion）专用的轻量瞬态标记生产者，
 * 走另一条通道且已工作正常；本投递是「残片形态」的生产者，二者互不干扰，均携带 {@code {remnant}} 标签。
 */
public record RemnantDelivery(
        Shape shape,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value hitTest,
        EntityRead ref
) implements Delivery {
    /** 默认形态：通用残片（{@code coe:remnant}）。 */
    public static final RemnantDelivery DEFAULT = new RemnantDelivery(
            new SphereShape(ExpressionCodec.of(1.5f)), ExpressionCodec.of(200.0), ExpressionCodec.of(1.0),
            EntityRead.of(Identifier.parse("coe:remnant")));

    public static final Codec<RemnantDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            Shape.CODEC.optionalFieldOf("shape", new SphereShape(ExpressionCodec.of(1.5f))).forGetter(RemnantDelivery::shape),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", ExpressionCodec.of(200.0))
                    .forGetter(RemnantDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(RemnantDelivery::hitTest),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(RemnantDelivery::ref)
    ).apply(i, RemnantDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.REMNANT; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public RemnantDelivery withRender(Identifier r) {
        return new RemnantDelivery(shape, lifetime, hitTest, ref.withRender(r));
    }

    /**
     * 残片形态：<b>只</b>携带 {@code remnant} 标签（用常量 {@link GateTag#REMNANT}，非字面量）。
     *
     * <p>该常量正是为残片形态预留的、长期处于「机制缺口」（无生产者）状态的标签；本投递即它等待的生产者。
     * 标签经 {@link #statTags()} 经两条通道进入 stat 作用域，激活上列 remnant 一族既有 EFF。
     */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.of(GateTag.REMNANT)); }
}
