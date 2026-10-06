package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicyTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryTracker;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 弹射 — 命中方块后反弹。
 */
public record Bounce(ExpressionCodec.Value maxBounces) implements CollisionPolicy {
    public static final MapCodec<Bounce> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("max_bounces").forGetter(Bounce::maxBounces)
    ).apply(i, Bounce::new));

    @Override public CollisionPolicy.Type<?> getType() { return CollisionPolicyTypes.BOUNCE; }

    @Override
    public boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx) {
        return false; // 命中实体即停止
    }

    @Override
    public boolean onHitBlock(SkillEntity entity, BlockHitResult result, DeliveryRuntimeContext ctx) {
        // 递归深度安全上限：超过 delivery 声明的 maxDepth 即停止弹射（兜底，不影响合法有限深度）。
        if (ctx.depth() >= ctx.delivery().maxDepth() || ctx.bounceCount() >= maxBounces.getInt()) return false;
        Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getUnitVec3i());
        Vec3 vel = entity.getDeltaMovement();
        Vec3 reflected = vel.subtract(normal.scale(2 * vel.dot(normal)));
        entity.setDeltaMovement(reflected);
        DeliveryTracker.update(entity.getUUID(), ctx.addBounce());
        return true;
    }
}
