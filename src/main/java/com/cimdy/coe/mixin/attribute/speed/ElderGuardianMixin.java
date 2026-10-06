package com.cimdy.coe.mixin.attribute.speed;

import com.cimdy.coe.registry.AttributeRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElderGuardian.class)
public abstract class ElderGuardianMixin extends Guardian {
    public ElderGuardianMixin(EntityType<? extends Guardian> p_32810_, Level p_32811_) {
        super(p_32810_, p_32811_);
    }

    @ModifyReturnValue(method = "getAttackDuration", at = @At("RETURN"))
    private int getAttackDuration(int original) {
        return (int) (original / this.getAttributeValue(AttributeRegistry.CAST_SPEED));
    }
}
