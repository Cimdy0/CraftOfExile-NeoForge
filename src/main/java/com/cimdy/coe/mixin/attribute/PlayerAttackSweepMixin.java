package com.cimdy.coe.mixin.attribute;

import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * 剑横扫之刃造成型事件闭环（Mixin 精确点位）。
 * <p>
 * 用户要求：横扫/举盾若无对应触发事件，用 Mixin 找原版点位，而非行为模糊匹配。
 * NeoForge 1.21 已移除原版 {@code EntitySweepAttackEvent}，故直接 Mixin 原版 {@code Player} 的横扫方法。
 * <p>
 * 精确点位：注入原版 {@code Player#doSweepAttack(Entity, float, DamageSource, float, AABB)} 的 {@code TAIL}。
 * 该方法是横扫伤害结算的真正实现（含 {@code PLAYER_ATTACK_SWEEP} 音效与范围伤害），
 * 且仅在 {@code Player#attack} 内 {@code if (sweepAttack)} 分支被调用（已解包 patched-26.2.0.32-beta-sources 验证：
 * attack 1052 行 `this.doSweepAttack(entity, baseDamage, damageSource, attackStrengthScale, sweepHitBox)`），
 * 是横扫动作的专属、唯一入口，从而精确命中原版横扫动作，而非「满蓄力 + 主手是剑」之类的跨字段启发式匹配。
 * 本模组在满蓄力近战攻击时不取消 {@code AttackEntityEvent}，故原版横扫分支仍真实执行。
 * 注：另有 4 参 {@code @Deprecated} 转发版 {@code doSweepAttack(Entity, float, DamageSource, float)}，此处注入 5 参真正实现版。
 * <p>
 * 触发时由攻击者（player）作为 owner、主攻击目标作为 target，写入 {@link EventName#CAUSED_SWEEP}
 * 计时并 {@link StatEngine#fire} 到 fire 通道，驱动「横扫命中时触发 X」类 on-cause 效果。
 */
@Mixin(Player.class)
public abstract class PlayerAttackSweepMixin {
    @Inject(method = "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V",
            at = @At(value = "TAIL"))
    private void doSweepAttack(Entity entity, float baseDamage, DamageSource damageSource, float attackStrengthScale, AABB sweepHitBox, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Event.record(self, EventName.CAUSED_SWEEP);
        StatEngine.fire(self,
                entity instanceof LivingEntity living ? living : null,
                StatContext.of(Set.of(EventName.CAUSED_SWEEP), null,
                        EventName.CAUSED_SWEEP, null), null);
    }
}
