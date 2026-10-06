package com.cimdy.coe.ui.widget.component.button;

import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 互斥单选组：一组 {@link ToggleWidget}（CHECK 风格），同一时刻只有一个被选中。
 *
 * <p>选中一个会自动清除其余 —— 这正是「单选」与「复选」的本质区别，按钮本身不会做这件事。
 * 数据无关：数据类型是调用方的 {@code T}，标签由 {@code Function<T,Component>} 提供。
 */
public class RadioGroup<T> extends Node {

    private final List<T> items = new ArrayList<>();
    private final List<ToggleWidget> toggles = new ArrayList<>();
    private Function<T, Component> labeler;
    private final Property<T> value = new SimpleObservable<>(null);
    private Consumer<T> onSelect;
    private int selected = -1;
    private boolean selectable = true;

    public RadioGroup() {
        this.id = "radio-group";
        column();
    }

    public RadioGroup<T> items(List<T> items, Function<T, Component> labeler) {
        this.items.clear();
        this.items.addAll(items);
        this.labeler = labeler;
        rebuild();
        return this;
    }

    public RadioGroup<T> onSelect(Consumer<T> c) { this.onSelect = c; value.onChange(c); return this; }

    public RadioGroup<T> selectable(boolean b) { this.selectable = b; return this; }

    public int selectedIndex() { return selected; }

    public T selected() {
        return (selected >= 0 && selected < items.size()) ? items.get(selected) : null;
    }

    public RadioGroup<T> select(int index) { setSelected(index, false); return this; }

    private void rebuild() {
        clearChildren();
        toggles.clear();
        for (int i = 0; i < items.size(); i++) {
            final int idx = i;
            T item = items.get(i);
            ToggleWidget t = new ToggleWidget().style(ToggleWidget.Style.CHECK);
            Component lbl = (labeler != null) ? labeler.apply(item) : Component.literal(String.valueOf(item));
            t.label(lbl);
            t.onChange(checked -> {
                if (!selectable) return;
                if (checked) setSelected(idx, true);
            });
            t.checked(i == selected);
            add(t);
            toggles.add(t);
        }
    }

    private void setSelected(int index, boolean fire) {
        if (index < -1 || index >= items.size()) return;
        selected = index;
        for (int i = 0; i < toggles.size(); i++) toggles.get(i).checked(i == index);
        if (fire && index >= 0 && index < items.size()) {
            value.set(items.get(index));
        }
    }

    @Override
    public RadioGroup<T> copy() {
        RadioGroup<T> g = new RadioGroup<>();
        g.flex.copyFrom(flex);
        g.id = id;
        g.visible = visible;
        g.state = state;
        g.items.addAll(items);
        g.labeler = labeler;
        if (onSelect != null) g.onSelect(onSelect);
        g.selectable = selectable;
        g.selected = selected;
        g.rebuild();
        return g;
    }
}
