package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.event.Event;

/**
 * 事件域读值节点类别（节点分类）：作用于当前候选实体的近期事件 / 坐标历史（{@link Event.State}）。
 * <p>
 * 统一收敛「解析事件状态」样板：数据源经 {@link ValueScope#event()} 归一
 * （候选被交换时读候选自身事件历史，回落 root 缓存），实现类只写
 * {@link #evaluate(Event.State, ValueScope)}。
 * <p>
 * 家族成员：移动状态 / 近期事件 / 事件计数器 / 事件计时（event 子包）。
 */
public interface EventNode extends ValueExpr {
    @Override
    default double evaluate(ValueScope scope) {
        return evaluate(scope.event(), scope);
    }

    /** 在解析出的事件状态上读值。 */
    double evaluate(Event.State state, ValueScope scope);
}
