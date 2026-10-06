package com.cimdy.coe.ui.widget.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 窗口 / 面板背景纹理叠加层（附录 D.2）。
 *
 * <p>纯 {@link Draw} 程序化手绘（fill / gradientV / cornerDots），<b>不引入任何贴图资源依赖</b>，
 * 与既有 {@code shadow()/glow()/shimmer()} 同一套「挂到节点 visuals 上的 Renderer」模式。
 * 颜色全部渲染时读 {@link Theme#current()} 令牌（{@link StylePreset#backgroundLineColor()} /
 * {@link StylePreset#backgroundGlowColor()} / {@link StylePreset#backgroundBands()}），换肤即时跟随。
 *
 * <p>纪律：所有颜色为极低 alpha 装饰色，画在底色之上、内容之下，<b>不改命中区与布局坐标</b>；
 * 不参与动画（{@link #isAnimated()} 恒 false），无 tick 开销。
 */
public final class BackgroundTextureRenderer implements Renderer {

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (w <= 0 || h <= 0) return;
        StylePreset skin = Theme.current();
        switch (skin.backgroundKind()) {
            case NONE     -> { /* 中性档：不画装饰纹理 */ }
            case BLUE_STEEL -> drawBlueSteel(g, x, y, w, h, skin);
            case PARCHMENT  -> drawParchment(g, x, y, w, h, skin);
            case NEON       -> drawNeon(g, x, y, w, h, skin);
            case CRIMSON    -> drawCrimson(g, x, y, w, h, skin);
            case AURORA     -> drawAurora(g, x, y, w, h, skin);
        }
    }

    /** 蓝钢：细网格线（16px 格）+ 底部暗蓝辉光。 */
    private static void drawBlueSteel(GuiGraphicsExtractor g, int x, int y, int w, int h, StylePreset skin) {
        int line = skin.backgroundLineColor();
        for (int cx = x + 16; cx < x + w; cx += 16) Draw.fill(g, cx, y, 1, h, line);
        for (int cy = y + 16; cy < y + h; cy += 16) Draw.fill(g, x, cy, w, 1, line);
        int glow = skin.backgroundGlowColor();
        int glowH = Math.min(20, h);
        int clear = glow & 0x00FFFFFF;
        Draw.gradientV(g, x, y + h - glowH, w, glowH, clear, glow);
    }

    /** 羊皮纸：边缘上下渐暗 + 稀疏纸纤维（水平线）。 */
    private static void drawParchment(GuiGraphicsExtractor g, int x, int y, int w, int h, StylePreset skin) {
        int edgeDark = 0x14000000; // 中性墨色，极低 alpha
        int edgeClear = 0x00000000;
        int vh = Math.min(10, h / 3);
        Draw.gradientV(g, x, y, w, vh, edgeDark, edgeClear);
        Draw.gradientV(g, x, y + h - vh, w, vh, edgeClear, edgeDark);
        int fiber = skin.backgroundLineColor();
        for (int fy = y + 5; fy < y + h - 5; fy += 5) Draw.fill(g, x, fy, w, 1, fiber);
    }

    /** 霓虹：细扫描线（3px 一行）+ 角部光晕点。 */
    private static void drawNeon(GuiGraphicsExtractor g, int x, int y, int w, int h, StylePreset skin) {
        int line = skin.backgroundLineColor();
        for (int sy = y; sy < y + h; sy += 3) Draw.fill(g, x, sy, w, 1, line);
        Draw.cornerDots(g, x, y, w, h, skin.backgroundGlowColor());
    }

    /** 绯红：织物斜纹（细竖纹近似）+ 顶部暗红渐变带。 */
    private static void drawCrimson(GuiGraphicsExtractor g, int x, int y, int w, int h, StylePreset skin) {
        int line = skin.backgroundLineColor();
        for (int cx = x; cx < x + w; cx += 6) Draw.fill(g, cx, y, 1, h, line);
        int glow = skin.backgroundGlowColor();
        int glowH = Math.min(14, h);
        int clear = glow & 0x00FFFFFF;
        Draw.gradientV(g, x, y, w, glowH, glow, clear);
    }

    /** 极光：竖向渐变带（青-紫-粉，低透明度，三段竖条色 wash）。 */
    private static void drawAurora(GuiGraphicsExtractor g, int x, int y, int w, int h, StylePreset skin) {
        int[] bands = skin.backgroundBands();
        int seg = Math.max(1, w / bands.length);
        for (int i = 0; i < bands.length; i++) {
            int bx = x + i * seg;
            int bw = (i == bands.length - 1) ? (x + w - bx) : seg;
            Draw.fill(g, bx, y, bw, h, bands[i]);
        }
    }

    @Override public Renderer copy() { return new BackgroundTextureRenderer(); }
}
