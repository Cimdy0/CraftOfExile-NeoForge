package com.cimdy.coe.ui.widget.graph.core;

import com.cimdy.coe.ui.widget.graph.painter.NodePainter;

/**
 * 图上的一个节点。
 *
 * <p>注意它<b>不是</b> {@link com.cimdy.coe.ui.widget.core.Node} —— 图节点动辄几千个，
 * 每个都做成参与 Flexbox 的 UI 节点太重。这里只是一条轻量记录：坐标 + 半径 + 业务载荷，
 * 画法交给 {@link NodePainter}，状态交给画布的状态函数。
 *
 * <p>{@code payload} 是唯一的业务出口：画布本身对它一无所知，
 * 只负责在回调（画笔 / 状态函数 / tooltip / 激活事件）里原样交还给业务代码。
 *
 * @param <T> 业务载荷类型
 */
public class GraphNode<T> {

    /** 命中与默认画法的形状。 */
    public enum Shape { CIRCLE, SQUARE }

    public final String id;

    /** world 坐标，来自数据而非布局计算。 */
    public float worldX, worldY;

    /** world 单位的半径（方形节点即为半边长）。命中测试与默认画笔都用它。 */
    public float radius = 8f;

    public Shape shape = Shape.CIRCLE;

    public boolean visible = true;

    public final T payload;

    public GraphNode(String id, float worldX, float worldY, T payload) {
        this.id = id;
        this.worldX = worldX;
        this.worldY = worldY;
        this.payload = payload;
    }

    public GraphNode<T> at(float x, float y) { this.worldX = x; this.worldY = y; return this; }

    public GraphNode<T> radius(float r) { this.radius = r; return this; }

    public GraphNode<T> shape(Shape s) { this.shape = s; return this; }

    public GraphNode<T> visible(boolean b) { this.visible = b; return this; }

    /** world 空间命中测试，{@code slack} 为额外容差（world 单位）。 */
    public boolean containsWorld(float wx, float wy, float slack) {
        float r = radius + slack;
        float dx = wx - worldX, dy = wy - worldY;
        if (shape == Shape.SQUARE) {
            return Math.abs(dx) <= r && Math.abs(dy) <= r;
        }
        return dx * dx + dy * dy <= r * r;
    }

    /** 命中优先级用的「离中心多近」，越小越优先。 */
    public float distance2(float wx, float wy) {
        float dx = wx - worldX, dy = wy - worldY;
        return dx * dx + dy * dy;
    }
}
