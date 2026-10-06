package com.cimdy.coe.data.gen.stat.wirings.wire.attribute;
import com.cimdy.coe.data.gen.stat.StatEntityConditionGen;

import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

import com.cimdy.coe.data.gen.stat.wirings.StatBuilder;
import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/** WireDefense 域接线（90 条）。 */
public final class WireDefense {

    private WireDefense() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // === C 纵切验证：天赋树节点 stat 接线（复用已有 effect，零新增注册，属性与对应 ADD_ 孪生一致） ===
        // "10~30% increased [Armour]"：Attributes.ARMOR 注册 base=0.0，ADD_MULTIPLIED_BASE 会静默归零
        // → 按三定律 #3 改用 ADD_MULTIPLIED_TOTAL（base=0 属性上唯一可行的 increased 近似）。
        map.put(StatIds.INC_ARMOR, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // livingmod 护甲 more 乘区（base 运算），复用 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR。
        map.put(StatIds.LIVINGMOD_MINECRAFT_ARMOR_OP_ADD_MULTIPLIED_BASE_1, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR));
        // 近期造成过近战命中 → 护甲提升（镜像 INC_ARMOR 同 EFF，COND_COUNT_RECENTLY_CAUSED_MELEE_HIT 复用 CAUSED_HIT:melee 计数）
        map.put(StatIds.INC_ARMOR_IF_YOU_VE_HIT_DAMAGE_AN_ENEMY_WITH_A_MELEE_ATTACK_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_MELEE_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // === C2 保守同义词桥接：ADD_ 孪生补全（复用已有注册 effect，零新增注册） ===
        map.put(StatIds.TO_EVASION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.TO_ARMOR, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_ARMOR_AND_EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR));
        map.put(StatIds.INC_DEFLECTION_RATING, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT));
        // 偏转抵挡的命中伤害量 → 同 DEFLECTION_RATING 属性（BlockHelper:28 已读取，R3 安全，零新增读点）
        map.put(StatIds.TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT));
        // === C4 穿透家族补接：节点常量未接线、对应穿透 EFF 已注册（零新增 effect 注册，R3 合规） ===
        map.put(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_FIRE_RESISTANCE));
        map.put(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_COLD_RESISTANCE));
        map.put(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_LIGHTNING_RESISTANCE));
        map.put(StatIds.ADD_ARMOR, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.ADD_BLOCK_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BLOCK_CHANCE));
        map.put(StatIds.ADD_CHAOS_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));
        map.put(StatIds.ADD_COLD_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));
        map.put(StatIds.ADD_EVASION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.ADD_FIRE_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));
        // === 同构扩展：ENEMIES_*_BY_YOU_HAVE_TO_*_RESISTANCE → 抗性降低须落在受害者属性上 ===
        // 不可再用 onTarget（= TGT_COND_ON_TARGET，target=null）：StatEngine.aggregate 的门控
        // resolveTargets(target).contains(reader) 在 target==null 时退化为 List.of(owner)，
        // 跨实体分支 owner=攻击者 / reader=受害者 → 门控恒 false，条目在条件求值前就被丢弃（死线）。
        // TGT_INLINE_TRIGGER_TARGET = (target, condition, value) = (EVENT_TARGET, EVENT_TARGET, null)：
        //   门控取受害者 ✓ ／条件在受害者身上求值（冻结/点燃/诅咒状态判的正是受害者）✓
        //   ／数值按持有者（攻击者）求值（节点幅度来自攻击者词缀）✓
        map.put(StatIds.ENEMIES_FREEZE_BY_YOU_HAVE_TO_COLD_RESISTANCE, stat(StatConditionIds.COND_FREEZE_STATUS, StatTargetIds.TGT_INLINE_TRIGGER_TARGET, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));
        map.put(StatIds.ENEMIES_IGNITED_BY_YOU_HAVE_TO_FIRE_RESISTANCES, stat(StatConditionIds.COND_IGNITE_STATUS, StatTargetIds.TGT_INLINE_TRIGGER_TARGET, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));
        // 诅咒目标侧：对带 CURSE 状态的敌施加属性，同源 freeze/fire 敌抗性范式（R3 安全）
        map.put(StatIds.ENEMIES_YOU_CURSE_HAVE_TO_CHAOS_RESISTANCES, stat(StatConditionIds.COND_ANY_CURSE, StatTargetIds.TGT_INLINE_TRIGGER_TARGET, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));
        // 诅咒目标侧·生命再生：原 onTarget(COND_ANY_CURSE, EFF_REGEN) 写法属 #33 死线——承伤/再生读点无 sourceEntity，
        // 跨实体分支恒跳过；且 onTarget 效果落持有者自身（#38）而非被诅咒敌。改 self() 落 caster 侧倍率标记
        // CURSE_TARGET_HEALTH_REGEN_MULT（base=1，+X%），由 StatusType.CURSE_STAT_PROVIDER 在诅咒作用于敌时
        // 以 (值-1) 写进敌身 REGENERATION_RATE 的 ADD_MULTIPLIED_BASE；敌 getHealthRegeneration 在 {health,regen}
        // 作用域真读（R3 安全，非死线）。条件「被诅咒」已由诅咒状态本身保证，无需条目侧 COND_ANY_CURSE。
        map.put(StatIds.TARGETS_CURSED_BY_YOU_HAVE_INC_HEALTH_REGENERATION_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CURSE_TARGET_HEALTH_REGEN));
        // M2：诅咒持续时长到期后增诅咒 buff 幅度。条件 COND_CURSE_DURATION_EXPIRED 经 conditionTarget(EVENT_TARGET)
        // 在被诅咒敌侧实时求值（status node (StatusValue.byCurseFraction) 读敌身 CURSE 状态存续比例 ≥ 0.5），R3 安全非死线；
        // EFF 指向施法者 AILMENT_MAGNITUDE@{curse,!taken}（诅咒幅度），由 StatusType.dynamic 注入的 CURSE_STAT_PROVIDER
        // 在 provide 时进入带 sourceEntity(target) 的作用域真实读取，复活既有 3 条诅咒幅度死线（R3 安全）。
        map.put(StatIds.YOUR_CURSES_HAVE_INC_BUFF_MAGNITUDE_IF_OF_CURSE_DURATION_EXPIRED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_CURSE_TAKEN_2)
                        .self().conditionTarget(TargetSelector.EVENT_TARGET).when(StatConditionIds.COND_CURSE_DURATION_EXPIRED).build());
        // ── 危害目标侧簇（纯数据；onTarget(COND_HAZARD_MARKED, EFF) 镜像 ENEMIES_YOU_CURSE_HAVE_TO_CHAOS_RESISTANCES）──
        // 机制：触发时（on_trigger） 对穿越 target 施 HAZARD_MARK（80t=4s，对应「recently」口径）；
        // 敌持该标记即满足条件。armor/evasion EFF 必须落「敌身」——故用 TGT_INLINE_TRIGGER_TARGET
        // （target=EVENT_TARGET 过 StatEngine.aggregate:224 门控、conditionTarget=EVENT_TARGET 在受害者判 HAZARD_MARK、
        // valueTarget=null 数值取持有者），而非 onTarget（target=null → 门控 List.of(owner=攻击者) 含受害者恒 false → 死线 #38）。
        // 读点 = victim 的 ARMOR/EVASION 属性 fold（stageDefend:593 / EvasionEntropy），该 fold 发生在 enterHitScopes
        // 作用域内（DamageEvent:242/247 sourceEntity=攻击者已注入），跨实体分支 StatEngine:121 正常聚合，
        // 效果仅在「你命中带 HAZARD_MARK 的敌人」时生效（R3 安全、非死线；与已修的诅咒降抗族同源）。
        // armor base=0 → MULTIPLIED_TOTAL；evasion base>0 → MULTIPLIED_BASE（三定律#3）。
        map.put(StatIds.ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_ARMOR,
                stat(StatConditionIds.COND_HAZARD_MARKED, StatTargetIds.TGT_INLINE_TRIGGER_TARGET, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_EVASION,
                stat(StatConditionIds.COND_HAZARD_MARKED, StatTargetIds.TGT_INLINE_TRIGGER_TARGET, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 诅咒目标侧·阻碍（R3 安全，纯数据）。复用同族 onTarget(COND_ANY_CURSE, EFF) 范式 +
        // EFF_CAUSED_HIT_GRANT_ON_EVENT_HINDER_STATUS_MAGNITUDE（GrantOnEventStatEffect，amountIsMagnitude=true，
        // StatEffectGen:676）→ CAUSED_HIT 向受害方授予 HINDER 状态，其 magnitude 直接取本 stat 的 $value
        // （onTrigger: intensity = round(entry.amount())，chance 恒 1.0）。
        // 语义：你对「被诅咒的敌人」命中时使其受阻，且语料 "{X}% increased Movement Speed" 即阻碍幅度 —— HINDER 行为
        // actionSpeed(magnitude/100) 真实减速，X% 越大减速越强（忠实承载 inc movement speed = 更强阻碍，非死线）。
        // 注：不可复用固定强度版 EFF_CAUSED_HIT_GRANT_ON_EVENT_HINDER_STATUS —— 该版 amount 是触发概率，
        // 会把本 stat 的 $value(=幅度) 误当几率（WireMisc:254 CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS 那类才需概率版）。
        map.put(StatIds.ENEMIES_YOU_CURSE_ARE_HINDERED_WITH_INC_MOVEMENT_SPEED, onTarget(StatConditionIds.COND_ANY_CURSE, StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_HINDER_STATUS_MAGNITUDE));
        map.put(StatIds.ADD_LIGHTNING_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));
        map.put(StatIds.ADD_MAX_CHAOS_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_MAX_RESISTANCES));
        map.put(StatIds.ADD_MAX_COLD_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD));
        map.put(StatIds.ADD_MAX_FIRE_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE));
        map.put(StatIds.ADD_MAX_LIGHTNING_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING));
        map.put(StatIds.ADD_MAX_RESISTANCES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES));
        map.put(StatIds.ARMOR_BREAK_FULLY, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // 破甲幅度 +X%：增攻击方 {armor_break} 作用域 ARMOR_BREAK 属性；DamagePipeline.stagePostReduction 真读该属性缩放破甲幅度（R3 安全，同源 LIVINGMOD_COE_ARMOR_BREAK_OP_ADD_VALUE_0_1_ARMOR_BREAK）
        map.put(StatIds.ARMOR_BREAK_INC_ARMOR, self(StatEffectIds.EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK));
        // 蔓生植物技能破甲 +X% 护甲：同款 ARMOR_BREAK {armor_break} 模型，作用域追加 plant（DamagePipeline:426 破甲读点已并入技能标签，plant 真存在时方生效，R3 真读非死线）。
        map.put(StatIds.OVERGROWN_PLANT_SKILLS_ARMOR_BREAK_INC_ARMOR, self(StatEffectIds.EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK_PLANT));
        // 承受破甲倍率：受害方持有时破甲幅度 ×(1+ARMOR_BREAK_TAKEN)；DamagePipeline.stageArmorBreak 真读取（R3 安全，与攻击方 ARMOR_BREAK 同模型单位）。
        map.put(StatIds.INC_ARMOR_BREAK_TAKEN, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ARMOR_BREAK_TAKEN));
        // 对带异常的目标破甲时增破甲幅度：攻击者持此 stat，仅当命中目标带异常（COND_ANY_AILMENT 经 conditionTarget(EVENT_TARGET) 在受害方求值）时，
        // 经 {armor_break} 作用域增攻击方 ARMOR_BREAK 属性（DamagePipeline 真读缩放破甲幅度，同源 ARMOR_BREAK_INC_ARMOR）。
        // conditionTarget(EVENT_TARGET)=TRIGGER_TARGET：依赖读点把受害者绑为 sourceEntity 才能求值。
        // 陷阱：破甲读点用的是 readInScope（StatContext.of(tags,null) 恒置 sourceEntity=null）→ triggerTarget=null
        // → 条件被静默跳过。该读点须用 readInScopeAgainst(..., s.living) 绑定受害者，条件才在受害方正确求值。
        map.put(StatIds.HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK)
                        .self().conditionTarget(TargetSelector.EVENT_TARGET).when(StatConditionIds.COND_ANY_AILMENT).build());
        // 重晕敌人时破甲 X% 护甲：GrantOnEvent（amount 即 magnitude 模式，chance=1.0 必定发生），CAUSED_AILMENT+heavy_stun 触发，
        // 向受害方（被重晕者）授予 ARMOUR_BREAK 状态（magnitude=节点值 X%，{armor_break} 作用域真读缩放破甲幅度，R3 安全）。
        // AilmentHelper.fireAilmentBySkill 已 fire 该事件（攻击者作用域持受害者，经 enterHitScopes 双向注入），触发通道真实存在，非死线。
        map.put(StatIds.ARMOR_BREAK_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY, self(StatEffectIds.EFF_CAUSED_AILMENT_HEAVY_STUN_ARMOR_BREAK_STATUS));
        // 钉刺敌人时破甲 X% 护甲：镜像重晕破甲，GrantOnEvent（amount 即 magnitude，chance=1.0），CAUSED_AILMENT+pin 触发，向受害方（被钉刺者）授予 ARMOUR_BREAK。
        // 触发通道：StatAction 施加 PIN 时经 AilmentHelper.fireStatusCaused 真 fire {caused_ailment, pin}（攻击者作用域持受害者），R3 真实触发，非死线。
        map.put(StatIds.ARMOR_BREAK_OF_ARMOR_ON_PINNING_AN_ENEMY, self(StatEffectIds.EFF_CAUSED_AILMENT_PIN_ARMOR_BREAK_STATUS));
        // 破甲时长 +X%：复用 AILMENT_DURATION 在 {armour_break, !taken} 作用域（StatusEngine.debuffAilmentDurationMultiplier 真读攻击方施放的破甲时长），零新属性原语，R3 安全。
        map.put(StatIds.INC_ARMOR_BREAK_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_ARMOUR_BREAK_TAKEN));
        // 完全破甲致残：攻击方持此 stat 时 flag 属性置 1.0；ARMOUR_BROKEN_FULLY 行为真读取并对受害方施 MAIM（R3 安全，非死线）。corpus 该 stat 为 COMPLEX/has_entry=false，故强度用默认 10% 硬编码于行为。
        map.put(StatIds.ENEMIES_YOU_FULLY_ARMOR_BREAK_ARE_MAIMED, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_MAIM_ON_FULLY_BREAK).self().amount("1.0").build());
        // 完全破甲禁再生：攻击方持此 stat 时 flag 属性置 1.0；ARMOUR_BROKEN_FULLY 行为真读取并对受害方施 CANNOT_REGEN_HEALTH（HealthHelper 归零再生，R3 安全）。corpus 该 stat 为 COMPLEX/has_entry=false，故强度用默认 flag 置位（1.0）硬编码于接线。
        map.put(StatIds.ENEMIES_YOU_FULLY_ARMOR_BREAK_CANNOT_REGENERATE_HEALTH, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_NO_REGEN_ON_FULLY_BREAK).self().amount("1.0").build());
        // 流血禁再生（同「完全破甲禁再生」范式）：施加方持此 stat 时 flag 属性置 1.0；BLEED 状态 init 行为真读取并对受害方施 CANNOT_REGEN_HEALTH（复用 HealthHelper 读点，R3 安全）。corpus 该 stat 为 COMPLEX/has_entry=false，强度用默认 flag 置位硬编码。
        // 走 GrantOnEvent，门控由事件标签 {caused_ailment, bleed} 承载，勿叠 COND_INFLICT_BLEED。
        map.put(StatIds.ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_NO_REGEN_ON_BLEED).self().amount("1.0").build());
        map.put(StatIds.ARMOR_REDUCTION, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // ES 不充能（POE2「Energy Shield does not Recharge」）：持有者 flag 属性置 1.0；
        // 每 tick 充能门控 ResourceRegistry.isEnergyShieldRechargeDisabled 真读（AttributeEvent 的 {regeneration} 作用域内），R3 安全。
        // 语料该 stat 为标记型（无强度），故 amount 固定 1.0。
        map.put(StatIds.ENERGY_SHIELD_DOES_NOT_RECHARGE, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ENERGY_SHIELD_RECHARGE_DISABLED).self().amount("1.0").build());
        // ES 不能回复到护甲值以上：flag 置位后 effectiveMax 把 ES 上限钳到护甲值；上限层钳制使回复/吸收/设置共用同一上界。
        map.put(StatIds.YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ENERGY_SHIELD_CAPPED_BY_ARMOR).self().amount("1.0").build());
        // 被你诅咒的敌人不能充能 ES：原 onTarget().when(COND_ANY_CURSE) 写法属 #33+#38 双重死线——效果落持有者自身（#38）、
        // 且承伤/充能读点无 sourceEntity 跨实体分支恒跳过（#33），敌身永不生效。改 self() 落 caster 侧标记
        // CURSE_TARGET_ES_RECHARGE_DISABLED（amount=1.0），由 CURSE_STAT_PROVIDER 在诅咒作用于敌时写进敌身
        // ENERGY_SHIELD_RECHARGE_DISABLED；ResourceRegistry.isEnergyShieldRechargeDisabled 在 {regeneration} 作用域真读（R3 安全，非死线）。
        map.put(StatIds.ENEMIES_YOU_CURSE_CANNOT_RECHARGE_ENERGY_SHIELD, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CURSE_TARGET_ES_RECHARGE_DISABLED).self().amount("1.0").build());
        // ES 不能从<b>再生</b>回复（POE2「cannot recover Energy Shield from Regeneration」）：
        // 与「ES 不充能」是两条独立门控——充能走 {recharge} 来源、由每 tick 拦；再生走 {regen} 来源、
        // 由 ResourceEventHub 的来源门控拦，故用独立标记属性，避免互相错杀。
        map.put(StatIds.YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ENERGY_SHIELD_REGEN_DISABLED).self().amount("1.0").build());
        // 生命再生的<b>溢出</b>部分转给 ES（语料无限定百分比 ⇒ 全额 1.0）：
        // EXCESS 模式在溢出发生后查询，先喂饱生命、只搬装不下的量；来源过滤 {regen} 使本转移
        // 仍受上一条「不能从再生回复 ES」门控约束（二者共用同一来源标签）。
        map.put(StatIds.EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_HEALTH_ENERGY_SHIELD_EXCESS_RESOURCE_CONVERT)
                        .self().param("value", 1.0).build());
        // 生命充能取代 ES 充能：只重路由 {recharge} 来源，故 ES 的再生 / 偷取 / 药剂来源不受影响。
        map.put(StatIds.HEALTH_RECHARGE_INSTEAD_OF_ENERGY_SHIELD,
                StatBuilder.effect(StatEffectIds.EFF_ENERGY_SHIELD_HEALTH_RECHARGE_RESOURCE_CONVERT)
                        .self().param("value", 1.0).build());
        map.put(StatIds.ATTACK_DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES, self(StatEffectIds.EFF_ADD_VALUE_ATTACK_ATTRIBUTE_COE_RESISTANCE_PENETRATION_ELEMENTAL_RESISTANCE));
        map.put(StatIds.CHANCE_DODGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DODGE_CHANCE_DODGE));
        map.put(StatIds.CHARM_RESIST_CHAOS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));
        map.put(StatIds.CHARM_RESIST_COLD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));
        map.put(StatIds.CHARM_RESIST_FIRE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));
        map.put(StatIds.CHARM_RESIST_LIGHTNING, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));
        map.put(StatIds.CONVERTS_ALL_EVASION_RATING, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.DAMAGE_PENETRATES_ELEMENTAL_DAMAGE_RESISTANCES_FOR_EACH_TIME_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_ELEMENTAL_GLORY_RESISTANCE));
        map.put(StatIds.DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_RESISTANCES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_ELEMENTAL_RESISTANCE));
        map.put(StatIds.DAMAGE_PENETRATION_ELEMENTAL_DAMAGE_RESISTANCES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_ELEMENTAL_RESISTANCE));
        map.put(StatIds.DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_ELEMENTAL_RESISTANCE));
        map.put(StatIds.EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.FORM_BEAR_ARMOR, self(StatConditionIds.COND_FORM_BEAR, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.GAIN_OF_EVASION_AS_EXTRA_ARMOR, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 走通用加性门槛 AILMENT_THRESHOLD_ADD：它同时计入异常门槛与眩晕门槛
        // （AilmentHelper:606 / StunMeter:33），正是语料「inc Ailment Threshold and Stun Threshold」的「and」语义。
        // 不可改接 DEFLECTION_RATING 的 EFF：与语料无关，且其 {deflect} 标签无发射点。
        map.put(StatIds.INC_AILMENT_THRESHOLD_AND_STUN_THRESHOLD_WHILE_SURROUNDED, self(StatConditionIds.COND_0_0_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_THRESHOLD_ADD_STUN));
        map.put(StatIds.INC_ARMOUR, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_ARMOUR_EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR));
        map.put(StatIds.INC_BLOCK_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BLOCK_CHANCE));
        map.put(StatIds.INC_DEFLECTION_RATING_EQUAL_EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.INC_DEFLECTION_RATING_EQUAL_EVASION_DEFLECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 「Gain X% of Evasion as Deflection Rating」——新增 ADD_VALUE EFF 作用于 DEFLECTION_RATING，源取当前 Evasion（per-scaling 范式镜像 ES→Mana），BlockHelper:28 真读，R3 安全。
        map.put(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEFLECTION_RATING).self().amount("$value * attribute:coe:evasion / $per").param("value", 1.0).param("per", 1.0).build());
        map.put(StatIds.INC_DEFLECTION_RATING_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT));
        map.put(StatIds.INC_EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // === 同构扩展：INC_*_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY → self(not_recently TAKEN_HIT, EFF) ===
        map.put(StatIds.INC_ARMOR_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_NOT_RECENTLY_TAKEN_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_NOT_RECENTLY_TAKEN_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.INC_MINION_ELEMENTAL_RESIST, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL_MINION_RESISTANCE));
        map.put(StatIds.INC_STUN_RECOVERY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_STUN_RECOVERY_STUN));
        map.put(StatIds.MINIONS_HAVE_INC_EVASION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL_MINION_RESISTANCE));
        map.put(StatIds.MINIONS_HAVE_TO_ALL_MAX_ELEMENTAL_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_ELEMENTAL_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_COLD_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_FIRE_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_LIGHTNING_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_MAX_COLD_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_MAX_FIRE_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE_MINION));
        map.put(StatIds.MINIONS_HAVE_TO_MAX_LIGHTNING_RESISTANCES, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING_MINION));
        // 「Your Minions are Gigantic」：随从体型放大。spec 表(unit_audit2.md:582) 目标属性 coe:minions_gigantic（op add_multiplied_base, $value/100）在引擎中**无读取方**（死属性），
        // 故改挂 live 的 vanilla Attributes.SCALE（复用 RARITY_*_SCALE 同 EFF），经 TGT_INLINE_MINION 把收件人重定向为随从实体本身（同 MINIONS_HAVE_TO_* 范式）。
        // 该 id 为无 $value 的布尔旗标（node_stat_map "46365" values=[] / has_entry=false）→ 按同表($value/100)条目的处理范式烘焙 amount=1.0（= +100% 体型）。
        map.put(StatIds.YOUR_MINIONS_ARE_GIGANTIC, wiring(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SCALE).self().amount("1.0").build()
                .withConditionRef(StatEntityConditionGen.relationId(RelationCondition.Relation.MINION)));
        map.put(StatIds.PENETRATE_CHAOS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCE_PENETRATION_RESISTANCE));
        map.put(StatIds.PENETRATE_COLD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_COLD_RESISTANCE));
        map.put(StatIds.PENETRATE_FIRE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_FIRE_RESISTANCE));
        map.put(StatIds.PENETRATE_LIGHTNING, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_LIGHTNING_RESISTANCE));
        map.put(StatIds.TO_MAX_BLOCK_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_BLOCK));
        map.put(StatIds.TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES, relation(RelationCondition.Relation.TOTEM, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL_RESISTANCE));
        // ===== C0 自动接线（CLEAN 桶，已由 build_wire_table.py 校验） =====
        // 作用域通过 COND/TGT 在接线时挂载；op 族与 EFF 注册一致（INC->ADD_MULTIPLIED_BASE, MORE->ADD_MULTIPLIED_TOTAL）。
        map.put(StatIds.INC_ARMOR_WHILE_STATIONARY, self(StatConditionIds.COND_MOVEMENT_STATIONARY, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_ARMOR_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_EVASION_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.INC_ARMOR_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_EVASION_WHILE_PARRYING, self(StatConditionIds.COND_LIVING_STATE_PARRYING, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 翻滚过近期 → 增闪避：复用 COND_RECENTLY_DODGE_ROLLED（DODGE_ROLL 事件 + recently 4s 窗口）+ 既有 EVASION 零基 EFF
        map.put(StatIds.INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY, self(StatConditionIds.COND_RECENTLY_DODGE_ROLLED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // ===== 结束 =====
        
        // ===== 抗性族（RESISTANCES / MAX_RESISTANCES 真实属性；勿与 RESISTANCE_PENETRATION 混接）=====
        // 自身 flat 抗性
        map.put(StatIds.TO_FIRE_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));
        map.put(StatIds.TO_COLD_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));
        map.put(StatIds.TO_LIGHTNING_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));
        map.put(StatIds.TO_CHAOS_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));
        map.put(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));
        map.put(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_2, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));
        // 自身 最大抗性（flat）
        map.put(StatIds.TO_MAX_COLD_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD));
        map.put(StatIds.TO_MAX_LIGHTNING_RESISTANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING));
        // 条件型最大抗性
        map.put(StatIds.TO_MAX_FIRE_RESISTANCE_WHILE_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE));
        map.put(StatIds.TO_MAX_COLD_RESISTANCE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD));
        map.put(StatIds.TO_MAX_FIRE_RESISTANCE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE));
        map.put(StatIds.TO_MAX_LIGHTNING_RESISTANCE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING));
        // 「+X% to Maximum Resistances of each elemental damage type you have been Hit by recently」：
        // per-element 扇出——三元素各带「近期被该元素命中」条件，共享同一 X（StatGroup 各成员均绑 $value，
        // 经 flatten 后各自独立求值，条件按成员各自生效）。PoE2「elemental」= fire/cold/lightning，混沌不接。
        map.put(StatIds.TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY,
                group(
                        self(StatConditionIds.COND_RECENTLY_TAKEN_HIT_FIRE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_FIRE),
                        self(StatConditionIds.COND_RECENTLY_TAKEN_HIT_COLD, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_COLD),
                        self(StatConditionIds.COND_RECENTLY_TAKEN_HIT_LIGHTNING, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RESISTANCES_LIGHTNING)));
        // 同伴 / 图腾 作用域（target 决定作用域，effect 沿用同属性）
        map.put(StatIds.COMPANIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, relation(RelationCondition.Relation.COMPANION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));
        map.put(StatIds.TOTEMS_GAIN_TO_ALL_ELEMENTAL_RESISTANCES, relation(RelationCondition.Relation.TOTEM, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));
        map.put(StatIds.COMPANIONS_HAVE_TO_CHAOS_RESISTANCES, relation(RelationCondition.Relation.COMPANION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES));
        map.put(StatIds.INC_ARMOR_WHILE_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // ===== 新 EFF 变体设计（通用暴击族）+ 机械 WHILE/在场 收割 =====
        // 新增 5 个通用 ALWAYS 暴击 EFF（StatEffectIds/StatEffectGen）复用 CRIT_CHANCE/CRIT_DAMAGE 属性，对全部作用域恒生效；
        // verify_gen 计数已同步 545→550。下列接线均经逐条核验：COND/TGT/sEFF 常量真实存在，op 族对齐。
        // --- 机械 WHILE 镜像（沿用已验证 subject→EFF，仅换 COND 原语）---
        map.put(StatIds.INC_ARMOR_WHILE_BLEEDING, self(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        map.put(StatIds.INC_DEFLECTION_RATING_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT));
        map.put(StatIds.INC_EVASION_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        map.put(StatIds.INC_EVASION_WHILE_SPRINTING, self(StatConditionIds.COND_SPRINTING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 2) 吸血时 INC 护甲与闪避 —— 复用 INC_ARMOR_AND_EVASION 同源 EFF（, MULTIPLIED_TOTAL）切 COND_LEECHING_LIVING_STATE。
        map.put(StatIds.INC_ARMOR_AND_EVASION_WHILE_LEECHING, self(StatConditionIds.COND_LEECHING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR));
        // 低魔力时闪电抗性穿透（flat，RESISTANCE_PENETRATION 零基属性只走 ADD_VALUE）
        map.put(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA, self(StatConditionIds.COND_LOW_MANA_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_LIGHTNING_RESISTANCE));
        // 闪避翻滚时受到更多/更少伤害（承伤侧乘算）
        // ⚠️ 极性待办：语料为 "Take 30% less Damage from Hits while Dodge Rolling"（负向），
        //    而归一化把 less→more 且未变号（value=+0.3）。op 族按 StatId 取 TOTAL，方向由注入侧决定，见 polarity_audit.json。
        map.put(StatIds.TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING, self(StatConditionIds.COND_DODGE_ROLLING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE));
        // UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL → 闪避翻滚时免疫冰缓（self + COND_DODGE_ROLLING_LIVING_STATE + EFF_CHILL_IMMUNE），零新增 EFF/COND。
        map.put(StatIds.UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL, self(StatConditionIds.COND_DODGE_ROLLING_LIVING_STATE, StatEffectIds.EFF_CHILL_IMMUNE));

        // WHEN_ON_LOW_HEALTH —— 镜像 INC_ARMOR_AND_EVASION（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_ARMOR_AND_EVASION_WHEN_ON_LOW_HEALTH, self(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR));
        // WHILE_SURROUNDED —— 镜像 INC_BLOCK_CHANCE（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_BLOCK_CHANCE_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BLOCK_CHANCE));
        // WHEN_ON_FULL_HEALTH —— 镜像 INC_EVASION（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_EVASION_WHEN_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));

        // 近期事件条件：镜像 base 的 EFF，条件走 EventHistoryValue
        map.put(StatIds.INC_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY, self(StatConditionIds.COND_CONSUME_ENDURANCE_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));

        // IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY —— 镜像 INC_EVASION（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_EVASION_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY, self(StatConditionIds.COND_CONSUME_FRENZY_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));

        // 被击中后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR));
        // 被击中后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 格挡后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_THORNS_DAMAGE_IF_YOU_VE_BLOCKED_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_BLOCK, StatEffectIds.EFF_DAMAGE_TAKEN_HIT));
        // 格挡后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_CHANCE_TO_BLOCK_IF_YOU_VE_BLOCKED_WITH_A_RAISED_SHIELD_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_BLOCK, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BLOCK_CHANCE));
        // 「举盾格挡的敌人命中伤害有几率使其眩晕」：CAUSED_BLOCK 触发 + 举盾门控。
        // EFF 的 STATUS 授予对象是 evalCtx.triggerTarget() = 被格挡的攻击者（见 StatEffectGen 中该 EFF 的注释），
        // 自身不中眩晕。COND_HOLDING_SHIELD_LIVING_STATE 承担「with a raised Shield」的装备门控。
        map.put(StatIds.CHANCE_TO_DAZE_ENEMIES_WHOSE_HIT_DAMAGE_YOU_BLOCK_WITH_A_RAISED_SHIELD, self(StatConditionIds.COND_HOLDING_SHIELD_LIVING_STATE, StatEffectIds.EFF_CAUSED_BLOCK_DAZE_GRANT_ON_EVENT_STATUS));

        // 主动击中敌人后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_EVASION_IF_YOU_HAVE_HIT_AN_ENEMY_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));

        // hybrid：护甲 + 闪避 + 能量护盾 共享同一 X%（无三向合并 EFF，故用组合条目）
        map.put(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD, groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        // hybrid + 引导中条件
        map.put(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING, groupSelfWhen(StatConditionIds.COND_CHANNELLING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        // hybrid + 持长杖中条件（复用 channelling 三 EFF 族，仅 COND 门控不同；零新 EFF，R3 安全）
        map.put(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_WIELDING_A_QUARTERSTAFF, groupSelfWhen(StatConditionIds.COND_WIELDING_QUARTERSTAFF, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));

        // 持长矛时命中
        map.put(StatIds.INC_ACCURACY_WITH_SPEARS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ACCURACY_SPEAR));

        // INC_ACCURACY 的 BOW 作用域变体
        map.put(StatIds.INC_ACCURACY_WITH_BOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ACCURACY_BOW));
        // 持长杖时命中（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_ACCURACY_WITH_QUARTERSTAVES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ACCURACY_QUARTERSTAFF));
        // INC_ACCURACY 的 MELEE 作用域变体
        map.put(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ACCURACY_MELEE));
        // MINIONS_BREAK_ARMOR_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT → 召唤物侧 ARMOR_BREAK {ARMOR_BREAK}（DamagePipeline:425 真读，作用域仅 {armor_break}，
        // 多余 MINION 标签会使 GateExpr#matches / StatEngine#conditionsHold 门控因缺 minion 返回 false 而成死线，已去 minion 标签，与兄弟 EFF 同口径）。
        map.put(StatIds.MINIONS_BREAK_ARMOR_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK));
        // SUCCESSFULLY_PARRYING_A_PROJECTILE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_MELEE_ATTACK → DAMAGE {DAMAGE,MELEE}
        map.put(StatIds.SUCCESSFULLY_PARRYING_A_PROJECTILE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_MELEE_ATTACK, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_PARRY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));
        // 上一条的远程兄弟：门控全在 COND_COUNT_RECENTLY_CAUSED_PARRY（招架触发窗口），
        // EFF 只需表达「伤害 × 投射物」作用域。口径与兄弟严格一致——兄弟所用的
        // EFF_..._DAMAGE_MELEE_PARRY 虽名带 PARRY，其 GateExpr 实为 {DAMAGE, MELEE}（StatEffectGen:861），
        // 即 parry 语义完全由 COND 承担；故此处复用既有的 {DAMAGE, PROJECTILE} EFF（StatEffectGen:260），零新增。
        // 注：GateTag.PARRY 的注入站点只在 DamagePipeline:455/479/480 的「招架承伤」作用域，
        // 出伤作用域永不出现，故给 EFF 加 parry 标签反而是死线。
        map.put(StatIds.SUCCESSFULLY_PARRYING_A_MELEE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_RANGED_ATTACK, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_PARRY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE));
        // ── 元素曝露减抗模板（StatusBehavior#exposure 的 $value 注入目标）──
        // 曝露 = 削减目标对应元素抗性：RESISTANCES 属性 + 元素标签作用域 + ADD_VALUE。
        // 三条齐备才生效：缺任一条则 StatRead 静默失败，EXPOSURE_FIRE/COLD/LIGHTNING 状态只挂标记不减抗。
        // 复用既有「RESISTANCES + 元素标签」ADD_VALUE EFF（与 TO_FIRE_RESISTANCE 等语料条目同族），零新增注册。
        map.put(StatIds.EXPOSURE_FIRE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE));
        map.put(StatIds.EXPOSURE_COLD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD));
        map.put(StatIds.EXPOSURE_LIGHTNING, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING));

        // ── 词缀（Mod）域接线 ──
        // 1) 「+#% of Armour also applies to Elemental Damage」：DamageTakenConvertStatEffect(armor, EXTRA) + {elemental} 标签，
        //    StatEffectGen:701 真读——承伤侧把护甲减免按 X% 额外套用到元素伤害分量。
        map.put(StatIds.ARMOUR_APPLIES_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_ARMOR_DAMAGE_TAKEN_CONVERT_ELEMENTAL_EXTRA));
        // 2) 「Gain Deflection Rating equal to #% of Evasion Rating」：沿用本仓既有约定——
        //    StatIds.INC_DEFLECTION_RATING_EQUAL_EVASION(_DEFLECT) 亦接 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION
        //    （WireDefense:179-180），即「闪避值按 X% 计入偏斜」在本引擎以「闪避 +X%」承载。零新增原语，非臆造。
        map.put(StatIds.EVASION_APPLIES_TO_DEFLECTION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));

    }
}
