package com.cimdy.coe.event.combat.status;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.status.runtime.StatusControls;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStatBridge;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.StatusHudBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class StatusEvent {
    @SubscribeEvent
    public static void tick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity instanceof LivingEntity living && entity.hasData(AttachmentRegistry.STATUS_STORE)) {
            StatusEngine.tick(living);
            StatusControls.apply(living);
            // 每秒重同步状态→Stat 贡献（覆盖天赋重建 STAT_STORE 的清空）
            if (living.tickCount % 20 == 0) {
                StatusStatBridge.resync(living);
            }
            // 每 10 tick 汇总状态快照到同步附件，供客户端 HUD 渲染
            if (living instanceof Player && living.tickCount % 10 == 0) {
                StatusHudBuilder.rebuild((Player) living);
            }
        }
    }

}
