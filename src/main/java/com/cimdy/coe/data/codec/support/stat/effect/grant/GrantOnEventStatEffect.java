package com.cimdy.coe.data.codec.support.stat.effect.grant;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.LeechStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.engine.ground.VolatilityDetonationManager;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.skill.helper.AncestralBoostAttacks;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.def.StatusType;


import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * 触发亚型：事件授予（Grant On Event）——「X% chance on [事件] to [授予 状态/充能/连击/资源]」。
 * <p>事件标签（如 {@code caused_hit} / {@code kill}）由 {@link GateExpr} 限定，经
 * {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 求值（与 {@link LeechStatEffect} 同构触发通道）。
 * <p><b>数值语义</b>：条目 {@code amount} = 触发概率（0~1）。掷骰命中后按 {@code target} 授予：
 * <ul>
 *   <li>{@link GrantTarget#STATUS}：对事件目标施加 {@code status}（强度 1.0 / 100 ticks 近似）；</li>
 *   <li>{@link GrantTarget#CHARGE}：对持有者经 {@link ResourceRegistry#generateCharge} 授予 {@code chargeType} 1 层；</li>
 *   <li>{@link GrantTarget#RESOURCE}：对持有者授予 {@code resource} 指定量（Energy/Volatility）；</li>
 *   <li>{@link GrantTarget#COMBO}：对持有者经 {@link ResourceRegistry#buildCombo} 构建 1 层连击。</li>
 * </ul>
 * 授予量由 {@code grantResource} / {@code StatusEngine.apply} 等中央入口固定（如 volatility=10），amount 恒为触发概率。
 * <p>充能/连击/资源授予经各系统<b>中央入口</b>（ResourceRegistry.generateCharge / ResourceRegistry.buildCombo / EnergyHelper /
 * VolatilityHelper），与「回调 + 中央入口」原则一致；本效果只负责触发与概率判定。
 */
public final class GrantOnEventStatEffect extends AbstractStatEffect {

    /** 授予目标类型。 */
    public enum GrantTarget implements StringRepresentable {
        STATUS,
        CHARGE,
        RESOURCE,
        COMBO,
        ANCESTRAL_BOOST;

        public static final Codec<GrantTarget> CODEC = StringRepresentable.fromEnum(GrantTarget::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    private final GrantTarget target;
    private final String param;      // status id / charge type name / resource name / combo id
    /** 授予时长缩放属性（null = 固定 {@code baseDurationTicks}）。缩放修饰符在属性专属作用域（如 puppet_master）内 fold。 */
    private final Holder<Attribute> durationAttr;
    /** 时长基座（ticks）：授予时长 = baseDurationTicks × (1 + fold(durationAttr))。默认 100。 */
    private final double baseDurationTicks;
    /** 状态强度缩放属性（null = 固定 1.0）。强度 = 1 × (1 + fold(intensityAttr))（下界钳制，复用 {@link StatEngine#scaledMagnitude}）。 */
    private final Holder<Attribute> intensityAttr;
    /** 状态层数缩放属性（null = 固定 1）。层数 = 1 × (1 + fold(stackAttr))（下界钳制，复用 {@link StatEngine#scaledMagnitude}）。 */
    private final Holder<Attribute> stackAttr;
    /**
     * 数值即强度模式：为 true 时 {@code entry.amount()} 直接作为授予状态强度（magnitude），
     * 触发视为必定发生（chance = 1.0，跳过掷骰）。用于「重晕/钉刺敌人时破甲 X% 护甲」类——
     * 节点数值 X% 即破甲 magnitude，触发条件本身为「一旦重晕即发生」，无独立几率通道。
     */
    private final boolean amountIsMagnitude;
    /** 固定层数（stackAttr 为 null 时生效）：非缩放，授予固定层数。默认 1。用于「施加 X 层」类（如施加 10 层 Incision）。 */
    private final int baseStacks;
    /**
     * 强度基座（默认 1.0）：授予强度 = {@code baseMagnitude} × 强度倍率。
     * POE2 部分状态有固定基础强度（恐吓 = ±10%），缺省 1.0 会授予 1% 的错误量纲（#48 同型）。
     */
    private final double baseMagnitude;
    /**
     * 数值即时长（秒）模式：为 true 时 {@code entry.amount()} 直接作为授予时长（秒 × 20 → ticks），
     * 触发视为必定发生（chance = 1.0，跳过掷骰）。用于「你重晕的敌人被恐吓 {X} 秒」类——
     */
    private final boolean amountIsDuration;

    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags) {
        this(target, param, tags, null, 100.0, null, null, false, 1);
    }

    /** 固定层数便捷构造（stackAttr 为 null，层数 = baseStacks），例如「施加 10 层」类状态授予。 */
    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags, int baseStacks) {
        this(target, param, tags, null, 100.0, null, null, false, baseStacks);
    }

    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags,
                                  @Nullable Holder<Attribute> durationAttr, double baseDurationTicks) {
        this(target, param, tags, durationAttr, baseDurationTicks, null, null, false, 1);
    }

    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags,
                                  @Nullable Holder<Attribute> durationAttr, double baseDurationTicks,
                                  @Nullable Holder<Attribute> intensityAttr, @Nullable Holder<Attribute> stackAttr) {
        this(target, param, tags, durationAttr, baseDurationTicks, intensityAttr, stackAttr, false, 1);
    }

    /** 数值即强度模式：amount 直接作为授予状态 magnitude，触发必定发生（chance = 1.0）。 */
    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags, boolean amountIsMagnitude) {
        this(target, param, tags, null, 100.0, null, null, amountIsMagnitude, 1);
    }

    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags,
                                  @Nullable Holder<Attribute> durationAttr, double baseDurationTicks,
                                  @Nullable Holder<Attribute> intensityAttr, @Nullable Holder<Attribute> stackAttr,
                                  boolean amountIsMagnitude, int baseStacks) {
        this(target, param, tags, durationAttr, baseDurationTicks, intensityAttr, stackAttr,
                amountIsMagnitude, baseStacks, 1.0, false);
    }

    /** 强度基座 + 数值即时长：见 {@link #baseMagnitude} / {@link #amountIsDuration}。 */
    public GrantOnEventStatEffect(GrantTarget target, String param, GateExpr tags,
                                  @Nullable Holder<Attribute> durationAttr, double baseDurationTicks,
                                  @Nullable Holder<Attribute> intensityAttr, @Nullable Holder<Attribute> stackAttr,
                                  boolean amountIsMagnitude, int baseStacks,
                                  double baseMagnitude, boolean amountIsDuration) {
        super(tags);
        this.target = target;
        this.param = param;
        this.durationAttr = durationAttr;
        this.baseDurationTicks = baseDurationTicks;
        this.intensityAttr = intensityAttr;
        this.stackAttr = stackAttr;
        this.amountIsMagnitude = amountIsMagnitude;
        this.baseStacks = baseStacks;
        this.baseMagnitude = baseMagnitude;
        this.amountIsDuration = amountIsDuration;
    }

    public static final MapCodec<GrantOnEventStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GrantTarget.CODEC.fieldOf("target").forGetter(GrantOnEventStatEffect::target),
            Codec.STRING.fieldOf("param").forGetter(GrantOnEventStatEffect::param),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags),
            Attribute.CODEC.optionalFieldOf("duration_attr").forGetter(e -> Optional.ofNullable(e.durationAttr)),
            Codec.DOUBLE.optionalFieldOf("base_duration", 100.0).forGetter(GrantOnEventStatEffect::baseDurationTicks),
            Attribute.CODEC.optionalFieldOf("intensity_attr").forGetter(e -> Optional.ofNullable(e.intensityAttr)),
            Attribute.CODEC.optionalFieldOf("stack_attr").forGetter(e -> Optional.ofNullable(e.stackAttr)),
            Codec.BOOL.optionalFieldOf("amount_is_magnitude", false).forGetter(GrantOnEventStatEffect::amountIsMagnitude),
            Codec.INT.optionalFieldOf("base_stacks", 1).forGetter(GrantOnEventStatEffect::baseStacks),
            Codec.DOUBLE.optionalFieldOf("base_magnitude", 1.0).forGetter(GrantOnEventStatEffect::baseMagnitude),
            Codec.BOOL.optionalFieldOf("amount_is_duration", false).forGetter(GrantOnEventStatEffect::amountIsDuration)
    ).apply(i, (t, p, tg, da, bd, ia, sa, am, bs, bm, ad) ->
            new GrantOnEventStatEffect(t, p, tg, da.orElse(null), bd, ia.orElse(null), sa.orElse(null), am, bs, bm, ad)));

    public GrantTarget target() {
        return target;
    }

    public String param() {
        return param;
    }

    public double baseDurationTicks() {
        return baseDurationTicks;
    }

    /** 状态强度缩放属性（null = 固定 1.0）。 */
    public Holder<Attribute> intensityAttr() {
        return intensityAttr;
    }

    /** 状态层数缩放属性（null = 固定 1）。 */
    public Holder<Attribute> stackAttr() {
        return stackAttr;
    }

    /** 数值即强度模式（amount 直接作为授予状态 magnitude，触发必定发生）。 */
    public boolean amountIsMagnitude() {
        return amountIsMagnitude;
    }

    /** 固定层数（stackAttr 为 null 时生效）。 */
    public int baseStacks() {
        return baseStacks;
    }

    /** 强度基座：授予强度 = baseMagnitude × 强度倍率。 */
    public double baseMagnitude() {
        return baseMagnitude;
    }

    /** 数值即时长（秒）模式。 */
    public boolean amountIsDuration() {
        return amountIsDuration;
    }

    /** 便捷构造：状态授予（amount 默认 $value 模板）。 */
    public static StatRead statusEntry(StatusType status, Set<String> tags) {
        return statusEntry(status, ExpressionCodec.VALUE, tags);
    }

    /** 便捷构造：状态授予（带时长缩放属性，amount 默认 $value 模板）。 */
    public static StatRead statusEntry(StatusType status, Set<String> tags,
                                        Holder<Attribute> durationAttr, double baseDurationTicks) {
        return statusEntry(status, ExpressionCodec.VALUE, tags, durationAttr, baseDurationTicks);
    }

    /** 便捷构造：状态授予（带强度/层数属性缩放，amount 默认 $value 模板）。 */
    public static StatRead statusEntry(StatusType status, Set<String> tags,
                                        @Nullable Holder<Attribute> intensityAttr, @Nullable Holder<Attribute> stackAttr) {
        return statusEntry(status, ExpressionCodec.VALUE, tags, intensityAttr, stackAttr);
    }

    /** 便捷构造：状态授予（amount 显式）。 */
    public static StatRead statusEntry(StatusType status, ExpressionCodec.Value chance, Set<String> tags) {
        return StatEntry.of(chance, List.of(),
                new GrantOnEventStatEffect(GrantTarget.STATUS, status.id, GateExpr.of(tags)));
    }

    /**
     * 便捷构造：状态授予（带时长缩放属性）。授予时长 = {@code baseDurationTicks} × (1 + 计算修饰符)，
     * 计算修饰符来自 {@code durationAttr}（如 {@code inc_duration_of_puppet_master} 缩放 puppet_master 基础时间），
     * 在其专属作用域（puppet_master）内 fold 读取——缩放属性应「在基础时间属性上计算」，不可移除。
     */
    public static StatRead statusEntry(StatusType status, ExpressionCodec.Value chance, Set<String> tags,
                                        Holder<Attribute> durationAttr, double baseDurationTicks) {
        return StatEntry.of(chance, List.of(),
                new GrantOnEventStatEffect(GrantTarget.STATUS, status.id, GateExpr.of(tags),
                        durationAttr, baseDurationTicks));
    }

    /**
     * 便捷构造：状态授予（带强度/层数属性缩放）。状态强度 = 1 × (1 + fold(intensityAttr))、
     * 层数 = 1 × (1 + fold(stackAttr))（下界钳制，复用 {@link StatEngine#scaledMagnitude}），
     * 对应 POE2「+X% 状态效果强度」「+X% 状态层数」基石语义（未配置的属性固定 1）。
     */
    public static StatRead statusEntry(StatusType status, ExpressionCodec.Value chance, Set<String> tags,
                                        @Nullable Holder<Attribute> intensityAttr, @Nullable Holder<Attribute> stackAttr) {
        return StatEntry.of(chance, List.of(),
                new GrantOnEventStatEffect(GrantTarget.STATUS, status.id, GateExpr.of(tags),
                        null, 100.0, intensityAttr, stackAttr));
    }

    /** 便捷构造：资源授予（amount = 概率，授予量 = 1 单位）。 */
    public static StatRead resourceEntry(ResourceType resource, double chance, Set<String> tags) {
        return StatEntry.of(ExpressionCodec.of(chance), List.of(),
                new GrantOnEventStatEffect(GrantTarget.RESOURCE, resource.getSerializedName(), GateExpr.of(tags)));
    }

    /** 便捷构造：资源授予（amount = $value 模板，概率由条目数值驱动，授予量 = 固定量）。 */
    public static StatRead resourceEntry(ResourceType resource, ExpressionCodec.Value chance, Set<String> tags) {
        return StatEntry.of(chance, List.of(),
                new GrantOnEventStatEffect(GrantTarget.RESOURCE, resource.getSerializedName(), GateExpr.of(tags)));
    }

    @Override
    public String type() {
        return "grant_on_event";
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        double amount = entry.amount().getValue();
        // 「数值即时长」模式下 amount 是秒数而非概率：触发必定发生，秒数 ≤0 视为无效不授予。
        if (amountIsDuration && amount <= 0) return;
        double chance = (amountIsMagnitude || amountIsDuration) ? 1.0 : amount;
        if (chance <= 0) return;
        if (evalCtx.owner().getRandom().nextDouble() >= chance) return;
        LivingEntity owner = evalCtx.owner();
        switch (target) {
            case STATUS -> {
                LivingEntity t = evalCtx.triggerTarget();
                if (t == null) return;
                int duration = amountIsDuration
                        ? (int) Math.max(1, Math.round(amount * 20))
                        // durationAttr 为 null 时缩放项为 0，时长即基座 baseDurationTicks 本身；
                        // 原写死 100 会静默丢弃基座（如致残 80 ticks / 亡灵大君 200 ticks），故改为取基座。
                        : (durationAttr == null ? (int) Math.round(baseDurationTicks) : computeDuration(owner));
                int intensity = amountIsMagnitude
                        ? (int) Math.round(amount)
                        : (int) Math.round(StatEngine.scaledMagnitude(owner, 1.0, intensityAttr));
                int stacks = stackAttr == null ? baseStacks : (int) Math.round(StatEngine.scaledMagnitude(owner, 1.0, stackAttr));
                double magnitude = baseMagnitude * intensity;
                // 穿刺幅度缩放（施加者侧 {impale,spell,not taken} 作用域读取 IMPALE_MAGNITUDE）：
                // INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS 的真实读点，仅对穿刺生效，非死线。
                if (statusType(param) == StatusDefs.IMPALE) {
                    magnitude *= readImpaleMagnitude(owner);
                } else if (statusType(param) == StatusDefs.INCISION) {
                    // 创口幅度缩放（施加者侧 {incision, !taken} 作用域读取 AILMENT_MAGNITUDE）：
                    // INC_INCISION_MAGNITUDE / INC_EFFECT_OF_INCISION 的真实读点，仅对创口生效，非死线。
                    // 缩放落在 magnitude（=「每层 1 个单位」）上，由 StatusBehavior.incisionBleedChance 消费。
                    magnitude *= readIncisionMagnitude(owner);
                }
                StatusEngine.apply(t, statusType(param), owner, magnitude, duration, stacks, false);
            }
            case CHARGE -> ResourceRegistry.generateCharge(owner, chargeType(param), 1, 3, -1, null, null);
            case RESOURCE -> grantResource(owner, param);
            case COMBO -> ResourceRegistry.buildCombo(owner, param, 1, 40, java.util.List.<Action>of(), null, null, null,
                    owner.level().getGameTime());
            case ANCESTRAL_BOOST -> AncestralBoostAttacks.grantFromHeavyStun(owner, baseStacks);
        }
    }

    private StatusType statusType(String id) {
        return StatusType.byId(id).orElseThrow(() -> new IllegalStateException("Unknown status: " + id));
    }

    /**
     * 施加者侧读 IMPALE_MAGNITUDE（{impale,spell,not taken} 作用域，base=1.0，ADD_MULTIPLIED_BASE 累积）。
     * 镜像 ResourceRegistry.GlorySave.generate：进入作用域后经 getAttributeValue（LivingAttributeValue mixin → StatEngine.fold 含 base）即时折叠。
     * 仅当施加者为穿刺(impale)时调用；无施加者返回 1.0（不改变基线）。
     * 注：不使用 {@code StatEngine.foldDelta(owner, IMPALE_MAGNITUDE)} —— 增量语义会丢弃 base 1.0，
     * 使 magnitude 退化为 base*Σinc（无 stat 时归零、炸基线），故走 getAttributeValue（fold 以真实当前值为 original）。
     * 两种语义现已分属独立缓存条目（fold / foldDelta），但 base 丢失是语义问题、与缓存无关，仍须走读路径。
     */
    private static double readImpaleMagnitude(LivingEntity owner) {
        if (owner == null) return 1.0;
        AutoCloseable s = StatScopes.enter(owner, StatContext.of(Set.of(GateTag.IMPALE, GateTag.SPELL, GateTag.not(GateTag.TAKEN)), null));
        try {
            return owner.getAttributeValue(AttributeRegistry.IMPALE_MAGNITUDE);
        } finally {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 施加者侧读 AILMENT_MAGNITUDE（{incision, not taken} 作用域，base=1.0，ADD_MULTIPLIED_BASE 累积）。
     * 镜像 {@link #readImpaleMagnitude}：进入作用域后经 getAttributeValue
     * （LivingAttributeValue mixin → StatEngine.fold 含 base）即时折叠。
     * 仅当施加者为创口(incision)时调用；无施加者返回 1.0（不改变基线）。
     * 注：不使用 {@code StatEngine.foldDelta(owner, AILMENT_MAGNITUDE)} —— 增量语义会丢弃 base 1.0，
     * 使 magnitude 退化为 base*Σinc（无 stat 时归零、炸基线），故走 getAttributeValue（fold 以真实当前值为 original）。
     * 两种语义现已分属独立缓存条目（fold / foldDelta），但 base 丢失是语义问题、与缓存无关，仍须走读路径。
     */
    private static double readIncisionMagnitude(LivingEntity owner) {
        if (owner == null) return 1.0;
        AutoCloseable s = StatScopes.enter(owner, StatContext.of(Set.of(GateTag.INCISION, GateTag.not(GateTag.TAKEN)), null));
        try {
            return owner.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
        } finally {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 计算授予状态时长：基座 {@code baseDurationTicks} × (1 + 计算修饰符)。
     * 计算修饰符来自 {@link #durationAttr}（如 {@code inc_duration_of_puppet_master} 缩放 puppet_master 基础时间），
     * 在其专属作用域（puppet_master）内 fold 读取——缩放属性应「在基础时间属性上计算」，不可移除。
     */
    private int computeDuration(LivingEntity owner) {
        AutoCloseable s = StatScopes.enter(owner, StatContext.of("puppet_master"));
        try {
            double scale = StatEngine.foldDelta(owner, durationAttr);
            return (int) Math.round(baseDurationTicks * (1.0 + Math.max(0.0, scale)));
        } finally {
            try {
                s.close();
            } catch (Exception ignored) {
                // StatScope.close() 不抛受检异常，此处仅满足 AutoCloseable.close() 契约
            }
        }
    }

    private ChargeType chargeType(String name) {
        for (ChargeType c : ChargeType.values()) {
            if (c.getSerializedName().equals(name)) return c;
        }
        throw new IllegalStateException("Unknown charge type: " + name);
    }

    private void grantResource(LivingEntity owner, String resource) {
        switch (resource) {
            // 元技能能量授予（参考荣耀；无技能上下文 → "default" 槽）
            case "energy" -> ResourceRegistry.generateMetaEnergy(owner, "default", 10,
                    owner.level().getGameTime());
            case "volatility" -> ResourceRegistry.gainVolatility(owner, 10);
            // volatility_max：引爆时「回收等量波动」——波动积满才引爆、引爆末尾清零（VolatilityDetonationManager.detonate:95），
            // 故「等价量」= 引爆前满值（maxV）；此处直接置回满值即忠实复原 PoE2「regain an Equivalent Amount of Volatility」。
            case "volatility_max" -> ResourceRegistry.setVolatility(owner, ResourceRegistry.getMaxVolatility(owner));
            // health_max：击杀回满生命（PoE2「Recover all Life」= 瞬间回满=setHealth(max)），忠实复原，区别于按比例回血。
            case "health_max" -> owner.setHealth(owner.getMaxHealth());
            // rage_max：命中伤害获怒时有几率将怒气补满至上限（PoE2 "instead gain up to your Maximum Rage"）。
            // 经 ResourceRegistry.gainRage 走统一资源枢纽 → 刷新固有流失宽限 + RESOURCE_GAIN 事件，与常规获怒同生命周期。
            case "rage_max" -> ResourceRegistry.gainRage(owner, ResourceRegistry.getMaxRage(owner) - ResourceRegistry.getRage(owner), "stat");
            default -> { /* 其余资源走各自 Helper（glory 等暂未接入事件触发） */ }
        }
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    @Override
    public List<Component> describeFragment(StatEntry entry) {
        return describeFragment(entry, Locale.ROOT);
    }

    /**
     * 本地化效果片段：硬编码 {@code grant_on_event[status:poison] @0.25} → 模板 {@code stat_effect.grant_on_event}
     * + 授予目标类型值键 {@code stat.value.grant_target.<t>}
     * + 参数值键（status→{@code stat.value.status.<id>}、charge→{@code stat.value.charge.<type>}、
     * resource→复用 {@code stat.value.status.<id>}、combo→{@code stat.value.grant_target.combo}（已注册）、
     * ancestral_boost→数值回落 humanize）+ 概率数值（走 {@link StatNumberFormat}）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        Component targetComp = StatValueKey.of("grant_target", target.getSerializedName());
        Component paramComp = switch (target) {
            case STATUS -> StatValueKey.of("status", param);
            case CHARGE -> StatValueKey.of("charge", param);
            case RESOURCE -> StatValueKey.of("status", param);
            case COMBO -> StatValueKey.of("grant_target", param);
            case ANCESTRAL_BOOST -> Component.literal(param);
        };
        Component chance = Component.literal(StatNumberFormat.format(entry.amount().getValue(), locale));
        return List.of(Component.translatable("stat_effect.grant_on_event", targetComp, paramComp, chance));
    }
}
