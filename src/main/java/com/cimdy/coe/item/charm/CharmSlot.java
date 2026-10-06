package com.cimdy.coe.item.charm;

import com.cimdy.coe.mixin.client.screen.InventoryScreenMixin;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Charm 槽位（原版背包 UI 内的护符专用槽）：只允许放入 {@link CharmItem}，单件不堆叠。
 * <p>
 * 槽位坐标挂在背包窗口<b>右侧外置面板</b>，位于 Flask 槽（y=9,31）下方（y=61 起），
 * 与 Flask 共用同一定位基线（{@code x=181}），由 {@code InventoryScreenMixin} 绘制底板/槽框/充能条。
 */
public class CharmSlot extends Slot {

    /** 槽位物品左上角 X（相对背包窗口左上角；176 = 窗口宽，故 181 位于窗口右外侧）。 */
    public static final int SLOT_X = 181;
    /** 第一槽物品左上角 Y（置于 2 个 Flask 槽之下）。 */
    public static final int SLOT_Y0 = 61;
    /** 槽间距（18 槽宽 + 4 间隙）。 */
    public static final int SLOT_STEP = 22;

    public CharmSlot(CharmContainer container, int index, int x, int y) {
        super(container, index, x, y);
    }

    /** 按槽序创建（坐标由常量推导，渲染侧共用同一组常量，避免两处硬编码漂移）。 */
    public static CharmSlot of(CharmContainer container, int index) {
        return new CharmSlot(container, index, SLOT_X, SLOT_Y0 + index * SLOT_STEP);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 统一类别判定：物品携带护符状态（MODIFIABLE_STATE 组件）即属护符类别，取代 instanceof CharmItem。
        return !stack.isEmpty() && ModifiableStateOps.charm(stack) != null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
