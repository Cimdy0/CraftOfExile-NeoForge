package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes17 {

    private MinorNodes17() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "5048", 9533.48f, 5975.93f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("14882", "261")
        ));

        list.add(TalentNodeDef.of(
                "53595", 11032.8f, -1601.81f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("1723", "29458", "38628")
        ));

        list.add(TalentNodeDef.of(
                "59644", 6911.88f, 689.942f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("22517", "42959")
        ));

        list.add(TalentNodeDef.of(
                "64325", 9026.76f, -1551.07f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("45304", "61119")
        ));

        list.add(TalentNodeDef.of(
                "6951", 5871.19f, 2826.09f,
                "poison_damage", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("23608", "35901")
        ));

        list.add(TalentNodeDef.of(
                "1723", 11188.4f, -1804.62f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("2134", "53595")
        ));

        list.add(TalentNodeDef.of(
                "24401", 6192.19f, 2536.84f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("23608", "63759")
        ));

        list.add(TalentNodeDef.of(
                "26565", 6526.19f, 2536.84f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("40024", "63759")
        ));

        list.add(TalentNodeDef.of(
                "28976", 11230.9f, -1058.34f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.1)
                ),
                List.of("14267", "23374", "29458")
        ));

        list.add(TalentNodeDef.of(
                "33815", 4763.72f, 1232.79f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("35644", "4083")
        ));

        list.add(TalentNodeDef.of(
                "45304", 8606.31f, -1873.69f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("33463", "6078", "64325")
        ));

        list.add(TalentNodeDef.of(
                "61119", 8776.31f, -1873.69f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("6078", "63431", "64325")
        ));

        list.add(TalentNodeDef.of(
                "9703", 11522.4f, -1804.62f,
                "poison_duration", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, 0.1)
                ),
                List.of("2134", "6030")
        ));

        list.add(TalentNodeDef.of(
                "61263", 5137.46f, 332.837f,
                "poison_duration_taken", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION_ON_YOU, -0.1)
                ),
                List.of("22115", "4544")
        ));

        list.add(TalentNodeDef.of(
                "24812", 3606.17f, -3575.34f,
                "power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.2)
                ),
                List.of("56360", "64643")
        ));

        list.add(TalentNodeDef.of(
                "56360", 3405.31f, -3776.2f,
                "power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.2)
                ),
                List.of("24812", "58329", "64643")
        ));

        list.add(TalentNodeDef.of(
                "64643", 3679.69f, -3849.72f,
                "power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.2)
                ),
                List.of("24812", "27176", "56360")
        ));

        list.add(TalentNodeDef.of(
                "13777", -8282.51f, -4427.82f,
                "power_charge_duration_and_energy", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.1)
                ),
                List.of("20791", "38532")
        ));

        list.add(TalentNodeDef.of(
                "20791", -8084.54f, -4401.76f,
                "power_charge_duration_and_energy", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY, 0.1)
                ),
                List.of("11741", "13777")
        ));

        list.add(TalentNodeDef.of(
                "11463", 8320.97f, 2188.17f,
                "presence_area", "passives_auraareaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, -0.25)
                ),
                List.of("17664")
        ));

        list.add(TalentNodeDef.of(
                "17792", 10486.8f, 649.94f,
                "presence_area", "passives_azmeriprimalmonkey",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("42226", "48734")
        ));

        list.add(TalentNodeDef.of(
                "21861", -6555.25f, -308.275f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("60551", "65243")
        ));

        list.add(TalentNodeDef.of(
                "26194", 729.705f, 6763.94f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("37872", "53166")
        ));

        list.add(TalentNodeDef.of(
                "33348", 8521.83f, 2389.03f,
                "presence_area", "passives_auraareaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.25)
                ),
                List.of("17664")
        ));

        list.add(TalentNodeDef.of(
                "45162", -8381.62f, 7618.79f,
                "presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("24801", "63031", "8171")
        ));

        list.add(TalentNodeDef.of(
                "4921", -3866.88f, 7830.16f,
                "presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("13524", "50023")
        ));

        list.add(TalentNodeDef.of(
                "52807", -6309.25f, -166.247f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.15)
                ),
                List.of("14654", "60551", "61836")
        ));

        list.add(TalentNodeDef.of(
                "53166", 552.928f, 6837.16f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("19223", "26194")
        ));

        list.add(TalentNodeDef.of(
                "53675", -4694.67f, -6646.36f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.15)
                ),
                List.of("15782", "59498")
        ));

        list.add(TalentNodeDef.of(
                "5740", 3800.66f, 10787.4f,
                "presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("10671", "40687")
        ));

        list.add(TalentNodeDef.of(
                "59498", -4612.67f, -6340.34f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("53675", "54814")
        ));

        list.add(TalentNodeDef.of(
                "60551", -6473.25f, -166.247f,
                "presence_area", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("21861", "52807")
        ));

        list.add(TalentNodeDef.of(
                "8171", -8256.61f, 7402.29f,
                "presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.2)
                ),
                List.of("31545", "45162")
        ));

        list.add(TalentNodeDef.of(
                "55664", 8073.32f, 5329.48f,
                "presence_area_and_companion_area", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.1),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_AREA_OF_EFFECT, 0.1)
                ),
                List.of("31826", "34702")
        ));

        list.add(TalentNodeDef.of(
                "1995", 4791.75f, 2293.82f,
                "projectile_ailment_chance", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_WITH_PROJECTILES, 0.2)
                ),
                List.of("30341")
        ));

        list.add(TalentNodeDef.of(
                "61347", -953.069f, 7675.25f,
                "projectile_critical_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.12)
                ),
                List.of("47157", "48215", "64488")
        ));

        list.add(TalentNodeDef.of(
                "64488", -992.798f, 7373.47f,
                "projectile_critical_chance", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.12)
                ),
                List.of("48215", "61347", "7201")
        ));

        list.add(TalentNodeDef.of(
                "10648", 7512.05f, 4152.81f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.15)
                ),
                List.of("11825", "26400")
        ));

        list.add(TalentNodeDef.of(
                "12078", 7698.37f, -2543.31f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("41877", "53771", "57462")
        ));

        list.add(TalentNodeDef.of(
                "13081", 451.065f, 2535.41f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("14254", "44605", "48588")
        ));

        list.add(TalentNodeDef.of(
                "14045", 3647.31f, 5022.05f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("33848", "36070", "65176")
        ));

        list.add(TalentNodeDef.of(
                "18049", 7434.9f, 4574.26f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM, 0.15)
                ),
                List.of("47374", "5802")
        ));

        list.add(TalentNodeDef.of(
                "19342", 2189.18f, 6911.84f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("17553", "19573", "27493", "46296")
        ));

        list.add(TalentNodeDef.of(
                "22049", 3638.69f, 1581.38f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("55429", "60505")
        ));

        list.add(TalentNodeDef.of(
                "22795", 2932.6f, 1160.18f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("28992", "42781")
        ));

        list.add(TalentNodeDef.of(
                "2455", 334.748f, 2215.84f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.08)
                ),
                List.of("48588", "59915")
        ));

        list.add(TalentNodeDef.of(
                "26400", 7750.05f, 4152.81f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.15)
                ),
                List.of("10648", "8904")
        ));

        list.add(TalentNodeDef.of(
                "27493", 2322.55f, 6988.84f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("17118", "19342")
        ));

        list.add(TalentNodeDef.of(
                "28800", -5085.88f, -2565.83f,
                "projectile_damage", "passives_projectiledmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("2955", "55308")
        ));

        list.add(TalentNodeDef.of(
                "2955", -5085.88f, -2729.83f,
                "projectile_damage", "passives_projectiledmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("28002", "28800")
        ));

        list.add(TalentNodeDef.of(
                "33053", 2252.83f, 2770.25f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("10909", "17726", "4844")
        ));

        list.add(TalentNodeDef.of(
                "33245", 7427.82f, 2075.69f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("31918", "45331", "6792", "9151")
        ));

        list.add(TalentNodeDef.of(
                "35234", 1771.39f, 800.31f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("35660", "56651", "6789")
        ));

        list.add(TalentNodeDef.of(
                "4313", 2262.88f, 865.015f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.08)
                ),
                List.of("28992", "6789")
        ));

        list.add(TalentNodeDef.of(
                "43578", 523.845f, 3886.32f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("41210", "49657")
        ));

        list.add(TalentNodeDef.of(
                "47157", -1169.57f, 7800.25f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.15)
                ),
                List.of("516", "54818", "61347")
        ));

        list.add(TalentNodeDef.of(
                "47374", 7315.9f, 4368.14f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM, 0.15)
                ),
                List.of("11825", "18049")
        ));

        list.add(TalentNodeDef.of(
                "4844", 2442.54f, 2745.27f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("33053", "58013")
        ));

        list.add(TalentNodeDef.of(
                "48588", 413.943f, 2367.97f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.08)
                ),
                List.of("13081", "2455")
        ));

        list.add(TalentNodeDef.of(
                "49406", 196.307f, 9909.76f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("51944", "52125")
        ));

        list.add(TalentNodeDef.of(
                "516", -1386.08f, 7675.25f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.15)
                ),
                List.of("47157", "48215", "7201")
        ));

        list.add(TalentNodeDef.of(
                "51944", 305.201f, 9800.87f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("43829", "47683", "49406", "51728")
        ));

        list.add(TalentNodeDef.of(
                "52361", 7984.38f, -3061.96f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("26107", "53771")
        ));

        list.add(TalentNodeDef.of(
                "53771", 8104.59f, -2941.75f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("12078", "52361")
        ));

        list.add(TalentNodeDef.of(
                "55429", 3398.9f, 1369.34f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("22049", "42781")
        ));

        list.add(TalentNodeDef.of(
                "56651", 1535.77f, 727.985f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("35234", "38143", "50459")
        ));

        list.add(TalentNodeDef.of(
                "58013", 2594.34f, 2861.75f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("4844", "50795")
        ));

        list.add(TalentNodeDef.of(
                "59028", 630.601f, 3487.89f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("41210", "8092")
        ));

        list.add(TalentNodeDef.of(
                "59915", 136.855f, 1689.33f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("2455", "50986", "7741", "97")
        ));

        list.add(TalentNodeDef.of(
                "65176", 3489.69f, 5113.05f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("14045", "42250")
        ));

        list.add(TalentNodeDef.of(
                "6789", 2055.32f, 706.603f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.08)
                ),
                List.of("35234", "4313")
        ));

        list.add(TalentNodeDef.of(
                "6792", 7595.82f, 2075.69f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("2408", "33245")
        ));

        list.add(TalentNodeDef.of(
                "7201", -1346.35f, 7373.47f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM, 0.15)
                ),
                List.of("48215", "516", "64488")
        ));

        list.add(TalentNodeDef.of(
                "8092", 549.761f, 3292.73f,
                "projectile_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.1)
                ),
                List.of("44605", "59028")
        ));

        list.add(TalentNodeDef.of(
                "46705", 3771.08f, 2672.84f,
                "projectile_damage_if_melee_hit", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.15)
                ),
                List.of("12822", "53539")
        ));

        list.add(TalentNodeDef.of(
                "53539", 3941.08f, 2672.84f,
                "projectile_damage_if_melee_hit", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.15)
                ),
                List.of("13379", "28050", "46705")
        ));

        list.add(TalentNodeDef.of(
                "1680", 5915.86f, 5321.85f,
                "projectile_parry_range", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PARRY_RANGE, 0.2)
                ),
                List.of("24843", "29320")
        ));

        list.add(TalentNodeDef.of(
                "17553", 2141.41f, 6680.14f,
                "projectile_pierce", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU, 0.25)
                ),
                List.of("19342", "38479")
        ));

        list.add(TalentNodeDef.of(
                "19573", 1964.63f, 6986.33f,
                "projectile_pierce", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU, 0.25)
                ),
                List.of("19342", "38479")
        ));

        list.add(TalentNodeDef.of(
                "33713", 7710.0f, -3135.48f,
                "projectile_speed", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08)
                ),
                List.of("26107", "57462")
        ));

        list.add(TalentNodeDef.of(
                "33848", 3744.91f, 4819.86f,
                "projectile_speed", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08)
                ),
                List.of("14045", "47677")
        ));

        list.add(TalentNodeDef.of(
                "38313", -5227.91f, -2483.83f,
                "projectile_speed", "passives_projectiledmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.1)
                ),
                List.of("55308")
        ));

        list.add(TalentNodeDef.of(
                "47677", 3974.8f, 4758.26f,
                "projectile_speed", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08)
                ),
                List.of("33848", "9472")
        ));

        list.add(TalentNodeDef.of(
                "57462", 7545.8f, -3091.48f,
                "projectile_speed", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.08)
                ),
                List.of("12078", "33713")
        ));

        list.add(TalentNodeDef.of(
                "58416", 7706.83f, 1641.5f,
                "projectile_speed", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.1)
                ),
                List.of("30132")
        ));

        list.add(TalentNodeDef.of(
                "17417", -3876.75f, 10785.5f,
                "projectile_speed_and_physical_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("27999", "3652", "43843")
        ));

        list.add(TalentNodeDef.of(
                "3652", -4007.59f, 10995.0f,
                "projectile_speed_and_physical_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("17417", "56714")
        ));

        list.add(TalentNodeDef.of(
                "43843", -3597.03f, 10624.0f,
                "projectile_speed_and_physical_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.08)
                ),
                List.of("17417", "64471")
        ));

        list.add(TalentNodeDef.of(
                "37946", 5112.65f, 2295.51f,
                "projectile_stun_buildup", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_STUN_BUILDUP, 0.2)
                ),
                List.of("30341")
        ));

        list.add(TalentNodeDef.of(
                "63679", 5044.86f, 7415.55f,
                "projectile_stun_buildup", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_STUN_BUILDUP, 0.18)
                ),
                List.of("20008", "64345")
        ));

        list.add(TalentNodeDef.of(
                "13123", 950.935f, -8590.68f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("14258", "62723")
        ));

        list.add(TalentNodeDef.of(
                "14686", -4769.98f, -8681.79f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("48552", "53795")
        ));

        list.add(TalentNodeDef.of(
                "19318", -4614.94f, -8423.88f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_PUPPET_MASTER, 0.2)
                ),
                List.of("53795")
        ));

        list.add(TalentNodeDef.of(
                "53324", -4239.24f, -8579.5f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("53795")
        ));

        list.add(TalentNodeDef.of(
                "53795", -4528.5f, -8746.5f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("14686", "19318", "53324", "62210", "64804")
        ));

        list.add(TalentNodeDef.of(
                "54733", 339.792f, -8849.72f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_PUPPET_MASTER, 0.15)
                ),
                List.of("5314", "7104")
        ));

        list.add(TalentNodeDef.of(
                "62723", 952.239f, -8335.81f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("13123", "38732")
        ));

        list.add(TalentNodeDef.of(
                "64804", -4361.5f, -8457.24f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_EACH_PUPPET_MASTER_STACK, 0.25)
                ),
                List.of("53795")
        ));

        list.add(TalentNodeDef.of(
                "7104", 584.454f, -8871.89f,
                "puppet_master_chance", "passives_puppeteernode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_PUPPET_MASTER, 0.15)
                ),
                List.of("14258", "54733")
        ));

        list.add(TalentNodeDef.of(
                "31449", 10463.3f, -2898.28f,
                "quarterstaff_critical_chance", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_QUARTERSTAVES, 0.1)
                ),
                List.of("326", "9444")
        ));

        list.add(TalentNodeDef.of(
                "326", 10504.0f, -3050.26f,
                "quarterstaff_critical_chance", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_QUARTERSTAVES, 0.1)
                ),
                List.of("2113", "31449")
        ));

        list.add(TalentNodeDef.of(
                "52399", 10573.9f, -2868.64f,
                "quarterstaff_critical_damage", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES, 0.18)
                ),
                List.of("59694", "9444")
        ));

        list.add(TalentNodeDef.of(
                "59694", 10614.8f, -3021.26f,
                "quarterstaff_critical_damage", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_QUARTERSTAVES, 0.18)
                ),
                List.of("2113", "52399")
        ));

        list.add(TalentNodeDef.of(
                "61632", 10802.1f, -4162.67f,
                "quarterstaff_freeze_and_daze_buildup", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP_WITH_QUARTERSTAVES, 0.2)
                ),
                List.of("34316", "64700")
        ));

        list.add(TalentNodeDef.of(
                "64700", 10769.3f, -4040.33f,
                "quarterstaff_freeze_and_daze_buildup", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP_WITH_QUARTERSTAVES, 0.2)
                ),
                List.of("37514", "61632")
        ));

        list.add(TalentNodeDef.of(
                "11598", 10688.0f, -3515.65f,
                "quarterstaff_speed", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_QUARTERSTAVES, 0.03)
                ),
                List.of("15775", "44516", "4536")
        ));

        list.add(TalentNodeDef.of(
                "44516", 10645.5f, -3357.24f,
                "quarterstaff_speed", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_QUARTERSTAVES, 0.03)
                ),
                List.of("11598", "2113")
        ));

        list.add(TalentNodeDef.of(
                "4536", 10730.4f, -3674.07f,
                "quarterstaff_speed", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_QUARTERSTAVES, 0.03)
                ),
                List.of("11598", "37514")
        ));

        list.add(TalentNodeDef.of(
                "2361", 10912.7f, -4133.03f,
                "quarterstaff_stun_and_knockback", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_QUARTERSTAVES, 0.2)
                ),
                List.of("32442", "34316")
        ));

        list.add(TalentNodeDef.of(
                "32442", 10878.5f, -4005.64f,
                "quarterstaff_stun_and_knockback", "passives_damagestaff",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_KNOCKBACK_DISTANCE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_QUARTERSTAVES, 0.2)
                ),
                List.of("2361", "37514")
        ));

        list.add(TalentNodeDef.of(
                "1020", 7841.16f, 1306.31f,
                "quiver_effect", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.06)
                ),
                List.of("35696", "54631")
        ));

        list.add(TalentNodeDef.of(
                "12498", 4953.65f, 2387.3f,
                "quiver_effect", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.06)
                ),
                List.of("30341", "61356")
        ));

        list.add(TalentNodeDef.of(
                "54631", 7664.38f, 1483.08f,
                "quiver_effect", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.06)
                ),
                List.of("1020", "30132")
        ));

        list.add(TalentNodeDef.of(
                "61356", 4953.65f, 2637.3f,
                "quiver_effect", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BONUSES_GAIN_FROM_EQUIPPED_QUIVER, 0.06)
                ),
                List.of("12498", "63888")
        ));

        list.add(TalentNodeDef.of(
                "270", -8500.77f, -6794.46f,
                "rage_decay", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_LOSS_OF_RAGE_IS_SLOWER, -0.15)
                ),
                List.of("27980", "56219")
        ));

        list.add(TalentNodeDef.of(
                "56219", -8539.77f, -7010.96f,
                "rage_decay", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.INHERENT_LOSS_OF_RAGE_IS_SLOWER, -0.15)
                ),
                List.of("270", "52764")
        ));

        list.add(TalentNodeDef.of(
                "13845", -8776.24f, -545.925f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("28408", "28892")
        ));

        list.add(TalentNodeDef.of(
                "17625", -6062.5f, -3045.95f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("10873", "47790")
        ));

        list.add(TalentNodeDef.of(
                "27740", -6861.42f, 8507.21f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("12255", "35792")
        ));

        list.add(TalentNodeDef.of(
                "36389", -5002.78f, 4783.44f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("53989", "61472")
        ));

        list.add(TalentNodeDef.of(
                "4624", -3243.25f, 4190.61f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("49550", "61935")
        ));

        list.add(TalentNodeDef.of(
                "47790", -5997.79f, -3158.02f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("17625", "18822")
        ));

        list.add(TalentNodeDef.of(
                "53989", -4787.99f, 4883.6f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("29372", "36389")
        ));

        list.add(TalentNodeDef.of(
                "55746", -3268.23f, 4000.9f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("13241", "61935")
        ));

        list.add(TalentNodeDef.of(
                "60068", -8776.24f, -211.925f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("37869", "47263", "55925")
        ));

        list.add(TalentNodeDef.of(
                "61935", -3484.73f, 4125.9f,
                "rage_on_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("4624", "55746")
        ));

        list.add(TalentNodeDef.of(
                "14205", -3986.59f, 5167.16f,
                "rage_on_ignite", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET, 1.0)
                ),
                List.of("25753", "32932")
        ));

        list.add(TalentNodeDef.of(
                "7720", -4427.34f, 4958.27f,
                "rage_on_ignite", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET, 1.0)
                ),
                List.of("29372", "32932")
        ));

        list.add(TalentNodeDef.of(
                "14509", -10507.0f, -3092.24f,
                "rage_on_melee_hit", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("589", "9323")
        ));

        list.add(TalentNodeDef.of(
                "46399", -10528.7f, -2528.14f,
                "rage_on_melee_hit", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("41768", "50820", "589")
        ));

        list.add(TalentNodeDef.of(
                "589", -10351.9f, -2834.33f,
                "rage_on_melee_hit", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0)
                ),
                List.of("14509", "46399")
        ));

        list.add(TalentNodeDef.of(
                "11656", -9872.82f, 1326.49f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("53822", "9106")
        ));

        list.add(TalentNodeDef.of(
                "27980", -8664.77f, -6630.46f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("270", "28981", "33722")
        ));

        list.add(TalentNodeDef.of(
                "28981", -8828.77f, -6794.46f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("27980", "34871")
        ));

        list.add(TalentNodeDef.of(
                "34871", -8789.77f, -7010.96f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("28981", "52764")
        ));

        list.add(TalentNodeDef.of(
                "53822", -10214.3f, 1418.0f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("11656", "1200")
        ));

        list.add(TalentNodeDef.of(
                "56342", -5952.13f, 8688.45f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("52373", "7341")
        ));

        list.add(TalentNodeDef.of(
                "9106", -9964.33f, 984.989f,
                "rage_when_hit", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, 2.0)
                ),
                List.of("11656", "54937")
        ));
        list.add(TalentNodeDef.of(
                "50459", 1274.68f, 735.845f,
                "ranger", "passives_blankdex",
                List.of(),
                List.of("13828", "56651")
        ));

        list.add(TalentNodeDef.of(
                "34626", -8068.92f, 2744.15f,
                "recover_life_on_consuming_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED, 0.02)
                ),
                List.of("38365", "4527", "61142")
        ));

        list.add(TalentNodeDef.of(
                "38365", -8352.97f, 2744.15f,
                "recover_life_on_consuming_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED, 0.02)
                ),
                List.of("34626", "46499", "61142")
        ));

        list.add(TalentNodeDef.of(
                "61142", -8210.95f, 2990.15f,
                "recover_life_on_consuming_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED, 0.02)
                ),
                List.of("34626", "38365")
        ));

        list.add(TalentNodeDef.of(
                "25890", -6744.5f, -8082.38f,
                "recover_mana_on_consuming_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE, 0.02)
                ),
                List.of("26863", "54378", "59093")
        ));

        list.add(TalentNodeDef.of(
                "26863", -6498.5f, -7940.35f,
                "recover_mana_on_consuming_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE, 0.02)
                ),
                List.of("25890", "54378", "58198")
        ));

        list.add(TalentNodeDef.of(
                "54378", -6498.5f, -8224.4f,
                "recover_mana_on_consuming_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE, 0.02)
                ),
                List.of("25890", "26863")
        ));

        list.add(TalentNodeDef.of(
                "35688", 132.005f, 12159.5f,
                "reduced_attribute_requirements", "passives_ascendants_skillpoint",
                List.of(
                        TalentNodeGen.stat(StatIds.EQUIPMENT_AND_SKILL_GEMS_HAVE_INC_ATTRIBUTES_REQUIREMENTS, -0.04)
                ),
                List.of("16618", "38138")
        ));

        list.add(TalentNodeDef.of(
                "38138", 7.0049f, 12600.0f,
                "reduced_attribute_requirements", "passives_ascendants_skillpoint",
                List.of(
                        TalentNodeGen.stat(StatIds.EQUIPMENT_AND_SKILL_GEMS_HAVE_INC_ATTRIBUTES_REQUIREMENTS, -0.04)
                ),
                List.of("35688", "45278")
        ));

        list.add(TalentNodeDef.of(
                "45278", -117.995f, 12159.5f,
                "reduced_attribute_requirements", "passives_ascendants_skillpoint",
                List.of(
                        TalentNodeGen.stat(StatIds.EQUIPMENT_AND_SKILL_GEMS_HAVE_INC_ATTRIBUTES_REQUIREMENTS, -0.04)
                ),
                List.of("38138", "57047")
        ));

        list.add(TalentNodeDef.of(
                "14127", 3740.14f, -10686.8f,
                "reduced_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.08)
                ),
                List.of("10131", "44690")
        ));

        list.add(TalentNodeDef.of(
                "37956", -3921.6f, 394.725f,
                "reduced_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.08)
                ),
                List.of("35581", "59006")
        ));

        list.add(TalentNodeDef.of(
                "44690", 3941.0f, -10887.7f,
                "reduced_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.08)
                ),
                List.of("14127", "55568")
        ));

        list.add(TalentNodeDef.of(
                "59006", -4296.44f, 407.485f,
                "reduced_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, -0.08)
                ),
                List.of("37956", "48631")
        ));

        list.add(TalentNodeDef.of(
                "11666", -12294.1f, -1187.7f,
                "reduced_movement_penalty", "passives_movementspeedandevasion",
                List.of(),
                List.of("13950", "60708")
        ));

        list.add(TalentNodeDef.of(
                "13950", -11977.2f, -1272.59f,
                "reduced_movement_penalty", "passives_movementspeedandevasion",
                List.of(),
                List.of("11666", "21218")
        ));
        return list;
    }
}
