package com.cimdy.coe.data.codec.skill.phase.event.trigger;

import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import net.minecraft.world.entity.Entity;

public record ReservationChangeEvent(
        Entity caster, SkillInstance skill,
        float oldReserved, float newReserved,
        float maxReservable
) implements SkillTriggerEvent {}
