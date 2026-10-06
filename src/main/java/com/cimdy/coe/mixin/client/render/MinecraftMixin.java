package com.cimdy.coe.mixin.client.render;

import com.cimdy.coe.helper.EntityHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyExpressionValue(method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    private boolean use(boolean original) {
        if (Minecraft.getInstance().player != null
                && EntityHelper.isUseItemSkill(Minecraft.getInstance().player)) {
            return !original;
        }
        return original;
    }
}
