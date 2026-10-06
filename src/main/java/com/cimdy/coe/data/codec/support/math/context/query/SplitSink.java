package com.cimdy.coe.data.codec.support.math.context.query;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.SelectorNode;
import net.minecraft.world.entity.LivingEntity;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.OutputMode;

/**
 * 求值期一对一拆分发射通道：由消费端（如 stat 触发效果）在求值前注入 {@link ValueScope}，
 * 选择器节点（{@code SelectorNode} 的 {@link OutputMode#EACH} 模式）对每个候选在其作用域内
 * 求出值后逐对发射 {@code (实体, 值)}——保留「哪个值属于哪个实体」的对应关系，
 * 使效果施加阶段能对每个求值实体单独作用（一对一），而非只拿到折叠后的总和。
 * <p>
 * 未注入 sink 时 EACH 退化为 SUM 语义（标量返回值恒为各候选值之和），旧调用方不受影响。
 */
@FunctionalInterface
public interface SplitSink {
    /** 发射一对「被求值实体 → 该实体作用域内求出的值」。 */
    void emit(LivingEntity entity, double value);
}
