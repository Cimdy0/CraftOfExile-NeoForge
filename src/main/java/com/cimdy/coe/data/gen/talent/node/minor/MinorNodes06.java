package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes06 {

    private MinorNodes06() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "12462", -3570.72f, -2061.64f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("50558", "64299")
        ));

        list.add(TalentNodeDef.of(
                "17655", -3139.44f, -1812.64f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("17517", "19873", "50216", "63209", "64299")
        ));

        list.add(TalentNodeDef.of(
                "45354", -6533.67f, -4773.94f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("21568", "64948")
        ));

        list.add(TalentNodeDef.of(
                "48264", -6327.22f, -4932.35f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("12964", "64948")
        ));

        list.add(TalentNodeDef.of(
                "50558", -3704.09f, -2138.64f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("12462", "32194", "39540", "58651")
        ));

        list.add(TalentNodeDef.of(
                "61836", -6249.23f, -390.275f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("52807", "65243")
        ));

        list.add(TalentNodeDef.of(
                "64948", -6253.7f, -4657.97f,
                "aura_effect", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("45354", "48264")
        ));

        list.add(TalentNodeDef.of(
                "39448", -9276.2f, 6487.27f,
                "axe_attack_speed", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_AXES, 0.03)
                ),
                List.of("11178", "45990")
        ));

        list.add(TalentNodeDef.of(
                "45990", -9126.47f, 6400.83f,
                "axe_attack_speed", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_AXES, 0.04)
                ),
                List.of("39448", "6269")
        ));

        list.add(TalentNodeDef.of(
                "6269", -8997.58f, 6565.03f,
                "axe_attack_speed", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_AXES, 0.03)
                ),
                List.of("45990", "57880")
        ));

        list.add(TalentNodeDef.of(
                "57880", -8803.85f, 6487.27f,
                "axe_damage", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_AXES, 0.12)
                ),
                List.of("11306", "27082", "6269")
        ));

        list.add(TalentNodeDef.of(
                "11306", -8881.61f, 6681.0f,
                "axe_rage_on_hit", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_AXE_HIT_DAMAGE, 1.0)
                ),
                List.of("53440", "57880")
        ));

        list.add(TalentNodeDef.of(
                "52300", -8803.85f, 6959.62f,
                "axe_rage_on_hit", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_AXE_HIT_DAMAGE, 1.0)
                ),
                List.of("53440", "62439")
        ));

        list.add(TalentNodeDef.of(
                "53440", -8717.41f, 6809.89f,
                "axe_rage_on_hit", "passives_damageaxe",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_AXE_HIT_DAMAGE, 2.0)
                ),
                List.of("11306", "52300")
        ));

        list.add(TalentNodeDef.of(
                "40597", -2411.35f, 9679.83f,
                "ballista_attack_speed", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_BALLISTAS_HAVE_INC_ATTACK_SPEED, 0.04)
                ),
                List.of("24368", "53607", "58817")
        ));

        list.add(TalentNodeDef.of(
                "56897", 2207.55f, 7848.28f,
                "ballista_crit_chanceical_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_DAMAGE_BONUS, 0.1)
                ),
                List.of("57785")
        ));

        list.add(TalentNodeDef.of(
                "31112", 2207.55f, 7415.27f,
                "ballista_crit_chanceical_strike", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("57785")
        ));

        list.add(TalentNodeDef.of(
                "63828", 1832.55f, 7631.77f,
                "ballista_crit_chanceical_strike_and_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_DAMAGE_BONUS, 0.06),
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("23996")
        ));

        list.add(TalentNodeDef.of(
                "23153", 1957.55f, 7848.28f,
                "ballista_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_DAMAGE, 0.15)
                ),
                List.of("23996", "39298")
        ));

        list.add(TalentNodeDef.of(
                "23996", 2082.55f, 7631.77f,
                "ballista_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_DAMAGE, 0.15)
                ),
                List.of("23153", "57785", "63828")
        ));

        list.add(TalentNodeDef.of(
                "24368", -2756.41f, 9774.89f,
                "ballista_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_DAMAGE, 0.15)
                ),
                List.of("37302", "40597", "40975")
        ));

        list.add(TalentNodeDef.of(
                "40975", -2756.41f, 10024.9f,
                "ballista_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_DAMAGE, 0.15)
                ),
                List.of("24368", "52860")
        ));

        list.add(TalentNodeDef.of(
                "52860", -2756.41f, 10274.9f,
                "ballista_damage", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_DAMAGE, 0.15)
                ),
                List.of("3282", "40975", "45494", "8493")
        ));

        list.add(TalentNodeDef.of(
                "3282", -2992.59f, 10261.1f,
                "ballista_immobilisation_buildup", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_IMMOBILISATION_BUILDUP, 0.2)
                ),
                List.of("52860")
        ));

        list.add(TalentNodeDef.of(
                "45494", -2520.24f, 10261.1f,
                "ballista_immobilisation_buildup", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BALLISTA_IMMOBILISATION_BUILDUP, 0.2)
                ),
                List.of("52860")
        ));

        list.add(TalentNodeDef.of(
                "20842", -3978.69f, 9920.58f,
                "banner_area", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.12)
                ),
                List.of("4661", "48026")
        ));

        list.add(TalentNodeDef.of(
                "21096", -1752.98f, 7380.21f,
                "banner_area", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.12)
                ),
                List.of("56388", "62258")
        ));

        list.add(TalentNodeDef.of(
                "41651", -1863.48f, 5716.51f,
                "banner_area", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.12)
                ),
                List.of("43791", "5098", "53320")
        ));

        list.add(TalentNodeDef.of(
                "5098", -1863.48f, 6000.56f,
                "banner_area", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.12)
                ),
                List.of("16385", "41651")
        ));

        list.add(TalentNodeDef.of(
                "65439", -3907.68f, 9627.58f,
                "banner_aura_effect", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("12821", "48026")
        ));

        list.add(TalentNodeDef.of(
                "6623", -4049.71f, 9545.58f,
                "banner_aura_effect", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("12821", "48026")
        ));

        list.add(TalentNodeDef.of(
                "10835", -4267.95f, 9753.58f,
                "banner_duration", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_DURATION, 0.2)
                ),
                List.of("48026", "53367")
        ));

        list.add(TalentNodeDef.of(
                "11752", -1501.68f, 7635.08f,
                "banner_duration", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_DURATION, 0.2)
                ),
                List.of("18801", "57616", "62258", "62455")
        ));

        list.add(TalentNodeDef.of(
                "18801", -1501.68f, 8068.09f,
                "banner_duration", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_DURATION, 0.2)
                ),
                List.of("11752", "54818", "63360")
        ));

        list.add(TalentNodeDef.of(
                "57616", -1500.37f, 7380.21f,
                "banner_duration", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_SKILLS_HAVE_INC_DURATION, 0.2)
                ),
                List.of("11752", "56388")
        ));

        list.add(TalentNodeDef.of(
                "12821", -3896.69f, 9444.55f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("4661", "53367", "65439", "6623")
        ));

        list.add(TalentNodeDef.of(
                "16385", -2027.48f, 6000.56f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("46475", "5098", "53320")
        ));

        list.add(TalentNodeDef.of(
                "48026", -4060.69f, 9728.6f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("10835", "20842", "28175", "65439", "6623")
        ));

        list.add(TalentNodeDef.of(
                "53320", -2027.48f, 5716.51f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("16385", "41651", "43791")
        ));

        list.add(TalentNodeDef.of(
                "62258", -1751.68f, 7635.08f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("11752", "21096", "62455", "63360")
        ));

        list.add(TalentNodeDef.of(
                "63360", -1751.68f, 8068.09f,
                "banner_glory_gained", "passives_bannerresourceareanode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION_FOR_BANNER_SKILLS, 0.2)
                ),
                List.of("18801", "2864", "62258")
        ));

        list.add(TalentNodeDef.of(
                "36894", -7192.15f, 6488.38f,
                "bleed_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("33137", "43778", "61938")
        ));

        list.add(TalentNodeDef.of(
                "40276", -4113.12f, -9545.42f,
                "bleed_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("32745", "60241")
        ));

        list.add(TalentNodeDef.of(
                "43778", -6735.9f, 6366.13f,
                "bleed_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("14515", "36894", "8600")
        ));

        list.add(TalentNodeDef.of(
                "60241", -4113.12f, -9254.29f,
                "bleed_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("40276", "57178", "58183")
        ));

        list.add(TalentNodeDef.of(
                "61905", 4748.75f, -10391.2f,
                "bleed_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("12778", "3251", "8938")
        ));

        list.add(TalentNodeDef.of(
                "64996", 4532.24f, -10850.2f,
                "bleed_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("33229", "4810", "7782")
        ));

        list.add(TalentNodeDef.of(
                "8938", 4459.49f, -10558.2f,
                "bleed_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("33229", "61905")
        ));

        list.add(TalentNodeDef.of(
                "33137", -7061.31f, 6697.83f,
                "bleed_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("17330", "36894", "3698")
        ));

        list.add(TalentNodeDef.of(
                "48079", -3620.66f, -681.657f,
                "bleed_duration", "passives_blood2",
                List.of(),
                List.of("60014", "8248")
        ));

        list.add(TalentNodeDef.of(
                "8248", -3560.64f, -905.685f,
                "bleed_duration", "passives_blood2",
                List.of(),
                List.of("37629", "48079")
        ));

        list.add(TalentNodeDef.of(
                "21721", -188.283f, 10274.9f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("15899", "52125", "55066", "62194")
        ));

        list.add(TalentNodeDef.of(
                "40336", -1410.85f, 8880.27f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("47709", "6655")
        ));

        list.add(TalentNodeDef.of(
                "47709", -1305.68f, 8698.1f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("40336", "63814", "65322")
        ));

        list.add(TalentNodeDef.of(
                "55231", -1113.12f, 4490.39f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("37361", "7628", "9857")
        ));

        list.add(TalentNodeDef.of(
                "63814", -1200.68f, 8879.97f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("33216", "47709")
        ));

        list.add(TalentNodeDef.of(
                "65322", -1305.68f, 8391.66f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("13425", "47709", "54818", "58718")
        ));

        list.add(TalentNodeDef.of(
                "9857", -906.387f, 4330.9f,
                "bleeding_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("54990", "55231")
        ));

        list.add(TalentNodeDef.of(
                "39570", 4244.81f, 7124.06f,
                "bleeding_chance_on_critical", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS, 0.1)
                ),
                List.of("18923", "49394", "56330")
        ));

        list.add(TalentNodeDef.of(
                "56330", 4562.08f, 7113.79f,
                "bleeding_chance_on_critical", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS, 0.1)
                ),
                List.of("33391", "39570")
        ));

        list.add(TalentNodeDef.of(
                "10576", 7459.44f, 6789.98f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION, 0.15)
                ),
                List.of("58644", "58848")
        ));

        list.add(TalentNodeDef.of(
                "15899", -313.535f, 10441.9f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("21627", "21721")
        ));

        list.add(TalentNodeDef.of(
                "21627", -477.535f, 10605.9f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("15899", "19796")
        ));

        list.add(TalentNodeDef.of(
                "28268", 3414.8f, 3611.44f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION, 0.15)
                ),
                List.of("20837", "36630", "57517")
        ));

        list.add(TalentNodeDef.of(
                "35015", -1587.62f, 8880.27f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("58718", "6655")
        ));

        list.add(TalentNodeDef.of(
                "37361", -1048.42f, 4330.9f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("40043", "55231")
        ));

        list.add(TalentNodeDef.of(
                "40043", -1048.42f, 4166.9f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("37361", "54990")
        ));

        list.add(TalentNodeDef.of(
                "57517", 3247.77f, 3615.89f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION, 0.15)
                ),
                List.of("28268", "29941")
        ));

        list.add(TalentNodeDef.of(
                "58644", 7282.66f, 6613.2f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION, 0.15)
                ),
                List.of("10576", "42714")
        ));

        list.add(TalentNodeDef.of(
                "58718", -1482.46f, 8698.1f,
                "bleeding_damage", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.1)
                ),
                List.of("35015", "65322")
        ));

        list.add(TalentNodeDef.of(
                "13425", -1137.16f, 8698.1f,
                "bleeding_duration", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLEEDING_DURATION, 0.1)
                ),
                List.of("315", "65322")
        ));

        list.add(TalentNodeDef.of(
                "315", -1032.12f, 8879.97f,
                "bleeding_duration", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLEEDING_DURATION, 0.1)
                ),
                List.of("13425", "33216")
        ));

        list.add(TalentNodeDef.of(
                "11871", 6517.59f, -2578.81f,
                "blind_chance", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("24958", "47235", "53471")
        ));

        list.add(TalentNodeDef.of(
                "1801", 9212.93f, 409.52f,
                "blind_chance", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("41017", "60")
        ));

        list.add(TalentNodeDef.of(
                "3660", 590.865f, 10235.5f,
                "blind_chance", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS, 0.08)
                ),
                List.of("25619", "42354", "43562", "57196")
        ));

        list.add(TalentNodeDef.of(
                "42974", 10638.3f, -2128.64f,
                "blind_chance", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("30808", "46152", "8302")
        ));

        list.add(TalentNodeDef.of(
                "43562", 1091.86f, 9946.21f,
                "blind_chance", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS, 0.08)
                ),
                List.of("25619", "3660", "42354", "63445")
        ));

        list.add(TalentNodeDef.of(
                "60", 8929.0f, 632.637f,
                "blind_chance", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("1801", "19338", "58426")
        ));

        list.add(TalentNodeDef.of(
                "40110", 11018.8f, -2003.64f,
                "blind_effect", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.1)
                ),
                List.of("42347", "46152")
        ));

        list.add(TalentNodeDef.of(
                "46152", 10802.3f, -2046.64f,
                "blind_effect", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.1)
                ),
                List.of("40110", "42974")
        ));

        list.add(TalentNodeDef.of(
                "63610", 3926.84f, 795.235f,
                "blind_effect", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.1)
                ),
                List.of("38459", "61373")
        ));

        list.add(TalentNodeDef.of(
                "24570", 6272.73f, -2298.64f,
                "blinded_enemies_critical", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_BLINDED_BY_YOU_HAVE_INC_CRITICAL_HIT_CHANCE, -0.15)
                ),
                List.of("28441", "47235")
        ));

        list.add(TalentNodeDef.of(
                "47235", 6306.15f, -2472.7f,
                "blinded_enemies_critical", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_BLINDED_BY_YOU_HAVE_INC_CRITICAL_HIT_CHANCE, -0.15)
                ),
                List.of("11871", "24570")
        ));

        list.add(TalentNodeDef.of(
                "11980", 2952.67f, 8082.3f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("20504", "38814", "52836")
        ));

        list.add(TalentNodeDef.of(
                "18629", -3067.29f, 2195.2f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("64327", "7721")
        ));

        list.add(TalentNodeDef.of(
                "18831", 4227.83f, 1918.24f,
                "block", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("39658", "49545", "60505")
        ));

        list.add(TalentNodeDef.of(
                "23192", -9189.0f, 2376.63f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("33452", "42578")
        ));

        list.add(TalentNodeDef.of(
                "30143", 5544.92f, 8671.21f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("22057", "33445", "34201")
        ));

        list.add(TalentNodeDef.of(
                "30390", -4841.44f, 2159.3f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("33978", "36163")
        ));

        list.add(TalentNodeDef.of(
                "33445", 5721.69f, 8744.43f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("30143", "37244")
        ));

        list.add(TalentNodeDef.of(
                "33452", -9602.0f, 1954.01f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("23192", "52796", "59785")
        ));

        list.add(TalentNodeDef.of(
                "36163", -4391.48f, 2275.27f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("16725", "30390", "31609")
        ));

        list.add(TalentNodeDef.of(
                "39517", -3401.29f, 2529.2f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("53373", "64327")
        ));

        list.add(TalentNodeDef.of(
                "39658", 4450.95f, 1719.2f,
                "block", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("15030", "18831")
        ));

        list.add(TalentNodeDef.of(
                "42578", -8858.56f, 2276.62f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("23192", "57471")
        ));

        list.add(TalentNodeDef.of(
                "52836", 2751.21f, 8145.82f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("11980", "56806", "62341")
        ));

        list.add(TalentNodeDef.of(
                "62341", 2595.46f, 8288.54f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("20504", "38814", "52836")
        ));

        list.add(TalentNodeDef.of(
                "20504", 2995.2f, 8568.45f,
                "block", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_HEALTH_WHEN_YOU_BLOCK, 5.0)
                ),
                List.of("11980", "62341")
        ));

        list.add(TalentNodeDef.of(
                "58088", -7472.17f, 6582.06f,
                "block_and_movement_penalty_with_raised_shield", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING, -0.05)
                ),
                List.of("16620", "3446", "41442")
        ));

        list.add(TalentNodeDef.of(
                "1214", -9121.78f, 1777.23f,
                "block_and_shield_defences", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.15)
                ),
                List.of("21684", "53823")
        ));

        list.add(TalentNodeDef.of(
                "21684", -8781.0f, 1954.01f,
                "block_and_shield_defences", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.15)
                ),
                List.of("10508", "1214", "59945")
        ));

        list.add(TalentNodeDef.of(
                "41442", -7639.17f, 6383.32f,
                "block_and_stun_threshold", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.05)
                ),
                List.of("58088", "64324")
        ));

        list.add(TalentNodeDef.of(
                "64324", -7794.79f, 6180.51f,
                "block_and_stun_threshold", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.05)
                ),
                List.of("26479", "41442")
        ));

        list.add(TalentNodeDef.of(
                "10636", -11144.5f, -3109.83f,
                "block_chance", "passives_dmgreduction",
                List.of(),
                List.of("28201", "38697")
        ));

        list.add(TalentNodeDef.of(
                "20391", -11616.9f, -3109.83f,
                "block_chance", "passives_blockstr",
                List.of(),
                List.of("16947", "38697")
        ));

        list.add(TalentNodeDef.of(
                "38697", -11380.7f, -3012.0f,
                "block_chance", "passives_blockstr",
                List.of(),
                List.of("10636", "20391")
        ));

        list.add(TalentNodeDef.of(
                "34076", 5544.92f, 9171.21f,
                "block_recovery", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_RECOVERY, 0.25)
                ),
                List.of("36808", "37244")
        ));

        list.add(TalentNodeDef.of(
                "54985", -3003.07f, 7144.02f,
                "bolt_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BOLT_SPEED, 0.08)
                ),
                List.of("14602", "60191")
        ));

        list.add(TalentNodeDef.of(
                "60191", -3179.85f, 7320.8f,
                "bolt_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BOLT_SPEED, 0.08)
                ),
                List.of("37612", "54985")
        ));

        list.add(TalentNodeDef.of(
                "25992", 11163.5f, 3861.26f,
                "bow_accuracy_rating", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_BOWS, 0.1)
                ),
                List.of("328")
        ));

        list.add(TalentNodeDef.of(
                "328", 11202.4f, 4037.83f,
                "bow_accuracy_rating", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_BOWS, 0.1)
                ),
                List.of("21112", "25992", "60764")
        ));

        list.add(TalentNodeDef.of(
                "23932", -12305.7f, -1864.76f,
                "bow_attack_speed", "passives_bowdamage",
                List.of(),
                List.of("7066", "8423")
        ));

        list.add(TalentNodeDef.of(
                "7066", -11891.7f, -1652.77f,
                "bow_attack_speed", "passives_bowdamage",
                List.of(),
                List.of("21218", "23932")
        ));

        list.add(TalentNodeDef.of(
                "8423", -12558.3f, -1474.15f,
                "bow_attack_speed", "passives_bowdamage",
                List.of(),
                List.of("23932", "60708")
        ));

        list.add(TalentNodeDef.of(
                "25586", 10755.4f, 5384.33f,
                "bow_critical_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_BOWS, 0.16)
                ),
                List.of("38993")
        ));

        list.add(TalentNodeDef.of(
                "38993", 10852.7f, 5275.51f,
                "bow_critical_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_BOWS, 0.16)
                ),
                List.of("11526", "21112", "25586")
        ));

        list.add(TalentNodeDef.of(
                "21112", 10210.9f, 4422.22f,
                "bow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_BOWS, 0.1)
                ),
                List.of("31055", "328", "38993", "48116")
        ));

        list.add(TalentNodeDef.of(
                "21218", -11813.0f, -1316.59f,
                "bow_damage", "passives_bowdamage",
                List.of(),
                List.of("13950", "45226", "7066")
        ));

        list.add(TalentNodeDef.of(
                "21788", 11316.9f, 4718.57f,
                "bow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_BOWS, 0.12)
                ),
                List.of("34612", "44141", "7651", "8573")
        ));

        list.add(TalentNodeDef.of(
                "34612", 11329.3f, 4434.44f,
                "bow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_BOWS, 0.12)
                ),
                List.of("21788", "60764")
        ));

        list.add(TalentNodeDef.of(
                "45226", -11713.0f, -986.145f,
                "bow_damage", "passives_bowdamage",
                List.of(),
                List.of("21218", "28982")
        ));

        list.add(TalentNodeDef.of(
                "60708", -12458.3f, -1143.7f,
                "bow_damage", "passives_bowdamage",
                List.of(),
                List.of("11666", "17894", "8423")
        ));

        list.add(TalentNodeDef.of(
                "8573", 11185.5f, 4970.84f,
                "bow_damage", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_BOWS, 0.12)
                ),
                List.of("11526", "21788")
        ));

        list.add(TalentNodeDef.of(
                "18969", 10793.4f, 4578.32f,
                "bow_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_BOWS, 0.03)
                ),
                List.of("31055", "44141")
        ));

        list.add(TalentNodeDef.of(
                "31055", 10523.8f, 4506.08f,
                "bow_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_BOWS, 0.03)
                ),
                List.of("18969", "21112")
        ));

        list.add(TalentNodeDef.of(
                "44141", 11004.9f, 4634.97f,
                "bow_speed", "passives_bowdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_BOWS, 0.03)
                ),
                List.of("18969", "21788")
        ));

        list.add(TalentNodeDef.of(
                "14096", -2156.7f, -4392.35f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("1543", "44293")
        ));

        list.add(TalentNodeDef.of(
                "15304", 4836.5f, -8894.18f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("16466", "22152")
        ));

        list.add(TalentNodeDef.of(
                "1543", -2074.7f, -4616.38f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("14096", "59376")
        ));

        list.add(TalentNodeDef.of(
                "17505", -6720.98f, -9710.8f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("23382", "28774", "52106")
        ));

        list.add(TalentNodeDef.of(
                "22152", 5000.5f, -8894.18f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("10382", "15304")
        ));

        list.add(TalentNodeDef.of(
                "25729", 8409.99f, -7424.9f,
                "cast_speed", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED, 0.03)
                ),
                List.of("33093", "37876")
        ));
        return list;
    }
}
