package com.cimdy.coe.data.gen.talent.node.minor;
import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.gen.talent.TalentNodeGen;

import java.util.Map;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.ArrayList;
import java.util.List;

public final class MinorNodes04 {

    private MinorNodes04() {
    }

    public static List<TalentNodeDef> build() {
        List<TalentNodeDef> list = new ArrayList<>();

        list.add(TalentNodeDef.of(
                "32399", 6506.75f, -6478.52f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_COST_EFFICIENCY, 0.05)
                ),
                List.of("40166", "46857")
        ));

        list.add(TalentNodeDef.of(
                "35660", 1970.43f, 1023.43f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("18548", "35234")
        ));

        list.add(TalentNodeDef.of(
                "35863", -4943.95f, 7752.96f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("10774", "51732")
        ));

        list.add(TalentNodeDef.of(
                "37767", 2514.33f, 11893.9f,
                "attack_speed", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_A_RARE_OR_UNIQUE_ENEMY_IS_IN_YOUR_PRESENCE, 0.04)
                ),
                List.of("63731", "6596", "9020")
        ));

        list.add(TalentNodeDef.of(
                "38501", -1700.58f, 3107.11f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("24880", "47796")
        ));

        list.add(TalentNodeDef.of(
                "41171", 2947.34f, 11643.9f,
                "attack_speed", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_A_RARE_OR_UNIQUE_ENEMY_IS_IN_YOUR_PRESENCE, 0.04)
                ),
                List.of("36341", "52354", "6596")
        ));

        list.add(TalentNodeDef.of(
                "43877", 5033.31f, -137.961f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("32701", "47895", "51522")
        ));

        list.add(TalentNodeDef.of(
                "44628", 6028.75f, -6216.8f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("20820", "4328")
        ));

        list.add(TalentNodeDef.of(
                "46961", 5371.78f, 9762.63f,
                "attack_speed", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("28142", "5191")
        ));

        list.add(TalentNodeDef.of(
                "47796", -1533.68f, 2812.54f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("27108", "38501", "62640")
        ));

        list.add(TalentNodeDef.of(
                "48714", -4350.96f, -2030.06f,
                "attack_speed", "passives_increasedattackdamagenode",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("36478", "65193")
        ));

        list.add(TalentNodeDef.of(
                "49231", -2553.76f, 5254.65f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("43183", "48670", "5049")
        ));

        list.add(TalentNodeDef.of(
                "49461", 4554.78f, 164.533f,
                "attack_speed", "passives_damage",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("18818", "50912")
        ));

        list.add(TalentNodeDef.of(
                "50635", 5846.6f, -2347.64f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("25971", "42750", "9050")
        ));

        list.add(TalentNodeDef.of(
                "51522", 4992.31f, -351.003f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("43877", "56493")
        ));

        list.add(TalentNodeDef.of(
                "51732", -4943.95f, 7916.96f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("26568", "35863")
        ));

        list.add(TalentNodeDef.of(
                "52442", -140.083f, 2336.36f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("34061", "97")
        ));

        list.add(TalentNodeDef.of(
                "57196", 757.865f, 10524.7f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("23702", "3660")
        ));

        list.add(TalentNodeDef.of(
                "62640", -1366.77f, 3107.11f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("24880", "47796")
        ));

        list.add(TalentNodeDef.of(
                "63445", 1258.86f, 10235.5f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("23702", "43562")
        ));

        list.add(TalentNodeDef.of(
                "6596", 2974.83f, 12191.5f,
                "attack_speed", "passives_executioner",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_A_RARE_OR_UNIQUE_ENEMY_IS_IN_YOUR_PRESENCE, 0.04)
                ),
                List.of("37767", "41171")
        ));

        list.add(TalentNodeDef.of(
                "9050", 6172.4f, -2724.57f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("24958", "37691", "42750", "50635")
        ));

        list.add(TalentNodeDef.of(
                "97", 1.945f, 2254.36f,
                "attack_speed", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03)
                ),
                List.of("14254", "52442", "59779", "59915")
        ));

        list.add(TalentNodeDef.of(
                "11153", -2553.76f, 5754.65f,
                "attack_speed_and_accuracy", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.05)
                ),
                List.of("42177", "43183", "5049")
        ));

        list.add(TalentNodeDef.of(
                "43183", -2303.76f, 5504.65f,
                "attack_speed_and_accuracy", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.INC_ACCURACY, 0.05)
                ),
                List.of("11153", "49231")
        ));

        list.add(TalentNodeDef.of(
                "28142", 5094.15f, 9900.68f,
                "attack_speed_and_companion_attack_speed", "passives_azmerivividwolf",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.COMPANIONS_HAVE_INC_ATTACK_SPEED, 0.06)
                ),
                List.of("17702", "44684", "46961")
        ));

        list.add(TalentNodeDef.of(
                "11504", 7336.8f, 1153.67f,
                "attack_speed_and_dexterity", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 5.0)
                ),
                List.of("30839", "35696")
        ));

        list.add(TalentNodeDef.of(
                "30839", 7172.6f, 1282.56f,
                "attack_speed_and_dexterity", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 5.0)
                ),
                List.of("11504", "35671")
        ));

        list.add(TalentNodeDef.of(
                "35671", 7023.84f, 1069.81f,
                "attack_speed_and_dexterity", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 5.0)
                ),
                List.of("30839", "31172")
        ));

        list.add(TalentNodeDef.of(
                "5049", -2803.76f, 5504.65f,
                "attack_speed_and_dexterity", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02),
                        TalentNodeGen.stat(StatIds.TO_DEXTERITY, 5.0)
                ),
                List.of("11153", "42177", "49231")
        ));

        list.add(TalentNodeDef.of(
                "45100", 6177.69f, 5088.04f,
                "attack_speed_and_flask_duration", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02)
                ),
                List.of("56928", "7163")
        ));

        list.add(TalentNodeDef.of(
                "56928", 5984.05f, 4838.07f,
                "attack_speed_and_flask_duration", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02)
                ),
                List.of("45100", "62350")
        ));

        list.add(TalentNodeDef.of(
                "62350", 6220.23f, 4765.9f,
                "attack_speed_and_flask_duration", "passives_attackspeed",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_FLASK_EFFECT_DURATION, 0.05),
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.02)
                ),
                List.of("56928", "63566")
        ));

        list.add(TalentNodeDef.of(
                "30258", -7885.44f, -2563.78f,
                "attack_speed_and_minion_attack_speed", "passives_minionstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_SPEED, 0.03)
                ),
                List.of("20115", "40105")
        ));

        list.add(TalentNodeDef.of(
                "40105", -8083.4f, -2537.72f,
                "attack_speed_and_minion_attack_speed", "passives_minionstr",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED, 0.03),
                        TalentNodeGen.stat(StatIds.MINIONS_HAVE_INC_ATTACK_SPEED, 0.03)
                ),
                List.of("20558", "30258")
        ));

        list.add(TalentNodeDef.of(
                "30197", 7382.34f, 2569.84f,
                "attack_speed_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.04)
                ),
                List.of("43102", "52415")
        ));

        list.add(TalentNodeDef.of(
                "52415", 7218.34f, 2569.84f,
                "attack_speed_with_companion_in_presence", "passives_companionsnode1",
                List.of(
                        TalentNodeGen.stat(StatIds.INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE, 0.04)
                ),
                List.of("30197", "32655")
        ));
        return list;
    }
}
