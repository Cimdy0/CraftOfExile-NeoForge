package com.cimdy.coe.item.accessory;

import com.cimdy.coe.mixin.client.screen.InventoryScreenMixin;
import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;

/**
 * 饰品槽（原版背包 UI 内的通用饰品专用槽）：允许放入的物品由所属槽组的 category
 * （物品 {@link com.cimdy.coe.data.codec.item.state.ModifiableState#types()} 类型链命中该类别路径，
 * 如 {@code accessory/amulet} / {@code armor/legs} / {@code weapon/off_hand/quiver}）决定，单件不堆叠。
 *
 * <p>槽位坐标挂在背包窗口<b>右侧外置面板</b>（{@code x=181}，窗口宽 176），
 * 置于 Charm 槽之下，按全局槽位下标垂直排列。底板/槽框由 {@code InventoryScreenMixin} 绘制，
 * 槽内物品/悬停高亮/tooltip 全部走原版 {@code extractSlot} 通道。
 */
public class AccessorySlot extends Slot {

    /** 槽位物品左上角 X（相对背包窗口左上角；176 = 窗口宽，故 181 位于窗口右外侧）。 */
    public static final int SLOT_X = 181;
    /** 第一槽物品左上角 Y（置于 Charm 槽 3 槽之下，沿用 22 槽距）。 */
    public static final int SLOT_Y0 = 127;
    /** 槽间距（18 槽宽 + 4 间隙）。 */
    public static final int SLOT_STEP = 22;

    private final Identifier category;
    private final int globalIndex;

    public AccessorySlot(AccessoryContainer container, int globalIndex, Identifier category, int x, int y) {
        super(container, globalIndex, x, y);
        this.category = category;
        this.globalIndex = globalIndex;
    }

    /** 按全局槽位描述创建（坐标由常量推导，渲染侧共用同一组常量，避免两处硬编码漂移）。 */
    public static AccessorySlot of(AccessoryContainer container, AccessorySlotRegistry.SlotDescriptor d) {
        return new AccessorySlot(container, d.globalIndex(), d.category(),
                SLOT_X, SLOT_Y0 + d.globalIndex() * SLOT_STEP);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 统一类别判定：物品状态类型链命中本槽类别路径即允许放入（取代原 item_tag 标签判定）。
        return !stack.isEmpty() && ModifiableStateOps.get(stack)
                .map(state -> ItemTypeHierarchy.isWithin(category, state.types()))
                .orElse(false);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    /** 全局槽位下标（= Stat 注入前缀 {@code accessory:<globalIndex>} 的索引）。 */
    public int globalIndex() {
        return globalIndex;
    }
}
