package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPatternTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * 螺旋飞行 — 螺旋前进。
 */
public record SpiralFlight(ExpressionCodec.Value spiralRadius, ExpressionCodec.Value frequency)
        implements FlightPattern {
    public static final MapCodec<SpiralFlight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("spiral_radius").forGetter(SpiralFlight::spiralRadius),
            ExpressionCodec.VALUE_CODEC.fieldOf("frequency").forGetter(SpiralFlight::frequency)
    ).apply(i, SpiralFlight::new));

    @Override public FlightPattern.Type<?> getType() { return FlightPatternTypes.SPIRAL; }

    @Override
    public Vec3 initialVelocity(Vec3 direction, double speed) {
        return direction.normalize().scale(speed);
    }

    @Override
    public Vec3 tickVelocity(Vec3 current, int age, SkillEntity entity) {
        return current.yRot((float) frequency.getFloat());
    }
}
