package com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.LinearFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 统一投射物投递 —— 以 {@link ProjectileKind} 轴覆盖飞行命中 / 落地引爆 / 边飞边脉冲 / 原地脉冲 四种子形态（D4丙 对称归并）。
 *
 * <p>四个子形态在记录层共存，运行时由 {@code ProjectileDeliveryExecutor} 按 {@code kind} 分流：
 * <ul>
 *   <li>{@link ProjectileKind#PROJECTILE}：飞行中命中实体即触发并消失（箭矢）；</li>
 *   <li>{@link ProjectileKind#THROWN}：不与实体碰撞，落地 → 引信 → 一次性范围引爆（手雷）；</li>
 *   <li>{@link ProjectileKind#BALL}：穿过实体、边飞边周期放能（本体不命中）；</li>
 *   <li>{@link ProjectileKind#ORB}：原地不动、按节奏周期放能。</li>
 * </ul>
 *
 * <p>字段按「并集」吸收：{@code flight}/{@code collision}/{@code radius}/{@code ref}/{@code origin} 为投射物通用；
 * {@code arc} 仅投掷物用；{@code tags} 为投掷物/球/orb 自述标签；{@code pulseInterval} 仅 orb 用。
 * 各字段对不适用的子形态为 {@link Optional#empty()} / 空集，由执行器按 {@code kind} 取默认兜底。
 */
public record ProjectileDelivery(
        ProjectileKind kind,
        FlightPattern flight,
        CollisionPolicy collision,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value speed,
        Optional<ExpressionCodec.Value> radius,
        EntityRead ref,
        Optional<ExpressionCodec.Value> origin,
        Optional<ExpressionCodec.Value> arc,
        Set<String> tags,
        Optional<ExpressionCodec.Value> pulseInterval
) implements Delivery {
    /** 形态引用默认值：{@code coe:arrow}（通用箭矢；技能可按 id 覆写 render / name）。 */
    public static final EntityRead DEFAULT_REF =
            EntityRead.of(net.minecraft.resources.Identifier.parse("coe:arrow"));

    /** 投掷物（手雷）默认形态引用：{@code coe:entity/grenade}。 */
    public static final EntityRead GRENADE_REF = EntityRead.of(Identifier.parse("coe:grenade"));
    /** 球默认形态引用：{@code coe:entity/ball}。 */
    public static final EntityRead BALL_REF = EntityRead.of(Identifier.parse("coe:ball"));
    /** Orb 默认形态引用：{@code coe:entity/orb}。 */
    public static final EntityRead ORB_REF = EntityRead.of(Identifier.parse("coe:orb"));

    /** 投掷物默认标签：{@code grenade}。 */
    public static final Set<String> THROWN_TAGS = Set.of(GateTag.GRENADE);
    /** 球默认标签：{@code projectile}。 */
    public static final Set<String> BALL_TAGS = Set.of(GateTag.PROJECTILE);
    /** Orb 默认标签：{@code area} + {@code aoe}。 */
    public static final Set<String> ORB_TAGS = Set.of(GateTag.AREA, GateTag.AOE);

    public static final Codec<Set<String>> TAGS_CODEC =
            Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf);

    public static final Codec<ProjectileDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ProjectileKind.CODEC.optionalFieldOf("kind", ProjectileKind.PROJECTILE).forGetter(ProjectileDelivery::kind),
            FlightPattern.CODEC.optionalFieldOf("flight", new LinearFlight()).forGetter(ProjectileDelivery::flight),
            CollisionPolicy.CODEC.optionalFieldOf("collision", new Stop()).forGetter(ProjectileDelivery::collision),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", ExpressionCodec.of(100.0))
                    .forGetter(ProjectileDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", ExpressionCodec.of(1.5))
                    .forGetter(ProjectileDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius").forGetter(ProjectileDelivery::radius),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT_REF).forGetter(ProjectileDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(ProjectileDelivery::origin),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("arc").forGetter(ProjectileDelivery::arc),
            TAGS_CODEC.optionalFieldOf("tags", Set.of()).forGetter(ProjectileDelivery::tags),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("pulse_interval").forGetter(ProjectileDelivery::pulseInterval)
    ).apply(i, ProjectileDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.PROJECTILE; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    @Override public ProjectileDelivery withRender(Identifier r) {
        return new ProjectileDelivery(kind, flight, collision, lifetime, speed, radius,
                ref.withRender(r), origin, arc, tags, pulseInterval);
    }

    /** 投射物形态：按 {@code kind} 取对应的 stat 标签（命中侧经 addDeliveryTags 同源并入）。 */
    @Override public Set<String> statTags() {
        return switch (kind) {
            case PROJECTILE -> EntityRead.statTags(ref, Set.of(GateTag.PROJECTILE));
            case THROWN, BALL, ORB -> EntityRead.statTags(ref, Set.copyOf(tags));
        };
    }
}
