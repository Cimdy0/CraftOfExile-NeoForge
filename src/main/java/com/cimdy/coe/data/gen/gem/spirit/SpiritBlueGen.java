package com.cimdy.coe.data.gen.gem.spirit;
import com.cimdy.coe.data.gen.stat.StatConditionIds;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.stat.effect.grant.MultiStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.SyntheticChillStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.action.control.ChanceAction;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.skill.phase.action.support.InfusionRemnantAction;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaGeometry;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import java.util.Optional;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.event.EventName;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.gem.skill.SkillPresets;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import java.util.Map;

/**
 * 蓝色（智力）精魂宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 *
 * <p>数据驱动迁移（#87）：每颗宝石经 {@link SkillPresets#registerPreset} 登记为 {@code coe:preset_<gem>}
 * 预设，宝石本体降为 {@code SkillRead(ref=preset_<gem>)} 引用之（发射 Read 标签）。
 */
public class SpiritBlueGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
        // 寒冰之捷 Herald of Ice：30 精魂。poe2wiki 真值（Buff, Attack, Persistent, AoE, Cold, Herald, Payoff）：
        // "While active, Shattering an enemy with a non-Herald Attack Hit will cause an icy explosion that deals
        //  Attack damage to surrounding enemies."
        // **无常驻增益** —— 效果全在「碎裂触发」，故不挂常驻属性修改器
        // （曾误接「more 冰霜伤害 +25%」，源 PoE1 语料，已按真值撤回）。
        reg(bootstrap, "blue/herald_of_ice",
                // poe2db 真值标签：Buff, Attack, Persistent, AoE, Cold, Herald, Payoff
                // Payoff（消耗异常触发）机制未落地，暂不贴；Attack 由伤害来源体现，此处不重复声明。
                SpiritGemFactory.herald(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        StatusDefs.HERALD_OF_ICE,
                        SpiritGemFactory.heraldProcPhase("cold", 12.0, 2.5),
                        java.util.List.of(GateTag.AURA, GateTag.HERALD, GateTag.BUFF, GateTag.PERSISTENT, GateTag.AOE, GateTag.COLD),
                        java.util.List.of()));
        // 纯净火焰 Purity of Fire：30 精魂，提升火焰抗性。poe2wiki: 30 Spirit；+22% Fire Resistance。
        // poe2db 真值标签：Buff, Persistent, Aura, Fire（AURA 为项目内精魂光环机制标签，保留）
        reg(bootstrap, "blue/purity_of_fire",
                SpiritGemFactory.of(GemGen.strIntReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.FIRE), java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(
                                SpiritGemFactory.attrBuff("resistances", 0, 0.22, "fire"))));
        // 纯净冰霜 Purity of Ice：30 精魂，提升冰霜抗性。poe2wiki: 30 Spirit；+22% Cold Resistance。
        // poe2db 真值标签：Buff, Persistent, Aura, Cold
        reg(bootstrap, "blue/purity_of_ice",
                SpiritGemFactory.of(GemGen.strIntReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.COLD), java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(
                                SpiritGemFactory.attrBuff("resistances", 0, 0.22, "cold"))));
        // 纯净闪电 Purity of Lightning：30 精魂，提升闪电抗性。poe2wiki: 30 Spirit；+22% Lightning Resistance。
        // poe2db 真值标签：Buff, Persistent, Aura, Lightning
        reg(bootstrap, "blue/purity_of_lightning",
                SpiritGemFactory.of(GemGen.strIntReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.LIGHTNING), java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(
                                SpiritGemFactory.attrBuff("resistances", 0, 0.22, "lightning"))));
        // 纯净混沌 Impurity：30 精魂，提升混沌抗性。poe2wiki: 30 Spirit；+10% Chaos Resistance。
        // poe2db 真值标签：Buff, Persistent, Aura, Chaos
        reg(bootstrap, "blue/impurity",
                SpiritGemFactory.of(GemGen.strIntReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.CHAOS), java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(
                                SpiritGemFactory.attrBuff("resistances", 0, 0.10, "chaos"))));
        // 清晰 Clarity：30 精魂，提升魔力回复。poe2wiki: 30 Spirit；+X% Mana Regeneration。
        // 真值标签：Buff, Persistent, Aura（光环类共性；元素标签不适用故不加）
        reg(bootstrap, "blue/clarity",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT), java.util.List.of()));
        // 狂怒 Wrath：30 精魂。真值复核（2026-09-10）：PoE2 精魂宝石列表**无 Wrath**
        // （仅存在 Wrath Sceptre 授予的 Fulmination）。旧注「+25% Lightning Damage」源 PoE1 语料，
        // 已撤回常驻增伤接线；在取得 PoE2 真值前保持无增益（宁缺勿编）。
        reg(bootstrap, "blue/wrath",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT), java.util.List.of()));
        // 元素虹吸 Siphon Elements：30 精魂。poe2wiki 真值（Buff, Persistent, Lightning, Cold, Fire, Remnant, Tier 8）：
        // "While active, has a chance to create an Infusion Remnant when you Freeze, Shock or Ignite a target."
        //   5% chance per Power to spawn a Cold Remnant on Freezing a target
        //   5% chance to spawn a Fire Remnant on Igniting a **non-Ignited** target
        //   5% chance to spawn a Lightning Remnant on Shocking a **non-Shocked** target
        // 即三条并列常驻监听（requireTags 为 AND 语义，无法一条表达三种异常；各自独立几率，真值未给共享冷却）。
        // 「每 1 枚暴击球 5%」走 charge 变量节点读当前充能数（上限属性 max_power_charges 不反映当前持有数）。
        // 「non-Ignited / non-Shocked」由事件侧 fresh_ailment 标签门控（新施加 vs 刷新已有异常）；
        // Freeze 一条真值无该约束，故不要求 fresh（对已冻结目标再次冻结同样可生成）。
        reg(bootstrap, "blue/siphon_elements",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT, GateTag.REMNANT,
                                GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING),
                        java.util.List.<SkillModifier>of(
                                remnantOnAilment(GateTag.FREEZE, "cold", perCharge("power", 0.05), false),
                                remnantOnAilment(GateTag.IGNITE, "fire", ExpressionCodec.of(0.05), true),
                                remnantOnAilment(GateTag.SHOCK, "lightning", ExpressionCodec.of(0.05), true))));
        // 闪现 Blink：60 精魂，Tier 8。poe2wiki 真值（Buff, Spell, Persistent, Travel）：
        // "While active, replaces your dodge roll with a short-cooldown Spell that allows you to tunnel
        //  through space, instantly reappearing a medium distance away."
        //   Reservation: 60 Spirit / Cooldown Time: (4.50—3.50)s / Cast Time: 0.70s / Teleports (4—4.9) metres
        // 机制：常驻保留 60 精魂；激活时由 DodgeRollHelper 以<b>翻滚键</b>触发本技能（真值「替换翻滚」，
        // 且不记 DODGE_ROLL 事件 —— 0.3.0 起 "Using Blink no longer counts as dodge rolling"）。
        // 施法时间取瞬时（time=0 → 1 tick）：真值 0.70s 前摇在本模型需按住施法键累积 pressTime，
        // 而 Blink 由点按翻滚键触发、pressTime 恒达不到阈值，取真值前摇将使技能永不结算（死线）。
        // 距离走 MoveAction.TELEPORT 的 force（= 最大传送距离，格，取 Lv1 真值 4）；
        // 冷却取 Lv1 真值 4.5s = 90 tick，落在 TRAVEL_BIND 冷却槽（见 DodgeRollHelper）。
        reg(bootstrap, "blue/blink",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(60),
                        SkillCast.of(new SkillCast.Spell(
                                ExpressionCodec.of(0.0), ExpressionCodec.of(90.0), ExpressionCodec.of(0.0))),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT,
                                GateTag.SPELL, GateTag.TRAVEL),
                        java.util.List.of(),
                        SpiritGemFactory.movePhase(new MoveAction(MoveAction.MoveType.TELEPORT,
                                blinkDistance(), java.util.Optional.empty()))));

        // 凋零光环 Withering Presence：30 精魂，Tier 4。poe2wiki 真值（Buff, Persistent, Aura, Chaos, Duration）：
        // "While active, emits an Aura that periodically Withers enemies in your Presence."
        //   Withered duration is 6 seconds
        //   Withers enemies in your Presence every (1.8—1.42) seconds
        // 机制：Presence 光环条目（MultiStatEffect 复合容器，目标 PRESENCE_ENEMIES）经 StatAction
        // 写入玩家 STAT_STORE，由 PeriodicDriver 按条目 INTERVAL 节拍对范围内敌人施加 coe:wither
        // （叠层上限 10，每层 +6% 承受混沌伤害，由 StatusBehavior.witherProv 真读）。
        // 间隔当前为固定 36 tick（真值 (1.8—1.42) 秒**未按宝石等级插值**：StatDuration.ticks() 返回 int，PeriodicDriver 间隔链无法承载表达式）；状态时长取真值 6s = 120 tick。
        reg(bootstrap, "blue/withering_presence",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT,
                                GateTag.CHAOS, GateTag.DURATION),
                        java.util.List.of(),
                        SpiritGemFactory.onCastPhase(new StatAction(
                                new StatGroup(java.util.List.of(presenceAuraEntry(
                                        new MultiStatEffect(java.util.List.of(new StatusStatEffect(StatusDefs.WITHER, StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS))),
                                        ExpressionCodec.of(1.0)).withDuration(StatDuration.of(StatDuration.Kind.INTERVAL, 36)))),
                                false,
                                java.util.Optional.of(Identifier.fromNamespaceAndPath(
                                        CraftOfExile.MODID, "stat_withering_presence")),
                                ExpressionCodec.of(60.0)))));

        // 魔力残迹 Mana Remnants：30 精魂，Tier 4。poe2wiki 真值（Buff, Persistent, Remnant）：
        // "While active, enemies you kill affected by Elemental Ailments have a chance to spawn a Mana Remnant,
        //  and Critically Hitting a target with Elemental Ailments spawns a Mana Remnant every few seconds.
        //  Picking up a Mana Remnant grants you Mana which can Overflow maximum Mana."
        //   Remnants last for 8 seconds
        //   25% chance to spawn a Remnant on killing an enemy affected by an Elemental Ailment
        //   Spawn a Remnant on Critically Hitting a target affected by an Elemental Ailment,
        //     no more than once every 2 seconds
        //   Each Remnant grants (28—281) Mana
        // 机制：两条常驻监听（kill / caused_crit），require_tags=[elemental]——元素异常状态
        // （ignite / chill / freeze / shock）的登记标签自带 elemental，故一条即可覆盖全部元素异常，
        // 无需按异常拆三条（拆开会让「带两种异常」的敌人被掷骰两次，概率被放大，违背真值）。
        // 产出走统一 AreaDelivery 的 REMNANT 几何（实体残片 coe:remnant，寿命 160 tick = 8 秒）；拾取时由残片形态
        // 点燃 ON_PICKUP 回复魔力——ON_PICKUP 只在拾取时刻点燃、事件触发时不执行，
        // 真值要求「拾取残片才回魔」，若复用 ON_HIT 会退化为「击杀瞬间即回魔」。
        reg(bootstrap, "blue/mana_remnants",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT, GateTag.REMNANT),
                        java.util.List.<SkillModifier>of(
                                remnantOnEvent(EventName.KILL, ExpressionCodec.of(0.25),
                                        java.util.Optional.empty()),
                                remnantOnEvent(EventName.CAUSED_CRIT, ExpressionCodec.of(1.0),
                                        java.util.Optional.of(ExpressionCodec.of(40.0))))));

        // 怨毒 Malice：30 精魂。poe2wiki 真值（Buff, Persistent, Aura, Duration）：
        // "While active, emits an Aura that continuously inflicts Critical Weakness on enemies in your Presence."
        //   Critical Weakness duration is 4 seconds
        //   Applies Critical Weakness to enemies every (0.7—0.51) seconds
        // 机制：复用 withering_presence 的 MultiStatEffect 复合通道（目标 PRESENCE_ENEMIES），
        // 由 PeriodicDriver 每 interval 对范围内敌人施加 coe:critical_weakness（状态已注册 StatusDefs.CRITICAL_WEAKNESS，
        // 消费 takenCritChance = magnitude/100）。magnitude 取 100.0，与既有 WARCRIES_INFLICT_CRITICAL_WEAKNESS
        // 对该状态的施加量级一致（同一状态、同一量纲，非编造）。间隔当前为固定 14 tick（真值 (0.7—0.51) 秒**未按宝石等级插值**：StatDuration.ticks() 返回 int，PeriodicDriver 间隔链无法承载表达式）。
        reg(bootstrap, "blue/malice",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.DURATION),
                        java.util.List.of(),
                        SpiritGemFactory.onCastPhase(new StatAction(
                                new StatGroup(java.util.List.of(presenceAuraEntry(
                                        new MultiStatEffect(java.util.List.of(new StatusStatEffect(StatusDefs.CRITICAL_WEAKNESS, StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS))),
                                        ExpressionCodec.of(100.0)).withDuration(StatDuration.of(StatDuration.Kind.INTERVAL, 14)))),
                                false,
                                java.util.Optional.of(Identifier.fromNamespaceAndPath(
                                        CraftOfExile.MODID, "stat_malice")),
                                ExpressionCodec.of(60.0)))));
        // 大法师 Archmage：100 精魂，Tier 14。poe2wiki/poe2db 真值（Buff, Persistent, Lightning）：
        // "While active, causes your Non-Channelling Spells to cost additional mana and deal extra Lightning damage,
        //  both based on your maximum Mana."
        //   Non-Channelling Spells Gain 4% of Damage as extra Lightning damage for each 100 maximum Mana
        //   Non-Channelling Spells cost an additional (8-6.1)% of your maximum Mana
        // 机制：常驻自增益（selfBuffPhase 经 ModifierAction 写 coe:archmage_lightning_gain = 0.04，base=0 不激活无效）；
        // 额外闪电由 DamagePipeline 新增 GAINED_AS_EXTRA 阶段在减免前并入闪电击槽位（读 archmage_lightning_gain x 最大魔力/100）。
        // "非引导法术" 由 GateTag.CHANNELING 门控（HoldInput/Continuous 在 Skill 构造 statTags 时打标，跳过）。
        // 额外魔力消耗（按最大魔力 (8->6.1)%）属运行期 cost 层全局挂钩，SkillContext 不暴露技能是否引导/法术，
        // 暂无干净挂点，列入已知近似项（见 docs/archmage_wiring.md），非静默死线。
        reg(bootstrap, "blue/archmage",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(100),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT, GateTag.LIGHTNING),
                        java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(archmageGain())));

        // 冰雪之心 Heart of Ice：30 精魂。poe2wiki 真值（Buff, Persistent, Aura, Cold）：
        // "Chills all enemies in your Presence as though dealing (26-888) to (39-1332) Cold damage."
        // 机制：目标 PRESENCE_ENEMIES（存在范围是距离筛选出的成员集合），由 PeriodicDriver 驱动，
        // 对范围内敌人按「合成冰霜伤害」推导冰缓百分比（AilmentHelper.chillPercentFromSyntheticCold，
        // 复用真实冰缓公式，门槛/积蓄/幅度修正全生效）。
        // 本条目<b>不声明任何时长/节拍</b>——语料没有「每隔 X 秒」的描述，而光环是集合关系：
        // 未声明节拍即由驱动每刻施加并刷新，离开范围即失效（不编造间隔数值）。
        // 冰缓的 2s（StatusDefs.CHILL，40t）由异常侧按合成冰霜伤害推导，<b>不由本光环赋予</b>。
        // 合成冰霜伤害取真值最大档（39->1332），经 skill.level 变量节点随宝石等级求值。
        reg(bootstrap, "blue/heart_of_ice",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.COLD),
                        java.util.List.of(),
                        SpiritGemFactory.onCastPhase(new StatAction(
                                new StatGroup(java.util.List.of(presenceAuraEntry(
                                        new MultiStatEffect(java.util.List.of(new SyntheticChillStatEffect(heartOfIceColdDamage(), GateExpr.ALWAYS))),
                                        ExpressionCodec.of(1.0)))),
                                false,
                                java.util.Optional.of(Identifier.fromNamespaceAndPath(
                                        CraftOfExile.MODID, "stat_heart_of_ice")),
                                ExpressionCodec.of(60.0)))));

        // 康复 Convalescence：30 精魂。poe2wiki 真值（Spirit, Persistent, ReserveInAllSets）：
        // 常驻即对自身施加 CONVALASCENCE 状态 → 能量护盾充能立即开始且不可被打断
        // （LivingTickDriver 充能门控扩展读取 StatusEngine.has(CONVALASCENCE)）。
        // 真值经「子技能触发 3s 窗口 + 30s 冷却 + 满护盾移除」封装，见 docs/convalescence_wiring.md。
        reg(bootstrap, "blue/convalescence",
                SpiritGemFactory.of(GemGen.intReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT),
                        java.util.List.of(),
                        SpiritGemFactory.onCastPhase(new StatAction(
                                new StatGroup(java.util.List.of(StatusStatEffect.selfEntry(
                                        StatusDefs.CONVALASCENCE, ExpressionCodec.of(1.0)))),
                                false,
                                java.util.Optional.of(Identifier.fromNamespaceAndPath(
                                        CraftOfExile.MODID, "stat_convalescence")),
                                ExpressionCodec.of(60.0)))));
    }

    /**
     * 常驻监听：某事件 + 目标带元素异常 → 产出一枚魔力残片（可带几率 / 内置冷却）。
     *
     * <p><b>两个 trigger 的分工</b>：{@code ON_LISTEN} 在事件触发时执行（生成残片），
     * {@code ON_PICKUP} 在残片被拾取时执行（回复魔力）。二者同属一个相位实例——残片由
     * 统一 AreaDelivery 的 REMNANT 几何生成时 retain 本实例，拾取时经 {@code (casterId, phaseId)} 反查回调。
     * 若都写成 ON_HIT，事件回调会连同回魔一起执行（击杀即回魔），与真值相悖。
     *
     * @param eventName 事件名（{@code kill} / {@code caused_crit}）
     * @param chance    产出几率（击杀侧真值 25%；暴击侧真值无几率，恒产出）
     * @param cooldown  内置冷却（暴击侧真值 2 秒 = 40 tick；击杀侧真值无）
     */
    private static EventListenEffect remnantOnEvent(String eventName, ExpressionCodec.Value chance,
                                                    java.util.Optional<ExpressionCodec.Value> cooldown) {
        return new EventListenEffect(new EventListenPhase(
                SkillEventType.CUSTOM,
                java.util.Optional.empty(),
                java.util.List.of(
                        new TriggeredAction(LifecycleTrigger.ON_LISTEN,
                                new ChanceAction(chance,
                                        java.util.List.of(new DeliveryAction(manaRemnantArea())))),
                        new TriggeredAction(LifecycleTrigger.ON_PICKUP,
                                new StatAction(new StatGroup(java.util.List.of(StatEntry.of(
                                        remnantManaAmount(),
                                        new ResourceStatEffect("mana", ResourceStatEffect.Direction.HEAL, true, GateExpr.ALWAYS)))),
                                        false, java.util.Optional.empty(), ExpressionCodec.of(0.0)))),
                cooldown,
                java.util.Optional.empty(),
                java.util.Optional.of(eventName),
                java.util.Optional.of(java.util.List.of(GateTag.ELEMENTAL))));
    }

    /** 魔力残片形态：通用残片（{@code coe:remnant}），寿命取真值 8 秒 = 160 tick。 */
    private static AreaDelivery manaRemnantArea() {
        return new AreaDelivery(
                AreaGeometry.REMNANT,
                new SphereShape(ExpressionCodec.of(1.5f)),
                new InstantDuration(),
                ExpressionCodec.of(1.0),
                Optional.of(EntityRead.of(Identifier.parse("coe:remnant"))),
                Optional.of(ExpressionCodec.of(160.0)),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** 每枚残片回复魔力：真值 «(28—281)» —— Lv1 = 28、Lv20 = 281，线性插值。 */
    private static ExpressionCodec.Value remnantManaAmount() {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Add(
                new ExpressionCodec.Constant(28.0),
                new ExpressionCodec.BinaryOperation.Multiply(
                        new ExpressionCodec.Constant((281.0 - 28.0) / 19.0),
                        new ExpressionCodec.BinaryOperation.Subtract(
                                ExpressionCodec.Variable.from("skill.level", 1.0),
                                new ExpressionCodec.Constant(1.0)))));
    }

    /** 大法师：额外闪电伤害比率（每 100 最大魔力）—— base=0 不激活无效；ADD_VALUE 写 0.04（4%，0.3.0 全等级恒定）。 */
    private static ModifierAction archmageGain() {
        return new ModifierAction(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "archmage_lightning_gain"),
                ExpressionCodec.of(0.0),
                ExpressionCodec.of(0.04),
                ExpressionCodec.of(0.0));
    }

    /**
     * 冰雪之心合成冰霜伤害（chill 推导用）：真值「as though dealing (26-888) to (39-1332) Cold damage」取最大档，
     * Lv1 = 39、Lv20 = 1332，线性插值（per = (1332-39)/19）。走 skill.level 变量节点真读，
     * 否则宝石等级对冰缓强度毫无影响（半接线）。该值仅用于冰缓百分比推导，不造成真实伤害。
     */
    private static ExpressionCodec.Value heartOfIceColdDamage() {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Add(
                new ExpressionCodec.Constant(39.0),
                new ExpressionCodec.BinaryOperation.Multiply(
                        new ExpressionCodec.Constant((1332.0 - 39.0) / 19.0),
                        new ExpressionCodec.BinaryOperation.Subtract(
                                ExpressionCodec.Variable.from("skill.level", 1.0),
                                new ExpressionCodec.Constant(1.0)))));
    }

    /** Presence 光环条目：数值 = {@code magnitude}，作用目标 = 持有者 Presence 范围内的敌人。 */
    private static StatEntry presenceAuraEntry(StatEffect effect,
                                               ExpressionCodec.Value magnitude) {
        return StatEntry.of(magnitude, java.util.List.of(),
                StatTargets.ofTarget(TargetSelector.SELF), effect)
                .withConditionRef(StatConditionIds.presenceId(PresenceCountValue.Relation.ENEMY));
    }

    /**
     * 传送距离（格）：真值 «Teleports (4—4.9) metres»，Lv1 = 4、Lv20 = 4.9，线性插值
     * （poe2db 未逐等级列距离，仅有区间端点；取 per-level = 0.9 / 19 ≈ 0.0474）。
     * 走 {@code skill.level} 变量节点真读当前技能等级 —— 否则宝石等级对 Blink 毫无影响（半接线）。
     */
    private static ExpressionCodec.Value blinkDistance() {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Add(
                new ExpressionCodec.Constant(4.0),
                new ExpressionCodec.BinaryOperation.Multiply(
                        new ExpressionCodec.Constant(0.9 / 19.0),
                        new ExpressionCodec.BinaryOperation.Subtract(
                                ExpressionCodec.Variable.from("skill.level", 1.0),
                                new ExpressionCodec.Constant(1.0)))));
    }

    /** 「造成某异常 → 按几率生成对应元素灌入残迹」的常驻监听。 */
    private static EventListenEffect remnantOnAilment(String ailmentTag, String kind,
                                                     ExpressionCodec.Value chance, boolean requireFresh) {
        java.util.List<String> tags = requireFresh
                ? java.util.List.of(ailmentTag, GateTag.FRESH_AILMENT)
                : java.util.List.of(ailmentTag);
        return new EventListenEffect(new EventListenPhase(
                SkillEventType.CUSTOM,
                java.util.Optional.empty(),
                java.util.List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                        new ChanceAction(chance,
                                java.util.List.of(new InfusionRemnantAction(kind))))),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.of(EventName.CAUSED_AILMENT),
                java.util.Optional.of(tags)));
    }

    /** 「每 1 枚 X 球 per 点」几率：当前充能数 × per（充能数走 charge 变量节点真读）。 */
    private static ExpressionCodec.Value perCharge(String charge, double per) {
        return ExpressionCodec.of(new ExpressionCodec.BinaryOperation.Multiply(
                ExpressionCodec.Variable.from("charge", Identifier.parse("coe:" + charge), 0.0),
                new ExpressionCodec.Constant(per)));
    }

    /** 预设 id 命名：剥离颜色前缀，仅保留宝石本名。 */
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
            "blue/siphon_elements", 8,
            "blue/blink", 8,
            "blue/withering_presence", 4,
            "blue/archmage", 14);
}
