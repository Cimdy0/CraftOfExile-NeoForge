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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 爆炸 — 命中后产生范围伤害。
 */
public record Explode(ExpressionCodec.Value radius) implements CollisionPolicy {
    public static final MapCodec<Explode> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("radius").forGetter(Explode::radius)
    ).apply(i, Explode::new));

    @Override public CollisionPolicy.Type<?> getType() { return CollisionPolicyTypes.EXPLODE; }

    @Override
    public boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx) {
        explode(entity);
        return false;
    }

    @Override
    public boolean onHitBlock(SkillEntity entity, BlockHitResult result, DeliveryRuntimeContext ctx) {
        explode(entity);
        return false;
    }

    private void explode(SkillEntity entity) {
        Entity owner = entity.getOwner();
        Level level = entity.level();
        level.explode(owner, entity.getX(), entity.getY(), entity.getZ(),
                radius.getFloat(), Level.ExplosionInteraction.MOB);
    }
}
