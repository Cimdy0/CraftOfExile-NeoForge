package com.cimdy.coe.data.codec.skill.phase.delivery.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 碰撞策略 — 投射物命中实体/方块后的行为。
 */
public interface CollisionPolicy {
    Registry<CollisionPolicy.Type<?>> REGISTRY = CollisionPolicyTypes.REGISTRY;

    Codec<CollisionPolicy> CODEC = CollisionPolicyTypes.REGISTRY.byNameCodec()
            .dispatch(CollisionPolicy::getType, CollisionPolicy.Type::codec);

    Type<?> getType();

    /**
     * 投射物命中实体时调用。
     * @return true 继续飞行（穿透/分裂），false 停止
     */
    boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx);

    /**
     * 投射物命中方块时调用，默认停止。
     * @return true 继续飞行（弹射），false 停止
     */
    default boolean onHitBlock(SkillEntity entity, BlockHitResult result, DeliveryRuntimeContext ctx) {
        return false;
    }

    record Type<T extends CollisionPolicy>(MapCodec<T> codec) {}
}
