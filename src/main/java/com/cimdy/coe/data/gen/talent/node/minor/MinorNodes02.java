package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes02 {

    private MinorNodes02() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();
        // [Stat] 6% increased Area of Effect
        list.add(TalentNodeDef.of(
                "28516", -7443.51f, -2078.61f,
                "area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("32599", "7542")
        ));
        // [Stat] 6% increased Area of Effect
        list.add(TalentNodeDef.of(
                "32599", -7585.53f, -1996.61f,
                "area_of_effect", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.06)
                ),
                List.of("15913", "28516")
        ));
        // [Stat] 4% increased Area of Effect
        // [Stat] 5% increased Area Damage
        list.add(TalentNodeDef.of(
                "15913", -7727.56f, -2078.61f,
                "area_of_effect_and_damage", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.04),
                        TalentNodeGen.stat(StatIds.INC_AREA_DAMAGE, 0.05)
                ),
                List.of("32599", "49734", "61362")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "10362", -9363.33f, 4400.29f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("10830", "52659", "59777", "6153", "9163")
        ));
        // [Stat] 30% increased [Armour] while stationary
        list.add(TalentNodeDef.of(
                "10713", -11467.2f, -3668.62f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_STATIONARY, 0.3)
                ),
                List.of("16615", "1887")
        ));
        // [Stat] 18% increased [Armour]
        list.add(TalentNodeDef.of(
                "10830", -9238.33f, 4183.79f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.18)
                ),
                List.of("10362", "59589")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "11786", -6009.53f, 9212.65f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05)
                ),
                List.of("29447", "7716")
        ));
        // [Stat] 20% increased [Armour|Armour] if you haven't been [HitDamage|Hit] [Recently|Recently]
        list.add(TalentNodeDef.of(
                "13293", -9848.42f, -542.535f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, 0.2)
                ),
                List.of("30457", "60274")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "13942", -1627.31f, 6429.16f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("18186", "65023")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "18186", -1863.48f, 6331.33f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("13942", "46475")
        ));
        // [Stat] 30% increased [Armour] while stationary
        list.add(TalentNodeDef.of(
                "1887", -11213.7f, -3635.26f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_STATIONARY, 0.3)
                ),
                List.of("10713", "56174")
        ));
        // [Stat] +20 to [Armour]
        list.add(TalentNodeDef.of(
                "1913", -1916.01f, 1377.92f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 20.0)
                ),
                List.of("36709", "38646", "4665")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "21286", -4502.51f, 3669.65f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("4128", "45992")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "26324", -4335.51f, 2953.25f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("27950", "46023", "54283")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "26532", -4171.51f, 3287.25f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("36629", "41657", "46023")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "27726", -2709.02f, 1727.22f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("32416", "7721")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "29447", -6125.5f, 9413.51f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05)
                ),
                List.of("11786", "18157")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "30457", -9598.42f, -292.535f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("13293", "18448", "54416")
        ));
        // [Stat] +20 to [Armour]
        list.add(TalentNodeDef.of(
                "38646", -1397.89f, 957.195f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 20.0)
                ),
                List.of("1913", "23570", "47175")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "4128", -4499.51f, 3287.25f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("21286", "54283", "54811")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "41657", -4168.51f, 3669.65f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("26532", "45992")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "45992", -4335.51f, 3714.39f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("17029", "21286", "41657", "60620")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "46023", -4099.34f, 3051.08f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("26324", "26532")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "54283", -4571.69f, 3051.08f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("26324", "4128")
        ));
        // [Stat] 20% increased [Armour] if you have been [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "54416", -9348.42f, -542.535f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 0.2)
                ),
                List.of("30457", "60274")
        ));
        // [Stat] 30% increased [Armour] while stationary
        list.add(TalentNodeDef.of(
                "56174", -11058.1f, -3432.45f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_STATIONARY, 0.3)
                ),
                List.of("1887", "28201")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "60274", -9598.42f, -792.535f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("13293", "19236", "54416")
        ));
        // [Stat] 15% increased [Armour]
        list.add(TalentNodeDef.of(
                "6529", -2851.04f, 1481.22f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15)
                ),
                List.of("32416", "5710")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "7716", -6210.39f, 9096.68f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05)
                ),
                List.of("11786", "51561")
        ));
        // [Stat] 18% increased [Armour]
        list.add(TalentNodeDef.of(
                "9163", -9146.82f, 4525.29f,
                "armor", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.18)
                ),
                List.of("10362", "44952")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Cold|Cold Damage]
        list.add(TalentNodeDef.of(
                "19820", -8400.74f, 2308.73f,
                "armor_and_applies_to_cold_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.1)
                ),
                List.of("25315", "5686")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Cold|Cold Damage]
        list.add(TalentNodeDef.of(
                "25315", -8176.71f, 2248.71f,
                "armor_and_applies_to_cold_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.1)
                ),
                List.of("19820", "25591")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Cold|Cold Damage]
        list.add(TalentNodeDef.of(
                "25591", -8116.69f, 2472.73f,
                "armor_and_applies_to_cold_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.1)
                ),
                List.of("25315", "4527")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Fire|Fire Damage]
        list.add(TalentNodeDef.of(
                "14655", -5441.63f, 5275.08f,
                "armor_and_applies_to_fire_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.1)
                ),
                List.of("34090", "372")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Fire|Fire Damage]
        list.add(TalentNodeDef.of(
                "34090", -5677.8f, 5261.25f,
                "armor_and_applies_to_fire_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.1)
                ),
                List.of("14655", "64870")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Fire|Fire Damage]
        list.add(TalentNodeDef.of(
                "64870", -5677.8f, 5511.25f,
                "armor_and_applies_to_fire_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.1)
                ),
                List.of("27439", "34090")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Lightning|Lightning Damage]
        list.add(TalentNodeDef.of(
                "23797", -7716.58f, 6730.73f,
                "armor_and_applies_to_lightning_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("31370", "41147")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Lightning|Lightning Damage]
        list.add(TalentNodeDef.of(
                "31370", -8000.63f, 6566.73f,
                "armor_and_applies_to_lightning_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("23797", "32448")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] +10% of [Armour] also applies to [Lightning|Lightning Damage]
        list.add(TalentNodeDef.of(
                "41147", -7940.6f, 6790.76f,
                "armor_and_applies_to_lightning_damage", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.1)
                ),
                List.of("23797", "3446")
        ));
        // [Stat] 5% increased [Block] chance
        // [Stat] 15% increased [Armour] while stationary
        list.add(TalentNodeDef.of(
                "28201", -10909.3f, -3219.7f,
                "armor_and_block_chance", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_STATIONARY, 0.15)
                ),
                List.of("10636", "29611", "56174")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] 10% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "10452", -4794.25f, -7794.74f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.1)
                ),
                List.of("1878", "31238", "44213")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "10635", -4917.34f, -791.755f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("31724", "51183")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "12777", -1970.21f, -1580.6f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("28950", "32597")
        ));
        // [Stat] +10 to [Armour]
        // [Stat] +5 to maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "13855", -1396.89f, -954.585f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 10.0),
                        TalentNodeGen.stat(StatIds.TO_MAX_ENERGY_SHIELD, 5.0)
                ),
                List.of("50626", "54708", "61525", "64370")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "14342", -10591.5f, -5180.09f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("23940", "49256")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "14439", -10158.4f, -5180.09f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("49256", "5728")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "17349", -10375.0f, -5555.09f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("23940", "5728")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "21081", -2786.04f, -2910.9f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("25745", "38856")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "25745", -2213.53f, -3057.25f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("21081", "31890")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "31890", -2134.92f, -3566.32f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("25745", "38827")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "32597", -1811.8f, -1538.15f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("12777", "50626")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "37609", -9479.92f, -3960.99f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("21213", "31290", "32764", "60332")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "38827", -2392.83f, -3721.36f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("1546", "31890")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "49256", -10375.0f, -4971.09f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("14342", "14439", "55888")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "506", -7162.06f, -2924.46f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("6416", "64357")
        ));
        // [Stat] +10 to [Armour]
        // [Stat] +5 to maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "50626", -1769.35f, -1379.74f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 10.0),
                        TalentNodeGen.stat(StatIds.TO_MAX_ENERGY_SHIELD, 5.0)
                ),
                List.of("13855", "32597")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] 10% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "51183", -5081.34f, -627.755f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.1)
                ),
                List.of("10635", "45301", "51052")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "55888", -10371.3f, -4590.02f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("18746", "32474", "440", "49256")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "6416", -7222.09f, -3148.48f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("506", "51821")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 12% increased maximum [EnergyShield|Energy Shield]
        list.add(TalentNodeDef.of(
                "64357", -6938.04f, -2984.48f,
                "armor_and_energy", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.12)
                ),
                List.of("50062", "506")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "10824", -8364.03f, -2026.93f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("25648", "49734")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "25648", -8633.67f, -1944.46f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("10824", "45736", "58894")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "26592", -9172.79f, -2155.91f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("15825", "43077", "58894")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "31290", -9604.92f, -4177.5f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("32474", "32764", "37609", "60332")
        ));
        // [Stat] +8% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "32764", -9696.43f, -3835.99f,
                "armor_and_energy", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.08)
                ),
                List.of("21213", "31290", "37609")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "43077", -9308.75f, -2236.37f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("26592", "43250")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "45736", -8850.17f, -1819.46f,
                "armor_and_energy", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("15825", "25648")
        ));
        // [Stat] 6% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "60332", -9263.42f, -4085.99f,
                "armor_and_energy", "passives_elementalresistance2",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.06)
                ),
                List.of("21213", "31290", "37609")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "44213", -4634.77f, -7717.42f,
                "armor_and_energy_delay", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("10452", "869")
        ));
        // [Stat] 12% increased [Armour]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "869", -4699.48f, -7475.94f,
                "armor_and_energy_delay", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.12),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("18959", "44213")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "1019", 1826.62f, 8513.85f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("21438", "39298", "45037")
        ));
        // [Stat] +10 to [Armour]
        // [Stat] +8 to [Evasion] Rating
        list.add(TalentNodeDef.of(
                "11311", -312.768f, 2219.24f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 10.0),
                        TalentNodeGen.stat(StatIds.TO_EVASION, 8.0)
                ),
                List.of("38057", "59779")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "1221", 1234.8f, 5027.55f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("35743", "59281", "62237")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "21438", 2068.11f, 8578.56f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("1019", "10998")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "23888", -1367.15f, 11261.0f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("29399", "54099", "7390")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "24009", -1518.42f, 7010.4f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("16744", "21755", "34433")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "28693", -1413.62f, 3620.35f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("45969", "49280")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "29399", -1656.4f, 11428.0f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("23888", "51446")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "34061", -429.085f, 2538.81f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("38057", "52442", "56910")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "34433", -1747.52f, 6781.3f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("24009", "32354")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "36170", -1498.51f, 3937.18f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("49280", "53853", "7628")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "38057", -391.963f, 2371.37f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("11311", "34061")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "38779", 143.973f, 2911.7f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("44605", "44836")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "45037", 1585.14f, 8578.56f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("1019", "9736")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "46475", -2208.55f, 6320.27f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("16385", "18186", "51299", "6626")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "47150", -140.083f, 2911.7f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("44836", "56910")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "49280", -1214.58f, 3843.47f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("28693", "36170")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "53647", -1367.15f, 11929.0f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("17150", "51446")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "59281", 1152.8f, 4885.53f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("1221", "14394", "50124", "58109")
        ));
        // [Stat] +10 to [Armour]
        // [Stat] +8 to [Evasion] Rating
        list.add(TalentNodeDef.of(
                "59779", -133.155f, 1690.41f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ARMOR, 10.0),
                        TalentNodeGen.stat(StatIds.TO_EVASION, 8.0)
                ),
                List.of("11311", "42350", "50986", "97")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "61318", 1740.18f, 8836.47f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("2843", "61396", "9736")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "61396", 1913.07f, 8836.47f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("10998", "2843", "61318")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "6626", -1979.45f, 6549.37f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("32354", "46475")
        ));
        // [Stat] 12% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "7390", -1077.89f, 11428.0f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.12)
                ),
                List.of("17150", "23888")
        ));
        // [Stat] +2% to [Resistances|Cold Resistance]
        // [Stat] 8% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "50124", 1376.82f, 4945.55f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_COLD_RESISTANCE, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.08)
                ),
                List.of("59281", "62237")
        ));
        // [Stat] 10% increased [Evasion|Evasion Rating]
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        list.add(TalentNodeDef.of(
                "24256", 150.839f, 10598.9f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05)
                ),
                List.of("26648", "63541")
        ));
        // [Stat] +2% to [Resistances|Lightning Resistance]
        // [Stat] 8% increased [Armour] and [Evasion] Rating
        list.add(TalentNodeDef.of(
                "14394", 1092.77f, 5109.55f,
                "armor_and_evasion", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.08)
                ),
                List.of("35743", "59281")
        ));
        // [Stat] 20% increased [Armour|Armour] while [Surrounded|Surrounded]
        // [Stat] 20% increased [Evasion|Evasion Rating] while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "29914", -4250.13f, 9332.37f,
                "armor_and_evasion_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SURROUNDED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_SURROUNDED, 0.2)
                ),
                List.of("46931", "55617")
        ));
        // [Stat] 20% increased [Armour|Armour] while [Surrounded|Surrounded]
        // [Stat] 20% increased [Evasion|Evasion Rating] while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "45585", -4659.19f, 8923.31f,
                "armor_and_evasion_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SURROUNDED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_SURROUNDED, 0.2)
                ),
                List.of("17825", "3339", "37258", "55617")
        ));
        // [Stat] 20% increased [Armour|Armour] while [Surrounded|Surrounded]
        // [Stat] 20% increased [Evasion|Evasion Rating] while [Surrounded|Surrounded]
        list.add(TalentNodeDef.of(
                "55617", -4336.57f, 9009.75f,
                "armor_and_evasion_while_surrounded", "passives_physicaldamageovertimenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SURROUNDED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_SURROUNDED, 0.2)
                ),
                List.of("19546", "29914", "45585")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] 5% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "21291", -10259.1f, 1760.17f,
                "armor_and_slow_effect_on_you", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.05)
                ),
                List.of("32836", "59785")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] 5% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "32836", -10216.6f, 1601.76f,
                "armor_and_slow_effect_on_you", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.05)
                ),
                List.of("21291", "675")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] 5% reduced [Slow|Slowing] Potency of [Debuff|Debuffs] on You
        list.add(TalentNodeDef.of(
                "675", -9942.25f, 1675.28f,
                "armor_and_slow_effect_on_you", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.05)
                ),
                List.of("13489", "32836")
        ));
        // [Stat] +15% of [Armour] also applies to [Cold|Cold Damage]
        list.add(TalentNodeDef.of(
                "33939", -9598.33f, 421.413f,
                "armor_applies_to_cold_damage_hits", "passives_coldresist",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.15)
                ),
                List.of("62034", "6872")
        ));
        // [Stat] +15% of [Armour] also applies to [Cold|Cold Damage]
        list.add(TalentNodeDef.of(
                "6872", -9598.42f, 252.095f,
                "armor_applies_to_cold_damage_hits", "passives_coldresist",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, 0.15)
                ),
                List.of("18448", "33939")
        ));
        // [Stat] +6% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "17750", -3082.14f, 8288.95f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("61657", "658")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "3191", -2957.14f, 8839.46f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("24736", "9863")
        ));
        // [Stat] +4% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 6% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "35623", -2720.97f, 8741.63f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.04),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.06)
                ),
                List.of("24736", "45808")
        ));
        // [Stat] +4% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 6% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "45808", -2707.14f, 8505.46f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.04),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.06)
                ),
                List.of("35623", "61657")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "61657", -2790.14f, 8216.2f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("13279", "17750", "45808")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 5% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "65324", -3246.4f, 8672.46f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.05)
                ),
                List.of("1861", "9863")
        ));
        // [Stat] +6% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] Gain [Deflect|Deflection Rating] equal to 4% of [Evasion|Evasion Rating]
        list.add(TalentNodeDef.of(
                "658", -3279.76f, 8419.01f,
                "armor_applies_to_elemental_damage_and_deflection", "passives_armorelementaldamagedeflect",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION, 0.04)
                ),
                List.of("17750", "1861")
        ));
        // [Stat] +6% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 3% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "32964", -5488.32f, -6979.69f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.03)
                ),
                List.of("34617", "46318")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "40377", -5557.5f, -6454.26f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("46318", "46628", "7554")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "41384", -6780.92f, -3242.76f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("51672", "65509")
        ));
        // [Stat] +6% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 3% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "46318", -5401.88f, -6657.07f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.03)
                ),
                List.of("32964", "40377")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "51672", -6903.17f, -2786.51f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("31955", "41384")
        ));
        // [Stat] +5% of [Armour|Armour] also applies to [ElementalDamage|Elemental Damage]
        // [Stat] 4% [FasterESRechargeStart|faster start of Energy Shield Recharge]
        list.add(TalentNodeDef.of(
                "65509", -7237.17f, -3365.02f,
                "armor_applies_to_elemental_damage_and_energy_delay", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.04)
                ),
                List.of("41384", "51821")
        ));
        // [Stat] +15% of [Armour] also applies to [Fire|Fire Damage]
        list.add(TalentNodeDef.of(
                "55931", -10099.3f, 710.665f,
                "armor_applies_to_fire_damage_hits", "passives_fireresist",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.15)
                ),
                List.of("62034", "62313")
        ));
        // [Stat] +15% of [Armour] also applies to [Fire|Fire Damage]
        list.add(TalentNodeDef.of(
                "62313", -10412.3f, 824.752f,
                "armor_applies_to_fire_damage_hits", "passives_fireresist",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, 0.15)
                ),
                List.of("1200", "55931")
        ));
        // [Stat] +15% of [Armour] also applies to [Lightning|Lightning Damage]
        list.add(TalentNodeDef.of(
                "4442", -9598.33f, 999.917f,
                "armor_applies_to_light_damage_hits", "passives_lightningstr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.15)
                ),
                List.of("59886", "62034")
        ));
        // [Stat] +15% of [Armour] also applies to [Lightning|Lightning Damage]
        list.add(TalentNodeDef.of(
                "59886", -9169.88f, 988.325f,
                "armor_applies_to_light_damage_hits", "passives_lightningstr",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.15)
                ),
                List.of("23307", "4442")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "12465", 2670.71f, 10656.9f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("30040", "58814")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "25300", -8308.16f, 504.355f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("10100", "38066", "61796")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "26437", -9022.09f, 5418.32f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("42111", "51129")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "28860", -2262.02f, 4750.59f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("14091", "56104")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "30040", 2754.71f, 10511.4f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("12465", "56016")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "42111", -8639.23f, 5196.46f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("21387", "26437", "45227")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "45227", -8249.22f, 4970.51f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("22626", "42111")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "4948", -5164.11f, 8167.23f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("20350", "62785")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "56104", -2301.28f, 4917.59f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("28860", "42981")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "62785", -5373.11f, 7950.72f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("28542", "4948")
        ));
        // [Stat] [ArmourBreak|Break] 20% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "65498", 9517.38f, -256.505f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.2)
                ),
                List.of("37026", "7294")
        ));
        // [Stat] 20% increased Damage against Enemies with [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "20350", -5331.11f, 8456.48f,
                "armor_break", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_ENEMIES_WITH_FULLY_BROKEN_ARMOR, 0.2)
                ),
                List.of("28304", "47006", "4948")
        ));
        // [Stat] [ArmourBreak|Break] [Armour] on [Critical|Critical Hit] with [Spell|Spells] equal to 5% of [Physical|Physical] Damage dealt
        list.add(TalentNodeDef.of(
                "32727", -1724.79f, -9305.67f,
                "armor_break", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, 0.05)
                ),
                List.of("12851", "53524")
        ));
        // [Stat] [ArmourBreak|Break] [Armour] on [Critical|Critical Hit] with [Spell|Spells] equal to 5% of [Physical|Physical] Damage dealt
        list.add(TalentNodeDef.of(
                "53524", -1923.83f, -9211.96f,
                "armor_break", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, 0.05)
                ),
                List.of("32727", "37608")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] [ArmourBreak|Break] 15% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "10286", -7976.54f, 578.735f,
                "armor_break_and_armor", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.15)
                ),
                List.of("21453", "38066")
        ));
        // [Stat] 10% increased [Armour]
        // [Stat] [ArmourBreak|Break] 15% increased [Armour|Armour]
        list.add(TalentNodeDef.of(
                "38066", -8193.04f, 703.735f,
                "armor_break_and_armor", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.15)
                ),
                List.of("10286", "25300")
        ));
        // [Stat] [ArmourBreak|Break] 10% increased [Armour|Armour]
        // [Stat] 6% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "14091", -2012.03f, 4668.59f,
                "armor_break_and_physical_damage", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.06)
                ),
                List.of("23993", "28860", "36449", "41646")
        ));
        // [Stat] [ArmourBreak|Break] 10% increased [Armour|Armour]
        // [Stat] 6% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "37026", 9659.41f, -256.505f,
                "armor_break_and_physical_damage", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.06)
                ),
                List.of("27513", "39307", "65498")
        ));
        // [Stat] [ArmourBreak|Break] 10% increased [Armour|Armour]
        // [Stat] 6% increased [Physical] Damage
        list.add(TalentNodeDef.of(
                "7294", 9446.37f, -133.505f,
                "armor_break_and_physical_damage", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.1),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.06)
                ),
                List.of("39307", "41017", "65498")
        ));
        // [Stat] 20% increased [ArmourBreak|Armour Break] Duration
        list.add(TalentNodeDef.of(
                "61796", -8409.55f, 328.735f,
                "armor_break_duration", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_DURATION, 0.2)
                ),
                List.of("25300", "8260")
        ));
        // [Stat] 20% increased [ArmourBreak|Armour Break] Duration
        list.add(TalentNodeDef.of(
                "8260", -8193.04f, 203.735f,
                "armor_break_duration", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_DURATION, 0.2)
                ),
                List.of("21453", "61796")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "26830", 2467.21f, 11009.3f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("58814")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "35594", 2937.74f, 10522.4f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("56016")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "43174", -5748.11f, 8167.23f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("28542", "47006")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "43964", 5614.17f, -8527.56f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("14082", "41645")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "47006", -5665.11f, 8456.48f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("20350", "43174")
        ));
        // [Stat] 10% increased effect of [ArmourBreak|Fully Broken Armour]
        list.add(TalentNodeDef.of(
                "65149", 2653.69f, 10358.4f,
                "armor_break_effect", "passives_armorbreak1bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_FULLY_BROKEN_ARMOR, 0.1)
                ),
                List.of("56016")
        ));
        // [Stat] 20% increased [Armour] if you've consumed an [Charges|Endurance Charge] [Recently]
        list.add(TalentNodeDef.of(
                "19122", -4889.11f, -169.955f,
                "armor_if_consumed_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY, 0.2)
                ),
                List.of("22616", "23062", "28432")
        ));
        // [Stat] 20% increased [Armour] if you've consumed an [Charges|Endurance Charge] [Recently]
        list.add(TalentNodeDef.of(
                "23062", -4747.08f, -415.955f,
                "armor_if_consumed_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY, 0.2)
                ),
                List.of("19122", "28432")
        ));
        // [Stat] 20% increased [Armour] if you've consumed an [Charges|Endurance Charge] [Recently]
        list.add(TalentNodeDef.of(
                "28432", -4605.06f, -169.955f,
                "armor_if_consumed_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_VE_CONSUMED_AN_ENDURANCE_CHARGE_RECENTLY, 0.2)
                ),
                List.of("19122", "20416", "23062")
        ));
        // [Stat] 20% increased [Armour] if you have been [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "28589", -11158.6f, 1108.17f,
                "armor_if_hit", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 0.2)
                ),
                List.of("30300", "38670")
        ));
        // [Stat] 20% increased [Armour] if you have been [HitDamage|Hit] [Recently]
        list.add(TalentNodeDef.of(
                "38670", -11188.1f, 1314.82f,
                "armor_if_hit", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 0.2)
                ),
                List.of("28589", "3188")
        ));
        return list;
    }
}
