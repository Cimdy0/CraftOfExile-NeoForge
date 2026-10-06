package com.cimdy.coe.ui.widget.attach;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 装饰附件：静态视觉细节（角标徽章 / 分隔线 / 标题栏高光带 / 双层描线 / 聚焦光晕）。
 *
 * <p>基于已有基底（{@code cornerDots} / {@code cornerTicks} / {@code WINDOW_TOP} / {@code gradient} /
 * {@code BORDER_STRONG} / {@code Theme.glow()}）组合，不重写基底；作为 {@link Attachment}
 * 挂到节点上画在 visuals 之上，或直接用 {@link #renderer()} 取单个 {@link Renderer} 内联使用。
 *
 * <p>取色一律走 {@code Theme.current()} / {@code StylePreset} 令牌，不写死色值。
 */
public final class Decor implements Attachment {

    /** 分隔线方向。 */
    public enum Divider { HORIZONTAL, VERTICAL }

    private final List<Renderer> layers = new ArrayList<>(2);
    private boolean doubleBorder = false;
    private boolean titleBand = false;
    private boolean cornerDeco = false;
    private int badgeColor = 0;
    private Component badgeText;
    private BadgeCorner badgeCorner = BadgeCorner.TOP_RIGHT;

    public enum BadgeCorner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private Decor() {}

    // ------------------------------------------------------------------
    // 构造
    // ------------------------------------------------------------------

    /** 空装饰（可链式叠加）。 */
    public static Decor of() { return new Decor(); }

    /** 由一组现成渲染器构成的装饰。 */
    public static Decor of(Renderer... renderers) {
        Decor d = new Decor();
        if (renderers != null) {
            for (Renderer r : renderers) if (r != null) d.layers.add(r);
        }
        return d;
    }

    // ------------------------------------------------------------------
    // 组件：分隔线
    // ------------------------------------------------------------------

    /** 渐变分隔线（角标徽章 / 分区之间）。 */
    public Decor divider(Divider dir) {
        return divider(dir, Theme.current().borderStrong(), 1);
    }

    public Decor divider(Divider dir, int color, int thickness) {
        layers.add(new DividerRenderer(dir, color, thickness));
        return this;
    }

    /** 标题栏高光带：顶部 2px WINDOW_TOP → 透明的渐变带。 */
    public Decor titleBand() {
        this.titleBand = true;
        return this;
    }

    /** 双层描线：BORDER_STRONG 外再加 1px 暗线（轮廓立体感）。 */
    public Decor doubleBorder() {
        this.doubleBorder = true;
        return this;
    }

    /** 四角点缀 + 四角短括号（复用既有 cornerDots/cornerTicks 基底）。 */
    public Decor cornerDeco() {
        this.cornerDeco = true;
        return this;
    }

    /** 聚焦光晕：复用 {@link Theme#glow()}（GlowBorderRenderer 呼吸描边）。 */
    public Decor focusGlow() {
        layers.add(Theme.glow());
        return this;
    }

    // ------------------------------------------------------------------
    // 组件：角标徽章
    // ------------------------------------------------------------------

    /** 角标徽章（计数 / 状态小胶囊），默认右上角。 */
    public Decor badge(Component text) {
        return badge(text, Theme.current().accent(), BadgeCorner.TOP_RIGHT);
    }

    public Decor badge(Component text, int color) {
        return badge(text, color, BadgeCorner.TOP_RIGHT);
    }

    public Decor badge(Component text, int color, BadgeCorner corner) {
        this.badgeText = text;
        this.badgeColor = color;
        this.badgeCorner = corner == null ? BadgeCorner.TOP_RIGHT : corner;
        return this;
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (w <= 0 || h <= 0) return;
        if (doubleBorder) {
            Draw.outline(g, x - 1, y - 1, w + 2, h + 2, Theme.darken(Theme.current().borderStrong(), 0.55F));
        }
        if (titleBand) {
            int top = Theme.WINDOW_TOP;
            Draw.gradientV(g, x, y, w, Math.min(2, h), top, (top & 0x00FFFFFF) | 0x10000000);
        }
        if (cornerDeco) {
            int dots = Theme.current().cornerDots();
            int ticks = Theme.current().cornerTicks();
            Draw.cornerDots(g, x, y, w, h, dots);
            Draw.cornerTicks(g, x, y, w, h, 2, 3, ticks);
        }
        for (Renderer r : layers) {
            try {
                r.render(g, x, y, w, h, ctx);
            } catch (RuntimeException ignored) { /* 附件失败静默降级 */ }
        }
        if (badgeText != null) drawBadge(g, x, y, w, h, ctx);
    }

    private void drawBadge(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        String s = badgeText.getString();
        int tw = Draw.width(s);
        int bw = tw + 8, bh = 8;
        int bx, by;
        int pad = 2;
        switch (badgeCorner) {
            case TOP_LEFT -> { bx = x + pad; by = y + pad; }
            case BOTTOM_LEFT -> { bx = x + pad; by = y + h - bh - pad; }
            case BOTTOM_RIGHT -> { bx = x + w - bw - pad; by = y + h - bh - pad; }
            default -> { bx = x + w - bw - pad; by = y + pad; }
        }
        Draw.fill(g, bx, by, bw, bh, badgeColor);
        int textColor = 0xFF10141A; // 徽章底色上的深色文字（徽章色为亮强调色时对比清晰）
        Draw.text(g, s, bx + 4, by + (bh - Draw.lineHeight()) / 2, textColor);
    }

    // ------------------------------------------------------------------

    /** 渐变分隔线渲染器（水平 / 垂直，默认两色）。 */
    private static final class DividerRenderer implements Renderer {
        private final Divider dir;
        private final int color;
        private final int thickness;

        DividerRenderer(Divider dir, int color, int thickness) {
            this.dir = dir;
            this.color = color;
            this.thickness = Math.max(1, thickness);
        }

        @Override
        public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
            int fade = (color & 0x00FFFFFF) | 0x10000000;
            if (dir == Divider.HORIZONTAL) {
                int mid = y + h / 2;
                Draw.gradientV(g, x, mid, w / 2, thickness, fade, color);
                Draw.gradientV(g, x + w / 2, mid, w - w / 2, thickness, color, fade);
            } else {
                int mid = x + w / 2;
                Draw.gradientV(g, mid, y, thickness, h / 2, fade, color);
                Draw.gradientV(g, mid, y + h / 2, thickness, h - h / 2, color, fade);
            }
        }

        @Override
        public Renderer copy() {
            return new DividerRenderer(dir, color, thickness);
        }
    }
}
