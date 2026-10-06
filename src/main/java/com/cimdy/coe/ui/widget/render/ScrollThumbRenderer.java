package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.Orientation;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.DoubleSupplier;

/**
 * 滚动条滑块渲染器：在整条轨道区域内，按滚动进度把「滑块」摆到正确位置。
 *
 * <p>它本身不画滑块外观，而是把算好的滑块矩形委托给内层渲染器 —— 于是滑块可以是
 * 纯色（{@link ColorRenderer}）也可以是贴图（{@link SkinRenderer}），不必为每种外观再写一个类。
 *
 * <p>相比旧 {@code ScrollStripRenderer}：进度改为 {@link DoubleSupplier} 拉取而非外部每帧 set，
 * 并支持横向轨道与「按内容比例自动决定滑块长度」。
 */
class ScrollThumbRenderer implements Renderer, ScrollThumbI {

    private final Renderer thumb;
    /** 滚动进度 [0,1] */
    private final DoubleSupplier progress;

    private Orientation orientation = Orientation.VERTICAL;
    /** 固定滑块长度；<=0 表示按 visibleRatio 自动计算 */
    private int thumbLength = 0;
    /** 可视比例 [0,1]，用于自动计算滑块长度 */
    private DoubleSupplier visibleRatio = () -> 1.0;
    private int minThumbLength = 8;

    public ScrollThumbRenderer(Renderer thumb, DoubleSupplier progress) {
        this.thumb = thumb;
        this.progress = progress;
    }

    /** 固定滑块长度。 */
    public ScrollThumbRenderer length(int px) { this.thumbLength = px; return this; }

    /** 按可视内容比例自动决定滑块长度。 */
    public ScrollThumbRenderer autoLength(DoubleSupplier visibleRatio) {
        this.visibleRatio = visibleRatio;
        this.thumbLength = 0;
        return this;
    }

    public ScrollThumbRenderer minLength(int px) { this.minThumbLength = px; return this; }

    public ScrollThumbRenderer orientation(Orientation o) { this.orientation = o; return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        boolean vertical = orientation == Orientation.VERTICAL;
        int track = vertical ? h : w;
        if (track <= 0) return;

        int len = thumbLength(track, visibleRatio.getAsDouble(), minThumbLength, thumbLength);

        int travel = track - len;
        int offset = travel <= 0 ? 0 : (int) Math.round(travel * Draw.clamp01(progress.getAsDouble()));

        if (vertical) {
            thumb.render(g, x, y + offset, w, len, ctx);
        } else {
            thumb.render(g, x + offset, y, len, h, ctx);
        }
    }

    /**
     * 滑块长度的唯一算法。滚动条组件做拖拽命中时必须用同一份公式，
     * 否则「看到的滑块」和「能拖的滑块」会对不上，所以这里暴露成静态方法共用。
     *
     * @param track        轨道总长
     * @param visibleRatio 可视内容比例 [0,1]
     * @param minLength    滑块最小长度
     * @param fixedLength  固定长度；&lt;=0 表示按比例自动
     */
    public static int thumbLength(int track, double visibleRatio, int minLength, int fixedLength) {
        if (track <= 0) return 0;
        int len = fixedLength > 0
                ? fixedLength
                : (int) Math.round(track * Draw.clamp01(visibleRatio));
        return Math.max(Math.min(minLength, track), Math.min(track, len));
    }

    @Override
    public boolean isAnimated() { return thumb.isAnimated(); }

    @Override
    public void tick() { thumb.tick(); }

    @Override
    public Renderer copy() {
        ScrollThumbRenderer c = new ScrollThumbRenderer(thumb.copy(), progress);
        c.orientation = orientation;
        c.thumbLength = thumbLength;
        c.visibleRatio = visibleRatio;
        c.minThumbLength = minThumbLength;
        return c;
    }
}
