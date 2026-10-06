package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes09 {

    private MinorNodes09() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "19341", 1694.52f, 6348.48f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("2486", "56118", "61487")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "22976", 2296.11f, 5712.48f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("42250", "56118")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "36596", 1444.52f, 5915.47f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("45013", "56118", "61487")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "56118", 1992.14f, 5887.98f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("19341", "22976", "36596")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "61487", 1146.89f, 6375.98f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("11410", "19341", "36596")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "63732", 1498.5f, 6090.98f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("2486", "8440")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "64995", -5443.28f, 6511.42f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("17924", "61490")
        ));
        // [Stat] 30% increased Damage with [HitDamage|Hits] against Enemies that are on [LowLife|Low Life]
        list.add(TalentNodeDef.of(
                "8440", 1640.53f, 6172.98f,
                "damage_against_enemies_on_low_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, 0.3)
                ),
                List.of("45013", "63732")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "16938", 3145.61f, 6863.03f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("29930", "32943", "8789")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "30910", 3884.38f, 9875.27f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("18519", "51048", "59647")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "32943", 3189.38f, 6420.76f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("16484", "16938", "29930")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "34702", 8401.82f, 5248.5f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("16568", "54984", "55664", "63246")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "52440", -8417.67f, -5967.44f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("53893", "55635")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "53893", -8335.67f, -5825.41f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("21784", "52440")
        ));
        // [Stat] [Companion|Companions] deal 12% increased Damage
        // [Stat] 10% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "55635", -8430.45f, -6144.21f,
                "damage_and_companion_damage", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_DEAL_INC_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("33722", "51807", "52440")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "21945", 10290.9f, -481.595f,
                "damage_and_criticals_vs_dazed_enemies", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("14262", "26572", "39128", "54545", "61718")
        ));
        // [Stat] 15% increased Damage
        // [Stat] [Minion|Minions] deal 15% increased Damage
        list.add(TalentNodeDef.of(
                "15580", -6090.58f, -3936.52f,
                "damage_and_minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("49938", "61977")
        ));
        // [Stat] 15% increased Damage
        // [Stat] [Minion|Minions] deal 15% increased Damage
        list.add(TalentNodeDef.of(
                "61471", -6090.58f, -4424.52f,
                "damage_and_minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("49153", "61977")
        ));
        // [Stat] 15% increased Damage
        // [Stat] [Minion|Minions] deal 15% increased Damage
        list.add(TalentNodeDef.of(
                "61977", -6155.96f, -4180.52f,
                "damage_and_minion_damage", "passives_miniondamageblue",
                List.of(),
                List.of("15580", "61471")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "49976", 7607.19f, -6773.05f,
                "damage_from_mana", "passives_damage_blue",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("47976", "62936")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "50817", 6203.89f, -5108.92f,
                "damage_from_mana", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("4552", "61355")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "61355", 6281.21f, -5268.4f,
                "damage_from_mana", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("29306", "50817")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "62122", -3304.21f, -8711.35f,
                "damage_from_mana", "passives_damage_blue",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("4295", "6748")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "62936", 7408.15f, -6549.93f,
                "damage_from_mana", "passives_damage_blue",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("49976", "51891")
        ));
        // [Stat] 4% of Damage is taken from Mana before Life
        list.add(TalentNodeDef.of(
                "6748", -3376.96f, -8419.35f,
                "damage_from_mana", "passives_damage_blue",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.04)
                ),
                List.of("48618", "62122")
        ));
        // [Stat] 10% increased Damage if you've dealt a [Critical|Critical Hit] [Recently]
        list.add(TalentNodeDef.of(
                "4519", 4542.03f, -5257.07f,
                "damage_on_critical", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_RECENTLY, 0.1)
                ),
                List.of("13724", "4346")
        ));
        // [Stat] 20% increased Damage against Enemies with [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "15892", -8785.92f, 5827.38f,
                "damage_vs_armor_broken_enemies", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR, 0.2)
                ),
                List.of("13482", "51129")
        ));
        // [Stat] 20% increased Damage against Enemies with [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "30136", -8013.05f, 5379.57f,
                "damage_vs_armor_broken_enemies", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR, 0.2)
                ),
                List.of("13482", "22626")
        ));
        // [Stat] 15% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
        list.add(TalentNodeDef.of(
                "10011", 6344.7f, -1933.58f,
                "damage_vs_blinded", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.15)
                ),
                List.of("28441", "32721")
        ));
        // [Stat] 15% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
        list.add(TalentNodeDef.of(
                "4467", 11018.8f, -2253.64f,
                "damage_vs_blinded", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.15)
                ),
                List.of("42347", "8302")
        ));
        // [Stat] 15% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
        list.add(TalentNodeDef.of(
                "5988", 4068.87f, 1041.23f,
                "damage_vs_blinded", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.15)
                ),
                List.of("38459", "61373")
        ));
        // [Stat] 15% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
        list.add(TalentNodeDef.of(
                "61373", 4216.1f, 792.235f,
                "damage_vs_blinded", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.15)
                ),
                List.of("5988", "63610", "65009")
        ));
        // [Stat] 15% increased Damage with [HitDamage|Hits] against [Blind|Blinded] Enemies
        list.add(TalentNodeDef.of(
                "8302", 10802.3f, -2292.64f,
                "damage_vs_blinded", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_BLINDED_ENEMIES, 0.15)
                ),
                List.of("42974", "4467")
        ));
        // [Stat] 15% increased Damage against [Daze|Dazed] Enemies
        list.add(TalentNodeDef.of(
                "61718", 10148.9f, -399.595f,
                "damage_vs_dazed_enemies", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_DAZED_ENEMIES, 0.15)
                ),
                List.of("21945", "38342")
        ));
        // [Stat] 20% increased Damage with [HitDamage|Hits] against Enemies that are on Full Life
        list.add(TalentNodeDef.of(
                "28371", 8227.16f, -594.435f,
                "damage_vs_full_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.2)
                ),
                List.of("32183", "52630", "60560")
        ));
        // [Stat] 20% increased Damage with [HitDamage|Hits] against Enemies that are on Full Life
        list.add(TalentNodeDef.of(
                "60560", 8443.67f, -719.435f,
                "damage_vs_full_life", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH, 0.2)
                ),
                List.of("28371", "29527")
        ));
        // [Stat] 20% increased Damage against [Immobilised] Enemies
        list.add(TalentNodeDef.of(
                "52576", 1268.94f, 4274.65f,
                "damage_vs_immobilised", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.2)
                ),
                List.of("27853", "64550")
        ));
        // [Stat] 20% increased Damage against [Immobilised] Enemies
        list.add(TalentNodeDef.of(
                "64550", 1311.39f, 4116.24f,
                "damage_vs_immobilised", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.2)
                ),
                List.of("22532", "52576")
        ));
        // [Stat] 10% increased Damage against [Immobilised] Enemies
        // [Stat] 8% increased [Immobilised|Immobilisation] buildup
        list.add(TalentNodeDef.of(
                "27853", 1088.27f, 4400.17f,
                "damage_vs_immobilised_and_buildup", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES, 0.1),
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.08)
                ),
                List.of("3365", "52576", "58109")
        ));
        // [Stat] 12% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "24178", 7184.38f, 2827.83f,
                "damage_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.12)
                ),
                List.of("32655", "40333")
        ));
        // [Stat] 12% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "40333", 7300.34f, 2961.86f,
                "damage_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.12)
                ),
                List.of("24178", "42998")
        ));
        // [Stat] 12% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "42998", 7416.31f, 2827.83f,
                "damage_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.12)
                ),
                List.of("40333", "43102")
        ));
        // [Stat] 12% increased Damage while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "43102", 7550.34f, 2711.86f,
                "damage_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.12)
                ),
                List.of("30197", "42118", "42998")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "64056", 4019.19f, -2510.92f,
                "daze_chance", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("27910", "36931")
        ));
        // [Stat] 15% increased [BuffMagnitude|Magnitude] of [Daze]
        list.add(TalentNodeDef.of(
                "39128", 10432.9f, -563.595f,
                "daze_magnitude", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_DAZE, 0.15)
                ),
                List.of("21945", "47514")
        ));
        // [Stat] 15% increased [BuffMagnitude|Magnitude] of [Daze]
        list.add(TalentNodeDef.of(
                "54545", 10290.9f, -645.595f,
                "daze_magnitude", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_DAZE, 0.15)
                ),
                List.of("21945", "38342")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "16013", 8531.88f, -2509.71f,
                "daze_on_hit", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("16140", "39881", "41811")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "16140", 8363.88f, -2367.69f,
                "daze_on_hit", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("16013", "31286")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "25851", 6172.4f, -4402.58f,
                "daze_on_hit", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("16705", "36270", "52695", "57230")
        ));
        // [Stat] 5% chance to [Daze] on [HitDamage|Hit]
        list.add(TalentNodeDef.of(
                "36270", 6498.21f, -4779.48f,
                "daze_on_hit", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, 0.05)
                ),
                List.of("25851", "5009", "57230")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        list.add(TalentNodeDef.of(
                "11764", 6008.68f, 7466.52f,
                "debuff_expiry", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1)
                ),
                List.of("24240", "38878")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        list.add(TalentNodeDef.of(
                "34898", 6221.64f, 7820.48f,
                "debuff_expiry", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1)
                ),
                List.of("38463", "38878")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        list.add(TalentNodeDef.of(
                "38878", 5986.51f, 7711.18f,
                "debuff_expiry", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1)
                ),
                List.of("11764", "34898")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        list.add(TalentNodeDef.of(
                "11330", -2363.32f, -11149.0f,
                "debuff_expiry_rate", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1)
                ),
                List.of("22185", "25211")
        ));
        // [Stat] [Debuff|Debuffs] on you expire 10% faster
        list.add(TalentNodeDef.of(
                "22185", -2461.15f, -10912.8f,
                "debuff_expiry_rate", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.1)
                ),
                List.of("11330", "17796", "54521")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "22368", 3601.85f, 6420.76f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("16484", "29930", "63182")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "27048", 11024.0f, 2449.53f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("39495", "47375")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "47375", 11305.3f, 2677.45f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("27048", "63618")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "51807", -8253.67f, -6131.44f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("55635", "57089")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "57089", -8111.65f, -6049.44f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("51807", "55450")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "59647", 4051.82f, 9933.36f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("30910", "8791")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "63182", 3645.61f, 6863.03f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("22368", "29930", "8789")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "63618", 11588.9f, 2604.19f,
                "defences_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("31129", "47375")
        ));
        // [Stat] [Companion|Companions] have 12% increased maximum Life
        // [Stat] 10% increased [Armour], [Evasion] and [EnergyShield|Energy Shield] while your [Companion] is in your [Presence]
        list.add(TalentNodeDef.of(
                "16568", 8240.32f, 5528.22f,
                "defenses_and_companion_life", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MAX_HEALTH, 0.12),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("34702", "60992")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "17994", 425.217f, 10440.5f,
                "deflection", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("22565", "63541")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "22565", 309.251f, 10324.5f,
                "deflection", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("17994", "26648")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "26648", 150.84f, 10367.0f,
                "deflection", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("22565", "24256", "52125")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "19224", 2267.06f, 8309.12f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("24963", "32555", "43431")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "20044", 11107.5f, 3229.08f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("2560", "30736")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "20779", 2511.05f, 8397.74f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("43431", "50239")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "22329", 6719.35f, 7863.32f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("32891", "38463", "58526")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "24963", 2165.99f, 8386.67f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("19224", "39298", "42169")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "2560", 10991.6f, 3028.22f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("20044", "53272")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "30736", 10906.7f, 3345.05f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("20044", "52180")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "3170", 8720.15f, 5709.31f,
                "deflection", "passives_azmerivividcat",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("22811", "65161")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "32555", 2386.05f, 8515.23f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("19224", "9535")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "37951", 8411.46f, 4682.13f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("41020", "54984", "9089")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "3994", 8326.57f, 4365.3f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("34908", "41020", "9089")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "40632", 5265.67f, 5355.09f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("48889", "49993")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "41020", 8298.0f, 4564.71f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("37951", "3994")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "42169", 2221.8f, 8564.74f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("24963", "61309")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "43431", 2384.75f, 8260.36f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("19224", "20779", "38814")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "44522", 8430.95f, -1146.01f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("47831", "63762")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "48889", 5049.16f, 5316.09f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("28038", "40632", "56488")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "49993", 5482.17f, 5316.09f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("40632", "64434", "7526")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "53272", 10659.9f, 3019.63f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("2560", "2582")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "58526", 6538.68f, 7737.8f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("22329", "50673")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "63762", 8594.95f, -1146.01f,
                "deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("34541", "44522")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 8% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "65161", 8720.15f, 5873.31f,
                "deflection", "passives_azmerivividcat",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.08)
                ),
                List.of("15625", "3170")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "15343", 9826.58f, -3030.78f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("15775", "58692", "60239")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "21227", 7239.4f, -6236.97f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("36290", "52971")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "23046", 7710.77f, -6363.28f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("36290", "47976")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "26556", 8934.99f, -2788.73f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("58593", "58692", "59657")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "28464", 4599.58f, -6751.84f,
                "deflection_and_energy_delay", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("3203", "8908")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "3203", 4395.4f, -6676.05f,
                "deflection_and_energy_delay", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("28464", "30562")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "36290", 7492.01f, -6236.97f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("21227", "23046")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "47831", 8348.95f, -1003.98f,
                "deflection_and_energy_delay", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("44522", "49996", "8734")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "51040", 7374.83f, -7151.38f,
                "deflection_and_energy_delay", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("58779", "9652")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "58692", 9344.05f, -3024.9f,
                "deflection_and_energy_delay", "passives_energyshieldrechargedeflectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("15343", "26556")
        ));
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "58779", 7538.83f, -7151.38f,
                "deflection_and_energy_delay", "passives_evasionandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("23822", "327", "47976", "51040")
        ));
        // [Stat] 8% increased [Evasion|Evasion Rating]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "14539", 9937.17f, 1028.21f,
                "deflection_and_evasion", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("38728", "44776")
        ));
        // [Stat] 8% increased [Evasion|Evasion Rating]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "32891", 6896.13f, 7686.54f,
                "deflection_and_evasion", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("22329", "30657", "331")
        ));
        // [Stat] 8% increased [Evasion|Evasion Rating]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "331", 6770.61f, 7505.87f,
                "deflection_and_evasion", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("32891", "42103")
        ));
        // [Stat] 8% increased [Evasion|Evasion Rating]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "38728", 9720.67f, 903.215f,
                "deflection_and_evasion", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("14539", "51707")
        ));
        // [Stat] 4% increased [Deflect|Deflection Rating]
        list.add(TalentNodeDef.of(
                "62427", 6538.68f, 7505.87f,
                "deflection_rating", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING, 0.04)
                ),
                List.of("42103", "50673")
        ));
        // [Stat] [Detonator] skills have 8% increased Area of Effect
        list.add(TalentNodeDef.of(
                "15590", -1894.72f, 9825.61f,
                "detonator_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.08)
                ),
                List.of("26356", "9737")
        ));
        // [Stat] [Detonator] skills have 8% increased Area of Effect
        list.add(TalentNodeDef.of(
                "57863", -1411.75f, 9696.2f,
                "detonator_area", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.08)
                ),
                List.of("26356", "3999")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "14769", 9644.04f, 228.555f,
                "dexterity", "passives_azmerivividstag",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("34623", "40471")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "19338", 8692.14f, 819.215f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("58397", "60")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "2334", 9027.14f, 1153.21f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("3209", "58397", "65091")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "24070", 8357.14f, 1153.21f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("35696", "58397")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "31647", 8692.14f, 1487.21f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("23305", "58397")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "3744", 553.431f, 11187.4f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("472", "5332")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "38776", -560.831f, 11190.6f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("32976", "57816")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "40471", 9644.04f, 478.555f,
                "dexterity", "passives_azmerivividstag",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("14769", "19027", "7847")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "472", 407.939f, 11103.4f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("34813", "3744")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "49485", 8389.74f, 2712.82f,
                "dexterity", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("12174", "42118", "49107")
        ));
        // [Stat] +8 to [Dexterity]
        list.add(TalentNodeDef.of(
                "57816", -415.339f, 11106.6f,
                "dexterity", "passives_plusdexterity",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 8.0)
                ),
                List.of("364", "38776")
        ));
        // [Stat] 10% increased Accuracy Rating while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "4377", 499.56f, 5150.83f,
                "dual_wielding_accuracy", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_DUAL_WIELDING, 0.1)
                ),
                List.of("45488", "50273")
        ));
        // [Stat] 12% increased [Attack] Damage while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "50273", 615.525f, 5034.86f,
                "dual_wielding_damage", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_DUAL_WIELDING, 0.12)
                ),
                List.of("3131", "4377", "47893", "63267")
        ));
        // [Stat] 12% increased [Attack] Damage while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "63267", 731.491f, 4918.9f,
                "dual_wielding_damage", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_DUAL_WIELDING, 0.12)
                ),
                List.of("50273", "65424")
        ));
        // [Stat] 12% increased [Attack] Damage while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "65424", 851.699f, 4798.69f,
                "dual_wielding_damage", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_DUAL_WIELDING, 0.12)
                ),
                List.of("58109", "63267")
        ));
        // [Stat] 3% increased [Attack] Speed while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "3131", 731.491f, 5150.83f,
                "dual_wielding_speed", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_DUAL_WIELDING, 0.03)
                ),
                List.of("2394", "50273")
        ));
        // [Stat] 3% increased [Attack] Speed while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "47893", 499.56f, 4918.9f,
                "dual_wielding_speed", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_DUAL_WIELDING, 0.03)
                ),
                List.of("50273", "57774")
        ));
        // [Stat] 3% increased [Attack] Speed while [DualWield|Dual Wielding]
        list.add(TalentNodeDef.of(
                "57774", 379.351f, 4798.69f,
                "dual_wielding_speed", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_DUAL_WIELDING, 0.03)
                ),
                List.of("47893", "49657")
        ));
        list.add(TalentNodeDef.of(
                "50986", 1.945f, 1469.82f,
                "duelist", "passives_damagedualwield",
                List.of(),
                List.of("59779", "59915")
        ));
        // [Stat] 10% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "13294", -3090.94f, -5820.56f,
                "duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("20718", "39990")
        ));
        // [Stat] 10% increased Skill Effect Duration
        list.add(TalentNodeDef.of(
                "20718", -3049.94f, -5641.57f,
                "duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("13294", "30979")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "29402", -307.998f, 9533.69f,
                "electrocute_buildup", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("38270", "4046", "54282")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "4046", -342.747f, 9792.46f,
                "electrocute_buildup", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("29402", "8875")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "5564", 3555.53f, 4441.55f,
                "electrocute_buildup", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("48833", "63585")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "62998", 9731.16f, 3030.3f,
                "electrocute_buildup", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("20649", "63600")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "63600", 9507.95f, 2888.65f,
                "electrocute_buildup", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("36364", "62998")
        ));
        // [Stat] 15% increased [Electrocute|Electrocute Buildup]
        list.add(TalentNodeDef.of(
                "8875", -555.789f, 9751.46f,
                "electrocute_buildup", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELECTROCUTE_BUILDUP, 0.15)
                ),
                List.of("4046", "50687")
        ));
        // [Stat] 10% increased [BuffMagnitude|Magnitude] of [Chill] you inflict
        // [Stat] 10% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict
        list.add(TalentNodeDef.of(
                "37304", 7049.21f, -1033.11f,
                "elemental", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.1)
                ),
                List.of("336", "49388", "61921", "64543")
        ));
        // [Stat] 10% increased [BuffMagnitude|Magnitude] of [Chill] you inflict
        // [Stat] 10% increased [BuffMagnitude|Magnitude] of [Shock|Shock] you inflict
        list.add(TalentNodeDef.of(
                "49388", 7362.17f, -949.251f,
                "elemental", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_CHILL_YOU_INFLICT, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.1)
                ),
                List.of("37304", "38215", "4806")
        ));
        // [Stat] 3% increased [Attack] and Cast Speed with Elemental Skills
        list.add(TalentNodeDef.of(
                "5088", -6246.67f, -7670.09f,
                "elemental", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS, 0.03)
                ),
                List.of("16596", "49537", "59093")
        ));
        // [Stat] 24% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 12% increased [Freeze|Freeze] Buildup
        // [Stat] 12% increased chance to [Shock]
        list.add(TalentNodeDef.of(
                "38068", -1035.43f, -8881.76f,
                "elemental_ailment_chance", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.24),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.12),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.12)
                ),
                List.of("39752", "43139", "44498")
        ));
        // [Stat] 20% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 10% increased [Freeze|Freeze] Buildup
        // [Stat] 10% increased chance to [Shock]
        list.add(TalentNodeDef.of(
                "51968", -812.245f, -3752.19f,
                "elemental_ailment_chance", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.1)
                ),
                List.of("22314", "51335")
        ));
        // [Stat] 24% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 12% increased [Freeze|Freeze] Buildup
        // [Stat] 12% increased chance to [Shock]
        list.add(TalentNodeDef.of(
                "56409", -190.185f, -8881.76f,
                "elemental_ailment_chance", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.24),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.12),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.12)
                ),
                List.of("25101", "43139", "65248")
        ));
        // [Stat] 20% increased [Flammability] [BuffMagnitude|Magnitude]
        // [Stat] 10% increased [Freeze|Freeze] Buildup
        // [Stat] 10% increased chance to [Shock]
        list.add(TalentNodeDef.of(
                "58362", -604.549f, -4223.57f,
                "elemental_ailment_chance", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.1)
                ),
                List.of("51335", "60230")
        ));
        // [Stat] 10% increased Duration of [Ignite], [Shock] and [Chill] on Enemies
        list.add(TalentNodeDef.of(
                "39752", -1035.43f, -8393.76f,
                "elemental_ailment_duration", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_IGNITE_SHOCK_AND_CHILL_ON_ENEMIES, 0.1)
                ),
                List.of("38068", "5936")
        ));
        // [Stat] 10% increased Duration of [Ignite], [Shock] and [Chill] on Enemies
        list.add(TalentNodeDef.of(
                "65248", -190.185f, -8393.76f,
                "elemental_ailment_duration", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_IGNITE_SHOCK_AND_CHILL_ON_ENEMIES, 0.1)
                ),
                List.of("56409", "5936")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "21495", 6679.27f, 5090.26f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("31433", "43044")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "28061", 4697.16f, 3274.72f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("35878", "37389")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "30905", 4121.95f, 8633.22f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("34201", "64140", "8697")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "35878", 4545.36f, 3391.21f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("28061", "50884")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "42794", 7013.27f, 4511.75f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("11825", "31433")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "51336", 3305.51f, 8851.98f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("53965", "56818", "64140")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "56818", 3316.57f, 8506.92f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("43423", "51336", "62510")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "62510", 3524.27f, 8035.54f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("17118", "56818", "8697")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "64140", 3817.57f, 8796.17f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("30905", "3985", "51336")
        ));
        // [Stat] 12% increased [ElementalDamage|Elemental] Damage with [Attack|Attacks]
        list.add(TalentNodeDef.of(
                "8697", 3817.57f, 8217.66f,
                "elemental_attack_damage", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.12)
                ),
                List.of("30905", "38895", "62510")
        ));
        // [Stat] 10% increased [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "10192", -478.245f, -4086.19f,
                "elemental_damage", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE, 0.1)
                ),
                List.of("3823", "60230")
        ));
        return list;
    }
}
