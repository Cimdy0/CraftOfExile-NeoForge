package com.cimdy.coe.ui.widget.component.list;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.ScrollThumbI;
import com.cimdy.coe.ui.widget.render.ColorRendererI;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.render.Orientation;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/**
 * 滚动条：绑定一个 {@link Scrollable}，显示滑块并支持拖拽 / 点击轨道跳转。
 *
 * <p>它<b>不存任何滚动状态</b> —— 位置每帧从目标容器读，拖拽时写回目标容器。
 * 旧库把「滚动值」同时存在滚动条和容器两处再靠 NBT 数据总线同步，
 * 是滚动位置对不上的主要来源。这里滚动量只有一个家：{@link Scrollable}。
 *
 * <p>滑块长度与位置的计算，与 {@link ScrollThumbRenderer} 共用同一个静态公式，
 * 保证「看到的」与「能拖的」严格一致。
 */
public class ScrollBar extends Node {

    public enum Orientation { VERTICAL, HORIZONTAL }

    private final Scrollable target;
    private final Orientation orientation;

    private int minThumbLength = 8;
    private int fixedThumbLength = 0;
    /** 无内容可滚时是否隐藏滑块（轨道仍占位）。 */
    private boolean hideWhenFull = true;

    /** 方向键每次微调的像素数（滚轮/翻页之外更细的步进）。 */
    private int lineStep = 16;

    private boolean dragging;
    /** 按下点相对滑块顶端的偏移，避免拖拽时滑块「跳」到鼠标中心。 */
    private int grabOffset;

    // ------------------------------------------------------------------
    // auto-hide（附录 D.1「Scrollable scrollbar auto-hide」，外包裹层裁决）
    //
    // Scrollable 核心禁动，故 auto-hide 落在本组件（非核心）自身渲染 alpha 上：
    // 静止 1.5s 后整条褪到 DIM_ALPHA，滚动 / 悬停 / 拖拽时淡回 1.0。只改渲染 alpha，
    // 不碰 Scrollable、不改滚动行为 / 命中。动画关闭 / reduced-motion 时恒 alpha=1（现状常显）。
    // ------------------------------------------------------------------

    /** 静止多久后开始褪隐（ms）。 */
    private static final long HIDE_AFTER_MS = 1500L;
    /** 褪隐后的目标 alpha（拖柄仍可辨 / 可点，不彻底隐藏）。 */
    private static final float DIM_ALPHA = 0.3F;

    /** 显隐补间（alpha），初始即静止终帧 1.0（预算中性）。 */
    private final Animator vis = new Animator(1F, 1F,
            AnimRegistry.ms(AnimRegistry.Trigger.VALUE), Easing.EASE_OUT_CUBIC);
    /** 最近一次「活动」（滚动 / 悬停 / 拖拽）时钟戳。 */
    private long lastActive = Util.getMillis();
    /** 上一帧观测到的滚动量，用于发现滚动活动（NaN = 首帧）。 */
    private double lastOffset = Double.NaN;

    public ScrollBar(Scrollable target, Orientation orientation) {
        this.target = target;
        this.orientation = orientation;
        this.id = "scrollbar";
        if (orientation == Orientation.VERTICAL) {
            flex.width = 4;
        } else {
            flex.height = 4;
        }
    }

    public static ScrollBar vertical(Scrollable target) {
        return new ScrollBar(target, Orientation.VERTICAL);
    }

    public static ScrollBar horizontal(Scrollable target) {
        return new ScrollBar(target, Orientation.HORIZONTAL);
    }

    // ------------------------------------------------------------------
    // 外观
    // ------------------------------------------------------------------

    /** 中性默认皮肤：暗色轨道 + 亮色滑块。 */
    public ScrollBar styled() {
        return track(Theme.scrollTrack())
                .thumb(Theme.scrollThumb());
    }

    /** 轨道外观（画在整条轨道上）。 */
    public ScrollBar track(Renderer r) {
        visuals.add(0, r);
        return this;
    }

    /** 滑块外观。会被包进 {@link ScrollThumbRenderer} 自动摆位。 */
    public ScrollBar thumb(Renderer r) {
        // 拖柄 hover 发光：底层若是纯色渲染器，开启状态色缓动（常态→高亮 100~150ms 非瞬切）
        if (r instanceof ColorRendererI cr) cr.smooth(true);
        ScrollThumbI t = Renderers.scrollThumb(r, this::progress)
                .autoLength(this::visibleRatio)
                .minLength(minThumbLength)
                .orientation(orientation == Orientation.VERTICAL
                        ? com.cimdy.coe.ui.widget.render.Orientation.VERTICAL
                        : com.cimdy.coe.ui.widget.render.Orientation.HORIZONTAL);
        if (fixedThumbLength > 0) t.length(fixedThumbLength);
        visuals.add(t);
        return this;
    }

    public ScrollBar minThumbLength(int px) { this.minThumbLength = px; return this; }

    public ScrollBar fixedThumbLength(int px) { this.fixedThumbLength = px; return this; }

    public ScrollBar hideWhenFull(boolean b) { this.hideWhenFull = b; return this; }

    /** 方向键微调步长（px）。默认 16，等于滚轮一格。 */
    public ScrollBar lineStep(int px) { this.lineStep = Math.max(1, px); return this; }

    public ScrollBar thickness(int px) {
        if (orientation == Orientation.VERTICAL) flex.width = px; else flex.height = px;
        return this;
    }

    // ------------------------------------------------------------------
    // 与目标容器的桥接
    // ------------------------------------------------------------------

    private boolean vertical() { return orientation == Orientation.VERTICAL; }

    private double progress() { return vertical() ? target.progressY() : target.progressX(); }

    private void progress(double p) {
        if (vertical()) target.progressY(p); else target.progressX(p);
    }

    private double visibleRatio() {
        return vertical() ? target.visibleRatioY() : target.visibleRatioX();
    }

    private boolean scrollable() {
        return vertical() ? target.canScrollY() : target.canScrollX();
    }

    private int track() { return vertical() ? height : width; }

    private int thumbLength() {
        return Renderers.thumbLength(track(), visibleRatio(), minThumbLength, fixedThumbLength);
    }

    // ------------------------------------------------------------------
    // 交互
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0 || !scrollable()) return false;
        // 抢占焦点，使之后方向键 / Page / Home / End 能微调本条滚动
        focus();

        int track = track();
        int len = thumbLength();
        int travel = track - len;
        double pos = vertical() ? ly : lx;
        int thumbStart = travel <= 0 ? 0 : (int) Math.round(travel * progress());

        if (pos >= thumbStart && pos < thumbStart + len) {
            // 命中滑块：记住抓取点，拖动时保持相对位置
            grabOffset = (int) (pos - thumbStart);
        } else {
            // 点空轨道：滑块中心跳到点击处
            grabOffset = len / 2;
            applyDrag(pos, travel, len);
        }
        dragging = true;
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        int len = thumbLength();
        applyDrag(vertical() ? ly : lx, track() - len, len);
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }

    private void applyDrag(double pos, int travel, int len) {
        if (travel <= 0) return;
        double start = pos - grabOffset;
        progress(start / travel);
    }

    /**
     * 键盘微调：纵向用上/下、横向用左/右；PageUp/Down 翻一屏、Home/End 跳首尾。
     * 仅在获得焦点时生效（点过轨道后焦点自然落在本条），不会干扰列表里正在打字或聚焦的输入框。
     */
    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!scrollable()) return false;
        boolean v = vertical();
        int page = Math.max(lineStep, (int) ((v ? target.contentHeight() : target.contentWidth()) * 0.9));
        double delta;
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:    delta = v ? -lineStep : 0; break;
            case GLFW.GLFW_KEY_DOWN:  delta = v ?  lineStep : 0; break;
            case GLFW.GLFW_KEY_LEFT:  delta = v ? 0 : -lineStep; break;
            case GLFW.GLFW_KEY_RIGHT: delta = v ? 0 :  lineStep; break;
            case GLFW.GLFW_KEY_PAGE_UP:   delta = -page; break;
            case GLFW.GLFW_KEY_PAGE_DOWN: delta =  page; break;
            case GLFW.GLFW_KEY_HOME:
                if (v) target.scrollToY(0); else target.scrollToX(0);
                return true;
            case GLFW.GLFW_KEY_END:
                if (v) target.scrollToBottom(); else target.scrollToX(target.maxOffsetX());
                return true;
            default:
                return false;
        }
        if (delta == 0) return false;
        if (v) target.scrollByY(delta); else target.scrollByX(delta);
        return true;
    }

    // ------------------------------------------------------------------

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        if (hideWhenFull && !scrollable()) return;
        // auto-hide：动画关闭 / reduced-motion 时恒 alpha=1（现状常显）；否则按显隐补间褪隐
        float a = AnimRegistry.animationsActive() ? vis.value() : 1F;
        RenderContext c = a >= 1F ? ctx : ctx.withAlpha(a);
        // 拖拽中强制按下态，松手前滑块保持高亮
        super.renderSelf(g, dragging ? c.with(c.state().pressed(true)) : c);
    }

    @Override
    public void tick() {
        long now = Util.getMillis();
        double off = vertical() ? target.offsetY() : target.offsetX();
        // 滚动活动（offset 变化）/ 悬停 / 拖拽 任一即「唤醒」，重置静止计时
        boolean wake = dragging || state.hovered() || off != lastOffset;
        lastOffset = off;
        if (wake) lastActive = now;
        boolean wantVisible = (now - lastActive) < HIDE_AFTER_MS;
        vis.retarget(wantVisible ? 1F : DIM_ALPHA);
        vis.tick();
        super.tick();
    }

    @Override
    public Node copy() {
        ScrollBar b = new ScrollBar(target, orientation);
        copyPropsInto(b);
        b.minThumbLength = minThumbLength;
        b.fixedThumbLength = fixedThumbLength;
        b.hideWhenFull = hideWhenFull;
        b.lineStep = lineStep;
        return b;
    }
}
