package com.cimdy.coe.mixin.attribute;

import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class DamageHandleEvent extends Entity {
    public DamageHandleEvent(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapWithCondition(method = "handleDamageEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/WalkAnimationState;setSpeed(F)V"))
    private boolean setSpeed(
            WalkAnimationState instance, float speed,
            @Local(argsOnly = true) DamageSource damageSource) {
        return !damageSource.is(DamageTypeTagRegistry.IS_AILMENT);
    }

    @WrapWithCondition(method = "handleDamageEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean playSound(
            LivingEntity instance, SoundEvent soundEvent, float v, float v2,
            @Local(argsOnly = true) DamageSource damageSource) {
        return !damageSource.is(DamageTypeTagRegistry.IS_AILMENT);
    }

    @WrapOperation(method = "handleDamageEvent",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurtDuration:I",
                    ordinal = 0))
    private void hurtTime(
            LivingEntity living, int value, Operation<Void> original,
            @Local(argsOnly = true) DamageSource damageSource) {
        if (!damageSource.is(DamageTypeTagRegistry.IS_AILMENT)) {
            original.call(living, value);
        }
    }
}
