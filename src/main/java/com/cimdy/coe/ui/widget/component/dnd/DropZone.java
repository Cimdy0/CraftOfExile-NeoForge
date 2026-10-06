package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.core.Node;

/**
 * 放置目标：拖拽松手时由 {@link DragRegistry} 按矩形命中最近的 zone 并派发 {@link #onDrop}。
 *
 * <p>库只约定「矩形命中」这一最小契约，具体命中区由实现自己决定（通常是某个 Node 的布局盒，
 * 见 {@link NodeDropZone}）。命中区坐标用屏幕绝对坐标，与 {@link Node#contains} 同口径。
 *
 * <p>{@link #scope()} 返回该放置区所属的作用域节点（最近的 {@link DragScope} 容器），
 * 用于 {@link DragRegistry} 的作用域优先级命中。默认 {@code null} 表示全局 zone。
 */
public interface DropZone {

    /** 该屏幕坐标是否落在放置区内（矩形命中）。 */
    boolean acceptDrop(double mx, double my);

    /** 拖拽在 {@code source} 上松手且本区被命中时调用。 */
    void onDrop(Draggable source);

    /** 放置区所属作用域（最近的 {@link DragScope} 容器节点）；{@code null} = 全局。 */
    default Node scope() { return null; }
}
