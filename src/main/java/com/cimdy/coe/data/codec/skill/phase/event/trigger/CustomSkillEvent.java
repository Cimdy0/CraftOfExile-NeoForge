package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;

public record CustomSkillEvent(
        Entity caster, SkillInstance skill, String eventName,
        Optional<CompoundTag> data, List<String> tags
) implements SkillTriggerEvent {
    public CustomSkillEvent(Entity caster, SkillInstance skill, String eventName, Optional<CompoundTag> data) {
        this(caster, skill, eventName, data, List.of());
    }
}
