package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.item.accessory.AccessorySlot;
import com.cimdy.coe.item.charm.CharmSlot;
import com.cimdy.coe.item.flask.FlaskSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 「已装备（穿着 / 佩戴 / 持握）物品不可修改」守卫。
 *
 * <p>POE2 中通货一律作用于背包内的物品；本模组据此要求：目标物品只要处于装备状态，
 * 任何通货（词缀 / 品质 / 孔）都不得改写它——先卸下、再工艺。</p>
 *
 * <p>判定一律以<b>槽位身份</b>为准（不使用「与身上物品是否同堆」的内容比对，
 * 那会把背包里碰巧同型的另一件一起误判为已装备）：</p>
 * <ul>
 *   <li>本模组的专用装备槽：药剂 / 护符 / 饰品槽。</li>
 *   <li>原版玩家容器的护甲槽（容器下标 36–39）与副手槽（40）。</li>
 *   <li>当前选中快捷栏格（= 主手持有的物品）且该物品是武器或护甲。</li>
 * </ul>
 */
public final class EquipGuard {

    private EquipGuard() {
    }

    /** 玩家容器内的护甲槽起始下标（36–39 = 靴/腿/胸/头）。 */
    private static final int ARMOR_SLOT_START = 36;
    /** 玩家容器内的副手槽下标。 */
    private static final int OFFHAND_SLOT = 40;

    public static boolean isEquipped(Player player, Slot slot) {
        ItemStack target = slot.getItem();
        if (target.isEmpty()) {
            return false;
        }
        // 本模组专用装备槽（药剂 / 护符 / 饰品）
        if (slot instanceof FlaskSlot || slot instanceof CharmSlot || slot instanceof AccessorySlot) {
            return true;
        }
        if (slot.container != player.getInventory()) {
            return false;
        }
        int index = slot.getContainerSlot();
        if (index >= ARMOR_SLOT_START && index <= OFFHAND_SLOT) {
            return true;
        }
        // 主手持握：本槽就是当前选中的快捷栏格（0–8），且持的是武器 / 护甲
        return index == player.getInventory().getSelectedSlot() && isWieldable(target);
    }

    private static boolean isWieldable(ItemStack stack) {
        EquipmentState state =ModifiableStateOps.equipment(stack);
        return state != null && (state.isWeapon() || state.isArmor());
    }

    /** 玩家容器实例（供测试 / 复用；与 {@link Inventory} 同义）。 */
    static Inventory inventory(Player player) {
        return player.getInventory();
    }
}
