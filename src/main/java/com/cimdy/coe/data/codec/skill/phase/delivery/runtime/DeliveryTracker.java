package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端映射: entity UUID → DeliveryRuntimeContext。
 * entity 被 discard 时必须 remove，防内存泄漏。
 */
public class DeliveryTracker {
    private static final ConcurrentHashMap<UUID, DeliveryRuntimeContext> ACTIVE = new ConcurrentHashMap<>();

    public static void register(UUID entityUuid, DeliveryRuntimeContext ctx) {
        ACTIVE.put(entityUuid, ctx);
    }

    public static DeliveryRuntimeContext get(UUID entityUuid) {
        return ACTIVE.get(entityUuid);
    }

    /** 更新已有 context（如 addHit / incrementTick / addBounce 后写回） */
    public static void update(UUID entityUuid, DeliveryRuntimeContext ctx) {
        ACTIVE.put(entityUuid, ctx);
    }

    public static void remove(UUID entityUuid) {
        ACTIVE.remove(entityUuid);
    }

    public static boolean has(UUID entityUuid) {
        return ACTIVE.containsKey(entityUuid);
    }
}
