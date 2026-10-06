package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes19 {

    private MinorNodes19() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "21225", 8557.89f, 7239.15f,
                "spear_damage", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SPEARS, 0.1)
                ),
                List.of("24786", "38369", "9240")
        ));

        list.add(TalentNodeDef.of(
                "36677", 8847.14f, 7072.15f,
                "spear_damage", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SPEARS, 0.1)
                ),
                List.of("9227", "9240")
        ));

        list.add(TalentNodeDef.of(
                "9240", 8705.12f, 7154.15f,
                "spear_damage", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SPEARS, 0.1)
                ),
                List.of("21225", "36677")
        ));

        list.add(TalentNodeDef.of(
                "16596", -6201.92f, -7837.09f,
                "speed_with_elemental_skills", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS, 0.03)
                ),
                List.of("38535", "5088")
        ));

        list.add(TalentNodeDef.of(
                "49537", -6201.92f, -7503.09f,
                "speed_with_elemental_skills", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS, 0.03)
                ),
                List.of("5088", "58016")
        ));

        list.add(TalentNodeDef.of(
                "50084", -1527.89f, -734.805f,
                "spell_and_attack_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("1143", "28446", "45923", "61525")
        ));

        list.add(TalentNodeDef.of(
                "14033", -1715.75f, -4874.19f,
                "spell_and_minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("34553", "36286")
        ));

        list.add(TalentNodeDef.of(
                "36286", -1532.72f, -4885.18f,
                "spell_and_minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("14033", "34058", "41130")
        ));

        list.add(TalentNodeDef.of(
                "41130", -1431.7f, -5038.19f,
                "spell_and_minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("36286", "8397")
        ));

        list.add(TalentNodeDef.of(
                "11788", 1149.31f, -6893.67f,
                "spell_area_damage", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, 0.1)
                ),
                List.of("14355", "17107")
        ));

        list.add(TalentNodeDef.of(
                "14355", 1326.08f, -6820.44f,
                "spell_area_damage", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, 0.1)
                ),
                List.of("11788", "8483")
        ));

        list.add(TalentNodeDef.of(
                "17107", 1264.03f, -7385.9f,
                "spell_area_damage", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_AREA_DAMAGE, 0.1)
                ),
                List.of("11788", "38732")
        ));

        list.add(TalentNodeDef.of(
                "17711", 1057.83f, -5957.03f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("40783", "46989")
        ));

        list.add(TalentNodeDef.of(
                "18846", -5069.6f, -5325.92f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("35171", "8660")
        ));

        list.add(TalentNodeDef.of(
                "22949", -5648.11f, -5659.92f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("35171", "44783", "44872")
        ));

        list.add(TalentNodeDef.of(
                "33180", 972.528f, -6466.89f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("46989", "60269")
        ));

        list.add(TalentNodeDef.of(
                "35171", -5191.85f, -5782.17f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("18846", "22949")
        ));

        list.add(TalentNodeDef.of(
                "46989", 1149.31f, -6393.67f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("17711", "33180")
        ));

        list.add(TalentNodeDef.of(
                "52615", 8159.99f, -7674.9f,
                "spell_area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("37876", "7302")
        ));

        list.add(TalentNodeDef.of(
                "12778", 5038.0f, -10558.2f,
                "spell_critical_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("26563", "61905")
        ));

        list.add(TalentNodeDef.of(
                "21245", -3804.23f, -9338.75f,
                "spell_critical_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("57178", "58183", "61179")
        ));

        list.add(TalentNodeDef.of(
                "22290", 0.005f, -4105.9f,
                "spell_critical_chance", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("32404", "36302", "48821")
        ));

        list.add(TalentNodeDef.of(
                "32404", -163.995f, -4439.9f,
                "spell_critical_chance", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("15618", "22290", "5501")
        ));

        list.add(TalentNodeDef.of(
                "3472", 228.135f, -7194.19f,
                "spell_critical_chance", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("26682", "46819", "56640")
        ));

        list.add(TalentNodeDef.of(
                "46088", 3925.63f, -9721.12f,
                "spell_critical_chance", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("13823", "55947")
        ));

        list.add(TalentNodeDef.of(
                "48805", 4965.25f, -10850.2f,
                "spell_critical_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("26563", "4810", "7782")
        ));

        list.add(TalentNodeDef.of(
                "55947", 3731.9f, -10115.7f,
                "spell_critical_chance", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("10131", "46088", "62153")
        ));

        list.add(TalentNodeDef.of(
                "61179", -3955.44f, -9600.64f,
                "spell_critical_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1)
                ),
                List.of("21245", "32745")
        ));

        list.add(TalentNodeDef.of(
                "57178", -3804.23f, -8886.17f,
                "spell_critical_chance_and_critical_ailment_effect", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_SPELLS, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS, 0.15)
                ),
                List.of("21245", "5284", "60241")
        ));

        list.add(TalentNodeDef.of(
                "1143", -2030.44f, -1011.11f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("50084", "58170")
        ));

        list.add(TalentNodeDef.of(
                "15618", 0.005f, -4773.9f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("32404", "48821", "57710")
        ));

        list.add(TalentNodeDef.of(
                "26682", 474.135f, -7336.21f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("10398", "3472", "56640")
        ));

        list.add(TalentNodeDef.of(
                "2672", -8019.59f, -5490.56f,
                "spell_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("28267", "45569")
        ));

        list.add(TalentNodeDef.of(
                "32054", 3453.28f, -9721.12f,
                "spell_critical_damage", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("13823", "62153")
        ));

        list.add(TalentNodeDef.of(
                "45569", -8183.59f, -5490.56f,
                "spell_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("2672", "55596")
        ));

        list.add(TalentNodeDef.of(
                "48821", 164.005f, -4439.9f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("15618", "22290", "5501")
        ));

        list.add(TalentNodeDef.of(
                "56640", 228.135f, -7478.24f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("10398", "26682", "3472")
        ));

        list.add(TalentNodeDef.of(
                "61067", -2304.82f, -1084.63f,
                "spell_critical_damage", "passives_spellmultiplyer2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("58170", "6898")
        ));

        list.add(TalentNodeDef.of(
                "62153", 3366.84f, -9870.85f,
                "spell_critical_damage", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_SPELL_DAMAGE_BONUS, 0.15)
                ),
                List.of("32054", "55947")
        ));

        list.add(TalentNodeDef.of(
                "1755", -532.637f, -2531.69f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08)
                ),
                List.of("18845", "41965")
        ));

        list.add(TalentNodeDef.of(
                "18845", -309.52f, -2332.65f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("1755", "4739", "55807")
        ));

        list.add(TalentNodeDef.of(
                "29652", -4034.71f, -2533.44f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("15180", "32194", "6008")
        ));

        list.add(TalentNodeDef.of(
                "32016", 5930.92f, -3628.28f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON, 0.12)
                ),
                List.of("49984", "51416", "5766")
        ));

        list.add(TalentNodeDef.of(
                "33254", -253.759f, -5933.95f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("53975", "55104")
        ));

        list.add(TalentNodeDef.of(
                "34096", -4163.6f, -3014.47f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.12)
                ),
                List.of("58096", "61444")
        ));

        list.add(TalentNodeDef.of(
                "37876", 8064.92f, -7329.84f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("25729", "47976", "52615")
        ));

        list.add(TalentNodeDef.of(
                "41965", -490.19f, -2690.1f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08)
                ),
                List.of("1755", "51184")
        ));

        list.add(TalentNodeDef.of(
                "45319", 5163.36f, -10081.4f,
                "spell_damage", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08)
                ),
                List.of("26135", "3251", "55041")
        ));

        list.add(TalentNodeDef.of(
                "4739", -138.055f, -1693.96f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("18845", "22419", "54447")
        ));

        list.add(TalentNodeDef.of(
                "53975", -154.177f, -5693.53f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("33254", "57710")
        ));

        list.add(TalentNodeDef.of(
                "55104", -421.205f, -5992.04f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("19125", "33254")
        ));

        list.add(TalentNodeDef.of(
                "5766", 5445.81f, -3983.07f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON, 0.12)
                ),
                List.of("16705", "26596", "32016", "51416")
        ));

        list.add(TalentNodeDef.of(
                "58170", -2146.41f, -1127.08f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("1143", "61067")
        ));

        list.add(TalentNodeDef.of(
                "6008", -4235.57f, -2734.3f,
                "spell_damage", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.1)
                ),
                List.of("29652", "58096")
        ));

        list.add(TalentNodeDef.of(
                "25829", -1853.84f, -7600.63f,
                "spell_damage_and_cast_speed", "passives_auranotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.02)
                ),
                List.of("44372", "57791")
        ));

        list.add(TalentNodeDef.of(
                "44372", -2094.25f, -7785.1f,
                "spell_damage_and_cast_speed", "passives_auranotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.02)
                ),
                List.of("11679", "25829")
        ));

        list.add(TalentNodeDef.of(
                "57791", -2154.28f, -7561.07f,
                "spell_damage_and_cast_speed", "passives_auranotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.02)
                ),
                List.of("25829", "52229")
        ));

        list.add(TalentNodeDef.of(
                "26135", 5539.06f, -9925.79f,
                "spell_damage_and_projectile_speed", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS, 0.08)
                ),
                List.of("2335", "45319")
        ));

        list.add(TalentNodeDef.of(
                "55041", 5216.44f, -10484.6f,
                "spell_damage_and_projectile_speed", "passives_spellcritical",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS, -0.05)
                ),
                List.of("35564", "45319")
        ));

        list.add(TalentNodeDef.of(
                "36450", 3832.63f, -9337.35f,
                "spell_damage_on_full_energy", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_ON_FULL_ENERGY_SHIELD, 0.12)
                ),
                List.of("10881", "11838")
        ));

        list.add(TalentNodeDef.of(
                "15180", -3961.19f, -2807.82f,
                "spell_hinder", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS, 0.1)
                ),
                List.of("29652", "61444")
        ));

        list.add(TalentNodeDef.of(
                "43453", 8124.67f, 632.405f,
                "sprint_movement_speed", "passives_movementspeedandevasion",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_SPRINTING, 0.03)
                ),
                List.of("54152", "64050")
        ));

        list.add(TalentNodeDef.of(
                "54152", 8280.54f, 369.295f,
                "sprint_movement_speed", "passives_movementspeedandevasion",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_WHILE_SPRINTING, 0.03)
                ),
                List.of("24656", "43453")
        ));

        list.add(TalentNodeDef.of(
                "13075", -8021.19f, 6127.75f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 12.0)
                ),
                List.of("50392", "61409")
        ));

        list.add(TalentNodeDef.of(
                "24646", -8445.51f, 6128.94f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 12.0)
                ),
                List.of("27082", "61409")
        ));

        list.add(TalentNodeDef.of(
                "34202", -678.325f, 11065.6f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("32976", "49285")
        ));

        list.add(TalentNodeDef.of(
                "49285", -426.325f, 10923.5f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("34202", "364")
        ));

        list.add(TalentNodeDef.of(
                "51702", -5674.31f, 4487.34f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("22975", "25482")
        ));

        list.add(TalentNodeDef.of(
                "54485", -5582.8f, 4145.84f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("25482", "57703")
        ));

        list.add(TalentNodeDef.of(
                "60203", 706.445f, 11086.4f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("5332", "7218")
        ));

        list.add(TalentNodeDef.of(
                "60620", -5241.3f, 4237.34f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("25482", "45992")
        ));

        list.add(TalentNodeDef.of(
                "61409", -8236.51f, 5912.43f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 12.0)
                ),
                List.of("13075", "24646")
        ));

        list.add(TalentNodeDef.of(
                "61472", -5332.8f, 4578.85f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("25482", "36389", "9417")
        ));

        list.add(TalentNodeDef.of(
                "7218", 457.445f, 10939.2f,
                "strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 8.0)
                ),
                List.of("34813", "60203")
        ));

        list.add(TalentNodeDef.of(
                "25458", 6149.25f, 5864.65f,
                "strength_and_critical_damage_bonus_on_you", "passives_azmeriwildox",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.05),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("34853", "37568")
        ));

        list.add(TalentNodeDef.of(
                "34853", 6365.75f, 5825.65f,
                "strength_and_critical_damage_bonus_on_you", "passives_azmeriwildox",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.05),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("25458", "43044")
        ));

        list.add(TalentNodeDef.of(
                "37568", 6529.75f, 5989.65f,
                "strength_and_critical_damage_bonus_on_you", "passives_azmeriwildox",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.05),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("25458", "45370")
        ));

        list.add(TalentNodeDef.of(
                "16871", 9150.0f, 4449.79f,
                "strength_and_dexterity", "passives_azmeriwildboar",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 4.0),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 4.0)
                ),
                List.of("37242", "9532")
        ));

        list.add(TalentNodeDef.of(
                "9532", 9275.0f, 4233.29f,
                "strength_and_dexterity", "passives_azmeriwildboar",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 4.0),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 4.0)
                ),
                List.of("16871", "59368")
        ));

        list.add(TalentNodeDef.of(
                "11861", -1448.57f, -2574.04f,
                "strength_and_spell_damage", "passives_plusstrength",
                List.of(),
                List.of("59795", "7258")
        ));

        list.add(TalentNodeDef.of(
                "35720", -1336.5f, -2155.78f,
                "strength_and_spell_damage", "passives_plusstrength",
                List.of(),
                List.of("3281", "7258")
        ));

        list.add(TalentNodeDef.of(
                "7258", -1271.79f, -2397.26f,
                "strength_and_spell_damage", "passives_plusstrength",
                List.of(),
                List.of("11861", "35720")
        ));

        list.add(TalentNodeDef.of(
                "13307", -2297.58f, -9965.56f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("3894", "58789")
        ));

        list.add(TalentNodeDef.of(
                "14666", 146.295f, -3752.19f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("9642", "2254")
        ));

        list.add(TalentNodeDef.of(
                "17602", 6668.1f, -8981.04f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("59798", "59799", "61403")
        ));

        list.add(TalentNodeDef.of(
                "25363", -2125.82f, -6600.67f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("44098", "1823", "34531")
        ));

        list.add(TalentNodeDef.of(
                "26614", -2864.23f, -9786.89f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("44344", "46275")
        ));

        list.add(TalentNodeDef.of(
                "44098", -1909.32f, -6225.67f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("25363", "4061", "34531")
        ));

        list.add(TalentNodeDef.of(
                "46275", -2654.79f, -9759.32f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("26614", "3894")
        ));

        list.add(TalentNodeDef.of(
                "517", 419.295f, -4284.19f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("39037", "61027", "9642")
        ));

        list.add(TalentNodeDef.of(
                "58789", -2216.74f, -10160.7f,
                "stun_and_ailment_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_AILMENT_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.08)
                ),
                List.of("13307", "54521")
        ));

        list.add(TalentNodeDef.of(
                "4621", -7598.42f, -9274.8f,
                "stun_and_elemental_threshold", "passives_life1",
                List.of(),
                List.of("57202", "57386", "59093")
        ));

        list.add(TalentNodeDef.of(
                "18472", 7031.53f, 7361.76f,
                "stun_and_freeze_buildup", "passives_channellingattacksnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("46533", "6842")
        ));

        list.add(TalentNodeDef.of(
                "36723", 7031.53f, 7033.76f,
                "stun_and_freeze_buildup", "passives_channellingattacksnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("28329", "3700")
        ));

        list.add(TalentNodeDef.of(
                "3700", 7195.53f, 7197.76f,
                "stun_and_freeze_buildup", "passives_channellingattacksnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("36723", "6842")
        ));

        list.add(TalentNodeDef.of(
                "46533", 6867.53f, 7197.76f,
                "stun_and_freeze_buildup", "passives_channellingattacksnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("18472", "28329")
        ));

        list.add(TalentNodeDef.of(
                "6842", 7267.71f, 7433.94f,
                "stun_and_freeze_buildup", "passives_channellingattacksnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("18472", "30657", "3700")
        ));

        list.add(TalentNodeDef.of(
                "10047", -7653.13f, -1285.88f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("4673", "62757")
        ));

        list.add(TalentNodeDef.of(
                "10251", -7853.22f, 1517.92f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("45327", "7204")
        ));

        list.add(TalentNodeDef.of(
                "17726", 2256.8f, 2935.01f,
                "stun_buildup", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("11826", "33053")
        ));

        list.add(TalentNodeDef.of(
                "20547", -4938.38f, -1099.87f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("22558", "33340", "34412")
        ));

        list.add(TalentNodeDef.of(
                "20691", -3447.4f, 6692.21f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("41739", "60404")
        ));

        list.add(TalentNodeDef.of(
                "21670", -9677.83f, 3405.65f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("27296", "29358")
        ));

        list.add(TalentNodeDef.of(
                "29358", -9478.42f, 3377.08f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("21670", "44069", "917")
        ));

        list.add(TalentNodeDef.of(
                "33340", -5179.86f, -1164.58f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("20547", "51267")
        ));

        list.add(TalentNodeDef.of(
                "39581", -2176.59f, 1158.71f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("46325", "6839")
        ));

        list.add(TalentNodeDef.of(
                "41739", -3507.42f, 6468.19f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("20691", "35265")
        ));

        list.add(TalentNodeDef.of(
                "44069", -9347.58f, 3167.63f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("29358", "65160")
        ));

        list.add(TalentNodeDef.of(
                "44406", -5197.82f, 5668.02f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("63451", "8600")
        ));

        list.add(TalentNodeDef.of(
                "45327", -8000.45f, 1602.92f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("10251", "59945")
        ));

        list.add(TalentNodeDef.of(
                "46741", -7817.13f, -1569.93f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("49734", "62757")
        ));

        list.add(TalentNodeDef.of(
                "51267", -5244.57f, -1406.06f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("11886", "33340")
        ));

        list.add(TalentNodeDef.of(
                "57846", -4471.23f, 6087.52f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("38876", "63451")
        ));

        list.add(TalentNodeDef.of(
                "62757", -7593.11f, -1509.91f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("10047", "46741")
        ));

        list.add(TalentNodeDef.of(
                "6839", -2518.09f, 1250.22f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("39581", "5710")
        ));

        list.add(TalentNodeDef.of(
                "7204", -7711.19f, 1435.92f,
                "stun_buildup", "passives_stunstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.15)
                ),
                List.of("10251", "4985", "53527", "64525")
        ));

        list.add(TalentNodeDef.of(
                "58157", 7143.3f, -3711.17f,
                "stun_recovery", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.2)
                ),
                List.of("15424", "61149")
        ));

        list.add(TalentNodeDef.of(
                "59213", -4538.14f, -1355.67f,
                "stun_recovery", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.2)
                ),
                List.of("48240", "60886")
        ));

        list.add(TalentNodeDef.of(
                "60886", -4381.17f, -1400.62f,
                "stun_recovery", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.2)
                ),
                List.of("55933", "59213")
        ));

        list.add(TalentNodeDef.of(
                "61149", 7342.34f, -3617.46f,
                "stun_recovery", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.2)
                ),
                List.of("42760", "58157")
        ));

        list.add(TalentNodeDef.of(
                "10372", -4381.17f, -1542.65f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("36191", "55933")
        ));

        list.add(TalentNodeDef.of(
                "10571", -4744.79f, -1385.19f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("48240", "7392")
        ));

        list.add(TalentNodeDef.of(
                "15424", 7242.76f, -3857.88f,
                "stun_threshold", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.15)
                ),
                List.of("35151", "51741", "58157")
        ));

        list.add(TalentNodeDef.of(
                "1628", -8265.04f, -9941.42f,
                "stun_threshold", "passives_life1",
                List.of(),
                List.of("49769", "57202", "9554")
        ));

        list.add(TalentNodeDef.of(
                "16332", -8381.72f, -10340.1f,
                "stun_threshold", "passives_life1",
                List.of(),
                List.of("11160", "49258")
        ));

        list.add(TalentNodeDef.of(
                "17532", -3366.12f, 7831.29f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("24477", "37612", "61934")
        ));

        list.add(TalentNodeDef.of(
                "18742", -2733.29f, 2529.2f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("62732", "7721")
        ));

        list.add(TalentNodeDef.of(
                "28361", -212.411f, 5837.0f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("47782", "48635")
        ));

        list.add(TalentNodeDef.of(
                "35151", 7320.08f, -4017.36f,
                "stun_threshold", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.15)
                ),
                List.of("15424", "44453")
        ));

        list.add(TalentNodeDef.of(
                "36191", -4538.14f, -1587.6f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("10372", "40325")
        ));

        list.add(TalentNodeDef.of(
                "36709", -1842.78f, 1554.69f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("1913", "63393")
        ));

        list.add(TalentNodeDef.of(
                "37242", 9275.0f, 4666.3f,
                "stun_threshold", "passives_azmeriwildboar",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("16871", "54031", "61215")
        ));

        list.add(TalentNodeDef.of(
                "38003", -212.411f, 5587.0f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("47782", "63526")
        ));

        list.add(TalentNodeDef.of(
                "40596", -6456.61f, 5794.09f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("53921", "8600")
        ));

        list.add(TalentNodeDef.of(
                "44453", 7500.75f, -3891.84f,
                "stun_threshold", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.15)
                ),
                List.of("35151", "42760")
        ));

        list.add(TalentNodeDef.of(
                "53373", -3412.36f, 2874.27f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("36629", "39517", "64192")
        ));

        list.add(TalentNodeDef.of(
                "57202", -7842.42f, -9533.42f,
                "stun_threshold", "passives_life1",
                List.of(),
                List.of("1628", "4621")
        ));

        list.add(TalentNodeDef.of(
                "58838", -7025.59f, 4808.59f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("26725", "53921")
        ));

        list.add(TalentNodeDef.of(
                "59368", 9525.0f, 4233.29f,
                "stun_threshold", "passives_azmeriwildboar",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("48116", "61215", "9532")
        ));

        list.add(TalentNodeDef.of(
                "61215", 9616.51f, 4574.79f,
                "stun_threshold", "passives_azmeriwildboar",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("37242", "59368")
        ));

        list.add(TalentNodeDef.of(
                "61934", -3142.09f, 7771.27f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("17532", "48418")
        ));

        list.add(TalentNodeDef.of(
                "63393", -2092.78f, 1804.69f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("36709", "7721")
        ));

        list.add(TalentNodeDef.of(
                "64192", -3067.29f, 2863.2f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("53373", "62732")
        ));

        list.add(TalentNodeDef.of(
                "7392", -4744.79f, -1558.08f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("10571", "40325")
        ));

        list.add(TalentNodeDef.of(
                "917", -9469.83f, 3623.89f,
                "stun_threshold", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.12)
                ),
                List.of("29358", "65160")
        ));

        list.add(TalentNodeDef.of(
                "38966", 4789.34f, 6711.05f,
                "stun_threshold_and_evasion_rating", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.12)
                ),
                List.of("30210", "35058", "61976")
        ));

        list.add(TalentNodeDef.of(
                "21885", -7562.53f, 3840.64f,
                "stun_threshold_and_strength", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("1352", "52126")
        ));

        list.add(TalentNodeDef.of(
                "24477", -3366.12f, 7995.29f,
                "stun_threshold_and_strength", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("17532", "34671")
        ));

        list.add(TalentNodeDef.of(
                "34671", -3142.09f, 8055.32f,
                "stun_threshold_and_strength", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("24477", "48418")
        ));

        list.add(TalentNodeDef.of(
                "52126", -7437.53f, 3460.13f,
                "stun_threshold_and_strength", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 5.0)
                ),
                List.of("21885", "52298")
        ));

        list.add(TalentNodeDef.of(
                "13379", 3866.11f, 2806.15f,
                "stun_threshold_during_parry", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_PARRYING, 0.2)
                ),
                List.of("27761", "53539")
        ));

        list.add(TalentNodeDef.of(
                "17447", 5751.86f, 5605.9f,
                "stun_threshold_during_parry", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_PARRYING, 0.2)
                ),
                List.of("24843", "29320", "7526")
        ));

        list.add(TalentNodeDef.of(
                "29320", 6050.37f, 5588.88f,
                "stun_threshold_during_parry", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_PARRYING, 0.2)
                ),
                List.of("1680", "17447", "56860")
        ));

        list.add(TalentNodeDef.of(
                "36997", 3599.3f, 2962.09f,
                "stun_threshold_during_parry", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_PARRYING, 0.2)
                ),
                List.of("27761", "45650")
        ));
        return list;
    }
}
