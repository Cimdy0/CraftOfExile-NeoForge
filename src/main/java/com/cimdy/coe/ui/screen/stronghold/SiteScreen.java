package com.cimdy.coe.ui.screen.stronghold;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.network.stronghold.SiteSetting;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.button.ToggleWidget;
import com.cimdy.coe.ui.widget.component.panel.ToastHost;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.LabeledSlider;
import com.cimdy.coe.ui.widget.compose.WorkerRow;
import com.cimdy.coe.ui.widget.compose.business.SiteForm;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/**
 * 站点设置界面（M2 重构，260×230 两栏）。
 *
 * <p>{@link SiteForm}：优先级<b>分歧式档位格</b>（四·七 A + C.3：{@link com.cimdy.coe.ui.widget.compose.badge.StatusGlyph#bars}
 * 五档语义色 红→中性→绿，经 {@link SiteForm#priorityBars} 装配，值域 -2..2）+ 自动派遣总闸 + 工人步进 + 只读 {@link WorkerRow} + 存储摘要。
 * <p><b>四·七 B：无「保存并同步」按钮</b> —— 优先级 / 自动派遣开关 / 工人步进均<b>变更即发包</b>；
 * 成功给 200ms 轻 Toast（{@link #synced()}），滑杆拖拽走行内状态不弹 Toast 避免刷屏。
 * 发包仍走 {@link SiteSetting} <b>整体包</b>（⚠️ 待服务端支持单值变更，先本地即时生效）。
 * <p><b>C.3</b>：原右栏据点状态卡（据点名/等级/能量 + 改名/升级）属据点级数据，已移出站点右栏——
 * 能量见 TerminalScreen 顶部 EnergyBar，据点名/等级/改名/升级归据点总览；本站点屏只留站点级数据。
 * {@code site==null} 空态保留。
 */
public class SiteScreen extends WidgetScreen {

    private static final int WINDOW_WIDTH = 260;
    private static final int WINDOW_HEIGHT = 230;
    private static final int PRIORITY_COUNT = 5;

    private final Level level;
    private final GlobalPos terminalPos;
    private final BlockPos sitePos;
    private final Site site;

    private int priority;
    private boolean autoAssign;
    private int maxWorkers;
    private final int maxWorkersCap;
    private final int assignedWorkers;
    private final int assignedStorage;

    public SiteScreen(Player player, GlobalPos terminalPos, BlockPos sitePos) {
        super(Component.empty());
        this.level = player.level();
        this.terminalPos = terminalPos;
        this.sitePos = sitePos;

        Stronghold hold = StrongholdHelper.getHoldAt(level, terminalPos.pos());
        this.site = hold == null ? null : hold.getSite(sitePos).orElse(null);
        if (site == null) {
            this.maxWorkersCap = 1;
            this.assignedWorkers = 0;
            this.assignedStorage = 0;
            return;
        }
        this.priority = site.getPriority();
        this.autoAssign = site.isAutoAssign();
        this.maxWorkers = site.getMaxWorkers();
        this.assignedWorkers = site.getAssignedWorker();
        this.assignedStorage = site.getAssignedStorage();
        this.maxWorkersCap = Math.max(1, hold.entities().size());
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    @Override
    protected void buildUi(WindowLayer root) {
        if (site == null) {
            // 站点在打开窗口间隙被移除 / 据点失效：保留 M0 空态提示面板
            Window invalid = CoeUi.panel(Component.translatable("hold.coe.site.not_found"), WINDOW_WIDTH, WINDOW_HEIGHT)
                    .onClose(this::onClose)
                    .center(this.width, this.height);
            Scrollable body = invalid.scrollBody();
            body.itemGap(2).contentPadding(CoeUi.PAD);
            invalid.add(CoeUi.paragraph(Component.translatable("hold.coe.site.not_found.desc"), 40));
            root.add(invalid);
            return;
        }

        Window window = CoeUi.panel(site.getName(), WINDOW_WIDTH, WINDOW_HEIGHT)
                .onClose(this::onClose)
                .center(this.width, this.height);

        // 默认附件（STANDARD，克制）
        AttachmentPresets preset = AttachmentPresets.defaults();
        window.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.SITE),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.SITE),
                AttachmentPresets.vfxFor(preset, AttachmentPresets.Target.SITE));

        // 左栏：站点设置表单（站点级：优先级 / 自动派遣 / 工人步进 / 已指派工人 / 存储摘要）
        SiteForm form = buildForm();

        // C.3：原右栏 HoldStatusCard（据点名/等级/能量 + 禁用态改名框/升级按钮）全部属【据点级】数据，已移出站点右栏。
        // 去向：· 据点能量 → TerminalScreen 顶部 EnergyBar 已有展示（保持不动）；
        //      · 据点名 / 等级 / 改名 / 升级 / 任务模式 → 据点总览级数据，归 TerminalScreen；
        // 本 shard 只从站点右栏移除，不在此新增 TerminalScreen 展示（TerminalScreen 侧已有则保持）。
        // 站点右栏只留站点级（位置/状态/工人数/存储摘要）——工人数与存储摘要已在左栏 SiteForm 内，
        // 故右栏不再单独成列，表单单列铺满。
        Node columns = new Node().row().gap(CoeUi.PAD);
        columns.flex.grow = 1;
        Node left = new Node().column().gap(2);
        left.flex.grow = 1;
        left.add(form);
        columns.add(left);

        window.add(columns);
        root.add(window);
    }

    private SiteForm buildForm() {
        // 四·七 A：优先级用 SiteForm.priorityBars（StatusGlyph.bars 分歧式档位格，皮肤驱动取色）。
        // C.3：5 档语义由 1..5 改为 -2 -1 0 1 2 —— idx(0..4) 偏移 -2；渲染色 红2→中性→绿2（见 SiteForm/StatusGlyph）。
        // 四·七 B：不再注入「保存并同步」按钮 —— 各控件回调即发包（setPriority / autoToggle / 步进）。
        SiteForm form = SiteForm.of()
                .priorityBars(PRIORITY_COUNT, idx -> setPriority(idx - 2))
                .workers(List.of(buildWorkerRow()))
                .storageSummary(Component.literal(String.valueOf(assignedStorage)));

        // 自动派遣总闸（canSetAssign 才给交互；否则不挂）
        if (site.canSetAssign()) {
            ToggleWidget autoToggle = new ToggleWidget()
                    .checked(autoAssign)
                    .label(Component.translatable("hold.coe.auto_assign"));
            autoToggle.onChange(on -> { autoAssign = on; sendUpdate(); synced(); });
            form.autoDispatch(autoToggle);

            LabeledSlider stepper = new LabeledSlider()
                    .label(Component.translatable("hold.coe.max_workers"))
                    .range(0, maxWorkersCap).step(1).value(maxWorkers)
                    .onChange(v -> setMaxWorkers((int) v));
            form.workerStepper(stepper);
        }
        return form;
    }

    private WorkerRow buildWorkerRow() {
        return WorkerRow.of()
                .name(Component.translatable("hold.coe.assigned_workers", assignedWorkers))
                .status(Component.literal(maxWorkers + "/" + maxWorkersCap));
    }

    // C.3：原 buildStatusCard()（HoldStatusCard：据点名/等级/能量 + 禁用态改名框/升级按钮）已整体移除——
    // 这些全是【据点级】数据，不属于站点右栏。HoldStatusCard 复合组件类本身保留不动（仍可复用），
    // 仅本站点屏不再实例化。据点能量见 TerminalScreen 顶部 EnergyBar；据点名/等级/改名/升级归据点总览。

    // ------------------------------------------------------------------
    // 行为（发包语义不变）
    // ------------------------------------------------------------------

    private void setPriority(int value) {
        if (priority == value) return;
        priority = value;
        sendUpdate();
        synced();
    }

    private void setMaxWorkers(int next) {
        next = Math.max(0, Math.min(maxWorkersCap, next));
        if (next == maxWorkers) return;
        maxWorkers = next;
        sendUpdate();
    }

    private void sendUpdate() {
        ClientPacketDistributor.sendToServer(new SiteSetting(
                terminalPos.pos(), sitePos, priority, autoAssign, maxWorkers));
    }

    /**
     * 即时同步成功的轻反馈（四·七 B：Toast 200ms ≈ 4 tick）。
     * 仅离散控件（优先级档位格 / 自动派遣总闸）调用；工人步进滑杆拖拽会连发 onChange，
     * 走行内状态（滑杆thumb/工人行文本）反馈，不进这里以免 Toast 刷屏。
     */
    private void synced() {
        ui.toasts().push(Component.translatable("hold.coe.site.saved"), ToastHost.ToastStyle.SUCCESS, 4);
    }
}
