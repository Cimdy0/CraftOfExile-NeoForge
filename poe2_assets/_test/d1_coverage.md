# D1 覆盖分析

判定每个拆分片是否已被 §A 精确命中的旧实现覆盖。已覆盖 -> D1 旧常量为重复登记，直接删除。

## 块 1

`Accuracy is Doubled Never deal Critical Hits`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `ACCURACY_IS_DOUBLED` | Accuracy is Doubled | **无** |
| 2 | `NEVER_DEAL_CRITICAL_HITS` | Never deal Critical Hits | **无** |

旧 D1 常量：REAL ['INC_ACCURACY_RATING_IS_DOUBLED', 'INC_ACCURACY_RATING_IS_DOUBLED_CRIT', 'RESOLUTE_TECHNIQUE_ACCURACY', 'RESOLUTE_TECHNIQUE_CRIT_CHANCE'] / DEAD []

**待裁决片（2）**：`ACCURACY_IS_DOUBLED`, `NEVER_DEAL_CRITICAL_HITS`

---

## 块 2

`All Damage Types is taken from Mana before Health {X}% more Mana Recovery Rate`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` | All Damage Types is taken from Mana before Health | **无** |
| 2 | `X_MORE_MANA_RECOVERY_RATE` | {X}% more Mana Recovery Rate | **无** |

旧 D1 常量：REAL ['MIND_OVER_MATTER_MANA_TAKE'] / DEAD []

**待裁决片（2）**：`ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH`, `X_MORE_MANA_RECOVERY_RATE`

---

## 块 3

`Bleeding you inflict is Aggravated Base Bleeding Duration is {X} second {X}% more Buff Magnitude of Bleeding you inflict`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `BLEEDING_YOU_INFLICT_IS_AGGRAVATED` | Bleeding you inflict is Aggravated | **无** |
| 2 | `BASE_BLEEDING_DURATION_IS_X_SECOND` | Base Bleeding Duration is {X} second | **无** |
| 3 | `X_MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` | {X}% more Buff Magnitude of Bleeding you inflict | **无** |

旧 D1 常量：REAL ['AGGRAVATE_BLEEDING_ON_HIT'] / DEAD []

**待裁决片（3）**：`BLEEDING_YOU_INFLICT_IS_AGGRAVATED`, `BASE_BLEEDING_DURATION_IS_X_SECOND`, `X_MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT`

---

## 块 4

`Cannot be Light Stunned Cannot Dodge Roll or Sprint`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `CANNOT_BE_LIGHT_STUNNED` | Cannot be Light Stunned | **无** |
| 2 | `CANNOT_DODGE_ROLL_OR_SPRINT` | Cannot Dodge Roll or Sprint | **无** |

旧 D1 常量：REAL ['UNWAVERING_STANCE_IMMUNE_STUN'] / DEAD []

**待裁决片（2）**：`CANNOT_BE_LIGHT_STUNNED`, `CANNOT_DODGE_ROLL_OR_SPRINT`

---

## 块 5

`Cannot use Charms {X}% more Recovery from Flasks`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `CANNOT_USE_CHARMS` | Cannot use Charms | **无** |
| 2 | `X_MORE_RECOVERY_FROM_FLASKS` | {X}% more Recovery from Flasks | **无** |

旧 D1 常量：REAL [] / DEAD ['MORE_CANNOT_CHARMS_RECOVERY_FLASKS']

**待裁决片（2）**：`CANNOT_USE_CHARMS`, `X_MORE_RECOVERY_FROM_FLASKS`

---

## 块 6

`Chance to Evasion is Unlucky Chance to Deflect is Lucky`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `CHANCE_TO_EVASION_IS_UNLUCKY` | Chance to Evasion is Unlucky | **无** |
| 2 | `CHANCE_TO_DEFLECT_IS_LUCKY` | Chance to Deflect is Lucky | **无** |

旧 D1 常量：REAL ['GLANCING_BLOWS_DEFLECT_LUCKY', 'GLANCING_BLOWS_EVADE_UNLUCKY'] / DEAD ['CHANCE_EVADE_IS_UNLUCKY_DEFLECT', 'CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY']

**待裁决片（2）**：`CHANCE_TO_EVASION_IS_UNLUCKY`, `CHANCE_TO_DEFLECT_IS_LUCKY`

---

## 块 7

`Create Lightning Elemental Infusion Remnants instead of Fire Create Cold Elemental Infusion Remnants instead of Lightning Create Fire Elemental Infusion Remnants instead of Cold`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE` | Create Lightning Elemental Infusion Remnants instead of Fire | **无** |
| 2 | `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING` | Create Cold Elemental Infusion Remnants instead of Lightning | **无** |
| 3 | `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD` | Create Fire Elemental Infusion Remnants instead of Cold | **无** |

旧 D1 常量：REAL [] / DEAD ['INC_CREATE_LIGHTNING_INFUSION_REMNANTS', 'INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION']

**待裁决片（3）**：`CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE`, `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING`, `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD`

---

## 块 8

`Excess Health Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD` | Excess Health Recovery from Regeneration is applied to Energy Shield | **无** |
| 2 | `ENERGY_SHIELD_DOES_NOT_RECHARGE` | Energy Shield does not Recharge | **无** |

旧 D1 常量：REAL [] / DEAD ['INC_EXCESS_LIFE_RECOVERY_REGENERATION', 'INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD']

**待裁决片（2）**：`EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD`, `ENERGY_SHIELD_DOES_NOT_RECHARGE`

---

## 块 9

`Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `FIRE_SPELLS_CONVERSION_X_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE` | Fire Spells Conversion {X}% of Fire Damage to Chaos Damage | **无** |
| 2 | `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE` | Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude | **无** |
| 3 | `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE` | Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage | **无** |

旧 D1 常量：REAL ['BLACKFLAME_COVENANT_FIRE_TO_CHAOS', 'FIRE_SPELLS_CONVERT_DAMAGE', 'FIRE_SPELLS_CONVERT_DAMAGE_SPELL'] / DEAD []

**待裁决片（3）**：`FIRE_SPELLS_CONVERSION_X_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE`, `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE`, `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE`

---

## 块 10

`Invocation Skills instead Trigger Spells every {X} seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume {X}% more Energy`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_X_SECONDS` | Invocation Skills instead Trigger Spells every {X} seconds | **无** |
| 2 | `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS` | Invocation Skills cannot gain Energy while Triggering Spells | **无** |
| 3 | `INVOKED_SPELLS_CONSUME_X_MORE_ENERGY` | Invoked Spells consume {X}% more Energy | **无** |

旧 D1 常量：REAL [] / DEAD ['MORE_INVOCATION_TRIGGER_SPELLS_CANNOT', 'MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL']

**待裁决片（3）**：`INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_X_SECONDS`, `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS`, `INVOKED_SPELLS_CONSUME_X_MORE_ENERGY`

---

## 块 11

`Max Health is {X} Immune to Chaos Damage and Bleeding`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `MAX_HEALTH_IS_X` | Max Health is {X} | **无** |
| 2 | `IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING` | Immune to Chaos Damage and Bleeding | **无** |

旧 D1 常量：REAL ['CHAOS_INOCULATION_IMMUNE_BLEED', 'CHAOS_INOCULATION_IMMUNE_CHAOS', 'CHAOS_INOCULATION_MAX_HEALTH'] / DEAD []

**待裁决片（2）**：`MAX_HEALTH_IS_X`, `IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING`

---

## 块 12

`Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `STAT_CONVERSION_X_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA` | Stat Conversion {X}% of Max Energy Shield to Max Mana | **无** |
| 2 | `MANA_COSTS_ARE_DOUBLED` | Mana Costs are Doubled | **无** |

旧 D1 常量：REAL ['ELDRITCH_BATTERY_ES_TO_MANA'] / DEAD ['CONVERT_ENERGY_SHIELD_MANA', 'CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION']

**待裁决片（2）**：`STAT_CONVERSION_X_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA`, `MANA_COSTS_ARE_DOUBLED`

---

## 块 13

`You can apply an additional Curse Double Activation Delay of Curses`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` | You can apply an additional Curse | `INC_ADDITIONAL_CURSE` |
| 2 | `DOUBLE_ACTIVATION_DELAY_OF_CURSES` | Double Activation Delay of Curses | **无** |

旧 D1 常量：REAL [] / DEAD ['INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION', 'INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE']

**待裁决片（1）**：`DOUBLE_ACTIVATION_DELAY_OF_CURSES`

---

## 块 14

`You have no Mana Skill Mana Costs Stat Conversion to Health Costs`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `YOU_HAVE_NO_MANA` | You have no Mana | **无** |
| 2 | `SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` | Skill Mana Costs Stat Conversion to Health Costs | **无** |

旧 D1 常量：REAL ['BLOOD_MAGIC_ENERGY_TO_LIFE', 'BLOOD_MAGIC_MANA_TO_LIFE'] / DEAD []

**待裁决片（2）**：`YOU_HAVE_NO_MANA`, `SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS`

---

## 块 15

`Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `YOUR_TOTEM_LIMIT_IS_DOUBLED` | Your Totem Limit is doubled | **无** |
| 2 | `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS` | No Charges requirement for placing Totems | **无** |
| 3 | `TOTEMS_RESERVE_X_SPIRIT_EACH` | Totems reserve {X} Spirit each | **无** |

旧 D1 常量：REAL [] / DEAD ['ADD_MAX_TOTEM', 'INC_TOTEM_LIMIT_IS_DOUBLED', 'INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE']

**待裁决片（3）**：`YOUR_TOTEM_LIMIT_IS_DOUBLED`, `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS`, `TOTEMS_RESERVE_X_SPIRIT_EACH`

---

## 块 16

`gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES` | gain Power Charges instead of Frenzy Charges | **无** |
| 2 | `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES` | gain Frenzy Charges instead of Endurance Charges | **无** |
| 3 | `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES` | gain Endurance Charges instead of Power Charges | **无** |

旧 D1 常量：REAL ['CHARGE_CONVERT_RESONANCE'] / DEAD ['INC_POWER_CHARGES_FRENZY_ENDURANCE', 'INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE']

**待裁决片（3）**：`GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES`, `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES`, `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES`

---

## 块 17

`{X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `X_MORE_MAX_RAGE` | {X}% more Max Rage | **无** |
| 2 | `REGENERATE_X_RAGE_PER_SECOND_PER_X_RAGE_SPENT_RECENTLY` | Regenerate {X} Rage per second per {X} Rage spent Recently | **无** |
| 3 | `NO_RAGE_EFFECT` | No Rage effect | **无** |

旧 D1 常量：REAL ['MORE_MAX_RAGE'] / DEAD ['MORE_RAGE_REGENERATE_SPENT_NO', 'MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY']

**待裁决片（3）**：`X_MORE_MAX_RAGE`, `REGENERATE_X_RAGE_PER_SECOND_PER_X_RAGE_SPENT_RECENTLY`, `NO_RAGE_EFFECT`

---

## 块 18

`{X}% more amount of Health Leeched Leech Health {X}% more quickly Cannot Recover Health other than from Health Leech Health Leech effects are not add when Reservation Health is Filled`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `X_MORE_AMOUNT_OF_HEALTH_LEECHED` | {X}% more amount of Health Leeched | **无** |
| 2 | `LEECH_HEALTH_X_MORE_QUICKLY` | Leech Health {X}% more quickly | **无** |
| 3 | `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH` | Cannot Recover Health other than from Health Leech | **无** |
| 4 | `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED` | Health Leech effects are not add when Reservation Health is Filled | **无** |

旧 D1 常量：REAL ['MORE_AMOUNT_LEECH'] / DEAD []

**待裁决片（4）**：`X_MORE_AMOUNT_OF_HEALTH_LEECHED`, `LEECH_HEALTH_X_MORE_QUICKLY`, `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH`, `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED`

---

## 块 19

`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `X_OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE` | {X}% of Damage Converted to Fire Damage | **无** |
| 2 | `DEAL_NO_NON_FIRE_DAMAGE` | Deal no Non-Fire Damage | **无** |

旧 D1 常量：REAL ['AVATAR_OF_FIRE_CHAOS_TO_FIRE', 'AVATAR_OF_FIRE_COLD_TO_FIRE', 'AVATAR_OF_FIRE_LIGHT_TO_FIRE', 'AVATAR_OF_FIRE_PHYS_TO_FIRE'] / DEAD []

**待裁决片（2）**：`X_OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE`, `DEAL_NO_NON_FIRE_DAMAGE`

---

## 块 20

`{X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armor`

| # | 新常量 | 片原文 | §A 已覆盖（旧实现） |
|---|---|---|---|
| 1 | `X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER` | {X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power | **无** |
| 2 | `ENERGY_SHIELD_DOES_NOT_ESRECHARGE` | Energy Shield does not ESRecharge | **无** |
| 3 | `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION` | You cannot Recover Energy Shield from Regeneration | **无** |
| 4 | `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR` | You cannot Recover Energy Shield to above Armor | **无** |

旧 D1 常量：REAL ['SCARRED_FAITH_PHYS_RECOUP_ENERGY'] / DEAD []

**待裁决片（4）**：`X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER`, `ENERGY_SHIELD_DOES_NOT_ESRECHARGE`, `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION`, `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR`

---

