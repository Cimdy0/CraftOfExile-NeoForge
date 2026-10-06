package com.cimdy.coe.data.codec.skill.modifier.impl.damage;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Exposure(
        String damageType,
        ExpressionCodec.Value amount,
        ExpressionCodec.Value duration
) {
    public static final Codec<Exposure> CODEC = RecordCodecBuilder.create(j -> j.group(
            Codec.STRING.fieldOf("damage_type").forGetter(Exposure::damageType),
            ExpressionCodec.VALUE_CODEC.fieldOf("amount").forGetter(Exposure::amount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(160.0))
                    .forGetter(Exposure::duration)
    ).apply(j, Exposure::new));
}
