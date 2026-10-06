package com.cimdy.coe.data.codec.skill.phase.delivery.impl;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;

/**
 * 自身投递 — 效果直接施加于施法者自身 (Buff/被动技能)。
 */
public record SelfDelivery(ExpressionCodec.Value hitTest) implements Delivery {
    public static final Codec<SelfDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(SelfDelivery::hitTest)
    ).apply(i, SelfDelivery::new));
    @Override public Delivery.Type<?> getType() { return DeliveryTypes.SELF; }

    /** 自身形态：携带自身标签。 */
    @Override public Set<String> statTags() { return Set.of("self"); }
}
