package com.cimdy.coe.mixin.effect;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.PoisonMobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoisonMobEffect.class)
public class PoisonMobEffectMixin {
    //将 效果改为 最大生命值的百分比
    @Inject(method = "applyEffectTick", at = @At("HEAD"))
    protected void applyEffectTick(ServerLevel p_376742_, LivingEntity living, int amp,
                                   CallbackInfoReturnable<Boolean> cir,
                                   @Share("data") LocalRef<LivingEntity> livingRef) {
        livingRef.set(living);
    }

    @ModifyArg(method = "applyEffectTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;" +
                            "hurtServer(Lnet/minecraft/server/level/ServerLevel;" +
                            "Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 2)
    protected float hurt(float hurtAmount,
                         @Share("data") LocalRef<LivingEntity> livingRef) {
        LivingEntity living = livingRef.get();
        int amount = (int) (living.getMaxHealth() * (hurtAmount / 20.0F));
        amount = (int) living.getHealth() - amount > 1 ? amount : Math.min(1, amount);
        return (float) amount;
    }
}
