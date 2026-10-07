package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.SelfDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.*;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SpawnDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.SpawnDeliveryExecutor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Delivery 类型 → 执行器的注册表。
 * SkillPhase.initialize() 通过此注册表派发执行。
 */
public class DeliveryExecutors {
    private static final Map<Class<?>, DeliveryExecutor<?>> REGISTRY = new ConcurrentHashMap<>();

    static {
        register(ProjectileDelivery.class, new ProjectileDeliveryExecutor());
        register(AreaDelivery.class, new AreaDeliveryExecutor());
        register(SelfDelivery.class, new SelfDeliveryExecutor());
        register(SpawnDelivery.class, new SpawnDeliveryExecutor());
    }

    public static <D extends Delivery> void register(Class<D> type, DeliveryExecutor<D> executor) {
        REGISTRY.put(type, executor);
    }

    @SuppressWarnings("unchecked")
    public static void execute(Delivery delivery, ActionContext context,
                               UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        DeliveryExecutor<Delivery> executor = (DeliveryExecutor<Delivery>) REGISTRY.get(delivery.getClass());
        if (executor != null) {
            executor.execute(delivery, context, casterId, phaseId, instance);
        }
    }
}
