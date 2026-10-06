package com.cimdy.coe.mixin.client.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Hud.class)
public abstract class GuiMixin {
    @Shadow
    public abstract Font getFont();

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "extractHealthLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;getCameraPlayer()Lnet/minecraft/world/entity/player/Player;"))
    private Player get(Player original) {
        return null;
    }

    @ModifyExpressionValue(method = "extractArmorLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;getCameraPlayer()Lnet/minecraft/world/entity/player/Player;"))
    private Player renderHealthLevel(Player original) {
        return null;
    }

    @ModifyExpressionValue(method = "extractVehicleHealth",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int renderHealthLevel(int original, @Local LivingEntity vehicle) {
        return (int) (vehicle.getHealth() / vehicle.getMaxHealth() * 20);
    }

    @ModifyArgs(method = "extractEffects",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void renderEffectsLevel(
            Args args, @Local MobEffectInstance instance,
            @Local(argsOnly = true) GuiGraphicsExtractor guiGraphics) {
        int scaleX = args.get(4);
        int scaleY = args.get(5);
        int k = args.get(2);
        int l = args.get(3);
        Font font = this.getFont();

        Component component = this.craftOfExile$getEffectLevel(instance);
        guiGraphics.text(font, component, k - font.width(component), l - 10, 16777215);
        if (this.minecraft.level != null) {
            Component component1 = MobEffectUtil.formatDuration(instance, 1.0F,
                    this.minecraft.level.tickRateManager().tickrate());
            guiGraphics.text(font, component1, k - font.width(component1), l, 8355711);
        }
    }

    @Unique
    private Component craftOfExile$getEffectLevel(MobEffectInstance instance) {
        MutableComponent var10000 = CommonComponents.SPACE.copy();
        int var10001 = instance.getAmplifier();
        if (instance.getAmplifier() >= 1 && instance.getAmplifier() <= 9) {
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        } else {
            int level = instance.getAmplifier() + 1;
            var10000.append(CommonComponents.SPACE)
                    .append(Component.translatable(String.valueOf(level)));
        }
        return var10000;
    }
}
