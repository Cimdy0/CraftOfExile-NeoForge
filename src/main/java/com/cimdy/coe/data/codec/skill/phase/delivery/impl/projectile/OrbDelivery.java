package com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.combat.DamageEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

/**
 * Orb 投递 — 在目标位置生成一个<b>原地驻场、周期性对周围放能</b>的球（{@code coe:orb}）。
 *
 * <p><b>POE2 真值</b>（poe2db / poe2wiki，0.3.0）：
 * <pre>{@code
 *   风暴漩涡 Orb of Storms（Spell / AoE / Sustained / Lightning / Duration / Chaining / Orb / Remnant）
 *     创造一个电力球，持续对附近敌人发射连锁闪电。
 *     每 4 秒发射一次；球半径 3.6 米；球持续 12 秒；同时最多 1 个。
 *   日曜球 Solar Orb（Spell / AoE / Sustained / Fire / Duration / Orb）
 *     创造一个会周期性释放火焰脉冲的火球。非常靠近球体的敌人会被点燃。
 *     每 1.4 秒脉冲一次；爆炸范围 2.5 米；球持续 10 秒。
 * }</pre>
 *
 * <h2>与既有投递的分工（五者互不替代）</h2>
 * <ul>
 *   <li>{@link ProjectileDelivery} 投射物：飞行中命中实体即触发并消失；</li>
 *   <li>{@link ThrownDelivery} 投掷物：落地 → 引信 → <b>一次性</b>范围引爆；</li>
 *   <li>{@link HazardDelivery} 危害物：静止驻场，<b>等目标走进来</b>才触发；</li>
 *   <li>{@link BallDelivery} 球：<b>边飞边</b>周期放能（本体不命中）；</li>
 *   <li><b>本投递 Orb</b>：<b>原地不动</b>、按自己的节奏周期放能 —— 不靠目标触发，也不移动。</li>
 * </ul>
 *
 * <p><b>与 {@link HazardDelivery} 的差别只有一个行为组件</b>（{@code pulse} vs {@code contact}），
 * 而正是「实体 / 行为拆分」让这一点差别<b>不需要一个新实体类</b>：
 * {@code coe:orb} 与 {@code coe:prop} 共用注册基类 {@code SkillEntity}，
 * 差异全在 {@code coe:entity} 数据里。危害物「敌人踩上来才响」，Orb「到点自己放电」。
 *
 * <p><b>效果不在本层定义</b>：与 {@link HazardDelivery} / {@link ThrownDelivery} 同范式 ——
 * 本投递只负责「把球放在哪、多大范围、活多久、多久放一次」；
 * 放电的伤害 / 状态 / 事件全部由技能阶段自身的 {@code actions} 定义，
 * 经 {@code EntityDef.on_trigger} 在每次脉冲时点燃。
 *
 * @param radius        作用半径（格）：可被范围词缀放大，装配时写入实体
 * @param lifetime      存活时长（tick）：到点消散（POE2 真值 12 秒 / 10 秒）
 * @param pulseInterval 脉冲间隔（tick）：节奏是技能层参数，装配时注入实体（覆盖形态自带值）
 * @param tags          本投递自述的 stat 标签（默认 {@code area} + {@code aoe}）
 * @param ref           实体定义引用（默认 {@code coe:entity/orb}）
 */
public record OrbDelivery(
        ExpressionCodec.Value radius,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value pulseInterval,
        List<String> tags,
        EntityRead ref
) implements Delivery {
    /** 默认形态：通用 Orb（{@code coe:orb}），节奏取风暴漩涡真值 4 秒 = 80 tick。 */
    public static final OrbDelivery DEFAULT = new OrbDelivery(
            ExpressionCodec.of(3.6), ExpressionCodec.of(240.0), ExpressionCodec.of(80.0),
            List.of(GateTag.AREA, GateTag.AOE),
            EntityRead.of(Identifier.parse("coe:orb")));

    public static final Codec<OrbDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", DEFAULT.radius()).forGetter(OrbDelivery::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", DEFAULT.lifetime())
                    .forGetter(OrbDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("pulse_interval", DEFAULT.pulseInterval())
                    .forGetter(OrbDelivery::pulseInterval),
            Codec.STRING.listOf().optionalFieldOf("tags", DEFAULT.tags()).forGetter(OrbDelivery::tags),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(OrbDelivery::ref)
    ).apply(i, OrbDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.ORB; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public OrbDelivery withRender(Identifier r) {
        return new OrbDelivery(radius, lifetime, pulseInterval, tags, ref.withRender(r));
    }

    /**
     * Orb 形态的 stat 标签：默认 {@code area} + {@code aoe}。
     *
     * <p><b>为什么是范围族而不是 {@code hazard}</b>：POE2 真值标签里 Orb 与 Hazard 是两回事
     * （风暴漩涡 = AoE/Orb/Duration/…，危害物另属 Hazard 一族词缀）；
     * 项目内也没有任何以 {@code orb} 为作用域的 stat 消费点，
     * 故<b>不新增 {@code orb} 标签</b>（无发射点的标签 = 死数据），
     * 而是按它真实命中的词缀族 —— 范围伤害 / 范围效果 —— 归入 {@code area} / {@code aoe}。
     *
     * <p><b>本返回值是 Orb 接线的激活入口</b>：标签经 {@code SkillStatScopes.enterAction} 与
     * {@code DamageEvent.addDeliveryTags} 两条通道进入 stat 作用域。
     */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.copyOf(tags)); }
}
