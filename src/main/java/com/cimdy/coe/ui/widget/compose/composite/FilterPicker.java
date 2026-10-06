package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.compose.filter.Filter;
import com.cimdy.coe.ui.widget.compose.filter.FilterBar;
import com.cimdy.coe.ui.widget.compose.picker.Pager;
import com.cimdy.coe.ui.widget.core.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 候选选择器（第 2 层复合组件，计划 §二 通用复合组件表）：
 * 「筛选栏 {@link FilterBar} + 候选列表 {@link Pager}（网格或列表）+ 选中回调」。
 *
 * <p>是 {@link com.cimdy.coe.ui.widget.compose.picker.ItemPicker} 的泛化升级：
 * 物品候选只是特例，这里候选类型任意（Screen 注入 {@code row} 行模板），
 * 并且在搜索/网格之外加了通用筛选与分页，用于「按类别/品质/词缀筛选后再挑选」的场景。
 *
 * <pre>{@code
 * FilterPicker<ItemStack> picker = FilterPicker.<ItemStack>of()
 *     .candidates(allItems)
 *     .row(stack -> itemRow(stack))
 *     .filter(categoryFilter)          // 可选
 *     .pageSize(40)
 *     .onPick(stack -> entry.setResource(stack));
 * }</pre>
 *
 * <p>管线：原始 {@link #candidates} 经 {@link Filter} 谓词过滤后交给 {@link Pager} 分页展示；
 * 选中（onPick）由 {@link Pager} 的行选中上抛。组件不认识任何业务类型、不发包。
 *
 * <p><b>潜在复用方</b>：StockConfigPanel（批量加条目的候选）、切割界面（按品质筛宝石）、
 * CodexDB（类别筛选后浏览）、任何「先筛选再从候选里挑一个」的弹窗/面板。
 *
 * @param <T> 候选类型
 */
public class FilterPicker<T> extends Node {

    private List<T> source = List.of();
    private Function<T, Node> rowFactory;
    private Filter<T> filter;
    private Consumer<T> onPick;

    private final Node filterMount = new Node().id("filter-picker-filter");
    private final Pager<T> pager;
    private FilterBar<T> filterBar;

    public FilterPicker() {
        this.id = "filter-picker";
        column();

        Node shell = new Node().row().id("filter-picker-row-shell");
        pager = new Pager<>(shell, this::bindRow);
        pager.clearBeforeBind()
                .itemGap(2)
                .contentPadding(CoeUi.PAD)
                .scrollBar(true)
                .selectable(true)
                .emptyView(CoeUi.emptyHint());
        pager.flex.grow = 1;
        pager.onSelect((item, index) -> { if (onPick != null) onPick.accept(item); });

        add(filterMount);
        add(pager);
    }

    public static <T> FilterPicker<T> of() {
        return new FilterPicker<>();
    }

    // ------------------------------------------------------------------
    // 装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /** 注入全部候选（未过滤前的原始集合）。 */
    public FilterPicker<T> candidates(List<T> items) {
        this.source = items == null ? List.of() : items;
        rebuild();
        return this;
    }

    /** 行模板工厂：给定一个候选，返回它的行/格节点（内部 {@link Pager} 克隆）。 */
    public FilterPicker<T> row(Function<T, Node> rowFactory) {
        this.rowFactory = rowFactory;
        rebuild();
        return this;
    }

    /** 可选：挂载筛选栏。 */
    public FilterPicker<T> filter(Filter<T> f) {
        this.filter = f;
        filterMount.clearChildren();
        filterBar = null;
        if (f != null) {
            filterBar = FilterBar.<T>of().filter(f);
            filterMount.add(filterBar);
        }
        rebuild();
        return this;
    }

    /** 选中回调（点击某候选）。 */
    public FilterPicker<T> onPick(Consumer<T> pick) {
        this.onPick = pick;
        return this;
    }

    /** 每页条数（{@link Pager} 复用）。 */
    public FilterPicker<T> pageSize(int n) {
        pager.pageSize(n);
        return this;
    }

    /** 取内部分页器做网格模式 / 行 hover 等细调。 */
    public Pager<T> pager() { return pager; }

    /** 取内部筛选栏（chip 切换后由调用方 {@link #refresh()}）。 */
    public FilterBar<T> filterBar() { return filterBar; }

    /** 外部触发重跑筛选 + 分页。 */
    public void refresh() { rebuild(); }

    // ------------------------------------------------------------------

    private void bindRow(Node view, T item, int index) {
        view.reset();
        if (rowFactory == null) return;
        Node row = rowFactory.apply(item);
        if (row != null) view.add(row);
    }

    private void rebuild() {
        List<T> work = new ArrayList<>(source);
        if (filter != null) {
            work.removeIf(item -> !filter.test(item));
        }
        pager.items(work);
    }
}
