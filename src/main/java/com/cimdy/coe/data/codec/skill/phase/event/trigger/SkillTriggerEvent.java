package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.event.skill.SkillEvent;
import net.minecraft.world.entity.Entity;

/**
 * 技能触发事件 — 技能间交互的消息载体（sealed 值类型）。
 *
 * 与 com.cimdy.coe.event.skill.SkillEvent（技能生命周期总管：activeSkills / cacheDamageContext /
 * cacheKeyPress / tick 循环）不是同一个类：本类仅作为 SkillEventBus 上传递的消息载体，
 * 二者在概念与职责上已分离，故命名区分。
 *
 * PoE2 新增事件：StaggerEvent, AilmentEvent, ComboEvent,
 * ChargeChangeEvent, ReservationChangeEvent
 */
public sealed interface SkillTriggerEvent
        permits SkillCastEvent, SkillHitEvent,
                SkillKillEvent, SkillExpireEvent,
                CustomSkillEvent,
                StaggerEvent, AilmentEvent,
                ComboEvent, ChargeChangeEvent,
                ReservationChangeEvent {

    Entity caster();
    SkillInstance skill();
}
