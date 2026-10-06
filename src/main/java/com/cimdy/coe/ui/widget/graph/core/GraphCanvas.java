package com.cimdy.coe.ui.widget.graph.core;

import com.cimdy.coe.ui.widget.graph.painter.EdgePainter;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.graph.painter.NodePainters;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 可平移 / 缩放的图画布：天赋树、科技树、关系图都是它的配置实例。
 *
 * <h3>与旧 {@code PannableGraphCanvas} 的区别</h3>
 * 旧画布自称通用，实际 {@code import} 了 {@code TalentAllocator}，还内建了「珠宝辐射范围圈」
 * 「武器组绑定着色」「PoE2 节点贴图」这些业务概念 —— 换个树就得改画布。这里把业务全部外移：
 *
 * <table border="1">
 *   <tr><th>业务概念</th><th>现在从哪来</th></tr>
 *   <tr><td>节点/连线长什么样</td><td>{@link NodePainter} / {@link EdgePainter}</td></tr>
 *   <tr><td>节点是「已点亮/可点/锁定」</td><td>{@link #nodeState(Function)} 返回 {@link VisualState}</td></tr>
 *   <tr><td>能不能点</td><td>{@link #activatable(Predicate)}</td></tr>
 *   <tr><td>点了之后干嘛</td><td>{@link #onActivate(Consumer)}（只发语义事件，不碰网络）</td></tr>
 *   <tr><td>悬浮显示什么</td><td>{@link #nodeTooltip(Function)}</td></tr>
 * </table>
 *
 * <h3>它是一个普通的 {@link Node}</h3>
 * 因此可以直接塞进 Flexbox 布局、参与统一事件分发；它的<b>子节点</b>是屏幕空间的浮层
 * （图例、按钮、搜索框），照常用 flex 排版，画在图之上。图元本身不是子节点 —— 几千个节点
 * 各做一个 UI 节点太重。
 *
 * <h3>提示气泡</h3>
 * 走 lib 统一的 {@code tooltipAt → collectTooltip → WindowLayer 顶层绘制} 链路，
 * 不再自己画一个盒子（旧画布自绘的提示会被后画的兄弟窗口盖住）。
 *
 * @param <T> 节点载荷类型
 */
public class GraphCanvas<T> extends Node {

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    private final Camera camera = new Camera();
    private final List<GraphNode<T>> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    /** id → 节点 的 O(1) 索引。上千节点时逐帧线性查两端会直接吃掉帧率。 */
    private final Map<String, GraphNode<T>> index = new HashMap<>();

    // ------------------------------------------------------------------
    // 注入点
    // ------------------------------------------------------------------

    private NodePainter<T> nodePainter = NodePainters.dot();
    private EdgePainter<T> edgePainter = NodePainters.line();

    private Function<GraphNode<T>, VisualState> nodeState = n -> VisualState.NONE;
    private Function<GraphEdge, VisualState> edgeState = e -> VisualState.NONE;
    private Predicate<GraphNode<T>> activatable = n -> true;
    private Consumer<GraphNode<T>> onActivate = n -> {};
    private Function<GraphNode<T>, List<Component>> tooltipFn;

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    private boolean panEnabled = true;
    private boolean zoomEnabled = true;
    private boolean clip = true;
    private float zoomStep = 1.1f;
    /** 按下后移动超过它才算拖拽，否则算点击。没有这个阈值，手抖一像素就点不中节点。 */
    private double dragThreshold = 4.0;
    /** 视口裁剪外扩量（world 单位），防止半个节点在边缘被整体剔除。 */
    private float cullMargin = 64f;
    /** 命中容差（屏幕像素）。缩得很小时给几像素容差，否则根本点不中。 */
    private float hitSlack = 2f;

    // ------------------------------------------------------------------
    // 交互瞬时状态
    // ------------------------------------------------------------------

    private boolean pressed, dragging;
    private int pressButton = -1;
    private double pressX, pressY;
    private GraphNode<T> pressedNode;

    public GraphCanvas() {
        this.id = "graph-canvas";
    }

    // ------------------------------------------------------------------
    // 图数据装配
    // ------------------------------------------------------------------

    public GraphCanvas<T> addNode(GraphNode<T> n) {
        nodes.add(n);
        index.put(n.id, n);
        return this;
    }

    public GraphCanvas<T> addEdge(GraphEdge e) { edges.add(e); return this; }

    public GraphCanvas<T> addEdge(String fromId, String toId) { return addEdge(new GraphEdge(fromId, toId)); }

    public GraphNode<T> node(String id) { return index.get(id); }

    public List<GraphNode<T>> nodes() { return Collections.unmodifiableList(nodes); }

    public List<GraphEdge> edges() { return Collections.unmodifiableList(edges); }

    // ------------------------------------------------------------------
    // 注入
    // ------------------------------------------------------------------

    public GraphCanvas<T> nodePainter(NodePainter<T> p) { if (p != null) this.nodePainter = p; return this; }

    public GraphCanvas<T> edgePainter(EdgePainter<T> p) { if (p != null) this.edgePainter = p; return this; }

    /** 节点状态函数。画布会在它的返回值上再叠加 hovered，业务不必自己判悬浮。 */
    public GraphCanvas<T> nodeState(Function<GraphNode<T>, VisualState> f) {
        if (f != null) this.nodeState = f;
        return this;
    }

    public GraphCanvas<T> edgeState(Function<GraphEdge, VisualState> f) {
        if (f != null) this.edgeState = f;
        return this;
    }

    public GraphCanvas<T> activatable(Predicate<GraphNode<T>> p) { if (p != null) this.activatable = p; return this; }

    public GraphCanvas<T> onActivate(Consumer<GraphNode<T>> c) { if (c != null) this.onActivate = c; return this; }

    public GraphCanvas<T> nodeTooltip(Function<GraphNode<T>, List<Component>> f) { this.tooltipFn = f; return this; }

    // ------------------------------------------------------------------
    // 相机 & 交互配置
    // ------------------------------------------------------------------

    public Camera camera() { return camera; }

    public GraphCanvas<T> panEnabled(boolean b)  { this.panEnabled = b; return this; }
    public GraphCanvas<T> zoomEnabled(boolean b) { this.zoomEnabled = b; return this; }
    public GraphCanvas<T> clip(boolean b)        { this.clip = b; return this; }
    public GraphCanvas<T> zoomStep(float f)      { if (f > 1f) this.zoomStep = f; return this; }
    public GraphCanvas<T> dragThreshold(double d){ this.dragThreshold = Math.max(0, d); return this; }
    public GraphCanvas<T> cullMargin(float m)    { this.cullMargin = Math.max(0, m); return this; }
    public GraphCanvas<T> hitSlack(float px)     { this.hitSlack = Math.max(0, px); return this; }

    /** 所有可见节点的 world 包围盒（含半径），空图返回 null。 */
    public float[] graphBounds() {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        boolean any = false;
        for (GraphNode<T> n : nodes) {
            if (!n.visible) continue;
            any = true;
            minX = Math.min(minX, n.worldX - n.radius);
            minY = Math.min(minY, n.worldY - n.radius);
            maxX = Math.max(maxX, n.worldX + n.radius);
            maxY = Math.max(maxY, n.worldY + n.radius);
        }
        return any ? new float[]{minX, minY, maxX, maxY} : null;
    }

    /** 把某个节点摆到视口正中。 */
    public GraphCanvas<T> centerOn(String nodeId) {
        GraphNode<T> n = index.get(nodeId);
        if (n != null) camera.centerOn(n.worldX, n.worldY, viewW(), viewH());
        return this;
    }

    // ------------------------------------------------------------------
    // 命中
    // ------------------------------------------------------------------

    /** 屏幕坐标 → 节点，取最靠近中心的那个；没命中返回 null。 */
    public GraphNode<T> nodeAt(double screenX, double screenY) {
        if (!Draw.inside(screenX, screenY, viewX(), viewY(), viewW(), viewH())) return null;
        float wx = camera.screenToWorldX((float) (screenX - viewX()));
        float wy = camera.screenToWorldY((float) (screenY - viewY()));
        float slack = hitSlack * camera.pixelsToWorld();
        GraphNode<T> best = null;
        float bestD = Float.MAX_VALUE;
        for (GraphNode<T> n : nodes) {
            if (!n.visible || !n.containsWorld(wx, wy, slack)) continue;
            float d = n.distance2(wx, wy);
            if (d < bestD) {
                best = n;
                bestD = d;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        super.renderSelf(g, ctx); // 背景等常规 visuals

        int x = viewX(), y = viewY(), w = viewW(), h = viewH();
        if (w <= 0 || h <= 0) return;

        GraphNode<T> hovered = ctx.state().disabled() ? null : nodeAt(ctx.mouseX(), ctx.mouseY());

        if (clip) g.enableScissor(x, y, x + w, y + h);
        try {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            try {
                pose.translate((float) x, (float) y);
                pose.scale(camera.zoom, camera.zoom);
                pose.translate(-camera.camX, -camera.camY);
                paintWorld(g, ctx, hovered, w, h);
            } finally {
                pose.popMatrix();
            }
        } finally {
            if (clip) g.disableScissor();
        }
    }

    private void paintWorld(GuiGraphicsExtractor g, RenderContext ctx, GraphNode<T> hovered, int w, int h) {
        float[] b = camera.visibleWorldBounds(w, h, cullMargin);
        float minX = b[0], minY = b[1], maxX = b[2], maxY = b[3];
        float zoom = camera.zoom;

        for (GraphEdge e : edges) {
            if (!e.visible) continue;
            GraphNode<T> a = index.get(e.fromId), c = index.get(e.toId);
            if (a == null || c == null || !a.visible || !c.visible) continue;
            // 包围盒粗筛：两端都在可见框外同侧才剔除
            if (Math.max(a.worldX, c.worldX) < minX || Math.min(a.worldX, c.worldX) > maxX
                    || Math.max(a.worldY, c.worldY) < minY || Math.min(a.worldY, c.worldY) > maxY) continue;
            edgePainter.paint(g, e, a, c, ctx.with(edgeState.apply(e)), zoom);
        }

        for (GraphNode<T> n : nodes) {
            if (!n.visible) continue;
            if (n.worldX + n.radius < minX || n.worldX - n.radius > maxX
                    || n.worldY + n.radius < minY || n.worldY - n.radius > maxY) continue;
            // 不做 normalizeForInteraction：图上「锁定」节点照样要有悬浮反馈（要看 tooltip）
            VisualState st = nodeState.apply(n).hovered(n == hovered).pressed(n == pressedNode);
            nodePainter.paint(g, n, ctx.with(st), zoom);
        }
    }

    // ------------------------------------------------------------------
    // 提示
    // ------------------------------------------------------------------

    @Override
    protected List<Component> tooltipAt(double mx, double my) {
        if (tooltipFn != null) {
            GraphNode<T> n = nodeAt(mx, my);
            if (n != null) {
                List<Component> lines = tooltipFn.apply(n);
                if (lines != null && !lines.isEmpty()) return lines;
            }
        }
        return super.tooltipAt(mx, my);
    }

    // ------------------------------------------------------------------
    // 输入：拖拽 = 平移，滚轮 = 缩放，点击 = 命中 + 规则 + 语义事件
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        pressed = true;
        dragging = false;
        pressButton = button;
        pressX = lx;
        pressY = ly;
        pressedNode = button == 0 ? nodeAt(absX + lx, absY + ly) : null;
        focus();
        return true;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!pressed) return false;
        if (!dragging && Math.hypot(lx - pressX, ly - pressY) > dragThreshold) dragging = true;
        if (dragging && panEnabled) {
            camera.pan((float) dx, (float) dy);
            return true;
        }
        return dragging;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!pressed) return false;
        GraphNode<T> target = (!dragging && button == pressButton && button == 0) ? pressedNode : null;
        pressed = false;
        dragging = false;
        pressButton = -1;
        pressedNode = null;
        if (target != null && activatable.test(target)) onActivate.accept(target);
        return true;
    }

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double sx, double sy) {
        if (!zoomEnabled || sy == 0) return false;
        camera.zoomAt((float) (lx - flex.padding.left), (float) (ly - flex.padding.top),
                sy > 0 ? zoomStep : 1f / zoomStep);
        return true;
    }

    // ------------------------------------------------------------------
    // 视口（内容区，扣掉 padding）
    // ------------------------------------------------------------------

    private int viewX() { return contentX(); }
    private int viewY() { return contentY(); }
    private int viewW() { return contentWidth(); }
    private int viewH() { return contentHeight(); }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        GraphCanvas<T> c = new GraphCanvas<>();
        copyInto(c);
        return c;
    }

    @Override
    protected void copyInto(Node n) {
        super.copyInto(n);
        if (!(n instanceof GraphCanvas<?> raw)) return;
        @SuppressWarnings("unchecked")
        GraphCanvas<T> c = (GraphCanvas<T>) raw;
        // 图数据是共享的只读大对象，克隆整棵图没有意义也很贵 —— 直接引用同一批实例
        c.nodes.addAll(nodes);
        c.edges.addAll(edges);
        c.index.putAll(index);
        c.nodePainter = nodePainter;
        c.edgePainter = edgePainter;
        c.nodeState = nodeState;
        c.edgeState = edgeState;
        c.activatable = activatable;
        c.onActivate = onActivate;
        c.tooltipFn = tooltipFn;
        c.panEnabled = panEnabled;
        c.zoomEnabled = zoomEnabled;
        c.clip = clip;
        c.zoomStep = zoomStep;
        c.dragThreshold = dragThreshold;
        c.cullMargin = cullMargin;
        c.hitSlack = hitSlack;
        c.camera.camX = camera.camX;
        c.camera.camY = camera.camY;
        c.camera.zoom = camera.zoom;
        c.camera.minZoom = camera.minZoom;
        c.camera.maxZoom = camera.maxZoom;
    }
}
