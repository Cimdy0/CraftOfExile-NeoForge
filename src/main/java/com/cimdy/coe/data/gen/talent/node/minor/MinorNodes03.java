package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes03 {

    private MinorNodes03() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "23708", -5140.8f, 995.045f,
                "armor_while_bleeding", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_BLEEDING, 0.3)
                ),
                List.of("35393", "3896")
        ));

        list.add(TalentNodeDef.of(
                "35393", -5058.8f, 603.017f,
                "armor_while_bleeding", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_BLEEDING, 0.3)
                ),
                List.of("23708", "8693")
        ));

        list.add(TalentNodeDef.of(
                "8693", -4899.32f, 809.75f,
                "armor_while_bleeding", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_BLEEDING, 0.3)
                ),
                List.of("35393", "53405")
        ));

        list.add(TalentNodeDef.of(
                "31566", -553.071f, 8504.62f,
                "armor_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SURROUNDED, 0.3)
                ),
                List.of("24766", "54818", "58038", "7049")
        ));

        list.add(TalentNodeDef.of(
                "7049", -789.244f, 8740.79f,
                "armor_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SURROUNDED, 0.3)
                ),
                List.of("22556", "31566")
        ));

        list.add(TalentNodeDef.of(
                "48935", -6610.4f, -475.569f,
                "attack_added_lighting_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.ADD_TO_LIGHTNING_DAMAGE_TO_ATTACKS, 1.0)
                ),
                List.of("26291", "61367")
        ));

        list.add(TalentNodeDef.of(
                "61367", -6764.57f, -97.3668f,
                "attack_added_lighting_damage", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.ADD_TO_LIGHTNING_DAMAGE_TO_ATTACKS, 1.0)
                ),
                List.of("48935", "64239")
        ));

        list.add(TalentNodeDef.of(
                "20236", 4542.03f, -4689.38f,
                "attack_and_cast_speed_on_critical", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, 0.03),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, 0.03)
                ),
                List.of("13724", "4346")
        ));

        list.add(TalentNodeDef.of(
                "16413", -7852.12f, -828.385f,
                "attack_and_minion_damage", "passives_minionstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.08)
                ),
                List.of("3723", "42660")
        ));

        list.add(TalentNodeDef.of(
                "3723", -8076.15f, -888.413f,
                "attack_and_minion_damage", "passives_minionstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.08)
                ),
                List.of("16413", "17092", "38707")
        ));

        list.add(TalentNodeDef.of(
                "29098", -9082.18f, -4783.33f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("10727", "32549", "43588")
        ));

        list.add(TalentNodeDef.of(
                "32549", -9306.21f, -4723.31f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("29098", "32474", "61835")
        ));

        list.add(TalentNodeDef.of(
                "43588", -9142.21f, -5007.36f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("29098", "61835", "39131")
        ));

        list.add(TalentNodeDef.of(
                "44902", -8343.97f, -2673.64f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("511", "61429")
        ));

        list.add(TalentNodeDef.of(
                "511", -8252.47f, -2332.13f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("38923", "44902", "49734")
        ));

        list.add(TalentNodeDef.of(
                "61429", -8685.48f, -2582.13f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("38923", "44902", "61404")
        ));

        list.add(TalentNodeDef.of(
                "61835", -9366.23f, -4947.33f,
                "attack_and_spell_damage", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("43588", "32549", "56237")
        ));

        list.add(TalentNodeDef.of(
                "10474", -6862.15f, 1322.32f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("53089", "53785", "64443")
        ));

        list.add(TalentNodeDef.of(
                "11252", 8702.16f, -4731.04f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("25070", "39911")
        ));

        list.add(TalentNodeDef.of(
                "17584", -2347.27f, -926.219f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("12430", "6898")
        ));

        list.add(TalentNodeDef.of(
                "20989", -10052.4f, -157.131f,
                "attack_area", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("30141", "42984")
        ));

        list.add(TalentNodeDef.of(
                "21142", 5090.8f, -3553.16f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("55829", "7054")
        ));

        list.add(TalentNodeDef.of(
                "21184", -10111.8f, -570.082f,
                "attack_area", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("20251", "42984")
        ));

        list.add(TalentNodeDef.of(
                "24813", -1778.23f, 9977.41f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("20397", "9737")
        ));

        list.add(TalentNodeDef.of(
                "25070", 8586.19f, -4615.07f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("11252", "32903", "57821")
        ));

        list.add(TalentNodeDef.of(
                "25162", -10572.4f, 2189.32f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("59785", "64489")
        ));

        list.add(TalentNodeDef.of(
                "28446", -2072.89f, -852.699f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("12430", "50084")
        ));

        list.add(TalentNodeDef.of(
                "31295", -10262.2f, -1353.26f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("28982", "9352")
        ));

        list.add(TalentNodeDef.of(
                "31991", 4039.51f, 4870.33f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("36070", "9472")
        ));

        list.add(TalentNodeDef.of(
                "32903", 8628.64f, -4456.66f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("25070", "25361")
        ));

        list.add(TalentNodeDef.of(
                "35921", -7728.69f, 2099.04f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("46674", "5642")
        ));

        list.add(TalentNodeDef.of(
                "36070", 3871.22f, 5038.62f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("14045", "31991")
        ));

        list.add(TalentNodeDef.of(
                "36522", -1526.93f, 10232.3f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("3999", "54099", "9737")
        ));

        list.add(TalentNodeDef.of(
                "42984", -10359.6f, -352.305f,
                "attack_area", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("20989", "21184")
        ));

        list.add(TalentNodeDef.of(
                "4577", -1436.73f, 9885.91f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("20397", "3999")
        ));

        list.add(TalentNodeDef.of(
                "46674", -7870.72f, 2017.05f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("35921", "64807")
        ));

        list.add(TalentNodeDef.of(
                "53089", -6862.15f, 1152.32f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("10474", "16626", "9918")
        ));

        list.add(TalentNodeDef.of(
                "53785", -7196.15f, 988.325f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("10474", "1170", "23307")
        ));

        list.add(TalentNodeDef.of(
                "55829", 4849.31f, -3652.45f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("1420", "21142")
        ));

        list.add(TalentNodeDef.of(
                "64489", -10736.4f, 2189.32f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("25162", "6952")
        ));

        list.add(TalentNodeDef.of(
                "6952", -10818.4f, 2047.3f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("64489", "750")
        ));

        list.add(TalentNodeDef.of(
                "9352", -10112.5f, -1439.7f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("31295", "32071")
        ));

        list.add(TalentNodeDef.of(
                "9737", -2124.61f, 9887.21f,
                "attack_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.06)
                ),
                List.of("15590", "24813", "36522", "42410", "59480")
        ));

        list.add(TalentNodeDef.of(
                "24883", 7837.88f, -1503.79f,
                "attack_area_and_combo", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("26211", "44573")
        ));

        list.add(TalentNodeDef.of(
                "26211", 7591.88f, -1645.82f,
                "attack_area_and_combo", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("24883", "55377")
        ));

        list.add(TalentNodeDef.of(
                "55377", 7755.88f, -1809.82f,
                "attack_area_and_combo", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("26211", "33463")
        ));

        list.add(TalentNodeDef.of(
                "31626", 2973.61f, 10843.1f,
                "attack_area_and_flammability_magnitude", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("26952", "50516")
        ));

        list.add(TalentNodeDef.of(
                "41447", 3327.57f, 11251.5f,
                "attack_area_and_flammability_magnitude", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("13610", "2814")
        ));

        list.add(TalentNodeDef.of(
                "50516", 3163.31f, 10868.0f,
                "attack_area_and_flammability_magnitude", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("2814", "31626")
        ));

        list.add(TalentNodeDef.of(
                "37250", 4707.29f, -3406.45f,
                "attack_area_damage", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.1)
                ),
                List.of("1420", "47009")
        ));

        list.add(TalentNodeDef.of(
                "47009", 4914.02f, -3246.97f,
                "attack_area_damage", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.1)
                ),
                List.of("37250", "7054")
        ));

        list.add(TalentNodeDef.of(
                "19674", -6036.21f, 3652.89f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("41493", "57703")
        ));

        list.add(TalentNodeDef.of(
                "41493", -6116.29f, 3951.76f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("19674", "50253")
        ));

        list.add(TalentNodeDef.of(
                "52574", -2657.78f, 3224.91f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("55478", "5920")
        ));

        list.add(TalentNodeDef.of(
                "55478", -2399.0f, 3259.66f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("16168", "48006", "52574")
        ));

        list.add(TalentNodeDef.of(
                "5920", -2623.03f, 3483.68f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("51921", "52574")
        ));

        list.add(TalentNodeDef.of(
                "64807", -7870.72f, 1853.04f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("46674", "57405", "59945")
        ));

        list.add(TalentNodeDef.of(
                "7054", 5065.82f, -3363.45f,
                "attack_area_damage_and_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AREA_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.04)
                ),
                List.of("17088", "21142", "47009")
        ));

        list.add(TalentNodeDef.of(
                "31825", 10900.1f, 2504.32f,
                "attack_cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_COLD_DAMAGE, 0.12)
                ),
                List.of("16142", "3543")
        ));

        list.add(TalentNodeDef.of(
                "3543", 10585.4f, 2262.84f,
                "attack_cold_damage", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_COLD_DAMAGE, 0.12)
                ),
                List.of("27705", "31825")
        ));

        list.add(TalentNodeDef.of(
                "10267", 8162.51f, 1273.12f,
                "attack_cold_damage_and_freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_COLD_DAMAGE, 0.08)
                ),
                List.of("35696", "8456")
        ));

        list.add(TalentNodeDef.of(
                "8456", 8504.02f, 1364.62f,
                "attack_cold_damage_and_freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_COLD_DAMAGE, 0.08)
                ),
                List.of("10267", "38329")
        ));

        list.add(TalentNodeDef.of(
                "21070", -8183.02f, 4005.84f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("43650", "57388")
        ));

        list.add(TalentNodeDef.of(
                "23259", 3270.21f, 9796.74f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("22864", "34074", "57227")
        ));

        list.add(TalentNodeDef.of(
                "33946", 3562.95f, 10089.5f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("34074", "51048", "57227")
        ));

        list.add(TalentNodeDef.of(
                "34074", 3343.73f, 10071.1f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("23259", "33946")
        ));

        list.add(TalentNodeDef.of(
                "35048", -7966.51f, 4294.84f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("43650", "63114")
        ));

        list.add(TalentNodeDef.of(
                "54983", 11481.0f, 400.619f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("38537", "39369")
        ));

        list.add(TalentNodeDef.of(
                "57227", 3544.59f, 9870.26f,
                "attack_critical_chance", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS, 0.1)
                ),
                List.of("23259", "33946")
        ));

        list.add(TalentNodeDef.of(
                "19802", -3375.29f, 5198.13f,
                "attack_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.15)
                ),
                List.of("51299", "64399")
        ));

        list.add(TalentNodeDef.of(
                "26176", -7824.49f, 4212.84f,
                "attack_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.15)
                ),
                List.of("43650", "63114")
        ));

        list.add(TalentNodeDef.of(
                "2936", 11572.5f, 492.125f,
                "attack_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.15)
                ),
                List.of("13407", "39369")
        ));

        list.add(TalentNodeDef.of(
                "53386", -7966.51f, 3880.84f,
                "attack_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.15)
                ),
                List.of("43650", "57388")
        ));

        list.add(TalentNodeDef.of(
                "64399", -3558.31f, 5187.15f,
                "attack_critical_damage", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_FOR_ATTACK_DAMAGE, 0.15)
                ),
                List.of("19802", "2511")
        ));

        list.add(TalentNodeDef.of(
                "10671", 3679.11f, 10629.0f,
                "attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("23419", "51048", "5740")
        ));

        list.add(TalentNodeDef.of(
                "17101", 9777.78f, -1141.16f,
                "attack_damage", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("17724", "25362", "7888")
        ));

        list.add(TalentNodeDef.of(
                "17589", 9165.52f, -916.987f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("36085", "37548", "53150", "64462")
        ));

        list.add(TalentNodeDef.of(
                "17724", 10013.9f, -904.986f,
                "attack_damage", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("17101", "3640")
        ));

        list.add(TalentNodeDef.of(
                "18073", -7338.54f, 4744.39f,
                "attack_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.12)
                ),
                List.of("36027", "49952", "63114")
        ));

        list.add(TalentNodeDef.of(
                "19223", 479.705f, 7013.94f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("21755", "25213", "53166")
        ));

        list.add(TalentNodeDef.of(
                "21314", 4438.81f, 365.391f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("18818", "50912")
        ));

        list.add(TalentNodeDef.of(
                "244", 2486.83f, 11346.3f,
                "attack_damage", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.16)
                ),
                List.of("52354", "58814", "63731")
        ));

        list.add(TalentNodeDef.of(
                "26068", 4769.91f, 2982.72f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("37389", "63888")
        ));

        list.add(TalentNodeDef.of(
                "27910", 3855.19f, -2592.92f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("38676", "53938", "64056")
        ));

        list.add(TalentNodeDef.of(
                "29270", -3502.49f, 4766.42f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("46060", "7251")
        ));

        list.add(TalentNodeDef.of(
                "33866", 1963.38f, -1284.94f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("49220", "7576")
        ));

        list.add(TalentNodeDef.of(
                "36027", -7588.54f, 4744.39f,
                "attack_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.12)
                ),
                List.of("18073", "45090")
        ));

        list.add(TalentNodeDef.of(
                "36478", -4290.93f, -2254.08f,
                "attack_damage", "passives_increasedattackdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("32194", "43014", "48714")
        ));

        list.add(TalentNodeDef.of(
                "36576", 8607.17f, 4654.88f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("13799", "25055", "54984")
        ));

        list.add(TalentNodeDef.of(
                "37389", 4622.68f, 3067.72f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("26068", "28061", "37644")
        ));

        list.add(TalentNodeDef.of(
                "37691", 6107.7f, -3322.09f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("42750", "56453", "9050")
        ));

        list.add(TalentNodeDef.of(
                "38676", 3855.19f, -2428.92f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("27910", "56045")
        ));

        list.add(TalentNodeDef.of(
                "42750", 5752.9f, -2836.98f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("17088", "37691", "50635", "9050")
        ));

        list.add(TalentNodeDef.of(
                "43014", -4514.96f, -2314.11f,
                "attack_damage", "passives_increasedattackdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.08)
                ),
                List.of("34308", "36478")
        ));

        list.add(TalentNodeDef.of(
                "45012", 9207.96f, -424.575f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("41017", "64462", "8246")
        ));

        list.add(TalentNodeDef.of(
                "45090", -7705.03f, 4896.19f,
                "attack_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.12)
                ),
                List.of("18505", "36027")
        ));

        list.add(TalentNodeDef.of(
                "46782", 4971.97f, -626.595f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("21746", "53698")
        ));

        list.add(TalentNodeDef.of(
                "52354", 2667.71f, 11496.1f,
                "attack_damage", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.16)
                ),
                List.of("244", "41171")
        ));

        list.add(TalentNodeDef.of(
                "53698", 5063.48f, -968.101f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("20916", "46782")
        ));

        list.add(TalentNodeDef.of(
                "53938", 3691.19f, -2510.92f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("27910", "36931")
        ));

        list.add(TalentNodeDef.of(
                "59180", -4108.36f, 7765.45f,
                "attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("13352", "50023")
        ));

        list.add(TalentNodeDef.of(
                "63731", 2537.98f, 11565.8f,
                "attack_damage", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES, 0.16)
                ),
                List.of("244", "37767")
        ));

        list.add(TalentNodeDef.of(
                "64345", 4943.84f, 7568.56f,
                "attack_damage", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("24922", "49968", "63679")
        ));

        list.add(TalentNodeDef.of(
                "7251", -3502.49f, 5016.42f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("29270", "44419")
        ));

        list.add(TalentNodeDef.of(
                "7576", 1821.35f, -1202.94f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("33866", "42857")
        ));

        list.add(TalentNodeDef.of(
                "8246", 9250.41f, -600.163f,
                "attack_damage", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.1)
                ),
                List.of("37548", "37742", "45012", "64462")
        ));

        list.add(TalentNodeDef.of(
                "33974", 1217.71f, 7013.94f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.05)
                ),
                List.of("31189", "51847")
        ));

        list.add(TalentNodeDef.of(
                "43149", -6771.69f, 5221.33f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.06)
                ),
                List.of("40292", "5800")
        ));

        list.add(TalentNodeDef.of(
                "43507", -6629.67f, 5467.33f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.06)
                ),
                List.of("40292", "45075")
        ));

        list.add(TalentNodeDef.of(
                "45075", -6413.16f, 5428.33f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.06)
                ),
                List.of("27439", "43507")
        ));

        list.add(TalentNodeDef.of(
                "51847", 1152.33f, 7257.94f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.05)
                ),
                List.of("21871", "33974", "64665")
        ));

        list.add(TalentNodeDef.of(
                "5800", -6629.67f, 5053.33f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.06)
                ),
                List.of("22975", "43149")
        ));

        list.add(TalentNodeDef.of(
                "64665", 973.705f, 7436.56f,
                "attack_damage_and_accuracy", "passives_accuracystr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.05)
                ),
                List.of("21274", "51847")
        ));

        list.add(TalentNodeDef.of(
                "3640", 10423.0f, -1141.16f,
                "attack_damage_and_combo", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("14267", "17724", "37780")
        ));

        list.add(TalentNodeDef.of(
                "28414", 9738.29f, 3696.59f,
                "attack_damage_and_companion_damage_as_chaos", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.04)
                ),
                List.of("47853", "62096")
        ));

        list.add(TalentNodeDef.of(
                "43064", 9288.71f, 3526.05f,
                "attack_damage_and_companion_damage_as_chaos", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.04)
                ),
                List.of("34478", "47853")
        ));

        list.add(TalentNodeDef.of(
                "62096", 9477.71f, 3854.15f,
                "attack_damage_and_companion_damage_as_chaos", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.04)
                ),
                List.of("28414", "63192")
        ));

        list.add(TalentNodeDef.of(
                "63192", 9712.8f, 4026.57f,
                "attack_damage_and_companion_damage_as_chaos", "passives_azmeriprimalsnake",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.04)
                ),
                List.of("48116", "62096")
        ));

        list.add(TalentNodeDef.of(
                "32185", 3113.83f, 7213.29f,
                "attack_damage_and_companion_damage_as_cold", "passives_azmeriprimalowl",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.04)
                ),
                List.of("17118", "3665")
        ));

        list.add(TalentNodeDef.of(
                "3665", 3028.9f, 6994.96f,
                "attack_damage_and_companion_damage_as_cold", "passives_azmeriprimalowl",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.04)
                ),
                List.of("32185", "52568")
        ));

        list.add(TalentNodeDef.of(
                "25170", 3230.53f, 3861.77f,
                "attack_damage_and_duration", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.08)
                ),
                List.of("19442", "30820")
        ));

        list.add(TalentNodeDef.of(
                "30820", 3096.03f, 4128.79f,
                "attack_damage_and_duration", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.08)
                ),
                List.of("25170", "26786")
        ));

        list.add(TalentNodeDef.of(
                "25055", 8874.19f, 4520.38f,
                "attack_damage_and_movement_speed", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08)
                ),
                List.of("36576", "41580")
        ));

        list.add(TalentNodeDef.of(
                "24801", -8298.62f, 7908.05f,
                "attack_damage_and_presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06)
                ),
                List.of("40736", "45162")
        ));

        list.add(TalentNodeDef.of(
                "31545", -8006.61f, 7402.29f,
                "attack_damage_and_presence_area", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.06)
                ),
                List.of("34305", "3446", "8171")
        ));

        list.add(TalentNodeDef.of(
                "13799", 8590.14f, 4356.38f,
                "attack_damage_and_slow_effect", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.04)
                ),
                List.of("36576", "41580")
        ));

        list.add(TalentNodeDef.of(
                "18818", 4597.23f, 322.945f,
                "attack_damage_and_speed", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05)
                ),
                List.of("21314", "29517", "49461")
        ));

        list.add(TalentNodeDef.of(
                "5324", -2172.38f, 11255.6f,
                "attack_damage_if_stunned_recently", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_BEEN_HEAVY_STUNNED_RECENTLY, 0.2)
                ),
                List.of("2397", "46268", "54437")
        ));

        list.add(TalentNodeDef.of(
                "54437", -2172.38f, 11005.6f,
                "attack_damage_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_ON_LOW_HEALTH, 0.2)
                ),
                List.of("16111", "2397", "5324", "8493")
        ));

        list.add(TalentNodeDef.of(
                "19796", -694.041f, 10566.9f,
                "attack_damage_vs_bleeding_enemies", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES, 0.16)
                ),
                List.of("18308", "21627", "55066")
        ));

        list.add(TalentNodeDef.of(
                "55066", -593.5f, 10326.0f,
                "attack_damage_vs_bleeding_enemies", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_AGAINST_BLEEDING_ENEMIES, 0.16)
                ),
                List.of("19129", "19796", "21721")
        ));

        list.add(TalentNodeDef.of(
                "17077", 4350.71f, 7926.5f,
                "attack_damage_while_moving", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.12)
                ),
                List.of("24922", "36114", "51463")
        ));

        list.add(TalentNodeDef.of(
                "36114", 4475.71f, 8143.0f,
                "attack_damage_while_moving", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.12)
                ),
                List.of("17077", "23360")
        ));

        list.add(TalentNodeDef.of(
                "51463", 4225.71f, 8143.0f,
                "attack_damage_while_moving", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.12)
                ),
                List.of("17077", "4364")
        ));

        list.add(TalentNodeDef.of(
                "46268", -1739.37f, 11255.6f,
                "attack_damage_while_no_remaining_life_flasks", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_NO_HEALTH_FLASK_USES_LEFT, 0.2)
                ),
                List.of("16111", "2397", "5324")
        ));

        list.add(TalentNodeDef.of(
                "10271", -144.006f, 8913.68f,
                "attack_damage_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25)
                ),
                List.of("11257", "58038")
        ));

        list.add(TalentNodeDef.of(
                "16111", -1739.37f, 11005.6f,
                "attack_damage_while_surrounded", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.2)
                ),
                List.of("2397", "46268", "54099", "54437")
        ));

        list.add(TalentNodeDef.of(
                "23036", -4808.92f, 9482.1f,
                "attack_damage_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25)
                ),
                List.of("3339", "46931", "59208")
        ));

        list.add(TalentNodeDef.of(
                "3339", -4895.36f, 9159.48f,
                "attack_damage_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25)
                ),
                List.of("23036", "45585")
        ));

        list.add(TalentNodeDef.of(
                "46931", -4486.3f, 9568.54f,
                "attack_damage_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25)
                ),
                List.of("17825", "23036", "28175", "29914")
        ));

        list.add(TalentNodeDef.of(
                "58038", -230.451f, 8591.06f,
                "attack_damage_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_SURROUNDED, 0.25)
                ),
                List.of("10271", "25711", "31566")
        ));

        list.add(TalentNodeDef.of(
                "10305", -5071.69f, 6296.75f,
                "attack_damage_with_ally", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_DEAL_INC_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.08)
                ),
                List.of("35011", "45586", "61490")
        ));

        list.add(TalentNodeDef.of(
                "35011", -5153.69f, 6154.73f,
                "attack_damage_with_ally", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.16)
                ),
                List.of("10305", "53187")
        ));

        list.add(TalentNodeDef.of(
                "45215", -4907.69f, 6012.7f,
                "attack_damage_with_ally", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_DEAL_INC_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.08)
                ),
                List.of("14761", "53187")
        ));

        list.add(TalentNodeDef.of(
                "21871", 946.211f, 7138.94f,
                "attack_damage_with_nearby_ally", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.16)
                ),
                List.of("25213", "37872", "51847")
        ));

        list.add(TalentNodeDef.of(
                "25213", 729.705f, 7263.94f,
                "attack_damage_with_nearby_ally", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE, 0.16)
                ),
                List.of("19223", "21871")
        ));

        list.add(TalentNodeDef.of(
                "12471", -6560.3f, -5665.96f,
                "attack_elemental_damage", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("19942", "22270")
        ));

        list.add(TalentNodeDef.of(
                "19942", -6359.44f, -5781.93f,
                "attack_elemental_damage", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("1144", "12471")
        ));

        list.add(TalentNodeDef.of(
                "14254", 143.973f, 2336.36f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("13081", "97")
        ));

        list.add(TalentNodeDef.of(
                "18548", 2229.38f, 990.015f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("28992", "35660")
        ));

        list.add(TalentNodeDef.of(
                "20820", 6170.77f, -5970.8f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("40166", "44628")
        ));

        list.add(TalentNodeDef.of(
                "23702", 1091.86f, 10524.7f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("55802", "57196", "63445")
        ));

        list.add(TalentNodeDef.of(
                "24871", -5147.23f, 2464.26f,
                "attack_speed", "passives_onehanddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_ONE_HANDED_WEAPONS, 0.03)
                ),
                List.of("10602", "57552")
        ));

        list.add(TalentNodeDef.of(
                "24880", -1533.68f, 3305.54f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("38501", "45969", "62640")
        ));
        return list;
    }
}
