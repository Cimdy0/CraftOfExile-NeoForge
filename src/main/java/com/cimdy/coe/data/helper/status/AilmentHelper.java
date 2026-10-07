package com.cimdy.coe.data.helper.status;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.TargetRarityValue;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.codec.support.status.runtime.StatusBehavior;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityTypeValue;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.def.AilmentSpec;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.registry.DamageTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 战斗状态效果施加库 —— 全工程「唯一」的异常/状态效果施加入口。
 *
 * <p>设计目标（消除多重实现、回调 Context）：
 * <ul>
 *   <li>所有异常（流血/点燃/中毒/冰缓/冰冻/感电/眩晕/电击）与状态 debuff（元素曝露/减速）
 *       的 {@code MobEffectInstance} 构造、属性判定、数值计算都集中在本类，禁止在
 *       {@code DamageEvent} / {@code DamageAction} / {@code AilmentAction} 等处重复构造；</li>
 *   <li>本类在「成功施加某异常」时统一向施法者总线发布元事件（on_ignite / on_freeze / on_shock），
 *       作为回调 Context 的唯一来源，不再由各调用方各自 {@code SkillEventBus.publishMeta}；</li>
 *   <li>伤害衍生异常（applyDamageAilment / applyColdAilment / applyLightAilment / applyStunAilment）
 *       由权威战斗链路 {@code DamageEvent} 调用，技能显式异常（applyXxxAilment 的 skill 重载）
 *       由 {@code AilmentAction} 调用 —— 两条入口最终都走同一套施加逻辑，语义一致。</li>
 * </ul>
 */
public class AilmentHelper {
    /** 伤害衍生异常（流血/点燃/中毒）的施加结果，供 DamageEvent 据此统一发布 on_ignite 元事件。 */
    public record DamageAilmentResult(boolean bleeding, boolean ignite, boolean poison) {}

    /**
     * 元事件发布（深度合并核心）：仅当来自某个技能（skill 非空）且施法者非空时发布，
     * 原版攻击没有技能上下文则不触发元宝石。供伤害衍生异常从 {@link DamageSource} 推导、
     * 技能显式异常从显式 (skill, caster) 直接发布，二者共用同一实现。
     */
    private static void publishMeta(DamageSource source, String event, String... tags) {
        if (source instanceof SkillDamageSource sds && sds.getSkill() != null) {
            LivingEntity caster = source.getEntity() instanceof LivingEntity l ? l : null;
            SkillEventBus.publishMeta(caster, sds.getSkill(), event, tags);
        }
    }

    private static void publishMeta(SkillInstance skill, LivingEntity caster, String event, String... tags) {
        SkillEventBus.publishMeta(caster, skill, event, tags);
    }

    /**
     * 异常事件回调闭环（<b>本类唯一的异常事件出口</b>，复刻 {@code DamageEvent} 的 on_kill 范式，
     * 落地用户两阶段循环模型的 ⑤⑥ 环节）：在某异常<b>真实生效之后</b>调用一次，
     * 一步完成「元事件 + 造成侧 + 承受侧」三写，调用方不再分别拼装：
     * <ol>
     *   <li><b>元事件</b>：{@code publishMeta}——供元宝石（{@code EventListenPhase}）消费；</li>
     *   <li><b>造成侧</b>（owner=attacker）：写计时键 {@code caused_ailment} 供 recently/count 查询，
     *       并 fire 携带 {@code caused_ailment + 异常名 + 形态标签} 的作用域，
     *       驱动 on-cause 效果（如「造成冰缓时回复 X% 最大生命」）；</li>
     *   <li><b>承受侧</b>（owner=victim）：与造成侧完全对称的 {@code taken_ailment} 通道，
     *       驱动 on-taken 效果（如「被冰缓时回复 X% 最大生命」）。</li>
     * </ol>
     * 任一侧 owner 为空自动跳过（如环境眩晕无 attacker）。与读取型 fold 通道正交：fold 只查 Context 不写。
     *
     * @param source    伤害源（推导技能标识与形态标签）
     * @param attacker  造成异常者（可空）
     * @param victim    承受异常者（可空）
     * @param ailmentId 异常 id（作为具体异常名标签；分类标签由 GateTag.match 层级汇总）
     */
    private static void fireAilment(DamageSource source, @Nullable LivingEntity attacker,
                                    @Nullable LivingEntity victim, String ailmentId) {
        fireAilment(source, attacker, victim, ailmentId, false);
    }

    /**
     * @param fresh 本次为<b>新施加</b>（目标施加前不处于该异常）而非刷新已有异常。
     *              为真时事件额外携带 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag#FRESH_AILMENT}
     *              标签，供「对未处于 X 的目标造成 X 时」一族真值精确门控
     *              （如 Siphon Elements「Igniting a <b>non-Ignited</b> target」）。
     */
    private static void fireAilment(DamageSource source, @Nullable LivingEntity attacker,
                                    @Nullable LivingEntity victim, String ailmentId, boolean fresh) {
        publishMeta(source, EventName.CAUSED_AILMENT, ailmentId);
        Entity direct = source.getDirectEntity();
        Identifier skill = SkillDamageSource.skillIdOf(source);
        Set<String> extra = fresh ? Set.of(GateTag.FRESH_AILMENT) : Set.of();
        GameEvent.ailmentCaused(source, direct, EventName.CAUSED_AILMENT, ailmentId)
                .fireCaused(attacker, victim, skill, extra);
        GameEvent.ailmentTaken(source, direct, EventName.TAKEN_AILMENT, ailmentId).fireTaken(victim, attacker, skill);
    }

    /**
     * 异常事件回调闭环——技能显式入口（无 {@link DamageSource}，形态标签退化为空，其余与
     * {@link #fireAilment(DamageSource, LivingEntity, LivingEntity, String)} 完全一致）。
     * 由 {@code AilmentAction} 等直接施加异常的技能动作调用。
     */
    private static void fireAilmentBySkill(@Nullable SkillInstance skill, @Nullable LivingEntity caster,
                                           @Nullable LivingEntity target, String ailmentId) {
        if (skill != null) publishMeta(skill, caster, EventName.CAUSED_AILMENT, ailmentId);
        Identifier skillId = SkillDamageSource.skillIdOf(skill);
        GameEvent.ofPlain(EventName.CAUSED_AILMENT, ailmentId).fireCaused(caster, target, skillId);
        GameEvent.ofPlain(EventName.TAKEN_AILMENT, ailmentId).fireTaken(target, caster, skillId);
    }

    /**
     * 技能经 {@code StatAction} 施加异常时补发的造成/承受事件闭环（{@code CAUSED_AILMENT} + ailmentId）。
     * 异常行为（减速/定身/破甲等）由 {@link StatusEngine#apply} 落地，但造成侧事件此前未触发，
     * 导致依赖 {@code CAUSED_AILMENT}+具体标签的 {@code GrantOnEvent}（如重晕/钉刺破甲）在技能驱动路径下收不到事件。
     * 此处统一补发，使所有经技能施加的异常都能真实触发造成侧回调。
     */
    public static void fireStatusCaused(@Nullable LivingEntity attacker, @Nullable LivingEntity victim, String ailmentId) {
        fireAilmentBySkill(null, attacker, victim, ailmentId);
    }

    /**
     * 重晕条（Stun Bar）击穿触发重晕时的造成/承受对称闭环（由 {@link ResourceRegistry#accumulateStun} 调用）。
     * 重晕本就是异常，故复用 {@code CAUSED_AILMENT / TAKEN_AILMENT} 事件名 + {@code heavy_stun} 标签区分，
     * 不另立独立事件名。attacker 可为空（环境眩晕），此时仅记录承受侧。
     */
    public static void fireStunEvents(@Nullable LivingEntity attacker, LivingEntity victim) {
        fireAilmentBySkill(null, attacker, victim, GateTag.HEAVY_STUN);
    }

    // ───────────────────────────── 伤害衍生异常（由 DamageEvent 调用） ─────────────────────────────

    public static DamageAilmentResult applyDamageAilment(
            DamageSource source, LivingEntity living, LivingEntity target, RandomSource random, float[] damages, boolean deflected) {
        // POE2 积蓄模型：各异常按「命中对应元素伤害 / 异常门槛」的比例积累积蓄条（见各 applyXxx 方法），
        // 积蓄满门槛即触发（点燃/中毒走 THRESHOLD 积累；流血保留 POE2 固定几率，不随门槛缩放）。
        boolean hadIgnite = StatusEngine.has(living, StatusDefs.IGNITE);
        boolean bleeding = applyBleedingAilment(living, target, random, damages, deflected);
        boolean ignite = applyIgniteAilment(source, living, target, random, damages);
        boolean poison = applyPoisonAilment(living, target, random, damages);
        applyBlindAilment(source, living, target, random);
        // 致残（命中型 debuff，固定几率，镜像流血）：{attack, maim} 作用域读 AILMENT_CHANCE，偏斜命中按 DEFLECTED_CANNOT_INFLICT_MAIM 闸门跳过。
        applyMaimAilment(living, target, random, deflected);
        // 造成型事件回调闭环（attacker=target 攻击者，victim=living 受害者）：存 Context + fire 触发 on-cause 效果
        if (bleeding) {
            fireAilment(source, target, living, StatusDefs.BLEED.id);
        }
        if (ignite) {
            fireAilment(source, target, living, StatusDefs.IGNITE.id, !hadIgnite);
        }
        if (poison) {
            fireAilment(source, target, living, StatusDefs.POISON.id);
        }
        return new DamageAilmentResult(bleeding, ignite, poison);
    }

    /**
     * 流血（CHANCE 固定几率）：POE2 中唯一仍走「每次命中固定几率」的伤害性异常，不随门槛缩放。
     * 命中物理伤害经「贡献」模型聚合为分量数组，几率通过后施加 DoT（magnitude 取物理分量）。
     */
    private static boolean applyBleedingAilment(LivingEntity living, LivingEntity target, RandomSource random, float[] damages, boolean deflected) {
        // 偏斜命中不可对承受方施加流血（DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU）：
        // 命中被格挡/偏斜且承受方持该标志 ≥1.0 时，本应施加的流血直接跳过（AilmentHelper 真读，非死线，R3 安全）。
        if (deflected && StatEngine.gateOf(living, "deflected_cannot_inflict", Set.of("bleed"))) {
            return false;
        }
        int bleedingChance = AilmentHelper.getBleedingChance(target);
        double[] bleedingDamage = AilmentHelper.getBleedingDamage(target, damages);
        double total = 0; for (double c : bleedingDamage) total += c;
        if (total > 0 && random.nextInt(100) < bleedingChance) {
            if (avoidsPublic(living, random, StatusDefs.BLEED.id)) return false;
            applyBleedingAilment(living, target, bleedingDamage, 5);
            StatusEngine.clear(living, StatusDefs.INCISION);
            return true;
        }
        return false;
    }

    /**
     * 致残（MAIM，命中型 debuff，固定几率）：镜像 {@link #applyBleedingAilment}——在 {attack, maim} 作用域读
     * {@code AILMENT_CHANCE}（由 EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_CHANCE_MAIM 等条目提供），命中则施加 MAIM 状态。
     * 此前该几率属性已接线却无施加点（死效果），本方法将其落地；偏斜命中按 DEFLECTED_CANNOT_INFLICT_MAIM 闸门跳过（R3 安全）。
     */
    private static boolean applyMaimAilment(LivingEntity living, LivingEntity target, RandomSource random, boolean deflected) {
        if (deflected && StatEngine.gateOf(living, "deflected_cannot_inflict", Set.of("maim"))) {
            return false;
        }
        int maimChance = getMaimChance(target);
        if (maimChance > 0 && random.nextInt(100) < maimChance) {
            if (avoidsPublic(living, random, StatusDefs.MAIM.id)) return false;
            // POE2 真值（#MAIM-B1）：强度 30.0 百分点 = 30% reduced 移速（+ 固定 15% reduced 闪避由 maim 行为给出），时长 4 秒 = 80 ticks
            StatusEngine.apply(living, StatusDefs.MAIM, target, StatusDefs.MAIM_DEFAULT_MAGNITUDE,
                    StatusDefs.MAIM_DEFAULT_DURATION_TICKS, 1);
            return true;
        }
        return false;
    }

    private static int getMaimChance(LivingEntity living) {
        Set<String> tags = Set.of(GateTag.ATTACK, GateTag.MAIM);
        return (int) (withScope(living, tags, AttributeRegistry.AILMENT_CHANCE) * 100);
    }

    public static void applyBleedingAilment(LivingEntity living, LivingEntity attacker, float damage, int duration) {
        // 直接施加（无来源伤害拆分）：归物理槽（流血默认来源=物理）
        double[] comp = {damage, 0, 0, 0, 0};
        applyBleedingAilment(living, attacker, comp, duration);
    }

    public static void applyBleedingAilment(LivingEntity living, LivingEntity attacker, double[] comp, int duration) {
        // 共享 DoT 施加原语（时长修正 / 加速 / 统一入口），见 {@link #applyDamageDoT}
        applyDamageDoT(living, attacker, StatusDefs.BLEED, comp, duration);
    }

    /**
     * 点燃（THRESHOLD 积蓄）：命中的火焰伤害按「占异常门槛比例」向点燃积蓄条增加，
     * 积蓄满门槛（=1.0）即触发点燃 DoT（magnitude 取火伤分量，由 StatusBehavior 按百分比结算）。
     * 大命中优势：单次高火伤比多次小火伤更快积累至满（POE2 核心改动）。
     */
    private static boolean applyIgniteAilment(DamageSource source, LivingEntity living, LivingEntity target, RandomSource random, float[] damages) {
        double[] igniteDamage = AilmentHelper.getIgniteDamage(source, target, damages);
        double total = 0; for (double c : igniteDamage) total += c;
        if (total <= 0) return false;
        if (avoidsPublic(living, random, StatusDefs.IGNITE.id)) return false;
        double threshold = StatusEngine.ailmentThreshold(living) * ailmentThresholdMul(living, StatusDefs.IGNITE)
                + ailmentThresholdAdd(living, StatusDefs.IGNITE);
        double gain = total / threshold * buildupMultiplier(target, StatusDefs.IGNITE);
        double buildup = StatusEngine.addBuildup(living, StatusDefs.IGNITE, gain);
        if (buildup >= 1.0) {
            applyIgniteAilment(living, target, igniteDamage, 4);
            StatusEngine.resetBuildup(living, StatusDefs.IGNITE, buildup - 1.0);
            return true;
        }
        return false;
    }

    public static void applyIgniteAilment(LivingEntity living, LivingEntity attacker, float damage, int duration) {
        // 直接施加（无来源伤害拆分）：归火焰槽（点燃默认来源=火焰）
        double[] comp = {0, 0, damage, 0, 0};
        applyIgniteAilment(living, attacker, comp, duration);
    }

    public static void applyIgniteAilment(LivingEntity living, LivingEntity attacker, double[] comp, int duration) {
        // 共享 DoT 施加原语（时长修正 / 加速 / 统一入口），见 {@link #applyDamageDoT}
        applyDamageDoT(living, attacker, StatusDefs.IGNITE, comp, duration);
    }

    /**
     * 中毒（THRESHOLD 积蓄、可叠层）：命中的(物理+混沌)伤害按「占异常门槛比例」向中毒积蓄条增加，
     * 积蓄满门槛即触发一层中毒 DoT（magnitude 取分量），溢出保留到下次积累。每层独立结算。
     */
    private static boolean applyPoisonAilment(LivingEntity living, LivingEntity target, RandomSource random, float[] damages) {
        double[] poisonDamage = AilmentHelper.getPoisonDamage(target, damages);
        double total = 0; for (double c : poisonDamage) total += c;
        if (total <= 0) return false;
        if (avoidsPublic(living, random, StatusDefs.POISON.id)) return false;
        double threshold = StatusEngine.ailmentThreshold(living) * ailmentThresholdMul(living, StatusDefs.POISON)
                + ailmentThresholdAdd(living, StatusDefs.POISON);
        double gain = total / threshold * buildupMultiplier(target, StatusDefs.POISON);
        double buildup = StatusEngine.addBuildup(living, StatusDefs.POISON, gain);
        if (buildup >= 1.0) {
            applyPoisonAilment(living, target, poisonDamage, 2);
            StatusEngine.resetBuildup(living, StatusDefs.POISON, buildup - 1.0);
            return true;
        }
        return false;
    }

    public static void applyPoisonAilment(LivingEntity living, LivingEntity attacker, float damage, int duration) {
        // 直接施加（无来源伤害拆分）：归混沌槽（中毒默认来源=物理+混沌；直接施加无来源时按混沌输出，与原机制一致）
        double[] comp = {0, damage, 0, 0, 0};
        applyPoisonAilment(living, attacker, comp, duration);
    }

    public static void applyPoisonAilment(LivingEntity living, LivingEntity attacker, double[] comp, int duration) {
        // 共享 DoT 施加原语（时长修正 / 加速 / 统一入口），见 {@link #applyDamageDoT}
        applyDamageDoT(living, attacker, StatusDefs.POISON, comp, duration);
    }

    /**
     * DoT 异常共享施加原语（bleed / ignite / poison 的直接施加路径共用）：
     * 时长 = 施加方（AILMENT_DURATION）× 承受方（taken 标签）× 基础时长；AILMENT_DAMAGE_RATE 加速（每秒伤害 ×N、时长 ÷N，总伤不变）；
     * 最终统一经 {@link StatusEngine#apply} 写入 StatusStore。异常类型定义性数值（每秒系数 / 默认来源槽位等）在 AilmentSpec 数据。
     *
     * @param duration 基础时长（秒）
     */
    private static void applyDamageDoT(LivingEntity living, LivingEntity attacker, StatusType type,
                                       double[] comp, int duration) {
        double durMul = (attacker != null ? ailmentDurationMul(attacker, living, type.id) : 1.0)
                * takenDurationMul(living, type.id);
        // 流血基础时长由 BASE_BLEEDING_DURATION 属性（秒，base=5.0）驱动：读承受方属性，无缝替代硬编码 5 秒缺省；
        // 其余 DoT（ignite/poison）沿用各自 duration 参数。AILMENT_DURATION 乘数与 AILMENT_DAMAGE_RATE 加速仍叠加其上。
        int durationTicks = (type == StatusDefs.BLEED)
                ? (int) (living.getAttributeValue(AttributeRegistry.BASE_BLEEDING_DURATION) * 20 * durMul)
                : (int) (duration * 20 * durMul);
        // 异常造成伤害加速（deal damage faster）：每秒伤害 ×N、持续时间 ÷N，总伤不变
        double rate = (attacker != null ? attacker.getAttributeValue(AttributeRegistry.AILMENT_DAMAGE_RATE) : 1.0);
        if (rate > 0 && rate != 1.0) {
            durationTicks = Math.max(1, (int) Math.round(durationTicks / rate));
            for (int i = 0; i < comp.length; i++) comp[i] *= rate;
        }
        if (durationTicks <= 0) return;
        // 登记「承受 DoT」计时事件（heartstopper 基石 ±50% DoT 门控 reads recently(taken_dot, window)）。
        // TAKEN_DOT 已列入 EventSubscription.MECHANICAL 恒记录，不依赖 stat 引用。
        Event.record(living, EventName.TAKEN_DOT);
        // 造成型 DoT 对称闭环（A-3 双向回调）：施加者（attacker）同样记录 + fire caused_dot，
        // 使「近期造成持续伤害」类 on-cause 效果（若有）可触发，并与 TAKEN_DOT 形成完整双向事件。
        if (attacker != null) {
            Event.record(attacker, EventName.CAUSED_DOT);
            StatEngine.fire(attacker, living, StatContext.of(Set.of("caused_dot"), null, EventName.CAUSED_DOT, null), null);
        }
        StatusEngine.apply(living, type, attacker, comp, durationTicks);
    }

    /**
     * 「事件施加异常」统一入口（供 {@code ApplyAilmentOnEventStatEffect} 等按事件+几率施加异常的 Stat 效果使用）：
     * 按 {@link AilmentSpec} 数据路由，<b>不再按状态 id switch</b>——
     * <ul>
     *   <li>DoT 异常（{@code perSecondCoeff > 0}，bleed/ignite/poison）：按 spec 默认来源槽位组装伤害分量，
     *       统一经 {@link #applyDamageDoT} 施加；</li>
     *   <li>控制类异常（有积蓄模式，chill/freeze/shock/electrocute）：强度 = spec.baseMagnitude × 100（clamp 1..100），
     *       经 {@link StatusEngine#apply} 施加并发布造成/承受事件；</li>
     *   <li>其余（无积蓄模式）：通用强度 1.0 施加。</li>
     * </ul>
     *
     * @param victim        承受者
     * @param attacker      施加者（可为 null）
     * @param status        异常状态类型
     * @param damage        伤害量（DoT 异常的强度基数）
     * @param durationTicks 持续时间（tick）
     */
    public static void applyStatusOnEvent(LivingEntity victim, LivingEntity attacker, StatusType status,
                                          float damage, int durationTicks) {
        AilmentSpec spec = status.ailment;
        if (spec == null || spec.mode() == AilmentSpec.BuildupMode.NONE) {
            StatusEngine.apply(victim, status, attacker, 1.0, durationTicks, 1);
            return;
        }
        if (spec.perSecondCoeff() > 0) {
            // DoT 异常：按 spec 默认来源槽位组装分量（直接施加路径，不参与积蓄）
            double[] comp = new double[DamageTypes.SLOT_CAPACITY];
            int slot = spec.defaultSourceSlot();
            if (slot >= 0 && slot < DamageTypes.width() && damage > 0) comp[slot] = damage;
            applyDamageDoT(victim, attacker, status, comp, Math.max(1, durationTicks / 20));
            return;
        }
        // 控制类异常：强度 = spec.baseMagnitude × 100（clamp 1..100），施加并发布造成/承受事件
        int magnitude = (int) Math.clamp(spec.baseMagnitude() * 100, 1, 100);
        StatusEngine.apply(victim, status, attacker, magnitude, durationTicks, 1);
        fireAilmentBySkill(null, attacker, victim, status.id);
    }

    /**
     * 冰霜命中：按 POE2 积蓄模型处理冰缓（PARTIAL 部分积累即时减速）与冰冻（THRESHOLD 满值触发定身）。
     * 冰霜伤害按「占异常门槛比例」计算 gain；冰缓每次命中直接减速（= gain×上限，封顶 50%），
     * 同时向冰冻积蓄条积累，满门槛即定身（大命中溢出可延长冰冻时长）。
     */
    public static boolean applyColdAilment(DamageSource source, LivingEntity living, LivingEntity target, float[] damages) {
        RandomSource random = living.getRandom();
        float coldDamage = getChillDamage(target, damages); // 冰霜贡献（冰缓/冰冻同源于冰霜）
        if (coldDamage <= 0) return false;
        double threshold = StatusEngine.ailmentThreshold(living) * ailmentThresholdMul(living, StatusDefs.CHILL)
                + ailmentThresholdAdd(living, StatusDefs.CHILL); // 冰缓/冰冻同源于冰霜，共用门槛
        double gain = coldDamage / threshold; // 占门槛比例（百分比进度）

        boolean applied = false;

        // 冰缓（PARTIAL）：每次命中直接按积累比例减速，无需满值（poe2db 上限 50%）
        if (!avoidsPublic(living, random, StatusDefs.CHILL.id)) {
            DamageContext.cacheDamageContext(source, living, new DamageContext().causeChill(true));
            // 强度 = 施加方（AILMENT_MAGNITUDE）× 承受方（taken 标签 AILMENT_MAGNITUDE，替代 CHILL_EFFECT_TAKEN）
            double chillEffect = ailmentMagnitude(target, StatusDefs.CHILL)
                    * takenMagnitude(living, StatusDefs.CHILL.id);
            // 大命中优势：单次冰霜伤害越大，冰缓减速越强（上限 50%）
            int chillPercent = (int) Math.clamp(gain * buildupMultiplier(target, StatusDefs.CHILL) * 50 * chillEffect, 1, 50);
            // 时长 = 施加方（AILMENT_DURATION）× 承受方（taken 标签 AILMENT_DURATION，替代 CHILL_DURATION/CHILL_DURATION_TAKEN）
            double chillDurMul = ailmentDurationMul(target, living, GateTag.CHILL)
                    * takenDurationMul(living, StatusDefs.CHILL.id);
            int chillTicks = (int) (2 * 20 * chillDurMul);
            if (chillTicks > 0) {
                StatusEngine.apply(living, StatusDefs.CHILL, target, chillPercent, chillTicks);
                fireAilment(source, target, living, StatusDefs.CHILL.id);
                applied = true;
            }
        }

        // 冰冻（THRESHOLD）：冰霜积蓄满门槛即定身
        if (!avoidsPublic(living, random, StatusDefs.FREEZE.id)) {
            double total = StatusEngine.addBuildup(living, StatusDefs.FREEZE, gain * buildupMultiplier(target, StatusDefs.FREEZE));
            if (total >= 1.0) {
                DamageContext.cacheDamageContext(source, living, new DamageContext().causeFreeze(true));
                // 时长 = 施加方（AILMENT_DURATION）× 承受方（taken 标签 AILMENT_DURATION，替代 FREEZE_DURATION/FREEZE_DURATION_TAKEN）
                double freezeDurMul = ailmentDurationMul(target, living, GateTag.FREEZE)
                        * takenDurationMul(living, StatusDefs.FREEZE.id);
                // 大命中溢出：满门槛后多余的积蓄延长冰冻时长
                double overflow = total - 1.0;
                int freezeTicks = (int) ((4 + overflow * 4) * 20 * freezeDurMul);
                if (freezeTicks > 0) {
                    StatusEngine.apply(living, StatusDefs.FREEZE, target, 100, freezeTicks);
                    fireAilment(source, target, living, StatusDefs.FREEZE.id);
                    applied = true;
                }
                StatusEngine.resetBuildup(living, StatusDefs.FREEZE, overflow);
            }
        }
        return applied;
    }

    /**
     * 合成冰霜伤害冰缓：用于「Chills enemies as though dealing X Cold damage」类效果（如 Heart of Ice 冰雪之心）。
     * 复用与真实冰霜命中完全相同的冰缓强度公式（门槛 / 积蓄 / 幅度修正全部生效），但冰霜伤害来源是合成值而非真实命中。
     *
     * @param source 合成冰霜伤害的施加方（光环持有者）
     * @param target 承受冰缓的目标（光环范围内的敌人）
     * @param syntheticColdDamage 合成冰霜伤害值（经 skill.level 等节点求值后的数值）
     * @return 冰缓百分比（1..50）；目标免疫冰缓或合成伤害无法产生有效冰缓时返回 0（不施加）
     */
    public static int chillPercentFromSyntheticCold(LivingEntity source, LivingEntity target,
                                                    double syntheticColdDamage) {
        if (syntheticColdDamage <= 0) return 0;
        if (avoidsPublic(target, target.getRandom(), StatusDefs.CHILL.id)) return 0;
        double threshold = StatusEngine.ailmentThreshold(target)
                * ailmentThresholdMul(target, StatusDefs.CHILL)
                + ailmentThresholdAdd(target, StatusDefs.CHILL);
        if (threshold <= 0) return 0;
        double gain = syntheticColdDamage / threshold;
        double chillEffect = ailmentMagnitude(source, StatusDefs.CHILL) * takenMagnitude(target, StatusDefs.CHILL.id);
        return (int) Math.clamp(gain * buildupMultiplier(source, StatusDefs.CHILL) * 50 * chillEffect, 1, 50);
    }

    /**
     * 闪电命中：感电按 POE2「几率型」模型处理——每造成目标异常门槛 100% 的伤害有 25% 基础几率施加
     * （命中伤害越大几率越高，可超 25%；感电不再走积蓄，与点燃同模）；电击保留独立积蓄条
     * （POE2 电击资料不足，且已与眩晕独立）。
     * 闪电伤害按「占异常门槛比例」换算几率与幅度。
     */
    public static boolean applyLightAilment(DamageSource source, LivingEntity living, LivingEntity target, RandomSource random, float[] damages) {
        float lightDamage = getShockDamage(target, damages);
        if (lightDamage <= 0) return false;
        double baseThreshold = StatusEngine.ailmentThreshold(living);
        boolean applied = false;

        // 感电（几率型，POE2）：基础几率 25%/100% 门槛，命中越大越高（不受 100% 门槛上限约束）
        boolean hadShock = StatusEngine.has(living, StatusDefs.SHOCK);
        if (!avoidsPublic(living, random, StatusDefs.SHOCK.id)) {
            double shockThreshold = baseThreshold * ailmentThresholdMul(living, StatusDefs.SHOCK)
                    + ailmentThresholdAdd(living, StatusDefs.SHOCK);
            double rawGain = lightDamage / shockThreshold; // 命中占门槛比例（如 2.0 = 200%）
            double baseChance = 0.25 * rawGain;           // POE2：100% 门槛 → 25% 基础几率，线性放大
            // 攻击方「异常幅度」作用域内修正（对感电标签）：同时放大几率与幅度（POE2 幅度即伤害放大）
            double chance = Math.clamp(baseChance * ailmentMagnitude(target, StatusDefs.SHOCK), 0, 1);
            if (random.nextFloat() < chance) {
                DamageContext.cacheDamageContext(source, living, new DamageContext().causeShock(true));
                // 强度 = 施加方（AILMENT_MAGNITUDE）× 承受方（taken 标签，替代 SHOCK_EFFECT_TAKEN）
                double shockEffect = ailmentMagnitude(target, StatusDefs.SHOCK)
                        * takenMagnitude(living, StatusDefs.SHOCK.id);
                // 命中越大，感电的受伤增加越高（约 20% × 命中比例，封顶 100%）
                double magnitude = Math.clamp(20 * shockEffect * Math.min(1.0, rawGain), 1, 100);
                // 时长 = 施加方（AILMENT_DURATION）× 承受方（taken 标签，替代 SHOCK_DURATION/SHOCK_DURATION_TAKEN）
                double shockDurMul = ailmentDurationMul(target, living, GateTag.SHOCK)
                        * takenDurationMul(living, StatusDefs.SHOCK.id);
                int shockTicks = (int) (4 * 20 * shockDurMul);
                if (shockTicks > 0) {
                    StatusEngine.apply(living, StatusDefs.SHOCK, target, magnitude, shockTicks);
                    fireAilment(source, target, living, StatusDefs.SHOCK.id, !hadShock);
                    applied = true;
                }
            }
        }

        // 电击（THRESHOLD，门槛倍率/时长基数从 ELECTROCUTE 异常类型引用，非硬编码）：
        // 独立积蓄满即定身（本质重击硬直的一种，POE2 电击资料不足保留积蓄）
        if (!avoidsPublic(living, random, StatusDefs.ELECTROCUTE.id)) {
            StatusType elec = StatusDefs.ELECTROCUTE;
            // 门槛倍率来自异常类型声明（AilmentSpec.thresholdMultiplier，当前 1.5）
            double elecThreshold = baseThreshold * ailmentThresholdMul(living, elec)
                    * elec.ailment.thresholdMultiplier()
                    + ailmentThresholdAdd(living, elec);
            double rawGain = lightDamage / elecThreshold;
            double total = StatusEngine.addBuildup(living, elec,
                    rawGain * buildupMultiplier(target, elec));
            if (total >= 1.0) {
                // 电击独立标志（不复用 causeStun：电击与眩晕语义分离）
                DamageContext.cacheDamageContext(source, living, new DamageContext().causeShock2(true));
                double durMul = ailmentDurationMul(target, living, elec.id);
                int ticks = (int) (elec.defaultDurationTicks * durMul);
                if (ticks > 0) {
                    StatusEngine.apply(living, StatusDefs.ELECTROCUTE, target, 1.0, ticks);
                    fireAilment(source, target, living, StatusDefs.ELECTROCUTE.id);
                    applied = true;
                }
                StatusEngine.resetBuildup(living, StatusDefs.ELECTROCUTE, total - 1.0);
            }
        }
        return applied;
    }

    /**
     * 在 entity 上进入带分类标签的 Stat 作用域后读取某属性（自动聚合 ailment / 元素 / 具体异常效果等加成）；
     * entity 为空则返回 1.0（无加成）。用于冰缓/感电等控制类异常的效果倍率，与 DoT 的标签驱动流程同源。
     * <b>并入当前栈顶外层标签</b>（{@link StatScopes#enterMerged}）：命中结算内调用时自动保留 spell / attack
     * 等外层行为标签，使「with Spells」类条目不依赖代码谓词反推（取代原 isSpellSource 补偿）。
     */
    public static double withScope(LivingEntity entity, Set<String> tags, Holder<Attribute> attr) {
        if (entity == null) return 1.0;
        AutoCloseable s = StatScopes.enterMerged(entity, StatContext.of(tags, null));
        try {
            return entity.getAttributeValue(attr);
        } finally {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 首类「法术/技能效果时长」倍率（Stat 驱动）：读取 {@code AttributeRegistry#SPELL_DURATION_MULT}（该属性常量已删除）
     * （默认 1.0，ADD_MULTIPLIED_TOTAL 累积），承载语料 STORM|PLANT_SPELLS_HAVE_75_PERCENT_LESS_DURATION
     * （作用域 {spell,storm}/{spell,plant}）。
     * <p>在<b>当前（已并入技能标签的）作用域</b>内读取：投递时长计算（{@code AreaDeliveryExecutor} /
     * 统一 {@code ProjectileDeliveryExecutor} 的 ORB 分支等）运行于 {@code SkillStatScopes.enterAction}，
     * 栈顶携带 spell / storm / plant 等技能标签，故对应条目即时折叠；非施法场景下恒为 1.0（无副作用）。
     */
    public static double spellDurationMultiplier(LivingEntity caster) {
        if (caster == null) return 1.0;
        return Math.max(0.0, caster.getAttributeValue(AttributeRegistry.DURATION_MULT));
    }

    /**
     * 非伤害性异常（冰缓/冰冻/感电）持续时间的通用加成层。
     * 在「状态 + 异常 + 非伤害性 + 具体异常 + 持续时间」分类作用域内，分别聚合三类通用持续时间倍率：
     * 状态效果持续时间、异常状态持续时间、非伤害性异常状态持续时间（均为「更多」型，默认 1.0）。
     * 与 DoT 伤害倍率、冰缓/感电效果倍率的标签驱动流程同源。
     *
     * @param target       承受异常者（持续时间加成作用于承受者侧）
     * @param specificTag 具体异常标签（GateTag.CHILL / FREEZE / SHOCK）
     */
    /** 敌种类别 tag（datapack entity_type tag，与 {@code EntityTypeValue} 共用，可热更增删成员）。 */
    private static final EntityTypeValue BEAST_TYPE = EntityTypeValue.of("coe", "beast");

    /** 施加方异常时长修正（Stat 驱动）：归一读取 {@code AILMENT_DURATION}，作用域携带「异常名+分类+分类标签」。 */
    public static double ailmentDurationMul(LivingEntity target, String specificTag) {
        return ailmentDurationMul(target, null, specificTag);
    }

    /**
     * 施加方异常时长修正（进攻侧，Stat 驱动）：在 {@link #ailmentDurationMul(LivingEntity, String)} 基础上，
     * 当 {@code victim} 属于兽类（{@code coe:beast} entity_type tag）时向其作用域注入 {@link GateTag#BEAST}，
     * 使「异常持续时间 对野兽 +X%」族（{@code inc_beast_ailment_duration} /
     * {@code inc_duration_of_ailments_on_beasts}，EFF 带 {@code {beast, not(taken)}} 标签）真正生效。
     * 受害者类别经 datapack tag 在运行期判定，与条件系统 {@code EntityTypeValue} 同源，零硬编码、可热更。
     */
    public static double ailmentDurationMul(LivingEntity target, @Nullable LivingEntity victim, String specificTag) {
        // 只发关键下位标签（具体异常名 specificTag）；elemental/non_damaging/damage_over_time/ailment
        // 等父类别由 GateTag.match 汇总判定（上位包含）。
        Set<String> tags = new HashSet<>(Set.of(GateTag.STATUS, GateTag.AILMENT, specificTag));
        if (victim != null && BEAST_TYPE.matches(victim)) {
            tags.add(GateTag.BEAST);
            return withScope(target, tags, AttributeRegistry.BEAST_DURATION);
        }
        return withScope(target, tags, AttributeRegistry.AILMENT_DURATION);
    }

    /**
     * 目标对某异常的「门槛」修正（防御侧，Stat 驱动）：统一读取 {@code AILMENT_THRESHOLD}
     * （加性百分比，默认 0 = 无影响）。作用域同时携带「分类标签」与「异常名」，由 Stat 系统按标签子集匹配
     * 自动聚合「通用异常门槛 / 元素门槛 / 具体异常门槛」等条目——无需按异常名 switch 选特定属性。
     * 分类（统一之后再区分）：元素异常→{@code elemental}、控制类(非伤害)→{@code non_damaging}、
     * 伤害性(DoT)→{@code damage_over_time}。
     */
    public static double ailmentThresholdMul(LivingEntity living, StatusType type) {
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, GateTag.THRESHOLD, type.id));
        // Empowered Attacks（战吼强化攻击，add_empowered_attacks>0）激活期间提升异常门槛（含眩晕门槛）：
        // 注入 EMPOWERED 标签使 {STUN,EMPOWERED} 类门槛 EFF 真实生效（R3 安全，复用既有强化攻击计数状态）。
        if (living.getAttributeValue(AttributeRegistry.ADD_EMPOWERED_ATTACKS) > 0) {
            tags.add(GateTag.EMPOWERED);
        }
        return 1.0 + withScope(living, tags, AttributeRegistry.AILMENT_THRESHOLD);
    }

    /** 异常加性门槛（绝对追加）：所有异常状态共有，同一 {ailment,threshold,异常id}+分类标签作用域读取
     *  AILMENT_THRESHOLD_ADD（与 ailmentThresholdMul 倍率正交并存）。眩晕额外叠加 STUN_THRESHOLD（历史词缀载体裸读）。 */
    public static double ailmentThresholdAdd(LivingEntity living, StatusType type) {
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, GateTag.THRESHOLD, type.id));
        return withScope(living, tags, AttributeRegistry.AILMENT_THRESHOLD_ADD);
    }

    /**
     * 异常积蓄量（进攻侧，Stat 驱动）：统一读取 {@code AILMENT_BUILDUP}（倍率，默认 1.0 = 不额外积累）。
     * 作用域携带「分类标签 + 异常名」，由 Stat 系统按标签聚合「通用异常积蓄 / 具体异常积蓄」等条目
     * （POE2「increased Ailment Buildup」类词缀即作用于此），同样无需按异常名 switch。
     * spell / attack 等外层行为标签由 {@link #withScope} 并入当前栈自动保留（无需调用方反推）。
     */
    public static double buildupMultiplier(LivingEntity attacker, StatusType type) {
        if (attacker == null) return 1.0;
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, type.id));
        return withScope(attacker, tags, AttributeRegistry.AILMENT_BUILDUP);
    }

    /**
     * 控制类异常（冰缓/感电）效果强度（进攻侧，Stat 驱动）：统一读取 {@code AILMENT_MAGNITUDE}
     * （倍率，默认 1.0 = 不额外增强）。作用域携带「分类标签 + 异常名」，与 SkillFactory
     * 中 {@code ailmentEntries} 经 {@code ailmentModifier(t, AILMENT_MAGNITUDE)} 注入的
     * 统一强度条目对齐——彻底替代旧的 CHILL_EFFECT / SHOCK_EFFECT 独立属性（绝对性完全更改，不并存）。
     * spell / attack 等外层行为标签由 {@link #withScope} 并入当前栈自动保留。
     */
    /**
     * 枯萎标签注入：若实体持有 wither 减益，则向给定的 ailment 作用域注入 {@link GateTag#WITHERED}。
     * 使以 {WITHERED} 过滤的 Stat 条目（{@code INC_BUFF_EFFECT_OF_WITHERED_ON_YOU} /
     * {@code INC_WITHERED_BUFF_MAGNITUDE} / {@code MINIONS_HAVE_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE}）
     * 在「该实体处于枯萎状态」时真实生效（R3 安全：引擎确读此标签，零硬编码、随 wither 状态运行期判定）。
     * 与 BEAST 同构——区别仅在于判定依据是动态状态（StatusEngine.has）而非静态实体种类。
     */
    private static void injectWitheredIfPresent(Set<String> tags, @Nullable LivingEntity e) {
        if (e != null && StatusEngine.has(e, StatusDefs.WITHER)) {
            tags.add(GateTag.WITHERED);
        }
    }

    /**
     * 灌入标签注入：若实体持有任一元素灌入状态（INFUSION_*，其 {@code .tags("infusion", element)}），
     * 则向给定的 ailment 作用域注入 {@link GateTag#INFUSION}。
     * 使以 {INFUSION} 过滤的 Stat 条目（{@code INC_INFUSION_MAGNITUDE} 等）在「该实体处于灌入状态」时真实生效
     * （与 WITHERED 同构——判定依据是动态状态 {@code StatusEngine.activeTags().contains(INFUSION)}）。
     * <p>修复：INFUSION_* 状态的 {@code "infusion"} tag 经 {@code StatusEngine.activeTags} 仅进事件作用域、
     * 不进 Stat 折叠作用域，故原 {@code {infusion}} 门控恒不匹配（死线）；此处显式注入复活该作用域。
     */
    private static void injectInfusionIfPresent(Set<String> tags, @Nullable LivingEntity e) {
        if (e != null && StatusEngine.activeTags(e).contains(GateTag.INFUSION)) {
            tags.add(GateTag.INFUSION);
        }
    }

    private static double ailmentMagnitude(LivingEntity caster, StatusType type) {
        if (caster == null) return 1.0;
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, type.id));
        injectWitheredIfPresent(tags, caster);
        injectInfusionIfPresent(tags, caster);
        return withScope(caster, tags, AttributeRegistry.AILMENT_MAGNITUDE);
    }

    /**
     * 眩晕时长倍率（独立属性 {@code STUN_DURATION}，作用域 {status, ailment, stun}）：与 {@link #ailmentDurationMul} 解耦，
     * 使「眩晕持续时间」类加成（{@code inc_stun_duration} 族，EFF 带 {@code {stun, not(taken)}} 标签）独立生效，
     * 不再共享 DoT 异常时长属性 {@code AILMENT_DURATION}。
     */
    public static double stunDurationMul(LivingEntity target) {
        Set<String> tags = new HashSet<>(Set.of(GateTag.STATUS, GateTag.AILMENT, StatusDefs.STUN.id));
        return withScope(target, tags, AttributeRegistry.STUN_DURATION);
    }

    /**
     * 承受方异常时长修正（防御侧，Stat 驱动）：统一读取 {@code AILMENT_DURATION}，
     * 作用域携带「异常名 + 分类 + taken」，由 Stat 系统按标签聚合
     * 「通用异常时长 / 具体异常时长 / 承受方时长」条目——彻底替代旧的 *_DURATION_TAKEN 独立属性。
     * 与施加方（{@link #ailmentDurationMul}）同一归一属性，仅以 taken 标签区分承受侧。
     */
    private static double takenDurationMul(LivingEntity living, String specificTag) {
        if (living == null) return 1.0;
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, specificTag, "taken"));
        return withScope(living, tags, AttributeRegistry.AILMENT_DURATION);
    }

    /**
     * 承受方异常强度修正（防御侧，Stat 驱动）：统一读取 {@code AILMENT_MAGNITUDE}，
     * 作用域携带「异常名 + 分类 + taken」，替代旧的 *_EFFECT_TAKEN 独立属性。
     * 与施加方（{@link #ailmentMagnitude}）同一归一属性，仅以 taken 标签区分承受侧。
     */
    private static double takenMagnitude(LivingEntity living, String specificTag) {
        if (living == null) return 1.0;
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, specificTag, "taken"));
        injectWitheredIfPresent(tags, living);
        return withScope(living, tags, AttributeRegistry.AILMENT_MAGNITUDE);
    }

    /**
     * 异常分类标签：在「统一属性」之上再按分类区分，使同一条统一属性可被不同粒度的 stat 条目命中。
     * <ul>
     *   <li>{@code elemental}：元素异常（点燃/冰缓/冰冻/感电/电击）；</li>
     *   <li>{@code non_damaging}：控制类(非伤害)异常（冰缓/冰冻/感电/电击/眩晕）；</li>
     *   <li>{@code damage_over_time}：伤害性(DoT)异常（流血/点燃/中毒）。</li>
     * </ul>
     * 异常名本身也已作为标签进入作用域，故可被「具体异常」粒度条目（如仅 freeze）单独命中。
     */
    /**
     * 异常名 → 分类标签。分类标签已在 <b>Status 注册阶段</b>随 {@code StatusType} 一并声明，
     * 此处仅经 Status 帮助方法 {@link StatusType#tagsOf(String)} 取用，不在本类内重复分类（消除反模式 switch）。
     * 在「统一属性」之上再按分类区分，使同一条统一属性可被不同粒度的 stat 条目命中：
     * <ul>
     *   <li>{@code elemental}：元素异常（点燃/冰缓/冰冻/感电/电击）；</li>
     *   <li>{@code non_damaging}：控制类(非伤害)异常（冰缓/冰冻/感电/电击/眩晕）；</li>
     *   <li>{@code damage_over_time}：伤害性(DoT)异常（流血/点燃/中毒）。</li>
     * </ul>
     * 异常名本身也已作为标签进入作用域（见调用处的 {@code type.id}），故可被「具体异常」粒度条目（如仅 freeze）单独命中。
     * 分类命中（{@code elemental}/{@code non_damaging}/{@code damage_over_time} 等父类别）由 GateTag.match 按标签层级汇总判定，
     * 不再在此手动广播分类标签。
     */

    /**
     * 异常回避判定（归一化）：在 {ailment, 异常id} 作用域内读 {@code AVOID_AILMENT_CHANCE}（1.0 = 免疫），
     * 替代旧 7 个 AVOID_XXX_CHANCE 独立属性与 AVOID_POISON_IMPALE_BLEEDING_CHANCE / AVOID_ELEMENTAL_AILMENTS_CHANCE
     * 复合属性。分类回避（元素异常/物理 DoT 等）由 Stat 条目挂分类标签（elemental/physical/damage_over_time）
     * 聚合进同一属性，无需 categoryAttr 兜底通道。命中即免疫本次施加。
     */
    public static boolean avoidsPublic(LivingEntity living, RandomSource random, String ailmentId) {
        Set<String> tags = new HashSet<>(Set.of(GateTag.AILMENT, ailmentId));
        double chance = withScope(living, tags, AttributeRegistry.AVOID_AILMENT_CHANCE);
        return chance > 0 && random.nextDouble() < chance;
    }

    public static void applyStunAilment(DamageSource source, LivingEntity living, RandomSource random, float[] damages) {
        float totalDamage = getAllDamage(damages);
        if (totalDamage <= 0) return;
        LivingEntity stunCaster = source.getEntity() instanceof LivingEntity le ? le : null;

        // ── 重晕（Heavy Stun）：积累制，仅非玩家（POE2：玩家/召唤物通常不被重晕）──
        boolean heavyStunned = false;
        if (!(living instanceof Player)) {
            float stunDamage = totalDamage + damages[0] * 0.5F;   // POE2：物理伤害对重晕积累 +50% more
            // 免疫窗口：重晕后短时间内（STUN_IMMUNE）不积累（POE2：之后更难再次重晕）
            if (!StatusEngine.has(living, StatusDefs.STUN_IMMUNE)) {
                // 门槛 = 倍率(AILMENT_THRESHOLD) + 通用加性(AILMENT_THRESHOLD_ADD, {stun} 作用域)，
                // 与点燃/中毒/冰缓/感电/电击完全同源（无 STUN_THRESHOLD 特殊通道）
                double threshold = StatusEngine.ailmentThreshold(living)
                        * ailmentThresholdMul(living, StatusDefs.STUN)
                        + ailmentThresholdAdd(living, StatusDefs.STUN);
                if (threshold > 0) {
                // 单次积累量 = 击晕伤害 / 击晕门槛 × 占比倍率（AILMENT_BUILDUP，{heavy_stun} 作用域聚合）
                double gain = (stunDamage / threshold)
                        * buildupMultiplier(stunCaster, StatusDefs.STUN);
                double buildup = StatusEngine.addBuildup(living, StatusDefs.STUN, gain);
                    if (buildup >= 1.0) {
                        applyHeavyStun(source, living, stunCaster, buildup);
                        heavyStunned = true;
                    }
                }
            }
        }

        // ── 轻晕（Light Stun）：几率制，所有实体（本次已重晕则不再轻晕，避免状态重叠）──
        if (!heavyStunned) {
            float chance = totalDamage / living.getMaxHealth();   // POE2：100% 最大生命 → 100% 几率
            if (chance >= 0.15f && random.nextFloat() < chance) {
                applyLightStun(source, living, stunCaster);
            }
        }
    }

    /** 轻晕（Light Stun）：极短停顿（基础 0.5s × STUN_DURATION），事件标签 stun。 */
    private static void applyLightStun(DamageSource source, LivingEntity living, LivingEntity caster) {
        double stunDuration = stunDurationMul(living);
        int durationTicks = (int) (Math.max(4, 10 * stunDuration) / Math.max(0.01, stunRecoveryMul(living)));
        StatusEngine.apply(living, StatusDefs.STUN, caster, 1.0, durationTicks);
        StatusInstance li = StatusEngine.query(living, StatusDefs.STUN);
        if (li != null) { li.heavy = false; li.iconOverride = null; }
        fireAilment(source, caster, living, StatusDefs.STUN.id);
        // 轻/重晕统一记录「被眩晕」事件，驱动近期被眩晕条件（护盾充能加速等）
        Event.record(living, EventName.TAKEN_STUN);
        // R3 修复 #65：TAKEN_STUN 此前只经 Event.record 写入计数队列（喂 recently/count 条件），
        // 不触发 GrantOnEventStatEffect（须 StatEngine.fire）。补真实发射点（owner=被眩晕者自身，
        // GrantOnEventStatEffect.RESOURCE 取 owner 授予波动率，语义正确：「受眩晕时获得波动率」）。
        StatEngine.fire(living, caster, StatContext.of("taken_stun"), null);
    }

    /** 重晕（Heavy Stun）：数秒定身 + 免疫窗口 + 保留溢出（大命中溢出到下一次积累），事件标签 heavy_stun。 */
    private static void applyHeavyStun(DamageSource source, LivingEntity living, LivingEntity caster, double buildup) {
        DamageContext.cacheDamageContext(source, living, new DamageContext().causeStun(true));
        double stunDuration = AilmentHelper.stunDurationMul(living);
        int durationTicks = (int) ((2 * 20 * stunDuration) / Math.max(0.01, stunRecoveryMul(living)));
        StatusEngine.apply(living, StatusDefs.STUN, caster, 1.0, durationTicks, 1, StatusDefs.HEAVY_STUN_ICON);
        StatusInstance hi = StatusEngine.query(living, StatusDefs.STUN);
        if (hi != null) hi.heavy = true;
        // 造成侧 fire 显式把「被重晕的敌人」绑定为本次事件作用域的<b>交互对方</b>（StatContext.sourceEntity）：
        // 使 caused_ailment 条件节点（TargetRarityValue / COND_TARGET_RARITY_RARE_UNIQUE「重晕稀有/传奇敌人」）
        // 经事件作用域读到被重晕者的真实稀有度（target-rarity），而不再依赖外层命中作用域的隐式注入
        // （applyStunAilment 虽在 DamagePipeline 的 enterHitScopes 内，此处仍显式绑定以保证语义自洽、可独立验证）。
        // enterMerged：并入外层行为标签（attack/melee/…），仅以本层 sourceEntity 覆写交互对方——纯加性、无遮蔽。
        if (caster != null && caster != living) {
            AutoCloseable evtScope = StatScopes.enterMerged(caster,
                    StatContext.of(Set.of(EventName.CAUSED_AILMENT, GateTag.HEAVY_STUN), null)
                            .withSourceEntity(living));
            try {
                fireAilment(source, caster, living, GateTag.HEAVY_STUN);
            } finally {
                try {
                    evtScope.close();
                } catch (Exception ignored) {
                }
            }
        } else {
            fireAilment(source, caster, living, GateTag.HEAVY_STUN);
        }
        Event.record(living, EventName.TAKEN_STUN);
        // R3 修复 #65（镜像 applyLightStun）：TAKEN_STUN 补真实发射点（owner=被眩晕者自身）。
        StatEngine.fire(living, caster, StatContext.of("taken_stun"), null);
        StatusEngine.applyIfAbsent(living, StatusDefs.STUN_IMMUNE, caster, 1.0, 30);
        StatusEngine.resetBuildup(living, StatusDefs.STUN, buildup - 1.0);
    }

    /** STUN_RECOVERY（stun_recovery，基 1.0）：眩晕恢复速率倍率（FOLD-KEEP，{STUN} 作用域）。
     *  值越大恢复越快 → 眩晕持续越短，按 1/rate 缩放眩晕时长。
     *
     *  <p><b>接线须知（死线 #37）</b>：本读点用 {@code StatScopes.enter}（<b>非</b> {@code enterMerged}），
     *  推入的 context 由 2 参 {@code StatContext.of(tags, null)} 构造、<b>sourceEntity 恒为 null</b>，
     *  故 {@code triggerTarget} 恒 null（与 {@link #withScope} 的 enterMerged 不同——后者会继承外层 sourceEntity）。
     *  因此喂给本读点的条目<b>必须</b>用 {@code self(...)}：改用 {@code onTarget(...)} 或
     *  {@code .conditionTarget(EVENT_TARGET)} 时条件将恒假、永不生效。
     *  读的是实体<b>自身</b>属性，本就不需要跨实体，故此处无需改 enterMerged。 */
    private static double stunRecoveryMul(LivingEntity living) {
        if (living == null) return 1.0;
        try (var scope = StatScopes.enter(living, StatContext.of(Set.of(GateTag.STUN), null))) {
            return living.getAttributeValue(AttributeRegistry.STUN_RECOVERY);
        } catch (Exception ignored) {}
        return 1.0;
    }

    // ───────────────────────────── 技能显式异常（统一入口） ─────────────────────────────
    // 技能显式异常不按状态分派（不为 applyIgnite/Chill/Freeze/Shock/ElectrocuteAilment 各写一份 skill 重载
    // 与 chillAmplifier），统一走 {@link #applyStatusOnEvent}（数据驱动路由）
    // + {@link #fireAilmentBySkill}（事件闭环）。

    // ───────────────────────────── 技能命中后状态 debuff（由 DamageAction 调用） ─────────────────────────────

    /** 元素曝露（Exposure）：命中后压低目标对应元素抗性。 */
    public static void applyExposure(LivingEntity target, Exposure exp, SkillContext skillCtx) {
        StatusType type = switch (exp.damageType().toLowerCase(Locale.ROOT)) {
            case "fire" -> StatusDefs.EXPOSURE_FIRE;
            case "cold" -> StatusDefs.EXPOSURE_COLD;
            case "lightning" -> StatusDefs.EXPOSURE_LIGHTNING;
            default -> null;
        };
        if (type == null) return;
        double magnitude = Math.max(0, exp.amount().getFloat(skillCtx));
        int duration = Math.max(1, (int) exp.duration().getFloat(skillCtx));
        StatusEngine.apply(target, type, null, magnitude, duration);
    }

    /**
     * 减速（Slow）：命中后施加非异常 debuff（Maim / Hinder 类）。
     * <p>强度（百分比放大器）= 基础 {@code amount × 100} × 承受方幅度 × 施加方幅度：
     * <ul>
     *   <li>承受方幅度：{@code AILMENT_MAGNITUDE @ {ailment, slow, taken}}（防御侧，替代旧 SLOW_EFFECT_TAKEN）；</li>
     *   <li>施加方幅度：{@code AILMENT_MAGNITUDE @ {ailment, slow}}（不含 taken，进攻侧，dead-line #20 闭环）；
     *       由 {@link #ailmentMagnitude} 经 enterMerged 并入当前栈（hazard 等外层标签自动保留），
     *       使「你施加的减速」「危害物施加的减速」分别命中对应 EFF。</li>
     * </ul>
     * 故 {@code INC_SLOWING_POTENCY_OF_DEBUFFS_ON_YOU}（承受侧）/ {@code DEBUFFS_YOU_INFLICT_HAVE_INC_SLOW_MAGNITUDE} /
     * {@code DEBUFFS_INFLICTED_BY_HAZARDS_HAVE_INC_SLOW_MAGNITUDE}（施加侧）等 EFF 真实生效。
     */
    public static void applySlow(LivingEntity target, Slow slow, SkillContext skillCtx) {
        // 承受方幅度（防御侧）：AILMENT_MAGNITUDE @ {ailment, slow, taken}
        double slowMul = takenMagnitude(target, StatusDefs.SLOW.id);
        // 施加方幅度（进攻侧，dead-line #20）：dealer 在 {ailment, slow} 作用域的 AILMENT_MAGNITUDE，不含 taken；
        // enterMerged 自动并入当前栈（hazard 等），危害物减速仅命中 HAZARD 作用域 EFF。
        LivingEntity dealer = skillCtx != null ? skillCtx.getCaster() : null;
        double dealMul = ailmentMagnitude(dealer, StatusDefs.SLOW);
        int amplifier = Math.max(0, (int) Math.round(slow.amount().getFloat(skillCtx) * 100 * slowMul * dealMul));
        int duration = Math.max(1, (int) slow.duration().getFloat(skillCtx));
        if (amplifier > 0) {
            StatusEngine.apply(target, StatusDefs.SLOW, dealer, amplifier, duration);
        }
    }

    // ───────────────────────────── 伤害量/几率取值（贡献模型） ─────────────────────────────

    /** 伤害槽位下标 ↔ 伤害类型标签（damages[]: 0=物理 1=混沌 2=火焰 3=冰霜 4=闪电）。
     *  ADR-003 阶段2：改由 {@link DamageTypes#mainTypeTags()} 统一派生，移除与 {@code DamageTypeRegistry} 重复的硬编码 {5}。 */
    private static final String[] DAMAGE_TYPE_TAGS = DamageTypes.mainTypeTags();

    /**
     * 「伤害贡献异常」聚合（忠于 poe2db「Damage Contributing to Ailments」）：
     * 权重 = 默认配对(1|0) + 攻方在 {ailment, 伤害类型, 异常} 作用域下聚合的 ailment_damage_contribution。
     * 数据侧：「A 也贡献 B」= +1 entry（tags [A, B]）；「A 贡献 B 而非 C」= +1 [A, B] 与 -1 [A, C] 两条
     * （对应 poe2 词缀「X Damage also Contributes to …」与「… Contributes to … instead of …」两种句式）。
     * 注：贡献只决定异常基础伤害由各来源伤害类型各占多少；中毒/流血的施加几率仍需独立几率来源。
     *
     * <p>机制备注（用户裁定，偏离 poe2db）：用户坚持「状态伤害类型基于来源伤害类型」——
     * 一次伤害携带的 100 混沌 + 80 物理 + 50 火焰若可造成中毒，则这一层中毒对三种伤害<b>分别计算、分别输出</b>，
     * 各自被对应抗性/护甲减免（物理份额外走护甲+物抗、混沌走混沌抗、火走火抗），而非统一成某种 DoT 类型。
     * 故本实现以 {@link #compositionDamage} 产出按来源类型拆分的数组，由 StatusBehavior.dealSplitDoT 分别结算。
     */
    private static float contributionDamage(LivingEntity attacker, float[] damages, String ailmentTag, Set<String> defaultTypes) {
        double total = 0;
        for (int i = 0; i < DAMAGE_TYPE_TAGS.length; i++) {
            if (damages[i] <= 0) continue;
            double weight = defaultTypes.contains(DAMAGE_TYPE_TAGS[i]) ? 1.0 : 0.0;
            if (attacker != null) {
                weight += withScope(attacker, Set.of(GateTag.AILMENT, DAMAGE_TYPE_TAGS[i], ailmentTag),
                        AttributeRegistry.AILMENT_DAMAGE_CONTRIBUTION);
            }
            if (weight > 0) total += damages[i] * weight;
        }
        return (float) total;
    }

    /**
     * 构建某伤害性异常（中毒/点燃/流血）按来源伤害类型<b>拆分</b>的伤害分量数组（索引对齐
     * DamageTypeRegistry.DAMAGE_TYPE_TAGS：0=物理 1=混沌 2=火焰 3=冰霜 4=闪电）。
     * 各份额外将在 tick 时以各自来源伤害类型（或被 ailment_output_convert 改写后的输出类型）分别结算、分别减免。
     * {@code allDamageAttr} 非 0 时（如「all damage can poison」）所有类型等权全额计入。
     */
    private static double[] compositionDamage(LivingEntity attacker, float[] damages, String ailmentTag,
                                             Set<String> defaultTypes) {
        double[] comp = new double[DamageTypes.SLOT_CAPACITY];
        for (int i = 0; i < DamageTypes.width(); i++) {
            if (damages[i] <= 0) continue;
            double weight = defaultTypes.contains(DAMAGE_TYPE_TAGS[i]) ? 1.0 : 0.0;
            if (attacker != null) {
                weight += withScope(attacker, Set.of(GateTag.AILMENT, DAMAGE_TYPE_TAGS[i], ailmentTag),
                        AttributeRegistry.AILMENT_DAMAGE_CONTRIBUTION);
            }
            if (weight > 0) comp[i] = damages[i] * weight;
        }
        return comp;
    }

    private static double[] getBleedingDamage(LivingEntity living, float[] damages) {
        double[] comp = compositionDamage(living, damages, GateTag.BLEED, Set.of(GateTag.PHYSICAL));
        return comp;
    }

    private static int getBleedingChance(LivingEntity living) {
        // 异常几率已归一：命中后（ATTACK 标签）在 {attack, bleed} 作用域内读 AILMENT_CHANCE 单属性，
        // 流血几率加成由数据侧挂 tags={attack, bleed} 的 Stat 条目表达（属性名不带 ON_ATTACK_HIT 条件词）。
        Set<String> tags = Set.of(GateTag.ATTACK, GateTag.BLEED);
        return (int) (withScope(living, tags, AttributeRegistry.AILMENT_CHANCE) * 100);
    }

    // ── 致盲（命中型异常，固定几率，无 DoT）── 镜像 getBleedingChance：在 {attack, blind} 作用域读 AILMENT_CHANCE。
    private static final double BLIND_BASE_MAGNITUDE = 20.0; // POE2 基础致盲 -20% 命中/闪避（StatusBehavior.blind 取 -magnitude/100）
    private static final int BLIND_DURATION_TICKS = 80;     // POE2 基础致盲 4 秒

    private static int getBlindChance(LivingEntity living) {
        Set<String> tags = new HashSet<>(Set.of(GateTag.ATTACK, GateTag.BLIND));
        injectWitheredIfPresent(tags, living);
        return (int) (withScope(living, tags, AttributeRegistry.AILMENT_CHANCE) * 100);
    }

    private static void applyBlindAilment(DamageSource source, LivingEntity living, LivingEntity target, RandomSource random) {
        // living=受害方(被致盲)，target=攻击方(致盲来源)。几率取自攻击方 AILMENT_CHANCE{BLIND}。
        int blindChance = getBlindChance(target);
        if (blindChance > 0 && random.nextInt(100) < blindChance) {
            if (avoidsPublic(living, random, StatusDefs.BLIND.id)) return;
            // 幅度/时长改为属性驱动（#48 同型：常量顶替属性 = 死线）——
            // 施加方 AILMENT_MAGNITUDE{blind} / AILMENT_DURATION{blind} × 承受方 taken 侧同名作用域。
            StatusEngine.apply(living, StatusDefs.BLIND, target,
                    blindMagnitude(target, living), blindDurationTicks(target, living), 1);
            fireAilment(source, target, living, StatusDefs.BLIND.id);
        }
    }

    /**
     * 致盲强度：基础 20（POE2 -20% 命中/闪避）× 施加方 {@code AILMENT_MAGNITUDE{blind}} × 承受方 taken 幅度。
     * 致盲施加的唯一幅度来源（命中致盲 + 标记致盲光环共用），使 {@code inc_blind_effect} 成为活消费者（R3 真读）。
     */
    public static double blindMagnitude(LivingEntity caster, LivingEntity victim) {
        return BLIND_BASE_MAGNITUDE * ailmentMagnitude(caster, StatusDefs.BLIND)
                * takenMagnitude(victim, StatusDefs.BLIND.id);
    }

    /**
     * 致盲时长（tick）：基础 80（4 秒）× 承受方 taken 时长倍率。
     * <b>只补 taken 侧</b>：施加方时长由 {@code StatusEngine} 中央 {@code scaleStatusDuration} 的 DEBUFF 分支
     * （{@code debuffAilmentDurationMultiplier} = AILMENT_DURATION{blind,!taken}）统一缩放，
     * 此处若再乘施加方会<b>双重缩放</b>（与冰缓/冰冻不同：后者 kind 非 DEBUFF，不走中央通路）。
     */
    public static int blindDurationTicks(LivingEntity caster, LivingEntity victim) {
        double mul = takenDurationMul(victim, StatusDefs.BLIND.id);
        return (int) Math.max(1, Math.round(BLIND_DURATION_TICKS * mul));
    }

    private static double[] getPoisonDamage(LivingEntity living, float[] damages) {
        return compositionDamage(living, damages, GateTag.POISON, Set.of(GateTag.PHYSICAL, GateTag.CHAOS));
    }

    /**
     * 点燃伤害组成：默认按 {FIRE} 贡献聚合（{@link #compositionDamage}）；额外叠加
     * 「火焰法术造成的混沌伤害亦计入点燃幅度」（StatId
     * {@code CHAOS_DAMAGE_FROM_FIRE_SPELLS_CONTRIBUTES_TO_FLAMMABILITY_AND_IGNITE_BUFF_MAGNITUDE}）——
     * 当本次命中来自<b>火焰法术</b>、且攻击者持 {@link com.cimdy.coe.data.codec.support.stat.effect.AilmentCompositionStatEffect}
     * 标记时，把命中的混沌伤害按标记权重（{@code Math.abs(markerMagnitude)}，节点值，flag 节点数据侧供 1.0）
     * 折入点燃的混沌组成槽（索引 1，与 {@link #DAMAGE_TYPE_TAGS} 对齐）。
     *
     * <p><b>严格作用域</b>：仅当 source 确为技能（{@link SkillDamageSource}）且其 stat 标签同时含
     *
     * <p>可燃性（Flammability）幅度未在此接线：其为 {@code StatusBehavior#curseResist} 上的标量折叠
     * （{@code base × AILMENT_MAGNITUDE{curse,!taken}}），与本次命中的混沌伤害组成无共同读点
     * （诅咒是常驻状态、非命中），故仅落地点燃半边。
     */
    private static double[] getIgniteDamage(DamageSource source, LivingEntity attacker, float[] damages) {
        double[] comp = compositionDamage(attacker, damages, GateTag.IGNITE, Set.of(GateTag.FIRE));
        if (attacker != null && isFireSpell(source)) {
            double weight = Math.abs(StatEngine.magnitudeOf(attacker, "chaos_fire_spell_ignite_magnitude", Set.of(GateTag.IGNITE, GateTag.CHAOS)));
            if (weight > 0.0) {
                // 索引对齐 DAMAGE_TYPE_TAGS：1=混沌（与 compositionDamage 同口径）。
                comp[1] += damages[1] * weight;
            }
        }
        return comp;
    }

    /** 本次命中是否来自「火焰法术」：技能源的 stat 标签同时含 spell 与 fire。 */
    private static boolean isFireSpell(DamageSource source) {
        if (!(source instanceof SkillDamageSource sds)) return false;
        SkillInstance si = sds.getSkill();
        if (si == null) return false;
        Set<String> tags = si.getSkill().getStatTags(si.getContext(), si.getSkillId());
        return tags.contains(GateTag.SPELL) && tags.contains(GateTag.FIRE);
    }

    private static float getChillDamage(LivingEntity living, float[] damages) {
        return contributionDamage(living, damages, GateTag.CHILL, Set.of(GateTag.COLD));
    }

    private static float getShockDamage(LivingEntity living, float[] damages) {
        return contributionDamage(living, damages, GateTag.SHOCK, Set.of(GateTag.LIGHTNING));
    }

    private static float getAllDamage(float[] damages) {
        float total = 0F;
        for (int i = 0; i < DamageTypes.width(); i++) total += damages[i];
        return total;
    }

}
