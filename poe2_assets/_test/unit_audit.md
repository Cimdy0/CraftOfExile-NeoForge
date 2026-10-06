# A4 单位一致性审计：PercentageAttribute + ADD_VALUE 必须写分数

## 判据（工程内实证）

1. `AttributeRegister.MAX_RESISTANCES = registerPercentage("max_resistances", 0.75)` —— 75% 上限存为 `0.75`，确认分数存储。
2. `compat/jade/MobsInfo.java:119` `getAttributeValue(RESISTANCES) * 100` —— 仅显示时 ×100。
3. `DamageAction.java:279-284` `r = min(r, max); denom = 1f - r;` 注释「抗性≈100% 时防除零」 —— `r` 为 1.0=100% 的分数。

**结论：`ADD_VALUE` 配裸 `$value` 放大 100 倍。命中 67 条**（P0 活属性+百分比描述 **35**；P1 活属性待判 14；P2 死属性 18）。

## P0 必修：活属性 + 百分比描述 → 实际放大 100 倍（35 条）

同属性已有条目写 `/100`，且运行时确有读取器消费 → 玩家可感知的数值错误。

| 属性 | stat 条目 | amount | POE2 描述 |
|---|---|---|---|
| `coe:ailment_chance` | `add_ailment_chance_bleed` | `$value` | 5% chance to inflict Bleeding on HitDamage |
| `coe:ailment_chance` | `add_ailment_chance_maim` | `$value` | Attacks have 10% chance to Maim on HitDamage |
| `coe:ailment_chance` | `add_ailment_chance_poison` | `$value` | 8% chance to Poison on HitDamage |
| `coe:ailment_chance` | `increased_chance_to_shock` | `$value` | {X}% increased chance to Shock |
| `coe:ailment_magnitude` | `add_exposure` | `$value` | Exposure you inflict lowers Resistances by an additional 5% |
| `coe:block_chance` | `add_block_chance` | `$value` | 4% increased Block chance |
| `coe:block_chance` | `to_max_block_chance` | `$value` | +{X}% to Max Block chance |
| `coe:charm_activation_chance` | `chance_when_a_charm_is_used_to_use_another_charm_without_consuming_charges` | `$value` | {X}% chance when a Charm is used to use another Charm without consuming Charges |
| `coe:crit_chance` | `add_crit_chance` | `$value` | 5% chance to Gain ArcaneSurgeDuration when you deal a Critical Hit |
| `coe:crit_damage` | `add_crit_damage` | `$value` | 25% increased Critical Damage Bonus for Attack Damage |
| `coe:flask_no_consume_chance` | `chance_flask_no_consume` | `$value` | 10% chance for Flasks you use to not consume Charges |
| `coe:flask_no_consume_chance` | `flask_no_consume` | `$value` | 10% chance for Flasks you use to not consume Charges |
| `coe:glory` | `skills_have_a_chance_to_not_consume_glory` | `$value` | Skills have a {X}% chance to not consume Glory |
| `coe:knockback` | `chance_to_knock_enemies_back_with_hit_damage_at_close_range` | `$value` | {X}% chance to Knock Enemies Back with Hit Damage at Close Range |
| `coe:max_resistances` | `add_max_chaos_resistance` | `$value` | +10% to Maximum Chaos Resistance |
| `coe:max_resistances` | `add_max_cold_resistance` | `$value` | +2% to Maximum Cold Resistance if you have at least 5 Blue Support Gems Socketed |
| `coe:max_resistances` | `add_max_fire_resistance` | `$value` | +2% to Maximum Fire Resistance if you have at least 5 Red Support Gems Socketed |
| `coe:max_resistances` | `add_max_lightning_resistance` | `$value` | +2% to Maximum Lightning Resistance if you have at least 5 Green Support Gems Socketed |
| `coe:max_resistances` | `add_max_resistances` | `$value` | +1% to Maximum Resistances of each ElementalDamage Damage Type you have been HitDamage with Recently |
| `coe:max_resistances` | `minions_have_to_all_max_elemental_resistances` | `$value` | Minions have +{X}% to all Max Elemental Resistances |
| `coe:max_resistances` | `minions_have_to_max_cold_resistances` | `$value` | Minions have +{X}% to Max Cold Resistances |
| `coe:max_resistances` | `minions_have_to_max_fire_resistances` | `$value` | Minions have +{X}% to Max Fire Resistances |
| `coe:max_resistances` | `minions_have_to_max_lightning_resistances` | `$value` | Minions have +{X}% to Max Lightning Resistances |
| `coe:projectile_pierce_chance` | `add_projectile_pierce_chance` | `$value` | 15% chance to Pierce an Enemy |
| `coe:puppet_master_chance` | `surpassing_chance_to_gain_a_puppet_master_stack_whenever_you_use_a_command_skill` | `$value` | {X}% Surpassing Chance to gain a Puppet Master stack whenever you use a Command Skill |
| `coe:puppet_master_effect` | `increased_effect_of_puppet_master` | `$value` | {X}% increased Effect of Puppet Master |
| `coe:remnant_extra_chance` | `chance_to_create_an_additional_remnant` | `$value` | {X}% chance to create an additional Remnant |
| `coe:resistances` | `add_chaos_resistance` | `$value` | +3% to Chaos Resistance |
| `coe:resistances` | `add_cold_resistance` | `$value` | +5% to Cold Resistance |
| `coe:resistances` | `add_fire_resistance` | `$value` | +3% to all ElementalDamage Resistances |
| `coe:resistances` | `add_lightning_resistance` | `$value` | +5% to Lightning Resistance |
| `coe:resistances` | `minions_have_to_chaos_resistances` | `$value` | Minions have +{X}% to Chaos Resistances |
| `coe:resistances` | `minions_have_to_cold_resistances` | `$value` | Minions have +{X}% to Cold Resistances |
| `coe:resistances` | `minions_have_to_fire_resistances` | `$value` | Minions have +{X}% to Fire Resistances |
| `coe:resistances` | `minions_have_to_lightning_resistances` | `$value` | Minions have +{X}% to Lightning Resistances |

## P1 待判：活属性但描述无百分号（14 条）

需人工确认描述语义是分数还是计数（如「+{X} 次」类应走 registerDouble）。

| 属性 | stat 条目 | amount | POE2 描述 |
|---|---|---|---|
| `coe:ailment_chance` | `chance_chill` | `$value` | Chance Chill |
| `coe:ailment_chance` | `chance_freeze` | `$value` | Chance Freeze |
| `coe:ailment_chance` | `chance_ignite` | `$value` | Chance Ignite |
| `coe:crit_damage` | `add_cast_crit_damage` | `$value` | Add Cast Crit Damage |
| `coe:dodge_chance` | `chance_dodge` | `$value` | Chance Dodge |
| `coe:energy_recharge` | `add_energy_recharge` | `$value` | Add Energy Recharge |
| `coe:found_item_rarity` | `add_found_item_rarity` | `$value` | Add Found Item Rarity |
| `coe:found_item_rarity` | `charm_golden_rarity` | `$value` | Charm Golden Rarity |
| `coe:puppet_master_duration` | `increased_duration_of_puppet_master` | `$value` | 原误挂在 INCREASED_EFFECT_OF_PUPPET_MASTER 下，Step6 拆分独立 key。 |
| `coe:resistances` | `charm_resist_chaos` | `$value` | Charm Resist Chaos |
| `coe:resistances` | `charm_resist_cold` | `$value` | Charm Resist Cold |
| `coe:resistances` | `charm_resist_fire` | `$value` | Charm Resist Fire |
| `coe:resistances` | `charm_resist_lightning` | `$value` | Charm Resist Lightning |
| `coe:taken_crit_chance` | `taken_crit` | `$value` | Taken Crit |

## P2 缓修：死属性（无运行时读取器）（18 条）

单位错误当前不显形，建议随该属性消费逻辑实现时一并纠正。

| 属性 | stat 条目 | amount | POE2 描述 |
|---|---|---|---|
| `coe:attack_ten_incision` | `chance_for_attack_hit_damage_to_apply_ten_incision` | `$value` | {X}% chance for Attack Hit Damage to apply ten Incision |
| `coe:attacks_critical` | `attacks_have_to_critical_hit_chance` | `$value` | Attacks have +{X}% to Critical Hit Chance |
| `coe:attacks_maim_poisoned` | `chance_for_attacks_to_maim_on_hit_damage_against_poisoned_enemies` | `$value` | {X}% chance for Attacks to Maim on Hit Damage against Poisoned Enemies |
| `coe:damage_lucky` | `chance_for_damage_with_hit_damage_to_be_lucky` | `$value` | {X}% chance for Damage with Hit Damage to be Lucky |
| `coe:defend_armour` | `chance_to_defend_with_of_armor` | `$value` | {X}% chance to Defend with {X}% of Armor |
| `coe:elemental_resistances_red_green` | `to_all_max_elemental_resistances_if_you_have_at_least_red_green_and_blue_support_gems_socketed` | `$value` | +{X}% to all Max Elemental Resistances if you have at least {X} Red, Green and Blue Support Gems Socketed |
| `coe:found_item_number` | `add_found_item_number` | `$value` | Add Found Item Number |
| `coe:grenades_activate_time` | `grenades_have_chance_to_activate_a_second_time` | `$value` | Grenades have {X}% chance to activate a second time |
| `coe:mace_slam_cause_additional` | `chance_for_mace_slam_skills_you_use_yourself_to_cause_an_additional_aftershock` | `$value` | {X}% chance for Mace Slam Skills you use yourself to cause an additional Aftershock |
| `coe:metres_melee_strike_range_2` | `metres_to_melee_strike_range_if_you_ve_dealt_a_projectile_attack_hit_damage_in_the_past_eight_seconds` | `$value` | +{X} metres to Melee Strike Range if you've dealt a Projectile Attack Hit Damage in the past eight seconds |
| `coe:minions_critical` | `minions_have_increased_critical_hit_chance` | `$value` | Minions have {X}% increased Critical Hit Chance |
| `coe:minions_inflict_withered` | `minions_have_chance_to_inflict_withered_on_hit_damage` | `$value` | Minions have {X}% chance to inflict Withered on Hit Damage |
| `coe:plants_immediately_overgrow` | `plants_have_a_chance_to_immediately_overgrow` | `$value` | Plants have a {X}% chance to immediately Overgrow |
| `coe:recover_all_life_kill` | `chance_to_recover_all_health_when_you_kill_an_enemy` | `$value` | {X}% chance to Recover all Health when you Kill an Enemy |
| `coe:reload_crossbow_immediate` | `chance_when_you_reload_a_crossbow_to_be_immediate` | `$value` | {X}% chance when you Reload a Crossbow to be immediate |
| `coe:remove_elemental_infusions_but` | `skills_have_chance_to_not_add_elemental_infusions_but_still_count_as_consuming_them` | `$value` | Skills have {X}% chance to not add Elemental Infusions but still count as consuming them |
| `coe:shock_electrocuted` | `increased_shock_chance_against_electrocuted_enemies` | `$value` | {X}% increased Shock Chance against Electrocuted Enemies |
| `coe:which_create_fissures_additional` | `skills_which_create_fissures_have_a_chance_to_create_an_additional_fissure` | `$value` | Skills which create Fissures have a {X}% chance to create an additional Fissure |

## 同属性同操作的合规计数（对照）

| 属性 | 操作 | 合规条目数 |
|---|---|---:|
| `coe:damage` | `add_multiplied_base` | 62 |
| `coe:ailment_duration` | `add_multiplied_base` | 41 |
| `coe:ailment_magnitude` | `add_multiplied_base` | 36 |
| `coe:ailment_buildup` | `add_multiplied_base` | 16 |
| `coe:resistance_penetration` | `add_value` | 9 |
| `coe:energy_recharge` | `add_multiplied_base` | 8 |
| `coe:status_extra_stacks` | `add_multiplied_base` | 8 |
| `coe:status_max_stacks` | `add_multiplied_base` | 8 |
| `coe:ailment_damage_rate` | `add_multiplied_base` | 6 |
| `coe:ailment_chance` | `add_value` | 5 |
| `coe:taken_damage` | `add_multiplied_base` | 5 |
| `coe:flask_recovery_rate` | `add_multiplied_base` | 4 |
| `coe:cooldown_recovery_rate` | `add_multiplied_base` | 4 |
| `coe:projectile_speed` | `add_multiplied_base` | 4 |
| `coe:resistances` | `add_value` | 4 |
| `coe:leech` | `add_value` | 3 |
| `coe:area_of_effect` | `add_multiplied_base` | 3 |
| `coe:volatility_generation` | `add_multiplied_base` | 2 |
| `coe:thorns` | `add_multiplied_base` | 2 |
| `coe:amount_leeched` | `add_multiplied_base` | 2 |
| `coe:crit_chance` | `add_multiplied_base` | 2 |
| `coe:mana_take_of_damage` | `add_value` | 2 |
| `coe:revive_speed` | `add_multiplied_base` | 2 |
| `coe:deflection_rating` | `add_multiplied_base` | 2 |
| `coe:crit_damage` | `add_multiplied_base` | 2 |
