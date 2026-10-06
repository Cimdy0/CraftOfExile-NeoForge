package com.cimdy.coe.data.gen.talent.node.notable;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;

public final class NotableNodes01 {

    private NotableNodes01() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "7338", 6780.17f, -9399.3f,
                "abasement", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.2),
                        TalentNodeGen.stat(StatIds.GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_MAX_ENERGY_SHIELD, 0.3)
                ),
                List.of("59799")
        ));

        list.add(TalentNodeDef.of(
                "43082", 8246.58f, 6153.46f,
                "acceleration", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.1)
                ),
                List.of("3431")
        ));

        list.add(TalentNodeDef.of(
                "12822", 3607.08f, 2672.84f,
                "adaptable_assault", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.4),
                        TalentNodeGen.stat(StatIds.PROJECTILES_HAVE_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, 0.25)
                ),
                List.of("46705", "9572")
        ));

        list.add(TalentNodeDef.of(
                "43250", -9485.55f, -2055.78f,
                "adaptive_skin", "passives_elementaldominion2",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RESISTANCES_OF_EACH_ELEMENTAL_DAMAGE_TYPE_YOU_HAVE_BEEN_HIT_DAMAGE_WITH_RECENTLY, 0.01)
                ),
                List.of("22697", "34818", "35404", "43077")
        ));

        list.add(TalentNodeDef.of(
                "35876", -6124.76f, 2666.83f,
                "admonisher", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_WARCRY_SPEED, 0.25),
                        TalentNodeGen.stat(StatIds.INC_WARCRY_COOLDOWN_RECOVERY_RATE, 0.25)
                ),
                List.of("27540", "53194")
        ));

        list.add(TalentNodeDef.of(
                "17340", 610.223f, 7627.51f,
                "adrenaline_rush", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY, 0.04),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_IF_YOU_VE_KILLED_RECENTLY, 0.08)
                ),
                List.of("40341", "62051")
        ));

        list.add(TalentNodeDef.of(
                "43829", 541.375f, 9564.7f,
                "advanced_munitions", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_INFLICT_AILMENTS_WITH_PROJECTILES, 0.25)
                ),
                List.of("51944")
        ));

        list.add(TalentNodeDef.of(
                "4295", -3593.46f, -8544.35f,
                "adverse_growth", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, -0.2),
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_MANA, 0.2)
                ),
                List.of("34248", "62122")
        ));

        list.add(TalentNodeDef.of(
                "51335", -655.022f, -3928.97f,
                "affliction_enforcer", "passives_elementaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.4),
                        TalentNodeGen.stat(StatIds.INC_FREEZE_BUILDUP, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.2)
                ),
                List.of("51968", "58362")
        ));

        list.add(TalentNodeDef.of(
                "4716", -2197.48f, 6665.33f,
                "afterimage", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_HIT_AN_ENEMY_RECENTLY, 0.6),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("24748")
        ));

        list.add(TalentNodeDef.of(
                "50253", -5918.9f, 4065.73f,
                "aftershocks", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.SLAM_SKILLS_YOU_USE_YOURSELF_HAVE_INC_AFTERSHOCK_AREA_OF_EFFECT, 0.3)
                ),
                List.of("41493")
        ));

        list.add(TalentNodeDef.of(
                "59938", -6813.53f, -3551.71f,
                "against_the_elements", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENT_THRESHOLD, 0.3),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.15)
                ),
                List.of("62303")
        ));

        list.add(TalentNodeDef.of(
                "6655", -1692.62f, 9062.14f,
                "aggravation", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_ATTACKS, 0.1)
                ),
                List.of("35015", "40336")
        ));

        list.add(TalentNodeDef.of(
                "8896", 8629.29f, 605.385f,
                "agile_sprinter", "passives_movementspeedandevasion",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_SPRINTING, 1.0)
                ),
                List.of("12800")
        ));

        list.add(TalentNodeDef.of(
                "56493", 5310.49f, -445.149f,
                "agile_succession", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_HIT_AN_ENEMY_RECENTLY, 0.3)
                ),
                List.of("47895", "51522")
        ));

        list.add(TalentNodeDef.of(
                "43088", 10740.7f, 3623.48f,
                "agonising_calamity", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHAOS_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_PLAGUE, 0.4),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_BLOOD, 0.4)
                ),
                List.of("178", "28835")
        ));

        list.add(TalentNodeDef.of(
                "55817", -1901.41f, 3924.05f,
                "alchemical_oil", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EXPOSURE_EFFECT, 0.3)
                ),
                List.of("37092")
        ));

        list.add(TalentNodeDef.of(
                "43854", -3313.87f, -3849.91f,
                "all_for_one", "passives_areaofeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PRESENCE_AREA_OF_EFFECT, -0.2),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.12)
                ),
                List.of("65493")
        ));

        list.add(TalentNodeDef.of(
                "58016", -6156.67f, -7247.47f,
                "all_natural", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE, 0.3)
                ),
                List.of("49537", "55101")
        ));

        list.add(TalentNodeDef.of(
                "48974", 7155.22f, -1540.02f,
                "altered_brain_chemistry", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_FROM_FLASKS, 0.25),
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_RATE_DURING_EFFECT_OF_ANY_MANA_FLASK, 0.1)
                ),
                List.of("47418", "63517")
        ));

        list.add(TalentNodeDef.of(
                "23764", 2459.21f, -3622.89f,
                "alternating_current", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_IF_YOU_HAVE_SHOCKED_AN_ENEMY_RECENTLY, 0.25),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.2)
                ),
                List.of("35503")
        ));

        list.add(TalentNodeDef.of(
                "20558", -8143.43f, -2761.74f,
                "among_the_hordes", "passives_minionstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_MOVEMENT_SPEED, 0.1)
                ),
                List.of("40105")
        ));

        list.add(TalentNodeDef.of(
                "2575", -9146.75f, 504.353f,
                "ancestral_alacrity", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_IF_YOU_VE_SUMMONED_A_TOTEM_RECENTLY, 0.08)
                ),
                List.of("11292")
        ));

        list.add(TalentNodeDef.of(
                "26339", -6210.4f, 7207.19f,
                "ancestral_artifice", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.MELEE_ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_TOTEMS, 1.0),
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_RANGE, 0.2)
                ),
                List.of("31017", "64405")
        ));

        list.add(TalentNodeDef.of(
                "51820", -6429.9f, -6729.0f,
                "ancestral_conduits", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_IF_YOU_VE_SUMMONED_A_TOTEM_RECENTLY, 0.12)
                ),
                List.of("38124", "48979", "5398")
        ));

        list.add(TalentNodeDef.of(
                "18419", -6460.4f, 7289.19f,
                "ancestral_mending", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_YOU_HAVE_A_TOTEM, 0.01),
                        TalentNodeGen.stat(StatIds.TOTEMS_REGENERATE_OF_MAX_HEALTH_PER_SECOND, 0.03)
                ),
                List.of("16784", "23667")
        ));

        list.add(TalentNodeDef.of(
                "43396", -5585.44f, -521.455f,
                "ancestral_reach", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_SPEED, 0.25),
                        TalentNodeGen.stat(StatIds.INC_TOTEM_PLACEMENT_RANGE, 0.5)
                ),
                List.of("49192")
        ));

        list.add(TalentNodeDef.of(
                "62609", -5960.4f, 7289.19f,
                "ancestral_unity", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACKS_USED_BY_TOTEMS_HAVE_INC_ATTACK_SPEED_PER_SUMMONED_TOTEM, 0.04)
                ),
                List.of("16051", "24259")
        ));

        list.add(TalentNodeDef.of(
                "5728", -10158.4f, -5430.09f,
                "ancient_aegis", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_FROM_EQUIPPED_BODY_ARMOR, 0.6),
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_FROM_EQUIPPED_BODY_ARMOR, 0.6)
                ),
                List.of("14439", "17349")
        ));

        list.add(TalentNodeDef.of(
                "45422", -4914.48f, -3550.97f,
                "anger_management", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 15.0),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_INHERENT_RAGE_LOSS, 2.0)
                ),
                List.of("34181")
        ));

        list.add(TalentNodeDef.of(
                "62431", 2898.16f, -5489.12f,
                "anticipation", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.SEALED_SKILLS_HAVE_INC_SEAL_GAIN_FREQUENCY, 0.25)
                ),
                List.of("11722")
        ));

        list.add(TalentNodeDef.of(
                "38398", -3547.22f, 11227.2f,
                "apocalypse", "passives_heraldbuffeffectnode2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, -0.3),
                        TalentNodeGen.stat(StatIds.TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS, 0.08)
                ),
                List.of("2211", "3921")
        ));

        list.add(TalentNodeDef.of(
                "46224", 2539.39f, 3317.93f,
                "arcane_alchemy", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.MANA_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("24045", "49799")
        ));

        list.add(TalentNodeDef.of(
                "14324", 177.602f, -6627.3f,
                "arcane_blossom", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_RECOVERY_RATE, 0.15)
                ),
                List.of("1468")
        ));

        list.add(TalentNodeDef.of(
                "19044", 2922.78f, -10055.7f,
                "arcane_intensity", "passives_mana",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_PER_MAX_MANA, 0.03)
                ),
                List.of("4828")
        ));

        list.add(TalentNodeDef.of(
                "46972", 1535.66f, -3686.77f,
                "arcane_mixtures", "passives_flaskint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_IF_YOU_VE_USED_A_MANA_FLASK_RECENTLY, 0.1),
                        TalentNodeGen.stat(StatIds.MANA_FLASKS_GAIN_CHARGES_PER_SECOND, 0.1)
                ),
                List.of("17706")
        ));

        list.add(TalentNodeDef.of(
                "16940", -5240.92f, -3792.19f,
                "arcane_nature", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_WHILE_YOU_HAVE_ARCANE_SURGE, 0.12),
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_WHILE_YOU_HAVE_ARCANE_SURGE, 0.3)
                ),
                List.of("32078")
        ));

        list.add(TalentNodeDef.of(
                "46124", 1185.36f, -4903.67f,
                "arcane_remnants", "passives_remnantnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT, 0.03)
                ),
                List.of("37593")
        ));

        list.add(TalentNodeDef.of(
                "42045", -455.981f, -11082.1f,
                "archon_of_the_blizzard", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ELEMENTAL_ARCHON_WHEN_YOUR_ENERGY_SHIELD_RECHARGE_BEGINS)
                ),
                List.of("50535", "61863")
        ));

        list.add(TalentNodeDef.of(
                "27434", 507.041f, -11053.8f,
                "archon_of_the_storm", "passives_archongenericnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_OF_YOUR_MAX_MANA, 1.0)
                ),
                List.of("41159", "51234")
        ));

        list.add(TalentNodeDef.of(
                "26926", -1647.74f, -11082.0f,
                "archon_of_undeath", "passives_archonofundeathnoteble",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_ARCHON_OF_UNDEATH_WHEN_YOU_USE_A_COMMAND_SKILL, 0.15)
                ),
                List.of("43979", "45333")
        ));

        list.add(TalentNodeDef.of(
                "12245", 2871.31f, 11373.8f,
                "arsonist", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.IGNITES_YOU_INFLICT_DEAL_DAMAGE_FASTER, 0.18)
                ),
                List.of("13610", "59661")
        ));

        list.add(TalentNodeDef.of(
                "14265", -5304.61f, -8799.35f,
                "arsonist", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_WHILE_IGNITED, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_FIRE_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("54868")
        ));

        list.add(TalentNodeDef.of(
                "58817", -2285.04f, 9898.59f,
                "artillery_strike", "passives_rangedtotemdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS, 1.0),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_WHILE_YOU_HAVE_A_TOTEM, 0.15)
                ),
                List.of("40597")
        ));

        list.add(TalentNodeDef.of(
                "12661", 2570.89f, -5775.44f,
                "asceticism", "passives_energyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.STUN_THRESHOLD_IS_BASED_ON_OF_YOUR_ENERGY_SHIELD_INSTEAD_OF_HEALTH, 0.3)
                ),
                List.of("18895")
        ));

        list.add(TalentNodeDef.of(
                "27388", -7003.21f, -5780.9f,
                "aspiring_genius", "passives_manaregeneration",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.2),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_GAIN_ARCANE_SURGE_WHEN_YOU_DEAL_A_CRITICAL_HIT, 0.1)
                ),
                List.of("28578")
        ));

        list.add(TalentNodeDef.of(
                "35560", -3116.51f, -7680.34f,
                "at_your_command", "passives_miniondamageblue",
                List.of(
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS_FOR_EACH_DIFFERENT_TYPE_OF_PERSISTENT_MINION_IN_YOUR_PRESENCE, 0.1)
                ),
                List.of("41511")
        ));

        list.add(TalentNodeDef.of(
                "20397", -1588.53f, 10002.4f,
                "authority", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_COOLDOWN_RECOVERY_RATE, 0.1)
                ),
                List.of("24813", "4577")
        ));

        list.add(TalentNodeDef.of(
                "50673", 6320.65f, 7621.83f,
                "avoiding_deflection", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION, -0.05),
                        TalentNodeGen.stat(StatIds.INC_DEFLECTION_RATING, 0.2)
                ),
                List.of("58526", "62427")
        ));

        list.add(TalentNodeDef.of(
                "33059", 2192.99f, 4881.44f,
                "back_in_action", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.8)
                ),
                List.of("20205")
        ));

        list.add(TalentNodeDef.of(
                "62001", 11090.4f, -5494.33f,
                "backstabbing", "passives_criticaldaggerint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS_WITH_DAGGERS, 0.25)
                ),
                List.of("54058")
        ));

        list.add(TalentNodeDef.of(
                "53853", -1697.55f, 3714.06f,
                "backup_plan", "passives_armorandevasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, 0.4),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, 0.4)
                ),
                List.of("36170")
        ));

        list.add(TalentNodeDef.of(
                "42914", -9353.83f, -3367.59f,
                "ball_and_chain", "passives_macedmg",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_FLAILS, 0.15),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_FLAILS, 0.06)
                ),
                List.of("33393", "50847")
        ));

        list.add(TalentNodeDef.of(
                "62455", -1626.68f, 7851.58f,
                "bannerman", "passives_bannerareanotable",
                List.of(
                        TalentNodeGen.stat(StatIds.BANNER_BUFFS_LINGER_ON_YOU_FOR_SECONDS_AFTER_YOU_LEAVE_THE_AREA, 2.0)
                ),
                List.of("11752", "62258")
        ));

        list.add(TalentNodeDef.of(
                "50562", -5809.71f, 1322.89f,
                "barbaric_strength", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, 0.45),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_OF_SKILLS, 0.1),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("41665")
        ));

        list.add(TalentNodeDef.of(
                "50062", -6998.06f, -3208.51f,
                "barrier_of_venarius", "passives_armorandenergyshieldnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_ENERGY_SHIELD, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_BREAK_TAKEN, -0.25),
                        TalentNodeGen.stat(StatIds.DEFEND_WITH_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD, 1.2)
                ),
                List.of("64357")
        ));

        list.add(TalentNodeDef.of(
                "8916", -5074.1f, -1300.35f,
                "bashing_beast", "passives_druidshapeshiftbearnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_SECONDS, 6.0)
                ),
                List.of("25915")
        ));

        list.add(TalentNodeDef.of(
                "64240", -7951.09f, -155.255f,
                "battle_fever", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("54228")
        ));

        list.add(TalentNodeDef.of(
                "56910", -409.198f, 2766.12f,
                "battle_hardened", "passives_meleerange",
                List.of(
                        TalentNodeGen.stat(StatIds.HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS, -0.2),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH_AND_DEXTERITY, 5.0)
                ),
                List.of("14926", "28510", "29328", "34061", "47150")
        ));

        list.add(TalentNodeDef.of(
                "37276", -5667.08f, 8986.91f,
                "battle_trance", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_MAX_RAGE, 8.0)
                ),
                List.of("52373")
        ));

        list.add(TalentNodeDef.of(
                "41620", -5562.78f, -2451.83f,
                "bear_s_roar", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.4)
                ),
                List.of("14996")
        ));

        list.add(TalentNodeDef.of(
                "59720", 9392.17f, 1722.19f,
                "beastial_skin", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_FROM_EQUIPPED_BODY_ARMOR, 1.0)
                ),
                List.of("3209", "9405")
        ));

        list.add(TalentNodeDef.of(
                "25482", -5457.8f, 4362.34f,
                "beef", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 25.0)
                ),
                List.of("51702", "54485", "60620", "61472")
        ));

        list.add(TalentNodeDef.of(
                "5642", -7586.67f, 2017.05f,
                "behemoth", "passives_areadmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, 0.03),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.08),
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.05)
                ),
                List.of("35921", "57405")
        ));

        list.add(TalentNodeDef.of(
                "10873", -6239.27f, -2888.72f,
                "bestial_rage", "passives_rage",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, 1.0),
                        TalentNodeGen.stat(StatIds.EVERY_RAGE_ALSO_GRANTS_INC_PHYSICAL_DAMAGE, 10.0)
                ),
                List.of("17625")
        ));

        list.add(TalentNodeDef.of(
                "15825", -8992.2f, -1987.46f,
                "bhatair_s_storm", "passives_armorelementaldamageenergyshieldrecharge",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, 0.12),
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE, 0.08),
                        TalentNodeGen.stat(StatIds.ARCHON_RECOVERY_PERIOD_EXPIRES_FASTER, 0.1),
                        TalentNodeGen.stat(StatIds.INC_EFFECT_OF_ARCHON_BUFFS_ON_YOU, 0.1)
                ),
                List.of("26592", "45736")
        ));

        list.add(TalentNodeDef.of(
                "38329", 8287.51f, 1489.62f,
                "biting_frost", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FREEZE_DURATION_ON_ENEMIES, -0.2),
                        TalentNodeGen.stat(StatIds.ENEMIES_FROZEN_BY_YOU_TAKE_INC_COLD_DAMAGE, 0.2)
                ),
                List.of("8456")
        ));

        list.add(TalentNodeDef.of(
                "17029", -4335.51f, 3544.39f,
                "blade_catcher", "passives_dmgreduction",
                List.of(
                        TalentNodeGen.stat(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS, 2.0),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 15.0)
                ),
                List.of("45992")
        ));

        list.add(TalentNodeDef.of(
                "2394", 851.699f, 5271.04f,
                "blade_flurry", "passives_nodedualwieldingdamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_DUAL_WIELDING, 0.06),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_CRITICAL_HIT_CHANCE_WHILE_DUAL_WIELDING, 0.15)
                ),
                List.of("3131")
        ));

        list.add(TalentNodeDef.of(
                "25753", -3777.59f, 5238.17f,
                "blazing_arms", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.16),
                        TalentNodeGen.stat(StatIds.INC_FLAMMABILITY_BUFF_MAGNITUDE, 0.3),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.16)
                ),
                List.of("14205", "25990")
        ));

        list.add(TalentNodeDef.of(
                "18308", -900.155f, 10685.9f,
                "bleeding_out", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ACCURACY_AGAINST_BLEEDING_ENEMIES, 250.0),
                        TalentNodeGen.stat(StatIds.BLEEDING_YOU_INFLICT_DEALS_DAMAGE_FASTER, 0.1)
                ),
                List.of("19796")
        ));

        list.add(TalentNodeDef.of(
                "48925", -6160.01f, -9759.97f,
                "blessing_of_the_moon", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION_PER_ENEMY_YOU_VE_FREEZE_IN_THE_LAST_SECONDS_UP_TO, 0.08)
                ),
                List.of("21985", "64572")
        ));

        list.add(TalentNodeDef.of(
                "42354", 924.865f, 10071.5f,
                "blinding_flash", "passives_evasionandblindnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLIND_EFFECT, 0.2),
                        TalentNodeGen.stat(StatIds.BLIND_ENEMIES_WHEN_THEY_STUN_YOU)
                ),
                List.of("3660", "43562")
        ));

        list.add(TalentNodeDef.of(
                "20916", 5404.98f, -626.595f,
                "blinding_strike", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.24),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE_WITH_ATTACKS, 0.1)
                ),
                List.of("53698")
        ));

        list.add(TalentNodeDef.of(
                "35792", -7025.42f, 8671.21f,
                "blood_of_rage", "passives_flaskstr",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK, 8.0)
                ),
                List.of("26416", "27740")
        ));

        list.add(TalentNodeDef.of(
                "49214", -10248.8f, -3724.53f,
                "blood_of_the_wolf", "passives_druidshapeshiftwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH_WHILE_SHAPESHIFTED, 0.15),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED, 0.15),
                        TalentNodeGen.stat(StatIds.TO_MAX_COLD_RESISTANCE_WHILE_SHAPESHIFTED, 0.01)
                ),
                List.of("36250")
        ));

        list.add(TalentNodeDef.of(
                "39083", -4542.4f, 925.595f,
                "blood_rush", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, 0.06)
                ),
                List.of("9638")
        ));

        list.add(TalentNodeDef.of(
                "58183", -3988.18f, -9392.13f,
                "blood_tearing", "witchbonestorm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE, 0.25)
                ),
                List.of("21245", "60241")
        ));

        list.add(TalentNodeDef.of(
                "48524", -4206.8f, -6656.62f,
                "blood_transfusion", "passives_manastr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE, 0.25),
                        TalentNodeGen.stat(StatIds.OF_SPELL_MANA_COST_STAT_CONVERSION_TO_HEALTH_COST, 0.25)
                ),
                List.of("4623")
        ));

        list.add(TalentNodeDef.of(
                "54990", -983.71f, 4007.42f,
                "bloodletting", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT, 0.15)
                ),
                List.of("40043", "9857")
        ));

        list.add(TalentNodeDef.of(
                "10772", -2087.68f, 7594.08f,
                "bloodthirsty", "passives_lifeleech",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_LEECHING, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_LEECHING, 0.1),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_AND_EVASION_WHILE_LEECHING, 0.3)
                ),
                List.of("2119")
        ));

        list.add(TalentNodeDef.of(
                "35987", 2105.04f, 1665.51f,
                "blur", "passives_finesse",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.2),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("19470", "29582", "29843", "48401", "50437", "55397", "59653")
        ));

        list.add(TalentNodeDef.of(
                "42177", -2553.76f, 5504.65f,
                "blurred_motion", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.1),
                        TalentNodeGen.stat(StatIds.INC_DEXTERITY, 0.05)
                ),
                List.of("11153", "5049")
        ));

        list.add(TalentNodeDef.of(
                "64299", -3423.49f, -1976.64f,
                "bolstering_presence", "passives_auraeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE, 0.12)
                ),
                List.of("12462", "17655")
        ));

        list.add(TalentNodeDef.of(
                "26070", -5840.71f, 2666.83f,
                "bolstering_yell", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_DEAL_INC_DAMAGE, 0.3),
                        TalentNodeGen.stat(StatIds.WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.3)
                ),
                List.of("62216", "62973")
        ));

        list.add(TalentNodeDef.of(
                "712", 8833.93f, 6608.57f,
                "bond_of_the_ape", "passives_azmeriprimalmonkeynotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT, 0.12),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_AREA_OF_EFFECT, 0.3)
                ),
                List.of("65167")
        ));

        list.add(TalentNodeDef.of(
                "1448", 7712.58f, 3564.17f,
                "bond_of_the_cat", "passives_azmerivividcatnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_MOVEMENT_SPEED, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.05)
                ),
                List.of("24269", "6530")
        ));

        list.add(TalentNodeDef.of(
                "47853", 9566.38f, 3484.69f,
                "bond_of_the_mamba", "passives_azmeriprimalsnakenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.04),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, 0.12)
                ),
                List.of("28414", "43064")
        ));

        list.add(TalentNodeDef.of(
                "52568", 2776.83f, 7019.78f,
                "bond_of_the_owl", "passives_azmeriprimalowlnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE, 0.12)
                ),
                List.of("3665")
        ));

        list.add(TalentNodeDef.of(
                "34478", 9189.45f, 3777.45f,
                "bond_of_the_viper", "passives_azmeriprimalsnakenotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.16),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_A_CHANCE_TO_POISON_ON_HIT_DAMAGE, 0.4)
                ),
                List.of("43064")
        ));

        list.add(TalentNodeDef.of(
                "5191", 5620.6f, 9982.22f,
                "bond_of_the_wolf", "passives_azmerivividwolfnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILL, 0.5)
                ),
                List.of("44684", "46961")
        ));

        list.add(TalentNodeDef.of(
                "17725", -7063.4f, -2210.03f,
                "bonded_precision", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_CRITICAL_HIT_CHANCE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.25)
                ),
                List.of("18737")
        ));

        list.add(TalentNodeDef.of(
                "26563", 5171.37f, -10969.2f,
                "bone_chains", "passives_blood2",
                List.of(
                        TalentNodeGen.stat(StatIds.PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED, 1.0)
                ),
                List.of("12778", "48805")
        ));

        list.add(TalentNodeDef.of(
                "52618", -6573.27f, -3222.72f,
                "boon_of_the_beast", "passives_druidgenericshapeshiftnotable",
                List.of(
                        TalentNodeGen.stat(StatIds.WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_GAIN_INC_SPELL_DAMAGE_PER_SECOND_YOU_WERE_SHAPESHIFTED_UP_TO_A_MAX_OF_FOR_SECONDS, 0.1)
                ),
                List.of("59710")
        ));

        list.add(TalentNodeDef.of(
                "15114", -4418.06f, -4540.25f,
                "boundless_growth", "passives_physicaldamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW, 0.2)
                ),
                List.of("32859", "6356", "71")
        ));

        list.add(TalentNodeDef.of(
                "23244", 4963.5f, 8953.29f,
                "bounty_hunter", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, 0.01),
                        TalentNodeGen.stat(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, 0.01),
                        TalentNodeGen.stat(StatIds.INC_CULLING_STRIKE_THRESHOLD, 0.25)
                ),
                List.of("21792", "49497")
        ));

        list.add(TalentNodeDef.of(
                "24721", 5541.32f, -9278.17f,
                "brain_freeze", "passives_colddamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_COLD_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.ENEMIES_FREEZE_BY_YOU_HAVE_TO_COLD_RESISTANCE, -0.08)
                ),
                List.of("32340")
        ));

        list.add(TalentNodeDef.of(
                "50498", 5279.03f, -9458.27f,
                "brain_storm", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_MANA_COST_EFFICIENCY, 0.15)
                ),
                List.of("43303")
        ));

        list.add(TalentNodeDef.of(
                "37806", 3052.85f, -8723.72f,
                "branching_bolts", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME, 0.6)
                ),
                List.of("46157")
        ));

        list.add(TalentNodeDef.of(
                "14777", -5529.77f, 7342.52f,
                "bravado", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.EMPOWERED_ATTACKS_HAVE_INC_STUN_BUILDUP, 0.5),
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_DURING_EMPOWERED_ATTACKS, 1.0)
                ),
                List.of("59466")
        ));

        list.add(TalentNodeDef.of(
                "21453", -8091.65f, 379.355f,
                "breakage", "passives_armorbreak2bufficon",
                List.of(
                        TalentNodeGen.stat(StatIds.ARMOR_BREAK_INC_ARMOR, 0.6),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_DEFEND_WITH_OF_ARMOR, 0.1)
                ),
                List.of("10286", "8260")
        ));

        list.add(TalentNodeDef.of(
                "39347", -8394.82f, 3211.51f,
                "breaking_blows", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.3),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY, 0.12)
                ),
                List.of("51903")
        ));

        list.add(TalentNodeDef.of(
                "7777", -5424.67f, -7670.09f,
                "breaking_point", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DURATION_OF_ELEMENTAL_AILMENTS_ON_ENEMIES, 0.1),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT, 0.3)
                ),
                List.of("22873", "26739")
        ));

        list.add(TalentNodeDef.of(
                "24655", -705.025f, -5123.73f,
                "breath_of_fire", "passives_firedamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("41573")
        ));

        list.add(TalentNodeDef.of(
                "18086", -1437.03f, -5546.35f,
                "breath_of_ice", "passives_colddamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_COLD_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_INTELLIGENCE, 10.0)
                ),
                List.of("62844")
        ));

        list.add(TalentNodeDef.of(
                "61338", -705.025f, -5968.98f,
                "breath_of_lightning", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCE, 0.15),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 10.0)
                ),
                List.of("14363")
        ));

        list.add(TalentNodeDef.of(
                "9535", 2511.05f, 8731.74f,
                "brinerot_ferocity", "passives_lightningstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.04),
                        TalentNodeGen.stat(StatIds.TO_LIGHTNING_RESISTANCE, 0.08),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, 0.3)
                ),
                List.of("32555", "51850")
        ));

        list.add(TalentNodeDef.of(
                "48565", -7183.17f, -6666.4f,
                "bringer_of_order", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.2)
                ),
                List.of("65328")
        ));

        list.add(TalentNodeDef.of(
                "53935", 2804.44f, -3288.1f,
                "briny_carapace", "passives_life1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_THRESHOLD_FOR_EACH_TIME_YOU_VE_BEEN_STUNNED_RECENTLY, 1.0)
                ),
                List.of("10677")
        ));

        list.add(TalentNodeDef.of(
                "63541", 382.771f, 10598.9f,
                "brush_off", "passives_evasionnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR, 0.15),
                        TalentNodeGen.stat(StatIds.PREVENT_OF_DAMAGE_FROM_DEFLECTED_CRITICAL_HIT_DAMAGE, 0.15)
                ),
                List.of("17994", "24256")
        ));

        list.add(TalentNodeDef.of(
                "5710", -2864.47f, 1160.02f,
                "brutal", "passives_strongarm",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP, 0.1),
                        TalentNodeGen.stat(StatIds.INC_MELEE_DAMAGE, 0.16),
                        TalentNodeGen.stat(StatIds.TO_STRENGTH, 10.0)
                ),
                List.of("38323", "43164", "53443", "58528", "6529", "6839")
        ));

        list.add(TalentNodeDef.of(
                "50392", -8217.79f, 6324.33f,
                "brute_strength", "passives_plusstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_MANA, -0.1),
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_PER_STRENGTH, 0.01)
                ),
                List.of("13075")
        ));

        list.add(TalentNodeDef.of(
                "15986", 11513.8f, -1557.81f,
                "building_toxins", "passives_poison",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_POISON_DURATION, -0.25),
                        TalentNodeGen.stat(StatIds.TARGETS_CAN_BE_AFFECTED_BY_OF_YOUR_POISONS_AT_THE_SAME_TIME, 1.0)
                ),
                List.of("6030")
        ));

        list.add(TalentNodeDef.of(
                "53294", -3498.41f, 321.545f,
                "burn_away", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.15),
                        TalentNodeGen.stat(StatIds.INC_IGNITE_BUFF_MAGNITUDE, 0.1),
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_FIRE_RESISTANCE, 0.1)
                ),
                List.of("33397", "39594")
        ));

        list.add(TalentNodeDef.of(
                "8554", -7739.15f, -371.105f,
                "burning_nature", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FIRE_DAMAGE, 0.25),
                        TalentNodeGen.stat(StatIds.INC_IGNITE_DURATION_ON_ENEMIES, 0.15)
                ),
                List.of("47191")
        ));

        list.add(TalentNodeDef.of(
                "6544", -5520.97f, 1563.1f,
                "burning_strikes", "passives_firedamageint",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_FIRE_DAMAGE, 0.12)
                ),
                List.of("37746", "56061")
        ));

        list.add(TalentNodeDef.of(
                "35324", -1218.9f, -11861.9f,
                "burnout", "passives_firedamagestr",
                List.of(
                        TalentNodeGen.stat(StatIds.IGNITES_YOU_INFLICT_DEAL_DAMAGE_FASTER, 0.15)
                ),
                List.of("34290", "5084")
        ));

        list.add(TalentNodeDef.of(
                "6514", -8441.12f, 4208.28f,
                "cacophony", "passives_warcryeffect",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WITH_WARCRIES, 0.4),
                        TalentNodeGen.stat(StatIds.WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT, 0.25)
                ),
                List.of("47722")
        ));

        list.add(TalentNodeDef.of(
                "20963", -9411.14f, -875.705f,
                "calculated_hunter", "passives_criticalstrikechance",
                List.of(),
                List.of("6100")
        ));

        list.add(TalentNodeDef.of(
                "18081", -6715.78f, -1613.46f,
                "call_upon_the_deep", "passives_elementaldamage",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATES_ELEMENTAL_DAMAGE_RESISTANCES_FOR_EACH_TIME_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS, 0.2)
                ),
                List.of("52993", "8107")
        ));

        list.add(TalentNodeDef.of(
                "32799", 4013.27f, 9394.24f,
                "captivating_companionship", "passives_companionsnotable1",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_FROM_HIT_DAMAGE_IS_TAKEN_FROM_YOUR_DAMAGEABLE_COMPANION_S_HEALTH_BEFORE_YOU, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.2)
                ),
                List.of("10041", "23961")
        ));

        list.add(TalentNodeDef.of(
                "50795", 2619.32f, 3051.46f,
                "careful_aim", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.15),
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_DAMAGE, 0.2)
                ),
                List.of("58013")
        ));

        list.add(TalentNodeDef.of(
                "46197", 7804.08f, -7732.16f,
                "careful_assassin", "passives_criticalstrikechance",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_DAMAGE_BONUS, -0.2),
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE, 0.5)
                ),
                List.of("31692", "61333")
        ));

        list.add(TalentNodeDef.of(
                "17955", 4516.56f, 6218.3f,
                "careful_consideration", "passives_evade",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVE_BEEN_HIT_DAMAGE_RECENTLY, -0.3),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, 1.0)
                ),
                List.of("37220")
        ));

        list.add(TalentNodeDef.of(
                "52348", -3904.0f, 4082.42f,
                "carved_earth", "passives_totemandbrandlife",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_TOTEM_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_AND_CAST_SPEED_IF_YOU_VE_SUMMONED_A_TOTEM_RECENTLY, 0.06)
                ),
                List.of("51206", "60568")
        ));

        list.add(TalentNodeDef.of(
                "44005", -7286.17f, -9611.14f,
                "casting_cascade", "passives_castspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE, -0.15),
                        TalentNodeGen.stat(StatIds.INC_CAST_SPEED_FOR_EACH_DIFFERENT_SPELL_YOU_VE_CAST_IN_THE_LAST_EIGHT_SECONDS, 0.06)
                ),
                List.of("52106")
        ));

        list.add(TalentNodeDef.of(
                "31433", 6988.3f, 4883.0f,
                "catalysis", "passives_elementaldamagewithattacks2",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WITH_ATTACKS, 0.2),
                        TalentNodeGen.stat(StatIds.OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE, 0.05)
                ),
                List.of("21495", "42794")
        ));

        list.add(TalentNodeDef.of(
                "9472", 4216.28f, 4693.55f,
                "catapult", "passives_projectilespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, 0.15),
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, 0.12)
                ),
                List.of("31991", "47677")
        ));

        list.add(TalentNodeDef.of(
                "20831", 2645.45f, 2166.68f,
                "catlike_agility", "passives_aspectofthelynx",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_EVASION, 0.25),
                        TalentNodeGen.stat(StatIds.INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY, 0.4),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.03)
                ),
                List.of("29843", "48585")
        ));

        list.add(TalentNodeDef.of(
                "44527", 2809.45f, 1882.63f,
                "cautious_concoctions", "passives_flasknotableflaskslastlonger",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.15),
                        TalentNodeGen.stat(StatIds.INC_FLASK_CHARGES_GAIN, 0.15)
                ),
                List.of("40630", "55397")
        ));

        list.add(TalentNodeDef.of(
                "32664", 4865.01f, -6372.1f,
                "chakra_of_breathing", "passives_monkenergyshieldchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHEN_NOT_ON_FULL_HEALTH, 0.2),
                        TalentNodeGen.stat(StatIds.INC_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD, 0.2)
                ),
                List.of("32681")
        ));

        list.add(TalentNodeDef.of(
                "63400", 5378.31f, -8121.08f,
                "chakra_of_elements", "passives_monkelementalchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_COLD_DAMAGE_AGAINST_SHOCKED_ENEMIES, 0.08),
                        TalentNodeGen.stat(StatIds.GAIN_OF_PHYSICAL_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_AGAINST_CHILLED_ENEMIES, 0.08)
                ),
                List.of("22851", "419")
        ));

        list.add(TalentNodeDef.of(
                "25362", 10057.9f, -1069.19f,
                "chakra_of_impact", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE, 0.2),
                        TalentNodeGen.stat(StatIds.SKILLS_DEAL_INC_DAMAGE_PER_COMBO_CONSUMED_UP_TO, 0.08)
                ),
                List.of("17101")
        ));

        list.add(TalentNodeDef.of(
                "35031", 11095.7f, -797.845f,
                "chakra_of_life", "passives_monkhealthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MAX_HEALTH, 0.03),
                        TalentNodeGen.stat(StatIds.INC_HEALTH_RECOVERY_RATE, 0.1)
                ),
                List.of("33830", "57683")
        ));

        list.add(TalentNodeDef.of(
                "28963", 10142.8f, -1386.02f,
                "chakra_of_rhythm", "passives_monkstrengthchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.06),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, 0.2)
                ),
                List.of("7888")
        ));

        list.add(TalentNodeDef.of(
                "42347", 11136.3f, -2128.64f,
                "chakra_of_sight", "passives_monkaccuracychakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHT_RADIUS, 0.2),
                        TalentNodeGen.stat(StatIds.CANNOT_BE_BLINDED, 1.0),
                        TalentNodeGen.stat(StatIds.CHANCE_TO_BLIND_ENEMIES_ON_HIT_DAMAGE, 0.12)
                ),
                List.of("40110", "4467")
        ));

        list.add(TalentNodeDef.of(
                "42760", 7455.8f, -3734.88f,
                "chakra_of_stability", "passives_monkstunchakra",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_RECOVERY, 0.3),
                        TalentNodeGen.stat(StatIds.REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED, 0.03),
                        TalentNodeGen.stat(StatIds.TO_STUN_THRESHOLD_PER_DEXTERITY, 1.0)
                ),
                List.of("44453", "61149")
        ));

        list.add(TalentNodeDef.of(
                "29306", 6180.19f, -5421.42f,
                "chakra_of_thought", "passives_monkmanachakra",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, 0.08),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_NOT_ON_LOW_MANA, 0.15)
                ),
                List.of("17668", "61355")
        ));

        list.add(TalentNodeDef.of(
                "5410", -10502.4f, 5903.06f,
                "channelled_heritage", "passives_meleeaoenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS, 0.3)
                ),
                List.of("30780")
        ));
        return list;
    }
}
