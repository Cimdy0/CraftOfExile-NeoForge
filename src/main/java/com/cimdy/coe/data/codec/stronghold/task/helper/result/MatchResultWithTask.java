package com.cimdy.coe.data.codec.stronghold.task.helper.result;

import com.cimdy.coe.data.codec.stronghold.task.helper.demand.DemandInfo;
import com.cimdy.coe.data.codec.stronghold.task.helper.supply.SupplyInfo;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;

import java.util.List;

public record MatchResultWithTask(
            List<CarryTask> tasks,
            List<DemandInfo> newDemands,
            List<SupplyInfo> newSupplies
    ) {
        public TransferResult toMatchResult() {
            return new TransferResult(newDemands, newSupplies);
        }
    }
