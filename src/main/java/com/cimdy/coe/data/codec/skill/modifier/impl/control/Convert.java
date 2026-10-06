package com.cimdy.coe.data.codec.skill.modifier.impl.control;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record Convert(
        String from,
        String to,
        ExpressionCodec.Value ratio,
        Optional<ExpressionCodec.Value> addedAsExtra
) {
    public static final Codec<Convert> CODEC = RecordCodecBuilder.create(j -> j.group(
            Codec.STRING.fieldOf("from").forGetter(Convert::from),
            Codec.STRING.fieldOf("to").forGetter(Convert::to),
            ExpressionCodec.VALUE_CODEC.fieldOf("ratio").forGetter(Convert::ratio),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("added_as_extra").forGetter(Convert::addedAsExtra)
    ).apply(j, Convert::new));
}
