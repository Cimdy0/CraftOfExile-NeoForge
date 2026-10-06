package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicyTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;

/**
 * 停止 — 命中即消失。
 */
public record Stop() implements CollisionPolicy {
    public static final MapCodec<Stop> CODEC = MapCodec.unit(new Stop());
    @Override public CollisionPolicy.Type<?> getType() { return CollisionPolicyTypes.STOP; }

    @Override
    public boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx) {
        return false; // 停止
    }
}
