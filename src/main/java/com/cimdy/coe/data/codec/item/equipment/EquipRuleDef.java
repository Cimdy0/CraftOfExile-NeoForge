package com.cimdy.coe.data.codec.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import com.cimdy.coe.data.helper.engine.EquipRules;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

/**
 * <b>装备 / 持握规则</b>定义（数据包驱动）。
 *
 * <p>一条规则 = 一次「约束」的声明：<b>某类物品</b>在<b>某槽位</b>上，与<b>另一手</b>或与<b>槽位占用</b>
 * 之间的一次互斥关系。求值引擎见 {@code com.cimdy.coe.data.helper.engine.EquipRules}。
 *
 * <p><b>设计约束：本类（及引擎）内不含任何物品 / 武器类别字面量。</b>
 * 全部类别身份一律由 <b>类型键</b>（{@code ModifiableState.types()} 中的叶类型键，如 {@code sceptre} /
 * {@code talisman} / {@code two_hand}，见 {@code EquipmentGen}）承载，规则实例由数据包 JSON 提供；
 * Java 只做「解析 + 求值」。因此未来新增
 * 「任意物品类别 × 任意槽位 × 任意另一手条件」的约束，都是<b>加一个数据文件</b>的事，零 Java 改动。
 * 类别判定统一走 {@code ItemTypeChecker}，不再依赖任何 item tag。</p>
 *
 * <p><b>字段语义</b>
 * <ul>
 *   <li>{@code type}：受本规则约束的物品类型键（必填，{@code ModifiableState.types()} 中的叶类型键）。</li>
 *   <li>{@code require_all_types}：附加类别条件，须<b>全部</b>命中（如「双手 且 猛击」）。</li>
 *   <li>{@code any_of_types}：附加类别条件，<b>任一</b>命中即可。</li>
 *   <li>{@code slot}：约束作用的槽位（{@code mainhand} / {@code offhand} / {@code accessory} 或任意自定义
 *       槽位上下文名）；{@code any} = 不筛槽位。</li>
 *   <li>{@code conflict_other_hand_type}：另一手持该类型物品 → 本物品被顶掉。</li>
 *   <li>{@code requires_other_hand_empty}：另一手非空 → 本物品被顶掉。</li>
 *   <li>{@code occupies_both_hands}：本物品占双槽。语义<b>只作用于另一手</b>——本物品永不被顶，
 *       但另一手的任何物品会被顶掉（保证「双手武器始终可用、副手被抑制」这一确定顺序）。
 *       豁免同一 stat 通道。</li>
 *   <li>{@code apply_only_if_any_types} / {@@code exempt_if_any_types}：<b>稀有度 / 词缀</b>等
 *       任意附加维度的可选门闩——前者「仅当物品命中其中之一时规则才适用」，后者「物品命中其中之一则豁免」。
 *       二者默认为空（= 不启用该维度）。</li>
 * </ul>
 *
 * <p><b>关于「稀有度 / 词缀」维度</b>：语料里存在 {@code non-Item Rarity} 这类修饰，本轮<b>不臆造</b>其
 * 语义（见 {@code EquipRules} 注释）。维度已以上述两个可选字段预留：一旦内容侧给出
 * 「稀有度 / 词缀」的类型键，填这两个字段即可闭合该维度，Java 无需改动。</p>
 */
public record EquipRuleDef(
        String type,
        List<String> requireAllTypes,
        List<String> anyOfTypes,
        List<String> applyOnlyIfAnyTypes,
        List<String> exemptIfAnyTypes,
        String slot,
        Optional<String> conflictOtherHandType,
        boolean requiresOtherHandEmpty,
        boolean occupiesBothHands
) {

    /** 槽位通配符：不筛槽位。 */
    public static final String ANY_SLOT = "any";

    public static final Codec<EquipRuleDef> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("type").forGetter(EquipRuleDef::type),
            Codec.STRING.listOf().optionalFieldOf("require_all_types", List.of()).forGetter(EquipRuleDef::requireAllTypes),
            Codec.STRING.listOf().optionalFieldOf("any_of_types", List.of()).forGetter(EquipRuleDef::anyOfTypes),
            Codec.STRING.listOf().optionalFieldOf("apply_only_if_any_types", List.of()).forGetter(EquipRuleDef::applyOnlyIfAnyTypes),
            Codec.STRING.listOf().optionalFieldOf("exempt_if_any_types", List.of()).forGetter(EquipRuleDef::exemptIfAnyTypes),
            Codec.STRING.optionalFieldOf("slot", ANY_SLOT).forGetter(EquipRuleDef::slot),
            Codec.STRING.optionalFieldOf("conflict_other_hand_type").forGetter(EquipRuleDef::conflictOtherHandType),
            Codec.BOOL.optionalFieldOf("requires_other_hand_empty", false).forGetter(EquipRuleDef::requiresOtherHandEmpty),
            Codec.BOOL.optionalFieldOf("occupies_both_hands", false).forGetter(EquipRuleDef::occupiesBothHands)
    ).apply(in, EquipRuleDef::new));

    /** 便捷构造：仅「受约束物品类型 + 另一手冲突类型」，其余取默认（作用全部槽位）。 */
    public static EquipRuleDef otherHandConflict(String type, String conflictOtherHandType) {
        return new EquipRuleDef(type, List.of(), List.of(), List.of(), List.of(), ANY_SLOT,
                Optional.of(conflictOtherHandType), false, false);
    }

    /** 便捷构造：仅「占双槽」，其余取默认（作用全部槽位）。 */
    public static EquipRuleDef occupiesBothHands(String type, List<String> anyOfTypes) {
        return new EquipRuleDef(type, List.of(), anyOfTypes, List.of(), List.of(), ANY_SLOT,
                Optional.empty(), false, true);
    }
}
