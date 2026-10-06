package com.cimdy.coe.data.codec.item.registry;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 物品分类层（解耦模型 Layer B）：承载“物品是什么”——由数据包加载的装备预设
 * （equipmentPresets），以及未来掉落系统使用的类型定义 {@link ItemClassDef}（骨架插槽）。
 */
public final class ItemClassification {

    private final Map<Identifier, List<EquipmentRead>> equipmentReads = new HashMap<>();
    private final Map<Identifier, ItemClassDef> classDefs = new HashMap<>();

    public void loadEquipmentRead(Identifier type, EquipmentRead read) {
        equipmentReads.computeIfAbsent(type, k -> new ArrayList<>()).add(read);
    }

    public Map<Identifier, List<EquipmentRead>> getEquipmentReads() {
        return equipmentReads;
    }

    public void clear() {
        equipmentReads.clear();
        classDefs.clear();
    }

    /**
     * 物品类型定义（骨架）：分类 / 内隐词缀 / 掉落钩子。
     * 当前数据包不产出，留待 Phase 7 / 掉落系统填充；此处仅定义类型与插槽。
     */
    public record ItemClassDef(
            Identifier id,
            List<AdditionType> implicits,
            Set<String> dropCategories,
            Optional<Integer> dropLevelHint,
            Optional<EntityType> dropEntityType
    ) {
    }
}
