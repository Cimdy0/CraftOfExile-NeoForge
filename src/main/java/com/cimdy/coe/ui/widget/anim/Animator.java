package com.cimdy.coe.ui.widget.anim;

import net.minecraft.util.Util;

/**
 * 时间驱动补间（duration + Easing 曲线 + 起止值 + 完成回调）。
 *
 * <p>框架原有的 {@code Animatable} 是指数逼近式持有器（每帧向目标移动剩余距离的固定比例），
 * 无时长概念、无曲线、无完成回调 —— 这里在其上扩展出「指定时长 + 曲线」的补间：
 * 推进方式与 {@code SidePanel} 的时间插值同源（{@code Util.getMillis()}），
 * 因此 {@code tick()}（20Hz）与渲染采样（每帧）都读同一把时钟，数值平滑且不依赖 tick 频率。
 *
 * <p>可中断：{@link #retarget(float)} 从<b>当前值</b>起重新计时逼近新目标，状态反转立即回弹不排队。
 * 每帧增量小：曲线输出天然平缓，不占主线程明显开销。
 */
public class Animator {

    private final Easing easing;
    private final int durationMs;

    private float from;
    private float to;
    private long start;
    private boolean done = true;
    private Runnable onComplete;

    /** 是否已占用全局补间预算名额（D.0 / §四・五）。归还须与占用配对。 */
    private boolean budgeted = false;

    /** 起止值相同时构造即完成。 */
    public Animator(float from, float to, int durationMs, Easing easing) {
        this.easing = easing == null ? Easing.EASE_OUT_CUBIC : easing;
        this.durationMs = Math.max(1, durationMs);
        this.from = from;
        this.to = to;
        this.start = Util.getMillis();
        this.done = from == to;
        // 全局关闭 / reduced-motion / 超预算：不起补间，直接落到静止终帧（D.0 纪律）
        if (!this.done && AnimRegistry.animationsActive()) {
            this.budgeted = AnimRegistry.acquire();
            if (!this.budgeted) this.done = true;
        }
    }

    /** 从当前值重新计时逼近 {@code newTo}（中断式反向）。 */
    public void retarget(float newTo) {
        if (newTo == to && !done) return;
        if (budgeted) { AnimRegistry.release(); budgeted = false; }
        this.from = value();
        this.to = newTo;
        this.start = Util.getMillis();
        this.done = this.from == newTo;
        if (!this.done && AnimRegistry.animationsActive()) {
            this.budgeted = AnimRegistry.acquire();
            if (!this.budgeted) this.done = true;
        }
    }

    /** 从显式起点重新计时逼近 {@code newTo}。 */
    public void retarget(float newFrom, float newTo) {
        if (budgeted) { AnimRegistry.release(); budgeted = false; }
        this.from = newFrom;
        this.to = newTo;
        this.start = Util.getMillis();
        this.done = newFrom == newTo;
        if (!this.done && AnimRegistry.animationsActive()) {
            this.budgeted = AnimRegistry.acquire();
            if (!this.budgeted) this.done = true;
        }
    }

    /**
     * 当前插值值；动画结束后返回终点。
     *
     * <p>D.0 总闸在此单点生效：{@link AnimRegistry#animationsActive()} 为假时
     * 一律瞬时返回终值（等价于「无动画 = 静止终帧」），所有既有调用方自动退化。
     */
    public float value() {
        if (!AnimRegistry.animationsActive()) {
            if (budgeted) { AnimRegistry.release(); budgeted = false; }
            done = true;
            return to;
        }
        if (done) return to;
        long elapsed = Util.getMillis() - start;
        float t = elapsed >= durationMs ? 1F : elapsed / (float) durationMs;
        if (t >= 1F) {
            done = true;
            if (budgeted) { AnimRegistry.release(); budgeted = false; }
            if (onComplete != null) {
                Runnable r = onComplete;
                onComplete = null;
                try { r.run(); } catch (RuntimeException ignored) { /* 附件级回调失败静默 */ }
            }
            return to;
        }
        return from + (to - from) * easing.apply(t);
    }

    /** 立即跳到目标值，不做动画。 */
    public void snapTo(float v) {
        if (budgeted) { AnimRegistry.release(); budgeted = false; }
        from = to = v;
        done = true;
        onComplete = null;
    }

    /** 完成回调（动画自然走完时触发一次；被 retarget 打断后再次走完仍触发）。 */
    public Animator onComplete(Runnable r) { this.onComplete = r; return this; }

    public boolean isRunning() { return !done; }

    public float from() { return from; }

    public float to() { return to; }

    /** tick 驱动入口：只负责推进完成判定与回调，数值读取走 {@link #value()}。 */
    public void tick() { value(); }
}
