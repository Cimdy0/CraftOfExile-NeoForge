package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record SkillCastEvent(
        Entity caster, SkillInstance skill, Vec3 position, float chargeAmount
) implements SkillTriggerEvent {
    public Identifier skillId() { return skill.getSkill().animation().orElse(null); }
}
