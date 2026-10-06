package com.cimdy.coe.ui.widget.compose.picker;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * 分页列表：顶部「上一页 / 页码 / 下一页」工具条 + 下方列表的复合组件。
 *
 * <p>外头只管喂整份数据 {@link #items(List)} 与每页条数，翻页、切片、页码文本全部内部维护。
 * 把 {@code TerminalScreen} 里手写的那套 {@code page / PAGE_SIZE / pageSlice() / changePage() /
 * updatePageText() / prev-next 按钮栏} 收进组件，分页界面从此零样板。
 *
 * <p>依旧数据无关：模板与 binder 由调用方注入，连「页码文字」也只是内部一个 {@link TextRenderer}。
 *
 * <pre>{@code
 * Pager<SaveEntity> pager = new Pager<>(gridCell(), this::bindCell)
 *         .pageSize(42)
 *         .grid().itemGap(3).contentPadding(3)
 *         .onRowHover((item, idx) -> showInfo(...));
 * pager.items(allEntities);
 * }</pre>
 */
public class Pager<T> extends Node {

    private final ListView<T> list;
    private final TextRendererI pageText;
    private final Button prev;
    private final Button next;

    private List<T> source = Collections.emptyList();
    private int pageSize = 40;
    private int page = 1;

    public Pager(Node template, ListView.Binder<T> binder) {
        this.id = "pager";
        column();

        // ---- 工具条 ----
        Node bar = new Node().row().gap(2);
        bar.id = "pager-bar";
        bar.flex.height = 12;
        bar.padding(1, 3);
        bar.with(Theme.pagerBar());

        prev = Button.of("<").styled();
        prev.flex.size(10, 10);
        prev.onClick(() -> changePage(-1));

        pageText = Renderers.text(Component.literal("1/1"))
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE);
        Node pageLabel = new Node().with(pageText);
        pageLabel.flex.grow = 1;

        next = Button.of(">").styled();
        next.flex.size(10, 10);
        next.onClick(() -> changePage(1));

        bar.add(prev);
        bar.add(pageLabel);
        bar.add(next);

        // ---- 列表 ----
        list = new ListView<>(template, binder);
        list.flex.grow = 1;

        add(bar);
        add(list);
    }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    public Pager<T> pageSize(int n) { this.pageSize = Math.max(1, n); return this; }

    public Pager<T> items(List<T> items) {
        this.source = items == null ? Collections.emptyList() : items;
        this.page = 1;
        apply();
        return this;
    }

    /** 当前页（1 基）。 */
    public int page() { return page; }

    public int pageCount() {
        return Math.max(1, (source.size() + pageSize - 1) / pageSize);
    }

    private List<T> pageSlice() {
        int size = source.size();
        int maxPage = pageCount();
        page = Math.clamp(page, 1, maxPage);
        int from = (page - 1) * pageSize;
        int to = Math.min(size, from + pageSize);
        return from >= size ? List.of() : source.subList(from, to);
    }

    private void changePage(int delta) {
        int maxPage = pageCount();
        int nextPage = Math.clamp(page + delta, 1, maxPage);
        if (nextPage == page) return;
        page = nextPage;
        apply();
    }

    private void apply() {
        list.items(pageSlice());
        updatePageText();
    }

    private void updatePageText() {
        pageText.setLine(0, Component.literal(page + "/" + pageCount()));
    }

    // ------------------------------------------------------------------
    // 委托给内部 ListView
    // ------------------------------------------------------------------

    public Pager<T> scrollBar(boolean b) { list.scrollBar(b); return this; }

    public Pager<T> grid() { list.grid(); return this; }

    public Pager<T> itemGap(int px) { list.itemGap(px); return this; }

    public Pager<T> contentPadding(int px) { list.contentPadding(px); return this; }

    public Pager<T> emptyView(Node node) { list.emptyView(node); return this; }

    public Pager<T> clearBeforeBind() { list.clearBeforeBind(); return this; }

    public Pager<T> onRowHover(ListView.HoverHandler<T> h) { list.onRowHover(h); return this; }

    public Pager<T> selectable(boolean b) { list.selectable(b); return this; }

    public Pager<T> onSelect(ListView.SelectHandler<T> h) { list.onSelect(h); return this; }

    /** 取内部列表做更细的接线。 */
    public ListView<T> list() { return list; }
}
