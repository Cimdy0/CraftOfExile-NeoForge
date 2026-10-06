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
 * 光束投递 — 发射一束能量，瞬发或持续。
 */
public record BeamDelivery(
        ExpressionCodec.Value range,
        ExpressionCodec.Value width,
        ExpressionCodec.Value duration,
        ExpressionCodec.Value hitTest,
        Optional<ExpressionCodec.Value> origin
) implements Delivery {
    public static final Codec<BeamDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("range").forGetter(BeamDelivery::range),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("width", ExpressionCodec.of(0.5))
                    .forGetter(BeamDelivery::width),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(0.0))
                    .forGetter(BeamDelivery::duration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(BeamDelivery::hitTest),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(BeamDelivery::origin)
    ).apply(i, BeamDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.BEAM; }

    /** 光束形态：携带光束标签。 */
    @Override public Set<String> statTags() { return Set.of("beam"); }
}
