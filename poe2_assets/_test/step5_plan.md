# 第 5 步 · 引用重构执行计划

- 旧 StatIds 条目：**1460**
- 新 StatIds 条目：**1166**
- 被任一消费文件引用的旧常量：**1395**（其中 StatGen 以外 **213**）

| 处置 | 数量 | 判据 | 动作 |
|---|---|---|---|
| A 机械改名 | 878 | 注释规范化后与新表逐字符相等 | 全局替换引用（其中 0 条新旧同名，零改动） |
| D1 一对多拆分 | 46 | 旧注释=多行块合并形，可切 >=2 片 | 逐块定序映射到多个新常量 |
| KEEP 保留 | 122 | 被 StatGen 以外的消费文件引用 | 手工通道预填，不随天赋树重建 |
| DROP 删除 | 414 | 仅 StatGen 引用或完全无引用 | 删常量 + 删 StatGen 实现 |
| **合计** | **1460** | | |

新表中尚无任何旧实现对应（引擎待实现）：**558**

### KEEP 来源构成

| 语料归因 | 数量 |
|---|---|
| D2 已剔除语料 | 4 |
| D2 已剔除语料（注：注释为多行合并巧合形） | 2 |
| D3 自有引擎 | 116 |

### KEEP 被哪些文件引用

| 消费文件 | 引用条数 |
|---|---|
| StatGen | 99 |
| AdditionType | 57 |
| StatusBehavior | 32 |
| CharmStatusTypes | 12 |
| FormStatusTypes | 12 |
| BuffStatusTypes | 10 |

### DROP 来源构成

| 语料归因 | 数量 |
|---|---|
| D2 已剔除语料 | 314 |
| D3 自有引擎 | 100 |

---

## KEEP 明细（保留，进手工通道）

| 旧常量 | 旧 id | 注释 | 归因 | 引用方 |
|---|---|---|---|---|
| `ACCURACY` | `accuracy` | Accuracy | D3 自有引擎 | StatGen, StatusBehavior |
| `ACTION_SPEED_ATTACK_SPEED` | `action_speed_attack_speed` | Action Speed Attack Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_BLOCK_BREAK_SPEED` | `action_speed_block_break_speed` | Action Speed Block Break Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_CAST_SPEED` | `action_speed_cast_speed` | Action Speed Cast Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_FLYING_SPEED` | `action_speed_flying_speed` | Action Speed Flying Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_JUMP_STRENGTH` | `action_speed_jump_strength` | Action Speed Jump Strength | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_MOVEMENT_SPEED` | `action_speed_movement_speed` | Action Speed Movement Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_SNEAKING_SPEED` | `action_speed_sneaking_speed` | Action Speed Sneaking Speed | D3 自有引擎 | StatusBehavior |
| `ACTION_SPEED_SUBMERGED_MINING_SPEED` | `action_speed_submerged_mining_speed` | Action Speed Submerged Mining Speed | D3 自有引擎 | StatusBehavior |
| `ADD_CAST_CRIT_DAMAGE` | `add_cast_crit_damage` | Add Cast Crit Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_CHAOS_DAMAGE` | `add_chaos_damage` | Add Chaos Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_COLD_DAMAGE` | `add_cold_damage` | Add Cold Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_CRIT_CHANCE` | `add_crit_chance` | 5% chance to Gain ArcaneSurgeDuration when you deal a Critical Hit | D3 自有引擎 | AdditionType, StatGen |
| `ADD_ELEMENTAL_DAMAGE` | `add_elemental_damage` | Add Elemental Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_ENERGY_RECHARGE` | `add_energy_recharge` | Add Energy Recharge | D3 自有引擎 | AdditionType, StatGen |
| `ADD_FIRE_DAMAGE` | `add_fire_damage` | Add Fire Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_FOUND_ITEM_NUMBER` | `add_found_item_number` | Add Found Item Number | D3 自有引擎 | AdditionType, StatGen |
| `ADD_FOUND_ITEM_RARITY` | `add_found_item_rarity` | Add Found Item Rarity | D3 自有引擎 | AdditionType, StatGen |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | `add_launch_additional_projectile_bolt` | Add Launch Additional Projectile Bolt | D3 自有引擎 | AdditionType, StatGen |
| `ADD_MAX_CHAOS_RESISTANCE` | `add_max_chaos_resistance` | +10% to Maximum Chaos Resistance | D2 已剔除语料 | AdditionType, StatGen |
| `ADD_MAX_HEALTH_REGENERATION` | `add_max_health_regeneration` | Add Max Health Regeneration | D3 自有引擎 | AdditionType, StatGen |
| `ADD_MAX_SPIRIT` | `add_max_spirit` | Add Max Spirit | D3 自有引擎 | AdditionType, StatGen |
| `ADD_PHYSICAL_DAMAGE` | `add_physical_damage` | Add Physical Damage | D3 自有引擎 | AdditionType, StatGen |
| `ADD_PROJECTILE_PIERCE` | `add_projectile_pierce` | Add Projectile Pierce | D3 自有引擎 | AdditionType, StatGen |
| `ADD_RECENTLY_CONSUMED_DAMAGE` | `add_recently_consumed_damage` | Add Recently Consumed Damage | D3 自有引擎 | BuffStatusTypes, StatGen |
| `ADD_STUN_THRESHOLD` | `add_stun_threshold` | Add Stun Threshold | D3 自有引擎 | AdditionType, StatGen |
| `ADRENALINE_ATTACK_SPEED` | `adrenaline_attack_speed` | Adrenaline Attack Speed | D3 自有引擎 | StatGen, StatusBehavior |
| `ADRENALINE_CAST_SPEED` | `adrenaline_cast_speed` | Adrenaline Cast Speed | D3 自有引擎 | StatGen, StatusBehavior |
| `ADRENALINE_DAMAGE` | `adrenaline_damage` | Adrenaline Damage | D3 自有引擎 | StatGen, StatusBehavior |
| `ADRENALINE_MOVEMENT_SPEED` | `adrenaline_movement_speed` | Adrenaline Movement Speed | D3 自有引擎 | StatGen, StatusBehavior |
| `ADRENALINE_TAKEN_PHYSICAL` | `adrenaline_taken_physical` | Adrenaline Taken Physical | D3 自有引擎 | StatGen, StatusBehavior |
| `AILMENT_MAGNITUDE_FREEZE` | `ailment_magnitude_freeze` | Ailment Magnitude Freeze | D3 自有引擎 | AdditionType, StatGen |
| `ARCANE_SURGE_CAST_SPEED` | `arcane_surge_cast_speed` | Arcane Surge Cast Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `ARCANE_SURGE_REGEN` | `arcane_surge_regen` | Arcane Surge Regen | D3 自有引擎 | BuffStatusTypes, StatGen |
| `ARCHON_FREEZE_BUILDUP` | `archon_freeze_buildup` | Archon Freeze Buildup | D3 自有引擎 | StatGen, StatusBehavior |
| `ARCHON_IGNITE_BUILDUP` | `archon_ignite_buildup` | Archon Ignite Buildup | D3 自有引擎 | StatGen, StatusBehavior |
| `ARCHON_NULLIFY_CHAOS` | `archon_nullify_chaos` | Archon Nullify Chaos | D3 自有引擎 | StatGen, StatusBehavior |
| `ARCHON_NULLIFY_PHYSICAL` | `archon_nullify_physical` | Archon Nullify Physical | D3 自有引擎 | StatGen, StatusBehavior |
| `ARCHON_SHOCK_MAGNITUDE` | `archon_shock_magnitude` | Archon Shock Magnitude | D3 自有引擎 | StatGen, StatusBehavior |
| `ARMOR_BREAK_FULLY` | `armor_break_fully` | Armor Break Fully | D3 自有引擎 | StatGen, StatusBehavior |
| `ARMOR_REDUCTION` | `armor_reduction` | Armor Reduction | D3 自有引擎 | StatGen, StatusBehavior |
| `ATTRIBUTE_REQUIREMENTS` | `attribute_requirements` | You can wield Two-Handed Axes, Maces and Swords in one hand Triple Attributes requirements of Martial Weapons Inherent Life granted by Strength is halved | D2 已剔除语料（注：注释为多行合并巧合形） | AdditionType, StatGen |
| `BANNER_ATTACK_SPEED` | `banner_attack_speed` | Banner Attack Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `BANNER_CAST_SPEED` | `banner_cast_speed` | Banner Cast Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `BANNER_MOVEMENT_SPEED` | `banner_movement_speed` | Banner Movement Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `CHANCE_CHILL` | `chance_chill` | Chance Chill | D3 自有引擎 | AdditionType, StatGen |
| `CHANCE_DODGE` | `chance_dodge` | Chance Dodge | D3 自有引擎 | AdditionType, StatGen |
| `CHANCE_FREEZE` | `chance_freeze` | Chance Freeze | D3 自有引擎 | AdditionType, StatGen |
| `CHANCE_IGNITE` | `chance_ignite` | Chance Ignite | D3 自有引擎 | AdditionType, StatGen |
| `CHARM_GOLDEN_RARITY` | `charm_golden_rarity` | Charm Golden Rarity | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_ANTIDOTE` | `charm_immune_antidote` | Charm Immune Antidote | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_DOUSING` | `charm_immune_dousing` | Charm Immune Dousing | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_GROUNDING` | `charm_immune_grounding` | Charm Immune Grounding | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_SILVER` | `charm_immune_silver` | Charm Immune Silver | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_STAUNCHING` | `charm_immune_staunching` | Charm Immune Staunching | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_STONE` | `charm_immune_stone` | Charm Immune Stone | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_IMMUNE_THAWING` | `charm_immune_thawing` | Charm Immune Thawing | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_RESIST_CHAOS` | `charm_resist_chaos` | Charm Resist Chaos | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_RESIST_COLD` | `charm_resist_cold` | Charm Resist Cold | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_RESIST_FIRE` | `charm_resist_fire` | Charm Resist Fire | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CHARM_RESIST_LIGHTNING` | `charm_resist_lightning` | Charm Resist Lightning | D3 自有引擎 | CharmStatusTypes, StatGen |
| `CONVERT_EXTRA_PHYS_TO_FIRE` | `convert_extra_phys_to_fire` | Convert Extra Phys To Fire | D3 自有引擎 | AdditionType, StatGen |
| `DAMAGE_DEALT_BEAR_FIRE` | `damage_dealt_bear_fire` | Damage Dealt Bear Fire | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_DEALT_BEAR_PHYSICAL` | `damage_dealt_bear_physical` | Damage Dealt Bear Physical | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_DEALT_ELEMENTAL_SPELL` | `damage_dealt_elemental_spell` | Damage Dealt Elemental Spell | D3 自有引擎 | StatGen, StatusBehavior |
| `DAMAGE_DEALT_WOLF_COLD` | `damage_dealt_wolf_cold` | Damage Dealt Wolf Cold | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_DEALT_WOLF_PHYSICAL` | `damage_dealt_wolf_physical` | Damage Dealt Wolf Physical | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_DEALT_WYVERN_CHAOS` | `damage_dealt_wyvern_chaos` | Damage Dealt Wyvern Chaos | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_DEALT_WYVERN_LIGHTNING` | `damage_dealt_wyvern_lightning` | Damage Dealt Wyvern Lightning | D3 自有引擎 | FormStatusTypes, StatGen |
| `DAMAGE_TAKEN` | `damage_taken` | Damage Taken | D3 自有引擎 | StatGen, StatusBehavior |
| `DAMAGE_TAKEN_CHAOS` | `damage_taken_chaos` | Damage Taken Chaos | D3 自有引擎 | StatGen, StatusBehavior |
| `DAMAGE_TAKEN_PHYSICAL` | `damage_taken_physical` | Damage Taken Physical | D3 自有引擎 | StatGen, StatusBehavior |
| `EVASION` | `evasion` | Evasion | D3 自有引擎 | StatGen, StatusBehavior |
| `EXTRA_FIRE_FROM_CHAOS` | `extra_fire_from_chaos` | Extra Fire From Chaos | D3 自有引擎 | AdditionType |
| `EXTRA_FIRE_FROM_COLD` | `extra_fire_from_cold` | Gain 25% of Cold Damage as Extra Fire Damage against Frozen Enemies | D3 自有引擎 | AdditionType |
| `EXTRA_LIGHTNING_FROM_CHAOS` | `extra_lightning_from_chaos` | Extra Lightning From Chaos | D3 自有引擎 | AdditionType |
| `EXTRA_PHYSICAL_FROM_CHAOS` | `extra_physical_from_chaos` | Extra Physical From Chaos | D3 自有引擎 | AdditionType |
| `EXTRA_PHYSICAL_FROM_COLD` | `extra_physical_from_cold` | Extra Physical From Cold | D3 自有引擎 | AdditionType |
| `EXTRA_PHYSICAL_FROM_FIRE` | `extra_physical_from_fire` | Extra Physical From Fire | D3 自有引擎 | AdditionType |
| `EXTRA_PHYSICAL_FROM_LIGHTNING` | `extra_physical_from_lightning` | Extra Physical From Lightning | D3 自有引擎 | AdditionType |
| `EXTRA_PHYSICAL_FROM_PHYSICAL` | `extra_physical_from_physical` | Extra Physical From Physical | D3 自有引擎 | AdditionType |
| `FORM_BEAR_ARMOR` | `form_bear_armor` | Form Bear Armor | D3 自有引擎 | FormStatusTypes, StatGen |
| `FORM_BEAR_ATTACK_SPEED` | `form_bear_attack_speed` | Form Bear Attack Speed | D3 自有引擎 | FormStatusTypes, StatGen |
| `FORM_WOLF_ATTACK_SPEED` | `form_wolf_attack_speed` | Form Wolf Attack Speed | D3 自有引擎 | FormStatusTypes, StatGen |
| `FORM_WOLF_MOVEMENT_SPEED` | `form_wolf_movement_speed` | Form Wolf Movement Speed | D3 自有引擎 | FormStatusTypes, StatGen |
| `FORM_WYVERN_CRIT_CHANCE` | `form_wyvern_crit_chance` | Form Wyvern Crit Chance | D3 自有引擎 | FormStatusTypes, StatGen |
| `FORM_WYVERN_CRIT_DAMAGE` | `form_wyvern_crit_damage` | Form Wyvern Crit Damage | D3 自有引擎 | FormStatusTypes, StatGen |
| `INC_AILMENT_DAMAGE_RATE_BLEED` | `inc_ailment_damage_rate_bleed` | Inc Ailment Damage Rate Bleed | D3 自有引擎 | AdditionType, StatGen |
| `INC_AILMENT_DAMAGE_RATE_POISON` | `inc_ailment_damage_rate_poison` | Inc Ailment Damage Rate Poison | D3 自有引擎 | AdditionType, StatGen |
| `INC_BANNER_DAMAGE` | `inc_banner_damage` | Inc Banner Damage | D3 自有引擎 | AdditionType |
| `INC_BUFF_EFFECT_TAKEN` | `inc_buff_effect_taken` | Inc Buff Effect Taken | D3 自有引擎 | AdditionType, StatGen |
| `INC_CHILL_DURATION_TAKEN` | `inc_chill_duration_taken` | Inc Chill Duration Taken | D3 自有引擎 | AdditionType, StatGen |
| `INC_COLD_DAMAGE_SPELL` | `inc_cold_damage_spell` | Inc Cold Damage Spell | D3 自有引擎 | AdditionType, StatGen |
| `INC_DAMAGE_BLOCKING` | `inc_damage_blocking` | Inc Damage Blocking | D3 自有引擎 | AdditionType, StatGen |
| `INC_DAMAGE_OVER_TIME` | `inc_damage_over_time` | Take 50% less Damage over Time if you've started taking Damage over Time in the past second Take 50% more Damage over Time if you haven't started taking Damage over Time in the past second | D2 已剔除语料（注：注释为多行合并巧合形） | AdditionType, StatGen |
| `INC_DAMAGE_RECENTLY_KILLED` | `inc_damage_recently_killed` | Inc Damage Recently Killed | D3 自有引擎 | AdditionType, StatGen |
| `INC_FIRE_DAMAGE_SPELL` | `inc_fire_damage_spell` | Inc Fire Damage Spell | D3 自有引擎 | AdditionType, StatGen |
| `INC_FOUND_QUANTITY` | `inc_found_quantity` | Inc Found Quantity | D3 自有引擎 | AdditionType, StatGen |
| `INC_FOUND_RARITY` | `inc_found_rarity` | Inc Found Rarity | D3 自有引擎 | AdditionType, StatGen |
| `INC_INFUSION_MAGNITUDE` | `inc_infusion_magnitude` | Inc Infusion Magnitude | D3 自有引擎 | AdditionType, BuffStatusTypes, StatGen |
| `INC_LIGHT_DAMAGE_SPELL` | `inc_light_damage_spell` | Inc Light Damage Spell | D3 自有引擎 | AdditionType, StatGen |
| `INC_META_DAMAGE` | `inc_meta_damage` | Inc Meta Damage | D3 自有引擎 | AdditionType |
| `INC_MINION_ELEMENTAL_RESIST` | `inc_minion_elemental_resist` | Inc Minion Elemental Resist | D3 自有引擎 | AdditionType, StatGen |
| `INC_MOVEMENT_SPEED_STATIONARY` | `inc_movement_speed_stationary` | Inc Movement Speed Stationary | D3 自有引擎 | AdditionType, StatGen |
| `INC_TAKEN_CRIT_DAMAGE` | `inc_taken_crit_damage` | Inc Taken Crit Damage | D3 自有引擎 | AdditionType, StatGen |
| `INC_VOLATILITY_GENERATION` | `inc_volatility_generation` | Inc Volatility Generation | D3 自有引擎 | AdditionType, StatGen |
| `LEECH_HEALTH` | `leech_health` | Leech Health | D3 自有引擎 | AdditionType, StatGen |
| `LEECH_MANA` | `leech_mana` | Leech Mana | D3 自有引擎 | AdditionType, StatGen |
| `LEECH_TRIGGER` | `leech_trigger` | Leech Trigger | D3 自有引擎 | AdditionType, StatGen |
| `ONSLAUGHT_ATTACK_SPEED` | `onslaught_attack_speed` | Onslaught Attack Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `ONSLAUGHT_CAST_SPEED` | `onslaught_cast_speed` | Onslaught Cast Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `ONSLAUGHT_MOVEMENT_SPEED` | `onslaught_movement_speed` | Onslaught Movement Speed | D3 自有引擎 | BuffStatusTypes, StatGen |
| `PENETRATE_CHAOS` | `penetrate_chaos` | Penetrate Chaos | D3 自有引擎 | AdditionType, StatGen |
| `PHYSICAL_TAKEN_AS_CHAOS` | `physical_taken_as_chaos` | 20% of Physical Damage taken as Chaos Damage | D2 已剔除语料 | AdditionType, StatGen |
| `PHYSICAL_TAKEN_AS_COLD` | `physical_taken_as_cold` | 5% of Physical Damage taken as Cold Damage | D2 已剔除语料 | AdditionType, StatGen |
| `PHYSICAL_TAKEN_AS_LIGHTNING` | `physical_taken_as_lightning` | 5% of Physical Damage taken as Lightning Damage | D2 已剔除语料 | AdditionType, StatGen |
| `TAKEN_CRIT` | `taken_crit` | Taken Crit | D3 自有引擎 | StatGen, StatusBehavior |
| `UNHOLY_MIGHT_CHAOS` | `unholy_might_chaos` | Unholy Might Chaos | D3 自有引擎 | StatusBehavior |
| `UNHOLY_MIGHT_COLD` | `unholy_might_cold` | Unholy Might Cold | D3 自有引擎 | StatusBehavior |
| `UNHOLY_MIGHT_FIRE` | `unholy_might_fire` | Unholy Might Fire | D3 自有引擎 | StatusBehavior |
| `UNHOLY_MIGHT_LIGHTNING` | `unholy_might_lightning` | Unholy Might Lightning | D3 自有引擎 | StatusBehavior |
| `UNHOLY_MIGHT_PHYSICAL` | `unholy_might_physical` | Unholy Might Physical | D3 自有引擎 | StatusBehavior |

---

## DROP 明细（删除）

| 旧常量 | 归因 | 引用方 |
|---|---|---|
| `ADD_ALL_ELEMENTAL_RESISTANCES_RED` | D3 自有引擎 | StatGen |
| `ADD_COLD_INFUSIONS` | D3 自有引擎 | StatGen |
| `ADD_COOLDOWN_USE` | D3 自有引擎 | StatGen |
| `ADD_DARKNESS_LEVEL` | D2 已剔除语料 | StatGen |
| `ADD_ELEMENTAL_RESISTANCE_MINION` | D3 自有引擎 | StatGen |
| `ADD_FIRE_INFUSIONS` | D3 自有引擎 | StatGen |
| `ADD_LIMIT_ELEMENTAL` | D2 已剔除语料 | StatGen |
| `ADD_MAX_BLOCK_CHANCE` | D3 自有引擎 | StatGen |
| `ADD_METRES_MELEE_STRIKE_RANGE_NO_CONDITION` | D3 自有引擎 | StatGen |
| `ADD_NUMBER_SUMMONED_TOTEMS` | D2 已剔除语料 | StatGen |
| `ADD_PHYSICAL_DAMAGE_REDUCTION` | D3 自有引擎 | StatGen |
| `ADD_PROJECTILE_RANDOM_FIRE` | D3 自有引擎 | StatGen |
| `ADD_QUALITY_ALL` | D2 已剔除语料 | StatGen |
| `ADD_RING_SLOT` | D2 已剔除语料 | StatGen |
| `ADD_SKILL_LEVEL` | D2 已剔除语料 | StatGen |
| `ADD_SKILL_SLOT` | D3 自有引擎 | StatGen |
| `ADD_SPIRIT` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_EMPTY_CHARM_SLOTS` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_EVASION_RATING_BODY` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_EVASION_RATING_BODY_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_ITEM_ENERGY_SHIELD_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `ADD_SPIRIT_LIFE` | D2 已剔除语料 | StatGen |
| `ALL_RAGE_SHAPESHIFTING_HUMAN` | D3 自有引擎 | StatGen |
| `ARMOUR_APPLIES_TO_CHAOS` | D2 已剔除语料 | StatGen |
| `BODY_ARMOUR_REGENERATE_LIFE` | D2 已剔除语料 | StatGen |
| `BODY_ARMOUR_UNAFFECTED_DAMAGING` | D2 已剔除语料 | StatGen |
| `CHANCE_AGGRAVATE_ON_HIT_VS_CURSED` | D3 自有引擎 | StatGen |
| `CHANCE_ALL_DAMAGE_CONTRIBUTES_SHOCK` | D3 自有引擎 | StatGen |
| `CHANCE_ATTACKS_EXCEED_ADDITIONAL_CRITICAL` | D3 自有引擎 | StatGen |
| `CHANCE_AVOID_AILMENT` | D3 自有引擎 | StatGen |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE` | D2 已剔除语料 | StatGen |
| `CHANCE_BASE_BLOCK_SHIELD_VALUE_EQUIPPED` | D2 已剔除语料 | StatGen |
| `CHANCE_BASE_CRITICAL_SPELLS_IS` | D2 已剔除语料 | StatGen |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED` | D2 已剔除语料 | StatGen |
| `CHANCE_BENEFITS_FRENZY_CHARGES_DOUBLED_CHARGE` | D2 已剔除语料 | StatGen |
| `CHANCE_BLOCK_IS` | D2 已剔除语料 | StatGen |
| `CHANCE_CASCADABLE_SPELLS_ECHO` | D2 已剔除语料 | StatGen |
| `CHANCE_CREATE_SHOCKED_GROUND_ON_SHOCK` | D2 已剔除语料 | StatGen |
| `CHANCE_CRITICAL_IS_UNLUCKY` | D2 已剔除语料 | StatGen |
| `CHANCE_CURSED_KILLED_OR_ALLIES` | D2 已剔除语料 | StatGen |
| `CHANCE_CURSED_KILLED_OR_ALLIES_CURSE` | D2 已剔除语料 | StatGen |
| `CHANCE_EVADE_IS` | D2 已剔除语料 | StatGen |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE` | D2 已剔除语料 | StatGen |
| `CHANCE_KILL_EXPLODE_DEALING_LIFE_PHYSICAL` | D2 已剔除语料 | StatGen |
| `CHANCE_REPEATABLE_SPELLS_REPEAT` | D2 已剔除语料 | StatGen |
| `CHANCE_SLAM_CAUSE_ADDITIONAL_AFTERSHOCK_SPELL` | D3 自有引擎 | StatGen |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS` | D2 已剔除语料 | StatGen |
| `CHANCE_SURPASSING_POWER_MOUNTAIN_TEACHINGS_PER_ENEMY` | D2 已剔除语料 | StatGen |
| `CHANCE_WHICH_CREATE_FISSURES_ADDITIONAL_FISSURE` | D3 自有引擎 | StatGen |
| `COLD_TAKEN_AS_FIRE` | D2 已剔除语料 | StatGen |
| `CONVERT_SKILL_MANA_TO_LIFE` | D3 自有引擎 | StatGen |
| `CREATE_CHILLED_GROUND_ON_FREEZE` | D2 已剔除语料 | StatGen |
| `CREATE_IGNITED_GROUND_ON_IGNITE` | D3 自有引擎 | StatGen |
| `DAMAGE_DEALT` | D3 自有引擎 | StatGen |
| `DAMAGE_TAKEN_COLD_ON_IGNITED` | D3 自有引擎 | StatGen |
| `DAMAGE_TAKEN_FIRE_ON_CHILLED` | D3 自有引擎 | StatGen |
| `DAMAGE_UNLUCKY` | D2 已剔除语料 | StatGen |
| `EVASION_RATING_ELEMENTAL_DAMAGE` | D2 已剔除语料 | StatGen |
| `EXPOSURE_COLD` | D3 自有引擎 | StatGen |
| `EXPOSURE_FIRE` | D3 自有引擎 | StatGen |
| `EXPOSURE_LIGHTNING` | D3 自有引擎 | StatGen |
| `EXTRA_RANDOM_ELEMENT_CYCLE` | D3 自有引擎 | StatGen |
| `EXTRA_RANDOM_ELEMENT_RANDOM` | D2 已剔除语料 | StatGen |
| `FLASKS_RECOVER_LIFE` | D2 已剔除语料 | StatGen |
| `GEM_QUALITY_SOCKETED_ADDITIONAL` | D2 已剔除语料 | StatGen |
| `GRANT_VOLATILITY_ON_AILMENT` | D3 自有引擎 | StatGen |
| `GRANT_VOLATILITY_ON_EXPLODE` | D3 自有引擎 | StatGen |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE` | D2 已剔除语料 | StatGen |
| `INC_ACCURACY_PENALTY_BASED_DISTANCE_BY_DISTANCE` | D2 已剔除语料 | StatGen |
| `INC_ADAPTATIONS_DURATION` | D3 自有引擎 | StatGen |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE` | D2 已剔除语料 | StatGen |
| `INC_ADAPT_HIGHEST_ELEMENTAL_DAMAGE_ADAPTATION` | D2 已剔除语料 | StatGen |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM` | D2 已剔除语料 | StatGen |
| `INC_ADDITIONAL_LIFE_EQUAL_ITEM_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_AILMENT_DAMAGE_RATE` | D3 自有引擎 | StatGen |
| `INC_ALLIES_IN_PRESENCE_ADDED` | D2 已剔除语料 | StatGen |
| `INC_ALLIES_IN_PRESENCE_ADDED_ATTACK` | D2 已剔除语料 | StatGen |
| `INC_ALLIES_IN_PRESENCE_REGENERATE_IN_PRESENCE` | D3 自有引擎 | StatGen |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING` | D2 已剔除语料 | StatGen |
| `INC_ALLOCATE_PASSIVE_SORCERESS_STARTING_SORCERESS` | D2 已剔除语料 | StatGen |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING` | D2 已剔除语料 | StatGen |
| `INC_ALLOCATE_PASSIVE_WARRIOR_STARTING_WARRIOR` | D2 已剔除语料 | StatGen |
| `INC_ALL_BONUSES_AMULET_MINIONS` | D3 自有引擎 | StatGen |
| `INC_ALL_DAMAGE_ALLIES_IN` | D3 自有引擎 | StatGen |
| `INC_ALL_DAMAGE_CONTRIBUTES_CHILL` | D3 自有引擎 | StatGen |
| `INC_ALL_FLAMES_CHAYULA_MANIFEST` | D3 自有引擎 | StatGen |
| `INC_ARCANE_SURGE` | D2 已剔除语料 | StatGen |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN` | D3 自有引擎 | StatGen |
| `INC_ARCANE_SURGE_SHAPESHIFT_HUMAN_ARCANESURGEDURATION` | D3 自有引擎 | StatGen |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL` | D2 已剔除语料 | StatGen |
| `INC_ATTACKS_WEAPONS_ADDED_PHYSICAL_ATTACK` | D2 已剔除语料 | StatGen |
| `INC_BANNERS_GLORY` | D2 已剔除语料 | StatGen |
| `INC_BASE_ENERGY_SHIELD_RECHARGE` | D2 已剔除语料 | StatGen |
| `INC_BASE_LIFE_COST_EQUAL` | D2 已剔除语料 | StatGen |
| `INC_BEAR_SPIRIT_GAINS_EMBRACE` | D3 自有引擎 | StatGen |
| `INC_BECOME_IGNITED_CRITICAL_TAKING` | D2 已剔除语料 | StatGen |
| `INC_BECOME_IGNITED_CRITICAL_TAKING_PER_SECOND` | D2 已剔除语料 | StatGen |
| `INC_BEING_DAMAGED_TAKE_DAMAGE` | D2 已剔除语料 | StatGen |
| `INC_BEING_DAMAGED_TAKE_DAMAGE_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES` | D2 已剔除语料 | StatGen |
| `INC_BENEFITS_BONDED_MODIFIERS_RUNES_SHAMANONLYMODS` | D2 已剔除语料 | StatGen |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS` | D2 已剔除语料 | StatGen |
| `INC_BLEEDING_INFLICT_CURSED_TARGETS_CURSE` | D2 已剔除语料 | StatGen |
| `INC_BONUSES_FOCUS` | D2 已剔除语料 | StatGen |
| `INC_BOW_ATTACK_SPEED` | D3 自有引擎 | StatGen |
| `INC_BOW_DAMAGE` | D3 自有引擎 | StatGen |
| `INC_BOW_PHYSICAL_DAMAGE` | D3 自有引擎 | StatGen |
| `INC_BREAK_ARMOUR_BELOW` | D2 已剔除语料 | StatGen |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE` | D2 已剔除语料 | StatGen |
| `INC_BREAK_ARMOUR_EQUAL_DAMAGE_ARMOURBREAK` | D2 已剔除语料 | StatGen |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE` | D2 已剔除语料 | StatGen |
| `INC_BREAK_CONCENTRATION_EQUAL_DAMAGE_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS` | D2 已剔除语料 | StatGen |
| `INC_BUILD_RETAIN_COMBO_REGARDLESS_WEAPONSETS` | D2 已剔除语料 | StatGen |
| `INC_CANNOT_SPIRIT` | D2 已剔除语料 | StatGen |
| `INC_CARRY_CHEST_WHICH_ADDS` | D2 已剔除语料 | StatGen |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER` | D2 已剔除语料 | StatGen |
| `INC_CENTRAL_PROJECTILE_OWL_FEATHER_EMPOWERED` | D2 已剔除语料 | StatGen |
| `INC_CHAOS_RESISTANCE_IS_DOUBLED` | D2 已剔除语料 | StatGen |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE` | D2 已剔除语料 | StatGen |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_CHARGES` | D2 已剔除语料 | StatGen |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_SURGE` | D2 已剔除语料 | StatGen |
| `INC_CHARGE_TRIGGER_ELEMENTAL_SURGE_V2` | D2 已剔除语料 | StatGen |
| `INC_CHILLS_SLOW_TARGETS_UP` | D2 已剔除语料 | StatGen |
| `INC_COLD_DAMAGE_ON_CHILLED` | D3 自有引擎 | StatGen |
| `INC_COLD_DAMAGE_TAKEN_FIRE` | D2 已剔除语料 | StatGen |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER` | D2 已剔除语料 | StatGen |
| `INC_COLLECTING_ELEMENTAL_INFUSION_ANOTHER_ELEMENTALINFUSION` | D2 已剔除语料 | StatGen |
| `INC_COMBO_ADDITIONAL` | D2 已剔除语料 | StatGen |
| `INC_COMBO_ALL_ATTACK` | D2 已剔除语料 | StatGen |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE` | D2 已剔除语料 | StatGen |
| `INC_COMPANIONS_ADDED_ATTACK_DAMAGE_COMPANION` | D2 已剔除语料 | StatGen |
| `INC_CONCENTRATION_EQUAL_LIFE` | D2 已剔除语料 | StatGen |
| `INC_CONCENTRATION_EQUAL_LIFE_MAX_LIFE` | D2 已剔除语料 | StatGen |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE` | D2 已剔除语料 | StatGen |
| `INC_CRITICAL_IGNORE_NON_NEGATIVE_RESISTANCES` | D2 已剔除语料 | StatGen |
| `INC_CRITICAL_WEAKNESS_MARK_THEM` | D2 已剔除语料 | StatGen |
| `INC_CRITICAL_WEAKNESS_MARK_THEM_CRITICALWEAKNESS` | D2 已剔除语料 | StatGen |
| `INC_CRIT_CHANCE_SPELL` | D3 自有引擎 | StatGen |
| `INC_CRIT_DAMAGE_SPELL` | D3 自有引擎 | StatGen |
| `INC_CRUSHING_BLOWS` | D2 已剔除语料 | StatGen |
| `INC_CULLING_STRIKE` | D2 已剔除语料 | StatGen |
| `INC_CURRENT_ENERGY_SHIELD_IS` | D2 已剔除语料 | StatGen |
| `INC_CURRENT_ENERGY_SHIELD_IS_ENERGYSHIELD` | D2 已剔除语料 | StatGen |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE` | D2 已剔除语料 | StatGen |
| `INC_CURRENT_ENERGY_SHIELD_RECHARGE_ESRECHARGE` | D2 已剔除语料 | StatGen |
| `INC_CURSES_INFLICT_INFINITE_DURATION` | D2 已剔除语料 | StatGen |
| `INC_DAMAGE_ALLIES_IN_PRESENCE` | D3 自有引擎 | StatGen |
| `INC_DAMAGE_CONTRIBUTES_CHILL_MAGNITUDE` | D3 自有引擎 | StatGen |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_ELEMENTALDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_DAMAGE_EXTRA_RANDOM_ELEMENT_RANDOM_ELEMENT` | D3 自有引擎 | StatGen |
| `INC_DAMAGE_IS_TAKEN_MANA_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_DAMAGE_IS_TAKEN_MANA_TAKEN_MANA` | D3 自有引擎 | StatGen |
| `INC_DARKNESS_RESERVATION_LASTS` | D2 已剔除语料 | StatGen |
| `INC_DECIMATING_STRIKE` | D2 已剔除语料 | StatGen |
| `INC_DEFEND_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_DEFLECTION_RATING_EQUAL_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_DEMONFLAME_NO` | D2 已剔除语料 | StatGen |
| `INC_DOUBLE_ADAPTATION_EFFECT` | D2 已剔除语料 | StatGen |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS` | D2 已剔除语料 | StatGen |
| `INC_DOUBLE_NUMBER_POISONS_TARGETS_POISONED` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_ARCHON_CAST_SPELL_WHILE_ON` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_ARCHON_EXPIRE_HIGH_WHILE_ON` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING_ELEMENTALDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN` | D2 已剔除语料 | StatGen |
| `INC_ELEMENTAL_DAMAGE_IS_TAKEN_ELEMENTALDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_EQUIP_FOCUS_WIELDING_STAFF` | D2 已剔除语料 | StatGen |
| `INC_ES_RECHARGE_START` | D3 自有引擎 | StatGen |
| `INC_EVASION_RATING_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_EVASION_RATING_EXTRA_AILMENT` | D2 已剔除语料 | StatGen |
| `INC_EVASION_RATING_EXTRA_AILMENT_AILMENTTHRESHOLD` | D2 已剔除语料 | StatGen |
| `INC_EVASION_RATING_HELMET_GLOVES` | D2 已剔除语料 | StatGen |
| `INC_EVASION_RATING_HELMET_GLOVES_HELMET` | D2 已剔除语料 | StatGen |
| `INC_FASTER_START_ENERGY_SHIELD_ARCHON` | D3 自有引擎 | StatGen |
| `INC_FASTER_START_ENERGY_SHIELD_FLAT` | D3 自有引擎 | StatGen |
| `INC_FASTER_START_ENERGY_SHIELD_NO_CONDITION` | D3 自有引擎 | StatGen |
| `INC_FASTER_START_ENERGY_SHIELD_UNCONDITIONAL` | D3 自有引擎 | StatGen |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING` | D2 已剔除语料 | StatGen |
| `INC_FEWER_LIGHTNING_SURGE_TRIGGERING_TRIGGER` | D2 已剔除语料 | StatGen |
| `INC_FIRE_DAMAGE_ON_IGNITED` | D3 自有引擎 | StatGen |
| `INC_FLAMES_CHAYULA_MANIFEST_BLUE` | D2 已剔除语料 | StatGen |
| `INC_FLAMES_CHAYULA_MANIFEST_PURPLE` | D2 已剔除语料 | StatGen |
| `INC_FLAMES_CHAYULA_MANIFEST_RED` | D2 已剔除语料 | StatGen |
| `INC_FREEZING_CREATE_CHILLED_GROUND` | D2 已剔除语料 | StatGen |
| `INC_FREEZING_CREATE_CHILLED_GROUND_FREEZE` | D2 已剔除语料 | StatGen |
| `INC_FROZEN_COLD_RESISTANCE` | D3 自有引擎 | StatGen |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_FULLY_BROKEN_ARMOUR_INFLICT_INFLICT` | D3 自有引擎 | StatGen |
| `INC_GLORY_GENERATION_GLOBAL` | D3 自有引擎 | StatGen |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES` | D3 自有引擎 | StatGen |
| `INC_HEAVY_STUN_BUILDUP_EMPTIES_RECENTLY` | D3 自有引擎 | StatGen |
| `INC_HINDER_DURATION` | D3 自有引擎 | StatGen |
| `INC_HITTING_LIFE_FLASK_IS` | D2 已剔除语料 | StatGen |
| `INC_HITTING_LIFE_FLASK_IS_WHILE_A` | D2 已剔除语料 | StatGen |
| `INC_IGNORE_ALL_MOVEMENT_PENALTIES` | D3 自有引擎 | StatGen |
| `INC_IGNORE_WARCRY_COOLDOWNS` | D2 已剔除语料 | StatGen |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION` | D2 已剔除语料 | StatGen |
| `INC_INCREASES_REDUCTIONS_MANA_REGENERATION_RAGE` | D2 已剔除语料 | StatGen |
| `INC_INEVITABLE_CRITICAL` | D2 已剔除语料 | StatGen |
| `INC_INFERNAL_FLAME_LOST_NONE` | D2 已剔除语料 | StatGen |
| `INC_INFERNAL_FLAME_LOST_NONE_IF_NONE` | D3 自有引擎 | StatGen |
| `INC_INHERENT_BONUSES_ATTRIBUTES_DOUBLED` | D2 已剔除语料 | StatGen |
| `INC_IN_PRESENCE_EXPOSURE` | D2 已剔除语料 | StatGen |
| `INC_IN_PRESENCE_SLOWED` | D2 已剔除语料 | StatGen |
| `INC_JEWELLERY_BONUSES` | D2 已剔除语料 | StatGen |
| `INC_JEWEL_BONUS_EFFECT` | D2 已剔除语料 | StatGen |
| `INC_LEECHES_RESPONSE_HEALTH` | D3 自有引擎 | StatGen |
| `INC_LEECHES_RESPONSE_MANA` | D3 自有引擎 | StatGen |
| `INC_LIFE_CANNOT_CHANGE_ENERGY` | D2 已剔除语料 | StatGen |
| `INC_LIFE_CANNOT_CHANGE_ENERGY_WHILE_YOU` | D2 已剔除语料 | StatGen |
| `INC_LIFE_FLASK_CHARGE_SPENT` | D2 已剔除语料 | StatGen |
| `INC_LIFE_FLASK_EFFECTS_REMOVED` | D2 已剔除语料 | StatGen |
| `INC_LIFE_FLASK_EFFECTS_REMOVED_RESERVATION` | D2 已剔除语料 | StatGen |
| `INC_LIFE_LOSS_IS_PREVENTED` | D2 已剔除语料 | StatGen |
| `INC_LIFE_LOSS_IS_PREVENTED_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_LIGHTNING_DAMAGE_ON_SHOCKED` | D3 自有引擎 | StatGen |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE` | D2 已剔除语料 | StatGen |
| `INC_LIGHTNING_DAMAGE_TAKEN_FIRE_CONVERT_FIRE` | D2 已剔除语料 | StatGen |
| `INC_LOSE_ALL_INFERNAL_FLAME` | D2 已剔除语料 | StatGen |
| `INC_LOSE_ALL_TAILWIND` | D2 已剔除语料 | StatGen |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING` | D2 已剔除语料 | StatGen |
| `INC_LOSE_ELEMENTAL_ARCHON_REACHING_ELEMENTALARCHON` | D2 已剔除语料 | StatGen |
| `INC_LOSE_MANA` | D2 已剔除语料 | StatGen |
| `INC_LOW_MANA_ALLIES_IN` | D2 已剔除语料 | StatGen |
| `INC_LOW_MANA_ALLIES_IN_WHILE_YOU` | D2 已剔除语料 | StatGen |
| `INC_MANA_IS_REPLACED_TWICE` | D2 已剔除语料 | StatGen |
| `INC_MANA_IS_REPLACED_TWICE_BY_INFERNAL_FLAME` | D2 已剔除语料 | StatGen |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT` | D2 已剔除语料 | StatGen |
| `INC_MANA_TRIGGER_CHARMS_INSUFFICIENT_CHARM` | D2 已剔除语料 | StatGen |
| `INC_MAX_DARKNESS` | D2 已剔除语料 | StatGen |
| `INC_MAX_LIFE_REGEN` | D3 自有引擎 | StatGen |
| `INC_MINIONS_REVIVE_FASTER_ALL` | D3 自有引擎 | StatGen |
| `INC_MINION_CRIT_CHANCE` | D3 自有引擎 | StatGen |
| `INC_NON_DAMAGING_AILMENT_EFFECT_TAKEN` | D3 自有引擎 | StatGen |
| `INC_NON_UNIQUE_TIME_LOST` | D3 自有引擎 | StatGen |
| `INC_NON_UNIQUE_TIME_LOST_RARITY` | D3 自有引擎 | StatGen |
| `INC_NO_INHERENT_LOSS_RAGE` | D2 已剔除语料 | StatGen |
| `INC_NO_INHERENT_MANA_REGENERATION` | D2 已剔除语料 | StatGen |
| `INC_NO_SPIRIT_BASE_DARKNESS` | D2 已剔除语料 | StatGen |
| `INC_NO_SPIRIT_BASE_DARKNESS_ENERGYSHIELD` | D2 已剔除语料 | StatGen |
| `INC_ONLY_NORMAL_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_OWL_FEATHERS_FASTER` | D2 已剔除语料 | StatGen |
| `INC_PASSIVE_POINTS_BECOME_WEAPON` | D3 自有引擎 | StatGen |
| `INC_PASSIVE_POINTS_BECOME_WEAPON_WEAPONSETPASSIVESKILLPOINTS` | D3 自有引擎 | StatGen |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_REDUCTION_ARMOUR_EVASION` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_REDUCTION_IS` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_TAKEN_CHAOS_CONVERT_CHAOS` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_TAKEN_COLD` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING` | D2 已剔除语料 | StatGen |
| `INC_PHYSICAL_DAMAGE_TAKEN_LIGHTNING_CONVERT_LIGHTNING` | D2 已剔除语料 | StatGen |
| `INC_PINNED_CANNOT_PERFORM_ACTIONS` | D2 已剔除语料 | StatGen |
| `INC_PRIMAL_OWL_FEATHER_UP` | D2 已剔除语料 | StatGen |
| `INC_PRIMAL_OWL_FEATHER_UP_OWL_FEATHER` | D3 自有引擎 | StatGen |
| `INC_PROJECTILE_DAMAGE_BUILDS_PIN` | D2 已剔除语料 | StatGen |
| `INC_RAGE_COST` | D2 已剔除语料 | StatGen |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN` | D2 已剔除语料 | StatGen |
| `INC_REGAIN_CONCENTRATION_THEY_HAVEN_IF_NOT_LOST` | D2 已剔除语料 | StatGen |
| `INC_REGENERATE_LIFE_PER_SECOND` | D3 自有引擎 | StatGen |
| `INC_REGENERATE_MANA_EQUAL_LIFE` | D2 已剔除语料 | StatGen |
| `INC_REGENERATE_RAGE` | D2 已剔除语料 | StatGen |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE` | D2 已剔除语料 | StatGen |
| `INC_REMNANTS_CREATE_REAPPEAR_ONCE_REAPPEAR_ONCE` | D2 已剔除语料 | StatGen |
| `INC_RESERVES_LIFE` | D2 已剔除语料 | StatGen |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE` | D2 已剔除语料 | StatGen |
| `INC_REVEAL_WEAKNESSES_RARE_UNIQUE_REVEALWEAKNESS` | D2 已剔除语料 | StatGen |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT` | D2 已剔除语料 | StatGen |
| `INC_SACRIFICING_ENERGY_SHIELD_INTERRUPT_SACRIFICE` | D2 已剔除语料 | StatGen |
| `INC_SKILL_QUALITY` | D3 自有引擎 | StatGen |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED` | D2 已剔除语料 | StatGen |
| `INC_SLAM_IS_ANCESTRALLY_BOOSTED_ANCESTRALBOOST` | D2 已剔除语料 | StatGen |
| `INC_SLOW_MAGNITUDE` | D3 自有引擎 | StatGen |
| `INC_SMALL_PASSIVE_EFFECT` | D2 已剔除语料 | StatGen |
| `INC_SOCKET_NON_UNIQUE_BASIC` | D3 自有引擎 | StatGen |
| `INC_SOCKET_NON_UNIQUE_BASIC_ITEMRARITY` | D3 自有引擎 | StatGen |
| `INC_SORCERY_WARD_BARRIER_TAKE` | D2 已剔除语料 | StatGen |
| `INC_SORCERY_WARD_BARRIER_TAKE_HITDAMAGE` | D2 已剔除语料 | StatGen |
| `INC_SPEED_IS_UNAFFECTED_SLOWS` | D2 已剔除语料 | StatGen |
| `INC_SPELL_DAMAGE_LEECHED_LIFE` | D2 已剔除语料 | StatGen |
| `INC_STACK_JADE` | D2 已剔除语料 | StatGen |
| `INC_TAILWIND` | D2 已剔除语料 | StatGen |
| `INC_TAKE_DAMAGE_BLOCKED` | D2 已剔除语料 | StatGen |
| `INC_TAKE_LIFE_ENERGY_SHIELD` | D2 已剔除语料 | StatGen |
| `INC_TAKE_LIFE_ENERGY_SHIELD_FIRE` | D2 已剔除语料 | StatGen |
| `INC_TAKE_NO_DAMAGE_VOLATILITY` | D2 已剔除语料 | StatGen |
| `INC_TAME_BEAST_CAPTURE_UNIQUE` | D3 自有引擎 | StatGen |
| `INC_TAME_BEAST_CAPTURE_UNIQUE_RARITY` | D3 自有引擎 | StatGen |
| `INC_TARGETS_AFFECTED_TWO_CHILLS` | D2 已剔除语料 | StatGen |
| `INC_TARGETS_AFFECTED_TWO_CHILLS_CHILL` | D2 已剔除语料 | StatGen |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS` | D2 已剔除语料 | StatGen |
| `INC_TARGETS_AFFECTED_TWO_SHOCKS_SHOCK` | D2 已剔除语料 | StatGen |
| `INC_TARGETS_CURSED_LIFE_RESERVED` | D2 已剔除语料 | StatGen |
| `INC_TARGETS_CURSED_LIFE_RESERVED_CURSE` | D2 已剔除语料 | StatGen |
| `INC_TATTOO_RUNES_ONTO_BODY` | D3 自有引擎 | StatGen |
| `INC_TATTOO_RUNES_ONTO_BODY_BODY_ARMOUR` | D3 自有引擎 | StatGen |
| `INC_THERE_IS_NO_LIMIT` | D2 已剔除语料 | StatGen |
| `INC_THERE_IS_NO_LIMIT_BANNER` | D2 已剔除语料 | StatGen |
| `INC_THIRD_SLAM_DOESN_CREATE` | D2 已剔除语料 | StatGen |
| `INC_THIRD_SLAM_DOESN_CREATE_AFTERSHOCK` | D2 已剔除语料 | StatGen |
| `INC_TOTEMS_ALL_ELEMENTAL_RESISTANCES` | D3 自有引擎 | StatGen |
| `INC_TOTEMS_DIE_LIFE_IS` | D2 已剔除语料 | StatGen |
| `INC_TOTEMS_ONLY_FIRE_ATTACK` | D2 已剔除语料 | StatGen |
| `INC_TOTEMS_ONLY_FIRE_ATTACK_PROJECTILE` | D2 已剔除语料 | StatGen |
| `INC_TRIGGER_ANCESTRAL_SPIRITS_SUMMON` | D2 已剔除语料 | StatGen |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE` | D2 已剔除语料 | StatGen |
| `INC_TRIGGER_ELEMENTAL_EXPRESSION_MELEE_CRITICAL` | D2 已剔除语料 | StatGen |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL` | D2 已剔除语料 | StatGen |
| `INC_TRIGGER_ELEMENTAL_STORM_CRITICAL_SPELL` | D2 已剔除语料 | StatGen |
| `INC_TWO_HAND_DAMAGE` | D3 自有引擎 | StatGen |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED` | D3 自有引擎 | StatGen |
| `INC_UNIQUE_TAMED_BEASTS_POSSESSED_RARITY` | D3 自有引擎 | StatGen |
| `INC_VIVID_STAGS_LEAP_TOWARDS` | D2 已剔除语料 | StatGen |
| `INC_VIVID_WISP_METRES_MOVE` | D2 已剔除语料 | StatGen |
| `INC_VIVID_WISP_METRES_MOVE_ATTACK` | D2 已剔除语料 | StatGen |
| `INC_VIVID_WISP_STAMPEDE_ENDS` | D2 已剔除语料 | StatGen |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING` | D3 自有引擎 | StatGen |
| `INC_VOLATILITY_ALLIED_PERSISTENT_REVIVING_MINIONS` | D3 自有引擎 | StatGen |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT` | D2 已剔除语料 | StatGen |
| `INC_VOLATILITY_INFLICTING_ELEMENTAL_AILMENT_ELEMENTALAILMENTS` | D2 已剔除语料 | StatGen |
| `INC_WALK_PATHS_TAKEN` | D2 已剔除语料 | StatGen |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING` | D2 已剔除语料 | StatGen |
| `INC_WARCRIES_EXPLODE_CORPSES_DEALING_CORPSE` | D2 已剔除语料 | StatGen |
| `LEECHING_LIFE_CAUSES_COMPANION` | D3 自有引擎 | StatGen |
| `LEECHING_LIFE_CAUSES_COMPANION_LIFELEECH` | D3 自有引擎 | StatGen |
| `LEECH_RECOVERS_BASED_CHAOS` | D2 已剔除语料 | StatGen |
| `LIFE_FLASKS_RECOVER_MANA` | D2 已剔除语料 | StatGen |
| `LIFE_FLASKS_RECOVER_MANA_FLASK` | D2 已剔除语料 | StatGen |
| `LIFE_LEECH_RECOVERS_BASED` | D2 已剔除语料 | StatGen |
| `LIGHTNING_TAKEN_AS_FIRE` | D2 已剔除语料 | StatGen |
| `MODIFIERS_FIRE_RESISTANCE_COLD` | D2 已剔除语料 | StatGen |
| `MODIFIERS_FIRE_RESISTANCE_COLD_COLD_LIGHTNING` | D2 已剔除语料 | StatGen |
| `MODIFIERS_FIRE_RESISTANCE_COLD_MAXIMUMRESISTANCES` | D2 已剔除语料 | StatGen |
| `MORE_DODGING_EXPEND_UP_OWL` | D2 已剔除语料 | StatGen |
| `MORE_DODGING_EXPEND_UP_OWL_EMPOWERED` | D2 已剔除语料 | StatGen |
| `MORE_GLOVES_EQUIP_BASE_TYPE` | D2 已剔除语料 | StatGen |
| `MORE_GLOVES_EQUIP_BASE_TYPE_WHILE_EQUIPPED` | D2 已剔除语料 | StatGen |
| `MORE_PERSISTENT_BUFFS_RESERVATION` | D2 已剔除语料 | StatGen |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP` | D2 已剔除语料 | StatGen |
| `MORE_STAGS_SHOCK_MAGNITUDE_LEAP_PER_LEAP` | D2 已剔除语料 | StatGen |
| `NON_KEYSTONE_PASSIVE_IN` | D2 已剔除语料 | StatGen |
| `NON_KEYSTONE_PASSIVE_IN_MEDIUM_RADIUS` | D2 已剔除语料 | StatGen |
| `NO_LIFE_REGEN_WHILE_BLEEDING` | D3 自有引擎 | StatGen |
| `PASSIVE_POINT` | D2 已剔除语料 | StatGen |
| `PASSIVE_POINTS` | D2 已剔除语料 | StatGen |
| `PHYSICAL_TAKEN_AS_FIRE_BODY_ARMOUR` | D2 已剔除语料 | StatGen |
| `PRESENCE_EXPOSURE_AURA` | D3 自有引擎 | StatGen |
| `RECOVER_PCT_ENERGY` | D3 自有引擎 | StatGen |
| `REGEN_MAX_HEALTH_PCT` | D3 自有引擎 | StatGen |
| `SANDS_TIME` | D2 已剔除语料 | StatGen |
| `SPEED_PER_STACK_ATTACK_SPEED` | D3 自有引擎 | StatGen |
| `SPEED_PER_STACK_CAST_SPEED` | D3 自有引擎 | StatGen |
| `SPEED_PER_STACK_MOVEMENT_SPEED` | D3 自有引擎 | StatGen |
| `TOTEMS_PLACE_EMBANKMENT_AURAS` | D3 自有引擎 | StatGen |
| `TOTEM_DAMAGE` | D2 已剔除语料 | StatGen |
| `UNCRITTABLE_WHILE_PARRYING` | D3 自有引擎 | StatGen |
| `UNDERLINE_ACIDIC_CONCOCTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ALIGN_FATE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ANCESTRAL_SPIRITS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_APOCALYPSE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ARCHON_CHAYULA` | D2 已剔除语料 | StatGen |
| `UNDERLINE_BLEEDING_CONCOCTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_BLOOD_BOIL` | D2 已剔除语料 | StatGen |
| `UNDERLINE_BREACH` | D2 已剔除语料 | StatGen |
| `UNDERLINE_CALLED_SHOTS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_DEMON_FORM` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ELEMENTAL_EXPRESSION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ELEMENTAL_STORM` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ELEMENTAL_SURGE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_ENCASE_IN_JADE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_EXPLOSIVE_CONCOCTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_FIRE_SPELL` | D2 已剔除语料 | StatGen |
| `UNDERLINE_FULMINATING_CONCOCTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_HOLLOW_FOCUS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_HOLLOW_FORM` | D2 已剔除语料 | StatGen |
| `UNDERLINE_HOLLOW_RESONANCE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_INEVITABLE_AGONY` | D2 已剔除语料 | StatGen |
| `UNDERLINE_KELARI_DECEPTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_KELARI_JUDGMENT` | D2 已剔除语料 | StatGen |
| `UNDERLINE_KELARI_MALEDICTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_KELARI_TAINTED_SANDS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_LIFE_REMNANTS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_MANIFEST_WEAPON` | D2 已剔除语料 | StatGen |
| `UNDERLINE_MEDITATE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_MIRAGE_DEADEYE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_MOMENT_VULNERABILITY` | D2 已剔除语料 | StatGen |
| `UNDERLINE_NAVIRA_FRACTURING` | D2 已剔除语料 | StatGen |
| `UNDERLINE_NAVIRA_LAST_MIRAGE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_NAVIRA_OASIS` | D2 已剔除语料 | StatGen |
| `UNDERLINE_NAVIRA_WELL` | D2 已剔除语料 | StatGen |
| `UNDERLINE_PRIMAL_BOUNTY` | D2 已剔除语料 | StatGen |
| `UNDERLINE_RITUAL_SACRIFICE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_RUZHAN_BLAZING_SWORD` | D2 已剔除语料 | StatGen |
| `UNDERLINE_RUZHAN_FURY` | D2 已剔除语料 | StatGen |
| `UNDERLINE_RUZHAN_RECKONING` | D2 已剔除语料 | StatGen |
| `UNDERLINE_RUZHAN_TRAP` | D2 已剔除语料 | StatGen |
| `UNDERLINE_SHATTERING_CONCOCTION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_SORCERY_WARD` | D2 已剔除语料 | StatGen |
| `UNDERLINE_SUMMON_INFERNAL_HOUND` | D2 已剔除语料 | StatGen |
| `UNDERLINE_SUPPORTING_FIRE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_TEMPER_WEAPON` | D2 已剔除语料 | StatGen |
| `UNDERLINE_TEMPORAL_RIFT` | D2 已剔除语料 | StatGen |
| `UNDERLINE_TIME_FREEZE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_TIME_SNAP` | D2 已剔除语料 | StatGen |
| `UNDERLINE_UNBOUND_AVATAR` | D2 已剔除语料 | StatGen |
| `UNDERLINE_VIRTUOUS_BARRIER` | D2 已剔除语料 | StatGen |
| `UNDERLINE_VIVID_STAMPEDE` | D2 已剔除语料 | StatGen |
| `UNDERLINE_VOID_ILLUSION` | D2 已剔除语料 | StatGen |
| `UNDERLINE_WILD_PROTECTOR` | D2 已剔除语料 | StatGen |
| `UNRAVELLING` | D2 已剔除语料 | StatGen |
