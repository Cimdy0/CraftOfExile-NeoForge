package com.cimdy.coe.ui.widget.component.bar;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.assoc.Association;
import com.cimdy.coe.ui.widget.component.assoc.AssociationRegistry;
import com.cimdy.coe.ui.widget.component.assoc.Binding;
import com.cimdy.coe.ui.widget.component.assoc.ChangeListener;
import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基础原语：刻度 / 范围轴。它实现 {@link ScaleCapable} 能力，是「功能性组件」思想里
 * 「基础原语按能力命名与定义」的样板——一个组件只要能给出 {@link #range()} 并支持 {@link #vertical()}，
 * 就能被任意复合体当作「刻度」使用，它与「条 / 滑块」毫无耦合。
 *
 * <p>它持有三个可绑定字段 —— 最小值 / 最大值 / 刻度数（均为 {@link Property}），
 * 并把「当前范围」对外暴露成一个具名 {@link Association}{@code <Range>}（命名 {@code scale:<id>}），
 * 供其它组件订阅（例如复合体 {@link Bar} 用它驱动内部滑块的取值量程）。
 *
 * <p>颜色全部来自 {@link Theme}：轴线与刻度用 {@link Theme#current()#borderDisabled()}，
 * 端值标签用 {@link Theme#current()#text()} —— 零内联取色，跟随当前风格预设。
 *
 * <p>本原语只负责把「范围」这一种数据讲清楚，至于范围怎么被滑块使用、怎么被另一个组件联动，
 * 交给复合体与关联系统，自己一概不管。这正是它能被 {@link Bar} / ScrollBar 这类复合体
 * 按能力选中的前提。
 */
public class Scale extends Node implements ScaleCapable {

    /** 对外暴露的范围关联；改名也能在注册表里被其它组件按名取到。 */
    private final Association<Range> rangeAssoc;

    private final SimpleObservable<Double> min = new SimpleObservable<>(0.0);
    private final SimpleObservable<Double> max = new SimpleObservable<>(100.0);
    private final SimpleObservable<Integer> ticks = new SimpleObservable<>(5);

    /** 全部绑定的句柄集合（本原语无 dispose，随组件生命周期存在）。 */
    private final List<Binding> bindings = new ArrayList<>();

    private final ScaleRenderer renderer;
    private final TextRendererI minLabel;
    private final TextRendererI maxLabel;

    public Scale(String id) {
        this(id, false);
    }

    public Scale(String id, boolean vertical) {
        this.id = "scale:" + id;
        this.rangeAssoc = new Association<>("scale:" + id, new Range(0.0, 100.0));
        AssociationRegistry.global().register(rangeAssoc);

        // min / max ↔ 范围关联 双向绑定（自带重入锁，避免回环）
        bindings.add(Binding.bindBidirectional(min, rangeAssoc, v -> new Range(v, max.get()), r -> r.min()));
        bindings.add(Binding.bindBidirectional(max, rangeAssoc, v -> new Range(min.get(), v), r -> r.max()));

        this.renderer = new ScaleRenderer(min::get, max::get, ticks::get, vertical);

        column();
        Node canvas = new Node().with(renderer);
        canvas.flex.height = 10;
        add(canvas);

        minLabel = Renderers.text("0").color(Theme.current().text()).align(Align.LEFT);
        maxLabel = Renderers.text("100").color(Theme.current().text()).align(Align.RIGHT);
        if (vertical) {
            Node top = new Node().with(maxLabel);
            top.flex.height = 9;
            Node bot = new Node().with(minLabel);
            bot.flex.height = 9;
            add(top);
            add(bot);
        } else {
            Node row = new Node().row();
            row.flex.height = 9;
            Node left = new Node().with(minLabel);
            left.flex.grow = 1;
            Node right = new Node().with(maxLabel);
            right.flex.grow = 1;
            row.add(left);
            row.add(right);
            add(row);
        }

        // 标签随端值刷新
        ChangeListener<Double> upd = (o, n) -> updateLabels();
        min.addListener(upd);
        max.addListener(upd);
    }

    private void updateLabels() {
        minLabel.setLine(0, Component.literal(fmt(min.get())));
        maxLabel.setLine(0, Component.literal(fmt(max.get())));
    }

    private static String fmt(double v) {
        return (v == Math.rint(v)) ? String.valueOf((long) v) : String.format("%.1f", v);
    }

    // ------------------------------------------------------------------
    // ScaleCapable 能力实现
    // ------------------------------------------------------------------

    /** 当前量程（可被关联 / 绑定驱动）。 */
    @Override
    public Property<Range> range() {
        return rangeAssoc;
    }

    /** 切到竖直方向（轴线沿 Y 轴、刻度沿 Y 分布）。 */
    @Override
    public void vertical() {
        renderer.vertical(true);
    }

    // ------------------------------------------------------------------
    // 便利设定 API（非能力部分，供构建期直接调）
    // ------------------------------------------------------------------


    public Scale min(double v) { min.set(v); return this; }

    public Scale max(double v) { max.set(v); return this; }

    /** 一次性设置范围（同时经关联对外广播）。 */
    public Scale range(double lo, double hi) { min.set(lo); max.set(hi); return this; }

    public Scale ticks(int n) { ticks.set(Math.max(0, n)); return this; }

    // ------------------------------------------------------------------
    // 轴线 + 刻度渲染器：每帧从 Theme 取色，跟随当前风格预设
    // ------------------------------------------------------------------

    private static final class ScaleRenderer implements Renderer {

        private final Supplier<Double> minSup, maxSup;
        private final Supplier<Integer> ticksSup;
        private boolean vertical;

        ScaleRenderer(Supplier<Double> minSup, Supplier<Double> maxSup, Supplier<Integer> ticksSup, boolean vertical) {
            this.minSup = minSup;
            this.maxSup = maxSup;
            this.ticksSup = ticksSup;
            this.vertical = vertical;
        }

        void vertical(boolean v) { this.vertical = v; }

        @Override
        public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
            if (w <= 0 || h <= 0) return;
            // 全部来自 Theme，零内联取色
            int line = Theme.current().borderDisabled();
            double mn = minSup.get();
            double mx = maxSup.get();
            if (mx <= mn) mx = mn + 1;     // 防止范围退化
            int n = Math.max(1, ticksSup.get());
            double denom = (n <= 1) ? 1.0 : (n - 1);   // 避免 n==1 时除以 0
            if (vertical) {
                int cx = x + w / 2;
                Draw.fill(g, cx, y, cx + 1, y + h, line);          // 轴线
                for (int i = 0; i < n; i++) {
                    int ty = y + (int) Math.round((double) (h - 1) * i / denom);
                    Draw.fill(g, cx - 3, ty, cx + 4, ty + 1, line); // 刻度
                }
            } else {
                int cy = y + h / 2;
                Draw.fill(g, x, cy, x + w, cy + 1, line);          // 轴线
                for (int i = 0; i < n; i++) {
                    int tx = x + (int) Math.round((double) (w - 1) * i / denom);
                    Draw.fill(g, tx, cy - 3, tx + 1, cy + 4, line); // 刻度
                }
            }
        }

        @Override
        public Renderer copy() {
            return new ScaleRenderer(minSup, maxSup, ticksSup, vertical);
        }
    }
}
