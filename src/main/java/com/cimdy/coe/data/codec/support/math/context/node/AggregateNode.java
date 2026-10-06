package com.cimdy.coe.data.codec.support.math.context.node;

/**
 * 多候选聚合节点标记 —— 与 {@link EntityNode} 平行。
 * <p>
 * 「单候选」语义（读 {@code scope.current()} 一个实体）与「聚合」语义（遍历一个集合）
 * 是两回事，不应塞进同一个接口。原先聚合节点被迫 {@code implements EntityNode} 后
 * 只能假装忽略单候选参数（见 {@code PresenceRarityValue} 的历史），本接口用于纠正这一点。
 * <p>
 * 与 {@link ValueExpr} 一样本接口暂不声明 {@code evaluate} 的具体形态，仅作类型标记；
 * 聚合语义的具体求值契约在后续切片中补充。
 */
public interface AggregateNode extends ValueExpr {}