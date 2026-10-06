package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.compose.EnergyBar;
import com.cimdy.coe.ui.widget.compose.WorkerRow;
import com.cimdy.coe.ui.widget.compose.item.ItemGrid;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 终端（Terminal）五窗格仪表盘：主区 {@link ItemGrid} 分页 + 工作区 + 库存 + 任务 WorkerRow 列表（带空态）+ 信息可滚动窗格，
 * 顶部 {@link EnergyBar}。
 *
 * <p>布局（计划 §3.1）：
 * <pre>
 * ┌──────────────── EnergyBar ────────────────┐
 * ├─ mainGrid(grow) ─┬─ workPane ────────────┤
 * │                  ├─ inventoryPane ─────────┤
 * ├─ taskPane ───────┬─ infoPane(Scrollable) ─┤
 * └──────────────────┴───────────────────────┘
 * </pre>
 *
 * <p>纪律：组件只做五窗格的布局装配 + 数据注入，不持有业务逻辑；各窗格内容由 Screen 注入。
 * 任务窗格无 WorkerRow 时显示空态提示。
 *
 * <p>潜在复用方：TerminalScreen（唯一当前消费者）；未来任何「多窗格工作台」型界面。
 */
public class TerminalDashboard extends AnimatedNode {

    public static TerminalDashboard of() { return new TerminalDashboard(); }

    private final Node energyRow = new Node().row();
    private final Node mainRow = new Node().row().gap(4);
    private final Node rightCol = new Node().column().gap(4);
    private final Node bottomRow = new Node().row().gap(4);
    private final Node taskList = new Node().column();

    private final Node mainPane = new Node().column();
    private final Node workPaneWrap = new Node().column();
    private final Node inventoryWrap = new Node().column();
    private final Node infoWrap = new Node().column();
    private final Node taskEmpty = new Node();

    private TerminalDashboard() {
        id("terminal-dashboard");
        column().gap(4);
        padding(4);
        with(Theme.panel());

        energyRow.grow(0);

        // 主区
        mainPane.with(Theme.panel());
        mainPane.grow(1);
        mainPane.add(titled("ui.coe.terminal.main_grid"));

        // 右列：工作区 + 库存
        workPaneWrap.with(Theme.panel());
        workPaneWrap.add(titled("ui.coe.terminal.work"));
        inventoryWrap.with(Theme.panel());
        inventoryWrap.add(titled("ui.coe.terminal.inventory"));
        rightCol.add(workPaneWrap);
        rightCol.add(inventoryWrap);

        mainRow.add(mainPane);
        mainRow.add(rightCol);
        mainRow.grow(1);

        // 底行：任务 + 信息
        Node taskPane = new Node().column();
        taskPane.with(Theme.panel());
        taskPane.add(titled("ui.coe.terminal.tasks"));
        taskList.gap(2);
        taskPane.add(taskList);
        taskEmpty.with(Renderers.text(Component.translatable("ui.coe.terminal.no_task"))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE)
                .color(Theme.current().placeholderText()));
        taskPane.add(taskEmpty);

        infoWrap.with(Theme.panel());
        infoWrap.add(titled("ui.coe.terminal.info"));
        infoWrap.grow(1);

        bottomRow.add(taskPane);
        bottomRow.add(infoWrap);
        bottomRow.grow(1);

        add(energyRow);
        add(mainRow);
        add(bottomRow);
    }

    // ------------------------------------------------------------------
    // 冻结签名 §2
    // ------------------------------------------------------------------

    /** 顶部能量条。 */
    public TerminalDashboard energy(EnergyBar bar) {
        energyRow.clearChildren();
        if (bar != null) energyRow.add(bar);
        return this;
    }

    /** 主区物品网格（分页）。 */
    public TerminalDashboard mainGrid(ItemGrid grid) {
        replaceContent(mainPane, grid);
        return this;
    }

    /** 工作区窗格。 */
    public TerminalDashboard workPane(Node n) {
        replaceContent(workPaneWrap, n);
        return this;
    }

    /** 库存窗格。 */
    public TerminalDashboard inventoryPane(Node n) {
        replaceContent(inventoryWrap, n);
        return this;
    }

    /** 任务 WorkerRow 列表；空列表显示空态。 */
    public TerminalDashboard taskPane(List<WorkerRow> rows) {
        taskList.clearChildren();
        if (rows != null) {
            for (WorkerRow r : rows) {
                if (r != null) taskList.add(r);
            }
        }
        taskEmpty.visible = taskList.children.isEmpty();
        return this;
    }

    /** 可滚动信息窗格。 */
    public TerminalDashboard infoPane(Scrollable info) {
        replaceContent(infoWrap, info);
        return this;
    }

    // ------------------------------------------------------------------

    /** 在带标题的窗格里，把内容节点挂到标题之后（保留标题行）。 */
    private void replaceContent(Node pane, Node content) {
        // pane 的第一个 child 是标题行；其后清空并挂 content
        Node title = pane.children.isEmpty() ? null : pane.children.get(0);
        pane.clearChildren();
        if (title != null) pane.add(title);
        if (content != null) {
            content.grow(1);
            pane.add(content);
        }
    }

    private Node titled(String key) {
        TextRendererI t = Renderers.text(Component.translatable(key))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE)
                .color(Theme.current().gemGold());
        Node n = new Node();
        n.with(t);
        return n;
    }
}
