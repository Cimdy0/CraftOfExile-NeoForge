package com.cimdy.coe.data.gen.stat.wirings.wire.flow;

import com.cimdy.coe.data.gen.stat.wirings.StatBuilder;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.GemAggregates;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * WireSocket 域接线：「if you have at least N <color> Support Gems Socketed」镶嵌门控簇。
 *
 * <p><b>条件</b>：{@code COND_SOCKET_RED_SUPPORT / COND_SOCKET_BLUE_SUPPORT /
 * COND_SOCKET_GREEN_SUPPORT} = {@code socket:<color>.support >= socket_n}。
 * 读源 {@code GemValue} 的 <b>count 聚合</b>（{@code GemAggregates.COUNT}）：
 * 遍历玩家装配槽，按颜色（gem id 首段）与种类（{@code support} 只数辅助位）计数，非玩家返回 0。
 *
 * <p><b>阈值参数化</b>：语料阈值为 5（抗性上限三色）与 10（最大生命/魔力/移速）两组，
 * 故沿用<b>距离簇范式</b>——阈值不硬编码进 COND，而由条目 {@code param("socket_n", N)}
 * 注入、COND 内 {@code VarRef("socket_n")} 读取，一个 COND 服务多组阈值。
 *
 * <p><b>颜色 ↔ 元素对应以语料为准</b>（不得凭 PoE1 习惯臆断——R3）：
 * 语料明写 Blue→Cold、Red→Fire、Green→Lightning（见各条下方注释所引原文）。
 *
 * <p><b>多条件 AND</b>：三色条目用 {@code when(Identifier...)} 可变参叠加，
 * 三个 COND 共读同一个 {@code socket_n} 参数，故只注入一次。
 */
public final class WireSocket {

    private WireSocket() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== 最大元素抗性上限（阈值 5）=====================
        // 「+2% to Maximum Cold Resistance if you have at least 5 Blue Support Gems Socketed」
        map.put(StatIds.TO_MAX_COLD_RESISTANCE_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD)
                        .self().when(StatConditionIds.COND_SOCKET_BLUE_SUPPORT)
                        .param("socket_n", 5.0).build());

        // 「+2% to Maximum Fire Resistance if you have at least 5 Red Support Gems Socketed」
        map.put(StatIds.TO_MAX_FIRE_RESISTANCE_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE)
                        .self().when(StatConditionIds.COND_SOCKET_RED_SUPPORT)
                        .param("socket_n", 5.0).build());

        // 「+2% to Maximum Lightning Resistance if you have at least 5 Green Support Gems Socketed」
        map.put(StatIds.TO_MAX_LIGHTNING_RESISTANCE_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING)
                        .self().when(StatConditionIds.COND_SOCKET_GREEN_SUPPORT)
                        .param("socket_n", 5.0).build());

        // 「+1% to all Maximum Elemental Resistances if you have at least 5 Red, Green and Blue Support Gems Socketed」
        // 三色须同时满足 → 三个 COND 叠加（AND）。无元素标签的 MAX_RESISTANCES 即「全元素」。
        map.put(StatIds.TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES)
                        .self().when(StatConditionIds.COND_SOCKET_RED_SUPPORT,
                                StatConditionIds.COND_SOCKET_GREEN_SUPPORT,
                                StatConditionIds.COND_SOCKET_BLUE_SUPPORT)
                        .param("socket_n", 5.0).build());

        // ===================== 上限类（阈值 10）=====================

        // 「5% increased Maximum Life if you have at least 10 Red Support Gems Socketed」
        map.put(StatIds.INC_MAX_HEALTH_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH)
                        .self().when(StatConditionIds.COND_SOCKET_RED_SUPPORT)
                        .param("socket_n", 10.0).build());

        // 「5% increased Maximum Mana if you have at least 10 Blue Support Gems Socketed」
        map.put(StatIds.INC_MAX_MANA_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_MAX_MANA)
                        .self().when(StatConditionIds.COND_SOCKET_BLUE_SUPPORT)
                        .param("socket_n", 10.0).build());

        // 「5% increased Movement Speed if you have at least 10 Green Support Gems Socketed」
        map.put(StatIds.INC_MOVEMENT_SPEED_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED)
                        .self().when(StatConditionIds.COND_SOCKET_GREEN_SUPPORT)
                        .param("socket_n", 10.0).build());
    }
}
