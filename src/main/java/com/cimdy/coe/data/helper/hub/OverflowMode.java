package com.cimdy.coe.data.helper.hub;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 资源溢出/上限模式：
 * <ul>
 *   <li>{@code REPLACE} —— 装不下的量被丢弃（默认）。</li>
 *   <li>{@code EXCESS} —— 原资源装满后，溢出部分按 resource_convert 转给另一资源。</li>
 *   <li>{@code GAIN} —— 原资源照常增加，同时额外以一定份额增加另一资源（共存）。</li>
 * </ul>
 * 当前仅 REPLACE 在通用池生效；EXCESS/GAIN 的转化通道随统一枢纽接入而启用。
 */
public enum OverflowMode implements StringRepresentable {
    REPLACE("replace"),
    EXCESS("excess"),
    GAIN("gain");

    private final String name;

    OverflowMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<OverflowMode> CODEC = StringRepresentable.fromEnum(OverflowMode::values);
}
