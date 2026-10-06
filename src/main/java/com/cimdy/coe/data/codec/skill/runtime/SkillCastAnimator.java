package com.cimdy.coe.data.codec.skill.runtime;

import com.cimdy.coe.CraftOfExile;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

/**
 * 技能施放动画控制器（Player Animation Library 层）。
 * <ul>
 *   <li><b>动画路径约定</b>：技能专属动画 = {@code assets/<modid>/player_animations/skill/<skill.path>.json}，
 *       注册键 = {@code <modid>:skill/<skill.path>}（PAL 以文件内动画键名注册）；缺失时回退默认施法动画
 *       {@code skill/cast_default}；再缺失兜底 {@code wave_left_hand}。</li>
 *   <li><b>变速</b>：PAL 播放位置 = {@code tick + startAnimFrom + partialTick}，内部每游戏 tick 前进 1 帧；
 *       本类在 {@link #tick} 中对 {@code startAnimFrom} 做 float 补偿 {@code (speedFactor-1)}，
 *       使等效进度每游戏 tick 前进 {@code speedFactor} 帧 —— 动画在 {@code castTimeTicks} 个游戏 tick 内播完，
 *       播放速度随施放速度（cast time / CAST_SPEED 属性）自适应。</li>
 * </ul>
 */
public class SkillCastAnimator extends PlayerAnimationController {

    /** 本层在玩家动画栈中的注册键（{@code PlayerAnimationAccess.getPlayerAnimationLayer} 用）。 */
    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_cast");
    /** 层优先级（高于底层 idle 动画）。 */
    public static final int LAYER_PRIORITY = 1000;
    /** 技能专属动画目录前缀（注册键 path 前缀）。 */
    private static final String SKILL_ANIM_PREFIX = "skill/";
    /** 默认施法动画（技能无专属动画时回退）。 */
    public static final Identifier DEFAULT_CAST = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill/cast_default");
    /** 最坏兜底：既有挥手动画（默认动画也缺失时）。 */
    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "wave_left_hand");

    /** 变速因子（播放帧/游戏 tick）；1 = 原速。 */
    private float speedFactor = 1f;
    /**
     * 时间轴锚点：施放起始世界时刻与总时长（来自服务端 {@code CastAnimationPacket}）。
     * 肢体动画不再自顾自按 {@code castTimeTicks} 播放，而是等到<b>世界时钟到达施放起刻</b>才触发，
     * 使其窗口严格对齐服务端施放时间轴 —— 与本类同源的 {@link SkillTimeline} 是整套技能
     * 「动画 / 效果生成 / 视觉 / 音效」共享的唯一时钟。
     */
    private long startWorldTime = 0L;
    private int castTicks = 1;
    private boolean started = false;
    private Identifier pendingAnim = null;

    public SkillCastAnimator(Avatar avatar) {
        // 仅接受 triggerAnimation 触发的动画；常规（非触发）动画一律 STOP，避免与底层动画抢占。
        super(avatar, (controller, state, animSetter) -> PlayState.STOP);
    }

    @Override
    public void tick(AnimationData data) {
        if (!started) {
            // 世界时钟尚未到达服务端施放起刻：保持就绪、不推进动画（延迟触发，锚定共享时间轴）。
            long now = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.getGameTime() : 0L;
            if (now >= startWorldTime) {
                started = true;
                if (pendingAnim != null) triggerAnimation(pendingAnim, 0f);
            } else {
                super.tick(data);
                return;
            }
        }
        // 变速补偿（见类注释）；float 精确，无逐帧取整误差。
        if (speedFactor != 1f) {
            startAnimFrom += (speedFactor - 1f);
        }
        super.tick(data);
    }

    /**
     * 播放施法动画：解析动画（指定动画 → {@code skill/<path>} → 默认 → 兜底），
     * 并按施放时长归一播放速度。<b>不在此时立即触发</b>——
     * 改由 {@link #tick} 在世界时钟到达 {@code startTime} 时才触发，
     * 使肢体动画窗口严格对齐服务端施放时间轴。
     *
     * @param requested    技能 JSON 显式声明的 {@code animation}（可为 null）
     * @param skillId      本次施放的技能 id（可为 null，如原版攻击无技能上下文）
     * @param castTimeTicks 施放时长（game tick，已含 CAST_SPEED 属性与辅助宝石倍率；{@code <=0} 视为立即结束）
     * @param startTime    施放起始世界时刻（{@code level.getGameTime()}，服务端权威；{@code <=0} 表示即时）
     */
    public void playCast(Identifier requested, Identifier skillId, int castTimeTicks, long startTime) {
        Identifier anim = resolve(requested, skillId);
        Animation data = PlayerAnimResources.getAnimationOptional(anim).orElse(null);
        if (data == null) return;   // 兜底动画也不存在（资源异常），静默不播
        this.pendingAnim = anim;
        this.castTicks = castTimeTicks > 0 ? castTimeTicks : 1;
        this.startWorldTime = startTime;
        this.speedFactor = castTimeTicks > 0
                ? Math.max(0.25f, Math.min(4f, data.length() / (float) castTicks))
                : 1f;
        this.startAnimFrom = 0f;
        this.started = false;
    }

    /** 施放中断：立即停止当前施法动画并复位速度与时间轴锚点。 */
    public void stopCast() {
        stop();
        speedFactor = 1f;
        started = false;
        pendingAnim = null;
    }

    /** 动画 id 解析（fallback 链）。 */
    private static Identifier resolve(Identifier requested, Identifier skillId) {
        if (requested != null) return requested;
        if (skillId != null) {
            Identifier skillAnim = Identifier.fromNamespaceAndPath(
                    skillId.getNamespace(), SKILL_ANIM_PREFIX + skillId.getPath());
            if (PlayerAnimResources.hasAnimation(skillAnim)) return skillAnim;
        }
        return PlayerAnimResources.hasAnimation(DEFAULT_CAST) ? DEFAULT_CAST : FALLBACK;
    }
}
