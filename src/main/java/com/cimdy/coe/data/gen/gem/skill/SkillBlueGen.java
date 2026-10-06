package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;

import static com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofLevelTable;

/**
 * 蓝色（智力）主动技能 — 法术投射物、光束与自身增益为主。
 */
public class SkillBlueGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
        // ---- 基础 ----
        reg(bootstrap, "blue/fireball",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(12), "fire", 12, 1, 3,
                        SkillFactory.ignite(0.20)));
        // 霜火矢：缓慢穿透弹，擅长冰冻命中敌人（POE2 "effective at Chilling enemies"）
        reg(bootstrap, "blue/frostbolt",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(9), "cold", 10, 1, 2.5, SkillFactory.chill(0.2)).withTags(GateTag.BOLT));
        reg(bootstrap, "blue/spark",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(6), "lightning", 6, 1, 1.5,
                        SkillFactory.electrocute(1.5)));

        // ---- 扩充（可完整实现：光束 / 范围 / 投射 / 自身增益） ----
        // 电弧连锁：释放链跳电弧，在敌人之间弹射并造成闪电伤害
        reg(bootstrap, "blue/arc",
                SkillFactory.beamSpell(GemGen.intReq(), SkillFactory.mana(9), "lightning", 8, 1, 1.5, 10.0, 1.0,
                        SkillFactory.electrocute(1.5)));
        // 火焰喷射：引导一道火焰光束持续灼烧前方敌人（瞬发光束近似）
        reg(bootstrap, "blue/incinerate",
                SkillFactory.beamSpell(GemGen.intReq(), SkillFactory.mana(7), "fire", 5, 1, 1, 8.0, 1.0,
                        new CreateGroundAction("on_ignited_ground", ExpressionCodec.of(5.8), ExpressionCodec.of(160.0)),
                        SkillFactory.ignite(0.20)));
        // 冰霜新星：以自身为中心爆发寒冰，造成冰霜伤害
        reg(bootstrap, "blue/frost_nova",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(10), "cold", 10, 1, 2, 3.0,
                        SkillFactory.chill(0.2)));
        // 火焰新星：以自身为中心爆发烈焰，造成火焰伤害

        // 火雨：在目标区域降下火焰风暴，持续灼烧范围内敌人
        // 标记 Storm 技能作用域（wildsurge_incantation 基石的 storm|plant 门控需此标签）。
        reg(bootstrap, "blue/firestorm",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(12), "fire", 10, 1, 2, 4.0,
                        SkillFactory.ignite(0.20)).withTags(GateTag.STORM));
        // 点燃爆发：在落点爆发火焰，造成火焰伤害

        // 冰锥：射出冰锥造成冰霜伤害
        reg(bootstrap, "blue/ice_shard",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(8), "cold", 9, 1, 2,
                        SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        // 骨矛：射出一杆贯穿的骨矛造成物理伤害

        // 电球（Ball Lightning）：射出持续放电的闪电球造成闪电伤害
        //
        // 真值（poe2db / poe2wiki，0.4.0）：Tier 11，Int，施放 1.00s，暴击 9%，投射物速度 4 m/s。
        //   「发射一个缓慢移动、可穿越敌人的投射物。该投射物本身无法击中敌人，
        //     但会不断对附近敌人释放闪电。每 0.2 秒击中范围内的敌人；
        //     弩箭以 1.8 公尺内的敌人为目标。375% 更多感电几率。」
        // 故走 ballSpell（球投递：本体不命中 + 周期脉冲）而非 projectileSpell（命中即结算）——
        // 换成投射物会让球在撞到第一个敌人时就消失，完全丢失「放电」语义。
        //
        // 伤害 = 单次脉冲的中值 (min+max)/2，按等级查表严格还原（40 级）：
        //   Lv1 1-4 → 2.5；Lv11 2-33 → 17.5；Lv20 6-105 → 55.5（与 wiki 明示锚点一致）。
        // 数据来源 poe2db 等级表；注：源表 Lv10 的 min 显示为 1（与 Lv9 的 2 相比疑为源数据异常），
        // 此处照抄未擅自修正。
        // 魔力（真值 Lv1-20：19→195）源表在 Lv21+ 缺失，故沿用既有固定值，待补齐后再换查表。
        reg(bootstrap, "blue/ball_lightning",
                SkillFactory.ballSpell(GemGen.intReq(), SkillFactory.mana(10), "lightning", ofLevelTable(
                        List.of(
                                2.5, 3.0, 4.0, 5.0, 6.5, 7.5, 9.5, 11.5, 13.0, 14.5,
                                17.5, 20.0, 23.0, 26.5, 30.0, 33.5, 38.5, 43.5, 49.0, 55.5,
                                62.0, 70.0, 79.0, 89.0, 99.5, 112.0, 126.0, 141.0, 158.5, 178.0,
                                200.0, 224.5, 252.5, 283.5, 319.5, 359.5, 404.5, 456.0, 514.0, 580.0
                        )), new CreateGroundAction("on_ignited_ground", ExpressionCodec.of(0.9), ExpressionCodec.of(80.0)),
                        SkillFactory.ignite(0.20)));
        // 彗星：投掷巨大冰球造成高额冰霜伤害
        reg(bootstrap, "blue/comet",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(16), "cold", 16, 1, 3.5));
        // 冰霜炸弹：投掷寒霜球（原地驻留，复用通用 Orb 投递，ref→coe:frost_bomb），周期冰霜曝曬 + 到期引爆
        reg(bootstrap, "blue/frost_bomb",
                SkillFactory.frostBombSpell(GemGen.intReq(), SkillFactory.mana(11), "cold",
                        ExpressionCodec.ofSkillLevel(11, 1, 2.5),   // ON_HIT 周期冰霜曝曬（沿用原 areaSpell 冷伤预算）
                        ExpressionCodec.ofSkillLevel(11, 1, 2.5),   // ON_EXPIRE 引爆冰霜伤害（同预算；待 poe2db 精确值替换）
                        2.5, 200, 20, 0.05, SkillFactory.chill(0.2)));
        // 混沌箭：射出混沌箭矢造成混沌伤害
        reg(bootstrap, "blue/chaos_bolt",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(6), "chaos", 6, 1, 1.5).withTags(GateTag.BOLT));

        // ---- 召唤（P2-2 SpawnAction）----
        // 唤起僵尸：召唤 2 具僵尸为你作战 30 秒（玩家阵营，只与敌对生物互殴）
        // 打 command 标签：傀儡师(puppet_master)生产者据此识别「[Command] 技能」触发叠层
        reg(bootstrap, "blue/raise_zombie",
                SkillPresets.read("generic_summon")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(18))
                        .vars("summon_count", ExpressionCodec.of(2.0))
                        .vars("summon_duration", ExpressionCodec.of(600.0))
                        .withTags("command", "corpse"));
        // 熔岩宝珠：投掷熔岩宝珠造成火焰伤害

        // 寒冬之眼：射出冰晶造成冰霜伤害

        // 钢铁看守：自身增益（守护），提供闪避与伤害减免（持续 200 tick ≈ 10 秒）
        // <<<AUTO_START>>>
        // 骨爆：咒术骨刺爆发，有几率造成流血（POE2 "potentially causing Bleeding"）
        reg(bootstrap, "blue/bone_blast",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "physical", ofLevelTable(
                        List.of(
                                6.5, 9.0, 13.0, 17.5, 22.5, 28.5, 35.5, 43.5, 53.5, 65.0, 76.0, 89.0,
                                103.5, 120.5, 139.5, 161.0, 186.0, 214.0, 246.0, 282.5, 325.0, 373.5, 429.0, 492.5,
                                566.0, 650.0, 747.5, 859.5, 989.0, 1139.0, 1313.0, 1515.0, 1749.0, 2021.5, 2339.5, 2710.0,
                                3143.0, 3649.5, 4242.5, 4939.0
                        )), 2.5, SkillFactory.bleed(0.15)));
        reg(bootstrap, "blue/bone_cage",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 9.0, 10.0, 12.0, 14.0, 16.0, 19.0, 21.0, 24.0, 27.0, 31.0, 34.0,
                                39.0, 43.0, 48.0, 53.0, 59.0, 66.0, 73.0, 80.0, 89.0, 98.0, 107.0, 118.0,
                                130.0, 143.0, 156.0, 171.0, 187.0, 205.0, 224.0, 245.0, 267.0, 292.0, 318.0, 347.0,
                                378.0, 411.0, 447.0, 487.0
                        ))), "physical", ofLevelTable(
                        List.of(
                                12.5, 17.5, 23.5, 31.0, 39.0, 48.5, 58.5, 70.5, 84.0, 99.5, 117.0, 136.5,
                                159.0, 185.0, 214.0, 247.5, 285.0, 328.5, 377.5, 434.0, 499.0, 573.5, 658.5, 756.0,
                                868.5, 997.5, 1147.5, 1319.5, 1518.5, 1749.0, 2016.0, 2325.5, 2685.0, 3104.0, 3591.5, 4160.5,
                                4825.0, 5602.5, 6513.5, 7582.5
                        )), 3.5));
        reg(bootstrap, "blue/coiling_bolts",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(5), "chaos", ofLevelTable(
                        List.of(
                                11.5, 15.5, 21.5, 27.5, 35.5, 43.0, 52.5, 62.5, 74.0, 87.0, 101.5, 117.5,
                                136.0, 157.0, 180.0, 206.0, 235.5, 268.5, 306.0, 348.5, 396.0, 450.0, 511.5, 581.0,
                                660.0, 749.5, 851.0, 967.0, 1098.5, 1248.5, 1420.0, 1616.0, 1840.0, 2097.0, 2391.0, 2728.5,
                                3116.0, 3562.5, 4077.0,                                 4669.5
                        ))).withTags(GateTag.BOLT));
        reg(bootstrap, "blue/conductivity",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/contagion",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                8.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 32.0, 36.0,
                                40.0, 45.0, 50.0, 55.0, 61.0, 68.0, 75.0, 83.0, 92.0, 101.0, 112.0, 123.0,
                                135.0, 148.0, 162.0, 178.0, 195.0, 213.0, 233.0, 254.0, 278.0, 303.0, 330.0, 360.0,
                                392.0, 427.0, 464.0, 505.0
                        ))), "chaos", 10.6, 1, 2.6, 2.5));
        // 康复 Convalescence 已迁移至精魂（SpiritBlueGen）：精魂常驻施加 CONVALASCENCE 状态。
        reg(bootstrap, "blue/decompose",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "chaos", 10.6, 1, 2.6, 2.5,
                        SkillFactory.poison(0.20)));
        reg(bootstrap, "blue/despair",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/detonate_dead",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                9.0, 11.0, 13.0, 15.0, 18.0, 20.0, 23.0, 26.0, 30.0, 34.0, 38.0, 42.0,
                                48.0, 53.0, 59.0, 66.0, 73.0, 81.0, 89.0, 99.0, 109.0, 120.0, 132.0, 146.0,
                                160.0, 175.0, 192.0, 211.0, 231.0, 252.0, 276.0, 301.0, 329.0, 359.0, 392.0, 427.0,
                                465.0, 506.0, 551.0, 599.0
                        ))), "fire", ofLevelTable(
                        List.of(
                                23.5, 26.5, 30.5, 35.0, 40.0, 45.5, 51.5, 57.5, 64.0, 71.5, 79.5, 88.0,
                                97.0, 106.5, 117.5, 128.5, 140.0, 153.5, 167.0, 181.5, 197.5, 214.5, 232.5, 252.5,
                                273.0, 295.0, 319.0, 345.0, 372.5, 402.0, 433.5, 467.5, 504.0, 543.5, 585.0, 630.0,
                                678.5, 731.0, 786.5, 847.0
                        )), 2.5).withTags(GateTag.DETONATOR));
        reg(bootstrap, "blue/elemental_conflux",
                SkillFactory.selfBuff(GemGen.intReq(), SkillFactory.mana(11), "damage", 1, 0.40).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        // 元素要害（Elemental Weakness，诅咒）：POE2 语义为降低目标元素抗性，
        // 故按诅咒技能实现：施放即对周围 24 格敌人施加
        // 「技能即状态」（StatusEngine.applyCurseToNearby：状态 id=技能 id、图标=技能图标，
        // kind=CURSE），持续 100 tick（5 秒）；范围/时长数据驱动（Skill.curseRange/curseDuration）。
        // 26282「on Cursed targets」条件经 status node (StatusValue.byKind("curse")) 按大类判定命中。
        reg(bootstrap, "blue/elemental_weakness",
                SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(100.0)));
        reg(bootstrap, "blue/ember_fusillade",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                3.0, 4.0, 5.0, 5.0, 6.0, 7.0, 8.0, 10.0, 11.0, 12.0, 14.0, 16.0,
                                18.0, 20.0, 22.0, 24.0, 27.0, 30.0, 33.0, 37.0, 41.0, 45.0, 49.0, 54.0,
                                60.0, 66.0, 72.0, 79.0, 86.0, 94.0, 103.0, 113.0, 123.0, 135.0, 147.0, 160.0,
                                174.0, 190.0, 206.0, 225.0
                        ))), "fire", 10.6, 1, 2.6));
        reg(bootstrap, "blue/enervating_nova",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "lightning", ofLevelTable(
                        List.of(
                                4.0, 5.5, 7.5, 9.5, 12.0, 15.5, 19.0, 23.0, 28.5, 33.5, 40.5, 47.0,
                                54.0, 62.5, 72.0, 82.5, 94.0, 107.5, 122.5, 139.5, 158.5, 180.0, 204.5, 232.5,
                                263.5, 299.5, 340.5, 387.0, 439.5, 499.5, 568.0, 646.5, 736.0, 838.5, 956.5, 1091.5,
                                1246.5, 1425.5, 1630.5, 1868.0
                        )), 3.5, SkillFactory.electrocute(1.5)));
        reg(bootstrap, "blue/enfeeble",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/essence_drain",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 22.0, 24.0,
                                27.0, 30.0, 34.0, 38.0, 42.0, 46.0, 51.0, 57.0, 63.0, 69.0, 76.0, 83.0,
                                92.0, 101.0, 110.0, 121.0, 133.0, 145.0, 158.0, 173.0, 189.0, 206.0, 225.0, 245.0,
                                267.0, 291.0, 316.0, 344.0
                        ))), "chaos", ofLevelTable(
                        List.of(
                                7.0, 9.0, 12.0, 15.5, 20.0, 24.5, 29.5, 35.5, 42.0, 49.0, 57.5, 67.0,
                                77.0, 88.5, 102.0, 117.0, 133.0, 152.5, 173.5, 197.5, 224.5, 255.5, 290.0, 329.0,
                                374.0, 424.5, 482.5, 547.5, 622.5, 707.5, 804.5, 915.5, 1042.5, 1188.5, 1354.5, 1546.0,
                                1766.0, 2018.5, 2310.0, 2646.0
                        ))));
        reg(bootstrap, "blue/eye_of_winter",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                19.0, 22.0, 26.0, 30.0, 35.0, 40.0, 46.0, 52.0, 59.0, 66.0, 75.0, 84.0,
                                94.0, 105.0, 117.0, 130.0, 144.0, 159.0, 176.0, 195.0, 215.0, 236.0, 260.0, 286.0,
                                314.0, 345.0, 378.0, 414.0, 453.0, 496.0, 542.0, 592.0, 646.0, 705.0, 769.0, 838.0,
                                913.0, 994.0, 1081.0, 1176.0
                        ))), "cold", ofLevelTable(
                        List.of(
                                1.5, 2.5, 3.5, 4.0, 5.0, 6.5, 7.5, 9.5, 11.0, 13.0, 15.0, 17.5,
                                20.0, 23.0, 26.0, 30.0, 34.0, 38.5, 43.5, 49.5, 56.0, 63.5, 71.5, 80.5,
                                91.0, 102.5, 115.5, 130.0, 147.0, 166.0, 187.5, 211.5, 239.0, 270.0, 305.0, 345.0,
                                391.0, 443.0, 502.5, 570.0
                        ))));
        reg(bootstrap, "blue/feast_of_flesh",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "physical", 10.6, 1, 2.6, 3.5));
        reg(bootstrap, "blue/firebolt",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(5), "fire", ofLevelTable(
                        List.of(
                                8.5, 12.5, 18.5, 24.5, 31.5, 40.0, 49.5, 60.5, 73.0, 88.5, 105.5, 122.5,
                                141.5, 163.0, 187.0, 214.0, 245.0, 279.5, 318.5, 362.5, 412.5, 468.5, 532.5, 604.0,
                                686.0, 779.0, 885.0, 1005.0, 1142.5, 1298.5, 1476.5, 1680.5, 1913.5, 2180.5, 2486.5, 2837.5,
                                3241.0, 3705.0, 4240.0, 4856.0
                        ))).withTags(GateTag.BOLT));
        reg(bootstrap, "blue/flame_wall",
                SkillFactory.wall(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                10.0, 12.0, 15.0, 17.0, 20.0, 23.0, 26.0, 30.0, 34.0, 38.0, 43.0, 48.0,
                                54.0, 60.0, 67.0, 74.0, 82.0, 91.0, 101.0, 111.0, 123.0, 135.0, 149.0, 164.0,
                                180.0, 198.0, 217.0, 237.0, 260.0, 284.0, 311.0, 339.0, 370.0, 404.0, 441.0, 480.0,
                                523.0, 570.0, 620.0, 674.0
                        ))), "fire", ExpressionCodec.ofSkillLevel(10.6, 1.0, 2.6), "wall_fire",
                        SkillFactory.ignite(0.20)));
        reg(bootstrap, "blue/flammability",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/freezing_shards",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(5), "cold", ofLevelTable(
                        List.of(
                                2.5, 3.5, 4.5, 6.0, 7.5, 9.0, 11.5, 14.0, 17.0, 20.0, 24.0, 28.0,
                                32.5, 37.0, 42.5, 48.0, 55.0, 62.0, 70.0, 80.0, 90.0, 101.5, 115.0, 129.5,
                                146.0, 165.0, 186.0, 210.0, 236.5, 266.5, 301.0, 340.0, 384.0, 434.0, 491.0, 555.0,
                                629.0, 712.5, 808.0, 917.0
                        )), SkillFactory.freeze(1.0)));
        reg(bootstrap, "blue/frost_darts",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 25.0,
                                28.0, 32.0, 35.0, 39.0, 44.0, 48.0, 53.0, 59.0, 65.0, 72.0, 79.0, 87.0,
                                96.0, 105.0, 115.0, 126.0, 138.0, 151.0, 165.0, 181.0, 197.0, 215.0, 235.0, 256.0,
                                279.0, 303.0, 330.0, 359.0
                        ))), "cold", ofLevelTable(
                        List.of(
                                2.5, 3.5, 5.0, 6.0, 7.5, 9.0, 11.0, 13.0, 15.0, 18.0, 21.0, 24.0,
                                27.5, 31.5, 36.0, 41.0, 46.5, 52.5, 59.0, 66.5, 75.0, 84.5, 95.0, 106.5,
                                120.0, 135.0, 151.5, 170.0, 191.0, 214.0, 241.0, 270.5, 304.0, 341.5, 384.5, 432.5,
                                487.5, 549.0, 619.0, 698.5
                        ))));
        reg(bootstrap, "blue/frost_wall",
                SkillFactory.wall(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 21.0, 24.0, 27.0, 30.0, 34.0,
                                38.0, 42.0, 47.0, 53.0, 58.0, 65.0, 72.0, 79.0, 87.0, 96.0, 106.0, 116.0,
                                128.0, 140.0, 154.0, 169.0, 185.0, 202.0, 221.0, 241.0, 263.0, 287.0, 313.0, 341.0,
                                372.0, 405.0, 441.0, 479.0
                        ))), "cold", ofLevelTable(
                        List.of(
                                9.0, 12.0, 16.0, 21.0, 26.0, 32.0, 38.5, 46.0, 54.0, 63.0, 73.5, 84.0,
                                96.5, 110.5, 126.0, 143.5, 162.5, 183.5, 207.5, 233.5, 263.5, 296.5, 333.5, 374.5,
                                421.0, 472.5, 531.0, 596.0, 669.0, 751.5, 844.0, 948.5, 1066.0, 1198.5, 1348.5, 1517.5,
                                1709.0, 1925.5, 2171.0, 2449.5
                        )), "wall_cold", SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        // 雷暴（Fulmination）：自身增益（捷）。激活时非引导闪电法术获得提高的闪电伤害
        // （POE2 真值约 10~15%）。经 Stat 引擎节点 INC_LIGHT_DAMAGE_SPELL（吃 $value）由 StatAction 注入 StatStore。
        List<StatEntry> fulmination = List.of(
                StatGen.ref(StatIds.INC_LIGHT_DAMAGE_SPELL, 0.15));
        reg(bootstrap, "blue/fulmination",
                SkillFactory.selfStatBuff(GemGen.intReq(), SkillFactory.mana(5), fulmination, 200.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        reg(bootstrap, "blue/galvanic_field",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "lightning", ofLevelTable(
                        List.of(
                                2.0, 2.5, 3.5, 4.5, 6.0, 7.5, 9.0, 11.0, 13.5, 16.5, 19.5, 23.0,
                                27.0, 31.0, 36.0, 41.5, 48.0, 55.5, 63.5, 73.0, 83.5, 96.5, 110.5, 127.5,
                                146.5, 168.0, 193.0, 222.0, 255.5, 294.0, 339.0, 391.0, 451.5, 522.0, 604.0, 700.0,
                                811.5, 942.0, 1096.0, 1275.5
                        )), 2.5));
        reg(bootstrap, "blue/hexblast",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                12.0, 14.0, 17.0, 20.0, 23.0, 26.0, 30.0, 34.0, 38.0, 43.0, 49.0, 55.0,
                                61.0, 68.0, 76.0, 84.0, 94.0, 104.0, 115.0, 127.0, 140.0, 154.0, 170.0, 187.0,
                                205.0, 225.0, 247.0, 270.0, 296.0, 324.0, 354.0, 387.0, 422.0, 460.0, 502.0, 547.0,
                                596.0, 649.0, 706.0, 768.0
                        ))), "chaos", ofLevelTable(
                        List.of(
                                30.0, 41.0, 56.0, 73.0, 93.0, 114.5, 140.0, 168.5, 200.5, 237.5, 279.0, 326.0,
                                380.0, 441.0, 510.5, 590.0, 680.0, 783.0, 901.5, 1036.0, 1190.0, 1367.0, 1570.0, 1803.0,
                                2071.5, 2380.0, 2736.0, 3147.0, 3622.0, 4171.5, 4808.5, 5547.0, 6405.5, 7404.0, 8567.0, 9924.0,
                                11509.5, 13364.5, 15537.5, 18087.0
                        )), 2.5));
        reg(bootstrap, "blue/hypothermia",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/ice_nova",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 8.0, 10.0, 11.0, 13.0, 15.0, 17.0, 20.0, 22.0, 25.0, 28.0, 32.0,
                                36.0, 40.0, 44.0, 49.0, 55.0, 61.0, 67.0, 74.0, 82.0, 90.0, 99.0, 109.0,
                                120.0, 131.0, 144.0, 158.0, 173.0, 189.0, 207.0, 226.0, 247.0, 269.0, 294.0, 320.0,
                                348.0, 379.0, 413.0, 449.0
                        ))), "cold", ofLevelTable(
                        List.of(
                                7.0, 8.5, 11.0, 14.0, 17.5, 21.5, 26.0, 31.0, 36.5, 42.5, 50.0, 57.5,
                                66.0, 76.0, 86.5, 99.0, 112.5, 127.5, 144.5, 163.5, 185.0, 209.0, 236.0, 266.0,
                                300.0, 338.5, 381.5, 431.0, 486.0, 548.0, 618.5, 698.5, 789.0, 891.5, 1008.5, 1141.0,
                                1291.5, 1463.5, 1660.0,                                 1883.5
                        )), 3.5, 0.6, new CreateGroundAction("on_chilled_ground", ExpressionCodec.of(4.8), ExpressionCodec.of(160.0)),
                        SkillFactory.chill(0.2), SkillFactory.freeze(1.0)));
        reg(bootstrap, "blue/icestorm",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                12.0, 14.0, 17.0, 19.0, 22.0, 26.0, 30.0, 34.0, 38.0, 43.0, 48.0, 54.0,
                                61.0, 68.0, 75.0, 84.0, 93.0, 103.0, 114.0, 126.0, 139.0, 153.0, 169.0, 186.0,
                                204.0, 224.0, 245.0, 269.0, 294.0, 322.0, 352.0, 384.0, 420.0, 458.0, 499.0, 544.0,
                                593.0, 645.0, 702.0, 764.0
                        ))), "cold", ofLevelTable(
                        List.of(
                                4.0, 5.0, 7.5, 9.0, 11.5, 14.5, 17.5, 21.0, 25.0, 29.0, 34.0, 39.0,
                                45.0, 52.5, 60.0, 68.5, 78.5, 89.5, 102.0, 116.0, 132.5, 150.0, 170.5, 193.5,
                                220.0, 250.0, 283.5, 322.5, 366.0, 416.0, 473.5, 538.5, 613.5, 699.0, 797.0, 909.5,
                                1039.0, 1187.5, 1359.0, 1556.5
                        )), 2.5).withTags(GateTag.STORM));
        reg(bootstrap, "blue/lightning_bolt",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "lightning", ofLevelTable(
                        List.of(
                                8.0, 11.0, 15.5, 20.0, 26.0, 32.5, 40.5, 49.5, 60.0, 72.0, 86.5, 100.5,
                                116.5, 133.5, 153.5, 176.0, 201.0, 229.5, 261.0, 297.5, 338.0, 384.0, 437.0, 496.0,
                                563.0, 639.5, 726.5, 824.5, 937.5, 1065.5, 1211.5, 1379.0, 1570.0, 1789.0, 2040.0, 2328.5,
                                2659.5, 3040.0, 3479.0, 3984.5
                        )), 2.5));
        reg(bootstrap, "blue/lightning_conduit",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                19.0, 22.0, 26.0, 30.0, 35.0, 40.0, 46.0, 52.0, 59.0, 66.0, 75.0, 84.0,
                                94.0, 105.0, 117.0, 130.0, 144.0, 159.0, 176.0, 195.0, 215.0, 236.0, 260.0, 286.0,
                                314.0, 345.0, 378.0, 414.0, 453.0, 496.0, 542.0, 592.0, 646.0, 705.0, 769.0, 838.0,
                                913.0, 994.0, 1081.0, 1176.0
                        ))), "lightning", ofLevelTable(
                        List.of(
                                11.5, 14.5, 20.5, 26.5, 34.0, 42.0, 51.5, 61.5, 73.5, 86.5, 102.0, 119.5,
                                138.5, 161.5, 186.5, 215.5, 248.5, 286.0, 329.5, 378.5, 434.5, 499.5, 574.0, 658.5,
                                756.5, 870.0, 1000.0, 1150.0, 1323.5, 1524.0, 1756.5, 2026.5, 2340.5, 2705.5, 3130.0, 3626.0,
                                4205.5, 4883.5, 5677.5, 6608.5
                        )), 2.5));
        reg(bootstrap, "blue/lightning_warp",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                8.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 32.0, 36.0,
                                40.0, 45.0, 50.0, 55.0, 61.0, 68.0, 75.0, 83.0, 92.0, 101.0, 112.0, 123.0,
                                135.0, 148.0, 162.0, 178.0, 195.0, 213.0, 233.0, 254.0, 278.0, 303.0, 330.0, 360.0,
                                392.0, 427.0, 464.0, 505.0
                        ))), "lightning", ofLevelTable(
                        List.of(
                                16.0, 21.5, 29.5, 38.5, 48.5, 60.0, 73.0, 88.5, 105.5, 124.0, 146.5, 171.0,
                                199.0, 231.0, 268.0, 309.5, 357.0, 410.5, 472.5, 543.0, 624.0, 717.0, 823.0, 946.0,
                                1086.5, 1248.0, 1434.5, 1650.5, 1899.5, 2187.5, 2521.0, 2908.5, 3359.0, 3882.0, 4492.0, 5203.5,
                                6035.5, 7008.0, 8147.5, 9484.0
                        )), 2.5, new CreateGroundAction("on_shocked_ground", ExpressionCodec.of(3.0), ExpressionCodec.of(160.0))));
        reg(bootstrap, "blue/living_bomb",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                8.0, 9.0, 11.0, 13.0, 15.0, 17.0, 20.0, 23.0, 26.0, 29.0, 32.0, 36.0,
                                41.0, 46.0, 51.0, 57.0, 63.0, 70.0, 77.0, 85.0, 94.0, 103.0, 114.0, 125.0,
                                138.0, 151.0, 166.0, 181.0, 199.0, 217.0, 238.0, 260.0, 283.0, 309.0, 337.0, 367.0,
                                400.0, 436.0, 474.0, 516.0
                        ))), "fire", ofLevelTable(
                        List.of(
                                12.5, 18.0, 26.0, 35.0, 46.0, 58.0, 71.5, 87.5, 106.0, 126.5, 150.0, 177.0,
                                207.5, 243.5, 284.0, 331.0, 385.0, 447.0, 518.5, 601.0, 696.5, 806.5, 935.0, 1083.0,
                                1255.0, 1456.0, 1689.0, 1961.0, 2279.0, 2651.0, 3086.0, 3596.5, 4196.5, 4902.5, 5734.0, 6716.0,
                                7876.5, 9251.0, 10881.0, 12817.0
                        )), 2.5));
        // 折射之镜（Mirror of Refraction）：自身增益（守护）。激活时受到暴击伤害降低（POE2 真值 20~25%）。
        // 经 Stat 引擎节点 INC_TAKEN_CRIT_DAMAGE（吃 $value）由 StatAction 注入 StatStore。
        // 注：格挡反弹伤害（Reflect X% of Damage Prevented by Block）暂无对应 Stat 节点，待引擎扩展后补。
        List<StatEntry> mirror = List.of(
                StatGen.ref(StatIds.INC_TAKEN_CRIT_DAMAGE, -0.25));
        reg(bootstrap, "blue/mirror_of_refraction",
                SkillFactory.selfStatBuff(GemGen.intReq(), SkillFactory.mana(5), mirror, 200.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        // 风暴漩涡（Orb of Storms）：真值（poe2db / poe2wiki 0.3.0）——
        //   「创造一个电力球，持续对附近敌人发射连锁闪电。球到期时留下闪电灌注残迹。」
        //   每 4 秒发射一次（80 tick）；球半径 3.6 米；球持续 12 秒（240 tick）；同时最多 1 个；
        //   基础暴击 10%；伤害表 = 单次放电的中值（Lv20 14~260 → 137）。
        // 故走 orbSpell（原地驻场 + 周期脉冲），而非 areaSpell（一次性范围结算）——
        // 一次性结算会把「12 秒里持续放电」压成「一发范围伤害」，完全丢失 Orb 语义。
        //
        // 未实装（待机制，不伪造）：① 链式（Chaining 3~5 次，项目内无 chain 机制）；
        // ② 「在球附近施放法术时额外发射一次」（需 EventListenPhase 的空间条件，当前无）；
        // ③ 到期留下闪电灌注残迹；④ 同时最多 1 个（Limit 机制未落地）。
        reg(bootstrap, "blue/orb_of_storms",
                SkillFactory.orbSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                12.0, 14.0, 17.0, 20.0, 23.0, 26.0, 30.0, 34.0, 39.0, 44.0, 49.0, 55.0,
                                62.0, 69.0, 77.0, 86.0, 95.0, 105.0, 117.0, 129.0, 142.0, 157.0, 172.0, 189.0,
                                208.0, 228.0, 250.0, 274.0, 300.0, 328.0, 359.0, 392.0, 428.0, 467.0, 509.0, 555.0,
                                605.0, 658.0, 716.0, 779.0
                        ))), "lightning", ofLevelTable(
                        List.of(
                                4.5, 6.0, 8.0, 10.0, 13.0, 16.0, 19.5, 23.0, 28.0, 32.5, 38.5, 44.5,
                                51.5, 60.0, 69.0, 79.5, 91.0, 104.0, 119.5, 137.0, 156.5, 178.5, 203.5, 232.5,
                                266.0, 303.5, 347.0, 397.0, 453.5, 519.0, 594.0, 681.0, 780.5, 896.0, 1029.0, 1183.0,
                                1362.0, 1569.0, 1810.0, 2090.0
                        )), 3.6, 240, 80, 0.10)                .withTags(GateTag.STORM));
        // Blight（蓝）：混沌地面危害 —— coe:hazard 平台首个真实技能消费方。
        // 敌人踏入 contact(enemy) → 点燃 ON_HIT（混沌伤害）+ 施 hazard_mark + 记录 HAZARD_TRIGGERED；
        // HAZARD_DAMAGE 等作用域经实体 GateTag.HAZARD 命中。半径 2m、存活 4s。
        reg(bootstrap, "blue/blight",
                SkillFactory.hazardSpell(GemGen.intReq(), SkillFactory.mana(6), "chaos", ofLevelTable(
                        List.of(
                                6.0, 7.0, 9.0, 11.0, 13.0, 15.0, 18.0, 21.0, 24.0, 28.0, 32.0, 37.0,
                                42.0, 48.0, 55.0, 62.0, 70.0, 79.0, 89.0, 100.0, 113.0, 127.0, 143.0, 161.0,
                                181.0, 203.0, 228.0, 256.0, 287.0, 322.0, 361.0, 405.0, 454.0, 509.0, 571.0, 640.0,
                                718.0, 805.0, 902.0, 1011.0
                        )), 2.0, 80).withTags(GateTag.HAZARD));
        reg(bootstrap, "blue/profane_ritual",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 7.0, 8.0, 10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0,
                                31.0, 35.0, 39.0, 43.0, 48.0, 53.0, 59.0, 65.0, 71.0, 79.0, 87.0, 95.0,
                                105.0, 115.0, 126.0, 138.0, 151.0, 166.0, 181.0, 198.0, 216.0, 236.0, 257.0, 280.0,
                                305.0, 332.0, 361.0, 393.0
                        ))), "chaos", 14.2, 1, 3.5, 2.5));
        reg(bootstrap, "blue/reap",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "physical", ofLevelTable(
                        List.of(
                                13.5, 18.5, 26.0, 34.0, 43.5, 55.0, 68.5, 83.0, 101.0, 121.5, 145.0, 168.5,
                                195.0, 224.5, 257.5, 295.0, 337.5, 385.0, 439.0, 499.5, 568.0, 645.5, 733.5, 832.5,
                                946.0, 1074.0, 1220.0, 1385.5, 1574.0, 1790.0, 2035.5, 2316.0, 2637.5, 3005.0, 3426.5, 3911.0,
                                4466.5, 5106.5, 5843.5, 6692.5
                        )), 2.5));
        reg(bootstrap, "blue/snap",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 21.0, 24.0, 27.0, 30.0, 34.0, 38.0,
                                43.0, 48.0, 53.0, 59.0, 65.0, 73.0, 80.0, 89.0, 98.0, 108.0, 119.0, 131.0,
                                144.0, 158.0, 173.0, 189.0, 207.0, 227.0, 248.0, 271.0, 296.0, 322.0, 352.0, 383.0,
                                417.0, 454.0, 495.0, 538.0
                        ))), "fire", ofLevelTable(
                        List.of(
                                7.0, 9.5, 13.0, 17.0, 21.5, 26.5, 32.5, 39.0, 46.5, 55.0, 65.0, 76.0,
                                88.5, 102.5, 118.5, 136.5, 157.5, 181.5, 209.0, 240.0, 276.0, 317.5, 364.0, 418.5,
                                481.0, 552.5, 635.0, 730.0, 840.5, 968.0, 1116.0, 1287.5, 1486.5, 1718.0, 1988.0, 2302.5,
                                2671.0, 3101.0, 3605.5, 4197.5
                        )), 2.5));
        // 日曜球（Solar Orb）：真值（poe2db / poe2wiki 0.3.0）——
        //   「创造一个会周期性释放火焰脉冲的火球。非常靠近球体的敌人会被点燃。」
        //   每 1.4 秒脉冲一次（28 tick）；爆炸范围 2.5 米；球持续 10 秒（200 tick）；
        //   基础暴击 7%；伤害表 = 单次脉冲的中值（Lv20 181~271 → 226）。
        // 接线（通用，复用 Frost Bomb 范式）：投掷 coe:sun_orb 实体（static ⊕ [pulse(28), detonate(200)]），
        // ON_HIT 周期火焰脉冲 + ON_EXPIRE 烈焰爆破 + 对附近敌人施加点燃(StatAction IGNITE, 1200t=1m)。
        // 已知简化：POE2「1 米内点燃」的逐目标小半径约束由 detonate 作用域(2.5m)近似覆盖，
        // 逐目标半径点燃属新机制，不在本次通用接线内。
        reg(bootstrap, "blue/solar_orb",
                SkillFactory.sunOrbSpell(GemGen.intReq(), SkillFactory.mana(5), "fire",
                        ofLevelTable(List.of(
                                4.5, 6.5, 10.0, 13.5, 17.5, 22.5, 27.5, 34.0, 41.5, 50.5, 61.0, 71.0,
                                82.5, 96.0, 111.5, 128.5, 148.5, 171.0, 196.5, 226.0, 259.5, 298.0, 342.5, 393.0,
                                451.5, 519.0, 596.5, 686.0, 789.5, 909.0, 1048.0, 1209.0, 1396.0, 1613.5, 1867.5, 2163.0,
                                2508.5, 2912.5, 3386.5, 3942.0
                        )),
                        ofLevelTable(List.of(
                                4.5, 6.5, 10.0, 13.5, 17.5, 22.5, 27.5, 34.0, 41.5, 50.5, 61.0, 71.0,
                                82.5, 96.0, 111.5, 128.5, 148.5, 171.0, 196.5, 226.0, 259.5, 298.0, 342.5, 393.0,
                                451.5, 519.0, 596.5, 686.0, 789.5, 909.0, 1048.0, 1209.0, 1396.0, 1613.5, 1867.5, 2163.0,
                                2508.5, 2912.5, 3386.5, 3942.0
                        )),
                        2.5, 200, 28, 0.07, SkillFactory.ignite(0.20)));
        reg(bootstrap, "blue/soulrend",
                SkillFactory.projectileSpell(GemGen.intReq(), SkillFactory.mana(5), "chaos", ofLevelTable(
                        List.of(
                                4.5, 7.0, 9.5, 12.5, 16.0, 20.5, 25.5, 31.0, 37.5, 45.5, 54.0, 63.0,
                                72.5, 83.5, 96.0, 110.0, 125.5, 143.0, 163.0, 186.0, 211.5, 240.0, 273.0, 310.0,
                                351.5, 400.0, 454.0, 515.5, 586.0, 666.0, 757.5, 861.5, 981.5, 1118.5, 1275.5, 1455.5,
                                1662.0, 1900.0, 2174.0, 2490.0
                        ))));
        reg(bootstrap, "blue/temporal_chains",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
        reg(bootstrap, "blue/untether",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "physical", ofLevelTable(
                        List.of(
                                15.5, 22.5, 32.5, 44.0, 57.5, 72.5, 90.0, 109.5, 132.5, 157.5, 187.5, 221.0,
                                260.0, 304.0, 355.0, 413.5, 481.0, 558.5, 648.0, 751.5, 870.5, 1008.5, 1168.5, 1354.0,
                                1569.0, 1820.0, 2111.5, 2451.5, 2849.0, 3313.5, 3857.5, 4496.0, 5246.0, 6128.5, 7168.0, 8395.0,
                                9846.0, 11563.5, 13601.0, 16021.0
                        )), 2.5));
        reg(bootstrap, "blue/volatile_dead",
                SkillFactory.areaSpell(GemGen.intReq(), SkillFactory.mana(5), "fire", ofLevelTable(
                        List.of(
                                3.5, 4.0, 4.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.5, 13.5,
                                15.0, 16.0, 17.5, 18.5, 20.0, 21.5, 23.0, 24.5, 26.0, 27.5, 29.5, 31.0,
                                33.0, 35.0, 36.5, 38.5, 40.5, 42.5, 44.5, 46.5, 49.0, 51.0, 53.5, 55.5,
                                58.0, 60.0, 62.5, 65.0
                        )), 2.5, SkillFactory.ignite(0.20)));
        reg(bootstrap, "blue/vulnerability",
        SkillPresets.read("generic_curse")
                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))
                        .vars("curse_range", ExpressionCodec.of(24.0))
                        .vars("curse_duration", ExpressionCodec.of(120.0)));
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

    /**
     * 数据驱动迁移（#87）：内联完整技能登记为 {@code coe:preset_<gem>} 预设，宝石本体降为
     * {@code SkillRead(ref=preset_<gem>)} 引用之——运行时由 {@code SkillRead.resolve()} 发射
     * Read 标签（{@code preset_<gem>}），使数据侧「对 [某技能] 类技能」的加成可命中。
     *
     * <p>入参已是 {@link SkillRead}（通用预设 / 图腾等已迁移条目）时原样返回，不重复登记。
     * 等阶不受影响：{@code SkillRead.withTier} 写入 vars 槽，{@code resolve()} 时应用，
     * 与原先直接 {@code Skill.withTier} 等价。
     */
    private static SkillEntry toRead(String path, SkillEntry skill) {
        if (!(skill instanceof Skill full)) return skill;
        String preset = presetName(path);
        SkillPresets.registerPreset(GemGen.id(preset), full);
        return SkillPresets.read(preset);
    }

    /** 预设 id 命名：剥离颜色前缀，仅保留宝石本名（如 {@code red/fireball} → {@code preset_fireball}）。 */
    private static String presetName(String path) {
        return "preset_" + path.substring(path.indexOf('/') + 1);
    }

    /** 等阶（原 GemTiers 数据，就近定义于技能注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "blue/arc", 5,
            "blue/ball_lightning", 11,
            "blue/comet", 11,
            "blue/fireball", 3,
            "blue/firestorm", 11,
            "blue/frostbolt", 5,
            "blue/ice_shard", 5,
            "blue/incinerate", 9,
            "blue/raise_zombie", 3
    );
}
