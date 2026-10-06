package com.cimdy.coe.data.codec.skill.modifier.impl.damage;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Penetration(
        String damageType,
        ExpressionCodec.Value amount
) {
    public static final Codec<Penetration> CODEC = RecordCodecBuilder.create(j -> j.group(
            Codec.STRING.fieldOf("damage_type").forGetter(Penetration::damageType),
            ExpressionCodec.VALUE_CODEC.fieldOf("amount").forGetter(Penetration::amount)
    ).apply(j, Penetration::new));
}
