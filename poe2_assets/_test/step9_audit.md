# Step 9 严格排查报告（修正版）

## 摘要

- StatIds 常量总数: 678（StatGen 接线 678，无接线 0）
- A. 孤儿 Id（全仓零引用）: 0
- B. 重复 StatGen: 3
- C. 共享注释（备注错接/重复生成）: 58 组（连贯簇 22 / 疑似错接 36）
- D. 重复 path: 0 组
- E. 悬空 StatGen: 0
- F. 仅 StatGen 死实现: 0

## A. 孤儿 Id（全仓零引用）


## B. 重复 StatGen

- `GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE` ×5 [extraDamage]
- `GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE` ×5 [extraDamage]
- `GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` ×6 [extraDamage]

## C. 共享注释（备注错接/重复生成）

- 连贯簇（同前缀 keystone 子效果，疑似正常）: 22 组
- 疑似错接（跨前缀/语义不符，需修）: 36 组

### C1. 连贯簇（疑似正常，仅记录）


**注释**: 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage  （公共前缀: `AVATAR_OF_FIRE`）

- `AVATAR_OF_FIRE_CHAOS_TO_FIRE` StatGen×1[map.put]
- `AVATAR_OF_FIRE_COLD_TO_FIRE` StatGen×1[map.put]
- `AVATAR_OF_FIRE_LIGHT_TO_FIRE` StatGen×1[map.put]
- `AVATAR_OF_FIRE_PHYS_TO_FIRE` StatGen×1[map.put]

**注释**: Maximum Life is 1 Immune to Chaos Damage and Bleeding  （公共前缀: `CHAOS_INOCULATION`）

- `CHAOS_INOCULATION_IMMUNE_BLEED` StatGen×1[map.put]
- `CHAOS_INOCULATION_IMMUNE_CHAOS` StatGen×1[map.put]
- `CHAOS_INOCULATION_MAX_HEALTH` StatGen×1[map.put]

**注释**: Damage Penetration 18% Cold Resistance  （公共前缀: `INC_AILMENT_CHANCE_VS_EXPOSED`）

- `INC_AILMENT_CHANCE_VS_EXPOSED_COLD` StatGen×1[-]
- `INC_AILMENT_CHANCE_VS_EXPOSED_FIRE` StatGen×1[-]
- `INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING` StatGen×1[-]

**注释**: Damage Penetration 18% Fire Resistance  （公共前缀: `INC_AILMENT_DURATION_VS_EXPOSED`）

- `INC_AILMENT_DURATION_VS_EXPOSED_COLD` StatGen×1[-]
- `INC_AILMENT_DURATION_VS_EXPOSED_FIRE` StatGen×1[-]
- `INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING` StatGen×1[-]

**注释**: 15% increased Stun Buildup  （公共前缀: `INC_STUN`）

- `INC_STUN_BUILDUP` StatGen×1[valueMod]
- `INC_STUN_DURATION` StatGen×1[valueMod]

**注释**: You have no Mana Skill Mana Costs StatConversion to Life Costs  （公共前缀: `BLOOD_MAGIC`）

- `BLOOD_MAGIC_ENERGY_TO_LIFE` StatGen×1[map.put]
- `BLOOD_MAGIC_MANA_TO_LIFE` StatGen×1[map.put]

**注释**: Attacks gain increased Accuracy Rating equal to their Critical Hit Chance  （公共前缀: `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL`）

- `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL` StatGen×1[chance]
- `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL` StatGen×1[chance]

**注释**: Damage with Hits is Lucky against Enemies that are on Low Life  （公共前缀: `DAMAGE_LUCKY`）

- `DAMAGE_LUCKY` StatGen×1[map.put]
- `DAMAGE_LUCKY_VS_LOW_LIFE` StatGen×1[map.put]

**注释**: Chance to Evasion is Unlucky Chance to Deflect is Lucky  （公共前缀: `GLANCING_BLOWS`）

- `GLANCING_BLOWS_DEFLECT_LUCKY` StatGen×1[map.put]
- `GLANCING_BLOWS_EVADE_UNLUCKY` StatGen×1[map.put]

**注释**: 50% increased effect of Incision  （公共前缀: `INC_INCISION`）

- `INC_INCISION_EFFECT` StatGen×1[inc]
- `INC_INCISION_MAGNITUDE` StatGen×1[inc]

**注释**: Increases and Reductions to Projectile Speed also apply to Damage with Bows  （公共前缀: `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED`）

- `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED` StatGen×1[inc]
- `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW` StatGen×1[inc]

**注释**: Regenerate 1% of maximum Life per second while affected by any Damaging Ailment  （公共前缀: `INC_REGENERATE_LIFE_AFFECTED_ANY`）

- `INC_REGENERATE_LIFE_AFFECTED_ANY` StatGen×1[-]
- `INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED` StatGen×1[-]

**注释**: Regenerate 1.5% of maximum Life per second while on Low Life  （公共前缀: `INC_REGENERATE_LIFE_LOW`）

- `INC_REGENERATE_LIFE_LOW` StatGen×1[-]
- `INC_REGENERATE_LIFE_LOW_WHILE_ON` StatGen×1[-]

**注释**: Regenerate 2.5% of maximum Life per second while Surrounded  （公共前缀: `INC_REGENERATE_LIFE_SURROUNDED`）

- `INC_REGENERATE_LIFE_SURROUNDED` StatGen×1[-]
- `INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND` StatGen×1[-]

**注释**: Regenerate 1% of maximum Life per second while you have a Totem  （公共前缀: `INC_REGENERATE_LIFE_TOTEM`）

- `INC_REGENERATE_LIFE_TOTEM` StatGen×1[-]
- `INC_REGENERATE_LIFE_TOTEM_WHILE_YOU` StatGen×1[-]

**注释**: Regenerate 1% of maximum Life per Second if you've used a Life Flask in the past 10 seconds  （公共前缀: `INC_REGENERATE_LIFE_VE_FLASK`）

- `INC_REGENERATE_LIFE_VE_FLASK` StatGen×1[-]
- `INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND` StatGen×1[-]

**注释**: 20% slower start of Energy Shield Recharge  （公共前缀: `INC_SLOWER_START_ENERGY_SHIELD`）

- `INC_SLOWER_START_ENERGY_SHIELD` StatGen×1[-]
- `INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION` StatGen×1[-]

**注释**: 6% increased Trap Throwing Speed  （公共前缀: `INC_TRAP`）

- `INC_TRAP_THROWING_SPEED` StatGen×1[inc]
- `INC_TRAP_THROW_SPEED` StatGen×1[inc]

**注释**: Recover 50% of maximum Life when you Heavy Stun a Rare or Unique Enemy  （公共前缀: `RECOVER_LIFE_HEAVY_STUN`）

- `RECOVER_LIFE_HEAVY_STUN` StatGen×1[recoverPctOnEvent]
- `RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN` StatGen×1[recoverPctOnEvent]

**注释**: Recover 2% of maximum Life on Killing a Poisoned Enemy  （公共前缀: `RECOVER_LIFE_KILLING_POISONED`）

- `RECOVER_LIFE_KILLING_POISONED` StatGen×1[recoverPctOnEvent]
- `RECOVER_LIFE_KILLING_POISONED_POISON` StatGen×1[recoverPctOnEvent]

**注释**: Recover 2% of maximum Life when one of your Minions is Reviving  （公共前缀: `RECOVER_LIFE_MINIONS_IS`）

- `RECOVER_LIFE_MINIONS_IS` StatGen×1[recoverPctOnEvent]
- `RECOVER_LIFE_MINIONS_IS_MINION` StatGen×1[recoverPctOnEvent]

**注释**: Accuracy Rating is Doubled Never deal Critical Hits  （公共前缀: `RESOLUTE_TECHNIQUE`）

- `RESOLUTE_TECHNIQUE_ACCURACY` StatGen×1[map.put]
- `RESOLUTE_TECHNIQUE_CRIT_CHANCE` StatGen×1[map.put]

### C2. 疑似错接组（优先排查）


**注释**: 3% increased Movement Speed  （公共前缀: `(无)`）

- `INC_MOVEMENT_SPEED` StatGen×1[valueMod]
- `INC_BLEED_DURATION` StatGen×1[inc]
- `MINION_MOVEMENT_SPEED` StatGen×1[inc]

**注释**: 5% chance to Daze on HitDamage  （公共前缀: `(无)`）

- `CHANCE_DAZE_ON_HIT` StatGen×1[map.put]
- `CONVERT_EXTRA_PHYS_TO_COLD` StatGen×1[map.put]
- `CONVERT_EXTRA_PHYS_TO_LIGHTNING` StatGen×1[map.put]

**注释**: 5% chance to inflict Bleeding on HitDamage  （公共前缀: `(无)`）

- `ADD_AILMENT_CHANCE_BLEED` StatGen×1[valueMod]
- `INC_DAMAGE_EMPOWERED_ATTACKS` StatGen×1[inc]

**注释**: +10 to Intelligence  （公共前缀: `(无)`）

- `ADD_INTELLIGENCE` StatGen×1[valueMod]
- `RECOVER_PCT_HEALTH_ON_KILL` StatGen×1[recoverPctOnEvent]

**注释**: +8% SurpassChance chance to fire an additional Projectile  （公共前缀: `(无)`）

- `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW` StatGen×1[valueMod]
- `DELIVERY_EXTRA_PROJECTILE` StatGen×1[map.put]

**注释**: +1 to Maximum Frenzy Charges  （公共前缀: `(无)`）

- `ADD_MAX_FRENZY` StatGen×1[add]
- `GRANT_FRENZY_CHARGE_ON_GAIN` StatGen×1[map.put]

**注释**: +1 to Maximum Power Charges  （公共前缀: `(无)`）

- `ADD_MAX_POWER` StatGen×1[add]
- `GRANT_POWER_CHARGE_ON_GAIN` StatGen×1[map.put]

**注释**: 10% increased Mana Recovery from Flasks  （公共前缀: `(无)`）

- `FLASK_MANA_RECOVERY` StatGen×1[inc]
- `INC_FLASK_MANA_RECOVERY` StatGen×1[inc]

**注释**: 10% increased Projectile Damage  （公共前缀: `INC`）

- `INC_ATTACK_DAMAGE` StatGen×1[valueMod]
- `INC_PROJECTILE_DAMAGE` StatGen×1[inc]

**注释**: Aura Skills have 5% increased BuffMagnitude  （公共前缀: `INC`）

- `INC_BUFF_MAGNITUDE` StatGen×1[valueMod]
- `INC_AURA_MAGNITUDE` StatGen×1[inc]

**注释**: Each Totem applies 2% increased Damage taken to Enemies in their Presence  （公共前缀: `(无)`）

- `INC_DAMAGE_PRESENCE_ENEMIES` StatGen×1[-]
- `TOTEM_RECOUP_HEALTH` StatGen×1[map.put]

**注释**: 3% increased Attributes  （公共前缀: `INC`）

- `INC_DEXTERITY` StatGen×1[valueMod]
- `INC_STRENGTH` StatGen×1[valueMod]

**注释**: 12% increased Grenade Damage  （公共前缀: `(无)`）

- `INC_GRENADE_DAMAGE` StatGen×1[-]
- `GRENADE_DAMAGE` StatGen×1[inc]

**注释**: 16% increased Hazard Damage  （公共前缀: `(无)`）

- `INC_HAZARD_DAMAGE` StatGen×1[-]
- `HAZARD_DAMAGE` StatGen×1[inc]

**注释**: Herald Skills deal 20% increased Damage  （公共前缀: `(无)`）

- `INC_HERALD_DAMAGE` StatGen×1[-]
- `HERALD_DAMAGE` StatGen×1[inc]

**注释**: Minions deal 10% increased Damage  （公共前缀: `(无)`）

- `INC_MINION_DAMAGE` StatGen×1[-]
- `MINION_DAMAGE` StatGen×1[inc]

**注释**: Minions have 3% increased Attack and Cast Speed  （公共前缀: `(无)`）

- `MINION_ATTACK_SPEED` StatGen×1[inc]
- `INC_MINIONS_ATTACK_CAST_SPEED` StatGen×1[inc]

**注释**: Gain 1 Rage on Melee HitDamage  （公共前缀: `(无)`）

- `RAGE_ON_HIT` StatGen×1[map.put]
- `INC_MAX_RAGE` StatGen×1[add]

**注释**: +1 to Maximum Endurance Charges  （公共前缀: `(无)`）

- `ADD_MAX_ENDURANCE` StatGen×1[add]
- `GRANT_ENDURANCE_CHARGE_ON_GAIN` StatGen×1[map.put]

**注释**: Temporary Minion Skills have +2 to Limit of Minions summoned  （公共前缀: `(无)`）

- `ADD_MAX_MINION` StatGen×1[add]
- `INC_CHARM_DURATION` StatGen×1[inc]

**注释**: 15% chance to Pierce an Enemy  （公共前缀: `(无)`）

- `ADD_PROJECTILE_PIERCE_CHANCE` StatGen×1[add]
- `INC_PROJECTILE_PIERCE` StatGen×1[inc]

**注释**: 10% increased BuffMagnitude of Bleeding you inflict  （公共前缀: `(无)`）

- `AILMENT_MAGNITUDE_BLEED` StatGen×1[-]
- `MORE_BLEEDING_MAGNITUDE_INFLICTED` StatGen×1[more]

**注释**: 10% increased BuffMagnitude of Poison you inflict  （公共前缀: `(无)`）

- `AILMENT_MAGNITUDE_POISON` StatGen×1[-]
- `INC_POISON_MAGNITUDE` StatGen×1[valueMod]

**注释**: 15% increased BuffMagnitude of Shock you inflict  （公共前缀: `(无)`）

- `AILMENT_MAGNITUDE_SHOCK` StatGen×1[-]
- `INC_SHOCK_MAGNITUDE` StatGen×1[valueMod]

**注释**: 20% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground  （公共前缀: `(无)`）

- `CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED` StatGen×1[map.put]
- `INC_JAGGED_GROUND_DURATION` StatGen×1[inc]

**注释**: 10% chance for Flasks you use to not consume Charges  （公共前缀: `(无)`）

- `CHANCE_FLASK_NO_CONSUME` StatGen×1[chance]
- `FLASK_NO_CONSUME` StatGen×1[chance]

**注释**: StatConversion all Evasion Rating to Armour  （公共前缀: `(无)`）

- `CONVERTS_ALL_EVASION_RATING` StatGen×1[inc]
- `IRON_REFLEXES_EVASION_TO_ARMOR` StatGen×1[map.put]

**注释**: Gain 10% of Damage as Extra Physical Damage  （公共前缀: `(无)`）

- `CONVERT_EXTRA_GENERIC_TO_PHYSICAL` StatGen×1[map.put]
- `EXTRA_PHYSICAL` StatGen×1[extraDamage]

**注释**: 10% increased Skill Effect Duration  （公共前缀: `INC`）

- `INC_AILMENT_DURATION` StatGen×1[inc]
- `INC_SKILL_EFFECT_DURATION` StatGen×1[inc]

**注释**: 15% increased Crossbow Reload Speed  （公共前缀: `INC`）

- `INC_CROSSBOW_DAMAGE` StatGen×1[inc]
- `INC_RELOAD_SPEED` StatGen×1[inc]

**注释**: 10% increased Attack Damage  （公共前缀: `INC`）

- `INC_DAMAGE_ATTACK` StatGen×1[inc]
- `INC_SPELL_DAMAGE` StatGen×1[inc]

**注释**: 4% of Damage is taken from Mana before Life  （公共前缀: `INC`）

- `INC_DAMAGE_IS_TAKEN_MANA` StatGen×1[inc]
- `INC_MANA_TAKE` StatGen×1[inc]

**注释**: Mark Skills have 10% increased Use Speed  （公共前缀: `(无)`）

- `INC_MARK_EFFECT` StatGen×1[inc]
- `MARK_USE_SPEED` StatGen×1[inc]

**注释**: Minions Reviving 5% faster  （公共前缀: `INC`）

- `INC_MINIONS_REVIVE_FASTER` StatGen×1[-]
- `INC_MINION_REVIVE` StatGen×1[inc]

**注释**: 10% increased Trap Damage  （公共前缀: `(无)`）

- `INC_TRAP_DAMAGE` StatGen×1[inc]
- `TRAP_DAMAGE` StatGen×1[inc]

**注释**: Minions Gain 20% of Elemental Damage as Extra Chaos Damage  （公共前缀: `MINION`）

- `MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS` StatGen×1[map.put]
- `MINION_RECOUP_HEALTH` StatGen×1[map.put]

## D. 重复 path


## E. 悬空 StatGen


## F. 仅 StatGen 死实现


## 附1：无 StatGen 接线的 Id（0）


## 附2：B. 重复 StatGen（同 Id ≥2 次）

- `GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE` ×5 [extraDamage]
- `GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE` ×5 [extraDamage]
- `GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` ×6 [extraDamage]