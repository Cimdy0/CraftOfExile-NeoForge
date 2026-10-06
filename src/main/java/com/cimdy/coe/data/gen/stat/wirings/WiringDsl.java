package com.cimdy.coe.data.gen.stat.wirings;

import com.cimdy.coe.data.gen.stat.StatGen;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.gen.stat.StatConditionGen;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.StatEntityConditionGen;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.gen.stat.StatTargetGen;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import com.cimdy.coe.data.codec.support.stat.StatIds;

/**
 * 接线 DSL：把 StatGen 里最高频的组装形状收成工厂方法。
 * 分布实测：881 条接线中 TGT_SELF_ONLY 占 740（84%）、TGT_COND_ON_TARGET 占 69，
 * 故 self(...) / onTarget(...) 覆盖绝大多数写法，省去重复的 StatTargetIds 限定名。
 */
public final class WiringDsl {

    private WiringDsl() {
    }

    /** 纯路径组装（无条件）。 */
    public static StatRead stat(Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(), targetsRef, effectRef);
    }

    /** 纯路径组装（单条件）。 */
    public static StatRead stat(Identifier condRef, Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(condRef), targetsRef, effectRef);
    }

    /** 纯路径组装（多条件）：具名 id 在此一次性解成真实部件对象（内容寻址已移除，全程内联）。 */
    public static StatRead stat(List<Identifier> condRefs, Identifier targetsRef, Identifier effectRef) {
        List<ValueExpr> conds = condRefs.isEmpty() ? List.of()
                : condRefs.stream().flatMap(c -> StatConditionGen.conditions(c).stream()).toList();
        return new StatRead(ExpressionCodec.VALUE, conds, StatTargetGen.targets(targetsRef),
                StatEffectGen.effect(effectRef));
    }

    /** 由 Condition 获取持有者 Presence 内的阵营集合，Target 保持基础 SELF。 */
    public static StatRead presence(PresenceCountValue.Relation camp, Identifier effectRef) {
        return stat(StatTargetIds.TGT_SELF_ONLY, effectRef)
                .withConditionRef(StatConditionIds.presenceId(camp));
    }

    /** Presence 获取 + 既有谓词过滤。 */
    public static StatRead presence(List<Identifier> condRefs, PresenceCountValue.Relation camp,
                                Identifier effectRef) {
        return stat(condRefs, StatTargetIds.TGT_SELF_ONLY, effectRef)
                .withConditionRef(StatConditionIds.presenceId(camp));
    }

    /** 由 Condition 获取持有者的归属实体集合，Target 保持基础 SELF。 */
    public static StatRead relation(RelationCondition.Relation relation, Identifier effectRef) {
        return stat(StatTargetIds.TGT_SELF_ONLY, effectRef)
                .withConditionRef(StatEntityConditionGen.relationId(relation));
    }

    // ---- 以下快捷方式全部经由 StatBuilder：「以 effect 起手 → 叠加 target/condition → build」----
    // 保留签名不变，故既有 881 条接线无需改写即可获得建造者语义。

    /** 自身生效（TGT_SELF_ONLY）。 */
    public static StatRead self(Identifier effectRef) {
        return StatBuilder.effect(effectRef).self().build();
    }

    /** 自身生效 + 单条件。 */
    public static StatRead self(Identifier condRef, Identifier effectRef) {
        return StatBuilder.effect(effectRef).self().when(condRef).build();
    }

    /** 对目标生效（TGT_COND_ON_TARGET）：条件在目标侧求值。 */
    public static StatRead onTarget(Identifier condRef, Identifier effectRef) {
        return StatBuilder.effect(effectRef).onTarget().when(condRef).build();
    }

    /**
     * 建造者入口：以 effect 起手，供「同 effect 派生多条件变体」的场景链式使用。
     * <pre>{@code
     * var dmg = wiring(EFF_DAMAGE).self();
     * map.put(StatIds.INC_DAMAGE,               dmg.build());
     * map.put(StatIds.INC_DAMAGE_WHILE_IGNITED, dmg.when(COND_IGNITE_STATUS).build());
     * }</pre>
     */
    public static StatBuilder wiring(Identifier effectRef) {
        return StatBuilder.effect(effectRef);
    }

    // ==================== 组合条目（PoE hybrid 词缀）====================
    // 语义 = 「这一条 = 这些成员一起生效」，共享同一 amount（各成员均绑 $value）。
    // 用于三向/双向组合且无专用合并 EFF 的情形（如 Armour+Evasion+Energy Shield、
    // Attack+Cast Speed、Max Health+Mana+ES、To all Attributes）。
    // 已有专用合并 EFF 的（如 ARMOR+EVASION 的 EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR）
    // 应直接复用该 EFF，不要拆成 group。

    /** 通用组合：直接给成员。 */
    public static StatGroup group(StatEntry... members) {
        return new StatGroup(List.of(members));
    }

    /** 自身生效的组合条目（无条件）：每个 effect 一个成员。 */
    public static StatGroup groupSelf(Identifier... effectRefs) {
        List<StatEntry> ms = new ArrayList<>();
        for (Identifier e : effectRefs) ms.add(stat(StatTargetIds.TGT_SELF_ONLY, e));
        return new StatGroup(ms);
    }

    /** 自身生效的组合条目（单条件）：条件对全部成员生效。 */
    public static StatGroup groupSelfWhen(Identifier condRef, Identifier... effectRefs) {
        List<StatEntry> ms = new ArrayList<>();
        for (Identifier e : effectRefs) ms.add(stat(condRef, StatTargetIds.TGT_SELF_ONLY, e));
        return new StatGroup(ms);
    }
}
