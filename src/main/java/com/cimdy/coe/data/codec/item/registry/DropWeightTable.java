package com.cimdy.coe.data.codec.item.registry;

import com.cimdy.coe.data.codec.item.ItemWeight;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * 掉落权重层（解耦模型 Layer C）：保留原 typeWeights 语义（按物品类型聚合的掉落权重），
 * 键为类型 {@link Identifier}（modid + 类别路径），独立为第一层，归属未来掉落系统消费。
 */
public final class DropWeightTable {

    private final Map<Identifier, Map<ItemWeight.Type, Integer>> typeWeights = new HashMap<>();

    public void loadTypeWeight(Identifier type, Map<ItemWeight.Type, Integer> map) {
        typeWeights.put(type, map);
    }

    public Map<Identifier, Map<ItemWeight.Type, Integer>> getTypeWeights() {
        return typeWeights;
    }

    public void clear() {
        typeWeights.clear();
    }
}
