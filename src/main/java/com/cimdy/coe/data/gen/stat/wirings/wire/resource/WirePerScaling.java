package com.cimdy.coe.data.gen.stat.wirings.wire.resource;
import com.cimdy.coe.data.gen.stat.StatEntityConditionGen;

import com.cimdy.coe.data.gen.stat.wirings.StatBuilder;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.group;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 线性缩放族接线（{@code per X} / {@code for each X} 形态）。
 *
 * <p><b>与其余 Wire* 的区别</b>——本域词条的数值<b>不是</b>单一注入量 {@code $value}，而是一条
 * <b>表达式</b>：「基准量 × 来源读值 ÷ 分母」。故统一经 {@link StatBuilder#amount(String)} 覆写
 * amount，语法沿用 math 系统（{@code MathParser}），不发明新语法：
 * <pre>{@code
 * $value * attribute:coe:strength / $per      // 每 $per 点力量给 $value
 * min($value * attribute:coe:accuracy / $per, $cap)   // 同上，但封顶 $cap
 * }</pre>
 *
 * <p><b>参数槽语义</b>（{@link StatEntry#params()}）：{@code $value} / {@code $per} / {@code $cap}
 * 的缺省值取自语料原文，实例侧（天赋节点 {@code vars} / 物品 roll）仍可覆盖，因此同一族不同
 * 比率的节点复用同一条 stat。每条接线的注释给出语料原文，便于回溯核对。
 *
 * <p><b>单位</b>：{@code ADD_MULTIPLIED_BASE} 的数值为小数比例（{@code 0.01} = 1%），与语料解析
 * 归一一致（{@code "16%"} → {@code 0.16}）；{@code ADD_VALUE} 为绝对值。
 *
 * <p><b>零基禁 inc</b>：被改属性均已核 base ≠ 0（damage=1.0 / evasion=53 /
 * minecraft:attack_speed=4.0），阈值类（{@code ailment_threshold} base=0）一律走 ADD_VALUE。
 * 作为<b>读值源</b>的属性 base 可为 0（{@code attribute:} 读的是最终计算值，由属性点/装备累加）。
 */
public final class WirePerScaling {

    private WirePerScaling() {
    }

    /** 「每 N 点来源属性给 X」的通用数值表达式。 */
    private static final String PER_SOURCE = "$value * %s / $per";

    /** 同上但封顶：语料的「up to Y%」。 */
    private static final String PER_SOURCE_CAPPED = "min($value * %s / $per, $cap)";

    private static String per(String source) {
        return String.format(PER_SOURCE, source);
    }

    private static String perCapped(String source) {
        return String.format(PER_SOURCE_CAPPED, source);
    }

    /** 「每 N 点怒气给 X」，并乘上 rage_effect（Berserk 的 increased Rage effect）。rage_effect 默认 1.0，Berserk 经 ModifierAction 设为 1.40~1.59。 */
    public static String perRage() {
        return "$value * rage * attribute:coe:rage_effect / $per";
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== 主属性缩放（力量 / 敏捷 / 智力）=====================

        // 「1% increased Damage per 15 Strength」
        map.put(StatIds.INC_DAMAGE_PER_STRENGTH,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(per("attribute:coe:strength"))
                        .param("value", 0.01)
                        .param("per", 15.0)
                        .build());

        // 「2% increased Spell Damage per 10 Strength」
        map.put(StatIds.INC_SPELL_DAMAGE_PER_STRENGTH,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL)
                        .self()
                        .amount(per("attribute:coe:strength"))
                        .param("value", 0.02)
                        .param("per", 10.0)
                        .build());

        // 「3% increased Evasion Rating per 10 Intelligence」
        map.put(StatIds.INC_EVASION_PER_INTELLIGENCE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION)
                        .self()
                        .amount(per("attribute:coe:intelligence"))
                        .param("value", 0.03)
                        .param("per", 10.0)
                        .build());

        // 「1% increased Attack Speed per 25 Dexterity」
        map.put(StatIds.INC_ATTACK_SPEED_PER_DEXTERITY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED)
                        .self()
                        .amount(per("attribute:coe:dexterity"))
                        .param("value", 0.01)
                        .param("per", 25.0)
                        .build());

        // 「+1 Life per 4 Dexterity」
        map.put(StatIds.HEALTH_PER_DEXTERITY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH)
                        .self()
                        .amount(per("attribute:coe:dexterity"))
                        .param("value", 1.0)
                        .param("per", 4.0)
                        .build());

        // 「+1 Life per 10 Strength」（力量→生命，减半率；PoE2 inherent health from Strength 的减半版）
        // 复用 HEALTH_PER_DEXTERITY 同源 EFF（ADD_VALUE → MAX_HEALTH，base=0 属性合法）。
        // 以 per=10 编码减半率（满率约为 per=5）。机制就绪待 node_stat_map 数据授节点。
        map.put(StatIds.INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH)
                        .self()
                        .amount(per("attribute:coe:strength"))
                        .param("value", 1.0)
                        .param("per", 10.0)
                        .build());

        // 「2% increased Damage per 5 of your lowest Attributes」
        // 「最低属性」= min(力量, 敏捷, 智力)，经嵌套 min 表达（MathParser 的 min 为二元）。
        map.put(StatIds.INC_DAMAGE_PER_OF_YOUR_LOWEST_ATTRIBUTES,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(per("min(min(attribute:coe:strength, attribute:coe:dexterity),"
                                + " attribute:coe:intelligence)"))
                        .param("value", 0.02)
                        .param("per", 5.0)
                        .build());

        // ===================== 阈值缩放（阈值属性 base=0，一律 ADD_VALUE）=====================

        // 「+1 to Stun Threshold per Dexterity」——每 1 点敏捷，故 $per = 1
        map.put(StatIds.TO_STUN_THRESHOLD_PER_DEXTERITY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("attribute:coe:dexterity"))
                        .param("value", 1.0)
                        .param("per", 1.0)
                        .build());

        // 「+3 to Stun Threshold per Strength」
        map.put(StatIds.TO_STUN_THRESHOLD_PER_STRENGTH,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("attribute:coe:strength"))
                        .param("value", 3.0)
                        .param("per", 1.0)
                        .build());

        // 「+4 to Ailment Threshold per Dexterity」
        map.put(StatIds.TO_AILMENT_THRESHOLD_PER_DEXTERITY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD)
                        .self()
                        .amount(per("attribute:coe:dexterity"))
                        .param("value", 4.0)
                        .param("per", 1.0)
                        .build());

        // ===================== 派生属性缩放（读值源为非属性点属性）=====================

        // 「3% increased Spell Damage per 100 maximum Mana」
        map.put(StatIds.INC_SPELL_DAMAGE_PER_MAX_MANA,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL)
                        .self()
                        .amount(per("attribute:coe:max_mana"))
                        .param("value", 0.03)
                        .param("per", 100.0)
                        .build());

        // 「1% increased Damage per 1% Chance to Block」——block_chance 为比率属性（0.01 = 1%），
        // 故分母同取比率 0.01，比值即「多少个 1%」。
        map.put(StatIds.INC_DAMAGE_PER_CHANCE_TO_BLOCK,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(per("attribute:coe:block_chance"))
                        .param("value", 0.01)
                        .param("per", 0.01)
                        .build());

        // 「1% increased Attack Speed per 400 Accuracy Rating, up to 20%」——封顶经 min 表达
        map.put(StatIds.INC_ATTACK_SPEED_PER_ACCURACY_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED)
                        .self()
                        .amount(perCapped("attribute:coe:accuracy"))
                        .param("value", 0.01)
                        .param("per", 400.0)
                        .param("cap", 0.2)
                        .build());

        // 「Attacks gain 1% increased Accuracy Rating per 1% Critical Hit Chance」——
        // crit_chance 为比率属性（registerPercentage，0.30 = 30%，见 DamageHelper.getCritChance 的 *100 换算），
        // 故分母同取比率 1.0：amount = 1.0 × crit_chance / 1.0 = crit_chance 本身，
        // 即「30% 暴击率 → +30% 提高命中值」，与 ADD_MULTIPLIED_BASE 的「+x ⇒ total ×(1+x)」口径一致。
        // 落点取无 GateExpr 的全局 ACCURACY EFF：ACCURACY 唯一读点 DamageHelper:261 只在
        // shouldCheckHitOrBlock 通过时生效，而该方法以 isAttackSkillSource 收口（法术不掷命中），
        // 故「Attacks gain」语义天然成立，不会向法术侧过度授予。
        map.put(StatIds.ATTACKS_GAIN_INC_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY)
                        .self()
                        .amount(per("attribute:coe:crit_chance"))
                        .param("value", 1.0)
                        .param("per", 1.0)
                        .build());

        // ===================== 装备读值缩放（equipment_stat:<slot>.<stat>）=====================
        // 槽位映射沿用 vanilla EquipmentSlot#getName()：boots→feet / helmet→head /
        // body_armour→chest / shield→offhand。stat 取 EquipmentState 扁平值 armor / evasion / energy。
        // 饰品栏读点（accessory_stat:<group>.<stat>，group=legs/quiver）由 AccessoryValue 提供，
        // 复用本表同一组真实读点，故下列 legs 条目不再缺源（见本文件末节「饰品栏读值缩放」）。

        // 「+2 to Armour per 1 Item Energy Shield on Equipped Boots」
        map.put(StatIds.TO_ARMOR_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_BOOTS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR)
                        .self()
                        .amount(per("equipment_stat:feet.energy"))
                        .param("value", 2.0)
                        .param("per", 1.0)
                        .build());

        // 「+1 to Evasion Rating per 1 Item Energy Shield on Equipped Helmet」
        map.put(StatIds.TO_EVASION_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_HELMET,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION)
                        .self()
                        .amount(per("equipment_stat:head.energy"))
                        .param("value", 1.0)
                        .param("per", 1.0)
                        .build());

        // 「+1 to Maximum Energy Shield per 8 Item Armour on Equipped Helmet」
        map.put(StatIds.TO_MAX_ENERGY_SHIELD_PER_ITEM_ARMOR_ON_EQUIPPED_HELMET,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("equipment_stat:head.armor"))
                        .param("value", 1.0)
                        .param("per", 8.0)
                        .build());

        // 「+1 to Maximum Energy Shield per 12 Item Evasion on Equipped Body Armour」
        map.put(StatIds.TO_MAX_ENERGY_SHIELD_PER_ITEM_EVASION_ON_EQUIPPED_BODY_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("equipment_stat:chest.evasion"))
                        .param("value", 1.0)
                        .param("per", 12.0)
                        .build());

        // 「4% increased Attack Damage per 75 Item Armour and Item Evasion on Equipped Shield」
        // 语料的「Armour and Evasion」= 两值之和，经加法表达。
        map.put(StatIds.INC_ATTACK_DAMAGE_PER_ITEM_ARMOR_AND_ITEM_EVASION_ON_EQUIPPED_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE)
                        .self()
                        .amount(per("(equipment_stat:offhand.armor + equipment_stat:offhand.evasion)"))
                        .param("value", 0.04)
                        .param("per", 75.0)
                        .build());

        // ===================== 饰品栏读值缩放（accessory_stat:<group>.<stat>）=====================
        // 读值源 = 饰品栏 legs/quiver 组物品（AccessoryValue，复用 EquipmentValue 真实读点），R3 安全、零新增死线。
        // legs/quiver 在 vanilla 无槽，现经 config/coe/slots.json 的 accessory_slots
        // 注册为饰品栏分组（无配置走内置兜底），故读点成立。

        // 「+1 to Evasion Rating per 1 Item Armour on Equipped Legs」
        // 读 legs 组物品 MODIFIABLE_STATE 的 armor（固有 12）→ 每点护甲 +1 闪避。落点 EVASION（base=0 → ADD_VALUE，合零基禁 inc）。
        map.put(StatIds.TO_EVASION_PER_ITEM_ARMOR_ON_EQUIPPED_LEGS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION)
                        .self()
                        .amount(per("accessory_stat:legs.armor"))
                        .param("value", 1.0)
                        .param("per", 1.0)
                        .build());

        // 「Gain Armour equal to 150% of total Strength Requirements of Equipped Boots, Legs and Helmet」
        // 来源 = 靴(feet)+头盔(head) 的 strength_requirement（装备读点，本文件新增输出键）
        //       + 腿甲(legs) 的 strength_requirement（饰品栏读点，复用同一输出键）。
        // 三者之和 × 150% 作为护甲 flat 加成（ADD_VALUE，base=0）。
        map.put(StatIds.GAIN_ARMOR_EQUAL_TO_OF_TOTAL_STRENGTH_REQUIREMENTS_OF_EQUIPPED_BOOTS_LEGS_AND_HELMET,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR)
                        .self()
                        .amount(per("equipment_stat:feet.strength_requirement + equipment_stat:head.strength_requirement + accessory_stat:legs.strength_requirement"))
                        .param("value", 1.5)
                        .param("per", 1.0)
                        .build());

        // 「{X}% increased bonuses gained from Equipped Quiver」= 把 X% increased 注入 INERT 标记幅度 equipment_bonus_mult（scope=quiver）
        // 由 AccessoryStatHelper.apply 在注入 quiver 组词条时按槽位分组 id 经 StatEngine.magnitudeOf 真读、对 quiver 全部 Stat 条目按 (1 + 幅度) 缩放
        // —— 通用设施：任意 accessory 类别只要存在 equipment_bonus_mult @ category 条目即被缩放，新增类别零 Java 改动（R3 安全）。
        // magnitude = $value × 1.0 / 1.0 = X%（小数，0.06 = 6%）。
        map.put(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER,
                StatBuilder.effect(StatEffectIds.EFF_TAGGED_MAGNITUDE_EQUIPMENT_BONUS_MULT)
                        .self()
                        .amount(per("1.0"))
                        .param("value", 0.01).param("per", 1.0)
                        .build());

        // 「+{X} Charm Slot」= 把 rolled 值（X）经 ADD_VALUE 累加到零基属性 charm_slot，
        // 消费端 CharmSlots.count 读 fold 取 3 + 本值（POE2 腰带「Charm Slots」属性）。
        // 幅度即 mod rolled 值，故直接走默认 amount（$value），不套 per() 缩放。
        map.put(StatIds.CHARM_SLOT,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHARM_SLOT)
                        .self()
                        .build());

        // ===================== 装备数值派生（复用既有 equipment_stat 读点）=====================
        // 复用本文件 line 171 已注册的 equipment_stat:<slot>.<stat> 读点（R3 安全：读 EquipmentState 扁平值）。
        // 目标属性 ARMOR / EVASION / MAX_ENERGY_SHIELD / AILMENT_THRESHOLD 均引擎真读（TO_* 同款 EFF 已接线验证）。
        // 数值语义：{X}% inc / Gain X from Equipped Y = 取装备该槽数值 × 比例作为对应属性 flat 加成（ADD_VALUE），
        // 与既有 TO_*（line 178-211）同源：per(source) = $value * source / $per。

        // 「{X}% increased Armour from Equipped Body Armour」= 护甲 += X% × 身体甲护甲
        map.put(StatIds.INC_ARMOR_FROM_EQUIPPED_BODY_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR)
                        .self()
                        .amount(per("equipment_stat:chest.armor"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「获得等于胸甲物品护甲 X% 的物理荆棘伤害」——镜像 WireMisc 的
        // GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED（同 EFF_DAMAGE_PHYSICAL_TAKEN_HIT +
        // 同 .self() 目标口径，仅数值源换成 equipment_stat:chest.armor）；TAKEN_HIT 事件真 fire，R3 安全，零新增读点。
        map.put(StatIds.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_BODY_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_DAMAGE_PHYSICAL_TAKEN_HIT)
                        .self()
                        .amount(per("equipment_stat:chest.armor"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「{X}% increased Evasion from Equipped Body Armour」= 闪避 += X% × 身体甲闪避
        map.put(StatIds.INC_EVASION_FROM_EQUIPPED_BODY_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION)
                        .self()
                        .amount(per("equipment_stat:chest.evasion"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「{X}% increased Energy Shield from Equipped Body Armour」= 最大 ES += X% × 身体甲能量护盾
        map.put(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_BODY_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("equipment_stat:chest.energy"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「徒手攻击视同持长杖」(StatIds.UNARMED_ATTACKS_...QUARTERSTAFF...)：一条 StatId 打包三子句 → group 三成员（各带自身 amount/条件）。
        // 子句 a：INERT 标记门控（消费端 DamageHelper#getMixedDamage 真读，双手皆空且持标记时以技能等级推导基础物理伤害）。
        //   节点无显式 $value → 烘焙 amount=1.0 供门控（>0）真读（同 TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS 先例）。
        // 子句 b：每 75 点「已装备护甲件」闪避 +1% 更多攻速（语料 node 值 1%/75）；子句 c：每 10 点「已装备护甲件」能量护盾 +0.1% 暴击率（语料 0.1%/10）。
        //   语料原文为 "Equipped Armour **Items**"（复数）→ 作用域 = 四个护甲槽之和（head+chest+legs+feet），
        //   而非单读胸甲；四槽名取自本文件既有 equipment_stat:<slot>.<stat> 读点（chest/feet/head/legs/offhand）。
        // 条件 COND_BOTH_HANDS_EMPTY = 主手与副手皆空（NodeStatMap 原文「Unarmed Attacks」）。

        // 64601 hollow_palm · 子句 a：徒手攻击（视同持长杖）基础物理伤害替换为按技能等级推导
        // 首位级效果 EFF_UNARMED_QUARTERSTAFF_SKILL_DAMAGE（UnarmedQuarterstaffSkillDamageStatEffect）：DamageHelper#getMixedDamage 经 StatEngine.hasUnarmedQuarterstaffSkillDamage 真读，
        // 双手皆空且持标记时以技能等级推导基础物理伤害；amount 烘焙为字面量 1.0 供门控（>0 真读，不 deref $value）。
        map.put(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL,
                StatBuilder.effect(StatEffectIds.EFF_UNARMED_QUARTERSTAFF_SKILL_DAMAGE).self().amount("1.0").when(StatConditionIds.COND_BOTH_HANDS_EMPTY).build());
        // 64601 hollow_palm · 子句 b：每 75 点「已装备护甲件」闪避 +1% 更多攻速（节点值 1%/75）；
        // 仅双手皆空（COND_BOTH_HANDS_EMPTY）生效；作用域 = 四护甲槽(head+chest+legs+feet)闪避之和（legs 非 vanilla 槽，按注释排除）。
        map.put(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ATTACK_SPEED_QUARTERSTAFF)
                        .self().when(StatConditionIds.COND_BOTH_HANDS_EMPTY)
                        .amount(per("equipment_stat:head.evasion + equipment_stat:chest.evasion + equipment_stat:legs.evasion + equipment_stat:feet.evasion"))
                        .param("value", 0.01).param("per", 75.0).build());
        // 64601 hollow_palm · 子句 c：每 10 点「已装备护甲件」能量护盾 +0.1% 暴击率（节点值 0.1%/10）；
        // 仅双手皆空（COND_BOTH_HANDS_EMPTY）生效；作用域 = 四护甲槽(head+chest+legs+feet)能量护盾之和。
        map.put(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_QUARTERSTAFF)
                        .self().when(StatConditionIds.COND_BOTH_HANDS_EMPTY)
                        .amount(per("equipment_stat:head.energy + equipment_stat:chest.energy + equipment_stat:legs.energy + equipment_stat:feet.energy"))
                        .param("value", 0.001).param("per", 10.0).build());

        // 「{X}% increased Energy Shield from Equipped Helmet」= 最大 ES += X% × 头盔能量护盾
        map.put(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_HELMET,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("equipment_stat:head.energy"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「{X}% increased Armour, Evasion and Energy Shield from Equipped Shield」= 三属性各 += X% × 持盾对应值
        // group(StatEntry...) 承载「同一 StatId 多 effect、各 effect 独立 per-source」组合（DSL group 支持异源成员）。
        map.put(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD,
                group(
                        StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR).self()
                                .amount(per("equipment_stat:offhand.armor")).param("value", 1.0).param("per", 1.0).build(),
                        StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION).self()
                                .amount(per("equipment_stat:offhand.evasion")).param("value", 1.0).param("per", 1.0).build(),
                        StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD).self()
                                .amount(per("equipment_stat:offhand.energy")).param("value", 1.0).param("per", 1.0).build()
                ));

        // 「{X}% increased Energy Shield from Equipped Focus」= 最大 ES += X% × 副手法器能量护盾
        // 槽位映射：focus（聚能器）与 shield 同为 PoE2 副手 → vanilla OFFHAND；读点 equipment_stat:offhand.energy
        // 与上方 INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD 的 ES 成员同源（R3 安全，零新增读点）。
        map.put(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("equipment_stat:offhand.energy"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「Gain X to Ailment Threshold equal to the lowest of Evasion and Armour on your Boots」
        map.put(StatIds.GAIN_AILMENT_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_BOOTS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD)
                        .self()
                        .amount(per("min(equipment_stat:feet.armor, equipment_stat:feet.evasion)"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「Gain X to Stun Threshold equal to the lowest of Evasion and Armour on your Helmet」
        // 沿用 TO_STUN_THRESHOLD_PER_DEXTERITY 的 EFF（AILMENT_THRESHOLD {stun} 作用域，AilmentHelper 实读）。
        map.put(StatIds.GAIN_STUN_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_HELMET,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("min(equipment_stat:head.armor, equipment_stat:head.evasion)"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // 「Gain X Additional Stun Threshold equal to #% of Item Armour on Equipped Armour Items」
        // 装甲件 = 身体甲+头盔+腿甲+靴（legs 非 vanilla 槽，模组未扩展，留待槽位扩展；见本文件 line 174 注释）。
        map.put(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("equipment_stat:chest.armor + equipment_stat:head.armor + equipment_stat:legs.armor + equipment_stat:feet.armor"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // ── 最大能量护盾派生门槛（POE2「Gain additional X Threshold equal to #% of maximum Energy Shield」）──
        // 与上两条装备派生同构：来源换成 MAX_ENERGY_SHIELD 属性，落点仍是各门槛属性的 ADD_VALUE。
        map.put(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD)
                        .self()
                        .amount(per("attribute:coe:max_energy_shield"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());
        map.put(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("attribute:coe:max_energy_shield"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());
        map.put(StatIds.GAIN_OF_MAX_ENERGY_SHIELD_AS_ADDITIONAL_FREEZE_THRESHOLD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHILL_COE_AILMENT_THRESHOLD_FREEZE)
                        .self()
                        .amount(per("attribute:coe:max_energy_shield"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());
        // 「Minions gain #% of their maximum Life as extra maximum Energy Shield」
        // 数值须按<b>随从自身</b>最大生命求值（非持有者），故显式声明 valueTarget=MINIONS：
        // 引擎在各随从读取 max_energy_shield 时把取值实体落到该随从自身（StatEngine#resolveStageEntity）。
        map.put(StatIds.MINIONS_GAIN_OF_THEIR_MAX_HEALTH_AS_EXTRA_MAX_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .valueTarget(TargetSelector.EVENT_TARGET)
                        .amount(per("attribute:minecraft:max_health"))
                        .param("value", 1.0).param("per", 1.0)
                        .build()
                        .withConditionRef(StatEntityConditionGen.relationId(RelationCondition.Relation.MINION)));
        // 「X% of Maximum Life converted to Energy Shield」——自身版本（镜像随从变体，复用同一 EFF 与 getMaxEnergyShield 读点，零新增读点）
        map.put(StatIds.OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD)
                        .self()
                        .amount(per("attribute:minecraft:max_health"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());
        // 「X% of Maximum Energy Shield converted to Maximum Mana」——镜像 OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD（同 per-scaling 范式，源换 MAX_ENERGY_SHIELD、落点换 MAX_MANA）；
        // 复用 EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_MANA + 引擎 MAX_MANA 读点，零新增读点，R3 安全。
        map.put(StatIds.STAT_CONVERSION_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_MANA)
                        .self()
                        .amount(per("attribute:coe:max_energy_shield"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());
        // 「全部闪避值转化为护甲」——镜像 OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD 的 per-scaling 范式
        // （引擎 STAT_CONVERSION 约定为「按比例加成、不扣减源」）：源换 EVASION（真读 DamageHelper:260），
        // 落点换原版 ARMOR（真读 DamagePipeline:566；base=0 故走 ADD_VALUE，合零基禁 inc 铁律）。零新增读点，R3 安全。
        map.put(StatIds.STAT_CONVERSION_ALL_EVASION_TO_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR)
                        .self()
                        .amount(per("attribute:coe:evasion"))
                        .param("value", 1.0).param("per", 1.0)
                        .build());

        // ===================== 事件计数缩放（count:<event>[:<tag>][.<seconds>]）=====================
        // 读值源为事件历史触发次数（EventHistoryValue mode=count）。窗口缺省 4s（PoE2 RECENTLY 语义），
        // 「in the past N seconds」经 .<seconds> 后缀显式指定（见 ValueExprTypes 的 count 中缀）。
        // 本段仅接「for each time…」= 明确的<b>次数</b>语义。
        // 「per Enemy you've Ignited」类是<b>去重敌</b>语义，绝不可用施加次数近似（单敌反复点燃会被错算成多敌）——
        // 见下方「去重敌计数缩放」小节。

        // fire 通道（GameEvent.fireCaused/fireTaken/firePair）除 timer + StatEngine.fire 外还须写
        // Event.State 计数队列，否则本族读值源恒为 0：GameEvent.fire 已写 Event.record(owner, eventName, tags)
        // （三参双写 triggers + taggedTriggers），Event.record(e, name, Set) 三参重载补无标签队列写入。

        // 「1% increased Movement Speed for each time you've Blocked in the past 10 seconds」
        // 格挡方在 DamagePipeline.stageBlock 是 firePair 的 <b>caused</b> 侧（防御方为造成侧），
        // 故事件键用 caused_block 而非 taken_block。窗口 10s 经 .<seconds> 后缀指定。
        map.put(StatIds.INC_MOVEMENT_SPEED_FOR_EACH_TIME_YOU_VE_BLOCKED_IN_THE_PAST_SECONDS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED)
                        .self()
                        .amount(per("count:caused_block.10"))
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());

        // 「10% increased Poison Duration for each Poison you have inflicted Recently, up to a maximum of 100%」
        // 分维计数：caused_ailment 事件 + poison 标签（AilmentHelper.fireAilment 以 ailmentId 作标签，
        // StatusDefs.POISON.id == "poison"）。
        // 注：EFF id 的 _TAKEN 后缀是<b>命名遗留</b>——实现为 Mode.GAIN + not(TAKEN)，语义正是「你施加的」，
        // 与本条「you have inflicted」一致（见 StatEffectGen L174）。
        map.put(StatIds.INC_POISON_DURATION_FOR_EACH_POISON_YOU_HAVE_INFLICTED_RECENTLY_UP_TO_A_MAX_OF,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_POISON_TAKEN)
                        .self()
                        .amount(perCapped("count:caused_ailment:poison"))
                        .param("value", 0.10)
                        .param("per", 1.0)
                        .param("cap", 1.0)
                        .build());

        // 「Gain 1% of Damage as Extra Fire Damage per Endurance Charge Consumed Recently」
        // 读值源必须用 <b>sum</b> 而非 count：ResourceRegistry.consumeCharge（ChargedState.java:128）一次调用
        // 只写一条 Event.record(living, consumeCharge("endurance"), real)，real = 本次实际消耗球数。
        // count 数的是「记录条数」（一次消耗 3 球仍只记 1）→ 静默欠授予；sum 累加 TriggerRecord.value()
        // 才等价于 PoE2 的「球数」。故本条同时驱动了 ValueExprTypes 新增 sum:<event> 中缀。
        // 落点复用既有「全伤害 → 额外火」EFF（DamageConvertStatEffect Mode.GAIN，
        // 源集合 = PHYSICAL/FIRE/CHAOS/COLD/LIGHTNING 全族 → FIRE，与 WireMisc 既有兄弟同口径）。
        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY,
                StatBuilder.effect(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_2)
                        .self()
                        .amount(per("sum:consume_endurance"))
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());

        // 「Skills deal 8% increased Damage per Combo consumed, up to 40%」
        // 读值源为 sum:combo_spent，但<b>语义是「本次施放消耗量」而非 recently 累计</b>：
        // 语料原文不含 "Recently"（对比上一条 endurance 语料明确写了 Consumed Recently）——
        // PoE2 中 combo 是终结技的当次代价，消耗 N 层即本次技能 +N×8%，不应把窗口内上一次消耗一并计入。
        // 故写入端 ResourceRegistry.consumeCombo 在每次真实消耗前先 Event.clear（见 Event#clear 注释），
        // 使 sum:combo_spent 恒等于本次消耗层数；此处仍走同一 sum 原语，语义由写入端的清空保证。
        // 必须用 sum 而非 count（同上条）：一次消耗多层只写一条记录，count 会静默欠授予。
        // 发射点选在 ResourceRegistry 内部而非两个调用点：SkillContext.consumeCombo 传 bus=null（连 ComboEvent 都不发），
        // 且 ComboEvent 不带消耗量（只有剩余层数），无法承载数量语义。
        map.put(StatIds.SKILLS_DEAL_INC_DAMAGE_PER_COMBO_CONSUMED_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(perCapped("sum:combo_spent"))
                        .param("value", 0.08)
                        .param("per", 1.0)
                        .param("cap", 0.4)
                        .build());

        // ===================== 去重敌计数缩放（distinct:<event>[.<seconds>]:<身份标签前缀>）=====================
        // 读值源 = EventHistoryValue mode=distinct：窗口内匹配标签<b>去重后</b>的数量。
        //
        // 与上一段 count 的分工是本族的<b>正确性核心</b>：
        //   · count:caused_ailment:ignite  → 点燃「发生了几次」（同一个敌人反复点燃计多次）
        //   · distinct:caused_ailment:victim_ignite → 点燃了「几个不同的敌人」（同一敌人只计 1）
        // 语料写的是 per <b>Enemy</b>，故必须走后者（R3：不得以「施加次数」近似「敌人数」）。
        //
        // 机制来源（三处协同，均为既有设施的最小启用/扩展）：
        //   ① GameEvent.fire 在「造成侧 + 有具体异常名 + 有受害者」时，向计数队列附加身份标签
        //      victim_<ailmentId>:<victimUUID>（仅进 Event.State 队列，不进 fire 作用域标签，见 withSubjectTag）；
        //   ② Event.State.countTagged(..., unique=true) —— 去重分支；
        //   ③ EventHistoryValue mode=distinct + PREFIX 匹配：PREFIX 聚合同族（victim_ignite:*），
        //      去重键取完整标签（含 UUID）→ 恰为不同敌人数。
        // 身份标签独立前缀 victim_ 是必需的：裸异常标签（ignite）与之同前缀会让 PREFIX 命中裸标签，
        // 使去重键退化为常量、计数恒 1（见 GameEvent.subjectTagPrefix 的说明）。

        // 「4% increased Area of Effect for Attacks per Enemy you've Ignited in the last 8 seconds, up to 40%」
        // 窗口 8s 经 .<seconds> 后缀显式指定（缺省 4s 不符本条语料）。
        // EFF 沿用 INC_AREA_OF_EFFECT_FOR_ATTACKS 的既有口径（WireMisc L276 同为「for Attacks」的 AoE 条目，
        // 用同一 {AREA_OF_EFFECT} MULTIPLIED_BASE EFF），保证同族两条不出现两套作用域。
        map.put(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS_PER_ENEMY_YOU_VE_IGNITED_IN_THE_LAST_SECONDS_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT)
                        .self()
                        .amount(perCapped("distinct:caused_ailment.8:victim_ignite"))
                        .param("value", 0.04)
                        .param("per", 1.0)
                        .param("cap", 0.40)
                        .build());

        // 「8% increased Skill Effect Duration per Enemy you've Frozen in the last 8 seconds, up to 40%」
        // 窗口 8s（语料 "in the last 8 seconds"）。去重维度 = 被冰冻的<b>不同敌人</b>
        // （distinct:caused_ailment.8:victim_freeze 以 victim_freeze:<uuid> 全标签作去重键，与 ignite 同构）。
        // EFF 走本引擎统一承载「Skill Effect Duration」的 {AILMENT_DURATION} 属性（StatEffectIds 设计注释：
        // 「Skill Effect Duration」统一落到 AILMENT_DURATION，与 {AILMENT, !TAKEN} 的施加侧变体口径一致）；
        // 配合 lingering/beam/banner 时长站点以 {AILMENT, !TAKEN} 作用域读取该属性，使加成真实作用于技能时长（非死线）。
        map.put(StatIds.INC_SKILL_EFFECT_DURATION_PER_ENEMY_YOU_VE_FREEZE_IN_THE_LAST_SECONDS_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION)
                        .self()
                        .amount(perCapped("distinct:caused_ailment.8:victim_freeze"))
                        .param("value", 0.08)
                        .param("per", 1.0)
                        .param("cap", 0.40)
                        .build());

        // 「20% increased Damage for each different Warcry you've used Recently」
        // 去重维度 = 战吼<b>技能类型</b>（而非次数）。SkillEvent.tryGrantWarcry 在 USE_WARCRY 计数队列里
        // 写入身份标签 skill_<id>（仅进计数队列、不进 fire 作用域），故 distinct 按完整标签（含 id）去重 =
        // 「用了几种不同战吼」。窗口走 RECENTLY 默认值（recently() 便捷构造固定 4s），与语料 "Recently" 口径一致。
        // EFF 走 {DAMAGE} MULTIPLIED_BASE（DAMAGE base 非零，三定律 #3 合规）。
        map.put(StatIds.INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE)
                        .self()
                        .amount(perCapped("distinct:use_warcry:skill"))
                        .param("value", 0.20)
                        .param("per", 1.0)
                        .param("cap", 1.0)
                        .build());

        // ===================== 施放技能族（CAST_SKILL 事件）=====================
        // 去重维度 = 施放技能的<b>类型</b>。SkillEvent 在技能成功施放处（canCast 通过后）发射 CAST_SKILL，
        // 并写入身份标签 skill_<id>（仅进计数队列、不进 fire 作用域），故 distinct 按完整标签去重 =
        // 「用了几种不同技能」——对应语料「each different Spell/Attack you've Cast/Used」。
        // 与 warcry 共享同一套 distinct 基础设施（身份前缀 skill_ 隔离），仅事件键换成 cast_skill。
        // 注：「non-instant」语义在模组数据层暂无对应标签（技能不区分瞬发/非瞬发），
        // 故对全部技能施放计数（R3：不臆造 non-instant 过滤；PoE2 的 non-instant 在本架构下近似为「施放」本身）。

        // 「12% increased Spell Damage for each different Non-Instant Spell you've used in the past 8 seconds」
        // 窗口 8s（语料 "in the past 8 seconds"）。EFF 走 {DAMAGE, SPELL} MULTIPLIED_BASE。
        // 注：StatIds 该常量名尾段为 NON_INSTANT_ATTACK（与 INC_ATTACK_DAMAGE…NON_INSTANT_SPELL 交叉命名），按既有常量接线。
        map.put(StatIds.INC_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK_YOU_VE_USED_IN_THE_PAST_SECONDS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL)
                        .self()
                        .amount(perCapped("distinct:cast_skill.8:skill"))
                        .param("value", 0.12)
                        .param("per", 1.0)
                        .param("cap", 1.0)
                        .build());

        // 「12% increased Attack Damage for each different Non-Instant Attack you've used in the past 8 seconds」
        // 窗口 8s。EFF 走 {DAMAGE} MULTIPLIED_BASE（本架构不细分 attack 伤害 EFF，与通用 DAMAGE 同族）。
        map.put(StatIds.INC_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL_YOU_VE_USED_IN_THE_PAST_SECONDS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE)
                        .self()
                        .amount(perCapped("distinct:cast_skill.8:skill"))
                        .param("value", 0.12)
                        .param("per", 1.0)
                        .param("cap", 1.0)
                        .build());

        // 「6% increased Cast Speed for each different Spell you've Cast in the last eight seconds」（×2 同义条目）
        // 窗口 8s（"last eight seconds"）。EFF 走 COE_CAST_SPEED MULTIPLIED_BASE。
        map.put(StatIds.INC_CAST_SPEED_FOR_EACH_DIFFERENT_SPELL_YOU_VE_CAST_IN_THE_LAST_EIGHT_SECONDS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED)
                        .self()
                        .amount(perCapped("distinct:cast_skill.8:skill"))
                        .param("value", 0.06)
                        .param("per", 1.0)
                        .param("cap", 1.0)
                        .build());

        // 「10% increased Stun Threshold for each time you've been Hit by an Enemy Recently, up to a maximum of 100%」
        // 事件键 taken_hit 的写入带天然敌我过滤（DamagePipeline.stageTrigger 仅在
        // attacker != null && attacker != self 时记录），故精确对应「被<b>敌人</b>击中」。
        // EFF 沿用 INC_STUN_THRESHOLD 惯例（WireAilment L170）：AILMENT_THRESHOLD 为加值槽，
        // 百分比数字按点数直接加（零基属性禁 ADD_MULTIPLIED_BASE，见三定律#3）。
        map.put(StatIds.INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_HIT_DAMAGE_BY_AN_ENEMY_RECENTLY_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(perCapped("count:taken_hit"))
                        .param("value", 10.0)
                        .param("per", 1.0)
                        .param("cap", 100.0)
                        .build());

        // 「100% increased Stun Threshold for each time you've been Stunned Recently」
        // taken_stun 由 AilmentHelper 轻晕/重晕统一写入（L618 / L630），两侧皆记于被眩晕者自身。
        map.put(StatIds.INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_STUNNED_RECENTLY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(per("count:taken_stun"))
                        .param("value", 100.0)
                        .param("per", 1.0)
                        .build());

        // 「8% increased Damage for each time you've Warcried Recently」
        // USE_WARCRY 由 SkillEvent.tryGrantWarcry（全工程唯一的战吼类技能施放判定点）
        // 经 Event.record(player, USE_WARCRY) 写入，故 count:use_warcry 可读——本条按本文件
        // 既有 per() 范式接线（8% / 每次 / 4s RECENTLY 窗口，与 taken_hit 同窗口口径）。
        // 落点与数值约定沿用本文件 INC_DAMAGE_PER_* 系列：全局 inc Damage 走
        // EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE（GateExpr.ALWAYS，非 {DAMAGE} 作用域变体），
        // value 用小数（0.08 = 8%），per=1.0 即「每 1 次」。
        map.put(StatIds.INC_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(per("count:use_warcry"))
                        .param("value", 0.08)
                        .param("per", 1.0)
                        .build());

        // 「{X}% inc Damage for each Hazard triggered Recently, up to {X}%」
        // caster 侧 count 计数：触发时（on_trigger） 对 owner 记录 HAZARD_TRIGGERED 事件，
        // 本 stat 在持有者 StatStore 引用 count:hazard_triggered → EventRefCollector 自动订阅该事件，
        // 故 count:hazard_triggered 真实可读（R3 安全，零新增读点，与 warcry/combo 同族）。
        // 落点复用全局 inc Damage（EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE，base=1.0 非零，三定律#3 合规）。
        // 注：本条是 HAZARD_TRIGGERED 记录的唯一消费者，闭环「触发→记录→读取」。
        map.put(StatIds.INC_DAMAGE_FOR_EACH_HAZARD_TRIGGERED_RECENTLY_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE)
                        .self()
                        .amount(perCapped("count:hazard_triggered"))
                        .param("value", 0.08)
                        .param("per", 1.0)
                        .param("cap", 0.40)
                        .build());

        // 「{X}% increased Cast Speed per Spell Echoed Recently, up to {X}%」
        // 语料实测（天赋 notable echo）：6% / 次，上限 30%（vals = [0.06, 0.30]）。
        // 门控计数：新建事件 ECHOED_SPELL，发射点 EchoState#recordEchoedSpell —— 仅在<b>回响重放</b>
        // 法术时记录（原施放不记），且要求技能携带 spell 标签，与 "Spell Echoed" 精确对齐；
        // 回响机制本身对任意技能生效（战吼 Echoing Cry 亦走同一引擎），故必须在事件层区分，
        // 不能让战吼回响混进本条（count: 无标签分维，见 WireRecent 顶部判据）。
        // 陷阱：不可用 count:cast_skill 顶替——那会把「施放次数」当成「回响次数」，语义越界。
        // 落点 CAST_SPEED（base=1.0 非零）→ ADD_MULTIPLIED_BASE，三定律 #3 合规；走 perCapped 带上 cap。
        map.put(StatIds.INC_CAST_SPEED_PER_SPELL_ECHOED_RECENTLY_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED)
                        .self()
                        .amount(perCapped("count:echoed_spell"))
                        .param("value", 0.06)
                        .param("per", 1.0)
                        .param("cap", 0.30)
                        .build());

        // 「Volatile Power also grants {X}% increased Critical Hit chance per Volatility exploded」
        // 权威语义（poe2wiki Volatility）：波动满层后延迟引爆，**爆炸后 10 秒内**获得 Volatile Power，
        // 其收益按「**爆炸掉的波动层数**」计（"for each Volatility which exploded"）——故两个要点：
        //   ① 读 **sum** 而非 count：VolatilityDetonationManager.detonate 的记录为
        //      Event.record(source, "volatility_detonated", (int) maxV)，回传值 = 本次引爆的层数（非 1），
        //      用 count 会把「每次引爆」当 1 层，与「per Volatility」差一个量级（同 #25 sum/count 分野）。
        //   ② 窗口取 **10 秒（.10 后缀）**：对齐 Volatile Power 的 buff 时长，而非 recently 默认的 4 秒。
        //      窗口即 buff 期：窗口外记录自动过期 ⇒ 天然表达「爆炸后 10 秒内享有」，无需额外 buff 状态。
        // 落点 CRIT_CHANCE（registerPercentage base=0.05，非零）→ ADD_MULTIPLIED_BASE 合规（三定律#3）。
        // EFF 取通用无作用域变体（StatEffectGen:122，无 GateExpr ⇒ ALWAYS 恒真），
        // 因本条是全局暴击率、无技能/伤害类型限定——加任何作用域标签反致死线（#36）。
        // 近似说明：PoE2 中 10 秒内二次引爆会**刷新** buff 而非叠加，滑动窗口 sum 会累加两次层数；
        // 但引爆需重新攒满波动（默认 4s 延迟 + 积攒时间），重叠窗口罕见，且此为现有通道下的最贴近表达。
        map.put(StatIds.VOLATILE_POWER_ALSO_GRANTS_INC_CRITICAL_HIT_CHANCE_PER_VOLATILITY_EXPLODED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE)
                        .self()
                        .amount(per("sum:volatility_detonated.10"))
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());

        // 「Consuming Glory grants you {X}% inc Attack damage per Glory consumed for {X} seconds, up to {X}%」
        // 权威数值（天赋 notable thrill_of_the_fight）：3% / 点，持续 6 秒，上限 60%。
        // 读源 **sum** 而非 count：ResourceRegistry.consumeGlory 的发射为 GameEvent.emit(..., real)，
        // 经 Event.record(e, name, int value) 只写<b>无标签</b>队列并承载回传点数（非恒 1），
        // 故 sum:consume_glory 得「窗口内消耗点数之和」；用 count 会把多点的那次消耗算成 1（同 #25 sum/count 分野）。
        // 窗口取 **6 秒（.6 后缀）**：对齐语料「for 6 seconds」——buff 期即窗口期，
        // 窗口外记录自动过期，无需额外 buff 状态（与上方 volatility 的 10 秒窗口同款手法）。
        // 落点 DAMAGE {DAMAGE, ATTACK}（StatEffectGen:129），DAMAGE base=1.0 → ADD_MULTIPLIED_BASE 合规。
        // 近似说明：PoE2 多次消耗会<b>刷新</b> buff 而非叠加，滑动窗口 sum 会累加窗口内各次消耗；
        // 此为现有通道下最贴近表达（需精确刷新语义则要新增带时长的 buff 机制，属 C 类）。
        map.put(StatIds.CONSUMING_GLORY_GRANTS_YOU_INC_ATTACK_DAMAGE_PER_GLORY_CONSUMED_FOR_SECONDS_UP_TO,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE)
                        .self()
                        .amount(perCapped("sum:consume_glory.6"))
                        .param("value", 0.03)
                        .param("per", 1.0)
                        .param("cap", 0.60)
                        .build());

        // ===================== 资源读值缩放（rage / mana / energy …，HealthValue）=====================
        // 读值源为 HealthValue 的具名模式（ValueExprTypes 的 health/max_health/mana/energy/spirit/rage
        // 等注册键），输出资源<b>当前值</b>（rage 经 AttachmentRegistry.RAGE 读取）。
        // 仅接「per <当前资源量>」；「per <资源> spent/consumed Recently」需事件计数（见上节缺口）。

        // 「Every Rage also grants 1% increased Armour」
        // ARMOR 为 vanilla 属性（base=0），零基禁 ADD_MULTIPLIED_BASE —— 沿用 WireDefense L24 的
        // INC_ARMOR 惯例走 ADD_MULTIPLIED_TOTAL（三定律#3）。
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_ARMOR,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR)
                        .self()
                        .amount(perRage())
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());

        // 「Every Rage also grants 2% increased Stun Threshold」
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_STUN_THRESHOLD,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_STUN)
                        .self()
                        .amount(perRage())
                        .param("value", 2.0)
                        .param("per", 1.0)
                        .build());

        // 「Every five Rage also grants you 2% increased Minion Attack Speed」
        map.put(StatIds.EVERY_FIVE_RAGE_ALSO_GRANTS_YOU_INC_MINION_ATTACK_SPEED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED_MINION)
                        .self()
                        .amount(perRage())
                        .param("value", 0.02)
                        .param("per", 5.0)
                        .build());

        // ===================== Presence 计数缩放（presence_count:<relation>.<kind>）=====================
        // 读值源为 Presence 范围内成员计数（PresenceCountValue）。relation=ally 计「同阵营或为你所有」的
        // 成员；kind 区分召唤物（coe:summoned 持久化键）与图腾（SkillTotem 类型），二者分别对应 PoE2 的
        // Minion 与 Summoned Totem。图腾<b>使用</b>的攻击/法术提速（ATTACKS_USED_BY_TOTEMS_…）作用于
        // 图腾自身而非持有者，需目标作用域承载，不在本族处理。

        // 「5% increased Attack Damage for each Minion in your Presence, up to a maximum of 80%」
        map.put(StatIds.INC_ATTACK_DAMAGE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE)
                        .self()
                        .amount(perCapped("presence_count:ally.summoned"))
                        .param("value", 0.05)
                        .param("per", 1.0)
                        .param("cap", 0.8)
                        .build());

        // 「X% increased Health and Mana Regeneration Rate for each Minion in your Presence, up to a maximum of Y%」
        // 双属性：REGENERATION_RATE 单一属性 + {health,regen}/{mana,regen} 双作用域（HealthHelper/ManaHelper 分读）。
        // 故用组合 EFF（GateExpr = (health,regen)|(mana,regen)）一次性覆盖生命与魔力回复速率，避免拆两条半截接线。
        // 读值源同上方 attack-damage 族 = presence_count:ally.summoned（coe:summoned 持久化键，live）。
        map.put(StatIds.INC_HEALTH_AND_MANA_REGENERATION_RATE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_AND_MANA_REGEN)
                        .self()
                        .amount(perCapped("presence_count:ally.summoned"))
                        .param("value", 0.05)
                        .param("per", 1.0)
                        .param("cap", 0.8)
                        .build());

        // 「10% increased Attack Damage per Summoned Totem in your Presence」
        map.put(StatIds.INC_ATTACK_DAMAGE_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE)
                        .self()
                        .amount(per("presence_count:ally.totem"))
                        .param("value", 0.10)
                        .param("per", 1.0)
                        .build());

        // 「10% increased Armour and Evasion Rating per Summoned Totem in your Presence」
        // 复用 INC_ARMOR_AND_EVASION 同源 EFF（armor base=0，故走 MULTIPLIED_TOTAL，符零基禁 inc）。
        map.put(StatIds.INC_ARMOR_AND_EVASION_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR)
                        .self()
                        .amount(per("presence_count:ally.totem"))
                        .param("value", 0.10)
                        .param("per", 1.0)
                        .build());

        // 「Each Totem applies X% increased Damage taken to Enemies in their Presence」
        // 语料主体是「<b>敌人</b>承伤增加」→ 加成落在敌人身上，故 target 用 TGT_INLINE_PRESENCE_ENEMIES
        // （用 self 会把承伤加成记到玩家自己头上，语义反了）。
        //
        // 落点不是普通 TAKEN_DAMAGE 修饰符而是 <b>PresenceAuraStatEffect 授予模式</b>，原因：
        // 承伤读取 readInScope(DamagePipeline:881) 推入的作用域 sourceEntity=null，
        // StatEngine.compute 的「攻击者条目 → 受害者」跨实体维度因此不参与聚合
        // ——挂在任何非受害者身上的 TAKEN_DAMAGE 条目都读不到（死线）。
        // 故由光环把条目<b>写进敌人自己的 store</b>（self 维度命中），与 PRESENCE_MARK 状态同读点、但不建状态。
        //
        // 「每个图腾」不写进 EFF：由本处 amount 表达式注入 presence_count:ally.totem 缩放基数，
        // 于光环刷新时在持有者上下文求值（图腾增减自动反映）。TAKEN_DAMAGE base=1.0 非零基 → ADD_MULTIPLIED_BASE 合规。
        map.put(StatIds.EACH_TOTEM_APPLIES_INC_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE,
                StatBuilder.effect(StatEffectIds.EFF_PRESENCE_AURA_TAKEN_DAMAGE)
                        .self()
                        .amount(per("presence_count:ally.totem"))
                        .param("value", 0.02)
                        .param("per", 1.0)
                        .build()
                        .withConditionRef(StatConditionIds.presenceId(PresenceCountValue.Relation.ENEMY)));

        // 「Minions deal 10% increased Damage with Command Skills for each different type of Persistent Minion in your Presence」
        // 读值源是本次扩展的 presence_count:ally.persistent_minion.<b>.distinct</b>——按实体类型<b>去重</b>计「种类数」。
        // 语料是 "each different type"：若沿用普通计数（ally.summoned），同种 5 个骷髅会被算成 5 → 静默过度授予。
        // 持久判据（coe:summoned 键存在 且 MINION_EXPIRE==0）与 SummonEvent.isPersistentMinion 同源，避免两处漂移。
        // 不能用事件的 distinct 原语：持久召唤物是<b>常驻状态</b>而非事件，窗口过期后必归零（R3 死线）。
        // 落点复用既有 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_COMMAND_MINION（{command,minion} 作用域，
        // 与已接线兄弟 MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS 同源）→ 本条<b>零新增属性/EFF/COND</b>。
        // 语料无 "up to" 上限，故用 per() 而非 perCapped()。
        map.put(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS_FOR_EACH_DIFFERENT_TYPE_OF_PERSISTENT_MINION_IN_YOUR_PRESENCE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_COMMAND_MINION)
                        .self()
                        .amount(per("presence_count:ally.persistent_minion.distinct"))
                        .param("value", 0.10)
                        .param("per", 1.0)
                        .build());

        // ===================== 图腾使用技能的增速 =====================
        // 与上方两条 _IN_YOUR_PRESENCE 的区别：那两条加成落在<b>持有者自身</b>（self），
        // 这两条语料主体是「Attacks used by Totems / Spells Cast by Totems」——加成落在<b>图腾</b>上，
        // 故 target 用 TGT_INLINE_TOTEM 而非 self（用 self 会把图腾数加成记到玩家头上，语义错）。
        // 缩放基数同为 presence_count:ally.totem（PresenceCountValue）。

        // 「Attacks used by Totems have 3% increased Attack Speed per Summoned Totem」
        map.put(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED_PER_SUMMONED_TOTEM,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED)
                        .self()
                        .amount(per("presence_count:ally.totem"))
                        .param("value", 0.03)
                        .param("per", 1.0)
                        .build()
                        .withConditionRef(StatEntityConditionGen.relationId(RelationCondition.Relation.TOTEM)));

        // 「Spells Cast by Totems have 3% increased Cast Speed per Summoned Totem」
        map.put(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED_PER_SUMMONED_TOTEM,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED)
                        .self()
                        .amount(per("presence_count:ally.totem"))
                        .param("value", 0.03)
                        .param("per", 1.0)
                        .build()
                        .withConditionRef(StatEntityConditionGen.relationId(RelationCondition.Relation.TOTEM)));

        // ===================== 近敌计数缩放（nearby_enemy_count:<radius>）=====================
        // 读值源为半径内敌对存活实体数（NearbyEnemyCountValue，球形扫描 + 排除自身 + !isAlliedTo，
        // 与 dist_nearest_enemy / LivingStateValue.surrounded 同一套敌对判定口径）。
        // 与上节 presence_count 的分工：那是「己方成员数」（召唤物 / 图腾），本节是「敌方数量」。
        // 与本文件开头注释的 count:<event> 分工：那是「事件发生次数」（历史累计），本节是「当下在场个数」，
        // 故本节正是开头注释所指「留待计数原语落地」的那类去重实体计数。
        // 半径口径：PoE2 官方关键词 Close Range = 距角色 2 米内，故取 nearby_enemy_count:2。

        // 「3% increased Attack Speed per Enemy in Close Range」
        // 无「up to」封顶（语料原文无上限），故用 per() 而非 perCapped()。
        // minecraft:attack_speed base=4.0（非零基），ADD_MULTIPLIED_BASE 合规。
        map.put(StatIds.INC_ATTACK_SPEED_PER_ENEMY_IN_CLOSE_RANGE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED)
                        .self()
                        .amount(per("nearby_enemy_count:2"))
                        .param("value", 0.03)
                        .param("per", 1.0)
                        .build());

        // ===================== 目标元素异常种类数缩放（elemental_ailment_type_count）=====================
        // 「Damage with Hits is increased by 15% per type of Elemental Ailment on Enemy」
        // （Yoke of Suffering 实证「(15—20)% increased Damage for each Elemental Ailment type among your Ailments on them」；
        // poe2db 实证 Elemental Ailments = Ignite/Chill/Freeze/Shock/Electrocute 五类）。
        // 机制：新建 elemental_ailment_type_count 读值源，经 sourceEntity 切到命中目标、统计其身上不同元素异常种类数，
        // 与 onTarget 条目的 effect 落持有者自身（damage 属性）语义一致（R3：读点由 StatusEngine.has 真读取，非死线）。
        // 0 种 → 0%（敌无元素异常时不生效）；5 种 → +75%。damage base=1.0，ADD_MULTIPLIED_BASE 合规。
        map.put(StatIds.INC_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE)
                        .onTarget()
                        .amount(per("elemental_ailment_type_count"))
                        .param("value", 0.15)
                        .param("per", 1.0)
                        .build());
    }
}
