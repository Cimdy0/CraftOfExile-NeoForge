package com.cimdy.coe.mixin.attribute.speed.goal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class RangedBowAttackGoalMixin<T extends Mob & RangedAttackMob> {
    @Shadow
    private int attackIntervalMin;

    @Shadow
    @Final
    private T mob;

    // 拉弓间隔
    @Inject(method = "setMinAttackInterval", at = @At("TAIL"))
    private void setMinAttackInterval(int attackCooldown, CallbackInfo ci) {
        this.attackIntervalMin = (int) (attackCooldown / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }

    // 拉弓速度
    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getTicksUsingItem()I"))
    private int modifyTicksUsingItem(int original) {
        return (int) (original * this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }
}
