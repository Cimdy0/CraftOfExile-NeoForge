package com.cimdy.coe.ui.screen.talent;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.client.ClientLocale;

import com.cimdy.coe.data.codec.talent.data.TalentWeaponSet;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatScopeDescriber;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.talent.data.*;
import com.cimdy.coe.data.codec.talent.effect.*;
import com.cimdy.coe.data.codec.talent.node.*;
import com.cimdy.coe.data.codec.talent.tree.*;
import com.cimdy.coe.data.codec.support.addition.GoldState;
import com.cimdy.coe.data.codec.talent.respec.TalentRespecCost;
import com.cimdy.coe.client.render.RenderHelper;
import com.cimdy.coe.item.gem.Jewel;
import com.cimdy.coe.network.skill.TalentActionPacket;
import com.cimdy.coe.network.skill.TalentCommitPacket;
import com.cimdy.coe.network.skill.TalentCommitResultPacket;
import com.cimdy.coe.network.skill.TalentSocketPacket;
import com.cimdy.coe.ui.menu.TalentMenu;
import com.cimdy.coe.ui.widget.component.list.OptionListPopup;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.screen.graph.NodeGraphController;
import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.graph.core.GraphCanvas;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.NodePainters;
import com.cimdy.coe.ui.widget.graph.painter.TalentNodePainterI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据驱动的天赋树界面基类（POE2 风格）。
 *
 * <p>布局：
 * <ol>
 *   <li>画布：<b>左侧全屏区域</b>渲染天赋树（视口被限制在右侧面板以西，面板点击不会被画布误吞）；</li>
 *   <li>标题：屏幕正中央上方（仅居中标题，无全宽条）；</li>
 *   <li>右侧信息面板：占屏幕宽度 1/7，从上到下依次为可用点数、武器套装长框、搜索框、
 *       缩放控件、属性加成下拉按钮；展开属性面板时面板向下延伸至屏幕底部。</li>
 * </ol>
 *
 * <p>武器套装：仿 POE2 长条样式——左侧箭头选择 / 中部段标号 + 名称 + 「已用 (剩余)」
 * / 右侧箭头删除。未填满时多渲染一个居中 `+` 的长条。
 *
 * <p>加点逻辑：参见 {@link TalentAllocator}，共享总预算，跨组加点走「换组」语义。
 *
 * <p>本类保留手绘外壳（右侧面板 / 套装条 / 缩放 / 摘要都是 {@code GuiGraphicsExtractor}
 * 直接绘制），画布、搜索框与弹窗选择器接入新库：{@link GraphCanvas} + {@link TextInput} +
 * {@link OptionListPopup}。
 */
public class AbstractTalentTreeScreen extends AbstractContainerScreen<TalentMenu> {
    /** 当前打开的天赋树实例（单实例；init 注册 / removed 注销，供 NetEvent 转发提交回执）。 */
    private static AbstractTalentTreeScreen current = null;

    public static AbstractTalentTreeScreen current() {
        return current;
    }

    private final Player player;

    private final Identifier treeId;

    private NodeGraphController<TalentNodeDef> controller;
    private GraphCanvas<TalentNodeDef> canvas;
    private TalentNodePainterI talentPainter;
    private TalentAllocator allocator;
    private TalentTreeModel model;
    private TextInput searchBox;
    /** 顶层弹窗选择器（选项列表），打开时优先消费点击。 */
    private OptionListPopup<?> popup;

    private final Map<String, GraphNode<TalentNodeDef>> nodeMap = new HashMap<>();
    /** 当前 UI 编辑的武器组（0=全局，1–9=快捷栏栏位 0–8）。 */
    private int uiActiveSet = 0;
    /** 已创建的武器套装集合：键 = 绑定值 1–9。 */
    private final Map<Integer, Integer> createdSets = new LinkedHashMap<>();
    /** 属性加成下拉面板状态。 */
    private boolean summaryOpen = false;
    private final List<Component> summaryCache = new ArrayList<>();
    private int summaryScroll = 0;

    // ===== 待保存状态机 =====
    /** 待保存模式：加点与重置互斥，二者不能同时处于待保存态。 */
    private PendingMode pendingMode = PendingMode.NONE;
    /** 已提交（服务端权威）的分配快照：用于计算待保存 diff 与「放弃改动」。 */
    private List<TalentAllocation> committedAllocations = List.of();
    /** 已发出保存包、等待服务端回同步；期间保留本地预览。 */
    private boolean awaitingCommit = false;
    private int commitWaitTicks = 0;
    /** RESET 待保存态下的本次花费与点数。 */
    private long pendingRespecCost = 0;
    private int pendingRespecPoints = 0;
    /** 每帧刷新的金币余额缓存。 */
    private long goldBalance = 0;
    /** 短暂的互斥 / 余额不足提示，单位：帧。 */
    private Component pendingHint;
    private int hintTicks = 0;

    // ===== 几何 =====
    private static final int PAD = 4;
    private static final int BAR_H = 18;          // 武器套装长框高
    private static final int SET_BAR_H = 18;       // 套装行总高（与 BAR_H 等大）
    private static final int SEARCH_H = 14;
    private static final int ZOOM_H = 14;
    private static final int SUMMARY_BTN_H = 16;
    private static final int SUMMARY_HEADER_H = 18;
    private static final int SUMMARY_ROW_H = 12;
    /** 顶部操作按钮（重置 / 保存）行高。 */
    private static final int ACTION_BTN_H = 14;
    /** 金币余额行高。 */
    private static final int GOLD_ROW_H = 10;
    /** 待保存花费 / 未保存提示行高。 */
    private static final int STATUS_ROW_H = 10;
    /** 同一行内两个按钮之间的间隔。 */
    private static final int BTN_GAP = 3;
    /** 提示文案存活帧数。 */
    private static final int HINT_TICKS = 60;
    /** 发出保存包后等待服务端回同步的最长帧数（超时兜底承认服务端权威状态）。 */
    private static final int COMMIT_WAIT_TICKS = 40;

    /**
     * POE2 原始节点坐标空间很大（±10000），直接渲染会导致节点间距过宽。
     * 此处按固定比例压缩坐标，使节点更密集；节点自身半径不缩放，从而相对变大、更易辨识。
     */
    private static final float LAYOUT_SCALE = 0.25f;

    private static final float DEFAULT_ZOOM = 0.35f;
    private static final float MIN_ZOOM = 0.08f;
    private static final float MAX_ZOOM = 4.0f;

    /** 右侧面板占屏幕宽度的比例（严格 1/7）。 */
    private static final float RIGHT_PANEL_RATIO = 1f / 7f;

    // ---- 运行时几何 ----
    private int rightPanelX;
    private int rightPanelW;
    private int contentX;
    private int contentW;
    private int summaryBtnRectX, summaryBtnRectY;
    /** 属性面板的 [x, y, w, h]（向下展开到屏幕底）。 */
    private int[] summaryPanelRect;
    /** 顶部操作行 Y：重置/保存按钮行、金币行、状态行。 */
    private int rowY_reset, rowY_gold, rowY_status;
    /** 顶部 [重置] 与 [保存] 按钮矩形。 */
    private int resetBtnRectX, resetBtnRectY, saveBtnRectX, saveBtnRectY;
    /** 单个操作按钮宽度（同行两个等宽）。 */
    private int actionBtnW;
    /** 套装长条矩形 [x, y, w, h]，键=绑定值；+ 号条为键 0。 */
    private final Map<Integer, int[]> setBarRects = new LinkedHashMap<>();
    /** 缩放控件：[-]、轨道、[+] 矩形。 */
    private int[] zoomMinusRect, zoomPlusRect, zoomTrackRect;
    /** 缩放滑块拖动状态：是否在拖动。 */
    private boolean draggingZoom = false;
    /** 搜索框矩形（手绘边框用）。 */
    private int searchBoxX, searchBoxY, searchBoxW, searchBoxH;

    public AbstractTalentTreeScreen(TalentMenu menu, Inventory inv, Component title, Identifier treeId) {
        super(menu, inv, title);
        this.player = inv.player;
        this.treeId = treeId;
    }

    @Override
    protected void init() {
        // 天赋屏单实例：注册当前实例供 NetEvent 转发提交回执（26.2 无 Minecraft.screen 访问器）
        current = this;
        TalentTreeModelLoader.ensureClientLoaded();
        model = TalentTreeModelLoader.get(treeId);
        Map<String, float[]> layout = scaleLayout(TalentTreeModelLoader.getLayout(treeId), LAYOUT_SCALE);
        TalentData td = player.hasData(AttachmentRegistry.TALENT_TREE)
                ? player.getData(AttachmentRegistry.TALENT_TREE) : TalentData.EMPTY;
        TalentState state = td.get(treeId).state();
        allocator = new TalentAllocator(model, state, player, layout, LAYOUT_SCALE);
        uiActiveSet = 0;
        allocator.setActiveSet(uiActiveSet);
        talentPainter = NodePainters.talent(allocator);
        committedAllocations = List.copyOf(allocator.allocations());
        goldBalance = GoldState.get(player);

        // ---- 右侧面板几何（严格 1/7 宽度） ----
        rightPanelW = Math.max(140, (int) (width * RIGHT_PANEL_RATIO));
        rightPanelX = width - rightPanelW;
        contentX = rightPanelX + PAD;
        contentW = rightPanelW - 2 * PAD;

        // ---- 画布：左侧区域（面板以西），视口不再覆盖右侧面板 ----
        controller = new NodeGraphController<TalentNodeDef>()
                .init(rightPanelX, height, width, height, MIN_ZOOM, MAX_ZOOM)
                .activatableFrom(allocator)
                .onActivate(this::onNodeClick)
                .nodeTooltip(this::nodeTooltip)
                .nodePainter(talentPainter);
        canvas = controller.canvas();
        controller.edgeState(e -> {
            GraphNode<TalentNodeDef> a = canvas.node(e.fromId), b = canvas.node(e.toId);
            boolean lit = (a != null && allocator.isActive(a.id)) || (b != null && allocator.isActive(b.id));
            return VisualState.NONE.active(lit);
        }).edgePainter(NodePainters.<TalentNodeDef>line()
                .color(TalentTheme.edgeDim())
                .colorActive(TalentTheme.edgeLit()));

        // 节点（保留与专精节点相连的边）
        for (TalentNodeDef def : model.nodes()) {
            float[] lp = layout.get(def.id());
            float nx = lp != null ? lp[0] : def.x();
            float ny = lp != null ? lp[1] : def.y();
            GraphNode<TalentNodeDef> gn = new GraphNode<>(def.id(), nx, ny, def)
                    .radius(TalentTheme.radiusFor(def.type()));
            canvas.addNode(gn);
            nodeMap.put(def.id(), gn);
        }
        // 边：保留所有模型解析出的边（含与专精节点相连的边）
        for (var e : model.resolvedEdges()) {
            if (model.node(e.fromId()) != null && model.node(e.toId()) != null) {
                canvas.addEdge(e.fromId(), e.toId());
            }
        }

        // 初始相机：居中所有起点的平均位置
        if (!model.startNodeIds().isEmpty()) {
            float cx = 0, cy = 0;
            int count = 0;
            for (String sid : model.startNodeIds()) {
                TalentNodeDef s = model.node(sid);
                if (s == null) continue;
                float[] lp = layout.get(sid);
                cx += lp != null ? lp[0] : s.x();
                cy += lp != null ? lp[1] : s.y();
                count++;
            }
            if (count > 0) {
                cx /= count;
                cy /= count;
                canvas.camera().zoom(DEFAULT_ZOOM);
                canvas.camera().centerOn(cx, cy, rightPanelX, height);
            }
        }

        layoutRightPanel();

        // ---- 搜索框：TextInput，包在一个绝对定位的 Window 里 ----
        searchBoxX = contentX;
        searchBoxY = rowY_search;
        searchBoxW = contentW;
        searchBoxH = SEARCH_H;
        searchBox = new TextInput().styled().placeholder(Component.translatable("ui.coe.search"));
        searchBox.flex.height = SEARCH_H;
        searchBox.onChange(this::onSearchChanged);
        Window searchHost = Window.blank();
        searchHost.draggable(false);
        searchHost.consumeClicks(true);
        searchHost.flex.size(searchBoxW, searchBoxH);
        searchHost.position(searchBoxX, searchBoxY);
        searchHost.body().add(searchBox);

        controller.addOverlay(searchHost);
    }

    /** 各行 Y 起始：从上到下紧密排列。 */
    private int rowY_points, rowY_sets, rowY_search, rowY_zoom, rowY_summary;

    /** 计算右侧面板内各控件矩形：套装长条 / 缩放 / 属性加成按钮。 */
    private void layoutRightPanel() {
        // 行 Y 坐标
        rowY_points = 4;
        rowY_sets = rowY_points + 14 + 4;
        rowY_search = rowY_sets + (createdSets.size() + 1) * (SET_BAR_H + 2) + 4;
        rowY_zoom = rowY_search + SEARCH_H + 4;
        rowY_summary = rowY_zoom + ZOOM_H + 4;

        // ---- 套装长条 ----
        setBarRects.clear();
        int by = rowY_sets;
        for (int v : sortedSets()) {
            setBarRects.put(v, new int[]{contentX, by, contentW, SET_BAR_H});
            by += SET_BAR_H + 2;
        }
        // + 号条：未满时占一行
        if (createdSets.size() < TalentWeaponSet.MAX_BINDING) {
            setBarRects.put(0, new int[]{contentX, by, contentW, SET_BAR_H});
        }

        // ---- 缩放控件 ----
        int zy = rowY_zoom;
        int btnW = 12;
        int trackW = contentW - 2 * btnW - 4;
        int tx = contentX;
        zoomMinusRect = new int[]{tx, zy, btnW, ZOOM_H};
        zoomTrackRect = new int[]{tx + btnW + 2, zy + 3, trackW, ZOOM_H - 6};
        zoomPlusRect = new int[]{tx + btnW + 2 + trackW + 2, zy, btnW, ZOOM_H};

        // ---- 属性加成按钮 ----
        summaryBtnRectX = contentX;
        summaryBtnRectY = rowY_summary;

        // ---- 顶部操作：重置 / 保存 + 金币 ----
        rowY_reset = rowY_summary + SUMMARY_BTN_H + 4;
        rowY_gold = rowY_reset + ACTION_BTN_H + 3;
        rowY_status = rowY_gold + GOLD_ROW_H + 2;
        actionBtnW = (contentW - BTN_GAP) / 2;
        resetBtnRectX = contentX;
        resetBtnRectY = rowY_reset;
        saveBtnRectX = contentX + actionBtnW + BTN_GAP;
        saveBtnRectY = rowY_reset;
    }

    // ====== 待保存状态机 ======

    /**
     * 请求进入 / 保持加点待保存模式。
     *
     * @return false 表示被互斥规则拒绝（已给出提示），调用方应放弃本次操作。
     */
    private boolean requestAllocate() {
        if (pendingMode == PendingMode.NONE) {
            pendingMode = PendingMode.ALLOCATE;
            return true;
        }
        if (pendingMode == PendingMode.ALLOCATE) return true;
        showHint(Component.translatable("talent.coe.reject_alloc_during_reset"));
        return false;
    }

    /**
     * 顶部 [重置]：把当前所有分配做本地预览取消（照 {@link #deleteWeaponSet(int)} 的循环），
     * 进入 RESET 待保存态并计算本次花费；再次点击则取消这次待保存重置。
     */
    private void onResetClick() {
        if (awaitingCommit) return;
        if (pendingMode == PendingMode.ALLOCATE) {
            showHint(Component.translatable("talent.coe.reject_reset_during_allocate"));
            return;
        }
        if (pendingMode == PendingMode.RESET) {
            revertPending();
            return;
        }
        int points = allocator.allocations().size();
        if (points <= 0) {
            showHint(Component.translatable("talent.coe.respec_nothing"));
            return;
        }
        for (TalentAllocation a : allocator.allocations()) allocator.preview(a.id(), false);
        if (uiActiveSet != 0) selectWeaponSet(0);
        rebuildSetBars();
        pendingRespecPoints = points;
        pendingRespecCost = TalentRespecCost.total(player.experienceLevel, points);
        pendingMode = PendingMode.RESET;
        layoutRightPanel();
    }

    /** 顶部 [保存]：把客户端暂存的差异补齐给服务端，再发提交包；之后等待服务端回同步。 */
    private void onSaveClick() {
        if (pendingMode == PendingMode.NONE || awaitingCommit) return;
        if (pendingMode == PendingMode.RESET && !canAffordRespec()) {
            showHint(Component.translatable("talent.coe.respec_insufficient"));
            return;
        }
        Map<String, TalentAllocation> prev = new HashMap<>();
        for (TalentAllocation a : committedAllocations) prev.put(a.id(), a);
        for (TalentAllocation a : allocator.allocations()) {
            TalentAllocation p = prev.get(a.id());
            if (p == null || p.choice() != a.choice() || p.weaponSet() != a.weaponSet()
                    || !ItemStack.matches(p.socket(), a.socket())) {
                ClientPacketDistributor.sendToServer(new TalentActionPacket(
                        treeId, a.id(), TalentActionPacket.ALLOCATE, a.choice(), a.weaponSet()));
            }
        }
        for (TalentAllocation a : committedAllocations) {
            if (!allocator.isAllocated(a.id())) {
                ClientPacketDistributor.sendToServer(new TalentActionPacket(
                        treeId, a.id(), TalentActionPacket.DEALLOCATE));
            }
        }
        if (pendingMode == PendingMode.RESET) {
            ClientPacketDistributor.sendToServer(TalentCommitPacket.reset(treeId));
        } else {
            // ALLOCATE 必须携带全量期望分配集合：服务端据此权威重算（节点存在性/连通性/
            // 点数上限），空集合会被判定为「回收全部已分配节点」。上方 diff 循环已先行把
            // 逐节点改动经 TalentActionPacket 落库，此处提交与之一致（幂等重算，无副作用）。
            List<TalentCommitPacket.Entry> entries = allocator.allocations().stream()
                    .map(a -> new TalentCommitPacket.Entry(a.id(), a.choice(), a.weaponSet()))
                    .toList();
            ClientPacketDistributor.sendToServer(TalentCommitPacket.allocate(treeId, entries));
        }
        awaitingCommit = true;
        commitWaitTicks = COMMIT_WAIT_TICKS;
        showHint(Component.translatable("talent.coe.commit_sent"));
    }

    /** 放弃改动：本地回滚到已提交快照。 */
    private void revertPending() {
        allocator.syncAllocations(committedAllocations);
        rebuildSetBars();
        pendingMode = PendingMode.NONE;
        awaitingCommit = false;
        commitWaitTicks = 0;
        pendingRespecCost = 0;
        pendingRespecPoints = 0;
        layoutRightPanel();
        showHint(Component.translatable("talent.coe.reverted"));
    }

    /** 重置花费是否付得起。 */
    private boolean canAffordRespec() {
        return goldBalance >= pendingRespecCost;
    }

    /**
     * 每帧：刷新金币、递减提示与等待计时，并按待保存状态决定是否接受服务端回同步。
     * 待保存期间不由服务端回同步，否则本地预览会在下一帧被抹掉。
     */
    private void tickPendingState(TalentState st) {
        goldBalance = GoldState.get(player);
        if (hintTicks > 0) hintTicks--;
        if (pendingMode != PendingMode.NONE) {
            // 保存已发出：等到服务端状态与本地一致（或超时兜底承认服务端），避免保存瞬间预览被回滚成旧状态
            if (awaitingCommit) {
                commitWaitTicks--;
                if (sameAllocState(st.allocated(), allocator.allocations()) || commitWaitTicks <= 0) {
                    awaitingCommit = false;
                    commitWaitTicks = 0;
                    pendingMode = PendingMode.NONE;
                    pendingRespecCost = 0;
                    pendingRespecPoints = 0;
                    committedAllocations = List.copyOf(allocator.allocations());
                }
            }
            return;
        }
        if (!sameAllocState(st.allocated(), allocator.allocations())) {
            allocator.syncAllocations(st.allocated());
            rebuildSetBars();
            committedAllocations = List.copyOf(allocator.allocations());
        }
    }

    private void showHint(Component hint) {
        pendingHint = hint;
        hintTicks = HINT_TICKS;
    }

    /**
     * 服务端提交回执（NetEvent 转发到当前天赋树屏幕）：
     * 成功 → 本地预览即权威（服务端已接受），清除待保存态；
     * 失败 → 保持本地预览不变（服务端未改动存档与金币），提示原因，
     * 随后由既有 {@link #tickPendingState} 的服务端回同步/超时兜底把预览回滚到权威状态。
     */
    public void handleCommitResult(TalentCommitResultPacket r) {
        if (!this.treeId.equals(r.treeId()) || !awaitingCommit) return;
        awaitingCommit = false;
        commitWaitTicks = 0;
        if (r.success()) {
            pendingMode = PendingMode.NONE;
            pendingRespecCost = 0;
            pendingRespecPoints = 0;
            committedAllocations = List.copyOf(allocator.allocations());
            showHint(Component.literal("保存成功"));
        } else {
            showHint(reasonHint(r.reason()));
        }
    }

    /** 失败原因 → 提示行（复用现有 lang key，其余为简短中文，避免依赖 runData 重新生成）。 */
    private static Component reasonHint(int reason) {
        return switch (reason) {
            case TalentCommitResultPacket.REASON_NOT_ENOUGH_GOLD ->
                    Component.translatable("talent.coe.respec_insufficient");
            case TalentCommitResultPacket.REASON_TOO_MANY_POINTS -> Component.literal("点数超出上限");
            case TalentCommitResultPacket.REASON_UNKNOWN_NODE -> Component.literal("存在无效节点");
            case TalentCommitResultPacket.REASON_UNREACHABLE_NODE -> Component.literal("存在未连通节点");
            case TalentCommitResultPacket.REASON_BAD_MODE -> Component.literal("提交模式错误");
            default -> Component.literal("提交被拒绝");
        };
    }

    private List<Integer> sortedSets() {
        List<Integer> l = new ArrayList<>(createdSets.keySet());
        l.sort(Integer::compareTo);
        return l;
    }

    // ====== 武器套装操作 ======

    private void selectWeaponSet(int value) {
        uiActiveSet = value;
        allocator.setActiveSet(value);
    }

    private static String toRoman(int n) {
        if (n < 1 || n > 9) return String.valueOf(n);
        String[] rs = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return rs[n - 1];
    }

    /** 打开「选择栏位创建新套装」选择器。 */
    private void openSlotPicker(int clickX, int clickY) {
        List<Integer> free = new ArrayList<>();
        for (int s = 1; s <= TalentWeaponSet.MAX_BINDING; s++) {
            if (!createdSets.containsKey(s)) free.add(s);
        }
        if (free.isEmpty()) return;
        OptionListPopup<Integer> p = OptionListPopup.of(free, v -> Component.literal(toRoman(v)))
                .onPick(v -> {
                    createdSets.put(v, v);
                    layoutRightPanel();
                    selectWeaponSet(v);
                });
        openPopup(p, clickX, clickY);
    }

    /** 通用多选一 tooltip：选项为 label 列表，点击回调按值分发。 */
    private <T> void openPopup(OptionListPopup<T> p, int clickX, int clickY) {
        popup = p;
        p.anchorAt(clickX, clickY);
        p.popupStyle();
    }

    // ====== tooltip 构建 ======

    private List<Component> nodeTooltip(GraphNode<TalentNodeDef> node) {
        TalentNodeDef def = model.node(node.id);
        if (def == null) return null;
        List<Component> lines = new ArrayList<>();
        lines.add(def.displayName());
        lines.addAll(def.statLines());
        if (def.type().socket()) {
            ItemStack jewel = allocator.socketOf(node.id);
            if (!jewel.isEmpty()) lines.addAll(socketJewelTooltip(node.id));
        } else {
            lines.addAll(weaponSetTooltip(allocator.weaponSetOf(node.id)));
        }
        return lines;
    }

    private static List<Component> weaponSetTooltip(int binding) {
        Component c = TalentWeaponSet.isGlobal(binding)
                ? Component.translatable("talent.coe.weapon_set_global")
                : Component.translatable("talent.coe.weapon_set_hotbar",
                        TalentWeaponSet.slotOf(binding) + 1,
                        toRoman(TalentWeaponSet.slotOf(binding) + 1));
        return List.of(c);
    }

    private List<Component> socketJewelTooltip(String id) {
        ItemStack jewel = allocator.socketOf(id);
        if (jewel.isEmpty()) return List.of();
        JewelData data = Jewel.dataOf(jewel);
        if (data.radiates()) {
            return List.of(Component.translatable("jewel.coe.radius", (int) data.radius()));
        }
        return List.of(Component.translatable("jewel.coe.base"));
    }

    // ====== 节点点击 ======

    private void onNodeClick(GraphNode<TalentNodeDef> node) {
        boolean allocated = allocator.isAllocated(node.id);
        TalentNodeDef def = model.node(node.id);
        if (def != null && def.type().socket() && allocated) {
            onSocketClick(node, def);
            return;
        }
        if (allocated) {
            if (model.startNodeIds().contains(node.id)) return;
            if (!requestAllocate()) return;
            allocator.preview(node.id, false);
        } else {
            if (!allocator.canAllocate(node.id)) return;
            if (!requestAllocate()) return;
            if (def != null && def.hasChoice()) {
                openChoicePopup(node, def);
            } else {
                allocator.preview(node.id, true);
            }
        }
    }

    private void openChoicePopup(GraphNode<TalentNodeDef> node, TalentNodeDef def) {
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < def.choices().size(); i++) idxs.add(i);
        OptionListPopup<Integer> p = OptionListPopup.of(idxs, i -> Component.literal(
                        def.choices().get(i).describe().stream()
                                .map(Component::getString)
                                .collect(Collectors.joining(" "))))
                .onPick(chosenIdx -> {
                    allocator.preview(node.id, true);
                    allocator.setChoice(node.id, chosenIdx);
                });
        openPopupAtNode(p, node);
    }

    private void onSocketClick(GraphNode<TalentNodeDef> node, TalentNodeDef def) {
        ItemStack current = allocator.socketOf(node.id);
        if (!current.isEmpty()) {
            allocator.setSocket(node.id, ItemStack.EMPTY);
            ClientPacketDistributor.sendToServer(
                    new TalentSocketPacket(treeId, node.id, TalentSocketPacket.OUT, -1));
            return;
        }
        Inventory inv = player.getInventory();
        List<Integer> jewelSlots = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof Jewel) jewelSlots.add(i);
        }
        if (jewelSlots.isEmpty()) {
            if (!requestAllocate()) return;
            allocator.preview(node.id, false);
            return;
        }
        // 加一个「取消点亮插槽」选项
        List<Integer> options = new ArrayList<>(jewelSlots);
        options.add(-1); // -1 = 取消
        OptionListPopup<Integer> p = OptionListPopup.of(options,
                        s -> s == -1 ? Component.translatable("talent.coe.cancel_socket")
                                : Component.literal(Jewel.dataOf(inv.getItem(s)).name().getString()))
                .onPick(slot -> {
                    if (slot == -1) {
                        if (!requestAllocate()) return;
                        allocator.preview(node.id, false);
                        return;
                    }
                    ItemStack jewel = inv.getItem(slot).copyWithCount(1);
                    allocator.setSocket(node.id, jewel);
                    ClientPacketDistributor.sendToServer(
                            new TalentSocketPacket(treeId, node.id, TalentSocketPacket.IN, slot));
                });
        openPopupAtNode(p, node);
    }

    private void openPopupAtNode(OptionListPopup<?> p, GraphNode<?> node) {
        int sx = (int) canvas.camera().worldToScreenX(node.worldX) + canvas.absX;
        int sy = (int) canvas.camera().worldToScreenY(node.worldY) + canvas.absY;
        openPopup(p, sx, sy);
    }

    // ====== 渲染 ======

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, Theme.current().talentScreenBg());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractRenderState(g, mx, my, partialTick);

        // 权威同步（待保存期间由 tickPendingState 挡住，保留本地预览）
        TalentState st = player.getData(AttachmentRegistry.TALENT_TREE).get(treeId).state();
        tickPendingState(st);

        // ---- 画布 + 搜索框（UI 树） ----
        controller.render(g, mx, my, partialTick);

        // ---- 标题：屏幕正中央上方 ----
        String title = treeDisplayName().getString();
        int titleW = Minecraft.getInstance().font.width(title);
        g.text(Minecraft.getInstance().font, Component.literal(title),
                (width - titleW) / 2, 4, Theme.current().talentAccent(), true);

        // ---- 右侧面板 ----
        drawRightPanel(g, mx, my);

        // ---- 属性加成面板（向下展开至屏底） ----
        if (summaryOpen) {
            summaryCache.clear();
            summaryCache.addAll(buildSummaryLines());
            drawSummaryPanel(g, mx, my);
        }

        // ---- 顶层弹窗选择器（最后画，盖住一切） ----
        if (popup != null && popup.visible) {
            popup.layoutSelf();
            popup.render(g, mx, my, partialTick);
        }
    }

    /** 右侧信息面板：半透明深色背景 + 各行信息。 */
    private void drawRightPanel(GuiGraphicsExtractor g, int mx, int my) {
        int panelBottom = summaryOpen ? height : rowY_status + STATUS_ROW_H + 4;
        g.fill(rightPanelX, 0, width, panelBottom, Theme.current().talentPanelBg());
        g.fill(rightPanelX - 1, 0, rightPanelX, panelBottom, Theme.current().talentPanelBorder());

        // ---- 1. 可用天赋点数 ----
        Component pts = Component.translatable("talent.coe.points",
                allocator.remainingForActiveSet(), allocator.totalPoints());
        int ptsW = Minecraft.getInstance().font.width(pts);
        g.text(Minecraft.getInstance().font, pts,
                rightPanelX + (rightPanelW - ptsW) / 2,
                rowY_points + 1, Theme.current().talentTextPoints(), true);

        // ---- 2. 武器套装长条 + + 号条 ----
        drawSetBars(g, mx, my);

        // ---- 3. 搜索框边框（TextInput 自渲染内容） ----
        g.outline(searchBoxX - 1, searchBoxY - 1, searchBoxW + 2, searchBoxH + 2, Theme.current().talentEdgeDim());

        // ---- 4. 缩放控件 ----
        drawZoomControls(g, mx, my);

        // ---- 5. 属性加成下拉按钮 ----
        int btnX = summaryBtnRectX, btnY = summaryBtnRectY;
        int btnW = contentW, btnH = SUMMARY_BTN_H;
        boolean sHover = RenderHelper.isHoverArea(mx, my, btnX, btnY, btnW, btnH);
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, sHover ? Theme.current().talentControlHoverBg() : Theme.current().talentControlBg());
        g.outline(btnX, btnY, btnW, btnH, summaryOpen ? Theme.current().talentOk() : Theme.current().textDisabled());
        g.centeredText(Minecraft.getInstance().font,
                Component.literal(summaryOpen ? "▲" : "▼").append(Component.translatable("talent.coe.stat_bonus")),
                btnX + btnW / 2, btnY + (btnH - 8) / 2, summaryOpen ? Theme.current().talentTextSummary() : Theme.current().talentTextIdle());

        // ---- 6. 重置 / 保存 按钮 ----
        drawActionButtons(g, mx, my);

        // ---- 7. 金币余额与待保存花费 / 提示 ----
        drawGoldRows(g);
    }

    /** 顶部操作行：[重置]（danger，仅在无待保存改动时可用）与 [保存]（仅在有待保存改动时可用）。 */
    private void drawActionButtons(GuiGraphicsExtractor g, int mx, int my) {
        boolean resetEnabled = pendingMode == PendingMode.NONE;
        boolean saveEnabled = pendingMode != PendingMode.NONE && !awaitingCommit;
        drawPanelButton(g, mx, my, resetBtnRectX, resetBtnRectY, actionBtnW, ACTION_BTN_H,
                Component.translatable("talent.coe.respec"), resetEnabled, true);
        drawPanelButton(g, mx, my, saveBtnRectX, saveBtnRectY, actionBtnW, ACTION_BTN_H,
                Component.translatable("talent.coe.save"), saveEnabled, false);
    }

    /** 三段式按钮（fill + outline + centeredText），与属性加成按钮同一套画法。 */
    private void drawPanelButton(GuiGraphicsExtractor g, int mx, int my, int x, int y, int w, int h,
                                 Component label, boolean enabled, boolean danger) {
        boolean hover = enabled && RenderHelper.isHoverArea(mx, my, x, y, w, h);
        int bg, border, text;
        if (!enabled) {
            bg = Theme.current().talentControlBg();
            border = Theme.current().textDisabled();
            text = Theme.current().textDisabled();
        } else if (danger) {
            bg = hover ? Theme.current().talentDangerHoverBg() : Theme.current().talentDangerBg();
            border = Theme.current().talentDangerBorder();
            text = Theme.current().talentDangerText();
        } else {
            bg = hover ? Theme.current().talentPrimaryHoverBg() : Theme.current().talentControlBg();
            border = Theme.current().talentOk();
            text = Theme.current().talentPrimaryTextHot();
        }
        g.fill(x, y, x + w, y + h, bg);
        g.outline(x, y, w, h, border);
        g.centeredText(Minecraft.getInstance().font, label, x + w / 2, y + (h - 8) / 2, text);
    }

    /** 金币余额行 + 状态行（待保存花费 / 未保存加点 / 短暂提示）。 */
    private void drawGoldRows(GuiGraphicsExtractor g) {
        g.text(Minecraft.getInstance().font,
                Component.translatable("talent.coe.gold", formatGold(goldBalance)),
                contentX, rowY_gold + 1, Theme.current().goldAccent(), true);
        Component status = statusLine();
        if (status != null) {
            g.text(Minecraft.getInstance().font, status,
                    contentX, rowY_status + 1, statusColor(), true);
        }
    }

    private Component statusLine() {
        if (hintTicks > 0 && pendingHint != null) return pendingHint;
        if (pendingMode == PendingMode.RESET) {
            return Component.translatable("talent.coe.respec_cost",
                    formatGold(pendingRespecCost), pendingRespecPoints);
        }
        if (pendingMode == PendingMode.ALLOCATE) {
            return Component.translatable("talent.coe.unsaved_allocations");
        }
        return null;
    }

    private int statusColor() {
        if (hintTicks > 0) return Theme.current().talentDangerText();
        if (pendingMode == PendingMode.RESET) {
            return canAffordRespec() ? Theme.current().talentOk() : Theme.current().talentDangerText();
        }
        return Theme.current().talentPreview();
    }

    private static String formatGold(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    /** 武器套装长条：每个套装渲染为 POE2 风格长框。 */
    private void drawSetBars(GuiGraphicsExtractor g, int mx, int my) {
        for (Map.Entry<Integer, int[]> e : setBarRects.entrySet()) {
            int v = e.getKey();
            int[] r = e.getValue();
            if (v == 0) {
                drawAddBar(g, r, RenderHelper.isHoverArea(mx, my, r[0], r[1], r[2], r[3]));
            } else {
                boolean active = (v == uiActiveSet);
                drawSetBar(g, r, v, active,
                        RenderHelper.isHoverArea(mx, my, r[0], r[1], r[2], r[3]));
            }
        }
    }

    /**
     * 绘制一个套装长框。
     * 内部布局：左 12 选中箭头 | 18 段标号 | 中部「武器套装」| 右 50 「已用 (剩余)」| 右 12 删除箭头
     */
    private void drawSetBar(GuiGraphicsExtractor g, int[] r, int v, boolean active, boolean hover) {
        int x = r[0], y = r[1], w = r[2], h = r[3];
        int arrowW = 12;
        int numW = 18;
        int counterW = 56;
        int gapInner = 4;
        int arrowL = x;
        int numX = arrowL + arrowW;
        int counterX = x + w - counterW - arrowW;
        int delX = x + w - arrowW;
        int midX = numX + numW + gapInner;

        int bg, border;
        if (active) {
            bg = Theme.current().talentTabActiveBg();
            border = Theme.current().talentAccent();
        } else if (hover) {
            bg = Theme.current().talentTabBg();
            border = Theme.current().talentTabBorder();
        } else {
            bg = Theme.current().talentTabIdleBg();
            border = Theme.current().talentEdgeDim();
        }
        g.fill(x, y, x + w, y + h, bg);
        g.outline(x, y, w, h, border);

        g.fill(arrowL, y, arrowL + arrowW, y + h, active ? Theme.current().talentTabActiveBg() : Theme.current().talentArrowIdleBg());
        g.outline(arrowL, y, arrowW, h, active ? Theme.current().talentAccent() : Theme.current().talentEdgeDim());
        g.centeredText(Minecraft.getInstance().font, Component.literal("<"),
                arrowL + arrowW / 2, y + (h - 8) / 2, active ? Theme.current().textBright() : Theme.current().talentTextIdle());

        int numColor = active ? Theme.current().talentNumActive() : Theme.current().talentTextIdle();
        g.centeredText(Minecraft.getInstance().font, Component.literal(toRoman(v)),
                numX + numW / 2, y + (h - 8) / 2, numColor);

        Component label = Component.translatable("talent.coe.weapon_set");
        g.text(Minecraft.getInstance().font, label,
                midX + 2, y + (h - 8) / 2, Theme.current().talentTextNeutral(), true);

        int used = allocator.usedForSet(v);
        int rem = Math.max(0, allocator.totalPoints() - used);
        String counter = used + " (" + rem + ")";
        g.text(Minecraft.getInstance().font, Component.literal(counter),
                counterX + counterW - Minecraft.getInstance().font.width(counter) - 2,
                y + (h - 8) / 2, Theme.current().talentPointsText(), true);

        g.fill(delX, y, delX + arrowW, y + h, hover ? Theme.current().talentDangerHoverBg() : Theme.current().talentDangerBg());
        g.outline(delX, y, arrowW, h, Theme.current().talentDangerBorder());
        g.centeredText(Minecraft.getInstance().font, Component.literal("X"),
                delX + arrowW / 2, y + (h - 8) / 2, Theme.current().talentDangerText());
    }

    /** + 号长条：未填满栏位时占一行；点击打开栏位选择器。 */
    private void drawAddBar(GuiGraphicsExtractor g, int[] r, boolean hover) {
        int x = r[0], y = r[1], w = r[2], h = r[3];
        g.fill(x, y, x + w, y + h, hover ? Theme.current().talentPrimaryHoverBg() : Theme.current().talentTabIdleBg());
        g.outline(x, y, w, h, Theme.current().talentTabBorder());
        g.centeredText(Minecraft.getInstance().font, Component.translatable("talent.coe.create_set"),
                x + w / 2, y + (h - 8) / 2, hover ? Theme.current().talentPrimaryTextHot() : Theme.current().talentPreview());
    }

    /** 缩放控件：[-] 轨道 [+] 与缩放数值。轨道支持点击跳位与拖动。 */
    private void drawZoomControls(GuiGraphicsExtractor g, int mx, int my) {
        int[] tr = zoomTrackRect;
        g.fill(tr[0], tr[1], tr[0] + tr[2], tr[1] + tr[3], Theme.current().talentControlTrackBg());
        g.outline(tr[0], tr[1], tr[2], tr[3], Theme.current().talentEdgeDim());
        float z = canvas.camera().zoom;
        float logMin = (float) Math.log(MIN_ZOOM);
        float logMax = (float) Math.log(MAX_ZOOM);
        float t = ((float) Math.log(z) - logMin) / (logMax - logMin);
        t = Math.max(0f, Math.min(1f, t));
        int knobW = 4;
        int knobX = (int) (tr[0] + t * (tr[2] - knobW));
        g.fill(knobX, tr[1] - 1, knobX + knobW, tr[1] + tr[3] + 1,
                draggingZoom ? Theme.current().talentAccent() : Theme.current().talentAccentDim());
        int[] mr = zoomMinusRect;
        g.fill(mr[0], mr[1], mr[0] + mr[2], mr[1] + mr[3],
                RenderHelper.isHoverArea(mx, my, mr[0], mr[1], mr[2], mr[3]) ? Theme.current().talentControlHoverBg() : Theme.current().talentControlBg());
        g.outline(mr[0], mr[1], mr[2], mr[3], Theme.current().textDisabled());
        g.centeredText(Minecraft.getInstance().font, Component.literal("-"),
                mr[0] + mr[2] / 2, mr[1] + (mr[3] - 8) / 2, Theme.current().talentTextIdle());
        int[] pr = zoomPlusRect;
        g.fill(pr[0], pr[1], pr[0] + pr[2], pr[1] + pr[3],
                RenderHelper.isHoverArea(mx, my, pr[0], pr[1], pr[2], pr[3]) ? Theme.current().talentControlHoverBg() : Theme.current().talentControlBg());
        g.outline(pr[0], pr[1], pr[2], pr[3], Theme.current().textDisabled());
        g.centeredText(Minecraft.getInstance().font, Component.literal("+"),
                pr[0] + pr[2] / 2, pr[1] + (pr[3] - 8) / 2, Theme.current().talentTextIdle());
    }

    /** 属性加成下拉面板：向下展开至屏幕底部，宽度 = 右侧面板内容宽。 */
    private void drawSummaryPanel(GuiGraphicsExtractor g, int mx, int my) {
        int panelX = rightPanelX;
        int panelY = rowY_status + STATUS_ROW_H + 2;
        int panelH = height - panelY - 2;
        int panelW = rightPanelW;
        summaryPanelRect = new int[]{panelX, panelY, panelW, panelH};

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, Theme.current().talentSummaryBg());
        g.outline(panelX, panelY, panelW, panelH, Theme.current().talentOk());

        Component header = Component.translatable("talent.coe.summary_header",
                uiActiveSet == 0 ? Component.translatable("talent.coe.global") : toRoman(uiActiveSet));
        g.text(Minecraft.getInstance().font, header,
                panelX + PAD, panelY + 4, Theme.current().talentTextSummary(), true);

        int innerX = panelX + PAD;
        int innerY = panelY + SUMMARY_HEADER_H;
        int innerH = panelH - SUMMARY_HEADER_H - PAD;

        int visibleRows = innerH / SUMMARY_ROW_H;
        int totalRows = summaryCache.size();
        int maxScroll = Math.max(0, totalRows - visibleRows);
        if (summaryScroll > maxScroll) summaryScroll = maxScroll;
        if (summaryScroll < 0) summaryScroll = 0;

        if (summaryCache.isEmpty()) {
            g.text(Minecraft.getInstance().font, Component.translatable("ui.coe.none"),
                    innerX, innerY, Theme.current().talentTextDim(), true);
        } else {
            for (int i = 0; i < visibleRows; i++) {
                int idx = i + summaryScroll;
                if (idx >= summaryCache.size()) break;
                int ly = innerY + i * SUMMARY_ROW_H;
                g.text(Minecraft.getInstance().font, summaryCache.get(idx),
                        innerX, ly, Theme.current().talentTextSoft(), true);
            }
        }
        if (totalRows > visibleRows && visibleRows > 0) {
            int sbX = panelX + panelW - 10;
            int sbY = innerY;
            int sbH = innerH;
            g.fill(sbX, sbY, sbX + 6, sbY + sbH, Theme.current().talentScrollTrackBg());
            g.outline(sbX, sbY, 6, sbH, Theme.current().talentOk());
            int knobH = Math.max(8, sbH * visibleRows / totalRows);
            int knobY = sbY + (maxScroll > 0 ? (sbH - knobH) * summaryScroll / maxScroll : 0);
            g.fill(sbX, knobY, sbX + 6, knobY + knobH, Theme.current().talentTextSummary());
        }
    }

    // ====== 鼠标事件 ======

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int mx = (int) event.x();
        int my = (int) event.y();

        // 弹窗选择器：打开时优先消费
        if (popup != null && popup.visible && popup.mouseClicked(mx, my, event.button())) {
            return true;
        }

        // 缩放滑块拖动
        if (RenderHelper.isHoverArea(mx, my, zoomTrackRect[0], zoomTrackRect[1], zoomTrackRect[2], zoomTrackRect[3])) {
            draggingZoom = true;
            applyZoomFromTrackX(mx);
            return true;
        }
        // 缩放 [-]
        if (RenderHelper.isHoverArea(mx, my, zoomMinusRect[0], zoomMinusRect[1], zoomMinusRect[2], zoomMinusRect[3])) {
            zoomBy(0.8f, mx, my);
            return true;
        }
        // 缩放 [+]
        if (RenderHelper.isHoverArea(mx, my, zoomPlusRect[0], zoomPlusRect[1], zoomPlusRect[2], zoomPlusRect[3])) {
            zoomBy(1.25f, mx, my);
            return true;
        }
        // 属性加成下拉按钮
        if (RenderHelper.isHoverArea(mx, my, summaryBtnRectX, summaryBtnRectY, contentW, SUMMARY_BTN_H)) {
            summaryOpen = !summaryOpen;
            summaryScroll = 0;
            return true;
        }
        // 顶部操作：重置 / 保存
        if (RenderHelper.isHoverArea(mx, my, resetBtnRectX, resetBtnRectY, actionBtnW, ACTION_BTN_H)) {
            onResetClick();
            return true;
        }
        if (RenderHelper.isHoverArea(mx, my, saveBtnRectX, saveBtnRectY, actionBtnW, ACTION_BTN_H)) {
            onSaveClick();
            return true;
        }
        // 套装长条
        for (Map.Entry<Integer, int[]> e : setBarRects.entrySet()) {
            int v = e.getKey();
            int[] r = e.getValue();
            if (!RenderHelper.isHoverArea(mx, my, r[0], r[1], r[2], r[3])) continue;
            if (v == 0) {
                openSlotPicker(mx, my);
                return true;
            }
            int arrowW = 12;
            int clickX = mx - r[0];
            if (clickX < arrowW) {
                if (uiActiveSet == v) selectWeaponSet(0);
                else selectWeaponSet(v);
                return true;
            } else if (clickX > r[2] - arrowW) {
                deleteWeaponSet(v);
                return true;
            }
            if (uiActiveSet == v) selectWeaponSet(0);
            else selectWeaponSet(v);
            return true;
        }

        // UI 树（画布 + 搜索框）：画布视口只在面板以西，不会吃掉面板点击
        if (controller.mouseClicked(mx, my, event.button())) {
            return true;
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingZoom) {
            applyZoomFromTrackX((int) event.x());
            return true;
        }
        if (controller.mouseDragged((int) event.x(), (int) event.y(), event.button(), dx, dy)) {
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingZoom) {
            draggingZoom = false;
            return true;
        }
        if (controller.mouseReleased((int) event.x(), (int) event.y(), event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    /** 按轨道上的 X 位置计算并应用缩放（对数刻度）。 */
    private void applyZoomFromTrackX(int mx) {
        int[] tr = zoomTrackRect;
        float t = (mx - tr[0]) / (float) tr[2];
        t = Math.max(0f, Math.min(1f, t));
        float logMin = (float) Math.log(MIN_ZOOM);
        float logMax = (float) Math.log(MAX_ZOOM);
        float newZoom = (float) Math.exp(logMin + t * (logMax - logMin));
        canvas.camera().zoom(newZoom);
    }

    private void zoomBy(float factor, int mx, int my) {
        // 缩放锚点：画布内对应鼠标位置（仅在画布范围内）
        int ww = canvas.width, wh = canvas.height;
        int sx, sy;
        if (RenderHelper.isHoverArea(mx, my, canvas.absX, canvas.absY, ww, wh)) {
            sx = mx - canvas.absX;
            sy = my - canvas.absY;
        } else {
            sx = ww / 2;
            sy = wh / 2;
        }
        canvas.camera().zoomAt(sx, sy, factor);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 属性面板：滚轮滚动
        if (summaryOpen && summaryPanelRect != null) {
            if (RenderHelper.isHoverArea(mouseX, mouseY, summaryPanelRect[0], summaryPanelRect[1],
                    summaryPanelRect[2], summaryPanelRect[3])) {
                int innerH = summaryPanelRect[3] - SUMMARY_HEADER_H - PAD;
                int visibleRows = Math.max(1, innerH / SUMMARY_ROW_H);
                int totalRows = summaryCache.size();
                int maxScroll = Math.max(0, totalRows - visibleRows);
                summaryScroll -= (int) Math.signum(scrollY);
                summaryScroll = Math.max(0, Math.min(maxScroll, summaryScroll));
                return true;
            }
        }
        if (controller.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** 删除一个套装：本地预览取消该套装的所有节点分配并从 UI 集合移除（改动待保存）。 */
    private void deleteWeaponSet(int v) {
        if (!requestAllocate()) return;
        List<String> toRemove = new ArrayList<>();
        for (TalentAllocation a : allocator.allocations()) {
            if (a.weaponSet() == v) toRemove.add(a.id());
        }
        for (String id : toRemove) {
            allocator.preview(id, false);
        }
        createdSets.remove(v);
        if (uiActiveSet == v) selectWeaponSet(0);
        layoutRightPanel();
    }

    // ====== 辅助 ======

    /** 搜索输入：更新画布高亮集合。 */
    private void onSearchChanged(String text) {
        String query = (text == null ? "" : text.trim().toLowerCase(Locale.ROOT));
        Set<String> hs = new HashSet<>();
        if (!query.isEmpty()) {
            for (TalentNodeDef n : model.nodes()) {
                if (n.name().toLowerCase(Locale.ROOT).contains(query)) hs.add(n.id());
            }
        }
        talentPainter.setHighlight(hs);
    }

    private static boolean sameAllocState(List<TalentAllocation> a, List<TalentAllocation> b) {
        if (a.size() != b.size()) return false;
        Map<String, TalentAllocation> map = new HashMap<>();
        for (TalentAllocation x : a) map.put(x.id(), x);
        for (TalentAllocation y : b) {
            TalentAllocation x = map.get(y.id());
            if (x == null || x.choice() != y.choice()) return false;
            if (!ItemStack.matches(x.socket(), y.socket())) return false;
        }
        return true;
    }

    private List<Component> buildSummaryLines() {
        // 收集当前预览（已激活）分配的全部属性亚型条目 → 临时 StatStore，
        // 经 StatEngine.accumulateGroupedStat 按 Stat 身份「累加」数值（尊重 tag/condition 门控、非 fold）：
        // 同身份数值相加 = 纯数值贡献；重置使预览集为空 -> 聚合为空 -> 不渲染任何相反数，自然归零。
        List<StatEntry> previewEntries = new ArrayList<>();
        for (TalentAllocation a : allocator.allocations()) {
            if (!allocator.isActive(a.id())) continue;
            TalentNodeDef def = model.node(a.id());
            if (def == null) continue;
            List<TalentEffect> effs = (def.hasChoice() && a.choice() >= 0 && a.choice() < def.choices().size())
                    ? List.of(def.choices().get(a.choice())) : def.effects();
            for (TalentEffect eff : effs) {
                if (eff instanceof StatTalentEffect ste) {
                    previewEntries.addAll(ste.entries());
                }
            }
        }
        List<Component> lines = new ArrayList<>();
        if (previewEntries.isEmpty()) return lines;
        StatStore previewStore = new StatStore(previewEntries);
        // 按 Stat 身份聚合（尊重 tag/condition 门控、非 fold）：同身份数值相加 = 纯数值贡献。
        // 重置使预览集为空 -> 聚合为空 -> 不渲染任何相反数，自然归零。
        Map<StatEngine.StatIdentity, Double> contrib = StatEngine.accumulateGroupedStat(previewStore, player);
        for (Map.Entry<StatEngine.StatIdentity, Double> e : contrib.entrySet()) {
            double v = e.getValue();
            if (Math.abs(v) < 1e-9) continue;
            lines.add(formatContribution(e.getKey(), v));
        }
        return lines;
    }

    /**
     * 聚合身份 -> 一行贡献文本：复用 ModifierStatEffect 的本地化效果片段（±号 / 运算口径 / 属性名）。
     * 非 fold，不渲染相反数。
     *
     * <p>外溢 / 转换作用域经 {@link StatEngine.StatIdentity} 的同名字段传入 {@link StatScopeDescriber}：
     * 聚合身份本身就把这两个字段各自成列（否则「计入 Y」与「转换为 Y」会被并成同一行），
     * 若在此处另造一个 {@code StatEntry.of(...)}，这两个字段会被丢掉、文本回退成与来源条目逐字相同。
     */
    private static Component formatContribution(StatEngine.StatIdentity identity, double value) {
        ModifierStatEffect effect = identity.effect();
        StatEntry tmp = StatEntry.of(ExpressionCodec.of(value), List.of(), effect);
        List<Component> frags = effect.describeFragment(tmp);
        if (frags.isEmpty()) return Component.empty();
        return StatScopeDescriber.wrap(frags.get(0), identity.spillTo(), identity.convertTo());
    }

    private void rebuildSetBars() {
        Set<Integer> fromAlloc = new HashSet<>();
        for (TalentAllocation a : allocator.allocations()) {
            int s = a.weaponSet();
            if (s >= 1 && s <= TalentWeaponSet.MAX_BINDING) fromAlloc.add(s);
        }
        boolean changed = false;
        Iterator<Integer> it = createdSets.keySet().iterator();
        while (it.hasNext()) {
            int k = it.next();
            if (!fromAlloc.contains(k)) {
                it.remove();
                changed = true;
            }
        }
        for (int s : fromAlloc) {
            if (!createdSets.containsKey(s)) {
                createdSets.put(s, s);
                changed = true;
            }
        }
        if (changed) layoutRightPanel();
    }

    protected Component treeDisplayName() {
        return TalentTreeModelLoader.displayName(treeId);
    }

    /** 将布局坐标统一缩放，使天赋树在视觉上更紧凑（节点半径不缩放）。 */
    private static Map<String, float[]> scaleLayout(Map<String, float[]> layout, float scale) {
        Map<String, float[]> scaled = new LinkedHashMap<>(layout.size());
        for (Map.Entry<String, float[]> e : layout.entrySet()) {
            float[] p = e.getValue();
            scaled.put(e.getKey(), new float[]{p[0] * scale, p[1] * scale});
        }
        return scaled;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ====== 退出保护 ======

    /**
     * 关闭界面（Esc 与背包键最终都汇聚到这里）：有未保存改动时不直接关，
     * 先弹三选一选择界面（保存并退出 / 放弃改动并退出 / 取消）。
     */
    @Override
    public void onClose() {
        if (pendingMode != PendingMode.NONE && !awaitingCommit) {
            openExitPopup();
            return;
        }
        super.onClose();
    }

    /** 屏幕移出时注销当前实例（无论 onClose 是否被退出保护拦截，removed 一定被调）。 */
    @Override
    public void removed() {
        if (current == this) current = null;
        super.removed();
    }

    /** Esc 优先关掉已打开的弹窗，避免与退出保护互相套娃。 */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape() && popup != null && popup.visible) {
            popup.close();
            return true;
        }
        return super.keyPressed(event);
    }

    /** 未保存改动时的退出选择界面。 */
    private void openExitPopup() {
        OptionListPopup<Void> p = OptionListPopup.<Void>dialog(Component.translatable("talent.coe.exit_message"))
                .action(Component.translatable("talent.coe.exit_save"), () -> {
                    onSaveClick();
                    // 保存被拒（如金币不足）时不关闭，留在界面等玩家处理
                    if (pendingMode == PendingMode.NONE || awaitingCommit) forceClose();
                })
                .action(Component.translatable("talent.coe.exit_discard"), () -> {
                    revertPending();
                    forceClose();
                })
                .action(Component.translatable("talent.coe.exit_cancel"), () -> {
                });
        p.applyAutoSize();
        p.flex.width = 180; // 说明文字较长，按内容自动宽度会缩到最小 48
        p.center(width, height);
        p.popupStyle();
        p.open();
        popup = p;
    }

    /** 绕过未保存保护真正关闭界面。 */
    private void forceClose() {
        pendingMode = PendingMode.NONE;
        awaitingCommit = false;
        super.onClose();
    }

    /** 待保存模式：加点与重置互斥，二者不能同时待保存。 */
    private enum PendingMode {
        NONE, ALLOCATE, RESET
    }
}
