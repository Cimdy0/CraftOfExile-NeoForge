package com.cimdy.coe.data.gen.gem.support;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Penetration;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;

/**
 * 蓝色（智慧）辅助宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 * 运行时修正（effects）与技能自身 modifiers 共用同一套 SkillModifier 形状。
 */
public class SupportBlueGen {
    public static void register(BootstrapContext<SupportGem> bootstrap) {
    registerPart1(bootstrap);
    registerPart2(bootstrap);
    registerPart3(bootstrap);
}

    private static void registerPart1(BootstrapContext<SupportGem> bootstrap) {

        // 附加闪电伤害 Added Lightning Damage：为链接技能附加 1~8 闪电伤害（POE2 1 级基准值）。
        // poe2wiki: Supported Skills have 1 to 8 added Lightning Damage (lvl 1)；+5 智慧；消耗 130%。
        reg(bootstrap, "blue/added_lightning_damage",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "added_lightning_damage",
                        List.of(SupportGemFactory.addedDamage("lightning",
                                ExpressionCodec.ofSkillLevel(1, 0.35, 0),
                                ExpressionCodec.ofSkillLevel(8, 2.6, 0))),
                        null, 1.3, 20, false));
        // 附加火焰伤害 Added Fire Damage：附加 1~8 火焰伤害（POE2 1 级基准值）。
        reg(bootstrap, "blue/added_fire_damage",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "added_fire_damage",
                        List.of(SupportGemFactory.addedDamage("fire",
                                ExpressionCodec.ofSkillLevel(1, 0.35, 0),
                                ExpressionCodec.ofSkillLevel(8, 2.6, 0))),
                        null, 1.3, 20, false));
        // 附加冰冷伤害 Added Cold Damage：附加 1~8 冰冷伤害（POE2 1 级基准值）。
        reg(bootstrap, "blue/added_cold_damage",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "added_cold_damage",
                        List.of(SupportGemFactory.addedDamage("cold",
                                ExpressionCodec.ofSkillLevel(1, 0.35, 0),
                                ExpressionCodec.ofSkillLevel(8, 2.6, 0))),
                        null, 1.3, 20, false));
        // 附加混沌伤害 Added Chaos Damage：附加 1~8 混沌伤害（POE2 1 级基准值）。
        reg(bootstrap, "blue/added_chaos_damage",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "added_chaos_damage",
                        List.of(SupportGemFactory.addedDamage("chaos",
                                ExpressionCodec.ofSkillLevel(1, 0.35, 0),
                                ExpressionCodec.ofSkillLevel(8, 2.6, 0))),
                        null, 1.3, 20, false));

        // 火焰穿透 Fire Penetration：命中穿透 30% 火焰抗性（POE2 基准值）。
        // poe2wiki: Supported Skills Penetrate 30% Fire Resistance；+5 智慧；消耗 130%。
        reg(bootstrap, "blue/fire_penetration",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "fire_penetration",
                        List.of(SupportGemFactory.penetration("fire", 0.30)),
                        null, 1.3, 20, false));
        // 冰冷穿透 Cold Penetration：命中穿透 30% 冰冷抗性。
        reg(bootstrap, "blue/cold_penetration",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "cold_penetration",
                        List.of(SupportGemFactory.penetration("cold", 0.30)),
                        null, 1.3, 20, false));
        // 闪电穿透 Lightning Penetration：命中穿透 30% 闪电抗性。
        reg(bootstrap, "blue/lightning_penetration",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "lightning_penetration",
                        List.of(SupportGemFactory.penetration("lightning", 0.30)),
                        null, 1.3, 20, false));

        // 元素曝露类：命中后压低目标对应元素抗性 20%，持续 8 秒（160 tick）。
        // POE2 中 Exposure 约 -20% 对应抗性；作为通用「曝露」辅助落地。
        reg(bootstrap, "blue/scorching_exposure",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "scorching_exposure",
                        List.of(SupportGemFactory.exposure("fire", 0.20, 160)),
                        null, 1.3, 20, false));
        reg(bootstrap, "blue/biting_exposure",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "biting_exposure",
                        List.of(SupportGemFactory.exposure("cold", 0.20, 160)),
                        null, 1.3, 20, false));
        reg(bootstrap, "blue/conductive_exposure",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "conductive_exposure",
                        List.of(SupportGemFactory.exposure("lightning", 0.20, 160)),
                        null, 1.3, 20, false));

        // 增幅效果 Magnified Effect：范围类技能 +30% 范围效果。
        // poe2wiki: Supported Skills have 30% more Area of Effect；+5 智慧；消耗 130%。

        // 终结 Culling Strike：目标生命低于阈值时该次伤害致命。
        // poe2wiki 实际为分级斩杀（普通 35% / 魔法 20% / 稀有 10% / 传奇 5%），本模型仅单阈值，
        // 取稀有怪阈值 10%（最常见的「斩杀」参考值）；+5 智慧；消耗 130%。
        reg(bootstrap, "blue/culling_strike",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "culling_strike",
                        List.of(SupportGemFactory.culling(0.10)),
                        null, 1.3, 20, false));
        // <<<AUTO_SUPPORTS_START>>>
        reg(bootstrap, "blue/ambush",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "ambush",
                        List.of(
                                SupportGemFactory.critChance(2.0)
                        ),
                        null, 1.0, 1.0, 20, false));
                // 本轮修正：权威「Supported Skills have 25% less Area of Effect」→ 0.75；旧值 1.25 方向反了（把减益做成增益）。
        reg(bootstrap, "blue/astral_projection",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "astral_projection",
                        List.of(
                                SupportGemFactory.areaAoe(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
    }

    private static void registerPart2(BootstrapContext<SupportGem> bootstrap) {

                // 本轮修正：权威「Supported Skills have 50% less Area of Effect」→ 0.5；旧值 1.5 方向反了（把减益做成增益）。
        reg(bootstrap, "blue/concentrated_area",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "concentrated_area",
                        List.of(
                                SupportGemFactory.areaAoe(0.5),
                                SupportGemFactory.more("area", 1.3)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/elemental_focus",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "elemental_focus",
                        List.of(
                                SupportGemFactory.more("elemental", 1.25)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/encroaching_ground",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "encroaching_ground",
                        List.of(
                                SupportGemFactory.areaAoe(1.2)
                        ),
                        null, 1.1, 1.1, 20, false));
        reg(bootstrap, "blue/expand",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "expand",
                        List.of(
                                SupportGemFactory.areaAoe(1.3),
                                // 封印（Unleash 系）：maxSeals=5、攒印 gain=50、释放 +30% 范围/封印。
                                // 数值 provisional，待平衡。
                                SupportGemFactory.sealGem(5, 50, 1, 0.30)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "blue/expanse",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "expanse",
                        List.of(
                                SupportGemFactory.areaAoe(1.5)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/fortress",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "fortress",
                        List.of(
                                SupportGemFactory.less(0.6)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/freezefork",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "freezefork",
                        List.of(
                                SupportGemFactory.projectile("fork_count", 1),
                                SupportGemFactory.splitMultiplier(0.7)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/gambleshot",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "gambleshot",
                        List.of(
                                // 权威（SupportLang）：Fork +「Projectiles from Supported Skills deal 15% less Damage」。
                                // splitMultiplier(0.7) 是 Fork 分裂惩罚（与 fork / freezefork 同族），
                                // 而 15% less 是独立于 Fork 的全局投射物减益，本轮补上（走 delivery 作用域真读）。
                                SupportGemFactory.projectile("fork_count", 1),
                                SupportGemFactory.splitMultiplier(0.7),
                                SupportGemFactory.moreDelivery("projectile", 0.85)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/inexorable_critical",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "inexorable_critical",
                        List.of(
                                SupportGemFactory.critChance(1.1)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "blue/ixchels_torment",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "ixchels_torment",
                        List.of(
                                SupportGemFactory.areaAoe(1.4)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "blue/magnified_area",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "magnified_area",
                        List.of(
                                SupportGemFactory.areaAoe(1.35)
                        ),
                        null, 1.3, 1.3, 20, false));
                // 本轮修正：权威「Supported Skills have 25% less area of effect」→ 0.75；旧值 1.25 方向反了（把减益做成增益）。
        reg(bootstrap, "blue/morganas_tempest",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "morganas_tempest",
                        List.of(
                                SupportGemFactory.areaAoe(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/pinpoint_critical",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "pinpoint_critical",
                        List.of(
                                // 权威（SupportLang）：60% more Critical Hit Chance → 1.6
                                SupportGemFactory.critChance(1.6),
                                // 本轮修正：权威「30% less Critical Damage Bonus」→ 0.7；
                                //   旧值 1.3 方向反了（把减益做成增益）。
                                SupportGemFactory.critDamage(0.7)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/profanity",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "profanity",
                        List.of(
                                SupportGemFactory.more("chaos", 1.3)
                        ),
                        null, 1.0, 1.0, 20, false));
    }

    private static void registerPart3(BootstrapContext<SupportGem> bootstrap) {

        reg(bootstrap, "blue/rising_tempest",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "rising_tempest",
                        List.of(
                                SupportGemFactory.more("elemental", 1.12)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "blue/ritualistic_curse",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "ritualistic_curse",
                        List.of(
                                SupportGemFactory.areaAoe(1.5)
                        ),
                        null, 1.0, 1.0, 20, false));
                // 本轮修正：权威「Supported Skills have 20% less Area of Effect」→ 0.8；
        //   旧值两个 areaAoe(1.2) 方向反了（1.2 是 +20%），且重复两项会叠乘成 1.44。
        //   另两条（Cascade 到两侧 / Cascades 20% less AoE）需级联机制，未建模。
        reg(bootstrap, "blue/spell_cascade",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "spell_cascade",
                        List.of(
                                SupportGemFactory.areaAoe(0.8),
                                SupportGemFactory.less(0.7)
                        ),
                        null, 1.0, 1.0, 20, false));
        // 法术回响（权威：poe2db「Spell Echo」）
        //   Supported Spells Echo 1 times
        //   Supported Skills have 30% less Cast Speed
        //   Echoes from Supported Spells have 40% increased Area of Effect
        // 延迟取 support_spell_echo_cascade_delay_ms = 500 → 10 tick。
        //
        // 旧建模为 areaAoe(1.4)——把「回响的那次 +40% 范围」错误地做成【整技能 +40% 范围】
        // （连原施放都吃到，语义越界）。回响的范围加成改由回响实例自带的 echo 标签作用域承载
        // （GateTag.ECHO，发射点 SkillStatScopes#enterEcho），只对重放出来的那一次生效。
        //
        // 「30% less Cast Speed」走 SpecialMod#castSpeedMultiplier 装配期通道（无需新增读点）：
        // SkillLinkResolver#applyAll → getTotalSpeedMultiplier → SkillCast#withSpeedMultiplier(0.7)
        // → getCastTime = base / 0.7（施法时间 ≈ 1.43 倍）。该惩罚作用于【被辅助技能整体】，
        // 而非仅回响那一次——回响本身是原施放的一部分，本就不额外占用施法时间。
        reg(bootstrap, "blue/spell_echo",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "spell_echo",
                        List.of(
                                SupportGemFactory.echoTag(1, 10, "spell", 0.40, 0.0)
                        ),
                        SupportGemFactory.castSpeed(0.7), 1.5, 1.5, 20, false));
        reg(bootstrap, "blue/supercritical",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "supercritical",
                        List.of(
                                // 权威（SupportLang）：100% increased Critical Damage Bonus → 2.0
                                SupportGemFactory.critDamage(2.0),
                                // 本轮修正：权威「20% less Critical Hit chance」→ 0.8；
                                //   旧值 1.2 方向反了（把减益做成增益）。
                                SupportGemFactory.critChance(0.8)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "blue/unleash",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "unleash",
                        List.of(
                                SupportGemFactory.less(0.5),
                                // 封印（Unleash）：maxSeals=3、攒印 gain=200、释放每封印重放 1 次。
                                // 数值 provisional，待平衡。
                                SupportGemFactory.sealGem(3, 200, 1, 0.0)
                        ),
                        null, 1.5, 1.5, 20, false));
        reg(bootstrap, "blue/withering_touch",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "withering_touch",
                        List.of(
                                SupportGemFactory.less(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
        // 泽洛克的克制（权威：poe2db「Zarokh's Refrain」，0.4.0 起）
        //   Supported Spells Echo 3 times（0.4.0 前为 2 次）
        //   Echoes from Supported Spells have 40% increased Area of Effect
        // 与 spell_echo 同族：延迟同为 500ms = 10 tick、回响范围同为 +40% increased。
        // 旧建模的 areaAoe(1.4) 属「回响范围加成被误做成全局范围加成」，已改回声专属载荷。
        // 注：官方另有「给被辅助技能 5 秒冷却」，属冷却机制，未建模（非回响范畴）。
        reg(bootstrap, "blue/zarokhs_refrain",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "zarokhs_refrain",
                        List.of(
                                SupportGemFactory.echoTag(3, 10, "spell", 0.40, 0.0)
                        ),
                        null, 1.3, 1.3, 20, false));
        reg(bootstrap, "blue/zarokhs_revolt",
                SupportGemFactory.of(GemGen.supportReq(0, 0, 5), "zarokhs_revolt",
                        List.of(
                                SupportGemFactory.less(0.8),
                                // 封印（Zarokh's Revolt）：maxSeals=2、攒印 gain=200、释放每封印重放 1 次。
                                // 数值 provisional，待平衡。
                                SupportGemFactory.sealGem(2, 200, 1, 0.0)
                        ),
                        null, 2.0, 2.0, 20, false));
        // <<<AUTO_SUPPORTS_END>>>

        // ---- 元宝石（Cast on X，P2-3）：为被辅助技能注入事件监听，条件满足时触发链接技能 ----
        // 暴击时施放：默认触发混沌箭（事件名统一为 crit；POE2 能量：3 次暴击满能量触发一次）
        reg(bootstrap, "blue/cast_on_crit",
                SupportGemFactory.castOn(GemGen.supportReq(0, 0, 5), "cast_on_crit",
                        EventName.CRIT, List.of("coe:blue/chaos_bolt"),
                        0.5, 1, 1.3, 20, 3));
        // 点燃时施放：默认触发震波图腾（POE2 能量：3 次点燃满能量触发一次）
        // 收敛：异常具体类型已是标签，事件名统一为 caused_ailment，靠 tag [ignite] 精确过滤
        reg(bootstrap, "blue/cast_on_ignite",
                SupportGemFactory.castOn(GemGen.supportReq(0, 0, 5), "cast_on_ignite",
                        EventName.CAUSED_AILMENT, List.of("ignite"), List.of("coe:red/shockwave_totem"),
                        0.5, 1, 1.3, 20, 3));
        // 感电时施放：默认触发唤起僵尸（POE2 能量：3 次感电满能量触发一次）
        reg(bootstrap, "blue/cast_on_shock",
                SupportGemFactory.castOn(GemGen.supportReq(0, 0, 5), "cast_on_shock",
                        EventName.CAUSED_AILMENT, List.of("shock"), List.of("coe:blue/raise_zombie"),
                        0.5, 1, 1.3, 20, 3));
        // 仲裁者的燃灼（Arbiter's Ignition，Lineage 辅助，POE2 实证）：用被辅助技能点燃敌人时获得 Elemental Archon。
        // 数据驱动渠道：监听 caused_ailment(tag=ignite) → StatAction 经中央引擎授予 archon（互斥/恢复由引擎按标签判定），
        // 目标为施法者自身（SELF）。其余渠道（天赋/升华）以同型数据定义即可，无需改代码。
        reg(bootstrap, "blue/arbiter_ignition",
                SupportGemFactory.gainArchonOnAilment(GemGen.supportReq(0, 0, 5), "arbiter_ignition",
                        StatusDefs.ELEMENTAL_ARCHON, List.of("ignite"),
                        0.5, 1, 1.3, 20));
    
    }

    private static void reg(BootstrapContext<SupportGem> bootstrap, String path, SupportGem gem) {
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_SUPPORT_GEM_KEY, GemGen.id(path)),
                gem.withTier(TIER.getOrDefault(path, 1)));
    }

    /** 等阶（原 GemTiers 数据，就近定义于辅助注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "blue/culling_strike", 4
    );
}
