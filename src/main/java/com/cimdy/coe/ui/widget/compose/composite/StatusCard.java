package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.widget.compose.semantic.KeyValue;
import com.cimdy.coe.ui.widget.compose.semantic.Section;
import com.cimdy.coe.ui.widget.compose.semantic.Tabs;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态卡（第 2 层复合组件 —— 计划一 StatusCard）：{@link Section} + {@link KeyValue} 组合。
 *
 * <p>一句话排开「标签 : 值」的只读状态表（等级 / 能量 / 模式…）。标签页模式可选：
 * {@link #tabs(Component...)} 后，随后的 {@link #add} 进入当前页签。
 *
 * <pre>{@code
 * StatusCard card = StatusCard.of()
 *         .title(Component.translatable("hold.coe.name"))
 *         .add(Component.translatable("hold.coe.level"), Component.literal("3"))
 *         .add(Component.translatable("hold.coe.energy"), Component.literal("120/200"));
 * }</pre>
 */
public class StatusCard extends Node {

    private final Section section = new Section();
    /** 当前 add() 落入的面板：无标签页时为 Section.body()，否则为当前页签面板。 */
    private Node pane;
    private Tabs tabs;
    private final List<Node> tabPanes = new ArrayList<>();

    private StatusCard() {
        this.id = "status-card";
        column();
        add(section);
        pane = section.body();
        pane.flex.gapMain = 1;
    }

    public static StatusCard of() { return new StatusCard(); }

    /** 卡片标题（空 / null 隐藏标题栏）。 */
    public StatusCard title(Component t) {
        section.title(t);
        return this;
    }

    /** 追加一行「标签 : 值」。 */
    public StatusCard add(Component label, Component value) {
        pane.add(new KeyValue().label(label).value(value).condensed());
        return this;
    }

    /** 追加一行「标签 : 自定义值节点」（如迷你条 / 图标）。 */
    public StatusCard add(Component label, Node value) {
        pane.add(new KeyValue().label(label).value(value).condensed());
        return this;
    }

    /**
     * 启用标签页模式：给定页签名。随后的 {@link #add} 进入 {@link #tab(int)} 指定的页签
     * （默认第 0 页）。标签切换由 {@link Tabs} 内置互斥。
     */
    public StatusCard tabs(Component... names) {
        tabs = new Tabs();
        section.body().add(tabs);
        tabPanes.clear();
        for (Component name : names) {
            Node p = new Node().column();
            p.flex.gapMain = 1;
            tabs.add(name, p);
            tabPanes.add(p);
        }
        pane = tabPanes.isEmpty() ? section.body() : tabPanes.get(0);
        return this;
    }

    /** 指定后续 {@link #add} 落入的页签下标。 */
    public StatusCard tab(int index) {
        if (index >= 0 && index < tabPanes.size()) pane = tabPanes.get(index);
        return this;
    }

    /** 当前选中的页签下标（无标签页模式恒为 0）。 */
    public int selectedTab() {
        return tabs == null ? 0 : tabs.selectedIndex();
    }
}
