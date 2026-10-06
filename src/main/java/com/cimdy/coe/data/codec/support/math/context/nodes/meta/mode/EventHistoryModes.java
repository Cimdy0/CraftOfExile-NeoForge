package com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.event.combat.GameEvent;

/**
 * 事件历史读值节点聚合模式常量（对应 {@link EventHistoryValue#mode()} 的收口字符串）。
 */
public final class EventHistoryModes {
    private EventHistoryModes() {}

    public static final String COUNT = "count";
    /**
     * 窗口内匹配标签的<b>去重</b>数量（distinct）：对命中标签按值去重后计数，
     * 走 {@code Event.State.countTagged(..., unique=true)}。
     * <p>与 {@link #COUNT} 的差异：COUNT 数「发生了几次」，DISTINCT 数「涉及了几个不同对象」。
     * 配合 {@code GameEvent} 写入的「对象身份标签」（{@code victim_<ailment>:<uuid>}）+ PREFIX 匹配，
     * 即得 PoE2「per Enemy you've Ignited Recently」这类<b>去重敌计数</b>语义
     * ——同一个敌人被反复点燃只算 1 个，不可用触发次数近似（会把单敌多次点燃错算成多敌）。
     */
    public static final String DISTINCT = "distinct";
    public static final String SUM = "sum";
    public static final String AVG = "avg";
    public static final String MAX = "max";
    public static final String LAST = "last";
    public static final String RECENTLY = "recently";
    public static final String ELAPSED = "elapsed";
}
