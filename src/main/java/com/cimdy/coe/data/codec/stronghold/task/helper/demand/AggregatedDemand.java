package com.cimdy.coe.data.codec.stronghold.task.helper.demand;

import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public record AggregatedDemand(ItemResource resource, int priority, int totalAmount, List<DemandInfo> targets) {}
