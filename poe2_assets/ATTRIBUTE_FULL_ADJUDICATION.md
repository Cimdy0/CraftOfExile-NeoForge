# 全量 119 属性逐条裁定表

> 通道判定（用户规则）：chance → Stat值(ModifierStatEffect scope-fold 读)；倍率(multiplier) → Effect数值(StatEffect.fire 通道)。

> 属性总数=119；有接线=78；漂移候选=5（已知 1 + 混合维度 4）。


## 漂移候选（优先裁定）

- **AILMENT_CHANCE** (`ailment_chance`)：[fold·scope ailment] 异常触发几率；StatGen 接线
    - `valueMod` {BLEED} `add_ailment_chance_bleed`
    - `valueMod` {MAIM} `add_ailment_chance_maim`
    - `valueMod` {POISON} `add_ailment_chance_poison`
    - `chance` {CHILL} `chance_chill`
    - `chance` {FREEZE} `chance_freeze`
    - `chance` {IGNITE} `chance_ignite`
    - `chance` {SHOCK} `inc_chance_to_shock`
    - `pctAdd` {AILMENT} `inc_ailment_chance`
    - `pctAdd` {AILMENT,ONE_HANDED} `attacks_with_one_handed_martial_weapon_have_inc_chance_to_inflict_ailments`
- **ACCURACY** (`accuracy`)：[核心·scope attack] 命中准确率；命中判定读取（攻方，{attack} 作用域）
    - `valueMod` {} `add_accuracy`
    - `valueMod` {} `inc_accuracy`
    - `chance` {} `chance_attacks_accuracy_rating_equal`
    - `inc` {} `gain_accuracy_equal_to_your_intelligence`
    - `inc` {} `gain_accuracy_equal_to_your_strength`
    - `chance` {} `chance_attacks_accuracy_rating_equal_critical`
    - `sbMod` {} `accuracy`
- **ADDED_DAMAGE** (`added_damage`)：[零基·禁inc] 附加伤害（按元素 valueMod 接）；base=0，禁 inc
    - `valueMod` {CHAOS} `add_chaos_damage`
    - `valueMod` {COLD} `add_cold_damage`
    - `add` {ELEMENTAL} `add_elemental_damage`
    - `valueMod` {FIRE} `add_fire_damage`
    - `valueMod` {LIGHTNING} `add_light_damage`
    - `add` {PHYSICAL} `add_physical_damage`
    - `chance` {ARMOR} `thorns_damage_has_chance_to_ignore_enemy_armor`
- **DEFEND_ARMOUR** (`defend_armour`)：[数据先行] 护甲招架几率；StatGen 接线（chance / pctAdd{crit}），消费端待接
    - `chance` {} `chance_to_defend_with_of_armor`
    - `pctAdd` {CRIT} `defend_with_of_armor_against_critical_hits`
- **KNOCKBACK** (`knockback`)：[数据先行] 击退距离倍率；StatGen 接线（chance / inc），消费端待接
    - `chance` {KNOCKBACK} `chance_to_knock_enemies_back_with_hit_damage_at_close_range`
    - `inc` {} `inc_knockback_distance`

## 全量裁定

| # | 属性 | key | section | 接线(op/tags/StatIds) | 真实语义锚 | 通道判定 | 裁定 |
|---|---|---|---|---|---|---|---|
| 1 | WORK_APTITUDE | work_aptitude | §A | — | [legacy] 全工种适应度加成；WorkSpeed 直接读取 | 直读/保留 | 保留 |
| 2 | WORK_SPEED | work_speed | §A | — | [legacy] 全工种适应度加成；WorkSpeed 直接读取 | 直读/保留 | 保留 |
| 3 | MAX_DARKNESS | max_darkness | §B | — | [核心·scope darkness] 最大黑暗值；DarknessHelper | 直读/保留 | 保留 |
| 4 | MAX_ENDURANCE_CHARGES | max_endurance_charges | §B | add·ENDURANCE:add_max_endurance | [核心] 最大耐力球数 | Effect数值(倍率) | 保留 |
| 5 | MAX_ENERGY_SHIELD | max_energy_shield | §B | valueMod:add_max_energy_shield / more:inc_max_energy / more:inc_armour_energy | [核心] 最大能量护盾（ES）；StateCalculator 读取，承伤吸收层 | Effect数值(倍率) | 保留 |
| 6 | MAX_FRENZY_CHARGES | max_frenzy_charges | §B | add·FRENZY:add_max_frenzy | [核心] 最大狂怒球 | Effect数值(倍率) | 保留 |
| 7 | MAX_INFUSION | max_infusion | §B | — | [核心·scope infusion] 元素灌入层数上限；InfusionHel | 直读/保留 | 保留 |
| 8 | MAX_MANA | max_mana | §B | valueMod:add_max_mana / valueMod:inc_max_mana | [核心] 最大魔力 | Effect数值(倍率) | 保留 |
| 9 | MAX_POWER_CHARGES | max_power_charges | §B | add·POWER:add_max_power | [核心] 最大能量球 | Effect数值(倍率) | 保留 |
| 10 | MAX_RAGE | max_rage | §B | add:to_max_rage / add:inc_max_rage / valueMod:more_max_rage | [核心] 最大怒气 | Effect数值(倍率) | 保留 |
| 11 | MAX_REGENERATION | max_regeneration | §B | valueMod·HEALTH,REGEN:add_max_health_regeneration / regenMod:inc_regenerate_life_ve_flask / regenMod:regenerate_of_max_health_per_second / regenMod:inc_regenerate_life_surrounded / regenMod:inc_regenerate_life_affected_any / regenMod:inc_regenerate_life_low / regenMod:regenerate_of_max_health_per_second_while_stationary / regenMod:inc_regenerate_life_totem / regenMod:minions_regenerate_of_max_health_per_second / regenMod:totems_regenerate_of_max_health_per_second / regenMod:inc_regenerate_life_ve_flask_per_second / regenMod:regenerate_of_max_health_per_second_if_you_have_been_hit_recently / regenMod:inc_regenerate_life_surrounded_per_second / regenMod:inc_regenerate_life_affected_any_while_affected / regenMod:inc_regenerate_life_low_while_on / regenMod:inc_regenerate_life_totem_while_you / regenMod:allies_in_your_presence_regenerate_of_your_max_health_per_second | [fold] 最大再生率 | Effect数值(倍率) | 保留 |
| 12 | MAX_RESISTANCES | max_resistances | §B | valueMod·CHAOS:add_max_chaos_resistance / valueMod·COLD:add_max_cold_resistance / valueMod·FIRE:add_max_fire_resistance / valueMod·LIGHTNING:add_max_lightning_resistance / add:add_max_resistances | [核心] 最大抗性上限（0.75） | Effect数值(倍率) | 保留 |
| 13 | MAX_SPIRIT | max_spirit | §B | valueMod:add_max_spirit / more:inc_spirit | [核心] 最大精魂 | Effect数值(倍率) | 保留 |
| 14 | MAX_VOLATILITY | max_volatility | §B | valueMod·VOLATILITY:inc_max_volatility | [核心·scope volatility] 波动上限；VolatilityHel | Effect数值(倍率) | 保留 |
| 15 | META_ENERGY_MAX | meta_energy_max | §B | inc·META:meta_energy | [核心] 元技能能量上限（base 100） | Effect数值(倍率) | 保留 |
| 16 | BASE_ENERGY_SHIELD_RECHARGE | base_energy_shield_recharge | §C | — | [核心] ES 基础回复倍率；EnergyShieldHelper 读取 | 直读/保留 | 保留 |
| 17 | CURRENT_ENERGY_SHIELD_RECHARGE | current_energy_shield_recharge | §C | — | [核心] ES 当前回复倍率；EnergyShieldHelper 读取 | 直读/保留 | 保留 |
| 18 | DARKNESS_RESERVATION_LASTS | darkness_reservation_lasts | §C | — | [核心·scope darkness] 黑暗保留持续；DarknessHelpe | 直读/保留 | 保留 |
| 19 | ENERGY_RECHARGE | energy_recharge | §C | valueMod·ENERGY:add_energy_recharge | [fold] 能量（施法资源）回复速率倍率 | Effect数值(倍率) | 保留 |
| 20 | GLORY | glory | §C | chance·GLORY:skills_have_a_chance_to_not_consume_glory | [核心] 荣耀资源（ResourceType.GLORY） | Stat值(chance) | 保留 |
| 21 | GLORY_GENERATION | glory_generation | §C | valueMod·GLORY:inc_glory_generation | [数据先行] 荣耀生成速率；StatGen 接线（{glory}，ADD_MUL | Effect数值(倍率) | 保留 |
| 22 | NO_INHERENT_MANA_REGENERATION | no_inherent_mana_regeneration | §C | — | [fold] 禁用固有魔力再生 | 直读/保留 | 保留 |
| 23 | NO_SPIRIT_BASE_DARKNESS | no_spirit_base_darkness | §C | — | [核心·scope darkness] 无精魂黑暗基底；DarknessHelp | 直读/保留 | 保留 |
| 24 | RAGE_LOSS_GRACE | rage_loss_grace | §C | — | [核心] 怒气固有流失宽限期（tick）；RageHelper 读取 | 直读/保留 | 保留 |
| 25 | RAGE_LOSS_RATE | rage_loss_rate | §C | — | [核心] 怒气固有流失速率倍率；RageHelper 读取 | 直读/保留 | 保留 |
| 26 | REGENERATE_MANA_EQUAL_LIFE | regenerate_mana_equal_life | §C | — | [fold] 魔力再生等于生命比例 | 直读/保留 | 保留 |
| 27 | RESERVATION_EFFICIENCY | reservation_efficiency | §C | pctAdd·RESERVATION:inc_reservation_efficiency | [fold·scope aura] 保留效率（降低精魂保留量） | Effect数值(倍率) | 保留 |
| 28 | VOLATILITY_DETONATION_TIME | volatility_detonation_time | §C | valueMod·VOLATILITY:inc_volatility_explosion_delay | [fold·scope volatility] 波动爆炸延迟（秒） | Effect数值(倍率) | 保留 |
| 29 | VOLATILITY_GENERATION | volatility_generation | §C | valueMod·VOLATILITY:inc_volatility_generation | [fold·scope volatility] 波动获取速率倍率 | Effect数值(倍率) | 保留 |
| 30 | DEXTERITY | dexterity | §D | valueMod:add_dexterity / more:inc_dexterity | [零基·禁inc] 敏捷（属性点）；base=0，仅 valueMod/pctA | Effect数值(倍率) | 保留 |
| 31 | INTELLIGENCE | intelligence | §D | valueMod:add_intelligence / more:inc_intelligence / pctAdd:inc_intelligence_2 | [零基·禁inc] 智力（属性点）；base=0，仅 valueMod/pctA | Effect数值(倍率) | 保留 |
| 32 | STRENGTH | strength | §D | valueMod:add_strength / entityMod·COMPANION:companions_gain_your_strength / more:inc_strength | [零基·禁inc] 力量（属性点）；base=0，仅 valueMod/pctA | Effect数值(倍率) | 保留 |
| 33 | AILMENT_BUILDUP | ailment_buildup | §E | inc·AILMENT:inc_freeze_buildup_with_empowered_attacks / inc·ELECTROCUTED:inc_electrocute_buildup / inc·FREEZE:inc_freeze_buildup / inc:inc_immobilisation_buildup / inc·PIN:inc_pin_buildup / valueMod·STUN:inc_stun_buildup / sbMod·AILMENT,FREEZE,SPELL:archon_freeze_buildup / sbMod·AILMENT,IGNITE,SPELL:archon_ignite_buildup | [fold·scope ailment] 异常积蓄倍率；StatGen 接线 | Effect数值(倍率) | 保留 |
| 34 | AILMENT_CHANCE | ailment_chance | §E | valueMod·BLEED:add_ailment_chance_bleed / valueMod·MAIM:add_ailment_chance_maim / valueMod·POISON:add_ailment_chance_poison / chance·CHILL:chance_chill / chance·FREEZE:chance_freeze / chance·IGNITE:chance_ignite / chance·SHOCK:inc_chance_to_shock / pctAdd·AILMENT:inc_ailment_chance / pctAdd·AILMENT,ONE_HANDED:attacks_with_one_handed_martial_weapon_have_inc_chance_to_inflict_ailments | [fold·scope ailment] 异常触发几率；StatGen 接线 | 混合：chance=Stat值 / 倍率=Effect数值 | 混合维度·待归一 |
| 35 | AILMENT_DAMAGE_CONTRIBUTION | ailment_damage_contribution | §E | — | [fold·scope ailment] 异常伤害贡献 | 直读/保留 | 保留 |
| 36 | AILMENT_DAMAGE_RATE | ailment_damage_rate | §E | valueMod·BLEED:inc_ailment_damage_rate_bleed / valueMod·POISON:inc_ailment_damage_rate_poison / inc·AILMENT,BLEED:bleeding_you_inflict_deals_damage_faster / inc·AILMENT:damaging_ailments_deal_damage_faster / inc·DAMAGE_OVER_TIME:inc_faster_ailments / inc·IGNITE:inc_ignite_faster | [fold·scope ailment] 异常伤害速率 | Effect数值(倍率) | 保留 |
| 37 | AILMENT_DURATION | ailment_duration | §E | inc·AILMENT,not,TAKEN:inc_ailment_duration / inc·ARCHON,not,TAKEN:inc_archon_buff_duration / inc·BANNER,not,TAKEN:banner_skills_have_inc_duration / inc·BEAST,not,TAKEN:inc_beast_ailment_duration / inc·BLEED,not,TAKEN:inc_bleed_duration / valueMod·TAKEN,BLEED,AILMENT:inc_bleeding_duration_taken / inc·CHARGE,not,TAKEN:inc_frenzy_charge_duration / inc·CHARM,not,TAKEN:inc_charm_duration / inc·TAKEN,AILMENT:inc_duration_of_ailments_on_you / valueMod·TAKEN,CHILL,AILMENT:inc_chill_duration_taken / inc·CURSE,not,TAKEN:inc_curse_duration / inc·DAMAGE_OVER_TIME,not,TAKEN:inc_duration_of_damaging_ailments_on_enemies / inc·DAZE,not,TAKEN:inc_daze_duration / inc·TAKEN,AILMENT:inc_debuff_expiry / inc·FLASK,not,TAKEN:inc_flask_effect_duration / valueMod·TAKEN,FREEZE,AILMENT:inc_freeze_duration_taken / inc·GRENADE,not,TAKEN:inc_grenade_detonation_time / inc·HAZARD,not,TAKEN:inc_hazard_duration / inc·IGNITE,not,TAKEN:inc_ignite_duration_on_enemies / valueMod·TAKEN,IGNITE,AILMENT:inc_ignite_duration_taken / valueMod·INFUSION,not,TAKEN:inc_infusion_duration / inc·JAGGED_GROUND,not,TAKEN:inc_jagged_ground_duration / inc·OFFERING,not,TAKEN:inc_offering_duration / inc·PARRY,not,TAKEN:inc_parried_debuff_duration / inc·PIN,not,TAKEN:inc_pinned_duration / inc·POISON,not,TAKEN:inc_poison_duration / valueMod·TAKEN,POISON,AILMENT:inc_poison_duration_taken / valueMod·TAKEN,SHOCK,AILMENT:inc_shock_duration_taken / inc·not,TAKEN:inc_skill_effect_duration / valueMod·STUN,not,TAKEN:inc_stun_duration | [fold·scope ailment] 异常时长倍率（归一属性） | Effect数值(倍率) | 保留 |
| 38 | AILMENT_MAGNITUDE | ailment_magnitude | §E | add·EXPOSURE,not,TAKEN:add_exposure / valueMod·AILMENT,not,TAKEN:inc_ailment_magnitude / valueMod·SHOCK,not,TAKEN:inc_shock_magnitude / valueMod·CHILL,not,TAKEN:inc_buff_magnitude_of_chill_you_inflict / valueMod·POISON,not,TAKEN:inc_poison_magnitude / valueMod·IGNITE,not,TAKEN:inc_ignite_buff_magnitude / inc·ARCANE_SURGE,not,TAKEN:inc_buff_effect_of_arcane_surge_on_you / inc·AURA,BANNER,not,TAKEN:banner_skills_have_inc_aura_buff_magnitude / inc·TAKEN,AILMENT,BLEED:inc_buff_magnitude_of_bleeding_on_you / inc·BLIND,not,TAKEN:inc_blind_effect / inc·AURA,TAKEN:inc_buff_effect_taken / valueMod·AURA,not,TAKEN:inc_buff_magnitude / inc·CURSE,TAKEN:inc_buff_effect_of_curses_on_you / inc·AURA,not,TAKEN:inc_aura_magnitude / inc·ARCHON,not,TAKEN:inc_effect_of_archon_buffs_on_you / inc·TAKEN,AILMENT,SHOCK:inc_buff_effect_of_shock_on_you / inc·TAKEN,WITHERED:inc_buff_effect_of_withered_on_you / inc·TAKEN,AILMENT,CHILL:inc_effect_of_chill_on_you / inc·CURSE,not,TAKEN:inc_curse_buff_magnitude / inc·DAMAGE_OVER_TIME,not,TAKEN:inc_buff_magnitude_of_damaging_ailments_you_inflict / more·AILMENT,BLEED,not,TAKEN:more_bleeding_magnitude_inflicted / inc·DAZE,not,TAKEN:inc_buff_magnitude_of_daze / inc·EXPOSURE,not,TAKEN:inc_exposure_effect / inc·CURSE,FIRE,not,TAKEN:inc_flammability_magnitude / inc·TAKEN,AILMENT,IGNITE:inc_ignite_magnitude_taken / inc·INCISION,not,TAKEN:inc_incision_magnitude / valueMod·INFUSION,not,TAKEN:inc_infusion_magnitude / inc·JAGGED_GROUND,not,TAKEN:inc_buff_magnitude_of_jagged_ground_you_create / inc·MARK,not,TAKEN:inc_mark_effect / valueMod·NON_DAMAGING,not,TAKEN:inc_non_damaging_ailment_magnitude / inc·OFFERING,not,TAKEN:offering_skills_have_inc_buff_effect / inc·PARRY,not,TAKEN:inc_parried_debuff_buff_magnitude / inc·TAKEN,SLOW:inc_slowing_potency_of_debuffs_on_you / inc·WARCRY,not,TAKEN:inc_total_power_counted_by_warcries / inc·WITHERED,not,TAKEN:inc_withered_buff_magnitude / sbMod·AILMENT,SHOCK,SPELL,not,TAKEN:archon_shock_magnitude | [fold·scope ailment] 异常幅度倍率（归一属性，按异常名作用域 | Effect数值(倍率) | 保留 |
| 39 | AILMENT_THRESHOLD | ailment_threshold | §E | — | [fold·scope ailment,threshold] 异常门槛倍率（归一 | 直读/保留 | 保留 |
| 40 | AILMENT_THRESHOLD_ADAPTATION | ailment_threshold_adaptation | §E | — | [核心] 异常门槛适应倍率；StatusEngine:470 读取 | 直读/保留 | 保留 |
| 41 | AILMENT_THRESHOLD_ADD | ailment_threshold_add | §E | — | [fold·scope ailment,threshold] 异常门槛加性（归一 | 直读/保留 | 保留 |
| 42 | AFTERSHOCK_AREA | aftershock_area | §F | inc:slam_skills_you_use_yourself_have_inc_aftershock_area_of_effect | [fold·scope slam] 余震范围效果倍率 | Effect数值(倍率) | 保留 |
| 43 | AFTERSHOCK_CHANCE | aftershock_chance | §F | chance:chance_for_slam_skills_to_cause_an_additional_aftershock / chance·MACE:chance_for_mace_slam_skills_you_use_yourself_to_cause_an_additional_aftershock / chance·FORM:chance_for_shapeshift_slam_skills_y_use_yourself_to_cause_an_additional_aftershock | [fold·scope slam] 余震几率（归一 {slam}/{mace,s | Stat值(chance) | 保留 |
| 44 | ARCHON_RECOVERY_RATE | archon_recovery_rate | §F | inc·ARCHON:archon_recovery_period_expires_slower | [fold·scope archon] Archon 恢复速率倍率 | Effect数值(倍率) | 保留 |
| 45 | CHARM_ACTIVATION_CHANCE | charm_activation_chance | §F | chance·CHARM:chance_when_a_charm_is_used_to_use_another_charm_without_consuming_charges | [fold·scope charm] 护符连锁触发几率 | Stat值(chance) | 保留 |
| 46 | CHARM_CHARGES_GAINED | charm_charges_gained | §F | inc·CHARM:inc_charm_charges_gain | [fold·scope charm] 护符充能获得量倍率 | Effect数值(倍率) | 保留 |
| 47 | CHARM_CHARGES_USED | charm_charges_used | §F | inc·CHARM:inc_charm_charges_used | [fold·scope charm] 护符充能消耗倍率（<1 减耗） | Effect数值(倍率) | 保留 |
| 48 | CHARM_EFFECT | charm_effect | §F | inc·CHARM:inc_charm_effect | [fold·scope charm] 护符效果强度倍率 | Effect数值(倍率) | 保留 |
| 49 | GUARD_RECOVERY | guard_recovery | §F | — | [fold·scope guard] Guard 护盾回复量 | 直读/保留 | 保留 |
| 50 | PUPPET_MASTER_DURATION | puppet_master_duration | §F | add:inc_duration_of_puppet_master | [fold·scope puppet] 傀儡师 buff 时长缩放（基础时间 4 | Effect数值(倍率) | 保留 |
| 51 | PUPPET_MASTER_EFFECT | puppet_master_effect | §F | add:inc_effect_of_puppet_master | [fold·scope puppet] 傀儡师效果强度（零基累积） | Effect数值(倍率) | 保留 |
| 52 | REMNANTS_COLLECTED_FURTHER_AWAY | remnants_collected_further_away | §F | inc·REMNANT:remnants_can_be_collected_from_further_away | [fold·scope remnant] 残迹收集距离倍率 | Effect数值(倍率) | 保留 |
| 53 | REMNANT_DOUBLE_CHANCE | remnant_double_chance | §F | — | [核心·scope remnant] 残迹双倍几率；RemnantState:5 | 直读/保留 | 保留 |
| 54 | REMNANT_EXTRA_CHANCE | remnant_extra_chance | §F | chance·REMNANT:chance_to_create_an_additional_remnant | [核心·scope remnant] 额外残迹几率；RemnantState 读 | Stat值(chance) | 保留 |
| 55 | REMNANT_MAGNITUDE | remnant_magnitude | §F | — | [核心·scope remnant] 残迹幅度；RemnantState:54  | 直读/保留 | 保留 |
| 56 | THORNS | thorns | §F | inc·PHYSICAL:gain_physical_thorns_damage_equal_to_of_max_health_while_shapeshifted / inc:inc_thorns_damage | [数据先行] 荆棘反伤倍率（受击方）；StatGen 接线（inc），消费端待接 | Effect数值(倍率) | 保留 |
| 57 | ACCURACY | accuracy | §G | valueMod:add_accuracy / valueMod:inc_accuracy / chance:chance_attacks_accuracy_rating_equal / inc:gain_accuracy_equal_to_your_intelligence / inc:gain_accuracy_equal_to_your_strength / chance:chance_attacks_accuracy_rating_equal_critical / sbMod:accuracy | [核心·scope attack] 命中准确率；命中判定读取（攻方，{attac | 混合：chance=Stat值 / 倍率=Effect数值 | 混合维度·待归一 |
| 58 | ADDED_DAMAGE | added_damage | §G | valueMod·CHAOS:add_chaos_damage / valueMod·COLD:add_cold_damage / add·ELEMENTAL:add_elemental_damage / valueMod·FIRE:add_fire_damage / valueMod·LIGHTNING:add_light_damage / add·PHYSICAL:add_physical_damage / chance·ARMOR:thorns_damage_has_chance_to_ignore_enemy_armor | [零基·禁inc] 附加伤害（按元素 valueMod 接）；base=0，禁  | 混合：chance=Stat值 / 倍率=Effect数值 | 混合维度·待归一 |
| 59 | EFFECT_AREA | area_of_effect | §G | inc:inc_area_of_effect_for_attacks / inc·MELEE,AREA:strikes_deal_splash_damage / entityMod·MINION:minions_have_inc_area_of_effect | [数据先行·scope] 范围效果倍率；StatGen 接线（inc），消费端待 | Effect数值(倍率) | 保留 |
| 60 | ARMOR_BREAK | armor_break | §G | — | [fold] 护甲破坏倍率 | 直读/保留 | 保留 |
| 61 | BLOCK_CHANCE | block_chance | §G | valueMod:add_block_chance / pctAdd:inc_block_chance / add:to_max_block_chance | [fold·scope block] 格挡几率（max 0.75） | Effect数值(倍率) | 保留 |
| 62 | CAST_SPEED | cast_speed | §G | inc·CURSE,AREA:inc_area_of_effect_of_curses / inc:inc_cast_speed / inc:inc_skill_speed / inc:minions_have_inc_cast_speed / inc·ATTACK:inc_minions_attack_cast_speed / inc·MINION:inc_minion_revive / inc·TOTEM:inc_totem_placement_speed / inc·TRAP:inc_trap_throw_speed / inc·MARK:mark_use_speed / inc·SEAL:seal_gain_speed / inc·WARCRY:warcry_speed / sbModC:adrenaline_cast_speed / sbMod:onslaught_cast_speed / sbMod·BANNER:banner_cast_speed / sbMod:arcane_surge_cast_speed | [核心] 施法速度倍率（与 ATTACK_SPEED 正交） | Effect数值(倍率) | 保留 |
| 63 | COOLDOWN_RECOVERY_RATE | cooldown_recovery_rate | §G | inc:inc_minions_cooldown_recovery_rate / inc·GRENADE:grenade_skills_have_cooldown_use / inc·COMMAND:inc_minions_cooldown_recovery_rate_command / inc:inc_cooldown_recovery_rate | [数据先行] 冷却回复速率倍率；StatGen 接线（inc），消费端待接 | Effect数值(倍率) | 保留 |
| 64 | CRIT_CHANCE | crit_chance | §G | add·ATTACK,CRIT:add_crit_chance / inc·HERALD:to_critical_hit_chance_of_herald_skills / inc·ATTACK:attacks_have_to_critical_hit_chance / inc·MINION:minions_have_inc_critical_hit_chance / inc·ATTACK,CRIT:inc_crit_chance / inc·ATTACK,CRIT:inc_critical_hit_chance_against_immobilised_enemies / sbModC·FORM,WYVERN,CRIT:form_wyvern_crit_chance | [fold·scope crit] 暴击几率 | Effect数值(倍率) | 保留 |
| 65 | CRIT_DAMAGE | crit_damage | §G | valueMod·SPELL,CRIT:add_cast_crit_damage / add·ATTACK,CRIT:add_crit_damage / inc·ATTACK,CRIT:inc_critical_damage_bonus / inc·ATTACK,CRIT:inc_critical_damage_bonus_if_you_ve_consumed_a_power_charge_recently / sbModC·FORM,WYVERN,CRIT:form_wyvern_crit_damage | [fold·scope crit] 暴击伤害倍率 | Effect数值(倍率) | 保留 |
| 66 | CROSS_BOW_RELOAD_TIME | cross_bow_reload_time | §G | inc·CROSSBOW:inc_reload_speed | [fold·scope crossbow] 弩装填时间倍率 | Effect数值(倍率) | 保留 |
| 67 | CULLING_STRIKE_THRESHOLD | culling_strike_threshold | §G | inc:inc_culling_strike_threshold | [数据先行] 斩杀阈值倍率；StatGen 接线（inc），消费端待接 | Effect数值(倍率) | 保留 |
| 68 | DAMAGE | damage | §G | inc·DAMAGE,BALLISTA:inc_ballista_damage / inc·DAMAGE,BOW:inc_damage_with_bows / inc·DAMAGE,ATTACK,EMPOWERED:empowered_attacks_deal_inc_damage / inc·DAMAGE,GRENADE:grenade_damage / inc·DAMAGE,HAZARD:hazard_damage / inc·DAMAGE,HERALD:herald_damage / inc·DAMAGE,ATTACK,AREA:inc_attack_area_damage / inc·DAMAGE,ELEMENTAL,ATTACK:inc_attack_elemental_damage / valueMod·DAMAGE,CHAOS:inc_chaos_damage / valueMod·DAMAGE,COLD:inc_cold_damage / valueMod·DAMAGE,SPELL,COLD:inc_cold_damage_spell / inc·DAMAGE,COMPANION:companions_deal_inc_damage / inc·DAMAGE,CROSSBOW:inc_crossbow_damage / inc·DAMAGE,AXE:inc_damage_with_axes / inc·DAMAGE,MACE:inc_damage_with_maces / inc·DAMAGE,SPEAR:inc_damage_with_spears / inc·DAMAGE,TRAP:inc_trap_damage / inc·DAMAGE:inc_damage_with_hits_against_chilled_enemies / inc·DAMAGE,ATTACK:inc_damage_attack / inc·DAMAGE,ATTACK,EMPOWERED:inc_damage_empowered_attacks / conditionMod:inc_damage_blocking / conditionMod:inc_damage_low_life / conditionMod:inc_damage_parrying / entityMod·DAMAGE:inc_damage_presence_allies / entityMod·DAMAGE:inc_damage_presence_enemies / conditionMod:inc_damage_recently_killed / conditionMod:inc_damage_shapeshifted / inc·DAMAGE,ELEMENTAL:inc_elemental_damage / inc·DAMAGE,not,TAKEN:add_recently_consumed_damage / valueMod·DAMAGE,FIRE:inc_fire_damage / valueMod·DAMAGE,SPELL,FIRE:inc_fire_damage_spell / valueMod·DAMAGE,LIGHTNING:inc_light_damage / valueMod·DAMAGE,SPELL,LIGHTNING:inc_light_damage_spell / entityMod·MINION,DAMAGE:inc_minion_damage / inc·DAMAGE,ONE_HANDED:inc_damage_with_one_handed_weapons / inc·DAMAGE,PARRY:inc_parry_damage / inc·DAMAGE,PHYSICAL:inc_physical_damage / inc·DAMAGE,PHYSICAL,FORM:inc_physical_damage_while_shapeshifted / inc·DAMAGE,PROJECTILE:inc_projectile_damage / inc·DAMAGE,SPELL:inc_spell_damage / inc·DAMAGE,SWORD:inc_damage_with_swords / inc·TOTEM:inc_totem_damage / inc·DAMAGE,TWO_HANDED:inc_damage_with_two_handed_weapons / more·DAMAGE,MACE:more_damage_against_heavy_stunned_enemies_with_maces / inc·DAMAGE,MELEE:inc_melee_damage / inc·DAMAGE,MINION:minion_damage / inc·DAMAGE,TRAP:trap_damage / sbMod·ELEMENTAL,SPELL:damage_dealt_elemental_spell / sbMod·FORM,BEAR,PHYSICAL:damage_dealt_bear_physical / sbMod·FORM,BEAR,FIRE:damage_dealt_bear_fire / sbMod·FORM,WOLF,COLD:damage_dealt_wolf_cold / sbMod·FORM,WOLF,PHYSICAL:damage_dealt_wolf_physical / sbMod·FORM,WYVERN,LIGHTNING:damage_dealt_wyvern_lightning / sbMod·FORM,WYVERN,CHAOS:damage_dealt_wyvern_chaos / sbModC:adrenaline_damage | [fold·scope] 伤害倍率（归一属性，按 scope 区分 attack | Effect数值(倍率) | 保留 |
| 69 | DAMAGE_OVER_TIME_MULTIPLIER | damage_over_time_multiplier | §G | inc·DAMAGE_OVER_TIME:inc_damage_over_time | [fold] 持续伤害倍率 | Effect数值(倍率) | 保留 |
| 70 | DEFEND_ARMOUR | defend_armour | §G | chance:chance_to_defend_with_of_armor / pctAdd·CRIT:defend_with_of_armor_against_critical_hits | [数据先行] 护甲招架几率；StatGen 接线（chance / pctAdd | 混合：chance=Stat值 / 倍率=Effect数值 | **漂移·改名** |
| 71 | DEFLECTION_RATING | deflection_rating | §G | inc·DEFLECT,SURROUNDED:inc_deflection_rating_while_surrounded | [fold·scope deflect] 招架判定强度（格挡判定读取） | Effect数值(倍率) | 保留 |
| 72 | DODGE_CHANCE | dodge_chance | §G | chance·DODGE:chance_dodge | [fold·scope dodge] 闪避几率（格挡判定读取） | Stat值(chance) | 保留 |
| 73 | ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING | elemental_damage_contributes_bleeding | §G | — | [fold·scope bleeding] 元素伤害贡献流血比例 | 直读/保留 | 保留 |
| 74 | EVASION | evasion | §G | valueMod:add_evasion / inc:inc_deflection_rating_equal_evasion / inc:gain_of_evasion_as_extra_armor / inc:minions_have_inc_evasion / inc:stat_converts_all_evasion_rating / inc:inc_deflection_rating_equal_evasion_deflect / valueMod:inc_evasion / sbMod:evasion | [核心] 闪避值 | Effect数值(倍率) | 保留 |
| 75 | KNOCKBACK | knockback | §G | chance·KNOCKBACK:chance_to_knock_enemies_back_with_hit_damage_at_close_range / inc:inc_knockback_distance | [数据先行] 击退距离倍率；StatGen 接线（chance / inc），消 | 混合：chance=Stat值 / 倍率=Effect数值 | 混合维度·待归一 |
| 76 | PARRY_RANGE | parry_range | §G | inc·PARRY:inc_parry_range | [fold·scope parry] 招架范围倍率 | Effect数值(倍率) | 保留 |
| 77 | STUN_RECOVERY | stun_recovery | §G | inc·STUN:inc_stun_recovery | [数据先行·scope stun] 眩晕恢复速率倍率；StatGen 接线（in | Effect数值(倍率) | 保留 |
| 78 | DAMAGE_TAKEN_BYPASSES_ENERGY | damage_taken_bypasses_energy | §H | inc·TAKEN,ENERGY_SHIELD:of_damage_taken_bypasses_energy_shield | [fold] 承伤绕过 ES 比例 | Effect数值(倍率) | 保留 |
| 79 | TAKEN_CRIT_CHANCE | taken_crit_chance | §H | sbMod:taken_crit | [fold·scope taken,crit] 被暴击几率（受害者侧） | 其他Effect(sbMod) | 保留 |
| 80 | TAKEN_CRIT_DAMAGE | taken_crit_damage | §H | valueMod:inc_taken_crit_damage | [fold·scope taken,crit] 承暴击伤害倍率 | Effect数值(倍率) | 保留 |
| 81 | TAKEN_DAMAGE | taken_damage | §H | valueMod:inc_taken_damage / sbMod:damage_taken / sbMod·CHAOS:damage_taken_chaos / sbMod·PHYSICAL:damage_taken_physical / sbModC·PHYSICAL:adrenaline_taken_physical | [fold·scope taken] 承伤倍率 | Effect数值(倍率) | 保留 |
| 82 | TAKE_NO_DAMAGE_VOLATILITY | take_no_damage_volatility | §H | — | [核心·scope volatility] 波动免伤；VolatilityDet | 直读/保留 | 保留 |
| 83 | AURA | aura | §H | — | [fold·scope totem,taken] 图腾承伤光环（攻击方读取附近图 | 直读/保留 | 保留（原 TOTEM_TAKEN_AURA：字段名内嵌作用域，见 ATTRIBUTE_SCOPE_IN_NAME_AUDIT.md §5 改名闭环） |
| 84 | AVOID_AILMENT_CHANCE | avoid_ailment_chance | §I | — | [fold·scope ailment] 异常回避几率 | 直读/保留 | 保留 |
| 85 | RESISTANCES | resistances | §I | valueMod·CHAOS:add_chaos_resistance / valueMod·COLD:add_cold_resistance / valueMod·FIRE:add_fire_resistance / valueMod·LIGHTNING:add_lightning_resistance / pctAdd·CHAOS:to_chaos_resistance_2 / entityMod·ELEMENTAL,RESISTANCE:totems_gain_to_all_max_elemental_resistances / entityMod·MINION,ELEMENTAL,RESISTANCE:inc_minion_elemental_resist / entityMod·MINION,ELEMENTAL,RESISTANCE:minions_have_to_all_elemental_resistances / sbMod·FIRE:charm_resist_fire / sbMod·COLD:charm_resist_cold / sbMod·LIGHTNING:charm_resist_lightning / sbMod·CHAOS:charm_resist_chaos | [fold·scope element] 抗性（归一属性，按元素作用域读取） | Effect数值(倍率) | 保留 |
| 86 | RESISTANCE_PENETRATION | resistance_penetration | §I | pctAdd·RESISTANCE,ELEMENTAL:damage_penetrates_of_enemy_elemental_resistances / pctAdd·RESISTANCE,ELEMENTAL:damage_penetration_elemental_damage_resistances / pctAdd·RESISTANCE,ELEMENTAL,ATTACK:attack_damage_penetrates_of_enemy_elemental_damage_resistances / pctAdd·RESISTANCE,ELEMENTAL,GLORY:damage_penetrates_elemental_damage_resistances_for_each_time_you_ve_used_a_skill_that_requires_glory_in_the_past_seconds / pctAdd·RESISTANCE,ELEMENTAL:damage_penetration_of_enemy_elemental_damage_resistances_while_shapeshifted | [fold·scope element] 抗性穿透（攻击方，全局乘数） | Effect数值(倍率) | 保留 |
| 87 | STATUS_EXTRA_STACKS | status_extra_stacks | §I | — | [零基·禁inc·scope status] 状态额外层数；base=0，Sta | 直读/保留 | 保留 |
| 88 | STATUS_MAX_STACKS | status_max_stacks | §I | — | [零基·禁inc·scope status] 状态最大层数；base=0，Sta | 直读/保留 | 保留 |
| 89 | AMOUNT_LEECHED | amount_leeched | §J | — | [fold·scope resource] 偷取量倍率（按资源作用域） | 直读/保留 | 保留 |
| 90 | FLASK_CHARGES_GAINED | flask_charges_gained | §J | inc·FLASK:inc_flask_charges_used_from_mana_flasks | [fold·scope flask] Flask 充能获得量倍率 | Effect数值(倍率) | 保留 |
| 91 | FLASK_NO_CONSUME_CHANCE | flask_no_consume_chance | §J | chance·FLASK:flask_no_consume / chance·FLASK:chance_flask_no_consume | [fold·scope flask] Flask 不消耗充能几率 | Stat值(chance) | 保留 |
| 92 | FLASK_RECOVERY_RATE | flask_recovery_rate | §J | inc·MANA,FLASK:flask_mana_recovery / inc·MANA,FLASK:inc_flask_mana_recovery / inc·HEALTH,FLASK:inc_flask_life_recovery / inc·FLASK:inc_health_recovery_from_flasks_used_when_on_low_health | [fold·scope flask] Flask 回复量倍率 | Effect数值(倍率) | 保留 |
| 93 | LEECH | leech | §J | — | [fold·scope resource] 偷取率（按资源作用域） | 直读/保留 | 保留 |
| 94 | LEECHES_RESPONSE | leeches_response | §J | — | [fold·scope resource] 偷取响应（按资源作用域） | 直读/保留 | 保留 |
| 95 | LEECH_RATE | leech_rate | §J | — | [核心] 偷取速率倍率（归一）；DamageHelper:295 读取 | 直读/保留 | 保留 |
| 96 | MANA_TAKE_OF_DAMAGE | mana_take_of_damage | §J | pctAdd:inc_damage_is_taken_mana / pctAdd·MANA_TAKE:inc_mana_take | [fold·scope] 受伤转魔力比例 | Effect数值(倍率) | 保留 |
| 97 | RECOVERY_RATE | recovery_rate | §J | inc:minions_have_inc_health_recovery_rate / inc:inc_speed_of_recoup_effects | [fold] 跨来源恢复率倍率（Regen/Leech/Flask/Recoup | Effect数值(倍率) | 保留 |
| 98 | REGENERATION_RATE | regeneration_rate | §J | valueMod·HEALTH,REGEN:add_health_regeneration_rate / valueMod·ENERGY,REGEN:inc_energy_regeneration_rate / valueMod·MANA,REGEN:inc_mana_regeneration_rate / sbMod·MANA,REGEN:arcane_surge_regen | [fold] 再生速率倍率 | Effect数值(倍率) | 保留 |
| 99 | LAUNCH_ADDITIONAL_PROJECTILE | launch_additional_projectile | §K | valueMod·PROJECTILE,ARROW,CROSSBOW:add_launch_additional_projectile_bolt / valueMod·PROJECTILE,ARROW,BOW:add_launch_additional_projectile_bow | [fold·scope projectile] 额外投射物数量 | Effect数值(倍率) | 保留 |
| 100 | PROJECTILE_PIERCE | projectile_pierce | §K | valueMod·PROJECTILE:add_projectile_pierce | [fold·scope projectile] 投射物穿透次数 | Effect数值(倍率) | 保留 |
| 101 | PROJECTILE_PIERCE_CHANCE | projectile_pierce_chance | §K | add·PROJECTILE:add_projectile_pierce_chance / pctAdd·PROJECTILE:inc_projectile_pierce | [fold·scope projectile] 投射物穿透几率 | Effect数值(倍率) | 保留 |
| 102 | PROJECTILE_RANDOM_FIRE | projectile_random_fire | §K | — | [fold·scope projectile] 投射物随机发射（布尔） | 直读/保留 | 保留 |
| 103 | PROJECTILE_SPEED | projectile_speed | §K | inc·BOLT:inc_bolt_speed / inc·PROJECTILE:inc_increases_reductions_projectile_speed / inc·PROJECTILE:inc_increases_reductions_projectile_speed_bow / inc·PROJECTILE:inc_projectile_speed | [fold·scope projectile] 投射物速度倍率 | Effect数值(倍率) | 保留 |
| 104 | TALENT_AUTO_SMELT | talent_auto_smelt | §L | — | [reserved] 天赋：自动熔炼等级（预留，无消费端） | 直读/保留 | 保留 |
| 105 | TALENT_EXTRA_XP | talent_extra_xp | §L | — | [reserved] 天赋：额外经验（预留，无消费端） | 直读/保留 | 保留 |
| 106 | TALENT_FORTUNE | talent_fortune | §L | — | [reserved] 天赋：幸运（预留，无消费端） | 直读/保留 | 保留 |
| 107 | TALENT_LOOTING | talent_looting | §L | — | [reserved] 天赋：掠夺（预留，无消费端） | 直读/保留 | 保留 |
| 108 | TALENT_MAGNET | talent_magnet | §L | — | [reserved] 天赋：磁吸（预留，无消费端） | 直读/保留 | 保留 |
| 109 | TALENT_MENDING | talent_mending | §L | — | [reserved] 天赋：修复（预留，无消费端） | 直读/保留 | 保留 |
| 110 | ADD_EMPOWERED_ATTACKS | empowered_attacks | §M | pctAdd·WARCRY,not,TAKEN:warcries_empowered_an_additional_attack / add·WARCRY,not,TAKEN:add_empowered_attacks | [零基·禁inc] 战吼强化额外攻击次数；base=0，StatGen 已改 p | Effect数值(倍率) | 保留 |
| 111 | BANNER_LINGER | banner_linger | §M | add·BANNER,not,TAKEN:add_banner_linger | [数据先行] 战旗离开后增益残留秒数；消费端待接 | Effect数值(倍率) | 保留 |
| 112 | CORPSE_RETAIN_CHANCE | corpse_retain_chance | §M | add·CORPSE:add_corpse_retain_chance | [fold·scope corpse] 消耗尸体不摧毁几率；ConsumeCor | Effect数值(倍率) | 保留 |
| 113 | CORRUPTION_ENDURES_CHANCE | corruption_endures_chance | §M | — | [核心] 免死几率（corruption_endures keystone）；D | 直读/保留 | 保留 |
| 114 | FOUND_ITEM_NUMBER | found_item_number | §M | valueMod:add_found_item_number / pctAdd:inc_found_quantity | [零基·禁inc] 掉落物数量；base=0，StatGen 已改 pctAdd | Effect数值(倍率) | 保留 |
| 115 | FOUND_ITEM_RARITY | found_item_rarity | §M | valueMod:add_found_item_rarity / pctAdd:inc_found_rarity / sbMod:charm_golden_rarity | [零基·禁inc] 掉落物稀有度；base=0，StatGen 已改 pctAd | Effect数值(倍率) | 保留 |
| 116 | ITEM_CAPACITY | item_capacity | §M | — | [核心] 物品容量；CapacityHelper 读取 | 直读/保留 | 保留 |
| 117 | PRESENCE_RANGE | presence_range | §M | — | [核心·scope presence] 光环/威势范围；EntityOperat | 直读/保留 | 保留 |
| 118 | RECENTLY_CONSUMED_DAMAGE | recently_consumed_damage | §M | add·CORPSE:inc_damage_if_you_have_consumed_a_corpse_recently | [数据先行] 近期消耗尸体攻击增伤点数；消费端待接 | Effect数值(倍率) | 保留 |
| 119 | SKILL_SLOT_COUNT | skill_slot_count | §M | — | [核心] 技能槽数量上限；SkillScreen 读取 | 直读/保留 | 保留 |