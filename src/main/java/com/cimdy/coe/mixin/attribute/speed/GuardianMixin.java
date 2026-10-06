package com.cimdy.coe.mixin.attribute.speed;

import com.cimdy.coe.registry.AttributeRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Guardian.class)
public abstract class GuardianMixin extends Monster {
    protected GuardianMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
    }

    @ModifyReturnValue(method = "getAttackDuration", at = @At("RETURN"))
    private int getAttackDuration(int original) {
        return (int) (original / this.getAttributeValue(AttributeRegistry.CAST_SPEED));
    }
}
