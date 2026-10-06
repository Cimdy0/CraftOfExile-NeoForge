package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 主从联动复合组件：左侧列表 + 右侧详情面板，行被悬停/选中时详情随之刷新。
 *
 * <p>把「列表选中 → 详情更新」这条原本散落在各 Screen 里的接线收进组件。组件本身
 * <b>不认识任何业务类型</b>：列表项 {@code T} 由调用方提供，详情长什么样由
 * {@link #detail(Function)} 决定（返回一组 {@link Component}）。
 *
 * <p>典型用法（单列表驱动）：
 * <pre>{@code
 * MasterDetail<SaveEntity> md = new MasterDetail<>(cell(), this::bindCell)
 *     .grid().listGap(3).listPadding(4)
 *     .detail(se -> se.getEntityDisplayData(level))
 *     .onSelect(se -> spawn(se));
 * }</pre>
 *
 * <p>多列表共用一块详情区时（如终端界面 main/hold 两个列表都往同一块信息面板写），
 * 用 {@link #detailPanel()} 拿到那块面板，额外的列表也能 {@code detailPanel().show(...)}。
 */
public class MasterDetail<T> extends Node {

    private final ListView<T> list;
    private final DetailPanel detail;

    public MasterDetail(Node template, ListView.Binder<T> binder) {
        this.id = "master-detail";
        this.row();
        list = new ListView<>(template, binder).clearBeforeBind();
        detail = new DetailPanel();
        list.flex.grow = 1;
        detail.flex.grow = 1;
        add(list);
        add(detail);
    }

    // ---- 列表相关配置（透传到内部 ListView）----
    public MasterDetail<T> grid() { list.grid(); return this; }
    public MasterDetail<T> listGap(int px) { list.itemGap(px); return this; }
    public MasterDetail<T> listPadding(int px) { list.contentPadding(px); return this; }
    public MasterDetail<T> emptyView(Node node) { list.emptyView(node); return this; }
    public MasterDetail<T> scrollBar(boolean b) { list.scrollBar(b); return this; }
    public MasterDetail<T> selectable(boolean b) { list.selectable(b); return this; }
    public MasterDetail<T> items(List<T> src) { list.items(src); return this; }

    public MasterDetail<T> onSelect(BiConsumer<T, Integer> c) { list.onSelect(c::accept); return this; }
    public MasterDetail<T> onHover(BiConsumer<T, Integer> c) { list.onRowHover(c::accept); return this; }

    /**
     * 设置详情渲染：每当悬浮/选中的项变化，就用它把 {@code T} 转成一组文本行刷新详情面板。
     * 离开所有行（item 为 null）时清空。
     */
    public MasterDetail<T> detail(Function<T, List<Component>> renderer) {
        list.onRowHover((item, idx) ->
                detail.show(item == null ? List.of() : renderer.apply(item)));
        return this;
    }

    /** 直接拿到详情面板，方便第二列表等额外来源也往里写。 */
    public DetailPanel detailPanel() { return detail; }

    /** 内部列表（需要更细的绑定/事件控制时直接用）。 */
    public ListView<T> list() { return list; }
}
