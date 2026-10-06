package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.client.ItemSearch;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.compose.codex.NavTree;
import com.cimdy.coe.ui.widget.compose.codex.CodexSchema;
import com.cimdy.coe.ui.widget.compose.codex.CodexSource;
import com.cimdy.coe.ui.widget.compose.composite.DetailPanel;
import com.cimdy.coe.ui.widget.compose.composite.SortableList;
import com.cimdy.coe.ui.widget.compose.item.ObfuscatedTooltip;
import com.cimdy.coe.ui.widget.compose.search.SearchLevel;
import com.cimdy.coe.ui.widget.component.button.SegmentToggle;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 图鉴浏览器复合组件 v2（冻结契约 §5）：
 * <b>左 {@link NavTree}（140px）→ 中 {@link SortableList} → 右 SegmentToggle tab 条 +
 * {@link DetailPanel} / affixView</b>。
 *
 * <p>相对 M1「左源按钮+徽章」三栏的差异：
 * <ul>
 *   <li><b>左导航</b>：删除旧的「源按钮+计数徽章」纵列，改为注入式 {@link NavTree}（二级折叠分类树，
 *       由 Screen 装配数据；本组件只负责挂载并接管其 {@link NavTree#onSelect}）；</li>
 *   <li><b>中列表</b>：仍是 {@link SortableList}{@code <Object>}，按当前 {@link CodexSource#schema} 渲染行；
 *       二级叶子选中时按 {@link CodexSource#subGroupKey(Object)} 在内部过滤条目（数据无关）；</li>
 *   <li><b>右栏</b>：顶部 {@link SegmentToggle} tab 条（详情/词缀），下方容器在
 *       {@link DetailPanel} 与注入的 {@link #affixView} 之间切换可见性。</li>
 * </ul>
 *
 * <p>换源 / 换二级叶子 / 换 tab 时，中列表与右内容整体淡入 150ms（{@link AnimatedNode#fadeIn}）。
 *
 * <p>数据契约（冻结）：组件只持有 {@link CodexSource} 抽象，不知道具体业务类型；
 * 二级叶子 {@link NavTree.Leaf} 的语义（选哪个源、记录哪个装备类型给词缀 tab）由 Screen
 * 在 {@link #onNavSelect} 回调里解释——本组件只上抛叶子并据此过滤中列表。
 *
 * <p>潜在复用方：CodexScreen（唯一当前消费者）；未来任何「分类树 + 源列表 + 明细/关联视图」型浏览。
 */
public class CodexBrowser extends AnimatedNode {

    private static final int NAV_WIDTH = 140;
    private static final int RIGHT_WIDTH = 200;
    /** 换源 / 换 tab / 换二级叶子的淡入时长（规格 §5/§9：150ms）。 */
    private static final int FADE_MS = 150;

    public static CodexBrowser of() { return new CodexBrowser(); }

    private final List<CodexSource<?>> sources = new ArrayList<>();

    // ---- 三栏容器 ----
    /** 左：注入式 NavTree 挂载点。 */
    private final Node leftCol = new Node().column();
    /** 中：列表外层 AnimatedNode，换源/换叶子整体 fadeIn(150)。 */
    private final AnimatedNode middle = new AnimatedNode();
    private SortableList<Object> list;
    /** 右：tab 条挂载点。 */
    private final Node tabsMount = new Node().row();
    /** 右：详情/词缀容器（下方在 detail 与 affixView 间切换可见性）。 */
    private final AnimatedNode rightContent = new AnimatedNode();
    private final DetailPanel detail = new DetailPanel();

    // ---- 注入件 / 回调 ----
    private NavTree navTree;
    private NavTree.Leaf currentLeaf;
    private Node affixView;
    private SegmentToggle tabs;
    private int selectedTab;

    private Consumer<CodexSource<?>> sourceSelectCb;
    private Consumer<NavTree.Leaf> navSelectCb;
    private IntConsumer tabSelectCb;
    /** 物品行 tooltip 混淆谓词（默认恒 false；命中 → {@link ObfuscatedTooltip#obfuscate}）。 */
    private Predicate<Object> obfPred;
    private CodexSource<?> current;
    /** 分层搜索匹配范围：全层（CATEGORY+ENTRY+DETAIL，默认）或仅条目层。 */
    private Scope scope = Scope.ALL;
    /** 中列：匹配范围切换条（仅条目/全层）。 */
    private final Node scopeBar = new Node().row().gap(4);
    /** 外部物品搜索器（冻结签名接入点；物品类源的匹配仍走 {@link CodexSource#matches}）。 */
    private ItemSearch externalSearch;

    /** 分层搜索范围。 */
    private enum Scope { ENTRY_ONLY, ALL }

    private CodexBrowser() {
        id("codex-browser");
        row().gap(4);
        padding(4);
        with(Theme.panel());

        // ---- 左：NavTree 挂载点（140px）----
        leftCol.width(NAV_WIDTH);
        leftCol.with(Theme.panel());
        Node navPlaceholder = new Node();
        navPlaceholder.with(Renderers.text(Component.empty())
                .align(Align.LEFT).vAlign(VAlign.TOP)
                .color(Theme.current().placeholderText()));
        leftCol.add(navPlaceholder);

        // ---- 中：匹配范围切换条 + SortableList ----
        middle.grow(1);
        middle.column().gap(2);
        SegmentToggle scopeToggle = SegmentToggle.of(
                Component.translatable("ui.coe.search.scope_entry_only"),
                Component.translatable("ui.coe.search.scope_all"));
        scopeToggle.onSelect(i -> {
            scope = i == 0 ? Scope.ENTRY_ONLY : Scope.ALL;
            list.refresh();
        });
        scopeToggle.select(1); // 默认全层
        scopeBar.add(scopeToggle);

        list = SortableList.of();
        list.grow(1);
        list.emptyHint(Component.translatable("ui.coe.search.no_match"));
        middle.add(scopeBar);
        middle.add(list);

        // ---- 右：tab 条 + 详情/词缀 ----
        Node rightCol = new Node().column().gap(2);
        rightCol.width(RIGHT_WIDTH);
        rightContent.grow(1);
        rightContent.column();
        detail.grow(1);
        rightContent.add(detail);
        rightCol.add(tabsMount);
        rightCol.add(rightContent);

        add(leftCol);
        add(middle);
        add(rightCol);
    }

    // ------------------------------------------------------------------
    // 冻结签名 §5 —— 保留
    // ------------------------------------------------------------------

    /** 注册一个数据源（装备条目 / 技能条目 / 附魔 / 掉落权重 …）。 */
    public <T> CodexBrowser registerSource(CodexSource<T> source) {
        if (source != null) {
            sources.add(source);
            if (current == null) selectSource(source.id());
        }
        return this;
    }

    /** 选中源回调上抛（Screen 可联动标题栏 / 其它面板）。 */
    public CodexBrowser onSourceSelect(Consumer<CodexSource<?>> cb) {
        this.sourceSelectCb = cb;
        return this;
    }

    /** 外部物品搜索器接入点（冻结签名）：物品类源查询匹配；实际过滤经当前 {@link CodexSource#matches}。 */
    @SuppressWarnings("rawtypes")
    public CodexBrowser search(ItemSearch search) {
        this.externalSearch = search;
        return this;
    }

    /** 按 id 切换数据源，重建中列表并淡入 150ms。 */
    public CodexBrowser selectSource(String id) {
        for (CodexSource<?> s : sources) {
            if (s.id().equals(id)) {
                applySource(s);
                return this;
            }
        }
        return this;
    }

    // ------------------------------------------------------------------
    // 冻结签名 §5 —— v2 新增
    // ------------------------------------------------------------------

    /**
     * 注入左侧二级分类树（Screen 构造；本组件内部挂载并接管其 {@link NavTree#onSelect}）。
     * 叶子选中时：本组件先按二级键过滤中列表，再把叶子上抛给 {@link #onNavSelect} 回调。
     */
    public CodexBrowser navTree(NavTree tree) {
        this.navTree = tree;
        leftCol.clearChildren();
        leftCol.add(tree);
        tree.onSelect(this::onNavLeaf);
        return this;
    }

    /** 二级叶子选中上抛（Screen 据此过滤列表 / 记录装备类型联动右栏词缀 tab）。 */
    public CodexBrowser onNavSelect(Consumer<NavTree.Leaf> cb) {
        this.navSelectCb = cb;
        return this;
    }

    /** 右侧 {@link SegmentToggle} tab 条（如 详情/词缀）；切换时右内容淡入 150ms。 */
    public CodexBrowser tabs(Component... names) {
        tabs = SegmentToggle.of(names);
        tabs.onSelect(i -> showTab(i, true));
        tabsMount.clearChildren();
        tabsMount.add(tabs);
        return this;
    }

    /** tab 切换上抛（词缀 tab 仅装备类型选中时有意义）。 */
    public CodexBrowser onTabSelect(IntConsumer cb) {
        this.tabSelectCb = cb;
        return this;
    }

    /** 程序化切 tab（同步切换 detail/affixView 可见性，并上抛回调以便 Screen 填充词缀视图）。 */
    public CodexBrowser selectedTab(int i) {
        showTab(i, true);
        return this;
    }

    /** 词缀 tab 内容注入点（Screen 渲染 poe2db 风格分组列表）。 */
    public CodexBrowser affixView(Node node) {
        if (this.affixView != null) rightContent.remove(this.affixView);
        this.affixView = node;
        rightContent.add(node);
        node.grow(1);
        node.visible(selectedTab != 0);
        return this;
    }

    /** 物品行 tooltip 混淆（默认恒 false；命中 → {@link ObfuscatedTooltip#obfuscate} 改写详情行）。 */
    public CodexBrowser obfuscateIf(Predicate<Object> p) {
        this.obfPred = p;
        return this;
    }

    // ------------------------------------------------------------------
    // 内部装配
    // ------------------------------------------------------------------

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void applySource(CodexSource<?> source) {
        current = source;
        if (sourceSelectCb != null) sourceSelectCb.accept(source);

        CodexSchema<Object> schema = (CodexSchema<Object>) source.schema();
        CodexSource<Object> src = (CodexSource<Object>) source;
        List<Object> entries = (List<Object>) source.entries();

        list.row(entry -> buildRow(schema, entry));
        // 分层搜索：逐条按 CATEGORY→ENTRY→DETAIL 判定取最高命中层（受匹配范围约束）。
        list.layeredSearch((query, entry) -> hitLevel(src, entry, query));
        list.items(filterByLeaf(entries));
        list.onSelect(this::showDetail);

        detail.clear();
        middle.fadeIn(FADE_MS); // 换源淡入 150ms（规格 §5）
    }

    /**
     * 命中层判定（计划 979-991）：对单条目按 CATEGORY→ENTRY→DETAIL 顺序问 {@code src.matches}，
     * 命中即返回该层（取最高层）；都不命中返回 {@code null}（该条目不入选）。
     * 范围为「仅条目层」时跳过 CATEGORY/DETAIL，只判 ENTRY。
     *
     * <p>说明：行内高亮依赖 TextRenderer 行内着色片段；当前 TextRenderer 不支持，
     * 故以「命中层徽标 + 按层分组排序」表达命中（javadoc 注明，后续 TextRenderer 支持行内片段高亮再升级）。
     */
    private SearchLevel hitLevel(CodexSource<Object> src, Object entry, String query) {
        if (query == null || query.isEmpty()) return SearchLevel.ENTRY;
        if (scope == Scope.ALL && src.matches(entry, query, SearchLevel.CATEGORY)) return SearchLevel.CATEGORY;
        if (src.matches(entry, query, SearchLevel.ENTRY)) return SearchLevel.ENTRY;
        if (scope == Scope.ALL && src.matches(entry, query, SearchLevel.DETAIL)) return SearchLevel.DETAIL;
        return null;
    }

    /**
     * 二级叶子选中：本组件先按当前源的 {@link CodexSource#subGroupKey} 过滤中列表，
     * 再把叶子原样上抛给 Screen（Screen 负责解释语义：切源 / 记录装备类型给词缀 tab）。
     */
    private void onNavLeaf(NavTree.Leaf leaf) {
        this.currentLeaf = leaf;
        if (current != null) {
            List<Object> entries = (List<Object>) current.entries();
            list.items(filterByLeaf(entries));
            detail.clear();
        }
        if (navSelectCb != null) navSelectCb.accept(leaf);
        middle.fadeIn(FADE_MS); // 换二级叶子淡入 150ms
    }

    /**
     * 按当前二级叶子过滤条目（数据无关）：
     * <ul>
     *   <li>无叶子 / 叶子 id == 当前源 id（= 该组「全部」叶）→ 不过滤；</li>
     *   <li>条目的二级键为空（源不细分，如装备条目无 type 映射）→ 恒保留（不编造映射）；</li>
     *   <li>否则只保留二级键 == 叶子 id 的条目。</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private List<Object> filterByLeaf(List<Object> entries) {
        if (current == null || currentLeaf == null) return entries;
        String leafId = currentLeaf.id();
        if (leafId.equals(current.id())) return entries; // 「全部」叶
        List<Object> out = new ArrayList<>();
        CodexSource<Object> src = (CodexSource<Object>) current;
        for (Object e : entries) {
            Optional<String> sg = src.subGroupKey(e);
            if (sg.isEmpty() || sg.get().equals(leafId)) out.add(e);
        }
        return out;
    }

    /** 切换右栏可见视图：tab 0 = DetailPanel，其余 = affixView（规格：详情/词缀）。 */
    private void showTab(int i, boolean fireEvent) {
        this.selectedTab = i;
        boolean affix = i != 0;
        detail.visible(!affix);
        if (affixView != null) affixView.visible(affix);
        if (tabs != null) tabs.select(i); // select 不触发 onSelect，避免递归
        rightContent.fadeIn(FADE_MS);     // 换 tab 内容淡入 150ms（规格 §5）
        if (fireEvent && tabSelectCb != null) tabSelectCb.accept(i);
    }

    /** 按当前 schema 的 icon/label 造一行。 */
    private Node buildRow(CodexSchema<Object> schema, Object entry) {
        Node row = new Node().row().gap(4);
        row.height(16);

        Node icon = new Node();
        icon.size(16, 16);
        ItemStack ic = schema.icon(entry);
        if (ic != null && !ic.isEmpty()) {
            ItemStack[] ref = { ic };
            Supplier<ItemStack> sup = () -> ref[0];
            icon.with(Renderers.item(sup));
        }

        TextRendererI name = Renderers.text(schema.label(entry))
                .align(Align.LEFT).vAlign(VAlign.MIDDLE)
                .color(Theme.current().text());
        Node nameNode = new Node();
        nameNode.grow(1);
        nameNode.with(name);

        row.add(icon);
        row.add(nameNode);
        return row;
    }

    /** 选中条目 → 右 DetailPanel 逐行渲染 schema.fields；命中混淆谓词时改写 tooltip。 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void showDetail(Object entry) {
        if (current == null) { detail.clear(); return; }
        CodexSchema<Object> schema = (CodexSchema<Object>) current.schema();
        List<Component> lines = new ArrayList<>();
        for (CodexSchema.Field<Object> f : schema.fields()) {
            lines.add(f.label().copy().append(": ").append(f.value().apply(entry)));
        }
        // 物品 tooltip 混淆（默认恒 false；命中 → 未鉴定样式：名遮蔽 + 词缀占位灰显）
        if (obfPred != null && obfPred.test(entry)) {
            lines = ObfuscatedTooltip.obfuscate(lines);
        }
        detail.show(lines);
    }
}
