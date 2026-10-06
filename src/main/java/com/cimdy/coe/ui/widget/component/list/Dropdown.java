package com.cimdy.coe.ui.widget.component.list;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 下拉选择器（ComboBox）：一个显示「当前选项 + 箭头」的按钮，点击在下方弹出选项列表，
 * 选中后更新按钮文案并回调；点击列表之外自动收起。
 *
 * <p>纯复合、数据无关：选项类型 {@code <T>} 由调用方决定，库只负责「弹 / 选 / 收」的交互骨架，
 * 弹出层直接复用 {@link OptionListPopup}（含滚动、自动定位、翻屏等能力）。
 *
 * <pre>{@code
 * Dropdown<Difficulty> dd = new Dropdown<Difficulty>()
 *         .options(listOf(Difficulty.values()), d -> Component.literal(d.name()))
 *         .selected(Difficulty.NORMAL)
 *         .onChange(d -> apply(d))
 *         .in(ui.layer());   // 指定弹出层
 * dd.flex.size(120, 16);
 * }</pre>
 *
 * <p>键盘：点开后用鼠标或方向选中（{@link OptionListPopup} 已处理滚动定位），
 * 点按钮本身可再次点开/收起；本组件不抢全局焦点，不会打断正在输入的文本框。
 */
public class Dropdown<T> extends Node {

    /** 选项文案右侧的折叠箭头。 */
    private static final String CARET = " ▾";

    private final Button trigger;
    private final TextRendererI triggerLabel;

    private final List<T> options = new ArrayList<>();
    private Function<T, Component> labeler = t -> Component.literal(String.valueOf(t));
    private T selected;
    private Component placeholder = Component.literal("—");

    private final Property<T> value = new SimpleObservable<>(null);
    private Consumer<T> onChange;

    /** 弹出层宿主（{@link #in} 注入）。 */
    private WindowLayer layer;
    /** 当前打开的弹层；非空表示下拉处于展开态。 */
    private OptionListPopup<T> popup;
    /** 透明全屏遮罩，吞掉弹层之外的点击以收起。 */
    private Node catcher;

    public Dropdown() {
        this.id = "dropdown";
        row();
        flex.height = 16;
        flex.alignItems = AlignItems.CENTER;

        triggerLabel = Renderers.text(placeholder)
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE);
        trigger = Button.of(placeholder)
                .styled()
                .onClick(this::toggle);
        // 按钮内部那层文字只居中，这里手动把文案改成左对齐
        if (trigger.labelRenderer() != null) {
            trigger.labelRenderer().align(Align.LEFT).vAlign(VAlign.MIDDLE);
        }
        trigger.flex.grow = 1;
        trigger.padding(0, 4);
        add(trigger);
        refreshLabel();
    }

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    /** 选项集合与文案映射。 */
    public Dropdown<T> options(List<T> items, Function<T, Component> labeler) {
        this.options.clear();
        if (items != null) this.options.addAll(items);
        if (labeler != null) this.labeler = labeler;
        if (selected != null && !options.contains(selected)) selected = null;
        refreshLabel();
        return this;
    }

    /** 纯文字选项（仅当 {@code T = Component} 时使用）。 */
    @SuppressWarnings("unchecked")
    public Dropdown<T> options(List<Component> items) {
        return options((List<T>) (List<?>) items, t -> (Component) t);
    }

    /** 当前选中项（未选时为 null，显示占位符）。 */
    public Dropdown<T> selected(T item) {
        this.selected = item;
        refreshLabel();
        return this;
    }

    public T selected() { return selected; }

    public Dropdown<T> placeholder(Component c) {
        this.placeholder = (c == null) ? Component.empty() : c;
        refreshLabel();
        return this;
    }

    /** 选中变化回调（仅在用户从列表选定时触发，程序化 {@link #selected} 不触发）。 */
    public Dropdown<T> onChange(Consumer<T> c) { this.onChange = c; value.onChange(c); return this; }

    /** 指定弹出层（通常 {@code ui.layer()}）。必须在点开前设置。 */
    public Dropdown<T> in(WindowLayer layer) { this.layer = layer; return this; }

    /** 取触发按钮做皮肤微调（圆角、配色等）。 */
    public Button trigger() { return trigger; }

    // ------------------------------------------------------------------
    // 展开 / 收起
    // ------------------------------------------------------------------

    private void toggle() {
        if (popup != null) closePopup();
        else openPopup();
    }

    private void openPopup() {
        if (layer == null || options.isEmpty()) return;

        popup = OptionListPopup.<T>of(options, labeler)
                .onPick(this::pick)
                .closeOnPick(false)        // 自己管收起，确保遮罩也一并清掉
                .popupStyle()
                .anchorBelow(trigger);

        // 先铺透明全屏遮罩，再让弹层置顶——这样点弹层之外一律落到遮罩上收起
        int sw = screenW(), sh = screenH();
        catcher = new Node() {
            @Override
            protected boolean onMouseClicked(double lx, double ly, int button) {
                closePopup();
                return true;
            }
        };
        catcher.id = "dropdown-catcher";
        catcher.flex.size(sw, sh);
        layer.add(catcher);

        popup.showIn(layer);   // 内部 bringToFront，弹层盖在遮罩之上
    }

    private void pick(T item) {
        selected = item;
        refreshLabel();
        closePopup();
        value.set(item);
    }

    private void closePopup() {
        if (popup != null) popup.close();
        if (catcher != null && catcher.parent != null) catcher.parent.remove(catcher);
        popup = null;
        catcher = null;
    }

    // ------------------------------------------------------------------
    // 文案
    // ------------------------------------------------------------------

    private void refreshLabel() {
        Component text = (selected != null) ? labeler.apply(selected) : placeholder;
        trigger.label(text.copy().append(Component.literal(CARET)));
    }

    /** 屏幕宽（遮罩尺寸用）。取不到时回退到触发按钮的屏幕尺寸估计。 */
    private int screenW() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.getWindow() != null) ? mc.getWindow().getGuiScaledWidth() : 1920;
    }

    private int screenH() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.getWindow() != null) ? mc.getWindow().getGuiScaledHeight() : 1080;
    }

    // ------------------------------------------------------------------
    // 拷贝：复合组件必须重建并重链内部字段，不能直接用 Node.copy()
    // ------------------------------------------------------------------

    @Override
    public Dropdown<T> copy() {
        Dropdown<T> c = new Dropdown<>();
        c.flex.copyFrom(flex);
        c.id = id;
        c.visible = visible;
        c.state = state;
        c.options.addAll(options);
        c.labeler = labeler;
        c.selected = selected;
        c.placeholder = placeholder;
        if (onChange != null) c.onChange(onChange);
        c.layer = layer;
        return c;
    }
}
