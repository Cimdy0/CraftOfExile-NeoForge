package com.cimdy.coe.mixin.attribute.speed.goal;

import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeAttackGoalMixin extends Goal {
    @Shadow
    private int attackTime;

    @Shadow
    @Final
    private Blaze blaze;

    // 近战攻击
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Blaze;" +
                            "doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"))
    private void melee(CallbackInfo ci) {
        this.attackTime = (int) (this.attackTime / this.blaze.getAttributeValue(Attributes.ATTACK_SPEED));
    }

    // 远程攻击
    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/Blaze;setCharged(Z)V",
            ordinal = 1))
    private void range(CallbackInfo ci) {
        this.attackTime = (int) (this.attackTime / this.blaze.getAttributeValue(AttributeRegistry.CAST_SPEED));
    }
}
