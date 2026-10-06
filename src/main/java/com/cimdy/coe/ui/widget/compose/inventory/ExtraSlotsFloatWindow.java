package com.cimdy.coe.ui.widget.compose.inventory;

import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.registry.item.CharmSlotRegistry;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

/**
 * C.10 药剂 / 护符 / 饰品三列浮窗（WindowLayer 浮窗骨架）。
 *
 * <p>设计目标：把原本独立容器屏 {@code ExtraSlotsScreen} 里右侧的「药剂 / 护符 / 饰品」三列专用槽，
 * 改造成原版背包界面上的 <b>WindowLayer 浮窗</b>（MC 单屏栈约束：独立 Screen 会顶掉当前背包屏）。
 *
 * <p><b>当前为 UI 骨架</b>：本组件每帧从已有注册表<b>只读</b>拉取各列槽数并画出空槽框网格，
 * 槽数与 {@code ExtraSlotsScreen} 一致——
 * <ul>
 *   <li>药剂列：{@link FlaskSlotRegistry#slots}（读 {@code LoadEvent.FLASK_SLOT_MAP} 快照，兜底 2）；</li>
 *   <li>护符列：{@link CharmSlotRegistry#slots}（属性驱动 [0,5]，基准 3）；</li>
 *   <li>饰品列：{@link AccessorySlotRegistry#slots}（读 {@code LoadEvent.ACCESSORY_SLOT_MAP} 快照，兜底 3 组）。</li>
 * </ul>
 * 槽内物品渲染与「取/放」交互<b>待服务端</b>：复用 {@code ExtraSlotsMenu} 服务端权威链路
 * （只读调用，本组件不写数据层），待浮窗宿主接管容器后在此接线。包就绪前这里只画空槽框，
 * 不读物品、不响应点击、不发包。
 *
 * <p>生命周期与 {@link InventoryFloatWindow} 一致：{@link #show}/{@link #toggle}/{@link #remountIfOpen}
 * 由宿主（原版背包屏 mixin 或 WidgetScreen）在 resize / rebuildUi 时调用重挂。
 */
public class ExtraSlotsFloatWindow {

    private static final int SLOT = Theme.SLOT_SIZE;
    private static final int PAD = 6;
    private static final int COL_GAP = 6;
    /** 列标题行高（槽网格上方一列小字）。 */
    private static final int TITLE_H = 10;

    private final Window window;
    private final Panel panel;
    private boolean open;

    public ExtraSlotsFloatWindow() {
        this.panel = new Panel();
        this.window = Window.of(Component.translatable("window.coe.extra_slots"))
                .styled()
                .closable(true)
                .onClose(() -> open = false);
        window.body().add(panel);
        // 尺寸按三列 + 最长列槽数预声明（每帧 renderSelf 再按实时 count 画网格）。
        int w = PAD * 2 + SLOT * 3 + COL_GAP * 2;
        window.flex.size(w, 120);
    }

    /** 宿主在 resize/rebuildUi 后重挂到当前 WindowLayer。 */
    public void remountIfOpen(WindowLayer layer) {
        if (!open) return;
        layer.removeWindow(window);
        layer.add(window);
        position(layer);
    }

    public void show(WindowLayer layer) {
        open = true;
        layer.removeWindow(window);
        layer.add(window);
        position(layer);
    }

    public void hide(WindowLayer layer) {
        open = false;
        layer.removeWindow(window);
    }

    public void toggle(WindowLayer layer) {
        if (open) hide(layer);
        else show(layer);
    }

    public boolean isOpen() { return open; }

    /** 浮窗靠右上摆放（原版背包右侧三列的旧位置语义）。 */
    private void position(WindowLayer layer) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sx = sw - window.flex.width - 20;
        window.position(Math.max(4, sx), 24);
        window.layoutSelf();
    }

    // ------------------------------------------------------------------
    // 三列空槽网格面板（只读骨架）
    // ------------------------------------------------------------------

    private static final class Panel extends Node {
        Panel() {
            this.id = "extra-slots-panel";
            column();
            padding(PAD, 0);
        }

        @Override
        protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
            Minecraft mc = Minecraft.getInstance();
            AbstractClientPlayer player = mc.player;
            if (player == null) return;

            var access = player.level().registryAccess();
            int flask = FlaskSlotRegistry.slots(access).size();
            int charm = CharmSlotRegistry.slots(player).size();
            int accessory = AccessorySlotRegistry.slots(access).size();

            int[] counts = { flask, charm, accessory };
            String[] titles = { "flask", "charm", "accessory" };

            int x0 = absX + PAD;
            int y0 = absY + PAD;
            int titleColor = Theme.current().textBright();
            int slotBg = Theme.current().panelSlotFill();
            int slotBorder = Theme.current().slotBorder();

            for (int c = 0; c < 3; c++) {
                int colX = x0 + c * (SLOT + COL_GAP);
                Draw.text(g, Component.literal(titles[c]), colX, y0, titleColor);
                // 待服务端：此处槽内应渲染 ExtraSlotsMenu 对应槽位的 ItemStack（读客户端容器），
                // 并把点击路由到服务端权威容器交互（复用 ExtraSlotsMenu 链路）。
                // 骨架阶段只画空槽框。
                for (int i = 0; i < counts[c]; i++) {
                    int slotY = y0 + TITLE_H + i * SLOT;
                    Draw.fill(g, colX, slotY, SLOT, SLOT, slotBg);
                    Draw.outline(g, colX, slotY, SLOT, SLOT, slotBorder);
                }
            }
        }
    }
}
