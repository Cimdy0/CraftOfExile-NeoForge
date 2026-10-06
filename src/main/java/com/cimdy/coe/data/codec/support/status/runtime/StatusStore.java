package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

/**
 * 实体的全部状态实例容器（作为附件挂在 LivingEntity 上，取代原 StatusContents）。
 *
 * <p>Map 以 {@link StatusType} 为键，保证「状态彼此独立」（文档 §0.4）。tickAll 由
 * {@link StatusEngine#tick(LivingEntity)} 驱动；本类仅持有数据。
 */
public final class StatusStore {

    public static final StatusStore EMPTY = new StatusStore();

    public final Map<StatusType, StatusInstance> instances = new LinkedHashMap<>();

    /**
     * POE2 异常「积蓄条（buildup meter）」：{@code type -> 当前积蓄值}（0..≥1，满 1.0 即达门槛）。
     * 由 {@link StatusEngine#addBuildup}、{@code resetBuildup} 维护，{@link #tickAll} 每 tick 衰减。
     * 仅运行时内存状态，不持久化（重启后积累清零，下次命中重新积累）。
     */
    public final Map<StatusType, Double> buildup = new HashMap<>();

    /** 每 tick 积蓄条衰减比例（约 1.6 秒衰减到 0），保证「连续命中才能积累满」且不会永久挂账。 */
    private static final double BUILDUP_DECAY_PER_TICK = 0.01;

    public StatusInstance get(StatusType type) {
        return instances.get(type);
    }

    public boolean has(StatusType type) {
        return instances.containsKey(type);
    }

    public void put(StatusInstance inst) {
        instances.put(inst.type, inst);
    }

    public void remove(StatusType type) {
        instances.remove(type);
    }

    public double magnitude(StatusType type) {
        StatusInstance i = instances.get(type);
        return i == null ? 0.0 : i.magnitude;
    }

    public int stacks(StatusType type) {
        StatusInstance i = instances.get(type);
        return i == null ? 0 : i.stacks;
    }

    public List<StatusInstance> tickAll(LivingEntity target) {
        // 收集本 tick 到期的实例，遍历结束后再由 StatusEngine 统一处理到期副作用（恢复冷却等），
        // 避免副作用内施加新状态（如 Archon 恢复）时向正在迭代的 instances Map 写入导致并发修改异常。
        List<StatusInstance> expired = new ArrayList<>();
        Iterator<Map.Entry<StatusType, StatusInstance>> it = instances.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StatusType, StatusInstance> e = it.next();
            StatusInstance inst = e.getValue();
            inst.type.tickLogic.tick(inst, target);
            // 所有状态（含叠层）统一按剩余时长衰减；层数仅表达强度，不用于到期判定。
            inst.durationTicks--;
            if (inst.durationTicks <= 0) {
                StatusStatBridge.remove(target, inst);
                it.remove();
                expired.add(inst);
            }
        }
        // POE2 积蓄条衰减：未触发的积蓄随时间回落，强迫「连续命中才能积累满」。
        Iterator<Map.Entry<StatusType, Double>> bit = buildup.entrySet().iterator();
        while (bit.hasNext()) {
            Map.Entry<StatusType, Double> e = bit.next();
            double v = e.getValue() - BUILDUP_DECAY_PER_TICK;
            if (v <= 0) bit.remove();
            else e.setValue(v);
        }
        return expired;
    }

    // ── Codec：以 type.id -> instance 序列化 ──
    public static final Codec<StatusStore> CODEC = Codec.unboundedMap(Codec.STRING, StatusInstance.CODEC)
            .xmap(
                    map -> {
                        StatusStore s = new StatusStore();
                        map.forEach((id, inst) -> {
                            if (inst != null && inst.type != null) s.instances.put(inst.type, inst);
                        });
                        return s;
                    },
                    s -> {
                        Map<String, StatusInstance> m = new HashMap<>();
                        s.instances.forEach((t, i) -> m.put(t.id, i));
                        return m;
                    });
}
