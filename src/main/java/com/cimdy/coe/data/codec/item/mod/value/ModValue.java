package com.cimdy.coe.data.codec.item.mod.value;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 一个数值（或范围）：{@code min} + 可选 {@code max} —— <b>两个值均为 {@link ExpressionCodec.Value}</b>
 * （常量或 math 表达式皆可）。词缀 tier 与装备基底词缀共用此承载。
 */
public record ModValue(ExpressionCodec.Value min, Optional<ExpressionCodec.Value> max) {

    public static final Codec<ModValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExpressionCodec.VALUE_CODEC.fieldOf("min").forGetter(ModValue::min),
                    ExpressionCodec.VALUE_CODEC.optionalFieldOf("max").forGetter(ModValue::max)
            ).apply(instance, ModValue::new));

    /** 定值。 */
    public static ModValue of(double value) {
        return new ModValue(ExpressionCodec.of(value), Optional.empty());
    }

    /** 范围。 */
    public static ModValue of(double min, double max) {
        return new ModValue(ExpressionCodec.of(min), Optional.of(ExpressionCodec.of(max)));
    }

    /** 由两个 {@link ExpressionCodec.Value} 构造（可选 max）。 */
    public static ModValue of(ExpressionCodec.Value min, Optional<ExpressionCodec.Value> max) {
        return new ModValue(min, max);
    }
}
