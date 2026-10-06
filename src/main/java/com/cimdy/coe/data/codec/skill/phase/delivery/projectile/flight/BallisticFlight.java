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
 * 弹道飞行 — 抛物线轨迹。
 */
public record BallisticFlight(ExpressionCodec.Value gravity, ExpressionCodec.Value arcHeight)
        implements FlightPattern {
    public static final MapCodec<BallisticFlight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("gravity", ExpressionCodec.of(0.05))
                    .forGetter(BallisticFlight::gravity),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("arc_height", ExpressionCodec.of(1.0))
                    .forGetter(BallisticFlight::arcHeight)
    ).apply(i, BallisticFlight::new));

    @Override public FlightPattern.Type<?> getType() { return FlightPatternTypes.BALLISTIC; }

    @Override
    public Vec3 initialVelocity(Vec3 direction, double speed) {
        return direction.normalize().scale(speed).add(0, arcHeight.getFloat(), 0);
    }

    @Override
    public Vec3 tickVelocity(Vec3 current, int age, SkillEntity entity) {
        return current.add(0.0, -gravity.getFloat(), 0.0);
    }
}
