package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NotableNodes06 {

    private NotableNodes06() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "44605", 431.178f, 2762.72f,
                "remorseless", "passives_newnewattackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_AGAINST_ENEMIES_WITHIN_METRES, 0.3),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH_AND_DEXTERITY, 5.0)
                ),
                List.of("13081", "38779", "57039", "59881", "8092")
        ));

        list.add(TalentNodeDef.of(
                "65468", 1366.53f, 11380.9f,
                "repeating_explosives", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME, 0.15)
                ),
                List.of("25807", "35653")
        ));

        list.add(TalentNodeDef.of(
                "20414", 10286.7f, 5858.72f,
                "reprisal", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.25)
                ),
                List.of("35043", "52410")
        ));

        list.add(TalentNodeDef.of(
                "10029", -5216.83f, -5574.92f,
                "repulsion", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AREA_SKILLS_HAVE_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, 0.2)
                ),
                List.of("19277")
        ));

        list.add(TalentNodeDef.of(
                "13505", -3115.35f, -1267.96f,
                "resilient_soul", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.05)
                ),
                List.of("22045")
        ));

        list.add(TalentNodeDef.of(
                "25361", 8826.6f, -4430.6f,
                "resolute_reach", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.18),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, -0.2)
                ),
                List.of("32903")
        ));

        list.add(TalentNodeDef.of(
                "56860", 5833.86f, 5463.88f,
                "resolute_reprisal", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_RANGE, 0.3),
                        TalentNodeGen.stat(StatIds.YOUR_HEAVY_STUN_BUILDUP_EMPTIES_FASTER_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY, 0.5)
                ),
                List.of("24843", "29320")
        ));

        list.add(TalentNodeDef.of(
                "40325", -4658.35f, -1707.81f,
                "resolution", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD, 0.1)
                ),
                List.of("36191", "7392")
        ));

        list.add(TalentNodeDef.of(
                "38972", -3972.07f, -5724.9f,
                "restless_dead", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER, 0.25)
                ),
                List.of("28458", "8357")
        ));

        list.add(TalentNodeDef.of(
                "31773", -3415.51f, -10086.3f,
                "resurging_archon", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.ARCHON_RECOVERY_PERIOD_EXPIRES_FASTER, 0.25)
                ),
                List.of("17061")
        ));

        list.add(TalentNodeDef.of(
                "7395", -10905.2f, 1720.04f,
                "retaliation", "passives_thornsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE_IF_YOU_VE_BLOCKED_RECENTLY, 0.75)
                ),
                List.of("30007", "3245")
        ));

        list.add(TalentNodeDef.of(
                "9009", -6291.04f, -7138.67f,
                "return_to_nature", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.OVERGROWN_PLANT_SKILLS_ARMOR_BREAK_INC_ARMOR, 0.5)
                ),
                List.of("32239")
        ));

        list.add(TalentNodeDef.of(
                "7062", 2343.8f, 11466.4f,
                "reusable_ammunition", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY, 0.3)
                ),
                List.of("16680", "43155", "44430")
        ));

        list.add(TalentNodeDef.of(
                "3188", -11406.2f, 1430.79f,
                "revenge", "passives_thornsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_BODY_ARMOR, 0.1)
                ),
                List.of("30007", "38670")
        ));

        list.add(TalentNodeDef.of(
                "59767", -3637.78f, 1713.0f,
                "reverberating_impact", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.25),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.12)
                ),
                List.of("20645", "53443")
        ));

        list.add(TalentNodeDef.of(
                "33400", 3507.9f, 5460.57f,
                "reverberating_parry", "passives_bucklersnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("43254", "5077")
        ));

        list.add(TalentNodeDef.of(
                "8660", -5525.85f, -5203.66f,
                "reverberation", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.15)
                ),
                List.of("18846")
        ));

        list.add(TalentNodeDef.of(
                "37619", -6598.9f, -4269.04f,
                "rhythm_of_fire", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_SLAM_SKILLS_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.05)
                ),
                List.of("290", "34331")
        ));

        list.add(TalentNodeDef.of(
                "13693", -6958.05f, -4267.12f,
                "rhythm_of_ice", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_PER_SPELL_ECHOED_RECENTLY_UP_TO, 0.06)
                ),
                List.of("34331", "8553")
        ));

        list.add(TalentNodeDef.of(
                "41210", 621.387f, 3698.94f,
                "ricochet", "passives_chainingprojectiles",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.1)
                ),
                List.of("43578", "59028")
        ));

        list.add(TalentNodeDef.of(
                "8957", -4597.5f, -10643.1f,
                "right_hand_of_darkness", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.SPELLS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.05)
                ),
                List.of("57021")
        ));

        list.add(TalentNodeDef.of(
                "59263", -3188.37f, 12405.9f,
                "ripping_blade", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.25)
                ),
                List.of("37963")
        ));

        list.add(TalentNodeDef.of(
                "28613", -9769.18f, 2853.9f,
                "roaring_cries", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.WARCRIES_HAVE_A_MINIMUM_OF_POWER, 10.0)
                ),
                List.of("39598")
        ));

        list.add(TalentNodeDef.of(
                "60269", 907.824f, -6708.37f,
                "roil", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, -0.1),
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.25)
                ),
                List.of("33180")
        ));

        list.add(TalentNodeDef.of(
                "61112", 9464.52f, 6847.44f,
                "roll_and_strike", "passives_spearsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SPEARS, 0.25),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SPEARS, 0.1)
                ),
                List.of("20105", "55680")
        ));

        list.add(TalentNodeDef.of(
                "57002", -4148.17f, -647.707f,
                "rough_carapace", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("47633")
        ));

        list.add(TalentNodeDef.of(
                "8483", 1390.79f, -6578.96f,
                "ruin", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, 0.35),
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, -0.1)
                ),
                List.of("14355")
        ));

        list.add(TalentNodeDef.of(
                "18959", -4958.25f, -7510.69f,
                "ruinic_helm", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_ENERGY_SHIELD_PER_ITEM_ARMOR_ON_EQUIPPED_HELMET, 1.0)
                ),
                List.of("14328", "869")
        ));

        list.add(TalentNodeDef.of(
                "53566", 4630.62f, 8326.24f,
                "run_and_gun", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05),
                        TalentNodeGen.stat(StatIds.PROJECTILE_ATTACKS_HAVE_A_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING, 0.12)
                ),
                List.of("23360")
        ));

        list.add(TalentNodeDef.of(
                "7782", 4748.75f, -11059.2f,
                "rupturing_pins", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_PINNED_ENEMIES, 0.4)
                ),
                List.of("48805", "64996")
        ));

        list.add(TalentNodeDef.of(
                "9290", -948.907f, 10315.6f,
                "rusted_pins", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED, 1.0)
                ),
                List.of("19129")
        ));

        list.add(TalentNodeDef.of(
                "27096", -10792.2f, -6261.38f,
                "rustle_of_the_leaves", "passives_totemandbrandlife",
                List.of(),
                List.of("33423", "36197")
        ));

        list.add(TalentNodeDef.of(
                "14294", -4370.8f, -6656.62f,
                "sacrificial_blood", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_COST_OF_SKILLS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH, 0.4)
                ),
                List.of("39130")
        ));

        list.add(TalentNodeDef.of(
                "25619", 924.865f, 10399.5f,
                "sand_in_the_eyes", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.1),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS, 0.15)
                ),
                List.of("3660", "43562")
        ));

        list.add(TalentNodeDef.of(
                "58215", -4337.43f, -9564.92f,
                "sanguimantic_rituals", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.01),
                        TalentNodeGen.stat(StatIds.ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE)
                ),
                List.of("32923")
        ));

        list.add(TalentNodeDef.of(
                "4810", 4748.75f, -10643.2f,
                "sanguine_tolerance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_CORRUPTED_BLOOD, 1.0),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_BLEEDING_ON_YOU, -0.4)
                ),
                List.of("48805", "64996")
        ));

        list.add(TalentNodeDef.of(
                "42070", -6401.88f, -5623.51f,
                "saqawal_s_guidance", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.15),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("1144")
        ));

        list.add(TalentNodeDef.of(
                "35743", 1234.8f, 5515.55f,
                "saqawal_s_hide", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.25)
                ),
                List.of("1221", "14394")
        ));

        list.add(TalentNodeDef.of(
                "62237", 1657.42f, 5271.55f,
                "saqawal_s_talon", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.25)
                ),
                List.of("1221", "50124")
        ));

        list.add(TalentNodeDef.of(
                "63255", 2899.54f, 2850.56f,
                "savagery", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_VE_CONSUMED_A_FRENZY_CHARGE_RECENTLY, 0.5),
                        TalentNodeGen.stat(StatIds.TO_MAX_FRENZY_CHARGES, 1.0)
                ),
                List.of("13341")
        ));

        list.add(TalentNodeDef.of(
                "18397", -9605.19f, 2275.67f,
                "savoured_blood", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.35),
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_SLOWER, -0.2)
                ),
                List.of("55063")
        ));

        list.add(TalentNodeDef.of(
                "45713", 8175.23f, -4097.21f,
                "savouring", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_FLASKS_YOU_USE_TO_NOT_CONSUME_CHARGES, 0.2)
                ),
                List.of("39607")
        ));

        list.add(TalentNodeDef.of(
                "60619", -7749.82f, -7956.47f,
                "scales_of_the_wyvern", "passives_druidshapeshiftwyvernnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED, 0.2),
                        TalentNodeGen.stat(StatIds.TO_MAX_LIGHTNING_RESISTANCE_WHILE_SHAPESHIFTED, 0.01)
                ),
                List.of("27216", "31010", "41609", "541")
        ));

        list.add(TalentNodeDef.of(
                "60014", -3919.17f, -698.685f,
                "scent_of_blood", "passives_blood2",
                List.of(),
                List.of("48079")
        ));

        list.add(TalentNodeDef.of(
                "39884", 3011.23f, 4728.08f,
                "searing_heat", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 1.0),
                        TalentNodeGen.stat(StatIds.IGNITES_YOU_INFLICT_DEAL_DAMAGE_FASTER, 0.1)
                ),
                List.of("44487")
        ));

        list.add(TalentNodeDef.of(
                "52229", -2012.26f, -7643.07f,
                "secrets_of_the_orb", "passives_auranotable",
                List.of(
                        TalentNodeGen.stat(StatIds.ORB_SKILLS_HAVE_TO_LIMIT, 1.0)
                ),
                List.of("57791")
        ));

        list.add(TalentNodeDef.of(
                "5009", 6411.76f, -4456.86f,
                "seeing_stars", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DAZE_DURATION, 0.25)
                ),
                List.of("36270")
        ));

        list.add(TalentNodeDef.of(
                "23630", -11338.9f, -2538.8f,
                "self_immolation", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU, 1.0),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.4)
                ),
                List.of("17885", "19794")
        ));

        list.add(TalentNodeDef.of(
                "44917", 2787.39f, -6150.44f,
                "self_mortification", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.2),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_ON_FULL_HEALTH, 0.2)
                ),
                List.of("703")
        ));

        list.add(TalentNodeDef.of(
                "37778", -8895.73f, -3586.54f,
                "self_sacrificing", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPIRIT_RESERVATION_EFFICIENCY_OF_SKILLS, -0.2),
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_MINION_SKILLS, 0.4)
                ),
                List.of("13108", "48160")
        ));

        list.add(TalentNodeDef.of(
                "36085", 9207.96f, -1092.57f,
                "serrated_edges", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.3)
                ),
                List.of("17589", "37548")
        ));

        list.add(TalentNodeDef.of(
                "13457", 7327.1f, -5706.17f,
                "shadow_dancing", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 0.4),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_IF_YOU_VE_BEEN_STUNNED_RECENTLY, 0.4)
                ),
                List.of("3630")
        ));

        list.add(TalentNodeDef.of(
                "53150", 8873.96f, -758.575f,
                "sharp_sight", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.3)
                ),
                List.of("17589", "64462")
        ));

        list.add(TalentNodeDef.of(
                "61703", -6340.93f, -813.185f,
                "sharpened_claw", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED, 0.3)
                ),
                List.of("57608", "9037")
        ));

        list.add(TalentNodeDef.of(
                "41811", 8448.88f, -2798.97f,
                "shatter_palm", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.2),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3)
                ),
                List.of("16013", "35173")
        ));

        list.add(TalentNodeDef.of(
                "49740", 3668.09f, -8466.37f,
                "shattered_crystal", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ICE_CRYSTAL_HEALTH, -0.6)
                ),
                List.of("23455")
        ));

        list.add(TalentNodeDef.of(
                "48658", 3560.12f, -4198.58f,
                "shattering", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.INC_CHILL_DURATION_ON_ENEMIES, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.2)
                ),
                List.of("27095")
        ));

        list.add(TalentNodeDef.of(
                "53527", -7711.19f, 1185.92f,
                "shattering_blow", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_OF_ARMOR_ON_HEAVY_STUNNING_AN_ENEMY, 0.5)
                ),
                List.of("7204")
        ));

        list.add(TalentNodeDef.of(
                "64415", 8116.02f, -6225.35f,
                "shattering_daze", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_DAZED_ENEMIES, 0.12)
                ),
                List.of("19461", "22063")
        ));

        list.add(TalentNodeDef.of(
                "15644", 4084.46f, 3485.05f,
                "shedding_skin", "passives_spellsuppresionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.4),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_ON_YOU, -0.1)
                ),
                List.of("41538", "55598")
        ));

        list.add(TalentNodeDef.of(
                "16947", -11868.7f, -3346.0f,
                "shelter_from_the_rain", "passives_blockstr",
                List.of(),
                List.of("20391")
        ));

        list.add(TalentNodeDef.of(
                "37244", 5686.94f, 9003.21f,
                "shield_expertise", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_BLOCK_RECOVERY, 0.4)
                ),
                List.of("33445", "34076")
        ));

        list.add(TalentNodeDef.of(
                "57617", -5885.4f, -2365.38f,
                "shifted_strikes", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.3)
                ),
                List.of("14996", "16506", "37509", "51743")
        ));

        list.add(TalentNodeDef.of(
                "53941", 4435.38f, -1558.44f,
                "shimmering", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_ENERGY_SHIELD, 0.03)
                ),
                List.of("17367", "62841")
        ));

        list.add(TalentNodeDef.of(
                "5335", 7086.36f, -9093.11f,
                "shimmering_mirage", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.3),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_AILMENTS_ON_YOU, -0.1)
                ),
                List.of("59798")
        ));

        list.add(TalentNodeDef.of(
                "29800", -606.772f, -9312.53f,
                "shocking_limit", "passives_instillationsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_LIGHTNING_INFUSIONS, 1.0)
                ),
                List.of("29391")
        ));

        list.add(TalentNodeDef.of(
                "32448", -7776.6f, 6506.71f,
                "shockproof", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.3),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.3)
                ),
                List.of("31370")
        ));

        list.add(TalentNodeDef.of(
                "1087", -4991.52f, 3607.2f,
                "shockwaves", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_IF_YOU_VE_STUNNED_AN_ENEMY_WITH_A_TWO_HANDED_MELEE_WEAPON_RECENTLY, 0.25)
                ),
                List.of("6923")
        ));

        list.add(TalentNodeDef.of(
                "46296", 1970.94f, 6785.84f,
                "short_shot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, -0.1),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.2)
                ),
                List.of("19342")
        ));

        list.add(TalentNodeDef.of(
                "55060", 1005.94f, 9100.13f,
                "shrapnel", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.3),
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.1)
                ),
                List.of("33037", "6505")
        ));

        list.add(TalentNodeDef.of(
                "14211", 4919.12f, 5747.96f,
                "shredding_contraptions", "passives_trap",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_ARMOR, -0.25),
                        TalentNodeGen.stat(StatIds.ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_EVASION, -0.25)
                ),
                List.of("43238", "50879")
        ));

        list.add(TalentNodeDef.of(
                "5284", -3905.88f, -9191.64f,
                "shredding_force", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS, 0.15)
                ),
                List.of("57178")
        ));

        list.add(TalentNodeDef.of(
                "47088", 3395.61f, 6375.03f,
                "sic_em", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.6)
                ),
                List.of("29930")
        ));

        list.add(TalentNodeDef.of(
                "63037", -7335.89f, -851.691f,
                "sigil_of_fire", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_IGNITED_ENEMIES, 0.3)
                ),
                List.of("24430")
        ));

        list.add(TalentNodeDef.of(
                "40803", -7007.94f, -628.821f,
                "sigil_of_ice", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_CHILLED_ENEMIES, 0.3)
                ),
                List.of("3218")
        ));

        list.add(TalentNodeDef.of(
                "46024", -7387.03f, -448.065f,
                "sigil_of_lightning", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HITS_AGAINST_SHOCKED_ENEMIES, 0.3)
                ),
                List.of("23930")
        ));

        list.add(TalentNodeDef.of(
                "17229", -7113.99f, -6140.97f,
                "silent_guardian", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.2)
                ),
                List.of("29985", "34493")
        ));

        list.add(TalentNodeDef.of(
                "56366", 10758.7f, -5168.92f,
                "silent_shiv", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_DAGGERS, 0.05),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS, 0.15)
                ),
                List.of("35755", "52215", "632")
        ));

        list.add(TalentNodeDef.of(
                "52392", -5636.75f, 3607.2f,
                "singular_purpose", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, -0.05),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.4)
                ),
                List.of("26092")
        ));

        list.add(TalentNodeDef.of(
                "15829", 3994.0f, -485.915f,
                "siphon", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.02),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.25)
                ),
                List.of("46146", "55227")
        ));

        list.add(TalentNodeDef.of(
                "12906", 2539.97f, 5787.5f,
                "sitting_duck", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_IMMOBILISED_ENEMIES, 0.35),
                        TalentNodeGen.stat(StatIds.YOUR_HITS_CANNOT_BE_EVASION_BY_PINNED_ENEMIES, 1.0)
                ),
                List.of("3234", "9272")
        ));

        list.add(TalentNodeDef.of(
                "2645", -10366.4f, 4162.38f,
                "skullcrusher", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES_WITH_MACES, 0.2)
                ),
                List.of("526", "52829")
        ));

        list.add(TalentNodeDef.of(
                "55308", -5085.88f, -2315.83f,
                "sling_shots", "passives_projectiledmgnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_WITH_PROJECTILES, 0.2)
                ),
                List.of("28800", "30701", "38313")
        ));

        list.add(TalentNodeDef.of(
                "23362", 8838.57f, -5546.51f,
                "slippery_ice", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.25),
                        TalentNodeGen.stat(StatIds.UNAFFECTED_BY_CHILL_DURING_DODGE_ROLL, 1.0)
                ),
                List.of("32672")
        ));

        list.add(TalentNodeDef.of(
                "31326", -2914.44f, -7108.15f,
                "slow_burn", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.2)
                ),
                List.of("39716", "44092")
        ));

        list.add(TalentNodeDef.of(
                "45363", -3810.67f, 1067.77f,
                "smash", "passives_physicaldamageovertimenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_AGAINST_HEAVY_STUNNED_ENEMIES, 0.4)
                ),
                List.of("19011", "58528")
        ));

        list.add(TalentNodeDef.of(
                "54148", -3244.23f, 9916.18f,
                "smoke_inhalation", "passives_firedamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_DAMAGING_AILMENTS_ON_ENEMIES, 0.15)
                ),
                List.of("36325", "52746")
        ));

        list.add(TalentNodeDef.of(
                "11526", 11010.8f, 5117.5f,
                "sniper", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ARROWS_GAIN_CRITICAL_HIT_CHANCE_AS_THEY_TRAVEL_FARTHER_UP_TO_INC_CRITICAL_HIT_CHANCE_AFTER_METRES, 0.4)
                ),
                List.of("38993", "8573")
        ));

        list.add(TalentNodeDef.of(
                "9421", 6693.54f, 958.875f,
                "snowpiercer", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("54557")
        ));

        list.add(TalentNodeDef.of(
                "51169", -175.952f, -6627.3f,
                "soul_bloom", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.15)
                ),
                List.of("25893")
        ));

        list.add(TalentNodeDef.of(
                "34473", 6876.66f, -5410.35f,
                "spaghettification", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.29),
                        TalentNodeGen.stat(StatIds.TO_ALL_ATTRIBUTES, 13.0),
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, -0.07),
                        TalentNodeGen.stat(StatIds.INC_LIGHT_RADIUS, -0.23)
                ),
                List.of("54883")
        ));

        list.add(TalentNodeDef.of(
                "14602", -3067.78f, 6902.54f,
                "specialised_shots", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BOLT_SPEED, 0.15),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_CROSSBOWS, 0.2)
                ),
                List.of("54985")
        ));

        list.add(TalentNodeDef.of(
                "34324", 7749.73f, -5950.17f,
                "spectral_ward", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_ENERGY_SHIELD_PER_ITEM_EVASION_ON_EQUIPPED_BODY_ARMOR, 1.0)
                ),
                List.of("56838")
        ));

        list.add(TalentNodeDef.of(
                "17254", 5300.06f, -3797.31f,
                "spell_haste", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.15),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.08)
                ),
                List.of("26596")
        ));

        list.add(TalentNodeDef.of(
                "49984", 5537.79f, -3733.62f,
                "spellblade", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON, 0.32),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("32016")
        ));

        list.add(TalentNodeDef.of(
                "3698", -6860.45f, 6813.8f,
                "spike_pit", "icongroundslam",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_IN_JAGGED_GROUND_YOU_CREATE_TAKE_INC_DAMAGE, 0.1)
                ),
                List.of("33137", "61927")
        ));

        list.add(TalentNodeDef.of(
                "40117", -7080.42f, 2210.67f,
                "spiked_armor", "passives_thornsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR, 0.5)
                ),
                List.of("17903", "31848")
        ));

        list.add(TalentNodeDef.of(
                "36808", 5402.89f, 9003.21f,
                "spiked_shield", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_PER_ITEM_ARMOR_AND_ITEM_EVASION_ON_EQUIPPED_SHIELD, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.5)
                ),
                List.of("22057", "34076")
        ));

        list.add(TalentNodeDef.of(
                "8535", -8976.33f, -2575.42f,
                "spiked_whip", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_FLAILS, 0.25)
                ),
                List.of("43443")
        ));

        list.add(TalentNodeDef.of(
                "1546", -2599.56f, -3561.88f,
                "spiral_into_depression", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.25),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.25)
                ),
                List.of("38827")
        ));

        list.add(TalentNodeDef.of(
                "2138", -2457.53f, -3315.88f,
                "spiral_into_insanity", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.29),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD, 0.2)
                ),
                List.of("46604")
        ));

        list.add(TalentNodeDef.of(
                "14934", -2315.51f, -3561.88f,
                "spiral_into_mania", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.1),
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.13)
                ),
                List.of("58930")
        ));

        list.add(TalentNodeDef.of(
                "51105", -6808.4f, -7384.58f,
                "spirit_bond", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.3),
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DURATION, 0.3)
                ),
                List.of("35974", "48979")
        ));

        list.add(TalentNodeDef.of(
                "9328", -9903.54f, -6537.44f,
                "spirit_of_the_bear", "passives_druidshapeshiftbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED, 0.5),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WHILE_SHAPESHIFTED, 0.25)
                ),
                List.of("58496")
        ));

        list.add(TalentNodeDef.of(
                "3348", -10650.0f, -3724.53f,
                "spirit_of_the_wolf", "passives_druidshapeshiftwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("48314")
        ));

        list.add(TalentNodeDef.of(
                "26104", -8692.57f, -7703.86f,
                "spirit_of_the_wyvern", "passives_druidshapeshiftwyvernnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_SHAPESHIFTED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.25)
                ),
                List.of("27611", "32186", "43282", "55672")
        ));

        list.add(TalentNodeDef.of(
                "49088", 1798.44f, -7166.71f,
                "splintering_force", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_BREAK_ARMOR_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, 0.03)
                ),
                List.of("17394")
        ));

        list.add(TalentNodeDef.of(
                "7449", 4230.65f, 3149.72f,
                "splinters", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS, 0.5)
                ),
                List.of("46874")
        ));

        list.add(TalentNodeDef.of(
                "42302", 6857.82f, 2075.69f,
                "split_shot", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING, 0.75)
                ),
                List.of("31918", "45331", "9151")
        ));

        list.add(TalentNodeDef.of(
                "13980", -10113.8f, 5105.13f,
                "split_the_earth", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.1),
                        TalentNodeGen.stat(StatIds.STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_CHANCE_TO_DEAL_SPLASH_DAMAGE, 0.1)
                ),
                List.of("12276", "14693")
        ));

        list.add(TalentNodeDef.of(
                "20251", -10375.0f, -715.924f,
                "splitting_ground", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_WHICH_CREATE_FISSURES_HAVE_A_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE, 0.2)
                ),
                List.of("21184")
        ));

        list.add(TalentNodeDef.of(
                "23736", 4265.97f, 8407.08f,
                "spray_and_pray", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_MOVING, -0.2),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.5),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("4364")
        ));

        list.add(TalentNodeDef.of(
                "11578", 3512.67f, 4752.05f,
                "spreading_shocks", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.SHOCKING_HIT_DAMAGE_HAVE_A_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_METRE_RADIUS, 0.5)
                ),
                List.of("4")
        ));

        list.add(TalentNodeDef.of(
                "63759", 6359.19f, 2492.09f,
                "stacking_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.TARGETS_CAN_BE_AFFECTED_BY_OF_YOUR_POISONS_AT_THE_SAME_TIME, 1.0),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, -0.2)
                ),
                List.of("24401", "26565")
        ));

        list.add(TalentNodeDef.of(
                "39881", 8344.62f, -2618.36f,
                "staggering_palm", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("16013", "35173")
        ));

        list.add(TalentNodeDef.of(
                "61104", 3779.38f, 3832.89f,
                "staggering_wounds", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE, 0.5)
                ),
                List.of("20837", "49473")
        ));

        list.add(TalentNodeDef.of(
                "46565", -3736.26f, 12649.7f,
                "stance_breaker", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.25)
                ),
                List.of("15855", "20091")
        ));

        list.add(TalentNodeDef.of(
                "5802", 7559.9f, 4790.76f,
                "stand_and_deliver", "passives_chainingprojectiles",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_WITHIN_XM, 0.4),
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM, 0.25)
                ),
                List.of("18049")
        ));

        list.add(TalentNodeDef.of(
                "6304", -5389.99f, 344.085f,
                "stand_ground", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_AFFECTED_BY_ANY_DAMAGING_AILMENT, 0.01),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_STATIONARY, 0.01)
                ),
                List.of("32600")
        ));

        list.add(TalentNodeDef.of(
                "2486", 1353.01f, 6256.98f,
                "stars_aligned", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 1.0)
                ),
                List.of("19341", "63732")
        ));

        list.add(TalentNodeDef.of(
                "34908", 8282.57f, 4201.1f,
                "staunch_deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_MAIM_ON_YOU),
                        TalentNodeGen.stat(StatIds.DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU)
                ),
                List.of("3994")
        ));

        list.add(TalentNodeDef.of(
                "37408", 3875.71f, 607.837f,
                "staunching", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("31855", "60738")
        ));

        list.add(TalentNodeDef.of(
                "26479", -7894.8f, 5850.06f,
                "steadfast_resolve", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_CANNOT_BE_LIGHT_STUNNED_IF_YOU_VE_BEEN_STUNNED_RECENTLY, 1.0)
                ),
                List.of("64324", "6900")
        ));

        list.add(TalentNodeDef.of(
                "47782", -483.905f, 5712.0f,
                "steady_footing", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.4),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.2)
                ),
                List.of("28361", "38003")
        ));

        list.add(TalentNodeDef.of(
                "48198", 2381.48f, -1222.15f,
                "step_like_mist", "passives_minemanareservationnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY_AND_INTELLIGENCE, 5.0)
                ),
                List.of("13411", "15975", "26931", "29361", "39964", "40068", "65437")
        ));

        list.add(TalentNodeDef.of(
                "47441", 776.035f, -7320.94f,
                "stigmata", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, 0.3),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_CREATE_AN_OFFERING, 0.03)
                ),
                List.of("34030")
        ));

        list.add(TalentNodeDef.of(
                "7163", 5755.66f, 5066.46f,
                "stimulants", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_DURING_ANY_FLASK_EFFECT, 0.16)
                ),
                List.of("45100")
        ));

        list.add(TalentNodeDef.of(
                "1603", 4657.1f, -7752.56f,
                "storm_driven", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD, 0.15)
                ),
                List.of("61926")
        ));

        list.add(TalentNodeDef.of(
                "61921", 7434.14f, -1229.42f,
                "storm_surge", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.15)
                ),
                List.of("37304", "38215")
        ));

        list.add(TalentNodeDef.of(
                "336", 7284.41f, -670.631f,
                "storm_swell", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.08)
                ),
                List.of("37304", "4806")
        ));

        list.add(TalentNodeDef.of(
                "43139", -612.805f, -9125.76f,
                "stormbreaker", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY, 0.2)
                ),
                List.of("38068", "56409")
        ));

        list.add(TalentNodeDef.of(
                "38535", -6156.67f, -8092.72f,
                "stormcharged", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_RESISTANCES, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS, 0.05)
                ),
                List.of("16596", "61063")
        ));

        list.add(TalentNodeDef.of(
                "47387", -10694.6f, -176.705f,
                "stormkeeper", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.3),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("18593")
        ));

        list.add(TalentNodeDef.of(
                "13515", -1446.33f, -9472.75f,
                "stormwalker", "passives_stormborn",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_ON_SHOCKED_GROUND, 0.15),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.4)
                ),
                List.of("17025", "22784")
        ));

        list.add(TalentNodeDef.of(
                "50908", -2431.32f, -2692.66f,
                "strength_of_the_deep", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES, 0.02),
                        TalentNodeGen.stat(StatIds.TO_MAX_ENDURANCE_CHARGES, 1.0)
                ),
                List.of("12601")
        ));

        list.add(TalentNodeDef.of(
                "45177", -2832.96f, 3529.49f,
                "strike_true", "passives_accuracydex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.2),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("30219")
        ));

        list.add(TalentNodeDef.of(
                "33922", 1669.67f, -5546.35f,
                "stripped_defences", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.EXPOSURE_YOU_INFLICT_LOWERS_RESISTANCES_BY_AN_ADDITIONAL, 0.05)
                ),
                List.of("60741", "6950")
        ));

        list.add(TalentNodeDef.of(
                "10998", 2070.62f, 8936.47f,
                "strong_chin", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.25),
                        TalentNodeGen.stat(StatIds.GAIN_STUN_THRESHOLD_EQUAL_TO_THE_LOWEST_OF_EVASION_AND_ARMOR_ON_YOUR_HELMET, 1.0)
                ),
                List.of("21438", "61396")
        ));

        list.add(TalentNodeDef.of(
                "39369", 11701.0f, 272.057f,
                "struck_through", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_HAVE_TO_CRITICAL_HIT_CHANCE, 0.01)
                ),
                List.of("2936", "54983")
        ));
        return list;
    }
}
