package com.cimdy.coe.data.codec.skill.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 切换开关 — 按一次开，再按一次关 (光环/被动技能)。
 */
public record ToggleInput(boolean defaultOn) implements InputMode {
    public static final Codec<ToggleInput> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("default_on", false).forGetter(ToggleInput::defaultOn)
    ).apply(i, ToggleInput::new));

    @Override
    public Type<?> getType() { return InputModes.TOGGLE; }
}
