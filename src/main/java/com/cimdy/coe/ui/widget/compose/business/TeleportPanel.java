package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.compose.EnergyBar;
import com.cimdy.coe.ui.widget.compose.composite.SortableList;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * 传送面板（业务复合组件，计划 §二 业务复合组件表 / §3.1 TeleportScreen）。
 *
 * <p>装配顺序（自上而下）：
 * <ol>
 *   <li>{@link EnergyBar}：能量余额 / 本次花费（顶部，低值阈值变色由功能组件负责）；</li>
 *   <li>{@link SortableList}：目标列表，支持按 花费 / 名称 / 距离 排序（{@link Sortable} 表头），
 *       行由 Screen 用 {@code CostLine}（绿/红着色）+「本维」角标徽章 {@code Decor.badge} 构建；</li>
 *   <li>脚注：维度统计小字（如「同维 x · 异维 y」，SCALE_META）。</li>
 * </ol>
 *
 * <p>组件只布局与上抛：目标数据、行模板、排序键、CostLine、本维徽章全部由 Screen 注入/配置；
 * 点击目标经 {@link #onSelect} 上抛，发包（{@code TeleportRequestPacket}）由 Screen 完成。
 *
 * <pre>{@code
 * TeleportPanel<Target> panel = TeleportPanel.<Target>of()
 *     .energy(energyBar)
 *     .targets(targets)
 *     .footer(Component.translatable("hold.coe.teleport.dim_summary", sameDim, otherDim))
 *     .onSelect(this::requestTeleport);
 * panel.list()
 *     .sortKeys(teleportSortKeys)
 *     .row(this::buildTargetRow);   // CostLine + 本维 Decor.badge
 * }</pre>
 *
 * <p><b>潜在复用方</b>：TeleportScreen / 未来任何传送类入口（石碑、回程、跨维锚点）。
 *
 * @param <T> 目标类型（Screen 自定，如 {@code TeleportTarget} 的视图包装）
 */
public class TeleportPanel<T> extends Node {

    private final Node energyMount = new Node().id("teleport-panel-energy");
    private final SortableList<T> list;
    private final Node footerMount = new Node().id("teleport-panel-footer");

    public TeleportPanel() {
        this.id = "teleport-panel";
        column().gap(2);

        list = SortableList.<T>of();
        list.flex.grow = 1;

        add(energyMount);
        add(list);
        add(footerMount);
    }

    public static <T> TeleportPanel<T> of() {
        return new TeleportPanel<>();
    }

    // ------------------------------------------------------------------
    // 装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /** 顶部能量条（注入）。 */
    public TeleportPanel<T> energy(EnergyBar bar) {
        energyMount.clearChildren();
        if (bar != null) energyMount.add(bar);
        return this;
    }

    /** 目标数据（推入内部 SortableList；行模板/排序键经 {@link #list()} 配置）。 */
    public TeleportPanel<T> targets(List<T> rows) {
        list.items(rows);
        return this;
    }

    /** 底部维度统计脚注小字。 */
    public TeleportPanel<T> footer(Component dimSummary) {
        footerMount.clearChildren();
        if (dimSummary == null) return this;
        TextRendererI text = Renderers.text(dimSummary)
                .color(Theme.current().placeholderText())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE);
        text.fixedScale(CoeUi.SCALE_META);
        footerMount.with(text);
        return this;
    }

    /** 选中目标上抛（发包由 Screen 完成）。 */
    public TeleportPanel<T> onSelect(Consumer<T> select) {
        list.onSelect(select);
        return this;
    }

    /**
     * 取内部 {@link SortableList} 配置行模板（CostLine + 本维 {@code Decor.badge}）与排序键。
     * 这是规格外但必要的扩展点：行渲染是 Screen 的业务（认识 {@code TeleportTarget}）。
     */
    public SortableList<T> list() { return list; }
}
