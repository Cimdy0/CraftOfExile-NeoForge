package com.cimdy.coe.mixin.attribute.speed.goal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RangedAttackGoal.class)
public class RangedAttackGoalMixin {
    @Shadow
    @Final
    private Mob mob;

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;floor(F)I"))
    private int tick(float value, Operation<Integer> original) {
        return (int) (original.call(value) / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }
}
