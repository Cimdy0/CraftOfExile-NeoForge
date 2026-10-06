package com.cimdy.coe.mixin;

import com.cimdy.coe.event.combat.TargetEvent;
import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 阵营战斗核心：在基类 {@link LivingEntity} 覆写「可否攻击」判据，把 {@link Camp} 映射进原生阵营关系。
 *
 * <p>原版两套 AI 系统（Goal 系统的 {@code TargetGoal.canContinueToUse} → {@code mob.canAttack}，
 * 与 Brain 系统的攻击 {@code Behavior} → {@code mob.canAttack}）共用 {@code canAttack} 这一唯一攻击门控。
 * 因此单点注入即可同时约束两套 AI、覆盖所有非模组实体，且<b>无需移除任何原版攻击 goal / Brain behavior</b>，
 * 替代原先 {@code TargetEvent} 中 {@code removeAllGoals(TargetGoal)} 与 {@code WorkerAIInjector} 中
 * {@code removeAllBehaviors()} 的粗暴「省事移除原行为」做法。
 *
 * <p>约定：双方均已分配阵营（非 {@link Camp#NONE}）且同阵营 ⇒ 互不攻击（同盟）；
 * 否则回落原版（基于 scoreboard team）的判据，保证未接入本阵营系统的实体行为完全不变。
 */
@Mixin(LivingEntity.class)
public abstract class CampAllianceMixin extends Entity {
    public CampAllianceMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void coe_campCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        // mixin 作用于 LivingEntity，但本类静态类型为 Entity，需经 Object 下转型（与 LivingAttributeValue 同款范式）。
        LivingEntity self = (LivingEntity) (Object) this;
        Camp selfCamp = self.getData(AttachmentRegistry.CAMP);
        Camp targetCamp = target.getData(AttachmentRegistry.CAMP);
        if (selfCamp != Camp.NONE && targetCamp != Camp.NONE && selfCamp == targetCamp) {
            cir.setReturnValue(false); // 同阵营互不攻击
        }
    }
}
