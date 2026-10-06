package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * <b>Condition = 「获取某种判断下的实体」，而不是「做一个布尔判断」</b>（项目主裁定）。
 * <p>
 * 单一契约 {@link #resolve}：<b>返回该判断下的实体集</b>；空列表 = 不满足。
 * 与既有 {@link com.cimdy.coe.data.codec.support.math.context.node.ValueExpr}
 * （{@code double evaluate(scope)}，谓词：非 0 即真）分层：
 * <ul>
 *   <li>{@code ValueExpr} —— 单实体的<b>谓词</b>（"被你点燃" / "生命低于 30%" …），保留原职；</li>
 *   <li>{@code StatCondition} —— 实体的<b>获取</b>（"被你杀死且被你点燃的那个敌人附近的敌人" …），本次新增。</li>
 * </ul>
 * 二者不是平行系统：{@link FilterCondition} 直接把 {@code ValueExpr} 谓词作为自己的内脏
 * （能力复用，不是兼容 shim），故既有 55 键 / 30+ 实现类与 486 条 datagen 条件全部可作为过滤器复用。
 *
 * <p><b>递归</b>：节点嵌套节点（{@link AndCondition} / {@link OrCondition} / {@link NotCondition} /
 * {@link ChainCondition} 的子字段经 {@code Codec.recursive} 真正嵌套）。
 * <b>多态 + 数据驱动</b>：JSON {@code type} 键 → {@link StatConditionTypes} 注册表分派，
 * 新增一种判断 = 一个实现类 + 一行 register。
 *
 * <p>典型组合（验收链，见 {@link ConditionChains}）：
 * <pre>
 * on_kill → anchor(killed) → filter(被你点燃) → chain → radius(锚点=当前元素, 敌方)
 * </pre>
 */
public interface StatCondition {

    /** 数据驱动分派 codec：按 JSON {@code type} 字段 dispatch 到各节点 CODEC（递归）。 */
    Codec<StatCondition> CODEC = StatConditionTypes.CODEC;

    /**
     * 解析出「本判断下的实体集」。空列表 = 不满足（调用方据此短路，无需额外布尔层）。
     * <p>
     * 节点分两类：
     * <ul>
     *   <li><b>生成型</b>（{@link AnchorCondition} / {@link RadiusCondition} / {@link PresenceCondition} /
     *       {@link RelationCondition}）—— 按作用域从世界 / 归属关系生成集合，忽略入参集；</li>
     *   <li><b>变换型</b>（{@link FilterCondition} / {@link NotCondition}）—— 作用于入参集
     *       （{@link ConditionScope#base()}，可由 {@code of} 子条件产出）。</li>
     * </ul>
     */
    List<LivingEntity> resolve(ConditionScope scope);
}
