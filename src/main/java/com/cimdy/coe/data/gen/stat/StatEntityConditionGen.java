package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import net.minecraft.resources.Identifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stat condition 子条目内容注册表 —— <b>实体获取条件</b>一侧（CODEC 工厂：JSON 经
 * {@link PresenceCondition} / {@link RelationCondition} 构造器直接构造真实对象，
 * Provider 编码落盘）。
 *
 * <p>与 {@link StatEffectGen} / {@link StatTargetGen} 同构：内容表在此、落盘在 Provider、
 * 身份取 {@link StatConditionIds} 的具名常量，全程 name-keyed 引用，不做内容寻址。
 *
 * <p><b>与 {@link StatConditionGen} 的分工</b>：{@code StatConditionGen} 产出<b>谓词</b>
 * （{@code List<ValueExpr>}，机制 B —— 进条目的 {@code conditions} 字段，直接内联展开）；
 * 本类产出<b>实体获取条件</b>（{@link StatCondition} 对象，机制 A —— 进条目的
 * {@code "condition": <id>} 字段，运行期由 {@code LoadEvent} 扫描 {@code stat/condition}
 * 目录解析）。两者正交，不可互相替代。
 *
 * <p><b>完备性</b>：两个枚举全域展开（{@code PresenceCountValue.Relation}
 * ANY/ALLY/ENEMY、{@code RelationCondition.Relation} MINION/TOTEM/COMPANION/ALLY/OWNED_BY），
 * 共 {@code 3 + 5} 条；枚举新增取值时由 {@link StatConditionIds#presenceId} /
 * {@link #relationId} 的无 {@code default} 穷尽 switch 在编译期暴露。
 */
public final class StatEntityConditionGen {
    private StatEntityConditionGen() {}

    /**
     * 具名实体获取条件内容表（全部 {@code ConditionAnchor.SELF} —— 锚点固定为持有者自身，
     * 接线层从无别的锚点诉求，故不按锚点再分维）。
     */
    public static Map<Identifier, StatCondition> contents() {
        Map<Identifier, StatCondition> m = new LinkedHashMap<>();
        for (PresenceCountValue.Relation camp : PresenceCountValue.Relation.values()) {
            m.put(StatConditionIds.presenceId(camp),
                    PresenceCondition.of(ConditionAnchor.SELF, camp));
        }
        for (RelationCondition.Relation relation : RelationCondition.Relation.values()) {
            m.put(relationId(relation),
                    RelationCondition.of(ConditionAnchor.SELF, relation));
        }
        return m;
    }

    /**
     * Relation 条件枚举 → 具名 id。穷尽 switch 无 {@code default}：枚举加值即编译失败，
     * 杜绝「新增取值忘了登记名字、运行期才抛未注册」。
     */
    public static Identifier relationId(RelationCondition.Relation relation) {
        return switch (relation) {
            case MINION -> StatConditionIds.CAC_RELATION_SELF_MINION;
            case TOTEM -> StatConditionIds.CAC_RELATION_SELF_TOTEM;
            case COMPANION -> StatConditionIds.CAC_RELATION_SELF_COMPANION;
            case ALLY -> StatConditionIds.CAC_RELATION_SELF_ALLY;
            case OWNED_BY -> StatConditionIds.CAC_RELATION_SELF_OWNED_BY;
        };
    }

    /**
     * 具名实体获取条件的<b>对象解析</b>（供接线层需要对象形态的场合；引用形态走
     * {@code StatRead#withConditionRef}）。未注册即抛错（fail-fast）。
     */
    public static StatCondition condition(Identifier id) {
        StatCondition c = contents().get(id);
        if (c == null) {
            throw new IllegalStateException("未注册的 stat condition 具名 id: " + id);
        }
        return c;
    }
}