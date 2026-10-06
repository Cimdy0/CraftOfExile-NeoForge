package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPatternTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 追踪飞行 — 投射物追踪目标。
 */
public record HomingFlight(
        ExpressionCodec.Value turnRate
) implements FlightPattern {
    public static final MapCodec<HomingFlight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("turn_rate").forGetter(HomingFlight::turnRate)
    ).apply(i, HomingFlight::new));

    @Override public FlightPattern.Type<?> getType() { return FlightPatternTypes.HOMING; }

    @Override
    public Vec3 initialVelocity(Vec3 direction, double speed) {
        return direction.normalize().scale(speed);
    }

    @Override
    public Vec3 tickVelocity(Vec3 current, int age, SkillEntity entity) {
        Level level = entity.level();
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 就近排序）
        var nearest = ExpressionContext.of(level, entity).queryEntity(
                EntityQuery.box(entity.getBoundingBox().inflate(15))
                        .exclude(entity.getOwner()).aliveOnly()
                        .nearestTo(entity.position()));
        if (nearest.isEmpty()) return current;

        Vec3 desired = nearest.get().position().subtract(entity.position()).normalize().scale(current.length());
        return current.lerp(desired, (float) turnRate.getFloat());
    }
}
