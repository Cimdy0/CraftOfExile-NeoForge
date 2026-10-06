package com.cimdy.coe.mixin.attribute.projectile;

import com.cimdy.coe.data.codec.support.addition.AdditionData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {
    @Shadow
    private boolean leftOwner;

    public ProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/EntityReference;)V", at = @At("TAIL"))
    private void setOwnerData(EntityReference<Entity> entityReference, CallbackInfo ci) {
        Entity owner = entityReference.getEntity(this.level(), Entity.class);
        if (owner instanceof LivingEntity living) {
            AdditionData.applyOwnerAttribute(this, living);
        }
    }
}
