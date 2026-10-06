package com.cimdy.coe.data.gen.talent.node.minor;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinorNodes18 {

    private MinorNodes18() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "58539", 9930.18f, 1918.61f,
                "reduced_movement_penalty", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.03)
                ),
                List.of("60899", "65212")
        ));

        list.add(TalentNodeDef.of(
                "60899", 9680.18f, 1918.61f,
                "reduced_movement_penalty", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.03)
                ),
                List.of("32543", "58539")
        ));

        list.add(TalentNodeDef.of(
                "23360", 4477.02f, 8397.87f,
                "reduced_movement_penalty_and_attack_damage_while_moving", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.08),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.02)
                ),
                List.of("36114", "53566")
        ));

        list.add(TalentNodeDef.of(
                "4364", 4106.71f, 8349.12f,
                "reduced_movement_penalty_and_attack_damage_while_moving", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_WHILE_MOVING, 0.08),
                        TalentNodeGen.stat(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING, -0.02)
                ),
                List.of("23736", "51463")
        ));

        list.add(TalentNodeDef.of(
                "30701", -4943.86f, -2483.83f,
                "reduced_projectile_speed", "passives_projectiledmgnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, -0.06)
                ),
                List.of("55308")
        ));

        list.add(TalentNodeDef.of(
                "49130", 7505.97f, 1440.64f,
                "reduced_projectile_speed", "passives_attackspeedbow",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PROJECTILE_SPEED, -0.06)
                ),
                List.of("30132")
        ));

        list.add(TalentNodeDef.of(
                "31037", 555.555f, -7085.72f,
                "remnant_effect", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_YOU_CREATE_HAVE_INC_EFFECT, 0.1)
                ),
                List.of("13909", "34866")
        ));

        list.add(TalentNodeDef.of(
                "34866", 719.555f, -7085.72f,
                "remnant_effect", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_YOU_CREATE_HAVE_INC_EFFECT, 0.1)
                ),
                List.of("31037", "40985")
        ));

        list.add(TalentNodeDef.of(
                "13909", 473.555f, -6943.69f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("31037", "46819", "62679")
        ));

        list.add(TalentNodeDef.of(
                "18744", 4496.61f, -8145.58f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("4059", "60324")
        ));

        list.add(TalentNodeDef.of(
                "37593", 1060.36f, -5120.17f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("46124", "63009", "63739")
        ));

        list.add(TalentNodeDef.of(
                "60324", 4332.61f, -8145.58f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("1506", "18744")
        ));

        list.add(TalentNodeDef.of(
                "62679", 637.555f, -6779.69f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("13909", "40985")
        ));

        list.add(TalentNodeDef.of(
                "63009", 1060.36f, -5284.17f,
                "remnant_pickup_range", "passives_remnant",
                List.of(
                        TalentNodeGen.stat(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, 0.2)
                ),
                List.of("37593", "40783")
        ));

        list.add(TalentNodeDef.of(
                "11722", 2756.14f, -5407.12f,
                "seal_generation_frequency", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.SEALED_SKILLS_HAVE_INC_SEAL_GAIN_FREQUENCY, 0.1)
                ),
                List.of("31517", "62431")
        ));

        list.add(TalentNodeDef.of(
                "31517", 3040.19f, -5571.12f,
                "seal_generation_frequency", "passives_damagespells",
                List.of(
                        TalentNodeGen.stat(StatIds.SEALED_SKILLS_HAVE_INC_SEAL_GAIN_FREQUENCY, 0.1)
                ),
                List.of("11722", "46034")
        ));

        list.add(TalentNodeDef.of(
                "34493", -7234.2f, -6261.18f,
                "sentinels", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("17229", "49593", "54964", "65328")
        ));

        list.add(TalentNodeDef.of(
                "4725", -7695.23f, -6722.21f,
                "sentinels", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("4140", "49593")
        ));

        list.add(TalentNodeDef.of(
                "49593", -7526.94f, -6553.92f,
                "sentinels", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("34493", "4725")
        ));

        list.add(TalentNodeDef.of(
                "54964", -7492.19f, -6295.15f,
                "sentinels", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("23078", "34493")
        ));

        list.add(TalentNodeDef.of(
                "65328", -7268.17f, -6519.17f,
                "sentinels", "passives_miraclemaker",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE, 0.1),
                        TalentNodeGen.stat(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.1)
                ),
                List.of("34493", "48565")
        ));

        list.add(TalentNodeDef.of(
                "53123", -9985.55f, -2055.78f,
                "shaman", "passives_elementaldominion2",
                List.of(
                        TalentNodeGen.stat(StatIds.TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, 0.03)
                ),
                List.of("41768", "54982", "5862", "8421")
        ));

        list.add(TalentNodeDef.of(
                "32186", -8501.1f, -7430.42f,
                "shapeshifted_accuracy_rating", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("26104")
        ));

        list.add(TalentNodeDef.of(
                "55672", -8724.49f, -7407.85f,
                "shapeshifted_accuracy_rating", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("26104")
        ));

        list.add(TalentNodeDef.of(
                "34490", -9524.42f, -6435.02f,
                "shapeshifted_aftershock_chance", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, 0.1)
                ),
                List.of("18972", "63085")
        ));

        list.add(TalentNodeDef.of(
                "43460", -9318.59f, -6434.75f,
                "shapeshifted_armor", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SHAPESHIFTED, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.05)
                ),
                List.of("63085", "63678", "6735")
        ));

        list.add(TalentNodeDef.of(
                "63678", -9318.59f, -6631.85f,
                "shapeshifted_armor", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SHAPESHIFTED, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.05)
                ),
                List.of("43460")
        ));

        list.add(TalentNodeDef.of(
                "6735", -9155.13f, -6351.08f,
                "shapeshifted_armor", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_WHILE_SHAPESHIFTED, 0.1),
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.05)
                ),
                List.of("19563", "41935", "43460")
        ));

        list.add(TalentNodeDef.of(
                "61896", -10520.3f, -3710.82f,
                "shapeshifted_critical_chance", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("20289", "48314", "53354", "8850")
        ));

        list.add(TalentNodeDef.of(
                "8850", -10520.3f, -3933.98f,
                "shapeshifted_critical_chance", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("61896")
        ));

        list.add(TalentNodeDef.of(
                "19563", -9524.42f, -6101.02f,
                "shapeshifted_damage", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("47931", "58496", "6735")
        ));

        list.add(TalentNodeDef.of(
                "25915", -4921.08f, -1483.38f,
                "shapeshifted_damage", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("34412", "8916")
        ));

        list.add(TalentNodeDef.of(
                "33562", -8221.2f, -7830.17f,
                "shapeshifted_damage", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("27216", "27611", "57273")
        ));

        list.add(TalentNodeDef.of(
                "34412", -4844.67f, -1298.91f,
                "shapeshifted_damage", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("20547", "25915")
        ));

        list.add(TalentNodeDef.of(
                "57273", -8094.89f, -7358.79f,
                "shapeshifted_damage", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("33562", "4140")
        ));

        list.add(TalentNodeDef.of(
                "61393", -10448.6f, -3316.27f,
                "shapeshifted_damage", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("29611", "43941", "56368")
        ));

        list.add(TalentNodeDef.of(
                "6222", -5986.97f, -404.704f,
                "shapeshifted_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("14654", "57608", "65189")
        ));

        list.add(TalentNodeDef.of(
                "63085", -9522.16f, -6562.27f,
                "shapeshifted_damage", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("34490", "36100", "37187", "43460")
        ));

        list.add(TalentNodeDef.of(
                "14026", -9717.96f, -6634.67f,
                "shapeshifted_damage_against_immobilised", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED, 0.2)
                ),
                List.of("37187")
        ));

        list.add(TalentNodeDef.of(
                "37187", -9717.96f, -6452.32f,
                "shapeshifted_damage_against_immobilised", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED, 0.2)
                ),
                List.of("14026", "58496", "63085")
        ));

        list.add(TalentNodeDef.of(
                "58496", -9903.54f, -6355.19f,
                "shapeshifted_damage_against_immobilised", "passives_druidshapeshiftbearnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED, 0.2)
                ),
                List.of("19563", "37187", "9328")
        ));

        list.add(TalentNodeDef.of(
                "28489", -8347.5f, -8301.54f,
                "shapeshifted_effect_duration", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.SHAPESHIFT_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION, 0.15)
                ),
                List.of("27216", "27611", "56890")
        ));

        list.add(TalentNodeDef.of(
                "27611", -8471.2f, -7830.17f,
                "shapeshifted_elemental_damage", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("26104", "28489", "30546", "33562")
        ));

        list.add(TalentNodeDef.of(
                "28770", -5911.75f, -1730.57f,
                "shapeshifted_elemental_damage", "passives_druidgenericshapeshiftnode",
                List.of(),
                List.of("35417", "479")
        ));

        list.add(TalentNodeDef.of(
                "29126", -6161.75f, -1297.56f,
                "shapeshifted_elemental_damage", "passives_druidgenericshapeshiftnode",
                List.of(),
                List.of("32353", "479")
        ));

        list.add(TalentNodeDef.of(
                "43282", -8981.59f, -7475.59f,
                "shapeshifted_elemental_damage", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("26104")
        ));

        list.add(TalentNodeDef.of(
                "65189", -5893.26f, -603.739f,
                "shapeshifted_elemental_penetration", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED, 0.06)
                ),
                List.of("17260", "6222")
        ));

        list.add(TalentNodeDef.of(
                "27216", -8004.69f, -7955.17f,
                "shapeshifted_energy_delay", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("28489", "30546", "33562", "60619")
        ));

        list.add(TalentNodeDef.of(
                "31010", -7778.92f, -7623.93f,
                "shapeshifted_energy_delay", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.FASTER_START_OF_ENERGY_SHIELD_RECHARGE_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("60619")
        ));

        list.add(TalentNodeDef.of(
                "41609", -7574.18f, -7716.08f,
                "shapeshifted_energy_recharge", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED, 0.15)
                ),
                List.of("60619")
        ));

        list.add(TalentNodeDef.of(
                "541", -7385.39f, -7903.29f,
                "shapeshifted_energy_recharge", "passives_druidshapeshiftwyvernnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED, 0.15)
                ),
                List.of("60619")
        ));

        list.add(TalentNodeDef.of(
                "33408", -10368.6f, -3933.98f,
                "shapeshifted_life_leech", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("53354")
        ));

        list.add(TalentNodeDef.of(
                "53354", -10368.8f, -3710.82f,
                "shapeshifted_life_leech", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_AMOUNT_OF_HEALTH_LEECH_WHILE_SHAPESHIFTED, 0.1)
                ),
                List.of("20289", "33408", "36250", "61896")
        ));

        list.add(TalentNodeDef.of(
                "36250", -10252.9f, -3509.98f,
                "shapeshifted_life_regeneration", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED, 0.15)
                ),
                List.of("49214", "53354", "55897", "56368")
        ));

        list.add(TalentNodeDef.of(
                "56368", -10251.2f, -3312.73f,
                "shapeshifted_life_regeneration", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_HEALTH_REGENERATION_RATE_WHILE_SHAPESHIFTED, 0.15)
                ),
                List.of("36250", "61393")
        ));

        list.add(TalentNodeDef.of(
                "55897", -10443.8f, -4121.17f,
                "shapeshifted_mana_regeneration", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE_WHILE_SHAPESHIFTED, 0.2)
                ),
                List.of("14432", "36250", "48314")
        ));

        list.add(TalentNodeDef.of(
                "57608", -6167.64f, -530.219f,
                "shapeshifted_physical_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED, 0.12)
                ),
                List.of("61703", "6222")
        ));

        list.add(TalentNodeDef.of(
                "43941", -10648.1f, -3316.27f,
                "shapeshifted_speed", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WHILE_SHAPESHIFTED, 0.03)
                ),
                List.of("48314", "61393")
        ));

        list.add(TalentNodeDef.of(
                "48314", -10650.0f, -3509.98f,
                "shapeshifted_speed", "passives_druidshapeshiftwolfnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WHILE_SHAPESHIFTED, 0.03)
                ),
                List.of("3348", "43941", "55897", "61896")
        ));

        list.add(TalentNodeDef.of(
                "9037", -6094.12f, -804.597f,
                "shapeshifted_speed", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED_WHILE_SHAPESHIFTED, 0.03)
                ),
                List.of("17260", "32353", "61703")
        ));

        list.add(TalentNodeDef.of(
                "41338", -3702.85f, -968.169f,
                "shapeshifting_armor_applies_to_elemental_damage_hits", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, 0.08)
                ),
                List.of("31673", "37629")
        ));

        list.add(TalentNodeDef.of(
                "42452", -5556.9f, -2934.36f,
                "shapeshifting_attack_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.15)
                ),
                List.of("28002", "51743")
        ));

        list.add(TalentNodeDef.of(
                "51743", -5885.4f, -2853.38f,
                "shapeshifting_attack_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.15)
                ),
                List.of("42452", "57617")
        ));

        list.add(TalentNodeDef.of(
                "37509", -6135.4f, -2365.38f,
                "shapeshifting_critical_hit_chance", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.15)
                ),
                List.of("57617", "57921")
        ));

        list.add(TalentNodeDef.of(
                "16506", -5760.4f, -2148.88f,
                "shapeshifting_elemental_ailment_chance", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.2)
                ),
                List.of("35417", "57617")
        ));

        list.add(TalentNodeDef.of(
                "21567", -6303.98f, -3464.21f,
                "shapeshifting_spell_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_HUMAN_FORM_RECENTLY, 0.12)
                ),
                List.of("18822", "59710")
        ));

        list.add(TalentNodeDef.of(
                "59710", -6416.05f, -3399.5f,
                "shapeshifting_spell_damage", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_HUMAN_FORM_RECENTLY, 0.12)
                ),
                List.of("21567", "52618")
        ));

        list.add(TalentNodeDef.of(
                "14996", -5760.4f, -2581.89f,
                "shapeshifting_stun_buildup", "passives_druidgenericshapeshiftnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_STUN_BUILDUP_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY, 0.2)
                ),
                List.of("41620", "57617")
        ));

        list.add(TalentNodeDef.of(
                "10508", -9121.78f, 2130.78f,
                "shield_attack_speed", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_HOLDING_A_SHIELD, 0.03)
                ),
                List.of("21684", "53823")
        ));

        list.add(TalentNodeDef.of(
                "18746", -10160.4f, -4757.69f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("46384", "55888")
        ));

        list.add(TalentNodeDef.of(
                "27581", -9952.33f, -5549.09f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("10681", "50510")
        ));

        list.add(TalentNodeDef.of(
                "32885", -1223.21f, 5444.17f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("45599", "54785", "6689")
        ));

        list.add(TalentNodeDef.of(
                "33402", -10863.0f, -5305.09f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("58125", "6133")
        ));

        list.add(TalentNodeDef.of(
                "440", -10594.1f, -4757.69f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("55888", "6133")
        ));

        list.add(TalentNodeDef.of(
                "48745", -4431.08f, -684.435f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("22558", "34375", "7878")
        ));

        list.add(TalentNodeDef.of(
                "49023", -6353.65f, 4583.4f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("12817", "22975", "49198")
        ));

        list.add(TalentNodeDef.of(
                "49198", -6478.65f, 4284.89f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("22967", "49023")
        ));

        list.add(TalentNodeDef.of(
                "50510", -9886.96f, -5305.09f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("27581", "46384")
        ));

        list.add(TalentNodeDef.of(
                "53901", -4431.08f, -350.435f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("34375", "45612", "7878")
        ));

        list.add(TalentNodeDef.of(
                "54785", -1191.1f, 5015.24f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("32885", "44707")
        ));

        list.add(TalentNodeDef.of(
                "58125", -10797.6f, -5549.09f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("10681", "33402")
        ));

        list.add(TalentNodeDef.of(
                "7878", -4573.11f, -602.435f,
                "shield_block", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BLOCK_CHANCE, 0.05)
                ),
                List.of("48745", "53901")
        ));

        list.add(TalentNodeDef.of(
                "30371", -8858.56f, 1631.39f,
                "shield_damage", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, 0.1)
                ),
                List.of("27687", "52796")
        ));

        list.add(TalentNodeDef.of(
                "44707", -1349.52f, 4972.79f,
                "shield_damage", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, 0.1)
                ),
                List.of("51735", "54785", "7628")
        ));

        list.add(TalentNodeDef.of(
                "51735", -1507.93f, 5015.24f,
                "shield_damage", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, 0.1)
                ),
                List.of("44707", "6689")
        ));

        list.add(TalentNodeDef.of(
                "52796", -9189.0f, 1531.38f,
                "shield_damage", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, 0.1)
                ),
                List.of("30371", "33452")
        ));

        list.add(TalentNodeDef.of(
                "6689", -1475.82f, 5444.17f,
                "shield_damage", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.ATTACK_SKILLS_DEAL_INC_DAMAGE_WHILE_HOLDING_A_SHIELD, 0.1)
                ),
                List.of("32885", "45599", "51735")
        ));

        list.add(TalentNodeDef.of(
                "12817", -6137.14f, 4376.4f,
                "shield_defences", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.25)
                ),
                List.of("22967", "49023")
        ));

        list.add(TalentNodeDef.of(
                "22057", 5368.14f, 8744.43f,
                "shield_defences", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.25)
                ),
                List.of("30143", "36808")
        ));

        list.add(TalentNodeDef.of(
                "31609", -4507.44f, 1825.3f,
                "shield_defences", "passives_blockstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.25)
                ),
                List.of("33978", "36163")
        ));

        list.add(TalentNodeDef.of(
                "34375", -4289.06f, -602.435f,
                "shield_defences", "passives_shieldblock",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.25)
                ),
                List.of("48745", "53901")
        ));

        list.add(TalentNodeDef.of(
                "45693", 4386.25f, 1960.69f,
                "shield_defences", "passives_bucklernode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_FROM_EQUIPPED_SHIELD, 0.25)
                ),
                List.of("15030", "64851")
        ));

        list.add(TalentNodeDef.of(
                "12925", 2809.49f, -2539.0f,
                "shock_chance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("36479", "61196")
        ));

        list.add(TalentNodeDef.of(
                "20387", 2504.3f, -8625.66f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("53266", "57810")
        ));

        list.add(TalentNodeDef.of(
                "22271", 1029.73f, -11468.6f,
                "shock_chance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("15083", "55554")
        ));

        list.add(TalentNodeDef.of(
                "32123", 6268.63f, -1688.97f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("27875", "54678")
        ));

        list.add(TalentNodeDef.of(
                "36778", 2266.17f, -2225.32f,
                "shock_chance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("36479", "49220")
        ));

        list.add(TalentNodeDef.of(
                "4", 3249.41f, 4552.82f,
                "shock_chance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("11578", "48833")
        ));

        list.add(TalentNodeDef.of(
                "54176", 5478.57f, -1107.81f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("26598", "55463")
        ));

        list.add(TalentNodeDef.of(
                "54678", 6720.04f, -1779.61f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("32123", "41877")
        ));

        list.add(TalentNodeDef.of(
                "55463", 5812.38f, -1232.72f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("27875", "54176")
        ));

        list.add(TalentNodeDef.of(
                "55554", 1139.73f, -11659.1f,
                "shock_chance", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("22271", "8821")
        ));

        list.add(TalentNodeDef.of(
                "57810", 2257.86f, -8717.37f,
                "shock_chance", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.15)
                ),
                List.of("20387", "40073")
        ));

        list.add(TalentNodeDef.of(
                "38944", 8521.5f, -6160.65f,
                "shock_chance_and_lightning_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.08)
                ),
                List.of("43338", "59538")
        ));

        list.add(TalentNodeDef.of(
                "43338", 8439.5f, -6018.62f,
                "shock_chance_and_lightning_damage", "passives_stun2h",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_LIGHTNING_DAMAGE, 0.08),
                        TalentNodeGen.stat(StatIds.INC_CHANCE_TO_SHOCK, 0.08)
                ),
                List.of("38944", "56767")
        ));

        list.add(TalentNodeDef.of(
                "14724", 7487.17f, 5195.85f,
                "shock_duration", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.2)
                ),
                List.of("37813", "62185")
        ));

        list.add(TalentNodeDef.of(
                "37813", 7678.02f, 5026.98f,
                "shock_duration", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SHOCK_DURATION, 0.2)
                ),
                List.of("14724", "50701")
        ));

        list.add(TalentNodeDef.of(
                "1953", 5316.81f, -1530.6f,
                "shock_effect", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("23905", "40480")
        ));

        list.add(TalentNodeDef.of(
                "23905", 5664.01f, -1616.29f,
                "shock_effect", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("1953", "27875")
        ));

        list.add(TalentNodeDef.of(
                "44932", 8107.74f, 5039.42f,
                "shock_effect", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("50701", "54984")
        ));

        list.add(TalentNodeDef.of(
                "50277", 7815.76f, 5381.08f,
                "shock_effect", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("32301", "50701")
        ));

        list.add(TalentNodeDef.of(
                "50701", 7874.86f, 5154.88f,
                "shock_effect", "passives_lightningint",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.15)
                ),
                List.of("37813", "44932", "50277")
        ));

        list.add(TalentNodeDef.of(
                "35503", 2744.59f, -3760.9f,
                "shock_effect_and_mana_regeneration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.1)
                ),
                List.of("23764", "4850")
        ));

        list.add(TalentNodeDef.of(
                "4850", 2577.55f, -4030.32f,
                "shock_effect_and_mana_regeneration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.1)
                ),
                List.of("35503", "7405")
        ));

        list.add(TalentNodeDef.of(
                "7405", 2886.7f, -4100.44f,
                "shock_effect_and_mana_regeneration", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_MANA_REGENERATION_RATE, 0.06),
                        TalentNodeGen.stat(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT, 0.1)
                ),
                List.of("31950", "4850")
        ));

        list.add(TalentNodeDef.of(
                "22784", -1282.33f, -9722.75f,
                "shock_effect_taken", "passives_lightningdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_BUFF_EFFECT_OF_SHOCK_ON_YOU, -0.1)
                ),
                List.of("13515", "17057")
        ));
        list.add(TalentNodeDef.of(
                "44683", 1270.43f, -728.835f,
                "six", "passives_tempint",
                List.of(),
                List.of("10364", "52980")
        ));

        list.add(TalentNodeDef.of(
                "14952", -5943.51f, -9384.97f,
                "skill_effect_duration", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("21985", "8982")
        ));

        list.add(TalentNodeDef.of(
                "21985", -5943.51f, -9634.97f,
                "skill_effect_duration", "passives_avoidchilling",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_EFFECT_DURATION, 0.1)
                ),
                List.of("14952", "48925")
        ));

        list.add(TalentNodeDef.of(
                "10364", 1398.73f, -958.945f,
                "skill_speed", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.04)
                ),
                List.of("42857", "44683", "55342")
        ));

        list.add(TalentNodeDef.of(
                "14725", 2589.29f, -2001.95f,
                "skill_speed", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("34233", "49220")
        ));

        list.add(TalentNodeDef.of(
                "26798", -4335.1f, 619.905f,
                "skill_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("53405", "9638")
        ));

        list.add(TalentNodeDef.of(
                "32545", 2835.29f, -2143.98f,
                "skill_speed", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("34233", "61196")
        ));

        list.add(TalentNodeDef.of(
                "3431", 8388.6f, 6317.46f,
                "skill_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("43082", "5305")
        ));

        list.add(TalentNodeDef.of(
                "38923", -8593.97f, -2240.63f,
                "skill_speed", "passives_inquistitor_increasedelementaldamageattackcastespeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("511", "61429")
        ));

        list.add(TalentNodeDef.of(
                "42857", 1604.85f, -1077.94f,
                "skill_speed", "passives_harrier",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.04)
                ),
                List.of("10364", "20024", "7576")
        ));

        list.add(TalentNodeDef.of(
                "5305", 8459.62f, 6194.46f,
                "skill_speed", "passives_increasedrunspeeddex",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("24287", "3431")
        ));

        list.add(TalentNodeDef.of(
                "9638", -4339.42f, 843.265f,
                "skill_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SKILL_SPEED, 0.03)
                ),
                List.of("26798", "39083")
        ));

        list.add(TalentNodeDef.of(
                "26772", 5818.98f, 7008.53f,
                "slow_effect", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.05)
                ),
                List.of("24240", "45774")
        ));

        list.add(TalentNodeDef.of(
                "45774", 5901.44f, 6738.89f,
                "slow_effect", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.05)
                ),
                List.of("26772", "54975")
        ));

        list.add(TalentNodeDef.of(
                "54975", 5615.48f, 6656.06f,
                "slow_effect", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.05)
                ),
                List.of("45774", "7526")
        ));

        list.add(TalentNodeDef.of(
                "44563", 3857.5f, -5882.32f,
                "slow_effect_and_hinder_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_HINDER_DURATION, 0.2)
                ),
                List.of("41029", "59053")
        ));

        list.add(TalentNodeDef.of(
                "59053", 4146.76f, -5715.32f,
                "slow_effect_and_hinder_duration", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE, 0.04),
                        TalentNodeGen.stat(StatIds.INC_HINDER_DURATION, 0.2)
                ),
                List.of("44563", "54805")
        ));

        list.add(TalentNodeDef.of(
                "12120", 6031.75f, 3674.83f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("35901", "51606")
        ));

        list.add(TalentNodeDef.of(
                "22538", -3031.32f, -11149.0f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("55412", "64659")
        ));

        list.add(TalentNodeDef.of(
                "29041", -6908.33f, 3155.11f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("31388", "52298")
        ));

        list.add(TalentNodeDef.of(
                "31388", -6585.72f, 2907.56f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("29041", "51394")
        ));

        list.add(TalentNodeDef.of(
                "33518", -2224.68f, 8932.47f,
                "slow_effect_on_you", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("39207", "63579")
        ));

        list.add(TalentNodeDef.of(
                "34968", 9930.18f, 2351.62f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("64637", "65212")
        ));

        list.add(TalentNodeDef.of(
                "39207", -2184.42f, 8668.39f,
                "slow_effect_on_you", "passives_legstrength",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("32564", "33518")
        ));

        list.add(TalentNodeDef.of(
                "45301", -5245.34f, -791.755f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("31724", "51183")
        ));

        list.add(TalentNodeDef.of(
                "55412", -2933.5f, -10912.8f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("17796", "22538", "47168")
        ));

        list.add(TalentNodeDef.of(
                "59367", -4637.92f, 7834.96f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("10774", "26568")
        ));

        list.add(TalentNodeDef.of(
                "64637", 9680.18f, 2351.62f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("32543", "34968")
        ));

        list.add(TalentNodeDef.of(
                "65207", 6198.75f, 3964.09f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("51606", "63566")
        ));

        list.add(TalentNodeDef.of(
                "65212", 10055.2f, 2135.11f,
                "slow_effect_on_you", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.08)
                ),
                List.of("27705", "34968", "58539")
        ));

        list.add(TalentNodeDef.of(
                "26568", -4801.92f, 7998.96f,
                "slow_effect_on_you_and_attack_speed", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.04)
                ),
                List.of("37258", "51732", "59367")
        ));

        list.add(TalentNodeDef.of(
                "3355", -2697.32f, -11483.0f,
                "slow_effect_on_you_and_debuff_expiry_rate", "passives_reducedskilleffectdurationnode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU, -0.04),
                        TalentNodeGen.stat(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, 0.03)
                ),
                List.of("25211", "64659")
        ));

        list.add(TalentNodeDef.of(
                "20105", 9764.39f, 6540.17f,
                "spear_accuracy", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ACCURACY_WITH_SPEARS, 0.1)
                ),
                List.of("13895", "61112")
        ));

        list.add(TalentNodeDef.of(
                "19767", 9220.97f, 6758.46f,
                "spear_attack_speed", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SPEARS, 0.03)
                ),
                List.of("13895", "35223")
        ));

        list.add(TalentNodeDef.of(
                "35223", 9136.4f, 6905.15f,
                "spear_attack_speed", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SPEARS, 0.03)
                ),
                List.of("19767", "55680", "9227")
        ));

        list.add(TalentNodeDef.of(
                "55680", 9305.71f, 6905.24f,
                "spear_attack_speed", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WITH_SPEARS, 0.03)
                ),
                List.of("35223", "61112")
        ));

        list.add(TalentNodeDef.of(
                "18910", 8278.17f, 7400.65f,
                "spear_critical_chance", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_SPEARS, 0.1)
                ),
                List.of("10265", "38369")
        ));

        list.add(TalentNodeDef.of(
                "38369", 8424.52f, 7316.15f,
                "spear_critical_chance", "passives_spearsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_CRITICAL_HIT_CHANCE_WITH_SPEARS, 0.1)
                ),
                List.of("18910", "21225")
        ));
        return list;
    }
}
