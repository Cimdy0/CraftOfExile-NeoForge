package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(DragonFireball.class)
public abstract class DragonFireBallEffect extends Projectile {

    protected DragonFireBallEffect(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

//    @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/AreaEffectCloud;setParticle(Lnet/minecraft/core/particles/ParticleOptions;)V"))
//    private ParticleOptions setParticle(ParticleOptions particleOption) {
//        return this.getData(AttachmentRegistry.AREA_EFFECT_CLOUD_DATA).particle();
//    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/AreaEffectCloud;setDuration(I)V"))
    private int setDuration(int i) {
        return (int) this.getData(AttachmentRegistry.AREA_EFFECT_CLOUD_DATA).duration();
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadius(F)V"))
    private float setRadius(float radius) {
        return (float) this.getData(AttachmentRegistry.AREA_EFFECT_CLOUD_DATA).radius();
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadiusPerTick(F)V"))
    private float setRadiusPerTick(float radiusPerTick) {
        return (float) this.getData(AttachmentRegistry.AREA_EFFECT_CLOUD_DATA).radiusPerTick();
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/AreaEffectCloud;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    private MobEffectInstance setRadius(MobEffectInstance effectInstance, @Local AreaEffectCloud areaEffectCloud) {
        List<MobEffectInstance> effects = this.getData(AttachmentRegistry.AREA_EFFECT_CLOUD_DATA).effects();
        if (effects.size() > 1) {
            for (int i = 1; i < effects.size(); i++) {
                areaEffectCloud.addEffect(effects.get(i));
            }
        }
        return effects.getFirst();
    }
}
