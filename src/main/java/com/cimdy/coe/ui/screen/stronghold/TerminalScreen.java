package com.cimdy.coe.ui.screen.stronghold;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.client.render.HighlightRenderer;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.stronghold.entity.SaveEntity;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.network.entity.RecycleEntityPacket;
import com.cimdy.coe.network.entity.SpawnEntityPacket;
import com.cimdy.coe.network.entity.ToggleAllyPacket;
import com.cimdy.coe.network.entity.ToggleCombatantPacket;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.base.MouseButton;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.EnergyBar;
import com.cimdy.coe.ui.widget.compose.composite.DetailPanel;
import com.cimdy.coe.ui.widget.compose.picker.Pager;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 终端总览界面：五窗格仪表盘。
 *
 * <ul>
 *   <li><b>主区</b>（保存的实体）：网格 + 分页（{@link Pager}），点击生成到终端旁；</li>
 *   <li><b>工作区</b>（据点内实体）：点击回收进存档；</li>
 *   <li><b>库存</b>：点击高亮该物品在据点内的所有存放位置；</li>
 *   <li><b>信息</b>：主/工作区悬停的实体详情（主从联动）；</li>
 *   <li><b>任务</b>：据点待派发任务队列（类型 · 状态），随刷新实时更新。</li>
 * </ul>
 *
 * <p>分页原本是手写的一整套 {@code page / PAGE_SIZE / pageSlice / changePage / updatePageText /
 * prev-next 按钮栏}，现在收进 {@link Pager}；单元格每行重绑前由
 * {@link ListView#clearBeforeBind()} 自动复位，binder 里不再手写 {@code clearVisuals(); styled();}。
 */
public class TerminalScreen extends WidgetScreen {

    private static final int CELL = 12;
    private static final int CELL_GAP = 3;
    /** 每页格子数（主区 7 列 × 6 行）。 */
    private static final int PAGE_SIZE = 42;

    // 五窗格的相对组坐标（沿用旧 WindowGroup 的布局语义，包围盒整体居中）
    private static final int MAIN_X = 85,  MAIN_Y = 0;
    private static final int HOLD_X = 85,  HOLD_Y = 110;
    private static final int SELF_X = 0,   SELF_Y = 0;
    private static final int TASK_X = 0,   TASK_Y = 105;
    private static final int INFO_X = 202, INFO_Y = 0;
    private static final int GROUP_W = 314, GROUP_H = 180;

    private final TerminalBlockEntity terminal;
    private final Player player;
    private final Level level;
    private final BlockPos pos;

    private List<SaveEntity> mainEntitiesData = new ArrayList<>();
    private List<UUID> holdEntitiesData = new ArrayList<>();
    private List<ItemCount> holdItemsData = new ArrayList<>();

    private Pager<SaveEntity> mainList;
    /** 主区搜索：按实体类型 id 的 path 模糊过滤（无名称索引，未做项 #1 按类型落地）。 */
    private String mainQuery = "";
    private TextInput mainSearch;
    private ListView<UUID> holdList;
    private ListView<ItemCount> itemList;
    private DetailPanel infoPanel;
    /** 任务窗格：渲染据点待派发任务队列（pending）。 */
    private DetailPanel taskPanel;

    @Override
    public boolean keyPressed(KeyEvent event) {
        // 修复批 5：Ctrl+F 聚焦主区搜索框（附 A.4 自定快捷键；优先级高于背包浮窗聚焦）。
        if (mainSearch != null && event.key() == GLFW.GLFW_KEY_F
                && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            mainSearch.focus();
            return true;
        }
        return super.keyPressed(event);
    }

    /** 窗格计数标题（M2：主区/工作/库存 标题带 x/y 计数）。 */
    private Window mainWindow, holdWindow, selfWindow;
    /** 顶部能量条（M1 EnergyBar）。 */
    private EnergyBar energyBar;

    public TerminalScreen(TerminalBlockEntity terminal, Player player) {
        super(Component.empty());
        this.terminal = terminal;
        this.player = player;
        this.level = player.level();
        this.pos = terminal.getBlockPos();
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    @Override
    protected void buildUi(WindowLayer root) {
        int wx = Math.max(0, (this.width - GROUP_W) / 2);
        int wy = Math.max(0, (this.height - GROUP_H) / 2);

        // 顶部能量条（M1 EnergyBar；STANDARD 附件克制装配）
        energyBar = EnergyBar.of()
                .label(Component.translatable("hold.coe.energy"))
                .range(0, Math.max(1.0, StrongholdHelper.getHoldEnergy(level, pos)))
                .value(StrongholdHelper.getHoldEnergy(level, pos));
        Window energyWindow = CoeUi.panel(Component.empty(), GROUP_W, 16)
                .position(wx, wy - 20);
        AttachmentPresets preset = AttachmentPresets.defaults();
        energyWindow.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.TELEPORT),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.TELEPORT));
        energyWindow.add(energyBar);
        root.add(energyWindow);

        mainWindow = createMain(wx + MAIN_X, wy + MAIN_Y);
        holdWindow = createHold(wx + HOLD_X, wy + HOLD_Y);
        selfWindow = createSelf(wx + SELF_X, wy + SELF_Y);
        root.add(mainWindow);
        root.add(holdWindow);
        root.add(selfWindow);
        root.add(createTask(wx + TASK_X, wy + TASK_Y));
        root.add(createInfo(wx + INFO_X, wy + INFO_Y));

        refreshData();
    }

    /** 主区：保存的实体，可翻页。分页工具条与切片由 {@link Pager} 内部维护。 */
    private Window createMain(int x, int y) {
        Window w = CoeUi.panel(Component.translatable("hold.terminal.coe.player_entity"), 112, 105)
                .position(x, y);

        mainList = new Pager<>(gridCell(), this::bindMainCell)
                .pageSize(PAGE_SIZE)
                .grid().itemGap(CELL_GAP).contentPadding(CoeUi.PAD)
                .emptyView(CoeUi.emptyHint())
                .clearBeforeBind()
                .onRowHover((item, idx) ->
                        showInfo(item == null ? List.of() : item.getEntityDisplayData(level)));
        mainList.flex.grow = 1;

        // 主区搜索：按实体类型 id 过滤（真实数据可读；名称索引不存在，见计划未做项 #1）
        mainSearch = new TextInput().styled()
                .placeholder(Component.translatable("hold.terminal.coe.search_type"));
        mainSearch.flex.height = 12;
        mainSearch.onChange(q -> {
            mainQuery = q == null ? "" : q.trim();
            refreshMainFiltered();
        });

        w.add(mainSearch);
        w.add(mainList);
        return w;
    }

    /** 工作区：据点内实体，点击回收。 */
    private Window createHold(int x, int y) {
        Window w = CoeUi.panel(Component.translatable("hold.terminal.coe.work_entity"), 112, 70)
                .position(x, y);

        holdList = new ListView<>(gridCell(), this::bindHoldCell).grid();
        holdList.clearBeforeBind().itemGap(CELL_GAP).contentPadding(CoeUi.PAD);
        holdList.flex.grow = 1;
        holdList.emptyView(CoeUi.emptyHint());
        holdList.onRowHover((item, idx) -> {
            if (item == null) {
                showInfo(List.of());
                return;
            }
            Entity e = level.getEntity(item);
            showInfo(SaveEntity.getEntityInfo(e));
        });

        w.add(holdList);
        return w;
    }

    /** 库存区：物品图标网格，点击高亮存放位置。 */
    private Window createSelf(int x, int y) {
        Window w = CoeUi.panel(Component.translatable("hold.terminal.coe.stock"), 80, 100)
                .position(x, y);

        itemList = new ListView<>(gridCell(), this::bindItemCell).grid();
        itemList.clearBeforeBind().itemGap(CELL_GAP).contentPadding(CoeUi.PAD);
        itemList.flex.grow = 1;
        itemList.emptyView(CoeUi.emptyHint());

        w.add(itemList);
        return w;
    }

    /** 任务区：据点待派发任务队列（pending），逐行显示「类型 · 状态」。 */
    private Window createTask(int x, int y) {
        Window w = CoeUi.panel(Component.translatable("hold.terminal.coe.task"), 80, 75)
                .position(x, y);
        taskPanel = new DetailPanel();
        taskPanel.flex.grow = 1;
        w.add(taskPanel);
        return w;
    }

    /** 信息区：只读详情面板，由主/工作区的悬浮联动填充（M2：长信息可滚动）。 */
    private Window createInfo(int x, int y) {
        Window w = CoeUi.panel(Component.translatable("ui.coe.info"), 112, 180)
                .position(x, y);

        w.scrollBody();
        infoPanel = new DetailPanel();
        infoPanel.flex.grow = 1;
        w.add(infoPanel);
        return w;
    }

    // ------------------------------------------------------------------
    // 行绑定（clearBeforeBind 已在重绑前复位行，这里直接叠外观 / 加子节点）
    // ------------------------------------------------------------------

    private void bindMainCell(Node view, SaveEntity item, int index) {
        if (!(view instanceof Button cell)) return;
        cell.styled();

        SaveEntity se = item;
        LivingEntity[] preview = new LivingEntity[1];
        cell.with(Renderers.entityPreview(() -> {
            if (preview[0] == null) preview[0] = se.getEntityFromData(level);
            return preview[0];
        }).scale(10).followMouse(false));
        if (se.isAlly()) allyMark(cell);
        combatantMark(cell, se.isCombatant());
        cell.with(Renderers.text(Component.literal("Lv" + (int) se.getValue("Level", 1)))
                .align(Align.RIGHT)
                .vAlign(VAlign.BOTTOM)
                .fixedScale(0.5F));
        // 左键放出到据点 / 右键切换盟友（盟友放出后仍在据点里，但不参与派工）/ 中键切换战斗开关
        cell.only(MouseButton.LEFT).accept(MouseButton.RIGHT).accept(MouseButton.MIDDLE);
        cell.onClick((src, btn) -> {
            if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                se.uuid().ifPresent(id -> {
                    ClientPacketDistributor.sendToServer(new ToggleCombatantPacket(id, pos));
                    delayedRefresh();
                });
            } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                se.uuid().ifPresent(id -> {
                    ClientPacketDistributor.sendToServer(new ToggleAllyPacket(id, pos));
                    delayedRefresh();
                });
            } else {
                // C.1：移除 CoeUi.confirm 包装 —— 左键放出到据点直接发包（原确认回调内的发包逻辑原样保留，
                // 仅去掉二次确认弹窗）。DialogScreen/CoeUi.confirm 组件保留，仍服务其它场景。
                ClientPacketDistributor.sendToServer(new SpawnEntityPacket(se.data(), pos));
                delayedRefresh();
            }
        });
    }

    private void bindHoldCell(Node view, UUID item, int index) {
        if (!(view instanceof Button cell)) return;
        cell.styled();

        LivingEntity living = level.getEntity(item) instanceof LivingEntity l ? l : null;
        cell.with(Renderers.entityPreview(() -> living).scale(10).followMouse(false));
        // 原 AttachmentRegistry.ALLY / COMBATANT 已迁入 AdditionData（key=ally / combatant，combatant 默认 true）
        if (living != null && AdditionData.getOrDefault(living, AdditionKeys.ALLY, 0.0) != 0.0) allyMark(cell);
        if (living != null)
            combatantMark(cell, AdditionData.getOrDefault(living, AdditionKeys.COMBATANT, 1.0) != 0.0);
        // 左键回收进存档 / 右键切换盟友（标记为盟友后立刻停止派工，进行中的任务自动回退）/ 中键切换战斗开关
        cell.only(MouseButton.LEFT).accept(MouseButton.RIGHT).accept(MouseButton.MIDDLE);
        cell.onClick((src, btn) -> {
            if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                ClientPacketDistributor.sendToServer(new ToggleCombatantPacket(item, pos));
                delayedRefresh();
            } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                ClientPacketDistributor.sendToServer(new ToggleAllyPacket(item, pos));
                delayedRefresh();
            } else {
                // C.1：移除 CoeUi.confirm 包装 —— 左键回收进存档直接发包（原确认回调内的发包逻辑原样保留）。
                ClientPacketDistributor.sendToServer(new RecycleEntityPacket(item, pos));
                delayedRefresh();
            }
        });
    }

    /** 盟友角标：金色描边 + 左上角「友」字，风格与既有格子一致（不额外加背景/边框标题）。 */
    private static void allyMark(Button cell) {
        cell.with(Renderers.borderOnly(Theme.current().toastWarn()));
        cell.with(Renderers.text(SaveEntity.allyMarkComponent())
                .color(Theme.current().toastWarn())
                .align(Align.LEFT)
                .vAlign(VAlign.TOP)
                .fixedScale(0.5F));
    }

    /** 战斗开关角标：右上角小字，战斗态金色、专注工作态灰色。 */
    private static void combatantMark(Button cell, boolean combatant) {
        cell.with(Renderers.text(Component.translatable("hold.terminal.coe.combatant")
                        .append(combatant
                                ? Component.translatable("hold.terminal.coe.combatant_on")
                                : Component.translatable("hold.terminal.coe.combatant_off")))
                .color(combatant ? Theme.current().toastWarn() : Theme.current().statusOffDim())
                .align(Align.RIGHT)
                .vAlign(VAlign.TOP)
                .fixedScale(0.5F));
    }

    private void bindItemCell(Node view, ItemCount item, int index) {
        if (!(view instanceof Button cell)) return;
        cell.styled();

        ItemCount ic = item;
        cell.with(Renderers.item(() -> ic.resource().toStack(ic.amount())).showTooltip(true));
        cell.with(Renderers.text(Component.literal(String.valueOf(ic.amount())))
                .align(Align.RIGHT)
                .vAlign(VAlign.BOTTOM)
                .fixedScale(0.5F));
        cell.onClick(() -> {
            Set<BlockPos> posList = terminal.getData(AttachmentRegistry.DETAILED)
                    .getResourceInfo(ic.resource());
            HighlightRenderer.addHighlightedPositions(posList, Theme.current().statusOn(), 100);
            // 修复：高亮后不再强制关窗（此前点一下库存条目整个终端屏就关闭，体验中断）；
            // 玩家可继续浏览，Esc / 关闭按钮自行退出。
        });
    }

    private static Button gridCell() {
        Button b = new Button();
        b.flex.size(CELL, CELL);
        return b;
    }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    private void refreshData() {
        mainEntitiesData = StrongholdHelper.getEntityData(player);
        holdEntitiesData = StrongholdHelper.getSaveEntity(level, pos);
        holdItemsData = terminal.getData(AttachmentRegistry.DETAILED).toSummary().summary();
        refreshMainFiltered();
        holdList.items(holdEntitiesData);
        itemList.items(holdItemsData);

        // 窗格计数标题（M2：「主区 x / 工作 y / 库存 z」；主区计数由 refreshMainFiltered 统一更新）
        holdWindow.title(Component.translatable("hold.terminal.coe.work_entity")
                .append(Component.literal(" " + holdEntitiesData.size())));
        selfWindow.title(Component.translatable("hold.terminal.coe.stock")
                .append(Component.literal(" " + holdItemsData.size())));

        if (energyBar != null) energyBar.value(StrongholdHelper.getHoldEnergy(level, pos));

        refreshTaskPanel();
    }

    /** 主区按 {@link #mainQuery} 过滤后喂给分页网格；标题计数随之更新。 */
    private void refreshMainFiltered() {
        String query = mainQuery.toLowerCase(java.util.Locale.ROOT);
        List<SaveEntity> filtered = mainEntitiesData;
        if (!query.isEmpty()) {
            filtered = new ArrayList<>();
            for (SaveEntity se : mainEntitiesData) {
                String type = entityTypeId(se);
                if (type != null && type.contains(query)) filtered.add(se);
            }
        }
        mainList.items(filtered);
        mainWindow.title(Component.translatable("hold.terminal.coe.player_entity")
                .append(Component.literal(" " + filtered.size())));
    }

    /** 实体类型 id 的 path 段（NBT "id"，免创建实体）；异常安全。 */
    private static String entityTypeId(SaveEntity se) {
        try {
            String id = se.data().copyTag().getString("id").orElse("");
            if (id.isEmpty()) return null;
            int slash = id.indexOf(':');
            return slash >= 0 ? id.substring(slash + 1) : id;
        } catch (Exception e) {
            return null;
        }
    }

    /** 把据点任务队列渲染进任务窗格（待派发 + 进行中，类型 · 状态 · 能量成本）。 */
    private void refreshTaskPanel() {
        if (taskPanel == null) return;
        Stronghold hold = terminal.getData(AttachmentRegistry.STRONGHOLD);
        List<Component> lines = new ArrayList<>();
        double balance = StrongholdHelper.getHoldEnergy(level, pos);
        if (hold != null) {
            for (ActiveTask at : hold.holdState().pendingTasks()) {
                net.minecraft.network.chat.MutableComponent line = Component.literal(taskTypeLabel(at.task()) + "  ")
                        .append(at.status().getComponentName());
                double cost = taskEnergyCost(at.task());
                if (cost > 0) {
                    boolean afford = balance >= cost;
                    line = line.append(Component.literal("  [" + (int) cost + "\u26A1]")
                            .withStyle(afford ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));
                }
                lines.add(line);
            }
            // 进行中任务段（修复：activeTasks 此前完全不可见——按实体分组展平，▶ 前缀区分，零新增 lang key）
            var active = hold.holdState().activeTasks();
            if (active != null && !active.isEmpty()) {
                lines.add(Component.empty());
                for (var entry : active.entrySet()) {
                    for (ActiveTask at : entry.getValue()) {
                        lines.add(Component.literal("\u25B6 ")
                                .append(Component.literal(taskTypeLabel(at.task()) + "  "))
                                .append(at.status().getComponentName()));
                    }
                }
            }
        }
        // 空态（M2）：无待派发 / 进行中任务
        if (lines.isEmpty()) {
            lines.add(Component.translatable("ui.coe.terminal.no_task"));
        }
        taskPanel.show(lines);
    }

    /** 任务能量成本：搬运任务按 TransportService.teleportEnergyCost（= getEnergyCost×2，20/60）；
     *  附魔任务按 EnchantTask.getEnderEnergyCost()；其余任务无能量成本返回 0。 */
    private static double taskEnergyCost(Task t) {
        if (t instanceof com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask ct) {
            return com.cimdy.coe.stronghold.transport.TransportService.teleportEnergyCost(ct);
        }
        if (t instanceof com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantTask et) {
            return et.getEnderEnergyCost();
        }
        return 0;
    }

    /** 由任务类型注册表键还原可读标签（去掉 task_ 前缀）。 */
    private static String taskTypeLabel(Task t) {
        Identifier id = TaskTypes.REGISTRY.getKey(t.getType());
        if (id == null) return "?";
        String p = id.getPath();
        return p.startsWith("task_") ? p.substring(5) : p;
    }

    private void showInfo(List<Component> lines) {
        if (infoPanel == null) return;
        infoPanel.show(lines);
    }

    /** 点击后的延迟刷新：等服务器处理完再拉一遍数据。
     *  复用宿主 tick 驱动的一次性延迟任务（约 2 tick ≈ 100ms），不再每次点击新建线程池。 */
    private void delayedRefresh() {
        ui.after(2, this::refreshData);
    }
}
