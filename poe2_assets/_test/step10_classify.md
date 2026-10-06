# Stat Ids 分类 + StatGen 实现逐类验证（step10）

- StatIds 常量：**678**
- 生成 stat_entry JSON：**859**
- 有实现（含展开）：**678** / 无实现：**0**
- 触发规则的条目：**428**；违规项按严重度：P0=106, P1=4, P2=2, P3=358

## L1 分类：按 effect type

| effect type | 条目数 | 运行时通道 |
|---|---:|---|
| `attribute` | 523 | FOLD（StatEngine.fold 读属性时聚合） |
| `damage_convert` | 44 | DAMAGE_CONVERT（伤害类型重路由，减免前） |
| `heal` | 28 | FIRE（事件触发资源回复） |
| `immune` | 23 | IMMUNE（StatusEngine.apply 短路） |
| `chance_on_event` | 12 | FIRE（概率授予状态） |
| `grant_on_event` | 10 | FIRE（事件授予） |
| `damage_taken_convert` | 9 | DAMAGE_TAKEN_CONVERT（承伤重路由） |
| `recoup` | 6 | FIRE（承伤按比例补给） |
| `delivery` | 5 | DELIVERY（SupportProfile 投送参数） |
| `charge_convert` | 5 | FIRE（充能获得改写） |
| `lucky` | 4 | LUCKY（DamageHelper 双掷） |
| `resource_convert` | 3 | RESOURCE_CONVERT（扣费前重定向） |
| `hit_flag` | 2 | HIT_FLAG（DamageHelper 短路） |
| `requirement` | 1 | INERT（仅词缀生成器读） |
| `leech` | 1 | FIRE（伤害事件偷取） |
| `random_convert` | 1 | DAMAGE_CONVERT（随机元素目标） |
| `random_taken_convert` | 1 | DAMAGE_TAKEN_CONVERT（随机元素） |

## L2 分类：各 effect type 的亚型分布

### `attribute`

| 亚型 | 条目数 |
|---|---:|
| `add_multiplied_base/gain` | 334 |
| `add_value/gain` | 170 |
| `add_multiplied_total/gain` | 17 |
| `add_value/replace` | 1 |
| `set_value/gain` | 1 |

### `damage_convert`

| 亚型 | 条目数 |
|---|---:|
| `gain` | 36 |
| `replace` | 5 |
| `nullify` | 3 |

### `heal`

| 亚型 | 条目数 |
|---|---:|
| `health` | 16 |
| `mana` | 7 |
| `rage` | 4 |
| `energy` | 1 |

### `immune`

| 亚型 | 条目数 |
|---|---:|
| `bleed` | 3 |
| `ignite` | 2 |
| `maim` | 2 |
| `poison` | 2 |
| `shock` | 2 |
| `stun` | 2 |
| `blind` | 1 |
| `chill` | 1 |
| `corrupted_blood` | 1 |
| `exposure_lightning,exposure_cold,exposure_fire` | 1 |
| `chill,freeze` | 1 |
| `hinder` | 1 |
| `electrocute` | 1 |
| `pin,electrocute,chill,freeze` | 1 |
| `heavy_stun,stun` | 1 |
| `freeze` | 1 |

### `chance_on_event`

| 亚型 | 条目数 |
|---|---:|
| `aggravated` | 5 |
| `incision` | 1 |
| `blind` | 1 |
| `hinder` | 1 |
| `impale` | 1 |
| `daze` | 1 |
| `debilitate` | 1 |
| `onslaught` | 1 |

### `grant_on_event`

| 亚型 | 条目数 |
|---|---:|
| `?` | 10 |

### `damage_taken_convert`

| 亚型 | 条目数 |
|---|---:|
| `armor->?` | 5 |
| `chaos->?` | 1 |
| `cold->?` | 1 |
| `lightning->?` | 1 |
| `fire->?` | 1 |

### `recoup`

| 亚型 | 条目数 |
|---|---:|
| `health` | 4 |
| `energy` | 2 |

### `delivery`

| 亚型 | 条目数 |
|---|---:|
| `?` | 5 |

### `charge_convert`

| 亚型 | 条目数 |
|---|---:|
| `-` | 5 |

### `lucky`

| 亚型 | 条目数 |
|---|---:|
| `?` | 4 |

### `resource_convert`

| 亚型 | 条目数 |
|---|---:|
| `?` | 3 |

### `hit_flag`

| 亚型 | 条目数 |
|---|---:|
| `unevadable` | 1 |
| `uncrittable` | 1 |

### `requirement`

| 亚型 | 条目数 |
|---|---:|
| `-` | 1 |

### `leech`

| 亚型 | 条目数 |
|---|---:|
| `?` | 1 |

### `random_convert`

| 亚型 | 条目数 |
|---|---:|
| `random` | 1 |

### `random_taken_convert`

| 亚型 | 条目数 |
|---|---:|
| `-` | 1 |

## L3 分类：语义族

| 语义族 | 条目数 |
|---|---:|
| OTHER | 242 |
| INC_* | 150 |
| ADD_* | 60 |
| CHANCE_* | 50 |
| SKILL_SCOPED | 46 |
| AILMENT/STATUS | 40 |
| IMMUNE_* | 21 |
| *_RESISTANCE | 17 |
| RECOUP/LEECH/REGEN | 16 |
| EXTRA_*_FROM_* | 15 |
| *_PER_* | 13 |
| MORE_* | 5 |
| EXTRA_* | 2 |
| MAX_* | 1 |

## 属性基值 × 运算 交叉校验（StatEngine.fold 硬不变量）

> `out = original + add + baseValue * multBase; out *= (1 + multTotal)`  
> → **base == 0 时 `add_multiplied_base` 贡献恒为 0**，修饰静默失效。

| 属性基值 | operation | 条目数 | 判定 |
|---|---|---:|---|
| base=0 | `add_value` | 117 | ✅ 正确（零基唯一可行写法） |
| base=0 | `add_multiplied_total` | 12 | ⚠️ 仅当已有 add 贡献时才生效 |
| base≠0 | `add_multiplied_base` | 334 | ✅ 正确（`{X}% increased`） |
| base≠0 | `add_value` | 54 | ⚠️ 平面加值（需描述为 `+{X} to`） |
| base≠0 | `add_multiplied_total` | 5 | ✅ 正确（`{X}% more`） |
| base≠0 | `set_value` | 1 | — |

### ✅ A1 无命中：不存在零基属性上的 add_multiplied_base

## 死属性交叉校验（attribute 是否有运行时读取器）

> 判据：排除 `data/gen/**`、`AttributeRegister.java`、`StatIds.java` 后，全工程 `.java` 无该属性常量引用 → 写入的 AttributeModifier 无任何消费方。

- AttributeRegister 中无运行时读取器的属性总数：**111**
- 其中被 stat_entry 实际引用（A3 命中）：**99** 个属性 / **106** 条 Stat

| 死属性 | 条目数 | Id 列表 |
|---|---:|---|
| `coe:cooldown_recovery_rate` | 4 | `GRENADE_SKILLS_HAVE_COOLDOWN_USE`, `INCREASED_COOLDOWN_RECOVERY_RATE`, `INC_MINIONS_COOLDOWN_RECOVERY_RATE`, `INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND` |
| `coe:area_of_effect` | 3 | `INCREASED_AREA_OF_EFFECT_FOR_ATTACKS`, `MINIONS_HAVE_INCREASED_AREA_OF_EFFECT`, `STRIKES_DEAL_SPLASH_DAMAGE` |
| `coe:found_item_number` | 2 | `ADD_FOUND_ITEM_NUMBER`, `INC_FOUND_QUANTITY` |
| `coe:thorns` | 2 | `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED`, `INCREASED_THORNS_DAMAGE` |
| `coe:additional_curse` | 1 | `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` |
| `coe:additional_stun_threshold_equal_2` | 1 | `GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS` |
| `coe:archon_effect_taken` | 1 | `INCREASED_EFFECT_OF_ARCHON_BUFFS_ON_YOU` |
| `coe:archon_recovery_period_expires_1` | 1 | `ARCHON_RECOVERY_PERIOD_EXPIRES_SLOWER` |
| `coe:arrows_pierce_additional_target` | 1 | `ARROWS_PIERCE_AN_ADDITIONAL_TARGET` |
| `coe:attack_ten_incision` | 1 | `CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION` |
| `coe:attacks_critical` | 1 | `ATTACKS_HAVE_TO_CRITICAL_HIT_CHANCE` |
| `coe:attacks_maim_poisoned` | 1 | `CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES` |
| `coe:aura_buffs_affect_allies` | 1 | `YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES` |
| `coe:aura_magnitude` | 1 | `INC_AURA_MAGNITUDE` |
| `coe:blind_they_stun` | 1 | `BLIND_ENEMIES_WHEN_THEY_STUN_YOU` |
| `coe:bonuses_amulet_minions` | 1 | `ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU` |
| `coe:buff_effect_taken` | 1 | `INC_BUFF_EFFECT_TAKEN` |
| `coe:buffs_expire_slower` | 1 | `BUFFS_ON_YOU_EXPIRE_SLOWER` |
| `coe:cannot_blinded_full_life` | 1 | `CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH` |
| `coe:cannot_critically_parrying` | 1 | `CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING` |
| `coe:cost_efficiency` | 1 | `INCREASED_MANA_COST_EFFICIENCY` |
| `coe:critical_herald` | 1 | `TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS` |
| `coe:culling_strike_mark` | 1 | `CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK` |
| `coe:culling_strike_threshold` | 1 | `INCREASED_CULLING_STRIKE_THRESHOLD` |
| `coe:curse_effect_taken` | 1 | `INCREASED_BUFF_EFFECT_OF_CURSES_ON_YOU` |
| `coe:damage_lucky` | 1 | `CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY` |
| `coe:defend_armour` | 1 | `CHANCE_TO_DEFEND_WITH_OF_ARMOR` |
| `coe:defend_armour_critical` | 1 | `DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS` |
| `coe:elemental_ailment_application_shapeshifted` | 1 | `INCREASED_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` |
| `coe:elemental_resistances_red_green` | 1 | `TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED` |
| `coe:es_from_focus` | 1 | `INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS` |
| `coe:es_from_helmet` | 1 | `INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_HELMET` |
| `coe:faster_curse_activation` | 1 | `FASTER_CURSE_ACTIVATION` |
| `coe:faster_start_inherent_rage` | 1 | `FASTER_START_OF_INHERENT_RAGE_LOSS` |
| `coe:glory_generation` | 1 | `INC_GLORY_GENERATION` |
| `coe:grenades_activate_time` | 1 | `GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME` |
| `coe:ignites_cause_reflected_back` | 1 | `IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU` |
| `coe:ignore_movement_penalties_armour` | 1 | `IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR` |
| `coe:incision_effect` | 1 | `INC_INCISION_EFFECT` |
| `coe:inflict_bleeding_cannot_regenerate` | 1 | `ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH` |
| `coe:invocated_energy` | 1 | `INVOCATED_SKILLS_HAVE_INCREASED_MAX_ENERGY` |
| `coe:leech_life_faster` | 1 | `HEALTH_LEECH_HEALTH_FASTER` |
| `coe:leech_life_slower` | 1 | `HEALTH_LEECH_HEALTH_SLOWER` |
| `coe:level_all_chaos` | 1 | `TO_LEVEL_OF_ALL_CHAOS_SKILLS` |
| `coe:level_all_cold` | 1 | `TO_LEVEL_OF_ALL_COLD_SKILLS` |
| `coe:life_converted_energy_shield` | 1 | `OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD` |
| `coe:life_dexterity` | 1 | `HEALTH_PER_DEXTERITY` |
| `coe:life_flasks_charges` | 1 | `HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND` |
| `coe:light_radius` | 1 | `INCREASED_LIGHT_RADIUS` |
| `coe:lightning_infusions` | 1 | `TO_MAX_LIGHTNING_INFUSIONS` |
| `coe:lose_combo_slower` | 1 | `SKILLS_GAIN_COMBO_SLOWER` |
| `coe:mace_slam_cause_additional` | 1 | `CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` |
| `coe:magnitude_impales_inflicted_spells` | 1 | `INCREASED_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS` |
| `coe:mana_flasks_charges` | 1 | `MANA_FLASKS_GAIN_CHARGES_PER_SECOND` |
| `coe:mark_blinded` | 1 | `ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED` |
| `coe:mark_cannot_critical` | 1 | `ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS` |
| `coe:max_banner` | 1 | `ADD_MAX_BANNER` |
| `coe:max_fissure` | 1 | `ADD_MAX_FISSURE` |
| `coe:max_minion` | 1 | `ADD_MAX_MINION` |
| `coe:max_orb` | 1 | `ADD_MAX_ORB` |
| `coe:max_totem` | 1 | `ADD_MAX_TOTEM` |
| `coe:meta_energy_gain` | 1 | `INC_META_ENERGY` |
| `coe:metres_dodge_roll_distance` | 1 | `METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED` |
| `coe:metres_melee_strike_range` | 1 | `METRES_TO_MELEE_STRIKE_RANGE` |
| `coe:metres_melee_strike_range_2` | 1 | `METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` |
| `coe:minions_critical` | 1 | `MINIONS_HAVE_INCREASED_CRITICAL_HIT_CHANCE` |
| `coe:minions_gigantic` | 1 | `YOUR_MINIONS_ARE_GIGANTIC` |
| `coe:minions_inflict_withered` | 1 | `MINIONS_HAVE_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE` |
| `coe:movement_penalty_reduction` | 1 | `INC_MOVEMENT_PENALTY_REDUCTION` |
| `coe:plants_immediately_overgrow` | 1 | `PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW` |
| `coe:quiver_bonuses` | 1 | `INC_QUIVER_BONUSES` |
| `coe:rage_life_flask` | 1 | `GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK` |
| `coe:rage_shapeshifting_human_form` | 1 | `CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED` |
| `coe:recover_all_life_kill` | 1 | `CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY` |
| `coe:reload_crossbow_immediate` | 1 | `CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE` |
| `coe:remove_curse_channelling` | 1 | `ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS` |
| `coe:remove_curse_mana_flask` | 1 | `ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK` |
| `coe:remove_elemental_infusions_but` | 1 | `SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM` |
| `coe:remove_ignite_warcry` | 1 | `ADD_IGNITE_WHEN_YOU_WARCRY` |
| `coe:shock_electrocuted` | 1 | `INCREASED_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES` |
| `coe:skill_cost_life` | 1 | `INCREASED_HEALTH_COST_OF_SKILLS` |
| `coe:skill_cost_mana` | 1 | `INCREASED_MANA_COST_OF_SKILLS` |
| `coe:stacks_puppet_master` | 1 | `MAX_STACKS_OF_PUPPET_MASTER` |
| `coe:stun_recovery` | 1 | `INCREASED_STUN_RECOVERY` |
| `coe:stun_threshold_strength` | 1 | `TO_STUN_THRESHOLD_PER_STRENGTH` |
| `coe:taken_crit_damage` | 1 | `INC_TAKEN_CRIT_DAMAGE` |
| `coe:thorns_critical` | 1 | `TO_THORNS_CRITICAL_HIT_CHANCE` |
| `coe:totem_placement_range` | 1 | `INCREASED_TOTEM_PLACEMENT_RANGE` |
| `coe:trap_throwing_speed` | 1 | `INC_TRAP_THROWING_SPEED` |
| `coe:unaffected_chill_during_dodge` | 1 | `UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL` |
| `coe:unaffected_chill_leeching_mana` | 1 | `UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA` |
| `coe:unaffected_chilled_ground` | 1 | `UNAFFECTED_BY_CHILLED_GROUND` |
| `coe:unaffected_elemental_weakness` | 1 | `UNAFFECTED_BY_ELEMENTAL_WEAKNESS` |
| `coe:unaffected_shocked_ground` | 1 | `UNAFFECTED_BY_SHOCKED_GROUND` |
| `coe:warcries_debilitate` | 1 | `WARCRIES_DEBILITATE_ENEMIES` |
| `coe:warcries_empower_additional_attack` | 1 | `WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK` |
| `coe:warcries_minimum_power` | 1 | `WARCRIES_HAVE_A_MINIMUM_OF_POWER` |
| `coe:which_create_fissures_additional` | 1 | `SKILLS_WHICH_CREATE_FISSURES_HAVE_A_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE` |
| `coe:withered_effect_taken` | 1 | `INCREASED_BUFF_EFFECT_OF_WITHERED_ON_YOU` |

> **定性区分**：A 类 = 该效果已有可用 effect 通道（immune / hit_flag / recoup / chance_on_event 等）却错用占位属性 → 属**错接**，应收口到活通道；B 类 = 属性归属正确、仅运行时消费逻辑未实现 → 属**功能缺口**，保留属性路线。

## 规则命中统计

| 规则 | 含义 | 命中数 |
|---|---|---:|
| `S1` | 描述 `{X}` ↔ amount `$value` 参数化一致 | 357 |
| `A3` | **死属性**：attribute 指向无运行时读取器的属性 → 修饰无消费方，恒不生效 | 106 |
| `R6` | `Immune to` 必须 immune | 2 |
| `R8` | Presence 应走 presence_aura | 2 |
| `R4` | `as Extra <T> Damage` 必须 damage_convert 且 mode=gain | 1 |
| `R5` | `Damage taken as` 必须 damage_taken_convert | 1 |
| `S3` | Id 名中的类别关键词应出现在 tags | 1 |

## 逐类别验证明细（仅列违规）

### `attribute` —— 523 条，违规 374 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `HEALTH_PER_DEXTERITY` | +{X} Health per {X} Dexterity | attribute coe:life_dexterity add_value | `A3`/P0 属性 coe:life_dexterity 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `MAX_STACKS_OF_PUPPET_MASTER` | +{X} Max stacks of Puppet Master | attribute coe:stacks_puppet_master add_value | `A3`/P0 属性 coe:stacks_puppet_master 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED` | +{X} metres to Dodge Roll distance while Surrounded | attribute coe:metres_dodge_roll_distance add_value | `A3`/P0 属性 coe:metres_dodge_roll_distance 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `METRES_TO_MELEE_STRIKE_RANGE` | +{X} metres to Melee Strike Range | attribute coe:metres_melee_strike_range add_value | `A3`/P0 属性 coe:metres_melee_strike_range 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS` | +{X} metres to Melee Strike Range if you've dealt a Projectile Attack Hit Damage in the past eight seconds | attribute coe:metres_melee_strike_range_2 add_value | `A3`/P0 属性 coe:metres_melee_strike_range_2 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_LEVEL_OF_ALL_CHAOS_SKILLS` | +{X} to Level of all Chaos Skills | attribute coe:level_all_chaos add_value | `A3`/P0 属性 coe:level_all_chaos 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_LEVEL_OF_ALL_COLD_SKILLS` | +{X} to Level of all Cold Skills | attribute coe:level_all_cold add_value | `A3`/P0 属性 coe:level_all_cold 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_STUN_THRESHOLD_PER_STRENGTH` | +{X} to Stun Threshold per Strength | attribute coe:stun_threshold_strength add_value | `A3`/P0 属性 coe:stun_threshold_strength 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_MAX_LIGHTNING_INFUSIONS` | +{X} to Max Lightning Infusions | attribute coe:lightning_infusions add_value | `A3`/P0 属性 coe:lightning_infusions 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS` | +{X}% to Critical Hit Chance of Herald Skills | attribute coe:critical_herald add_value | `A3`/P0 属性 coe:critical_herald 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_THORNS_CRITICAL_HIT_CHANCE` | +{X}% to Thorns Critical Hit Chance | attribute coe:thorns_critical add_value | `A3`/P0 属性 coe:thorns_critical 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED` | +{X}% to all Max Elemental Resistances if you have at least {X} Red, Green and Blue Support Gems Socketed | attribute coe:elemental_resistances_red_green add_value | `A3`/P0 属性 coe:elemental_resistances_red_green 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU` | All bonuses from Equipped Amulet apply to your Minions instead of you | attribute coe:bonuses_amulet_minions add_multiplied_base | `A3`/P0 属性 coe:bonuses_amulet_minions 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCHON_RECOVERY_PERIOD_EXPIRES_SLOWER` | Archon recovery period expires {X}% slower | attribute coe:archon_recovery_period_expires_1 add_multiplied_base | `A3`/P0 属性 coe:archon_recovery_period_expires_1 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ARROWS_PIERCE_AN_ADDITIONAL_TARGET` | Arrows Pierce an additional Target | attribute coe:arrows_pierce_additional_target add_multiplied_base | `A3`/P0 属性 coe:arrows_pierce_additional_target 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ATTACKS_HAVE_TO_CRITICAL_HIT_CHANCE` | Attacks have +{X}% to Critical Hit Chance | attribute coe:attacks_critical add_value | `A3`/P0 属性 coe:attacks_critical 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `BLIND_ENEMIES_WHEN_THEY_STUN_YOU` | Blind Enemies when they Stun you | attribute coe:blind_they_stun add_multiplied_base | `A3`/P0 属性 coe:blind_they_stun 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `BUFFS_ON_YOU_EXPIRE_SLOWER` | Buffs on you expire {X}% slower | attribute coe:buffs_expire_slower add_multiplied_base | `A3`/P0 属性 coe:buffs_expire_slower 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH` | Cannot be Blinded while on Full Health | attribute coe:cannot_blinded_full_life add_multiplied_base | `A3`/P0 属性 coe:cannot_blinded_full_life 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING` | Cannot be Critically Hit while Parrying | attribute coe:cannot_critically_parrying add_multiplied_base | `A3`/P0 属性 coe:cannot_critically_parrying 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `COMPANIONS_GAIN_YOUR_STRENGTH` | Companions gain your Strength | attribute coe:strength add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED` | Consume all Rage when Shapeshifting to Human form to recover {X}% of Max Health per Rage Consumed | attribute coe:rage_shapeshifting_human_form add_multiplied_base | `A3`/P0 属性 coe:rage_shapeshifting_human_form 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK` | Culling Strike against Enemies you Mark | attribute coe:culling_strike_mark add_multiplied_base | `A3`/P0 属性 coe:culling_strike_mark 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS` | Defend with {X}% of Armor against Critical Hits | attribute coe:defend_armour_critical add_multiplied_base | `A3`/P0 属性 coe:defend_armour_critical 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED` | Enemies near Enemies you Mark are Blinded | attribute coe:mark_blinded add_multiplied_base | `A3`/P0 属性 coe:mark_blinded 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS` | Enemies you Mark cannot deal Critical Hits | attribute coe:mark_cannot_critical add_multiplied_base | `A3`/P0 属性 coe:mark_cannot_critical 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH` | Enemies you inflict Bleeding on cannot Regenerate Health | attribute coe:inflict_bleeding_cannot_regenerate add_multiplied_base | `A3`/P0 属性 coe:inflict_bleeding_cannot_regenerate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `GRENADE_SKILLS_HAVE_COOLDOWN_USE` | Grenade Skills have +{X} Cooldown Use | attribute coe:cooldown_recovery_rate add_multiplied_base | `A3`/P0 属性 coe:cooldown_recovery_rate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME` | Grenades have {X}% chance to activate a second time | attribute coe:grenades_activate_time add_value | `A3`/P0 属性 coe:grenades_activate_time 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU` | Ignites you cause are reflected back to you | attribute coe:ignites_cause_reflected_back add_multiplied_base | `A3`/P0 属性 coe:ignites_cause_reflected_back 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR` | Ignore all Movement Penalties from Equip Armor | attribute coe:ignore_movement_penalties_armour add_multiplied_base | `A3`/P0 属性 coe:ignore_movement_penalties_armour 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INVOCATED_SKILLS_HAVE_INCREASED_MAX_ENERGY` | Invocated skills have {X}% increased Max Energy | attribute coe:invocated_energy add_multiplied_base | `A3`/P0 属性 coe:invocated_energy 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND` | Health Flasks gain {X} charges per Second | attribute coe:life_flasks_charges add_multiplied_base | `A3`/P0 属性 coe:life_flasks_charges 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `HEALTH_LEECH_HEALTH_FASTER` | Health Leech Health {X}% faster | attribute coe:leech_life_faster add_multiplied_base | `A3`/P0 属性 coe:leech_life_faster 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `HEALTH_LEECH_HEALTH_SLOWER` | Health Leech Health {X}% slower | attribute coe:leech_life_slower add_multiplied_base | `A3`/P0 属性 coe:leech_life_slower 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `MANA_FLASKS_GAIN_CHARGES_PER_SECOND` | Mana Flasks gain {X} charges per Second | attribute coe:mana_flasks_charges add_multiplied_base | `A3`/P0 属性 coe:mana_flasks_charges 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `MINIONS_HAVE_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE` | Minions have {X}% chance to inflict Withered on Hit Damage | attribute coe:minions_inflict_withered add_value | `A3`/P0 属性 coe:minions_inflict_withered 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `MINIONS_HAVE_INCREASED_AREA_OF_EFFECT` | Minions have {X}% increased Area of Effect | attribute coe:area_of_effect add_multiplied_base | `A3`/P0 属性 coe:area_of_effect 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `MINIONS_HAVE_INCREASED_CRITICAL_HIT_CHANCE` | Minions have {X}% increased Critical Hit Chance | attribute coe:minions_critical add_value | `A3`/P0 属性 coe:minions_critical 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW` | Plants have a {X}% chance to immediately Overgrow | attribute coe:plants_immediately_overgrow add_value | `A3`/P0 属性 coe:plants_immediately_overgrow 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `SKILLS_GAIN_COMBO_SLOWER` | Skills gain Combo {X}% slower | attribute coe:lose_combo_slower add_multiplied_base | `A3`/P0 属性 coe:lose_combo_slower 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM` | Skills have {X}% chance to not add Elemental Infusions but still count as consuming them | attribute coe:remove_elemental_infusions_but add_value | `A3`/P0 属性 coe:remove_elemental_infusions_but 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `SKILLS_WHICH_CREATE_FISSURES_HAVE_A_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE` | Skills which create Fissures have a {X}% chance to create an additional Fissure | attribute coe:which_create_fissures_additional add_value | `A3`/P0 属性 coe:which_create_fissures_additional 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `STRIKES_DEAL_SPLASH_DAMAGE` | Strikes deal Splash Damage | attribute coe:area_of_effect add_multiplied_base | `A3`/P0 属性 coe:area_of_effect 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL` | Unaffected by Chill during Dodge Roll | attribute coe:unaffected_chill_during_dodge add_multiplied_base | `A3`/P0 属性 coe:unaffected_chill_during_dodge 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA` | Unaffected by Chill while Mana Leech Mana | attribute coe:unaffected_chill_leeching_mana add_multiplied_base | `A3`/P0 属性 coe:unaffected_chill_leeching_mana 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `UNAFFECTED_BY_CHILLED_GROUND` | Unaffected by Chilled Ground | attribute coe:unaffected_chilled_ground add_multiplied_base | `A3`/P0 属性 coe:unaffected_chilled_ground 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `UNAFFECTED_BY_ELEMENTAL_WEAKNESS` | Unaffected by Elemental Weakness | attribute coe:unaffected_elemental_weakness add_multiplied_base | `A3`/P0 属性 coe:unaffected_elemental_weakness 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `UNAFFECTED_BY_SHOCKED_GROUND` | Unaffected by Shocked Ground | attribute coe:unaffected_shocked_ground add_multiplied_base | `A3`/P0 属性 coe:unaffected_shocked_ground 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `WARCRIES_DEBILITATE_ENEMIES` | Warcries Debilitate Enemies | attribute coe:warcries_debilitate add_multiplied_base | `A3`/P0 属性 coe:warcries_debilitate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK` | Warcries Empowered an additional Attack | attribute coe:warcries_empower_additional_attack add_multiplied_base | `A3`/P0 属性 coe:warcries_empower_additional_attack 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `WARCRIES_HAVE_A_MINIMUM_OF_POWER` | Warcries have a minimum of {X} Power | attribute coe:warcries_minimum_power add_multiplied_base | `A3`/P0 属性 coe:warcries_minimum_power 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `YOU_CAN_APPLY_AN_ADDITIONAL_CURSE` | You can apply an additional Curse | attribute coe:additional_curse add_multiplied_base | `A3`/P0 属性 coe:additional_curse 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES` | Your Aura Buffs do not affect Allies | attribute coe:aura_buffs_affect_allies add_multiplied_base | `A3`/P0 属性 coe:aura_buffs_affect_allies 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `YOUR_MINIONS_ARE_GIGANTIC` | Your Minions are Gigantic | attribute coe:minions_gigantic add_multiplied_base | `A3`/P0 属性 coe:minions_gigantic 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_IGNITE_WHEN_YOU_WARCRY` | add Ignite when you Warcry | attribute coe:remove_ignite_warcry add_multiplied_base | `A3`/P0 属性 coe:remove_ignite_warcry 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS` | add a Curse after Channelling for {X} seconds | attribute coe:remove_curse_channelling add_multiplied_base | `A3`/P0 属性 coe:remove_curse_channelling 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK` | add a Curse when you use a Mana Flask | attribute coe:remove_curse_mana_flask add_multiplied_base | `A3`/P0 属性 coe:remove_curse_mana_flask 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE` | gain Accuracy equal to your Intelligence | attribute coe:accuracy add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GAIN_ACCURACY_EQUAL_TO_YOUR_STRENGTH` | gain Accuracy equal to your Strength | attribute coe:accuracy add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED` | gain Physical Thorns damage equal to {X}% of Max Health while Shapeshifted | attribute coe:thorns add_multiplied_base | `A3`/P0 属性 coe:thorns 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS` | gain additional Stun Threshold equal to {X}% of Item Armor on Equipped Armor Items | attribute coe:additional_stun_threshold_equal_2 add_multiplied_base | `A3`/P0 属性 coe:additional_stun_threshold_equal_2 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK` | gain {X} Rage when you use a Health Flask | attribute coe:rage_life_flask add_multiplied_base | `A3`/P0 属性 coe:rage_life_flask 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION` | {X}% chance for Attack Hit Damage to apply ten Incision | attribute coe:attack_ten_incision add_value | `A3`/P0 属性 coe:attack_ten_incision 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES` | {X}% chance for Attacks to Maim on Hit Damage against Poisoned Enemies | attribute coe:attacks_maim_poisoned add_value | `A3`/P0 属性 coe:attacks_maim_poisoned 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY` | {X}% chance for Damage with Hit Damage to be Lucky | attribute coe:damage_lucky add_value | `A3`/P0 属性 coe:damage_lucky 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` | {X}% chance for Mace Slam Skills you use yourself to cause an additional Aftershock | attribute coe:mace_slam_cause_additional add_value | `A3`/P0 属性 coe:mace_slam_cause_additional 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_TO_DEFEND_WITH_OF_ARMOR` | {X}% chance to Defend with {X}% of Armor | attribute coe:defend_armour add_value | `A3`/P0 属性 coe:defend_armour 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY` | {X}% chance to Recover all Health when you Kill an Enemy | attribute coe:recover_all_life_kill add_value | `A3`/P0 属性 coe:recover_all_life_kill 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE` | {X}% chance when you Reload a Crossbow to be immediate | attribute coe:reload_crossbow_immediate add_value | `A3`/P0 属性 coe:reload_crossbow_immediate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `FASTER_CURSE_ACTIVATION` | {X}% faster Curse Activation | attribute coe:faster_curse_activation add_multiplied_base | `A3`/P0 属性 coe:faster_curse_activation 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `FASTER_START_OF_INHERENT_RAGE_LOSS` | {X}% faster start of inherent Rage loss | attribute coe:faster_start_inherent_rage add_multiplied_base | `A3`/P0 属性 coe:faster_start_inherent_rage 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_AREA_OF_EFFECT_FOR_ATTACKS` | {X}% increased Area of Effect for Attacks | attribute coe:area_of_effect add_multiplied_base | `A3`/P0 属性 coe:area_of_effect 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_BUFF_EFFECT_OF_CURSES_ON_YOU` | {X}% increased Buff Effect of Curses on you | attribute coe:curse_effect_taken add_multiplied_base | `A3`/P0 属性 coe:curse_effect_taken 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_BUFF_EFFECT_OF_WITHERED_ON_YOU` | {X}% increased Buff Effect of Withered on you | attribute coe:withered_effect_taken add_multiplied_base | `A3`/P0 属性 coe:withered_effect_taken 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS` | {X}% increased Buff Magnitude of Impales inflicted with Spells | attribute coe:magnitude_impales_inflicted_spells add_multiplied_base | `A3`/P0 属性 coe:magnitude_impales_inflicted_spells 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_COOLDOWN_RECOVERY_RATE` | {X}% increased Cooldown Recovery Rate | attribute coe:cooldown_recovery_rate add_multiplied_base | `A3`/P0 属性 coe:cooldown_recovery_rate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_CULLING_STRIKE_THRESHOLD` | {X}% increased Culling Strike Threshold | attribute coe:culling_strike_threshold add_multiplied_base | `A3`/P0 属性 coe:culling_strike_threshold 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` | {X}% increased Elemental Ailments Ailment Application if you have Shapeshifted to an Animal form Recently | attribute coe:elemental_ailment_application_shapeshifted add_multiplied_base | `A3`/P0 属性 coe:elemental_ailment_application_shapeshifted 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS` | {X}% increased Energy Shield from Equipped Focus | attribute coe:es_from_focus add_multiplied_base | `A3`/P0 属性 coe:es_from_focus 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_ENERGY_SHIELD_FROM_EQUIPPED_HELMET` | {X}% increased Energy Shield from Equipped Helmet | attribute coe:es_from_helmet add_multiplied_base | `A3`/P0 属性 coe:es_from_helmet 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_HEALTH_COST_OF_SKILLS` | {X}% increased Health Cost of Skills | attribute coe:skill_cost_life add_multiplied_base | `A3`/P0 属性 coe:skill_cost_life 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_LIGHT_RADIUS` | {X}% increased Light Radius | attribute coe:light_radius add_multiplied_base | `A3`/P0 属性 coe:light_radius 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_MANA_COST_EFFICIENCY` | {X}% increased Mana Cost Efficiency | attribute coe:cost_efficiency add_multiplied_base | `A3`/P0 属性 coe:cost_efficiency 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_MANA_COST_OF_SKILLS` | {X}% increased Mana Cost of Skills | attribute coe:skill_cost_mana add_multiplied_base | `A3`/P0 属性 coe:skill_cost_mana 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES` | {X}% increased Shock Chance against Electrocuted Enemies | attribute coe:shock_electrocuted add_value | `A3`/P0 属性 coe:shock_electrocuted 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_STUN_RECOVERY` | {X}% increased Stun Recovery | attribute coe:stun_recovery add_multiplied_base | `A3`/P0 属性 coe:stun_recovery 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_THORNS_DAMAGE` | {X}% increased Thorns damage | attribute coe:thorns add_multiplied_base | `A3`/P0 属性 coe:thorns 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_TOTEM_PLACEMENT_RANGE` | {X}% increased Totem Placement range | attribute coe:totem_placement_range add_multiplied_base | `A3`/P0 属性 coe:totem_placement_range 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `INCREASED_EFFECT_OF_ARCHON_BUFFS_ON_YOU` | {X}% increased effect of Archon Buffs on you | attribute coe:archon_effect_taken add_multiplied_base | `A3`/P0 属性 coe:archon_effect_taken 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD` | {X}% of Max Health Stat Conversion to Energy Shield | attribute coe:life_converted_energy_shield add_multiplied_base | `A3`/P0 属性 coe:life_converted_energy_shield 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效** |
| `ACCURACY` | ---- 子系统引用（AdditionType / StatusBehavior / *StatusTypes / SkillFactory）---- Accuracy | attribute coe:accuracy add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_ATTACK_SPEED` | Action Speed Attack Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_BLOCK_BREAK_SPEED` | Action Speed Block Break Speed | attribute minecraft:block_break_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_CAST_SPEED` | Action Speed Cast Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_FLYING_SPEED` | Action Speed Flying Speed | attribute minecraft:flying_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_JUMP_STRENGTH` | Action Speed Jump Strength | attribute minecraft:jump_strength add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_MOVEMENT_SPEED` | Action Speed Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_SNEAKING_SPEED` | Action Speed Sneaking Speed | attribute minecraft:sneaking_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ACTION_SPEED_SUBMERGED_MINING_SPEED` | Action Speed Submerged Mining Speed | attribute minecraft:submerged_mining_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_ACCURACY` | +30 to Accuracy Rating | attribute coe:accuracy add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_AILMENT_CHANCE_BLEED` | 5% chance to inflict Bleeding on HitDamage | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_AILMENT_CHANCE_MAIM` | Attacks have 10% chance to Maim on HitDamage | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_AILMENT_CHANCE_POISON` | 8% chance to Poison on HitDamage | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_ARMOUR` | +10 to Armour | attribute minecraft:armor add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_ATTACK_DAMAGE` | Adds 1 to 37 Lightning damage to Attacks | attribute minecraft:attack_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_BLOCK_CHANCE` | 4% increased Block chance | attribute coe:block_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CAST_CRIT_DAMAGE` | Add Cast Crit Damage | attribute coe:crit_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CHAOS_DAMAGE` | Add Chaos Damage | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CHAOS_RESISTANCE` | +3% to Chaos Resistance | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_COLD_DAMAGE` | Add Cold Damage | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_COLD_RESISTANCE` | +5% to Cold Resistance | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CRIT_CHANCE` | 5% chance to Gain ArcaneSurgeDuration when you deal a Critical Hit | attribute coe:crit_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CRIT_DAMAGE` | 25% increased Critical Damage Bonus for Attack Damage | attribute coe:crit_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_DEXTERITY` | +10 to Dexterity | attribute coe:dexterity add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_ELEMENTAL_DAMAGE` | Add Elemental Damage | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_ENERGY_RECHARGE` | Add Energy Recharge | attribute coe:energy_recharge add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_EVASION` | +8 to Evasion Rating | attribute coe:evasion add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_FIRE_DAMAGE` | Add Fire Damage | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_FIRE_RESISTANCE` | +3% to all ElementalDamage Resistances | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_FOUND_ITEM_NUMBER` | Add Found Item Number | attribute coe:found_item_number add_value | `A3`/P0 属性 coe:found_item_number 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_FOUND_ITEM_RARITY` | Add Found Item Rarity | attribute coe:found_item_rarity add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_HEALTH_REGENERATION_RATE` | 10% increased Life Regeneration rate | attribute coe:regeneration_rate add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_INTELLIGENCE` | +10 to Intelligence | attribute coe:intelligence add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT` | Add Launch Additional Projectile Bolt | attribute coe:launch_additional_projectile add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW` | +8% SurpassChance chance to fire an additional Projectile | attribute coe:launch_additional_projectile add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_LIGHTNING_RESISTANCE` | +5% to Lightning Resistance | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_LIGHT_DAMAGE` | Adds 1 to 7 Lightning damage to Attacks | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_CHAOS_RESISTANCE` | +10% to Maximum Chaos Resistance | attribute coe:max_resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_COLD_RESISTANCE` | +2% to Maximum Cold Resistance if you have at least 5 Blue Support Gems Socketed | attribute coe:max_resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_ENERGY_SHIELD` | Gain additional Ailment Threshold equal to 8% of maximum Energy Shield | attribute coe:max_energy_shield add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_FIRE_RESISTANCE` | +2% to Maximum Fire Resistance if you have at least 5 Red Support Gems Socketed | attribute coe:max_resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_FRENZY` | +1 to Maximum Frenzy Charges | attribute coe:max_frenzy_charges add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_HEALTH` | +20 to maximum Life | attribute minecraft:max_health add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_HEALTH_REGENERATION` | Add Max Health Regeneration | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_LIGHTNING_RESISTANCE` | +2% to Maximum Lightning Resistance if you have at least 5 Green Support Gems Socketed | attribute coe:max_resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_MANA` | +20 to maximum Mana | attribute coe:max_mana add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_POWER` | +1 to Maximum Power Charges | attribute coe:max_power_charges add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_SPIRIT` | Add Max Spirit | attribute coe:max_spirit add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_TOTEM` | Your Totem Limit is doubled No Charges requirement for placing Totems Totems reserve 75 Spirit each | attribute coe:max_totem add_value | `A3`/P0 属性 coe:max_totem 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_PHYSICAL_DAMAGE` | Add Physical Damage | attribute coe:added_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_PROJECTILE_PIERCE` | Add Projectile Pierce | attribute coe:projectile_pierce add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_RECENTLY_CONSUMED_DAMAGE` | Add Recently Consumed Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_STRENGTH` | +10 to Strength | attribute coe:strength add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_STUN_THRESHOLD` | Add Stun Threshold | attribute coe:ailment_threshold add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_FREEZE` | Ailment Magnitude Freeze | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCANE_SURGE_CAST_SPEED` | Arcane Surge Cast Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCANE_SURGE_REGEN` | Arcane Surge Regen | attribute coe:regeneration_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCHON_FREEZE_BUILDUP` | Archon Freeze Buildup | attribute coe:ailment_buildup add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCHON_IGNITE_BUILDUP` | Archon Ignite Buildup | attribute coe:ailment_buildup add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARCHON_SHOCK_MAGNITUDE` | Archon Shock Magnitude | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ARMOR_REDUCTION` | Armor Reduction | attribute minecraft:armor add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `BANNER_ATTACK_SPEED` | Banner Attack Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `BANNER_CAST_SPEED` | Banner Cast Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `BANNER_MOVEMENT_SPEED` | Banner Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_CHILL` | Chance Chill | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_DODGE` | Chance Dodge | attribute coe:dodge_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_FREEZE` | Chance Freeze | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_IGNITE` | Chance Ignite | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARM_GOLDEN_RARITY` | Charm Golden Rarity | attribute coe:found_item_rarity add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARM_RESIST_CHAOS` | Charm Resist Chaos | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARM_RESIST_COLD` | Charm Resist Cold | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARM_RESIST_FIRE` | Charm Resist Fire | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARM_RESIST_LIGHTNING` | Charm Resist Lightning | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_BEAR_FIRE` | Damage Dealt Bear Fire | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_BEAR_PHYSICAL` | Damage Dealt Bear Physical | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_ELEMENTAL_SPELL` | Damage Dealt Elemental Spell | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_WOLF_COLD` | Damage Dealt Wolf Cold | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_WOLF_PHYSICAL` | Damage Dealt Wolf Physical | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_WYVERN_CHAOS` | Damage Dealt Wyvern Chaos | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_DEALT_WYVERN_LIGHTNING` | Damage Dealt Wyvern Lightning | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_TAKEN` | Damage Taken | attribute coe:taken_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_TAKEN_CHAOS` | Damage Taken Chaos | attribute coe:taken_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DAMAGE_TAKEN_PHYSICAL` | Damage Taken Physical | attribute coe:taken_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `EVASION` | Evasion | attribute coe:evasion add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `FLASK_MANA_RECOVERY` | 10% increased Mana Recovery from Flasks | attribute coe:flask_recovery_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ACCURACY` | 8% increased Accuracy Rating | attribute coe:accuracy add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DAMAGE_RATE_BLEED` | Inc Ailment Damage Rate Bleed | attribute coe:ailment_damage_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DAMAGE_RATE_POISON` | Inc Ailment Damage Rate Poison | attribute coe:ailment_damage_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_MAGNITUDE` | 10% increased BuffMagnitude of Ailments you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ARMOUR` | 10% increased Armour | attribute minecraft:armor add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ARMOUR_ENERGY` | 10% increased Armour, Evasion and Energy Shield | attribute coe:max_energy_shield add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ARMOUR_EVASION` | 12% increased Armour and Evasion Rating | attribute minecraft:armor add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ATTACK_DAMAGE` | 10% increased Projectile Damage | attribute minecraft:attack_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ATTACK_DAMAGE_PHYSICAL` | 12% increased Attack Physical Damage | attribute minecraft:attack_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ATTACK_SPEED` | 3% increased Attack Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BANNER_DAMAGE` | Inc Banner Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BLEEDING_DURATION_TAKEN` | 40% reduced Duration of Bleeding on You | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BLOCK_CHANCE` | 5% increased Block chance | attribute coe:block_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BUFF_EFFECT_TAKEN` | Inc Buff Effect Taken | attribute coe:buff_effect_taken add_multiplied_base | `A3`/P0 属性 coe:buff_effect_taken 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BUFF_MAGNITUDE` | Aura Skills have 5% increased BuffMagnitude | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CHAOS_DAMAGE` | 7% increased Chaos Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CHILL_DURATION_TAKEN` | Inc Chill Duration Taken | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_COLD_DAMAGE` | 12% increased Cold Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_COLD_DAMAGE_SPELL` | Inc Cold Damage Spell | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CRIT_CHANCE` | 10% increased Critical Hit Chance | attribute coe:crit_chance add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CURSE_DAMAGE` | 30% increased Damage per Curse on you | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_BLOCKING` | Inc Damage Blocking | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_LOW_LIFE` | 5% increased Damage taken while on Low Life | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_OVER_TIME` | Take 50% less Damage over Time if you've started taking Damage over Time in the past second Take 50% more Dama | attribute coe:damage_over_time_multiplier add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_PARRYING` | Successfully Parrying a Melee HitDamage grants 40% increased Damage to your next Ranged Attack | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_PRESENCE_ALLIES` | Allies in your Presence deal 16% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_PRESENCE_ENEMIES` | Each Totem applies 2% increased Damage taken to Enemies in their Presence | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_RECENTLY_KILLED` | Inc Damage Recently Killed | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_SHAPESHIFTED` | 10% increased Damage while Shapeshifted | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DEXTERITY` | 3% increased Attributes | attribute coe:dexterity add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ELEMENTAL_AILMENT_THRESHOLD` | 15% increased Elemental Ailment Threshold | attribute coe:ailment_threshold add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ENERGY_REGENERATION_RATE` | 15% increased Energy Shield Recharge Rate while Shapeshifted | attribute coe:regeneration_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_EVASION` | 15% increased Evasion Rating | attribute coe:evasion add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FIRE_DAMAGE` | 12% increased Fire Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FIRE_DAMAGE_SPELL` | Inc Fire Damage Spell | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FOUND_QUANTITY` | Inc Found Quantity | attribute coe:found_item_number add_value | `A3`/P0 属性 coe:found_item_number 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FOUND_RARITY` | Inc Found Rarity | attribute coe:found_item_rarity add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FREEZE_DURATION_TAKEN` | 25% reduced Freeze Duration on you | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_GLORY_GENERATION` | 15% increased Glory generation | attribute coe:glory_generation add_multiplied_base | `A3`/P0 属性 coe:glory_generation 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_GRENADE_DAMAGE` | 12% increased Grenade Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_HAZARD_DAMAGE` | 16% increased Hazard Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_HERALD_DAMAGE` | Herald Skills deal 20% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_IGNITE_DURATION_TAKEN` | 25% reduced Ignite Duration on you | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INFUSION_DURATION` | 10% increased Elemental Infusion duration | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INFUSION_MAGNITUDE` | Inc Infusion Magnitude | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_LIGHT_DAMAGE` | 12% increased Lightning Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_LIGHT_DAMAGE_SPELL` | Inc Light Damage Spell | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MANA_REGENERATION_RATE` | 10% increased Mana Regeneration Rate | attribute coe:regeneration_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MARK_DAMAGE` | Enemies you Mark take 10% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MAX_ENERGY` | 12% increased maximum Energy Shield | attribute coe:max_energy_shield add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MAX_HEALTH` | Companions have 12% increased maximum Life | attribute minecraft:max_health add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MAX_VOLATILITY` | 15% increased Volatility Explosion delay | attribute coe:max_volatility add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_META_DAMAGE` | Inc Meta Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_ATTACK_SPEED` | Minions have 3% increased Attack Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_DAMAGE` | Minions deal 10% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_ELEMENTAL_RESIST` | Inc Minion Elemental Resist | attribute coe:resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_MAX_HEALTH` | Minions have 10% increased maximum Life | attribute minecraft:max_health add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_MOVEMENT_SPEED` | Minions have 10% increased Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MOVEMENT_SPEED` | 3% increased Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MOVEMENT_SPEED_STATIONARY` | Inc Movement Speed Stationary | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_NON_DAMAGING_AILMENT_MAGNITUDE` | 10% increased BuffMagnitude of Non-Damaging Ailments you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_POISON_DURATION_TAKEN` | 10% reduced Poison Duration on you | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_PROJECTILE_SPEED` | 8% increased Projectile Speed | attribute coe:projectile_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_RESERVATION_EFFICIENCY` | 6% increased Reservation Efficiency of Herald Skills | attribute coe:reservation_efficiency add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SHOCK_DURATION_TAKEN` | 25% reduced Shock duration on you | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_STRENGTH` | 3% increased Attributes | attribute coe:strength add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_STUN_BUILDUP` | 15% increased Stun Buildup | attribute coe:ailment_buildup add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_STUN_DURATION` | 15% increased Stun Buildup | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_STUN_THRESHOLD` | 12% increased Stun Threshold | attribute coe:ailment_threshold add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_TAKEN_CRIT_DAMAGE` | Inc Taken Crit Damage | attribute coe:taken_crit_damage add_multiplied_base | `A3`/P0 属性 coe:taken_crit_damage 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_TAKEN_DAMAGE` | 40% increased maximum Energy Shield | attribute coe:taken_damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_VOLATILITY_GENERATION` | Inc Volatility Generation | attribute coe:volatility_generation add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_WARCRY_DAMAGE` | 16% increased Damage with Warcries | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `LEECH_HEALTH` | Leech Health | attribute coe:leech add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `LEECH_MANA` | Leech Mana | attribute coe:leech add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MINION_ATTACK_SPEED` | Minions have 3% increased Attack and Cast Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ONSLAUGHT_ATTACK_SPEED` | Onslaught Attack Speed | attribute minecraft:attack_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ONSLAUGHT_CAST_SPEED` | Onslaught Cast Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ONSLAUGHT_MOVEMENT_SPEED` | Onslaught Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PENETRATE_CHAOS` | Penetrate Chaos | attribute coe:resistance_penetration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `TAKEN_CRIT` | Taken Crit | attribute coe:taken_crit_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_AILMENT_THRESHOLD` | ---- 手写关键石 / 引擎实现，待接线到 POE2 语料条目（TODO 第 6 步）---- Gain additional Ailment Threshold equal to 8% of maximum Ener | attribute coe:ailment_threshold add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_BANNER_LINGER` | Banner Buffs linger on you for 2 seconds after you leave the Area | attribute coe:banner_linger add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_CORPSE_RETAIN_CHANCE` | 5% chance to not destroy Corpses when Consuming Corpses | attribute coe:corpse_retain_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_EMPOWERED_ATTACKS` | Warcries have 15% chance to Empowered 3 additional Attacks | attribute coe:empowered_attacks add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_EXPOSURE` | Exposure you inflict lowers Resistances by an additional 5% | attribute coe:ailment_magnitude add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_BANNER` | +1 to maximum number of placed Banners | attribute coe:max_banner add_value | `A3`/P0 属性 coe:max_banner 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_ENDURANCE` | +1 to Maximum Endurance Charges | attribute coe:max_endurance_charges add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_FISSURE` | Fissure Skills have +1 to Limit | attribute coe:max_fissure add_value | `A3`/P0 属性 coe:max_fissure 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_MINION` | Temporary Minion Skills have +2 to Limit of Minions summoned | attribute coe:max_minion add_value | `A3`/P0 属性 coe:max_minion 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_ORB` | Orb Skills have +1 to Limit | attribute coe:max_orb add_value | `A3`/P0 属性 coe:max_orb 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_MAX_RESISTANCES` | +1% to Maximum Resistances of each ElementalDamage Damage Type you have been HitDamage with Recently | attribute coe:max_resistances add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `ADD_PROJECTILE_PIERCE_CHANCE` | 15% chance to Pierce an Enemy | attribute coe:projectile_pierce_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_BLEED` | 10% increased BuffMagnitude of Bleeding you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_CHILL` | 10% increased BuffMagnitude of Shock you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_IGNITE` | 15% increased Flammability BuffMagnitude | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_POISON` | 10% increased BuffMagnitude of Poison you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `AILMENT_MAGNITUDE_SHOCK` | 15% increased BuffMagnitude of Shock you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL` | Attacks gain increased Accuracy Rating equal to their Critical Hit Chance | attribute coe:accuracy add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL` | Attacks gain increased Accuracy Rating equal to their Critical Hit Chance | attribute coe:accuracy add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_FLASK_NO_CONSUME` | 10% chance for Flasks you use to not consume Charges | attribute coe:flask_no_consume_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHAOS_INOCULATION_MAX_HEALTH` | Maximum Life is 1 Immune to Chaos Damage and Bleeding | attribute minecraft:max_health add_value mode=replace | `R6`/P1 描述含 'Immune to' 但 effect=attribute（应为 immune）<br>`S3`/P3 Id 含 'chaos' 但 tags=[] 未含（attribute=minecraft:max_health） |
| `CONVERTS_ALL_EVASION_RATING` | StatConversion all Evasion Rating to Armour | attribute coe:evasion add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `DEC_RAGE_LOSS_RATE` | Inherent loss of Rage is 15% slower | attribute coe:rage_loss_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `FLASK_NO_CONSUME` | 10% chance for Flasks you use to not consume Charges | attribute coe:flask_no_consume_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRENADE_DAMAGE` | 12% increased Grenade Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `HAZARD_DAMAGE` | 16% increased Hazard Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `HERALD_DAMAGE` | Herald Skills deal 20% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_CHANCE` | 10% increased chance to inflict Ailments | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_CHANCE_VS_EXPOSED_COLD` | Damage Penetration 18% Cold Resistance | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_CHANCE_VS_EXPOSED_FIRE` | Damage Penetration 18% Cold Resistance | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING` | Damage Penetration 18% Cold Resistance | attribute coe:ailment_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DURATION` | 10% increased Skill Effect Duration | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DURATION_VS_EXPOSED_COLD` | Damage Penetration 18% Fire Resistance | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DURATION_VS_EXPOSED_FIRE` | Damage Penetration 18% Fire Resistance | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING` | Damage Penetration 18% Fire Resistance | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AMOUNT_LEECH_HEALTH` | 8% increased amount of Life LifeLeech | attribute coe:amount_leeched add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_ATTACK_ELEMENTAL_DAMAGE` | 12% increased ElementalDamage Damage with Attacks | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_AURA_MAGNITUDE` | Aura Skills have 5% increased BuffMagnitude | attribute coe:aura_magnitude add_multiplied_base | `A3`/P0 属性 coe:aura_magnitude 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BEAST_AILMENT_DURATION` | 10% increased Duration of Ailments on Beasts | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_BLEED_DURATION` | 3% increased Movement Speed | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CHARM_DURATION` | Temporary Minion Skills have +2 to Limit of Minions summoned | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CHARM_EFFECT` | Charms applied to you have 25% increased Effect | attribute coe:charm_effect add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_CROSSBOW_DAMAGE` | 15% increased Crossbow Reload Speed | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_ATTACK` | 10% increased Attack Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_EMPOWERED_ATTACKS` | 5% chance to inflict Bleeding on HitDamage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAMAGE_IS_TAKEN_MANA` | 4% of Damage is taken from Mana before Life | attribute coe:mana_take_of_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DAZE_DURATION` | 10% chance to Daze on HitDamage | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DEBUFF_EXPIRY` | Debuffs on you expire 10% faster | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DEFLECTION_RATING_EQUAL_EVASION` | Gain Deflection Rating equal to 8% of Evasion Rating | attribute coe:evasion add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_DEFLECTION_RATING_EQUAL_EVASION_DEFLECT` | Gain Deflection Rating equal to 10% of Evasion Rating | attribute coe:evasion add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FASTER_AILMENTS` | 6% increased chance to inflict Ailments | attribute coe:ailment_damage_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FASTER_START_ENERGY_SHIELD` | 4% faster start of Energy Shield Recharge | attribute coe:energy_recharge add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FASTER_START_ENERGY_SHIELD_FASTERESRECHARGESTART` | 10% faster start of Energy Shield Recharge | attribute coe:energy_recharge add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FLAMMABILITY_MAGNITUDE` | 30% increased Flammability BuffMagnitude | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FLASK_EFFECT_DURATION` | 20% chance for Flasks you use to not consume Charges | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FLASK_LIFE_RECOVERY` | 10% increased Life Recovery from Flasks | attribute coe:flask_recovery_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FLASK_MANA_RECOVERY` | 10% increased Mana Recovery from Flasks | attribute coe:flask_recovery_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_FREEZE_BUILDUP` | 15% increased Freeze Buildup | attribute coe:ailment_buildup add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_IGNITE_FASTER` | Ignites you inflict deal Damage 15% faster | attribute coe:ailment_damage_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_IGNITE_MAGNITUDE_TAKEN` | 15% reduced BuffMagnitude of Ignite on you | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INCISION_EFFECT` | 50% increased effect of Incision | attribute coe:incision_effect add_multiplied_base | `A3`/P0 属性 coe:incision_effect 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INCISION_MAGNITUDE` | 50% increased effect of Incision | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED` | Increases and Reductions to Projectile Speed also apply to Damage with Bows | attribute coe:projectile_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW` | Increases and Reductions to Projectile Speed also apply to Damage with Bows | attribute coe:projectile_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_JAGGED_GROUND_DURATION` | 20% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_LEECH_HEALTH` | 10% of Thorns Damage Leeched as Life | attribute coe:leech add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_LEECH_RATE` | 12% increased amount of Life LifeLeech | attribute coe:leech_rate add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MANA_TAKE` | 4% of Damage is taken from Mana before Life | attribute coe:mana_take_of_damage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MARK_EFFECT` | Mark Skills have 10% increased Use Speed | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MAX_RAGE` | Gain 1 Rage on Melee HitDamage | attribute coe:max_rage add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_META_ENERGY` | Meta Skills gain 8% increased Energy | attribute coe:meta_energy_gain add_multiplied_base | `A3`/P0 属性 coe:meta_energy_gain 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINIONS_ATTACK_CAST_SPEED` | Minions have 3% increased Attack and Cast Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINIONS_COOLDOWN_RECOVERY_RATE` | Minions have 8% increased Cooldown Recovery Rate for Command Skills | attribute coe:cooldown_recovery_rate add_multiplied_base | `A3`/P0 属性 coe:cooldown_recovery_rate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND` | Minions have 20% increased Cooldown Recovery Rate for Command Skills | attribute coe:cooldown_recovery_rate add_multiplied_base | `A3`/P0 属性 coe:cooldown_recovery_rate 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINIONS_REVIVE_FASTER` | Minions Reviving 5% faster | attribute coe:revive_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MINION_REVIVE` | Minions Reviving 5% faster | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_MOVEMENT_PENALTY_REDUCTION` | 20% increased Evasion Rating | attribute coe:movement_penalty_reduction add_value | `A3`/P0 属性 coe:movement_penalty_reduction 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_OFFERING_DURATION` | Offering Skills have 15% increased Buff effect | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_PHYSICAL_DAMAGE` | 10% increased Physical Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_PIN_BUILDUP` | 15% increased Pinned Buildup | attribute coe:ailment_buildup add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_POISON_MAGNITUDE` | 10% increased BuffMagnitude of Poison you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_PROJECTILE_DAMAGE` | 10% increased Projectile Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_PROJECTILE_PIERCE` | 15% chance to Pierce an Enemy | attribute coe:projectile_pierce_chance add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_QUIVER_BONUSES` | 6% increased bonuses gained from Equipped Quiver | attribute coe:quiver_bonuses add_multiplied_base | `A3`/P0 属性 coe:quiver_bonuses 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_RAGE_LOSS_GRACE` | Inherent Rage loss starts 1 second later | attribute coe:rage_loss_grace add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_AFFECTED_ANY` | Regenerate 1% of maximum Life per second while affected by any Damaging Ailment | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED` | Regenerate 1% of maximum Life per second while affected by any Damaging Ailment | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_LOW` | Regenerate 1.5% of maximum Life per second while on Low Life | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_LOW_WHILE_ON` | Regenerate 1.5% of maximum Life per second while on Low Life | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_SURROUNDED` | Regenerate 2.5% of maximum Life per second while Surrounded | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND` | Regenerate 2.5% of maximum Life per second while Surrounded | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_TOTEM` | Regenerate 1% of maximum Life per second while you have a Totem | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_TOTEM_WHILE_YOU` | Regenerate 1% of maximum Life per second while you have a Totem | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_VE_FLASK` | Regenerate 1% of maximum Life per Second if you've used a Life Flask in the past 10 seconds | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND` | Regenerate 1% of maximum Life per Second if you've used a Life Flask in the past 10 seconds | attribute coe:max_regeneration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_RELOAD_SPEED` | 15% increased Crossbow Reload Speed | attribute coe:cross_bow_reload_time add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SHOCK_MAGNITUDE` | 15% increased BuffMagnitude of Shock you inflict | attribute coe:ailment_magnitude add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SKILL_EFFECT_DURATION` | 10% increased Skill Effect Duration | attribute coe:ailment_duration add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SLOWER_START_ENERGY_SHIELD` | 20% slower start of Energy Shield Recharge | attribute coe:energy_recharge add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION` | 20% slower start of Energy Shield Recharge | attribute coe:energy_recharge add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_SPELL_DAMAGE` | 10% increased Attack Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_TRAP_DAMAGE` | 10% increased Trap Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_TRAP_THROWING_SPEED` | 6% increased Trap Throwing Speed | attribute coe:trap_throwing_speed add_multiplied_base | `A3`/P0 属性 coe:trap_throwing_speed 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INC_TRAP_THROW_SPEED` | 6% increased Trap Throwing Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MARK_USE_SPEED` | Mark Skills have 10% increased Use Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `META_ENERGY` | Meta Skills gain 15% increased Energy | attribute coe:meta_energy_max add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MINION_DAMAGE` | Minions deal 10% increased Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MINION_MOVEMENT_SPEED` | 3% increased Movement Speed | attribute minecraft:movement_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MORE_BLEEDING_MAGNITUDE_INFLICTED` | 10% increased BuffMagnitude of Bleeding you inflict | attribute coe:ailment_magnitude add_multiplied_total | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MORE_MAX_RAGE` | 100% more Maximum Rage Regenerate 1 Rage per second per 4 Rage spent Recently No Rage effect | attribute coe:max_rage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PENETRATE_COLD` | Damage Penetration 6% Cold Resistance | attribute coe:resistance_penetration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PENETRATE_FIRE` | Damage Penetration 6% Fire Resistance | attribute coe:resistance_penetration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PENETRATE_LIGHTNING` | Damage Penetration 6% Lightning Resistance | attribute coe:resistance_penetration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `SEAL_GAIN_SPEED` | Sealed Skills have 10% increased Seal gain frequency | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `TRAP_DAMAGE` | 10% increased Trap Damage | attribute coe:damage add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `WARCRY_SPEED` | 16% increased Warcry Speed | attribute coe:cast_speed add_multiplied_base | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `INCREASED_DURATION_OF_PUPPET_MASTER` | X% increased Duration of Puppet Master（PUPPET_MASTER_DURATION 属性，SkillEvent 消费）。 原误挂在 INCREASED_EFFECT_OF_PUPP | attribute coe:puppet_master_duration add_value | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `damage_convert` —— 44 条，违规 9 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `CONVERT_EXTRA_PHYS_TO_FIRE` | Convert Extra Phys To Fire | damage_convert ['physical']→fire gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHAOS_INOCULATION_IMMUNE_CHAOS` | Maximum Life is 1 Immune to Chaos Damage and Bleeding | damage_convert ['chaos']→ nullify | `R6`/P1 描述含 'Immune to' 但 effect=damage_convert（应为 immune） |
| `CONVERT_EXTRA_GENERIC_TO_COLD` | Gain 15% of Damage as Extra Cold Damage while on Chilled Ground | damage_convert ['fire', 'chaos', 'cold', 'lightning', 'physical']→cold gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CONVERT_EXTRA_GENERIC_TO_LIGHTNING` | Gain 15% of Damage as Extra Lightning Damage while on Shocked Ground | damage_convert ['fire', 'chaos', 'cold', 'lightning', 'physical']→lightning gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CONVERT_EXTRA_GENERIC_TO_PHYSICAL` | Gain 10% of Damage as Extra Physical Damage | damage_convert ['fire', 'chaos', 'cold', 'lightning', 'physical']→physical gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CONVERT_EXTRA_PHYS_TO_COLD` | 5% chance to Daze on HitDamage | damage_convert ['physical']→cold gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CONVERT_EXTRA_PHYS_TO_LIGHTNING` | 5% chance to Daze on HitDamage | damage_convert ['physical']→lightning gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `EXTRA_PHYSICAL` | Gain 10% of Damage as Extra Physical Damage | damage_convert ['chaos']→physical gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS` | Minions Gain 20% of Elemental Damage as Extra Chaos Damage | damage_convert ['fire', 'chaos', 'cold', 'lightning', 'physical']→chaos gain | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `heal` —— 28 条，违规 13 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `LIFE_PER_KILL` | Gain 3 Life per enemy killed | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RAGE_ON_HIT_TAKEN` | Gain 2 Rage when HitDamage by an Enemy | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RAGE_ON_IGNITE` | Gain 1 Rage when your HitDamage Ignites a target | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_HEAVY_STUN` | Recover 50% of maximum Life when you Heavy Stun a Rare or Unique Enemy | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN` | Recover 50% of maximum Life when you Heavy Stun a Rare or Unique Enemy | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_KILLING_POISONED` | Recover 2% of maximum Life on Killing a Poisoned Enemy | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_KILLING_POISONED_POISON` | Recover 2% of maximum Life on Killing a Poisoned Enemy | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_MINIONS_IS` | Recover 2% of maximum Life when one of your Minions is Reviving | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_LIFE_MINIONS_IS_MINION` | Recover 2% of maximum Life when one of your Minions is Reviving | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_PCT_HEALTH` | 15% increased Armour | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_PCT_HEALTH_ON_KILL` | +10 to Intelligence | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RECOVER_PCT_MANA_ON_KILL` | 15% increased Mana Regeneration Rate | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `RAGE_ON_HIT_TALENT` | ---- Step6 分流新增（原同名劫持/双注册拆出的独立 key）---- 天赋：命中 +Rage（$value，由节点注入）。与熊形 Maul 固定 +5 的 RAGE_ON_HIT 分流。 | heal | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `immune` —— 23 条，违规 0 条

✅ 全部通过规则校验。

### `chance_on_event` —— 12 条，违规 7 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `AGGRAVATE_BLEEDING_ON_HIT` | Bleeding you inflict is Aggravated Base Bleeding Duration is 1 second 50% more BuffMagnitude of Bleeding you i | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD` | Attack HitDamage Aggravate any Bleeding on targets which is older than 4 seconds | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED` | 20% chance for Bleeding to be Aggravated when Inflicted against Enemies on Jagged Ground | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_AGGRAVATE_ON_HIT_VS_PINNED` | 30% increased Pinned Buildup | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_DAZE_ON_HIT` | 5% chance to Daze on HitDamage | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_DEBILITATE_ON_HIT` | 25% increased Warcry Speed | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHANCE_ONSLAUGHT_ON_KILL` | 50% chance to gain Onslaught on Killing Blow with Axes | chance_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `grant_on_event` —— 10 条，违规 10 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `GRANT_CHARGE_ON_GAIN` | 10% chance when collecting an Elemental Infusion to gain an additional Elemental Infusion of the same type | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_COMBO_ON_HIT` | 10% Chance to build an additional Combo on HitDamage | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_ENDURANCE_CHARGE_ON_GAIN` | +1 to Maximum Endurance Charges | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_ENERGY_ON_VOLATILITY_DETONATE` | 25% chance that when Volatility on you explodes, you regain an equivalent amount of Volatility | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_ENERGY_REFUND` | 25% chance for Trigger skills to refund half of Energy Spent | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_FRENZY_CHARGE_ON_CONSUME_POWER` | Quarterstaff Skills that consume Power Charges count as consuming an additional Power Charge | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_FRENZY_CHARGE_ON_GAIN` | +1 to Maximum Frenzy Charges | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_INTIMIDATE_ON_HEAVY_STUN` | Enemies you Heavy Stun while Shapeshifted are Intimidated for 6 seconds | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_POWER_CHARGE_ON_GAIN` | +1 to Maximum Power Charges | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `GRANT_VOLATILITY_ON_KILL` | 3% chance to gain Volatility on Kill | grant_on_event | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `damage_taken_convert` —— 9 条，违规 4 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `PHYSICAL_TAKEN_AS_CHAOS` | 20% of Physical Damage taken as Chaos Damage | damage_taken_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PHYSICAL_TAKEN_AS_COLD` | 5% of Physical Damage taken as Cold Damage | damage_taken_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PHYSICAL_TAKEN_AS_LIGHTNING` | 5% of Physical Damage taken as Lightning Damage | damage_taken_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `PHYSICAL_TAKEN_AS_FIRE` | 5% of Physical Damage taken as Fire Damage | damage_taken_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `recoup` —— 6 条，违规 3 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH` | Minions Recoup {X}% of Damage taken as Health | recoup | `R5`/P1 描述为 'Damage taken as' 但 effect=recoup（应为 damage_taken_convert） |
| `MINION_RECOUP_HEALTH` | Minions Gain 20% of Elemental Damage as Extra Chaos Damage | recoup | `R4`/P1 描述为 'as Extra ... Damage' 但 effect=recoup（应为 damage_convert/random_convert）<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `TOTEM_RECOUP_HEALTH` | Each Totem applies 2% increased Damage taken to Enemies in their Presence | recoup | `R8`/P2 描述含 Presence 但 effect=recoup<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `delivery` —— 5 条，违规 0 条

✅ 全部通过规则校验。

### `charge_convert` —— 5 条，违规 5 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `CHARGE_CONVERT_CONDUIT` | If you would gain a Charges, Allies in your Presence gain that Charges instead | charge_convert | `R8`/P2 描述含 Presence 但 effect=charge_convert<br>`S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARGE_CONVERT_GAIN_MAX_ENDURANCE` | 2% chance that if you would gain Endurance Charges, you instead gain up to maximum Endurance Charges | charge_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARGE_CONVERT_GAIN_MAX_FRENZY` | 2% chance that if you would gain Frenzy Charges, you instead gain up to your maximum number of Frenzy Charges | charge_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARGE_CONVERT_GAIN_MAX_POWER` | 2% chance that if you would gain Power Charges, you instead gain up to your maximum number of Power Charges | charge_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
| `CHARGE_CONVERT_RESONANCE` | Gain Power Charges instead of Frenzy Charges Gain Frenzy Charges instead of Endurance Charges Gain Endurance C | charge_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `lucky` —— 4 条，违规 0 条

✅ 全部通过规则校验。

### `resource_convert` —— 3 条，违规 0 条

✅ 全部通过规则校验。

### `hit_flag` —— 2 条，违规 0 条

✅ 全部通过规则校验。

### `requirement` —— 1 条，违规 1 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `ATTRIBUTE_REQUIREMENTS` | You can wield Two-Handed Axes, Maces and Swords in one hand Triple Attributes requirements of Martial Weapons  | requirement | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `leech` —— 1 条，违规 0 条

✅ 全部通过规则校验。

### `random_convert` —— 1 条，违规 1 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `EXTRA_RANDOM_ELEMENT_RANDOM_SHAPESHIFTED` | Gain 8% of Damage as Extra Damage of a random ElementalDamage while Shapeshifted | random_convert ['fire', 'chaos', 'cold', 'lightning', 'physical']→None random | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |

### `random_taken_convert` —— 1 条，违规 1 条

| Id | 描述(注释) | 实测 | 问题 |
|---|---|---|---|
| `PHYSICAL_TAKEN_AS_RANDOM_ELEMENT` | 20% increased ElementalDamage Damage with Attacks | random_taken_convert | `S1`/P3 描述无 {X}（固定效果）但 amount=$value |
