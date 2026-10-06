package com.cimdy.coe.ui.widget.compose.picker;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 物品选择弹窗：把「搜索框 + 物品网格 + 选中回调」封成一个可直接弹出的组件。
 *
 * <p>原本每个需要选物品的界面都要手写一遍 Window + SearchableList + 一堆 bindCell，
 * 这里收成一处。组件<b>不依赖任何业务类型</b>：候选用 {@link ItemStack}（MC 原语），
 * 候选的采集（翻创造标签页等）留给调用方 —— 那是 Screen 的事，不该污染 lib。
 *
 * <pre>{@code
 * ItemPicker picker = new ItemPicker(Component.literal("选择物品"), 120, 80)
 *     .candidates(allItems)
 *     .onPick(stack -> entry.setItem(stack));
 * picker.open(root);   // root 是 Screen 的 WindowLayer
 * }</pre>
 */
public class ItemPicker extends Window {

    private final SearchableList<ItemStack> list;
    private final Property<ItemStack> value = new SimpleObservable<>(null);
    private WindowLayer root;

    public ItemPicker(Component title, int w, int h) {
        super();
        this.id = "item-picker";
        title(title);
        styled();
        flex.size(w, h);

        list = new SearchableList<>(pickerCell(), this::bindCell)
                .textOf(stack -> stack.getDisplayName().getString())
                .grid().itemGap(3).contentPadding(3).clearBeforeBind()
                .scrollBar(true);
        list.flex.grow = 1;
        add(list);
    }

    /** 候选物品（已采集好的列表，不含业务过滤逻辑）。 */
    public ItemPicker candidates(List<ItemStack> items) {
        list.items(items);
        return this;
    }

    /** 选中后回调。 */
    public ItemPicker onPick(Consumer<ItemStack> c) {
        value.addListener((o, n) -> c.accept(n));
        return this;
    }

    /** 自定义「查询 × 物品」匹配器（默认等价于按显示名子串）。业务侧的复杂匹配放这里。 */
    public ItemPicker match(java.util.function.BiPredicate<String, ItemStack> m) {
        list.match(m);
        return this;
    }

    /** 弹出为模态。记住 root 以便选中后自行关闭。 */
    public void open(WindowLayer root) {
        this.root = root;
        root.openModal(this);
    }

    private void pick(ItemStack stack) {
        value.set(stack);
        if (root != null) root.closeModal();
    }

    private static Button pickerCell() {
        Button b = new Button();
        b.flex.size(12, 12);
        return b;
    }

    private void bindCell(Node view, ItemStack item, int index) {
        if (!(view instanceof Button cell)) return;
        cell.styled();
        cell.with(Renderers.item(() -> item).showTooltip(true));
        cell.onClick(() -> pick(item));
    }
}
