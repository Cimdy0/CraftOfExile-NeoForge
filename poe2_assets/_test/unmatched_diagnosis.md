# 594 条失配 · 溯源诊断

| 归因 | 数量 | 根因 | 处置 |
|---|---|---|---|
| D1 多行合并型 | 48 | 旧管道未按行拆分（第 3 步已修的同一 bug） | 拆解为多个新常量 |
| D2 已剔除语料型 | 318 | 来自第 1 步删除的升华/专精/不可达节点 | 超范围，遗留或废弃 |
| D3 自有引擎 stat | 216 | 非天赋树语料，属模组自有子系统 | **原样保留** |
| **合计** | **582** | | |

D1 涉及 22 个多行块。

---

## D1 多行合并型（旧常量族 -> 新表逐行模板）

**合并原文**：Accuracy is Doubled Never deal Critical Hits

- 旧常量（4）：`INC_ACCURACY_RATING_IS_DOUBLED`, `INC_ACCURACY_RATING_IS_DOUBLED_CRIT`, `RESOLUTE_TECHNIQUE_ACCURACY`, `RESOLUTE_TECHNIQUE_CRIT_CHANCE`
- 新表逐行模板（2）：
    - `ACCURACY_IS_DOUBLED` — Accuracy is Doubled
    - `NEVER_DEAL_CRITICAL_HITS` — Never deal Critical Hits

**合并原文**：All Damage Types is taken from Mana before Health {X}% more Mana Recovery Rate

- 旧常量（1）：`MIND_OVER_MATTER_MANA_TAKE`
- 新表逐行模板（2）：
    - `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` — All Damage Types is taken from Mana before Health
    - `X_MORE_MANA_RECOVERY_RATE` — {X}% more Mana Recovery Rate

**合并原文**：Bleeding you inflict is Aggravated Base Bleeding Duration is {X} second {X}% more Buff Magnitude of Bleeding you inflict

- 旧常量（1）：`AGGRAVATE_BLEEDING_ON_HIT`
- 新表逐行模板（3）：
    - `BLEEDING_YOU_INFLICT_IS_AGGRAVATED` — Bleeding you inflict is Aggravated
    - `BASE_BLEEDING_DURATION_IS_X_SECOND` — Base Bleeding Duration is {X} second
    - `X_MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` — {X}% more Buff Magnitude of Bleeding you inflict

**合并原文**：Cannot be Light Stunned Cannot Dodge Roll or Sprint

- 旧常量（1）：`UNWAVERING_STANCE_IMMUNE_STUN`
- 新表逐行模板（2）：
    - `CANNOT_BE_LIGHT_STUNNED` — Cannot be Light Stunned
    - `CANNOT_DODGE_ROLL_OR_SPRINT` — Cannot Dodge Roll or Sprint

**合并原文**：Cannot use Charms {X}% more Recovery from Flasks

- 旧常量（1）：`MORE_CANNOT_CHARMS_RECOVERY_FLASKS`
- 新表逐行模板（2）：
    - `CANNOT_USE_CHARMS` — Cannot use Charms
    - `X_MORE_RECOVERY_FROM_FLASKS` — {X}% more Recovery from Flasks

**合并原文**：Chance to Evasion is Unlucky Chance to Deflect is Lucky

- 旧常量（4）：`CHANCE_EVADE_IS_UNLUCKY_DEFLECT`, `CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY`, `GLANCING_BLOWS_DEFLECT_LUCKY`, `GLANCING_BLOWS_EVADE_UNLUCKY`
- 新表逐行模板（2）：
    - `CHANCE_TO_EVASION_IS_UNLUCKY` — Chance to Evasion is Unlucky
    - `CHANCE_TO_DEFLECT_IS_LUCKY` — Chance to Deflect is Lucky

**合并原文**：Create Lightning Elemental Infusion Remnants instead of Fire Create Cold Elemental Infusion Remnants instead of Lightning Create Fire Elemental Infusion Remnants instead of Cold

- 旧常量（2）：`INC_CREATE_LIGHTNING_INFUSION_REMNANTS`, `INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION`
- 新表逐行模板（3）：
    - `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE` — Create Lightning Elemental Infusion Remnants instead of Fire
    - `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING` — Create Cold Elemental Infusion Remnants instead of Lightning
    - `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD` — Create Fire Elemental Infusion Remnants instead of Cold

**合并原文**：Excess Health Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge

- 旧常量（2）：`INC_EXCESS_LIFE_RECOVERY_REGENERATION`, `INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD`
- 新表逐行模板（2）：
    - `EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD` — Excess Health Recovery from Regeneration is applied to Energy Shield
    - `ENERGY_SHIELD_DOES_NOT_RECHARGE` — Energy Shield does not Recharge

**合并原文**：Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage

- 旧常量（3）：`BLACKFLAME_COVENANT_FIRE_TO_CHAOS`, `FIRE_SPELLS_CONVERT_DAMAGE`, `FIRE_SPELLS_CONVERT_DAMAGE_SPELL`
- 新表逐行模板（3）：
    - `FIRE_SPELLS_CONVERSION_X_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE` — Fire Spells Conversion {X}% of Fire Damage to Chaos Damage
    - `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE` — Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude
    - `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE` — Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage

**合并原文**：Invocation Skills instead Trigger Spells every {X} seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume {X}% more Energy

- 旧常量（2）：`MORE_INVOCATION_TRIGGER_SPELLS_CANNOT`, `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL`
- 新表逐行模板（3）：
    - `INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_X_SECONDS` — Invocation Skills instead Trigger Spells every {X} seconds
    - `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS` — Invocation Skills cannot gain Energy while Triggering Spells
    - `INVOKED_SPELLS_CONSUME_X_MORE_ENERGY` — Invoked Spells consume {X}% more Energy

**合并原文**：Max Health is {X} Immune to Chaos Damage and Bleeding

- 旧常量（3）：`CHAOS_INOCULATION_IMMUNE_BLEED`, `CHAOS_INOCULATION_IMMUNE_CHAOS`, `CHAOS_INOCULATION_MAX_HEALTH`
- 新表逐行模板（2）：
    - `MAX_HEALTH_IS_X` — Max Health is {X}
    - `IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING` — Immune to Chaos Damage and Bleeding

**合并原文**：Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled

- 旧常量（3）：`CONVERT_ENERGY_SHIELD_MANA`, `CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION`, `ELDRITCH_BATTERY_ES_TO_MANA`
- 新表逐行模板（2）：
    - `STAT_CONVERSION_X_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA` — Stat Conversion {X}% of Max Energy Shield to Max Mana
    - `MANA_COSTS_ARE_DOUBLED` — Mana Costs are Doubled

**合并原文**：Take {X}% more Damage over Time if you've started taking Damage over Time in the past second Take {X}% more Damage over Time if you haven't started taking Damage over Time in the past second

- 旧常量（1）：`INC_DAMAGE_OVER_TIME`
- 新表逐行模板（2）：
    - `TAKE_X_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` — Take {X}% more Damage over Time if you've started taking Damage over Time in the past second
    - `TAKE_X_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` — Take {X}% more Damage over Time if you haven't started taking Damage over Time in the past second

**合并原文**：You can apply an additional Curse Double Activation Delay of Curses

- 旧常量（2）：`INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION`, `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE`
- 新表逐行模板（2）：
    - `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` — You can apply an additional Curse
    - `DOUBLE_ACTIVATION_DELAY_OF_CURSES` — Double Activation Delay of Curses

**合并原文**：You can wield Two-Handed Axes, Maces and Swords in one hand Triple Attributes requirements of Martial Weapons Inherent Health granted by Strength is halved

- 旧常量（1）：`ATTRIBUTE_REQUIREMENTS`
- 新表逐行模板（3）：
    - `YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND` — You can wield Two-Handed Axes, Maces and Swords in one hand
    - `TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS` — Triple Attributes requirements of Martial Weapons
    - `INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED` — Inherent Health granted by Strength is halved

**合并原文**：You have no Mana Skill Mana Costs Stat Conversion to Health Costs

- 旧常量（2）：`BLOOD_MAGIC_ENERGY_TO_LIFE`, `BLOOD_MAGIC_MANA_TO_LIFE`
- 新表逐行模板（2）：
    - `YOU_HAVE_NO_MANA` — You have no Mana
    - `SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` — Skill Mana Costs Stat Conversion to Health Costs

**合并原文**：Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each

- 旧常量（3）：`ADD_MAX_TOTEM`, `INC_TOTEM_LIMIT_IS_DOUBLED`, `INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE`
- 新表逐行模板（3）：
    - `YOUR_TOTEM_LIMIT_IS_DOUBLED` — Your Totem Limit is doubled
    - `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS` — No Charges requirement for placing Totems
    - `TOTEMS_RESERVE_X_SPIRIT_EACH` — Totems reserve {X} Spirit each

**合并原文**：gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges

- 旧常量（3）：`CHARGE_CONVERT_RESONANCE`, `INC_POWER_CHARGES_FRENZY_ENDURANCE`, `INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE`
- 新表逐行模板（3）：
    - `GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES` — gain Power Charges instead of Frenzy Charges
    - `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES` — gain Frenzy Charges instead of Endurance Charges
    - `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES` — gain Endurance Charges instead of Power Charges

**合并原文**：{X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect

- 旧常量（3）：`MORE_MAX_RAGE`, `MORE_RAGE_REGENERATE_SPENT_NO`, `MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY`
- 新表逐行模板（3）：
    - `X_MORE_MAX_RAGE` — {X}% more Max Rage
    - `REGENERATE_X_RAGE_PER_SECOND_PER_X_RAGE_SPENT_RECENTLY` — Regenerate {X} Rage per second per {X} Rage spent Recently
    - `NO_RAGE_EFFECT` — No Rage effect

**合并原文**：{X}% more amount of Health Leeched Leech Health {X}% more quickly Cannot Recover Health other than from Health Leech Health Leech effects are not add when Reservation Health is Filled

- 旧常量（1）：`MORE_AMOUNT_LEECH`
- 新表逐行模板（4）：
    - `X_MORE_AMOUNT_OF_HEALTH_LEECHED` — {X}% more amount of Health Leeched
    - `LEECH_HEALTH_X_MORE_QUICKLY` — Leech Health {X}% more quickly
    - `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH` — Cannot Recover Health other than from Health Leech
    - `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED` — Health Leech effects are not add when Reservation Health is Filled

**合并原文**：{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage

- 旧常量（4）：`AVATAR_OF_FIRE_CHAOS_TO_FIRE`, `AVATAR_OF_FIRE_COLD_TO_FIRE`, `AVATAR_OF_FIRE_LIGHT_TO_FIRE`, `AVATAR_OF_FIRE_PHYS_TO_FIRE`
- 新表逐行模板（2）：
    - `X_OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE` — {X}% of Damage Converted to Fire Damage
    - `DEAL_NO_NON_FIRE_DAMAGE` — Deal no Non-Fire Damage

**合并原文**：{X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armor

- 旧常量（1）：`SCARRED_FAITH_PHYS_RECOUP_ENERGY`
- 新表逐行模板（4）：
    - `X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER` — {X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power
    - `ENERGY_SHIELD_DOES_NOT_ESRECHARGE` — Energy Shield does not ESRecharge
    - `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION` — You cannot Recover Energy Shield from Regeneration
    - `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR` — You cannot Recover Energy Shield to above Armor

---

## D2 已剔除语料型（升华/专精/不可达节点）

| 旧常量 | 规范化 stat | 被引用 |
|---|---|---|
| `ADD_DARKNESS_LEVEL` | +{X} to Max Darkness per Level | 是 |
| `ADD_LIMIT_ELEMENTAL` | +{X} to Limit for Elemental Damage Skills | 是 |
| `ADD_MAX_CHAOS_RESISTANCE` | +{X}% to Max Chaos Resistance | 是 |
| `ADD_NUMBER_SUMMONED_TOTEMS` | +{X} to Max number of Summoned Totems | 是 |
| `ADD_QUALITY_ALL` | +{X}% to Quality of all Skills | 是 |
| `ADD_RING_SLOT` | +{X} Ring Slot | 是 |
| `ADD_SKILL_LEVEL` | +{X} to Level of all Skills with an Intelligence requirement | 是 |
| `ADD_SPIRIT` | +{X} to Spirit | 是 |
| `ADD_SPIRIT_EMPTY_CHARM_SLOTS` | +{X} to Spirit for each of your empty Charm slots | 是 |
| `ADD_SPIRIT_EVASION_RATING_BODY` | +{X} to Spirit for every {X} Evasion on Equipped Body Armor | 是 |
| `ADD_SPIRIT_EVASION_RATING_BODY_BODY_ARMOUR` | +{X} to Spirit for every {X} Evasion on Equipped Body Armor | 是 |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD` | +{X} to Spirit for every {X} Item Energy Shield on Equipped Body Armor | 是 |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD_BODY_ARMOUR` | +{X} to Spirit for every {X} Item Energy Shield on Equipped Body Armor | 是 |
| `ADD_SPIRIT_LIFE` | +{X} to Max Spirit per {X} Max Health | 是 |
| `ARMOUR_APPLIES_TO_CHAOS` | Body Armor grants +{X}% of Armor also applies to Chaos Damage | 是 |
| `BODY_ARMOUR_REGENERATE_LIFE` | Body Armor grants regenerate {X}% of Max Health per second | 是 |
| `BODY_ARMOUR_UNAFFECTED_DAMAGING` | Body Armor grants Unaffected by Damaging Ailments | 是 |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE` | gain {X}% Base Chance to Block from Equipped Shield instead of the Shield's value | 是 |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE_EQUIPPED` | gain {X}% Base Chance to Block from Equipped Shield instead of the Shield's value | 是 |
| `CHANCE_BASE_CRITICAL_SPELLS_IS` | Base Critical Hit Chance for Spells is {X}% | 是 |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED` | Benefits from consuming Frenzy Charges for your Skills have {X}% chance to be doubled | 是 |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED_CHARGE` | Benefits from consuming Frenzy Charges for your Skills have {X}% chance to be doubled | 是 |
| `CHANCE_BLOCK_IS` | Max Block chance is {X}% | 是 |
| `CHANCE_CASCADABLE_SPELLS_ECHO` | Cascadable Spells have {X}% chance to Cascadable | 是 |
| `CHANCE_CREATE_SHOCKED_GROUND_ON_SHOCK` | {X}% chance on Shocking Enemies to created Shocked Ground | 是 |
| `CHANCE_CRITICAL_IS_UNLUCKY` | Enemy Critical Hit Chance against you is Unlucky | 是 |
| `CHANCE_CURSED_KILLED_OR_ALLIES` | Cursed Enemies killed by you, or by Allies in your Presence, have a {X}% chance to explode, dealing a quarter of their Max Health as Chaos damage | 是 |
| `CHANCE_CURSED_KILLED_OR_ALLIES_CURSE` | Cursed Enemies killed by you, or by Allies in your Presence, have a {X}% chance to explode, dealing a quarter of their Max Health as Chaos damage | 是 |
| `CHANCE_EVADE_IS` | Max Chance to Evasion is {X}% | 是 |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE` | {X}% chance for Enemies you Kill to Explode, dealing {X}% of their Max Health as Physical Damage Chance is doubled against Undead and Demons | 是 |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE_PHYSICAL` | {X}% chance for Enemies you Kill to Explode, dealing {X}% of their Max Health as Physical Damage Chance is doubled against Undead and Demons | 是 |
| `CHANCE_REPEATABLE_SPELLS_REPEAT` | Repeatable Spells have {X}% chance to Repeat | 是 |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS` | {X}% Surpass Chance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a Max of {X} gain a Mountain's Teaching when you are Hit Damage, or when you use or Sustained an Attack that benefits from Mountain's Teachings | 是 |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS_PER_ENEMY` | {X}% Surpass Chance chance per enemy Power to gain Mountain's Teachings on Immobilising an enemy, up to a Max of {X} gain a Mountain's Teaching when you are Hit Damage, or when you use or Sustained an Attack that benefits from Mountain's Teachings | 是 |
| `COLD_TAKEN_AS_FIRE` | {X}% of Cold Damage taken as Fire Damage | 是 |
| `CREATE_CHILLED_GROUND_ON_FREEZE` | On Freezing Enemies create Chilled Ground | 是 |
| `DAMAGE_UNLUCKY` | Damage of Enemies Hit Damage you is Unlucky | 是 |
| `EVASION_RATING_ELEMENTAL_DAMAGE` | {X}% of Evasion also grants Elemental Damage reduction | 是 |
| `EXTRA_RANDOM_ELEMENT_RANDOM` | gain {X}% of Damage as Extra Damage of a random Elemental Damage | 是 |
| `FLASKS_RECOVER_LIFE` | Flasks do not recover Health | 是 |
| `GEM_QUALITY_SOCKETED_ADDITIONAL` | Gem Quality grants Socketed Skills an additional effect | 是 |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE` | Enemies have an Accuracy Penalty against you based on Distance | 是 |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE_BY_DISTANCE` | Enemies have an Accuracy Penalty against you based on Distance | 是 |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE` | Adaptation to the highest Elemental Damage Type of each Hit Damage you take | 是 |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE_ADAPTATION` | Adaptation to the highest Elemental Damage Type of each Hit Damage you take | 是 |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM` | gain additional Max Health equal to {X}% of the Item Energy Shield on Equipped Body Armor | 是 |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM_BODY_ARMOUR` | gain additional Max Health equal to {X}% of the Item Energy Shield on Equipped Body Armor | 是 |
| `INC_ALLIES_IN_PRESENCE_ADDED` | Allies in your Presence gain add Attack Damage equal to {X}% of your main hand Weapon's damage | 是 |
| `INC_ALLIES_IN_PRESENCE_ADDED_ATTACK` | Allies in your Presence gain add Attack Damage equal to {X}% of your main hand Weapon's damage | 是 |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING` | Can Allocate Passive Skills from the Sorceress's starting point | 是 |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING_SORCERESS` | Can Allocate Passive Skills from the Sorceress's starting point | 是 |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING` | Can Allocate Passive Skills from the Warrior's starting point | 是 |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING_WARRIOR` | Can Allocate Passive Skills from the Warrior's starting point | 是 |
| `INC_ARCANE_SURGE` | You have Arcane Surge | 是 |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL` | Attacks using your Weapons have add Physical Damage equal to {X}% of the Accuracy on the Weapon | 是 |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL_ATTACK` | Attacks using your Weapons have add Physical Damage equal to {X}% of the Accuracy on the Weapon | 是 |
| `INC_BANNERS_GLORY` | Banners gain {X} Glory per second | 是 |
| `INC_BASE_ENERGY_SHIELD_RECHARGE` | -{X} second to base Energy Shield Recharge delay | 是 |
| `INC_BASE_LIFE_COST_EQUAL` | Skills gain a Base Health Cost equal to Base Mana Cost | 是 |
| `INC_BECOME_IGNITED_CRITICAL_TAKING` | Become Ignited when you deal a Critical Hit, taking {X}% of your Max Health and Energy Shield as Fire Damage per second | 是 |
| `INC_BECOME_IGNITED_CRITICAL_TAKING_PER_SECOND` | Become Ignited when you deal a Critical Hit, taking {X}% of your Max Health and Energy Shield as Fire Damage per second | 是 |
| `INC_BEING_DAMAGED_TAKE_DAMAGE` | {X} seconds after being Damaged by an Enemy Hit Damage, take Damage equal to {X}% of that Hit Damage's Damage | 是 |
| `INC_BEING_DAMAGED_TAKE_DAMAGE_HITDAMAGE` | {X} seconds after being Damaged by an Enemy Hit Damage, take Damage equal to {X}% of that Hit Damage's Damage | 是 |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES` | gain the benefits of Shaman Only Mods modifiers on Runes and Idols | 是 |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES_SHAMANONLYMODS` | gain the benefits of Shaman Only Mods modifiers on Runes and Idols | 是 |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS` | Bleeding you inflict on Cursed targets is Aggravated | 是 |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS_CURSE` | Bleeding you inflict on Cursed targets is Aggravated | 是 |
| `INC_BONUSES_FOCUS` | {X}% increased bonuses gain from Equipped Focus | 是 |
| `INC_BREAK_ARMOUR_BELOW` | You can Break Enemy Armor to below {X} | 是 |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE` | Break Armor equal to {X}% of Hit Damage dealt | 是 |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE_ARMOURBREAK` | Break Armor equal to {X}% of Hit Damage dealt | 是 |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE` | Break enemy Concentration on Hit Damage equal to {X}% of Damage Dealt | 是 |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE_HITDAMAGE` | Break enemy Concentration on Hit Damage equal to {X}% of Damage Dealt | 是 |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS` | Skills can build and retain Combo regardless of Weapon Set | 是 |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS_WEAPONSETS` | Skills can build and retain Combo regardless of Weapon Set | 是 |
| `INC_CANNOT_SPIRIT` | Cannot gain Spirit from Equipment | 是 |
| `INC_CARRY_CHEST_WHICH_ADDS` | Carry a Chest which add {X} Inventory Slots | 是 |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER` | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground | 是 |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER_EMPOWERED` | Central Projectile of Owl Feather-Empowered Skills leaves a trail of Soaring Ground | 是 |
| `INC_CHAOS_RESISTANCE_IS_DOUBLED` | Chaos Resistance is doubled | 是 |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE` | When you Consume a Charges, Trigger Elemental Surge to gain {X} Lightning Surges | 是 |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_CHARGES` | When you Consume a Charges, Trigger Elemental Surge to gain {X} Cold Surges | 是 |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_SURGE` | When you Consume a Charges, Trigger Elemental Surge to gain {X} Fire Surges | 是 |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_V2` | When you Consume a Charges, Trigger Elemental Surge to gain {X} Lightning Surges | 是 |
| `INC_CHILLS_SLOW_TARGETS_UP` | Your Chills can Slow targets by up to a Max of {X}% | 是 |
| `INC_COLD_DAMAGE_TAKEN_FIRE` | {X}% of Cold Damage taken as Fire Damage | 是 |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER` | When collecting an Elemental Infusion, gain another different Elemental Infusion | 是 |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER_ELEMENTALINFUSION` | When collecting an Elemental Infusion, gain another different Elemental Infusion | 是 |
| `INC_COMBO_ADDITIONAL` | When you gain Combo, gain an additional Combo | 是 |
| `INC_COMBO_ALL_ATTACK` | gain Combo from all Attack Hit Damage | 是 |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE` | Companions gain add Attack damage equal to {X}% of your main hand Weapon's damage | 是 |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE_COMPANION` | Companions gain add Attack damage equal to {X}% of your main hand Weapon's damage | 是 |
| `INC_CONCENTRATION_EQUAL_LIFE` | Enemies have Max Concentration equal to {X}% of their Max Health | 是 |
| `INC_CONCENTRATION_EQUAL_LIFE_MAX_LIFE` | Enemies have Max Concentration equal to {X}% of their Max Health | 是 |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE` | Critical Hits ignore non-negative Enemy Monster Elemental Resistances | 是 |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE_RESISTANCES` | Critical Hits ignore non-negative Enemy Monster Elemental Resistances | 是 |
| `INC_CRITICAL_WEAKNESS_MARK_THEM` | Apply {X} Critical Weakness to Enemies when Consuming a Mark on them | 是 |
| `INC_CRITICAL_WEAKNESS_MARK_THEM_CRITICALWEAKNESS` | Apply {X} Critical Weakness to Enemies when Consuming a Mark on them | 是 |
| `INC_CRUSHING_BLOWS` | Your Hit Damage are Crushing Blows | 是 |
| `INC_CULLING_STRIKE` | Culling Strike | 是 |
| `INC_CURRENT_ENERGY_SHIELD_IS` | {X}% of your current Energy Shield is add to your Armor for determining your Physical Damage Reduction from Armor | 是 |
| `INC_CURRENT_ENERGY_SHIELD_IS_ENERGYSHIELD` | {X}% of your current Energy Shield is add to your Armor for determining your Physical Damage Reduction from Armor | 是 |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE` | -{X} seconds to current Energy Shield Recharge delay per Combo expended when using Skills | 是 |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE_ESRECHARGE` | -{X} seconds to current Energy Shield Recharge delay per Combo expended when using Skills | 是 |
| `INC_CURSES_INFLICT_INFINITE_DURATION` | Curses you inflict have infinite Duration | 是 |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_ELEMENTALDAMAGE` | gain {X}% of Damage as Extra Damage of a random Elemental Damage | 是 |
| `INC_DAMAGE_IS_TAKEN_MANA_HITDAMAGE` | Hit Damage damage is taken from Mana before Health if your current Mana is higher than your current Health | 是 |
| `INC_DARKNESS_RESERVATION_LASTS` | Darkness Reservation lasts for {X} seconds | 是 |
| `INC_DECIMATING_STRIKE` | Decimating Strike | 是 |
| `INC_DEFEND_ARMOUR` | Defend with {X}% of Armor | 是 |
| `INC_DEFLECTION_RATING_EQUAL_ARMOUR` | gain Deflection Rating equal to {X}% of Armor | 是 |
| `INC_DEMONFLAME_NO` | Demonflame has no Max | 是 |
| `INC_DOUBLE_ADAPTATION_EFFECT` | Double Adaptation Effect | 是 |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS` | Double the number of your Poisons that targets can be affected by at the same time | 是 |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS_POISONED` | Double the number of your Poisons that targets can be affected by at the same time | 是 |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL` | gain Elemental Archon when you cast a Spell while on High Infernal Flame | 是 |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL_WHILE_ON` | gain Elemental Archon when you cast a Spell while on High Infernal Flame | 是 |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH` | Elemental Archon does not expire while on High Infernal Flame | 是 |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH_WHILE_ON` | Elemental Archon does not expire while on High Infernal Flame | 是 |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING` | Elemental Damage also Contributes to Bleeding Buff Magnitude | 是 |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING_ELEMENTALDAMAGE` | Elemental Damage also Contributes to Bleeding Buff Magnitude | 是 |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN` | {X}% of Elemental Damage is taken from Mana before Health | 是 |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN_ELEMENTALDAMAGE` | {X}% of Elemental Damage is taken from Mana before Health | 是 |
| `INC_EQUIP_FOCUS_WIELDING_STAFF` | You can equip a Focus while wielding a Staff | 是 |
| `INC_EVASION_RATING_BODY_ARMOUR` | Evasion from Equipped Body Armor is halved | 是 |
| `INC_EVASION_RATING_EXTRA_AILMENT` | gain {X}% of Evasion as extra Ailment Threshold | 是 |
| `INC_EVASION_RATING_EXTRA_AILMENT_AILMENTTHRESHOLD` | gain {X}% of Evasion as extra Ailment Threshold | 是 |
| `INC_EVASION_RATING_HELMET_GLOVES` | Evasion from Equipped Helmet, Gloves and Boots is doubled | 是 |
| `INC_EVASION_RATING_HELMET_GLOVES_HELMET` | Evasion from Equipped Helmet, Gloves and Boots is doubled | 是 |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING` | gain {X} fewer Lightning Surge from Triggering Elemental Surge | 是 |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING_TRIGGER` | gain {X} fewer Lightning Surge from Triggering Elemental Surge | 是 |
| `INC_FLAMES_CHAYULA_MANIFEST_BLUE` | All Flames of Chayula that you manifest are Blue Flames Of Chayula | 是 |
| `INC_FLAMES_CHAYULA_MANIFEST_PURPLE` | All Flames of Chayula that you manifest are Purple Flames Of Chayula | 是 |
| `INC_FLAMES_CHAYULA_MANIFEST_RED` | All Flames of Chayula that you manifest are Red Flames Of Chayula | 是 |
| `INC_FREEZING_CREATE_CHILLED_GROUND` | On Freezing Enemies create Chilled Ground | 是 |
| `INC_FREEZING_CREATE_CHILLED_GROUND_FREEZE` | On Freezing Enemies create Chilled Ground | 是 |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_HITDAMAGE` | Fully Broken Armor you inflict increased all Damage Taken from Hit Damage instead | 是 |
| `INC_HITTING_LIFE_FLASK_IS` | On Hit Damage an Enemy while a Health Flask is at full Charges, {X}% of its Charges are consumed gain {X}% of damage as Physical damage for {X} seconds per Charge consumed this way | 是 |
| `INC_HITTING_LIFE_FLASK_IS_WHILE_A` | On Hit Damage an Enemy while a Health Flask is at full Charges, {X}% of its Charges are consumed gain {X}% of damage as Physical damage for {X} seconds per Charge consumed this way | 是 |
| `INC_IGNORE_WARCRY_COOLDOWNS` | Ignore Warcry Cooldowns | 是 |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION` | increased and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate | 是 |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION_RAGE` | increased and Reductions to Mana Regeneration Rate also apply to Rage Regeneration Rate | 是 |
| `INC_INEVITABLE_CRITICAL` | Inevitable Critical Hits | 是 |
| `INC_INFERNAL_FLAME_LOST_NONE` | {X}% of Infernal Flame gain per second if none was gain in the past {X} seconds | 是 |
| `INC_INHERENT_BONUSES_ATTRIBUTES_DOUBLED` | Inherent bonuses gain from Attributes are doubled | 是 |
| `INC_IN_PRESENCE_EXPOSURE` | Enemies in your Presence have Exposure | 是 |
| `INC_IN_PRESENCE_SLOWED` | Enemies in your Presence are Slowed by {X}% | 是 |
| `INC_JEWELLERY_BONUSES` | {X}% increased bonuses gain from Equipped Rings and Amulets | 是 |
| `INC_JEWEL_BONUS_EFFECT` | {X}% increased Effect of bonuses gain from Socketed Jewel | 是 |
| `INC_LIFE_CANNOT_CHANGE_ENERGY` | Your Health cannot change while you have Energy Shield | 是 |
| `INC_LIFE_CANNOT_CHANGE_ENERGY_WHILE_YOU` | Your Health cannot change while you have Energy Shield | 是 |
| `INC_LIFE_FLASK_CHARGE_SPENT` | gain {X} Health Flask Charge per {X}% Health spent | 是 |
| `INC_LIFE_FLASK_EFFECTS_REMOVED` | Health Flask Effects are not add when Reservation Health is Filled Health Flask Effects do not Queue | 是 |
| `INC_LIFE_FLASK_EFFECTS_REMOVED_RESERVATION` | Health Flask Effects are not add when Reservation Health is Filled Health Flask Effects do not Queue | 是 |
| `INC_LIFE_LOSS_IS_PREVENTED` | {X}% of Health Loss from Hit Damage is prevented, then that much Health is gain over {X} seconds instead | 是 |
| `INC_LIFE_LOSS_IS_PREVENTED_HITDAMAGE` | {X}% of Health Loss from Hit Damage is prevented, then that much Health is gain over {X} seconds instead | 是 |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE` | {X}% of Lightning Damage taken as Fire Damage | 是 |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE_CONVERT_FIRE` | {X}% of Lightning Damage taken as Fire Damage | 是 |
| `INC_LOSE_ALL_INFERNAL_FLAME` | gain all Infernal Flame on reaching Max Infernal Flame | 是 |
| `INC_LOSE_ALL_TAILWIND` | gain all Tailwind when Hit Damage | 是 |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING` | gain Elemental Archon on reaching Max Infernal Flame | 是 |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING_ELEMENTALARCHON` | gain Elemental Archon on reaching Max Infernal Flame | 是 |
| `INC_LOSE_MANA` | gain {X}% of Max Mana per Second | 是 |
| `INC_LOW_MANA_ALLIES_IN` | While you are not on Low Mana, you and Allies in your Presence have Unholy Might | 是 |
| `INC_LOW_MANA_ALLIES_IN_WHILE_YOU` | While you are not on Low Mana, you and Allies in your Presence have Unholy Might | 是 |
| `INC_MANA_IS_REPLACED_TWICE` | Max Mana is replaced by twice as much Max Infernal Flame gain Infernal Flame instead of spending Mana for Skill costs | 是 |
| `INC_MANA_IS_REPLACED_TWICE_BY_INFERNAL_FLAME` | Max Mana is replaced by twice as much Max Infernal Flame gain Infernal Flame instead of spending Mana for Skill costs | 是 |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT` | Can instead consume {X}% of Max Mana to trigger Charms with insufficient charges | 是 |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT_CHARM` | Can instead consume {X}% of Max Mana to trigger Charms with insufficient charges | 是 |
| `INC_MAX_DARKNESS` | {X}% increased Max Darkness | 是 |
| `INC_NO_INHERENT_LOSS_RAGE` | No Inherent loss of Rage | 是 |
| `INC_NO_INHERENT_MANA_REGENERATION` | No inherent Mana Regeneration | 是 |
| `INC_NO_SPIRIT_BASE_DARKNESS` | You have no Spirit Base Max Darkness is {X} Damage taken is Reserved from Darkness before being taken from Health or Energy Shield | 是 |
| `INC_NO_SPIRIT_BASE_DARKNESS_ENERGYSHIELD` | You have no Spirit Base Max Darkness is {X} Damage taken is Reserved from Darkness before being taken from Health or Energy Shield | 是 |
| `INC_ONLY_NORMAL_BODY_ARMOUR` | Can only use a Item Rarity Body Armor | 是 |
| `INC_OWL_FEATHERS_FASTER` | gain Owl Feathers {X}% faster | 是 |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR` | Physical Damage Reduction from Armor is based on your combined Armor and Evasion | 是 |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR_EVASION` | Physical Damage Reduction from Armor is based on your combined Armor and Evasion | 是 |
| `INC_PHYSICAL_DAMAGE_REDUCTION_IS` | Max Physical Damage Reduction is {X}% | 是 |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS` | {X}% of Physical Damage taken as Chaos Damage | 是 |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS_CONVERT_CHAOS` | {X}% of Physical Damage taken as Chaos Damage | 是 |
| `INC_PHYSICAL_DAMAGE_TAKEN_COLD` | {X}% of Physical Damage taken as Cold Damage | 是 |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING` | {X}% of Physical Damage taken as Lightning Damage | 是 |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING_CONVERT_LIGHTNING` | {X}% of Physical Damage taken as Lightning Damage | 是 |
| `INC_PINNED_CANNOT_PERFORM_ACTIONS` | Pinned enemies cannot perform actions | 是 |
| `INC_PRIMAL_OWL_FEATHER_UP` | gain a Primal Owl Feather every {X} seconds, up to a Max of {X} Expend an Owl Feather when you Dodge to trigger Primal Bounty | 是 |
| `INC_PROJECTILE_DAMAGE_BUILDS_PIN` | Projectile Damage builds Pinned | 是 |
| `INC_RAGE_COST` | Skills have +{X} to Rage cost | 是 |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN` | Enemies regain {X}% of Concentration every second if they haven't gain Concentration in the past {X} seconds | 是 |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN_IF_NOT_LOST` | Enemies regain {X}% of Concentration every second if they haven't gain Concentration in the past {X} seconds | 是 |
| `INC_REGENERATE_MANA_EQUAL_LIFE` | Regenerate Mana equal to {X}% of Max Health per second | 是 |
| `INC_REGENERATE_RAGE` | Regenerate {X}% of your Max Rage per second | 是 |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE` | Remnants you create reappear once, {X} seconds after being collected | 是 |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE_REAPPEAR_ONCE` | Remnants you create reappear once, {X} seconds after being collected | 是 |
| `INC_RESERVES_LIFE` | Reservation {X}% of Health | 是 |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE` | Reveal Weaknesses against Rare and Unique enemies | 是 |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE_REVEALWEAKNESS` | Reveal Weaknesses against Rare and Unique enemies | 是 |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT` | Sacrificing Energy Shield does not interrupt ESRecharge | 是 |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT_SACRIFICE` | Sacrificing Energy Shield does not interrupt ESRecharge | 是 |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED` | Every second Slam Skill you use yourself is Ancestrally Boosted | 是 |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED_ANCESTRALBOOST` | Every second Slam Skill you use yourself is Ancestrally Boosted | 是 |
| `INC_SMALL_PASSIVE_EFFECT` | {X}% increased effect of Small Passive Skills | 是 |
| `INC_SORCERY_WARD_BARRIER_TAKE` | Sorcery Ward's Barrier can also take Physical and Chaos Damage from Hit Damage | 是 |
| `INC_SORCERY_WARD_BARRIER_TAKE_HITDAMAGE` | Sorcery Ward's Barrier can also take Physical and Chaos Damage from Hit Damage | 是 |
| `INC_SPEED_IS_UNAFFECTED_SLOWS` | Your speed is unaffected by Slows | 是 |
| `INC_SPELL_DAMAGE_LEECHED_LIFE` | {X}% of Spell Damage Leeched as Health | 是 |
| `INC_STACK_JADE` | gain a stack of Jade every second | 是 |
| `INC_TAILWIND` | gain Tailwind on Skill use | 是 |
| `INC_TAKE_DAMAGE_BLOCKED` | You take {X}% of damage from Blocked Hit Damage | 是 |
| `INC_TAKE_LIFE_ENERGY_SHIELD` | Take Max Health and Energy Shield as Fire Damage when Infernal Flame reaches Max | 是 |
| `INC_TAKE_LIFE_ENERGY_SHIELD_FIRE` | Take Max Health and Energy Shield as Fire Damage when Infernal Flame reaches Max | 是 |
| `INC_TAKE_NO_DAMAGE_VOLATILITY` | Take no Damage from Volatility | 是 |
| `INC_TARGETS_AFFECTED_TWO_CHILLS` | Targets can be affected by two of your Chills at the same time | 是 |
| `INC_TARGETS_AFFECTED_TWO_CHILLS_CHILL` | Targets can be affected by two of your Chills at the same time | 是 |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS` | Targets can be affected by two of your Shocks at the same time | 是 |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS_SHOCK` | Targets can be affected by two of your Shocks at the same time | 是 |
| `INC_TARGETS_CURSED_LIFE_RESERVED` | Targets Cursed by you have at least {X}% of Health Reservation | 是 |
| `INC_TARGETS_CURSED_LIFE_RESERVED_CURSE` | Targets Cursed by you have at least {X}% of Health Reservation | 是 |
| `INC_THERE_IS_NO_LIMIT` | There is no Limit on the number of Banners you can place | 是 |
| `INC_THERE_IS_NO_LIMIT_BANNER` | There is no Limit on the number of Banners you can place | 是 |
| `INC_THIRD_SLAM_DOESN_CREATE` | Every Third Slam skill that doesn't create Fissures which you use yourself causes {X} additional Aftershocks ahead and to each side of the initial area | 是 |
| `INC_THIRD_SLAM_DOESN_CREATE_AFTERSHOCK` | Every Third Slam skill that doesn't create Fissures which you use yourself causes {X} additional Aftershocks ahead and to each side of the initial area | 是 |
| `INC_TOTEMS_DIE_LIFE_IS` | Totems die {X} seconds after their Health is increased to {X} | 是 |
| `INC_TOTEMS_ONLY_FIRE_ATTACK` | Totems only use Skills when you fire an Attack Projectile | 是 |
| `INC_TOTEMS_ONLY_FIRE_ATTACK_PROJECTILE` | Totems only use Skills when you fire an Attack Projectile | 是 |
| `INC_TRIGGER_ANCESTRAL_SPIRITS_SUMMON` | Trigger Ancestral Spirits when you Summon a Totem | 是 |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE` | Trigger Elemental Expression on Melee Critical Hit | 是 |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE_CRITICAL` | Trigger Elemental Expression on Melee Critical Hit | 是 |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL` | Trigger Elemental Storm on Critical Hit with Spells | 是 |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL_SPELL` | Trigger Elemental Storm on Critical Hit with Spells | 是 |
| `INC_VIVID_STAGS_LEAP_TOWARDS` | Vivid Stags leap towards enemies | 是 |
| `INC_VIVID_WISP_METRES_MOVE` | gain a Vivid Wisp for every {X} metres you move, up to a Max of {X} Expend all Vivid Wisps to trigger Vivid Stampede when you Attack | 是 |
| `INC_VIVID_WISP_METRES_MOVE_ATTACK` | gain a Vivid Wisp for every {X} metres you move, up to a Max of {X} Expend all Vivid Wisps to trigger Vivid Stampede when you Attack | 是 |
| `INC_VIVID_WISP_STAMPEDE_ENDS` | gain a Vivid Wisp when Vivid Stampede ends | 是 |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT` | gain {X} Volatility on inflicting an Elemental Ailment | 是 |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT_ELEMENTALAILMENTS` | gain {X} Volatility on inflicting an Elemental Ailment | 是 |
| `INC_WALK_PATHS_TAKEN` | Walk the Paths Not Taken | 是 |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING` | Warcries Explode Corpses dealing {X}% of their Health as Physical Damage | 是 |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING_CORPSE` | Warcries Explode Corpses dealing {X}% of their Health as Physical Damage | 是 |
| `LEECH_RECOVERS_BASED_CHAOS` | Leech recovers based on Chaos Damage as well as Physical Damage | 是 |
| `LIFE_FLASKS_RECOVER_MANA` | Health Flasks also recover Mana Mana Flasks also recover Health | 是 |
| `LIFE_FLASKS_RECOVER_MANA_FLASK` | Health Flasks also recover Mana Mana Flasks also recover Health | 是 |
| `LIFE_LEECH_RECOVERS_BASED` | Health Leech recovers based on your Elemental Damage damage as well as Physical damage | 是 |
| `LIGHTNING_TAKEN_AS_FIRE` | {X}% of Lightning Damage taken as Fire Damage | 是 |
| `MODIFIERS_FIRE_RESISTANCE_COLD` | Modifiers to Max Fire Resistance also grant Max Cold and Lightning Resistance | 是 |
| `MODIFIERS_FIRE_RESISTANCE_COLD_COLD_LIGHTNING` | Modifiers to Fire Resistance also grant Cold and Lightning Resistance at {X}% of their value | 是 |
| `MODIFIERS_FIRE_RESISTANCE_COLD_MAXIMUMRESISTANCES` | Modifiers to Max Fire Resistance also grant Max Cold and Lightning Resistance | 是 |
| `MORE_DODGING_EXPEND_UP_OWL` | Dodging can expend up to {X} Owl Feathers, granting Primal Bounty {X}% more Empowerment effect per additional Feather expended | 是 |
| `MORE_DODGING_EXPEND_UP_OWL_EMPOWERED` | Dodging can expend up to {X} Owl Feathers, granting Primal Bounty {X}% more Empowerment effect per additional Feather expended | 是 |
| `MORE_GLOVES_EQUIP_BASE_TYPE` | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers | 是 |
| `MORE_GLOVES_EQUIP_BASE_TYPE_WHILE_EQUIPPED` | Gloves you equip have their Base Type transformed to Fists of Stone while equipped, and their Explicit Modifiers are transformed into more powerful related Modifiers | 是 |
| `MORE_PERSISTENT_BUFFS_RESERVATION` | Persistent Buffs have {X}% more Reservation | 是 |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP` | Stags have {X}% more Shock Buff Magnitude per leap | 是 |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP_PER_LEAP` | Stags have {X}% more Shock Buff Magnitude per leap | 是 |
| `NON_KEYSTONE_PASSIVE_IN` | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree | 是 |
| `NON_KEYSTONE_PASSIVE_IN_MEDIUM_RADIUS` | Non-Keystone Passive Skills in Medium Radius of allocated Keystone Passive Skills can be allocated without being connected to your tree | 是 |
| `PASSIVE_POINT` | Grants {X} Passive Skill Point | 是 |
| `PASSIVE_POINTS` | Grants {X} Passive Skill Points | 是 |
| `PHYSICAL_TAKEN_AS_CHAOS` | {X}% of Physical Damage taken as Chaos Damage | 是 |
| `PHYSICAL_TAKEN_AS_COLD` | {X}% of Physical Damage taken as Cold Damage | 是 |
| `PHYSICAL_TAKEN_AS_FIRE_BODY_ARMOUR` | Body Armor grants {X}% of Physical Damage from Hit Damage taken as Fire Damage | 是 |
| `PHYSICAL_TAKEN_AS_LIGHTNING` | {X}% of Physical Damage taken as Lightning Damage | 是 |
| `SANDS_TIME` | Grants Sands of Time | 是 |
| `TOTEM_DAMAGE` | {X}% of Damage from Hit Damage is taken from your nearest Totem's Health before you | 是 |
| `UNDERLINE_ACIDIC_CONCOCTION` | Grants Skill: <underline>{Acidic Concoction} | 是 |
| `UNDERLINE_ALIGN_FATE` | Grants Skill: <underline>{Align Fate} | 是 |
| `UNDERLINE_ANCESTRAL_SPIRITS` | Grants Skill: <underline>{Ancestral Spirits} | 是 |
| `UNDERLINE_APOCALYPSE` | Grants Skill: <underline>{Apocalypse} | 是 |
| `UNDERLINE_ARCHON_CHAYULA` | Grants Skill: <underline>{Archon of Chayula} | 是 |
| `UNDERLINE_BLEEDING_CONCOCTION` | Grants Skill: <underline>{Bleeding Concoction} | 是 |
| `UNDERLINE_BLOOD_BOIL` | Grants Skill: <underline>{Blood Boil} | 是 |
| `UNDERLINE_BREACH` | Grants Skill: <underline>{Into the Breach} | 是 |
| `UNDERLINE_CALLED_SHOTS` | Grants Skill: <underline>{Called Shots} | 是 |
| `UNDERLINE_DEMON_FORM` | Grants Skill: <underline>{Demon Form} | 是 |
| `UNDERLINE_ELEMENTAL_EXPRESSION` | Grants Skill: <underline>{Elemental Expression} | 是 |
| `UNDERLINE_ELEMENTAL_STORM` | Grants Skill: <underline>{Elemental Storm} | 是 |
| `UNDERLINE_ELEMENTAL_SURGE` | Grants Skill: <underline>{Elemental Surge} | 是 |
| `UNDERLINE_ENCASE_IN_JADE` | Grants Skill: <underline>{Encase in Jade} | 是 |
| `UNDERLINE_EXPLOSIVE_CONCOCTION` | Grants Skill: <underline>{Explosive Concoction} | 是 |
| `UNDERLINE_FIRE_SPELL` | Grants Skill: <underline>{Fire Spell on Hit} | 是 |
| `UNDERLINE_FULMINATING_CONCOCTION` | Grants Skill: <underline>{Fulminating Concoction} | 是 |
| `UNDERLINE_HOLLOW_FOCUS` | Grants Skill: <underline>{Hollow Focus} | 是 |
| `UNDERLINE_HOLLOW_FORM` | Grants Skill: <underline>{Hollow Form} | 是 |
| `UNDERLINE_HOLLOW_RESONANCE` | Grants Skill: <underline>{Hollow Resonance} | 是 |
| `UNDERLINE_INEVITABLE_AGONY` | Grants Skill: <underline>{Inevitable Agony} | 是 |
| `UNDERLINE_KELARI_DECEPTION` | Grants Skill: <underline>{Kelari's Deception} | 是 |
| `UNDERLINE_KELARI_JUDGMENT` | Grants Skill: <underline>{Kelari's Judgment} | 是 |
| `UNDERLINE_KELARI_MALEDICTION` | Grants Skill: <underline>{Kelari's Malediction} | 是 |
| `UNDERLINE_KELARI_TAINTED_SANDS` | Grants Skill: <underline>{Kelari, the Tainted Sands} | 是 |
| `UNDERLINE_LIFE_REMNANTS` | Grants Skill: <underline>{Health Remnants} | 是 |
| `UNDERLINE_MANIFEST_WEAPON` | Grants Skill: <underline>{Manifest Weapon} | 是 |
| `UNDERLINE_MEDITATE` | Grants Skill: <underline>{Meditate} | 是 |
| `UNDERLINE_MIRAGE_DEADEYE` | Grants Skill: <underline>{Mirage Deadeye} | 是 |
| `UNDERLINE_MOMENT_VULNERABILITY` | Grants Skill: <underline>{Moment of Vulnerability} | 是 |
| `UNDERLINE_NAVIRA_FRACTURING` | Grants Skill: <underline>{Navira's Fracturing} | 是 |
| `UNDERLINE_NAVIRA_LAST_MIRAGE` | Grants Skill: <underline>{Navira, the Last Mirage} | 是 |
| `UNDERLINE_NAVIRA_OASIS` | Grants Skill: <underline>{Navira's Oasis} | 是 |
| `UNDERLINE_NAVIRA_WELL` | Grants Skill: <underline>{Navira's Well} | 是 |
| `UNDERLINE_PRIMAL_BOUNTY` | Grants Skill: <underline>{Primal Bounty} | 是 |
| `UNDERLINE_RITUAL_SACRIFICE` | Grants Skill: <underline>{Ritual Sacrifice} | 是 |
| `UNDERLINE_RUZHAN_BLAZING_SWORD` | Grants Skill: <underline>{Ruzhan, the Blazing Sword} | 是 |
| `UNDERLINE_RUZHAN_FURY` | Grants Skill: <underline>{Ruzhan's Fury} | 是 |
| `UNDERLINE_RUZHAN_RECKONING` | Grants Skill: <underline>{Ruzhan's Reckoning} | 是 |
| `UNDERLINE_RUZHAN_TRAP` | Grants Skill: <underline>{Ruzhan's Trap} | 是 |
| `UNDERLINE_SHATTERING_CONCOCTION` | Grants Skill: <underline>{Shattering Concoction} | 是 |
| `UNDERLINE_SORCERY_WARD` | Grants Skill: <underline>{Sorcery Ward} | 是 |
| `UNDERLINE_SUMMON_INFERNAL_HOUND` | Grants Skill: <underline>{Summon Infernal Hound} | 是 |
| `UNDERLINE_SUPPORTING_FIRE` | Grants Skill: <underline>{Supporting Fire} | 是 |
| `UNDERLINE_TEMPER_WEAPON` | Grants Skill: <underline>{Temper Weapon} | 是 |
| `UNDERLINE_TEMPORAL_RIFT` | Grants Skill: <underline>{Temporal Rift} | 是 |
| `UNDERLINE_TIME_FREEZE` | Grants Skill: <underline>{Time Freeze} | 是 |
| `UNDERLINE_TIME_SNAP` | Grants Skill: <underline>{Time Snap} | 是 |
| `UNDERLINE_UNBOUND_AVATAR` | Grants Skill: <underline>{Unbound Avatar} | 是 |
| `UNDERLINE_VIRTUOUS_BARRIER` | Grants Skill: <underline>{Virtuous Barrier} | 是 |
| `UNDERLINE_VIVID_STAMPEDE` | Grants Skill: <underline>{Vivid Stampede} | 是 |
| `UNDERLINE_VOID_ILLUSION` | Grants Skill: <underline>{Void Illusion} | 是 |
| `UNDERLINE_WILD_PROTECTOR` | Grants Skill: <underline>{Wild Protector} | 是 |
| `UNRAVELLING` | Grants Unravelling Buff | 是 |

---

## D3 自有引擎 stat（原样保留）

| 旧常量 | 注释 | 归因 |
|---|---|---|
| `ACCURACY` | Accuracy | 常量名回译 |
| `ACTION_SPEED_ATTACK_SPEED` | Action Speed Attack Speed | 常量名回译 |
| `ACTION_SPEED_BLOCK_BREAK_SPEED` | Action Speed Block Break Speed | 常量名回译 |
| `ACTION_SPEED_CAST_SPEED` | Action Speed Cast Speed | 常量名回译 |
| `ACTION_SPEED_FLYING_SPEED` | Action Speed Flying Speed | 常量名回译 |
| `ACTION_SPEED_JUMP_STRENGTH` | Action Speed Jump Strength | 常量名回译 |
| `ACTION_SPEED_MOVEMENT_SPEED` | Action Speed Movement Speed | 常量名回译 |
| `ACTION_SPEED_SNEAKING_SPEED` | Action Speed Sneaking Speed | 常量名回译 |
| `ACTION_SPEED_SUBMERGED_MINING_SPEED` | Action Speed Submerged Mining Speed | 常量名回译 |
| `ADD_ALL_ELEMENTAL_RESISTANCES_RED` | Add All Elemental Resistances Red | 常量名回译 |
| `ADD_CAST_CRIT_DAMAGE` | Add Cast Crit Damage | 常量名回译 |
| `ADD_CHAOS_DAMAGE` | Add Chaos Damage | 常量名回译 |
| `ADD_COLD_DAMAGE` | Add Cold Damage | 常量名回译 |
| `ADD_COLD_INFUSIONS` | +1 to maximum ElementalInfusion | 完整语料中亦无对应 |
| `ADD_COOLDOWN_USE` | Add Cooldown Use | 常量名回译 |
| `ADD_CRIT_CHANCE` | 5% chance to Gain ArcaneSurgeDuration when you deal a Critical Hit | 完整语料中亦无对应 |
| `ADD_ELEMENTAL_DAMAGE` | Add Elemental Damage | 常量名回译 |
| `ADD_ELEMENTAL_RESISTANCE_MINION` | Add Elemental Resistance Minion | 常量名回译 |
| `ADD_ENERGY_RECHARGE` | Add Energy Recharge | 常量名回译 |
| `ADD_FIRE_DAMAGE` | Add Fire Damage | 常量名回译 |
| `ADD_FIRE_INFUSIONS` | +1 to maximum ElementalInfusion | 完整语料中亦无对应 |
| `ADD_FOUND_ITEM_NUMBER` | Add Found Item Number | 常量名回译 |
| `ADD_FOUND_ITEM_RARITY` | Add Found Item Rarity | 常量名回译 |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | Add Launch Additional Projectile Bolt | 常量名回译 |
| `ADD_MAX_BLOCK_CHANCE` | Add Max Block Chance | 常量名回译 |
| `ADD_MAX_HEALTH_REGENERATION` | Add Max Health Regeneration | 常量名回译 |
| `ADD_MAX_SPIRIT` | Add Max Spirit | 常量名回译 |
| `ADD_METRES_MELEE_STRIKE_RANGE_NO_CONDITION` | Add Metres Melee Strike Range No Condition | 常量名回译 |
| `ADD_PHYSICAL_DAMAGE` | Add Physical Damage | 常量名回译 |
| `ADD_PHYSICAL_DAMAGE_REDUCTION` | Add Physical Damage Reduction | 常量名回译 |
| `ADD_PROJECTILE_PIERCE` | Add Projectile Pierce | 常量名回译 |
| `ADD_PROJECTILE_RANDOM_FIRE` | Add Projectile Random Fire | 常量名回译 |
| `ADD_RECENTLY_CONSUMED_DAMAGE` | Add Recently Consumed Damage | 常量名回译 |
| `ADD_SKILL_SLOT` | Add Skill Slot | 常量名回译 |
| `ADD_STUN_THRESHOLD` | Add Stun Threshold | 常量名回译 |
| `ADRENALINE_ATTACK_SPEED` | Adrenaline Attack Speed | 常量名回译 |
| `ADRENALINE_CAST_SPEED` | Adrenaline Cast Speed | 常量名回译 |
| `ADRENALINE_DAMAGE` | Adrenaline Damage | 常量名回译 |
| `ADRENALINE_MOVEMENT_SPEED` | Adrenaline Movement Speed | 常量名回译 |
| `ADRENALINE_TAKEN_PHYSICAL` | Adrenaline Taken Physical | 常量名回译 |
| `AILMENT_MAGNITUDE_FREEZE` | Ailment Magnitude Freeze | 常量名回译 |
| `ALL_RAGE_SHAPESHIFTING_HUMAN` | Stat All Rage Shapeshifting Human | 完整语料中亦无对应 |
| `ARCANE_SURGE_CAST_SPEED` | Arcane Surge Cast Speed | 常量名回译 |
| `ARCANE_SURGE_REGEN` | Arcane Surge Regen | 常量名回译 |
| `ARCHON_FREEZE_BUILDUP` | Archon Freeze Buildup | 常量名回译 |
| `ARCHON_IGNITE_BUILDUP` | Archon Ignite Buildup | 常量名回译 |
| `ARCHON_NULLIFY_CHAOS` | Archon Nullify Chaos | 常量名回译 |
| `ARCHON_NULLIFY_PHYSICAL` | Archon Nullify Physical | 常量名回译 |
| `ARCHON_SHOCK_MAGNITUDE` | Archon Shock Magnitude | 常量名回译 |
| `ARMOR_BREAK_FULLY` | Armor Break Fully | 常量名回译 |
| `ARMOR_REDUCTION` | Armor Reduction | 常量名回译 |
| `BANNER_ATTACK_SPEED` | Banner Attack Speed | 常量名回译 |
| `BANNER_CAST_SPEED` | Banner Cast Speed | 常量名回译 |
| `BANNER_MOVEMENT_SPEED` | Banner Movement Speed | 常量名回译 |
| `CHANCE_AGGRAVATE_ON_HIT_VS_CURSED` | Chance Aggravate On Hit Vs Cursed | 常量名回译 |
| `CHANCE_ALL_DAMAGE_CONTRIBUTES_SHOCK` | DamageTypes from HitDamage Contributes to Shock Chance | 完整语料中亦无对应 |
| `CHANCE_ATTACKS_EXCEED_ADDITIONAL_CRITICAL` | Chance to HitDamage with Attacks can ExceedChance Gain additional Critical Hit Chance equal to 25% of excess chance to HitDamage with Attacks | 完整语料中亦无对应 |
| `CHANCE_AVOID_AILMENT` | Chance Avoid Ailment | 常量名回译 |
| `CHANCE_CHILL` | Chance Chill | 常量名回译 |
| `CHANCE_DODGE` | Chance Dodge | 常量名回译 |
| `CHANCE_FREEZE` | Chance Freeze | 常量名回译 |
| `CHANCE_IGNITE` | Chance Ignite | 常量名回译 |
| `CHANCE_SLAM_CAUSE_ADDITIONAL_AFTERSHOCK_SPELL` | Chance Slam Cause Additional Aftershock Spell | 常量名回译 |
| `CHANCE_WHICH_CREATE_FISSURES_ADDITIONAL_FISSURE` | Skills which create Fissures have a #% chance to create an additional Fissure | 完整语料中亦无对应 |
| `CHARM_GOLDEN_RARITY` | Charm Golden Rarity | 常量名回译 |
| `CHARM_IMMUNE_ANTIDOTE` | Charm Immune Antidote | 常量名回译 |
| `CHARM_IMMUNE_DOUSING` | Charm Immune Dousing | 常量名回译 |
| `CHARM_IMMUNE_GROUNDING` | Charm Immune Grounding | 常量名回译 |
| `CHARM_IMMUNE_SILVER` | Charm Immune Silver | 常量名回译 |
| `CHARM_IMMUNE_STAUNCHING` | Charm Immune Staunching | 常量名回译 |
| `CHARM_IMMUNE_STONE` | Charm Immune Stone | 常量名回译 |
| `CHARM_IMMUNE_THAWING` | Charm Immune Thawing | 常量名回译 |
| `CHARM_RESIST_CHAOS` | Charm Resist Chaos | 常量名回译 |
| `CHARM_RESIST_COLD` | Charm Resist Cold | 常量名回译 |
| `CHARM_RESIST_FIRE` | Charm Resist Fire | 常量名回译 |
| `CHARM_RESIST_LIGHTNING` | Charm Resist Lightning | 常量名回译 |
| `CONVERT_EXTRA_PHYS_TO_FIRE` | Convert Extra Phys To Fire | 常量名回译 |
| `CONVERT_SKILL_MANA_TO_LIFE` | Convert Skill Mana To Life | 常量名回译 |
| `CREATE_IGNITED_GROUND_ON_IGNITE` | Create Ignited Ground On Ignite | 常量名回译 |
| `DAMAGE_DEALT` | Damage Dealt | 常量名回译 |
| `DAMAGE_DEALT_BEAR_FIRE` | Damage Dealt Bear Fire | 常量名回译 |
| `DAMAGE_DEALT_BEAR_PHYSICAL` | Damage Dealt Bear Physical | 常量名回译 |
| `DAMAGE_DEALT_ELEMENTAL_SPELL` | Damage Dealt Elemental Spell | 常量名回译 |
| `DAMAGE_DEALT_WOLF_COLD` | Damage Dealt Wolf Cold | 常量名回译 |
| `DAMAGE_DEALT_WOLF_PHYSICAL` | Damage Dealt Wolf Physical | 常量名回译 |
| `DAMAGE_DEALT_WYVERN_CHAOS` | Damage Dealt Wyvern Chaos | 常量名回译 |
| `DAMAGE_DEALT_WYVERN_LIGHTNING` | Damage Dealt Wyvern Lightning | 常量名回译 |
| `DAMAGE_TAKEN` | Damage Taken | 常量名回译 |
| `DAMAGE_TAKEN_CHAOS` | Damage Taken Chaos | 常量名回译 |
| `DAMAGE_TAKEN_COLD_ON_IGNITED` | Damage Taken Cold On Ignited | 常量名回译 |
| `DAMAGE_TAKEN_FIRE_ON_CHILLED` | Damage Taken Fire On Chilled | 常量名回译 |
| `DAMAGE_TAKEN_PHYSICAL` | Damage Taken Physical | 常量名回译 |
| `EVASION` | Evasion | 常量名回译 |
| `EXPOSURE_COLD` | Exposure Cold | 常量名回译 |
| `EXPOSURE_FIRE` | Exposure Fire | 常量名回译 |
| `EXPOSURE_LIGHTNING` | Exposure Lightning | 常量名回译 |
| `EXTRA_FIRE_FROM_CHAOS` | Extra Fire From Chaos | 常量名回译 |
| `EXTRA_FIRE_FROM_COLD` | Gain 25% of Cold Damage as Extra Fire Damage against Frozen Enemies | 完整语料中亦无对应 |
| `EXTRA_LIGHTNING_FROM_CHAOS` | Extra Lightning From Chaos | 常量名回译 |
| `EXTRA_PHYSICAL_FROM_CHAOS` | Extra Physical From Chaos | 常量名回译 |
| `EXTRA_PHYSICAL_FROM_COLD` | Extra Physical From Cold | 常量名回译 |
| `EXTRA_PHYSICAL_FROM_FIRE` | Extra Physical From Fire | 常量名回译 |
| `EXTRA_PHYSICAL_FROM_LIGHTNING` | Extra Physical From Lightning | 常量名回译 |
| `EXTRA_PHYSICAL_FROM_PHYSICAL` | Extra Physical From Physical | 常量名回译 |
| `EXTRA_RANDOM_ELEMENT_CYCLE` | Extra Random Element Cycle | 常量名回译 |
| `FORM_BEAR_ARMOR` | Form Bear Armor | 常量名回译 |
| `FORM_BEAR_ATTACK_SPEED` | Form Bear Attack Speed | 常量名回译 |
| `FORM_WOLF_ATTACK_SPEED` | Form Wolf Attack Speed | 常量名回译 |
| `FORM_WOLF_MOVEMENT_SPEED` | Form Wolf Movement Speed | 常量名回译 |
| `FORM_WYVERN_CRIT_CHANCE` | Form Wyvern Crit Chance | 常量名回译 |
| `FORM_WYVERN_CRIT_DAMAGE` | Form Wyvern Crit Damage | 常量名回译 |
| `GRANT_VOLATILITY_ON_AILMENT` | Grant Volatility On Ailment | 常量名回译 |
| `GRANT_VOLATILITY_ON_EXPLODE` | Grant Volatility On Explode | 常量名回译 |
| `INC_ADAPTATIONS_DURATION` | Inc Adaptations Duration | 常量名回译 |
| `INC_AILMENT_DAMAGE_RATE` | Inc Ailment Damage Rate | 常量名回译 |
| `INC_AILMENT_DAMAGE_RATE_BLEED` | Inc Ailment Damage Rate Bleed | 常量名回译 |
| `INC_AILMENT_DAMAGE_RATE_POISON` | Inc Ailment Damage Rate Poison | 常量名回译 |
| `INC_ALLIES_IN_PRESENCE_REGENERATE_IN_PRESENCE` | Inc Allies In Presence Regenerate In Presence | 常量名回译 |
| `INC_ALL_BONUSES_AMULET_MINIONS` | Inc All Bonuses Amulet Minions | 常量名回译 |
| `INC_ALL_DAMAGE_ALLIES_IN` | Inc All Damage Allies In | 常量名回译 |
| `INC_ALL_DAMAGE_CONTRIBUTES_CHILL` | Inc All Damage Contributes Chill | 常量名回译 |
| `INC_ALL_FLAMES_CHAYULA_MANIFEST` | Inc All Flames Chayula Manifest | 常量名回译 |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN` | Gain ArcaneSurgeDuration when you Shapeshift to Human form after being Shapeshifted for at least 8 seconds | 完整语料中亦无对应 |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN_ARCANESURGEDURATION` | Gain ArcaneSurgeDuration when you Shapeshift to Human form after being Shapeshifted for at least 8 seconds | 完整语料中亦无对应 |
| `INC_BANNER_DAMAGE` | Inc Banner Damage | 常量名回译 |
| `INC_BEAR_SPIRIT_GAINS_EMBRACE` | Bear Spirit gains SpiritWalkerBearAura | 完整语料中亦无对应 |
| `INC_BOW_ATTACK_SPEED` | Inc Bow Attack Speed | 常量名回译 |
| `INC_BOW_DAMAGE` | Inc Bow Damage | 常量名回译 |
| `INC_BOW_PHYSICAL_DAMAGE` | Inc Bow Physical Damage | 常量名回译 |
| `INC_BUFF_EFFECT_TAKEN` | Inc Buff Effect Taken | 常量名回译 |
| `INC_CHILL_DURATION_TAKEN` | Inc Chill Duration Taken | 常量名回译 |
| `INC_COLD_DAMAGE_ON_CHILLED` | Inc Cold Damage On Chilled | 常量名回译 |
| `INC_COLD_DAMAGE_SPELL` | Inc Cold Damage Spell | 常量名回译 |
| `INC_CRIT_CHANCE_SPELL` | Inc Crit Chance Spell | 常量名回译 |
| `INC_CRIT_DAMAGE_SPELL` | Inc Crit Damage Spell | 常量名回译 |
| `INC_DAMAGE_ALLIES_IN_PRESENCE` | All Damage from you and Allies in your Presence contributes to Flammability and Ignite BuffMagnitude | 完整语料中亦无对应 |
| `INC_DAMAGE_BLOCKING` | Inc Damage Blocking | 常量名回译 |
| `INC_DAMAGE_CONTRIBUTES_CHILL_MAGNITUDE` | DamageTypes from HitDamage Contributes to Chill Magnitude | 完整语料中亦无对应 |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_RANDOM_ELEMENT` | Inc Damage Extra Random Element Random Element | 常量名回译 |
| `INC_DAMAGE_IS_TAKEN_MANA_TAKEN_MANA` | Inc Damage Is Taken Mana Taken Mana | 常量名回译 |
| `INC_DAMAGE_RECENTLY_KILLED` | Inc Damage Recently Killed | 常量名回译 |
| `INC_ES_RECHARGE_START` | Inc Es Recharge Start | 常量名回译 |
| `INC_FASTER_START_ENERGY_SHIELD_ARCHON` | Inc Faster Start Energy Shield Archon | 常量名回译 |
| `INC_FASTER_START_ENERGY_SHIELD_FLAT` | Inc Faster Start Energy Shield Flat | 常量名回译 |
| `INC_FASTER_START_ENERGY_SHIELD_NO_CONDITION` | Inc Faster Start Energy Shield No Condition | 常量名回译 |
| `INC_FASTER_START_ENERGY_SHIELD_UNCONDITIONAL` | Inc Faster Start Energy Shield Unconditional | 常量名回译 |
| `INC_FIRE_DAMAGE_ON_IGNITED` | Inc Fire Damage On Ignited | 常量名回译 |
| `INC_FIRE_DAMAGE_SPELL` | Inc Fire Damage Spell | 常量名回译 |
| `INC_FOUND_QUANTITY` | Inc Found Quantity | 常量名回译 |
| `INC_FOUND_RARITY` | Inc Found Rarity | 常量名回译 |
| `INC_FROZEN_COLD_RESISTANCE` | Enemies Frozen by you have -8% to Cold Resistance | 完整语料中亦无对应 |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_INFLICT` | Inc Fully Broken Armour Inflict Inflict | 常量名回译 |
| `INC_GLORY_GENERATION_GLOBAL` | Inc Glory Generation Global | 常量名回译 |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES` | Your HeavyStunPlayer buildup empties 50% faster if you've successfully Parried Recently | 完整语料中亦无对应 |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES_RECENTLY` | Your HeavyStunPlayer buildup empties 50% faster if you've successfully Parried Recently | 完整语料中亦无对应 |
| `INC_HINDER_DURATION` | Debuffs you inflict have 4% increased SlowMagnitudeModifier | 完整语料中亦无对应 |
| `INC_IGNORE_ALL_MOVEMENT_PENALTIES` | Inc Ignore All Movement Penalties | 常量名回译 |
| `INC_INFERNAL_FLAME_LOST_NONE_IF_NONE` | #% of Infernal Flame lost per second if none was gained in the past # seconds | 完整语料中亦无对应 |
| `INC_INFUSION_MAGNITUDE` | Inc Infusion Magnitude | 常量名回译 |
| `INC_LEECHES_RESPONSE_HEALTH` | Inc Leeches Response Health | 常量名回译 |
| `INC_LEECHES_RESPONSE_MANA` | Inc Leeches Response Mana | 常量名回译 |
| `INC_LIGHTNING_DAMAGE_ON_SHOCKED` | Inc Lightning Damage On Shocked | 常量名回译 |
| `INC_LIGHT_DAMAGE_SPELL` | Inc Light Damage Spell | 常量名回译 |
| `INC_MAX_LIFE_REGEN` | Inc Max Life Regen | 常量名回译 |
| `INC_META_DAMAGE` | Inc Meta Damage | 常量名回译 |
| `INC_MINIONS_REVIVE_FASTER_ALL` | Inc Minions Revive Faster All | 常量名回译 |
| `INC_MINION_CRIT_CHANCE` | Inc Minion Crit Chance | 常量名回译 |
| `INC_MINION_ELEMENTAL_RESIST` | Inc Minion Elemental Resist | 常量名回译 |
| `INC_MOVEMENT_SPEED_STATIONARY` | Inc Movement Speed Stationary | 常量名回译 |
| `INC_NON_DAMAGING_AILMENT_EFFECT_TAKEN` | Inc Non Damaging Ailment Effect Taken | 常量名回译 |
| `INC_NON_UNIQUE_TIME_LOST` | Non-Unique Time-Lost Jewels have 40% increased radius | 完整语料中亦无对应 |
| `INC_NON_UNIQUE_TIME_LOST_RARITY` | Non-Unique Time-Lost Jewels have 40% increased radius | 完整语料中亦无对应 |
| `INC_PASSIVE_POINTS_BECOME_WEAPON` | 100 Passive Skill Points become WeaponSetPassiveSkillPoints | 完整语料中亦无对应 |
| `INC_PASSIVE_POINTS_BECOME_WEAPON_WEAPONSETPASSIVESKILLPOINTS` | 100 Passive Skill Points become WeaponSetPassiveSkillPoints | 完整语料中亦无对应 |
| `INC_PRIMAL_OWL_FEATHER_UP_OWL_FEATHER` | Gain a Primal Owl Feather every # seconds, up to a maximum of # Expend an Owl Feather when you Dodge to trigger Primal Bounty | 完整语料中亦无对应 |
| `INC_REGENERATE_LIFE_PER_SECOND` | Regenerate #% of maximum Life per second if you have been Hi | 完整语料中亦无对应 |
| `INC_SKILL_QUALITY` | Inc Skill Quality | 常量名回译 |
| `INC_SLOW_MAGNITUDE` | Debuffs you inflict have 5% increased SlowMagnitudeModifier | 完整语料中亦无对应 |
| `INC_SOCKET_NON_UNIQUE_BASIC` | Can Socket a non-Unique Basic Jewel into the Phylactery | 完整语料中亦无对应 |
| `INC_SOCKET_NON_UNIQUE_BASIC_ITEMRARITY` | Can Socket a non-Unique Basic Jewel into the Phylactery | 完整语料中亦无对应 |
| `INC_TAKEN_CRIT_DAMAGE` | Inc Taken Crit Damage | 常量名回译 |
| `INC_TAME_BEAST_CAPTURE_UNIQUE` | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned | 完整语料中亦无对应 |
| `INC_TAME_BEAST_CAPTURE_UNIQUE_RARITY` | Tame Beast can capture Unique Beasts Can have up to one Unique Tamed Beast summoned | 完整语料中亦无对应 |
| `INC_TATTOO_RUNES_ONTO_BODY` | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * 1 Helmet socket * 2 Body Armour sockets * 1 Gloves socket * 1 Boots socket | 完整语料中亦无对应 |
| `INC_TATTOO_RUNES_ONTO_BODY_BODY_ARMOUR` | Can tattoo Runes onto your body, gaining additional Rune-only sockets: * 1 Helmet socket * 2 Body Armour sockets * 1 Gloves socket * 1 Boots socket | 完整语料中亦无对应 |
| `INC_TOTEMS_ALL_ELEMENTAL_RESISTANCES` | Inc Totems All Elemental Resistances | 常量名回译 |
| `INC_TWO_HAND_DAMAGE` | Inc Two Hand Damage | 常量名回译 |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED` | Unique Tamed Beasts are SpiritPossessed by random Azmeri Spirits, changing every 20 seconds | 完整语料中亦无对应 |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED_RARITY` | Unique Tamed Beasts are SpiritPossessed by random Azmeri Spirits, changing every 20 seconds | 完整语料中亦无对应 |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING` | Gain 3 Volatility when an Allied Persistent Reviving Minion is MinionDeath | 完整语料中亦无对应 |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING_MINIONS` | Gain 3 Volatility when an Allied Persistent Reviving Minion is MinionDeath | 完整语料中亦无对应 |
| `INC_VOLATILITY_GENERATION` | Inc Volatility Generation | 常量名回译 |
| `LEECHING_LIFE_CAUSES_COMPANION` | Leeching Life from your HitDamage causes your Companion to also Leech the LeechSameAmount of Life | 完整语料中亦无对应 |
| `LEECHING_LIFE_CAUSES_COMPANION_LIFELEECH` | Leeching Life from your HitDamage causes your Companion to also Leech the LeechSameAmount of Life | 完整语料中亦无对应 |
| `LEECH_HEALTH` | Leech Health | 常量名回译 |
| `LEECH_MANA` | Leech Mana | 常量名回译 |
| `LEECH_TRIGGER` | Leech Trigger | 常量名回译 |
| `NO_LIFE_REGEN_WHILE_BLEEDING` | No Life Regen While Bleeding | 常量名回译 |
| `ONSLAUGHT_ATTACK_SPEED` | Onslaught Attack Speed | 常量名回译 |
| `ONSLAUGHT_CAST_SPEED` | Onslaught Cast Speed | 常量名回译 |
| `ONSLAUGHT_MOVEMENT_SPEED` | Onslaught Movement Speed | 常量名回译 |
| `PENETRATE_CHAOS` | Penetrate Chaos | 常量名回译 |
| `PRESENCE_EXPOSURE_AURA` | Presence Exposure Aura | 常量名回译 |
| `RECOVER_PCT_ENERGY` | Recover Pct Energy | 常量名回译 |
| `REGEN_MAX_HEALTH_PCT` | Regen Max Health Pct | 常量名回译 |
| `SPEED_PER_STACK_ATTACK_SPEED` | Speed Per Stack Attack Speed | 常量名回译 |
| `SPEED_PER_STACK_CAST_SPEED` | Speed Per Stack Cast Speed | 常量名回译 |
| `SPEED_PER_STACK_MOVEMENT_SPEED` | Speed Per Stack Movement Speed | 常量名回译 |
| `TAKEN_CRIT` | Taken Crit | 常量名回译 |
| `TOTEMS_PLACE_EMBANKMENT_AURAS` | Totems you place grant TacticianTotemBuff | 完整语料中亦无对应 |
| `UNCRITTABLE_WHILE_PARRYING` | Uncrittable While Parrying | 常量名回译 |
| `UNHOLY_MIGHT_CHAOS` | Unholy Might Chaos | 常量名回译 |
| `UNHOLY_MIGHT_COLD` | Unholy Might Cold | 常量名回译 |
| `UNHOLY_MIGHT_FIRE` | Unholy Might Fire | 常量名回译 |
| `UNHOLY_MIGHT_LIGHTNING` | Unholy Might Lightning | 常量名回译 |
| `UNHOLY_MIGHT_PHYSICAL` | Unholy Might Physical | 常量名回译 |
