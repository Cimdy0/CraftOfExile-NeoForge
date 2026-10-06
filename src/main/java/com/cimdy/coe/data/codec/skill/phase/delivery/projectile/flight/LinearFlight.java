package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPatternTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.phys.Vec3;

/**
 * 直线飞行 — 保持初始速度方向。
 */
public record LinearFlight() implements FlightPattern {
    public static final MapCodec<LinearFlight> CODEC = MapCodec.unit(new LinearFlight());
    @Override public FlightPattern.Type<?> getType() { return FlightPatternTypes.LINEAR; }

    @Override
    public Vec3 initialVelocity(Vec3 direction, double speed) {
        return direction.normalize().scale(speed);
    }
}
