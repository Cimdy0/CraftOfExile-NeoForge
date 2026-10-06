package com.cimdy.coe.event.combat.status;

import com.cimdy.coe.data.helper.engine.ground.VolatilityDetonationManager;

import com.cimdy.coe.CraftOfExile;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 灌入残迹（Infusion Remnant）每 tick 驱动：遍历各维度的残迹标记，处理寿命衰减与玩家拾取。
 * 经 {@code @EventBusSubscriber} 自动注册（与 {@code VolatilityDetonationManager} 同范式）。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class RemnantEvent {

    private RemnantEvent() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ResourceRegistry.tickRemnants(level);
        }
    }
}
