package com.cimdy.coe.ui.screen;

import com.cimdy.coe.ui.menu.TestPanelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 测试用面板界面：验证 {@link AbstractPanelScreen} 基类——
 * 内嵌可交互背包（Menu 操控物品、点击拾取 / 拖拽）、面板默认屏幕正下方、
 * B 键切换显隐（隐藏时槽位物品同步隐藏）。
 *
 * <p>位置 / 底色自定义示例（不调用则用基类默认：正下方 + 默认底色）：
 * <pre>
 * &#64;Override protected void init() {
 *     super.init();
 *     enableInventoryPanel(40, 120, 0xC0182030);
 * }
 * </pre>
 */
public class TestPanelScreen extends AbstractPanelScreen<TestPanelMenu> {

    public TestPanelScreen(TestPanelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
