package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WirePerScaling;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NotableNodes07 {

    private NotableNodes07() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();
        // [Stat] 10% chance to [Daze] on [HitDamage|Hit]
        // [Stat] 30% increased Damage against [Daze|Dazed] Enemies
        list.add(TalentNodeDef.of(
                "38342", 10074.4f, -606.595f,
                "stupefy", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_DAZED_ENEMIES, 0.3)
                ),
                List.of("54545", "61718")
        ));
        // [Stat] [Companion|Companions] gain your [Strength]
        // [Stat] +15 to [Strength]
        list.add(TalentNodeDef.of(
                "8791", 4249.44f, 9803.3f,
                "sturdy_ally", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_YOUR_STRENGTH, 1.0),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 15.0)
                ),
                List.of("23961", "59647")
        ));
        // [Stat] 80% increased [Armour] from Equipped Body Armour
        list.add(TalentNodeDef.of(
                "32416", -2993.07f, 1727.22f,
                "sturdy_metal", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_FROM_EQUIPPED_BODY_ARMOR, 0.8)
                ),
                List.of("27726", "6529")
        ));
        // [Stat] +30 to maximum Mana
        // [Stat] 14% increased Mana Regeneration Rate
        list.add(TalentNodeDef.of(
                "57967", -2656.19f, -2063.25f,
                "sturdy_mind", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_MANA, 30.0),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.14)
                ),
                List.of("30834")
        ));
        // [Stat] [HitDamage|Hits] [ArmourBreak|Break] 30% increased [Armour|Armour] on targets with [Ailments]
        // [Stat] +10 to [Strength]
        // [Stat] 25% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "60138", 6342.45f, -4198.19f,
                "stylebender", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS, 0.3),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("52695")
        ));
        // [Stat] +1 to [Evasion] Rating per 1 [ItemEnergyShield|Item Energy Shield] on Equipped Helmet
        list.add(TalentNodeDef.of(
                "55193", 5995.34f, -584.865f,
                "subterfuge_mask", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_EVASION_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_HELMET, 1.0)
                ),
                List.of("10944")
        ));
        // [Stat] 30% increased Life Regeneration rate during Effect of any Life [Flask]
        list.add(TalentNodeDef.of(
                "30392", 4990.49f, 10339.5f,
                "succour", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_DURING_EFFECT_OF_ANY_HEALTH_FLASK, 0.3)
                ),
                List.of("13157", "28106")
        ));
        // [Stat] 16% increased [Critical|Critical Hit Chance] for [Spell|Spells]
        // [Stat] 8% increased Cast Speed if you've dealt a [Critical|Critical Hit] [Recently|Recently]
        list.add(TalentNodeDef.of(
                "10398", 310.135f, -7336.21f,
                "sudden_escalation", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.16),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, 0.08)
                ),
                List.of("26682", "56640")
        ));
        // [Stat] 4% chance that if you would gain [Rage] on [HitDamage|Hit], you instead gain up to your maximum Rage
        list.add(TalentNodeDef.of(
                "29372", -4596.57f, 4909.67f,
                "sudden_infuriation", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE, 0.04)
                ),
                List.of("53989", "7720")
        ));
        // [Stat] 30% increased amount of Mana [ManaLeech|Leeched]
        // [Stat] [Unaffected] by [Chill] while [ManaLeech|Leeching] Mana
        list.add(TalentNodeDef.of(
                "14383", 6104.56f, 8403.35f,
                "suffusion", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.3),
                        TalentNodeGen.stat(StatIds.UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA, 1.0)
                ),
                List.of("46601")
        ));
        // [Stat] 25% increased [CriticalDamageBonus|Critical Damage Bonus] for [Attack] Damage
        // [Stat] +25% to [CriticalDamageBonus|Critical Damage Bonus] against Stunned Enemies
        list.add(TalentNodeDef.of(
                "2511", -3806.57f, 4949.13f,
                "sundering", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.TO_CRITICAL_DAMAGE_BONUS_AGAINST_STUNNED_ENEMIES, 0.25)
                ),
                List.of("64399")
        ));
        // [Stat] 25% increased Damage while you have a [Totem|Totem]
        // [Stat] [Spell|Spells] Cast by [Totem|Totems] have 3% increased Cast Speed per Summoned [Totem|Totem]
        // [Stat] [Attack|Attacks] used by [Totem|Totems] have 3% increased [Attack] Speed per Summoned [Totem|Totem]
        list.add(TalentNodeDef.of(
                "19249", -5585.44f, -1360.46f,
                "supportive_ancestors", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM, 0.25),
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED_PER_SUMMONED_TOTEM, 0.03),
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED_PER_SUMMONED_TOTEM, 0.03)
                ),
                List.of("2174", "33209")
        ));
        // [Stat] Gain [ArcaneSurgeDuration|Arcane Surge] when you [Shapeshift] to Human form after
        //        being [Shapeshift|Shapeshifted] for at least 8 seconds
        list.add(TalentNodeDef.of(
                "29881", -5376.64f, -3509.7f,
                "surging_beast", "passives_druidshapeshiftwyvernnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ARCANE_SURGE_WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_AFTER_BEING_SHAPESHIFTED_FOR_AT_LEAST_SECONDS, 8.0)
                ),
                List.of("63402")
        ));
        // [Stat] Damage [Penetration|Penetrates] 15% [Resistances|Lightning Resistance]
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "42065", 5878.32f, 1805.79f,
                "surging_currents", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("30372")
        ));
        // [Stat] 12% increased [Block] chance
        // [Stat] 1% increased Movement Speed for each time you've Blocked in the past 10 seconds
        list.add(TalentNodeDef.of(
                "56806", 2870.2f, 8351.94f,
                "swift_blocking", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_FOR_EACH_TIME_YOU_VE_BLOCKED_IN_THE_PAST_SECONDS, 0.01)
                ),
                List.of("52836")
        ));
        // [Stat] 10% increased [SkillSpeed|Skill Speed] while [Shapeshift|Shapeshifted]
        list.add(TalentNodeDef.of(
                "32353", -6138.12f, -968.804f,
                "swift_claw", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("29126", "9037")
        ));
        // [Stat] 15% increased [Projectile] Speed
        // [Stat] 20% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "56714", -3999.0f, 11241.8f,
                "swift_flight", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("3652")
        ));
        // [Stat] 12% increased [Attack] Speed if you've successfully [Parry|Parried] [Recently]
        // [Stat] 6% increased Movement Speed if you've successfully [Parry|Parried] [Recently]
        list.add(TalentNodeDef.of(
                "65265", 8122.32f, 3085.39f,
                "swift_interruption", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY, 0.06)
                ),
                List.of("17146", "31366", "3843")
        ));
        // [Stat] [Banner] Skills have 30% increased Area of Effect
        // [Stat] [Banner] Skills have 30% increased Duration
        list.add(TalentNodeDef.of(
                "53367", -3978.69f, 9252.58f,
                "symbol_of_defiance", "passives_bannerareanotable",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_DURATION, 0.3)
                ),
                List.of("10835", "12821")
        ));
        // [Stat] +0.5 metres to Dodge Roll distance while [Surrounded]
        // [Stat] 10% increased Movement Speed while [Surrounded]
        list.add(TalentNodeDef.of(
                "17825", -4572.75f, 9245.92f,
                "tactical_retreat", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED, 0.5),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_SURROUNDED, 0.1)
                ),
                List.of("45585", "46931")
        ));
        // [Stat] 20% increased [Critical|Critical Hit Chance] for [Attack|Attacks]
        // [Stat] 30% increased [BuffMagnitude|Magnitude] of [NonDamagingAilments|Non-Damaging Ailments] you inflict with [Critical|Critical Hits]
        list.add(TalentNodeDef.of(
                "22864", 3150.0f, 9676.53f,
                "tainted_strike", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS, 0.3)
                ),
                List.of("23259")
        ));
        // [Stat] Gain 20 Life per enemy killed
        // [Stat] 2% chance to Recover all Life when you Kill an Enemy
        list.add(TalentNodeDef.of(
                "40213", 3682.03f, -553.869f,
                "taste_for_blood", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, 20.0),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY, 0.02)
                ),
                List.of("55846")
        ));
        // [Stat] 20% of [Physical] Damage taken [Recoup|Recouped] as Life
        list.add(TalentNodeDef.of(
                "48774", 410.905f, -6145.21f,
                "taut_flesh", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.2)
                ),
                List.of("34367")
        ));
        // [Stat] 25% increased [Armour]
        // [Stat] +15% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "18157", -6326.36f, 9297.54f,
                "tempered_defences", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.25),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.15)
                ),
                List.of("29447")
        ));
        // [Stat] 15% increased effect of [ArmourBreak|Fully Broken Armour]
        // [Stat] +10 to [Strength]
        // [Stat] 20% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "8831", 5472.15f, -8445.56f,
                "tempered_mind", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("14082")
        ));
        // [Stat] 16% increased [CooldownRecovery|Cooldown Recovery Rate]
        list.add(TalentNodeDef.of(
                "12412", -2697.32f, -11231.0f,
                "temporal_mastery", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.16)
                ),
                List.of("17796")
        ));
        // [Stat] 4% increased [Attack] Speed
        // [Stat] 6% increased [Attack] Speed if you've been Hit [Recently]
        // [Stat] +10 to [Strength]
        list.add(TalentNodeDef.of(
                "25971", 5933.04f, -2670.25f,
                "tenfold_attacks", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_VE_BEEN_HIT_RECENTLY, 0.06),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("50635")
        ));
        // [Stat] 40% reduced [Poison|Poison] Duration on you
        // [Stat] Life [Flask|Flasks] gain 0.1 charges per Second
        // [Stat] +10 to [Intelligence]
        list.add(TalentNodeDef.of(
                "4544", 5311.52f, 299.425f,
                "the_ancient_serpent", "passives_azmeriprimalsnakenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION_ON_YOU, -0.4),
                        TalentNodeGen.stat(StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("59446", "61263")
        ));
        // [Stat] 40% increased [Charges|Endurance, Frenzy and Power Charge] Duration
        // [Stat] +10 to [Dexterity]
        // [Stat] Skills have 10% chance to not remove [Charges|Charges] but still count as consuming them
        list.add(TalentNodeDef.of(
                "7847", 9860.54f, 353.555f,
                "the_fabled_stag", "passives_azmerivividstagnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION, 0.4),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0),
                        TalentNodeGen.stat(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_CHARGES_BUT_STILL_COUNT_AS_CONSUMING_THEM, 0.1)
                ),
                List.of("19027", "40471")
        ));
        // [Stat] 30% increased Damage if you've consumed a [Charges|Frenzy Charge] [Recently]
        // [Stat] 10% increased [SkillSpeed|Skill Speed] if you've consumed a [Charges|Frenzy Charge] [Recently]
        // [Stat] +10 to [Strength]
        list.add(TalentNodeDef.of(
                "34543", 5279.5f, 6677.73f,
                "the_frenzied_bear", "passives_azmeriwildbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY, 0.3),
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("26932", "35689")
        ));
        // [Stat] +1 Life per 4 [Dexterity]
        // [Stat] +1 to [StunThreshold|Stun Threshold] per [Dexterity|Dexterity]
        // [Stat] +5 to [Strength]
        list.add(TalentNodeDef.of(
                "54031", 9400.0f, 4449.79f,
                "the_great_boar", "passives_azmeriwildboarnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_PER_DEXTERITY, 1.0),
                        TalentNodeGen.stat(StatIds.TO_STUN_THRESHOLD_PER_DEXTERITY, 1.0),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("37242")
        ));
        // [Stat] 50% reduced [BuffEffect|effect] of [Curse|Curses] on you
        // [Stat] 35% reduced Effect of [NonDamagingAilments|Non-Damaging Ailments] on you
        list.add(TalentNodeDef.of(
                "42078", 9257.6f, -2702.28f,
                "the_hollowkeeper", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.5),
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_NON_DAMAGING_AILMENTS_ON_YOU, -0.35)
                ),
                List.of("59657")
        ));
        // [Stat] 15% increased [Presence|Presence] Area of Effect
        // [Stat] [Aura] Skills have 10% increased [BuffMagnitude|Magnitudes]
        // [Stat] +10 to [Intelligence]
        list.add(TalentNodeDef.of(
                "48734", 10660.1f, 366.975f,
                "the_howling_primate", "passives_azmeriprimalmonkeynotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.15),
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("12066", "17792")
        ));
        // [Stat] +10% to [Resistances|Fire Resistance]
        // [Stat] 15% increased effect of [ArmourBreak|Fully Broken Armour]
        // [Stat] [ArmourBreak|Fully Broken Armour] you inflict also increases [Fire] Damage Taken from [HitDamage|Hits]
        list.add(TalentNodeDef.of(
                "28542", -5457.11f, 8238.24f,
                "the_molten_one_s_gift", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_FIRE_RESISTANCE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.FULLY_BROKEN_ARMOR_YOU_INFLICT_ALSO_INC_FIRE_DAMAGE_TAKEN_FROM_HIT_DAMAGE, 1.0)
                ),
                List.of("43174", "62785")
        ));
        // [Stat] 25% increased [BuffMagnitude|Magnitude] of [Ailments] you inflict against [Mark|Marked] Enemies
        // [Stat] 20% increased [Critical] Hit Chance against [Mark|Marked] Enemies
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "2745", 8635.74f, 2712.82f,
                "the_noble_wolf", "passives_azmerivividwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_AILMENTS_YOU_INFLICT_AGAINST_MARKED_ENEMIES, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES, 0.2),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("18864", "54562")
        ));
        // [Stat] 20% increased [CriticalDamageBonus|Critical Damage Bonus] if you've gained a [Charges|Power Charge] [Recently]
        // [Stat] +1 to Maximum [Charges|Power Charges]
        list.add(TalentNodeDef.of(
                "27176", 3563.72f, -3733.75f,
                "the_power_within", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_GAIN_A_POWER_CHARGE_RECENTLY, 0.2),
                        TalentNodeGen.stat(StatIds.TO_MAX_POWER_CHARGES, 1.0)
                ),
                List.of("64643")
        ));
        // [Stat] 20% increased [Deflect|Deflection Rating] while moving
        list.add(TalentNodeDef.of(
                "21349", 5143.24f, 7907.42f,
                "the_quick_fox", "passives_azmerisacredfoxnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING_WHILE_MOVING, 0.2)
                ),
                List.of("27017")
        ));
        // [Stat] [HitDamage|Hits] against you have 30% reduced [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] 15% reduced Duration of [Ailments|Ailments] on You
        // [Stat] +10 to [Strength]
        list.add(TalentNodeDef.of(
                "45370", 6223.73f, 6071.65f,
                "the_raging_ox", "passives_azmeriwildoxnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.3),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_ON_YOU, -0.15),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("37568")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        // [Stat] 8% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        // [Stat] 10% increased Mana Cost [Efficiency]
        // [Stat] 10% increased [Reservation] [Efficiency] of [Minion] Skills
        list.add(TalentNodeDef.of(
                "52971", 7020.64f, -6363.28f,
                "the_soul_meridian", "passives_energyshieldrechargedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.1),
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS, 0.1)
                ),
                List.of("21227", "46857")
        ));
        // [Stat] 20% chance for Damage of Enemies [HitDamage|Hitting] you to be [Unlucky]
        // [Stat] 20% chance for Damage with [HitDamage|Hits] to be [Lucky]
        list.add(TalentNodeDef.of(
                "11774", 7334.57f, 8277.54f,
                "the_spring_hare", "passives_azmerisacredrabbitnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY, 0.2)
                ),
                List.of("58513")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 12% of [Evasion|Evasion Rating]
        // [Stat] 40% increased [Evasion] Rating while moving
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "22811", 8862.17f, 5627.31f,
                "the_wild_cat", "passives_azmerivividcatnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.12),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_MOVING, 0.4),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("23253", "3170")
        ));
        // [Stat] 3% increased [Evasion] Rating per 10 [Intelligence]
        // [Stat] Gain Accuracy Rating equal to your [Intelligence]
        // [Stat] +10 to [Intelligence]
        list.add(TalentNodeDef.of(
                "53185", 9880.47f, 1643.66f,
                "the_winter_owl", "passives_azmeriprimalowlnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_PER_INTELLIGENCE, 0.03),
                        TalentNodeGen.stat(StatIds.GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE, 1.0),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("18897", "60891")
        ));
        // [Stat] Regenerate 0.5% of maximum Life per second
        // [Stat] 40% increased Life Regeneration Rate while stationary
        list.add(TalentNodeDef.of(
                "35849", -7523.03f, -4513.11f,
                "thickened_arteries", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.005),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, 0.4)
                ),
                List.of("54962")
        ));
        // [Stat] 20% chance for [Charm|Charms] you use to not consume Charges
        // [Stat] Recover 5% of maximum Mana when a [Charm|Charm] is used
        list.add(TalentNodeDef.of(
                "56893", 6445.26f, 371.465f,
                "thicket_warding", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_CHARMS_YOU_USE_TO_NOT_CONSUME_CHARGES, 0.2),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_A_CHARM_IS_USED, 0.05)
                ),
                List.of("14001", "29049")
        ));
        // [Stat] 20% increased [Freeze|Freeze] Buildup
        // [Stat] 50% increased Damage with Hits against [Frozen] Enemies
        list.add(TalentNodeDef.of(
                "19722", 6689.44f, -7475.15f,
                "thin_ice", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES, 0.5)
                ),
                List.of("22713", "3128")
        ));
        // [Stat] 25% chance when you gain an [Charges|Endurance Charge] to gain an additional [Charges|Endurance Charge]
        list.add(TalentNodeDef.of(
                "59433", -8155.99f, -4880.6f,
                "thirst_for_endurance", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE, 0.25)
                ),
                List.of("11027")
        ));
        // [Stat] 25% chance when you gain a [Charges|Power Charge] to gain an additional [Charges|Power Charge]
        list.add(TalentNodeDef.of(
                "38532", -8308.57f, -4625.78f,
                "thirst_for_power", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE, 0.25)
                ),
                List.of("13777")
        ));
        // [Stat] [LifeLeech|Leeching Life] from your [HitDamage|Hits] causes your [Companion] to also Leech the [LeechSameAmount|same amount] of Life
        list.add(TalentNodeDef.of(
                "17600", 3604.21f, 9630.41f,
                "thirsting_ally", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.LEECHING_HEALTH_FROM_YOUR_HIT_DAMAGE_CAUSES_YOUR_COMPANION_TO_ALSO_LEECH_THE_SAME_AMOUNT_OF_HEALTH)
                ),
                List.of("10041", "18519")
        ));
        // [Stat] +6% to Thorns [Critical|Critical Hit] Chance
        list.add(TalentNodeDef.of(
                "43711", -6534.69f, 420.575f,
                "thornhide", "passives_thornsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_THORNS_CRITICAL_HIT_CHANCE, 0.06)
                ),
                List.of("5544")
        ));
        // [Stat] Enemies you apply [Incision] to take 2% increased [Physical] Damage per [Incision]
        list.add(TalentNodeDef.of(
                "42714", 6890.46f, 6461.48f,
                "thousand_cuts", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_APPLY_INCISION_TO_TAKE_INC_PHYSICAL_DAMAGE_PER_INCISION, 0.02)
                ),
                List.of("55724", "58644")
        ));
        // [Stat] 20% increased [Attack|Attack] Speed while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "25711", -308.213f, 8784.79f,
                "thrill_of_battle", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_SURROUNDED, 0.2)
                ),
                List.of("58038")
        ));
        // [Stat] Consuming [Glory] grants you 3% increased [Attack] damage per [Glory] consumed for 6 seconds, up to 60%
        // 已接线（WirePerScaling）：min(3% × sum:consume_glory.6 点数, 60%) —— 按消耗点数、6 秒窗口、60% 封顶。
        list.add(TalentNodeDef.of(
                "15606", -7887.61f, 8041.42f,
                "thrill_of_the_fight", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CONSUMING_GLORY_GRANTS_YOU_INC_ATTACK_DAMAGE_PER_GLORY_CONSUMED_FOR_SECONDS_UP_TO, 0.03)
                ),
                List.of("40736")
        ));
        // [Stat] 60% increased [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] 20% reduced [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "56265", 10381.9f, -3859.28f,
                "throatseeker", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.6),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, -0.2)
                ),
                List.of("60362", "6891")
        ));
        // [Stat] 50% increased [Electrocute|Electrocute Buildup] against [Shock|Shocked] Enemies
        // [Stat] 50% increased [Shock|Shock] Chance against [Electrocute|Electrocuted] Enemies
        list.add(TalentNodeDef.of(
                "63585", 3915.95f, 4292.71f,
                "thunderstruck", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP_AGAINST_SHOCKED_ENEMIES, 0.5),
                        TalentNodeGen.stat(StatIds.INC_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES, 0.5)
                ),
                List.of("5564")
        ));
        // [Stat] 25% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "42813", -4902.18f, 158.96f,
                "tides_of_change", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.25)
                ),
                List.of("35787")
        ));
        // [Stat] [Debuff|Debuffs] you inflict have 10% increased [SlowMagnitudeModifier|Slow Magnitude]
        // [Stat] [Debuff|Debuffs] on you expire 20% faster
        list.add(TalentNodeDef.of(
                "24240", 5943.98f, 7225.03f,
                "time_manipulation", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.1),
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.2)
                ),
                List.of("11764", "26772")
        ));
        // [Stat] 25% increased [StunThreshold|Stun Threshold]
        // [Stat] 20% increased Life Regeneration Rate while moving
        list.add(TalentNodeDef.of(
                "62732", -2831.12f, 2765.38f,
                "titan_s_determination", "passives_hearty",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.25),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_MOVING, 0.2)
                ),
                List.of("18742", "64192")
        ));
        // [Stat] 30% increased [Stun] Buildup
        // [Stat] 30% increased [StunThreshold|Stun Threshold]
        // [Stat] 5% increased [Strength]
        list.add(TalentNodeDef.of(
                "65160", -9314.22f, 3421.08f,
                "titanic", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.3),
                        TalentNodeGen.stat(StatIds.INC_STRENGTH, 0.05)
                ),
                List.of("44069", "917")
        ));
        // [Stat] 15% increased [Armour] and [Evasion] Rating
        // [Stat] Immune to [Bleeding] if Equipped Helmet has higher [Armour] than [Evasion] Rating
        // [Stat] Immune to [Poison] if Equipped Helmet has higher [Evasion] Rating than [Armour]
        list.add(TalentNodeDef.of(
                "2843", 1826.62f, 9170.85f,
                "tolerant_equipment", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.15),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_BLEEDING_IF_EQUIPPED_HELMET_HAS_HIGHER_ARMOR_THAN_EVASION, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_POISON_IF_EQUIPPED_HELMET_HAS_HIGHER_EVASION_THAN_ARMOR, 1.0)
                ),
                List.of("61318", "61396")
        ));
        // [Stat] 10% increased Ignite Duration on Enemies
        // [Stat] 25% increased Damage with [HitDamage|Hits] against [Ignite|Ignited] Enemies
        list.add(TalentNodeDef.of(
                "28482", -5701.71f, -4295.66f,
                "total_incineration", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES, 0.25)
                ),
                List.of("53632", "54311")
        ));
        // [Stat] 40% increased [BuffEffect|effect] of [ArcaneSurge|Arcane Surge] on you
        list.add(TalentNodeDef.of(
                "27626", -5560.32f, -8409.77f,
                "touch_the_arcane", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_ARCANE_SURGE_ON_YOU, 0.4)
                ),
                List.of("15628", "54067")
        ));
        // [Stat] 25% increased Chance to [Block] if you've Blocked with a raised [Shield] [Recently]
        // [Stat] 50% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] from Equipped [Shield]
        list.add(TalentNodeDef.of(
                "53823", -9279.0f, 1954.01f,
                "towering_shield", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_BLOCK_IF_YOU_VE_BLOCKED_WITH_A_RAISED_SHIELD_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.5)
                ),
                List.of("10508", "1214")
        ));
        // [Stat] 40% increased Duration of [Poison|Poisons] you inflict against [Slow|Slowed] Enemies
        list.add(TalentNodeDef.of(
                "261", 9563.0f, 6182.58f,
                "toxic_sludge", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_POISONS_YOU_INFLICT_AGAINST_SLOWED_ENEMIES, 0.4)
                ),
                List.of("1073", "5048")
        ));
        // [Stat] Immune to [Poison]
        list.add(TalentNodeDef.of(
                "2134", 11355.4f, -1849.36f,
                "toxic_tolerance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_POISON, 1.0)
                ),
                List.of("1723", "9703")
        ));
        // [Stat] Prevent +6% of Damage from [Deflect|Deflected] [HitDamage|Hits]
        list.add(TalentNodeDef.of(
                "52180", 10867.1f, 3186.63f,
                "trained_deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.PREVENT_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE, 0.06)
                ),
                List.of("30736")
        ));
        // [Stat] 25% increased [Ballista] [CriticalDamageBonus|Critical Damage Bonus]
        // [Stat] 20% increased [Ballista] [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "57785", 2332.55f, 7631.77f,
                "trained_turrets", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_DAMAGE_BONUS, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("23996", "31112", "56897")
        ));
        // [Stat] [Strike|Strikes] deal [MeleeSplash|Splash Damage]
        list.add(TalentNodeDef.of(
                "750", -10736.4f, 1905.27f,
                "tribal_fury", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.STRIKES_DEAL_SPLASH_DAMAGE, 1.0)
                ),
                List.of("6952")
        ));
        // [Stat] [Projectile|Projectiles] have 15% chance to [Chain] an additional time from terrain
        list.add(TalentNodeDef.of(
                "23221", 7259.38f, 1753.08f,
                "trick_shot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.15)
                ),
                List.of("45331")
        ));
        // [Stat] +10 to [Dexterity]
        // [Stat] 20% increased [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "61601", 5579.63f, -4981.71f,
                "true_strike", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("38541", "44420")
        ));
        // [Stat] [Companion|Companions] have 20% increased maximum Life
        // [Stat] 5% of Damage from [HitDamage|Hits] is taken from your Damageable [Companion]'s Life before you
        list.add(TalentNodeDef.of(
                "7553", -12251.6f, -5384.29f,
                "trusted_partner", "passives_miniondamageblue",
                List.of(),
                List.of("43385", "44485")
        ));
        // [Stat] 50% of Skill Mana costs [StatConversion|Converted] to Life Costs during any Life [Flask] Effect
        list.add(TalentNodeDef.of(
                "53131", -7676.37f, 496.685f,
                "tukohama_s_brew", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS_DURING_ANY_HEALTH_FLASK_EFFECT, 0.5)
                ),
                List.of("7060")
        ));
        // [Stat] 15% increased [Spell] Damage
        // [Stat] 10% reduced [Projectile] Speed for [Spell] Skills
        list.add(TalentNodeDef.of(
                "35564", 5452.61f, -10582.4f,
                "turn_the_clock_back", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS, -0.1)
                ),
                List.of("55041", "65310")
        ));
        // [Stat] 20% increased [Spell] Damage
        // [Stat] 15% increased [Projectile] Speed for [Spell] Skills
        list.add(TalentNodeDef.of(
                "2335", 5741.87f, -10081.4f,
                "turn_the_clock_forward", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS, 0.15)
                ),
                List.of("26135", "51565")
        ));
        // [Stat] 3% increased maximum Life
        // [Stat] 10% increased [StunThreshold|Stun Threshold] for each time you've been [HitDamage|Hit] by an Enemy [Recently], up to 100%
        list.add(TalentNodeDef.of(
                "1352", -7312.53f, 3840.64f,
                "unbending", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, 0.03),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_HIT_DAMAGE_BY_AN_ENEMY_RECENTLY_UP_TO, 0.1)
                ),
                List.of("21885")
        ));
        // [Stat] +10% to [Resistances|Cold Resistance]
        // [Stat] +2% to [MaximumResistances|Maximum Cold Resistance] if you have at least 5 Blue [SupportGem|Support Gems] Socketed
        list.add(TalentNodeDef.of(
                "4579", 1918.12f, -10762.1f,
                "unbothering_cold", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_MAX_COLD_RESISTANCE_IF_YOU_HAVE_AT_LEAST_BLUE_SUPPORT_GEMS_SOCKETED, 0.02)
                ),
                List.of("26061")
        ));
        // [Stat] 40% increased [Chill|Chill] Duration on Enemies
        // [Stat] 40% increased [Shock] Duration
        // [Stat] 25% increased [BuffMagnitude|Magnitude] of [Chill] you inflict
        // [Stat] 25% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict
        list.add(TalentNodeDef.of(
                "64543", 6885.97f, -1076.85f,
                "unbound_forces", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHILL_DURATION_ON_ENEMIES, 0.4),
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.4),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.25)
                ),
                List.of("37304")
        ));
        // [Stat] 15% increased [Armour]
        // [Stat] 50% reduced [ArmourBreak|Armour Break] taken
        // [Stat] 10% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "13489", -9778.05f, 1631.28f,
                "unbreakable", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_TAKEN, -0.5),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.1)
                ),
                List.of("675")
        ));
        // [Stat] 30% increased [StunThreshold|Stun Threshold]
        // [Stat] 30% increased [AilmentThreshold|Elemental Ailment Threshold]
        list.add(TalentNodeDef.of(
                "53921", -7025.59f, 5465.59f,
                "unbreaking", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.3)
                ),
                List.of("40596", "58838")
        ));
        // [Stat] 16% increased [Accuracy] Rating with One Handed [Melee] Weapons
        // [Stat] 16% increased [Accuracy] Rating with Two Handed [Melee] Weapons
        // [Stat] +0.2 metres to [Melee] [Strike] Range
        list.add(TalentNodeDef.of(
                "38888", 1598.81f, 3490.19f,
                "unerring_impact", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, 0.16),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_TWO_HANDED_MELEE_WEAPONS, 0.16),
                        TalentNodeGen.stat(StatIds.METRES_TO_MELEE_STRIKE_RANGE, 0.2)
                ),
                List.of("9941")
        ));
        // [Stat] 20% increased [Attack] Damage
        // [Stat] 30% increased [Accuracy] Rating while moving
        list.add(TalentNodeDef.of(
                "31189", 1152.33f, 6769.94f,
                "unexpected_finesse", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_MOVING, 0.3)
                ),
                List.of("33974")
        ));
        // [Stat] +4 to Maximum [Rage]
        // [Stat] Inherent loss of [Rage|Rage] is 20% slower
        list.add(TalentNodeDef.of(
                "8881", -6722.38f, 3368.19f,
                "unforgiving", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 4.0),
                        TalentNodeGen.stat(StatIds.INHERENT_LOSS_OF_RAGE_IS_SLOWER, -0.2)
                ),
                List.of("18004")
        ));
        // [Stat] 20% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        // [Stat] 6% reduced Movement Speed Penalty from using Skills while moving
        list.add(TalentNodeDef.of(
                "32543", 9805.18f, 2135.11f,
                "unhindered", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.2),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.06)
                ),
                List.of("60899", "64637")
        ));
        // [Stat] 24% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "51394", -6741.33f, 2531.86f,
                "unimpeded", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.24)
                ),
                List.of("31388")
        ));
        // [Stat] 30% increased [Stun] Buildup with [Melee|Melee Damage]
        // [Stat] [Projectile|Projectiles] deal 75% increased Damage against [HeavyStun|Heavy Stunned] Enemies
        list.add(TalentNodeDef.of(
                "20008", 5302.37f, 7361.56f,
                "unleash_fire", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_MELEE_DAMAGE, 0.3),
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, 0.75)
                ),
                List.of("49968", "63679")
        ));
        // [Stat] 30% increased [Armour] while stationary
        // [Stat] 30% increased Life Regeneration Rate while stationary
        list.add(TalentNodeDef.of(
                "16615", -11803.3f, -3590.0f,
                "unmoving_craiceann", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_STATIONARY, 0.3),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, 0.3)
                ),
                List.of("10713")
        ));
        // [Stat] +3% to all [ElementalDamage|Elemental] [Resistances]
        // [Stat] +2% to [MaximumResistances|Maximum Fire Resistance] if you have at least 5 Red [SupportGem|Support Gems] Socketed
        list.add(TalentNodeDef.of(
                "4547", -7612.36f, 8202.29f,
                "unnatural_resilience", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, 0.03),
                        TalentNodeGen.stat(StatIds.TO_MAX_FIRE_RESISTANCE_IF_YOU_HAVE_AT_LEAST_RED_SUPPORT_GEMS_SOCKETED, 0.02)
                ),
                List.of("41414")
        ));
        // [Stat] [Gain] 18% of [Physical] Damage as Extra [Lightning] Damage
        list.add(TalentNodeDef.of(
                "58197", -12570.0f, 397.045f,
                "unquenchable_iron", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, 0.18)
                ),
                List.of("42762", "50142")
        ));
        // [Stat] Enemies near Enemies you [Mark] are [Blind|Blinded]
        // [Stat] Enemies you [Mark] cannot deal [Critical|Critical Hits]
        list.add(TalentNodeDef.of(
                "51602", 8365.09f, 1769.99f,
                "unsight", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED, 1.0),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS, 1.0)
                ),
                List.of("23305")
        ));
        // [Stat] [Companion|Companions] have +30% to [Chaos] [Resistances|Resistance]
        // [Stat] [Companion|Companions] have +30% to all Elemental [Resistances]
        list.add(TalentNodeDef.of(
                "33585", 8395.94f, 5903.92f,
                "unspoken_bond", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_TO_CHAOS_RESISTANCES, 0.3),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.3)
                ),
                List.of("63246")
        ));
        // [Stat] Gain 3 [Volatility] when an [Allies|Allied] [Persistent] [Reviving] [Minion] is [MinionDeath|Killed]
        list.add(TalentNodeDef.of(
                "18485", -1940.55f, -8720.27f,
                "unstable_bond", "passives_cursemitigationclusternode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_VOLATILITY_WHEN_AN_ALLIES_PERSISTENT_REVIVING_MINION_IS_MINION_DEATH, 3.0)
                ),
                List.of("15809", "54036")
        ));
        // [Stat] 10% increased [Block] chance
        // [Stat] 15% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "33978", -4684.22f, 1982.53f,
                "unstoppable_barrier", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.15)
                ),
                List.of("30390", "31609")
        ));
        // [Stat] 15% increased [Attack] Speed if you've been Hit [Recently]
        // [Stat] 8% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "10774", -4801.92f, 7670.96f,
                "unyielding", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_VE_BEEN_HIT_RECENTLY, 0.15),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("35863", "59367")
        ));
        // [Stat] Recover 2% of maximum Life and Mana when you use a [Warcry]
        // [Stat] 24% increased [Warcry|Warcry] Speed
        // [Stat] 18% increased [Warcry|Warcry] Cooldown Recovery Rate
        list.add(TalentNodeDef.of(
                "1169", -6930.8f, 9162.67f,
                "urgent_call", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY, 0.02),
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.24),
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.18)
                ),
                List.of("63813", "9750")
        ));
        // [Stat] 40% increased Cooldown Recovery Rate for [Grenade] Skills
        // [Stat] 80% reduced [Grenade] Damage
        list.add(TalentNodeDef.of(
                "17303", -2149.07f, 11558.8f,
                "utility_ordnance", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS, 0.4),
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, -0.8)
                ),
                List.of("17026", "53895")
        ));
        // [Stat] [Offering|Offerings] cannot be damaged if they have been created [Recently]
        list.add(TalentNodeDef.of(
                "41033", 4146.33f, -7967.58f,
                "utmost_offering", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_CANNOT_BE_DAMAGED_IF_THEY_HAVE_BEEN_CREATED_RECENTLY)
                ),
                List.of("41654", "47821", "55872")
        ));
        // [Stat] 50% increased [Armour] while Bleeding
        // [Stat] 50% reduced [BuffMagnitude|Magnitude] of [Bleeding] on You
        list.add(TalentNodeDef.of(
                "3896", -5382.29f, 680.34f,
                "vale_dweller", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_BLEEDING, 0.5),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_ON_YOU, -0.5)
                ),
                List.of("23708")
        ));
        // [Stat] [Charm|Charms] gain 0.15 charges per Second
        list.add(TalentNodeDef.of(
                "12750", 3112.88f, 7857.0f,
                "vale_shelter", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARMS_GAIN_CHARGES_PER_SECOND, 0.15)
                ),
                List.of("48135")
        ));
        // [Stat] 10% of [Thorns|Thorns] Damage [LifeLeech|Leeched as Life]
        list.add(TalentNodeDef.of(
                "17762", -4149.68f, 6117.24f,
                "vengeance", "passives_thornsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_THORNS_DAMAGE_LEECHED_AS_HEALTH, 0.1)
                ),
                List.of("2964")
        ));
        // [Stat] Gain 5 [Rage] when [HitDamage|Hit] by an Enemy
        // [Stat] Every [Rage] also grants 1% increased [Armour]
        list.add(TalentNodeDef.of(
                "54937", -10171.3f, 1201.49f,
                "vengeful_fury", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 5.0),
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_ARMOR, 0.01)
                ),
                List.of("9106")
        ));
        // [Stat] 6% increased [Attack] Speed with One Handed [Melee] Weapons
        // [Stat] 15% increased [Accuracy] Rating with One Handed [Melee] Weapons
        // [Stat] +10 to [Strength|Strength] and [Dexterity|Dexterity]
        list.add(TalentNodeDef.of(
                "4238", 5585.65f, 8198.85f,
                "versatile_arms", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, 0.15),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH_AND_DEXTERITY, 10.0)
                ),
                List.of("46688", "60173")
        ));
        // [Stat] 3% increased [Attack] Speed per Enemy in [CloseRange|Close Range]
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "65193", -4519.4f, -1849.47f,
                "viciousness", "passives_increasedattackdamagenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_PER_ENEMY_IN_CLOSE_RANGE, 0.03),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("37414", "48714")
        ));
        // [Stat] 12% increased [Block] chance
        // [Stat] 10 Life gained when you [Block|Block]
        // [Stat] +2% to maximum [Block] chance
        list.add(TalentNodeDef.of(
                "22967", -6267.2f, 4178.78f,
                "vigilance", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.HEALTH_GAIN_WHEN_YOU_BLOCK, 10.0),
                        TalentNodeGen.stat(StatIds.TO_MAX_BLOCK_CHANCE, 0.02)
                ),
                List.of("12817", "49198")
        ));
        // [Stat] Recover 3% of Maximum Life when you collect a [Remnant]
        list.add(TalentNodeDef.of(
                "63739", 935.365f, -4903.67f,
                "vigorous_remnants", "passives_remnantnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT, 0.03)
                ),
                List.of("37593")
        ));
        // [Stat] [Minion|Minions] have 20% increased maximum Life
        // [Stat] [Minion|Minions] Regenerate 3% of maximum Life per second
        // [Stat] [Minion|Minions] have +13% to [Chaos] [Resistances|Resistance]
        list.add(TalentNodeDef.of(
                "36507", -1380.22f, -7433.57f,
                "vile_mending", "passives_minionchaosresistancenode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.13)
                ),
                List.of("54632", "60313")
        ));
        // [Stat] 33% increased Damage with [HitDamage|Hits] against Enemies affected by [Ailments]
        list.add(TalentNodeDef.of(
                "30695", 3027.38f, 1478.98f,
                "vile_wounds", "passives_clawsofthemagpie",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_AILMENTS, 0.33)
                ),
                List.of("13783", "19808")
        ));
        // [Stat] [Warcry|Warcries] [Empowered|Empower] an additional [Attack]
        list.add(TalentNodeDef.of(
                "31373", -8992.42f, 4305.49f,
                "vocal_empowerment", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK, 1.0)
                ),
                List.of("50561")
        ));
        // [Stat] 29% increased [Chaos] Damage
        // [Stat] Enemies you [Curse] have -3% to Chaos [Resistances|Resistance]
        list.add(TalentNodeDef.of(
                "3492", -1522.24f, -7017.57f,
                "void", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.29),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_CURSE_HAVE_TO_CHAOS_RESISTANCES, -0.03)
                ),
                List.of("19112")
        ));
        // [Stat] 8% increased Area of Effect
        // [Stat] 10% increased [CooldownRecovery|Cooldown Recovery Rate]
        list.add(TalentNodeDef.of(
                "11037", 181.295f, 3700.23f,
                "volatile_catalyst", "explosivegrenade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.08),
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("14572", "57039")
        ));
        // [Stat] 25% reduced [Grenade] [DetonationTime|Detonation Time]
        list.add(TalentNodeDef.of(
                "17882", 1450.76f, 7527.62f,
                "volatile_grenades", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DETONATION_TIME, -0.25)
                ),
                List.of("48856")
        ));
        // [Stat] [Gain] 8% of Damage as Extra [Fire] Damage
        // [Stat] +20% to [Resistances|Fire Resistance]
        list.add(TalentNodeDef.of(
                "11366", -1104.81f, -12174.9f,
                "volcanic_skin", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.TO_FIRE_RESISTANCE, 0.2)
                ),
                List.of("558")
        ));
        // [Stat] +15% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 10% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        // [Stat] 10% increased [Block] chance
        // [Stat] Gain 10 [EnergyShield|Energy Shield] when you Block
        // [Stat] Recover 10 Life when you [Block]
        list.add(TalentNodeDef.of(
                "31955", -7359.43f, -2908.76f,
                "voll_s_protection", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_ENERGY_SHIELD_WHEN_YOU_BLOCK, 10.0),
                        TalentNodeGen.stat(StatIds.RECOVER_HEALTH_WHEN_YOU_BLOCK, 10.0)
                ),
                List.of("51672")
        ));
        // [Stat] 15% increased [Attack] Speed while Leeching
        list.add(TalentNodeDef.of(
                "46060", -3285.98f, 4641.42f,
                "voracious", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_LEECHING, 0.15)
                ),
                List.of("29270", "29788")
        ));
        // [Stat] 10% reduced maximum Mana
        // [Stat] +10 to [Strength]
        // [Stat] 30% increased [Critical|Critical Hit Chance]
        list.add(TalentNodeDef.of(
                "27303", -6242.72f, 1322.89f,
                "vulgar_methods", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_MANA, -0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.3)
                ),
                List.of("16861")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 25% faster
        list.add(TalentNodeDef.of(
                "25211", -2461.15f, -11385.2f,
                "waning_hindrances", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.25)
                ),
                List.of("11330", "3355")
        ));
        // [Stat] 30% increased Damage per [Curse] on you
        // [Stat] 30% reduced [BuffEffect|effect] of [Curse|Curses] on you
        // [Stat] 60% increased [EnergyShield|Energy Shield] from Equipped [Focus]
        list.add(TalentNodeDef.of(
                "31925", -6073.0f, -8397.31f,
                "warding_fetish", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_PER_CURSE_ON_YOU, 0.3),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.3),
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.6)
                ),
                List.of("36474", "38596")
        ));
        // [Stat] 10% reduced [Flask] Charges used from Mana [Flask|Flasks]
        // [Stat] Remove a [Curse] when you use a Mana [Flask]
        list.add(TalentNodeDef.of(
                "47418", 7387.15f, -1540.02f,
                "warding_potions", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, -0.1),
                        TalentNodeGen.stat(StatIds.ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK, 1.0)
                ),
                List.of("23839", "48974")
        ));
        // [Stat] 40% reduced [Presence|Presence] Area of Effect
        // [Stat] [Allies] in your [Presence] Regenerate 5 [Rage] per second if you have gained [Rage] [Recently]
        list.add(TalentNodeDef.of(
                "53187", -5071.69f, 6012.7f,
                "warlord_berserker", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, -0.4),
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_REGENERATE_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY, 5.0)
                ),
                List.of("35011", "45215")
        ));
        // [Stat] [Allies|Allies] in your [Presence|Presence] deal 40% increased Damage
        // [Stat] 40% increased [Presence|Presence] Area of Effect
        list.add(TalentNodeDef.of(
                "14761", -4825.69f, 6154.73f,
                "warlord_leader", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_DEAL_INC_DAMAGE, 0.4),
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.4)
                ),
                List.of("45215", "45586")
        ));
        // [Stat] 25% reduced [Freeze|Freeze] Duration on you
        // [Stat] 60% increased [FreezeThreshold|Freeze Threshold]
        list.add(TalentNodeDef.of(
                "12998", 7530.37f, 5774.45f,
                "warm_the_heart", "passives_spellsuppresionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_DURATION_ON_YOU, -0.25),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.6)
                ),
                List.of("30463", "58971")
        ));
        // [Stat] Cannot be [LightStun|Light Stunned] if you haven't been [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "64650", 4789.34f, 7039.05f,
                "wary_dodging", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.CANNOT_BE_LIGHT_STUNNED_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, 1.0)
                ),
                List.of("30210", "35058")
        ));
        // [Stat] 15% increased Duration of [DamagingAilments|Damaging Ailments] on Enemies
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies affected by [Ailments]
        list.add(TalentNodeDef.of(
                "51213", 4563.3f, 4714.06f,
                "wasting", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES, 0.15),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_AILMENTS, 0.3)
                ),
                List.of("1773", "64747")
        ));
        // [Stat] 25% increased Damage with [HitDamage|Hits] against [Hinder|Hindered] Enemies
        // [Stat] 15% chance to [Hinder] Enemies on Hit with [Spell|Spells]
        list.add(TalentNodeDef.of(
                "61444", -3910.15f, -2981.11f,
                "wasting_casts", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_HINDERED_ENEMIES, 0.25),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS, 0.15)
                ),
                List.of("15180", "34096")
        ));
        // [Stat] [Recoup] 5% of damage taken by your [Totem|Totems] as Life
        // [Stat] Each [Totem] applies 2% increased Damage taken to Enemies in their [Presence]
        // [Deferred] 5580 Watchtowers presence-mark effect per advancement plan (talent injection deferred).
        list.add(TalentNodeDef.of(
                "5580", -9716.02f, 6484.44f,
                "watchtowers", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOUP_OF_DAMAGE_TAKEN_BY_YOUR_TOTEMS_AS_HEALTH, 0.05),
                        TalentNodeGen.stat(StatIds.EACH_TOTEM_APPLIES_INC_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE, 0.02)
                ),
                List.of("42710", "65287", "9568")
        ));
        // [Stat] Recover 2% of maximum Life when you use a Mana [Flask]
        // [Stat] Mana [Flask|Flasks] gain 0.1 charges per Second
        list.add(TalentNodeDef.of(
                "51509", 2622.75f, 6605.25f,
                "waters_of_life", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_USE_A_MANA_FLASK, 0.02),
                        TalentNodeGen.stat(StatIds.MANA_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1)
                ),
                List.of("35848")
        ));
        // [Stat] 20% increased [CriticalDamageBonus|Critical Damage Bonus] if you've consumed a [Charges|Power Charge] [Recently]
        // [Stat] Recover 5% of maximum Mana when you consume a [Charges|Power Charge]
        list.add(TalentNodeDef.of(
                "58198", -6580.5f, -8082.38f,
                "well_of_power", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.2),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE, 0.05)
                ),
                List.of("26863")
        ));
        // [Stat] 30% increased Mana Recovery from [Flask|Flasks]
        // [Stat] 8% increased [Attack] and Cast Speed during Effect of any Mana [Flask]
        list.add(TalentNodeDef.of(
                "2021", 9298.78f, 4895.5f,
                "wellspring", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_DURING_EFFECT_OF_ANY_MANA_FLASK, 0.08)
                ),
                List.of("17687", "27422")
        ));
        // [Stat] 8% increased [Attack] Speed with [Quarterstaff|Quarterstaves]
        // [Stat] [Knockback|Knocks Back] Enemies if you get a [Critical|Critical Hit] with a [Quarterstaff|Quarterstaff]
        list.add(TalentNodeDef.of(
                "37514", 10774.4f, -3838.27f,
                "whirling_assault", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_QUARTERSTAVES, 0.08),
                        TalentNodeGen.stat(StatIds.KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF, 1.0)
                ),
                List.of("32442", "4536", "64700")
        ));
        // [Stat] 50% chance to gain [Onslaught|Onslaught] on Killing Blow with [Axe|Axes]
        list.add(TalentNodeDef.of(
                "11178", -9362.64f, 6637.0f,
                "whirling_onslaught", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_ONSLAUGHT_ON_KILLING_BLOW_WITH_AXES, 0.5)
                ),
                List.of("39448")
        ));
        // [Stat] 20% reduced [Armour]
        // [Stat] 30% increased [Block] chance
        list.add(TalentNodeDef.of(
                "46384", -9952.33f, -5061.09f,
                "wide_barrier", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, -0.2),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.3)
                ),
                List.of("18746", "50510")
        ));
        // [Stat] 50% increased [Hazard] Area of Effect
        // [Stat] 20% reduced [Hazard] Damage
        list.add(TalentNodeDef.of(
                "65256", 6617.23f, 9944.27f,
                "widespread_coverage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_AREA_OF_EFFECT, 0.5),
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, -0.2)
                ),
                List.of("2128", "34845")
        ));
        // [Stat] [Gain] 4% of Damage as Extra [Cold] Damage
        // [Stat] [Gain] 4% of Damage as Extra [Lightning] Damage
        // [Stat] +10 to [Dexterity]
        list.add(TalentNodeDef.of(
                "7809", 8464.96f, -3633.16f,
                "wild_storm", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.04),
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, 0.04),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("60483")
        ));
        // [Stat] Unwithered enemies are [Withered] for 8 seconds when they enter your [Presence]
        // [Stat] 20% increased [Withered|Withered] [BuffMagnitude|Magnitude]
        list.add(TalentNodeDef.of(
                "44373", 7501.94f, -8629.84f,
                "wither_away", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.UNWITHERED_ENEMIES_ARE_WITHERED_FOR_SECONDS_WHEN_THEY_ENTER_YOUR_PRESENCE, 8.0),
                        TalentNodeGen.stat(StatIds.INC_WITHERED_BUFF_MAGNITUDE, 0.2)
                ),
                List.of("35380")
        ));
        // [Stat] 30% increased [Critical|Critical Hit] Chance if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
        list.add(TalentNodeDef.of(
                "57921", -6121.57f, -2601.56f,
                "wolf_s_howl", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.3)
                ),
                List.of("37509")
        ));
        // [Stat] [Charm|Charms] applied to you have 25% increased Effect
        list.add(TalentNodeDef.of(
                "62803", 7429.98f, 8930.1f,
                "woodland_aspect", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT, 0.25)
                ),
                List.of("38497", "48462")
        ));
        // [Stat] 20% increased bonuses gained from Equipped [Quiver]
        list.add(TalentNodeDef.of(
                "30132", 7487.61f, 1659.86f,
                "wrapped_quiver", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.2)
                ),
                List.of("49130", "54631", "58416")
        ));
        // [Stat] 40% increased [ElementalAilments|Elemental] [AilmentApplication|Ailment Application] if you have [Shapeshift|Shapeshifted] to an Animal form [Recently]
        list.add(TalentNodeDef.of(
                "35417", -5971.84f, -2042.77f,
                "wyvern_s_breath", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.4)
                ),
                List.of("16506", "28770")
        ));
        // [Stat] 20% increased Area of Effect of [Curse|Curses]
        // [Stat] 10% increased [Curse] [BuffMagnitude|Magnitudes]
        // [Stat] Enemies you [Curse] are [Hinder|Hindered], with 15% reduced Movement Speed
        list.add(TalentNodeDef.of(
                "50485", 2333.88f, -5053.36f,
                "zone_of_control", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_OF_CURSES, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.1),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_CURSE_ARE_HINDERED_WITH_INC_MOVEMENT_SPEED, -0.15)
                ),
                List.of("28229")
        ));
        return list;
    }
}
