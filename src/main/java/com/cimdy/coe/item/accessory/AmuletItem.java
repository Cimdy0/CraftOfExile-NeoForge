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
 * Amulet（护身符，POE2 实证）：装备在背包 UI 的 Amulet 槽，单槽。
 * 携带 {@code MODIFIABLE_STATE}（type=amulet）；后续接线阶段由「装备护身符加成」
 * （ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU 等）词缀引用。
 * 数值/词缀覆盖留待内容层。
 */
public class AmuletItem extends Item {
    public AmuletItem(Properties p) {
        super(p);
    }

    /** 生成空 MODIFIABLE_STATE（type=amulet），供后续派生词缀识别与加成。 */
    public static EquipmentState amuletState() {
        return new EquipmentState(100, Rarity.COMMON, List.of(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "amulet")), Map.of(),
                Optional.empty(), List.of(), 0, List.of());
    }
}
