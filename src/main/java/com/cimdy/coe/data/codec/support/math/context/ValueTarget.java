package com.cimdy.coe.data.codec.support.math.context;

/**
 * 实体域绑定目标（{@link BindExpr} 的 target）：决定 inner 在哪个实体作用域上求值。
 * <p>取代原 SourceScopeNode / CandidateScopeNode 两个独立类，单一枚举 + 单一组合子覆盖全部「换实体」语义（强复用、零双轨）。</p>
 */
public enum ValueTarget {
    /** 当前参照实体（默认，等价于不包装）。 */
    SELF,
    /** 交互对方（命中结算中的对侧实体，{@code StatContext.sourceEntity}）。 */
    SOURCE,
    /** filter / sort 表达式中的被判断对象（候选）。 */
    CANDIDATE,
    /** 命名实体槽位（如 hit / hitter，由 DamagePipeline 注入）。 */
    SLOT
}
