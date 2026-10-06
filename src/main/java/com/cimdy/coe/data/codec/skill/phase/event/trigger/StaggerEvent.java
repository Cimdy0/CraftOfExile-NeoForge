package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record StaggerEvent(
        Entity caster, SkillInstance skill, Entity target,
        float buildup, float threshold
) implements SkillTriggerEvent {}
