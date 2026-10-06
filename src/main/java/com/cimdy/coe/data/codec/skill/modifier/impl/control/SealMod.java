package com.cimdy.coe.data.codec.skill.modifier.impl.control;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.skill.helper.state.SealState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 封印效果（Seal）— 让被辅助技能获得 POE2「Unleash / 封印」机制：每次施放累积封印、
 * 持有期间施放获 cast speed 加成（seal 作用域）、存满后释放重放。技能与辅助宝石共用本类。
 *
 * <p>字段语义见 {@code SealState.Save}。数值为临时占位（provisional），待平衡。
 */
public record SealMod(
        int maxSeals,
        double gainEveryCastTimeMultiplier,
        int repeatPerSealBroken,
        double areaIncPerSealBroken
) implements SkillModifier {

    public static final Codec<SealMod> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("max_seals", 1).forGetter(SealMod::maxSeals),
            Codec.DOUBLE.optionalFieldOf("gain_every_cast_time_multiplier", 100.0)
                    .forGetter(SealMod::gainEveryCastTimeMultiplier),
            Codec.INT.optionalFieldOf("repeat_per_seal_broken", 1).forGetter(SealMod::repeatPerSealBroken),
            Codec.DOUBLE.optionalFieldOf("area_inc_per_seal_broken", 0.0)
                    .forGetter(SealMod::areaIncPerSealBroken)
    ).apply(i, SealMod::new));

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.SEAL_MOD; }
    @Override public String descriptionKey() { return "skill.coe.seal_mod"; }
}
