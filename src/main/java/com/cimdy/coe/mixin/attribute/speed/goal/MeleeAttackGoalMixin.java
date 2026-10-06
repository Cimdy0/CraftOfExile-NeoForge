package com.cimdy.coe.mixin.attribute.speed.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin extends Goal {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @ModifyArg(method = "resetAttackCooldown",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;adjustedTickDelay(I)I"))
    private int resetAttackCooldown(int i) {
        return (int) (i / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }

    @ModifyConstant(method = "canUse", constant = @Constant(longValue = 20L))
    public long canUse(long constant) {
        return (long) (constant / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }
}
