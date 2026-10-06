package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record ComboEvent(
        Entity caster, SkillInstance skill,
        String comboId, int step, int totalSteps
) implements SkillTriggerEvent {}
