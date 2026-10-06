# StatIds 按描述去重扫描
总常量数: 676 | StatGen 已注册: 127 | 孤儿(无StatGen): 549

## A. 描述相同聚类（>1 成员）

### 「3% increased movement speed」 ×3
  - `INC_MOVEMENT_SPEED` (json=inc_movement_speed) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_BLEED_DURATION` (json=inc_bleed_duration) refs=1(ext=0)
    sig=<<无StatGen>>  - `MINION_MOVEMENT_SPEED` (json=minion_movement_speed) refs=1(ext=0)
    sig=<<无StatGen>>
### 「5% chance to daze on hitdamage」 ×3
  - `CHANCE_DAZE_ON_HIT` (json=chance_daze_on_hit) refs=1(ext=0)
    sig=ChanceOnEventStatEffect.entry(DebuffStatusTypes.DAZE, 0.0, Set.of(EventName.CAUSED_HIT))  - `CONVERT_EXTRA_PHYS_TO_COLD` (json=convert_extra_phys_to_cold) refs=1(ext=0)
    sig=DamageConvertStatEffect.entry(StatTag.PHYSICAL, StatTag.COLD, DamageConvertStatEffect.Mode.GAIN, 0.0)  - `CONVERT_EXTRA_PHYS_TO_LIGHTNING` (json=convert_extra_phys_to_lightning) refs=1(ext=0)
    sig=DamageConvertStatEffect.entry(StatTag.PHYSICAL, StatTag.LIGHTNING, DamageConvertStatEffect.Mode.GAIN, 0.0)
### 「maximum life is 1 immune to chaos damage and bleeding」 ×3
  - `CHAOS_INOCULATION_IMMUNE_BLEED` (json=chaos_inoculation_immune_bleed) refs=1(ext=0)
    sig=ImmuneStatEffect.entry(AilmentStatusTypes.BLEED)  - `CHAOS_INOCULATION_IMMUNE_CHAOS` (json=chaos_inoculation_immune_chaos) refs=1(ext=0)
    sig=DamageConvertStatEffect.entry(Set.of(StatTag.CHAOS), DamageConvertStatEffect.Mode.NULLIFY, 1.0, false, TagExpr.ALWAYS)  - `CHAOS_INOCULATION_MAX_HEALTH` (json=chaos_inoculation_max_health) refs=1(ext=0)
    sig=new Stat(ExpressionCodec.of(1.0), List.of(), StatTargets.EMPTY, new ModifierStatEffect(Attributes.MAX_HEALTH, Either.left(AttributeModifier.Operation.ADD_VALUE) …
### 「damage penetration 18% cold resistance」 ×3
  - `INC_AILMENT_CHANCE_VS_EXPOSED_COLD` (json=inc_ailment_chance_vs_exposed_cold) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_AILMENT_CHANCE_VS_EXPOSED_FIRE` (json=inc_ailment_chance_vs_exposed_fire) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING` (json=inc_ailment_chance_vs_exposed_lightning) refs=1(ext=0)
    sig=<<无StatGen>>
### 「damage penetration 18% fire resistance」 ×3
  - `INC_AILMENT_DURATION_VS_EXPOSED_COLD` (json=inc_ailment_duration_vs_exposed_cold) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_AILMENT_DURATION_VS_EXPOSED_FIRE` (json=inc_ailment_duration_vs_exposed_fire) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING` (json=inc_ailment_duration_vs_exposed_lightning) refs=1(ext=0)
    sig=<<无StatGen>>
### 「gain {x}% of cold damage as extra lightning damage」 ×2
  - `GAIN_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` (json=gain_of_cold_damage_as_extra_lightning_damage) refs=1(ext=0)
    sig=DamageConvertStatEffect.entry(StatTag.COLD, StatTag.LIGHTNING, DamageConvertStatEffect.Mode.GAIN, 0.0)  - `EXTRA_LIGHTNING_FROM_COLD` (json=extra_lightning_from_cold) refs=2(ext=1)
    sig=<<无StatGen>>
### 「5% chance to inflict bleeding on hitdamage」 ×2
  - `ADD_AILMENT_CHANCE_BLEED` (json=add_ailment_chance_bleed) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_DAMAGE_EMPOWERED_ATTACKS` (json=inc_damage_empowered_attacks) refs=1(ext=0)
    sig=<<无StatGen>>
### 「+10 to intelligence」 ×2
  - `ADD_INTELLIGENCE` (json=add_intelligence) refs=2(ext=1)
    sig=<<无StatGen>>  - `RECOVER_PCT_HEALTH_ON_KILL` (json=recover_pct_health_on_kill) refs=1(ext=0)
    sig=<<无StatGen>>
### 「+8% surpasschance chance to fire an additional projectile」 ×2
  - `ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW` (json=add_launch_additional_projectile_bow) refs=2(ext=1)
    sig=<<无StatGen>>  - `DELIVERY_EXTRA_PROJECTILE` (json=delivery_extra_projectile) refs=1(ext=0)
    sig=DeliveryStatEffect.entry("projectile", "additional_projectiles", ExpressionCodec.of(MathParser.parse("$value")), TagExpr.ALWAYS)
### 「+1 to maximum frenzy charges」 ×2
  - `ADD_MAX_FRENZY` (json=add_max_frenzy) refs=2(ext=1)
    sig=<<无StatGen>>  - `GRANT_FRENZY_CHARGE_ON_GAIN` (json=grant_frenzy_charge_on_gain) refs=1(ext=0)
    sig=GrantOnEventStatEffect.chargeEntry(ChargeType.FRENZY, 0.0, Set.of("gain_frenzy"))
### 「+1 to maximum power charges」 ×2
  - `ADD_MAX_POWER` (json=add_max_power) refs=2(ext=1)
    sig=<<无StatGen>>  - `GRANT_POWER_CHARGE_ON_GAIN` (json=grant_power_charge_on_gain) refs=1(ext=0)
    sig=GrantOnEventStatEffect.chargeEntry(ChargeType.POWER, 0.0, Set.of("gain_power"))
### 「10% increased mana recovery from flasks」 ×2
  - `FLASK_MANA_RECOVERY` (json=flask_mana_recovery) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_FLASK_MANA_RECOVERY` (json=inc_flask_mana_recovery) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% increased projectile damage」 ×2
  - `INC_ATTACK_DAMAGE` (json=inc_attack_damage) refs=3(ext=2)
    sig=<<无StatGen>>  - `INC_PROJECTILE_DAMAGE` (json=inc_projectile_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「aura skills have 5% increased buffmagnitude」 ×2
  - `INC_BUFF_MAGNITUDE` (json=inc_buff_magnitude) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_AURA_MAGNITUDE` (json=inc_aura_magnitude) refs=1(ext=0)
    sig=<<无StatGen>>
### 「each totem applies 2% increased damage taken to enemies in their presence」 ×2
  - `INC_DAMAGE_PRESENCE_ENEMIES` (json=inc_damage_presence_enemies) refs=2(ext=1)
    sig=<<无StatGen>>  - `TOTEM_RECOUP_HEALTH` (json=totem_recoup_health) refs=1(ext=0)
    sig=new Stat(ExpressionCodec.of(MathParser.parse("$value")), List.of(), StatTargets.ofTarget(TargetSelector.TOTEMS), new RecoupStatEffect(Set.of(StatTag.HEALTH), Ta …
### 「12% increased grenade damage」 ×2
  - `INC_GRENADE_DAMAGE` (json=inc_grenade_damage) refs=2(ext=1)
    sig=<<无StatGen>>  - `GRENADE_DAMAGE` (json=grenade_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「16% increased hazard damage」 ×2
  - `INC_HAZARD_DAMAGE` (json=inc_hazard_damage) refs=2(ext=1)
    sig=<<无StatGen>>  - `HAZARD_DAMAGE` (json=hazard_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「herald skills deal 20% increased damage」 ×2
  - `INC_HERALD_DAMAGE` (json=inc_herald_damage) refs=2(ext=1)
    sig=<<无StatGen>>  - `HERALD_DAMAGE` (json=herald_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「minions deal 10% increased damage」 ×2
  - `INC_MINION_DAMAGE` (json=inc_minion_damage) refs=4(ext=3)
    sig=<<无StatGen>>  - `MINION_DAMAGE` (json=minion_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「15% increased stun buildup」 ×2
  - `INC_STUN_BUILDUP` (json=inc_stun_buildup) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_STUN_DURATION` (json=inc_stun_duration) refs=2(ext=1)
    sig=<<无StatGen>>
### 「minions have 3% increased attack and cast speed」 ×2
  - `MINION_ATTACK_SPEED` (json=minion_attack_speed) refs=2(ext=1)
    sig=<<无StatGen>>  - `INC_MINIONS_ATTACK_CAST_SPEED` (json=inc_minions_attack_cast_speed) refs=1(ext=0)
    sig=<<无StatGen>>
### 「gain 1 rage on melee hitdamage」 ×2
  - `RAGE_ON_HIT` (json=rage_on_hit) refs=2(ext=0)
    sig=ResourceStatEffect.entry(ResourceType.RAGE, ExpressionCodec.of(5.0), Set.of(), List.of())  - `INC_MAX_RAGE` (json=inc_max_rage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「+1 to maximum endurance charges」 ×2
  - `ADD_MAX_ENDURANCE` (json=add_max_endurance) refs=1(ext=0)
    sig=<<无StatGen>>  - `GRANT_ENDURANCE_CHARGE_ON_GAIN` (json=grant_endurance_charge_on_gain) refs=1(ext=0)
    sig=GrantOnEventStatEffect.chargeEntry(ChargeType.ENDURANCE, 0.0, Set.of("gain_endurance"))
### 「temporary minion skills have +2 to limit of minions summoned」 ×2
  - `ADD_MAX_MINION` (json=add_max_minion) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_CHARM_DURATION` (json=inc_charm_duration) refs=1(ext=0)
    sig=<<无StatGen>>
### 「15% chance to pierce an enemy」 ×2
  - `ADD_PROJECTILE_PIERCE_CHANCE` (json=add_projectile_pierce_chance) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_PROJECTILE_PIERCE` (json=inc_projectile_pierce) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% increased buffmagnitude of bleeding you inflict」 ×2
  - `AILMENT_MAGNITUDE_BLEED` (json=ailment_magnitude_bleed) refs=1(ext=0)
    sig=<<无StatGen>>  - `MORE_BLEEDING_MAGNITUDE_INFLICTED` (json=more_bleeding_magnitude_inflicted) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% increased buffmagnitude of poison you inflict」 ×2
  - `AILMENT_MAGNITUDE_POISON` (json=ailment_magnitude_poison) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_POISON_MAGNITUDE` (json=inc_poison_magnitude) refs=1(ext=0)
    sig=<<无StatGen>>
### 「15% increased buffmagnitude of shock you inflict」 ×2
  - `AILMENT_MAGNITUDE_SHOCK` (json=ailment_magnitude_shock) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_SHOCK_MAGNITUDE` (json=inc_shock_magnitude) refs=1(ext=0)
    sig=<<无StatGen>>
### 「you have no mana skill mana costs statconversion to life costs」 ×2
  - `BLOOD_MAGIC_ENERGY_TO_LIFE` (json=blood_magic_energy_to_life) refs=1(ext=0)
    sig=ResourceConvertStatEffect.entry(ResourceType.ENERGY, ResourceType.HEALTH, 1.0)  - `BLOOD_MAGIC_MANA_TO_LIFE` (json=blood_magic_mana_to_life) refs=1(ext=0)
    sig=ResourceConvertStatEffect.entry(ResourceType.MANA, ResourceType.HEALTH, 1.0)
### 「20% chance for bleeding to be aggravated when inflicted against enemies on jagged ground」 ×2
  - `CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED` (json=chance_aggravate_on_hit_vs_jagged) refs=1(ext=0)
    sig=StatEntry.of(jaggedBase.amount(), new StatTargets(null, TargetSelector.EVENT_TARGET, null), List.of(new StatusValue(GroundStatusTypes.ON_JAGGED_GROUND.id, Craft …  - `INC_JAGGED_GROUND_DURATION` (json=inc_jagged_ground_duration) refs=1(ext=0)
    sig=<<无StatGen>>
### 「attacks gain increased accuracy rating equal to their critical hit chance」 ×2
  - `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL` (json=chance_attacks_accuracy_rating_equal) refs=1(ext=0)
    sig=<<无StatGen>>  - `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL` (json=chance_attacks_accuracy_rating_equal_critical) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% chance for flasks you use to not consume charges」 ×2
  - `CHANCE_FLASK_NO_CONSUME` (json=chance_flask_no_consume) refs=1(ext=0)
    sig=<<无StatGen>>  - `FLASK_NO_CONSUME` (json=flask_no_consume) refs=1(ext=0)
    sig=<<无StatGen>>
### 「statconversion all evasion rating to armour」 ×2
  - `CONVERTS_ALL_EVASION_RATING` (json=stat_converts_all_evasion_rating) refs=1(ext=0)
    sig=<<无StatGen>>  - `IRON_REFLEXES_EVASION_TO_ARMOR` (json=iron_reflexes_evasion_to_armor) refs=1(ext=0)
    sig=ModifierStatEffect.entry(Attributes.ARMOR, new ExpressionCodec.Value(Either.right( Conversions.convertPercent(Conversions.attributeRef("evasion"), 100.0))), Eit …
### 「gain 10% of damage as extra physical damage」 ×2
  - `CONVERT_EXTRA_GENERIC_TO_PHYSICAL` (json=convert_extra_generic_to_physical) refs=1(ext=0)
    sig=DamageConvertStatEffect.entry(Set.of(StatTag.PHYSICAL, StatTag.CHAOS, StatTag.FIRE, StatTag.COLD, StatTag.LIGHTNING), StatTag.PHYSICAL, DamageConvertStatEffect. …  - `EXTRA_PHYSICAL` (json=extra_physical) refs=1(ext=0)
    sig=<<无StatGen>>
### 「damage with hits is lucky against enemies that are on low life」 ×2
  - `DAMAGE_LUCKY` (json=damage_lucky) refs=1(ext=0)
    sig=LuckyStatEffect.entry(LuckyStatEffect.RollSubject.DAMAGE, LuckyStatEffect.RollMode.LUCKY)  - `DAMAGE_LUCKY_VS_LOW_LIFE` (json=damage_lucky_vs_low_life) refs=1(ext=0)
    sig=StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), new StatTargets(null, TargetSelector.EVENT_TARGET, null), List.of(new LivingStateValue("low_life", 80. …
### 「chance to evasion is unlucky chance to deflect is lucky」 ×2
  - `GLANCING_BLOWS_DEFLECT_LUCKY` (json=glancing_blows_deflect_lucky) refs=1(ext=0)
    sig=LuckyStatEffect.entry(LuckyStatEffect.RollSubject.DEFLECT, LuckyStatEffect.RollMode.LUCKY)  - `GLANCING_BLOWS_EVADE_UNLUCKY` (json=glancing_blows_evade_unlucky) refs=1(ext=0)
    sig=LuckyStatEffect.entry(LuckyStatEffect.RollSubject.EVADE, LuckyStatEffect.RollMode.UNLUCKY)
### 「10% increased skill effect duration」 ×2
  - `INC_AILMENT_DURATION` (json=inc_ailment_duration) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_SKILL_EFFECT_DURATION` (json=inc_skill_effect_duration) refs=1(ext=0)
    sig=<<无StatGen>>
### 「15% increased crossbow reload speed」 ×2
  - `INC_CROSSBOW_DAMAGE` (json=inc_crossbow_damage) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_RELOAD_SPEED` (json=inc_reload_speed) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% increased attack damage」 ×2
  - `INC_DAMAGE_ATTACK` (json=inc_damage_attack) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_SPELL_DAMAGE` (json=inc_spell_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「4% of damage is taken from mana before life」 ×2
  - `INC_DAMAGE_IS_TAKEN_MANA` (json=inc_damage_is_taken_mana) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_MANA_TAKE` (json=inc_mana_take) refs=1(ext=0)
    sig=<<无StatGen>>
### 「50% increased effect of incision」 ×2
  - `INC_INCISION_EFFECT` (json=inc_incision_effect) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_INCISION_MAGNITUDE` (json=inc_incision_magnitude) refs=1(ext=0)
    sig=<<无StatGen>>
### 「increases and reductions to projectile speed also apply to damage with bows」 ×2
  - `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED` (json=inc_increases_reductions_projectile_speed) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW` (json=inc_increases_reductions_projectile_speed_bow) refs=1(ext=0)
    sig=<<无StatGen>>
### 「mark skills have 10% increased use speed」 ×2
  - `INC_MARK_EFFECT` (json=inc_mark_effect) refs=1(ext=0)
    sig=<<无StatGen>>  - `MARK_USE_SPEED` (json=mark_use_speed) refs=1(ext=0)
    sig=<<无StatGen>>
### 「minions reviving 5% faster」 ×2
  - `INC_MINIONS_REVIVE_FASTER` (json=inc_minions_revive_faster) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_MINION_REVIVE` (json=inc_minion_revive) refs=1(ext=0)
    sig=<<无StatGen>>
### 「regenerate 1% of maximum life per second while affected by any damaging ailment」 ×2
  - `INC_REGENERATE_LIFE_AFFECTED_ANY` (json=inc_regenerate_life_affected_any) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_REGENERATE_LIFE_AFFECTED_ANY_WHILE_AFFECTED` (json=inc_regenerate_life_affected_any_while_affected) refs=1(ext=0)
    sig=<<无StatGen>>
### 「regenerate 15% of maximum life per second while on low life」 ×2
  - `INC_REGENERATE_LIFE_LOW` (json=inc_regenerate_life_low) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_REGENERATE_LIFE_LOW_WHILE_ON` (json=inc_regenerate_life_low_while_on) refs=1(ext=0)
    sig=<<无StatGen>>
### 「regenerate 25% of maximum life per second while surrounded」 ×2
  - `INC_REGENERATE_LIFE_SURROUNDED` (json=inc_regenerate_life_surrounded) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_REGENERATE_LIFE_SURROUNDED_PER_SECOND` (json=inc_regenerate_life_surrounded_per_second) refs=1(ext=0)
    sig=<<无StatGen>>
### 「regenerate 1% of maximum life per second while you have a totem」 ×2
  - `INC_REGENERATE_LIFE_TOTEM` (json=inc_regenerate_life_totem) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_REGENERATE_LIFE_TOTEM_WHILE_YOU` (json=inc_regenerate_life_totem_while_you) refs=1(ext=0)
    sig=<<无StatGen>>
### 「regenerate 1% of maximum life per second if you've used a life flask in the past 10 seconds」 ×2
  - `INC_REGENERATE_LIFE_VE_FLASK` (json=inc_regenerate_life_ve_flask) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_REGENERATE_LIFE_VE_FLASK_PER_SECOND` (json=inc_regenerate_life_ve_flask_per_second) refs=1(ext=0)
    sig=<<无StatGen>>
### 「20% slower start of energy shield recharge」 ×2
  - `INC_SLOWER_START_ENERGY_SHIELD` (json=inc_slower_start_energy_shield) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_SLOWER_START_ENERGY_SHIELD_NO_CONDITION` (json=inc_slower_start_energy_shield_no_condition) refs=1(ext=0)
    sig=<<无StatGen>>
### 「10% increased trap damage」 ×2
  - `INC_TRAP_DAMAGE` (json=inc_trap_damage) refs=1(ext=0)
    sig=<<无StatGen>>  - `TRAP_DAMAGE` (json=trap_damage) refs=1(ext=0)
    sig=<<无StatGen>>
### 「6% increased trap throwing speed」 ×2
  - `INC_TRAP_THROWING_SPEED` (json=inc_trap_throwing_speed) refs=1(ext=0)
    sig=<<无StatGen>>  - `INC_TRAP_THROW_SPEED` (json=inc_trap_throw_speed) refs=1(ext=0)
    sig=<<无StatGen>>
### 「minions gain 20% of elemental damage as extra chaos damage」 ×2
  - `MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS` (json=minion_convert_extra_generic_to_chaos) refs=1(ext=0)
    sig=new Stat(ExpressionCodec.of(MathParser.parse("$value")), List.of(), StatTargets.ofTarget(TargetSelector.MINIONS), new DamageConvertStatEffect( Set.of(StatTag.PH …  - `MINION_RECOUP_HEALTH` (json=minion_recoup_health) refs=1(ext=0)
    sig=new Stat(ExpressionCodec.of(MathParser.parse("$value")), List.of(), StatTargets.ofTarget(TargetSelector.MINIONS), new RecoupStatEffect(Set.of(StatTag.HEALTH), T …
### 「recover 50% of maximum life when you heavy stun a rare or unique enemy」 ×2
  - `RECOVER_LIFE_HEAVY_STUN` (json=stat_recover_life_heavy_stun) refs=1(ext=0)
    sig=<<无StatGen>>  - `RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN` (json=stat_recover_life_heavy_stun_heavystun) refs=1(ext=0)
    sig=<<无StatGen>>
### 「recover 2% of maximum life on killing a poisoned enemy」 ×2
  - `RECOVER_LIFE_KILLING_POISONED` (json=stat_recover_life_killing_poisoned) refs=1(ext=0)
    sig=<<无StatGen>>  - `RECOVER_LIFE_KILLING_POISONED_POISON` (json=stat_recover_life_killing_poisoned_poison) refs=1(ext=0)
    sig=<<无StatGen>>
### 「recover 2% of maximum life when one of your minions is reviving」 ×2
  - `RECOVER_LIFE_MINIONS_IS` (json=stat_recover_life_minions_is) refs=1(ext=0)
    sig=<<无StatGen>>  - `RECOVER_LIFE_MINIONS_IS_MINION` (json=stat_recover_life_minions_is_minion) refs=1(ext=0)
    sig=<<无StatGen>>
### 「accuracy rating is doubled never deal critical hits」 ×2
  - `RESOLUTE_TECHNIQUE_ACCURACY` (json=resolute_technique_accuracy) refs=1(ext=0)
    sig=ModifierStatEffect.entry(AttributeRegister.ACCURACY, ExpressionCodec.of(1.0), Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), Set.of(), List.of() …  - `RESOLUTE_TECHNIQUE_CRIT_CHANCE` (json=resolute_technique_crit_chance) refs=1(ext=0)
    sig=new Stat(ExpressionCodec.of(0.0), List.of(), StatTargets.EMPTY, new ModifierStatEffect(AttributeRegister.CRIT_CHANCE, Either.right(AttributeLimiters.Operation.S …
## B. 无描述注释的常量（需补/查）: 0

## C. 底层签名相同聚类（功能重复，>1 成员）

### sig=ChanceOnEventStatEffect.entry(DebuffStatusTypes.AGGRAVATED, #, Set.of(EventName.CAUSED_HIT ×2
  - `AGGRAVATE_BLEEDING_ON_HIT` json=aggravate_bleeding_on_hit refs=1(ext=0) comment='Bleeding you inflict is Aggravated Base Bleeding D'  - `CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS` json=chance_to_aggravate_bleeding_on_targets_you_hit_damage_with_empowered_attacks refs=1(ext=0) comment='{X}% chance to Aggravate Bleeding on targets you H'
### sig=ImmuneStatEffect.entry(AilmentStatusTypes.BLEED) ×2
  - `IMMUNE_TO_BLEEDING_IF_EQUIPPED_HELMET_HAS_HIGHER_ARMOR_THAN_EVASION` json=immune_to_bleeding_if_equipped_helmet_has_higher_armor_than_evasion refs=1(ext=0) comment='Immune to Bleeding if Equipped Helmet has higher A'  - `CHAOS_INOCULATION_IMMUNE_BLEED` json=chaos_inoculation_immune_bleed refs=1(ext=0) comment='Maximum Life is 1 Immune to Chaos Damage and Bleed'
### sig=ImmuneStatEffect.entry(DebuffStatusTypes.STUN) ×2
  - `YOUR_HIT_DAMAGE_CANNOT_BE_EVASION_BY_HEAVY_STUNNED_ENEMIES` json=your_hit_damage_cannot_be_evasion_by_heavy_stunned_enemies refs=1(ext=0) comment='Your Hit Damage cannot be Evasion by Heavy Stunned'  - `UNWAVERING_STANCE_IMMUNE_STUN` json=unwavering_stance_immune_stun refs=1(ext=0) comment='Cannot be Light Stunned Cannot Dodge Roll or Sprin'
### sig=ImmuneStatEffect.entry(DebuffStatusTypes.MAIM) ×2
  - `IMMUNE_TO_MAIM` json=immune_to_maim refs=1(ext=0) comment='Immune to Maim'  - `IMMUNE_TO_MAIM_WHILE_SHAPESHIFTED` json=immune_to_maim_while_shapeshifted refs=1(ext=0) comment='Immune to Maim while Shapeshifted'
### sig=new Stat(ExpressionCodec.of(MathParser.parse("$value")), List.of(), StatTargets.ofTarget(T ×2
  - `MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH` json=minions_recoup_of_damage_taken_as_health refs=1(ext=0) comment='Minions Recoup {X}% of Damage taken as Health'  - `MINION_RECOUP_HEALTH` json=minion_recoup_health refs=1(ext=0) comment='Minions Gain 20% of Elemental Damage as Extra Chao'
### sig=new Stat(ExpressionCodec.of(MathParser.parse("$value")), List.of(), StatTargets.ofTarget(T ×2
  - `RECOUP_OF_DAMAGE_TAKEN_BY_YOUR_TOTEMS_AS_HEALTH` json=recoup_of_damage_taken_by_your_totems_as_health refs=1(ext=0) comment='Recoup {X}% of damage taken by your Totems as Heal'  - `TOTEM_RECOUP_HEALTH` json=totem_recoup_health refs=1(ext=0) comment='Each Totem applies 2% increased Damage taken to En'
### sig=ResourceConvertStatEffect.entry(ResourceType.MANA, ResourceType.HEALTH, #) ×2
  - `OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS` json=of_skill_mana_costs_stat_conversion_to_health_costs refs=1(ext=0) comment='{X}% of Skill Mana Costs Stat Conversion to Health'  - `BLOOD_MAGIC_MANA_TO_LIFE` json=blood_magic_mana_to_life refs=1(ext=0) comment='You have no Mana Skill Mana Costs StatConversion t'