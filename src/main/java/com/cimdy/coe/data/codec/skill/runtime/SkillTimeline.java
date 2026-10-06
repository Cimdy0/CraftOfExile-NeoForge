package com.cimdy.coe.data.codec.skill.runtime;

import net.minecraft.world.level.Level;

/**
 * 贯穿始终的技能时间轴 —— 服务端权威、客户端同源。
 *
 * <p>锚定世界共享游戏时间（{@link Level#getGameTime()}，服务端 / 客户端严格同步），
 * 以「施放起始刻」为原点、以「经施法 / 攻击速度缩放后的施法时长」为全长，给出
 * {@code 0..1} 的施放进度。它是让 <b>动画、效果生成时机、效果视觉、音效</b> 全部对齐到
 * 同一条时钟的唯一真值源。
 *
 * <p><b>为什么用世界游戏时间而非各自计数</b>：原系统里服务端用 {@code pressTime ≥ castTime} 门控效果、
 * 客户端用 {@code castTimeTicks} 自顾自播肢体动画，两侧没有共享基准，天然不对齐。
 * 世界游戏时间在客户端 / 服务端是同一序列（Minecraft 同步），任一端用
 * {@code (level.getGameTime() - startTime) / totalCastTicks} 都能算出同一进度，
 * 于是「动画窗口」「效果门控」「波前位置」「命中音效」全部由这一个数驱动。
 *
 * <p><b>施放速度（攻击 / 法术）已纳入</b>：{@code totalCastTicks} 来自
 * {@code Skill.getCastTime}，其底层 {@code SkillCast.getCastTime} 已除以
 * {@code ATTACK_SPEED} / {@code CAST_SPEED} 及辅助宝石 {@code speedMultiplier}，
 * 故时间轴天然随施放速度伸缩，动画与效果同步伸缩。
 */
public final class SkillTimeline {
    private final long startTime;        // 施放起始世界游戏时刻
    private final int totalCastTicks;   // 施法总时长（tick，已含施法/攻击速度倍率）

    public SkillTimeline(long startTime, int totalCastTicks) {
        this.startTime = startTime;
        this.totalCastTicks = Math.max(1, totalCastTicks);
    }

    /** 当前施放进度 0..1（基于给定世界时刻）。 */
    public float progress(long currentWorldTime) {
        long elapsed = currentWorldTime - startTime;
        if (elapsed <= 0) return 0f;
        if (elapsed >= totalCastTicks) return 1f;
        return (float) elapsed / totalCastTicks;
    }

    public boolean isComplete(long currentWorldTime) {
        return currentWorldTime - startTime >= totalCastTicks;
    }

    /** 便捷：取当前世界时刻的进度（客户端 / 服务端各自传入自己的 level）。 */
    public float progress(Level level) {
        return progress(level.getGameTime());
    }

    public long startTime() { return startTime; }
    public int totalCastTicks() { return totalCastTicks; }
}
