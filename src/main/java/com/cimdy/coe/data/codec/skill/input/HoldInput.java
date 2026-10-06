package com.cimdy.coe.data.codec.skill.input;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 长按蓄力 — 松手时根据蓄力比例释放。
 */
public record HoldInput(
        ExpressionCodec.Value maxChargeTicks,
        Optional<ExpressionCodec.Value> minCharge
) implements InputMode {
    public static final Codec<HoldInput> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("max_charge_ticks").forGetter(HoldInput::maxChargeTicks),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("min_charge").forGetter(HoldInput::minCharge)
    ).apply(i, HoldInput::new));

    @Override
    public Type<?> getType() { return InputModes.HOLD; }
}
