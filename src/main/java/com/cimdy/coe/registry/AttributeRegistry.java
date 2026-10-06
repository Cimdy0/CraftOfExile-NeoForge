package com.cimdy.coe.registry;

import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.ImmuneStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public class AttributeRegistry {

    // ===== 基础设施：DeferredRegister / 全属性清单 / 作用域标签表（从编译产物逆向还原）=====
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, "coe");
    public static final List<Holder<Attribute>> ALL_ATTRIBUTE = new ArrayList<>();
    public static final Map<String, Set<String>> SCOPE_TAGS = new HashMap<>();

    public static final Holder<Attribute> WORK_APTITUDE = registerDouble("work_aptitude", 0.0);
    public static final Holder<Attribute> WORK_SPEED = registerDouble("work_speed", 1.0);
    public static final Holder<Attribute> MAX_DARKNESS = registerPercentage("max_darkness", 1.0);
    public static final Holder<Attribute> MAX_ENDURANCE_CHARGES = registerDouble("max_endurance_charges", 3.0);
    public static final Holder<Attribute> MAX_ENERGY_SHIELD = registerDouble("max_energy_shield", 0.0, 0.0);
    public static final Holder<Attribute> MAX_RUNIC_WARD = registerDouble("max_runic_ward", 0.0, 0.0);
    public static final Holder<Attribute> MAX_FRENZY_CHARGES = registerDouble("max_frenzy_charges", 3.0);
    public static final Holder<Attribute> MAX_BANNERS = registerDouble("max_banners", 1.0);
    public static final Holder<Attribute> MAX_CURSES = registerDouble("max_curses", 1.0);
    public static final Holder<Attribute> MAX_TOTEMS = registerDouble("max_totems", 1.0);
    public static final Holder<Attribute> MAX_ORB = registerDouble("max_orb", 1.0);
    public static final Holder<Attribute> MAX_FISSURE = registerDouble("max_fissure", 1.0);
    public static final Holder<Attribute> MAX_WALL = registerDouble("max_wall", 3.0);
    public static final Holder<Attribute> MAX_COMPANIONS = registerDouble("max_companions", 1.0);
    public static final Holder<Attribute> MINION_LIMIT = registerDouble("minion_limit", 1.0);
    public static final Holder<Attribute> MINION_DURATION = registerDouble("minion_duration", 1.0, 0.0);
    public static final Holder<Attribute> STUN_THRESHOLD_FROM_ES = registerDouble("stun_threshold_from_es", 0.0);
    public static final Holder<Attribute> MAX_INFUSION = registerDouble("max_infusion", 1.0);
    public static final Holder<Attribute> MAX_TOTAL_ELEMENTAL_INFUSIONS = registerDouble("max_total_elemental_infusions", 3.0);
    public static final Holder<Attribute> MAX_MANA = registerDouble("max_mana", 40.0, 0.0);
    public static final Holder<Attribute> ARCHMAGE_LIGHTNING_GAIN = registerDouble("archmage_lightning_gain", 0.0);
    public static final Holder<Attribute> MAX_POWER_CHARGES = registerDouble("max_power_charges", 3.0);
    public static final Holder<Attribute> MAX_RAGE = registerDouble("max_rage", 50.0);
    public static final Holder<Attribute> MAX_REGENERATION = registerDouble("max_regeneration", 0.05);
    public static final Holder<Attribute> MAX_RESISTANCES = registerPercentage("max_resistances", 0.75, "resistance");
    public static final Holder<Attribute> MAX_BLOCK = registerPercentage("max_block", 0.75);
    public static final Holder<Attribute> MAX_SPIRIT = registerDouble("max_spirit", 0.0, 0.0);
    public static final Holder<Attribute> MAX_VOLATILITY = registerDouble("max_volatility", 100.0);
    public static final Holder<Attribute> SKILL_LEVEL_BONUS = registerDouble("skill_level_bonus", 0.0);
    public static final Holder<Attribute> META_ENERGY_MAX = registerDouble("meta_energy_max", 100.0, 0.0);
    public static final Holder<Attribute> BASE_ENERGY_SHIELD_RECHARGE = registerPercentage("base_energy_shield_recharge", 1.0);
    public static final Holder<Attribute> CURRENT_ENERGY_SHIELD_RECHARGE = registerPercentage("current_energy_shield_recharge", 1.0);
    public static final Holder<Attribute> DARKNESS_RESERVATION_LASTS = registerPercentage("darkness_reservation_lasts", 1.0);
    public static final Holder<Attribute> ENERGY_RECHARGE = registerPercentage("energy_recharge", 1.0);
    public static final Holder<Attribute> ENERGY_SHIELD_RECHARGE_DISABLED = registerDouble("energy_shield_recharge_disabled", 0.0);
    public static final Holder<Attribute> ENERGY_SHIELD_CAPPED_BY_ARMOR = registerDouble("energy_shield_capped_by_armor", 0.0);
    public static final Holder<Attribute> ENERGY_SHIELD_REGEN_DISABLED = registerDouble("energy_shield_regen_disabled", 0.0);
    public static final Holder<Attribute> CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH = registerDouble("cannot_recover_health_other_than_from_health_leech", 0.0);
    public static final Holder<Attribute> CANNOT_USE_CHARMS = registerDouble("cannot_use_charms", 0.0);
    public static final Holder<Attribute> ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS = registerDouble("add_a_curse_after_channelling_for_seconds", 0.0, 0.0);
    public static final Holder<Attribute> CURSE_TARGET_HEALTH_REGEN_MULT = registerDouble("curse_target_health_regen_mult", 1.0);
    public static final Holder<Attribute> GLORY_CONSUME_SKIP_CHANCE = registerPercentage("glory_consume_skip_chance", 0.0);
    public static final Holder<Attribute> GLORY_GENERATION = registerPercentage("glory_generation", 1.0);
    public static final Holder<Attribute> RAGE_LOSS_GRACE = registerDouble("rage_loss_grace", 0.0, 0, 4096);
    public static final Holder<Attribute> RAGE_LOSS_RATE = registerPercentage("rage_loss_rate", 1.0);
    public static final Holder<Attribute> RAGE_REGEN_PER_RAGE_SPENT = registerDouble("rage_regen_per_rage_spent", 0.0);
    public static final Holder<Attribute> RAGE_REGEN_PER_SECOND = registerDouble("rage_regen_per_second", 0.0);
    /** 怒气效果全局乘子 —— 盛怒Berserk「(40—59)% increased Rage effect」的落点。base=1.0（倍率型，非增量）。
     *  <p><b>写入侧</b>：盛怒经 {@code ModifierAction}（operation=2 → ADD_MULTIPLIED_BASE，
     *  {@code SpiritRedGen#berserkRageEffect} 逐等级 0.40—0.59）写入 ⇒ 运行期 1.40—1.59，符合「随等级成长」意图。
     *  <p><b>消费侧</b>：唯一入口 {@code WirePerScaling#perRage()}（{@code $value * rage * attribute:coe:rage_effect / $per}），
     *  7 条「每点怒气给 X%」通道（fire/physical/spell/minion 伤害、armor、stun threshold、minion atk speed）全经此读。
     *  <p><b>曾为死线且症状比「不生效」更重</b>：本属性漏注册时 {@code AttributeValue#evaluate} 走
     *  {@code orElseThrow}（非回落 0）抛 IllegalArgumentException，经 StatEngine.fold（finally 无 catch）
     *  一路上抛至 {@code LivingAttributeValue} mixin 的 {@code getAttributeValue}（无 catch）—— 读即崩，非静默为0。
     *  <p><b>range 依据</b>：用 {@code registerPercentage(name, base)} 2 参重载的 ±999999.0 默认区间
     *  （同 DAMAGE/PROJECTILE_SPEED 等base=1.0 倍率属性惯例；本仓 63 处 base=1.0 percentage 均走此重载，
     *  <b>无</b>任何 base=1.0 percentage 显式收窄 min/max 的先例，故不自创区间）。
     *  取 Percentage 而非 Double：与同为「base=1.0 + 经 {@code attribute:coe:X} 被当原始乘子读」的
     *  {@code PROJECTILE_SPEED} 同形，且「increased Rage effect」按百分比呈现（1.40 → 140%）。 */
    public static final Holder<Attribute> RAGE_EFFECT = registerPercentage("rage_effect", 1.0);
    public static final Holder<Attribute> RESERVATION_EFFICIENCY = registerPercentage("reservation_efficiency", 0.0);
    public static final Holder<Attribute> INVOKED_ENERGY_COST_MULT = registerPercentage("invoked_energy_cost_mult", 1.0, "invoked");
    public static final Holder<Attribute> VOLATILITY_DETONATION_TIME = registerDouble("volatility_detonation_time", 5.0);
    public static final Holder<Attribute> VOLATILITY_GENERATION = registerPercentage("volatility_generation", 1.0);
    public static final Holder<Attribute> DEXTERITY = registerDouble("dexterity", 0.0, 0.0);
    public static final Holder<Attribute> INTELLIGENCE = registerDouble("intelligence", 0.0, 0.0);
    public static final Holder<Attribute> STRENGTH = registerDouble("strength", 0.0, 0.0);
    public static final Holder<Attribute> AILMENT_BUILDUP = registerPercentage("ailment_buildup", 1.0);
    public static final Holder<Attribute> AILMENT_CHANCE = registerPercentage("ailment_chance", 0.0);
    public static final Holder<Attribute> AILMENT_DAMAGE_CONTRIBUTION = registerPercentage("ailment_damage_contribution", 0.0);
    public static final Holder<Attribute> AILMENT_DAMAGE_RATE = registerPercentage("ailment_damage_rate", 1.0);
    public static final Holder<Attribute> AILMENT_DURATION = registerPercentage("ailment_duration", 1.0);
    public static final Holder<Attribute> AILMENT_MAGNITUDE = registerPercentage("ailment_magnitude", 1.0);
    public static final Holder<Attribute> IMPALE_MAGNITUDE = registerPercentage("impale_magnitude", 1.0);
    public static final Holder<Attribute> INCISION_TAKEN_PHYSICAL_PER_INCISION = registerDouble("incision_taken_physical_per_incision", 0.0);
    public static final Holder<Attribute> AILMENT_THRESHOLD = registerDouble("ailment_threshold", 0.0);
    public static final Holder<Attribute> AILMENT_THRESHOLD_ADAPTATION = registerPercentage("ailment_threshold_adaptation", 1.0);
    public static final Holder<Attribute> AILMENT_THRESHOLD_ADD = registerDouble("ailment_threshold_add", 0.0);
    public static final Holder<Attribute> AFTERSHOCK_AREA = registerPercentage("aftershock_area", 1.0);
    public static final Holder<Attribute> AFTERSHOCK_CHANCE = registerPercentage("aftershock_chance", 0.0);
    public static final Holder<Attribute> ARCHON_RECOVERY_RATE = registerPercentage("archon_recovery_rate", 1.0);
    public static final Holder<Attribute> ARCHON_RECOVERY_PERIOD_MULT = registerPercentage("archon_recovery_period_mult", 1.0);
    public static final Holder<Attribute> BUFF_DURATION_MULT = registerPercentage("buff_duration_mult", 1.0);
    public static final Holder<Attribute> DEBUFF_DURATION_MULT = registerPercentage("debuff_duration_mult", 1.0);
    public static final Holder<Attribute> DURATION_MULT = registerPercentage("duration_mult", 1.0);
    public static final Holder<Attribute> CHARM_ACTIVATION_CHANCE = registerPercentage("charm_activation_chance", 0.0);
    public static final Holder<Attribute> HAZARD_REARM_CHANCE = registerPercentage("hazard_rearm_chance", 0.0);
    public static final Holder<Attribute> CHARM_CHARGES_GAINED = registerPercentage("charm_charges_gained", 1.0);
    public static final Holder<Attribute> CHARM_CHARGES_USED = registerPercentage("charm_charges_used", 1.0);
    public static final Holder<Attribute> CHARM_CHARGE_RATE = registerPercentage("charm_charge_rate", 0.0);
    public static final Holder<Attribute> CHARM_EFFECT = registerPercentage("charm_effect", 1.0);
    public static final Holder<Attribute> CHARM_SLOT = registerDouble("charm_slot", 3.0);
    public static final Holder<Attribute> GUARD_RECOVERY = registerPercentage("guard_recovery", 0.0);
    public static final Holder<Attribute> PUPPET_MASTER_DURATION = registerPercentage("puppet_master_duration", 0.0);
    public static final Holder<Attribute> PUPPET_MASTER_EFFECT = registerPercentage("puppet_master_effect", 0.0);
    public static final Holder<Attribute> REMNANTS_COLLECTED_FURTHER_AWAY = registerPercentage("remnants_collected_further_away", 1.0);
    public static final Holder<Attribute> REMNANT_DOUBLE_CHANCE = registerPercentage("remnant_double_chance", 0.0);
    public static final Holder<Attribute> REMNANT_EXTRA_CHANCE = registerPercentage("remnant_extra_chance", 0.0);
    public static final Holder<Attribute> REMNANT_MAGNITUDE = registerDouble("remnant_magnitude", 1.0);
    public static final Holder<Attribute> REMNANT_CONVERSION = registerDouble("remnant_conversion", 0.0, 0.0);
    public static final Holder<Attribute> BURNING_EXPLODE_ON_KILL_CHANCE = registerPercentage("burning_explode_on_kill_chance", 0.0);
    public static final Holder<Attribute> EMPOWERED_EXPLODE_ON_KILL_CHANCE = registerPercentage("empowered_explode_on_kill_chance", 0.0);
    public static final Holder<Attribute> ADDITIONAL_INFUSION_CHANCE = registerPercentage("additional_infusion_chance", 0.0);
    public static final Holder<Attribute> CROSSBOW_IMMEDIATE_RELOAD_CHANCE = registerPercentage("crossbow_immediate_reload_chance", 0.0);
    public static final Holder<Attribute> CHARGE_CONSUME_SKIP_CHANCE = registerPercentage("charge_consume_skip_chance", 0.0);
    public static final Holder<Attribute> INFUSION_CONSUME_SKIP_CHANCE = registerPercentage("infusion_consume_skip_chance", 0.0);
    public static final Holder<Attribute> INVOKED_ENERGY_COST_CHANCE = registerPercentage("invoked_energy_cost_chance", 1.0, "invoked");
    public static final Holder<Attribute> TRIGGER_ENERGY_REFUND_CHANCE = registerPercentage("trigger_energy_refund_chance", 0.0);
    public static final Holder<Attribute> MINION_REVIVE_SPEED = registerPercentage("minion_revive_speed", 1.0);
    public static final Holder<Attribute> ACCURACY = registerDouble("accuracy", 15.0, 0.0);
    public static final Holder<Attribute> EFFECT_AREA = registerPercentage("area_of_effect", 1.0);
    // 读点存在（EchoState#statEchoCount 经 LivingAttributeValue mixin -> StatEngine.fold 真读），
    // stat 通道完整（StatIds.ECHO_COUNT + StatGen + EFF_ADD_VALUE_ATTRIBUTE_COE_ECHO_COUNT + ModifierStatEffect），
    // 但 POE2 语料中回响次数来源仅 4 个辅助宝石（已全部走 EchoMod），无装备词缀/天赋生产者 —— 按铁律不自创内容，故恒为 0。
    public static final Holder<Attribute> ECHO_COUNT = registerDouble("echo_count", 0.0, 0.0);
    public static final Holder<Attribute> ARMOR_BREAK = registerPercentage("armor_break", 0.0);
    public static final Holder<Attribute> ARMOR_BREAK_TAKEN = registerPercentage("armor_break_taken", 0.0);
    public static final Holder<Attribute> BASE_BLEEDING_DURATION = registerDouble("base_bleeding_duration", 5.0, 0.0);
    public static final Holder<Attribute> COMBO_SLOWDOWN = registerDouble("combo_slowdown", 0.0);
    public static final Holder<Attribute> PHYSICAL_DAMAGE_REDUCTION = registerPercentage("physical_damage_reduction", 0.0);
    public static final Holder<Attribute> BLOCK_CHANCE = registerPercentage("block_chance", 0.0);
    public static final Holder<Attribute> CAST_SPEED = registerDouble("cast_speed", 1.0, 0, 1024);
    public static final Holder<Attribute> COOLDOWN_RECOVERY_RATE = registerPercentage("cooldown_recovery_rate", 1.0);
    public static final Holder<Attribute> CRIT_CHANCE = registerPercentage("crit_chance", 0.05);
    public static final Holder<Attribute> CRIT_DAMAGE = registerPercentage("crit_damage", 1.0);
    public static final Holder<Attribute> CROSS_BOW_RELOAD_TIME = registerPercentage("cross_bow_reload_time", 1.0);
    public static final Holder<Attribute> DAMAGE = registerPercentage("damage", 1.0);
    public static final Holder<Attribute> DAMAGE_OVER_TIME_MULTIPLIER = registerPercentage("damage_over_time_multiplier", 1.0);
    public static final Holder<Attribute> DEFLECTION_RATING = registerPercentage("deflection_rating", 1.0);
    public static final Holder<Attribute> DODGE_CHANCE = registerPercentage("dodge_chance", 0.0);
    public static final Holder<Attribute> CANNOT_DODGE_ROLL_OR_SPRINT = registerDouble("cannot_dodge_roll_or_sprint", 0.0);
    public static final Holder<Attribute> DODGE_ROLL_DISTANCE = registerDouble("dodge_roll_distance", 0.0);
    public static final Holder<Attribute> BLIND_ON_STUN_TAKEN = registerDouble("blind_on_stun_taken", 0.0);
    public static final Holder<Attribute> CHANCE_TO_AVOID_DEATH = registerDouble("chance_to_avoid_death", 0.0);
    public static final Holder<Attribute> ARCANE_SURGE_GRANTS_HEALTH_REGEN = registerDouble("arcane_surge_grants_health_regen", 0.0);
    public static final Holder<Attribute> EVASION = registerDouble("evasion", 53.0, 0.0);
    public static final Holder<Attribute> KNOCKBACK = registerPercentage("knockback", 1.0);
    public static final Holder<Attribute> KNOCKBACK_CHANCE = registerPercentage("knockback_chance", 0.0);
    public static final Holder<Attribute> PARRY_RANGE = registerPercentage("parry_range", 1.0);
    public static final Holder<Attribute> STUN_RECOVERY = registerPercentage("stun_recovery", 1.0);
    public static final Holder<Attribute> DAMAGE_TAKEN_BYPASSES_ENERGY = registerPercentage("damage_taken_bypasses_energy", 1.0);
    public static final Holder<Attribute> TAKEN_CRIT_CHANCE = registerPercentage("taken_crit_chance", 0.0);
    public static final Holder<Attribute> TAKEN_CRIT_DAMAGE = registerPercentage("taken_crit_damage", 1.0);
    public static final Holder<Attribute> TAKEN_DAMAGE = registerPercentage("taken_damage", 1.0);
    public static final Holder<Attribute> AVOID_AILMENT_CHANCE = registerPercentage("avoid_ailment_chance", 0.0);
    public static final Holder<Attribute> RESISTANCES = registerPercentage("resistances", 0.0, "resistance");
    public static final Holder<Attribute> RESISTANCE_PENETRATION = registerPercentage("resistance_penetration", 0.0, "resistance");
    public static final Holder<Attribute> INVERT_ELEMENTAL_RESISTANCE_CHANCE = registerPercentage("invert_elemental_resistance_chance", 0.0, "resistance");
    public static final Holder<Attribute> STATUS_EXTRA_STACKS = registerPercentage("status_extra_stacks", 0.0);
    public static final Holder<Attribute> STATUS_MAX_STACKS = registerPercentage("status_max_stacks", 0.0);
    public static final Holder<Attribute> AMOUNT_LEECHED = registerPercentage("amount_leeched", 1.0);
    public static final Holder<Attribute> OFFERING_DAMAGE_BONUS = registerPercentage("offering_damage_bonus", 0.0);
    public static final Holder<Attribute> TOTEM_SPIRIT_RESERVATION = registerDouble("totem_spirit_reservation", 0.0);
    public static final Holder<Attribute> FLASK_CHARGES_GAINED = registerPercentage("flask_charges_gained", 1.0);
    public static final Holder<Attribute> FLASK_CHARGE_RATE = registerPercentage("flask_charge_rate", 0.0);
    public static final Holder<Attribute> FLASK_CHARGES_USED = registerPercentage("flask_charges_used", 1.0);
    public static final Holder<Attribute> FLASK_NO_CONSUME_CHANCE = registerPercentage("flask_no_consume_chance", 0.0);
    public static final Holder<Attribute> FLASK_RECOVERY_RATE = registerPercentage("flask_recovery_rate", 1.0);
    /** 药剂持续时间独立倍率（与全局 RECOVERY_RATE 正交，叠乘于基底时长）。 */
    public static final Holder<Attribute> FLASK_DURATION = registerPercentage("flask_duration", 1.0);
    /** 药剂即时回复比例（"X% of Recovery applied Instantly" 词缀）：饮用时一次性注入，剩余转为持续回复。 */
    public static final Holder<Attribute> FLASK_INSTANT_RECOVERY = registerPercentage("flask_instant_recovery", 0.0);
    public static final Holder<Attribute> LEECH = registerPercentage("leech", 0.0);
    public static final Holder<Attribute> LEECHES_RESPONSE = registerPercentage("leeches_response", 0.2);
    public static final Holder<Attribute> LEECH_RATE = registerPercentage("leech_rate", 1.0);
    public static final Holder<Attribute> MANA_TAKE_OF_DAMAGE = registerPercentage("mana_take_of_damage", 0.0);
    public static final Holder<Attribute> DAMAGE_TAKE_SHARE = registerPercentage("damage_take_share", 0.0);
    public static final Holder<Attribute> ATTRIBUTE_REQUIREMENTS = registerPercentage("attribute_requirements", 0.0);
    public static final Holder<Attribute> RECOVERY_RATE = registerPercentage("recovery_rate", 1.0);
    public static final Holder<Attribute> REGENERATION_RATE = registerDouble("regeneration_rate", 1.0);
    public static final Holder<Attribute> LAUNCH_ADDITIONAL_PROJECTILE = registerDouble("launch_additional_projectile", 0.0);
    public static final Holder<Attribute> MOVING_ADDITIONAL_PROJECTILE_CHANCE = registerPercentage("moving_additional_projectile_chance", 0.0);
    public static final Holder<Attribute> SURPASS_ADDITIONAL_PROJECTILE_CHANCE = registerPercentage("surpass_additional_projectile_chance", 0.0);
    public static final Holder<Attribute> ADDITIONAL_PROJECTILE_CHANCE = registerPercentage("additional_projectile_chance", 0.0);
    public static final Holder<Attribute> ADDITIONAL_PROJECTILE_COUNT = registerDouble("additional_projectile_count", 0.0);
    public static final Holder<Attribute> MARK_BLINDED = registerPercentage("mark_blinded", 0.0);
    public static final Holder<Attribute> CURSE_ACTIVATION_DELAY_MORE = registerDouble("curse_activation_delay_more", 0.0);
    public static final Holder<Attribute> CURSE_ACTIVATION_FREQUENCY = registerPercentage("curse_activation_frequency", 0.0);
    public static final Holder<Attribute> PROJECTILE_PIERCE = registerDouble("projectile_pierce", 0.0);
    public static final Holder<Attribute> PROJECTILE_PIERCE_CHANCE = registerPercentage("projectile_pierce_chance", 0.0);
    public static final Holder<Attribute> PROJECTILE_FORK_CHANCE = registerPercentage("projectile_fork_chance", 0.0);
    public static final Holder<Attribute> PROJECTILE_FORK_EXTRA_CHANCE = registerPercentage("projectile_fork_extra_chance", 0.0);
    public static final Holder<Attribute> CHAIN_CHANCE = registerPercentage("chain_chance", 0.0);
    public static final Holder<Attribute> PROJECTILE_RANDOM_FIRE = registerBoolean("projectile_random_fire", false);
    public static final Holder<Attribute> PROJECTILE_SPEED = registerPercentage("projectile_speed", 1.0);
    public static final Holder<Attribute> TALENT_AUTO_SMELT = registerDouble("talent_auto_smelt", 0.0, 0.0);
    public static final Holder<Attribute> TALENT_EXTRA_XP = registerDouble("talent_extra_xp", 0.0, 0.0);
    public static final Holder<Attribute> TALENT_FORTUNE = registerDouble("talent_fortune", 0.0, 0.0);
    public static final Holder<Attribute> TALENT_LOOTING = registerDouble("talent_looting", 0.0, 0.0);
    public static final Holder<Attribute> TALENT_MAGNET = registerDouble("talent_magnet", 0.0, 0.0);
    public static final Holder<Attribute> TALENT_MENDING = registerDouble("talent_mending", 0.0, 0.0);
    public static final Holder<Attribute> ADD_EMPOWERED_ATTACKS = registerDouble("empowered_attacks", 0.0, 0.0);
    public static final Holder<Attribute> WARCRY_MIN_POWER = registerDouble("warcry_min_power", 0.0);
    public static final Holder<Attribute> BANNER_LINGER = registerDouble("banner_linger", 0.0, 0.0);
    public static final Holder<Attribute> CORPSE_RETAIN_CHANCE = registerDouble("corpse_retain_chance", 0.0, 0.0);
    public static final Holder<Attribute> FOUND_ITEM_NUMBER = registerPercentage("found_item_number", 0.0);
    public static final Holder<Attribute> FOUND_ITEM_RARITY = registerPercentage("found_item_rarity", 0.0);
    public static final Holder<Attribute> ITEM_CAPACITY = registerDouble("item_capacity", 16.0, 0.0);
    public static final Holder<Attribute> PRESENCE_RANGE = registerDouble("presence_range", 6.0, 0.0);
    public static final Holder<Attribute> LIGHT_RADIUS = registerDouble("light_radius", 0.0, 0, 15);
    // 技能装配槽数属性；base 9，范围 [0,99]（上限远大于 SkillAssembly.SIZE=12，
    // 实际生效值由读点 clamp 到 SIZE，故上限不构成约束）。**stat 通道完整（非断链）**：
    // StatIds.SKILL_SLOT_COUNT → StatGen → StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_SKILL_SLOT_COUNT
    // → StatEffectGen 的 ModifierStatEffect(ADD_VALUE/GAIN)，
    // 读点 SkillScreen#init 经 LivingAttributeValue mixin → StatEngine.fold 取值（真实读点）。
    // POE2 语料无「+# 技能槽」装备词缀/天赋 —— 按铁律不自创内容，故当前恒为 base 9。
    public static final Holder<Attribute> SKILL_SLOT_COUNT = registerDouble("skill_slot_count", 9.0, 0, 99);

    // ===== 逆向恢复补回 [Recovered · duration/charge consts] =====
    // 下列 19 属性在 .class 逆向重建时因旧编译产物（pre-duration-split）缺失而漏建；
    // 其在全仓被 21 处引用（StatEffectGen / AilmentHelper / CharmEngine / FlaskHelper /
    // ResourceCost / StatusEngine / DamageHelper / PersistentDeliveryManager / DodgeRollHelper / StatusBehavior），
    // 故从完整备份 336.m 真值补回，声明为 final 与本品其余 const 一致（全仓无重赋值写入）。
    public static final Holder<Attribute> ARCHON_DURATION = registerPercentage("archon_duration", 1.0);
    public static final Holder<Attribute> AURA_DURATION = registerPercentage("aura_duration", 1.0);
    public static final Holder<Attribute> BANNER_DURATION = registerPercentage("banner_duration", 1.0);
    public static final Holder<Attribute> BEAST_DURATION = registerPercentage("beast_duration", 1.0);
    public static final Holder<Attribute> CHARGE_DURATION = registerPercentage("charge_duration", 1.0);
    public static final Holder<Attribute> CHARGE_RATE = registerPercentage("charge_rate", 0.0);
    public static final Holder<Attribute> CHARGES_GAINED = registerPercentage("charges_gained", 1.0);
    public static final Holder<Attribute> CHARGES_USED = registerPercentage("charges_used", 1.0);
    public static final Holder<Attribute> CHARM_DURATION = registerPercentage("charm_duration", 1.0);
    public static final Holder<Attribute> COST_EFFICIENCY = registerPercentage("cost_efficiency", 0.0);
    public static final Holder<Attribute> CURSE_DURATION = registerPercentage("curse_duration", 1.0);
    public static final Holder<Attribute> DAZE_DURATION = registerPercentage("daze_duration", 1.0);
    public static final Holder<Attribute> DEBUFF_DURATION = registerPercentage("debuff_duration", 1.0);
    public static final Holder<Attribute> GROUND_DURATION = registerPercentage("ground_duration", 1.0);
    public static final Holder<Attribute> MARK_DURATION = registerPercentage("mark_duration", 1.0);
    public static final Holder<Attribute> OFFERING_DURATION = registerPercentage("offering_duration", 1.0);
    public static final Holder<Attribute> PARRY_DURATION = registerPercentage("parry_duration", 1.0);
    public static final Holder<Attribute> STUN_DURATION = registerPercentage("stun_duration", 1.0);
    public static final Holder<Attribute> TOTEM_DURATION = registerPercentage("totem_duration", 1.0);
    // ===================== §N 已归一 — 移除属性归档 [Normalized · Removed] =====================
    // 下列属性已按归一繁律（#2：禁裸属性+硬编码消费端）移出属性层，转 INERT StatEffect 存 STAT_STORE；
    // 取消分配天赋节点即 STAT_STORE 条目移除、机制立即失效（无 Buff 残留泄漏）。
    //   ADDED_DAMAGE              → 原版 ATTACK_DAMAGE（ADD_*_DAMAGE 经 valueMod ADD_VALUE 接 Attributes.ATTACK_DAMAGE）
    //   AURA                      → PresenceAuraStatEffect（PRESENCE_ENEMIES 作用域；5580 Watchtowers 承伤光环）
    //   CORRUPTION_ENDURES_CHANCE → CorruptionEnduresStatEffect（keystone 49769，stageHealth 致死前掷骼免死）
    //   DEFEND_WITH_ARMOUR        → DefendStatEffect（4 条件变体 crit/projectile/far/low_energy，stageDefend 护甲承护）
    //   CONSUMED_CORPSE_DAMAGE    → ConsumedCorpseDamageStatEffect（26804/55405 点数，RECENTLY_CONSUMED 窗口转 inc DAMAGE）
    //   TAKE_NO_DAMAGE_VOLATILITY → TakeNoVolatilityDamageStatEffect（波动免伤 keystone，VolatilityDetonationManager 掷骼）
    //   QUARTERSTAFF_EXTRA_POWER_CHARGE_CONSUMED → MarkerStatEffect(Kind.EXTRA_CHARGE_CONSUMED)（长杖视为额外多消耗能力球）
    //   ELEMENTAL_WEAKNESS_IMMUNE → ImmuneStatEffect(StatusDefs.ELEMENTAL_WEAKNESS_CURSE)（走 StatusEngine.apply 中央免疫短路）
    //   OFFERING_IMMUNE → MarkerStatEffect(Kind.OFFERING_RECENT_IMMUNE)（近期创造祭品免疫，转 INERT MarkerStatEffect）
    //   HEAVY_STUN_GRANTS_ANCESTRAL_BOOST → GrantOnEventStatEffect(GrantTarget.ANCESTRAL_BOOST)（重晕→先祖强化，转 fire 通道）

    public static Holder<Attribute> registerDouble(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, 0.0, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue, double min) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, min, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue, int min, int max) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerBoolean(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new BooleanAttribute("attribute.name." + name, false).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerBoolean(String name, boolean defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new BooleanAttribute("attribute.name." + name, defaultValue).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, 0.0, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue, double min) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, min, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue, double min, double max) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    /**
     * 带「语义作用域标签」的注册：读取本属性时，这些标签由 {@code StatEngine} 自动并入读取作用域。
     * <p>
     * 读点只需携带本次行为的下位标签（如抗性读点带 {@code {fire}}），属性自带的语义标签
     * （如 {@code {resistance}}）由引擎统一并入，无需每个读点重复广播；漏带则要求该标签的
     * 细分条目（{@code {resistance, fire}} 之类）永不命中。
     */
    public static Holder<Attribute> registerPercentage(String name, double defaultValue, String... scopeTags) {
        Holder<Attribute> attributeHolder = registerPercentage(name, defaultValue);
        if (scopeTags != null && scopeTags.length > 0) SCOPE_TAGS.put(name, Set.of(scopeTags));
        return attributeHolder;
    }

    /** 读取本属性时自动并入作用域的语义标签（未登记则空集）。运行期调用，属性此时已绑定注册键。 */
    public static Set<String> scopeTagsOf(Holder<Attribute> attribute) {
        if (attribute == null) return Set.of();
        return attribute.unwrapKey()
                .map(key -> SCOPE_TAGS.getOrDefault(key.identifier().getPath(), Set.<String>of()))
                .orElse(Set.of());
    }
}
