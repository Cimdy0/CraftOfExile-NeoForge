package com.cimdy.coe.data.codec.stronghold.storage;

import com.cimdy.coe.data.codec.stronghold.storage.stock.StockEntry;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CapacityCalculator {
    private int usedSlots = 0;
    private final int totalSlots;
    private final Map<ItemResource, StockEntry.SlotRequirement> requirements;

    public CapacityCalculator(int totalSlots) {
        this.totalSlots = totalSlots;
        this.requirements = new LinkedHashMap<>();
    }

    public void addRequirement(ItemResource resource, StockEntry.SlotRequirement req) {
        requirements.put(resource, req);
    }

    /**
     * 分配槽位并返回每个物品的 (newMax, total)。
     * @param originalMaxMap 原始 max 值
     */
    public Map<ItemResource, int[]> allocate(Map<ItemResource, Integer> originalMaxMap) {
        // 计算总占用槽位
        Map<ItemResource, Integer> allocated = new HashMap<>();
        int usedSlots = 0;
        for (var entry : requirements.entrySet()) {
            usedSlots += entry.getValue().desiredSlots();
            allocated.put(entry.getKey(), entry.getValue().desiredSlots());
        }

        // 计算最后剩余槽位
        int remainingSlots = totalSlots - usedSlots;
        this.usedSlots = remainingSlots;

        // 计算 total 和 newMax
        Map<ItemResource, int[]> results = new HashMap<>();
        for (var entry : requirements.entrySet()) {
            ItemResource res = entry.getKey();
            int slots = allocated.get(res);
            int stackLimit = entry.getValue().stackLimit();
            int total = (slots + remainingSlots) * stackLimit;
            int originalMax = originalMaxMap.getOrDefault(res, 0);
            int newMax = Math.min(originalMax, total);
            results.put(res, new int[]{newMax, total});
        }

        return results;
    }

    public boolean hasEmptySlot() {
        return usedSlots > 0;
    }
}