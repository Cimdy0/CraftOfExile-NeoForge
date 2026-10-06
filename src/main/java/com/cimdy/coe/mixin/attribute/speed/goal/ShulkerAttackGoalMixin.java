package com.cimdy.coe.mixin.attribute.speed.goal;

import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Shulker$ShulkerAttackGoal")
public abstract class ShulkerAttackGoalMixin extends Goal {

    @Shadow
    private int attackTime;

    @Shadow
    @Final
    Shulker this$0;

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void tick(CallbackInfo ci) {
        this.attackTime = (int) (this.attackTime / this.this$0.getAttributeValue(AttributeRegistry.CAST_SPEED));
    }

    @Inject(method = "start", at = @At(value = "TAIL"))
    private void setAttackTime(CallbackInfo ci) {
        this.attackTime = (int) (this.attackTime / this.this$0.getAttributeValue(AttributeRegistry.CAST_SPEED));
    }
}