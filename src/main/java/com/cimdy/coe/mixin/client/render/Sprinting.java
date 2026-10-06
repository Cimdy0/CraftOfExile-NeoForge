package com.cimdy.coe.mixin.client.render;

import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;

import com.cimdy.coe.client.animation.DodgeRollAnimation;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.client.InputState;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Set;

/**
 * 将原版奔跑（sprint）系统经「翻滚键」（默认 X）驱动：按住翻滚键且满足移动/地面/饥饿等原版条件时，
 * 强制进入<b>原版 sprint</b>。保留全部原版行为——饥饿消耗、撞墙减速、水中停止、离地停止等。
 *
 * <p>翻滚与奔跑是两个<b>独立机制</b>：翻滚由 {@code DodgeRollHelper}（服务端权威）驱动；本 mixin 只负责把
 * 翻滚键接成原版 sprint 的额外触发源（与 Ctrl 原版 sprint 并存）。联动仅体现为：点按 = 翻滚；
 * 按住翻滚键（翻滚结束后仍按住）→ 进入原版 sprint。
 *
 * <p>禁用条件：翻滚动画进行中、被击倒地调整窗口内、{@code CANNOT_DODGE_ROLL_OR_SPRINT} ≥ 1.0
 * （如 Unwavering Stance 同时禁用翻滚与奔跑）时，不强制 sprint。
 */
@Mixin(LocalPlayer.class)
public abstract class Sprinting {
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void coe$forceSprintFromDodge(CallbackInfo ci) {
        // mixin 类编译期不继承目标类，故经 Object 桥接为 LocalPlayer 以调用其方法
        LocalPlayer player = (LocalPlayer) (Object) this;
        // 已处于 sprint：交给原版逻辑（饥饿/水中/离地自动停止），本 mixin 不干预
        if (player.isSprinting()) return;
        // 翻滚键未按住：不触发（点按仅在翻滚结束后仍按住才接 sprint）
        if (!InputState.DODGE_ROLL.isDown()) return;
        // 翻滚中 / 被击倒地调整中：禁止奔跑
        if (DodgeRollAnimation.isClientRolling(player)) return;
        if (DodgeRollAnimation.isClientDowned(player)) return;
        // Unwavering Stance 等：同时禁用翻滚与奔跑
        if (StatEngine.gateOf(player, "cannot_dodge_roll_or_sprint", Set.of())) return;
        // 复用原版 sprint 起始条件：需前向移动输入、不在水中、在地面、未使用物品、非乘骑、有饥饿值
        // NeoForge 26.2 将玩家输入重构进 ClientInput，前向脉冲改由 hasForwardImpulse()（布尔）读取，
        // 旧 @Shadow float zza 字段已不存在（datagen 因而报 No refMap / field zza not located）。
        if (!player.input.hasForwardImpulse()) return;
        if (player.isInWater() || !player.onGround() || player.isUsingItem() || player.isPassenger()) return;
        if (player.getFoodData().getFoodLevel() <= 6) return; // 原版饥饿门控（保留消耗，不去消耗）
        player.setSprinting(true);
    }
}
