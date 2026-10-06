package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import static com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofLevelTable;

/**
 * 绿色（敏捷）主动技能 — 武器/攻击投射物、范围与位移为主。
 */
public class SkillGreenGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
    registerPart1(bootstrap);
    registerPart2(bootstrap);
    registerPart3(bootstrap);
    registerPart4(bootstrap);
}

    private static void registerPart1(BootstrapContext<SkillEntry> bootstrap) {

        // ---- 形态攻击（POE2 Druid 实证）：使用即变身（tags 含 shapeshifted + 形态名）----
        // 狼形态基础攻击 Rake：物理挥砍（POE2 wolf form 定位：高速 + 可转冰；本实现物理基础）
        reg(bootstrap, "green/wolf_rake",
                SkillPresets.read("generic_shapeshift_physical")
                        .require(GemGen.dexReq()).cost(SkillFactory.mana(0))
                        .withTags(GateTag.WOLF));

        // ---- 基础 ----
        reg(bootstrap, "green/lightning_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(8), "lightning", 8, 1, 2,
                        SkillFactory.electrocute(1.5)));
        reg(bootstrap, "green/toxic_rain",
                SkillFactory.projectileSpell(GemGen.dexReq(), SkillFactory.mana(10), "physical", 9, 1, 2));
        reg(bootstrap, "green/cyclone",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(10), "physical", 7, 1, 1.5, 3.0,
                        SkillFactory.bleed(0.15)));

        // ---- 扩充（可完整实现：投射 / 范围 / 位移） ----
        // 闪电打击：跃向目标以自身为中心爆发闪电，造成位移与范围闪电伤害
        reg(bootstrap, "green/lightning_strike",
                SkillFactory.movementSkill(GemGen.dexReq(), SkillFactory.mana(10),
                        MoveAction.MoveType.DASH, 1.0, "lightning", 12, 1, 2.5, 3.0,
                        0.6, SkillFactory.stun(0.20)));
        // 箭雨：在目标区域降下箭雨，造成范围物理伤害

        // 电花：射出多枚闪电弹片造成闪电伤害
        reg(bootstrap, "green/galvanic_shards",
                SkillFactory.projectileSpell(GemGen.dexReq(), SkillFactory.mana(7), "lightning", 7, 1, 1.5));
        // 闪电矛：掷出一杆闪电长矛造成闪电伤害
        reg(bootstrap, "green/lightning_spear",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(9), "lightning", 11, 1, 2.5));
        // 穿刺：射出穿刺箭造成物理伤害
        reg(bootstrap, "green/puncture",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(8), "physical", 10, 1, 2,
                        SkillFactory.bleed(0.15)));
        // 霜火箭：射出冰火混合箭造成冰霜伤害

        // 剧毒药剂：掷出剧毒药剂造成混沌伤害

        // 旋风斩：持续旋转挥砍周围敌人
        reg(bootstrap, "green/whirling_slash",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(9), "physical", 9, 1, 2, 2.5, 0.6));
        // 割裂：挥砍造成范围物理伤害，血 stance 下有几率施加流血（POE2 "chance to inflict bleeding"）
        reg(bootstrap, "green/lacerate",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(9), "physical", 10, 1, 2, 2.5, SkillFactory.bleed(0.15)));
        // 猎血寻腥（POE2 实证）：Lunge at a target and skewers them. Hitting an enemy that has suffered
        // Blood Loss will cause a blood explosion and also **Consume Bleeding**.
        //   → 命中后消费目标流血（Consume Bleeding），血爆令敌人失去等同「被消耗失血量 25%」的生命
        //     （Enemies lose Life equal to 25% of Consumed Blood Loss；非命中伤害，不可暴击）。
        // 【已接线】血爆已接入 Consume Bleeding 通道：命中时由 consumeAilmentOnHit(StatusDefs.BLEED)
        //     消费目标流血，payoff 按被消耗失血量 25% 对目标结算生命损失（consume_ailment(bleed) 闭合）。
        reg(bootstrap, "green/blood_hunt",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(8), "physical", 9, 1, 2.5, 2.5,
                        // 命中消费目标流血（Consume Bleeding）→ 血爆：敌人失去等同被消耗失血量 25% 的生命
                        // （非命中伤害、不可暴击；变量 $consumed_bleed 由 ConsumeAilmentAction 注入）。
                        SkillFactory.consumeAilmentOnHit(StatusDefs.BLEED,
                                new DamageAction("physical",
                                        ExpressionCodec.of(MathParser.parse("$consumed_bleed * 0.25")),
                                        Optional.empty(),
                                        ExpressionCodec.of(0.0)))));
        // <<<AUTO_START>>>
        reg(bootstrap, "green/alchemist_s_boon",
                SkillFactory.selfBuff(GemGen.dexReq(), SkillFactory.mana(11), "flask_charges_gained", 1, 1.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        reg(bootstrap, "green/bloodhound_s_mark",
                SkillPresets.read("generic_mark")
                        .require(GemGen.dexReq()).cost(SkillFactory.mana(9))
                        .vars("mark_damage", ExpressionCodec.ofSkillLevel(15.4, 1, 3.8)));
    }

    private static void registerPart2(BootstrapContext<SkillEntry> bootstrap) {

        reg(bootstrap, "green/cull_the_weak",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 13.0, 14.0, 16.0, 17.0, 19.0, 21.0,
                                23.0, 25.0, 28.0, 30.0, 33.0, 36.0, 38.0, 42.0, 45.0, 49.0, 52.0, 56.0,
                                60.0, 65.0, 70.0, 75.0, 80.0, 86.0, 92.0, 98.0, 105.0, 112.0, 119.0, 127.0,
                                136.0, 144.0, 154.0, 164.0
                        ))), "physical", 11.8, 1, 3, 2.5));
        reg(bootstrap, "green/disengage",
                SkillFactory.movementSkill(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 14.0, 15.0, 17.0, 18.0,
                                20.0, 22.0, 24.0, 26.0, 28.0, 31.0, 33.0, 36.0, 39.0, 42.0, 45.0, 49.0,
                                52.0, 56.0, 60.0, 65.0, 69.0, 74.0, 79.0, 85.0, 91.0, 97.0, 103.0, 110.0,
                                117.0, 125.0, 133.0, 142.0
                        ))), MoveAction.MoveType.DASH, 1.0, "physical", 10.6, 1, 2.6, 2.5));
        reg(bootstrap, "green/electrocuting_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 17.0, 19.0, 21.0, 23.0,
                                25.0, 28.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 49.0, 53.0, 57.0, 62.0,
                                66.0, 71.0, 76.0, 82.0, 88.0, 94.0, 100.0, 107.0, 114.0, 122.0, 130.0, 139.0,
                                148.0, 158.0, 168.0, 179.0
                        ))), "lightning", 13, 1, 3.2));
        reg(bootstrap, "green/elemental_sundering",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 8.0, 9.0, 11.0, 12.0, 14.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0,
                                28.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 50.0, 54.0, 58.0, 62.0, 67.0,
                                72.0, 78.0, 83.0, 89.0, 96.0, 102.0, 109.0, 117.0, 125.0, 133.0, 142.0, 152.0,
                                162.0, 172.0, 184.0, 195.0
                        ))), "fire", 16.6, 1, 4.2, 3.5));
        reg(bootstrap, "green/escape_shot",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                11.0, 12.0, 14.0, 16.0, 18.0, 21.0, 23.0, 26.0, 28.0, 31.0, 35.0, 38.0,
                                42.0, 45.0, 49.0, 54.0, 58.0, 63.0, 69.0, 74.0, 80.0, 86.0, 93.0, 100.0,
                                107.0, 115.0, 124.0, 133.0, 142.0, 152.0, 163.0, 174.0, 186.0, 198.0, 212.0, 226.0,
                                241.0, 256.0, 273.0, 290.0
                        ))), "cold", 10.6, 1, 2.6, SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        reg(bootstrap, "green/explosive_spear",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 13.0, 14.0, 16.0, 17.0,
                                19.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 34.0, 37.0, 40.0, 43.0, 46.0,
                                49.0, 53.0, 57.0, 61.0, 65.0, 70.0, 75.0, 80.0, 86.0, 91.0, 98.0, 104.0,
                                111.0, 118.0, 126.0, 134.0
                        ))), "fire", 10.6, 1, 2.6, new CreateGroundAction("on_ignited_ground", ExpressionCodec.of(2.0), ExpressionCodec.of(160.0)),
                        SkillFactory.ignite(0.20)));
        reg(bootstrap, "green/fangs_of_frost",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 15.0, 16.0,
                                18.0, 19.0, 21.0, 23.0, 25.0, 27.0, 30.0, 32.0, 35.0, 37.0, 40.0, 43.0,
                                46.0, 50.0, 54.0, 57.0, 62.0, 66.0, 71.0, 75.0, 81.0, 86.0, 92.0, 98.0,
                                104.0, 111.0, 118.0, 126.0
                        ))), "cold", 11.8, 1, 3, 2.5, new CreateGroundAction("on_chilled_ground", ExpressionCodec.of(2.2), ExpressionCodec.of(160.0)),
                        SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        reg(bootstrap, "green/freezing_salvo",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                15.0, 18.0, 20.0, 23.0, 26.0, 29.0, 33.0, 36.0, 40.0, 44.0, 49.0, 54.0,
                                59.0, 64.0, 70.0, 76.0, 83.0, 90.0, 97.0, 105.0, 113.0, 122.0, 131.0, 141.0,
                                152.0, 163.0, 175.0, 188.0, 201.0, 215.0, 230.0, 246.0, 263.0, 280.0, 299.0, 319.0,
                                340.0, 362.0, 386.0, 410.0
                        ))), "cold", 11.8, 1, 3, SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        reg(bootstrap, "green/gas_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                9.0, 10.0, 12.0, 13.0, 15.0, 17.0, 19.0, 21.0, 24.0, 26.0, 29.0, 32.0,
                                35.0, 38.0, 41.0, 45.0, 49.0, 53.0, 57.0, 62.0, 67.0, 72.0, 78.0, 84.0,
                                90.0, 97.0, 104.0, 111.0, 119.0, 128.0, 136.0, 146.0, 156.0, 166.0, 177.0, 189.0,
                                202.0, 215.0, 229.0, 243.0
                        ))), "chaos", 14.2, 1, 3.5, SkillFactory.poison(0.20)));
        // 冰霜长枪：投掷穿刺长枪，残片冰冻周围敌人（POE2 "The fragments Chill nearby enemies"）
        reg(bootstrap, "green/glacial_lance",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                8.0, 9.0, 10.0, 12.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0, 28.0,
                                31.0, 33.0, 37.0, 40.0, 43.0, 47.0, 51.0, 55.0, 59.0, 64.0, 69.0, 74.0,
                                80.0, 85.0, 92.0, 98.0, 105.0, 113.0, 120.0, 129.0, 138.0, 147.0, 157.0, 167.0,
                                178.0, 190.0, 202.0, 215.0
                        ))), "cold", 14.2, 1, 3.5, SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        reg(bootstrap, "green/ice_shot",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 15.0, 17.0, 19.0, 20.0,
                                22.0, 24.0, 27.0, 29.0, 31.0, 34.0, 37.0, 40.0, 43.0, 47.0, 50.0, 54.0,
                                58.0, 62.0, 67.0, 72.0, 77.0, 82.0, 88.0, 94.0, 101.0, 107.0, 115.0, 122.0,
                                130.0, 139.0, 148.0, 157.0
                        ))), "cold", 15.4, 1, 3.8, SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
    }

    private static void registerPart3(BootstrapContext<SkillEntry> bootstrap) {

        reg(bootstrap, "green/lightning_rod",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                4.0, 5.0, 6.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 13.0, 14.0, 15.0,
                                17.0, 18.0, 20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 33.0, 35.0, 38.0, 41.0,
                                44.0, 47.0, 51.0, 54.0, 58.0, 62.0, 67.0, 71.0, 76.0, 81.0, 87.0, 93.0,
                                99.0, 105.0, 112.0, 119.0
                        ))), "lightning", 10.6, 1, 2.6));
        reg(bootstrap, "green/magnetic_salvo",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                24.0, 27.0, 31.0, 36.0, 40.0, 45.0, 51.0, 56.0, 62.0, 69.0, 76.0, 83.0,
                                91.0, 99.0, 108.0, 117.0, 128.0, 138.0, 150.0, 162.0, 175.0, 188.0, 203.0, 218.0,
                                234.0, 251.0, 270.0, 289.0, 310.0, 331.0, 354.0, 379.0, 405.0, 432.0, 461.0, 491.0,
                                523.0, 558.0, 594.0, 632.0
                        ))), "lightning", 17.8, 1, 4.5));
        reg(bootstrap, "green/plague_bearer",
                SkillFactory.selfBuff(GemGen.dexReq(), SkillFactory.mana(7), "ailment_damage_rate", 1, 0.50).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        // 剧毒药剂：命中爆开一团毒云，对范围内敌人施加中毒（POE2 "creates a burst of Poison on hit"）
        reg(bootstrap, "green/poisonburst_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 13.0, 14.0, 16.0, 18.0, 19.0,
                                21.0, 23.0, 25.0, 28.0, 30.0, 33.0, 35.0, 38.0, 41.0, 44.0, 48.0, 52.0,
                                55.0, 60.0, 64.0, 69.0, 73.0, 79.0, 84.0, 90.0, 96.0, 103.0, 110.0, 117.0,
                                124.0, 133.0, 141.0, 150.0
                        ))), "chaos", 10.6, 1, 2.6, SkillFactory.poison(0.20)));
        reg(bootstrap, "green/primal_strikes",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 13.0, 15.0, 16.0, 18.0, 20.0,
                                21.0, 23.0, 26.0, 28.0, 30.0, 33.0, 36.0, 38.0, 42.0, 45.0, 48.0, 52.0,
                                56.0, 60.0, 64.0, 69.0, 74.0, 79.0, 85.0, 91.0, 97.0, 103.0, 110.0, 118.0,
                                125.0, 134.0, 142.0, 151.0
                        ))), "lightning", 15.4, 1, 3.8, 2.5));
        reg(bootstrap, "green/rain_of_arrows",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                15.0, 17.0, 20.0, 23.0, 26.0, 29.0, 32.0, 36.0, 40.0, 44.0, 48.0, 53.0,
                                58.0, 63.0, 69.0, 75.0, 81.0, 88.0, 95.0, 103.0, 111.0, 120.0, 129.0, 139.0,
                                149.0, 161.0, 172.0, 185.0, 198.0, 212.0, 226.0, 242.0, 258.0, 276.0, 294.0, 314.0,
                                334.0, 356.0, 379.0, 404.0
                        ))), "physical", 15.4, 1, 3.8));
        reg(bootstrap, "green/rake",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 15.0, 17.0, 19.0, 20.0,
                                22.0, 24.0, 27.0, 29.0, 32.0, 34.0, 37.0, 40.0, 43.0, 47.0, 50.0, 54.0,
                                58.0, 63.0, 67.0, 72.0, 77.0, 83.0, 88.0, 95.0, 101.0, 108.0, 115.0, 123.0,
                                131.0, 139.0, 148.0, 158.0
                        ))), "physical", 11.8, 1, 3, 2.5, SkillFactory.bleed(0.15)));
        reg(bootstrap, "green/rapid_assault",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 24.0, 26.0, 29.0, 32.0, 35.0,
                                38.0, 42.0, 46.0, 50.0, 54.0, 59.0, 63.0, 69.0, 74.0, 80.0, 86.0, 93.0,
                                99.0, 107.0, 115.0, 123.0, 132.0, 141.0, 151.0, 161.0, 172.0, 184.0, 196.0, 209.0,
                                223.0, 237.0, 253.0, 269.0
                        ))), "physical", 13, 1, 3.2, 2.5, 0.6, SkillFactory.bleed(0.15)));
        reg(bootstrap, "green/shockchain_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 26.0, 29.0, 32.0, 35.0,
                                38.0, 42.0, 45.0, 49.0, 54.0, 58.0, 63.0, 68.0, 73.0, 79.0, 85.0, 92.0,
                                99.0, 106.0, 114.0, 122.0, 130.0, 140.0, 149.0, 160.0, 170.0, 182.0, 194.0, 207.0,
                                221.0, 235.0, 250.0, 266.0
                        ))), "lightning", 16.6, 1, 4.2));
        reg(bootstrap, "green/spear_of_solaris",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                13.0, 15.0, 17.0, 20.0, 23.0, 25.0, 28.0, 31.0, 35.0, 39.0, 42.0, 47.0,
                                51.0, 56.0, 61.0, 66.0, 72.0, 78.0, 84.0, 91.0, 98.0, 106.0, 114.0, 123.0,
                                132.0, 142.0, 152.0, 163.0, 174.0, 187.0, 200.0, 213.0, 228.0, 243.0, 260.0, 277.0,
                                295.0, 314.0, 335.0, 356.0
                        ))), "fire", 17.8, 1, 4.5, new CreateGroundAction("on_ignited_ground", ExpressionCodec.of(2.0), ExpressionCodec.of(80.0)),
                        SkillFactory.ignite(0.20)));
        reg(bootstrap, "green/spearfield",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 9.0, 11.0, 12.0, 13.0, 15.0, 16.0, 18.0, 20.0,
                                22.0, 24.0, 26.0, 28.0, 31.0, 33.0, 36.0, 39.0, 42.0, 45.0, 49.0, 53.0,
                                57.0, 61.0, 65.0, 70.0, 75.0, 80.0, 86.0, 92.0, 98.0, 105.0, 112.0, 119.0,
                                127.0, 135.0, 144.0, 153.0
                        ))), "physical", 13, 1, 3.2, 2.5));
        reg(bootstrap, "green/spiral_volley",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 8.0, 10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 22.0, 24.0, 27.0,
                                31.0, 34.0, 38.0, 42.0, 47.0, 52.0, 58.0, 64.0, 71.0, 78.0, 86.0, 94.0,
                                103.0, 114.0, 125.0, 136.0, 149.0, 163.0, 179.0, 195.0, 213.0, 233.0, 254.0, 276.0,
                                301.0, 328.0, 357.0, 388.0
                        ))), "physical", 17.8, 1, 4.5));
    }

    private static void registerPart4(BootstrapContext<SkillEntry> bootstrap) {

        reg(bootstrap, "green/storm_lance",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 17.0, 19.0, 21.0, 23.0,
                                25.0, 28.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 49.0, 53.0, 57.0, 62.0,
                                66.0, 71.0, 76.0, 82.0, 88.0, 94.0, 100.0, 107.0, 114.0, 122.0, 130.0, 139.0,
                                148.0, 158.0, 168.0, 179.0
                        ))), "lightning", 13, 1, 3.2));
        reg(bootstrap, "green/stormcaller_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 8.0, 9.0, 11.0, 12.0, 14.0, 15.0, 17.0, 19.0, 21.0, 23.0, 26.0,
                                28.0, 31.0, 33.0, 36.0, 39.0, 43.0, 46.0, 50.0, 54.0, 58.0, 63.0, 68.0,
                                73.0, 78.0, 84.0, 90.0, 96.0, 103.0, 110.0, 118.0, 126.0, 134.0, 143.0, 153.0,
                                163.0, 174.0, 185.0, 197.0
                        ))), "lightning", 11.8, 1, 3));
        reg(bootstrap, "green/thunderous_leap",
                SkillFactory.movementSkill(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                11.0, 13.0, 14.0, 17.0, 19.0, 21.0, 24.0, 26.0, 29.0, 32.0, 35.0, 39.0,
                                42.0, 46.0, 51.0, 55.0, 60.0, 65.0, 70.0, 76.0, 82.0, 88.0, 95.0, 102.0,
                                110.0, 118.0, 127.0, 136.0, 145.0, 156.0, 166.0, 178.0, 190.0, 203.0, 216.0, 231.0,
                                246.0, 262.0, 279.0, 297.0
                        ))), MoveAction.MoveType.DASH, 1.0, "lightning", 15.4, 1, 3.8, 3));
        reg(bootstrap, "green/tornado_shot",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                13.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 31.0, 34.0, 37.0, 41.0, 45.0,
                                50.0, 54.0, 59.0, 64.0, 70.0, 76.0, 82.0, 88.0, 96.0, 103.0, 111.0, 119.0,
                                128.0, 138.0, 148.0, 158.0, 170.0, 182.0, 194.0, 208.0, 222.0, 237.0, 253.0, 269.0,
                                287.0, 306.0, 326.0, 346.0
                        ))), "physical", 16.6, 1, 4.2));
        reg(bootstrap, "green/toxic_domain",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                24.0, 27.0, 31.0, 34.0, 38.0, 41.0, 45.0, 49.0, 53.0, 57.0, 61.0, 66.0,
                                70.0, 75.0, 80.0, 85.0, 90.0, 95.0, 101.0, 106.0, 112.0, 118.0, 124.0, 130.0,
                                137.0, 144.0, 151.0, 158.0, 165.0, 173.0, 180.0, 188.0, 196.0, 205.0, 214.0, 222.0,
                                232.0, 241.0, 251.0, 261.0
                        ))), "chaos", 17.8, 1, 4.5, 2.5, SkillFactory.poison(0.20)));
        reg(bootstrap, "green/toxic_growth",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                14.0, 16.0, 19.0, 21.0, 24.0, 27.0, 30.0, 34.0, 38.0, 41.0, 46.0, 50.0,
                                55.0, 60.0, 65.0, 71.0, 77.0, 84.0, 90.0, 98.0, 106.0, 114.0, 123.0, 132.0,
                                142.0, 152.0, 163.0, 175.0, 187.0, 201.0, 215.0, 229.0, 245.0, 262.0, 279.0, 298.0,
                                317.0, 338.0, 360.0, 383.0
                        ))), "chaos", 13, 1, 3.2, SkillFactory.poison(0.20)).withTags(GateTag.PLANT));
        reg(bootstrap, "green/twister",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 15.0, 16.0,
                                18.0, 19.0, 21.0, 23.0, 25.0, 27.0, 30.0, 32.0, 35.0, 37.0, 40.0, 43.0,
                                46.0, 50.0, 54.0, 57.0, 62.0, 66.0, 71.0, 75.0, 81.0, 86.0, 92.0, 98.0,
                                104.0, 111.0, 118.0, 126.0
                        ))), "physical", 10.6, 1, 2.6));
        reg(bootstrap, "green/vine_arrow",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 8.0, 9.0, 10.0, 12.0, 13.0, 15.0, 16.0, 18.0, 20.0, 22.0, 24.0,
                                26.0, 29.0, 32.0, 34.0, 37.0, 40.0, 44.0, 47.0, 51.0, 55.0, 59.0, 64.0,
                                69.0, 74.0, 79.0, 85.0, 91.0, 97.0, 104.0, 111.0, 119.0, 127.0, 136.0, 145.0,
                                154.0, 164.0, 175.0, 186.0
                        ))), "chaos", 11.8, 1, 3));
        reg(bootstrap, "green/whirlwind_lance",
                SkillFactory.projectileAttack(GemGen.dexReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 17.0, 19.0, 21.0, 23.0,
                                25.0, 28.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 49.0, 53.0, 57.0, 62.0,
                                66.0, 71.0, 76.0, 82.0, 88.0, 94.0, 100.0, 107.0, 114.0, 122.0, 130.0, 139.0,
                                148.0, 158.0, 168.0, 179.0
                        ))), "physical", 16.6, 1, 4.2, 0.6));
        reg(bootstrap, "green/wind_serpent_s_fury",
                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(10), "physical", 17.8, 1, 4.5, 2.5, 0.8));
        // 手雷主动技能族（thrown 平台首批接线）：投掷 → 引信 3s → 半径 2 范围引爆（单次高伤 AoE）。
        // 伤害走武器攻击通道（WeaponAttack），数值为 skill-level 线性估算，对齐「单次重击」量级。
        // 爆裂手雷：投掷手雷，引信耗尽时引爆烈焰并击退周围敌人。
        reg(bootstrap, "green/explosive_grenade",
                SkillFactory.thrownAttack(GemGen.dexReq(), SkillFactory.mana(12), "fire",
                        18, 1, 4.5, 2.0, 120, 100.0, 0.8));
        // 伏特手雷：投掷手雷，引信耗尽时释放感电闪电并击退敌人（POE2 电击冲击）。
        reg(bootstrap, "green/voltaic_grenade",
                SkillFactory.thrownAttack(GemGen.dexReq(), SkillFactory.mana(10), "lightning",
                        15, 1, 3.2, 2.0, 120, 80.0, 0.7, SkillFactory.electrocute(1.5)));
        // <<<AUTO_END>>>
    
    }

    private static void reg(BootstrapContext<SkillEntry> bootstrap, String path, SkillEntry skill) {
        // Task #377 客户端效果匹配：未显式声明声画身份的技能，按配色/家族注入主题化施放音效+粒子。
        if (skill instanceof Skill s && (s.castSound().isEmpty() || s.castParticle().isEmpty())) {
            skill = SkillFactory.withDefaultCastFx(path, s);
        }
        // 技能皮肤（#CoE）：在 toRead 之前抓取「当前底层渲染 id」并记录，再把投递外观引用覆盖为技能 id。
        // 仅当确实录得底层渲染才覆盖：录不到却覆盖会让投递指向技能 id，而该 id 在
        // coe:entity_render 查不到 → 实体不可见，且皮肤为空也还原不回来。
        if (skill instanceof Skill inline
                && SkillSkinDefs.recordRender(SkillIds.id(path), inline)) {
            skill = inline.withDeliveryRender(SkillIds.id(path));
        }
        skill = toRead(path, skill);
        // 宝石本体路径经 SkillIds 登记（生成期按路径生成），使其他位置可 SkillIds + SkillRead 引用本技能。
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_KEY, SkillIds.id(path)),
                skill.withTier(TIER.getOrDefault(path, 1)));
    }

    /** {@link #toRead} 同 SkillBlueGen：内联技能 → 预设 + SkillRead（发射 Read 标签）。 */
    private static SkillEntry toRead(String path, SkillEntry skill) {
        if (!(skill instanceof Skill full)) return skill;
        String preset = presetName(path);
        SkillPresets.registerPreset(GemGen.id(preset), full);
        return SkillPresets.read(preset);
    }

    /** 预设 id 命名：剥离颜色前缀，仅保留宝石本名。 */
    private static String presetName(String path) {
        return "preset_" + path.substring(path.indexOf('/') + 1);
    }

    /** 等阶（原 GemTiers 数据，就近定义于技能注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "green/lightning_spear", 3,
            "green/galvanic_shards", 5
            // 注：green/blink 为死条目（无对应注册），Blink 真值属**精魂宝石**，已改注册为 blue/blink。
    );
}
