package com.cimdy.coe.data.codec.skill.modifier.impl.control;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Slow(
        ExpressionCodec.Value amount,
        ExpressionCodec.Value duration
) {
    public static final Codec<Slow> CODEC = RecordCodecBuilder.create(j -> j.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("amount").forGetter(Slow::amount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(80.0))
                    .forGetter(Slow::duration)
    ).apply(j, Slow::new));
}
