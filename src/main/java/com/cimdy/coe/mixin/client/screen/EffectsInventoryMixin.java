package com.cimdy.coe.mixin.client.screen;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EffectsInInventory.class)
public class EffectsInventoryMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyReturnValue(method = "getEffectName", at = @At("RETURN"))
    private Component getLevel(Component original, @Local(argsOnly = true) MobEffectInstance instance) {
        MutableComponent var10001 = original.copy();
        if (instance.getAmplifier() > 9) {
            int level = instance.getAmplifier() + 1;
            var10001.append(CommonComponents.SPACE)
                    .append(Component.translatable("level.coe." + level));
        }
        return var10001;
    }
}
