package com.cimdy.coe.data.codec.skill.phase.event;

import com.cimdy.coe.data.codec.skill.phase.event.trigger.CustomSkillEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillTriggerEvent;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.event.combat.DamageEvent;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 技能事件总线 — 每个实体持有一个实例。
 * 技能可以发布事件，也可以通过 EventListenPhase 订阅事件。
 */
public class SkillEventBus {
    private static final Map<UUID, SkillEventBus> ENTITY_BUSES = new ConcurrentHashMap<>();

    private final List<EventListenerEntry> listeners = new ArrayList<>();
    /** 已注册的监听器去重键集合（用于避免同一事件监听随技能重复施放而堆叠）。 */
    private final Set<String> keys = ConcurrentHashMap.newKeySet();
    /** 最近一次发布的事件名（CustomSkillEvent 的 eventName；其它事件为 null），供 MetaSkillAction 自校验条件。 */
    private String lastEventName = null;
    /** 最近一次发布的事件附带标签（如异常类型 chill/ignite），供 MetaSkillAction 的 {@code event_tag:} 条件判定。 */
    private List<String> lastEventTags = List.of();

    public static SkillEventBus forEntity(Entity entity) {
        return ENTITY_BUSES.computeIfAbsent(entity.getUUID(), k -> new SkillEventBus());
    }

    /**
     * 清理指定实体的总线，防止内存泄漏。
     */
    public static void removeEntity(Entity entity) {
        ENTITY_BUSES.remove(entity.getUUID());
    }

    public String getLastEventName() {
        return lastEventName;
    }

    public List<String> getLastEventTags() {
        return lastEventTags;
    }

    /**
     * 统一的元事件发布入口（深度合并核心）：由权威战斗链路（{@code DamageEvent}）与强制施加异常
     * （{@code AilmentAction}）共用，向施法者总线发布 on_crit / on_kill / on_ignite / on_freeze /
     * on_shock 等元事件。cast 或 skill 为 null 时静默跳过（如原版攻击没有技能上下文，不应触发元宝石）。
     */
    public static void publishMeta(Entity caster, SkillInstance skill, String eventName, String... tags) {
        if (caster == null || skill == null) return;
        forEntity(caster).publish(new CustomSkillEvent(caster, skill, eventName,
                Optional.empty(), List.of(tags)));
    }

    /**
     * 无技能实例的元事件发布入口 —— 面向<b>不是技能产生</b>的行为事件（如翻滚 {@code dodge_roll}）。
     *
     * <p>翻滚由按键直接驱动、不经任何技能，故没有 {@code SkillInstance} 可携带；
     * 而 {@link #publishMeta(Entity, SkillInstance, String, String...)} 对 null skill 直接早退，
     * 常驻监听（精魂「翻滚时 X」）便无从订阅。事件的宿主技能来自<b>注册侧</b>（精魂宝石自身），
     * 与事件携带的 skill 无关，故此处传 null 不影响订阅与回调
     * （见 {@code EventListenPhase#tryConsume} 的 {@code event.skill() != null} 判定）。
     */
    public static void publishMeta(Entity caster, String eventName, String... tags) {
        if (caster == null) return;
        forEntity(caster).publish(new CustomSkillEvent(caster, null, eventName,
                Optional.empty(), List.of(tags)));
    }

    /**
     * 注册事件监听器（带去重键）。若同一 key 已注册则跳过，返回 false。
     * 用于 EventListenPhase：技能每次施放都会初始化阶段并试图注册监听，
     * 去重键（实体 + 事件类型 + 事件名 + 动作哈希）保证「同一事件监听终生只注册一次」。
     */
    public boolean registerIfAbsent(String key, Class<? extends SkillTriggerEvent> eventType, EventListener listener) {
        if (!keys.add(key)) return false;
        listeners.add(new EventListenerEntry(eventType, listener));
        return true;
    }

    /**
     * 占位去重键（供需要句柄的可注销注册使用）：与 {@link #registerIfAbsent} 共用同一去重集合，
     * 故两种注册方式彼此幂等 —— 常驻监听（精魂光环）与施放期监听不会重复注册同一份动作。
     */
    public boolean tryReserve(String key) {
        return keys.add(key);
    }

    /**
     * 注册事件监听器。返回一个句柄用于后续取消注册。
     */
    public ListenerHandle register(Class<? extends SkillTriggerEvent> eventType, EventListener listener) {
        EventListenerEntry entry = new EventListenerEntry(eventType, listener);
        listeners.add(entry);
        return () -> listeners.remove(entry);
    }

    /**
     * 取消注册（兼容旧 API）。
     */
    public void unregister(EventListener listener) {
        listeners.removeIf(e -> e.listener == listener);
    }

    /**
     * 发布事件。使用快照迭代避免 ConcurrentModificationException。
     */
    public void publish(SkillTriggerEvent event) {
        if (event instanceof CustomSkillEvent ce) {
            this.lastEventName = ce.eventName();
            this.lastEventTags = ce.tags();
        } else {
            this.lastEventName = null;
            this.lastEventTags = List.of();
        }
        Class<?> eventClass = event.getClass();
        // 只遍历一次，不使用 snapshot 除非 listener 数量很大
        for (int i = listeners.size() - 1; i >= 0; i--) {
            EventListenerEntry entry = listeners.get(i);
            if (entry.eventType.isAssignableFrom(eventClass)) {
                if (entry.listener.tryConsume(event)) {
                    entry.listener.onEvent(event);
                }
            }
        }
    }

    /**
     * 监听器句柄，用于取消注册。
     */
    @FunctionalInterface
    public interface ListenerHandle {
        void unregister();
    }

    public interface EventListener {
        boolean tryConsume(SkillTriggerEvent event);
        void onEvent(SkillTriggerEvent event);
    }

    private record EventListenerEntry(
            Class<? extends SkillTriggerEvent> eventType,
            EventListener listener
    ) {}
}
