package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes12 {

    private MinorNodes12() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "62542", 8175.23f, -4431.21f,
                "flask_charges_gained", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_GAIN, 0.1)
                ),
                List.of("16329", "2559", "57821")
        ));

        list.add(TalentNodeDef.of(
                "16329", 8257.22f, -4239.24f,
                "flask_charges_used", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_USED, -0.05)
                ),
                List.of("39607", "62542")
        ));

        list.add(TalentNodeDef.of(
                "7922", -3917.83f, 5980.24f,
                "flask_duration", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.1)
                ),
                List.of("45962", "48589")
        ));

        list.add(TalentNodeDef.of(
                "28625", 2622.75f, 6355.25f,
                "flask_recovery", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("32135", "35848")
        ));

        list.add(TalentNodeDef.of(
                "35848", 2406.25f, 6480.25f,
                "flask_recovery", "passives_flaskdex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS, 0.1)
                ),
                List.of("28625", "51509")
        ));

        list.add(TalentNodeDef.of(
                "10881", 3832.63f, -9003.35f,
                "focus_energy", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.4)
                ),
                List.of("29240", "33112", "36450")
        ));

        list.add(TalentNodeDef.of(
                "24767", -5928.45f, -8611.42f,
                "focus_energy", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.4)
                ),
                List.of("35408", "36474")
        ));

        list.add(TalentNodeDef.of(
                "33112", 3832.63f, -8669.35f,
                "focus_energy", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.4)
                ),
                List.of("10881", "11838")
        ));

        list.add(TalentNodeDef.of(
                "35408", -6212.5f, -8611.42f,
                "focus_energy", "passives_shieldnodeoffensive",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_FOCUS, 0.4)
                ),
                List.of("23382", "24767", "38596")
        ));

        list.add(TalentNodeDef.of(
                "9151", 7181.82f, 2075.69f,
                "forking_projectiles", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING, 0.25)
                ),
                List.of("33245", "42302")
        ));

        list.add(TalentNodeDef.of(
                "13701", 8684.39f, -5924.71f,
                "freeze_and_chill_resistance", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.1)
                ),
                List.of("17871", "59538")
        ));

        list.add(TalentNodeDef.of(
                "17871", 8602.39f, -5618.69f,
                "freeze_and_chill_resistance", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.1)
                ),
                List.of("13701", "32672")
        ));

        list.add(TalentNodeDef.of(
                "32672", 8602.39f, -5448.69f,
                "freeze_and_chill_resistance", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_CHILL_ON_YOU, -0.05),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.1)
                ),
                List.of("17871", "23362", "4031", "9928")
        ));

        list.add(TalentNodeDef.of(
                "1151", 711.201f, -8279.42f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("36782", "4113")
        ));

        list.add(TalentNodeDef.of(
                "15698", -10933.8f, -816.472f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("28982", "55260", "65")
        ));

        list.add(TalentNodeDef.of(
                "1869", 3602.57f, -4357.0f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("21080", "27095")
        ));

        list.add(TalentNodeDef.of(
                "20787", 10473.3f, 2327.55f,
                "freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("27705", "5390")
        ));

        list.add(TalentNodeDef.of(
                "21080", 3318.64f, -4263.29f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("14890", "1869", "31950")
        ));

        list.add(TalentNodeDef.of(
                "27095", 3801.61f, -4133.88f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("14890", "1869", "48658")
        ));

        list.add(TalentNodeDef.of(
                "33601", -3698.96f, -419.575f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("2863", "35708", "41154")
        ));

        list.add(TalentNodeDef.of(
                "36504", -11068.1f, -481.283f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("35618", "65")
        ));

        list.add(TalentNodeDef.of(
                "36782", 494.695f, -8184.59f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("1151", "3025", "57626")
        ));

        list.add(TalentNodeDef.of(
                "40068", 3060.82f, -850.529f,
                "freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("32683", "48198")
        ));

        list.add(TalentNodeDef.of(
                "4113", 711.201f, -8559.59f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("1151", "4627")
        ));

        list.add(TalentNodeDef.of(
                "41154", -3638.93f, -195.547f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2)
                ),
                List.of("33601", "5692")
        ));

        list.add(TalentNodeDef.of(
                "52799", 0.435f, -11786.5f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("19955", "44455", "47270")
        ));

        list.add(TalentNodeDef.of(
                "53149", 3604.14f, -1164.21f,
                "freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("24647", "32683")
        ));

        list.add(TalentNodeDef.of(
                "5390", 10525.1f, 2720.83f,
                "freeze_buildup", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("16142", "20787")
        ));

        list.add(TalentNodeDef.of(
                "58926", -6376.52f, -9384.97f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("64572", "8982")
        ));

        list.add(TalentNodeDef.of(
                "64572", -6376.52f, -9634.97f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("48925", "58926")
        ));

        list.add(TalentNodeDef.of(
                "65", -10952.1f, -597.249f,
                "freeze_buildup", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.15)
                ),
                List.of("15698", "36504")
        ));

        list.add(TalentNodeDef.of(
                "22063", 8275.5f, -6302.67f,
                "freeze_buildup_and_cold_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.08)
                ),
                List.of("5797", "64415")
        ));

        list.add(TalentNodeDef.of(
                "5797", 8439.5f, -6302.67f,
                "freeze_buildup_and_cold_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.08)
                ),
                List.of("22063", "59538")
        ));

        list.add(TalentNodeDef.of(
                "8982", -6160.01f, -9175.97f,
                "freeze_buildup_and_effect_duration", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.06)
                ),
                List.of("14952", "23382", "58926")
        ));

        list.add(TalentNodeDef.of(
                "19751", -11269.0f, -682.141f,
                "freeze_threshold", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.15)
                ),
                List.of("35618", "55260")
        ));

        list.add(TalentNodeDef.of(
                "55260", -11153.0f, -798.107f,
                "freeze_threshold", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_THRESHOLD, 0.15)
                ),
                List.of("15698", "19751")
        ));

        list.add(TalentNodeDef.of(
                "13341", 3041.57f, 2768.56f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("18451", "56841", "63255")
        ));

        list.add(TalentNodeDef.of(
                "18451", 2899.54f, 3014.56f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("13341", "30047", "56841")
        ));

        list.add(TalentNodeDef.of(
                "24210", 5279.5f, 6427.73f,
                "frenzy_charge_duration", "passives_azmeriwildbear",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("26932", "7526")
        ));

        list.add(TalentNodeDef.of(
                "26932", 5443.5f, 6677.73f,
                "frenzy_charge_duration", "passives_azmeriwildbear",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("24210", "34543")
        ));

        list.add(TalentNodeDef.of(
                "41873", 10923.8f, 1282.08f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("48773", "55995")
        ));

        list.add(TalentNodeDef.of(
                "55995", 11147.8f, 1342.1f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("41873", "57970")
        ));

        list.add(TalentNodeDef.of(
                "56841", 2757.52f, 2768.56f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("13341", "18451", "61312")
        ));

        list.add(TalentNodeDef.of(
                "57970", 11087.8f, 1566.13f,
                "frenzy_charge_duration", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FRENZY_CHARGE_DURATION, 0.2)
                ),
                List.of("21537", "55995")
        ));

        list.add(TalentNodeDef.of(
                "12601", -2349.32f, -2834.69f,
                "gain_max_endurance_charges_on_gaining_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES, 0.02)
                ),
                List.of("35745", "483", "50908")
        ));

        list.add(TalentNodeDef.of(
                "35745", -2349.32f, -2550.64f,
                "gain_max_endurance_charges_on_gaining_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES, 0.02)
                ),
                List.of("12601", "483")
        ));

        list.add(TalentNodeDef.of(
                "483", -2595.32f, -2692.66f,
                "gain_max_endurance_charges_on_gaining_endurance_charge", "passives_chargestr",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES, 0.02)
                ),
                List.of("12601", "35745", "6744")
        ));

        list.add(TalentNodeDef.of(
                "25058", -4208.73f, -7574.4f,
                "gain_max_frenzy_charges_on_gaining_frenzy_charge", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES, 0.02)
                ),
                List.of("4681", "48828")
        ));

        list.add(TalentNodeDef.of(
                "4681", -4350.76f, -7820.4f,
                "gain_max_frenzy_charges_on_gaining_frenzy_charge", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES, 0.02)
                ),
                List.of("25058", "26228", "48828")
        ));

        list.add(TalentNodeDef.of(
                "48828", -4066.71f, -7820.4f,
                "gain_max_frenzy_charges_on_gaining_frenzy_charge", "passives_chargedex",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES, 0.02)
                ),
                List.of("25058", "34840", "4681")
        ));

        list.add(TalentNodeDef.of(
                "13228", -8073.86f, -3150.76f,
                "gain_max_power_charges_on_gaining_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES, 0.02)
                ),
                List.of("39102", "43486")
        ));

        list.add(TalentNodeDef.of(
                "39102", -7799.48f, -3224.28f,
                "gain_max_power_charges_on_gaining_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES, 0.02)
                ),
                List.of("13228", "43486", "51821")
        ));

        list.add(TalentNodeDef.of(
                "43486", -7873.0f, -2949.9f,
                "gain_max_power_charges_on_gaining_power_charge", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES, 0.02)
                ),
                List.of("13228", "39102", "54289")
        ));

        list.add(TalentNodeDef.of(
                "23419", 3494.64f, 10705.4f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("10671", "55930")
        ));

        list.add(TalentNodeDef.of(
                "27638", -3754.81f, 7411.9f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("38010", "54923")
        ));

        list.add(TalentNodeDef.of(
                "32768", -7196.81f, -1657.46f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(),
                List.of("49734", "8107")
        ));

        list.add(TalentNodeDef.of(
                "50820", -10834.9f, -2704.92f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("46399", "65472")
        ));

        list.add(TalentNodeDef.of(
                "54923", -3690.1f, 7653.38f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("13524", "27638")
        ));

        list.add(TalentNodeDef.of(
                "55930", 3494.64f, 10869.4f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("23419", "40687")
        ));

        list.add(TalentNodeDef.of(
                "65472", -10829.6f, -3005.8f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.15)
                ),
                List.of("50820", "9323")
        ));

        list.add(TalentNodeDef.of(
                "8107", -6913.4f, -1483.4f,
                "glory_generation", "passives_increasedphysicaldamage",
                List.of(),
                List.of("18081", "32768")
        ));

        list.add(TalentNodeDef.of(
                "13352", -4173.07f, 7523.97f,
                "glory_generation_and_attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.08)
                ),
                List.of("38010", "59180")
        ));

        list.add(TalentNodeDef.of(
                "34305", -7881.61f, 7618.79f,
                "glory_generation_and_attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.08)
                ),
                List.of("31545", "40736")
        ));

        list.add(TalentNodeDef.of(
                "38010", -3996.29f, 7347.19f,
                "glory_generation_and_attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.08)
                ),
                List.of("13352", "27638", "31903")
        ));

        list.add(TalentNodeDef.of(
                "40736", -8006.61f, 7835.3f,
                "glory_generation_and_attack_damage", "passives_increasedphysicaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.05),
                        TalentNodeGen.stat(StatIds.INC_GLORY_GENERATION, 0.08)
                ),
                List.of("15606", "24801", "34305")
        ));

        list.add(TalentNodeDef.of(
                "3109", -283.095f, 6556.71f,
                "grenade_area", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_AREA_OF_EFFECT, 0.1)
                ),
                List.of("29514", "52274")
        ));

        list.add(TalentNodeDef.of(
                "36169", -703.541f, 6234.1f,
                "grenade_area", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_AREA_OF_EFFECT, 0.1)
                ),
                List.of("29514", "48429")
        ));

        list.add(TalentNodeDef.of(
                "17026", -2317.07f, 11416.7f,
                "grenade_cooldown_recovery_rate", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS, 0.15)
                ),
                List.of("17303", "23373")
        ));

        list.add(TalentNodeDef.of(
                "21077", -617.095f, 6890.71f,
                "grenade_cooldown_recovery_rate", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS, 0.15)
                ),
                List.of("354", "52274")
        ));

        list.add(TalentNodeDef.of(
                "354", -906.348f, 6723.71f,
                "grenade_cooldown_recovery_rate", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS, 0.15)
                ),
                List.of("21077", "48429")
        ));

        list.add(TalentNodeDef.of(
                "48429", -906.347f, 6389.71f,
                "grenade_cooldown_recovery_rate", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS, 0.15)
                ),
                List.of("354", "36169", "58714")
        ));

        list.add(TalentNodeDef.of(
                "18115", 1692.24f, 7842.33f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("39298", "48856")
        ));

        list.add(TalentNodeDef.of(
                "23373", -2566.07f, 11269.5f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("17026", "23428", "8493")
        ));

        list.add(TalentNodeDef.of(
                "23428", -2733.07f, 11558.8f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("23373", "47623")
        ));

        list.add(TalentNodeDef.of(
                "35653", 940.845f, 11379.0f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("35653", "3717", "65468")
        ));

        list.add(TalentNodeDef.of(
                "47623", -2481.07f, 11700.8f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("23428", "38570")
        ));

        list.add(TalentNodeDef.of(
                "48856", 1667.26f, 7652.62f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("17882", "18115")
        ));

        list.add(TalentNodeDef.of(
                "52274", -380.921f, 6792.89f,
                "grenade_damage", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DAMAGE, 0.12)
                ),
                List.of("21077", "21755", "3109")
        ));

        list.add(TalentNodeDef.of(
                "53895", -2109.82f, 11725.8f,
                "grenade_fuse_duration", "passives_mineareaofeffectnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_GRENADE_DETONATION_TIME, -0.15)
                ),
                List.of("17303", "38570")
        ));

        list.add(TalentNodeDef.of(
                "2128", 6402.75f, 9679.75f,
                "hazard_area", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_AREA_OF_EFFECT, 0.1)
                ),
                List.of("45329", "65256")
        ));

        list.add(TalentNodeDef.of(
                "34845", 6636.34f, 9687.17f,
                "hazard_area", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_AREA_OF_EFFECT, 0.1)
                ),
                List.of("60273", "65256")
        ));

        list.add(TalentNodeDef.of(
                "11015", 6470.71f, 9436.59f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("45329", "46431")
        ));

        list.add(TalentNodeDef.of(
                "12329", 10082.8f, 4615.9f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("17523", "18624")
        ));

        list.add(TalentNodeDef.of(
                "17523", 10299.3f, 4990.9f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("12329", "1778", "42032")
        ));

        list.add(TalentNodeDef.of(
                "18624", 9915.79f, 4576.65f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("12329", "39608", "48116")
        ));

        list.add(TalentNodeDef.of(
                "22972", 5206.64f, 5913.96f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("43238", "50879", "7526")
        ));

        list.add(TalentNodeDef.of(
                "46431", 6576.88f, 8924.35f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("11015", "28199", "38463")
        ));

        list.add(TalentNodeDef.of(
                "50879", 5206.64f, 5663.96f,
                "hazard_damage", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DAMAGE, 0.16)
                ),
                List.of("14211", "22972")
        ));

        list.add(TalentNodeDef.of(
                "1778", 10082.8f, 5115.9f,
                "hazard_duration", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DURATION, 0.2)
                ),
                List.of("17523", "30408", "39608")
        ));

        list.add(TalentNodeDef.of(
                "39608", 9866.28f, 4740.9f,
                "hazard_duration", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_DURATION, 0.2)
                ),
                List.of("1778", "18624")
        ));

        list.add(TalentNodeDef.of(
                "28199", 6670.73f, 9432.42f,
                "hazard_immobilisation_buildup", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HAZARD_IMMOBILISATION_BUILDUP, 0.2)
                ),
                List.of("46431", "60273")
        ));

        list.add(TalentNodeDef.of(
                "43238", 4990.13f, 6038.96f,
                "hazard_rearm_chance", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.HAZARDS_HAVE_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED, 0.05)
                ),
                List.of("14211", "22972")
        ));

        list.add(TalentNodeDef.of(
                "2211", -3663.71f, 10946.0f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, 0.2)
                ),
                List.of("38398", "7473")
        ));

        list.add(TalentNodeDef.of(
                "27859", 1188.06f, 8919.62f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, 0.2)
                ),
                List.of("38694", "38763", "57190")
        ));

        list.add(TalentNodeDef.of(
                "28835", 10451.5f, 3456.48f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD, 0.12)
                ),
                List.of("28044", "43088", "56847", "60480", "8157")
        ));

        list.add(TalentNodeDef.of(
                "38694", 1404.56f, 8794.62f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, 0.2)
                ),
                List.of("22188", "27859")
        ));

        list.add(TalentNodeDef.of(
                "56847", 10326.5f, 3239.98f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD, 0.12)
                ),
                List.of("2582", "28835")
        ));

        list.add(TalentNodeDef.of(
                "7473", -3486.93f, 10769.2f,
                "herald_damage", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.HERALD_SKILLS_DEAL_INC_DAMAGE, 0.2)
                ),
                List.of("2211", "64471")
        ));

        list.add(TalentNodeDef.of(
                "178", 10780.0f, 4025.46f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.08)
                ),
                List.of("28044", "43088")
        ));

        list.add(TalentNodeDef.of(
                "22188", 1188.06f, 8419.62f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.06)
                ),
                List.of("21274", "38694", "38763")
        ));

        list.add(TalentNodeDef.of(
                "31898", -3180.74f, 11075.4f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.06)
                ),
                List.of("35284", "3921")
        ));

        list.add(TalentNodeDef.of(
                "35284", -3245.45f, 10833.9f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.06)
                ),
                List.of("31898", "64471")
        ));

        list.add(TalentNodeDef.of(
                "38763", 971.549f, 8794.62f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.06)
                ),
                List.of("22188", "27859")
        ));

        list.add(TalentNodeDef.of(
                "60480", 10593.5f, 3374.48f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.08)
                ),
                List.of("28835")
        ));

        list.add(TalentNodeDef.of(
                "8157", 10309.5f, 3538.48f,
                "herald_reservation", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_RESERVATION_EFFICIENCY_OF_HERALD_SKILLS, 0.08)
                ),
                List.of("28835")
        ));

        list.add(TalentNodeDef.of(
                "30334", -8860.46f, -5753.21f,
                "ignite_duration", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.08)
                ),
                List.of("56214", "9324")
        ));

        list.add(TalentNodeDef.of(
                "56214", -8624.29f, -5655.38f,
                "ignite_duration", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.08)
                ),
                List.of("30334", "43939")
        ));

        list.add(TalentNodeDef.of(
                "9324", -9096.64f, -5655.38f,
                "ignite_duration", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.08)
                ),
                List.of("30334", "47931")
        ));

        list.add(TalentNodeDef.of(
                "27186", -1731.48f, -10153.3f,
                "ignite_effect_on_you", "passives_firedamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.1)
                ),
                List.of("61170", "62963")
        ));

        list.add(TalentNodeDef.of(
                "15522", -10851.3f, 2815.77f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("17348", "24630")
        ));

        list.add(TalentNodeDef.of(
                "17348", -10856.7f, 3116.65f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("11275", "15522")
        ));

        list.add(TalentNodeDef.of(
                "32364", 9807.86f, 5902.41f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("32858", "59083")
        ));

        list.add(TalentNodeDef.of(
                "34290", -1076.87f, -11609.9f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("35324", "57832")
        ));

        list.add(TalentNodeDef.of(
                "44092", -2750.44f, -7272.15f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("31326", "54911")
        ));

        list.add(TalentNodeDef.of(
                "44487", 3188.01f, 4904.86f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("39884", "45137")
        ));

        list.add(TalentNodeDef.of(
                "45137", 3163.03f, 5094.56f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("42250", "44487")
        ));

        list.add(TalentNodeDef.of(
                "57832", -1102.93f, -11411.9f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("34290", "46300")
        ));

        list.add(TalentNodeDef.of(
                "59083", 9865.95f, 5734.96f,
                "ignite_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1)
                ),
                List.of("32364", "34015")
        ));

        list.add(TalentNodeDef.of(
                "1073", 9691.89f, 6018.38f,
                "ignite_magnitude_and_poison_magnitude", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT, 0.06)
                ),
                List.of("261", "32858")
        ));

        list.add(TalentNodeDef.of(
                "17885", -11171.9f, -2340.06f,
                "ignite_magnitude_on_you", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.15)
                ),
                List.of("11392", "23630")
        ));

        list.add(TalentNodeDef.of(
                "19794", -11505.9f, -2340.06f,
                "ignite_magnitude_on_you", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_IGNITE_ON_YOU, -0.15)
                ),
                List.of("23630", "51868")
        ));

        list.add(TalentNodeDef.of(
                "11667", 1599.39f, 10254.6f,
                "immobilisation_buildup", "passives_witchhunter_witchunternode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP_AGAINST_CONSTRUCTS, 0.1)
                ),
                List.of("48617", "60085")
        ));

        list.add(TalentNodeDef.of(
                "16786", 4661.73f, 9346.57f,
                "immobilisation_buildup", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.15)
                ),
                List.of("19001", "20467")
        ));

        list.add(TalentNodeDef.of(
                "19001", 4536.73f, 9130.06f,
                "immobilisation_buildup", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.15)
                ),
                List.of("16786", "29899")
        ));

        list.add(TalentNodeDef.of(
                "3365", 994.563f, 4201.13f,
                "immobilisation_buildup", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.15)
                ),
                List.of("27853", "59289")
        ));

        list.add(TalentNodeDef.of(
                "59289", 1037.01f, 4042.72f,
                "immobilisation_buildup", "passives_trapsmax",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_IMMOBILISATION_BUILDUP, 0.15)
                ),
                List.of("22532", "3365")
        ));

        list.add(TalentNodeDef.of(
                "15141", -9327.78f, -7903.64f,
                "impale_chance", "passives_physicaldamagenode",
                List.of(),
                List.of("19162")
        ));

        list.add(TalentNodeDef.of(
                "52973", -1639.9f, -8988.84f,
                "impale_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_IMPALE_ON_SPELL_HIT_DAMAGE, 0.15)
                ),
                List.of("12851", "57555")
        ));

        list.add(TalentNodeDef.of(
                "57555", -1859.12f, -8970.48f,
                "impale_chance", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_IMPALE_ON_SPELL_HIT_DAMAGE, 0.15)
                ),
                List.of("37608", "52973")
        ));

        list.add(TalentNodeDef.of(
                "12066", 10660.1f, 700.975f,
                "inc_buff_magnitude", "passives_azmeriprimalmonkey",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("34892", "48734")
        ));

        list.add(TalentNodeDef.of(
                "34892", 10457.3f, 856.594f,
                "inc_buff_magnitude", "passives_azmeriprimalmonkey",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.05)
                ),
                List.of("12066", "42226")
        ));

        list.add(TalentNodeDef.of(
                "22962", 7334.44f, 7006.48f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("22817", "58848")
        ));

        list.add(TalentNodeDef.of(
                "29941", 3092.19f, 3615.89f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("18882", "57517", "60829")
        ));

        list.add(TalentNodeDef.of(
                "49473", 3660.16f, 3626.42f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("36630", "61104")
        ));

        list.add(TalentNodeDef.of(
                "55724", 7000.87f, 6623.07f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("22817", "42714")
        ));

        list.add(TalentNodeDef.of(
                "58848", 7584.44f, 7006.48f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("10576", "22962", "24786", "30657")
        ));

        list.add(TalentNodeDef.of(
                "60829", 3332.8f, 3387.42f,
                "incision_chance", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, 0.2)
                ),
                List.of("29941", "36630")
        ));

        list.add(TalentNodeDef.of(
                "31419", -4483.92f, 400.442f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("35787", "53405")
        ));

        list.add(TalentNodeDef.of(
                "31554", -9604.39f, -7864.27f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(),
                List.of("19162", "49929")
        ));

        list.add(TalentNodeDef.of(
                "31805", -7144.51f, 3057.29f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("44461", "52298")
        ));

        list.add(TalentNodeDef.of(
                "35787", -4660.69f, 141.665f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("31419", "42813")
        ));

        list.add(TalentNodeDef.of(
                "39280", 3869.03f, -11167.9f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("32951", "44669")
        ));

        list.add(TalentNodeDef.of(
                "44461", -7085.11f, 2644.34f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("31805", "54998")
        ));

        list.add(TalentNodeDef.of(
                "44669", 4018.76f, -10609.1f,
                "increased_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("10131", "39280")
        ));

        list.add(TalentNodeDef.of(
                "38103", -2541.03f, -1212.22f,
                "increased_duration_and_stun_threshold", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.08),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.08)
                ),
                List.of("25429", "6898")
        ));

        list.add(TalentNodeDef.of(
                "41838", -2324.53f, -1587.22f,
                "increased_duration_and_stun_threshold", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.08),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD, 0.08)
                ),
                List.of("25429", "28950")
        ));

        list.add(TalentNodeDef.of(
                "18793", -956.525f, -7892.82f,
                "infused_spell_damage", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.15)
                ),
                List.of("33639", "51194")
        ));

        list.add(TalentNodeDef.of(
                "29391", -265.995f, -9489.3f,
                "infused_spell_damage", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.15)
                ),
                List.of("29009", "29800", "50715", "5314", "61354")
        ));

        list.add(TalentNodeDef.of(
                "33639", -1048.03f, -7801.31f,
                "infused_spell_damage", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.15)
                ),
                List.of("18793", "24087")
        ));

        list.add(TalentNodeDef.of(
                "45522", -144.245f, -3752.19f,
                "infused_spell_damage", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.15)
                ),
                List.of("22314", "64046")
        ));

        list.add(TalentNodeDef.of(
                "55909", -351.941f, -4223.57f,
                "infused_spell_damage", "passives_instillationsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY, 0.15)
                ),
                List.of("60230", "64046")
        ));

        list.add(TalentNodeDef.of(
                "44188", 2240.32f, -8220.42f,
                "infusion_and_power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.08)
                ),
                List.of("51892", "64427")
        ));

        list.add(TalentNodeDef.of(
                "51892", 2039.46f, -8421.28f,
                "infusion_and_power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.06)
                ),
                List.of("44188", "59387", "64427")
        ));

        list.add(TalentNodeDef.of(
                "64427", 1965.94f, -8146.9f,
                "infusion_and_power_charge_duration", "passives_chargeint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POWER_CHARGE_DURATION, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_INFUSION_DURATION, 0.06)
                ),
                List.of("17672", "44188", "51892")
        ));
        return list;
    }
}
