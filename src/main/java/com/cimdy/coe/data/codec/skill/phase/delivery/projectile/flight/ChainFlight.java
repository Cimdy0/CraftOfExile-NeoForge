package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPatternTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import com.cimdy.coe.entity.custom.skill.SkillEntity;

/**
 * 链跳飞行 — 直线飞行，碰撞时由 CollisionPolicy 处理链跳。
 */
public record ChainFlight(
        /**
         * 死字段：连锁次数上限实际由投射物携带的 {@code supportChain}（辅助宝石链数值）决定，
         * 见 {@link com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext#supportChain()} 与
         * {@code SkillArrow} 的连锁分支；本字段无读取点，保留仅为避免破坏既有 codec，待后续移除。
         */
        ExpressionCodec.Value maxChains,
        ExpressionCodec.Value chainRange
) implements FlightPattern {
    public static final MapCodec<ChainFlight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("max_chains").forGetter(ChainFlight::maxChains),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("chain_range", ExpressionCodec.of(10.0))
                    .forGetter(ChainFlight::chainRange)
    ).apply(i, ChainFlight::new));

    @Override public FlightPattern.Type<?> getType() { return FlightPatternTypes.CHAIN; }

    @Override
    public Vec3 initialVelocity(Vec3 direction, double speed) {
        return direction.normalize().scale(speed);
    }
}
