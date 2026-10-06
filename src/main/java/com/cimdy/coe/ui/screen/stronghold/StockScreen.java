package com.cimdy.coe.ui.screen.stronghold;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.JustifyContent;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.client.ItemSearch;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.storage.stock.*;
import com.cimdy.coe.network.stronghold.StockSettingPacket;
import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.attach.AttachmentPresets;
import com.cimdy.coe.ui.widget.attach.Vfx;
import com.cimdy.coe.ui.widget.component.bar.*;
import com.cimdy.coe.ui.widget.component.base.*;
import com.cimdy.coe.ui.widget.component.button.*;
import com.cimdy.coe.ui.widget.component.list.*;
import com.cimdy.coe.ui.widget.component.panel.*;
import com.cimdy.coe.ui.widget.component.*;
import com.cimdy.coe.ui.widget.compose.badge.StatusGlyph;
import com.cimdy.coe.ui.widget.compose.business.StockConfigPanel;
import com.cimdy.coe.ui.widget.compose.filter.Filter;
import com.cimdy.coe.ui.widget.compose.filter.FilterBar;
import com.cimdy.coe.ui.widget.compose.picker.ItemPicker;
import com.cimdy.coe.ui.widget.compose.LabeledSlider;
import com.cimdy.coe.ui.widget.compose.semantic.ButtonBar;
import com.cimdy.coe.ui.widget.compose.semantic.Section;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 存量设定界面（M2 重构，220×150）：用 {@link StockConfigPanel} 装配。
 *
 * <p>面板自上而下：{@link FilterBar}（已设定 / 空 筛选；词缀/类别维度为已知数据层边界，UI 预留）
 * → 条目行列表（沿用原 {@link ListView} 行绑定：物品格 + 容量 ValueBar + 供应模式按钮组）
 * → 底部优先级滑杆 + 「批量加条目」按钮；下方保留最小搬运量滑杆。
 * {@link StockDisplay#from} 五元组语义、{@link StockSettingPacket} 发包均不变。
 */
public class StockScreen extends WidgetScreen {

    /** 存量上限滑杆的范围上限（堆叠基准）。 */
    private static final int MAX_CAP = 64;

    private final BlockPos pos;
    private final TerminalBlockEntity terminal;
    private final BlockEntity be;

    private List<StockEntry> entries;
    private float minTransfer;
    private int priority;

    private WindowLayer root;
    private ListView<StockDisplay> list;
    private List<StockDisplay> display = List.of();
    /** 已设定/空 筛选（词缀维度为待服务端数据边界，仅做已配置条目筛选）。 */
    private StockFilter stockFilter;

    /** 物品选择窗当前编辑的条目下标。 */
    private int pickerIndex = -1;
    /** D.5 stock_zero 存量清零警示闪烁；挂到窗根，存量归零时触发。 */
    private Vfx stockVfx;
    /** 已触发清零警示的条目资源（按 ItemResource 身份，防列表重排/筛选后索引错位：重复闪或漏闪；存量恢复后移除，供下次清零再闪）。 */
    private final java.util.Set<ItemResource> zeroWarned = new java.util.HashSet<>();

    public StockScreen(Player player, BlockEntity be, TerminalBlockEntity terminal) {
        super(Component.empty());
        this.be = be;
        this.pos = be.getBlockPos();
        this.terminal = terminal;
        StockSet stockSet = be.getData(AttachmentRegistry.STOCK_SET);
        this.minTransfer = stockSet.minTransfer();
        this.priority = stockSet.priority();
        this.entries = new ArrayList<>(stockSet.entries());
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    @Override
    protected void buildUi(WindowLayer root) {
        this.root = root;

        Window window = CoeUi.panel(Component.translatable("hold.coe.stock_set"), 220, 150)
                .onClose(this::onClose)
                .center(this.width, this.height);

        AttachmentPresets preset = AttachmentPresets.defaults();
        stockVfx = AttachmentPresets.vfxFor(preset, AttachmentPresets.Target.STOCK);
        window.attach(
                AttachmentPresets.decorFor(preset, AttachmentPresets.Target.STOCK),
                AttachmentPresets.sfxFor(preset, AttachmentPresets.Target.STOCK),
                stockVfx);

        list = new ListView<>(new Button(), this::bindRow);
        list.clearBeforeBind().itemGap(2).contentPadding(CoeUi.PAD);
        list.scrollBar(true);
        list.flex.grow = 1;
        list.emptyView(CoeUi.emptyHint());

        stockFilter = new StockFilter(this::refreshRows);
        FilterBar<StockDisplay> filterBar = FilterBar.<StockDisplay>of().filter(stockFilter);

        LabeledSlider prioritySlider = new LabeledSlider()
                .label(Component.translatable("hold.stock.coe.priority"))
                .range(1, 10).value(priority)
                .fill(Theme.current().stockSliderFill())
                .onChange(v -> { priority = (int) v; send(); });

        Button batchAdd = Button.of(Component.translatable("hold.coe.stock.add_entry")).styled();
        batchAdd.flex.height = CoeUi.BAR_HEIGHT;
        // 四·七 B：批量加条目属高代价操作，保留确认弹窗；确认后才新增空条目并即时 sync()+send()。
        batchAdd.onClick(() -> CoeUi.confirm(
                Component.translatable("hold.coe.stock.add_entry.confirm_title"),
                Component.translatable("hold.coe.stock.add_entry.confirm_msg"),
                () -> { entries.add(StockEntry.empty()); sync(); }));

        StockConfigPanel panel = StockConfigPanel.of()
                .filterBar(filterBar)
                .rows(List.of(list))
                .capacitySlider(prioritySlider)
                .batchAddButton(batchAdd);

        window.add(panel);
        window.add(minTransferConfig());
        root.add(window);

        refreshRows();
    }

    /** 最小搬运量滑杆（保留原塔级参数区，语义不变）。 */
    private Node minTransferConfig() {
        Section box = CoeUi.section(Component.empty());
        box.flex.height = 12;
        Node body = box.body();
        body.padding(0, CoeUi.PAD, 1, CoeUi.PAD);
        body.add(new LabeledSlider()
                .label(Component.translatable("hold.stock.coe.min_transfer"))
                .range(0, 100).value((int) (minTransfer * 100))
                .fill(Theme.current().stockSliderFill())
                .onChange(v -> { minTransfer = (float) (v / 100); send(); }));
        return box;
    }

    // ------------------------------------------------------------------
    // 行绑定（clearBeforeBind 已复位，这里直接 styled + 重建子节点）
    // ------------------------------------------------------------------

    private void bindRow(Node view, StockDisplay d, int index) {
        if (!(view instanceof Button row)) return;
        row.styled();

        boolean empty = d.entry().isEmpty();
        // 注意：不要对整行 row.disabled(empty)！禁用节点会短路事件分发，导致空行内的 "+" 按钮收不到点击。

        // ---- 物品图标（带槽框）：左键选物品 / 右键移除条目 ----
        Button itemBtn = new Button();
        itemBtn.flex.size(16, 16);
        itemBtn.only(MouseButton.LEFT).accept(MouseButton.RIGHT);
        itemBtn.onClick((src, btn) -> {
            if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !empty) {
                entries.remove(index);
                sync();
            } else if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                openItemPicker(index);
            }
        });
        itemBtn.with(Theme.slot());
        if (empty) {
            itemBtn.label(Component.literal("+")
                    .withStyle(s -> s.withColor(Theme.current().placeholderText())));
        } else {
            ItemStack icon = d.resource().toStack(1);
            itemBtn.with(Renderers.item(() -> icon).showTooltip(true));
        }

        // ---- 中间：容量条（可拖动，带把手）+ 三行数量 ----
        Node mid = new Node().column().gap(1);
        mid.flex.grow = 1;
        mid.padding(0, 2);

        Slider bar = Slider.of(1, MAX_CAP).interactive(!empty).showHandle(!empty);
        bar.value(d.max());
        bar.onChange(v -> {
            if (index < entries.size() && !empty) {
                entries.set(index, entries.get(index).setMax((int) v));
                sync();
            }
        });
        if (!empty) {
            double stored = d.stored(), transit = d.transit();
            double s0 = d.storedBand(), s1 = s0 + d.transitBand();
            bar.segment(Math.max(0, Math.min(1, s0 / MAX_CAP)),
                    Math.max(0, Math.min(1, s1 / MAX_CAP)), Theme.current().stockTransitBand());
            bar.segment(0, Math.max(0, Math.min(1, stored / MAX_CAP)), Theme.current().stockStoredBand());
        }
        bar.flex.height = 5;

        Node qty = new Node().column();
        qty.flex.grow = 1;
        // C.2（DSP 风三行小号，三色语义）：白=已存 stored / 蓝=在途未到（transit>0=incoming）/ 橙=在途未取（-transit=outgoing）。
        // 数据来源（客户端可枚举，只读）：StockDisplay.stored() 取实际库存；StockEntry.transit() 字段 = incoming - outgoing
        // （见 TeleportCoordinator#updateContainerStock），正负号即物流方向，故按符号拆成蓝/橙两行，不硬编码假数据。
        // 【待数据层】客户端目前只有净额（incoming-outgoing）；未到/未取的毛额拆分需服务端 TransportInfo
        // （StrongholdHelper.getTransportInfo 仅 ServerLevel 可用，客户端读不到）——数据层补毛额通道后这里改为 gross incoming/outgoing。
        int transit = d.transit();
        qty.add(qtyRow(Component.translatable("hold.stock.coe.stored"), d.stored(), Theme.current().stockQtyValue()));
        qty.add(qtyRow(Component.translatable("hold.stock.coe.transit"), Math.max(0, transit), Theme.current().accent()));
        qty.add(qtyRow(Component.translatable("hold.stock.coe.pending"), Math.max(0, -transit), Theme.current().toastWarn()));

        mid.add(bar);
        mid.add(qty);

        // ---- 右侧：两个供应模式按钮（竖排 ButtonBar）----
        ButtonBar right = new ButtonBar().vertical().gap(2).buttonHeight(10);
        right.flex.width = 42;
        right.flex.alignItems = AlignItems.STRETCH;
        right.add(modeButton(d, true, empty, index));
        right.add(modeButton(d, false, empty, index));

        row.add(itemBtn);
        row.add(mid);
        row.add(right);
    }

    private Node qtyRow(Component label, int value, int valueColor) {
        return CoeUi.keyValue(label, Component.literal(String.valueOf(value)))
                .textScale(0.7F)
                .autoHeight()
                .gap(0)
                .alignItems(AlignItems.STRETCH)
                .labelColor(Theme.current().stockQtyLabel())
                .valueColor(valueColor);
    }

    private Button modeButton(StockDisplay d, boolean forFirst, boolean empty, int index) {
        Button b = Button.of(Component.empty()).styled();
        b.flex.height = 10;
        b.disabled(empty);
        if (!empty) {
            // 四·七 A：供应模式渲染 StatusGlyph.arrow（▲供应/▼需求/◇仓储，自绘+皮肤取色），
            // 范围+模式文字降为 tooltip。
            Pair<StockRange, StockMode> slot = forFirst ? d.supply().first() : d.supply().second();
            StatusGlyph glyph = StatusGlyph.arrow(arrowDir(slot.getSecond()));
            b.row();
            b.flex.alignItems = AlignItems.CENTER;
            b.flex.justifyContent = JustifyContent.CENTER;
            b.add(glyph);
            b.tooltip(forFirst ? d.supply().getFirstComponentName() : d.supply().getSecondComponentName());
            b.onClick(() -> openModePicker(index, forFirst));
        }
        return b;
    }

    /**
     * 供应模式 → 方向符号语义（四·七 A：{@link StatusGlyph#arrow}）。
     * SUPPLY→SUPPLY 上 / DEMAND→DEMAND 下 / STORAGE→STORE 菱形；取色由 StatusGlyph 内部走皮肤令牌。
     */
    private static StatusGlyph.Direction arrowDir(StockMode mode) {
        return switch (mode) {
            case SUPPLY  -> StatusGlyph.Direction.SUPPLY;
            case DEMAND  -> StatusGlyph.Direction.DEMAND;
            case STORAGE -> StatusGlyph.Direction.STORE;
        };
    }

    // ------------------------------------------------------------------
    // 物品选择窗
    // ------------------------------------------------------------------

    private void openItemPicker(int index) {
        this.pickerIndex = index;
        List<ItemStack> candidates = allItemCandidates();

        ItemPicker picker = new ItemPicker(Component.translatable("hold.coe.stock_set"), 120, 80)
                .candidates(candidates)
                .match(ItemSearch::matchesInput)
                .onPick(stack -> {
                    if (pickerIndex >= 0 && pickerIndex < entries.size()) {
                        entries.set(pickerIndex, entries.get(pickerIndex).setItem(stack));
                        sync();
                    }
                    pickerIndex = -1;
                });
        picker.position((this.width - 120) / 2, (this.height - 80) / 2);
        picker.open(root);
    }

    private List<ItemStack> allItemCandidates() {
        Set<ItemStack> all = new HashSet<>();
        CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
                Minecraft.getInstance().getConnection().enabledFeatures(),
                false,
                Minecraft.getInstance().getConnection().registryAccess());
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            tab.buildContents(params);
            all.addAll(tab.getSearchTabDisplayItems());
        }
        return all.stream()
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .filter(stack -> entries.stream().noneMatch(e -> e.resource().matches(stack)))
                .collect(Collectors.toMap(
                        stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + stack.getComponentsPatch().toString(),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing((ItemStack s) -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString())
                        .thenComparing(s -> s.getComponentsPatch().toString()))
                .toList();
    }

    // ------------------------------------------------------------------
    // 供应模式选择窗
    // ------------------------------------------------------------------

    private void openModePicker(int index, boolean forFirst) {
        StockEntry entry = entries.get(index);
        List<Pair<StockRange, StockMode>> candidates = entry.supply().possibleReplacements(forFirst);
        if (candidates.isEmpty()) return;

        int winH = Math.min(100, 12 + candidates.size() * 12);
        OptionListPopup<Pair<StockRange, StockMode>> popup = OptionListPopup
                .of(candidates, pair -> pair.getFirst().getComponentName().copy()
                        .append(pair.getSecond().getComponentName()))
                .onPick(p -> {
                    entries.set(index, entries.get(index).setSupply(p, forFirst));
                    sync();
                    root.closeModal();
                })
                .closeOnPick(false)
                .popupStyle();
        popup.flex.size(56, winH);
        popup.position((this.width - 56) / 2, (this.height - winH) / 2);
        root.openModal(popup);
    }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    private List<StockDisplay> currentDisplay() {
        return entries.stream()
                .map(e -> StockDisplay.from(e, terminal.getData(AttachmentRegistry.DETAILED).getContainerSummary(pos)))
                .toList();
    }

    /** 重算显示并按筛选喂给列表（条目不重建）。 */
    private void refreshRows() {
        display = currentDisplay();
        list.items(display.stream().filter(stockFilter::test).toList());
    }

    /** 本地改动后的统一出口：刷新显示 + 发回服务器。 */
    private void sync() {
        refreshRows();
        send();
    }

    private void send() {
        ClientPacketDistributor.sendToServer(new StockSettingPacket(pos,
                new StockSet(entries, minTransfer, priority)));
    }

    @Override
    public void tick() {
        super.tick();
        List<StockDisplay> fresh = currentDisplay();
        if (!fresh.equals(display)) {
            display = fresh;
            list.items(display.stream().filter(stockFilter::test).toList());
        }
        // D.5 stock_zero：非空条目存量清零瞬间闪一次警示（仅 StatusGlyph 语义，挂窗根存量显示区）。
        // 存量恢复后从 warned 集合移除 → 下次再归零即重闪；渲染受 Vfx.MASTER_ENABLED 全局默认关约束。
        // 警示状态以 ItemResource 身份记录（不用列表下标）：条目增删/排序/筛选切换导致重排时不再张冠李戴。
        for (int i = 0; i < fresh.size(); i++) {
            StockDisplay d = fresh.get(i);
            if (!d.entry().isEmpty() && d.stored() == 0) {
                if (stockVfx != null && zeroWarned.add(d.resource())) {
                    stockVfx.stockZeroFlash(110F, 75F, Theme.current().toastWarn());
                }
            } else {
                zeroWarned.remove(d.resource());
            }
        }
    }

    /** 已设定 / 空 筛选（词缀维度为待服务端数据边界，仅做已配置条目筛选）。 */
    private final class StockFilter implements Filter<StockDisplay> {
        private final Set<String> selected = new HashSet<>();
        private final Runnable onChange;

        StockFilter(Runnable onChange) { this.onChange = onChange; }

        @Override public String id() { return "configured"; }
        @Override public Component label() { return Component.translatable("hold.coe.stock.filter_mode"); }
        @Override public List<Option> options() {
            return List.of(
                    new Option("set", Component.translatable("hold.coe.stock.configured")),
                    new Option("empty", Component.translatable("hold.stock.coe.unset")));
        }
        @Override public Set<String> selected() { return selected; }
        @Override public void toggle(String optionId) {
            if (!selected.add(optionId)) selected.remove(optionId);
            onChange.run();
        }
        @Override public void clear() { selected.clear(); onChange.run(); }
        @Override public boolean test(StockDisplay d) {
            if (selected.isEmpty()) return true;
            if (selected.contains("set") && !d.entry().isEmpty()) return true;
            if (selected.contains("empty") && d.entry().isEmpty()) return true;
            return false;
        }
    }
}
