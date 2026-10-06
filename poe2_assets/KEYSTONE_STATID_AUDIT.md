# 基石天赋 StatId 全量审计（33 基石）

判据：`poe2_assets/poe2_tree_clean.json` 语料原文（按行拆）↔ `KeystoneNodes01.java` 实际接线。

## 一、架构能力判定：是没做，不是做不到

| 能力 | 状态 | 依据 |
|---|---|---|
| 多条件（作用域） | 已支持 | `StatEntry.conditions()` = `List<ContextNode>`，`satisfies()` 逐条求值任一为 0 即失败（`StatEntry.java:89,218-228`） |
| AND / OR / NOT + 嵌套 | 已支持 | `ExpressionCodec.Logical`；`Conditions.java:91-93` 已有 `and(...)` 复合条件 |
| 按伤害类型取作用域 | 已支持 | `DamageValue` 提供 `damage.chaos`、`damage.bleeding`、physical/fire/cold/light/total（`DamageValue.java:82-108`） |
| 一条 id 挂多个效果 | 已支持 | `StatGroup`「一个词缀 id → N 条 StatEntry 同时生效，共享 amount 与 conditions」（`StatGroup.java:24-25`） |

> 结论：「免疫混沌和流血伤害」可写成**一条** StatId + 作用域 `OR(damage.chaos, damage.bleeding)`；
> 现行实现把它拆成 `CHAOS_INOCULATION_IMMUNE_CHAOS` + `CHAOS_INOCULATION_IMMUNE_BLEED` 两条，属实现偷懒。

## 二、基石全量清单

| 节点 id | 基石名 | 语料行 | 接线 | 状态 | 当前 StatId |
|---|---|---|---|---|---|
| `45202` | Ancestral Bond | 3 | 3 | **OK** | `YOUR_TOTEM_LIMIT_IS_DOUBLED`, `NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS`, `TOTEMS_RESERVE_SPIRIT_EACH` |
| `18684` | Avatar of Fire | 2 | 3 | **拆** | `AVATAR_OF_FIRE`, `OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE`, `DEAL_NO_NON_FIRE_DAMAGE` |
| `42680` | Blackflame Covenant | 3 | 3 | **OK** | `BLACKFLAME_COVENANT_FIRE_TO_CHAOS`, `CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE`, `IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE` |
| `51749` | Blood Magic | 2 | 3 | **拆** | `YOU_HAVE_NO_MANA`, `BLOOD_MAGIC_MANA_TO_LIFE`, `BLOOD_MAGIC_ENERGY_TO_LIFE` |
| `56605` | Bulwark | 2 | 2 | **OK** | `DODGE_ROLL_CANNOT_AVOID_DAMAGE`, `TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING` |
| `56349` | Chaos Inoculation | 2 | 3 | **拆** | `CHAOS_INOCULATION_MAX_HEALTH`, `CHAOS_INOCULATION_IMMUNE_CHAOS`, `CHAOS_INOCULATION_IMMUNE_BLEED` |
| `33979` | Conduit | 1 | 2 | **拆** | `CHARGE_CONVERT_CONDUIT`, `IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD` |
| `9085` | Crimson Assault | 3 | 3 | **OK** | `BLEEDING_YOU_INFLICT_IS_AGGRAVATED`, `BASE_BLEEDING_DURATION_IS_SECOND`, `MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT` |
| `14226` | Dance with Death | 2 | 1 | **少** | `MORE_SKILL_SPEED_WHILE_OFF_HAND_IS_EMPTY_AND_YOU_HAVE_A_ONE_HANDED_MARTIAL_WEAPON_EQUIPPED_IN_YOUR_MAIN_HAND` |
| `57513` | Eldritch Battery | 2 | 2 | **OK** | `STAT_CONVERSION_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA`, `MANA_COSTS_ARE_DOUBLED` |
| `46742` | Elemental Equilibrium | 3 | 3 | **OK** | `CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE`, `CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING`, `CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD` |
| `33404` | Eternal Youth | 2 | 2 | **OK** | `HEALTH_HEALTH_RECHARGE_INSTEAD_OF_ENERGY_SHIELD`, `MORE_HEALTH_RECOVERY_FROM_FLASKS` |
| `32349` | Giant's Blood | 3 | 3 | **OK** | `YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND`, `ATTRIBUTE_REQUIREMENTS`, `INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED` |
| `19288` | Glancing Blows | 2 | 2 | **OK** | `GLANCING_BLOWS_EVADE_UNLUCKY`, `GLANCING_BLOWS_DEFLECT_LUCKY` |
| `34497` | Heartstopper | 2 | 2 | **OK** | `TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND`, `TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` |
| `64601` | Hollow Palm Technique | 5 | 2 | **少** | `CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY`, `UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS` |
| `28492` | Iron Reflexes | 1 | 1 | **OK** | `STAT_CONVERSION_ALL_EVASION_TO_ARMOR` |
| `61942` | Lord of the Wilds | 3 | 3 | **OK** | `YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN`, `MORE_SPIRIT`, `NON_MINION_SKILLS_HAVE_MORE_RESERVATION_EFFICIENCY` |
| `45918` | Mind Over Matter | 2 | 3 | **拆** | `ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH`, `MIND_OVER_MATTER_MANA_TAKE`, `MORE_MANA_RECOVERY_RATE` |
| `39935` | Necromantic Talisman | 1 | 1 | **OK** | `ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU` |
| `25100` | Oasis | 2 | 2 | **OK** | `CANNOT_USE_CHARMS`, `MORE_RECOVERY_FROM_FLASKS` |
| `55048` | Pain Attunement | 2 | 2 | **OK** | `MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH`, `MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_LOW_HEALTH` |
| `37484` | Primal Hunger | 3 | 3 | **OK** | `MORE_MAX_RAGE`, `REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY`, `NO_RAGE_EFFECT` |
| `44017` | Resolute Technique | 2 | 2 | **OK** | `ACCURACY_IS_DOUBLED`, `NEVER_DEAL_CRITICAL_HITS` |
| `25520` | Resonance | 3 | 4 | **拆** | `CHARGE_CONVERT_RESONANCE`, `GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES`, `GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES`, `GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES` |
| `11230` | Ritual Cadence | 3 | 3 | **OK** | `INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_SECONDS`, `INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS`, `INVOKED_SPELLS_CONSUME_MORE_ENERGY` |
| `49547` | Scarred Faith | 4 | 4 | **OK** | `SCARRED_FAITH_PHYS_RECOUP_ENERGY`, `ENERGY_SHIELD_DOES_NOT_RECHARGE`, `YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION`, `YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR` |
| `41861` | Trusted Kinship | 3 | 3 | **OK** | `YOU_CAN_HAVE_TWO_COMPANIONS_OF_DIFFERENT_TYPES`, `MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS`, `MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS` |
| `14540` | Unwavering Stance | 2 | 2 | **OK** | `CANNOT_BE_LIGHT_STUNNED`, `CANNOT_DODGE_ROLL_OR_SPRINT` |
| `33369` | Vaal Pact | 4 | 4 | **OK** | `HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED`, `MORE_AMOUNT_OF_HEALTH_LEECHED`, `HEALTH_LEECH_HEALTH_SLOWER`, `CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH` |
| `47759` | Whispers of Doom | 2 | 2 | **OK** | `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE`, `DOUBLE_ACTIVATION_DELAY_OF_CURSES` |
| `49363` | Wildsurge Incantation | 4 | 5 | **拆** | `STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE_COST_MORE_HAVE_MORE_DURATION`, `STORM_SPELLS_HAVE_50_PERCENT_LESS_COST`, `PLANT_SPELLS_HAVE_50_PERCENT_LESS_COST`, `STORM_SPELLS_HAVE_75_PERCENT_LESS_DURATION`, `PLANT_SPELLS_HAVE_75_PERCENT_LESS_DURATION` |
| `52` | Zealot's Oath | 2 | 2 | **OK** | `EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD`, `ENERGY_SHIELD_DOES_NOT_RECHARGE` |

合计：语料行 **82**，接线 **85**。

## 三、错误清单

### E1 · 节点名污染（15 条，StatId 含基石节点名）

| 污染前缀 | StatId 常量 |
|---|---|
| `blackflame_covenant` | `BLACKFLAME_COVENANT_FIRE_TO_CHAOS` |
| `blood_magic` | `BLOOD_MAGIC_ENERGY_TO_LIFE` |
| `blood_magic` | `BLOOD_MAGIC_MANA_TO_LIFE` |
| `chaos_inoculation` | `CHAOS_INOCULATION_IMMUNE_BLEED` |
| `chaos_inoculation` | `CHAOS_INOCULATION_IMMUNE_CHAOS` |
| `chaos_inoculation` | `CHAOS_INOCULATION_MAX_HEALTH` |
| `eldritch_battery` | `ELDRITCH_BATTERY_ES_TO_MANA` |
| `glancing_blows` | `GLANCING_BLOWS_DEFLECT_LUCKY` |
| `glancing_blows` | `GLANCING_BLOWS_EVADE_UNLucky` |
| `iron_reflexes` | `IRON_REFLEXES_EVASION_TO_ARMOR` |
| `mind_over_matter` | `MIND_OVER_MATTER_MANA_TAKE` |
| `resolute_technique` | `RESOLUTE_TECHNIQUE_ACCURACY` |
| `resolute_technique` | `RESOLUTE_TECHNIQUE_CRIT_CHANCE` |
| `scarred_faith` | `SCARRED_FAITH_PHYS_RECOUP_ENERGY` |
| `unwavering_stance` | `UNWAVERING_STANCE_IMMUNE_STUN` |

### E2 · 孤儿 id（19 条，不来自任何描述文本的转写）

`avatar_of_fire` · `blood_magic_mana_to_life` · `blood_magic_energy_to_life` · `attribute_requirements` ·
`mind_over_matter_mana_take` · `scarred_faith_phys_recoup_energy` · `storm_spells_have_50_percent_less_cost` ·
`plant_spells_have_50_percent_less_cost` · `storm_spells_have_75_percent_less_duration` ·
`plant_spells_have_75_percent_less_duration` · `blackflame_covenant_fire_to_chaos` · `glancing_blows_evade_unlucky` ·
`glancing_blows_deflect_lucky` · `charge_convert_conduit` · `charge_convert_resonance` · `chaos_inoculation_max_health` ·
`chaos_inoculation_immune_chaos` · `chaos_inoculation_immune_bleed` · `unarmed_quarterstaff_skill_damage_by_skill_level_...`

### E3 · 拆（一条语料被拆成多条 StatId，7 个基石）

| 基石 | 语料行 | 接线 | 多余/重复项 |
|---|---|---|---|
| Avatar of Fire | 2 | 3 | `AVATAR_OF_FIRE`（整节点 id，多余） |
| Blood Magic | 2 | 3 | `BLOOD_MAGIC_MANA_TO_LIFE` + `BLOOD_MAGIC_ENERGY_TO_LIFE` 同属一行语料 |
| Chaos Inoculation | 2 | 3 | `..._IMMUNE_CHAOS` + `..._IMMUNE_BLEED` 同属一行语料 |
| Conduit | 1 | 2 | `CHARGE_CONVERT_CONDUIT`（多余） |
| Mind Over Matter | 2 | 3 | `MIND_OVER_MATTER_MANA_TAKE`（与首条重复） |
| Resonance | 3 | 4 | `CHARGE_CONVERT_RESONANCE`（多余） |
| Wildsurge Incantation | 4 | 5 | `STORM_AND_PLANT_SPELLS_DEAL_MORE_...`（与 4 条手写项重复） |

### E4 · 子弹列表未拆（Hollow Palm / Wildsurge）

- `[64601] Hollow Palm Technique`：`•` 后 3 条独立 mod 被压成 1 个 101 字符巨型 id。
- `[49363] Wildsurge Incantation`：语料 1 条含 3 个 `•` 子项，`node_stat_map` 也未拆（产出 `..._cost_more_have_more_duration`）。

### E5 · less → more 语义反转（命名错，非玩法错）

语料里含 `less` 的 12 条全部被转写成 `_more_`，`_less_` 在转写结果中出现 **0** 次。
已核对：这些 id 的 value 写了**负值**（-0.3/-0.5/-0.2），所以运行时正确，**错只在 id 命名**。

| 语料 | 现 id | value |
|---|---|---|
| Take 30% less Damage from Hits while Dodge Rolling | `TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING` | -0.3 |
| 50% less Life Recovery from Flasks | `MORE_HEALTH_RECOVERY_FROM_FLASKS` | -0.5 |
| 50% less Spirit | `MORE_SPIRIT` | -0.5 |
| Non-Minion Skills have 50% less Reservation Efficiency | `NON_MINION_SKILLS_HAVE_MORE_RESERVATION_EFFICIENCY` | -0.5 |
| 50% less Mana Recovery Rate | `MORE_MANA_RECOVERY_RATE` | -0.5 |
| Invoked Spells consume 50% less Energy | `INVOKED_SPELLS_CONSUME_MORE_ENERGY` | -0.5 |
| 30% less Critical Damage Bonus when on Full Life | `MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH` | -0.3 |
| 20% less Reservation Efficiency of non-Companion Skills | `MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS` | -0.2 |
