package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record SkillExpireEvent(
        Entity caster, SkillInstance skill, Vec3 position
) implements SkillTriggerEvent {}
