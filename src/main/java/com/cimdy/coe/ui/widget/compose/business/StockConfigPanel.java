package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.compose.LabeledSlider;
import com.cimdy.coe.ui.widget.compose.filter.FilterBar;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;

import java.util.List;

/**
 * 存量配置面板（业务复合组件，计划 §二 业务复合组件表 / §3.1 StockScreen）。
 *
 * <p>装配顺序（自上而下）：
 * <ol>
 *   <li>{@link FilterBar}：类别 / 品质 / 词缀筛选（筛选项即数据，由 Screen 注入；
 *       「词缀」维度对模板候选池无效、对容器内实体物品有效，UI 先预留）；</li>
 *   <li>条目行列表：每行 = {@code ItemGrid} 物品格 + 容量 {@code ValueBar} +
 *       {@code SegmentToggle} 供应模式三段（Screen 预建好行节点后注入）；</li>
 *   <li>底部：容量 {@link LabeledSlider} +「批量加条目」按钮。</li>
 * </ol>
 *
 * <p>组件只布局与上抛：行内容、筛选数据、滑杆范围、按钮回调全部由 Screen 注入；
 * 批量加条目 / 右键移除 / 供应模式弹窗的写回（{@code StockSettingPacket}）由 Screen 完成，组件不发包。
 *
 * <pre>{@code
 * StockConfigPanel panel = StockConfigPanel.of()
 *     .filterBar(stockFilterBar)
 *     .rows(stockRowNodes)                 // 每行 = ItemGrid + 容量条 + 供应模式 SegmentToggle
 *     .capacitySlider(capacitySlider)
 *     .batchAddButton(batchAddBtn);
 * }</pre>
 *
 * <p><b>待服务端</b>：按词缀聚合条目（{@code StockEntry} 仅精确 ItemResource）——筛选 UI 与
 * {@code Filter} 接口预留，数据层谓词就绪后接上；物品栏取放（{@code TakePutPacket}）同理只做骨架。
 *
 * <p><b>潜在复用方</b>：StockScreen / 任何「容器规则配置」（标签页化的存量、物流塔规则、仓储策略）。
 */
public class StockConfigPanel extends Node {

    private final Node filterMount = new Node().id("stock-config-filter");
    private final Node rowsMount = new Node().column().gap(2).id("stock-config-rows");
    private final Node bottomMount = new Node().row().gap(2).id("stock-config-bottom");

    public StockConfigPanel() {
        this.id = "stock-config-panel";
        column().gap(2);

        rowsMount.flex.grow = 1;

        add(filterMount);
        add(rowsMount);
        add(bottomMount);
    }

    public static StockConfigPanel of() {
        return new StockConfigPanel();
    }

    // ------------------------------------------------------------------
    // 装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /** 顶部筛选栏（注入；类别 / 品质 / 词缀筛选项由 Screen 配好）。 */
    public StockConfigPanel filterBar(FilterBar<?> bar) {
        filterMount.clearChildren();
        if (bar != null) filterMount.add(bar);
        return this;
    }

    /**
     * 条目行列表（注入预建好的行节点）。每行 = 物品格 + 容量 ValueBar + 供应模式 SegmentToggle。
     * 行数量随筛选/增删动态变化，由 Screen 重新调用本方法刷新。
     */
    public StockConfigPanel rows(List<Node> rows) {
        rowsMount.clearChildren();
        if (rows != null) {
            for (Node r : rows) rowsMount.add(r);
        }
        if (rows == null || rows.isEmpty()) {
            rowsMount.add(CoeUi.emptyHint());
        }
        return this;
    }

    /** 底部容量滑杆（注入）。 */
    public StockConfigPanel capacitySlider(LabeledSlider slider) {
        bottomMount.clearChildren();
        if (slider != null) {
            slider.flex.grow = 1;
            bottomMount.add(slider);
        }
        return this;
    }

    /** 底部「批量加条目」按钮（注入）：点击把当前筛选结果批量建条目，写回由 Screen 完成。 */
    public StockConfigPanel batchAddButton(Button b) {
        if (b == null) return this;
        b.flex.shrink = 0;
        bottomMount.add(b);
        return this;
    }
}
