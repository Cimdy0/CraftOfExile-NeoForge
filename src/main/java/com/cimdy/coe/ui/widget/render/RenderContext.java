package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.core.VisualState;

/**
 * 一帧的渲染上下文。
 *
 * <p>把「鼠标位置 + 帧插值 + 可视状态」打成一个值对象往下传，避免旧库那种
 * 每个渲染方法都拖着 {@code mouseX, mouseY, partialTick} 三个裸参数的签名污染，
 * 后续要加字段（如 UI 缩放、动画时钟）也不必再改所有实现。
 *
 * <p>鼠标坐标之所以还要保留，是因为少数渲染确实需要它做<b>定位</b>而非<b>判态</b>：
 * 物品 tooltip 要跟着鼠标走、实体预览要朝向鼠标。判态一律走 {@link #state()}。
 *
 * <p>alpha 通道（0..1，默认 1）：动画原语（Fade）把透明度沿树下传，
 * {@code ColorRenderer} / {@code TextRenderer} 会把绘制颜色的 alpha 乘以此值；
 * alpha=1 时路径与无动画完全一致（零回归）。
 */
public record RenderContext(int mouseX, int mouseY, float partialTick, VisualState state, float alpha) {

    public static RenderContext of(int mouseX, int mouseY, float partialTick) {
        return new RenderContext(mouseX, mouseY, partialTick, VisualState.NONE, 1F);
    }

    /** 鼠标「不在任何地方」的哨兵坐标，用于屏蔽被裁剪掉的区域的 hover。 */
    public static final int MOUSE_NOWHERE = Integer.MIN_VALUE / 2;

    /** 换一个可视状态，其余不变。 */
    public RenderContext with(VisualState s) {
        return s == state ? this : new RenderContext(mouseX, mouseY, partialTick, s, alpha);
    }

    /** 换一个鼠标位置，其余不变。 */
    public RenderContext withMouse(int mx, int my) {
        return (mx == mouseX && my == mouseY) ? this : new RenderContext(mx, my, partialTick, state, alpha);
    }

    /** 换一个透明度（沿树下传）。 */
    public RenderContext withAlpha(float a) {
        return a == alpha ? this : new RenderContext(mouseX, mouseY, partialTick, state, a);
    }

    /**
     * 把鼠标挪到「无处」。滚动容器在鼠标落到裁剪区外时用它下传，
     * 这样被裁掉的子节点不会算出虚假的 hover（旧库的经典毛病：列表滚出可视区的项仍然高亮）。
     */
    public RenderContext withoutMouse() {
        return withMouse(MOUSE_NOWHERE, MOUSE_NOWHERE);
    }

    public boolean hovered()  { return state.hovered(); }
    public boolean active()   { return state.active(); }
    public boolean disabled() { return state.disabled(); }
    public boolean focused()  { return state.focused(); }
    public boolean pressed()  { return state.pressed(); }
}
