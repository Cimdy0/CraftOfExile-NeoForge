package com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.combat.DamageEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 球投递 — 推出一个<b>缓慢飞行、本体不命中、周期性对周围放能</b>的球（{@code coe:skill_ball}）。
 *
 * <p><b>POE2 真值</b>（天雷之珠 Ball Lightning）：
 * <pre>{@code
 *   发射一个缓慢移动、可穿越敌人的投射物。该投射物本身无法击中敌人，
 *   但会不断对附近敌人释放闪电。每 0.2 秒击中范围内的敌人；弩箭以 1.8 公尺内的敌人为目标。
 * }</pre>
 *
 * <h2>与既有三种投递的分工（四者互不替代）</h2>
 * <ul>
 *   <li>{@link ProjectileDelivery} 投射物：<b>飞行中命中实体即触发并消失</b>（箭矢）；</li>
 *   <li>{@link ThrownDelivery} 投掷物：不与实体碰撞，落地 → 引信 → <b>一次性</b>范围引爆（手雷）；</li>
 *   <li>{@link HazardDelivery} 危害物：<b>静止</b>驻场，等目标走进来触发；</li>
 *   <li><b>本投递 球</b>：穿过实体、<b>边飞边周期放能</b> —— 既不是「撞到就结算」，
 *       也不是「落地炸一次」，更不是「原地等人」。</li>
 * </ul>
 *
 * <p><b>脉冲参数在哪</b>：脉冲间隔 {@code pulse_interval} 与半径 {@code pulse_radius} 属于
 * <b>实体自身的运动契约</b>（同投掷物的 {@code gravity} / {@code fuse}），故定义在
 * {@code EntityDef}（{@code coe:entity/ball}）而非本投递；本投递只给「推多远 / 多大范围 / 活多久」，
 * 其中 {@code radius} 是<b>可被范围词缀放大</b>的运行值，会覆盖定义里的静态 {@code pulse_radius}。
 *
 * <p><b>效果不在本层定义</b>：与 {@link HazardDelivery} / {@link ThrownDelivery} 同范式 ——
 * 本投递只负责把球推出去；放电的伤害 / 状态 / 事件全部由技能阶段自身的 {@code actions} 定义，
 * 经 {@code EntityDef.on_trigger} 在每次脉冲时点燃。
 *
 * @param speed    飞行初速（格/tick）；POE2 电球真值 4 m/s ≈ 0.2 格/tick（远慢于普通箭矢）
 * @param radius   脉冲半径（格）；POE2 电球真值 1.8 米
 * @param lifetime 存活上限（tick），到点必定消散（兜底，防止飞出边界永驻）
 * @param tags     本投递自述的 stat 标签（默认 {@code projectile}）
 * @param ref      实体定义引用（{@code coe:entity/ball} 等）
 */
public record BallDelivery(
        ExpressionCodec.Value speed,
        ExpressionCodec.Value radius,
        ExpressionCodec.Value lifetime,
        List<String> tags,
        EntityRead ref,
        Optional<ExpressionCodec.Value> origin
) implements Delivery {
    /** 默认形态：通用球（{@code coe:entity/ball}）。 */
    public static final BallDelivery DEFAULT = new BallDelivery(
            ExpressionCodec.of(0.2), ExpressionCodec.of(1.8), ExpressionCodec.of(100.0),
            List.of(GateTag.PROJECTILE),
            EntityRead.of(Identifier.parse("coe:ball")), Optional.empty());

    public static final Codec<BallDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", DEFAULT.speed()).forGetter(BallDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", DEFAULT.radius()).forGetter(BallDelivery::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", DEFAULT.lifetime())
                    .forGetter(BallDelivery::lifetime),
            Codec.STRING.listOf().optionalFieldOf("tags", DEFAULT.tags()).forGetter(BallDelivery::tags),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(BallDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(BallDelivery::origin)
    ).apply(i, BallDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.BALL; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public BallDelivery withRender(Identifier r) {
        return new BallDelivery(speed, radius, lifetime, tags, ref.withRender(r), origin);
    }

    /**
     * 球形态的 stat 标签：默认<b>只</b>带 {@code projectile}。
     *
     * <p><b>为什么不贴 {@code sustained} / {@code chaining}</b>：POE2 电球的真值标签确实含
     * Sustained / Chaining / Duration，但项目内这三个标签<b>没有任何 stat 消费点</b> ——
     * 贴上即「有标签无机制」的死数据（同 {@code Payoff / Meta / Conditional} 一律不贴的口径）。
     * 球本体确实是持续伤害，但它在引擎里走的是 {@code projectile} 一族词缀，故只贴这一条。
     *
     * <p><b>本返回值是球类接线的激活入口</b>：标签经 {@code SkillStatScopes.enterAction} 与
     * {@code DamageEvent.addDeliveryTags} 两条通道进入 stat 作用域。
     */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.copyOf(tags)); }
}
