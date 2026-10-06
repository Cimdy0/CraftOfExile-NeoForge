package com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.Set;

/**
 * 波前投递（Wave）— 沿施法者朝向发射一道<b>向前推进的球形波前</b>，
 * 在 {@code range} 距离内以 {@code speed}（格/tick）匀速前进，沿途每个实体仅被命中一次。
 *
 * <p><b>POE2 真值对应</b>：
 * <pre>{@code
 *   碾压冲击波 Rolling Slam（Shockwave）— 在身前推出一道冲击波，向前滚动并击退沿线敌人；
 *   寒霜波 Frost Wave / 震荡波 Shockwave — 以施法者为源向前扩散的弧形波，经过的敌人受创。
 * }</pre>
 * 与「飞行中消失」的 {@link ProjectileDelivery}、与「原地不动」的 {@link OrbDelivery}、与
 * 「静止持续命中」的 {@link BeamDelivery} 三者均不替代：
 * <ul>
 *   <li>{@link ProjectileDelivery}：命中实体即触发并消失（一次性）；</li>
 *   <li>{@link OrbDelivery}：原地驻场、按节奏脉冲，不移动；</li>
 *   <li>{@link BeamDelivery}：沿线段持续命中（施法者→射程内的整条线）；</li>
 *   <li><b>本投递 Wave</b>：波前<b>逐 tick 向前推进</b>，像一堵移动的能量墙，
 *       经过的实体被命中一次后不再重复命中（去重），到 {@code range} 尽头即消散。</li>
 * </ul>
 *
 * <p><b>几何实现</b>：复用 {@code PersistentDeliveryManager} 的逐 tick 推进框架，
 * 每 tick 取 {@code front = origin + direction * (tickCounter * speed)}，
 * 以 {@code front} 为球心、{@code radius} 为半径做球形扫描，
 * 仅返回<b>尚未命中过</b>的实体（Entry 内 {@code alreadyHit} 去重集保证每实体仅一次）。
 *
 * <p><b>效果不在本层定义</b>：与 Orb / Beam 同范式，本投递只负责「波从哪出发、推多远、多快、多厚」；
 * 波前命中的伤害 / 状态 / 事件全部由技能阶段自身的 {@code actions} 经 {@code ON_HIT} 触发。
 *
 * @param range  波前最大推进距离（格）：到点即消散，{@code remaining = ceil(range/speed)} tick
 * @param radius 波前球形半径（格）：可被辅助宝石「范围扩大」({@code radius_multiplier}) 放大，装配时读取
 * @param speed  波前推进速度（格/tick）：决定波通过一段距离所需时间
 * @param hitTest 命中过滤条件（统一 math 表达式，非零即通过），默认 1.0（不过滤）
 */
public record WaveDelivery(
        ExpressionCodec.Value range,
        ExpressionCodec.Value radius,
        ExpressionCodec.Value speed,
        ExpressionCodec.Value hitTest,
        Optional<ExpressionCodec.Value> origin
) implements Delivery {
    /** 默认形态：中距冲击波，10 格内以 0.4 格/tick 推进、波前厚 2 格（约 1.25s 走完）。 */
        public static final WaveDelivery DEFAULT = new WaveDelivery(
            ExpressionCodec.of(10.0), ExpressionCodec.of(2.0),
            ExpressionCodec.of(0.4), ExpressionCodec.of(1.0), Optional.empty());

    public static final Codec<WaveDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("range", DEFAULT.range()).forGetter(WaveDelivery::range),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", DEFAULT.radius()).forGetter(WaveDelivery::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", DEFAULT.speed()).forGetter(WaveDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", DEFAULT.hitTest()).forGetter(WaveDelivery::hitTest),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(WaveDelivery::origin)
    ).apply(i, WaveDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.WAVE; }

    /** 波前形态：携带波前标签（命中侧同源，使「波前伤害提高」类带标签天赋即时生效）。 */
    @Override public Set<String> statTags() { return Set.of("wave"); }
}
