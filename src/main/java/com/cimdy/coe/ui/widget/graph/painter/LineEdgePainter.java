package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.ui.widget.graph.painter.EdgePainter;
import com.cimdy.coe.ui.widget.graph.core.GraphEdge;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.StateValue;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * 默认连线画笔：两点之间一条直线。
 *
 * <p>实现要点是<b>用 pose 旋转代替逐像素画线</b> —— 把坐标系转到线的方向上，
 * 一次 fill 就是一条线，每条边只有 1 次绘制调用。上千条边时这个差别是数量级的。
 *
 * <p>线宽按屏幕像素声明，绘制时换算成 world 单位，因此缩到很小也不会细到看不见。
 *
 * @param <T> 节点载荷类型
 */
class LineEdgePainter<T> implements EdgePainter<T>, EdgePainterI<T> {

    private final StateValue<Integer> color = StateValue.of(Theme.current().popupBorder());

    /** 线宽（屏幕像素）。 */
    private float widthPixels = 1f;

    public LineEdgePainter() {
        color.active(Theme.current().accent()).hovered(Theme.current().graphEdgeHover()).disabled(Theme.current().borderDisabled());
    }

    public EdgePainterI<T> color(int argb)         { color.normal(argb);   return this; }
    public EdgePainterI<T> colorActive(int argb)   { color.active(argb);   return this; }
    public EdgePainterI<T> colorHover(int argb)    { color.hovered(argb);  return this; }
    public EdgePainterI<T> colorDisabled(int argb) { color.disabled(argb); return this; }
    public EdgePainterI<T> widthPixels(float px)   { this.widthPixels = Math.max(0.1F, px); return this; }

    @Override
    public void paint(GuiGraphicsExtractor g, GraphEdge edge,
                      GraphNode<T> from, GraphNode<T> to, RenderContext ctx, float zoom) {
        int argb = color.get(ctx.state());
        if (Draw.isTransparent(argb)) return;

        float dx = to.worldX - from.worldX;
        float dy = to.worldY - from.worldY;
        float len = (float) Math.hypot(dx, dy);
        if (len < 1e-3F) return;

        float mx = (from.worldX + to.worldX) * 0.5F;
        float my = (from.worldY + to.worldY) * 0.5F;
        float angle = (float) Math.atan2(dy, dx);

        // 1 屏幕像素 = 1/zoom world 单位
        float thick = Math.max(0.5F, widthPixels / Math.max(1e-4F, zoom));
        int halfLen = (int) Math.ceil(len * 0.5F);
        int halfThick = Math.max(1, (int) Math.ceil(thick * 0.5F));

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        try {
            pose.translate(mx, my);
            pose.rotate(angle);
            g.fill(-halfLen, -halfThick, halfLen, halfThick, argb);
        } finally {
            pose.popMatrix();
        }
    }
}
