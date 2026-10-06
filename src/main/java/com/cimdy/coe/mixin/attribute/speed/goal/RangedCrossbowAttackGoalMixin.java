package com.cimdy.coe.mixin.attribute.speed.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedCrossbowAttackGoal.class)
public abstract class RangedCrossbowAttackGoalMixin<T extends Mob & CrossbowAttackMob> extends Goal {
    @Shadow
    private int attackDelay;

    @Shadow
    @Final
    private T mob;

    // 装填后射出的间隔
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/CrossbowAttackMob;setChargingCrossbow(Z)V"))
    private void tick(CallbackInfo ci) {
        this.attackDelay = (int) (this.attackDelay / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }
}
