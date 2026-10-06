package com.cimdy.coe.data.codec.skill.phase.delivery.impl.area;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.area.AreaDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.Shape;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;

/**
 * 范围投递 — 在目标位置产生范围效果。
 */
public record AreaDelivery(
        Shape shape,
        AreaDuration duration,
        ExpressionCodec.Value hitTest
) implements Delivery {
    public static final Codec<AreaDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            Shape.CODEC.optionalFieldOf("shape", new SphereShape(ExpressionCodec.of(3.0f))).forGetter(AreaDelivery::shape),
            AreaDuration.CODEC.optionalFieldOf("duration", new InstantDuration()).forGetter(AreaDelivery::duration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(AreaDelivery::hitTest)
    ).apply(i, AreaDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.AREA; }

    /** 范围形态：携带范围标签（含范围命中 aoe，命中侧同源）。 */
    @Override public Set<String> statTags() { return Set.of("area", "aoe"); }
}
