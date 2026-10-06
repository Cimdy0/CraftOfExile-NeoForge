package com.cimdy.coe.ui.widget.component.bar;

/**
 * 不可变范围：用于关联传播与比较。
 *
 * <p>作为「能力」之间传递量程的统一数据类型——{@code ScaleCapable} 暴露它、
 * {@code ValueEditable} 也暴露它，二者据此对齐。记录类型保证「同值即相等」，
 * 避免绑定里无谓的级联刷新。从原 {@code Axis.Range} 提升为包级共享，去掉 {@code Axis.} 前缀。
 *
 * @param min 量程下界
 * @param max 量程上界
 */
public record Range(double min, double max) {

    /** 夹到 [min,max]；NaN 视为下界。 */
    public double clamp(double v) {
        double lo = Math.min(min, max), hi = Math.max(min, max);
        if (Double.isNaN(v)) return lo;
        return Math.max(lo, Math.min(hi, v));
    }

    /** step>0 时吸附到网格再夹；step<=0 直接夹到 [min,max]。 */
    public double snap(double v, double step) {
        double lo = Math.min(min, max), hi = Math.max(min, max);
        double clamped = Math.max(lo, Math.min(hi, v));
        if (step > 0) {
            clamped = lo + Math.round((clamped - lo) / step) * step;
            clamped = Math.max(lo, Math.min(hi, clamped));
        }
        return clamped;
    }
}
