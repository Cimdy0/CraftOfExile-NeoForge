package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 桥：把既有 {@link ValueExpr} <b>谓词</b>提升为新模型中的 {@code filter} 节点。
 * <p>
 * 既有的 ~50 个值节点（"被你点燃" / "生命低于 30%" / "稀有度为传奇" / "最近被击中" …）
 * 与 486 条 datagen 条件<b>一个都不重写</b>：它们在旧模型里是谓词（{@code evaluate != 0} 即真），
 * 在新模型里作为 {@link FilterCondition} 的内脏 —— 「某种判断下的实体」= 入参集中谓词成立的那些。
 *
 * <p>这是<b>能力复用</b>：谓词层（{@code ValueExpr}）与获取层（{@link StatCondition}）分工不变，
 * 不是为兼容旧数据而设的平行系统。
 */
public final class ConditionBridge {
    private ConditionBridge() {}

    /**
     * 单谓词 → 过滤节点（作用于入参集 {@link ConditionScope#base()}）。
     * <p>
     * 例：{@code filter(new IgnitedBySourceValue())} = 「入参集中被你点燃的那些」。
     */
    public static FilterCondition filter(ValueExpr predicate) {
        return FilterCondition.of(predicate);
    }

    /** 多谓词（逻辑与）→ 过滤节点：复用 {@link ValueExpr#of(List)} 压成单个谓词节点。 */
    public static FilterCondition filter(List<ValueExpr> predicates) {
        return FilterCondition.of(ValueExpr.of(predicates));
    }

    /** 谓词 + 入参来源 → 过滤节点。 */
    public static FilterCondition filter(ValueExpr predicate, StatCondition of) {
        return FilterCondition.of(predicate, of);
    }

    /** 谓词 + 入参来源 → 过滤节点（多谓词版本）。 */
    public static FilterCondition filter(List<ValueExpr> predicates, StatCondition of) {
        return FilterCondition.of(ValueExpr.of(predicates), of);
    }

    /**
     * 谓词 → 条件节点（与 {@link #filter(ValueExpr)} 同义，读起来更贴近「把条件搬进新模型」）。
     */
    public static StatCondition of(ValueExpr predicate) {
        return filter(predicate);
    }

    /** 谓词列表 → 条件节点。 */
    public static StatCondition of(List<ValueExpr> predicates) {
        return filter(predicates);
    }

    /**
     * 便捷判定：给定作用域，{@code condition} 是否解析出非空集合。
     * <p>
     * 供 Phase 2 迁移期「布尔式用法」临时收口 —— 注意模型本身不产布尔，
     * 真正的判定是「集合非空」；效果作用于集合本身而非先判定再取目标。
     */
    public static boolean any(StatCondition condition, ConditionScope scope) {
        if (condition == null) return false;
        List<LivingEntity> resolved = condition.resolve(scope);
        return resolved != null && !resolved.isEmpty();
    }
}
