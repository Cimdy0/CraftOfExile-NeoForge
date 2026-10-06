package com.cimdy.coe.data.codec.support.stat;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

/**
 * Stat 路径唯一缓存词表：集中缓存全部 Stat 引用（StatGen 注册 id 与 stat_entry 名）的 {@link Identifier}。
 * 数据生成（StatGen 注册）与所有引用方（AdditionType / StatusBehavior / SkillFactory / 天赋节点 /
 * 异常面动态生成 / offering 等）统一经本类字段常量 + {@link #byName(String)} 引用，
 * 避免散落的字符串字面量造成拼写漂移与改名遗漏。
 *
 * <p>本类由 poe2_assets/gen_talent_stats.py 生成，勿手改；重跑该脚本即可刷新。
 * 条目两类来源（统一由 node_stat_map.json 派生，单一真相源）：
 *   (1) 天赋树授予的 stat 常量：来自 node_stat_map 的 `constant` 字段（经 [Tag|Text] 取词丰侧 ->
 *       {X} 剔除 -> 极性归一 -> CamelCase 拆分 -> MC 术语替换 生成），注释为该 stat 的 PoE2 原文本；
 *   (2) 非天赋树孤儿常量（mod / skill / status 等）：当前 StatIds 中存在但 node_stat_map 未授予，
 *       原样保留，确保编译不破（不丢引用）。
 * 歧义修正（CONSTANT_REMAP）：TO_CHAOS_RESISTANCE_2 合并进 TO_CHAOS_RESISTANCE（极性由 value 符号承载）。
 * byName(path) 按真实路径（含连字符）反查，缺失即抛错（fail-fast）。
 *
 * <p>StatIds 为 Stat 路径的唯一缓存类，不再经「Ids -> StatPaths」第二层委托（StatPaths 已移除）。
 */
public final class StatIds {

    // explicit code-referenced stat id -> coe:extra_physical
    public static final Identifier EXTRA_PHYSICAL = id("extra_physical");

    // explicit code-referenced stat id -> coe:converts_all_evasion_rating
    public static final Identifier CONVERTS_ALL_EVASION_RATING = id("converts_all_evasion_rating");

    // explicit code-referenced stat id -> coe:recover_life_heavy_stun
    public static final Identifier RECOVER_LIFE_HEAVY_STUN = id("recover_life_heavy_stun");

    // explicit code-referenced stat id -> coe:recover_life_heavy_stun_heavystun
    public static final Identifier RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN = id("recover_life_heavy_stun_heavystun");

    // explicit code-referenced stat id -> coe:skill_slot_count
    // 注：BY_NAME 反射静态块已移至类末，字段声明顺序不再影响 byName 反查。
    // 本行及其注释会被 gen_talent_stats.py 作为 orphan 原样回捞重发，重跑脚本不会丢失。
    public static final Identifier SKILL_SLOT_COUNT = id("skill_slot_count");

    // explicit code-referenced stat id -> coe:recover_life_killing_poisoned
    public static final Identifier RECOVER_LIFE_KILLING_POISONED = id("recover_life_killing_poisoned");

    // explicit code-referenced stat id -> coe:recover_life_killing_poisoned_poison
    public static final Identifier RECOVER_LIFE_KILLING_POISONED_POISON = id("recover_life_killing_poisoned_poison");

    // explicit code-referenced stat id -> coe:recover_life_minions_is
    public static final Identifier RECOVER_LIFE_MINIONS_IS = id("recover_life_minions_is");

    // explicit code-referenced stat id -> coe:recover_life_minions_is_minion
    public static final Identifier RECOVER_LIFE_MINIONS_IS_MINION = id("recover_life_minions_is_minion");

    // stat_entry/accuracy.json -> coe:accuracy
    public static final Identifier ACCURACY = id("accuracy");

    // stat_entry/action_speed_attack_speed.json -> coe:action_speed_attack_speed
    public static final Identifier ACTION_SPEED_ATTACK_SPEED = id("action_speed_attack_speed");

    // stat_entry/action_speed_block_break_speed.json -> coe:action_speed_block_break_speed
    public static final Identifier ACTION_SPEED_BLOCK_BREAK_SPEED = id("action_speed_block_break_speed");

    // stat_entry/action_speed_cast_speed.json -> coe:action_speed_cast_speed
    public static final Identifier ACTION_SPEED_CAST_SPEED = id("action_speed_cast_speed");

    // stat_entry/action_speed_flying_speed.json -> coe:action_speed_flying_speed
    public static final Identifier ACTION_SPEED_FLYING_SPEED = id("action_speed_flying_speed");

    // stat_entry/action_speed_jump_strength.json -> coe:action_speed_jump_strength
    public static final Identifier ACTION_SPEED_JUMP_STRENGTH = id("action_speed_jump_strength");

    // stat_entry/action_speed_movement_speed.json -> coe:action_speed_movement_speed
    public static final Identifier ACTION_SPEED_MOVEMENT_SPEED = id("action_speed_movement_speed");

    // stat_entry/action_speed_sneaking_speed.json -> coe:action_speed_sneaking_speed
    public static final Identifier ACTION_SPEED_SNEAKING_SPEED = id("action_speed_sneaking_speed");

    // stat_entry/action_speed_submerged_mining_speed.json -> coe:action_speed_submerged_mining_speed
    public static final Identifier ACTION_SPEED_SUBMERGED_MINING_SPEED = id("action_speed_submerged_mining_speed");

    // stat_entry/add_accuracy.json -> coe:add_accuracy
    public static final Identifier ADD_ACCURACY = id("add_accuracy");

    // stat_entry/add_ailment_chance_bleed.json -> coe:add_ailment_chance_bleed
    public static final Identifier ADD_AILMENT_CHANCE_BLEED = id("add_ailment_chance_bleed");

    // stat_entry/add_ailment_chance_maim.json -> coe:add_ailment_chance_maim
    public static final Identifier ADD_AILMENT_CHANCE_MAIM = id("add_ailment_chance_maim");

    // stat_entry/add_ailment_chance_poison.json -> coe:add_ailment_chance_poison
    public static final Identifier ADD_AILMENT_CHANCE_POISON = id("add_ailment_chance_poison");

    // stat_entry/add_ailment_threshold.json -> coe:add_ailment_threshold
    public static final Identifier ADD_AILMENT_THRESHOLD = id("add_ailment_threshold");

    // stat_entry/add_armor.json -> coe:add_armor
    public static final Identifier ADD_ARMOR = id("add_armor");

    // stat_entry/add_attack_damage.json -> coe:add_attack_damage
    public static final Identifier ADD_ATTACK_DAMAGE = id("add_attack_damage");
    public static final Identifier ADD_ATTACK_SPEED = id("add_attack_speed");
    public static final Identifier ADD_ENERGY_SHIELD = id("add_energy_shield");
    public static final Identifier ADD_RELOAD_TIME = id("add_reload_time");
    public static final Identifier ADD_MOVEMENT_PENALTY = id("add_movement_penalty");

    // stat_entry/add_banner_linger.json -> coe:add_banner_linger
    public static final Identifier ADD_BANNER_LINGER = id("add_banner_linger");

    // stat_entry/add_block_chance.json -> coe:add_block_chance
    public static final Identifier ADD_BLOCK_CHANCE = id("add_block_chance");

    // stat_entry/add_cast_crit_damage.json -> coe:add_cast_crit_damage
    public static final Identifier ADD_CAST_CRIT_DAMAGE = id("add_cast_crit_damage");

    // stat_entry/add_chaos_damage.json -> coe:add_chaos_damage
    public static final Identifier ADD_CHAOS_DAMAGE = id("add_chaos_damage");

    // stat_entry/add_chaos_resistance.json -> coe:add_chaos_resistance
    public static final Identifier ADD_CHAOS_RESISTANCE = id("add_chaos_resistance");

    // stat_entry/add_cold_damage.json -> coe:add_cold_damage
    public static final Identifier ADD_COLD_DAMAGE = id("add_cold_damage");

    // stat_entry/add_cold_resistance.json -> coe:add_cold_resistance
    public static final Identifier ADD_COLD_RESISTANCE = id("add_cold_resistance");

    // stat_entry/add_corpse_retain_chance.json -> coe:add_corpse_retain_chance
    public static final Identifier ADD_CORPSE_RETAIN_CHANCE = id("add_corpse_retain_chance");

    // stat_entry/add_crit_chance.json -> coe:add_crit_chance
    public static final Identifier ADD_CRIT_CHANCE = id("add_crit_chance");

    // stat_entry/add_crit_damage.json -> coe:add_crit_damage
    public static final Identifier ADD_CRIT_DAMAGE = id("add_crit_damage");

    // stat_entry/add_dexterity.json -> coe:add_dexterity
    public static final Identifier ADD_DEXTERITY = id("add_dexterity");

    // stat_entry/add_elemental_damage.json -> coe:add_elemental_damage
    public static final Identifier ADD_ELEMENTAL_DAMAGE = id("add_elemental_damage");

    // stat_entry/add_empowered_attacks.json -> coe:add_empowered_attacks
    public static final Identifier ADD_EMPOWERED_ATTACKS = id("add_empowered_attacks");

    // stat_entry/add_energy_recharge.json -> coe:add_energy_recharge
    public static final Identifier ADD_ENERGY_RECHARGE = id("add_energy_recharge");

    // stat_entry/add_evasion.json -> coe:add_evasion
    public static final Identifier ADD_EVASION = id("add_evasion");

    // stat_entry/add_exposure.json -> coe:add_exposure
    public static final Identifier ADD_EXPOSURE = id("add_exposure");

    // stat_entry/add_fire_damage.json -> coe:add_fire_damage
    public static final Identifier ADD_FIRE_DAMAGE = id("add_fire_damage");

    // stat_entry/add_fire_resistance.json -> coe:add_fire_resistance
    public static final Identifier ADD_FIRE_RESISTANCE = id("add_fire_resistance");

    // stat_entry/add_found_item_number.json -> coe:add_found_item_number
    public static final Identifier ADD_FOUND_ITEM_NUMBER = id("add_found_item_number");

    // stat_entry/add_found_item_rarity.json -> coe:add_found_item_rarity
    public static final Identifier ADD_FOUND_ITEM_RARITY = id("add_found_item_rarity");

    // stat_entry/add_health_regeneration_rate.json -> coe:add_health_regeneration_rate
    public static final Identifier ADD_HEALTH_REGENERATION_RATE = id("add_health_regeneration_rate");

    // stat_entry/add_intelligence.json -> coe:add_intelligence
    public static final Identifier ADD_INTELLIGENCE = id("add_intelligence");

    // stat_entry/add_launch_additional_projectile_bolt.json -> coe:add_launch_additional_projectile_bolt
    public static final Identifier ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT = id("add_launch_additional_projectile_bolt");

    // stat_entry/add_launch_additional_projectile_bow.json -> coe:add_launch_additional_projectile_bow
    public static final Identifier ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW = id("add_launch_additional_projectile_bow");

    // stat_entry/add_light_damage.json -> coe:add_light_damage
    public static final Identifier ADD_LIGHT_DAMAGE = id("add_light_damage");

    // stat_entry/add_lightning_resistance.json -> coe:add_lightning_resistance
    public static final Identifier ADD_LIGHTNING_RESISTANCE = id("add_lightning_resistance");

    // stat_entry/add_max_chaos_resistance.json -> coe:add_max_chaos_resistance
    public static final Identifier ADD_MAX_CHAOS_RESISTANCE = id("add_max_chaos_resistance");

    // stat_entry/add_max_cold_resistance.json -> coe:add_max_cold_resistance
    public static final Identifier ADD_MAX_COLD_RESISTANCE = id("add_max_cold_resistance");

    // stat_entry/add_max_endurance.json -> coe:add_max_endurance
    public static final Identifier ADD_MAX_ENDURANCE = id("add_max_endurance");

    // stat_entry/add_max_energy_shield.json -> coe:add_max_energy_shield
    public static final Identifier ADD_MAX_ENERGY_SHIELD = id("add_max_energy_shield");

    // stat_entry/add_max_runic_ward.json -> coe:add_max_runic_ward  (占位：Runic Ward 引擎 effect 暂未实现)
    public static final Identifier ADD_MAX_RUNIC_WARD = id("add_max_runic_ward");

    // stat_entry/add_max_fire_resistance.json -> coe:add_max_fire_resistance
    public static final Identifier ADD_MAX_FIRE_RESISTANCE = id("add_max_fire_resistance");

    // stat_entry/add_max_frenzy.json -> coe:add_max_frenzy
    public static final Identifier ADD_MAX_FRENZY = id("add_max_frenzy");

    // stat_entry/add_max_health.json -> coe:add_max_health
    public static final Identifier ADD_MAX_HEALTH = id("add_max_health");

    // stat_entry/add_max_health_regeneration.json -> coe:add_max_health_regeneration
    public static final Identifier ADD_MAX_HEALTH_REGENERATION = id("add_max_health_regeneration");

    // stat_entry/add_max_lightning_resistance.json -> coe:add_max_lightning_resistance
    public static final Identifier ADD_MAX_LIGHTNING_RESISTANCE = id("add_max_lightning_resistance");

    // stat_entry/add_max_mana.json -> coe:add_max_mana
    public static final Identifier ADD_MAX_MANA = id("add_max_mana");

    // stat_entry/add_max_power.json -> coe:add_max_power
    public static final Identifier ADD_MAX_POWER = id("add_max_power");

    // stat_entry/add_max_resistances.json -> coe:add_max_resistances
    public static final Identifier ADD_MAX_RESISTANCES = id("add_max_resistances");

    // stat_entry/add_max_spirit.json -> coe:add_max_spirit
    public static final Identifier ADD_MAX_SPIRIT = id("add_max_spirit");

    // stat_entry/add_physical_damage.json -> coe:add_physical_damage
    public static final Identifier ADD_PHYSICAL_DAMAGE = id("add_physical_damage");

    // stat_entry/add_projectile_pierce.json -> coe:add_projectile_pierce
    public static final Identifier ADD_PROJECTILE_PIERCE = id("add_projectile_pierce");

    // stat_entry/add_projectile_pierce_chance.json -> coe:add_projectile_pierce_chance
    public static final Identifier ADD_PROJECTILE_PIERCE_CHANCE = id("add_projectile_pierce_chance");

    // stat_entry/add_recently_consumed_damage.json -> coe:add_recently_consumed_damage
    public static final Identifier ADD_RECENTLY_CONSUMED_DAMAGE = id("add_recently_consumed_damage");

    // stat_entry/add_strength.json -> coe:add_strength
    public static final Identifier ADD_STRENGTH = id("add_strength");

    // stat_entry/add_stun_threshold.json -> coe:add_stun_threshold
    public static final Identifier ADD_STUN_THRESHOLD = id("add_stun_threshold");

    // stat_entry/adrenaline_attack_speed.json -> coe:adrenaline_attack_speed
    public static final Identifier ADRENALINE_ATTACK_SPEED = id("adrenaline_attack_speed");

    // stat_entry/adrenaline_cast_speed.json -> coe:adrenaline_cast_speed
    public static final Identifier ADRENALINE_CAST_SPEED = id("adrenaline_cast_speed");

    // stat_entry/adrenaline_damage.json -> coe:adrenaline_damage
    public static final Identifier ADRENALINE_DAMAGE = id("adrenaline_damage");

    // stat_entry/adrenaline_movement_speed.json -> coe:adrenaline_movement_speed
    public static final Identifier ADRENALINE_MOVEMENT_SPEED = id("adrenaline_movement_speed");

    // stat_entry/adrenaline_taken_physical.json -> coe:adrenaline_taken_physical
    public static final Identifier ADRENALINE_TAKEN_PHYSICAL = id("adrenaline_taken_physical");

    // stat_entry/aggravate_bleeding_on_hit.json -> coe:aggravate_bleeding_on_hit
    public static final Identifier AGGRAVATE_BLEEDING_ON_HIT = id("aggravate_bleeding_on_hit");

    // stat_entry/ailment_buildup_bleed.json -> coe:ailment_buildup_bleed
    public static final Identifier AILMENT_BUILDUP_BLEED = id("ailment_buildup_bleed");

    // stat_entry/ailment_buildup_chill.json -> coe:ailment_buildup_chill
    public static final Identifier AILMENT_BUILDUP_CHILL = id("ailment_buildup_chill");

    // stat_entry/ailment_buildup_electrocute.json -> coe:ailment_buildup_electrocute
    public static final Identifier AILMENT_BUILDUP_ELECTROCUTE = id("ailment_buildup_electrocute");

    // stat_entry/ailment_buildup_freeze.json -> coe:ailment_buildup_freeze
    public static final Identifier AILMENT_BUILDUP_FREEZE = id("ailment_buildup_freeze");

    // stat_entry/ailment_buildup_ignite.json -> coe:ailment_buildup_ignite
    public static final Identifier AILMENT_BUILDUP_IGNITE = id("ailment_buildup_ignite");

    // stat_entry/ailment_buildup_pin.json -> coe:ailment_buildup_pin
    public static final Identifier AILMENT_BUILDUP_PIN = id("ailment_buildup_pin");

    // stat_entry/ailment_buildup_poison.json -> coe:ailment_buildup_poison
    public static final Identifier AILMENT_BUILDUP_POISON = id("ailment_buildup_poison");

    // stat_entry/ailment_buildup_shock.json -> coe:ailment_buildup_shock
    public static final Identifier AILMENT_BUILDUP_SHOCK = id("ailment_buildup_shock");

    // stat_entry/ailment_duration_bleed.json -> coe:ailment_duration_bleed
    public static final Identifier AILMENT_DURATION_BLEED = id("ailment_duration_bleed");

    // stat_entry/ailment_duration_chill.json -> coe:ailment_duration_chill
    public static final Identifier AILMENT_DURATION_CHILL = id("ailment_duration_chill");

    // stat_entry/ailment_duration_electrocute.json -> coe:ailment_duration_electrocute
    public static final Identifier AILMENT_DURATION_ELECTROCUTE = id("ailment_duration_electrocute");

    // stat_entry/ailment_duration_freeze.json -> coe:ailment_duration_freeze
    public static final Identifier AILMENT_DURATION_FREEZE = id("ailment_duration_freeze");

    // stat_entry/ailment_duration_ignite.json -> coe:ailment_duration_ignite
    public static final Identifier AILMENT_DURATION_IGNITE = id("ailment_duration_ignite");

    // stat_entry/ailment_duration_pin.json -> coe:ailment_duration_pin
    public static final Identifier AILMENT_DURATION_PIN = id("ailment_duration_pin");

    // stat_entry/ailment_duration_poison.json -> coe:ailment_duration_poison
    public static final Identifier AILMENT_DURATION_POISON = id("ailment_duration_poison");

    // stat_entry/ailment_duration_shock.json -> coe:ailment_duration_shock
    public static final Identifier AILMENT_DURATION_SHOCK = id("ailment_duration_shock");

    // stat_entry/ailment_magnitude_bleed.json -> coe:ailment_magnitude_bleed
    public static final Identifier AILMENT_MAGNITUDE_BLEED = id("ailment_magnitude_bleed");

    // stat_entry/ailment_magnitude_chill.json -> coe:ailment_magnitude_chill
    public static final Identifier AILMENT_MAGNITUDE_CHILL = id("ailment_magnitude_chill");

    // stat_entry/ailment_magnitude_electrocute.json -> coe:ailment_magnitude_electrocute
    public static final Identifier AILMENT_MAGNITUDE_ELECTROCUTE = id("ailment_magnitude_electrocute");

    // stat_entry/ailment_magnitude_freeze.json -> coe:ailment_magnitude_freeze
    public static final Identifier AILMENT_MAGNITUDE_FREEZE = id("ailment_magnitude_freeze");

    // stat_entry/ailment_magnitude_ignite.json -> coe:ailment_magnitude_ignite
    public static final Identifier AILMENT_MAGNITUDE_IGNITE = id("ailment_magnitude_ignite");

    // stat_entry/ailment_magnitude_pin.json -> coe:ailment_magnitude_pin
    public static final Identifier AILMENT_MAGNITUDE_PIN = id("ailment_magnitude_pin");

    // stat_entry/ailment_magnitude_poison.json -> coe:ailment_magnitude_poison
    public static final Identifier AILMENT_MAGNITUDE_POISON = id("ailment_magnitude_poison");

    // stat_entry/ailment_magnitude_shock.json -> coe:ailment_magnitude_shock
    public static final Identifier AILMENT_MAGNITUDE_SHOCK = id("ailment_magnitude_shock");

    // stat_entry/arcane_surge_cast_speed.json -> coe:arcane_surge_cast_speed
    public static final Identifier ARCANE_SURGE_CAST_SPEED = id("arcane_surge_cast_speed");

    // stat_entry/arcane_surge_regen.json -> coe:arcane_surge_regen
    public static final Identifier ARCANE_SURGE_REGEN = id("arcane_surge_regen");

    // stat_entry/archon_freeze_buildup.json -> coe:archon_freeze_buildup
    public static final Identifier ARCHON_FREEZE_BUILDUP = id("archon_freeze_buildup");

    // stat_entry/archon_ignite_buildup.json -> coe:archon_ignite_buildup
    public static final Identifier ARCHON_IGNITE_BUILDUP = id("archon_ignite_buildup");

    // stat_entry/archon_nullify_chaos.json -> coe:archon_nullify_chaos
    public static final Identifier ARCHON_NULLIFY_CHAOS = id("archon_nullify_chaos");

    // stat_entry/archon_nullify_physical.json -> coe:archon_nullify_physical
    public static final Identifier ARCHON_NULLIFY_PHYSICAL = id("archon_nullify_physical");

    // stat_entry/archon_shock_magnitude.json -> coe:archon_shock_magnitude
    public static final Identifier ARCHON_SHOCK_MAGNITUDE = id("archon_shock_magnitude");

    // stat_entry/armor_break_fully.json -> coe:armor_break_fully
    public static final Identifier ARMOR_BREAK_FULLY = id("armor_break_fully");

    // stat_entry/armor_reduction.json -> coe:armor_reduction
    public static final Identifier ARMOR_REDUCTION = id("armor_reduction");

    // stat_entry/banner_attack_speed.json -> coe:banner_attack_speed
    public static final Identifier BANNER_ATTACK_SPEED = id("banner_attack_speed");

    // stat_entry/banner_cast_speed.json -> coe:banner_cast_speed
    public static final Identifier BANNER_CAST_SPEED = id("banner_cast_speed");

    // stat_entry/banner_movement_speed.json -> coe:banner_movement_speed
    public static final Identifier BANNER_MOVEMENT_SPEED = id("banner_movement_speed");

    // stat_entry/chance_aggravate_on_hit_vs_bleed_old.json -> coe:chance_aggravate_on_hit_vs_bleed_old
    public static final Identifier CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD = id("chance_aggravate_on_hit_vs_bleed_old");

    // stat_entry/chance_aggravate_on_hit_vs_jagged.json -> coe:chance_aggravate_on_hit_vs_jagged
    public static final Identifier CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED = id("chance_aggravate_on_hit_vs_jagged");

    // stat_entry/chance_aggravate_on_hit_vs_pinned.json -> coe:chance_aggravate_on_hit_vs_pinned
    public static final Identifier CHANCE_AGGRAVATE_ON_HIT_VS_PINNED = id("chance_aggravate_on_hit_vs_pinned");

    // stat_entry/chance_attacks_accuracy_rating_equal.json -> coe:chance_attacks_accuracy_rating_equal
    public static final Identifier CHANCE_ATTACKS_ACCURACY_RATING_EQUAL = id("chance_attacks_accuracy_rating_equal");

    // stat_entry/chance_attacks_accuracy_rating_equal_critical.json -> coe:chance_attacks_accuracy_rating_equal_critical
    public static final Identifier CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL = id("chance_attacks_accuracy_rating_equal_critical");

    // stat_entry/chance_chill.json -> coe:chance_chill
    public static final Identifier CHANCE_CHILL = id("chance_chill");

    // stat_entry/chance_daze_on_hit.json -> coe:chance_daze_on_hit
    public static final Identifier CHANCE_DAZE_ON_HIT = id("chance_daze_on_hit");

    // stat_entry/chance_debilitate_on_hit.json -> coe:chance_debilitate_on_hit
    public static final Identifier CHANCE_DEBILITATE_ON_HIT = id("chance_debilitate_on_hit");

    // stat_entry/chance_dodge.json -> coe:chance_dodge
    public static final Identifier CHANCE_DODGE = id("chance_dodge");

    // stat_entry/chance_flask_no_consume.json -> coe:chance_flask_no_consume
    public static final Identifier CHANCE_FLASK_NO_CONSUME = id("chance_flask_no_consume");

    // stat_entry/chance_freeze.json -> coe:chance_freeze
    public static final Identifier CHANCE_FREEZE = id("chance_freeze");

    // stat_entry/chance_ignite.json -> coe:chance_ignite
    public static final Identifier CHANCE_IGNITE = id("chance_ignite");

    // stat_entry/chance_onslaught_on_kill.json -> coe:chance_onslaught_on_kill
    public static final Identifier CHANCE_ONSLAUGHT_ON_KILL = id("chance_onslaught_on_kill");

    // stat_entry/charge_convert_gain_max_endurance.json -> coe:charge_convert_gain_max_endurance
    public static final Identifier CHARGE_CONVERT_GAIN_MAX_ENDURANCE = id("charge_convert_gain_max_endurance");

    // stat_entry/charge_convert_gain_max_frenzy.json -> coe:charge_convert_gain_max_frenzy
    public static final Identifier CHARGE_CONVERT_GAIN_MAX_FRENZY = id("charge_convert_gain_max_frenzy");

    // stat_entry/charge_convert_gain_max_power.json -> coe:charge_convert_gain_max_power
    public static final Identifier CHARGE_CONVERT_GAIN_MAX_POWER = id("charge_convert_gain_max_power");

    // stat_entry/charm_golden_rarity.json -> coe:charm_golden_rarity
    public static final Identifier CHARM_GOLDEN_RARITY = id("charm_golden_rarity");

    // stat_entry/charm_immune_antidote.json -> coe:charm_immune_antidote
    public static final Identifier CHARM_IMMUNE_ANTIDOTE = id("charm_immune_antidote");

    // stat_entry/charm_immune_dousing.json -> coe:charm_immune_dousing
    public static final Identifier CHARM_IMMUNE_DOUSING = id("charm_immune_dousing");

    // stat_entry/charm_immune_grounding.json -> coe:charm_immune_grounding
    public static final Identifier CHARM_IMMUNE_GROUNDING = id("charm_immune_grounding");

    // stat_entry/charm_immune_silver.json -> coe:charm_immune_silver
    public static final Identifier CHARM_IMMUNE_SILVER = id("charm_immune_silver");

    // stat_entry/charm_immune_staunching.json -> coe:charm_immune_staunching
    public static final Identifier CHARM_IMMUNE_STAUNCHING = id("charm_immune_staunching");

    // stat_entry/charm_immune_stone.json -> coe:charm_immune_stone
    public static final Identifier CHARM_IMMUNE_STONE = id("charm_immune_stone");

    // stat_entry/charm_immune_thawing.json -> coe:charm_immune_thawing
    public static final Identifier CHARM_IMMUNE_THAWING = id("charm_immune_thawing");

    // stat_entry/charm_resist_chaos.json -> coe:charm_resist_chaos
    public static final Identifier CHARM_RESIST_CHAOS = id("charm_resist_chaos");

    // stat_entry/charm_resist_cold.json -> coe:charm_resist_cold
    public static final Identifier CHARM_RESIST_COLD = id("charm_resist_cold");

    // stat_entry/charm_resist_fire.json -> coe:charm_resist_fire
    public static final Identifier CHARM_RESIST_FIRE = id("charm_resist_fire");

    // stat_entry/charm_resist_lightning.json -> coe:charm_resist_lightning
    public static final Identifier CHARM_RESIST_LIGHTNING = id("charm_resist_lightning");

    // stat_entry/convert_extra_generic_to_cold.json -> coe:convert_extra_generic_to_cold
    public static final Identifier CONVERT_EXTRA_GENERIC_TO_COLD = id("convert_extra_generic_to_cold");

    // stat_entry/convert_extra_generic_to_lightning.json -> coe:convert_extra_generic_to_lightning
    public static final Identifier CONVERT_EXTRA_GENERIC_TO_LIGHTNING = id("convert_extra_generic_to_lightning");

    // stat_entry/convert_extra_generic_to_physical.json -> coe:convert_extra_generic_to_physical
    public static final Identifier CONVERT_EXTRA_GENERIC_TO_PHYSICAL = id("convert_extra_generic_to_physical");

    // stat_entry/convert_extra_phys_to_cold.json -> coe:convert_extra_phys_to_cold
    public static final Identifier CONVERT_EXTRA_PHYS_TO_COLD = id("convert_extra_phys_to_cold");

    // stat_entry/convert_extra_phys_to_fire.json -> coe:convert_extra_phys_to_fire
    public static final Identifier CONVERT_EXTRA_PHYS_TO_FIRE = id("convert_extra_phys_to_fire");

    // stat_entry/convert_extra_phys_to_lightning.json -> coe:convert_extra_phys_to_lightning
    public static final Identifier CONVERT_EXTRA_PHYS_TO_LIGHTNING = id("convert_extra_phys_to_lightning");

    // stat_entry/damage_dealt.json -> coe:damage_dealt
    public static final Identifier DAMAGE_DEALT = id("damage_dealt");

    // stat_entry/damage_dealt_bear_fire.json -> coe:damage_dealt_bear_fire
    public static final Identifier DAMAGE_DEALT_BEAR_FIRE = id("damage_dealt_bear_fire");

    // stat_entry/damage_dealt_bear_physical.json -> coe:damage_dealt_bear_physical
    public static final Identifier DAMAGE_DEALT_BEAR_PHYSICAL = id("damage_dealt_bear_physical");

    // stat_entry/damage_dealt_elemental_spell.json -> coe:damage_dealt_elemental_spell
    public static final Identifier DAMAGE_DEALT_ELEMENTAL_SPELL = id("damage_dealt_elemental_spell");

    // stat_entry/damage_dealt_wolf_cold.json -> coe:damage_dealt_wolf_cold
    public static final Identifier DAMAGE_DEALT_WOLF_COLD = id("damage_dealt_wolf_cold");

    // stat_entry/damage_dealt_wolf_physical.json -> coe:damage_dealt_wolf_physical
    public static final Identifier DAMAGE_DEALT_WOLF_PHYSICAL = id("damage_dealt_wolf_physical");

    // stat_entry/damage_dealt_wyvern_chaos.json -> coe:damage_dealt_wyvern_chaos
    public static final Identifier DAMAGE_DEALT_WYVERN_CHAOS = id("damage_dealt_wyvern_chaos");

    // stat_entry/damage_dealt_wyvern_lightning.json -> coe:damage_dealt_wyvern_lightning
    public static final Identifier DAMAGE_DEALT_WYVERN_LIGHTNING = id("damage_dealt_wyvern_lightning");

    // stat_entry/damage_lucky.json -> coe:damage_lucky
    public static final Identifier DAMAGE_LUCKY = id("damage_lucky");

    // stat_entry/damage_lucky_vs_low_life.json -> coe:damage_lucky_vs_low_life
    public static final Identifier DAMAGE_LUCKY_VS_LOW_LIFE = id("damage_lucky_vs_low_life");

    // stat_entry/damage_taken.json -> coe:damage_taken
    public static final Identifier DAMAGE_TAKEN = id("damage_taken");

    // stat_entry/damage_taken_chaos.json -> coe:damage_taken_chaos
    public static final Identifier DAMAGE_TAKEN_CHAOS = id("damage_taken_chaos");

    // stat_entry/damage_taken_physical.json -> coe:damage_taken_physical
    public static final Identifier DAMAGE_TAKEN_PHYSICAL = id("damage_taken_physical");

    // stat_entry/dec_rage_loss_rate.json -> coe:dec_rage_loss_rate
    public static final Identifier DEC_RAGE_LOSS_RATE = id("dec_rage_loss_rate");

    // stat_entry/delivery_extra_projectile.json -> coe:delivery_extra_projectile
    public static final Identifier DELIVERY_EXTRA_PROJECTILE = id("delivery_extra_projectile");

    // stat_entry/delivery_extra_projectile_spell.json -> coe:delivery_extra_projectile_spell
    public static final Identifier DELIVERY_EXTRA_PROJECTILE_SPELL = id("delivery_extra_projectile_spell");

    // stat_entry/delivery_fork.json -> coe:delivery_fork
    public static final Identifier DELIVERY_FORK = id("delivery_fork");

    // stat_entry/effectmod_fire_resist_provide_30.json -> coe:effectmod_fire_resist_provide_30
    public static final Identifier EFFECTMOD_FIRE_RESIST_PROVIDE_30 = id("effectmod_fire_resist_provide_30");

    // stat_entry/effectmod_resist_provide_cold_5.json -> coe:effectmod_resist_provide_cold_5
    public static final Identifier EFFECTMOD_RESIST_PROVIDE_COLD_5 = id("effectmod_resist_provide_cold_5");

    // stat_entry/effectmod_resist_provide_fire_5.json -> coe:effectmod_resist_provide_fire_5
    public static final Identifier EFFECTMOD_RESIST_PROVIDE_FIRE_5 = id("effectmod_resist_provide_fire_5");

    // stat_entry/effectmod_resist_provide_lightning_5.json -> coe:effectmod_resist_provide_lightning_5
    public static final Identifier EFFECTMOD_RESIST_PROVIDE_LIGHTNING_5 = id("effectmod_resist_provide_lightning_5");

    // stat_entry/evasion.json -> coe:evasion
    public static final Identifier EVASION = id("evasion");

    // stat_entry/exposure_fire.json -> coe:exposure_fire
    public static final Identifier EXPOSURE_FIRE = id("exposure_fire");

    // stat_entry/exposure_cold.json -> coe:exposure_cold
    public static final Identifier EXPOSURE_COLD = id("exposure_cold");

    // stat_entry/exposure_lightning.json -> coe:exposure_lightning
    public static final Identifier EXPOSURE_LIGHTNING = id("exposure_lightning");

    // stat_entry/extra_chaos_from_chaos.json -> coe:extra_chaos_from_chaos
    public static final Identifier EXTRA_CHAOS_FROM_CHAOS = id("extra_chaos_from_chaos");

    // stat_entry/extra_cold_from_chaos.json -> coe:extra_cold_from_chaos
    public static final Identifier EXTRA_COLD_FROM_CHAOS = id("extra_cold_from_chaos");

    // stat_entry/extra_fire_damage.json -> coe:extra_fire_damage
    public static final Identifier EXTRA_FIRE_DAMAGE = id("extra_fire_damage");

    // stat_entry/extra_lightning_damage.json -> coe:extra_lightning_damage
    public static final Identifier EXTRA_LIGHTNING_DAMAGE = id("extra_lightning_damage");

    // stat_entry/extra_physical_damage.json -> coe:extra_physical_damage
    public static final Identifier EXTRA_PHYSICAL_DAMAGE = id("extra_physical_damage");

    // stat_entry/extra_physical_from_chaos.json -> coe:extra_physical_from_chaos
    public static final Identifier EXTRA_PHYSICAL_FROM_CHAOS = id("extra_physical_from_chaos");

    // stat_entry/extra_physical_from_cold.json -> coe:extra_physical_from_cold
    public static final Identifier EXTRA_PHYSICAL_FROM_COLD = id("extra_physical_from_cold");

    // stat_entry/extra_physical_from_fire.json -> coe:extra_physical_from_fire
    public static final Identifier EXTRA_PHYSICAL_FROM_FIRE = id("extra_physical_from_fire");

    // stat_entry/extra_physical_from_lightning.json -> coe:extra_physical_from_lightning
    public static final Identifier EXTRA_PHYSICAL_FROM_LIGHTNING = id("extra_physical_from_lightning");

    // stat_entry/extra_physical_from_physical.json -> coe:extra_physical_from_physical
    public static final Identifier EXTRA_PHYSICAL_FROM_PHYSICAL = id("extra_physical_from_physical");

    // stat_entry/extra_random_element_random_shapeshifted.json -> coe:extra_random_element_random_shapeshifted
    public static final Identifier EXTRA_RANDOM_ELEMENT_RANDOM_SHAPESHIFTED = id("extra_random_element_random_shapeshifted");

    // stat_entry/flask_mana_recovery.json -> coe:flask_mana_recovery
    public static final Identifier FLASK_MANA_RECOVERY = id("flask_mana_recovery");

    // stat_entry/flask_no_consume.json -> coe:flask_no_consume
    public static final Identifier FLASK_NO_CONSUME = id("flask_no_consume");

    // stat_entry/form_bear_armor.json -> coe:form_bear_armor
    public static final Identifier FORM_BEAR_ARMOR = id("form_bear_armor");

    // stat_entry/form_bear_attack_speed.json -> coe:form_bear_attack_speed
    public static final Identifier FORM_BEAR_ATTACK_SPEED = id("form_bear_attack_speed");

    // stat_entry/form_wolf_attack_speed.json -> coe:form_wolf_attack_speed
    public static final Identifier FORM_WOLF_ATTACK_SPEED = id("form_wolf_attack_speed");

    // stat_entry/form_wolf_movement_speed.json -> coe:form_wolf_movement_speed
    public static final Identifier FORM_WOLF_MOVEMENT_SPEED = id("form_wolf_movement_speed");

    // stat_entry/form_wyvern_crit_chance.json -> coe:form_wyvern_crit_chance
    public static final Identifier FORM_WYVERN_CRIT_CHANCE = id("form_wyvern_crit_chance");

    // stat_entry/form_wyvern_crit_damage.json -> coe:form_wyvern_crit_damage
    public static final Identifier FORM_WYVERN_CRIT_DAMAGE = id("form_wyvern_crit_damage");

    // stat_entry/gain_of_damage_as_extra_chaos_damage_from_chaos.json -> coe:gain_of_damage_as_extra_chaos_damage_from_chaos
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_CHAOS = id("gain_of_damage_as_extra_chaos_damage_from_chaos");

    // stat_entry/gain_of_damage_as_extra_chaos_damage_from_cold.json -> coe:gain_of_damage_as_extra_chaos_damage_from_cold
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_COLD = id("gain_of_damage_as_extra_chaos_damage_from_cold");

    // stat_entry/gain_of_damage_as_extra_chaos_damage_from_fire.json -> coe:gain_of_damage_as_extra_chaos_damage_from_fire
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_FIRE = id("gain_of_damage_as_extra_chaos_damage_from_fire");

    // stat_entry/gain_of_damage_as_extra_chaos_damage_from_lightning.json -> coe:gain_of_damage_as_extra_chaos_damage_from_lightning
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_LIGHTNING = id("gain_of_damage_as_extra_chaos_damage_from_lightning");

    // stat_entry/gain_of_damage_as_extra_chaos_damage_from_physical.json -> coe:gain_of_damage_as_extra_chaos_damage_from_physical
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_PHYSICAL = id("gain_of_damage_as_extra_chaos_damage_from_physical");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_chaos.json -> coe:gain_of_damage_as_extra_cold_damage_from_chaos
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS = id("gain_of_damage_as_extra_cold_damage_from_chaos");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_chaos_on_chilled.json -> coe:gain_of_damage_as_extra_cold_damage_from_chaos_on_chilled
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_ON_CHILLED = id("gain_of_damage_as_extra_cold_damage_from_chaos_on_chilled");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_chaos_shapeshifted.json -> coe:gain_of_damage_as_extra_cold_damage_from_chaos_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_from_chaos_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_chaos_vs_daze.json -> coe:gain_of_damage_as_extra_cold_damage_from_chaos_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_VS_DAZE = id("gain_of_damage_as_extra_cold_damage_from_chaos_vs_daze");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_chaos_vs_shock.json -> coe:gain_of_damage_as_extra_cold_damage_from_chaos_vs_shock
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_VS_SHOCK = id("gain_of_damage_as_extra_cold_damage_from_chaos_vs_shock");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_cold.json -> coe:gain_of_damage_as_extra_cold_damage_from_cold
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD = id("gain_of_damage_as_extra_cold_damage_from_cold");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_cold_on_chilled.json -> coe:gain_of_damage_as_extra_cold_damage_from_cold_on_chilled
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_ON_CHILLED = id("gain_of_damage_as_extra_cold_damage_from_cold_on_chilled");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_cold_shapeshifted.json -> coe:gain_of_damage_as_extra_cold_damage_from_cold_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_from_cold_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_cold_vs_daze.json -> coe:gain_of_damage_as_extra_cold_damage_from_cold_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_VS_DAZE = id("gain_of_damage_as_extra_cold_damage_from_cold_vs_daze");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_cold_vs_shock.json -> coe:gain_of_damage_as_extra_cold_damage_from_cold_vs_shock
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_VS_SHOCK = id("gain_of_damage_as_extra_cold_damage_from_cold_vs_shock");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_fire.json -> coe:gain_of_damage_as_extra_cold_damage_from_fire
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE = id("gain_of_damage_as_extra_cold_damage_from_fire");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_fire_on_chilled.json -> coe:gain_of_damage_as_extra_cold_damage_from_fire_on_chilled
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_ON_CHILLED = id("gain_of_damage_as_extra_cold_damage_from_fire_on_chilled");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_fire_shapeshifted.json -> coe:gain_of_damage_as_extra_cold_damage_from_fire_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_from_fire_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_fire_vs_daze.json -> coe:gain_of_damage_as_extra_cold_damage_from_fire_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_VS_DAZE = id("gain_of_damage_as_extra_cold_damage_from_fire_vs_daze");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_fire_vs_shock.json -> coe:gain_of_damage_as_extra_cold_damage_from_fire_vs_shock
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_VS_SHOCK = id("gain_of_damage_as_extra_cold_damage_from_fire_vs_shock");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_lightning.json -> coe:gain_of_damage_as_extra_cold_damage_from_lightning
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING = id("gain_of_damage_as_extra_cold_damage_from_lightning");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_lightning_on_chilled.json -> coe:gain_of_damage_as_extra_cold_damage_from_lightning_on_chilled
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_ON_CHILLED = id("gain_of_damage_as_extra_cold_damage_from_lightning_on_chilled");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_lightning_shapeshifted.json -> coe:gain_of_damage_as_extra_cold_damage_from_lightning_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_from_lightning_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_lightning_vs_daze.json -> coe:gain_of_damage_as_extra_cold_damage_from_lightning_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_VS_DAZE = id("gain_of_damage_as_extra_cold_damage_from_lightning_vs_daze");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_lightning_vs_shock.json -> coe:gain_of_damage_as_extra_cold_damage_from_lightning_vs_shock
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_VS_SHOCK = id("gain_of_damage_as_extra_cold_damage_from_lightning_vs_shock");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_physical.json -> coe:gain_of_damage_as_extra_cold_damage_from_physical
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL = id("gain_of_damage_as_extra_cold_damage_from_physical");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_physical_on_chilled.json -> coe:gain_of_damage_as_extra_cold_damage_from_physical_on_chilled
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_ON_CHILLED = id("gain_of_damage_as_extra_cold_damage_from_physical_on_chilled");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_physical_shapeshifted.json -> coe:gain_of_damage_as_extra_cold_damage_from_physical_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_from_physical_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_physical_vs_daze.json -> coe:gain_of_damage_as_extra_cold_damage_from_physical_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_VS_DAZE = id("gain_of_damage_as_extra_cold_damage_from_physical_vs_daze");

    // stat_entry/gain_of_damage_as_extra_cold_damage_from_physical_vs_shock.json -> coe:gain_of_damage_as_extra_cold_damage_from_physical_vs_shock
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_VS_SHOCK = id("gain_of_damage_as_extra_cold_damage_from_physical_vs_shock");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_chaos.json -> coe:gain_of_damage_as_extra_fire_damage_from_chaos
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS = id("gain_of_damage_as_extra_fire_damage_from_chaos");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_chaos_on_ignited.json -> coe:gain_of_damage_as_extra_fire_damage_from_chaos_on_ignited
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_ON_IGNITED = id("gain_of_damage_as_extra_fire_damage_from_chaos_on_ignited");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_chaos_shapeshifted.json -> coe:gain_of_damage_as_extra_fire_damage_from_chaos_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_from_chaos_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_chaos_vs_freeze.json -> coe:gain_of_damage_as_extra_fire_damage_from_chaos_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_VS_FREEZE = id("gain_of_damage_as_extra_fire_damage_from_chaos_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_chaos_vs_heavy_stun.json -> coe:gain_of_damage_as_extra_fire_damage_from_chaos_vs_heavy_stun
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_VS_HEAVY_STUN = id("gain_of_damage_as_extra_fire_damage_from_chaos_vs_heavy_stun");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_cold.json -> coe:gain_of_damage_as_extra_fire_damage_from_cold
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD = id("gain_of_damage_as_extra_fire_damage_from_cold");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_cold_on_ignited.json -> coe:gain_of_damage_as_extra_fire_damage_from_cold_on_ignited
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_ON_IGNITED = id("gain_of_damage_as_extra_fire_damage_from_cold_on_ignited");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_cold_shapeshifted.json -> coe:gain_of_damage_as_extra_fire_damage_from_cold_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_from_cold_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_cold_vs_freeze.json -> coe:gain_of_damage_as_extra_fire_damage_from_cold_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_VS_FREEZE = id("gain_of_damage_as_extra_fire_damage_from_cold_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_cold_vs_heavy_stun.json -> coe:gain_of_damage_as_extra_fire_damage_from_cold_vs_heavy_stun
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_VS_HEAVY_STUN = id("gain_of_damage_as_extra_fire_damage_from_cold_vs_heavy_stun");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_fire.json -> coe:gain_of_damage_as_extra_fire_damage_from_fire
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE = id("gain_of_damage_as_extra_fire_damage_from_fire");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_fire_on_ignited.json -> coe:gain_of_damage_as_extra_fire_damage_from_fire_on_ignited
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_ON_IGNITED = id("gain_of_damage_as_extra_fire_damage_from_fire_on_ignited");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_fire_shapeshifted.json -> coe:gain_of_damage_as_extra_fire_damage_from_fire_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_from_fire_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_fire_vs_freeze.json -> coe:gain_of_damage_as_extra_fire_damage_from_fire_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_VS_FREEZE = id("gain_of_damage_as_extra_fire_damage_from_fire_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_fire_vs_heavy_stun.json -> coe:gain_of_damage_as_extra_fire_damage_from_fire_vs_heavy_stun
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_VS_HEAVY_STUN = id("gain_of_damage_as_extra_fire_damage_from_fire_vs_heavy_stun");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_lightning.json -> coe:gain_of_damage_as_extra_fire_damage_from_lightning
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING = id("gain_of_damage_as_extra_fire_damage_from_lightning");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_lightning_on_ignited.json -> coe:gain_of_damage_as_extra_fire_damage_from_lightning_on_ignited
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_ON_IGNITED = id("gain_of_damage_as_extra_fire_damage_from_lightning_on_ignited");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_lightning_shapeshifted.json -> coe:gain_of_damage_as_extra_fire_damage_from_lightning_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_from_lightning_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_lightning_vs_freeze.json -> coe:gain_of_damage_as_extra_fire_damage_from_lightning_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_VS_FREEZE = id("gain_of_damage_as_extra_fire_damage_from_lightning_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_lightning_vs_heavy_stun.json -> coe:gain_of_damage_as_extra_fire_damage_from_lightning_vs_heavy_stun
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_VS_HEAVY_STUN = id("gain_of_damage_as_extra_fire_damage_from_lightning_vs_heavy_stun");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_physical.json -> coe:gain_of_damage_as_extra_fire_damage_from_physical
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL = id("gain_of_damage_as_extra_fire_damage_from_physical");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_physical_on_ignited.json -> coe:gain_of_damage_as_extra_fire_damage_from_physical_on_ignited
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_ON_IGNITED = id("gain_of_damage_as_extra_fire_damage_from_physical_on_ignited");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_physical_shapeshifted.json -> coe:gain_of_damage_as_extra_fire_damage_from_physical_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_from_physical_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_physical_vs_freeze.json -> coe:gain_of_damage_as_extra_fire_damage_from_physical_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_VS_FREEZE = id("gain_of_damage_as_extra_fire_damage_from_physical_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_fire_damage_from_physical_vs_heavy_stun.json -> coe:gain_of_damage_as_extra_fire_damage_from_physical_vs_heavy_stun
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_VS_HEAVY_STUN = id("gain_of_damage_as_extra_fire_damage_from_physical_vs_heavy_stun");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS = id("gain_of_damage_as_extra_lightning_damage_from_chaos");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos_on_shocked.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos_on_shocked
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_ON_SHOCKED = id("gain_of_damage_as_extra_lightning_damage_from_chaos_on_shocked");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos_shapeshifted.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_from_chaos_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos_vs_daze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_DAZE = id("gain_of_damage_as_extra_lightning_damage_from_chaos_vs_daze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos_vs_electrocute.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos_vs_electrocute
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_ELECTROCUTE = id("gain_of_damage_as_extra_lightning_damage_from_chaos_vs_electrocute");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_chaos_vs_freeze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_chaos_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_FREEZE = id("gain_of_damage_as_extra_lightning_damage_from_chaos_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD = id("gain_of_damage_as_extra_lightning_damage_from_cold");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold_on_shocked.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold_on_shocked
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_ON_SHOCKED = id("gain_of_damage_as_extra_lightning_damage_from_cold_on_shocked");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold_shapeshifted.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_from_cold_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold_vs_daze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_DAZE = id("gain_of_damage_as_extra_lightning_damage_from_cold_vs_daze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold_vs_electrocute.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold_vs_electrocute
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_ELECTROCUTE = id("gain_of_damage_as_extra_lightning_damage_from_cold_vs_electrocute");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_cold_vs_freeze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_cold_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_FREEZE = id("gain_of_damage_as_extra_lightning_damage_from_cold_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE = id("gain_of_damage_as_extra_lightning_damage_from_fire");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire_on_shocked.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire_on_shocked
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_ON_SHOCKED = id("gain_of_damage_as_extra_lightning_damage_from_fire_on_shocked");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire_shapeshifted.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_from_fire_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire_vs_daze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_DAZE = id("gain_of_damage_as_extra_lightning_damage_from_fire_vs_daze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire_vs_electrocute.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire_vs_electrocute
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_ELECTROCUTE = id("gain_of_damage_as_extra_lightning_damage_from_fire_vs_electrocute");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_fire_vs_freeze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_fire_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_FREEZE = id("gain_of_damage_as_extra_lightning_damage_from_fire_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING = id("gain_of_damage_as_extra_lightning_damage_from_lightning");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning_on_shocked.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning_on_shocked
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_ON_SHOCKED = id("gain_of_damage_as_extra_lightning_damage_from_lightning_on_shocked");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning_shapeshifted.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_from_lightning_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning_vs_daze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_DAZE = id("gain_of_damage_as_extra_lightning_damage_from_lightning_vs_daze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning_vs_electrocute.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning_vs_electrocute
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_ELECTROCUTE = id("gain_of_damage_as_extra_lightning_damage_from_lightning_vs_electrocute");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_lightning_vs_freeze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_lightning_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_FREEZE = id("gain_of_damage_as_extra_lightning_damage_from_lightning_vs_freeze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL = id("gain_of_damage_as_extra_lightning_damage_from_physical");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical_on_shocked.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical_on_shocked
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_ON_SHOCKED = id("gain_of_damage_as_extra_lightning_damage_from_physical_on_shocked");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical_shapeshifted.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical_shapeshifted
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_from_physical_shapeshifted");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical_vs_daze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical_vs_daze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_DAZE = id("gain_of_damage_as_extra_lightning_damage_from_physical_vs_daze");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical_vs_electrocute.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical_vs_electrocute
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_ELECTROCUTE = id("gain_of_damage_as_extra_lightning_damage_from_physical_vs_electrocute");

    // stat_entry/gain_of_damage_as_extra_lightning_damage_from_physical_vs_freeze.json -> coe:gain_of_damage_as_extra_lightning_damage_from_physical_vs_freeze
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_FREEZE = id("gain_of_damage_as_extra_lightning_damage_from_physical_vs_freeze");

    // stat_entry/grant_charge_on_gain.json -> coe:grant_charge_on_gain
    public static final Identifier GRANT_CHARGE_ON_GAIN = id("grant_charge_on_gain");

    // stat_entry/grant_combo_on_hit.json -> coe:grant_combo_on_hit
    public static final Identifier GRANT_COMBO_ON_HIT = id("grant_combo_on_hit");

    // stat_entry/grant_endurance_charge_on_gain.json -> coe:grant_endurance_charge_on_gain
    public static final Identifier GRANT_ENDURANCE_CHARGE_ON_GAIN = id("grant_endurance_charge_on_gain");

    // stat_entry/grant_energy_on_volatility_detonate.json -> coe:grant_energy_on_volatility_detonate
    public static final Identifier GRANT_ENERGY_ON_VOLATILITY_DETONATE = id("grant_energy_on_volatility_detonate");

    // stat_entry/grant_energy_refund.json -> coe:grant_energy_refund
    public static final Identifier GRANT_ENERGY_REFUND = id("grant_energy_refund");

    // stat_entry/grant_frenzy_charge_on_consume_power.json -> coe:grant_frenzy_charge_on_consume_power
    public static final Identifier GRANT_FRENZY_CHARGE_ON_CONSUME_POWER = id("grant_frenzy_charge_on_consume_power");

    // stat_entry/grant_frenzy_charge_on_gain.json -> coe:grant_frenzy_charge_on_gain
    public static final Identifier GRANT_FRENZY_CHARGE_ON_GAIN = id("grant_frenzy_charge_on_gain");

    // stat_entry/grant_intimidate_on_heavy_stun.json -> coe:grant_intimidate_on_heavy_stun
    public static final Identifier GRANT_INTIMIDATE_ON_HEAVY_STUN = id("grant_intimidate_on_heavy_stun");

    // stat_entry/grant_power_charge_on_gain.json -> coe:grant_power_charge_on_gain
    public static final Identifier GRANT_POWER_CHARGE_ON_GAIN = id("grant_power_charge_on_gain");

    // stat_entry/grant_volatility_on_kill.json -> coe:grant_volatility_on_kill
    public static final Identifier GRANT_VOLATILITY_ON_KILL = id("grant_volatility_on_kill");

    // stat_entry/grenade_damage.json -> coe:grenade_damage
    public static final Identifier GRENADE_DAMAGE = id("grenade_damage");

    // stat_entry/hazard_damage.json -> coe:hazard_damage
    public static final Identifier HAZARD_DAMAGE = id("hazard_damage");

    // stat_entry/herald_damage.json -> coe:herald_damage
    public static final Identifier HERALD_DAMAGE = id("herald_damage");

    // stat_entry/inc_ailment_chance.json -> coe:inc_ailment_chance
    public static final Identifier INC_AILMENT_CHANCE = id("inc_ailment_chance");

    // stat_entry/inc_ailment_chance_vs_exposed_cold.json -> coe:inc_ailment_chance_vs_exposed_cold
    public static final Identifier INC_AILMENT_CHANCE_VS_EXPOSED_COLD = id("inc_ailment_chance_vs_exposed_cold");

    // stat_entry/inc_ailment_chance_vs_exposed_fire.json -> coe:inc_ailment_chance_vs_exposed_fire
    public static final Identifier INC_AILMENT_CHANCE_VS_EXPOSED_FIRE = id("inc_ailment_chance_vs_exposed_fire");

    // stat_entry/inc_ailment_chance_vs_exposed_lightning.json -> coe:inc_ailment_chance_vs_exposed_lightning
    public static final Identifier INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING = id("inc_ailment_chance_vs_exposed_lightning");

    // stat_entry/inc_ailment_damage_rate_bleed.json -> coe:inc_ailment_damage_rate_bleed
    public static final Identifier INC_AILMENT_DAMAGE_RATE_BLEED = id("inc_ailment_damage_rate_bleed");

    // stat_entry/inc_ailment_damage_rate_poison.json -> coe:inc_ailment_damage_rate_poison
    public static final Identifier INC_AILMENT_DAMAGE_RATE_POISON = id("inc_ailment_damage_rate_poison");

    // stat_entry/inc_ailment_duration.json -> coe:inc_ailment_duration
    public static final Identifier INC_AILMENT_DURATION = id("inc_ailment_duration");

    // stat_entry/inc_ailment_duration_vs_exposed_cold.json -> coe:inc_ailment_duration_vs_exposed_cold
    public static final Identifier INC_AILMENT_DURATION_VS_EXPOSED_COLD = id("inc_ailment_duration_vs_exposed_cold");

    // stat_entry/inc_ailment_duration_vs_exposed_fire.json -> coe:inc_ailment_duration_vs_exposed_fire
    public static final Identifier INC_AILMENT_DURATION_VS_EXPOSED_FIRE = id("inc_ailment_duration_vs_exposed_fire");

    // stat_entry/inc_ailment_duration_vs_exposed_lightning.json -> coe:inc_ailment_duration_vs_exposed_lightning
    public static final Identifier INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING = id("inc_ailment_duration_vs_exposed_lightning");

    // stat_entry/inc_ailment_magnitude.json -> coe:inc_ailment_magnitude
    public static final Identifier INC_AILMENT_MAGNITUDE = id("inc_ailment_magnitude");

    // stat_entry/inc_amount_leech_health.json -> coe:inc_amount_leech_health
    public static final Identifier INC_AMOUNT_LEECH_HEALTH = id("inc_amount_leech_health");

    // stat_entry/inc_armour.json -> coe:inc_armour
    public static final Identifier INC_ARMOUR = id("inc_armour");

    // stat_entry/inc_armour_energy.json -> coe:inc_armour_energy
    public static final Identifier INC_ARMOUR_ENERGY = id("inc_armour_energy");

    // stat_entry/inc_armour_evasion.json -> coe:inc_armour_evasion
    public static final Identifier INC_ARMOUR_EVASION = id("inc_armour_evasion");

    // stat_entry/inc_attack_damage_physical.json -> coe:inc_attack_damage_physical
    public static final Identifier INC_ATTACK_DAMAGE_PHYSICAL = id("inc_attack_damage_physical");

    // stat_entry/inc_attack_elemental_damage.json -> coe:inc_attack_elemental_damage
    public static final Identifier INC_ATTACK_ELEMENTAL_DAMAGE = id("inc_attack_elemental_damage");

    // stat_entry/inc_aura_magnitude.json -> coe:inc_aura_magnitude
    public static final Identifier INC_AURA_MAGNITUDE = id("inc_aura_magnitude");

    // stat_entry/inc_banner_damage.json -> coe:inc_banner_damage
    public static final Identifier INC_BANNER_DAMAGE = id("inc_banner_damage");

    // stat_entry/inc_beast_ailment_duration.json -> coe:inc_beast_ailment_duration
    public static final Identifier INC_BEAST_AILMENT_DURATION = id("inc_beast_ailment_duration");

    // stat_entry/inc_bleed_duration.json -> coe:inc_bleed_duration
    public static final Identifier INC_BLEED_DURATION = id("inc_bleed_duration");

    // stat_entry/inc_bleeding_duration_taken.json -> coe:inc_bleeding_duration_taken
    public static final Identifier INC_BLEEDING_DURATION_TAKEN = id("inc_bleeding_duration_taken");

    // stat_entry/inc_buff_effect_taken.json -> coe:inc_buff_effect_taken
    public static final Identifier INC_BUFF_EFFECT_TAKEN = id("inc_buff_effect_taken");

    // stat_entry/inc_buff_magnitude.json -> coe:inc_buff_magnitude
    public static final Identifier INC_BUFF_MAGNITUDE = id("inc_buff_magnitude");

    // stat_entry/inc_charm_duration.json -> coe:inc_charm_duration
    public static final Identifier INC_CHARM_DURATION = id("inc_charm_duration");

    // stat_entry/inc_charm_effect.json -> coe:inc_charm_effect
    public static final Identifier INC_CHARM_EFFECT = id("inc_charm_effect");

    // stat_entry/inc_chill_duration_taken.json -> coe:inc_chill_duration_taken
    public static final Identifier INC_CHILL_DURATION_TAKEN = id("inc_chill_duration_taken");

    // stat_entry/inc_cold_damage_spell.json -> coe:inc_cold_damage_spell
    public static final Identifier INC_COLD_DAMAGE_SPELL = id("inc_cold_damage_spell");

    // stat_entry/inc_crit_chance.json -> coe:inc_crit_chance
    public static final Identifier INC_CRIT_CHANCE = id("inc_crit_chance");

    // stat_entry/inc_crossbow_damage.json -> coe:inc_crossbow_damage
    public static final Identifier INC_CROSSBOW_DAMAGE = id("inc_crossbow_damage");

    // stat_entry/inc_curse_damage.json -> coe:inc_curse_damage
    public static final Identifier INC_CURSE_DAMAGE = id("inc_curse_damage");

    // stat_entry/inc_damage_attack.json -> coe:inc_damage_attack
    public static final Identifier INC_DAMAGE_ATTACK = id("inc_damage_attack");

    // stat_entry/inc_damage_blocking.json -> coe:inc_damage_blocking
    public static final Identifier INC_DAMAGE_BLOCKING = id("inc_damage_blocking");

    // stat_entry/inc_damage_empowered_attacks.json -> coe:inc_damage_empowered_attacks
    public static final Identifier INC_DAMAGE_EMPOWERED_ATTACKS = id("inc_damage_empowered_attacks");

    // stat_entry/inc_damage_is_taken_mana.json -> coe:inc_damage_is_taken_mana
    public static final Identifier INC_DAMAGE_IS_TAKEN_MANA = id("inc_damage_is_taken_mana");

    // stat_entry/inc_damage_low_life.json -> coe:inc_damage_low_life
    public static final Identifier INC_DAMAGE_LOW_LIFE = id("inc_damage_low_life");

    // stat_entry/inc_damage_over_time.json -> coe:inc_damage_over_time
    public static final Identifier INC_DAMAGE_OVER_TIME = id("inc_damage_over_time");

    // stat_entry/inc_damage_parrying.json -> coe:inc_damage_parrying
    public static final Identifier INC_DAMAGE_PARRYING = id("inc_damage_parrying");

    // stat_entry/inc_damage_presence_allies.json -> coe:inc_damage_presence_allies
    public static final Identifier INC_DAMAGE_PRESENCE_ALLIES = id("inc_damage_presence_allies");

    // stat_entry/inc_damage_presence_enemies.json -> coe:inc_damage_presence_enemies
    public static final Identifier INC_DAMAGE_PRESENCE_ENEMIES = id("inc_damage_presence_enemies");

    // stat_entry/inc_damage_recently_killed.json -> coe:inc_damage_recently_killed
    public static final Identifier INC_DAMAGE_RECENTLY_KILLED = id("inc_damage_recently_killed");

    // stat_entry/inc_damage_shapeshifted.json -> coe:inc_damage_shapeshifted
    public static final Identifier INC_DAMAGE_SHAPESHIFTED = id("inc_damage_shapeshifted");

    // stat_entry/inc_debuff_expiry.json -> coe:inc_debuff_expiry
    public static final Identifier INC_DEBUFF_EXPIRY = id("inc_debuff_expiry");

    // stat_entry/inc_deflection_rating_equal_evasion.json -> coe:inc_deflection_rating_equal_evasion
    public static final Identifier INC_DEFLECTION_RATING_EQUAL_EVASION = id("inc_deflection_rating_equal_evasion");

    // stat_entry/inc_deflection_rating_equal_evasion_deflect.json -> coe:inc_deflection_rating_equal_evasion_deflect
    public static final Identifier INC_DEFLECTION_RATING_EQUAL_EVASION_DEFLECT = id("inc_deflection_rating_equal_evasion_deflect");

    // stat_entry/inc_duration_of_puppet_master.json -> coe:inc_duration_of_puppet_master
    public static final Identifier INC_DURATION_OF_PUPPET_MASTER = id("inc_duration_of_puppet_master");

    // stat_entry/inc_energy_regeneration_rate.json -> coe:inc_energy_regeneration_rate
    public static final Identifier INC_ENERGY_REGENERATION_RATE = id("inc_energy_regeneration_rate");

    // stat_entry/inc_faster_ailments.json -> coe:inc_faster_ailments
    public static final Identifier INC_FASTER_AILMENTS = id("inc_faster_ailments");

    // stat_entry/inc_faster_start_energy_shield.json -> coe:inc_faster_start_energy_shield
    public static final Identifier INC_FASTER_START_ENERGY_SHIELD = id("inc_faster_start_energy_shield");

    // stat_entry/inc_faster_start_energy_shield_fasteresrechargestart.json -> coe:inc_faster_start_energy_shield_fasteresrechargestart
    public static final Identifier INC_FASTER_START_ENERGY_SHIELD_FASTERESRECHARGESTART = id("inc_faster_start_energy_shield_fasteresrechargestart");

    // stat_entry/inc_fire_damage_spell.json -> coe:inc_fire_damage_spell
    public static final Identifier INC_FIRE_DAMAGE_SPELL = id("inc_fire_damage_spell");


    // stat_entry/inc_flask_life_recovery.json -> coe:inc_flask_life_recovery
    public static final Identifier INC_FLASK_LIFE_RECOVERY = id("inc_flask_life_recovery");

    // stat_entry/inc_flask_mana_recovery.json -> coe:inc_flask_mana_recovery
    public static final Identifier INC_FLASK_MANA_RECOVERY = id("inc_flask_mana_recovery");

    // stat_entry/inc_found_quantity.json -> coe:inc_found_quantity
    public static final Identifier INC_FOUND_QUANTITY = id("inc_found_quantity");

    // stat_entry/inc_found_rarity.json -> coe:inc_found_rarity
    public static final Identifier INC_FOUND_RARITY = id("inc_found_rarity");

    // stat_entry/inc_freeze_duration_taken.json -> coe:inc_freeze_duration_taken
    public static final Identifier INC_FREEZE_DURATION_TAKEN = id("inc_freeze_duration_taken");

    // stat_entry/inc_herald_damage.json -> coe:inc_herald_damage
    public static final Identifier INC_HERALD_DAMAGE = id("inc_herald_damage");

    // stat_entry/inc_ignite_duration_taken.json -> coe:inc_ignite_duration_taken
    public static final Identifier INC_IGNITE_DURATION_TAKEN = id("inc_ignite_duration_taken");

    // stat_entry/inc_ignite_faster.json -> coe:inc_ignite_faster
    public static final Identifier INC_IGNITE_FASTER = id("inc_ignite_faster");

    // stat_entry/inc_ignite_magnitude_taken.json -> coe:inc_ignite_magnitude_taken
    public static final Identifier INC_IGNITE_MAGNITUDE_TAKEN = id("inc_ignite_magnitude_taken");

    // stat_entry/inc_incision_magnitude.json -> coe:inc_incision_magnitude
    public static final Identifier INC_INCISION_MAGNITUDE = id("inc_incision_magnitude");

    // stat_entry/inc_increases_reductions_projectile_speed.json -> coe:inc_increases_reductions_projectile_speed
    public static final Identifier INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED = id("inc_increases_reductions_projectile_speed");

    // stat_entry/inc_increases_reductions_projectile_speed_bow.json -> coe:inc_increases_reductions_projectile_speed_bow
    public static final Identifier INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW = id("inc_increases_reductions_projectile_speed_bow");

    // stat_entry/inc_infusion_duration.json -> coe:inc_infusion_duration
    public static final Identifier INC_INFUSION_DURATION = id("inc_infusion_duration");

    // stat_entry/inc_infusion_magnitude.json -> coe:inc_infusion_magnitude
    public static final Identifier INC_INFUSION_MAGNITUDE = id("inc_infusion_magnitude");

    // stat_entry/inc_intelligence_2.json -> coe:inc_intelligence_2
    public static final Identifier INC_INTELLIGENCE_2 = id("inc_intelligence_2");

    // stat_entry/inc_leech_health.json -> coe:inc_leech_health
    public static final Identifier INC_LEECH_HEALTH = id("inc_leech_health");

    // stat_entry/inc_leech_rate.json -> coe:inc_leech_rate
    public static final Identifier INC_LEECH_RATE = id("inc_leech_rate");

    // stat_entry/inc_light_damage.json -> coe:inc_light_damage
    public static final Identifier INC_LIGHT_DAMAGE = id("inc_light_damage");

    // stat_entry/inc_light_damage_spell.json -> coe:inc_light_damage_spell
    public static final Identifier INC_LIGHT_DAMAGE_SPELL = id("inc_light_damage_spell");

    // stat_entry/inc_mana_take.json -> coe:inc_mana_take
    public static final Identifier INC_MANA_TAKE = id("inc_mana_take");

    // stat_entry/inc_mark_damage.json -> coe:inc_mark_damage
    public static final Identifier INC_MARK_DAMAGE = id("inc_mark_damage");

    // stat_entry/inc_mark_effect.json -> coe:inc_mark_effect
    public static final Identifier INC_MARK_EFFECT = id("inc_mark_effect");

    // stat_entry/inc_max_energy.json -> coe:inc_max_energy
    public static final Identifier INC_MAX_ENERGY = id("inc_max_energy");

    // stat_entry/inc_max_rage.json -> coe:inc_max_rage
    public static final Identifier INC_MAX_RAGE = id("inc_max_rage");

    // stat_entry/inc_max_volatility.json -> coe:inc_max_volatility
    public static final Identifier INC_MAX_VOLATILITY = id("inc_max_volatility");

    // stat_entry/inc_meta_damage.json -> coe:inc_meta_damage
    public static final Identifier INC_META_DAMAGE = id("inc_meta_damage");

    // stat_entry/inc_minion_attack_speed.json -> coe:inc_minion_attack_speed
    public static final Identifier INC_MINION_ATTACK_SPEED = id("inc_minion_attack_speed");

    // stat_entry/inc_minion_damage.json -> coe:inc_minion_damage
    public static final Identifier INC_MINION_DAMAGE = id("inc_minion_damage");

    // stat_entry/inc_minion_elemental_resist.json -> coe:inc_minion_elemental_resist
    public static final Identifier INC_MINION_ELEMENTAL_RESIST = id("inc_minion_elemental_resist");

    // stat_entry/inc_minion_max_health.json -> coe:inc_minion_max_health
    public static final Identifier INC_MINION_MAX_HEALTH = id("inc_minion_max_health");

    // stat_entry/inc_minion_movement_speed.json -> coe:inc_minion_movement_speed
    public static final Identifier INC_MINION_MOVEMENT_SPEED = id("inc_minion_movement_speed");

    // stat_entry/inc_minion_revive.json -> coe:inc_minion_revive
    public static final Identifier INC_MINION_REVIVE = id("inc_minion_revive");

    // stat_entry/inc_minions_attack_cast_speed.json -> coe:inc_minions_attack_cast_speed
    public static final Identifier INC_MINIONS_ATTACK_CAST_SPEED = id("inc_minions_attack_cast_speed");

    // stat_entry/inc_minions_cooldown_recovery_rate.json -> coe:inc_minions_cooldown_recovery_rate
    public static final Identifier INC_MINIONS_COOLDOWN_RECOVERY_RATE = id("inc_minions_cooldown_recovery_rate");

    // stat_entry/inc_minions_cooldown_recovery_rate_command.json -> coe:inc_minions_cooldown_recovery_rate_command
    public static final Identifier INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND = id("inc_minions_cooldown_recovery_rate_command");

    // stat_entry/inc_movement_speed_stationary.json -> coe:inc_movement_speed_stationary
    public static final Identifier INC_MOVEMENT_SPEED_STATIONARY = id("inc_movement_speed_stationary");

    // stat_entry/inc_non_damaging_ailment_magnitude.json -> coe:inc_non_damaging_ailment_magnitude
    public static final Identifier INC_NON_DAMAGING_AILMENT_MAGNITUDE = id("inc_non_damaging_ailment_magnitude");

    // stat_entry/inc_offering_damage.json -> coe:inc_offering_damage
    public static final Identifier INC_OFFERING_DAMAGE = id("inc_offering_damage");

    // stat_entry/inc_offering_duration.json -> coe:inc_offering_duration
    public static final Identifier INC_OFFERING_DURATION = id("inc_offering_duration");

    // stat_entry/inc_pin_buildup.json -> coe:inc_pin_buildup
    public static final Identifier INC_PIN_BUILDUP = id("inc_pin_buildup");

    // stat_entry/inc_poison_duration_taken.json -> coe:inc_poison_duration_taken
    public static final Identifier INC_POISON_DURATION_TAKEN = id("inc_poison_duration_taken");

    // stat_entry/inc_poison_magnitude.json -> coe:inc_poison_magnitude
    public static final Identifier INC_POISON_MAGNITUDE = id("inc_poison_magnitude");

    // stat_entry/inc_projectile_pierce.json -> coe:inc_projectile_pierce
    public static final Identifier INC_PROJECTILE_PIERCE = id("inc_projectile_pierce");

    // stat_entry/inc_rage_loss_grace.json -> coe:inc_rage_loss_grace
    public static final Identifier INC_RAGE_LOSS_GRACE = id("inc_rage_loss_grace");

    // stat_entry/inc_regenerate_life_affected_any.json -> coe:inc_regenerate_life_affected_any
    public static final Identifier INC_REGENERATE_LIFE_AFFECTED_ANY = id("inc_regenerate_life_affected_any");

    // stat_entry/inc_regenerate_life_affected_any_while_affected.json -> coe:inc_regenerate_life_affected_any_while_affected
    public static final Identifier INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED = id("inc_regenerate_life_affected_any_while_affected");

    // stat_entry/inc_regenerate_life_low.json -> coe:inc_regenerate_life_low
    public static final Identifier INC_REGENERATE_LIFE_LOW = id("inc_regenerate_life_low");

    // stat_entry/inc_regenerate_life_low_while_on.json -> coe:inc_regenerate_life_low_while_on
    public static final Identifier INC_REGENERATE_LIFE_LOW_WHILE_ON = id("inc_regenerate_life_low_while_on");

    // stat_entry/inc_regenerate_life_surrounded.json -> coe:inc_regenerate_life_surrounded
    public static final Identifier INC_REGENERATE_LIFE_SURROUNDED = id("inc_regenerate_life_surrounded");

    // stat_entry/inc_regenerate_life_surrounded_per_second.json -> coe:inc_regenerate_life_surrounded_per_second
    public static final Identifier INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND = id("inc_regenerate_life_surrounded_per_second");

    // stat_entry/inc_regenerate_life_totem.json -> coe:inc_regenerate_life_totem
    public static final Identifier INC_REGENERATE_LIFE_TOTEM = id("inc_regenerate_life_totem");

    // stat_entry/inc_regenerate_life_totem_while_you.json -> coe:inc_regenerate_life_totem_while_you
    public static final Identifier INC_REGENERATE_LIFE_TOTEM_WHILE_YOU = id("inc_regenerate_life_totem_while_you");

    // stat_entry/inc_regenerate_life_ve_flask.json -> coe:inc_regenerate_life_ve_flask
    public static final Identifier INC_REGENERATE_LIFE_VE_FLASK = id("inc_regenerate_life_ve_flask");

    // stat_entry/inc_regenerate_life_ve_flask_per_second.json -> coe:inc_regenerate_life_ve_flask_per_second
    public static final Identifier INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND = id("inc_regenerate_life_ve_flask_per_second");

    // stat_entry/inc_reload_speed.json -> coe:inc_reload_speed
    public static final Identifier INC_RELOAD_SPEED = id("inc_reload_speed");

    // stat_entry/inc_reservation_efficiency.json -> coe:inc_reservation_efficiency
    public static final Identifier INC_RESERVATION_EFFICIENCY = id("inc_reservation_efficiency");

    // stat_entry/inc_shock_duration_taken.json -> coe:inc_shock_duration_taken
    public static final Identifier INC_SHOCK_DURATION_TAKEN = id("inc_shock_duration_taken");

    // stat_entry/inc_shock_magnitude.json -> coe:inc_shock_magnitude
    public static final Identifier INC_SHOCK_MAGNITUDE = id("inc_shock_magnitude");

    // stat_entry/inc_slower_start_energy_shield.json -> coe:inc_slower_start_energy_shield
    public static final Identifier INC_SLOWER_START_ENERGY_SHIELD = id("inc_slower_start_energy_shield");

    // stat_entry/inc_slower_start_energy_shield_no_condition.json -> coe:inc_slower_start_energy_shield_no_condition
    public static final Identifier INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION = id("inc_slower_start_energy_shield_no_condition");

    // stat_entry/inc_stun_duration.json -> coe:inc_stun_duration
    public static final Identifier INC_STUN_DURATION = id("inc_stun_duration");

    // stat_entry/inc_take_no_damage_volatility.json -> coe:inc_take_no_damage_volatility
    public static final Identifier INC_TAKE_NO_DAMAGE_VOLATILITY = id("inc_take_no_damage_volatility");

    // stat_entry/inc_taken_crit_damage.json -> coe:inc_taken_crit_damage
    public static final Identifier INC_TAKEN_CRIT_DAMAGE = id("inc_taken_crit_damage");

    // stat_entry/inc_taken_damage.json -> coe:inc_taken_damage
    public static final Identifier INC_TAKEN_DAMAGE = id("inc_taken_damage");

    // stat_entry/inc_trap_throw_speed.json -> coe:inc_trap_throw_speed
    public static final Identifier INC_TRAP_THROW_SPEED = id("inc_trap_throw_speed");

    // stat_entry/inc_volatility_generation.json -> coe:inc_volatility_generation
    public static final Identifier INC_VOLATILITY_GENERATION = id("inc_volatility_generation");

    // stat_entry/inc_warcry_damage.json -> coe:inc_warcry_damage
    public static final Identifier INC_WARCRY_DAMAGE = id("inc_warcry_damage");

    // stat_entry/leech_health.json -> coe:leech_health
    public static final Identifier LEECH_HEALTH = id("leech_health");

    // stat_entry/leech_mana.json -> coe:leech_mana
    public static final Identifier LEECH_MANA = id("leech_mana");

    // stat_entry/leech_trigger.json -> coe:leech_trigger
    public static final Identifier LEECH_TRIGGER = id("leech_trigger");

    // stat_entry/life_per_kill.json -> coe:life_per_kill
    public static final Identifier LIFE_PER_KILL = id("life_per_kill");

    // stat_entry/livingmod_armor_4.json -> coe:livingmod_armor_4
    public static final Identifier LIVINGMOD_ARMOR_4 = id("livingmod_armor_4");

    // stat_entry/livingmod_cave_attack_damage.json -> coe:livingmod_cave_attack_damage
    public static final Identifier LIVINGMOD_CAVE_ATTACK_DAMAGE = id("livingmod_cave_attack_damage");

    // stat_entry/livingmod_coe_accuracy_op_add_multiplied_base_2.json -> coe:livingmod_coe_accuracy_op_add_multiplied_base_2
    public static final Identifier LIVINGMOD_COE_ACCURACY_OP_ADD_MULTIPLIED_BASE_2 = id("livingmod_coe_accuracy_op_add_multiplied_base_2");

    // stat_entry/livingmod_coe_ailment_buildup_op_add_value_1_stun.json -> coe:livingmod_coe_ailment_buildup_op_add_value_1_stun
    public static final Identifier LIVINGMOD_COE_AILMENT_BUILDUP_OP_ADD_VALUE_1_STUN = id("livingmod_coe_ailment_buildup_op_add_value_1_stun");

    // stat_entry/livingmod_coe_ailment_chance_op_add_value_1_bleed.json -> coe:livingmod_coe_ailment_chance_op_add_value_1_bleed
    public static final Identifier LIVINGMOD_COE_AILMENT_CHANCE_OP_ADD_VALUE_1_BLEED = id("livingmod_coe_ailment_chance_op_add_value_1_bleed");

    // stat_entry/livingmod_coe_ailment_chance_op_add_value_1_poison.json -> coe:livingmod_coe_ailment_chance_op_add_value_1_poison
    public static final Identifier LIVINGMOD_COE_AILMENT_CHANCE_OP_ADD_VALUE_1_POISON = id("livingmod_coe_ailment_chance_op_add_value_1_poison");

    // stat_entry/livingmod_coe_ailment_threshold_op_add_value_1_5_stun.json -> coe:livingmod_coe_ailment_threshold_op_add_value_1_5_stun
    public static final Identifier LIVINGMOD_COE_AILMENT_THRESHOLD_OP_ADD_VALUE_1_5_STUN = id("livingmod_coe_ailment_threshold_op_add_value_1_5_stun");

    // stat_entry/livingmod_coe_armor_break_op_add_value_0_1_armor_break.json -> coe:livingmod_coe_armor_break_op_add_value_0_1_armor_break
    public static final Identifier LIVINGMOD_COE_ARMOR_BREAK_OP_ADD_VALUE_0_1_ARMOR_BREAK = id("livingmod_coe_armor_break_op_add_value_0_1_armor_break");

    // stat_entry/livingmod_coe_cast_speed_op_add_value_0_3.json -> coe:livingmod_coe_cast_speed_op_add_value_0_3
    public static final Identifier LIVINGMOD_COE_CAST_SPEED_OP_ADD_VALUE_0_3 = id("livingmod_coe_cast_speed_op_add_value_0_3");

    // stat_entry/livingmod_coe_crit_chance_op_add_value_0_2_attack_crit.json -> coe:livingmod_coe_crit_chance_op_add_value_0_2_attack_crit
    public static final Identifier LIVINGMOD_COE_CRIT_CHANCE_OP_ADD_VALUE_0_2_ATTACK_CRIT = id("livingmod_coe_crit_chance_op_add_value_0_2_attack_crit");

    // stat_entry/livingmod_coe_damage_op_add_value_0_4_chaos_damage.json -> coe:livingmod_coe_damage_op_add_value_0_4_chaos_damage
    public static final Identifier LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_CHAOS_DAMAGE = id("livingmod_coe_damage_op_add_value_0_4_chaos_damage");

    // stat_entry/livingmod_coe_damage_op_add_value_0_4_cold_damage.json -> coe:livingmod_coe_damage_op_add_value_0_4_cold_damage
    public static final Identifier LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_COLD_DAMAGE = id("livingmod_coe_damage_op_add_value_0_4_cold_damage");

    // stat_entry/livingmod_coe_damage_op_add_value_0_4_damage_fire.json -> coe:livingmod_coe_damage_op_add_value_0_4_damage_fire
    public static final Identifier LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_DAMAGE_FIRE = id("livingmod_coe_damage_op_add_value_0_4_damage_fire");

    // stat_entry/livingmod_coe_damage_op_add_value_0_4_damage_lightning.json -> coe:livingmod_coe_damage_op_add_value_0_4_damage_lightning
    public static final Identifier LIVINGMOD_COE_DAMAGE_OP_ADD_VALUE_0_4_DAMAGE_LIGHTNING = id("livingmod_coe_damage_op_add_value_0_4_damage_lightning");

    // stat_entry/livingmod_coe_launch_additional_projectile_op_add_value_4.json -> coe:livingmod_coe_launch_additional_projectile_op_add_value_4
    public static final Identifier LIVINGMOD_COE_LAUNCH_ADDITIONAL_PROJECTILE_OP_ADD_VALUE_4 = id("livingmod_coe_launch_additional_projectile_op_add_value_4");

    // stat_entry/livingmod_coe_mana_take_of_damage_op_add_value_0_3_mana_take.json -> coe:livingmod_coe_mana_take_of_damage_op_add_value_0_3_mana_take
    public static final Identifier LIVINGMOD_COE_MANA_TAKE_OF_DAMAGE_OP_ADD_VALUE_0_3_MANA_TAKE = id("livingmod_coe_mana_take_of_damage_op_add_value_0_3_mana_take");

    // stat_entry/livingmod_coe_max_regeneration_op_add_value_0_02_health_regen.json -> coe:livingmod_coe_max_regeneration_op_add_value_0_02_health_regen
    public static final Identifier LIVINGMOD_COE_MAX_REGENERATION_OP_ADD_VALUE_0_02_HEALTH_REGEN = id("livingmod_coe_max_regeneration_op_add_value_0_02_health_regen");

    // stat_entry/livingmod_coe_max_resistances_op_add_value_0_1_cold.json -> coe:livingmod_coe_max_resistances_op_add_value_0_1_cold
    public static final Identifier LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_COLD = id("livingmod_coe_max_resistances_op_add_value_0_1_cold");

    // stat_entry/livingmod_coe_max_resistances_op_add_value_0_1_fire.json -> coe:livingmod_coe_max_resistances_op_add_value_0_1_fire
    public static final Identifier LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_FIRE = id("livingmod_coe_max_resistances_op_add_value_0_1_fire");

    // stat_entry/livingmod_coe_max_resistances_op_add_value_0_1_lightning.json -> coe:livingmod_coe_max_resistances_op_add_value_0_1_lightning
    public static final Identifier LIVINGMOD_COE_MAX_RESISTANCES_OP_ADD_VALUE_0_1_LIGHTNING = id("livingmod_coe_max_resistances_op_add_value_0_1_lightning");

    // stat_entry/livingmod_coe_resistances_op_add_value_0_5_chaos.json -> coe:livingmod_coe_resistances_op_add_value_0_5_chaos
    public static final Identifier LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_CHAOS = id("livingmod_coe_resistances_op_add_value_0_5_chaos");

    // stat_entry/livingmod_coe_resistances_op_add_value_0_5_cold.json -> coe:livingmod_coe_resistances_op_add_value_0_5_cold
    public static final Identifier LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_COLD = id("livingmod_coe_resistances_op_add_value_0_5_cold");

    // stat_entry/livingmod_coe_resistances_op_add_value_0_5_fire.json -> coe:livingmod_coe_resistances_op_add_value_0_5_fire
    public static final Identifier LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_FIRE = id("livingmod_coe_resistances_op_add_value_0_5_fire");

    // stat_entry/livingmod_coe_resistances_op_add_value_0_5_lightning.json -> coe:livingmod_coe_resistances_op_add_value_0_5_lightning
    public static final Identifier LIVINGMOD_COE_RESISTANCES_OP_ADD_VALUE_0_5_LIGHTNING = id("livingmod_coe_resistances_op_add_value_0_5_lightning");

    // stat_entry/livingmod_coe_taken_crit_damage_op_add_value_-0_8.json -> coe:livingmod_coe_taken_crit_damage_op_add_value_-0_8
    public static final Identifier LIVINGMOD_COE_TAKEN_CRIT_DAMAGE_OP_ADD_VALUE__0_8 = id("livingmod_coe_taken_crit_damage_op_add_value_-0_8");

    // stat_entry/livingmod_convert_phys_to_chaos_100.json -> coe:livingmod_convert_phys_to_chaos_100
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_CHAOS_100 = id("livingmod_convert_phys_to_chaos_100");

    // stat_entry/livingmod_convert_phys_to_chaos_60.json -> coe:livingmod_convert_phys_to_chaos_60
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_CHAOS_60 = id("livingmod_convert_phys_to_chaos_60");

    // stat_entry/livingmod_convert_phys_to_chaos_70.json -> coe:livingmod_convert_phys_to_chaos_70
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_CHAOS_70 = id("livingmod_convert_phys_to_chaos_70");

    // stat_entry/livingmod_convert_phys_to_cold_100.json -> coe:livingmod_convert_phys_to_cold_100
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_COLD_100 = id("livingmod_convert_phys_to_cold_100");

    // stat_entry/livingmod_convert_phys_to_cold_60.json -> coe:livingmod_convert_phys_to_cold_60
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_COLD_60 = id("livingmod_convert_phys_to_cold_60");

    // stat_entry/livingmod_convert_phys_to_fire_60.json -> coe:livingmod_convert_phys_to_fire_60
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_FIRE_60 = id("livingmod_convert_phys_to_fire_60");

    // stat_entry/livingmod_convert_phys_to_fire_70.json -> coe:livingmod_convert_phys_to_fire_70
    public static final Identifier LIVINGMOD_CONVERT_PHYS_TO_FIRE_70 = id("livingmod_convert_phys_to_fire_70");

    // stat_entry/livingmod_minecraft_armor_op_add_multiplied_base_1.json -> coe:livingmod_minecraft_armor_op_add_multiplied_base_1
    public static final Identifier LIVINGMOD_MINECRAFT_ARMOR_OP_ADD_MULTIPLIED_BASE_1 = id("livingmod_minecraft_armor_op_add_multiplied_base_1");

    // stat_entry/livingmod_minecraft_attack_speed_op_add_value_0_3.json -> coe:livingmod_minecraft_attack_speed_op_add_value_0_3
    public static final Identifier LIVINGMOD_MINECRAFT_ATTACK_SPEED_OP_ADD_VALUE_0_3 = id("livingmod_minecraft_attack_speed_op_add_value_0_3");

    // stat_entry/livingmod_minecraft_max_health_op_add_multiplied_base_0_5.json -> coe:livingmod_minecraft_max_health_op_add_multiplied_base_0_5
    public static final Identifier LIVINGMOD_MINECRAFT_MAX_HEALTH_OP_ADD_MULTIPLIED_BASE_0_5 = id("livingmod_minecraft_max_health_op_add_multiplied_base_0_5");

    // stat_entry/livingmod_minecraft_movement_speed_op_add_value_0_3.json -> coe:livingmod_minecraft_movement_speed_op_add_value_0_3
    public static final Identifier LIVINGMOD_MINECRAFT_MOVEMENT_SPEED_OP_ADD_VALUE_0_3 = id("livingmod_minecraft_movement_speed_op_add_value_0_3");

    // stat_entry/livingmod_resist_chaos_10.json -> coe:livingmod_resist_chaos_10
    public static final Identifier LIVINGMOD_RESIST_CHAOS_10 = id("livingmod_resist_chaos_10");

    // stat_entry/livingmod_resist_chaos_20.json -> coe:livingmod_resist_chaos_20
    public static final Identifier LIVINGMOD_RESIST_CHAOS_20 = id("livingmod_resist_chaos_20");

    // stat_entry/livingmod_resist_cold_10.json -> coe:livingmod_resist_cold_10
    public static final Identifier LIVINGMOD_RESIST_COLD_10 = id("livingmod_resist_cold_10");

    // stat_entry/livingmod_resist_cold_20.json -> coe:livingmod_resist_cold_20
    public static final Identifier LIVINGMOD_RESIST_COLD_20 = id("livingmod_resist_cold_20");

    // stat_entry/livingmod_resist_elemental_10.json -> coe:livingmod_resist_elemental_10
    public static final Identifier LIVINGMOD_RESIST_ELEMENTAL_10 = id("livingmod_resist_elemental_10");

    // stat_entry/livingmod_resist_fire_20.json -> coe:livingmod_resist_fire_20
    public static final Identifier LIVINGMOD_RESIST_FIRE_20 = id("livingmod_resist_fire_20");

    // stat_entry/mark_use_speed.json -> coe:mark_use_speed
    public static final Identifier MARK_USE_SPEED = id("mark_use_speed");

    // stat_entry/meta_energy.json -> coe:meta_energy
    public static final Identifier META_ENERGY = id("meta_energy");

    // stat_entry/minion_attack_speed.json -> coe:minion_attack_speed
    public static final Identifier MINION_ATTACK_SPEED = id("minion_attack_speed");

    // stat_entry/minion_convert_extra_generic_to_chaos.json -> coe:minion_convert_extra_generic_to_chaos
    public static final Identifier MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS = id("minion_convert_extra_generic_to_chaos");

    // stat_entry/minion_damage.json -> coe:minion_damage
    public static final Identifier MINION_DAMAGE = id("minion_damage");

    // stat_entry/minion_movement_speed.json -> coe:minion_movement_speed
    public static final Identifier MINION_MOVEMENT_SPEED = id("minion_movement_speed");

    // stat_entry/minion_recoup_health.json -> coe:minion_recoup_health
    public static final Identifier MINION_RECOUP_HEALTH = id("minion_recoup_health");

    // stat_entry/more_amount_leech.json -> coe:more_amount_leech
    public static final Identifier MORE_AMOUNT_LEECH = id("more_amount_leech");

    // stat_entry/more_bleeding_magnitude_inflicted.json -> coe:more_bleeding_magnitude_inflicted
    public static final Identifier MORE_BLEEDING_MAGNITUDE_INFLICTED = id("more_bleeding_magnitude_inflicted");

    // stat_entry/onslaught_attack_speed.json -> coe:onslaught_attack_speed
    public static final Identifier ONSLAUGHT_ATTACK_SPEED = id("onslaught_attack_speed");

    // stat_entry/onslaught_cast_speed.json -> coe:onslaught_cast_speed
    public static final Identifier ONSLAUGHT_CAST_SPEED = id("onslaught_cast_speed");

    // stat_entry/onslaught_movement_speed.json -> coe:onslaught_movement_speed
    public static final Identifier ONSLAUGHT_MOVEMENT_SPEED = id("onslaught_movement_speed");

    // stat_entry/penetrate_chaos.json -> coe:penetrate_chaos
    public static final Identifier PENETRATE_CHAOS = id("penetrate_chaos");

    // stat_entry/penetrate_cold.json -> coe:penetrate_cold
    public static final Identifier PENETRATE_COLD = id("penetrate_cold");

    // stat_entry/penetrate_fire.json -> coe:penetrate_fire
    public static final Identifier PENETRATE_FIRE = id("penetrate_fire");

    // stat_entry/penetrate_lightning.json -> coe:penetrate_lightning
    public static final Identifier PENETRATE_LIGHTNING = id("penetrate_lightning");

    // stat_entry/physical_taken_as_chaos.json -> coe:physical_taken_as_chaos
    public static final Identifier PHYSICAL_TAKEN_AS_CHAOS = id("physical_taken_as_chaos");

    // stat_entry/physical_taken_as_cold.json -> coe:physical_taken_as_cold
    public static final Identifier PHYSICAL_TAKEN_AS_COLD = id("physical_taken_as_cold");

    // stat_entry/physical_taken_as_fire.json -> coe:physical_taken_as_fire
    public static final Identifier PHYSICAL_TAKEN_AS_FIRE = id("physical_taken_as_fire");

    // stat_entry/physical_taken_as_lightning.json -> coe:physical_taken_as_lightning
    public static final Identifier PHYSICAL_TAKEN_AS_LIGHTNING = id("physical_taken_as_lightning");

    // stat_entry/physical_taken_as_random_element.json -> coe:physical_taken_as_random_element
    public static final Identifier PHYSICAL_TAKEN_AS_RANDOM_ELEMENT = id("physical_taken_as_random_element");

    // stat_entry/pinned_enemies_no_crit.json -> coe:pinned_enemies_no_crit
    public static final Identifier PINNED_ENEMIES_NO_CRIT = id("pinned_enemies_no_crit");

    // stat_entry/rage_on_hit.json -> coe:rage_on_hit
    public static final Identifier RAGE_ON_HIT = id("rage_on_hit");

    // stat_entry/rage_on_hit_taken.json -> coe:rage_on_hit_taken
    public static final Identifier RAGE_ON_HIT_TAKEN = id("rage_on_hit_taken");

    // stat_entry/rage_on_hit_talent.json -> coe:rage_on_hit_talent
    public static final Identifier RAGE_ON_HIT_TALENT = id("rage_on_hit_talent");

    // stat_entry/rage_on_ignite.json -> coe:rage_on_ignite
    public static final Identifier RAGE_ON_IGNITE = id("rage_on_ignite");

    // stat_entry/rarity_magic_attack_speed.json -> coe:rarity_magic_attack_speed
    public static final Identifier RARITY_MAGIC_ATTACK_SPEED = id("rarity_magic_attack_speed");

    // stat_entry/rarity_magic_cast_speed.json -> coe:rarity_magic_cast_speed
    public static final Identifier RARITY_MAGIC_CAST_SPEED = id("rarity_magic_cast_speed");

    // stat_entry/rarity_magic_max_health.json -> coe:rarity_magic_max_health
    public static final Identifier RARITY_MAGIC_MAX_HEALTH = id("rarity_magic_max_health");

    // stat_entry/rarity_magic_movement_speed.json -> coe:rarity_magic_movement_speed
    public static final Identifier RARITY_MAGIC_MOVEMENT_SPEED = id("rarity_magic_movement_speed");

    // stat_entry/rarity_magic_scale.json -> coe:rarity_magic_scale
    public static final Identifier RARITY_MAGIC_SCALE = id("rarity_magic_scale");

    // stat_entry/rarity_rare_attack_speed.json -> coe:rarity_rare_attack_speed
    public static final Identifier RARITY_RARE_ATTACK_SPEED = id("rarity_rare_attack_speed");

    // stat_entry/rarity_rare_cast_speed.json -> coe:rarity_rare_cast_speed
    public static final Identifier RARITY_RARE_CAST_SPEED = id("rarity_rare_cast_speed");

    // stat_entry/rarity_rare_max_health.json -> coe:rarity_rare_max_health
    public static final Identifier RARITY_RARE_MAX_HEALTH = id("rarity_rare_max_health");

    // stat_entry/rarity_rare_movement_speed.json -> coe:rarity_rare_movement_speed
    public static final Identifier RARITY_RARE_MOVEMENT_SPEED = id("rarity_rare_movement_speed");

    // stat_entry/rarity_rare_scale.json -> coe:rarity_rare_scale
    public static final Identifier RARITY_RARE_SCALE = id("rarity_rare_scale");

    // stat_entry/rarity_unique_attack_speed.json -> coe:rarity_unique_attack_speed
    public static final Identifier RARITY_UNIQUE_ATTACK_SPEED = id("rarity_unique_attack_speed");

    // stat_entry/rarity_unique_cast_speed.json -> coe:rarity_unique_cast_speed
    public static final Identifier RARITY_UNIQUE_CAST_SPEED = id("rarity_unique_cast_speed");

    // stat_entry/rarity_unique_max_health.json -> coe:rarity_unique_max_health
    public static final Identifier RARITY_UNIQUE_MAX_HEALTH = id("rarity_unique_max_health");

    // stat_entry/rarity_unique_movement_speed.json -> coe:rarity_unique_movement_speed
    public static final Identifier RARITY_UNIQUE_MOVEMENT_SPEED = id("rarity_unique_movement_speed");

    // stat_entry/rarity_unique_scale.json -> coe:rarity_unique_scale
    public static final Identifier RARITY_UNIQUE_SCALE = id("rarity_unique_scale");

    // stat_entry/recover_pct_health.json -> coe:recover_pct_health
    public static final Identifier RECOVER_PCT_HEALTH = id("recover_pct_health");

    // stat_entry/recover_pct_health_on_kill.json -> coe:recover_pct_health_on_kill
    public static final Identifier RECOVER_PCT_HEALTH_ON_KILL = id("recover_pct_health_on_kill");

    // stat_entry/recover_pct_mana_on_kill.json -> coe:recover_pct_mana_on_kill
    public static final Identifier RECOVER_PCT_MANA_ON_KILL = id("recover_pct_mana_on_kill");

    // stat_entry/seal_gain_speed.json -> coe:seal_gain_speed
    public static final Identifier SEAL_GAIN_SPEED = id("seal_gain_speed");

    // stat_entry/speed_per_stack_attack_speed.json -> coe:speed_per_stack_attack_speed
    public static final Identifier SPEED_PER_STACK_ATTACK_SPEED = id("speed_per_stack_attack_speed");

    // stat_entry/speed_per_stack_cast_speed.json -> coe:speed_per_stack_cast_speed
    public static final Identifier SPEED_PER_STACK_CAST_SPEED = id("speed_per_stack_cast_speed");

    // stat_entry/speed_per_stack_movement_speed.json -> coe:speed_per_stack_movement_speed
    public static final Identifier SPEED_PER_STACK_MOVEMENT_SPEED = id("speed_per_stack_movement_speed");

    // stat_entry/status_extra_stacks_bleed.json -> coe:status_extra_stacks_bleed
    public static final Identifier STATUS_EXTRA_STACKS_BLEED = id("status_extra_stacks_bleed");

    // stat_entry/status_extra_stacks_chill.json -> coe:status_extra_stacks_chill
    public static final Identifier STATUS_EXTRA_STACKS_CHILL = id("status_extra_stacks_chill");

    // stat_entry/status_extra_stacks_electrocute.json -> coe:status_extra_stacks_electrocute
    public static final Identifier STATUS_EXTRA_STACKS_ELECTROCUTE = id("status_extra_stacks_electrocute");

    // stat_entry/status_extra_stacks_freeze.json -> coe:status_extra_stacks_freeze
    public static final Identifier STATUS_EXTRA_STACKS_FREEZE = id("status_extra_stacks_freeze");

    // stat_entry/status_extra_stacks_ignite.json -> coe:status_extra_stacks_ignite
    public static final Identifier STATUS_EXTRA_STACKS_IGNITE = id("status_extra_stacks_ignite");

    // stat_entry/status_extra_stacks_pin.json -> coe:status_extra_stacks_pin
    public static final Identifier STATUS_EXTRA_STACKS_PIN = id("status_extra_stacks_pin");

    // stat_entry/status_extra_stacks_poison.json -> coe:status_extra_stacks_poison
    public static final Identifier STATUS_EXTRA_STACKS_POISON = id("status_extra_stacks_poison");

    // stat_entry/status_extra_stacks_shock.json -> coe:status_extra_stacks_shock
    public static final Identifier STATUS_EXTRA_STACKS_SHOCK = id("status_extra_stacks_shock");

    // stat_entry/status_max_stacks_bleed.json -> coe:status_max_stacks_bleed
    public static final Identifier STATUS_MAX_STACKS_BLEED = id("status_max_stacks_bleed");

    // stat_entry/status_max_stacks_chill.json -> coe:status_max_stacks_chill
    public static final Identifier STATUS_MAX_STACKS_CHILL = id("status_max_stacks_chill");

    // stat_entry/status_max_stacks_electrocute.json -> coe:status_max_stacks_electrocute
    public static final Identifier STATUS_MAX_STACKS_ELECTROCUTE = id("status_max_stacks_electrocute");

    // stat_entry/status_max_stacks_freeze.json -> coe:status_max_stacks_freeze
    public static final Identifier STATUS_MAX_STACKS_FREEZE = id("status_max_stacks_freeze");

    // stat_entry/status_max_stacks_ignite.json -> coe:status_max_stacks_ignite
    public static final Identifier STATUS_MAX_STACKS_IGNITE = id("status_max_stacks_ignite");

    // stat_entry/status_max_stacks_pin.json -> coe:status_max_stacks_pin
    public static final Identifier STATUS_MAX_STACKS_PIN = id("status_max_stacks_pin");

    // stat_entry/status_max_stacks_poison.json -> coe:status_max_stacks_poison
    public static final Identifier STATUS_MAX_STACKS_POISON = id("status_max_stacks_poison");

    // stat_entry/status_max_stacks_shock.json -> coe:status_max_stacks_shock
    public static final Identifier STATUS_MAX_STACKS_SHOCK = id("status_max_stacks_shock");

    // stat_entry/taken_crit.json -> coe:taken_crit
    public static final Identifier TAKEN_CRIT = id("taken_crit");

    // stat_entry/talent_coe_dexterity_op_add_value_5.json -> coe:talent_coe_dexterity_op_add_value_5
    public static final Identifier TALENT_COE_DEXTERITY_OP_ADD_VALUE_5 = id("talent_coe_dexterity_op_add_value_5");

    // stat_entry/talent_coe_intelligence_op_add_value_5.json -> coe:talent_coe_intelligence_op_add_value_5
    public static final Identifier TALENT_COE_INTELLIGENCE_OP_ADD_VALUE_5 = id("talent_coe_intelligence_op_add_value_5");

    // stat_entry/talent_coe_strength_op_add_value_5.json -> coe:talent_coe_strength_op_add_value_5
    public static final Identifier TALENT_COE_STRENGTH_OP_ADD_VALUE_5 = id("talent_coe_strength_op_add_value_5");

    // stat_entry/talent_minecraft_armor_op_add_value_15.json -> coe:talent_minecraft_armor_op_add_value_15
    public static final Identifier TALENT_MINECRAFT_ARMOR_OP_ADD_VALUE_15 = id("talent_minecraft_armor_op_add_value_15");

    // stat_entry/talent_minecraft_max_health_op_add_value_20.json -> coe:talent_minecraft_max_health_op_add_value_20
    public static final Identifier TALENT_MINECRAFT_MAX_HEALTH_OP_ADD_VALUE_20 = id("talent_minecraft_max_health_op_add_value_20");

    // stat_entry/to_all_elemental_damage_resistances_2.json -> coe:to_all_elemental_damage_resistances_2
    public static final Identifier TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_2 = id("to_all_elemental_damage_resistances_2");

    // stat_entry/totem_recoup_health.json -> coe:totem_recoup_health
    public static final Identifier TOTEM_RECOUP_HEALTH = id("totem_recoup_health");

    // stat_entry/trap_damage.json -> coe:trap_damage
    public static final Identifier TRAP_DAMAGE = id("trap_damage");

    // stat_entry/unholy_might_chaos.json -> coe:unholy_might_chaos
    public static final Identifier UNHOLY_MIGHT_CHAOS = id("unholy_might_chaos");

    // stat_entry/unholy_might_cold.json -> coe:unholy_might_cold
    public static final Identifier UNHOLY_MIGHT_COLD = id("unholy_might_cold");

    // stat_entry/unholy_might_fire.json -> coe:unholy_might_fire
    public static final Identifier UNHOLY_MIGHT_FIRE = id("unholy_might_fire");

    // stat_entry/unholy_might_lightning.json -> coe:unholy_might_lightning
    public static final Identifier UNHOLY_MIGHT_LIGHTNING = id("unholy_might_lightning");

    // stat_entry/unholy_might_physical.json -> coe:unholy_might_physical
    public static final Identifier UNHOLY_MIGHT_PHYSICAL = id("unholy_might_physical");

    // stat_entry/warcry_speed.json -> coe:warcry_speed
    public static final Identifier WARCRY_SPEED = id("warcry_speed");

    // === 手书常量（gen_stat_ids.py 不产出，须保留）===
    public static final Identifier INCISION_BLEED_CHANCE_ON_HITTER = id("incision_bleed_chance_on_hitter");

    public static final Identifier GAIN_ELEMENTAL_INFUSION = id("gain_elemental_infusion");

    // [Accuracy] Rating is Doubled
    public static final Identifier ACCURACY_IS_DOUBLED = id("accuracy_is_doubled");

    // Remove a [Curse] after Channelling for 2 seconds
    public static final Identifier ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS = id("add_a_curse_after_channelling_for_seconds");

    // Remove a [Curse] when you use a Mana [Flask]
    public static final Identifier ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK = id("add_a_curse_when_you_use_a_mana_flask");

    // Remove [Ignite] when you [Warcry]
    public static final Identifier ADD_IGNITE_WHEN_YOU_WARCRY = id("add_ignite_when_you_warcry");

    // Adds 1 to 7 [Lightning] damage to [Attack|Attacks]
    public static final Identifier ADD_TO_LIGHTNING_DAMAGE_TO_ATTACKS = id("add_to_lightning_damage_to_attacks");

    // [Allies|Allies] in your [Presence|Presence] deal 8% increased Damage
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_DEAL_INC_DAMAGE = id("allies_in_your_presence_deal_inc_damage");

    // [Allies|Allies] in your [Presence|Presence] have 6% increased [Attack|Attack] Speed
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_HAVE_INC_ATTACK_SPEED = id("allies_in_your_presence_have_inc_attack_speed");

    // [Allies|Allies] in your [Presence|Presence] have 6% increased Cast Speed
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_HAVE_INC_CAST_SPEED = id("allies_in_your_presence_have_inc_cast_speed");

    // [Allies|Allies] in your [Presence|Presence] have 25% increased [Critical|Critical Hit] Chance
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_HAVE_INC_CRITICAL_HIT_CHANCE = id("allies_in_your_presence_have_inc_critical_hit_chance");

    // [Allies] in your [Presence] have 30% increased [Glory] generation
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_HAVE_INC_GLORY_GENERATION = id("allies_in_your_presence_have_inc_glory_generation");

    // [Allies|Allies] in your [Presence|Presence] have +100 to [Accuracy|Accuracy] Rating
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_HAVE_TO_ACCURACY = id("allies_in_your_presence_have_to_accuracy");

    // [Allies] in your [Presence] Regenerate 1% of your Maximum Life per second
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_REGENERATE_OF_YOUR_MAX_HEALTH_PER_SECOND = id("allies_in_your_presence_regenerate_of_your_max_health_per_second");

    // [Allies] in your [Presence] Regenerate 5 [Rage] per second if you have gained [Rage] [Recently]
    public static final Identifier ALLIES_IN_YOUR_PRESENCE_REGENERATE_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY = id("allies_in_your_presence_regenerate_rage_per_second_if_you_have_gain_rage_recently");

    // All bonuses from [Equipped] Amulet apply to your [Minion|Minions] instead of you
    public static final Identifier ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU = id("all_bonuses_from_equipped_amulet_apply_to_your_minions_instead_of_you");

    // All [DamageTypes|Damage] is taken from Mana before Life
    public static final Identifier ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH = id("all_damage_types_is_taken_from_mana_before_health");

    // [AncestralBoost|Ancestrally Boosted] [Attack|Attacks] deal 16% increased Damage
    public static final Identifier ANCESTRALLY_BOOSTED_ATTACKS_DEAL_INC_DAMAGE = id("ancestrally_boosted_attacks_deal_inc_damage");

    // Apply [Debilitate] to Enemies 30 Metres in front of you while your [Shield] is raised
    public static final Identifier APPLY_DEBILITATE_TO_ENEMIES_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED = id("apply_debilitate_to_enemies_metres_in_front_of_you_while_your_shield_is_raised");

    // [ArcaneSurge|Arcane Surge] grants more Life Regeneration Rate instead of Mana Regeneration Rate
    public static final Identifier ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE = id("arcane_surge_grants_more_health_regeneration_rate_instead_of_mana_regeneration_rate");

    // [Archon] Buffs also grant 50% increased [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_DAMAGE_BONUS = id("archon_buffs_also_grant_inc_critical_damage_bonus");

    // [Archon] Buffs also grant 30% increased [Critical|Critical Hit Chance]
    public static final Identifier ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_HIT_CHANCE = id("archon_buffs_also_grant_inc_critical_hit_chance");

    // [Archon] Buffs also grant 10% increased Movement Speed
    public static final Identifier ARCHON_BUFFS_ALSO_GRANT_INC_MOVEMENT_SPEED = id("archon_buffs_also_grant_inc_movement_speed");

    // [Archon] Buffs also grant +20% to all [ElementalDamage|Elemental] [Resistances]
    public static final Identifier ARCHON_BUFFS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES = id("archon_buffs_also_grant_to_all_elemental_damage_resistances");

    // [Archon] recovery period expires 10% faster
    public static final Identifier ARCHON_RECOVERY_PERIOD_EXPIRES_FASTER = id("archon_recovery_period_expires_faster");

    // [Archon] recovery period expires 30% slower
    public static final Identifier ARCHON_RECOVERY_PERIOD_EXPIRES_SLOWER = id("archon_recovery_period_expires_slower");

    // Area Skills have 20% chance to [Knockback|Knock Enemies Back] on [HitDamage|Hit]
    public static final Identifier AREA_SKILLS_HAVE_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE = id("area_skills_have_chance_to_knock_enemies_back_on_hit_damage");

    // [ArmourBreak|Break] [Armour] on [Critical|Critical Hit] with [Spell|Spells] equal to 5% of [Physical|Physical] Damage dealt
    public static final Identifier ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT = id("armor_break_armor_on_critical_hit_with_spells_equal_to_of_physical_damage_dealt");

    // [ArmourBreak|Break] 20% increased [Armour|Armour]
    public static final Identifier ARMOR_BREAK_INC_ARMOR = id("armor_break_inc_armor");

    // [ArmourBreak|Break] 50% of [Armour] on [HeavyStun|Heavy Stunning] an Enemy
    public static final Identifier ARMOR_BREAK_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY = id("armor_break_of_armor_on_heavy_stunning_an_enemy");

    // [ArmourBreak|Break] 50% of [Armour] on [Pinned|Pinning] an Enemy
    public static final Identifier ARMOR_BREAK_OF_ARMOR_ON_PINNING_AN_ENEMY = id("armor_break_of_armor_on_pinning_an_enemy");

    // Arrows gain [Critical|Critical Hit] Chance as they travel farther, up to 40% increased [Critical|Critical Hit] Chance after 7 metres
    public static final Identifier ARROWS_GAIN_CRITICAL_HIT_CHANCE_AS_THEY_TRAVEL_FARTHER_UP_TO_INC_CRITICAL_HIT_CHANCE_AFTER_METRES = id("arrows_gain_critical_hit_chance_as_they_travel_farther_up_to_inc_critical_hit_chance_after_metres");

    // Arrows [Pierce] an additional Target
    public static final Identifier ARROWS_PIERCE_AN_ADDITIONAL_TARGET = id("arrows_pierce_an_additional_target");

    // Attacks gain increased [Accuracy|Accuracy Rating] equal to their [Critical|Critical Hit Chance]
    public static final Identifier ATTACKS_GAIN_INC_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE = id("attacks_gain_inc_accuracy_equal_to_their_critical_hit_chance");

    // [Attack|Attacks] [Gain] 5% of Damage as extra [Chaos|Chaos] Damage
    public static final Identifier ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("attacks_gain_of_damage_as_extra_chaos_damage");

    // [Attack|Attacks] have 10% chance to [Maim|Maim] on [HitDamage|Hit]
    public static final Identifier ATTACKS_HAVE_CHANCE_TO_MAIM_ON_HIT_DAMAGE = id("attacks_have_chance_to_maim_on_hit_damage");

    // [Attack|Attacks] have +1% to [Critical|Critical Hit Chance]
    public static final Identifier ATTACKS_HAVE_TO_CRITICAL_HIT_CHANCE = id("attacks_have_to_critical_hit_chance");

    // [Attack|Attacks] used by [Ballista|Ballistas] have 10% increased [Attack] Speed
    public static final Identifier ATTACKS_USED_BY_BALLISTAS_HAVE_INC_ATTACK_SPEED = id("attacks_used_by_ballistas_have_inc_attack_speed");

    // [Attack|Attacks] used by Totems have 2% increased [Attack] Speed
    public static final Identifier ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED = id("attacks_used_by_totems_have_inc_attack_speed");

    // [Attack|Attacks] used by [Totem|Totems] have 4% increased [Attack] Speed per Summoned [Totem|Totem]
    public static final Identifier ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED_PER_SUMMONED_TOTEM = id("attacks_used_by_totems_have_inc_attack_speed_per_summoned_totem");

    // [Attack|Attacks] with [One-Handed] [MartialWeapon|Weapons] have 15% increased Chance to inflict [Ailments|Ailments]
    public static final Identifier ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_INC_CHANCE_TO_INFLICT_AILMENTS = id("attacks_with_one_handed_martial_weapon_have_inc_chance_to_inflict_ailments");

    // [Attack] Damage Penetrates 15% of Enemy [ElementalDamage|Elemental] [Resistances]
    public static final Identifier ATTACK_DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES = id("attack_damage_penetrates_of_enemy_elemental_damage_resistances");

    // [Attack] [HitDamage|Hits] [Aggravate] any [Bleeding] on targets which is older than 4 seconds
    public static final Identifier ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_SECONDS = id("attack_hit_damage_aggravate_any_bleeding_on_targets_which_is_older_than_seconds");

    // [Attack|Attack] Skills deal 10% increased Damage while holding a [Shield|Shield]
    public static final Identifier ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD = id("attack_skills_deal_inc_damage_while_holding_a_shield");

    // [Attack] Skills have +1 to maximum number of Summoned [Ballista] [Totem|Totems]
    public static final Identifier ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS = id("attack_skills_have_to_max_number_of_summoned_ballista_totems");

    // [Aura] Skills have 5% increased [BuffMagnitude|Magnitudes]
    public static final Identifier AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE = id("aura_skills_have_inc_buff_magnitude");

    // [Banner|Banners] also grant +25% to all [ElementalDamage|Elemental] [Resistances] affected targets
    public static final Identifier BANNERS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_AFFECTED_TARGETS = id("banners_also_grant_to_all_elemental_damage_resistances_affected_targets");

    // [Banner] [Buff|Buffs] linger on you for 2 seconds after you leave the Area
    public static final Identifier BANNER_BUFFS_LINGER_ON_YOU_FOR_SECONDS_AFTER_YOU_LEAVE_THE_AREA = id("banner_buffs_linger_on_you_for_seconds_after_you_leave_the_area");

    // [Banner] Skills have 30% increased Area of Effect
    public static final Identifier BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("banner_skills_have_inc_area_of_effect");

    // Banner Skills have 12% increased Aura [BuffMagnitude|Magnitudes]
    public static final Identifier BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE = id("banner_skills_have_inc_aura_buff_magnitude");

    // [Banner] Skills have 30% increased Duration
    public static final Identifier BANNER_SKILLS_HAVE_INC_DURATION = id("banner_skills_have_inc_duration");

    // Base [Bleeding] Duration is 1 second
    public static final Identifier BASE_BLEEDING_DURATION_IS_SECOND = id("base_bleeding_duration_is_second");

    // Bleeding you inflict deals Damage 10% faster
    public static final Identifier BLEEDING_YOU_INFLICT_DEALS_DAMAGE_FASTER = id("bleeding_you_inflict_deals_damage_faster");

    // [Bleeding] you inflict is [Aggravate|Aggravated]
    public static final Identifier BLEEDING_YOU_INFLICT_IS_AGGRAVATED = id("bleeding_you_inflict_is_aggravated");

    // [Bleeding] you inflict on [Pinned] Enemies is [Aggravate|Aggravated]
    public static final Identifier BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED = id("bleeding_you_inflict_on_pinned_enemies_is_aggravated");

    // [Blind|Blind] Enemies when they [Stun|Stun] you
    public static final Identifier BLIND_ENEMIES_WHEN_THEY_STUN_YOU = id("blind_enemies_when_they_stun_you");

    // Bolts fired by [Crossbow|Crossbow] [Attack|Attacks] have 30% chance to not expend [Ammunition] if you've Reloaded [Recently]
    public static final Identifier BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY = id("bolts_fired_by_crossbow_attacks_have_chance_to_not_expend_ammunition_if_you_ve_reloaded_recently");

    // [Bow|Bow] [Attack|Attacks] have [CullingStrike|Culling Strike]
    public static final Identifier BOW_ATTACKS_HAVE_CULLING_STRIKE = id("bow_attacks_have_culling_strike");

    // [Buff|Buffs] on you expire 10% slower
    public static final Identifier BUFFS_ON_YOU_EXPIRE_SLOWER = id("buffs_on_you_expire_slower");

    // [Burning] Enemies you kill have a 5% chance to Explode, dealing a tenth of their maximum Life as [Fire] Damage
    public static final Identifier BURNING_ENEMIES_YOU_KILL_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE = id("burning_enemies_you_kill_have_a_chance_to_explode_dealing_a_tenth_of_their_max_health_as_fire_damage");

    // Cannot be [Blind|Blinded]
    public static final Identifier CANNOT_BE_BLINDED = id("cannot_be_blinded");

    // Cannot be [Blind|Blinded] while on Full Life
    public static final Identifier CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH = id("cannot_be_blinded_while_on_full_health");

    // Cannot be [Critical|Critically Hit] while [Parry|Parrying]
    public static final Identifier CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING = id("cannot_be_critically_hit_while_parrying");

    // Cannot be [LightStun|Light Stunned]
    public static final Identifier CANNOT_BE_LIGHT_STUNNED = id("cannot_be_light_stunned");

    // Cannot be [LightStun|Light Stunned] if you haven't been [HitDamage|Hit] [Recently]
    public static final Identifier CANNOT_BE_LIGHT_STUNNED_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY = id("cannot_be_light_stunned_if_you_haven_t_been_hit_damage_recently");

    // Cannot Dodge Roll or Sprint
    public static final Identifier CANNOT_DODGE_ROLL_OR_SPRINT = id("cannot_dodge_roll_or_sprint");

    // Cannot Recover Life other than from [LifeLeech|Leech]
    public static final Identifier CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH = id("cannot_recover_health_other_than_from_health_leech");

    // Cannot use [Charm|Charms]
    public static final Identifier CANNOT_USE_CHARMS = id("cannot_use_charms");

    // Can [Attack] as though using a [Quarterstaff] while both of your hand slots are empty
    public static final Identifier CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY = id("can_attack_as_though_using_a_quarterstaff_while_both_of_your_hand_slots_are_empty");

    // 25% chance for [Attack|Attacks] to [Maim|Maim] on [HitDamage|Hit] against [Poison|Poisoned] Enemies
    public static final Identifier CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES = id("chance_for_attacks_to_maim_on_hit_damage_against_poisoned_enemies");

    // 20% chance for [Attack] [HitDamage|Hits] to apply [Incision]
    public static final Identifier CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION = id("chance_for_attack_hit_damage_to_apply_incision");

    // 10% chance for [Attack] [HitDamage|Hits] to apply ten [Incision]
    public static final Identifier CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION = id("chance_for_attack_hit_damage_to_apply_ten_incision");

    // 20% chance for [Bleeding] to be [Aggravate|Aggravated] when Inflicted against Enemies on [JaggedGround|Jagged Ground]
    public static final Identifier CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND = id("chance_for_bleeding_to_be_aggravated_when_inflicted_against_enemies_on_jagged_ground");

    // 20% chance for [Charm|Charms] you use to not consume Charges
    public static final Identifier CHANCE_FOR_CHARMS_YOU_USE_TO_NOT_CONSUME_CHARGES = id("chance_for_charms_you_use_to_not_consume_charges");

    // 20% chance for Damage of Enemies [HitDamage|Hitting] you to be [Unlucky]
    public static final Identifier CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY = id("chance_for_damage_of_enemies_hit_damage_you_to_be_unlucky");

    // 20% chance for Damage with [HitDamage|Hits] to be [Lucky]
    public static final Identifier CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY = id("chance_for_damage_with_hit_damage_to_be_lucky");

    // 10% chance for [Flask|Flasks] you use to not consume Charges
    public static final Identifier CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES = id("chance_for_flasks_you_use_to_not_consume_charges");

    // 30% chance for [Lightning] Damage with [HitDamage|Hits] to be [Lucky]
    public static final Identifier CHANCE_FOR_LIGHTNING_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY = id("chance_for_lightning_damage_with_hit_damage_to_be_lucky");

    // 20% chance for [Lightning] Skills to [Chain] an additional time
    public static final Identifier CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME = id("chance_for_lightning_skills_to_chain_an_additional_time");

    // 8% chance for [Mace] [Slam] Skills you use yourself to cause an additional [Aftershock]
    public static final Identifier CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK = id("chance_for_mace_slam_skills_you_use_yourself_to_cause_an_additional_aftershock");

    // 25% chance for [Projectile|Projectiles] to [Pierce|Pierce] Enemies within 3m distance of you
    public static final Identifier CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU = id("chance_for_projectiles_to_pierce_enemies_within_xm_distance_of_you");

    // 15% chance for [Remnant|Remnants] you create to grant their effects twice
    public static final Identifier CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE = id("chance_for_remnants_you_create_to_grant_their_effects_twice");

    // 10% chance for [Shapeshift] [Slam] Skills you use yourself to cause an additional [Aftershock]
    public static final Identifier CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK = id("chance_for_shapeshift_slam_skills_you_use_yourself_to_cause_an_additional_aftershock");

    // 5% chance for [Slam] Skills to cause an additional [Aftershock]
    public static final Identifier CHANCE_FOR_SLAM_SKILLS_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK = id("chance_for_slam_skills_to_cause_an_additional_aftershock");

    // 5% chance for [Slam] Skills you use yourself to cause an additional [Aftershock]
    public static final Identifier CHANCE_FOR_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK = id("chance_for_slam_skills_you_use_yourself_to_cause_an_additional_aftershock");

    // 12% chance for [Spell] Skills to fire 2 additional [Projectile|Projectiles]
    public static final Identifier CHANCE_FOR_SPELL_SKILLS_TO_FIRE_ADDITIONAL_PROJECTILES = id("chance_for_spell_skills_to_fire_additional_projectiles");

    // 25% chance for [Trigger] skills to refund half of [Energy] Spent
    public static final Identifier CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT = id("chance_for_trigger_skills_to_refund_half_of_energy_spent");

    // 25% chance on [Consume|Consuming] a [Shock] on an Enemy to reapply it
    public static final Identifier CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT = id("chance_on_consuming_a_shock_on_an_enemy_to_reapply_it");

    // 2% chance that if you would gain [Charges|Endurance Charges], you instead gain up to maximum [Charges|Endurance Charges]
    public static final Identifier CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES = id("chance_that_if_you_would_gain_endurance_charges_you_instead_gain_up_to_max_endurance_charges");

    // 2% chance that if you would gain [Charges|Frenzy Charges], you instead gain up to your maximum number of [Charges|Frenzy Charges]
    public static final Identifier CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES = id("chance_that_if_you_would_gain_frenzy_charges_you_instead_gain_up_to_your_max_number_of_frenzy_charges");

    // 2% chance that if you would gain [Charges|Power Charges], you instead gain up to your maximum number of [Charges|Power Charges]
    public static final Identifier CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES = id("chance_that_if_you_would_gain_power_charges_you_instead_gain_up_to_your_max_number_of_power_charges");

    // 4% chance that if you would gain [Rage] on [HitDamage|Hit], you instead gain up to your maximum Rage
    public static final Identifier CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE = id("chance_that_if_you_would_gain_rage_on_hit_damage_you_instead_gain_up_to_your_max_rage");

    // 25% chance that when [Volatility] on you explodes, you regain an equivalent amount of [Volatility]
    public static final Identifier CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY = id("chance_that_when_volatility_on_you_explodes_you_regain_an_equivalent_amount_of_volatility");

    // 20% chance to [Aggravate] [Bleeding] on targets you [Critical|Critically Hit] with [Attack|Attacks]
    public static final Identifier CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_CRITICALLY_HIT_WITH_ATTACKS = id("chance_to_aggravate_bleeding_on_targets_you_critically_hit_with_attacks");

    // 10% chance to [Aggravate] [Bleeding] on targets you [HitDamage|Hit] with [Attack|Attacks]
    public static final Identifier CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_ATTACKS = id("chance_to_aggravate_bleeding_on_targets_you_hit_damage_with_attacks");

    // 20% chance to [Aggravate] [Bleeding] on targets you [HitDamage|Hit] with [Empowered] [Attack|Attacks]
    public static final Identifier CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS = id("chance_to_aggravate_bleeding_on_targets_you_hit_damage_with_empowered_attacks");

    // 7% chance to Avoid Death from [HitDamage|Hits]
    public static final Identifier CHANCE_TO_AVOID_DEATH_FROM_HIT_DAMAGE = id("chance_to_avoid_death_from_hit_damage");

    // 5% chance to [Blind] Enemies on [HitDamage|Hit]
    public static final Identifier CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE = id("chance_to_blind_enemies_on_hit_damage");

    // 15% chance to [Blind] Enemies on [HitDamage|Hit] with [Attack|Attacks]
    public static final Identifier CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS = id("chance_to_blind_enemies_on_hit_damage_with_attacks");

    // 5% Chance to build an additional [Combo] on [HitDamage|Hit]
    public static final Identifier CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE = id("chance_to_build_an_additional_combo_on_hit_damage");

    // 5% chance to create an additional [Remnant]
    public static final Identifier CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT = id("chance_to_create_an_additional_remnant");

    // 100% chance to [Daze] Enemies whose [HitDamage|Hits] you [Block] with a raised [Shield]
    public static final Identifier CHANCE_TO_DAZE_ENEMIES_WHOSE_HIT_DAMAGE_YOU_BLOCK_WITH_A_RAISED_SHIELD = id("chance_to_daze_enemies_whose_hit_damage_you_block_with_a_raised_shield");

    // 5% chance to [Daze] on [HitDamage|Hit]
    public static final Identifier CHANCE_TO_DAZE_ON_HIT_DAMAGE = id("chance_to_daze_on_hit_damage");

    // 10% chance to Defend with 200% of [Armour]
    public static final Identifier CHANCE_TO_DEFEND_WITH_OF_ARMOR = id("chance_to_defend_with_of_armor");

    // Chance to [Deflect] is [Lucky]
    public static final Identifier CHANCE_TO_DEFLECT_IS_LUCKY = id("chance_to_deflect_is_lucky");

    // Chance to [Evasion|Evade] is [Unlucky]
    public static final Identifier CHANCE_TO_EVASION_IS_UNLUCKY = id("chance_to_evasion_is_unlucky");

    // 10% chance to Gain [ArcaneSurgeDuration|Arcane Surge] when you deal a [Critical|Critical Hit]
    public static final Identifier CHANCE_TO_GAIN_ARCANE_SURGE_WHEN_YOU_DEAL_A_CRITICAL_HIT = id("chance_to_gain_arcane_surge_when_you_deal_a_critical_hit");

    // 15% chance to gain [UndeadArchon|Archon of Undeath] when you use a [Command|Command] skill
    public static final Identifier CHANCE_TO_GAIN_ARCHON_OF_UNDEATH_WHEN_YOU_USE_A_COMMAND_SKILL = id("chance_to_gain_archon_of_undeath_when_you_use_a_command_skill");

    // 50% chance to gain [Onslaught|Onslaught] on Killing Blow with [Axe|Axes]
    public static final Identifier CHANCE_TO_GAIN_ONSLAUGHT_ON_KILLING_BLOW_WITH_AXES = id("chance_to_gain_onslaught_on_killing_blow_with_axes");

    // 3% chance to gain [Volatility] on Kill
    public static final Identifier CHANCE_TO_GAIN_VOLATILITY_ON_KILL = id("chance_to_gain_volatility_on_kill");

    // 50% chance to gain [Volatility] when you are [Stun|Stunned]
    public static final Identifier CHANCE_TO_GAIN_VOLATILITY_WHEN_YOU_ARE_STUNNED = id("chance_to_gain_volatility_when_you_are_stunned");

    // 15% chance to [Hinder] Enemies on Hit with [Spell|Spells]
    public static final Identifier CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS = id("chance_to_hinder_enemies_on_hit_with_spells");

    // 15% chance to [Impale] on [Spell] [HitDamage|Hit]
    public static final Identifier CHANCE_TO_IMPALE_ON_SPELL_HIT_DAMAGE = id("chance_to_impale_on_spell_hit_damage");

    // 10% chance to inflict Bleeding on [Critical|Critical Hit] with [Attack|Attacks]
    public static final Identifier CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS = id("chance_to_inflict_bleeding_on_critical_hit_with_attacks");

    // 5% chance to inflict [Bleeding|Bleeding] on [HitDamage|Hit]
    public static final Identifier CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE = id("chance_to_inflict_bleeding_on_hit_damage");

    // 25% chance to inflict [Daze] with [HitDamage|Hits] against Enemies further than 6m
    public static final Identifier CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM = id("chance_to_inflict_daze_with_hit_damage_against_enemies_further_than_xm");

    // 50% chance to [Knockback|Knock Back] [Bleeding] Enemies with [HitDamage|Hits]
    public static final Identifier CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE = id("chance_to_knock_back_bleeding_enemies_with_hit_damage");

    // 20% chance to [Knockback|Knock Enemies Back] with [HitDamage|Hits] at [CloseRange|Close Range]
    public static final Identifier CHANCE_TO_KNOCK_ENEMIES_BACK_WITH_HIT_DAMAGE_AT_CLOSE_RANGE = id("chance_to_knock_enemies_back_with_hit_damage_at_close_range");

    // 15% chance to not destroy [Corpse|Corpses] when Consuming [Corpse|Corpses]
    public static final Identifier CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES = id("chance_to_not_destroy_corpses_when_consuming_corpses");

    // 30% chance to [Pierce|Pierce] an Enemy
    public static final Identifier CHANCE_TO_PIERCE_AN_ENEMY = id("chance_to_pierce_an_enemy");

    // 8% chance to [Poison|Poison] on [HitDamage|Hit]
    public static final Identifier CHANCE_TO_POISON_ON_HIT_DAMAGE = id("chance_to_poison_on_hit_damage");

    // 30% chance to [Poison] on [HitDamage|Hit] against Enemies that are not Poisoned
    public static final Identifier CHANCE_TO_POISON_ON_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_NOT_POISONED = id("chance_to_poison_on_hit_damage_against_enemies_that_are_not_poisoned");

    // 2% chance to Recover all Life when you Kill an Enemy
    public static final Identifier CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY = id("chance_to_recover_all_health_when_you_kill_an_enemy");

    // 10% chance when a [Charm|Charm] is used to use another Charm without consuming Charges
    public static final Identifier CHANCE_WHEN_A_CHARM_IS_USED_TO_USE_ANOTHER_CHARM_WITHOUT_CONSUMING_CHARGES = id("chance_when_a_charm_is_used_to_use_another_charm_without_consuming_charges");

    // 10% chance when collecting an [ElementalInfusion|Elemental Infusion] to gain an additional [ElementalInfusion|Elemental Infusion] of the same type
    public static final Identifier CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE = id("chance_when_collecting_an_elemental_infusion_to_gain_an_additional_elemental_infusion_of_the_same_type");

    // 25% chance when you gain an [Charges|Endurance Charge] to gain an additional [Charges|Endurance Charge]
    public static final Identifier CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE = id("chance_when_you_gain_an_endurance_charge_to_gain_an_additional_endurance_charge");

    // 10% chance when you gain a [Charges|Frenzy Charge] to gain an additional [Charges|Frenzy Charge]
    public static final Identifier CHANCE_WHEN_YOU_GAIN_A_FRENZY_CHARGE_TO_GAIN_AN_ADDITIONAL_FRENZY_CHARGE = id("chance_when_you_gain_a_frenzy_charge_to_gain_an_additional_frenzy_charge");

    // 25% chance when you gain a [Charges|Power Charge] to gain an additional [Charges|Power Charge]
    public static final Identifier CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE = id("chance_when_you_gain_a_power_charge_to_gain_an_additional_power_charge");

    // 30% chance when you Reload a [Crossbow] to be immediate
    public static final Identifier CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE = id("chance_when_you_reload_a_crossbow_to_be_immediate");

    // [Channelling] Skills deal 6% increased Damage
    public static final Identifier CHANNELLING_SKILLS_DEAL_INC_DAMAGE = id("channelling_skills_deal_inc_damage");

    // [Chaos|Chaos Damage] from [Fire] [Spell|Spells] [Contributes] to [Flammability] and [Ignite] [BuffMagnitude|Magnitudes]
    public static final Identifier CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE = id("chaos_damage_from_fire_spells_contributes_to_flammability_and_ignite_buff_magnitude");

    // [Charm|Charms] applied to you have 10% increased Effect
    public static final Identifier CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT = id("charms_applied_to_you_have_inc_effect");

    // [Charm|Charms] gain 0.15 charges per Second
    public static final Identifier CHARMS_GAIN_CHARGES_PER_SECOND = id("charms_gain_charges_per_second");

    // +1 [Charm] Slot
    public static final Identifier CHARM_SLOT = id("charm_slot");

    // +N to the number of times Supported Skills/Spells Echo (属性通道 -> coe:echo_count)
    // 语料无「+# additional Echo」类词缀 / 天赋：POE2 回响次数来源仅 4 个辅助宝石
    //（Echoing Cry 2 次 / Spell Echo 1 次 / Zarokh's Refrain 3 次 / Seal 联动），
    // 已全部走 EchoMod 数据通道。故本条为<b>纯接线</b>——先把属性通道挂上，
    // 语料补词缀/天赋时零 Java 改动即自动生效（读点见 EchoState#statEchoCount）。
    public static final Identifier ECHO_COUNT = id("echo_count");

    // [Companion|Companions] deal 12% increased Damage
    public static final Identifier COMPANIONS_DEAL_INC_DAMAGE = id("companions_deal_inc_damage");

    // [Companion|Companions] deal 60% increased damage against [Immobilised] enemies
    public static final Identifier COMPANIONS_DEAL_INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES = id("companions_deal_inc_damage_against_immobilised_enemies");

    // [Companion|Companions] gain 4% Damage as extra [Chaos|Chaos] Damage
    public static final Identifier COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("companions_gain_damage_as_extra_chaos_damage");

    // [Companion|Companions] gain 12% Damage as extra [Cold] Damage
    public static final Identifier COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE = id("companions_gain_damage_as_extra_cold_damage");

    // [Companion|Companions] gain your [Strength]
    public static final Identifier COMPANIONS_GAIN_YOUR_STRENGTH = id("companions_gain_your_strength");

    // [Companion|Companions] have a 40% chance to [Poison] on [HitDamage|Hit]
    public static final Identifier COMPANIONS_HAVE_A_CHANCE_TO_POISON_ON_HIT_DAMAGE = id("companions_have_a_chance_to_poison_on_hit_damage");

    // [Companion|Companions] have 50% chance to gain [Onslaught] on Kill
    public static final Identifier COMPANIONS_HAVE_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILL = id("companions_have_chance_to_gain_onslaught_on_kill");

    // [Companion|Companions] have 10% increased Area of Effect
    public static final Identifier COMPANIONS_HAVE_INC_AREA_OF_EFFECT = id("companions_have_inc_area_of_effect");

    // [Companion|Companions] have 6% increased [Attack] Speed
    public static final Identifier COMPANIONS_HAVE_INC_ATTACK_SPEED = id("companions_have_inc_attack_speed");

    // [Companion|Companions] have 12% increased maximum Life
    public static final Identifier COMPANIONS_HAVE_INC_MAX_HEALTH = id("companions_have_inc_max_health");

    // [Companion|Companions] have 20% increased Movement Speed
    public static final Identifier COMPANIONS_HAVE_INC_MOVEMENT_SPEED = id("companions_have_inc_movement_speed");

    // [Companion|Companions] have +12% to all Elemental [Resistances]
    public static final Identifier COMPANIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES = id("companions_have_to_all_elemental_resistances");

    // [Companion|Companions] have +30% to [Chaos] [Resistances|Resistance]
    public static final Identifier COMPANIONS_HAVE_TO_CHAOS_RESISTANCES = id("companions_have_to_chaos_resistances");

    // [Companion|Companions] in your [Presence] have [Onslaught] while you are [Shapeshift|Shapeshifted]
    public static final Identifier COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED = id("companions_in_your_presence_have_onslaught_while_you_are_shapeshifted");

    // Consume all [Rage] when [Shapeshift|Shapeshifting] to Human form to recover 1% of maximum life per [Rage] Consumed
    public static final Identifier CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED = id("consume_all_rage_when_shapeshifting_to_human_form_to_recover_of_max_health_per_rage_consumed");

    // Consuming [Glory] grants you 3% increased [Attack] damage per [Glory] consumed for 6 seconds, up to 60%
    public static final Identifier CONSUMING_GLORY_GRANTS_YOU_INC_ATTACK_DAMAGE_PER_GLORY_CONSUMED_FOR_SECONDS_UP_TO = id("consuming_glory_grants_you_inc_attack_damage_per_glory_consumed_for_seconds_up_to");

    // Create [Cold] [ElementalInfusion|Infusion] [Remnant|Remnants] instead of [Lightning]
    public static final Identifier CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING = id("create_cold_elemental_infusion_remnants_instead_of_lightning");

    // Create [Fire] [ElementalInfusion|Infusion] [Remnant|Remnants] instead of [Cold]
    public static final Identifier CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD = id("create_fire_elemental_infusion_remnants_instead_of_cold");

    // Create [Lightning] [ElementalInfusion|Infusion] [Remnant|Remnants] instead of [Fire]
    public static final Identifier CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE = id("create_lightning_elemental_infusion_remnants_instead_of_fire");

    // [Critical|Critical Hits] with [Dagger|Daggers] have a 25% chance to [Poison] the Enemy
    public static final Identifier CRITICAL_HITS_WITH_DAGGERS_HAVE_A_CHANCE_TO_POISON_THE_ENEMY = id("critical_hits_with_daggers_have_a_chance_to_poison_the_enemy");

    // [CullingStrike|Culling Strike] against Beasts while your [Companion] is in your [Presence]
    public static final Identifier CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE = id("culling_strike_against_beasts_while_your_companion_is_in_your_presence");

    // [CullingStrike|Culling Strike] against Enemies you [Mark]
    public static final Identifier CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK = id("culling_strike_against_enemies_you_mark");

    // Damage Penetrates 20% [ElementalDamage|Elemental] [Resistances] for each time you've used a Skill that Requires [Glory] in the past 6 seconds
    public static final Identifier DAMAGE_PENETRATES_ELEMENTAL_DAMAGE_RESISTANCES_FOR_EACH_TIME_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS = id("damage_penetrates_elemental_damage_resistances_for_each_time_you_ve_used_a_skill_that_requires_glory_in_the_past_seconds");

    // Damage Penetrates 3% of Enemy Elemental Resistances
    public static final Identifier DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_RESISTANCES = id("damage_penetrates_of_enemy_elemental_resistances");

    // Damage [Penetration|Penetrates] 6% [Resistances|Cold Resistance]
    public static final Identifier DAMAGE_PENETRATION_COLD_RESISTANCE = id("damage_penetration_cold_resistance");

    // Damage [Penetration|Penetrates] 12% [ElementalDamage|Elemental] [Resistances]
    public static final Identifier DAMAGE_PENETRATION_ELEMENTAL_DAMAGE_RESISTANCES = id("damage_penetration_elemental_damage_resistances");

    // Damage [Penetration|Penetrates] 8% [Resistances|Fire Resistance]
    public static final Identifier DAMAGE_PENETRATION_FIRE_RESISTANCE = id("damage_penetration_fire_resistance");

    // Damage [Penetration|Penetrates] 6% [Resistances|Lightning Resistance]
    public static final Identifier DAMAGE_PENETRATION_LIGHTNING_RESISTANCE = id("damage_penetration_lightning_resistance");

    // Damage [Penetration|Penetrates] 10% [Lightning|Lightning] [Resistances|Resistance] if on [LowMana|Low Mana]
    public static final Identifier DAMAGE_PENETRATION_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA = id("damage_penetration_lightning_resistances_if_on_low_mana");

    // Damage [Penetration|Penetrates] 15% of Enemy [ElementalDamage|Elemental] [Resistances] while [Shapeshift|Shapeshifted]
    public static final Identifier DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED = id("damage_penetration_of_enemy_elemental_damage_resistances_while_shapeshifted");

    // [HitDamage|Damage with Hits] is [Lucky|Lucky] against Enemies that are on [LowLife|Low Life]
    public static final Identifier DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH = id("damage_with_hits_is_lucky_against_enemies_that_are_on_low_health");

    // [HitDamage|Damage with Hits] is [Lucky|Lucky] against [HeavyStun|Heavy Stunned] Enemies
    public static final Identifier DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_HEAVY_STUNNED_ENEMIES = id("damage_with_hits_is_lucky_against_heavy_stunned_enemies");

    // [DamagingAilments|Damaging Ailments] Cannot Be inflicted on you while you already have one
    public static final Identifier DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE = id("damaging_ailments_cannot_be_inflicted_on_you_while_you_already_have_one");

    // [DamagingAilments|Damaging Ailments] deal damage 5% faster
    public static final Identifier DAMAGING_AILMENTS_DEAL_DAMAGE_FASTER = id("damaging_ailments_deal_damage_faster");

    // Deal no Non-Fire Damage
    public static final Identifier DEAL_NO_NON_FIRE_DAMAGE = id("deal_no_non_fire_damage");

    // [Debuff|Debuffs] inflicted by [Hazard|Hazards] have 30% increased [SlowMagnitudeModifier|Slow Magnitude]
    public static final Identifier DEBUFFS_INFLICTED_BY_HAZARDS_HAVE_INC_SLOW_MAGNITUDE = id("debuffs_inflicted_by_hazards_have_inc_slow_magnitude");

    // [Debuff|Debuffs] on you expire 25% faster
    public static final Identifier DEBUFFS_ON_YOU_EXPIRE_FASTER = id("debuffs_on_you_expire_faster");

    // [Debuff|Debuffs] you inflict have 5% increased [SlowMagnitudeModifier|Slow Magnitude]
    public static final Identifier DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE = id("debuffs_you_inflict_have_inc_slow_magnitude");

    // Defend with 200% of [Armour] against [Critical|Critical Hits]
    public static final Identifier DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS = id("defend_with_of_armor_against_critical_hits");

    // Defend with 150% of [Armour] against [HitDamage|Hits] from Enemies that are further than 6m away
    public static final Identifier DEFEND_WITH_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY = id("defend_with_of_armor_against_hit_damage_from_enemies_that_are_further_than_xm_away");

    // Defend with 120% of [Armour] against [Projectile] [Attack|Attacks]
    public static final Identifier DEFEND_WITH_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS = id("defend_with_of_armor_against_projectile_attacks");

    // Defend with 120% of [Armour] while not on [LowEnergyShield|Low Energy Shield]
    public static final Identifier DEFEND_WITH_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD = id("defend_with_of_armor_while_not_on_low_energy_shield");

    // [Deflect|Deflected] [HitDamage|Hits] cannot inflict [Bleeding] on you
    public static final Identifier DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU = id("deflected_hit_damage_cannot_inflict_bleeding_on_you");

    // [Deflect|Deflected] [HitDamage|Hits] cannot inflict [Maim] on you
    public static final Identifier DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_MAIM_ON_YOU = id("deflected_hit_damage_cannot_inflict_maim_on_you");

    // [Detonator] skills have 40% increased Area of Effect
    public static final Identifier DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("detonator_skills_have_inc_area_of_effect");

    // [Detonator] skills have 80% reduced damage
    public static final Identifier DETONATOR_SKILLS_HAVE_INC_DAMAGE = id("detonator_skills_have_inc_damage");

    // Dodge Roll cannot Avoid Damage
    public static final Identifier DODGE_ROLL_CANNOT_AVOID_DAMAGE = id("dodge_roll_cannot_avoid_damage");

    // Double Activation Delay of [Curse|Curses]
    public static final Identifier DOUBLE_ACTIVATION_DELAY_OF_CURSES = id("double_activation_delay_of_curses");

    // Each [Totem] applies 2% increased Damage taken to Enemies in their [Presence]
    public static final Identifier EACH_TOTEM_APPLIES_INC_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE = id("each_totem_applies_inc_damage_taken_to_enemies_in_their_presence");

    // Echoed [Spell|Spells] have 25% increased Area of Effect
    public static final Identifier ECHOED_SPELLS_HAVE_INC_AREA_OF_EFFECT = id("echoed_spells_have_inc_area_of_effect");

    // [Empowered|Empowered Attacks] deal 8% increased Damage
    public static final Identifier EMPOWERED_ATTACKS_DEAL_INC_DAMAGE = id("empowered_attacks_deal_inc_damage");

    // [Empowered] [Attack|Attacks] [Gain] 16% of Damage as Extra [Cold] Damage
    public static final Identifier EMPOWERED_ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE = id("empowered_attacks_gain_of_damage_as_extra_cold_damage");

    // [Empowered] [Attack|Attacks] [Gain] 15% of [Physical] Damage as Extra [Fire] damage
    public static final Identifier EMPOWERED_ATTACKS_GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE = id("empowered_attacks_gain_of_physical_damage_as_extra_fire_damage");

    // [Empowered] [Attack|Attacks] have 50% increased [Stun] Buildup
    public static final Identifier EMPOWERED_ATTACKS_HAVE_INC_STUN_BUILDUP = id("empowered_attacks_have_inc_stun_buildup");

    // Enemies affected by your [Hazard|Hazards] [Recently] have 25% reduced [Armour]
    public static final Identifier ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_ARMOR = id("enemies_affected_by_your_hazards_recently_have_inc_armor");

    // Enemies affected by your [Hazard|Hazards] [Recently] have 25% reduced [Evasion|Evasion Rating]
    public static final Identifier ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_EVASION = id("enemies_affected_by_your_hazards_recently_have_inc_evasion");

    // Enemies are [Intimidate|Intimidated] for 4 seconds when you [Immobilised|Immobilise] them
    public static final Identifier ENEMIES_ARE_INTIMIDATED_FOR_SECONDS_WHEN_YOU_IMMOBILISED_THEM = id("enemies_are_intimidated_for_seconds_when_you_immobilised_them");

    // Enemies are [Maim|Maimed] for 4 seconds after becoming [Pinned|Unpinned]
    public static final Identifier ENEMIES_ARE_MAIMED_FOR_SECONDS_AFTER_BECOMING_UNPINNED = id("enemies_are_maimed_for_seconds_after_becoming_unpinned");

    // Enemies Blinded by you have 50% reduced [Critical|Critical Hit] Chance
    public static final Identifier ENEMIES_BLINDED_BY_YOU_HAVE_INC_CRITICAL_HIT_CHANCE = id("enemies_blinded_by_you_have_inc_critical_hit_chance");

    // Enemies [Freeze|Frozen] by you have -8% to Cold Resistance
    public static final Identifier ENEMIES_FREEZE_BY_YOU_HAVE_TO_COLD_RESISTANCE = id("enemies_freeze_by_you_have_to_cold_resistance");

    // Enemies [Frozen|Frozen] by you take 20% increased [Cold] Damage
    public static final Identifier ENEMIES_FROZEN_BY_YOU_TAKE_INC_COLD_DAMAGE = id("enemies_frozen_by_you_take_inc_cold_damage");

    // Enemies [Ignite|Ignited] by you have -5% to Fire [Resistances|Resistance]
    public static final Identifier ENEMIES_IGNITED_BY_YOU_HAVE_TO_FIRE_RESISTANCES = id("enemies_ignited_by_you_have_to_fire_resistances");

    // Enemies [Ignite|Ignited] by you permanently take 1% increased [Fire] Damage for each second they have ever been [Ignite|Ignited] by you, up to a maximum of 10%
    public static final Identifier ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_INC_FIRE_DAMAGE_FOR_EACH_SECOND_THEY_HAVE_EVER_BEEN_IGNITED_BY_YOU_UP_TO_A_MAX_OF = id("enemies_ignited_by_you_permanently_take_inc_fire_damage_for_each_second_they_have_ever_been_ignited_by_you_up_to_a_max_of");

    // Enemies in [JaggedGround|Jagged Ground] you create take 10% increased Damage
    public static final Identifier ENEMIES_IN_JAGGED_GROUND_YOU_CREATE_TAKE_INC_DAMAGE = id("enemies_in_jagged_ground_you_create_take_inc_damage");

    // Enemies in your [Presence|Presence] have 10% reduced Cooldown Recovery Rate
    public static final Identifier ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_COOLDOWN_RECOVERY_RATE = id("enemies_in_your_presence_have_inc_cooldown_recovery_rate");

    // Enemies in your [Presence] have 75% reduced Life Regeneration rate
    public static final Identifier ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_HEALTH_REGENERATION_RATE = id("enemies_in_your_presence_have_inc_health_regeneration_rate");

    // Enemies near Enemies you [Mark] are [Blind|Blinded]
    public static final Identifier ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED = id("enemies_near_enemies_you_mark_are_blinded");

    // Enemies you apply [Incision] to take 2% increased [Physical] Damage per [Incision]
    public static final Identifier ENEMIES_YOU_APPLY_INCISION_TO_TAKE_INC_PHYSICAL_DAMAGE_PER_INCISION = id("enemies_you_apply_incision_to_take_inc_physical_damage_per_incision");

    // Enemies you [Curse] are [Hinder|Hindered], with 15% reduced Movement Speed
    public static final Identifier ENEMIES_YOU_CURSE_ARE_HINDERED_WITH_INC_MOVEMENT_SPEED = id("enemies_you_curse_are_hindered_with_inc_movement_speed");

    // Enemies you [Curse] cannot Recharge [EnergyShield|Energy Shield]
    public static final Identifier ENEMIES_YOU_CURSE_CANNOT_RECHARGE_ENERGY_SHIELD = id("enemies_you_curse_cannot_recharge_energy_shield");

    // Enemies you [Curse] have -3% to Chaos [Resistances|Resistance]
    public static final Identifier ENEMIES_YOU_CURSE_HAVE_TO_CHAOS_RESISTANCES = id("enemies_you_curse_have_to_chaos_resistances");

    // Enemies you [Electrocute|Electrocute] have 20% increased Damage taken
    public static final Identifier ENEMIES_YOU_ELECTROCUTE_HAVE_INC_DAMAGE_TAKEN = id("enemies_you_electrocute_have_inc_damage_taken");

    // Enemies you [ArmourBreak|Fully Armour Break] are [Maim|Maimed]
    public static final Identifier ENEMIES_YOU_FULLY_ARMOR_BREAK_ARE_MAIMED = id("enemies_you_fully_armor_break_are_maimed");

    // Enemies you [ArmourBreak|Fully Armour Break] cannot Regenerate Life
    public static final Identifier ENEMIES_YOU_FULLY_ARMOR_BREAK_CANNOT_REGENERATE_HEALTH = id("enemies_you_fully_armor_break_cannot_regenerate_health");

    // Enemies you [HeavyStun|Heavy Stun] while [Shapeshift|Shapeshifted] are [Intimidate|Intimidated] for 6 seconds
    public static final Identifier ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_SECONDS = id("enemies_you_heavy_stun_while_shapeshifted_are_intimidated_for_seconds");

    // Enemies you inflict [Bleeding] on cannot Regenerate Life
    public static final Identifier ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH = id("enemies_you_inflict_bleeding_on_cannot_regenerate_health");

    // Enemies you kill with [Empowered] [Attack|Attacks] have a 10% chance to Explode, dealing a tenth of their maximum Life as [Fire] Damage
    public static final Identifier ENEMIES_YOU_KILL_WITH_EMPOWERED_ATTACKS_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE = id("enemies_you_kill_with_empowered_attacks_have_a_chance_to_explode_dealing_a_tenth_of_their_max_health_as_fire_damage");

    // Enemies you [Mark] cannot deal [Critical|Critical Hits]
    public static final Identifier ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS = id("enemies_you_mark_cannot_deal_critical_hits");

    // Enemies you [Mark] have 10% reduced [Accuracy|Accuracy Rating]
    public static final Identifier ENEMIES_YOU_MARK_HAVE_INC_ACCURACY = id("enemies_you_mark_have_inc_accuracy");

    // Enemies you [Mark] take 10% increased Damage
    public static final Identifier ENEMIES_YOU_MARK_TAKE_INC_DAMAGE = id("enemies_you_mark_take_inc_damage");

    // [EnergyShield|Energy Shield] does not Recharge
    public static final Identifier ENERGY_SHIELD_DOES_NOT_RECHARGE = id("energy_shield_does_not_recharge");

    // Equipment and Skill Gems have 4% reduced [Attributes|Attribute] Requirements
    public static final Identifier EQUIPMENT_AND_SKILL_GEMS_HAVE_INC_ATTRIBUTES_REQUIREMENTS = id("equipment_and_skill_gems_have_inc_attributes_requirements");

    // Every five [Rage] also grants you 2% increased Minion Attack Speed
    public static final Identifier EVERY_FIVE_RAGE_ALSO_GRANTS_YOU_INC_MINION_ATTACK_SPEED = id("every_five_rage_also_grants_you_inc_minion_attack_speed");

    // Every [Rage] also grants 1% increased [Armour]
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_INC_ARMOR = id("every_rage_also_grants_inc_armor");

    // Every [Rage] also grants 1% increased [Fire] Damage
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_INC_FIRE_DAMAGE = id("every_rage_also_grants_inc_fire_damage");

    // Every 10 [Rage|Rage] also grants 12% increased [Physical|Physical] Damage
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_INC_PHYSICAL_DAMAGE = id("every_rage_also_grants_inc_physical_damage");

    // Every [Rage] also grants 2% increased [Spell] Damage
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_INC_SPELL_DAMAGE = id("every_rage_also_grants_inc_spell_damage");

    // Every [Rage] also grants 2% increased [StunThreshold|Stun Threshold]
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_INC_STUN_THRESHOLD = id("every_rage_also_grants_inc_stun_threshold");

    // Every [Rage] also grants you 1% increased Minion Damage
    public static final Identifier EVERY_RAGE_ALSO_GRANTS_YOU_INC_MINION_DAMAGE = id("every_rage_also_grants_you_inc_minion_damage");

    // Excess Life Recovery from Regeneration is applied to [EnergyShield|Energy Shield]
    public static final Identifier EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD = id("excess_health_recovery_from_regeneration_is_applied_to_energy_shield");

    // [Exposure] you inflict lowers [Resistances] by an additional 5%
    public static final Identifier EXPOSURE_YOU_INFLICT_LOWERS_RESISTANCES_BY_AN_ADDITIONAL = id("exposure_you_inflict_lowers_resistances_by_an_additional");

    // 10% faster [Curse] Activation
    public static final Identifier FASTER_CURSE_ACTIVATION = id("faster_curse_activation");

    // 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
    public static final Identifier FASTER_START_OF_ENERGY_SHIELD_RECHARGE = id("faster_start_of_energy_shield_recharge");

    // 40% [FasterESRechargeStart|faster start of Energy Shield Recharge] if you've been [Stun|Stunned] [Recently]
    public static final Identifier FASTER_START_OF_ENERGY_SHIELD_RECHARGE_IF_YOU_VE_BEEN_STUNNED_RECENTLY = id("faster_start_of_energy_shield_recharge_if_you_ve_been_stunned_recently");

    // 20% [FasterESRechargeStart|faster start of Energy Shield Recharge] when not on Full Life
    public static final Identifier FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHEN_NOT_ON_FULL_HEALTH = id("faster_start_of_energy_shield_recharge_when_not_on_full_health");

    // 20% faster start of [FasterESRechargeStart|Energy Shield Recharge] while affected by an [Archon] Buff
    public static final Identifier FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_AFFECTED_BY_AN_ARCHON_BUFF = id("faster_start_of_energy_shield_recharge_while_affected_by_an_archon_buff");

    // 10% [FasterESRechargeStart|faster start of Energy Shield Recharge] while [Shapeshift|Shapeshifted]
    public static final Identifier FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED = id("faster_start_of_energy_shield_recharge_while_shapeshifted");

    // 200% faster start of inherent [Rage] loss
    public static final Identifier FASTER_START_OF_INHERENT_RAGE_LOSS = id("faster_start_of_inherent_rage_loss");

    // [Fire] [Spell|Spells] [Conversion|Convert] 100% of Fire Damage to [Chaos|Chaos Damage]
    public static final Identifier FIRE_SPELLS_CONVERSION_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE = id("fire_spells_conversion_of_fire_damage_to_chaos_damage");

    // Fissure Skills have +1 to [Limit]
    public static final Identifier FISSURE_SKILLS_HAVE_TO_LIMIT = id("fissure_skills_have_to_limit");

    // [ArmourBreak|Fully Broken Armour] you inflict also increases [Fire] Damage Taken from [HitDamage|Hits]
    public static final Identifier FULLY_BROKEN_ARMOR_YOU_INFLICT_ALSO_INC_FIRE_DAMAGE_TAKEN_FROM_HIT_DAMAGE = id("fully_broken_armor_you_inflict_also_inc_fire_damage_taken_from_hit_damage");

    // Gain Accuracy Rating equal to your [Intelligence]
    public static final Identifier GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE = id("gain_accuracy_equal_to_your_intelligence");

    // Gain [Accuracy] Rating equal to your [Strength]
    public static final Identifier GAIN_ACCURACY_EQUAL_TO_YOUR_STRENGTH = id("gain_accuracy_equal_to_your_strength");

    // Gain additional [AilmentThreshold|Ailment Threshold] equal to 8% of maximum [EnergyShield|Energy Shield]
    public static final Identifier GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD = id("gain_additional_ailment_threshold_equal_to_of_max_energy_shield");

    // Gain additional [StunThreshold|Stun Threshold] equal to 30% of [ItemArmour|Item Armour] on [EquipArmour|Equipped Armour Items]
    public static final Identifier GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS = id("gain_additional_stun_threshold_equal_to_of_item_armor_on_equipped_armor_items");

    // Gain additional [StunThreshold|Stun Threshold] equal to 8% of maximum [EnergyShield|Energy Shield]
    public static final Identifier GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD = id("gain_additional_stun_threshold_equal_to_of_max_energy_shield");

    // Gain [AilmentThreshold|Ailment Threshold] equal to the lowest of [Evasion|Evasion] and [Armour|Armour] on your Boots
    public static final Identifier GAIN_AILMENT_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_BOOTS = id("gain_ailment_threshold_equal_to_the_lowest_of_evasion_and_armor_on_your_boots");

    // Gain an [Charges|Endurance Charge] when you [HeavyStun|Heavy Stun] a [Rarity|Rare or Unique] Enemy
    public static final Identifier GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY = id("gain_an_endurance_charge_when_you_heavy_stun_a_rare_or_unique_enemy");

    // Gain [ArcaneSurgeDuration|Arcane Surge] when you [Shapeshift] to Human form after being [Shapeshift|Shapeshifted] for at least 8 seconds
    public static final Identifier GAIN_ARCANE_SURGE_WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_AFTER_BEING_SHAPESHIFTED_FOR_AT_LEAST_SECONDS = id("gain_arcane_surge_when_you_shapeshift_to_human_form_after_being_shapeshifted_for_at_least_seconds");

    // Gain [Armour] equal to 150% of total [Strength] Requirements of Equipped Boots, Legs and Helmet
    public static final Identifier GAIN_ARMOR_EQUAL_TO_OF_TOTAL_STRENGTH_REQUIREMENTS_OF_EQUIPPED_BOOTS_LEGS_AND_HELMET = id("gain_armor_equal_to_of_total_strength_requirements_of_equipped_boots_legs_and_helmet");

    // Gain a [Charges|Power Charge] when you consume an [ElementalInfusion|Elemental Infusion]
    public static final Identifier GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION = id("gain_a_power_charge_when_you_consume_an_elemental_infusion");

    // Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
    public static final Identifier GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION = id("gain_deflection_rating_equal_to_of_evasion");

    // Gain [ElementalArchon|Elemental Archon] after spending 100% of your Maximum Mana
    public static final Identifier GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_OF_YOUR_MAX_MANA = id("gain_elemental_archon_after_spending_of_your_max_mana");

    // Gain [ElementalArchon|Elemental Archon] when your [ESRecharge|Energy Shield Recharge] begins
    public static final Identifier GAIN_ELEMENTAL_ARCHON_WHEN_YOUR_ENERGY_SHIELD_RECHARGE_BEGINS = id("gain_elemental_archon_when_your_energy_shield_recharge_begins");

    // Gain [Charges|Endurance Charges] instead of [Charges|Power Charges]
    public static final Identifier GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES = id("gain_endurance_charges_instead_of_power_charges");

    // Gain 10 [EnergyShield|Energy Shield] when you Block
    public static final Identifier GAIN_ENERGY_SHIELD_WHEN_YOU_BLOCK = id("gain_energy_shield_when_you_block");

    // Gain [Charges|Frenzy Charges] instead of [Charges|Endurance Charges]
    public static final Identifier GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES = id("gain_frenzy_charges_instead_of_endurance_charges");

    // Gain 3 Life per enemy killed
    public static final Identifier GAIN_HEALTH_PER_ENEMY_KILLED = id("gain_health_per_enemy_killed");

    // [Gain] 25% of [Cold] Damage as Extra [Fire] Damage against [Freeze|Frozen] Enemies
    public static final Identifier GAIN_OF_COLD_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_FREEZE_ENEMIES = id("gain_of_cold_damage_as_extra_fire_damage_against_freeze_enemies");

    // [Gain] 6% of [Cold] damage as Extra [Lightning] damage
    public static final Identifier GAIN_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE = id("gain_of_cold_damage_as_extra_lightning_damage");

    // [Gain] 11% of Damage as Extra [Chaos] Damage
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("gain_of_damage_as_extra_chaos_damage");

    // [Gain] 6% of Damage as Extra [Cold] Damage
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE = id("gain_of_damage_as_extra_cold_damage");

    // [Gain] 15% of Damage as Extra [Cold] Damage while on [ChilledGround|Chilled Ground]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND = id("gain_of_damage_as_extra_cold_damage_while_on_chilled_ground");

    // [Gain] 8% of Damage as Extra [Cold] Damage while [Shapeshift|Shapeshifted]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED = id("gain_of_damage_as_extra_cold_damage_while_shapeshifted");

    // [Gain] 8% of Damage as Extra Damage of a random [ElementalDamage|Element] while [Shapeshift|Shapeshifted]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED = id("gain_of_damage_as_extra_damage_of_a_random_elemental_damage_while_shapeshifted");

    // [Gain] 5% of Damage as Extra [Fire] Damage
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE = id("gain_of_damage_as_extra_fire_damage");

    // [Gain] 4% of Damage as Extra [Fire] Damage for every different [Grenade] fired in the past 8 seconds
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FOR_EVERY_DIFFERENT_GRENADE_FIRED_IN_THE_PAST_SECONDS = id("gain_of_damage_as_extra_fire_damage_for_every_different_grenade_fired_in_the_past_seconds");

    // Gain 2% of Damage as Extra [Fire] Damage per [Charges|Endurance Charge] consumed [Recently]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY = id("gain_of_damage_as_extra_fire_damage_per_endurance_charge_consumed_recently");

    // [Gain] 15% of Damage as Extra [Fire] Damage while on [IgnitedGround|Ignited Ground]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_ON_IGNITED_GROUND = id("gain_of_damage_as_extra_fire_damage_while_on_ignited_ground");

    // [Gain] 8% of Damage as Extra [Fire] Damage while [Shapeshift|Shapeshifted]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED = id("gain_of_damage_as_extra_fire_damage_while_shapeshifted");

    // [Gain] 5% of Damage as Extra [Lightning] Damage
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE = id("gain_of_damage_as_extra_lightning_damage");

    // [Gain] 15% of Damage as Extra [Lightning] Damage while on [ShockedGround|Shocked Ground]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND = id("gain_of_damage_as_extra_lightning_damage_while_on_shocked_ground");

    // [Gain] 8% of Damage as Extra [Lightning] Damage while [Shapeshift|Shapeshifted]
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED = id("gain_of_damage_as_extra_lightning_damage_while_shapeshifted");

    // [Gain] 10% of Damage as Extra [Physical] Damage
    public static final Identifier GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE = id("gain_of_damage_as_extra_physical_damage");

    // [Gain] 6% of [ElementalDamage|Elemental Damage] as Extra [Cold] Damage
    public static final Identifier GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_COLD_DAMAGE = id("gain_of_elemental_damage_as_extra_cold_damage");

    // [Gain] 6% of [ElementalDamage|Elemental Damage] as Extra [Fire] Damage
    public static final Identifier GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE = id("gain_of_elemental_damage_as_extra_fire_damage");

    // [Gain] 6% of [ElementalDamage|Elemental Damage] as Extra [Lightning] Damage
    public static final Identifier GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE = id("gain_of_elemental_damage_as_extra_lightning_damage");

    // [Gain] 8% of [Evasion] Rating as extra [Armour]
    public static final Identifier GAIN_OF_EVASION_AS_EXTRA_ARMOR = id("gain_of_evasion_as_extra_armor");

    // [Gain] 6% of [Lightning] damage as Extra [Cold] damage
    public static final Identifier GAIN_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE = id("gain_of_lightning_damage_as_extra_cold_damage");

    // Gain 15% of maximum [EnergyShield|Energy Shield] as additional [FreezeThreshold|Freeze Threshold]
    public static final Identifier GAIN_OF_MAX_ENERGY_SHIELD_AS_ADDITIONAL_FREEZE_THRESHOLD = id("gain_of_max_energy_shield_as_additional_freeze_threshold");

    // [Gain] 3% of [Physical|Physical] Damage as extra [Chaos|Chaos] Damage
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("gain_of_physical_damage_as_extra_chaos_damage");

    // [Gain] 12% of [Physical] Damage as Extra [Cold] Damage against [Daze|Dazed] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_DAZED_ENEMIES = id("gain_of_physical_damage_as_extra_cold_damage_against_dazed_enemies");

    // [Gain] 8% of [Physical] Damage as Extra [Cold] Damage against [Shock|Shocked] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_SHOCKED_ENEMIES = id("gain_of_physical_damage_as_extra_cold_damage_against_shocked_enemies");

    // [Gain] 12% of [Physical] Damage as Extra [Fire] Damage
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE = id("gain_of_physical_damage_as_extra_fire_damage");

    // [Gain] 25% of [Physical] Damage as Extra [Fire] Damage against [HeavyStun|Heavy Stunned] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES = id("gain_of_physical_damage_as_extra_fire_damage_against_heavy_stunned_enemies");

    // [Gain] 18% of [Physical] Damage as Extra [Lightning] Damage
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE = id("gain_of_physical_damage_as_extra_lightning_damage");

    // [Gain] 8% of [Physical] Damage as Extra [Lightning] Damage against [Chill|Chilled] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES = id("gain_of_physical_damage_as_extra_lightning_damage_against_chilled_enemies");

    // [Gain] 12% of [Physical] Damage as Extra [Lightning] Damage against [Daze|Dazed] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_DAZED_ENEMIES = id("gain_of_physical_damage_as_extra_lightning_damage_against_dazed_enemies");

    // [Gain] 25% of [Physical] Damage as Extra [Lightning] Damage against [Electrocute|Electrocuted] Enemies
    public static final Identifier GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES = id("gain_of_physical_damage_as_extra_lightning_damage_against_electrocuted_enemies");

    // Gain [Physical|Physical] [Thorns|Thorns] damage equal to 10% of [ItemArmour|Item Armour] on [Equipped] Body Armour
    public static final Identifier GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_BODY_ARMOR = id("gain_physical_thorns_damage_equal_to_of_item_armor_on_equipped_body_armor");

    // Gain [Physical|Physical] [Thorns|Thorns] damage equal to 8% of maximum Life while [Shapeshift|Shapeshifted]
    public static final Identifier GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED = id("gain_physical_thorns_damage_equal_to_of_max_health_while_shapeshifted");

    // Gain [Charges|Power Charges] instead of [Charges|Frenzy Charges]
    public static final Identifier GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES = id("gain_power_charges_instead_of_frenzy_charges");

    // Gain 2 [Rage|Rage] on [Melee|Melee] [Axe|Axe] [HitDamage|Hit]
    public static final Identifier GAIN_RAGE_ON_MELEE_AXE_HIT_DAMAGE = id("gain_rage_on_melee_axe_hit_damage");

    // Gain 1 [Rage|Rage] on [Melee] [HitDamage|Hit]
    public static final Identifier GAIN_RAGE_ON_MELEE_HIT_DAMAGE = id("gain_rage_on_melee_hit_damage");

    // Gain 2 [Rage] when [HitDamage|Hit] by an Enemy
    public static final Identifier GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY = id("gain_rage_when_hit_damage_by_an_enemy");

    // Gain 1 [Rage] when your [HitDamage|Hit] [Ignite|Ignites] a target
    public static final Identifier GAIN_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET = id("gain_rage_when_your_hit_damage_ignites_a_target");

    // Gain 8 [Rage] when you use a Life [Flask]
    public static final Identifier GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK = id("gain_rage_when_you_use_a_health_flask");

    // Gain [StunThreshold|Stun Threshold] equal to the lowest of [Evasion|Evasion] and [Armour|Armour] on your Helmet
    public static final Identifier GAIN_STUN_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_HELMET = id("gain_stun_threshold_equal_to_the_lowest_of_evasion_and_armor_on_your_helmet");

    // Gain 3 [Volatility] when an [Allies|Allied] [Persistent] [Reviving] [Minion] is [MinionDeath|Killed]
    public static final Identifier GAIN_VOLATILITY_WHEN_AN_ALLIES_PERSISTENT_REVIVING_MINION_IS_MINION_DEATH = id("gain_volatility_when_an_allies_persistent_reviving_minion_is_minion_death");

    // [Grenade|Grenades] have 15% chance to activate a second time
    public static final Identifier GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME = id("grenades_have_chance_to_activate_a_second_time");

    // [Grenade] Skills Fire an additional [Projectile|Projectile]
    public static final Identifier GRENADE_SKILLS_FIRE_AN_ADDITIONAL_PROJECTILE = id("grenade_skills_fire_an_additional_projectile");

    // [Grenade] Skills have +1 Cooldown Use
    public static final Identifier GRENADE_SKILLS_HAVE_COOLDOWN_USE = id("grenade_skills_have_cooldown_use");

    // [Hazard|Hazards] have 5% chance to rearm after they are triggered
    public static final Identifier HAZARDS_HAVE_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED = id("hazards_have_chance_to_rearm_after_they_are_triggered");

    // Life [Flask|Flasks] applied to you grant [Guard] for 4 seconds equal to 8% of the Life Recovery per Second they apply
    public static final Identifier HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD_FOR_SECONDS_EQUAL_TO_OF_THE_HEALTH_RECOVERY_PER_SECOND_THEY_APPLY = id("health_flasks_applied_to_you_grant_guard_for_seconds_equal_to_of_the_health_recovery_per_second_they_apply");

    // Life [Flask|Flasks] gain 0.1 charges per Second
    public static final Identifier HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND = id("health_flasks_gain_charges_per_second");

    // 10 Life gained when you [Block|Block]
    public static final Identifier HEALTH_GAIN_WHEN_YOU_BLOCK = id("health_gain_when_you_block");

    // [LifeLeech|Life Leech] effects are not removed when [Reservation|Unreserved] Life is Filled
    public static final Identifier HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED = id("health_leech_effects_are_not_add_when_reservation_health_is_filled");

    // [LifeLeech|Leech] Life 15% faster
    public static final Identifier HEALTH_LEECH_HEALTH_FASTER = id("health_leech_health_faster");

    // [LifeLeech|Leech] Life 5% slower
    public static final Identifier HEALTH_LEECH_HEALTH_SLOWER = id("health_leech_health_slower");

    // [LifeLoss|Lose] 3% of maximum Life and [EnergyShield|Energy Shield] when you use a Chaos Skill
    public static final Identifier HEALTH_LOSS_OF_MAX_HEALTH_AND_ENERGY_SHIELD_WHEN_YOU_USE_A_CHAOS_SKILL = id("health_loss_of_max_health_and_energy_shield_when_you_use_a_chaos_skill");

    // +1 Life per 4 [Dexterity]
    public static final Identifier HEALTH_PER_DEXTERITY = id("health_per_dexterity");

    // Life [LifeRecharge|Recharges] instead of [ESRecharge|Energy Shield]
    public static final Identifier HEALTH_RECHARGE_INSTEAD_OF_ENERGY_SHIELD = id("health_recharge_instead_of_energy_shield");

    // Life Recovery from your [Flask|Flasks] also applies to your [Companion|Companions]
    public static final Identifier HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS = id("health_recovery_from_your_flasks_also_applies_to_your_companions");

    // [Herald] Skills deal 20% increased Damage
    public static final Identifier HERALD_SKILLS_DEAL_INC_DAMAGE = id("herald_skills_deal_inc_damage");

    // [Herald] Skills have 25% increased Area of Effect
    public static final Identifier HERALD_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("herald_skills_have_inc_area_of_effect");

    // Hits that [HeavyStun|Heavy Stun] Enemies have [CullingStrike|Culling Strike]
    public static final Identifier HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE = id("hits_that_heavy_stun_enemies_have_culling_strike");

    // [HitDamage|Hits] against you have 25% reduced [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS = id("hit_damage_against_you_have_inc_critical_damage_bonus");

    // [HitDamage|Hits] [ArmourBreak|Break] 30% increased [Armour|Armour] on targets with [Ailments]
    public static final Identifier HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS = id("hit_damage_armor_break_inc_armor_on_targets_with_ailments");

    // [HitDamage|Hits] have 15% chance to treat Enemy Monster [ElementalDamage|Elemental] [Resistances|Resistance] values as inverted
    public static final Identifier HIT_DAMAGE_HAVE_CHANCE_TO_TREAT_ENEMY_MONSTER_ELEMENTAL_DAMAGE_RESISTANCES_VALUES_AS_INVERTED = id("hit_damage_have_chance_to_treat_enemy_monster_elemental_damage_resistances_values_as_inverted");

    // [HitDamage|Hits] have 25% reduced [Critical|Critical Hit] Chance against you
    public static final Identifier HIT_DAMAGE_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_YOU = id("hit_damage_have_inc_critical_hit_chance_against_you");

    // [HitDamage|Hits] ignore non-negative [ElementalDamage|Elemental] [Resistances] of [Frozen] Enemies
    public static final Identifier HIT_DAMAGE_IGNORE_NON_NEGATIVE_ELEMENTAL_DAMAGE_RESISTANCES_OF_FROZEN_ENEMIES = id("hit_damage_ignore_non_negative_elemental_damage_resistances_of_frozen_enemies");

    // If you would gain a [Charges|Charge], [Allies] in your [Presence] gain that [Charges|Charge] instead
    public static final Identifier IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD = id("if_you_would_gain_a_charges_allies_in_your_presence_gain_that_charges_instead");

    // [Ignite|Ignites] you cause are reflected back to you
    public static final Identifier IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU = id("ignites_you_cause_are_reflected_back_to_you");

    // [Ignite|Ignites] you inflict deal Damage 15% faster
    public static final Identifier IGNITES_YOU_INFLICT_DEAL_DAMAGE_FASTER = id("ignites_you_inflict_deal_damage_faster");

    // [Ignite] inflicted with [Fire] [Spell|Spells] deals [Chaos|Chaos Damage] instead of Fire Damage
    public static final Identifier IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE = id("ignite_inflicted_with_fire_spells_deals_chaos_damage_instead_of_fire_damage");

    // [Ignite] [Fire] [Damage] is [Cold|Cold Damage] instead of Fire Damage（点燃的火焰伤害视为冰霜伤害）
    public static final Identifier IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_COLD_DAMAGE_INSTEAD_OF_FIRE_DAMAGE = id("ignite_inflicted_with_fire_spells_deals_cold_damage_instead_of_fire_damage");
    // [Cold] [Damage] Contributes to [Ignite]（冰霜伤害造成点燃）
    public static final Identifier COLD_DAMAGE_CONTRIBUTES_TO_IGNITE = id("cold_damage_contributes_to_ignite");

    // Ignore all [ArmourPenalties|Movement Penalties] from [EquipArmour|Armour]
    public static final Identifier IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR = id("ignore_all_movement_penalties_from_equip_armor");

    // Immune to [Bleeding] if Equipped Helmet has higher [Armour] than [Evasion] Rating
    public static final Identifier IMMUNE_TO_BLEEDING_IF_EQUIPPED_HELMET_HAS_HIGHER_ARMOR_THAN_EVASION = id("immune_to_bleeding_if_equipped_helmet_has_higher_armor_than_evasion");

    // Immune to [Bleeding] while affected by an [Archon] [Buff]
    public static final Identifier IMMUNE_TO_BLEEDING_WHILE_AFFECTED_BY_AN_ARCHON_BUFF = id("immune_to_bleeding_while_affected_by_an_archon_buff");

    // Immune to [Bleeding] while [Shapeshift|Shapeshifted]
    public static final Identifier IMMUNE_TO_BLEEDING_WHILE_SHAPESHIFTED = id("immune_to_bleeding_while_shapeshifted");

    // Immune to [Chaos] Damage and [Bleeding]
    public static final Identifier IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING = id("immune_to_chaos_damage_and_bleeding");

    // Immune to [Chill] if a majority of your Socketed [SupportGem|Support Gems] are Blue
    public static final Identifier IMMUNE_TO_CHILL_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_BLUE = id("immune_to_chill_if_a_majority_of_your_socketed_support_gems_are_blue");

    // Immune to [CorruptedBlood|Corrupted Blood]
    public static final Identifier IMMUNE_TO_CORRUPTED_BLOOD = id("immune_to_corrupted_blood");

    // Immune to [Exposure]
    public static final Identifier IMMUNE_TO_EXPOSURE = id("immune_to_exposure");

    // Immune to [Freeze] and [Chill] while affected by an [Archon] [Buff]
    public static final Identifier IMMUNE_TO_FREEZE_AND_CHILL_WHILE_AFFECTED_BY_AN_ARCHON_BUFF = id("immune_to_freeze_and_chill_while_affected_by_an_archon_buff");

    // Immune to [Hinder]
    public static final Identifier IMMUNE_TO_HINDER = id("immune_to_hinder");

    // Immune to [Ignite] if a majority of your Socketed [SupportGem|Support Gems] are Red
    public static final Identifier IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED = id("immune_to_ignite_if_a_majority_of_your_socketed_support_gems_are_red");

    // Immune to [Maim]
    public static final Identifier IMMUNE_TO_MAIM = id("immune_to_maim");

    // Immune to [Maim] while [Shapeshift|Shapeshifted]
    public static final Identifier IMMUNE_TO_MAIM_WHILE_SHAPESHIFTED = id("immune_to_maim_while_shapeshifted");

    // Immune to [Poison]
    public static final Identifier IMMUNE_TO_POISON = id("immune_to_poison");

    // Immune to [Poison] if Equipped Helmet has higher [Evasion] Rating than [Armour]
    public static final Identifier IMMUNE_TO_POISON_IF_EQUIPPED_HELMET_HAS_HIGHER_EVASION_THAN_ARMOR = id("immune_to_poison_if_equipped_helmet_has_higher_evasion_than_armor");

    // Immune to [Shock] if a majority of your Socketed [SupportGem|Support Gems] are Green
    public static final Identifier IMMUNE_TO_SHOCK_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_GREEN = id("immune_to_shock_if_a_majority_of_your_socketed_support_gems_are_green");

    // Immune to [Shock] while affected by an [Archon] [Buff]
    public static final Identifier IMMUNE_TO_SHOCK_WHILE_AFFECTED_BY_AN_ARCHON_BUFF = id("immune_to_shock_while_affected_by_an_archon_buff");

    // 10% increased [Accuracy] Rating
    public static final Identifier INC_ACCURACY = id("inc_accuracy");

    // 20% increased [Accuracy] Rating against [Rarity|Rare or Unique] Enemies
    public static final Identifier INC_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES = id("inc_accuracy_against_rare_or_unique_enemies");

    // 30% increased [Accuracy] Rating at [CloseRange|Close Range]
    public static final Identifier INC_ACCURACY_AT_CLOSE_RANGE = id("inc_accuracy_at_close_range");

    // 10% increased Accuracy Rating while [DualWield|Dual Wielding]
    public static final Identifier INC_ACCURACY_WHILE_DUAL_WIELDING = id("inc_accuracy_while_dual_wielding");

    // 30% increased [Accuracy] Rating while moving
    public static final Identifier INC_ACCURACY_WHILE_MOVING = id("inc_accuracy_while_moving");

    // 20% increased [Accuracy] Rating while [Shapeshift|Shapeshifted]
    public static final Identifier INC_ACCURACY_WHILE_SHAPESHIFTED = id("inc_accuracy_while_shapeshifted");

    // 30% reduced [Accuracy] Rating while [Surrounded]
    public static final Identifier INC_ACCURACY_WHILE_SURROUNDED = id("inc_accuracy_while_surrounded");

    // 10% increased Accuracy Rating with Bows
    public static final Identifier INC_ACCURACY_WITH_BOWS = id("inc_accuracy_with_bows");

    // 8% increased [Accuracy] Rating with One Handed [Melee] Weapons
    public static final Identifier INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS = id("inc_accuracy_with_one_handed_melee_weapons");

    // 25% increased [Accuracy] Rating with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_ACCURACY_WITH_QUARTERSTAVES = id("inc_accuracy_with_quarterstaves");

    // 25% increased [Accuracy] Rating with [Spear|Spears]
    public static final Identifier INC_ACCURACY_WITH_SPEARS = id("inc_accuracy_with_spears");

    // 8% increased [Accuracy] Rating with Two Handed [Melee] Weapons
    public static final Identifier INC_ACCURACY_WITH_TWO_HANDED_MELEE_WEAPONS = id("inc_accuracy_with_two_handed_melee_weapons");

    // 40% increased [AilmentThreshold|Ailment] and [StunThreshold|Stun] Threshold while [Surrounded]
    public static final Identifier INC_AILMENT_THRESHOLD_AND_STUN_THRESHOLD_WHILE_SURROUNDED = id("inc_ailment_threshold_and_stun_threshold_while_surrounded");

    // 12% increased amount of Life [LifeLeech|Leeched]
    public static final Identifier INC_AMOUNT_OF_HEALTH_LEECH = id("inc_amount_of_health_leech");

    // 10% increased amount of Life [LifeLeech|Leeched] while [Shapeshift|Shapeshifted]
    public static final Identifier INC_AMOUNT_OF_HEALTH_LEECH_WHILE_SHAPESHIFTED = id("inc_amount_of_health_leech_while_shapeshifted");

    // 10% increased amount of Mana [ManaLeech|Leeched]
    public static final Identifier INC_AMOUNT_OF_MANA_LEECH = id("inc_amount_of_mana_leech");

    // Increases and Reductions to [Companion] Damage also apply to you
    public static final Identifier INC_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU = id("inc_and_reductions_to_companion_damage_also_apply_to_you");

    // Increases and Reductions to [Projectile|Projectile] Speed also apply to Damage with [Bow|Bows]
    public static final Identifier INC_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS = id("inc_and_reductions_to_projectile_speed_also_apply_to_damage_with_bows");

    // 15% increased [Archon] Buff duration
    public static final Identifier INC_ARCHON_BUFF_DURATION = id("inc_archon_buff_duration");

    // 5% increased Area Damage
    public static final Identifier INC_AREA_DAMAGE = id("inc_area_damage");

    // 6% increased Area of Effect
    public static final Identifier INC_AREA_OF_EFFECT = id("inc_area_of_effect");

    // 6% increased Area of Effect for [Attack|Attacks]
    public static final Identifier INC_AREA_OF_EFFECT_FOR_ATTACKS = id("inc_area_of_effect_for_attacks");

    // 4% increased Area of Effect for [Attack|Attacks] per Enemy you've [Ignite|Ignited] in the last 8 seconds, up to 40%
    public static final Identifier INC_AREA_OF_EFFECT_FOR_ATTACKS_PER_ENEMY_YOU_VE_IGNITED_IN_THE_LAST_SECONDS_UP_TO = id("inc_area_of_effect_for_attacks_per_enemy_you_ve_ignited_in_the_last_seconds_up_to");

    // 10% increased Area of Effect for Skills used by Totems
    public static final Identifier INC_AREA_OF_EFFECT_FOR_SKILLS_USED_BY_TOTEMS = id("inc_area_of_effect_for_skills_used_by_totems");

    // 12% increased Area of Effect if you have [Stun|Stunned] an Enemy [Recently|Recently]
    public static final Identifier INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY = id("inc_area_of_effect_if_you_have_stunned_an_enemy_recently");

    // 15% increased Area of Effect if you've Killed [Recently]
    public static final Identifier INC_AREA_OF_EFFECT_IF_YOU_VE_KILLED_RECENTLY = id("inc_area_of_effect_if_you_ve_killed_recently");

    // 25% increased Area of Effect if you've [Stun|Stunned] an Enemy with a Two Handed [Melee] Weapon [Recently|Recently]
    public static final Identifier INC_AREA_OF_EFFECT_IF_YOU_VE_STUNNED_AN_ENEMY_WITH_A_TWO_HANDED_MELEE_WEAPON_RECENTLY = id("inc_area_of_effect_if_you_ve_stunned_an_enemy_with_a_two_handed_melee_weapon_recently");

    // 4% increased Area of Effect of [AncestralBoost|Ancestrally Boosted] [Attack|Attacks]
    public static final Identifier INC_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS = id("inc_area_of_effect_of_ancestrally_boosted_attacks");

    // 20% increased Area of Effect of [Curse|Curses]
    public static final Identifier INC_AREA_OF_EFFECT_OF_CURSES = id("inc_area_of_effect_of_curses");

    // 12% increased Area of Effect while you have [ArcaneSurge|Arcane Surge]
    public static final Identifier INC_AREA_OF_EFFECT_WHILE_YOU_HAVE_ARCANE_SURGE = id("inc_area_of_effect_while_you_have_arcane_surge");

    // 15% increased Area of Effect while you have a Totem
    public static final Identifier INC_AREA_OF_EFFECT_WHILE_YOU_HAVE_A_TOTEM = id("inc_area_of_effect_while_you_have_a_totem");

    // 10% increased [Armour]
    public static final Identifier INC_ARMOR = id("inc_armor");
    public static final Identifier INC_ENERGY_SHIELD = id("inc_energy_shield");

    // 12% increased [Armour] and [Evasion] Rating
    public static final Identifier INC_ARMOR_AND_EVASION = id("inc_armor_and_evasion");

    // 10% increased [Armour] and [Evasion] Rating per Summoned [Totem] in your [Presence]
    public static final Identifier INC_ARMOR_AND_EVASION_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE = id("inc_armor_and_evasion_per_summoned_totem_in_your_presence");

    // 80% increased [Armour] and [Evasion] Rating when on [LowLife|Low Life]
    public static final Identifier INC_ARMOR_AND_EVASION_WHEN_ON_LOW_HEALTH = id("inc_armor_and_evasion_when_on_low_health");

    // 30% increased [Armour] and [Evasion] Rating while Leeching
    public static final Identifier INC_ARMOR_AND_EVASION_WHILE_LEECHING = id("inc_armor_and_evasion_while_leeching");

    // 100% increased [ArmourBreak|Armour Break] Duration
    public static final Identifier INC_ARMOR_BREAK_DURATION = id("inc_armor_break_duration");

    // 50% reduced [ArmourBreak|Armour Break] taken
    public static final Identifier INC_ARMOR_BREAK_TAKEN = id("inc_armor_break_taken");

    // 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield]
    public static final Identifier INC_ARMOR_EVASION_AND_ENERGY_SHIELD = id("inc_armor_evasion_and_energy_shield");

    // 100% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] from Equipped [Shield]
    public static final Identifier INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD = id("inc_armor_evasion_and_energy_shield_from_equipped_shield");

    // 4% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while [Channelling]
    public static final Identifier INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING = id("inc_armor_evasion_and_energy_shield_while_channelling");

    // 30% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while wielding a [Quarterstaff]
    public static final Identifier INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_WIELDING_A_QUARTERSTAFF = id("inc_armor_evasion_and_energy_shield_while_wielding_a_quarterstaff");

    // 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
    public static final Identifier INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE = id("inc_armor_evasion_and_energy_shield_while_your_companion_is_in_your_presence");

    // 60% increased [Armour] from Equipped Body Armour
    public static final Identifier INC_ARMOR_FROM_EQUIPPED_BODY_ARMOR = id("inc_armor_from_equipped_body_armor");

    // 20% increased [Armour|Armour] if you haven't been [HitDamage|Hit] [Recently|Recently]
    public static final Identifier INC_ARMOR_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY = id("inc_armor_if_you_haven_t_been_hit_damage_recently");

    // 20% increased [Armour] if you have been [HitDamage|Hit] [Recently]
    public static final Identifier INC_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY = id("inc_armor_if_you_have_been_hit_damage_recently");

    // 10% increased [Armour] if you've consumed an [Charges|Endurance Charge] [Recently]
    public static final Identifier INC_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY = id("inc_armor_if_you_ve_consumed_an_endurance_charge_recently");

    // 25% increased [Armour] if you've [HitDamage|Hit] an Enemy with a [Melee|Melee Attack] [Recently]
    public static final Identifier INC_ARMOR_IF_YOU_VE_HIT_DAMAGE_AN_ENEMY_WITH_A_MELEE_ATTACK_RECENTLY = id("inc_armor_if_you_ve_hit_damage_an_enemy_with_a_melee_attack_recently");

    // 30% increased [Armour] while Bleeding
    public static final Identifier INC_ARMOR_WHILE_BLEEDING = id("inc_armor_while_bleeding");

    // 50% increased [Armour] while [Ignite|Ignited]
    public static final Identifier INC_ARMOR_WHILE_IGNITED = id("inc_armor_while_ignited");

    // 10% increased [Armour] while [Shapeshift|Shapeshifted]
    public static final Identifier INC_ARMOR_WHILE_SHAPESHIFTED = id("inc_armor_while_shapeshifted");

    // 15% increased [Armour] while stationary
    public static final Identifier INC_ARMOR_WHILE_STATIONARY = id("inc_armor_while_stationary");

    // 20% increased [Armour|Armour] while [Surrounded|Surrounded]
    public static final Identifier INC_ARMOR_WHILE_SURROUNDED = id("inc_armor_while_surrounded");

    // 8% increased [Attack] and Cast Speed
    public static final Identifier INC_ATTACK_AND_CAST_SPEED = id("inc_attack_and_cast_speed");

    // 8% increased [Attack] and Cast Speed during Effect of any Mana [Flask]
    public static final Identifier INC_ATTACK_AND_CAST_SPEED_DURING_EFFECT_OF_ANY_MANA_FLASK = id("inc_attack_and_cast_speed_during_effect_of_any_mana_flask");

    // 8% increased [Attack] and Cast Speed if you've summoned a [Totem|Totem] [Recently|Recently]
    public static final Identifier INC_ATTACK_AND_CAST_SPEED_IF_YOU_VE_SUMMONED_A_TOTEM_RECENTLY = id("inc_attack_and_cast_speed_if_you_ve_summoned_a_totem_recently");

    // 3% increased [Attack] and Cast Speed with Elemental Skills
    public static final Identifier INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS = id("inc_attack_and_cast_speed_with_elemental_skills");

    // 3% increased [Attack] and Cast Speed with [Lightning|Lightning] Skills
    public static final Identifier INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS = id("inc_attack_and_cast_speed_with_lightning_skills");

    // 6% increased [Attack] Area Damage
    public static final Identifier INC_ATTACK_AREA_DAMAGE = id("inc_attack_area_damage");

    // 8% increased [Attack] Cold Damage
    public static final Identifier INC_ATTACK_COLD_DAMAGE = id("inc_attack_cold_damage");

    // 15% increased [Attack] [Critical|Critical Hit Chance] while [DualWield|Dual Wielding]
    public static final Identifier INC_ATTACK_CRITICAL_HIT_CHANCE_WHILE_DUAL_WIELDING = id("inc_attack_critical_hit_chance_while_dual_wielding");

    // 8% increased [Attack] Damage
    public static final Identifier INC_ATTACK_DAMAGE = id("inc_attack_damage");

    // 16% increased [Attack] Damage against Bleeding Enemies
    public static final Identifier INC_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES = id("inc_attack_damage_against_bleeding_enemies");

    // 40% increased [Attack] Damage against Maimed Enemies
    public static final Identifier INC_ATTACK_DAMAGE_AGAINST_MAIMED_ENEMIES = id("inc_attack_damage_against_maimed_enemies");

    // 16% increased [Attack] Damage against [Rarity|Rare or Unique] Enemies
    public static final Identifier INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES = id("inc_attack_damage_against_rare_or_unique_enemies");

    // 12% increased [Attack] Damage for each different Non-Instant [Spell] you've used in the past 8 seconds
    public static final Identifier INC_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL_YOU_VE_USED_IN_THE_PAST_SECONDS = id("inc_attack_damage_for_each_different_non_instant_spell_you_ve_used_in_the_past_seconds");

    // 5% increased [Attack] Damage for each [Minion] in your [Presence], up to a maximum of 80%
    public static final Identifier INC_ATTACK_DAMAGE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF = id("inc_attack_damage_for_each_minion_in_your_presence_up_to_a_max_of");

    // 25% increased [Attack] Damage if you have been [HeavyStunPlayer|Heavy Stunned] [Recently]
    public static final Identifier INC_ATTACK_DAMAGE_IF_YOU_HAVE_BEEN_HEAVY_STUNNED_RECENTLY = id("inc_attack_damage_if_you_have_been_heavy_stunned_recently");

    // 15% increased [Attack] Damage if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
    public static final Identifier INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY = id("inc_attack_damage_if_you_have_shapeshifted_to_an_animal_form_recently");

    // 30% increased [Attack] Damage if you've Cast a [Spell] [Recently]
    public static final Identifier INC_ATTACK_DAMAGE_IF_YOU_VE_CAST_A_SPELL_RECENTLY = id("inc_attack_damage_if_you_ve_cast_a_spell_recently");

    // 4% increased [Attack] Damage per 75 [ItemArmour|Item Armour] and [ItemEvasion|Evasion] on Equipped Shield
    public static final Identifier INC_ATTACK_DAMAGE_PER_ITEM_ARMOR_AND_ITEM_EVASION_ON_EQUIPPED_SHIELD = id("inc_attack_damage_per_item_armor_and_item_evasion_on_equipped_shield");

    // 10% increased [Attack] Damage per Summoned [Totem] in your [Presence]
    public static final Identifier INC_ATTACK_DAMAGE_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE = id("inc_attack_damage_per_summoned_totem_in_your_presence");

    // 12% increased [Attack] Damage while [DualWield|Dual Wielding]
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_DUAL_WIELDING = id("inc_attack_damage_while_dual_wielding");

    // 12% increased [Attack] Damage while moving
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_MOVING = id("inc_attack_damage_while_moving");

    // 40% increased [Attack] Damage while on Full Life
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_ON_FULL_HEALTH = id("inc_attack_damage_while_on_full_health");

    // 25% increased [Attack|Attack] Damage while on [LowLife|Low Life]
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH = id("inc_attack_damage_while_on_low_health");

    // 25% increased [Attack|Attack] Damage while [Surrounded|Surrounded]
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_SURROUNDED = id("inc_attack_damage_while_surrounded");

    // 8% increased [Attack|Attack] Damage while you have an [Allies|Ally] in your [Presence|Presence]
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE = id("inc_attack_damage_while_you_have_an_allies_in_your_presence");

    // 25% increased [Attack] Damage while you have no Life [Flask] uses left
    public static final Identifier INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_NO_HEALTH_FLASK_USES_LEFT = id("inc_attack_damage_while_you_have_no_health_flask_uses_left");

    // 12% increased [Attack] [Physical] Damage
    public static final Identifier INC_ATTACK_PHYSICAL_DAMAGE = id("inc_attack_physical_damage");

    // 3% increased [Attack] Speed
    public static final Identifier INC_ATTACK_SPEED = id("inc_attack_speed");

    // 6% increased [Attack] Speed during any [Flask] Effect
    public static final Identifier INC_ATTACK_SPEED_DURING_ANY_FLASK_EFFECT = id("inc_attack_speed_during_any_flask_effect");

    // 16% increased [Attack] Speed if you haven't Attacked [Recently|Recently]
    public static final Identifier INC_ATTACK_SPEED_IF_YOU_HAVEN_T_ATTACKED_RECENTLY = id("inc_attack_speed_if_you_haven_t_attacked_recently");

    // 15% increased [Attack] Speed if you've been Hit [Recently]
    public static final Identifier INC_ATTACK_SPEED_IF_YOU_VE_BEEN_HIT_RECENTLY = id("inc_attack_speed_if_you_ve_been_hit_recently");

    // 3% increased [Attack] Speed if you've dealt a [Critical|Critical Hit] [Recently|Recently]
    public static final Identifier INC_ATTACK_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY = id("inc_attack_speed_if_you_ve_dealt_a_critical_hit_recently");

    // 8% increased [Attack] Speed if you've killed [Recently]
    public static final Identifier INC_ATTACK_SPEED_IF_YOU_VE_KILLED_RECENTLY = id("inc_attack_speed_if_you_ve_killed_recently");

    // 12% increased [Attack] Speed if you've successfully [Parry|Parried] [Recently]
    public static final Identifier INC_ATTACK_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY = id("inc_attack_speed_if_you_ve_successfully_parried_recently");

    // 1% increased [Attack] Speed per 400 [Accuracy|Accuracy] Rating, up to 20%
    public static final Identifier INC_ATTACK_SPEED_PER_ACCURACY_UP_TO = id("inc_attack_speed_per_accuracy_up_to");

    // 1% increased [Attack] Speed per 25 [Dexterity]
    public static final Identifier INC_ATTACK_SPEED_PER_DEXTERITY = id("inc_attack_speed_per_dexterity");

    // 3% increased [Attack] Speed per Enemy in [CloseRange|Close Range]
    public static final Identifier INC_ATTACK_SPEED_PER_ENEMY_IN_CLOSE_RANGE = id("inc_attack_speed_per_enemy_in_close_range");

    // 4% increased [Attack] Speed while a [Rarity|Rare or Unique] Enemy is in your [Presence]
    public static final Identifier INC_ATTACK_SPEED_WHILE_A_RARE_OR_UNIQUE_ENEMY_IS_IN_YOUR_PRESENCE = id("inc_attack_speed_while_a_rare_or_unique_enemy_is_in_your_presence");

    // 3% increased [Attack] Speed while [DualWield|Dual Wielding]
    public static final Identifier INC_ATTACK_SPEED_WHILE_DUAL_WIELDING = id("inc_attack_speed_while_dual_wielding");

    // 3% increased [Attack] Speed while holding a Shield
    public static final Identifier INC_ATTACK_SPEED_WHILE_HOLDING_A_SHIELD = id("inc_attack_speed_while_holding_a_shield");

    // 15% increased [Attack] Speed while Leeching
    public static final Identifier INC_ATTACK_SPEED_WHILE_LEECHING = id("inc_attack_speed_while_leeching");

    // 15% increased [Attack] Speed while not on Low Mana
    public static final Identifier INC_ATTACK_SPEED_WHILE_NOT_ON_LOW_MANA = id("inc_attack_speed_while_not_on_low_mana");

    // 20% increased [Attack|Attack] Speed while [Surrounded|Surrounded]
    public static final Identifier INC_ATTACK_SPEED_WHILE_SURROUNDED = id("inc_attack_speed_while_surrounded");

    // 4% increased [Attack] Speed while your [Companion] is in your [Presence]
    public static final Identifier INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE = id("inc_attack_speed_while_your_companion_is_in_your_presence");

    // 3% increased [Attack] Speed with [Axe|Axes]
    public static final Identifier INC_ATTACK_SPEED_WITH_AXES = id("inc_attack_speed_with_axes");

    // 3% increased [Attack] Speed with Bows
    public static final Identifier INC_ATTACK_SPEED_WITH_BOWS = id("inc_attack_speed_with_bows");

    // 15% reduced [Attack] Speed with [Crossbow|Crossbows]
    public static final Identifier INC_ATTACK_SPEED_WITH_CROSSBOWS = id("inc_attack_speed_with_crossbows");

    // 3% increased [Attack] Speed with [Dagger|Daggers]
    public static final Identifier INC_ATTACK_SPEED_WITH_DAGGERS = id("inc_attack_speed_with_daggers");

    // 6% increased [Attack] Speed with [Flail|Flails]
    public static final Identifier INC_ATTACK_SPEED_WITH_FLAILS = id("inc_attack_speed_with_flails");

    // 6% increased [Attack] Speed with One Handed [Melee] Weapons
    public static final Identifier INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS = id("inc_attack_speed_with_one_handed_melee_weapons");

    // 8% increased [Attack] Speed with One Handed Weapons
    public static final Identifier INC_ATTACK_SPEED_WITH_ONE_HANDED_WEAPONS = id("inc_attack_speed_with_one_handed_weapons");

    // 8% increased [Attack] Speed with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_ATTACK_SPEED_WITH_QUARTERSTAVES = id("inc_attack_speed_with_quarterstaves");

    // 3% increased [Attack] Speed with [Spear|Spears]
    public static final Identifier INC_ATTACK_SPEED_WITH_SPEARS = id("inc_attack_speed_with_spears");

    // 3% increased [Attack] Speed with Swords
    public static final Identifier INC_ATTACK_SPEED_WITH_SWORDS = id("inc_attack_speed_with_swords");

    // 7% increased [Attributes]
    public static final Identifier INC_ATTRIBUTES = id("inc_attributes");

    // 10% increased [Ballista] [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier INC_BALLISTA_CRITICAL_DAMAGE_BONUS = id("inc_ballista_critical_damage_bonus");

    // 10% increased [Ballista] [Critical|Critical Hit Chance]
    public static final Identifier INC_BALLISTA_CRITICAL_HIT_CHANCE = id("inc_ballista_critical_hit_chance");

    // 15% increased [Ballista] damage
    public static final Identifier INC_BALLISTA_DAMAGE = id("inc_ballista_damage");

    // 20% increased [Ballista] [Immobilised|Immobilisation] buildup
    public static final Identifier INC_BALLISTA_IMMOBILISATION_BUILDUP = id("inc_ballista_immobilisation_buildup");

    // 10% increased Bleeding Duration
    public static final Identifier INC_BLEEDING_DURATION = id("inc_bleeding_duration");

    // 25% increased [Blind] duration
    public static final Identifier INC_BLIND_DURATION = id("inc_blind_duration");

    // 20% increased [Blind] Effect
    public static final Identifier INC_BLIND_EFFECT = id("inc_blind_effect");

    // 5% increased [Block] chance
    public static final Identifier INC_BLOCK_CHANCE = id("inc_block_chance");

    // 30% increased [Block|Block] chance while [Surrounded]
    public static final Identifier INC_BLOCK_CHANCE_WHILE_SURROUNDED = id("inc_block_chance_while_surrounded");

    // 40% increased [Block] Recovery
    public static final Identifier INC_BLOCK_RECOVERY = id("inc_block_recovery");

    // 8% increased Bolt Speed
    public static final Identifier INC_BOLT_SPEED = id("inc_bolt_speed");

    // 6% increased bonuses gained from Equipped [Quiver]
    public static final Identifier INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER = id("inc_bonuses_gain_from_equipped_quiver");

    // 15% increased [BuffEffect|effect] of [ArcaneSurge|Arcane Surge] on you
    public static final Identifier INC_BUFF_EFFECT_OF_ARCANE_SURGE_ON_YOU = id("inc_buff_effect_of_arcane_surge_on_you");

    // 15% reduced [BuffEffect|effect] of [Curse|Curses] on you
    public static final Identifier INC_BUFF_EFFECT_OF_CURSES_ON_YOU = id("inc_buff_effect_of_curses_on_you");

    // 30% reduced [BuffEffect|effect] of [Shock|Shock] on you
    public static final Identifier INC_BUFF_EFFECT_OF_SHOCK_ON_YOU = id("inc_buff_effect_of_shock_on_you");

    // 50% reduced [BuffEffect|effect] of [Withered|Withered] on you
    public static final Identifier INC_BUFF_EFFECT_OF_WITHERED_ON_YOU = id("inc_buff_effect_of_withered_on_you");

    // 30% increased [BuffMagnitude|Magnitude] of [Ailments] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT = id("inc_buff_magnitude_of_ailments_you_inflict");

    // 25% increased [BuffMagnitude|Magnitude] of [Ailments] you inflict against [Mark|Marked] Enemies
    public static final Identifier INC_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT_AGAINST_MARKED_ENEMIES = id("inc_buff_magnitude_of_ailments_you_inflict_against_marked_enemies");

    // 50% reduced [BuffMagnitude|Magnitude] of [Bleeding] on You
    public static final Identifier INC_BUFF_MAGNITUDE_OF_BLEEDING_ON_YOU = id("inc_buff_magnitude_of_bleeding_on_you");

    // 10% increased [BuffMagnitude|Magnitude] of [Bleeding|Bleeding] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT = id("inc_buff_magnitude_of_bleeding_you_inflict");

    // 15% increased [BuffMagnitude|Magnitude] of [Bleeding|Bleeding] you inflict against Enemies affected by [Incision]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION = id("inc_buff_magnitude_of_bleeding_you_inflict_against_enemies_affected_by_incision");

    // 40% increased [BuffMagnitude|Magnitude] of [Bleeding] you inflict against [Pinned] Enemies
    public static final Identifier INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_PINNED_ENEMIES = id("inc_buff_magnitude_of_bleeding_you_inflict_against_pinned_enemies");

    // 15% increased [BuffMagnitude|Magnitude] of [Bleeding|Bleeding] you inflict with [Critical|Critical Hits]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS = id("inc_buff_magnitude_of_bleeding_you_inflict_with_critical_hits");

    // 12% increased [BuffMagnitude|Magnitude] of [Chill] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT = id("inc_buff_magnitude_of_chill_you_inflict");

    // 20% increased [BuffMagnitude|Magnitude] of [DamagingAilments|Damaging Ailments] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT = id("inc_buff_magnitude_of_damaging_ailments_you_inflict");

    // 15% increased [BuffMagnitude|Magnitude] of [DamagingAilments|Damaging Ailments] you inflict with [Critical|Critical Hits]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS = id("inc_buff_magnitude_of_damaging_ailments_you_inflict_with_critical_hits");

    // 15% increased [BuffMagnitude|Magnitude] of [Daze]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_DAZE = id("inc_buff_magnitude_of_daze");

    // 35% increased [BuffMagnitude|Magnitude] of [Ignite|Ignite] against [Poison|Poisoned] enemies
    public static final Identifier INC_BUFF_MAGNITUDE_OF_IGNITE_AGAINST_POISONED_ENEMIES = id("inc_buff_magnitude_of_ignite_against_poisoned_enemies");

    // 40% reduced [BuffMagnitude|Magnitude] of [Ignite|Ignite] on you
    public static final Identifier INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU = id("inc_buff_magnitude_of_ignite_on_you");

    // 20% increased [BuffMagnitude|Magnitude] of [Impale]s inflicted with Spells
    public static final Identifier INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS = id("inc_buff_magnitude_of_impales_inflicted_with_spells");

    // 15% increased [BuffMagnitude|Magnitude] of [JaggedGround|Jagged Ground] you create
    public static final Identifier INC_BUFF_MAGNITUDE_OF_JAGGED_GROUND_YOU_CREATE = id("inc_buff_magnitude_of_jagged_ground_you_create");

    // 30% increased [BuffMagnitude|Magnitude] of [NonDamagingAilments|Non-Damaging Ailments] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT = id("inc_buff_magnitude_of_non_damaging_ailments_you_inflict");

    // 20% increased [BuffMagnitude|Magnitude] of [NonDamagingAilments|Non-Damaging Ailments] you inflict with [Critical|Critical Hits]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS = id("inc_buff_magnitude_of_non_damaging_ailments_you_inflict_with_critical_hits");

    // 10% increased [BuffMagnitude|Magnitude] of [Poison] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT = id("inc_buff_magnitude_of_poison_you_inflict");

    // 80% increased [BuffMagnitude|Magnitude] of [Poison] you inflict on targets that are not Poisoned
    public static final Identifier INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED = id("inc_buff_magnitude_of_poison_you_inflict_on_targets_that_are_not_poisoned");

    // 15% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict
    public static final Identifier INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT = id("inc_buff_magnitude_of_shock_you_inflict");

    // 40% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict with [Critical|Critical Hits]
    public static final Identifier INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT_WITH_CRITICAL_HITS = id("inc_buff_magnitude_of_shock_you_inflict_with_critical_hits");

    // 3% increased Cast Speed
    public static final Identifier INC_CAST_SPEED = id("inc_cast_speed");

    // 6% increased Cast Speed for each different [Spell] you've Cast in the last eight seconds
    public static final Identifier INC_CAST_SPEED_FOR_EACH_DIFFERENT_SPELL_YOU_VE_CAST_IN_THE_LAST_EIGHT_SECONDS = id("inc_cast_speed_for_each_different_spell_you_ve_cast_in_the_last_eight_seconds");

    // 10% increased Cast Speed if you've Attacked [Recently]
    public static final Identifier INC_CAST_SPEED_IF_YOU_VE_ATTACKED_RECENTLY = id("inc_cast_speed_if_you_ve_attacked_recently");

    // 16% increased Cast Speed if you've dealt a [Critical|Critical Hit] [Recently|Recently]
    public static final Identifier INC_CAST_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY = id("inc_cast_speed_if_you_ve_dealt_a_critical_hit_recently");

    // 10% increased Cast Speed if you've used a [Flask|Mana Flask] [Recently]
    public static final Identifier INC_CAST_SPEED_IF_YOU_VE_USED_A_MANA_FLASK_RECENTLY = id("inc_cast_speed_if_you_ve_used_a_mana_flask_recently");

    // 6% increased Cast Speed per [Spell|Spell] Echoed [Recently], up to 30%
    public static final Identifier INC_CAST_SPEED_PER_SPELL_ECHOED_RECENTLY_UP_TO = id("inc_cast_speed_per_spell_echoed_recently_up_to");

    // 10% reduced Cast Speed when on Full Life
    public static final Identifier INC_CAST_SPEED_WHEN_ON_FULL_HEALTH = id("inc_cast_speed_when_on_full_health");

    // 20% increased Cast Speed when on [LowLife|Low Life]
    public static final Identifier INC_CAST_SPEED_WHEN_ON_LOW_HEALTH = id("inc_cast_speed_when_on_low_health");

    // 10% increased Cast Speed while Chilled
    public static final Identifier INC_CAST_SPEED_WHILE_CHILLED = id("inc_cast_speed_while_chilled");

    // 10% increased Cast Speed while Ignited
    public static final Identifier INC_CAST_SPEED_WHILE_IGNITED = id("inc_cast_speed_while_ignited");

    // 12% increased Cast Speed while on Full Mana
    public static final Identifier INC_CAST_SPEED_WHILE_ON_FULL_MANA = id("inc_cast_speed_while_on_full_mana");

    // 3% increased Cast Speed with [Cold] Skills
    public static final Identifier INC_CAST_SPEED_WITH_COLD_SKILLS = id("inc_cast_speed_with_cold_skills");

    // 25% increased Chance to [Block] if you've Blocked with a raised [Shield] [Recently]
    public static final Identifier INC_CHANCE_TO_BLOCK_IF_YOU_VE_BLOCKED_WITH_A_RAISED_SHIELD_RECENTLY = id("inc_chance_to_block_if_you_ve_blocked_with_a_raised_shield_recently");

    // 8% increased chance to inflict [Ailments]
    public static final Identifier INC_CHANCE_TO_INFLICT_AILMENTS = id("inc_chance_to_inflict_ailments");

    // 20% increased chance to inflict [Ailments|Ailments] against Enemies with [Exposure]
    public static final Identifier INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE = id("inc_chance_to_inflict_ailments_against_enemies_with_exposure");

    // 30% increased chance to inflict [Ailments] against [Rarity|Rare or Unique] Enemies
    public static final Identifier INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES = id("inc_chance_to_inflict_ailments_against_rare_or_unique_enemies");

    // 20% increased chance to inflict [Ailments|Ailments] with [Projectile|Projectiles]
    public static final Identifier INC_CHANCE_TO_INFLICT_AILMENTS_WITH_PROJECTILES = id("inc_chance_to_inflict_ailments_with_projectiles");

    // 12% increased chance to [Shock]
    public static final Identifier INC_CHANCE_TO_SHOCK = id("inc_chance_to_shock");

    // 7% increased [Chaos] Damage
    public static final Identifier INC_CHAOS_DAMAGE = id("inc_chaos_damage");

    // 40% increased [Chaos] Damage while affected by [Herald] of Plague
    public static final Identifier INC_CHAOS_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_PLAGUE = id("inc_chaos_damage_while_affected_by_herald_of_plague");

    // 10% increased [Charm] Charges gained
    public static final Identifier INC_CHARM_CHARGES_GAIN = id("inc_charm_charges_gain");

    // 6% reduced [Charm] Charges used
    public static final Identifier INC_CHARM_CHARGES_USED = id("inc_charm_charges_used");

    // 10% increased [Charm] Effect Duration
    public static final Identifier INC_CHARM_EFFECT_DURATION = id("inc_charm_effect_duration");

    // 15% increased [Chill] and [Freeze] Duration on Enemies
    public static final Identifier INC_CHILL_AND_FREEZE_DURATION_ON_ENEMIES = id("inc_chill_and_freeze_duration_on_enemies");

    // 15% increased [Chill|Chill] Duration on Enemies
    public static final Identifier INC_CHILL_DURATION_ON_ENEMIES = id("inc_chill_duration_on_enemies");

    // 12% increased [Cold] Damage
    public static final Identifier INC_COLD_DAMAGE = id("inc_cold_damage");

    // 40% increased [Cold] Damage while affected by [Herald] of Ice
    public static final Identifier INC_COLD_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ICE = id("inc_cold_damage_while_affected_by_herald_of_ice");

    // 5% increased [CooldownRecovery|Cooldown Recovery Rate]
    public static final Identifier INC_COOLDOWN_RECOVERY_RATE = id("inc_cooldown_recovery_rate");

    // 15% increased Cooldown Recovery Rate for [Grenade] Skills
    public static final Identifier INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS = id("inc_cooldown_recovery_rate_for_grenade_skills");

    // 15% increased Cost [Efficiency]
    public static final Identifier INC_COST_EFFICIENCY = id("inc_cost_efficiency");

    // 15% increased Cost [Efficiency] of [Attack|Attacks]
    public static final Identifier INC_COST_EFFICIENCY_OF_ATTACKS = id("inc_cost_efficiency_of_attacks");

    // 15% increased [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS = id("inc_critical_damage_bonus");

    // 100% increased [CriticalDamageBonus|Critical Damage Bonus] against Enemies that are on Full Life
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH = id("inc_critical_damage_bonus_against_enemies_that_are_on_full_health");

    // 50% increased [CriticalDamageBonus|Critical Damage Bonus] against Enemies that have exited your [Presence] [Recently]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_HAVE_EXITED_YOUR_PRESENCE_RECENTLY = id("inc_critical_damage_bonus_against_enemies_that_have_exited_your_presence_recently");

    // 15% increased [CriticalDamageBonus|Critical Damage Bonus] for [Attack] Damage
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE = id("inc_critical_damage_bonus_for_attack_damage");

    // 20% increased [CriticalDamageBonus|Critical Damage Bonus] if you haven't dealt a [Critical|Critical Hit] [Recently]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY = id("inc_critical_damage_bonus_if_you_haven_t_dealt_a_critical_hit_recently");

    // 20% increased [CriticalDamageBonus|Critical Damage Bonus] if you've consumed a [Charges|Power Charge] [Recently]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY = id("inc_critical_damage_bonus_if_you_ve_consumed_a_power_charge_recently");

    // 25% increased [CriticalDamageBonus|Critical Damage Bonus] if you've dealt a [Critical|Non-Critical Hit] [Recently]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY = id("inc_critical_damage_bonus_if_you_ve_dealt_a_non_critical_hit_recently");

    // 20% increased [CriticalDamageBonus|Critical Damage Bonus] if you've gained a [Charges|Power Charge] [Recently]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_GAIN_A_POWER_CHARGE_RECENTLY = id("inc_critical_damage_bonus_if_you_ve_gain_a_power_charge_recently");

    // 16% increased [CriticalDamageBonus|Critical Damage Bonus] with Bows
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_BOWS = id("inc_critical_damage_bonus_with_bows");

    // 80% increased [CriticalDamageBonus|Critical Damage Bonus] with [Crossbow|Crossbows]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_CROSSBOWS = id("inc_critical_damage_bonus_with_crossbows");

    // 10% increased [CriticalDamageBonus|Critical Damage Bonus] with [Dagger|Daggers]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS = id("inc_critical_damage_bonus_with_daggers");

    // 20% increased [CriticalDamageBonus|Critical Damage Bonus] with [Flail|Flails]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_FLAILS = id("inc_critical_damage_bonus_with_flails");

    // 40% increased [CriticalDamageBonus|Critical Damage Bonus] with One Handed [Melee] Weapons
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_ONE_HANDED_MELEE_WEAPONS = id("inc_critical_damage_bonus_with_one_handed_melee_weapons");

    // 18% increased [CriticalDamageBonus|Critical Damage Bonus] with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES = id("inc_critical_damage_bonus_with_quarterstaves");

    // 40% increased [CriticalDamageBonus|Critical Damage Bonus] with [Spear|Spears]
    public static final Identifier INC_CRITICAL_DAMAGE_BONUS_WITH_SPEARS = id("inc_critical_damage_bonus_with_spears");

    // 5% increased [Critical|Critical Hit Chance]
    public static final Identifier INC_CRITICAL_HIT_CHANCE = id("inc_critical_hit_chance");

    // 25% increased [Critical|Critical Hit Chance] against [Bleeding] Enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_BLEEDING_ENEMIES = id("inc_critical_hit_chance_against_bleeding_enemies");

    // 30% increased [Critical|Critical Hit] Chance against Blinded Enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES = id("inc_critical_hit_chance_against_blinded_enemies");

    // 25% increased [Critical] Hit Chance against [Daze|Dazed] Enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_DAZED_ENEMIES = id("inc_critical_hit_chance_against_dazed_enemies");

    // 35% increased [Critical|Critical Hit Chance] against Enemies that are affected by no Elemental [Ailments]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_AFFECTED_BY_NO_ELEMENTAL_AILMENTS = id("inc_critical_hit_chance_against_enemies_that_are_affected_by_no_elemental_ailments");

    // 50% increased [Critical|Critical Hit Chance] against Enemies that are on Full Life
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH = id("inc_critical_hit_chance_against_enemies_that_are_on_full_health");

    // 12% increased [Critical|Critical Hit] Chance against Enemies that have entered your [Presence] [Recently]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY = id("inc_critical_hit_chance_against_enemies_that_have_entered_your_presence_recently");

    // 15% increased [Critical|Critical Hit Chance] against enemies with [Exposure]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_WITH_EXPOSURE = id("inc_critical_hit_chance_against_enemies_with_exposure");

    // 10% increased [Critical|Critical Hit Chance] against Humanoids
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_HUMANOIDS = id("inc_critical_hit_chance_against_humanoids");

    // 35% increased [Critical|Critical Hit Chance] against [Immobilised] enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_IMMOBILISED_ENEMIES = id("inc_critical_hit_chance_against_immobilised_enemies");

    // 20% increased [Critical] Hit Chance against [Mark|Marked] Enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES = id("inc_critical_hit_chance_against_marked_enemies");

    // 15% increased [Critical|Critical Hit] Chance against [Shock|Shocked] Enemies
    public static final Identifier INC_CRITICAL_HIT_CHANCE_AGAINST_SHOCKED_ENEMIES = id("inc_critical_hit_chance_against_shocked_enemies");

    // 10% increased [Critical|Critical Hit Chance] for [Attack|Attacks]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS = id("inc_critical_hit_chance_for_attacks");

    // 10% increased [Critical|Critical Hit Chance] for [Spell|Spells]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_FOR_SPELLS = id("inc_critical_hit_chance_for_spells");

    // 16% increased [Critical|Critical Hit Chance] if you haven't dealt a Critical Hit [Recently]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY = id("inc_critical_hit_chance_if_you_haven_t_dealt_a_critical_hit_recently");

    // 10% increased [Critical|Critical Hit Chance] if you have Killed [Recently]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY = id("inc_critical_hit_chance_if_you_have_killed_recently");

    // 30% increased [Critical|Critical Hit] Chance if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY = id("inc_critical_hit_chance_if_you_have_shapeshifted_to_an_animal_form_recently");

    // 25% increased [Critical|Critical Hit] Chance if you've [Trigger|Triggered] a Skill [Recently]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY = id("inc_critical_hit_chance_if_you_ve_triggered_a_skill_recently");

    // 20% increased [Critical|Critical Hit] Chance while [Shapeshift|Shapeshifted]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED = id("inc_critical_hit_chance_while_shapeshifted");

    // 10% increased [Critical|Critical Hit Chance] with [Crossbow|Crossbows]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS = id("inc_critical_hit_chance_with_crossbows");

    // 10% increased [Critical|Critical Hit Chance] with [Dagger|Daggers]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS = id("inc_critical_hit_chance_with_daggers");

    // 10% increased [Critical|Critical Hit Chance] with [Flail|Flails]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_FLAILS = id("inc_critical_hit_chance_with_flails");

    // 10% increased [Critical|Critical Hit Chance] with One Handed [Melee] Weapons
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_ONE_HANDED_MELEE_WEAPONS = id("inc_critical_hit_chance_with_one_handed_melee_weapons");

    // 10% increased [Critical|Critical Hit Chance] with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_QUARTERSTAVES = id("inc_critical_hit_chance_with_quarterstaves");

    // 10% increased [Critical|Critical Hit Chance] with [Spear|Spears]
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_SPEARS = id("inc_critical_hit_chance_with_spears");

    // 10% increased [Critical|Critical Hit Chance] with Traps
    public static final Identifier INC_CRITICAL_HIT_CHANCE_WITH_TRAPS = id("inc_critical_hit_chance_with_traps");

    // 15% increased [CriticalDamageBonus|Critical Spell Damage Bonus]
    public static final Identifier INC_CRITICAL_SPELL_DAMAGE_BONUS = id("inc_critical_spell_damage_bonus");

    // 15% increased [Crossbow|Crossbow] Reload Speed
    public static final Identifier INC_CROSSBOW_RELOAD_SPEED = id("inc_crossbow_reload_speed");

    // 5% increased [CullingStrike|Culling Strike] Threshold
    public static final Identifier INC_CULLING_STRIKE_THRESHOLD = id("inc_culling_strike_threshold");

    // 40% increased [CullingStrike|Culling Strike] Threshold against [Immobilised] Enemies
    public static final Identifier INC_CULLING_STRIKE_THRESHOLD_AGAINST_IMMOBILISED_ENEMIES = id("inc_culling_strike_threshold_against_immobilised_enemies");

    // 40% increased [CullingStrike|Culling Strike] Threshold against [Rarity|Rare or Unique] Enemies
    public static final Identifier INC_CULLING_STRIKE_THRESHOLD_AGAINST_RARE_OR_UNIQUE_ENEMIES = id("inc_culling_strike_threshold_against_rare_or_unique_enemies");

    // 18% increased [Curse] [BuffMagnitude|Magnitudes]
    public static final Identifier INC_CURSE_BUFF_MAGNITUDE = id("inc_curse_buff_magnitude");

    // 25% reduced [Curse] Duration
    public static final Identifier INC_CURSE_DURATION = id("inc_curse_duration");

    // 10% increased Damage
    public static final Identifier INC_DAMAGE = id("inc_damage");

    // 30% increased Damage against [Daze|Dazed] Enemies
    public static final Identifier INC_DAMAGE_AGAINST_DAZED_ENEMIES = id("inc_damage_against_dazed_enemies");

    // 10% increased Damage against Demons
    public static final Identifier INC_DAMAGE_AGAINST_DEMONS = id("inc_damage_against_demons");

    // 20% increased Damage against Enemies with [ArmourBreak|Fully Broken Armour]
    public static final Identifier INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR = id("inc_damage_against_enemies_with_fully_broken_armor");

    // 10% increased Damage against [Immobilised] Enemies
    public static final Identifier INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES = id("inc_damage_against_immobilised_enemies");

    // 50% increased Damage against [Immobilised] Enemies while [Shapeshift|Shapeshifted]
    public static final Identifier INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED = id("inc_damage_against_immobilised_enemies_while_shapeshifted");

    // 30% increased damage against Undead Enemies
    public static final Identifier INC_DAMAGE_AGAINST_UNDEAD_ENEMIES = id("inc_damage_against_undead_enemies");

    // 25% increased Damage during any [Flask] Effect
    public static final Identifier INC_DAMAGE_DURING_ANY_FLASK_EFFECT = id("inc_damage_during_any_flask_effect");

    // 20% increased Damage for each different [Warcry|Warcry] you've used [Recently|Recently]
    public static final Identifier INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY = id("inc_damage_for_each_different_warcry_you_ve_used_recently");

    // 10% increased Damage for each [Hazard] triggered [Recently], up to 50%
    public static final Identifier INC_DAMAGE_FOR_EACH_HAZARD_TRIGGERED_RECENTLY_UP_TO = id("inc_damage_for_each_hazard_triggered_recently_up_to");

    // 8% increased Damage for each time you've [Warcry|Warcried] [Recently|Recently]
    public static final Identifier INC_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY = id("inc_damage_for_each_time_you_ve_warcried_recently");

    // 20% increased Damage for each type of [ElementalAilments|Elemental Ailment] on Enemy
    public static final Identifier INC_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY = id("inc_damage_for_each_type_of_elemental_ailment_on_enemy");

    // 15% increased Damage if you have Consumed a [Corpse] [Recently]
    public static final Identifier INC_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY = id("inc_damage_if_you_have_consumed_a_corpse_recently");

    // 30% increased Damage if you've consumed a [Charges|Frenzy Charge] [Recently]
    public static final Identifier INC_DAMAGE_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY = id("inc_damage_if_you_ve_consumed_a_frenzy_charge_recently");

    // 25% increased Damage if you've dealt a [Critical|Critical Hit] in the past 8 seconds
    public static final Identifier INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_SECONDS = id("inc_damage_if_you_ve_dealt_a_critical_hit_in_the_past_seconds");

    // 10% increased Damage if you've dealt a [Critical|Critical Hit] [Recently]
    public static final Identifier INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY = id("inc_damage_if_you_ve_dealt_a_critical_hit_recently");

    // 40% increased Damage if you've [Trigger|Triggered] a Skill [Recently]
    public static final Identifier INC_DAMAGE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY = id("inc_damage_if_you_ve_triggered_a_skill_recently");

    // 1% increased Damage per 1% Chance to [Block|Block]
    public static final Identifier INC_DAMAGE_PER_CHANCE_TO_BLOCK = id("inc_damage_per_chance_to_block");

    // 30% increased Damage per [Curse] on you
    public static final Identifier INC_DAMAGE_PER_CURSE_ON_YOU = id("inc_damage_per_curse_on_you");

    // 2% increased Damage per 5 of your lowest [Attributes|Attribute]
    public static final Identifier INC_DAMAGE_PER_OF_YOUR_LOWEST_ATTRIBUTES = id("inc_damage_per_of_your_lowest_attributes");

    // 1% increased Damage per 15 [Strength]
    public static final Identifier INC_DAMAGE_PER_STRENGTH = id("inc_damage_per_strength");

    // 5% increased Damage taken while on [LowLife|Low Life]
    public static final Identifier INC_DAMAGE_TAKEN_WHILE_ON_LOW_HEALTH = id("inc_damage_taken_while_on_low_health");

    // 12% increased Damage while affected by a Herald
    public static final Identifier INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD = id("inc_damage_while_affected_by_a_herald");

    // 20% increased Damage while [Leech|Leeching]
    public static final Identifier INC_DAMAGE_WHILE_LEECHING = id("inc_damage_while_leeching");

    // 40% increased Damage while Leeching Life
    public static final Identifier INC_DAMAGE_WHILE_LEECHING_HEALTH = id("inc_damage_while_leeching_health");

    // 12% increased Damage while [Shapeshift|Shapeshifted]
    public static final Identifier INC_DAMAGE_WHILE_SHAPESHIFTED = id("inc_damage_while_shapeshifted");

    // 10% increased Damage while your [Companion] is in your [Presence]
    public static final Identifier INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE = id("inc_damage_while_your_companion_is_in_your_presence");

    // 30% increased Damage while you have an active [Charm|Charm]
    public static final Identifier INC_DAMAGE_WHILE_YOU_HAVE_AN_ACTIVE_CHARM = id("inc_damage_while_you_have_an_active_charm");

    // 25% increased Damage while you have a [Totem|Totem]
    public static final Identifier INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM = id("inc_damage_while_you_have_a_totem");

    // 12% increased Damage with [Axe|Axes]
    public static final Identifier INC_DAMAGE_WITH_AXES = id("inc_damage_with_axes");

    // 12% increased Damage with Bows
    public static final Identifier INC_DAMAGE_WITH_BOWS = id("inc_damage_with_bows");

    // 20% increased Damage with [Crossbow|Crossbows]
    public static final Identifier INC_DAMAGE_WITH_CROSSBOWS = id("inc_damage_with_crossbows");

    // 25% increased Damage with [Crossbow|Crossbows] for each type of [Ammunition|Ammunition] fired in the past 10 seconds
    public static final Identifier INC_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_SECONDS = id("inc_damage_with_crossbows_for_each_type_of_ammunition_fired_in_the_past_seconds");

    // 10% increased Damage with [Dagger|Daggers]
    public static final Identifier INC_DAMAGE_WITH_DAGGERS = id("inc_damage_with_daggers");

    // 10% increased Damage with [Flail|Flails]
    public static final Identifier INC_DAMAGE_WITH_FLAILS = id("inc_damage_with_flails");

    // 30% increased Damage with Hits against Chilled Enemies
    public static final Identifier INC_DAMAGE_WITH_HITS_AGAINST_CHILLED_ENEMIES = id("inc_damage_with_hits_against_chilled_enemies");

    // 50% increased Damage with Hits against [Frozen] Enemies
    public static final Identifier INC_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES = id("inc_damage_with_hits_against_frozen_enemies");

    // 30% increased Damage with Hits against Shocked Enemies
    public static final Identifier INC_DAMAGE_WITH_HITS_AGAINST_SHOCKED_ENEMIES = id("inc_damage_with_hits_against_shocked_enemies");

    // 25% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES = id("inc_damage_with_hit_damage_against_blinded_enemies");

    // 14% increased Damage with [HitDamage|Hits] against [Burning|Burning] Enemies
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES = id("inc_damage_with_hit_damage_against_burning_enemies");

    // 33% increased Damage with [HitDamage|Hits] against Enemies affected by [Ailments]
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_AILMENTS = id("inc_damage_with_hit_damage_against_enemies_affected_by_ailments");

    // 24% increased Damage with [HitDamage|Hits] against Enemies affected by [ElementalAilments|Elemental Ailments]
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS = id("inc_damage_with_hit_damage_against_enemies_affected_by_elemental_ailments");

    // 20% increased Damage with [HitDamage|Hits] against Enemies that are on Full Life
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH = id("inc_damage_with_hit_damage_against_enemies_that_are_on_full_health");

    // 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH = id("inc_damage_with_hit_damage_against_enemies_that_are_on_low_health");

    // 25% increased Damage with [HitDamage|Hits] against [Hinder|Hindered] Enemies
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_HINDERED_ENEMIES = id("inc_damage_with_hit_damage_against_hindered_enemies");

    // 40% increased Damage with [HitDamage|Hits] against [Ignite|Ignited] Enemies
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES = id("inc_damage_with_hit_damage_against_ignited_enemies");

    // 25% increased Damage with [HitDamage|Hits] against [Rarity|Rare and Unique] Enemies
    public static final Identifier INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_RARE_AND_UNIQUE_ENEMIES = id("inc_damage_with_hit_damage_against_rare_and_unique_enemies");

    // 15% increased Damage with [Mace|Maces]
    public static final Identifier INC_DAMAGE_WITH_MACES = id("inc_damage_with_maces");

    // 10% increased Damage with One Handed Weapons
    public static final Identifier INC_DAMAGE_WITH_ONE_HANDED_WEAPONS = id("inc_damage_with_one_handed_weapons");

    // 12% increased Damage with [Plant|Plant] Skills
    public static final Identifier INC_DAMAGE_WITH_PLANT_SKILLS = id("inc_damage_with_plant_skills");

    // 10% increased Damage with [Spear|Spears]
    public static final Identifier INC_DAMAGE_WITH_SPEARS = id("inc_damage_with_spears");

    // 25% increased Damage with Swords
    public static final Identifier INC_DAMAGE_WITH_SWORDS = id("inc_damage_with_swords");

    // 10% increased Damage with Two Handed Weapons
    public static final Identifier INC_DAMAGE_WITH_TWO_HANDED_WEAPONS = id("inc_damage_with_two_handed_weapons");

    // 16% increased Damage with [Warcry|Warcries]
    public static final Identifier INC_DAMAGE_WITH_WARCRIES = id("inc_damage_with_warcries");

    // 25% increased [Daze] Duration
    public static final Identifier INC_DAZE_DURATION = id("inc_daze_duration");

    // 10% increased [Deflect|Deflection Rating]
    public static final Identifier INC_DEFLECTION_RATING = id("inc_deflection_rating");

    // 20% increased [Deflect|Deflection Rating] while moving
    public static final Identifier INC_DEFLECTION_RATING_WHILE_MOVING = id("inc_deflection_rating_while_moving");

    // 10% increased [Deflect|Deflection Rating] while [Surrounded]
    public static final Identifier INC_DEFLECTION_RATING_WHILE_SURROUNDED = id("inc_deflection_rating_while_surrounded");

    // 5% increased [Dexterity]
    public static final Identifier INC_DEXTERITY = id("inc_dexterity");

    // 15% increased Duration of [Ailments|Ailments] against Enemies with [Exposure]
    public static final Identifier INC_DURATION_OF_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE = id("inc_duration_of_ailments_against_enemies_with_exposure");

    // 10% increased Duration of [Ailments|Ailments] on Beasts
    public static final Identifier INC_DURATION_OF_AILMENTS_ON_BEASTS = id("inc_duration_of_ailments_on_beasts");

    // 10% reduced Duration of [Ailments|Ailments] on You
    public static final Identifier INC_DURATION_OF_AILMENTS_ON_YOU = id("inc_duration_of_ailments_on_you");

    // 20% increased duration of [Ailments] you inflict against [Curse|Cursed] Enemies
    public static final Identifier INC_DURATION_OF_AILMENTS_YOU_INFLICT_AGAINST_CURSED_ENEMIES = id("inc_duration_of_ailments_you_inflict_against_cursed_enemies");

    // 40% reduced Duration of [Bleeding] on You
    public static final Identifier INC_DURATION_OF_BLEEDING_ON_YOU = id("inc_duration_of_bleeding_on_you");

    // 20% increased Duration of [DamagingAilments|Damaging Ailments] on Enemies
    public static final Identifier INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES = id("inc_duration_of_damaging_ailments_on_enemies");

    // 25% increased Duration of each [PuppetMaster|Puppet Master] stack
    public static final Identifier INC_DURATION_OF_EACH_PUPPET_MASTER_STACK = id("inc_duration_of_each_puppet_master_stack");

    // 10% increased Duration of Elemental [Ailments] on Enemies
    public static final Identifier INC_DURATION_OF_ELEMENTAL_AILMENTS_ON_ENEMIES = id("inc_duration_of_elemental_ailments_on_enemies");

    // 10% increased Duration of [Ignite], [Shock] and [Chill] on Enemies
    public static final Identifier INC_DURATION_OF_IGNITE_SHOCK_AND_CHILL_ON_ENEMIES = id("inc_duration_of_ignite_shock_and_chill_on_enemies");

    // 40% increased Duration of [Poison|Poisons] you inflict against [Slow|Slowed] Enemies
    public static final Identifier INC_DURATION_OF_POISONS_YOU_INFLICT_AGAINST_SLOWED_ENEMIES = id("inc_duration_of_poisons_you_inflict_against_slowed_enemies");

    // 10% increased effect of [Archon] [Buff|Buffs] on you
    public static final Identifier INC_EFFECT_OF_ARCHON_BUFFS_ON_YOU = id("inc_effect_of_archon_buffs_on_you");

    // 30% reduced Effect of [Chill|Chill] on you
    public static final Identifier INC_EFFECT_OF_CHILL_ON_YOU = id("inc_effect_of_chill_on_you");

    // 30% increased effect of [ArmourBreak|Fully Broken Armour]
    public static final Identifier INC_EFFECT_OF_FULLY_BROKEN_ARMOR = id("inc_effect_of_fully_broken_armor");

    // 50% increased effect of [Incision]
    public static final Identifier INC_EFFECT_OF_INCISION = id("inc_effect_of_incision");

    // 35% reduced Effect of [NonDamagingAilments|Non-Damaging Ailments] on you
    public static final Identifier INC_EFFECT_OF_NON_DAMAGING_AILMENTS_ON_YOU = id("inc_effect_of_non_damaging_ailments_on_you");

    // 20% increased Effect of [PuppetMaster|Puppet Master]
    public static final Identifier INC_EFFECT_OF_PUPPET_MASTER = id("inc_effect_of_puppet_master");

    // 10% increased Effect of your [Mark] Skills
    public static final Identifier INC_EFFECT_OF_YOUR_MARK_SKILLS = id("inc_effect_of_your_mark_skills");

    // 15% increased [Electrocute|Electrocute Buildup]
    public static final Identifier INC_ELECTROCUTE_BUILDUP = id("inc_electrocute_buildup");

    // 50% increased [Electrocute|Electrocute Buildup] against [Shock|Shocked] Enemies
    public static final Identifier INC_ELECTROCUTE_BUILDUP_AGAINST_SHOCKED_ENEMIES = id("inc_electrocute_buildup_against_shocked_enemies");

    // 40% increased [ElementalAilments|Elemental] [AilmentApplication|Ailment Application] if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
    public static final Identifier INC_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY = id("inc_elemental_ailments_ailment_application_if_you_have_shapeshifted_to_an_animal_form_recently");

    // 21% increased [AilmentThreshold|Elemental Ailment Threshold]
    public static final Identifier INC_ELEMENTAL_AILMENT_THRESHOLD = id("inc_elemental_ailment_threshold");

    // 10% increased [ElementalDamage|Elemental Damage]
    public static final Identifier INC_ELEMENTAL_DAMAGE = id("inc_elemental_damage");

    // 30% increased [ElementalDamage|Elemental] Damage if you've [Chill|Chilled] an Enemy [Recently]
    public static final Identifier INC_ELEMENTAL_DAMAGE_IF_YOU_VE_CHILLED_AN_ENEMY_RECENTLY = id("inc_elemental_damage_if_you_ve_chilled_an_enemy_recently");

    // 30% increased [ElementalDamage|Elemental] Damage if you've [Ignite|Ignited] an Enemy [Recently]
    public static final Identifier INC_ELEMENTAL_DAMAGE_IF_YOU_VE_IGNITED_AN_ENEMY_RECENTLY = id("inc_elemental_damage_if_you_ve_ignited_an_enemy_recently");

    // 30% increased [ElementalDamage|Elemental] Damage if you've [Shock|Shocked] an Enemy [Recently]
    public static final Identifier INC_ELEMENTAL_DAMAGE_IF_YOU_VE_SHOCKED_AN_ENEMY_RECENTLY = id("inc_elemental_damage_if_you_ve_shocked_an_enemy_recently");

    // 12% increased [ElementalDamage|Elemental] Damage while [Shapeshift|Shapeshifted]
    public static final Identifier INC_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED = id("inc_elemental_damage_while_shapeshifted");

    // 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
    public static final Identifier INC_ELEMENTAL_DAMAGE_WITH_ATTACKS = id("inc_elemental_damage_with_attacks");

    // 40% increased Elemental Damage with [Attack] Skills during any [Flask] Effect
    public static final Identifier INC_ELEMENTAL_DAMAGE_WITH_ATTACK_SKILLS_DURING_ANY_FLASK_EFFECT = id("inc_elemental_damage_with_attack_skills_during_any_flask_effect");

    // 10% increased [ElementalInfusion|Elemental Infusion] duration
    public static final Identifier INC_ELEMENTAL_INFUSION_DURATION = id("inc_elemental_infusion_duration");

    // 10% increased [Charges|Endurance Charge] Duration
    public static final Identifier INC_ENDURANCE_CHARGE_DURATION = id("inc_endurance_charge_duration");

    // 15% increased [Charges|Endurance, Frenzy and Power Charge] Duration
    public static final Identifier INC_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION = id("inc_endurance_frenzy_and_power_charge_duration");

    // 60% increased [EnergyShield|Energy Shield] from Equipped Body Armour
    public static final Identifier INC_ENERGY_SHIELD_FROM_EQUIPPED_BODY_ARMOR = id("inc_energy_shield_from_equipped_body_armor");

    // 60% increased [EnergyShield|Energy Shield] from Equipped [Focus]
    public static final Identifier INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS = id("inc_energy_shield_from_equipped_focus");

    // 70% increased [EnergyShield|Energy Shield] from Equipped Helmet
    public static final Identifier INC_ENERGY_SHIELD_FROM_EQUIPPED_HELMET = id("inc_energy_shield_from_equipped_helmet");

    // 12% increased [ESRechargeRate|Energy Shield Recharge Rate]
    public static final Identifier INC_ENERGY_SHIELD_RECHARGE_RATE = id("inc_energy_shield_recharge_rate");

    // 15% increased [ESRechargeRate|Energy Shield Recharge Rate] while [Shapeshift|Shapeshifted]
    public static final Identifier INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED = id("inc_energy_shield_recharge_rate_while_shapeshifted");

    // 15% increased [Evasion|Evasion Rating]
    public static final Identifier INC_EVASION = id("inc_evasion");

    // 100% increased [Evasion] Rating from Equipped Body Armour
    public static final Identifier INC_EVASION_FROM_EQUIPPED_BODY_ARMOR = id("inc_evasion_from_equipped_body_armor");

    // 50% increased [Evasion|Evasion Rating] if [ESRecharge|Energy Shield Recharge] has started in the past 2 seconds
    public static final Identifier INC_EVASION_IF_ENERGY_SHIELD_RECHARGE_HAS_STARTED_IN_THE_PAST_SECONDS = id("inc_evasion_if_energy_shield_recharge_has_started_in_the_past_seconds");

    // 30% reduced [Evasion] Rating if you haven't been [HitDamage|Hit] [Recently]
    public static final Identifier INC_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY = id("inc_evasion_if_you_haven_t_been_hit_damage_recently");

    // 40% increased [Evasion|Evasion Rating] if you have been [HitDamage|Hit] [Recently|Recently]
    public static final Identifier INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY = id("inc_evasion_if_you_have_been_hit_damage_recently");

    // 60% increased [Evasion|Evasion Rating] if you have Hit an Enemy [Recently]
    public static final Identifier INC_EVASION_IF_YOU_HAVE_HIT_AN_ENEMY_RECENTLY = id("inc_evasion_if_you_have_hit_an_enemy_recently");

    // 50% increased [Evasion|Evasion Rating] if you've consumed a [Charges|Frenzy Charge] [Recently|Recently]
    public static final Identifier INC_EVASION_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY = id("inc_evasion_if_you_ve_consumed_a_frenzy_charge_recently");

    // 40% increased [Evasion|Evasion Rating] if you've Dodge Rolled [Recently]
    public static final Identifier INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY = id("inc_evasion_if_you_ve_dodge_rolled_recently");

    // 3% increased [Evasion] Rating per 10 [Intelligence]
    public static final Identifier INC_EVASION_PER_INTELLIGENCE = id("inc_evasion_per_intelligence");

    // 50% increased [Evasion|Evasion Rating] when on Full Life
    public static final Identifier INC_EVASION_WHEN_ON_FULL_HEALTH = id("inc_evasion_when_on_full_health");

    // 20% increased [Evasion] Rating while moving
    public static final Identifier INC_EVASION_WHILE_MOVING = id("inc_evasion_while_moving");

    // 25% increased [Evasion|Evasion Rating] while [Parry|Parrying]
    public static final Identifier INC_EVASION_WHILE_PARRYING = id("inc_evasion_while_parrying");

    // 15% increased [Evasion|Evasion Rating] while Sprinting
    public static final Identifier INC_EVASION_WHILE_SPRINTING = id("inc_evasion_while_sprinting");

    // 20% increased [Evasion|Evasion Rating] while [Surrounded|Surrounded]
    public static final Identifier INC_EVASION_WHILE_SURROUNDED = id("inc_evasion_while_surrounded");

    // 30% increased [Evasion|Evasion Rating] while you have [EnergyShield|Energy Shield]
    public static final Identifier INC_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD = id("inc_evasion_while_you_have_energy_shield");

    // 7% increased [Exposure] Effect
    public static final Identifier INC_EXPOSURE_EFFECT = id("inc_exposure_effect");

    // 10% increased [Fire] Damage
    public static final Identifier INC_FIRE_DAMAGE = id("inc_fire_damage");

    // 3% increased [Fire] Damage per [Charges|Endurance Charge] consumed [Recently]
    public static final Identifier INC_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY = id("inc_fire_damage_per_endurance_charge_consumed_recently");

    // 40% increased [Fire] Damage while affected by [Herald] of Ash
    public static final Identifier INC_FIRE_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ASH = id("inc_fire_damage_while_affected_by_herald_of_ash");

    // 50% increased [Fire] Damage while [Ignite|Ignited]
    public static final Identifier INC_FIRE_DAMAGE_WHILE_IGNITED = id("inc_fire_damage_while_ignited");

    // 80% increased [Flammability] [BuffMagnitude|Magnitude]
    public static final Identifier INC_FLAMMABILITY_BUFF_MAGNITUDE = id("inc_flammability_buff_magnitude");

    // 8% increased [Flask|Flask] and [Charm] Charges gained
    public static final Identifier INC_FLASK_AND_CHARM_CHARGES_GAIN = id("inc_flask_and_charm_charges_gain");

    // 10% increased [Flask|Flask] Charges gained
    public static final Identifier INC_FLASK_CHARGES_GAIN = id("inc_flask_charges_gain");

    // 5% reduced [Flask|Flask] Charges used
    public static final Identifier INC_FLASK_CHARGES_USED = id("inc_flask_charges_used");

    // 4% reduced [Flask] Charges used from Mana [Flask|Flasks]
    public static final Identifier INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS = id("inc_flask_charges_used_from_mana_flasks");

    // 10% increased [Flask|Flask] Effect Duration
    public static final Identifier INC_FLASK_EFFECT_DURATION = id("inc_flask_effect_duration");

    // 40% increased [Flask] Life Recovery rate
    public static final Identifier INC_FLASK_HEALTH_RECOVERY_RATE = id("inc_flask_health_recovery_rate");

    // 15% increased [Freeze|Freeze] Buildup
    public static final Identifier INC_FREEZE_BUILDUP = id("inc_freeze_buildup");

    // 20% increased [Freeze] Buildup with [Empowered] [Attack|Attacks]
    public static final Identifier INC_FREEZE_BUILDUP_WITH_EMPOWERED_ATTACKS = id("inc_freeze_buildup_with_empowered_attacks");

    // 30% increased [Freeze] Buildup with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_FREEZE_BUILDUP_WITH_QUARTERSTAVES = id("inc_freeze_buildup_with_quarterstaves");

    // 20% increased [Freeze] Duration on Enemies
    public static final Identifier INC_FREEZE_DURATION_ON_ENEMIES = id("inc_freeze_duration_on_enemies");

    // 25% reduced [Freeze|Freeze] Duration on you
    public static final Identifier INC_FREEZE_DURATION_ON_YOU = id("inc_freeze_duration_on_you");

    // 15% increased [FreezeThreshold|Freeze Threshold]
    public static final Identifier INC_FREEZE_THRESHOLD = id("inc_freeze_threshold");

    // 20% increased [Charges|Frenzy Charge] Duration
    public static final Identifier INC_FRENZY_CHARGE_DURATION = id("inc_frenzy_charge_duration");

    // 15% increased [Glory] generation
    public static final Identifier INC_GLORY_GENERATION = id("inc_glory_generation");

    // 20% increased [Glory] generation for [Banner] Skills
    public static final Identifier INC_GLORY_GENERATION_FOR_BANNER_SKILLS = id("inc_glory_generation_for_banner_skills");

    // 10% increased [Grenade] Area of Effect
    public static final Identifier INC_GRENADE_AREA_OF_EFFECT = id("inc_grenade_area_of_effect");

    // 12% increased [Grenade] Damage
    public static final Identifier INC_GRENADE_DAMAGE = id("inc_grenade_damage");

    // 15% reduced [Grenade] [DetonationTime|Detonation Time]
    public static final Identifier INC_GRENADE_DETONATION_TIME = id("inc_grenade_detonation_time");

    // 10% increased [Hazard] Area of Effect
    public static final Identifier INC_HAZARD_AREA_OF_EFFECT = id("inc_hazard_area_of_effect");

    // 16% increased [Hazard] Damage
    public static final Identifier INC_HAZARD_DAMAGE = id("inc_hazard_damage");

    // 30% increased [Hazard] Duration
    public static final Identifier INC_HAZARD_DURATION = id("inc_hazard_duration");

    // 20% increased [Hazard] [Immobilised|Immobilisation] buildup
    public static final Identifier INC_HAZARD_IMMOBILISATION_BUILDUP = id("inc_hazard_immobilisation_buildup");

    // 10% increased Life and Mana Recovery from [Flask|Flasks]
    public static final Identifier INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS = id("inc_health_and_mana_recovery_from_flasks");

    // 40% increased Life and Mana Recovery from [Flask|Flasks] while you have an active [Charm|Charm]
    public static final Identifier INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS_WHILE_YOU_HAVE_AN_ACTIVE_CHARM = id("inc_health_and_mana_recovery_from_flasks_while_you_have_an_active_charm");

    // 5% increased Life and Mana Regeneration Rate for each [Minion] in your [Presence], up to a maximum of 40%
    public static final Identifier INC_HEALTH_AND_MANA_REGENERATION_RATE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF = id("inc_health_and_mana_regeneration_rate_for_each_minion_in_your_presence_up_to_a_max_of");

    // 15% increased Life Cost of Skills
    public static final Identifier INC_HEALTH_COST_OF_SKILLS = id("inc_health_cost_of_skills");

    // 15% increased Life [Flask|Flask] Charges gained
    public static final Identifier INC_HEALTH_FLASK_CHARGES_GAIN = id("inc_health_flask_charges_gain");

    // 10% increased Life Recovery from [Flask|Flasks]
    public static final Identifier INC_HEALTH_RECOVERY_FROM_FLASKS = id("inc_health_recovery_from_flasks");

    // 40% increased Life Recovery from [Flask|Flasks] used when on [LowLife|Low Life]
    public static final Identifier INC_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH = id("inc_health_recovery_from_flasks_used_when_on_low_health");

    // 15% increased Life Recovery rate
    public static final Identifier INC_HEALTH_RECOVERY_RATE = id("inc_health_recovery_rate");

    // 20% increased Life Recovery Rate while your [Companion] is in your [Presence]
    public static final Identifier INC_HEALTH_RECOVERY_RATE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE = id("inc_health_recovery_rate_while_your_companion_is_in_your_presence");

    // 10% increased Life Regeneration rate
    public static final Identifier INC_HEALTH_REGENERATION_RATE = id("inc_health_regeneration_rate");

    // 30% increased Life Regeneration rate during Effect of any Life [Flask]
    public static final Identifier INC_HEALTH_REGENERATION_RATE_DURING_EFFECT_OF_ANY_HEALTH_FLASK = id("inc_health_regeneration_rate_during_effect_of_any_health_flask");

    // 30% increased Life Regeneration rate while [Ignite|Ignited]
    public static final Identifier INC_HEALTH_REGENERATION_RATE_WHILE_IGNITED = id("inc_health_regeneration_rate_while_ignited");

    // 20% increased Life Regeneration Rate while moving
    public static final Identifier INC_HEALTH_REGENERATION_RATE_WHILE_MOVING = id("inc_health_regeneration_rate_while_moving");

    // 15% increased Life Regeneration Rate while on [LowLife|Low Life]
    public static final Identifier INC_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH = id("inc_health_regeneration_rate_while_on_low_health");

    // 15% increased Life Regeneration rate while [Shapeshift|Shapeshifted]
    public static final Identifier INC_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED = id("inc_health_regeneration_rate_while_shapeshifted");

    // 30% increased Life Regeneration Rate while stationary
    public static final Identifier INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY = id("inc_health_regeneration_rate_while_stationary");

    // 20% increased [Hinder] Duration
    public static final Identifier INC_HINDER_DURATION = id("inc_hinder_duration");

    // 60% reduced [IceCrystals|Ice Crystal] Life
    public static final Identifier INC_ICE_CRYSTAL_HEALTH = id("inc_ice_crystal_health");

    // 15% increased [Ignite|Ignite] [BuffMagnitude|Magnitude]
    public static final Identifier INC_IGNITE_BUFF_MAGNITUDE = id("inc_ignite_buff_magnitude");

    // 8% increased Ignite Duration on Enemies
    public static final Identifier INC_IGNITE_DURATION_ON_ENEMIES = id("inc_ignite_duration_on_enemies");

    // 25% reduced [Ignite|Ignite] Duration on you
    public static final Identifier INC_IGNITE_DURATION_ON_YOU = id("inc_ignite_duration_on_you");

    // 12% increased [Immobilised|Immobilisation] buildup
    public static final Identifier INC_IMMOBILISATION_BUILDUP = id("inc_immobilisation_buildup");

    // 10% increased [Immobilised|Immobilisation] buildup against Constructs
    public static final Identifier INC_IMMOBILISATION_BUILDUP_AGAINST_CONSTRUCTS = id("inc_immobilisation_buildup_against_constructs");

    // 6% increased [Intelligence]
    public static final Identifier INC_INTELLIGENCE = id("inc_intelligence");

    // 40% increased [JaggedGround|Jagged Ground] Duration
    public static final Identifier INC_JAGGED_GROUND_DURATION = id("inc_jagged_ground_duration");

    // 20% increased [Knockback] Distance
    public static final Identifier INC_KNOCKBACK_DISTANCE = id("inc_knockback_distance");

    // 20% increased [Lightning] Damage
    public static final Identifier INC_LIGHTNING_DAMAGE = id("inc_lightning_damage");

    // 40% increased [Lightning] Damage while affected by [Herald] of Thunder
    public static final Identifier INC_LIGHTNING_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_THUNDER = id("inc_lightning_damage_while_affected_by_herald_of_thunder");

    // 10% increased [LightRadius|Light Radius]
    public static final Identifier INC_LIGHT_RADIUS = id("inc_light_radius");

    // 15% increased Mana Cost [Efficiency]
    public static final Identifier INC_MANA_COST_EFFICIENCY = id("inc_mana_cost_efficiency");

    // 30% increased Mana Cost [Efficiency] of [Attack|Attacks] during any Mana [Flask] Effect
    public static final Identifier INC_MANA_COST_EFFICIENCY_OF_ATTACKS_DURING_ANY_MANA_FLASK_EFFECT = id("inc_mana_cost_efficiency_of_attacks_during_any_mana_flask_effect");

    // 15% increased Mana Cost [Efficiency] of [Command] Skills
    public static final Identifier INC_MANA_COST_EFFICIENCY_OF_COMMAND_SKILLS = id("inc_mana_cost_efficiency_of_command_skills");

    // 60% increased Mana Cost [Efficiency] of [Mark|Marks]
    public static final Identifier INC_MANA_COST_EFFICIENCY_OF_MARKS = id("inc_mana_cost_efficiency_of_marks");

    // 25% increased Mana Cost [Efficiency] while on [LowMana|Low Mana]
    public static final Identifier INC_MANA_COST_EFFICIENCY_WHILE_ON_LOW_MANA = id("inc_mana_cost_efficiency_while_on_low_mana");

    // 15% increased Mana Cost of Skills
    public static final Identifier INC_MANA_COST_OF_SKILLS = id("inc_mana_cost_of_skills");

    // 15% increased Mana [Flask|Flask] Charges gained
    public static final Identifier INC_MANA_FLASK_CHARGES_GAIN = id("inc_mana_flask_charges_gain");

    // 10% increased Mana Recovery from [Flask|Flasks]
    public static final Identifier INC_MANA_RECOVERY_FROM_FLASKS = id("inc_mana_recovery_from_flasks");

    // 15% increased Mana Recovery rate
    public static final Identifier INC_MANA_RECOVERY_RATE = id("inc_mana_recovery_rate");

    // 10% increased Mana Recovery Rate during Effect of any Mana [Flask]
    public static final Identifier INC_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK = id("inc_mana_recovery_rate_during_effect_of_any_mana_flask");

    // 20% increased Mana Regeneration Rate
    public static final Identifier INC_MANA_REGENERATION_RATE = id("inc_mana_regeneration_rate");

    // 25% increased Mana Regeneration Rate if you have [Shock|Shocked] an Enemy [Recently]
    public static final Identifier INC_MANA_REGENERATION_RATE_IF_YOU_HAVE_SHOCKED_AN_ENEMY_RECENTLY = id("inc_mana_regeneration_rate_if_you_have_shocked_an_enemy_recently");

    // 50% increased Mana Regeneration Rate while moving
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_MOVING = id("inc_mana_regeneration_rate_while_moving");

    // 16% increased Mana Regeneration Rate while not on [LowMana|Low Mana]
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_NOT_ON_LOW_MANA = id("inc_mana_regeneration_rate_while_not_on_low_mana");

    // 40% increased Mana Regeneration Rate while on Full Life
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_ON_FULL_HEALTH = id("inc_mana_regeneration_rate_while_on_full_health");

    // 20% increased Mana Regeneration Rate while [Shapeshift|Shapeshifted]
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_SHAPESHIFTED = id("inc_mana_regeneration_rate_while_shapeshifted");

    // 30% increased Mana Regeneration Rate while [Shock|Shocked]
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_SHOCKED = id("inc_mana_regeneration_rate_while_shocked");

    // 35% increased Mana Regeneration Rate while stationary
    public static final Identifier INC_MANA_REGENERATION_RATE_WHILE_STATIONARY = id("inc_mana_regeneration_rate_while_stationary");

    // 12% increased maximum [EnergyShield|Energy Shield]
    public static final Identifier INC_MAX_ENERGY_SHIELD = id("inc_max_energy_shield");

    // 10% increased maximum [EnergyShield|Energy Shield] if you've consumed a [Charges|Power Charge] [Recently|Recently]
    public static final Identifier INC_MAX_ENERGY_SHIELD_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY = id("inc_max_energy_shield_if_you_ve_consumed_a_power_charge_recently");

    // 3% increased maximum Life
    public static final Identifier INC_MAX_HEALTH = id("inc_max_health");

    // 5% increased Maximum Life if you have at least 10 Red [SupportGem|Support Gems] Socketed
    public static final Identifier INC_MAX_HEALTH_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED = id("inc_max_health_if_you_have_at_least_red_support_gems_socketed");

    // 3% increased maximum Life, Mana and [EnergyShield|Energy Shield]
    public static final Identifier INC_MAX_HEALTH_MANA_AND_ENERGY_SHIELD = id("inc_max_health_mana_and_energy_shield");

    // 10% reduced maximum Mana
    public static final Identifier INC_MAX_MANA = id("inc_max_mana");

    // 5% increased Maximum Mana if you have at least 10 Blue [SupportGem|Support Gems] Socketed
    public static final Identifier INC_MAX_MANA_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED = id("inc_max_mana_if_you_have_at_least_blue_support_gems_socketed");

    // 3% increased [Melee] [Attack] Speed
    public static final Identifier INC_MELEE_ATTACK_SPEED = id("inc_melee_attack_speed");

    // 10% increased [Melee] [Critical|Critical Hit Chance]
    public static final Identifier INC_MELEE_CRITICAL_HIT_CHANCE = id("inc_melee_critical_hit_chance");

    // 10% increased [Melee] Damage
    public static final Identifier INC_MELEE_DAMAGE = id("inc_melee_damage");

    // 20% increased [Melee] Damage against [HeavyStun|Heavy Stunned] enemies
    public static final Identifier INC_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES = id("inc_melee_damage_against_heavy_stunned_enemies");

    // 50% increased [Melee|Melee Damage] against [Immobilised] Enemies
    public static final Identifier INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES = id("inc_melee_damage_against_immobilised_enemies");

    // 15% increased [Melee] Damage if you've dealt a [Projectile] [Attack] [HitDamage|Hit] in the past eight seconds
    public static final Identifier INC_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS = id("inc_melee_damage_if_you_ve_dealt_a_projectile_attack_hit_damage_in_the_past_eight_seconds");

    // 30% increased [Melee] Damage when on Full Life
    public static final Identifier INC_MELEE_DAMAGE_WHEN_ON_FULL_HEALTH = id("inc_melee_damage_when_on_full_health");

    // 15% increased [Melee] Damage with [HitDamage|Hits] at [CloseRange|Close Range]
    public static final Identifier INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE = id("inc_melee_damage_with_hit_damage_at_close_range");

    // 75% increased [Melee] Damage with [Spear|Spears] while [Surrounded]
    public static final Identifier INC_MELEE_DAMAGE_WITH_SPEARS_WHILE_SURROUNDED = id("inc_melee_damage_with_spears_while_surrounded");

    // 50% increased [Minion] Damage while you have at least two different active [Offering|Offerings]
    public static final Identifier INC_MINION_DAMAGE_WHILE_YOU_HAVE_AT_LEAST_TWO_DIFFERENT_ACTIVE_OFFERINGS = id("inc_minion_damage_while_you_have_at_least_two_different_active_offerings");

    // 8% increased Minion Duration
    public static final Identifier INC_MINION_DURATION = id("inc_minion_duration");

    // 3% increased Movement Speed
    public static final Identifier INC_MOVEMENT_SPEED = id("inc_movement_speed");

    // 1% increased Movement Speed for each time you've Blocked in the past 10 seconds
    public static final Identifier INC_MOVEMENT_SPEED_FOR_EACH_TIME_YOU_VE_BLOCKED_IN_THE_PAST_SECONDS = id("inc_movement_speed_for_each_time_you_ve_blocked_in_the_past_seconds");

    // 5% increased Movement Speed if you have at least 10 Green [SupportGem|Support Gems] Socketed
    public static final Identifier INC_MOVEMENT_SPEED_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED = id("inc_movement_speed_if_you_have_at_least_green_support_gems_socketed");

    // 4% increased Movement Speed if you've Killed [Recently]
    public static final Identifier INC_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY = id("inc_movement_speed_if_you_ve_killed_recently");

    // 5% increased Movement Speed if you've [Pinned] an Enemy [Recently]
    public static final Identifier INC_MOVEMENT_SPEED_IF_YOU_VE_PINNED_AN_ENEMY_RECENTLY = id("inc_movement_speed_if_you_ve_pinned_an_enemy_recently");

    // 6% increased Movement Speed if you've successfully [Parry|Parried] [Recently]
    public static final Identifier INC_MOVEMENT_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY = id("inc_movement_speed_if_you_ve_successfully_parried_recently");

    // 4% increased Movement Speed if you've used a [Mark] [Recently]
    public static final Identifier INC_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY = id("inc_movement_speed_if_you_ve_used_a_mark_recently");

    // 5% reduced Movement Speed Penalty from using Cold Skills while moving
    public static final Identifier INC_MOVEMENT_SPEED_PENALTY_FROM_USING_COLD_SKILLS_WHILE_MOVING = id("inc_movement_speed_penalty_from_using_cold_skills_while_moving");

    // 5% reduced Movement Speed Penalty from using Fire Skills while moving
    public static final Identifier INC_MOVEMENT_SPEED_PENALTY_FROM_USING_FIRE_SKILLS_WHILE_MOVING = id("inc_movement_speed_penalty_from_using_fire_skills_while_moving");

    // 5% reduced Movement Speed Penalty from using Skills while moving
    public static final Identifier INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING = id("inc_movement_speed_penalty_from_using_skills_while_moving");

    // 5% reduced Movement Speed Penalty while Actively [Block|Blocking]
    public static final Identifier INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING = id("inc_movement_speed_penalty_while_actively_blocking");

    // 10% reduced [MovementSpeed|Movement Speed] Penalty with [Command] Skills（Puppet Master 每层）
    public static final Identifier REDUCED_MOVEMENT_SPEED_PENALTY_WITH_COMMAND_SKILLS = id("reduced_movement_speed_penalty_with_command_skills");

    // 3% increased Movement Speed while [DualWield|Dual Wielding]
    public static final Identifier INC_MOVEMENT_SPEED_WHILE_DUAL_WIELDING = id("inc_movement_speed_while_dual_wielding");

    // 3% increased Movement Speed while Sprinting
    public static final Identifier INC_MOVEMENT_SPEED_WHILE_SPRINTING = id("inc_movement_speed_while_sprinting");

    // 10% increased Movement Speed while [Surrounded]
    public static final Identifier INC_MOVEMENT_SPEED_WHILE_SURROUNDED = id("inc_movement_speed_while_surrounded");

    // 6% increased Movement Speed while you have an active [Charm|Charm]
    public static final Identifier INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_AN_ACTIVE_CHARM = id("inc_movement_speed_while_you_have_an_active_charm");

    // 3% increased Movement Speed while you have [EnergyShield|Energy Shield]
    public static final Identifier INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_ENERGY_SHIELD = id("inc_movement_speed_while_you_have_energy_shield");

    // 15% increased [ParriedDebuff|Parried Debuff] [BuffMagnitude|Magnitude]
    public static final Identifier INC_PARRIED_DEBUFF_BUFF_MAGNITUDE = id("inc_parried_debuff_buff_magnitude");

    // 20% increased [ParriedDebuff|Parried Debuff] Duration
    public static final Identifier INC_PARRIED_DEBUFF_DURATION = id("inc_parried_debuff_duration");

    // 20% increased [Parry] Damage
    public static final Identifier INC_PARRY_DAMAGE = id("inc_parry_damage");

    // 8% increased [Parry] [HitDamage|Hit] Area of Effect
    public static final Identifier INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT = id("inc_parry_hit_damage_area_of_effect");

    // 30% increased [Parry] Range
    public static final Identifier INC_PARRY_RANGE = id("inc_parry_range");

    // 30% increased penalty to [Accuracy] Rating at range
    public static final Identifier INC_PENALTY_TO_ACCURACY_AT_RANGE = id("inc_penalty_to_accuracy_at_range");

    // 10% increased [Physical] Damage
    public static final Identifier INC_PHYSICAL_DAMAGE = id("inc_physical_damage");

    // 40% increased [Physical] Damage while affected by [Herald] of Blood
    public static final Identifier INC_PHYSICAL_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_BLOOD = id("inc_physical_damage_while_affected_by_herald_of_blood");

    // 30% increased [Physical] Damage while [Shapeshift|Shapeshifted]
    public static final Identifier INC_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED = id("inc_physical_damage_while_shapeshifted");

    // 20% increased Physical Damage with Bows
    public static final Identifier INC_PHYSICAL_DAMAGE_WITH_BOWS = id("inc_physical_damage_with_bows");

    // 15% increased [Pinned|Pin] Buildup
    public static final Identifier INC_PINNED_BUILDUP = id("inc_pinned_buildup");

    // 15% increased [Pinned|Pin] duration
    public static final Identifier INC_PINNED_DURATION = id("inc_pinned_duration");

    // 10% increased [Poison] Duration
    public static final Identifier INC_POISON_DURATION = id("inc_poison_duration");

    // 10% increased [Poison] Duration for each [Poison] you have inflicted Recently, up to a maximum of 100%
    public static final Identifier INC_POISON_DURATION_FOR_EACH_POISON_YOU_HAVE_INFLICTED_RECENTLY_UP_TO_A_MAX_OF = id("inc_poison_duration_for_each_poison_you_have_inflicted_recently_up_to_a_max_of");

    // 10% reduced [Poison|Poison] Duration on you
    public static final Identifier INC_POISON_DURATION_ON_YOU = id("inc_poison_duration_on_you");

    // 10% increased [Charges|Power Charge] Duration
    public static final Identifier INC_POWER_CHARGE_DURATION = id("inc_power_charge_duration");

    // 10% increased [Presence|Presence] Area of Effect
    public static final Identifier INC_PRESENCE_AREA_OF_EFFECT = id("inc_presence_area_of_effect");

    // 10% increased [Projectile] Damage
    public static final Identifier INC_PROJECTILE_DAMAGE = id("inc_projectile_damage");

    // 15% increased [Projectile] Damage if you've dealt a [Melee] [HitDamage|Hit] in the past eight seconds
    public static final Identifier INC_PROJECTILE_DAMAGE_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS = id("inc_projectile_damage_if_you_ve_dealt_a_melee_hit_damage_in_the_past_eight_seconds");

    // 40% increased Projectile Damage with [Spear|Spears] while there are no Enemies within 3m
    public static final Identifier INC_PROJECTILE_DAMAGE_WITH_SPEARS_WHILE_THERE_ARE_NO_ENEMIES_WITHIN_XM = id("inc_projectile_damage_with_spears_while_there_are_no_enemies_within_xm");

    // 10% increased [Projectile] Speed
    public static final Identifier INC_PROJECTILE_SPEED = id("inc_projectile_speed");

    // 5% reduced [Projectile] Speed for [Spell] Skills
    public static final Identifier INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS = id("inc_projectile_speed_for_spell_skills");

    // 40% increased [Projectile|Projectile] [Stun|Stun Buildup]
    public static final Identifier INC_PROJECTILE_STUN_BUILDUP = id("inc_projectile_stun_buildup");

    // 8% increased [Reservation] [Efficiency] of [Companion] Skills
    public static final Identifier INC_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS = id("inc_reservation_efficiency_of_companion_skills");

    // 6% increased [Reservation] [Efficiency] of [Herald] Skills
    public static final Identifier INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS = id("inc_reservation_efficiency_of_herald_skills");

    // 40% increased [Reservation] [Efficiency] of [Minion] Skills
    public static final Identifier INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS = id("inc_reservation_efficiency_of_minion_skills");

    // 30% increased [Shock|Shock] Chance against [Electrocute|Electrocuted] Enemies
    public static final Identifier INC_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES = id("inc_shock_chance_against_electrocuted_enemies");

    // 15% increased [Shock] Duration
    public static final Identifier INC_SHOCK_DURATION = id("inc_shock_duration");

    // 25% reduced [Shock|Shock] duration on you
    public static final Identifier INC_SHOCK_DURATION_ON_YOU = id("inc_shock_duration_on_you");

    // 10% increased Skill Effect Duration
    public static final Identifier INC_SKILL_EFFECT_DURATION = id("inc_skill_effect_duration");

    // 8% increased Skill Effect Duration per Enemy you've [Freeze|Frozen] in the last 8 seconds, up to 40%
    public static final Identifier INC_SKILL_EFFECT_DURATION_PER_ENEMY_YOU_VE_FREEZE_IN_THE_LAST_SECONDS_UP_TO = id("inc_skill_effect_duration_per_enemy_you_ve_freeze_in_the_last_seconds_up_to");

    // 3% reduced [SkillSpeed|Skill Speed]
    public static final Identifier INC_SKILL_SPEED = id("inc_skill_speed");

    // 10% increased [SkillSpeed|Skill Speed] if you've consumed a [Charges|Frenzy Charge] [Recently]
    public static final Identifier INC_SKILL_SPEED_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY = id("inc_skill_speed_if_you_ve_consumed_a_frenzy_charge_recently");

    // 8% increased [SkillSpeed|Skill Speed] while [Shapeshift|Shapeshifted]
    public static final Identifier INC_SKILL_SPEED_WHILE_SHAPESHIFTED = id("inc_skill_speed_while_shapeshifted");

    // 2% increased [SkillSpeed|Skill Speed] with [Channelling] Skills
    public static final Identifier INC_SKILL_SPEED_WITH_CHANNELLING_SKILLS = id("inc_skill_speed_with_channelling_skills");

    // 10% increased [SkillSpeed|Skill Speed] with [Command] Skills（Puppet Master 每层）
    public static final Identifier INC_SKILL_SPEED_WITH_COMMAND_SKILLS = id("inc_skill_speed_with_command_skills");

    // 5% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
    public static final Identifier INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU = id("inc_slowing_potency_of_debuffs_on_you");

    // 8% increased speed of [Recoup] Effects
    public static final Identifier INC_SPEED_OF_RECOUP_EFFECTS = id("inc_speed_of_recoup_effects");

    // 10% increased [Spell] Area Damage
    public static final Identifier INC_SPELL_AREA_DAMAGE = id("inc_spell_area_damage");

    // 8% increased [Spell] Damage
    public static final Identifier INC_SPELL_DAMAGE = id("inc_spell_damage");

    // 12% increased [Spell] Damage for each different Non-Instant [Attack] you've used in the past 8 seconds
    public static final Identifier INC_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK_YOU_VE_USED_IN_THE_PAST_SECONDS = id("inc_spell_damage_for_each_different_non_instant_attack_you_ve_used_in_the_past_seconds");

    // 40% increased [Spell] Damage if one of your [Minion|Minions] has died [Recently]
    public static final Identifier INC_SPELL_DAMAGE_IF_ONE_OF_YOUR_MINIONS_HAS_DIED_RECENTLY = id("inc_spell_damage_if_one_of_your_minions_has_died_recently");

    // 15% increased [Spell] Damage if you have consumed an [ElementalInfusion|Elemental Infusion] [Recently]
    public static final Identifier INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY = id("inc_spell_damage_if_you_have_consumed_an_elemental_infusion_recently");

    // 12% increased [Spell] Damage if you have [Shapeshift|Shapeshifted] to Human form [Recently]
    public static final Identifier INC_SPELL_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_HUMAN_FORM_RECENTLY = id("inc_spell_damage_if_you_have_shapeshifted_to_human_form_recently");

    // 3% increased [Spell] Damage per 100 maximum Mana
    public static final Identifier INC_SPELL_DAMAGE_PER_MAX_MANA = id("inc_spell_damage_per_max_mana");

    // 2% increased Spell Damage per 10 [Strength]
    public static final Identifier INC_SPELL_DAMAGE_PER_STRENGTH = id("inc_spell_damage_per_strength");

    // 25% increased [Spell] Damage while on Full [EnergyShield|Energy Shield]
    public static final Identifier INC_SPELL_DAMAGE_WHILE_ON_FULL_ENERGY_SHIELD = id("inc_spell_damage_while_on_full_energy_shield");

    // 12% increased [Spell] Damage while wielding a [Melee] Weapon
    public static final Identifier INC_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON = id("inc_spell_damage_while_wielding_a_melee_weapon");

    // 15% increased Spell Damage while you have [ArcaneSurge|Arcane Surge]
    public static final Identifier INC_SPELL_DAMAGE_WHILE_YOU_HAVE_ARCANE_SURGE = id("inc_spell_damage_while_you_have_arcane_surge");

    // 40% increased [Spell] Damage with [Spell|Spells] that cost Life
    public static final Identifier INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH = id("inc_spell_damage_with_spells_that_cost_health");

    // 4% increased [Spirit]
    public static final Identifier INC_SPIRIT = id("inc_spirit");

    // 20% reduced [Spirit] [Reservation] [Efficiency] of Skills
    public static final Identifier INC_SPIRIT_RESERVATION_EFFICIENCY_OF_SKILLS = id("inc_spirit_reservation_efficiency_of_skills");

    // 5% increased [Strength]
    public static final Identifier INC_STRENGTH = id("inc_strength");

    // 10% increased [Stun] Buildup
    public static final Identifier INC_STUN_BUILDUP = id("inc_stun_buildup");

    // 30% increased [Stun|Stun] Buildup against Enemies that are on [LowLife|Low Life]
    public static final Identifier INC_STUN_BUILDUP_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH = id("inc_stun_buildup_against_enemies_that_are_on_low_health");

    // 40% increased [Stun] Buildup against enemies within 2 metres
    public static final Identifier INC_STUN_BUILDUP_AGAINST_ENEMIES_WITHIN_METRES = id("inc_stun_buildup_against_enemies_within_metres");

    // 40% increased [Stun] buildup if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
    public static final Identifier INC_STUN_BUILDUP_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY = id("inc_stun_buildup_if_you_have_shapeshifted_to_an_animal_form_recently");

    // 25% increased [Stun] buildup while [Shapeshift|Shapeshifted]
    public static final Identifier INC_STUN_BUILDUP_WHILE_SHAPESHIFTED = id("inc_stun_buildup_while_shapeshifted");

    // 18% increased [Stun|Stun Buildup] with [Mace|Maces]
    public static final Identifier INC_STUN_BUILDUP_WITH_MACES = id("inc_stun_buildup_with_maces");

    // 15% increased [Stun] Buildup with [Melee|Melee Damage]
    public static final Identifier INC_STUN_BUILDUP_WITH_MELEE_DAMAGE = id("inc_stun_buildup_with_melee_damage");

    // 20% increased [Stun] Buildup with [Quarterstaff|Quarterstaves]
    public static final Identifier INC_STUN_BUILDUP_WITH_QUARTERSTAVES = id("inc_stun_buildup_with_quarterstaves");

    // 40% increased [StunRecovery|Stun Recovery]
    public static final Identifier INC_STUN_RECOVERY = id("inc_stun_recovery");

    // 12% increased [StunThreshold|Stun Threshold]
    public static final Identifier INC_STUN_THRESHOLD = id("inc_stun_threshold");

    // 100% increased [StunThreshold|Stun Threshold] during [Empowered] [Attack|Attacks]
    public static final Identifier INC_STUN_THRESHOLD_DURING_EMPOWERED_ATTACKS = id("inc_stun_threshold_during_empowered_attacks");

    // 10% increased [StunThreshold|Stun Threshold] for each time you've been [HitDamage|Hit] by an Enemy [Recently], up to 100%
    public static final Identifier INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_HIT_DAMAGE_BY_AN_ENEMY_RECENTLY_UP_TO = id("inc_stun_threshold_for_each_time_you_ve_been_hit_damage_by_an_enemy_recently_up_to");

    // 100% increased [StunThreshold|Stun Threshold] for each time you've been [Stun|Stunned] [Recently]
    public static final Identifier INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_STUNNED_RECENTLY = id("inc_stun_threshold_for_each_time_you_ve_been_stunned_recently");

    // 20% increased [StunThreshold|Stun Threshold] if you haven't been [Stun|Stunned] [Recently]
    public static final Identifier INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY = id("inc_stun_threshold_if_you_haven_t_been_stunned_recently");

    // 60% increased [StunThreshold|Stun Threshold] while [Channelling]
    public static final Identifier INC_STUN_THRESHOLD_WHILE_CHANNELLING = id("inc_stun_threshold_while_channelling");

    // 20% increased [StunThreshold|Stun Threshold] while on Full Life
    public static final Identifier INC_STUN_THRESHOLD_WHILE_ON_FULL_HEALTH = id("inc_stun_threshold_while_on_full_health");

    // 20% increased [StunThreshold|Stun Threshold] while [Parry|Parrying]
    public static final Identifier INC_STUN_THRESHOLD_WHILE_PARRYING = id("inc_stun_threshold_while_parrying");

    // 25% increased [StunThreshold|Stun Threshold] while [Shapeshift|Shapeshifted]
    public static final Identifier INC_STUN_THRESHOLD_WHILE_SHAPESHIFTED = id("inc_stun_threshold_while_shapeshifted");

    // 50% increased [Surrounded] Area of Effect
    public static final Identifier INC_SURROUNDED_AREA_OF_EFFECT = id("inc_surrounded_area_of_effect");

    // 30% increased [Thorns|Thorns] [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier INC_THORNS_CRITICAL_DAMAGE_BONUS = id("inc_thorns_critical_damage_bonus");

    // 16% increased [Thorns|Thorns] damage
    public static final Identifier INC_THORNS_DAMAGE = id("inc_thorns_damage");

    // 75% increased [Thorns|Thorns] damage if you've [Block|Blocked] [Recently|Recently]
    public static final Identifier INC_THORNS_DAMAGE_IF_YOU_VE_BLOCKED_RECENTLY = id("inc_thorns_damage_if_you_ve_blocked_recently");

    // 5% increased total Power counted by Warcries
    public static final Identifier INC_TOTAL_POWER_COUNTED_BY_WARCRIES = id("inc_total_power_counted_by_warcries");

    // 15% increased [Totem|Totem] Damage
    public static final Identifier INC_TOTEM_DAMAGE = id("inc_totem_damage");

    // 30% increased [Totem] Duration
    public static final Identifier INC_TOTEM_DURATION = id("inc_totem_duration");

    // 16% increased [Totem] Life
    public static final Identifier INC_TOTEM_HEALTH = id("inc_totem_health");

    // 20% increased [Totem|Totem] Placement range
    public static final Identifier INC_TOTEM_PLACEMENT_RANGE = id("inc_totem_placement_range");

    // 20% increased [Totem|Totem] Placement speed
    public static final Identifier INC_TOTEM_PLACEMENT_SPEED = id("inc_totem_placement_speed");

    // 10% increased [Trap] Damage
    public static final Identifier INC_TRAP_DAMAGE = id("inc_trap_damage");

    // 6% increased Trap Throwing Speed
    public static final Identifier INC_TRAP_THROWING_SPEED = id("inc_trap_throwing_speed");

    // 15% reduced [Volatility] Explosion delay
    public static final Identifier INC_VOLATILITY_EXPLOSION_DELAY = id("inc_volatility_explosion_delay");

    // 10% increased [Warcry|Warcry] Cooldown Recovery Rate
    public static final Identifier INC_WARCRY_COOLDOWN_RECOVERY_RATE = id("inc_warcry_cooldown_recovery_rate");

    // 16% increased [Warcry|Warcry] Speed
    public static final Identifier INC_WARCRY_SPEED = id("inc_warcry_speed");

    // 10% increased [Withered|Withered] [BuffMagnitude|Magnitude]
    public static final Identifier INC_WITHERED_BUFF_MAGNITUDE = id("inc_withered_buff_magnitude");

    // Inherent Life granted by [Strength] is halved
    public static final Identifier INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED = id("inherent_health_granted_by_strength_is_halved");

    // Inherent loss of [Rage|Rage] is 15% slower
    public static final Identifier INHERENT_LOSS_OF_RAGE_IS_SLOWER = id("inherent_loss_of_rage_is_slower");

    // Inherent [Rage|Rage] loss starts 1 second later
    public static final Identifier INHERENT_RAGE_LOSS_STARTS_SECOND_LATER = id("inherent_rage_loss_starts_second_later");

    // [Invoke|Invocated] skills have 30% increased Maximum [Energy]
    public static final Identifier INVOKED_SKILLS_HAVE_INC_MAX_ENERGY = id("invoked_skills_have_inc_max_energy");

    // [Invoke|Invocated] [Spell|Spells] deal 15% increased Damage
    public static final Identifier INVOKED_SPELLS_DEAL_INC_DAMAGE = id("invoked_spells_deal_inc_damage");

    // [Invoke|Invocated] [Spell|Spells] have 40% chance to consume half as much [Energy]
    public static final Identifier INVOKED_SPELLS_HAVE_CHANCE_TO_CONSUME_HALF_AS_MUCH_ENERGY = id("invoked_spells_have_chance_to_consume_half_as_much_energy");

    // [Invoke|Invocated] [Spell|Spells] have 30% increased [Critical|Critical Hit Chance]
    public static final Identifier INVOKED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE = id("invoked_spells_have_inc_critical_hit_chance");

    // [Invoke|Invocation] Skills cannot gain [Energy] while [Trigger|Triggering] [Spell|Spells]
    public static final Identifier INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS = id("invocation_skills_cannot_gain_energy_while_triggering_spells");

    // [Invoke|Invocation] Skills instead [Trigger] [Spell|Spells] every 2 seconds
    public static final Identifier INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_SECONDS = id("invocation_skills_instead_trigger_spells_every_seconds");

    // [Invoke|Invocation] [Spell|Spells] have 20% increased [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS = id("invocation_spells_have_inc_critical_damage_bonus");

    // [Invoke|Invoked] Spells consume 50% less [Energy]
    public static final Identifier INVOKED_SPELLS_CONSUME_MORE_ENERGY = id("invoked_spells_consume_more_energy");

    // [Knockback|Knocks Back] Enemies if you get a [Critical|Critical Hit] with a [Quarterstaff|Quarterstaff]
    public static final Identifier KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF = id("knocks_back_enemies_if_you_get_a_critical_hit_with_a_quarterstaff");

    // [LifeLeech|Leeching Life] from your [HitDamage|Hits] causes your [Companion] to also Leech the [LeechSameAmount|same amount] of Life
    public static final Identifier LEECHING_HEALTH_FROM_YOUR_HIT_DAMAGE_CAUSES_YOUR_COMPANION_TO_ALSO_LEECH_THE_SAME_AMOUNT_OF_HEALTH = id("leeching_health_from_your_hit_damage_causes_your_companion_to_also_leech_the_same_amount_of_health");

    // [LifeLeech|Leech Life] 67% less quickly
    public static final Identifier LEECH_HEALTH_MORE_QUICKLY = id("leech_health_more_quickly");

    // Mana Costs are Doubled
    public static final Identifier MANA_COSTS_ARE_DOUBLED = id("mana_costs_are_doubled");

    // Mana [Flask|Flasks] gain 0.1 charges per Second
    public static final Identifier MANA_FLASKS_GAIN_CHARGES_PER_SECOND = id("mana_flasks_gain_charges_per_second");

    // 5 Mana gained when you [Block|Block]
    public static final Identifier MANA_GAIN_WHEN_YOU_BLOCK = id("mana_gain_when_you_block");

    // [Mark] Skills have 25% increased Skill Effect Duration
    public static final Identifier MARK_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION = id("mark_skills_have_inc_skill_effect_duration");

    // [Mark] Skills have 10% increased Use Speed
    public static final Identifier MARK_SKILLS_HAVE_INC_USE_SPEED = id("mark_skills_have_inc_use_speed");

    // Maximum Life is 1
    public static final Identifier MAX_HEALTH_IS = id("max_health_is");

    // +15 maximum [Rage] if you've used a Skill that Requires [Glory] in the past 20 seconds
    public static final Identifier MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS = id("max_rage_if_you_ve_used_a_skill_that_requires_glory_in_the_past_seconds");

    // +1 maximum stacks of [PuppetMaster|Puppet Master]
    public static final Identifier MAX_STACKS_OF_PUPPET_MASTER = id("max_stacks_of_puppet_master");

    // Maximum [Volatility] is 30
    public static final Identifier MAX_VOLATILITY_IS = id("max_volatility_is");

    // [Melee] [Attack] Skills have +1 to maximum number of Summoned [Totem|Totems]
    public static final Identifier MELEE_ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_TOTEMS = id("melee_attack_skills_have_to_max_number_of_summoned_totems");

    // [Meta] Skills gain 8% increased [Energy]
    public static final Identifier META_SKILLS_GAIN_INC_ENERGY = id("meta_skills_gain_inc_energy");

    // [Meta] Skills gain 25% increased [Energy] if you've dealt a [Critical] [HitDamage|Hit] [Recently]
    public static final Identifier META_SKILLS_GAIN_INC_ENERGY_IF_YOU_VE_DEALT_A_CRITICAL_HIT_DAMAGE_RECENTLY = id("meta_skills_gain_inc_energy_if_you_ve_dealt_a_critical_hit_damage_recently");

    // [Meta] Skills have 20% increased [Reservation] [Efficiency]
    public static final Identifier META_SKILLS_HAVE_INC_RESERVATION_EFFICIENCY = id("meta_skills_have_inc_reservation_efficiency");

    // +0.5 metres to Dodge Roll distance while [Surrounded]
    public static final Identifier METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED = id("metres_to_dodge_roll_distance_while_surrounded");

    // +0.2 metres to [Melee] [Strike] Range
    public static final Identifier METRES_TO_MELEE_STRIKE_RANGE = id("metres_to_melee_strike_range");

    // +0.4 metres to [Melee] [Strike] Range if you've dealt a [Projectile] [Attack] [HitDamage|Hit] in the past eight seconds
    public static final Identifier METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS = id("metres_to_melee_strike_range_if_you_ve_dealt_a_projectile_attack_hit_damage_in_the_past_eight_seconds");

    // [Minion|Minions] [ArmourBreak|Break Armour] equal to 3% of [Physical] damage dealt
    public static final Identifier MINIONS_BREAK_ARMOR_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT = id("minions_break_armor_equal_to_of_physical_damage_dealt");

    // [Minion|Minions] cause 15% increased [Stun] Buildup
    public static final Identifier MINIONS_CAUSE_INC_STUN_BUILDUP = id("minions_cause_inc_stun_buildup");

    // [Minion|Minions] deal 8% increased Damage
    public static final Identifier MINIONS_DEAL_INC_DAMAGE = id("minions_deal_inc_damage");

    // [Minion|Minions] deal 30% increased Damage if you've [HitDamage|Hit] [Recently]
    public static final Identifier MINIONS_DEAL_INC_DAMAGE_IF_YOU_VE_HIT_DAMAGE_RECENTLY = id("minions_deal_inc_damage_if_you_ve_hit_damage_recently");

    // [Minion|Minions] deal 100% increased Damage with [Command] Skills
    public static final Identifier MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS = id("minions_deal_inc_damage_with_command_skills");

    // [Minion|Minions] deal 10% increased Damage with [Command] Skills for each different type of [Persistent] [Minion] in your [Presence]
    public static final Identifier MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS_FOR_EACH_DIFFERENT_TYPE_OF_PERSISTENT_MINION_IN_YOUR_PRESENCE = id("minions_deal_inc_damage_with_command_skills_for_each_different_type_of_persistent_minion_in_your_presence");

    // [Minion|Minions] [Gain] 20% of Elemental Damage as Extra Chaos Damage
    public static final Identifier MINIONS_GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("minions_gain_of_elemental_damage_as_extra_chaos_damage");

    // [Minion|Minions] gain 10% of [Physical|Physical] Damage as [Chaos|Chaos] Damage
    public static final Identifier MINIONS_GAIN_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE = id("minions_gain_of_physical_damage_as_chaos_damage");

    // [Minion|Minions] gain 15% of their maximum Life as Extra maximum [EnergyShield|Energy Shield]
    public static final Identifier MINIONS_GAIN_OF_THEIR_MAX_HEALTH_AS_EXTRA_MAX_ENERGY_SHIELD = id("minions_gain_of_their_max_health_as_extra_max_energy_shield");

    // [Minion|Minions] have 8% additional [Physical] Damage Reduction
    public static final Identifier MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION = id("minions_have_additional_physical_damage_reduction");

    // [Minion|Minions] have 10% chance to inflict [Withered] on [HitDamage|Hit]
    public static final Identifier MINIONS_HAVE_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE = id("minions_have_chance_to_inflict_withered_on_hit_damage");

    // [Minion|Minions] have 8% increased Area of Effect
    public static final Identifier MINIONS_HAVE_INC_AREA_OF_EFFECT = id("minions_have_inc_area_of_effect");

    // [Minion|Minions] have 3% increased [Attack] and Cast Speed
    public static final Identifier MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED = id("minions_have_inc_attack_and_cast_speed");

    // Minions have 3% increased [Attack] Speed
    public static final Identifier MINIONS_HAVE_INC_ATTACK_SPEED = id("minions_have_inc_attack_speed");

    // Minions have 15% reduced Cast Speed
    public static final Identifier MINIONS_HAVE_INC_CAST_SPEED = id("minions_have_inc_cast_speed");

    // Minions have 20% increased Cooldown Recovery Rate
    public static final Identifier MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE = id("minions_have_inc_cooldown_recovery_rate");

    // [Minion|Minions] have 20% increased [CooldownRecovery|Cooldown Recovery Rate] for [Command] Skills
    public static final Identifier MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS = id("minions_have_inc_cooldown_recovery_rate_for_command_skills");

    // [Minion|Minions] have 40% increased [CriticalDamageBonus|Critical Damage Bonus]
    public static final Identifier MINIONS_HAVE_INC_CRITICAL_DAMAGE_BONUS = id("minions_have_inc_critical_damage_bonus");

    // [Minion|Minions] have 20% increased [Critical|Critical Hit Chance]
    public static final Identifier MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE = id("minions_have_inc_critical_hit_chance");

    // [Minion|Minions] have 25% increased [Evasion] Rating
    public static final Identifier MINIONS_HAVE_INC_EVASION = id("minions_have_inc_evasion");

    // Minions have 10% reduced Life Recovery rate
    public static final Identifier MINIONS_HAVE_INC_HEALTH_RECOVERY_RATE = id("minions_have_inc_health_recovery_rate");

    // [Minion|Minions] have 8% increased maximum Life
    public static final Identifier MINIONS_HAVE_INC_MAX_HEALTH = id("minions_have_inc_max_health");

    // [Minion|Minions] have 10% increased Movement Speed
    public static final Identifier MINIONS_HAVE_INC_MOVEMENT_SPEED = id("minions_have_inc_movement_speed");

    // [Minion|Minions] have +20% to all Elemental [Resistances]
    public static final Identifier MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES = id("minions_have_to_all_elemental_resistances");

    // [Minion|Minions] have +5% to all [MaximumResistances|Maximum Elemental Resistances]
    public static final Identifier MINIONS_HAVE_TO_ALL_MAX_ELEMENTAL_RESISTANCES = id("minions_have_to_all_max_elemental_resistances");

    // [Minion|Minions] have +7% to [Chaos] [Resistances|Resistance]
    public static final Identifier MINIONS_HAVE_TO_CHAOS_RESISTANCES = id("minions_have_to_chaos_resistances");

    // [Minion|Minions] have +20% to Cold [Resistances|Resistance]
    public static final Identifier MINIONS_HAVE_TO_COLD_RESISTANCES = id("minions_have_to_cold_resistances");

    // [Minion|Minions] have +20% to Fire [Resistances|Resistance]
    public static final Identifier MINIONS_HAVE_TO_FIRE_RESISTANCES = id("minions_have_to_fire_resistances");

    // [Minion|Minions] have +20% to Lightning [Resistances|Resistance]
    public static final Identifier MINIONS_HAVE_TO_LIGHTNING_RESISTANCES = id("minions_have_to_lightning_resistances");

    // [Minion|Minions] have +3% to [MaximumResistances|Maximum Cold Resistances]
    public static final Identifier MINIONS_HAVE_TO_MAX_COLD_RESISTANCES = id("minions_have_to_max_cold_resistances");

    // [Minion|Minions] have +3% to [MaximumResistances|Maximum Fire Resistances]
    public static final Identifier MINIONS_HAVE_TO_MAX_FIRE_RESISTANCES = id("minions_have_to_max_fire_resistances");

    // [Minion|Minions] have +3% to [MaximumResistances|Maximum Lightning Resistances]
    public static final Identifier MINIONS_HAVE_TO_MAX_LIGHTNING_RESISTANCES = id("minions_have_to_max_lightning_resistances");

    // [Minion|Minions] [Recoup] 30% of Damage taken as Life
    public static final Identifier MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH = id("minions_recoup_of_damage_taken_as_health");

    // [Minion|Minions] Regenerate 3% of maximum Life per second
    public static final Identifier MINIONS_REGENERATE_OF_MAX_HEALTH_PER_SECOND = id("minions_regenerate_of_max_health_per_second");

    // [Minion|Minions] [Reviving|Revive] 25% faster
    public static final Identifier MINIONS_REVIVING_FASTER = id("minions_reviving_faster");

    // [Minion|Minions] [Reviving|Revive] 35% faster if all your [Minion|Minions] are [Companion|Companions]
    public static final Identifier MINIONS_REVIVING_FASTER_IF_ALL_YOUR_MINIONS_ARE_COMPANIONS = id("minions_reviving_faster_if_all_your_minions_are_companions");

    // 50% more amount of [LifeLeech|Life Leeched]
    public static final Identifier MORE_AMOUNT_OF_HEALTH_LEECHED = id("more_amount_of_health_leeched");

    // 50% more [BuffMagnitude|Magnitude] of [Bleeding] you inflict
    public static final Identifier MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT = id("more_buff_magnitude_of_bleeding_you_inflict");

    // 30% less [CriticalDamageBonus|Critical Damage Bonus] when on Full Life
    public static final Identifier MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH = id("more_critical_damage_bonus_when_on_full_health");

    // 30% more Critical Damage Bonus when on [LowLife|Low Life]
    public static final Identifier MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_LOW_HEALTH = id("more_critical_damage_bonus_when_on_low_health");

    // 20% more Damage against [HeavyStun|Heavy Stunned] Enemies with [Mace|Maces]
    public static final Identifier MORE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES_WITH_MACES = id("more_damage_against_heavy_stunned_enemies_with_maces");

    // 50% less Life Recovery from [Flask|Flasks]
    public static final Identifier MORE_HEALTH_RECOVERY_FROM_FLASKS = id("more_health_recovery_from_flasks");

    // 80% less [Knockback] Distance for [Block|Blocked] [HitDamage|Hits]
    public static final Identifier MORE_KNOCKBACK_DISTANCE_FOR_BLOCKED_HIT_DAMAGE = id("more_knockback_distance_for_blocked_hit_damage");

    // 50% less Mana Recovery Rate
    public static final Identifier MORE_MANA_RECOVERY_RATE = id("more_mana_recovery_rate");

    // 10% more Maximum [Lightning|Lightning Damage]
    public static final Identifier MORE_MAX_LIGHTNING_DAMAGE = id("more_max_lightning_damage");

    // 100% more Maximum [Rage]
    public static final Identifier MORE_MAX_RAGE = id("more_max_rage");

    // 30% more Recovery from [Flask|Flasks]
    public static final Identifier MORE_RECOVERY_FROM_FLASKS = id("more_recovery_from_flasks");

    // 30% more [Reservation] [Efficiency] of [Companion] Skills
    public static final Identifier MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS = id("more_reservation_efficiency_of_companion_skills");

    // 20% less [Reservation] [Efficiency] of non-[Companion] Skills
    public static final Identifier MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS = id("more_reservation_efficiency_of_non_companion_skills");

    // 25% more Skill Speed while Off Hand is empty and you have a [One-Handed] [MartialWeapon|Martial Weapon] equipped in your Main Hand
    public static final Identifier MORE_SKILL_SPEED_WHILE_OFF_HAND_IS_EMPTY_AND_YOU_HAVE_A_ONE_HANDED_MARTIAL_WEAPON_EQUIPPED_IN_YOUR_MAIN_HAND = id("more_skill_speed_while_off_hand_is_empty_and_you_have_a_one_handed_martial_weapon_equipped_in_your_main_hand");

    // 50% less [Spirit]
    public static final Identifier MORE_SPIRIT = id("more_spirit");

    // 20% more [Stun|Stun] Buildup with [Critical|Critical Hits]
    public static final Identifier MORE_STUN_BUILDUP_WITH_CRITICAL_HITS = id("more_stun_buildup_with_critical_hits");

    // Never deal [Critical|Critical Hits]
    public static final Identifier NEVER_DEAL_CRITICAL_HITS = id("never_deal_critical_hits");

    // Non-[Minion] Skills have 50% less [Reservation] [Efficiency]
    public static final Identifier NON_MINION_SKILLS_HAVE_MORE_RESERVATION_EFFICIENCY = id("non_minion_skills_have_more_reservation_efficiency");

    // No [Charges|Charge] requirement for placing [Totem|Totems]
    public static final Identifier NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS = id("no_charges_requirement_for_placing_totems");

    // No [Rage] effect
    public static final Identifier NO_RAGE_EFFECT = id("no_rage_effect");

    // [Offering|Offerings] cannot be damaged if they have been created [Recently]
    public static final Identifier OFFERINGS_CANNOT_BE_DAMAGED_IF_THEY_HAVE_BEEN_CREATED_RECENTLY = id("offerings_cannot_be_damaged_if_they_have_been_created_recently");

    // [Offering|Offerings] have 30% increased Maximum Life
    public static final Identifier OFFERINGS_HAVE_INC_MAX_HEALTH = id("offerings_have_inc_max_health");

    // [Offering] Skills have 20% increased Area of Effect
    public static final Identifier OFFERING_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("offering_skills_have_inc_area_of_effect");

    // [Offering] Skills have 15% increased [Buff] effect
    public static final Identifier OFFERING_SKILLS_HAVE_INC_BUFF_EFFECT = id("offering_skills_have_inc_buff_effect");

    // [Offering] Skills have 30% reduced Duration
    public static final Identifier OFFERING_SKILLS_HAVE_INC_DURATION = id("offering_skills_have_inc_duration");

    // +20% of [Armour] also applies to [Chaos|Chaos Damage]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_CHAOS_DAMAGE = id("of_armor_also_applies_to_chaos_damage");

    // +15% of [Armour] also applies to [Cold|Cold Damage]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE = id("of_armor_also_applies_to_cold_damage");

    // +30% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE = id("of_armor_also_applies_to_elemental_damage");

    // +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage] while [Shapeshift|Shapeshifted]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED = id("of_armor_also_applies_to_elemental_damage_while_shapeshifted");

    // +15% of [Armour] also applies to [Fire|Fire Damage]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE = id("of_armor_also_applies_to_fire_damage");

    // +15% of [Armour] also applies to [Lightning|Lightning Damage]
    public static final Identifier OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE = id("of_armor_also_applies_to_lightning_damage");

    // 75% of Damage Converted to Fire Damage
    public static final Identifier OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE = id("of_damage_converted_to_fire_damage");

    // 5% of Damage from [HitDamage|Hits] is taken from your Damageable [Companion]'s Life before you
    public static final Identifier OF_DAMAGE_FROM_HIT_DAMAGE_IS_TAKEN_FROM_YOUR_DAMAGEABLE_COMPANION_S_HEALTH_BEFORE_YOU = id("of_damage_from_hit_damage_is_taken_from_your_damageable_companion_s_health_before_you");

    // 10% of Damage is taken from Mana before Life
    public static final Identifier OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH = id("of_damage_is_taken_from_mana_before_health");

    // 5% of Damage taken bypasses [EnergyShield|Energy Shield]
    public static final Identifier OF_DAMAGE_TAKEN_BYPASSES_ENERGY_SHIELD = id("of_damage_taken_bypasses_energy_shield");

    // 15% of Damage taken from [Deflect|Deflected] [HitDamage|Hits] [Recoup|Recouped] as Life
    public static final Identifier OF_DAMAGE_TAKEN_FROM_DEFLECTED_HIT_DAMAGE_RECOUPED_AS_HEALTH = id("of_damage_taken_from_deflected_hit_damage_recouped_as_health");

    // 8% of Damage taken [Recoup|Recouped] as Life
    public static final Identifier OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH = id("of_damage_taken_recouped_as_health");

    // 3% of Damage Taken [Recoup|Recouped] as Life, Mana and [EnergyShield|Energy Shield]
    public static final Identifier OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_MANA_AND_ENERGY_SHIELD = id("of_damage_taken_recouped_as_health_mana_and_energy_shield");

    // 10% of Damage taken Recouped as Life while [Channelling]
    public static final Identifier OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING = id("of_damage_taken_recouped_as_health_while_channelling");

    // 3% of Damage taken [Recoup|Recouped] as Mana
    public static final Identifier OF_DAMAGE_TAKEN_RECOUPED_AS_MANA = id("of_damage_taken_recouped_as_mana");

    // 3% of [ElementalDamage|Elemental Damage] taken [Recoup|Recouped] as [EnergyShield|Energy Shield]
    public static final Identifier OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD = id("of_elemental_damage_taken_recouped_as_energy_shield");

    // 20% of [ElementalDamage|Elemental Damage] taken [Recoup|Recouped] as Life
    public static final Identifier OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH = id("of_elemental_damage_taken_recouped_as_health");

    // 4% of Maximum Life [StatConversion|Converted] to [EnergyShield|Energy Shield]
    public static final Identifier OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD = id("of_max_health_stat_conversion_to_energy_shield");

    // 5% of [Physical] Damage from [HitDamage|Hits] taken as Damage of a Random [ElementalDamage|Element]
    public static final Identifier OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE = id("of_physical_damage_from_hit_damage_taken_as_damage_of_a_random_elemental_damage");

    // 5% of [Physical] Damage prevented [Recoup|Recouped] as [EnergyShield|Energy Shield] per enemy [Power]
    public static final Identifier OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER = id("of_physical_damage_prevented_recouped_as_energy_shield_per_enemy_power");

    // 5% of Physical Damage prevented [Recoup|Recouped] as Life
    public static final Identifier OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_HEALTH = id("of_physical_damage_prevented_recouped_as_health");

    // 5% of [Physical|Physical] Damage taken as [Fire|Fire] Damage
    public static final Identifier OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE = id("of_physical_damage_taken_as_fire_damage");

    // 20% of [Physical] Damage taken [Recoup|Recouped] as Life
    public static final Identifier OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH = id("of_physical_damage_taken_recouped_as_health");

    // 18% of Skill Mana Costs [StatConversion|Converted] to Life Costs
    public static final Identifier OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS = id("of_skill_mana_costs_stat_conversion_to_health_costs");

    // 50% of Skill Mana costs [StatConversion|Converted] to Life Costs during any Life [Flask] Effect
    public static final Identifier OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS_DURING_ANY_HEALTH_FLASK_EFFECT = id("of_skill_mana_costs_stat_conversion_to_health_costs_during_any_health_flask_effect");

    // 8% of [Spell] Mana Cost [StatConversion|Converted] to Life Cost
    public static final Identifier OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST = id("of_spell_mana_cost_stat_conversion_to_health_cost");

    // 10% of [Thorns|Thorns] Damage [LifeLeech|Leeched as Life]
    public static final Identifier OF_THORNS_DAMAGE_LEECHED_AS_HEALTH = id("of_thorns_damage_leeched_as_health");

    // [Orb] Skills have +1 to [Limit]
    public static final Identifier ORB_SKILLS_HAVE_TO_LIMIT = id("orb_skills_have_to_limit");

    // [Plant|Overgrown Plant] Skills [ArmourBreak|Break] 50% increased [Armour|Armour]
    public static final Identifier OVERGROWN_PLANT_SKILLS_ARMOR_BREAK_INC_ARMOR = id("overgrown_plant_skills_armor_break_inc_armor");

    // [Parry] has 20% increased [Stun] Buildup
    public static final Identifier PARRY_HAS_INC_STUN_BUILDUP = id("parry_has_inc_stun_buildup");

    // [Physical] [Spell] [Critical] [HitDamage|Hits] build [Pinned|Pin]
    public static final Identifier PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED = id("physical_spell_critical_hit_damage_build_pinned");

    // [Pinned] Enemies cannot deal [Critical|Critical Hits]
    public static final Identifier PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS = id("pinned_enemies_cannot_deal_critical_hits");

    // [Plant|Plants] have a 20% chance to immediately [Plant|Overgrow]
    public static final Identifier PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW = id("plants_have_a_chance_to_immediately_overgrow");

    // Prevent +15% of Damage from [Deflect|Deflected] [Critical] [HitDamage|Hits]
    public static final Identifier PREVENT_OF_DAMAGE_FROM_DEFLECTED_CRITICAL_HIT_DAMAGE = id("prevent_of_damage_from_deflected_critical_hit_damage");

    // Prevent +3% of Damage from [Deflect|Deflected] [HitDamage|Hits]
    public static final Identifier PREVENT_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE = id("prevent_of_damage_from_deflected_hit_damage");

    // [Projectile|Projectiles] deal 75% increased Damage against [HeavyStun|Heavy Stunned] Enemies
    public static final Identifier PROJECTILES_DEAL_INC_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES = id("projectiles_deal_inc_damage_against_heavy_stunned_enemies");

    // [Projectile|Projectiles] deal 15% increased Damage with Hits against Enemies further than 6m
    public static final Identifier PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM = id("projectiles_deal_inc_damage_with_hits_against_enemies_further_than_xm");

    // [Projectile|Projectiles] deal 15% increased Damage with Hits against Enemies within 2m
    public static final Identifier PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM = id("projectiles_deal_inc_damage_with_hits_against_enemies_within_xm");

    // Projectiles have 75% chance for an additional Projectile when [Fork|Forking]
    public static final Identifier PROJECTILES_HAVE_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING = id("projectiles_have_chance_for_an_additional_projectile_when_forking");

    // [Projectile|Projectiles] have 10% chance to [Chain] an additional time from terrain
    public static final Identifier PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN = id("projectiles_have_chance_to_chain_an_additional_time_from_terrain");

    // [Projectile|Projectiles] have 25% chance to [Fork] if you've dealt a [Melee] [HitDamage|Hit] in the past eight seconds
    public static final Identifier PROJECTILES_HAVE_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS = id("projectiles_have_chance_to_fork_if_you_ve_dealt_a_melee_hit_damage_in_the_past_eight_seconds");

    // [Projectile|Projectiles] have 30% increased [CriticalDamageBonus|Critical Damage Bonus] against Enemies further than 6m
    public static final Identifier PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_FURTHER_THAN_XM = id("projectiles_have_inc_critical_damage_bonus_against_enemies_further_than_xm");

    // [Projectile|Projectiles] have 40% increased [CriticalDamageBonus|Critical Damage Bonus] against Enemies within 2m
    public static final Identifier PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_WITHIN_XM = id("projectiles_have_inc_critical_damage_bonus_against_enemies_within_xm");

    // [Projectile|Projectiles] have 12% increased [Critical] Hit Chance against Enemies further than 6m
    public static final Identifier PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM = id("projectiles_have_inc_critical_hit_chance_against_enemies_further_than_xm");

    // [Projectile|Projectiles] [Pierce] enemies with [ArmourBreak|Fully Broken Armour]
    public static final Identifier PROJECTILES_PIERCE_ENEMIES_WITH_FULLY_BROKEN_ARMOR = id("projectiles_pierce_enemies_with_fully_broken_armor");

    // [Projectile] [Attack|Attacks] have a 12% chance to fire two additional [Projectile|Projectiles] while moving
    public static final Identifier PROJECTILE_ATTACKS_HAVE_A_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING = id("projectile_attacks_have_a_chance_to_fire_two_additional_projectiles_while_moving");

    // [Quarterstaff|Quarterstaff] Skills that consume [Charges|Power Charges] count as consuming an additional Power Charge
    public static final Identifier QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE = id("quarterstaff_skills_that_consume_power_charges_count_as_consuming_an_additional_power_charge");

    // [Recoup] 5% of damage taken by your [Totem|Totems] as Life
    public static final Identifier RECOUP_OF_DAMAGE_TAKEN_BY_YOUR_TOTEMS_AS_HEALTH = id("recoup_of_damage_taken_by_your_totems_as_health");

    // Recover 20 Life when you [Block]
    public static final Identifier RECOVER_HEALTH_WHEN_YOU_BLOCK = id("recover_health_when_you_block");

    // Recover 2% of maximum Life and Mana when you use a [Warcry]
    public static final Identifier RECOVER_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY = id("recover_of_max_health_and_mana_when_you_use_a_warcry");

    // Recover 2% of maximum Life for each [Charges|Endurance Charge] consumed
    public static final Identifier RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED = id("recover_of_max_health_for_each_endurance_charge_consumed");

    // Recover 3% of maximum Life on Kill
    public static final Identifier RECOVER_OF_MAX_HEALTH_ON_KILL = id("recover_of_max_health_on_kill");

    // Recover 2% of maximum Life on Killing a [Poison|Poisoned] Enemy
    public static final Identifier RECOVER_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY = id("recover_of_max_health_on_killing_a_poisoned_enemy");

    // Recover 1% of maximum Life per [Glory] consumed
    public static final Identifier RECOVER_OF_MAX_HEALTH_PER_GLORY_CONSUMED = id("recover_of_max_health_per_glory_consumed");

    // Recover 2% of maximum Life when one of your [Minion|Minions] is [Reviving|Revived]
    public static final Identifier RECOVER_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING = id("recover_of_max_health_when_one_of_your_minions_is_reviving");

    // Recover 3% of Maximum Life when you collect a [Remnant]
    public static final Identifier RECOVER_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT = id("recover_of_max_health_when_you_collect_a_remnant");

    // Recover 3% of maximum Life when you create an [Offering]
    public static final Identifier RECOVER_OF_MAX_HEALTH_WHEN_YOU_CREATE_AN_OFFERING = id("recover_of_max_health_when_you_create_an_offering");

    // Recover 50% of maximum Life when you [HeavyStun|Heavy Stun] a [Rarity|Rare or Unique] Enemy
    public static final Identifier RECOVER_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY = id("recover_of_max_health_when_you_heavy_stun_a_rare_or_unique_enemy");

    // Recover 2% of maximum Life when you use a Mana [Flask]
    public static final Identifier RECOVER_OF_MAX_HEALTH_WHEN_YOU_USE_A_MANA_FLASK = id("recover_of_max_health_when_you_use_a_mana_flask");

    // Recover 1% of maximum Mana on Kill
    public static final Identifier RECOVER_OF_MAX_MANA_ON_KILL = id("recover_of_max_mana_on_kill");

    // Recover 5% of maximum Mana when a [Charm|Charm] is used
    public static final Identifier RECOVER_OF_MAX_MANA_WHEN_A_CHARM_IS_USED = id("recover_of_max_mana_when_a_charm_is_used");

    // Recover 3% of Maximum Mana when you collect a [Remnant]
    public static final Identifier RECOVER_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT = id("recover_of_max_mana_when_you_collect_a_remnant");

    // Recover 2% of maximum Mana when you consume a [Charges|Power Charge]
    public static final Identifier RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE = id("recover_of_max_mana_when_you_consume_a_power_charge");

    // Regenerate 5% of maximum Life over 1 second when [Stun|Stunned]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED = id("regenerate_of_max_health_over_second_when_stunned");

    // Regenerate 0.5% of maximum Life per second
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND = id("regenerate_of_max_health_per_second");

    // Regenerate 0.5% of maximum Life per second if you have been Hit [Recently]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_HAVE_BEEN_HIT_RECENTLY = id("regenerate_of_max_health_per_second_if_you_have_been_hit_recently");

    // Regenerate 1% of maximum Life per Second if you've used a Life [Flask] in the past 10 seconds
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_SECONDS = id("regenerate_of_max_health_per_second_if_you_ve_used_a_health_flask_in_the_past_seconds");

    // Regenerate 1% of maximum Life per second while affected by any [DamagingAilments|Damaging Ailment]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT = id("regenerate_of_max_health_per_second_while_affected_by_any_damaging_ailment");

    // Regenerate 1.5% of maximum Life per second while on [LowLife|Low Life]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_ON_LOW_HEALTH = id("regenerate_of_max_health_per_second_while_on_low_health");

    // Regenerate 1% of maximum Life per second while stationary
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_STATIONARY = id("regenerate_of_max_health_per_second_while_stationary");

    // Regenerate 2.5% of maximum Life per second while [Surrounded|Surrounded]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_SURROUNDED = id("regenerate_of_max_health_per_second_while_surrounded");

    // Regenerate 1% of maximum Life per second while you have a [Totem|Totem]
    public static final Identifier REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_YOU_HAVE_A_TOTEM = id("regenerate_of_max_health_per_second_while_you_have_a_totem");

    // Regenerate 1 [Rage] per second per 4 [Rage] spent [Recently]
    public static final Identifier REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY = id("regenerate_rage_per_second_per_rage_spent_recently");

    // [Remnant|Remnants] can be collected from 30% further away
    public static final Identifier REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY = id("remnants_can_be_collected_from_further_away");

    // [Remnant|Remnants] you create have 10% increased effect
    public static final Identifier REMNANTS_YOU_CREATE_HAVE_INC_EFFECT = id("remnants_you_create_have_inc_effect");

    // [Seal|Sealed] Skills have 10% increased [Seal] gain frequency
    public static final Identifier SEALED_SKILLS_HAVE_INC_SEAL_GAIN_FREQUENCY = id("sealed_skills_have_inc_seal_gain_frequency");

    // Shapeshift Skills have 15% increased Skill Effect Duration
    public static final Identifier SHAPESHIFT_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION = id("shapeshift_skills_have_inc_skill_effect_duration");

    // [Shock|Shocking] [HitDamage|Hits] have a 50% chance to also Shock enemies in a 1.5 metre radius
    public static final Identifier SHOCKING_HIT_DAMAGE_HAVE_A_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_METRE_RADIUS = id("shocking_hit_damage_have_a_chance_to_also_shock_enemies_in_a_metre_radius");

    // Skills deal 8% increased Damage per [Combo] consumed, up to 40%
    public static final Identifier SKILLS_DEAL_INC_DAMAGE_PER_COMBO_CONSUMED_UP_TO = id("skills_deal_inc_damage_per_combo_consumed_up_to");

    // Skills lose [Combo] 20% slower
    public static final Identifier SKILLS_GAIN_COMBO_SLOWER = id("skills_gain_combo_slower");

    // Skills gain 1 [Glory] every 2 seconds for each [Rarity|Rare or Unique] monster in your [Presence]
    public static final Identifier SKILLS_GAIN_GLORY_EVERY_SECONDS_FOR_EACH_RARE_OR_UNIQUE_MONSTER_IN_YOUR_PRESENCE = id("skills_gain_glory_every_seconds_for_each_rare_or_unique_monster_in_your_presence");

    // Skills have a 15% chance to not consume [Glory]
    public static final Identifier SKILLS_HAVE_A_CHANCE_TO_NOT_CONSUME_GLORY = id("skills_have_a_chance_to_not_consume_glory");

    // Skills have 10% chance to not remove [Charges|Charges] but still count as consuming them
    public static final Identifier SKILLS_HAVE_CHANCE_TO_NOT_ADD_CHARGES_BUT_STILL_COUNT_AS_CONSUMING_THEM = id("skills_have_chance_to_not_add_charges_but_still_count_as_consuming_them");

    // Skills have 5% chance to not remove [ElementalInfusion|Elemental Infusions] but still count as consuming them
    public static final Identifier SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM = id("skills_have_chance_to_not_add_elemental_infusions_but_still_count_as_consuming_them");

    // Skills which create Fissures have a 20% chance to create an additional Fissure
    public static final Identifier SKILLS_WHICH_CREATE_FISSURES_HAVE_A_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE = id("skills_which_create_fissures_have_a_chance_to_create_an_additional_fissure");

    // Skill Mana Costs [StatConversion|Converted] to Life Costs
    public static final Identifier SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS = id("skill_mana_costs_stat_conversion_to_health_costs");

    // [Slam] Skills you use yourself have 30% increased [Aftershock] Area of Effect
    public static final Identifier SLAM_SKILLS_YOU_USE_YOURSELF_HAVE_INC_AFTERSHOCK_AREA_OF_EFFECT = id("slam_skills_you_use_yourself_have_inc_aftershock_area_of_effect");

    // 20% [FasterESRechargeStart|slower start of Energy Shield Recharge]
    public static final Identifier SLOWER_START_OF_ENERGY_SHIELD_RECHARGE = id("slower_start_of_energy_shield_recharge");

    // [Spell|Spells] Cast by [Totem|Totems] have 2% increased Cast Speed
    public static final Identifier SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED = id("spells_cast_by_totems_have_inc_cast_speed");

    // [Spell|Spells] Cast by [Totem|Totems] have 3% increased Cast Speed per Summoned [Totem|Totem]
    public static final Identifier SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED_PER_SUMMONED_TOTEM = id("spells_cast_by_totems_have_inc_cast_speed_per_summoned_totem");

    // [Spell|Spells] [Gain] 5% of Damage as extra [Chaos|Chaos] Damage
    public static final Identifier SPELLS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE = id("spells_gain_of_damage_as_extra_chaos_damage");

    // [Spell|Spell] Skills have 6% increased Area of Effect
    public static final Identifier SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("spell_skills_have_inc_area_of_effect");

    // [StatConversion|Converts] all [Evasion|Evasion Rating] to [Armour]
    public static final Identifier STAT_CONVERSION_ALL_EVASION_TO_ARMOR = id("stat_conversion_all_evasion_to_armor");

    // [StatConversion|Convert] 100% of maximum [EnergyShield|Energy Shield] to maximum Mana
    public static final Identifier STAT_CONVERSION_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA = id("stat_conversion_of_max_energy_shield_to_max_mana");

    // [Storm|Storm] and [Plant|Plant] [Spell|Spells]: cost 50% less
    public static final Identifier STORM_AND_PLANT_SPELLS_COST_MORE = id("storm_and_plant_spells_cost_more");

    // [Storm|Storm] and [Plant|Plant] [Spell|Spells]: deal 50% more damage
    public static final Identifier STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE = id("storm_and_plant_spells_deal_more_damage");

    // [Storm|Storm] and [Plant|Plant] [Spell|Spells]: have 75% less duration
    public static final Identifier STORM_AND_PLANT_SPELLS_HAVE_MORE_DURATION = id("storm_and_plant_spells_have_more_duration");

    // [Strike|Strikes] deal [MeleeSplash|Splash Damage]
    public static final Identifier STRIKES_DEAL_SPLASH_DAMAGE = id("strikes_deal_splash_damage");

    // [Strike] Skills you use yourself with [Mace|Maces] have 10% chance to deal [MeleeSplash|Splash Damage]
    public static final Identifier STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_CHANCE_TO_DEAL_SPLASH_DAMAGE = id("strike_skills_you_use_yourself_with_maces_have_chance_to_deal_splash_damage");

    // [StunThreshold|Stun Threshold] is based on 30% of your [EnergyShield|Energy Shield] instead of Life
    public static final Identifier STUN_THRESHOLD_IS_BASED_ON_OF_YOUR_ENERGY_SHIELD_INSTEAD_OF_HEALTH = id("stun_threshold_is_based_on_of_your_energy_shield_instead_of_health");

    // Successfully [Parry|Parrying] a [Melee] [HitDamage|Hit] grants 40% increased Damage to your next Ranged [Attack]
    public static final Identifier SUCCESSFULLY_PARRYING_A_MELEE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_RANGED_ATTACK = id("successfully_parrying_a_melee_hit_damage_grants_inc_damage_to_your_next_ranged_attack");

    // Successfully [Parry|Parrying] a [Projectile] [HitDamage|Hit] grants 40% increased Damage to your next [Melee] [Attack]
    public static final Identifier SUCCESSFULLY_PARRYING_A_PROJECTILE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_MELEE_ATTACK = id("successfully_parrying_a_projectile_hit_damage_grants_inc_damage_to_your_next_melee_attack");

    // 15% [SurpassChance|Surpassing Chance] to gain a [PuppetMaster|Puppet Master] stack whenever you use a [Command|Command] Skill
    public static final Identifier SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL = id("surpassing_chance_to_gain_a_puppet_master_stack_whenever_you_use_a_command_skill");

    // +8% [SurpassChance|Surpassing] chance to fire an additional Arrow
    public static final Identifier SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW = id("surpass_chance_to_fire_an_additional_arrow");

    // +8% [SurpassChance|Surpassing] chance to fire an additional [Projectile]
    public static final Identifier SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE = id("surpass_chance_to_fire_an_additional_projectile");

    // Take 30% less [HitDamage|Damage from Hits] while Dodge Rolling
    public static final Identifier TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING = id("take_more_damage_from_hits_while_dodge_rolling");

    // Take 50% more Damage over Time if you haven't started taking Damage over Time in the past second
    public static final Identifier TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND = id("take_more_damage_over_time_if_you_haven_t_started_taking_damage_over_time_in_the_past_second");

    // Take 50% less Damage over Time if you've started taking Damage over Time in the past second
    public static final Identifier TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND = id("take_more_damage_over_time_if_you_ve_started_taking_damage_over_time_in_the_past_second");

    // Targets can be affected by +1 of your [Poison|Poisons] at the same time
    public static final Identifier TARGETS_CAN_BE_AFFECTED_BY_OF_YOUR_POISONS_AT_THE_SAME_TIME = id("targets_can_be_affected_by_of_your_poisons_at_the_same_time");

    // Targets [Curse|Cursed] by you have 50% reduced Life Regeneration Rate
    public static final Identifier TARGETS_CURSED_BY_YOU_HAVE_INC_HEALTH_REGENERATION_RATE = id("targets_cursed_by_you_have_inc_health_regeneration_rate");

    // [TemporaryMinion|Temporary Minion] Skills have +2 to [Limit] of [Minion|Minions] summoned
    public static final Identifier TEMPORARY_MINION_SKILLS_HAVE_TO_LIMIT_OF_MINIONS_SUMMONED = id("temporary_minion_skills_have_to_limit_of_minions_summoned");

    // The next [Attack] you use within 4 seconds after [HeavyStun|Heavy Stunning] a [Rarity|Rare or Unique] Enemy is [AncestralBoostSpell|Ancestrally Boosted]
    public static final Identifier THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED = id("the_next_attack_you_use_within_seconds_after_heavy_stunning_a_rare_or_unique_enemy_is_ancestrally_boosted");

    // [Thorns|Thorns Damage] has 50% chance to ignore Enemy [Armour|Armour]
    public static final Identifier THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR = id("thorns_damage_has_chance_to_ignore_enemy_armor");

    // [Totem|Totems] gain +12% to all Elemental [Resistances]
    public static final Identifier TOTEMS_GAIN_TO_ALL_ELEMENTAL_RESISTANCES = id("totems_gain_to_all_elemental_resistances");

    // [Totem|Totems] gain +2% to all [MaximumResistances|Maximum Elemental Resistances]
    public static final Identifier TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES = id("totems_gain_to_all_max_elemental_resistances");

    // [Totem|Totems] have 12% additional [Physical] Damage Reduction
    public static final Identifier TOTEMS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION = id("totems_have_additional_physical_damage_reduction");

    // [Totem|Totems] Regenerate 3% of maximum Life per second
    public static final Identifier TOTEMS_REGENERATE_OF_MAX_HEALTH_PER_SECOND = id("totems_regenerate_of_max_health_per_second");

    // [Totem|Totems] reserve 75 [Spirit] each
    public static final Identifier TOTEMS_RESERVE_SPIRIT_EACH = id("totems_reserve_spirit_each");

    // +30 to [Accuracy|Accuracy] Rating
    public static final Identifier TO_ACCURACY = id("to_accuracy");

    // +250 to Accuracy against Bleeding Enemies
    public static final Identifier TO_ACCURACY_AGAINST_BLEEDING_ENEMIES = id("to_accuracy_against_bleeding_enemies");

    // +4 to [AilmentThreshold|Ailment Threshold] per [Dexterity|Dexterity]
    public static final Identifier TO_AILMENT_THRESHOLD_PER_DEXTERITY = id("to_ailment_threshold_per_dexterity");

    // +3 to all [Attributes|Attributes]
    public static final Identifier TO_ALL_ATTRIBUTES = id("to_all_attributes");

    // +3% to all [ElementalDamage|Elemental] [Resistances]
    public static final Identifier TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES = id("to_all_elemental_damage_resistances");

    // +1% to all [MaximumResistances|Maximum Elemental Resistances] if you have at least 5 Red, Green and Blue [SupportGem|Support Gems] Socketed
    public static final Identifier TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED = id("to_all_max_elemental_resistances_if_you_have_at_least_red_green_and_blue_support_gems_socketed");

    // -5% to amount of [HitDamage|Damage] Prevented by [Deflect|Deflection]
    public static final Identifier TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION = id("to_amount_of_hit_damage_prevented_by_deflection");

    // +5 to any [Attributes|Attribute]
    public static final Identifier TO_ANY_ATTRIBUTES = id("to_any_attributes");

    // +20 to [Armour]
    public static final Identifier TO_ARMOR = id("to_armor");

    // +2 to [Armour] per 1 [ItemEnergyShield|Item Energy Shield] on Equipped Boots
    public static final Identifier TO_ARMOR_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_BOOTS = id("to_armor_per_item_energy_shield_on_equipped_boots");

    // +500 to [Armour] while Frozen
    public static final Identifier TO_ARMOR_WHILE_FROZEN = id("to_armor_while_frozen");

    // +8% to [Resistances|Chaos Resistance]
    public static final Identifier TO_CHAOS_RESISTANCE = id("to_chaos_resistance");

    // +5% to [Resistances|Cold Resistance]
    public static final Identifier TO_COLD_RESISTANCE = id("to_cold_resistance");

    // +25% to [CriticalDamageBonus|Critical Damage Bonus] against Stunned Enemies
    public static final Identifier TO_CRITICAL_DAMAGE_BONUS_AGAINST_STUNNED_ENEMIES = id("to_critical_damage_bonus_against_stunned_enemies");

    // +8% to [Critical|Critical Hit] Chance of [Herald] Skills
    public static final Identifier TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS = id("to_critical_hit_chance_of_herald_skills");

    // +10 to [Dexterity]
    public static final Identifier TO_DEXTERITY = id("to_dexterity");

    // +5 to [Dexterity|Dexterity] and [Intelligence|Intelligence]
    public static final Identifier TO_DEXTERITY_AND_INTELLIGENCE = id("to_dexterity_and_intelligence");

    // +8 to [Evasion] Rating
    public static final Identifier TO_EVASION = id("to_evasion");

    // +1 to [Evasion] Rating per 1 [ItemArmour|Item Armour] on Equipped Legs
    public static final Identifier TO_EVASION_PER_ITEM_ARMOR_ON_EQUIPPED_LEGS = id("to_evasion_per_item_armor_on_equipped_legs");

    // +1 to [Evasion] Rating per 1 [ItemEnergyShield|Item Energy Shield] on Equipped Helmet
    public static final Identifier TO_EVASION_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_HELMET = id("to_evasion_per_item_energy_shield_on_equipped_helmet");

    // +5% to [Resistances|Fire Resistance]
    public static final Identifier TO_FIRE_RESISTANCE = id("to_fire_resistance");

    // +15 to [Intelligence]
    public static final Identifier TO_INTELLIGENCE = id("to_intelligence");

    // +1 to Level of all [Chaos] Skills
    public static final Identifier TO_LEVEL_OF_ALL_CHAOS_SKILLS = id("to_level_of_all_chaos_skills");

    // +1 to Level of all [Cold] Skills
    public static final Identifier TO_LEVEL_OF_ALL_COLD_SKILLS = id("to_level_of_all_cold_skills");

    // +5% to [Resistances|Lightning Resistance]
    public static final Identifier TO_LIGHTNING_RESISTANCE = id("to_lightning_resistance");

    // +4% to maximum [Block] chance
    public static final Identifier TO_MAX_BLOCK_CHANCE = id("to_max_block_chance");

    // +1 to maximum [ElementalInfusion|Cold Infusions]
    public static final Identifier TO_MAX_COLD_INFUSIONS = id("to_max_cold_infusions");

    // +1% to [MaximumResistances|Maximum Cold Resistance]
    public static final Identifier TO_MAX_COLD_RESISTANCE = id("to_max_cold_resistance");

    // +2% to [MaximumResistances|Maximum Cold Resistance] if you have at least 5 Blue [SupportGem|Support Gems] Socketed
    public static final Identifier TO_MAX_COLD_RESISTANCE_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED = id("to_max_cold_resistance_if_you_have_at_least_blue_support_gems_socketed");

    // +1% to [MaximumResistances|Maximum Cold Resistance] while [Shapeshift|Shapeshifted]
    public static final Identifier TO_MAX_COLD_RESISTANCE_WHILE_SHAPESHIFTED = id("to_max_cold_resistance_while_shapeshifted");

    // +1 to Maximum [Charges|Endurance Charges]
    public static final Identifier TO_MAX_ENDURANCE_CHARGES = id("to_max_endurance_charges");

    // +5 to maximum [EnergyShield|Energy Shield]
    public static final Identifier TO_MAX_ENERGY_SHIELD = id("to_max_energy_shield");

    // +1 to Maximum [EnergyShield|Energy Shield] per 8 [ItemArmour|Item Armour] on Equipped Helmet
    public static final Identifier TO_MAX_ENERGY_SHIELD_PER_ITEM_ARMOR_ON_EQUIPPED_HELMET = id("to_max_energy_shield_per_item_armor_on_equipped_helmet");

    // +1 to Maximum [EnergyShield|Energy Shield] per 12 [ItemEvasion|Item Evasion] on Equipped Body Armour
    public static final Identifier TO_MAX_ENERGY_SHIELD_PER_ITEM_EVASION_ON_EQUIPPED_BODY_ARMOR = id("to_max_energy_shield_per_item_evasion_on_equipped_body_armor");

    // +1 to maximum [ElementalInfusion|Fire Infusions]
    public static final Identifier TO_MAX_FIRE_INFUSIONS = id("to_max_fire_infusions");

    // +2% to [MaximumResistances|Maximum Fire Resistance] if you have at least 5 Red [SupportGem|Support Gems] Socketed
    public static final Identifier TO_MAX_FIRE_RESISTANCE_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED = id("to_max_fire_resistance_if_you_have_at_least_red_support_gems_socketed");

    // +2% to [MaximumResistances|Maximum Fire Resistance] while [Ignite|Ignited]
    public static final Identifier TO_MAX_FIRE_RESISTANCE_WHILE_IGNITED = id("to_max_fire_resistance_while_ignited");

    // +1% to [MaximumResistances|Maximum Fire Resistance] while [Shapeshift|Shapeshifted]
    public static final Identifier TO_MAX_FIRE_RESISTANCE_WHILE_SHAPESHIFTED = id("to_max_fire_resistance_while_shapeshifted");

    // +1 to Maximum [Charges|Frenzy Charges]
    public static final Identifier TO_MAX_FRENZY_CHARGES = id("to_max_frenzy_charges");

    // +20 to maximum Life
    public static final Identifier TO_MAX_HEALTH = id("to_max_health");

    // +1 to maximum [ElementalInfusion|Lightning Infusions]
    public static final Identifier TO_MAX_LIGHTNING_INFUSIONS = id("to_max_lightning_infusions");

    // +1% to [MaximumResistances|Maximum Lightning Resistance]
    public static final Identifier TO_MAX_LIGHTNING_RESISTANCE = id("to_max_lightning_resistance");

    // +2% to [MaximumResistances|Maximum Lightning Resistance] if you have at least 5 Green [SupportGem|Support Gems] Socketed
    public static final Identifier TO_MAX_LIGHTNING_RESISTANCE_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED = id("to_max_lightning_resistance_if_you_have_at_least_green_support_gems_socketed");

    // +1% to [MaximumResistances|Maximum Lightning Resistance] while [Shapeshift|Shapeshifted]
    public static final Identifier TO_MAX_LIGHTNING_RESISTANCE_WHILE_SHAPESHIFTED = id("to_max_lightning_resistance_while_shapeshifted");

    // +30 to maximum Mana
    public static final Identifier TO_MAX_MANA = id("to_max_mana");

    // +1 to maximum number of [ElementalInfusion|Elemental Infusions]
    public static final Identifier TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS = id("to_max_number_of_elemental_infusions");

    // +1 to maximum number of placed [Banner|Banners]
    public static final Identifier TO_MAX_NUMBER_OF_PLACED_BANNERS = id("to_max_number_of_placed_banners");

    // +1 to Maximum [Charges|Power Charges]
    public static final Identifier TO_MAX_POWER_CHARGES = id("to_max_power_charges");

    // +2 to Maximum [Rage]
    public static final Identifier TO_MAX_RAGE = id("to_max_rage");

    // +3 to maximum [Rage] while [Shapeshift|Shapeshifted]
    public static final Identifier TO_MAX_RAGE_WHILE_SHAPESHIFTED = id("to_max_rage_while_shapeshifted");

    // +10 to Maximum [Rage|Rage] while wielding an [Axe|Axe]
    public static final Identifier TO_MAX_RAGE_WHILE_WIELDING_AN_AXE = id("to_max_rage_while_wielding_an_axe");

    // +1% to [MaximumResistances|Maximum Resistances] of each [ElementalDamage|Elemental] Damage Type you have been [HitDamage|Hit] with [Recently]
    public static final Identifier TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY = id("to_max_resistances_of_each_elemental_damage_type_you_have_been_hit_damage_with_recently");

    // +10 to [Strength]
    public static final Identifier TO_STRENGTH = id("to_strength");

    // +5 to [Strength|Strength] and [Dexterity|Dexterity]
    public static final Identifier TO_STRENGTH_AND_DEXTERITY = id("to_strength_and_dexterity");

    // +5 to [Strength|Strength] and [Intelligence|Intelligence]
    public static final Identifier TO_STRENGTH_AND_INTELLIGENCE = id("to_strength_and_intelligence");

    // +1 to [StunThreshold|Stun Threshold] per [Dexterity|Dexterity]
    public static final Identifier TO_STUN_THRESHOLD_PER_DEXTERITY = id("to_stun_threshold_per_dexterity");

    // +3 to [StunThreshold|Stun Threshold] per [Strength|Strength]
    public static final Identifier TO_STUN_THRESHOLD_PER_STRENGTH = id("to_stun_threshold_per_strength");

    // +6% to Thorns [Critical|Critical Hit] Chance
    public static final Identifier TO_THORNS_CRITICAL_HIT_CHANCE = id("to_thorns_critical_hit_chance");

    // [Trigger|Triggered] [Spell|Spells] deal 14% increased [Spell] Damage
    public static final Identifier TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE = id("triggered_spells_deal_inc_spell_damage");

    // Triple [Attributes|Attribute] requirements of [MartialWeapon|Martial Weapons]
    public static final Identifier TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS = id("triple_attributes_requirements_of_martial_weapons");

    // Unaffected by [ChilledGround|Chilled Ground]
    public static final Identifier UNAFFECTED_BY_CHILLED_GROUND = id("unaffected_by_chilled_ground");

    // [Unaffected] by [Chill] during Dodge Roll
    public static final Identifier UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL = id("unaffected_by_chill_during_dodge_roll");

    // [Unaffected] by [Chill] while [ManaLeech|Leeching] Mana
    public static final Identifier UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA = id("unaffected_by_chill_while_mana_leech_mana");

    // Unaffected by [ElementalWeakness|Elemental Weakness]
    public static final Identifier UNAFFECTED_BY_ELEMENTAL_WEAKNESS = id("unaffected_by_elemental_weakness");

    // Unaffected by [ShockedGround|Shocked Ground]
    public static final Identifier UNAFFECTED_BY_SHOCKED_GROUND = id("unaffected_by_shocked_ground");

    // [UnarmedAttack|Unarmed Attacks] that would use an [Equipped] [Quarterstaff]'s damage have: Base [UnarmedDamage|Unarmed] [Physical] damage replaced with damage based on their Skill Level
    public static final Identifier UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL = id("unarmed_attacks_that_would_use_an_equipped_quarterstaff_s_damage_have_base_unarmed_damage_physical_damage_replaced_with_damage_based_on_their_skill_level");

    // [UnarmedAttack|Unarmed Attacks] that would use an [Equipped] [Quarterstaff]'s damage have: 1% more [Attack] Speed per 75 [ItemEvasion|Item Evasion] on [EquipArmour|Equipped Armour Items]
    public static final Identifier UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS = id("unarmed_attacks_that_would_use_an_equipped_quarterstaff_s_damage_have_more_attack_speed_per_item_evasion_on_equipped_armor_items");

    // [UnarmedAttack|Unarmed Attacks] that would use an [Equipped] [Quarterstaff]'s damage have: +0.1% to [Critical|Critical Hit Chance] per 10 [ItemEnergyShield|Item Energy Shield] on [EquipArmour|Equipped Armour Items]
    public static final Identifier UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS = id("unarmed_attacks_that_would_use_an_equipped_quarterstaff_s_damage_have_to_critical_hit_chance_per_item_energy_shield_on_equipped_armor_items");

    // Unwithered enemies are [Withered] for 8 seconds when they enter your [Presence]
    public static final Identifier UNWITHERED_ENEMIES_ARE_WITHERED_FOR_SECONDS_WHEN_THEY_ENTER_YOUR_PRESENCE = id("unwithered_enemies_are_withered_for_seconds_when_they_enter_your_presence");

    // [Volatility|Volatile Power] also grants 1% increased [Critical|Critical Hit chance] per [Volatility] exploded
    public static final Identifier VOLATILE_POWER_ALSO_GRANTS_INC_CRITICAL_HIT_CHANCE_PER_VOLATILITY_EXPLODED = id("volatile_power_also_grants_inc_critical_hit_chance_per_volatility_exploded");

    // [Warcry|Warcries] [Debilitate] Enemies
    public static final Identifier WARCRIES_DEBILITATE_ENEMIES = id("warcries_debilitate_enemies");

    // [Warcry|Warcries] [Empowered|Empower] an additional [Attack]
    public static final Identifier WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK = id("warcries_empowered_an_additional_attack");

    // Warcries have a minimum of 10 Power
    public static final Identifier WARCRIES_HAVE_A_MINIMUM_OF_POWER = id("warcries_have_a_minimum_of_power");

    // [Warcry|Warcries] have 15% chance to [Empowered|Empower] 3 additional [Attack|Attacks]
    public static final Identifier WARCRIES_HAVE_CHANCE_TO_EMPOWERED_ADDITIONAL_ATTACKS = id("warcries_have_chance_to_empowered_additional_attacks");

    // [Warcry|Warcries] inflict 3 [CriticalWeakness|Critical Weakness] on Enemies
    public static final Identifier WARCRIES_INFLICT_CRITICAL_WEAKNESS_ON_ENEMIES = id("warcries_inflict_critical_weakness_on_enemies");

    // [Warcry|Warcry] Skills have 25% increased Area of Effect
    public static final Identifier WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT = id("warcry_skills_have_inc_area_of_effect");

    // When a [Banner] expires, recover 15% of the [Glory] required for that Banner
    public static final Identifier WHEN_A_BANNER_EXPIRES_RECOVER_OF_THE_GLORY_REQUIRED_FOR_THAT_BANNER = id("when_a_banner_expires_recover_of_the_glory_required_for_that_banner");

    // When you [Shapeshift] to Human form, gain 10% increased [Spell] Damage per second you were [Shapeshift|Shapeshifted], up to a maximum of 80%, for 8 seconds
    public static final Identifier WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_GAIN_INC_SPELL_DAMAGE_PER_SECOND_YOU_WERE_SHAPESHIFTED_UP_TO_A_MAX_OF_FOR_SECONDS = id("when_you_shapeshift_to_human_form_gain_inc_spell_damage_per_second_you_were_shapeshifted_up_to_a_max_of_for_seconds");

    // Your [Aura] [Buff|Buffs] do not affect [Allies]
    public static final Identifier YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES = id("your_aura_buffs_do_not_affect_allies");

    // Your [Curse|Curses] have 20% increased [BuffMagnitude|Magnitudes] if 50% of [Curse] Duration expired
    public static final Identifier YOUR_CURSES_HAVE_INC_BUFF_MAGNITUDE_IF_OF_CURSE_DURATION_EXPIRED = id("your_curses_have_inc_buff_magnitude_if_of_curse_duration_expired");

    // Your [HeavyStunPlayer|Heavy Stun] buildup empties 50% faster
    public static final Identifier YOUR_HEAVY_STUN_BUILDUP_EMPTIES_FASTER = id("your_heavy_stun_buildup_empties_faster");

    // Your [HeavyStunPlayer|Heavy Stun] buildup empties 50% faster if you've successfully [Parry|Parried] [Recently]
    public static final Identifier YOUR_HEAVY_STUN_BUILDUP_EMPTIES_FASTER_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY = id("your_heavy_stun_buildup_empties_faster_if_you_ve_successfully_parried_recently");

    // Your Hits cannot be [Evasion|Evaded] by [Pinned] Enemies
    public static final Identifier YOUR_HITS_CANNOT_BE_EVASION_BY_PINNED_ENEMIES = id("your_hits_cannot_be_evasion_by_pinned_enemies");

    // Your [HitDamage|Hits] cannot be [Evasion|Evaded] by [HeavyStun|Heavy Stunned] Enemies
    public static final Identifier YOUR_HIT_DAMAGE_CANNOT_BE_EVASION_BY_HEAVY_STUNNED_ENEMIES = id("your_hit_damage_cannot_be_evasion_by_heavy_stunned_enemies");

    // Your Minions are [Gigantic]
    public static final Identifier YOUR_MINIONS_ARE_GIGANTIC = id("your_minions_are_gigantic");

    // Your [Totem] [Limit] is doubled
    public static final Identifier YOUR_TOTEM_LIMIT_IS_DOUBLED = id("your_totem_limit_is_doubled");

    // You cannot be [Electrocute|Electrocuted]
    public static final Identifier YOU_CANNOT_BE_ELECTROCUTED = id("you_cannot_be_electrocuted");

    // You cannot be [LightStun|Light Stunned] if you've been [Stun|Stunned] [Recently]
    public static final Identifier YOU_CANNOT_BE_LIGHT_STUNNED_IF_YOU_VE_BEEN_STUNNED_RECENTLY = id("you_cannot_be_light_stunned_if_you_ve_been_stunned_recently");

    // You cannot Recover [EnergyShield|Energy Shield] from Regeneration
    public static final Identifier YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION = id("you_cannot_recover_energy_shield_from_regeneration");

    // You cannot Recover [EnergyShield|Energy Shield] to above [Armour]
    public static final Identifier YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR = id("you_cannot_recover_energy_shield_to_above_armor");

    // You can apply an additional [Curse]
    public static final Identifier YOU_CAN_APPLY_AN_ADDITIONAL_CURSE = id("you_can_apply_an_additional_curse");

    // You can equip a non-[ItemRarity|Unique] [Sceptre] while wielding a [Talisman]
    public static final Identifier YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN = id("you_can_equip_a_non_item_rarity_sceptre_while_wielding_a_talisman");

    // You can have two [Companion|Companions] of different types
    public static final Identifier YOU_CAN_HAVE_TWO_COMPANIONS_OF_DIFFERENT_TYPES = id("you_can_have_two_companions_of_different_types");

    // You can wield [Two-Handed] [Axe|Axes], [Mace|Maces] and [Sword|Swords] in one hand
    public static final Identifier YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND = id("you_can_wield_two_handed_axes_maces_and_swords_in_one_hand");

    // You have no Mana
    public static final Identifier YOU_HAVE_NO_MANA = id("you_have_no_mana");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }

    /**
     * 按 stat_entry 名（即真实路径，含连字符）查缓存路径：供动态生成点（异常面 / status 面循环、offering 等）
     * 经字段缓存引用，替代裸 {@code Identifier.fromNamespaceAndPath(ns, "ailment_buildup_"+t.id)}。
     * 缺失即抛错（fail-fast），避免拼写漂移导致的静默悬空。
     */
    private static final Map<Identifier, Identifier> BY_NAME = new HashMap<>();

    // 注：填充 BY_NAME 的反射静态块已移至类末（所有常量字段声明之后），见文件末尾；
    // 新增字段声明在该静态块之前或之后均可被反射索引。

    public static Identifier byName(String name) {
        Identifier r = BY_NAME.get(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name));
        if (r == null) {
            throw new IllegalStateException("StatIds 未缓存 stat_entry: " + name);
        }
        return r;
    }



    // ── 原 ModStatIds 并入（词缀专用 stat 路径；合并入 StatIds 以消除双注册表键冲突）──
    public static final Identifier INC_SOCKETED_GEM_LEVEL = id("inc_socketed_gem_level");
    public static final Identifier INC_SPELL_CRIT_CHANCE = id("inc_spell_crit_chance");
    public static final Identifier THORNS = id("thorns");
    public static final Identifier ADD_PHYSICAL_DAMAGE_REDUCTION = id("add_physical_damage_reduction");
    public static final Identifier INC_PHYSICAL_DAMAGE_REDUCTION = id("inc_physical_damage_reduction");
    public static final Identifier DAMAGE_TAKEN_GAINED_AS_LIFE = id("damage_taken_gained_as_life");
    public static final Identifier REDUCED_EXTRA_DAMAGE_FROM_CRITS = id("reduced_extra_damage_from_crits");
    public static final Identifier REDUCED_PHYSICAL_DAMAGE_TAKEN = id("reduced_physical_damage_taken");
    public static final Identifier REDUCED_AILMENT_DURATION = id("reduced_ailment_duration");
    public static final Identifier LIFE_GAINED_FROM_ENEMY_DEATH = id("life_gained_from_enemy_death");
    public static final Identifier MANA_GAINED_FROM_ENEMY_DEATH = id("mana_gained_from_enemy_death");
    public static final Identifier LIFE_GAIN_PER_TARGET = id("life_gain_per_target");
    public static final Identifier LIFE_GAIN_PER_TARGET_ATTACKS = id("life_gain_per_target_attacks");
    public static final Identifier PERCENT_DAMAGE_GOES_TO_MANA = id("percent_damage_goes_to_mana");
    public static final Identifier PRESENCE_RADIUS = id("presence_radius");
    public static final Identifier PRESENCE_CRIT_CHANCE = id("presence_crit_chance");
    public static final Identifier PRESENCE_CRIT_DAMAGE = id("presence_crit_damage");
    public static final Identifier PRESENCE_ATTACK_SPEED = id("presence_attack_speed");
    public static final Identifier PRESENCE_CAST_SPEED = id("presence_cast_speed");
    public static final Identifier PRESENCE_LIFE_REGEN = id("presence_life_regen");
    public static final Identifier PRESENCE_DAMAGE = id("presence_damage");
    public static final Identifier PRESENCE_COLD_DAMAGE = id("presence_cold_damage");
    public static final Identifier PRESENCE_FIRE_DAMAGE = id("presence_fire_damage");
    public static final Identifier PRESENCE_LIGHTNING_DAMAGE = id("presence_lightning_damage");
    public static final Identifier PRESENCE_PHYSICAL_DAMAGE = id("presence_physical_damage");
    public static final Identifier PRESENCE_ALL_ELEMENTAL_RESISTANCES = id("presence_all_elemental_resistances");
    public static final Identifier ADD_COLD_DAMAGE_TO_ATTACKS = id("add_cold_damage_to_attacks");
    public static final Identifier ADD_FIRE_DAMAGE_TO_ATTACKS = id("add_fire_damage_to_attacks");
    public static final Identifier ADD_LIGHTNING_DAMAGE_TO_ATTACKS = id("add_lightning_damage_to_attacks");
    public static final Identifier ADD_PHYSICAL_DAMAGE_TO_ATTACKS = id("add_physical_damage_to_attacks");
    public static final Identifier GAIN_COLD_DAMAGE_AS_EXTRA = id("gain_cold_damage_as_extra");
    public static final Identifier GAIN_FIRE_DAMAGE_AS_EXTRA = id("gain_fire_damage_as_extra");
    public static final Identifier GAIN_LIGHTNING_DAMAGE_AS_EXTRA = id("gain_lightning_damage_as_extra");
    public static final Identifier GAIN_PHYSICAL_DAMAGE_AS_EXTRA = id("gain_physical_damage_as_extra");
    public static final Identifier ARMOUR_APPLIES_ELEMENTAL_DAMAGE = id("armour_applies_elemental_damage");
    public static final Identifier EVASION_APPLIES_TO_DEFLECTION = id("evasion_applies_to_deflection");
    public static final Identifier LIGHT_RADIUS = id("light_radius");
    public static final Identifier TRAP_AND_MINE_THROW_SPEED = id("trap_and_mine_throw_speed");
    public static final Identifier INC_MINION_MAX_LIFE = id("inc_minion_max_life");
    public static final Identifier INC_DAMAGE_WITH_BOW_SKILLS = id("inc_damage_with_bow_skills");
    public static final Identifier INC_FLAMMABILITY_MAGNITUDE = id("inc_flammability_magnitude");
    public static final Identifier INC_MAX_SPIRIT = id("inc_max_spirit");
    public static final Identifier REDUCED_ATTRIBUTE_REQUIREMENTS = id("reduced_attribute_requirements");
    public static final Identifier REDUCED_CHILL_DURATION = id("reduced_chill_duration");
    public static final Identifier REDUCED_FREEZE_DURATION = id("reduced_freeze_duration");
    public static final Identifier REDUCED_IGNITE_DURATION = id("reduced_ignite_duration");
    public static final Identifier REDUCED_POISON_DURATION = id("reduced_poison_duration");
    public static final Identifier REDUCED_BLEEDING_DURATION = id("reduced_bleeding_duration");
    public static final Identifier REDUCED_SHOCK_DURATION = id("reduced_shock_duration");
    public static final Identifier BELT_CHARM_DURATION = id("belt_charm_duration");
    public static final Identifier BELT_FLASK_RECOVERY_RATE = id("belt_flask_recovery_rate");
    public static final Identifier BELT_CHARM_CHARGES_GAINED = id("belt_charm_charges_gained");
    public static final Identifier BELT_CHARM_CHARGES_USED = id("belt_charm_charges_used");
    public static final Identifier BELT_FLASK_CHARGES_GAINED = id("belt_flask_charges_gained");
    public static final Identifier BELT_FLASK_CHARGES_USED = id("belt_flask_charges_used");
    public static final Identifier BELT_FLASK_LIFE_RECOVERY_RATE = id("belt_flask_life_recovery_rate");
    public static final Identifier BELT_FLASK_MANA_RECOVERY_RATE = id("belt_flask_mana_recovery_rate");
    public static final Identifier FLASK_RECOVERY_AMOUNT = id("flask_recovery_amount");
    public static final Identifier FLASK_RECOVERY_SPEED = id("flask_recovery_speed");
    public static final Identifier FLASK_CHARGES_USED = id("flask_charges_used");
    public static final Identifier FLASK_GAIN_CHARGE = id("flask_gain_charge");
    public static final Identifier FLASK_NUM_CHARGES = id("flask_num_charges");
    public static final Identifier FLASK_RECHARGE_RATE = id("flask_recharge_rate");
    public static final Identifier FLASK_CHANCE_GAIN_CHARGE_ON_KILL = id("flask_chance_gain_charge_on_kill");
    public static final Identifier FLASK_CHARGES_GAINED = id("flask_charges_gained");
    public static final Identifier FLASK_GRANT_GUARD = id("flask_grant_guard");
    public static final Identifier FLASK_RECOVER_LIFE = id("flask_recover_life");
    public static final Identifier FLASK_RECOVER_MANA = id("flask_recover_mana");
    public static final Identifier CHARM_DURATION = id("charm_duration");
    public static final Identifier CHARM_CHARGES_GAINED = id("charm_charges_gained");
    public static final Identifier CHARM_CHARGES_USED = id("charm_charges_used");
    public static final Identifier CHARM_GAIN_CHARGE = id("charm_gain_charge");
    public static final Identifier CHARM_GRANT_GUARD = id("charm_grant_guard");
    public static final Identifier SURPASSING_CHANCE_ADDITIONAL_ARROW = id("surpassing_chance_additional_arrow");
    public static final Identifier LEECH_PHYSICAL_ATTACK_DAMAGE_AS_LIFE = id("leech_physical_attack_damage_as_life");
    public static final Identifier LEECH_PHYSICAL_DAMAGE_AS_LIFE = id("leech_physical_damage_as_life");
    public static final Identifier LEECH_PHYSICAL_DAMAGE_AS_MANA = id("leech_physical_damage_as_mana");

    static {
        for (var f : StatIds.class.getDeclaredFields()) {
            if (f.getType() == Identifier.class) {
                try {
                    Identifier id = (Identifier) f.get(null);
                    // 按真实路径（含连字符，如 livingmod_..._op_add_value_-0_8）建索引，
                    // 与字段名脱钩——字段名已 sanitize（'-' -> '_'）以满足 Java 标识符规则。
                    BY_NAME.put(id, id);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }
}
