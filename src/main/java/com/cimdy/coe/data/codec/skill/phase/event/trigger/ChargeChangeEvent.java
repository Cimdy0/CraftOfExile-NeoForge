package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record ChargeChangeEvent(
        Entity caster, SkillInstance skill,
        String chargeType, int oldCount, int newCount,
        String changeType  // "generate", "consume", "expire"
) implements SkillTriggerEvent {}
