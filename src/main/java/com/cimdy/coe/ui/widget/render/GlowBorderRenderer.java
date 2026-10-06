package com.cimdy.coe.ui.widget.render;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 脉冲发光边框：描边 alpha 用正弦呼吸，营造「被选中 / 需要留意」的动感。
 *
 * <p>用 {@link Draw#roundRectOutline} 画环，所以<b>叠加在内容之上也不会吃掉背景</b>
 * （区别于实色边框）。典型用法：聚焦的窗口、待确认的危险操作、Toast 强调。
 *
 * <p>isAnimated 返回 true，由 {@code Node.tick()} 驱动缓动；{@code tick()} 只推进相位，
 * 真正的颜色在 render 里按相位算。
 */
class GlowBorderRenderer implements Renderer, GlowBorderRendererI {

    private int color = Theme.current().accent();
    private float speed = 1.6F;     // 每秒脉冲次数
    private float minA = 0.20F;
    private float maxA = 0.85F;
    private int thickness = 1;
    private int radius = 0;
    private boolean halo = true;    // 外圈再叠一层更淡的环，强化辉光
    private float t = 0;            // 相位（单位：周期）

    public GlowBorderRenderer() {}

    public GlowBorderRenderer(int color) { this.color = color; }

    public GlowBorderRendererI color(int c)       { this.color = c; return this; }
    public GlowBorderRendererI speed(float s)     { this.speed = s; return this; }
    public GlowBorderRendererI maxAlpha(float a)  { this.maxA = a; return this; }
    public GlowBorderRendererI thickness(int t)   { this.thickness = Math.max(1, t); return this; }
    public GlowBorderRendererI radius(int r)      { this.radius = r; return this; }
    public GlowBorderRendererI halo(boolean b)    { this.halo = b; return this; }

    @Override
    public boolean isAnimated() { return true; }

    @Override
    public void tick() {
        t += speed / 60F;           // 假设 ~60fps；与渲染解耦，慢一档无妨
        if (t > 1024F) t -= 1024F;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(t * Math.PI * 2F);
        int rgb = color & 0x00FFFFFF;
        int a = (int) (Draw.clamp01(minA + (maxA - minA) * pulse) * 255);
        int c = rgb | (a << 24);

        stroke(g, x, y, w, h, radius, thickness, c);
        if (halo) {
            int ha = (int) (Draw.clamp01(a * 0.35F) * 255);
            int hc = rgb | (ha << 24);
            stroke(g, x - 1, y - 1, w + 2, h + 2, radius + 1, thickness, hc);
        }
    }

    private void stroke(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int t, int c) {
        if (r > 0) Draw.roundRectOutline(g, x, y, w, h, r, t, c);
        else Draw.outline(g, x, y, w, h, c);
    }

    @Override
    public Renderer copy() {
        GlowBorderRenderer n = new GlowBorderRenderer(color);
        n.speed = speed; n.minA = minA; n.maxA = maxA;
        n.thickness = thickness; n.radius = radius; n.halo = halo; n.t = t;
        return n;
    }
}
