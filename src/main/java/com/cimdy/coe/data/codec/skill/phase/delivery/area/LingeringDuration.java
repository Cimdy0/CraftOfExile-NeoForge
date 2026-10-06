package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 持续效果 — 范围效果持续存在，每 N tick 触发一次。
 */
public record LingeringDuration(
        ExpressionCodec.Value duration,
        ExpressionCodec.Value tickInterval
) implements AreaDuration {
    public static final Codec<LingeringDuration> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("duration").forGetter(LingeringDuration::duration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("tick_interval", ExpressionCodec.of(10.0))
                    .forGetter(LingeringDuration::tickInterval)
    ).apply(i, LingeringDuration::new));

    @Override public AreaDuration.Type<?> getType() { return AreaDurationTypes.LINGERING; }
}
