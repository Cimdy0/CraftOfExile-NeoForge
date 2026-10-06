# Step6-B 冲突组裁决打分

占位标记行 = AttributeRegister L280；引擎消费属性 71 个。

| 判定 | 组数 |
| --- | --- |
| DEDUP | 7 |
| MANUAL | 14 |
| RANK | 15 |

## DEDUP — 7 组

### `ATTACK_DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES`
语料：`Attack Damage Penetrates {X}% of Enemy Elemental Damage Resistances`
- **保留** L1038 rank=2 `inc` attrs=ADDED_DAMAGE tags=ATTACK) [占位]
- 删除 L1142 rank=2 `inc` attrs=ADDED_DAMAGE tags=ATTACK) [占位 b3块]

### `CONVERTS_ALL_EVASION_RATING`（手工通道）
语料：`TODO(接线) StatConversion all Evasion Rating to Armour`
- **保留** L1068 rank=0 `inc` attrs=EVASION tags=— [引擎消费]
- 删除 L1149 rank=0 `inc` attrs=EVASION tags=— [引擎消费 b3块]

### `DAMAGE_PENETRATION_X_ELEMENTAL_DAMAGE_RESISTANCES`
语料：`Damage Penetration {X}% Elemental Damage Resistances`
- **保留** L914 rank=2 `inc` attrs=ADDED_DAMAGE tags=— [占位]
- 删除 L1100 rank=2 `inc` attrs=ADDED_DAMAGE tags=— [占位 b3块]

### `INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND`（手工通道）
语料：`TODO(接线) Minions have 20% increased Cooldown Recovery Rate for Command Skills`
- **保留** L1062 rank=1 `inc` attrs=COOLDOWN_RECOVERY_RATE tags=COMMAND) [—]
- 删除 L1146 rank=1 `inc` attrs=COOLDOWN_RECOVERY_RATE tags=COMMAND) [b3块]

### `RECOVER_X_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED`
语料：`Recover {X}% of Max Health for each Endurance Charge consumed`
- **保留** L958 rank=0 `recoverPctOnEvent` attrs=— tags=EventName.consumeCharge(ChargeType.ENDURANCE.getSerializedName())),HEALTH [—]
- 删除 L1110 rank=0 `recoverPctOnEvent` attrs=— tags=EventName.consumeCharge(ChargeType.ENDURANCE.getSerializedName())),HEALTH [b3块]

### `RECOVER_X_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE`
语料：`Recover {X}% of Max Mana when you consume a Power Charge`
- **保留** L960 rank=0 `recoverPctOnEvent` attrs=— tags=EventName.consumeCharge(ChargeType.POWER.getSerializedName())),MANA [—]
- 删除 L1112 rank=0 `recoverPctOnEvent` attrs=— tags=EventName.consumeCharge(ChargeType.POWER.getSerializedName())),MANA [b3块]

### `THORNS_DAMAGE_HAS_X_CHANCE_TO_IGNORE_ENEMY_ARMOR`
语料：`Thorns Damage has {X}% chance to ignore Enemy Armor`
- **保留** L1070 rank=2 `chance` attrs=ADDED_DAMAGE tags=— [占位]
- 删除 L1150 rank=2 `chance` attrs=ADDED_DAMAGE tags=— [占位 b3块]

## RANK — 15 组

### `IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED`
语料：`Immune to Ignite if a majority of your Socketed Support Gems are Red`
- **保留** L927 rank=2 `inc` attrs=IMMUNE_IGNITE_MAJORITY_SOCKETED tags=IGNITE) [占位]
- 删除 L1106 rank=3 `inc` attrs=IMMUNE_IGNITE_MAJORITY_SOCKETED_1 tags=IGNITE) [占位 _N b3块]

### `INC_ARMOUR_ENERGY`（手工通道）
语料：`10% increased Armour, Evasion and Energy Shield`
- **保留** L1415 rank=0 `valueMod` attrs=MAX_ENERGY_SHIELD tags=EVASION) [引擎消费]
- 删除 L785 rank=1 `valueMod` attrs=ARMOR tags=ENERGY_SHIELD) [—]

### `INC_INFUSION_DURATION`（手工通道）
语料：`10% increased Elemental Infusion duration`
- **保留** L1378 rank=0 `valueMod` attrs=AILMENT_DURATION tags=INFUSION,not(TAKEN)) [引擎消费]
- 删除 L1420 rank=2 `valueMod` attrs=MAX_INFUSION tags=INFUSION) [占位]

### `MINIONS_HAVE_X_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE`
语料：`Minions have {X}% chance to inflict Withered on Hit Damage`
- **保留** L1060 rank=2 `chance` attrs=MINIONS_INFLICT_WITHERED tags=WITHERED) [占位]
- 删除 L1145 rank=3 `chance` attrs=MINIONS_INFLICT_WITHERED_1 tags=WITHERED) [占位 _N b3块]

### `MINIONS_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE`
语料：`Minions have {X}% increased Critical Hit Chance`
- **保留** L1063 rank=2 `chance` attrs=MINIONS_CRITICAL tags=— [占位]
- 删除 L1147 rank=3 `chance` attrs=MINIONS_CRITICAL_1 tags=— [占位 _N b3块]

### `PLANTS_HAVE_A_X_CHANCE_TO_IMMEDIATELY_OVERGROW`
语料：`Plants have a {X}% chance to immediately Overgrow`
- **保留** L1065 rank=2 `chance` attrs=PLANTS_IMMEDIATELY_OVERGROW tags=— [占位]
- 删除 L1148 rank=3 `chance` attrs=PLANTS_IMMEDIATELY_OVERGROW_1 tags=— [占位 _N b3块]

### `SKILLS_HAVE_X_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM`
语料：`Skills have {X}% chance to not add Elemental Infusions but still count as consuming them`
- **保留** L1024 rank=2 `chance` attrs=REMOVE_ELEMENTAL_INFUSIONS_BUT tags=— [占位]
- 删除 L1139 rank=3 `chance` attrs=REMOVE_ELEMENTAL_INFUSIONS_BUT_1 tags=— [占位 _N b3块]

### `X_CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES`
语料：`{X}% chance for Attacks to Maim on Hit Damage against Poisoned Enemies`
- **保留** L880 rank=2 `chance` attrs=ATTACKS_MAIM_POISONED tags=MAIM) [占位]
- 删除 L1088 rank=3 `chance` attrs=ATTACKS_MAIM_POISONED_1 tags=MAIM) [占位 _N b3块]

### `X_CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION`
语料：`{X}% chance for Attack Hit Damage to apply ten Incision`
- **保留** L879 rank=2 `chance` attrs=ATTACK_TEN_INCISION tags=ATTACK,INCISION) [占位]
- 删除 L1087 rank=3 `chance` attrs=ATTACK_TEN_INCISION_1 tags=ATTACK,INCISION) [占位 _N b3块]

### `X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS`
语料：`{X}% chance to Aggravate Bleeding on targets you Hit Damage with Empowered Attacks`
- **保留** L884 rank=2 `chance` attrs=AGGRAVATE_BLEEDING_TARGETS_EMPOWERED tags=— [占位]
- 删除 L1089 rank=3 `chance` attrs=AGGRAVATE_BLEEDING_TARGETS_EMPOWERED_1 tags=— [占位 _N b3块]

### `X_INCREASED_BUFF_EFFECT_OF_ARCANE_SURGE_ON_YOU`
语料：`{X}% increased Buff Effect of Arcane Surge on you`
- **保留** L769 rank=0 `inc` attrs=AILMENT_MAGNITUDE tags=ARCANE_SURGE,not(TAKEN)) [引擎消费]
- 删除 L860 rank=1 `inc` attrs=ARCANE_SURGE_EFFECT_TAKEN tags=ARCANE_SURGE,TAKEN) [—]

### `X_INCREASED_EFFECT_OF_CHILL_ON_YOU`
语料：`{X}% increased Effect of Chill on you`
- **保留** L1157 rank=0 `inc` attrs=AILMENT_MAGNITUDE tags=AILMENT,CHILL),TAKEN [引擎消费]
- 删除 L862 rank=1 `inc` attrs=CHILL_EFFECT_TAKEN tags=CHILL,TAKEN) [—]

### `X_INCREASED_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY`
语料：`{X}% increased Elemental Ailments Ailment Application if you have Shapeshifted to an Animal form Recently`
- **保留** L890 rank=2 `inc` attrs=ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED tags=— [占位]
- 删除 L1091 rank=3 `inc` attrs=ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED_1 tags=— [占位 _N b3块]

### `X_INCREASED_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES`
语料：`{X}% increased Shock Chance against Electrocuted Enemies`
- **保留** L891 rank=2 `chance` attrs=SHOCK_ELECTROCUTED tags=SHOCK) [占位]
- 删除 L1092 rank=3 `chance` attrs=SHOCK_ELECTROCUTED_1 tags=SHOCK) [占位 _N b3块]

### `X_OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD`
语料：`{X}% of Max Health Stat Conversion to Energy Shield`
- **保留** L894 rank=2 `inc` attrs=LIFE_CONVERTED_ENERGY_SHIELD tags=— [占位]
- 删除 L1093 rank=3 `inc` attrs=LIFE_CONVERTED_ENERGY_SHIELD_1 tags=— [占位 _N b3块]

## MANUAL — 14 组

### `DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_RESISTANCES`
语料：`Damage Penetrates {X}% of Enemy Elemental Resistances`
- **保留** L912 rank=2 `inc` attrs=ADDED_DAMAGE tags=GLORY) [占位]
- 删除 L913 rank=2 `inc` attrs=ADDED_DAMAGE tags=— [占位]
- 删除 L915 rank=2 `inc` attrs=ADDED_DAMAGE tags=— [占位]

### `INC_CHARM_DURATION`（手工通道）
语料：`TODO(接线) Temporary Minion Skills have +2 to Limit of Minions summoned`
- **保留** L851 rank=0 `inc` attrs=AILMENT_DURATION tags=CHARM,not(TAKEN)) [引擎消费]
- 删除 L1459 rank=0 `inc` attrs=AILMENT_DURATION tags=MINION,not(TAKEN)) [引擎消费]

### `INC_DAMAGE_EMPOWERED_ATTACKS`（手工通道）
语料：`TODO(接线) 5% chance to inflict Bleeding on HitDamage`
- **保留** L1203 rank=0 `inc` attrs=DAMAGE tags=ATTACK,DAMAGE,EMPOWERED) [引擎消费]
- 删除 L1287 rank=0 `inc` attrs=DAMAGE tags=DAMAGE,EMPOWERED) [引擎消费]

### `INC_DEXTERITY`（手工通道）
语料：`3% increased Attributes`
- **保留** L1276 rank=0 `valueMod` attrs=DEXTERITY tags=— [引擎消费]
- 删除 L1382 rank=0 `valueMod` attrs=INTELLIGENCE tags=— [引擎消费]

### `INC_MAX_VOLATILITY`（手工通道）
语料：`15% increased Volatility Explosion delay`
- **保留** L1437 rank=2 `valueMod` attrs=MAX_VOLATILITY tags=VOLATILITY) [占位]
- 删除 L1659 rank=2 `valueMod` attrs=VOLATILITY_DETONATION_TIME tags=VOLATILITY) [占位 引擎消费]

### `INC_OFFERING_DURATION`（手工通道）
语料：`TODO(接线) Offering Skills have 15% increased Buff effect`
- **保留** L1539 rank=0 `inc` attrs=AILMENT_DURATION tags=OFFERING,not(TAKEN)) [引擎消费]
- 删除 L1541 rank=0 `inc` attrs=AILMENT_MAGNITUDE tags=OFFERING,not(TAKEN)) [引擎消费]

### `INC_PROJECTILE_SPEED`（手工通道）
语料：`8% increased Projectile Speed`
- **保留** L1578 rank=1 `valueMod` attrs=PROJECTILE_SPEED tags=— [—]
- 删除 L1580 rank=1 `inc` attrs=PROJECTILE_SPEED tags=PROJECTILE) [—]

### `LIFE_PER_KILL`（手工通道）
语料：`TODO(接线) Gain 3 Life per enemy killed`
- **保留** L949 rank=0 `recoverPctOnEvent` attrs=— tags=EventName.KILL),HEALTH [—]
- 删除 L1707 rank=0 `map.put` attrs=— tags=EventName.KILL),ResourceType.HEALTH [—]

### `RAGE_ON_HIT`（手工通道）
语料：`Gain 1 Rage on Melee HitDamage`
- **保留** L2418 rank=0 `map.put` attrs=— tags=ResourceType.RAGE [—]
- 删除 L2423 rank=0 `map.put` attrs=— tags=EventName.CAUSED_HIT),ResourceType.RAGE [—]

### `X_CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT`
语料：`{X}% chance to create an additional Remnant`
- **保留** L458 rank=2 `chance` attrs=REMNANT_EXTRA_CHANCE tags=— [占位]
- 删除 L885 rank=2 `chance` attrs=CREATE_ADDITIONAL_REMNANT tags=REMNANT) [占位]

### `X_INCREASED_ARCHON_BUFF_DURATION`
语料：`{X}% increased Archon Buff duration`
- **保留** L771 rank=0 `inc` attrs=AILMENT_DURATION tags=ARCHON,not(TAKEN)) [引擎消费]
- 删除 L1190 rank=0 `inc` attrs=AILMENT_DURATION tags=— [引擎消费]

### `X_INCREASED_EFFECT_OF_PUPPET_MASTER`
语料：`{X}% increased Effect of Puppet Master`
- **保留** L329 rank=2 `add` attrs=PUPPET_MASTER_EFFECT tags=— [占位 引擎消费]
- 删除 L330 rank=2 `add` attrs=PUPPET_MASTER_DURATION tags=— [占位 引擎消费]

### `X_OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD`
语料：`{X}% of Elemental Damage taken Recouped as Energy Shield`
- **保留** L1772 rank=0 `recoup` attrs=— tags=ENERGY) [—]
- 删除 L1774 rank=0 `recoup` attrs=— tags=HEALTH) [—]
- 删除 L1792 rank=0 `recoup` attrs=— tags=MANA) [—]

### `X_SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL`
语料：`{X}% Surpassing Chance to gain a Puppet Master stack whenever you use a Command Skill`
- **保留** L328 rank=2 `chance` attrs=PUPPET_MASTER_CHANCE tags=— [占位 引擎消费]
- 删除 L877 rank=2 `chance` attrs=SURPASSING_PUPPET_MASTER_STACK tags=COMMAND) [占位]
- 删除 L1086 rank=3 `chance` attrs=SURPASSING_PUPPET_MASTER_STACK_1 tags=COMMAND) [占位 _N b3块]
