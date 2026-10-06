package com.cimdy.coe.mixin.attribute.speed.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OcelotAttackGoal.class)
public class OcelotAttackGoalMixin {
    @Shadow
    private int attackTime;

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;doHurtTarget(Lnet/minecraft/server/level/ServerLevel;" +
                            "Lnet/minecraft/world/entity/Entity;)Z"))
    private void tick(CallbackInfo ci) {
        this.attackTime = (int) (this.attackTime / this.mob.getAttributeValue(Attributes.ATTACK_SPEED));
    }
}
