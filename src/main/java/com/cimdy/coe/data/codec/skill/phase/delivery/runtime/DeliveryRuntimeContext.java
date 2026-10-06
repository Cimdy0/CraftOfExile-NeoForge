package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 每个 spawned delivery entity 携带的运行时上下文。
 * 存储在 {@link DeliveryTracker} 中，由 entity UUID 索引。
 *
 * <p>辅助宝石的投射物效果（穿透 / 连锁 / 分裂 / 返回 / 分裂降伤）以字段形式携带于此，
 * 由投射物位置（承载实体）按需消费——这正是"辅助 effect 仅作用于
 * 技能中对应位置（投射物）"的关系体现。
 */
public record DeliveryRuntimeContext(
        UUID casterId,
        UUID phaseId,
        Delivery delivery,
        Vec3 origin,
        Vec3 direction,
        int ticksAlive,
        int hitCount,
        int bounceCount,
        int chainCount,
        int depth,
        int supportPierce,
        int supportChain,
        int supportFork,
        boolean returnToCaster,
        boolean returning,
        float damageMultiplier,
        float splitDamageMultiplier,
        List<UUID> hitEntities
) {
    public DeliveryRuntimeContext incrementTick() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive + 1, hitCount, bounceCount, chainCount, depth, supportPierce, supportChain, supportFork,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    public DeliveryRuntimeContext addHit(UUID entityId) {
        List<UUID> list = new ArrayList<>(hitEntities);
        list.add(entityId);
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount + 1, bounceCount, chainCount, depth, supportPierce, supportChain, supportFork,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, list);
    }

    public DeliveryRuntimeContext addBounce() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount, bounceCount + 1, chainCount, depth, supportPierce, supportChain, supportFork,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    /** 记录一次连锁，返回新的上下文。 */
    public DeliveryRuntimeContext addChain() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount, bounceCount, chainCount + 1, depth, supportPierce, supportChain, supportFork,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    /** 消耗分裂能力（分裂出的投射物不再分裂，避免指数爆炸）。 */
    public DeliveryRuntimeContext consumeFork() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount, bounceCount, chainCount, depth, supportPierce, supportChain, 0,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    /** 标记投射物已进入返回施法者阶段。 */
    public DeliveryRuntimeContext addReturning() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount, bounceCount, chainCount, depth, supportPierce, supportChain, supportFork,
                returnToCaster, true, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    /** 递归深度 +1（弹射/连锁/分裂/分裂叉 复合递归时传播给子投射物）。 */
    public DeliveryRuntimeContext incrementDepth() {
        return new DeliveryRuntimeContext(casterId, phaseId, delivery, origin, direction,
                ticksAlive, hitCount, bounceCount, chainCount, depth + 1, supportPierce, supportChain, supportFork,
                returnToCaster, returning, damageMultiplier, splitDamageMultiplier, hitEntities);
    }

    public boolean hasHit(UUID entityId) {
        return hitEntities.contains(entityId);
    }
}
