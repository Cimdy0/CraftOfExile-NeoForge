package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicyTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;

/**
 * 穿透 — 命中实体继续飞行，最多穿透 N 次。
 */
public record Pierce(ExpressionCodec.Value maxPierce) implements CollisionPolicy {
    public static final MapCodec<Pierce> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("max_pierce").forGetter(Pierce::maxPierce)
    ).apply(i, Pierce::new));

    @Override public CollisionPolicy.Type<?> getType() { return CollisionPolicyTypes.PIERCE; }

    @Override
    public boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx) {
        return ctx.hitCount() < maxPierce.getInt();
    }
}
