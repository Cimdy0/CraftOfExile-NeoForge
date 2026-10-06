package com.cimdy.coe.ui.widget.compose;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.bar.Slider;
import com.cimdy.coe.ui.widget.component.assoc.Binding;
import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.LabelPolicy;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/**
 * 带标签的数值滑杆：左边固定宽度的标题，右边一条可拖动 {@link Slider}。
 *
 * <p>把 {@code StockScreen} 里那个「标题节点 + 交互滑杆」的 {@code configRow} 收成一个组件。
 * 仍是复合组件、数据无关：标签文案与取值范围都由调用方给。
 *
 * <p>内部把 {@link Slider} 的值域锁死成归一化 {@code [0,1]}，真实取值 {@code [min,max]} 由本组件
 * 在「真实值 ↔ 归一化进度」之间做映射。这样做让 {@link #logScale()}（对数刻度）能就地生效，
 * 而不必给 {@link Slider} 加坐标变换逻辑：
 *
 * <pre>{@code
 * LabeledSlider row = new LabeledSlider()
 *         .label(Component.translatable("hold.stock.coe.priority"))
 *         .range(1, 1000).value(50)
 *         .logScale()                       // 大跨度量程下左半段不再挤成一条
 *         .fill(0x80ADD8E6)
 *         .onChange(v -> { priority = (int) v; send(); });
 * }</pre>
 *
 * 要精确离散取值就叠加 {@link #withStepButtons()}，在两端加 {@code −/+}，长按也能连发：
 *
 * <pre>{@code
 * new LabeledSlider().label(Component.literal("数量"))
 *         .range(1, 64).step(1)
 *         .withStepButtons()                // [−] 标签 [====滑杆====] [+]
 *         .onChange(v -> setCount((int) v));
 * }</pre>
 */
public class LabeledSlider extends Node {

    private static final int LABEL_WIDTH = 58;

    private final TextRendererI labelText;
    private final Slider slider;
    private final List<Binding> bindings = new ArrayList<>();

    /** 真实量程与当前值。内部 slider 一律在归一化 [0,1] 上工作。 */
    private double rmin = 0, rmax = 1;
    private final Property<Double> realValue = new SimpleObservable<>(0.0);

    /** 对数刻度：真实值 ↔ 进度走 log 映射（要求正量程，否则自动退化成线性）。 */
    private boolean log = false;

    /** 步进网格；>0 时取值吸附到网格、按钮按此步进退。 */
    private double step = 0;

    /** 是否已挂 ± 按钮。 */
    private boolean withStep = false;
    private Button minusBtn, plusBtn;

    /** 外部值变化回调（用户拖动 / 点按钮都会触发，程序化 setValue 不触发）。 */
    private DoubleConsumer change;

    /** 防止程序化 setReal 经 realValue 监听回灌 onChange 造成重复 fire。 */
    private boolean suppressChange = false;

    private Component label = Component.empty();

    public LabeledSlider() {
        this.id = "labeled-slider";
        row();
        flex.height = 10;
        flex.alignItems = AlignItems.CENTER;

        Node labelNode = new Node();
        labelNode.id = "slider-label";
        labelNode.flex.size(LABEL_WIDTH, 10);
        labelText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE);
        labelNode.with(labelText);

        // 内部 slider 恒为归一化 [0,1]，真实取值由本组件映射。
        slider = new Slider("labeled-slider");
        slider.range(0.0, 1.0);
        slider.flex.grow = 1;
        add(labelNode);
        add(slider);

        // 真实值 ↔ 归一化进度 双向绑定（自带重入锁，无回环风险）
        bindings.add(Binding.bindBidirectional(realValue, slider.value(), this::toNorm, this::fromNorm));
        realValue.addListener((o, n) -> {
            refreshButtons();
            if (!suppressChange && change != null) change.accept(n);
        });
    }

    // ------------------------------------------------------------------
    // 真实值 ↔ 归一化进度
    // ------------------------------------------------------------------

    private double toNorm(double real) {
        if (log && rmin > 0 && rmax > 0 && rmax != rmin) {
            double lo = Math.min(rmin, rmax), hi = Math.max(rmin, rmax);
            double v = Math.max(lo, Math.min(hi, real));
            return Math.log(v / lo) / Math.log(hi / lo);
        }
        double span = rmax - rmin;
        return span == 0 ? 0 : (real - rmin) / span;
    }

    private double fromNorm(double n) {
        n = Math.max(0, Math.min(1, n));
        double real;
        if (log && rmin > 0 && rmax > 0 && rmax != rmin) {
            double lo = Math.min(rmin, rmax), hi = Math.max(rmin, rmax);
            real = lo * Math.pow(hi / lo, n);
        } else {
            real = rmin + n * (rmax - rmin);
        }
        // 与旧 onBarChange 一致：夹回量程 + 步进吸附
        double lo = Math.min(rmin, rmax), hi = Math.max(rmin, rmax);
        real = Math.max(lo, Math.min(hi, real));
        if (step > 0) {
            real = lo + Math.round((real - lo) / step) * step;
            real = Math.max(lo, Math.min(hi, real));
        }
        return real;
    }

    // ------------------------------------------------------------------
    // 取值
    // ------------------------------------------------------------------

    public LabeledSlider range(double min, double max) {
        rmin = min;
        rmax = max;
        setReal(realValue.get(), false);
        reproject();
        return this;
    }

    /** 设值（吸附到 step 网格，若已开）。不触发回调 —— 用于初始化 / 程序化复位。 */
    public LabeledSlider value(double v) {
        setReal(v, false);
        return this;
    }

    /** 当前真实值。 */
    public double value() { return realValue.get(); }

    public double min() { return rmin; }

    public double max() { return rmax; }

    public LabeledSlider onChange(DoubleConsumer c) {
        this.change = c;
        return this;
    }

    /** 主进度色（转发给内部 slider）。 */
    public LabeledSlider fill(int argb) {
        slider.fill(argb);
        return this;
    }

    public LabeledSlider label(Component c) {
        label = (c == null) ? Component.empty() : c;
        labelText.setLine(0, label);
        return this;
    }

    /** 两端数值标签，按真实量程标注。 */
    public LabeledSlider labels(LabelPolicy policy) {
        slider.labels(rmin, rmax, policy);
        return this;
    }

    // ------------------------------------------------------------------
    // 对数刻度 / 步进按钮
    // ------------------------------------------------------------------

    public LabeledSlider logScale() { return logScale(true); }

    public LabeledSlider logScale(boolean b) {
        this.log = b;
        setReal(realValue.get(), false);
        reproject();
        return this;
    }

    /** 步进网格；>0 时取值吸附到网格、按钮按此步进退（默认 0 = 连续）。 */
    public LabeledSlider step(double s) {
        this.step = s > 0 ? s : 0;
        setReal(realValue.get(), false);
        reproject();
        return this;
    }

    /** 在两端挂 {@code −/+}，按 step（未设则取量程 1/10）精确进退，到边界自动禁用。 */
    public LabeledSlider withStepButtons() { return withStepButtons(0); }

    public LabeledSlider withStepButtons(double step) {
        this.step = step > 0 ? step : this.step;
        this.withStep = true;
        ensureStepButtons();
        refreshButtons();
        return this;
    }

    // ------------------------------------------------------------------
    // 内部
    // ------------------------------------------------------------------

    private void setReal(double real, boolean fire) {
        double lo = Math.min(rmin, rmax), hi = Math.max(rmin, rmax);
        double next = Math.max(lo, Math.min(hi, real));
        if (step > 0) {
            next = lo + Math.round((next - lo) / step) * step;
            next = Math.max(lo, Math.min(hi, next));
        }
        boolean changed = next != realValue.get();
        // 经绑定回写 slider 位置；suppressChange 拦掉回灌的 onChange
        suppressChange = true;
        try {
            realValue.set(next);
        } finally {
            suppressChange = false;
        }
        refreshButtons();
        if (changed && fire && change != null) change.accept(next);
    }

    /**
     * 量程 / 对数 / 步进变化后，按新映射把当前真实值重新投影到滑块位置。
     * 这些情况不改变真实值本身，故 {@link #setReal} 不会经绑定推动滑块，
     * 必须显式重投影一次（用 suppressChange 拦掉回灌的 onChange）。
     */
    private void reproject() {
        suppressChange = true;
        try {
            slider.value().set(toNorm(realValue.get()));
        } finally {
            suppressChange = false;
        }
    }

    private void ensureStepButtons() {
        if (minusBtn != null) return;
        minusBtn = Button.of(Component.literal("-")).styled();
        plusBtn = Button.of(Component.literal("+")).styled();
        minusBtn.flex.size(14, 14);
        plusBtn.flex.size(14, 14);
        minusBtn.clickSound(true).onClick(() -> bump(-1));
        plusBtn.clickSound(true).onClick(() -> bump(+1));
        children.add(0, minusBtn);
        minusBtn.parent = this;
        children.add(plusBtn);
        plusBtn.parent = this;
    }

    private void bump(int dir) {
        double delta = step > 0 ? step : (rmax - rmin) / 10.0;
        setReal(realValue.get() + dir * delta, true);
    }

    private void refreshButtons() {
        if (minusBtn == null) return;
        double lo = Math.min(rmin, rmax), hi = Math.max(rmin, rmax);
    minusBtn.disabled(realValue.get() <= lo);
    plusBtn.disabled(realValue.get() >= hi);
    }

    /** 取内部滑杆做更细调整（注意其内部是归一化 [0,1] 口径）。 */
    public Slider bar() { return slider; }

    // ------------------------------------------------------------------
    // 拷贝：复合组件必须重建并重链内部字段，不能直接用 Node.copy()
    // ------------------------------------------------------------------

    @Override
    public LabeledSlider copy() {
        LabeledSlider c = new LabeledSlider();
        c.flex.copyFrom(flex);
        c.id = id;
        c.visible = visible;
        c.state = state;
        c.rmin = rmin;
        c.rmax = rmax;
        c.log = log;
        c.step = step;
        c.withStep = withStep;
        c.change = change;
        c.label = label;
        c.label(label);
        if (withStep) c.ensureStepButtons();
        c.setReal(realValue.get(), false);
        c.refreshButtons();
        return c;
    }
}
