package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NotableNodes05 {

    private NotableNodes05() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "27108", -1533.68f, 3136.54f,
                "mass_hysteria", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_ATTACK_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.06)
                ),
                List.of("47796")
        ));

        list.add(TalentNodeDef.of(
                "34340", -1956.3f, 2892.54f,
                "mass_rejuvenation", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_REGENERATE_OF_YOUR_MAX_HEALTH_PER_SECOND, 0.01),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.005)
                ),
                List.of("17294")
        ));

        list.add(TalentNodeDef.of(
                "30341", 4953.65f, 2137.3f,
                "master_fletching", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.2)
                ),
                List.of("12498", "1995", "37946")
        ));

        list.add(TalentNodeDef.of(
                "40345", 1070.74f, -9662.83f,
                "master_of_hexes", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, -0.25),
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.18)
                ),
                List.of("37991")
        ));

        list.add(TalentNodeDef.of(
                "27513", 9804.9f, -340.505f,
                "material_solidification", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.15)
                ),
                List.of("37026")
        ));

        list.add(TalentNodeDef.of(
                "11886", -5067.79f, -1582.84f,
                "mauling_stuns", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_AGAINST_ENEMIES_WITHIN_METRES, 0.4),
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, 0.2)
                ),
                List.of("51267")
        ));

        list.add(TalentNodeDef.of(
                "25620", 3646.31f, -6878.38f,
                "meat_recycling", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES, 0.15)
                ),
                List.of("30539", "55405")
        ));

        list.add(TalentNodeDef.of(
                "34940", -8989.31f, -1318.15f,
                "meditative_focus", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_CHANNELLING, 0.6),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING, 0.3)
                ),
                List.of("6874")
        ));

        list.add(TalentNodeDef.of(
                "3215", 1600.5f, -4006.86f,
                "melding", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.4),
                        TalentNodeGen.stat(StatIds.INC_MAX_MANA, -0.1)
                ),
                List.of("62436")
        ));

        list.add(TalentNodeDef.of(
                "43939", -8526.46f, -5419.21f,
                "melting_flames", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_INC_FIRE_DAMAGE_FOR_EACH_SECOND_THEY_HAVE_EVER_BEEN_IGNITED_BY_YOU_UP_TO_A_MAX_OF, 0.01)
                ),
                List.of("56214")
        ));

        list.add(TalentNodeDef.of(
                "9652", 7210.83f, -7151.38f,
                "mending_deflection", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_FROM_DEFLECTED_HIT_DAMAGE_RECOUPED_AS_HEALTH, 0.15),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHEN_NOT_ON_FULL_HEALTH, 0.2)
                ),
                List.of("23822", "327", "51040")
        ));

        list.add(TalentNodeDef.of(
                "16466", 4918.5f, -9200.21f,
                "mental_alacrity", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("15304")
        ));

        list.add(TalentNodeDef.of(
                "9226", -3746.22f, -5546.19f,
                "mental_perseverance", "passives_damage_blue",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.1),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 15.0)
                ),
                List.of("47591")
        ));

        list.add(TalentNodeDef.of(
                "24120", 7386.63f, -380.495f,
                "mental_toughness", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.18),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY_WHILE_ON_LOW_MANA, 0.25)
                ),
                List.of("34717", "52053")
        ));

        list.add(TalentNodeDef.of(
                "39857", -5647.86f, -1915.04f,
                "mentorship", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_SPEED, -0.15),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CAST_SPEED, -0.15),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, 1.0)
                ),
                List.of("22133")
        ));

        list.add(TalentNodeDef.of(
                "45400", -10450.7f, -6602.88f,
                "mighty_trunk", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.03),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_SKILLS_USED_BY_TOTEMS, 0.2)
                ),
                List.of("15672", "59908")
        ));

        list.add(TalentNodeDef.of(
                "45632", -7179.99f, -5604.12f,
                "mind_eraser", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.15)
                ),
                List.of("25503")
        ));

        list.add(TalentNodeDef.of(
                "62984", 3077.59f, -1776.84f,
                "mindful_awareness", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.24),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.24)
                ),
                List.of("15975", "21336")
        ));

        list.add(TalentNodeDef.of(
                "11392", -10916.6f, -2554.1f,
                "molten_being", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE, 0.05)
                ),
                List.of("17885", "41012")
        ));

        list.add(TalentNodeDef.of(
                "51868", -11761.2f, -2554.1f,
                "molten_carapace", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_IGNITED, 0.5),
                        TalentNodeGen.stat(StatIds.TO_MAX_FIRE_RESISTANCE_WHILE_IGNITED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE_WHILE_IGNITED, 0.5)
                ),
                List.of("19794", "47753")
        ));

        list.add(TalentNodeDef.of(
                "36100", -9522.16f, -6739.15f,
                "molten_claw", "passives_druidshapeshiftbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("63085")
        ));

        list.add(TalentNodeDef.of(
                "17548", 3757.11f, -3197.79f,
                "moment_of_truth", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.15)
                ),
                List.of("14958", "630")
        ));

        list.add(TalentNodeDef.of(
                "63579", -2065.42f, 8874.5f,
                "momentum", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR, 1.0),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.05)
                ),
                List.of("33518")
        ));

        list.add(TalentNodeDef.of(
                "64770", -6047.11f, -6829.96f,
                "morgana_the_storm_seer", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.YOU_CANNOT_BE_ELECTROCUTED, 1.0),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.5)
                ),
                List.of("23039")
        ));

        list.add(TalentNodeDef.of(
                "49370", -9222.33f, -2717.44f,
                "morning_star", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_FLAILS, 0.3),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_FLAILS, 0.2)
                ),
                List.of("43443")
        ));

        list.add(TalentNodeDef.of(
                "47560", 6269.82f, 2075.69f,
                "multi_shot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE, 0.24)
                ),
                List.of("60323")
        ));

        list.add(TalentNodeDef.of(
                "8810", 4012.19f, 7447.81f,
                "multitasking", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.15),
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.12)
                ),
                List.of("33751")
        ));

        list.add(TalentNodeDef.of(
                "50239", 2752.54f, 8667.03f,
                "mutewind_agility", "passives_coldresist",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.3)
                ),
                List.of("20779")
        ));

        list.add(TalentNodeDef.of(
                "63064", 3198.47f, -1247.22f,
                "mystic_stance", "passives_minemanareservationnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_ON_FULL_HEALTH, 0.3)
                ),
                List.of("30634", "65437")
        ));

        list.add(TalentNodeDef.of(
                "52764", -8664.77f, -6876.46f,
                "mystical_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_SPELL_DAMAGE, 0.02)
                ),
                List.of("34871", "56219")
        ));

        list.add(TalentNodeDef.of(
                "934", -593.443f, 7627.68f,
                "natural_immunity", "passives_spellsuppresionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_AILMENT_THRESHOLD_PER_DEXTERITY, 4.0)
                ),
                List.of("12526", "9825")
        ));

        list.add(TalentNodeDef.of(
                "53265", 9754.7f, -1887.15f,
                "nature_s_bite", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS, 0.15)
                ),
                List.of("45382")
        ));

        list.add(TalentNodeDef.of(
                "35581", -3872.48f, 156.685f,
                "near_at_hand", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.16),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.1)
                ),
                List.of("37956")
        ));

        list.add(TalentNodeDef.of(
                "4709", 7474.14f, 654.439f,
                "near_sighted", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.3),
                        TalentNodeGen.stat(StatIds.INC_PENALTY_TO_ACCURACY_AT_RANGE, 0.3)
                ),
                List.of("15301")
        ));

        list.add(TalentNodeDef.of(
                "10499", -2097.34f, 4042.32f,
                "necromantic_ward", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.3),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_UNDEAD_ENEMIES, 0.3)
                ),
                List.of("42583", "51788")
        ));

        list.add(TalentNodeDef.of(
                "11376", -144.949f, -9938.76f,
                "necrotic_touch", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.4)
                ),
                List.of("50720")
        ));

        list.add(TalentNodeDef.of(
                "59541", 2401.41f, -6886.68f,
                "necrotised_flesh", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.4),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_HEALTH_RECOVERY_RATE, -0.1)
                ),
                List.of("14548", "28573")
        ));

        list.add(TalentNodeDef.of(
                "56466", -2701.66f, -4273.5f,
                "night_s_bite", "passives_poison",
                List.of(),
                List.of("17729")
        ));

        list.add(TalentNodeDef.of(
                "40292", -6558.65f, 5262.33f,
                "nimble_strength", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_ACCURACY_EQUAL_TO_YOUR_STRENGTH, 1.0)
                ),
                List.of("43149", "43507")
        ));

        list.add(TalentNodeDef.of(
                "37266", 3395.61f, 6863.03f,
                "nourishing_ally", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.2),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_RATE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.2)
                ),
                List.of("29930")
        ));

        list.add(TalentNodeDef.of(
                "60992", 8114.32f, 5746.46f,
                "nurturing_guardian", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS, 1.0)
                ),
                List.of("16568")
        ));

        list.add(TalentNodeDef.of(
                "52115", -4109.59f, -3719.51f,
                "nurturing_nature", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_ON_FULL_HEALTH, 0.4)
                ),
                List.of("34769")
        ));

        list.add(TalentNodeDef.of(
                "42036", 10566.6f, 5373.91f,
                "off_balancing_retort", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_DURATION, 0.3)
                ),
                List.of("12116", "55329")
        ));

        list.add(TalentNodeDef.of(
                "35918", -3796.84f, -3979.32f,
                "one_for_all", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.4),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, -0.08)
                ),
                List.of("9018")
        ));

        list.add(TalentNodeDef.of(
                "44753", -10615.2f, 3051.94f,
                "one_with_flame", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.5)
                ),
                List.of("11433")
        ));

        list.add(TalentNodeDef.of(
                "34316", 10905.1f, -4326.07f,
                "one_with_the_river", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_WIELDING_A_QUARTERSTAFF, 0.3),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP_WITH_QUARTERSTAVES, 0.3),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_QUARTERSTAVES, 0.3)
                ),
                List.of("2361", "61632")
        ));

        list.add(TalentNodeDef.of(
                "9444", 10470.8f, -2705.24f,
                "one_with_the_storm", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE)
                ),
                List.of("31449", "52399")
        ));

        list.add(TalentNodeDef.of(
                "59636", 0.005f, -2781.45f,
                "open_mind", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.25)
                ),
                List.of("13769", "3242")
        ));

        list.add(TalentNodeDef.of(
                "52199", -612.805f, -8637.76f,
                "overexposure", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EXPOSURE_EFFECT, 0.3)
                ),
                List.of("25101", "44498")
        ));

        list.add(TalentNodeDef.of(
                "65204", 9488.37f, -4973.05f,
                "overflowing_power", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_POWER_CHARGES, 2.0)
                ),
                List.of("30615")
        ));

        list.add(TalentNodeDef.of(
                "42390", -10448.3f, 2593.37f,
                "overheating_blow", "passives_firedamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, 0.25)
                ),
                List.of("11433", "13893")
        ));

        list.add(TalentNodeDef.of(
                "47635", 5578.23f, -7243.44f,
                "overload", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA, 0.1),
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.15)
                ),
                List.of("53207")
        ));

        list.add(TalentNodeDef.of(
                "25513", -1013.39f, 5435.6f,
                "overwhelm", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, -0.05),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.4)
                ),
                List.of("9164")
        ));

        list.add(TalentNodeDef.of(
                "57388", -8133.51f, 3841.59f,
                "overwhelming_strike", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.MORE_STUN_BUILDUP_WITH_CRITICAL_HITS, 0.2)
                ),
                List.of("21070", "53386")
        ));

        list.add(TalentNodeDef.of(
                "10295", -6811.98f, -9868.42f,
                "overzealous", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.16),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_OF_SKILLS, 0.15)
                ),
                List.of("28774", "52106")
        ));

        list.add(TalentNodeDef.of(
                "21784", -8188.97f, -5725.95f,
                "pack_encouragement", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_FOR_EACH_MINION_IN_YOUR_PRESENCE_UP_TO_A_MAX_OF, 0.05)
                ),
                List.of("50118", "53893")
        ));

        list.add(TalentNodeDef.of(
                "24766", -466.625f, 8827.24f,
                "paranoia", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SURROUNDED_AREA_OF_EFFECT, 0.5)
                ),
                List.of("11257", "31566")
        ));

        list.add(TalentNodeDef.of(
                "56016", 2877.71f, 10298.3f,
                "passthrough_rounds", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_PIERCE_ENEMIES_WITH_FULLY_BROKEN_ARMOR)
                ),
                List.of("30040", "35594", "65149")
        ));

        list.add(TalentNodeDef.of(
                "51850", 2678.05f, 9020.99f,
                "path_of_the_renegade", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_CHAOS_DAMAGE, 0.2)
                ),
                List.of("9535")
        ));

        list.add(TalentNodeDef.of(
                "62230", 2922.78f, -10953.2f,
                "patient_barrier", "passives_energyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.5),
                        TalentNodeGen.stat(StatIds.SLOWER_START_OF_ENERGY_SHIELD_RECHARGE, -0.2)
                ),
                List.of("19355", "37974")
        ));

        list.add(TalentNodeDef.of(
                "60404", -3671.42f, 6752.24f,
                "perfect_opportunity", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_HEAVY_STUNNED_ENEMIES, 1.0)
                ),
                List.of("20691")
        ));

        list.add(TalentNodeDef.of(
                "49661", 4758.1f, 7289.29f,
                "perfectly_placed_knife", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLEEDING_ENEMIES, 0.25),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_CRITICALLY_HIT_WITH_ATTACKS, 0.2)
                ),
                List.of("33391")
        ));

        list.add(TalentNodeDef.of(
                "17330", -6945.34f, 6496.97f,
                "perforation", "icongroundslam",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND, 0.2),
                        TalentNodeGen.stat(StatIds.INC_JAGGED_GROUND_DURATION, 0.4)
                ),
                List.of("33137", "61927")
        ));

        list.add(TalentNodeDef.of(
                "2863", -3556.93f, -337.575f,
                "perpetual_freeze", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CHILL_AND_FREEZE_DURATION_ON_ENEMIES, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.15)
                ),
                List.of("33601", "35708")
        ));

        list.add(TalentNodeDef.of(
                "34308", -4755.57f, -2258.53f,
                "personal_touch", "passives_increasedattackdamagenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.12)
                ),
                List.of("37414", "43014")
        ));

        list.add(TalentNodeDef.of(
                "7651", 11492.7f, 4765.67f,
                "pierce_the_heart", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ARROWS_PIERCE_AN_ADDITIONAL_TARGET, 1.0)
                ),
                List.of("21788")
        ));

        list.add(TalentNodeDef.of(
                "17260", -5884.67f, -935.438f,
                "piercing_claw", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED, 0.15)
                ),
                List.of("65189", "9037")
        ));

        list.add(TalentNodeDef.of(
                "4534", 7259.38f, 2398.31f,
                "piercing_shot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.5)
                ),
                List.of("31918")
        ));

        list.add(TalentNodeDef.of(
                "51129", -8915.98f, 5629.76f,
                "pile_on", "passives_armorbreak2bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.3)
                ),
                List.of("15892", "26437")
        ));

        list.add(TalentNodeDef.of(
                "60083", 4332.46f, -271.815f,
                "pin_and_run", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_PINNED_AN_ENEMY_RECENTLY, 0.05)
                ),
                List.of("44239", "55270")
        ));

        list.add(TalentNodeDef.of(
                "4447", 2756.47f, 6162.5f,
                "pin_their_motivation", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_DURATION, 0.2),
                        TalentNodeGen.stat(StatIds.PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS, 1.0)
                ),
                List.of("18470", "3234")
        ));

        list.add(TalentNodeDef.of(
                "16816", 4278.42f, 10564.5f,
                "pinpoint_shot", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_GAIN_INC_ACCURACY_EQUAL_TO_THEIR_CRITICAL_HIT_CHANCE, 1.0)
                ),
                List.of("8249")
        ));

        list.add(TalentNodeDef.of(
                "38111", 5891.44f, -7002.35f,
                "pliable_flesh", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.25)
                ),
                List.of("10242")
        ));

        list.add(TalentNodeDef.of(
                "58426", 8855.48f, 358.259f,
                "pocket_sand", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.5)
                ),
                List.of("60")
        ));

        list.add(TalentNodeDef.of(
                "27950", -4335.51f, 3123.25f,
                "polished_iron", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.25),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS, 0.3)
                ),
                List.of("26324")
        ));

        list.add(TalentNodeDef.of(
                "57047", -561.974f, 12271.5f,
                "polymathy", "passives_ascendants_skillpoint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTRIBUTES, 0.07)
                ),
                List.of("44176", "45278")
        ));

        list.add(TalentNodeDef.of(
                "19125", -367.219f, -5734.53f,
                "potent_incantation", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.3),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, -0.05)
                ),
                List.of("55104")
        ));

        list.add(TalentNodeDef.of(
                "15083", 905.735f, -11253.8f,
                "power_conduction", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.25)
                ),
                List.of("22271")
        ));

        list.add(TalentNodeDef.of(
                "12683", -2833.44f, -6016.57f,
                "power_of_the_storm", "passives_auraeffect",
                List.of(),
                List.of("4873")
        ));

        list.add(TalentNodeDef.of(
                "6178", 1900.19f, 11910.0f,
                "power_shots", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_CROSSBOWS, -0.15),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_CROSSBOWS, 0.8)
                ),
                List.of("33415")
        ));

        list.add(TalentNodeDef.of(
                "3281", -1513.28f, -2332.55f,
                "powerful_casting", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_PER_STRENGTH, 0.02)
                ),
                List.of("35720")
        ));

        list.add(TalentNodeDef.of(
                "36302", 0.005f, -3281.45f,
                "practiced_signs", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.06)
                ),
                List.of("22290", "29502", "47307")
        ));

        list.add(TalentNodeDef.of(
                "49150", 5573.07f, -5913.25f,
                "precise_invocations", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.3)
                ),
                List.of("18167")
        ));

        list.add(TalentNodeDef.of(
                "13895", 9350.44f, 6649.83f,
                "precise_point", "passives_spearsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SPEARS, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_SPEARS, 0.25)
                ),
                List.of("19767", "20105")
        ));

        list.add(TalentNodeDef.of(
                "34425", 9817.3f, -2539.79f,
                "precise_volatility", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.VOLATILE_POWER_ALSO_GRANTS_INC_CRITICAL_HIT_CHANCE_PER_VOLATILITY_EXPLODED, 0.01)
                ),
                List.of("47021", "51774")
        ));

        list.add(TalentNodeDef.of(
                "19337", 5320.34f, 2650.99f,
                "precision_salvo", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.12)
                ),
                List.of("62166")
        ));

        list.add(TalentNodeDef.of(
                "21380", 6124.9f, 363.562f,
                "preemptive_strike", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 1.0)
                ),
                List.of("41529")
        ));

        list.add(TalentNodeDef.of(
                "37872", 946.211f, 6888.94f,
                "presence_present", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_TO_ACCURACY, 100.0),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.35)
                ),
                List.of("21871", "26194")
        ));

        list.add(TalentNodeDef.of(
                "32951", 3459.97f, -10758.8f,
                "preservation", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.25)
                ),
                List.of("39280")
        ));

        list.add(TalentNodeDef.of(
                "28329", 6795.36f, 6961.59f,
                "pressure_points", "passives_channellingattacksnotable2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.35),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.35)
                ),
                List.of("36723", "46533")
        ));

        list.add(TalentNodeDef.of(
                "9908", -5393.92f, 893.335f,
                "price_of_freedom", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COST_EFFICIENCY_OF_ATTACKS, 0.15),
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 0.18)
                ),
                List.of("20303", "63470")
        ));

        list.add(TalentNodeDef.of(
                "32071", -9859.06f, -1406.34f,
                "primal_growth", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_IF_YOU_VE_KILLED_RECENTLY, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.08)
                ),
                List.of("15427", "9352")
        ));

        list.add(TalentNodeDef.of(
                "31364", 8927.04f, 4861.35f,
                "primal_protection", "passives_charmnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_EFFECT_DURATION, 0.4),
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.4)
                ),
                List.of("50420")
        ));

        list.add(TalentNodeDef.of(
                "28892", -8609.24f, -835.177f,
                "primal_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE_WHILE_SHAPESHIFTED, 12.0)
                ),
                List.of("13845", "37290")
        ));

        list.add(TalentNodeDef.of(
                "50884", 4355.65f, 3366.23f,
                "primal_sundering", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_ELEMENTAL_DAMAGE_RESISTANCES, 0.12),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.08)
                ),
                List.of("35878")
        ));

        list.add(TalentNodeDef.of(
                "26356", -1653.23f, 9760.91f,
                "primed_to_explode", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.4),
                        TalentNodeGen.stat(StatIds.DETONATOR_SKILLS_HAVE_INC_DAMAGE, -0.8)
                ),
                List.of("15590", "57863")
        ));

        list.add(TalentNodeDef.of(
                "64046", -301.468f, -3928.97f,
                "principal_infusion", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.3),
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.3)
                ),
                List.of("45522", "55909")
        ));

        list.add(TalentNodeDef.of(
                "62034", -9765.33f, 710.665f,
                "prism_guard", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.3)
                ),
                List.of("33939", "4442", "55931")
        ));

        list.add(TalentNodeDef.of(
                "26228", -4208.73f, -7738.4f,
                "prize_of_the_hunt", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES, 0.02),
                        TalentNodeGen.stat(StatIds.TO_MAX_FRENZY_CHARGES, 1.0)
                ),
                List.of("4681")
        ));

        list.add(TalentNodeDef.of(
                "54814", -4776.67f, -6340.34f,
                "profane_commander", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.3),
                        TalentNodeGen.stat(StatIds.INC_SPIRIT, 0.04)
                ),
                List.of("59498")
        ));

        list.add(TalentNodeDef.of(
                "58397", 8692.14f, 1153.21f,
                "proficiency", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 25.0)
                ),
                List.of("19338", "2334", "24070", "31647")
        ));

        list.add(TalentNodeDef.of(
                "19236", -9598.42f, -542.535f,
                "projectile_bulwark", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.3),
                        TalentNodeGen.stat(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS, 1.2)
                ),
                List.of("60274")
        ));

        list.add(TalentNodeDef.of(
                "45874", -6917.28f, -6155.67f,
                "proliferating_weeds", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.FISSURE_SKILLS_HAVE_TO_LIMIT, 1.0)
                ),
                List.of("18270")
        ));

        list.add(TalentNodeDef.of(
                "19442", 3454.56f, 4085.79f,
                "prolonged_assault", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.16),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.16),
                        TalentNodeGen.stat(StatIds.BUFFS_ON_YOU_EXPIRE_SLOWER, -0.1)
                ),
                List.of("25170")
        ));

        list.add(TalentNodeDef.of(
                "49550", -3484.73f, 4375.9f,
                "prolonged_fury", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_LOSS_OF_RAGE_IS_SLOWER, -0.25)
                ),
                List.of("4624")
        ));

        list.add(TalentNodeDef.of(
                "54998", -6749.92f, 2778.67f,
                "protraction", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES, 0.15)
                ),
                List.of("44461")
        ));

        list.add(TalentNodeDef.of(
                "38614", 2760.07f, -6585.35f,
                "psychic_fragmentation", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_SPELL_SKILLS_TO_FIRE_ADDITIONAL_PROJECTILES, 0.12)
                ),
                List.of("27662", "44201", "55241")
        ));

        list.add(TalentNodeDef.of(
                "13482", -8394.02f, 5599.53f,
                "punctured_lung", "passives_armorbreak2bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_FULLY_ARMOR_BREAK_CANNOT_REGENERATE_HEALTH),
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_FULLY_ARMOR_BREAK_ARE_MAIMED)
                ),
                List.of("15892", "30136")
        ));

        list.add(TalentNodeDef.of(
                "62210", -4205.88f, -8832.94f,
                "puppet_master_chance", "passives_puppeteernoteble",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY_OF_COMMAND_SKILLS, 0.15),
                        TalentNodeGen.stat(StatIds.MAX_STACKS_OF_PUPPET_MASTER, 1.0)
                ),
                List.of("53795")
        ));

        list.add(TalentNodeDef.of(
                "14258", 941.901f, -8923.15f,
                "puppet_master_chance", "passives_puppeteernoteble",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.35)
                ),
                List.of("13123", "7104")
        ));

        list.add(TalentNodeDef.of(
                "55149", 7119.01f, -8248.39f,
                "pure_chaos", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.11)
                ),
                List.of("32438")
        ));

        list.add(TalentNodeDef.of(
                "2254", 486.985f, -3030.38f,
                "pure_energy", "passives_deepwisdom",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.3),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("13769", "14666", "15408", "43736", "47307", "4748", "60685")
        ));

        list.add(TalentNodeDef.of(
                "28975", 1249.73f, -12353.7f,
                "pure_power", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_MAX_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("26905")
        ));

        list.add(TalentNodeDef.of(
                "6229", -3825.32f, 8460.21f,
                "push_the_advantage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_ONE_HANDED_MELEE_WEAPONS, 0.4)
                ),
                List.of("13356", "26490")
        ));

        list.add(TalentNodeDef.of(
                "33542", 12068.2f, 1163.81f,
                "quick_fingers", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, 0.24)
                ),
                List.of("32319", "48836")
        ));

        list.add(TalentNodeDef.of(
                "48240", -4658.35f, -1235.46f,
                "quick_recovery", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.4),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED, 0.05)
                ),
                List.of("10571", "59213")
        ));

        list.add(TalentNodeDef.of(
                "13233", -2880.6f, -1668.56f,
                "radial_force", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.12)
                ),
                List.of("17517", "19873")
        ));

        list.add(TalentNodeDef.of(
                "55450", -8012.19f, -5902.73f,
                "rallying_form", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED, 1.0)
                ),
                List.of("50118", "57089")
        ));

        list.add(TalentNodeDef.of(
                "43791", -1945.48f, 5524.53f,
                "rallying_icon", "passives_bannerareanotable",
                List.of(
                        TalentNodeGen.stat(StatIds.WHEN_A_BANNER_EXPIRES_RECOVER_OF_THE_GLORY_REQUIRED_FOR_THAT_BANNER, 0.15)
                ),
                List.of("41651", "53320")
        ));

        list.add(TalentNodeDef.of(
                "17973", 657.072f, -3928.97f,
                "rapid_recharge", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE, 0.12),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.12)
                ),
                List.of("22691", "4748")
        ));

        list.add(TalentNodeDef.of(
                "64119", 1234.25f, 7652.62f,
                "rapid_reload", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.4)
                ),
                List.of("12311")
        ));

        list.add(TalentNodeDef.of(
                "7604", 9091.22f, 2242.59f,
                "rapid_strike", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ACCURACY, 30.0),
                        TalentNodeGen.stat(StatIds.INC_MELEE_ATTACK_SPEED, 0.08)
                ),
                List.of("13987", "35985")
        ));

        list.add(TalentNodeDef.of(
                "62185", 7236.46f, 5087.69f,
                "rattled", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_MANA, 20.0),
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.5)
                ),
                List.of("14724")
        ));

        list.add(TalentNodeDef.of(
                "32148", -8976.33f, -2859.47f,
                "rattling_ball", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_FLAILS, 0.25)
                ),
                List.of("43443")
        ));

        list.add(TalentNodeDef.of(
                "3567", 2633.53f, -9806.75f,
                "raw_mana", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_MANA, 0.08),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_OF_SKILLS, 0.1)
                ),
                List.of("10159", "25026")
        ));

        list.add(TalentNodeDef.of(
                "51184", -478.295f, -3030.38f,
                "raw_power", "passives_increasedmanacostnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("22314", "29502", "3242", "41965", "47555", "6686")
        ));

        list.add(TalentNodeDef.of(
                "17372", 9091.22f, 1908.59f,
                "reaching_strike", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.METRES_TO_MELEE_STRIKE_RANGE, 0.2)
                ),
                List.of("31273", "35985")
        ));

        list.add(TalentNodeDef.of(
                "10602", -4969.07f, 2577.77f,
                "reaving", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_WEAPONS, 0.08),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 15.0)
                ),
                List.of("24871")
        ));

        list.add(TalentNodeDef.of(
                "61309", 2334.28f, 8908.51f,
                "redblade_discipline", "passives_fireresist",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_FIRE_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.2),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.3)
                ),
                List.of("42169")
        ));

        list.add(TalentNodeDef.of(
                "45244", 4732.5f, 10537.4f,
                "refills", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND, 0.15)
                ),
                List.of("23343", "3775")
        ));

        list.add(TalentNodeDef.of(
                "26447", 418.515f, -9399.2f,
                "refocus", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, 0.2)
                ),
                List.of("12918")
        ));

        list.add(TalentNodeDef.of(
                "20388", -2906.48f, -8891.3f,
                "regenerative_flesh", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH, 0.15)
                ),
                List.of("2606")
        ));

        list.add(TalentNodeDef.of(
                "56388", -1626.68f, 7194.58f,
                "reinforced_rallying", "passives_bannerareanotable",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_NUMBER_OF_PLACED_BANNERS, 1.0)
                ),
                List.of("21096", "57616")
        ));

        list.add(TalentNodeDef.of(
                "35809", 5658.94f, 4371.75f,
                "reinvigoration", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_SECONDS, 0.01)
                ),
                List.of("32813")
        ));

        list.add(TalentNodeDef.of(
                "7721", -2437.85f, 1899.76f,
                "relentless", "passives_warrior",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.005),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("18629", "18742", "27726", "54232", "61534", "63393")
        ));

        list.add(TalentNodeDef.of(
                "55180", 1562.27f, -6930.54f,
                "relentless_fallen", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MOVEMENT_SPEED, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.08)
                ),
                List.of("3443", "45530")
        ));

        list.add(TalentNodeDef.of(
                "6898", -2774.38f, -1070.72f,
                "relentless_vindicator", "passives_pressurepoints",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH_AND_INTELLIGENCE, 5.0)
                ),
                List.of("17517", "17584", "30704", "38103", "48305", "61067")
        ));

        list.add(TalentNodeDef.of(
                "11160", -7988.12f, -10523.7f,
                "relinquish_your_life", "passives_life1",
                List.of(),
                List.of("16332", "43721")
        ));

        list.add(TalentNodeDef.of(
                "1506", 4272.59f, -8369.6f,
                "remnant_attraction", "passives_remnantnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT, 0.1),
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.5)
                ),
                List.of("60324", "60974")
        ));
        return list;
    }
}
