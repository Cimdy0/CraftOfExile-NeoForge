package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.list.SortHeader;
import com.cimdy.coe.ui.widget.component.list.Sortable;
import com.cimdy.coe.ui.widget.component.list.Sortable.Direction;
import com.cimdy.coe.ui.widget.component.list.Sortable.SortKey;
import com.cimdy.coe.ui.widget.compose.filter.Filter;
import com.cimdy.coe.ui.widget.compose.filter.FilterBar;
import com.cimdy.coe.ui.widget.compose.picker.SearchableList;
import com.cimdy.coe.ui.widget.compose.search.SearchLevel;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 可搜索 + 可排序 + 可筛选列表（第 2 层复合组件，计划 §二 通用复合组件表）。
 *
 * <p>把「搜索框 + 列表」({@link SearchableList})、「可点击表头排序」({@link Sortable}/{@link SortHeader})
 * 与「通用筛选栏」({@link FilterBar}) 三种能力收成一处，Screen 只负责喂数据与行模板：
 *
 * <pre>{@code
 * SortableList<Row> list = SortableList.<Row>of()
 *     .sortKeys(List.of(
 *         new Sortable.SortKey<>("cost", Component.translatable("...cost"), Comparator.comparingDouble(Row::cost)),
 *         new Sortable.SortKey<>("name", Component.translatable("...name"), Comparator.comparing(r -> r.name().getString()))))
 *     .search((query, row) -> row.name().getString().toLowerCase().contains(query.toLowerCase()))
 *     .filter(categoryFilter)                 // 可选
 *     .row(row -> rowView(row))                // 行模板工厂（内部 ListView 克隆）
 *     .onSelect(this::open)
 *     .emptyHint(Component.translatable("ui.coe.empty"));
 * list.items(allRows);
 * }</pre>
 *
 * <p><b>叠加管线（先 search，再 filter，最后 sort）</b>：原始 {@code items} 依次经过
 * 搜索谓词（{@link #search}）→ 筛选谓词（{@link #filter}）→ 排序比较器（{@link Sortable#comparator()}），
 * 最终结果一次性推给内部列表。三者都是 AND / 全序关系，顺序无关但语义上按此叙述。
 *
 * <p>本组件<b>自身实现 {@link Sortable}</b>：排序状态（当前列 / 方向）住在这里，
 * {@link SortHeader} 点击列时回调 {@link #setSort}，组件据此重跑管线。
 *
 * <p><b>复用约定</b>：内部 {@link SearchableList} 仅借用其 {@link ListView} / 空态 / 选中 / 行 hover 管线，
 * 自带的搜索框被隐藏（{@code showSearch(false)}），搜索输入由本组件自有，以便编排叠加管线；
 * 行模板克隆沿用既有 Button 克隆模式（见 {@link com.cimdy.coe.ui.widget.compose.picker.ItemPicker}）。
 *
 * <p><b>待服务端 / 事件</b>：筛选栏 chip 由 {@link FilterBar} 内部切换后，调用方需在切换完成时
 * {@link #refresh()}（重跑管线）——功能组件 A 的 {@code FilterBar} 落地后可直接把其变更回调接到这里。
 *
 * <p><b>潜在复用方</b>：TeleportPanel（花费/名称/距离）、StockConfigPanel（条目排序）、
 * CodexBrowser（结果列表）、TerminalScreen（主区列表）——任何「搜索 × 排序 × 筛选」三合一界面。
 *
 * @param <T> 列表条目类型（数据无关，行模板由调用方注入）
 */
public class SortableList<T> extends Node implements Sortable<T> {

    private List<T> source = List.of();
    private Function<T, Node> rowFactory;
    /** 搜索谓词：(原始查询串, 条目) → 是否命中；null 表示不做搜索过滤。 */
    private BiPredicate<String, T> searchMatcher;
    /** 分层搜索：(原始查询串, 条目) → 命中层（CATEGORY/ENTRY/DETAIL）；null=不命中。优先于 {@link #searchMatcher}。 */
    private BiFunction<String, T, SearchLevel> layerMatcher;
    /** 当前查询下每条命中项的命中层（仅 query 非空且 layerMatcher 启用时填充）。 */
    private final IdentityHashMap<T, SearchLevel> hitLevels = new IdentityHashMap<>();
    private String query = "";
    private Filter<T> filter;
    private Consumer<T> onSelect;
    private Component emptyHint = Component.translatable("ui.coe.empty");

    // ---- Sortable 状态 ----
    private List<SortKey<T>> sortKeys = List.of();
    private int sortIndex = -1;
    private Direction sortDirection;

    // ---- 内部部件 ----
    private final TextInput searchBox;
    private final Node headerMount = new Node().id("sortable-list-header");
    private final Node filterMount = new Node().id("sortable-list-filter");
    private final SearchableList<T> engine;
    private SortHeader sortHeader;
    private FilterBar<T> filterBar;

    public SortableList() {
        this.id = "sortable-list";
        column();

        // 自有搜索框：查询串由本组件持有，参与叠加管线
        searchBox = CoeUi.searchBox();
        searchBox.onChange(q -> { this.query = q == null ? "" : q; rebuild(); });

        // 列表引擎：借用 SearchableList 的 ListView/空态/选中/hover 管线，隐藏其自带搜索框
        Node shell = new Node().row().id("sortable-row-shell");
        engine = new SearchableList<>(shell, this::bindRow);
        engine.showSearch(false)
                .clearBeforeBind()
                .itemGap(2)
                .contentPadding(CoeUi.PAD)
                .scrollBar(true)
                .selectable(true)
                .emptyView(CoeUi.emptyHint(emptyHint));
        engine.flex.grow = 1;
        engine.onSelect((item, index) -> { if (onSelect != null) onSelect.accept(item); });

        add(headerMount);
        add(filterMount);
        add(searchBox);
        add(engine);
    }

    public static <T> SortableList<T> of() {
        return new SortableList<>();
    }

    // ------------------------------------------------------------------
    // 数据与装配（规格 §2 签名）
    // ------------------------------------------------------------------

    /** 注入原始数据并立即重跑管线。 */
    public SortableList<T> items(List<T> items) {
        this.source = items == null ? List.of() : items;
        rebuild();
        return this;
    }

    /** 行模板工厂：给定一条数据，返回它的行节点（内部 ListView 会按行克隆并复用）。 */
    public SortableList<T> row(Function<T, Node> rowFactory) {
        this.rowFactory = rowFactory;
        rebuild();
        return this;
    }

    /**
     * 排序键定义：挂上可点击表头 {@link SortHeader}。传空列表 = 不显示表头。
     * 排序状态由本组件（{@link Sortable}）持有，表头点击回调 {@link #setSort}。
     */
    public SortableList<T> sortKeys(List<SortKey<T>> keys) {
        setSortKeys(keys);
        headerMount.clearChildren();
        sortHeader = null;
        if (keys != null && !keys.isEmpty()) {
            sortHeader = SortHeader.of(this, keys);
            headerMount.add(sortHeader);
        }
        rebuild();
        return this;
    }

    /**
     * 搜索谓词：(原始查询串, 条目) → 是否命中。等价规格里的 {@code ItemSearch<T>}
     * （既有 {@code com.cimdy.coe.client.ItemSearch} 是静态工具而非泛型接口，故以通用 BiPredicate 落地）。
     */
    public SortableList<T> search(BiPredicate<String, T> matcher) {
        this.searchMatcher = matcher;
        rebuild();
        return this;
    }

    /**
     * 分层搜索（计划 979-991）：(原始查询串, 条目) → 命中层。命中非 null 的条目入选，
     * 并按命中层分组排序（类别 → 条目 → 内容），行尾渲染对应层徽标。
     * 启用后优先于 {@link #search}（二选一；图鉴链路走本路）。
     */
    public SortableList<T> layeredSearch(BiFunction<String, T, SearchLevel> matcher) {
        this.layerMatcher = matcher;
        rebuild();
        return this;
    }

    /** 可选：挂载一个筛选栏（{@link FilterBar}）。筛选项即数据，由 Screen 注入。 */
    public SortableList<T> filter(Filter<T> f) {
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

    /** 行选中回调。 */
    public SortableList<T> onSelect(Consumer<T> select) {
        this.onSelect = select;
        return this;
    }

    /** 空态文案（默认 {@code ui.coe.empty}）。 */
    public SortableList<T> emptyHint(Component text) {
        this.emptyHint = text == null ? Component.translatable("ui.coe.empty") : text;
        engine.emptyView(CoeUi.emptyHint(this.emptyHint));
        return this;
    }

    /** 取内部搜索框做占位符等细调。 */
    public TextInput searchBox() { return searchBox; }

    /** 取内部列表引擎做更细接线（行 hover 等）。 */
    public SearchableList<T> engine() { return engine; }

    /**
     * 外部触发重跑管线：筛选栏 chip 切换后由调用方调用（功能 FilterBar 落地后可直接接其变更回调）。
     */
    public void refresh() { rebuild(); }

    // ------------------------------------------------------------------
    // Sortable 实现：排序状态 + Comparator 推导
    // ------------------------------------------------------------------

    @Override public void setSortKeys(List<SortKey<T>> keys) { this.sortKeys = keys == null ? List.of() : List.copyOf(keys); }
    @Override public List<SortKey<T>> sortKeys() { return sortKeys; }
    @Override public int sortIndex() { return sortIndex; }
    @Override public Direction sortDirection() { return sortDirection; }

    @Override
    public void setSort(int keyIndex, Direction dir) {
        this.sortIndex = keyIndex;
        this.sortDirection = dir;
        rebuild();
    }

    @Override
    public Comparator<T> comparator() {
        if (sortIndex < 0 || sortIndex >= sortKeys.size() || sortDirection == null) return null;
        Comparator<T> c = sortKeys.get(sortIndex).comparator();
        return sortDirection == Direction.DESC ? c.reversed() : c;
    }

    // ------------------------------------------------------------------
    // 管线：search → filter → sort，一次性推给引擎
    // ------------------------------------------------------------------

    private void bindRow(Node view, T item, int index) {
        view.reset();
        if (rowFactory == null) return;
        Node row = rowFactory.apply(item);
        if (row != null) {
            SearchLevel lvl = hitLevels.get(item);
            if (lvl != null) row.add(levelBadge(lvl)); // 行尾命中层徽标
            view.add(row);
        }
    }

    /** 命中层小徽标（类别/条目/内容）；取色走 Theme 令牌，文字走双语 key。 */
    private Node levelBadge(SearchLevel level) {
        Node b = new Node();
        Component label = switch (level) {
            case CATEGORY -> Component.translatable("ui.coe.search.level_category");
            case ENTRY -> Component.translatable("ui.coe.search.level_entry");
            case DETAIL -> Component.translatable("ui.coe.search.level_detail");
        };
        int color = switch (level) {
            case CATEGORY -> Theme.current().goldAccent();
            case ENTRY -> Theme.current().accent();
            case DETAIL -> Theme.current().textMuted();
        };
        b.with(Renderers.text(label).align(Align.CENTER).vAlign(VAlign.MIDDLE)
                .color(color).fixedScale(0.6f));
        return b;
    }

    private void rebuild() {
        List<T> work = new ArrayList<>(source);
        hitLevels.clear();
        boolean hasQuery = !query.isEmpty();

        // 1) 搜索（分层优先；否则走旧布尔谓词）
        if (hasQuery && layerMatcher != null) {
            List<T> kept = new ArrayList<>();
            for (T item : work) {
                SearchLevel lvl = layerMatcher.apply(query, item);
                if (lvl != null) {
                    kept.add(item);
                    hitLevels.put(item, lvl);
                }
            }
            work = kept;
            // 按命中层分组：CATEGORY(0) → ENTRY(1) → DETAIL(2)
            work.sort(Comparator.comparingInt(item -> hitLevels.getOrDefault(item, SearchLevel.ENTRY).ordinal()));
        } else if (searchMatcher != null && hasQuery) {
            work.removeIf(item -> !searchMatcher.test(query, item));
        }
        // 2) 筛选
        if (filter != null) {
            work.removeIf(item -> !filter.test(item));
        }
        // 3) 排序（分层查询已按层分组，不再叠加显式排序）
        Comparator<T> c = comparator();
        if (c != null && layerMatcher == null) work.sort(c);

        engine.items(work);
    }
}
