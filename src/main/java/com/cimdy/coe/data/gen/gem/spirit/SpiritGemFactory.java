package com.cimdy.coe.data.gen.gem.spirit;

import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.helper.hub.ReservationManager;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceUsage;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.input.Click;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 精魂宝石构造工厂 — 精魂宝石本质是预留精魂（PRESERVE）的光环/增益技能。
 *
 * <p>PoE2 对应：一颗 Spirit Gem 占用一部分灵魂值（Reservation），激活后持续提供增益。
 * 本模型中：
 * <ul>
 *   <li>消耗用 {@link #spirit(double)} 构造 {@code ResourceCost(SPIRIT, PRESERVE, value)}，
 *       施放时由 {@code ReservationManager} 登记保留，技能停止时释放；</li>
 *   <li>增益通过自施放范围相位（命中自身）上的 {@link ModifierAction} 落地，
 *       对施法者的属性（如火焰伤害、闪避）施加持续修改器（duration<=0 视为常驻）；</li>
 *   <li>数值对齐 {@code poe2wiki.net}（0.3 / Third Edict），精魂预留统一 30。</li>
 * </ul>
 *
 * <p>已知近似：PoE2 的增益是「对所有技能生效」的全局光环，本模型通过给施法者属性加修改器
 * 近似实现（如 more 火焰伤害 = 给 {@code coe:fire_damage} 加 25% 乘算修改器）；
 * 分层（Wind Dancer 每层 +10% 闪避）目前仅取 1 层基准值。
 */
public final class SpiritGemFactory {
    private SpiritGemFactory() {}

    /** 精魂预留消耗（PRESERVE）：施放时登记保留，停止时释放。 */
    public static ResourceCost spirit(double amount) {
        return new ResourceCost(ResourceType.SPIRIT, ResourceUsage.PRESERVE, ExpressionCodec.of(amount));
    }

    /** 生命预留消耗（PRESERVE）：施放时登记 health 预留，停止时释放（生命预留源）。 */
    public static ResourceCost life(double amount) {
        return new ResourceCost(ResourceType.HEALTH, ResourceUsage.PRESERVE, ExpressionCodec.of(amount));
    }

    /**
     * 属性增益动作：对施法者施加持续属性修改器。
     * @param attributeName 属性注册名（如 {@code "fire_damage"}，对应 {@code coe:fire_damage}）
     * @param operation     0=加法 / 1=乘算总计(ADD_MULTIPLIED_TOTAL) / 2=乘算基础
     * @param value        修改量（乘算时为比例，如 0.25 = +25%）
     */
    public static ModifierAction attrBuff(String attributeName, int operation, double value) {
        return new ModifierAction(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, attributeName),
                ExpressionCodec.of((double) operation),
                ExpressionCodec.of(value),
                ExpressionCodec.of(0.0));
    }

    /**
     * 带语义标签的属性增益 —— 归一属性（抗性 / 伤害）按标签区分维度，缺标签即落不到具体维度。
     *
     * @param tags 维度标签（抗性 = 元素名；伤害 = 元素名 + {@code "damage"}）
     */
    public static ModifierAction attrBuff(String attributeName, int operation, double value, String... tags) {
        return new ModifierAction(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, attributeName),
                ExpressionCodec.of((double) operation),
                ExpressionCodec.of(value),
                ExpressionCodec.of(0.0),
                tags);
    }

    /** 自施放范围相位：在施法者自身触发属性增益动作（ON_CAST）。 */
    public static SkillPhase selfBuffPhase(ModifierAction... actions) {
        AreaDelivery delivery = new AreaDelivery(
                new SphereShape(ExpressionCodec.of(1.0f)),
                new InstantDuration(),
                ExpressionCodec.of(1.0));
        List<TriggeredAction> tas = Arrays.stream(actions)
                .map(a -> new TriggeredAction(LifecycleTrigger.ON_CAST, a))
                .toList();
        return new SkillPhase(Optional.of(delivery), tas, List.of(), Optional.empty());
    }

    /** 组装一颗精魂宝石（光环）。phases 为空时退化为仅预留精魂的无动作光环。所有精魂宝石默认带 {@code aura} 标签。 */
    public static Skill of(Require require, ResourceCost spirit, List<SkillModifier> modifiers,
                           SkillPhase... phases) {
        return of(require, spirit, List.of(GateTag.AURA), modifiers, phases);
    }

    /** 组装一颗精魂宝石，并附加额外的技能类型标签（如 {@code herald} 捷类）。所有精魂宝石默认带 {@code aura} 标签。 */
    public static Skill of(Require require, ResourceCost spirit, List<String> tags,
                           List<SkillModifier> modifiers, SkillPhase... phases) {
        SkillPhase phase = phases.length > 0 ? phases[0] : selfBuffPhase();
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(),
                SkillCast.of(new SkillCast.Spell(
                        ExpressionCodec.of(0.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.0))),
                new Click(), List.of(spirit), List.of(player), modifiers, 1, tags,
                Optional.empty(), Optional.empty(), Map.of(),
                Optional.empty(), Optional.empty());
    }

    /**
     * 组装一颗「可主动触发」的精魂宝石（如 Blink）：与常驻光环不同，这类技能由<b>外部按键</b>
     * （Blink 由翻滚键）瞬时触发，须自带真实冷却与施法时间，故允许显式指定 {@link SkillCast}
     * （通用 {@link #of} 的 cast 恒为 0/0/0，被触发时冷却无从表达）。
     */
    public static Skill of(Require require, ResourceCost spirit, SkillCast cast, List<String> tags,
                           List<SkillModifier> modifiers, SkillPhase phase) {
        SkillPhasePlayer player = new SkillPhasePlayer(Optional.empty(), phase);
        return new Skill(require, "", Optional.empty(), cast,
                new Click(), List.of(spirit), List.of(player), modifiers, 1, tags,
                Optional.empty(), Optional.empty(), Map.of(),
                Optional.empty(), Optional.empty());
    }

    /** 位移相位（<b>无投递</b>）：ON_CAST 直接执行位移动作，不生成任何投递物。 */
    public static SkillPhase movePhase(Action action) {
        return onCastPhase(action);
    }

    /**
     * ON_CAST 相位（<b>无投递</b>）：直接执行单个动作，不生成任何投递物。
     * <p>供「常驻即生效」的精魂效果使用（如 {@link StatAction} 写 Presence 光环条目 ——
     * 目标由条目自身的 {@code PRESENCE_ENEMIES} 选择器解析，无需投递物）。
     */
    public static SkillPhase onCastPhase(Action action) {
        return new SkillPhase(Optional.empty(),
                List.of(new TriggeredAction(LifecycleTrigger.ON_CAST, action)), List.of(), Optional.empty());
    }

    /**
     * 捷的触发效果相位——PoE2 中捷的<b>伤害</b>来源：击杀时以死者为中心产生一次元素范围爆发
     * （灰烬=火、寒冰=冰、雷霆=电、瘟疫=混沌、血=物理）。
     *
     * <p>作为<b>子相位</b>挂在主相位下：主相位只声明 ON_CAST（常驻增益），子相位只声明 ON_HIT
     * （爆发伤害）。{@link com.cimdy.coe.data.helper.hub.SpiritManager} 的每 tick 光环重放只执行
     * ON_CAST，故子相位不会被每 tick 误触发；爆发经 {@code triggerHeraldProcs} 在击杀时显式点燃。
     *
     * <p>伤害由 {@link DamageAction} 结算，作用域自动并入本技能的 {@code herald} 标签 ——
     * 这是「捷技能造成的伤害提高」类词条（{@code herald_skills_deal_inc_damage}）唯一的真实读点，
     * 缺此相位时该类词条虽已接线却永不命中（静默死线）。
     */
    public static SkillPhase heraldProcPhase(String damageType, double amount, double radius) {
        AreaDelivery delivery = new AreaDelivery(
                new SphereShape(ExpressionCodec.of((float) radius)),
                new InstantDuration(),
                ExpressionCodec.of(1.0));
        DamageAction hit = new DamageAction(damageType, ExpressionCodec.of(amount),
                Optional.empty(), ExpressionCodec.of(1.0));
        List<TriggeredAction> tas = List.of(new TriggeredAction(LifecycleTrigger.ON_HIT, hit));
        return new SkillPhase(Optional.of(delivery), tas, List.of(), Optional.empty());
    }

    /**
     * 组装一颗「捷（Herald）」精魂宝石：在 {@code aura} 标签基础上附加 {@code herald} 标签，
     * 把「受该捷影响」的存在性标记挂进首个相位的 ON_CAST，并把爆发相位挂为其子相位。
     *
     * <p>标记由 {@link com.cimdy.coe.data.helper.hub.SpiritManager} 每 tick 随光环重放刷新，
     * 预留停止后自然过期——因此 {@code while affected by Herald of X} 类门控可由
     * {@code status:<mark id>} 直接读取，技能侧无需任何按 id 的分支。
     *
     * @param proc 击杀爆发相位（可为 null = 该捷无爆发）；经 {@link #heraldProcPhase} 构造。
     */
    public static Skill herald(Require require, ResourceCost spirit, StatusType marker,
                               SkillPhase proc, List<SkillModifier> modifiers, SkillPhase... phases) {
        return herald(require, spirit, marker, proc, List.of(GateTag.AURA, GateTag.HERALD), modifiers, phases);
    }

    /** 捷（带完整分类标签）：{@code tags} 覆盖默认的 {@code aura}/{@code herald}，须自带二者再加真值标签。 */
    public static Skill herald(Require require, ResourceCost spirit, StatusType marker,
                               SkillPhase proc, List<String> tags,
                               List<SkillModifier> modifiers, SkillPhase... phases) {
        SkillPhase base = withMarker(phases.length > 0 ? phases[0] : selfBuffPhase(), marker);
        if (proc != null) base = withChild(base, proc);
        return of(require, spirit, tags, modifiers, base);
    }

    /** 在相位下追加一个子相位。 */
    public static SkillPhase withChild(SkillPhase phase, SkillPhase child) {
        List<SkillPhasePlayer> children = new ArrayList<>(phase.children());
        children.add(new SkillPhasePlayer(Optional.empty(), child));
        return new SkillPhase(phase.delivery(), phase.actions(), children, phase.eventListen());
    }

    /**     * 在相位动作表头部追加一条 ON_CAST 的 StatAction（StatusStatEffect 存在性标记）。
     * 置于头部使标记先于属性增益落地，同 tick 内读取即一致；施加为幂等刷新，重放不会叠层。
     */
    public static SkillPhase withMarker(SkillPhase phase, StatusType marker) {
        if (marker == null) return phase;        TriggeredAction action = new TriggeredAction(LifecycleTrigger.ON_CAST,
                new StatAction(new StatGroup(java.util.List.of(StatusStatEffect.selfEntry(marker, ExpressionCodec.of(1.0)))),
                        false, java.util.Optional.empty(), ExpressionCodec.of(0.0)));
        List<TriggeredAction> actions = new ArrayList<>(phase.actions());
        actions.add(0, action);
        return new SkillPhase(phase.delivery(), actions, phase.children(), phase.eventListen());
    }
}
