package com.cimdy.coe.data.codec.skill.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.skill.support.SupportScope;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Comparator;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 技能链接解析器 — 将辅助宝石效果应用到技能数据上。
 * 辅助宝石列表完全由玩家的技能装配数据（SkillData.supports）决定。
 *
 * <p>装配期只处理「特殊修正」（SpecialMod：等级/品质/速度/消耗）与「注入动作」
 * （AdditionalActionEffect）。运行时投递/效果修正（DeliveryMod / EffectMod）不在此处理，
 * 而是由 SupportProfile 在施放时逐次查询。
 */
public class SkillLinkResolver {

    /**
     * PoE2 Allow Type 词 → 本模组机制标签（小写）。补全随 M5 数据填充；未知词走小写原词。
     * 仅作语义桥接，使 {@link SupportScope} 的后缀布尔求值能命中本模组的 {@code Skill.tags()}。
     */
    private static final Map<String, String> POE2_TO_MOD_TAG = Map.ofEntries(
            Map.entry("spell", "spell"), Map.entry("attack", "attack"),
            Map.entry("melee", "melee"), Map.entry("physical", "physical"),
            Map.entry("fire", "fire"), Map.entry("cold", "cold"),
            Map.entry("lightning", "lightning"), Map.entry("chaos", "chaos"),
            Map.entry("aoe", "aoe"), Map.entry("projectile", "projectile"),
            Map.entry("warcry", "warcry"), Map.entry("banner", "banner"),
            Map.entry("bow", "bow"), Map.entry("curse", "curse"),
            Map.entry("mark", "mark"), Map.entry("herald", "herald"),
            Map.entry("buff", "buff"), Map.entry("aura", "aura"),
            Map.entry("minion", "minion"), Map.entry("totem", "totem"),
            Map.entry("trap", "trap"), Map.entry("mine", "mine"),
            Map.entry("trigger", "trigger"), Map.entry("channel", "channel"),
            Map.entry("channelling", "channel"), Map.entry("duration", "duration"),
            Map.entry("slam", "slam"), Map.entry("strike", "strike"),
            Map.entry("travel", "travel"), Map.entry("wind", "wind"),
            Map.entry("persistent", "persistent"),
            Map.entry("meleesingletarget", "melee"),
            Map.entry("crossbowammoskill", "crossbow"), Map.entry("crossbowskill", "crossbow")
    );

    /**
     * 兼容性闸口（ADR-008 阶段 2）：依据辅助宝石的 Allow Type 表达式（PoE2 详情页「Supported Gem」语义）
     * 过滤掉不能辅助本技能的辅助宝石。无 allow_type 的辅助宝石（数据未填或"支持一切"）一律保留。
     *
     * <p>求值委托 {@link SupportScope}（后缀布尔表达式，操作符仅 AND/OR，隐式连接符 OR），词汇先转小写再经
     * {@link #POE2_TO_MOD_TAG} 归一。
     *
     * <p><b>Fail-open（防误杀）</b>：若任一 Allow Type 词无法映射到本模组已知标签（词表缺口），或技能自身无 tags
     * （数据缺失），该辅助宝石<b>保留</b>而非丢弃——宁可放过、不可误拒。仅当所有词均可映射且表达式明确判 DENIED
     * 时才丢弃。词表随 M5 数据填充逐步补全。表达式异常同样放过（不崩溃装配）。
     */
    /** 兼容性拒绝记录（诊断用，非静默）。 */
    public record Rejection(String gemTag, String reason) {}

    /** 兼容求解结果：保留的宝石列表 + 拒绝原因（供装配期 debug 日志）。 */
    public record CompatResult(List<SupportGem> kept, List<Rejection> rejections) {
        public static CompatResult all(List<SupportGem> gems) {
            return new CompatResult(gems, List.of());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillLinkResolver.class);

    private enum TagVerdict { KEEP, REJECT, UNCONSTRAINED }

    /**
     * 声明式约束求解器（P3，蓝图 §7.5.2）：在装配期统一校验辅助宝石与技能、辅助宝石之间的兼容性，
     * 取代硬编码白名单（Q1 方案 C）。
     *
     * <p>校验维度：
     * <ul>
     *   <li><b>targets</b>（allow_type）：技能类型表达式，不匹配则拒绝（沿用原 filterCompatible 语义）；</li>
     *   <li><b>requires</b>：技能必须满足条件，不满足则拒绝；</li>
     *   <li><b>forbids</b>：技能命中禁止条件则拒绝；</li>
     *   <li><b>consumes / produces</b> 跨宝石互斥：同一资源被多颗宝石引用（消费或生产）时，仅保留 tag 字典序最小者（顺序无关）。</li>
     * </ul>
     *
     * <p><b>Fail-open（防误杀）</b>：任一约束词无法映射到本模组已知标签（词表缺口）、表达式为隐式无约束、
     * 或表达式求值异常时，对应宝石<b>保留</b>而非丢弃——宁可放过、不可误拒（与原 allow_type 策略一致）。
     */
    public static CompatResult filterCompatibleDetailed(Skill base, List<SupportGem> supportGems) {
        if (supportGems == null || supportGems.isEmpty())
            return CompatResult.all(supportGems == null ? List.of() : supportGems);
        Set<String> skillTags = base.tags().stream().map(String::toLowerCase).collect(Collectors.toSet());
        // 形态（Totem/Trap/Mine）作为技能级标签注入，使 targets/requires/forbids 表达式可针对形态判定兼容性。
        base.form().ifPresent(f -> skillTags.add(f.toLowerCase()));
        List<Rejection> rejections = new ArrayList<>();
        List<SupportGem> survivors = new ArrayList<>();
        for (SupportGem gem : supportGems) {
            // targets（allow_type）：不匹配技能类型则拒绝
            if (evalTagExpr(gem.allowType(), skillTags, null,
                    "targets/incompatible skill type", rejections, gem) == TagVerdict.REJECT) continue;
            // requires：技能不满足必要条件则拒绝
            if (evalTagExpr(gem.requires(), skillTags, null,
                    "requires unmet", rejections, gem) == TagVerdict.REJECT) continue;
            // forbids：技能命中禁止条件则拒绝
            if (evalTagExpr(gem.forbids(), skillTags,
                    "forbidden by skill tag", null, rejections, gem) == TagVerdict.REJECT) continue;
            survivors.add(gem);
        }
        // 跨宝石 consumes/produces 资源互斥裁决：同一资源被多颗宝石引用时仅保留 tag 字典序最小者
        Map<String, List<SupportGem>> byResource = new HashMap<>();
        for (SupportGem g : survivors) {
            g.consumes().ifPresent(list -> list.forEach(r ->
                    byResource.computeIfAbsent(r.toLowerCase(), k -> new ArrayList<>()).add(g)));
            g.produces().ifPresent(list -> list.forEach(r ->
                    byResource.computeIfAbsent(r.toLowerCase(), k -> new ArrayList<>()).add(g)));
        }
        Set<SupportGem> dropped = new HashSet<>();
        for (Map.Entry<String, List<SupportGem>> e : byResource.entrySet()) {
            List<SupportGem> refs = e.getValue();
            if (refs.size() <= 1) continue;
            SupportGem kept = refs.stream()
                    .min(Comparator.comparing(g -> g.tag().orElse("~"))).orElse(refs.get(0));
            for (SupportGem g : refs) {
                if (g != kept) {
                    dropped.add(g);
                    rejections.add(new Rejection(gemTag(g),
                            "resource '" + e.getKey() + "' conflict with " + gemTag(kept)));
                }
            }
        }
        List<SupportGem> kept = survivors.stream().filter(g -> !dropped.contains(g)).toList();
        return new CompatResult(kept, rejections);
    }

    /**
     * 标签表达式求值（targets/requires/forbids 共用）：把表达式词归一后经 {@link SupportScope} 求值。
     * @param expr            表达式（allow_type / requires / forbids），空表示无约束
     * @param skillTags       技能标签 + 形态集合
     * @param rejectIfMatch   命中表达式时的拒绝原因（forbids 用）；null 表示命中不拒绝
     * @param rejectIfNoMatch 未命中表达式时的拒绝原因（requires/targets 用）；null 表示未命中不拒绝
     */
    private static TagVerdict evalTagExpr(Optional<List<String>> expr, Set<String> skillTags,
                                         String rejectIfMatch, String rejectIfNoMatch,
                                         List<Rejection> rejections, SupportGem gem) {
        if (expr.isEmpty()) return TagVerdict.UNCONSTRAINED;
        List<String> norm = expr.get().stream()
                .map(t -> SupportScope.isOperator(t) ? t
                        : POE2_TO_MOD_TAG.getOrDefault(t.toLowerCase(), t.toLowerCase()))
                .toList();
        try {
            SupportScope.Compiled compiled = SupportScope.compile(norm);
            if (compiled.unconstrained()) return TagVerdict.UNCONSTRAINED;
            Set<String> unknown = new HashSet<>();
            boolean match = compiled.matches(skillTags, SupportScope.UnknownPolicy.FALSE, unknown);
            if (!unknown.isEmpty()) return TagVerdict.KEEP; // 词表缺口 → 放过（fail-open）
            if (rejectIfMatch != null && match) {
                rejections.add(new Rejection(gemTag(gem), rejectIfMatch));
                return TagVerdict.REJECT;
            }
            if (rejectIfNoMatch != null && !match) {
                rejections.add(new Rejection(gemTag(gem), rejectIfNoMatch));
                return TagVerdict.REJECT;
            }
            return TagVerdict.KEEP;
        } catch (Exception e) {
            return TagVerdict.KEEP; // 表达式异常 → 放过，不阻断装配
        }
    }

    private static String gemTag(SupportGem gem) {
        return gem.tag().orElse("<untagged>");
    }

    /**
     * 兼容性闸口（保留原签名，装配期调用）：返回经 {@link #filterCompatibleDetailed} 过滤后的宝石列表。
     */
    public static List<SupportGem> filterCompatible(Skill base, List<SupportGem> supportGems) {
        return filterCompatibleDetailed(base, supportGems).kept();
    }

    /** 计算辅助宝石总魔力消耗倍率（mana_cost_multiplier 连乘，作用于 MANA/ENERGY/HEALTH 消耗）。 */
    public static float getTotalManaCostMultiplier(List<SupportGem> supportGems) {
        return supportGems.stream()
                .map(SupportGem::getManaCostMultiplier)
                .reduce(1.0f, (a, b) -> a * b);
    }

    /** 计算辅助宝石总精魂保留倍率（reservation_multiplier 连乘，仅作用于 SPIRIT 保留）。 */
    public static float getTotalReservationMultiplier(List<SupportGem> supportGems) {
        return supportGems.stream()
                .map(SupportGem::getReservationMultiplier)
                .reduce(1.0f, (a, b) -> a * b);
    }

    /**
     * 计算辅助宝石总速度倍率（连乘），写入 {@link SkillCast#speedMultiplier}，
     * 最终使施放时间变为 {@code base / multiplier}（见 {@code SkillCast#getCastTime}）。
     *
     * <p><b>按技能类型二选一</b>（POE2 三速度维度语义，详见 {@link SpecialMod}）：
     * <ul>
     *   <li>{@code skillSpeedMultiplier}（Skill Speed）：对攻击与法术<b>皆</b>生效，无条件连乘。</li>
     *   <li>攻击技能（{@code cast().left() = Attack}）取 {@code attackSpeedMultiplier}；</li>
     *   <li>法术技能（{@code cast().right() = Spell}）取 {@code castSpeedMultiplier}；</li>
     *   <li>另一通道<b>不参与</b>——否则「对法术的施法速度惩罚」会错误地落到攻击技能上，
     *       且把 Skill Speed 拆成两通道同设 0.8 会得到 0.64（−36%）而非 −20%。</li>
     * </ul>
     *
     * @param base 基础技能（用于判定攻击 / 法术类型）
     */
    public static float getTotalSpeedMultiplier(Skill base, List<SupportGem> supportGems) {
        boolean isAttack = base.cast().cast().left().isPresent();
        double m = 1.0;
        for (SupportGem gem : supportGems) {
            if (gem.special().isPresent()) {
                SpecialMod s = gem.special().get();
                m *= s.getSkillSpeedMultiplier();
                m *= isAttack ? s.getAttackSpeedMultiplier() : s.getCastSpeedMultiplier();
            }
        }
        return (float) m;
    }

    /**
     * 计算辅助宝石总等级加成。
     * @param supportGems 已解析的辅助宝石定义列表
     * @param supportLevels 辅助宝石自身等级列表（与 supportGems 顺序一致）
     */
    public static int getTotalLevelBonus(List<SupportGem> supportGems, List<Integer> supportLevels) {
        int total = 0;
        for (int i = 0; i < supportGems.size() && i < supportLevels.size(); i++) {
            SupportGem gem = supportGems.get(i);
            if (gem.special().isPresent()) {
                total += gem.special().get().getAdditionalLevels(supportLevels.get(i));
            }
        }
        return total;
    }

    /**
     * 计算辅助宝石总品质加成（对应 PoE 的 Enhance：提升链接技能品质）。
     */
    public static int getTotalQualityBonus(List<SupportGem> supportGems) {
        double total = 0;
        for (SupportGem gem : supportGems) {
            if (gem.special().isPresent()) total += gem.special().get().getQualityBonus();
        }
        return (int) Math.round(total);
    }

    /**
     * 提取技能自身与所有辅助宝石注入的额外动作（二者共用 AdditionalActionEffect 形状）。
     * @param base 已解析的基础技能（其 modifiers 可能包含 AdditionalActionEffect）
     */
    public static List<AdditionalActionEffect> getAdditionalActions(Skill base, List<SupportGem> supportGems) {
        List<AdditionalActionEffect> result = new ArrayList<>();
        for (SkillModifier m : base.modifiers()) {
            if (m instanceof AdditionalActionEffect a) result.add(a);
        }
        for (SupportGem gem : supportGems) {
            for (SkillModifier m : gem.effects()) {
                if (m instanceof AdditionalActionEffect a) result.add(a);
            }
        }
        return result;
    }

    /**
     * 提取技能自身与所有辅助宝石携带的事件监听（EventListenEffect，二者共用同一形状）。
     * 用于把「Cast on Crit / Cast on Ignite」等元宝石的事件订阅注入到被辅助技能。
     */
    public static List<EventListenEffect> collectEventListens(Skill base, List<SupportGem> supportGems) {
        List<EventListenEffect> result = new ArrayList<>();
        for (SkillModifier m : base.modifiers()) {
            if (m instanceof EventListenEffect e) result.add(e);
        }
        for (SupportGem gem : supportGems) {
            for (SkillModifier m : gem.effects()) {
                if (m instanceof EventListenEffect e) result.add(e);
            }
        }
        return result;
    }

    /**
     * 将多个 EventListenEffect 合并后注入到技能的每一个阶段。
     * 每个阶段的原有 eventListen（若有）会与注入的监听再合并（动作列表拼接）。
     */
    public static Skill applyEventListens(Skill skill, List<EventListenEffect> effects) {
        if (effects.isEmpty()) return skill;
        List<EventListenPhase> listens = effects.stream().map(EventListenEffect::eventListen).toList();
        EventListenPhase merged = mergeEventListens(listens);
        List<SkillPhasePlayer> newPhases = new ArrayList<>();
        for (SkillPhasePlayer player : skill.phases()) {
            SkillPhase phase = player.phase();
            EventListenPhase combined = phase.eventListen()
                    .map(existing -> mergeEventListens(List.of(existing, merged)))
                    .orElse(merged);
            SkillPhase modified = new SkillPhase(phase.delivery(), phase.actions(),
                    phase.children(), Optional.of(combined));
            newPhases.add(new SkillPhasePlayer(player.condition(), modified));
        }
        return new Skill(skill.require(), skill.equipment(), skill.animation(), skill.cast(),
                skill.input(), skill.cost(), newPhases, skill.modifiers(), skill.tier(), skill.tags(),
                skill.form(), skill.aoeRanges(),
                skill.params(), skill.castSound(), skill.castParticle());
    }

    /** 合并多个事件监听：动作列表拼接，事件类型/事件名/过滤/冷却取首个（同插槽内假定一致）。 */
    private static EventListenPhase mergeEventListens(List<EventListenPhase> list) {
        EventListenPhase first = list.get(0);
        List<TriggeredAction> actions = new ArrayList<>();
        for (EventListenPhase p : list) actions.addAll(p.actions());
        return new EventListenPhase(first.eventType(), first.filter(), actions,
                first.internalCooldown(), first.maxTriggersPerCast(), first.eventName());
    }

    /**
     * 将消耗倍率、速度倍率与合并后的需求应用到基础技能上。
     *
     * @param require           合并了辅助宝石额外需求后的最终技能需求
     * @param manaCostMult      魔力消耗倍率（作用于 MANA/ENERGY/HEALTH 消耗）
     * @param reservationMult   精魂保留倍率（仅作用于 SPIRIT 保留）
     */
    public static Skill applyToSkill(Skill base, Require require, float manaCostMult, float reservationMult, float speedMult) {
        List<ResourceCost> resolvedCost = base.cost().stream()
                .map(cost -> scaleCost(cost, manaCostMult, reservationMult))
                .toList();
        SkillCast resolvedCast = base.cast().withSpeedMultiplier(speedMult);
        return new Skill(
                require, base.equipment(), base.animation(),
                resolvedCast, base.input(), resolvedCost, base.phases(), base.modifiers(), base.tier(), base.tags(),
                base.form(), base.aoeRanges(),
                base.params(), base.castSound(), base.castParticle());
    }

    /**
     * 将额外的 TriggeredAction 注入到技能的所有阶段中。
     * <p>若 AdditionalActionEffect 指定了 phaseIndex，则只注入到对应阶段；
     * 否则注入到所有阶段（默认行为）。
     *
     * @return 注入了额外动作后的新 Skill（不可变）
     */
    public static Skill applyAdditionalActions(Skill skill, List<AdditionalActionEffect> additionalActions) {
        if (additionalActions.isEmpty()) return skill;
        List<SkillPhasePlayer> oldPhases = skill.phases();
        List<SkillPhasePlayer> newPhases = new ArrayList<>();
        for (int i = 0; i < oldPhases.size(); i++) {
            SkillPhasePlayer player = oldPhases.get(i);
            SkillPhase phase = player.phase();
            // 收集应注入本阶段的动作
            List<TriggeredAction> injectActions = new ArrayList<>();
            for (AdditionalActionEffect aae : additionalActions) {
                if (aae.phaseIndex().isEmpty() || aae.phaseIndex().get().getInt() == i) {
                    injectActions.add(aae.action());
                }
            }
            if (injectActions.isEmpty()) {
                newPhases.add(player);
            } else {
                List<TriggeredAction> merged = new ArrayList<>(phase.actions());
                merged.addAll(injectActions);
                SkillPhase modifiedPhase = new SkillPhase(
                        phase.delivery(), merged, phase.children(), phase.eventListen());
                newPhases.add(new SkillPhasePlayer(player.condition(), modifiedPhase));
            }
        }
        return new Skill(skill.require(), skill.equipment(), skill.animation(),
                skill.cast(), skill.input(), skill.cost(), newPhases, skill.modifiers(), skill.tier(), skill.tags(),
                skill.form(), skill.aoeRanges(),
                skill.params(), skill.castSound(), skill.castParticle());
    }

    /**
     * 将所有辅助宝石效果完整应用到基础技能。
     * 包括：消耗倍率、速度倍率、附加动作注入。
     */
    public static Skill applyAll(Skill base, List<SupportGem> supportGems) {
        CompatResult result = filterCompatibleDetailed(base, supportGems);
        if (!result.rejections().isEmpty() && LOGGER.isDebugEnabled()) {
            for (Rejection r : result.rejections()) {
                LOGGER.debug("SkillLinkResolver: support gem '{}' rejected: {}", r.gemTag(), r.reason());
            }
        }
        supportGems = result.kept();
        float manaCostMult = getTotalManaCostMultiplier(supportGems);
        float reservationMult = getTotalReservationMultiplier(supportGems);
        float speedMult = getTotalSpeedMultiplier(base, supportGems);
        List<AdditionalActionEffect> actions = getAdditionalActions(base, supportGems);
        Require mergedRequire = mergeRequire(base.require(), supportGems);
        Skill mid = applyToSkill(base, mergedRequire, manaCostMult, reservationMult, speedMult);
        Skill withActions = applyAdditionalActions(mid, actions);
        return applyEventListens(withActions, collectEventListens(base, supportGems));
    }

    /**
     * 将所有辅助宝石的额外属性需求累加到基础技能需求上。
     * 多个辅助宝石可叠加（如两个 +5 智慧 -> +10 智慧）。
     */
    public static Require mergeRequire(Require base, List<SupportGem> supportGems) {
        Require merged = base;
        for (SupportGem gem : supportGems) {
            merged = gem.require().addToRequire(merged);
        }
        return merged;
    }

    /**
     * 按资源类型分别应用倍率：SPIRIT（精魂保留）用 reservationMult，其余（魔力/能量/生命消耗）用 manaCostMult。
     * 二者独立，使辅助宝石可「仅降低消耗」或「仅降低保留」。
     */
    private static ResourceCost scaleCost(ResourceCost cost, float manaCostMult, float reservationMult) {
        float multiplier = (cost.type() == ResourceType.SPIRIT) ? reservationMult : manaCostMult;
        if (multiplier == 1.0f) return cost;
        return new ResourceCost(cost.type(), cost.usage(), scaleValue(cost.value(), multiplier));
    }

    private static ExpressionCodec.Value scaleValue(ExpressionCodec.Value value, float multiplier) {
        Either<Double, MathExpression> either = value.value();
        if (either.left().isPresent()) {
            return ExpressionCodec.of(either.left().get() * multiplier);
        }
        return new ExpressionCodec.Value(Either.right(
                new ExpressionCodec.BinaryOperation.Multiply(either.right().get(),
                        new ExpressionCodec.Constant(multiplier))));
    }
}
