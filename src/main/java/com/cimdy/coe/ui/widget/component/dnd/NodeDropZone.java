package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.core.Node;

import java.util.function.Consumer;

/**
 * 把任意 {@link Node} 的布局盒变成一个放置区：命中即按矩形判定，命中后回调 {@code action}。
 *
 * <p>最常见的落地方式 —— 给某个面板 / 格子节点包一层即可，无需自己算坐标：
 * <pre>{@code
 * DropZone zone = new NodeDropZone(myPanel, src -> onDropped(src)).autoScope();
 * layer.dragRegistry().register(zone);
 * }</pre>
 *
 * <h3>作用域（嵌套 DragRegistry）</h3>
 * 默认该 zone 是全局（{@code scope() == null}）。调用 {@link #autoScope()} 让它继承所属容器的
 * 作用域（最近的 {@link DragScope} 祖先，通常是所在窗口 / 滚动容器），命中时被该容器内的拖拽优先选中；
 * 也可 {@link #withScope(Node)} 显式指定。
 *
 * <h3>物理重定位</h3>
 * 调用 {@link #relocateSource()} 后，命中时不仅回调 {@code action}，还会把被拖的 {@link Draggable}
 * 节点真正<b>搬进</b>本节点（从旧父级移除并加入本节点），让「松手即移动」落到实处——
 * 配合每帧重布局，节点会立即出现在目标容器里。
 */
public class NodeDropZone implements DropZone {

    private final Node node;
    private final Consumer<Draggable> action;

    /** 作用域节点（最近的 DragScope 容器），{@code null} = 全局。 */
    private Node scopeNode;
    /** 命中时是否把被拖节点物理搬进本节点。 */
    private boolean relocateSource = false;

    public NodeDropZone(Node node, Consumer<Draggable> action) {
        this.node = node;
        this.action = action;
    }

    /** 显式指定作用域（通常是某个窗口 / 滚动容器节点）。 */
    public NodeDropZone withScope(Node scope) { this.scopeNode = scope; return this; }

    /** 作用域继承所属容器：取 node 最近的 DragScope 祖先（窗口 / 滚动容器）。 */
    public NodeDropZone autoScope() { this.scopeNode = node.findDragScopeNode(); return this; }

    /** 命中时把被拖节点物理搬进本节点（默认关）。 */
    public NodeDropZone relocateSource() { this.relocateSource = true; return this; }

    /** 命中时是否把被拖节点物理搬进本节点。 */
    public NodeDropZone relocateSource(boolean b) { this.relocateSource = b; return this; }

    @Override
    public Node scope() { return scopeNode; }

    @Override
    public boolean acceptDrop(double mx, double my) {
        return node.contains(mx, my);
    }

    @Override
    public void onDrop(Draggable source) {
        if (relocateSource && source != null && source != node) {
            // 物理重定位：从旧父级摘除，真正搬进本节点（下一帧重布局即生效）
            if (source.parent != null) source.parent.remove(source);
            node.add(source);
        }
        if (action != null) action.accept(source);
    }
}
