package com.cimdy.coe.ui.widget.graph.painter;

/**
 * 连线画笔的可配置能力面：在 {@link EdgePainter} 的「画」语义之上补上链式配置方法。
 *
 * <p>具体实现类包私有不可见，外部一律经 {@link NodePainters#line()} 拿到本接口实例。
 *
 * @param <T> 节点载荷类型
 */
public interface EdgePainterI<T> extends EdgePainter<T> {

    EdgePainterI<T> color(int argb);

    EdgePainterI<T> colorActive(int argb);

    EdgePainterI<T> colorHover(int argb);

    EdgePainterI<T> colorDisabled(int argb);

    EdgePainterI<T> widthPixels(float px);
}
