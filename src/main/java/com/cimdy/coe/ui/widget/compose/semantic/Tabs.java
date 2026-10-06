package com.cimdy.coe.ui.widget.compose.semantic;

import com.cimdy.coe.ui.widget.component.bar.TabBar;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * 标签页容器（语义部件）：标签栏 + 随选中切换的内容面板。
 *
 * <p>在已有 {@link TabBar}（互斥分段选择）之上封一层内容管理：每加一个页签就附一块内容节点，
 * 选哪个页签就只显示那一块、其余隐藏。选哪页的逻辑由 {@link TabBar} 保证互斥，
 * 本组件只负责「选中 → 显隐对应面板」，屏幕完全不必手写 {@code setVisible} 联动。
 *
 * <pre>{@code
 * Tabs tabs = new Tabs()
 *         .add(Component.literal("属性"), attrPane)
 *         .add(Component.literal("技能"), skillPane)
 *         .onSelect(i -> onPage(i));
 * }</pre>
 */
public class Tabs extends Node {

    private final TabBar tabBar;
    private final Node body;
    private final List<Component> labels = new ArrayList<>();
    private final List<Node> panes = new ArrayList<>();
    private IntConsumer onSelect;

    public Tabs() {
        this.id = "tabs";
        column();
        tabBar = new TabBar().styled();
        tabBar.flex.gapMain = 2;
        body = new Node().column();
        body.id = "tabs-body";
        body.flex.grow = 1;
        add(tabBar);
        add(body);
        // 选中变化 → 切换面板显隐；先挂监听，后续 add 触发的 rebuild 不会丢
        tabBar.onSelect(this::show);
    }

    /** 追加一个页签及其内容面板（首块默认可见）。返回 this 便于链式。 */
    public Tabs add(Component label, Node content) {
        labels.add(label);
        tabBar.items(labels);                 // 重建标签栏（保留已挂的 onSelect 监听）
        content.flex.grow = 1;
        body.add(content);
        panes.add(content);
        show(tabBar.selectedIndex());         // 按当前选中同步显隐，不跳转到新页
        return this;
    }

    /** 选中变化回调，参数是新选中的页签下标。 */
    public Tabs onSelect(IntConsumer c) { this.onSelect = c; return this; }

    /** 以代码方式切到某页（同步标签高亮与面板显隐，不触发 {@link #onSelect}）。 */
    public Tabs select(int index) {
        tabBar.select(index);
        show(index);
        return this;
    }

    public int selectedIndex() { return tabBar.selectedIndex(); }

    /** 按选中下标切换面板显隐，并通知外部回调。 */
    private void show(int index) {
        for (int i = 0; i < panes.size(); i++) {
            panes.get(i).visible = (i == index);
        }
        if (onSelect != null) onSelect.accept(index);
    }

    @Override
    public Tabs copy() {
        Tabs t = new Tabs();
        t.flex.copyFrom(flex);
        t.id = id;
        t.visible = visible;
        t.state = state;
        for (int i = 0; i < labels.size(); i++) {
            t.add(labels.get(i), panes.get(i).copy());
        }
        if (onSelect != null) t.onSelect(onSelect);
        return t;
    }
}
