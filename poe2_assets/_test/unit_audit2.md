# A4-v2 单位一致性审计（调用方感知）

percentage 属性 + `$value` 模板条目共 **397** 条。

| 分级 | 条数 | 含义 |
|---|---|---|
| W-BAD | 0 | 已接线且调用方传整数百分点 + 条目裸 `$value` → **真 100x 放大** |
| W-DIV | 1 | 已接线且调用方传小数 + 条目烘 `/100` → **反向缩小 100 倍** |
| W-UNK | 2 | 已接线但幅度为不可解析表达式 → **须人工判** |
| U-MIX | 192 | 未接线，且 (属性,operation) 族内约定不一致 → **接线前须统一** |
| W-OK  | 11 | 已接线且单位自洽 |
| U-UNI | 191 | 未接线但族内约定一致 |

## 家族约定分布（div100 / bare）

| 属性 | operation | `/100` | 裸 `$value` | 是否分裂 |
|---|---|---|---|---|
| `coe:damage` | add_multiplied_base | 34 | 28 | **是** |
| `coe:ailment_magnitude` | add_multiplied_base | 19 | 17 | **是** |
| `coe:ailment_duration` | add_multiplied_base | 25 | 16 | **是** |
| `coe:ailment_chance` | add_value | 5 | 7 | **是** |
| `coe:ailment_buildup` | add_multiplied_base | 5 | 11 | **是** |
| `coe:resistances` | add_value | 4 | 12 | **是** |
| `coe:ailment_damage_rate` | add_multiplied_base | 4 | 2 | **是** |
| `coe:block_chance` | add_value | 1 | 2 | **是** |
| `coe:found_item_number` | add_value | 1 | 1 | **是** |
| `coe:found_item_rarity` | add_value | 1 | 2 | **是** |
| `coe:projectile_pierce_chance` | add_value | 1 | 1 | **是** |
| `coe:taken_damage` | add_multiplied_base | 1 | 4 | **是** |
| `coe:deflection_rating` | add_multiplied_base | 1 | 1 | **是** |
| `coe:remove_curse_channelling` | add_multiplied_base | 1 | 0 | 否 |
| `coe:remove_curse_mana_flask` | add_multiplied_base | 1 | 0 | 否 |
| `coe:crit_damage` | add_value | 0 | 2 | 否 |
| `coe:crit_chance` | add_value | 0 | 1 | 否 |
| `coe:energy_recharge` | add_value | 0 | 1 | 否 |
| `coe:ailment_magnitude` | add_value | 0 | 1 | 否 |
| `coe:remove_ignite_warcry` | add_multiplied_base | 1 | 0 | 否 |
| `coe:max_resistances` | add_value | 0 | 9 | 否 |
| `coe:bonuses_amulet_minions` | add_multiplied_base | 1 | 0 | 否 |
| `coe:archon_recovery_period_expires_1` | add_multiplied_base | 1 | 0 | 否 |
| `coe:arrows_pierce_additional_target` | add_multiplied_base | 1 | 0 | 否 |
| `coe:resistance_penetration` | add_value | 9 | 0 | 否 |
| `coe:attacks_critical` | add_value | 0 | 1 | 否 |
| `coe:blind_they_stun` | add_multiplied_base | 1 | 0 | 否 |
| `coe:buffs_expire_slower` | add_multiplied_base | 1 | 0 | 否 |
| `coe:cannot_blinded_full_life` | add_multiplied_base | 1 | 0 | 否 |
| `coe:cannot_critically_parrying` | add_multiplied_base | 1 | 0 | 否 |
| `coe:dodge_chance` | add_value | 0 | 1 | 否 |
| `coe:flask_no_consume_chance` | add_value | 0 | 2 | 否 |
| `coe:attack_ten_incision` | add_value | 0 | 1 | 否 |
| `coe:attacks_maim_poisoned` | add_value | 0 | 1 | 否 |
| `coe:damage_lucky` | add_value | 0 | 1 | 否 |
| `coe:mace_slam_cause_additional` | add_value | 0 | 1 | 否 |
| `coe:remnant_extra_chance` | add_value | 0 | 1 | 否 |
| `coe:defend_armour` | add_value | 0 | 1 | 否 |
| `coe:volatility_generation` | add_multiplied_base | 0 | 2 | 否 |
| `coe:knockback` | add_value | 0 | 1 | 否 |
| `coe:recover_all_life_kill` | add_value | 0 | 1 | 否 |
| `coe:charm_activation_chance` | add_value | 0 | 1 | 否 |
| `coe:reload_crossbow_immediate` | add_value | 0 | 1 | 否 |
| `coe:rage_shapeshifting_human_form` | add_multiplied_base | 1 | 0 | 否 |
| `coe:culling_strike_mark` | add_multiplied_base | 1 | 0 | 否 |
| `coe:rage_loss_rate` | add_multiplied_base | 1 | 0 | 否 |
| `coe:defend_armour_critical` | add_multiplied_base | 1 | 0 | 否 |
| `coe:mark_blinded` | add_multiplied_base | 1 | 0 | 否 |
| `coe:inflict_bleeding_cannot_regenerate` | add_multiplied_base | 1 | 0 | 否 |
| `coe:mark_cannot_critical` | add_multiplied_base | 1 | 0 | 否 |
| `coe:faster_curse_activation` | add_multiplied_base | 1 | 0 | 否 |
| `coe:energy_recharge` | add_multiplied_base | 8 | 0 | 否 |
| `coe:faster_start_inherent_rage` | add_multiplied_base | 1 | 0 | 否 |
| `coe:flask_recovery_rate` | add_multiplied_base | 4 | 0 | 否 |
| `coe:additional_stun_threshold_equal_2` | add_multiplied_base | 1 | 0 | 否 |
| `coe:thorns` | add_multiplied_base | 2 | 0 | 否 |
| `coe:rage_life_flask` | add_multiplied_base | 1 | 0 | 否 |
| `coe:cooldown_recovery_rate` | add_multiplied_base | 4 | 0 | 否 |
| `coe:grenades_activate_time` | add_value | 0 | 1 | 否 |
| `coe:life_flasks_charges` | add_multiplied_base | 1 | 0 | 否 |
| `coe:leech_life_faster` | add_multiplied_base | 1 | 0 | 否 |
| `coe:leech_life_slower` | add_multiplied_base | 1 | 0 | 否 |
| `coe:ignites_cause_reflected_back` | add_multiplied_base | 1 | 0 | 否 |
| `coe:ignore_movement_penalties_armour` | add_multiplied_base | 1 | 0 | 否 |
| `coe:amount_leeched` | add_multiplied_base | 2 | 0 | 否 |
| `coe:aura_magnitude` | add_multiplied_base | 1 | 0 | 否 |
| `coe:buff_effect_taken` | add_multiplied_base | 0 | 1 | 否 |
| `coe:charm_effect` | add_multiplied_base | 1 | 0 | 否 |
| `coe:crit_chance` | add_multiplied_base | 2 | 0 | 否 |
| `coe:mana_take_of_damage` | add_value | 2 | 0 | 否 |
| `coe:damage_over_time_multiplier` | add_multiplied_base | 1 | 0 | 否 |
| `coe:glory_generation` | add_multiplied_base | 0 | 1 | 否 |
| `coe:incision_effect` | add_multiplied_base | 1 | 0 | 否 |
| `coe:projectile_speed` | add_multiplied_base | 4 | 0 | 否 |
| `coe:leech` | add_value | 3 | 0 | 否 |
| `coe:leech_rate` | add_multiplied_base | 1 | 0 | 否 |
| `coe:meta_energy_gain` | add_multiplied_base | 1 | 0 | 否 |
| `coe:revive_speed` | add_multiplied_base | 2 | 0 | 否 |
| `coe:movement_penalty_reduction` | add_value | 1 | 0 | 否 |
| `coe:quiver_bonuses` | add_multiplied_base | 1 | 0 | 否 |
| `coe:cross_bow_reload_time` | add_multiplied_base | 1 | 0 | 否 |
| `coe:reservation_efficiency` | add_value | 1 | 0 | 否 |
| `coe:taken_crit_damage` | add_multiplied_base | 0 | 1 | 否 |
| `coe:trap_throwing_speed` | add_multiplied_base | 1 | 0 | 否 |
| `coe:area_of_effect` | add_multiplied_base | 3 | 0 | 否 |
| `coe:curse_effect_taken` | add_multiplied_base | 1 | 0 | 否 |
| `coe:shock_effect_taken` | add_multiplied_base | 1 | 0 | 否 |
| `coe:withered_effect_taken` | add_multiplied_base | 1 | 0 | 否 |
| `coe:magnitude_impales_inflicted_spells` | add_multiplied_base | 1 | 0 | 否 |
| `coe:charm_charges_gained` | add_multiplied_base | 1 | 0 | 否 |
| `coe:charm_charges_used` | add_multiplied_base | 1 | 0 | 否 |
| `coe:crit_damage` | add_multiplied_base | 2 | 0 | 否 |
| `coe:culling_strike_threshold` | add_multiplied_base | 1 | 0 | 否 |
| `coe:puppet_master_duration` | add_value | 0 | 1 | 否 |
| `coe:archon_effect_taken` | add_multiplied_base | 1 | 0 | 否 |
| `coe:puppet_master_effect` | add_value | 0 | 1 | 否 |
| `coe:elemental_ailment_application_shapeshifted` | add_multiplied_base | 1 | 0 | 否 |
| `coe:es_from_focus` | add_multiplied_base | 1 | 0 | 否 |
| `coe:es_from_helmet` | add_multiplied_base | 1 | 0 | 否 |
| `coe:flask_charges_gained` | add_multiplied_base | 2 | 0 | 否 |
| `coe:skill_cost_life` | add_multiplied_base | 1 | 0 | 否 |
| `coe:knockback` | add_multiplied_base | 1 | 0 | 否 |
| `coe:light_radius` | add_multiplied_base | 1 | 0 | 否 |
| `coe:cost_efficiency` | add_multiplied_base | 1 | 0 | 否 |
| `coe:skill_cost_mana` | add_multiplied_base | 1 | 0 | 否 |
| `coe:parry_range` | add_multiplied_base | 1 | 0 | 否 |
| `coe:shock_electrocuted` | add_value | 0 | 1 | 否 |
| `coe:recovery_rate` | add_multiplied_base | 2 | 0 | 否 |
| `coe:stun_recovery` | add_multiplied_base | 1 | 0 | 否 |
| `coe:totem_placement_range` | add_multiplied_base | 1 | 0 | 否 |
| `coe:invocated_energy` | add_multiplied_base | 1 | 0 | 否 |
| `coe:mana_flasks_charges` | add_multiplied_base | 1 | 0 | 否 |
| `coe:metres_melee_strike_range_2` | add_value | 0 | 1 | 否 |
| `coe:minions_inflict_withered` | add_value | 0 | 1 | 否 |
| `coe:minions_critical` | add_value | 0 | 1 | 否 |
| `coe:amount_leeched` | add_multiplied_total | 1 | 0 | 否 |
| `coe:ailment_magnitude` | add_multiplied_total | 1 | 0 | 否 |
| `coe:damage` | add_multiplied_total | 1 | 0 | 否 |
| `coe:damage_taken_bypasses_energy` | add_multiplied_base | 1 | 0 | 否 |
| `coe:life_converted_energy_shield` | add_multiplied_base | 1 | 0 | 否 |
| `coe:plants_immediately_overgrow` | add_value | 0 | 1 | 否 |
| `coe:remnants_collected_further_away` | add_multiplied_base | 1 | 0 | 否 |
| `coe:lose_combo_slower` | add_multiplied_base | 1 | 0 | 否 |
| `coe:glory` | add_value | 0 | 1 | 否 |
| `coe:remove_elemental_infusions_but` | add_value | 0 | 1 | 否 |
| `coe:which_create_fissures_additional` | add_value | 0 | 1 | 否 |
| `coe:aftershock_area` | add_multiplied_base | 1 | 0 | 否 |
| `coe:status_extra_stacks` | add_multiplied_base | 0 | 8 | 否 |
| `coe:status_max_stacks` | add_multiplied_base | 0 | 8 | 否 |
| `coe:puppet_master_chance` | add_value | 0 | 1 | 否 |
| `coe:taken_crit_chance` | add_value | 0 | 1 | 否 |
| `coe:elemental_resistances_red_green` | add_value | 0 | 1 | 否 |
| `coe:unaffected_chill_during_dodge` | add_multiplied_base | 1 | 0 | 否 |
| `coe:unaffected_chill_leeching_mana` | add_multiplied_base | 1 | 0 | 否 |
| `coe:unaffected_chilled_ground` | add_multiplied_base | 1 | 0 | 否 |
| `coe:unaffected_elemental_weakness` | add_multiplied_base | 1 | 0 | 否 |
| `coe:unaffected_shocked_ground` | add_multiplied_base | 1 | 0 | 否 |
| `coe:warcries_debilitate` | add_multiplied_base | 1 | 0 | 否 |
| `coe:warcries_empower_additional_attack` | add_multiplied_base | 1 | 0 | 否 |
| `coe:warcries_minimum_power` | add_multiplied_base | 1 | 0 | 否 |
| `coe:additional_curse` | add_multiplied_base | 1 | 0 | 否 |
| `coe:aura_buffs_affect_allies` | add_multiplied_base | 1 | 0 | 否 |
| `coe:minions_gigantic` | add_multiplied_base | 1 | 0 | 否 |

## W-DIV（1 条）

| id | 属性 | op | amount | 调用方 | 描述 |
|---|---|---|---|---|---|
| `inc_minion_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | BuffStatusTypes.java:51(`perStack * inst.stac`→`0.03 * effect`) | Minions deal 10% increased Damage |

## W-UNK（2 条）

| id | 属性 | op | amount | 调用方 | 描述 |
|---|---|---|---|---|---|
| `add_recently_consumed_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | BuffStatusTypes.java:125(`points`→`StatEngine.fold(livi`) | Add Recently Consumed Damage |
| `inc_infusion_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | BuffStatusTypes.java:140(`m`→`inst.magnitude`) | Inc Infusion Magnitude |

## U-MIX（192 条）

| id | 属性 | op | amount | 调用方 | 描述 |
|---|---|---|---|---|---|
| `add_ailment_chance_bleed` | `coe:ailment_chance` | add_value | `$value` | — | 5% chance to inflict Bleeding on HitDamage |
| `add_ailment_chance_maim` | `coe:ailment_chance` | add_value | `$value` | — | Attacks have 10% chance to Maim on HitDamage |
| `add_ailment_chance_poison` | `coe:ailment_chance` | add_value | `$value` | — | 8% chance to Poison on HitDamage |
| `add_block_chance` | `coe:block_chance` | add_value | `$value` | — | 4% increased Block chance |
| `add_chaos_resistance` | `coe:resistances` | add_value | `$value` | — | +3% to Chaos Resistance |
| `add_cold_resistance` | `coe:resistances` | add_value | `$value` | — | +5% to Cold Resistance |
| `add_fire_resistance` | `coe:resistances` | add_value | `$value` | — | +3% to all ElementalDamage Resistances |
| `add_found_item_number` | `coe:found_item_number` | add_value | `$value` | — | Add Found Item Number |
| `add_found_item_rarity` | `coe:found_item_rarity` | add_value | `$value` | — | Add Found Item Rarity |
| `add_lightning_resistance` | `coe:resistances` | add_value | `$value` | — | +5% to Lightning Resistance |
| `add_projectile_pierce_chance` | `coe:projectile_pierce_chance` | add_value | `$value` | — | 15% chance to Pierce an Enemy |
| `ailment_buildup_bleed` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_chill` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_electrocute` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_freeze` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_ignite` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_pin` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_poison` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_buildup_shock` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_bleed` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_chill` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_electrocute` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_freeze` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_ignite` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_pin` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_poison` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_duration_shock` | `coe:ailment_duration` | add_multiplied_base | `$value` | — |  |
| `ailment_magnitude_bleed` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Bleeding you inflict |
| `ailment_magnitude_chill` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Shock you inflict |
| `ailment_magnitude_electrocute` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — |  |
| `ailment_magnitude_freeze` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | Ailment Magnitude Freeze |
| `ailment_magnitude_ignite` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 15% increased Flammability BuffMagnitude |
| `ailment_magnitude_pin` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — |  |
| `ailment_magnitude_poison` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Poison you inflict |
| `ailment_magnitude_shock` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 15% increased BuffMagnitude of Shock you inflict |
| `archon_freeze_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — | Archon Freeze Buildup |
| `archon_ignite_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — | Archon Ignite Buildup |
| `archon_shock_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | Archon Shock Magnitude |
| `attacks_with_one_handed_martial_weapon_have_increased_chance_to_inflict_ailments` | `coe:ailment_chance` | add_value | `$value / 100` | — | Attacks with One-Handed Martial Weapon have {X}% inc |
| `banner_skills_have_increased_aura_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | Banner Skills have {X}% increased Aura Buff Magnitud |
| `banner_skills_have_increased_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Banner Skills have {X}% increased Duration |
| `bleeding_you_inflict_deals_damage_faster` | `coe:ailment_damage_rate` | add_multiplied_base | `$value / 100` | — | Bleeding you inflict deals Damage {X}% faster |
| `chance_chill` | `coe:ailment_chance` | add_value | `$value` | — | Chance Chill |
| `chance_freeze` | `coe:ailment_chance` | add_value | `$value` | — | Chance Freeze |
| `chance_ignite` | `coe:ailment_chance` | add_value | `$value` | — | Chance Ignite |
| `companions_deal_increased_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Companions deal {X}% increased Damage |
| `damage_dealt_elemental_spell` | `coe:damage` | add_multiplied_base | `$value` | — | Damage Dealt Elemental Spell |
| `damage_taken` | `coe:taken_damage` | add_multiplied_base | `$value` | — | Damage Taken |
| `damage_taken_chaos` | `coe:taken_damage` | add_multiplied_base | `$value` | — | Damage Taken Chaos |
| `damage_taken_physical` | `coe:taken_damage` | add_multiplied_base | `$value` | — | Damage Taken Physical |
| `damaging_ailments_deal_damage_faster` | `coe:ailment_damage_rate` | add_multiplied_base | `$value / 100` | — | Damaging Ailments deal damage {X}% faster |
| `empowered_attacks_deal_increased_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Empowered Attacks deal {X}% increased Damage |
| `enemies_in_jagged_ground_you_create_take_increased_damage` | `coe:taken_damage` | add_multiplied_base | `$value / 100` | — | Enemies in Jagged Ground you create take {X}% increa |
| `grenade_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 12% increased Grenade Damage |
| `hazard_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 16% increased Hazard Damage |
| `herald_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Herald Skills deal 20% increased Damage |
| `inc_ailment_chance` | `coe:ailment_chance` | add_value | `$value / 100` | — | 10% increased chance to inflict Ailments |
| `inc_ailment_chance_vs_exposed_cold` | `coe:ailment_chance` | add_value | `$value / 100` | — | Damage Penetration 18% Cold Resistance |
| `inc_ailment_chance_vs_exposed_fire` | `coe:ailment_chance` | add_value | `$value / 100` | — | Damage Penetration 18% Cold Resistance |
| `inc_ailment_chance_vs_exposed_lightning` | `coe:ailment_chance` | add_value | `$value / 100` | — | Damage Penetration 18% Cold Resistance |
| `inc_ailment_damage_rate_bleed` | `coe:ailment_damage_rate` | add_multiplied_base | `$value` | — | Inc Ailment Damage Rate Bleed |
| `inc_ailment_damage_rate_poison` | `coe:ailment_damage_rate` | add_multiplied_base | `$value` | — | Inc Ailment Damage Rate Poison |
| `inc_ailment_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 10% increased Skill Effect Duration |
| `inc_ailment_duration_vs_exposed_cold` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Damage Penetration 18% Fire Resistance |
| `inc_ailment_duration_vs_exposed_fire` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Damage Penetration 18% Fire Resistance |
| `inc_ailment_duration_vs_exposed_lightning` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Damage Penetration 18% Fire Resistance |
| `inc_ailment_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Ailments you inflict |
| `inc_attack_elemental_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 12% increased ElementalDamage Damage with Attacks |
| `inc_banner_damage` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Banner Damage |
| `inc_beast_ailment_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 10% increased Duration of Ailments on Beasts |
| `inc_bleed_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 3% increased Movement Speed |
| `inc_bleeding_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 40% reduced Duration of Bleeding on You |
| `inc_block_chance` | `coe:block_chance` | add_value | `$value / 100` | — | 5% increased Block chance |
| `inc_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | Aura Skills have 5% increased BuffMagnitude |
| `inc_chaos_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 7% increased Chaos Damage |
| `inc_charm_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Temporary Minion Skills have +2 to Limit of Minions  |
| `inc_chill_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | Inc Chill Duration Taken |
| `inc_cold_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 12% increased Cold Damage |
| `inc_cold_damage_spell` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Cold Damage Spell |
| `inc_crossbow_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 15% increased Crossbow Reload Speed |
| `inc_curse_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 30% increased Damage per Curse on you |
| `inc_damage_attack` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Attack Damage |
| `inc_damage_blocking` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Damage Blocking |
| `inc_damage_empowered_attacks` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 5% chance to inflict Bleeding on HitDamage |
| `inc_damage_low_life` | `coe:damage` | add_multiplied_base | `$value` | — | 5% increased Damage taken while on Low Life |
| `inc_damage_parrying` | `coe:damage` | add_multiplied_base | `$value` | — | Successfully Parrying a Melee HitDamage grants 40% i |
| `inc_damage_presence_allies` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Allies in your Presence deal 16% increased Damage |
| `inc_damage_presence_enemies` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Each Totem applies 2% increased Damage taken to Enem |
| `inc_damage_recently_killed` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Damage Recently Killed |
| `inc_damage_shapeshifted` | `coe:damage` | add_multiplied_base | `$value` | — | 10% increased Damage while Shapeshifted |
| `inc_daze_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 10% chance to Daze on HitDamage |
| `inc_debuff_expiry` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Debuffs on you expire 10% faster |
| `inc_faster_ailments` | `coe:ailment_damage_rate` | add_multiplied_base | `$value / 100` | — | 6% increased chance to inflict Ailments |
| `inc_fire_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 12% increased Fire Damage |
| `inc_fire_damage_spell` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Fire Damage Spell |
| `inc_flammability_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | 30% increased Flammability BuffMagnitude |
| `inc_flask_effect_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 20% chance for Flasks you use to not consume Charges |
| `inc_found_quantity` | `coe:found_item_number` | add_value | `$value / 100` | — | Inc Found Quantity |
| `inc_found_rarity` | `coe:found_item_rarity` | add_value | `$value / 100` | — | Inc Found Rarity |
| `inc_freeze_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value / 100` | — | 15% increased Freeze Buildup |
| `inc_freeze_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 25% reduced Freeze Duration on you |
| `inc_grenade_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 12% increased Grenade Damage |
| `inc_hazard_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 16% increased Hazard Damage |
| `inc_herald_damage` | `coe:damage` | add_multiplied_base | `$value` | — | Herald Skills deal 20% increased Damage |
| `inc_ignite_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 25% reduced Ignite Duration on you |
| `inc_ignite_faster` | `coe:ailment_damage_rate` | add_multiplied_base | `$value / 100` | — | Ignites you inflict deal Damage 15% faster |
| `inc_ignite_magnitude_taken` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | 15% reduced BuffMagnitude of Ignite on you |
| `inc_incision_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | 50% increased effect of Incision |
| `inc_infusion_duration` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 10% increased Elemental Infusion duration |
| `inc_jagged_ground_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 20% chance for Bleeding to be Aggravated when Inflic |
| `inc_light_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 12% increased Lightning Damage |
| `inc_light_damage_spell` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Light Damage Spell |
| `inc_mark_damage` | `coe:damage` | add_multiplied_base | `$value` | — | Enemies you Mark take 10% increased Damage |
| `inc_mark_effect` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | Mark Skills have 10% increased Use Speed |
| `inc_meta_damage` | `coe:damage` | add_multiplied_base | `$value` | — | Inc Meta Damage |
| `inc_minion_elemental_resist` | `coe:resistances` | add_value | `$value / 100` | — | Inc Minion Elemental Resist |
| `inc_non_damaging_ailment_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Non-Damaging Ailments |
| `inc_offering_damage` | `coe:damage` | add_multiplied_base | `$value` | — |  |
| `inc_offering_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | Offering Skills have 15% increased Buff effect |
| `inc_physical_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Physical Damage |
| `inc_pin_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value / 100` | — | 15% increased Pinned Buildup |
| `inc_poison_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 10% reduced Poison Duration on you |
| `inc_poison_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 10% increased BuffMagnitude of Poison you inflict |
| `inc_projectile_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Projectile Damage |
| `inc_projectile_pierce` | `coe:projectile_pierce_chance` | add_value | `$value / 100` | — | 15% chance to Pierce an Enemy |
| `inc_shock_duration_taken` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 25% reduced Shock duration on you |
| `inc_shock_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | 15% increased BuffMagnitude of Shock you inflict |
| `inc_skill_effect_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | 10% increased Skill Effect Duration |
| `inc_spell_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Attack Damage |
| `inc_stun_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value` | — | 15% increased Stun Buildup |
| `inc_stun_duration` | `coe:ailment_duration` | add_multiplied_base | `$value` | — | 15% increased Stun Buildup |
| `inc_taken_damage` | `coe:taken_damage` | add_multiplied_base | `$value` | — | 40% increased maximum Energy Shield |
| `inc_trap_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Trap Damage |
| `inc_warcry_damage` | `coe:damage` | add_multiplied_base | `$value` | — | 16% increased Damage with Warcries |
| `increased_ailment_threshold_and_stun_threshold_threshold_while_surrounded` | `coe:deflection_rating` | add_multiplied_base | `$value` | — | {X}% increased Ailment Threshold and Stun Threshold  |
| `increased_archon_buff_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Archon Buff duration |
| `increased_attack_area_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Attack Area Damage |
| `increased_ballista_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Ballista damage |
| `increased_blind_effect` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Blind Effect |
| `increased_buff_effect_of_arcane_surge_on_you` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Effect of Arcane Surge on you |
| `increased_buff_magnitude_of_bleeding_on_you` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Magnitude of Bleeding on You |
| `increased_buff_magnitude_of_chill_you_inflict` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | {X}% increased Buff Magnitude of Chill you inflict |
| `increased_buff_magnitude_of_damaging_ailments_you_inflict` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Magnitude of Damaging Ailments y |
| `increased_buff_magnitude_of_daze` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Magnitude of Daze |
| `increased_buff_magnitude_of_jagged_ground_you_create` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Magnitude of Jagged Ground you c |
| `increased_chance_to_shock` | `coe:ailment_chance` | add_value | `$value` | — | {X}% increased chance to Shock |
| `increased_curse_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Curse Buff Magnitude |
| `increased_curse_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Curse Duration |
| `increased_damage_with_axes` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Axes |
| `increased_damage_with_bows` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Bows |
| `increased_damage_with_hits_against_chilled_enemies` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Hits against Chilled Enem |
| `increased_damage_with_maces` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Maces |
| `increased_damage_with_one_handed_weapons` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with One Handed Weapons |
| `increased_damage_with_spears` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Spears |
| `increased_damage_with_swords` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Swords |
| `increased_damage_with_two_handed_weapons` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Damage with Two Handed Weapons |
| `increased_deflection_rating_while_surrounded` | `coe:deflection_rating` | add_multiplied_base | `$value / 100` | — | {X}% increased Deflection Rating while Surrounded |
| `increased_duration_of_ailments_on_you` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Duration of Ailments on You |
| `increased_duration_of_damaging_ailments_on_enemies` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Duration of Damaging Ailments on Enem |
| `increased_effect_of_chill_on_you` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Effect of Chill on you |
| `increased_electrocute_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value / 100` | — | {X}% increased Electrocute Buildup |
| `increased_elemental_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Elemental Damage |
| `increased_exposure_effect` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Exposure Effect |
| `increased_freeze_buildup_with_empowered_attacks` | `coe:ailment_buildup` | add_multiplied_base | `$value / 100` | — | {X}% increased Freeze Buildup with Empowered Attacks |
| `increased_frenzy_charge_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Frenzy Charge Duration |
| `increased_grenade_detonation_time` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Grenade Detonation Time |
| `increased_hazard_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Hazard Duration |
| `increased_ignite_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value` | — | {X}% increased Ignite Buff Magnitude |
| `increased_ignite_duration_on_enemies` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Ignite Duration on Enemies |
| `increased_immobilisation_buildup` | `coe:ailment_buildup` | add_multiplied_base | `$value / 100` | — | {X}% increased Immobilisation buildup |
| `increased_melee_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Melee Damage |
| `increased_parried_debuff_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Parried Debuff Buff Magnitude |
| `increased_parried_debuff_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Parried Debuff Duration |
| `increased_parry_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Parry Damage |
| `increased_physical_damage_while_shapeshifted` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Physical Damage while Shapeshifted |
| `increased_pinned_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Pinned duration |
| `increased_poison_duration` | `coe:ailment_duration` | add_multiplied_base | `$value / 100` | — | {X}% increased Poison Duration |
| `increased_slowing_potency_of_debuffs_on_you` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Slowing Potency of Debuffs on You |
| `increased_total_power_counted_by_warcries` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased total Power counted by Warcries |
| `increased_totem_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Totem Damage |
| `increased_withered_buff_magnitude` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | {X}% increased Withered Buff Magnitude |
| `minion_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | Minions deal 10% increased Damage |
| `minions_have_to_all_elemental_resistances` | `coe:resistances` | add_value | `$value / 100` | — | Minions have +{X}% to all Elemental Resistances |
| `minions_have_to_chaos_resistances` | `coe:resistances` | add_value | `$value` | — | Minions have +{X}% to Chaos Resistances |
| `minions_have_to_cold_resistances` | `coe:resistances` | add_value | `$value` | — | Minions have +{X}% to Cold Resistances |
| `minions_have_to_fire_resistances` | `coe:resistances` | add_value | `$value` | — | Minions have +{X}% to Fire Resistances |
| `minions_have_to_lightning_resistances` | `coe:resistances` | add_value | `$value` | — | Minions have +{X}% to Lightning Resistances |
| `offering_skills_have_increased_buff_effect` | `coe:ailment_magnitude` | add_multiplied_base | `$value / 100` | — | Offering Skills have {X}% increased Buff effect |
| `to_chaos_resistance_2` | `coe:resistances` | add_value | `$value / 100` | — | -{X}% to Chaos Resistance |
| `to_max_block_chance` | `coe:block_chance` | add_value | `$value` | — | +{X}% to Max Block chance |
| `totems_gain_to_all_max_elemental_resistances` | `coe:resistances` | add_value | `$value / 100` | — | Totems gain +{X}% to all Max Elemental Resistances |
| `trap_damage` | `coe:damage` | add_multiplied_base | `$value / 100` | — | 10% increased Trap Damage |

## W-OK（11 条）

| id | 属性 | op | amount | 调用方 | 描述 |
|---|---|---|---|---|---|
| `charm_golden_rarity` | `coe:found_item_rarity` | add_value | `$value` | CharmStatusTypes.java:92(`0.20 * effectMult(li`→`0.20 * effectMult(li`) | Charm Golden Rarity |
| `charm_resist_chaos` | `coe:resistances` | add_value | `$value` | CharmStatusTypes.java:55(`0.18 * effectMult(li`→`0.18 * effectMult(li`) | Charm Resist Chaos |
| `charm_resist_cold` | `coe:resistances` | add_value | `$value` | CharmStatusTypes.java:45(`0.25 * effectMult(li`→`0.25 * effectMult(li`) | Charm Resist Cold |
| `charm_resist_fire` | `coe:resistances` | add_value | `$value` | CharmStatusTypes.java:40(`0.25 * effectMult(li`→`0.25 * effectMult(li`) | Charm Resist Fire |
| `charm_resist_lightning` | `coe:resistances` | add_value | `$value` | CharmStatusTypes.java:50(`0.25 * effectMult(li`→`0.25 * effectMult(li`) | Charm Resist Lightning |
| `damage_dealt_bear_fire` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:71(`0.30`→`0.30`) | Damage Dealt Bear Fire |
| `damage_dealt_bear_physical` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:70(`0.30`→`0.30`) | Damage Dealt Bear Physical |
| `damage_dealt_wolf_cold` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:82(`0.20`→`0.20`) | Damage Dealt Wolf Cold |
| `damage_dealt_wolf_physical` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:83(`0.20`→`0.20`) | Damage Dealt Wolf Physical |
| `damage_dealt_wyvern_chaos` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:94(`0.25`→`0.25`) | Damage Dealt Wyvern Chaos |
| `damage_dealt_wyvern_lightning` | `coe:damage` | add_multiplied_base | `$value` | FormStatusTypes.java:93(`0.25`→`0.25`) | Damage Dealt Wyvern Lightning |

## U-UNI（191 条）

| id | 属性 | op | amount | 调用方 | 描述 |
|---|---|---|---|---|---|
| `add_a_curse_after_channelling_for_seconds` | `coe:remove_curse_channelling` | add_multiplied_base | `$value / 100` | — | add a Curse after Channelling for {X} seconds |
| `add_a_curse_when_you_use_a_mana_flask` | `coe:remove_curse_mana_flask` | add_multiplied_base | `$value / 100` | — | add a Curse when you use a Mana Flask |
| `add_cast_crit_damage` | `coe:crit_damage` | add_value | `$value` | — | Add Cast Crit Damage |
| `add_crit_chance` | `coe:crit_chance` | add_value | `$value` | — | 5% chance to Gain ArcaneSurgeDuration when you deal  |
| `add_crit_damage` | `coe:crit_damage` | add_value | `$value` | — | 25% increased Critical Damage Bonus for Attack Damag |
| `add_energy_recharge` | `coe:energy_recharge` | add_value | `$value` | — | Add Energy Recharge |
| `add_exposure` | `coe:ailment_magnitude` | add_value | `$value` | — | Exposure you inflict lowers Resistances by an additi |
| `add_ignite_when_you_warcry` | `coe:remove_ignite_warcry` | add_multiplied_base | `$value / 100` | — | add Ignite when you Warcry |
| `add_max_chaos_resistance` | `coe:max_resistances` | add_value | `$value` | — | +10% to Maximum Chaos Resistance |
| `add_max_cold_resistance` | `coe:max_resistances` | add_value | `$value` | — | +2% to Maximum Cold Resistance if you have at least  |
| `add_max_fire_resistance` | `coe:max_resistances` | add_value | `$value` | — | +2% to Maximum Fire Resistance if you have at least  |
| `add_max_lightning_resistance` | `coe:max_resistances` | add_value | `$value` | — | +2% to Maximum Lightning Resistance if you have at l |
| `add_max_resistances` | `coe:max_resistances` | add_value | `$value` | — | +1% to Maximum Resistances of each ElementalDamage D |
| `all_bonuses_from_equipped_amulet_apply_to_your_minions_instead_of_you` | `coe:bonuses_amulet_minions` | add_multiplied_base | `$value / 100` | — | All bonuses from Equipped Amulet apply to your Minio |
| `archon_recovery_period_expires_slower` | `coe:archon_recovery_period_expires_1` | add_multiplied_base | `$value / 100` | — | Archon recovery period expires {X}% slower |
| `arrows_pierce_an_additional_target` | `coe:arrows_pierce_additional_target` | add_multiplied_base | `$value / 100` | — | Arrows Pierce an additional Target |
| `attack_damage_penetrates_of_enemy_elemental_damage_resistances` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Attack Damage Penetrates {X}% of Enemy Elemental Dam |
| `attacks_have_to_critical_hit_chance` | `coe:attacks_critical` | add_value | `$value` | — | Attacks have +{X}% to Critical Hit Chance |
| `blind_enemies_when_they_stun_you` | `coe:blind_they_stun` | add_multiplied_base | `$value / 100` | — | Blind Enemies when they Stun you |
| `buffs_on_you_expire_slower` | `coe:buffs_expire_slower` | add_multiplied_base | `$value / 100` | — | Buffs on you expire {X}% slower |
| `cannot_be_blinded_while_on_full_health` | `coe:cannot_blinded_full_life` | add_multiplied_base | `$value / 100` | — | Cannot be Blinded while on Full Health |
| `cannot_be_critically_hit_while_parrying` | `coe:cannot_critically_parrying` | add_multiplied_base | `$value / 100` | — | Cannot be Critically Hit while Parrying |
| `chance_dodge` | `coe:dodge_chance` | add_value | `$value` | — | Chance Dodge |
| `chance_flask_no_consume` | `coe:flask_no_consume_chance` | add_value | `$value` | — | 10% chance for Flasks you use to not consume Charges |
| `chance_for_attack_hit_damage_to_apply_ten_incision` | `coe:attack_ten_incision` | add_value | `$value` | — | {X}% chance for Attack Hit Damage to apply ten Incis |
| `chance_for_attacks_to_maim_on_hit_damage_against_poisoned_enemies` | `coe:attacks_maim_poisoned` | add_value | `$value` | — | {X}% chance for Attacks to Maim on Hit Damage agains |
| `chance_for_damage_with_hit_damage_to_be_lucky` | `coe:damage_lucky` | add_value | `$value` | — | {X}% chance for Damage with Hit Damage to be Lucky |
| `chance_for_mace_slam_skills_you_use_yourself_to_cause_an_additional_aftershock` | `coe:mace_slam_cause_additional` | add_value | `$value` | — | {X}% chance for Mace Slam Skills you use yourself to |
| `chance_to_create_an_additional_remnant` | `coe:remnant_extra_chance` | add_value | `$value` | — | {X}% chance to create an additional Remnant |
| `chance_to_defend_with_of_armor` | `coe:defend_armour` | add_value | `$value` | — | {X}% chance to Defend with {X}% of Armor |
| `chance_to_gain_volatility_when_you_are_stunned` | `coe:volatility_generation` | add_multiplied_base | `$value` | — | {X}% chance to gain Volatility when you are Stunned |
| `chance_to_knock_enemies_back_with_hit_damage_at_close_range` | `coe:knockback` | add_value | `$value` | — | {X}% chance to Knock Enemies Back with Hit Damage at |
| `chance_to_recover_all_health_when_you_kill_an_enemy` | `coe:recover_all_life_kill` | add_value | `$value` | — | {X}% chance to Recover all Health when you Kill an E |
| `chance_when_a_charm_is_used_to_use_another_charm_without_consuming_charges` | `coe:charm_activation_chance` | add_value | `$value` | — | {X}% chance when a Charm is used to use another Char |
| `chance_when_you_reload_a_crossbow_to_be_immediate` | `coe:reload_crossbow_immediate` | add_value | `$value` | — | {X}% chance when you Reload a Crossbow to be immedia |
| `consume_all_rage_when_shapeshifting_to_human_form_to_recover_of_max_health_per_rage_consumed` | `coe:rage_shapeshifting_human_form` | add_multiplied_base | `$value / 100` | — | Consume all Rage when Shapeshifting to Human form to |
| `culling_strike_against_enemies_you_mark` | `coe:culling_strike_mark` | add_multiplied_base | `$value / 100` | — | Culling Strike against Enemies you Mark |
| `damage_penetrates_elemental_damage_resistances_for_each_time_you_ve_used_a_skill_that_requires_glory_in_the_past_seconds` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetrates {X}% Elemental Damage Resistances  |
| `damage_penetrates_of_enemy_elemental_resistances` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetrates {X}% of Enemy Elemental Resistance |
| `damage_penetration_elemental_damage_resistances` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetration {X}% Elemental Damage Resistances |
| `damage_penetration_of_enemy_elemental_damage_resistances_while_shapeshifted` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetration {X}% of Enemy Elemental Damage Re |
| `dec_rage_loss_rate` | `coe:rage_loss_rate` | add_multiplied_base | `-$value / 100` | — | Inherent loss of Rage is 15% slower |
| `defend_with_of_armor_against_critical_hits` | `coe:defend_armour_critical` | add_multiplied_base | `$value / 100` | — | Defend with {X}% of Armor against Critical Hits |
| `enemies_near_enemies_you_mark_are_blinded` | `coe:mark_blinded` | add_multiplied_base | `$value / 100` | — | Enemies near Enemies you Mark are Blinded |
| `enemies_you_inflict_bleeding_on_cannot_regenerate_health` | `coe:inflict_bleeding_cannot_regenerate` | add_multiplied_base | `$value / 100` | — | Enemies you inflict Bleeding on cannot Regenerate He |
| `enemies_you_mark_cannot_deal_critical_hits` | `coe:mark_cannot_critical` | add_multiplied_base | `$value / 100` | — | Enemies you Mark cannot deal Critical Hits |
| `faster_curse_activation` | `coe:faster_curse_activation` | add_multiplied_base | `$value / 100` | — | {X}% faster Curse Activation |
| `faster_start_of_energy_shield_recharge_if_you_ve_been_stunned_recently` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | {X}% faster start of Energy Shield Recharge if you'v |
| `faster_start_of_energy_shield_recharge_when_not_on_full_health` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | {X}% faster start of Energy Shield Recharge when not |
| `faster_start_of_energy_shield_recharge_while_affected_by_an_archon_buff` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | {X}% faster start of Energy Shield Recharge while af |
| `faster_start_of_energy_shield_recharge_while_shapeshifted` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | {X}% faster start of Energy Shield Recharge while Sh |
| `faster_start_of_inherent_rage_loss` | `coe:faster_start_inherent_rage` | add_multiplied_base | `$value / 100` | — | {X}% faster start of inherent Rage loss |
| `flask_mana_recovery` | `coe:flask_recovery_rate` | add_multiplied_base | `$value / 100` | — | 10% increased Mana Recovery from Flasks |
| `flask_no_consume` | `coe:flask_no_consume_chance` | add_value | `$value` | — | 10% chance for Flasks you use to not consume Charges |
| `gain_additional_stun_threshold_equal_to_of_item_armor_on_equipped_armor_items` | `coe:additional_stun_threshold_equal_2` | add_multiplied_base | `$value / 100` | — | gain additional Stun Threshold equal to {X}% of Item |
| `gain_physical_thorns_damage_equal_to_of_max_health_while_shapeshifted` | `coe:thorns` | add_multiplied_base | `$value / 100` | — | gain Physical Thorns damage equal to {X}% of Max Hea |
| `gain_rage_when_you_use_a_health_flask` | `coe:rage_life_flask` | add_multiplied_base | `$value / 100` | — | gain {X} Rage when you use a Health Flask |
| `grenade_skills_have_cooldown_use` | `coe:cooldown_recovery_rate` | add_multiplied_base | `$value / 100` | — | Grenade Skills have +{X} Cooldown Use |
| `grenades_have_chance_to_activate_a_second_time` | `coe:grenades_activate_time` | add_value | `$value` | — | Grenades have {X}% chance to activate a second time |
| `health_flasks_gain_charges_per_second` | `coe:life_flasks_charges` | add_multiplied_base | `$value / 100` | — | Health Flasks gain {X} charges per Second |
| `health_leech_health_faster` | `coe:leech_life_faster` | add_multiplied_base | `$value / 100` | — | Health Leech Health {X}% faster |
| `health_leech_health_slower` | `coe:leech_life_slower` | add_multiplied_base | `$value / 100` | — | Health Leech Health {X}% slower |
| `ignites_you_cause_are_reflected_back_to_you` | `coe:ignites_cause_reflected_back` | add_multiplied_base | `$value / 100` | — | Ignites you cause are reflected back to you |
| `ignore_all_movement_penalties_from_equip_armor` | `coe:ignore_movement_penalties_armour` | add_multiplied_base | `$value / 100` | — | Ignore all Movement Penalties from Equip Armor |
| `inc_amount_leech_health` | `coe:amount_leeched` | add_multiplied_base | `$value / 100` | — | 8% increased amount of Life LifeLeech |
| `inc_aura_magnitude` | `coe:aura_magnitude` | add_multiplied_base | `$value / 100` | — | Aura Skills have 5% increased BuffMagnitude |
| `inc_buff_effect_taken` | `coe:buff_effect_taken` | add_multiplied_base | `$value` | — | Inc Buff Effect Taken |
| `inc_charm_effect` | `coe:charm_effect` | add_multiplied_base | `$value / 100` | — | Charms applied to you have 25% increased Effect |
| `inc_crit_chance` | `coe:crit_chance` | add_multiplied_base | `$value / 100` | — | 10% increased Critical Hit Chance |
| `inc_damage_is_taken_mana` | `coe:mana_take_of_damage` | add_value | `$value / 100` | — | 4% of Damage is taken from Mana before Life |
| `inc_damage_over_time` | `coe:damage_over_time_multiplier` | add_multiplied_base | `$value / 100` | — | Take 50% less Damage over Time if you've started tak |
| `inc_faster_start_energy_shield` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | 4% faster start of Energy Shield Recharge |
| `inc_faster_start_energy_shield_fasteresrechargestart` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | 10% faster start of Energy Shield Recharge |
| `inc_flask_life_recovery` | `coe:flask_recovery_rate` | add_multiplied_base | `$value / 100` | — | 10% increased Life Recovery from Flasks |
| `inc_flask_mana_recovery` | `coe:flask_recovery_rate` | add_multiplied_base | `$value / 100` | — | 10% increased Mana Recovery from Flasks |
| `inc_glory_generation` | `coe:glory_generation` | add_multiplied_base | `$value` | — | 15% increased Glory generation |
| `inc_incision_effect` | `coe:incision_effect` | add_multiplied_base | `$value / 100` | — | 50% increased effect of Incision |
| `inc_increases_reductions_projectile_speed` | `coe:projectile_speed` | add_multiplied_base | `$value / 100` | — | Increases and Reductions to Projectile Speed also ap |
| `inc_increases_reductions_projectile_speed_bow` | `coe:projectile_speed` | add_multiplied_base | `$value / 100` | — | Increases and Reductions to Projectile Speed also ap |
| `inc_leech_health` | `coe:leech` | add_value | `$value / 100` | — | 10% of Thorns Damage Leeched as Life |
| `inc_leech_rate` | `coe:leech_rate` | add_multiplied_base | `$value / 100` | — | 12% increased amount of Life LifeLeech |
| `inc_mana_take` | `coe:mana_take_of_damage` | add_value | `$value / 100` | — | 4% of Damage is taken from Mana before Life |
| `inc_meta_energy` | `coe:meta_energy_gain` | add_multiplied_base | `$value / 100` | — | Meta Skills gain 8% increased Energy |
| `inc_minions_cooldown_recovery_rate` | `coe:cooldown_recovery_rate` | add_multiplied_base | `$value / 100` | — | Minions have 8% increased Cooldown Recovery Rate for |
| `inc_minions_cooldown_recovery_rate_command` | `coe:cooldown_recovery_rate` | add_multiplied_base | `$value / 100` | — | Minions have 20% increased Cooldown Recovery Rate fo |
| `inc_minions_revive_faster` | `coe:revive_speed` | add_multiplied_base | `$value / 100` | — | Minions Reviving 5% faster |
| `inc_movement_penalty_reduction` | `coe:movement_penalty_reduction` | add_value | `$value / 100` | — | 20% increased Evasion Rating |
| `inc_projectile_speed` | `coe:projectile_speed` | add_multiplied_base | `$value / 100` | — | 8% increased Projectile Speed |
| `inc_quiver_bonuses` | `coe:quiver_bonuses` | add_multiplied_base | `$value / 100` | — | 6% increased bonuses gained from Equipped Quiver |
| `inc_reload_speed` | `coe:cross_bow_reload_time` | add_multiplied_base | `$value / 100` | — | 15% increased Crossbow Reload Speed |
| `inc_reservation_efficiency` | `coe:reservation_efficiency` | add_value | `$value / 100` | — | 6% increased Reservation Efficiency of Herald Skills |
| `inc_slower_start_energy_shield` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | 20% slower start of Energy Shield Recharge |
| `inc_slower_start_energy_shield_no_condition` | `coe:energy_recharge` | add_multiplied_base | `$value / 100` | — | 20% slower start of Energy Shield Recharge |
| `inc_taken_crit_damage` | `coe:taken_crit_damage` | add_multiplied_base | `$value` | — | Inc Taken Crit Damage |
| `inc_trap_throwing_speed` | `coe:trap_throwing_speed` | add_multiplied_base | `$value / 100` | — | 6% increased Trap Throwing Speed |
| `inc_volatility_generation` | `coe:volatility_generation` | add_multiplied_base | `$value` | — | Inc Volatility Generation |
| `increased_amount_of_mana_mana_leech` | `coe:amount_leeched` | add_multiplied_base | `$value / 100` | — | {X}% increased amount of Mana Mana Leech |
| `increased_area_of_effect_for_attacks` | `coe:area_of_effect` | add_multiplied_base | `$value / 100` | — | {X}% increased Area of Effect for Attacks |
| `increased_bolt_speed` | `coe:projectile_speed` | add_multiplied_base | `$value / 100` | — | {X}% increased Bolt Speed |
| `increased_buff_effect_of_curses_on_you` | `coe:curse_effect_taken` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Effect of Curses on you |
| `increased_buff_effect_of_shock_on_you` | `coe:shock_effect_taken` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Effect of Shock on you |
| `increased_buff_effect_of_withered_on_you` | `coe:withered_effect_taken` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Effect of Withered on you |
| `increased_buff_magnitude_of_impales_inflicted_with_spells` | `coe:magnitude_impales_inflicted_spells` | add_multiplied_base | `$value / 100` | — | {X}% increased Buff Magnitude of Impales inflicted w |
| `increased_charm_charges_gain` | `coe:charm_charges_gained` | add_multiplied_base | `$value / 100` | — | {X}% increased Charm Charges gain |
| `increased_charm_charges_used` | `coe:charm_charges_used` | add_multiplied_base | `$value / 100` | — | {X}% increased Charm Charges used |
| `increased_cooldown_recovery_rate` | `coe:cooldown_recovery_rate` | add_multiplied_base | `$value / 100` | — | {X}% increased Cooldown Recovery Rate |
| `increased_critical_damage_bonus` | `coe:crit_damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Critical Damage Bonus |
| `increased_critical_damage_bonus_if_you_ve_consumed_a_power_charge_recently` | `coe:crit_damage` | add_multiplied_base | `$value / 100` | — | {X}% increased Critical Damage Bonus if you've consu |
| `increased_critical_hit_chance_against_immobilised_enemies` | `coe:crit_chance` | add_multiplied_base | `$value / 100` | — | {X}% increased Critical Hit Chance against Immobilis |
| `increased_culling_strike_threshold` | `coe:culling_strike_threshold` | add_multiplied_base | `$value / 100` | — | {X}% increased Culling Strike Threshold |
| `increased_duration_of_puppet_master` | `coe:puppet_master_duration` | add_value | `$value` | — | 原误挂在 INCREASED_EFFECT_OF_PUPPET_MASTER 下，Step6 拆分独立  |
| `increased_effect_of_archon_buffs_on_you` | `coe:archon_effect_taken` | add_multiplied_base | `$value / 100` | — | {X}% increased effect of Archon Buffs on you |
| `increased_effect_of_puppet_master` | `coe:puppet_master_effect` | add_value | `$value` | — | {X}% increased Effect of Puppet Master |
| `increased_elemental_ailments_ailment_application_if_you_have_shapeshifted_to_an_animal_form_recently` | `coe:elemental_ailment_application_shapeshifted` | add_multiplied_base | `$value / 100` | — | {X}% increased Elemental Ailments Ailment Applicatio |
| `increased_energy_shield_from_equipped_focus` | `coe:es_from_focus` | add_multiplied_base | `$value / 100` | — | {X}% increased Energy Shield from Equipped Focus |
| `increased_energy_shield_from_equipped_helmet` | `coe:es_from_helmet` | add_multiplied_base | `$value / 100` | — | {X}% increased Energy Shield from Equipped Helmet |
| `increased_flask_charges_used_from_mana_flasks` | `coe:flask_charges_gained` | add_multiplied_base | `$value / 100` | — | {X}% increased Flask Charges used from Mana Flasks |
| `increased_health_cost_of_skills` | `coe:skill_cost_life` | add_multiplied_base | `$value / 100` | — | {X}% increased Health Cost of Skills |
| `increased_health_flask_charges_gain` | `coe:flask_charges_gained` | add_multiplied_base | `$value / 100` | — | {X}% increased Health Flask Charges gain |
| `increased_health_recovery_from_flasks_used_when_on_low_health` | `coe:flask_recovery_rate` | add_multiplied_base | `$value / 100` | — | {X}% increased Health Recovery from Flasks used when |
| `increased_knockback_distance` | `coe:knockback` | add_multiplied_base | `$value / 100` | — | {X}% increased Knockback Distance |
| `increased_light_radius` | `coe:light_radius` | add_multiplied_base | `$value / 100` | — | {X}% increased Light Radius |
| `increased_mana_cost_efficiency` | `coe:cost_efficiency` | add_multiplied_base | `$value / 100` | — | {X}% increased Mana Cost Efficiency |
| `increased_mana_cost_of_skills` | `coe:skill_cost_mana` | add_multiplied_base | `$value / 100` | — | {X}% increased Mana Cost of Skills |
| `increased_parry_range` | `coe:parry_range` | add_multiplied_base | `$value / 100` | — | {X}% increased Parry Range |
| `increased_shock_chance_against_electrocuted_enemies` | `coe:shock_electrocuted` | add_value | `$value` | — | {X}% increased Shock Chance against Electrocuted Ene |
| `increased_speed_of_recoup_effects` | `coe:recovery_rate` | add_multiplied_base | `$value / 100` | — | {X}% increased speed of Recoup Effects |
| `increased_stun_recovery` | `coe:stun_recovery` | add_multiplied_base | `$value / 100` | — | {X}% increased Stun Recovery |
| `increased_thorns_damage` | `coe:thorns` | add_multiplied_base | `$value / 100` | — | {X}% increased Thorns damage |
| `increased_totem_placement_range` | `coe:totem_placement_range` | add_multiplied_base | `$value / 100` | — | {X}% increased Totem Placement range |
| `invocated_skills_have_increased_max_energy` | `coe:invocated_energy` | add_multiplied_base | `$value / 100` | — | Invocated skills have {X}% increased Max Energy |
| `leech_health` | `coe:leech` | add_value | `$value / 100` | — | Leech Health |
| `leech_mana` | `coe:leech` | add_value | `$value / 100` | — | Leech Mana |
| `mana_flasks_gain_charges_per_second` | `coe:mana_flasks_charges` | add_multiplied_base | `$value / 100` | — | Mana Flasks gain {X} charges per Second |
| `metres_to_melee_strike_range_if_you_ve_dealt_a_projectile_attack_hit_damage_in_the_past_eight_seconds` | `coe:metres_melee_strike_range_2` | add_value | `$value` | — | +{X} metres to Melee Strike Range if you've dealt a  |
| `minions_have_chance_to_inflict_withered_on_hit_damage` | `coe:minions_inflict_withered` | add_value | `$value` | — | Minions have {X}% chance to inflict Withered on Hit  |
| `minions_have_increased_area_of_effect` | `coe:area_of_effect` | add_multiplied_base | `$value / 100` | — | Minions have {X}% increased Area of Effect |
| `minions_have_increased_critical_hit_chance` | `coe:minions_critical` | add_value | `$value` | — | Minions have {X}% increased Critical Hit Chance |
| `minions_have_increased_health_recovery_rate` | `coe:recovery_rate` | add_multiplied_base | `$value / 100` | — | Minions have {X}% increased Health Recovery rate |
| `minions_have_to_all_max_elemental_resistances` | `coe:max_resistances` | add_value | `$value` | — | Minions have +{X}% to all Max Elemental Resistances |
| `minions_have_to_max_cold_resistances` | `coe:max_resistances` | add_value | `$value` | — | Minions have +{X}% to Max Cold Resistances |
| `minions_have_to_max_fire_resistances` | `coe:max_resistances` | add_value | `$value` | — | Minions have +{X}% to Max Fire Resistances |
| `minions_have_to_max_lightning_resistances` | `coe:max_resistances` | add_value | `$value` | — | Minions have +{X}% to Max Lightning Resistances |
| `minions_reviving_faster_if_all_your_minions_are_companions` | `coe:revive_speed` | add_multiplied_base | `$value / 100` | — | Minions Reviving {X}% faster if all your Minions are |
| `more_amount_leech` | `coe:amount_leeched` | add_multiplied_total | `$value / 100` | — | 50% more amount of Life Leeched Leech Life 67% less  |
| `more_bleeding_magnitude_inflicted` | `coe:ailment_magnitude` | add_multiplied_total | `$value / 100` | — | 10% increased BuffMagnitude of Bleeding you inflict |
| `more_damage_against_heavy_stunned_enemies_with_maces` | `coe:damage` | add_multiplied_total | `$value / 100` | — | {X}% more Damage against Heavy Stunned Enemies with  |
| `of_damage_taken_bypasses_energy_shield` | `coe:damage_taken_bypasses_energy` | add_multiplied_base | `$value / 100` | — | {X}% of Damage taken bypasses Energy Shield |
| `of_max_health_stat_conversion_to_energy_shield` | `coe:life_converted_energy_shield` | add_multiplied_base | `$value / 100` | — | {X}% of Max Health Stat Conversion to Energy Shield |
| `penetrate_chaos` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Penetrate Chaos |
| `penetrate_cold` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetration 6% Cold Resistance |
| `penetrate_fire` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetration 6% Fire Resistance |
| `penetrate_lightning` | `coe:resistance_penetration` | add_value | `$value / 100` | — | Damage Penetration 6% Lightning Resistance |
| `plants_have_a_chance_to_immediately_overgrow` | `coe:plants_immediately_overgrow` | add_value | `$value` | — | Plants have a {X}% chance to immediately Overgrow |
| `remnants_can_be_collected_from_further_away` | `coe:remnants_collected_further_away` | add_multiplied_base | `$value / 100` | — | Remnants can be collected from {X}% further away |
| `skills_gain_combo_slower` | `coe:lose_combo_slower` | add_multiplied_base | `$value / 100` | — | Skills gain Combo {X}% slower |
| `skills_have_a_chance_to_not_consume_glory` | `coe:glory` | add_value | `$value` | — | Skills have a {X}% chance to not consume Glory |
| `skills_have_chance_to_not_add_elemental_infusions_but_still_count_as_consuming_them` | `coe:remove_elemental_infusions_but` | add_value | `$value` | — | Skills have {X}% chance to not add Elemental Infusio |
| `skills_which_create_fissures_have_a_chance_to_create_an_additional_fissure` | `coe:which_create_fissures_additional` | add_value | `$value` | — | Skills which create Fissures have a {X}% chance to c |
| `slam_skills_you_use_yourself_have_increased_aftershock_area_of_effect` | `coe:aftershock_area` | add_multiplied_base | `$value / 100` | — | Slam Skills you use yourself have {X}% increased Aft |
| `status_extra_stacks_bleed` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_chill` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_electrocute` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_freeze` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_ignite` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_pin` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_poison` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_extra_stacks_shock` | `coe:status_extra_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_bleed` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_chill` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_electrocute` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_freeze` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_ignite` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_pin` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_poison` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `status_max_stacks_shock` | `coe:status_max_stacks` | add_multiplied_base | `$value` | — |  |
| `strikes_deal_splash_damage` | `coe:area_of_effect` | add_multiplied_base | `$value / 100` | — | Strikes deal Splash Damage |
| `surpassing_chance_to_gain_a_puppet_master_stack_whenever_you_use_a_command_skill` | `coe:puppet_master_chance` | add_value | `$value` | — | {X}% Surpassing Chance to gain a Puppet Master stack |
| `taken_crit` | `coe:taken_crit_chance` | add_value | `$value` | — | Taken Crit |
| `to_all_max_elemental_resistances_if_you_have_at_least_red_green_and_blue_support_gems_socketed` | `coe:elemental_resistances_red_green` | add_value | `$value` | — | +{X}% to all Max Elemental Resistances if you have a |
| `unaffected_by_chill_during_dodge_roll` | `coe:unaffected_chill_during_dodge` | add_multiplied_base | `$value / 100` | — | Unaffected by Chill during Dodge Roll |
| `unaffected_by_chill_while_mana_leech_mana` | `coe:unaffected_chill_leeching_mana` | add_multiplied_base | `$value / 100` | — | Unaffected by Chill while Mana Leech Mana |
| `unaffected_by_chilled_ground` | `coe:unaffected_chilled_ground` | add_multiplied_base | `$value / 100` | — | Unaffected by Chilled Ground |
| `unaffected_by_elemental_weakness` | `coe:unaffected_elemental_weakness` | add_multiplied_base | `$value / 100` | — | Unaffected by Elemental Weakness |
| `unaffected_by_shocked_ground` | `coe:unaffected_shocked_ground` | add_multiplied_base | `$value / 100` | — | Unaffected by Shocked Ground |
| `warcries_debilitate_enemies` | `coe:warcries_debilitate` | add_multiplied_base | `$value / 100` | — | Warcries Debilitate Enemies |
| `warcries_empowered_an_additional_attack` | `coe:warcries_empower_additional_attack` | add_multiplied_base | `$value / 100` | — | Warcries Empowered an additional Attack |
| `warcries_have_a_minimum_of_power` | `coe:warcries_minimum_power` | add_multiplied_base | `$value / 100` | — | Warcries have a minimum of {X} Power |
| `you_can_apply_an_additional_curse` | `coe:additional_curse` | add_multiplied_base | `$value / 100` | — | You can apply an additional Curse |
| `your_aura_buffs_do_not_affect_allies` | `coe:aura_buffs_affect_allies` | add_multiplied_base | `$value / 100` | — | Your Aura Buffs do not affect Allies |
| `your_minions_are_gigantic` | `coe:minions_gigantic` | add_multiplied_base | `$value / 100` | — | Your Minions are Gigantic |
