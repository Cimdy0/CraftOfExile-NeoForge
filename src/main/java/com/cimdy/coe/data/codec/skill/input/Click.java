package com.cimdy.coe.data.codec.skill.input;

import com.mojang.serialization.Codec;

/**
 * 点击 — 按下即释放。
 */
public record Click() implements InputMode {
    public static final Codec<Click> CODEC = Codec.BOOL.xmap(b -> new Click(), v -> true);

    @Override
    public Type<?> getType() { return InputModes.CLICK; }
}
