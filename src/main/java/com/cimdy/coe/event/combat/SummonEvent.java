package com.cimdy.coe.event.combat;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.engine.entity.MinionLifecycle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 仅派发：召唤物生命周期机制全部在 {@link MinionLifecycle}（铁律 #56）。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class SummonEvent {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        MinionLifecycle.onExpireTick(entity);
        if (entity instanceof LivingEntity owner) MinionLifecycle.onReconcileTick(owner);
    }

    /**
     * 可复生召唤物被击败：真死（不取消），在持有者侧登记待复活并重置全部延迟。
     * 用 HIGHEST 优先拿到处理权（早于尸体 / 掉落等监听）；仅登记，不干预死亡流程。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMinionDeath(LivingDeathEvent event) {
        MinionLifecycle.onMinionDeath(event.getEntity());
    }

    /** 持有者侧复活调度：每 tick 递减待复活队列，归零即重生对应召唤物。 */
    @SubscribeEvent
    public static void onOwnerReviveTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity owner) MinionLifecycle.onOwnerReviveTick(owner);
    }

    /** 可复生召唤物受击：记录上次受击刻。 */
    @SubscribeEvent
    public static void onMinionDamage(LivingDamageEvent.Pre event) {
        MinionLifecycle.onMinionDamaged(event.getEntity());
    }

    /** 可复生召唤物脱战回血。 */
    @SubscribeEvent
    public static void onMinionRegenTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity minion) MinionLifecycle.onMinionRegenTick(minion);
    }
}
