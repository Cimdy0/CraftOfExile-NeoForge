package com.cimdy.coe.registry.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.item.charm.CharmSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 护符槽访问入口（容量属性驱动，取代原数据包注册表）。
 *
 * <p>护符槽数完全由属性 {@code AttributeRegistry#CHARM_SLOT} 决定（基础 3，天赋 / 腰带词缀叠加，钳 [0,5]），
 * 经 {@link CharmSlots#count} 读取，不再从数据包 / 注册表取数。本类把容量派生为有序的全局槽位列表
 * （{@link AccessorySlotRegistry.SlotDescriptor}）供统一窗口菜单构造槽、容器容量复用；
 * 类别固定 {@code accessory/charm}（护符物品类型路径），与饰品 / 药剂槽同口径。
 */
public final class CharmSlotRegistry {
    private CharmSlotRegistry() {
    }

    /**
     * 读取护符槽（容量属性驱动）：按 {@link CharmSlots#count(Player)} 派生 {@code count} 个有序槽位。
     *
     * @param player 用于经 {@link CharmSlots#count} 取当前属性累加容量（天赋 / 腰带生效）。
     */
    public static List<AccessorySlotRegistry.SlotDescriptor> slots(Player player) {
        int count = CharmSlots.count(player);
        List<AccessorySlotRegistry.SlotDescriptor> out = new ArrayList<>(count);
        Identifier group = Identifier.parse(CraftOfExile.MODID + "/charm");
        for (int i = 0; i < count; i++) {
            out.add(new AccessorySlotRegistry.SlotDescriptor(i, group, i,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "accessory/charm")));
        }
        return out;
    }

    /** 护符槽总数（= 当前属性容量）。 */
    public static int totalSlots(Player player) {
        return CharmSlots.count(player);
    }
}
