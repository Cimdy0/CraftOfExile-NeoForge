package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes08 {

    private MinorNodes08() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "50118", -8182.66f, -5926.44f,
                "companion_resistance_and_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_TO_ALL_ELEMENTAL_RESISTANCES, 0.12),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("21784", "55450")
        ));

        list.add(TalentNodeDef.of(
                "11066", 261.174f, 8659.07f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("26663", "54282")
        ));

        list.add(TalentNodeDef.of(
                "12451", 3941.17f, 7740.81f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("24922", "33751")
        ));

        list.add(TalentNodeDef.of(
                "14572", 136.547f, 3867.23f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("11037", "49657")
        ));

        list.add(TalentNodeDef.of(
                "17796", -2697.32f, -10985.0f,
                "cooldown_recovery_rate", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("22185", "55412")
        ));

        list.add(TalentNodeDef.of(
                "22533", 437.95f, 8482.29f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("21274", "26663")
        ));

        list.add(TalentNodeDef.of(
                "26663", 502.655f, 8723.77f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("11066", "22533", "44765")
        ));

        list.add(TalentNodeDef.of(
                "27405", -7281.1f, -1418.05f,
                "cooldown_recovery_rate", "passives_movementspeedandevasion",
                List.of(),
                List.of("11464", "30781")
        ));

        list.add(TalentNodeDef.of(
                "33751", 3691.17f, 7406.81f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("12451", "8810")
        ));

        list.add(TalentNodeDef.of(
                "57039", 136.548f, 3533.23f,
                "cooldown_recovery_rate", "passives_greenattacksmallpassive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.05)
                ),
                List.of("11037", "44605")
        ));

        list.add(TalentNodeDef.of(
                "63772", -6728.43f, -1434.83f,
                "cooldown_recovery_rate", "passives_movementspeedandevasion",
                List.of(),
                List.of("30781", "37888")
        ));

        list.add(TalentNodeDef.of(
                "26804", 3896.31f, -7311.39f,
                "corpses", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY, 0.15)
                ),
                List.of("25557", "55405", "59909")
        ));

        list.add(TalentNodeDef.of(
                "55405", 3700.3f, -7135.88f,
                "corpses", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY, 0.15)
                ),
                List.of("25620", "26804")
        ));

        list.add(TalentNodeDef.of(
                "30539", 3521.31f, -7094.88f,
                "corpses", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES, 0.05)
                ),
                List.of("25620", "59909")
        ));

        list.add(TalentNodeDef.of(
                "59909", 3646.31f, -7311.39f,
                "corpses", "passives_corpsedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_NOT_DESTROY_CORPSES_WHEN_CONSUMING_CORPSES, 0.05)
                ),
                List.of("26804", "30539")
        ));

        list.add(TalentNodeDef.of(
                "23786", 4803.05f, 7446.26f,
                "critical_bleeding_effect", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS, 0.15)
                ),
                List.of("33391", "49394")
        ));

        list.add(TalentNodeDef.of(
                "33391", 5009.7f, 7243.85f,
                "critical_bleeding_effect", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS, 0.15)
                ),
                List.of("23786", "49661", "56330")
        ));

        list.add(TalentNodeDef.of(
                "49394", 4445.6f, 7265.59f,
                "critical_bleeding_effect", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS, 0.15)
                ),
                List.of("23786", "39570")
        ));

        list.add(TalentNodeDef.of(
                "11509", 8127.24f, 2582.76f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY, 0.12)
                ),
                List.of("42118", "7465")
        ));

        list.add(TalentNodeDef.of(
                "14958", 3561.1f, -3022.28f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("13419", "17548")
        ));

        list.add(TalentNodeDef.of(
                "16861", -6193.21f, 908.632f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("26969", "27303")
        ));

        list.add(TalentNodeDef.of(
                "18737", -6938.4f, -2426.53f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("17725", "33829")
        ));

        list.add(TalentNodeDef.of(
                "1915", 1493.63f, 10118.8f,
                "critical_chance", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_HUMANOIDS, 0.1)
                ),
                List.of("48617", "60085")
        ));

        list.add(TalentNodeDef.of(
                "21017", -6026.21f, 540.885f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("14654", "26969", "55789")
        ));

        list.add(TalentNodeDef.of(
                "24420", -7313.4f, -2643.04f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("20115", "33829", "9442")
        ));

        list.add(TalentNodeDef.of(
                "26319", 1737.77f, 4614.0f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("14340", "30990")
        ));

        list.add(TalentNodeDef.of(
                "26969", -6152.52f, 726.513f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("16861", "21017")
        ));

        list.add(TalentNodeDef.of(
                "30990", 1595.74f, 4860.0f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("26319", "58939")
        ));

        list.add(TalentNodeDef.of(
                "31692", 7761.63f, -7890.57f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("45702", "46197")
        ));

        list.add(TalentNodeDef.of(
                "32660", -3475.98f, -5009.77f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY, 0.1)
                ),
                List.of("24483", "8631")
        ));

        list.add(TalentNodeDef.of(
                "33829", -7063.4f, -2643.04f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("18737", "24420")
        ));

        list.add(TalentNodeDef.of(
                "38541", 5457.07f, -4742.78f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("34621", "61601")
        ));

        list.add(TalentNodeDef.of(
                "39540", -3723.74f, -1688.4f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("18465", "50558", "55933")
        ));

        list.add(TalentNodeDef.of(
                "40760", 2021.24f, -4414.36f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("21779", "47177", "9185")
        ));

        list.add(TalentNodeDef.of(
                "4157", 2444.54f, -2103.71f,
                "critical_chance", "passives_criticalstrikechance2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("34168", "49220")
        ));

        list.add(TalentNodeDef.of(
                "4346", 4705.26f, -4974.35f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("20236", "20677", "4519")
        ));

        list.add(TalentNodeDef.of(
                "44420", 5457.07f, -5200.42f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("28021", "61601")
        ));

        list.add(TalentNodeDef.of(
                "45702", 7487.25f, -7817.05f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("14446", "31692", "61333")
        ));

        list.add(TalentNodeDef.of(
                "46171", 9974.56f, -5114.7f,
                "critical_chance", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("56334", "61421")
        ));

        list.add(TalentNodeDef.of(
                "46760", -3360.01f, -4893.8f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY, 0.16)
                ),
                List.of("51534", "8631")
        ));

        list.add(TalentNodeDef.of(
                "49107", 8471.74f, 2854.85f,
                "critical_chance", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("49485", "54562")
        ));

        list.add(TalentNodeDef.of(
                "51534", -3527.01f, -4604.55f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY, 0.16)
                ),
                List.of("24483", "46760", "8531")
        ));

        list.add(TalentNodeDef.of(
                "54562", 8635.74f, 2854.85f,
                "critical_chance", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("2745", "49107")
        ));

        list.add(TalentNodeDef.of(
                "55088", 2819.54f, -2320.22f,
                "critical_chance", "passives_criticalstrikechance2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("34168", "61196")
        ));

        list.add(TalentNodeDef.of(
                "55621", 11139.5f, 742.125f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("38493", "38537")
        ));

        list.add(TalentNodeDef.of(
                "59256", -3201.6f, -4851.36f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY, 0.1)
                ),
                List.of("8531", "8631")
        ));

        list.add(TalentNodeDef.of(
                "60107", 2094.76f, -4139.98f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("57204", "9185")
        ));

        list.add(TalentNodeDef.of(
                "61333", 7688.11f, -7616.19f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("45702", "46197")
        ));

        list.add(TalentNodeDef.of(
                "7353", 5557.57f, 3680.57f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("39569", "9046")
        ));

        list.add(TalentNodeDef.of(
                "7465", 8247.45f, 2462.55f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_HAVE_ENTERED_YOUR_PRESENCE_RECENTLY, 0.12)
                ),
                List.of("11509", "17664")
        ));

        list.add(TalentNodeDef.of(
                "8631", -3193.01f, -5183.06f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("30979", "32660", "46760", "59256")
        ));

        list.add(TalentNodeDef.of(
                "9046", 5688.41f, 3890.01f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("56776", "7353")
        ));

        list.add(TalentNodeDef.of(
                "9185", 2137.21f, -4298.39f,
                "critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("40760", "60107")
        ));

        list.add(TalentNodeDef.of(
                "43650", -8007.51f, 4059.83f,
                "critical_chance_and_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.08),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.05)
                ),
                List.of("21070", "26176", "35048", "53386")
        ));

        list.add(TalentNodeDef.of(
                "13419", 3507.11f, -2764.78f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("14958", "61196", "630")
        ));

        list.add(TalentNodeDef.of(
                "20024", 1696.35f, -1419.45f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("42857", "44223")
        ));

        list.add(TalentNodeDef.of(
                "21779", 1820.38f, -4298.39f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("40760", "57204")
        ));

        list.add(TalentNodeDef.of(
                "23040", 11231.0f, 833.631f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("13407", "38493")
        ));

        list.add(TalentNodeDef.of(
                "23915", 5948.12f, 186.785f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("26598", "41529")
        ));

        list.add(TalentNodeDef.of(
                "28021", 5324.92f, -5429.12f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("3463", "44420", "9782")
        ));

        list.add(TalentNodeDef.of(
                "28223", -9204.61f, -389.562f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(),
                List.of("59039", "6100")
        ));

        list.add(TalentNodeDef.of(
                "29959", 10217.9f, -3575.23f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("15775", "60362", "6891")
        ));

        list.add(TalentNodeDef.of(
                "3458", 5356.71f, 3564.6f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("39569", "45609")
        ));

        list.add(TalentNodeDef.of(
                "34621", 5324.92f, -4513.88f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("16705", "38541", "535")
        ));

        list.add(TalentNodeDef.of(
                "36602", -3407.77f, -1423.26f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("18465", "63209")
        ));

        list.add(TalentNodeDef.of(
                "38493", 11010.9f, 962.193f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("23040", "48773", "55621")
        ));

        list.add(TalentNodeDef.of(
                "39569", 5524.15f, 3506.51f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("3458", "35901", "7353")
        ));

        list.add(TalentNodeDef.of(
                "41529", 6106.54f, 144.339f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("21380", "23915")
        ));

        list.add(TalentNodeDef.of(
                "41665", -5859.21f, 908.633f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("50562", "55789")
        ));

        list.add(TalentNodeDef.of(
                "44223", 1886.06f, -1444.43f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("20024", "49220")
        ));

        list.add(TalentNodeDef.of(
                "45609", 5109.9f, 3556.01f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("3458", "56776")
        ));

        list.add(TalentNodeDef.of(
                "535", 5193.02f, -4742.69f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("20677", "34621")
        ));

        list.add(TalentNodeDef.of(
                "55596", -8390.85f, -5181.53f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("39131", "45569", "8509")
        ));

        list.add(TalentNodeDef.of(
                "55789", -5899.91f, 726.513f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("21017", "41665")
        ));

        list.add(TalentNodeDef.of(
                "59039", -9361.23f, -228.686f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(),
                List.of("18448", "28223")
        ));

        list.add(TalentNodeDef.of(
                "59061", -7937.59f, -5348.53f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY, 0.2)
                ),
                List.of("28267", "8509")
        ));

        list.add(TalentNodeDef.of(
                "60362", 10463.9f, -3717.25f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("29959", "56265")
        ));

        list.add(TalentNodeDef.of(
                "6100", -9182.44f, -634.224f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(),
                List.of("20963", "28223")
        ));

        list.add(TalentNodeDef.of(
                "630", 3703.13f, -2940.28f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("13419", "17548")
        ));

        list.add(TalentNodeDef.of(
                "6891", 10217.9f, -3859.28f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("29959", "56265")
        ));

        list.add(TalentNodeDef.of(
                "8509", -8019.59f, -5206.51f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY, 0.2)
                ),
                List.of("55596", "59061")
        ));

        list.add(TalentNodeDef.of(
                "9782", 5193.02f, -5200.3f,
                "critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("20677", "28021")
        ));

        list.add(TalentNodeDef.of(
                "52630", 8378.96f, -477.954f,
                "critical_damage_vs_full_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.4)
                ),
                List.of("28371", "61800")
        ));

        list.add(TalentNodeDef.of(
                "61800", 8508.37f, -477.954f,
                "critical_damage_vs_full_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.4)
                ),
                List.of("29527", "52630")
        ));

        list.add(TalentNodeDef.of(
                "30615", 9287.51f, -4857.08f,
                "critical_damage_when_consuming_a_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.2)
                ),
                List.of("3336", "65204")
        ));

        list.add(TalentNodeDef.of(
                "3336", 9171.54f, -5057.94f,
                "critical_damage_when_consuming_a_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.2)
                ),
                List.of("30615", "36231")
        ));

        list.add(TalentNodeDef.of(
                "36231", 9372.4f, -5173.91f,
                "critical_damage_when_consuming_a_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.2)
                ),
                List.of("31765", "3336")
        ));

        list.add(TalentNodeDef.of(
                "11836", 6589.56f, -2213.75f,
                "critical_vs_blinded", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES, 0.12)
                ),
                List.of("32721", "53471")
        ));

        list.add(TalentNodeDef.of(
                "53471", 6647.65f, -2381.19f,
                "critical_vs_blinded", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_BLINDED_ENEMIES, 0.12)
                ),
                List.of("11836", "11871")
        ));

        list.add(TalentNodeDef.of(
                "26572", 10290.9f, -317.595f,
                "criticals_vs_dazed_enemies", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_DAZED_ENEMIES, 0.12)
                ),
                List.of("21945", "47514")
        ));

        list.add(TalentNodeDef.of(
                "31763", 2202.72f, 11768.9f,
                "crossbow_critical_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS, 0.1)
                ),
                List.of("33415", "43155")
        ));

        list.add(TalentNodeDef.of(
                "33415", 2063.89f, 11866.1f,
                "crossbow_critical_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS, 0.1)
                ),
                List.of("31763", "6178")
        ));

        list.add(TalentNodeDef.of(
                "43155", 2299.93f, 11630.1f,
                "crossbow_critical_chance", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS, 0.1)
                ),
                List.of("30082", "31763", "7062")
        ));

        list.add(TalentNodeDef.of(
                "19998", 1559.69f, 11253.6f,
                "crossbow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_CROSSBOWS, 0.12)
                ),
                List.of("3717", "44430")
        ));

        list.add(TalentNodeDef.of(
                "3717", 1168.66f, 11146.2f,
                "crossbow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_CROSSBOWS, 0.12)
                ),
                List.of("19998", "35653", "55802")
        ));

        list.add(TalentNodeDef.of(
                "44430", 1949.12f, 11357.8f,
                "crossbow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_CROSSBOWS, 0.12)
                ),
                List.of("19998", "7062")
        ));

        list.add(TalentNodeDef.of(
                "12311", 1234.25f, 7902.62f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("3995", "64119")
        ));

        list.add(TalentNodeDef.of(
                "16680", 2329.03f, 11297.5f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("48137", "7062")
        ));

        list.add(TalentNodeDef.of(
                "25807", 1694.19f, 11468.7f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("53683", "65468")
        ));

        list.add(TalentNodeDef.of(
                "30082", 2092.43f, 11575.4f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("43155", "53683")
        ));

        list.add(TalentNodeDef.of(
                "3995", 1386.05f, 8019.11f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("12311", "39298")
        ));

        list.add(TalentNodeDef.of(
                "48137", 2257.4f, 11143.9f,
                "crossbow_reload_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CROSSBOW_RELOAD_SPEED, 0.15)
                ),
                List.of("16680", "33887")
        ));

        list.add(TalentNodeDef.of(
                "20467", 4786.73f, 9464.06f,
                "culling_strike_threshold", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD, 0.05)
                ),
                List.of("16786", "17702", "58312")
        ));

        list.add(TalentNodeDef.of(
                "49497", 5036.73f, 9130.06f,
                "culling_strike_threshold", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD, 0.05)
                ),
                List.of("23244", "58312")
        ));

        list.add(TalentNodeDef.of(
                "58312", 4911.73f, 9346.57f,
                "culling_strike_threshold", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD, 0.05)
                ),
                List.of("20467", "49497")
        ));

        list.add(TalentNodeDef.of(
                "21208", 6363.25f, -6771.11f,
                "curse_activation_speed_and_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.03),
                        TalentNodeGen.stat(StatIds.FASTER_CURSE_ACTIVATION, 0.1)
                ),
                List.of("4328", "56336")
        ));

        list.add(TalentNodeDef.of(
                "54725", 6488.25f, -7237.61f,
                "curse_activation_speed_and_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.03),
                        TalentNodeGen.stat(StatIds.FASTER_CURSE_ACTIVATION, 0.1)
                ),
                List.of("21748", "56336")
        ));

        list.add(TalentNodeDef.of(
                "56336", 6271.75f, -7112.61f,
                "curse_activation_speed_and_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.03),
                        TalentNodeGen.stat(StatIds.FASTER_CURSE_ACTIVATION, 0.1)
                ),
                List.of("21208", "54725")
        ));

        list.add(TalentNodeDef.of(
                "28229", 2497.88f, -5217.36f,
                "curse_area", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_OF_CURSES, 0.1)
                ),
                List.of("45230", "50485")
        ));

        list.add(TalentNodeDef.of(
                "45230", 2639.9f, -5135.36f,
                "curse_area", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_OF_CURSES, 0.1)
                ),
                List.of("28229", "8540")
        ));

        list.add(TalentNodeDef.of(
                "8540", 2579.88f, -4911.34f,
                "curse_area", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_OF_CURSES, 0.1)
                ),
                List.of("45230", "8569")
        ));

        list.add(TalentNodeDef.of(
                "22710", 6952.45f, -6862.61f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("26268", "45111")
        ));

        list.add(TalentNodeDef.of(
                "26268", 6735.94f, -6737.61f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("22710", "59214")
        ));

        list.add(TalentNodeDef.of(
                "32009", 1200.8f, -9885.51f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("36639", "36814")
        ));

        list.add(TalentNodeDef.of(
                "36639", 950.795f, -9885.51f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("32009", "62677")
        ));

        list.add(TalentNodeDef.of(
                "36814", 1325.8f, -9669.0f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("16499", "32009")
        ));

        list.add(TalentNodeDef.of(
                "45111", 6860.94f, -7204.12f,
                "curse_duration", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_DURATION, 0.2)
                ),
                List.of("14446", "22710")
        ));

        list.add(TalentNodeDef.of(
                "30061", 3411.99f, -7646.13f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("31908", "55420")
        ));

        list.add(TalentNodeDef.of(
                "31908", 3411.99f, -7482.13f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("30061", "5594")
        ));

        list.add(TalentNodeDef.of(
                "37991", 820.745f, -9412.83f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("40345", "52254")
        ));

        list.add(TalentNodeDef.of(
                "42290", 1195.74f, -9196.33f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("38732", "52254")
        ));

        list.add(TalentNodeDef.of(
                "52254", 945.745f, -9196.33f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("37991", "42290")
        ));

        list.add(TalentNodeDef.of(
                "55420", 3269.96f, -7728.13f,
                "curse_effect", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CURSE_BUFF_MAGNITUDE, 0.06)
                ),
                List.of("30061", "50881", "55668")
        ));

        list.add(TalentNodeDef.of(
                "36474", -5842.29f, -8243.17f,
                "curse_effect_on_self", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.15)
                ),
                List.of("24767", "31925")
        ));

        list.add(TalentNodeDef.of(
                "38596", -6300.11f, -8244.85f,
                "curse_effect_on_self", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.15)
                ),
                List.of("31925", "35408")
        ));

        list.add(TalentNodeDef.of(
                "50107", 3127.94f, -7482.13f,
                "curse_effect_on_self", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.15)
                ),
                List.of("50881", "5594")
        ));

        list.add(TalentNodeDef.of(
                "50881", 3127.94f, -7646.13f,
                "curse_effect_on_self", "passives_curseeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.15)
                ),
                List.of("50107", "55420")
        ));

        list.add(TalentNodeDef.of(
                "5066", -1955.32f, 4288.32f,
                "curse_effect_on_you", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.1)
                ),
                List.of("51788", "6714")
        ));

        list.add(TalentNodeDef.of(
                "51788", -1955.32f, 4124.32f,
                "curse_effect_on_you", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.1)
                ),
                List.of("10499", "5066")
        ));

        list.add(TalentNodeDef.of(
                "6714", -2097.34f, 4370.32f,
                "curse_effect_on_you_and_life_regeneration_rate", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_CURSES_ON_YOU, -0.05)
                ),
                List.of("41646", "42583", "5066")
        ));

        list.add(TalentNodeDef.of(
                "30973", 10237.3f, -4777.44f,
                "dagger_critical_chance", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS, 0.1)
                ),
                List.of("3419", "49320")
        ));

        list.add(TalentNodeDef.of(
                "49320", 10446.2f, -4843.3f,
                "dagger_critical_chance", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS, 0.1)
                ),
                List.of("30973", "52215")
        ));

        list.add(TalentNodeDef.of(
                "52215", 10794.6f, -5031.25f,
                "dagger_critical_chance", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_DAGGERS, 0.1)
                ),
                List.of("49320", "56366")
        ));

        list.add(TalentNodeDef.of(
                "35755", 10905.9f, -5253.92f,
                "dagger_critical_damage", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS, 0.1)
                ),
                List.of("54058", "56366")
        ));

        list.add(TalentNodeDef.of(
                "54058", 11048.0f, -5335.92f,
                "dagger_critical_damage", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS, 0.1)
                ),
                List.of("35755", "4423", "62001")
        ));

        list.add(TalentNodeDef.of(
                "3419", 10041.5f, -4728.01f,
                "dagger_damage", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_DAGGERS, 0.1)
                ),
                List.of("20429", "30973", "722")
        ));

        list.add(TalentNodeDef.of(
                "20429", 10159.2f, -4912.73f,
                "dagger_speed", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_DAGGERS, 0.03)
                ),
                List.of("28797", "3419")
        ));

        list.add(TalentNodeDef.of(
                "28797", 10320.6f, -5060.7f,
                "dagger_speed", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_DAGGERS, 0.03)
                ),
                List.of("20429", "632")
        ));

        list.add(TalentNodeDef.of(
                "632", 10657.3f, -5268.96f,
                "dagger_speed", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_DAGGERS, 0.03)
                ),
                List.of("28797", "56366")
        ));

        list.add(TalentNodeDef.of(
                "18822", -6280.27f, -3151.71f,
                "damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1)
                ),
                List.of("21567", "47790", "49938")
        ));

        list.add(TalentNodeDef.of(
                "2200", 5063.0f, 6552.73f,
                "damage", "passives_azmeriwildbear",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1)
                ),
                List.of("35689", "61976")
        ));

        list.add(TalentNodeDef.of(
                "25570", -3604.52f, -11024.1f,
                "damage", "passives_damage_blue",
                List.of(),
                List.of("21549", "27572", "37694", "44560", "47168", "64083")
        ));

        list.add(TalentNodeDef.of(
                "35689", 5197.5f, 6819.76f,
                "damage", "passives_azmeriwildbear",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1)
                ),
                List.of("2200", "34543")
        ));

        list.add(TalentNodeDef.of(
                "60085", 1493.63f, 10472.4f,
                "damage", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1)
                ),
                List.of("11667", "15839", "1915", "55802", "6266")
        ));

        list.add(TalentNodeDef.of(
                "6266", 1847.18f, 10472.4f,
                "damage", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_DEMONS, 0.1)
                ),
                List.of("48617", "60085")
        ));

        list.add(TalentNodeDef.of(
                "15969", -2076.24f, -5035.25f,
                "damage_against_ailments", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.12)
                ),
                List.of("15838", "41129", "59376")
        ));

        list.add(TalentNodeDef.of(
                "21572", 5873.12f, 6361.42f,
                "damage_against_ailments", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.12)
                ),
                List.of("6660", "7526")
        ));

        list.add(TalentNodeDef.of(
                "24338", -2190.33f, -5348.26f,
                "damage_against_ailments", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.12)
                ),
                List.of("41129", "48581")
        ));

        list.add(TalentNodeDef.of(
                "41129", -2160.99f, -5181.83f,
                "damage_against_ailments", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.12)
                ),
                List.of("15969", "24338")
        ));

        list.add(TalentNodeDef.of(
                "6660", 6015.15f, 6607.42f,
                "damage_against_ailments", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_AFFECTED_BY_ELEMENTAL_AILMENTS, 0.12)
                ),
                List.of("21572", "39050")
        ));

        list.add(TalentNodeDef.of(
                "51369", -4870.82f, 1520.57f,
                "damage_against_burning_enemies", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES, 0.14)
                ),
                List.of("27373", "45503", "56061")
        ));

        list.add(TalentNodeDef.of(
                "56061", -5034.82f, 1684.57f,
                "damage_against_burning_enemies", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BURNING_ENEMIES, 0.14)
                ),
                List.of("51369", "6544")
        ));

        list.add(TalentNodeDef.of(
                "11410", 842.92f, 6551.48f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("21755", "61487")
        ));

        list.add(TalentNodeDef.of(
                "17924", -5732.54f, 6678.42f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("51867", "64995")
        ));
        return list;
    }
}
