package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record SkillHitEvent(
        Entity caster, SkillInstance skill, Entity target,
        float damage, Object hitContext  // Object = HitContext, 避免循环依赖
) implements SkillTriggerEvent {}
