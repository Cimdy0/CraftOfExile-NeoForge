package com.cimdy.coe.data.gen.gem.spirit;
import com.cimdy.coe.data.gen.stat.StatConditionIds;

import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.stat.effect.grant.MultiStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.mojang.datafixers.util.Either;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.gem.skill.SkillPresets;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import java.util.Map;

/**
 * 红色（力量）精魂宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 * 精魂宝石本质是预留精魂的光环，增益通过自施放属性修改器落地。
 *
 * <p>数据驱动迁移（#87）：每颗宝石的完整 Skill 经 {@link SkillPresets#registerPreset} 登记为
 * {@code coe:preset_<gem>} 预设，宝石本体降为 {@code SkillRead(ref=preset_<gem>)} 引用之——
 * 运行时由 {@link com.cimdy.coe.data.codec.skill.core.SkillRead#resolve()} 发射 Read 标签
 * （{@code preset_<gem>}），使数据侧「对 [某预设] 类技能」加成可命中。光环/捷的数值（固定属性修改器）
 * 无法走 {@code $槽}（SpiritManager 重放通道为 null SkillContext），故烘焙进预设。
 */
public class SpiritRedGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
        // 灰烬之捷 Herald of Ash：30 精魂。poe2wiki 真值（Buff, Persistent, AoE, Fire, Duration, Herald）：
        // "While active, enemies you sufficiently Overkill with non-Herald Attacks will cause an explosion that
        //  Ignites nearby enemies based on the Overkill damage dealt."（过度击杀 ≥20% 最大生命；爆炸半径 1m）
        // **无常驻增益** —— 效果全在「过度击杀触发」，故不挂常驻属性修改器。
        reg(bootstrap, "red/herald_of_ash",
                // poe2db 真值标签：Buff, Persistent, AoE, Fire, Duration, Herald
                SpiritGemFactory.herald(GemGen.strReq(), SpiritGemFactory.spirit(30),
                        StatusDefs.HERALD_OF_ASH,
                        SpiritGemFactory.heraldProcPhase("fire", 12.0, 2.5),
                        java.util.List.of(GateTag.AURA, GateTag.HERALD, GateTag.BUFF, GateTag.PERSISTENT, GateTag.AOE, GateTag.FIRE, GateTag.DURATION),
                        java.util.List.of()));
        // 血之捷 Herald of Blood：30 精魂。poe2wiki 真值（Buff, Attack, Persistent, AoE, Physical, Herald, Payoff）：
        // "While active, killing an enemy with Blood Loss will cause a bloody explosion that deals Physical Attack
        //  damage to surrounding enemies based off the life of the exploded enemy."（并有几率 Aggravate 流血）
        // **无常驻增益** —— 效果全在「击杀流血敌人触发」，故不挂常驻属性修改器。
        // 语料存在「受血之捷影响时物理伤害提高」类词条，缺此宝石会使该门控成为死限定。
        reg(bootstrap, "red/herald_of_blood",
                // poe2db 真值标签：Buff, Attack, Persistent, AoE, Physical, Herald, Payoff
                SpiritGemFactory.herald(GemGen.strReq(), SpiritGemFactory.spirit(30),
                        StatusDefs.HERALD_OF_BLOOD,
                        SpiritGemFactory.heraldProcPhase("physical", 12.0, 2.5),
                        java.util.List.of(GateTag.AURA, GateTag.HERALD, GateTag.BUFF, GateTag.PERSISTENT, GateTag.AOE, GateTag.PHYSICAL),
                        java.util.List.of()));
        // 压迫气场 Overwhelming Presence：30 精魂，Tier 8。poe2wiki 真值（Buff, Persistent, Aura）：
        // "While active, emits an Aura that makes enemies in your Presence easier to Stun and inflict
        //  Elemental Ailments upon."
        //   Enemies in your Presence have (15—24)% reduced Elemental Ailment Threshold
        //   Enemies in your Presence have (15—25)% reduced Stun Threshold
        // 机制：两条 Presence 光环<b>授予</b>条目 —— 向范围内敌人的 STAT_STORE 写归一属性
        // coe:ailment_threshold（ADD_VALUE，值取负 = reduced），作用域标签 {stun} / {elemental}。
        // 读点为 AilmentHelper.ailmentThresholdMul 的 {ailment, threshold, <异常>} 作用域：
        // {elemental} 经标签层级表命中点燃 / 冰缓 / 冰冻 / 感电，{stun} 命中眩晕。
        reg(bootstrap, "red/overwhelming_presence",
                SpiritGemFactory.of(GemGen.strReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT),
                        java.util.List.of(),
                        SpiritGemFactory.onCastPhase(new StatAction(
                                new StatGroup(java.util.List.of(
                                        thresholdGrant(negativeLinear(0.15, 0.25), GateTag.STUN),
                                        thresholdGrant(negativeLinear(0.15, 0.24), GateTag.ELEMENTAL))),
                                false,
                                java.util.Optional.of(Identifier.fromNamespaceAndPath(
                                        CraftOfExile.MODID, "stat_overwhelming_presence")),
                                ExpressionCodec.of(60.0)))));
        // 紧急时刻 Time of Need：30 精魂，Tier 8。poe2wiki 真值（Buff, Persistent, Duration）：
        // "While active, periodically invokes divine blessings to heal you and remove Curses and
        //  Elemental Ailments from you."
        //   (12—10.1) second duration between Blessings
        //   Blessing recovers (17—628) Life and removes Curses and Elemental Ailments
        // 机制：常驻相位周期门控 —— ON_CAST 由 SpiritManager 每 tick 重放，带 period_ticks 的动作
        // 按倒计时节流（通用能力；此前「每 X 秒」类常驻真值只能每 tick 触发，故整体缺失）。        // 祝福 = 回复生命（ResourceStatEffect 走统一资源枢纽，自动受回复率缩放）+ 净化（StatusStatEffect(direction=REMOVE)：
        // kinds=curse 命中全部诅咒；tags=elemental 命中点燃 / 冰缓 / 冰冻 / 感电 —— 四者跨
        // DAMAGING 与 CONTROL 两个大类，唯标签能一次覆盖）。
        reg(bootstrap, "red/time_of_need",
                SpiritGemFactory.of(GemGen.strReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT, GateTag.DURATION),
                        java.util.List.of(),
                        periodicPhase(blessingInterval(),                                new StatAction(new StatGroup(java.util.List.of(StatEntry.of(
                                        ExpressionCodec.ofLevelTable(java.util.List.of(
                                                17.0, 26.0, 37.0, 49.0, 63.0, 79.0, 97.0, 117.0, 140.0, 166.0,
                                                194.0, 226.0, 262.0, 301.0, 344.0, 392.0, 443.0, 500.0, 561.0, 628.0)),
                                        new ResourceStatEffect(ResourceType.HEALTH, ResourceStatEffect.Direction.HEAL, false, GateExpr.ALWAYS)))),
                                        false, java.util.Optional.empty(), ExpressionCodec.of(0.0)),
                                new StatAction(new StatGroup(java.util.List.of(StatusStatEffect.removeEntry(
                                        java.util.List.of("curse"), java.util.List.of(GateTag.ELEMENTAL), java.util.Set.of(), java.util.List.of()))),
                                        false, java.util.Optional.empty(), ExpressionCodec.of(0.0)))));

        // 无尽怒火 Eternal Rage：100 精魂，Tier 14。poe2wiki 真值（Buff, Persistent, ReserveInAllSets）：
        // "While active, you constantly regenerate Rage. Regenerate (0.25—5) Rage per second."
        // 当前 0.4+ 版本已无「怒气不再衰减 / 近战命中回怒」等旧效果，仅余每秒回怒。
        // 机制：常驻相位周期门控 —— ON_CAST 由 SpiritManager 每 tick 重放，周期 20 tick（=1 秒）触发一次，        // 经 ResourceStatEffect(rage) 走统一资源枢纽回怒（RageHelper 钳制到上限，满怒不溢出）。
        // 数值 (0.25—5) Rage/秒随等级线性成长（Lv1=0.25 … Lv20=5），逐等级中值严格查表。
        reg(bootstrap, "red/eternal_rage",
                SpiritGemFactory.of(GemGen.strReq(), SpiritGemFactory.spirit(100),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT),
                        java.util.List.of(),
                        periodicPhase(ExpressionCodec.of(20.0),                                new StatAction(new StatGroup(java.util.List.of(StatEntry.of(
                                        eternalRageRate(),
                                        new ResourceStatEffect(ResourceType.RAGE, ResourceStatEffect.Direction.HEAL, false, GateExpr.ALWAYS)))),
                                        false, java.util.Optional.empty(), ExpressionCodec.of(0.0)))));
        // 盛怒 Berserk：100 精魂，Tier 14。poe2wiki 真值（Buff, Persistent, Aura）：
        // "While active, strengthens your Rage ... Grants (40—59)% increased Rage effect."
        // 机制：常驻自增益（selfBuffPhase 经 ModifierAction 写 coe:rage_effect，ADD_MULTIPLIED_BASE，
        // 随等级 (40—59)% 查表）。rage_effect 默认 1.0，全部「每点怒气加成」通道（perRage()）乘此值，
        // 故 Berserk 放大所有 per-rage 收益（armor/stun threshold/minion atk speed/fire/phys/spell/minion dmg）。
        // 生命流失副作用（"Lose 0.1% max life per Rage per second"）属新机制（按当前怒气×最大生命对自身的周期伤害），
        // 当前未实装，列入已知近似项（见 docs/berserk_wiring.md），非静默死线。
        reg(bootstrap, "red/berserk",
                SpiritGemFactory.of(GemGen.strReq(), SpiritGemFactory.spirit(100),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT),
                        java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(berserkRageEffect())));
    }

    /**
     * 「范围内敌人 X% reduced 门槛」授予条目：向目标 STAT_STORE 写归一属性
     * {@code coe:ailment_threshold}（{@code scope} 作用域标签）。
     */
    private static StatEntry thresholdGrant(ExpressionCodec.Value magnitude, String scope) {
        return StatEntry.of(magnitude, java.util.List.of(),
                StatTargets.ofTarget(TargetSelector.SELF),
                new MultiStatEffect(java.util.List.of(new ModifierStatEffect(AttributeRegistry.AILMENT_THRESHOLD, Either.left(AttributeModifier.Operation.ADD_VALUE), ModifierStatEffect.Mode.GAIN, GateExpr.of(java.util.Set.of(scope))))))
                        .withConditionRef(StatConditionIds.presenceId(PresenceCountValue.Relation.ENEMY));
    }

    /** 负值线性成长（reduced 语义）：Lv1 = -{@code from}、Lv20 = -{@code to}。 */
    private static ExpressionCodec.Value negativeLinear(double from, double to) {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Subtract(
                new ExpressionCodec.Constant(-from),
                new ExpressionCodec.BinaryOperation.Multiply(
                        new ExpressionCodec.Constant((to - from) / 19.0),
                        new ExpressionCodec.BinaryOperation.Subtract(
                                ExpressionCodec.Variable.from("skill.level", 1.0),
                                new ExpressionCodec.Constant(1.0)))));
    }

    /** 祝福间隔（tick）：真值 (12—10.1) 秒随等级线性缩短 ⇒ 240 − 2×(等级−1)。 */
    private static ExpressionCodec.Value blessingInterval() {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Subtract(
                new ExpressionCodec.Constant(240.0),
                new ExpressionCodec.BinaryOperation.Multiply(
                        new ExpressionCodec.Constant(2.0),
                        new ExpressionCodec.BinaryOperation.Subtract(
                                ExpressionCodec.Variable.from("skill.level", 1.0),
                                new ExpressionCodec.Constant(1.0)))));
    }

    /** 无尽怒火每秒回怒量（Rage/秒）：真值 Lv1=0.25 … Lv20=5 线性成长，逐等级严格查表。 */
    private static ExpressionCodec.Value eternalRageRate() {
        return ExpressionCodec.ofLevelTable(java.util.List.of(
                0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5,
                2.75, 3.0, 3.25, 3.5, 3.75, 4.0, 4.25, 4.5, 4.75, 5.0));
    }

    /** 盛怒：怒气效果乘子（40—59）% 随等级成长，逐等级严格查表（ADD_MULTIPLIED_BASE，base=1.0）。 */
    private static ModifierAction berserkRageEffect() {
        return new ModifierAction(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "rage_effect"),
                ExpressionCodec.of(2.0),
                ExpressionCodec.ofLevelTable(java.util.List.of(
                        0.40, 0.41, 0.42, 0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49,
                        0.50, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59)),
                ExpressionCodec.of(0.0));
    }

    /**
     * 周期相位（<b>无投递</b>）：常驻重放路径下按 {@code periodTicks} 节流执行动作。
     * 「While active, every X seconds Y」类真值的通用载体（读点 {@code SpiritManager#applyPhaseAura}）。
     */
    private static SkillPhase periodicPhase(ExpressionCodec.Value periodTicks, Action... actions) {
        java.util.List<TriggeredAction> tas = new java.util.ArrayList<>();
        for (Action a : actions) {
            tas.add(new TriggeredAction(LifecycleTrigger.ON_CAST, a, java.util.Optional.of(periodTicks)));
        }
        return new SkillPhase(java.util.Optional.empty(), tas, java.util.List.of(),
                java.util.Optional.empty());
    }

    /** 预设 id 命名：剥离颜色前缀，仅保留宝石本名（如 {@code red/herald_of_ash} → {@code preset_herald_of_ash}）。 */
    private static String presetName(String path) {
        return "preset_" + path.substring(path.indexOf('/') + 1);
    }

    /** 数据驱动迁移（#87）：登记完整 Skill 为预设，宝石本体经 SkillRead 引用之（发射 Read 标签）。 */
    private static void reg(BootstrapContext<SkillEntry> bootstrap, String path, Skill full) {
        SkillPresets.registerPreset(GemGen.id(presetName(path)), full);
        // 精魂宝石本体路径经 SkillIds 登记（生成期按路径生成），供 SkillIds + SkillRead 引用。
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_SPIRIT_KEY, SkillIds.id(path)),
                SkillPresets.read(presetName(path)).withTier(TIER.getOrDefault(path, 1)));
    }

    /** 等阶（原 GemTiers 数据，就近定义于精魂注册文件）。默认 1，仅列非 1 项。 */
    private static final Map<String, Integer> TIER = Map.of(
            "red/overwhelming_presence", 8,
            "red/time_of_need", 8,
            "red/eternal_rage", 14,
            "red/berserk", 14);
}
