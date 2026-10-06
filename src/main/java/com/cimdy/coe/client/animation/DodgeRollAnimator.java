package com.cimdy.coe.client.animation;

import com.cimdy.coe.CraftOfExile;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

/**
 * 翻滚动画控制器（Player Animation Library 层）。
 * 资源路径约定：{@code assets/<modid>/player_animations/dodge_roll.json}，注册键 {@code coe:dodge_roll}。
 * 由 {@link DodgeRollAnimation#onRoll} 触发，按 {@code rollTicks} 时长归一播放速度，结束自动停止。
 */
public class DodgeRollAnimator extends PlayerAnimationController {

    /** 本层在玩家动画栈中的注册键（{@code PlayerAnimationAccess.getPlayerAnimationLayer} 用）。 */
    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_roll");
    /** 层优先级（高于底层 idle 动画，低于施法动画）。 */
    public static final int LAYER_PRIORITY = 900;
    /** 翻滚动画注册键。 */
    public static final Identifier ROLL = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_roll");
    /** 被击中断后的“调整动作”动画注册键。 */
    public static final Identifier ROLL_RECOVERY = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_recovery");
    /** 空翻（翻滚 + 空格）动画注册键。 */
    public static final Identifier ROLL_FLIP = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "dodge_flip");

    /** 变速因子（播放帧/游戏 tick）；1 = 原速。 */
    private float speedFactor = 1f;

    public DodgeRollAnimator(Avatar avatar) {
        // 仅接受 triggerAnimation 触发的动画；常规（非触发）动画一律 STOP，避免与底层动画抢占。
        super(avatar, (controller, state, animSetter) -> PlayState.STOP);
    }

    @Override
    public void tick(AnimationData data) {
        // 变速补偿：使动画在 rollTicks 个游戏 tick 内播完，播放速度随翻滚时长自适应。
        if (speedFactor != 1f) {
            startAnimFrom += (speedFactor - 1f);
        }
        super.tick(data);
    }

    /** 播放翻滚动画：按翻滚时长归一播放速度。 */
    public void playRoll(int rollTicks) {
        Animation data = PlayerAnimResources.getAnimationOptional(ROLL).orElse(null);
        if (data == null) return;   // 动画资源缺失，静默不播
        speedFactor = rollTicks > 0
                ? Math.max(0.25f, Math.min(4f, data.length() / (float) rollTicks))
                : 1f;
        startAnimFrom = 0f;
        triggerAnimation(ROLL, 0f);
    }

    /** 播放被击中断后的“调整动作”动画。 */
    public void playRecovery(int ticks) {
        Animation data = PlayerAnimResources.getAnimationOptional(ROLL_RECOVERY).orElse(null);
        if (data == null) return;
        speedFactor = ticks > 0
                ? Math.max(0.25f, Math.min(4f, data.length() / (float) ticks))
                : 1f;
        startAnimFrom = 0f;
        triggerAnimation(ROLL_RECOVERY, 0f);
    }

    /** 播放空翻（翻滚 + 空格）动画；若空翻资源缺失则回退普通翻滚动画，保证有视觉反馈。 */
    public void playFlip(int ticks) {
        Animation data = PlayerAnimResources.getAnimationOptional(ROLL_FLIP).orElse(null);
        if (data == null) {
            playRoll(ticks);
            return;
        }
        speedFactor = ticks > 0
                ? Math.max(0.25f, Math.min(4f, data.length() / (float) ticks))
                : 1f;
        startAnimFrom = 0f;
        triggerAnimation(ROLL_FLIP, 0f);
    }
}
