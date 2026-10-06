package com.cimdy.coe.data.codec.support.logic;

import java.util.List;

/**
 * 布尔谓词表达式 —— 「与 / 或 / 非」组合机制的通用抽象。
 *
 * <h2>剥离了什么</h2>
 * 「结论由若干子谓词经 与 / 或 / 非 合成」是一套<b>与载体无关</b>的组合逻辑：
 * 与 = 全部成立、或 = 任一成立、非 = 取反、空「与」= 恒真（无约束）。
 * 这套逻辑曾在三个维度上被各自实现一遍：
 * <ul>
 *   <li>{@code GateTag.match}——扁平集合形态（用 {@code !} 前缀表达非、用父标签辖子表达或）；</li>
 *   <li>{@code GateExpr}——文本 DSL 形态（{@code a|b, !c}）；</li>
 *   <li>{@code SlotExpr}——JSON 对象形态（{@code {"any":[…]}}）。</li>
 * </ul>
 * 本接口把该机制收口为一处定义；各维度<b>只保留叶子判定</b>（维度特有，无法通用：
 * Stat 侧查标签集合与 {@code proj:} 通配，部位侧查 {@code ItemTypeChecker} 的族别名与祖先展开）。
 *
 * <h2>复用的两种方式</h2>
 * <ul>
 *   <li><b>直接实现</b>：本类同时提供 {@code Always / All / Any / Not} 四个泛型算子 record
 *       与 {@link #all}/{@link #any}/{@link #not}/{@link #always} 工厂，供无需独立 sealed 层级的
 *       新维度直接复用；这些算子同时就是 {@link LogicalExprs#codec(LeafMatcher)} 描述的
 *       那棵树，故新维度天然获得「递归 JSON」这一数据驱动能力。</li>
 *   <li><b>委托求值</b>：已有 sealed 层级（{@code GateExpr.Node} / {@code SlotExpr}）的组合节点
 *       保持自身类型以获得 Codec 与模式匹配能力，其 {@code matches} 改为委托
 *       {@link LogicalExprs#allOf}/{@link LogicalExprs#anyOf}——语义唯一真相源仍在本接口，
 *       且不产生额外对象分配（见这两个方法的「零分配」说明）。</li>
 * </ul>
 *
 * @param <C> 求值上下文（载体）。Stat 标签维度为 {@code Set<String>}；
 *            装备部位维度为 {@code ModifiableState}；其余维度可自行定义。
 */
public interface LogicalExpr<C> {

    // ── 组合算子（通用求值机制，与载体类型无关）──

    /** 恒真（无约束）。等价于空「与」，是「不带部位约束 / 无条件」的标准表达。 */
    record Always<C>() implements LogicalExpr<C> {
        @Override
        public boolean matches(C carrier) {
            return true;
        }
    }

    /** 与：全部子项成立才真；<b>空集恒真</b>（无约束）。 */
    record All<C>(List<LogicalExpr<C>> parts) implements LogicalExpr<C> {
        @Override
        public boolean matches(C carrier) {
            return LogicalExprs.allOf(this.parts, carrier);
        }
    }

    /** 或：任一子项成立即真；<b>空集恒假</b>（布尔代数上或的中性元是假）。 */
    record Any<C>(List<LogicalExpr<C>> parts) implements LogicalExpr<C> {
        @Override
        public boolean matches(C carrier) {
            return LogicalExprs.anyOf(this.parts, carrier);
        }
    }

    /** 非：子项取反。 */
    record Not<C>(LogicalExpr<C> part) implements LogicalExpr<C> {
        @Override
        public boolean matches(C carrier) {
            return !this.part.matches(carrier);
        }
    }

    // ── 求值 ──

    /**
     * 对给定载体求谓词。
     *
     * @param carrier 求值上下文（载体）
     */
    boolean matches(C carrier);

    // ── 构造便捷 ──

    /** 恒真（无约束）。 */
    static <C> LogicalExpr<C> always() {
        return new Always<C>();
    }

    /** 与：空集归一为 {@link #always()}。 */
    static <C> LogicalExpr<C> all(List<LogicalExpr<C>> parts) {
        return parts.isEmpty() ? always() : new All<>(List.copyOf(parts));
    }

    /** 或：空集恒假（不归一，保留「强制至少一项」的表达能力）。 */
    static <C> LogicalExpr<C> any(List<LogicalExpr<C>> parts) {
        return parts.isEmpty() ? new Any<C>(List.of()) : new Any<>(List.copyOf(parts));
    }

    /** 非。 */
    static <C> LogicalExpr<C> not(LogicalExpr<C> part) {
        return new Not<>(part);
    }

    /** 与（可变参数）。 */
    @SafeVarargs
    static <C> LogicalExpr<C> all(LogicalExpr<C>... parts) {
        return all(List.of(parts));
    }

    /** 或（可变参数）。 */
    @SafeVarargs
    static <C> LogicalExpr<C> any(LogicalExpr<C>... parts) {
        return any(List.of(parts));
    }
}
