package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 节点画笔：在 <b>world 空间</b>画一个节点。
 *
 * <p>调用时相机变换已经压进 pose，直接用 {@code node.worldX/worldY} 当坐标画即可，
 * 不要自己再做一遍 world→screen（两套坐标换算并存时缩放一变就对不齐）。
 *
 * <p>节点是什么状态由画布算好后放进 {@code ctx.state()}，画笔只回答「这个状态下我怎么画」。
 * 业务专属的视觉（贴图边框、绑定色、图标）在业务侧实现本接口，lib 不掺和。
 *
 * @param <T> 节点载荷类型
 */
@FunctionalInterface
public interface NodePainter<T> {

    /**
     * @param zoom 当前缩放系数。需要「无论怎么缩放都至少 1 像素粗」的细节靠它换算。
     */
    void paint(GuiGraphicsExtractor g, GraphNode<T> node, RenderContext ctx, float zoom);
}
