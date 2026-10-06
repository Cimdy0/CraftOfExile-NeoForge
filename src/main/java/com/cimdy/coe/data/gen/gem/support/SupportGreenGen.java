package com.cimdy.coe.data.gen.gem.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;

/**
 * 绿色（敏捷）辅助宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 * 运行时修正（effects）与技能自身 modifiers 共用同一套 SkillModifier 形状。
 */
public class SupportGreenGen {
    public static void register(BootstrapContext<SupportGem> bootstrap) {
    registerPart1(bootstrap);
    registerPart2(bootstrap);
}

    private static void registerPart1(BootstrapContext<SupportGem> bootstrap) {

        // 多重投射 Martial Projectiles：攻击额外发射 +2 投射物。
        // poe2wiki: Supported Attacks fire +2 Projectiles；+5 敏捷；消耗 130%。
        reg(bootstrap, "green/martial_projectiles",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "martial_projectiles",
                        List.of(SupportGemFactory.projectile("additional_projectiles", 2)),
                        null, 1.3, 20, false));

        // 分叉 Fork：投射物命中后分裂，且分叉后伤害 -30%（splitMultiplier 近似作用于分裂投射物）。
        // poe2wiki: Projectiles fork + 30% less Damage after Forking；+5 敏捷；消耗 130%。
        reg(bootstrap, "green/fork",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "fork",
                        List.of(SupportGemFactory.projectile("fork_count", 1),
                                SupportGemFactory.splitMultiplier(0.7)),
                        null, 1.3, 20, false));
        // 连锁 Chain：投射物额外连锁，且命中伤害 -30%。
        // poe2wiki: Supported Skills Chain +2 times / 30% less Damage with Hits；+5 敏捷；消耗 130%。
        reg(bootstrap, "green/chain",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "chain",
                        List.of(SupportGemFactory.projectile("chain_count", 2),
                                SupportGemFactory.less(0.7)),
                        null, 1.3, 20, false));
        // <<<AUTO_SUPPORTS_START>>>
        reg(bootstrap, "green/adhesive_grenades",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "adhesive_grenades",
                        List.of(
                                SupportGemFactory.less(0.9)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/alignment",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "alignment",
                        List.of(
                                SupportGemFactory.more("projectile", 1.3)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/blindside",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "blindside",
                        List.of(
                                // ⚠️ 语义越界（机制缺口，未修）：权威（SupportLang）两条均为【对盲目的敌人】才生效——
                                //   「15% more Critical Damage Bonus with Hits against Blinded Enemies」
                                //   「15% more chance to Critically Hit Blinded Enemies」
                                //   另有一条「cannot inflict Blind」。
                                // 当前 EffectMod 只有 hit/crit 两种 condition，无 blinded 条件通道，
                                // 故此处退化为【无条件全局 +15% 暴击伤害】。正数値对，作用域偏宽（连非盲目标也吃到）。
                                SupportGemFactory.critDamage(1.15)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/bursting_plague",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "bursting_plague",
                        List.of(
                                SupportGemFactory.areaAoe(2.0),
                                SupportGemFactory.critChance(1.05)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/close_combat",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "close_combat",
                        List.of(
                                SupportGemFactory.more("attack", 1.2)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/deadly_poison",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "deadly_poison",
                        List.of(
                                SupportGemFactory.less(0.75)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/garukhans_resolve",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "garukhans_resolve",
                        List.of(
                                // ⚠️ 语义错误（机制缺口，未修）：权威（SupportLang）是
                                //   「Max Critical Hit chance with Supported Skills is 50%」+
                                //   「Attack Critical Hit Chance with Supported Skills Bifurcates」
                                // —— 前者是【暴击率上限设定 50%】，后者是【掷两次取优】，都不是 +50% 暴击率。
                                // 当前以 critChance(1.5) 表达，会无条件放大 1.5 倍，与权威语义完全不同。
                                // 修正需 max_crit_chance 上限通道 + bifurcate（双重掷骰）机制，暂保持并标注。
                                SupportGemFactory.critChance(1.5)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/longshot",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "longshot",
                        List.of(
                                SupportGemFactory.more("attack", 1.25)
                        ),
                        null, 1.2, 1.2, 20, false));
    }

    private static void registerPart2(BootstrapContext<SupportGem> bootstrap) {

        // 多重射击（权威：SupportLang「Multishot」）
        //   Supported Skills fire 2 additional Projectiles   ← 本轮补（projectile 通道）
        //   Supported Skills deal 35% less Damage            ← less(0.65)
        //   Supported Skills have 20% less Skill Speed       ← 本轮补（skillSpeed 通道）
        reg(bootstrap, "green/multishot",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "multishot",
                        List.of(
                                SupportGemFactory.projectile("additional_projectiles", 2),
                                SupportGemFactory.less(0.65)
                        ),
                        SupportGemFactory.skillSpeed(0.8), 1.0, 1.0, 20, false));
        reg(bootstrap, "green/murderous_intent",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "murderous_intent",
                        List.of(
                                // ⚠️ 语义越界（机制缺口，未修）：权威（SupportLang）是
                                //   【Skills Empowered by Supported Skills】 have Culling Strike
                                // ——斩杀应作用于「被本技能强化（Empowered）的那些技能」，而非本技能自身。
                                // 当前无 Empowered 传递通道，故退化为本技能自身带斩杀。阈值 10% 与权威一致。
                                SupportGemFactory.culling(0.10)
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/overextend",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "overextend",
                        List.of(
                                SupportGemFactory.critDamage(1.3)
                        ),
                        null, 1.15, 1.15, 20, false));
        reg(bootstrap, "green/overreach",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "overreach",
                        List.of(
                                SupportGemFactory.areaAoe(1.4)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/pierce",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "pierce",
                        List.of(
                                SupportGemFactory.projectile("pierce_count", 1)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/pursuit",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "pursuit",
                        List.of(
                                SupportGemFactory.more("melee", 1.2),
                                SupportGemFactory.suppress("projectile")
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/retreat",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "retreat",
                        List.of(
                                SupportGemFactory.more("projectile", 1.2),
                                SupportGemFactory.suppress("melee")
                        ),
                        null, 1.2, 1.2, 20, false));
        reg(bootstrap, "green/rigwalds_ferocity",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "rigwalds_ferocity",
                        List.of(
                                SupportGemFactory.less(0.85)
                        ),
                        null, 1.0, 1.0, 20, false));
        reg(bootstrap, "green/spectral_volley",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "spectral_volley",
                        List.of(
                                SupportGemFactory.less(0.4)
                        ),
                        null, 1.2, 1.2, 20, false));
        // 流线弹药（权威：SupportLang「Streamlined Rounds」）
        //   Supported Skills load 5 additional bolts       ← 待装填重构（NO_PW 弩 ×1，暂缓）
        //   Supported Skills have 50% more Attack speed    ← 本轮补（SpecialMod#attackSpeed）
        //   Supported Skills deal 60% less Damage          ← 已有 less(0.4)
        //   Supported Skills have 50% less reload speed    ← 待装填重构（NO_PW 弩 ×1，暂缓）
        // 注：装填（load / reload speed）耦合待定的装填重构，攻击速度与伤害两条互不依赖，先行落地。
        reg(bootstrap, "green/streamlined_rounds",
                SupportGemFactory.of(GemGen.supportReq(0, 5, 0), "streamlined_rounds",
                        List.of(
                                SupportGemFactory.less(0.4)
                        ),
                        SupportGemFactory.attackSpeed(1.5), 1.2, 1.2, 20, false));
        // <<<AUTO_SUPPORTS_END>>>
    
    }

    private static void reg(BootstrapContext<SupportGem> bootstrap, String path, SupportGem gem) {
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_SUPPORT_GEM_KEY, GemGen.id(path)),
                gem.withTier(TIER.getOrDefault(path, 1)));
    }

    /** 等阶（原 GemTiers 数据，就近定义于辅助注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "green/fork", 3
    );
}
