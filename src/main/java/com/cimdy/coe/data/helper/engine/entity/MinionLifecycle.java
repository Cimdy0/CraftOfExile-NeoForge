package com.cimdy.coe.data.helper.engine.entity;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.event.combat.SummonEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.helper.combat.ReviveEntry;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 召唤物生命周期机制（原 {@code event.SummonEvent} 内联逻辑，铁律 #56 迁出）：
 * 限时召唤物到期移除 + <b>可复生召唤物复活</b> + <b>脱战回血</b> + 计数对账。
 *
 * <h3>复活（POE2 语义）</h3>
 * 可复生召唤物 = 带 {@code coe:summoned} 标记且 {@code MINION_EXPIRE == 0} 的永久召唤物（含盟友）。
 * 其死亡<b>不再取消</b>，而是：
 * <ol>
 *   <li>真死：计数 −1（{@link SummonCountHelper#onMinionRemoved}），实体正常从世界移除；</li>
 *   <li>在<b>持有者</b>侧写入一条待复活记录（实体 id / 是否盟友 / 剩余 tick）；</li>
 *   <li><b>共享/联动延迟</b>：任何一只可复生召唤物被击败时，把持有者队列里的<b>全部</b>项剩余时间重置为满；</li>
 *   <li>持有者每 tick 调度：倒计时归零即按其记录重建实体（{@link SpawnAction#spawnMinionEntity}），
 *       重生体为永久召唤物，计数重新 +1（净零）；</li>
 *   <li>队列非空期间持有者显示 {@code minion_reviving} 状态（HUD 标记），清空即剥夺。</li>
 * </ol>
 *
 * <h3>脱战回血</h3>
 * 可复生召唤物在<b>一段时间未受击</b>后按最大生命百分比回血（{@code MINION_LAST_DAMAGED} 记录上次受击刻）。
 */
public final class MinionLifecycle {
    /** 复活基础耗时（tick，5 秒），经持有者 {@link AttributeRegistry#MINION_REVIVE_SPEED} 缩放（倍率越高越快）。 */
    private static final int BASE_REVIVE_TICKS = 100;
    /** {@code minion_reviving} 状态施加时长（tick）：足够覆盖任意复活窗口；队列清空时显式剥夺。 */
    private static final int REVIVE_STATUS_DURATION = 100_000_000;
    /** 脱战回血判定：距上次受击超过该 tick 数（5 秒）才开始回血。 */
    private static final int REGEN_DELAY_TICKS = 100;
    /** 回血节奏（每 {@link #REGEN_INTERVAL_TICKS} tick 一次）与速率（每秒回最大生命的百分比）。 */
    private static final int REGEN_INTERVAL_TICKS = 20;
    private static final double REGEN_PER_SECOND = 0.02;

    private MinionLifecycle() {
    }

    /** 限时召唤物到期销毁（每 20 tick 检查一次）。 */
    public static void onExpireTick(Entity entity) {
        if (entity.level().isClientSide()) return;
        if (entity.tickCount % 20 != 0) return;
        if (!entity.hasData(AttachmentRegistry.MINION_EXPIRE)) return;

        long expire = entity.getData(AttachmentRegistry.MINION_EXPIRE);
        if (expire > 0 && entity.level().getGameTime() >= expire) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        8, 0.3, 0.3, 0.3, 0.02);
            }
            // 限时召唤物到期真销毁（永久召唤物走复活而非本分支，故不在此 −1）
            SummonCountHelper.onMinionRemoved(entity);
            entity.discard();
        }
    }

    /**
     * 召唤物 / 图腾计数低频对账（每 100 tick = 5 秒）：重算真值写回，校正事件计数的漂移
     * （跨维度 / 区块卸载 / 限时召唤物被伤害打死走死亡移除而非到期 {@code discard} 等）。
     */
    public static void onReconcileTick(LivingEntity owner) {
        if (owner.level().isClientSide()) return;
        if (owner.tickCount % 100 != 0) return;
        SummonCountHelper.reconcile(owner);
    }

    /** 可复生召唤物被击败：真死（不取消），在持有者侧登记待复活并重置全部延迟。 */
    public static void onMinionDeath(LivingEntity dead) {
        if (dead.level().isClientSide()) return;
        if (!isRevivableMinion(dead)) return;
        LivingEntity owner = (LivingEntity) OwnerHelper.getOwner(dead);
        if (owner == null || owner.level().isClientSide()) return;
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType());
        if (entityId == null) return;
        boolean companion = AdditionData.getOrDefault(dead, AdditionKeys.IS_COMPANION, 0.0) != 0.0;

        // 真死：计数 −1（复活时由 spawnMinionEntity 重新 +1，净零）
        SummonCountHelper.onMinionRemoved(dead);

        // 写入持有者待复活队列，并把全部项（含本条）剩余时间重置为满（共享/联动延迟：任一被击败即重置全部）
        int full = reviveTicks(owner);
        List<ReviveEntry> pending = owner.hasData(AttachmentRegistry.PENDING_REVIVES)
                ? new ArrayList<>(owner.getData(AttachmentRegistry.PENDING_REVIVES)) : new ArrayList<>();
        pending.add(new ReviveEntry(entityId, companion, full));
        List<ReviveEntry> reset = new ArrayList<>(pending.size());
        for (ReviveEntry e : pending) reset.add(new ReviveEntry(e.entityId(), e.companion(), full));
        owner.setData(AttachmentRegistry.PENDING_REVIVES, List.copyOf(reset));

        // 队列非空期间持有者显示「待复活」状态
        if (!StatusEngine.has(owner, StatusDefs.MINION_REVIVING)) {
            StatusEngine.apply(owner, StatusDefs.MINION_REVIVING, owner, 1.0, REVIVE_STATUS_DURATION, 1);
        }
    }

    /**
     * 持有者侧复活调度：每 tick 递减待复活队列，归零即重生对应召唤物（永久），并维护 {@code minion_reviving} 状态。
     * 仅在持有者持有 {@code PENDING_REVIVES} 附件时介入——绝大多数实体（无召唤物）直接早退，不做每 tick 扫描。
     */
    public static void onOwnerReviveTick(LivingEntity owner) {
        if (owner.level().isClientSide()) return;
        if (!owner.hasData(AttachmentRegistry.PENDING_REVIVES)) return;

        List<ReviveEntry> pending = owner.getData(AttachmentRegistry.PENDING_REVIVES);
        if (pending.isEmpty()) {
            owner.setData(AttachmentRegistry.PENDING_REVIVES, List.of());
            StatusEngine.clear(owner, StatusDefs.MINION_REVIVING);
            return;
        }
        if (!(owner.level() instanceof ServerLevel serverLevel)) return;

        List<ReviveEntry> next = new ArrayList<>();
        for (ReviveEntry e : pending) {
            int left = e.ticksLeft() - 1;
            if (left <= 0) {
                // 在持有者位置重生（永久召唤物：lifetime=0）
                SpawnAction.spawnMinionEntity(serverLevel, owner, e.entityId(), owner.position(), e.companion(), 0);
                // R3 修复 #65：复活完成此刻发射 MINION_REVIVED（驱动「一只召唤物复活时回复生命」类效果）。
                // owner = 复活持有者（获治疗者），标签 minion_revived（事件键）+ minion（GateTag）与
                // EFF_HEAL_HEALTH_MINION_MINION_REVIVED 的 GateExpr 对齐；other=null（ResourceStatEffect 走 self 结算）。
                GameEvent.emit(EventName.MINION_REVIVED, owner, null, Set.of(GateTag.MINION));
            } else {
                next.add(new ReviveEntry(e.entityId(), e.companion(), left));
            }
        }
        owner.setData(AttachmentRegistry.PENDING_REVIVES, List.copyOf(next));

        if (next.isEmpty()) {
            StatusEngine.clear(owner, StatusDefs.MINION_REVIVING);
        } else if (!StatusEngine.has(owner, StatusDefs.MINION_REVIVING)) {
            StatusEngine.apply(owner, StatusDefs.MINION_REVIVING, owner, 1.0, REVIVE_STATUS_DURATION, 1);
        }
    }

    /** 可复生召唤物受击：记录上次受击刻，打断脱战回血窗口。 */
    public static void onMinionDamaged(LivingEntity minion) {
        if (minion.level().isClientSide()) return;
        if (!isRevivableMinion(minion)) return;
        minion.setData(AttachmentRegistry.MINION_LAST_DAMAGED, minion.level().getGameTime());
    }

    /** 可复生召唤物脱战回血：距上次受击超过 {@link #REGEN_DELAY_TICKS} 且未满血，按最大生命百分比回血。 */
    public static void onMinionRegenTick(LivingEntity minion) {
        if (minion.level().isClientSide()) return;
        if (!minion.hasData(AttachmentRegistry.MINION_LAST_DAMAGED)) return;
        if (minion.tickCount % REGEN_INTERVAL_TICKS != 0) return;
        long last = minion.getData(AttachmentRegistry.MINION_LAST_DAMAGED);
        long now = minion.level().getGameTime();
        if (now - last > REGEN_DELAY_TICKS && minion.getHealth() < minion.getMaxHealth()) {
            minion.heal((float) (REGEN_PER_SECOND * minion.getMaxHealth()));
        }
    }

    /** 可复生召唤物：带 {@code coe:summoned} 标记且无到期时间（{@code MINION_EXPIRE == 0}）的永久召唤物。 */
    private static boolean isRevivableMinion(LivingEntity e) {
        return AdditionData.getOrDefault(e, AdditionKeys.SUMMONED, 0.0) != 0.0
                && e.getData(AttachmentRegistry.MINION_EXPIRE) == 0L;
    }

    /** 复活耗时（tick）：基础 5 秒 ÷ 持有者复活速度倍率（下限 0.1 防止除零/无限长）。 */
    private static int reviveTicks(LivingEntity owner) {
        double speed = owner.getAttributeValue(AttributeRegistry.MINION_REVIVE_SPEED);
        return Math.max(1, (int) Math.round(BASE_REVIVE_TICKS / Math.max(0.1, speed)));
    }
}
