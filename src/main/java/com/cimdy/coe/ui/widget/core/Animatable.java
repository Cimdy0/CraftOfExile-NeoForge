package com.cimdy.coe.ui.widget.core;

import com.cimdy.coe.ui.widget.component.panel.SidePanel;

/**
 * 缓动数值持有器：把「跳跃式」的目标值平滑成「渐变式」的当前值。
 *
 * <p>折叠面板的高度、Toast 的滑入位移、开关的滑块位置，本质都是「一个值朝目标靠拢」的动画。
 * 以前这类动画要么写死在组件里（{@code SidePanel} 的 {@code easeOutCubic}），要么干脆没有。
 * 这里抽成独立的持有器，由组件的 {@code tick()} 驱动，新组件直接复用。
 *
 * <p>逼近采用指数缓动（每帧向 target 移动剩余距离的 30%），手感上等价于 easeOut，
 * 且天然对「半途反向」连续目标友好，不会突跳。
 */
public class Animatable {

    private double value;
    private double target;
    private final double rate;

    public Animatable() { this(0); }

    public Animatable(double initial) {
        this(initial, 0.3);
    }

    public Animatable(double initial, double rate) {
        this.value = initial;
        this.target = initial;
        this.rate = Math.max(0.05, Math.min(1.0, rate));
    }

    /** 设定目标值。下一次 {@link #tick()} 起逐渐逼近。 */
    public Animatable target(double t) { this.target = t; return this; }

    public double target() { return target; }

    public double value() { return value; }

    /** 立即跳到目标，不做动画。 */
    public Animatable snap() { this.value = this.target; return this; }

    /** 每帧推进一次。 */
    public void tick() {
        value += (target - value) * rate;
        if (Math.abs(target - value) < 0.01) value = target;
    }
}
