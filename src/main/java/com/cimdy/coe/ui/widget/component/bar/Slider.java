package com.cimdy.coe.ui.widget.component.bar;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.ValueBarView;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.LabelPolicy;
import com.cimdy.coe.ui.widget.render.Orientation;
import com.cimdy.coe.ui.widget.render.Segment;
import com.cimdy.coe.ui.widget.render.Tick;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * 基础原语：轨道 + 填充 + 可拖拽把手（数值编辑）。它实现 {@link ValueEditable} 能力，
 * 是「功能性组件」思想里「基础原语按能力命名与定义」的样板——复合体（{@link Bar} / ScrollBar）
 * 只认 {@code ValueEditable}，因此同一个 {@code Slider} 既能被当「数值条」也能被当「滚动条」复用，
 * 原语与名字无关，可跨复合体复用。
 *
 * <p>它对外只暴露能力要求的字段：
 * <ul>
 *   <li>{@link #value()} —— 当前数值（落在 {@link #range()} 之内）；</li>
 *   <li>{@link #range()} —— 当前取值范围（{@link Range}）。</li>
 * </ul>
 * 渲染职责由本原语直接持有的 {@link ValueBarRenderer} 完成：绘制算法与旧 {@code ValueBar} 逐字节一致，
 * 只是「数据从哪来」从内部 {@code ValueBar} 改为本类自身字段——这正是把 {@code ValueBar} 折叠进
 * {@code Slider} 后的形态：bar 包唯一的数值条能力原语，具体渲染实现不再作为公开类暴露。
 *
 * <p>颜色全部来自 {@link Theme}：轨道用 {@link Theme#progressTrack()}（凹槽再落到
 * {@link Theme#current()#progressTrackColor()}），填充用 {@link Theme#current()#progressFill()}，
 * 把手用 {@link Theme#current()#accent()}。零内联取色，跟随当前风格预设。
 *
 * <p>本原语刻意不负责「联动谁 / 代表什么语义」—— 那属于复合体用
 * {@link com.cimdy.coe.ui.widget.component.assoc.Binding} 接线的事。把「长得像一条可拖的条」与
 * 「和谁联动 / 代表数值还是滚动偏移」解耦，正是它能被复用的关键。
 */
public class Slider extends AnimatedNode implements ValueEditable {

    // —— 渲染器：绘制算法与旧 ValueBar 逐字节一致，数据直接读本类字段 ——
    private final ValueBarView bar;

    // —— 数值状态（原 ValueBar 的 value / range / step，现为唯一数据源）——
    private final SimpleObservable<Double> value = new SimpleObservable<>(0.0);
    private final SimpleObservable<Range> range = new SimpleObservable<>(new Range(0.0, 100.0));
    /** 步进；<=0 表示连续。 */
    private double step = 0;

    // —— 外观状态（原 ValueBar 字段，经同名方法直接读写）——
    private int fillColor = Theme.current().progressFill();
    private boolean interactive = false;
    /** 除主进度外，调用方追加的静态色带与刻度。 */
    private final List<Segment> extraSegments = new ArrayList<>();
    private final List<Tick> ticks = new ArrayList<>();

    /** 拖拽中标记（鼠标按下到松开之间）。 */
    private boolean dragging;

    public Slider(String id) {
        this.id = "slider:" + id;
        flex.height = 6;

        bar = Renderers.valueBar(this::buildSegments, () -> ticks);
        bar.progressSupplier(this::progress);
        with(bar);

        range(0.0, 100.0);                                    // 初始量程
        interactive(true);                                    // 默认开启交互（把手 + 默认轨道槽）
        // —— 全部颜色来自 Theme ——
        bar.trackColor(Theme.current().progressTrackColor()); // 凹槽底色（与整条轨道一致）
        track(Theme.progressTrack());                         // 整条轨道皮肤
        fill(Theme.current().progressFill());                 // 填充（强调色）
        bar.handleColor(Theme.current().accent());            // 把手（强调色）

        column();
    }

    // ------------------------------------------------------------------
    // ValueEditable 能力实现
    // ------------------------------------------------------------------

    /** 当前值可绑定字段（落在 {@link #range()} 之内）。 */
    @Override
    public Property<Double> value() {
        return value;
    }

    /** 当前取值范围可绑定字段。 */
    @Override
    public Property<Range> range() {
        return range;
    }

    /** 切到竖直方向。 */
    @Override
    public void vertical() {
        bar.orientation(Orientation.VERTICAL);
    }

    /** 条厚（像素）。 */
    @Override
    public void thickness(int px) {
        flex.height = Math.max(2, px);
    }

    // ------------------------------------------------------------------
    // 便利设定 API（非能力部分）
    // ------------------------------------------------------------------


    /** 主进度色。设 0 表示不画主进度（纯多段条模式）。 */
    public Slider fill(int argb) {
        this.fillColor = argb;
        return this;
    }


    // ------------------------------------------------------------------
    // 取值 / 外观 API（原 ValueBar 同名方法，现直接读写本类字段，无转发 / 无双写）
    // ------------------------------------------------------------------

    /** 设取值范围（双参重载）。 */
    public Slider range(double min, double max) {
        range.set(new Range(min, max));
        value.set(clampValue(value.get()));
        return this;
    }

    /** 设取值范围（{@link Range} 重载）。 */
    public Slider range(Range r) {
        range.set(r);
        value.set(clampValue(value.get()));
        return this;
    }

    /** 当前最小值。 */
    public double min() { return range.get().min(); }

    /** 当前最大值。 */
    public double max() { return range.get().max(); }

    /** 设当前值（含夹值 / 步进吸附）。 */
    public Slider value(double v) {
        value.set(clampValue(v));
        return this;
    }

    /** 当前归一化进度 [0,1]。 */
    public double progress() {
        Range r = range.get();
        double span = r.max() - r.min();
        return span <= 0 ? 0 : (value.get() - r.min()) / span;
    }

    /** 按归一化进度 [0,1] 设值。 */
    public Slider progress(double p) {
        Range r = range.get();
        return value(r.min() + clamp01(p) * (r.max() - r.min()));
    }

    /** 步进网格（>0 时取值吸附到网格）。 */
    public Slider step(double s) {
        this.step = s;
        return this;
    }

    /** 值变化回调。 */
    public Slider onChange(DoubleConsumer c) {
        value.addListener((o, n) -> c.accept(n));
        return this;
    }


    /** 轨道底色（int 重载，插在最底层）。 */
    public Slider track(int argb) {
        visuals.add(0, Renderers.color(argb));
        return this;
    }

    /** 轨道底色（Renderer 重载，插在最底层）。 */
    public Slider track(Renderer r) {
        visuals.add(0, r);
        return this;
    }

    /** 追加一段静态色带（归一化区间）。 */
    public Slider segment(double from, double to, int argb) {
        extraSegments.add(new Segment(from, to, argb));
        return this;
    }

    /** 追加一条刻度线（归一化位置）。 */
    public Slider tick(double at, int argb, int thickness) {
        ticks.add(new Tick(at, argb, thickness));
        return this;
    }

    /** 两端数值标签（指定真实量程）。 */
    public Slider labels(double min, double max, LabelPolicy policy) {
        bar.labels(min, max, policy);
        return this;
    }

    /** 两端数值标签（取当前量程）。 */
    public Slider labels(LabelPolicy policy) {
        return labels(range.get().min(), range.get().max(), policy);
    }

    /** 数值标签格式。 */
    public Slider labelFormat(DoubleFunction<String> f) { bar.labelFormat(f); return this; }

    /** 整条轨道槽底色（0 表示不画）。 */
    public Slider trackColor(int argb) { bar.trackColor(argb); return this; }

    /** 可拖动把手开关。 */
    public Slider showHandle(boolean b) { bar.handle(b); return this; }

    /** 把手颜色。 */
    public Slider handleColor(int argb) { bar.handleColor(argb); return this; }

    /** 把手粗细。 */
    public Slider handleThickness(int px) { bar.handleThickness(px); return this; }

    /**
     * 交互开关：开启时默认露出「轴」（暗色轨道槽 + 可拖动把手），让 0 值也看得见、可抓。
     */
    public Slider interactive(boolean b) {
        this.interactive = b;
        if (b) {
            if (bar.trackColor() == 0) bar.trackColor(Theme.current().track());
            bar.handle(true);
        }
        return this;
    }

    /** 主进度段 + 调用方追加的静态色带。fillColor == 0 时只画追加的色带（纯多段条模式）。 */
    private List<Segment> buildSegments() {
        if (fillColor == 0) return extraSegments;
        List<Segment> out = new ArrayList<>(extraSegments.size() + 1);
        out.add(new Segment(0, progress(), fillColor));
        out.addAll(extraSegments);
        return out;
    }

    // ------------------------------------------------------------------
    // 交互（滑杆模式）
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (!interactive || button != 0) return false;
        dragging = true;
        bar.handleActive(true);
        focus();
        applyPointer(lx, ly);
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        applyPointer(lx, ly);
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!dragging) return false;
        dragging = false;
        bar.handleActive(false);
        thumbBounce();
        return true;
    }

    /**
     * 拖动结束（释放）时的把手强调回弹（附录 F 缺口 1：Slider thumb 回弹）。
     *
     * <p>时长读 {@link AnimRegistry#ms(AnimRegistry.Trigger)} VALUE 令牌（默认 200ms），
     * 缩放 1 → 1.08 → 1，回程走 {@link com.cimdy.coe.ui.widget.anim.Easing#EASE_OUT_BACK}。
     * 拖动中实时跟手、不挂动画；hover 提亮沿用 {@code ValueBarRenderer} 既有 HoverLerp(120)，
     * 此处不重复实现。纯视觉缩放（AnimatedNode 变换），不改命中区与事件链路；
     * 全局关动画 / reduced-motion 时经 Animator 通道瞬时落终帧，零回归。
     * {@code LabeledSlider} 内部组合本类，自动复用此回弹，不重复接线。
     */
    private void thumbBounce() {
        pulse(0.08F, AnimRegistry.ms(AnimRegistry.Trigger.VALUE));
    }

    private void applyPointer(double lx, double ly) {
        boolean vertical = bar.orientation() == Orientation.VERTICAL;
        int track = vertical ? height : width;
        if (track <= 0) return;
        progress((vertical ? ly : lx) / track);
    }

    /**
     * 键盘微调滑杆：纵向认上/下、横向认左/右；PageUp/Down 大步进退、Home/End 跳到量程两端。
     * 仅在交互模式且获得焦点（点击过滑杆）时生效。step>0 时按步进退并吸附到网格，
     * 否则按量程的 5% / 20% 平滑进退。
     */
    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!interactive || !isFocused()) return false;
        boolean vertical = bar.orientation() == Orientation.VERTICAL;
        Range r = range.get();
        double span = Math.max(r.min(), r.max()) - Math.min(r.min(), r.max());
        double unit = step > 0 ? step : span * 0.05;
        double page = step > 0 ? step * 10 : span * 0.2;
        double cur = value.get();
        double next;
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:    next = vertical ? cur - unit : cur; break;
            case GLFW.GLFW_KEY_DOWN:  next = vertical ? cur + unit : cur; break;
            case GLFW.GLFW_KEY_LEFT:  next = vertical ? cur : cur - unit; break;
            case GLFW.GLFW_KEY_RIGHT: next = vertical ? cur : cur + unit; break;
            case GLFW.GLFW_KEY_PAGE_UP:   next = cur - page; break;
            case GLFW.GLFW_KEY_PAGE_DOWN: next = cur + page; break;
            case GLFW.GLFW_KEY_HOME: value(r.min()); return true;
            case GLFW.GLFW_KEY_END:  value(r.max()); return true;
            default: return false;
        }
        if (next == cur) return false;
        value(next);
        return true;
    }

    private static double clamp01(double v) {
        return Double.isNaN(v) ? 0 : Math.max(0, Math.min(1, v));
    }

    private double clampValue(double v) {
        return range.get().snap(v, step);
    }

    /** 克隆：复刻值与量程；外观层由原屏幕在拷贝体上重新链式设色。 */
    @Override
    public Slider copy() {
        String sid = id.startsWith("slider:") ? id.substring("slider:".length()) : id;
        Slider s = new Slider(sid);
        s.value.set(this.value.get());
        s.range.set(this.range.get());
        return s;
    }

    /** 工厂：等价于旧 {@code ValueBar.of}，返回 Slider 以便链式调用。 */
    public static Slider of(double min, double max) {
        Slider s = new Slider("of");
        s.range(min, max);
        return s;
    }


}
