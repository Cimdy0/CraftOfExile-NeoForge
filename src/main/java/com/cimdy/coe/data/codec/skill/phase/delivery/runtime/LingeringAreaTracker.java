package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.event.skill.SkillEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 持续区域脉冲调度器 — AreaDelivery 的 Lingering 持续时间模式运行时。
 *
 * <p>AreaDeliveryExecutor 在首次即时命中后，若 duration 为 {@code LingeringDuration}，
 * 将（中心 / 半径 / 脉冲间隔 / 到期刻 / 阶段实例）登记到本调度器；
 * 由 {@code SkillEvent.onSkillEvent}（ServerTickEvent.Post）按维度每 tick 驱动：
 * 到达脉冲刻时重新扫描半径内实体并逐个点燃 ON_HIT（复用与即时模式相同的伤害管线），
 * 到期后移除条目。
 *
 * <p>服务端内存态：重启丢失可接受——持续区域本就是短生命周期效果。
 */
public final class LingeringAreaTracker {

    private record Entry(
            UUID casterId,
            SkillPhaseInstance instance,
            Vec3 center,
            double radius,
            int interval,
            long nextPulse,
            long expireAt
    ) {
        Entry withNextPulse(long next) {
            return new Entry(casterId, instance, center, radius, interval, next, expireAt);
        }
    }

    /** 按维度分桶，避免跨维度扫描。 */
    private static final ConcurrentHashMap<ResourceKey<Level>, Queue<Entry>> PENDING = new ConcurrentHashMap<>();

    private LingeringAreaTracker() {}

    /**
     * 登记一个持续区域。
     *
     * @param level         所在维度
     * @param casterId      施法者 UUID（脉冲扫描时排除自身）
     * @param center        区域中心（登记时固定，不随施法者移动）
     * @param radius        检测半径（已含辅助宝石半径倍率）
     * @param intervalTicks 脉冲间隔（tick，>=1）
     * @param durationTicks 持续时长（tick，>=1）
     * @param instance      阶段实例（脉冲命中经其点燃 ON_HIT）
     */
    public static void register(ServerLevel level, UUID casterId, Vec3 center, double radius,
                                int intervalTicks, int durationTicks, SkillPhaseInstance instance) {
        long now = level.getGameTime();
        Entry entry = new Entry(casterId, instance, center, radius,
                Math.max(1, intervalTicks),
                now + Math.max(1, intervalTicks),
                now + Math.max(1, durationTicks));
        PENDING.computeIfAbsent(level.dimension(), k -> new ConcurrentLinkedQueue<>()).add(entry);
    }

    /** 每 tick 由 SkillEvent（ServerTickEvent.Post）对每个维度调用。 */
    public static void tick(ServerLevel level) {
        Queue<Entry> queue = PENDING.get(level.dimension());
        if (queue == null || queue.isEmpty()) return;
        long now = level.getGameTime();

        Iterator<Entry> it = queue.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (now >= entry.expireAt()) {
                it.remove();
                continue;
            }
            if (now < entry.nextPulse()) continue;

            pulse(level, entry);
            // 就地替换为推进了脉冲刻的新条目（Queue 无 set，采用移除后重加，顺序不敏感）
            it.remove();
            queue.add(entry.withNextPulse(entry.nextPulse() + entry.interval()));
        }
    }

    /** 单次脉冲：扫描半径内存活实体（排除施法者），逐个点燃 ON_HIT。 */
    private static void pulse(ServerLevel level, Entry entry) {
        Entity caster = level.getEntity(entry.casterId());
        Vec3 center = entry.center();
        double radius = entry.radius();

        // 获取实体统一经 Context 数据基站回调（EntityQuery 球形扫描）
        List<LivingEntity> targets = ExpressionContext.of(level, caster).queryEntities(
                EntityQuery.sphere(center, radius).exclude(caster).aliveOnly());
        for (LivingEntity target : targets) {
            HitContext hit = HitContext.entityHit(target,
                    target.position(),
                    center.subtract(target.position()).normalize(),
                    (float) center.distanceTo(target.position()));
            entry.instance().fireTrigger(LifecycleTrigger.ON_HIT, hit);
        }
    }
}
