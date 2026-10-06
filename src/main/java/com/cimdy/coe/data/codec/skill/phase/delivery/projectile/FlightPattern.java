package com.cimdy.coe.data.codec.skill.phase.delivery.projectile;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.phys.Vec3;

/**
 * 飞行模式 — 投射物每 tick 如何运动。
 */
public interface FlightPattern {
    Registry<FlightPattern.Type<?>> REGISTRY = FlightPatternTypes.REGISTRY;

    Codec<FlightPattern> CODEC = FlightPatternTypes.REGISTRY.byNameCodec()
            .dispatch(FlightPattern::getType, FlightPattern.Type::codec);

    Type<?> getType();

    /** 初始速度向量（direction 已归一化） */
    Vec3 initialVelocity(Vec3 direction, double speed);

    /** 每 tick 速度修正，默认不变（直线飞行） */
    default Vec3 tickVelocity(Vec3 current, int age, SkillEntity entity) {
        return current;
    }

    record Type<T extends FlightPattern>(MapCodec<T> codec) {}
}
