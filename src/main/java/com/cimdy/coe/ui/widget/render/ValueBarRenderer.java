package com.cimdy.coe.ui.widget.render;
import com.cimdy.coe.ui.widget.anim.HoverLerp;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.ui.widget.render.LabelPolicy;
import com.cimdy.coe.ui.widget.render.Orientation;
import com.cimdy.coe.ui.widget.render.Segment;
import com.cimdy.coe.ui.widget.render.Tick;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;

/**
 * 数值条渲染器：在一条轨道上叠加若干色带与刻度，两端可选显示范围标签。
 *
 * <p>旧 {@code ValueBarRenderer} 有两处硬伤，这里一并解决：
 * <ul>
 *   <li>它 {@code import com.cimdy.coe.ui.widget.AdaptiveValueBar}，把渲染器和某个具体组件
 *       死绑在一起 —— 渲染器反过来依赖组件，等于两者谁都不能单独复用。现在色带/刻度是
 *       lib 自己的 {@link Segment} / {@link Tick}，纯归一化数值，谁都能用；</li>
 *   <li>它 {@code import com.cimdy.coe.client.InputState} 去查「有没有按 Shift」，一个 UI 库
 *       直接读模组的按键状态。现在换成 {@link LabelPolicy}，要什么条件由调用方给。</li>
 * </ul>
 *
 * <p>所有位置都用 {@code [0,1]} 归一化坐标表达，与真实数值范围解耦；
 * {@code min/max} 只在显示标签时用到。
 */
class ValueBarRenderer implements Renderer, ValueBarView {

    private final Supplier<List<Segment>> segments;
    private final Supplier<List<Tick>> ticks;

    private Orientation orientation = Orientation.HORIZONTAL;
    private LabelPolicy labelPolicy = LabelPolicy.NEVER;
    private DoubleFunction<String> labelFormat = v -> String.valueOf(Math.round(v));
    private double min = 0;
    private double max = 100;
    private float labelScale = 0.5F;
    private int labelColor = 0xFFFFFFFF;

    /** 整条轨道槽底色（0 表示不画），让条形在有/无数值时都看得见「轴」。 */
    private int trackColor = 0;
    /** 可拖动把手（0 表示不画）。水平条上是竖线，竖直条上是横线。 */
    private boolean handleEnabled = false;
    private int handleColor = Theme.current().accent();
    private int handleThickness = 2;
    /** 把手悬浮/按下高亮色。 */
    private int handleHoverColor = Theme.current().accentBright();
    /** 当前是否处于悬浮/拖拽（由组件每帧喂入）。 */
    private boolean handleActive = false;
    /** 把手 hover/按下发光量：常态色 → 高亮色按 120ms 缓动（计划 Slider 把手 hover 发光）。 */
    private final HoverLerp handleHover = new HoverLerp(120);
    /** 把手位置进度 [0,1]，由组件注入（与 {@link Slider#progress()} 同源）。 */
    private java.util.function.DoubleSupplier progressSupplier = () -> 0;

    public ValueBarRenderer(Supplier<List<Segment>> segments, Supplier<List<Tick>> ticks) {
        this.segments = segments;
        this.ticks = ticks;
    }



    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public ValueBarRenderer orientation(Orientation o) { this.orientation = o; return this; }

    public Orientation orientation() { return orientation; }

    /** 两端标签的显示策略与数值范围。 */
    public ValueBarRenderer labels(double min, double max, LabelPolicy policy) {
        this.min = min;
        this.max = max;
        this.labelPolicy = policy;
        return this;
    }

    public ValueBarRenderer labelFormat(DoubleFunction<String> f) { this.labelFormat = f; return this; }
    public ValueBarRenderer labelScale(float s) { this.labelScale = s; return this; }
    public ValueBarRenderer labelColor(int argb) { this.labelColor = argb; return this; }

    /** 整条轨道槽底色（常年可见的「轴」），让条形在数值为 0 时也不空。 */
    public ValueBarRenderer trackColor(int argb) { this.trackColor = argb; return this; }
    /** 当前轨道槽色（0 表示未设）。 */
    public int trackColor() { return trackColor; }

    /** 开启可拖动把手（落在当前进度位置上，悬浮/拖拽时高亮）。 */
    public ValueBarRenderer handle(boolean b) { this.handleEnabled = b; return this; }
    public ValueBarRenderer handleColor(int argb) { this.handleColor = argb; return this; }
    public ValueBarRenderer handleHoverColor(int argb) { this.handleHoverColor = argb; return this; }
    public ValueBarRenderer handleThickness(int px) { this.handleThickness = Math.max(1, px); return this; }

    public boolean handleEnabled() { return handleEnabled; }
    public int handleColor() { return handleColor; }
    public int handleHoverColor() { return handleHoverColor; }
    public int handleThickness() { return handleThickness; }

    /** 由组件每帧喂入把手是否处于悬浮/拖拽（决定是否高亮）。 */
    public ValueBarRenderer handleActive(boolean b) { this.handleActive = b; return this; }

    /** 注入把手的进度来源 [0,1]。 */
    public ValueBarRenderer progressSupplier(java.util.function.DoubleSupplier p) { this.progressSupplier = p; return this; }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (w <= 0 || h <= 0) return;

        boolean horizontal = orientation == Orientation.HORIZONTAL;
        int trackLength = horizontal ? w : h;

        // 轨道槽：常年可见的「轴」，让条形在数值为 0 时也不显空
        if (trackColor != 0) {
            if (horizontal) Draw.fill(g, x, y, w, h, trackColor);
            else            Draw.fill(g, x, y, w, h, trackColor);
        }

        List<Segment> segs = segments.get();
        if (segs != null) {
            for (Segment s : segs) {
                int a = (int) Math.round(clamp01(Math.min(s.from(), s.to())) * trackLength);
                int b = (int) Math.round(clamp01(Math.max(s.from(), s.to())) * trackLength);
                if (b <= a) continue;
                if (horizontal) {
                    Draw.fill(g, x + a, y, b - a, h, s.color());
                } else {
                    Draw.fill(g, x, y + a, w, b - a, s.color());
                }
            }
        }

        List<Tick> tks = ticks.get();
        if (tks != null) {
            for (Tick t : tks) {
                int p = (int) Math.round(clamp01(t.at()) * trackLength);
                int thick = Math.max(1, t.thickness());
                if (horizontal) {
                    Draw.fill(g, x + p - thick / 2, y, thick, h, t.color());
                } else {
                    Draw.fill(g, x, y + p - thick / 2, w, thick, t.color());
                }
            }
        }

        // 可拖动把手：落在进度位置，悬浮/拖拽时高亮 —— 旧 AdaptiveValueBar.Axis 的「轴」
        // hover 发光由 HoverLerp 在常态色与高亮色之间 120ms 缓动（非瞬切）
        if (handleEnabled) {
            double p = clamp01(progressSupplier.getAsDouble());
            int pos = (int) Math.round(p * (trackLength - handleThickness));
            handleHover.update(handleActive || ctx.state().hovered() || ctx.state().pressed());
            int col = handleHover.color(handleColor, handleHoverColor);
            if (horizontal) {
                Draw.fill(g, x + pos, y, handleThickness, h, col);
            } else {
                Draw.fill(g, x, y + pos, w, handleThickness, col);
            }
        }

        if (labelPolicy.show(ctx.state())) {
            renderLabels(g, x, y, w, h, horizontal);
        }
    }

    private void renderLabels(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean horizontal) {
        Component lo = Component.literal(labelFormat.apply(min));
        Component hi = Component.literal(labelFormat.apply(max));
        int lineH = Math.round(Draw.lineHeight() * labelScale);

        if (horizontal) {
            // 贴在轨道左右两侧外缘，垂直居中
            int ty = y + (h - lineH) / 2;
            int loW = Math.round(Draw.width(lo) * labelScale);
            Draw.textScaled(g, lo, x - loW - 1, ty, labelScale, labelColor);
            Draw.textScaled(g, hi, x + w + 1, ty, labelScale, labelColor);
        } else {
            // 贴在轨道上下两端外缘，水平居中
            int cx = x + w / 2;
            Draw.centeredTextScaled(g, lo, cx, y - lineH - 1, labelScale, labelColor);
            Draw.centeredTextScaled(g, hi, cx, y + h + 1, labelScale, labelColor);
        }
    }

    private static double clamp01(Double v) {
        if (v == null) return 0;
        return Math.max(0, Math.min(1, v));
    }

    @Override
    public Renderer copy() {
        ValueBarRenderer c = new ValueBarRenderer(segments, ticks);
        c.orientation = orientation;
        c.labelPolicy = labelPolicy;
        c.labelFormat = labelFormat;
        c.min = min;
        c.max = max;
        c.labelScale = labelScale;
        c.labelColor = labelColor;
        c.trackColor = trackColor;
        c.handleEnabled = handleEnabled;
        c.handleColor = handleColor;
        c.handleHoverColor = handleHoverColor;
        c.handleThickness = handleThickness;
        c.handleActive = handleActive;
        c.progressSupplier = progressSupplier;
        return c;
    }


}
