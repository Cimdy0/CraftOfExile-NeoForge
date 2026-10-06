package com.cimdy.coe.ui.screen;

import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.menu.AbstractPanelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

/**
 * 面板基类 Screen：提供「内嵌可交互背包面板」渲染。
 * <b>物品栏数据由 Menu 操控</b>（原版容器机制）：本类只负责画面板 + 转发鼠标事件，
 * 点击拾取 / 放下 / 拖拽 / 数字键等交互全部由继承的 {@link AbstractContainerScreen} 原版链路
 * （{@code mouseClicked → getHoveredSlot → slotClicked → menu.clicked}，服务端权威）处理。
 *
 * <h2>原版机制事实（决定本类设计，见相关方法）</h2>
 * <ul>
 *   <li>{@code AbstractContainerScreen.extractSlots} 遍历 {@code menu.slots} 且以
 *       {@link Slot#isActive()} 过滤——槽位<b>渲染</b>的裁决权在 Menu / Slot，不在 Screen；
 *       故面板隐藏用 {@link AbstractPanelMenu#setInventorySlotsActive}（Menu 层）实现。</li>
 *   <li>槽位物品由原版 {@code extractSlot} 画在 {@code leftPos + slot.x / topPos + slot.y}，
 *       格子底原版来自背景纹理（{@code InventoryScreen.extractBackground} blit 一张贴图）；
 *       本项目无纹理，基类补画纯色面板 + 格子。</li>
 *   <li>{@code quickMoveStack} 是 abstract（无 Shift 移动语义由 Menu 空实现给出）。</li>
 * </ul>
 *
 * <h2>面板</h2>
 * 默认<b>屏幕最下方</b>（贴底）启用（{@link #init}）；位置由子类告知：
 * <pre>
 * &#64;Override protected void init() {
 *     super.init();
 *     enableInventoryPanel(comboX, comboY); // 自定义位置
 * }
 * </pre>
 * 渲染：{@link #renderInventoryPanel}（只画槽位格子底色，<b>不画面板整体底色、不画格子 1px 边框、不渲染背景</b>）。
 *
 * <h2>背景</h2>
 * 覆写 {@link #extractBackground} 为空：原版 {@code Screen.extractBackground} 会画模糊 / 暗化
 * 全屏背景，本类禁用之使游戏世界清晰显示。
 *
 * <h2>B 键</h2>
 * 切换面板显隐（不替换当前界面）：隐藏时面板不渲染、槽位物品同步隐藏
 * （Menu 层 {@code setInventorySlotsActive(false)}）、面板区点击被吞。
 *
 * <h2>子类用法</h2>
 * <pre>
 * class MyScreen extends AbstractPanelScreen&lt;MyMenu&gt; {
 *     MyScreen(MyMenu menu, Inventory inv, Component title) { super(menu, inv, title); }
 *     &#64;Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float p) {
 *         renderInventoryPanel(g);            // 画背包面板（默认正下方）
 *         super.extractRenderState(g, mx, my, p); // 原版渲染物品 + 悬停高亮 + tooltip
 *         // ……业务区渲染
 *     }
 * }
 * </pre>
 * 前提：菜单把背包槽位 {@code addSlot} 进 menu（推荐 {@link AbstractPanelMenu}，含 {@link ToggleSlot} 开关）；
 * 面板区域不被其它 widget（如 WindowGroup）覆盖；不覆写鼠标三方法破坏原版链路。
 */
public abstract class AbstractPanelScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    /** 单槽尺寸（原版）。 */
    public static final int SLOT_SIZE = 18;

    /** 面板可视宽（原版玩家背包纹理 176 宽，含左右边距）。 */
    public static final int INVENTORY_WIDTH = 176;
    /** 面板可视高（8 顶距 + 76 槽位区 + 18 快捷栏含 8 顶距 = 102；含 8 下边距）。 */
    public static final int INVENTORY_HEIGHT = 102;

    /**
     * 槽位格子底色（深色填充；不画 1px 边框以避免与原版 extractSlot 物品 1px 偏移重叠产生错位）。
     *
     * <p>原来是 {@code private static final int}：类加载时即冻结，换肤对它无效。改为每次现取。
     */
    private static int slotFill() {
        return Theme.current().panelSlotFill();
    }

    protected final Player player;
    private boolean panelEnabled;
    private boolean panelVisible = true;

    protected AbstractPanelScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.player = inv.player;
    }

    /**
     * 默认在<b>屏幕最下方</b>（贴底）启用内嵌背包面板（{@code leftPos/topPos} 对齐面板，
     * 原版槽位渲染 / 命中以此为基准）。子类可在 {@code init} 里改调
     * {@link #enableInventoryPanel} 自定义位置。
     */
    @Override
    protected void init() {
        super.init();
        enableInventoryPanel(
                Math.max(4, (this.width - INVENTORY_WIDTH) / 2),
                this.height - INVENTORY_HEIGHT);
    }

    /** 启用内嵌背包面板：<b>位置由子类告知</b>（默认屏幕最下方居中）。 */
    protected void enableInventoryPanel(int panelX, int panelY) {
        this.panelEnabled = true;
        this.leftPos = panelX;
        this.topPos = panelY;
    }

    /** B 键切换面板显隐（不替换当前界面）；隐藏时槽位物品经 Menu 层同步隐藏。 */
    protected void toggleInventoryPanel() {
        this.panelVisible = !this.panelVisible;
        // 槽位渲染由原版 extractSlots 按 isActive 过滤（裁决权在 Menu），隐藏时同步关闭
        if (this.menu instanceof AbstractPanelMenu panelMenu) {
            panelMenu.setInventorySlotsActive(this.panelVisible);
        }
        if (!this.panelVisible) {
            this.hoveredSlot = null;
        }
    }

    protected boolean isInventoryPanelVisible() {
        return panelVisible;
    }

    /**
     * 渲染内嵌背包面板：<b>只画槽位格子底色</b>（不画面板整体底色、不画格子 1px 边框），
     * 物品 / 数量 / 悬停高亮由原版 {@code extractSlot} 渲染在格子之上。在
     * {@code super.extractRenderState} 之前调用；隐藏时不渲染。
     *
     * <p>不画 1px 边框：原版 {@code extractSlot} 将物品渲染在 {@code (slotX+1, slotY+1)}
     * 起 16×16（格子内 1px 居中边距），与格子 1px 边框会重叠产生视觉错位；省略边框后
     * 物品与格子底色天然对齐。
     */
    protected void renderInventoryPanel(GuiGraphicsExtractor graphics) {
        if (!panelEnabled || !panelVisible) return;
        for (Slot slot : this.menu.slots) {
            if (!slot.isActive()) continue;
            int sx = leftPos + slot.x, sy = topPos + slot.y;
            graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, slotFill());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // 先画背包面板格子底（在物品之下），再交给原版画物品/悬停高亮/tooltip
        renderInventoryPanel(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    /**
     * 覆写背景渲染：<b>不渲染背景</b>（原版 {@code Screen.extractBackground} 会画模糊 / 暗化
     * 全屏背景，本类覆写为空使游戏世界清晰显示）。
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 故意不调 super，禁用原版背景渲染
    }

    /** B 键：切换内嵌背包面板（不替换界面）。 */
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_B && this.panelEnabled) {
            this.toggleInventoryPanel();
            return true;
        }
        return super.keyPressed(event);
    }

    /** 面板隐藏时，面板区域点击 / 拖拽 / 释放被吞掉（槽位不可交互），其余区域走原版。 */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (!panelVisible && isInInventoryPanel(event.x(), event.y())) return true;
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!panelVisible && isInInventoryPanel(event.x(), event.y())) return true;
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!panelVisible && isInInventoryPanel(event.x(), event.y())) return true;
        return super.mouseReleased(event);
    }

    private boolean isInInventoryPanel(double mx, double my) {
        return panelEnabled
                && mx >= leftPos && mx < leftPos + INVENTORY_WIDTH
                && my >= topPos && my < topPos + INVENTORY_HEIGHT;
    }
}
