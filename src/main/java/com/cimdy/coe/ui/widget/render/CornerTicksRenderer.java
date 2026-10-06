package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.StateValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 四角短横线装饰（旧 {@code CornerBorderRenderer}）。
 *
 * <p>在距边 {@code padding} 处，从四个角各引出一段长 {@code length} 的水平线，
 * 用来给面板加「取景框」质感，并带状态感知的颜色。
 */
class CornerTicksRenderer implements Renderer, CornerTicksI {

    private final int padding;
    private final int length;
    private StateValue<Integer> color;

    public CornerTicksRenderer(int padding, int length, int argb) {
        this.padding = padding;
        this.length = length;
        this.color = StateValue.of(argb);
    }

    public CornerTicksRenderer colorHover(int argb)    { color.hovered(argb);  return this; }
    public CornerTicksRenderer colorActive(int argb)   { color.active(argb);   return this; }
    public CornerTicksRenderer colorDisabled(int argb) { color.disabled(argb); return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        Draw.cornerTicks(g, x, y, w, h, padding, length, color.get(ctx.state()));
    }

    @Override
    public Renderer copy() {
        CornerTicksRenderer c = new CornerTicksRenderer(padding, length, 0);
        c.color = color.copy();
        return c;
    }
}
