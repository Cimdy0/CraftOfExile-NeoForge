package com.cimdy.coe.mixin.attribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedAttribute.class)
public class RangeAttributeMixin {
    @Mutable
    @Shadow
    @Final
    private double maxValue;

    @Mutable
    @Shadow
    @Final
    private double minValue;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(String descriptionId, double defaultValue, double min, double max, CallbackInfo ci) {
        if (descriptionId.equals("attribute.name.max_health")
                || descriptionId.equals("attribute.name.armor")) {
            this.maxValue = 9999999;
        }
        if (descriptionId.equals("attribute.name.armor")) {
            this.minValue = -9999999;
        }
    }
}
