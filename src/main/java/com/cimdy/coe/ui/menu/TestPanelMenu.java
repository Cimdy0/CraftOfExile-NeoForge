package com.cimdy.coe.ui.menu;

import net.minecraft.world.entity.player.Inventory;

/**
 * 测试用面板菜单：直接继承 {@link AbstractPanelMenu}（背包 36 槽 + 原版容器交互），
 * 用于验证基类「Menu 操控物品栏 + 内嵌背包面板」链路。Shift 快速移动 = 原版默认（无）。
 */
public class TestPanelMenu extends AbstractPanelMenu {

    public TestPanelMenu(int containerId, Inventory playerInventory) {
        super(MenuRegistry.TEST_PANEL_MENU.get(), containerId, playerInventory);
    }
}
