package com.cimdy.coe.event.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.ItemTooltipAssembler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 仅派发：把 tooltip 组装交给 {@link ItemTooltipAssembler}（铁律 #56）。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class TooltipEvent {
    @SubscribeEvent
    public static void ItemTooltipEvent(ItemTooltipEvent event) {
        ItemTooltipAssembler.append(event.getItemStack(), event.getEntity(), event.getToolTip());
    }
}
