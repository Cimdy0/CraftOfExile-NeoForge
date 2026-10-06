package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 拖拽注册表：挂在 {@code WindowLayer} 上的协调点，支持「嵌套作用域」。
 *
 * <p>职责：
 * <ul>
 *   <li>登记 / 注销 {@link DropZone}（带作用域，去重）；</li>
 *   <li>持有当前激活拖拽的可见数据（{@link ActiveDrag}），供 {@link GhostOverlay} 每帧绘制残影；</li>
 *   <li>命中测试：按作用域优先级（同容器 &gt; 父容器 &gt; 全局 &gt; 无关兄弟）返回最该响应的 zone；</li>
 *   <li>拖拽中自动滚动：光标贴近滚动容器边缘时驱动该容器滚动（{@link #autoScroll}）。</li>
 * </ul>
 *
 * <h3>嵌套作用域（Nested DragRegistry）</h3>
 * 每个 {@link DragScope} 容器（{@code Window} / {@code Scrollable}）持有一个<b>子注册表</b>，
 * 父指针指向外层作用域的注册表，链顶是 {@code WindowLayer} 的根注册表。
 * 激活态（active drag）与命中测试都只在<b>根</b>上聚合，因此：
 * <ul>
 *   <li>在滚动 / 嵌套容器里发起拖拽时，放置区登记到该容器作用域，命中优先于外层，互不串扰；</li>
 *   <li>跨窗口拖拽时，根注册表 DFS 收集所有作用域的 zone，松手即可命中另一窗口的放置区。</li>
 * </ul>
 *
 * <p>{@link Draggable} 只是把按下 / 移动 / 松手翻译成对这里的调用；本类不主动监听鼠标。
 */
public class DragRegistry {

    /** 当前激活拖拽的可视数据；松手或取消后归 {@code null}。激活态只存在根注册表上。 */
    public static final class ActiveDrag {
        public final Draggable source;
        public double x, y;
        public int w, h;
        public Component label;
        /** 松手前实时计算结果：光标当前是否悬在某个 DropZone 之上（供残影高亮）。 */
        public boolean hovered;
        /** 发起拖拽时源节点所属的作用域（最近的 {@link DragScope} 祖先），命中优先级用它。 */
        public Node sourceScope;

        ActiveDrag(Draggable source, double x, double y, int w, int h, Component label) {
            this.source = source;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
        }
    }

    /** 自动滚动：光标进入视口边缘多少 px 内开始滚动。 */
    private static final int AUTO_SCROLL_MARGIN = 28;
    /** 自动滚动：单帧最大滚动像素（按进入深度线性加速）。 */
    private static final int AUTO_SCROLL_SPEED = 10;

    private final DragRegistry parent;
    private final Node scopeNode;
    private final List<DragRegistry> children = new ArrayList<>();
    private final List<Entry> zones = new ArrayList<>();
    private long seqCounter = 0;

    /** 仅根注册表使用：当前激活拖拽。 */
    private ActiveDrag active;

    /** 根注册表构造器。 */
    public DragRegistry() {
        this(null, null);
    }

    /** 子作用域构造器（由 {@link #createChild} 调用）。 */
    private DragRegistry(DragRegistry parent, Node scopeNode) {
        this.parent = parent;
        this.scopeNode = scopeNode;
    }

    /** 在根注册表上创建一个子作用域并返回它。 */
    public DragRegistry createChild(Node scopeNode) {
        DragRegistry c = new DragRegistry(this, scopeNode);
        children.add(c);
        return c;
    }

    /** 走到链顶的根注册表。 */
    public DragRegistry root() {
        DragRegistry r = this;
        while (r.parent != null) r = r.parent;
        return r;
    }

    // ------------------------------------------------------------------
    // 放置区登记
    // ------------------------------------------------------------------

    /** 登记放置区（无作用域，视为全局）。去重。 */
    public void register(DropZone z) {
        register(z, null);
    }

    /**
     * 登记放置区到指定作用域。{@code scope} 为 {@code null} 时登记到本注册表（全局）。
     * 否则路由到 {@code scope} 所属作用域的注册表（scope 的最近 {@link DragScope} 祖先，找不到则根）。
     */
    public void register(DropZone z, Node scope) {
        if (z == null) return;
        DragRegistry target = (scope != null) ? scope.findDragRegistry() : this;
        if (target == null) target = this;
        target.registerLocal(z, scope);
    }

    private void registerLocal(DropZone z, Node scope) {
        zones.removeIf(e -> e.zone == z); // 去重：同一 zone 重复登记只保留一份
        zones.add(new Entry(z, scope, seqCounter++));
    }

    /** 注销放置区（从本注册表及其所有子作用域递归清除，避免跨次累积泄漏 / 误命中）。 */
    public void unregister(DropZone z) {
        zones.removeIf(e -> e.zone == z);
        for (DragRegistry c : children) c.unregister(z);
    }

    // ------------------------------------------------------------------
    // 命中测试
    // ------------------------------------------------------------------

    /**
     * 命中测试（兼容旧调用）：按全局口径返回最上层且包含该点的 zone。
     * 等价 {@link #hitTest(double, double, Node)} 传 {@code null} 作用域。
     */
    public DropZone hitTest(double mx, double my) {
        return hitTest(mx, my, null);
    }

    /**
     * 命中测试：从根聚合所有作用域的 zone，返回<b>优先级最高</b>且满足矩形命中的 zone；
     * 同优先级取后登记（序号大，更靠上）的。
     *
     * <p>优先级（越小越优先）：
     * <ol>
     *   <li>0 —— 与源节点同作用域（拖回原容器）；</li>
     *   <li>1 —— zone 是源作用域的祖先（拖进父容器）；</li>
     *   <li>2 —— zone 是源作用域的后代（拖进嵌套容器）；</li>
     *   <li>3 —— 全局 zone（无作用域）；</li>
     *   <li>4 —— 无关兄弟 / 其它（含跨窗口落到无关容器，仅当无更优命中时兜底）。</li>
     * </ol>
     */
    public DropZone hitTest(double mx, double my, Node sourceScope) {
        List<Entry> all = new ArrayList<>();
        root().collect(all);
        DropZone best = null;
        int bestPri = Integer.MAX_VALUE;
        long bestSeq = -1;
        for (Entry e : all) {
            if (!e.zone.acceptDrop(mx, my)) continue;
            int pri = priority(e.scope, sourceScope);
            if (pri < bestPri || (pri == bestPri && e.seq > bestSeq)) {
                bestPri = pri;
                best = e.zone;
                bestSeq = e.seq;
            }
        }
        return best;
    }

    private static int priority(Node scope, Node sourceScope) {
        if (scope == null) return 3;                       // 全局
        if (sourceScope == null) return 4;                 // 有作用域但源无作用域
        if (scope == sourceScope) return 0;                // 同容器
        if (isAncestor(scope, sourceScope)) return 1;      // 拖进父容器
        if (isAncestor(sourceScope, scope)) return 2;      // 拖进嵌套容器
        return 4;                                         // 无关（跨窗口/兄弟）
    }

    private static boolean isAncestor(Node a, Node b) {
        Node n = b;
        while (n != null) {
            if (n == a) return true;
            n = n.parent;
        }
        return false;
    }

    private void collect(List<Entry> out) {
        out.addAll(zones);
        for (DragRegistry c : children) c.collect(out);
    }

    // ------------------------------------------------------------------
    // 激活拖拽（所有写入都落到根，保证 GhostOverlay 能读到）
    // ------------------------------------------------------------------

    /** 开始一次拖拽：记录残影初始位置与尺寸；激活态只存在根上。 */
    public void beginDrag(ActiveDrag d) {
        root().active = d;
    }

    /** 拖拽中更新残影位置（保持抓取点相对残影左上角不变由调用方负责）。 */
    public void moveGhost(double x, double y) {
        DragRegistry r = root();
        if (r.active != null) {
            r.active.x = x;
            r.active.y = y;
        }
    }

    /** 结束拖拽（松手或取消），清空激活态。 */
    public void endDrag() {
        root().active = null;
    }

    /** 当前激活拖拽；无拖拽时 {@code null}。 */
    public ActiveDrag activeDrag() {
        return root().active;
    }

    // ------------------------------------------------------------------
    // 自动滚动：拖拽中光标贴近滚动容器边缘时驱动其滚动
    // ------------------------------------------------------------------

    /**
     * 若当前有激活拖拽且光标正落在某个滚动容器视口内、贴近其边缘，则按进入深度线性滚动该容器。
     * 由 {@code WindowLayer.renderLayer} 每帧在渲染前调用（此时布局已算好，translate 立即生效）。
     *
     * @param layer 根层（{@code WindowLayer}），用于从光标定位最深的滚动容器。
     */
    public void autoScroll(WindowLayer layer, double mx, double my) {
        if (active == null || layer == null) return;
        Scrollable s = deepestScrollableAt(layer, mx, my);
        if (s == null || !s.visible) return;
        int margin = AUTO_SCROLL_MARGIN;
        int cx = s.contentX(), cy = s.contentY();
        int cw = s.contentWidth(), ch = s.contentHeight();
        if (cw <= 0 || ch <= 0) return;

        Scrollable.Axis axis = s.axis();
        boolean v = axis != Scrollable.Axis.HORIZONTAL;
        boolean h = axis != Scrollable.Axis.VERTICAL;
        double dy = 0, dx = 0;
        if (v) {
            if (my >= cy && my <= cy + margin) dy = -(margin - (my - cy));
            else if (my >= cy + ch - margin && my <= cy + ch) dy = (margin - (cy + ch - my));
        }
        if (h) {
            if (mx >= cx && mx <= cx + margin) dx = -(margin - (mx - cx));
            else if (mx >= cx + cw - margin && mx <= cx + cw) dx = (margin - (cx + cw - mx));
        }
        if (dy != 0) s.scrollByY(ramp(dy, margin));
        if (dx != 0) s.scrollByX(ramp(dx, margin));
    }

    private static double ramp(double raw, int margin) {
        double f = Math.min(1.0, Math.abs(raw) / margin);
        return Math.signum(raw) * AUTO_SCROLL_SPEED * f;
    }

    /** 从光标命中的最深节点向上找最近的滚动容器（其视口包含光标）。 */
    private static Scrollable deepestScrollableAt(Node root, double mx, double my) {
        Node hit = root.hitTest(mx, my);
        while (hit != null) {
            if (hit instanceof Scrollable s) return s;
            hit = hit.parent;
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 内部：放置区条目（zone + 作用域 + 登记序号）
    // ------------------------------------------------------------------

    private static final class Entry {
        final DropZone zone;
        final Node scope;
        final long seq;

        Entry(DropZone zone, Node scope, long seq) {
            this.zone = zone;
            this.scope = scope;
            this.seq = seq;
        }
    }
}
