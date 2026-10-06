# A4-v3 词缀注入面单位审计（AdditionType → stat_entry）

词缀 roll 值经 `AdditionType.Effect.toStatEntry(rolled)` 注入 `vars["value"]`，
故条目 amount 约定必须与 roll 表单位一致。

| 判定 | 条数 | 含义 |
|---|---|---|
| BUG-DIV | 14 | 条目烘 `/100` + roll 传小数 → **缩小 100 倍** |
| BUG-MUL | 0 | 条目裸 `$value` + roll 传整数点 → **放大 100 倍** |
| CHECK | 0 | roll 单位混杂/不可判 → 人工 |
| OK | 61 | 自洽 |

## BUG-DIV（14 条）

| AdditionType | stat id | amount | 属性 | op | roll 单位 | roll 范围 | 定义文件 |
|---|---|---|---|---|---|---|---|
| `ADD_STUN_THRESHOLD` | `add_stun_threshold` | `$value / 100` | `coe:ailment_threshold` | add_value | flat | 6.0~352.0 | SuffixMod.java |
| `ATTACK_CRIT_CHANCE_INC` | `inc_crit_chance` | `$value / 100` | `coe:crit_chance` | add_multiplied_base | fraction | 0.1~0.34 | SuffixMod.java |
| `DAMAGE_PRESENCE_ALLIES_INC` | `inc_damage_presence_allies` | `$value / 100` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_PRESENCE_ENEMIES_INC` | `inc_damage_presence_enemies` | `$value / 100` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DEXTERITY_INC` | `inc_dexterity` | `$value / 100` | `coe:dexterity` | add_multiplied_total | fraction | 0.04~0.2 | SuffixMod.java |
| `INTELLIGENCE_INC` | `inc_dexterity` | `$value / 100` | `coe:dexterity` | add_multiplied_total | fraction | 0.04~0.2 | SuffixMod.java |
| `MINION_ATTACK_SPEED_INC` | `inc_minion_attack_speed` | `$value / 100` | `minecraft:attack_speed` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MINION_DAMAGE_INC` | `inc_minion_damage` | `$value / 100` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MINION_ELEMENTAL_RESIST_INC` | `inc_minion_elemental_resist` | `$value / 100` | `coe:resistances` | add_value | fraction | 0.04~0.2 | SuffixMod.java |
| `MINION_MAX_HEALTH_INC` | `inc_minion_max_health` | `$value / 100` | `minecraft:max_health` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MINION_MOVEMENT_SPEED_INC` | `inc_minion_movement_speed` | `$value / 100` | `minecraft:movement_speed` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `OFFERING_DAMAGE_INC` | `inc_minion_damage` | `$value / 100` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `RESERVATION_EFFICIENCY_INC` | `inc_reservation_efficiency` | `$value / 100` | `coe:reservation_efficiency` | add_value | fraction | 0.04~0.2 | SuffixMod.java |
| `STRENGTH_INC` | `inc_strength` | `$value / 100` | `coe:strength` | add_multiplied_total | fraction | 0.04~0.2 | SuffixMod.java |

## OK（61 条）

| AdditionType | stat id | amount | 属性 | op | roll 单位 | roll 范围 | 定义文件 |
|---|---|---|---|---|---|---|---|
| `ADD_ACCURACY` | `add_accuracy` | `$value` | `coe:accuracy` | add_value | flat | 10.0~550.0 | PrefixMod.java |
| `ADD_CHAOS_RESISTANCE` | `add_chaos_resistance` | `$value` | `coe:resistances` | add_value | fraction | 0.04~0.27 | SuffixMod.java |
| `ADD_COLD_RESISTANCE` | `add_cold_resistance` | `$value` | `coe:resistances` | add_value | fraction | 0.06~0.45 | SuffixMod.java |
| `ADD_DEXTERITY` | `add_dexterity` | `$value` | `coe:dexterity` | add_value | flat | 5.0~33.0 | SuffixMod.java |
| `ADD_FIRE_RESISTANCE` | `add_fire_resistance` | `$value` | `coe:resistances` | add_value | fraction | 0.06~0.45 | SuffixMod.java |
| `ADD_HEALTH_REGENERATION_PER_SECOND` | `add_health_regeneration_rate` | `$value` | `coe:regeneration_rate` | add_value | flat | 1.0~36.0 | SuffixMod.java |
| `ADD_INTELLIGENCE` | `add_intelligence` | `$value` | `coe:intelligence` | add_value | flat | 5.0~33.0 | SuffixMod.java |
| `ADD_LIGHT_RESISTANCE` | `add_lightning_resistance` | `$value` | `coe:resistances` | add_value | fraction | 0.06~0.45 | SuffixMod.java |
| `ADD_MAX_HEALTH` | `add_max_health` | `$value` | `minecraft:max_health` | add_value | flat | 10.0~214.0 | PrefixMod.java |
| `ADD_MAX_MANA` | `add_max_mana` | `$value` | `coe:max_mana` | add_value | flat | 10.0~124.0 | PrefixMod.java |
| `ADD_STRENGTH` | `add_strength` | `$value` | `coe:strength` | add_value | flat | 5.0~33.0 | SuffixMod.java |
| `AILMENT_MAGNITUDE_INC` | `inc_ailment_magnitude` | `$value` | `coe:ailment_magnitude` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `ATTACK_ADD_COLD_DAMAGE` | `add_cold_damage` | `$value` | `coe:added_damage` | add_value | flat | 1.0~63.0 | PrefixMod.java |
| `ATTACK_ADD_FIRE_DAMAGE` | `add_fire_damage` | `$value` | `coe:added_damage` | add_value | flat | 1.0~78.0 | PrefixMod.java |
| `ATTACK_ADD_LIGHT_DAMAGE` | `add_light_damage` | `$value` | `coe:added_damage` | add_value | flat | 1.0~10.0 | PrefixMod.java |
| `ATTACK_ADD_PHYSICAL_DAMAGE` | `add_attack_damage` | `$value` | `minecraft:attack_damage` | add_value | flat | 1.0~19.0 | PrefixMod.java |
| `ATTACK_CRIT_DAMAGE` | `add_crit_damage` | `$value` | `coe:crit_damage` | add_value | fraction | 0.1~0.39 | SuffixMod.java |
| `ATTACK_DAMAGE_INC` | `inc_attack_damage` | `$value` | `minecraft:attack_damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `ATTACK_ELEMENTAL_DAMAGE_INC` | `inc_fire_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.19~1.0 | PrefixMod.java |
| `ATTACK_PHYSICAL_DAMAGE_INC` | `inc_attack_damage_physical` | `$value` | `minecraft:attack_damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `ATTACK_SPEED_INC` | `inc_attack_speed` | `$value` | `minecraft:attack_speed` | add_multiplied_base | fraction | 0.05~0.16 | SuffixMod.java |
| `BANNER_DAMAGE_INC` | `inc_banner_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `BLEEDING_DURATION_ON_YOU_INC` | `inc_bleeding_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `BUFF_EFFECT_TAKEN_INC` | `inc_buff_effect_taken` | `$value` | `coe:buff_effect_taken` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `BUFF_MAGNITUDE_INC` | `inc_buff_magnitude` | `$value` | `coe:ailment_magnitude` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `CAUSES_STUN_BUILDUP_INC` | `inc_stun_buildup` | `$value` | `coe:ailment_buildup` | add_multiplied_base | fraction | 0.21~0.8 | SuffixMod.java |
| `CHAOS_DAMAGE_INC` | `inc_chaos_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `CHILL_DURATION_ON_YOU_INC` | `inc_chill_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `COLD_DAMAGE_INC` | `inc_cold_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `CURSE_DAMAGE_INC` | `inc_curse_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_BLOCKING_INC` | `inc_damage_blocking` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_LOW_LIFE_INC` | `inc_damage_low_life` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_PARRYING_INC` | `inc_damage_parrying` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_RECENTLY_KILLED_INC` | `inc_damage_recently_killed` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `DAMAGE_SHAPESHIFTED_INC` | `inc_damage_shapeshifted` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `ENERGY_RECHARGE_RATE_INC` | `inc_energy_regeneration_rate` | `$value` | `coe:regeneration_rate` | add_multiplied_base | fraction | 0.26~0.5 | SuffixMod.java |
| `FIRE_DAMAGE_INC` | `inc_fire_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `FOUND_ITEM_RARITY_INC` | `add_found_item_rarity` | `$value` | `coe:found_item_rarity` | add_value | fraction | 0.06~0.25 | PrefixMod.java |
| `FREEZE_DURATION_ON_YOU_INC` | `inc_freeze_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `GLORY_GENERATION_INC` | `inc_glory_generation` | `$value` | `coe:glory_generation` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `GRENADE_DAMAGE_INC` | `inc_grenade_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `HAZARD_DAMAGE_INC` | `inc_hazard_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `HERALD_DAMAGE_INC` | `inc_herald_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `IGNITE_DURATION_ON_YOU_INC` | `inc_ignite_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `INFUSION_DURATION_INC` | `inc_infusion_duration` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `INFUSION_MAGNITUDE_INC` | `inc_infusion_magnitude` | `$value` | `coe:ailment_magnitude` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `LIGHT_DAMAGE_INC` | `inc_light_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.25~1.19 | PrefixMod.java |
| `MANA_REGENERATION_RATE_INC` | `inc_mana_regeneration_rate` | `$value` | `coe:regeneration_rate` | add_multiplied_base | fraction | 0.1~0.69 | SuffixMod.java |
| `MARK_DAMAGE_INC` | `inc_mark_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MAX_INFUSION_INC` | `inc_infusion_duration` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MAX_VOLATILITY_INC` | `inc_max_volatility` | `$value` | `coe:max_volatility` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `META_DAMAGE_INC` | `inc_meta_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `MOVEMENT_SPEED_STATIONARY_INC` | `inc_movement_speed_stationary` | `$value` | `minecraft:movement_speed` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `NON_DAMAGING_AILMENT_MAGNITUDE_INC` | `inc_non_damaging_ailment_magnitude` | `$value` | `coe:ailment_magnitude` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `POISON_DURATION_ON_YOU_INC` | `inc_poison_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `SHOCK_DURATION_ON_YOU_INC` | `inc_shock_duration_taken` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | -0.6~-0.36 | SuffixMod.java |
| `STUN_DURATION_INC` | `inc_stun_duration` | `$value` | `coe:ailment_duration` | add_multiplied_base | fraction | 0.11~0.3 | SuffixMod.java |
| `TAKEN_CRIT_DAMAGE_INC` | `inc_taken_crit_damage` | `$value` | `coe:taken_crit_damage` | add_multiplied_base | fraction | -0.54~-0.21 | SuffixMod.java |
| `VOLATILITY_DETONATION_TIME_INC` | `inc_max_volatility` | `$value` | `coe:max_volatility` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `VOLATILITY_GENERATION_INC` | `inc_volatility_generation` | `$value` | `coe:volatility_generation` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |
| `WARCRY_DAMAGE_INC` | `inc_warcry_damage` | `$value` | `coe:damage` | add_multiplied_base | fraction | 0.04~0.2 | SuffixMod.java |

## 无 roll 表（53 条，未被任何 Mod 引用或 roll 形态未识别）

| AdditionType | stat id | amount | 属性 |
|---|---|---|---|
| `ADD_ELEMENTAL_DAMAGE` | `add_elemental_damage` | `$value` | `coe:added_damage` |
| `ADD_ELEMENTAL_RESISTANCE` | `add_fire_resistance` | `$value` | `coe:resistances` |
| `ADD_MAX_CHAOS_RESISTANCE` | `add_max_chaos_resistance` | `$value` | `coe:max_resistances` |
| `ADD_MAX_COLD_RESISTANCE` | `add_max_cold_resistance` | `$value` | `coe:max_resistances` |
| `ADD_MAX_ELEMENTAL_RESISTANCE` | `add_max_fire_resistance` | `$value` | `coe:max_resistances` |
| `ADD_MAX_FIRE_RESISTANCE` | `add_max_fire_resistance` | `$value` | `coe:max_resistances` |
| `ADD_MAX_FRENZY_CHARGES` | `add_max_frenzy` | `$value` | `coe:max_frenzy_charges` |
| `ADD_MAX_LIGHT_RESISTANCE` | `add_max_lightning_resistance` | `$value` | `coe:max_resistances` |
| `ADD_MAX_POWER_CHARGES` | `add_max_power` | `$value` | `coe:max_power_charges` |
| `ADD_PHYSICAL_DAMAGE` | `add_physical_damage` | `$value` | `coe:added_damage` |
| `AILMENT_MAGNITUDE_FREEZE` | `ailment_magnitude_freeze` | `$value` | `coe:ailment_magnitude` |
| `ATTACK_ADD_CHAOS_DAMAGE` | `add_chaos_damage` | `$value` | `coe:added_damage` |
| `ATTACK_ELEMENTAL_SKILL_DAMAGE_INC` | `inc_fire_damage_spell` | `$value` | `coe:damage` |
| `BLEEDING_CHANCE_ON_ATTACK_HIT` | `add_ailment_chance_bleed` | `$value` | `coe:ailment_chance` |
| `BLEEDING_DAMAGE_INC` | `inc_ailment_damage_rate_bleed` | `$value` | `coe:ailment_damage_rate` |
| `CHANCE_TO_CHILL_ON_HIT` | `chance_chill` | `$value` | `coe:ailment_chance` |
| `CHANCE_TO_DODGE` | `chance_dodge` | `$value` | `coe:dodge_chance` |
| `CHANCE_TO_FREEZE_ON_HIT` | `chance_freeze` | `$value` | `coe:ailment_chance` |
| `CHANCE_TO_IGNITE_ON_HIT` | `chance_ignite` | `$value` | `coe:ailment_chance` |
| `CHANCE_TO_MAIM_ON_HIT` | `add_ailment_chance_maim` | `$value` | `coe:ailment_chance` |
| `CHANCE_TO_POISON_ON_HIT` | `add_ailment_chance_poison` | `$value` | `coe:ailment_chance` |
| `CONVERT_EXTRA_PHYS_TO_FIRE` | `convert_extra_phys_to_fire` | `$value` | `` |
| `DAMAGE_OVER_TIME_INC` | `inc_damage_over_time` | `$value / 100` | `coe:damage_over_time_multiplier` |
| `ELEMENTAL_AILMENT_THRESHOLD_INC` | `inc_elemental_ailment_threshold` | `$value / 100` | `coe:ailment_threshold` |
| `ENERGY_RECHARGE_START_FASTER` | `add_energy_recharge` | `$value` | `coe:energy_recharge` |
| `EXTRA_CHAOS_DAMAGE` | `extra_chaos_from_chaos` | `$value` | `` |
| `EXTRA_COLD_DAMAGE` | `extra_cold_from_chaos` | `$value` | `` |
| `EXTRA_FIRE_DAMAGE` | `extra_fire_from_physical` | `$value` | `` |
| `EXTRA_LIGHTNING_DAMAGE` | `extra_lightning_from_physical` | `$value` | `` |
| `EXTRA_PHYSICAL_DAMAGE` | `extra_physical_from_physical` | `$value` | `` |
| `FLASK_MANA_RECOVERY` | `flask_mana_recovery` | `$value / 100` | `coe:flask_recovery_rate` |
| `FOUND_ITEM_NUMBER_INC` | `add_found_item_number` | `$value` | `coe:found_item_number` |
| `FOUND_ITEM_QUANTITY` | `inc_found_quantity` | `$value / 100` | `coe:found_item_number` |
| `FOUND_ITEM_RARITY` | `inc_found_rarity` | `$value / 100` | `coe:found_item_rarity` |
| `GLOBAL_ACCURACY_INC` | `inc_accuracy` | `$value` | `coe:accuracy` |
| `GLOBAL_CRIT_DAMAGE` | `add_crit_damage` | `$value` | `coe:crit_damage` |
| `HEALTH_REGENERATION_PERCENTAGE_PER_SECOND` | `add_max_health_regeneration` | `$value` | `coe:max_regeneration` |
| `LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | `$value` | `coe:launch_additional_projectile` |
| `LAUNCH_ADDITIONAL_PROJECTILE_BOW` | `add_launch_additional_projectile_bow` | `$value` | `coe:launch_additional_projectile` |
| `LEECH_HEALTH_INC` | `leech_health` | `$value / 100` | `coe:leech` |
| `LEECH_MANA_INC` | `leech_mana` | `$value / 100` | `coe:leech` |
| `MAX_HEALTH_INC` | `inc_max_health` | `$value` | `minecraft:max_health` |
| `MINION_ATTACK_SPEED` | `minion_attack_speed` | `$value / 100` | `minecraft:attack_speed` |
| `MOVEMENT_SPEED_INC` | `inc_movement_speed` | `$value` | `minecraft:movement_speed` |
| `PENETRATE_CHAOS` | `penetrate_chaos` | `$value / 100` | `coe:resistance_penetration` |
| `PHYSICAL_TAKEN_AS_CHAOS` | `physical_taken_as_chaos` | `$value` | `` |
| `PHYSICAL_TAKEN_AS_COLD` | `physical_taken_as_cold` | `$value` | `` |
| `PHYSICAL_TAKEN_AS_LIGHTNING` | `physical_taken_as_lightning` | `$value` | `` |
| `POISON_DAMAGE_INC` | `inc_ailment_damage_rate_poison` | `$value` | `coe:ailment_damage_rate` |
| `PROJECTILE_PIERCE` | `add_projectile_pierce` | `$value` | `coe:projectile_pierce` |
| `PROJECTILE_SPEED_INC` | `inc_projectile_speed` | `$value / 100` | `coe:projectile_speed` |
| `STUN_THRESHOLD_INC` | `inc_stun_threshold` | `$value / 100` | `coe:ailment_threshold` |
| `TAKEN_DAMAGE_INC` | `inc_taken_damage` | `$value` | `coe:taken_damage` |
