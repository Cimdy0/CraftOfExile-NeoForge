package com.cimdy.coe.ui.widget.component.bar;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 标签页栏 / 分段选择器：一排互斥的标签，同一时刻只有一个处于选中态。
 *
 * <p>各界面若都自己摆一排 {@code Button} 再手写「点一个就把其它的 active 清掉」，
 * 至少抄了三遍且每次都容易漏掉边界（比如删掉当前页后索引越界）。这里收敛成一个组件：
 *
 * <ul>
 *   <li>互斥选中由组件保证，回调只给索引，不碰业务数据；</li>
 *   <li>{@link #closable(IntConsumer)} 打开后每个标签右侧长出关闭按钮，
 *       点它<b>不会</b>顺带触发切页（子节点命中即止）；</li>
 *   <li>{@link #onAdd(Runnable)} 会在末尾补一个 {@code +} 按钮；</li>
 *   <li>标签宽度默认由文字撑开，不必手算。</li>
 * </ul>
 *
 * <pre>{@code
 * TabBar tabs = new TabBar().styled()
 *         .items(Component.literal("属性"), Component.literal("技能"))
 *         .onSelect(i -> showPage(i));
 * }</pre>
 */
public class TabBar extends Node {

    private final List<Component> labels = new ArrayList<>();
    private final List<Button> tabs = new ArrayList<>();

    private int selected = 0;
    private final Property<Integer> value = new SimpleObservable<>(null);
    private IntConsumer onSelect;
    private IntConsumer onClose;
    private Runnable onAdd;

    private boolean closable = false;
    private boolean styled = false;
    private int tabHeight = 16;
    private int tabPaddingX = 6;

    /**
     * 选中指示条横向偏移（px，相对本节点左边），附录 D.1「Tab·PageNav 指示条 slide」。
     *
     * <p>与 {@code SegmentToggle} 选中块同一先例：tick 里把目标吸附到当前选中 tab 的左边，
     * 补间从<b>当前值</b>起滑向新档位（{@link Animator#retarget} 中断式），切页即旧档滑到新档。
     * 时长读 {@link AnimRegistry#ms(AnimRegistry.Trigger)} SELECTED 令牌；只画渲染偏移，
     * 不改 absX/命中（指示条不是命中目标）。
     */
    private final Animator indicatorX;

    public TabBar() {
        this.id = "tab-bar";
        row();
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = 2;
        this.indicatorX = new Animator(0F, 0F,
                AnimRegistry.ms(AnimRegistry.Trigger.SELECTED), Easing.EASE_IN_OUT_CUBIC);
    }

    public static TabBar of(Component... items) { return new TabBar().items(items); }

    // ------------------------------------------------------------------
    // 内容
    // ------------------------------------------------------------------

    public TabBar items(List<Component> items) {
        labels.clear();
        labels.addAll(items);
        clampSelection();
        rebuild();
        return this;
    }

    public TabBar items(Component... items) { return items(Arrays.asList(items)); }

    /** 删除一个标签，并把选中索引收敛到仍然有效的位置。 */
    public TabBar removeTab(int index) {
        if (index < 0 || index >= labels.size()) return this;
        labels.remove(index);
        if (selected > index) selected--;
        clampSelection();
        rebuild();
        return this;
    }

    public int count() { return labels.size(); }

    public int selectedIndex() { return selected; }

    /** 以代码方式切页，不触发 {@link #onSelect} 回调。 */
    public TabBar select(int index) { setSelected(index, false); return this; }

    // ------------------------------------------------------------------
    // 行为
    // ------------------------------------------------------------------

    /** 切页回调，参数是新选中的索引。 */
    public TabBar onSelect(IntConsumer c) { this.onSelect = c; value.addListener((o, n) -> c.accept(n)); return this; }

    /** 打开关闭按钮。回调只负责通知，真正要删还得调用方自己调 {@link #removeTab(int)}。 */
    public TabBar closable(IntConsumer onClose) {
        this.closable = true;
        this.onClose = onClose;
        rebuild();
        return this;
    }

    /** 末尾追加一个 {@code +} 按钮。 */
    public TabBar onAdd(Runnable r) {
        this.onAdd = r;
        rebuild();
        return this;
    }

    public TabBar tabHeight(int px) {
        this.tabHeight = Math.max(1, px);
        rebuild();
        return this;
    }

    /** 套一套默认皮肤：标签底一层覆盖常态/悬浮/选中三态。 */
    public TabBar styled() {
        this.styled = true;
        rebuild();
        return this;
    }

    private void clampSelection() {
        if (labels.isEmpty()) selected = -1;
        else selected = Math.max(0, Math.min(labels.size() - 1, selected));
    }

    private void setSelected(int index, boolean fire) {
        if (index < -1 || index >= labels.size()) return;
        selected = index;
        for (int i = 0; i < tabs.size(); i++) tabs.get(i).active(i == index);
        if (fire && index >= 0) value.set(index);
    }

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    private void rebuild() {
        clearChildren();
        tabs.clear();

        for (int i = 0; i < labels.size(); i++) {
            final int idx = i;
            Button tab = buildTab(labels.get(i), idx);
            tab.active(i == selected);
            tab.onClick(() -> setSelected(idx, true));
            add(tab);
            tabs.add(tab);
        }

        if (onAdd != null) {
            Button plus = Button.of(Component.literal("+"));
            plus.flex.size(tabHeight, tabHeight);
            plus.onClick(() -> { if (onAdd != null) onAdd.run(); });
            if (styled) plus.styled();
            add(plus);
        }
    }

    private Button buildTab(Component label, int idx) {
        Button tab = new Button();
        tab.id = "tab";
        tab.flex.height = tabHeight;
        tab.flex.padding.set(0, tabPaddingX, 0, tabPaddingX);
        tab.flex.alignItems = AlignItems.CENTER;
        tab.flex.gapMain = 3;

        if (!closable) {
            // 无关闭按钮时让它保持叶子：文字能直接把标签撑开，不用手算宽度
            tab.label(label);
        } else {
            Node text = new Node();
            text.with(Renderers.text(label)
                    .align(Align.CENTER)
                    .vAlign(VAlign.MIDDLE));
            tab.add(text);

            Button close = Button.of(Component.literal("x"));
            close.id = "tab-close";
            close.flex.size(7, 9);
            close.clickSound(false).onClick(() -> { if (onClose != null) onClose.accept(idx); });
            if (close.labelRenderer() != null) {
                close.labelRenderer().color(Theme.current().textDisabled()).colorHover(Theme.current().textHover());
            }
            tab.add(close);
        }

        if (styled) {
            tab.background(Theme.tabBg());
            if (tab.labelRenderer() != null) {
                tab.labelRenderer().colorDisabled(Theme.current().textDisabled()).colorHover(Theme.current().textHover());
            }
        }
        return tab;
    }

    // ------------------------------------------------------------------
    // 选中指示条 slide（附录 D.1；只改渲染偏移，不改布局/命中）
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, RenderContext ctx, boolean parentDisabled) {
        super.render(g, ctx, parentDisabled);
        if (visible) drawIndicator(g);
    }

    /** 在选中 tab 底部画一条随选中滑动的 accent 下划线；未选中或无尺寸时不画。 */
    private void drawIndicator(GuiGraphicsExtractor g) {
        if (selected < 0 || selected >= tabs.size()) return;
        Button sel = tabs.get(selected);
        if (sel.width <= 0 || sel.height <= 0) return;
        int ix = Math.round(absX + indicatorX.value());
        int iy = sel.absY + sel.height - 2;
        Draw.roundRect(g, ix, iy, sel.width, 2, 1, Theme.current().accent());
    }

    @Override
    public void tick() {
        // 布局稳定后把指示条吸附到当前选中 tab 的左边；选中变化即从旧位滑向新位（无空转）
        if (selected >= 0 && selected < tabs.size()) {
            indicatorX.retarget(tabs.get(selected).absX - absX);
        }
        indicatorX.tick();
        super.tick();
    }

    // ------------------------------------------------------------------

    @Override
    public TabBar copy() {
        TabBar t = new TabBar();
        t.flex.copyFrom(flex);
        t.id = id;
        t.visible = visible;
        t.state = state;
        t.labels.addAll(labels);
        t.selected = selected;
        if (onSelect != null) t.onSelect(onSelect);
        t.onClose = onClose;
        t.onAdd = onAdd;
        t.closable = closable;
        t.styled = styled;
        t.tabHeight = tabHeight;
        t.tabPaddingX = tabPaddingX;
        t.rebuild();
        return t;
    }
}
