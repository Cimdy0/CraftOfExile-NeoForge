package com.cimdy.coe.data.gen.talent.node.minor;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinorNodes14 {

    private MinorNodes14() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "23650", -2977.66f, 4647.94f,
                "life_regeneration_on_low_life", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH, 0.15)
                ),
                List.of("13562", "43895")
        ));

        list.add(TalentNodeDef.of(
                "43895", -2748.56f, 4483.94f,
                "life_regeneration_on_low_life", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_ON_LOW_HEALTH, 0.15)
                ),
                List.of("23650", "48670")
        ));

        list.add(TalentNodeDef.of(
                "11580", -4152.04f, -3397.1f,
                "life_regeneration_rate", "passives_lifepercentage",
                List.of(),
                List.of("34769", "51618")
        ));

        list.add(TalentNodeDef.of(
                "23825", -4827.89f, -9014.32f,
                "life_regeneration_rate", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("11572", "24325", "48552")
        ));

        list.add(TalentNodeDef.of(
                "34769", -4273.59f, -3555.51f,
                "life_regeneration_rate", "passives_lifepercentage",
                List.of(),
                List.of("11580", "52115")
        ));

        list.add(TalentNodeDef.of(
                "42583", -2261.34f, 4206.32f,
                "life_regeneration_rate", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.1)
                ),
                List.of("10499", "6714")
        ));

        list.add(TalentNodeDef.of(
                "24325", -4586.41f, -8949.61f,
                "life_regeneration_rate_and_presence", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.1)
                ),
                List.of("23825", "40006")
        ));

        list.add(TalentNodeDef.of(
                "40006", -4492.7f, -9148.65f,
                "life_regeneration_rate_and_presence", "passives_liferecoupnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.1)
                ),
                List.of("24325", "9896")
        ));

        list.add(TalentNodeDef.of(
                "31159", -7739.54f, -4638.11f,
                "life_regeneration_while_stationary", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, 0.15)
                ),
                List.of("11741", "46017")
        ));

        list.add(TalentNodeDef.of(
                "46017", -7523.03f, -4763.11f,
                "life_regeneration_while_stationary", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, 0.15)
                ),
                List.of("31159", "54962")
        ));

        list.add(TalentNodeDef.of(
                "54962", -7306.53f, -4638.11f,
                "life_regeneration_while_stationary", "passives_lifepercentage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_STATIONARY, 0.15)
                ),
                List.of("35849", "46017")
        ));

        list.add(TalentNodeDef.of(
                "4623", -4146.78f, -6880.65f,
                "life_spell_costs", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST, 0.15)
                ),
                List.of("48524", "48530")
        ));

        list.add(TalentNodeDef.of(
                "39130", -4430.83f, -6880.65f,
                "life_spell_damage", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH, 0.12)
                ),
                List.of("14294", "48530")
        ));

        list.add(TalentNodeDef.of(
                "48530", -4288.8f, -6962.65f,
                "life_spell_damage_and_costs", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST, 0.08)
                ),
                List.of("1433", "39130", "4623")
        ));

        list.add(TalentNodeDef.of(
                "19461", 8068.25f, -5993.65f,
                "lightning_and_cold_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08)
                ),
                List.of("56767", "64415")
        ));

        list.add(TalentNodeDef.of(
                "46157", 3017.04f, -8315.27f,
                "lightning_chain_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME, 0.2)
                ),
                List.of("37806", "53560")
        ));

        list.add(TalentNodeDef.of(
                "53560", 2785.02f, -8428.64f,
                "lightning_chain_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME, 0.2)
                ),
                List.of("46157", "53266")
        ));

        list.add(TalentNodeDef.of(
                "11315", 2015.42f, 9673.14f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("48846", "5961")
        ));

        list.add(TalentNodeDef.of(
                "11736", 1249.73f, -11026.7f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("18651", "62677")
        ));

        list.add(TalentNodeDef.of(
                "17025", -1610.33f, -9722.75f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("13515", "17057")
        ));

        list.add(TalentNodeDef.of(
                "17057", -1446.33f, -9804.75f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("17025", "22784", "45497")
        ));

        list.add(TalentNodeDef.of(
                "17420", 9124.17f, -3861.86f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("15356", "18717", "25565")
        ));

        list.add(TalentNodeDef.of(
                "18593", -10577.4f, -391.035f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("47080", "47387")
        ));

        list.add(TalentNodeDef.of(
                "18651", 1129.94f, -11238.3f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("11736", "63863")
        ));

        list.add(TalentNodeDef.of(
                "18717", 8862.26f, -3861.86f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("17420", "60483")
        ));

        list.add(TalentNodeDef.of(
                "19953", -11718.4f, 600.039f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(),
                List.of("3544", "50142")
        ));

        list.add(TalentNodeDef.of(
                "20649", 9947.79f, 2860.79f,
                "lightning_damage", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("2582", "62998", "65290")
        ));

        list.add(TalentNodeDef.of(
                "21560", -204.425f, -7142.42f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("44405", "46819")
        ));

        list.add(TalentNodeDef.of(
                "23724", -609.775f, 9493.96f,
                "lightning_damage", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("38270", "7275")
        ));

        list.add(TalentNodeDef.of(
                "25565", 9234.09f, -4261.87f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("17420", "722", "8273")
        ));

        list.add(TalentNodeDef.of(
                "26905", 1414.12f, -12145.0f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("28975", "8821")
        ));

        list.add(TalentNodeDef.of(
                "30662", -6906.44f, -363.325f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("26291", "45383")
        ));

        list.add(TalentNodeDef.of(
                "33292", 4784.23f, -9692.38f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("3251", "57928")
        ));

        list.add(TalentNodeDef.of(
                "3544", -11626.9f, 941.545f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(),
                List.of("18353", "19953")
        ));

        list.add(TalentNodeDef.of(
                "37113", -10559.1f, -803.165f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("28982", "47080")
        ));

        list.add(TalentNodeDef.of(
                "38270", -484.775f, 9376.46f,
                "lightning_damage", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("23724", "29402")
        ));

        list.add(TalentNodeDef.of(
                "43303", 4997.21f, -9613.09f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("50498", "57928")
        ));

        list.add(TalentNodeDef.of(
                "44345", 2947.79f, 4537.32f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("48833", "64352")
        ));

        list.add(TalentNodeDef.of(
                "44405", -382.905f, -7264.94f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("21560", "60878")
        ));

        list.add(TalentNodeDef.of(
                "45383", -7073.34f, -333.895f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("23930", "30662")
        ));

        list.add(TalentNodeDef.of(
                "47080", -10673.3f, -593.395f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("18593", "37113")
        ));

        list.add(TalentNodeDef.of(
                "48833", 3228.15f, 4292.78f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("4", "44345", "5564")
        ));

        list.add(TalentNodeDef.of(
                "48846", 1657.19f, 9770.52f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("11315", "44566")
        ));

        list.add(TalentNodeDef.of(
                "50142", -12302.0f, 649.639f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(),
                List.of("19953", "58197")
        ));

        list.add(TalentNodeDef.of(
                "54675", 2281.59f, 10332.1f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("58814", "5961")
        ));

        list.add(TalentNodeDef.of(
                "57928", 5022.51f, -9397.47f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("33292", "43303")
        ));

        list.add(TalentNodeDef.of(
                "5961", 2260.4f, 9939.27f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("11315", "5295", "54675")
        ));

        list.add(TalentNodeDef.of(
                "60483", 8733.08f, -3633.16f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("18717", "36540", "7809")
        ));

        list.add(TalentNodeDef.of(
                "63863", 1367.44f, -11645.1f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("18651", "27785", "8821")
        ));

        list.add(TalentNodeDef.of(
                "64083", -3427.75f, -11534.9f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(),
                List.of("12940", "25570")
        ));

        list.add(TalentNodeDef.of(
                "64352", 2806.74f, 4311.96f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("26786", "44345")
        ));

        list.add(TalentNodeDef.of(
                "8821", 1249.73f, -11848.7f,
                "lightning_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.12)
                ),
                List.of("26905", "55554", "63863")
        ));

        list.add(TalentNodeDef.of(
                "16401", 8325.04f, -131.005f,
                "lightning_damage_and_electrocute_buildup", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.1)
                ),
                List.of("12253", "44490")
        ));

        list.add(TalentNodeDef.of(
                "44490", 8203.42f, -391.255f,
                "lightning_damage_and_electrocute_buildup", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.1)
                ),
                List.of("16401", "43090")
        ));

        list.add(TalentNodeDef.of(
                "57069", 9696.74f, 2772.94f,
                "lightning_damage_and_resistance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.03)
                ),
                List.of("52257", "65290")
        ));

        list.add(TalentNodeDef.of(
                "65290", 10006.4f, 2637.88f,
                "lightning_damage_and_resistance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.03)
                ),
                List.of("20649", "57069")
        ));

        list.add(TalentNodeDef.of(
                "12992", -7465.29f, -7177.17f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("16485", "296")
        ));

        list.add(TalentNodeDef.of(
                "14363", -782.025f, -5835.61f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("4776", "61338")
        ));

        list.add(TalentNodeDef.of(
                "15356", 8992.39f, -3633.64f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("17420", "18815")
        ));

        list.add(TalentNodeDef.of(
                "16485", -7301.29f, -7177.17f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("12992", "2344")
        ));

        list.add(TalentNodeDef.of(
                "18815", 9125.5f, -3407.73f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("15356", "40990")
        ));

        list.add(TalentNodeDef.of(
                "27785", 1492.44f, -11428.6f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("36293", "63863")
        ));

        list.add(TalentNodeDef.of(
                "29763", 4889.32f, -7413.25f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("25557", "39423")
        ));

        list.add(TalentNodeDef.of(
                "30372", 5994.29f, 1604.93f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("42065", "55400")
        ));

        list.add(TalentNodeDef.of(
                "31345", 6079.18f, 1921.76f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("41096", "55400")
        ));

        list.add(TalentNodeDef.of(
                "36293", 1381.34f, -11239.6f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("27785", "55708")
        ));

        list.add(TalentNodeDef.of(
                "38215", 7662.61f, -988.805f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("49388", "49996", "61921")
        ));

        list.add(TalentNodeDef.of(
                "39423", 5146.95f, -7492.44f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("29763", "53207")
        ));

        list.add(TalentNodeDef.of(
                "4776", -867.025f, -5688.38f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("14363", "4456")
        ));

        list.add(TalentNodeDef.of(
                "5295", 2476.38f, 9574.5f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("12337", "5961")
        ));

        list.add(TalentNodeDef.of(
                "53207", 5359.99f, -7369.44f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("39423", "47635", "56914")
        ));

        list.add(TalentNodeDef.of(
                "53965", 3203.89f, 9325.66f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("12337", "51336")
        ));

        list.add(TalentNodeDef.of(
                "55400", 6195.15f, 1720.9f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("30372", "31345")
        ));

        list.add(TalentNodeDef.of(
                "56914", 5530.46f, -7475.15f,
                "lightning_penetration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.06)
                ),
                List.of("50121", "53207")
        ));

        list.add(TalentNodeDef.of(
                "22697", -9653.55f, -1913.76f,
                "lightning_resistance", "passives_lightningresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05)
                ),
                List.of("43250", "5862")
        ));

        list.add(TalentNodeDef.of(
                "36540", 8733.08f, -3371.16f,
                "lightning_resistance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05)
                ),
                List.of("56988", "60483")
        ));

        list.add(TalentNodeDef.of(
                "41363", -7267.3f, 7857.22f,
                "lightning_resistance", "passives_lightningstr",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05)
                ),
                List.of("62498", "62518")
        ));

        list.add(TalentNodeDef.of(
                "5862", -9860.55f, -1839.28f,
                "lightning_resistance", "passives_lightningresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.05)
                ),
                List.of("22697", "53123")
        ));

        list.add(TalentNodeDef.of(
                "13576", 2802.27f, -8676.29f,
                "lightning_speed", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.03)
                ),
                List.of("17024", "53266")
        ));

        list.add(TalentNodeDef.of(
                "17024", 2607.33f, -8878.62f,
                "lightning_speed", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.03)
                ),
                List.of("13576", "37372")
        ));

        list.add(TalentNodeDef.of(
                "37372", 2810.3f, -9096.02f,
                "lightning_speed", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.03)
                ),
                List.of("13738", "17024")
        ));

        list.add(TalentNodeDef.of(
                "53266", 2453.78f, -8312.45f,
                "lightning_speed", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS, 0.03)
                ),
                List.of("13576", "20387", "35896", "53560")
        ));

        list.add(TalentNodeDef.of(
                "12276", -10366.4f, 5105.13f,
                "mace_aftershock_chance", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.08)
                ),
                List.of("13980", "375")
        ));

        list.add(TalentNodeDef.of(
                "13937", -9998.64f, 4698.46f,
                "mace_damage", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_MACES, 0.14)
                ),
                List.of("14693", "17791", "59777")
        ));

        list.add(TalentNodeDef.of(
                "14693", -9895.06f, 4978.82f,
                "mace_damage", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_MACES, 0.14)
                ),
                List.of("13937", "13980")
        ));

        list.add(TalentNodeDef.of(
                "375", -10481.6f, 4698.46f,
                "mace_damage", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_MACES, 0.15)
                ),
                List.of("12276", "52829")
        ));

        list.add(TalentNodeDef.of(
                "52829", -10585.2f, 4288.69f,
                "mace_damage_and_stun_buildup", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_MACES, 0.1)
                ),
                List.of("2645", "375")
        ));

        list.add(TalentNodeDef.of(
                "17791", -10063.3f, 4456.98f,
                "mace_stun_buildup", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_MACES, 0.18)
                ),
                List.of("13937", "526", "59777")
        ));

        list.add(TalentNodeDef.of(
                "526", -10073.2f, 4175.19f,
                "mace_stun_buildup", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_MACES, 0.18)
                ),
                List.of("17791", "2645")
        ));

        list.add(TalentNodeDef.of(
                "2732", 1705.18f, -5106.14f,
                "mana_cost_efficiency", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.08)
                ),
                List.of("16790", "65393")
        ));

        list.add(TalentNodeDef.of(
                "29408", 4580.04f, -3018.56f,
                "mana_cost_efficiency", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.08)
                ),
                List.of("11604", "31888")
        ));

        list.add(TalentNodeDef.of(
                "31888", 4580.04f, -3182.56f,
                "mana_cost_efficiency", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.08)
                ),
                List.of("29408", "34300")
        ));

        list.add(TalentNodeDef.of(
                "65393", 1604.15f, -4953.12f,
                "mana_cost_efficiency", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.08)
                ),
                List.of("11672", "2732")
        ));

        list.add(TalentNodeDef.of(
                "17687", 9465.68f, 5104.06f,
                "mana_flask_charges", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("10472", "2021")
        ));

        list.add(TalentNodeDef.of(
                "17706", 1458.34f, -3527.28f,
                "mana_flask_charges_used", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, -0.04)
                ),
                List.of("42076", "46972")
        ));

        list.add(TalentNodeDef.of(
                "23839", 7353.19f, -1798.01f,
                "mana_flask_charges_used", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, -0.04)
                ),
                List.of("47418", "51006")
        ));

        list.add(TalentNodeDef.of(
                "42076", 1600.36f, -3445.28f,
                "mana_flask_charges_used", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, -0.04)
                ),
                List.of("17706", "4203")
        ));

        list.add(TalentNodeDef.of(
                "51006", 7357.63f, -1978.6f,
                "mana_flask_charges_used", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED_FROM_MANA_FLASKS, -0.04)
                ),
                List.of("23839", "41877", "53958")
        ));

        list.add(TalentNodeDef.of(
                "10472", 9465.68f, 5354.06f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("17687", "27422", "34015")
        ));

        list.add(TalentNodeDef.of(
                "24045", 2573.36f, 3575.92f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("18882", "46224")
        ));

        list.add(TalentNodeDef.of(
                "27422", 9288.91f, 5177.29f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("10472", "2021")
        ));

        list.add(TalentNodeDef.of(
                "49799", 2315.37f, 3377.95f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("16489", "46224")
        ));

        list.add(TalentNodeDef.of(
                "53958", 7184.74f, -1978.6f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("41877", "51006", "63517")
        ));

        list.add(TalentNodeDef.of(
                "63517", 7189.19f, -1798.01f,
                "mana_flask_recovery", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("48974", "53958")
        ));

        list.add(TalentNodeDef.of(
                "1973", -7317.84f, 453.685f,
                "mana_flasks", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("28214", "8607")
        ));

        list.add(TalentNodeDef.of(
                "28214", -7317.84f, 289.685f,
                "mana_flasks", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("1973", "58295")
        ));

        list.add(TalentNodeDef.of(
                "37548", 9366.38f, -801.021f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("17589", "36085", "37742", "8246")
        ));

        list.add(TalentNodeDef.of(
                "46146", 4210.51f, -610.915f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("15829", "43691")
        ));

        list.add(TalentNodeDef.of(
                "46601", 5940.56f, 8403.35f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("14383", "18568", "43720")
        ));

        list.add(TalentNodeDef.of(
                "46887", 6438.56f, 8403.35f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("18568", "38463", "43720")
        ));

        list.add(TalentNodeDef.of(
                "55227", 3929.3f, -244.434f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("15829", "29479")
        ));

        list.add(TalentNodeDef.of(
                "64462", 9049.55f, -716.129f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("17589", "45012", "53150", "8246")
        ));

        list.add(TalentNodeDef.of(
                "64927", 7011.4f, -3563.57f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("51741", "52464", "54351", "8045")
        ));

        list.add(TalentNodeDef.of(
                "8045", 7388.51f, -3237.71f,
                "mana_leech", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("51871", "52464", "64927")
        ));

        list.add(TalentNodeDef.of(
                "18568", 6147.01f, 8244.94f,
                "mana_leech_and_cold_resistance", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("46601", "46887")
        ));

        list.add(TalentNodeDef.of(
                "43720", 6147.01f, 8561.77f,
                "mana_leech_and_cold_resistance", "passives_manaleechthemednode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_MANA_LEECH, 0.1)
                ),
                List.of("46601", "46887")
        ));

        list.add(TalentNodeDef.of(
                "52241", 1828.18f, -5035.12f,
                "mana_on_kill", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.01)
                ),
                List.of("58115", "94")
        ));

        list.add(TalentNodeDef.of(
                "58115", 1746.18f, -4871.12f,
                "mana_on_kill", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.01)
                ),
                List.of("11672", "52241")
        ));

        list.add(TalentNodeDef.of(
                "13500", -3579.22f, -5753.44f,
                "mana_recoup", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA, 0.03)
                ),
                List.of("41044", "44733")
        ));

        list.add(TalentNodeDef.of(
                "41044", -3621.22f, -5247.68f,
                "mana_recoup", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA, 0.03)
                ),
                List.of("13500", "47591")
        ));

        list.add(TalentNodeDef.of(
                "47591", -3888.25f, -5382.19f,
                "mana_recoup", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA, 0.03)
                ),
                List.of("41044", "9226")
        ));

        list.add(TalentNodeDef.of(
                "10079", 276.487f, -9153.2f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("4017", "5314")
        ));

        list.add(TalentNodeDef.of(
                "10159", 2678.78f, -9551.12f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("31977", "3567")
        ));

        list.add(TalentNodeDef.of(
                "10314", 3166.78f, -9551.12f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("16256", "31977")
        ));

        list.add(TalentNodeDef.of(
                "11938", 1958.85f, -978.145f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("18970", "39964")
        ));

        list.add(TalentNodeDef.of(
                "12918", 560.543f, -9153.2f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("26447", "4017")
        ));

        list.add(TalentNodeDef.of(
                "13769", 142.033f, -2949.45f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("2254", "59636")
        ));

        list.add(TalentNodeDef.of(
                "1459", -7642.03f, -3994.19f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, 0.16)
                ),
                List.of("35369", "47252")
        ));

        list.add(TalentNodeDef.of(
                "1468", 164.825f, -6450.52f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("14324", "7971")
        ));

        list.add(TalentNodeDef.of(
                "17282", -7858.54f, -4369.19f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, 0.16)
                ),
                List.of("11741", "47252")
        ));

        list.add(TalentNodeDef.of(
                "2071", -3516.49f, -3399.91f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("38420", "38856")
        ));

        list.add(TalentNodeDef.of(
                "25026", 2633.53f, -10140.7f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("3567", "36379")
        ));

        list.add(TalentNodeDef.of(
                "25503", -7155.01f, -5414.42f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("27068", "45632")
        ));

        list.add(TalentNodeDef.of(
                "27068", -7063.5f, -5322.91f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("25503", "35831")
        ));

        list.add(TalentNodeDef.of(
                "30834", -2574.19f, -2205.28f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("50216", "57967", "63469")
        ));

        list.add(TalentNodeDef.of(
                "31977", 2922.78f, -9316.75f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("10159", "10314", "29240", "4828")
        ));

        list.add(TalentNodeDef.of(
                "3242", -142.023f, -2949.45f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("51184", "59636")
        ));

        list.add(TalentNodeDef.of(
                "33345", 3381.35f, -10140.7f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("10131", "61923")
        ));

        list.add(TalentNodeDef.of(
                "34248", -3882.72f, -8711.35f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("37327", "4295")
        ));

        list.add(TalentNodeDef.of(
                "35831", -6761.73f, -5362.64f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("11248", "27068", "904")
        ));

        list.add(TalentNodeDef.of(
                "36379", 2464.21f, -10140.7f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("25026", "46554")
        ));

        list.add(TalentNodeDef.of(
                "37327", -3809.97f, -8419.35f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("34248", "48618")
        ));

        list.add(TalentNodeDef.of(
                "38420", -3658.52f, -3317.91f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("2071", "37543")
        ));

        list.add(TalentNodeDef.of(
                "3918", -2043.73f, -1306.22f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("54708", "59695")
        ));

        list.add(TalentNodeDef.of(
                "39964", 2100.88f, -1060.15f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("11938", "48198")
        ));

        list.add(TalentNodeDef.of(
                "4017", 418.515f, -9071.2f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("10079", "12918")
        ));

        list.add(TalentNodeDef.of(
                "43036", -5153.32f, -8409.77f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("2244", "36880", "48552")
        ));

        list.add(TalentNodeDef.of(
                "43576", 290.077f, -6283.52f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("7971", "8616")
        ));

        list.add(TalentNodeDef.of(
                "45481", 4295.99f, -3018.56f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("34300", "52765")
        ));

        list.add(TalentNodeDef.of(
                "4552", 6023.22f, -5234.44f,
                "mana_regeneration", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("17215", "42379", "50817")
        ));

        list.add(TalentNodeDef.of(
                "47252", -7858.54f, -4119.19f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_STATIONARY, 0.16)
                ),
                List.of("1459", "17282")
        ));

        list.add(TalentNodeDef.of(
                "4828", 2922.78f, -9809.75f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("19044", "31977")
        ));

        list.add(TalentNodeDef.of(
                "48618", -3593.46f, -8210.35f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("34840", "37327", "6748")
        ));

        list.add(TalentNodeDef.of(
                "52053", 7097.38f, -213.495f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("14048", "14658", "24120")
        ));
        return list;
    }
}
