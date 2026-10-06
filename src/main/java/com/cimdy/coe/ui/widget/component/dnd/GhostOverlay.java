package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 残影图层：挂在 {@link com.cimdy.coe.ui.widget.component.panel.WindowLayer} 上的纯绘制节点，
 * 每帧从 {@link DragRegistry#activeDrag()} 读当前拖拽并画出跟随光标的残影。
 *
 * <p>它是覆盖全屏的「最上层叶子」：自身 {@link #contains} 永远返回 {@code false}，因此不抢点击 /
 * 不影响 tooltip 命中，只负责在一切之上画残影。残影只依赖当前激活拖拽的绝对坐标，
 * 不参与任何 Flex 布局（布局会把它铺满全屏，但我们忽略它自己的盒子，直接画在拖拽坐标处）。
 */
public class GhostOverlay extends Node {

    private final DragRegistry registry;

    public GhostOverlay(DragRegistry registry) {
        this.registry = registry;
        this.id = "drag-ghost-overlay";
    }

    /** 残影图层不参与命中 / 事件：它只是覆盖全屏的绘制层。 */
    @Override
    public boolean contains(double mx, double my) {
        return false;
    }

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        DragRegistry.ActiveDrag d = registry.activeDrag();
        if (d == null) return;
        int x = (int) Math.round(d.x);
        int y = (int) Math.round(d.y);
        int w = d.w;
        int h = d.h;
        int bg = Theme.current().panelColor();
        int border = d.hovered ? Theme.current().accent() : Theme.current().panelSide();
        Draw.roundRect(g, x, y, w, h, 3, bg);
        Draw.roundRectOutline(g, x, y, w, h, 3, 1, border);
        if (d.label != null) {
            int ty = y + Math.max(0, (h - Draw.lineHeight()) / 2);
            Draw.text(g, d.label, x + 4, ty, Theme.current().text());
        }
    }
}
