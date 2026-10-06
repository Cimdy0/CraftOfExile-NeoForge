package com.cimdy.coe.event.world;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.engine.MovingLightManager;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 移动光源 tick 钩子（服务端）。逻辑全在 {@link MovingLightManager}，本类只负责挂点：
 * <ul>
 *   <li>{@link EntityTickEvent.Post} → 对齐锚点（Manager 内部按 3 tick 节流）；</li>
 *   <li>{@link EntityLeaveLevelEvent} → 实体离场（死亡移除 / 换维度 / 登出）撤锚；</li>
 *   <li>{@link ServerStoppingEvent} → 关停前清空全部锚点，防存档残留隐形光块。</li>
 * </ul>
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class MovingLightEvent {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity living) {
            MovingLightManager.tick(living);
        }
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity living) {
            MovingLightManager.clear(living);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MovingLightManager.clearAll(event.getServer());
    }
}
