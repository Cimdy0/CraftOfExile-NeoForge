package com.cimdy.coe.data.gen.stat.wirings.wire.resource;

import com.cimdy.coe.data.codec.item.mod.core.ModStatReader;

import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.resource.HealthHelper;


import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.effect.resource.RecoupStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskActiveValue;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import java.util.Set;

/** WireResource 域接线（92 条）。 */
public final class WireResource {

    private WireResource() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        map.put(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        map.put(StatIds.TO_MAX_ENERGY_SHIELD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        map.put(StatIds.TO_MAX_MANA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_MANA));
        // C3-verify: "increased Maximum Energy Shield" 仅注册 MULTIPLIED_TOTAL 变体（无 BASE）按 inc 意图用唯一乘算 effect
        map.put(StatIds.INC_MAX_ENERGY_SHIELD, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        map.put(StatIds.INC_FLASK_CHARGES_GAIN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK));
        map.put(StatIds.ADD_ENERGY_RECHARGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.ADD_HEALTH_REGENERATION_RATE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        map.put(StatIds.ADD_MAX_ENERGY_SHIELD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        map.put(StatIds.ADD_MAX_RUNIC_WARD, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RUNIC_WARD));
        map.put(StatIds.ADD_MAX_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));
        map.put(StatIds.ADD_MAX_HEALTH_REGENERATION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.ADD_MAX_MANA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_MANA));
        map.put(StatIds.ADD_MAX_SPIRIT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_SPIRIT));
        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_REGENERATE_OF_YOUR_MAX_HEALTH_PER_SECOND, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // 「在场范围内每秒回复 X% 最大生命」（存在·友军生命再生）：StatIds.PRESENCE_LIFE_REGEN（ModAdditions.PRESENCE_LIFE_REGEN
        // 已声明真实词缀路径）→ 在场友军 MAX_REGENERATION {health, regen}，target TGT_INLINE_PRESENCE_ALLIES。
        // 由 PresenceAuraStatEffect 每 tick 解析持有者 Presence 范围内友军并注入 STAT_STORE；HealthHelper.getHealthRegeneration
        // 真读 MAX_REGENERATION（{health, regen}）按每秒回血——与 StatIds 版同源（ALLIES_IN_YOUR_PRESENCE_REGENERATE_*），R4 安全。
        map.put(StatIds.PRESENCE_LIFE_REGEN, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // 「Allies in your Presence regenerate {X} Rage per second if you have gained Rage recently」：RAGE_REGEN_PER_SECOND 属性（base=0，ADD_VALUE）
        // 施加于 presence 内盟友（TGT_INLINE_PRESENCE_ALLIES），每 tick 由 ResourceRegistry.tickRageRegenPerSecond 经 ResourceEventHub.onHeal 回复；
        // 条件 COND_RECENTLY_GAINED_RAGE 在持有者（你）侧判 isTimerActive(RAGE_GAINED)（获怒即刷新 timer，等价「recently gained rage」）。R3 安全，零跨实体死线。
        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_REGENERATE_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY,
                presence(List.of(StatConditionIds.COND_RECENTLY_GAINED_RAGE), PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_PRESENCE_GRANT_COE_RAGE_REGEN_PER_SECOND));
        map.put(StatIds.ARCANE_SURGE_REGEN, self(StatConditionIds.COND_ARCANE_SURGE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        // 恢复更慢：改用 ARCHON_RECOVERY_PERIOD_MULT（base=1.0）放大恢复周期 —— period > 1 ⇒ 周期延长 = 真正更慢，
        // 与抬高 ARCHON_RECOVERY_RATE（rate > 1 = 更快）方向相反。StatusEngine.grantArchonRecovery 真读该属性
        // （周期 = 400 × periodMult / rate），R3 安全。
        map.put(StatIds.ARCHON_RECOVERY_PERIOD_EXPIRES_SLOWER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ARCHON_ATTRIBUTE_COE_ARCHON_RECOVERY_PERIOD_MULT));
        // 恢复更快：rate > 1 = 更快（StatusEngine:367），ADD_MULTIPLIED_BASE 抬高 rate，引擎真读 ARCHON_RECOVERY_RATE（StatusEngine:383），R3 安全。
        map.put(StatIds.ARCHON_RECOVERY_PERIOD_EXPIRES_FASTER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ARCHON_ATTRIBUTE_COE_ARCHON_RECOVERY_RATE));
        // 「Block Recovery」= 本模组的 Archon 格挡恢复周期：proposed_wiring tmpl="{X}% increased Block Recovery"，
        // 引擎 StatusEngine:365-367 语料「Archon recovery period expires X% faster/slower」= 失去 Archon 后的 400t 恢复冷却，由 ARCHON_RECOVERY_RATE 缩放。
        // 复用已 live 的 ARCHON_RECOVERY_RATE EFF（与 ARCHON_RECOVERY_PERIOD_EXPIRES_* 同源），引擎 scopedArchonRate 在 {archon} 作用域真读（StatusEngine:383），零新增注册、零引擎改动，R3 安全。
        map.put(StatIds.INC_BLOCK_RECOVERY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ARCHON_ATTRIBUTE_COE_ARCHON_RECOVERY_RATE));
        map.put(StatIds.CHANCE_FLASK_NO_CONSUME, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_NO_CONSUME_CHANCE_FLASK));
        // 使用药剂不消耗充能几率：复用 CHANCE_FLASK_NO_CONSUME 同源 EFF（ADD_VALUE 作用于 FLASK_NO_CONSUME_CHANCE，引擎真读），R3 安全。
        map.put(StatIds.CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_NO_CONSUME_CHANCE_FLASK));
        map.put(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_IF_YOU_VE_BEEN_STUNNED_RECENTLY, stat(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_STUN, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHEN_NOT_ON_FULL_HEALTH, stat(StatConditionIds.COND_FULL_LIFE_LIVING_STATE_2, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, stat(StatConditionIds.COND_ARCHON_LIVING_STATE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED, stat(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.FLASK_MANA_RECOVERY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        map.put(StatIds.FLASK_NO_CONSUME, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_NO_CONSUME_CHANCE_FLASK));
        map.put(StatIds.INC_AMOUNT_LEECH_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING));
        map.put(StatIds.INC_AMOUNT_OF_MANA_LEECH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_LEECHING_MANA));
        map.put(StatIds.INC_ARMOUR_ENERGY, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        map.put(StatIds.INC_ENERGY_REGENERATION_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_ENERGY_REGEN));
        map.put(StatIds.INC_FASTER_START_ENERGY_SHIELD, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.INC_FASTER_START_ENERGY_SHIELD_FASTERESRECHARGESTART, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK));
        // 药剂充能消耗倍率：base=1 倍率属性 FLASK_CHARGES_USED，引擎 drink() 真读 {FLASK, 资源} 作用域倍率，R3 安全。
        map.put(StatIds.INC_FLASK_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_USED_FLASK));
        // 药剂/护符每秒充能：零基属性 FLASK_CHARGE_RATE / CHARM_CHARGE_RATE，引擎 tick() 真读小数累积，R3 安全。
        map.put(StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_CHARGE_RATE_HEALTH));
        map.put(StatIds.MANA_FLASKS_GAIN_CHARGES_PER_SECOND, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_CHARGE_RATE_MANA));
        map.put(StatIds.CHARMS_GAIN_CHARGES_PER_SECOND, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHARM_CHARGE_RATE_CHARM));
        map.put(StatIds.INC_FLASK_LIFE_RECOVERY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        map.put(StatIds.INC_FLASK_MANA_RECOVERY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        map.put(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK));
        map.put(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH, self(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        map.put(StatIds.INC_ICE_CRYSTAL_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ICE_CRYSTAL_MINECRAFT_MAX_HEALTH));
        map.put(StatIds.INC_LEECH_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_HEALTH_LEECHING));
        map.put(StatIds.INC_LEECH_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_LEECH_RATE_LEECHING));
        map.put(StatIds.INC_MANA_REGENERATION_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        map.put(StatIds.INC_MAX_ENERGY, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        map.put(StatIds.INC_MAX_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));
        map.put(StatIds.INC_MAX_MANA, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_MAX_MANA));
        map.put(StatIds.INC_MAX_RAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE));
        map.put(StatIds.INC_MAX_VOLATILITY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_MAX_VOLATILITY_VOLATILITY));
        map.put(StatIds.INC_MINION_MAX_HEALTH, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_MINION));
        map.put(StatIds.INC_REGENERATE_LIFE_AFFECTED_ANY, stat(StatConditionIds.COND_LIVING_STATE_RECENTLY_TOOK_DOT, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED, stat(StatConditionIds.COND_LIVING_STATE_RECENTLY_TOOK_DOT, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_LOW, stat(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_LOW_WHILE_ON, stat(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_SURROUNDED, stat(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND, stat(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_TOTEM, stat(StatConditionIds.COND_COUNT_RECENTLY_USE_TOTEM, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN_TOTEM));
        map.put(StatIds.INC_REGENERATE_LIFE_TOTEM_WHILE_YOU, stat(StatConditionIds.COND_COUNT_RECENTLY_USE_TOTEM, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN_TOTEM));
        map.put(StatIds.INC_REGENERATE_LIFE_VE_FLASK, stat(StatConditionIds.COND_COUNT_RECENTLY_USE_ITEM, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_FLASK_HEALTH_REGEN));
        map.put(StatIds.INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND, stat(StatConditionIds.COND_COUNT_RECENTLY_USE_ITEM, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_FLASK_HEALTH_REGEN));
        map.put(StatIds.INC_SLOWER_START_ENERGY_SHIELD, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        map.put(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RECOVERY_RATE));
        map.put(StatIds.INC_SPIRIT, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_SPIRIT));
        map.put(StatIds.INC_TOTEM_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_TOTEM));
        map.put(StatIds.LEECH_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_HEALTH));
        map.put(StatIds.LEECH_MANA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_MANA));
        map.put(StatIds.META_ENERGY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_META_ENERGY_MAX_META));
        // 无条件版「元技能获得 inc 能量」：与条件变体共用 META_ENERGY_MAX 读点（ResourceRegistry 真读，:59 封顶），纯数据接线零新原语。
        map.put(StatIds.META_SKILLS_GAIN_INC_ENERGY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_META_ENERGY_MAX_META));
        // INVOKED_SKILLS_HAVE_INC_MAX_ENERGY → 施法（invocation）技能 inc 最大元能量，复用 META_ENERGY_MAX {META} 读点（ResourceRegistry:59 真读）。零新增 EFF/COND。
        map.put(StatIds.INVOKED_SKILLS_HAVE_INC_MAX_ENERGY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_META_ENERGY_MAX_META));
        // 元技能触发期间不获能：INERT 标记门控 MetaSkillAction#execute 能量获取（机制就绪待数据）。
        map.put(StatIds.INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS, self(StatEffectIds.EFF_INVOCATION_ENERGY_GATE));
        map.put(StatIds.MINIONS_HAVE_INC_HEALTH_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RECOVERY_RATE));
        map.put(StatIds.MINIONS_REGENERATE_OF_MAX_HEALTH_PER_SECOND, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // 召唤物存在时长：SpawnAction.spawnMinion 写 MINION_EXPIRE 前乘 MINION_DURATION（base=1，inc 时长走 ADD_MULTIPLIED_BASE），仅作用于临时召唤物路径。
        map.put(StatIds.INC_MINION_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_MINION_DURATION));
        // 眩晕门槛基数来源：stun 门槛改用能量护盾（POE2「基于能量护盾而非生命」）；ResourceRegistry.stunThreshold 读取 STUN_THRESHOLD_FROM_ES（base=0 → ADD_VALUE）。
        map.put(StatIds.STUN_THRESHOLD_IS_BASED_ON_OF_YOUR_ENERGY_SHIELD_INSTEAD_OF_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STUN_THRESHOLD_FROM_ES));
        // 物理伤害减免（base=0，ADD_VALUE）：随从/图腾受害方承伤作用域经主语标签命中；DamageHelper.applyDamageReductions 真读取。
        map.put(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PHYSICAL_DAMAGE_REDUCTION_MINION));
        map.put(StatIds.TOTEMS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PHYSICAL_DAMAGE_REDUCTION_TOTEM));
        map.put(StatIds.MORE_AMOUNT_LEECH, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_LEECHING));
        // "100% more Maximum Rage"：MORE_ 前缀须走 ADD_MULTIPLIED_TOTAL（MAX_RAGE base=50 非零基，inc 变体不适用）。
        map.put(StatIds.MORE_MAX_RAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_RAGE));
        map.put(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_CREATE_AN_OFFERING, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_OFFERING));
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_HAVE_BEEN_HIT_RECENTLY, stat(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_HIT, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_STATIONARY, stat(StatConditionIds.COND_LIVING_STATE_STATIONARY, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // 「被晕时每秒回复 X% 最大生命」——镜像 REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_STATIONARY，COND 改用 COND_STUN_STATUS（轻晕 STUN 亦算，非仅重晕）。EFF 复用同源 MAX_REGEN {HEALTH,REGEN}（HealthHelper:22 真读）。
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED, stat(StatConditionIds.COND_STUN_STATUS, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.TO_MAX_RAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE));
        map.put(StatIds.TOTEMS_REGENERATE_OF_MAX_HEALTH_PER_SECOND, relation(RelationCondition.Relation.TOTEM, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, self(StatConditionIds.COND_MOVEMENT_STATIONARY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        map.put(StatIds.MORE_SPIRIT, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_SPIRIT));
        map.put(StatIds.TO_MAX_RAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE));
        // 武器持有门控：主手持斧 → COND_WIELDING_AXE（判定经 ItemTypeChecker.isAxe）。
        // 复用 TO_MAX_RAGE 同族 ADD_VALUE op（TO_ 前缀 = 平加，base=50 非零基）。
        map.put(StatIds.TO_MAX_RAGE_WHILE_WIELDING_AN_AXE, self(StatConditionIds.COND_WIELDING_AXE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE));
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_SHOCKED, self(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        // 同构扩展：非低蓝时 INC 法力回复 → 通用 !low_mana 取反 COND（REGENERATION_RATE base=1 非零基，MULTIPLIED_BASE 安全）。
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_NOT_ON_LOW_MANA, self(StatConditionIds.COND_NOT_LOW_MANA_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        // TO_MAX_HEALTH → ADD_VALUE op（与 TO_MAX_RAGE 同族）EFF 已确认（行 410）。
        map.put(StatIds.TO_MAX_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH));
        // INC_ENERGY_SHIELD_RECHARGE_RATE → ENERGY_RECHARGE（行 218：{ENERGY}）。
        map.put(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        // INC_FLASK_HEALTH_RECOVERY_RATE → FLASK_RECOVERY_RATE（行 246：{HEALTH,FLASK}）。
        map.put(StatIds.INC_FLASK_HEALTH_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        // INC_HEALTH_REGENERATION_RATE_DURING_EFFECT_OF_ANY_HEALTH_FLASK → 同上 FLASK_RECOVERY_RATE（{HEALTH,FLASK}）。
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_DURING_EFFECT_OF_ANY_HEALTH_FLASK, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        // INC_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK → FLASK_RECOVERY_RATE（行 247：{MANA,FLASK}）。
        map.put(StatIds.INC_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        // 「{X}% increased Life Recovery rate」——INC_FLASK_HEALTH_RECOVERY_RATE 去掉 Flask 限定后的通用版。
        // 走通用 RECOVERY_RATE（与 INC_SPEED_OF_RECOUP_EFFECTS、MINIONS_HAVE_INC_HEALTH_RECOVERY_RATE 同源），
        // 而非 FLASK_RECOVERY_RATE——后者带 {HEALTH,FLASK} 标签，只在药剂回复路径生效，语义更窄。
        map.put(StatIds.INC_HEALTH_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RECOVERY_RATE));
        // 魔力回复速率（INC/MORE）：走 RECOVERY_RATE 的 {mana,regen} 作用域变体（ManaHelper:67 实读，R3 安全）。
        map.put(StatIds.INC_MANA_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RECOVERY_RATE_MANA_REGEN));
        map.put(StatIds.MORE_MANA_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_RECOVERY_RATE_MANA_REGEN));
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        map.put(StatIds.INC_MANA_REGENERATION_RATE_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));
        map.put(StatIds.INC_AMOUNT_OF_HEALTH_LEECH_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING));
        map.put(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        // 4) 低血/被包围时 每秒最大生命回复 —— 复用 REGENERATE_OF_MAX_HEALTH_PER_SECOND 同源 EFF（, TGT_INLINE_SELF, ADD_VALUE 速率）切对应 COND。
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_ON_LOW_HEALTH, stat(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_SURROUNDED, stat(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT → 自侧持任意"伤害性异常"（ailment∩damaging = ignite/bleed/poison；corrupted_blood 缺 "ailment" 标签、控制异常缺 DAMAGING，均排除）+ 复用同源 MAX_REGEN EFF（ADD_VALUE 速率）。
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT, stat(List.of(StatConditionIds.COND_ANY_AILMENT, StatConditionIds.COND_ANY_DAMAGING), StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // ---- op 变体补全（数据驱动需求分析，见 poe2_assets/opgap_demand.json）----
        // 仅补「属性已存在、仅缺 op 变体」且 base≠0 的缺口；零基项（RESERVATION_EFFICIENCY/BLOCK_CHANCE/
        // AILMENT_THRESHOLD/PUPPET_MASTER_DURATION/MAX_ENERGY_SHIELD/ARMOR）按三定律 #3 一律不补。
        // 1) 药剂回复的 more/less 分支 —— 复用 INC_HEALTH_RECOVERY_FROM_FLASKS 同源（FLASK_RECOVERY_RATE
        //    + {HEALTH,FLASK}），op 由 BASE 换 TOTAL。FLASK_RECOVERY_RATE base=1.0 非零基。
        //    ⚠️ 极性待办：MORE_HEALTH_RECOVERY_FROM_FLASKS 语料为 "50% less ..."（负向），而语料归一化把
        // less→more 且未变号（value=+0.5）。接线 op 族正确，方向由注入侧 value 符号决定，
        // 须在天赋树数据接入前一并修 polarity_audit.json 所列 42 条冲突项。
        map.put(StatIds.MORE_HEALTH_RECOVERY_FROM_FLASKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        map.put(StatIds.MORE_RECOVERY_FROM_FLASKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        // 2) 生命回复速率的 increased 分支 —— 与 ENERGY_REGEN / MANA_REGEN 两个 BASE 变体同构，
        //    仅缺 HEALTH 分支。REGENERATION_RATE base=1.0 非零基。
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // 语料 "10% increased amount of Life Leeched"；与已接线的 INC_AMOUNT_LEECH_HEALTH 同义（同 EFF）
        map.put(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING));
        // 语料 "20% increased Life Regeneration Rate while moving"；移动为自身状态 -> self
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // 语料 "15% increased Life Regeneration Rate while on Low Life"；与 REGENERATE_..._WHILE_ON_LOW_HEALTH 先例同构（TGT_INLINE_SELF）
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH, stat(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // WHILE_SHAPESHIFTED —— 镜像 INC_HEALTH_REGENERATION_RATE（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // 点燃时生命回复速率（复用既有 COND_IGNITE_STATUS + HEALTH_REGEN EFF，零新增注册）
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_IGNITED, self(StatConditionIds.COND_IGNITE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // 静止时生命回复速率（复用 COND_MOVEMENT_STATIONARY，与 MANA 变体同族）
        map.put(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, self(StatConditionIds.COND_MOVEMENT_STATIONARY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY —— 镜像 INC_MAX_ENERGY_SHIELD（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_MAX_ENERGY_SHIELD_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, self(StatConditionIds.COND_CONSUME_POWER_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));

        // 使敌人感电后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_MANA_REGENERATION_RATE_IF_YOU_HAVE_SHOCKED_AN_ENEMY_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN));

        // hybrid：最大生命 + 魔力 + 能量护盾 共享同一 X%
        map.put(StatIds.INC_MAX_HEALTH_MANA_AND_ENERGY_SHIELD, groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_MAX_MANA, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        // hybrid：药剂生命 + 魔力回复 共享同一 X%
        map.put(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));

        // hybrid：药剂 + 护符 充能获取 共享 X%
        map.put(StatIds.INC_FLASK_AND_CHARM_CHARGES_GAIN, groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_GAINED));

        // ── 药剂 / 护符显式词缀接线 ──
        // 这些 ModStatIds 由 ExplicitFlaskModGen 经 AdditionType.global 产出，故必须在此补
        // 「源 stat id → 既有 EFF」映射，否则全为死线（compile 绿 ≠ 接线活）。零新增原语/属性。
        // 资源双作用域项用 groupSelf 同时命中生命/魔力药剂（affix 注册在父类层 FLASK_MOD，两类通吃）。
        // 每个目标 EFF 的作用域与 FlaskHelper/CharmEngine 的真读点一一对应（见各条注释）。
        // 1) 充能获取（{flask}）：FlaskHelper.tickRecharge / onKill 真读 CHARGES_GAINED {FLASK,资源}。
        map.put(StatIds.FLASK_CHARGES_GAINED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK));
        // 2) 充能消耗（{flask}）：FlaskHelper.drink 真读 CHARGES_USED {FLASK,资源}（"reduced" 由条目负值承载）。
        map.put(StatIds.FLASK_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_USED_FLASK));
        // 3) 回复总量（#% increased Amount Recovered）：落 FLASK_RECOVERY_RATE 的 {health,flask}∪{mana,flask} 双作用域，
        //    FlaskHelper.drink 在该属性 {FLASK,资源} 作用域读取 amountMult。
        map.put(StatIds.FLASK_RECOVERY_AMOUNT, groupSelf(
                StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH,
                StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        // 4) 回复速率（#% increased Recovery rate / Duration）：落通用 RECOVERY_RATE（FlaskHelper.drink 读 rateMult）。
        map.put(StatIds.FLASK_RECOVERY_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RECOVERY_RATE));
        // 5b) 充能率（"#% increased Charge Recovery" / 药剂充能率）：FLASK_RECHARGE_RATE → CHARGE_RATE {flask} 零基 ADD_VALUE。
        //     FlaskHelper.tickRecharge:326 真读 CHARGE_RATE（reads {FLASK,资源}，{flask} 为超集经 merged scope 命中）→
        //     每秒被动充能按速率小数累积；与 StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND（落 {health,flask}）同源，R4 安全。
        map.put(StatIds.FLASK_RECHARGE_RATE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHARGE_RATE_FLASK));
        // 5) 每秒充能（Gains # Charges per Second）：落 CHARGE_RATE 的 {health,flask}∪{mana,flask} 双作用域，
        //    FlaskHelper.tickRecharge 读取 FLASK_CHARGE_RATE（=CHARGE_RATE {FLASK,资源}）。
        map.put(StatIds.FLASK_GAIN_CHARGE, groupSelf(
                StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_CHARGE_RATE_HEALTH,
                StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_CHARGE_RATE_MANA));
        // 6) 授予 Guard（Also grants # Guard）：复用 EFF_FLASK_GUARD 标记，FlaskHelper.drink 经 StatEngine.magnitudeOf 真读。
        map.put(StatIds.FLASK_GRANT_GUARD, self(StatEffectIds.EFF_FLASK_GUARD));
        // 7) 护符使用即时回血/回魔（charm 池「Recover # Life/Mana when Used」）：CharmEngine.drink 发射 USE_CHARM 事件，
        //    ResourceStatEffect 在 {use_charm, charm} 标签上即时结算 flat 数值（= 词缀 roll 值）。
        map.put(StatIds.FLASK_RECOVER_LIFE, self(StatEffectIds.EFF_CHARM_HEAL_HEALTH_USE_CHARM));
        map.put(StatIds.FLASK_RECOVER_MANA, self(StatEffectIds.EFF_CHARM_HEAL_MANA_USE_CHARM));
        // ── 护符专属词缀接线（与药剂严格分流：charm 词缀须落 {charm} 作用域，绝不复用 flask ModStatId 以免污染 {flask} 读点）──
        // 每条 charm ModStatId 仅解析到对应 {charm} 作用域 EFF，与 CharmEngine 真读点一一对齐 → 注入即被消费（非虚接）。
        // 1) 持续时间（"#% increased Duration"）：落 CHARM_DURATION 的 {charm, !taken} 作用域，
        //    CharmEngine.charmDuration 在 {charm} 作用域真读 CHARM_DURATION。
        map.put(StatIds.CHARM_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_AILMENT_DURATION_TAKEN));
        // 2) 充能获取（{charm}）：CharmEngine.onKillCharge 真读 CHARGES_GAINED {charm}。
        map.put(StatIds.CHARM_CHARGES_GAINED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_GAINED));
        // 3) 充能消耗（{charm}）：CharmEngine.triggerCharm 真读 CHARGES_USED {charm}（"reduced" 由条目负值承载）。
        map.put(StatIds.CHARM_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_USED));
        // 4) 每秒充能（{charm}）：CharmEngine.tick 真读 CHARGE_RATE {charm}（= 护符每秒被动充能）。
        map.put(StatIds.CHARM_GAIN_CHARGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHARM_CHARGE_RATE_CHARM));
        // 5) 授予 Guard（charm 使用即时授予）：落 charm_guard 标记（tagless，matches 任意），
        //    CharmEngine.triggerCharm 注入后真读 magnitudeOf(player, "charm_guard", Set.of()) 并施加限时 GUARD_FROM_FLASK 状态。
        map.put(StatIds.CHARM_GRANT_GUARD, self(StatEffectIds.EFF_CHARM_GUARD));
        // 8) 药剂/护符「#% increased Charges」：落 INERT 标记（安全网，防 StatRead.resolve() 崩溃）。
        //    真实机制（动态充能上限）由 ModStatReader.readRolledPercent 直接读物品 ITEM_STAT_ENTRIES 实现，
        //    经 FlaskHelper.flaskMaxCharges / CharmEngine.charmMaxCharges 注入各充能读写点（scope 天然正确，非虚接）。
        map.put(StatIds.FLASK_NUM_CHARGES, self(StatEffectIds.EFF_FLASK_NUM_CHARGES));
        // 9) 药剂/护符「#% Chance to gain a Charge when you kill」：落 INERT 标记（安全网）。
        //    真实机制由 FlaskHelper.onKill / CharmEngine.onKillCharge 经 readRolledPercent 读几率分支实现（每槽独立掷骰）。
        map.put(StatIds.FLASK_CHANCE_GAIN_CHARGE_ON_KILL, self(StatEffectIds.EFF_FLASK_CHANCE_GAIN_CHARGE_ON_KILL));

        // ── 腰带 / 资源类词缀接线 ──
        // 腰带词缀与 flask/charm 同语义但独立成路径（BELT_* 专用 ModStatId），落点即对应 {flask}/{charm} 作用域 EFF，
        // 与 FlaskHelper / CharmEngine 的真读点完全同路径 → 注入即被消费。
        // 1) 「#% increased Charm Effect Duration」（腰带）：落 CHARM_DURATION 的 {charm} 作用域，
        //    CharmEngine.charmDuration 真读（与 StatIds.CHARM_DURATION 同 EFF，零新增原语）。
        map.put(StatIds.BELT_CHARM_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_AILMENT_DURATION_TAKEN));
        // 2) 「#% increased Charm Charges gained」（腰带）：CharmEngine.onKillCharge 真读 CHARGES_GAINED {charm}。
        map.put(StatIds.BELT_CHARM_CHARGES_GAINED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_GAINED));
        // 3) 「#% reduced Charm Charges used」（腰带）：CharmEngine.triggerCharm 真读 CHARGES_USED {charm}（"reduced" 由条目负值承载）。
        map.put(StatIds.BELT_CHARM_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_USED));
        // 4) 「#% increased Flask Charges gained」（腰带）：FlaskHelper.tickRecharge / onKill 真读 CHARGES_GAINED {FLASK,资源}。
        map.put(StatIds.BELT_FLASK_CHARGES_GAINED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_FLASK));
        // 5) 「#% reduced Flask Charges used」（腰带）：FlaskHelper.drink 真读 CHARGES_USED {FLASK,资源}（"reduced" 由条目负值承载）。
        map.put(StatIds.BELT_FLASK_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_USED_FLASK));
        // 6) 「#% increased Flask Life Recovery rate」（腰带）：FlaskHelper.drink 真读 FLASK_RECOVERY_RATE {HEALTH,FLASK}。
        map.put(StatIds.BELT_FLASK_LIFE_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH));
        // 7) 「Gain # Life per enemy killed」：KILL 事件即时回血，ResourceStatEffect(HEALTH,HEAL,{kill})，与 StatIds.LIFE_PER_KILL 同落点。
        map.put(StatIds.LIFE_GAINED_FROM_ENEMY_DEATH, self(StatEffectIds.EFF_HEAL_HEALTH_KILL));
        // 8) 「Gain # Mana per enemy killed」：同上 KILL 事件，MANA 通道（与 StatIds.RECOVER_PCT_MANA_ON_KILL 同落点）。
        map.put(StatIds.MANA_GAINED_FROM_ENEMY_DEATH, self(StatEffectIds.EFF_HEAL_KILL_MANA));
        // 9) 「Grants # Life per Enemy Hit」：CAUSED_HIT 事件即时回血（#241 新增最小原语 EFF_CAUSED_HIT_HEAL_HEALTH）。
        map.put(StatIds.LIFE_GAIN_PER_TARGET, self(StatEffectIds.EFF_CAUSED_HIT_HEAL_HEALTH));
        // 10) 「Leeches #% of Physical Damage as Life」：LEECH 属性 {health} 作用域 ADD_VALUE（base=0），
        //     ⚠️ 既有 LEECH 读点无 physical 细分标签，故本接线复用 StatIds.LEECH_HEALTH 同款 EFF（丢掉 physical 限定）。
        map.put(StatIds.LEECH_PHYSICAL_DAMAGE_AS_LIFE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_HEALTH));
        // 10b) 「Leeches #% of Physical Damage as Mana」：LEECH 属性 {mana} 作用域 ADD_VALUE（复用既有 EFF，零新增原语）。
        //      ⚠️ 本 id 为修复 ExplicitModGen 中 ManaLeech 家族误引 LEECH_PHYSICAL_DAMAGE_AS_LIFE（生命）而新增（#241 主理人批准，仅此一行常量）。
        map.put(StatIds.LEECH_PHYSICAL_DAMAGE_AS_MANA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_MANA));
        // 11) 「#% of Damage taken Recouped as Life」：RecoupStatEffect(HEALTH)，承伤补给真读点（与 StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH 同落点）。
        map.put(StatIds.DAMAGE_TAKEN_GAINED_AS_LIFE, self(StatEffectIds.EFF_HEALTH_RECOUP));
        // 12) 「#% of Damage taken Recouped as Mana」：RecoupStatEffect(MANA)（与 StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA 同落点）。
        map.put(StatIds.PERCENT_DAMAGE_GOES_TO_MANA, self(StatEffectIds.EFF_MANA_RECOUP));
        // ── 以下 5 条当前<b>无任何词缀引用</b>（仅 ModStatIds 声明），接线纯粹为防未来引用时成为死线 ──
        // 13) 「#% reduced Flask Mana Recovery rate」（腰带）：FLASK_RECOVERY_RATE {MANA,FLASK}（FlaskHelper.drink 真读）。
        map.put(StatIds.BELT_FLASK_MANA_RECOVERY_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        // 14) 「#% increased Flask Recovery rate」（腰带，泛化版）：生命/魔力双作用域共享同一 X%。
        map.put(StatIds.BELT_FLASK_RECOVERY_RATE, groupSelf(
                StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH,
                StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        // 15) 「Grants # Life per Enemy Hit with Attacks」：与 LIFE_GAIN_PER_TARGET 同落点（CAUSED_HIT 即时回血）。
        map.put(StatIds.LIFE_GAIN_PER_TARGET_ATTACKS, self(StatEffectIds.EFF_CAUSED_HIT_HEAL_HEALTH));
        // 16) 「Leech #% of Physical Attack Damage as Life」：与 LEECH_PHYSICAL_DAMAGE_AS_LIFE 同落点（LEECH {health}）。
        map.put(StatIds.LEECH_PHYSICAL_ATTACK_DAMAGE_AS_LIFE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LEECH_HEALTH));
        // 17) 「#% increased maximum Mana」：MAX_MANA（base=40，ADD_MULTIPLIED_BASE），与 StatIds.INC_MAX_MANA 同落点。

        // 补给：承伤按比例转为魔力（纯数据变体）
        map.put(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA, self(StatEffectIds.EFF_MANA_RECOUP));
        // 补给：物理承伤转为生命
        map.put(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, self(StatEffectIds.EFF_HEALTH_PHYSICAL_RECOUP));
        // 补给：元素承伤转为生命
        map.put(StatIds.OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, self(StatEffectIds.EFF_HEALTH_ELEMENTAL_RECOUP));
        // 补给：承伤同时转为 生命+魔力+能量护盾（共享同一 X%，故用组合条目）
        map.put(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_MANA_AND_ENERGY_SHIELD, groupSelf(StatEffectIds.EFF_HEALTH_RECOUP, StatEffectIds.EFF_MANA_RECOUP, StatEffectIds.EFF_ENERGY_RECOUP));

        // 汲取速率：镜像 INC_LEECH_RATE（语料 faster 为正、slower / less quickly 已取负）
        map.put(StatIds.HEALTH_LEECH_HEALTH_FASTER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_LEECH_RATE_LEECHING));
        map.put(StatIds.HEALTH_LEECH_HEALTH_SLOWER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_LEECH_RATE_LEECHING));
        map.put(StatIds.LEECH_HEALTH_MORE_QUICKLY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_LEECH_RATE_LEECHING));
        // 汲取总量（MORE_ → TOTAL）
        map.put(StatIds.MORE_AMOUNT_OF_HEALTH_LEECHED, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING));

        // 魔力药剂充能获取
        map.put(StatIds.INC_MANA_FLASK_CHARGES_GAIN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_CHARGES_GAINED_MANA));

        // 魔力药剂回复
        map.put(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));
        // 能量护盾回复开始加快
        map.put(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        // 能量护盾回复开始减慢（值已取负）
        map.put(StatIds.SLOWER_START_OF_ENERGY_SHIELD_RECHARGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY));
        // 技能魔力消耗转为生命消耗
        map.put(StatIds.SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, self(StatEffectIds.EFF_HEALTH_MANA_REPLACE_RESOURCE_CONVERT));
        // 法术魔力消耗转为生命消耗（镜像技能魔力转生命，复用同一 RESOURCE_CONVERT，零新增读点）
        map.put(StatIds.OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST, self(StatEffectIds.EFF_HEALTH_MANA_REPLACE_RESOURCE_CONVERT));
        // 技能魔力消耗转为生命消耗（任一生命药水生效中）——镜像 OF_SPELL_MANA_COST 变体，复用同一 RESOURCE_CONVERT，
        // 门控 COND_FLASK_ACTIVE_HEALTH 读 FLASK_ACTIVE 附件（FlaskHelper.tick 真实填充，R3 安全），零新增读点。
        map.put(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS_DURING_ANY_HEALTH_FLASK_EFFECT, self(StatConditionIds.COND_FLASK_ACTIVE_HEALTH, StatEffectIds.EFF_HEALTH_MANA_REPLACE_RESOURCE_CONVERT));

        // INC_RESERVATION_EFFICIENCY 的 COMPANION 作用域变体
        map.put(StatIds.INC_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_COMPANION_RESERVATION));
        // INC_RESERVATION_EFFICIENCY 的 HERALD 作用域变体
        map.put(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_HERALD_RESERVATION));
        // INC_RESERVATION_EFFICIENCY 的 MINION 作用域变体
        map.put(StatIds.INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_MINION_RESERVATION));
        // ── 魔力消耗效率（Mana Cost Efficiency）族：零基属性 MANA_COST_EFFICIENCY（base=0 → ADD_VALUE），
        // 读点 ResourceCost.runCost/matches 仅对 MANA 消耗按 (1 - 值) 缩放。command/mark 经技能标签作用域分流，
        // low_mana 经 COND_LOW_MANA_LIVING_STATE 门控。flask-effect 变体经 COND_FLASK_ACTIVE_MANA
        // （FlaskActiveValue 读 FLASK_ACTIVE 附件，R3 读点 FlaskHelper.tick 真实填充）解锁，见下方接线。
        map.put(StatIds.INC_MANA_COST_EFFICIENCY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));
        // 「技能生命消耗提高 X%」——落点 HEALTH_COST_EFFICIENCY（base=0，ADD_VALUE）；读点 ResourceCost.effectiveCost 仅对 HEALTH 消耗按 (1 - 值) 缩放（负值=提高消耗，与 MANA 同源范式）。
        map.put(StatIds.INC_HEALTH_COST_OF_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_HEALTH_COST_EFFICIENCY));
        // 「被调用法术消耗 X% 更多能量」——落点 INVOKED_SPELLS_CONSUME_MORE_ENERGY（base=0，ADD_VALUE）；读点 MetaSkillAction.execute
        // 按 (1 + 值) 缩放元技能触发能耗（ResourceRegistry 能量消耗，唯一 invocation 能量消费者，R3 非死线）。
        map.put(StatIds.INVOKED_SPELLS_CONSUME_MORE_ENERGY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INVOKED_SPELLS_CONSUME_MORE_ENERGY));
        map.put(StatIds.INC_MANA_COST_EFFICIENCY_OF_COMMAND_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY_COMMAND));
        map.put(StatIds.INC_MANA_COST_EFFICIENCY_OF_MARKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY_MARK));
        map.put(StatIds.INC_MANA_COST_EFFICIENCY_WHILE_ON_LOW_MANA, self(StatConditionIds.COND_LOW_MANA_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));
        // ── 药水效果生效中（COND_FLASK_ACTIVE_* 读 FLASK_ACTIVE 附件；R3 读点 FlaskHelper.tick 真实填充）──
        // 注：INC_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK 另经 FLASK_RECOVERY_RATE（{mana,flask}，FlaskHelper 实读）接线，勿重复。
        // 任一魔力药水生效中 + 攻击技能：魔力消耗效率 +X%（{ATTACK} 作用域，读点 ResourceCost 按 (1-值) 缩放 MANA 消耗）
        map.put(StatIds.INC_MANA_COST_EFFICIENCY_OF_ATTACKS_DURING_ANY_MANA_FLASK_EFFECT, self(StatConditionIds.COND_FLASK_ACTIVE_MANA, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY_ATTACK));
        // 「X% increased Cost Efficiency」——通用（无任何技能标签作用域）魔力消耗效率，读点 ResourceCost 按 (1-值) 缩放全体技能 MANA 消耗。
        map.put(StatIds.INC_COST_EFFICIENCY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));
        // 「X% increased Cost Efficiency of Attacks」——{ATTACK} 作用域变体，仅攻击技能生效。
        map.put(StatIds.INC_COST_EFFICIENCY_OF_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY_ATTACK));
        // 「魔力消耗翻倍」——MANA_COST_EFFICIENCY 取 -1.0 ⇒ (1 - (-1.0)) = 2 ⇒ 消耗×2（ResourceCost:122 `raw * max(0, 1 - eff)`）。
        map.put(StatIds.MANA_COSTS_ARE_DOUBLED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));
        // 「技能魔力消耗提高 X%」——提高消耗 = 降低效率，magnitude 取负值（同一 MANA_COST_EFFICIENCY 属性，读点 ResourceCost 按 (1-值) 缩放全体技能 MANA 消耗）。
        map.put(StatIds.INC_MANA_COST_OF_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));
        // 64601/49363 wildsurge_incantation：风暴+植物法术消耗（storm_and_plant_spells_cost_more，节点值 -0.5）
        // → STORM 与 PLANT 双作用域复用 MANA_COST_EFFICIENCY 读点（ResourceCost 按 (1-eff) 缩放）；
        // 节点值 -0.5 ⇒ 风暴/植物法术各 +50% 魔力消耗（EFF 命名 "less_cost" 仅其原型 +0.5 语义，幅度由节点值承载）。
        map.put(StatIds.STORM_AND_PLANT_SPELLS_COST_MORE,
                groupSelf(StatEffectIds.EFF_STORM_SPELLS_HAVE_50_PERCENT_LESS_COST, StatEffectIds.EFF_PLANT_SPELLS_HAVE_50_PERCENT_LESS_COST));
        // 64601/49363 wildsurge_incantation：风暴+植物法术持续时间（storm_and_plant_spells_have_more_duration，节点值 -0.75）
        // → STORM 与 PLANT 双作用域复用 SPELL_DURATION_MULT（ADD_MULTIPLIED_TOTAL，base=1.0）；
        // 节点值 -0.75 ⇒ 风暴/植物法术时长 ×(1-0.75)=0.25（即 75% less，EFF 命名 "less_duration" 即此语义）。
        map.put(StatIds.STORM_AND_PLANT_SPELLS_HAVE_MORE_DURATION,
                groupSelf(StatEffectIds.EFF_STORM_SPELLS_HAVE_75_PERCENT_LESS_DURATION, StatEffectIds.EFF_PLANT_SPELLS_HAVE_75_PERCENT_LESS_DURATION));

        // INC_MAX_HEALTH 的 COMPANION 作用域变体
        map.put(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MAX_HEALTH_COMPANION));
        // INC_GLORY_GENERATION 的 BANNER 作用域变体
        map.put(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_GLORY_GENERATION_BANNER_GLORY));
        // INC_RESERVATION_EFFICIENCY 的 SPIRIT 作用域变体
        map.put(StatIds.INC_SPIRIT_RESERVATION_EFFICIENCY_OF_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_RESERVATION_SPIRIT));
        // INC_RESERVATION_EFFICIENCY 的 META 作用域变体
        map.put(StatIds.META_SKILLS_HAVE_INC_RESERVATION_EFFICIENCY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_META_RESERVATION));
        // 「30% more Reservation Efficiency of Companion Skills」（vals=[0.30]）。
        // 落点仍走 ADD_VALUE 而非 ADD_MULTIPLIED_TOTAL——RESERVATION_EFFICIENCY base = 0.0
        // （AttributeRegistry:143 registerPercentage(..., 0.0)），按<b>三定律 #3 零基禁 inc</b>，
        // 任何乘算（BASE/TOTAL）对零基恒为 0，是死接线。本文件上方 INC_/META/SPIRIT 变体一律走 ADD_VALUE，
        // 即该属性被编码成「加成池」语义：+0.30 = 效率 +30%。MORE_ 前缀在此与 INC_ 同落点是该约定的直接推论，
        // 不是近似顶替——差别只在于语措，数值语义一致。
        map.put(StatIds.MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_COMPANION_RESERVATION));
        // 「more Reservation Efficiency of non-Companion Skills」——排除语义走 !companion GateExpr，
        // 与上方 COMPANION 正作用域变体互补（二者无重叠，不是近似顶替）。
        map.put(StatIds.MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_NON_COMPANION_RESERVATION));
        // 「non-Minion Skills have X% more Reservation Efficiency」——!minion GateExpr（RESERVATION_EFFICIENCY base=0，ADD_VALUE）
        map.put(StatIds.NON_MINION_SKILLS_HAVE_MORE_RESERVATION_EFFICIENCY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_NON_MINION_RESERVATION));
        // INC_MAX_HEALTH 的 MINION 作用域变体（复用既有 EFF）
        map.put(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_MINION));
        // INC_MAX_HEALTH 的 OFFERING 作用域变体（复用既有 EFF）
        map.put(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_OFFERING));
        // META_SKILLS_GAIN_INC_ENERGY_IF_YOU_VE_DEALT_A_CRITICAL_HIT_DAMAGE_RECENTLY → META_ENERGY_MAX {META}（ResourceRegistry 真读）
        map.put(StatIds.META_SKILLS_GAIN_INC_ENERGY_IF_YOU_VE_DEALT_A_CRITICAL_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_CRIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_META_ENERGY_MAX_META));
        // MAX_VOLATILITY_IS → SET_VALUE 覆盖 volatility 上限
        map.put(StatIds.MAX_VOLATILITY_IS, self(StatEffectIds.EFF_ATTRIBUTE_COE_MAX_VOLATILITY_SET_VALUE));
        // MAX_HEALTH_IS → SET_VALUE 覆盖生命上限
        map.put(StatIds.MAX_HEALTH_IS, self(StatEffectIds.EFF_ATTRIBUTE_COE_MAX_HEALTH_SET_VALUE));
        // 无蓝：覆盖 MAX_MANA 为条目值（0）。
        map.put(StatIds.YOU_HAVE_NO_MANA, self(StatEffectIds.EFF_MAX_MANA_SET_VALUE));

    }
}
