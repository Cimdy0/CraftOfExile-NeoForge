# D1 一对多拆分 · 逐块裁决材料

共 **46** 条旧常量 / **20** 个多行块。全部仅被 StatGen 引用。

每块给出：① 语料原文按换行语义切出的新常量（有序）② 旧常量及其现有 StatGen 实现原文。
裁决目标：把每个旧实现挂到对应的新常量上；无对应者标记删除。

---

## 块 1

**合并原文**：`Accuracy is Doubled Never deal Critical Hits`

### 新常量（2，有序）

1. `ACCURACY_IS_DOUBLED` — Accuracy is Doubled
2. `NEVER_DEAL_CRITICAL_HITS` — Never deal Critical Hits

### 旧常量（4）及现有实现

**`INC_ACCURACY_RATING_IS_DOUBLED`** (id `inc_accuracy_rating_is_doubled`)  StatGen:1463

```java
        inc(map, StatIds.INC_ACCURACY_RATING_IS_DOUBLED, AttributeRegister.ACCURACY);
```

**`INC_ACCURACY_RATING_IS_DOUBLED_CRIT`** (id `inc_accuracy_rating_is_doubled_crit`)  StatGen:1851

```java
        inc(map, StatIds.INC_ACCURACY_RATING_IS_DOUBLED_CRIT, AttributeRegister.ACCURACY);
```

**`RESOLUTE_TECHNIQUE_ACCURACY`** (id `resolute_technique_accuracy`)  StatGen:2972

```java
        // resolute_technique：精准 +100% more（ADD_MULTIPLIED_TOTAL）
        map.put(StatIds.RESOLUTE_TECHNIQUE_ACCURACY,
                ModifierStatEffect.entry(AttributeRegister.ACCURACY, ExpressionCodec.of(1.0),
                        Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), Set.of(), List.of()));
```

**`RESOLUTE_TECHNIQUE_CRIT_CHANCE`** (id `resolute_technique_crit_chance`)  StatGen:2961

```java
        // 绝不暴击：暴击率经 AttributeLimiters 绝对钉死 0（set_value 通道，压制一切暴击来源）
        map.put(StatIds.RESOLUTE_TECHNIQUE_CRIT_CHANCE,
                new Stat(ExpressionCodec.of(0.0), List.of(), StatTargets.EMPTY,
                        new ModifierStatEffect(AttributeRegister.CRIT_CHANCE,
                                Either.right(AttributeLimiters.Operation.SET_VALUE),
                                ModifierStatEffect.Mode.GAIN, TagExpr.ALWAYS)));
```

---

## 块 2

**合并原文**：`All Damage Types is taken from Mana before Health {X}% more Mana Recovery Rate`

### 新常量（2，有序）

1. `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH` — All Damage Types is taken from Mana before Health
2. `X_MORE_MANA_RECOVERY_RATE` — {X}% more Mana Recovery Rate

### 旧常量（1）及现有实现

**`MIND_OVER_MATTER_MANA_TAKE`** (id `mind_over_matter_mana_take`)  StatGen:2967

```java
        // ═══ 以下为 KeystoneNodes01 内联条目的具体定义（全量重构：写数据生成统一用 Read 引用）═══
        // mind_over_matter：受击方全部承伤从魔力扣除（mana_take=1，ADD_VALUE）
        map.put(StatIds.MIND_OVER_MATTER_MANA_TAKE,
                ModifierStatEffect.entry(AttributeRegister.MANA_TAKE_OF_DAMAGE, ExpressionCodec.of(1.0),
                        Either.left(AttributeModifier.Operation.ADD_VALUE), Set.of(), List.of()));
```

---

## 块 3

**合并原文**：`Bleeding you inflict is Aggravated Base Bleeding Duration is {X} second {X}% more Buff Magnitude of Bleeding you inflict`

### 新常量（3，有序）

1. `BLEEDING_YOU_INFLICT_IS_AGGRAVATED` — Bleeding you inflict is Aggravated
2. `BASE_BLEEDING_DURATION_IS_X_SECOND` — Base Bleeding Duration is {X} second
3. `X_MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` — {X}% more Buff Magnitude of Bleeding you inflict

### 旧常量（1）及现有实现

**`AGGRAVATE_BLEEDING_ON_HIT`** (id `aggravate_bleeding_on_hit`)  StatGen:437

```java
        // 「你施加的流血必加重」（POE2 9085 Crimson Assault 第一从句「Bleeding you inflict is Aggravated」）：
        // 与 chance_aggravate_on_hit 同通道（CAUSED_HIT 事件），节点传 1.0 即命中必定施加 AGGRAVATED。
        // （精确语义为「施加流血时加重」，项目仅有命中事件通道，敌人已流血时等效；未流血时施加无副作用。）
        map.put(StatIds.AGGRAVATE_BLEEDING_ON_HIT,
                ChanceOnEventStatEffect.entry(DebuffStatusTypes.AGGRAVATED, 0.0, Set.of(EventName.CAUSED_HIT))
                        .withAmount(ExpressionCodec.of(MathParser.parse("$value"))));
```

---

## 块 4

**合并原文**：`Cannot be Light Stunned Cannot Dodge Roll or Sprint`

### 新常量（2，有序）

1. `CANNOT_BE_LIGHT_STUNNED` — Cannot be Light Stunned
2. `CANNOT_DODGE_ROLL_OR_SPRINT` — Cannot Dodge Roll or Sprint

### 旧常量（1）及现有实现

**`UNWAVERING_STANCE_IMMUNE_STUN`** (id `unwavering_stance_immune_stun`)  StatGen:3029

```java
        // unwavering_stance：免疫轻晕（STUN）
        map.put(StatIds.UNWAVERING_STANCE_IMMUNE_STUN,
                ImmuneStatEffect.entry(DebuffStatusTypes.STUN));
```

---

## 块 5

**合并原文**：`Cannot use Charms {X}% more Recovery from Flasks`

### 新常量（2，有序）

1. `CANNOT_USE_CHARMS` — Cannot use Charms
2. `X_MORE_RECOVERY_FROM_FLASKS` — {X}% more Recovery from Flasks

### 旧常量（1）及现有实现

**`MORE_CANNOT_CHARMS_RECOVERY_FLASKS`** (id `more_cannot_charms_recovery_flasks`)  StatGen:1122

```java
        more(map, StatIds.MORE_CANNOT_CHARMS_RECOVERY_FLASKS, AttributeRegister.CANNOT_CHARMS_RECOVERY_FLASKS);
```

---

## 块 6

**合并原文**：`Chance to Evasion is Unlucky Chance to Deflect is Lucky`

### 新常量（2，有序）

1. `CHANCE_TO_EVASION_IS_UNLUCKY` — Chance to Evasion is Unlucky
2. `CHANCE_TO_DEFLECT_IS_LUCKY` — Chance to Deflect is Lucky

### 旧常量（4）及现有实现

**`CHANCE_EVADE_IS_UNLUCKY_DEFLECT`** (id `chance_evade_is_unlucky_deflect`)  StatGen:1125

```java
        chance(map, StatIds.CHANCE_EVADE_IS_UNLUCKY_DEFLECT, AttributeRegister.EVADE_IS_UNLUCKY_DEFLECT, StatTag.DEFLECT);
```

**`CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY`** (id `chance_evade_is_unlucky_deflect_lucky`)  StatGen:1709

```java
        chance(map, StatIds.CHANCE_EVADE_IS_UNLUCKY_DEFLECT_LUCKY, AttributeRegister.EVADE_IS_UNLUCKY_DEFLECT_1, StatTag.DEFLECT);
```

**`GLANCING_BLOWS_DEFLECT_LUCKY`** (id `glancing_blows_deflect_lucky`)  StatGen:3027

```java
        map.put(StatIds.GLANCING_BLOWS_DEFLECT_LUCKY,
                LuckyStatEffect.entry(LuckyStatEffect.RollSubject.DEFLECT, LuckyStatEffect.RollMode.LUCKY));
```

**`GLANCING_BLOWS_EVADE_UNLUCKY`** (id `glancing_blows_evade_unlucky`)  StatGen:3024

```java
        // glancing_blows：闪避双掷取劣 + 招架双掷取优
        map.put(StatIds.GLANCING_BLOWS_EVADE_UNLUCKY,
                LuckyStatEffect.entry(LuckyStatEffect.RollSubject.EVADE, LuckyStatEffect.RollMode.UNLUCKY));
```

---

## 块 7

**合并原文**：`Create Lightning Elemental Infusion Remnants instead of Fire Create Cold Elemental Infusion Remnants instead of Lightning Create Fire Elemental Infusion Remnants instead of Cold`

### 新常量（3，有序）

1. `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE` — Create Lightning Elemental Infusion Remnants instead of Fire
2. `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING` — Create Cold Elemental Infusion Remnants instead of Lightning
3. `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD` — Create Fire Elemental Infusion Remnants instead of Cold

### 旧常量（2）及现有实现

**`INC_CREATE_LIGHTNING_INFUSION_REMNANTS`** (id `inc_create_lightning_infusion_remnants`)  StatGen:1128

```java
        inc(map, StatIds.INC_CREATE_LIGHTNING_INFUSION_REMNANTS, AttributeRegister.CREATE_LIGHTNING_INFUSION_REMNANTS);
```

**`INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION`** (id `inc_create_lightning_infusion_remnants_elementalinfusion`)  StatGen:1712

```java
        inc(map, StatIds.INC_CREATE_LIGHTNING_INFUSION_REMNANTS_ELEMENTALINFUSION, AttributeRegister.CREATE_LIGHTNING_INFUSION_REMNANTS_1);
```

---

## 块 8

**合并原文**：`Excess Health Recovery from Regeneration is applied to Energy Shield Energy Shield does not Recharge`

### 新常量（2，有序）

1. `EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD` — Excess Health Recovery from Regeneration is applied to Energy Shield
2. `ENERGY_SHIELD_DOES_NOT_RECHARGE` — Energy Shield does not Recharge

### 旧常量（2）及现有实现

**`INC_EXCESS_LIFE_RECOVERY_REGENERATION`** (id `inc_excess_life_recovery_regeneration`)  StatGen:1165

```java
        inc(map, StatIds.INC_EXCESS_LIFE_RECOVERY_REGENERATION, AttributeRegister.EXCESS_LIFE_RECOVERY_REGENERATION);
```

**`INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD`** (id `inc_excess_life_recovery_regeneration_energyshield`)  StatGen:1737

```java
        inc(map, StatIds.INC_EXCESS_LIFE_RECOVERY_REGENERATION_ENERGYSHIELD, AttributeRegister.EXCESS_LIFE_RECOVERY_REGENERATION_1);
```

---

## 块 9

**合并原文**：`Fire Spells Conversion {X}% of Fire Damage to Chaos Damage Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage`

### 新常量（3，有序）

1. `FIRE_SPELLS_CONVERSION_X_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE` — Fire Spells Conversion {X}% of Fire Damage to Chaos Damage
2. `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE` — Chaos Damage from Fire Spells Contributes to Flammability and Ignite Buff Magnitude
3. `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE` — Ignite inflicted with Fire Spells deals Chaos Damage instead of Fire Damage

### 旧常量（3）及现有实现

**`BLACKFLAME_COVENANT_FIRE_TO_CHAOS`** (id `blackflame_covenant_fire_to_chaos`)  StatGen:3007

```java
        // blackflame_covenant：火焰「法术」伤 100% REPLACE 转混沌（仅法术来源，POE2「Fire Spells Convert 100% of Fire Damage to Chaos」）
        map.put(StatIds.BLACKFLAME_COVENANT_FIRE_TO_CHAOS,
                DamageConvertStatEffect.entry(Set.of(StatTag.FIRE), StatTag.CHAOS,
                        DamageConvertStatEffect.Mode.REPLACE, 1.0, false, TagExpr.of(Set.of(StatTag.SPELL))));
```

**`FIRE_SPELLS_CONVERT_DAMAGE`** (id `stat_fire_spells_convert_damage`)  StatGen:1519

```java
        inc(map, StatIds.FIRE_SPELLS_CONVERT_DAMAGE, AttributeRegister.ADDED_DAMAGE, StatTag.IGNITE);
```

**`FIRE_SPELLS_CONVERT_DAMAGE_SPELL`** (id `stat_fire_spells_convert_damage_spell`)  StatGen:1888

```java
        inc(map, StatIds.FIRE_SPELLS_CONVERT_DAMAGE_SPELL, AttributeRegister.ADDED_DAMAGE, StatTag.IGNITE);
```

---

## 块 10

**合并原文**：`Invocation Skills instead Trigger Spells every {X} seconds Invocation Skills cannot gain Energy while Triggering Spells Invoked Spells consume {X}% more Energy`

### 新常量（3，有序）

1. `INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_X_SECONDS` — Invocation Skills instead Trigger Spells every {X} seconds
2. `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS` — Invocation Skills cannot gain Energy while Triggering Spells
3. `INVOKED_SPELLS_CONSUME_X_MORE_ENERGY` — Invoked Spells consume {X}% more Energy

### 旧常量（2）及现有实现

**`MORE_INVOCATION_TRIGGER_SPELLS_CANNOT`** (id `more_invocation_trigger_spells_cannot`)  StatGen:1536

```java
        more(map, StatIds.MORE_INVOCATION_TRIGGER_SPELLS_CANNOT, AttributeRegister.INVOCATION_TRIGGER_SPELLS_CANNOT);
```

**`MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL`** (id `more_invocation_trigger_spells_cannot_spell`)  StatGen:1897

```java
        more(map, StatIds.MORE_INVOCATION_TRIGGER_SPELLS_CANNOT_SPELL, AttributeRegister.INVOCATION_TRIGGER_SPELLS_CANNOT_1);
```

---

## 块 11

**合并原文**：`Max Health is {X} Immune to Chaos Damage and Bleeding`

### 新常量（2，有序）

1. `MAX_HEALTH_IS_X` — Max Health is {X}
2. `IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING` — Immune to Chaos Damage and Bleeding

### 旧常量（3）及现有实现

**`CHAOS_INOCULATION_IMMUNE_BLEED`** (id `chaos_inoculation_immune_bleed`)  StatGen:2976

```java
        // chaos_inoculation：免疫流血
        map.put(StatIds.CHAOS_INOCULATION_IMMUNE_BLEED,
                ImmuneStatEffect.entry(AilmentStatusTypes.BLEED));
```

**`CHAOS_INOCULATION_IMMUNE_CHAOS`** (id `chaos_inoculation_immune_chaos`)  StatGen:2979

```java
        // chaos_inoculation：免疫混沌伤害（POE2 "Immune to Chaos Damage"）——全量（命中+持续伤害 DoT）混沌伤按 ratio=1.0 归零，无作用域限制。
        // 复用 DamageConvertStatEffect.Mode.NULLIFY（与 Elemental Archon 的 archon_nullify_chaos 同源机制，但此处不限定法术作用域、且 hitsOnly=false 覆盖 DoT）。
        map.put(StatIds.CHAOS_INOCULATION_IMMUNE_CHAOS,
                DamageConvertStatEffect.entry(Set.of(StatTag.CHAOS), DamageConvertStatEffect.Mode.NULLIFY, 1.0, false, TagExpr.ALWAYS));
```

**`CHAOS_INOCULATION_MAX_HEALTH`** (id `chaos_inoculation_max_health`)  StatGen:2955

```java
        // 混沌 inoculation：最大生命 REPLACE=1（覆盖原值，对应 POE「生命替代」）
        map.put(StatIds.CHAOS_INOCULATION_MAX_HEALTH,
                new Stat(ExpressionCodec.of(1.0), List.of(), StatTargets.EMPTY,
                        new ModifierStatEffect(Attributes.MAX_HEALTH,
                                Either.left(AttributeModifier.Operation.ADD_VALUE),
                                ModifierStatEffect.Mode.REPLACE, TagExpr.ALWAYS)));
```

---

## 块 12

**合并原文**：`Stat Conversion {X}% of Max Energy Shield to Max Mana Mana Costs are Doubled`

### 新常量（2，有序）

1. `STAT_CONVERSION_X_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA` — Stat Conversion {X}% of Max Energy Shield to Max Mana
2. `MANA_COSTS_ARE_DOUBLED` — Mana Costs are Doubled

### 旧常量（3）及现有实现

**`CONVERT_ENERGY_SHIELD_MANA`** (id `stat_convert_energy_shield_mana`)  StatGen:1588

```java
        inc(map, StatIds.CONVERT_ENERGY_SHIELD_MANA, AttributeRegister.CONVERT_ENERGY_SHIELD_MANA);
```

**`CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION`** (id `stat_convert_energy_shield_mana_statconversion`)  StatGen:1926

```java
        inc(map, StatIds.CONVERT_ENERGY_SHIELD_MANA_STATCONVERSION, AttributeRegister.CONVERT_ENERGY_SHIELD_MANA_1);
```

**`ELDRITCH_BATTERY_ES_TO_MANA`** (id `eldritch_battery_es_to_mana`)  StatGen:2991

```java
        // eldritch_battery：把全部最大能量护盾转化为最大法力（POE2 "100% of max Energy Shield to max Mana"）。
        // MAX_MANA += 100% 的 max_energy（加法式转换，同源机制）。「魔力消耗翻倍」属消耗乘子，见 Stage 4 子系统 B（本条目暂不建模）。
        map.put(StatIds.ELDRITCH_BATTERY_ES_TO_MANA,
                ModifierStatEffect.entry(AttributeRegister.MAX_MANA,
                        new ExpressionCodec.Value(Either.right(
                                Conversions.convertPercent(Conversions.attributeRef("max_energy"), 100.0))),
                        Either.left(AttributeModifier.Operation.ADD_VALUE), Set.of(), List.of()));
```

---

## 块 13

**合并原文**：`You can apply an additional Curse Double Activation Delay of Curses`

### 新常量（2，有序）

1. `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` — You can apply an additional Curse
2. `DOUBLE_ACTIVATION_DELAY_OF_CURSES` — Double Activation Delay of Curses

### 旧常量（2）及现有实现

**`INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION`** (id `inc_additional_curse_double_activation`)  StatGen:1447

```java
        inc(map, StatIds.INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION, AttributeRegister.ADDITIONAL_CURSE_DOUBLE_ACTIVATION, StatTag.CURSE);
```

**`INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE`** (id `inc_additional_curse_double_activation_curse`)  StatGen:1845

```java
        inc(map, StatIds.INC_ADDITIONAL_CURSE_DOUBLE_ACTIVATION_CURSE, AttributeRegister.ADDITIONAL_CURSE_DOUBLE_ACTIVATION_1, StatTag.CURSE);
```

---

## 块 14

**合并原文**：`You have no Mana Skill Mana Costs Stat Conversion to Health Costs`

### 新常量（2，有序）

1. `YOU_HAVE_NO_MANA` — You have no Mana
2. `SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` — Skill Mana Costs Stat Conversion to Health Costs

### 旧常量（2）及现有实现

**`BLOOD_MAGIC_ENERGY_TO_LIFE`** (id `blood_magic_energy_to_life`)  StatGen:3022

```java
        map.put(StatIds.BLOOD_MAGIC_ENERGY_TO_LIFE,
                ResourceConvertStatEffect.entry(ResourceType.ENERGY, ResourceType.HEALTH, 1.0));
```

**`BLOOD_MAGIC_MANA_TO_LIFE`** (id `blood_magic_mana_to_life`)  StatGen:3015

```java
        // blood_magic：魔力/能量 100% 转换为生命成本
        map.put(StatIds.BLOOD_MAGIC_MANA_TO_LIFE,
                ResourceConvertStatEffect.entry(ResourceType.MANA, ResourceType.HEALTH, 1.0));
```

---

## 块 15

**合并原文**：`Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve {X} Spirit each`

### 新常量（3，有序）

1. `YOUR_TOTEM_LIMIT_IS_DOUBLED` — Your Totem Limit is doubled
2. `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS` — No Charges requirement for placing Totems
3. `TOTEMS_RESERVE_X_SPIRIT_EACH` — Totems reserve {X} Spirit each

### 旧常量（3）及现有实现

**`ADD_MAX_TOTEM`** (id `add_max_totem`)  StatGen:327

```java
        // limit 类（MAX_* 属性，ADD_VALUE）
        add(map, StatIds.ADD_MAX_TOTEM, AttributeRegister.MAX_TOTEM);
```

**`INC_TOTEM_LIMIT_IS_DOUBLED`** (id `inc_totem_limit_is_doubled`)  StatGen:1461

```java
        inc(map, StatIds.INC_TOTEM_LIMIT_IS_DOUBLED, AttributeRegister.TOTEM_LIMIT_IS_DOUBLED, StatTag.TOTEM, StatTag.CHARGE);
```

**`INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE`** (id `inc_totem_limit_is_doubled_charge`)  StatGen:1850

```java
        inc(map, StatIds.INC_TOTEM_LIMIT_IS_DOUBLED_CHARGE, AttributeRegister.TOTEM_LIMIT_IS_DOUBLED_1, StatTag.TOTEM, StatTag.CHARGE);
```

---

## 块 16

**合并原文**：`gain Power Charges instead of Frenzy Charges gain Frenzy Charges instead of Endurance Charges gain Endurance Charges instead of Power Charges`

### 新常量（3，有序）

1. `GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES` — gain Power Charges instead of Frenzy Charges
2. `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES` — gain Frenzy Charges instead of Endurance Charges
3. `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES` — gain Endurance Charges instead of Power Charges

### 旧常量（3）及现有实现

**`CHARGE_CONVERT_RESONANCE`** (id `charge_convert_resonance`)  StatGen:723

```java
        // ── 充能获得事件改写（charge_convert，2026-08-17 第六十五节）──
        // 事件标签挂 gain_frenzy / gain_power / 通用 gain_charge（ChargedState.fireChargeEvent 已同时 fire
        // 类型标签与通用标签两枚）；amount = 概率（0~1）。
        // 「得狂怒球改为得暴击球」（POE2 25520 resonance，keystone 必触发 1.0）。
        map.put(StatIds.CHARGE_CONVERT_RESONANCE,
                ChargeConvertStatEffect.convertEntry(ChargeType.FRENZY, ChargeType.POWER, 0.0, Set.of("gain_frenzy"))
                        .withAmount(ExpressionCodec.of(MathParser.parse("$value"))));
```

**`INC_POWER_CHARGES_FRENZY_ENDURANCE`** (id `inc_power_charges_frenzy_endurance`)  StatGen:1185

```java
        inc(map, StatIds.INC_POWER_CHARGES_FRENZY_ENDURANCE, AttributeRegister.POWER_CHARGES_FRENZY_ENDURANCE, StatTag.POWER, StatTag.FRENZY, StatTag.ENDURANCE);
```

**`INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE`** (id `inc_power_charges_frenzy_endurance_charge`)  StatGen:1748

```java
        inc(map, StatIds.INC_POWER_CHARGES_FRENZY_ENDURANCE_CHARGE, AttributeRegister.POWER_CHARGES_FRENZY_ENDURANCE_1, StatTag.POWER, StatTag.FRENZY, StatTag.ENDURANCE);
```

---

## 块 17

**合并原文**：`{X}% more Max Rage Regenerate {X} Rage per second per {X} Rage spent Recently No Rage effect`

### 新常量（3，有序）

1. `X_MORE_MAX_RAGE` — {X}% more Max Rage
2. `REGENERATE_X_RAGE_PER_SECOND_PER_X_RAGE_SPENT_RECENTLY` — Regenerate {X} Rage per second per {X} Rage spent Recently
3. `NO_RAGE_EFFECT` — No Rage effect

### 旧常量（3）及现有实现

**`MORE_MAX_RAGE`** (id `more_max_rage`)  StatGen:2268

```java
        // 「100% more Maximum Rage」（37484 Primal Hunger 关键石）：引擎 RageHelper.getMaxRage 裸读
        // MAX_RAGE（ADD_MULTIPLIED_BASE 倍率生效），节点传小数（1.0 = ×2）。
        // 2026-08-19 机制推进：原节点错接 ADD_MAX_RAGE 1.0（+1 怒气），语义不符。
        valueMod(map, StatIds.MORE_MAX_RAGE, AttributeRegister.MAX_RAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
```

**`MORE_RAGE_REGENERATE_SPENT_NO`** (id `more_rage_regenerate_spent_no`)  StatGen:1032

```java
        more(map, StatIds.MORE_RAGE_REGENERATE_SPENT_NO, AttributeRegister.RAGE_REGENERATE_SPENT_NO, StatTag.RAGE);
```

**`MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY`** (id `more_rage_regenerate_spent_no_recently`)  StatGen:1662

```java
        more(map, StatIds.MORE_RAGE_REGENERATE_SPENT_NO_RECENTLY, AttributeRegister.RAGE_REGENERATE_SPENT_NO_1, StatTag.RAGE);
```

---

## 块 18

**合并原文**：`{X}% more amount of Health Leeched Leech Health {X}% more quickly Cannot Recover Health other than from Health Leech Health Leech effects are not add when Reservation Health is Filled`

### 新常量（4，有序）

1. `X_MORE_AMOUNT_OF_HEALTH_LEECHED` — {X}% more amount of Health Leeched
2. `LEECH_HEALTH_X_MORE_QUICKLY` — Leech Health {X}% more quickly
3. `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH` — Cannot Recover Health other than from Health Leech
4. `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED` — Health Leech effects are not add when Reservation Health is Filled

### 旧常量（1）及现有实现

**`MORE_AMOUNT_LEECH`** (id `more_amount_leech`)  StatGen:2633

```java
        // 偷取量 more（vaal_pact「50% more amount of Life Leeched」：ADD_MULTIPLIED_TOTAL + $value/100）
        map.put(StatIds.MORE_AMOUNT_LEECH,
                ModifierStatEffect.entry(AttributeRegister.AMOUNT_LEECHED, ExpressionCodec.of(MathParser.parse("$value/100")),
                        Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        Set.of(StatTag.LEECHING, "amount"), List.of()));
```

---

## 块 19

**合并原文**：`{X}% of Damage Converted to Fire Damage Deal no Non-Fire Damage`

### 新常量（2，有序）

1. `X_OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE` — {X}% of Damage Converted to Fire Damage
2. `DEAL_NO_NON_FIRE_DAMAGE` — Deal no Non-Fire Damage

### 旧常量（4）及现有实现

**`AVATAR_OF_FIRE_CHAOS_TO_FIRE`** (id `avatar_of_fire_chaos_to_fire`)  StatGen:3005

```java
        map.put(StatIds.AVATAR_OF_FIRE_CHAOS_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.CHAOS, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_COLD_TO_FIRE`** (id `avatar_of_fire_cold_to_fire`)  StatGen:3003

```java
        map.put(StatIds.AVATAR_OF_FIRE_COLD_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.COLD, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_LIGHT_TO_FIRE`** (id `avatar_of_fire_light_to_fire`)  StatGen:3001

```java
        map.put(StatIds.AVATAR_OF_FIRE_LIGHT_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.LIGHTNING, StatTag.FIRE, 0.75));
```

**`AVATAR_OF_FIRE_PHYS_TO_FIRE`** (id `avatar_of_fire_phys_to_fire`)  StatGen:2998

```java
        // avatar_of_fire：物理/闪电/冰霜/混沌 各 75% REPLACE 转火
        map.put(StatIds.AVATAR_OF_FIRE_PHYS_TO_FIRE,
                DamageConvertStatEffect.entry(StatTag.PHYSICAL, StatTag.FIRE, 0.75));
```

---

## 块 20

**合并原文**：`{X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power Energy Shield does not ESRecharge You cannot Recover Energy Shield from Regeneration You cannot Recover Energy Shield to above Armor`

### 新常量（4，有序）

1. `X_OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER` — {X}% of Physical Damage prevented Recouped as Energy Shield per enemy Power
2. `ENERGY_SHIELD_DOES_NOT_ESRECHARGE` — Energy Shield does not ESRecharge
3. `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION` — You cannot Recover Energy Shield from Regeneration
4. `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR` — You cannot Recover Energy Shield to above Armor

### 旧常量（1）及现有实现

**`SCARRED_FAITH_PHYS_RECOUP_ENERGY`** (id `scarred_faith_phys_recoup_energy`)  StatGen:3011

```java
        // scarred_faith：物理伤害 5% 以能量护盾形式补给（POE2「5% of Physical Damage prevented Recouped as Energy Shield」；
        //   「prevented」基数为承伤近似，且「ES 不回充/不从再生恢复/不超过护甲」暂未建模）
        map.put(StatIds.SCARRED_FAITH_PHYS_RECOUP_ENERGY,
                RecoupStatEffect.entry(Set.of(StatTag.ENERGY), TagExpr.of(Set.of(StatTag.PHYSICAL)), 0.05));
```

---

