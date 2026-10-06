package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes13 {

    private MinorNodes13() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "12324", -4756.21f, -10056.7f,
                "infusion_chance", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE, 0.05)
                ),
                List.of("24764", "30985")
        ));

        list.add(TalentNodeDef.of(
                "30985", -4633.21f, -9985.65f,
                "infusion_chance", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE, 0.05)
                ),
                List.of("11284", "12324")
        ));

        list.add(TalentNodeDef.of(
                "24060", -760.511f, -7717.31f,
                "infusion_consumption_chance", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM, 0.05)
                ),
                List.of("3091", "51194")
        ));

        list.add(TalentNodeDef.of(
                "3091", -872.525f, -7605.3f,
                "infusion_consumption_chance", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM, 0.05)
                ),
                List.of("24060", "8782")
        ));

        list.add(TalentNodeDef.of(
                "11284", -4473.73f, -10063.0f,
                "infusion_duration", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.1)
                ),
                List.of("30985", "31697", "50104")
        ));

        list.add(TalentNodeDef.of(
                "31697", -4573.19f, -10209.7f,
                "infusion_duration", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.1)
                ),
                List.of("11284", "51303")
        ));

        list.add(TalentNodeDef.of(
                "51194", -831.525f, -7840.31f,
                "infusion_duration", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.1)
                ),
                List.of("18793", "24060", "49512")
        ));

        list.add(TalentNodeDef.of(
                "51303", -4756.21f, -10198.7f,
                "infusion_duration", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.1)
                ),
                List.of("31697", "38965")
        ));

        list.add(TalentNodeDef.of(
                "14428", -511.325f, 11354.8f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("32976", "6287")
        ));

        list.add(TalentNodeDef.of(
                "22115", 5024.0f, 133.425f,
                "intelligence", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("32701", "59446", "61263")
        ));

        list.add(TalentNodeDef.of(
                "32436", 542.445f, 11370.4f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("5332", "62505")
        ));

        list.add(TalentNodeDef.of(
                "42226", 10203.9f, 823.227f,
                "intelligence", "passives_azmeriprimalmonkey",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("17792", "34892", "48773")
        ));

        list.add(TalentNodeDef.of(
                "50755", 3200.45f, -10569.2f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("10131", "19355", "39567")
        ));

        list.add(TalentNodeDef.of(
                "50816", 2645.12f, -10569.2f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("37974", "39567", "46554")
        ));

        list.add(TalentNodeDef.of(
                "56564", -1438.27f, -4174.89f,
                "intelligence", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("42077", "8349")
        ));

        list.add(TalentNodeDef.of(
                "62505", 290.445f, 11228.4f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("32436", "34813")
        ));

        list.add(TalentNodeDef.of(
                "6287", -262.325f, 11207.6f,
                "intelligence", "passives_plusintelligence",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("14428", "364")
        ));

        list.add(TalentNodeDef.of(
                "64990", 10285.1f, 1872.13f,
                "intelligence", "passives_azmeriprimalowl",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("18897", "27705", "60891")
        ));

        list.add(TalentNodeDef.of(
                "8349", -1480.72f, -4333.3f,
                "intelligence", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 8.0)
                ),
                List.of("31644", "56564")
        ));

        list.add(TalentNodeDef.of(
                "16024", 5168.46f, -6141.72f,
                "invocation_critical_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS, 0.2)
                ),
                List.of("29288", "42245")
        ));

        list.add(TalentNodeDef.of(
                "18167", 5332.46f, -5857.67f,
                "invocation_critical_hits", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.12)
                ),
                List.of("42245", "49150")
        ));

        list.add(TalentNodeDef.of(
                "18856", 1802.15f, -9144.16f,
                "invocation_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_DEAL_INC_DAMAGE, 0.15)
                ),
                List.of("24491", "3438")
        ));

        list.add(TalentNodeDef.of(
                "20861", 1802.15f, -8560.16f,
                "invocation_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_DEAL_INC_DAMAGE, 0.15)
                ),
                List.of("17672", "45350", "8522")
        ));

        list.add(TalentNodeDef.of(
                "3438", 2038.32f, -9046.33f,
                "invocation_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_DEAL_INC_DAMAGE, 0.15)
                ),
                List.of("18856", "45350")
        ));

        list.add(TalentNodeDef.of(
                "45350", 2052.15f, -8810.16f,
                "invocation_spell_damage", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_DEAL_INC_DAMAGE, 0.15)
                ),
                List.of("20861", "3438")
        ));

        list.add(TalentNodeDef.of(
                "14515", -6613.64f, 6822.38f,
                "jagged_ground_effect", "icongroundslam",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_JAGGED_GROUND_YOU_CREATE, 0.15)
                ),
                List.of("43778", "61927", "61938")
        ));

        list.add(TalentNodeDef.of(
                "61927", -6744.48f, 6612.94f,
                "jagged_ground_effect", "icongroundslam",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_JAGGED_GROUND_YOU_CREATE, 0.15)
                ),
                List.of("14515", "17330", "3698")
        ));

        list.add(TalentNodeDef.of(
                "61938", -7069.9f, 6944.64f,
                "jagged_ground_effect", "icongroundslam",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_JAGGED_GROUND_YOU_CREATE, 0.15)
                ),
                List.of("14515", "3446", "36894")
        ));

        list.add(TalentNodeDef.of(
                "20837", 3537.82f, 3734.46f,
                "knockback", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.08)
                ),
                List.of("28268", "61104")
        ));

        list.add(TalentNodeDef.of(
                "43444", 220.221f, 5837.0f,
                "knockback", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.08)
                ),
                List.of("48635", "57805")
        ));

        list.add(TalentNodeDef.of(
                "7788", 220.221f, 5587.0f,
                "knockback", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.08)
                ),
                List.of("57805", "63526")
        ));

        list.add(TalentNodeDef.of(
                "26726", 8294.62f, -1613.59f,
                "knockback_and_stun_buildup", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.1)
                ),
                List.of("48103", "5163")
        ));

        list.add(TalentNodeDef.of(
                "5163", 8178.65f, -1333.63f,
                "knockback_and_stun_buildup", "passives_knockback",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.1)
                ),
                List.of("26726", "49996")
        ));

        list.add(TalentNodeDef.of(
                "14176", -6376.4f, 3450.19f,
                "later_rage_loss_start", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, 1.0)
                ),
                List.of("18004", "39621")
        ));

        list.add(TalentNodeDef.of(
                "18004", -6556.99f, 3281.75f,
                "later_rage_loss_start", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, 1.0)
                ),
                List.of("14176", "8881")
        ));

        list.add(TalentNodeDef.of(
                "21413", -9017.72f, -610.63f,
                "later_rage_loss_start", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, 1.0)
                ),
                List.of("28408", "37869")
        ));

        list.add(TalentNodeDef.of(
                "37869", -8992.74f, -420.925f,
                "later_rage_loss_start", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, 1.0)
                ),
                List.of("21413", "60068")
        ));

        list.add(TalentNodeDef.of(
                "39621", -6234.38f, 3368.19f,
                "later_rage_loss_start", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, 1.0)
                ),
                List.of("14176", "57703")
        ));

        list.add(TalentNodeDef.of(
                "19470", 2250.45f, 1571.91f,
                "life_and_mana_flask_recovery", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("10053", "35987")
        ));

        list.add(TalentNodeDef.of(
                "50328", 2486.62f, 1162.85f,
                "life_and_mana_flask_recovery", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("10053", "28992")
        ));

        list.add(TalentNodeDef.of(
                "21792", 4786.73f, 9048.06f,
                "life_and_mana_on_kill", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.01),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.01)
                ),
                List.of("23244", "29899")
        ));

        list.add(TalentNodeDef.of(
                "23195", -6074.1f, -6169.2f,
                "life_and_mana_regeneration_rate", "passives_lifeandmana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("4970", "55375")
        ));

        list.add(TalentNodeDef.of(
                "4970", -5958.13f, -5968.34f,
                "life_and_mana_regeneration_rate", "passives_lifeandmana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("23195", "44872")
        ));

        list.add(TalentNodeDef.of(
                "16090", -4875.76f, 1000.39f,
                "life_costs", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 0.06)
                ),
                List.of("50302", "53405")
        ));

        list.add(TalentNodeDef.of(
                "43324", -3820.34f, -3722.51f,
                "life_costs", "passives_manastr",
                List.of(),
                List.of("51618", "57596")
        ));

        list.add(TalentNodeDef.of(
                "50302", -5078.57f, 1156.0f,
                "life_costs", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 0.06)
                ),
                List.of("16090", "63470")
        ));

        list.add(TalentNodeDef.of(
                "57596", -4152.13f, -4041.66f,
                "life_costs", "passives_manastr",
                List.of(),
                List.of("13468", "43324")
        ));

        list.add(TalentNodeDef.of(
                "63470", -5332.01f, 1122.64f,
                "life_costs", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 0.06)
                ),
                List.of("50302", "9908")
        ));

        list.add(TalentNodeDef.of(
                "24993", -7519.81f, -9783.87f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("43721", "54297")
        ));

        list.add(TalentNodeDef.of(
                "25678", -7678.42f, -9697.42f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("49258", "54297")
        ));

        list.add(TalentNodeDef.of(
                "43721", -7842.42f, -10185.4f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("11160", "24993")
        ));

        list.add(TalentNodeDef.of(
                "49258", -8086.42f, -10120.0f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("16332", "25678", "49769")
        ));

        list.add(TalentNodeDef.of(
                "54297", -7419.8f, -9453.42f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("23382", "24993", "25678")
        ));

        list.add(TalentNodeDef.of(
                "9554", -8485.14f, -10236.7f,
                "life_costs_and_chaos_damage", "passives_life1",
                List.of(),
                List.of("1628", "8723")
        ));

        list.add(TalentNodeDef.of(
                "51618", -3984.59f, -3339.01f,
                "life_costs_and_regeneration", "passives_manastr",
                List.of(),
                List.of("11580", "43324", "49357")
        ));

        list.add(TalentNodeDef.of(
                "26363", 6732.33f, -819.642f,
                "life_flask_charge_generation", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("49291", "52803")
        ));

        list.add(TalentNodeDef.of(
                "49291", 6732.33f, -450.288f,
                "life_flask_charge_generation", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("26363", "57945")
        ));

        list.add(TalentNodeDef.of(
                "57945", 6555.55f, -377.065f,
                "life_flask_charge_generation", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("14658", "49291", "7412")
        ));

        list.add(TalentNodeDef.of(
                "13619", 5658.94f, 4039.75f,
                "life_flask_charges", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("12208", "59600")
        ));

        list.add(TalentNodeDef.of(
                "28106", 5016.55f, 10537.4f,
                "life_flask_charges", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("30392", "3775")
        ));

        list.add(TalentNodeDef.of(
                "3775", 4874.52f, 10619.4f,
                "life_flask_charges", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("28106", "45244")
        ));

        list.add(TalentNodeDef.of(
                "45709", 6378.78f, -819.642f,
                "life_flask_charges", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("52803", "7412")
        ));

        list.add(TalentNodeDef.of(
                "59446", 5253.43f, 131.979f,
                "life_flask_charges", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("22115", "4544")
        ));

        list.add(TalentNodeDef.of(
                "7412", 6378.78f, -450.288f,
                "life_flask_charges", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("45709", "57945")
        ));

        list.add(TalentNodeDef.of(
                "45962", -3845.86f, 5615.18f,
                "life_flask_recovery", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("15617", "7183", "7922")
        ));

        list.add(TalentNodeDef.of(
                "48589", -3716.97f, 6096.21f,
                "life_flask_recovery", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("35265", "7183", "7922")
        ));

        list.add(TalentNodeDef.of(
                "7183", -3601.0f, 5895.35f,
                "life_flask_recovery", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("45962", "48589")
        ));

        list.add(TalentNodeDef.of(
                "12208", 5835.72f, 4112.97f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("13619", "30871", "32813")
        ));

        list.add(TalentNodeDef.of(
                "12255", -7025.42f, 8343.21f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("27740", "62498", "989")
        ));

        list.add(TalentNodeDef.of(
                "13157", 4960.97f, 10132.8f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("30392", "58388")
        ));

        list.add(TalentNodeDef.of(
                "23343", 4758.56f, 10339.5f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("45244", "58388")
        ));

        list.add(TalentNodeDef.of(
                "24339", -7459.86f, 207.685f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("58295", "7060")
        ));

        list.add(TalentNodeDef.of(
                "26416", -7167.44f, 8589.21f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.15)
                ),
                List.of("35792", "989")
        ));

        list.add(TalentNodeDef.of(
                "30871", 5835.73f, 4513.83f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("12208", "49466", "63566")
        ));

        list.add(TalentNodeDef.of(
                "31855", 4159.64f, 514.13f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("37408", "65009")
        ));

        list.add(TalentNodeDef.of(
                "32813", 5658.94f, 4168.29f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("12208", "35809", "59600")
        ));

        list.add(TalentNodeDef.of(
                "49466", 5658.96f, 4587.05f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("30871", "9411")
        ));

        list.add(TalentNodeDef.of(
                "58388", 4788.08f, 10132.8f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("13157", "17702", "23343")
        ));

        list.add(TalentNodeDef.of(
                "59600", 5482.17f, 4112.97f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH, 0.25)
                ),
                List.of("13619", "32813", "9411")
        ));

        list.add(TalentNodeDef.of(
                "60738", 3759.74f, 406.979f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("24825", "37408")
        ));

        list.add(TalentNodeDef.of(
                "7060", -7601.89f, 289.685f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("24339", "53131")
        ));

        list.add(TalentNodeDef.of(
                "9411", 5482.18f, 4513.83f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH, 0.25)
                ),
                List.of("49466", "59600", "64434")
        ));

        list.add(TalentNodeDef.of(
                "989", -7167.44f, 8425.21f,
                "life_flasks", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_FROM_FLASKS, 0.15)
                ),
                List.of("12255", "26416")
        ));

        list.add(TalentNodeDef.of(
                "2119", -2087.68f, 7430.08f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.1)
                ),
                List.of("10772", "39274", "53505")
        ));

        list.add(TalentNodeDef.of(
                "21468", -2087.68f, 7928.08f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("2864", "39274", "53505")
        ));

        list.add(TalentNodeDef.of(
                "21716", -4469.53f, 10235.0f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("16691", "28175", "9583")
        ));

        list.add(TalentNodeDef.of(
                "26520", 1674.87f, 4209.12f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("14340", "37190", "58783")
        ));

        list.add(TalentNodeDef.of(
                "2888", -4991.37f, 10756.9f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("47316", "8827")
        ));

        list.add(TalentNodeDef.of(
                "29788", -3069.48f, 4766.42f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("44419", "46060")
        ));

        list.add(TalentNodeDef.of(
                "37190", 1790.84f, 4093.15f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("26520", "35855")
        ));

        list.add(TalentNodeDef.of(
                "39274", -1923.68f, 7594.08f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_SLOWER, -0.08)
                ),
                List.of("2119", "21468")
        ));

        list.add(TalentNodeDef.of(
                "44419", -3160.98f, 5107.93f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("29788", "51299", "7251")
        ));

        list.add(TalentNodeDef.of(
                "53505", -2251.68f, 7594.08f,
                "life_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_FASTER, 0.08)
                ),
                List.of("2119", "21468")
        ));

        list.add(TalentNodeDef.of(
                "18519", 3710.32f, 9841.85f,
                "life_leech_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08),
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("17600", "30910")
        ));

        list.add(TalentNodeDef.of(
                "9583", -4814.59f, 10330.1f,
                "life_leech_and_physical_damage", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, 0.01),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.08)
                ),
                List.of("21716", "47316")
        ));

        list.add(TalentNodeDef.of(
                "51535", -9970.25f, 2520.53f,
                "life_leech_and_slower_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.12),
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_SLOWER, -0.05)
                ),
                List.of("55063", "8852")
        ));

        list.add(TalentNodeDef.of(
                "55063", -9769.39f, 2319.67f,
                "life_leech_and_slower_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.12),
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_SLOWER, -0.05)
                ),
                List.of("18397", "51535")
        ));

        list.add(TalentNodeDef.of(
                "8852", -10086.2f, 2404.56f,
                "life_leech_and_slower_leech", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.12),
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_SLOWER, -0.05)
                ),
                List.of("51535", "59785")
        ));

        list.add(TalentNodeDef.of(
                "48583", 1589.98f, 3892.29f,
                "life_leech_armor_and_evasion_while_leeching", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_WHILE_LEECHING, 0.08)
                ),
                List.of("35855", "58783")
        ));

        list.add(TalentNodeDef.of(
                "58783", 1474.01f, 4008.26f,
                "life_leech_armor_and_evasion_while_leeching", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_WHILE_LEECHING, 0.08)
                ),
                List.of("26520", "48583")
        ));

        list.add(TalentNodeDef.of(
                "16691", -4564.59f, 10580.1f,
                "life_leech_speed", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_HEALTH_FASTER, 0.15)
                ),
                List.of("21716", "8827")
        ));

        list.add(TalentNodeDef.of(
                "24239", 3559.03f, -482.855f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, 5.0)
                ),
                List.of("34136", "55846")
        ));

        list.add(TalentNodeDef.of(
                "26931", 3102.56f, -1405.59f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, 3.0)
                ),
                List.of("48198", "7344")
        ));

        list.add(TalentNodeDef.of(
                "52464", 6899.0f, -3144.07f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.01)
                ),
                List.of("24958", "54351", "64927", "8045")
        ));

        list.add(TalentNodeDef.of(
                "54351", 6413.89f, -3498.87f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.01)
                ),
                List.of("24062", "52464", "64927")
        ));

        list.add(TalentNodeDef.of(
                "55846", 3682.03f, -411.841f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, 5.0)
                ),
                List.of("24239", "40213")
        ));

        list.add(TalentNodeDef.of(
                "58182", 2768.56f, -1984.1f,
                "life_on_kill", "passives_hiredkiller2",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, 3.0)
                ),
                List.of("49220", "7344")
        ));

        list.add(TalentNodeDef.of(
                "10242", 6033.46f, -6756.35f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("38111", "6079")
        ));

        list.add(TalentNodeDef.of(
                "21540", 682.785f, -5708.98f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("27658", "40783", "58090")
        ));

        list.add(TalentNodeDef.of(
                "22141", -2447.32f, -10245.5f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("51797", "54521")
        ));

        list.add(TalentNodeDef.of(
                "23822", 7374.83f, -7315.38f,
                "life_recoup", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("58779", "9652")
        ));

        list.add(TalentNodeDef.of(
                "2508", -2947.32f, -10245.5f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("47168", "59425")
        ));

        list.add(TalentNodeDef.of(
                "27658", 787.345f, -5929.12f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("13542", "21540")
        ));

        list.add(TalentNodeDef.of(
                "34367", 275.935f, -5926.79f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("48774", "58090")
        ));

        list.add(TalentNodeDef.of(
                "38212", 10953.7f, -715.845f,
                "life_recoup", "passives_monkhealthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("14267", "57683")
        ));

        list.add(TalentNodeDef.of(
                "38368", -7138.53f, -5046.81f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("35966", "4091")
        ));

        list.add(TalentNodeDef.of(
                "41105", -7138.53f, -4796.81f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("21568", "38835", "54288")
        ));

        list.add(TalentNodeDef.of(
                "51797", -2480.82f, -10120.5f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("22141", "23939")
        ));

        list.add(TalentNodeDef.of(
                "54288", -6922.03f, -4921.81f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("35966", "41105")
        ));

        list.add(TalentNodeDef.of(
                "57683", 11138.2f, -639.433f,
                "life_recoup", "passives_monkhealthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("35031", "38212")
        ));

        list.add(TalentNodeDef.of(
                "58090", 399.295f, -5702.27f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("21540", "34367", "57710")
        ));

        list.add(TalentNodeDef.of(
                "59425", -2913.83f, -10120.5f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("23939", "2508")
        ));

        list.add(TalentNodeDef.of(
                "59603", 2229.99f, -3149.3f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("4203", "62159")
        ));

        list.add(TalentNodeDef.of(
                "6079", 5749.41f, -6756.35f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("10242", "24165")
        ));

        list.add(TalentNodeDef.of(
                "62159", 2372.02f, -3067.3f,
                "life_recoup", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, 0.03)
                ),
                List.of("57110", "59603")
        ));

        list.add(TalentNodeDef.of(
                "15801", -2494.45f, -5536.38f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("32128", "59438")
        ));

        list.add(TalentNodeDef.of(
                "18101", -2496.26f, -5249.94f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("32128", "59413", "59438", "61615")
        ));

        list.add(TalentNodeDef.of(
                "38835", -7355.04f, -4921.81f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("4091", "41105")
        ));

        list.add(TalentNodeDef.of(
                "59413", -2250.63f, -4971.77f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("18101", "59442")
        ));

        list.add(TalentNodeDef.of(
                "59442", -2496.77f, -4970.02f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("46358", "59413", "61615")
        ));

        list.add(TalentNodeDef.of(
                "61615", -2744.4f, -4969.4f,
                "life_recoup_speed", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPEED_OF_RECOUP_EFFECTS, 0.08)
                ),
                List.of("18101", "59442")
        ));

        list.add(TalentNodeDef.of(
                "11329", -3492.15f, 6889.04f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("48035", "54676")
        ));

        list.add(TalentNodeDef.of(
                "11916", -137.757f, 3867.21f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.002)
                ),
                List.of("28370", "50609")
        ));

        list.add(TalentNodeDef.of(
                "14926", -137.757f, 3533.21f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.002)
                ),
                List.of("50609", "56910")
        ));

        list.add(TalentNodeDef.of(
                "1499", 11095.7f, -961.845f,
                "life_regeneration", "passives_monkhealthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("14267", "33830")
        ));

        list.add(TalentNodeDef.of(
                "16311", -5674.04f, 344.085f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("14654", "32600")
        ));

        list.add(TalentNodeDef.of(
                "17294", -1675.7f, 2730.54f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("19330", "27501", "34340")
        ));

        list.add(TalentNodeDef.of(
                "19330", -2014.26f, 2733.29f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("17294", "45916")
        ));

        list.add(TalentNodeDef.of(
                "20303", -5677.97f, 893.335f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("32600", "9908")
        ));

        list.add(TalentNodeDef.of(
                "22045", -3197.35f, -1125.94f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.002)
                ),
                List.of("13505", "30704", "63209")
        ));

        list.add(TalentNodeDef.of(
                "27501", -1847.36f, 3022.37f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("17294", "45916")
        ));

        list.add(TalentNodeDef.of(
                "32600", -5532.01f, 590.085f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("16311", "20303", "6304")
        ));

        list.add(TalentNodeDef.of(
                "33830", 11254.1f, -840.291f,
                "life_regeneration", "passives_monkhealthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("1499", "35031")
        ));

        list.add(TalentNodeDef.of(
                "39759", -3328.15f, 7173.09f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("48035", "54676")
        ));

        list.add(TalentNodeDef.of(
                "45916", -2102.65f, 2977.04f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("16168", "19330", "27501")
        ));

        list.add(TalentNodeDef.of(
                "4665", -2092.78f, 1304.69f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.002)
                ),
                List.of("1913", "61534")
        ));

        list.add(TalentNodeDef.of(
                "48035", -3294.19f, 6915.1f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("11329", "15374", "39759")
        ));

        list.add(TalentNodeDef.of(
                "52659", -9579.83f, 4275.29f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("10362", "59589")
        ));

        list.add(TalentNodeDef.of(
                "54676", -3526.12f, 7147.03f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("11329", "37612", "39759")
        ));

        list.add(TalentNodeDef.of(
                "6153", -9488.33f, 4616.8f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("10362", "44952")
        ));

        list.add(TalentNodeDef.of(
                "61534", -2342.78f, 1554.69f,
                "life_regeneration", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.002)
                ),
                List.of("4665", "7721")
        ));

        list.add(TalentNodeDef.of(
                "30704", -2951.35f, -1267.96f,
                "life_regeneration_and_damage", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.001)
                ),
                List.of("22045", "63209", "6898")
        ));

        list.add(TalentNodeDef.of(
                "63209", -3197.35f, -1409.99f,
                "life_regeneration_and_damage", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.001)
                ),
                List.of("17655", "22045", "30704", "36602")
        ));

        list.add(TalentNodeDef.of(
                "13562", -3248.56f, 4483.94f,
                "life_regeneration_on_low_life", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH, 0.15)
                ),
                List.of("23650", "56616")
        ));
        return list;
    }
}
