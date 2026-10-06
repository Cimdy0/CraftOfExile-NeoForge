package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes16 {

    private MinorNodes16() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "43366", -3061.21f, -8608.16f,
                "minion_physical_damage_reduction", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.12)
                ),
                List.of("2606", "4407")
        ));

        list.add(TalentNodeDef.of(
                "4407", -3069.12f, -8364.27f,
                "minion_physical_damage_reduction", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.12),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_EVASION, 0.25)
                ),
                List.of("43366")
        ));

        list.add(TalentNodeDef.of(
                "22331", -4201.82f, -11017.0f,
                "minion_resistances", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.08)
                ),
                List.of("40200", "8983")
        ));

        list.add(TalentNodeDef.of(
                "4271", 2249.33f, -9849.58f,
                "minion_resistances", "passives_minionelementalresistancesnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.08)
                ),
                List.of("33225", "48611", "61768", "62887", "63926")
        ));

        list.add(TalentNodeDef.of(
                "48611", 2107.3f, -10095.6f,
                "minion_resistances", "passives_minionelementalresistancesnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.08)
                ),
                List.of("4271", "46554")
        ));

        list.add(TalentNodeDef.of(
                "28573", 2415.23f, -6650.5f,
                "minion_revive_speed", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER, 0.05)
                ),
                List.of("44255", "59541")
        ));

        list.add(TalentNodeDef.of(
                "44255", 2165.23f, -6400.5f,
                "minion_revive_speed", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER, 0.05)
                ),
                List.of("15358", "28573", "41905")
        ));

        list.add(TalentNodeDef.of(
                "17394", 1798.44f, -7416.71f,
                "minion_stun_buildup", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_CAUSE_INC_STUN_BUILDUP, 0.15)
                ),
                List.of("45530", "47155", "49088")
        ));

        list.add(TalentNodeDef.of(
                "45530", 1548.44f, -7166.71f,
                "minion_stun_buildup", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_CAUSE_INC_STUN_BUILDUP, 0.15)
                ),
                List.of("17394", "55180")
        ));

        list.add(TalentNodeDef.of(
                "16620", -7305.17f, 6383.32f,
                "movement_penalty_with_raised_shield", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING, -0.1)
                ),
                List.of("21161", "58088")
        ));

        list.add(TalentNodeDef.of(
                "21161", -7149.56f, 6180.51f,
                "movement_penalty_with_raised_shield", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING, -0.1)
                ),
                List.of("10500", "16620")
        ));

        list.add(TalentNodeDef.of(
                "11464", -7502.73f, -1193.34f,
                "movement_speed", "passives_movementspeedandevasion",
                List.of(),
                List.of("27405", "38707")
        ));

        list.add(TalentNodeDef.of(
                "17045", -2553.42f, 8874.5f,
                "movement_speed", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("37519", "55131")
        ));

        list.add(TalentNodeDef.of(
                "29285", 7093.09f, 8212.83f,
                "movement_speed", "passives_azmerisacredrabbit",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("16602", "38463", "9745")
        ));

        list.add(TalentNodeDef.of(
                "30781", -6997.73f, -1193.34f,
                "movement_speed", "passives_movementspeedandevasion",
                List.of(),
                List.of("27405", "63772")
        ));

        list.add(TalentNodeDef.of(
                "37519", -2434.42f, 8668.39f,
                "movement_speed", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("17045", "32564")
        ));

        list.add(TalentNodeDef.of(
                "40341", 599.155f, 7972.57f,
                "movement_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY, 0.03)
                ),
                List.of("17340", "21274")
        ));

        list.add(TalentNodeDef.of(
                "59653", 1884.97f, 1536.95f,
                "movement_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("35987", "61106")
        ));

        list.add(TalentNodeDef.of(
                "61106", 1644.01f, 1436.41f,
                "movement_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("49691", "59653")
        ));

        list.add(TalentNodeDef.of(
                "62051", 265.155f, 7638.57f,
                "movement_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY, 0.03)
                ),
                List.of("17340", "21755")
        ));

        list.add(TalentNodeDef.of(
                "9745", 7334.57f, 8527.54f,
                "movement_speed", "passives_azmerisacredrabbit",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02)
                ),
                List.of("29285", "58513")
        ));

        list.add(TalentNodeDef.of(
                "32564", -2309.42f, 8451.88f,
                "movement_speed_and_slow_effect_on_you", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.01),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.04)
                ),
                List.of("2864", "37519", "39207")
        ));

        list.add(TalentNodeDef.of(
                "37164", 5072.29f, -8039.08f,
                "non_damaging_ailment_magnitude", "passives_monkelementalchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT, 0.1)
                ),
                List.of("2446", "419")
        ));

        list.add(TalentNodeDef.of(
                "419", 5214.31f, -7957.08f,
                "non_damaging_ailment_magnitude", "passives_monkelementalchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT, 0.1)
                ),
                List.of("37164", "63400")
        ));

        list.add(TalentNodeDef.of(
                "43713", 452.015f, -9772.2f,
                "offering_area", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.2)
                ),
                List.of("27009", "3051", "35602", "45570")
        ));

        list.add(TalentNodeDef.of(
                "45570", 202.015f, -9772.2f,
                "offering_area", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.2)
                ),
                List.of("29009", "43713")
        ));

        list.add(TalentNodeDef.of(
                "13634", 612.035f, -7570.94f,
                "offering_duration", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_DURATION, 0.3)
                ),
                List.of("34030")
        ));

        list.add(TalentNodeDef.of(
                "42614", 940.035f, -7570.94f,
                "offering_duration", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_DURATION, -0.3)
                ),
                List.of("34030")
        ));

        list.add(TalentNodeDef.of(
                "59651", 4231.22f, -7650.75f,
                "offering_duration", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_DURATION, 0.2)
                ),
                List.of("25557", "41654", "47821")
        ));

        list.add(TalentNodeDef.of(
                "47821", 4030.36f, -7766.72f,
                "offering_effect", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_BUFF_EFFECT, 0.15)
                ),
                List.of("41033", "59651")
        ));

        list.add(TalentNodeDef.of(
                "55872", 4102.33f, -8131.78f,
                "offering_effect", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERING_SKILLS_HAVE_INC_BUFF_EFFECT, 0.15)
                ),
                List.of("41033")
        ));

        list.add(TalentNodeDef.of(
                "25594", 776.035f, -7820.94f,
                "offering_life", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, 0.15)
                ),
                List.of("3025", "34030", "38732")
        ));

        list.add(TalentNodeDef.of(
                "3051", 452.015f, -9936.2f,
                "offering_life", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, 0.3)
                ),
                List.of("43713")
        ));

        list.add(TalentNodeDef.of(
                "34030", 776.035f, -7570.94f,
                "offering_life", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, 0.15)
                ),
                List.of("13634", "25594", "42614", "47441")
        ));

        list.add(TalentNodeDef.of(
                "35602", 452.015f, -9608.2f,
                "offering_life", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, -0.3)
                ),
                List.of("43713")
        ));

        list.add(TalentNodeDef.of(
                "41654", 4347.19f, -7851.61f,
                "offering_life", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.OFFERINGS_HAVE_INC_MAX_HEALTH, 0.15)
                ),
                List.of("41033", "59651")
        ));

        list.add(TalentNodeDef.of(
                "60173", 5408.88f, 8022.08f,
                "one_handed_accuracy", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, 0.12)
                ),
                List.of("4238", "62986")
        ));

        list.add(TalentNodeDef.of(
                "62986", 5335.65f, 8198.85f,
                "one_handed_accuracy", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, 0.12)
                ),
                List.of("17316", "60173")
        ));

        list.add(TalentNodeDef.of(
                "47733", 419.127f, 6608.18f,
                "one_handed_ailment_chance", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_INC_CHANCE_TO_INFLICT_AILMENTS, 0.15)
                ),
                List.of("9217")
        ));

        list.add(TalentNodeDef.of(
                "5108", 144.75f, 6449.77f,
                "one_handed_ailment_chance", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_INC_CHANCE_TO_INFLICT_AILMENTS, 0.15)
                ),
                List.of("9217")
        ));

        list.add(TalentNodeDef.of(
                "40244", 5369.15f, 8323.85f,
                "one_handed_attack_speed", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS, 0.03)
                ),
                List.of("17316", "43263")
        ));

        list.add(TalentNodeDef.of(
                "43263", 5520.95f, 8440.34f,
                "one_handed_attack_speed", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS, 0.03)
                ),
                List.of("40244", "64492")
        ));

        list.add(TalentNodeDef.of(
                "46688", 5650.36f, 7957.37f,
                "one_handed_attack_speed", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS, 0.03)
                ),
                List.of("4238", "64492")
        ));

        list.add(TalentNodeDef.of(
                "64492", 5762.43f, 8375.63f,
                "one_handed_attack_speed", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_MELEE_WEAPONS, 0.03)
                ),
                List.of("43263", "46688")
        ));

        list.add(TalentNodeDef.of(
                "12751", -4182.77f, 8640.88f,
                "one_handed_critical_chance", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_ONE_HANDED_MELEE_WEAPONS, 0.1)
                ),
                List.of("18489", "26490")
        ));

        list.add(TalentNodeDef.of(
                "26490", -3950.84f, 8640.88f,
                "one_handed_critical_chance", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_ONE_HANDED_MELEE_WEAPONS, 0.1)
                ),
                List.of("12751", "6229")
        ));

        list.add(TalentNodeDef.of(
                "13356", -4066.8f, 8360.92f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("18489", "6229")
        ));

        list.add(TalentNodeDef.of(
                "13474", -5314.13f, 2829.25f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("16084", "54811")
        ));

        list.add(TalentNodeDef.of(
                "16084", -5314.13f, 2655.72f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("13474", "57552")
        ));

        list.add(TalentNodeDef.of(
                "16538", 342.715f, 6423.71f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("44330", "9217")
        ));

        list.add(TalentNodeDef.of(
                "17316", 5114.28f, 8325.16f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("34201", "40244", "62986")
        ));

        list.add(TalentNodeDef.of(
                "18489", -4308.29f, 8460.21f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("12751", "13356", "37258")
        ));

        list.add(TalentNodeDef.of(
                "57552", -5314.13f, 2434.83f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("16084", "24871", "46696")
        ));

        list.add(TalentNodeDef.of(
                "61281", 178.715f, 6707.76f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("21755", "9217")
        ));

        list.add(TalentNodeDef.of(
                "9217", 260.715f, 6565.73f,
                "one_handed_damage", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_ONE_HANDED_WEAPONS, 0.1)
                ),
                List.of("16538", "47733", "5108", "61281")
        ));

        list.add(TalentNodeDef.of(
                "29246", 9993.74f, 5617.1f,
                "parried_debuff_magnitude", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("22927", "35043")
        ));

        list.add(TalentNodeDef.of(
                "35043", 10080.5f, 5904.44f,
                "parried_debuff_magnitude", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("20414", "29246")
        ));

        list.add(TalentNodeDef.of(
                "22927", 10127.1f, 5443.33f,
                "parried_debuff_magnitude_and_duration", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_DURATION, 0.08)
                ),
                List.of("20582", "29246", "34015")
        ));

        list.add(TalentNodeDef.of(
                "20582", 10210.9f, 5240.98f,
                "parried_duration", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_DURATION, 0.15)
                ),
                List.of("22927", "55329")
        ));

        list.add(TalentNodeDef.of(
                "55329", 10503.1f, 5172.44f,
                "parried_duration", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_DURATION, 0.15)
                ),
                List.of("20582", "42036")
        ));

        list.add(TalentNodeDef.of(
                "12116", 10539.1f, 5583.35f,
                "parry_area", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("42036", "52410")
        ));

        list.add(TalentNodeDef.of(
                "5077", 3649.93f, 5378.57f,
                "parry_area", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, 0.15)
                ),
                List.of("33400", "9441")
        ));

        list.add(TalentNodeDef.of(
                "9441", 3649.93f, 5542.57f,
                "parry_area", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, 0.15)
                ),
                List.of("41770", "5077")
        ));

        list.add(TalentNodeDef.of(
                "41770", 3465.46f, 5618.99f,
                "parry_area_and_debuff_magnitude", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, 0.08)
                ),
                List.of("16484", "33080", "9441")
        ));

        list.add(TalentNodeDef.of(
                "17146", 7963.91f, 3127.84f,
                "parry_damage", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_DAMAGE, 0.2)
                ),
                List.of("3843", "57518", "65265")
        ));

        list.add(TalentNodeDef.of(
                "49545", 4321.54f, 2202.17f,
                "parry_damage", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_DAMAGE, 0.2)
                ),
                List.of("18831", "64851")
        ));

        list.add(TalentNodeDef.of(
                "57518", 8079.88f, 2926.98f,
                "parry_damage", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_DAMAGE, 0.2)
                ),
                List.of("17146", "31366", "42118")
        ));

        list.add(TalentNodeDef.of(
                "33080", 3343.9f, 5460.57f,
                "parry_debuff_magnitude", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("41770", "43254")
        ));

        list.add(TalentNodeDef.of(
                "43254", 3425.9f, 5318.55f,
                "parry_debuff_magnitude", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRIED_DEBUFF_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("33080", "33400")
        ));

        list.add(TalentNodeDef.of(
                "31366", 8280.74f, 3042.95f,
                "parry_stun_buildup", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.PARRY_HAS_INC_STUN_BUILDUP, 0.2)
                ),
                List.of("3843", "57518", "65265")
        ));

        list.add(TalentNodeDef.of(
                "3843", 8164.77f, 3243.81f,
                "parry_stun_buildup", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.PARRY_HAS_INC_STUN_BUILDUP, 0.25)
                ),
                List.of("17146", "31366", "65265")
        ));

        list.add(TalentNodeDef.of(
                "31286", 8114.88f, -2220.46f,
                "physical", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("1599", "16140", "33463")
        ));

        list.add(TalentNodeDef.of(
                "18353", -11017.2f, 962.236f,
                "physical_and_lightning_damage", "passives_lightningdamagenode",
                List.of(),
                List.of("1200", "20496", "3544")
        ));

        list.add(TalentNodeDef.of(
                "45086", 2474.92f, -7440.12f,
                "physical_as_extra_chaos_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.03)
                ),
                List.of("34520", "58002")
        ));

        list.add(TalentNodeDef.of(
                "55575", 2706.85f, -7208.19f,
                "physical_as_extra_chaos_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.03)
                ),
                List.of("34520", "58002")
        ));

        list.add(TalentNodeDef.of(
                "37644", 4480.65f, 2899.72f,
                "physical_attack_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("37389", "46874")
        ));

        list.add(TalentNodeDef.of(
                "46874", 4303.88f, 2972.95f,
                "physical_attack_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("37644", "7449")
        ));

        list.add(TalentNodeDef.of(
                "11984", -11563.5f, 416.799f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(),
                List.of("20496", "42762")
        ));

        list.add(TalentNodeDef.of(
                "12851", -1440.86f, -9211.96f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("32507", "32727", "52973")
        ));

        list.add(TalentNodeDef.of(
                "14082", 5688.65f, -8320.56f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("43964", "6490", "8831")
        ));

        list.add(TalentNodeDef.of(
                "19426", 3695.58f, -5536.76f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("15443", "50574")
        ));

        list.add(TalentNodeDef.of(
                "20496", -11509.4f, 816.545f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(),
                List.of("11984", "18353")
        ));

        list.add(TalentNodeDef.of(
                "21549", -4063.09f, -11525.0f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(),
                List.of("12940", "25570")
        ));

        list.add(TalentNodeDef.of(
                "23993", -2012.03f, 4914.59f,
                "physical_damage", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("14091", "42981", "52684")
        ));

        list.add(TalentNodeDef.of(
                "3027", -8197.09f, -297.283f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("54228", "60064")
        ));

        list.add(TalentNodeDef.of(
                "32745", -4116.33f, -9838.01f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("40276", "50104", "61179")
        ));

        list.add(TalentNodeDef.of(
                "34143", -9063.01f, -7879.35f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(),
                List.of("19162", "36408")
        ));

        list.add(TalentNodeDef.of(
                "34415", -5455.64f, -5022.59f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("48387", "55422")
        ));

        list.add(TalentNodeDef.of(
                "34520", 2414.11f, -7147.38f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("14548", "23738", "45086", "55575", "63545")
        ));

        list.add(TalentNodeDef.of(
                "37608", -2153.72f, -9020.95f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("53524", "57555", "61042")
        ));

        list.add(TalentNodeDef.of(
                "39307", 9588.39f, -133.505f,
                "physical_damage", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("37026", "7294")
        ));

        list.add(TalentNodeDef.of(
                "41645", 5182.89f, -8612.56f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("10382", "43964", "6490")
        ));

        list.add(TalentNodeDef.of(
                "42762", -12243.2f, 536.64f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(),
                List.of("11984", "58197")
        ));

        list.add(TalentNodeDef.of(
                "48387", -5702.91f, -4856.77f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("11248", "34415")
        ));

        list.add(TalentNodeDef.of(
                "52695", 6237.11f, -3805.06f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("25851", "57230", "60138")
        ));

        list.add(TalentNodeDef.of(
                "54228", -8033.09f, -297.283f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("3027", "64240")
        ));

        list.add(TalentNodeDef.of(
                "55422", -5378.51f, -4656.24f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.12)
                ),
                List.of("34415", "54640")
        ));

        list.add(TalentNodeDef.of(
                "57230", 6591.9f, -4290.17f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("25851", "36270", "51741", "52695")
        ));

        list.add(TalentNodeDef.of(
                "58002", 2827.06f, -7560.33f,
                "physical_damage", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("45086", "55575", "55668")
        ));

        list.add(TalentNodeDef.of(
                "60064", -8279.09f, -155.255f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("3027", "47263")
        ));

        list.add(TalentNodeDef.of(
                "6490", 5472.15f, -8281.56f,
                "physical_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.1)
                ),
                List.of("14082", "41645")
        ));

        list.add(TalentNodeDef.of(
                "27999", -4094.99f, 10911.5f,
                "physical_damage_and_ailment_chance", "passives_physicaldamagechaosnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS, 0.08),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("17417", "45777")
        ));

        list.add(TalentNodeDef.of(
                "1599", 8117.88f, -2509.71f,
                "physical_damage_and_critical_chance", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("31286", "35173")
        ));

        list.add(TalentNodeDef.of(
                "35173", 8156.88f, -2726.22f,
                "physical_damage_and_critical_chance", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("1599", "39881", "41811")
        ));

        list.add(TalentNodeDef.of(
                "19162", -9303.49f, -7638.88f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(),
                List.of("15141", "21809", "31554", "34143")
        ));

        list.add(TalentNodeDef.of(
                "21809", -9391.51f, -7310.38f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(),
                List.of("19162", "44452")
        ));

        list.add(TalentNodeDef.of(
                "27900", -4814.41f, -4807.39f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.04),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("54640", "6356")
        ));

        list.add(TalentNodeDef.of(
                "44452", -9303.49f, -6981.88f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(),
                List.of("21809", "61811")
        ));

        list.add(TalentNodeDef.of(
                "61811", -9063.01f, -6741.4f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(),
                List.of("33722", "44452")
        ));

        list.add(TalentNodeDef.of(
                "6356", -4594.84f, -4717.02f,
                "physical_damage_and_increased_duration", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.04),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("15114", "27900")
        ));

        list.add(TalentNodeDef.of(
                "50574", 3411.53f, -5536.76f,
                "physical_damage_and_life_recoup", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.05)
                ),
                List.of("19426", "31928", "46034")
        ));

        list.add(TalentNodeDef.of(
                "62376", -4879.12f, -4565.91f,
                "physical_damage_and_reduced_duration", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.04),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("54640", "71")
        ));

        list.add(TalentNodeDef.of(
                "71", -4659.55f, -4475.54f,
                "physical_damage_and_reduced_duration", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.04),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("15114", "62376")
        ));

        list.add(TalentNodeDef.of(
                "31928", 3553.55f, -5290.76f,
                "physical_life_recoup", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.06)
                ),
                List.of("15443", "50574")
        ));

        list.add(TalentNodeDef.of(
                "31918", 7169.05f, 2252.47f,
                "pierce_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.15)
                ),
                List.of("33245", "42302", "4534", "60323")
        ));

        list.add(TalentNodeDef.of(
                "51728", 699.787f, 9607.14f,
                "pierce_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.15)
                ),
                List.of("51944", "6505")
        ));

        list.add(TalentNodeDef.of(
                "6505", 830.628f, 9397.7f,
                "pierce_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, 0.15)
                ),
                List.of("51728", "55060")
        ));

        list.add(TalentNodeDef.of(
                "18470", 2827.49f, 5953.5f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("3624", "4447")
        ));

        list.add(TalentNodeDef.of(
                "19129", -713.709f, 10205.8f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("55066", "62194", "9290")
        ));

        list.add(TalentNodeDef.of(
                "3624", 3079.09f, 5826.05f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("10927", "16484", "18470")
        ));

        list.add(TalentNodeDef.of(
                "36449", -1762.03f, 4750.59f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("14091", "8115")
        ));

        list.add(TalentNodeDef.of(
                "44239", 4115.96f, -146.815f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("29479", "60083")
        ));

        list.add(TalentNodeDef.of(
                "55270", 4397.17f, -513.296f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("43691", "60083")
        ));

        list.add(TalentNodeDef.of(
                "62194", -477.535f, 10107.9f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("19129", "21721")
        ));

        list.add(TalentNodeDef.of(
                "8115", -1722.77f, 4917.59f,
                "pin_buildup", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_BUILDUP, 0.15)
                ),
                List.of("36449", "52684")
        ));

        list.add(TalentNodeDef.of(
                "10927", 2992.65f, 5676.32f,
                "pin_duration", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_DURATION, 0.1)
                ),
                List.of("3624", "42250", "9272")
        ));

        list.add(TalentNodeDef.of(
                "3234", 2685.46f, 5953.5f,
                "pin_duration", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_DURATION, 0.15)
                ),
                List.of("12906", "31745", "4447")
        ));

        list.add(TalentNodeDef.of(
                "9272", 2756.47f, 5830.5f,
                "pin_duration", "passives_increasedprojectilespeednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PINNED_DURATION, 0.1)
                ),
                List.of("10927", "12906")
        ));

        list.add(TalentNodeDef.of(
                "12189", -3931.92f, -4582.78f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("32859", "56547")
        ));

        list.add(TalentNodeDef.of(
                "16721", -6348.3f, -6484.17f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("18270", "20848", "32239", "34187")
        ));

        list.add(TalentNodeDef.of(
                "18270", -7002.8f, -6541.43f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("16721", "45874")
        ));

        list.add(TalentNodeDef.of(
                "20848", -6131.8f, -6359.17f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("16721", "46628")
        ));

        list.add(TalentNodeDef.of(
                "32239", -6261.86f, -6806.78f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("16721", "9009")
        ));

        list.add(TalentNodeDef.of(
                "32859", -4181.89f, -4304.07f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("12189", "15114")
        ));

        list.add(TalentNodeDef.of(
                "34187", -6770.93f, -6728.17f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("16721", "7128")
        ));

        list.add(TalentNodeDef.of(
                "56547", -3783.45f, -4370.2f,
                "plant_damage", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_PLANT_SKILLS, 0.12)
                ),
                List.of("12189", "50423")
        ));

        list.add(TalentNodeDef.of(
                "2091", 6847.19f, 2826.09f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("12890", "40024")
        ));

        list.add(TalentNodeDef.of(
                "22517", 7088.65f, 263.165f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("14658", "32896", "59644")
        ));

        list.add(TalentNodeDef.of(
                "23374", 11480.8f, -1058.34f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("2500", "28976")
        ));

        list.add(TalentNodeDef.of(
                "2500", 11480.4f, -1298.86f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("23374", "6030")
        ));

        list.add(TalentNodeDef.of(
                "25337", -5102.13f, -5876.71f,
                "poison_chance", "passives_poison",
                List.of(),
                List.of("34990", "46628")
        ));

        list.add(TalentNodeDef.of(
                "32896", 7265.43f, 689.942f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("22517", "42959")
        ));

        list.add(TalentNodeDef.of(
                "34990", -5002.67f, -5894.0f,
                "poison_chance", "passives_poison",
                List.of(),
                List.of("25337", "6088")
        ));

        list.add(TalentNodeDef.of(
                "40024", 6575.69f, 2701.09f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("2091", "26565", "61741")
        ));

        list.add(TalentNodeDef.of(
                "6030", 11678.0f, -1601.81f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("15986", "2500", "9703")
        ));

        list.add(TalentNodeDef.of(
                "6078", 9026.76f, -2196.3f,
                "poison_chance", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.08)
                ),
                List.of("45304", "61119")
        ));

        list.add(TalentNodeDef.of(
                "14882", 9399.45f, 5859.96f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("34015", "5048")
        ));

        list.add(TalentNodeDef.of(
                "23608", 6142.68f, 2701.09f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("24401", "61741", "6951")
        ));

        list.add(TalentNodeDef.of(
                "29458", 11230.4f, -1298.86f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("28976", "53595")
        ));

        list.add(TalentNodeDef.of(
                "35644", 4518.86f, 952.616f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("33815", "45193", "65009")
        ));

        list.add(TalentNodeDef.of(
                "4083", 4647.75f, 1433.65f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("33815", "43677", "45193")
        ));

        list.add(TalentNodeDef.of(
                "45193", 4446.89f, 1317.68f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("35644", "4083")
        ));

        list.add(TalentNodeDef.of(
                "46069", -4860.65f, -5976.0f,
                "poison_damage", "passives_poison",
                List.of(),
                List.of("50184", "6088")
        ));

        list.add(TalentNodeDef.of(
                "50184", -4795.94f, -6053.49f,
                "poison_damage", "passives_poison",
                List.of(),
                List.of("46069", "46628")
        ));
        return list;
    }
}
