package com.cimdy.coe.ui.widget.render;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 柔和投影：画在节点<b>正下方</b>（向右下偏移的半透圆角矩形叠层），让面板从背景里浮起来。
 *
 * <p>本身是半透且用 {@link Draw#shadow} 做多层递减 alpha，所以<b>不需要已知背景色</b>，
 * 直接 {@code with()} 到任意节点最底层即可。窗口 {@code styled()} 默认就挂一层。
 */
class ShadowRenderer implements Renderer, ShadowRendererI {

    private int offset = 2;
    private int blur = 4;
    private int radius = 3;
    private int argb = Theme.current().panelDim();

    public ShadowRenderer() {}

    public ShadowRenderer(int offset, int blur, int radius, int argb) {
        this.offset = offset;
        this.blur = blur;
        this.radius = radius;
        this.argb = argb;
    }

    public ShadowRendererI offset(int o)  { this.offset = o; return this; }
    public ShadowRendererI blur(int b)    { this.blur = b; return this; }
    public ShadowRendererI radius(int r)  { this.radius = r; return this; }
    public ShadowRendererI color(int c)   { this.argb = c; return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        Draw.shadow(g, x + offset, y + offset, w, h, radius, blur, argb);
    }

    @Override
    public Renderer copy() {
        return new ShadowRenderer(offset, blur, radius, argb);
    }
}
