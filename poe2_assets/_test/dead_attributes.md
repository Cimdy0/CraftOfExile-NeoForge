# 死属性扫描：stat_entry 引用了但运行时从不读取的属性

- AttributeRegister 声明属性：**209**
- stat_entry 实际引用的 coe:* 属性：**172**
- 其中**无运行时读取器**：**92**
- 受影响 stat 条目（修饰恒不被消费）：**100**

> 判定：排除 data/gen/**、AttributeRegister.java、StatIds.java 后，全工程 .java 无该常量引用 → 写入的 AttributeModifier 无任何消费方。

## 死属性明细（按受影响条目数降序）

| 属性 | 常量 | 受影响条目数 | stat 条目 |
|---|---|---:|---|
| `coe:cooldown_recovery_rate` | `COOLDOWN_RECOVERY_RATE` | 4 | `grenade_skills_have_cooldown_use`, `inc_minions_cooldown_recovery_rate`, `inc_minions_cooldown_recovery_rate_command`, `increased_cooldown_recovery_rate` |
| `coe:area_of_effect` | `AREA_OF_EFFECT` | 3 | `increased_area_of_effect_for_attacks`, `minions_have_increased_area_of_effect`, `strikes_deal_splash_damage` |
| `coe:found_item_number` | `FOUND_ITEM_NUMBER` | 2 | `add_found_item_number`, `inc_found_quantity` |
| `coe:taken_crit_damage` | `TAKEN_CRIT_DAMAGE` | 2 | `inc_taken_crit_damage`, `livingmod_coe_taken_crit_damage_op_add_value_-0_8` |
| `coe:thorns` | `THORNS` | 2 | `gain_physical_thorns_damage_equal_to_of_max_health_while_shapeshifted`, `increased_thorns_damage` |
| `coe:additional_curse` | `ADDITIONAL_CURSE` | 1 | `you_can_apply_an_additional_curse` |
| `coe:additional_stun_threshold_equal_2` | `ADDITIONAL_STUN_THRESHOLD_EQUAL_2` | 1 | `gain_additional_stun_threshold_equal_to_of_item_armor_on_equipped_armor_items` |
| `coe:arrows_pierce_additional_target` | `ARROWS_PIERCE_ADDITIONAL_TARGET` | 1 | `arrows_pierce_an_additional_target` |
| `coe:attack_ten_incision` | `ATTACK_TEN_INCISION` | 1 | `chance_for_attack_hit_damage_to_apply_ten_incision` |
| `coe:attacks_critical` | `ATTACKS_CRITICAL` | 1 | `attacks_have_to_critical_hit_chance` |
| `coe:attacks_maim_poisoned` | `ATTACKS_MAIM_POISONED` | 1 | `chance_for_attacks_to_maim_on_hit_damage_against_poisoned_enemies` |
| `coe:aura_buffs_affect_allies` | `AURA_BUFFS_AFFECT_ALLIES` | 1 | `your_aura_buffs_do_not_affect_allies` |
| `coe:aura_magnitude` | `AURA_MAGNITUDE` | 1 | `inc_aura_magnitude` |
| `coe:blind_they_stun` | `BLIND_THEY_STUN` | 1 | `blind_enemies_when_they_stun_you` |
| `coe:bonuses_amulet_minions` | `BONUSES_AMULET_MINIONS` | 1 | `all_bonuses_from_equipped_amulet_apply_to_your_minions_instead_of_you` |
| `coe:buffs_expire_slower` | `BUFFS_EXPIRE_SLOWER` | 1 | `buffs_on_you_expire_slower` |
| `coe:cannot_blinded_full_life` | `CANNOT_BLINDED_FULL_LIFE` | 1 | `cannot_be_blinded_while_on_full_health` |
| `coe:cannot_critically_parrying` | `CANNOT_CRITICALLY_PARRYING` | 1 | `cannot_be_critically_hit_while_parrying` |
| `coe:cost_efficiency` | `COST_EFFICIENCY` | 1 | `increased_mana_cost_efficiency` |
| `coe:critical_herald` | `CRITICAL_HERALD` | 1 | `to_critical_hit_chance_of_herald_skills` |
| `coe:culling_strike_mark` | `CULLING_STRIKE_MARK` | 1 | `culling_strike_against_enemies_you_mark` |
| `coe:culling_strike_threshold` | `CULLING_STRIKE_THRESHOLD` | 1 | `increased_culling_strike_threshold` |
| `coe:damage_lucky` | `DAMAGE_LUCKY` | 1 | `chance_for_damage_with_hit_damage_to_be_lucky` |
| `coe:defend_armour` | `DEFEND_ARMOUR` | 1 | `chance_to_defend_with_of_armor` |
| `coe:defend_armour_critical` | `DEFEND_ARMOUR_CRITICAL` | 1 | `defend_with_of_armor_against_critical_hits` |
| `coe:elemental_ailment_application_shapeshifted` | `ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED` | 1 | `increased_elemental_ailments_ailment_application_if_you_have_shapeshifted_to_an_animal_form_recently` |
| `coe:elemental_resistances_red_green` | `ELEMENTAL_RESISTANCES_RED_GREEN` | 1 | `to_all_max_elemental_resistances_if_you_have_at_least_red_green_and_blue_support_gems_socketed` |
| `coe:es_from_focus` | `ES_FROM_FOCUS` | 1 | `increased_energy_shield_from_equipped_focus` |
| `coe:es_from_helmet` | `ES_FROM_HELMET` | 1 | `increased_energy_shield_from_equipped_helmet` |
| `coe:faster_curse_activation` | `FASTER_CURSE_ACTIVATION` | 1 | `faster_curse_activation` |
| `coe:faster_start_inherent_rage` | `FASTER_START_INHERENT_RAGE` | 1 | `faster_start_of_inherent_rage_loss` |
| `coe:glory_generation` | `GLORY_GENERATION` | 1 | `inc_glory_generation` |
| `coe:grenades_activate_time` | `GRENADES_ACTIVATE_TIME` | 1 | `grenades_have_chance_to_activate_a_second_time` |
| `coe:ignites_cause_reflected_back` | `IGNITES_CAUSE_REFLECTED_BACK` | 1 | `ignites_you_cause_are_reflected_back_to_you` |
| `coe:ignore_movement_penalties_armour` | `IGNORE_MOVEMENT_PENALTIES_ARMOUR` | 1 | `ignore_all_movement_penalties_from_equip_armor` |
| `coe:inflict_bleeding_cannot_regenerate` | `INFLICT_BLEEDING_CANNOT_REGENERATE` | 1 | `enemies_you_inflict_bleeding_on_cannot_regenerate_health` |
| `coe:invocated_energy` | `INVOCATED_ENERGY` | 1 | `invocated_skills_have_increased_max_energy` |
| `coe:leech_life_faster` | `LEECH_LIFE_FASTER` | 1 | `health_leech_health_faster` |
| `coe:leech_life_slower` | `LEECH_LIFE_SLOWER` | 1 | `health_leech_health_slower` |
| `coe:level_all_chaos` | `LEVEL_ALL_CHAOS` | 1 | `to_level_of_all_chaos_skills` |
| `coe:level_all_cold` | `LEVEL_ALL_COLD` | 1 | `to_level_of_all_cold_skills` |
| `coe:life_converted_energy_shield` | `LIFE_CONVERTED_ENERGY_SHIELD` | 1 | `of_max_health_stat_conversion_to_energy_shield` |
| `coe:life_dexterity` | `LIFE_DEXTERITY` | 1 | `health_per_dexterity` |
| `coe:life_flasks_charges` | `LIFE_FLASKS_CHARGES` | 1 | `health_flasks_gain_charges_per_second` |
| `coe:light_radius` | `LIGHT_RADIUS` | 1 | `increased_light_radius` |
| `coe:lightning_infusions` | `LIGHTNING_INFUSIONS` | 1 | `to_max_lightning_infusions` |
| `coe:lose_combo_slower` | `LOSE_COMBO_SLOWER` | 1 | `skills_gain_combo_slower` |
| `coe:mace_slam_cause_additional` | `MACE_SLAM_CAUSE_ADDITIONAL` | 1 | `chance_for_mace_slam_skills_you_use_yourself_to_cause_an_additional_aftershock` |
| `coe:magnitude_impales_inflicted_spells` | `MAGNITUDE_IMPALES_INFLICTED_SPELLS` | 1 | `increased_buff_magnitude_of_impales_inflicted_with_spells` |
| `coe:mana_flasks_charges` | `MANA_FLASKS_CHARGES` | 1 | `mana_flasks_gain_charges_per_second` |
| `coe:mark_blinded` | `MARK_BLINDED` | 1 | `enemies_near_enemies_you_mark_are_blinded` |
| `coe:mark_cannot_critical` | `MARK_CANNOT_CRITICAL` | 1 | `enemies_you_mark_cannot_deal_critical_hits` |
| `coe:max_banner` | `MAX_BANNER` | 1 | `add_max_banner` |
| `coe:max_fissure` | `MAX_FISSURE` | 1 | `add_max_fissure` |
| `coe:max_minion` | `MAX_MINION` | 1 | `add_max_minion` |
| `coe:max_orb` | `MAX_ORB` | 1 | `add_max_orb` |
| `coe:max_totem` | `MAX_TOTEM` | 1 | `add_max_totem` |
| `coe:meta_energy_gain` | `META_ENERGY_GAIN` | 1 | `inc_meta_energy` |
| `coe:metres_dodge_roll_distance` | `METRES_DODGE_ROLL_DISTANCE` | 1 | `metres_to_dodge_roll_distance_while_surrounded` |
| `coe:metres_melee_strike_range` | `METRES_MELEE_STRIKE_RANGE` | 1 | `metres_to_melee_strike_range` |
| `coe:metres_melee_strike_range_2` | `METRES_MELEE_STRIKE_RANGE_2` | 1 | `metres_to_melee_strike_range_if_you_ve_dealt_a_projectile_attack_hit_damage_in_the_past_eight_seconds` |
| `coe:minions_critical` | `MINIONS_CRITICAL` | 1 | `minions_have_increased_critical_hit_chance` |
| `coe:minions_gigantic` | `MINIONS_GIGANTIC` | 1 | `your_minions_are_gigantic` |
| `coe:minions_inflict_withered` | `MINIONS_INFLICT_WITHERED` | 1 | `minions_have_chance_to_inflict_withered_on_hit_damage` |
| `coe:movement_penalty_reduction` | `MOVEMENT_PENALTY_REDUCTION` | 1 | `inc_movement_penalty_reduction` |
| `coe:plants_immediately_overgrow` | `PLANTS_IMMEDIATELY_OVERGROW` | 1 | `plants_have_a_chance_to_immediately_overgrow` |
| `coe:quiver_bonuses` | `QUIVER_BONUSES` | 1 | `inc_quiver_bonuses` |
| `coe:rage_life_flask` | `RAGE_LIFE_FLASK` | 1 | `gain_rage_when_you_use_a_health_flask` |
| `coe:rage_shapeshifting_human_form` | `RAGE_SHAPESHIFTING_HUMAN_FORM` | 1 | `consume_all_rage_when_shapeshifting_to_human_form_to_recover_of_max_health_per_rage_consumed` |
| `coe:recover_all_life_kill` | `RECOVER_ALL_LIFE_KILL` | 1 | `chance_to_recover_all_health_when_you_kill_an_enemy` |
| `coe:reload_crossbow_immediate` | `RELOAD_CROSSBOW_IMMEDIATE` | 1 | `chance_when_you_reload_a_crossbow_to_be_immediate` |
| `coe:remove_curse_channelling` | `REMOVE_CURSE_CHANNELLING` | 1 | `add_a_curse_after_channelling_for_seconds` |
| `coe:remove_curse_mana_flask` | `REMOVE_CURSE_MANA_FLASK` | 1 | `add_a_curse_when_you_use_a_mana_flask` |
| `coe:remove_elemental_infusions_but` | `REMOVE_ELEMENTAL_INFUSIONS_BUT` | 1 | `skills_have_chance_to_not_add_elemental_infusions_but_still_count_as_consuming_them` |
| `coe:remove_ignite_warcry` | `REMOVE_IGNITE_WARCRY` | 1 | `add_ignite_when_you_warcry` |
| `coe:shock_electrocuted` | `SHOCK_ELECTROCUTED` | 1 | `increased_shock_chance_against_electrocuted_enemies` |
| `coe:skill_cost_life` | `SKILL_COST_LIFE` | 1 | `increased_health_cost_of_skills` |
| `coe:skill_cost_mana` | `SKILL_COST_MANA` | 1 | `increased_mana_cost_of_skills` |
| `coe:stacks_puppet_master` | `STACKS_PUPPET_MASTER` | 1 | `max_stacks_of_puppet_master` |
| `coe:stun_recovery` | `STUN_RECOVERY` | 1 | `increased_stun_recovery` |
| `coe:stun_threshold_strength` | `STUN_THRESHOLD_STRENGTH` | 1 | `to_stun_threshold_per_strength` |
| `coe:thorns_critical` | `THORNS_CRITICAL` | 1 | `to_thorns_critical_hit_chance` |
| `coe:totem_placement_range` | `TOTEM_PLACEMENT_RANGE` | 1 | `increased_totem_placement_range` |
| `coe:trap_throwing_speed` | `TRAP_THROWING_SPEED` | 1 | `inc_trap_throwing_speed` |
| `coe:unaffected_chill_during_dodge` | `UNAFFECTED_CHILL_DURING_DODGE` | 1 | `unaffected_by_chill_during_dodge_roll` |
| `coe:unaffected_chill_leeching_mana` | `UNAFFECTED_CHILL_LEECHING_MANA` | 1 | `unaffected_by_chill_while_mana_leech_mana` |
| `coe:unaffected_chilled_ground` | `UNAFFECTED_CHILLED_GROUND` | 1 | `unaffected_by_chilled_ground` |
| `coe:unaffected_elemental_weakness` | `UNAFFECTED_ELEMENTAL_WEAKNESS` | 1 | `unaffected_by_elemental_weakness` |
| `coe:unaffected_shocked_ground` | `UNAFFECTED_SHOCKED_GROUND` | 1 | `unaffected_by_shocked_ground` |
| `coe:warcries_debilitate` | `WARCRIES_DEBILITATE` | 1 | `warcries_debilitate_enemies` |
| `coe:warcries_minimum_power` | `WARCRIES_MINIMUM_POWER` | 1 | `warcries_have_a_minimum_of_power` |
| `coe:which_create_fissures_additional` | `WHICH_CREATE_FISSURES_ADDITIONAL` | 1 | `skills_which_create_fissures_have_a_chance_to_create_an_additional_fissure` |
