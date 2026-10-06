package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes07 {

    private MinorNodes07() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "26596", 4977.45f, -3883.76f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("17254", "51416", "5766")
        ));

        list.add(TalentNodeDef.of(
                "28774", -6497.76f, -9898.1f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("10295", "17505", "2999")
        ));

        list.add(TalentNodeDef.of(
                "28839", -5249.58f, -6899.78f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("56703", "56762")
        ));

        list.add(TalentNodeDef.of(
                "29502", -142.023f, -3113.45f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("36302", "51184")
        ));

        list.add(TalentNodeDef.of(
                "47307", 142.033f, -3113.45f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("2254", "36302")
        ));

        list.add(TalentNodeDef.of(
                "4847", -2786.03f, -4048.85f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("19240", "6294")
        ));

        list.add(TalentNodeDef.of(
                "51416", 5333.4f, -3563.57f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("17088", "26596", "32016", "5766")
        ));

        list.add(TalentNodeDef.of(
                "52106", -6994.78f, -9611.14f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("10295", "17505", "44005")
        ));

        list.add(TalentNodeDef.of(
                "52429", -2543.98f, -3157.26f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("58930", "6294")
        ));

        list.add(TalentNodeDef.of(
                "56703", -4965.53f, -6899.78f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("15782", "28839")
        ));

        list.add(TalentNodeDef.of(
                "56762", -5249.58f, -7063.78f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("20032", "28839")
        ));

        list.add(TalentNodeDef.of(
                "58930", -2280.76f, -3303.1f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("14934", "52429")
        ));

        list.add(TalentNodeDef.of(
                "6294", -2945.53f, -3479.88f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("4847", "52429")
        ));

        list.add(TalentNodeDef.of(
                "12166", 6439.29f, -7908.16f,
                "cast_speed_with_cold_skills", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WITH_COLD_SKILLS, 0.03)
                ),
                List.of("19003", "26331", "4959")
        ));

        list.add(TalentNodeDef.of(
                "3128", 6439.29f, -7475.15f,
                "cast_speed_with_cold_skills", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WITH_COLD_SKILLS, 0.03)
                ),
                List.of("19003", "19722", "26331", "50121")
        ));

        list.add(TalentNodeDef.of(
                "33037", 708.375f, 9275.44f,
                "chaining_projectiles", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.05)
                ),
                List.of("47683", "55060")
        ));

        list.add(TalentNodeDef.of(
                "45331", 7169.05f, 1898.92f,
                "chaining_projectiles", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.05)
                ),
                List.of("23221", "33245", "42302", "60323")
        ));

        list.add(TalentNodeDef.of(
                "47683", 498.929f, 9406.28f,
                "chaining_projectiles", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, 0.05)
                ),
                List.of("33037", "51944")
        ));

        list.add(TalentNodeDef.of(
                "17729", -2543.25f, -4315.94f,
                "chance_to_poison_and_spell_damage", "passives_poison",
                List.of(),
                List.of("56466", "59136")
        ));

        list.add(TalentNodeDef.of(
                "56090", -2990.92f, -4106.5f,
                "chance_to_poison_and_spell_damage", "passives_poison",
                List.of(),
                List.of("38856", "59136")
        ));

        list.add(TalentNodeDef.of(
                "59136", -2878.44f, -4450.27f,
                "chance_to_poison_and_spell_damage", "passives_poison",
                List.of(),
                List.of("17729", "56090")
        ));

        list.add(TalentNodeDef.of(
                "10260", -4586.14f, -3228.93f,
                "channelling_damage", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("19203", "33730")
        ));

        list.add(TalentNodeDef.of(
                "11087", -7125.41f, -3801.09f,
                "channelling_damage", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("12232", "42635")
        ));

        list.add(TalentNodeDef.of(
                "19203", -4744.55f, -3186.48f,
                "channelling_damage", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("10260", "2653")
        ));

        list.add(TalentNodeDef.of(
                "42635", -7003.85f, -3642.67f,
                "channelling_damage", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("11087", "1502")
        ));

        list.add(TalentNodeDef.of(
                "12232", -7283.82f, -3758.64f,
                "channelling_damage_and_defences", "passives_channellingspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING, 0.04)
                ),
                List.of("11087", "39710", "872")
        ));

        list.add(TalentNodeDef.of(
                "2653", -4870.07f, -3005.81f,
                "channelling_damage_and_speed", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANNELLING_SKILLS_DEAL_INC_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WITH_CHANNELLING_SKILLS, 0.02)
                ),
                List.of("19203", "28002", "30896")
        ));

        list.add(TalentNodeDef.of(
                "36556", -7167.85f, -3478.67f,
                "channelling_defences", "passives_channellingspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING, 0.08)
                ),
                List.of("1502", "872")
        ));

        list.add(TalentNodeDef.of(
                "872", -7326.27f, -3600.23f,
                "channelling_defences", "passives_channellingspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_CHANNELLING, 0.08)
                ),
                List.of("12232", "36556")
        ));

        list.add(TalentNodeDef.of(
                "55033", -9235.31f, -1176.12f,
                "channelling_life_recoup", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING, 0.1)
                ),
                List.of("17059", "33203")
        ));

        list.add(TalentNodeDef.of(
                "6874", -8986.31f, -1607.4f,
                "channelling_life_recoup", "passives_channellingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING, 0.1)
                ),
                List.of("17059", "34940")
        ));

        list.add(TalentNodeDef.of(
                "30896", -4671.03f, -2912.1f,
                "channelling_speed", "passives_channellingspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WITH_CHANNELLING_SKILLS, 0.03)
                ),
                List.of("2653", "49172")
        ));

        list.add(TalentNodeDef.of(
                "49172", -4512.62f, -2954.55f,
                "channelling_speed", "passives_channellingspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WITH_CHANNELLING_SKILLS, 0.03)
                ),
                List.of("30896", "33730")
        ));

        list.add(TalentNodeDef.of(
                "17059", -9295.34f, -1400.15f,
                "channelling_stun_threshold", "passives_channellingdamage",
                List.of(),
                List.of("55033", "6874")
        ));

        list.add(TalentNodeDef.of(
                "33203", -8808.25f, -973.077f,
                "channelling_stun_threshold", "passives_channellingdamage",
                List.of(),
                List.of("38707", "55033")
        ));

        list.add(TalentNodeDef.of(
                "12367", -1522.24f, -7685.57f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("15885", "36358")
        ));

        list.add(TalentNodeDef.of(
                "19112", -1232.99f, -7184.57f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("3492", "36358")
        ));

        list.add(TalentNodeDef.of(
                "19779", 1917.54f, -11254.9f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("4925", "63891")
        ));

        list.add(TalentNodeDef.of(
                "20782", 7023.89f, -5325.35f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("52191", "59775")
        ));

        list.add(TalentNodeDef.of(
                "26885", 6312.01f, -5736.35f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("3463", "59775")
        ));

        list.add(TalentNodeDef.of(
                "27572", -3145.95f, -11525.0f,
                "chaos_damage", "passives_chaosdamage",
                List.of(),
                List.of("12940", "25570")
        ));

        list.add(TalentNodeDef.of(
                "32438", 7037.31f, -8704.88f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.11)
                ),
                List.of("55149", "6800")
        ));

        list.add(TalentNodeDef.of(
                "35095", 9617.68f, -6052.48f,
                "chaos_damage", "passives_chaosdamage2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.1)
                ),
                List.of("19359", "41886")
        ));

        list.add(TalentNodeDef.of(
                "36358", -1232.99f, -7518.57f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("12367", "19112")
        ));

        list.add(TalentNodeDef.of(
                "42999", 2166.53f, -10823.6f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("46554", "4925")
        ));

        list.add(TalentNodeDef.of(
                "46604", -2699.02f, -3415.17f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("2138", "52454")
        ));

        list.add(TalentNodeDef.of(
                "4925", 2288.79f, -11279.8f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("19779", "42999")
        ));

        list.add(TalentNodeDef.of(
                "5186", 7764.53f, -8668.33f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.11)
                ),
                List.of("58022", "6800")
        ));

        list.add(TalentNodeDef.of(
                "52454", -2693.71f, -3716.05f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("46604", "58387")
        ));

        list.add(TalentNodeDef.of(
                "54883", 6883.65f, -5638.13f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("34473", "57724")
        ));

        list.add(TalentNodeDef.of(
                "57724", 6518.13f, -5617.35f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("52191", "54883")
        ));

        list.add(TalentNodeDef.of(
                "58022", 7691.01f, -8333.04f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.11)
                ),
                List.of("14446", "26762", "5186")
        ));

        list.add(TalentNodeDef.of(
                "58387", -2213.53f, -3902.5f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("52454", "858")
        ));

        list.add(TalentNodeDef.of(
                "59775", 7020.48f, -5774.94f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("20782", "26885")
        ));

        list.add(TalentNodeDef.of(
                "63891", 1958.54f, -11041.8f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("19779", "63074")
        ));

        list.add(TalentNodeDef.of(
                "6800", 7559.71f, -8913.34f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.11)
                ),
                List.of("32438", "5186")
        ));

        list.add(TalentNodeDef.of(
                "858", -1800.53f, -3479.88f,
                "chaos_damage", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.07)
                ),
                List.of("3041", "58387")
        ));

        list.add(TalentNodeDef.of(
                "12419", 2459.54f, -5520.63f,
                "chaos_damage_and_duration", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.05)
                ),
                List.of("18913", "56063")
        ));

        list.add(TalentNodeDef.of(
                "18913", 2192.51f, -5655.14f,
                "chaos_damage_and_duration", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.05)
                ),
                List.of("12419", "39987")
        ));

        list.add(TalentNodeDef.of(
                "39987", 2150.51f, -5149.38f,
                "chaos_damage_and_duration", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.05)
                ),
                List.of("18913", "47177")
        ));

        list.add(TalentNodeDef.of(
                "19359", 9926.71f, -6259.73f,
                "chaos_damage_and_resistance", "passives_chaosdamage2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.03)
                ),
                List.of("35095", "52245", "9663")
        ));

        list.add(TalentNodeDef.of(
                "41886", 9677.71f, -5828.45f,
                "chaos_damage_and_resistance", "passives_chaosdamage2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.03)
                ),
                List.of("31765", "35095", "9663")
        ));

        list.add(TalentNodeDef.of(
                "9663", 9901.73f, -5888.48f,
                "chaos_resistance", "passives_chaosdamage2",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_CHAOS_RESISTANCE, 0.05)
                ),
                List.of("19359", "41886")
        ));

        list.add(TalentNodeDef.of(
                "19027", 9860.54f, 603.555f,
                "charge_duration", "passives_azmerivividstag",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION, 0.15)
                ),
                List.of("21156", "40471", "7847")
        ));

        list.add(TalentNodeDef.of(
                "21156", 10077.1f, 478.555f,
                "charge_duration", "passives_azmerivividstag",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION, 0.15)
                ),
                List.of("19027", "34623")
        ));

        list.add(TalentNodeDef.of(
                "34623", 9942.54f, 211.527f,
                "charge_duration_and_dexterity", "passives_azmerivividstag",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENDURANCE_FRENZY_AND_POWER_CHARGE_DURATION, 0.1),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 5.0)
                ),
                List.of("14262", "14769", "21156")
        ));

        list.add(TalentNodeDef.of(
                "21111", 6836.09f, 8742.5f,
                "charm_activation_chance", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_A_CHARM_IS_USED_TO_USE_ANOTHER_CHARM_WITHOUT_CONSUMING_CHARGES, 0.1)
                ),
                List.of("28823", "33099", "38463")
        ));

        list.add(TalentNodeDef.of(
                "28823", 7009.37f, 9389.19f,
                "charm_activation_chance", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_A_CHARM_IS_USED_TO_USE_ANOTHER_CHARM_WITHOUT_CONSUMING_CHARGES, 0.1)
                ),
                List.of("21111", "59303")
        ));

        list.add(TalentNodeDef.of(
                "20049", 3112.88f, 7607.0f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("17118", "30252", "32818")
        ));

        list.add(TalentNodeDef.of(
                "32818", 3362.88f, 7857.0f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("20049", "48135")
        ));

        list.add(TalentNodeDef.of(
                "48135", 3112.88f, 8107.0f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("12750", "30252", "32818")
        ));

        list.add(TalentNodeDef.of(
                "50420", 9009.04f, 4719.33f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("31364", "51241", "55118")
        ));

        list.add(TalentNodeDef.of(
                "51241", 9009.04f, 5003.38f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("50420", "55118")
        ));

        list.add(TalentNodeDef.of(
                "55118", 8763.04f, 4861.35f,
                "charm_charges", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_GAIN, 0.1)
                ),
                List.of("50420", "51241", "54984")
        ));

        list.add(TalentNodeDef.of(
                "14001", 6579.76f, 104.437f,
                "charm_charges_used", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_USED, -0.06)
                ),
                List.of("1631", "56893")
        ));

        list.add(TalentNodeDef.of(
                "38497", 7659.87f, 8739.09f,
                "charm_charges_used", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_USED, -0.06)
                ),
                List.of("43522", "62803")
        ));

        list.add(TalentNodeDef.of(
                "43522", 7012.87f, 8565.72f,
                "charm_charges_used", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_CHARGES_USED, -0.06)
                ),
                List.of("33099", "38463", "38497")
        ));

        list.add(TalentNodeDef.of(
                "1631", 6803.79f, 164.465f,
                "charm_duration", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_EFFECT_DURATION, 0.1)
                ),
                List.of("14001", "14658", "29049")
        ));

        list.add(TalentNodeDef.of(
                "29049", 6743.76f, 388.493f,
                "charm_duration", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHARM_EFFECT_DURATION, 0.1)
                ),
                List.of("1631", "56893")
        ));

        list.add(TalentNodeDef.of(
                "11094", 7250.85f, 9453.9f,
                "charm_effect", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT, 0.1)
                ),
                List.of("48462", "59303")
        ));

        list.add(TalentNodeDef.of(
                "30252", 2862.88f, 7857.0f,
                "charm_effect", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT, 0.1)
                ),
                List.of("20049", "48135")
        ));

        list.add(TalentNodeDef.of(
                "48462", 7724.58f, 8980.57f,
                "charm_effect", "passives_charmnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT, 0.1)
                ),
                List.of("11094", "62803")
        ));

        list.add(TalentNodeDef.of(
                "1091", -1161.05f, -10153.3f,
                "chill_effect_taken", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.1)
                ),
                List.of("13333", "48699")
        ));

        list.add(TalentNodeDef.of(
                "11337", -288.818f, -11285.5f,
                "chill_magnitude", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.15)
                ),
                List.of("23427", "29369", "44455")
        ));

        list.add(TalentNodeDef.of(
                "29369", -243.565f, -11029.8f,
                "chill_magnitude", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.15)
                ),
                List.of("11337", "41669")
        ));

        list.add(TalentNodeDef.of(
                "35708", -3474.93f, -479.603f,
                "chill_magnitude", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.12)
                ),
                List.of("2863", "33601", "5692")
        ));

        list.add(TalentNodeDef.of(
                "5692", -3414.91f, -255.575f,
                "chill_magnitude", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.12)
                ),
                List.of("35708", "41154", "8406")
        ));

        list.add(TalentNodeDef.of(
                "14890", 3517.68f, -4040.17f,
                "chill_magnitude_and_duration", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHILL_DURATION_ON_ENEMIES, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.1)
                ),
                List.of("21080", "27095")
        ));

        list.add(TalentNodeDef.of(
                "55572", 278.189f, -8559.59f,
                "cold_and_fire_damage", "passives_coldfirenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("4627", "57626")
        ));

        list.add(TalentNodeDef.of(
                "57626", 278.189f, -8279.42f,
                "cold_and_fire_damage", "passives_coldfirenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("36782", "55572")
        ));

        list.add(TalentNodeDef.of(
                "17044", -191.895f, -7617.42f,
                "cold_and_lightning_damage", "passives_coldlightningnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("23907", "60878")
        ));

        list.add(TalentNodeDef.of(
                "22851", 5214.31f, -8285.08f,
                "cold_and_lightning_damage", "passives_monkelementalchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08)
                ),
                List.of("50268", "63400")
        ));

        list.add(TalentNodeDef.of(
                "50268", 5072.29f, -8203.08f,
                "cold_and_lightning_damage", "passives_monkelementalchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08)
                ),
                List.of("22851", "2446")
        ));

        list.add(TalentNodeDef.of(
                "12099", -5263.61f, -9401.91f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("44191", "4833")
        ));

        list.add(TalentNodeDef.of(
                "13333", -1243.05f, -9989.29f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("1091", "1700", "45497")
        ));

        list.add(TalentNodeDef.of(
                "14310", 5569.62f, -8962.42f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("32340", "45377")
        ));

        list.add(TalentNodeDef.of(
                "1700", -1161.05f, -9825.29f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("13333", "48699")
        ));

        list.add(TalentNodeDef.of(
                "19003", 6564.44f, -7691.65f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("12166", "22713", "3128")
        ));

        list.add(TalentNodeDef.of(
                "22713", 6814.29f, -7691.65f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("14446", "19003", "19722", "4959")
        ));

        list.add(TalentNodeDef.of(
                "23455", 3668.09f, -8216.37f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("47754", "49740", "55847")
        ));

        list.add(TalentNodeDef.of(
                "32340", 5818.69f, -9005.17f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("14310", "24721")
        ));

        list.add(TalentNodeDef.of(
                "35387", -612.835f, -7781.69f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("49512", "6554")
        ));

        list.add(TalentNodeDef.of(
                "37415", -6394.63f, -4266.42f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("51328", "8553")
        ));

        list.add(TalentNodeDef.of(
                "41669", 0.435f, -11118.5f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("29369", "44455", "55250", "59362")
        ));

        list.add(TalentNodeDef.of(
                "43653", -7134.25f, -1100.19f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("26518", "48171")
        ));

        list.add(TalentNodeDef.of(
                "44191", -4971.36f, -9503.08f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("12099", "50104")
        ));

        list.add(TalentNodeDef.of(
                "44455", 0.435f, -11452.5f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("11337", "41669", "52799", "56649", "60515", "62914")
        ));

        list.add(TalentNodeDef.of(
                "44560", -3604.52f, -11522.1f,
                "cold_damage", "passives_colddamagenode",
                List.of(),
                List.of("12940", "25570")
        ));

        list.add(TalentNodeDef.of(
                "45377", 5901.0f, -8772.67f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("14310", "61403")
        ));

        list.add(TalentNodeDef.of(
                "47754", 3451.59f, -8091.37f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("23455", "55668")
        ));

        list.add(TalentNodeDef.of(
                "48171", -7037.04f, -961.363f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("3218", "43653")
        ));

        list.add(TalentNodeDef.of(
                "4833", -5147.0f, -9203.79f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("12099", "33852")
        ));

        list.add(TalentNodeDef.of(
                "50121", 5988.31f, -7475.15f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.1)
                ),
                List.of("3128", "56914")
        ));

        list.add(TalentNodeDef.of(
                "51328", -6534.71f, -4055.73f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("37415", "49938")
        ));

        list.add(TalentNodeDef.of(
                "60515", 289.688f, -11619.5f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("19955", "41972", "44455")
        ));

        list.add(TalentNodeDef.of(
                "62914", -288.817f, -11619.5f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("23427", "44455", "47270")
        ));

        list.add(TalentNodeDef.of(
                "6554", -477.995f, -7622.1f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("23907", "35387")
        ));

        list.add(TalentNodeDef.of(
                "8553", -6788.28f, -4451.88f,
                "cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.12)
                ),
                List.of("13693", "37415")
        ));

        list.add(TalentNodeDef.of(
                "52537", 9547.88f, -3320.26f,
                "cold_penetration", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.1)
                ),
                List.of("44891", "55835")
        ));

        list.add(TalentNodeDef.of(
                "20909", 9632.77f, -3637.09f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("44891", "55835")
        ));

        list.add(TalentNodeDef.of(
                "28086", 6469.51f, 1018.9f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("144", "57088")
        ));

        list.add(TalentNodeDef.of(
                "30959", -7465.29f, -6893.12f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("296", "31778")
        ));

        list.add(TalentNodeDef.of(
                "31778", -7301.29f, -6893.12f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("2344", "30959")
        ));

        list.add(TalentNodeDef.of(
                "32427", -1113.03f, -5546.35f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("4456", "62844")
        ));

        list.add(TalentNodeDef.of(
                "44891", 9748.74f, -3436.23f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("15775", "20909", "52537")
        ));

        list.add(TalentNodeDef.of(
                "4806", 7602.58f, -764.777f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("32183", "336", "49388")
        ));

        list.add(TalentNodeDef.of(
                "54557", 6633.51f, 734.847f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("57088", "9421")
        ));

        list.add(TalentNodeDef.of(
                "55250", 244.435f, -11029.8f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("41669", "56649")
        ));

        list.add(TalentNodeDef.of(
                "56649", 289.687f, -11285.5f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("41972", "44455", "55250")
        ));

        list.add(TalentNodeDef.of(
                "57088", 6409.49f, 794.875f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("28086", "54557")
        ));

        list.add(TalentNodeDef.of(
                "62844", -1283.03f, -5546.35f,
                "cold_penetration", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.06)
                ),
                List.of("18086", "32427")
        ));

        list.add(TalentNodeDef.of(
                "35404", -9610.55f, -2272.29f,
                "cold_resistance", "passives_coldresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05)
                ),
                List.of("43250", "8421")
        ));

        list.add(TalentNodeDef.of(
                "41414", -7677.07f, 7960.8f,
                "cold_resistance", "passives_coldresist",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05)
                ),
                List.of("4547", "62518")
        ));

        list.add(TalentNodeDef.of(
                "8421", -9817.55f, -2197.81f,
                "cold_resistance", "passives_coldresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05)
                ),
                List.of("35404", "53123")
        ));

        list.add(TalentNodeDef.of(
                "37780", 10186.8f, -1550.22f,
                "combo_gain", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.1)
                ),
                List.of("3640", "7888")
        ));

        list.add(TalentNodeDef.of(
                "7888", 9864.22f, -1463.78f,
                "combo_gain", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.1)
                ),
                List.of("17101", "28963", "37780")
        ));

        list.add(TalentNodeDef.of(
                "22133", -5214.85f, -1915.04f,
                "command_cooldown", "passives_miniondamageblue",
                List.of(),
                List.of("39857", "761")
        ));

        list.add(TalentNodeDef.of(
                "35645", -3915.69f, -7172.57f,
                "command_cooldown", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.2)
                ),
                List.of("6077")
        ));

        list.add(TalentNodeDef.of(
                "6077", -3709.27f, -7173.96f,
                "command_cooldown", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.2)
                ),
                List.of("35645", "8737")
        ));

        list.add(TalentNodeDef.of(
                "761", -5431.35f, -1540.04f,
                "command_cooldown", "passives_miniondamageblue",
                List.of(),
                List.of("22133", "51052")
        ));

        list.add(TalentNodeDef.of(
                "25927", -3622.31f, -7106.13f,
                "command_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, 0.2)
                ),
                List.of("32847", "8737")
        ));

        list.add(TalentNodeDef.of(
                "32847", -3856.25f, -7033.48f,
                "command_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, 0.2)
                ),
                List.of("25927")
        ));

        list.add(TalentNodeDef.of(
                "41511", -3445.11f, -7610.06f,
                "command_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, 0.15)
                ),
                List.of("35560", "8737")
        ));

        list.add(TalentNodeDef.of(
                "44684", 5328.27f, 10118.5f,
                "companion_attack_speed", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_ATTACK_SPEED, 0.1)
                ),
                List.of("28142", "5191")
        ));

        list.add(TalentNodeDef.of(
                "39495", 11053.8f, 2092.2f,
                "companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("27048", "27705", "46386")
        ));

        list.add(TalentNodeDef.of(
                "39986", 11330.5f, 1564.9f,
                "companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("46386", "57933")
        ));

        list.add(TalentNodeDef.of(
                "44309", -11625.3f, -5058.29f,
                "companion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("36025", "44485")
        ));

        list.add(TalentNodeDef.of(
                "44485", -11960.3f, -5117.35f,
                "companion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("44309", "7553")
        ));

        list.add(TalentNodeDef.of(
                "46386", 11403.3f, 1937.84f,
                "companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("39495", "39986")
        ));

        list.add(TalentNodeDef.of(
                "57933", 11768.8f, 1960.45f,
                "companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("16150", "39986")
        ));

        list.add(TalentNodeDef.of(
                "29930", 3395.61f, 6699.03f,
                "companion_damage_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("16938", "22368", "32943", "37266", "47088", "63182")
        ));

        list.add(TalentNodeDef.of(
                "8789", 3395.61f, 7027.03f,
                "companion_damage_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("16938", "17118", "63182")
        ));

        list.add(TalentNodeDef.of(
                "44699", 12345.7f, 2292.24f,
                "companion_reservation", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, 0.08)
                ),
                List.of("10315", "16150")
        ));

        list.add(TalentNodeDef.of(
                "47443", 12118.1f, 2530.75f,
                "companion_reservation", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, 0.08)
                ),
                List.of("10315", "31129")
        ));
        return list;
    }
}
