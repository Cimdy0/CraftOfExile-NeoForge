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

import java.util.Optional;
import java.util.Set;

/**
 * 投射物投递 — 发射一个或多个投射物。
 */
public record ProjectileDelivery(
        FlightPattern flight,
        CollisionPolicy collision,
        ExpressionCodec.Value lifetime,
        ExpressionCodec.Value speed,
        Optional<ExpressionCodec.Value> radius,
        EntityRead ref,
        Optional<ExpressionCodec.Value> origin
) implements Delivery {
    /** 形态引用默认值：{@code coe:entity/arrow}（通用箭矢；技能可按 id 覆写 render / name）。 */
    public static final EntityRead DEFAULT_REF =
            EntityRead.of(net.minecraft.resources.Identifier.parse("coe:arrow"));

    /** CODEC 默认值样板（须随 {@link #DEFAULT_REF} 同步）。 */
    private static final ProjectileDelivery DEFAULT = new ProjectileDelivery(
            new LinearFlight(), new Stop(), ExpressionCodec.of(100.0), ExpressionCodec.of(1.5),
            java.util.Optional.empty(), DEFAULT_REF, java.util.Optional.empty());

    public static final Codec<ProjectileDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            FlightPattern.CODEC.optionalFieldOf("flight", new LinearFlight()).forGetter(ProjectileDelivery::flight),
            CollisionPolicy.CODEC.optionalFieldOf("collision", new Stop()).forGetter(ProjectileDelivery::collision),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime", ExpressionCodec.of(100.0))
                    .forGetter(ProjectileDelivery::lifetime),

            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed", ExpressionCodec.of(1.5))
                    .forGetter(ProjectileDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius").forGetter(ProjectileDelivery::radius),
            EntityRead.CODEC.optionalFieldOf("ref", DEFAULT.ref()).forGetter(ProjectileDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(ProjectileDelivery::origin)
    ).apply(i, ProjectileDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.PROJECTILE; }

    /** 返回覆写外观（render）后的副本；其余字段原样保留（技能皮肤机制：render 改为技能 id）。 */
    public ProjectileDelivery withRender(Identifier r) {
        return new ProjectileDelivery(flight, collision, lifetime, speed, radius, ref.withRender(r), origin);
    }

    /** 投射物形态：携带投射物标签（命中侧经 addDeliveryTags 同源并入）。 */
    @Override public Set<String> statTags() { return EntityRead.statTags(ref, Set.of(GateTag.PROJECTILE)); }
}
