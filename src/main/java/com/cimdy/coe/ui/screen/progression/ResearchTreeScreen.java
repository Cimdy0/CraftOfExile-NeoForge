package com.cimdy.coe.ui.screen.progression;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.block.entity.ResearchBenchBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.research.ResearchBench;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.network.research.OpenResearchPacket;
import com.cimdy.coe.network.research.StartResearchPacket;
import com.cimdy.coe.ui.screen.graph.NodeGraphController;
import com.cimdy.coe.ui.widget.graph.core.GraphCanvas;
import com.cimdy.coe.ui.widget.graph.core.GraphEdge;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.NodePainters;
import com.cimdy.coe.ui.widget.graph.painter.ProgressionNodePainterI;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 研究树界面（树状 DAG，复用通用画布 {@link GraphCanvas}）。
 *
 * <p>入口是<b>研究台</b>：玩家右键空闲研究台，服务端下发 {@link OpenResearchPacket}，
 * 客户端据其中的 {@code benchPos} 打开本界面；点击节点后回发
 * {@link StartResearchPacket}，仍带上同一坐标——由此保证「研究是在某台机器上做的」，
 * 而不是玩家凭空点出来的。
 *
 * <p>外壳（背景 / 右侧面板 / 页签）来自 {@link ProgressionTreeScreen}，视觉走青绿色系以区分科技树。
 * 与科技树不同，本屏<b>不套内容区裁剪</b>：画布是全屏组件，自带裁剪与背景填充，
 * 外层再裁一次会把贴近顶边的节点 tooltip 切掉。
 */
public class ResearchTreeScreen extends ProgressionTreeScreen {

    /** 数据包里的 x/y 是「格子坐标」（0,1,2…），乘以本值展开为世界像素间距。 */
    private static final float LAYOUT_SCALE = 96f;
    private static final float NODE_RADIUS = 16f;
    private static final float MIN_ZOOM = 0.15f;
    private static final float MAX_ZOOM = 3.0f;

    private NodeGraphController<ResearchNodeDef> controller;
    private GraphCanvas<ResearchNodeDef> canvas;
    private ProgressionNodePainterI nodePainter;
    private ResearchAllocator allocator;
    private final Map<String, GraphNode<ResearchNodeDef>> nodeMap = new HashMap<>();

    /** 数据包里的 x/y 是「格子坐标」（0,1,2…），乘以本值展开为世界像素间距。 */
    /** 节点激活 Pulse（规格 §四·五：激活节点 Pulse 300ms 一次）：最近点击的节点 id + 起始时刻。 */
    private String pulseNodeId;
    private long pulseStartNanos;

    /** 前置指向不存在节点的研究个数（数据包错误计数，>0 时在右面板告警）。 */
    private int brokenNodes = 0;

    public ResearchTreeScreen(BlockPos benchPos) {
        super(Component.translatable("screen.coe.research_tree"), benchPos);
    }

    @Override
    protected TabAccent accent() {
        // 不能再是 static final：那会在类加载时冻结，换肤对它无效（与 TalentTheme 同一个坑）
        return new TabAccent(Theme.current().progressionResearchAccent(),
                Theme.current().progressionResearchAccentBg(),
                Theme.current().progressionResearchAccentText());
    }

    @Override
    protected boolean isTechScreen() {
        return false;
    }

    @Override
    protected void init() {
        initPanel();

        nodeMap.clear();
        brokenNodes = 0;
        allocator = new ResearchAllocator(PlayerProgress.get(player));
        nodePainter = NodePainters.progression(allocator);

        controller = new NodeGraphController<ResearchNodeDef>()
                .init(width, height, width, height, MIN_ZOOM, MAX_ZOOM)
                .activatableFrom(allocator)
                .onActivate(this::onNodeClick)
                .nodeTooltip(this::nodeTooltip)
                .nodePainter(nodePainter)
                .edgePainter(NodePainters.<ResearchNodeDef>line()
                        .color(ProgressionTheme.edgeDim())
                        .colorActive(ProgressionTheme.edgeLit()));
        canvas = controller.canvas();

        List<ResearchNodeDef> defs = ProgressionData.researchSorted();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (ResearchNodeDef def : defs) {
            float nx = def.x() * LAYOUT_SCALE;
            float ny = def.y() * LAYOUT_SCALE;
            GraphNode<ResearchNodeDef> gn = new GraphNode<>(def.id().toString(), nx, ny, def)
                    .radius(NODE_RADIUS);
            canvas.addNode(gn);
            nodeMap.put(gn.id, gn);
            minX = Math.min(minX, nx);
            minY = Math.min(minY, ny);
            maxX = Math.max(maxX, nx);
            maxY = Math.max(maxY, ny);
        }
        linkPrerequisites(defs);

        // 初始视野：把整棵树装进画布左侧内容区（右侧面板不参与自适应，避免节点被面板遮住）
        if (!defs.isEmpty()) {
            controller.fitToContent(rightPanelX, height, NODE_RADIUS * 3f);
        }
    }

    /** 前置依赖 → 连线；前置缺失的节点标红。 */
    private void linkPrerequisites(List<ResearchNodeDef> defs) {
        java.util.Set<String> brokenIds = new java.util.HashSet<>();
        for (ResearchNodeDef def : defs) {
            List<Component> missing = new ArrayList<>();
            for (Identifier pre : def.prerequisites()) {
                String from = pre.toString();
                if (nodeMap.containsKey(from)) {
                    canvas.addEdge(new GraphEdge(from, def.id().toString()));
                } else {
                    missing.add(Component.translatable("research.coe.tooltip.missing_prereq", from));
                }
            }
            if (missing.isEmpty()) continue;
            brokenNodes++;
            brokenIds.add(def.id().toString());
            GraphNode<ResearchNodeDef> gn = nodeMap.get(def.id().toString());
            if (gn != null) nodePainter.markBroken(gn.id, missing);
        }
        nodePainter.setBroken(brokenIds);
    }

    // ====== 交互 ======

    private void onNodeClick(GraphNode<ResearchNodeDef> node) {
        // 没有研究台就没有承载研究的机器，服务端也会拒收带 null 坐标的包
        if (benchPos == null) return;
        ResearchNodeDef def = ResearchAllocator.defOf(node.id);
        if (def == null || !allocator.canAllocate(node.id)) return;
        // 研究台被占用时不发包：服务端也会拒，但本地先拦一道，省一次无效往返
        if (benchState().isRunning()) return;
        allocator.preview(node.id, true);
        // 激活 Pulse（规格 §四·五）：记录节点 + 起始时刻，extractRenderState 里画 300ms 扩散环
        pulseNodeId = node.id;
        pulseStartNanos = System.nanoTime();
        ClientPacketDistributor.sendToServer(new StartResearchPacket(benchPos, def.id()));
    }

    private List<Component> nodeTooltip(GraphNode<ResearchNodeDef> node) {
        ResearchNodeDef def = node.payload;
        if (def == null) return null;
        List<Component> lines = new ArrayList<>();
        lines.add(def.displayName());
        lines.addAll(def.statLines());
        List<Component> extra = nodePainter.brokenDetails(node.id);
        if (extra != null) lines.addAll(extra);
        return lines;
    }

    /**
     * 读取研究台当前状态（附件已由 {@code registerSync} 同步到客户端）。
     *
     * <p>{@code benchPos} 允许为 null（从科技树页签切过来时可能没有研究台），
     * 此时按「空闲」处理而不是解引用崩溃。
     */
    private ResearchBench benchState() {
        if (benchPos == null || minecraft == null || minecraft.level == null) return ResearchBench.EMPTY;
        return minecraft.level.getBlockEntity(benchPos) instanceof ResearchBenchBlockEntity bench
                ? bench.getData(AttachmentRegistry.RESEARCH_BENCH) : ResearchBench.EMPTY;
    }

    // ====== 渲染 ======

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        // 权威同步：每帧从已同步附件重建分配态，服务端拒绝的乐观预览会在此自动回滚
        allocator.refresh(PlayerProgress.get(player));

        super.extractRenderState(g, mx, my, partialTick);

        // 画布（全屏）渲染在内容层
        controller.render(g, mx, my, partialTick);

        // 节点激活 Pulse 叠加（画布之上）
        drawNodePulse(g);

        drawTitle(g, getTitle());
        drawRightPanel(g, mx, my);
    }

    /** 节点激活 Pulse（规格 §四·五，300ms 一次）：以世界→屏幕换算画一圈扩散渐隐的高亮框。 */
    private void drawNodePulse(GuiGraphicsExtractor g) {
        if (pulseNodeId == null) return;
        GraphNode<ResearchNodeDef> gn = nodeMap.get(pulseNodeId);
        if (gn == null) { pulseNodeId = null; return; }
        long elapsedMs = (System.nanoTime() - pulseStartNanos) / 1_000_000L;
        if (elapsedMs > 300L) { pulseNodeId = null; return; }
        float t = elapsedMs / 300f;
        float cx = controller.canvas().camera().worldToScreenX(gn.worldX);
        float cy = controller.canvas().camera().worldToScreenY(gn.worldY);
        float r = gn.radius * (1f + t * 0.9f);
        int alpha = (int) (200 * (1f - t));
        int color = (alpha << 24) | (Theme.current().progressionResearchAccent() & 0xFFFFFF);
        g.outline((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r), color);
    }

    private void drawRightPanel(GuiGraphicsExtractor g, int mx, int my) {
        drawPanelChrome(g, mx, my);

        PlayerProgress p = allocator.progress();
        int y = PAD + TAB_H + 6;
        y = line(g, y, Component.translatable("progression.coe.level", p.level()), Theme.current().progressionLevelText());
        y = line(g, y, Component.translatable("progression.coe.research_points", p.researchPoints()),
                Theme.current().progressionResearchAccent());
        y = line(g, y, Component.translatable("progression.coe.completed_research",
                p.completedResearch().size(), ProgressionData.RESEARCH_NODES.size()), Theme.current().progressionInfoText());

        // ---- 研究台状态 ----
        y += 6;
        ResearchBench bench = benchState();
        if (bench.isRunning()) {
            Identifier id = bench.research().orElse(null);
            ResearchNodeDef def = id == null ? null : ProgressionData.RESEARCH_NODES.get(id);
            int duration = def == null ? 0 : def.durationTicks();
            float progress = bench.progress(duration);
            Component name = def != null ? def.displayName()
                    : Component.literal(id == null ? "?" : id.getPath());
            y = line(g, y, Component.translatable("research.coe.bench.running", name),
                Theme.current().progressionBenchRunningText());
            // 进度条
            int barX = contentX, barW = contentW, barH = 8;
            g.fill(barX, y, barX + barW, y + barH, Theme.current().progressionBarTrack());
            g.fill(barX, y, barX + (int) (barW * progress), y + barH, Theme.current().progressionBarFill());
            g.outline(barX, y, barW, barH, Theme.current().progressionBarBorder());
            y += barH + 2;
            g.centeredText(font(), Component.literal(Math.round(progress * 100f) + "%"),
                    barX + barW / 2, y, Theme.current().progressionResearchAccentText());
            y += ROW_H;
            y = line(g, y, Component.translatable("research.coe.bench.remaining",
                    Math.max(0, (duration - bench.ticks()) / 20)), Theme.current().progressionTextSecondary());
        } else if (benchPos == null) {
            y = line(g, y, Component.translatable("research.coe.bench.absent"), Theme.current().progressionBenchAbsent());
        } else {
            y = line(g, y, Component.translatable("research.coe.bench.idle"), Theme.current().progressionTextSecondary());
            y = line(g, y, Component.translatable("research.coe.bench.hint"), Theme.current().progressionTextHint());
        }

        // ---- 数据包告警：有节点的前置指向不存在的研究 ----
        if (brokenNodes > 0) {
            y += 6;
            y = line(g, y, Component.translatable("research.coe.panel.broken", brokenNodes),
                Theme.current().progressionDanger());
        }

        // ---- 操作提示 ----
        y += 6;
        line(g, y, Component.translatable("screen.coe.hint.pan_zoom"), Theme.current().progressionHint());
    }

    // ====== 鼠标 ======

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int mx = (int) event.x();
        int my = (int) event.y();
        // 页签切换 + 面板区吞点击（避免误触画布拖拽）
        if (consumedByPanel(mx, my)) return true;
        if (controller.mouseClicked(mx, my, event.button())) return true;
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (controller.mouseDragged(mx(event), my(event), event.button(), dx, dy)) return true;
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (controller.mouseReleased(mx(event), my(event), event.button())) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= rightPanelX) return true;
        if (controller.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static int mx(MouseButtonEvent e) { return (int) e.x(); }
    private static int my(MouseButtonEvent e) { return (int) e.y(); }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
