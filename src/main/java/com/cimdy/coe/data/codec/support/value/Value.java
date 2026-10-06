package com.cimdy.coe.data.codec.support.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Value(float original, int duration) {
    public static final Value EMPTY = new Value(0, 0);
    public static final Codec<Value> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("original").forGetter(Value::original),
            Codec.INT.fieldOf("duration").forGetter(Value::duration)
    ).apply(instance, Value::new));
}
