package com.cimdy.coe.data.codec.support.logic;

/**
 * 叶子判定器 —— {@link LogicalExpr} 组合机制中<b>唯一维度特有</b>的部分。
 *
 * <p>「与 / 或 / 非」的组合逻辑对所有维度都是同一套（见 {@link LogicalExpr}）；真正的差异只在于
 * <b>「一个键命中载体」意味着什么</b>：
 * <ul>
 *   <li>Stat 标签维度：载体是标签集合，键命中 = 集合含该标签、或其父类别下有任一子标签命中
 *       （{@code elemental} 辖 {@code fire}），并额外支持 {@code proj:} 前缀通配；</li>
 *   <li>装备部位维度：载体是 {@code ModifiableState}，键命中 = {@code ItemTypeChecker.hasType}
 *       经<b>族别名</b>（{@code sword} → 单/双手剑）与<b>祖先类别</b>展开后命中；
 *       路径型键（{@code armor/chest}）则每段都必须落在（叶 ∪ 祖先）中——即「递归子目录」语义。</li>
 * </ul>
 * 组合算子只负责把若干叶子按 与 / 或 / 非 合成，故叶子判定由此接口单独承担。
 *
 * <p><b>适用范围</b>：这里判定的是<b>归属</b>——「本载体算不算这一类」（载体的标签属于「元素」这一族、
 * 物品属于「胸甲」这一支）。它<b>不判定因果关系</b>：「火能造成灼烧」不是「火」的归属问题，
 * 不该由叶子的层级展开来表达，否则「元素」会被「火的下位功能」反向污染。
 *
 * @param <C> 求值上下文（载体）
 */
@FunctionalInterface
public interface LeafMatcher<C> {

    /**
     * 载体是否命中给定键。
     *
     * @param carrier 求值上下文（载体）
     * @param key     叶子键：单键（{@code fire}）或路径（{@code armor/chest}）
     */
    boolean matches(C carrier, String key);
}
