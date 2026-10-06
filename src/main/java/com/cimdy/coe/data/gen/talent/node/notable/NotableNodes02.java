package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;

public final class NotableNodes02 {

    private NotableNodes02() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "23427", -487.565f, -11452.5f,
                "chilled_to_the_bone", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHILL_DURATION_ON_ENEMIES, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.3)
                ),
                List.of("11337", "62914")
        ));

        list.add(TalentNodeDef.of(
                "5686", -8340.71f, 2532.76f,
                "chillproof", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.3),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.3),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.3)
                ),
                List.of("19820")
        ));

        list.add(TalentNodeDef.of(
                "39990", -2808.46f, -5826.87f,
                "chronomancy", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.2),
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.1)
                ),
                List.of("13294")
        ));

        list.add(TalentNodeDef.of(
                "21213", -9354.92f, -3744.49f,
                "cirel_of_tarth_s_light", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_LIGHT_RADIUS, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.1),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.1)
                ),
                List.of("32764", "37609", "60332")
        ));

        list.add(TalentNodeDef.of(
                "42781", 3191.38f, 1194.93f,
                "clean_shot", "passives_projectilesnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.15)
                ),
                List.of("22795", "55429")
        ));

        list.add(TalentNodeDef.of(
                "57805", 491.715f, 5712.0f,
                "clear_space", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_KNOCK_ENEMIES_BACK_WITH_HIT_DAMAGE_AT_CLOSE_RANGE, 0.2)
                ),
                List.of("43444", "7788")
        ));

        list.add(TalentNodeDef.of(
                "63659", 9171.85f, -6835.22f,
                "clever_construction", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, 0.25)
                ),
                List.of("27834", "33964")
        ));

        list.add(TalentNodeDef.of(
                "4627", 494.695f, -8654.42f,
                "climate_change", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.GAIN_OF_COLD_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_FREEZE_ENEMIES, 0.25)
                ),
                List.of("4113", "55572")
        ));

        list.add(TalentNodeDef.of(
                "38479", 1757.9f, 6662.84f,
                "close_confines", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU, 0.5)
                ),
                List.of("17553", "19573")
        ));

        list.add(TalentNodeDef.of(
                "29514", -380.921f, 6320.54f,
                "cluster_bombs", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DETONATION_TIME, 0.5),
                        TalentNodeGen.stat(StatIds.GRENADE_SKILLS_FIRE_AN_ADDITIONAL_PROJECTILE, 1.0)
                ),
                List.of("3109", "36169")
        ));

        list.add(TalentNodeDef.of(
                "44330", 427.715f, 6276.48f,
                "coated_arms", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.25),
                        TalentNodeGen.stat(StatIds.ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_INC_CHANCE_TO_INFLICT_AILMENTS, 0.2)
                ),
                List.of("16538")
        ));

        list.add(TalentNodeDef.of(
                "4423", 11206.4f, -5293.47f,
                "coated_knife", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.CRITICAL_HITS_WITH_DAGGERS_HAVE_A_CHANCE_TO_POISON_THE_ENEMY, 0.25)
                ),
                List.of("54058")
        ));

        list.add(TalentNodeDef.of(
                "35618", -11287.3f, -462.918f,
                "cold_coat", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.25),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_DURATION_ON_YOU, -0.25),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.25)
                ),
                List.of("19751", "36504")
        ));

        list.add(TalentNodeDef.of(
                "26518", -7273.08f, -1197.4f,
                "cold_nature", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CHILL_DURATION_ON_ENEMIES, 0.15)
                ),
                List.of("43653")
        ));

        list.add(TalentNodeDef.of(
                "47363", -878.695f, 5009.24f,
                "colossal_weapon", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.12),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("51825")
        ));

        list.add(TalentNodeDef.of(
                "10053", 2431.04f, 1403.46f,
                "combat_alchemy", "passives_flasknotablecritstrikerecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES, 0.1),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, 0.2)
                ),
                List.of("19470", "50328")
        ));

        list.add(TalentNodeDef.of(
                "28044", 10451.5f, 3790.48f,
                "coming_calamity", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ICE, 0.4),
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ASH, 0.4),
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_THUNDER, 0.4)
                ),
                List.of("178", "28835")
        ));

        list.add(TalentNodeDef.of(
                "42660", -7912.15f, -604.357f,
                "commanding_rage", "passives_ragenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.EVERY_FIVE_RAGE_ALSO_GRANTS_YOU_INC_MINION_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_YOU_INC_MINION_DAMAGE, 0.01)
                ),
                List.of("10484", "16413")
        ));

        list.add(TalentNodeDef.of(
                "49153", -5911.96f, -4603.15f,
                "comradery", "passives_miniondamageblue",
                List.of(),
                List.of("61471")
        ));

        list.add(TalentNodeDef.of(
                "34617", -5810.94f, -7066.13f,
                "conall_the_hunted", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.05),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_BLEEDING_WHILE_SHAPESHIFTED, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_MAIM_WHILE_SHAPESHIFTED, 1.0)
                ),
                List.of("32964")
        ));

        list.add(TalentNodeDef.of(
                "36931", 3855.19f, -2760.92f,
                "concussive_attack", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("53938", "64056")
        ));

        list.add(TalentNodeDef.of(
                "52257", 9455.83f, 2570.72f,
                "conductive_embrace", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_MAX_LIGHTNING_RESISTANCE_IF_YOU_HAVE_AT_LEAST_GREEN_SUPPORT_GEMS_SOCKETED, 0.02)
                ),
                List.of("57069")
        ));

        list.add(TalentNodeDef.of(
                "34300", 4356.01f, -3242.59f,
                "conservative_casting", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.15)
                ),
                List.of("31888", "45481")
        ));

        list.add(TalentNodeDef.of(
                "15030", 4627.73f, 1895.98f,
                "consistent_intake", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING, 1.0)
                ),
                List.of("39658", "45693")
        ));

        list.add(TalentNodeDef.of(
                "54640", -5055.9f, -4742.69f,
                "constricting", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("27900", "55422", "62376")
        ));

        list.add(TalentNodeDef.of(
                "30748", 6032.38f, -8559.51f,
                "controlled_chaos", "passives_cursemitigationclusternode",
                List.of(
                        TalentNodeGen.stat(StatIds.MAX_VOLATILITY_IS, 30.0)
                ),
                List.of("44369")
        ));

        list.add(TalentNodeDef.of(
                "13823", 3603.01f, -9634.68f,
                "controlling_magic", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.25),
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_YOU, -0.25)
                ),
                List.of("32054", "46088")
        ));

        list.add(TalentNodeDef.of(
                "36623", 4067.74f, -9977.13f,
                "convalescence", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE, -0.1),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.2)
                ),
                List.of("31630")
        ));

        list.add(TalentNodeDef.of(
                "56776", 5358.15f, 3794.03f,
                "cooked", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.6),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD, -0.25)
                ),
                List.of("45609", "9046")
        ));

        list.add(TalentNodeDef.of(
                "6133", -10797.6f, -5061.09f,
                "core_of_the_guardian", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, -0.2),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.3)
                ),
                List.of("33402", "440")
        ));

        list.add(TalentNodeDef.of(
                "49769", -7958.39f, -9813.39f,
                "corruption_endures", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_AVOID_DEATH_FROM_HIT_DAMAGE, 0.07)
                ),
                List.of("1628", "49258")
        ));

        list.add(TalentNodeDef.of(
                "27761", 3776.11f, 2965.62f,
                "counterstancing", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.SUCCESSFULLY_PARRYING_A_MELEE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_RANGED_ATTACK, 0.4),
                        TalentNodeGen.stat(StatIds.SUCCESSFULLY_PARRYING_A_PROJECTILE_HIT_DAMAGE_GRANTS_INC_DAMAGE_TO_YOUR_NEXT_MELEE_ATTACK, 0.4)
                ),
                List.of("13379", "36997")
        ));

        list.add(TalentNodeDef.of(
                "50687", -609.775f, 9926.97f,
                "coursing_energy", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.4),
                        TalentNodeGen.stat(StatIds.INC_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES, 0.3)
                ),
                List.of("8875")
        ));

        list.add(TalentNodeDef.of(
                "12940", -3604.52f, -11846.1f,
                "cower_before_the_first_ones", "passives_damage_blue",
                List.of(),
                List.of("21549", "27572", "37694", "44560", "64083")
        ));

        list.add(TalentNodeDef.of(
                "63451", -4778.32f, 5780.43f,
                "cranial_impact", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY, 1.0)
                ),
                List.of("44406", "57846", "64312")
        ));

        list.add(TalentNodeDef.of(
                "34168", 2569.54f, -2320.22f,
                "crashing_wave", "passives_criticalstrikesnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_SECONDS, 0.25)
                ),
                List.of("4157", "55088")
        ));

        list.add(TalentNodeDef.of(
                "9323", -10635.9f, -2928.04f,
                "craving_slaughter", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS, 15.0)
                ),
                List.of("14509", "65472")
        ));

        list.add(TalentNodeDef.of(
                "20511", -8156.53f, -3363.34f,
                "cremating_cries", "passives_firedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, 0.15)
                ),
                List.of("50228", "53804")
        ));

        list.add(TalentNodeDef.of(
                "19715", -1462.89f, -11950.5f,
                "cremation", "passives_firedamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, 0.06)
                ),
                List.of("62603")
        ));

        list.add(TalentNodeDef.of(
                "43677", 4605.3f, 1275.23f,
                "crippling_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.25)
                ),
                List.of("4083")
        ));

        list.add(TalentNodeDef.of(
                "57204", 1936.35f, -4097.53f,
                "critical_exploit", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.25)
                ),
                List.of("21779", "60107")
        ));

        list.add(TalentNodeDef.of(
                "5501", 0.005f, -4439.9f,
                "critical_overload", "passives_annihilation",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("32404", "48821")
        ));

        list.add(TalentNodeDef.of(
                "45488", 379.351f, 5271.04f,
                "cross_strike", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_DUAL_WIELDING, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_DUAL_WIELDING, 0.03)
                ),
                List.of("4377")
        ));

        list.add(TalentNodeDef.of(
                "18465", -3595.15f, -1520.81f,
                "cruel_fate", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS, 0.2)
                ),
                List.of("36602", "39540")
        ));

        list.add(TalentNodeDef.of(
                "42981", -2178.93f, 5209.16f,
                "cruel_methods", "passives_armorbreak2bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.4),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("23993", "56104")
        ));

        list.add(TalentNodeDef.of(
                "35739", -1717.94f, 9519.42f,
                "crushing_judgement", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_DURATION, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.25)
                ),
                List.of("37665", "42410")
        ));

        list.add(TalentNodeDef.of(
                "18505", -7680.05f, 5085.9f,
                "crushing_verdict", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, -0.05),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.5)
                ),
                List.of("45090")
        ));

        list.add(TalentNodeDef.of(
                "38895", 3808.99f, 8549.36f,
                "crystal_elixir", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACK_SKILLS_DURING_ANY_FLASK_EFFECT, 0.4)
                ),
                List.of("8697")
        ));

        list.add(TalentNodeDef.of(
                "61026", -4261.32f, -6225.9f,
                "crystalline_flesh", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.05)
                ),
                List.of("17378", "41991")
        ));

        list.add(TalentNodeDef.of(
                "32151", -2211.36f, 10325.0f,
                "crystalline_resistance", "passives_gemling_gemlingnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED, 0.01)
                ),
                List.of("3685")
        ));

        list.add(TalentNodeDef.of(
                "5332", 766.473f, 11310.4f,
                "crystallised_immunities", "passives_gemling_gemlingnode",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_CHILL_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_BLUE, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_SHOCK_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_GREEN, 1.0)
                ),
                List.of("32436", "3744", "60203")
        ));

        list.add(TalentNodeDef.of(
                "36341", 2812.83f, 11910.9f,
                "cull_the_hordes", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.4)
                ),
                List.of("41171")
        ));

        list.add(TalentNodeDef.of(
                "13708", -587.031f, 5300.9f,
                "curved_weapon", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.15),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("51825")
        ));

        list.add(TalentNodeDef.of(
                "32507", -1682.34f, -9147.25f,
                "cut_to_the_bone", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.2)
                ),
                List.of("12851")
        ));

        list.add(TalentNodeDef.of(
                "9642", 303.518f, -3928.97f,
                "dampening_shield", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.28),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("517", "14666")
        ));

        list.add(TalentNodeDef.of(
                "7128", -6941.57f, -7077.43f,
                "dangerous_blossom", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("34187")
        ));

        list.add(TalentNodeDef.of(
                "63074", 2070.55f, -11153.8f,
                "dark_entries", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LEVEL_OF_ALL_CHAOS_SKILLS, 1.0)
                ),
                List.of("63891")
        ));

        list.add(TalentNodeDef.of(
                "10500", -7049.55f, 5850.06f,
                "dazing_blocks", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ENEMIES_WHOSE_HIT_DAMAGE_YOU_BLOCK_WITH_A_RAISED_SHIELD, 1.0)
                ),
                List.of("21161", "6900")
        ));

        list.add(TalentNodeDef.of(
                "30523", -612.118f, -10274.6f,
                "dead_can_dance", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.25),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_EVASION, 0.25)
                ),
                List.of("50720")
        ));

        list.add(TalentNodeDef.of(
                "49618", -2620.07f, 7379.36f,
                "deadly_flourish", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_CRITICAL_HIT_CHANCE, 0.25)
                ),
                List.of("38663", "62039")
        ));

        list.add(TalentNodeDef.of(
                "13724", 4384.03f, -4974.35f,
                "deadly_force", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_SECONDS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.15)
                ),
                List.of("20236", "4519")
        ));

        list.add(TalentNodeDef.of(
                "29288", 5336.9f, -6322.31f,
                "deadly_invocations", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS, 0.5)
                ),
                List.of("16024")
        ));

        list.add(TalentNodeDef.of(
                "5777", -3114.56f, -2353.21f,
                "deadly_swarm", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("44951", "58651")
        ));

        list.add(TalentNodeDef.of(
                "36408", -8734.51f, -7967.38f,
                "deadly_thorns", "passives_physicaldamagenode",
                List.of(),
                List.of("34143")
        ));

        list.add(TalentNodeDef.of(
                "38053", -8595.72f, -1095.54f,
                "deafening_cries", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY, 0.08)
                ),
                List.of("28564", "9528")
        ));

        list.add(TalentNodeDef.of(
                "8904", 8000.05f, 4152.81f,
                "death_from_afar", "passives_chainingprojectiles",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.25),
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.25)
                ),
                List.of("26400")
        ));

        list.add(TalentNodeDef.of(
                "17664", 8363.42f, 2346.58f,
                "decisive_retreat", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_HAVE_EXITED_YOUR_PRESENCE_RECENTLY, 0.5)
                ),
                List.of("11463", "33348", "7465")
        ));

        list.add(TalentNodeDef.of(
                "5594", 3269.96f, -7400.13f,
                "decrepifying_curse", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_YOU_INFLICT_AGAINST_CURSED_ENEMIES, 0.2)
                ),
                List.of("31908", "50107")
        ));

        list.add(TalentNodeDef.of(
                "16142", 10817.1f, 2793.58f,
                "deep_freeze", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.ENEMIES_FREEZE_BY_YOU_HAVE_TO_COLD_RESISTANCE, -0.08)
                ),
                List.of("31825", "5390")
        ));

        list.add(TalentNodeDef.of(
                "40166", 6312.8f, -6216.8f,
                "deep_trance", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.INC_COST_EFFICIENCY, 0.15)
                ),
                List.of("20820", "32399")
        ));

        list.add(TalentNodeDef.of(
                "33216", -927.125f, 9061.83f,
                "deep_wounds", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_SECONDS, 4.0)
                ),
                List.of("315", "63814")
        ));

        list.add(TalentNodeDef.of(
                "64327", -3303.47f, 2293.03f,
                "defender_s_resolve", "passives_steelspan",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.YOUR_HEAVY_STUN_BUILDUP_EMPTIES_FASTER, 0.5)
                ),
                List.of("18629", "39517")
        ));

        list.add(TalentNodeDef.of(
                "45612", -4431.08f, -520.435f,
                "defensive_reflexes", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.MANA_GAIN_WHEN_YOU_BLOCK, 5.0)
                ),
                List.of("53901")
        ));

        list.add(TalentNodeDef.of(
                "10681", -10375.0f, -5793.09f,
                "defensive_stance", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_BLOCK_CHANCE, 0.04)
                ),
                List.of("27581", "58125")
        ));

        list.add(TalentNodeDef.of(
                "32354", -1863.48f, 6665.33f,
                "defiance", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_WHEN_ON_LOW_HEALTH, 0.8)
                ),
                List.of("34433", "6626")
        ));

        list.add(TalentNodeDef.of(
                "45329", 6129.78f, 9643.2f,
                "delayed_danger", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DURATION, 0.3),
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.4)
                ),
                List.of("11015", "2128")
        ));

        list.add(TalentNodeDef.of(
                "38570", -2274.07f, 11775.3f,
                "demolitionist", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FOR_EVERY_DIFFERENT_GRENADE_FIRED_IN_THE_PAST_SECONDS, 0.04)
                ),
                List.of("47623", "53895")
        ));

        list.add(TalentNodeDef.of(
                "4931", -4117.42f, -8299.44f,
                "dependable_ward", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.12)
                ),
                List.of("21404", "44082")
        ));

        list.add(TalentNodeDef.of(
                "28267", -7812.34f, -5515.53f,
                "desensitisation", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.25),
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.25)
                ),
                List.of("2672", "59061")
        ));

        list.add(TalentNodeDef.of(
                "56616", -2977.66f, 4319.94f,
                "desperate_times", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_ON_LOW_HEALTH, 0.015),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH, 0.4)
                ),
                List.of("13562")
        ));

        list.add(TalentNodeDef.of(
                "27417", 8677.92f, -7180.28f,
                "destructive_apparatus", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_DAMAGE, 0.25)
                ),
                List.of("64295", "8644")
        ));

        list.add(TalentNodeDef.of(
                "14343", 3946.1f, 8993.8f,
                "deterioration", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE, 1.0),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT, 0.2)
                ),
                List.of("54746")
        ));

        list.add(TalentNodeDef.of(
                "24753", -3044.02f, 6253.4f,
                "determined_precision", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_AT_CLOSE_RANGE, 0.3),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("151", "34747", "56567")
        ));

        list.add(TalentNodeDef.of(
                "48006", -2304.23f, 3578.46f,
                "devastation", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.12)
                ),
                List.of("55478")
        ));

        list.add(TalentNodeDef.of(
                "37688", 9516.92f, -6341.28f,
                "devestating_devices", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_DAMAGE, 0.25)
                ),
                List.of("34449", "43938")
        ));

        list.add(TalentNodeDef.of(
                "28950", -2308.21f, -1862.64f,
                "devoted_protector", "passives_bodysoul",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH_AND_INTELLIGENCE, 5.0)
                ),
                List.of("10156", "12777", "19873", "41838", "59695", "63469")
        ));

        list.add(TalentNodeDef.of(
                "2344", -7133.29f, -7035.15f,
                "dimensional_weakspot", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_HAVE_CHANCE_TO_TREAT_ENEMY_MONSTER_ELEMENTAL_DAMAGE_RESISTANCES_VALUES_AS_INVERTED, 0.15)
                ),
                List.of("16485", "23331", "31778")
        ));

        list.add(TalentNodeDef.of(
                "24483", -3682.63f, -4807.36f,
                "direct_approach", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_AFFECTED_BY_NO_ELEMENTAL_AILMENTS, 0.35)
                ),
                List.of("32660", "51534")
        ));

        list.add(TalentNodeDef.of(
                "44573", 7755.88f, -1645.82f,
                "disciplined_training", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.1),
                        TalentNodeGen.stat(StatIds.SKILLS_GAIN_COMBO_SLOWER, -0.2)
                ),
                List.of("24883")
        ));

        list.add(TalentNodeDef.of(
                "38459", 3784.82f, 1041.23f,
                "disorientation", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_DURATION, 0.25),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.25)
                ),
                List.of("5988", "63610")
        ));

        list.add(TalentNodeDef.of(
                "58939", 1879.79f, 4860.0f,
                "dispatch_foes", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY, 0.4)
                ),
                List.of("30990")
        ));

        list.add(TalentNodeDef.of(
                "52245", 9759.71f, -5970.48f,
                "distant_dreamer", "passives_cursemitigationclusternotable",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_WITHERED_ON_YOU, -0.5)
                ),
                List.of("19359")
        ));

        list.add(TalentNodeDef.of(
                "32721", 6495.85f, -2014.71f,
                "distracted_target", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES, 0.3)
                ),
                List.of("10011", "11836")
        ));

        list.add(TalentNodeDef.of(
                "44765", 266.481f, 8487.6f,
                "distracting_presence", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.1),
                        TalentNodeGen.stat(StatIds.ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_COOLDOWN_RECOVERY_RATE, -0.1)
                ),
                List.of("26663")
        ));

        list.add(TalentNodeDef.of(
                "47514", 10507.4f, -356.595f,
                "dizzying_hits", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_DAZED_ENEMIES, 0.25)
                ),
                List.of("26572", "39128")
        ));

        list.add(TalentNodeDef.of(
                "1420", 4632.81f, -3613.45f,
                "dizzying_sweep", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.1),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("37250", "55829")
        ));

        list.add(TalentNodeDef.of(
                "58894", -8807.73f, -2227.88f,
                "dominus_providence", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_EXPOSURE, 1.0),
                        TalentNodeGen.stat(StatIds.UNAFFECTED_BY_ELEMENTAL_WEAKNESS)
                ),
                List.of("25648", "26592")
        ));

        list.add(TalentNodeDef.of(
                "57190", 1188.06f, 8751.62f,
                "doomsayer", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.25),
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, 0.3)
                ),
                List.of("27859")
        ));

        list.add(TalentNodeDef.of(
                "1502", -7167.85f, -3642.67f,
                "draiocht_cleansing", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS, 2.0)
                ),
                List.of("36556", "42635")
        ));

        list.add(TalentNodeDef.of(
                "32858", 9885.62f, 6096.14f,
                "dread_engineer_s_concoction", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_AGAINST_POISONED_ENEMIES, 0.35)
                ),
                List.of("1073", "32364")
        ));

        list.add(TalentNodeDef.of(
                "11838", 4082.63f, -9003.35f,
                "dreamcatcher", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_ON_FULL_ENERGY_SHIELD, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.75)
                ),
                List.of("33112", "36450")
        ));

        list.add(TalentNodeDef.of(
                "40073", 2343.9f, -9085.31f,
                "drenched", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.4),
                        TalentNodeGen.stat(StatIds.GAIN_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.05)
                ),
                List.of("57810")
        ));

        list.add(TalentNodeDef.of(
                "3688", 4248.36f, -6479.58f,
                "dynamism", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY, 0.4),
                        TalentNodeGen.stat(StatIds.META_SKILLS_GAIN_INC_ENERGY, 0.15)
                ),
                List.of("47614", "52260")
        ));

        list.add(TalentNodeDef.of(
                "19104", 2770.83f, 1599.66f,
                "eagle_eye", "passives_eagleeye",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ACCURACY, 30.0),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.1)
                ),
                List.of("29582", "43923")
        ));

        list.add(TalentNodeDef.of(
                "10315", 12637.9f, 2423.74f,
                "easy_going", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, 0.25)
                ),
                List.of("44699", "47443")
        ));

        list.add(TalentNodeDef.of(
                "64525", -7494.69f, 1310.92f,
                "easy_target", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.YOUR_HIT_DAMAGE_CANNOT_BE_EVASION_BY_HEAVY_STUNNED_ENEMIES, 1.0)
                ),
                List.of("7204")
        ));

        list.add(TalentNodeDef.of(
                "18972", -9524.42f, -6265.02f,
                "echoes_of_ferocity", "passives_druidshapeshiftbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.15)
                ),
                List.of("34490")
        ));

        list.add(TalentNodeDef.of(
                "60692", 3842.13f, -4660.29f,
                "echoing_flames", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_IGNITED_AN_ENEMY_RECENTLY, 0.3)
                ),
                List.of("16367", "22359")
        ));

        list.add(TalentNodeDef.of(
                "5257", 3625.63f, -5035.29f,
                "echoing_frost", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_CHILLED_AN_ENEMY_RECENTLY, 0.3)
                ),
                List.of("16367", "38338")
        ));

        list.add(TalentNodeDef.of(
                "7302", 8409.99f, -7924.9f,
                "echoing_pulse", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.ECHOED_SPELLS_HAVE_INC_AREA_OF_EFFECT, 0.25)
                ),
                List.of("52615")
        ));

        list.add(TalentNodeDef.of(
                "5703", 4058.64f, -5035.29f,
                "echoing_thunder", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_IF_YOU_VE_SHOCKED_AN_ENEMY_RECENTLY, 0.3)
                ),
                List.of("16367", "9141")
        ));

        list.add(TalentNodeDef.of(
                "33093", 8659.99f, -7674.9f,
                "effervescent", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_FOR_EACH_DIFFERENT_SPELL_YOU_VE_CAST_IN_THE_LAST_EIGHT_SECONDS, 0.04)
                ),
                List.of("25729")
        ));

        list.add(TalentNodeDef.of(
                "46692", 2406.25f, 6730.25f,
                "efficient_alchemy", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_AND_CHARM_CHARGES_GAIN, 0.2),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS_WHILE_YOU_HAVE_AN_ACTIVE_CHARM, 0.4)
                ),
                List.of("53196")
        ));

        list.add(TalentNodeDef.of(
                "16790", 1746.18f, -5285.12f,
                "efficient_casting", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.2)
                ),
                List.of("2732")
        ));

        list.add(TalentNodeDef.of(
                "30408", 9866.28f, 4990.9f,
                "efficient_contraptions", "passives_trap",
                List.of(
                        TalentNodeGen.stat(StatIds.HAZARDS_HAVE_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED, 0.15)
                ),
                List.of("1778")
        ));

        list.add(TalentNodeDef.of(
                "42245", 5179.44f, -5958.69f,
                "efficient_inscriptions", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.META_SKILLS_HAVE_INC_RESERVATION_EFFICIENCY, 0.2)
                ),
                List.of("16024", "18167", "9069")
        ));

        list.add(TalentNodeDef.of(
                "94", 1962.68f, -5160.12f,
                "efficient_killing", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.02)
                ),
                List.of("52241")
        ));

        list.add(TalentNodeDef.of(
                "53683", 1904.34f, 11525.0f,
                "efficient_loading", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE, 0.3)
                ),
                List.of("25807", "30082")
        ));

        list.add(TalentNodeDef.of(
                "3894", -2453.32f, -9822.84f,
                "eldritch_will", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH_MANA_AND_ENERGY_SHIELD, 0.03),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.15),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.15)
                ),
                List.of("13307", "46275")
        ));

        list.add(TalentNodeDef.of(
                "55708", 1506.34f, -11023.1f,
                "electric_amplification", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.18),
                        TalentNodeGen.stat(StatIds.GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, 0.06)
                ),
                List.of("36293")
        ));

        list.add(TalentNodeDef.of(
                "56988", 8506.18f, -3240.16f,
                "electric_blood", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_LIGHTNING_RESISTANCE, 0.01),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.5)
                ),
                List.of("36540")
        ));

        list.add(TalentNodeDef.of(
                "30546", -8285.9f, -8071.65f,
                "electrified_claw", "passives_druidshapeshiftwyvernnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("27216", "27611")
        ));

        list.add(TalentNodeDef.of(
                "56767", 8292.8f, -5919.16f,
                "electrifying_daze", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_DAZED_ENEMIES, 0.12)
                ),
                List.of("19461", "43338")
        ));

        list.add(TalentNodeDef.of(
                "26291", -6759.66f, -448.065f,
                "electrifying_nature", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.15)
                ),
                List.of("30662", "48935")
        ));

        list.add(TalentNodeDef.of(
                "7275", -798.455f, 9336.63f,
                "electrocuting_exposure", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_ELECTROCUTED_ENEMIES, 0.25)
                ),
                List.of("23724")
        ));

        list.add(TalentNodeDef.of(
                "36364", 9287.06f, 3103.82f,
                "electrocution", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_ELECTROCUTE_HAVE_INC_DAMAGE_TAKEN, 0.2)
                ),
                List.of("63600")
        ));

        list.add(TalentNodeDef.of(
                "43090", 8601.56f, -234.445f,
                "electrotherapy", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.3)
                ),
                List.of("44490")
        ));

        list.add(TalentNodeDef.of(
                "3823", -478.245f, -3834.19f,
                "elemental_force", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, 0.03),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE, 0.2)
                ),
                List.of("10192", "22821")
        ));

        list.add(TalentNodeDef.of(
                "59781", -1569.98f, -10888.3f,
                "embodiment_of_death", "passives_archonofundeathnoteble",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_BLEEDING_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, 1.0)
                ),
                List.of("45333")
        ));

        list.add(TalentNodeDef.of(
                "10612", -533.743f, -10888.4f,
                "embodiment_of_frost", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_FREEZE_AND_CHILL_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, 1.0)
                ),
                List.of("50535")
        ));

        list.add(TalentNodeDef.of(
                "15991", 584.803f, -10860.1f,
                "embodiment_of_lightning", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_SHOCK_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, 1.0)
                ),
                List.of("51234")
        ));

        list.add(TalentNodeDef.of(
                "43423", 3608.13f, 8348.5f,
                "emboldened_avatar", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.5),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.25)
                ),
                List.of("56818")
        ));

        list.add(TalentNodeDef.of(
                "10727", -9137.76f, -4542.72f,
                "emboldening_casts", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL_YOU_VE_USED_IN_THE_PAST_SECONDS, 0.12)
                ),
                List.of("29098")
        ));

        list.add(TalentNodeDef.of(
                "34553", -1790.23f, -5081.19f,
                "emboldening_lead", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_IF_YOU_VE_HIT_DAMAGE_RECENTLY, 0.3)
                ),
                List.of("14033")
        ));

        list.add(TalentNodeDef.of(
                "9928", 8602.39f, -5294.69f,
                "embracing_frost", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_COLD_RESISTANCE, 0.01),
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.1)
                ),
                List.of("32672")
        ));

        list.add(TalentNodeDef.of(
                "8782", -1008.3f, -7499.54f,
                "empowering_infusions", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.35)
                ),
                List.of("3091")
        ));

        list.add(TalentNodeDef.of(
                "8397", -1573.72f, -5206.19f,
                "empowering_remains", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_ONE_OF_YOUR_MINIONS_HAS_DIED_RECENTLY, 0.4)
                ),
                List.of("41130")
        ));

        list.add(TalentNodeDef.of(
                "40985", 801.555f, -6943.69f,
                "empowering_remnants", "passives_remnantnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE, 0.15)
                ),
                List.of("34866", "62679")
        ));
        return list;
    }
}
