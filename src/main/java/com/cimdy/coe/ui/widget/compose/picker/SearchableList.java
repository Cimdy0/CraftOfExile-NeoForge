package com.cimdy.coe.ui.widget.compose.picker;

import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 可搜索列表：搜索框 + 列表的复合组件。把「输入框变化 → 过滤列表」这件每个列表界面都手写一遍的
 * 接线工作收进组件，Screen 只声明「怎么算一条命中」即可。
 *
 * <p>这是库内的<b>复合组件</b>（搭在 {@link ListView} + {@link TextInput} 之上），仍完全数据无关：
 * 模板、binder、匹配规则全部由调用方注入，组件本身不认识任何业务类型。
 *
 * <pre>{@code
 * // 1) 按某字段做子串匹配（最常用）
 * SearchableList<TeleportTarget> list = new SearchableList<>(CoeUi.listRow(), this::bindRow)
 *         .textOf(t -> t.label().getString())
 *         .itemGap(2).contentPadding(CoeUi.PAD).emptyView(CoeUi.emptyHint());
 *
 * // 2) 自定义匹配（比如物品要按注册名/ tooltip 检索）
 * SearchableList<ItemStack> picker = new SearchableList<>(cell, this::bindCell)
 *         .match((query, stack) -> ItemSearch.matchesInput(query, stack))
 *         .grid();
 * list.items(allTargets);
 * }</pre>
 *
 * <p>搜索框默认显示；小列表可用 {@link #showSearch(boolean)} 隐藏，此时它退化为一个普通
 * {@link ListView} 但接口一致。
 */
public class SearchableList<T> extends Node {

    private final ListView<T> list;
    private final TextInput search;

    /** 显式匹配器：拿到原始输入串与条目，返回是否命中。优先级高于 {@link #toStringer}。 */
    private BiPredicate<String, T> matcher;
    /** 省事匹配器：条目 → 文本，再用「query 子串」判定（不区分大小写）。 */
    private Function<T, String> toStringer;

    public SearchableList(Node template, ListView.Binder<T> binder) {
        this.id = "searchable-list";
        column();

        search = new TextInput().styled()
                .placeholder(Component.translatable("input_box.coe.input"));
        search.flex.height = 12;
        search.flex.margin.set(1, 3, 3, 3);
        search.onChange(this::onQuery);

        list = new ListView<>(template, binder);
        list.flex.grow = 1;

        add(search);
        add(list);
    }

    // ------------------------------------------------------------------
    // 匹配规则
    // ------------------------------------------------------------------

    /** 自定义命中判定（输入串原样传入，大小写等由调用方决定）。 */
    public SearchableList<T> match(BiPredicate<String, T> m) {
        this.matcher = m;
        this.toStringer = null;
        return this;
    }

    /** 按条目的某个文本字段做子串匹配（不区分大小写）。更省事的写法。 */
    public SearchableList<T> textOf(Function<T, String> textProvider) {
        this.toStringer = textProvider;
        this.matcher = null;
        return this;
    }

    /** 修复批 5：程序化聚焦内部搜索框（Screen 侧 Ctrl+F 调用）。 */
    public SearchableList<T> focusSearch() {
        search.focus();
        return this;
    }

    private void onQuery(String text) {
        String q = text == null ? "" : text;
        if (q.isEmpty()) {
            list.filter(null);
            return;
        }
        if (matcher != null) {
            list.filter(item -> matcher.test(q, item));
        } else if (toStringer != null) {
            String lc = q.toLowerCase(Locale.ROOT);
            list.filter(item -> {
                String t = toStringer.apply(item);
                return t != null && t.toLowerCase(Locale.ROOT).contains(lc);
            });
        } else {
            // 没声明任何匹配规则：退化为不过滤
            list.filter(null);
        }
    }

    // ------------------------------------------------------------------
    // 委托给内部 ListView
    // ------------------------------------------------------------------

    public SearchableList<T> items(List<T> items) { list.items(items); return this; }

    /** 开启内置滚动条（物品多 / 列表长时让滑块可见、可拖）。 */
    public SearchableList<T> scrollBar(boolean b) { list.scrollBar(b); return this; }

    public SearchableList<T> grid() { list.grid(); return this; }

    public SearchableList<T> itemGap(int px) { list.itemGap(px); return this; }

    public SearchableList<T> contentPadding(int px) { list.contentPadding(px); return this; }

    public SearchableList<T> emptyView(Node node) { list.emptyView(node); return this; }

    /** 重绑前自动复位行（见 {@link ListView#clearBeforeBind()}）。 */
    public SearchableList<T> clearBeforeBind() { list.clearBeforeBind(); return this; }

    public SearchableList<T> onRowHover(ListView.HoverHandler<T> h) { list.onRowHover(h); return this; }

    public SearchableList<T> selectable(boolean b) { list.selectable(b); return this; }

    public SearchableList<T> onSelect(ListView.SelectHandler<T> h) { list.onSelect(h); return this; }

    /** 取内部列表做更细的接线（如主从联动）。 */
    public ListView<T> list() { return list; }

    /** 是否显示顶部搜索框。 */
    public SearchableList<T> showSearch(boolean b) {
        search.visible(b);
        return this;
    }
}
