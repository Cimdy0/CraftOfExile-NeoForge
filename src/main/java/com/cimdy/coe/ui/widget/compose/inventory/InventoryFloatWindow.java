package com.cimdy.coe.ui.widget.compose.inventory;

import com.cimdy.coe.ui.widget.component.TextInput;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 背包浮窗：挂在当前 Screen 的 {@link WindowLayer} 上的非模态浮层（计划 §3.5b 修正版）。
 *
 * <p><b>为什么是浮窗而非独立 Screen</b>：MC 同一时间只能打开一个 {@code Screen}
 * （{@code Minecraft.setScreen} 单栈），独立背包 Screen 一打开就会顶掉当前屏（存量 / 切割等）。
 * 故背包做成 WindowLayer 上的浮窗——B 键弹出 / 关闭，<b>不替换当前 Screen</b>，
 * 关闭浮窗即回到原屏。{@code ExtraSlotsScreen}（原版 Menu 容器屏）作为独立容器操作屏保留，两条路并存。
 *
 * <p><b>内容 = 玩家背包 36 槽</b>，直接复用 {@link MiniInventory} 的槽位渲染
 * （{@code ItemStack} 图标 + 原生 tooltip + {@link com.cimdy.coe.ui.widget.compose.item.ObfuscatedTooltip}
 * 混淆能力 + hover 提亮）。<b>只读浏览</b>：每帧从 {@link #readSlots()} 拉取，不修改任何栈。
 *
 * <p><b>槽位序号映射</b>（{@link MiniInventory} 行优先约定 ↔ MC {@link Inventory} 索引）：
 * <ul>
 *   <li>MiniInventory 0..26（上 3 行主背包）→ {@code inv.getItem(9..35)}；</li>
 *   <li>MiniInventory 27..35（底行热键栏）→ {@code inv.getItem(0..8)}。</li>
 * </ul>
 * {@link #onSlotClick(Consumer)} 上抛的是<b>真实 MC 槽位号</b>（0..8 热键栏 / 9..35 主背包），
 * 便于未来 {@code TakePutPacket} 直接消费。
 *
 * <p><b>取 / 放是骨架</b>：点击只上抛槽位号，真实物品移动需服务端容器交互包
 * （{@code TakePutPacket}，计划六章待办），包就绪前不发包、不挪物品。
 *
 * <p><b>关闭</b>：标题栏 X（{@link Window#closable(boolean)}）/ 再按 B / Esc（由 Screen 侧在打开期间绑定，见 WidgetScreen 接线）。
 *
 * <p><b>rebuildUi 存活</b>：Screen 重建界面会 {@code layer.clearChildren()} 清掉本浮窗节点；
 * 若浮窗当时处于打开态，Screen 重建后应调 {@link #remountIfOpen(WindowLayer)} 把节点重新挂回。
 */
public class InventoryFloatWindow {

    /** 内容区统一留白（与 CoeUi.PAD 对齐，widget 层不反向依赖 screen 装配层）。 */
    private static final int PAD = 3;
    /** 搜索框高度。 */
    private static final int SEARCH_H = 16;
    /** 匹配计数 / 空态提示行高度。 */
    private static final int INFO_H = 10;
    /** 窗口底边距屏幕下沿的距离（避开原版热键栏区域）。 */
    private static final int BOTTOM_MARGIN = 20;

    /** 浮窗宽 = 9 槽 × 槽尺寸 + 两侧留白。 */
    private static final int WIN_W = MiniInventory.COLS * Theme.SLOT_SIZE + 2 * PAD;
    /** 浮窗高 = 标题栏 + 搜索框 + 信息行 + 4 行槽 + 各段留白。 */
    private static final int WIN_H = Window.TITLE_BAR_HEIGHT
            + SEARCH_H + INFO_H
            + (MiniInventory.MAIN_ROWS + MiniInventory.HOTBAR_ROWS) * Theme.SLOT_SIZE + 4 * PAD;

    private final Window window;
    private final MiniInventory inventory = MiniInventory.of();
    private final TextInput search = new TextInput();
    /** 匹配计数 / 空态提示行（修复批 5：搜索时显示「匹配 n/36」或「背包内无匹配物品」）。 */
    private final Node info = new Node() {
        @Override
        protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
            if (lastInfo == null) return;
            Draw.text(g, Component.literal(lastInfo), absX, absY, Theme.current().textDisabled());
        }
    };
    private String lastInfo;

    /** 只读缓冲：避免每格每帧 {@code new ArrayList}（{@link MiniInventory} 每帧对每格调一次 supplier）。 */
    private final List<ItemStack> buffer = new ArrayList<>(MiniInventory.SLOT_COUNT);

    private boolean open;
    private Consumer<Integer> onSlotClick;
    private Predicate<ItemStack> obfuscate;
    private Runnable onShown;
    private Runnable onHidden;

    /** 客户端手持栈镜像（服务端权威，随 {@code TakePutAckPacket} 更新）；背包浮窗打开时在鼠标处渲染。 */
    private static ItemStack carried = ItemStack.EMPTY;

    public static void setCarried(ItemStack stack) {
        carried = stack == null ? ItemStack.EMPTY : stack;
    }

    public static ItemStack carried() {
        return carried;
    }

    public InventoryFloatWindow() {
        this.window = Window.of(Component.translatable("ui.coe.inventory.title"))
                .styled()
                .closable(true)
                .glow();
        this.window.flex.size(WIN_W, WIN_H);

        for (int i = 0; i < MiniInventory.SLOT_COUNT; i++) buffer.add(ItemStack.EMPTY);

        this.window.body().padding(PAD, PAD);
        this.inventory.bind(this::readSlots)
                .onSlotClick(this::onMiniSlot)
                .obfuscateIf(s -> obfuscate != null && obfuscate.test(s));
        this.search.styled().placeholder("搜索背包…")
                .onChange(this::applyFilter);
        this.search.flex.size(WIN_W - 2 * PAD, SEARCH_H);
        this.info.flex.size(WIN_W - 2 * PAD, INFO_H);
        this.window.body().add(search);
        this.window.body().add(info);
        this.window.body().add(inventory);

        // X 按钮 / 程序化 close() 都会走这里：同步 open 标志并通知 Screen 侧解绑 Esc。
        this.window.onClose(this::handleClosed);
    }

    public static InventoryFloatWindow of() {
        return new InventoryFloatWindow();
    }

    // ------------------------------------------------------------------
    // 配置（链式）
    // ------------------------------------------------------------------

    /** 点击某槽时回调其<b>真实 MC 槽位号</b>（0..8 热键栏 / 9..35 主背包）；真实取 / 放待 {@code TakePutPacket}。 */
    public InventoryFloatWindow onSlotClick(Consumer<Integer> slotClick) {
        this.onSlotClick = slotClick;
        return this;
    }

    /** 加法式：命中谓词的槽，其 hover tooltip 走未鉴定混淆样式（默认关闭）。传 {@code null} 复位关闭。 */
    public InventoryFloatWindow obfuscateIf(Predicate<ItemStack> p) {
        this.obfuscate = p;
        return this;
    }

    /** 浮窗打开时回调（Screen 侧在此绑定 Esc 关闭键）。 */
    public InventoryFloatWindow onShown(Runnable r) { this.onShown = r; return this; }

    /** 浮窗关闭时回调（Screen 侧在此解绑 Esc 关闭键）。 */
    public InventoryFloatWindow onHidden(Runnable r) { this.onHidden = r; return this; }

    public boolean isOpen() { return open; }

    /** 修复批 5：程序化聚焦搜索框（Screen 侧 Ctrl+F 调用）。 */
    public void focusSearch() {
        search.focus();
    }

    // ------------------------------------------------------------------
    // 搜索过滤（修复批 5）：名称模糊匹配，大小写不敏感；空查询复位全量
    // ------------------------------------------------------------------

    private void applyFilter(String q) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        if (needle.isEmpty()) {
            inventory.filter(null);
            lastInfo = null;
            return;
        }
        List<ItemStack> list = readSlots();
        int matched = 0;
        for (ItemStack s : list) {
            if (!s.isEmpty() && s.getHoverName().getString().toLowerCase().contains(needle)) matched++;
        }
        Predicate<ItemStack> p = s -> s.isEmpty()
                || s.getHoverName().getString().toLowerCase().contains(needle);
        inventory.filter(p);
        lastInfo = matched == 0 ? "背包内无匹配物品" : "匹配 " + matched + " / " + MiniInventory.SLOT_COUNT;
    }

    // ------------------------------------------------------------------
    // 生命周期
    // ------------------------------------------------------------------

    /** 弹出：挂到层上、居中偏下、置顶。幂等（已开则直接返回）。 */
    public void show(WindowLayer layer) {
        if (open || layer == null) return;
        open = true;
        if (window.parent != layer) layer.add(window);
        position(layer);
        layer.bringToFront(window);
        window.open(); // visible = true
        if (onShown != null) onShown.run();
    }

    /** 关闭：走 {@link Window#close()}（隐藏 + 从层分离 + 触发 onClose → handleClosed）。 */
    public void hide() {
        if (!open) return;
        window.close();
    }

    /** 开 → 关 / 关 → 开。 */
    public void toggle(WindowLayer layer) {
        if (open) hide();
        else show(layer);
    }

    /**
     * Screen 重建界面（{@code layer.clearChildren()}）后调用：若浮窗当时处于打开态，
     * 把窗口节点重新挂回层上并重定位，避免重建后浮窗静默消失。
     */
    public void remountIfOpen(WindowLayer layer) {
        if (!open || layer == null) return;
        if (window.parent != layer) layer.add(window);
        position(layer);
        layer.bringToFront(window);
    }

    /** X 按钮 / close() 统一收口：复位打开态并通知 Screen 侧。 */
    private void handleClosed() {
        open = false;
        if (onHidden != null) onHidden.run();
    }

    // ------------------------------------------------------------------
    // 位置：水平居中、垂直偏下（原版容器 GUI 背包区）
    // ------------------------------------------------------------------

    private void position(WindowLayer layer) {
        Minecraft mc = Minecraft.getInstance();
        int sw = layer.width > 0 ? layer.width
                : (mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : WIN_W);
        int sh = layer.height > 0 ? layer.height
                : (mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScaledHeight() : WIN_H);
        int x = (sw - WIN_W) / 2;
        int y = sh - WIN_H - BOTTOM_MARGIN;
        window.position(x, Math.max(0, y));
    }

    // ------------------------------------------------------------------
    // 数据：只读拉取玩家 36 槽
    // ------------------------------------------------------------------

    /** 填充并返回只读缓冲（MiniInventory 顺序：主背包 0..26 在上，热键栏 27..35 在下）。 */
    private List<ItemStack> readSlots() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            for (int i = 0; i < MiniInventory.SLOT_COUNT; i++) buffer.set(i, ItemStack.EMPTY);
            return buffer;
        }
        Inventory inv = p.getInventory();
        for (int k = 0; k < MiniInventory.MAIN_ROWS * MiniInventory.COLS; k++) {
            buffer.set(k, orEmpty(inv.getItem(MiniInventory.COLS + k))); // inv 9..35
        }
        for (int k = 0; k < MiniInventory.HOTBAR_ROWS * MiniInventory.COLS; k++) {
            buffer.set(MiniInventory.MAIN_ROWS * MiniInventory.COLS + k, orEmpty(inv.getItem(k))); // inv 0..8
        }
        return buffer;
    }

    private static ItemStack orEmpty(ItemStack s) {
        return s == null ? ItemStack.EMPTY : s;
    }

    /** MiniInventory 槽位号 → 真实 MC 槽位号后上抛。 */
    private void onMiniSlot(int miniSlot) {
        int mcSlot = miniSlot < MiniInventory.MAIN_ROWS * MiniInventory.COLS
                ? miniSlot + MiniInventory.COLS
                : miniSlot - MiniInventory.MAIN_ROWS * MiniInventory.COLS;
        if (onSlotClick != null) onSlotClick.accept(mcSlot);
    }
}
