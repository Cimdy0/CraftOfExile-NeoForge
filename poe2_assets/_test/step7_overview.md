# Step 7 总览 — StatIds / StatGen / AttributeRegister 三层现状（修正版）

## 1. StatIds 层
| 指标 | 数量 |
| --- | --- |
| 常量总数 | 1569 |
| 被引用（全仓 .java） | 678 |
| 死常量（零引用，含资源引用 0） | 891 |
| 　├ X_ 前缀死常量 | 563 |
| 　└ 非 X_ 死常量 | 328 |
| X_ 常量总数（被引用/死） | 709 (146/563) |
| 带 TODO 注释常量（已实现/未实现） | 187 (187/0) |

## 2. StatGen 层
| 指标 | 数量 |
| --- | --- |
| 引用常量去重数 | 642 |

## 3. AttributeRegister 层
| 指标 | 数量 |
| --- | --- |
| 属性声明总数 | 814 |
| 重复声明（同名） | 0 [] |
| 被引用（全仓 .java） | 226 |
| 死属性（零引用） | 588 |
| 编号/占位属性(_N) | 228 |

## 4. TODO 但已实现（应去 TODO，共 187）样例
- `ADD_AILMENT_THRESHOLD`  L4168  // TODO(接线) Gain additional Ailment Threshold equal to 8% of ma
- `ADD_BANNER_LINGER`  L4171  // TODO(接线) Banner Buffs linger on you for 2 seconds after you 
- `ADD_CORPSE_RETAIN_CHANCE`  L4174  // TODO(接线) 5% chance to not destroy Corpses when Consuming Cor
- `ADD_EMPOWERED_ATTACKS`  L4177  // TODO(接线) Warcries have 15% chance to Empowered 3 additional 
- `ADD_EXPOSURE`  L4180  // TODO(接线) Exposure you inflict lowers Resistances by an addit
- `ADD_MAX_BANNER`  L4183  // TODO(接线) +1 to maximum number of placed Banners
- `ADD_MAX_ENDURANCE`  L4186  // TODO(接线) +1 to Maximum Endurance Charges
- `ADD_MAX_FISSURE`  L4189  // TODO(接线) Fissure Skills have +1 to Limit
- `ADD_MAX_MINION`  L4192  // TODO(接线) Temporary Minion Skills have +2 to Limit of Minions
- `ADD_MAX_ORB`  L4195  // TODO(接线) Orb Skills have +1 to Limit
- `ADD_MAX_RESISTANCES`  L4198  // TODO(接线) +1% to Maximum Resistances of each ElementalDamage 
- `ADD_PROJECTILE_PIERCE_CHANCE`  L4201  // TODO(接线) 15% chance to Pierce an Enemy
- `AGGRAVATE_BLEEDING_ON_HIT`  L4204  // TODO(接线) Bleeding you inflict is Aggravated Base Bleeding Du
- `AILMENT_MAGNITUDE_BLEED`  L4207  // TODO(接线) 10% increased BuffMagnitude of Bleeding you inflict
- `AILMENT_MAGNITUDE_CHILL`  L4210  // TODO(接线) 10% increased BuffMagnitude of Shock you inflict
- `AILMENT_MAGNITUDE_IGNITE`  L4213  // TODO(接线) 15% increased Flammability BuffMagnitude
- `AILMENT_MAGNITUDE_POISON`  L4216  // TODO(接线) 10% increased BuffMagnitude of Poison you inflict
- `AILMENT_MAGNITUDE_SHOCK`  L4219  // TODO(接线) 15% increased BuffMagnitude of Shock you inflict
- `AVATAR_OF_FIRE_CHAOS_TO_FIRE`  L4222  // TODO(接线) 75% of Damage Converted to Fire Damage Deal no Non-
- `AVATAR_OF_FIRE_COLD_TO_FIRE`  L4225  // TODO(接线) 75% of Damage Converted to Fire Damage Deal no Non-
- `AVATAR_OF_FIRE_LIGHT_TO_FIRE`  L4228  // TODO(接线) 75% of Damage Converted to Fire Damage Deal no Non-
- `AVATAR_OF_FIRE_PHYS_TO_FIRE`  L4231  // TODO(接线) 75% of Damage Converted to Fire Damage Deal no Non-
- `BLACKFLAME_COVENANT_FIRE_TO_CHAOS`  L4234  // TODO(接线) Fire Spells Conversion 100% of Fire Damage to Chaos
- `BLOOD_MAGIC_ENERGY_TO_LIFE`  L4237  // TODO(接线) You have no Mana Skill Mana Costs StatConversion to
- `BLOOD_MAGIC_MANA_TO_LIFE`  L4240  // TODO(接线) You have no Mana Skill Mana Costs StatConversion to
- `CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD`  L4243  // TODO(接线) Attack HitDamage Aggravate any Bleeding on targets 
- `CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED`  L4246  // TODO(接线) 20% chance for Bleeding to be Aggravated when Infli
- `CHANCE_AGGRAVATE_ON_HIT_VS_PINNED`  L4249  // TODO(接线) 30% increased Pinned Buildup
- `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL`  L4252  // TODO(接线) Attacks gain increased Accuracy Rating equal to the
- `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL`  L4255  // TODO(接线) Attacks gain increased Accuracy Rating equal to the
- `CHANCE_DAZE_ON_HIT`  L4258  // TODO(接线) 5% chance to Daze on HitDamage
- `CHANCE_DEBILITATE_ON_HIT`  L4261  // TODO(接线) 25% increased Warcry Speed
- `CHANCE_FLASK_NO_CONSUME`  L4264  // TODO(接线) 10% chance for Flasks you use to not consume Charge
- `CHANCE_ONSLAUGHT_ON_KILL`  L4267  // TODO(接线) 50% chance to gain Onslaught on Killing Blow with A
- `CHAOS_INOCULATION_IMMUNE_BLEED`  L4270  // TODO(接线) Maximum Life is 1 Immune to Chaos Damage and Bleedi
- `CHAOS_INOCULATION_IMMUNE_CHAOS`  L4273  // TODO(接线) Maximum Life is 1 Immune to Chaos Damage and Bleedi
- `CHAOS_INOCULATION_MAX_HEALTH`  L4276  // TODO(接线) Maximum Life is 1 Immune to Chaos Damage and Bleedi
- `CHARGE_CONVERT_CONDUIT`  L4279  // TODO(接线) If you would gain a Charges, Allies in your Presenc
- `CHARGE_CONVERT_GAIN_MAX_ENDURANCE`  L4282  // TODO(接线) 2% chance that if you would gain Endurance Charges,
- `CHARGE_CONVERT_GAIN_MAX_FRENZY`  L4285  // TODO(接线) 2% chance that if you would gain Frenzy Charges, yo
- `CHARGE_CONVERT_GAIN_MAX_POWER`  L4288  // TODO(接线) 2% chance that if you would gain Power Charges, you
- `CHARGE_CONVERT_RESONANCE`  L4291  // TODO(接线) Gain Power Charges instead of Frenzy Charges Gain F
- `CONVERTS_ALL_EVASION_RATING`  L4294  // TODO(接线) StatConversion all Evasion Rating to Armour
- `CONVERT_EXTRA_GENERIC_TO_COLD`  L4297  // TODO(接线) Gain 15% of Damage as Extra Cold Damage while on Ch
- `CONVERT_EXTRA_GENERIC_TO_LIGHTNING`  L4300  // TODO(接线) Gain 15% of Damage as Extra Lightning Damage while 
- `CONVERT_EXTRA_GENERIC_TO_PHYSICAL`  L4303  // TODO(接线) Gain 10% of Damage as Extra Physical Damage
- `CONVERT_EXTRA_PHYS_TO_COLD`  L4306  // TODO(接线) 5% chance to Daze on HitDamage
- `CONVERT_EXTRA_PHYS_TO_LIGHTNING`  L4309  // TODO(接线) 5% chance to Daze on HitDamage
- `DAMAGE_LUCKY`  L4312  // TODO(接线) Damage with Hits is Lucky against Enemies that are 
- `DAMAGE_LUCKY_VS_LOW_LIFE`  L4315  // TODO(接线) Damage with Hits is Lucky against Enemies that are 

## 5. 死常量（零引用，共 891）样例（前 80）
- `ACCURACY_IS_DOUBLED`  // Accuracy is Doubled
- `ADD_X_TO_X_LIGHTNING_DAMAGE_TO_ATTACKS`  // add {X} to {X} Lightning damage to Attacks
- `ALLIES_IN_YOUR_PRESENCE_DEAL_X_INCREASED_DAMAGE`  // Allies in your Presence deal {X}% increased Damage
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_ATTACK_SPEED`  // Allies in your Presence have {X}% increased Attack Speed
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_CAST_SPEED`  // Allies in your Presence have {X}% increased Cast Speed
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE`  // Allies in your Presence have {X}% increased Critical Hit Cha
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_INCREASED_GLORY_GENERATION`  // Allies in your Presence have {X}% increased Glory generation
- `ALLIES_IN_YOUR_PRESENCE_HAVE_X_TO_ACCURACY`  // Allies in your Presence have +{X} to Accuracy
- `ALLIES_IN_YOUR_PRESENCE_REGENERATE_X_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY`  // Allies in your Presence Regenerate {X} Rage per second if yo
- `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH`  // All Damage Types is taken from Mana before Health
- `ANCESTRALLY_BOOSTED_ATTACKS_DEAL_X_INCREASED_DAMAGE`  // Ancestrally Boosted Attacks deal {X}% increased Damage
- `APPLY_DEBILITATE_TO_ENEMIES_X_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED`  // Apply Debilitate to Enemies {X} Metres in front of you while
- `ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE`  // Arcane Surge grants more Health Regeneration Rate instead of
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_CRITICAL_DAMAGE_BONUS`  // Archon Buffs also grant {X}% increased Critical Damage Bonus
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_CRITICAL_HIT_CHANCE`  // Archon Buffs also grant {X}% increased Critical Hit Chance
- `ARCHON_BUFFS_ALSO_GRANT_X_INCREASED_MOVEMENT_SPEED`  // Archon Buffs also grant {X}% increased Movement Speed
- `ARCHON_BUFFS_ALSO_GRANT_X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES`  // Archon Buffs also grant +{X}% to all Elemental Damage Resist
- `ARCHON_RECOVERY_PERIOD_EXPIRES_X_FASTER`  // Archon recovery period expires {X}% faster
- `AREA_SKILLS_HAVE_X_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE`  // Area Skills have {X}% chance to Knock Enemies Back on Hit Da
- `ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_X_OF_PHYSICAL_DAMAGE_DEALT`  // Armor Break Armor on Critical Hit with Spells equal to {X}% 
- `ARMOR_BREAK_X_INCREASED_ARMOR`  // Armor Break {X}% increased Armor
- `ARMOR_BREAK_X_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY`  // Armor Break {X}% of Armor on Heavy Stunning an Enemy
- `ARMOR_BREAK_X_OF_ARMOR_ON_PINNING_AN_ENEMY`  // Armor Break {X}% of Armor on Pinning an Enemy
- `ARROWS_GAIN_CRITICAL_HIT_CHANCE_AS_THEY_TRAVEL_FARTHER_UP_TO_X_INCREASED_CRITICAL_HIT_CHANCE_AFTER_X_METRES`  // Arrows gain Critical Hit Chance as they travel farther, up t
- `ATTACKS_GAIN_INCREASED_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE`  // Attacks gain increased Accuracy equal to their Critical Hit 
- `ATTACKS_HAVE_X_CHANCE_TO_MAIM_ON_HIT_DAMAGE`  // Attacks have {X}% chance to Maim on Hit Damage
- `ATTACKS_USED_BY_BALLISTAS_HAVE_X_INCREASED_ATTACK_SPEED`  // Attacks used by Ballistas have {X}% increased Attack Speed
- `ATTACKS_USED_BY_TOTEMS_HAVE_X_INCREASED_ATTACK_SPEED`  // Attacks used by Totems have {X}% increased Attack Speed
- `ATTACKS_USED_BY_TOTEMS_HAVE_X_INCREASED_ATTACK_SPEED_PER_SUMMONED_TOTEM`  // Attacks used by Totems have {X}% increased Attack Speed per 
- `ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_X_SECONDS`  // Attack Hit Damage Aggravate any Bleeding on targets which is
- `ATTACK_SKILLS_DEAL_X_INCREASED_DAMAGE_WHILE_HOLDING_A_SHIELD`  // Attack Skills deal {X}% increased Damage while holding a Shi
- `ATTACK_SKILLS_HAVE_X_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS`  // Attack Skills have +{X} to Max number of Summoned Ballista T
- `AURA_SKILLS_HAVE_X_INCREASED_BUFF_MAGNITUDE`  // Aura Skills have {X}% increased Buff Magnitude
- `BANNERS_ALSO_GRANT_X_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_AFFECTED_TARGETS`  // Banners also grant +{X}% to all Elemental Damage Resistances
- `BANNER_BUFFS_LINGER_ON_YOU_FOR_X_SECONDS_AFTER_YOU_LEAVE_THE_AREA`  // Banner Buffs linger on you for {X} seconds after you leave t
- `BANNER_SKILLS_HAVE_X_INCREASED_AREA_OF_EFFECT`  // Banner Skills have {X}% increased Area of Effect
- `BASE_BLEEDING_DURATION_IS_X_SECOND`  // Base Bleeding Duration is {X} second
- `BLEEDING_YOU_INFLICT_IS_AGGRAVATED`  // Bleeding you inflict is Aggravated
- `BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED`  // Bleeding you inflict on Pinned Enemies is Aggravated
- `BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_X_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY`  // Bolts fired by Crossbow Attacks have {X}% chance to not expe
- `BOW_ATTACKS_HAVE_CULLING_STRIKE`  // Bow Attacks have Culling Strike
- `BURNING_ENEMIES_YOU_KILL_HAVE_A_X_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE`  // Burning Enemies you kill have a {X}% chance to Explode, deal
- `CANNOT_BE_LIGHT_STUNNED`  // Cannot be Light Stunned
- `CANNOT_BE_LIGHT_STUNNED_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY`  // Cannot be Light Stunned if you haven't been Hit Damage Recen
- `CANNOT_DODGE_ROLL_OR_SPRINT`  // Cannot Dodge Roll or Sprint
- `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH`  // Cannot Recover Health other than from Health Leech
- `CANNOT_USE_CHARMS`  // Cannot use Charms
- `CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY`  // Can Attack as though using a Quarterstaff while both of your
- `CHANCE_TO_DEFLECT_IS_LUCKY`  // Chance to Deflect is Lucky
- `CHANCE_TO_EVASION_IS_UNLUCKY`  // Chance to Evasion is Unlucky
- `CHANNELLING_SKILLS_DEAL_X_INCREASED_DAMAGE`  // Channelling Skills deal {X}% increased Damage
- `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE`  // Chaos Damage from Fire Spells Contributes to Flammability an
- `CHARMS_APPLIED_TO_YOU_HAVE_X_INCREASED_EFFECT`  // Charms applied to you have {X}% increased Effect
- `CHARMS_GAIN_X_CHARGES_PER_SECOND`  // Charms gain {X} charges per Second
- `COMPANIONS_DEAL_X_INCREASED_DAMAGE_AGAINST_IMMOBILISED_ENEMIES`  // Companions deal {X}% increased damage against Immobilised en
- `COMPANIONS_GAIN_X_DAMAGE_AS_EXTRA_CHAOS_DAMAGE`  // Companions gain {X}% Damage as extra Chaos Damage
- `COMPANIONS_GAIN_X_DAMAGE_AS_EXTRA_COLD_DAMAGE`  // Companions gain {X}% Damage as extra Cold Damage
- `COMPANIONS_HAVE_A_X_CHANCE_TO_POISON_ON_HIT_DAMAGE`  // Companions have a {X}% chance to Poison on Hit Damage
- `COMPANIONS_HAVE_X_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILL`  // Companions have {X}% chance to gain Onslaught on Kill
- `COMPANIONS_HAVE_X_INCREASED_AREA_OF_EFFECT`  // Companions have {X}% increased Area of Effect
- `COMPANIONS_HAVE_X_INCREASED_MAX_HEALTH`  // Companions have {X}% increased Max Health
- `COMPANIONS_HAVE_X_INCREASED_MOVEMENT_SPEED`  // Companions have {X}% increased Movement Speed
- `COMPANIONS_HAVE_X_TO_ALL_ELEMENTAL_RESISTANCES`  // Companions have +{X}% to all Elemental Resistances
- `COMPANIONS_HAVE_X_TO_CHAOS_RESISTANCES`  // Companions have +{X}% to Chaos Resistances
- `COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED`  // Companions in your Presence have Onslaught while you are Sha
- `CONSUMING_GLORY_GRANTS_YOU_X_INCREASED_ATTACK_DAMAGE_PER_GLORY_CONSUMED_FOR_X_SECONDS_UP_TO_X`  // Consuming Glory grants you {X}% increased Attack damage per 
- `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING`  // Create Cold Elemental Infusion Remnants instead of Lightning
- `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD`  // Create Fire Elemental Infusion Remnants instead of Cold
- `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE`  // Create Lightning Elemental Infusion Remnants instead of Fire
- `CRITICAL_HITS_WITH_DAGGERS_HAVE_A_X_CHANCE_TO_POISON_THE_ENEMY`  // Critical Hits with Daggers have a {X}% chance to Poison the 
- `CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE`  // Culling Strike against Beasts while your Companion is in you
- `DAMAGE_PENETRATION_X_COLD_RESISTANCE`  // Damage Penetration {X}% Cold Resistance
- `DAMAGE_PENETRATION_X_FIRE_RESISTANCE`  // Damage Penetration {X}% Fire Resistance
- `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCE`  // Damage Penetration {X}% Lightning Resistance
- `DAMAGE_PENETRATION_X_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA`  // Damage Penetration {X}% Lightning Resistances if on Low Mana
- `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH`  // Damage with Hits is Lucky against Enemies that are on Low He
- `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_HEAVY_STUNNED_ENEMIES`  // Damage with Hits is Lucky against Heavy Stunned Enemies
- `DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE`  // Damaging Ailments Cannot Be inflicted on you while you alrea
- `DEAL_NO_NON_FIRE_DAMAGE`  // Deal no Non-Fire Damage
- `DEBUFFS_INFLICTED_BY_HAZARDS_HAVE_X_INCREASED_SLOW_MAGNITUDE`  // Debuffs inflicted by Hazards have {X}% increased Slow Magnit

## 6. 死属性（零引用，共 588）样例（前 80）
- `ADAPTATIONS_DURATION`
- `ADDITIONAL_AILMENT_THRESHOLD_EQUAL`
- `ADDITIONAL_AILMENT_THRESHOLD_EQUAL_1`
- `ADDITIONAL_CURSE_DOUBLE_ACTIVATION`
- `ADDITIONAL_CURSE_DOUBLE_ACTIVATION_1`
- `ADDITIONAL_LIFE_EQUAL_ITEM`
- `ADDITIONAL_LIFE_EQUAL_ITEM_1`
- `ADDITIONAL_STUN_THRESHOLD_EQUAL`
- `ADDITIONAL_STUN_THRESHOLD_EQUAL_1`
- `ADDITIONAL_STUN_THRESHOLD_EQUAL_3`
- `AGGRAVATE_BLEEDING_TARGETS_ATTACKS`
- `AGGRAVATE_BLEEDING_TARGETS_ATTACKS_1`
- `AGGRAVATE_BLEEDING_TARGETS_EMPOWERED_1`
- `AILMENT_THRESHOLD_DEXTERITY`
- `AILMENT_THRESHOLD_DEXTERITY_1`
- `AILMENT_THRESHOLD_EQUAL_LOWEST`
- `AILMENT_THRESHOLD_EQUAL_LOWEST_1`
- `ALLIES_IN_PRESENCE_REGENERATE`
- `ALLIES_IN_PRESENCE_REGENERATE_2`
- `ALLOCATE_PASSIVE_SORCERESS_STARTING`
- `ALLOCATE_PASSIVE_SORCERESS_STARTING_1`
- `ALLOCATE_PASSIVE_WARRIOR_STARTING`
- `ALLOCATE_PASSIVE_WARRIOR_STARTING_1`
- `ALL_BONUSES_AMULET_MINIONS`
- `ALL_ELEMENTAL_RESISTANCES_RED`
- `ALL_FLAMES_CHAYULA_MANIFEST`
- `ALL_FLAMES_CHAYULA_MANIFEST_1`
- `ALL_FLAMES_CHAYULA_MANIFEST_2`
- `ALL_RAGE_SHAPESHIFTING_HUMAN`
- `ARCANE_SURGE`
- `ARCANE_SURGE_EFFECT_TAKEN`
- `ARCANE_SURGE_LIFE_REGENERATION`
- `ARCANE_SURGE_LIFE_REGENERATION_1`
- `ARCANE_SURGE_SHAPESHIFT_HUMAN`
- `ARCANE_SURGE_SHAPESHIFT_HUMAN_1`
- `ARCHON_RECOVERY_PERIOD_EXPIRES`
- `ARCHON_UNDEATH_COMMAND`
- `ARCHON_UNDEATH_COMMAND_1`
- `ARMOUR_EQUAL_TOTAL_STRENGTH`
- `ARMOUR_EQUAL_TOTAL_STRENGTH_1`
- `ATTACKS_EXCEED_ADDITIONAL_CRITICAL`
- `ATTACKS_EXCEED_ADDITIONAL_CRITICAL_1`
- `ATTACKS_MAIM_POISONED_1`
- `ATTACKS_WEAPONS_ADDED_PHYSICAL`
- `ATTACKS_WEAPONS_ADDED_PHYSICAL_1`
- `ATTACK_AGGRAVATE_ANY_BLEEDING`
- `ATTACK_AGGRAVATE_ANY_BLEEDING_1`
- `ATTACK_NUMBER_SUMMONED_BALLISTA`
- `ATTACK_NUMBER_SUMMONED_BALLISTA_1`
- `ATTACK_TEN_INCISION_1`
- `AVOID_DEATH_CHANCE`
- `BANNERS_GLORY`
- `BANNER_BUFFS_LINGER_LEAVE`
- `BANNER_BUFFS_LINGER_LEAVE_1`
- `BANNER_EXPIRES_RECOVER_GLORY`
- `BANNER_EXPIRES_RECOVER_GLORY_1`
- `BASE_CRITICAL_SPELLS_IS`
- `BASE_LIFE_COST_EQUAL`
- `BEAR_SPIRIT_GAINS_EMBRACE`
- `BECOME_IGNITED_CRITICAL_TAKING`
- `BECOME_IGNITED_CRITICAL_TAKING_1`
- `BENEFITS_BONDED_MODIFIERS_RUNES`
- `BENEFITS_BONDED_MODIFIERS_RUNES_1`
- `BENEFITS_FRENZY_CHARGES_DOUBLED`
- `BENEFITS_FRENZY_CHARGES_DOUBLED_1`
- `BLEEDING_AGGRAVATED_INFLICTED_JAGGED`
- `BLEEDING_AGGRAVATED_INFLICTED_JAGGED_1`
- `BLEEDING_INFLICT_CURSED_TARGETS`
- `BLEEDING_INFLICT_CURSED_TARGETS_1`
- `BLEEDING_INFLICT_PINNED_IS`
- `BLEEDING_INFLICT_PINNED_IS_1`
- `BODY_ARMOUR_UNAFFECTED_DAMAGING`
- `BODY_ARMOUR_UNAFFECTED_DAMAGING_1`
- `BOLTS_FIRED_CROSSBOW_ATTACKS`
- `BOLTS_FIRED_CROSSBOW_ATTACKS_1`
- `BONUSES_FOCUS`
- `BOW_ATTACKS_CULLING_STRIKE`
- `BOW_ATTACKS_CULLING_STRIKE_1`
- `BREAK_ARMOUR_BELOW`
- `BREAK_ARMOUR_CRITICAL_SPELLS`
- ... 共 588
