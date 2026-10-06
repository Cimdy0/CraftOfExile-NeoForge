package com.cimdy.coe.ui.screen.stronghold;

import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.data.codec.stronghold.storage.TeleportTarget;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.GlobalPos;
import com.cimdy.coe.network.stronghold.TeleportRequestPacket;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.attach.Decor;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.component.list.Sortable;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.CostLine;
import com.cimdy.coe.ui.widget.compose.EnergyBar;
import com.cimdy.coe.ui.widget.compose.business.TeleportPanel;
import com.cimdy.coe.ui.widget.compose.filter.Filter;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 终端传送界面（M2 重构）：用 M1 {@link TeleportPanel} 装配。
 *
 * <p>布局 250×230：顶部 {@link EnergyBar}（末影能量余额），中间 {@link SortableList} 三列
 * （花费 / 名称 / 距离），行 = 名称 + 「本维」角标 {@link Decor#badge} + {@link CostLine}，
 * 底部维度统计脚注。数据仍读 {@link StrongholdHelper#getTeleportTargets}，点击行经
 * {@link TeleportPanel#onSelect} 上抛，发包（{@link TeleportRequestPacket}）语义不变。
 */
public class TeleportScreen extends WidgetScreen {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 230;
    /** 重新拉取目标列表的间隔（tick）。其余帧只刷新能量 / 花费着色。 */
    private static final int RELOAD_INTERVAL = 20;

    private final Level level;
    private final BlockPos terminalPos;

    private List<TeleportTarget> targets;
    private TeleportPanel<TeleportTarget> panel;
    private EnergyBar energyBar;
    private int reloadCountdown = RELOAD_INTERVAL;
    /** D.5 teleport 空间涟漪（2 圈）；传送确认时触发。 */
    private Vfx teleportVfx;

    public TeleportScreen(TerminalBlockEntity terminal, Player player) {
        super(Component.translatable("hold.coe.terminal_teleport"));
        this.terminalPos = terminal.getBlockPos();
        this.level = player.level();
        this.targets = StrongholdHelper.getTeleportTargets(level, terminalPos);
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    @Override
    protected void buildUi(WindowLayer root) {
        Window window = CoeUi.panel(getTitle(), WINDOW_WIDTH, WINDOW_HEIGHT)
                .onClose(this::onClose)
                .center(this.width, this.height);

        // 默认附件（STANDARD，克制）：标题带 + 角点 + 标准音效族 + teleport 涟漪特效
        AttachmentPresets preset = AttachmentPresets.defaults();
        teleportVfx = AttachmentPresets.vfxFor(preset, AttachmentPresets.Target.TELEPORT);
        window.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.TELEPORT),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.TELEPORT),
                teleportVfx);

        energyBar = buildEnergyBar();

        panel = TeleportPanel.<TeleportTarget>of()
                .energy(energyBar)
                .targets(targets)
                .footer(dimSummary())
                .onSelect(this::requestTeleport);

        panel.list()
                .sortKeys(List.of(
                        new Sortable.SortKey<>("cost",
                                Component.translatable("hold.coe.teleport.col_cost"),
                                Comparator.comparingDouble(this::costOf)),
                        new Sortable.SortKey<>("name",
                                Component.translatable("hold.coe.teleport.col_name"),
                                Comparator.comparing(TeleportTarget::name)),
                        new Sortable.SortKey<>("dist",
                                Component.translatable("hold.coe.teleport.col_dist"),
                                Comparator.comparingLong(this::distOf))))
                .search((q, t) -> t.label().getString().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
                // 四·七 B：维度选择（本维/异维）即时生效——chip 切换 → DimFilter.onChange → list().refresh() 重跑管线。
                .filter(new DimFilter(() -> panel.list().refresh()))
                .row(this::buildTargetRow)
                .onSelect(this::requestTeleport)
                .emptyHint(Component.translatable("ui.coe.empty"));

        window.add(panel);
        root.add(window);
    }

    private EnergyBar buildEnergyBar() {
        double balance = StrongholdHelper.getHoldEnergy(level, terminalPos);
        double maxCost = targets.stream().mapToDouble(this::costOf).max().orElse(0);
        return EnergyBar.of()
                .label(Component.translatable("hold.coe.energy"))
                .range(0, Math.max(1.0, balance + maxCost))
                .value(balance);
    }

    /** 一行目标：名称（grow）+ 本维角标 + 花费。 */
    private Node buildTargetRow(TeleportTarget t) {
        Node row = new Node().row().id("teleport-row");
        row.flex.height = 14;
        row.flex.alignItems = AlignItems.CENTER;
        row.flex.gapMain = 3;
        row.padding(0, 4);

        Node name = new Node().with(Renderers.text(t.label())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE));
        name.flex.grow = 1;
        row.add(name);

        // 维度角标（右下角小胶囊）：本维 accent / 跨维 warn
        boolean sameDim = t.pos().dimension().equals(level.dimension());
        row.attach(Decor.of().badge(
                Component.translatable(sameDim ? "hold.coe.teleport.same_dim" : "hold.coe.teleport.cross_dim"),
                sameDim ? Theme.current().accent() : Theme.current().toastWarn(),
                Decor.BadgeCorner.BOTTOM_RIGHT));

        CostLine cost = CostLine.of()
                .cost(costOf(t))
                .affordable(affordable(t));
        row.add(cost);

        // 能量不足：整行置灰（视觉），点击仍由 requestTeleport 兜底拦截
        row.disabled(!affordable(t));

        row.tooltip(() -> describe(t));
        return row;
    }

    private Component dimSummary() {
        long same = targets.stream().filter(t -> t.pos().dimension().equals(level.dimension())).count();
        long other = targets.size() - same;
        return Component.translatable("hold.coe.teleport.dim_summary", (int) same, (int) other);
    }

    private List<Component> describe(TeleportTarget target) {
        double cost = costOf(target);
        boolean ok = StrongholdHelper.hasEnderEnergy(level, terminalPos, cost);
        List<Component> lines = new java.util.ArrayList<>();
        lines.add(target.label());
        lines.add(Component.translatable("hold.coe.energy")
                .append(Component.literal(": " + formatCost(cost)))
                .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (!ok) {
            lines.add(Component.translatable("ui.coe.energy.insufficient").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // 行为（发包语义不变）
    // ------------------------------------------------------------------

    private double costOf(TeleportTarget target) {
        return StrongholdHelper.getEnergyCost(new GlobalPos(level.dimension(), terminalPos), target.pos());
    }

    private boolean affordable(TeleportTarget target) {
        return StrongholdHelper.hasEnderEnergy(level, terminalPos, costOf(target));
    }

    /** 同维曼哈顿距离；异维排到最后。 */
    private long distOf(TeleportTarget t) {
        if (!t.pos().dimension().equals(level.dimension())) return Long.MAX_VALUE;
        BlockPos a = terminalPos;
        BlockPos b = t.pos().pos();
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private void requestTeleport(TeleportTarget target) {
        // 兜底：状态可能在按下那一刻刚变
        if (!affordable(target)) return;
        // D.5 teleport：空间涟漪（2 圈）在传送执行瞬间触发；随后即关屏（行为不变）。
        // 坐标相对窗根取窗口中心，取色 accent。渲染仍受 Vfx.MASTER_ENABLED 全局默认关约束。
        if (teleportVfx != null) teleportVfx.teleportRipple(WINDOW_WIDTH / 2F, WINDOW_HEIGHT / 2F, Theme.current().accent());
        ClientPacketDistributor.sendToServer(new TeleportRequestPacket(terminalPos, target.pos()));
        onClose();
    }

    private static String formatCost(double cost) {
        return cost == Math.floor(cost) ? String.valueOf((long) cost) : String.format(Locale.ROOT, "%.1f", cost);
    }

    // ------------------------------------------------------------------
    // 刷新
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (panel == null) return;

        energyBar.value(StrongholdHelper.getHoldEnergy(level, terminalPos));

        if (--reloadCountdown <= 0) {
            reloadCountdown = RELOAD_INTERVAL;
            targets = StrongholdHelper.getTeleportTargets(level, terminalPos);
            panel.targets(targets);   // 条数可能变了，走重建（内部复用行节点）
            panel.footer(dimSummary());
        } else {
            panel.list().engine().list().refresh();   // 只重绑，刷新花费着色
        }
    }

    /**
     * 维度筛选（四·七 B「维度选择即时生效」）：本维 / 异维 多选 chip，OR 组合，空选=全部。
     * 纯客户端列表过滤，不发包；chip 切换经 {@code onChange} → {@code SortableList.refresh()} 重跑管线。
     */
    private final class DimFilter implements Filter<TeleportTarget> {
        private final Set<String> selected = new HashSet<>();
        private final Runnable onChange;

        DimFilter(Runnable onChange) { this.onChange = onChange; }

        @Override public String id() { return "dimension"; }
        @Override public Component label() { return Component.translatable("hold.coe.teleport.filter_dim"); }
        @Override public List<Option> options() {
            return List.of(
                    new Option("same", Component.translatable("hold.coe.teleport.same_dim")),
                    new Option("other", Component.translatable("hold.coe.teleport.filter_other")));
        }
        @Override public Set<String> selected() { return selected; }
        @Override public void toggle(String optionId) {
            if (!selected.add(optionId)) selected.remove(optionId);
            onChange.run();
        }
        @Override public void clear() { selected.clear(); onChange.run(); }
        @Override public boolean test(TeleportTarget t) {
            if (selected.isEmpty()) return true;
            boolean sameDim = t.pos().dimension().equals(level.dimension());
            if (selected.contains("same") && sameDim) return true;
            if (selected.contains("other") && !sameDim) return true;
            return false;
        }
    }
}
