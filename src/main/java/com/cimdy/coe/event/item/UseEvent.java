package com.cimdy.coe.event.item;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.helper.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class UseEvent {
    @SubscribeEvent
    public static void AttackEntityEvent(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.getAttackStrengthScale(1.0F) != 1.0
                || player.getWeaponItem().is(Tags.Items.RANGED_WEAPON_TOOLS)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void ArrowLooseEvent(ArrowLooseEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getBow();
        EquipmentState state =ModifiableStateOps.equipment(stack);
        boolean canceled = EntityHelper.isJustReleasedSkillKey(player);

        if ((event.getCharge() < 20 / player.getAttributeValue(Attributes.ATTACK_SPEED) && state.isBow())
                || (player.getAttackStrengthScale(1.0F) != 1.0 && state.isCrossBow())) {
            canceled = true;
        } else if (state.isCrossBow()) {
            player.resetAttackStrengthTicker();
        }

        if (canceled) {
            event.setCanceled(true);
            player.resetAttackStrengthTicker();
        }
    }

    @SubscribeEvent
    public static void LivingUseItemTickEvent(LivingEntityUseItemEvent.Tick event) {
        LivingEntity living = event.getEntity();
        ItemStack stack = event.getItem();
        EquipmentState state =ModifiableStateOps.equipment(stack);
        if (EntityHelper.isUseItemSkill(living)) {
            int duration = EntityHelper.getUseSkillUseTime(living);
            if (duration > 0) {
                event.setDuration(72000 - duration);
            }
        } else if (state.isBow()) {
            if (event.getDuration() > 71999 && event.getEntity() instanceof Player player) {
                player.resetAttackStrengthTicker();
            }
            event.setDuration((int) (event.getDuration() + 1 -
                    event.getEntity().getAttributeValue(Attributes.ATTACK_SPEED)));
        }
    }

    @SubscribeEvent
    public static void LivingUseItemStopEvent(LivingEntityUseItemEvent.Stop event) {
        LivingEntity living = event.getEntity();
        ItemStack stack = event.getItem();
        boolean canceled = EntityHelper.isJustReleasedSkillKey(living);

        if (canceled) {
            event.setCanceled(true);
        }
    }

}
