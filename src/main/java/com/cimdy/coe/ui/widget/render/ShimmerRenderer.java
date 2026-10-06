package com.cimdy.coe.ui.widget.render;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 扫光带：一条半透亮带从左向右掠过节点，循环往复，给面板/按钮一点「活气」。
 *
 * <p>实现为若干层竖直 1px 亮条（沿带宽做三角 alpha 衰减），每帧把整带右移 {@code speed} 像素，
 * 越界即回到左侧重来。代价稳定（≈ {@code band/step} 次 fill/帧），与节点尺寸无关。
 *
 * <p>默认始终扫动；若要「仅悬浮时扫」，由调用方按 {@link RenderContext#hovered()} 控制是否挂本层即可。
 */
class ShimmerRenderer implements Renderer, ShimmerRendererI {

    private int color = Theme.current().titleBarBg();
    private float speed = 3.0F;     // 像素/帧
    private int band = 16;          // 光带半宽
    private int step = 2;           // 亮条间距（越小越密越平滑）
    private float maxA = 0.45F;
    private int radius = 0;
    private float pos = -band;      // 当前光带中心（节点局部 x）

    public ShimmerRenderer() {}

    public ShimmerRenderer(int color) { this.color = color; }

    public ShimmerRendererI color(int c)    { this.color = c; return this; }
    public ShimmerRendererI speed(float s)  { this.speed = s; return this; }
    public ShimmerRendererI band(int b)     { this.band = Math.max(1, b); return this; }
    public ShimmerRendererI step(int s)     { this.step = Math.max(1, s); return this; }
    public ShimmerRendererI maxAlpha(float a){ this.maxA = a; return this; }
    public ShimmerRendererI radius(int r)   { this.radius = r; return this; }

    @Override
    public boolean isAnimated() { return true; }

    @Override
    public void tick() {
        pos += speed;
        if (pos > 0 && pos > band * 2) pos = -band;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (w <= 0 || h <= 0) return;
        int rgb = color & 0x00FFFFFF;
        int center = (int) pos;
        for (int k = -band; k <= band; k += step) {
            int jx = center + k;
            if (jx < 0 || jx >= w) continue;
            float a = maxA * (1F - (float) Math.abs(k) / band);
            if (a <= 0) continue;
            int c = rgb | (Math.max(0, Math.min(255, (int) (a * 255))) << 24);
            if (radius > 0) {
                // 圆角节点：用 roundRect 仅在带内画一小段圆角竖条，边角处自然收缩
                Draw.roundRect(g, x + jx, y, 1, h, radius, c);
            } else {
                Draw.fill(g, x + jx, y, 1, h, c);
            }
        }
    }

    @Override
    public Renderer copy() {
        ShimmerRenderer n = new ShimmerRenderer(color);
        n.speed = speed; n.band = band; n.step = step; n.maxA = maxA; n.radius = radius; n.pos = pos;
        return n;
    }
}
