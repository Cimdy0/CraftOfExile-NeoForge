package com.cimdy.coe.data.codec.skill.modifier.impl.effect;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 附加动作效果 — 为技能注入全新的 {@link TriggeredAction}。技能与辅助宝石共用本类。
 *
 * <p>这是最具扩展性的效果类型：它可以在技能的任意生命周期阶段插入新的行为，
 * 而不仅仅是修改数值。典型用途（技能自身或辅助宝石均可）：
 * <ul>
 *   <li>ON_KILL + DamageAction → "击杀时造成额外爆炸伤害"</li>
 *   <li>ON_HIT + AilmentAction → "命中时施加额外异常状态"</li>
 *   <li>ON_CAST + ModifierAction → "施放时获得 buff / 得球"</li>
 *   <li>ON_EXPIRE + SpawnAction → "投射物消失时召唤仆从"</li>
 * </ul>
 */
public record AdditionalActionEffect(
        TriggeredAction action,
        Optional<ExpressionCodec.Value> phaseIndex
) implements SkillModifier {
    public static final Codec<AdditionalActionEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
            TriggeredAction.CODEC.fieldOf("action").forGetter(AdditionalActionEffect::action),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("phase_index").forGetter(AdditionalActionEffect::phaseIndex)
    ).apply(i, AdditionalActionEffect::new));

    /** 不指定阶段索引时注入所有阶段。 */
    public AdditionalActionEffect(TriggeredAction action) {
        this(action, Optional.empty());
    }

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.ADDITIONAL_ACTION; }
    @Override public String descriptionKey() { return "skill.coe.additional_action"; }
}
