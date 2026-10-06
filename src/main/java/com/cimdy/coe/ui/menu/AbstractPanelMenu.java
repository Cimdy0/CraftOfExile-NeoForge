package com.cimdy.coe.ui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 面板基类菜单：统一持有玩家背包 36 槽（主背包 27 + 快捷栏 9），
 * <b>物品栏数据由 Menu 操控</b>（原版容器机制）——点击拾取 / 放下 / 拖拽由
 * {@link #clicked} 在服务端权威处理，Screen 仅渲染与转发事件。
 *
 * <p>槽位坐标相对 {@code leftPos/topPos}（原版惯例）：主背包 3×9 起点 (8, 18)，
 * 快捷栏 9 起点 (8, 76)。子类可在背包槽位之后追加业务槽位。
 *
 * <p><b>Shift 快速移动</b>：{@link #quickMoveStack} 是 {@link AbstractContainerMenu}
 * 的 abstract 方法，本类提供<b>空实现</b>（返回 {@link ItemStack#EMPTY}，即无快速移动，
 * 对齐原版「未实现即不可用」语义）。子类需要时自行覆写（可参考原版 PlayerInventoryMenu /
 * ChestMenu 的移动逻辑）。
 *
 * <p><b>槽位渲染控制</b>：原版 {@code AbstractContainerScreen.extractSlots} 遍历
 * {@code menu.slots} 且以 {@link Slot#isActive()} 过滤渲染 / 命中——即「哪些槽位显示」
 * 的裁决权在 Menu 层。本类的背包槽位是 {@link ToggleSlot}，可用
 * {@link #setInventorySlotsActive} 整体开关（配合 Screen 的 B 键隐藏面板）。
 */
public abstract class AbstractPanelMenu extends AbstractContainerMenu {

    /** 面板内槽位网格宽高（含 8px 边距）。 */
    public static final int INVENTORY_WIDTH = 180;
    /**
     * 面板区域高度，与 {@link com.cimdy.coe.ui.screen.AbstractPanelScreen#INVENTORY_HEIGHT}
     * 保持同一口径 102（原值 158 为过时笔误：布局区主背包 18..54 + 快捷栏 76..94 + 8 边距 = 102；
     * 本常量无消费者，修正仅为消除口径差误导）。
     */
    public static final int INVENTORY_HEIGHT = 102;

    protected final Player player;

    /** 背包槽位网格起点坐标（相对 leftPos/topPos），子类可按需覆盖。 */
    protected static final int DEFAULT_MAIN_X = 8, DEFAULT_MAIN_Y = 18, DEFAULT_HOTBAR_Y = 76;

    protected AbstractPanelMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, DEFAULT_MAIN_X, DEFAULT_MAIN_Y, DEFAULT_HOTBAR_Y);
    }

    /**
     * 带坐标参数的构造：背包 36 槽（主背包 27 + 快捷栏 9）按给定起点排布，
     * 槽位坐标相对 leftPos/topPos（原版惯例）。子类传入自身布局坐标即可复用，无需重复写循环。
     */
    protected AbstractPanelMenu(MenuType<?> type, int containerId, Inventory playerInventory,
                                int mainX, int mainY, int hotbarY) {
        super(type, containerId);
        this.player = playerInventory.player;

        // 主背包 27 格（3 行 × 9 列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new ToggleSlot(playerInventory, 9 + row * 9 + col, mainX + col * 18, mainY + row * 18));
            }
        }
        // 快捷栏 9 格
        for (int col = 0; col < 9; col++) {
            addSlot(new ToggleSlot(playerInventory, col, mainX + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    /** 无 Shift 快速移动（原版默认语义）；子类需要时覆写。 */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /**
     * 开关全部背包槽位（{@link ToggleSlot} 的 {@code isActive}）。
     * {@code isActive()==false} 的槽位：原版 {@code extractSlots} 不渲染物品、
     * 鼠标命中（{@code getHoveredSlot}）不选中——用于 Screen 隐藏面板时同步隐藏物品。
     */
    public void setInventorySlotsActive(boolean active) {
        for (Slot slot : this.slots) {
            if (slot instanceof ToggleSlot toggle) {
                toggle.active = active;
            }
        }
    }

    /** 可整体开关的槽位：isActive 由 Menu 控制（原版渲染 / 命中的裁决依据）。 */
    protected static class ToggleSlot extends Slot {
        private boolean active = true;

        ToggleSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }
}
