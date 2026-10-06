# 旧 StatIds -> 新 StatIds 精确映射报告

- 旧条目总数：**1460**
- 新条目总数：**1166**
- 被代码引用的旧常量：**1395**

| 分类 | 数量 | 处置 |
|---|---|---|
| A 精确命中（可自动改名） | 878 | 直接替换引用 |
| B 失配且被引用 | 582 | **须人工裁决** |
| C 失配且无引用 | 0 | 可直接废弃 |
| D 新条目无旧实现 | 608 | 引擎待实现 |

---

## §B 失配且被代码引用（必须裁决，否则编译不过）

| 旧常量 | 旧 id | 旧注释 | 规范化后（新表中无此模板） |
|---|---|---|---|
| `ACCURACY` | `accuracy` | Accuracy | Accuracy |
| `ACTION_SPEED_ATTACK_SPEED` | `action_speed_attack_speed` | Action Speed Attack Speed | Action Speed Attack Speed |
| `ACTION_SPEED_BLOCK_BREAK_SPEED` | `action_speed_block_break_speed` | Action Speed Block Break Speed | Action Speed Block Break Speed |
| `ACTION_SPEED_CAST_SPEED` | `action_speed_cast_speed` | Action Speed Cast Speed | Action Speed Cast Speed |
| `ACTION_SPEED_FLYING_SPEED` | `action_speed_flying_speed` | Action Speed Flying Speed | Action Speed Flying Speed |
| `ACTION_SPEED_JUMP_STRENGTH` | `action_speed_jump_strength` | Action Speed Jump Strength | Action Speed Jump Strength |
| `ACTION_SPEED_MOVEMENT_SPEED` | `action_speed_movement_speed` | Action Speed Movement Speed | Action Speed Movement Speed |
| `ACTION_SPEED_SNEAKING_SPEED` | `action_speed_sneaking_speed` | Action Speed Sneaking Speed | Action Speed Sneaking Speed |
| `ACTION_SPEED_SUBMERGED_MINING_SPEED` | `action_speed_submerged_mining_speed` | Action Speed Submerged Mining Speed | Action Speed Submerged Mining Speed |
| `ADD_ALL_ELEMENTAL_RESISTANCES_RED` | `add_all_elemental_resistances_red` | Add All Elemental Resistances Red | add All Elemental Resistances Red |
| `ADD_CAST_CRIT_DAMAGE` | `add_cast_crit_damage` | Add Cast Crit Damage | add Cast Crit Damage |
| `ADD_CHAOS_DAMAGE` | `add_chaos_damage` | Add Chaos Damage | add Chaos Damage |
| `ADD_COLD_DAMAGE` | `add_cold_damage` | Add Cold Damage | add Cold Damage |
| `ADD_COLD_INFUSIONS` | `add_cold_infusions` | +1 to maximum ElementalInfusion | +{X} to Max Elemental Infusion |
| `ADD_COOLDOWN_USE` | `add_cooldown_use` | Add Cooldown Use | add Cooldown Use |
| `ADD_CRIT_CHANCE` | `add_crit_chance` | 5% chance to Gain ArcaneSurgeDuration when you deal a Critical Hit | {X}% chance to gain Arcane Surge Duration when you deal a Critical Hit |
| `ADD_DARKNESS_LEVEL` | `add_darkness_level` | +10 to Maximum Darkness per Level | +{X} to Max Darkness per Level |
| `ADD_ELEMENTAL_DAMAGE` | `add_elemental_damage` | Add Elemental Damage | add Elemental Damage |
| `ADD_ELEMENTAL_RESISTANCE_MINION` | `add_elemental_resistance_minion` | Add Elemental Resistance Minion | add Elemental Resistance Minion |
| `ADD_ENERGY_RECHARGE` | `add_energy_recharge` | Add Energy Recharge | add Energy Recharge |
| `ADD_FIRE_DAMAGE` | `add_fire_damage` | Add Fire Damage | add Fire Damage |
| `ADD_FIRE_INFUSIONS` | `add_fire_infusions` | +1 to maximum ElementalInfusion | +{X} to Max Elemental Infusion |
| `ADD_FOUND_ITEM_NUMBER` | `add_found_item_number` | Add Found Item Number | add Found Item Number |
| `ADD_FOUND_ITEM_RARITY` | `add_found_item_rarity` | Add Found Item Rarity | add Found Item Rarity |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | Add Launch Additional Projectile Bolt | add Launch Additional Projectile Bolt |
| `ADD_LIMIT_ELEMENTAL` | `add_limit_elemental` | +2 to Limit for ElementalDamage Skills | +{X} to Limit for Elemental Damage Skills |
| `ADD_MAX_BLOCK_CHANCE` | `add_max_block_chance` | Add Max Block Chance | add Max Block Chance |
| `ADD_MAX_CHAOS_RESISTANCE` | `add_max_chaos_resistance` | +10% to Maximum Chaos Resistance | +{X}% to Max Chaos Resistance |
| `ADD_MAX_HEALTH_REGENERATION` | `add_max_health_regeneration` | Add Max Health Regeneration | add Max Health Regeneration |
| `ADD_MAX_SPIRIT` | `add_max_spirit` | Add Max Spirit | add Max Spirit |
| `ADD_MAX_TOTEM` | `add_max_totem` | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve 75 Spirit each | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each |
| `ADD_METRES_MELEE_STRIKE_RANGE_NO_CONDITION` | `add_metres_melee_strike_range_no_condition` | Add Metres Melee Strike Range No Condition | add Metres Melee Strike Range No Condition |
| `ADD_NUMBER_SUMMONED_TOTEMS` | `add_number_summoned_totems` | +1 to maximum number of Summoned Totems | +{X} to Max number of Summoned Totems |
| `ADD_PHYSICAL_DAMAGE` | `add_physical_damage` | Add Physical Damage | add Physical Damage |
| `ADD_PHYSICAL_DAMAGE_REDUCTION` | `add_physical_damage_reduction` | Add Physical Damage Reduction | add Physical Damage Reduction |
| `ADD_PROJECTILE_PIERCE` | `add_projectile_pierce` | Add Projectile Pierce | add Projectile Pierce |
| `ADD_PROJECTILE_RANDOM_FIRE` | `add_projectile_random_fire` | Add Projectile Random Fire | add Projectile Random Fire |
| `ADD_QUALITY_ALL` | `add_quality_all` | +2% to Quality of all Skills | +{X}% to Quality of all Skills |
| `ADD_RECENTLY_CONSUMED_DAMAGE` | `add_recently_consumed_damage` | Add Recently Consumed Damage | add Recently Consumed Damage |
| `ADD_RING_SLOT` | `add_ring_slot` | +1 Ring Slot | +{X} Ring Slot |
| `ADD_SKILL_LEVEL` | `add_skill_level` | +2 to Level of all Skills with an Intelligence requirement | +{X} to Level of all Skills with an Intelligence requirement |
| `ADD_SKILL_SLOT` | `add_skill_slot` | Add Skill Slot | add Skill Slot |
| `ADD_SPIRIT` | `add_spirit` | +10 to Spirit | +{X} to Spirit |
| `ADD_SPIRIT_EMPTY_CHARM_SLOTS` | `add_spirit_empty_charm_slots` | +40 to Spirit for each of your empty Charm slots | +{X} to Spirit for each of your empty Charm slots |
| `ADD_SPIRIT_EVASION_RATING_BODY` | `add_spirit_evasion_rating_body` | +1 to Spirit for every 20 Evasion Rating on Equipped Body Armour | +{X} to Spirit for every {X} Evasion on Equipped Body Armor |
| `ADD_SPIRIT_EVASION_RATING_BODY_BODY_ARMOUR` | `add_spirit_evasion_rating_body_body_armour` | +1 to Spirit for every 20 Evasion Rating on Equipped Body Armour | +{X} to Spirit for every {X} Evasion on Equipped Body Armor |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD` | `add_spirit_item_energy_shield` | +1 to Spirit for every 8 Item Energy Shield on Equipped Body Armour | +{X} to Spirit for every {X} Item Energy Shield on Equipped Body Armor |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD_BODY_ARMOUR` | `add_spirit_item_energy_shield_body_armour` | +1 to Spirit for every 8 Item Energy Shield on Equipped Body Armour | +{X} to Spirit for every {X} Item Energy Shield on Equipped Body Armor |
| `ADD_SPIRIT_LIFE` | `add_spirit_life` | +1 to Maximum Spirit per 25 Maximum Life | +{X} to Max Spirit per {X} Max Health |
| `ADD_STUN_THRESHOLD` | `add_stun_threshold` | Add Stun Threshold | add Stun Threshold |
| `ADRENALINE_ATTACK_SPEED` | `adrenaline_attack_speed` | Adrenaline Attack Speed | Adrenaline Attack Speed |
| `ADRENALINE_CAST_SPEED` | `adrenaline_cast_speed` | Adrenaline Cast Speed | Adrenaline Cast Speed |
| `ADRENALINE_DAMAGE` | `adrenaline_damage` | Adrenaline Damage | Adrenaline Damage |
| `ADRENALINE_MOVEMENT_SPEED` | `adrenaline_movement_speed` | Adrenaline Movement Speed | Adrenaline Movement Speed |
| `ADRENALINE_TAKEN_PHYSICAL` | `adrenaline_taken_physical` | Adrenaline Taken Physical | Adrenaline Taken Physical |
| `AGGRAVATE_BLEEDING_ON_HIT` | `aggravate_bleeding_on_hit` | Bleeding you inflict is Aggravated Base Bleeding Duration is 1 second 50% more BuffMagnitude of Bleeding you inflict | Bleeding you inflict is Aggravated Base Bleeding Duration is {X} second {X}% more Buff Magnitude of Bleeding you inflict |
| `AILMENT_MAGNITUDE_FREEZE` | `ailment_magnitude_freeze` | Ailment Magnitude Freeze | Ailment Magnitude Freeze |
| `ALL_RAGE_SHAPESHIFTING_HUMAN` | `stat_all_rage_shapeshifting_human` | Stat All Rage Shapeshifting Human | Stat All Rage Shapeshifting Human |
| `ARCANE_SURGE_CAST_SPEED` | `arcane_surge_cast_speed` | Arcane Surge Cast Speed | Arcane Surge Cast Speed |
| `ARCANE_SURGE_REGEN` | `arcane_surge_regen` | Arcane Surge Regen | Arcane Surge Regen |
| `ARCHON_FREEZE_BUILDUP` | `archon_freeze_buildup` | Archon Freeze Buildup | Archon Freeze Buildup |
| `ARCHON_IGNITE_BUILDUP` | `archon_ignite_buildup` | Archon Ignite Buildup | Archon Ignite Buildup |
| `ARCHON_NULLIFY_CHAOS` | `archon_nullify_chaos` | Archon Nullify Chaos | Archon Nullify Chaos |
| `ARCHON_NULLIFY_PHYSICAL` | `archon_nullify_physical` | Archon Nullify Physical | Archon Nullify Physical |
| `ARCHON_SHOCK_MAGNITUDE` | `archon_shock_magnitude` | Archon Shock Magnitude | Archon Shock Magnitude |
| `ARMOR_BREAK_FULLY` | `armor_break_fully` | Armor Break Fully | Armor Break Fully |
| `ARMOR_REDUCTION` | `armor_reduction` | Armor Reduction | Armor Reduction |
| `ARMOUR_APPLIES_TO_CHAOS` | `armour_applies_to_chaos` | Body Armour grants +100% of Armour also applies to Chaos Damage | Body Armor grants +{X}% of Armor also applies to Chaos Damage |
| `ATTRIBUTE_REQUIREMENTS` | `attribute_requirements` | You can wield Two-Handed Axes, Maces and Swords in one hand Triple Attributes requirements of Martial Weapons Inherent Life granted by Strength is halved | You can wield Two-Handed Axes, Maces and Swords in one hand Triple Attributes requirements of Martial Weapons Inherent Health granted by Strength is halved |
| `AVATAR_OF_FIRE_CHAOS_TO_FIRE` | `avatar_of_fire_chaos_to_fire` | 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage | {X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage |
| `AVATAR_OF_FIRE_COLD_TO_FIRE` | `avatar_of_fire_cold_to_fire` | 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage | {X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage |
| `AVATAR_OF_FIRE_LIGHT_TO_FIRE` | `avatar_of_fire_light_to_fire` | 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage | {X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage |
| `AVATAR_OF_FIRE_PHYS_TO_FIRE` | `avatar_of_fire_phys_to_fire` | 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage | {X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage |
| `BANNER_ATTACK_SPEED` | `banner_attack_speed` | Banner Attack Speed | Banner Attack Speed |
| `BANNER_CAST_SPEED` | `banner_cast_speed` | Banner Cast Speed | Banner Cast Speed |
| `BANNER_MOVEMENT_SPEED` | `banner_movement_speed` | Banner Movement Speed | Banner Movement Speed |
| `BLACKFLAME_COVENANT_FIRE_TO_CHAOS` | `blackflame_covenant_fire_to_chaos` | Fire Spells Conversion 100% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite BuffMagnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage | Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage |
| `BLOOD_MAGIC_ENERGY_TO_LIFE` | `blood_magic_energy_to_life` | You have no Mana Skill Mana Costs StatConversion to Life Costs | You have no Mana Skill Mana Costs Stat Conversion to Health Costs |
| `BLOOD_MAGIC_MANA_TO_LIFE` | `blood_magic_mana_to_life` | You have no Mana Skill Mana Costs StatConversion to Life Costs | You have no Mana Skill Mana Costs Stat Conversion to Health Costs |
| `BODY_ARMOUR_REGENERATE_LIFE` | `stat_body_armour_regenerate_life` | Body Armour grants regenerate 3% of maximum Life per second | Body Armor grants regenerate {X}% of Max Health per second |
| `BODY_ARMOUR_UNAFFECTED_DAMAGING` | `stat_body_armour_unaffected_damaging` | Body Armour grants Unaffected by Damaging Ailments | Body Armor grants Unaffected by Damaging Ailments |
| `CHANCE_AGGRAVATE_ON_HIT_VS_CURSED` | `chance_aggravate_on_hit_vs_cursed` | Chance Aggravate On Hit Vs Cursed | Chance Aggravate On Hit Vs Cursed |
| `CHANCE_ALL_DAMAGE_CONTRIBUTES_SHOCK` | `chance_all_damage_contributes_shock` | DamageTypes from HitDamage Contributes to Shock Chance | Damage Types from Hit Damage Contributes to Shock Chance |
| `CHANCE_ATTACKS_EXCEED_ADDITIONAL_CRITICAL` | `chance_attacks_exceed_additional_critical` | Chance to HitDamage with Attacks can ExceedChance Gain additional Critical Hit Chance equal to 25% of excess chance to HitDamage with Attacks | Chance to Hit Damage with Attacks can Exceed Chance gain additional Critical Hit Chance equal to {X}% of excess chance to Hit Damage with Attacks |
| `CHANCE_AVOID_AILMENT` | `chance_avoid_ailment` | Chance Avoid Ailment | Chance Avoid Ailment |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE` | `chance_base_block_shield_value` | Gain 35% Base Chance to Block from Equipped Shield instead of the Shield's value | gain {X}% Base Chance to Block from Equipped Shield instead of the Shield's value |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE_EQUIPPED` | `chance_base_block_shield_value_equipped` | Gain 35% Base Chance to Block from Equipped Shield instead of the Shield's value | gain {X}% Base Chance to Block from Equipped Shield instead of the Shield's value |
| `CHANCE_BASE_CRITICAL_SPELLS_IS` | `chance_base_critical_spells_is` | Base Critical Hit Chance for Spells is 15% | Base Critical Hit Chance for Spells is {X}% |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED` | `chance_benefits_frenzy_charges_doubled` | Benefits from consuming Frenzy Charges for your Skills have 50% chance to be doubled | Benefits from consuming Frenzy Charges for your Skills have {X}% chance to be doubled |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED_CHARGE` | `chance_benefits_frenzy_charges_doubled_charge` | Benefits from consuming Frenzy Charges for your Skills have 50% chance to be doubled | Benefits from consuming Frenzy Charges for your Skills have {X}% chance to be doubled |
| `CHANCE_BLOCK_IS` | `chance_block_is` | Maximum Block chance is 75% | Max Block chance is {X}% |
| `CHANCE_CASCADABLE_SPELLS_ECHO` | `chance_cascadable_spells_echo` | Cascadable Spells have 20% chance to Cascadable | Cascadable Spells have {X}% chance to Cascadable |
| `CHANCE_CHILL` | `chance_chill` | Chance Chill | Chance Chill |
| `CHANCE_CREATE_SHOCKED_GROUND_ON_SHOCK` | `chance_create_shocked_ground_on_shock` | 25% chance on Shocking Enemies to created Shocked Ground | {X}% chance on Shocking Enemies to created Shocked Ground |
| `CHANCE_CRITICAL_IS_UNLUCKY` | `chance_critical_is_unlucky` | Enemy Critical Hit Chance against you is Unlucky | Enemy Critical Hit Chance against you is Unlucky |
| `CHANCE_CURSED_KILLED_OR_ALLIES` | `chance_cursed_killed_or_allies` | Cursed Enemies killed by you, or by Allies in your Presence, have a 33% chance to explode, dealing a quarter of their maximum Life as Chaos damage | Cursed Enemies killed by you, or by Allies in your Presence, have a {X}% chance to explode, dealing a quarter of their Max Health as Chaos damage |
| `CHANCE_CURSED_KILLED_OR_ALLIES_CURSE` | `chance_cursed_killed_or_allies_curse` | Cursed Enemies killed by you, or by Allies in your Presence, have a 33% chance to explode, dealing a quarter of their maximum Life as Chaos damage | Cursed Enemies killed by you, or by Allies in your Presence, have a {X}% chance to explode, dealing a quarter of their Max Health as Chaos damage |
| `CHANCE_DODGE` | `chance_dodge` | Chance Dodge | Chance Dodge |
| `CHANCE_EVADE_IS` | `chance_evade_is` | Maximum Chance to Evasion is 50% | Max Chance to Evasion is {X}% |
| `CHANCE_EVADE_IS_UNLUCKY_DEFLECT` | `chance_evade_is_unlucky_deflect` | Chance to Evasion is Unlucky Chance to Deflect is Lucky | Chance to Evasion is Unlucky Chance to Deflect is Lucky |
| `CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY` | `chance_evade_is_unlucky_deflect_lucky` | Chance to Evasion is Unlucky Chance to Deflect is Lucky | Chance to Evasion is Unlucky Chance to Deflect is Lucky |
| `CHANCE_FREEZE` | `chance_freeze` | Chance Freeze | Chance Freeze |
| `CHANCE_IGNITE` | `chance_ignite` | Chance Ignite | Chance Ignite |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE` | `chance_kill_explode_dealing_life` | 10% chance for Enemies you Kill to Explode, dealing 100% of their maximum Life as Physical Damage Chance is doubled against Undead and Demons | {X}% chance for Enemies you Kill to Explode, dealing {X}% of their Max Health as Physical Damage Chance is doubled against Undead and Demons |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE_PHYSICAL` | `chance_kill_explode_dealing_life_physical` | 10% chance for Enemies you Kill to Explode, dealing 100% of their maximum Life as Physical Damage Chance is doubled against Undead and Demons | {X}% chance for Enemies you Kill to Explode, dealing {X}% of their Max Health as Physical Damage Chance is doubled against Undead and Demons |
| `CHANCE_REPEATABLE_SPELLS_REPEAT` | `chance_repeatable_spells_repeat` | Repeatable Spells have 20% chance to Repeat | Repeatable Spells have {X}% chance to Repeat |
| `CHANCE_SLAM_CAUSE_ADDITIONAL_AFTERSHOCK_SPELL` | `chance_slam_cause_additional_aftershock_spell` | Chance Slam Cause Additional Aftershock Spell | Chance Slam Cause Additional Aftershock Spell |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS` | `chance_surpassing_power_mountain_teachings` | 100% SurpassChance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a maximum of 30 Lose a Mountain's Teaching when you are HitDamage, or when you use or Sustained an Attack that benefits from Mountain's Teachings | {X}% Surpass Chance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a Max of {X} gain a Mountain's Teaching when you are Hit Damage, or when you use or Sustained an Attack that benefits from Mountain's Teachings |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS_PER_ENEMY` | `chance_surpassing_power_mountain_teachings_per_enemy` | 100% SurpassChance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a maximum of 30 Lose a Mountain's Teaching when you are HitDamage, or when you use or Sustained an Attack that benefits from Mountain's Teachings | {X}% Surpass Chance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a Max of {X} gain a Mountain's Teaching when you are Hit Damage, or when you use or Sustained an Attack that benefits from Mountain's Teachings |
| `CHANCE_WHICH_CREATE_FISSURES_ADDITIONAL_FISSURE` | `chance_which_create_fissures_additional_fissure` | Skills which create Fissures have a #% chance to create an additional Fissure | Skills which create Fissures have a #% chance to create an additional Fissure |
| `CHAOS_INOCULATION_IMMUNE_BLEED` | `chaos_inoculation_immune_bleed` | Maximum Life is 1 Immune to Chaos Damage and Bleeding | Max Health is {X} Immune to Chaos Damage and Bleeding |
| `CHAOS_INOCULATION_IMMUNE_CHAOS` | `chaos_inoculation_immune_chaos` | Maximum Life is 1 Immune to Chaos Damage and Bleeding | Max Health is {X} Immune to Chaos Damage and Bleeding |
| `CHAOS_INOCULATION_MAX_HEALTH` | `chaos_inoculation_max_health` | Maximum Life is 1 Immune to Chaos Damage and Bleeding | Max Health is {X} Immune to Chaos Damage and Bleeding |
| `CHARGE_CONVERT_RESONANCE` | `charge_convert_resonance` | Gain Power Charges instead of Frenzy Charges Gain Frenzy Charges instead of Endurance Charges Gain Endurance Charges instead of Power Charges | gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges |
| `CHARM_GOLDEN_RARITY` | `charm_golden_rarity` | Charm Golden Rarity | Charm Golden Rarity |
| `CHARM_IMMUNE_ANTIDOTE` | `charm_immune_antidote` | Charm Immune Antidote | Charm Immune Antidote |
| `CHARM_IMMUNE_DOUSING` | `charm_immune_dousing` | Charm Immune Dousing | Charm Immune Dousing |
| `CHARM_IMMUNE_GROUNDING` | `charm_immune_grounding` | Charm Immune Grounding | Charm Immune Grounding |
| `CHARM_IMMUNE_SILVER` | `charm_immune_silver` | Charm Immune Silver | Charm Immune Silver |
| `CHARM_IMMUNE_STAUNCHING` | `charm_immune_staunching` | Charm Immune Staunching | Charm Immune Staunching |
| `CHARM_IMMUNE_STONE` | `charm_immune_stone` | Charm Immune Stone | Charm Immune Stone |
| `CHARM_IMMUNE_THAWING` | `charm_immune_thawing` | Charm Immune Thawing | Charm Immune Thawing |
| `CHARM_RESIST_CHAOS` | `charm_resist_chaos` | Charm Resist Chaos | Charm Resist Chaos |
| `CHARM_RESIST_COLD` | `charm_resist_cold` | Charm Resist Cold | Charm Resist Cold |
| `CHARM_RESIST_FIRE` | `charm_resist_fire` | Charm Resist Fire | Charm Resist Fire |
| `CHARM_RESIST_LIGHTNING` | `charm_resist_lightning` | Charm Resist Lightning | Charm Resist Lightning |
| `COLD_TAKEN_AS_FIRE` | `cold_taken_as_fire` | 20% of Cold Damage taken as Fire Damage | {X}% of Cold Damage taken as Fire Damage |
| `CONVERT_ENERGY_SHIELD_MANA` | `stat_convert_energy_shield_mana` | StatConversion 100% of maximum Energy Shield to maximum Mana Mana Costs are Doubled | Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled |
| `CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION` | `stat_convert_energy_shield_mana_statconversion` | StatConversion 100% of maximum Energy Shield to maximum Mana Mana Costs are Doubled | Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled |
| `CONVERT_EXTRA_PHYS_TO_FIRE` | `convert_extra_phys_to_fire` | Convert Extra Phys To Fire | Convert Extra Phys To Fire |
| `CONVERT_SKILL_MANA_TO_LIFE` | `convert_skill_mana_to_life` | Convert Skill Mana To Life | Convert Skill Mana To Health |
| `CREATE_CHILLED_GROUND_ON_FREEZE` | `create_chilled_ground_on_freeze` | On Freezing Enemies create Chilled Ground | On Freezing Enemies create Chilled Ground |
| `CREATE_IGNITED_GROUND_ON_IGNITE` | `create_ignited_ground_on_ignite` | Create Ignited Ground On Ignite | Create Ignited Ground On Ignite |
| `DAMAGE_DEALT` | `damage_dealt` | Damage Dealt | Damage Dealt |
| `DAMAGE_DEALT_BEAR_FIRE` | `damage_dealt_bear_fire` | Damage Dealt Bear Fire | Damage Dealt Bear Fire |
| `DAMAGE_DEALT_BEAR_PHYSICAL` | `damage_dealt_bear_physical` | Damage Dealt Bear Physical | Damage Dealt Bear Physical |
| `DAMAGE_DEALT_ELEMENTAL_SPELL` | `damage_dealt_elemental_spell` | Damage Dealt Elemental Spell | Damage Dealt Elemental Spell |
| `DAMAGE_DEALT_WOLF_COLD` | `damage_dealt_wolf_cold` | Damage Dealt Wolf Cold | Damage Dealt Wolf Cold |
| `DAMAGE_DEALT_WOLF_PHYSICAL` | `damage_dealt_wolf_physical` | Damage Dealt Wolf Physical | Damage Dealt Wolf Physical |
| `DAMAGE_DEALT_WYVERN_CHAOS` | `damage_dealt_wyvern_chaos` | Damage Dealt Wyvern Chaos | Damage Dealt Wyvern Chaos |
| `DAMAGE_DEALT_WYVERN_LIGHTNING` | `damage_dealt_wyvern_lightning` | Damage Dealt Wyvern Lightning | Damage Dealt Wyvern Lightning |
| `DAMAGE_TAKEN` | `damage_taken` | Damage Taken | Damage Taken |
| `DAMAGE_TAKEN_CHAOS` | `damage_taken_chaos` | Damage Taken Chaos | Damage Taken Chaos |
| `DAMAGE_TAKEN_COLD_ON_IGNITED` | `damage_taken_cold_on_ignited` | Damage Taken Cold On Ignited | Damage Taken Cold On Ignited |
| `DAMAGE_TAKEN_FIRE_ON_CHILLED` | `damage_taken_fire_on_chilled` | Damage Taken Fire On Chilled | Damage Taken Fire On Chilled |
| `DAMAGE_TAKEN_PHYSICAL` | `damage_taken_physical` | Damage Taken Physical | Damage Taken Physical |
| `DAMAGE_UNLUCKY` | `damage_unlucky` | Damage of Enemies HitDamage you is Unlucky | Damage of Enemies Hit Damage you is Unlucky |
| `ELDRITCH_BATTERY_ES_TO_MANA` | `eldritch_battery_es_to_mana` | StatConversion 100% of maximum Energy Shield to maximum Mana Mana Costs are Doubled | Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled |
| `EVASION` | `evasion` | Evasion | Evasion |
| `EVASION_RATING_ELEMENTAL_DAMAGE` | `stat_evasion_rating_elemental_damage` | 50% of Evasion Rating also grants Elemental Damage reduction | {X}% of Evasion also grants Elemental Damage reduction |
| `EXPOSURE_COLD` | `exposure_cold` | Exposure Cold | Exposure Cold |
| `EXPOSURE_FIRE` | `exposure_fire` | Exposure Fire | Exposure Fire |
| `EXPOSURE_LIGHTNING` | `exposure_lightning` | Exposure Lightning | Exposure Lightning |
| `EXTRA_FIRE_FROM_CHAOS` | `extra_fire_from_chaos` | Extra Fire From Chaos | Extra Fire From Chaos |
| `EXTRA_FIRE_FROM_COLD` | `extra_fire_from_cold` | Gain 25% of Cold Damage as Extra Fire Damage against Frozen Enemies | gain {X}% of Cold Damage as Extra Fire Damage against Frozen Enemies |
| `EXTRA_LIGHTNING_FROM_CHAOS` | `extra_lightning_from_chaos` | Extra Lightning From Chaos | Extra Lightning From Chaos |
| `EXTRA_PHYSICAL_FROM_CHAOS` | `extra_physical_from_chaos` | Extra Physical From Chaos | Extra Physical From Chaos |
| `EXTRA_PHYSICAL_FROM_COLD` | `extra_physical_from_cold` | Extra Physical From Cold | Extra Physical From Cold |
| `EXTRA_PHYSICAL_FROM_FIRE` | `extra_physical_from_fire` | Extra Physical From Fire | Extra Physical From Fire |
| `EXTRA_PHYSICAL_FROM_LIGHTNING` | `extra_physical_from_lightning` | Extra Physical From Lightning | Extra Physical From Lightning |
| `EXTRA_PHYSICAL_FROM_PHYSICAL` | `extra_physical_from_physical` | Extra Physical From Physical | Extra Physical From Physical |
| `EXTRA_RANDOM_ELEMENT_CYCLE` | `extra_random_element_cycle` | Extra Random Element Cycle | Extra Random Element Cycle |
| `EXTRA_RANDOM_ELEMENT_RANDOM` | `extra_random_element_random` | Gain 10% of Damage as Extra Damage of a random ElementalDamage | gain {X}% of Damage as Extra Damage of a random Elemental Damage |
| `FIRE_SPELLS_CONVERT_DAMAGE` | `stat_fire_spells_convert_damage` | Fire Spells Conversion 100% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite BuffMagnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage | Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage |
| `FIRE_SPELLS_CONVERT_DAMAGE_SPELL` | `stat_fire_spells_convert_damage_spell` | Fire Spells Conversion 100% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite BuffMagnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage | Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage |
| `FLASKS_RECOVER_LIFE` | `stat_flasks_recover_life` | Flasks do not recover Life | Flasks do not recover Health |
| `FORM_BEAR_ARMOR` | `form_bear_armor` | Form Bear Armor | Form Bear Armor |
| `FORM_BEAR_ATTACK_SPEED` | `form_bear_attack_speed` | Form Bear Attack Speed | Form Bear Attack Speed |
| `FORM_WOLF_ATTACK_SPEED` | `form_wolf_attack_speed` | Form Wolf Attack Speed | Form Wolf Attack Speed |
| `FORM_WOLF_MOVEMENT_SPEED` | `form_wolf_movement_speed` | Form Wolf Movement Speed | Form Wolf Movement Speed |
| `FORM_WYVERN_CRIT_CHANCE` | `form_wyvern_crit_chance` | Form Wyvern Crit Chance | Form Wyvern Crit Chance |
| `FORM_WYVERN_CRIT_DAMAGE` | `form_wyvern_crit_damage` | Form Wyvern Crit Damage | Form Wyvern Crit Damage |
| `GEM_QUALITY_SOCKETED_ADDITIONAL` | `stat_gem_quality_socketed_additional` | Gem Quality grants Socketed Skills an additional effect | Gem Quality grants Socketed Skills an additional effect |
| `GLANCING_BLOWS_DEFLECT_LUCKY` | `glancing_blows_deflect_lucky` | Chance to Evasion is Unlucky Chance to Deflect is Lucky | Chance to Evasion is Unlucky Chance to Deflect is Lucky |
| `GLANCING_BLOWS_EVADE_UNLUCKY` | `glancing_blows_evade_unlucky` | Chance to Evasion is Unlucky Chance to Deflect is Lucky | Chance to Evasion is Unlucky Chance to Deflect is Lucky |
| `GRANT_VOLATILITY_ON_AILMENT` | `grant_volatility_on_ailment` | Grant Volatility On Ailment | Grant Volatility On Ailment |
| `GRANT_VOLATILITY_ON_EXPLODE` | `grant_volatility_on_explode` | Grant Volatility On Explode | Grant Volatility On Explode |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE` | `inc_accuracy_penalty_based_distance` | Enemies have an Accuracy Penalty against you based on Distance | Enemies have an Accuracy Penalty against you based on Distance |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE_BY_DISTANCE` | `inc_accuracy_penalty_based_distance_by_distance` | Enemies have an Accuracy Penalty against you based on Distance | Enemies have an Accuracy Penalty against you based on Distance |
| `INC_ACCURACY_RATING_IS_DOUBLED` | `inc_accuracy_rating_is_doubled` | Accuracy Rating is Doubled Never deal Critical Hits | Accuracy is Doubled Never deal Critical Hits |
| `INC_ACCURACY_RATING_IS_DOUBLED_CRIT` | `inc_accuracy_rating_is_doubled_crit` | Accuracy Rating is Doubled Never deal Critical Hits | Accuracy is Doubled Never deal Critical Hits |
| `INC_ADAPTATIONS_DURATION` | `inc_adaptations_duration` | Inc Adaptations Duration | Inc Adaptations Duration |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE` | `inc_adapt_highest_elemental_damage` | Adaptation to the highest Elemental Damage Type of each HitDamage you take | Adaptation to the highest Elemental Damage Type of each Hit Damage you take |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE_ADAPTATION` | `inc_adapt_highest_elemental_damage_adaptation` | Adaptation to the highest Elemental Damage Type of each HitDamage you take | Adaptation to the highest Elemental Damage Type of each Hit Damage you take |
| `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION` | `inc_additional_curse_double_activation` | You can apply an additional Curse Double Activation Delay of Curses | You can apply an additional Curse Double Activation Delay of Curses |
| `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE` | `inc_additional_curse_double_activation_curse` | You can apply an additional Curse Double Activation Delay of Curses | You can apply an additional Curse Double Activation Delay of Curses |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM` | `inc_additional_life_equal_item` | Gain additional maximum Life equal to 100% of the Item Energy Shield on Equipped Body Armour | gain additional Max Health equal to {X}% of the Item Energy Shield on Equipped Body Armor |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM_BODY_ARMOUR` | `inc_additional_life_equal_item_body_armour` | Gain additional maximum Life equal to 100% of the Item Energy Shield on Equipped Body Armour | gain additional Max Health equal to {X}% of the Item Energy Shield on Equipped Body Armor |
| `INC_AILMENT_DAMAGE_RATE` | `inc_ailment_damage_rate` | Inc Ailment Damage Rate | Inc Ailment Damage Rate |
| `INC_AILMENT_DAMAGE_RATE_BLEED` | `inc_ailment_damage_rate_bleed` | Inc Ailment Damage Rate Bleed | Inc Ailment Damage Rate Bleed |
| `INC_AILMENT_DAMAGE_RATE_POISON` | `inc_ailment_damage_rate_poison` | Inc Ailment Damage Rate Poison | Inc Ailment Damage Rate Poison |
| `INC_ALLIES_IN_PRESENCE_ADDED` | `inc_allies_in_presence_added` | Allies in your Presence gain added Attack Damage equal to 25% of your main hand Weapon's damage | Allies in your Presence gain add Attack Damage equal to {X}% of your main hand Weapon's damage |
| `INC_ALLIES_IN_PRESENCE_ADDED_ATTACK` | `inc_allies_in_presence_added_attack` | Allies in your Presence gain added Attack Damage equal to 25% of your main hand Weapon's damage | Allies in your Presence gain add Attack Damage equal to {X}% of your main hand Weapon's damage |
| `INC_ALLIES_IN_PRESENCE_REGENERATE_IN_PRESENCE` | `inc_allies_in_presence_regenerate_in_presence` | Inc Allies In Presence Regenerate In Presence | Inc Allies In Presence Regenerate In Presence |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING` | `inc_allocate_passive_sorceress_starting` | Can Allocate Passive Skills from the Sorceress's starting point | Can Allocate Passive Skills from the Sorceress's starting point |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING_SORCERESS` | `inc_allocate_passive_sorceress_starting_sorceress` | Can Allocate Passive Skills from the Sorceress's starting point | Can Allocate Passive Skills from the Sorceress's starting point |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING` | `inc_allocate_passive_warrior_starting` | Can Allocate Passive Skills from the Warrior's starting point | Can Allocate Passive Skills from the Warrior's starting point |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING_WARRIOR` | `inc_allocate_passive_warrior_starting_warrior` | Can Allocate Passive Skills from the Warrior's starting point | Can Allocate Passive Skills from the Warrior's starting point |
| `INC_ALL_BONUSES_AMULET_MINIONS` | `inc_all_bonuses_amulet_minions` | Inc All Bonuses Amulet Minions | Inc All Bonuses Amulet Minions |
| `INC_ALL_DAMAGE_ALLIES_IN` | `inc_all_damage_allies_in` | Inc All Damage Allies In | Inc All Damage Allies In |
| `INC_ALL_DAMAGE_CONTRIBUTES_CHILL` | `inc_all_damage_contributes_chill` | Inc All Damage Contributes Chill | Inc All Damage Contributes Chill |
| `INC_ALL_FLAMES_CHAYULA_MANIFEST` | `inc_all_flames_chayula_manifest` | Inc All Flames Chayula Manifest | Inc All Flames Chayula Manifest |
| `INC_ARCANE_SURGE` | `inc_arcane_surge` | You have Arcane Surge | You have Arcane Surge |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN` | `inc_arcane_surge_shapeshift_human` | Gain ArcaneSurgeDuration when you Shapeshift to Human form after being Shapeshifted for at least 8 seconds | gain Arcane Surge Duration when you Shapeshift to Human form after being Shapeshifted for at least {X} seconds |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN_ARCANESURGEDURATION` | `inc_arcane_surge_shapeshift_human_arcanesurgeduration` | Gain ArcaneSurgeDuration when you Shapeshift to Human form after being Shapeshifted for at least 8 seconds | gain Arcane Surge Duration when you Shapeshift to Human form after being Shapeshifted for at least {X} seconds |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL` | `inc_attacks_weapons_added_physical` | Attacks using your Weapons have Added Physical Damage equal to 25% of the Accuracy Rating on the Weapon | Attacks using your Weapons have add Physical Damage equal to {X}% of the Accuracy on the Weapon |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL_ATTACK` | `inc_attacks_weapons_added_physical_attack` | Attacks using your Weapons have Added Physical Damage equal to 25% of the Accuracy Rating on the Weapon | Attacks using your Weapons have add Physical Damage equal to {X}% of the Accuracy on the Weapon |
| `INC_BANNERS_GLORY` | `inc_banners_glory` | Banners gain 5 Glory per second | Banners gain {X} Glory per second |
| `INC_BANNER_DAMAGE` | `inc_banner_damage` | Inc Banner Damage | Inc Banner Damage |
| `INC_BASE_ENERGY_SHIELD_RECHARGE` | `inc_base_energy_shield_recharge` | -1 second to base Energy Shield Recharge delay | -{X} second to base Energy Shield Recharge delay |
| `INC_BASE_LIFE_COST_EQUAL` | `inc_base_life_cost_equal` | Skills gain a Base Life Cost equal to Base Mana Cost | Skills gain a Base Health Cost equal to Base Mana Cost |
| `INC_BEAR_SPIRIT_GAINS_EMBRACE` | `inc_bear_spirit_gains_embrace` | Bear Spirit gains SpiritWalkerBearAura | Bear Spirit gain Spirit Walker Bear Aura |
| `INC_BECOME_IGNITED_CRITICAL_TAKING` | `inc_become_ignited_critical_taking` | Become Ignited when you deal a Critical Hit, taking 15% of your maximum Life and Energy Shield as Fire Damage per second | Become Ignited when you deal a Critical Hit, taking {X}% of your Max Health and Energy Shield as Fire Damage per second |
| `INC_BECOME_IGNITED_CRITICAL_TAKING_PER_SECOND` | `inc_become_ignited_critical_taking_per_second` | Become Ignited when you deal a Critical Hit, taking 15% of your maximum Life and Energy Shield as Fire Damage per second | Become Ignited when you deal a Critical Hit, taking {X}% of your Max Health and Energy Shield as Fire Damage per second |
| `INC_BEING_DAMAGED_TAKE_DAMAGE` | `inc_being_damaged_take_damage` | 4 seconds after being Damaged by an Enemy HitDamage, take Damage equal to 30% of that HitDamage's Damage | {X} seconds after being Damaged by an Enemy Hit Damage, take Damage equal to {X}% of that Hit Damage's Damage |
| `INC_BEING_DAMAGED_TAKE_DAMAGE_HITDAMAGE` | `inc_being_damaged_take_damage_hitdamage` | 4 seconds after being Damaged by an Enemy HitDamage, take Damage equal to 30% of that HitDamage's Damage | {X} seconds after being Damaged by an Enemy Hit Damage, take Damage equal to {X}% of that Hit Damage's Damage |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES` | `inc_benefits_bonded_modifiers_runes` | Gain the benefits of ShamanOnlyMods modifiers on Runes and Idols | gain the benefits of Shaman Only Mods modifiers on Runes and Idols |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES_SHAMANONLYMODS` | `inc_benefits_bonded_modifiers_runes_shamanonlymods` | Gain the benefits of ShamanOnlyMods modifiers on Runes and Idols | gain the benefits of Shaman Only Mods modifiers on Runes and Idols |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS` | `inc_bleeding_inflict_cursed_targets` | Bleeding you inflict on Cursed targets is Aggravated | Bleeding you inflict on Cursed targets is Aggravated |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS_CURSE` | `inc_bleeding_inflict_cursed_targets_curse` | Bleeding you inflict on Cursed targets is Aggravated | Bleeding you inflict on Cursed targets is Aggravated |
| `INC_BONUSES_FOCUS` | `inc_bonuses_focus` | 50% reduced bonuses gained from Equipped Focus | {X}% increased bonuses gain from Equipped Focus |
| `INC_BOW_ATTACK_SPEED` | `inc_bow_attack_speed` | Inc Bow Attack Speed | Inc Bow Attack Speed |
| `INC_BOW_DAMAGE` | `inc_bow_damage` | Inc Bow Damage | Inc Bow Damage |
| `INC_BOW_PHYSICAL_DAMAGE` | `inc_bow_physical_damage` | Inc Bow Physical Damage | Inc Bow Physical Damage |
| `INC_BREAK_ARMOUR_BELOW` | `inc_break_armour_below` | You can Break Enemy Armour to below 0 | You can Break Enemy Armor to below {X} |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE` | `inc_break_armour_equal_damage` | Break Armour equal to 10% of Hit Damage dealt | Break Armor equal to {X}% of Hit Damage dealt |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE_ARMOURBREAK` | `inc_break_armour_equal_damage_armourbreak` | Break Armour equal to 10% of Hit Damage dealt | Break Armor equal to {X}% of Hit Damage dealt |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE` | `inc_break_concentration_equal_damage` | Break enemy Concentration on HitDamage equal to 100% of Damage Dealt | Break enemy Concentration on Hit Damage equal to {X}% of Damage Dealt |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE_HITDAMAGE` | `inc_break_concentration_equal_damage_hitdamage` | Break enemy Concentration on HitDamage equal to 100% of Damage Dealt | Break enemy Concentration on Hit Damage equal to {X}% of Damage Dealt |
| `INC_BUFF_EFFECT_TAKEN` | `inc_buff_effect_taken` | Inc Buff Effect Taken | Inc Buff Effect Taken |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS` | `inc_build_retain_combo_regardless` | Skills can build and retain Combo regardless of Weapon Set | Skills can build and retain Combo regardless of Weapon Set |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS_WEAPONSETS` | `inc_build_retain_combo_regardless_weaponsets` | Skills can build and retain Combo regardless of Weapon Set | Skills can build and retain Combo regardless of Weapon Set |
| `INC_CANNOT_SPIRIT` | `inc_cannot_spirit` | Cannot gain Spirit from Equipment | Cannot gain Spirit from Equipment |
| `INC_CARRY_CHEST_WHICH_ADDS` | `inc_carry_chest_which_adds` | Carry a Chest which adds 20 Inventory Slots | Carry a Chest which add {X} Inventory Slots |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER` | `inc_central_projectile_owl_feather` | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER_EMPOWERED` | `inc_central_projectile_owl_feather_empowered` | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground |
| `INC_CHAOS_RESISTANCE_IS_DOUBLED` | `inc_chaos_resistance_is_doubled` | Chaos Resistance is doubled | Chaos Resistance is doubled |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE` | `inc_charge_trigger_elemental_surge` | When you Consume a Charges, Trigger Elemental Surge to gain 3 Lightning Surges | When you Consume a Charges, Trigger Elemental Surge to gain {X} Lightning Surges |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_CHARGES` | `inc_charge_trigger_elemental_surge_charges` | When you Consume a Charges, Trigger Elemental Surge to gain 2 Cold Surges | When you Consume a Charges, Trigger Elemental Surge to gain {X} Cold Surges |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_SURGE` | `inc_charge_trigger_elemental_surge_surge` | When you Consume a Charges, Trigger Elemental Surge to gain 2 Fire Surges | When you Consume a Charges, Trigger Elemental Surge to gain {X} Fire Surges |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_V2` | `inc_charge_trigger_elemental_surge_v2` | When you Consume a Charges, Trigger Elemental Surge to gain 3 Lightning Surges | When you Consume a Charges, Trigger Elemental Surge to gain {X} Lightning Surges |
| `INC_CHILLS_SLOW_TARGETS_UP` | `inc_chills_slow_targets_up` | Your Chills can Slow targets by up to a maximum of 35% | Your Chills can Slow targets by up to a Max of {X}% |
| `INC_CHILL_DURATION_TAKEN` | `inc_chill_duration_taken` | Inc Chill Duration Taken | Inc Chill Duration Taken |
| `INC_COLD_DAMAGE_ON_CHILLED` | `inc_cold_damage_on_chilled` | Inc Cold Damage On Chilled | Inc Cold Damage On Chilled |
| `INC_COLD_DAMAGE_SPELL` | `inc_cold_damage_spell` | Inc Cold Damage Spell | Inc Cold Damage Spell |
| `INC_COLD_DAMAGE_TAKEN_FIRE` | `inc_cold_damage_taken_fire` | 20% of Cold Damage taken as Fire Damage | {X}% of Cold Damage taken as Fire Damage |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER` | `inc_collecting_elemental_infusion_another` | When collecting an Elemental Infusion, gain another different Elemental Infusion | When collecting an Elemental Infusion, gain another different Elemental Infusion |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER_ELEMENTALINFUSION` | `inc_collecting_elemental_infusion_another_elementalinfusion` | When collecting an Elemental Infusion, gain another different Elemental Infusion | When collecting an Elemental Infusion, gain another different Elemental Infusion |
| `INC_COMBO_ADDITIONAL` | `inc_combo_additional` | When you gain Combo, gain an additional Combo | When you gain Combo, gain an additional Combo |
| `INC_COMBO_ALL_ATTACK` | `inc_combo_all_attack` | Gain Combo from all Attack HitDamage | gain Combo from all Attack Hit Damage |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE` | `inc_companions_added_attack_damage` | Companions gain added Attack damage equal to 60% of your main hand Weapon's damage | Companions gain add Attack damage equal to {X}% of your main hand Weapon's damage |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE_COMPANION` | `inc_companions_added_attack_damage_companion` | Companions gain added Attack damage equal to 60% of your main hand Weapon's damage | Companions gain add Attack damage equal to {X}% of your main hand Weapon's damage |
| `INC_CONCENTRATION_EQUAL_LIFE` | `inc_concentration_equal_life` | Enemies have Maximum Concentration equal to 30% of their Maximum Life | Enemies have Max Concentration equal to {X}% of their Max Health |
| `INC_CONCENTRATION_EQUAL_LIFE_MAX_LIFE` | `inc_concentration_equal_life_max_life` | Enemies have Maximum Concentration equal to 30% of their Maximum Life | Enemies have Max Concentration equal to {X}% of their Max Health |
| `INC_CREATE_LIGHTNING_INFUSION_REMNANTS` | `inc_create_lightning_infusion_remnants` | Create Lightning ElementalInfusion Remnants instead of Fire Create Cold ElementalInfusion Remnants instead of Lightning Create Fire ElementalInfusion Remnants instead of Cold | Create Lightning Elemental Infusion Remnants instead of Fire Create Cold Elemental Infusion Remnants instead of Lightning Create Fire Elemental Infusion Remnants instead of Cold |
| `INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION` | `inc_create_lightning_infusion_remnants_elementalinfusion` | Create Lightning ElementalInfusion Remnants instead of Fire Create Cold ElementalInfusion Remnants instead of Lightning Create Fire ElementalInfusion Remnants instead of Cold | Create Lightning Elemental Infusion Remnants instead of Fire Create Cold Elemental Infusion Remnants instead of Lightning Create Fire Elemental Infusion Remnants instead of Cold |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE` | `inc_critical_ignore_non_negative` | Critical Hits ignore non-negative Enemy Monster Elemental Resistances | Critical Hits ignore non-negative Enemy Monster Elemental Resistances |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE_RESISTANCES` | `inc_critical_ignore_non_negative_resistances` | Critical Hits ignore non-negative Enemy Monster Elemental Resistances | Critical Hits ignore non-negative Enemy Monster Elemental Resistances |
| `INC_CRITICAL_WEAKNESS_MARK_THEM` | `inc_critical_weakness_mark_them` | Apply 10 Critical Weakness to Enemies when Consuming a Mark on them | Apply {X} Critical Weakness to Enemies when Consuming a Mark on them |
| `INC_CRITICAL_WEAKNESS_MARK_THEM_CRITICALWEAKNESS` | `inc_critical_weakness_mark_them_criticalweakness` | Apply 10 Critical Weakness to Enemies when Consuming a Mark on them | Apply {X} Critical Weakness to Enemies when Consuming a Mark on them |
| `INC_CRIT_CHANCE_SPELL` | `inc_crit_chance_spell` | Inc Crit Chance Spell | Inc Crit Chance Spell |
| `INC_CRIT_DAMAGE_SPELL` | `inc_crit_damage_spell` | Inc Crit Damage Spell | Inc Crit Damage Spell |
| `INC_CRUSHING_BLOWS` | `inc_crushing_blows` | Your HitDamage are Crushing Blows | Your Hit Damage are Crushing Blows |
| `INC_CULLING_STRIKE` | `inc_culling_strike` | Culling Strike | Culling Strike |
| `INC_CURRENT_ENERGY_SHIELD_IS` | `inc_current_energy_shield_is` | 60% of your current Energy Shield is added to your Armour for determining your Physical Damage Reduction from Armour | {X}% of your current Energy Shield is add to your Armor for determining your Physical Damage Reduction from Armor |
| `INC_CURRENT_ENERGY_SHIELD_IS_ENERGYSHIELD` | `inc_current_energy_shield_is_energyshield` | 60% of your current Energy Shield is added to your Armour for determining your Physical Damage Reduction from Armour | {X}% of your current Energy Shield is add to your Armor for determining your Physical Damage Reduction from Armor |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE` | `inc_current_energy_shield_recharge` | -0.2 seconds to current Energy Shield Recharge delay per Combo expended when using Skills | -{X} seconds to current Energy Shield Recharge delay per Combo expended when using Skills |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE_ESRECHARGE` | `inc_current_energy_shield_recharge_esrecharge` | -0.2 seconds to current Energy Shield Recharge delay per Combo expended when using Skills | -{X} seconds to current Energy Shield Recharge delay per Combo expended when using Skills |
| `INC_CURSES_INFLICT_INFINITE_DURATION` | `inc_curses_inflict_infinite_duration` | Curses you inflict have infinite Duration | Curses you inflict have infinite Duration |
| `INC_DAMAGE_ALLIES_IN_PRESENCE` | `inc_damage_allies_in_presence` | All Damage from you and Allies in your Presence contributes to Flammability and Ignite BuffMagnitude | All Damage from you and Allies in your Presence contributes to Flammability and Ignite Buff Magnitude |
| `INC_DAMAGE_BLOCKING` | `inc_damage_blocking` | Inc Damage Blocking | Inc Damage Blocking |
| `INC_DAMAGE_CONTRIBUTES_CHILL_MAGNITUDE` | `inc_damage_contributes_chill_magnitude` | DamageTypes from HitDamage Contributes to Chill Magnitude | Damage Types from Hit Damage Contributes to Chill Magnitude |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_ELEMENTALDAMAGE` | `inc_damage_extra_random_element_elementaldamage` | Gain 10% of Damage as Extra Damage of a random ElementalDamage | gain {X}% of Damage as Extra Damage of a random Elemental Damage |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_RANDOM_ELEMENT` | `inc_damage_extra_random_element_random_element` | Inc Damage Extra Random Element Random Element | Inc Damage Extra Random Element Random Element |
| `INC_DAMAGE_IS_TAKEN_MANA_HITDAMAGE` | `inc_damage_is_taken_mana_hitdamage` | HitDamage damage is taken from Mana before Life if your current Mana is higher than your current Life | Hit Damage damage is taken from Mana before Health if your current Mana is higher than your current Health |
| `INC_DAMAGE_IS_TAKEN_MANA_TAKEN_MANA` | `inc_damage_is_taken_mana_taken_mana` | Inc Damage Is Taken Mana Taken Mana | Inc Damage Is Taken Mana Taken Mana |
| `INC_DAMAGE_OVER_TIME` | `inc_damage_over_time` | Take 50% less Damage over Time if you've started taking Damage over Time in the past second Take 50% more Damage over Time if you haven't started taking Damage over Time in the past second | Take {X}% more Damage over Time if you've started taking Damage over Time in the past second Take {X}% more Damage over Time if you haven't started taking Damage over Time in the past second |
| `INC_DAMAGE_RECENTLY_KILLED` | `inc_damage_recently_killed` | Inc Damage Recently Killed | Inc Damage Recently Killed |
| `INC_DARKNESS_RESERVATION_LASTS` | `inc_darkness_reservation_lasts` | Darkness Reservation lasts for 5 seconds | Darkness Reservation lasts for {X} seconds |
| `INC_DECIMATING_STRIKE` | `inc_decimating_strike` | Decimating Strike | Decimating Strike |
| `INC_DEFEND_ARMOUR` | `inc_defend_armour` | Defend with 200% of Armour | Defend with {X}% of Armor |
| `INC_DEFLECTION_RATING_EQUAL_ARMOUR` | `inc_deflection_rating_equal_armour` | Gain Deflection Rating equal to 20% of Armour | gain Deflection Rating equal to {X}% of Armor |
| `INC_DEMONFLAME_NO` | `inc_demonflame_no` | Demonflame has no maximum | Demonflame has no Max |
| `INC_DOUBLE_ADAPTATION_EFFECT` | `inc_double_adaptation_effect` | Double Adaptation Effect | Double Adaptation Effect |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS` | `inc_double_number_poisons_targets` | Double the number of your Poisons that targets can be affected by at the same time | Double the number of your Poisons that targets can be affected by at the same time |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS_POISONED` | `inc_double_number_poisons_targets_poisoned` | Double the number of your Poisons that targets can be affected by at the same time | Double the number of your Poisons that targets can be affected by at the same time |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL` | `inc_elemental_archon_cast_spell` | Gain Elemental Archon when you cast a Spell while on High Infernal Flame | gain Elemental Archon when you cast a Spell while on High Infernal Flame |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL_WHILE_ON` | `inc_elemental_archon_cast_spell_while_on` | Gain Elemental Archon when you cast a Spell while on High Infernal Flame | gain Elemental Archon when you cast a Spell while on High Infernal Flame |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH` | `inc_elemental_archon_expire_high` | Elemental Archon does not expire while on High Infernal Flame | Elemental Archon does not expire while on High Infernal Flame |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH_WHILE_ON` | `inc_elemental_archon_expire_high_while_on` | Elemental Archon does not expire while on High Infernal Flame | Elemental Archon does not expire while on High Infernal Flame |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING` | `inc_elemental_damage_contributes_bleeding` | Elemental Damage also Contributes to Bleeding BuffMagnitude | Elemental Damage also Contributes to Bleeding Buff Magnitude |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING_ELEMENTALDAMAGE` | `inc_elemental_damage_contributes_bleeding_elementaldamage` | Elemental Damage also Contributes to Bleeding BuffMagnitude | Elemental Damage also Contributes to Bleeding Buff Magnitude |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN` | `inc_elemental_damage_is_taken` | 100% of Elemental Damage is taken from Mana before Life | {X}% of Elemental Damage is taken from Mana before Health |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN_ELEMENTALDAMAGE` | `inc_elemental_damage_is_taken_elementaldamage` | 100% of Elemental Damage is taken from Mana before Life | {X}% of Elemental Damage is taken from Mana before Health |
| `INC_EQUIP_FOCUS_WIELDING_STAFF` | `inc_equip_focus_wielding_staff` | You can equip a Focus while wielding a Staff | You can equip a Focus while wielding a Staff |
| `INC_ES_RECHARGE_START` | `inc_es_recharge_start` | Inc Es Recharge Start | Inc Es Recharge Start |
| `INC_EVASION_RATING_BODY_ARMOUR` | `inc_evasion_rating_body_armour` | Evasion Rating from Equipped Body Armour is halved | Evasion from Equipped Body Armor is halved |
| `INC_EVASION_RATING_EXTRA_AILMENT` | `inc_evasion_rating_extra_ailment` | Gain 100% of Evasion Rating as extra Ailment Threshold | gain {X}% of Evasion as extra Ailment Threshold |
| `INC_EVASION_RATING_EXTRA_AILMENT_AILMENTTHRESHOLD` | `inc_evasion_rating_extra_ailment_ailmentthreshold` | Gain 100% of Evasion Rating as extra Ailment Threshold | gain {X}% of Evasion as extra Ailment Threshold |
| `INC_EVASION_RATING_HELMET_GLOVES` | `inc_evasion_rating_helmet_gloves` | Evasion Rating from Equipped Helmet, Gloves and Boots is doubled | Evasion from Equipped Helmet, Gloves and Boots is doubled |
| `INC_EVASION_RATING_HELMET_GLOVES_HELMET` | `inc_evasion_rating_helmet_gloves_helmet` | Evasion Rating from Equipped Helmet, Gloves and Boots is doubled | Evasion from Equipped Helmet, Gloves and Boots is doubled |
| `INC_EXCESS_LIFE_RECOVERY_REGENERATION` | `inc_excess_life_recovery_regeneration` | Excess Life Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge | Excess Health Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge |
| `INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD` | `inc_excess_life_recovery_regeneration_energyshield` | Excess Life Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge | Excess Health Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge |
| `INC_FASTER_START_ENERGY_SHIELD_ARCHON` | `inc_faster_start_energy_shield_archon` | Inc Faster Start Energy Shield Archon | Inc Faster Start Energy Shield Archon |
| `INC_FASTER_START_ENERGY_SHIELD_FLAT` | `inc_faster_start_energy_shield_flat` | Inc Faster Start Energy Shield Flat | Inc Faster Start Energy Shield Flat |
| `INC_FASTER_START_ENERGY_SHIELD_NO_CONDITION` | `inc_faster_start_energy_shield_no_condition` | Inc Faster Start Energy Shield No Condition | Inc Faster Start Energy Shield No Condition |
| `INC_FASTER_START_ENERGY_SHIELD_UNCONDITIONAL` | `inc_faster_start_energy_shield_unconditional` | Inc Faster Start Energy Shield Unconditional | Inc Faster Start Energy Shield Unconditional |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING` | `inc_fewer_lightning_surge_triggering` | Gain 1 fewer Lightning Surge from Triggering Elemental Surge | gain {X} fewer Lightning Surge from Triggering Elemental Surge |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING_TRIGGER` | `inc_fewer_lightning_surge_triggering_trigger` | Gain 1 fewer Lightning Surge from Triggering Elemental Surge | gain {X} fewer Lightning Surge from Triggering Elemental Surge |
| `INC_FIRE_DAMAGE_ON_IGNITED` | `inc_fire_damage_on_ignited` | Inc Fire Damage On Ignited | Inc Fire Damage On Ignited |
| `INC_FIRE_DAMAGE_SPELL` | `inc_fire_damage_spell` | Inc Fire Damage Spell | Inc Fire Damage Spell |
| `INC_FLAMES_CHAYULA_MANIFEST_BLUE` | `inc_flames_chayula_manifest_blue` | All Flames of Chayula that you manifest are BlueFlamesOfChayula | All Flames of Chayula that you manifest are Blue Flames Of Chayula |
| `INC_FLAMES_CHAYULA_MANIFEST_PURPLE` | `inc_flames_chayula_manifest_purple` | All Flames of Chayula that you manifest are PurpleFlamesOfChayula | All Flames of Chayula that you manifest are Purple Flames Of Chayula |
| `INC_FLAMES_CHAYULA_MANIFEST_RED` | `inc_flames_chayula_manifest_red` | All Flames of Chayula that you manifest are RedFlamesOfChayula | All Flames of Chayula that you manifest are Red Flames Of Chayula |
| `INC_FOUND_QUANTITY` | `inc_found_quantity` | Inc Found Quantity | Inc Found Quantity |
| `INC_FOUND_RARITY` | `inc_found_rarity` | Inc Found Rarity | Inc Found Rarity |
| `INC_FREEZING_CREATE_CHILLED_GROUND` | `inc_freezing_create_chilled_ground` | On Freezing Enemies create Chilled Ground | On Freezing Enemies create Chilled Ground |
| `INC_FREEZING_CREATE_CHILLED_GROUND_FREEZE` | `inc_freezing_create_chilled_ground_freeze` | On Freezing Enemies create Chilled Ground | On Freezing Enemies create Chilled Ground |
| `INC_FROZEN_COLD_RESISTANCE` | `inc_frozen_cold_resistance` | Enemies Frozen by you have -8% to Cold Resistance | Enemies Frozen by you have -{X}% to Cold Resistance |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_HITDAMAGE` | `inc_fully_broken_armour_inflict_hitdamage` | Fully Broken Armour you inflict increases all Damage Taken from HitDamage instead | Fully Broken Armor you inflict increased all Damage Taken from Hit Damage instead |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_INFLICT` | `inc_fully_broken_armour_inflict_inflict` | Inc Fully Broken Armour Inflict Inflict | Inc Fully Broken Armor Inflict Inflict |
| `INC_GLORY_GENERATION_GLOBAL` | `inc_glory_generation_global` | Inc Glory Generation Global | Inc Glory Generation Global |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES` | `inc_heavy_stun_buildup_empties` | Your HeavyStunPlayer buildup empties 50% faster if you've successfully Parried Recently | Your Heavy Stun Player buildup empties {X}% faster if you've successfully Parried Recently |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES_RECENTLY` | `inc_heavy_stun_buildup_empties_recently` | Your HeavyStunPlayer buildup empties 50% faster if you've successfully Parried Recently | Your Heavy Stun Player buildup empties {X}% faster if you've successfully Parried Recently |
| `INC_HINDER_DURATION` | `inc_hinder_duration` | Debuffs you inflict have 4% increased SlowMagnitudeModifier | Debuffs you inflict have {X}% increased Slow Magnitude Modifier |
| `INC_HITTING_LIFE_FLASK_IS` | `inc_hitting_life_flask_is` | On HitDamage an Enemy while a Life Flask is at full Charges, 40% of its Charges are consumed Gain 1% of damage as Physical damage for 5 seconds per Charge consumed this way | On Hit Damage an Enemy while a Health Flask is at full Charges, {X}% of its Charges are consumed gain {X}% of damage as Physical damage for {X} seconds per Charge consumed this way |
| `INC_HITTING_LIFE_FLASK_IS_WHILE_A` | `inc_hitting_life_flask_is_while_a` | On HitDamage an Enemy while a Life Flask is at full Charges, 40% of its Charges are consumed Gain 1% of damage as Physical damage for 5 seconds per Charge consumed this way | On Hit Damage an Enemy while a Health Flask is at full Charges, {X}% of its Charges are consumed gain {X}% of damage as Physical damage for {X} seconds per Charge consumed this way |
| `INC_IGNORE_ALL_MOVEMENT_PENALTIES` | `inc_ignore_all_movement_penalties` | Inc Ignore All Movement Penalties | Inc Ignore All Movement Penalties |
| `INC_IGNORE_WARCRY_COOLDOWNS` | `inc_ignore_warcry_cooldowns` | Ignore Warcry Cooldowns | Ignore Warcry Cooldowns |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION` | `inc_increases_reductions_mana_regeneration` | Increases and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate | increased and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION_RAGE` | `inc_increases_reductions_mana_regeneration_rage` | Increases and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate | increased and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate |
| `INC_INEVITABLE_CRITICAL` | `inc_inevitable_critical` | Inevitable Critical Hits | Inevitable Critical Hits |
| `INC_INFERNAL_FLAME_LOST_NONE` | `inc_infernal_flame_lost_none` | 25% of Infernal Flame lost per second if none was gained in the past 2 seconds | {X}% of Infernal Flame gain per second if none was gain in the past {X} seconds |
| `INC_INFERNAL_FLAME_LOST_NONE_IF_NONE` | `inc_infernal_flame_lost_none_if_none` | #% of Infernal Flame lost per second if none was gained in the past # seconds | #% of Infernal Flame gain per second if none was gain in the past # seconds |
| `INC_INFUSION_MAGNITUDE` | `inc_infusion_magnitude` | Inc Infusion Magnitude | Inc Infusion Magnitude |
| `INC_INHERENT_BONUSES_ATTRIBUTES_DOUBLED` | `inc_inherent_bonuses_attributes_doubled` | Inherent bonuses gained from Attributes are doubled | Inherent bonuses gain from Attributes are doubled |
| `INC_IN_PRESENCE_EXPOSURE` | `inc_in_presence_exposure` | Enemies in your Presence have Exposure | Enemies in your Presence have Exposure |
| `INC_IN_PRESENCE_SLOWED` | `inc_in_presence_slowed` | Enemies in your Presence are Slowed by 20% | Enemies in your Presence are Slowed by {X}% |
| `INC_JEWELLERY_BONUSES` | `inc_jewellery_bonuses` | 25% increased bonuses gained from Equipped Rings and Amulets | {X}% increased bonuses gain from Equipped Rings and Amulets |
| `INC_JEWEL_BONUS_EFFECT` | `inc_jewel_bonus_effect` | 100% increased Effect of bonuses gained from Socketed Jewel | {X}% increased Effect of bonuses gain from Socketed Jewel |
| `INC_LEECHES_RESPONSE_HEALTH` | `inc_leeches_response_health` | Inc Leeches Response Health | Inc Leeches Response Health |
| `INC_LEECHES_RESPONSE_MANA` | `inc_leeches_response_mana` | Inc Leeches Response Mana | Inc Leeches Response Mana |
| `INC_LIFE_CANNOT_CHANGE_ENERGY` | `inc_life_cannot_change_energy` | Your Life cannot change while you have Energy Shield | Your Health cannot change while you have Energy Shield |
| `INC_LIFE_CANNOT_CHANGE_ENERGY_WHILE_YOU` | `inc_life_cannot_change_energy_while_you` | Your Life cannot change while you have Energy Shield | Your Health cannot change while you have Energy Shield |
| `INC_LIFE_FLASK_CHARGE_SPENT` | `inc_life_flask_charge_spent` | Gain 1 Life Flask Charge per 2% Life spent | gain {X} Health Flask Charge per {X}% Health spent |
| `INC_LIFE_FLASK_EFFECTS_REMOVED` | `inc_life_flask_effects_removed` | Life Flask Effects are not removed when Reservation Life is Filled Life Flask Effects do not Queue | Health Flask Effects are not add when Reservation Health is Filled Health Flask Effects do not Queue |
| `INC_LIFE_FLASK_EFFECTS_REMOVED_RESERVATION` | `inc_life_flask_effects_removed_reservation` | Life Flask Effects are not removed when Reservation Life is Filled Life Flask Effects do not Queue | Health Flask Effects are not add when Reservation Health is Filled Health Flask Effects do not Queue |
| `INC_LIFE_LOSS_IS_PREVENTED` | `inc_life_loss_is_prevented` | 25% of Life Loss from HitDamage is prevented, then that much Life is lost over 4 seconds instead | {X}% of Health Loss from Hit Damage is prevented, then that much Health is gain over {X} seconds instead |
| `INC_LIFE_LOSS_IS_PREVENTED_HITDAMAGE` | `inc_life_loss_is_prevented_hitdamage` | 25% of Life Loss from HitDamage is prevented, then that much Life is lost over 4 seconds instead | {X}% of Health Loss from Hit Damage is prevented, then that much Health is gain over {X} seconds instead |
| `INC_LIGHTNING_DAMAGE_ON_SHOCKED` | `inc_lightning_damage_on_shocked` | Inc Lightning Damage On Shocked | Inc Lightning Damage On Shocked |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE` | `inc_lightning_damage_taken_fire` | 20% of Lightning Damage taken as Fire Damage | {X}% of Lightning Damage taken as Fire Damage |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE_CONVERT_FIRE` | `inc_lightning_damage_taken_fire_convert_fire` | 20% of Lightning Damage taken as Fire Damage | {X}% of Lightning Damage taken as Fire Damage |
| `INC_LIGHT_DAMAGE_SPELL` | `inc_light_damage_spell` | Inc Light Damage Spell | Inc Light Damage Spell |
| `INC_LOSE_ALL_INFERNAL_FLAME` | `inc_lose_all_infernal_flame` | Lose all Infernal Flame on reaching maximum Infernal Flame | gain all Infernal Flame on reaching Max Infernal Flame |
| `INC_LOSE_ALL_TAILWIND` | `inc_lose_all_tailwind` | Lose all Tailwind when HitDamage | gain all Tailwind when Hit Damage |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING` | `inc_lose_elemental_archon_reaching` | Lose Elemental Archon on reaching maximum Infernal Flame | gain Elemental Archon on reaching Max Infernal Flame |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING_ELEMENTALARCHON` | `inc_lose_elemental_archon_reaching_elementalarchon` | Lose Elemental Archon on reaching maximum Infernal Flame | gain Elemental Archon on reaching Max Infernal Flame |
| `INC_LOSE_MANA` | `inc_lose_mana` | Lose 5% of maximum Mana per Second | gain {X}% of Max Mana per Second |
| `INC_LOW_MANA_ALLIES_IN` | `inc_low_mana_allies_in` | While you are not on Low Mana, you and Allies in your Presence have Unholy Might | While you are not on Low Mana, you and Allies in your Presence have Unholy Might |
| `INC_LOW_MANA_ALLIES_IN_WHILE_YOU` | `inc_low_mana_allies_in_while_you` | While you are not on Low Mana, you and Allies in your Presence have Unholy Might | While you are not on Low Mana, you and Allies in your Presence have Unholy Might |
| `INC_MANA_IS_REPLACED_TWICE` | `inc_mana_is_replaced_twice` | Maximum Mana is replaced by twice as much Maximum Infernal Flame Gain Infernal Flame instead of spending Mana for Skill costs | Max Mana is replaced by twice as much Max Infernal Flame gain Infernal Flame instead of spending Mana for Skill costs |
| `INC_MANA_IS_REPLACED_TWICE_BY_INFERNAL_FLAME` | `inc_mana_is_replaced_twice_by_infernal_flame` | Maximum Mana is replaced by twice as much Maximum Infernal Flame Gain Infernal Flame instead of spending Mana for Skill costs | Max Mana is replaced by twice as much Max Infernal Flame gain Infernal Flame instead of spending Mana for Skill costs |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT` | `inc_mana_trigger_charms_insufficient` | Can instead consume 25% of maximum Mana to trigger Charms with insufficient charges | Can instead consume {X}% of Max Mana to trigger Charms with insufficient charges |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT_CHARM` | `inc_mana_trigger_charms_insufficient_charm` | Can instead consume 25% of maximum Mana to trigger Charms with insufficient charges | Can instead consume {X}% of Max Mana to trigger Charms with insufficient charges |
| `INC_MAX_DARKNESS` | `inc_max_darkness` | 10% increased maximum Darkness | {X}% increased Max Darkness |
| `INC_MAX_LIFE_REGEN` | `inc_max_life_regen` | Inc Max Life Regen | Inc Max Health Regen |
| `INC_META_DAMAGE` | `inc_meta_damage` | Inc Meta Damage | Inc Meta Damage |
| `INC_MINIONS_REVIVE_FASTER_ALL` | `inc_minions_revive_faster_all` | Inc Minions Revive Faster All | Inc Minions Revive Faster All |
| `INC_MINION_CRIT_CHANCE` | `inc_minion_crit_chance` | Inc Minion Crit Chance | Inc Minion Crit Chance |
| `INC_MINION_ELEMENTAL_RESIST` | `inc_minion_elemental_resist` | Inc Minion Elemental Resist | Inc Minion Elemental Resist |
| `INC_MOVEMENT_SPEED_STATIONARY` | `inc_movement_speed_stationary` | Inc Movement Speed Stationary | Inc Movement Speed Stationary |
| `INC_NON_DAMAGING_AILMENT_EFFECT_TAKEN` | `inc_non_damaging_ailment_effect_taken` | Inc Non Damaging Ailment Effect Taken | Inc Non Damaging Ailment Effect Taken |
| `INC_NON_UNIQUE_TIME_LOST` | `inc_non_unique_time_lost` | Non-Unique Time-Lost Jewels have 40% increased radius | Non-Unique Time-gain Jewels have {X}% increased radius |
| `INC_NON_UNIQUE_TIME_LOST_RARITY` | `inc_non_unique_time_lost_rarity` | Non-Unique Time-Lost Jewels have 40% increased radius | Non-Unique Time-gain Jewels have {X}% increased radius |
| `INC_NO_INHERENT_LOSS_RAGE` | `inc_no_inherent_loss_rage` | No Inherent loss of Rage | No Inherent loss of Rage |
| `INC_NO_INHERENT_MANA_REGENERATION` | `inc_no_inherent_mana_regeneration` | No inherent Mana Regeneration | No inherent Mana Regeneration |
| `INC_NO_SPIRIT_BASE_DARKNESS` | `inc_no_spirit_base_darkness` | You have no Spirit Base Maximum Darkness is 100 Damage taken is Reserved from Darkness before being taken from Life or Energy Shield | You have no Spirit Base Max Darkness is {X} Damage taken is Reserved from Darkness before being taken from Health or Energy Shield |
| `INC_NO_SPIRIT_BASE_DARKNESS_ENERGYSHIELD` | `inc_no_spirit_base_darkness_energyshield` | You have no Spirit Base Maximum Darkness is 100 Damage taken is Reserved from Darkness before being taken from Life or Energy Shield | You have no Spirit Base Max Darkness is {X} Damage taken is Reserved from Darkness before being taken from Health or Energy Shield |
| `INC_ONLY_NORMAL_BODY_ARMOUR` | `inc_only_normal_body_armour` | Can only use a ItemRarity Body Armour | Can only use a Item Rarity Body Armor |
| `INC_OWL_FEATHERS_FASTER` | `inc_owl_feathers_faster` | Gain Owl Feathers 50% faster | gain Owl Feathers {X}% faster |
| `INC_PASSIVE_POINTS_BECOME_WEAPON` | `inc_passive_points_become_weapon` | 100 Passive Skill Points become WeaponSetPassiveSkillPoints | {X} Passive Skill Points become Weapon Set Passive Skill Points |
| `INC_PASSIVE_POINTS_BECOME_WEAPON_WEAPONSETPASSIVESKILLPOINTS` | `inc_passive_points_become_weapon_weaponsetpassiveskillpoints` | 100 Passive Skill Points become WeaponSetPassiveSkillPoints | {X} Passive Skill Points become Weapon Set Passive Skill Points |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR` | `inc_physical_damage_reduction_armour` | Physical Damage Reduction from Armour is based on your combined Armour and Evasion Rating | Physical Damage Reduction from Armor is based on your combined Armor and Evasion |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR_EVASION` | `inc_physical_damage_reduction_armour_evasion` | Physical Damage Reduction from Armour is based on your combined Armour and Evasion Rating | Physical Damage Reduction from Armor is based on your combined Armor and Evasion |
| `INC_PHYSICAL_DAMAGE_REDUCTION_IS` | `inc_physical_damage_reduction_is` | Maximum Physical Damage Reduction is 50% | Max Physical Damage Reduction is {X}% |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS` | `inc_physical_damage_taken_chaos` | 20% of Physical Damage taken as Chaos Damage | {X}% of Physical Damage taken as Chaos Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS_CONVERT_CHAOS` | `inc_physical_damage_taken_chaos_convert_chaos` | 20% of Physical Damage taken as Chaos Damage | {X}% of Physical Damage taken as Chaos Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_COLD` | `inc_physical_damage_taken_cold` | 5% of Physical Damage taken as Cold Damage | {X}% of Physical Damage taken as Cold Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING` | `inc_physical_damage_taken_lightning` | 5% of Physical Damage taken as Lightning Damage | {X}% of Physical Damage taken as Lightning Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING_CONVERT_LIGHTNING` | `inc_physical_damage_taken_lightning_convert_lightning` | 5% of Physical Damage taken as Lightning Damage | {X}% of Physical Damage taken as Lightning Damage |
| `INC_PINNED_CANNOT_PERFORM_ACTIONS` | `inc_pinned_cannot_perform_actions` | Pinned enemies cannot perform actions | Pinned enemies cannot perform actions |
| `INC_POWER_CHARGES_FRENZY_ENDURANCE` | `inc_power_charges_frenzy_endurance` | Gain Power Charges instead of Frenzy Charges Gain Frenzy Charges instead of Endurance Charges Gain Endurance Charges instead of Power Charges | gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges |
| `INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE` | `inc_power_charges_frenzy_endurance_charge` | Gain Power Charges instead of Frenzy Charges Gain Frenzy Charges instead of Endurance Charges Gain Endurance Charges instead of Power Charges | gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges |
| `INC_PRIMAL_OWL_FEATHER_UP` | `inc_primal_owl_feather_up` | Gain a Primal Owl Feather every 4 seconds, up to a maximum of 3 Expend an Owl Feather when you Dodge to trigger Primal Bounty | gain a Primal Owl Feather every {X} seconds, up to a Max of {X} Expend an Owl Feather when you Dodge to trigger Primal Bounty |
| `INC_PRIMAL_OWL_FEATHER_UP_OWL_FEATHER` | `inc_primal_owl_feather_up_owl_feather` | Gain a Primal Owl Feather every # seconds, up to a maximum of # Expend an Owl Feather when you Dodge to trigger Primal Bounty | gain a Primal Owl Feather every # seconds, up to a Max of # Expend an Owl Feather when you Dodge to trigger Primal Bounty |
| `INC_PROJECTILE_DAMAGE_BUILDS_PIN` | `inc_projectile_damage_builds_pin` | Projectile Damage builds Pinned | Projectile Damage builds Pinned |
| `INC_RAGE_COST` | `inc_rage_cost` | Skills have +5 to Rage cost | Skills have +{X} to Rage cost |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN` | `inc_regain_concentration_they_haven` | Enemies regain 10% of Concentration every second if they haven't lost Concentration in the past 5 seconds | Enemies regain {X}% of Concentration every second if they haven't gain Concentration in the past {X} seconds |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN_IF_NOT_LOST` | `inc_regain_concentration_they_haven_if_not_lost` | Enemies regain 10% of Concentration every second if they haven't lost Concentration in the past 5 seconds | Enemies regain {X}% of Concentration every second if they haven't gain Concentration in the past {X} seconds |
| `INC_REGENERATE_LIFE_PER_SECOND` | `inc_regenerate_life_per_second` | Regenerate #% of maximum Life per second if you have been Hi | Regenerate #% of Max Health per second if you have been Hi |
| `INC_REGENERATE_MANA_EQUAL_LIFE` | `inc_regenerate_mana_equal_life` | Regenerate Mana equal to 6% of maximum Life per second | Regenerate Mana equal to {X}% of Max Health per second |
| `INC_REGENERATE_RAGE` | `inc_regenerate_rage` | Regenerate 6% of your maximum Rage per second | Regenerate {X}% of your Max Rage per second |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE` | `inc_remnants_create_reappear_once` | Remnants you create reappear once, 3 seconds after being collected | Remnants you create reappear once, {X} seconds after being collected |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE_REAPPEAR_ONCE` | `inc_remnants_create_reappear_once_reappear_once` | Remnants you create reappear once, 3 seconds after being collected | Remnants you create reappear once, {X} seconds after being collected |
| `INC_RESERVES_LIFE` | `inc_reserves_life` | Reservation 25% of Life | Reservation {X}% of Health |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE` | `inc_reveal_weaknesses_rare_unique` | Reveal Weaknesses against Rare and Unique enemies | Reveal Weaknesses against Rare and Unique enemies |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE_REVEALWEAKNESS` | `inc_reveal_weaknesses_rare_unique_revealweakness` | Reveal Weaknesses against Rare and Unique enemies | Reveal Weaknesses against Rare and Unique enemies |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT` | `inc_sacrificing_energy_shield_interrupt` | Sacrificing Energy Shield does not interrupt ESRecharge | Sacrificing Energy Shield does not interrupt ESRecharge |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT_SACRIFICE` | `inc_sacrificing_energy_shield_interrupt_sacrifice` | Sacrificing Energy Shield does not interrupt ESRecharge | Sacrificing Energy Shield does not interrupt ESRecharge |
| `INC_SKILL_QUALITY` | `inc_skill_quality` | Inc Skill Quality | Inc Skill Quality |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED` | `inc_slam_is_ancestrally_boosted` | Every second Slam Skill you use yourself is Ancestrally Boosted | Every second Slam Skill you use yourself is Ancestrally Boosted |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED_ANCESTRALBOOST` | `inc_slam_is_ancestrally_boosted_ancestralboost` | Every second Slam Skill you use yourself is Ancestrally Boosted | Every second Slam Skill you use yourself is Ancestrally Boosted |
| `INC_SLOW_MAGNITUDE` | `inc_slow_magnitude` | Debuffs you inflict have 5% increased SlowMagnitudeModifier | Debuffs you inflict have {X}% increased Slow Magnitude Modifier |
| `INC_SMALL_PASSIVE_EFFECT` | `inc_small_passive_effect` | 50% increased effect of Small Passive Skills | {X}% increased effect of Small Passive Skills |
| `INC_SOCKET_NON_UNIQUE_BASIC` | `inc_socket_non_unique_basic` | Can Socket a non-Unique Basic Jewel into the Phylactery | Can Socket a non-Unique Basic Jewel into the Phylactery |
| `INC_SOCKET_NON_UNIQUE_BASIC_ITEMRARITY` | `inc_socket_non_unique_basic_itemrarity` | Can Socket a non-Unique Basic Jewel into the Phylactery | Can Socket a non-Unique Basic Jewel into the Phylactery |
| `INC_SORCERY_WARD_BARRIER_TAKE` | `inc_sorcery_ward_barrier_take` | Sorcery Ward's Barrier can also take Physical and Chaos Damage from HitDamage | Sorcery Ward's Barrier can also take Physical and Chaos Damage from Hit Damage |
| `INC_SORCERY_WARD_BARRIER_TAKE_HITDAMAGE` | `inc_sorcery_ward_barrier_take_hitdamage` | Sorcery Ward's Barrier can also take Physical and Chaos Damage from HitDamage | Sorcery Ward's Barrier can also take Physical and Chaos Damage from Hit Damage |
| `INC_SPEED_IS_UNAFFECTED_SLOWS` | `inc_speed_is_unaffected_slows` | Your speed is unaffected by Slows | Your speed is unaffected by Slows |
| `INC_SPELL_DAMAGE_LEECHED_LIFE` | `inc_spell_damage_leeched_life` | 20% of Spell Damage Leeched as Life | {X}% of Spell Damage Leeched as Health |
| `INC_STACK_JADE` | `inc_stack_jade` | Gain a stack of Jade every second | gain a stack of Jade every second |
| `INC_TAILWIND` | `inc_tailwind` | Gain Tailwind on Skill use | gain Tailwind on Skill use |
| `INC_TAKEN_CRIT_DAMAGE` | `inc_taken_crit_damage` | Inc Taken Crit Damage | Inc Taken Crit Damage |
| `INC_TAKE_DAMAGE_BLOCKED` | `inc_take_damage_blocked` | You take 20% of damage from Blocked HitDamage | You take {X}% of damage from Blocked Hit Damage |
| `INC_TAKE_LIFE_ENERGY_SHIELD` | `inc_take_life_energy_shield` | Take maximum Life and Energy Shield as Fire Damage when Infernal Flame reaches maximum | Take Max Health and Energy Shield as Fire Damage when Infernal Flame reaches Max |
| `INC_TAKE_LIFE_ENERGY_SHIELD_FIRE` | `inc_take_life_energy_shield_fire` | Take maximum Life and Energy Shield as Fire Damage when Infernal Flame reaches maximum | Take Max Health and Energy Shield as Fire Damage when Infernal Flame reaches Max |
| `INC_TAKE_NO_DAMAGE_VOLATILITY` | `inc_take_no_damage_volatility` | Take no Damage from Volatility | Take no Damage from Volatility |
| `INC_TAME_BEAST_CAPTURE_UNIQUE` | `inc_tame_beast_capture_unique` | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned |
| `INC_TAME_BEAST_CAPTURE_UNIQUE_RARITY` | `inc_tame_beast_capture_unique_rarity` | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned |
| `INC_TARGETS_AFFECTED_TWO_CHILLS` | `inc_targets_affected_two_chills` | Targets can be affected by two of your Chills at the same time | Targets can be affected by two of your Chills at the same time |
| `INC_TARGETS_AFFECTED_TWO_CHILLS_CHILL` | `inc_targets_affected_two_chills_chill` | Targets can be affected by two of your Chills at the same time | Targets can be affected by two of your Chills at the same time |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS` | `inc_targets_affected_two_shocks` | Targets can be affected by two of your Shocks at the same time | Targets can be affected by two of your Shocks at the same time |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS_SHOCK` | `inc_targets_affected_two_shocks_shock` | Targets can be affected by two of your Shocks at the same time | Targets can be affected by two of your Shocks at the same time |
| `INC_TARGETS_CURSED_LIFE_RESERVED` | `inc_targets_cursed_life_reserved` | Targets Cursed by you have at least 15% of Life Reservation | Targets Cursed by you have at least {X}% of Health Reservation |
| `INC_TARGETS_CURSED_LIFE_RESERVED_CURSE` | `inc_targets_cursed_life_reserved_curse` | Targets Cursed by you have at least 15% of Life Reservation | Targets Cursed by you have at least {X}% of Health Reservation |
| `INC_TATTOO_RUNES_ONTO_BODY` | `inc_tattoo_runes_onto_body` | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * 1 Helmet socket * 2 Body Armour sockets * 1 Gloves socket * 1 Boots socket | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * {X} Helmet socket * {X} Body Armor sockets * {X} Gloves socket * {X} Boots socket |
| `INC_TATTOO_RUNES_ONTO_BODY_BODY_ARMOUR` | `inc_tattoo_runes_onto_body_body_armour` | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * 1 Helmet socket * 2 Body Armour sockets * 1 Gloves socket * 1 Boots socket | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * {X} Helmet socket * {X} Body Armor sockets * {X} Gloves socket * {X} Boots socket |
| `INC_THERE_IS_NO_LIMIT` | `inc_there_is_no_limit` | There is no Limit on the number of Banners you can place | There is no Limit on the number of Banners you can place |
| `INC_THERE_IS_NO_LIMIT_BANNER` | `inc_there_is_no_limit_banner` | There is no Limit on the number of Banners you can place | There is no Limit on the number of Banners you can place |
| `INC_THIRD_SLAM_DOESN_CREATE` | `inc_third_slam_doesn_create` | Every Third Slam skill that doesn't create Fissures which you use yourself causes 3 additional Aftershocks ahead and to each side of the initial area | Every Third Slam skill that doesn't create Fissures which you use yourself causes {X} additional Aftershocks ahead and to each side of the initial area |
| `INC_THIRD_SLAM_DOESN_CREATE_AFTERSHOCK` | `inc_third_slam_doesn_create_aftershock` | Every Third Slam skill that doesn't create Fissures which you use yourself causes 3 additional Aftershocks ahead and to each side of the initial area | Every Third Slam skill that doesn't create Fissures which you use yourself causes {X} additional Aftershocks ahead and to each side of the initial area |
| `INC_TOTEMS_ALL_ELEMENTAL_RESISTANCES` | `inc_totems_all_elemental_resistances` | Inc Totems All Elemental Resistances | Inc Totems All Elemental Resistances |
| `INC_TOTEMS_DIE_LIFE_IS` | `inc_totems_die_life_is` | Totems die 6 seconds after their Life is reduced to 0 | Totems die {X} seconds after their Health is increased to {X} |
| `INC_TOTEMS_ONLY_FIRE_ATTACK` | `inc_totems_only_fire_attack` | Totems only use Skills when you fire an Attack Projectile | Totems only use Skills when you fire an Attack Projectile |
| `INC_TOTEMS_ONLY_FIRE_ATTACK_PROJECTILE` | `inc_totems_only_fire_attack_projectile` | Totems only use Skills when you fire an Attack Projectile | Totems only use Skills when you fire an Attack Projectile |
| `INC_TOTEM_LIMIT_IS_DOUBLED` | `inc_totem_limit_is_doubled` | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve 75 Spirit each | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each |
| `INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE` | `inc_totem_limit_is_doubled_charge` | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve 75 Spirit each | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each |
| `INC_TRIGGER_ANCESTRAL_SPIRITS_SUMMON` | `inc_trigger_ancestral_spirits_summon` | Trigger Ancestral Spirits when you Summon a Totem | Trigger Ancestral Spirits when you Summon a Totem |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE` | `inc_trigger_elemental_expression_melee` | Trigger Elemental Expression on Melee Critical Hit | Trigger Elemental Expression on Melee Critical Hit |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE_CRITICAL` | `inc_trigger_elemental_expression_melee_critical` | Trigger Elemental Expression on Melee Critical Hit | Trigger Elemental Expression on Melee Critical Hit |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL` | `inc_trigger_elemental_storm_critical` | Trigger Elemental Storm on Critical Hit with Spells | Trigger Elemental Storm on Critical Hit with Spells |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL_SPELL` | `inc_trigger_elemental_storm_critical_spell` | Trigger Elemental Storm on Critical Hit with Spells | Trigger Elemental Storm on Critical Hit with Spells |
| `INC_TWO_HAND_DAMAGE` | `inc_two_hand_damage` | Inc Two Hand Damage | Inc Two Hand Damage |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED` | `inc_unique_tamed_beasts_possessed` | Unique Tamed Beasts are SpiritPossessed by random Azmeri Spirits, changing every 20 seconds | Unique Tamed Beasts are Spirit Possessed by random Azmeri Spirits, changing every {X} seconds |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED_RARITY` | `inc_unique_tamed_beasts_possessed_rarity` | Unique Tamed Beasts are SpiritPossessed by random Azmeri Spirits, changing every 20 seconds | Unique Tamed Beasts are Spirit Possessed by random Azmeri Spirits, changing every {X} seconds |
| `INC_VIVID_STAGS_LEAP_TOWARDS` | `inc_vivid_stags_leap_towards` | Vivid Stags leap towards enemies | Vivid Stags leap towards enemies |
| `INC_VIVID_WISP_METRES_MOVE` | `inc_vivid_wisp_metres_move` | Gain a Vivid Wisp for every 10 metres you move, up to a maximum of 3 Expend all Vivid Wisps to trigger Vivid Stampede when you Attack | gain a Vivid Wisp for every {X} metres you move, up to a Max of {X} Expend all Vivid Wisps to trigger Vivid Stampede when you Attack |
| `INC_VIVID_WISP_METRES_MOVE_ATTACK` | `inc_vivid_wisp_metres_move_attack` | Gain a Vivid Wisp for every 10 metres you move, up to a maximum of 3 Expend all Vivid Wisps to trigger Vivid Stampede when you Attack | gain a Vivid Wisp for every {X} metres you move, up to a Max of {X} Expend all Vivid Wisps to trigger Vivid Stampede when you Attack |
| `INC_VIVID_WISP_STAMPEDE_ENDS` | `inc_vivid_wisp_stampede_ends` | Gain a Vivid Wisp when Vivid Stampede ends | gain a Vivid Wisp when Vivid Stampede ends |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING` | `inc_volatility_allied_persistent_reviving` | Gain 3 Volatility when an Allied Persistent Reviving Minion is MinionDeath | gain {X} Volatility when an Allied Persistent Reviving Minion is Minion Death |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING_MINIONS` | `inc_volatility_allied_persistent_reviving_minions` | Gain 3 Volatility when an Allied Persistent Reviving Minion is MinionDeath | gain {X} Volatility when an Allied Persistent Reviving Minion is Minion Death |
| `INC_VOLATILITY_GENERATION` | `inc_volatility_generation` | Inc Volatility Generation | Inc Volatility Generation |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT` | `inc_volatility_inflicting_elemental_ailment` | Gain 1 Volatility on inflicting an Elemental Ailment | gain {X} Volatility on inflicting an Elemental Ailment |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT_ELEMENTALAILMENTS` | `inc_volatility_inflicting_elemental_ailment_elementalailments` | Gain 1 Volatility on inflicting an Elemental Ailment | gain {X} Volatility on inflicting an Elemental Ailment |
| `INC_WALK_PATHS_TAKEN` | `inc_walk_paths_taken` | Walk the Paths Not Taken | Walk the Paths Not Taken |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING` | `inc_warcries_explode_corpses_dealing` | Warcries Explode Corpses dealing 25% of their Life as Physical Damage | Warcries Explode Corpses dealing {X}% of their Health as Physical Damage |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING_CORPSE` | `inc_warcries_explode_corpses_dealing_corpse` | Warcries Explode Corpses dealing 25% of their Life as Physical Damage | Warcries Explode Corpses dealing {X}% of their Health as Physical Damage |
| `LEECHING_LIFE_CAUSES_COMPANION` | `stat_leeching_life_causes_companion` | Leeching Life from your HitDamage causes your Companion to also Leech the LeechSameAmount of Life | Leeching Health from your Hit Damage causes your Companion to also Leech the Leech Same Amount of Health |
| `LEECHING_LIFE_CAUSES_COMPANION_LIFELEECH` | `stat_leeching_life_causes_companion_lifeleech` | Leeching Life from your HitDamage causes your Companion to also Leech the LeechSameAmount of Life | Leeching Health from your Hit Damage causes your Companion to also Leech the Leech Same Amount of Health |
| `LEECH_HEALTH` | `leech_health` | Leech Health | Leech Health |
| `LEECH_MANA` | `leech_mana` | Leech Mana | Leech Mana |
| `LEECH_RECOVERS_BASED_CHAOS` | `stat_leech_recovers_based_chaos` | Leech recovers based on Chaos Damage as well as Physical Damage | Leech recovers based on Chaos Damage as well as Physical Damage |
| `LEECH_TRIGGER` | `leech_trigger` | Leech Trigger | Leech Trigger |
| `LIFE_FLASKS_RECOVER_MANA` | `stat_life_flasks_recover_mana` | Life Flasks also recover Mana Mana Flasks also recover Life | Health Flasks also recover Mana Mana Flasks also recover Health |
| `LIFE_FLASKS_RECOVER_MANA_FLASK` | `stat_life_flasks_recover_mana_flask` | Life Flasks also recover Mana Mana Flasks also recover Life | Health Flasks also recover Mana Mana Flasks also recover Health |
| `LIFE_LEECH_RECOVERS_BASED` | `stat_life_leech_recovers_based` | Life Leech recovers based on your ElementalDamage damage as well as Physical damage | Health Leech recovers based on your Elemental Damage damage as well as Physical damage |
| `LIGHTNING_TAKEN_AS_FIRE` | `lightning_taken_as_fire` | 20% of Lightning Damage taken as Fire Damage | {X}% of Lightning Damage taken as Fire Damage |
| `MIND_OVER_MATTER_MANA_TAKE` | `mind_over_matter_mana_take` | All DamageTypes is taken from Mana before Life 50% less Mana Recovery Rate | All Damage Types is taken from Mana before Health {X}% more Mana Recovery Rate |
| `MODIFIERS_FIRE_RESISTANCE_COLD` | `stat_modifiers_fire_resistance_cold` | Modifiers to Maximum Fire Resistance also grant Maximum Cold and Lightning Resistance | Modifiers to Max Fire Resistance also grant Max Cold and Lightning Resistance |
| `MODIFIERS_FIRE_RESISTANCE_COLD_COLD_LIGHTNING` | `stat_modifiers_fire_resistance_cold_cold_lightning` | Modifiers to Fire Resistance also grant Cold and Lightning Resistance at 50% of their value | Modifiers to Fire Resistance also grant Cold and Lightning Resistance at {X}% of their value |
| `MODIFIERS_FIRE_RESISTANCE_COLD_MAXIMUMRESISTANCES` | `stat_modifiers_fire_resistance_cold_maximumresistances` | Modifiers to Maximum Fire Resistance also grant Maximum Cold and Lightning Resistance | Modifiers to Max Fire Resistance also grant Max Cold and Lightning Resistance |
| `MORE_AMOUNT_LEECH` | `more_amount_leech` | 50% more amount of Life Leeched Leech Life 67% less quickly Cannot Recover Life other than from LifeLeech Life Leech effects are not removed when Reservation Life is Filled | {X}% more amount of Health Leeched Leech Health {X}% more quickly Cannot Recover Health other than from Health Leech Health Leech effects are not add when Reservation Health is Filled |
| `MORE_CANNOT_CHARMS_RECOVERY_FLASKS` | `more_cannot_charms_recovery_flasks` | Cannot use Charms 30% more Recovery from Flasks | Cannot use Charms {X}% more Recovery from Flasks |
| `MORE_DODGING_EXPEND_UP_OWL` | `more_dodging_expend_up_owl` | Dodging can expend up to 3 Owl Feathers, granting Primal Bounty 100% more Empowerment effect per additional Feather expended | Dodging can expend up to {X} Owl Feathers, granting Primal Bounty {X}% more Empowerment effect per additional Feather expended |
| `MORE_DODGING_EXPEND_UP_OWL_EMPOWERED` | `more_dodging_expend_up_owl_empowered` | Dodging can expend up to 3 Owl Feathers, granting Primal Bounty 100% more Empowerment effect per additional Feather expended | Dodging can expend up to {X} Owl Feathers, granting Primal Bounty {X}% more Empowerment effect per additional Feather expended |
| `MORE_GLOVES_EQUIP_BASE_TYPE` | `more_gloves_equip_base_type` | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers |
| `MORE_GLOVES_EQUIP_BASE_TYPE_WHILE_EQUIPPED` | `more_gloves_equip_base_type_while_equipped` | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers |
| `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT` | `more_invocation_trigger_spells_cannot` | Invocation Skills instead Trigger Spells every 2 seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume 50% less Energy | Invocation Skills instead Trigger Spells every {X} seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume {X}% more Energy |
| `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL` | `more_invocation_trigger_spells_cannot_spell` | Invocation Skills instead Trigger Spells every 2 seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume 50% less Energy | Invocation Skills instead Trigger Spells every {X} seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume {X}% more Energy |
| `MORE_MAX_RAGE` | `more_max_rage` | 100% more Maximum Rage Regenerate 1 Rage per second per 4 Rage spent Recently No Rage effect | {X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect |
| `MORE_PERSISTENT_BUFFS_RESERVATION` | `more_persistent_buffs_reservation` | Persistent Buffs have 50% less Reservation | Persistent Buffs have {X}% more Reservation |
| `MORE_RAGE_REGENERATE_SPENT_NO` | `more_rage_regenerate_spent_no` | 100% more Maximum Rage Regenerate 1 Rage per second per 4 Rage spent Recently No Rage effect | {X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect |
| `MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY` | `more_rage_regenerate_spent_no_recently` | 100% more Maximum Rage Regenerate 1 Rage per second per 4 Rage spent Recently No Rage effect | {X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP` | `more_stags_shock_magnitude_leap` | Stags have 20% more Shock BuffMagnitude per leap | Stags have {X}% more Shock Buff Magnitude per leap |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP_PER_LEAP` | `more_stags_shock_magnitude_leap_per_leap` | Stags have 20% more Shock BuffMagnitude per leap | Stags have {X}% more Shock Buff Magnitude per leap |
| `NON_KEYSTONE_PASSIVE_IN` | `stat_non_keystone_passive_in` | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree |
| `NON_KEYSTONE_PASSIVE_IN_MEDIUM_RADIUS` | `stat_non_keystone_passive_in_medium_radius` | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree |
| `NO_LIFE_REGEN_WHILE_BLEEDING` | `no_life_regen_while_bleeding` | No Life Regen While Bleeding | No Health Regen While Bleeding |
| `ONSLAUGHT_ATTACK_SPEED` | `onslaught_attack_speed` | Onslaught Attack Speed | Onslaught Attack Speed |
| `ONSLAUGHT_CAST_SPEED` | `onslaught_cast_speed` | Onslaught Cast Speed | Onslaught Cast Speed |
| `ONSLAUGHT_MOVEMENT_SPEED` | `onslaught_movement_speed` | Onslaught Movement Speed | Onslaught Movement Speed |
| `PASSIVE_POINT` | `stat_passive_point` | Grants 1 Passive Skill Point | Grants {X} Passive Skill Point |
| `PASSIVE_POINTS` | `stat_passive_points` | Grants 4 Passive Skill Points | Grants {X} Passive Skill Points |
| `PENETRATE_CHAOS` | `penetrate_chaos` | Penetrate Chaos | Penetrate Chaos |
| `PHYSICAL_TAKEN_AS_CHAOS` | `physical_taken_as_chaos` | 20% of Physical Damage taken as Chaos Damage | {X}% of Physical Damage taken as Chaos Damage |
| `PHYSICAL_TAKEN_AS_COLD` | `physical_taken_as_cold` | 5% of Physical Damage taken as Cold Damage | {X}% of Physical Damage taken as Cold Damage |
| `PHYSICAL_TAKEN_AS_FIRE_BODY_ARMOUR` | `physical_taken_as_fire_body_armour` | Body Armour grants 25% of Physical Damage from HitDamage taken as Fire Damage | Body Armor grants {X}% of Physical Damage from Hit Damage taken as Fire Damage |
| `PHYSICAL_TAKEN_AS_LIGHTNING` | `physical_taken_as_lightning` | 5% of Physical Damage taken as Lightning Damage | {X}% of Physical Damage taken as Lightning Damage |
| `PRESENCE_EXPOSURE_AURA` | `presence_exposure_aura` | Presence Exposure Aura | Presence Exposure Aura |
| `RECOVER_PCT_ENERGY` | `recover_pct_energy` | Recover Pct Energy | Recover Pct Energy |
| `REGEN_MAX_HEALTH_PCT` | `regen_max_health_pct` | Regen Max Health Pct | Regen Max Health Pct |
| `RESOLUTE_TECHNIQUE_ACCURACY` | `resolute_technique_accuracy` | Accuracy Rating is Doubled Never deal Critical Hits | Accuracy is Doubled Never deal Critical Hits |
| `RESOLUTE_TECHNIQUE_CRIT_CHANCE` | `resolute_technique_crit_chance` | Accuracy Rating is Doubled Never deal Critical Hits | Accuracy is Doubled Never deal Critical Hits |
| `SANDS_TIME` | `stat_sands_time` | Grants Sands of Time | Grants Sands of Time |
| `SCARRED_FAITH_PHYS_RECOUP_ENERGY` | `scarred_faith_phys_recoup_energy` | 5% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armour | {X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armor |
| `SPEED_PER_STACK_ATTACK_SPEED` | `speed_per_stack_attack_speed` | Speed Per Stack Attack Speed | Speed Per Stack Attack Speed |
| `SPEED_PER_STACK_CAST_SPEED` | `speed_per_stack_cast_speed` | Speed Per Stack Cast Speed | Speed Per Stack Cast Speed |
| `SPEED_PER_STACK_MOVEMENT_SPEED` | `speed_per_stack_movement_speed` | Speed Per Stack Movement Speed | Speed Per Stack Movement Speed |
| `TAKEN_CRIT` | `taken_crit` | Taken Crit | Taken Crit |
| `TOTEMS_PLACE_EMBANKMENT_AURAS` | `stat_totems_place_embankment_auras` | Totems you place grant TacticianTotemBuff | Totems you place grant Tactician Totem Buff |
| `TOTEM_DAMAGE` | `totem_damage` | 20% of Damage from HitDamage is taken from your nearest Totem's Life before you | {X}% of Damage from Hit Damage is taken from your nearest Totem's Health before you |
| `UNCRITTABLE_WHILE_PARRYING` | `uncrittable_while_parrying` | Uncrittable While Parrying | Uncrittable While Parrying |
| `UNDERLINE_ACIDIC_CONCOCTION` | `stat_underline_acidic_concoction` | Grants Skill: <underline>{Acidic Concoction} | Grants Skill: <underline>{Acidic Concoction} |
| `UNDERLINE_ALIGN_FATE` | `stat_underline_align_fate` | Grants Skill: <underline>{Align Fate} | Grants Skill: <underline>{Align Fate} |
| `UNDERLINE_ANCESTRAL_SPIRITS` | `stat_underline_ancestral_spirits` | Grants Skill: <underline>{Ancestral Spirits} | Grants Skill: <underline>{Ancestral Spirits} |
| `UNDERLINE_APOCALYPSE` | `stat_underline_apocalypse` | Grants Skill: <underline>{Apocalypse} | Grants Skill: <underline>{Apocalypse} |
| `UNDERLINE_ARCHON_CHAYULA` | `stat_underline_archon_chayula` | Grants Skill: <underline>{Archon of Chayula} | Grants Skill: <underline>{Archon of Chayula} |
| `UNDERLINE_BLEEDING_CONCOCTION` | `stat_underline_bleeding_concoction` | Grants Skill: <underline>{Bleeding Concoction} | Grants Skill: <underline>{Bleeding Concoction} |
| `UNDERLINE_BLOOD_BOIL` | `stat_underline_blood_boil` | Grants Skill: <underline>{Blood Boil} | Grants Skill: <underline>{Blood Boil} |
| `UNDERLINE_BREACH` | `stat_underline_breach` | Grants Skill: <underline>{Into the Breach} | Grants Skill: <underline>{Into the Breach} |
| `UNDERLINE_CALLED_SHOTS` | `stat_underline_called_shots` | Grants Skill: <underline>{Called Shots} | Grants Skill: <underline>{Called Shots} |
| `UNDERLINE_DEMON_FORM` | `stat_underline_demon_form` | Grants Skill: <underline>{Demon Form} | Grants Skill: <underline>{Demon Form} |
| `UNDERLINE_ELEMENTAL_EXPRESSION` | `stat_underline_elemental_expression` | Grants Skill: <underline>{Elemental Expression} | Grants Skill: <underline>{Elemental Expression} |
| `UNDERLINE_ELEMENTAL_STORM` | `stat_underline_elemental_storm` | Grants Skill: <underline>{Elemental Storm} | Grants Skill: <underline>{Elemental Storm} |
| `UNDERLINE_ELEMENTAL_SURGE` | `stat_underline_elemental_surge` | Grants Skill: <underline>{Elemental Surge} | Grants Skill: <underline>{Elemental Surge} |
| `UNDERLINE_ENCASE_IN_JADE` | `stat_underline_encase_in_jade` | Grants Skill: <underline>{Encase in Jade} | Grants Skill: <underline>{Encase in Jade} |
| `UNDERLINE_EXPLOSIVE_CONCOCTION` | `stat_underline_explosive_concoction` | Grants Skill: <underline>{Explosive Concoction} | Grants Skill: <underline>{Explosive Concoction} |
| `UNDERLINE_FIRE_SPELL` | `stat_underline_fire_spell` | Grants Skill: <underline>{Fire Spell on Hit} | Grants Skill: <underline>{Fire Spell on Hit} |
| `UNDERLINE_FULMINATING_CONCOCTION` | `stat_underline_fulminating_concoction` | Grants Skill: <underline>{Fulminating Concoction} | Grants Skill: <underline>{Fulminating Concoction} |
| `UNDERLINE_HOLLOW_FOCUS` | `stat_underline_hollow_focus` | Grants Skill: <underline>{Hollow Focus} | Grants Skill: <underline>{Hollow Focus} |
| `UNDERLINE_HOLLOW_FORM` | `stat_underline_hollow_form` | Grants Skill: <underline>{Hollow Form} | Grants Skill: <underline>{Hollow Form} |
| `UNDERLINE_HOLLOW_RESONANCE` | `stat_underline_hollow_resonance` | Grants Skill: <underline>{Hollow Resonance} | Grants Skill: <underline>{Hollow Resonance} |
| `UNDERLINE_INEVITABLE_AGONY` | `stat_underline_inevitable_agony` | Grants Skill: <underline>{Inevitable Agony} | Grants Skill: <underline>{Inevitable Agony} |
| `UNDERLINE_KELARI_DECEPTION` | `stat_underline_kelari_deception` | Grants Skill: <underline>{Kelari's Deception} | Grants Skill: <underline>{Kelari's Deception} |
| `UNDERLINE_KELARI_JUDGMENT` | `stat_underline_kelari_judgment` | Grants Skill: <underline>{Kelari's Judgment} | Grants Skill: <underline>{Kelari's Judgment} |
| `UNDERLINE_KELARI_MALEDICTION` | `stat_underline_kelari_malediction` | Grants Skill: <underline>{Kelari's Malediction} | Grants Skill: <underline>{Kelari's Malediction} |
| `UNDERLINE_KELARI_TAINTED_SANDS` | `stat_underline_kelari_tainted_sands` | Grants Skill: <underline>{Kelari, the Tainted Sands} | Grants Skill: <underline>{Kelari, the Tainted Sands} |
| `UNDERLINE_LIFE_REMNANTS` | `stat_underline_life_remnants` | Grants Skill: <underline>{Life Remnants} | Grants Skill: <underline>{Health Remnants} |
| `UNDERLINE_MANIFEST_WEAPON` | `stat_underline_manifest_weapon` | Grants Skill: <underline>{Manifest Weapon} | Grants Skill: <underline>{Manifest Weapon} |
| `UNDERLINE_MEDITATE` | `stat_underline_meditate` | Grants Skill: <underline>{Meditate} | Grants Skill: <underline>{Meditate} |
| `UNDERLINE_MIRAGE_DEADEYE` | `stat_underline_mirage_deadeye` | Grants Skill: <underline>{Mirage Deadeye} | Grants Skill: <underline>{Mirage Deadeye} |
| `UNDERLINE_MOMENT_VULNERABILITY` | `stat_underline_moment_vulnerability` | Grants Skill: <underline>{Moment of Vulnerability} | Grants Skill: <underline>{Moment of Vulnerability} |
| `UNDERLINE_NAVIRA_FRACTURING` | `stat_underline_navira_fracturing` | Grants Skill: <underline>{Navira's Fracturing} | Grants Skill: <underline>{Navira's Fracturing} |
| `UNDERLINE_NAVIRA_LAST_MIRAGE` | `stat_underline_navira_last_mirage` | Grants Skill: <underline>{Navira, the Last Mirage} | Grants Skill: <underline>{Navira, the Last Mirage} |
| `UNDERLINE_NAVIRA_OASIS` | `stat_underline_navira_oasis` | Grants Skill: <underline>{Navira's Oasis} | Grants Skill: <underline>{Navira's Oasis} |
| `UNDERLINE_NAVIRA_WELL` | `stat_underline_navira_well` | Grants Skill: <underline>{Navira's Well} | Grants Skill: <underline>{Navira's Well} |
| `UNDERLINE_PRIMAL_BOUNTY` | `stat_underline_primal_bounty` | Grants Skill: <underline>{Primal Bounty} | Grants Skill: <underline>{Primal Bounty} |
| `UNDERLINE_RITUAL_SACRIFICE` | `stat_underline_ritual_sacrifice` | Grants Skill: <underline>{Ritual Sacrifice} | Grants Skill: <underline>{Ritual Sacrifice} |
| `UNDERLINE_RUZHAN_BLAZING_SWORD` | `stat_underline_ruzhan_blazing_sword` | Grants Skill: <underline>{Ruzhan, the Blazing Sword} | Grants Skill: <underline>{Ruzhan, the Blazing Sword} |
| `UNDERLINE_RUZHAN_FURY` | `stat_underline_ruzhan_fury` | Grants Skill: <underline>{Ruzhan's Fury} | Grants Skill: <underline>{Ruzhan's Fury} |
| `UNDERLINE_RUZHAN_RECKONING` | `stat_underline_ruzhan_reckoning` | Grants Skill: <underline>{Ruzhan's Reckoning} | Grants Skill: <underline>{Ruzhan's Reckoning} |
| `UNDERLINE_RUZHAN_TRAP` | `stat_underline_ruzhan_trap` | Grants Skill: <underline>{Ruzhan's Trap} | Grants Skill: <underline>{Ruzhan's Trap} |
| `UNDERLINE_SHATTERING_CONCOCTION` | `stat_underline_shattering_concoction` | Grants Skill: <underline>{Shattering Concoction} | Grants Skill: <underline>{Shattering Concoction} |
| `UNDERLINE_SORCERY_WARD` | `stat_underline_sorcery_ward` | Grants Skill: <underline>{Sorcery Ward} | Grants Skill: <underline>{Sorcery Ward} |
| `UNDERLINE_SUMMON_INFERNAL_HOUND` | `stat_underline_summon_infernal_hound` | Grants Skill: <underline>{Summon Infernal Hound} | Grants Skill: <underline>{Summon Infernal Hound} |
| `UNDERLINE_SUPPORTING_FIRE` | `stat_underline_supporting_fire` | Grants Skill: <underline>{Supporting Fire} | Grants Skill: <underline>{Supporting Fire} |
| `UNDERLINE_TEMPER_WEAPON` | `stat_underline_temper_weapon` | Grants Skill: <underline>{Temper Weapon} | Grants Skill: <underline>{Temper Weapon} |
| `UNDERLINE_TEMPORAL_RIFT` | `stat_underline_temporal_rift` | Grants Skill: <underline>{Temporal Rift} | Grants Skill: <underline>{Temporal Rift} |
| `UNDERLINE_TIME_FREEZE` | `stat_underline_time_freeze` | Grants Skill: <underline>{Time Freeze} | Grants Skill: <underline>{Time Freeze} |
| `UNDERLINE_TIME_SNAP` | `stat_underline_time_snap` | Grants Skill: <underline>{Time Snap} | Grants Skill: <underline>{Time Snap} |
| `UNDERLINE_UNBOUND_AVATAR` | `stat_underline_unbound_avatar` | Grants Skill: <underline>{Unbound Avatar} | Grants Skill: <underline>{Unbound Avatar} |
| `UNDERLINE_VIRTUOUS_BARRIER` | `stat_underline_virtuous_barrier` | Grants Skill: <underline>{Virtuous Barrier} | Grants Skill: <underline>{Virtuous Barrier} |
| `UNDERLINE_VIVID_STAMPEDE` | `stat_underline_vivid_stampede` | Grants Skill: <underline>{Vivid Stampede} | Grants Skill: <underline>{Vivid Stampede} |
| `UNDERLINE_VOID_ILLUSION` | `stat_underline_void_illusion` | Grants Skill: <underline>{Void Illusion} | Grants Skill: <underline>{Void Illusion} |
| `UNDERLINE_WILD_PROTECTOR` | `stat_underline_wild_protector` | Grants Skill: <underline>{Wild Protector} | Grants Skill: <underline>{Wild Protector} |
| `UNHOLY_MIGHT_CHAOS` | `unholy_might_chaos` | Unholy Might Chaos | Unholy Might Chaos |
| `UNHOLY_MIGHT_COLD` | `unholy_might_cold` | Unholy Might Cold | Unholy Might Cold |
| `UNHOLY_MIGHT_FIRE` | `unholy_might_fire` | Unholy Might Fire | Unholy Might Fire |
| `UNHOLY_MIGHT_LIGHTNING` | `unholy_might_lightning` | Unholy Might Lightning | Unholy Might Lightning |
| `UNHOLY_MIGHT_PHYSICAL` | `unholy_might_physical` | Unholy Might Physical | Unholy Might Physical |
| `UNRAVELLING` | `stat_unravelling` | Grants UnravellingBuff | Grants Unravelling Buff |
| `UNWAVERING_STANCE_IMMUNE_STUN` | `unwavering_stance_immune_stun` | Cannot be Light Stunned Cannot Dodge Roll or Sprint | Cannot be Light Stunned Cannot Dodge Roll or Sprint |

---

## §A 精确命中（自动映射）

| 旧常量 | 新常量 | 模板 |
|---|---|---|
| `ADDS_LIGHTNING_DAMAGE_ATTACKS` | `ADD_X_TO_X_LIGHTNING_DAMAGE_TO_ATTACKS` | add {X} to {X} Lightning damage to Attacks |
| `ADD_ACCURACY` | `X_TO_ACCURACY` | +{X} to Accuracy |
| `ADD_AILMENT_CHANCE_BLEED` | `X_CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE` | {X}% chance to inflict Bleeding on Hit Damage |
| `ADD_AILMENT_CHANCE_MAIM` | `ATTACKS_HAVE_X_CHANCE_TO_MAIM_ON_HIT_DAMAGE` | Attacks have {X}% chance to Maim on Hit Damage |
| `ADD_AILMENT_CHANCE_POISON` | `X_CHANCE_TO_POISON_ON_HIT_DAMAGE` | {X}% chance to Poison on Hit Damage |
| `ADD_AILMENT_THRESHOLD` | `GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Ailment Threshold equal to {X}% of Max Energy Shield |
| `ADD_AILMENT_THRESHOLD_DEXTERITY` | `X_TO_AILMENT_THRESHOLD_PER_DEXTERITY` | +{X} to Ailment Threshold per Dexterity |
| `ADD_AILMENT_THRESHOLD_DEXTERITY_PER_DEXTERITY` | `X_TO_AILMENT_THRESHOLD_PER_DEXTERITY` | +{X} to Ailment Threshold per Dexterity |
| `ADD_ARMOUR` | `X_TO_ARMOR` | +{X} to Armor |
| `ADD_ATTACK_DAMAGE` | `ADD_X_TO_X_LIGHTNING_DAMAGE_TO_ATTACKS` | add {X} to {X} Lightning damage to Attacks |
| `ADD_BANNER_LINGER` | `BANNER_BUFFS_LINGER_ON_YOU_FOR_X_SECONDS_AFTER_YOU_LEAVE_THE_AREA` | Banner Buffs linger on you for {X} seconds after you leave the Area |
| `ADD_BLOCK` | `X_TO_MAX_BLOCK_CHANCE` | +{X}% to Max Block chance |
| `ADD_BLOCK_CHANCE` | `X_INCREASED_BLOCK_CHANCE` | {X}% increased Block chance |
| `ADD_CHAOS_RESISTANCE` | `X_TO_CHAOS_RESISTANCE` | +{X}% to Chaos Resistance |
| `ADD_CHAOS_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_CHAOS_RESISTANCES` | Minions have +{X}% to Chaos Resistances |
| `ADD_CHARM_CHARGES_PER_SECOND` | `CHARMS_GAIN_X_CHARGES_PER_SECOND` | Charms gain {X} charges per Second |
| `ADD_CHARM_SLOT` | `X_CHARM_SLOT` | +{X} Charm Slot |
| `ADD_CHARM_SLOTS` | `X_CHARM_SLOT` | +{X} Charm Slot |
| `ADD_COLD_RESISTANCE` | `X_TO_COLD_RESISTANCE` | +{X}% to Cold Resistance |
| `ADD_COLD_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_COLD_RESISTANCES` | Minions have +{X}% to Cold Resistances |
| `ADD_CORPSE_RETAIN_CHANCE` | `X_CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES` | {X}% chance to not destroy Corpses when Consuming Corpses |
| `ADD_CORRUPTION_ENDURES_CHANCE` | `X_CHANCE_TO_AVOID_DEATH_FROM_HIT_DAMAGE` | {X}% chance to Avoid Death from Hit Damage |
| `ADD_CRITICAL_DAMAGE_BONUS_STUNNED` | `X_TO_CRITICAL_DAMAGE_BONUS_AGAINST_STUNNED_ENEMIES` | +{X}% to Critical Damage Bonus against Stunned Enemies |
| `ADD_CRITICAL_DAMAGE_BONUS_STUNNED_AGAINST_STUNNED` | `X_TO_CRITICAL_DAMAGE_BONUS_AGAINST_STUNNED_ENEMIES` | +{X}% to Critical Damage Bonus against Stunned Enemies |
| `ADD_CRITICAL_HERALD` | `X_TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS` | +{X}% to Critical Hit Chance of Herald Skills |
| `ADD_CRIT_DAMAGE` | `X_INCREASED_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE` | {X}% increased Critical Damage Bonus for Attack Damage |
| `ADD_DEXTERITY` | `X_TO_DEXTERITY` | +{X} to Dexterity |
| `ADD_ELEMENTAL_RESISTANCES` | `X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES` | +{X}% to all Elemental Damage Resistances |
| `ADD_ELEMENTAL_RESISTANCES_RED_GREEN` | `X_TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_X_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED` | +{X}% to all Max Elemental Resistances if you have at least {X} Red, Green and Blue Support Gems Socketed |
| `ADD_EMPOWERED_ATTACKS` | `WARCRIES_HAVE_X_CHANCE_TO_EMPOWERED_X_ADDITIONAL_ATTACKS` | Warcries have {X}% chance to Empowered {X} additional Attacks |
| `ADD_EVASION` | `X_TO_EVASION` | +{X} to Evasion |
| `ADD_EXPOSURE` | `EXPOSURE_YOU_INFLICT_LOWERS_RESISTANCES_BY_AN_ADDITIONAL_X` | Exposure you inflict lowers Resistances by an additional {X}% |
| `ADD_FIRE_RESISTANCE` | `X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES` | +{X}% to all Elemental Damage Resistances |
| `ADD_FIRE_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_FIRE_RESISTANCES` | Minions have +{X}% to Fire Resistances |
| `ADD_HEALTH_REGENERATION_RATE` | `X_INCREASED_HEALTH_REGENERATION_RATE` | {X}% increased Health Regeneration rate |
| `ADD_INTELLIGENCE` | `X_TO_INTELLIGENCE` | +{X} to Intelligence |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW` | `X_SURPASS_CHANCE_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE` | +{X}% Surpass Chance chance to fire an additional Projectile |
| `ADD_LEVEL_ALL_CHAOS` | `X_TO_LEVEL_OF_ALL_CHAOS_SKILLS` | +{X} to Level of all Chaos Skills |
| `ADD_LEVEL_ALL_COLD` | `X_TO_LEVEL_OF_ALL_COLD_SKILLS` | +{X} to Level of all Cold Skills |
| `ADD_LIFE_DEXTERITY` | `X_HEALTH_PER_X_DEXTERITY` | +{X} Health per {X} Dexterity |
| `ADD_LIGHTNING_INFUSIONS` | `X_TO_MAX_LIGHTNING_INFUSIONS` | +{X} to Max Lightning Infusions |
| `ADD_LIGHTNING_RESISTANCE` | `X_TO_LIGHTNING_RESISTANCE` | +{X}% to Lightning Resistance |
| `ADD_LIGHTNING_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_LIGHTNING_RESISTANCES` | Minions have +{X}% to Lightning Resistances |
| `ADD_LIGHT_DAMAGE` | `ADD_X_TO_X_LIGHTNING_DAMAGE_TO_ATTACKS` | add {X} to {X} Lightning damage to Attacks |
| `ADD_MAX_BANNER` | `X_TO_MAX_NUMBER_OF_PLACED_BANNERS` | +{X} to Max number of placed Banners |
| `ADD_MAX_COLD_RESISTANCE` | `X_TO_MAX_COLD_RESISTANCE_IF_YOU_HAVE_AT_LEAST_X_BLUE_SUPPORT_GEMS_SOCKETED` | +{X}% to Max Cold Resistance if you have at least {X} Blue Support Gems Socketed |
| `ADD_MAX_COLD_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_MAX_COLD_RESISTANCES` | Minions have +{X}% to Max Cold Resistances |
| `ADD_MAX_ELEMENTAL_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_ALL_MAX_ELEMENTAL_RESISTANCES` | Minions have +{X}% to all Max Elemental Resistances |
| `ADD_MAX_ENDURANCE` | `X_TO_MAX_ENDURANCE_CHARGES` | +{X} to Max Endurance Charges |
| `ADD_MAX_ENERGY_SHIELD` | `GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Ailment Threshold equal to {X}% of Max Energy Shield |
| `ADD_MAX_FIRE_RESISTANCE` | `X_TO_MAX_FIRE_RESISTANCE_IF_YOU_HAVE_AT_LEAST_X_RED_SUPPORT_GEMS_SOCKETED` | +{X}% to Max Fire Resistance if you have at least {X} Red Support Gems Socketed |
| `ADD_MAX_FIRE_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_MAX_FIRE_RESISTANCES` | Minions have +{X}% to Max Fire Resistances |
| `ADD_MAX_FISSURE` | `FISSURE_SKILLS_HAVE_X_TO_LIMIT` | Fissure Skills have +{X} to Limit |
| `ADD_MAX_FRENZY` | `X_TO_MAX_FRENZY_CHARGES` | +{X} to Max Frenzy Charges |
| `ADD_MAX_HEALTH` | `X_TO_MAX_HEALTH` | +{X} to Max Health |
| `ADD_MAX_INFUSION` | `X_TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS` | +{X} to Max number of Elemental Infusions |
| `ADD_MAX_LIGHTNING_RESISTANCE` | `X_TO_MAX_LIGHTNING_RESISTANCE_IF_YOU_HAVE_AT_LEAST_X_GREEN_SUPPORT_GEMS_SOCKETED` | +{X}% to Max Lightning Resistance if you have at least {X} Green Support Gems Socketed |
| `ADD_MAX_LIGHTNING_RESISTANCE_MINION` | `MINIONS_HAVE_X_TO_MAX_LIGHTNING_RESISTANCES` | Minions have +{X}% to Max Lightning Resistances |
| `ADD_MAX_MANA` | `X_TO_MAX_MANA` | +{X} to Max Mana |
| `ADD_MAX_MINION` | `TEMPORARY_MINION_SKILLS_HAVE_X_TO_LIMIT_OF_MINIONS_SUMMONED` | Temporary Minion Skills have +{X} to Limit of Minions summoned |
| `ADD_MAX_ORB` | `ORB_SKILLS_HAVE_X_TO_LIMIT` | Orb Skills have +{X} to Limit |
| `ADD_MAX_POWER` | `X_TO_MAX_POWER_CHARGES` | +{X} to Max Power Charges |
| `ADD_MAX_RAGE` | `X_TO_MAX_RAGE` | +{X} to Max Rage |
| `ADD_MAX_RESISTANCES` | `X_TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY` | +{X}% to Max Resistances of each Elemental Damage Damage Type you have been Hit Damage with Recently |
| `ADD_MAX_VOLATILITY` | `MAX_VOLATILITY_IS_X` | Max Volatility is {X} |
| `ADD_METRES_DODGE_ROLL_DISTANCE` | `X_METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED` | +{X} metres to Dodge Roll distance while Surrounded |
| `ADD_METRES_MELEE_STRIKE_RANGE` | `X_METRES_TO_MELEE_STRIKE_RANGE` | +{X} metres to Melee Strike Range |
| `ADD_METRES_MELEE_STRIKE_RANGE_PROJECTILE` | `X_METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` | +{X} metres to Melee Strike Range if you've dealt a Projectile Attack Hit Damage in the past eight seconds |
| `ADD_NUMBER_ELEMENTAL_INFUSIONS` | `X_TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS` | +{X} to Max number of Elemental Infusions |
| `ADD_NUMBER_ELEMENTAL_INFUSIONS_ELEMENTALINFUSION` | `X_TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS` | +{X} to Max number of Elemental Infusions |
| `ADD_NUMBER_PLACED_BANNERS` | `X_TO_MAX_NUMBER_OF_PLACED_BANNERS` | +{X} to Max number of placed Banners |
| `ADD_PROJECTILE_PIERCE_CHANCE` | `X_CHANCE_TO_PIERCE_AN_ENEMY` | {X}% chance to Pierce an Enemy |
| `ADD_PUPPET_MASTER` | `X_INCREASED_MANA_COST_EFFICIENCY_OF_COMMAND_SKILLS` | {X}% increased Mana Cost Efficiency of Command Skills |
| `ADD_RAGE_VE_REQUIRES_GLORY` | `X_MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_X_SECONDS` | +{X} Max Rage if you've used a Skill that Requires Glory in the past {X} seconds |
| `ADD_RAGE_VE_REQUIRES_GLORY_REQUIRES_GLORY` | `X_MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_X_SECONDS` | +{X} Max Rage if you've used a Skill that Requires Glory in the past {X} seconds |
| `ADD_RECENTLY_CONSUMED_DAMAGE_POINTS` | `X_INCREASED_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY` | {X}% increased Damage if you have Consumed a Corpse Recently |
| `ADD_RESISTANCES_ELEMENTAL_DAMAGE_TYPE` | `X_TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY` | +{X}% to Max Resistances of each Elemental Damage Damage Type you have been Hit Damage with Recently |
| `ADD_RESISTANCES_ELEMENTAL_DAMAGE_TYPE_RECENTLY` | `X_TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY` | +{X}% to Max Resistances of each Elemental Damage Damage Type you have been Hit Damage with Recently |
| `ADD_STACKS_PUPPET_MASTER` | `X_MAX_STACKS_OF_PUPPET_MASTER` | +{X} Max stacks of Puppet Master |
| `ADD_STRENGTH` | `X_TO_STRENGTH` | +{X} to Strength |
| `ADD_STUN_THRESHOLD_DEXTERITY` | `X_TO_STUN_THRESHOLD_PER_DEXTERITY` | +{X} to Stun Threshold per Dexterity |
| `ADD_STUN_THRESHOLD_DEXTERITY_PER_DEXTERITY` | `X_TO_STUN_THRESHOLD_PER_DEXTERITY` | +{X} to Stun Threshold per Dexterity |
| `ADD_STUN_THRESHOLD_STRENGTH` | `X_TO_STUN_THRESHOLD_PER_STRENGTH` | +{X} to Stun Threshold per Strength |
| `ADD_SURPASSING_FIRE_ADDITIONAL_ARROW` | `X_SURPASS_CHANCE_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW` | +{X}% Surpass Chance chance to fire an additional Arrow |
| `ADD_SURPASSING_FIRE_ADDITIONAL_ARROW_SURPASSCHANCE` | `X_SURPASS_CHANCE_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW` | +{X}% Surpass Chance chance to fire an additional Arrow |
| `ADD_THORNS_CRITICAL` | `X_TO_THORNS_CRITICAL_HIT_CHANCE` | +{X}% to Thorns Critical Hit Chance |
| `AILMENT_MAGNITUDE_BLEED` | `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` | {X}% increased Buff Magnitude of Bleeding you inflict |
| `AILMENT_MAGNITUDE_CHILL` | `X_INCREASED_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT` | {X}% increased Buff Magnitude of Shock you inflict |
| `AILMENT_MAGNITUDE_IGNITE` | `X_INCREASED_FLAMMABILITY_BUFF_MAGNITUDE` | {X}% increased Flammability Buff Magnitude |
| `AILMENT_MAGNITUDE_POISON` | `X_INCREASED_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT` | {X}% increased Buff Magnitude of Poison you inflict |
| `AILMENT_MAGNITUDE_SHOCK` | `X_INCREASED_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT` | {X}% increased Buff Magnitude of Shock you inflict |
| `ARMOUR_APPLIES_TO_COLD` | `X_OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE` | +{X}% of Armor also applies to Cold Damage |
| `ARMOUR_APPLIES_TO_ELEMENTAL` | `X_OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE` | +{X}% of Armor also applies to Elemental Damage |
| `ARMOUR_APPLIES_TO_ELEMENTAL_SHAPESHIFTED` | `X_OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED` | +{X}% of Armor also applies to Elemental Damage while Shapeshifted |
| `ARMOUR_APPLIES_TO_FIRE` | `X_OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE` | +{X}% of Armor also applies to Fire Damage |
| `ARMOUR_APPLIES_TO_LIGHTNING` | `X_OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE` | +{X}% of Armor also applies to Lightning Damage |
| `ATTACK_DAMAGE_PENETRATES_ELEMENTAL` | `ATTACK_DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES` | Attack Damage Penetrates {X}% of Enemy Elemental Damage Resistances |
| `BALLISTA_DAMAGE` | `X_INCREASED_BALLISTA_DAMAGE` | {X}% increased Ballista damage |
| `BANNER_EXPIRES_RECOVER_GLORY` | `WHEN_A_BANNER_EXPIRES_RECOVER_X_OF_THE_GLORY_REQUIRED_FOR_THAT_BANNER` | When a Banner expires, recover {X}% of the Glory required for that Banner |
| `BANNER_EXPIRES_RECOVER_GLORY_BANNER` | `WHEN_A_BANNER_EXPIRES_RECOVER_X_OF_THE_GLORY_REQUIRED_FOR_THAT_BANNER` | When a Banner expires, recover {X}% of the Glory required for that Banner |
| `BOW_DAMAGE` | `X_INCREASED_DAMAGE_WITH_BOWS` | {X}% increased Damage with Bows |
| `CHANCE_AFTERSHOCK` | `X_INCREASED_MAX_HEALTH` | {X}% increased Max Health |
| `CHANCE_AFTERSHOCK_MACE_SLAM` | `STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_X_CHANCE_TO_DEAL_SPLASH_DAMAGE` | Strike Skills you use yourself with Maces have {X}% chance to deal Splash Damage |
| `CHANCE_AFTERSHOCK_SHAPESHIFT_SLAM` | `X_CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` | {X}% chance for Shapeshift Slam Skills you use yourself to cause an additional Aftershock |
| `CHANCE_AGGRAVATE_BLEEDING_TARGETS_ATTACKS` | `X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_ATTACKS` | {X}% chance to Aggravate Bleeding on targets you Hit Damage with Attacks |
| `CHANCE_AGGRAVATE_BLEEDING_TARGETS_ATTACKS_HITDAMAGE` | `X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_ATTACKS` | {X}% chance to Aggravate Bleeding on targets you Hit Damage with Attacks |
| `CHANCE_AGGRAVATE_BLEEDING_TARGETS_EMPOWERED` | `X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS` | {X}% chance to Aggravate Bleeding on targets you Hit Damage with Empowered Attacks |
| `CHANCE_AGGRAVATE_ON_HIT` | `EMPOWERED_ATTACKS_DEAL_X_INCREASED_DAMAGE` | Empowered Attacks deal {X}% increased Damage |
| `CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD` | `ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_X_SECONDS` | Attack Hit Damage Aggravate any Bleeding on targets which is older than {X} seconds |
| `CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED` | `X_CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND` | {X}% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground |
| `CHANCE_AGGRAVATE_ON_HIT_VS_PINNED` | `X_INCREASED_PINNED_BUILDUP` | {X}% increased Pinned Buildup |
| `CHANCE_ARCHON_UNDEATH_COMMAND` | `X_CHANCE_TO_GAIN_ARCHON_OF_UNDEATH_WHEN_YOU_USE_A_COMMAND_SKILL` | {X}% chance to gain Archon of Undeath when you use a Command skill |
| `CHANCE_ARCHON_UNDEATH_COMMAND_UNDEADARCHON` | `X_CHANCE_TO_GAIN_ARCHON_OF_UNDEATH_WHEN_YOU_USE_A_COMMAND_SKILL` | {X}% chance to gain Archon of Undeath when you use a Command skill |
| `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL` | `ATTACKS_GAIN_INCREASED_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE` | Attacks gain increased Accuracy equal to their Critical Hit Chance |
| `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL` | `ATTACKS_GAIN_INCREASED_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE` | Attacks gain increased Accuracy equal to their Critical Hit Chance |
| `CHANCE_ATTACKS_CRITICAL` | `ATTACKS_HAVE_X_TO_CRITICAL_HIT_CHANCE` | Attacks have +{X}% to Critical Hit Chance |
| `CHANCE_ATTACKS_MAIM_POISONED` | `X_CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES` | {X}% chance for Attacks to Maim on Hit Damage against Poisoned Enemies |
| `CHANCE_ATTACK_TEN_INCISION` | `X_CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION` | {X}% chance for Attack Hit Damage to apply ten Incision |
| `CHANCE_AVOID_DEATH` | `X_CHANCE_TO_AVOID_DEATH_FROM_HIT_DAMAGE` | {X}% chance to Avoid Death from Hit Damage |
| `CHANCE_BLEEDING_AGGRAVATED_INFLICTED_JAGGED` | `X_CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND` | {X}% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground |
| `CHANCE_BLEEDING_AGGRAVATED_INFLICTED_JAGGED_AGGRAVATE` | `X_CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND` | {X}% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground |
| `CHANCE_BLIND_ON_HIT` | `X_CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE` | {X}% chance to Blind Enemies on Hit Damage |
| `CHANCE_BOLTS_FIRED_CROSSBOW_ATTACKS` | `BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_X_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY` | Bolts fired by Crossbow Attacks have {X}% chance to not expend Ammunition if you've Reloaded Recently |
| `CHANCE_BOLTS_FIRED_CROSSBOW_ATTACKS_RECENTLY` | `BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_X_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY` | Bolts fired by Crossbow Attacks have {X}% chance to not expend Ammunition if you've Reloaded Recently |
| `CHANCE_BUILD_ADDITIONAL_COMBO` | `X_CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE` | {X}% Chance to build an additional Combo on Hit Damage |
| `CHANCE_BURNING_KILL_EXPLODE_DEALING` | `BURNING_ENEMIES_YOU_KILL_HAVE_A_X_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE` | Burning Enemies you kill have a {X}% chance to Explode, dealing a tenth of their Max Health as Fire Damage |
| `CHANCE_BURNING_KILL_EXPLODE_DEALING_FIRE` | `BURNING_ENEMIES_YOU_KILL_HAVE_A_X_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE` | Burning Enemies you kill have a {X}% chance to Explode, dealing a tenth of their Max Health as Fire Damage |
| `CHANCE_CHARM_ACTIVATION` | `X_CHANCE_WHEN_A_CHARM_IS_USED_TO_USE_ANOTHER_CHARM_WITHOUT_CONSUMING_CHARGES` | {X}% chance when a Charm is used to use another Charm without consuming Charges |
| `CHANCE_COLLECTING_ELEMENTAL_INFUSION_ADDITIONAL` | `X_CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE` | {X}% chance when collecting an Elemental Infusion to gain an additional Elemental Infusion of the same type |
| `CHANCE_CREATE_ADDITIONAL_REMNANT` | `X_CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT` | {X}% chance to create an additional Remnant |
| `CHANCE_DAMAGE_HITTING_UNLUCKY` | `X_CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY` | {X}% chance for Damage of Enemies Hit Damage you to be Unlucky |
| `CHANCE_DAMAGE_HITTING_UNLUCKY_HITDAMAGE` | `X_CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY` | {X}% chance for Damage of Enemies Hit Damage you to be Unlucky |
| `CHANCE_DAMAGE_LUCKY` | `X_CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY` | {X}% chance for Damage with Hit Damage to be Lucky |
| `CHANCE_DAZE_ON_HIT` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `CHANCE_DEBILITATE_ON_HIT` | `X_INCREASED_WARCRY_SPEED` | {X}% increased Warcry Speed |
| `CHANCE_DEFEND_ARMOUR` | `X_CHANCE_TO_DEFEND_WITH_X_OF_ARMOR` | {X}% chance to Defend with {X}% of Armor |
| `CHANCE_DESTROY_CORPSES` | `X_CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES` | {X}% chance to not destroy Corpses when Consuming Corpses |
| `CHANCE_ENDURANCE_CHARGE_ADDITIONAL` | `X_CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE` | {X}% chance when you gain an Endurance Charge to gain an additional Endurance Charge |
| `CHANCE_ENDURANCE_CHARGE_ADDITIONAL_CHARGES` | `X_CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE` | {X}% chance when you gain an Endurance Charge to gain an additional Endurance Charge |
| `CHANCE_FLASK_NO_CONSUME` | `X_CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES` | {X}% chance for Flasks you use to not consume Charges |
| `CHANCE_FRENZY_CHARGE_ADDITIONAL` | `X_CHANCE_WHEN_YOU_GAIN_A_FRENZY_CHARGE_TO_GAIN_AN_ADDITIONAL_FRENZY_CHARGE` | {X}% chance when you gain a Frenzy Charge to gain an additional Frenzy Charge |
| `CHANCE_FRENZY_CHARGE_ADDITIONAL_CHARGES` | `X_CHANCE_WHEN_YOU_GAIN_A_FRENZY_CHARGE_TO_GAIN_AN_ADDITIONAL_FRENZY_CHARGE` | {X}% chance when you gain a Frenzy Charge to gain an additional Frenzy Charge |
| `CHANCE_GAIN_ONSLAUGHT_ON_KILL` | `X_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILLING_BLOW_WITH_AXES` | {X}% chance to gain Onslaught on Killing Blow with Axes |
| `CHANCE_GAIN_PUPPET_MASTER` | `X_SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL` | {X}% Surpassing Chance to gain a Puppet Master stack whenever you use a Command Skill |
| `CHANCE_GAIN_VOLATILITY` | `X_CHANCE_TO_GAIN_VOLATILITY_ON_KILL` | {X}% chance to gain Volatility on Kill |
| `CHANCE_GLORY` | `SKILLS_HAVE_A_X_CHANCE_TO_NOT_CONSUME_GLORY` | Skills have a {X}% chance to not consume Glory |
| `CHANCE_GRENADES_ACTIVATE_TIME` | `GRENADES_HAVE_X_CHANCE_TO_ACTIVATE_A_SECOND_TIME` | Grenades have {X}% chance to activate a second time |
| `CHANCE_HAZARDS_REARM_THEY_TRIGGERED` | `HAZARDS_HAVE_X_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED` | Hazards have {X}% chance to rearm after they are triggered |
| `CHANCE_HAZARDS_REARM_THEY_TRIGGERED_HAZARD` | `HAZARDS_HAVE_X_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED` | Hazards have {X}% chance to rearm after they are triggered |
| `CHANCE_HINDER_ON_HIT` | `X_CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS` | {X}% chance to Hinder Enemies on Hit with Spells |
| `CHANCE_IMPALE_ON_HIT` | `X_CHANCE_TO_IMPALE_ON_SPELL_HIT_DAMAGE` | {X}% chance to Impale on Spell Hit Damage |
| `CHANCE_INCISION_ON_HIT` | `X_CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION` | {X}% chance for Attack Hit Damage to apply Incision |
| `CHANCE_INFLICT_DAZE_FURTHER_THAN` | `X_CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM` | {X}% chance to inflict Daze with Hit Damage against Enemies further than {X}m |
| `CHANCE_INFLICT_DAZE_FURTHER_THAN_HITDAMAGE` | `X_CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM` | {X}% chance to inflict Daze with Hit Damage against Enemies further than {X}m |
| `CHANCE_INVOCATED_SPELLS_HALF_MUCH` | `INVOCATED_SPELLS_HAVE_X_CHANCE_TO_CONSUME_HALF_AS_MUCH_ENERGY` | Invocated Spells have {X}% chance to consume half as much Energy |
| `CHANCE_INVOCATED_SPELLS_HALF_MUCH_SPELL` | `INVOCATED_SPELLS_HAVE_X_CHANCE_TO_CONSUME_HALF_AS_MUCH_ENERGY` | Invocated Spells have {X}% chance to consume half as much Energy |
| `CHANCE_KILL_EMPOWERED_ATTACKS_EXPLODE` | `ENEMIES_YOU_KILL_WITH_EMPOWERED_ATTACKS_HAVE_A_X_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE` | Enemies you kill with Empowered Attacks have a {X}% chance to Explode, dealing a tenth of their Max Health as Fire Damage |
| `CHANCE_KILL_EMPOWERED_ATTACKS_EXPLODE_ATTACK` | `ENEMIES_YOU_KILL_WITH_EMPOWERED_ATTACKS_HAVE_A_X_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE` | Enemies you kill with Empowered Attacks have a {X}% chance to Explode, dealing a tenth of their Max Health as Fire Damage |
| `CHANCE_KNOCKBACK` | `X_CHANCE_TO_KNOCK_ENEMIES_BACK_WITH_HIT_DAMAGE_AT_CLOSE_RANGE` | {X}% chance to Knock Enemies Back with Hit Damage at Close Range |
| `CHANCE_KNOCK_BACK_BLEEDING` | `X_CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE` | {X}% chance to Knock Back Bleeding Enemies with Hit Damage |
| `CHANCE_KNOCK_BACK_BLEEDING_KNOCKBACK` | `X_CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE` | {X}% chance to Knock Back Bleeding Enemies with Hit Damage |
| `CHANCE_LIGHTNING_DAMAGE_LUCKY` | `X_CHANCE_FOR_LIGHTNING_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY` | {X}% chance for Lightning Damage with Hit Damage to be Lucky |
| `CHANCE_LIGHTNING_DAMAGE_LUCKY_HITDAMAGE` | `X_CHANCE_FOR_LIGHTNING_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY` | {X}% chance for Lightning Damage with Hit Damage to be Lucky |
| `CHANCE_MACE_SLAM_CAUSE_ADDITIONAL` | `X_CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` | {X}% chance for Mace Slam Skills you use yourself to cause an additional Aftershock |
| `CHANCE_MINIONS_CRITICAL` | `MINIONS_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE` | Minions have {X}% increased Critical Hit Chance |
| `CHANCE_MINIONS_INFLICT_WITHERED` | `MINIONS_HAVE_X_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE` | Minions have {X}% chance to inflict Withered on Hit Damage |
| `CHANCE_ONSLAUGHT_ON_KILL` | `X_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILLING_BLOW_WITH_AXES` | {X}% chance to gain Onslaught on Killing Blow with Axes |
| `CHANCE_PLANTS_IMMEDIATELY_OVERGROW` | `PLANTS_HAVE_A_X_CHANCE_TO_IMMEDIATELY_OVERGROW` | Plants have a {X}% chance to immediately Overgrow |
| `CHANCE_POWER_CHARGE_ADDITIONAL` | `X_CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE` | {X}% chance when you gain a Power Charge to gain an additional Power Charge |
| `CHANCE_POWER_CHARGE_ADDITIONAL_CHARGES` | `X_CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE` | {X}% chance when you gain a Power Charge to gain an additional Power Charge |
| `CHANCE_PROJECTILES_ADDITIONAL_PROJECTILE_FORKING` | `PROJECTILES_HAVE_X_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING` | Projectiles have {X}% chance for an additional Projectile when Forking |
| `CHANCE_PROJECTILES_ADDITIONAL_PROJECTILE_FORKING_FORK` | `PROJECTILES_HAVE_X_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING` | Projectiles have {X}% chance for an additional Projectile when Forking |
| `CHANCE_PROJECTILES_FORK_VE_DEALT` | `PROJECTILES_HAVE_X_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` | Projectiles have {X}% chance to Fork if you've dealt a Melee Hit Damage in the past eight seconds |
| `CHANCE_PROJECTILES_FORK_VE_DEALT_PROJECTILE` | `PROJECTILES_HAVE_X_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` | Projectiles have {X}% chance to Fork if you've dealt a Melee Hit Damage in the past eight seconds |
| `CHANCE_PROJECTILE_ATTACKS_FIRE_TWO` | `PROJECTILE_ATTACKS_HAVE_A_X_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING` | Projectile Attacks have a {X}% chance to fire two additional Projectiles while moving |
| `CHANCE_PROJECTILE_ATTACKS_FIRE_TWO_WHILE_MOVING` | `PROJECTILE_ATTACKS_HAVE_A_X_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING` | Projectile Attacks have a {X}% chance to fire two additional Projectiles while moving |
| `CHANCE_RECOVER_ALL_LIFE_KILL` | `X_CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY` | {X}% chance to Recover all Health when you Kill an Enemy |
| `CHANCE_RELOAD_CROSSBOW_IMMEDIATE` | `X_CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE` | {X}% chance when you Reload a Crossbow to be immediate |
| `CHANCE_REMNANTS_CREATE_EFFECTS_TWICE` | `X_CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE` | {X}% chance for Remnants you create to grant their effects twice |
| `CHANCE_REMNANTS_CREATE_EFFECTS_TWICE_GRANT_EFFECTS` | `X_CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE` | {X}% chance for Remnants you create to grant their effects twice |
| `CHANCE_REMNANT_DOUBLE_EFFECT` | `X_CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE` | {X}% chance for Remnants you create to grant their effects twice |
| `CHANCE_REMOVE_CHARGES_BUT_STILL` | `SKILLS_HAVE_X_CHANCE_TO_NOT_ADD_CHARGES_BUT_STILL_COUNT_AS_CONSUMING_THEM` | Skills have {X}% chance to not add Charges but still count as consuming them |
| `CHANCE_REMOVE_CHARGES_BUT_STILL_CHARGE` | `SKILLS_HAVE_X_CHANCE_TO_NOT_ADD_CHARGES_BUT_STILL_COUNT_AS_CONSUMING_THEM` | Skills have {X}% chance to not add Charges but still count as consuming them |
| `CHANCE_REMOVE_ELEMENTAL_INFUSIONS_BUT` | `SKILLS_HAVE_X_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM` | Skills have {X}% chance to not add Elemental Infusions but still count as consuming them |
| `CHANCE_SHAPESHIFT_SLAM_CAUSE_ADDITIONAL` | `X_CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` | {X}% chance for Shapeshift Slam Skills you use yourself to cause an additional Aftershock |
| `CHANCE_SHOCK` | `X_INCREASED_CHANCE_TO_SHOCK` | {X}% increased chance to Shock |
| `CHANCE_SHOCKING_SHOCK_IN_METRE` | `SHOCKING_HIT_DAMAGE_HAVE_A_X_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_X_METRE_RADIUS` | Shocking Hit Damage have a {X}% chance to also Shock enemies in a {X} metre radius |
| `CHANCE_SHOCKING_SHOCK_IN_METRE_HITDAMAGE` | `SHOCKING_HIT_DAMAGE_HAVE_A_X_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_X_METRE_RADIUS` | Shocking Hit Damage have a {X}% chance to also Shock enemies in a {X} metre radius |
| `CHANCE_SHOCK_ELECTROCUTED` | `X_INCREASED_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES` | {X}% increased Shock Chance against Electrocuted Enemies |
| `CHANCE_SHOCK_REAPPLY_IT` | `X_CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT` | {X}% chance on Consuming a Shock on an Enemy to reapply it |
| `CHANCE_SHOCK_REAPPLY_IT_CONSUME` | `X_CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT` | {X}% chance on Consuming a Shock on an Enemy to reapply it |
| `CHANCE_SLAM_CAUSE_ADDITIONAL_AFTERSHOCK_SELF` | `X_INCREASED_MAX_HEALTH` | {X}% increased Max Health |
| `CHANCE_SPELL_FIRE_ADDITIONAL_PROJECTILES` | `X_CHANCE_FOR_SPELL_SKILLS_TO_FIRE_X_ADDITIONAL_PROJECTILES` | {X}% chance for Spell Skills to fire {X} additional Projectiles |
| `CHANCE_STRIKE_MACES_SPLASH_DAMAGE` | `STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_X_CHANCE_TO_DEAL_SPLASH_DAMAGE` | Strike Skills you use yourself with Maces have {X}% chance to deal Splash Damage |
| `CHANCE_STRIKE_MACES_SPLASH_DAMAGE_MACE` | `STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_X_CHANCE_TO_DEAL_SPLASH_DAMAGE` | Strike Skills you use yourself with Maces have {X}% chance to deal Splash Damage |
| `CHANCE_THORNS_DAMAGE_IGNORE_ARMOUR` | `THORNS_DAMAGE_HAS_X_CHANCE_TO_IGNORE_ENEMY_ARMOR` | Thorns Damage has {X}% chance to ignore Enemy Armor |
| `CHANCE_TREAT_MONSTER_ELEMENTAL_RESISTANCE` | `HIT_DAMAGE_HAVE_X_CHANCE_TO_TREAT_ENEMY_MONSTER_ELEMENTAL_DAMAGE_RESISTANCES_VALUES_AS_INVERTED` | Hit Damage have {X}% chance to treat Enemy Monster Elemental Damage Resistances values as inverted |
| `CHANCE_TRIGGER_REFUND_HALF_ENERGY` | `X_CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT` | {X}% chance for Trigger skills to refund half of Energy Spent |
| `CHANCE_TRIGGER_REFUND_HALF_ENERGY_SHOCK` | `X_CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT` | {X}% chance for Trigger skills to refund half of Energy Spent |
| `CHANCE_VOLATILITY_EXPLODES_REGAIN_EQUIVALENT` | `X_CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY` | {X}% chance that when Volatility on you explodes, you regain an equivalent amount of Volatility |
| `CHANCE_VOLATILITY_EXPLODES_REGAIN_EQUIVALENT_VOLATILITY` | `X_CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY` | {X}% chance that when Volatility on you explodes, you regain an equivalent amount of Volatility |
| `CHANCE_WARCRIES_EMPOWER_ADDITIONAL_ATTACKS` | `WARCRIES_HAVE_X_CHANCE_TO_EMPOWERED_X_ADDITIONAL_ATTACKS` | Warcries have {X}% chance to Empowered {X} additional Attacks |
| `CHANCE_WARCRIES_EMPOWER_ADDITIONAL_ATTACKS_EMPOWERED` | `WARCRIES_HAVE_X_CHANCE_TO_EMPOWERED_X_ADDITIONAL_ATTACKS` | Warcries have {X}% chance to Empowered {X} additional Attacks |
| `CHANCE_WHICH_CREATE_FISSURES_ADDITIONAL` | `SKILLS_WHICH_CREATE_FISSURES_HAVE_A_X_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE` | Skills which create Fissures have a {X}% chance to create an additional Fissure |
| `CHANCE_WOULD_ENDURANCE_CHARGES_UP` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES` | {X}% chance that if you would gain Endurance Charges, you instead gain up to Max Endurance Charges |
| `CHANCE_WOULD_ENDURANCE_CHARGES_UP_CHARGE` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES` | {X}% chance that if you would gain Endurance Charges, you instead gain up to Max Endurance Charges |
| `CHANCE_WOULD_FRENZY_CHARGES_UP` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES` | {X}% chance that if you would gain Frenzy Charges, you instead gain up to your Max number of Frenzy Charges |
| `CHANCE_WOULD_FRENZY_CHARGES_UP_CHARGE` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES` | {X}% chance that if you would gain Frenzy Charges, you instead gain up to your Max number of Frenzy Charges |
| `CHANCE_WOULD_POWER_CHARGES_UP` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES` | {X}% chance that if you would gain Power Charges, you instead gain up to your Max number of Power Charges |
| `CHANCE_WOULD_POWER_CHARGES_UP_CHARGE` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES` | {X}% chance that if you would gain Power Charges, you instead gain up to your Max number of Power Charges |
| `CHANCE_WOULD_RAGE_UP` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE` | {X}% chance that if you would gain Rage on Hit Damage, you instead gain up to your Max Rage |
| `CHANCE_WOULD_RAGE_UP_HITDAMAGE` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE` | {X}% chance that if you would gain Rage on Hit Damage, you instead gain up to your Max Rage |
| `CHARGE_CONVERT_CONDUIT` | `IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD` | If you would gain a Charges, Allies in your Presence gain that Charges instead |
| `CHARGE_CONVERT_GAIN_MAX_ENDURANCE` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES` | {X}% chance that if you would gain Endurance Charges, you instead gain up to Max Endurance Charges |
| `CHARGE_CONVERT_GAIN_MAX_FRENZY` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES` | {X}% chance that if you would gain Frenzy Charges, you instead gain up to your Max number of Frenzy Charges |
| `CHARGE_CONVERT_GAIN_MAX_POWER` | `X_CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES` | {X}% chance that if you would gain Power Charges, you instead gain up to your Max number of Power Charges |
| `COMPANION_ATTACK_SPEED` | `COMPANIONS_HAVE_X_INCREASED_ATTACK_SPEED` | Companions have {X}% increased Attack Speed |
| `CONVERTS_ALL_EVASION_RATING` | `STAT_CONVERSION_ALL_EVASION_TO_ARMOR` | Stat Conversion all Evasion to Armor |
| `CONVERT_EXTRA_COLD_TO_LIGHTNING` | `GAIN_X_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` | gain {X}% of Cold damage as Extra Lightning damage |
| `CONVERT_EXTRA_GENERIC_TO_CHAOS` | `ATTACKS_GAIN_X_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` | Attacks gain {X}% of Damage as extra Chaos Damage |
| `CONVERT_EXTRA_GENERIC_TO_COLD` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `CONVERT_EXTRA_GENERIC_TO_FIRE` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FOR_EVERY_DIFFERENT_GRENADE_FIRED_IN_THE_PAST_X_SECONDS` | gain {X}% of Damage as Extra Fire Damage for every different Grenade fired in the past {X} seconds |
| `CONVERT_EXTRA_GENERIC_TO_LIGHTNING` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `CONVERT_EXTRA_GENERIC_TO_PHYSICAL` | `GAIN_X_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE` | gain {X}% of Damage as Extra Physical Damage |
| `CONVERT_EXTRA_LIGHTNING_TO_COLD` | `GAIN_X_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE` | gain {X}% of Lightning damage as Extra Cold damage |
| `CONVERT_EXTRA_PHYS_TO_COLD` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `CONVERT_EXTRA_PHYS_TO_LIGHTNING` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `CURSE_AREA` | `X_INCREASED_AREA_OF_EFFECT_OF_CURSES` | {X}% increased Area of Effect of Curses |
| `DAMAGE_LUCKY` | `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` | Damage with Hits is Lucky against Enemies that are on Low Health |
| `DAMAGE_LUCKY_VS_LOW_LIFE` | `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` | Damage with Hits is Lucky against Enemies that are on Low Health |
| `DAMAGE_PENETRATES_ELEMENTAL_RESISTANCES` | `DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_RESISTANCES` | Damage Penetrates {X}% of Enemy Elemental Resistances |
| `DAMAGE_PENETRATES_ELEMENTAL_RESISTANCES_PENETRATES` | `DAMAGE_PENETRATION_X_ELEMENTAL_DAMAGE_RESISTANCES` | Damage Penetration {X}% Elemental Damage Resistances |
| `DAMAGE_PENETRATES_ELEMENTAL_RESISTANCES_REQUIRES_GLORY` | `DAMAGE_PENETRATES_X_ELEMENTAL_DAMAGE_RESISTANCES_FOR_EACH_TIME_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_X_SECONDS` | Damage Penetrates {X}% Elemental Damage Resistances for each time you've used a Skill that Requires Glory in the past {X} seconds |
| `DAMAGE_PENETRATES_ELEMENTAL_RESISTANCES_SHAPESHIFTED` | `DAMAGE_PENETRATION_X_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED` | Damage Penetration {X}% of Enemy Elemental Damage Resistances while Shapeshifted |
| `DAMAGE_TAKEN_ON_JAGGED` | `ENEMIES_IN_JAGGED_GROUND_YOU_CREATE_TAKE_X_INCREASED_DAMAGE` | Enemies in Jagged Ground you create take {X}% increased Damage |
| `DEC_RAGE_LOSS_RATE` | `INHERENT_LOSS_OF_RAGE_IS_X_SLOWER` | Inherent loss of Rage is {X}% slower |
| `DELIVERY_CHAIN` | `PROJECTILES_HAVE_X_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN` | Projectiles have {X}% chance to Chain an additional time from terrain |
| `DELIVERY_EXTRA_PROJECTILE` | `X_SURPASS_CHANCE_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE` | +{X}% Surpass Chance chance to fire an additional Projectile |
| `DELIVERY_EXTRA_PROJECTILE_GRENADE` | `GRENADE_SKILLS_FIRE_AN_ADDITIONAL_PROJECTILE` | Grenade Skills Fire an additional Projectile |
| `DELIVERY_EXTRA_PROJECTILE_SPELL` | `X_CHANCE_FOR_SPELL_SKILLS_TO_FIRE_X_ADDITIONAL_PROJECTILES` | {X}% chance for Spell Skills to fire {X} additional Projectiles |
| `DELIVERY_FORK` | `PROJECTILES_HAVE_X_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING` | Projectiles have {X}% chance for an additional Projectile when Forking |
| `EXTRA_CHAOS` | `GAIN_X_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` | gain {X}% of Damage as Extra Chaos Damage |
| `EXTRA_CHAOS_FROM_CHAOS` | `COMPANIONS_GAIN_X_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` | Companions gain {X}% Damage as extra Chaos Damage |
| `EXTRA_COLD` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE` | gain {X}% of Damage as Extra Cold Damage |
| `EXTRA_COLD_FROM_CHAOS` | `COMPANIONS_GAIN_X_DAMAGE_AS_EXTRA_COLD_DAMAGE` | Companions gain {X}% Damage as extra Cold Damage |
| `EXTRA_COLD_FROM_CHAOS_ON_CHILLED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `EXTRA_COLD_FROM_CHAOS_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Cold Damage while Shapeshifted |
| `EXTRA_COLD_FROM_CHAOS_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_COLD_FROM_CHAOS_VS_SHOCK` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_COLD_FROM_COLD_ON_CHILLED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `EXTRA_COLD_FROM_COLD_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Cold Damage while Shapeshifted |
| `EXTRA_COLD_FROM_COLD_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_COLD_FROM_COLD_VS_SHOCK` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_COLD_FROM_FIRE_ON_CHILLED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `EXTRA_COLD_FROM_FIRE_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Cold Damage while Shapeshifted |
| `EXTRA_COLD_FROM_FIRE_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_COLD_FROM_FIRE_VS_SHOCK` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_COLD_FROM_LIGHTNING_ON_CHILLED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `EXTRA_COLD_FROM_LIGHTNING_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Cold Damage while Shapeshifted |
| `EXTRA_COLD_FROM_LIGHTNING_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_COLD_FROM_LIGHTNING_VS_SHOCK` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_COLD_FROM_PHYSICAL_ON_CHILLED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_ON_CHILLED_GROUND` | gain {X}% of Damage as Extra Cold Damage while on Chilled Ground |
| `EXTRA_COLD_FROM_PHYSICAL_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Cold Damage while Shapeshifted |
| `EXTRA_COLD_FROM_PHYSICAL_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_COLD_FROM_PHYSICAL_VS_SHOCK` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_FIRE` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE` | gain {X}% of Damage as Extra Fire Damage |
| `EXTRA_FIRE_FROM_CHAOS_ON_IGNITED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `EXTRA_FIRE_FROM_CHAOS_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Fire Damage while Shapeshifted |
| `EXTRA_FIRE_FROM_CHAOS_VS_FREEZE` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `EXTRA_FIRE_FROM_CHAOS_VS_HEAVY_STUN` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_FIRE_FROM_COLD_ON_IGNITED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `EXTRA_FIRE_FROM_COLD_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Fire Damage while Shapeshifted |
| `EXTRA_FIRE_FROM_COLD_VS_FREEZE` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `EXTRA_FIRE_FROM_COLD_VS_HEAVY_STUN` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_FIRE_FROM_FIRE` | `GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE` | gain {X}% of Elemental Damage as Extra Fire Damage |
| `EXTRA_FIRE_FROM_FIRE_ON_IGNITED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `EXTRA_FIRE_FROM_FIRE_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Fire Damage while Shapeshifted |
| `EXTRA_FIRE_FROM_FIRE_VS_FREEZE` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `EXTRA_FIRE_FROM_FIRE_VS_HEAVY_STUN` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_FIRE_FROM_LIGHTNING_ON_IGNITED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `EXTRA_FIRE_FROM_LIGHTNING_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Fire Damage while Shapeshifted |
| `EXTRA_FIRE_FROM_LIGHTNING_VS_FREEZE` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `EXTRA_FIRE_FROM_LIGHTNING_VS_HEAVY_STUN` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_FIRE_FROM_PHYSICAL` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_FIRE_FROM_PHYSICAL_ON_IGNITED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `EXTRA_FIRE_FROM_PHYSICAL_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Fire Damage while Shapeshifted |
| `EXTRA_FIRE_FROM_PHYSICAL_VS_FREEZE` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `EXTRA_FIRE_FROM_PHYSICAL_VS_HEAVY_STUN` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` | gain {X}% of Physical Damage as Extra Fire Damage against Heavy Stunned Enemies |
| `EXTRA_LIGHTNING` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` | gain {X}% of Damage as Extra Lightning Damage |
| `EXTRA_LIGHTNING_FROM_CHAOS_ON_SHOCKED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `EXTRA_LIGHTNING_FROM_CHAOS_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Lightning Damage while Shapeshifted |
| `EXTRA_LIGHTNING_FROM_CHAOS_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_LIGHTNING_FROM_CHAOS_VS_ELECTROCUTE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_CHAOS_VS_FREEZE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_LIGHTNING_FROM_COLD` | `GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` | gain {X}% of Elemental Damage as Extra Lightning Damage |
| `EXTRA_LIGHTNING_FROM_COLD_ON_SHOCKED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `EXTRA_LIGHTNING_FROM_COLD_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Lightning Damage while Shapeshifted |
| `EXTRA_LIGHTNING_FROM_COLD_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_LIGHTNING_FROM_COLD_VS_ELECTROCUTE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_COLD_VS_FREEZE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_LIGHTNING_FROM_FIRE` | `GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` | gain {X}% of Elemental Damage as Extra Lightning Damage |
| `EXTRA_LIGHTNING_FROM_FIRE_ON_SHOCKED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `EXTRA_LIGHTNING_FROM_FIRE_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Lightning Damage while Shapeshifted |
| `EXTRA_LIGHTNING_FROM_FIRE_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_LIGHTNING_FROM_FIRE_VS_ELECTROCUTE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_FIRE_VS_FREEZE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_LIGHTNING_FROM_LIGHTNING_ON_SHOCKED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `EXTRA_LIGHTNING_FROM_LIGHTNING_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Lightning Damage while Shapeshifted |
| `EXTRA_LIGHTNING_FROM_LIGHTNING_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_LIGHTNING_FROM_LIGHTNING_VS_ELECTROCUTE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_LIGHTNING_VS_FREEZE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_LIGHTNING_FROM_PHYSICAL` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_PHYSICAL_ON_SHOCKED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND` | gain {X}% of Damage as Extra Lightning Damage while on Shocked Ground |
| `EXTRA_LIGHTNING_FROM_PHYSICAL_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Lightning Damage while Shapeshifted |
| `EXTRA_LIGHTNING_FROM_PHYSICAL_VS_DAZE` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `EXTRA_LIGHTNING_FROM_PHYSICAL_VS_ELECTROCUTE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Electrocuted Enemies |
| `EXTRA_LIGHTNING_FROM_PHYSICAL_VS_FREEZE` | `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES` | gain {X}% of Physical Damage as Extra Lightning Damage against Chilled Enemies |
| `EXTRA_PHYSICAL` | `GAIN_X_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE` | gain {X}% of Damage as Extra Physical Damage |
| `EXTRA_RANDOM_ELEMENT_RANDOM_SHAPESHIFTED` | `GAIN_X_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Damage of a random Elemental Damage while Shapeshifted |
| `FLASK_MANA_RECOVERY` | `X_INCREASED_MANA_RECOVERY_FROM_FLASKS` | {X}% increased Mana Recovery from Flasks |
| `FLASK_NO_CONSUME` | `X_CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES` | {X}% chance for Flasks you use to not consume Charges |
| `GRANT_CHARGE_ON_GAIN` | `X_CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE` | {X}% chance when collecting an Elemental Infusion to gain an additional Elemental Infusion of the same type |
| `GRANT_COMBO_ON_HIT` | `X_CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE` | {X}% Chance to build an additional Combo on Hit Damage |
| `GRANT_ENDURANCE_CHARGE_ON_GAIN` | `X_TO_MAX_ENDURANCE_CHARGES` | +{X} to Max Endurance Charges |
| `GRANT_ENERGY_ON_VOLATILITY_DETONATE` | `X_CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY` | {X}% chance that when Volatility on you explodes, you regain an equivalent amount of Volatility |
| `GRANT_ENERGY_REFUND` | `X_CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT` | {X}% chance for Trigger skills to refund half of Energy Spent |
| `GRANT_FRENZY_CHARGE_ON_CONSUME_POWER` | `QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE` | Quarterstaff Skills that consume Power Charges count as consuming an additional Power Charge |
| `GRANT_FRENZY_CHARGE_ON_GAIN` | `X_TO_MAX_FRENZY_CHARGES` | +{X} to Max Frenzy Charges |
| `GRANT_INTIMIDATE_ON_HEAVY_STUN` | `ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_X_SECONDS` | Enemies you Heavy Stun while Shapeshifted are Intimidated for {X} seconds |
| `GRANT_POWER_CHARGE_ON_GAIN` | `X_TO_MAX_POWER_CHARGES` | +{X} to Max Power Charges |
| `GRANT_VOLATILITY_ON_KILL` | `X_CHANCE_TO_GAIN_VOLATILITY_ON_KILL` | {X}% chance to gain Volatility on Kill |
| `GRENADE_DAMAGE` | `X_INCREASED_GRENADE_DAMAGE` | {X}% increased Grenade Damage |
| `HAZARD_DAMAGE` | `X_INCREASED_HAZARD_DAMAGE` | {X}% increased Hazard Damage |
| `HERALD_DAMAGE` | `HERALD_SKILLS_DEAL_X_INCREASED_DAMAGE` | Herald Skills deal {X}% increased Damage |
| `IMMUNE_BLEEDING` | `IMMUNE_TO_BLEEDING_IF_EQUIPPED_HELMET_HAS_HIGHER_ARMOR_THAN_EVASION` | Immune to Bleeding if Equipped Helmet has higher Armor than Evasion |
| `IMMUNE_CHILL` | `IMMUNE_TO_CHILL_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_BLUE` | Immune to Chill if a majority of your Socketed Support Gems are Blue |
| `IMMUNE_CORRUPTED_BLOOD` | `IMMUNE_TO_CORRUPTED_BLOOD` | Immune to Corrupted Blood |
| `IMMUNE_EXPOSURE` | `IMMUNE_TO_EXPOSURE` | Immune to Exposure |
| `IMMUNE_FREEZE_CHILL` | `IMMUNE_TO_FREEZE_AND_CHILL_WHILE_AFFECTED_BY_AN_ARCHON_BUFF` | Immune to Freeze and Chill while affected by an Archon Buff |
| `IMMUNE_HINDER` | `IMMUNE_TO_HINDER` | Immune to Hinder |
| `IMMUNE_IGNITE_MAJORITY_SOCKETED` | `IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED` | Immune to Ignite if a majority of your Socketed Support Gems are Red |
| `IMMUNE_MAIM` | `IMMUNE_TO_MAIM` | Immune to Maim |
| `IMMUNE_MAIM_SHAPESHIFTED` | `IMMUNE_TO_MAIM_WHILE_SHAPESHIFTED` | Immune to Maim while Shapeshifted |
| `IMMUNE_POISON` | `IMMUNE_TO_POISON_IF_EQUIPPED_HELMET_HAS_HIGHER_EVASION_THAN_ARMOR` | Immune to Poison if Equipped Helmet has higher Evasion than Armor |
| `IMMUNE_SHOCK` | `IMMUNE_TO_SHOCK_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_GREEN` | Immune to Shock if a majority of your Socketed Support Gems are Green |
| `IMMUNE_STUN` | `YOUR_HIT_DAMAGE_CANNOT_BE_EVASION_BY_HEAVY_STUNNED_ENEMIES` | Your Hit Damage cannot be Evasion by Heavy Stunned Enemies |
| `INC_ACCURACY` | `X_INCREASED_ACCURACY` | {X}% increased Accuracy |
| `INC_ACCURACY_RATING_EQUAL_INTELLIGENCE` | `GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE` | gain Accuracy equal to your Intelligence |
| `INC_ACCURACY_RATING_EQUAL_STRENGTH` | `GAIN_ACCURACY_EQUAL_TO_YOUR_STRENGTH` | gain Accuracy equal to your Strength |
| `INC_ADDITIONAL_AILMENT_THRESHOLD_EQUAL` | `GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Ailment Threshold equal to {X}% of Max Energy Shield |
| `INC_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_AILMENTTHRESHOLD` | `GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Ailment Threshold equal to {X}% of Max Energy Shield |
| `INC_ADDITIONAL_CURSE` | `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` | You can apply an additional Curse |
| `INC_ADDITIONAL_STUN_THRESHOLD_EQUAL` | `GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Stun Threshold equal to {X}% of Max Energy Shield |
| `INC_ADDITIONAL_STUN_THRESHOLD_EQUAL_ENERGYSHIELD` | `GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_X_OF_MAX_ENERGY_SHIELD` | gain additional Stun Threshold equal to {X}% of Max Energy Shield |
| `INC_ADDITIONAL_STUN_THRESHOLD_EQUAL_STUNTHRESHOLD` | `GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_X_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS` | gain additional Stun Threshold equal to {X}% of Item Armor on Equipped Armor Items |
| `INC_AFTERSHOCK_AREA` | `SLAM_SKILLS_YOU_USE_YOURSELF_HAVE_X_INCREASED_AFTERSHOCK_AREA_OF_EFFECT` | Slam Skills you use yourself have {X}% increased Aftershock Area of Effect |
| `INC_AILMENT_BUILDUP` | `X_INCREASED_FREEZE_BUILDUP_WITH_EMPOWERED_ATTACKS` | {X}% increased Freeze Buildup with Empowered Attacks |
| `INC_AILMENT_CHANCE` | `X_INCREASED_CHANCE_TO_INFLICT_AILMENTS` | {X}% increased chance to inflict Ailments |
| `INC_AILMENT_CHANCE_ONE_HANDED` | `ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_X_INCREASED_CHANCE_TO_INFLICT_AILMENTS` | Attacks with One-Handed Martial Weapon have {X}% increased Chance to inflict Ailments |
| `INC_AILMENT_CHANCE_VS_EXPOSED_COLD` | `DAMAGE_PENETRATION_X_COLD_RESISTANCE` | Damage Penetration {X}% Cold Resistance |
| `INC_AILMENT_CHANCE_VS_EXPOSED_FIRE` | `DAMAGE_PENETRATION_X_COLD_RESISTANCE` | Damage Penetration {X}% Cold Resistance |
| `INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING` | `DAMAGE_PENETRATION_X_COLD_RESISTANCE` | Damage Penetration {X}% Cold Resistance |
| `INC_AILMENT_DURATION` | `X_INCREASED_SKILL_EFFECT_DURATION` | {X}% increased Skill Effect Duration |
| `INC_AILMENT_DURATION_TAKEN` | `X_INCREASED_DURATION_OF_AILMENTS_ON_YOU` | {X}% increased Duration of Ailments on You |
| `INC_AILMENT_DURATION_VS_EXPOSED_COLD` | `DAMAGE_PENETRATION_X_FIRE_RESISTANCE` | Damage Penetration {X}% Fire Resistance |
| `INC_AILMENT_DURATION_VS_EXPOSED_FIRE` | `DAMAGE_PENETRATION_X_FIRE_RESISTANCE` | Damage Penetration {X}% Fire Resistance |
| `INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING` | `DAMAGE_PENETRATION_X_FIRE_RESISTANCE` | Damage Penetration {X}% Fire Resistance |
| `INC_AILMENT_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT` | {X}% increased Buff Magnitude of Ailments you inflict |
| `INC_AILMENT_THRESHOLD_EQUAL_LOWEST` | `GAIN_AILMENT_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_BOOTS` | gain Ailment Threshold equal to the lowest of Evasion and Armor on your Boots |
| `INC_AILMENT_THRESHOLD_EQUAL_LOWEST_AILMENTTHRESHOLD` | `GAIN_AILMENT_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_BOOTS` | gain Ailment Threshold equal to the lowest of Evasion and Armor on your Boots |
| `INC_ALLIES_IN_PRESENCE_REGENERATE` | `ALLIES_IN_YOUR_PRESENCE_REGENERATE_X_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY` | Allies in your Presence Regenerate {X} Rage per second if you have gain Rage Recently |
| `INC_ALLIES_IN_PRESENCE_REGENERATE_PER_SECOND` | `ALLIES_IN_YOUR_PRESENCE_REGENERATE_X_OF_YOUR_MAX_HEALTH_PER_SECOND` | Allies in your Presence Regenerate {X}% of your Max Health per second |
| `INC_ALLIES_IN_PRESENCE_REGENERATE_RECENTLY` | `ALLIES_IN_YOUR_PRESENCE_REGENERATE_X_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY` | Allies in your Presence Regenerate {X} Rage per second if you have gain Rage Recently |
| `INC_AMOUNT_DAMAGE_PREVENTED_DEFLECTION` | `X_TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION` | -{X}% to amount of Hit Damage Prevented by Deflection |
| `INC_AMOUNT_DAMAGE_PREVENTED_DEFLECTION_HITDAMAGE` | `X_TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION` | -{X}% to amount of Hit Damage Prevented by Deflection |
| `INC_AMOUNT_LEECH_HEALTH` | `X_INCREASED_AMOUNT_OF_HEALTH_HEALTH_LEECH` | {X}% increased amount of Health Health Leech |
| `INC_AMOUNT_LEECH_MANA` | `X_INCREASED_AMOUNT_OF_MANA_MANA_LEECH` | {X}% increased amount of Mana Mana Leech |
| `INC_ARCANE_SURGE_EFFECT` | `X_INCREASED_BUFF_EFFECT_OF_ARCANE_SURGE_ON_YOU` | {X}% increased Buff Effect of Arcane Surge on you |
| `INC_ARCHON_DURATION` | `X_INCREASED_ARCHON_BUFF_DURATION` | {X}% increased Archon Buff duration |
| `INC_ARCHON_EFFECT` | `X_INCREASED_EFFECT_OF_ARCHON_BUFFS_ON_YOU` | {X}% increased effect of Archon Buffs on you |
| `INC_ARCHON_RECOVERY` | `ARCHON_RECOVERY_PERIOD_EXPIRES_X_FASTER` | Archon recovery period expires {X}% faster |
| `INC_ARCHON_RECOVERY_PERIOD_EXPIRES` | `ARCHON_RECOVERY_PERIOD_EXPIRES_X_FASTER` | Archon recovery period expires {X}% faster |
| `INC_ARCHON_RECOVERY_PERIOD_EXPIRES_ARCHON` | `ARCHON_RECOVERY_PERIOD_EXPIRES_X_SLOWER` | Archon recovery period expires {X}% slower |
| `INC_AREA_DAMAGE` | `X_INCREASED_ATTACK_AREA_DAMAGE` | {X}% increased Attack Area Damage |
| `INC_AREA_OF_EFFECT` | `X_INCREASED_AREA_OF_EFFECT_FOR_ATTACKS` | {X}% increased Area of Effect for Attacks |
| `INC_ARMOUR` | `X_INCREASED_ARMOR` | {X}% increased Armor |
| `INC_ARMOUR_ENERGY` | `X_INCREASED_ARMOR_EVASION_AND_ENERGY_SHIELD` | {X}% increased Armor, Evasion and Energy Shield |
| `INC_ARMOUR_EQUAL_TOTAL_STRENGTH` | `GAIN_ARMOR_EQUAL_TO_X_OF_TOTAL_STRENGTH_REQUIREMENTS_OF_EQUIPPED_BOOTS_GLOVES_AND_HELMET` | gain Armor equal to {X}% of total Strength Requirements of Equipped Boots, Gloves and Helmet |
| `INC_ARMOUR_EQUAL_TOTAL_STRENGTH_HELMET` | `GAIN_ARMOR_EQUAL_TO_X_OF_TOTAL_STRENGTH_REQUIREMENTS_OF_EQUIPPED_BOOTS_GLOVES_AND_HELMET` | gain Armor equal to {X}% of total Strength Requirements of Equipped Boots, Gloves and Helmet |
| `INC_ARMOUR_EVASION` | `X_INCREASED_ARMOR_AND_EVASION` | {X}% increased Armor and Evasion |
| `INC_ARROWS_PIERCE_ADDITIONAL_TARGET` | `ARROWS_PIERCE_AN_ADDITIONAL_TARGET` | Arrows Pierce an additional Target |
| `INC_ATTACK_AGGRAVATE_ANY_BLEEDING` | `ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_X_SECONDS` | Attack Hit Damage Aggravate any Bleeding on targets which is older than {X} seconds |
| `INC_ATTACK_AGGRAVATE_ANY_BLEEDING_HITDAMAGE` | `ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_X_SECONDS` | Attack Hit Damage Aggravate any Bleeding on targets which is older than {X} seconds |
| `INC_ATTACK_DAMAGE` | `X_INCREASED_PROJECTILE_DAMAGE` | {X}% increased Projectile Damage |
| `INC_ATTACK_DAMAGE_PHYSICAL` | `X_INCREASED_ATTACK_PHYSICAL_DAMAGE` | {X}% increased Attack Physical Damage |
| `INC_ATTACK_ELEMENTAL_DAMAGE` | `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_WITH_ATTACKS` | {X}% increased Elemental Damage Damage with Attacks |
| `INC_ATTACK_NUMBER_SUMMONED_BALLISTA` | `ATTACK_SKILLS_HAVE_X_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS` | Attack Skills have +{X} to Max number of Summoned Ballista Totems |
| `INC_ATTACK_NUMBER_SUMMONED_BALLISTA_TOTEM` | `ATTACK_SKILLS_HAVE_X_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS` | Attack Skills have +{X} to Max number of Summoned Ballista Totems |
| `INC_ATTACK_SPEED` | `X_INCREASED_ATTACK_SPEED` | {X}% increased Attack Speed |
| `INC_AURA_BUFFS_AFFECT_ALLIES` | `YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES` | Your Aura Buffs do not affect Allies |
| `INC_AURA_MAGNITUDE` | `AURA_SKILLS_HAVE_X_INCREASED_BUFF_MAGNITUDE` | Aura Skills have {X}% increased Buff Magnitude |
| `INC_AXE_DAMAGE` | `X_INCREASED_DAMAGE_WITH_AXES` | {X}% increased Damage with Axes |
| `INC_BANNER_AURA_MAGNITUDE` | `BANNER_SKILLS_HAVE_X_INCREASED_AURA_BUFF_MAGNITUDE` | Banner Skills have {X}% increased Aura Buff Magnitude |
| `INC_BANNER_BUFFS_LINGER_LEAVE` | `BANNER_BUFFS_LINGER_ON_YOU_FOR_X_SECONDS_AFTER_YOU_LEAVE_THE_AREA` | Banner Buffs linger on you for {X} seconds after you leave the Area |
| `INC_BANNER_BUFFS_LINGER_LEAVE_BUFF` | `BANNER_BUFFS_LINGER_ON_YOU_FOR_X_SECONDS_AFTER_YOU_LEAVE_THE_AREA` | Banner Buffs linger on you for {X} seconds after you leave the Area |
| `INC_BANNER_DURATION` | `BANNER_SKILLS_HAVE_X_INCREASED_DURATION` | Banner Skills have {X}% increased Duration |
| `INC_BEAST_AILMENT_DURATION` | `X_INCREASED_DURATION_OF_AILMENTS_ON_BEASTS` | {X}% increased Duration of Ailments on Beasts |
| `INC_BLEEDING_DURATION_TAKEN` | `X_INCREASED_DURATION_OF_BLEEDING_ON_YOU` | {X}% increased Duration of Bleeding on You |
| `INC_BLEEDING_INFLICT_DEALS_DAMAGE` | `BLEEDING_YOU_INFLICT_DEALS_DAMAGE_X_FASTER` | Bleeding you inflict deals Damage {X}% faster |
| `INC_BLEEDING_INFLICT_PINNED_IS` | `BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED` | Bleeding you inflict on Pinned Enemies is Aggravated |
| `INC_BLEEDING_INFLICT_PINNED_IS_AGGRAVATE` | `BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED` | Bleeding you inflict on Pinned Enemies is Aggravated |
| `INC_BLEED_DURATION` | `X_INCREASED_MOVEMENT_SPEED` | {X}% increased Movement Speed |
| `INC_BLEED_MAGNITUDE_TAKEN` | `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_ON_YOU` | {X}% increased Buff Magnitude of Bleeding on You |
| `INC_BLIND_EFFECT` | `X_INCREASED_BLIND_EFFECT` | {X}% increased Blind Effect |
| `INC_BLIND_THEY_STUN` | `BLIND_ENEMIES_WHEN_THEY_STUN_YOU` | Blind Enemies when they Stun you |
| `INC_BLOCK_CHANCE` | `X_INCREASED_BLOCK_CHANCE` | {X}% increased Block chance |
| `INC_BOLT_SPEED` | `X_INCREASED_BOLT_SPEED` | {X}% increased Bolt Speed |
| `INC_BONUSES_AMULET_MINIONS` | `ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU` | All bonuses from Equipped Amulet apply to your Minions instead of you |
| `INC_BOW_ATTACKS_CULLING_STRIKE` | `BOW_ATTACKS_HAVE_CULLING_STRIKE` | Bow Attacks have Culling Strike |
| `INC_BOW_ATTACKS_CULLING_STRIKE_ATTACK` | `BOW_ATTACKS_HAVE_CULLING_STRIKE` | Bow Attacks have Culling Strike |
| `INC_BREAK_ARMOUR_CRITICAL_SPELLS` | `ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_X_OF_PHYSICAL_DAMAGE_DEALT` | Armor Break Armor on Critical Hit with Spells equal to {X}% of Physical Damage dealt |
| `INC_BREAK_ARMOUR_CRITICAL_SPELLS_SPELL` | `ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_X_OF_PHYSICAL_DAMAGE_DEALT` | Armor Break Armor on Critical Hit with Spells equal to {X}% of Physical Damage dealt |
| `INC_BREAK_ARMOUR_HEAVY_STUNNING` | `ARMOR_BREAK_X_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY` | Armor Break {X}% of Armor on Heavy Stunning an Enemy |
| `INC_BREAK_ARMOUR_HEAVY_STUNNING_ARMOURBREAK` | `ARMOR_BREAK_X_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY` | Armor Break {X}% of Armor on Heavy Stunning an Enemy |
| `INC_BREAK_ARMOUR_PINNING` | `ARMOR_BREAK_X_OF_ARMOR_ON_PINNING_AN_ENEMY` | Armor Break {X}% of Armor on Pinning an Enemy |
| `INC_BREAK_ARMOUR_PINNING_ARMOURBREAK` | `ARMOR_BREAK_X_OF_ARMOR_ON_PINNING_AN_ENEMY` | Armor Break {X}% of Armor on Pinning an Enemy |
| `INC_BUFFS_EXPIRE_SLOWER` | `BUFFS_ON_YOU_EXPIRE_X_SLOWER` | Buffs on you expire {X}% slower |
| `INC_BUFF_EFFECT` | `OFFERING_SKILLS_HAVE_X_INCREASED_BUFF_EFFECT` | Offering Skills have {X}% increased Buff effect |
| `INC_BUFF_MAGNITUDE` | `AURA_SKILLS_HAVE_X_INCREASED_BUFF_MAGNITUDE` | Aura Skills have {X}% increased Buff Magnitude |
| `INC_CANNOT_BLINDED` | `CANNOT_BE_BLINDED` | Cannot be Blinded |
| `INC_CANNOT_BLINDED_FULL_LIFE` | `CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH` | Cannot be Blinded while on Full Health |
| `INC_CANNOT_CRITICALLY_PARRYING` | `CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING` | Cannot be Critically Hit while Parrying |
| `INC_CANNOT_ELECTROCUTED` | `YOU_CANNOT_BE_ELECTROCUTED` | You cannot be Electrocuted |
| `INC_CANNOT_EVADED_PINNED` | `YOUR_HITS_CANNOT_BE_EVASION_BY_PINNED_ENEMIES` | Your Hits cannot be Evasion by Pinned Enemies |
| `INC_CAST_SPEED` | `X_INCREASED_CAST_SPEED` | {X}% increased Cast Speed |
| `INC_CHAOS_DAMAGE` | `X_INCREASED_CHAOS_DAMAGE` | {X}% increased Chaos Damage |
| `INC_CHAOS_RESISTANCE` | `X_TO_CHAOS_RESISTANCE_2` | -{X}% to Chaos Resistance |
| `INC_CHARGE_DURATION` | `X_INCREASED_FRENZY_CHARGE_DURATION` | {X}% increased Frenzy Charge Duration |
| `INC_CHARMS_CHARGES` | `CHARMS_GAIN_X_CHARGES_PER_SECOND` | Charms gain {X} charges per Second |
| `INC_CHARMS_EFFECT` | `CHARMS_APPLIED_TO_YOU_HAVE_X_INCREASED_EFFECT` | Charms applied to you have {X}% increased Effect |
| `INC_CHARM_CHARGES` | `X_INCREASED_CHARM_CHARGES_GAIN` | {X}% increased Charm Charges gain |
| `INC_CHARM_CHARGES_USED` | `X_INCREASED_CHARM_CHARGES_USED` | {X}% increased Charm Charges used |
| `INC_CHARM_DURATION` | `TEMPORARY_MINION_SKILLS_HAVE_X_TO_LIMIT_OF_MINIONS_SUMMONED` | Temporary Minion Skills have +{X} to Limit of Minions summoned |
| `INC_CHARM_EFFECT` | `CHARMS_APPLIED_TO_YOU_HAVE_X_INCREASED_EFFECT` | Charms applied to you have {X}% increased Effect |
| `INC_CHILL_EFFECT_TAKEN` | `X_INCREASED_EFFECT_OF_CHILL_ON_YOU` | {X}% increased Effect of Chill on you |
| `INC_CHILL_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT` | {X}% increased Buff Magnitude of Chill you inflict |
| `INC_COLD_DAMAGE` | `X_INCREASED_COLD_DAMAGE` | {X}% increased Cold Damage |
| `INC_COLD_RESISTANCE` | `X_TO_INTELLIGENCE` | +{X} to Intelligence |
| `INC_COMPANIONS_IN_PRESENCE_ONSLAUGHT` | `COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED` | Companions in your Presence have Onslaught while you are Shapeshifted |
| `INC_COMPANIONS_IN_PRESENCE_ONSLAUGHT_WHILE_YOU` | `COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED` | Companions in your Presence have Onslaught while you are Shapeshifted |
| `INC_COMPANIONS_STRENGTH` | `COMPANIONS_GAIN_YOUR_STRENGTH` | Companions gain your Strength |
| `INC_COMPANION_DAMAGE` | `COMPANIONS_DEAL_X_INCREASED_DAMAGE` | Companions deal {X}% increased Damage |
| `INC_COOLDOWN_RECOVERY_RATE` | `X_INCREASED_COOLDOWN_RECOVERY_RATE` | {X}% increased Cooldown Recovery Rate |
| `INC_COST_EFFICIENCY` | `X_INCREASED_MANA_COST_EFFICIENCY` | {X}% increased Mana Cost Efficiency |
| `INC_COST_EFFICIENCY_COMMAND` | `X_INCREASED_MANA_COST_EFFICIENCY_OF_COMMAND_SKILLS` | {X}% increased Mana Cost Efficiency of Command Skills |
| `INC_CRIT_CHANCE` | `X_INCREASED_CRITICAL_HIT_CHANCE` | {X}% increased Critical Hit Chance |
| `INC_CRIT_DAMAGE` | `X_INCREASED_CRITICAL_DAMAGE_BONUS` | {X}% increased Critical Damage Bonus |
| `INC_CROSSBOW_DAMAGE` | `X_INCREASED_CROSSBOW_RELOAD_SPEED` | {X}% increased Crossbow Reload Speed |
| `INC_CULLING_STRIKE_BEASTS_COMPANION` | `CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` | Culling Strike against Beasts while your Companion is in your Presence |
| `INC_CULLING_STRIKE_BEASTS_COMPANION_WHILE_YOUR` | `CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` | Culling Strike against Beasts while your Companion is in your Presence |
| `INC_CULLING_STRIKE_MARK` | `CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK` | Culling Strike against Enemies you Mark |
| `INC_CULLING_STRIKE_THRESHOLD` | `X_INCREASED_CULLING_STRIKE_THRESHOLD` | {X}% increased Culling Strike Threshold |
| `INC_CURSE_CANNOT_RECHARGE_ENERGY` | `ENEMIES_YOU_CURSE_CANNOT_RECHARGE_ENERGY_SHIELD` | Enemies you Curse cannot Recharge Energy Shield |
| `INC_CURSE_CANNOT_RECHARGE_ENERGY_ENERGYSHIELD` | `ENEMIES_YOU_CURSE_CANNOT_RECHARGE_ENERGY_SHIELD` | Enemies you Curse cannot Recharge Energy Shield |
| `INC_CURSE_CHAOS_RESISTANCE` | `ENEMIES_YOU_CURSE_HAVE_X_TO_CHAOS_RESISTANCES` | Enemies you Curse have -{X}% to Chaos Resistances |
| `INC_CURSE_CHAOS_RESISTANCE_RESISTANCES` | `ENEMIES_YOU_CURSE_HAVE_X_TO_CHAOS_RESISTANCES` | Enemies you Curse have -{X}% to Chaos Resistances |
| `INC_CURSE_DAMAGE` | `X_INCREASED_DAMAGE_PER_CURSE_ON_YOU` | {X}% increased Damage per Curse on you |
| `INC_CURSE_DURATION` | `X_INCREASED_CURSE_DURATION` | {X}% increased Curse Duration |
| `INC_CURSE_EFFECT_TAKEN` | `X_INCREASED_BUFF_EFFECT_OF_CURSES_ON_YOU` | {X}% increased Buff Effect of Curses on you |
| `INC_CURSE_MAGNITUDE` | `X_INCREASED_CURSE_BUFF_MAGNITUDE` | {X}% increased Curse Buff Magnitude |
| `INC_DAMAGE_ATTACK` | `X_INCREASED_ATTACK_DAMAGE` | {X}% increased Attack Damage |
| `INC_DAMAGE_EMPOWERED_ATTACKS` | `X_CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE` | {X}% chance to inflict Bleeding on Hit Damage |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT` | `GAIN_X_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Damage of a random Elemental Damage while Shapeshifted |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_SHAPESHIFT` | `GAIN_X_OF_DAMAGE_AS_EXTRA_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED` | gain {X}% of Damage as Extra Damage of a random Elemental Damage while Shapeshifted |
| `INC_DAMAGE_HITS` | `X_INCREASED_DAMAGE_WITH_HITS_AGAINST_CHILLED_ENEMIES` | {X}% increased Damage with Hits against Chilled Enemies |
| `INC_DAMAGE_IS_TAKEN_DAMAGEABLE` | `X_OF_DAMAGE_FROM_HIT_DAMAGE_IS_TAKEN_FROM_YOUR_DAMAGEABLE_COMPANION_S_HEALTH_BEFORE_YOU` | {X}% of Damage from Hit Damage is taken from your Damageable Companion's Health before you |
| `INC_DAMAGE_IS_TAKEN_DAMAGEABLE_HITDAMAGE` | `X_OF_DAMAGE_FROM_HIT_DAMAGE_IS_TAKEN_FROM_YOUR_DAMAGEABLE_COMPANION_S_HEALTH_BEFORE_YOU` | {X}% of Damage from Hit Damage is taken from your Damageable Companion's Health before you |
| `INC_DAMAGE_IS_TAKEN_MANA` | `X_OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` | {X}% of Damage is taken from Mana before Health |
| `INC_DAMAGE_LOW_LIFE` | `X_INCREASED_DAMAGE_TAKEN_WHILE_ON_LOW_HEALTH` | {X}% increased Damage taken while on Low Health |
| `INC_DAMAGE_PARRYING` | `SUCCESSFULLY_PARRYING_A_MELEE_HIT_DAMAGE_GRANTS_X_INCREASED_DAMAGE_TO_YOUR_NEXT_RANGED_ATTACK` | Successfully Parrying a Melee Hit Damage grants {X}% increased Damage to your next Ranged Attack |
| `INC_DAMAGE_PRESENCE_ALLIES` | `ALLIES_IN_YOUR_PRESENCE_DEAL_X_INCREASED_DAMAGE` | Allies in your Presence deal {X}% increased Damage |
| `INC_DAMAGE_PRESENCE_ENEMIES` | `EACH_TOTEM_APPLIES_X_INCREASED_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE` | Each Totem applies {X}% increased Damage taken to Enemies in their Presence |
| `INC_DAMAGE_SHAPESHIFTED` | `X_INCREASED_DAMAGE_WHILE_SHAPESHIFTED` | {X}% increased Damage while Shapeshifted |
| `INC_DAMAGE_TAKEN_BYPASSES_ENERGY` | `X_OF_DAMAGE_TAKEN_BYPASSES_ENERGY_SHIELD` | {X}% of Damage taken bypasses Energy Shield |
| `INC_DAMAGING_AILMENTS_CANNOT_INFLICTED` | `DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE` | Damaging Ailments Cannot Be inflicted on you while you already have one |
| `INC_DAMAGING_AILMENTS_CANNOT_INFLICTED_WHILE_YOU` | `DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE` | Damaging Ailments Cannot Be inflicted on you while you already have one |
| `INC_DAMAGING_AILMENTS_DAMAGE_FASTER` | `DAMAGING_AILMENTS_DEAL_DAMAGE_X_FASTER` | Damaging Ailments deal damage {X}% faster |
| `INC_DAMAGING_AILMENT_DURATION` | `X_INCREASED_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES` | {X}% increased Duration of Damaging Ailments on Enemies |
| `INC_DAMAGING_AILMENT_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT` | {X}% increased Buff Magnitude of Damaging Ailments you inflict |
| `INC_DAZE_DURATION` | `X_CHANCE_TO_DAZE_ON_HIT_DAMAGE` | {X}% chance to Daze on Hit Damage |
| `INC_DAZE_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_DAZE` | {X}% increased Buff Magnitude of Daze |
| `INC_DEBILITATE_METRES_IN_FRONT` | `APPLY_DEBILITATE_TO_ENEMIES_X_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED` | Apply Debilitate to Enemies {X} Metres in front of you while your Shield is raised |
| `INC_DEBILITATE_METRES_IN_FRONT_WHILE_YOUR` | `APPLY_DEBILITATE_TO_ENEMIES_X_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED` | Apply Debilitate to Enemies {X} Metres in front of you while your Shield is raised |
| `INC_DEBUFFS_EXPIRE_FASTER` | `DEBUFFS_ON_YOU_EXPIRE_X_FASTER` | Debuffs on you expire {X}% faster |
| `INC_DEBUFF_EXPIRY` | `DEBUFFS_ON_YOU_EXPIRE_X_FASTER` | Debuffs on you expire {X}% faster |
| `INC_DEFEND_ARMOUR_CRITICAL` | `DEFEND_WITH_X_OF_ARMOR_AGAINST_CRITICAL_HITS` | Defend with {X}% of Armor against Critical Hits |
| `INC_DEFEND_ARMOUR_FURTHER_THAN` | `DEFEND_WITH_X_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY` | Defend with {X}% of Armor against Hit Damage from Enemies that are further than {X}m away |
| `INC_DEFEND_ARMOUR_FURTHER_THAN_HITDAMAGE` | `DEFEND_WITH_X_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY` | Defend with {X}% of Armor against Hit Damage from Enemies that are further than {X}m away |
| `INC_DEFEND_ARMOUR_LOW_ENERGY` | `DEFEND_WITH_X_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD` | Defend with {X}% of Armor while not on Low Energy Shield |
| `INC_DEFEND_ARMOUR_LOW_ENERGY_WHILE_NOT` | `DEFEND_WITH_X_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD` | Defend with {X}% of Armor while not on Low Energy Shield |
| `INC_DEFEND_ARMOUR_PROJECTILE_ATTACKS` | `DEFEND_WITH_X_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS` | Defend with {X}% of Armor against Projectile Attacks |
| `INC_DEFEND_ARMOUR_PROJECTILE_ATTACKS_ATTACK` | `DEFEND_WITH_X_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS` | Defend with {X}% of Armor against Projectile Attacks |
| `INC_DEFLECTED_CANNOT_INFLICT_BLEEDING` | `DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU` | Deflected Hit Damage cannot inflict Bleeding on you |
| `INC_DEFLECTED_CANNOT_INFLICT_BLEEDING_DEFLECT` | `DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU` | Deflected Hit Damage cannot inflict Bleeding on you |
| `INC_DEFLECTED_CANNOT_INFLICT_MAIM` | `DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_MAIM_ON_YOU` | Deflected Hit Damage cannot inflict Maim on you |
| `INC_DEFLECTED_CANNOT_INFLICT_MAIM_DEFLECT` | `DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_MAIM_ON_YOU` | Deflected Hit Damage cannot inflict Maim on you |
| `INC_DEFLECTION` | `X_INCREASED_DEFLECTION_RATING_WHILE_SURROUNDED` | {X}% increased Deflection Rating while Surrounded |
| `INC_DEFLECTION_RATING_EQUAL_EVASION` | `GAIN_DEFLECTION_RATING_EQUAL_TO_X_OF_EVASION` | gain Deflection Rating equal to {X}% of Evasion |
| `INC_DEFLECTION_RATING_EQUAL_EVASION_DEFLECT` | `GAIN_DEFLECTION_RATING_EQUAL_TO_X_OF_EVASION` | gain Deflection Rating equal to {X}% of Evasion |
| `INC_DEFLECTION_WHILE_SURROUNDED` | `X_INCREASED_AILMENT_THRESHOLD_AND_STUN_THRESHOLD_THRESHOLD_WHILE_SURROUNDED` | {X}% increased Ailment Threshold and Stun Threshold Threshold while Surrounded |
| `INC_DEXTERITY` | `X_INCREASED_ATTRIBUTES` | {X}% increased Attributes |
| `INC_DURATION_AILMENTS_BEASTS` | `X_INCREASED_DURATION_OF_AILMENTS_ON_BEASTS` | {X}% increased Duration of Ailments on Beasts |
| `INC_EFFECT_NON_DAMAGING_AILMENTS` | `X_INCREASED_EFFECT_OF_NON_DAMAGING_AILMENTS_ON_YOU` | {X}% increased Effect of Non-Damaging Ailments on you |
| `INC_EFFECT_NON_DAMAGING_AILMENTS_NONDAMAGINGAILMENTS` | `X_INCREASED_EFFECT_OF_NON_DAMAGING_AILMENTS_ON_YOU` | {X}% increased Effect of Non-Damaging Ailments on you |
| `INC_ELECTROCUTE_BUILDUP` | `X_INCREASED_ELECTROCUTE_BUILDUP` | {X}% increased Electrocute Buildup |
| `INC_ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED` | `X_INCREASED_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` | {X}% increased Elemental Ailments Ailment Application if you have Shapeshifted to an Animal form Recently |
| `INC_ELEMENTAL_AILMENT_THRESHOLD` | `X_INCREASED_ELEMENTAL_AILMENT_THRESHOLD` | {X}% increased Elemental Ailment Threshold |
| `INC_ELEMENTAL_ARCHON_ENERGY_SHIELD` | `GAIN_ELEMENTAL_ARCHON_WHEN_YOUR_ENERGY_SHIELD_RECHARGE_BEGINS` | gain Elemental Archon when your Energy Shield Recharge begins |
| `INC_ELEMENTAL_ARCHON_ENERGY_SHIELD_ELEMENTALARCHON` | `GAIN_ELEMENTAL_ARCHON_WHEN_YOUR_ENERGY_SHIELD_RECHARGE_BEGINS` | gain Elemental Archon when your Energy Shield Recharge begins |
| `INC_ELEMENTAL_ARCHON_SPENDING_MANA` | `GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_X_OF_YOUR_MAX_MANA` | gain Elemental Archon after spending {X}% of your Max Mana |
| `INC_ELEMENTAL_ARCHON_SPENDING_MANA_ELEMENTALARCHON` | `GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_X_OF_YOUR_MAX_MANA` | gain Elemental Archon after spending {X}% of your Max Mana |
| `INC_ELEMENTAL_DAMAGE` | `X_INCREASED_ELEMENTAL_DAMAGE` | {X}% increased Elemental Damage |
| `INC_ENDURANCE_CHARGE_HEAVY_STUN` | `GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY` | gain an Endurance Charge when you Heavy Stun a Rare or Unique Enemy |
| `INC_ENDURANCE_CHARGE_HEAVY_STUN_CHARGES` | `GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY` | gain an Endurance Charge when you Heavy Stun a Rare or Unique Enemy |
| `INC_ENERGY_REGENERATION_RATE` | `X_INCREASED_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED` | {X}% increased Energy Shield Recharge Rate while Shapeshifted |
| `INC_ENERGY_SHIELD_ADDITIONAL_FREEZE` | `GAIN_X_OF_MAX_ENERGY_SHIELD_AS_ADDITIONAL_FREEZE_THRESHOLD` | gain {X}% of Max Energy Shield as additional Freeze Threshold |
| `INC_ENERGY_SHIELD_ADDITIONAL_FREEZE_ENERGYSHIELD` | `GAIN_X_OF_MAX_ENERGY_SHIELD_AS_ADDITIONAL_FREEZE_THRESHOLD` | gain {X}% of Max Energy Shield as additional Freeze Threshold |
| `INC_ENERGY_SHIELD_BLOCK` | `GAIN_X_ENERGY_SHIELD_WHEN_YOU_BLOCK` | gain {X} Energy Shield when you Block |
| `INC_ES_FROM_FOCUS` | `X_INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS` | {X}% increased Energy Shield from Equipped Focus |
| `INC_ES_FROM_HELMET` | `X_INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_HELMET` | {X}% increased Energy Shield from Equipped Helmet |
| `INC_EVASION` | `X_INCREASED_EVASION` | {X}% increased Evasion |
| `INC_EVASION_RATING_EXTRA_ARMOUR` | `GAIN_X_OF_EVASION_AS_EXTRA_ARMOR` | gain {X}% of Evasion as extra Armor |
| `INC_EXPOSURE_EFFECT` | `X_INCREASED_EXPOSURE_EFFECT` | {X}% increased Exposure Effect |
| `INC_EXPOSURE_INFLICT_LOWERS_RESISTANCES` | `EXPOSURE_YOU_INFLICT_LOWERS_RESISTANCES_BY_AN_ADDITIONAL_X` | Exposure you inflict lowers Resistances by an additional {X}% |
| `INC_FASTER_AILMENTS` | `X_INCREASED_CHANCE_TO_INFLICT_AILMENTS` | {X}% increased chance to inflict Ailments |
| `INC_FASTER_CURSE_ACTIVATION` | `X_FASTER_CURSE_ACTIVATION` | {X}% faster Curse Activation |
| `INC_FASTER_START_ENERGY_SHIELD` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE` | {X}% faster start of Energy Shield Recharge |
| `INC_FASTER_START_ENERGY_SHIELD_ARCHON_BUFF` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_AFFECTED_BY_AN_ARCHON_BUFF` | {X}% faster start of Energy Shield Recharge while affected by an Archon Buff |
| `INC_FASTER_START_ENERGY_SHIELD_FASTERESRECHARGESTART` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE` | {X}% faster start of Energy Shield Recharge |
| `INC_FASTER_START_ENERGY_SHIELD_NO_FULL_LIFE` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHEN_NOT_ON_FULL_HEALTH` | {X}% faster start of Energy Shield Recharge when not on Full Health |
| `INC_FASTER_START_ENERGY_SHIELD_RECENTLY` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE_IF_YOU_VE_BEEN_STUNNED_RECENTLY` | {X}% faster start of Energy Shield Recharge if you've been Stunned Recently |
| `INC_FASTER_START_ENERGY_SHIELD_SHAPESHIFT` | `X_FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED` | {X}% faster start of Energy Shield Recharge while Shapeshifted |
| `INC_FASTER_START_INHERENT_RAGE` | `X_FASTER_START_OF_INHERENT_RAGE_LOSS` | {X}% faster start of inherent Rage loss |
| `INC_FIRE_DAMAGE` | `X_INCREASED_FIRE_DAMAGE` | {X}% increased Fire Damage |
| `INC_FIRE_RESISTANCE` | `X_TO_STRENGTH` | +{X} to Strength |
| `INC_FISSURE_LIMIT` | `FISSURE_SKILLS_HAVE_X_TO_LIMIT` | Fissure Skills have +{X} to Limit |
| `INC_FLAMMABILITY_MAGNITUDE` | `X_INCREASED_FLAMMABILITY_BUFF_MAGNITUDE` | {X}% increased Flammability Buff Magnitude |
| `INC_FLASK_CHARGES_GAINED` | `X_INCREASED_HEALTH_FLASK_CHARGES_GAIN` | {X}% increased Health Flask Charges gain |
| `INC_FLASK_CHARGES_USED` | `X_INCREASED_FLASK_CHARGES_USED_FROM_MANA_FLASKS` | {X}% increased Flask Charges used from Mana Flasks |
| `INC_FLASK_EFFECT_DURATION` | `X_CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES` | {X}% chance for Flasks you use to not consume Charges |
| `INC_FLASK_LIFE_RECOVERY` | `X_INCREASED_HEALTH_RECOVERY_FROM_FLASKS` | {X}% increased Health Recovery from Flasks |
| `INC_FLASK_MANA_RECOVERY` | `X_INCREASED_MANA_RECOVERY_FROM_FLASKS` | {X}% increased Mana Recovery from Flasks |
| `INC_FLASK_RECOVERY` | `X_INCREASED_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH` | {X}% increased Health Recovery from Flasks used when on Low Health |
| `INC_FREEZE_BUILDUP` | `X_INCREASED_FREEZE_BUILDUP` | {X}% increased Freeze Buildup |
| `INC_FREEZE_CHILL_THRESHOLD` | `X_INCREASED_FREEZE_THRESHOLD` | {X}% increased Freeze Threshold |
| `INC_FREEZE_DURATION_TAKEN` | `X_INCREASED_FREEZE_DURATION_ON_YOU` | {X}% increased Freeze Duration on you |
| `INC_FULLY_ARMOUR_BREAK_CANNOT` | `ENEMIES_YOU_FULLY_ARMOR_BREAK_CANNOT_REGENERATE_HEALTH` | Enemies you Fully Armor Break cannot Regenerate Health |
| `INC_FULLY_ARMOUR_BREAK_CANNOT_ARMOURBREAK` | `ENEMIES_YOU_FULLY_ARMOR_BREAK_CANNOT_REGENERATE_HEALTH` | Enemies you Fully Armor Break cannot Regenerate Health |
| `INC_FULLY_ARMOUR_BREAK_MAIMED` | `ENEMIES_YOU_FULLY_ARMOR_BREAK_ARE_MAIMED` | Enemies you Fully Armor Break are Maimed |
| `INC_FULLY_ARMOUR_BREAK_MAIMED_ARMOURBREAK` | `ENEMIES_YOU_FULLY_ARMOR_BREAK_ARE_MAIMED` | Enemies you Fully Armor Break are Maimed |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT` | `FULLY_BROKEN_ARMOR_YOU_INFLICT_ALSO_INCREASED_FIRE_DAMAGE_TAKEN_FROM_HIT_DAMAGE` | Fully Broken Armor you inflict also increased Fire Damage Taken from Hit Damage |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_ARMOURBREAK` | `FULLY_BROKEN_ARMOR_YOU_INFLICT_ALSO_INCREASED_FIRE_DAMAGE_TAKEN_FROM_HIT_DAMAGE` | Fully Broken Armor you inflict also increased Fire Damage Taken from Hit Damage |
| `INC_GEMS_ATTRIBUTE_REQUIREMENTS` | `EQUIPMENT_AND_SKILL_GEMS_HAVE_X_INCREASED_ATTRIBUTES_REQUIREMENTS` | Equipment and Skill Gems have {X}% increased Attributes Requirements |
| `INC_GEMS_ATTRIBUTE_REQUIREMENTS_ATTRIBUTES` | `EQUIPMENT_AND_SKILL_GEMS_HAVE_X_INCREASED_ATTRIBUTES_REQUIREMENTS` | Equipment and Skill Gems have {X}% increased Attributes Requirements |
| `INC_GLORY_GENERATION` | `X_INCREASED_GLORY_GENERATION` | {X}% increased Glory generation |
| `INC_GLORY_RARE_OR_UNIQUE` | `SKILLS_GAIN_X_GLORY_EVERY_X_SECONDS_FOR_EACH_RARE_OR_UNIQUE_MONSTER_IN_YOUR_PRESENCE` | Skills gain {X} Glory every {X} seconds for each Rare or Unique monster in your Presence |
| `INC_GLORY_RARE_OR_UNIQUE_RARITY` | `SKILLS_GAIN_X_GLORY_EVERY_X_SECONDS_FOR_EACH_RARE_OR_UNIQUE_MONSTER_IN_YOUR_PRESENCE` | Skills gain {X} Glory every {X} seconds for each Rare or Unique monster in your Presence |
| `INC_GRENADE_COOLDOWN` | `GRENADE_SKILLS_HAVE_X_COOLDOWN_USE` | Grenade Skills have +{X} Cooldown Use |
| `INC_GRENADE_DAMAGE` | `X_INCREASED_GRENADE_DAMAGE` | {X}% increased Grenade Damage |
| `INC_GRENADE_DETONATION_TIME` | `X_INCREASED_GRENADE_DETONATION_TIME` | {X}% increased Grenade Detonation Time |
| `INC_GUARD` | `X_INCREASED_HEALTH_RECOVERY_FROM_FLASKS` | {X}% increased Health Recovery from Flasks |
| `INC_HAZARD_DAMAGE` | `X_INCREASED_HAZARD_DAMAGE` | {X}% increased Hazard Damage |
| `INC_HAZARD_DURATION` | `X_INCREASED_HAZARD_DURATION` | {X}% increased Hazard Duration |
| `INC_HEAVY_STUN_CULLING_STRIKE` | `HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE` | Hits that Heavy Stun Enemies have Culling Strike |
| `INC_HEAVY_STUN_CULLING_STRIKE_HEAVYSTUN` | `HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE` | Hits that Heavy Stun Enemies have Culling Strike |
| `INC_HEAVY_STUN_SHAPESHIFTED_INTIMIDATED` | `ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_X_SECONDS` | Enemies you Heavy Stun while Shapeshifted are Intimidated for {X} seconds |
| `INC_HEAVY_STUN_SHAPESHIFTED_INTIMIDATED_HEAVYSTUN` | `ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_X_SECONDS` | Enemies you Heavy Stun while Shapeshifted are Intimidated for {X} seconds |
| `INC_HERALD_DAMAGE` | `HERALD_SKILLS_DEAL_X_INCREASED_DAMAGE` | Herald Skills deal {X}% increased Damage |
| `INC_ICE_CRYSTALS_LIFE` | `X_INCREASED_ICE_CRYSTAL_HEALTH` | {X}% increased Ice Crystal Health |
| `INC_IGNITED_FIRE_RESISTANCE` | `ENEMIES_IGNITED_BY_YOU_HAVE_X_TO_FIRE_RESISTANCES` | Enemies Ignited by you have -{X}% to Fire Resistances |
| `INC_IGNITED_FIRE_RESISTANCE_IGNITE` | `ENEMIES_IGNITED_BY_YOU_HAVE_X_TO_FIRE_RESISTANCES` | Enemies Ignited by you have -{X}% to Fire Resistances |
| `INC_IGNITES_CAUSE_REFLECTED_BACK` | `IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU` | Ignites you cause are reflected back to you |
| `INC_IGNITES_INFLICT_DAMAGE_FASTER` | `IGNITES_YOU_INFLICT_DEAL_DAMAGE_X_FASTER` | Ignites you inflict deal Damage {X}% faster |
| `INC_IGNITE_DURATION` | `X_INCREASED_IGNITE_DURATION_ON_ENEMIES` | {X}% increased Ignite Duration on Enemies |
| `INC_IGNITE_DURATION_TAKEN` | `X_INCREASED_IGNITE_DURATION_ON_YOU` | {X}% increased Ignite Duration on you |
| `INC_IGNITE_FASTER` | `IGNITES_YOU_INFLICT_DEAL_DAMAGE_X_FASTER` | Ignites you inflict deal Damage {X}% faster |
| `INC_IGNITE_MAGNITUDE` | `X_INCREASED_IGNITE_BUFF_MAGNITUDE` | {X}% increased Ignite Buff Magnitude |
| `INC_IGNITE_MAGNITUDE_TAKEN` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU` | {X}% increased Buff Magnitude of Ignite on you |
| `INC_IGNORE_MOVEMENT_PENALTIES_ARMOUR` | `IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR` | Ignore all Movement Penalties from Equip Armor |
| `INC_IGNORE_NON_NEGATIVE_ELEMENTAL` | `HIT_DAMAGE_IGNORE_NON_NEGATIVE_ELEMENTAL_DAMAGE_RESISTANCES_OF_FROZEN_ENEMIES` | Hit Damage ignore non-negative Elemental Damage Resistances of Frozen Enemies |
| `INC_IGNORE_NON_NEGATIVE_ELEMENTAL_FROZEN` | `HIT_DAMAGE_IGNORE_NON_NEGATIVE_ELEMENTAL_DAMAGE_RESISTANCES_OF_FROZEN_ENEMIES` | Hit Damage ignore non-negative Elemental Damage Resistances of Frozen Enemies |
| `INC_IMMOBILISATION_BUILDUP` | `X_INCREASED_IMMOBILISATION_BUILDUP` | {X}% increased Immobilisation buildup |
| `INC_IMPALE_MAGNITUDE` | `X_INCREASED_PHYSICAL_DAMAGE` | {X}% increased Physical Damage |
| `INC_INCISION_EFFECT` | `X_INCREASED_EFFECT_OF_INCISION` | {X}% increased effect of Incision |
| `INC_INCISION_MAGNITUDE` | `X_INCREASED_EFFECT_OF_INCISION` | {X}% increased effect of Incision |
| `INC_INCREASES_REDUCTIONS_COMPANION_DAMAGE` | `INCREASED_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU` | increased and Reductions to Companion Damage also apply to you |
| `INC_INCREASES_REDUCTIONS_COMPANION_DAMAGE_PROJECTILE` | `INCREASED_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU` | increased and Reductions to Companion Damage also apply to you |
| `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED` | `INCREASED_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS` | increased and Reductions to Projectile Speed also apply to Damage with Bows |
| `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW` | `INCREASED_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS` | increased and Reductions to Projectile Speed also apply to Damage with Bows |
| `INC_INFLICT_BLEEDING_CANNOT_REGENERATE` | `ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH` | Enemies you inflict Bleeding on cannot Regenerate Health |
| `INC_INFUSION_DURATION` | `X_INCREASED_ELEMENTAL_INFUSION_DURATION` | {X}% increased Elemental Infusion duration |
| `INC_INHERENT_LOSS_RAGE_IS` | `INHERENT_LOSS_OF_RAGE_IS_X_SLOWER` | Inherent loss of Rage is {X}% slower |
| `INC_INHERENT_RAGE_LOSS_STARTS` | `INHERENT_RAGE_LOSS_STARTS_X_SECOND_LATER` | Inherent Rage loss starts {X} second later |
| `INC_INTIMIDATED_IMMOBILISE_THEM` | `ENEMIES_ARE_INTIMIDATED_FOR_X_SECONDS_WHEN_YOU_IMMOBILISED_THEM` | Enemies are Intimidated for {X} seconds when you Immobilised them |
| `INC_INTIMIDATED_IMMOBILISE_THEM_INTIMIDATE` | `ENEMIES_ARE_INTIMIDATED_FOR_X_SECONDS_WHEN_YOU_IMMOBILISED_THEM` | Enemies are Intimidated for {X} seconds when you Immobilised them |
| `INC_INVOCATED_ENERGY` | `INVOCATED_SKILLS_HAVE_X_INCREASED_MAX_ENERGY` | Invocated skills have {X}% increased Max Energy |
| `INC_JAGGED_GROUND_DURATION` | `X_CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND` | {X}% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground |
| `INC_JAGGED_GROUND_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_JAGGED_GROUND_YOU_CREATE` | {X}% increased Buff Magnitude of Jagged Ground you create |
| `INC_KNOCKBACK` | `X_INCREASED_KNOCKBACK_DISTANCE` | {X}% increased Knockback Distance |
| `INC_KNOCKS_BACK_GET_CRITICAL` | `KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF` | Knocks Back Enemies if you get a Critical Hit with a Quarterstaff |
| `INC_KNOCKS_BACK_GET_CRITICAL_KNOCKBACK` | `KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF` | Knocks Back Enemies if you get a Critical Hit with a Quarterstaff |
| `INC_LEECH_HEALTH` | `X_OF_THORNS_DAMAGE_LEECHED_AS_HEALTH` | {X}% of Thorns Damage Leeched as Health |
| `INC_LEECH_RATE` | `X_INCREASED_AMOUNT_OF_HEALTH_HEALTH_LEECH` | {X}% increased amount of Health Health Leech |
| `INC_LIFE_BLOCK` | `X_HEALTH_GAIN_WHEN_YOU_BLOCK` | {X} Health gain when you Block |
| `INC_LIFE_FLASKS_CHARGES` | `HEALTH_FLASKS_GAIN_X_CHARGES_PER_SECOND` | Health Flasks gain {X} charges per Second |
| `INC_LIFE_KILLED` | `GAIN_X_HEALTH_PER_ENEMY_KILLED` | gain {X} Health per enemy killed |
| `INC_LIFE_RECOVERY_FLASKS_COMPANIONS` | `HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS` | Health Recovery from your Flasks also applies to your Companions |
| `INC_LIFE_RECOVERY_FLASKS_COMPANIONS_FLASK` | `HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS` | Health Recovery from your Flasks also applies to your Companions |
| `INC_LIFE_REGEN` | `X_INCREASED_MANA_REGENERATION_RATE` | {X}% increased Mana Regeneration Rate |
| `INC_LIGHTNING_RESISTANCE` | `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCE` | Damage Penetration {X}% Lightning Resistance |
| `INC_LIGHT_DAMAGE` | `X_INCREASED_LIGHTNING_DAMAGE` | {X}% increased Lightning Damage |
| `INC_LIGHT_RADIUS` | `X_INCREASED_LIGHT_RADIUS` | {X}% increased Light Radius |
| `INC_LOSE_COMBO_SLOWER` | `SKILLS_GAIN_COMBO_X_SLOWER` | Skills gain Combo {X}% slower |
| `INC_LOSE_LIFE_ENERGY_SHIELD` | `HEALTH_LOSS_X_OF_MAX_HEALTH_AND_ENERGY_SHIELD_WHEN_YOU_USE_A_CHAOS_SKILL` | Health Loss {X}% of Max Health and Energy Shield when you use a Chaos Skill |
| `INC_LOSE_LIFE_ENERGY_SHIELD_LIFELOSS` | `HEALTH_LOSS_X_OF_MAX_HEALTH_AND_ENERGY_SHIELD_WHEN_YOU_USE_A_CHAOS_SKILL` | Health Loss {X}% of Max Health and Energy Shield when you use a Chaos Skill |
| `INC_MACE_DAMAGE` | `X_INCREASED_DAMAGE_WITH_MACES` | {X}% increased Damage with Maces |
| `INC_MAGNITUDE_IGNITE_POISONED` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_AGAINST_POISONED_ENEMIES` | {X}% increased Buff Magnitude of Ignite against Poisoned enemies |
| `INC_MAGNITUDE_IGNITE_POISONED_BUFFMAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_IGNITE_AGAINST_POISONED_ENEMIES` | {X}% increased Buff Magnitude of Ignite against Poisoned enemies |
| `INC_MAGNITUDE_IMPALES_INFLICTED_SPELLS` | `X_INCREASED_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS` | {X}% increased Buff Magnitude of Impales inflicted with Spells |
| `INC_MAIMED_BECOMING_UNPINNED` | `ENEMIES_ARE_MAIMED_FOR_X_SECONDS_AFTER_BECOMING_UNPINNED` | Enemies are Maimed for {X} seconds after becoming Unpinned |
| `INC_MAIMED_BECOMING_UNPINNED_AFTER_UNPINNED` | `ENEMIES_ARE_MAIMED_FOR_X_SECONDS_AFTER_BECOMING_UNPINNED` | Enemies are Maimed for {X} seconds after becoming Unpinned |
| `INC_MANA_BLOCK` | `X_MANA_GAIN_WHEN_YOU_BLOCK` | {X} Mana gain when you Block |
| `INC_MANA_FLASKS_CHARGES` | `MANA_FLASKS_GAIN_X_CHARGES_PER_SECOND` | Mana Flasks gain {X} charges per Second |
| `INC_MANA_REGENERATION_RATE` | `X_INCREASED_MANA_REGENERATION_RATE` | {X}% increased Mana Regeneration Rate |
| `INC_MANA_TAKE` | `X_OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` | {X}% of Damage is taken from Mana before Health |
| `INC_MARK_BLINDED` | `ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED` | Enemies near Enemies you Mark are Blinded |
| `INC_MARK_CANNOT_CRITICAL` | `ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS` | Enemies you Mark cannot deal Critical Hits |
| `INC_MARK_DAMAGE` | `ENEMIES_YOU_MARK_TAKE_X_INCREASED_DAMAGE` | Enemies you Mark take {X}% increased Damage |
| `INC_MARK_EFFECT` | `MARK_SKILLS_HAVE_X_INCREASED_USE_SPEED` | Mark Skills have {X}% increased Use Speed |
| `INC_MAX_ENERGY` | `X_INCREASED_MAX_ENERGY_SHIELD` | {X}% increased Max Energy Shield |
| `INC_MAX_HEALTH` | `COMPANIONS_HAVE_X_INCREASED_MAX_HEALTH` | Companions have {X}% increased Max Health |
| `INC_MAX_MANA` | `X_INCREASED_MAX_MANA` | {X}% increased Max Mana |
| `INC_MAX_RAGE` | `GAIN_X_RAGE_ON_MELEE_HIT_DAMAGE` | gain {X} Rage on Melee Hit Damage |
| `INC_MAX_SPIRIT` | `X_INCREASED_SPIRIT` | {X}% increased Spirit |
| `INC_MAX_VOLATILITY` | `X_INCREASED_VOLATILITY_EXPLOSION_DELAY` | {X}% increased Volatility Explosion delay |
| `INC_MELEE_ATTACK_NUMBER_SUMMONED` | `MELEE_ATTACK_SKILLS_HAVE_X_TO_MAX_NUMBER_OF_SUMMONED_TOTEMS` | Melee Attack Skills have +{X} to Max number of Summoned Totems |
| `INC_MELEE_ATTACK_NUMBER_SUMMONED_TOTEM` | `MELEE_ATTACK_SKILLS_HAVE_X_TO_MAX_NUMBER_OF_SUMMONED_TOTEMS` | Melee Attack Skills have +{X} to Max number of Summoned Totems |
| `INC_META_ENERGY` | `META_SKILLS_GAIN_X_INCREASED_ENERGY` | Meta Skills gain {X}% increased Energy |
| `INC_MINIONS_ADDITIONAL_PHYSICAL_DAMAGE` | `MINIONS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Minions have {X}% additional Physical Damage Reduction |
| `INC_MINIONS_ADDITIONAL_PHYSICAL_DAMAGE_MINION` | `MINIONS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Minions have {X}% additional Physical Damage Reduction |
| `INC_MINIONS_ATTACK_CAST_SPEED` | `MINIONS_HAVE_X_INCREASED_ATTACK_AND_CAST_SPEED` | Minions have {X}% increased Attack and Cast Speed |
| `INC_MINIONS_BREAK_ARMOUR_EQUAL` | `MINIONS_BREAK_ARMOR_EQUAL_TO_X_OF_PHYSICAL_DAMAGE_DEALT` | Minions Break Armor equal to {X}% of Physical damage dealt |
| `INC_MINIONS_BREAK_ARMOUR_EQUAL_MINION` | `MINIONS_BREAK_ARMOR_EQUAL_TO_X_OF_PHYSICAL_DAMAGE_DEALT` | Minions Break Armor equal to {X}% of Physical damage dealt |
| `INC_MINIONS_CAST_SPEED` | `MINIONS_HAVE_X_INCREASED_CAST_SPEED` | Minions have {X}% increased Cast Speed |
| `INC_MINIONS_COOLDOWN_RECOVERY_RATE` | `MINIONS_HAVE_X_INCREASED_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS` | Minions have {X}% increased Cooldown Recovery Rate for Command Skills |
| `INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND` | `MINIONS_HAVE_X_INCREASED_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS` | Minions have {X}% increased Cooldown Recovery Rate for Command Skills |
| `INC_MINIONS_EVASION_RATING` | `MINIONS_HAVE_X_INCREASED_EVASION` | Minions have {X}% increased Evasion |
| `INC_MINIONS_GIGANTIC` | `YOUR_MINIONS_ARE_GIGANTIC` | Your Minions are Gigantic |
| `INC_MINIONS_LIFE_EXTRA_ENERGY` | `MINIONS_GAIN_X_OF_THEIR_MAX_HEALTH_AS_EXTRA_MAX_ENERGY_SHIELD` | Minions gain {X}% of their Max Health as Extra Max Energy Shield |
| `INC_MINIONS_LIFE_EXTRA_ENERGY_MINION` | `MINIONS_GAIN_X_OF_THEIR_MAX_HEALTH_AS_EXTRA_MAX_ENERGY_SHIELD` | Minions gain {X}% of their Max Health as Extra Max Energy Shield |
| `INC_MINIONS_LIFE_RECOVERY_RATE` | `MINIONS_HAVE_X_INCREASED_HEALTH_RECOVERY_RATE` | Minions have {X}% increased Health Recovery rate |
| `INC_MINIONS_PHYSICAL_DAMAGE_CHAOS` | `MINIONS_GAIN_X_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE` | Minions gain {X}% of Physical Damage as Chaos Damage |
| `INC_MINIONS_PHYSICAL_DAMAGE_CHAOS_MINION` | `MINIONS_GAIN_X_OF_PHYSICAL_DAMAGE_AS_CHAOS_DAMAGE` | Minions gain {X}% of Physical Damage as Chaos Damage |
| `INC_MINIONS_REGENERATE_LIFE` | `MINIONS_REGENERATE_X_OF_MAX_HEALTH_PER_SECOND` | Minions Regenerate {X}% of Max Health per second |
| `INC_MINIONS_REVIVE_FASTER` | `MINIONS_REVIVING_X_FASTER` | Minions Reviving {X}% faster |
| `INC_MINIONS_REVIVE_FASTER_COMPANIONS` | `MINIONS_REVIVING_X_FASTER_IF_ALL_YOUR_MINIONS_ARE_COMPANIONS` | Minions Reviving {X}% faster if all your Minions are Companions |
| `INC_MINION_AREA_OF_EFFECT` | `MINIONS_HAVE_X_INCREASED_AREA_OF_EFFECT` | Minions have {X}% increased Area of Effect |
| `INC_MINION_ATTACK_SPEED` | `MINIONS_HAVE_X_INCREASED_ATTACK_SPEED` | Minions have {X}% increased Attack Speed |
| `INC_MINION_DAMAGE` | `MINIONS_DEAL_X_INCREASED_DAMAGE` | Minions deal {X}% increased Damage |
| `INC_MINION_MAX_HEALTH` | `MINIONS_HAVE_X_INCREASED_MAX_HEALTH` | Minions have {X}% increased Max Health |
| `INC_MINION_MOVEMENT_SPEED` | `MINIONS_HAVE_X_INCREASED_MOVEMENT_SPEED` | Minions have {X}% increased Movement Speed |
| `INC_MINION_PHYS_REDUCTION` | `MINIONS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Minions have {X}% additional Physical Damage Reduction |
| `INC_MINION_RESIST` | `MINIONS_HAVE_X_TO_ALL_ELEMENTAL_RESISTANCES` | Minions have +{X}% to all Elemental Resistances |
| `INC_MINION_REVIVE` | `MINIONS_REVIVING_X_FASTER` | Minions Reviving {X}% faster |
| `INC_MOVEMENT_PENALTY_REDUCTION` | `X_INCREASED_EVASION` | {X}% increased Evasion |
| `INC_MOVEMENT_SPEED` | `X_INCREASED_MOVEMENT_SPEED` | {X}% increased Movement Speed |
| `INC_NEXT_ATTACK_HEAVY_STUNNING` | `THE_NEXT_ATTACK_YOU_USE_WITHIN_X_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED` | The next Attack you use within {X} seconds after Heavy Stunning a Rare or Unique Enemy is Ancestrally Boosted |
| `INC_NEXT_ATTACK_HEAVY_STUNNING_HEAVYSTUN` | `THE_NEXT_ATTACK_YOU_USE_WITHIN_X_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED` | The next Attack you use within {X} seconds after Heavy Stunning a Rare or Unique Enemy is Ancestrally Boosted |
| `INC_NON_DAMAGING_AILMENT_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT` | {X}% increased Buff Magnitude of Non-Damaging Ailments you inflict |
| `INC_OFFERINGS_CANNOT_DAMAGED_THEY` | `OFFERINGS_CANNOT_BE_DAMAGED_IF_THEY_HAVE_BEEN_CREATED_RECENTLY` | Offerings cannot be damaged if they have been created Recently |
| `INC_OFFERINGS_CANNOT_DAMAGED_THEY_RECENTLY` | `OFFERINGS_CANNOT_BE_DAMAGED_IF_THEY_HAVE_BEEN_CREATED_RECENTLY` | Offerings cannot be damaged if they have been created Recently |
| `INC_OFFERING_DURATION` | `OFFERING_SKILLS_HAVE_X_INCREASED_BUFF_EFFECT` | Offering Skills have {X}% increased Buff effect |
| `INC_ONE_HANDED_DAMAGE` | `X_INCREASED_DAMAGE_WITH_ONE_HANDED_WEAPONS` | {X}% increased Damage with One Handed Weapons |
| `INC_ORB_LIMIT` | `ORB_SKILLS_HAVE_X_TO_LIMIT` | Orb Skills have +{X} to Limit |
| `INC_PARRIED_DURATION` | `X_INCREASED_PARRIED_DEBUFF_DURATION` | {X}% increased Parried Debuff Duration |
| `INC_PARRIED_MAGNITUDE` | `X_INCREASED_PARRIED_DEBUFF_BUFF_MAGNITUDE` | {X}% increased Parried Debuff Buff Magnitude |
| `INC_PARRY_DAMAGE` | `X_INCREASED_PARRY_DAMAGE` | {X}% increased Parry Damage |
| `INC_PARRY_RANGE` | `X_INCREASED_PARRY_RANGE` | {X}% increased Parry Range |
| `INC_PENETRATE` | `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCE` | Damage Penetration {X}% Lightning Resistance |
| `INC_PHYSICAL_DAMAGE` | `X_INCREASED_PHYSICAL_DAMAGE` | {X}% increased Physical Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_FIRE` | `X_OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE` | {X}% of Physical Damage taken as Fire Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_RANDOM` | `X_OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE` | {X}% of Physical Damage from Hit Damage taken as Damage of a Random Elemental Damage |
| `INC_PHYSICAL_DAMAGE_TAKEN_RANDOM_HITDAMAGE` | `X_OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE` | {X}% of Physical Damage from Hit Damage taken as Damage of a Random Elemental Damage |
| `INC_PHYSICAL_SHAPESHIFT_DAMAGE` | `X_INCREASED_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED` | {X}% increased Physical Damage while Shapeshifted |
| `INC_PHYSICAL_SPELL_CRITICAL_BUILD` | `PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED` | Physical Spell Critical Hit Damage build Pinned |
| `INC_PHYSICAL_SPELL_CRITICAL_BUILD_HITDAMAGE` | `PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED` | Physical Spell Critical Hit Damage build Pinned |
| `INC_PHYSICAL_THORNS_DAMAGE_EQUAL` | `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_X_OF_ITEM_ARMOR_ON_EQUIPPED_BODY_ARMOR` | gain Physical Thorns damage equal to {X}% of Item Armor on Equipped Body Armor |
| `INC_PHYSICAL_THORNS_DAMAGE_EQUAL_BODY_ARMOUR` | `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_X_OF_ITEM_ARMOR_ON_EQUIPPED_BODY_ARMOR` | gain Physical Thorns damage equal to {X}% of Item Armor on Equipped Body Armor |
| `INC_PHYSICAL_THORNS_DAMAGE_EQUAL_SHAPESHIFT` | `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_X_OF_MAX_HEALTH_WHILE_SHAPESHIFTED` | gain Physical Thorns damage equal to {X}% of Max Health while Shapeshifted |
| `INC_PINNED_CANNOT_CRITICAL` | `PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS` | Pinned Enemies cannot deal Critical Hits |
| `INC_PIN_BUILDUP` | `X_INCREASED_PINNED_BUILDUP` | {X}% increased Pinned Buildup |
| `INC_PIN_DURATION` | `X_INCREASED_PINNED_DURATION` | {X}% increased Pinned duration |
| `INC_POISON_DURATION` | `X_INCREASED_POISON_DURATION` | {X}% increased Poison Duration |
| `INC_POISON_DURATION_TAKEN` | `X_INCREASED_POISON_DURATION_ON_YOU` | {X}% increased Poison Duration on you |
| `INC_POISON_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT` | {X}% increased Buff Magnitude of Poison you inflict |
| `INC_POWER_CHARGE_ELEMENTAL_INFUSION` | `GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION` | gain a Power Charge when you consume an Elemental Infusion |
| `INC_POWER_CHARGE_ELEMENTAL_INFUSION_CHARGES` | `GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION` | gain a Power Charge when you consume an Elemental Infusion |
| `INC_PREVENT_DAMAGE_DEFLECTED` | `PREVENT_X_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE` | Prevent +{X}% of Damage from Deflected Hit Damage |
| `INC_PREVENT_DAMAGE_DEFLECTED_CRITICAL` | `PREVENT_X_OF_DAMAGE_FROM_DEFLECTED_CRITICAL_HIT_DAMAGE` | Prevent +{X}% of Damage from Deflected Critical Hit Damage |
| `INC_PREVENT_DAMAGE_DEFLECTED_CRITICAL_DEFLECT` | `PREVENT_X_OF_DAMAGE_FROM_DEFLECTED_CRITICAL_HIT_DAMAGE` | Prevent +{X}% of Damage from Deflected Critical Hit Damage |
| `INC_PREVENT_DAMAGE_DEFLECTED_DEFLECT` | `PREVENT_X_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE` | Prevent +{X}% of Damage from Deflected Hit Damage |
| `INC_PROJECTILES_PIERCE_FULLY_BROKEN` | `PROJECTILES_PIERCE_ENEMIES_WITH_FULLY_BROKEN_ARMOR` | Projectiles Pierce enemies with Fully Broken Armor |
| `INC_PROJECTILES_PIERCE_FULLY_BROKEN_PROJECTILE` | `PROJECTILES_PIERCE_ENEMIES_WITH_FULLY_BROKEN_ARMOR` | Projectiles Pierce enemies with Fully Broken Armor |
| `INC_PROJECTILE_DAMAGE` | `X_INCREASED_PROJECTILE_DAMAGE` | {X}% increased Projectile Damage |
| `INC_PROJECTILE_PIERCE` | `X_CHANCE_TO_PIERCE_AN_ENEMY` | {X}% chance to Pierce an Enemy |
| `INC_PROJECTILE_SPEED` | `X_INCREASED_PROJECTILE_SPEED` | {X}% increased Projectile Speed |
| `INC_PUPPET_MASTER_DURATION` | `X_INCREASED_EFFECT_OF_PUPPET_MASTER` | {X}% increased Effect of Puppet Master |
| `INC_QUARTERSTAFF_POWER_CHARGES_COUNT` | `QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE` | Quarterstaff Skills that consume Power Charges count as consuming an additional Power Charge |
| `INC_QUARTERSTAFF_POWER_CHARGES_COUNT_CHARGE` | `QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE` | Quarterstaff Skills that consume Power Charges count as consuming an additional Power Charge |
| `INC_QUIVER_BONUS` | `X_INCREASED_BONUSES_GAIN_FROM_EQUIPPED_QUIVER` | {X}% increased bonuses gain from Equipped Quiver |
| `INC_QUIVER_BONUSES` | `X_INCREASED_BONUSES_GAIN_FROM_EQUIPPED_QUIVER` | {X}% increased bonuses gain from Equipped Quiver |
| `INC_RAGE` | `GAIN_X_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY` | gain {X} Rage when Hit Damage by an Enemy |
| `INC_RAGE_IGNITES_TARGET` | `GAIN_X_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET` | gain {X} Rage when your Hit Damage Ignites a target |
| `INC_RAGE_IGNITES_TARGET_HITDAMAGE` | `GAIN_X_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET` | gain {X} Rage when your Hit Damage Ignites a target |
| `INC_RAGE_LIFE_FLASK` | `GAIN_X_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK` | gain {X} Rage when you use a Health Flask |
| `INC_RAGE_LOSS_GRACE` | `INHERENT_RAGE_LOSS_STARTS_X_SECOND_LATER` | Inherent Rage loss starts {X} second later |
| `INC_RAGE_MELEE_AXE` | `GAIN_X_RAGE_ON_MELEE_AXE_HIT_DAMAGE` | gain {X} Rage on Melee Axe Hit Damage |
| `INC_RAGE_MELEE_AXE_HITDAMAGE` | `GAIN_X_RAGE_ON_MELEE_AXE_HIT_DAMAGE` | gain {X} Rage on Melee Axe Hit Damage |
| `INC_RARITY_DAMAGE` | `X_INCREASED_ATTACK_SPEED_WHILE_A_RARE_OR_UNIQUE_ENEMY_IS_IN_YOUR_PRESENCE` | {X}% increased Attack Speed while a Rare or Unique Enemy is in your Presence |
| `INC_RECOVERY_RATE` | `X_INCREASED_SPEED_OF_RECOUP_EFFECTS` | {X}% increased speed of Recoup Effects |
| `INC_REGENERATE_LIFE` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND` | Regenerate {X}% of Max Health per second |
| `INC_REGENERATE_LIFE_AFFECTED_ANY` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT` | Regenerate {X}% of Max Health per second while affected by any Damaging Ailment |
| `INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT` | Regenerate {X}% of Max Health per second while affected by any Damaging Ailment |
| `INC_REGENERATE_LIFE_LOW` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_ON_LOW_HEALTH` | Regenerate {X}% of Max Health per second while on Low Health |
| `INC_REGENERATE_LIFE_LOW_WHILE_ON` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_ON_LOW_HEALTH` | Regenerate {X}% of Max Health per second while on Low Health |
| `INC_REGENERATE_LIFE_OVER_STUNNED` | `REGENERATE_X_OF_MAX_HEALTH_OVER_X_SECOND_WHEN_STUNNED` | Regenerate {X}% of Max Health over {X} second when Stunned |
| `INC_REGENERATE_LIFE_OVER_STUNNED_WHEN_STUNNED` | `REGENERATE_X_OF_MAX_HEALTH_OVER_X_SECOND_WHEN_STUNNED` | Regenerate {X}% of Max Health over {X} second when Stunned |
| `INC_REGENERATE_LIFE_RECENTLY` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_IF_YOU_HAVE_BEEN_HIT_RECENTLY` | Regenerate {X}% of Max Health per second if you have been Hit Recently |
| `INC_REGENERATE_LIFE_STATIONARY` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_STATIONARY` | Regenerate {X}% of Max Health per second while stationary |
| `INC_REGENERATE_LIFE_SURROUNDED` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_SURROUNDED` | Regenerate {X}% of Max Health per second while Surrounded |
| `INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_SURROUNDED` | Regenerate {X}% of Max Health per second while Surrounded |
| `INC_REGENERATE_LIFE_TOTEM` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_YOU_HAVE_A_TOTEM` | Regenerate {X}% of Max Health per second while you have a Totem |
| `INC_REGENERATE_LIFE_TOTEM_WHILE_YOU` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_WHILE_YOU_HAVE_A_TOTEM` | Regenerate {X}% of Max Health per second while you have a Totem |
| `INC_REGENERATE_LIFE_VE_FLASK` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_X_SECONDS` | Regenerate {X}% of Max Health per Second if you've used a Health Flask in the past {X} seconds |
| `INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND` | `REGENERATE_X_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_X_SECONDS` | Regenerate {X}% of Max Health per Second if you've used a Health Flask in the past {X} seconds |
| `INC_RELOAD_SPEED` | `X_INCREASED_CROSSBOW_RELOAD_SPEED` | {X}% increased Crossbow Reload Speed |
| `INC_REMNANTS_COLLECTED_FURTHER_AWAY` | `REMNANTS_CAN_BE_COLLECTED_FROM_X_FURTHER_AWAY` | Remnants can be collected from {X}% further away |
| `INC_REMNANTS_CREATE_EFFECT` | `REMNANTS_YOU_CREATE_HAVE_X_INCREASED_EFFECT` | Remnants you create have {X}% increased effect |
| `INC_REMNANT_MAGNITUDE` | `REMNANTS_YOU_CREATE_HAVE_X_INCREASED_EFFECT` | Remnants you create have {X}% increased effect |
| `INC_REMOVE_CURSE_CHANNELLING` | `ADD_A_CURSE_AFTER_CHANNELLING_FOR_X_SECONDS` | add a Curse after Channelling for {X} seconds |
| `INC_REMOVE_CURSE_MANA_FLASK` | `ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK` | add a Curse when you use a Mana Flask |
| `INC_REMOVE_IGNITE_WARCRY` | `ADD_IGNITE_WHEN_YOU_WARCRY` | add Ignite when you Warcry |
| `INC_RESERVATION_EFFICIENCY` | `X_INCREASED_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS` | {X}% increased Reservation Efficiency of Herald Skills |
| `INC_RESISTANCE_PENETRATION` | `HIT_DAMAGE_HAVE_X_CHANCE_TO_TREAT_ENEMY_MONSTER_ELEMENTAL_DAMAGE_RESISTANCES_VALUES_AS_INVERTED` | Hit Damage have {X}% chance to treat Enemy Monster Elemental Damage Resistances values as inverted |
| `INC_SEALED_SEAL_FREQUENCY` | `SEALED_SKILLS_HAVE_X_INCREASED_SEAL_GAIN_FREQUENCY` | Sealed Skills have {X}% increased Seal gain frequency |
| `INC_SHOCK_DURATION_TAKEN` | `X_INCREASED_SHOCK_DURATION_ON_YOU` | {X}% increased Shock duration on you |
| `INC_SHOCK_EFFECT_TAKEN` | `X_INCREASED_BUFF_EFFECT_OF_SHOCK_ON_YOU` | {X}% increased Buff Effect of Shock on you |
| `INC_SHOCK_MAGNITUDE` | `X_INCREASED_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT` | {X}% increased Buff Magnitude of Shock you inflict |
| `INC_SKILL_COST_LIFE` | `X_INCREASED_HEALTH_COST_OF_SKILLS` | {X}% increased Health Cost of Skills |
| `INC_SKILL_COST_MANA` | `X_INCREASED_MANA_COST_OF_SKILLS` | {X}% increased Mana Cost of Skills |
| `INC_SKILL_EFFECT_DURATION` | `X_INCREASED_SKILL_EFFECT_DURATION` | {X}% increased Skill Effect Duration |
| `INC_SKILL_SPEED` | `X_INCREASED_SKILL_SPEED` | {X}% increased Skill Speed |
| `INC_SLOWER_START_ENERGY_SHIELD` | `X_SLOWER_START_OF_ENERGY_SHIELD_RECHARGE` | {X}% slower start of Energy Shield Recharge |
| `INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION` | `X_SLOWER_START_OF_ENERGY_SHIELD_RECHARGE` | {X}% slower start of Energy Shield Recharge |
| `INC_SLOW_POTENCY` | `X_INCREASED_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU` | {X}% increased Slowing Potency of Debuffs on You |
| `INC_SPEAR_DAMAGE` | `X_INCREASED_DAMAGE_WITH_SPEARS` | {X}% increased Damage with Spears |
| `INC_SPELL_DAMAGE` | `X_INCREASED_ATTACK_DAMAGE` | {X}% increased Attack Damage |
| `INC_STRENGTH` | `X_INCREASED_ATTRIBUTES` | {X}% increased Attributes |
| `INC_STRIKES_SPLASH_DAMAGE` | `STRIKES_DEAL_SPLASH_DAMAGE` | Strikes deal Splash Damage |
| `INC_STUN_BUILDUP` | `X_INCREASED_STUN_BUILDUP` | {X}% increased Stun Buildup |
| `INC_STUN_DURATION` | `X_INCREASED_STUN_BUILDUP` | {X}% increased Stun Buildup |
| `INC_STUN_RECOVERY` | `X_INCREASED_STUN_RECOVERY` | {X}% increased Stun Recovery |
| `INC_STUN_THRESHOLD` | `X_INCREASED_STUN_THRESHOLD` | {X}% increased Stun Threshold |
| `INC_STUN_THRESHOLD_EQUAL_LOWEST` | `GAIN_STUN_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_HELMET` | gain Stun Threshold equal to the lowest of Evasion and Armor on your Helmet |
| `INC_STUN_THRESHOLD_EQUAL_LOWEST_STUNTHRESHOLD` | `GAIN_STUN_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_HELMET` | gain Stun Threshold equal to the lowest of Evasion and Armor on your Helmet |
| `INC_STUN_THRESHOLD_IS_BASED` | `STUN_THRESHOLD_IS_BASED_ON_X_OF_YOUR_ENERGY_SHIELD_INSTEAD_OF_HEALTH` | Stun Threshold is based on {X}% of your Energy Shield instead of Health |
| `INC_STUN_THRESHOLD_IS_BASED_STUNTHRESHOLD` | `STUN_THRESHOLD_IS_BASED_ON_X_OF_YOUR_ENERGY_SHIELD_INSTEAD_OF_HEALTH` | Stun Threshold is based on {X}% of your Energy Shield instead of Health |
| `INC_SWORD_DAMAGE` | `X_INCREASED_DAMAGE_WITH_SWORDS` | {X}% increased Damage with Swords |
| `INC_TAKEN_DAMAGE` | `X_INCREASED_MAX_ENERGY_SHIELD` | {X}% increased Max Energy Shield |
| `INC_TARGETS_AFFECTED_POISONS_SAME` | `TARGETS_CAN_BE_AFFECTED_BY_X_OF_YOUR_POISONS_AT_THE_SAME_TIME` | Targets can be affected by +{X} of your Poisons at the same time |
| `INC_TARGETS_AFFECTED_POISONS_SAME_POISONED` | `TARGETS_CAN_BE_AFFECTED_BY_X_OF_YOUR_POISONS_AT_THE_SAME_TIME` | Targets can be affected by +{X} of your Poisons at the same time |
| `INC_THORNS` | `X_INCREASED_THORNS_DAMAGE` | {X}% increased Thorns damage |
| `INC_THORNS_DAMAGE_LEECHED_LIFE` | `X_OF_THORNS_DAMAGE_LEECHED_AS_HEALTH` | {X}% of Thorns Damage Leeched as Health |
| `INC_TOTEMS_ADDITIONAL_PHYSICAL_DAMAGE` | `TOTEMS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Totems have {X}% additional Physical Damage Reduction |
| `INC_TOTEMS_ADDITIONAL_PHYSICAL_DAMAGE_TOTEM` | `TOTEMS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Totems have {X}% additional Physical Damage Reduction |
| `INC_TOTEMS_ELEMENTAL_RESISTANCES` | `TOTEMS_GAIN_X_TO_ALL_MAX_ELEMENTAL_RESISTANCES` | Totems gain +{X}% to all Max Elemental Resistances |
| `INC_TOTEMS_REGENERATE_LIFE` | `TOTEMS_REGENERATE_X_OF_MAX_HEALTH_PER_SECOND` | Totems Regenerate {X}% of Max Health per second |
| `INC_TOTEM_DAMAGE` | `X_INCREASED_TOTEM_DAMAGE` | {X}% increased Totem Damage |
| `INC_TOTEM_LIFE` | `X_INCREASED_TOTEM_HEALTH` | {X}% increased Totem Health |
| `INC_TOTEM_PHYS_REDUCTION` | `TOTEMS_HAVE_X_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION` | Totems have {X}% additional Physical Damage Reduction |
| `INC_TOTEM_PLACEMENT_RANGE` | `X_INCREASED_TOTEM_PLACEMENT_RANGE` | {X}% increased Totem Placement range |
| `INC_TOTEM_PLACEMENT_SPEED` | `X_INCREASED_TOTEM_PLACEMENT_SPEED` | {X}% increased Totem Placement speed |
| `INC_TRAP_DAMAGE` | `X_INCREASED_TRAP_DAMAGE` | {X}% increased Trap Damage |
| `INC_TRAP_THROWING_SPEED` | `X_INCREASED_TRAP_THROWING_SPEED` | {X}% increased Trap Throwing Speed |
| `INC_TRAP_THROW_SPEED` | `X_INCREASED_TRAP_THROWING_SPEED` | {X}% increased Trap Throwing Speed |
| `INC_TWO_HANDED_DAMAGE` | `X_INCREASED_DAMAGE_WITH_TWO_HANDED_WEAPONS` | {X}% increased Damage with Two Handed Weapons |
| `INC_UNAFFECTED_CHILLED_GROUND` | `UNAFFECTED_BY_CHILLED_GROUND` | Unaffected by Chilled Ground |
| `INC_UNAFFECTED_CHILL_DURING_DODGE` | `UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL` | Unaffected by Chill during Dodge Roll |
| `INC_UNAFFECTED_CHILL_LEECHING_MANA` | `UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA` | Unaffected by Chill while Mana Leech Mana |
| `INC_UNAFFECTED_ELEMENTAL_WEAKNESS` | `UNAFFECTED_BY_ELEMENTAL_WEAKNESS` | Unaffected by Elemental Weakness |
| `INC_UNAFFECTED_SHOCKED_GROUND` | `UNAFFECTED_BY_SHOCKED_GROUND` | Unaffected by Shocked Ground |
| `INC_UNWITHERED_WITHERED_THEY_ENTER` | `UNWITHERED_ENEMIES_ARE_WITHERED_FOR_X_SECONDS_WHEN_THEY_ENTER_YOUR_PRESENCE` | Unwithered enemies are Withered for {X} seconds when they enter your Presence |
| `INC_UNWITHERED_WITHERED_THEY_ENTER_PRESENCE` | `UNWITHERED_ENEMIES_ARE_WITHERED_FOR_X_SECONDS_WHEN_THEY_ENTER_YOUR_PRESENCE` | Unwithered enemies are Withered for {X} seconds when they enter your Presence |
| `INC_VOLATILITY_GENERATION_GLOBAL` | `X_CHANCE_TO_GAIN_VOLATILITY_WHEN_YOU_ARE_STUNNED` | {X}% chance to gain Volatility when you are Stunned |
| `INC_VOLATILITY_IS` | `MAX_VOLATILITY_IS_X` | Max Volatility is {X} |
| `INC_WARCRIES_DEBILITATE` | `WARCRIES_DEBILITATE_ENEMIES` | Warcries Debilitate Enemies |
| `INC_WARCRIES_EMPOWER_ADDITIONAL_ATTACK` | `WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK` | Warcries Empowered an additional Attack |
| `INC_WARCRIES_INFLICT_CRITICAL_WEAKNESS` | `WARCRIES_INFLICT_X_CRITICAL_WEAKNESS_ON_ENEMIES` | Warcries inflict {X} Critical Weakness on Enemies |
| `INC_WARCRIES_INFLICT_CRITICAL_WEAKNESS_WARCRY` | `WARCRIES_INFLICT_X_CRITICAL_WEAKNESS_ON_ENEMIES` | Warcries inflict {X} Critical Weakness on Enemies |
| `INC_WARCRIES_MINIMUM_POWER` | `WARCRIES_HAVE_A_MINIMUM_OF_X_POWER` | Warcries have a minimum of {X} Power |
| `INC_WARCRY_DAMAGE` | `X_INCREASED_DAMAGE_WITH_WARCRIES` | {X}% increased Damage with Warcries |
| `INC_WARCRY_POWER` | `X_INCREASED_TOTAL_POWER_COUNTED_BY_WARCRIES` | {X}% increased total Power counted by Warcries |
| `INC_WITHERED_EFFECT_TAKEN` | `X_INCREASED_BUFF_EFFECT_OF_WITHERED_ON_YOU` | {X}% increased Buff Effect of Withered on you |
| `INC_WITHERED_MAGNITUDE` | `X_INCREASED_WITHERED_BUFF_MAGNITUDE` | {X}% increased Withered Buff Magnitude |
| `INC_WOULD_CHARGE_ALLIES_IN` | `IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD` | If you would gain a Charges, Allies in your Presence gain that Charges instead |
| `INC_WOULD_CHARGE_ALLIES_IN_CHARGES` | `IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD` | If you would gain a Charges, Allies in your Presence gain that Charges instead |
| `IRON_REFLEXES_EVASION_TO_ARMOR` | `STAT_CONVERSION_ALL_EVASION_TO_ARMOR` | Stat Conversion all Evasion to Armor |
| `LEECH_LIFE_FASTER` | `HEALTH_LEECH_HEALTH_X_FASTER` | Health Leech Health {X}% faster |
| `LEECH_LIFE_SLOWER` | `HEALTH_LEECH_HEALTH_X_SLOWER` | Health Leech Health {X}% slower |
| `LIFE_CONVERTED_ENERGY_SHIELD` | `X_OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD` | {X}% of Max Health Stat Conversion to Energy Shield |
| `LIFE_FLASKS_GUARD_EQUAL` | `HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD_FOR_X_SECONDS_EQUAL_TO_X_OF_THE_HEALTH_RECOVERY_PER_SECOND_THEY_APPLY` | Health Flasks applied to you grant Guard for {X} seconds equal to {X}% of the Health Recovery per Second they apply |
| `LIFE_FLASKS_GUARD_EQUAL_PER_SECOND` | `HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD_FOR_X_SECONDS_EQUAL_TO_X_OF_THE_HEALTH_RECOVERY_PER_SECOND_THEY_APPLY` | Health Flasks applied to you grant Guard for {X} seconds equal to {X}% of the Health Recovery per Second they apply |
| `LIFE_PER_KILL` | `GAIN_X_HEALTH_PER_ENEMY_KILLED` | gain {X} Health per enemy killed |
| `MACE_DAMAGE` | `X_MORE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES_WITH_MACES` | {X}% more Damage against Heavy Stunned Enemies with Maces |
| `MANA_COST_TO_LIFE` | `X_OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` | {X}% of Skill Mana Costs Stat Conversion to Health Costs |
| `MARK_USE_SPEED` | `MARK_SKILLS_HAVE_X_INCREASED_USE_SPEED` | Mark Skills have {X}% increased Use Speed |
| `MELEE_DAMAGE` | `X_INCREASED_MELEE_DAMAGE` | {X}% increased Melee Damage |
| `META_ENERGY` | `META_SKILLS_GAIN_X_INCREASED_ENERGY` | Meta Skills gain {X}% increased Energy |
| `MINIONS_RECOUP_DAMAGE_TAKEN` | `MINIONS_RECOUP_X_OF_DAMAGE_TAKEN_AS_HEALTH` | Minions Recoup {X}% of Damage taken as Health |
| `MINION_ATTACK_SPEED` | `MINIONS_HAVE_X_INCREASED_ATTACK_AND_CAST_SPEED` | Minions have {X}% increased Attack and Cast Speed |
| `MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS` | `MINIONS_GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` | Minions gain {X}% of Elemental Damage as Extra Chaos Damage |
| `MINION_DAMAGE` | `MINIONS_DEAL_X_INCREASED_DAMAGE` | Minions deal {X}% increased Damage |
| `MINION_MOVEMENT_SPEED` | `X_INCREASED_MOVEMENT_SPEED` | {X}% increased Movement Speed |
| `MINION_RECOUP_HEALTH` | `MINIONS_GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` | Minions gain {X}% of Elemental Damage as Extra Chaos Damage |
| `MORE_ARCANE_SURGE_LIFE_REGENERATION` | `ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE` | Arcane Surge grants more Health Regeneration Rate instead of Mana Regeneration Rate |
| `MORE_ARCANE_SURGE_LIFE_REGENERATION_ARCANESURGE` | `ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE` | Arcane Surge grants more Health Regeneration Rate instead of Mana Regeneration Rate |
| `MORE_BLEEDING_MAGNITUDE_INFLICTED` | `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` | {X}% increased Buff Magnitude of Bleeding you inflict |
| `MORE_CAST_SPEED` | `X_MORE_SKILL_SPEED_WHILE_OFF_HAND_IS_EMPTY_AND_YOU_HAVE_A_ONE_HANDED_MARTIAL_WEAPON_EQUIPPED_IN_YOUR_MAIN_HAND` | {X}% more Skill Speed while Off Hand is empty and you have a One-Handed Martial Weapon equipped in your Main Hand |
| `OFFERING_LIFE` | `RECOVER_X_OF_MAX_HEALTH_WHEN_YOU_CREATE_AN_OFFERING` | Recover {X}% of Max Health when you create an Offering |
| `PENETRATE_COLD` | `DAMAGE_PENETRATION_X_COLD_RESISTANCE` | Damage Penetration {X}% Cold Resistance |
| `PENETRATE_FIRE` | `DAMAGE_PENETRATION_X_FIRE_RESISTANCE` | Damage Penetration {X}% Fire Resistance |
| `PENETRATE_LIGHTNING` | `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCE` | Damage Penetration {X}% Lightning Resistance |
| `PHYSICAL_TAKEN_AS_FIRE` | `X_OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE` | {X}% of Physical Damage taken as Fire Damage |
| `PHYSICAL_TAKEN_AS_RANDOM_ELEMENT` | `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_WITH_ATTACKS` | {X}% increased Elemental Damage Damage with Attacks |
| `PINNED_ENEMIES_NO_CRIT` | `PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS` | Pinned Enemies cannot deal Critical Hits |
| `RAGE_ON_HIT` | `GAIN_X_RAGE_ON_MELEE_HIT_DAMAGE` | gain {X} Rage on Melee Hit Damage |
| `RAGE_ON_HIT_TAKEN` | `GAIN_X_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY` | gain {X} Rage when Hit Damage by an Enemy |
| `RAGE_ON_IGNITE` | `GAIN_X_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET` | gain {X} Rage when your Hit Damage Ignites a target |
| `RAGE_SHAPESHIFTING_HUMAN_FORM` | `CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_X_OF_MAX_HEALTH_PER_RAGE_CONSUMED` | Consume all Rage when Shapeshifting to Human form to recover {X}% of Max Health per Rage Consumed |
| `RECOUP_DAMAGE_TAKEN_TOTEMS` | `RECOUP_X_OF_DAMAGE_TAKEN_BY_YOUR_TOTEMS_AS_HEALTH` | Recoup {X}% of damage taken by your Totems as Health |
| `RECOUP_ENERGY` | `X_OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD` | {X}% of Elemental Damage taken Recouped as Energy Shield |
| `RECOVER_LIFE_BLOCK` | `RECOVER_X_HEALTH_WHEN_YOU_BLOCK` | Recover {X} Health when you Block |
| `RECOVER_LIFE_COLLECT_REMNANT` | `RECOVER_X_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT` | Recover {X}% of Max Health when you collect a Remnant |
| `RECOVER_LIFE_ENDURANCE_CHARGE` | `RECOVER_X_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED` | Recover {X}% of Max Health for each Endurance Charge consumed |
| `RECOVER_LIFE_GLORY` | `RECOVER_X_OF_MAX_HEALTH_PER_GLORY_CONSUMED` | Recover {X}% of Max Health per Glory consumed |
| `RECOVER_LIFE_HEAVY_STUN` | `RECOVER_X_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY` | Recover {X}% of Max Health when you Heavy Stun a Rare or Unique Enemy |
| `RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN` | `RECOVER_X_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY` | Recover {X}% of Max Health when you Heavy Stun a Rare or Unique Enemy |
| `RECOVER_LIFE_KILLING_POISONED` | `RECOVER_X_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY` | Recover {X}% of Max Health on Killing a Poisoned Enemy |
| `RECOVER_LIFE_KILLING_POISONED_POISON` | `RECOVER_X_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY` | Recover {X}% of Max Health on Killing a Poisoned Enemy |
| `RECOVER_LIFE_MANA_FLASK` | `RECOVER_X_OF_MAX_HEALTH_WHEN_YOU_USE_A_MANA_FLASK` | Recover {X}% of Max Health when you use a Mana Flask |
| `RECOVER_LIFE_MANA_WARCRY` | `RECOVER_X_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY` | Recover {X}% of Max Health and Mana when you use a Warcry |
| `RECOVER_LIFE_MINIONS_IS` | `RECOVER_X_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING` | Recover {X}% of Max Health when one of your Minions is Reviving |
| `RECOVER_LIFE_MINIONS_IS_MINION` | `RECOVER_X_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING` | Recover {X}% of Max Health when one of your Minions is Reviving |
| `RECOVER_MANA_CHARM_IS` | `RECOVER_X_OF_MAX_MANA_WHEN_A_CHARM_IS_USED` | Recover {X}% of Max Mana when a Charm is used |
| `RECOVER_MANA_COLLECT_REMNANT` | `RECOVER_X_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT` | Recover {X}% of Max Mana when you collect a Remnant |
| `RECOVER_MANA_KILL` | `RECOVER_X_OF_MAX_MANA_ON_KILL` | Recover {X}% of Max Mana on Kill |
| `RECOVER_MANA_POWER_CHARGE` | `RECOVER_X_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE` | Recover {X}% of Max Mana when you consume a Power Charge |
| `RECOVER_PCT_HEALTH` | `X_INCREASED_ARMOR` | {X}% increased Armor |
| `RECOVER_PCT_HEALTH_ON_KILL` | `X_TO_INTELLIGENCE` | +{X} to Intelligence |
| `RECOVER_PCT_MANA` | `X_CHANCE_FOR_CHARMS_YOU_USE_TO_NOT_CONSUME_CHARGES` | {X}% chance for Charms you use to not consume Charges |
| `RECOVER_PCT_MANA_ON_KILL` | `X_INCREASED_MANA_REGENERATION_RATE` | {X}% increased Mana Regeneration Rate |
| `RECOVER_PCT_MANA_ON_POWER_CHARGE` | `X_INCREASED_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY` | {X}% increased Critical Damage Bonus if you've consumed a Power Charge Recently |
| `SEAL_GAIN_SPEED` | `SEALED_SKILLS_HAVE_X_INCREASED_SEAL_GAIN_FREQUENCY` | Sealed Skills have {X}% increased Seal gain frequency |
| `TOTEM_RECOUP_HEALTH` | `EACH_TOTEM_APPLIES_X_INCREASED_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE` | Each Totem applies {X}% increased Damage taken to Enemies in their Presence |
| `TOTEM_TAKEN_AURA` | `EACH_TOTEM_APPLIES_X_INCREASED_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE` | Each Totem applies {X}% increased Damage taken to Enemies in their Presence |
| `TRAP_DAMAGE` | `X_INCREASED_TRAP_DAMAGE` | {X}% increased Trap Damage |
| `UNEVADABLE_VS_PINNED` | `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_IMMOBILISED_ENEMIES` | {X}% increased Critical Hit Chance against Immobilised enemies |
| `WARCRY_SPEED` | `X_INCREASED_WARCRY_SPEED` | {X}% increased Warcry Speed |

---

## §C 失配且无引用（可废弃）


---

## §D 新条目尚无旧实现（引擎待实现）

- `X_TO_ACCURACY_AGAINST_BLEEDING_ENEMIES` — +{X} to Accuracy against Bleeding Enemies
- `X_TO_ARMOR_PER_X_ITEM_ENERGY_SHIELD_ON_EQUIPPED_BOOTS` — +{X} to Armor per {X} Item Energy Shield on Equipped Boots
- `X_TO_ARMOR_WHILE_FROZEN` — +{X} to Armor while Frozen
- `X_TO_DEXTERITY_AND_INTELLIGENCE` — +{X} to Dexterity and Intelligence
- `X_TO_EVASION_PER_X_ITEM_ARMOR_ON_EQUIPPED_GLOVES` — +{X} to Evasion per {X} Item Armor on Equipped Gloves
- `X_TO_EVASION_PER_X_ITEM_ENERGY_SHIELD_ON_EQUIPPED_HELMET` — +{X} to Evasion per {X} Item Energy Shield on Equipped Helmet
- `X_TO_MAX_ENERGY_SHIELD_PER_X_ITEM_ARMOR_ON_EQUIPPED_HELMET` — +{X} to Max Energy Shield per {X} Item Armor on Equipped Helmet
- `X_TO_MAX_ENERGY_SHIELD_PER_X_ITEM_EVASION_ON_EQUIPPED_BODY_ARMOR` — +{X} to Max Energy Shield per {X} Item Evasion on Equipped Body Armor
- `X_TO_MAX_RAGE_WHILE_WIELDING_AN_AXE` — +{X} to Max Rage while wielding an Axe
- `X_TO_STRENGTH_AND_DEXTERITY` — +{X} to Strength and Dexterity
- `X_TO_STRENGTH_AND_INTELLIGENCE` — +{X} to Strength and Intelligence
- `X_TO_ALL_ATTRIBUTES` — +{X} to all Attributes
- `X_TO_ANY_ATTRIBUTES` — +{X} to any Attributes
- `X_TO_MAX_COLD_INFUSIONS` — +{X} to Max Cold Infusions
- `X_TO_MAX_ENERGY_SHIELD` — +{X} to Max Energy Shield
- `X_TO_MAX_FIRE_INFUSIONS` — +{X} to Max Fire Infusions
- `X_TO_MAX_RAGE_WHILE_SHAPESHIFTED` — +{X} to Max Rage while Shapeshifted
- `X_OF_ARMOR_ALSO_APPLIES_TO_CHAOS_DAMAGE` — +{X}% of Armor also applies to Chaos Damage
- `X_TO_FIRE_RESISTANCE` — +{X}% to Fire Resistance
- `X_TO_MAX_COLD_RESISTANCE` — +{X}% to Max Cold Resistance
- `X_TO_MAX_COLD_RESISTANCE_WHILE_SHAPESHIFTED` — +{X}% to Max Cold Resistance while Shapeshifted
- `X_TO_MAX_FIRE_RESISTANCE_WHILE_IGNITED` — +{X}% to Max Fire Resistance while Ignited
- `X_TO_MAX_FIRE_RESISTANCE_WHILE_SHAPESHIFTED` — +{X}% to Max Fire Resistance while Shapeshifted
- `X_TO_MAX_LIGHTNING_RESISTANCE` — +{X}% to Max Lightning Resistance
- `X_TO_MAX_LIGHTNING_RESISTANCE_WHILE_SHAPESHIFTED` — +{X}% to Max Lightning Resistance while Shapeshifted
- `X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_2` — -{X}% to all Elemental Damage Resistances
- `ACCURACY_IS_DOUBLED` — Accuracy is Doubled
- `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` — All Damage Types is taken from Mana before Health
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_TO_ACCURACY` — Allies in your Presence have +{X} to Accuracy
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_ATTACK_SPEED` — Allies in your Presence have {X}% increased Attack Speed
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_CAST_SPEED` — Allies in your Presence have {X}% increased Cast Speed
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE` — Allies in your Presence have {X}% increased Critical Hit Chance
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_GLORY_GENERATION` — Allies in your Presence have {X}% increased Glory generation
- `ANCESTRALLY_BOOSTED_ATTACKS_DEAL_X_INCREASED_DAMAGE` — Ancestrally Boosted Attacks deal {X}% increased Damage
- `ARCHON_BUFFS_ALSO_GRANT_X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES` — Archon Buffs also grant +{X}% to all Elemental Damage Resistances
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_CRITICAL_DAMAGE_BONUS` — Archon Buffs also grant {X}% increased Critical Damage Bonus
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_CRITICAL_HIT_CHANCE` — Archon Buffs also grant {X}% increased Critical Hit Chance
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_MOVEMENT_SPEED` — Archon Buffs also grant {X}% increased Movement Speed
- `AREA_SKILLS_HAVE_X_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE` — Area Skills have {X}% chance to Knock Enemies Back on Hit Damage
- `ARMOR_BREAK_X_INCREASED_ARMOR` — Armor Break {X}% increased Armor
- `ARROWS_GAIN_CRITICAL_HIT_CHANCE_AS_THEY_TRAVEL_FARTHER_UP_TO_X_INCREASED_CRITICAL_HIT_CHANCE_AFTER_X_METRES` — Arrows gain Critical Hit Chance as they travel farther, up to {X}% increased Critical Hit Chance after {X} metres
- `ATTACK_SKILLS_DEAL_X_INCREASED_DAMAGE_WHILE_HOLDING_A_SHIELD` — Attack Skills deal {X}% increased Damage while holding a Shield
- `ATTACKS_USED_BY_BALLISTAS_HAVE_X_INCREASED_ATTACK_SPEED` — Attacks used by Ballistas have {X}% increased Attack Speed
- `ATTACKS_USED_BY_TOTEMS_HAVE_X_INCREASED_ATTACK_SPEED` — Attacks used by Totems have {X}% increased Attack Speed
- `ATTACKS_USED_BY_TOTEMS_HAVE_X_INCREASED_ATTACK_SPEED_PER_SUMMONED_TOTEM` — Attacks used by Totems have {X}% increased Attack Speed per Summoned Totem
- `BANNER_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Banner Skills have {X}% increased Area of Effect
- `BANNERS_ALSO_GRANT_X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_AFFECTED_TARGETS` — Banners also grant +{X}% to all Elemental Damage Resistances affected targets
- `BASE_BLEEDING_DURATION_IS_X_SECOND` — Base Bleeding Duration is {X} second
- `BLEEDING_YOU_INFLICT_IS_AGGRAVATED` — Bleeding you inflict is Aggravated
- `CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY` — Can Attack as though using a Quarterstaff while both of your hand slots are empty
- `CANNOT_DODGE_ROLL_OR_SPRINT` — Cannot Dodge Roll or Sprint
- `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH` — Cannot Recover Health other than from Health Leech
- `CANNOT_BE_LIGHT_STUNNED` — Cannot be Light Stunned
- `CANNOT_BE_LIGHT_STUNNED_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY` — Cannot be Light Stunned if you haven't been Hit Damage Recently
- `CANNOT_USE_CHARMS` — Cannot use Charms
- `CHANCE_TO_DEFLECT_IS_LUCKY` — Chance to Deflect is Lucky
- `CHANCE_TO_EVASION_IS_UNLUCKY` — Chance to Evasion is Unlucky
- `CHANNELLING_SKILLS_DEAL_X_INCREASED_DAMAGE` — Channelling Skills deal {X}% increased Damage
- `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE` — Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude
- `COMPANIONS_DEAL_X_INCREASED_DAMAGE_AGAINST_IMMOBILISED_ENEMIES` — Companions deal {X}% increased damage against Immobilised enemies
- `COMPANIONS_HAVE_X_TO_CHAOS_RESISTANCES` — Companions have +{X}% to Chaos Resistances
- `COMPANIONS_HAVE_X_TO_ALL_ELEMENTAL_RESISTANCES` — Companions have +{X}% to all Elemental Resistances
- `COMPANIONS_HAVE_A_X_CHANCE_TO_POISON_ON_HIT_DAMAGE` — Companions have a {X}% chance to Poison on Hit Damage
- `COMPANIONS_HAVE_X_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILL` — Companions have {X}% chance to gain Onslaught on Kill
- `COMPANIONS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Companions have {X}% increased Area of Effect
- `COMPANIONS_HAVE_X_INCREASED_MOVEMENT_SPEED` — Companions have {X}% increased Movement Speed
- `CONSUMING_GLORY_GRANTS_YOU_X_INCREASED_ATTACK_DAMAGE_PER_GLORY_CONSUMED_FOR_X_SECONDS_UP_TO_X` — Consuming Glory grants you {X}% increased Attack damage per Glory consumed for {X} seconds, up to {X}%
- `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING` — Create Cold Elemental Infusion Remnants instead of Lightning
- `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD` — Create Fire Elemental Infusion Remnants instead of Cold
- `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE` — Create Lightning Elemental Infusion Remnants instead of Fire
- `CRITICAL_HITS_WITH_DAGGERS_HAVE_A_X_CHANCE_TO_POISON_THE_ENEMY` — Critical Hits with Daggers have a {X}% chance to Poison the Enemy
- `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA` — Damage Penetration {X}% Lightning Resistances if on Low Mana
- `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_HEAVY_STUNNED_ENEMIES` — Damage with Hits is Lucky against Heavy Stunned Enemies
- `DEAL_NO_NON_FIRE_DAMAGE` — Deal no Non-Fire Damage
- `DEBUFFS_INFLICTED_BY_HAZARDS_HAVE_X_INCREASED_SLOW_MAGNITUDE` — Debuffs inflicted by Hazards have {X}% increased Slow Magnitude
- `DEBUFFS_YOU_INFLICT_HAVE_X_INCREASED_SLOW_MAGNITUDE` — Debuffs you inflict have {X}% increased Slow Magnitude
- `DETONATOR_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Detonator skills have {X}% increased Area of Effect
- `DETONATOR_SKILLS_HAVE_X_INCREASED_DAMAGE` — Detonator skills have {X}% increased damage
- `DODGE_ROLL_CANNOT_AVOID_DAMAGE` — Dodge Roll cannot Avoid Damage
- `DOUBLE_ACTIVATION_DELAY_OF_CURSES` — Double Activation Delay of Curses
- `ECHOED_SPELLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Echoed Spells have {X}% increased Area of Effect
- `EMPOWERED_ATTACKS_GAIN_X_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE` — Empowered Attacks gain {X}% of Damage as Extra Cold Damage
- `EMPOWERED_ATTACKS_GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE` — Empowered Attacks gain {X}% of Physical Damage as Extra Fire damage
- `EMPOWERED_ATTACKS_HAVE_X_INCREASED_STUN_BUILDUP` — Empowered Attacks have {X}% increased Stun Buildup
- `ENEMIES_BLINDED_BY_YOU_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE` — Enemies Blinded by you have {X}% increased Critical Hit Chance
- `ENEMIES_FREEZE_BY_YOU_HAVE_X_TO_COLD_RESISTANCE` — Enemies Freeze by you have -{X}% to Cold Resistance
- `ENEMIES_FROZEN_BY_YOU_TAKE_X_INCREASED_COLD_DAMAGE` — Enemies Frozen by you take {X}% increased Cold Damage
- `ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_X_INCREASED_FIRE_DAMAGE_FOR_EACH_SECOND_THEY_HAVE_EVER_BEEN_IGNITED_BY_YOU_UP_TO_A_MAX_OF_X` — Enemies Ignited by you permanently take {X}% increased Fire Damage for each second they have ever been Ignited by you, up to a Max of {X}%
- `ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_X_INCREASED_ARMOR` — Enemies affected by your Hazards Recently have {X}% increased Armor
- `ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_X_INCREASED_EVASION` — Enemies affected by your Hazards Recently have {X}% increased Evasion
- `ENEMIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_COOLDOWN_RECOVERY_RATE` — Enemies in your Presence have {X}% increased Cooldown Recovery Rate
- `ENEMIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_HEALTH_REGENERATION_RATE` — Enemies in your Presence have {X}% increased Health Regeneration rate
- `ENEMIES_YOU_CURSE_ARE_HINDERED_WITH_X_INCREASED_MOVEMENT_SPEED` — Enemies you Curse are Hindered, with {X}% increased Movement Speed
- `ENEMIES_YOU_ELECTROCUTE_HAVE_X_INCREASED_DAMAGE_TAKEN` — Enemies you Electrocute have {X}% increased Damage taken
- `ENEMIES_YOU_MARK_HAVE_X_INCREASED_ACCURACY` — Enemies you Mark have {X}% increased Accuracy
- `ENEMIES_YOU_APPLY_INCISION_TO_TAKE_X_INCREASED_PHYSICAL_DAMAGE_PER_INCISION` — Enemies you apply Incision to take {X}% increased Physical Damage per Incision
- `ENERGY_SHIELD_DOES_NOT_ESRECHARGE` — Energy Shield does not ESRecharge
- `ENERGY_SHIELD_DOES_NOT_RECHARGE` — Energy Shield does not Recharge
- `EVERY_RAGE_ALSO_GRANTS_YOU_X_INCREASED_MINION_DAMAGE` — Every Rage also grants you {X}% increased Minion Damage
- `EVERY_RAGE_ALSO_GRANTS_X_INCREASED_ARMOR` — Every Rage also grants {X}% increased Armor
- `EVERY_RAGE_ALSO_GRANTS_X_INCREASED_FIRE_DAMAGE` — Every Rage also grants {X}% increased Fire Damage
- `EVERY_RAGE_ALSO_GRANTS_X_INCREASED_SPELL_DAMAGE` — Every Rage also grants {X}% increased Spell Damage
- `EVERY_RAGE_ALSO_GRANTS_X_INCREASED_STUN_THRESHOLD` — Every Rage also grants {X}% increased Stun Threshold
- `EVERY_FIVE_RAGE_ALSO_GRANTS_YOU_X_INCREASED_MINION_ATTACK_SPEED` — Every five Rage also grants you {X}% increased Minion Attack Speed
- `EVERY_X_RAGE_ALSO_GRANTS_X_INCREASED_PHYSICAL_DAMAGE` — Every {X} Rage also grants {X}% increased Physical Damage
- `EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD` — Excess Health Recovery from Regeneration is applied to Energy Shield
- `FIRE_SPELLS_CONVERSION_X_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE` — Fire Spells Conversion {X}% of Fire Damage to Chaos Damage
- `HERALD_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Herald Skills have {X}% increased Area of Effect
- `HIT_DAMAGE_ARMOR_BREAK_X_INCREASED_ARMOR_ON_TARGETS_WITH_AILMENTS` — Hit Damage Armor Break {X}% increased Armor on targets with Ailments
- `HIT_DAMAGE_AGAINST_YOU_HAVE_X_INCREASED_CRITICAL_DAMAGE_BONUS` — Hit Damage against you have {X}% increased Critical Damage Bonus
- `HIT_DAMAGE_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_YOU` — Hit Damage have {X}% increased Critical Hit Chance against you
- `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE` — Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage
- `IMMUNE_TO_BLEEDING_WHILE_SHAPESHIFTED` — Immune to Bleeding while Shapeshifted
- `IMMUNE_TO_BLEEDING_WHILE_AFFECTED_BY_AN_ARCHON_BUFF` — Immune to Bleeding while affected by an Archon Buff
- `IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING` — Immune to Chaos Damage and Bleeding
- `IMMUNE_TO_POISON` — Immune to Poison
- `IMMUNE_TO_SHOCK_WHILE_AFFECTED_BY_AN_ARCHON_BUFF` — Immune to Shock while affected by an Archon Buff
- `INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED` — Inherent Health granted by Strength is halved
- `INVOCATED_SPELLS_DEAL_X_INCREASED_DAMAGE` — Invocated Spells deal {X}% increased Damage
- `INVOCATED_SPELLS_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE` — Invocated Spells have {X}% increased Critical Hit Chance
- `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS` — Invocation Skills cannot gain Energy while Triggering Spells
- `INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_X_SECONDS` — Invocation Skills instead Trigger Spells every {X} seconds
- `INVOCATION_SPELLS_HAVE_X_INCREASED_CRITICAL_DAMAGE_BONUS` — Invocation Spells have {X}% increased Critical Damage Bonus
- `INVOKED_SPELLS_CONSUME_X_MORE_ENERGY` — Invoked Spells consume {X}% more Energy
- `LEECH_HEALTH_X_MORE_QUICKLY` — Leech Health {X}% more quickly
- `LEECHING_HEALTH_FROM_YOUR_HIT_DAMAGE_CAUSES_YOUR_COMPANION_TO_ALSO_LEECH_THE_SAME_AMOUNT_OF_HEALTH` — Leeching Health from your Hit Damage causes your Companion to also Leech the same amount of Health
- `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED` — Health Leech effects are not add when Reservation Health is Filled
- `HEALTH_HEALTH_RECHARGE_INSTEAD_OF_ENERGY_SHIELD` — Health Health Recharge instead of Energy Shield
- `MANA_COSTS_ARE_DOUBLED` — Mana Costs are Doubled
- `MARK_SKILLS_HAVE_X_INCREASED_SKILL_EFFECT_DURATION` — Mark Skills have {X}% increased Skill Effect Duration
- `MAX_HEALTH_IS_X` — Max Health is {X}
- `META_SKILLS_GAIN_X_INCREASED_ENERGY_IF_YOU_VE_DEALT_A_CRITICAL_HIT_DAMAGE_RECENTLY` — Meta Skills gain {X}% increased Energy if you've dealt a Critical Hit Damage Recently
- `META_SKILLS_HAVE_X_INCREASED_RESERVATION_EFFICIENCY` — Meta Skills have {X}% increased Reservation Efficiency
- `MINIONS_CAUSE_X_INCREASED_STUN_BUILDUP` — Minions cause {X}% increased Stun Buildup
- `MINIONS_DEAL_X_INCREASED_DAMAGE_IF_YOU_VE_HIT_DAMAGE_RECENTLY` — Minions deal {X}% increased Damage if you've Hit Damage Recently
- `MINIONS_DEAL_X_INCREASED_DAMAGE_WITH_COMMAND_SKILLS` — Minions deal {X}% increased Damage with Command Skills
- `MINIONS_DEAL_X_INCREASED_DAMAGE_WITH_COMMAND_SKILLS_FOR_EACH_DIFFERENT_TYPE_OF_PERSISTENT_MINION_IN_YOUR_PRESENCE` — Minions deal {X}% increased Damage with Command Skills for each different type of Persistent Minion in your Presence
- `MINIONS_HAVE_X_INCREASED_COOLDOWN_RECOVERY_RATE` — Minions have {X}% increased Cooldown Recovery Rate
- `MINIONS_HAVE_X_INCREASED_CRITICAL_DAMAGE_BONUS` — Minions have {X}% increased Critical Damage Bonus
- `NEVER_DEAL_CRITICAL_HITS` — Never deal Critical Hits
- `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS` — No Charges requirement for placing Totems
- `NO_RAGE_EFFECT` — No Rage effect
- `NON_MINION_SKILLS_HAVE_X_MORE_RESERVATION_EFFICIENCY` — Non-Minion Skills have {X}% more Reservation Efficiency
- `OFFERING_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Offering Skills have {X}% increased Area of Effect
- `OFFERING_SKILLS_HAVE_X_INCREASED_DURATION` — Offering Skills have {X}% increased Duration
- `OFFERINGS_HAVE_X_INCREASED_MAX_HEALTH` — Offerings have {X}% increased Max Health
- `OVERGROWN_PLANT_SKILLS_ARMOR_BREAK_X_INCREASED_ARMOR` — Overgrown Plant Skills Armor Break {X}% increased Armor
- `PARRY_HAS_X_INCREASED_STUN_BUILDUP` — Parry has {X}% increased Stun Buildup
- `PROJECTILES_DEAL_X_INCREASED_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` — Projectiles deal {X}% increased Damage against Heavy Stunned Enemies
- `PROJECTILES_DEAL_X_INCREASED_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM` — Projectiles deal {X}% increased Damage with Hits against Enemies further than {X}m
- `PROJECTILES_DEAL_X_INCREASED_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM` — Projectiles deal {X}% increased Damage with Hits against Enemies within {X}m
- `PROJECTILES_HAVE_X_INCREASED_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_FURTHER_THAN_XM` — Projectiles have {X}% increased Critical Damage Bonus against Enemies further than {X}m
- `PROJECTILES_HAVE_X_INCREASED_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_WITHIN_XM` — Projectiles have {X}% increased Critical Damage Bonus against Enemies within {X}m
- `PROJECTILES_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM` — Projectiles have {X}% increased Critical Hit Chance against Enemies further than {X}m
- `RECOVER_X_OF_MAX_HEALTH_ON_KILL` — Recover {X}% of Max Health on Kill
- `REGENERATE_X_RAGE_PER_SECOND_PER_X_RAGE_SPENT_RECENTLY` — Regenerate {X} Rage per second per {X} Rage spent Recently
- `SHAPESHIFT_SKILLS_HAVE_X_INCREASED_SKILL_EFFECT_DURATION` — Shapeshift Skills have {X}% increased Skill Effect Duration
- `SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` — Skill Mana Costs Stat Conversion to Health Costs
- `SKILLS_DEAL_X_INCREASED_DAMAGE_PER_COMBO_CONSUMED_UP_TO_X` — Skills deal {X}% increased Damage per Combo consumed, up to {X}%
- `SPELL_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Spell Skills have {X}% increased Area of Effect
- `SPELLS_CAST_BY_TOTEMS_HAVE_X_INCREASED_CAST_SPEED` — Spells Cast by Totems have {X}% increased Cast Speed
- `SPELLS_CAST_BY_TOTEMS_HAVE_X_INCREASED_CAST_SPEED_PER_SUMMONED_TOTEM` — Spells Cast by Totems have {X}% increased Cast Speed per Summoned Totem
- `SPELLS_GAIN_X_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` — Spells gain {X}% of Damage as extra Chaos Damage
- `STAT_CONVERSION_X_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA` — Stat Conversion {X}% of Max Energy Shield to Max Mana
- `STORM_AND_PLANT_SPELLS_DEAL_X_MORE_DAMAGE_COST_X_MORE_HAVE_X_MORE_DURATION` — Storm and Plant Spells: • deal {X}% more damage • cost {X}% more • have {X}% more duration
- `SUCCESSFULLY_PARRYING_A_PROJECTILE_HIT_DAMAGE_GRANTS_X_INCREASED_DAMAGE_TO_YOUR_NEXT_MELEE_ATTACK` — Successfully Parrying a Projectile Hit Damage grants {X}% increased Damage to your next Melee Attack
- `TAKE_X_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING` — Take {X}% more Damage from Hits while Dodge Rolling
- `TAKE_X_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` — Take {X}% more Damage over Time if you haven't started taking Damage over Time in the past second
- `TAKE_X_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` — Take {X}% more Damage over Time if you've started taking Damage over Time in the past second
- `TARGETS_CURSED_BY_YOU_HAVE_X_INCREASED_HEALTH_REGENERATION_RATE` — Targets Cursed by you have {X}% increased Health Regeneration Rate
- `TOTEMS_GAIN_X_TO_ALL_ELEMENTAL_RESISTANCES` — Totems gain +{X}% to all Elemental Resistances
- `TOTEMS_RESERVE_X_SPIRIT_EACH` — Totems reserve {X} Spirit each
- `TRIGGERED_SPELLS_DEAL_X_INCREASED_SPELL_DAMAGE` — Triggered Spells deal {X}% increased Spell Damage
- `TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS` — Triple Attributes requirements of Martial Weapons
- `UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL_X_MORE_ATTACK_SPEED_PER_X_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS_X_TO_CRITICAL_HIT_CHANCE_PER_X_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS` — Unarmed Attacks that would use an Equipped Quarterstaff's damage have: • Base Unarmed Damage Physical damage replaced with damage based on their Skill Level • {X}% more Attack Speed per {X} Item Evasion on Equipped Armor Items • +{X}% to Critical Hit Chance per {X} Item Energy Shield on Equipped Armor Items
- `VOLATILE_POWER_ALSO_GRANTS_X_INCREASED_CRITICAL_HIT_CHANCE_PER_VOLATILITY_EXPLODED` — Volatile Power also grants {X}% increased Critical Hit chance per Volatility exploded
- `WARCRY_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT` — Warcry Skills have {X}% increased Area of Effect
- `WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_GAIN_X_INCREASED_SPELL_DAMAGE_PER_SECOND_YOU_WERE_SHAPESHIFTED_UP_TO_A_MAX_OF_X_FOR_X_SECONDS` — When you Shapeshift to Human form, gain {X}% increased Spell Damage per second you were Shapeshifted, up to a Max of {X}%, for {X} seconds
- `YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN` — You can equip a non-Item Rarity Sceptre while wielding a Talisman
- `YOU_CAN_HAVE_TWO_COMPANIONS_OF_DIFFERENT_TYPES` — You can have two Companions of different types
- `YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND` — You can wield Two-Handed Axes, Maces and Swords in one hand
- `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION` — You cannot Recover Energy Shield from Regeneration
- `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR` — You cannot Recover Energy Shield to above Armor
- `YOU_CANNOT_BE_LIGHT_STUNNED_IF_YOU_VE_BEEN_STUNNED_RECENTLY` — You cannot be Light Stunned if you've been Stunned Recently
- `YOU_HAVE_NO_MANA` — You have no Mana
- `YOUR_CURSES_HAVE_X_INCREASED_BUFF_MAGNITUDE_IF_X_OF_CURSE_DURATION_EXPIRED` — Your Curses have {X}% increased Buff Magnitude if {X}% of Curse Duration expired
- `YOUR_HEAVY_STUN_BUILDUP_EMPTIES_X_FASTER` — Your Heavy Stun buildup empties {X}% faster
- `YOUR_HEAVY_STUN_BUILDUP_EMPTIES_X_FASTER_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY` — Your Heavy Stun buildup empties {X}% faster if you've successfully Parried Recently
- `YOUR_TOTEM_LIMIT_IS_DOUBLED` — Your Totem Limit is doubled
- `GAIN_ARCANE_SURGE_WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_AFTER_BEING_SHAPESHIFTED_FOR_AT_LEAST_X_SECONDS` — gain Arcane Surge when you Shapeshift to Human form after being Shapeshifted for at least {X} seconds
- `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES` — gain Endurance Charges instead of Power Charges
- `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES` — gain Frenzy Charges instead of Endurance Charges
- `GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES` — gain Power Charges instead of Frenzy Charges
- `GAIN_X_VOLATILITY_WHEN_AN_ALLIES_PERSISTENT_REVIVING_MINION_IS_MINION_DEATH` — gain {X} Volatility when an Allies Persistent Reviving Minion is Minion Death
- `GAIN_X_OF_COLD_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_FREEZE_ENEMIES` — gain {X}% of Cold Damage as Extra Fire Damage against Freeze Enemies
- `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY` — gain {X}% of Damage as Extra Fire Damage per Endurance Charge consumed Recently
- `GAIN_X_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_ON_IGNITED_GROUND` — gain {X}% of Damage as Extra Fire Damage while on Ignited Ground
- `GAIN_X_OF_ELEMENTAL_DAMAGE_AS_EXTRA_COLD_DAMAGE` — gain {X}% of Elemental Damage as Extra Cold Damage
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_DAZED_ENEMIES` — gain {X}% of Physical Damage as Extra Cold Damage against Dazed Enemies
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_SHOCKED_ENEMIES` — gain {X}% of Physical Damage as Extra Cold Damage against Shocked Enemies
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE` — gain {X}% of Physical Damage as Extra Fire Damage
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` — gain {X}% of Physical Damage as Extra Lightning Damage
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_DAZED_ENEMIES` — gain {X}% of Physical Damage as Extra Lightning Damage against Dazed Enemies
- `GAIN_X_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE` — gain {X}% of Physical Damage as extra Chaos Damage
- `X_CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME` — {X}% chance for Lightning Skills to Chain an additional time
- `X_CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU` — {X}% chance for Projectiles to Pierce Enemies within {X}m distance of you
- `X_CHANCE_FOR_SLAM_SKILLS_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` — {X}% chance for Slam Skills to cause an additional Aftershock
- `X_CHANCE_FOR_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` — {X}% chance for Slam Skills you use yourself to cause an additional Aftershock
- `X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_CRITICALLY_HIT_WITH_ATTACKS` — {X}% chance to Aggravate Bleeding on targets you Critically Hit with Attacks
- `X_CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS` — {X}% chance to Blind Enemies on Hit Damage with Attacks
- `X_CHANCE_TO_DAZE_ENEMIES_WHOSE_HIT_DAMAGE_YOU_BLOCK_WITH_A_RAISED_SHIELD` — {X}% chance to Daze Enemies whose Hit Damage you Block with a raised Shield
- `X_CHANCE_TO_POISON_ON_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_NOT_POISONED` — {X}% chance to Poison on Hit Damage against Enemies that are not Poisoned
- `X_CHANCE_TO_GAIN_ARCANE_SURGE_WHEN_YOU_DEAL_A_CRITICAL_HIT` — {X}% chance to gain Arcane Surge when you deal a Critical Hit
- `X_CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS` — {X}% chance to inflict Bleeding on Critical Hit with Attacks
- `X_INCREASED_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES` — {X}% increased Accuracy against Rare or Unique Enemies
- `X_INCREASED_ACCURACY_AT_CLOSE_RANGE` — {X}% increased Accuracy at Close Range
- `X_INCREASED_ACCURACY_WHILE_DUAL_WIELDING` — {X}% increased Accuracy while Dual Wielding
- `X_INCREASED_ACCURACY_WHILE_SHAPESHIFTED` — {X}% increased Accuracy while Shapeshifted
- `X_INCREASED_ACCURACY_WHILE_SURROUNDED` — {X}% increased Accuracy while Surrounded
- `X_INCREASED_ACCURACY_WHILE_MOVING` — {X}% increased Accuracy while moving
- `X_INCREASED_ACCURACY_WITH_BOWS` — {X}% increased Accuracy with Bows
- `X_INCREASED_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS` — {X}% increased Accuracy with One Handed Melee Weapons
- `X_INCREASED_ACCURACY_WITH_QUARTERSTAVES` — {X}% increased Accuracy with Quarterstaves
- `X_INCREASED_ACCURACY_WITH_SPEARS` — {X}% increased Accuracy with Spears
- `X_INCREASED_ACCURACY_WITH_TWO_HANDED_MELEE_WEAPONS` — {X}% increased Accuracy with Two Handed Melee Weapons
- `X_INCREASED_AREA_DAMAGE` — {X}% increased Area Damage
- `X_INCREASED_AREA_OF_EFFECT` — {X}% increased Area of Effect
- `X_INCREASED_AREA_OF_EFFECT_FOR_ATTACKS_PER_ENEMY_YOU_VE_IGNITED_IN_THE_LAST_X_SECONDS_UP_TO_X` — {X}% increased Area of Effect for Attacks per Enemy you've Ignited in the last {X} seconds, up to {X}%
- `X_INCREASED_AREA_OF_EFFECT_FOR_SKILLS_USED_BY_TOTEMS` — {X}% increased Area of Effect for Skills used by Totems
- `X_INCREASED_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY` — {X}% increased Area of Effect if you have Stunned an Enemy Recently
- `X_INCREASED_AREA_OF_EFFECT_IF_YOU_VE_KILLED_RECENTLY` — {X}% increased Area of Effect if you've Killed Recently
- `X_INCREASED_AREA_OF_EFFECT_IF_YOU_VE_STUNNED_AN_ENEMY_WITH_A_TWO_HANDED_MELEE_WEAPON_RECENTLY` — {X}% increased Area of Effect if you've Stunned an Enemy with a Two Handed Melee Weapon Recently
- `X_INCREASED_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS` — {X}% increased Area of Effect of Ancestrally Boosted Attacks
- `X_INCREASED_AREA_OF_EFFECT_WHILE_YOU_HAVE_ARCANE_SURGE` — {X}% increased Area of Effect while you have Arcane Surge
- `X_INCREASED_AREA_OF_EFFECT_WHILE_YOU_HAVE_A_TOTEM` — {X}% increased Area of Effect while you have a Totem
- `X_INCREASED_ARMOR_BREAK_DURATION` — {X}% increased Armor Break Duration
- `X_INCREASED_ARMOR_BREAK_TAKEN` — {X}% increased Armor Break taken
- `X_INCREASED_ARMOR_AND_EVASION_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE` — {X}% increased Armor and Evasion per Summoned Totem in your Presence
- `X_INCREASED_ARMOR_AND_EVASION_WHEN_ON_LOW_HEALTH` — {X}% increased Armor and Evasion when on Low Health
- `X_INCREASED_ARMOR_AND_EVASION_WHILE_LEECHING` — {X}% increased Armor and Evasion while Leeching
- `X_INCREASED_ARMOR_FROM_EQUIPPED_BODY_ARMOR` — {X}% increased Armor from Equipped Body Armor
- `X_INCREASED_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY` — {X}% increased Armor if you have been Hit Damage Recently
- `X_INCREASED_ARMOR_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY` — {X}% increased Armor if you haven't been Hit Damage Recently
- `X_INCREASED_ARMOR_IF_YOU_VE_HIT_DAMAGE_AN_ENEMY_WITH_A_MELEE_ATTACK_RECENTLY` — {X}% increased Armor if you've Hit Damage an Enemy with a Melee Attack Recently
- `X_INCREASED_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY` — {X}% increased Armor if you've consumed an Endurance Charge Recently
- `X_INCREASED_ARMOR_WHILE_BLEEDING` — {X}% increased Armor while Bleeding
- `X_INCREASED_ARMOR_WHILE_IGNITED` — {X}% increased Armor while Ignited
- `X_INCREASED_ARMOR_WHILE_SHAPESHIFTED` — {X}% increased Armor while Shapeshifted
- `X_INCREASED_ARMOR_WHILE_SURROUNDED` — {X}% increased Armor while Surrounded
- `X_INCREASED_ARMOR_WHILE_STATIONARY` — {X}% increased Armor while stationary
- `X_INCREASED_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD` — {X}% increased Armor, Evasion and Energy Shield from Equipped Shield
- `X_INCREASED_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING` — {X}% increased Armor, Evasion and Energy Shield while Channelling
- `X_INCREASED_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_WIELDING_A_QUARTERSTAFF` — {X}% increased Armor, Evasion and Energy Shield while wielding a Quarterstaff
- `X_INCREASED_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` — {X}% increased Armor, Evasion and Energy Shield while your Companion is in your Presence
- `X_INCREASED_ATTACK_COLD_DAMAGE` — {X}% increased Attack Cold Damage
- `X_INCREASED_ATTACK_CRITICAL_HIT_CHANCE_WHILE_DUAL_WIELDING` — {X}% increased Attack Critical Hit Chance while Dual Wielding
- `X_INCREASED_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES` — {X}% increased Attack Damage against Bleeding Enemies
- `X_INCREASED_ATTACK_DAMAGE_AGAINST_MAIMED_ENEMIES` — {X}% increased Attack Damage against Maimed Enemies
- `X_INCREASED_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES` — {X}% increased Attack Damage against Rare or Unique Enemies
- `X_INCREASED_ATTACK_DAMAGE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF_X` — {X}% increased Attack Damage for each Minion in your Presence, up to a Max of {X}%
- `X_INCREASED_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL_YOU_VE_USED_IN_THE_PAST_X_SECONDS` — {X}% increased Attack Damage for each different Non-Instant Spell you've used in the past {X} seconds
- `X_INCREASED_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` — {X}% increased Attack Damage if you have Shapeshifted to an Animal form Recently
- `X_INCREASED_ATTACK_DAMAGE_IF_YOU_HAVE_BEEN_HEAVY_STUNNED_RECENTLY` — {X}% increased Attack Damage if you have been Heavy Stunned Recently
- `X_INCREASED_ATTACK_DAMAGE_IF_YOU_VE_CAST_A_SPELL_RECENTLY` — {X}% increased Attack Damage if you've Cast a Spell Recently
- `X_INCREASED_ATTACK_DAMAGE_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE` — {X}% increased Attack Damage per Summoned Totem in your Presence
- `X_INCREASED_ATTACK_DAMAGE_PER_X_ITEM_ARMOR_AND_ITEM_EVASION_ON_EQUIPPED_SHIELD` — {X}% increased Attack Damage per {X} Item Armor and Item Evasion on Equipped Shield
- `X_INCREASED_ATTACK_DAMAGE_WHILE_DUAL_WIELDING` — {X}% increased Attack Damage while Dual Wielding
- `X_INCREASED_ATTACK_DAMAGE_WHILE_SURROUNDED` — {X}% increased Attack Damage while Surrounded
- `X_INCREASED_ATTACK_DAMAGE_WHILE_MOVING` — {X}% increased Attack Damage while moving
- `X_INCREASED_ATTACK_DAMAGE_WHILE_ON_FULL_HEALTH` — {X}% increased Attack Damage while on Full Health
- `X_INCREASED_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH` — {X}% increased Attack Damage while on Low Health
- `X_INCREASED_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE` — {X}% increased Attack Damage while you have an Allies in your Presence
- `X_INCREASED_ATTACK_DAMAGE_WHILE_YOU_HAVE_NO_HEALTH_FLASK_USES_LEFT` — {X}% increased Attack Damage while you have no Health Flask uses left
- `X_INCREASED_ATTACK_SPEED_DURING_ANY_FLASK_EFFECT` — {X}% increased Attack Speed during any Flask Effect
- `X_INCREASED_ATTACK_SPEED_IF_YOU_HAVEN_T_ATTACKED_RECENTLY` — {X}% increased Attack Speed if you haven't Attacked Recently
- `X_INCREASED_ATTACK_SPEED_IF_YOU_VE_BEEN_HIT_RECENTLY` — {X}% increased Attack Speed if you've been Hit Recently
- `X_INCREASED_ATTACK_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY` — {X}% increased Attack Speed if you've dealt a Critical Hit Recently
- `X_INCREASED_ATTACK_SPEED_IF_YOU_VE_KILLED_RECENTLY` — {X}% increased Attack Speed if you've killed Recently
- `X_INCREASED_ATTACK_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY` — {X}% increased Attack Speed if you've successfully Parried Recently
- `X_INCREASED_ATTACK_SPEED_PER_ENEMY_IN_CLOSE_RANGE` — {X}% increased Attack Speed per Enemy in Close Range
- `X_INCREASED_ATTACK_SPEED_PER_X_ACCURACY_UP_TO_X` — {X}% increased Attack Speed per {X} Accuracy, up to {X}%
- `X_INCREASED_ATTACK_SPEED_PER_X_DEXTERITY` — {X}% increased Attack Speed per {X} Dexterity
- `X_INCREASED_ATTACK_SPEED_WHILE_DUAL_WIELDING` — {X}% increased Attack Speed while Dual Wielding
- `X_INCREASED_ATTACK_SPEED_WHILE_LEECHING` — {X}% increased Attack Speed while Leeching
- `X_INCREASED_ATTACK_SPEED_WHILE_SURROUNDED` — {X}% increased Attack Speed while Surrounded
- `X_INCREASED_ATTACK_SPEED_WHILE_HOLDING_A_SHIELD` — {X}% increased Attack Speed while holding a Shield
- `X_INCREASED_ATTACK_SPEED_WHILE_NOT_ON_LOW_MANA` — {X}% increased Attack Speed while not on Low Mana
- `X_INCREASED_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` — {X}% increased Attack Speed while your Companion is in your Presence
- `X_INCREASED_ATTACK_SPEED_WITH_AXES` — {X}% increased Attack Speed with Axes
- `X_INCREASED_ATTACK_SPEED_WITH_BOWS` — {X}% increased Attack Speed with Bows
- `X_INCREASED_ATTACK_SPEED_WITH_CROSSBOWS` — {X}% increased Attack Speed with Crossbows
- `X_INCREASED_ATTACK_SPEED_WITH_DAGGERS` — {X}% increased Attack Speed with Daggers
- `X_INCREASED_ATTACK_SPEED_WITH_FLAILS` — {X}% increased Attack Speed with Flails
- `X_INCREASED_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS` — {X}% increased Attack Speed with One Handed Melee Weapons
- `X_INCREASED_ATTACK_SPEED_WITH_ONE_HANDED_WEAPONS` — {X}% increased Attack Speed with One Handed Weapons
- `X_INCREASED_ATTACK_SPEED_WITH_QUARTERSTAVES` — {X}% increased Attack Speed with Quarterstaves
- `X_INCREASED_ATTACK_SPEED_WITH_SPEARS` — {X}% increased Attack Speed with Spears
- `X_INCREASED_ATTACK_SPEED_WITH_SWORDS` — {X}% increased Attack Speed with Swords
- `X_INCREASED_ATTACK_AND_CAST_SPEED` — {X}% increased Attack and Cast Speed
- `X_INCREASED_ATTACK_AND_CAST_SPEED_DURING_EFFECT_OF_ANY_MANA_FLASK` — {X}% increased Attack and Cast Speed during Effect of any Mana Flask
- `X_INCREASED_ATTACK_AND_CAST_SPEED_IF_YOU_VE_SUMMONED_A_TOTEM_RECENTLY` — {X}% increased Attack and Cast Speed if you've summoned a Totem Recently
- `X_INCREASED_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS` — {X}% increased Attack and Cast Speed with Elemental Skills
- `X_INCREASED_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS` — {X}% increased Attack and Cast Speed with Lightning Skills
- `X_INCREASED_BALLISTA_CRITICAL_DAMAGE_BONUS` — {X}% increased Ballista Critical Damage Bonus
- `X_INCREASED_BALLISTA_CRITICAL_HIT_CHANCE` — {X}% increased Ballista Critical Hit Chance
- `X_INCREASED_BALLISTA_IMMOBILISATION_BUILDUP` — {X}% increased Ballista Immobilisation buildup
- `X_INCREASED_BLEEDING_DURATION` — {X}% increased Bleeding Duration
- `X_INCREASED_BLIND_DURATION` — {X}% increased Blind duration
- `X_INCREASED_BLOCK_RECOVERY` — {X}% increased Block Recovery
- `X_INCREASED_BLOCK_CHANCE_WHILE_SURROUNDED` — {X}% increased Block chance while Surrounded
- `X_INCREASED_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT_AGAINST_MARKED_ENEMIES` — {X}% increased Buff Magnitude of Ailments you inflict against Marked Enemies
- `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION` — {X}% increased Buff Magnitude of Bleeding you inflict against Enemies affected by Incision
- `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_PINNED_ENEMIES` — {X}% increased Buff Magnitude of Bleeding you inflict against Pinned Enemies
- `X_INCREASED_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS` — {X}% increased Buff Magnitude of Bleeding you inflict with Critical Hits
- `X_INCREASED_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS` — {X}% increased Buff Magnitude of Damaging Ailments you inflict with Critical Hits
- `X_INCREASED_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS` — {X}% increased Buff Magnitude of Non-Damaging Ailments you inflict with Critical Hits
- `X_INCREASED_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED` — {X}% increased Buff Magnitude of Poison you inflict on targets that are not Poisoned
- `X_INCREASED_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT_WITH_CRITICAL_HITS` — {X}% increased Buff Magnitude of Shock you inflict with Critical Hits
- `X_INCREASED_CAST_SPEED_FOR_EACH_DIFFERENT_SPELL_YOU_VE_CAST_IN_THE_LAST_EIGHT_SECONDS` — {X}% increased Cast Speed for each different Spell you've Cast in the last eight seconds
- `X_INCREASED_CAST_SPEED_IF_YOU_VE_ATTACKED_RECENTLY` — {X}% increased Cast Speed if you've Attacked Recently
- `X_INCREASED_CAST_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY` — {X}% increased Cast Speed if you've dealt a Critical Hit Recently
- `X_INCREASED_CAST_SPEED_IF_YOU_VE_USED_A_MANA_FLASK_RECENTLY` — {X}% increased Cast Speed if you've used a Mana Flask Recently
- `X_INCREASED_CAST_SPEED_PER_SPELL_ECHOED_RECENTLY_UP_TO_X` — {X}% increased Cast Speed per Spell Echoed Recently, up to {X}%
- `X_INCREASED_CAST_SPEED_WHEN_ON_FULL_HEALTH` — {X}% increased Cast Speed when on Full Health
- `X_INCREASED_CAST_SPEED_WHEN_ON_LOW_HEALTH` — {X}% increased Cast Speed when on Low Health
- `X_INCREASED_CAST_SPEED_WHILE_CHILLED` — {X}% increased Cast Speed while Chilled
- `X_INCREASED_CAST_SPEED_WHILE_IGNITED` — {X}% increased Cast Speed while Ignited
- `X_INCREASED_CAST_SPEED_WHILE_ON_FULL_MANA` — {X}% increased Cast Speed while on Full Mana
- `X_INCREASED_CAST_SPEED_WITH_COLD_SKILLS` — {X}% increased Cast Speed with Cold Skills
- `X_INCREASED_CHANCE_TO_BLOCK_IF_YOU_VE_BLOCKED_WITH_A_RAISED_SHIELD_RECENTLY` — {X}% increased Chance to Block if you've Blocked with a raised Shield Recently
- `X_INCREASED_CHAOS_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_PLAGUE` — {X}% increased Chaos Damage while affected by Herald of Plague
- `X_INCREASED_CHARM_EFFECT_DURATION` — {X}% increased Charm Effect Duration
- `X_INCREASED_CHILL_DURATION_ON_ENEMIES` — {X}% increased Chill Duration on Enemies
- `X_INCREASED_CHILL_AND_FREEZE_DURATION_ON_ENEMIES` — {X}% increased Chill and Freeze Duration on Enemies
- `X_INCREASED_COLD_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ICE` — {X}% increased Cold Damage while affected by Herald of Ice
- `X_INCREASED_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS` — {X}% increased Cooldown Recovery Rate for Grenade Skills
- `X_INCREASED_COST_EFFICIENCY` — {X}% increased Cost Efficiency
- `X_INCREASED_COST_EFFICIENCY_OF_ATTACKS` — {X}% increased Cost Efficiency of Attacks
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` — {X}% increased Critical Damage Bonus against Enemies that are on Full Health
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_HAVE_EXITED_YOUR_PRESENCE_RECENTLY` — {X}% increased Critical Damage Bonus against Enemies that have exited your Presence Recently
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY` — {X}% increased Critical Damage Bonus if you haven't dealt a Critical Hit Recently
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY` — {X}% increased Critical Damage Bonus if you've dealt a Non-Critical Hit Recently
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_GAIN_A_POWER_CHARGE_RECENTLY` — {X}% increased Critical Damage Bonus if you've gain a Power Charge Recently
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_BOWS` — {X}% increased Critical Damage Bonus with Bows
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_CROSSBOWS` — {X}% increased Critical Damage Bonus with Crossbows
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS` — {X}% increased Critical Damage Bonus with Daggers
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_FLAILS` — {X}% increased Critical Damage Bonus with Flails
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_ONE_HANDED_MELEE_WEAPONS` — {X}% increased Critical Damage Bonus with One Handed Melee Weapons
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES` — {X}% increased Critical Damage Bonus with Quarterstaves
- `X_INCREASED_CRITICAL_DAMAGE_BONUS_WITH_SPEARS` — {X}% increased Critical Damage Bonus with Spears
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_BLEEDING_ENEMIES` — {X}% increased Critical Hit Chance against Bleeding Enemies
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES` — {X}% increased Critical Hit Chance against Blinded Enemies
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_DAZED_ENEMIES` — {X}% increased Critical Hit Chance against Dazed Enemies
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_AFFECTED_BY_NO_ELEMENTAL_AILMENTS` — {X}% increased Critical Hit Chance against Enemies that are affected by no Elemental Ailments
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` — {X}% increased Critical Hit Chance against Enemies that are on Full Health
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY` — {X}% increased Critical Hit Chance against Enemies that have entered your Presence Recently
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_HUMANOIDS` — {X}% increased Critical Hit Chance against Humanoids
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES` — {X}% increased Critical Hit Chance against Marked Enemies
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_SHOCKED_ENEMIES` — {X}% increased Critical Hit Chance against Shocked Enemies
- `X_INCREASED_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_WITH_EXPOSURE` — {X}% increased Critical Hit Chance against enemies with Exposure
- `X_INCREASED_CRITICAL_HIT_CHANCE_FOR_ATTACKS` — {X}% increased Critical Hit Chance for Attacks
- `X_INCREASED_CRITICAL_HIT_CHANCE_FOR_SPELLS` — {X}% increased Critical Hit Chance for Spells
- `X_INCREASED_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY` — {X}% increased Critical Hit Chance if you have Killed Recently
- `X_INCREASED_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` — {X}% increased Critical Hit Chance if you have Shapeshifted to an Animal form Recently
- `X_INCREASED_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY` — {X}% increased Critical Hit Chance if you haven't dealt a Critical Hit Recently
- `X_INCREASED_CRITICAL_HIT_CHANCE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY` — {X}% increased Critical Hit Chance if you've Triggered a Skill Recently
- `X_INCREASED_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED` — {X}% increased Critical Hit Chance while Shapeshifted
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS` — {X}% increased Critical Hit Chance with Crossbows
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_DAGGERS` — {X}% increased Critical Hit Chance with Daggers
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_FLAILS` — {X}% increased Critical Hit Chance with Flails
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_ONE_HANDED_MELEE_WEAPONS` — {X}% increased Critical Hit Chance with One Handed Melee Weapons
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_QUARTERSTAVES` — {X}% increased Critical Hit Chance with Quarterstaves
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_SPEARS` — {X}% increased Critical Hit Chance with Spears
- `X_INCREASED_CRITICAL_HIT_CHANCE_WITH_TRAPS` — {X}% increased Critical Hit Chance with Traps
- `X_INCREASED_CRITICAL_SPELL_DAMAGE_BONUS` — {X}% increased Critical Spell Damage Bonus
- `X_INCREASED_CULLING_STRIKE_THRESHOLD_AGAINST_IMMOBILISED_ENEMIES` — {X}% increased Culling Strike Threshold against Immobilised Enemies
- `X_INCREASED_CULLING_STRIKE_THRESHOLD_AGAINST_RARE_OR_UNIQUE_ENEMIES` — {X}% increased Culling Strike Threshold against Rare or Unique Enemies
- `X_INCREASED_DAMAGE` — {X}% increased Damage
- `X_INCREASED_DAMAGE_AGAINST_DAZED_ENEMIES` — {X}% increased Damage against Dazed Enemies
- `X_INCREASED_DAMAGE_AGAINST_DEMONS` — {X}% increased Damage against Demons
- `X_INCREASED_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR` — {X}% increased Damage against Enemies with Fully Broken Armor
- `X_INCREASED_DAMAGE_AGAINST_IMMOBILISED_ENEMIES` — {X}% increased Damage against Immobilised Enemies
- `X_INCREASED_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED` — {X}% increased Damage against Immobilised Enemies while Shapeshifted
- `X_INCREASED_DAMAGE_DURING_ANY_FLASK_EFFECT` — {X}% increased Damage during any Flask Effect
- `X_INCREASED_DAMAGE_FOR_EACH_HAZARD_TRIGGERED_RECENTLY_UP_TO_X` — {X}% increased Damage for each Hazard triggered Recently, up to {X}%
- `X_INCREASED_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY` — {X}% increased Damage for each different Warcry you've used Recently
- `X_INCREASED_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY` — {X}% increased Damage for each time you've Warcried Recently
- `X_INCREASED_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY` — {X}% increased Damage for each type of Elemental Ailment on Enemy
- `X_INCREASED_DAMAGE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY` — {X}% increased Damage if you've Triggered a Skill Recently
- `X_INCREASED_DAMAGE_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY` — {X}% increased Damage if you've consumed a Frenzy Charge Recently
- `X_INCREASED_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY` — {X}% increased Damage if you've dealt a Critical Hit Recently
- `X_INCREASED_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_X_SECONDS` — {X}% increased Damage if you've dealt a Critical Hit in the past {X} seconds
- `X_INCREASED_DAMAGE_PER_X_STRENGTH` — {X}% increased Damage per {X} Strength
- `X_INCREASED_DAMAGE_PER_X_OF_YOUR_LOWEST_ATTRIBUTES` — {X}% increased Damage per {X} of your lowest Attributes
- `X_INCREASED_DAMAGE_PER_X_CHANCE_TO_BLOCK` — {X}% increased Damage per {X}% Chance to Block
- `X_INCREASED_DAMAGE_WHILE_LEECHING` — {X}% increased Damage while Leeching
- `X_INCREASED_DAMAGE_WHILE_LEECHING_HEALTH` — {X}% increased Damage while Leeching Health
- `X_INCREASED_DAMAGE_WHILE_AFFECTED_BY_A_HERALD` — {X}% increased Damage while affected by a Herald
- `X_INCREASED_DAMAGE_WHILE_YOU_HAVE_A_TOTEM` — {X}% increased Damage while you have a Totem
- `X_INCREASED_DAMAGE_WHILE_YOU_HAVE_AN_ACTIVE_CHARM` — {X}% increased Damage while you have an active Charm
- `X_INCREASED_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` — {X}% increased Damage while your Companion is in your Presence
- `X_INCREASED_DAMAGE_WITH_CROSSBOWS` — {X}% increased Damage with Crossbows
- `X_INCREASED_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_X_SECONDS` — {X}% increased Damage with Crossbows for each type of Ammunition fired in the past {X} seconds
- `X_INCREASED_DAMAGE_WITH_DAGGERS` — {X}% increased Damage with Daggers
- `X_INCREASED_DAMAGE_WITH_FLAILS` — {X}% increased Damage with Flails
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES` — {X}% increased Damage with Hit Damage against Blinded Enemies
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES` — {X}% increased Damage with Hit Damage against Burning Enemies
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_AILMENTS` — {X}% increased Damage with Hit Damage against Enemies affected by Ailments
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS` — {X}% increased Damage with Hit Damage against Enemies affected by Elemental Ailments
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` — {X}% increased Damage with Hit Damage against Enemies that are on Full Health
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` — {X}% increased Damage with Hit Damage against Enemies that are on Low Health
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_HINDERED_ENEMIES` — {X}% increased Damage with Hit Damage against Hindered Enemies
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES` — {X}% increased Damage with Hit Damage against Ignited Enemies
- `X_INCREASED_DAMAGE_WITH_HIT_DAMAGE_AGAINST_RARE_AND_UNIQUE_ENEMIES` — {X}% increased Damage with Hit Damage against Rare and Unique Enemies
- `X_INCREASED_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES` — {X}% increased Damage with Hits against Frozen Enemies
- `X_INCREASED_DAMAGE_WITH_HITS_AGAINST_SHOCKED_ENEMIES` — {X}% increased Damage with Hits against Shocked Enemies
- `X_INCREASED_DAMAGE_WITH_PLANT_SKILLS` — {X}% increased Damage with Plant Skills
- `X_INCREASED_DAZE_DURATION` — {X}% increased Daze Duration
- `X_INCREASED_DEFLECTION_RATING` — {X}% increased Deflection Rating
- `X_INCREASED_DEFLECTION_RATING_WHILE_MOVING` — {X}% increased Deflection Rating while moving
- `X_INCREASED_DEXTERITY` — {X}% increased Dexterity
- `X_INCREASED_DURATION_OF_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE` — {X}% increased Duration of Ailments against Enemies with Exposure
- `X_INCREASED_DURATION_OF_ELEMENTAL_AILMENTS_ON_ENEMIES` — {X}% increased Duration of Elemental Ailments on Enemies
- `X_INCREASED_DURATION_OF_IGNITE_SHOCK_AND_CHILL_ON_ENEMIES` — {X}% increased Duration of Ignite, Shock and Chill on Enemies
- `X_INCREASED_DURATION_OF_POISONS_YOU_INFLICT_AGAINST_SLOWED_ENEMIES` — {X}% increased Duration of Poisons you inflict against Slowed Enemies
- `X_INCREASED_DURATION_OF_EACH_PUPPET_MASTER_STACK` — {X}% increased Duration of each Puppet Master stack
- `X_INCREASED_EFFECT_OF_YOUR_MARK_SKILLS` — {X}% increased Effect of your Mark Skills
- `X_INCREASED_ELECTROCUTE_BUILDUP_AGAINST_SHOCKED_ENEMIES` — {X}% increased Electrocute Buildup against Shocked Enemies
- `X_INCREASED_ELEMENTAL_DAMAGE_WITH_ATTACK_SKILLS_DURING_ANY_FLASK_EFFECT` — {X}% increased Elemental Damage with Attack Skills during any Flask Effect
- `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_IF_YOU_VE_CHILLED_AN_ENEMY_RECENTLY` — {X}% increased Elemental Damage Damage if you've Chilled an Enemy Recently
- `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_IF_YOU_VE_IGNITED_AN_ENEMY_RECENTLY` — {X}% increased Elemental Damage Damage if you've Ignited an Enemy Recently
- `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_IF_YOU_VE_SHOCKED_AN_ENEMY_RECENTLY` — {X}% increased Elemental Damage Damage if you've Shocked an Enemy Recently
- `X_INCREASED_ELEMENTAL_DAMAGE_DAMAGE_WHILE_SHAPESHIFTED` — {X}% increased Elemental Damage Damage while Shapeshifted
- `X_INCREASED_ENDURANCE_CHARGE_DURATION` — {X}% increased Endurance Charge Duration
- `X_INCREASED_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION` — {X}% increased Endurance, Frenzy and Power Charge Duration
- `X_INCREASED_ENERGY_SHIELD_RECHARGE_RATE` — {X}% increased Energy Shield Recharge Rate
- `X_INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_BODY_ARMOR` — {X}% increased Energy Shield from Equipped Body Armor
- `X_INCREASED_EVASION_FROM_EQUIPPED_BODY_ARMOR` — {X}% increased Evasion from Equipped Body Armor
- `X_INCREASED_EVASION_IF_ENERGY_SHIELD_RECHARGE_HAS_STARTED_IN_THE_PAST_X_SECONDS` — {X}% increased Evasion if Energy Shield Recharge has started in the past {X} seconds
- `X_INCREASED_EVASION_IF_YOU_HAVE_HIT_AN_ENEMY_RECENTLY` — {X}% increased Evasion if you have Hit an Enemy Recently
- `X_INCREASED_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY` — {X}% increased Evasion if you have been Hit Damage Recently
- `X_INCREASED_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY` — {X}% increased Evasion if you haven't been Hit Damage Recently
- `X_INCREASED_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY` — {X}% increased Evasion if you've Dodge Rolled Recently
- `X_INCREASED_EVASION_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY` — {X}% increased Evasion if you've consumed a Frenzy Charge Recently
- `X_INCREASED_EVASION_PER_X_INTELLIGENCE` — {X}% increased Evasion per {X} Intelligence
- `X_INCREASED_EVASION_WHEN_ON_FULL_HEALTH` — {X}% increased Evasion when on Full Health
- `X_INCREASED_EVASION_WHILE_PARRYING` — {X}% increased Evasion while Parrying
- `X_INCREASED_EVASION_WHILE_SPRINTING` — {X}% increased Evasion while Sprinting
- `X_INCREASED_EVASION_WHILE_SURROUNDED` — {X}% increased Evasion while Surrounded
- `X_INCREASED_EVASION_WHILE_MOVING` — {X}% increased Evasion while moving
- `X_INCREASED_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD` — {X}% increased Evasion while you have Energy Shield
- `X_INCREASED_FIRE_DAMAGE_PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY` — {X}% increased Fire Damage per Endurance Charge consumed Recently
- `X_INCREASED_FIRE_DAMAGE_WHILE_IGNITED` — {X}% increased Fire Damage while Ignited
- `X_INCREASED_FIRE_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ASH` — {X}% increased Fire Damage while affected by Herald of Ash
- `X_INCREASED_FLASK_CHARGES_GAIN` — {X}% increased Flask Charges gain
- `X_INCREASED_FLASK_CHARGES_USED` — {X}% increased Flask Charges used
- `X_INCREASED_FLASK_EFFECT_DURATION` — {X}% increased Flask Effect Duration
- `X_INCREASED_FLASK_HEALTH_RECOVERY_RATE` — {X}% increased Flask Health Recovery rate
- `X_INCREASED_FLASK_AND_CHARM_CHARGES_GAIN` — {X}% increased Flask and Charm Charges gain
- `X_INCREASED_FREEZE_BUILDUP_WITH_QUARTERSTAVES` — {X}% increased Freeze Buildup with Quarterstaves
- `X_INCREASED_FREEZE_DURATION_ON_ENEMIES` — {X}% increased Freeze Duration on Enemies
- `X_INCREASED_GLORY_GENERATION_FOR_BANNER_SKILLS` — {X}% increased Glory generation for Banner Skills
- `X_INCREASED_GRENADE_AREA_OF_EFFECT` — {X}% increased Grenade Area of Effect
- `X_INCREASED_HAZARD_AREA_OF_EFFECT` — {X}% increased Hazard Area of Effect
- `X_INCREASED_HAZARD_IMMOBILISATION_BUILDUP` — {X}% increased Hazard Immobilisation buildup
- `X_INCREASED_HINDER_DURATION` — {X}% increased Hinder Duration
- `X_INCREASED_IMMOBILISATION_BUILDUP_AGAINST_CONSTRUCTS` — {X}% increased Immobilisation buildup against Constructs
- `X_INCREASED_INTELLIGENCE` — {X}% increased Intelligence
- `X_INCREASED_JAGGED_GROUND_DURATION` — {X}% increased Jagged Ground Duration
- `X_INCREASED_HEALTH_RECOVERY_RATE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` — {X}% increased Health Recovery Rate while your Companion is in your Presence
- `X_INCREASED_HEALTH_RECOVERY_RATE` — {X}% increased Health Recovery rate
- `X_INCREASED_HEALTH_REGENERATION_RATE_WHILE_MOVING` — {X}% increased Health Regeneration Rate while moving
- `X_INCREASED_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH` — {X}% increased Health Regeneration Rate while on Low Health
- `X_INCREASED_HEALTH_REGENERATION_RATE_WHILE_STATIONARY` — {X}% increased Health Regeneration Rate while stationary
- `X_INCREASED_HEALTH_REGENERATION_RATE_DURING_EFFECT_OF_ANY_HEALTH_FLASK` — {X}% increased Health Regeneration rate during Effect of any Health Flask
- `X_INCREASED_HEALTH_REGENERATION_RATE_WHILE_IGNITED` — {X}% increased Health Regeneration rate while Ignited
- `X_INCREASED_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED` — {X}% increased Health Regeneration rate while Shapeshifted
- `X_INCREASED_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS` — {X}% increased Health and Mana Recovery from Flasks
- `X_INCREASED_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS_WHILE_YOU_HAVE_AN_ACTIVE_CHARM` — {X}% increased Health and Mana Recovery from Flasks while you have an active Charm
- `X_INCREASED_HEALTH_AND_MANA_REGENERATION_RATE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF_X` — {X}% increased Health and Mana Regeneration Rate for each Minion in your Presence, up to a Max of {X}%
- `X_INCREASED_LIGHTNING_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_THUNDER` — {X}% increased Lightning Damage while affected by Herald of Thunder
- `X_INCREASED_MANA_COST_EFFICIENCY_OF_ATTACKS_DURING_ANY_MANA_FLASK_EFFECT` — {X}% increased Mana Cost Efficiency of Attacks during any Mana Flask Effect
- `X_INCREASED_MANA_COST_EFFICIENCY_OF_MARKS` — {X}% increased Mana Cost Efficiency of Marks
- `X_INCREASED_MANA_COST_EFFICIENCY_WHILE_ON_LOW_MANA` — {X}% increased Mana Cost Efficiency while on Low Mana
- `X_INCREASED_MANA_FLASK_CHARGES_GAIN` — {X}% increased Mana Flask Charges gain
- `X_INCREASED_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK` — {X}% increased Mana Recovery Rate during Effect of any Mana Flask
- `X_INCREASED_MANA_RECOVERY_RATE` — {X}% increased Mana Recovery rate
- `X_INCREASED_MANA_REGENERATION_RATE_IF_YOU_HAVE_SHOCKED_AN_ENEMY_RECENTLY` — {X}% increased Mana Regeneration Rate if you have Shocked an Enemy Recently
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_SHAPESHIFTED` — {X}% increased Mana Regeneration Rate while Shapeshifted
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_SHOCKED` — {X}% increased Mana Regeneration Rate while Shocked
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_MOVING` — {X}% increased Mana Regeneration Rate while moving
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_NOT_ON_LOW_MANA` — {X}% increased Mana Regeneration Rate while not on Low Mana
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_ON_FULL_HEALTH` — {X}% increased Mana Regeneration Rate while on Full Health
- `X_INCREASED_MANA_REGENERATION_RATE_WHILE_STATIONARY` — {X}% increased Mana Regeneration Rate while stationary
- `X_INCREASED_MAX_HEALTH_IF_YOU_HAVE_AT_LEAST_X_RED_SUPPORT_GEMS_SOCKETED` — {X}% increased Max Health if you have at least {X} Red Support Gems Socketed
- `X_INCREASED_MAX_MANA_IF_YOU_HAVE_AT_LEAST_X_BLUE_SUPPORT_GEMS_SOCKETED` — {X}% increased Max Mana if you have at least {X} Blue Support Gems Socketed
- `X_INCREASED_MELEE_ATTACK_SPEED` — {X}% increased Melee Attack Speed
- `X_INCREASED_MELEE_CRITICAL_HIT_CHANCE` — {X}% increased Melee Critical Hit Chance
- `X_INCREASED_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES` — {X}% increased Melee Damage against Heavy Stunned enemies
- `X_INCREASED_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES` — {X}% increased Melee Damage against Immobilised Enemies
- `X_INCREASED_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` — {X}% increased Melee Damage if you've dealt a Projectile Attack Hit Damage in the past eight seconds
- `X_INCREASED_MELEE_DAMAGE_WHEN_ON_FULL_HEALTH` — {X}% increased Melee Damage when on Full Health
- `X_INCREASED_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE` — {X}% increased Melee Damage with Hit Damage at Close Range
- `X_INCREASED_MELEE_DAMAGE_WITH_SPEARS_WHILE_SURROUNDED` — {X}% increased Melee Damage with Spears while Surrounded
- `X_INCREASED_MINION_DAMAGE_WHILE_YOU_HAVE_AT_LEAST_TWO_DIFFERENT_ACTIVE_OFFERINGS` — {X}% increased Minion Damage while you have at least two different active Offerings
- `X_INCREASED_MINION_DURATION` — {X}% increased Minion Duration
- `X_INCREASED_MOVEMENT_SPEED_PENALTY_FROM_USING_COLD_SKILLS_WHILE_MOVING` — {X}% increased Movement Speed Penalty from using Cold Skills while moving
- `X_INCREASED_MOVEMENT_SPEED_PENALTY_FROM_USING_FIRE_SKILLS_WHILE_MOVING` — {X}% increased Movement Speed Penalty from using Fire Skills while moving
- `X_INCREASED_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING` — {X}% increased Movement Speed Penalty from using Skills while moving
- `X_INCREASED_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING` — {X}% increased Movement Speed Penalty while Actively Blocking
- `X_INCREASED_MOVEMENT_SPEED_FOR_EACH_TIME_YOU_VE_BLOCKED_IN_THE_PAST_X_SECONDS` — {X}% increased Movement Speed for each time you've Blocked in the past {X} seconds
- `X_INCREASED_MOVEMENT_SPEED_IF_YOU_HAVE_AT_LEAST_X_GREEN_SUPPORT_GEMS_SOCKETED` — {X}% increased Movement Speed if you have at least {X} Green Support Gems Socketed
- `X_INCREASED_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY` — {X}% increased Movement Speed if you've Killed Recently
- `X_INCREASED_MOVEMENT_SPEED_IF_YOU_VE_PINNED_AN_ENEMY_RECENTLY` — {X}% increased Movement Speed if you've Pinned an Enemy Recently
- `X_INCREASED_MOVEMENT_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY` — {X}% increased Movement Speed if you've successfully Parried Recently
- `X_INCREASED_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY` — {X}% increased Movement Speed if you've used a Mark Recently
- `X_INCREASED_MOVEMENT_SPEED_WHILE_DUAL_WIELDING` — {X}% increased Movement Speed while Dual Wielding
- `X_INCREASED_MOVEMENT_SPEED_WHILE_SPRINTING` — {X}% increased Movement Speed while Sprinting
- `X_INCREASED_MOVEMENT_SPEED_WHILE_SURROUNDED` — {X}% increased Movement Speed while Surrounded
- `X_INCREASED_MOVEMENT_SPEED_WHILE_YOU_HAVE_ENERGY_SHIELD` — {X}% increased Movement Speed while you have Energy Shield
- `X_INCREASED_MOVEMENT_SPEED_WHILE_YOU_HAVE_AN_ACTIVE_CHARM` — {X}% increased Movement Speed while you have an active Charm
- `X_INCREASED_PARRY_HIT_DAMAGE_AREA_OF_EFFECT` — {X}% increased Parry Hit Damage Area of Effect
- `X_INCREASED_PHYSICAL_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_BLOOD` — {X}% increased Physical Damage while affected by Herald of Blood
- `X_INCREASED_PHYSICAL_DAMAGE_WITH_BOWS` — {X}% increased Physical Damage with Bows
- `X_INCREASED_POISON_DURATION_FOR_EACH_POISON_YOU_HAVE_INFLICTED_RECENTLY_UP_TO_A_MAX_OF_X` — {X}% increased Poison Duration for each Poison you have inflicted Recently, up to a Max of {X}%
- `X_INCREASED_POWER_CHARGE_DURATION` — {X}% increased Power Charge Duration
- `X_INCREASED_PRESENCE_AREA_OF_EFFECT` — {X}% increased Presence Area of Effect
- `X_INCREASED_PROJECTILE_DAMAGE_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` — {X}% increased Projectile Damage if you've dealt a Melee Hit Damage in the past eight seconds
- `X_INCREASED_PROJECTILE_DAMAGE_WITH_SPEARS_WHILE_THERE_ARE_NO_ENEMIES_WITHIN_XM` — {X}% increased Projectile Damage with Spears while there are no Enemies within {X}m
- `X_INCREASED_PROJECTILE_SPEED_FOR_SPELL_SKILLS` — {X}% increased Projectile Speed for Spell Skills
- `X_INCREASED_PROJECTILE_STUN_BUILDUP` — {X}% increased Projectile Stun Buildup
- `X_INCREASED_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS` — {X}% increased Reservation Efficiency of Companion Skills
- `X_INCREASED_RESERVATION_EFFICIENCY_OF_MINION_SKILLS` — {X}% increased Reservation Efficiency of Minion Skills
- `X_INCREASED_SHOCK_DURATION` — {X}% increased Shock Duration
- `X_INCREASED_SKILL_EFFECT_DURATION_PER_ENEMY_YOU_VE_FREEZE_IN_THE_LAST_X_SECONDS_UP_TO_X` — {X}% increased Skill Effect Duration per Enemy you've Freeze in the last {X} seconds, up to {X}%
- `X_INCREASED_SKILL_SPEED_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY` — {X}% increased Skill Speed if you've consumed a Frenzy Charge Recently
- `X_INCREASED_SKILL_SPEED_WHILE_SHAPESHIFTED` — {X}% increased Skill Speed while Shapeshifted
- `X_INCREASED_SKILL_SPEED_WITH_CHANNELLING_SKILLS` — {X}% increased Skill Speed with Channelling Skills
- `X_INCREASED_SPELL_AREA_DAMAGE` — {X}% increased Spell Area Damage
- `X_INCREASED_SPELL_DAMAGE` — {X}% increased Spell Damage
- `X_INCREASED_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK_YOU_VE_USED_IN_THE_PAST_X_SECONDS` — {X}% increased Spell Damage for each different Non-Instant Attack you've used in the past {X} seconds
- `X_INCREASED_SPELL_DAMAGE_IF_ONE_OF_YOUR_MINIONS_HAS_DIED_RECENTLY` — {X}% increased Spell Damage if one of your Minions has died Recently
- `X_INCREASED_SPELL_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_HUMAN_FORM_RECENTLY` — {X}% increased Spell Damage if you have Shapeshifted to Human form Recently
- `X_INCREASED_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY` — {X}% increased Spell Damage if you have consumed an Elemental Infusion Recently
- `X_INCREASED_SPELL_DAMAGE_PER_X_STRENGTH` — {X}% increased Spell Damage per {X} Strength
- `X_INCREASED_SPELL_DAMAGE_PER_X_MAX_MANA` — {X}% increased Spell Damage per {X} Max Mana
- `X_INCREASED_SPELL_DAMAGE_WHILE_ON_FULL_ENERGY_SHIELD` — {X}% increased Spell Damage while on Full Energy Shield
- `X_INCREASED_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON` — {X}% increased Spell Damage while wielding a Melee Weapon
- `X_INCREASED_SPELL_DAMAGE_WHILE_YOU_HAVE_ARCANE_SURGE` — {X}% increased Spell Damage while you have Arcane Surge
- `X_INCREASED_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH` — {X}% increased Spell Damage with Spells that cost Health
- `X_INCREASED_SPIRIT_RESERVATION_EFFICIENCY_OF_SKILLS` — {X}% increased Spirit Reservation Efficiency of Skills
- `X_INCREASED_STRENGTH` — {X}% increased Strength
- `X_INCREASED_STUN_BUILDUP_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` — {X}% increased Stun Buildup against Enemies that are on Low Health
- `X_INCREASED_STUN_BUILDUP_AGAINST_ENEMIES_WITHIN_X_METRES` — {X}% increased Stun Buildup against enemies within {X} metres
- `X_INCREASED_STUN_BUILDUP_WITH_MACES` — {X}% increased Stun Buildup with Maces
- `X_INCREASED_STUN_BUILDUP_WITH_MELEE_DAMAGE` — {X}% increased Stun Buildup with Melee Damage
- `X_INCREASED_STUN_BUILDUP_WITH_QUARTERSTAVES` — {X}% increased Stun Buildup with Quarterstaves
- `X_INCREASED_STUN_THRESHOLD_DURING_EMPOWERED_ATTACKS` — {X}% increased Stun Threshold during Empowered Attacks
- `X_INCREASED_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_HIT_DAMAGE_BY_AN_ENEMY_RECENTLY_UP_TO_X` — {X}% increased Stun Threshold for each time you've been Hit Damage by an Enemy Recently, up to {X}%
- `X_INCREASED_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_STUNNED_RECENTLY` — {X}% increased Stun Threshold for each time you've been Stunned Recently
- `X_INCREASED_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY` — {X}% increased Stun Threshold if you haven't been Stunned Recently
- `X_INCREASED_STUN_THRESHOLD_WHILE_CHANNELLING` — {X}% increased Stun Threshold while Channelling
- `X_INCREASED_STUN_THRESHOLD_WHILE_PARRYING` — {X}% increased Stun Threshold while Parrying
- `X_INCREASED_STUN_THRESHOLD_WHILE_SHAPESHIFTED` — {X}% increased Stun Threshold while Shapeshifted
- `X_INCREASED_STUN_THRESHOLD_WHILE_ON_FULL_HEALTH` — {X}% increased Stun Threshold while on Full Health
- `X_INCREASED_STUN_BUILDUP_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` — {X}% increased Stun buildup if you have Shapeshifted to an Animal form Recently
- `X_INCREASED_STUN_BUILDUP_WHILE_SHAPESHIFTED` — {X}% increased Stun buildup while Shapeshifted
- `X_INCREASED_SURROUNDED_AREA_OF_EFFECT` — {X}% increased Surrounded Area of Effect
- `X_INCREASED_THORNS_CRITICAL_DAMAGE_BONUS` — {X}% increased Thorns Critical Damage Bonus
- `X_INCREASED_THORNS_DAMAGE_IF_YOU_VE_BLOCKED_RECENTLY` — {X}% increased Thorns damage if you've Blocked Recently
- `X_INCREASED_TOTEM_DURATION` — {X}% increased Totem Duration
- `X_INCREASED_WARCRY_COOLDOWN_RECOVERY_RATE` — {X}% increased Warcry Cooldown Recovery Rate
- `X_INCREASED_AMOUNT_OF_HEALTH_HEALTH_LEECH_WHILE_SHAPESHIFTED` — {X}% increased amount of Health Health Leech while Shapeshifted
- `X_INCREASED_CHANCE_TO_INFLICT_AILMENTS_AGAINST_ENEMIES_WITH_EXPOSURE` — {X}% increased chance to inflict Ailments against Enemies with Exposure
- `X_INCREASED_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES` — {X}% increased chance to inflict Ailments against Rare or Unique Enemies
- `X_INCREASED_CHANCE_TO_INFLICT_AILMENTS_WITH_PROJECTILES` — {X}% increased chance to inflict Ailments with Projectiles
- `X_INCREASED_DAMAGE_AGAINST_UNDEAD_ENEMIES` — {X}% increased damage against Undead Enemies
- `X_INCREASED_DURATION_OF_AILMENTS_YOU_INFLICT_AGAINST_CURSED_ENEMIES` — {X}% increased duration of Ailments you inflict against Cursed Enemies
- `X_INCREASED_EFFECT_OF_FULLY_BROKEN_ARMOR` — {X}% increased effect of Fully Broken Armor
- `X_INCREASED_MAX_ENERGY_SHIELD_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY` — {X}% increased Max Energy Shield if you've consumed a Power Charge Recently
- `X_INCREASED_MAX_HEALTH_MANA_AND_ENERGY_SHIELD` — {X}% increased Max Health, Mana and Energy Shield
- `X_INCREASED_PENALTY_TO_ACCURACY_AT_RANGE` — {X}% increased penalty to Accuracy at range
- `X_MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` — {X}% more Buff Magnitude of Bleeding you inflict
- `X_MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH` — {X}% more Critical Damage Bonus when on Full Health
- `X_MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_LOW_HEALTH` — {X}% more Critical Damage Bonus when on Low Health
- `X_MORE_KNOCKBACK_DISTANCE_FOR_BLOCKED_HIT_DAMAGE` — {X}% more Knockback Distance for Blocked Hit Damage
- `X_MORE_HEALTH_RECOVERY_FROM_FLASKS` — {X}% more Health Recovery from Flasks
- `X_MORE_MANA_RECOVERY_RATE` — {X}% more Mana Recovery Rate
- `X_MORE_MAX_LIGHTNING_DAMAGE` — {X}% more Max Lightning Damage
- `X_MORE_MAX_RAGE` — {X}% more Max Rage
- `X_MORE_RECOVERY_FROM_FLASKS` — {X}% more Recovery from Flasks
- `X_MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS` — {X}% more Reservation Efficiency of Companion Skills
- `X_MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS` — {X}% more Reservation Efficiency of non-Companion Skills
- `X_MORE_SPIRIT` — {X}% more Spirit
- `X_MORE_STUN_BUILDUP_WITH_CRITICAL_HITS` — {X}% more Stun Buildup with Critical Hits
- `X_MORE_AMOUNT_OF_HEALTH_LEECHED` — {X}% more amount of Health Leeched
- `X_OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE` — {X}% of Damage Converted to Fire Damage
- `X_OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_MANA_AND_ENERGY_SHIELD` — {X}% of Damage Taken Recouped as Health, Mana and Energy Shield
- `X_OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH` — {X}% of Damage taken Recouped as Health
- `X_OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING` — {X}% of Damage taken Recouped as Health while Channelling
- `X_OF_DAMAGE_TAKEN_RECOUPED_AS_MANA` — {X}% of Damage taken Recouped as Mana
- `X_OF_DAMAGE_TAKEN_FROM_DEFLECTED_HIT_DAMAGE_RECOUPED_AS_HEALTH` — {X}% of Damage taken from Deflected Hit Damage Recouped as Health
- `X_OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH` — {X}% of Elemental Damage taken Recouped as Health
- `X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER` — {X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power
- `X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_HEALTH` — {X}% of Physical Damage prevented Recouped as Health
- `X_OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH` — {X}% of Physical Damage taken Recouped as Health
- `X_OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS_DURING_ANY_HEALTH_FLASK_EFFECT` — {X}% of Skill Mana costs Stat Conversion to Health Costs during any Health Flask Effect
- `X_OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST` — {X}% of Spell Mana Cost Stat Conversion to Health Cost
