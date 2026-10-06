package com.cimdy.coe.event;

import com.cimdy.coe.data.codec.skill.helper.cast.ProjectileHelper;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.event.world.EntityOperationEvent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件缓存「按需订阅」注册表。
 * <p>
 * 目标：事件不再全量固定存储——事件名只在被 Stat 条目实际引用（recently:/count:/time_/elapsed 等节点）
 * 时才被 {@link Event#record} 记录并计时。驱动方式：
 * <ul>
 *   <li>{@link #COUNTS}：事件名 → 引用计数。由各实体 StatStore 的引用集（{@code EventRefCollector} 提取）
 *       在 {@code AttributeEvent} tick 对齐点 diff 驱动（增订/退订），归零即移除；</li>
 *   <li>{@link #MECHANICAL}：机制必需白名单——不依赖 stat 引用也必须记录的事件名
 *       （leech 供 LivingStateValue、parry 招架姿态、caused_hit/taken_hit/kill 命中与击杀 recency 主干）；</li>
 *   <li>{@code presenceNeeded}：是否有 stat 引用 presence 节点（recently_entered/recently_left），
 *       驱动 {@code EntityOperationEvent} 的每 tick 全域进/出扫描开关（无引用恒关闭）。</li>
 * </ul>
 * 线程安全：{@code setData} 可能发生在任意事件回调中。
 */
public final class EventSubscription {

    /** 事件名 → 引用计数（有任一 stat 引用即 > 0）。 */
    private static final Map<String, Integer> COUNTS = new ConcurrentHashMap<>();

    /** 机制必需事件名：无论是否有 stat 引用都必须记录（防止按需订阅破坏核心机制/跨系统消费）。 */
    private static final Set<String> MECHANICAL = Set.of(
            EventName.LEECH, EventName.PARRY,
            EventName.CAUSED_HIT, EventName.TAKEN_HIT, EventName.KILL,
            EventName.TAKEN_DOT, EventName.CAUSED_DOT, EventName.RAGE_SPENT,
            // 弩装填完成：Java 读点 ProjectileHelper#useAmmo 直接经 Event.get(..).recently(RELOADED, ..) 消费，
            // 无 stat 表达式引用（MarkerStatEffect 不携带表达式），故须恒记录才能让该读点真实生效。
            EventName.RELOADED);

    /** 是否有 stat 引用 presence 节点（驱动每 tick 全域进/出扫描）；引用计数（多实体各贡献一次）。 */
    private static final AtomicInteger PRESENCE_COUNT = new AtomicInteger(0);

    private EventSubscription() {
    }

    /** 事件名当前是否应被记录（机制白名单 或 被 stat 订阅）。 */
    public static boolean isSubscribed(String name) {
        return MECHANICAL.contains(name) || COUNTS.containsKey(name);
    }

    /** 订阅事件名（引用计数 +1）。 */
    public static void subscribe(String name) {
        COUNTS.merge(name, 1, Integer::sum);
    }

    /** 退订事件名（引用计数 -1，归零移除）。 */
    public static void unsubscribe(String name) {
        COUNTS.computeIfPresent(name, (k, v) -> v <= 1 ? null : v - 1);
    }

    /** presence 需求计数调整（+1 有实体引用 / -1 实体退场）；>0 即启用扫描。 */
    public static void adjustPresence(int delta) {
        PRESENCE_COUNT.addAndGet(delta);
    }

    /** presence 扫描是否被需要（恒 false 时 EntityOperationEvent 不执行每 tick 全域扫描）。 */
    public static boolean isPresenceNeeded() {
        return PRESENCE_COUNT.get() > 0;
    }

    /**
     * 实体退场清理（M1：防订阅计数泄漏）：撤销该实体经 tick 对齐贡献的全部订阅与 presence 计数，
     * 并清空其 ExpressionContext 缓存。由 {@code EntityOperationEvent} 的离开世界回调调用。
     */
    public static void cleanup(LivingEntity living) {
        ExpressionContext ctx = ExpressionContext.get(living);
        for (String n : ctx.getSubscribedEvents()) {
            unsubscribe(n);
        }
        ctx.setSubscribedEvents(Set.of());
        if (ctx.isPresenceSubscribed()) {
            adjustPresence(-1);
            ctx.setPresenceSubscribed(false);
        }
    }

}
