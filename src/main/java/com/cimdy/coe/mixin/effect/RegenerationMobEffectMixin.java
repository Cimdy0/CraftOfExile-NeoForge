package com.cimdy.coe.mixin.effect;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.additions.RegenerationMobEffect")
public class RegenerationMobEffectMixin {
    //将 效果改为 最大生命值的百分比
//    @Inject(method = "applyEffectTick", at = @At("HEAD"))
//    protected void applyEffectTick(ServerLevel p_376742_, LivingEntity livingData, int amp,
//                                   CallbackInfoReturnable<Boolean> cir,
//                                   @Share("livingData") LocalRef<LivingEntity> livingRef) {
//        livingRef.set(livingData);
//    }
//
//    @ModifyArg(method = "applyEffectTick",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
//    protected float applyUpdateEffect(float healAmount,
//                                      @Share("livingData") LocalRef<LivingEntity> livingRef) {
//        return livingRef.get().getMaxHealth() * (healAmount / 20.0F);
//    }
}
