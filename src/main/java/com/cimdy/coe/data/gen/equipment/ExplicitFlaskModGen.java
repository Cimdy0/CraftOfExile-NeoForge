package com.cimdy.coe.data.gen.equipment;

import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.mod.value.ModValue;

import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.codec.item.ItemInstance;
import net.minecraft.data.worldgen.BootstrapContext;

/**
 * 药剂 / 护符显式词缀数据（离线烘焙）：由 {@code _scratch/gen_flask_mods.py} 从
 * {@code design/refactor/affix-data/crawled/{life_flask,mana_flask,charm}.json} 烘出，
 * 每条词缀 = 一条字面量建造者调用，无运行时解析。
 *
 * <h2>为什么要按「文本模板」拆家族</h2>
 * 语料的 {@code ModFamilyList} 一个家族可含多个语义不同的文本，例如 {@code FlaskRechargeRate}
 * 同时含「#% increased Charges gained」与「#% Chance to gain a Charge when you kill an enemy」——
 *
 * <h2>门控（spawn_no 实证）</h2>
 * <ul>
 *   <li>{@code spawn == {default}} → 三类通吃（24 条，三个语料文件完全一致）；</li>
 *   <li>含 {@code life_flask} / {@code mana_flask} → 对应药剂；两者共有词缀注册在父类层
 *       {@code flask/mod/*}（type 树 {@code flask/type.json} 声明二者为子类别，故父类池对两者同时命中）；</li>
 *   <li>含 {@code utility_flask} → 护符（POE2 护符与功能药剂共享词缀池）。</li>
 * </ul>
 *
 */
public final class ExplicitFlaskModGen {

    private ExplicitFlaskModGen() {
    }

    public static void register(BootstrapContext<ItemInstance> bootstrap) {

        // ---- FLASK_MOD 前缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_PREFIX, "FlaskRecoveryAmount",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_RECOVERY_AMOUNT))
                .withTier("opaque", ModValue.of(41), 1, 1000)
                .withTier("compact", ModValue.of(46, 50), 11, 1000)
                .withTier("full_bodied", ModValue.of(51, 55), 23, 1000)
                .withTier("abundant", ModValue.of(56, 60), 34, 1000)
                .withTier("substantial", ModValue.of(61, 65), 46, 1000)
                .withTier("concentrated", ModValue.of(66, 70), 56, 1000)
                .withTier("potent", ModValue.of(71, 75), 67, 1000)
                .withTier("saturated", ModValue.of(76, 80), 83, 1000)
                .build());

        // ---- CHARM_MOD 前缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_PREFIX, "FlaskDuration",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_DURATION))
                .withTier("investigator_s", ModValue.of(16), 1, 1000)
                .withTier("analyst_s", ModValue.of(21, 25), 20, 1000)
                .withTier("examiner_s", ModValue.of(26, 30), 42, 1000)
                .withTier("clinician_s", ModValue.of(31, 35), 61, 1000)
                .withTier("experimenter_s", ModValue.of(36, 40), 78, 1000)
                .build());

        // ---- FLASK_MOD 前缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_PREFIX, "FlaskRecoveryRate",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_RECOVERY_SPEED))
                .withTier("dense", ModValue.of(41), 1, 1000)
                .withTier("undiluted", ModValue.of(46, 50), 15, 1000)
                .withTier("hearty", ModValue.of(51, 55), 31, 1000)
                .withTier("viscous", ModValue.of(56, 60), 46, 1000)
                .withTier("condensed", ModValue.of(61, 65), 61, 1000)
                .withTier("catalysed", ModValue.of(66, 70), 81, 1000)
                .build());

        // ---- CHARM_MOD 前缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_PREFIX, "AlsoGrantsGuard",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_GRANT_GUARD))
                .withTier("sunny", ModValue.of(44), 10, 1000)
                .withTier("dawnlit", ModValue.of(85, 128), 21, 1000)
                .withTier("bright", ModValue.of(148, 200), 36, 1000)
                .withTier("vibrant", ModValue.of(205, 260), 48, 1000)
                .withTier("lustrous", ModValue.of(265, 350), 60, 1000)
                .withTier("sunburst", ModValue.of(355, 500), 75, 1000)
                .build());
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_PREFIX, "RecoverLifeWhenUsed",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_RECOVER_LIFE))
                .withTier("herbal", ModValue.of(8), 1, 1000)
                .withTier("floral", ModValue.of(35, 52), 14, 1000)
                .withTier("blooming", ModValue.of(63, 92), 26, 1000)
                .withTier("sprouting", ModValue.of(96, 130), 36, 1000)
                .withTier("petaled", ModValue.of(134, 180), 47, 1000)
                .withTier("botanic", ModValue.of(185, 230), 58, 1000)
                .withTier("natural", ModValue.of(235, 280), 67, 1000)
                .withTier("evergreen", ModValue.of(285, 350), 76, 1000)
                .build());
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_PREFIX, "RecoverManaWhenUsed",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_RECOVER_MANA))
                .withTier("drizzling", ModValue.of(16), 1, 1000)
                .withTier("soaked", ModValue.of(33, 50), 14, 1000)
                .withTier("mistbound", ModValue.of(55, 75), 26, 1000)
                .withTier("tidebound", ModValue.of(80, 110), 36, 1000)
                .withTier("aqueous", ModValue.of(115, 145), 47, 1000)
                .withTier("flooded", ModValue.of(150, 180), 58, 1000)
                .withTier("oceanic", ModValue.of(185, 225), 67, 1000)
                .withTier("raindancer_s", ModValue.of(230, 300), 76, 1000)
                .build());

        // ---- FLASK_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_SUFFIX, "FlaskChanceGainChargeOnKill",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_CHANCE_GAIN_CHARGE_ON_KILL))
                .withTier("of_the_medic", ModValue.of(21), 8, 1000)
                .withTier("of_the_doctor", ModValue.of(26, 30), 26, 1000)
                .withTier("of_the_surgeon", ModValue.of(31, 35), 45, 1000)
                .build());

        // ---- CHARM_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_SUFFIX, "FlaskChanceGainChargeOnKill",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_CHANCE_GAIN_CHARGE_ON_KILL))
                .withTier("of_the_medic", ModValue.of(21), 8, 1000)
                .withTier("of_the_doctor", ModValue.of(26, 30), 26, 1000)
                .withTier("of_the_surgeon", ModValue.of(31, 35), 45, 1000)
                .build());

        // ---- FLASK_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_SUFFIX, "FlaskNumCharges",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_NUM_CHARGES))
                .withTier("of_the_wide", ModValue.of(23), 1, 1000)
                .withTier("of_the_copious", ModValue.of(31, 38), 12, 1000)
                .withTier("of_the_plentiful", ModValue.of(39, 46), 32, 1000)
                .withTier("of_the_bountiful", ModValue.of(47, 54), 47, 1000)
                .withTier("of_the_abundant", ModValue.of(55, 62), 62, 1000)
                .withTier("of_the_ample", ModValue.of(63, 70), 81, 1000)
                .build());

        // ---- CHARM_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_SUFFIX, "FlaskNumCharges",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_NUM_CHARGES))
                .withTier("of_the_wide", ModValue.of(23), 1, 1000)
                .withTier("of_the_copious", ModValue.of(31, 38), 12, 1000)
                .withTier("of_the_plentiful", ModValue.of(39, 46), 32, 1000)
                .withTier("of_the_bountiful", ModValue.of(47, 54), 47, 1000)
                .withTier("of_the_abundant", ModValue.of(55, 62), 62, 1000)
                .withTier("of_the_ample", ModValue.of(63, 70), 81, 1000)
                .build());

        // ---- FLASK_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_SUFFIX, "FlaskChargesGained",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_CHARGES_GAINED))
                .withTier("of_the_constant", ModValue.of(23), 1, 1000)
                .withTier("of_the_continuous", ModValue.of(31, 38), 13, 1000)
                .withTier("of_the_endless", ModValue.of(39, 46), 33, 1000)
                .withTier("of_the_bottomless", ModValue.of(47, 54), 48, 1000)
                .withTier("of_the_perpetual", ModValue.of(55, 62), 63, 1000)
                .withTier("of_the_eternal", ModValue.of(63, 70), 82, 1000)
                .build());

        // ---- CHARM_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_SUFFIX, "FlaskChargesGained",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_CHARGES_GAINED))
                .withTier("of_the_constant", ModValue.of(23), 1, 1000)
                .withTier("of_the_continuous", ModValue.of(31, 38), 13, 1000)
                .withTier("of_the_endless", ModValue.of(39, 46), 33, 1000)
                .withTier("of_the_bottomless", ModValue.of(47, 54), 48, 1000)
                .withTier("of_the_perpetual", ModValue.of(55, 62), 63, 1000)
                .withTier("of_the_eternal", ModValue.of(63, 70), 82, 1000)
                .build());

        // ---- FLASK_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_SUFFIX, "FlaskChargesUsed",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_CHARGES_USED))
                .withTier("of_the_apprentice", ModValue.of(15), 1, 1000)
                .withTier("of_the_practitioner", ModValue.of(18, 20), 14, 1000)
                .withTier("of_the_mixologist", ModValue.of(21, 23), 34, 1000)
                .withTier("of_the_distiller", ModValue.of(24, 26), 49, 1000)
                .withTier("of_the_brewer", ModValue.of(27, 29), 64, 1000)
                .withTier("of_the_chemist", ModValue.of(30, 32), 83, 1000)
                .build());

        // ---- CHARM_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_SUFFIX, "FlaskChargesUsed",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_CHARGES_USED))
                .withTier("of_the_apprentice", ModValue.of(15), 1, 1000)
                .withTier("of_the_practitioner", ModValue.of(18, 20), 14, 1000)
                .withTier("of_the_mixologist", ModValue.of(21, 23), 34, 1000)
                .withTier("of_the_distiller", ModValue.of(24, 26), 49, 1000)
                .withTier("of_the_brewer", ModValue.of(27, 29), 64, 1000)
                .withTier("of_the_chemist", ModValue.of(30, 32), 83, 1000)
                .build());

        // ---- FLASK_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.FLASK_MOD_SUFFIX, "FlaskGainChargePerSecond",
            ModPreset.builder(AdditionType.global(StatIds.FLASK_GAIN_CHARGE))
                .withTier("of_the_foliage", ModValue.of(0.15, 0.15), 8, 1000)
                .withTier("of_the_verdant", ModValue.of(0.2, 0.2), 26, 1000)
                .withTier("of_the_sylvan", ModValue.of(0.25, 0.25), 45, 1000)
                .build());

        // ---- CHARM_MOD 后缀 ----
        EquipmentGen.registerMod(bootstrap, EquipmentGen.CHARM_MOD_SUFFIX, "FlaskGainChargePerSecond",
            ModPreset.builder(AdditionType.global(StatIds.CHARM_GAIN_CHARGE))
                .withTier("of_the_foliage", ModValue.of(0.15, 0.15), 8, 1000)
                .withTier("of_the_verdant", ModValue.of(0.2, 0.2), 26, 1000)
                .withTier("of_the_sylvan", ModValue.of(0.25, 0.25), 45, 1000)
                .build());
    }
}
