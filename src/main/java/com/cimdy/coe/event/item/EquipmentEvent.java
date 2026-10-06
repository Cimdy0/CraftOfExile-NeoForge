package com.cimdy.coe.event.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.data.helper.skill.EquipmentSkillHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class EquipmentEvent {
    @SubscribeEvent
    public static void updateItemLimiter(LivingEquipmentChangeEvent event) {
        LivingEntity living = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        ItemStack toStack = event.getTo();
        ItemStack fromStack = event.getFrom();
        if (living.level() instanceof ServerLevel) {
            AttributeLimiterHelper.applyItemLimiter(living, toStack, slot);
            AttributeLimiterHelper.removeItemLimiter(living, fromStack, slot);
            // 装备授予技能：与属性条目同款来源前缀、对称注入/移除（EquipmentSkillHelper）
            EquipmentSkillHelper.grant(living, toStack, slot);
            EquipmentSkillHelper.revoke(living, fromStack, slot);
            // 玩家侧：装备技能变化后立即按「手动 + 装备」合并视图重算按键绑定，
            // 使装备授予的技能在穿戴当刻即被绑定到空闲按键、可被施放（无需重开装配界面）。
            if (living instanceof Player player) {
                player.setData(AttachmentRegistry.SKILL_SAVE,
                        KeyBind.autoBind(EquipmentSkillHelper.mergedAssembly(player)));
            }
        }
    }
}
