package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record AilmentEvent(
        Entity caster, SkillInstance skill, Entity target,
        String ailmentType, float magnitude
) implements SkillTriggerEvent {}
