package com.cimdy.coe.data.codec.skill.input;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 持续释放 — 按住期间每隔 N tick 释放一次。
 */
public record Continuous(
        ExpressionCodec.Value tickInterval,
        Optional<ExpressionCodec.Value> maxDuration
) implements InputMode {
    public static final Codec<Continuous> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("tick_interval").forGetter(Continuous::tickInterval),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_duration").forGetter(Continuous::maxDuration)
    ).apply(i, Continuous::new));

    @Override
    public Type<?> getType() { return InputModes.CONTINUOUS; }
}
