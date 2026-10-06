package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(EnchantmentHelper.class)
public class ProjectileInEnchant {
    @Inject(method = "processProjectileCount", at = @At(value = "RETURN"), cancellable = true)
    private static void modifyProjectileCount(
            ServerLevel level, ItemStack tool, Entity entity, int projectileCount, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof LivingEntity shooter) {
            // 归一：LAUNCH_ADDITIONAL_PROJECTILE 单属性 + 武器标签（弓→{bow}，弩→{crossbow}）
            Set<String> tags = new HashSet<>(Set.of(GateTag.PROJECTILE, GateTag.ARROW));
            if (tool.is(ItemTags.BOW_ENCHANTABLE)) tags.add(GateTag.BOW);
            if (tool.is(ItemTags.CROSSBOW_ENCHANTABLE)) tags.add(GateTag.CROSSBOW);
            projectileCount += (int) DamageHelper.readInScopePublic(shooter, tags, AttributeRegistry.LAUNCH_ADDITIONAL_PROJECTILE);
            // 「Projectile Attacks have a X% chance to fire two additional Projectiles while moving」：
            // 移动判据与 {@code LivingStateValue#moving} 同源（水平移动向量非零），避免两套口径漂移；
            // 几率为零基属性，仅 &gt;0 时才掷骰——未点该天赋时零开销且不改变既有行为。
            double movingChance = shooter.getAttributeValue(AttributeRegistry.MOVING_ADDITIONAL_PROJECTILE_CHANCE);
            if (movingChance > 0
                    && shooter.getRandom().nextDouble() < movingChance) {
                projectileCount += 2;
            }
            // 「超越几率」额外发射投射物：floor(value) 保证额外数，小数部分掷骰再 +1（>100% 即保证 +1 后掷小数）。
            double surpass = shooter.getAttributeValue(AttributeRegistry.SURPASS_ADDITIONAL_PROJECTILE_CHANCE);
            if (surpass > 0) {
                int guaranteed = (int) surpass;
                projectileCount += guaranteed;
                double frac = surpass - guaranteed;
                if (frac > 0 && shooter.getRandom().nextDouble() < frac) projectileCount += 1;
            }
        }
        cir.setReturnValue(projectileCount);
    }

    @Inject(method = "processProjectileSpread", at = @At(value = "RETURN"), cancellable = true)
    private static void modifyProjectileCount(
            ServerLevel level, ItemStack tool, Entity entity, float projectileSpread, CallbackInfoReturnable<Float> cir) {
        int i = EnchantmentHelper.processProjectileCount(level, tool, entity, 1);
        cir.setReturnValue(1.5F * i);
    }

    @Inject(method = "modifyCrossbowChargingTime", at = @At(value = "RETURN"), cancellable = true)
    private static void modifyCrossbowChargingTime(
            ItemStack stack, LivingEntity living, float crossbowChargingTime, CallbackInfoReturnable<Float> cir) {
        if (stack.has(DataComponentRegistry.MODIFIABLE_STATE)) {
            // 「装填即时完成」几率（零基百分比属性）：命中则装填耗时归零；未命中走常规装填速度倒数。
            double immediate = living.getAttributeValue(AttributeRegistry.CROSSBOW_IMMEDIATE_RELOAD_CHANCE);
            if (immediate > 0 && living.getRandom().nextDouble() < Math.min(1.0, immediate)) {
                cir.setReturnValue(0.0F);
                return;
            }
            cir.setReturnValue(1.0F / (float) living.getAttributeValue(AttributeRegistry.CROSS_BOW_RELOAD_TIME));
        }
    }
}
