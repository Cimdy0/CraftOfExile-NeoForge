package com.cimdy.coe.data.gen.gem.support;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;

/**
 * 红色（力量）辅助宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 * 运行时修正（effects）与技能自身 modifiers 共用同一套 SkillModifier 形状。
 */
public class SupportRedGen {
    public static void register(BootstrapContext<SupportGem> bootstrap) {
        // 残暴 Brutality：+25% 物理伤害，且无法造成元素伤害（对每个元素类型 suppress）。
        // poe2wiki: Supported Skills deal 25% more Physical Damage / no Elemental Damage；+5 力量；消耗 130%。
        reg(bootstrap, "red/brutality",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "brutality",
                        concat(SupportGemFactory.more("physical", 1.25),
                                SupportGemFactory.suppressElemental()),
                        null, 1.3, 20, false));
        // 火焰灌注 Fire Infusion：将伤害转为火焰，并 +25% 火焰伤害（0.3 Infusion 机制）。
        // poe2wiki: 转为火焰 + 更多火焰伤害；+5 力量；消耗 130%。
        reg(bootstrap, "red/fire_infusion",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "fire_infusion",
                        List.of(SupportGemFactory.convert("physical", "fire", 1.0),
                                SupportGemFactory.more("fire", 1.25)),
                        null, 1.3, 20, false));
        // 沉重挥击 Heavy Swing：+30% 命中伤害（POE2 另有 30% less 攻击速度，本模型未建模攻击速度，仅取增伤）。
        // poe2wiki: 30% more Damage with Hits；+5 力量；消耗 130%。
        reg(bootstrap, "red/heavy_swing",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "heavy_swing",
                        List.of(SupportGemFactory.more(1.30)),
                        null, 1.3, 20, false));
        // <<<AUTO_SUPPORTS_START>>>
        reg(bootstrap, "red/battershout",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "battershout",
                        List.of(
                                SupportGemFactory.critChance(1.05)
                        ),
                        null, 1.3, 1.3, 20, false));
                // 本轮修正：权威「Branched Fissures have 35% less area of effect」→ 0.65；旧值 1.35 方向反了（把减益做成增益）。
        reg(bootstrap, "red/branching_fissures",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "branching_fissures",
                        List.of(
                                SupportGemFactory.areaAoe(0.65)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "red/cirels_cultivation",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "cirels_cultivation",
                        List.of(
                                SupportGemFactory.areaAoe(1.05)
                        ),
                        null, 1.15, 1.15, 20, false));
        reg(bootstrap, "red/clash",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "clash",
                        List.of(
                                SupportGemFactory.more("melee", 1.4)
                        ),
                        null, 1.2, 1.2, 20, false));
                // 本轮修正：权威「Supported Warcries have 30% less Area of Effect」→ 0.7；旧值 1.3 方向反了（把减益做成增益）。
        reg(bootstrap, "red/corrupting_cry",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "corrupting_cry",
                        List.of(
                                SupportGemFactory.areaAoe(0.7)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "red/deep_cuts",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "deep_cuts",
                        List.of(
                                SupportGemFactory.less(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
        // 回响战吼：被辅助技能回响 2 次，间隔 1.3 秒 = 26 tick（POE2 官方数值）。
        // 旧建模用 areaAoe(1.5)+areaAoe(2.0) 硬凑「每次回响 +100% 范围」——既把加成施加到原施放，
        // 也无法表达「第 1 次回响 ×2、第 2 次回响 ×3」的递增。现改为真回响；范围递减（50% less）
        // 与伤害递减（40% less）是「被辅助技能」的全局修正，照旧保留。
        // 回响战吼（权威：poe2db「Echoing Cry」）
        //   Supported Skills Echo 2 times, with a 1.3 second delay      → 26 tick
        //   Supported Skills have 50% less Area of Effect               → areaAoe(0.5)（全局，含原施放）
        //   Supported Skills deal 40% less Damage                       → less(0.6)
        //   Supported Skills have 100% more area of effect per time they have Echoed
        //       → areaMorePerEcho = 1.0：第 1 次回响 ×2、第 2 次 ×3，原施放（已回响 0 次）×1。
        //         这是「按回响序号递增」的乘法项，故走 EchoMod 载荷而非全局倍率；
        //         旧建模用 areaAoe(1.5)+areaAoe(2.0) 相乘硬凑，既施加到原施放、也表达不了递增。
        //   未建模（待新机制）：「count enemies as having 50% less Power」、
        //                    「become usable after you have moved 10 metres」使用条件。
        reg(bootstrap, "red/echoing_cry",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "echoing_cry",
                        List.of(
                                SupportGemFactory.echoSkill(2, 26, 0.0, 1.0),
                                SupportGemFactory.areaAoe(0.5),
                                SupportGemFactory.less(0.6)
                        ),
                        null, 1.5, 1.5, 20, false));
        // 卡奥姆的疯狂（权威：SupportLang「Kaoms Madness」）
        //   Supported Skills create 3 additional fissures      ← 需裂隙机制，未建模
        //   Supported Skills deal 20% less Damage              ← less(0.8)
        //   Supported Skills have 20% less Skill Speed         ← 本轮补（skillSpeed 通道）
        //   Supported Skills have 30% less Area of Effect      ← 本轮修正：旧值 areaAoe(1.3) 方向反了
        //     （权威为 30% less，应为 0.7；1.3 是 +30%，把减益做成了增益）
        reg(bootstrap, "red/kaoms_madness",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "kaoms_madness",
                        List.of(
                                SupportGemFactory.less(0.8),
                                SupportGemFactory.areaAoe(0.7)
                        ),
                        SupportGemFactory.skillSpeed(0.8), 1.0, 1.0, 20, false));
        reg(bootstrap, "red/searing_flame",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "searing_flame",
                        List.of(
                                SupportGemFactory.less(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "red/splinter_totem",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "splinter_totem",
                        List.of(
                                SupportGemFactory.areaAoe(1.2)
                        ),
                        null, 1.2, 1.2, 20, false));
        // 构造板块（权威：SupportLang「Tectonic Slams」）
        //   Supported Skills cause 1 additional Aftershocks ahead and to each side of the initial area
        //   Supported Skills deal 45% less damage with Aftershocks
        //   Supported Skills have 30% less Aftershock area of effect
        //   Supported Skills have 15% less Attack Speed                 ← 本轮补（SpecialMod#attackSpeed）
        //
        // 已知偏差（本轮不修，待核定）：上面第 2、3 条权威语义只作用于【余震】，
        // 现建模却按【主技能】近似施加——less(0.55) 扣的是主技能伤害；
        // 且 areaAoe(1.3) 方向是反的（权威为 30% less，应为 0.7，现为 +30%）。
        // 余震已有独立属性通道（AFTERSHOCK_AREA / AFTERSHOCK_CHANCE，AreaDeliveryExecutor:130-153 真读），
        // 正确落点应走属性而非全局 modifier——需宝石侧属性注入能力，超出本轮范围。
        reg(bootstrap, "red/tectonic_slams",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "tectonic_slams",
                        List.of(
                                SupportGemFactory.less(0.55),
                                SupportGemFactory.areaAoe(1.3)
                        ),
                        SupportGemFactory.attackSpeed(0.85), 1.3, 1.3, 20, false));
        // 隆升（权威：SupportLang「Upheaval」）
        //   Supported Skills create 1 additional fissures   ← 需裂隙机制，未建模
        //   Supported Skills deal 20% less Damage           ← less(0.8)
        //   Supported Skills have 20% less Skill Speed      ← 本轮补（skillSpeed 通道）
        reg(bootstrap, "red/upheaval",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "upheaval",
                        List.of(
                                SupportGemFactory.less(0.8)
                        ),
                        SupportGemFactory.skillSpeed(0.8), 1.0, 1.0, 20, false));
        reg(bootstrap, "red/vruuns_inevitability",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "vruuns_inevitability",
                        List.of(
                                SupportGemFactory.areaAoe(1.06)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "red/zerphis_infamy",
                SupportGemFactory.of(GemGen.supportReq(5, 0, 0), "zerphis_infamy",
                        List.of(
                                // 权威（SupportLang）：50% increased Area of Effect + 50% more Damage。
                                // 另两条（requires 50 Glory / gain Glory = 5% of Life spent）需 Glory 机制，未建模。
                                SupportGemFactory.areaAoe(1.5),
                                SupportGemFactory.more(1.5)
                        ),
                        null, 1.0, 1.0, 20, false));
        // <<<AUTO_SUPPORTS_END>>>
    }

    private static List<SkillModifier> concat(SkillModifier a, List<? extends SkillModifier> b) {
        var list = new java.util.ArrayList<SkillModifier>();
        list.add(a);
        list.addAll(b);
        return list;
    }

    private static void reg(BootstrapContext<SupportGem> bootstrap, String path, SupportGem gem) {
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_SUPPORT_GEM_KEY, GemGen.id(path)),
                gem.withTier(TIER.getOrDefault(path, 1)));
    }

    /** 等阶（原 GemTiers 数据，就近定义于辅助注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "red/heavy_swing", 4
    );
}
