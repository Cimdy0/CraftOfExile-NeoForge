package com.cimdy.coe.data.gen.talent.node;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.effect.TalentEffect;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class KeystoneNodes01 {

    private KeystoneNodes01() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "45202", -7414.29f, -8107.33f,
                "ancestral_bond", "passives_totemmax",
                List.of(
                        TalentNodeGen.stat(StatIds.YOUR_TOTEM_LIMIT_IS_DOUBLED, 1.0),
                        TalentNodeGen.stat(StatIds.NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS, 1.0),
                        TalentNodeGen.stat(StatIds.TOTEMS_RESERVE_SPIRIT_EACH, 75.0)
                ),
                List.of("59093")
        ));

        list.add(TalentNodeDef.of(
                "18684", -10198.3f, 3732.13f,
                "avatar_of_fire", "passives_keystoneavataroffire",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_CONVERTED_TO_FIRE_DAMAGE, 0.75),
                        TalentNodeGen.stat(StatIds.DEAL_NO_NON_FIRE_DAMAGE, 1.0)
                ),
                List.of("27296")
        ));

        list.add(TalentNodeDef.of(
                "42680", -1385.44f, -8272.26f,
                "blackflame_covenant", "passives_firespellsbecomechaosspellskeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.FIRE_SPELLS_CONVERSION_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE, 1.0),
                        TalentNodeGen.stat(StatIds.CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE, 1.0),
                        TalentNodeGen.stat(StatIds.IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE, 1.0)
                ),
                List.of("15885")
        ));

        list.add(TalentNodeDef.of(
                "51749", -10158.4f, 296.695f,
                "blood_magic", "passives_keystonebloodmagic",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_HAVE_NO_MANA, 1.0),
                        TalentNodeGen.stat(StatIds.SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 1.0)
                ),
                List.of("30141")
        ));

        list.add(TalentNodeDef.of(
                "56605", -4163.59f, 6722.67f,
                "bulwark", "passives_bulwarkkeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.DODGE_ROLL_CANNOT_AVOID_DAMAGE),
                        TalentNodeGen.stat(StatIds.TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING, -0.3)
                ),
                List.of("31903")
        ));

        list.add(TalentNodeDef.of(
                "56349", 6372.73f, -8307.67f,
                "chaos_inoculation", "passives_keystonechaosinoculation",
                List.of(
                        TalentNodeGen.stat(StatIds.MAX_HEALTH_IS, 1.0),
                        TalentNodeGen.stat(StatIds.IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING, 1.0)
                ),
                List.of("61403")
        ));

        list.add(TalentNodeDef.of(
                "33979", 4648.31f, -8702.96f,
                "conduit", "passives_keystoneconduit",
                List.of(
                        TalentNodeGen.stat(StatIds.IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD, 1.0)
                ),
                List.of("10382")
        ));

        list.add(TalentNodeDef.of(
                "9085", 4347.0f, 6925.8f,
                "crimson_assault", "passives_crimsonassaultkeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.BLEEDING_YOU_INFLICT_IS_AGGRAVATED, 1.0),
                        TalentNodeGen.stat(StatIds.BASE_BLEEDING_DURATION_IS_SECOND, 1.0),
                        TalentNodeGen.stat(StatIds.MORE_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.5)
                ),
                List.of("18923")
        ));

        list.add(TalentNodeDef.of(
                "14226", 9119.2f, 6374.82f,
                "dance_with_death", "passives_dancewithdeathkeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_SKILL_SPEED_WHILE_OFF_HAND_IS_EMPTY_AND_YOU_HAVE_A_ONE_HANDED_MARTIAL_WEAPON_EQUIPPED_IN_YOUR_MAIN_HAND, 0.25)
                ),
                List.of("24287")
        ));

        list.add(TalentNodeDef.of(
                "57513", 3444.63f, -9280.49f,
                "eldritch_battery", "passives_keystoneeldritchbattery",
                List.of(
                        TalentNodeGen.stat(StatIds.STAT_CONVERSION_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA, 1.0),
                        TalentNodeGen.stat(StatIds.MANA_COSTS_ARE_DOUBLED, 1.0)
                ),
                List.of("29240")
        ));

        list.add(TalentNodeDef.of(
                "46742", -4102.54f, -10310.9f,
                "elemental_equilibrium", "passives_keystoneelementalequilibrium",
                List.of(
                        TalentNodeGen.stat(StatIds.CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE),
                        TalentNodeGen.stat(StatIds.CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING),
                        TalentNodeGen.stat(StatIds.CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD)
                ),
                List.of("50104")
        ));

        list.add(TalentNodeDef.of(
                "33404", 7810.1f, -4823.01f,
                "eternal_youth", "passives_eternalyouth",
                List.of(
                        TalentNodeGen.stat(StatIds.HEALTH_RECHARGE_INSTEAD_OF_ENERGY_SHIELD, 1.0),
                        TalentNodeGen.stat(StatIds.MORE_HEALTH_RECOVERY_FROM_FLASKS, -0.5)
                ),
                List.of("57821")
        ));

        list.add(TalentNodeDef.of(
                "32349", -8206.04f, 7089.12f,
                "giant_s_blood", "passives_giantbloodkeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND, 1.0),
                        TalentNodeGen.stat(StatIds.TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS),
                        TalentNodeGen.stat(StatIds.INHERENT_HEALTH_GRANTED_BY_STRENGTH_IS_HALVED, 1.0)
                ),
                List.of("3446")
        ));

        list.add(TalentNodeDef.of(
                "19288", 2503.68f, 7415.67f,
                "glancing_blows", "passives_glancingblows",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_EVASION_IS_UNLUCKY, 1.0),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DEFLECT_IS_LUCKY, 1.0)
                ),
                List.of("38814")
        ));

        list.add(TalentNodeDef.of(
                "34497", 7768.26f, 228.335f,
                "heartstopper", "passives_heartstopperkeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND, -0.5),
                        TalentNodeGen.stat(StatIds.TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND, 0.5)
                ),
                List.of("12253")
        ));

        list.add(TalentNodeDef.of(
                "64601", 7273.71f, -2633.18f,
                "hollow_palm_technique", "passives_hollowpalmtechniquekeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY, 1.0),
                        TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL),
                        TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS, 0.01),
                        TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS, 0.001)
                ),
                List.of("41877")
        ));

        list.add(TalentNodeDef.of(
                "28492", -1099.85f, 11095.7f,
                "iron_reflexes", "passives_keystoneironreflexes",
                List.of(
                        TalentNodeGen.stat(StatIds.STAT_CONVERSION_ALL_EVASION_TO_ARMOR, 1.0)
                ),
                List.of("54099")
        ));

        list.add(TalentNodeDef.of(
                "61942", -9449.45f, -4442.32f,
                "lord_of_the_wilds", "passives_druidanimism",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN, 1.0),
                        TalentNodeGen.stat(StatIds.MORE_SPIRIT, -0.5),
                        TalentNodeGen.stat(StatIds.NON_MINION_SKILLS_HAVE_MORE_RESERVATION_EFFICIENCY, -0.5)
                ),
                List.of("32474")
        ));

        list.add(TalentNodeDef.of(
                "45918", -4999.75f, -8120.71f,
                "mind_over_matter", "passives_heroicspirit",
                List.of(
                        TalentNodeGen.stat(StatIds.ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 1.0),
                        TalentNodeGen.stat(StatIds.MORE_MANA_RECOVERY_RATE, -0.5)
                ),
                List.of("31238")
        ));

        list.add(TalentNodeDef.of(
                "39935", -3184.23f, -9385.96f,
                "necromantic_talisman", "passives_necromantictalismankeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU, 1.0)
                ),
                List.of("44344")
        ));

        list.add(TalentNodeDef.of(
                "25100", 3733.3f, 5789.19f,
                "oasis", "passives_oasiskeystone2",
                List.of(
                        TalentNodeGen.stat(StatIds.CANNOT_USE_CHARMS),
                        TalentNodeGen.stat(StatIds.MORE_RECOVERY_FROM_FLASKS, 0.3)
                ),
                List.of("16484")
        ));

        list.add(TalentNodeDef.of(
                "55048", -8287.77f, -6378.53f,
                "pain_attunement", "passives_keystonepainattunement",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_FULL_HEALTH, -0.3),
                        TalentNodeGen.stat(StatIds.MORE_CRITICAL_DAMAGE_BONUS_WHEN_ON_LOW_HEALTH, 0.3)
                ),
                List.of("33722")
        ));

        list.add(TalentNodeDef.of(
                "37484", -10073.8f, -2358.14f,
                "primal_hunger", "passives_druidragekeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_MAX_RAGE, 1.0),
                        TalentNodeGen.stat(StatIds.REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY, 1.0),
                        TalentNodeGen.stat(StatIds.NO_RAGE_EFFECT)
                ),
                List.of("41768")
        ));

        list.add(TalentNodeDef.of(
                "44017", -7860.53f, 2931.66f,
                "resolute_technique", "passives_keystoneresolutetechnique",
                List.of(
                        TalentNodeGen.stat(StatIds.ACCURACY_IS_DOUBLED),
                        TalentNodeGen.stat(StatIds.NEVER_DEAL_CRITICAL_HITS, 1.0)
                ),
                List.of("4527")
        ));

        list.add(TalentNodeDef.of(
                "25520", 5975.67f, -4755.44f,
                "resonance", "passives_resonancekeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES, 1.0),
                        TalentNodeGen.stat(StatIds.GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES, 1.0),
                        TalentNodeGen.stat(StatIds.GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES, 1.0)
                ),
                List.of("42379")
        ));

        list.add(TalentNodeDef.of(
                "11230", 1797.54f, -7675.13f,
                "ritual_cadence", "passives_sorceressinvocationspellskeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_SECONDS, 2.0),
                        TalentNodeGen.stat(StatIds.INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS, 1.0),
                        TalentNodeGen.stat(StatIds.INVOKED_SPELLS_CONSUME_MORE_ENERGY, -0.5)
                ),
                List.of("17672")
        ));

        list.add(TalentNodeDef.of(
                "49547", -4997.46f, -3305.76f,
                "scarred_faith", "passives_druidalternateenergyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER, 0.05),
                        TalentNodeGen.stat(StatIds.ENERGY_SHIELD_DOES_NOT_RECHARGE),
                        TalentNodeGen.stat(StatIds.YOU_CANNOT_RECOVER_ENERGY_SHIELD_FROM_REGENERATION),
                        TalentNodeGen.stat(StatIds.YOU_CANNOT_RECOVER_ENERGY_SHIELD_TO_ABOVE_ARMOR)
                ),
                List.of("28002")
        ));

        list.add(TalentNodeDef.of(
                "41861", 9855.92f, 3274.55f,
                "trusted_kinship", "passives_multiplebeastcompanionskeystone",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_CAN_HAVE_TWO_COMPANIONS_OF_DIFFERENT_TYPES, 1.0),
                        TalentNodeGen.stat(StatIds.MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS, 0.3),
                        TalentNodeGen.stat(StatIds.MORE_RESERVATION_EFFICIENCY_OF_NON_COMPANION_SKILLS, -0.2)
                ),
                List.of("2582")
        ));

        list.add(TalentNodeDef.of(
                "14540", -4431.15f, 7184.65f,
                "unwavering_stance", "passives_keystoneunwaveringstance",
                List.of(
                        TalentNodeGen.stat(StatIds.CANNOT_BE_LIGHT_STUNNED, 1.0),
                        TalentNodeGen.stat(StatIds.CANNOT_DODGE_ROLL_OR_SPRINT)
                ),
                List.of("31903")
        ));

        list.add(TalentNodeDef.of(
                "33369", -1760.47f, 8417.92f,
                "vaal_pact", "passives_vaalpact",
                List.of(
                        TalentNodeGen.stat(StatIds.MORE_AMOUNT_OF_HEALTH_LEECHED, 0.5),
                        TalentNodeGen.stat(StatIds.LEECH_HEALTH_MORE_QUICKLY, -0.67),
                        TalentNodeGen.stat(StatIds.CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH),
                        TalentNodeGen.stat(StatIds.HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED, 1.0)
                ),
                List.of("2864")
        ));

        list.add(TalentNodeDef.of(
                "47759", 1436.89f, -10147.3f,
                "whispers_of_doom", "passives_keystonewhispersofdoom",
                List.of(
                        TalentNodeGen.stat(StatIds.YOU_CAN_APPLY_AN_ADDITIONAL_CURSE, 1.0),
                        TalentNodeGen.stat(StatIds.DOUBLE_ACTIVATION_DELAY_OF_CURSES, 1.0)
                ),
                List.of("62677")
        ));

        list.add(TalentNodeDef.of(
                "49363", -3496.22f, -6579.23f,
                "wildsurge_incantation", "passives_druidwildsurgeincantation",
                List.of(
                        TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE, 0.5),
                        TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_COST_MORE, -0.5),
                        TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_HAVE_MORE_DURATION, -0.75)
                ),
                List.of("44733")
        ));

        list.add(TalentNodeDef.of(
                "52", -8682.17f, -4609.88f,
                "zealot_s_oath", "passives_liferegentoenergyshield",
                List.of(
                        TalentNodeGen.stat(StatIds.EXCESS_HEALTH_RECOVERY_FROM_REGENERATION_IS_APPLIED_TO_ENERGY_SHIELD, 1.0),
                        TalentNodeGen.stat(StatIds.ENERGY_SHIELD_DOES_NOT_RECHARGE)
                ),
                List.of("39131")
        ));
        return list;
    }
}
