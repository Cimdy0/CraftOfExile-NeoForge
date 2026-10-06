package com.cimdy.coe.data.codec.skill.input;

import com.mojang.serialization.Codec;

import java.util.Map;

/**
 * 输入模式 — 定义玩家如何触发技能。
 * Click(点击)/HoldInput(长按蓄力)/Continuous(持续释放)/ToggleInput(切换开关)
 * 与 CastMechanics 完全正交——任何输入模式都可以配合任何施法类型。
 */
public interface InputMode {
    Codec<Map<InputMode.Type<?>, InputMode>> MAP_CODEC = Codec.dispatchedMap(
            InputModes.REGISTRY.byNameCodec(),
            InputMode.Type::codec);

    Codec<InputMode> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    Type<?> getType();

    record Type<T extends InputMode>(Codec<T> codec) {}
}
