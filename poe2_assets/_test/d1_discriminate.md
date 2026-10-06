# D1 判别：真实实现 vs 机械占位实现

D1 共 **46** 条。判别结果：REAL **26** / DEAD **20**

`engine_reads` = 该属性在 AttributeRegister 声明与 StatGen 注册之外的引用数。为 0 表示引擎从不读取，属 bug 期自动生成的占位属性。

| 旧常量 | 判定 | 实现形态 | 属性 engine_reads | StatGen 行 |
|---|---|---|---|---|
| `ADD_MAX_TOTEM` | **DEAD** | 单行helper | MAX_TOTEM=0 | 327 |
| `CHANCE_EVADE_IS_UNLUCKY_DEFLECT` | **DEAD** | 单行helper | EVADE_IS_UNLUCKY_DEFLECT=0 | 1125 |
| `CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY` | **DEAD** | 单行helper | EVADE_IS_UNLUCKY_DEFLECT_1=0 | 1709 |
| `CONVERT_ENERGY_SHIELD_MANA` | **DEAD** | 单行helper | CONVERT_ENERGY_SHIELD_MANA=0 | 1588 |
| `CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION` | **DEAD** | 单行helper | CONVERT_ENERGY_SHIELD_MANA_1=0 | 1926 |
| `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION` | **DEAD** | 单行helper | ADDITIONAL_CURSE_DOUBLE_ACTIVATION=0 | 1447 |
| `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE` | **DEAD** | 单行helper | ADDITIONAL_CURSE_DOUBLE_ACTIVATION_1=0 | 1845 |
| `INC_CREATE_LIGHTNING_INFUSION_REMNANTS` | **DEAD** | 单行helper | CREATE_LIGHTNING_INFUSION_REMNANTS=0 | 1128 |
| `INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION` | **DEAD** | 单行helper | CREATE_LIGHTNING_INFUSION_REMNANTS_1=0 | 1712 |
| `INC_EXCESS_LIFE_RECOVERY_REGENERATION` | **DEAD** | 单行helper | EXCESS_LIFE_RECOVERY_REGENERATION=0 | 1165 |
| `INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD` | **DEAD** | 单行helper | EXCESS_LIFE_RECOVERY_REGENERATION_1=0 | 1737 |
| `INC_POWER_CHARGES_FRENZY_ENDURANCE` | **DEAD** | 单行helper | POWER_CHARGES_FRENZY_ENDURANCE=0 | 1185 |
| `INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE` | **DEAD** | 单行helper | POWER_CHARGES_FRENZY_ENDURANCE_1=0 | 1748 |
| `INC_TOTEM_LIMIT_IS_DOUBLED` | **DEAD** | 单行helper | TOTEM_LIMIT_IS_DOUBLED=0 | 1461 |
| `INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE` | **DEAD** | 单行helper | TOTEM_LIMIT_IS_DOUBLED_1=0 | 1850 |
| `MORE_CANNOT_CHARMS_RECOVERY_FLASKS` | **DEAD** | 单行helper | CANNOT_CHARMS_RECOVERY_FLASKS=0 | 1122 |
| `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT` | **DEAD** | 单行helper | INVOCATION_TRIGGER_SPELLS_CANNOT=0 | 1536 |
| `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL` | **DEAD** | 单行helper | INVOCATION_TRIGGER_SPELLS_CANNOT_1=0 | 1897 |
| `MORE_RAGE_REGENERATE_SPENT_NO` | **DEAD** | 单行helper | RAGE_REGENERATE_SPENT_NO=0 | 1032 |
| `MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY` | **DEAD** | 单行helper | RAGE_REGENERATE_SPENT_NO_1=0 | 1662 |
| `AGGRAVATE_BLEEDING_ON_HIT` | **REAL** | Effect类 | - | 437 |
| `AVATAR_OF_FIRE_CHAOS_TO_FIRE` | **REAL** | Effect类 | - | 3005 |
| `AVATAR_OF_FIRE_COLD_TO_FIRE` | **REAL** | Effect类 | - | 3003 |
| `AVATAR_OF_FIRE_LIGHT_TO_FIRE` | **REAL** | Effect类 | - | 3001 |
| `AVATAR_OF_FIRE_PHYS_TO_FIRE` | **REAL** | Effect类 | - | 2998 |
| `BLACKFLAME_COVENANT_FIRE_TO_CHAOS` | **REAL** | Effect类 | - | 3007 |
| `BLOOD_MAGIC_ENERGY_TO_LIFE` | **REAL** | Effect类 | - | 3022 |
| `BLOOD_MAGIC_MANA_TO_LIFE` | **REAL** | Effect类 | - | 3015 |
| `CHAOS_INOCULATION_IMMUNE_BLEED` | **REAL** | Effect类 | - | 2976 |
| `CHAOS_INOCULATION_IMMUNE_CHAOS` | **REAL** | Effect类 | - | 2979 |
| `CHAOS_INOCULATION_MAX_HEALTH` | **REAL** | map.put块 | MAX_HEALTH=14 | 2955 |
| `CHARGE_CONVERT_RESONANCE` | **REAL** | Effect类 | - | 723 |
| `ELDRITCH_BATTERY_ES_TO_MANA` | **REAL** | Effect类 | MAX_MANA=4 | 2991 |
| `FIRE_SPELLS_CONVERT_DAMAGE` | **REAL** | 单行helper | ADDED_DAMAGE=4 | 1519 |
| `FIRE_SPELLS_CONVERT_DAMAGE_SPELL` | **REAL** | 单行helper | ADDED_DAMAGE=4 | 1888 |
| `GLANCING_BLOWS_DEFLECT_LUCKY` | **REAL** | Effect类 | - | 3027 |
| `GLANCING_BLOWS_EVADE_UNLUCKY` | **REAL** | Effect类 | - | 3024 |
| `INC_ACCURACY_RATING_IS_DOUBLED` | **REAL** | 单行helper | ACCURACY=8 | 1463 |
| `INC_ACCURACY_RATING_IS_DOUBLED_CRIT` | **REAL** | 单行helper | ACCURACY=8 | 1851 |
| `MIND_OVER_MATTER_MANA_TAKE` | **REAL** | map.put块 | MANA_TAKE_OF_DAMAGE=5 | 2967 |
| `MORE_AMOUNT_LEECH` | **REAL** | Effect类 | AMOUNT_LEECHED=3 | 2633 |
| `MORE_MAX_RAGE` | **REAL** | 单行helper | MAX_RAGE=6 | 2268 |
| `RESOLUTE_TECHNIQUE_ACCURACY` | **REAL** | map.put块 | ACCURACY=8 | 2972 |
| `RESOLUTE_TECHNIQUE_CRIT_CHANCE` | **REAL** | map.put块 | CRIT_CHANCE=7 | 2961 |
| `SCARRED_FAITH_PHYS_RECOUP_ENERGY` | **REAL** | Effect类 | - | 3011 |
| `UNWAVERING_STANCE_IMMUNE_STUN` | **REAL** | Effect类 | - | 3029 |

---

## DEAD（机械占位，连同占位属性一并删除）

- `ADD_MAX_TOTEM` — 占位属性 MAX_TOTEM  (StatGen:327)
- `CHANCE_EVADE_IS_UNLUCKY_DEFLECT` — 占位属性 EVADE_IS_UNLUCKY_DEFLECT  (StatGen:1125)
- `CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY` — 占位属性 EVADE_IS_UNLUCKY_DEFLECT_1  (StatGen:1709)
- `CONVERT_ENERGY_SHIELD_MANA` — 占位属性 CONVERT_ENERGY_SHIELD_MANA  (StatGen:1588)
- `CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION` — 占位属性 CONVERT_ENERGY_SHIELD_MANA_1  (StatGen:1926)
- `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION` — 占位属性 ADDITIONAL_CURSE_DOUBLE_ACTIVATION  (StatGen:1447)
- `INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE` — 占位属性 ADDITIONAL_CURSE_DOUBLE_ACTIVATION_1  (StatGen:1845)
- `INC_CREATE_LIGHTNING_INFUSION_REMNANTS` — 占位属性 CREATE_LIGHTNING_INFUSION_REMNANTS  (StatGen:1128)
- `INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION` — 占位属性 CREATE_LIGHTNING_INFUSION_REMNANTS_1  (StatGen:1712)
- `INC_EXCESS_LIFE_RECOVERY_REGENERATION` — 占位属性 EXCESS_LIFE_RECOVERY_REGENERATION  (StatGen:1165)
- `INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD` — 占位属性 EXCESS_LIFE_RECOVERY_REGENERATION_1  (StatGen:1737)
- `INC_POWER_CHARGES_FRENZY_ENDURANCE` — 占位属性 POWER_CHARGES_FRENZY_ENDURANCE  (StatGen:1185)
- `INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE` — 占位属性 POWER_CHARGES_FRENZY_ENDURANCE_1  (StatGen:1748)
- `INC_TOTEM_LIMIT_IS_DOUBLED` — 占位属性 TOTEM_LIMIT_IS_DOUBLED  (StatGen:1461)
- `INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE` — 占位属性 TOTEM_LIMIT_IS_DOUBLED_1  (StatGen:1850)
- `MORE_CANNOT_CHARMS_RECOVERY_FLASKS` — 占位属性 CANNOT_CHARMS_RECOVERY_FLASKS  (StatGen:1122)
- `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT` — 占位属性 INVOCATION_TRIGGER_SPELLS_CANNOT  (StatGen:1536)
- `MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL` — 占位属性 INVOCATION_TRIGGER_SPELLS_CANNOT_1  (StatGen:1897)
- `MORE_RAGE_REGENERATE_SPENT_NO` — 占位属性 RAGE_REGENERATE_SPENT_NO  (StatGen:1032)
- `MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY` — 占位属性 RAGE_REGENERATE_SPENT_NO_1  (StatGen:1662)

---

## REAL（真实实现，须挂到新常量）

**`AGGRAVATE_BLEEDING_ON_HIT`**  (StatGen:437)

合并原文：`Bleeding you inflict is Aggravated Base Bleeding Duration is {X} second {X}% more Buff Magnitude of Bleeding you inflict`

```java
        // 「你施加的流血必加重」（POE2 9085 Crimson Assault 第一从句「Bleeding you inflict is Aggravated」）：
        // 与 chance_aggravate_on_hit 同通道（CAUSED_HIT 事件），节点传 1.0 即命中必定施加 AGGRAVATED。
        // （精确语义为「施加流血时加重」，项目仅有命中事件通道，敌人已流血时等效；未流血时施加无副作用。）
        map.put(StatIds.AGGRAVATE_BLEEDING_ON_HIT,
                ChanceOnEventStatEffect.entry(DebuffStatusTypes.AGGRAVATED, 0.0, Set.of(EventName.CAUSED_HIT))
                        .withAmount(ExpressionCodec.of(MathParser.parse("$value"))));
```

**`AVATAR_OF_FIRE_CHAOS_TO_FIRE`**  (StatGen:3005)

合并原文：`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

```java
        map.put(StatIds.AVATAR_OF_FIRE_CHAOS_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.CHAOS, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_COLD_TO_FIRE`**  (StatGen:3003)

合并原文：`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

```java
        map.put(StatIds.AVATAR_OF_FIRE_COLD_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.COLD, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_LIGHT_TO_FIRE`**  (StatGen:3001)

合并原文：`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

```java
        map.put(StatIds.AVATAR_OF_FIRE_LIGHT_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.LIGHTNING, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_PHYS_TO_FIRE`**  (StatGen:2998)

合并原文：`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

```java
        // avatar_of_fire：物理/闪电/冰霜/混沌 各 75% REPLACE 转火
        map.put(StatIds.AVATAR_OF_FIRE_PHYS_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.PHYSICAL, StatTag.FIRE, 0.75));
```

**`BLACKFLAME_COVENANT_FIRE_TO_CHAOS`**  (StatGen:3007)

合并原文：`Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage`

```java
        // blackflame_covenant：火焰「法术」伤 100% REPLACE 转混沌（仅法术来源，POE2「Fire Spells Convert 100% of Fire Damage to Chaos」）
        map.put(StatIds.BLACKFLAME_COVENANT_FIRE_TO_CHAOS,
                DamageConvertStatEffect.entry(Set.of(StatTag.FIRE), StatTag.CHAOS,
                        DamageConvertStatEffect.Mode.REPLACE, 1.0, false, TagExpr.of(Set.of(StatTag.SPELL))));
```

**`BLOOD_MAGIC_ENERGY_TO_LIFE`**  (StatGen:3022)

合并原文：`You have no Mana Skill Mana Costs Stat Conversion to Health Costs`

```java
        map.put(StatIds.BLOOD_MAGIC_ENERGY_TO_LIFE,
                ResourceConvertStatEffect.entry(ResourceType.ENERGY, ResourceType.HEALTH, 1.0));
```

**`BLOOD_MAGIC_MANA_TO_LIFE`**  (StatGen:3015)

合并原文：`You have no Mana Skill Mana Costs Stat Conversion to Health Costs`

```java
        // blood_magic：魔力/能量 100% 转换为生命成本
        map.put(StatIds.BLOOD_MAGIC_MANA_TO_LIFE,
                ResourceConvertStatEffect.entry(ResourceType.MANA, ResourceType.HEALTH, 1.0));
```

**`CHAOS_INOCULATION_IMMUNE_BLEED`**  (StatGen:2976)

合并原文：`Max Health is {X} Immune to Chaos Damage and Bleeding`

```java
        // chaos_inoculation：免疫流血
        map.put(StatIds.CHAOS_INOCULATION_IMMUNE_BLEED,
                ImmuneStatEffect.entry(AilmentStatusTypes.BLEED));
```

**`CHAOS_INOCULATION_IMMUNE_CHAOS`**  (StatGen:2979)

合并原文：`Max Health is {X} Immune to Chaos Damage and Bleeding`

```java
        // chaos_inoculation：免疫混沌伤害（POE2 "Immune to Chaos Damage"）——全量（命中+持续伤害 DoT）混沌伤按 ratio=1.0 归零，无作用域限制。
        // 复用 DamageConvertStatEffect.Mode.NULLIFY（与 Elemental Archon 的 archon_nullify_chaos 同源机制，但此处不限定法术作用域、且 hitsOnly=false 覆盖 DoT）。
        map.put(StatIds.CHAOS_INOCULATION_IMMUNE_CHAOS,
                DamageConvertStatEffect.entry(Set.of(StatTag.CHAOS), DamageConvertStatEffect.Mode.NULLIFY, 1.0, false, TagExpr.ALWAYS));
```

**`CHAOS_INOCULATION_MAX_HEALTH`**  (StatGen:2955)

合并原文：`Max Health is {X} Immune to Chaos Damage and Bleeding`

```java
        // 混沌 inoculation：最大生命 REPLACE=1（覆盖原值，对应 POE「生命替代」）
        map.put(StatIds.CHAOS_INOCULATION_MAX_HEALTH,
                new Stat(ExpressionCodec.of(1.0), List.of(), StatTargets.EMPTY,
                        new ModifierStatEffect(Attributes.MAX_HEALTH,
                                Either.left(AttributeModifier.Operation.ADD_VALUE),
                                ModifierStatEffect.Mode.REPLACE, TagExpr.ALWAYS)));
```

**`CHARGE_CONVERT_RESONANCE`**  (StatGen:723)

合并原文：`gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges`

```java
        // ── 充能获得事件改写（charge_convert，2026-08-17 第六十五节）──
        // 事件标签挂 gain_frenzy / gain_power / 通用 gain_charge（ChargedState.fireChargeEvent 已同时 fire
        // 类型标签与通用标签两枚）；amount = 概率（0~1）。
        // 「得狂怒球改为得暴击球」（POE2 25520 resonance，keystone 必触发 1.0）。
        map.put(StatIds.CHARGE_CONVERT_RESONANCE,
                ChargeConvertStatEffect.convertEntry(ChargeType.FRENZY, ChargeType.POWER, 0.0, Set.of("gain_frenzy"))
                        .withAmount(ExpressionCodec.of(MathParser.parse("$value"))));
```

**`ELDRITCH_BATTERY_ES_TO_MANA`**  (StatGen:2991)

合并原文：`Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled`

```java
        // eldritch_battery：把全部最大能量护盾转化为最大法力（POE2 "100% of max Energy Shield to max Mana"）。
        // MAX_MANA += 100% 的 max_energy（加法式转换，同源机制）。「魔力消耗翻倍」属消耗乘子，见 Stage 4 子系统 B（本条目暂不建模）。
        map.put(StatIds.ELDRITCH_BATTERY_ES_TO_MANA,
                ModifierStatEffect.entry(AttributeRegister.MAX_MANA,
                        new ExpressionCodec.Value(Either.right(
                                Conversions.convertPercent(Conversions.attributeRef("max_energy"), 100.0))),
                        Either.left(AttributeModifier.Operation.ADD_VALUE), Set.of(), List.of()));
```

**`FIRE_SPELLS_CONVERT_DAMAGE`**  (StatGen:1519)

合并原文：`Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage`

```java
        inc(map, StatIds.FIRE_SPELLS_CONVERT_DAMAGE, AttributeRegister.ADDED_DAMAGE, StatTag.IGNITE);
```

**`FIRE_SPELLS_CONVERT_DAMAGE_SPELL`**  (StatGen:1888)

合并原文：`Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage`

```java
        inc(map, StatIds.FIRE_SPELLS_CONVERT_DAMAGE_SPELL, AttributeRegister.ADDED_DAMAGE, StatTag.IGNITE);
```

**`GLANCING_BLOWS_DEFLECT_LUCKY`**  (StatGen:3027)

合并原文：`Chance to Evasion is Unlucky Chance to Deflect is Lucky`

```java
        map.put(StatIds.GLANCING_BLOWS_DEFLECT_LUCKY,
                LuckyStatEffect.entry(LuckyStatEffect.RollSubject.DEFLECT, LuckyStatEffect.RollMode.LUCKY));
```

**`GLANCING_BLOWS_EVADE_UNLUCKY`**  (StatGen:3024)

合并原文：`Chance to Evasion is Unlucky Chance to Deflect is Lucky`

```java
        // glancing_blows：闪避双掷取劣 + 招架双掷取优
        map.put(StatIds.GLANCING_BLOWS_EVADE_UNLUCKY,
                LuckyStatEffect.entry(LuckyStatEffect.RollSubject.EVADE, LuckyStatEffect.RollMode.UNLUCKY));
```

**`INC_ACCURACY_RATING_IS_DOUBLED`**  (StatGen:1463)

合并原文：`Accuracy is Doubled Never deal Critical Hits`

```java
        inc(map, StatIds.INC_ACCURACY_RATING_IS_DOUBLED, AttributeRegister.ACCURACY);
```

**`INC_ACCURACY_RATING_IS_DOUBLED_CRIT`**  (StatGen:1851)

合并原文：`Accuracy is Doubled Never deal Critical Hits`

```java
        inc(map, StatIds.INC_ACCURACY_RATING_IS_DOUBLED_CRIT, AttributeRegister.ACCURACY);
```

**`MIND_OVER_MATTER_MANA_TAKE`**  (StatGen:2967)

合并原文：`All Damage Types is taken from Mana before Health {X}% more Mana Recovery Rate`

```java
        // ═══ 以下为 KeystoneNodes01 内联条目的具体定义（全量重构：写数据生成统一用 Read 引用）═══
        // mind_over_matter：受击方全部承伤从魔力扣除（mana_take=1，ADD_VALUE）
        map.put(StatIds.MIND_OVER_MATTER_MANA_TAKE,
                ModifierStatEffect.entry(AttributeRegister.MANA_TAKE_OF_DAMAGE, ExpressionCodec.of(1.0),
                        Either.left(AttributeModifier.Operation.ADD_VALUE), Set.of(), List.of()));
```

**`MORE_AMOUNT_LEECH`**  (StatGen:2633)

合并原文：`{X}% more amount of Health Leeched Leech Health {X}% more quickly Cannot Recover Health other than from Health Leech Health Leech effects are not add when Reservation Health is Filled`

```java
        // 偷取量 more（vaal_pact「50% more amount of Life Leeched」：ADD_MULTIPLIED_TOTAL + $value/100）
        map.put(StatIds.MORE_AMOUNT_LEECH,
                ModifierStatEffect.entry(AttributeRegister.AMOUNT_LEECHED, ExpressionCodec.of(MathParser.parse("$value/100")),
                        Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        Set.of(StatTag.LEECHING, "amount"), List.of()));
```

**`MORE_MAX_RAGE`**  (StatGen:2268)

合并原文：`{X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect`

```java
        // 「100% more Maximum Rage」（37484 Primal Hunger 关键石）：引擎 RageHelper.getMaxRage 裸读
        // MAX_RAGE（ADD_MULTIPLIED_BASE 倍率生效），节点传小数（1.0 = ×2）。
        // 2026-08-19 机制推进：原节点错接 ADD_MAX_RAGE 1.0（+1 怒气），语义不符。
        valueMod(map, StatIds.MORE_MAX_RAGE, AttributeRegister.MAX_RAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
```

**`RESOLUTE_TECHNIQUE_ACCURACY`**  (StatGen:2972)

合并原文：`Accuracy is Doubled Never deal Critical Hits`

```java
        // resolute_technique：精准 +100% more（ADD_MULTIPLIED_TOTAL）
        map.put(StatIds.RESOLUTE_TECHNIQUE_ACCURACY,
                ModifierStatEffect.entry(AttributeRegister.ACCURACY, ExpressionCodec.of(1.0),
                        Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), Set.of(), List.of()));
```

**`RESOLUTE_TECHNIQUE_CRIT_CHANCE`**  (StatGen:2961)

合并原文：`Accuracy is Doubled Never deal Critical Hits`

```java
        // 绝不暴击：暴击率经 AttributeLimiters 绝对钉死 0（set_value 通道，压制一切暴击来源）
        map.put(StatIds.RESOLUTE_TECHNIQUE_CRIT_CHANCE,
                new Stat(ExpressionCodec.of(0.0), List.of(), StatTargets.EMPTY,
                        new ModifierStatEffect(AttributeRegister.CRIT_CHANCE,
                                Either.right(AttributeLimiters.Operation.SET_VALUE),
                                ModifierStatEffect.Mode.GAIN, TagExpr.ALWAYS)));
```

**`SCARRED_FAITH_PHYS_RECOUP_ENERGY`**  (StatGen:3011)

合并原文：`{X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armor`

```java
        // scarred_faith：物理伤害 5% 以能量护盾形式补给（POE2「5% of Physical Damage prevented Recouped as Energy Shield」；
        //   「prevented」基数为承伤近似，且「ES 不回充/不从再生恢复/不超过护甲」暂未建模）
        map.put(StatIds.SCARRED_FAITH_PHYS_RECOUP_ENERGY,
                RecoupStatEffect.entry(Set.of(StatTag.ENERGY), TagExpr.of(Set.of(StatTag.PHYSICAL)), 0.05));
```

**`UNWAVERING_STANCE_IMMUNE_STUN`**  (StatGen:3029)

合并原文：`Cannot be Light Stunned Cannot Dodge Roll or Sprint`

```java
        // unwavering_stance：免疫轻晕（STUN）
        map.put(StatIds.UNWAVERING_STANCE_IMMUNE_STUN,
                ImmuneStatEffect.entry(DebuffStatusTypes.STUN));
```

