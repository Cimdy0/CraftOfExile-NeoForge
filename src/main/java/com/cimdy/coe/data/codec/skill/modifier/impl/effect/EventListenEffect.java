package com.cimdy.coe.data.codec.skill.modifier.impl.effect;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;

import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;

import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 事件监听效果 — 让辅助宝石（如 Cast on Crit / Cast on Ignite）为被辅助技能注入一个
 * {@link EventListenPhase}（订阅事件总线并在事件发生时触发动作）。
 *
 * <p>这是最具扩展性的效果类型之一：它与 {@link AdditionalActionEffect} 互补——
 * 后者在技能的「生命周期阶段」插入动作，而本类在「外部事件」上插入监听。
 * 典型用途：元宝石把「事件 → 触发链接技能」逻辑注入到技能中。
 */
public record EventListenEffect(EventListenPhase eventListen) implements SkillModifier {
    public static final Codec<EventListenEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
            EventListenPhase.CODEC.fieldOf("event_listen").forGetter(EventListenEffect::eventListen)
    ).apply(i, EventListenEffect::new));

    @Override public SkillModifier.Type<?> getType() { return SkillModifierTypes.EVENT_LISTEN; }
    @Override public String descriptionKey() { return "skill.coe.event_listen"; }
}
