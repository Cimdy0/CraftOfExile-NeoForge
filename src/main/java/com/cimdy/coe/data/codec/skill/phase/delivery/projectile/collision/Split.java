package com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicyTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 分裂 — 命中后分裂为多个投射物。
 *
 * <p>不再 {@code new SkillArrow}：改用统一的 {@link ProjectileDeliveryExecutor#spawnProjectile} 工厂，
 * 使分裂体与原投射物走同一「数据驱动承载 + 投射物路径」，可指向任意 {@code coe:entity} 引用。
 */
public record Split(ExpressionCodec.Value splitCount, ExpressionCodec.Value spreadAngle)
        implements CollisionPolicy {
    public static final MapCodec<Split> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("split_count").forGetter(Split::splitCount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("spread_angle", ExpressionCodec.of(15.0))
                    .forGetter(Split::spreadAngle)
    ).apply(i, Split::new));

    @Override public CollisionPolicy.Type<?> getType() { return CollisionPolicyTypes.SPLIT; }

    @Override
    public boolean onHitEntity(SkillEntity entity, Entity target, DeliveryRuntimeContext ctx) {
        int count = splitCount.getInt();
        // 递归深度安全上限：超过 delivery 声明的 maxDepth 即不再分裂（避免指数爆炸式递归）。
        if (count <= 0 || ctx.depth() >= ctx.delivery().maxDepth()) return false;
        float spread = spreadAngle.getFloat();
        Vec3 baseDir = entity.getDeltaMovement().normalize();
        Level level = entity.level();
        Entity owner = entity.getOwner();
        double speed = entity.getDeltaMovement().length();
        if (!(level instanceof ServerLevel serverLevel)) return false;

        // 子投射物复用同一投递（其 ref 即父形态引用），继承父深度 +1，由工厂装配形态与运行时上下文。
        ProjectileDelivery delivery = (ProjectileDelivery) ctx.delivery();
        for (int i = 0; i < count; i++) {
            double angle = (i - (count - 1) / 2.0) * Math.toRadians(spread);
            Vec3 newDir = baseDir.yRot((float) angle);
            ProjectileDeliveryExecutor.spawnProjectile(serverLevel, delivery,
                    ctx.casterId(), ctx.phaseId(), entity.position(), newDir, owner,
                    ctx.depth() + 1, (float) speed, 0, 0, 0, false, 1.0f, 1.0f);
            // 子投射物继承父 context（深度 +1，传播给下一层递归），并增加阶段实例引用计数
            SkillPhaseInstanceTracker.retain(ctx.casterId(), ctx.phaseId());
        }
        return false; // 原投射物停止
    }
}
