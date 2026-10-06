# 面6 装备预设注入面全量清单（56 处）

| 文件 | 行 | AdditionType | stat | 条目形态 | 属性 | 类型 | op | 数值 | 单位 | 期望 | 判定 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| EnchantModifierProvider.java | 25 | `PHYSICAL_DAMAGE_INC_LOCAL` | `?` | const |  | - |  | [0.15] | fraction | fraction | **-** |
| EnchantModifierProvider.java | 32 | `ATTACK_ADD_FIRE_DAMAGE` | `add_fire_damage` | bare | coe:added_damage | double | add_value | [60.0] | points | fraction | **-** |
| ChestItem.java | 18 | `PHYSICAL_TAKEN_AS_COLD` | `physical_taken_as_cold` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| ChestItem.java | 32 | `PHYSICAL_TAKEN_AS_LIGHTNING` | `physical_taken_as_lightning` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| ChestItem.java | 42 | `PHYSICAL_TAKEN_AS_CHAOS` | `physical_taken_as_chaos` | bare |  | - |  | [0.1] | fraction | fraction | **-** |
| ChestItem.java | 95 | `HEALTH_REGENERATION_PERCENTAGE_PER_SECOND` | `add_max_health_regeneration` | bare | coe:max_regeneration | double | add_value | [0.015, 0.025] | fraction | fraction | **-** |
| ChestItem.java | 312 | `ADD_MAX_HEALTH` | `add_max_health` | bare | minecraft:max_health | vanilla | add_value | [60.0, 80.0] | points | fraction | **-** |
| ChestItem.java | 380 | `ADD_MAX_SPIRIT` | `?` | const |  | - |  | [20.0, 30.0] | points | fraction | **-** |
| ChestItem.java | 448 | `MOVEMENT_SPEED_INC` | `inc_movement_speed` | bare | minecraft:movement_speed | vanilla | add_multiplied_base | [0.05] | fraction | fraction | **-** |
| ChestItem.java | 645 | `HEALTH_REGENERATION_PERCENTAGE_PER_SECOND` | `add_max_health_regeneration` | bare | coe:max_regeneration | double | add_value | [0.015, 0.025] | fraction | fraction | **-** |
| ChestItem.java | 695 | `ADD_MAX_HEALTH` | `add_max_health` | bare | minecraft:max_health | vanilla | add_value | [60.0, 80.0] | points | fraction | **-** |
| ChestItem.java | 719 | `ADD_MAX_SPIRIT` | `?` | const |  | - |  | [20.0, 30.0] | points | fraction | **-** |
| ChestItem.java | 745 | `MOVEMENT_SPEED_INC` | `inc_movement_speed` | bare | minecraft:movement_speed | vanilla | add_multiplied_base | [0.05] | fraction | fraction | **-** |
| HeadItem.java | 22 | `ADD_MAX_FRENZY_CHARGES` | `add_max_frenzy` | bare | coe:max_frenzy_charges | double | add_value | [1.0] | points | fraction | **-** |
| HeadItem.java | 32 | `ADD_MAX_POWER_CHARGES` | `add_max_power` | bare | coe:max_power_charges | double | add_value | [1.0] | points | fraction | **-** |
| HeadItem.java | 42 | `MINION_ATTACK_SPEED` | `minion_attack_speed` | bare | minecraft:attack_speed | vanilla | add_multiplied_base | [0.15] | fraction | fraction | **-** |
| AxeItem.java | 18 | `ADD_ELEMENTAL_DAMAGE` | `add_elemental_damage` | bare | coe:added_damage | double | add_value | [5.0] | points | fraction | **-** |
| BowItem.java | 78 | `LAUNCH_ADDITIONAL_PROJECTILE_BOW` | `add_launch_additional_projectile_bow` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| BowItem.java | 175 | `LAUNCH_ADDITIONAL_PROJECTILE_BOW` | `add_launch_additional_projectile_bow` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| BowItem.java | 242 | `LAUNCH_ADDITIONAL_PROJECTILE_BOW` | `add_launch_additional_projectile_bow` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| CrossBowItem.java | 50 | `EXTRA_FIRE_DAMAGE` | `extra_fire_from_physical` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| CrossBowItem.java | 70 | `EXTRA_COLD_DAMAGE` | `extra_cold_from_chaos` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| CrossBowItem.java | 82 | `LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| CrossBowItem.java | 209 | `LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| CrossBowItem.java | 276 | `LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | bare | coe:launch_additional_projectile | double | add_value | [1.0] | points | fraction | **-** |
| MaceItem.java | 26 | `EXTRA_LIGHTNING_DAMAGE` | `extra_lightning_from_physical` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| MaceItem.java | 36 | `EXTRA_CHAOS_DAMAGE` | `extra_chaos_from_chaos` | bare |  | - |  | [0.1] | fraction | fraction | **-** |
| MaceItem.java | 53 | `EXTRA_PHYSICAL_DAMAGE` | `extra_physical_from_physical` | bare |  | - |  | [0.1] | fraction | fraction | **-** |
| MaceItem.java | 63 | `CONVERT_EXTRA_PHYS_TO_FIRE` | `convert_extra_phys_to_fire` | bare |  | - |  | [0.15] | fraction | fraction | **-** |
| SwordItem.java | 18 | `ADD_PHYSICAL_DAMAGE` | `add_physical_damage` | bare | coe:added_damage | double | add_value | [8.0] | points | fraction | **-** |
| EnchantModifierProvider.java | 39 | `ADD_FIRE_RESISTANCE` | `add_fire_resistance` | bare | coe:resistances | percentage | add_value | [0.05] | fraction | fraction | **OK** |
| ChestItem.java | 88 | `STUN_THRESHOLD_INC` | `inc_stun_threshold` | bare | coe:ailment_threshold | double | add_value | [0.3, 0.4] | fraction | fraction | **OK** |
| ChestItem.java | 166 | `ADD_ELEMENTAL_RESISTANCE` | `add_fire_resistance` | bare | coe:resistances | percentage | add_value | [0.05, 0.08] | fraction | fraction | **OK** |
| ChestItem.java | 178 | `ELEMENTAL_AILMENT_THRESHOLD_INC` | `inc_elemental_ailment_threshold` | bare | coe:ailment_threshold | double | add_value | [0.3, 0.4] | fraction | fraction | **OK** |
| ChestItem.java | 249 | `MANA_REGENERATION_RATE_INC` | `inc_mana_regeneration_rate` | bare | coe:regeneration_rate | double | add_multiplied_base | [0.4, 0.5] | fraction | fraction | **OK** |
| ChestItem.java | 256 | `ENERGY_RECHARGE_START_FASTER` | `add_energy_recharge` | bare | coe:energy_recharge | percentage | add_value | [-0.4, -0.5] | fraction | fraction | **OK** |
| ChestItem.java | 373 | `ADD_MAX_ELEMENTAL_RESISTANCE` | `add_max_fire_resistance` | bare | coe:max_resistances | percentage | add_value | [0.01] | fraction | fraction | **OK** |
| ChestItem.java | 441 | `ADD_CHAOS_RESISTANCE` | `add_chaos_resistance` | bare | coe:resistances | percentage | add_value | [0.07, 0.13] | fraction | fraction | **OK** |
| ChestItem.java | 652 | `STUN_THRESHOLD_INC` | `inc_stun_threshold` | bare | coe:ailment_threshold | double | add_value | [0.3, 0.4] | fraction | fraction | **OK** |
| ChestItem.java | 669 | `ELEMENTAL_AILMENT_THRESHOLD_INC` | `inc_elemental_ailment_threshold` | bare | coe:ailment_threshold | double | add_value | [-0.3, -0.4] | fraction | fraction | **OK** |
| ChestItem.java | 681 | `MANA_REGENERATION_RATE_INC` | `inc_mana_regeneration_rate` | bare | coe:regeneration_rate | double | add_multiplied_base | [0.4, 0.5] | fraction | fraction | **OK** |
| ChestItem.java | 688 | `ENERGY_RECHARGE_START_FASTER` | `add_energy_recharge` | bare | coe:energy_recharge | percentage | add_value | [-0.4, -0.5] | fraction | fraction | **OK** |
| ChestItem.java | 702 | `ADD_ELEMENTAL_RESISTANCE` | `add_fire_resistance` | bare | coe:resistances | percentage | add_value | [0.05, 0.08] | fraction | fraction | **OK** |
| ChestItem.java | 726 | `ADD_MAX_ELEMENTAL_RESISTANCE` | `add_max_fire_resistance` | bare | coe:max_resistances | percentage | add_value | [0.01] | fraction | fraction | **OK** |
| ChestItem.java | 738 | `ADD_CHAOS_RESISTANCE` | `add_chaos_resistance` | bare | coe:resistances | percentage | add_value | [0.07, 0.13] | fraction | fraction | **OK** |
| HeadItem.java | 52 | `AILMENT_MAGNITUDE_FREEZE` | `ailment_magnitude_freeze` | bare | coe:ailment_magnitude | percentage | add_multiplied_base | [0.2] | fraction | fraction | **OK** |
| LegsItem.java | 18 | `CHANCE_TO_DODGE` | `chance_dodge` | bare | coe:dodge_chance | percentage | add_value | [0.1] | fraction | fraction | **OK** |
| AxeItem.java | 28 | `CHANCE_TO_CHILL_ON_HIT` | `chance_chill` | bare | coe:ailment_chance | percentage | add_value | [0.1] | fraction | fraction | **OK** |
| AxeItem.java | 64 | `TAKEN_DAMAGE_INC` | `inc_taken_damage` | bare | coe:taken_damage | percentage | add_multiplied_base | [0.1] | fraction | fraction | **OK** |
| BowItem.java | 56 | `CHANCE_TO_IGNITE_ON_HIT` | `chance_ignite` | bare | coe:ailment_chance | percentage | add_value | [0.1] | fraction | fraction | **OK** |
| BowItem.java | 88 | `PENETRATE_CHAOS` | `penetrate_chaos` | bare | coe:resistance_penetration | percentage | add_value | [0.06] | fraction | fraction | **OK** |
| CrossBowItem.java | 102 | `DAMAGE_OVER_TIME_INC` | `inc_damage_over_time` | bare | coe:damage_over_time_multiplier | percentage | add_multiplied_base | [0.15] | fraction | fraction | **OK** |
| MaceItem.java | 47 | `GLOBAL_CRIT_DAMAGE` | `add_crit_damage` | bare | coe:crit_damage | percentage | add_value | [0.1, 0.15] | fraction | fraction | **OK** |
| MaceItem.java | 109 | `GLOBAL_CRIT_DAMAGE` | `add_crit_damage` | bare | coe:crit_damage | percentage | add_value | [0.1, 0.15] | fraction | fraction | **OK** |
| MaceItem.java | 146 | `GLOBAL_CRIT_DAMAGE` | `add_crit_damage` | bare | coe:crit_damage | percentage | add_value | [0.1, 0.15] | fraction | fraction | **OK** |
| SwordItem.java | 28 | `CHANCE_TO_FREEZE_ON_HIT` | `chance_freeze` | bare | coe:ailment_chance | percentage | add_value | [0.1] | fraction | fraction | **OK** |