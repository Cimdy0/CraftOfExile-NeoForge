package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按 (casterId, phaseId) 索引活跃的 SkillPhaseInstance，供 entity 命中回调 O(1) 反查。
 *
 * 使用引用计数管理生命周期：每次生成/分裂投射物时 {@link #retain}，
 * 投射物销毁时 {@link #release}，归零即移除，避免内存泄漏。
 */
public class SkillPhaseInstanceTracker {
    private record Entry(SkillPhaseInstance instance, int refCount) {
        Entry retain() {
            return new Entry(instance, refCount + 1);
        }

        Entry release() {
            return new Entry(instance, refCount - 1);
        }

        boolean isDead() {
            return refCount <= 0;
        }
    }

    private static final ConcurrentHashMap<String, Entry> INDEX = new ConcurrentHashMap<>();

    public static void register(UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        INDEX.put(key(casterId, phaseId), new Entry(instance, 1));
    }

    public static void retain(UUID casterId, UUID phaseId) {
        INDEX.computeIfPresent(key(casterId, phaseId), (k, e) -> e.retain());
    }

    public static void release(UUID casterId, UUID phaseId) {
        String k = key(casterId, phaseId);
        Entry e = INDEX.get(k);
        if (e == null) return;
        Entry updated = e.release();
        if (updated.isDead()) INDEX.remove(k);
    }

    /**
     * 按 (casterId, phaseId) 反查阶段实例并点燃指定触发器 —— 世界物件（残片 / 危害物 / Orb /
     * 手雷 / 球）回调技能阶段的<b>唯一入口</b>。
     *
     * <p>二者任一为空即静默跳过：<b>存档重载后 phaseId 不持久化</b>，阶段实例已不存在，
     * 此时点燃只会产生「没有结算的假触发」（伤害 / 事件全部落空）。
     */
    public static void fire(@Nullable UUID casterId, @Nullable UUID phaseId,
                            LifecycleTrigger trigger, HitContext hit) {
        if (casterId == null || phaseId == null) return;
        SkillPhaseInstance phase = find(casterId, phaseId);
        if (phase != null) {
            phase.fireTrigger(trigger, hit);
        }
    }

    public static SkillPhaseInstance find(UUID casterId, UUID phaseId) {
        Entry e = INDEX.get(key(casterId, phaseId));
        return e == null ? null : e.instance();
    }

    public static boolean contains(UUID casterId, UUID phaseId) {
        return INDEX.containsKey(key(casterId, phaseId));
    }

    private static String key(UUID casterId, UUID phaseId) {
        return casterId.toString() + ":" + phaseId.toString();
    }
}
