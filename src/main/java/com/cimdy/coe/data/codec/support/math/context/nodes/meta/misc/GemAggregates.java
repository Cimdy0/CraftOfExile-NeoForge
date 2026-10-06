package com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;

/**
 * 装配宝石读值节点聚合方式常量（对应 {@link GemValue#aggregate()} 的收口字符串）。
 */
public final class GemAggregates {
    private GemAggregates() {}

    public static final String FIRST = "first";
    public static final String COUNT = "count";
}
