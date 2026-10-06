package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.StateValue;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 默认节点画笔：纯色圆点 / 方块 + 状态描边。
 *
 * <p>它的定位是<b>不带任何业务假设的兜底画法</b> —— 一个新的图不写任何渲染代码也能直接看见。
 * 需要贴图边框、图标、绑定色这类专属视觉时，在业务侧实现 {@link NodePainter} 替换掉它。
 *
 * <p>颜色按 {@link StateValue} 的状态优先级取：disabled &gt; pressed &gt; active &gt; hovered &gt; normal。
 * 图上的语义可以这样映射：已激活 → {@code active}，可交互 → {@code normal}，
 * 未解锁 → {@code disabled}，被聚焦 → {@code focused}（用 {@link #focusRing(int)} 单独着色）。
 *
 * @param <T> 节点载荷类型
 */
class DotNodePainter<T> implements NodePainter<T> {

    private final StateValue<Integer> fill = StateValue.of(Theme.current().panelColor());
    private final StateValue<Integer> border = StateValue.of(Theme.current().graphNodeBorder());

    /** 聚焦（键盘/点击聚焦）时额外套的一圈，0 表示不画。 */
    private int focusRing = Theme.current().focusRing();
    /** 悬浮时额外套的一圈，0 表示不画。 */
    private int hoverRing = Theme.current().graphHoverRing();
    /** 边框厚度，单位是<b>屏幕像素</b>，画的时候按 zoom 换算成 world 单位，保证任何缩放下都看得见。 */
    private float borderPixels = 1f;

    public DotNodePainter() {
        fill.hovered(Theme.current().popupBorder())
                .active(Theme.current().graphNodeActiveFill())
                .disabled(Theme.current().graphNodeDisabledFill())
                .pressed(Theme.current().graphNodeActiveFill());
        border.hovered(Theme.current().graphHoverRing())
                .active(Theme.current().accent())
                .disabled(Theme.current().graphNodeDisabledBorder())
                .pressed(Theme.current().accent());
    }

    // ------------------------------------------------------------------

    public DotNodePainter<T> fill(int argb)         { fill.normal(argb);   return this; }

    public DotNodePainter<T> border(int argb)         { border.normal(argb);   return this; }
    public DotNodePainter<T> borderHover(int argb)    { border.hovered(argb);  return this; }
    public DotNodePainter<T> borderActive(int argb)   { border.active(argb);   return this; }
    public DotNodePainter<T> borderDisabled(int argb) { border.disabled(argb); return this; }

    public DotNodePainter<T> focusRing(int argb) { this.focusRing = argb; return this; }
    public DotNodePainter<T> hoverRing(int argb) { this.hoverRing = argb; return this; }
    public DotNodePainter<T> borderPixels(float px) { this.borderPixels = Math.max(0, px); return this; }

    // ------------------------------------------------------------------

    @Override
    public void paint(GuiGraphicsExtractor g, GraphNode<T> node, RenderContext ctx, float zoom) {
        int cx = Math.round(node.worldX);
        int cy = Math.round(node.worldY);
        int r = Math.max(1, Math.round(node.radius));
        int thick = worldThickness(borderPixels, zoom);

        int fillColor = fill.get(ctx.state());
        int borderColor = border.get(ctx.state());

        // 先画外圈再画本体 —— 圆形没有廉价的镂空画法，靠「本体盖住外圈内部」造出环的效果，
        // 顺序反了就会变成一坨实心色块把节点糊掉
        if (ctx.hovered() && !Draw.isTransparent(hoverRing)) {
            ring(g, node, cx, cy, r + thick * 2, hoverRing);
        }
        if (ctx.focused() && !Draw.isTransparent(focusRing)) {
            ring(g, node, cx, cy, r + thick, focusRing);
        }

        if (node.shape == GraphNode.Shape.SQUARE) {
            Draw.fill(g, cx - r, cy - r, r * 2, r * 2, fillColor);
            for (int i = 0; i < thick; i++) {
                Draw.outline(g, cx - r + i, cy - r + i, r * 2 - i * 2, r * 2 - i * 2, borderColor);
            }
        } else {
            Draw.discWithBorder(g, cx, cy, r, fillColor, borderColor, thick);
            // 禁用 = 断点环（与天赋/研究树同形状语言）：四正方向 30° 缺口用 fill 回涂，
            // 纯色边框在浅底上近乎隐形（ACC-002 §4），缺口形状线索是唯一可靠区分。
            if (ctx.state().disabled()) {
                int rIn = r - thick, rOut = r;
                int gap = Math.max(2, (int) Math.round((r - thick * 0.5F) * 2 * Math.sin(Math.toRadians(15))));
                Draw.fill(g, cx - gap / 2, cy - rOut, gap, rOut - rIn, fillColor);   // N
                Draw.fill(g, cx - gap / 2, cy + rIn, gap, rOut - rIn, fillColor);    // S
                Draw.fill(g, cx - rOut, cy - gap / 2, rOut - rIn, gap, fillColor);   // W
                Draw.fill(g, cx + rIn, cy - gap / 2, rOut - rIn, gap, fillColor);    // E
            }
        }
    }

    /** 在半径 r 处铺一层底色，随后本体会盖住中间部分，于是看上去是一圈环。 */
    private void ring(GuiGraphicsExtractor g, GraphNode<T> node, int cx, int cy, int r, int argb) {
        if (node.shape == GraphNode.Shape.SQUARE) {
            Draw.fill(g, cx - r, cy - r, r * 2, r * 2, argb);
        } else {
            Draw.disc(g, cx, cy, r, argb);
        }
    }

    /** 把「屏幕像素」换算成 world 单位，至少 1，保证缩到很小时描边不会消失。 */
    private static int worldThickness(float px, float zoom) {
        if (px <= 0) return 0;
        return Math.max(1, Math.round(px / Math.max(1e-4F, zoom)));
    }
}
