package com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;

/**
 * 移动状态读值节点模式常量（对应 {@link MovementValue#mode()} 的收口字符串）。
 */
public final class MovementModes {
    private MovementModes() {}

    public static final String STATIONARY = "stationary";
    public static final String MOVING = "moving";
    public static final String DISTANCE = "distance";
    public static final String SPEED = "speed";
}
