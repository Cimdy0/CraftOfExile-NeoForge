package com.cimdy.coe.data.gen.stat.wirings.wire.attribute;

import com.cimdy.coe.data.gen.stat.StatGen;

import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.gen.stat.wirings.StatBuilder;
import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.damage.DamageStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskUsesValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import com.cimdy.coe.event.combat.DamageEvent;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.wire.resource.WirePerScaling.perRage;
import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/** WireDamage 域接线（152 条）。 */
public final class WireDamage {

    private WireDamage() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        map.put(StatIds.INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        // ── 药水效果生效中（COND_FLASK_ACTIVE_* 读 FLASK_ACTIVE 附件；R3 读点 FlaskHelper.tick 真实填充）──
        // 任意药水生效中：伤害 +X%（复用通用 DAMAGE 增益 EFF）
        map.put(StatIds.INC_DAMAGE_DURING_ANY_FLASK_EFFECT, self(StatConditionIds.COND_FLASK_ACTIVE_ANY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        // 任意药水生效中：攻击技能的元素伤害 +X%（{DAMAGE, ATTACK, ELEMENTAL} 作用域）
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACK_SKILLS_DURING_ANY_FLASK_EFFECT, self(StatConditionIds.COND_FLASK_ACTIVE_ANY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        // === channelling 条件族（COND_CHANNELLING_LIVING_STATE）===
        // 「Channelling Skills deal {X}% increased Damage」：沿用 INC_DAMAGE 的全局增伤 EFF，仅在引导时生效。
        map.put(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, self(StatConditionIds.COND_CHANNELLING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        // === C3.2 token-subset 候选补接：节点 token ⊆ 已注册 effect SEG，op 意图匹配，逐条签名核验（零新增 effect 注册） ===
        map.put(StatIds.INC_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING));
        // 更多最大闪电伤害（MORE_MAX_LIGHTNING_DAMAGE）：INERT 标记，仅抬升闪电伤害偏差区间上沿；
        // 消费端 DamageHelper#rollDamageDeviation 经 StatEngine.markerMagnitude 真读（仅闪电槽位生效，下沿不动）。
        map.put(StatIds.MORE_MAX_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_MORE_MAX_LIGHTNING_DAMAGE));
        map.put(StatIds.INC_ATTACK_PHYSICAL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE_PHYSICAL));
        map.put(StatIds.INC_DAMAGE_PER_CURSE_ON_YOU, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_CURSE_DAMAGE));
        map.put(StatIds.ADD_ATTACK_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_CAST_CRIT_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT_SPELL));
        map.put(StatIds.ADD_CHAOS_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHAOS_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_COLD_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COLD_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_CRIT_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE_CRIT));
        map.put(StatIds.ADD_CRIT_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));
        map.put(StatIds.ADD_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_ELEMENTAL_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_FIRE_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_FIRE_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_LIGHT_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_LIGHTNING_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_PHYSICAL_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE_PHYSICAL));
        map.put(StatIds.ADRENALINE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.ADRENALINE_TAKEN_PHYSICAL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE_PHYSICAL));
        map.put(StatIds.ATTACKS_HAVE_TO_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.COMPANIONS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COMPANION_DAMAGE));
        // 同伴伤害加成亦作用于自身：ALLY_DAMAGE_APPLIES_TO_SELF 开关置位后，DamageHelper.applyDamageMultipliers
        // 以 {companion} 作用域读 coe:damage（捕获 coe:damage{companion,damage} 增/减伤），复用 COMPANIONS_DEAL_INC_DAMAGE 同源聚合，R3 安全。
        map.put(StatIds.INC_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_COMPANION_DAMAGE_APPLIES_TO_YOU));
        // 「increased and Reductions to Projectile Speed also apply to Damage with Bows」：弓伤害乘区取 coe:damage{bow,damage}
        // 的一个 ADD_MULTIPLIED_BASE 修饰量 = attribute:coe:projectile_speed - 1（DAMAGE base=1.0，故 1+该修饰量 == 属性值本身：
        // +30% 速度 → 弓伤害 ×1.3；-20% → ×0.8，增减同源）。弓限制由效果标签 {damage, bow} 表达——bow 由命中作用域的
        // 武器标签注入（HitScopeHelper#addWeaponTags），与 INC_DAMAGE_WITH_BOWS 同通道，二者叠加于同一 {damage, bow} 乘区。
        map.put(StatIds.INC_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS,
                wiring(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BOW_COE_DAMAGE_DAMAGE)
                        .self()
                        .amount("attribute:coe:projectile_speed - 1")
                        .build());
        // COMPANIONS_DEAL_INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES → 同伴对定身敌人增伤（镜像 INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES 同族）：
        // onTarget(COND_PIN_STATUS) 条件在受害方（被定身）求值，effect 落持有者；{COMPANION} 作用域经 DamageEvent 注入攻击者，R3 安全。零新增 EFF/COND。
        map.put(StatIds.COMPANIONS_DEAL_INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COMPANION_DAMAGE));
        // 出伤侧通用倍率模板（无类型作用域）：StatusBehavior#damageDealt(fraction) 的 $value 注入目标。
        // 与 DAMAGE_TAKEN（承伤侧）同构：本条目为出伤侧通用倍率，缺失时 StatRead 静默失败，
        // 会使 intimidate（恐吓）的「造成伤害 -X%」半边为 no-op（另一半「承受伤害 +X%」经 DAMAGE_TAKEN 生效）。
        map.put(StatIds.DAMAGE_DEALT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_BEAR_FIRE, self(StatConditionIds.COND_FORM_BEAR, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_BEAR_PHYSICAL, self(StatConditionIds.COND_FORM_BEAR, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_ELEMENTAL_SPELL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_ELEMENTAL_SPELL));
        map.put(StatIds.DAMAGE_DEALT_WOLF_COLD, self(StatConditionIds.COND_FORM_WOLF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_WOLF_PHYSICAL, self(StatConditionIds.COND_FORM_WOLF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_WYVERN_CHAOS, self(StatConditionIds.COND_FORM_WYVERN, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_DEALT_WYVERN_LIGHTNING, self(StatConditionIds.COND_FORM_WYVERN, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.DAMAGE_TAKEN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE));
        map.put(StatIds.DAMAGE_TAKEN_CHAOS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHAOS_COE_TAKEN_DAMAGE));
        map.put(StatIds.DAMAGE_TAKEN_PHYSICAL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE_PHYSICAL));
        map.put(StatIds.EMPOWERED_ATTACKS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE_EMPOWERED));
        // 「你创造的锯齿地面中的敌人受到增加伤害」→ 走攻击侧 {DAMAGE, JAGGED_GROUND}。
        // 不可用承伤侧 TAKEN_DAMAGE：条目落玩家 store，而承伤读点 sourceEntity 恒 null → 恒不生效。
        // 此处必须用 self(...) 而非 onTarget(...)：状态门控已由读点（DamagePipeline 判受害者 ON_JAGGED_GROUND）承担，
        // 而 readInScope 推入的 context sourceEntity 恒 null → triggerTarget 恒 null → onTarget 的条件恒不成立。
        map.put(StatIds.ENEMIES_IN_JAGGED_GROUND_YOU_CREATE_TAKE_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_JAGGED_GROUND));
        map.put(StatIds.FORM_WYVERN_CRIT_CHANCE, self(StatConditionIds.COND_FORM_WYVERN, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.FORM_WYVERN_CRIT_DAMAGE, self(StatConditionIds.COND_FORM_WYVERN, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));
        map.put(StatIds.GRENADE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_GRENADE));
        map.put(StatIds.HAZARD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_HAZARD));
        map.put(StatIds.HERALD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_HERALD));
        map.put(StatIds.INC_ATTACK_AREA_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AREA_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));
        // === 近期施放过法术则攻击增伤（COND_CAST_SPELL_RECENTLY 由 CAST_SKILL 事件 spell 标签驱动，R3 安全）===
        map.put(StatIds.INC_ATTACK_DAMAGE_IF_YOU_VE_CAST_A_SPELL_RECENTLY, self(StatConditionIds.COND_CAST_SPELL_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE_PHYSICAL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE_PHYSICAL));
        map.put(StatIds.INC_ATTACK_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        map.put(StatIds.INC_BALLISTA_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BALLISTA_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_BANNER_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_CHAOS_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHAOS_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_COLD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE));
        map.put(StatIds.INC_COLD_DAMAGE_SPELL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE_SPELL));
        map.put(StatIds.INC_CRIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE_CRIT));
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, self(StatConditionIds.COND_CONSUME_POWER_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY, self(StatConditionIds.COND_NOT_RECENTLY_CAUSED_CRIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_IMMOBILISED_ENEMIES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE_CRIT));
        map.put(StatIds.INC_CROSSBOW_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_CROSSBOW_DAMAGE));
        map.put(StatIds.INC_CURSE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_CURSE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_ATTACK, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_BLOCKING, stat(StatConditionIds.COND_BLOCKING_LIVING_STATE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_EMPOWERED_ATTACKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE_EMPOWERED));
        map.put(StatIds.INC_DAMAGE_LOW_LIFE, stat(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_OVER_TIME, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_OVER_TIME_MULTIPLIER_DAMAGE_OVER_TIME));
        map.put(StatIds.INC_DAMAGE_PARRYING, stat(StatConditionIds.COND_LIVING_STATE_PARRYING, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_PRESENCE_ALLIES, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_PRESENCE_GRANT_COE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_PRESENCE_ENEMIES, presence(PresenceCountValue.Relation.ENEMY, StatEffectIds.EFF_PRESENCE_GRANT_COE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_RECENTLY_KILLED, stat(StatConditionIds.COND_COUNT_KILL_RECENTLY, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_SHAPESHIFTED, stat(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_AXES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_AXE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_BOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BOW_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_CHILLED_ENEMIES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // 对异常/元素异常敌人增伤：onTarget 条件（敌持 ailment/elemental 标签）→ 复用通用 DAMAGE_DAMAGE 乘区
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_AILMENTS, onTarget(StatConditionIds.COND_ANY_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, onTarget(StatConditionIds.COND_ANY_ELEMENTAL_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_MACES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MACE));
        map.put(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ONE_HANDED));
        map.put(StatIds.INC_DAMAGE_WITH_SPEARS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPEAR));
        map.put(StatIds.INC_DAMAGE_WITH_DAGGERS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_DAGGER));
        map.put(StatIds.INC_DAMAGE_WITH_FLAILS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FLAIL));
        map.put(StatIds.INC_DAMAGE_WITH_SWORDS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SWORD));
        map.put(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TWO_HANDED));
        map.put(StatIds.INC_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        map.put(StatIds.INC_FIRE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE));
        map.put(StatIds.INC_FIRE_DAMAGE_SPELL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE_SPELL));
        // 「火焰伤害增加值作为数值 增加到 闪电伤害」：复用同一个 {damage,fire} 增伤效果（同一个
        // coe:damage 属性、同一个 ADD_MULTIPLIED_BASE 运算、同一组 {damage,fire} 标签），仅追加
        // 一条外溢声明 spill_to = {damage,lightning}——本条目的数值在火焰作用域与闪电作用域下
        // 各完整生效一次（并集，非替代；两侧互不扣减，故不同于 DamageConvert 的 from→to 重路由，
        // 也不同于按 source_attribute 折算的 math 表达式）。
        // 单向：外溢只挂在火焰这一侧；闪电侧条目（EFF_..._DAMAGE_LIGHTNING）不声明反向，故闪电的
        // 增加值不计入火焰。外溢是贡献方的一元声明，数据形状上无法写出互指成环。
        // 零新增 effect：与 INC_FIRE_DAMAGE 共用同一 EFF，仅条目多一个 spill_to 字段——
        // 五种元素两两组合无需任何硬编码。
        map.put(StatIds.id("fire_damage_increase_also_applies_to_lightning_damage"),
                wiring(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE)
                        .self()
                        .spillTo(GateTag.DAMAGE, GateTag.LIGHTNING)
                        .build());
        // 「火焰伤害增加值转换为 闪电伤害」：同样复用 {damage,fire} 增伤效果，但门控是【替代】而非并集——
        // 声明 convert_to = {damage,lightning} 后，本条目在火焰作用域下【不再命中】（那部分火焰
        // 增伤已全部让给闪电），只在闪电作用域下贡献一次。与上一条的 spill_to 恰成对照：
        // spill_to 使条目在两侧各计一次（COPY），convert_to 使条目只在目标侧计一次（MOVE）。
        // 单向：转换只挂在火焰这一侧，闪电侧条目不声明反向，故火焰不会反过来从闪电取增伤。
        // 零新增 effect：与 INC_FIRE_DAMAGE 共用同一 EFF，仅条目多一个 convert_to 字段——
        // 五种元素两两组合无需任何硬编码。与 DamageConvertStatEffect 的区别：后者按 ratio 重路由
        // 伤害值构成（在伤害管线结算），此处只重映射属性增量的计入池（在同一趟聚合内改写门控）。
        map.put(StatIds.id("fire_damage_increase_converted_to_lightning_damage"),
                wiring(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE)
                        .self()
                        .convertTo(GateTag.DAMAGE, GateTag.LIGHTNING)
                        .build());
        map.put(StatIds.INC_GRENADE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_GRENADE));
        map.put(StatIds.INC_HAZARD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_HAZARD));
        map.put(StatIds.INC_HERALD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_HERALD));
        map.put(StatIds.INC_LIGHT_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING));
        map.put(StatIds.INC_LIGHT_DAMAGE_SPELL, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING_SPELL));
        map.put(StatIds.INC_MARK_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MARK));
        map.put(StatIds.INC_MELEE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));
        map.put(StatIds.INC_META_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_META));
        map.put(StatIds.INC_MINION_DAMAGE, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION));
        map.put(StatIds.INC_OFFERING_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_OFFERING));
        map.put(StatIds.INC_PARRY_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PARRY));
        map.put(StatIds.INC_PHYSICAL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PHYSICAL));
        map.put(StatIds.INC_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PHYSICAL));
        map.put(StatIds.INC_PROJECTILE_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE));
        map.put(StatIds.INC_SPELL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));
        map.put(StatIds.INC_TAKEN_CRIT_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_CRIT_DAMAGE));
        map.put(StatIds.INC_TAKEN_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE));
        map.put(StatIds.INC_TOTEM_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_TOTEM));
        map.put(StatIds.INC_TRAP_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TRAP));
        map.put(StatIds.INC_WARCRY_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_WARCRY));
        // ===================== 技能标签族·伤害落点 =====================
        // 「Detonator Skills deal X% increased Damage」——{DAMAGE, DETONATOR}。
        // 标签 live：blue/detonate_dead 经 .withTags(GateTag.DETONATOR) 携带，Skill.getStatTags 发射。
        map.put(StatIds.DETONATOR_SKILLS_HAVE_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_DETONATOR));
        // 「Triggered Spells deal X% increased Spell Damage」——{DAMAGE, TRIGGERED, SPELL}。
        // 标签 live：SkillTriggerHelper 用虚拟绑定槽 TRIGGER_BIND 施放被触发技能，
        // Skill.getStatTags 依 getBind()==TRIGGER_BIND 附加 triggered（引擎改动），
        // 故加成只对被触发的施放生效，不串到玩家主动施放的同一法术。
        map.put(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TRIGGERED_SPELL));
        // 「Ancestrally Boosted Attacks deal X% increased Damage」——{DAMAGE, ANCESTRAL_BOOST, ATTACK}。
        // 标签当前惰性：引擎尚无「重击后下次攻击被先祖强化」的 Warrior 机制（天赋节点
        // ancestral_boosted_area_and_damage 已存在但无 stat 发射点）。按 GateTag.REMNANT 口径先行接线，
        // 语义正确优先于暂时惰性——绝不可退而求其次接到 MELEE（会把加成放大到全部近战攻击）。
        map.put(StatIds.ANCESTRALLY_BOOSTED_ATTACKS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ANCESTRAL_BOOST_ATTACK));
        map.put(StatIds.MINION_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION));
        map.put(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_MINION));
        map.put(StatIds.MORE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES_WITH_MACES, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_DAMAGE_DAMAGE_MACE));
        map.put(StatIds.OF_DAMAGE_TAKEN_BYPASSES_ENERGY_SHIELD, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_TAKEN_BYPASSES_ENERGY_ENERGY_SHIELD_TAKEN));
        map.put(StatIds.TAKEN_CRIT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TAKEN_CRIT_CHANCE));
        // 「命中对你造成暴击的几率提高」——复用 TAKEN_CRIT 同源 EFF（TAKEN_CRIT_CHANCE 属性，DamagePipeline 真读 incoming 暴击几率）。
        map.put(StatIds.HIT_DAMAGE_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_YOU, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TAKEN_CRIT_CHANCE));
        // 「对你造成的暴击伤害提高」——复用 INC_TAKEN_CRIT_DAMAGE 同源 EFF（TAKEN_CRIT_DAMAGE 属性，真读 incoming 暴击伤害）。
        map.put(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TAKEN_CRIT_DAMAGE));
        // 「永不造成暴击」——复用 resolute_technique 同源 SET_VALUE EFF（CRIT_CHANCE 置 0 = 永不暴击），magnitude 由条目提供 0。
        map.put(StatIds.NEVER_DEAL_CRITICAL_HITS, self(StatEffectIds.EFF_ATTRIBUTE_COE_CRIT_CHANCE_SET_VALUE));
        // "+8% to Critical Hit Chance of Herald Skills"：TO_ 前缀须走 ADD_VALUE（flat）{HERALD} 作用域保持。
        map.put(StatIds.TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE_HERALD));
        map.put(StatIds.TRAP_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TRAP));
        // ===== 伤害/护甲/怒气 等（matcher 结果中经人工核验正确的子集；penetration/flask 为假阳性）=====
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        map.put(StatIds.INC_DAMAGE_WITH_WARCRIES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_WARCRY));
        map.put(StatIds.ADD_TO_LIGHTNING_DAMAGE_TO_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_LIGHTNING_MINECRAFT_ATTACK_DAMAGE));
        // INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD 不在此接：{herald} 作用域伤害 EFF 表达的是
        // 「捷技能造成的伤害提高」，与「受捷影响时增伤」语义相反（后者见 WireWhile 的 COND_ANY_HERALD 接线）。
        // ===== 条件作用域精确接线（R3 逐条核验：EFF=正确基属性 generic，COND/TGT=已存在原语，非 matcher 臆造）=====
        // 1) 自身受变形门控，effect 用 generic 伤害（与 INC_ARMOR_WHILE_SHAPESHIFTED 同族 op/属性）。
        map.put(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // 注：INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE / INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM 收件人为“自身”，但无对应条件原语
        // （仅 TGT_COMPANION_INLINE / TGT_INLINE_TOTEM 作用域重定向，会误将 buff 给到同伴/图腾）需新增条件 → 推迟至 /5。
        
        // ===== DAMAGE 族精确接线（R3：镜像 GAIN_*_DAMAGE_FROM_* sibling 用 EFF_*_DAMAGE_CONVERT_GAIN_*_TRUE；全部 EFF/StatIds 已 grep 确认存在，非 matcher 臆造）=====
        // INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE → 攻击标签伤害 INC（ADD_MULTIPLIED_BASE）EFF 已确认（行 84）。
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // ===== 无条件 scope 族（R3：matcher 仅 scout，逐条核验 EFF 标签集与语义；114 exact 候选中绝大多数为关键词碰撞假阳性）=====
        // 注：INC_SPELL_DAMAGE 已在 line 763 用 EFF_..._DAMAGE_DAMAGE_SPELL 接好（佐证本映射正确）此处不重复。
        // INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS → 既有 ATTACK+CRIT 作用域 CRIT_CHANCE INC EFF（行 107：{ATTACK,CRIT}）与 INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE 同族 op 机制。
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE_CRIT));
        // ===== 手工核验安全族（matcher 仅 scout；逐条对照 EFF 属性+标签确认语义精确，非关键词子集臆造）=====
        // INC_CRITICAL_SPELL_DAMAGE_BONUS → SPELL 作用域 DAMAGE INC EFF（StatEffectGen 行 205：{DAMAGE,SPELL}）。
        map.put(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));
        // INC_THORNS_CRITICAL_DAMAGE_BONUS → 通用 DAMAGE INC EFF（行 185：{DAMAGE}）。
        map.put(StatIds.INC_THORNS_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // ===== AGAINST_<STATUS>_ENEMIES 条件族（R3：复用 INC_*_AGAINST_SHOCKED_ENEMIES 先例模板
        // stat(COND_<STATUS>, TGT_COND_ON_TARGET, <基础 EFF>)；COND 用真实存在的状态原语，基础 EFF 均为已确认存在的族）=====
        // INC_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES → ATTACK DAMAGE + COND_BLEED_STATUS_AGE。
        map.put(StatIds.INC_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES, onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // INC_DAMAGE_AGAINST_DAZED_ENEMIES → 通用 DAMAGE + COND_DAZE_STATUS。
        map.put(StatIds.INC_DAMAGE_AGAINST_DAZED_ENEMIES, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES / INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES → 通用/近战 DAMAGE + COND_PIN_STATUS（IMMOBILISED=pin，COND 已注册，复用既有 EFF）。
        map.put(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));
        // INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED → 复合双门控（目标侧 + 自侧）：
        //   目标侧 COND_PIN_STATUS（受击方被定身，与上一行同源）
        // + 自侧 COND_SOURCE_LIVING_STATE_SHAPESHIFTED（onTarget 条目的条件按受击方求值，故自侧形变
        //   经 SourceScopeNode 切到 StatContext.sourceEntity()＝命中结算中注入的攻击者作用域读取）。
        // 复用既有通用 DAMAGE INC EFF，零新增 effect 原语；DSL 无「onTarget + 多条件」快捷，故走 StatBuilder 链式。
        map.put(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE)
                        .onTarget()
                        .when(StatConditionIds.COND_PIN_STATUS)
                        .when(StatConditionIds.COND_SOURCE_LIVING_STATE_SHAPESHIFTED)
                        .build());
        // INC_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES → 通用 DAMAGE + COND_FREEZE_STATUS。
        map.put(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR → 通用 DAMAGE + COND_ARMOUR_BROKEN_FULLY_STATUS（镜像 FROZEN/SHOCKED/IMMOBILISED 同族，状态由破甲引擎对受害方真施加）。
        map.put(StatIds.INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR, onTarget(StatConditionIds.COND_ARMOUR_BROKEN_FULLY_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // 完全破甲时火焰承伤提高：攻击方 {damage, fire, fully_broken_armor} 作用域 DAMAGE（ADD_MULTIPLIED_BASE）；
        // 读点 DamagePipeline（约 :825）真读取，受害方持 ARMOUR_BROKEN_FULLY 才生效（R3 安全）。
        // 必须用 self(...)：该读点是 readInScope 型
        // （StatContext.of(tags, null) 恒置 sourceEntity=null → triggerTarget 恒 null → onTarget 条件恒假）；
        // 读点已自带 StatusEngine.has(living, ARMOUR_BROKEN_FULLY) 门控，条目侧无需再挂条件，
        // 与下一条 INC_EFFECT_OF_FULLY_BROKEN_ARMOR（同为破甲族）保持一致。
        map.put(StatIds.FULLY_BROKEN_ARMOR_YOU_INFLICT_ALSO_INC_FIRE_DAMAGE_TAKEN_FROM_HIT_DAMAGE,
                self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_FIRE_FULLY_BROKEN_ARMOR));
        // INC_EFFECT_OF_FULLY_BROKEN_ARMOR → 通用 DAMAGE + {damage, fully_broken_armor} 作用域（镜像 FIRE 变体，去掉 FIRE 标签）。
        // DamagePipeline.stageFullyBrokenArmor (line 796) 攻击方 {damage, fully_broken_armor} 真读取 DAMAGE，受害方持 ARMOUR_BROKEN_FULLY 才生效（R3 安全）。
        map.put(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_FULLY_BROKEN_ARMOR));
        // INC_DAMAGE_WITH_HITS_AGAINST_SHOCKED_ENEMIES → 通用 DAMAGE + COND_SHOCK_STATUS（确认存在）。
        map.put(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_SHOCKED_ENEMIES, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // ── 敌种类别目标侧（新 COND_ENEMY_TYPE_* 原语，判定目标实体类型，R3 安全）──
        // 同族范式：镜像 FROZEN/SHOCKED 目标侧，仅把"状态存在"换成"实体类型命中"（EntityFlagValue 命中返回 1.0）。
        map.put(StatIds.INC_DAMAGE_AGAINST_DEMONS, onTarget(StatConditionIds.COND_ENEMY_TYPE_DEMON, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_AGAINST_UNDEAD_ENEMIES, onTarget(StatConditionIds.COND_ENEMY_TYPE_UNDEAD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_HUMANOIDS, onTarget(StatConditionIds.COND_ENEMY_TYPE_HUMANOID, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE));
        // 定身（immobilisation）= PIN build-up，复用既有的 PIN_TAKEN build-up EFF。
        map.put(StatIds.INC_IMMOBILISATION_BUILDUP_AGAINST_CONSTRUCTS, onTarget(StatConditionIds.COND_ENEMY_TYPE_CONSTRUCT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_BUILDUP_PIN_TAKEN));
        // INC_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES → MELEE DAMAGE + COND_HEAVY_STUN_STATUS。
        map.put(StatIds.INC_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));
        // PROJECTILES_DEAL_INC_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES → PROJECTILE DAMAGE + COND_HEAVY_STUN_STATUS。
        map.put(StatIds.PROJECTILES_DEAL_INC_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE));
        // --- 在场稀有/独特（presence 门控，TGT_SELF_ONLY 非 COND_ON_TARGET）---
        map.put(StatIds.INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES, self(StatConditionIds.COND_PRESENCE_RARITY_RARE_UNIQUE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // WHILE_HOLDING_A_SHIELD：复用既有 ATTACK DAMAGE EFF + COND_HOLDING_SHIELD_LIVING_STATE，零新增注册。
        map.put(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, self(StatConditionIds.COND_HOLDING_SHIELD_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // ── 生命药剂耗尽门控（纯数据；新增 COND_NO_HEALTH_FLASK_USES_LEFT，落点复用 ATTACK DAMAGE EFF）──
        // 「while you have no Health Flask uses left」= 生命药剂剩余可用次数 < 1。
        // 读源 FlaskUsesValue("health")（FLASK_SAVE 槽充能 ÷ 单次消耗，与 FlaskHelper.drink 判据同源）；
        // 空槽时返 0 同样 < 1 → 条件成立，与 PoE2「没得喝即 0 次」一致。
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_NO_HEALTH_FLASK_USES_LEFT, self(StatConditionIds.COND_NO_HEALTH_FLASK_USES_LEFT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // ── AGAINST_PRESENCE_RECENTLY 族（纯数据；复用 COND_RECENTLY_(ENTERED|LEFT)_PRESENCE + 既有全局 CRIT EFF，零新增注册）──
        // INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY → onTarget 求值（候选=敌），复用全局 CRIT_CHANCE EFF。
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY, onTarget(StatConditionIds.COND_RECENTLY_ENTERED_PRESENCE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // ── ANY_EXPOSURE 聚合（纯数据）── INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_WITH_EXPOSURE → 目标侧任意曝光。
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_WITH_EXPOSURE, onTarget(StatConditionIds.COND_ANY_EXPOSURE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // ── 无元素异常取反（作用域标签引用取反）── 目标侧不持任意 "elemental" 标签状态 → 复用全局 CRIT_CHANCE EFF。
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_AFFECTED_BY_NO_ELEMENTAL_AILMENTS, onTarget(StatConditionIds.COND_NO_ELEMENTAL_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_HAVE_EXITED_YOUR_PRESENCE_RECENTLY → onTarget，复用全局 CRIT_DAMAGE EFF。
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_HAVE_EXITED_YOUR_PRESENCE_RECENTLY, onTarget(StatConditionIds.COND_RECENTLY_LEFT_PRESENCE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));
        // ── RECENTLY_CONSUMED_ENDURANCE 作用域火伤（纯数据；复用 COND_CONSUME_ENDURANCE_COUNT_RECENTLY + 既有 DAMAGE_DAMAGE_FIRE EFF，零新增注册）──
        // INC_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY → 近期消耗耐力球窗口内，火伤提高（{FIRE} MULTIPLIED_BASE）。
        map.put(StatIds.INC_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY, self(StatConditionIds.COND_CONSUME_ENDURANCE_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE));
        // --- 通用 CRIT_CHANCE（ALWAYS，全局）---
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLEEDING_ENEMIES, onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_DAZED_ENEMIES, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_SHOCKED_ENEMIES, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY, self(StatConditionIds.COND_COUNT_KILL_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.TO_THORNS_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE));
        // --- 通用 CRIT_DAMAGE（ALWAYS，全局；武器/作用域限定词条不接此处，避免越界）---
        map.put(StatIds.MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_CRIT_DAMAGE));
        map.put(StatIds.MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_LOW_HEALTH, self(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_CRIT_DAMAGE));
        map.put(StatIds.TO_CRITICAL_DAMAGE_BONUS_AGAINST_STUNNED_ENEMIES, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE));
        // ── ：新增状态/战斗状态 COND 原语驱动的接线（R3：复用既有 proven EFF，条件语义经 StatusValue/LivingStateValue 源核验）──
        // 状态 AGAINST（目标持有状态）→ TGT_COND_ON_TARGET + 通用/攻击 DAMAGE·CRIT 族
        map.put(StatIds.INC_ATTACK_DAMAGE_AGAINST_MAIMED_ENEMIES, onTarget(StatConditionIds.COND_MAIM_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES, onTarget(StatConditionIds.COND_BLIND_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // 同构扩展：enemies blinded by you → 你对该敌暴击率提升（复用 EFF_CRIT_CHANCE + COND_BLIND_STATUS）。
        map.put(StatIds.ENEMIES_BLINDED_BY_YOU_HAVE_INC_CRITICAL_HIT_CHANCE, onTarget(StatConditionIds.COND_BLIND_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, onTarget(StatConditionIds.COND_BLIND_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_HINDERED_ENEMIES, onTarget(StatConditionIds.COND_HINDER_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES, onTarget(StatConditionIds.COND_IGNITE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // 同构扩展：frozen enemies by you take inc cold damage → 建 COLD 兄弟 EFF（mirror FIRE/LIGHTNING）+ COND_FREEZE_STATUS。
        map.put(StatIds.ENEMIES_FROZEN_BY_YOU_TAKE_INC_COLD_DAMAGE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE));
        // enemies you electrocute have inc damage taken → 走攻击侧 {DAMAGE, ELECTROCUTED}。
        // 两条约束：① 不可用 EFF_..._TAKEN_DAMAGE（承伤侧），条目落玩家 store 而承伤读点 sourceEntity 恒 null → 不生效；
        // ② 亦不可用 onTarget(COND_ELECTROCUTE_STATUS)：readInScope 推入的 context sourceEntity 恒 null → triggerTarget 恒 null
        //    → onTarget 的条件恒不成立。状态门控由读点自身（DamagePipeline 判受害者 ELECTROCUTE）承担，故条目侧用 self(...)。
        map.put(StatIds.ENEMIES_YOU_ELECTROCUTE_HAVE_INC_DAMAGE_TAKEN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELECTROCUTED));
        // INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES → 通用 DAMAGE + COND_IGNITE_STATUS（burning=ignite，与 IGNITED 同状态，独立 StatId）。
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES, onTarget(StatConditionIds.COND_IGNITE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WHILE_LEECHING, self(StatConditionIds.COND_LEECHING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WHILE_LEECHING_HEALTH, self(StatConditionIds.COND_LEECHING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_CRITICAL_HIT_CHANCE_WHILE_DUAL_WIELDING, self(StatConditionIds.COND_DUAL_WIELD_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_DUAL_WIELDING, self(StatConditionIds.COND_DUAL_WIELD_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH, self(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        map.put(StatIds.INC_FIRE_DAMAGE_WHILE_IGNITED, self(StatConditionIds.COND_IGNITE_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE));
        // Bucket D 精确子集：无条件 + EFF 已注册，scope 标签精确匹配，op=ADD_MULTIPLIED_BASE 与 INC_* 一致
        // 均复刻既有同构先例（INC_CROSSBOW_DAMAGE / INC_HERALD_DAMAGE / INC_PHYSICAL_DAMAGE / INC_SPELL_DAMAGE / MINION_DAMAGE）。
        map.put(StatIds.INC_DAMAGE_WITH_CROSSBOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_CROSSBOW_DAMAGE));
        // 「过去 10 秒内每发射过一种不同弹药，弩伤 +25%」：去重弹药计数缩放（distinct:fired_ammunition.10:ammo）。
        // 读值源 = 发射时经 Event.record 写入的 "ammo:<物品id>" 身份标签去重计数（ProjectileInShoot#modifyArgs），
        // 落点复用 INC_DAMAGE_WITH_CROSSBOWS 同源弩伤 EFF（{CROSSBOW} 作用域），保证与同胞条目同一乘区。
        map.put(StatIds.INC_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_SECONDS,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_CROSSBOW_DAMAGE)
                        .self()
                        .amount("$value * distinct:fired_ammunition.10:ammo / $per")
                        .param("value", 0.25)
                        .param("per", 1.0)
                        .build());
        map.put(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_HERALD));
        // 「Every Rage also grants 1% increased X Damage」—— 真值（每点怒气 +1%），乘 rage_effect（Berserk）。
        // 需 perRage() 缩放；仅 amount=$value 而无 per-rage 缩放属死线。
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_FIRE_DAMAGE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE)
                        .self()
                        .amount(perRage())
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_PHYSICAL_DAMAGE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PHYSICAL)
                        .self()
                        .amount(perRage())
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_SPELL_DAMAGE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL)
                        .self()
                        .amount(perRage())
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());
        map.put(StatIds.EVERY_RAGE_ALSO_GRANTS_YOU_INC_MINION_DAMAGE,
                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION)
                        .self()
                        .amount(perRage())
                        .param("value", 0.01)
                        .param("per", 1.0)
                        .build());
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, onTarget(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, onTarget(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, onTarget(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, onTarget(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));
        // 2) 对稀有/传奇敌人 INC 命中伤害 —— 复用 INC_DAMAGE_WITH_HIT_DAMAGE 同源 EFF（, MULTIPLIED_BASE）+ COND_PRESENCE_RARITY_RARE_UNIQUE + TGT_SELF_ONLY（ 同构）。
        map.put(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_RARE_AND_UNIQUE_ENEMIES, self(StatConditionIds.COND_PRESENCE_RARITY_RARE_UNIQUE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        // 对被标记的敌人 —— increased 暴击率（COND_PRESENCE_MARK_STATUS 纯数据原语 + 目标侧求值）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES, onTarget(StatConditionIds.COND_PRESENCE_MARK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // 对被标记的敌人 —— 承受更多伤害（onTarget + COND_PRESENCE_MARK_STATUS，复用 INC_MARK_DAMAGE 同源 EFF_DAMAGE_DAMAGE_MARK，{DAMAGE,MARK} 作用域双门控）
        map.put(StatIds.ENEMIES_YOU_MARK_TAKE_INC_DAMAGE, onTarget(StatConditionIds.COND_PRESENCE_MARK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MARK));

        // WHEN_ON_FULL_HEALTH —— 镜像 INC_MELEE_DAMAGE（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_MELEE_DAMAGE_WHEN_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));
        // 长矛近战伤害（MELEE+SPEAR 组合作用域，新增 EFF 变体，复用 COND_LIVING_STATE_SURROUNDED）
        map.put(StatIds.INC_MELEE_DAMAGE_WITH_SPEARS_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE_SPEAR));
        // 冰冻/电击持续时间（"on you" = 承受侧，复用既有 TAKEN 形态 EFF，零新增注册）
        map.put(StatIds.INC_FREEZE_DURATION_ON_YOU, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_FREEZE_TAKEN));
        map.put(StatIds.INC_SHOCK_DURATION_ON_YOU, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_SHOCK_TAKEN));
        // 电击持续时间（bare 别名）：与 INC_SHOCK_DURATION_ON_YOU 同节点，复用其 TAKEN 形态 EFF（R3 安全，零新增注册）。
        map.put(StatIds.INC_SHOCK_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_SHOCK_TAKEN));

        // 近期事件条件：镜像 base 的 EFF，条件走 EventHistoryValue
        map.put(StatIds.INC_DAMAGE_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY, self(StatConditionIds.COND_CONSUME_FRENZY_COUNT_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        // IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY —— 镜像 INC_DAMAGE（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_CRIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        // 承伤侧条件变体：走 TAKEN_DAMAGE 而非 DAMAGE
        // （按前缀会被误配到 base INC_DAMAGE=造成伤害，故单独指定承伤侧 EFF）
        map.put(StatIds.INC_DAMAGE_TAKEN_WHILE_ON_LOW_HEALTH, self(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE));

        // 使敌人感电后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_SHOCKED_AN_ENEMY_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        // 触发技能后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_DAMAGE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_META_SKILL_TRIGGERED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        // 形变为动物形态后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_FORM_CHANGED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));
        // 形变为动物形态后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_FORM_CHANGED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
        // 获得能量球后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_GAIN_A_POWER_CHARGE_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_GAIN_CHARGE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));
        // 被重击晕后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_BEEN_HEAVY_STUNNED_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_STUN, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));

        // IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY —— 镜像 INC_CRITICAL_HIT_CHANCE（复用其 EFF 与 COND，零新增注册）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_META_SKILL_TRIGGERED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));

        // 主动击中后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.MINIONS_DEAL_INC_DAMAGE_IF_YOU_VE_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION));
        // 使敌人冰缓后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_CHILLED_AN_ENEMY_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        // 使敌人点燃后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_IGNITED_AN_ENEMY_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_AILMENT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL));
        // 形变回人形后的近期事件条件（复用 base 的 EFF）
        map.put(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_HUMAN_FORM_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_FORM_CLEARED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));

        // 近战暴击率
        map.put(StatIds.INC_MELEE_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_MELEE));
        // 弩炮暴击率
        map.put(StatIds.INC_BALLISTA_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_BALLISTA));
        // 弩炮暴击伤害加成
        map.put(StatIds.INC_BALLISTA_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE_BALLISTA));
        // 物理伤害的 FIRE 额外增益（复用 FROM_PHYSICAL 变体）
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));
        // 物理伤害的 LIGHTNING 额外增益（复用 FROM_PHYSICAL 变体）
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));
        // 物理伤害的 CHAOS 额外增益（复用 FROM_PHYSICAL 变体）
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));
        // MINIONS_GAIN_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE → 随从版「物理转混沌额外获得」：复用同一 DamageConvertStatEffect({PHYSICAL}->CHAOS, GAIN)，
        // 经 stat(TGT_INLINE_MINION) 把收件人重定向为随从实体本身（同 INC_MINION_DAMAGE WireDamage:117 范式）。零新增 EFF/COND/TGT，R3 安全。
        map.put(StatIds.MINIONS_GAIN_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        // 持弩时暴击率
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_CROSSBOW));
        // 持长矛时暴击率
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_SPEARS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_SPEAR));
        // 持匕首时暴击率（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_DAGGER));
        // 持链枷时暴击率（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_FLAILS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_FLAIL));
        // 持长杖时暴击率（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_QUARTERSTAVES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_QUARTERSTAFF));
        // 法术暴击率
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_SPELL));
        // 陷阱暴击率
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_TRAP));
        // 持弩时暴击伤害加成
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_CROSSBOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_CROSSBOW));
        // 持长矛时暴击伤害加成
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_SPEARS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_SPEAR));
        // 持匕首时暴击伤害加成（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_DAGGER));
        // 持链枷时暴击伤害加成（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_FLAILS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_FLAIL));
        // 持长杖时暴击伤害加成（数据先行：引擎武器标签消费端待接）
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_QUARTERSTAFF));
        // 弓类物理伤害
        map.put(StatIds.INC_PHYSICAL_DAMAGE_WITH_BOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_BOW_DAMAGE_PHYSICAL));
        // 攻击冰霜伤害
        map.put(StatIds.INC_ATTACK_COLD_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ATTACK_DAMAGE_COLD));
        // 法术范围伤害
        map.put(StatIds.INC_SPELL_AREA_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_AREA_DAMAGE_SPELL));
        // 范围伤害（不限攻击）
        map.put(StatIds.INC_AREA_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_AREA_DAMAGE));

        // INC_CRITICAL_DAMAGE_BONUS 的 BOW 作用域变体
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_BOWS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_BOW_CRIT));
        // INC_CRITICAL_DAMAGE_BONUS 的 MELEE 作用域变体
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_ONE_HANDED_MELEE_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_MELEE));
        // INC_DAMAGE 的 PLANT 作用域变体
        map.put(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_PLANT));
        // INC_CRITICAL_HIT_CHANCE 的 MELEE 作用域变体（复用既有 EFF）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_ONE_HANDED_MELEE_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_MELEE));
        // 法术获得额外混沌伤害
        map.put(StatIds.SPELLS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_SPELL));
        // 伙伴获得额外混沌伤害
        map.put(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_COMPANION));
        // 伙伴获得额外冰霜伤害
        map.put(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_COMPANION));

        // ===== 伤害类型转换 vein（R3 安全：$value 驱动比例，from/to/mode 精确匹配语料；
        //   AGAINST_<状态>_ENEMIES 因 damageConvert(attacker) 不绑定目标实体、onTarget 条件会恒空 → parked，待目标感知转换通道）=====
        // 元素伤害 → 额外 冰/火/雷（GAIN）
        map.put(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_COLD_DAMAGE, self(StatEffectIds.EFF_ELEMENTAL_DAMAGE_CONVERT_COLD_GAIN));
        map.put(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, self(StatEffectIds.EFF_ELEMENTAL_DAMAGE_CONVERT_FIRE_GAIN));
        map.put(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_ELEMENTAL_DAMAGE_CONVERT_LIGHTNING_GAIN));
        // LIVINGMOD_CONVERT_PHYS_TO_* 不在此 map.put：这些 id 的定义权威在 GenEvent#registerModifier
        // （StatGen.read(id, 内联 REPLACE 条目)，比例来自 DamageConvert 权重比、以内联 amount 落盘）。
        // 在此再 put 一次会被 GenEvent 的注册静默覆盖（同一 entry path 后写覆盖），留下「看似已接线、实则无效」的影子绑定。
        // 全部伤害 → 火（REPLACE）
        map.put(StatIds.OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE, self(StatEffectIds.EFF_ALL_DAMAGE_CONVERT_FIRE_REPLACE));
        // 强化攻击：全部伤害 → 额外冰冷 / 物理 → 额外火焰
        map.put(StatIds.EMPOWERED_ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, self(StatEffectIds.EFF_ALL_DAMAGE_CONVERT_COLD_GAIN_EMPOWERED));
        map.put(StatIds.EMPOWERED_ATTACKS_GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, self(StatEffectIds.EFF_PHYSICAL_DAMAGE_CONVERT_FIRE_GAIN_EMPOWERED));
        // 伙伴：元素伤害 → 额外混乱
        map.put(StatIds.MINIONS_GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_ELEMENTAL_DAMAGE_CONVERT_CHAOS_GAIN_COMPANION));
        // 自侧门控（self 条件在 attacker 上求值，安全）：地面/变形
        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL));
        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_ON_IGNITED_GROUND, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_2));
        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_3));
        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_RANDOM_TRUE));

        // ===== AGAINST_<状态>_ENEMIES 转换（依赖 DamagePipeline 维度C 绑定命中目标为 sourceEntity，
        //   onTarget(COND_X_STATUS) 可正确按目标实时状态求值；damageConvert(attacker) 因不绑定目标而不适用）=====
        map.put(StatIds.GAIN_OF_COLD_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_FREEZE_ENEMIES, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_DAZED_ENEMIES, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_SHOCKED_ENEMIES, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES, onTarget(StatConditionIds.COND_CHILL_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_DAZED_ENEMIES, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL));
        map.put(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL));

        // 复用 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION（已用于同义语料）
        map.put(StatIds.MINIONS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION));
        // INC_DAMAGE 的 COMMAND+MINION 作用域变体
        map.put(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_COMMAND_MINION));
        // INC_CRITICAL_DAMAGE_BONUS 的 MINION 作用域变体
        map.put(StatIds.MINIONS_HAVE_INC_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_DAMAGE_ATTACK_CRIT_MINION));
        // INVOKED_SPELLS_DEAL_INC_DAMAGE → 复用既有 TRIGGERED_SPELL 伤害 EFF（{DAMAGE,TRIGGERED,SPELL} 由 Skill.getStatTags 发射，DamageHelper 真读）
        map.put(StatIds.INVOKED_SPELLS_DEAL_INC_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TRIGGERED_SPELL));
        // INVOKED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE → 触发法术暴击率（{TRIGGERED,SPELL}）
        map.put(StatIds.INVOKED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_TRIGGERED_SPELL));
        // INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS → 触发法术暴伤（{TRIGGERED,SPELL,CRIT}，镜像 CRIT_DAMAGE_CRIT_SPELL）
        map.put(StatIds.INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE_TRIGGERED_SPELL));
        // INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH → 法术伤害+耗血作用域（HEALTH 标签 Skill.java:170 发射）
        map.put(StatIds.INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL_HEALTH));
        // 64601/49363 wildsurge_incantation：风暴+植物法术增伤（storm_and_plant_spells_deal_more_damage，节点值 0.5）
        // → STORM 与 PLANT 双作用域共用增伤通道：DAMAGE_STORM({damage,storm}) + DAMAGE_PLANT({damage,plant})。
        // PLANT 增伤 EFF 复用 INC_DAMAGE_WITH_PLANT_SKILLS 同源；STORM 增伤 EFF 并行；节点值各自 +50% 增伤。
        map.put(StatIds.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE,
                groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_STORM, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_DAMAGE_PLANT));

        // ── 词缀（Mod）域接线 ──
        // 1) 「Adds # to # <元素> Damage to Attacks」（局部武器）：与 StatIds.ADD_COLD/FIRE/LIGHT/PHYSICAL_DAMAGE
        //    完全同义（均为攻击侧平加伤害），故直接复用其 EFF（ATTACK_DAMAGE + 元素标签，ADD_VALUE），零新增原语。
        map.put(StatIds.ADD_COLD_DAMAGE_TO_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COLD_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_FIRE_DAMAGE_TO_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_FIRE_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_LIGHTNING_DAMAGE_TO_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_LIGHTNING_MINECRAFT_ATTACK_DAMAGE));
        map.put(StatIds.ADD_PHYSICAL_DAMAGE_TO_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE_PHYSICAL));
        // 2) 「Gain #% of Damage as Extra <元素> Damage」：DamageConvertStatEffect(GAIN) from 全五类 → 目标元素。
        //    复用既有 EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL 系列（from={physical,chaos,fire,cold,lightning}）：
        //    本体 → cold、_2 → fire、_3 → lightning、_4 → physical，与 StatEffectGen:756-764 真读点一致，零新增原语。
        map.put(StatIds.GAIN_COLD_DAMAGE_AS_EXTRA, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL));
        map.put(StatIds.GAIN_FIRE_DAMAGE_AS_EXTRA, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_2));
        map.put(StatIds.GAIN_LIGHTNING_DAMAGE_AS_EXTRA, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_3));
        map.put(StatIds.GAIN_PHYSICAL_DAMAGE_AS_EXTRA, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_4));
        // 3) 「#% increased Damage with Bow Skills」：DAMAGE 属性 {damage, bow} 作用域（ADD_MULTIPLIED_BASE，base=1.0）。
        map.put(StatIds.INC_DAMAGE_WITH_BOW_SKILLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BOW_COE_DAMAGE_DAMAGE));
        // 4) 「#% increased Critical Hit Chance for Spells」：CRIT_CHANCE 属性 {spell} 作用域（ADD_MULTIPLIED_BASE，base=0.05）。
        map.put(StatIds.INC_SPELL_CRIT_CHANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CRIT_CHANCE_SPELL));
        // 5) 「# to # Physical Thorns damage」：被击中时对事件目标造成物理伤害（DamageStatEffect {taken_hit, physical}），
        //    与 StatIds.INC_THORNS_DAMAGE / GAIN_PHYSICAL_THORNS_DAMAGE_* 同源（min/max 两条目各绑自己的值槽）。
        map.put(StatIds.THORNS, self(StatEffectIds.EFF_DAMAGE_PHYSICAL_TAKEN_HIT));
        // 6) 「#% additional Physical Damage Reduction」（自身）：PHYSICAL_DAMAGE_REDUCTION {taken, physical} ADD_VALUE，
        //    DamageHelper.applyDamageReductions:177 真读（该读口含自身主语变体，另有 minion/totem 变体）。
        map.put(StatIds.ADD_PHYSICAL_DAMAGE_REDUCTION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PHYSICAL_DAMAGE_REDUCTION));
        // 6b) 「#% increased Physical Damage Reduction」（自身）：INC 与 ADD 同落点（PHYSICAL_DAMAGE_REDUCTION {taken, physical}
        //     零基 ADD_VALUE；base=0 故「increased」也走 ADD_VALUE，否则 ADD_MULTIPLIED_BASE 乘 0 无效）。
        //     DamageHelper.applyDamageReductions:177 在护甲减免之后对 damages[0]（仅物理）叠加乘算 = 硬减伤；
        //     不影响元素伤害（slots 1~4 走抗性/ARMOUR_APPLIES_ELEMENTAL_DAMAGE 的 EXTRA 分支，互不串扰），R4 安全。
        map.put(StatIds.INC_PHYSICAL_DAMAGE_REDUCTION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PHYSICAL_DAMAGE_REDUCTION));
        // 7) 「Hits against you have #% reduced Critical Damage Bonus」：TAKEN_CRIT_DAMAGE（base=1.0，ADD_MULTIPLIED_BASE），
        //    "reduced" 由条目负值承载；与 StatIds.INC_TAKEN_CRIT_DAMAGE 同落点。
        map.put(StatIds.REDUCED_EXTRA_DAMAGE_FROM_CRITS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_CRIT_DAMAGE));

        // ── 以下 10 条当前<b>无任何词缀引用</b>（仅 ModStatIds 声明），接线纯粹为防未来引用时成为死线 ──
        // 8) 「#% increased Spell Damage」：DAMAGE {damage, spell}（ADD_MULTIPLIED_BASE，base=1.0）。
        // 9) 「#% increased Trap Damage」：DAMAGE {damage, trap}。
        // 10) 「#% reduced Physical Damage taken」：TAKEN_DAMAGE {taken, physical}（ADD_MULTIPLIED_BASE，base=1.0），负值=减伤。
        map.put(StatIds.REDUCED_PHYSICAL_DAMAGE_TAKEN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE_PHYSICAL));
        // 11) 「#% reduced Duration of Ailments on You」（泛化）：AILMENT_DURATION {ailment, taken}。
        map.put(StatIds.REDUCED_AILMENT_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_TAKEN));
        // 12) 单异常时长降低：各自 {ailment, taken, <异常>} 作用域（与 REDUCED_BLEEDING_DURATION 同族）。
        map.put(StatIds.REDUCED_CHILL_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_CHILL_COE_AILMENT_DURATION_TAKEN));
        map.put(StatIds.REDUCED_FREEZE_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_FREEZE_TAKEN));
        map.put(StatIds.REDUCED_IGNITE_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_IGNITE_TAKEN));
        map.put(StatIds.REDUCED_POISON_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_POISON_TAKEN));
        map.put(StatIds.REDUCED_SHOCK_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_SHOCK_TAKEN));

    }
}
