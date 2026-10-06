package com.cimdy.coe.data.codec.skill.modifier.impl.damage;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AddedDamage(
        String damageType,
        ExpressionCodec.Value min,
        ExpressionCodec.Value max
) {
    public static final Codec<AddedDamage> CODEC = RecordCodecBuilder.create(j -> j.group(
            Codec.STRING.fieldOf("damage_type").forGetter(AddedDamage::damageType),
            ExpressionCodec.VALUE_CODEC.fieldOf("min").forGetter(AddedDamage::min),
            ExpressionCodec.VALUE_CODEC.fieldOf("max").forGetter(AddedDamage::max)
    ).apply(j, AddedDamage::new));
}
