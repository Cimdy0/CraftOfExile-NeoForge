package com.cimdy.coe.data.gen.gem.spirit;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventType;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaGeometry;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import java.util.Optional;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeAction;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.gem.skill.SkillPresets;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import java.util.Map;

/**
 * 绿色（敏捷）精魂宝石生成 — 对齐 poe2wiki（0.3 / Third Edict）。
 *
 * <p>数据驱动迁移（#87）：每颗宝石经 {@link SkillPresets#registerPreset} 登记为 {@code coe:preset_<gem>}
 * 预设，宝石本体降为 {@code SkillRead(ref=preset_<gem>)} 引用之（发射 Read 标签）。
 */
public class SpiritGreenGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
        // 风舞者 Wind Dancer：30 精魂，每层获得更多闪避（PoE2 每层 +10% 闪避，最多 4 层）。
        // poe2wiki: Reservation 30 Spirit；Grants 10% more Evasion Rating per stage。
        // 本模型以「more 闪避 +10%」（coe:evasion 乘算修改器）近似 1 层基准值。
        // poe2db 真值标签：Buff, Attack, Persistent, AoE, Melee, Trigger, Staged
        // Staged（阶段层数）机制尚未落地，故不贴该标签，避免「有标签无机制」的虚接。
        reg(bootstrap, "green/wind_dancer",
                SpiritGemFactory.of(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT, GateTag.AOE, GateTag.MELEE, GateTag.TRIGGER), java.util.List.of(),
                        SpiritGemFactory.selfBuffPhase(
                                SpiritGemFactory.attrBuff("evasion", 2, 0.10))));
        // 雷之捷 Herald of Thunder：30 精魂。poe2wiki 真值（Buff, Attack, Persistent, AoE, Lightning, Herald, Payoff）：
        // "While active, killing Shocked enemies with a non-Herald Attack Hit will cause subsequent Attack Hits to
        //  release lightning bolts which deals Attack damage to all surrounding enemies."
        // **无常驻增益** —— 效果全在「击杀感电敌人后强化后续 (2-3) 次攻击」，故不挂常驻属性修改器。
        reg(bootstrap, "green/herald_of_thunder",
                // poe2db 真值标签：Buff, Attack, Persistent, AoE, Lightning, Herald, Payoff
                SpiritGemFactory.herald(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        StatusDefs.HERALD_OF_THUNDER,
                        SpiritGemFactory.heraldProcPhase("lightning", 12.0, 2.5),
                        java.util.List.of(GateTag.AURA, GateTag.HERALD, GateTag.BUFF, GateTag.PERSISTENT, GateTag.AOE, GateTag.LIGHTNING),
                        java.util.List.of()));
        // 瘟疫之捷 Herald of Plague：30 精魂。poe2wiki 真值（Buff, Persistent, Chaos, Herald）：
        // "While active, killing a Poisoned enemy spreads its Poison to other nearby enemies, and has a chance to
        //  also Hinder them."（扩散半径 (1.5-1.8)m，(10-29)% 几率 Hinder）
        // **无常驻增益** —— 效果全在「击杀中毒敌人扩散」，故不挂常驻属性修改器。
        reg(bootstrap, "green/herald_of_plague",
                // poe2db 真值标签：Buff, Persistent, Chaos, Herald
                SpiritGemFactory.herald(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        StatusDefs.HERALD_OF_PLAGUE,
                        SpiritGemFactory.heraldProcPhase("chaos", 12.0, 2.5),
                        java.util.List.of(GateTag.AURA, GateTag.HERALD, GateTag.BUFF, GateTag.PERSISTENT, GateTag.CHAOS),
                        java.util.List.of()));
        // 战斗狂怒 Combat Frenzy：30 精魂。poe2wiki 真值（Buff, Persistent, Tier 8）：
        // "Gain a Frenzy Charge when you Freeze, Electrocute or Pin an enemy,
        //  no more than once every (8-6.1) seconds"
        // 即「常驻激活 → 造成冻结/触电/钉住时获得 1 枚狂怒球」，内置冷却按 Lv1 真值 8s = 160 tick
        // （随等级缩短至 6.1s 的曲线未建模；事件侧为三条并列监听，各自独立冷却）。
        reg(bootstrap, "green/combat_frenzy",
                SpiritGemFactory.of(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.BUFF, GateTag.PERSISTENT),
                        // requireTags 语义为「事件须携带全部标签」，故三种异常各一条监听（OR 语义由三条并列表达）
                        java.util.List.<SkillModifier>of(
                                chargeOnAilment(GateTag.FREEZE),
                                chargeOnAilment(GateTag.ELECTROCUTE),
                                chargeOnAilment(GateTag.PIN))));
        // 优雅 Grace：30 精魂，提升闪避。poe2wiki: 30 Spirit；+X% Evasion Rating。
        // 真值标签：Buff, Persistent, Aura
        reg(bootstrap, "green/grace",
                SpiritGemFactory.of(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        java.util.List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT), java.util.List.of()));
        // 铁蒺藜之路 Trail of Caltrops：30 精魂，Tier 8（敏捷）。poe2wiki 真值
        // （Attack, Buff, Persistent, AoE, Projectile, Trigger, Physical, Duration, Hazard）：
        // "While active, scatters caltrops in your wake when you dodge." /
        // "Scatters 3 caltrops when you dodge" / "Caltrops Critical Hit Chance: 5.00%" /
        // "Maims and deals Attack area damage when triggered by an Enemy. Caltrops are destroyed when
        //  triggered, and a maximum of 20 Caltrops can be active at once" /
        // "(6—159) to (9—238) base Physical Damage" / "Maim on Hit" /
        // "Caltrop radius is 0.5 metres" / "Caltrop duration is 8 seconds"
        // Projectile 是 poe2db 的<b>内部实现</b>标签：本模组以危害物形态落地，无投射物投递，
        // 贴上即「有标签无机制」的虚接，故不贴。
        reg(bootstrap, "green/trail_of_caltrops",
                SpiritGemFactory.of(GemGen.dexReq(), SpiritGemFactory.spirit(30),
                        // 显式指定 cast：通用 of(...) 的 Spell cast 暴击恒为 0，会让真值
                        // 「铁蒺藜暴击率 5%」无处落地（DamageAction 读技能基础暴击率）。
                        // 走 Attack(OtherAttack) 而非 WeaponAttack —— 后者 hasSkillCrit() 为 false。
                        SkillCast.of(new SkillCast.Attack(
                                com.mojang.datafixers.util.Either.right(new SkillCast.Attack.OtherAttack(
                                        ExpressionCodec.of(0.0), ExpressionCodec.of(0.05))),
                                ExpressionCodec.of(0.0))),
                        java.util.List.of(GateTag.AURA, GateTag.ATTACK, GateTag.BUFF, GateTag.PERSISTENT,
                                GateTag.AOE, GateTag.TRIGGER, GateTag.PHYSICAL, GateTag.DURATION,
                                GateTag.HAZARD),
                        java.util.List.<SkillModifier>of(caltropsOnDodge()),
                        // 无投递、无动作的宿主相位：本宝石只有「翻滚 → 撒铁蒺藜」一条链路，
                        // 全部由常驻监听承载，每 tick 的 ON_CAST 重放无事可做。
                        new SkillPhase(java.util.Optional.empty(), java.util.List.of(),
                                java.util.List.of(), java.util.Optional.empty())));
    }

    /**
     * 常驻监听：翻滚 → 撒下 3 枚铁蒺藜（形态 {@code coe:caltrop}，半径 0.5m / 存活 8s / 上限 20 枚）。
     *
     * <p><b>触发分工</b>：{@code ON_LISTEN} 在翻滚事件触发时执行（生成铁蒺藜）；
     * {@code ON_HIT} 由铁蒺藜被敌人踩中时回调（伤害 + Maim）。事件回调侧 ON_HIT 同样会被执行，
     * 但此刻 {@code ActionContext} 无目标，伤害与状态动作均以「无目标」直接早退，
     * 不会退化成「翻滚瞬间就造成伤害」。
     */
    private static EventListenEffect caltropsOnDodge() {
        return new EventListenEffect(new EventListenPhase(
                SkillEventType.CUSTOM,
                java.util.Optional.empty(),
                java.util.List.of(
                        new TriggeredAction(LifecycleTrigger.ON_LISTEN,
                                new DeliveryAction(caltropDelivery())),
                        new TriggeredAction(LifecycleTrigger.ON_HIT, caltropDamage()),
                        new TriggeredAction(LifecycleTrigger.ON_HIT, caltropMaim())),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.of(EventName.DODGE_ROLL),
                java.util.Optional.empty()));
    }

    /** 铁蒺藜形态：{@code coe:caltrop}（static + contact(enemy)，触发即销毁）；真值 0.5m / 8s / 3 枚 / 上限 20。 */
    private static AreaDelivery caltropDelivery() {
        return new AreaDelivery(
                AreaGeometry.HAZARD,
                new SphereShape(ExpressionCodec.of(0.5f)),
                new InstantDuration(),
                ExpressionCodec.of(1.0),
                Optional.of(EntityRead.of(Identifier.parse("coe:caltrop"))),
                Optional.of(ExpressionCodec.of(160.0)),
                Optional.of(ExpressionCodec.of(3.0)),
                Optional.of(ExpressionCodec.of(20.0)),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * 铁蒺藜伤害：真值 «(6—159) to (9—238) base Physical Damage» —— 伤害动作只收单一数值，
     * 逐等级取 min/max 的<b>中值</b>（与既有技能同口径）。
     */
    private static DamageAction caltropDamage() {
        return new DamageAction("physical", ExpressionCodec.ofLevelTable(java.util.List.of(
                7.5, 12.0, 18.5, 25.5, 33.0, 41.0, 49.0, 58.0, 67.5, 77.0,
                87.5, 97.5, 108.5, 120.0, 132.5, 144.5, 157.5, 170.5, 184.0, 198.5)),
                java.util.Optional.empty(), ExpressionCodec.of(1.0));
    }

    /**
     * Maim on Hit：真值 «Maimed enemies suffer from 30% Slower Movement Speed and 15% reduced
     * Evasion Rating. Maim lasts for 4 seconds» —— 时长 4 秒 = 80 tick。
     *
     * <p><b>强度 30.0 而非 0.30</b>：MAIM 的 stat 提供器是 {@code maim}，
     * 按 {@code magnitude / 100} 折算减速比例（见 {@code StatusBehavior#maim}），
     * 即强度单位是<b>百分点</b> —— 与「标准致残 30%」的 {@code StatusDefs.MAIM_DEFAULT_MAGNITUDE=30.0}
     * 同口径（#MAIM-B1 后全仓致残强度统一为 30.0，并自带固定 15% reduced 闪避）。
     *
     * <p>闪避维已补齐（#MAIM-B1）：{@code StatusBehavior#maim} 在减速项之外追加固定的
     * 「15% reduced Evasion Rating」（POE2 真值，不随 magnitude 缩放），本处无需另行建模。
     */    private static StatAction caltropMaim() {
        StatEntry maim = StatEntry.of(ExpressionCodec.of(30.0), java.util.List.of(),
                        StatTargets.ofTarget(TargetSelector.EVENT_TARGET),
                        new StatusStatEffect(StatusDefs.MAIM, StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS))
                .withDuration(StatDuration.of(StatDuration.Kind.DURATION, 80));
        return new StatAction(new StatGroup(java.util.List.of(maim)),
                false, java.util.Optional.empty(), ExpressionCodec.of(0.0));
    }

    /** 「造成某异常 → 生成 1 枚狂怒球」的常驻监听（事件名 caused_ailment + 异常类型标签收敛）。 */
    private static EventListenEffect chargeOnAilment(String ailmentTag) {
        return new EventListenEffect(new EventListenPhase(
                SkillEventType.CUSTOM,
                java.util.Optional.empty(),
                java.util.List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                        new ChargeAction(ChargeType.FRENZY, ChargeAction.ChargeActionMode.GENERATE,
                                ExpressionCodec.of(1.0), java.util.Optional.empty(),
                                java.util.Optional.empty(), java.util.Optional.empty(), false,
                                java.util.List.of()))),
                java.util.Optional.of(ExpressionCodec.of(160.0)),
                java.util.Optional.empty(),
                java.util.Optional.of(EventName.CAUSED_AILMENT),
                java.util.Optional.of(java.util.List.of(ailmentTag))));
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
            "green/wind_dancer", 4,
            "green/combat_frenzy", 8,
            "green/trail_of_caltrops", 8
    );
}
