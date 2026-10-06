package com.cimdy.coe.data.codec.stronghold.task.helper.result;

import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import net.minecraft.core.GlobalPos;

import java.util.List;
import java.util.Map;

public record DistributedResult(
        Map<GlobalPos, List<CarryTask>> newTasksByHold,
        Map<GlobalPos, List<DemandInfo>> newDemands,
        Map<GlobalPos, List<SupplyInfo>> newSupplies
) {
    public static DistributedResult empty() {
        return new DistributedResult(Map.of(), Map.of(), Map.of());
    }
}