package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.BallDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.BeamDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.HazardDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.OrbDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.RemnantDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.SelfDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ThrownDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.*;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ThrownDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BallDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BeamDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.WaveDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.WaveDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.HazardDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;

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
        register(HazardDelivery.class, new HazardDeliveryExecutor());
        register(RemnantDelivery.class, new RemnantDeliveryExecutor());
        register(ThrownDelivery.class, new ThrownDeliveryExecutor());
        register(BallDelivery.class, new BallDeliveryExecutor());
        register(OrbDelivery.class, new OrbDeliveryExecutor());
        register(BeamDelivery.class, new BeamDeliveryExecutor());
        register(WaveDelivery.class, new WaveDeliveryExecutor());
        register(SelfDelivery.class, new SelfDeliveryExecutor());
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
