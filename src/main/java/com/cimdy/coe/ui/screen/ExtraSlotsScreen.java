package com.cimdy.coe.ui.screen;

import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.item.accessory.AccessorySlot;
import com.cimdy.coe.item.charm.CharmEngine;
import com.cimdy.coe.item.charm.CharmItem;
import com.cimdy.coe.item.charm.CharmSlot;
import com.cimdy.coe.item.flask.FlaskHelper;
import com.cimdy.coe.item.flask.FlaskItem;
import com.cimdy.coe.item.flask.FlaskSlot;
import com.cimdy.coe.ui.menu.ExtraSlotsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 药剂 / 护符 / 饰品 统一面板界面。
 *
 * <p>背包 36 槽由基类 {@link AbstractPanelScreen} 渲染（含槽位底色）；三类专用槽在背包右侧三列集中渲染，
 * 其槽位底色亦由基类 {@code renderInventoryPanel} 统一绘制（遍历全部 active 槽）。
 * 本类仅补：列标题（药剂 / 护符 / 饰品）+ Flask/Charm 竖向充能条。
 *
 * <p>物品拾取 / 放下 / 拖拽 / 数字键由继承的原版 {@code AbstractContainerScreen} 链路处理，
 * 数据经菜单 {@link ExtraSlotsMenu} 落玩家附件（服务端权威）。
 */
public class ExtraSlotsScreen extends AbstractPanelScreen<ExtraSlotsMenu> {

    /**
     * 充能条：满格金黄，底槽深灰（取原版 GUI 调色）。
     *
     * <p>原来是 {@code private static final int}：类加载时即冻结，换肤对它无效。改为每次现取；
     * 满格金与全局金色强调同源（{@code goldAccent()}），换肤后两处仍会一起变。
     */
    private static int chargeColor() {
        return Theme.current().goldAccent();
    }

    private static int chargeTrackColor() {
        return Theme.current().chargeTrackBg();
    }

    public ExtraSlotsScreen(ExtraSlotsMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 基类：画背包 + 三类专用槽底色，再画物品 / 悬停高亮 / tooltip
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        // 列标题 / 充能条跟随专用槽区域恒显（B 键只隐藏背包查看格 ToggleSlot，
        // 专用槽是功能槽、物品恒渲染——标题与充能条若随面板消失会与槽位不一致）。
        drawColumnLabels(graphics);
        drawChargeBars(graphics);
    }

    /** 列标题：药剂 / 护符 / 饰品，置于各列首槽上方。 */
    private void drawColumnLabels(GuiGraphicsExtractor graphics) {
        int y = topPos + ExtraSlotsMenu.COL_Y0 - 12;
        graphics.text(Minecraft.getInstance().font, Component.translatable("ui.coe.flask"),
                leftPos + ExtraSlotsMenu.FLASK_X, y, Theme.current().textBright(), false);
        graphics.text(Minecraft.getInstance().font, Component.translatable("ui.coe.charm"),
                leftPos + ExtraSlotsMenu.CHARM_X, y, Theme.current().textBright(), false);
        graphics.text(Minecraft.getInstance().font, Component.translatable("ui.coe.trinket"),
                leftPos + ExtraSlotsMenu.ACCESSORY_X, y, Theme.current().textBright(), false);
    }

    /** Flask / Charm 槽右侧竖向充能条（POE2 充能可视化；空槽或非对应物品不绘制）。 */
    private void drawChargeBars(GuiGraphicsExtractor graphics) {
        for (Slot slot : this.menu.slots) {
            if (!(slot instanceof FlaskSlot) && !(slot instanceof CharmSlot)) continue;
            int sx = leftPos + slot.x, sy = topPos + slot.y;
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof FlaskItem flask) {
                drawBar(graphics, sx + 18, sy, flask.charges(stack), FlaskHelper.flaskMaxCharges(stack, flask));
            } else if (stack.getItem() instanceof CharmItem charm) {
                drawBar(graphics, sx + 18, sy, charm.charges(stack), CharmEngine.charmMaxCharges(stack, charm));
            }
        }
    }

    private void drawBar(GuiGraphicsExtractor graphics, int x, int y, int cur, int max) {
        int m = Math.max(1, max);
        int c = Math.max(0, Math.min(m, cur));
        int filled = Math.round(16f * c / m);
        graphics.fill(x, y, x + 3, y + 16, chargeTrackColor());
        if (filled > 0) graphics.fill(x, y + 16 - filled, x + 3, y + 16, chargeColor());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
