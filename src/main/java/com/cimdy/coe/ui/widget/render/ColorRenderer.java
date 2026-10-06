package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.component.panel.SidePanel;
import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.StateValue;
import com.cimdy.coe.ui.widget.render.BorderStyle;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 纯色背景 + 边框渲染器 —— 整个库最常用的一层。
 *
 * <p>一个类顶掉旧库的三个：{@code SimpleColorRenderer}（背景+全边框）、
 * {@code ColorBorder}（只要边框）、{@code ColorTitle} 的底盒（背景+边框+角点）。
 * 差异不再靠「换类」表达，而是靠配置：边框样式 + 各状态颜色。
 *
 * <pre>{@code
 * // 常态深灰、悬浮亮一档、禁用变暗，边框金色
 * ColorRenderer.of(0xFF2A2A2A, 0xFF5A4A2A)
 *     .backgroundHover(0xFF3A3A3A)
 *     .backgroundDisabled(0xFF1A1A1A);
 * }</pre>
 */
class ColorRenderer implements Renderer, ColorRendererI {

    private StateValue<Integer> background = StateValue.of(0);
    private StateValue<Integer> border = StateValue.of(0);
    private BorderStyle borderStyle = BorderStyle.NONE;
    /** 四角 1px 点缀色，0 表示不画 */
    private int cornerDotColor = 0;

    /** 圆角半径（px）；0 表示直角。 */
    private int rounding = 0;
    /** 垂直渐变顶部/底部色；任一非 null 即启用渐变背景（覆盖实色）。 */
    private StateValue<Integer> gradTop;
    private StateValue<Integer> gradBottom;
    /** 平滑过渡：bg/border 色随状态缓动而非瞬切。 */
    private boolean smooth = false;
    private AnimatedColor bgAnim;
    private AnimatedColor borderAnim;

    /** 悬停/按下时叠加的渐变高光顶部色（0 表示不画）。对应旧库 AssemblySlotRenderer / SidePanel 的「悬停渐变提亮」。 */
    private int hoverGlow = 0;
    private int pressedGlow = 0;

    private ColorRenderer() {}

    // ------------------------------------------------------------------
    // 构造
    // ------------------------------------------------------------------

    /** 纯背景，无边框。 */
    public static ColorRenderer of(int backgroundArgb) {
        ColorRenderer r = new ColorRenderer();
        r.background = StateValue.of(backgroundArgb);
        return r;
    }

    /** 背景 + 全边框。 */
    public static ColorRenderer of(int backgroundArgb, int borderArgb) {
        ColorRenderer r = of(backgroundArgb);
        r.border = StateValue.of(borderArgb);
        r.borderStyle = BorderStyle.FULL;
        return r;
    }

    /** 只画边框，背景透明。 */
    public static ColorRenderer borderOnly(int borderArgb) {
        return of(0, borderArgb);
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public ColorRenderer background(int argb)         { background.normal(argb);   return this; }
    public ColorRenderer backgroundHover(int argb)    { background.hovered(argb);  return this; }
    public ColorRenderer backgroundActive(int argb)   { background.active(argb);   return this; }
    public ColorRenderer backgroundPressed(int argb)  { background.pressed(argb);  return this; }
    public ColorRenderer backgroundDisabled(int argb) { background.disabled(argb); return this; }

    public ColorRenderer border(int argb)         { border.normal(argb);   ensureBorderVisible(); return this; }
    public ColorRenderer borderHover(int argb)    { border.hovered(argb);  ensureBorderVisible(); return this; }
    public ColorRenderer borderActive(int argb)   { border.active(argb);   ensureBorderVisible(); return this; }
    public ColorRenderer borderDisabled(int argb) { border.disabled(argb); ensureBorderVisible(); return this; }

    public ColorRenderer borderStyle(BorderStyle style) { this.borderStyle = style; return this; }

    /** 四角 1px 点缀（旧 renderBorderBox 的角点效果）。 */
    public ColorRenderer cornerDots(int argb) { this.cornerDotColor = argb; return this; }

    /** 圆角半径（px）；0 表示直角。与边框/背景同时生效。 */
    public ColorRenderer rounding(int r) { this.rounding = Math.max(0, r); return this; }

    /** 开启 bg/border 色随状态平滑缓动（hover/active/disabled 不再瞬切）。 */
    public ColorRenderer smooth(boolean on) {
        this.smooth = on;
        if (on) {
            bgAnim = new AnimatedColor(safe(background.base()));
            borderAnim = new AnimatedColor(safe(border.base()));
        }
        return this;
    }

    /**
     * 悬停时叠加一层「顶亮 → 透底」的垂直渐变高光，重现旧库列表项 / 槽位悬停时的
     * 渐变提亮（旧 {@code fillGradient(x,y,x+w,y+h, 0x33101B2E, 0x08000000)}）。
     * 不传 / 传 0 即关闭。
     */
    public ColorRenderer hoverGradient(int topArgb) { this.hoverGlow = topArgb; return this; }

    /** 按下时叠加的渐变高光（顶部色），不传 / 传 0 即关闭。 */
    public ColorRenderer pressedGradient(int topArgb) { this.pressedGlow = topArgb; return this; }

    /**
     * 常态垂直渐变（顶部 → 底部），覆盖实色背景。
     * 任一参数为 0 即关闭渐变恢复实色；与 {@link #rounding(int)} 同时使用时，
     * 渐变+圆角会退化为「圆角顶色实底」（见 {@link #render}），皮肤层需自行取舍。
     */
    public ColorRenderer gradient(int topArgb, int bottomArgb) {
        if (topArgb == 0 || bottomArgb == 0) {
            gradTop = null;
            gradBottom = null;
            return this;
        }
        gradTop().normal(topArgb);
        gradBottom().normal(bottomArgb);
        return this;
    }

    private StateValue<Integer> gradTop() {
        if (gradTop == null) gradTop = StateValue.of(0);
        return gradTop;
    }

    private StateValue<Integer> gradBottom() {
        if (gradBottom == null) gradBottom = StateValue.of(0);
        return gradBottom;
    }

    private static int safe(Integer v) { return v == null ? 0 : v; }

    private void ensureBorderVisible() {
        if (borderStyle == BorderStyle.NONE) borderStyle = BorderStyle.FULL;
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        VisualState st = ctx.state();
        int r = rounding;
        float alpha = ctx.alpha();

        int bgTarget = background.get(st);
        int borderTarget = border.get(st);
        // 平滑过渡：render 里朝目标缓动（每帧一次），tick() 无需另做
        int bg = smooth ? bgAnim.tick(bgTarget, 0.28F) : bgTarget;
        int border = smooth ? borderAnim.tick(borderTarget, 0.28F) : borderTarget;
        // 动画透明度：alpha<1 时把绘制颜色整体淡出（alpha=1 时与旧路径完全一致，零回归）
        int bgDraw = alpha < 1F ? fade(bg, alpha) : bg;
        int borderDraw = alpha < 1F ? fade(border, alpha) : border;

        // 背景：渐变优先，否则实色；圆角走 roundRect
        if (gradTop != null) {
            int gt = gradTop.get(st), gb = gradBottom.get(st);
            if (alpha < 1F) { gt = fade(gt, alpha); gb = fade(gb, alpha); }
            if (r > 0) Draw.roundRect(g, x, y, w, h, r, gt);   // 渐变+圆角退化为圆角顶色，避免边角瑕疵
            else Draw.gradientV(g, x, y, w, h, gt, gb);
        } else {
            paintBox(g, x, y, w, h, r, bgDraw);
        }

        // 边框
        if (borderStyle == BorderStyle.FULL) {
            if (r > 0) {
                Draw.roundRect(g, x, y, w, h, r, borderDraw);
                // 用背景把内圈抠出 1px 环
                if (gradTop != null) {
                    int gt = gradTop.get(st);
                    if (alpha < 1F) gt = fade(gt, alpha);
                    Draw.roundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), gt);
                } else {
                    paintBox(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), bgDraw);
                }
            } else {
                Draw.outline(g, x, y, w, h, borderDraw);
            }
        } else if (borderStyle == BorderStyle.BRACKETS) {
            Draw.cornerBrackets(g, x, y, w, h, borderDraw);
        }

        if (r == 0 && cornerDotColor != 0) {
            Draw.cornerDots(g, x, y, w, h, alpha < 1F ? fade(cornerDotColor, alpha) : cornerDotColor);
        }

        // 悬停 / 按下渐变高光：旧库招牌式 hover 提亮（顶亮 → 透底）
        int glow = st.pressed() ? pressedGlow : (st.hovered() ? hoverGlow : 0);
        if (glow != 0 && w > 0 && h > 0) {
            int inset = r > 0 ? 1 : 0; // 圆角时内缩 1px，避免高光溢出圆角
            int glowTop = alpha < 1F ? fade(glow, alpha) : glow;
            Draw.gradientV(g, x + inset, y + inset, w - inset * 2, h - inset * 2,
                    glowTop, (glowTop & 0x00FFFFFF) | 0x10000000);
        }
    }

    /** 把已烘焙的 alpha 再乘一个 0~1 的淡出系数。 */
    private static int fade(int argb, float a) {
        if (a >= 1F) return argb;
        int baked = (argb >>> 24) & 0xFF;
        int na = (int) (baked * Math.max(0F, Math.min(1F, a)));
        return (argb & 0x00FFFFFF) | (na << 24);
    }

    private void paintBox(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int argb) {
        if (r > 0) Draw.roundRect(g, x, y, w, h, r, argb);
        else Draw.fill(g, x, y, w, h, argb);
    }

    @Override
    public boolean isAnimated() { return smooth; }

    @Override
    public Renderer copy() {
        ColorRenderer c = new ColorRenderer();
        c.background = background.copy();
        c.border = border.copy();
        c.borderStyle = borderStyle;
        c.cornerDotColor = cornerDotColor;
        c.rounding = rounding;
        c.gradTop = gradTop == null ? null : gradTop.copy();
        c.gradBottom = gradBottom == null ? null : gradBottom.copy();
        c.smooth = smooth;
        c.hoverGlow = hoverGlow;
        c.pressedGlow = pressedGlow;
        if (smooth) {
            c.bgAnim = new AnimatedColor(bgAnim == null ? 0 : bgAnim.current);
            c.borderAnim = new AnimatedColor(borderAnim == null ? 0 : borderAnim.current);
        }
        return c;
    }

    /** 当前显示色缓动持有器：每帧朝目标插值。 */
    private static final class AnimatedColor {
        int current;
        AnimatedColor(int c) { current = c; }
        int tick(int target, float rate) {
            current = Draw.blend(current, target, rate);
            return current;
        }
    }
}
