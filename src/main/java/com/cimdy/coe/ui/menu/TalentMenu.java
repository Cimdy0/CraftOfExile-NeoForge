package com.cimdy.coe.ui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 天赋树菜单：天赋树本身不涉及物品栏交互，因此没有任何槽位。
 * 仅作为「服务端以菜单形式打开界面」的载体，使界面打开受服务端权威控制。
 */
public class TalentMenu extends AbstractContainerMenu {
    public TalentMenu(int containerId, Inventory playerInventory) {
        super(MenuRegistry.TALENT_MENU.get(), containerId);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
