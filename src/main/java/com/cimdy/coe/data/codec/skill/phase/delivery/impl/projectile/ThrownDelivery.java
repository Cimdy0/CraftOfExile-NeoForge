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
 * 投掷物投递 — 扔出一个弹体（{@code coe:grenade}），落地后按引信起爆。
 *
 * <p><b>与 {@link ProjectileDelivery}（投射物）的分工</b>：
 * <ul>
 *   <li>投射物：<b>飞行中命中实体即触发</b>（原版箭矢命中回调 + {@code CollisionPolicy}）；</li>
 *   <li>投掷物：<b>不与实体碰撞</b> —— 抛物线飞出 → 落地 → 引信倒计时 → 以自身为中心<b>范围引爆</b>。</li>
 * </ul>
 * 这是 POE2 手雷的真实行为：手雷不会被半空的小怪挡下，落地才起爆。
 * 因此手雷不能复用投射物投递（那会让它撞到第一个实体就炸）。
 *
 * <p><b>效果不在本层定义</b>：与 {@link HazardDelivery} / {@link RemnantDelivery} 同范式 ——
 * 本投递只负责「把弹体扔出去、决定炸多大、多久后炸」，爆炸的伤害 / 状态 / 事件
 * 全部由技能阶段自身的 {@code actions} 定义，经 {@code EntityDef.on_trigger} 在引爆瞬间点燃。
 *
 * @param speed    投掷初速（格/tick 量级），技能层「扔多用力」
 * @param radius   引爆半径（格）
 * @param lifetime 存活上限（tick），到点必定引爆（兜底，防止弹体永驻）
 * @param arc      上抬分量占初速的比例（0.35 = 初速的 35% 作为竖直上抛，形成抛物弧线）
 * @param tags     本投递自述的 stat 标签（默认 {@code grenade}）
 * @param ref      实体定义引用（{@code coe:entity/grenade} 等）
 */
public record ThrownDelivery(
        ExpressionCodec.Value speed,
        ExpressionCodec.Value radius,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value arc,
        List<String> tags,
        EntityRead ref,
        Optional<ExpressionCodec.Value> origin
) implements Delivery {
    /** 默认形态：通用手雷（{@code coe:entity/grenade}）。 */
    public static final ThrownDelivery DEFAULT = new ThrownDelivery(
            ExpressionCodec.of(1.1), ExpressionCodec.of(2.5), ExpressionCodec.of(100.0),
            ExpressionCodec.of(0.35), List.of(GateTag.GRENADE),
            EntityRead.of(Identifier.parse("coe:grenade")), Optional.empty());

    public static final Codec<ThrownDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", DEFAULT.speed()).forGetter(ThrownDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", DEFAULT.radius()).forGetter(ThrownDelivery::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", DEFAULT.lifetime()).forGetter(ThrownDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("arc", DEFAULT.arc()).forGetter(ThrownDelivery::arc),
            Codec.STRING.listOf().optionalFieldOf("tags", DEFAULT.tags()).forGetter(ThrownDelivery::tags),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(ThrownDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(ThrownDelivery::origin)
    ).apply(i, ThrownDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.THROWN; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public ThrownDelivery withRender(Identifier r) {
        return new ThrownDelivery(speed, radius, lifetime, arc, tags, ref.withRender(r), origin);
    }

    /**
     * 投掷物形态标签：默认<b>只</b>带 {@code grenade}。
     *
     * <p><b>为什么不并入 {@code projectile}</b>：手雷在 POE2 有自己独立的
     * 「Grenade Damage / Grenade Area of Effect / Grenade Detonation Time」一族词缀，
     * 与「Projectile Damage」是两条线；在此额外返回 {@code projectile} 会让通用投射物词缀
     * 一并作用于手雷 —— 属过度授予，故与 {@link HazardDelivery}（只带 {@code hazard}）同口径。
     *
     * <p><b>本返回值是 grenade 接线的激活入口</b>：{@code DAMAGE_GRENADE} / {@code AREA_OF_EFFECT_GRENADE} /
     * {@code COOLDOWN_RECOVERY_RATE_GRENADE} / {@code DELIVERY_GRENADE_PROJECTILE} 仍由此标签驱动；
     * 而手雷创造的<b>地面异常持续时间</b>已统一收拢到 {@code GROUND_DURATION}（作用域 {@code ailment,!taken}，
     * 承伤侧为 {@code GROUND_DURATION_TAKEN}），不再存在专属的 {@code AILMENT_DURATION_GRENADE_TAKEN} EFF。
     * 标签经 {@code SkillStatScopes.enterAction} 与 {@code DamageEvent.addDeliveryTags} 两条通道进入作用域。
     */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.copyOf(tags)); }
}
