package com.cimdy.coe.ui.screen.graph;

import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.graph.core.GraphCanvas;
import com.cimdy.coe.ui.widget.graph.core.GraphEdge;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.EdgePainter;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 节点图控制器（A1+A2 架构收敛的核心）：把「画布 + 顶层 UI 树 + 每帧布局渲染 + 鼠标转发」这套
 * 天赋树（{@code AbstractTalentTreeScreen}）与研究树（{@code ResearchTreeScreen}）此前各自手写的样板
 * 收成<b>单一事实来源</b>。两棵树现在只负责「业务装配」——建节点、连边、配 painter、写状态函数——
 * 其余机械活（画布怎么被宿主屏承载、输入怎么转给画布、相机怎么适配内容）全部交给本类。
 *
 * <p>本类刻意<b>不碰</b>节点 / 连线的业务语义：节点长什么样、什么状态什么色，由各自的
 * {@code NodePainter} / {@code TalentTheme} / {@code ProgressionTheme} 决定。它只做一件事——
 * 「一个可平移缩放的图，如何被宿主屏承载、交互怎么透传到画布」。这正是两棵树真正的重合处。
 *
 * @param <N> 节点载荷类型（天赋 = {@code TalentNodeDef}，研究 = {@code ResearchNodeDef}）
 */
public class NodeGraphController<N> {

    private final GraphCanvas<N> canvas = new GraphCanvas<>();
    private final WindowLayer uiRoot = new WindowLayer();

    private int screenW, screenH;

    /**
     * 创建画布并接入 UI 树根。画布铺满 {@code canvasW × canvasH}（天赋树 = 面板以西区域，研究树 = 全屏）。
     * {@code screenW / screenH} 是宿主屏尺寸，用于每帧对 UI 树做整体布局。
     */
    public NodeGraphController<N> init(int canvasW, int canvasH, int screenW, int screenH,
                                       float minZoom, float maxZoom) {
        this.screenW = screenW;
        this.screenH = screenH;
        canvas.flex.size(canvasW, canvasH);
        canvas.padding(0);
        canvas.camera().zoomRange(minZoom, maxZoom);
        uiRoot.add(canvas);
        return this;
    }

    /** 底层画布（建节点 / 连边 / 读相机 / 算屏幕坐标都通过它）。 */
    public GraphCanvas<N> canvas() { return canvas; }

    // ------------------------------------------------------------------
    // 画布接线（直接透传到 GraphCanvas 的同名 fluent 方法）
    // ------------------------------------------------------------------

    public NodeGraphController<N> nodePainter(NodePainter<N> p) { canvas.nodePainter(p); return this; }

    public NodeGraphController<N> edgePainter(EdgePainter<N> p) { canvas.edgePainter(p); return this; }

    public NodeGraphController<N> activatable(Predicate<GraphNode<N>> p) { canvas.activatable(p); return this; }

    public NodeGraphController<N> onActivate(Consumer<GraphNode<N>> c) { canvas.onActivate(c); return this; }

    public NodeGraphController<N> nodeTooltip(Function<GraphNode<N>, List<Component>> f) { canvas.nodeTooltip(f); return this; }

    public NodeGraphController<N> nodeState(Function<GraphNode<N>, VisualState> f) { canvas.nodeState(f); return this; }

    public NodeGraphController<N> edgeState(Function<GraphEdge, VisualState> f) { canvas.edgeState(f); return this; }

    /**
     * 共享接线：从 {@link NodeAllocator} 取「可点」语义。两棵树此前都写 {@code n -> allocator.canAllocate(n.id)}，
     * 现在统一收口；分配器的其余专属语义不受影响。
     */
    public NodeGraphController<N> activatableFrom(NodeAllocator a) {
        canvas.activatable(n -> a.canAllocate(n.id));
        return this;
    }

    /** 把一个额外控件（搜索框宿主 Window 等）加到 UI 树根，浮在画布之上。 */
    public NodeGraphController<N> addOverlay(Node child) { uiRoot.add(child); return this; }

    // ------------------------------------------------------------------
    // 每帧渲染 + 输入转发（两棵树此前各写一遍的 uiRoot 样板）
    // ------------------------------------------------------------------

    /** 重新布局整棵 UI 树（画布 + 浮层）并渲染。宿主屏在 {@code extractRenderState} 末尾调用。 */
    public void render(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        uiRoot.layoutAll(screenW, screenH);
        uiRoot.renderLayer(g, mx, my, partialTick);
    }

    public boolean mouseClicked(int mx, int my, int button) { return uiRoot.mouseClicked(mx, my, button); }

    public boolean mouseDragged(int mx, int my, int button, double dx, double dy) {
        return uiRoot.mouseDragged(mx, my, button, dx, dy);
    }

    public boolean mouseReleased(int mx, int my, int button) { return uiRoot.mouseReleased(mx, my, button); }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        return uiRoot.mouseScrolled(mx, my, sx, sy);
    }

    // ------------------------------------------------------------------
    // 相机适配
    // ------------------------------------------------------------------

    /** 把全部可见节点装进视口（右侧面板以西），缩放到留出 padding。研究树用。 */
    public void fitToContent(int rightPanelX, int height, float pad) {
        float[] b = canvas.graphBounds();
        if (b == null) return;
        canvas.camera().fitToBounds(b[0], b[1], b[2], b[3], Math.max(1, rightPanelX), height, pad);
    }

    /** 把视野中心对准给定世界坐标（天赋树用：对准起点簇平均位置）。 */
    public void centerOn(float wx, float wy, int viewW, int viewH) {
        canvas.camera().centerOn(wx, wy, viewW, viewH);
    }
}
