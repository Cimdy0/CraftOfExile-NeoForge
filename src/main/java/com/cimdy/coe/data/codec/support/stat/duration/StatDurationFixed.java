package com.cimdy.coe.data.codec.support.stat.duration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

/**
 * 时长段的内联形态：{@link StatDuration.Kind} + tick 数，覆盖全部三种时间语义。
 * 扁平写出形如 {@code duration:120} / {@code cooldown:60} / {@code interval:20}。
 *
 * @param kind  时间语义（持续 / 冷却 / 周期）
 * @param ticks 时间量（tick，恒正）
 */
public record StatDurationFixed(StatDuration.Kind kind, int ticks) implements StatDuration {

    public StatDurationFixed {
        Objects.requireNonNull(kind, "StatDurationFixed.kind 不能为 null");
        if (ticks <= 0) throw new IllegalArgumentException("StatDurationFixed.ticks 须为正: " + ticks);
    }

    @Override
    public String type() {
        return "fixed";
    }

    public static final MapCodec<StatDurationFixed> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StatDuration.Kind.CODEC.fieldOf("kind").forGetter(StatDurationFixed::kind),
            com.mojang.serialization.Codec.INT.fieldOf("ticks").forGetter(StatDurationFixed::ticks)
    ).apply(i, StatDurationFixed::new));

    public static final com.mojang.serialization.Codec<StatDurationFixed> CODEC = MAP_CODEC.codec();

    @Override
    public String toString() {
        return flatten();
    }
}
