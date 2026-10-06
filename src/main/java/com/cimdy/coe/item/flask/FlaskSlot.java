package com.cimdy.coe.item.flask;

import com.cimdy.coe.mixin.client.screen.InventoryScreenMixin;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Flask 槽位（原版背包 UI 内的药剂专用槽）：只允许放入 {@link FlaskItem}，单件不堆叠。
 * <p>
 * 槽位坐标挂在背包窗口<b>右侧外置面板</b>（{@code x=181}，窗口宽 176），
 * 原版背包内部并无空白区可容纳（左护甲 / 中模型 / 右 2×2 合成 + 结果格 + 箭头已占满上半区）。
 * 面板底板与槽框由 {@code InventoryScreenMixin} 在背景阶段绘制，
 * 槽内物品/悬停高亮/tooltip 全部走原版 {@code extractSlot} 通道，无需额外处理。
 */
public class FlaskSlot extends Slot {

    /** 槽位物品左上角 X（相对背包窗口左上角；176 = 窗口宽，故 181 位于窗口右外侧）。 */
    public static final int SLOT_X = 181;
    /** 第一槽（生命药剂）物品左上角 Y。 */
    public static final int SLOT_Y0 = 9;
    /** 槽间距（18 槽宽 + 4 间隙）。 */
    public static final int SLOT_STEP = 22;

    public FlaskSlot(FlaskContainer container, int index, int x, int y) {
        super(container, index, x, y);
    }

    /** 按槽序创建（坐标由常量推导，渲染侧共用同一组常量，避免两处硬编码漂移）。 */
    public static FlaskSlot of(FlaskContainer container, int index) {
        return new FlaskSlot(container, index, SLOT_X, SLOT_Y0 + index * SLOT_STEP);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 统一类别判定：物品携带药剂状态（MODIFIABLE_STATE 组件）即属药剂类别，取代 instanceof FlaskItem。
        return !stack.isEmpty() && ModifiableStateOps.flask(stack) != null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
