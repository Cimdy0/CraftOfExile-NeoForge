package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes15 {

    private MinorNodes15() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "52765", 4438.01f, -2936.56f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("11604", "45481")
        ));

        list.add(TalentNodeDef.of(
                "54708", -1885.32f, -1263.77f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("13855", "3918")
        ));

        list.add(TalentNodeDef.of(
                "55807", -215.813f, -2616.58f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("18845", "6686")
        ));

        list.add(TalentNodeDef.of(
                "59695", -2086.18f, -1464.63f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("28950", "3918")
        ));

        list.add(TalentNodeDef.of(
                "61923", 3212.04f, -10140.7f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("16256", "33345")
        ));

        list.add(TalentNodeDef.of(
                "6686", -331.779f, -2732.55f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("51184", "55807")
        ));

        list.add(TalentNodeDef.of(
                "7971", 374.655f, -6764.21f,
                "mana_regeneration", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.1)
                ),
                List.of("1468", "43576")
        ));

        list.add(TalentNodeDef.of(
                "17215", 5897.7f, -5415.11f,
                "mana_regeneration_and_attack_speed", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.05)
                ),
                List.of("17668", "4552")
        ));

        list.add(TalentNodeDef.of(
                "17668", 6057.19f, -5492.43f,
                "mana_regeneration_and_attack_speed", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.05)
                ),
                List.of("17215", "29306")
        ));

        list.add(TalentNodeDef.of(
                "28578", -6813.5f, -5755.92f,
                "mana_regeneration_and_critical_chance", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.08)
                ),
                List.of("27388", "904")
        ));

        list.add(TalentNodeDef.of(
                "904", -6722.0f, -5664.42f,
                "mana_regeneration_and_critical_chance", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.08)
                ),
                List.of("28578", "35831")
        ));

        list.add(TalentNodeDef.of(
                "50216", -2820.19f, -2063.25f,
                "mana_regeneration_and_speed", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.05)
                ),
                List.of("17655", "30834", "44951", "63469")
        ));

        list.add(TalentNodeDef.of(
                "63469", -2574.19f, -1921.23f,
                "mana_regeneration_and_speed", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.05)
                ),
                List.of("28950", "30834", "50216")
        ));

        list.add(TalentNodeDef.of(
                "14048", 6898.63f, -380.495f,
                "mana_regeneration_while_not_on_low_mana", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_NOT_ON_LOW_MANA, 0.16)
                ),
                List.of("34717", "52053")
        ));

        list.add(TalentNodeDef.of(
                "34717", 7097.38f, -547.495f,
                "mana_regeneration_while_not_on_low_mana", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_NOT_ON_LOW_MANA, 0.16)
                ),
                List.of("14048", "24120")
        ));
        list.add(TalentNodeDef.of(
                "47175", -1271.19f, 733.095f,
                "marauder", "passives_blankstr",
                List.of(),
                List.of("38646", "3936")
        ));

        list.add(TalentNodeDef.of(
                "13624", 7971.88f, 8022.14f,
                "mark_duration", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION, 0.25)
                ),
                List.of("28258", "45390", "63830")
        ));

        list.add(TalentNodeDef.of(
                "44841", 7855.91f, 8138.11f,
                "mark_duration", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION, 0.25)
                ),
                List.of("28258", "36927", "44756")
        ));

        list.add(TalentNodeDef.of(
                "28258", 8014.32f, 8180.55f,
                "mark_effect", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_YOUR_MARK_SKILLS, 0.1)
                ),
                List.of("13624", "36927", "36976", "44841", "45390", "59064")
        ));

        list.add(TalentNodeDef.of(
                "59064", 7778.15f, 7944.38f,
                "mark_effect", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_YOUR_MARK_SKILLS, 0.1)
                ),
                List.of("28258", "30657")
        ));

        list.add(TalentNodeDef.of(
                "44280", 8481.06f, 1885.96f,
                "mark_effect_and_blind_chance", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_YOUR_MARK_SKILLS, 0.08),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS, 0.05)
                ),
                List.of("23305", "35534")
        ));

        list.add(TalentNodeDef.of(
                "21279", 8249.13f, 1654.03f,
                "mark_effect_and_blind_effect", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_YOUR_MARK_SKILLS, 0.08),
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.1)
                ),
                List.of("23305", "35534")
        ));

        list.add(TalentNodeDef.of(
                "23305", 8481.06f, 1654.03f,
                "mark_use_speed", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_USE_SPEED, 0.1)
                ),
                List.of("21279", "31647", "44280", "51602")
        ));

        list.add(TalentNodeDef.of(
                "35534", 8249.13f, 1885.96f,
                "mark_use_speed", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_USE_SPEED, 0.1)
                ),
                List.of("21279", "2408", "44280")
        ));

        list.add(TalentNodeDef.of(
                "36927", 8056.77f, 8338.97f,
                "mark_use_speed", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_USE_SPEED, 0.1)
                ),
                List.of("28258", "44756", "44841")
        ));

        list.add(TalentNodeDef.of(
                "45390", 8172.74f, 8223.0f,
                "mark_use_speed", "passives_marknode",
                List.of(
                        TalentNodeGen.stat(StatIds.MARK_SKILLS_HAVE_INC_USE_SPEED, 0.1)
                ),
                List.of("13624", "28258", "63830")
        ));

        list.add(TalentNodeDef.of(
                "6900", -7472.17f, 5437.06f,
                "max_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_BLOCK_CHANCE, 0.01)
                ),
                List.of("10500", "26479", "45751")
        ));

        list.add(TalentNodeDef.of(
                "10484", -8076.15f, -604.357f,
                "max_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 2.0)
                ),
                List.of("17092", "42660")
        ));

        list.add(TalentNodeDef.of(
                "16347", -5503.08f, 8702.86f,
                "max_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 2.0)
                ),
                List.of("52373", "52556")
        ));

        list.add(TalentNodeDef.of(
                "17092", -8158.15f, -746.385f,
                "max_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 2.0)
                ),
                List.of("10484", "3723")
        ));

        list.add(TalentNodeDef.of(
                "18441", -4914.48f, -3117.96f,
                "max_rage", "passives_rage",
                List.of(),
                List.of("28002", "34181")
        ));

        list.add(TalentNodeDef.of(
                "34181", -4789.48f, -3334.46f,
                "max_rage", "passives_rage",
                List.of(),
                List.of("18441", "45422")
        ));

        list.add(TalentNodeDef.of(
                "52373", -5727.11f, 8762.88f,
                "max_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 2.0)
                ),
                List.of("16347", "37276", "56342")
        ));

        list.add(TalentNodeDef.of(
                "52556", -5443.06f, 8926.88f,
                "max_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 2.0)
                ),
                List.of("16347", "28304")
        ));

        list.add(TalentNodeDef.of(
                "37290", -8534.75f, -610.63f,
                "max_rage_while_shapeshifted", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE_WHILE_SHAPESHIFTED, 3.0)
                ),
                List.of("28892", "55925")
        ));

        list.add(TalentNodeDef.of(
                "55925", -8559.73f, -420.925f,
                "max_rage_while_shapeshifted", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE_WHILE_SHAPESHIFTED, 3.0)
                ),
                List.of("37290", "60068")
        ));

        list.add(TalentNodeDef.of(
                "14511", -9794.87f, 5540.58f,
                "melee_and_stun", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("18207", "53719", "54999")
        ));

        list.add(TalentNodeDef.of(
                "13987", 8943.99f, 2157.59f,
                "melee_attack_speed", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_ATTACK_SPEED, 0.03)
                ),
                List.of("48531", "7604")
        ));

        list.add(TalentNodeDef.of(
                "48531", 8760.96f, 2146.61f,
                "melee_attack_speed", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_ATTACK_SPEED, 0.03)
                ),
                List.of("13987", "56761")
        ));

        list.add(TalentNodeDef.of(
                "38663", -2370.07f, 7379.36f,
                "melee_critical_chance", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_CRITICAL_HIT_CHANCE, 0.1)
                ),
                List.of("3516", "39564", "49618")
        ));

        list.add(TalentNodeDef.of(
                "9941", 1422.03f, 3416.97f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_ONE_HANDED_MELEE_WEAPONS, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_TWO_HANDED_MELEE_WEAPONS, 0.08)
                ),
                List.of("38888", "53996")
        ));

        list.add(TalentNodeDef.of(
                "12430", -2231.3f, -810.253f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17584", "28446")
        ));

        list.add(TalentNodeDef.of(
                "15427", -9692.06f, -1117.08f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.12)
                ),
                List.of("32071", "57379")
        ));

        list.add(TalentNodeDef.of(
                "17138", -8158.65f, 3447.68f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("51903", "53308")
        ));

        list.add(TalentNodeDef.of(
                "17686", 1233.32f, 3390.26f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("43575", "53996")
        ));

        list.add(TalentNodeDef.of(
                "19011", -4013.48f, 1223.38f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("45363", "64284")
        ));

        list.add(TalentNodeDef.of(
                "31273", 8943.99f, 1993.59f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17372", "8560")
        ));

        list.add(TalentNodeDef.of(
                "33556", -2268.09f, 817.209f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("46325", "55473")
        ));

        list.add(TalentNodeDef.of(
                "3516", -2620.07f, 7125.23f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("23227", "38663", "62039")
        ));

        list.add(TalentNodeDef.of(
                "35985", 9458.96f, 2075.59f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17372", "7604")
        ));

        list.add(TalentNodeDef.of(
                "39190", -10112.5f, -794.466f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE, 0.15)
                ),
                List.of("57379", "8800")
        ));

        list.add(TalentNodeDef.of(
                "3936", -1525.89f, 733.415f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("13397", "46325", "47175")
        ));

        list.add(TalentNodeDef.of(
                "39564", -2620.07f, 7625.23f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("13279", "38663", "62039")
        ));

        list.add(TalentNodeDef.of(
                "43164", -2609.6f, 908.715f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("55473", "5710")
        ));

        list.add(TalentNodeDef.of(
                "43575", 1258.3f, 3579.96f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17686", "41512")
        ));

        list.add(TalentNodeDef.of(
                "46325", -2151.61f, 969.01f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("33556", "3936", "39581")
        ));

        list.add(TalentNodeDef.of(
                "51903", -8481.26f, 3534.13f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17138", "39347", "55058")
        ));

        list.add(TalentNodeDef.of(
                "53308", -7836.03f, 3361.24f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("17138", "52298")
        ));

        list.add(TalentNodeDef.of(
                "53996", 1349.81f, 3238.46f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("17686", "28556", "9941")
        ));

        list.add(TalentNodeDef.of(
                "55473", -2457.8f, 792.234f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("33556", "43164")
        ));

        list.add(TalentNodeDef.of(
                "56761", 8467.96f, 2075.59f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("2408", "48531", "8560")
        ));

        list.add(TalentNodeDef.of(
                "58528", -3557.22f, 1101.13f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("45363", "5710")
        ));

        list.add(TalentNodeDef.of(
                "62039", -2870.07f, 7379.36f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("3516", "39564", "49618")
        ));

        list.add(TalentNodeDef.of(
                "64284", -4195.6f, 1516.69f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.08)
                ),
                List.of("19011", "20645", "27373")
        ));

        list.add(TalentNodeDef.of(
                "8560", 8760.96f, 2004.58f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.1)
                ),
                List.of("31273", "56761")
        ));

        list.add(TalentNodeDef.of(
                "8800", -10262.2f, -880.911f,
                "melee_damage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE, 0.15)
                ),
                List.of("28982", "39190")
        ));

        list.add(TalentNodeDef.of(
                "55058", -8727.62f, 3776.18f,
                "melee_damage_against_immobilised", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.2)
                ),
                List.of("51903", "63790")
        ));

        list.add(TalentNodeDef.of(
                "63790", -8815.64f, 3447.68f,
                "melee_damage_against_immobilised", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.2)
                ),
                List.of("48014", "55058")
        ));

        list.add(TalentNodeDef.of(
                "45650", 3440.08f, 2962.1f,
                "melee_damage_if_projectile_hit", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.15)
                ),
                List.of("30047", "36997", "9572")
        ));

        list.add(TalentNodeDef.of(
                "9572", 3525.08f, 2814.87f,
                "melee_damage_if_projectile_hit", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.15)
                ),
                List.of("12822", "45650")
        ));

        list.add(TalentNodeDef.of(
                "49968", 5126.86f, 7557.58f,
                "melee_stun_buildup", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_WITH_MELEE_DAMAGE, 0.18)
                ),
                List.of("20008", "64345")
        ));

        list.add(TalentNodeDef.of(
                "45343", -4525.31f, -11081.7f,
                "minion_area", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.08)
                ),
                List.of("50483", "57021")
        ));

        list.add(TalentNodeDef.of(
                "50483", -4450.82f, -11306.2f,
                "minion_area", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.08)
                ),
                List.of("45343", "61842")
        ));

        list.add(TalentNodeDef.of(
                "57021", -4573.08f, -10850.0f,
                "minion_area", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, 0.08)
                ),
                List.of("45343", "8957")
        ));

        list.add(TalentNodeDef.of(
                "10742", -3972.07f, -6058.9f,
                "minion_attack_and_cast_speed", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.03)
                ),
                List.of("41991", "44733", "8357")
        ));

        list.add(TalentNodeDef.of(
                "1928", -3908.2f, -7290.56f,
                "minion_attack_and_cast_speed", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.05)
                ),
                List.of("56284")
        ));

        list.add(TalentNodeDef.of(
                "41991", -4094.32f, -6181.16f,
                "minion_attack_and_cast_speed", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.03)
                ),
                List.of("10742", "61026")
        ));

        list.add(TalentNodeDef.of(
                "56284", -3754.39f, -7303.0f,
                "minion_attack_and_cast_speed", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.05)
                ),
                List.of("1928", "8737")
        ));

        list.add(TalentNodeDef.of(
                "8357", -3927.32f, -5891.9f,
                "minion_attack_and_cast_speed", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_AND_CAST_SPEED, 0.03)
                ),
                List.of("10742", "38972")
        ));

        list.add(TalentNodeDef.of(
                "3332", -2530.04f, -8376.52f,
                "minion_cold_resistance", "passives_coldresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_COLD_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_COLD_RESISTANCES, 0.03)
                ),
                List.of("47284")
        ));

        list.add(TalentNodeDef.of(
                "47284", -2588.8f, -8604.12f,
                "minion_cold_resistance", "passives_coldresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_COLD_RESISTANCES, 0.2)
                ),
                List.of("2606", "3332")
        ));

        list.add(TalentNodeDef.of(
                "61768", 2157.18f, -9511.45f,
                "minion_cold_resistance", "passives_coldresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_COLD_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_COLD_RESISTANCES, 0.03)
                ),
                List.of("4271")
        ));

        list.add(TalentNodeDef.of(
                "15809", -2107.54f, -8675.53f,
                "minion_critical_chance", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("18485", "26945", "31175", "54036")
        ));

        list.add(TalentNodeDef.of(
                "18160", -2065.55f, -8169.77f,
                "minion_critical_chance", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("22783", "26945")
        ));

        list.add(TalentNodeDef.of(
                "26945", -2098.96f, -8343.83f,
                "minion_critical_chance", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_HIT_CHANCE, 0.2)
                ),
                List.of("15809", "18160")
        ));

        list.add(TalentNodeDef.of(
                "34199", -560.078f, -9940.3f,
                "minion_critical_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("50720")
        ));

        list.add(TalentNodeDef.of(
                "43557", -396.554f, -9811.32f,
                "minion_critical_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_CRITICAL_DAMAGE_BONUS, 0.15)
                ),
                List.of("50720")
        ));

        list.add(TalentNodeDef.of(
                "1447", -4428.32f, -5602.65f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("14945", "22393")
        ));

        list.add(TalentNodeDef.of(
                "17501", -1782.13f, -8343.83f,
                "minion_damage", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("22783", "54036")
        ));

        list.add(TalentNodeDef.of(
                "22221", -11557.6f, -5929.3f,
                "minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("57079", "64139")
        ));

        list.add(TalentNodeDef.of(
                "22393", -4261.32f, -5557.9f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("1447", "28458")
        ));

        list.add(TalentNodeDef.of(
                "22783", -1815.55f, -8169.77f,
                "minion_damage", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("15885", "17501", "18160")
        ));

        list.add(TalentNodeDef.of(
                "28458", -4094.32f, -5602.65f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("22393", "38972")
        ));

        list.add(TalentNodeDef.of(
                "29985", -6878.23f, -5992.16f,
                "minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("17229", "61113")
        ));

        list.add(TalentNodeDef.of(
                "33612", -4354.84f, -10976.0f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("40200", "8983")
        ));

        list.add(TalentNodeDef.of(
                "3443", 1798.44f, -6916.71f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("14548", "55180", "63545")
        ));

        list.add(TalentNodeDef.of(
                "36025", -11263.1f, -4986.2f,
                "minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("32474", "44309", "829")
        ));

        list.add(TalentNodeDef.of(
                "37594", -4397.91f, -10369.9f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("50104", "8983")
        ));

        list.add(TalentNodeDef.of(
                "40200", -4283.82f, -11181.0f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("22331", "33612", "61842")
        ));

        list.add(TalentNodeDef.of(
                "43385", -11932.6f, -5712.79f,
                "minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("64139", "7553")
        ));

        list.add(TalentNodeDef.of(
                "44951", -2936.4f, -2239.7f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("50216", "5777")
        ));

        list.add(TalentNodeDef.of(
                "47155", 2034.62f, -7402.89f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("17394", "35896", "63545")
        ));

        list.add(TalentNodeDef.of(
                "50720", -160.381f, -10220.4f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("11376", "30523", "34199", "43557", "59362")
        ));

        list.add(TalentNodeDef.of(
                "54036", -1773.55f, -8675.53f,
                "minion_damage", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("15809", "17501", "18485", "30720")
        ));

        list.add(TalentNodeDef.of(
                "58651", -3324.0f, -2380.78f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("49357", "50558", "5777")
        ));

        list.add(TalentNodeDef.of(
                "61113", -6628.23f, -5992.16f,
                "minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("29985", "53910")
        ));

        list.add(TalentNodeDef.of(
                "61842", -4283.82f, -11351.0f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("14712", "33240", "40200", "50483")
        ));

        list.add(TalentNodeDef.of(
                "63545", 2048.44f, -7166.71f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("3443", "34520", "47155")
        ));

        list.add(TalentNodeDef.of(
                "64653", -7299.95f, -8772.41f,
                "minion_damage", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.16)
                ),
                List.of("47420")
        ));

        list.add(TalentNodeDef.of(
                "8737", -3697.15f, -7461.02f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("25927", "41511", "56284", "6077", "95")
        ));

        list.add(TalentNodeDef.of(
                "8983", -4283.82f, -10683.0f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.12)
                ),
                List.of("22331", "33612", "37594")
        ));

        list.add(TalentNodeDef.of(
                "9065", -6680.9f, -8742.95f,
                "minion_damage", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("23382", "752")
        ));

        list.add(TalentNodeDef.of(
                "95", -4084.76f, -7312.1f,
                "minion_damage", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("1433", "8737")
        ));

        list.add(TalentNodeDef.of(
                "43979", -1745.57f, -10845.8f,
                "minion_damage_and_command_cooldown", "passives_archonofundeathnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.08)
                ),
                List.of("26926", "50837")
        ));

        list.add(TalentNodeDef.of(
                "14598", -1411.57f, -10511.8f,
                "minion_damage_and_command_speed", "passives_archonofundeathnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.08)
                ),
                List.of("4345", "50837")
        ));

        list.add(TalentNodeDef.of(
                "4345", -1175.39f, -10609.7f,
                "minion_damage_and_command_speed", "passives_archonofundeathnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.08)
                ),
                List.of("14598", "45885")
        ));

        list.add(TalentNodeDef.of(
                "50837", -1647.74f, -10609.7f,
                "minion_damage_and_command_speed", "passives_archonofundeathnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_COOLDOWN_RECOVERY_RATE_FOR_COMMAND_SKILLS, 0.08)
                ),
                List.of("14598", "43979")
        ));

        list.add(TalentNodeDef.of(
                "11048", -7339.68f, -9074.18f,
                "minion_damage_and_duration", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_MINION_DURATION, 0.08)
                ),
                List.of("47420")
        ));

        list.add(TalentNodeDef.of(
                "752", -7007.21f, -8833.22f,
                "minion_damage_and_duration", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_MINION_DURATION, 0.06)
                ),
                List.of("47420", "53444", "9065")
        ));

        list.add(TalentNodeDef.of(
                "19006", -2526.28f, -9378.04f,
                "minion_damage_and_life", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06)
                ),
                List.of("39461", "54453")
        ));

        list.add(TalentNodeDef.of(
                "229", -2198.28f, -9378.04f,
                "minion_damage_and_life", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06)
                ),
                List.of("39461", "46365")
        ));

        list.add(TalentNodeDef.of(
                "39461", -2362.28f, -9542.04f,
                "minion_damage_and_life", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06)
                ),
                List.of("19006", "229")
        ));

        list.add(TalentNodeDef.of(
                "54453", -2362.28f, -9214.04f,
                "minion_damage_and_life", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.06),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.06)
                ),
                List.of("19006", "61042")
        ));

        list.add(TalentNodeDef.of(
                "19966", -11028.1f, -5655.53f,
                "minion_duration", "passives_miniondamageblue",
                List.of(),
                List.of("57079", "829")
        ));

        list.add(TalentNodeDef.of(
                "52676", -6946.4f, -9125.96f,
                "minion_duration", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MINION_DURATION, 0.16)
                ),
                List.of("47420")
        ));

        list.add(TalentNodeDef.of(
                "53444", -6916.94f, -8506.91f,
                "minion_duration", "passives_minionsandmananode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MINION_DURATION, 0.12)
                ),
                List.of("59093", "752")
        ));

        list.add(TalentNodeDef.of(
                "829", -11144.4f, -5335.96f,
                "minion_duration", "passives_miniondamageblue",
                List.of(),
                List.of("19966", "36025")
        ));

        list.add(TalentNodeDef.of(
                "15194", -3049.19f, -8762.37f,
                "minion_fire_resistance", "passives_fireresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_FIRE_RESISTANCES, 0.2)
                ),
                List.of("25303", "2606")
        ));

        list.add(TalentNodeDef.of(
                "25303", -3196.2f, -8995.5f,
                "minion_fire_resistance", "passives_fireresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_FIRE_RESISTANCES, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_FIRE_RESISTANCES, 0.2)
                ),
                List.of("15194")
        ));

        list.add(TalentNodeDef.of(
                "33225", 1960.52f, -9606.62f,
                "minion_fire_resistance", "passives_fireresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_FIRE_RESISTANCES, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_FIRE_RESISTANCES, 0.2)
                ),
                List.of("4271")
        ));

        list.add(TalentNodeDef.of(
                "10320", 1915.23f, -6650.5f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("14548", "15358")
        ));

        list.add(TalentNodeDef.of(
                "1218", -4595.32f, -5891.9f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("14945", "40894")
        ));

        list.add(TalentNodeDef.of(
                "14548", 2165.23f, -6900.5f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("10320", "3443", "34520", "59541")
        ));

        list.add(TalentNodeDef.of(
                "14712", -4116.82f, -11306.2f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("3866", "61842")
        ));

        list.add(TalentNodeDef.of(
                "17378", -4428.32f, -6181.16f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("40894", "61026")
        ));

        list.add(TalentNodeDef.of(
                "21606", -2824.48f, -8415.27f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("2606", "29148")
        ));

        list.add(TalentNodeDef.of(
                "2606", -2824.48f, -8667.27f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("15194", "20388", "21606", "3414", "43366", "47284")
        ));

        list.add(TalentNodeDef.of(
                "30554", -9916.34f, -2923.91f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("10055", "18245", "29611")
        ));

        list.add(TalentNodeDef.of(
                "32258", -3994.57f, -10850.0f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("19644", "3866")
        ));

        list.add(TalentNodeDef.of(
                "3363", -6074.1f, -5852.37f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("38433", "44872")
        ));

        list.add(TalentNodeDef.of(
                "38433", -6274.96f, -5968.34f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("3363", "55375")
        ));

        list.add(TalentNodeDef.of(
                "3866", -4042.34f, -11081.7f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.12)
                ),
                List.of("14712", "32258")
        ));

        list.add(TalentNodeDef.of(
                "40511", -6536.72f, -6333.67f,
                "minion_life", "passives_minionlife",
                List.of(),
                List.of("55375", "58368")
        ));

        list.add(TalentNodeDef.of(
                "40894", -4550.58f, -6058.9f,
                "minion_life", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1)
                ),
                List.of("1218", "17378", "46628")
        ));

        list.add(TalentNodeDef.of(
                "58368", -6753.23f, -6458.67f,
                "minion_life", "passives_minionlife",
                List.of(),
                List.of("40511", "53910")
        ));

        list.add(TalentNodeDef.of(
                "10055", -9574.83f, -3015.42f,
                "minion_life_and_chaos_resistance", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.07)
                ),
                List.of("30554", "41497")
        ));

        list.add(TalentNodeDef.of(
                "41497", -9449.83f, -2798.91f,
                "minion_life_and_chaos_resistance", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.07)
                ),
                List.of("10055", "21164")
        ));

        list.add(TalentNodeDef.of(
                "54632", -1522.24f, -7187.57f,
                "minion_life_and_chaos_resistance", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.07)
                ),
                List.of("36507", "56956")
        ));

        list.add(TalentNodeDef.of(
                "56956", -1664.27f, -7433.57f,
                "minion_life_and_chaos_resistance", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_CHAOS_RESISTANCES, 0.07)
                ),
                List.of("15885", "54632")
        ));

        list.add(TalentNodeDef.of(
                "15358", 1929.06f, -6414.33f,
                "minion_life_and_minion_revive_speed", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_REVIVING_FASTER, 0.08)
                ),
                List.of("10320", "40783", "44255")
        ));

        list.add(TalentNodeDef.of(
                "18245", -9916.34f, -2673.91f,
                "minion_life_and_physical_damage_reduction", "passives_minionlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.08)
                ),
                List.of("21164", "30554")
        ));

        list.add(TalentNodeDef.of(
                "14575", -2730.42f, -9028.75f,
                "minion_lightning_resistance", "passives_lightningresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_LIGHTNING_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_LIGHTNING_RESISTANCES, 0.03)
                ),
                List.of("3414")
        ));

        list.add(TalentNodeDef.of(
                "3414", -2625.49f, -8808.47f,
                "minion_lightning_resistance", "passives_lightningresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_LIGHTNING_RESISTANCES, 0.2)
                ),
                List.of("14575", "2606")
        ));

        list.add(TalentNodeDef.of(
                "63926", 2393.91f, -9611.59f,
                "minion_lightning_resistance", "passives_lightningresistnode",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_LIGHTNING_RESISTANCES, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_TO_MAX_LIGHTNING_RESISTANCES, 0.03)
                ),
                List.of("4271")
        ));
        return list;
    }
}
