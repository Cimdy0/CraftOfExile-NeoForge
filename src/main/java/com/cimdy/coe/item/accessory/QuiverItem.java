package com.cimdy.coe.item.accessory;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Quiver（箭袋，POE2 实证）：装备在背包 UI 的 Quiver 槽，单槽。
 * 携带 {@code MODIFIABLE_STATE}（type=quiver）；后续接线阶段由「装备箭袋加成」(#55) 等词缀引用。
 * 数值/词缀覆盖留待内容层。
 */
public class QuiverItem extends Item {
    public QuiverItem(Properties p) {
        super(p);
    }

    /** 生成空 MODIFIABLE_STATE（type=quiver），供后续派生词缀识别与加成。 */
    public static EquipmentState quiverState() {
        return new EquipmentState(100, Rarity.COMMON, List.of(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "quiver")), Map.of(),
                Optional.empty(), List.of(), 0, List.of());
    }
}
