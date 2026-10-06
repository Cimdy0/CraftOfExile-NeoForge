package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.status.def.AilmentData;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofLevelTable;

/**
 * 红色（力量）主动技能 — 近战/范围猛击、位移与处决为主。
 */
public class SkillRedGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
    registerPart1(bootstrap);
    registerPart2(bootstrap);
}

    /**
     * 击晕异常构造器（数据驱动）：buildup 为单次命中累积量，越高越易触发重晕。
     * 用于猛击族「Stun enemies / Heavy Stun」语义（rolling_slam 轻晕、boneshatter 重晕）。
     */
    private static AilmentData stun(double buildup) {
        return new AilmentData(StatusDefs.STUN, ExpressionCodec.of(buildup),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static void registerPart1(BootstrapContext<SkillEntry> bootstrap) {

        // ---- 形态攻击（POE2 Druid 实证）：使用即变身（tags 含 shapeshifted + 形态名，
        // 经 SkillInstance 构造器回调自动 applyForm）----
        // 熊形态基础攻击 Maul：重击生成 Rage（POE2 poe2wiki 实证：每次命中 +5 Rage）
        reg(bootstrap, "red/bear_maul",
                SkillPresets.read("generic_shapeshift_physical_rage")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(0))
                        .vars("dmg", ExpressionCodec.ofSkillLevel(18, 2, 4))
                        .withTags(GateTag.BEAR));
        // 龙形态基础攻击 Rend：近战利爪 + 闪电（POE2 wyvern form 定位：近战爪击 + 电）
        reg(bootstrap, "red/wyvern_rend",
                SkillPresets.read("generic_shapeshift_lightning")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(0))
                        .vars("dmg", ExpressionCodec.ofSkillLevel(15, 2, 3))
                        .withTags(GateTag.WYVERN));

        // ---- 基础猛击 ----
        // rolling_slam：猛砸地面击晕并击退敌人，随后前冲二次猛砸（POE2 二次冲击以击退+范围伤害近似）。
        reg(bootstrap, "red/rolling_slam",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(7),
                        "physical", 14, 1, 3, 3.5, stun(0.20)));
        // earthquake：砸地造成范围伤害并在落点留下锯齿地面（Jagged Ground，减速），持续后触发余震。
        reg(bootstrap, "red/earthquake",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(9),
                        "physical", 16, 1, 3.5, 4.0,
                        new CreateGroundAction("on_jagged_ground",
                                ExpressionCodec.of(4.0), ExpressionCodec.of(100.0))));

        // 终结猛击：技能自身携带 culling 修正（与 blue/culling_strike 辅助宝石共用同一 EffectMod 形状），
        // 印证「技能本身也能做到辅助能做的事」。

        // ---- 扩充猛击（可完整实现：范围 + 伤害 / 击退） ----
        // 跃击：冲刺至目标点并造成范围物理伤害
        reg(bootstrap, "red/leap_slam",
                SkillFactory.movementSkill(GemGen.strReq(), SkillFactory.mana(9),
                        MoveAction.MoveType.DASH, 1.2, "physical", 14, 1, 3, 3.0, 0.6, SkillFactory.stun(0.20)));
        // 火山裂隙：猛砸地面引发熔岩裂隙（POE2 Molten Fissure「deals damage as it travels and remains
        // for a duration」）—— 落点留下点燃地面（on_ignited_ground，火焰 DoT），真实还原裂隙残留语义。
        // 注：原投机击退（0.6）在 POE2 描述中无依据，已替换为点燃地面云。
        reg(bootstrap, "red/volcanic_fissure",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(11),
                        "fire", 12, 1, 2.5, 3.0,
                        new CreateGroundAction("on_ignited_ground",
                                ExpressionCodec.of(4.0), ExpressionCodec.of(100.0))));
        // 冲击波：释放冲击波击退并伤害前方敌人
        reg(bootstrap, "red/shockwave",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(7),
                        "physical", 10, 1, 2, 3.0, 0.8));
        // 余震：在猛击落点引发二次范围物理伤害
        reg(bootstrap, "red/aftershock",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(7),
                        "physical", 12, 1, 2.5, 2.5));
        // 碎骨：猛击造成范围物理伤害，对已被 Prime 的敌人造成重晕（Heavy Stun），重晕时额外引发范围冲击波。
        // 注意：poe2db 权威描述含 "Cannot cause Stun buildup" —— 碎骨不累积晕眩槽，仅在目标已被 Prime 时触发重晕。
        // 当前工厂无「Prime 条件重晕」专用构造器，为避免与描述冲突（错接通用 stun 累积），此处仅保留范围物理猛击，
        // 重晕-on-Prime 作为已知未建模限制（与「不近似接线」原则一致：宁缺毋错接）。
        reg(bootstrap, "red/boneshatter",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(9),
                        "physical", 13, 1, 3, 2.5));
        // 裂地：劈开地面造成范围物理伤害；波击中的敌人释放冲击波击退其他敌人（POE2 shockwave）。
        // 注：poe2db 权威描述未提及击退（Sunder 本体不击退），原 0.6 击退为近似错接，已回退。
        reg(bootstrap, "red/sunder",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(11),
                        "physical", 15, 1, 3, 3.0));
        // 穿刺：向前突刺造成范围物理伤害
        reg(bootstrap, "red/perforate",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(8),
                        "physical", 11, 1, 2.5, 2.5, SkillFactory.bleed(0.15)));
        // 地裂斩：大范围横扫物理伤害。POE2 中 Ground Slam 留下的是「Cracked Ground」，而非 Jagged Ground，
        // 当前 GroundTypes 仅注册 jagged/ignited/shocked/chilled 四种，无 cracked 类型；为避免错接（把无关地面类型接到技能上），
        // 此处不接线地面，待补充 on_cracked_ground 类型后精确补接。
        reg(bootstrap, "red/ground_slam",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(8),
                        "physical", 12, 1, 2.5, 4.0));
        // 战旗 War Banner（poe2db Id=WarBanner，Uncut Spirit Gem Tier 4）：
        // 标签 Banner | Buff, Persistent, AoE, Aura, Duration, Conditional；ActiveType 无 Attack/Damage
        //   → 技能本体<b>不造成伤害</b>（原 red/banner 的物理猛击为错接，已随 generic_banner 移除）。
        // 真值参数：「Banner Aura radius is 6 metres」「Banner duration is 6 seconds」（Gem Lv1）。
        // 光环授予（见 StatusBehavior#bannerAura）：25% more Attack damage / 50% inc Accuracy / 25% inc Attack Speed。
        reg(bootstrap, "red/war_banner",
                SkillPresets.read("generic_banner")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(10))
                        .vars("banner_radius", ExpressionCodec.of(6.0))
                        .vars("banner_duration", ExpressionCodec.of(6.0))
                        .vars("banner_magnitude", ExpressionCodec.of(1.0)));
        // 战吼 Seismic Cry（poe2db Id=SeismicCry，Uncut Skill Gem Tier 11）：
        // 标签 Warcry | AoE, Physical, Nova；ActiveType 含 Damage —— 真值中<b>唯一造成伤害</b>的战吼
        //   （"Perform a damaging Warcry, Knocking Back nearby enemies"），故 areaSpell+knockback 范式忠实对位。
        // 真值参数：「Warcry radius is 4 metres」、Cost (19—85) Mana、Deals (7—223) to (10—335) Physical。
        // 伤害按等级表严格还原（各级 min/max 均值），替代线性估算。
        // 未实现部分（不影响已实现语义的准确性）：Empowers Slam per 10 enemy Power、Heavy Stun、忍耐充能绕过冷却。
        reg(bootstrap, "red/seismic_cry",
                SkillPresets.read("generic_warcry")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(19))
                        .vars("warcry_radius", ExpressionCodec.of(4.0))
                        .vars("warcry_knockback", ExpressionCodec.of(1.0))
                        .vars("warcry_damage", ExpressionCodec.ofLevelTable(java.util.List.of(
                                8.5, 11.0, 15.0, 20.0, 25.0, 31.0, 37.5, 45.0, 54.0, 64.0,
                                75.0, 87.5, 102.5, 118.5, 137.5, 159.0, 183.0, 211.0, 242.5, 279.0)))
                        .withTags(GateTag.AOE));
        // 尸体消耗（Consume Corpse）：消耗 1 具尸体并授予「近期消耗」增益（#54）
        reg(bootstrap, "red/consume_corpse",
                SkillPresets.read("generic_consume_corpse")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(1)));
        // 神之锤：蓄力重砸造成高额范围物理伤害

        // 破裂：撕裂敌人造成范围物理伤害
        reg(bootstrap, "red/rupture",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(10),
                        "physical", 14, 1, 3, 3.0));
        // 完美打击：高暴击范围物理伤害
        reg(bootstrap, "red/perfect_strike",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(11),
                        "physical", 16, 1, 3, 2.5));
        // 践踏：冲锋撕裂大地，每步留下锯齿地面（Jagged Ground），终结猛踩引爆全部锯齿地面。
        reg(bootstrap, "red/stampede",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(9),
                        "physical", 13, 1, 2.5, 3.0,
                        new CreateGroundAction("on_jagged_ground",
                                ExpressionCodec.of(4.0), ExpressionCodec.of(100.0))));

        // ---- 图腾（P2-2 SpawnAction）----
        // 震波图腾：立起图腾驻场 15 秒，每秒对 8 格内最近敌人造成物理伤害
        // 真值标签（poe2wiki 0.3.0）：Attack, Totem, AoE, Melee, Slam, Duration, Nova
        // 注：0.3.0 起 Shockwave Totem 已移除 Physical 标签；Attack 由 cast 决定，此处只声明机制/形态标签。
        reg(bootstrap, "red/shockwave_totem",
                SkillPresets.read("generic_totem")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(14))
                        .vars("totem_health", ExpressionCodec.of(30.0))
                        .vars("totem_damage", ExpressionCodec.ofSkillLevel(8, 1, 2))
                        .withTags(GateTag.AOE, GateTag.MELEE, GateTag.SLAM,
                                GateTag.DURATION, GateTag.NOVA));
        // 先祖战士图腾：攻击更快、射程较短的近卫图腾
        // 真值标签（poe2wiki 0.3.0）：Attack, Totem, AoE, Melee, Duration, Conditional, Meta
        // Meta（可镶嵌）待元技能镶嵌机制落地后再补，避免「贴标签但机制未实现」的虚接。
        reg(bootstrap, "red/ancestral_warrior_totem",
                SkillPresets.read("generic_totem")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(12))
                        .vars("totem_interval", ExpressionCodec.of(14.0))
                        .vars("totem_range", ExpressionCodec.of(5.0))
                        .vars("totem_health", ExpressionCodec.of(40.0))
                        .vars("totem_damage", ExpressionCodec.ofSkillLevel(6, 1, 1.5))
                        .withTags(GateTag.AOE, GateTag.MELEE, GateTag.DURATION));
        // 处决：范围猛击 + 斩杀（目标生命比例低于阈值时该次伤害致命，35% 对齐普通/魔法怪常用值）
        reg(bootstrap, "red/execute",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(10),
                        "physical", 14, 1, 3, 3.0, List.of(executeMod(0.35))));
    }

    private static void registerPart2(BootstrapContext<SkillEntry> bootstrap) {

        // <<<AUTO_START>>>
        reg(bootstrap, "red/armor_breaker",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 13.0, 14.0, 16.0, 17.0, 19.0,
                                21.0, 23.0, 25.0, 27.0, 30.0, 32.0, 35.0, 38.0, 41.0, 44.0, 47.0, 51.0,
                                55.0, 59.0, 63.0, 68.0, 72.0, 78.0, 83.0, 89.0, 95.0, 101.0, 108.0, 115.0,
                                123.0, 131.0, 139.0, 148.0
                        ))), "physical", 11.8, 1, 3, 2.5));
        reg(bootstrap, "red/earthshatter",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                7.0, 8.0, 10.0, 11.0, 12.0, 14.0, 16.0, 17.0, 19.0, 21.0, 24.0, 26.0,
                                28.0, 31.0, 34.0, 37.0, 40.0, 44.0, 47.0, 51.0, 55.0, 59.0, 64.0, 69.0,
                                74.0, 79.0, 85.0, 91.0, 98.0, 105.0, 112.0, 120.0, 128.0, 137.0, 146.0, 155.0,
                                166.0, 177.0, 188.0, 200.0
                        ))), "physical", 14.2, 1, 3.5, 3,
                        new CreateGroundAction("on_jagged_ground",
                                ExpressionCodec.of(4.0), ExpressionCodec.of(100.0))));
        reg(bootstrap, "red/forge_hammer",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                9.0, 10.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 26.0, 28.0, 31.0,
                                34.0, 37.0, 40.0, 44.0, 48.0, 52.0, 56.0, 60.0, 65.0, 70.0, 76.0, 82.0,
                                88.0, 94.0, 101.0, 108.0, 116.0, 124.0, 133.0, 142.0, 152.0, 162.0, 173.0, 184.0,
                                196.0, 209.0, 223.0, 237.0
                        ))), "fire", 15.4, 1, 3.8, 3));
        reg(bootstrap, "red/fortifying_cry",
                SkillPresets.read("generic_warcry")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(ofLevelTable(
                        List.of(
                                19.0, 22.0, 24.0, 27.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 49.0, 52.0,
                                56.0, 60.0, 64.0, 68.0, 72.0, 76.0, 80.0, 85.0, 90.0, 94.0, 99.0, 104.0,
                                110.0, 115.0, 120.0, 126.0, 132.0, 138.0, 144.0, 151.0, 157.0, 164.0, 171.0, 178.0,
                                185.0, 193.0, 201.0, 208.0
                        ))))
                        .vars("warcry_damage", ExpressionCodec.ofSkillLevel(15.4, 1, 3.8)));
        reg(bootstrap, "red/hammer_of_the_gods",
                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 23.0, 25.0, 28.0, 31.0, 34.0, 37.0,
                                41.0, 44.0, 48.0, 53.0, 57.0, 62.0, 67.0, 73.0, 78.0, 85.0, 91.0, 98.0,
                                105.0, 113.0, 121.0, 130.0, 139.0, 149.0, 160.0, 170.0, 182.0, 194.0, 207.0, 221.0,
                                236.0, 251.0, 267.0, 285.0
                        ))), "physical", 17.8, 1, 4.5, 3, stun(0.35)));
        reg(bootstrap, "red/infernal_cry",
                SkillPresets.read("generic_warcry_fire")
                        .require(GemGen.strReq()).cost(SkillFactory.mana(ofLevelTable(
                        List.of(
                                19.0, 22.0, 24.0, 27.0, 30.0, 33.0, 36.0, 39.0, 42.0, 46.0, 49.0, 52.0,
                                56.0, 60.0, 64.0, 68.0, 72.0, 76.0, 80.0, 85.0, 90.0, 94.0, 99.0, 104.0,
                                110.0, 115.0, 120.0, 126.0, 132.0, 138.0, 144.0, 151.0, 157.0, 164.0, 171.0, 178.0,
                                185.0, 193.0, 201.0, 208.0
                        ))))
                        .vars("warcry_damage", ofLevelTable(
                        List.of(
                                4.0, 5.5, 7.5, 10.0, 12.5, 15.5, 19.0, 22.5, 27.0, 32.0, 37.5, 44.0,
                                51.0, 59.0, 68.5, 79.5, 91.5, 105.5, 121.5, 139.5, 160.0, 184.0, 211.5, 242.5,
                                279.0, 320.5, 368.5, 423.5, 487.5, 561.5, 647.5, 746.5, 862.5, 996.5, 1153.5, 1336.0,
                                1549.0, 1799.0, 2091.5, 2435.0
                        ))));
        // 钢铁看守（Iron Ward）：自身增益（守护）。激活时获得闪避值与承受伤害减免。
        // 数值取自 POE2 真值（0.3）：+200~280 闪避、承受伤害减少 10~20%。
        // 经 Stat 引擎节点 ADD_EVASION / DAMAGE_TAKEN（均吃 $value）由 StatAction 注入 StatStore。
        List<StatEntry> ironWard = List.of(
                StatGen.ref(StatIds.ADD_EVASION, 280.0),
                StatGen.ref(StatIds.DAMAGE_TAKEN, -0.20));
        reg(bootstrap, "red/iron_ward",
                SkillFactory.selfStatBuff(GemGen.strReq(), SkillFactory.mana(7), ironWard, 200.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        // 熔岩壁垒（Magma Barrier）：自身增益（守护）。激活时获得「护甲按比例的额外闪避值」
        // （POE2 真值约 30% of Armour as Extra Evasion）。经 talentAttr 造「armour->evasion」节点。
        StatEntry magmaBarrier = StatGen.talentAttr(
                AttributeRegistry.EVASION, 0.30,
                Either.left(AttributeModifier.Operation.ADD_VALUE),
                Attributes.ARMOR, Set.of());
        reg(bootstrap, "red/magma_barrier",
                SkillFactory.selfStatBuff(GemGen.strReq(), SkillFactory.mana(7), List.of(magmaBarrier), 200.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        reg(bootstrap, "red/molten_blast",
                SkillFactory.projectileAttack(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                6.0, 6.0, 7.0, 9.0, 10.0, 11.0, 12.0, 14.0, 15.0, 17.0, 19.0, 20.0,
                                22.0, 25.0, 27.0, 29.0, 32.0, 34.0, 37.0, 40.0, 43.0, 47.0, 50.0, 54.0,
                                58.0, 63.0, 67.0, 72.0, 77.0, 83.0, 89.0, 95.0, 101.0, 108.0, 115.0, 123.0,
                                131.0, 140.0, 149.0, 158.0
                        ))), "fire", 13, 1, 3.2, SkillFactory.ignite(0.20)));
        // 拾荒镀甲（Scavenged Plating）：自身增益（守护）。激活时获得「能量护盾按比例的额外护甲」
        // （POE2 真值约 15% of Energy Shield as Extra Armour）。经 talentAttr 造「energy_shield->armour」节点。
        StatEntry scavengedPlating = StatGen.talentAttr(
                Attributes.ARMOR, 0.15,
                Either.left(AttributeModifier.Operation.ADD_VALUE),
                AttributeRegistry.MAX_ENERGY_SHIELD, Set.of());
        reg(bootstrap, "red/scavenged_plating",
                SkillFactory.selfStatBuff(GemGen.strReq(), SkillFactory.mana(7), List.of(scavengedPlating), 200.0).withTags(GateTag.BUFF, GateTag.PERSISTENT));
        reg(bootstrap, "red/shield_wall",
                SkillFactory.wall(GemGen.strReq(), SkillFactory.mana(ofLevelTable(
                        List.of(
                                13.0, 15.0, 18.0, 20.0, 23.0, 25.0, 28.0, 32.0, 35.0, 39.0, 43.0, 47.0,
                                51.0, 56.0, 61.0, 66.0, 72.0, 78.0, 85.0, 92.0, 99.0, 107.0, 115.0, 123.0,
                                133.0, 142.0, 153.0, 164.0, 175.0, 188.0, 201.0, 215.0, 229.0, 245.0, 261.0, 279.0,
                                297.0, 316.0, 337.0, 358.0
                        ))), "physical", ExpressionCodec.ofSkillLevel(14.2, 1.0, 3.5), "wall"));
        // <<<AUTO_END>>>
    
    }

    /** 斩杀修正：目标生命比例低于 threshold 时，本次命中造成的对应伤害变为致命。 */
    private static EffectMod executeMod(double threshold) {
        return new EffectMod(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                false, Optional.empty(), Optional.empty(),
                Optional.of(ExpressionCodec.of(threshold)),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 猛击（Slam）技能集合 — 数据驱动的「slam」标签标记，供余震(aftershock)生产者识别。
     *  属可审计的策划数据，按需增删即可调整余震触发范围。 */
    private static final Set<String> SLAM_SKILLS = Set.of(
            "red/rolling_slam", "red/leap_slam", "red/ground_slam", "red/perfect_strike",
            "red/sunder", "red/earthshatter", "red/boneshatter", "red/volcanic_fissure",
            "red/shockwave", "red/rupture", "red/stampede", "red/execute", "red/earthquake");

    private static void reg(BootstrapContext<SkillEntry> bootstrap, String path, SkillEntry skill) {
        if (SLAM_SKILLS.contains(path)) {
            skill = skill.withTags("slam");
        }
        // Task #377 客户端效果匹配：未显式声明声画身份的技能，按配色/家族注入主题化施放音效+粒子。
        if (skill instanceof Skill s && (s.castSound().isEmpty() || s.castParticle().isEmpty())) {
            skill = SkillFactory.withDefaultCastFx(path, s);
        }
        // 技能皮肤（#CoE）：在 toRead 之前抓取「当前底层渲染 id」并记录，再把投递外观引用覆盖为技能 id。
        // 仅当确实录得底层渲染才覆盖：录不到却覆盖会让投递指向技能 id，而该 id 在
        // coe:entity_render 查不到 → 实体不可见，且皮肤为空也还原不回来。
        // 仅对仍为内联 Skill 的形态生效（SkillRead 形态的投递在预设内，此处不可见、跳过）。
        if (skill instanceof Skill inline
                && SkillSkinDefs.recordRender(SkillIds.id(path), inline)) {
            skill = inline.withDeliveryRender(SkillIds.id(path));
        }
        // 数据驱动迁移（#87）：内联技能 → 预设 + SkillRead（发射 Read 标签 preset_<gem>）。
        // 先打 slam（属技能定义本身），再转 Read，使预设即宝石的完整定义。
        skill = toRead(path, skill);
        // 宝石本体路径经 SkillIds 登记（生成期按路径生成），使其他位置可 SkillIds + SkillRead 引用本技能。
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_KEY, SkillIds.id(path)),
                skill.withTier(TIER.getOrDefault(path, 1)));
    }

    /** 内联完整技能 → {@code coe:preset_<gem>} 预设 + {@code SkillRead} 引用；已是 read 形态则原样返回。 */
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
            "red/leap_slam", 7,
            "red/volcanic_fissure", 7,
            "red/sunder", 9,
            "red/rupture", 3,
            "red/perfect_strike", 5,
            "red/stampede", 11,
            "red/shockwave_totem", 5,
            "red/ancestral_warrior_totem", 3,
            "red/war_banner", 4,
            "red/seismic_cry", 11
    );
}
