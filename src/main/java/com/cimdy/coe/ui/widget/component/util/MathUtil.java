package com.cimdy.coe.ui.widget.component.util;

/**
 * 数值工具：抽取自各组件里反复出现的「本地 clamp」实现，作为共享原语。
 *
 * <p>语义与原本地实现保持一致：{@code v} 为 NaN 时返回下界 {@code lo}。
 */
public final class MathUtil {

    private MathUtil() {}

    /** 将 {@code v} 钳制到 {@code [lo, hi]}；{@code v} 为 NaN 时返回 {@code lo}。 */
    public static double clamp(double v, double lo, double hi) {
        return Double.isNaN(v) ? lo : Math.max(lo, Math.min(hi, v));
    }
}
