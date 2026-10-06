package com.cimdy.coe.data.codec.item.currency;

/** 一条通货的数据描述：前置条件 + 操作。每种通货 = 一个此类实例（当前由代码注册，将来可改为数据包 JSON）。 */
public record CurrencyOperation(Precondition precondition, Operation operation) {
    public static final CurrencyOperation NONE = new CurrencyOperation(Precondition.ALLOW_ALL, Operation.none());
}
