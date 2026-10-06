package com.cimdy.coe.ui.widget.screen;

import com.cimdy.coe.network.inventory.TakePutPacket;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.compose.inventory.InventoryFloatWindow;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.StylePresets;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 用本库搭界面的 Screen 基类。子类只需要实现 {@link #buildUi}。
 *
 * <p>与旧 {@code WindowGroup} 时代的区别：界面<b>不再</b>把状态写进 NBT 标签、
 * 也不再每帧轮询「按钮是不是刚按下」。节点直接持有 {@code onClick} 回调与
 * {@code Supplier} 数据源，业务代码写在回调里，读什么画什么由 Supplier 决定。
 *
 * <h2>默认行为</h2>
 * <ul>
 *   <li>默认画原版菜单背景（模糊 + 暗化），与旧 Screen 观感一致；不需要时调
 *       {@link #dimBackground(boolean) dimBackground(false)} 让游戏世界清晰可见；</li>
 *   <li>不暂停游戏（{@code isPauseScreen() == false}）；</li>
 *   <li>UI 没吃掉的事件继续走原版链路，ESC 关界面照常生效。</li>
 * </ul>
 */
public abstract class WidgetScreen extends Screen implements WidgetHosted {

    protected final WidgetHost ui = new WidgetHost();

    private boolean dimBackground = true;
    private boolean pauseGame = false;

    protected WidgetScreen(Component title) {
        super(title);
    }

    // ------------------------------------------------------------------
    // 背包浮窗（计划 §3.5b）：任意 WidgetScreen 上按 B 弹出 / 关闭背包浮窗，
    // 挂在 WindowLayer 上，不替换当前 Screen。加法式：只增字段与私有安装方法，不改既有签名。
    // ------------------------------------------------------------------

    private InventoryFloatWindow backpackWindow;
    /** 仅浮窗打开期间存在的 Esc 绑定（关闭浮窗而非整屏）；关闭即解绑，避免永久吞掉 Esc。 */
    private WidgetHost.KeyBinding backpackEsc;
    private boolean backpackKeyBound;

    /** 懒建背包浮窗 + 绑定 B 键 + 重建后重挂。幂等：init() / rebuildUi() 均可安全重复调用。 */
    /** 背包浮窗槽位点击：上抛真实 MC 槽位号 → 发取放包（服务端权威执行，回包镜像本地）。 */
    private void onBackpackSlotClick(int mcSlot) {
        ClientPacketDistributor.sendToServer(new TakePutPacket(mcSlot, 0));
    }

    private void installBackpackOverlay() {
        if (backpackWindow == null) {
            backpackWindow = new InventoryFloatWindow();
            backpackWindow.onSlotClick(this::onBackpackSlotClick);
            backpackWindow.onShown(() -> backpackEsc = ui.bindKey(GLFW.GLFW_KEY_ESCAPE, backpackWindow::hide));
            backpackWindow.onHidden(() -> {
                if (backpackEsc != null) {
                    ui.unbind(backpackEsc);
                    backpackEsc = null;
                }
            });
        }
        if (!backpackKeyBound) {
            backpackKeyBound = true;
            // B：toggle。输入框聚焦时 layer.keyPressed 先吃掉打字键，快捷键不会打断输入。
            ui.bindKey(GLFW.GLFW_KEY_B, () -> backpackWindow.toggle(ui.layer()));
        }
        // layer.clearChildren() 会清掉浮窗节点；若当时开着，重建后重新挂回。
        backpackWindow.remountIfOpen(ui.layer());
    }

    @Override
    public WidgetHost ui() { return ui; }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    /**
     * 搭界面：往 {@code root} 上挂窗口。每次 {@code init()}（含窗口尺寸变化）都会重新调用，
     * 因此这里应当是<b>纯构建</b>，不要在里面做网络请求之类的副作用。
     */
    protected abstract void buildUi(WindowLayer root);

    @Override
    protected void init() {
        super.init();
        StylePresets.setScreen(screenId());
        Theme.setPresetChangeListener(this::rebuildUi);
        ui.clear();
        buildUi(ui.layer());
        ui.resize(this.width, this.height);
        installBackpackOverlay();
    }

    /** 数据结构性变化后重建整个界面（例如列表来源换了一批）。 */
    protected void rebuildUi() {
        ui.clear();
        buildUi(ui.layer());
        ui.resize(this.width, this.height);
        installBackpackOverlay();
    }

    protected WindowLayer layer() { return ui.layer(); }

    /** 当前屏幕在级联预设中的键名（默认取类名；可在子类覆写以指向自定义 screens.<id> 覆盖）。 */
    protected String screenId() { return getClass().getSimpleName(); }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        StylePresets.setScreen(screenId());
        ui.render(g, mouseX, mouseY, partialTick);
        renderBackpackCarried(g, mouseX, mouseY);
    }

    /** 背包浮窗打开且手持物品时，在鼠标处渲染物品图标 + 数量（MC 标准 carried 观感）。 */
    private void renderBackpackCarried(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (backpackWindow == null || !backpackWindow.isOpen()) return;
        ItemStack c = InventoryFloatWindow.carried();
        if (c.isEmpty()) return;
        int box = Theme.SLOT_SIZE;
        int cx = mouseX + 8;
        int cy = mouseY - 8 - box / 2;
        Draw.item(g, c, cx, cy, box);
        if (c.getCount() > 1) {
            Component count = Component.literal(String.valueOf(c.getCount()));
            Draw.text(g, count, cx + box - Draw.width(count) - 1, cy + box - Draw.lineHeight() - 1, 0xFFFFFFFF);
        }
    }

    /** 原版会画一层模糊 + 暗化；默认开启以还原旧 Screen 的菜单背景观感。 */
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (dimBackground) super.extractBackground(g, mouseX, mouseY, partialTick);
    }

    protected void dimBackground(boolean b) { this.dimBackground = b; }

    protected void pauseGame(boolean b) { this.pauseGame = b; }

    @Override
    public boolean isPauseScreen() { return pauseGame; }

    @Override
    public void tick() {
        super.tick();
        ui.tick();
    }

    // ------------------------------------------------------------------
    // 事件：先给 UI，剩下的还给原版
    // ------------------------------------------------------------------

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return ui.mouseClicked(event) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return ui.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return ui.mouseDragged(event, dx, dy) || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return ui.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // 修复批 5：背包浮窗打开时 Ctrl+F 聚焦搜索框（附 A.4 自定快捷键）。
        if (backpackWindow != null && backpackWindow.isOpen()
                && event.key() == GLFW.GLFW_KEY_F
                && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            backpackWindow.focusSearch();
            return true;
        }
        return ui.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event) || super.charTyped(event);
    }
}
