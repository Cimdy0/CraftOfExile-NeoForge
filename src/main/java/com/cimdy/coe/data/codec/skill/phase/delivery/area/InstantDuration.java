package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;

/**
 * 瞬发 — 一次性范围效果。
 */
public record InstantDuration() implements AreaDuration {
    public static final Codec<InstantDuration> CODEC =
            ExpressionCodec.VALUE_CODEC.xmap(v -> new InstantDuration(), v -> ExpressionCodec.of(1.0));
    @Override public AreaDuration.Type<?> getType() { return AreaDurationTypes.INSTANT; }
}
