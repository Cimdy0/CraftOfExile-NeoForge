package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.ui.widget.graph.core.GraphEdge;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 连线画笔：在 <b>world 空间</b>画一条边。两端节点已由画布解析好一并传入，
 * 免得每个实现都去查一次索引表。
 *
 * @param <T> 节点载荷类型
 */
@FunctionalInterface
public interface EdgePainter<T> {

    void paint(GuiGraphicsExtractor g, GraphEdge edge,
               GraphNode<T> from, GraphNode<T> to, RenderContext ctx, float zoom);
}
