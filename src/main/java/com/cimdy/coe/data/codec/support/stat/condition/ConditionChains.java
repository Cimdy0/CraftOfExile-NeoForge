package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.IgnitedBySourceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;

import java.util.Optional;

/**
 * <b>验收链</b>：项目主给出的规范示例在模型中的精确组合形状。
 * <p>
 * 「你杀死的那个敌人，且该敌人是被你点燃的，把过量伤害作为点燃扩散给<b>它</b>附近的敌人」：
 * <pre>
 * on_kill 事件
 *   → Target: 取 B（被杀实体）                 → anchor(killed)
 *   → Condition 层 1: 用「被你点燃」测试 B      → filter{of: anchor(killed), where: ignited_by_source}
 *   → Condition 层 2（递归/多跳）: 以 B 为锚点，
 *       做范围实体获取 + 敌方测试                → chain{then: radius(center=current, camp=enemy)}
 *   → Effect 落在解析出的集合上（B 附近的敌人）
 * </pre>
 *
 * <p>对应 JSON 数据形状（可直接落盘，由 {@code StatCondition.CODEC} 递归解析）：
 * <pre>
 * {
 *   "type": "chain",
 *   "inner": {
 *     "type": "filter",
 *     "of": { "type": "anchor", "anchor": "killed" },
 *     "where": { "type": "ignited_by_source" }
 *   },
 *   "then": {
 *     "type": "radius",
 *     "radius": 2.5,
 *     "camp": "enemy",
 *     "center": "current"
 *   }
 * }
 * </pre>
 *
 * <p>该形状此前只能硬编码（{@code KillBurstHelper} 的半径 2.5 / 10% 最大生命 / 火类型），
 * 原因是 Condition 不可递归、且没有「本次被杀实体」这一事件对象槽 —— 二者已由
 * {@link ChainCondition}（{@code Codec.recursive} 多跳）与 {@link ConditionScope#eventEntity()} 补齐。
 */
public final class ConditionChains {
    private ConditionChains() {}

    /**
     * 验收链（对象形状）：被你杀死且被你点燃的敌人 → 以它为锚点 → 它附近的敌方集合。
     *
     * @param radius 扩散半径（原 {@code KillBurstHelper} 的硬编码 2.5 现为数据）
     */
    public static StatCondition ignitedKillSpreadToNearbyEnemies(double radius) {
        StatCondition killedAndIgnitedByYou = new FilterCondition(
                new IgnitedBySourceValue(),
                Optional.of(new AnchorCondition(ConditionAnchor.KILLED)));
        return new ChainCondition(
                killedAndIgnitedByYou,
                RadiusCondition.aroundCurrent(radius, PresenceCountValue.Relation.ENEMY));
    }

    /**
     * 验收链的「范围获取 + 显式敌方测试」拆分形状：半径先取范围（不判阵营），
     * 再由 {@code filter} 以谓词做敌方测试 —— 与项目主给出的层次逐层对应：
     * <pre>
     * chain{ inner: filter{of: anchor(killed), where: ignited_by_source},
     *        then:  filter{of: radius(center=current), where: enemyPredicate} }
     * </pre>
     *
     * @param radius         扩散半径
     * @param enemyPredicate 敌方判定谓词（复用既有 {@link ValueExpr} 谓词层）
     */
    public static StatCondition ignitedKillSpreadToNearbyEnemies(double radius, ValueExpr enemyPredicate) {
        StatCondition killedAndIgnitedByYou = new FilterCondition(
                new IgnitedBySourceValue(),
                Optional.of(new AnchorCondition(ConditionAnchor.KILLED)));
        StatCondition nearby = new FilterCondition(
                enemyPredicate,
                Optional.of(RadiusCondition.aroundCurrent(radius, PresenceCountValue.Relation.ANY)));
        return new ChainCondition(killedAndIgnitedByYou, nearby);
    }

    /** 通用「锚点 → 范围」一跳（{@code anchor → radius}），供 datagen / 迁移期复用。 */
    public static StatCondition aroundAnchor(ConditionAnchor anchor, double radius,
                                             PresenceCountValue.Relation camp) {
        return new ChainCondition(
                new AnchorCondition(anchor),
                RadiusCondition.around(ConditionAnchor.CURRENT, radius, camp));
    }

    /** 表达式型半径版本的验收链（半径由数值表达式给出，而非字面量）。 */
    public static StatCondition ignitedKillSpreadToNearbyEnemies(ExpressionCodec.Value radius) {
        StatCondition killedAndIgnitedByYou = new FilterCondition(
                new IgnitedBySourceValue(),
                Optional.of(new AnchorCondition(ConditionAnchor.KILLED)));
        return new ChainCondition(
                killedAndIgnitedByYou,
                new RadiusCondition(radius, PresenceCountValue.Relation.ENEMY,
                        Optional.of(ConditionAnchor.CURRENT)));
    }
}
