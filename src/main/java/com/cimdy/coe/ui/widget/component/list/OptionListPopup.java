package com.cimdy.coe.ui.widget.component.list;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.FlexDirection;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 弹出层：一个浮动的小窗，用来「让用户选一个东西」或「确认一件事」。
 *
 * <p>合并了旧库的 {@code TooltipPicker} 与 {@code ModalDialog}。这两个类各写了一遍
 * 「浮层定位 + 选项列表 + 点击回调 + 关闭」，差别仅在于一个挂在鼠标旁、一个居中并带按钮。
 * 既然骨架相同，就只留一个组件，用两种装配方式表达：
 *
 * <pre>{@code
 * // 选择器：挂在某个节点下方
 * OptionListPopup.of(colors, c -> Component.literal(c.name()))
 *         .onPick(c -> apply(c))
 *         .anchorBelow(button)
 *         .showIn(layer);
 *
 * // 确认框：居中模态
 * OptionListPopup.<Void>dialog(Component.literal("确认删除？"))
 *         .action(Component.literal("删除"), this::doDelete)
 *         .action(Component.literal("取消"), () -> {})
 *         .showModalIn(layer);
 * }</pre>
 *
 * @param <T> 选项的数据类型，lib 不关心它是什么
 */
public class OptionListPopup<T> extends Window {

    private static final int ROW_HEIGHT = 12;
    private static final int MIN_WIDTH = 48;

    private final List<T> options = new ArrayList<>();
    private Function<T, Component> labeler = t -> Component.literal(String.valueOf(t));
    private final Property<T> value = new SimpleObservable<>(null);

    private final Scrollable list;
    private Node actionRow;

    /** 最多同时显示几行，超出则滚动。 */
    private int maxVisibleRows = 8;
    private boolean autoWidth = true;
    private boolean closeOnPick = true;

    public OptionListPopup() {
        this.id = "option-popup";
        draggable(false);
        list = scrollBody();
        list.itemGap(1);
    }

    // ------------------------------------------------------------------
    // 工厂
    // ------------------------------------------------------------------

    public static <T> OptionListPopup<T> of(List<T> options, Function<T, Component> labeler) {
        OptionListPopup<T> p = new OptionListPopup<>();
        p.labeler = labeler;
        p.options(options);
        return p;
    }

    /** 确认框风格：一段消息 + 若干动作按钮。 */
    public static <T> OptionListPopup<T> dialog(Component message) {
        OptionListPopup<T> p = new OptionListPopup<>();
        p.message(message);
        return p;
    }

    // ------------------------------------------------------------------
    // 选项
    // ------------------------------------------------------------------

    public OptionListPopup<T> options(List<T> items) {
        options.clear();
        if (items != null) options.addAll(items);
        rebuild();
        return this;
    }

    public OptionListPopup<T> onPick(Consumer<T> c) { value.addListener((o, n) -> c.accept(n)); return this; }

    public OptionListPopup<T> closeOnPick(boolean b) { this.closeOnPick = b; return this; }

    public OptionListPopup<T> maxVisibleRows(int n) { this.maxVisibleRows = Math.max(1, n); return this; }

    public OptionListPopup<T> autoWidth(boolean b) { this.autoWidth = b; return this; }

    private void rebuild() {
        list.clearChildren();
        for (T item : options) {
            Component label = labeler.apply(item);
            Button b = Button.of(label).clickSound(true);
            b.id = "option";
            b.flex.height = ROW_HEIGHT;
            if (b.labelRenderer() != null) {
                b.labelRenderer()
                        .align(Align.LEFT)
                        .vAlign(VAlign.MIDDLE)
                        .colorHover(Theme.current().textHover());
            }
            b.padding(0, 3);
            b.background(Renderers.color(Theme.current().surfaceTransparent()).backgroundHover(Theme.current().optionHoverBg()));
            b.onClick(() -> pick(item));
            list.add(b);
        }
        applyAutoSize();
    }

    public void pick(T item) {
        value.set(item);
        if (closeOnPick) close();
    }

    // ------------------------------------------------------------------
    // 确认框风格
    // ------------------------------------------------------------------

    /** 在选项列表上方放一段说明文字。 */
    public OptionListPopup<T> message(Component text) {
        Node msg = new Node().with(Renderers.text(text)
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE)
                .wrap(true));
        msg.id = "popup-message";
        msg.padding(4);
        list.add(msg);
        applyAutoSize();
        return this;
    }

    /** 底部动作按钮，从左到右排列。 */
    public OptionListPopup<T> action(Component label, Runnable handler) {
        ensureActionRow();
        Button b = Button.of(label).styled().onClick(() -> {
            handler.run();
            if (closeOnPick) close();
        });
        b.flex.grow = 1;
        b.flex.height = 14;
        actionRow.add(b);
        return this;
    }

    private void ensureActionRow() {
        if (actionRow != null) return;
        actionRow = new Node().row().gap(3);
        actionRow.id = "popup-actions";
        actionRow.flex.height = 14;
        actionRow.margin(3, 3, 3, 3);
        // 动作行不进滚动区，永远贴在底部
        addChrome(actionRow);
    }

    // ------------------------------------------------------------------
    // 定位与显示
    // ------------------------------------------------------------------

    /** 自动按内容算宽高。 */
    public void applyAutoSize() {
        int rows = list.content.children.size();
        int h = Math.min(rows, maxVisibleRows) * (ROW_HEIGHT + 1) + 2;
        if (actionRow != null) h += 20;
        if (hasTitleBar()) h += Window.TITLE_BAR_HEIGHT;
        flex.height = Math.max(h, ROW_HEIGHT + 4);

        if (autoWidth) {
            int w = MIN_WIDTH;
            for (T item : options) w = Math.max(w, Draw.width(labeler.apply(item)) + 12);
            flex.width = Math.min(w, 220);
        }
    }

    /** 挂在某个节点正下方，超出屏幕则翻到上方。 */
    public OptionListPopup<T> anchorBelow(Node anchor) {
        applyAutoSize();
        int x = anchor.absX;
        int y = anchor.absY + anchor.height + 1;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            if (x + flex.width > sw) x = Math.max(0, sw - flex.width);
            if (y + flex.height > sh) y = Math.max(0, anchor.absY - flex.height - 1);
        }
        position(x, y);
        return this;
    }

    /** 挂在鼠标旁。 */
    public OptionListPopup<T> anchorAt(int mouseX, int mouseY) {
        applyAutoSize();
        position(mouseX + 2, mouseY + 2);
        return this;
    }

    /** 加入窗口层并显示（非模态）。 */
    public OptionListPopup<T> showIn(WindowLayer layer) {
        applyAutoSize();
        layer.add(this);
        layer.bringToFront(this);
        open();
        return this;
    }

    /** 以模态方式居中显示。 */
    public OptionListPopup<T> showModalIn(WindowLayer layer) {
        applyAutoSize();
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            center(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }
        layer.add(this);
        layer.openModal(this);
        return this;
    }

    // ------------------------------------------------------------------

    /** 弹出层默认皮肤：不透明底 + 细边框，保证盖住背后内容看得清。 */
    public OptionListPopup<T> popupStyle() {
        background(Theme.popup());
        flex.direction = FlexDirection.COLUMN;
        return this;
    }
}
