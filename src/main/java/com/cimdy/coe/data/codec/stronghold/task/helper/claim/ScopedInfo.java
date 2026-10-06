package com.cimdy.coe.data.codec.stronghold.task.helper.claim;

import com.cimdy.coe.data.codec.stronghold.storage.stock.StockRange;

public interface ScopedInfo {
    Enum<StockRange> range();
    int priority();
    int remaining();
    int minTransfer();
}
