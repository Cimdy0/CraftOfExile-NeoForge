package com.cimdy.coe.data.codec.stronghold.task.helper.supply;

import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

public record AggregatedSupply(ItemResource resource, int priority, int totalAmount, List<SupplyInfo> sources) {
        public AggregatedSupply copyWithAmount(int newAmount) {
            return new AggregatedSupply(resource, priority, newAmount, new ArrayList<>(sources));
        }
    }
