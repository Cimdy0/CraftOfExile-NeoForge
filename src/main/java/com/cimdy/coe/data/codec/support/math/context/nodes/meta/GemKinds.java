package com.cimdy.coe.data.codec.support.math.context.nodes.meta;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;

/**
 * 装配宝石读值节点种类常量（对应 {@link GemValue#kind()} 的收口字符串）。
 */
public final class GemKinds {
    private GemKinds() {}

    public static final String ALL = "all";
    public static final String SKILL = "skill";
    public static final String SUPPORT = "support";
}
