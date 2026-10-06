package com.cimdy.coe.mixin.effect;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.additions.HealOrHarmMobEffect")
public class HealOrHarmMobEffectMixin {
    //将 效果改为 最大生命值的百分比
//    @Inject(method = "applyEffectTick", at = @At("HEAD"))
//    protected void applyEffectTick(ServerLevel p_376742_, LivingEntity livingData, int amp,
//                                   CallbackInfoReturnable<Boolean> cir,
//                                   @Share("livingData") LocalRef<LivingEntity> livingRef,
//                                   @Share("amp") LocalIntRef ampRef) {
//        livingRef.set(livingData);
//        ampRef.set(amp);
//    }
//
//    @ModifyArg(method = "applyEffectTick",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
//    protected float heal(float healAmount,
//                         @Share("livingData") LocalRef<LivingEntity> livingRef,
//                         @Share("amp") LocalIntRef ampRef) {
//        return (float) Math.max((int) (livingRef.get().getMaxHealth() * (healAmount / 20.0F)) << ampRef.get(), 0);
//    }
//
//    @ModifyArg(method = "applyEffectTick",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/LivingEntity;" +
//                            "hurtServer(Lnet/minecraft/server/level/ServerLevel;" +
//                            "Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 2)
//    protected float hurt(float hurtAmount,
//                         @Share("livingData") LocalRef<LivingEntity> livingRef,
//                         @Share("amp") LocalIntRef ampRef) {
//        return (float) Math.max((int) (livingRef.get().getMaxHealth() * (hurtAmount / 20.0F)) << ampRef.get(), 0);
//    }
}
