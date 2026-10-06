package com.cimdy.coe.event.skill;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.skill.TalentPerkHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 仅派发：天赋「工具能力」的全部机制在 {@link TalentPerkHelper}（铁律 #56）。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class TalentPerkEvent {
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        TalentPerkHelper.onBlockDrops(event.getBreaker(), (ServerLevel) event.getLevel(), event.getDrops());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        TalentPerkHelper.onLivingDrops(event.getSource().getEntity(), event.getDrops());
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        TalentPerkHelper.onPickupXp(event.getEntity(), event.getOrb());
    }

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(player.level() instanceof ServerLevel)) return;
        TalentPerkHelper.onPlayerTick(player);
    }
}
