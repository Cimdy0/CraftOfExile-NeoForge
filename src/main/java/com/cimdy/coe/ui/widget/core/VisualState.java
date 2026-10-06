package com.cimdy.coe.ui.widget.core;

/**
 * 组件的可视状态 —— 渲染器的一等公民输入。
 *
 * <p>旧库里 hover 是每个渲染器自己拿 {@code mouseX/mouseY} 反复算出来的，
 * 而 active/disabled 则靠外部「换掉整个渲染器」来表达，导致同一份画法要写好几遍。
 * 现在状态由节点统一算出、逐级下传，渲染器只管「这个状态下我怎么画」。
 *
 * <p>不可变值对象，改状态用 wither 方法。
 */
public record VisualState(boolean hovered, boolean active, boolean disabled, boolean focused, boolean pressed) {

    /** 全 false 的默认状态。 */
    public static final VisualState NONE = new VisualState(false, false, false, false, false);

    public VisualState hovered(boolean b)  { return b == hovered  ? this : new VisualState(b, active, disabled, focused, pressed); }
    public VisualState active(boolean b)   { return b == active   ? this : new VisualState(hovered, b, disabled, focused, pressed); }
    public VisualState disabled(boolean b) { return b == disabled ? this : new VisualState(hovered, active, b, focused, pressed); }
    public VisualState focused(boolean b)  { return b == focused  ? this : new VisualState(hovered, active, disabled, b, pressed); }
    public VisualState pressed(boolean b)  { return b == pressed  ? this : new VisualState(hovered, active, disabled, focused, b); }

    public boolean enabled() { return !disabled; }

    /** 禁用态屏蔽交互性状态：禁用时不应表现为 hover/pressed。 */
    public VisualState normalizeForInteraction() {
        return disabled ? new VisualState(false, active, true, focused, false) : this;
    }
}
