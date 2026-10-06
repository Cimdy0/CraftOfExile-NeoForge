# 属性 FOLD-vs-FIRE 架构轴裁定表

> 架构事实：`StatEffect` 仅描述语义，数值由 `StatEntry.amount` 持有；仅 `ModifierStatEffect`(FOLD) 强制依赖属性，

> 其余 FIRE/DELIVERY/IMMUNE 等亚型经 `entry.amount` 自带数值、无需属性。命中/受击事件已建 `StatEngine.fire` 通道。

> 判定准则：连续倍率/上限/伤害减免→FOLD-KEEP；chance→FOLD-KEEP*(Stat值)；事件驱动一次性动作/倍率→FIRE-候选；伤害期条件减免→边界。


属性总数=120；原 FIRE-候选=10（已落地 2：THORNS/CULLING；其余 8 经原则复核为连续倍率→FOLD-KEEP/FOLD-KEEP*）；边界=2（DEFEND/KNOCKBACK 保留 FOLD）；待定(孤儿)=4（保留 FOLD 数据先行）。

| # | 属性 | key | base | StatGen op/tags | 消费端 | 裁定 | 理由 |
|---|---|---|---|---|---|---|---|
| 1 | DEFEND_WITH_ARMOUR | defend_with_armour | 0.0 | pctAdd / CRIT,FAR,LOW_ENERGY,PROJECTILE | 1处(DamagePipeline) | **边界** | chance 触发门 + magnitude 效果值（q-1 同构混合语义）；magnitude 应走 FIRE 效果自带数值 |
| 2 | KNOCKBACK | knockback | 1.0 | chance,inc / KNOCKBACK | 0处 | **边界** | chance 触发门 + magnitude 效果值（q-1 同构混合语义）；magnitude 应走 FIRE 效果自带数值 |
| 3 | BANNER_LINGER | banner_linger | 0 | add / BANNER,TAKEN,not | 1处(BannerAction) | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 |
| 4 | CORPSE_RETAIN_CHANCE | corpse_retain_chance | 0 | add / CORPSE | 1处(CorpseState) | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 |
| 5 | CULLING_STRIKE_THRESHOLD | culling_strike_threshold | 1.0 | inc | 0处 | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 (孤儿无消费端) |
| 6 | FOUND_ITEM_NUMBER | found_item_number | 0 | pctAdd,valueMod | 0处 | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 (孤儿无消费端) |
| 7 | FOUND_ITEM_RARITY | found_item_rarity | 0 | pctAdd,sbMod,valueMod | 0处 | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 (孤儿无消费端) |
| 8 | MANA_TAKE_OF_DAMAGE | mana_take_of_damage | 0.0 | pctAdd / MANA_TAKE | 3处(StatTag;EntityGen;DamagePipeline) | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 |
| 9 | PUPPET_MASTER_DURATION | puppet_master_duration | 0.0 | add / puppet_master | 0处 | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 (孤儿无消费端) |
| 10 | PUPPET_MASTER_EFFECT | puppet_master_effect | 0.0 | add / puppet_master | 1处(BuffStatusTypes) | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 |
| 11 | RECENTLY_CONSUMED_DAMAGE | recently_consumed_damage | 0 | add / CORPSE | 1处(BuffStatusTypes) | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 |
| 12 | THORNS | thorns | 1.0 | inc / PHYSICAL | 0处 | **FIRE-候选** | 事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值 (孤儿无消费端) |
| 13 | ACCURACY | accuracy | 15 | chance,inc,sbMod,valueMod | 2处(EntityGen;DamageHelper) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 14 | ADDED_DAMAGE | added_damage | 0 | add,chance,valueMod / ARMOR,CHAOS,COLD,ELEMENTAL,FIRE,LIGHTNING,PHYSICAL,thorns | 1处(DamageHelper) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 15 | AFTERSHOCK_CHANCE | aftershock_chance | 0.0 | chance / FORM,MACE,slam | 5处(AreaDeliveryExecutor;MinorNodes14;MinorNodes18;NotableNodes01;NotableNodes06) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 16 | AILMENT_CHANCE | ailment_chance | 0.0 | chance,pctAdd,valueMod / AILMENT,BLEED,CHILL,FREEZE,IGNITE,MAIM,ONE_HANDED,POISON,SHOCK | 2处(EntityGen;AilmentHelper) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 17 | CHANCE_DEFEND_WITH_ARMOUR | chance_defend_with_armour | 0.0 | chance | 1处(DamagePipeline) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 18 | CHARM_ACTIVATION_CHANCE | charm_activation_chance | 0.0 | chance / CHARM | 1处(CharmEngine) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 19 | DODGE_CHANCE | dodge_chance | 0.0 | chance / DODGE | 1处(DamagePipeline) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 20 | FLASK_NO_CONSUME_CHANCE | flask_no_consume_chance | 0.0 | chance / FLASK | 1处(FlaskHelper) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 21 | GLORY | glory | 0.0 | chance / GLORY | 0处 | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 22 | REMNANT_EXTRA_CHANCE | remnant_extra_chance | 0.0 | chance / REMNANT | 1处(RemnantState) | **FOLD-KEEP*** | chance=Stat值(q-1)：fold 聚合触发几率 |
| 23 | ADD_EMPOWERED_ATTACKS | empowered_attacks | 0 | add,pctAdd / TAKEN,WARCRY,not | 1处(SkillEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 24 | AFTERSHOCK_AREA | aftershock_area | 1.0 | inc / slam | 2处(AreaDeliveryExecutor;NotableNodes01) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 25 | AILMENT_BUILDUP | ailment_buildup | 1.0 | inc,sbMod,valueMod / AILMENT,ELECTROCUTED,FREEZE,IGNITE,PIN,SPELL,STUN | 2处(EntityGen;AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 26 | AILMENT_DAMAGE_CONTRIBUTION | ailment_damage_contribution | 0.0 | (无接线) | 1处(AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 27 | AILMENT_DAMAGE_RATE | ailment_damage_rate | 1.0 | inc,valueMod / AILMENT,BLEED,DAMAGE_OVER_TIME,IGNITE,POISON | 1处(AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 28 | AILMENT_DURATION | ailment_duration | 1.0 | inc,valueMod / AILMENT,ARCHON,BANNER,BEAST,BLEED,CHARGE,CHARM,CHILL,CURSE,DAMAGE_OVER_TIME,DAZE,FLASK,FREEZE,GRENADE,HAZARD,IGNITE,INFUSION,JAGGED_GROUND,OFFERING,PARRY,PIN,POISON,SHOCK,STUN,TAKEN,not | 6处(CreateGroundAction;CreateGroundOnAilmentEffect;StatusEngine;AilmentHelper;DamagePipeline;CharmEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 29 | AILMENT_MAGNITUDE | ailment_magnitude | 1.0 | add,inc,more,sbMod,valueMod / AILMENT,ARCANE_SURGE,ARCHON,AURA,BANNER,BLEED,BLIND,CHILL,CURSE,DAMAGE_OVER_TIME,DAZE,EXPOSURE,FIRE,IGNITE,INCISION,INFUSION,JAGGED_GROUND,MARK,NON_DAMAGING,OFFERING,PARRY,POISON,SHOCK,SLOW,SPELL,TAKEN,WARCRY,WITHERED,not | 8处(BannerAction;CreateGroundAction;CreateGroundOnAilmentEffect;StatusBehavior;StatusEngine;AilmentHelper;DamagePipeline;SkillEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 30 | AILMENT_THRESHOLD | ailment_threshold | 0.0 | (无接线) | 3处(StatusEngine;EntityGen;AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 31 | AILMENT_THRESHOLD_ADAPTATION | ailment_threshold_adaptation | 1.0 | (无接线) | 1处(StatusEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 32 | AILMENT_THRESHOLD_ADD | ailment_threshold_add | 0.0 | (无接线) | 1处(AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 33 | AMOUNT_LEECHED | amount_leeched | 1.0 | (无接线) | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 34 | ARCHON_RECOVERY_RATE | archon_recovery_rate | 1.0 | inc / ARCHON | 1处(StatusEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 35 | ARMOR_BREAK | armor_break | 0.0 | (无接线) | 2处(EntityGen;DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 36 | AVOID_AILMENT_CHANCE | avoid_ailment_chance | 0 | (无接线) | 1处(AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 37 | BASE_ENERGY_SHIELD_RECHARGE | base_energy_shield_recharge | 1.0 | (无接线) | 1处(EnergyShieldHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 38 | BLOCK_CHANCE | block_chance | 0 | add,pctAdd,valueMod | 2处(StateCalculator;BlockHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 39 | CAST_SPEED | cast_speed | 1 | inc,sbMod,sbModC / AREA,ATTACK,BANNER,CURSE,MARK,MINION,SEAL,TOTEM,TRAP,WARCRY | 10处(SkillCast;BuffStatusTypes;StatusBehavior;EntityGen;AttributeEvent;GenEvent;ElderGuardianMixin;BlazeAttackGoalMixin;ShulkerAttackGoalMixin;GuardianMixin) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 40 | CHARM_CHARGES_GAINED | charm_charges_gained | 1.0 | inc / CHARM | 1处(CharmEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 41 | CHARM_CHARGES_USED | charm_charges_used | 1.0 | inc / CHARM | 1处(CharmEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 42 | CHARM_EFFECT | charm_effect | 1.0 | inc / CHARM | 1处(CharmStatusTypes) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 43 | CORRUPTION_ENDURES_CHANCE | corruption_endures_chance | 0.0 | (无接线) | 2处(NotableNodes02;DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 44 | CRIT_CHANCE | crit_chance | 1.0 | add,inc,sbModC / ATTACK,CRIT,FORM,HERALD,MINION,WYVERN | 3处(StateCalculator;EntityGen;DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 45 | CRIT_DAMAGE | crit_damage | 1.0 | add,inc,sbModC,valueMod / ATTACK,CRIT,FORM,SPELL,WYVERN | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 46 | CROSS_BOW_RELOAD_TIME | cross_bow_reload_time | 1.0 | inc / CROSSBOW | 1处(ProjectileInEnchant) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 47 | CURRENT_ENERGY_SHIELD_RECHARGE | current_energy_shield_recharge | 1.0 | (无接线) | 1处(EnergyShieldHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 48 | DAMAGE | damage | 1.0 | entityMod,inc,more,sbMod,sbModC,valueMod / AREA,ATTACK,AXE,BALLISTA,BEAR,BOW,CHAOS,COLD,COMPANION,CROSSBOW,DAMAGE,ELEMENTAL,EMPOWERED,FIRE,FORM,GRENADE,HAZARD,HERALD,LIGHTNING,MACE,MELEE,MINION,ONE_HANDED,PARRY,PHYSICAL,PROJECTILE,SPEAR,SPELL,SWORD,TAKEN,TOTEM,TRAP,TWO_HANDED,WOLF,WYVERN,not | 4处(StatusBehavior;EntityGen;DamageHelper;DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 49 | DAMAGE_OVER_TIME_MULTIPLIER | damage_over_time_multiplier | 1.0 | inc / DAMAGE_OVER_TIME | 1处(StatusBehavior) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 50 | DAMAGE_TAKEN_BYPASSES_ENERGY | damage_taken_bypasses_energy | 1.0 | inc / ENERGY_SHIELD,TAKEN | 1处(DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 51 | DARKNESS_RESERVATION_LASTS | darkness_reservation_lasts | 1.0 | (无接线) | 1处(DarknessHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 52 | DEFLECTION_RATING | deflection_rating | 1.0 | inc / DEFLECT,SURROUNDED | 1处(BlockHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 53 | DEXTERITY | dexterity | 0 | more,valueMod | 5处(RequirePreset;Require;TalentNodeGen;AffixGenerator;TooltipEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 54 | ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING | elemental_damage_contributes_bleeding | 1.0 | (无接线) | 1处(AilmentHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 55 | ENERGY_RECHARGE | energy_recharge | 1.0 | valueMod / ENERGY | 1处(EnergyShieldHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 56 | EVASION | evasion | 53 | inc,sbMod,valueMod | 2处(StateCalculator;DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 57 | FLASK_CHARGES_GAINED | flask_charges_gained | 1.0 | inc / FLASK | 1处(FlaskHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 58 | FLASK_RECOVERY_RATE | flask_recovery_rate | 1.0 | inc / FLASK,HEALTH,MANA | 1处(FlaskHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 59 | GUARD_RECOVERY | guard_recovery | 0.0 | (无接线) | 1处(DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 60 | INTELLIGENCE | intelligence | 0 | more,pctAdd,valueMod | 5处(RequirePreset;Require;TalentNodeGen;AffixGenerator;TooltipEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 61 | ITEM_CAPACITY | item_capacity | 16 | (无接线) | 2处(CapacityHelper;AttributeEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 62 | LAUNCH_ADDITIONAL_PROJECTILE | launch_additional_projectile | 0 | valueMod / ARROW,BOW,CROSSBOW,PROJECTILE | 3处(ProjectileHelper;EntityGen;ProjectileInEnchant) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 63 | LEECH | leech | 0 | (无接线) | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 64 | LEECHES_RESPONSE | leeches_response | 0.20 | (无接线) | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 65 | LEECH_RATE | leech_rate | 1.0 | (无接线) | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 66 | MAX_DARKNESS | max_darkness | 1.0 | (无接线) | 1处(DarknessHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 67 | MAX_ENDURANCE_CHARGES | max_endurance_charges | 3 | add / ENDURANCE | 1处(ChargedState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 68 | MAX_ENERGY_SHIELD | max_energy_shield | 0 | more,valueMod | 3处(StateCalculator;EnergyShieldHelper;AttributeEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 69 | MAX_FRENZY_CHARGES | max_frenzy_charges | 3 | add / FRENZY | 1处(ChargedState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 70 | MAX_INFUSION | max_infusion | 1.0 | (无接线) | 1处(InfusionHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 71 | MAX_MANA | max_mana | 40 | valueMod | 2处(ManaHelper;AttributeEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 72 | MAX_POWER_CHARGES | max_power_charges | 3 | add / POWER | 1处(ChargedState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 73 | MAX_RAGE | max_rage | 50 | add,valueMod | 2处(RageHelper;AttributeEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 74 | MAX_REGENERATION | max_regeneration | 0.05 | valueMod / HEALTH,REGEN | 4处(EntityGen;EnergyShieldHelper;HealthHelper;ManaHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 75 | MAX_RESISTANCES | max_resistances | 0.75 | add,valueMod / CHAOS,COLD,FIRE,LIGHTNING | 3处(DamageTypeConvert;DamageAction;EntityGen) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 76 | MAX_SPIRIT | max_spirit | 0 | more,valueMod | 2处(StateCalculator;HealthValue) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 77 | MAX_VOLATILITY | max_volatility | 100.0 | valueMod / VOLATILITY | 1处(VolatilityHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 78 | META_ENERGY_MAX | meta_energy_max | 100 | inc / META | 3处(MetaEnergyState;HealthValue;ResourceEventHub) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 79 | NO_INHERENT_MANA_REGENERATION | no_inherent_mana_regeneration | 1.0 | (无接线) | 1处(ManaHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 80 | NO_SPIRIT_BASE_DARKNESS | no_spirit_base_darkness | 1.0 | (无接线) | 2处(DarknessHelper;ManaHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 81 | PARRY_RANGE | parry_range | 1.0 | inc / PARRY | 1处(DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 82 | PRESENCE_RANGE | presence_range | 6.0 | (无接线) | 1处(EntityOperationEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 83 | PROJECTILE_PIERCE | projectile_pierce | 0 | valueMod / PROJECTILE | 2处(SkillArrow;ArrowPiercing) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 84 | PROJECTILE_PIERCE_CHANCE | projectile_pierce_chance | 0 | add,pctAdd / PROJECTILE | 2处(SkillArrow;ArrowPiercing) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 85 | PROJECTILE_RANDOM_FIRE | projectile_random_fire |  | (无接线) | 1处(ProjectileInShoot) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 86 | PROJECTILE_SPEED | projectile_speed | 1.0 | inc / BOLT,PROJECTILE | 1处(ProjectileInShoot) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 87 | RAGE_LOSS_GRACE | rage_loss_grace | 0 | (无接线) | 1处(RageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 88 | RAGE_LOSS_RATE | rage_loss_rate | 1.0 | (无接线) | 1处(RageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 89 | RECOVERY_RATE | recovery_rate | 1.0 | inc | 4处(EnergyShieldHelper;HealthHelper;ManaHelper;FlaskHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 90 | REGENERATE_MANA_EQUAL_LIFE | regenerate_mana_equal_life | 1.0 | (无接线) | 1处(ManaHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 91 | REGENERATION_RATE | regeneration_rate | 1 | sbMod,valueMod / ENERGY,HEALTH,MANA,REGEN | 3处(EnergyShieldHelper;HealthHelper;ManaHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 92 | REMNANTS_COLLECTED_FURTHER_AWAY | remnants_collected_further_away | 1.0 | inc / REMNANT | 1处(RemnantState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 93 | REMNANT_DOUBLE_CHANCE | remnant_double_chance | 0.0 | (无接线) | 1处(RemnantState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 94 | REMNANT_MAGNITUDE | remnant_magnitude | 1.0 | (无接线) | 1处(RemnantState) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 95 | RESERVATION_EFFICIENCY | reservation_efficiency | 0.0 | pctAdd / RESERVATION | 1处(SpiritManager) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 96 | RESISTANCES | resistances | 0.0 | entityMod,pctAdd,sbMod,valueMod / CHAOS,COLD,ELEMENTAL,FIRE,LIGHTNING,MINION,RESISTANCE | 8处(MobsInfo;DamageTypeConvert;DamageAction;EquipmentValue;DebuffStatusTypes;EntityGen;AttributeEvent;GenEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 97 | RESISTANCE_PENETRATION | resistance_penetration | 0.0 | pctAdd / ATTACK,ELEMENTAL,GLORY,RESISTANCE | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 98 | SKILL_SLOT_COUNT | skill_slot_count | 9 | (无接线) | 2处(SkillAssembly;SkillScreen) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 99 | STATUS_EXTRA_STACKS | status_extra_stacks | 0.0 | (无接线) | 1处(StatusEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 100 | STATUS_MAX_STACKS | status_max_stacks | 0.0 | (无接线) | 1处(StatusEngine) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 101 | STRENGTH | strength | 0 | entityMod,more,valueMod / COMPANION | 5处(RequirePreset;Require;TalentNodeGen;AffixGenerator;TooltipEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 102 | TAKEN_CRIT_CHANCE | taken_crit_chance | 0.0 | sbMod | 1处(DamageHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 103 | TAKEN_CRIT_DAMAGE | taken_crit_damage | 1.0 | valueMod | 2处(EntityGen;DamageHelper.applyTakenCriticalDamage) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取（原仅 EntityGen 写入、无读取=死属性，2026-08-22 TAKEN类归一接消费端） |
| 104 | TAKEN_DAMAGE | taken_damage | 1.0 | sbMod,sbModC,valueMod / CHAOS,PHYSICAL | 2处(StatusBehavior;DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 105 | TAKE_NO_DAMAGE_VOLATILITY | take_no_damage_volatility | 1.0 | (无接线) | 1处(VolatilityDetonationManager) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 106 | TALENT_AUTO_SMELT | talent_auto_smelt | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 107 | TALENT_EXTRA_XP | talent_extra_xp | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 108 | TALENT_FORTUNE | talent_fortune | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 109 | TALENT_LOOTING | talent_looting | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 110 | TALENT_MAGNET | talent_magnet | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 111 | TALENT_MENDING | talent_mending | 0 | (无接线) | 1处(TalentPerkEvent) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 112 | AURA | aura | 0.0 | (无接线) | 2处(NotableNodes07;DamagePipeline) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取（原 TOTEM_TAKEN_AURA：字段名内嵌作用域，见 ATTRIBUTE_SCOPE_IN_NAME_AUDIT.md §5 改名闭环） |
| 113 | VOLATILITY_DETONATION_TIME | volatility_detonation_time | 5.0 | valueMod / VOLATILITY | 1处(VolatilityDetonationManager) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 114 | VOLATILITY_GENERATION | volatility_generation | 1.0 | valueMod / VOLATILITY | 1处(VolatilityHelper) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 115 | WORK_APTITUDE | work_aptitude | 0.0 | (无接线) | 1处(WorkSpeed) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 116 | WORK_SPEED | work_speed | 1.0 | (无接线) | 1处(WorkSpeed) | **FOLD-KEEP** | 连续倍率/上限/伤害减免，计算期按作用域读取 |
| 117 | COOLDOWN_RECOVERY_RATE | cooldown_recovery_rate | 1.0 | inc / COMMAND,GRENADE | 0处 | **待定(孤儿)** | 无消费端；需裁定 保留/转FIRE/删 |
| 118 | EFFECT_AREA | area_of_effect | 1.0 | entityMod,inc / AREA,MELEE,MINION | 0处 | **待定(孤儿)** | 无消费端；需裁定 保留/转FIRE/删 |
| 119 | GLORY_GENERATION | glory_generation | 1.0 | valueMod / GLORY | 0处 | **待定(孤儿)** | 无消费端；需裁定 保留/转FIRE/删 |
| 120 | STUN_RECOVERY | stun_recovery | 1.0 | inc / STUN | 0处 | **待定(孤儿)** | 无消费端；需裁定 保留/转FIRE/删 |

---

## §1 判定准则（详）

| 裁定 | 判定依据 | 是否依赖属性 | 例 |
|---|---|---|---|
| **FOLD-KEEP** | 连续倍率 / 资源上限 / 伤害减免 / 容量，计算期按作用域读取（`ModifierStatEffect` 折叠进 vanilla `Attribute`） | 是 | DAMAGE、RESISTANCES、MAX_*、EVASION、CRIT_CHANCE(非chance部分)、CAST_SPEED、TAKEN_DAMAGE |
| **FOLD-KEEP\*** | chance 类（q-1：几率=Stat值），fold 聚合触发几率，由消费端掷骰 | 是（仅几率） | DODGE_CHANCE、CHARM_ACTIVATION_CHANCE、AILMENT_CHANCE |
| **边界** | chance 触发门 + magnitude 效果值（q-1 同构混合语义）；chance→Stat值(fold)，magnitude→FIRE 效果自带数值 | 部分（chance 用，magnitude 不用） | DEFEND_WITH_ARMOUR、KNOCKBACK |
| **FIRE-候选** | 事件驱动一次性动作/倍率（命中/受击/击杀/消耗触发），值走 FIRE 效果（`entry.amount` 自带数值） | 否 | THORNS、CULLING_STRIKE_THRESHOLD、FOUND_ITEM_* |
| **待定(孤儿)** | 无字面 `AttributeRegister.X` 消费端引用；需裁定 保留/转FIRE/删（注：仅捕获字面引用，资源型如 GLORY 经 `ResourceType` 消费可能漏判，见 §3） | 视裁定 | COOLDOWN_RECOVERY_RATE、EFFECT_AREA、GLORY_GENERATION、STUN_RECOVERY |

**核心结论**：仅 `ModifierStatEffect`(FOLD) 强制依赖属性；其余 19 个 `StatEffect` 亚型均经 `entry.amount` 自带数值。故「不是所有 StatEffect 都要用属性」= 事件驱动机制应走 FIRE 效果，而非为倍率另建 FOLD 属性。

---

## §2 FIRE 候选详析（建议效果型 + 消费事件）

> 实施前需先核对 effect 包是否已有可复用亚型（如 `ResourceConvertStatEffect`/`RecoupStatEffect`/`DamageTakenConvertStatEffect`/`GrantOnEventStatEffect`/`StatusStatEffect`），避免重复建类。

| 属性 | 机制 | 建议 FIRE 效果型 | 消费事件/通道 | 处置 |
|---|---|---|---|---|
| THORNS | 受击反射物理伤害 | `ThornsStatEffect`（新建，tags={taken}） | TAKEN 事件 / `DamagePipeline` stageTaken | 删 THORNS 属性 |
| CULLING_STRIKE_THRESHOLD | 命中斩杀阈值 | `CullingStatEffect`（新建，tags={hit}） | HIT 事件 | 删属性（孤儿） |
| FOUND_ITEM_NUMBER / RARITY | 击杀掉落物数量/稀有度 | 复用 `LootStatEffect` 或 `GrantOnEventStatEffect`（tags={kill}） | KILL 事件 | 删属性（孤儿） |
| PUPPET_MASTER_DURATION | 傀儡师 buff 时长 | 复用 `StatusStatEffect`(aspect=duration) | 施加时 | 删属性（孤儿） |
| KNOCKBACK（边界） | 击退距离（chance 门+倍率） | `KnockbackStatEffect`（新建，tags={hit/taken_knockback}）承载距离；chance 门保留 fold | `LivingKnockBackEvent`→TAKEN_KNOCKBACK fire | 删 magnitude 属性，保留 chance 属性 |
| MANA_TAKE_OF_DAMAGE | 受伤转魔力比例 | 复用 `DamageTakenConvertStatEffect` 或 `ResourceConvertStatEffect` | TAKEN 事件 | 改走 FIRE（已有消费端 DamagePipeline） |
| CORPSE_RETAIN_CHANCE | 消耗尸体不摧毁几率 | 复用 `ChanceOnEventStatEffect`（tags={corpse_consumed}） | CORPSE 消耗事件 | 改走 FIRE |
| RECENTLY_CONSUMED_DAMAGE | 近期消耗尸体攻击增伤 | `GrantOnEventStatEffect` / `StatusStatEffect` | CORPSE 消耗事件 | 改走 FIRE |
| BANNER_LINGER | 战旗离开后增益残留 | `StatusStatEffect`（duration 延长） | BANNER 离开事件 | 改走 FIRE |
| PUPPET_MASTER_EFFECT | 傀儡师效果强度 | `StatusStatEffect`(aspect=magnitude) | 施加时 | 改走 FIRE |

**边界项（DEFEND_WITH_ARMOUR）**：magnitude「X% of Armour」减伤 = 伤害期条件减免，与 Dodge/Guard 同构（FOLD 读取自然）；但 q-1 明确「倍率=Effect数值」。两读均可，待你裁定：① 维持 FOLD（与管线一致，最小改动）；② 改为 `DefendStatEffect`(FIRE) 承载 magnitude，删 DEFEND_WITH_ARMOUR 属性。

---

## §3 待定孤儿处置（待裁定）

| 属性 | 现状 | 建议 |
|---|---|---|
| COOLDOWN_RECOVERY_RATE | 连续倍率 | **已落地 FOLD-KEEP**：`SkillContext.setCooldown` 读全局倍率，有效冷却 = cooldown ÷ rate |
| EFFECT_AREA | 范围倍率 | **已落地 FOLD-KEEP**：`AreaDeliveryExecutor` 投送半径 × 倍率（{AREA,MELEE,MINION} 作用域） |
| GLORY_GENERATION | 荣耀生成速率 | **已落地 FOLD-KEEP**：`GloryState.generate` 读 {GLORY} 作用域缩放生成增量 |
| STUN_RECOVERY | 眩晕恢复速率 | **已落地 FOLD-KEEP**：`AilmentHelper` 轻/重晕时长 ÷ rate（{STUN} 作用域） |

> 这 4 项均为**连续倍率**而非事件动作，按裁定接消费端（FOLD-KEEP），不转 FIRE。均已通过 compileJava → runData → grep 三道门禁。

---

## §4 执行顺序（待你裁定后实施）

1. **边界项**：DEFEND_WITH_ARMOUR / KNOCKBACK 的 magnitude 改 FIRE 效果，删 magnitude 属性；chance 保留 fold。
2. **FIRE 候选·孤儿优先**：THORNS、CULLING_STRIKE_THRESHOLD、FOUND_ITEM_*、PUPPET_MASTER_DURATION → 建效果 + 消费端，删属性。
3. **FIRE 候选·有消费端**：MANA_TAKE_OF_DAMAGE、CORPSE_RETAIN_CHANCE、RECENTLY_CONSUMED_DAMAGE、BANNER_LINGER、PUPPET_MASTER_EFFECT → 改走 FIRE（复用现有效果型）。
4. **待定孤儿**：COOLDOWN_RECOVERY_RATE / EFFECT_AREA / GLORY_GENERATION / STUN_RECOVERY → 接消费端（FOLD-KEEP）。

**验收门禁**（每步）：`compileJava` → `runData` → `grep` 无孤儿 `AttributeRegister.X` 残留（R1/R2/R3 沿用）。

---

## §5 执行闭环（2026-08-22：应用「并非所有 StatEffect 都要用属性」原则）

按原则对 120 属性逐条复核，结论：**仅 THORNS / CULLING_STRIKE_THRESHOLD 为真正事件驱动、应转 FIRE**；
原审计把 8 个「连续倍率 / chance」属性误标 FIRE-候选——它们本质是计算期按作用域读取的 FOLD，转 FIRE 反而破坏既有系统。

### §5.1 已落地（FIRE，删属性）
| 属性 | 机制 | FIRE 效果 | 触发 | 消费端 |
|---|---|---|---|---|
| THORNS | 受击反射物理伤害 | 复用 `DamageStatEffect`（type=damage） | `taken_hit` | 随既有 TAKEN_HIT fire 自触发，对攻击者(triggerTarget)造成伤害 |
| CULLING_STRIKE_THRESHOLD | 命中伤害≥阈值%最大生命即斩杀 | 新建 `CullingStatEffect`（type=culling，已登记 `StatEffectTypes`） | `caused_hit` | 随 CAUSED_HIT fire 自触发；阈值读 `tc.takenPreBlock()` 比 `amount×maxHealth` |

- `AttributeRegister.THORNS` / `CULLING_STRIKE_THRESHOLD` 已删除（R2 合规：非「未实现」，已转为 FIRE 效果承载数值）。
- `StatGen` 三处 `inc(...)` 改为 `DamageStatEffect.entry(...)` / `CullingStatEffect.entry(...)`（值走 `$value` 模板，自带数值）。
- 三道门禁：`compileJava` ✅ / `runData` ✅（3 个 stat_entry 再生）/ `grep` ✅（无 `AttributeRegister.THORNS|CULLING_STRIKE_THRESHOLD` 残留）。

### §5.2 复核纠正（保留 FOLD，原审计误标）
| 属性 | 原裁定 | 纠正裁定 | 理由 |
|---|---|---|---|
| CORPSE_RETAIN_CHANCE | FIRE-候选 | **FOLD-KEEP\*** | chance 类（q-1：几率=Stat值），fold 聚合；非一次性动作 |
| MANA_TAKE_OF_DAMAGE | FIRE-候选 | **FOLD-KEEP** | 连续减免（Mind Over Matter），DamagePipeline/EntityGen 按 scope 读取；转 FIRE 破坏核心机制 |
| BANNER_LINGER | FIRE-候选 | **FOLD-KEEP** | 连续时长倍率，`BannerAction` fold 读取 |
| RECENTLY_CONSUMED_DAMAGE | FIRE-候选 | **FOLD-KEEP** | 连续 buff 幅度，`BuffStatusTypes` fold 读取 |
| PUPPET_MASTER_EFFECT | FIRE-候选 | **FOLD-KEEP** | 连续 buff 幅度，`BuffStatusTypes` fold 读取 |
| PUPPET_MASTER_DURATION | FIRE-候选 | **FOLD-KEEP** | 连续时长，作 `GrantOnEventStatEffect` durationAttr 缩放（不可移除） |

### §5.3 边界（保留 FOLD，待机制就绪再拆）
| 属性 | 裁定 | 理由 |
|---|---|---|
| DEFEND_WITH_ARMOUR | 保留 FOLD | 幅度=伤害期条件减免，与 Dodge/Guard 同构（FOLD 自然）；`stageDefend` 已接消费端 |
| KNOCKBACK | 保留 FOLD | chance+magnitude 单一机制，`StatEngine.fold` 读取；拆 magnitude 至 FIRE 需新建击退施加消费端（MoveAction 走技能，无独立击退消费机），留待后续 |

### §5.4 待定孤儿（保留 FOLD 数据先行）
COOLDOWN_RECOVERY_RATE / EFFECT_AREA / GLORY_GENERATION / STUN_RECOVERY——**均已接消费端（FOLD-KEEP），见 §3 落地记录**，通过三道门禁。

FOUND_ITEM_NUMBER/RARITY（事件驱动掉落，需 loot-drop hook 才能转 FIRE `LootStatEffect`）——**已接消费端（FOLD-KEEP）于 `TalentPerkEvent.onLivingDrops`**：
- `FOUND_ITEM_NUMBER`：掉落数量倍率（规范效果，读击杀者属性缩放额外掉落数）。
- `FOUND_ITEM_RARITY`：暂以「额外掉落数量」近似（掉落稀有度重投需 loot-rarity 钩子；`LootStatEffect` 尚未实现），保持属性被消费、不污染既有物品；转 FIRE `LootStatEffect` 待基础设施就绪后再裁定。

> 二者均已非孤儿；R2（不删未实现属性）满足。RARITY 的规范稀有度重投为后续 backlog。

## §6 属性注册纪律收紧（2026-08-22 续：q-0 + 功能属性清理 + 抗性/格挡统一）

### §6.1 q-0 轴确认：触发几率在 Effect 上计算
用户裁定：**触发型几率属于「Effect/通道」职责，不单独建属性**（q-0）。据此重做 Defend 守护技：
- **删除 `CHANCE_DEFEND_WITH_ARMOUR`**（Stat 值属性 + StatGen `chance(CHANCE_TO_DEFEND_WITH_OF_ARMOR)` 接线 + `stageDefend` 掷骰）。
- `DamagePipeline.stageDefend` 改为**通道门控**：命中且满足 `{crit}/{projectile}/{far}/{low_energy}` 条件即承护，几率由「是否装备 Defend 守护技」通道掌管（support gem 链路生效即承护），不再掷骰独立几率属性。
- 幅度维度仅留 **`DEFEND_WITH_ARMOUR`**（按条件作用域 pctAdd 折叠）；原「magnitude + chance 双属性」反模式消除。

### §6.2 移除 4 个功能/中转换计算属性（均为死属性）
经核实这些属性**无任何 StatGen 接线**（StatIds 未接到 AttributeRegister），消费端 `>1.0` 分支恒不触发（默认 1.0），移除即回落默认行为，**不改变当前运行表现**：
| 属性 | 原消费端 | 处置 |
|---|---|---|
| `NO_INHERENT_MANA_REGENERATION` | ManaHelper.getManaRegeneration（固有再生开关） | 删属性 + 删分支，固有再生常开 |
| `NO_SPIRIT_BASE_DARKNESS` | DarknessHelper.isDarknessEnabled / ManaHelper.getSpirit | 删属性；`isDarknessEnabled` 返回 false，getSpirit 直返 SPIRIT |
| `REGENERATE_MANA_EQUAL_LIFE` | ManaHelper.getManaRegeneration（等生命再生） | 删属性 + 删分支 |
| `ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING` | AilmentHelper.getBleedingDamage（元素贡献流血） | 删属性 + 删分支 |

> 这类属性属「功能性开关 / 派生转换」，不符合归一属性特征（q-0：功能性不建 FOLD 属性），按用户「严格约束，不必保留」裁定删除。

### §6.3 抗性/格挡家族统一
- **新增 `MAX_BLOCK`**（§B，默认 0.75），与 `MAX_RESISTANCES` 同族。
- `BlockHelper.getBlock` 钳制 `BLOCK_CHANCE → MAX_BLOCK`（对齐 `DamageTypeConvert` 对 `RESISTANCES/MAX_RESISTANCES` 的 `Math.min(res, max)` 钳制范式）。
- `BLOCK_CHANCE` 去掉注册硬上限 0.75（改开放范围 `registerPercentage("block_chance", 0.0)`），上限改由 `MAX_BLOCK` 在消费端钳制。
- `TO_MAX_BLOCK_CHANCE` 改接 `MAX_BLOCK`（原误接 `BLOCK_CHANCE`），对齐 `ADD_MAX_RESISTANCES → MAX_RESISTANCES`。
- `LangProvider` 补 `attribute.name.max_block`（最大格挡）。

### §6.4 门禁
- `compileJava` ✓ / `runData` ✓ / `grep` 无孤儿（`AttributeRegister` 已删 5 属性；仅 `AttributeRegister.java.pre_annotate` / `.new` 备份残留引用，非编译源，无害）。

### §6.5 TAKEN 类归一（2026-08-22）
承伤侧 §H 家族已对称（攻击 `CRIT_CHANCE`/`CRIT_DAMAGE` ↔ 承伤 `TAKEN_CRIT_CHANCE`/`TAKEN_CRIT_DAMAGE`），本次补齐唯一缺口：

- **死属性闭环 `TAKEN_CRIT_DAMAGE`**：原仅 `EntityGen`（boss `setAttribute(-0.8)`）与后缀词 `TAKEN_CRIT_DAMAGE_INC_MOD` 写入，**全工程无 `readInScope(...TAKEN_CRIT_DAMAGE)` 消费端**——语义漂移/R2 孤儿。
  - `DamageHelper` 新增 `applyTakenCriticalDamage(victim, damages)`：以 `{taken, crit}` 作用域读承伤方 `TAKEN_CRIT_DAMAGE` 乘入暴击伤害（与 `TAKEN_CRIT_CHANCE` 跨实体读取对称；`Crit` 维度独立于攻击方 `CRIT_DAMAGE`）。
  - `DamagePipeline.stageAilmentAndCrit` 对所有 `crit` 命中调用（技能源不重复乘攻击方倍率，受害者倍率恒生效）。
- **后缀词标签归一**：`TAKEN_CRIT_DAMAGE_INC_MOD` 标签 `List.of(DAMAGE, CRIT)` → `List.of(StatTag.TAKEN, CRIT)`，对齐受害侧 `{taken,*}` 家族约定（原 `{damage,crit}` 为攻击侧标签泄漏）。
- **javadoc 校正**：`TAKEN_CRIT_CHANCE` 原标注 `{taken,crit}` 不实（实为全局条目经 `StatTag.TAKEN_CRIT` 读取），已改准确；`TAKEN_CRIT_DAMAGE` 标注 `{taken,crit}` 与新增消费端一致。

> TAKEN 类其余属性（TAKEN_DAMAGE 全局+元素作用域、DAMAGE_TAKEN_BYPASSES_ENERGY、TAKEN_CRIT_CHANCE）均已规范接线，无孤儿，本轮无需改动。

### §6.6 门禁（TAKEN 类归一）
- `compileJava` ✓ / `runData` ✓ / `grep` 无孤儿（`TAKEN_CRIT_DAMAGE` 消费端 `applyTakenCriticalDamage` 已接）。

## §7 常驻机制转 INERT Effect（2026-08-22 续：CORRUPTION / DEFEND 移除转 Effect 实现）

用户裁定（q-1）：DEFEND / AURA / CORRUPTION「移除转 Effect 实现，但现在的属性实现也不一定为对的，需要查找源头确认语意」。本轮在 AURA keystone 已转 `PresenceAuraStatEffect`（AURA 标记，经 STAT_STORE 直读）之后，补齐另两条常驻机制：

### §7.1 免死几率 `CORRUPTION_ENDURES_CHANCE` → `CorruptionEnduresStatEffect`
- **源头**：POE2 49769 corruption_endures keystone「7% chance to Avoid Death from Hits」。原模组以属性 `CORRUPTION_ENDURES_CHANCE`（备注「免死几率」）承载，**违反纪律 #2（禁裸属性 + 硬编码消费端）**——免死为一次性掷骰机制，非连续 fold 维度。
- **处置**：删属性 + 删消费端 `readInScope(s.living, Set.of(StatTag.CORRUPTION), CORRUPTION_ENDURES_CHANCE)`；节点 49769 改经 `StatEntry.of(ExpressionCodec.of(0.07), SELF, [], new CorruptionEnduresStatEffect())` 授予（INERT 标记，`type="corruption_endures"`，无 extra 字段）。
- **消费端**：`DamagePipeline.stageHealth` 致死伤害结算前调 `corruptionEnduresChance(living)`——遍历持有者 `STAT_STORE.entries()`，`effect().left() instanceof CorruptionEnduresStatEffect` 则 `evaluateAmount(ExpressionContext.get(living))` 求和，对总几率掷骰压至保留 1 HP。
- **为何 INERT 而非 Buff**：`TalentEffectApplier` 仅 `removeByKeyPrefix` 移除 STAT_STORE 条目、不回滚 `StatusEngine` 施加的 `StatusInstance`；走 Buff 会在节点取消分配后仍按 duration 滞留（泄漏）。INERT 直读：取消分配即条目移除、免死立即失效（复用 AURA 验证过的无索引直接读范式）。

### §7.2 Defend 承护幅度 `DEFEND_WITH_ARMOUR` → `DefendStatEffect`（4 条件作用域变体）
- **源头**：POE2「Defend with {X}% of Armour [against Crit / Projectile / far / while not on Low Energy Shield]」。原模组以属性 `DEFEND_WITH_ARMOUR` 折叠幅度。
- **死属性发现**：grep 全 talent 生成目录无 `DEFEND` 引用、notable id（49963/23649/35356/66480）在生成代码中不存在 → **当前无任何节点授予**，`readInScope` 恒读 base 0（机制本就失效）。仍按 q-1 移除转 Effect，使 notable 接入后即可生效。
- **处置**：删属性 + 删 StatGen `pctAdd(..., DEFEND_WITH_ARMOUR, ...)`；`StatGen` 4 个 `DEFEND_WITH_OF_ARMOR_*` StatIds 改映射 `DefendStatEffect` 条目（SELF，amount `$value` 模板，conditionTags 分别 CRIT / PROJECTILE / FAR / LOW_ENERGY）。
- **JSON 键碰撞修复**：效果层初版 codec 用 `"conditions"` 作 conditionTags 键，但 `Stat` 条目层 codec 同样把 `conditions` 编为 `"conditions"` 键 → 同 JSON 对象层冲突。效果层键改为 `"against"`（`.optionalFieldOf("against", List.of())`）。
- **消费端**：`DamagePipeline.stageDefend` 命中后调 `defendMagnitude(living, cond)`——遍历 `STAT_STORE.entries()`，`fx instanceof DefendStatEffect d && cond.containsAll(d.conditionTags())` 则 `evaluateAmount` 求和；减伤 = 护甲 × 幅度（封顶本次承伤）。`cond` 由 `s.crit` / `s.tags⊇projectile` / 远距 / 非低能量实时构建（语义对齐「while not on Low Energy Shield」）。
- **为何 INERT**：同 §7.1，常驻增强节点取消分配即失效，无 Buff 残留。

> 旧 `stageDefend` javadoc 残留 `DEFEND_WITH_ARMOUR` 字样（幅度维度描述）已校正为 `DefendStatEffect` INERT 直读表述。

### §7.3 三道门禁（CORRUPTION / DEFEND 转 Effect）
- `compileJava` ✓（首次缺 `CorruptionEnduresStatEffect` 构造器 `TagExpr` 实参 → 补 `TagExpr.ALWAYS` 后 BUILD SUCCESSFUL）。
- `runData` ✓（Stat Entries / Talent Tree provider 全过）。
- `grep` 无孤儿：`AttributeRegister.DEFEND_WITH_ARMOUR` / `CORRUPTION_ENDURES_CHANCE` 仅存于 `[已归一]` 文档注释；`readInScopePublic` 残留引用均指向仍存在的属性。

### §7.4 范围维度澄清（PRESENCE_RANGE / EFFECT_AREA 两独立维度）
- `EFFECT_AREA`（`area_of_effect`，base 1.0，inc 生效）= **效果范围倍率**（AoE 幅度），§G 注释已澄清。
- `PRESENCE_RANGE`（`presence_range`，base 6.0）= **存在/威势半径（作用域）**，光环等仅作 `{presence}` 作用域，非倍率——与 `EFFECT_AREA` 为两个独立维度，不归并。
- `RECENTLY_CONSUMED_DAMAGE` 归一已落地：无同名属性，经 `ADD_RECENTLY_CONSUMED_DAMAGE` Stat（BuffStatusTypes 在 RECENTLY_CONSUMED 窗口内 inc `AttributeRegister.DAMAGE`）+ `ConsumedCorpseDamageStatEffect`（INERT 标记，26804/55405 节点经 STAT_STORE 存增伤点数）实现；「近期」为 Buff 窗口门控，属性名不内嵌条件（详见 §7.5）。
- **属性注册层已按 §A–§M 语义域重排**（原版兼容 / 资源上限 / 资源机制 / 属性点 / 异常 / 效果 / 核心战斗 / 承伤 / 抗性状态 / 偷取回复 / 投射物 / 天赋预留 / 数据先行），重排仅调整字段顺序与注解，注册键字符串不变，无行为影响。

### §7.5 增伤点数累加器 `CONSUMED_CORPSE_DAMAGE` → `ConsumedCorpseDamageStatEffect`（INERT，补一致性缺口）

- **用户裁定（续 §7）**：`CORRUPTION_ENDURES_CHANCE` / `DEFEND_WITH_ARMOUR` 已转 INERT Effect，但 `CONSUMED_CORPSE_DAMAGE`（消耗尸体存储增伤点数，base 0，ADD_VALUE）仍是**同类反模式**——一个伤害相关量（点数）存成裸属性，再被 `RECENTLY_CONSUMED` 增益 `StatEngine.fold` 读出转 `inc DAMAGE`。若只转前两者、留此属性，归一不一致。
- **原链路**：天赋节点 `INC_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY`（`add` 接线）→ `CONSUMED_CORPSE_DAMAGE` 累加点数 → 消耗尸体授 `RECENTLY_CONSUMED` 增益（80 tick）→ 窗口内 `BuffStatusTypes.RECENTLY_CONSUMED` statProvider `fold(CONSUMED_CORPSE_DAMAGE)` 得点数 → `ADD_RECENTLY_CONSUMED_DAMAGE`（inc DAMAGE）。
- **处置（对齐 §7.1/§7.2 INERT 范式）**：
  - 删 `CONSUMED_CORPSE_DAMAGE` 属性；`StatGen` 节点改经 `new Stat($value 模板, SELF, [], new ConsumedCorpseDamageStatEffect(TagExpr.ALWAYS))` 授予 INERT 条目（点数存 STAT_STORE）。
  - `BuffStatusTypes.RECENTLY_CONSUMED` statProvider 改 `consumedCorpseDamagePoints(living)`：遍历 `STAT_STORE.entries()`，`instanceof ConsumedCorpseDamageStatEffect` 则 `evaluateAmount(ExpressionContext.get(living))` 求和 → 仍经 `ADD_RECENTLY_CONSUMED_DAMAGE`（inc DAMAGE）换算。
  - `StatEffectTypes` 注册 `consumed_corpse_damage`；`CorpseState` 注释校正（不再提属性）。
- **为何 INERT 而非属性**：点数（伤害概念）落 STAT_STORE 而非属性层，与 DEFEND 同构——天赋取消分配即 INERT 条目移除、点数失效；增益窗口二次门控使增伤仅窗口内生效，无 Buff 残留。
- **runData 重生成（§21 铁律）**：节点 `inc_damage_if_you_have_consumed_a_corpse_recently.json` 由旧 `coe:consumed_corpse_damage` 属性引用重生成为 `"type":"consumed_corpse_damage"`（effect 引用，amount `$value`，target self）——验证无 stale 属性键残留。

### §7.6 三道门禁（CONSUMED_CORPSE_DAMAGE 转 Effect）
- `compileJava` ✓（首编 `BuffStatusTypes` 误引 `ExpressionContext` 路径 `codec.support.stat` → 修正为 `codec.support.math` 后 BUILD SUCCESSFUL）。
- `runData` ✓（written:44，Stat Entries / Talent Tree provider 全过；节点 JSON 重生成为 effect 引用）。
- `grep` 无孤儿：`CONSUMED_CORPSE_DAMAGE` 仅存 `[已归一]` 文档注释与新效果 javadoc（过去时）；生成 JSON 仅含 effect 类型串 `consumed_corpse_damage`，无 `coe:consumed_corpse_damage` 属性键。

## §8 TAKEN 侧彻底归一 + 全属性层反模式扫查（2026-08-22 续：用户指出「TAKEN 侧未统一 + 归一不完整」）

### §8.1 波动免伤 `TAKE_NO_DAMAGE_VOLATILITY` → `TakeNoVolatilityDamageStatEffect`（INERT）

- **用户裁定**：`TAKE_NO_DAMAGE_VOLATILITY` 不是普通属性接线，应当移除；「非要用也应该是 Stat 条目而不是一条属性」。即与 CORRUPTION/DEFEND/CONSUMED_CORPSE 同构——转 INERT StatEffect 的 Stat 条目，不建裸属性。
- **原反模式**：POE2「Take no Damage from Volatility」keystone（免疫波动引爆伤害）被存成 `registerPercentage("take_no_damage_volatility", 1.0)`，由 `VolatilityDetonationManager.detonate` 以
  `target.getAttributeValue(AttributeRegister.TAKE_NO_DAMAGE_VOLATILITY) > 1.0` **硬门控**消费——既绕过 Stat 系统 scope/fold，又 `StatGen` 无任何节点接线（死属性，类似当年 `DEFEND_WITH_ARMOUR`）。正是纪律 #2 禁止的「裸属性 + 硬编码消费端」。
- **处置（对齐 INERT 范式）**：
  - 删 `TAKE_NO_DAMAGE_VOLATILITY` 属性；`StatEffectTypes` 注册 `take_no_volatility_damage`。
  - 新建 `TakeNoVolatilityDamageStatEffect`（INERT，type=`"take_no_volatility_damage"`，无 extra 字段；幅度 = 条目 amount，1.0 = 100% 免疫）。
  - `StatIds` 补 `INC_TAKE_NO_DAMAGE_VOLATILITY = id("inc_take_no_damage_volatility")`；`StatGen` 照 DEFEND 样板接 `new Stat($value 模板, SELF, [], new TakeNoVolatilityDamageStatEffect(TagExpr.ALWAYS))`——**StatGen 卡点解除**（此前该 keystone 既无 StatId 也无映射）。
  - `VolatilityDetonationManager.detonate` 改 `takesNoVolatilityDamage(target)`：遍历目标 `STAT_STORE.entries()`，`instanceof TakeNoVolatilityDamageStatEffect` 则 `evaluateAmount(ExpressionContext.get(target))` 求和，随机 `< min(1.0, sum)` 则免伤；节点取消分配即条目移除、免伤失效（无 Buff 残留）。
- **为何 INERT**：免伤是离散 keystone 机制（非资源/战斗数值维度），落 STAT_STORE 经 INERT 直读，复用 AURA/CORRUPTION/DEFEND 验证过的无索引无泄漏范式。

### §8.2 全属性层反模式扫查结论（彻底性验证）

- 对 `src` 全量 `getAttributeValue(AttributeRegister.X)` 逐一甄别，结论：**`TAKE_NO_DAMAGE_VOLATILITY` 是属性层最后一个反模式**，现已归一。
- **合法保留的属性**（直接读 `getAttributeValue` 正确，它们是 Stat 系统的 fold 目标而非 effect）：
  - 资源上限/机制：`MAX_MANA` `MAX_ENERGY_SHIELD` `MAX_VOLATILITY` `MAX_SPIRIT` `META_ENERGY_MAX` `MAX_RAGE` `MAX_DARKNESS` `MAX_INFUSION` `MAX_RESISTANCES` `MAX_BLOCK` `ITEM_CAPACITY` `MAX_POWER/FRENZY/ENDURANCE_CHARGES` 等；
  - 属性点：`STRENGTH` `DEXTERITY` `INTELLIGENCE`；
  - 战斗维度：`EVASION` `ACCURACY` `BLOCK_CHANCE` `CRIT_CHANCE` `KNOCKBACK_CHANCE` `CAST_SPEED` `ATTACK_SPEED` `RESISTANCES` `AILMENT_MAGNITUDE` `AILMENT_DURATION` `EFFECT_AREA` `AFTERSHOCK_AREA` `PRESENCE_RANGE` 等。
- **走合法 fold 路径（非硬编码门控）的属性**（经 `StatEngine.fold` / `readInScope`，保留）：`CORPSE_RETAIN_CHANCE`（动作掷骰，符合 q-0）、`PUPPET_MASTER_EFFECT`（buff fold 读）、`LAUNCH_ADDITIONAL_PROJECTILE`、`BANNER_LINGER` 等。
- **已归一四例（INERT StatEffect，反模式闭环）**：`CORRUPTION_ENDURES_CHANCE`→`CorruptionEnduresStatEffect`；`DEFEND_WITH_ARMOUR`→`DefendStatEffect`（4 条件）；`CONSUMED_CORPSE_DAMAGE`→`ConsumedCorpseDamageStatEffect`；`TAKE_NO_DAMAGE_VOLATILITY`→`TakeNoVolatilityDamageStatEffect`。
- **归一铁律（固化）**：凡属性语义为「某承伤/伤害修饰机制（免死/承护/免伤/增伤点数）」且消费端是引擎/manager/增益（非战斗属性 fold），一律转 INERT StatEffect 存 STAT_STORE；资源/战斗数值维度维持属性 + fold 正路。

### §8.3 三道门禁（TAKEN 侧 / 波动免伤 转 Effect）
- `compileJava` ✓（BUILD SUCCESSFUL，20s；无新增导入错误）。
- `runData` ✓（BUILD SUCCESSFUL，52s；Stat Entries / Talent Tree provider 全过；无 `take_no_damage_volatility` stale 属性键，生成 JSON 无引用）。
- `grep` 无孤儿：`TAKE_NO_DAMAGE_VOLATILITY` 仅存 `[已归一]` 文档注释 + 新效果 javadoc（过去时）；`INC_TAKE_NO_DAMAGE_VOLATILITY` / `TakeNoVolatilityDamageStatEffect` 已在 StatIds/StatGen/StatEffectTypes 接线。
