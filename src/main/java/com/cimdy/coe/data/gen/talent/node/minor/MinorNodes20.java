package com.cimdy.coe.data.gen.talent.node.minor;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinorNodes20 {

    private MinorNodes20() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "52410", 10454.3f, 5730.12f,
                "stun_threshold_during_parry", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_WHILE_PARRYING, 0.2)
                ),
                List.of("12116", "20414")
        ));

        list.add(TalentNodeDef.of(
                "10552", 3003.9f, -5775.44f,
                "stun_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("18895", "46034", "703")
        ));

        list.add(TalentNodeDef.of(
                "18895", 2787.39f, -5818.44f,
                "stun_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("10552", "12661")
        ));

        list.add(TalentNodeDef.of(
                "59799", 6773.86f, -9198.81f,
                "stun_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("17602", "7338")
        ));

        list.add(TalentNodeDef.of(
                "703", 2858.41f, -5941.44f,
                "stun_threshold_from_energy", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("10552", "44917")
        ));

        list.add(TalentNodeDef.of(
                "20205", 2335.02f, 4635.44f,
                "stun_threshold_if_no_recent_stun", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.25)
                ),
                List.of("33059", "39839")
        ));

        list.add(TalentNodeDef.of(
                "35058", 4625.34f, 6875.05f,
                "stun_threshold_if_no_recent_stun", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.25)
                ),
                List.of("38966", "64650")
        ));

        list.add(TalentNodeDef.of(
                "39839", 2050.97f, 4635.44f,
                "stun_threshold_if_no_recent_stun", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.25)
                ),
                List.of("14340", "20205")
        ));

        list.add(TalentNodeDef.of(
                "10677", 2744.42f, -3512.12f,
                "stun_threshold_if_not_stunned_recently", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.25)
                ),
                List.of("53935", "56638")
        ));

        list.add(TalentNodeDef.of(
                "56638", 2968.44f, -3572.15f,
                "stun_threshold_if_not_stunned_recently", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY, 0.25)
                ),
                List.of("10677", "58329")
        ));

        list.add(TalentNodeDef.of(
                "32319", 11851.7f, 1038.8f,
                "surpassing_arrow_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, 0.1)
                ),
                List.of("33542", "57615")
        ));

        list.add(TalentNodeDef.of(
                "48836", 11851.7f, 1288.81f,
                "surpassing_arrow_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, 0.1)
                ),
                List.of("33542", "57615")
        ));

        list.add(TalentNodeDef.of(
                "52800", 11229.2f, 1163.81f,
                "surpassing_arrow_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, 0.08)
                ),
                List.of("48773", "57615")
        ));

        list.add(TalentNodeDef.of(
                "57615", 11580.2f, 1163.81f,
                "surpassing_arrow_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, 0.08)
                ),
                List.of("32319", "48836", "52800")
        ));

        list.add(TalentNodeDef.of(
                "60323", 6506.82f, 2075.69f,
                "surpassing_projectile_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE, 0.08)
                ),
                List.of("31918", "45331", "47560", "9199")
        ));

        list.add(TalentNodeDef.of(
                "9199", 6688.82f, 2075.69f,
                "surpassing_projectile_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE, 0.08)
                ),
                List.of("60323")
        ));

        list.add(TalentNodeDef.of(
                "10783", -3438.52f, 12010.0f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("38564", "9762")
        ));

        list.add(TalentNodeDef.of(
                "15855", -3592.33f, 12530.0f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("37963", "46565")
        ));

        list.add(TalentNodeDef.of(
                "20091", -3673.33f, 12466.2f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("38564", "46565")
        ));

        list.add(TalentNodeDef.of(
                "37963", -3401.12f, 12257.2f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("15855", "54138", "59263")
        ));

        list.add(TalentNodeDef.of(
                "38564", -3532.67f, 12164.1f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("10783", "20091", "27290", "54138")
        ));

        list.add(TalentNodeDef.of(
                "45824", -2787.46f, 11061.0f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("61441", "8493", "9762")
        ));

        list.add(TalentNodeDef.of(
                "9762", -2941.92f, 11167.6f,
                "sword_damage", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_SWORDS, 0.1)
                ),
                List.of("10783", "45824")
        ));

        list.add(TalentNodeDef.of(
                "54138", -3314.67f, 12098.6f,
                "sword_speed", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SWORDS, 0.03)
                ),
                List.of("37963", "38564", "61441")
        ));

        list.add(TalentNodeDef.of(
                "61441", -2833.44f, 11247.3f,
                "sword_speed", "passives_damagesword",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SWORDS, 0.03)
                ),
                List.of("45824", "54138")
        ));
        list.add(TalentNodeDef.of(
                "61525", -1245.18f, -728.895f,
                "templar", "passives_axedmgspeed",
                List.of(),
                List.of("13855", "50084")
        ));

        list.add(TalentNodeDef.of(
                "14459", -6288.69f, 278.547f,
                "thorn_critical_damage", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_CRITICAL_DAMAGE_BONUS, 0.3)
                ),
                List.of("14654", "5544")
        ));

        list.add(TalentNodeDef.of(
                "5544", -6288.69f, 562.603f,
                "thorn_critical_damage", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_CRITICAL_DAMAGE_BONUS, 0.3)
                ),
                List.of("14459", "43711")
        ));

        list.add(TalentNodeDef.of(
                "1286", -7296.92f, 2585.67f,
                "thorns", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.16)
                ),
                List.of("17903", "54701")
        ));

        list.add(TalentNodeDef.of(
                "17903", -7080.42f, 2460.67f,
                "thorns", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.16)
                ),
                List.of("1286", "40117")
        ));

        list.add(TalentNodeDef.of(
                "30007", -11113.2f, 1501.8f,
                "thorns", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.16)
                ),
                List.of("3188", "7395")
        ));

        list.add(TalentNodeDef.of(
                "30300", -10990.2f, 1288.76f,
                "thorns", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.16)
                ),
                List.of("1200", "12565", "28589")
        ));

        list.add(TalentNodeDef.of(
                "47633", -4088.15f, -871.735f,
                "thorns", "passives_thornsnode1",
                List.of(),
                List.of("57002", "62378")
        ));

        list.add(TalentNodeDef.of(
                "54701", -7513.43f, 2460.67f,
                "thorns", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.16)
                ),
                List.of("1286", "21089", "4527")
        ));

        list.add(TalentNodeDef.of(
                "62378", -4372.2f, -871.735f,
                "thorns", "passives_thornsnode1",
                List.of(),
                List.of("22558", "47633")
        ));

        list.add(TalentNodeDef.of(
                "12565", -10749.6f, 1344.34f,
                "thorns_and_block", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.1)
                ),
                List.of("30300", "3245")
        ));

        list.add(TalentNodeDef.of(
                "3245", -10913.8f, 1473.23f,
                "thorns_and_block", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.1)
                ),
                List.of("12565", "7395")
        ));

        list.add(TalentNodeDef.of(
                "18374", -3948.82f, 6318.1f,
                "thorns_and_leech", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08),
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.12)
                ),
                List.of("2964", "35265")
        ));

        list.add(TalentNodeDef.of(
                "2964", -4223.2f, 6391.62f,
                "thorns_and_leech", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08),
                        TalentNodeGen.stat(StatIds.INC_THORNS_DAMAGE, 0.12)
                ),
                List.of("17762", "18374")
        ));

        list.add(TalentNodeDef.of(
                "21089", -7513.43f, 2210.67f,
                "thorns_ignore_armor", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR, 0.25)
                ),
                List.of("31848", "54701")
        ));

        list.add(TalentNodeDef.of(
                "31848", -7296.92f, 2085.67f,
                "thorns_ignore_armor", "passives_thornsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR, 0.25)
                ),
                List.of("21089", "40117")
        ));

        list.add(TalentNodeDef.of(
                "59908", -10662.1f, -6708.99f,
                "totem_area_of_effect", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_SKILLS_USED_BY_TOTEMS, 0.1)
                ),
                List.of("36197", "45400")
        ));

        list.add(TalentNodeDef.of(
                "16051", -5985.69f, 6671.82f,
                "totem_attack_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED, 0.04)
                ),
                List.of("31650", "62609")
        ));

        list.add(TalentNodeDef.of(
                "24259", -5985.69f, 7906.57f,
                "totem_attack_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED, 0.04)
                ),
                List.of("25312", "62609")
        ));

        list.add(TalentNodeDef.of(
                "10534", -5503.44f, -663.483f,
                "totem_cast_and_attack_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED, 0.04)
                ),
                List.of("41615", "46205")
        ));

        list.add(TalentNodeDef.of(
                "33209", -5500.69f, -1002.04f,
                "totem_cast_and_attack_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED, 0.04)
                ),
                List.of("19249", "46205")
        ));

        list.add(TalentNodeDef.of(
                "33423", -10640.4f, -6144.89f,
                "totem_cast_and_attack_speed", "passives_totemandbrandlife",
                List.of(),
                List.of("27096", "65192")
        ));

        list.add(TalentNodeDef.of(
                "65192", -10489.2f, -6063.76f,
                "totem_cast_and_attack_speed", "passives_totemandbrandlife",
                List.of(),
                List.of("33423", "63170")
        ));

        list.add(TalentNodeDef.of(
                "63170", -10339.5f, -6150.2f,
                "totem_cast_and_attack_speed_and_elemental_resistance", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.01),
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED, 0.02)
                ),
                List.of("47931", "65192", "6999")
        ));

        list.add(TalentNodeDef.of(
                "22484", -7099.4f, -7455.59f,
                "totem_cast_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED, 0.04)
                ),
                List.of("14110", "5398")
        ));

        list.add(TalentNodeDef.of(
                "5398", -6764.66f, -6975.01f,
                "totem_cast_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.SPELLS_CAST_BY_TOTEMS_HAVE_INC_CAST_SPEED, 0.04)
                ),
                List.of("22484", "51820")
        ));

        list.add(TalentNodeDef.of(
                "13839", -9387.52f, 5915.46f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("58117", "65287")
        ));

        list.add(TalentNodeDef.of(
                "14110", -7016.4f, -7744.85f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("22484", "35974", "59093", "6355")
        ));

        list.add(TalentNodeDef.of(
                "2174", -5670.18f, -1002.04f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("19249", "46205")
        ));

        list.add(TalentNodeDef.of(
                "25312", -6210.4f, 7946.19f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("23667", "24259", "51561", "64405")
        ));

        list.add(TalentNodeDef.of(
                "31017", -6210.4f, 6955.19f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("26339", "31650")
        ));

        list.add(TalentNodeDef.of(
                "31650", -6210.4f, 6632.19f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("16051", "16784", "31017", "8600")
        ));

        list.add(TalentNodeDef.of(
                "38124", -6475.58f, -7141.91f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("51820", "6355")
        ));

        list.add(TalentNodeDef.of(
                "46205", -5585.44f, -855.455f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("10534", "2174", "33209", "51683")
        ));

        list.add(TalentNodeDef.of(
                "4882", -3611.26f, 3789.68f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("38172", "49642", "51921")
        ));

        list.add(TalentNodeDef.of(
                "49642", -3662.52f, 3664.16f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("4882", "51206")
        ));

        list.add(TalentNodeDef.of(
                "51206", -4049.84f, 3992.09f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("46748", "49642", "52348")
        ));

        list.add(TalentNodeDef.of(
                "51683", -5667.44f, -663.483f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("44787", "46205")
        ));

        list.add(TalentNodeDef.of(
                "58117", -9545.98f, 5849.82f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("13839", "41186", "53719", "56996")
        ));

        list.add(TalentNodeDef.of(
                "6355", -6724.4f, -7672.1f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("14110", "38124")
        ));

        list.add(TalentNodeDef.of(
                "64405", -6210.4f, 7623.19f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("25312", "26339")
        ));

        list.add(TalentNodeDef.of(
                "65287", -9552.02f, 6484.44f,
                "totem_damage", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.15)
                ),
                List.of("13839", "5580")
        ));

        list.add(TalentNodeDef.of(
                "15672", -10334.2f, -6451.08f,
                "totem_elemental_resistance", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.02)
                ),
                List.of("45400", "6999")
        ));

        list.add(TalentNodeDef.of(
                "48121", -4120.96f, 4565.35f,
                "totem_elemental_resistance", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_ELEMENTAL_RESISTANCES, 0.12)
                ),
                List.of("24438", "9417")
        ));

        list.add(TalentNodeDef.of(
                "6999", -10253.0f, -6299.93f,
                "totem_elemental_resistance", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES, 0.02)
                ),
                List.of("15672", "63170")
        ));

        list.add(TalentNodeDef.of(
                "16784", -6435.1f, 6671.82f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("18419", "31650")
        ));

        list.add(TalentNodeDef.of(
                "23667", -6435.1f, 7906.57f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("18419", "25312")
        ));

        list.add(TalentNodeDef.of(
                "35974", -6931.4f, -7597.62f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("14110", "51105")
        ));

        list.add(TalentNodeDef.of(
                "46748", -3995.5f, 4196.65f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("24438", "51206", "60568")
        ));

        list.add(TalentNodeDef.of(
                "48979", -6682.4f, -7166.34f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("51105", "51820")
        ));

        list.add(TalentNodeDef.of(
                "56996", -9589.72f, 6013.06f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("58117", "9568")
        ));

        list.add(TalentNodeDef.of(
                "9417", -4427.82f, 4646.67f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("13171", "48121", "61472")
        ));

        list.add(TalentNodeDef.of(
                "9568", -9673.58f, 6326.02f,
                "totem_life", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_HEALTH, 0.16)
                ),
                List.of("5580", "56996")
        ));

        list.add(TalentNodeDef.of(
                "13171", -4346.51f, 4339.79f,
                "totem_physical_damage_reduction", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.TOTEMS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.12)
                ),
                List.of("24438", "9417")
        ));

        list.add(TalentNodeDef.of(
                "11292", -9064.75f, 198.325f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("2575", "56757")
        ));

        list.add(TalentNodeDef.of(
                "36197", -10898.3f, -6472.82f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(),
                List.of("27096", "59908")
        ));

        list.add(TalentNodeDef.of(
                "38172", -3485.74f, 3840.94f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("4882", "60568")
        ));

        list.add(TalentNodeDef.of(
                "41186", -9716.02f, 5827.44f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("42710", "58117")
        ));

        list.add(TalentNodeDef.of(
                "41615", -5460.44f, -304.949f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("10534", "22616", "49192")
        ));

        list.add(TalentNodeDef.of(
                "42710", -9858.05f, 6402.44f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("41186", "5580")
        ));

        list.add(TalentNodeDef.of(
                "44787", -5710.44f, -304.949f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("14654", "49192", "51683")
        ));

        list.add(TalentNodeDef.of(
                "49192", -5585.44f, -271.455f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("41615", "43396", "44787")
        ));

        list.add(TalentNodeDef.of(
                "56757", -8982.75f, 504.353f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("10100", "11292")
        ));

        list.add(TalentNodeDef.of(
                "60568", -3813.67f, 4228.26f,
                "totem_placement_speed", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.2)
                ),
                List.of("38172", "46748", "52348")
        ));

        list.add(TalentNodeDef.of(
                "27834", 9310.68f, -6738.01f,
                "trap_critical_chance", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, 0.1)
                ),
                List.of("34449", "63659")
        ));

        list.add(TalentNodeDef.of(
                "33964", 9074.64f, -6974.05f,
                "trap_critical_chance", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, 0.1)
                ),
                List.of("63659", "64295")
        ));

        list.add(TalentNodeDef.of(
                "34449", 9516.92f, -6692.28f,
                "trap_critical_chance", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, 0.1)
                ),
                List.of("27834", "37688")
        ));

        list.add(TalentNodeDef.of(
                "64295", 9028.92f, -7180.28f,
                "trap_critical_chance", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_TRAPS, 0.1)
                ),
                List.of("27417", "33964")
        ));

        list.add(TalentNodeDef.of(
                "34912", 8923.65f, -6587.02f,
                "trap_damage", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_DAMAGE, 0.1)
                ),
                List.of("4664", "59538", "62496")
        ));

        list.add(TalentNodeDef.of(
                "62496", 8790.32f, -6760.78f,
                "trap_damage", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_DAMAGE, 0.1)
                ),
                List.of("34912", "8644")
        ));

        list.add(TalentNodeDef.of(
                "8644", 8706.5f, -6963.14f,
                "trap_damage", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_DAMAGE, 0.1)
                ),
                List.of("27417", "62496")
        ));

        list.add(TalentNodeDef.of(
                "43938", 9299.77f, -6369.87f,
                "trap_throw_speed", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_THROWING_SPEED, 0.06)
                ),
                List.of("37688", "4664")
        ));

        list.add(TalentNodeDef.of(
                "4664", 9097.42f, -6453.69f,
                "trap_throw_speed", "passives_trapdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TRAP_THROWING_SPEED, 0.06)
                ),
                List.of("34912", "43938")
        ));

        list.add(TalentNodeDef.of(
                "14231", 6259.88f, -9140.06f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.14)
                ),
                List.of("40453", "47359", "61403")
        ));

        list.add(TalentNodeDef.of(
                "22219", 3815.35f, -6229.58f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.14)
                ),
                List.of("41029", "47614", "52351")
        ));

        list.add(TalentNodeDef.of(
                "43281", 6147.81f, -9558.32f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.16)
                ),
                List.of("47359", "51934")
        ));

        list.add(TalentNodeDef.of(
                "47359", 6083.1f, -9316.84f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.16)
                ),
                List.of("14231", "43281")
        ));

        list.add(TalentNodeDef.of(
                "47614", 3949.85f, -6496.61f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.14)
                ),
                List.of("22219", "3688")
        ));

        list.add(TalentNodeDef.of(
                "63668", 5539.71f, -6166.69f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.14)
                ),
                List.of("4328", "9069")
        ));

        list.add(TalentNodeDef.of(
                "9069", 5392.48f, -6081.69f,
                "triggered_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE, 0.14)
                ),
                List.of("42245", "63668")
        ));

        list.add(TalentNodeDef.of(
                "14997", -376.015f, 4798.23f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("28370", "47856")
        ));

        list.add(TalentNodeDef.of(
                "26092", -5481.13f, 3810.01f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("30123", "52392")
        ));

        list.add(TalentNodeDef.of(
                "30123", -5314.13f, 3854.75f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("26092", "64939", "6923")
        ));

        list.add(TalentNodeDef.of(
                "32561", -656.299f, 5078.51f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("47856", "51825")
        ));

        list.add(TalentNodeDef.of(
                "34210", -5314.13f, 3397.64f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("54811", "64939")
        ));

        list.add(TalentNodeDef.of(
                "47856", -536.091f, 4958.3f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("14997", "32561")
        ));

        list.add(TalentNodeDef.of(
                "51825", -765.193f, 5187.4f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("13708", "32561", "47363", "9164")
        ));

        list.add(TalentNodeDef.of(
                "64312", -4588.07f, 5427.44f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("23861", "54886", "63451")
        ));

        list.add(TalentNodeDef.of(
                "64939", -5314.13f, 3684.75f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("30123", "34210")
        ));

        list.add(TalentNodeDef.of(
                "6923", -5147.13f, 3810.01f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("1087", "30123")
        ));

        list.add(TalentNodeDef.of(
                "9164", -884.694f, 5306.9f,
                "two_handed_damage", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("25513", "51825")
        ));

        list.add(TalentNodeDef.of(
                "23861", -4496.57f, 5768.94f,
                "two_handed_damage_and_stun", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("56997", "64312")
        ));

        list.add(TalentNodeDef.of(
                "54886", -4929.58f, 5518.94f,
                "two_handed_damage_and_stun", "passives_2handeddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_TWO_HANDED_WEAPONS, 0.1)
                ),
                List.of("56997", "64312")
        ));

        list.add(TalentNodeDef.of(
                "36217", 10030.3f, -2498.79f,
                "volatility_detonation_time", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_VOLATILITY_EXPLOSION_DELAY, 0.15)
                ),
                List.of("29990", "47021")
        ));

        list.add(TalentNodeDef.of(
                "47021", 9959.33f, -2621.79f,
                "volatility_detonation_time", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_VOLATILITY_EXPLOSION_DELAY, 0.15)
                ),
                List.of("34425", "36217")
        ));

        list.add(TalentNodeDef.of(
                "47477", 9959.33f, -2375.79f,
                "volatility_detonation_time", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_VOLATILITY_EXPLOSION_DELAY, -0.15)
                ),
                List.of("29990", "51774")
        ));

        list.add(TalentNodeDef.of(
                "51774", 9817.3f, -2375.79f,
                "volatility_detonation_time", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_VOLATILITY_EXPLOSION_DELAY, -0.15)
                ),
                List.of("34425", "47477")
        ));

        list.add(TalentNodeDef.of(
                "24150", 6148.35f, -8425.48f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.03)
                ),
                List.of("44369", "61403")
        ));

        list.add(TalentNodeDef.of(
                "29990", 10175.8f, -2332.79f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.05)
                ),
                List.of("30808", "36217", "47477")
        ));

        list.add(TalentNodeDef.of(
                "30102", 7034.63f, -4806.08f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.03)
                ),
                List.of("43944", "53177", "62578")
        ));

        list.add(TalentNodeDef.of(
                "44369", 5815.88f, -8184.51f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.03)
                ),
                List.of("24150", "30748")
        ));

        list.add(TalentNodeDef.of(
                "45798", 6990.63f, -4325.05f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.03)
                ),
                List.of("46615", "51741", "62578")
        ));

        list.add(TalentNodeDef.of(
                "62578", 6860.57f, -4522.67f,
                "volatility_on_kill", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, 0.03)
                ),
                List.of("30102", "45798")
        ));

        list.add(TalentNodeDef.of(
                "46615", 7202.07f, -4431.16f,
                "volatility_when_stunned", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_WHEN_YOU_ARE_STUNNED, 0.5)
                ),
                List.of("45798", "53177")
        ));

        list.add(TalentNodeDef.of(
                "53177", 7193.04f, -4763.63f,
                "volatility_when_stunned", "passives_increasedchaosdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_VOLATILITY_WHEN_YOU_ARE_STUNNED, 0.5)
                ),
                List.of("30102", "43944", "46615")
        ));

        list.add(TalentNodeDef.of(
                "4015", -5143.75f, 7001.9f,
                "warcry_cooldown", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("37226", "47429", "59466")
        ));

        list.add(TalentNodeDef.of(
                "47429", -4968.25f, 6697.93f,
                "warcry_cooldown", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("4015", "61490")
        ));

        list.add(TalentNodeDef.of(
                "63979", -6497.79f, 9162.67f,
                "warcry_cooldown", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("17999", "9750")
        ));

        list.add(TalentNodeDef.of(
                "9750", -6714.29f, 9287.67f,
                "warcry_cooldown", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("1169", "63979")
        ));

        list.add(TalentNodeDef.of(
                "17999", -6497.79f, 8912.67f,
                "warcry_cooldown_and_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.08),
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.06)
                ),
                List.of("42026", "51561", "63979")
        ));

        list.add(TalentNodeDef.of(
                "38130", -5982.73f, 3082.83f,
                "warcry_cooldown_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("53194", "57703", "62216")
        ));

        list.add(TalentNodeDef.of(
                "53194", -6124.76f, 2830.83f,
                "warcry_cooldown_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("35876", "38130")
        ));

        list.add(TalentNodeDef.of(
                "62200", -8651.3f, -1500.16f,
                "warcry_cooldown_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("29762", "9528")
        ));

        list.add(TalentNodeDef.of(
                "9528", -8745.45f, -1181.98f,
                "warcry_cooldown_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("38053", "62200")
        ));

        list.add(TalentNodeDef.of(
                "47722", -8358.1f, 4483.55f,
                "warcry_damage", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_WARCRIES, 0.16)
                ),
                List.of("51832", "6514")
        ));

        list.add(TalentNodeDef.of(
                "51832", -8483.35f, 4650.55f,
                "warcry_damage", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_WARCRIES, 0.16)
                ),
                List.of("12418", "3988", "47722")
        ));

        list.add(TalentNodeDef.of(
                "27540", -6069.18f, 2426.22f,
                "warcry_power_counted", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTAL_POWER_COUNTED_BY_WARCRIES, 0.1)
                ),
                List.of("35876", "62973")
        ));

        list.add(TalentNodeDef.of(
                "62973", -5896.29f, 2426.22f,
                "warcry_power_counted", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTAL_POWER_COUNTED_BY_WARCRIES, 0.1)
                ),
                List.of("26070", "27540")
        ));

        list.add(TalentNodeDef.of(
                "25014", -7655.53f, -3652.59f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("50228", "51821", "62360")
        ));

        list.add(TalentNodeDef.of(
                "28564", -8367.25f, -1336.16f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("38053", "38707", "40328")
        ));

        list.add(TalentNodeDef.of(
                "37226", -5098.49f, 7257.52f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("4015", "9187")
        ));

        list.add(TalentNodeDef.of(
                "37260", -7230.84f, -2268.12f,
                "warcry_speed", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("30395", "9442")
        ));

        list.add(TalentNodeDef.of(
                "40328", -8273.1f, -1654.33f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("28564", "29762")
        ));

        list.add(TalentNodeDef.of(
                "42026", -6714.29f, 8787.67f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("17999", "63813")
        ));

        list.add(TalentNodeDef.of(
                "63813", -6930.8f, 8912.67f,
                "warcry_speed", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("1169", "42026")
        ));

        list.add(TalentNodeDef.of(
                "9442", -7352.4f, -2426.53f,
                "warcry_speed", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.16)
                ),
                List.of("24420", "37260")
        ));
        list.add(TalentNodeDef.of(
                "54447", 0.005f, -1490.58f,
                "witch", "passives_blankint",
                List.of(),
                List.of("44871", "4739")
        ));

        list.add(TalentNodeDef.of(
                "26762", 7420.17f, -8305.21f,
                "withered_effect", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WITHERED_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("35380", "58022")
        ));

        list.add(TalentNodeDef.of(
                "35380", 7324.01f, -8522.88f,
                "withered_effect", "passives_chaosdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WITHERED_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("26762", "44373")
        ));
        return list;
    }
}
