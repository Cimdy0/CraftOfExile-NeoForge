package com.cimdy.coe.event;

import com.cimdy.coe.data.helper.engine.DamagePipeline;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.world.EntityOperationEvent;
import com.mojang.serialization.DataResult;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 事件能力矩阵（单一事实源）+ 事件键校验器（B3）。
 * <p>
 * 自动反射 {@link EventName} 全部 {@code public static final String} 常量，作为权威事件键集合；
 * 任何 JSON 引用的事件键若既不在 {@link EventName}、也不匹配动态前缀白名单，即视为未知键——
 * 在解码（{@link #checkEventKey}）/ mod init（{@link #audit}）两处即报错，不再静默新建
 * {@code Event.State}（止血 doc/code/键名漂移，如 prior {@code recent}≠{@code recently}）。
 * 无构建期审计 provider：事件键在生成期全部由 {@link EventName} 字段接线产出。
 */
public final class EventCatalog {

    /** 权威事件键集合（反射自 EventName）。 */
    private static final Set<String> ALL_KEYS = new HashSet<>();

    /** 动态前缀白名单：拼接型键，非 EventName 常量（skill 事件 / 充能球存取）。 */
    private static final Set<String> PREFIXES = Set.of("skill_event:", "gain_", "consume_");

    /** 可选文档/元数据表：键 = 事件键，值 = 双键对 / 记录标签 / 发布点。audit() 校验其键均合法。 */
    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    private EventCatalog() {}

    static {
        for (Field f : EventName.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                    && f.getType() == String.class) {
                f.setAccessible(true);
                try {
                    String v = (String) f.get(null);
                    if (v != null) ALL_KEYS.add(v);
                } catch (IllegalAccessException ignored) {
                    // 反射不可达不应发生（同模块 public 常量），忽略。
                }
            }
        }
        // 已知双键对（文档/元数据；可选，audit 校验其键均注册到 EventName）。
        put(EventName.CAUSED_HIT, EventName.TAKEN_HIT, "DamagePipeline.stageTrigger");
        put(EventName.CAUSED_CRIT, EventName.TAKEN_CRIT, "DamagePipeline.stageTrigger");
        put(EventName.CAUSED_AILMENT, EventName.TAKEN_AILMENT, "AilmentHelper");
        put(EventName.CAUSED_DOT, EventName.TAKEN_DOT, "AilmentHelper.applyDamageDoT");
        put(EventName.KILL, null, "DamageEvent");
        put(EventName.TAKEN_DEATH, null, "DamageEvent");
        put(EventName.ATTACK_START, null, "DamagePipeline");
        put(EventName.ENTER_AREA, null, "EntityOperationEvent");
        put(EventName.JOIN_LEVEL, null, "EntityOperationEvent");
        put(EventName.LEAVE_LEVEL, null, "EntityOperationEvent");
    }

    private static void put(String key, @Nullable String dual, String publishPoint) {
        ENTRIES.put(key, new Entry(key, dual, publishPoint));
        if (dual != null) ENTRIES.put(dual, new Entry(dual, key, publishPoint));
    }

    /** 文档/元数据条目。 */
    public record Entry(String key, @Nullable String dual, String publishPoint) {}

    /** 事件键是否已知（EventName 常量，或匹配任一动态前缀）。 */
    public static boolean known(String name) {
        if (name == null) return false;
        if (ALL_KEYS.contains(name)) return true;
        for (String p : PREFIXES) if (name.startsWith(p)) return true;
        return false;
    }

    /** 校验某节点引用的事件键，返回解码结果（供 Codec flatXmap 使用）。 */
    public static <T> DataResult<T> checkEventKey(T node, String key) {
        return known(key)
                ? DataResult.success(node)
                : DataResult.error(() -> "未知事件键: " + key
                    + "（不在 EventName，也不匹配动态前缀 skill_event:/gain_/consume_）");
    }

    /** mod init 调用：校验元数据表键全部合法（防止 EventName 改动后文档漂移）。 */
    public static void audit() {
        for (Map.Entry<String, Entry> e : ENTRIES.entrySet()) {
            if (!ALL_KEYS.contains(e.getKey())) {
                throw new IllegalStateException("EventCatalog 元数据键未注册到 EventName: " + e.getKey());
            }
            Entry v = e.getValue();
            if (v.dual() != null && !ALL_KEYS.contains(v.dual())) {
                throw new IllegalStateException("EventCatalog 双键未注册到 EventName: " + v.dual());
            }
        }
    }
}
