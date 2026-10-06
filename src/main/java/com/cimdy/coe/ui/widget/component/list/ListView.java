package com.cimdy.coe.ui.widget.component.list;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.FlexWrap;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * 列表视图：一个模板节点 + 一份数据，克隆出 N 行并逐行绑定。
 *
 * <p><b>泛型化是这里最重要的改动。</b>旧 {@code ListableWidget} 直接持有具体业务数据，
 * 换一种列表就得改库；现在数据类型是调用方的 {@code T}，lib 只负责「克隆 + 回调绑定」，
 * 对 {@code T} 一无所知。
 *
 * <pre>{@code
 * Node row = new Node().row().gap(4);
 * row.flex.height = 18;
 * row.with(Renderers.text("").align(LEFT).vAlign(MIDDLE));
 *
 * ListView<String> list = new ListView<>(row, (view, item, i) ->
 *         ((TextRenderer) view.visuals.getFirst()).setLine(0, Component.literal(item)));
 * list.items(List.of("A", "B", "C"));
 * }</pre>
 *
 * <p>用克隆而非虚拟滚动，是因为这类界面的行数量级在几十到几百，
 * 克隆的代价远小于虚拟滚动带来的「行状态漂移」问题（旧库列表滚动后 hover 错位就是这么来的）。
 */
public class ListView<T> extends Scrollable {

    /** 把一条数据绑定到一行视图上。 */
    @FunctionalInterface
    public interface Binder<T> {
        void bind(Node view, T item, int index);
    }

    /** 选中回调。 */
    @FunctionalInterface
    public interface SelectHandler<T> {
        void onSelect(T item, int index);
    }

    /** 悬浮回调：进入某行时给 (item, index)，离开所有行时给 (null, -1)。只在变化时触发一次。 */
    @FunctionalInterface
    public interface HoverHandler<T> {
        void onHover(T item, int index);
    }

    private final Node template;
    private final Binder<T> binder;

    private final List<Node> rows = new ArrayList<>();
    private List<T> source = Collections.emptyList();
    private List<T> visible = Collections.emptyList();

    private Predicate<T> filter;
    /** 固定列数；<=0 表示不固定（按内容自动换行）。 */
    private int columns = 0;
    /** 重绑前是否先 {@link Node#reset()} 行（清理上一轮残留的子节点/外观层）。 */
    private boolean clearBeforeBind = false;
    private boolean selectable = false;
    private int selectedIndex = -1;
    private final Property<T> value = new SimpleObservable<>(null);
    private SelectHandler<T> onSelect;
    private HoverHandler<T> onRowHover;
    private int lastHoverRow = -1;

    /** 数据为空时显示的占位节点。 */
    private Node emptyView;

    // ------------------------------------------------------------------
    // 挂载错峰（附录 D.1「List·Grid stagger mount」）
    //
    // 首次填充 / 过滤重建时条目依次淡入：错峰 20ms/项，单批 ≤12 条（超出者与末档同批出现，
    // 不无限拖延）。纯渲染 alpha —— 直接写各行 {@link AnimatedNode#anim()} 的 alpha，
    // 由 AnimatedNode.render 自读；不改布局时序、不改命中（行位置照常参与 Flex 布局）。
    //
    // 打断语义：每次 rebuild 重置 {@link #mountEpoch}，解析式按当前时钟重算 alpha ——
    // 「新一批打断旧一批」，不新建逐行补间对象、不排队堆积；动画关闭 / reduced-motion
    // 时 {@link AnimRegistry#animationsActive()} 为假，一律 alpha=1（静止终帧全显）。
    // ------------------------------------------------------------------

    /** 错峰间隔 ms/项（附录 D.1）。 */
    private static final int STAGGER_GAP_MS = 20;
    /** 单批错峰条目上限（超出者并入末档同时淡入）。 */
    private static final int STAGGER_MAX = 12;
    /** 最近一次结构性重建（一批挂载）的时钟戳。 */
    private long mountEpoch;

    public ListView(Node template, Binder<T> binder) {
        this.template = template;
        this.binder = binder;
        this.id = "list-view";
        column();
    }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    public ListView<T> items(List<T> items) {
        this.source = items == null ? Collections.emptyList() : items;
        rebuild();
        return this;
    }

    public List<T> items() { return visible; }

    /** 过滤器（如搜索框）。传 null 表示不过滤。 */
    public ListView<T> filter(Predicate<T> p) {
        this.filter = p;
        rebuild();
        return this;
    }

    /** 数据内容变了但条数没变时，只重新绑定，不重建节点。 */
    public void refresh() {
        for (int i = 0; i < rows.size() && i < visible.size(); i++) {
            if (clearBeforeBind) rows.get(i).reset();
            binder.bind(rows.get(i), visible.get(i), i);
        }
    }

    /**
     * 打开「重绑前自动复位行」。行模板克隆体被反复绑定时，每轮 binder 会重新叠外观 / 加子节点，
     * 不复位就会累积。打开后 binder 可放心直接写「{@code styled(); cell.with(...);}」，
     * 不必每轮手写 {@code clearVisuals();}。
     */
    public ListView<T> clearBeforeBind() {
        this.clearBeforeBind = true;
        return this;
    }

    /** 重建行节点（条数变化时）。 */
    public void rebuild() {
        visible = applyFilter(source);
        mountEpoch = Util.getMillis();   // 新一批挂载：错峰淡入从此刻重新计时（打断旧批）

        // 多退少补：尽量复用已有克隆体，避免每帧重新分配
        while (rows.size() > visible.size()) {
            Node r = rows.removeLast();
            remove(r);
        }
        while (rows.size() < visible.size()) {
            Node r = template.copy();
            r.id = "list-row";
            rows.add(r);
            add(r);
        }

        for (int i = 0; i < visible.size(); i++) {
            if (clearBeforeBind) rows.get(i).reset();
            binder.bind(rows.get(i), visible.get(i), i);
        }

        if (selectedIndex >= visible.size()) selectedIndex = -1;
        applySelectionState();
        syncEmptyView();
        lastHoverRow = -1;   // 数据变了，下一帧强制重触发一次 hover，让主从面板跟着刷新
        layoutContent();
    }

    private List<T> applyFilter(List<T> src) {
        if (filter == null) return src;
        List<T> out = new ArrayList<>(src.size());
        for (T t : src) if (filter.test(t)) out.add(t);
        return out;
    }

    // ------------------------------------------------------------------
    // 选中
    // ------------------------------------------------------------------

    public ListView<T> selectable(boolean b) { this.selectable = b; return this; }

    public ListView<T> onSelect(SelectHandler<T> h) { this.onSelect = h; value.addListener((o, n) -> h.onSelect(n, selectedIndex)); return this; }

    /** 悬浮联动（主从面板）：进入/离开行时回调，只在变化时触发。 */
    public ListView<T> onRowHover(HoverHandler<T> h) { this.onRowHover = h; return this; }

    /**
     * 网格模式：内容按行排布、自动换行，行高由模板决定。
     * 默认是纵向列表；调用后行会从左到右排满视口宽度再换行。
     *
     * <p>每行塞几个由「单元宽度 vs 视口宽度」决定 —— 想要<b>固定列数</b>用 {@link #columns(int)}。
     */
    public ListView<T> grid() {
        content.row();
        content.flex.wrap = FlexWrap.WRAP;
        return this;
    }

    /**
     * 固定列数的网格：每行恰好 {@code n} 个，单元宽度按视口宽度等分（自动扣掉列间距）。
     *
     * <p>{@link #grid()} 只能「排满就换行」，做不到「无论多宽都四列」；而背包 / 图鉴这类界面
     * 恰恰要求列数固定，否则窗口一缩放格子数就变。这里在每次内容排版前重算单元宽并写回行节点，
     * 因此窗口缩放、滚动条出现导致视口变窄时都会自动跟随。
     *
     * <p>传 {@code <= 0} 关闭固定列数，退回 {@link #grid()} 的自动换行。
     */
    public ListView<T> columns(int n) {
        this.columns = n;
        if (n > 0) grid();
        return this;
    }

    public int columns() { return columns; }

    /** 固定列数模式下，把等分出来的单元宽写回每一行。 */
    @Override
    public void layoutContent() {
        if (columns > 0 && !rows.isEmpty()) {
            int avail = contentWidth() - content.flex.padding.horizontal();
            int cell = (avail - content.flex.gapMain * (columns - 1)) / columns;
            if (cell > 0) {
                for (Node r : rows) {
                    r.flex.width = cell;
                    r.flex.basis = cell;
                    r.flex.grow = 0;
                    r.flex.shrink = 0;
                }
            }
        }
        super.layoutContent();
    }

    /** 协变覆写：让 {@code itemGap().contentPadding().clearBeforeBind()} 这类链式调用顺序无关。 */
    @Override
    public ListView<T> itemGap(int px) { super.itemGap(px); return this; }

    @Override
    public ListView<T> contentPadding(int all) { super.contentPadding(all); return this; }

    public int selectedIndex() { return selectedIndex; }

    public T selected() {
        return (selectedIndex >= 0 && selectedIndex < visible.size()) ? visible.get(selectedIndex) : null;
    }

    public void select(int index) {
        if (index < -1 || index >= visible.size()) return;
        selectedIndex = index;
        applySelectionState();
        if (index >= 0) {
            scrollIntoView(rows.get(index));
            value.set(visible.get(index));
        }
    }

    public void clearSelection() { select(-1); }

    /** 选中态用 {@code active} 表达，行的渲染器按状态自己换色即可。 */
    private void applySelectionState() {
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).state = rows.get(i).state.active(i == selectedIndex);
        }
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (!selectable || button != 0) return false;
        double mx = absX + lx, my = absY + ly;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).contains(mx, my)) {
                select(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 键盘导航：聚焦本列表后，上/下逐行移动选中、PageUp/Down 翻一屏、Home/End 跳首尾、
     * 回车/空格确认当前选中（触发 {@link #onSelect}）。选中项经 {@link #select} 自动滚入可视区。
     * 非可选列表（{@code selectable=false}）退化成普通滚动容器的方向键滚动。
     */
    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        int n = visible.size();
        if (n == 0) return false;
        if (!selectable) return super.onKeyPressed(keyCode, scanCode, modifiers);

        int page = rowsPerPage();
        int cur = selectedIndex < 0 ? 0 : selectedIndex;
        int next;
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:         next = Math.max(0, cur - 1); break;
            case GLFW.GLFW_KEY_DOWN:       next = Math.min(n - 1, cur + 1); break;
            case GLFW.GLFW_KEY_PAGE_UP:    next = Math.max(0, cur - page); break;
            case GLFW.GLFW_KEY_PAGE_DOWN:  next = Math.min(n - 1, cur + page); break;
            case GLFW.GLFW_KEY_HOME:       next = 0; break;
            case GLFW.GLFW_KEY_END:        next = n - 1; break;
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
            case GLFW.GLFW_KEY_SPACE:
                if (selectedIndex >= 0) value.set(selected());
                return true;
            default:
                return false;
        }
        select(next);
        return true;
    }

    /** 可视区内大致能容纳的行数，用于 PageUp/Down 的翻页步长。 */
    private int rowsPerPage() {
        if (rows.isEmpty()) return 1;
        int h = rows.get(0).height;
        return h > 0 ? Math.max(1, contentHeight() / h) : 1;
    }

    // ------------------------------------------------------------------
    // 悬浮检测：主从面板的输入源
    // ------------------------------------------------------------------

    @Override
    protected void renderChildren(GuiGraphicsExtractor g, RenderContext ctx, boolean disabled) {
        updateRowHover(ctx);
        applyRowStagger();
        super.renderChildren(g, ctx, disabled);
    }

    /**
     * 按「当前时钟 − mountEpoch − 行号错峰」解析式写各行的挂载 alpha。
     *
     * <p>只作用于 {@link AnimatedNode} 行（库内行模板为 Button，即 AnimatedNode）；
     * 普通 Node 行不受影响（直接全显，无错峰 —— 优雅降级，不报错）。写在 super.renderChildren
     * 之前：随后 Scrollable 裁剪渲染 content 时，各行 AnimatedNode.render 自读该 alpha。
     */
    private void applyRowStagger() {
        boolean active = AnimRegistry.animationsActive();
        long elapsed = Util.getMillis() - mountEpoch;
        int dur = AnimRegistry.ms(AnimRegistry.Trigger.MOUNT);
        for (int i = 0; i < rows.size(); i++) {
            if (!(rows.get(i) instanceof AnimatedNode an)) continue;
            if (!active) { an.anim().alpha = 1F; continue; }
            int delay = Math.min(i, STAGGER_MAX - 1) * STAGGER_GAP_MS;
            long t = elapsed - delay;
            float a = t <= 0 ? 0F : (t >= dur ? 1F : Easing.EASE_OUT_CUBIC.apply((float) t / dur));
            an.anim().alpha = a;
        }
    }

    private void updateRowHover(RenderContext ctx) {
        if (onRowHover == null) return;
        int hit = -1;
        // 鼠标必须真的在视口里（Scrollable 会把视口外的鼠标置空，这里再确认一次）
        if (Draw.inside(ctx.mouseX(), ctx.mouseY(), contentX(), contentY(), contentWidth(), contentHeight())) {
            double mx = ctx.mouseX(), my = ctx.mouseY();
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).contains(mx, my)) { hit = i; break; }
            }
        }
        if (hit == lastHoverRow) return;
        lastHoverRow = hit;
        if (hit >= 0 && hit < visible.size()) onRowHover.onHover(visible.get(hit), hit);
        else onRowHover.onHover(null, -1);
    }

    // ------------------------------------------------------------------
    // 空态
    // ------------------------------------------------------------------

    public ListView<T> emptyView(Node node) {
        if (emptyView != null) remove(emptyView);
        emptyView = node;
        if (node != null) {
            node.id = "list-empty";
            add(node);
        }
        syncEmptyView();
        return this;
    }

    private void syncEmptyView() {
        if (emptyView == null) return;
        emptyView.visible = visible.isEmpty();
        // 占位节点永远排在最后，不然会插在复用出来的行中间
        if (content.children.getLast() != emptyView) {
            content.remove(emptyView);
            content.add(emptyView);
        }
    }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        ListView<T> v = new ListView<>(template.copy(), binder);
        copyPropsInto(v);
        v.content.flex.copyFrom(content.flex);
        v.selectable = selectable;
        if (onSelect != null) v.onSelect(onSelect);
        v.filter = filter;
        v.clearBeforeBind = clearBeforeBind;
        v.columns = columns;
        v.items(source);
        return v;
    }
}
