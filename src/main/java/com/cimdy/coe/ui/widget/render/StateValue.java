package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.core.VisualState;

import java.util.function.UnaryOperator;

/**
 * 随可视状态变化的取值容器。
 *
 * <p>「常态一个色、悬浮一个色、禁用再一个色」是 UI 里最高频的需求。旧库要么写死两个字段
 * （backgroundColor / backgroundHoverColor），要么整个换渲染器；这里统一成一个容器，
 * 未设置的状态自动回退到常态值，因此只关心常态时写法和以前一样简单。
 *
 * <p>优先级：disabled &gt; pressed &gt; active &gt; hovered &gt; normal。
 *
 * @param <T> 取值类型，如 {@code Integer}（颜色）、{@code Component}（文本）、{@code String}（贴图路径）
 */
public final class StateValue<T> {

    private T normal;
    private T hovered;
    private T active;
    private T disabled;
    private T pressed;

    private StateValue(T normal) {
        this.normal = normal;
    }

    public static <T> StateValue<T> of(T normal) {
        return new StateValue<>(normal);
    }

    public StateValue<T> normal(T v)   { this.normal = v;   return this; }
    public StateValue<T> hovered(T v)  { this.hovered = v;  return this; }
    public StateValue<T> active(T v)   { this.active = v;   return this; }
    public StateValue<T> disabled(T v) { this.disabled = v; return this; }
    public StateValue<T> pressed(T v)  { this.pressed = v;  return this; }

    /** 按状态优先级取值，缺失的状态回退到常态。 */
    public T get(VisualState s) {
        if (s.disabled() && disabled != null) return disabled;
        if (s.pressed()  && pressed  != null) return pressed;
        if (s.active()   && active   != null) return active;
        if (s.hovered()  && hovered  != null) return hovered;
        return normal;
    }

    /** 取常态值。 */
    public T base() { return normal; }

    public StateValue<T> copy() {
        return copy(UnaryOperator.identity());
    }

    /** 深拷贝，元素本身的拷贝方式由 {@code cloner} 决定。 */
    public StateValue<T> copy(UnaryOperator<T> cloner) {
        StateValue<T> c = new StateValue<>(normal == null ? null : cloner.apply(normal));
        c.hovered  = hovered  == null ? null : cloner.apply(hovered);
        c.active   = active   == null ? null : cloner.apply(active);
        c.disabled = disabled == null ? null : cloner.apply(disabled);
        c.pressed  = pressed  == null ? null : cloner.apply(pressed);
        return c;
    }
}
