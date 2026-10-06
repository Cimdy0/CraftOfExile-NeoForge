package com.cimdy.coe.mixin.effect;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.additions.WitherMobEffect")
public class WitherMobEffectMixin {
//    //将 效果改为 最大生命值的百分比
//    @Inject(method = "applyEffectTick", at = @At("HEAD"))
//    protected void applyEffectTick(ServerLevel p_376742_, LivingEntity livingData, int amp,
//                                   CallbackInfoReturnable<Boolean> cir,
//                                   @Share("livingData") LocalRef<LivingEntity> livingRef) {
//        livingRef.set(livingData);
//    }
//
//    @ModifyArg(method = "applyEffectTick",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/LivingEntity;" +
//                            "hurtServer(Lnet/minecraft/server/level/ServerLevel;" +
//                            "Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 2)
//    protected float hurt(float hurtAmount,
//                         @Share("livingData") LocalRef<LivingEntity> livingRef) {
//        LivingEntity livingData = livingRef.get();
//        int amount = (int) (livingData.getMaxHealth() * (hurtAmount / 20.0F));
//        return (float) amount;
//    }
}
