package com.cimdy.coe.data.codec.support.stat.effect.status;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStore;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.event.TriggerContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

/**
 * 触发亚型：施加任意统一状态（{@link StatusType}）——异常 / 减益 / 增益 / 充能 / 神龛 皆可。
 * 经统一状态系统 {@link StatusEngine#apply} 施加。
 *
 * <h2>唯一数值</h2>
 * 一条词缀只有一个数（条目 {@link StatEntry} 的 {@code amount}）。状态施加涉及
 * 「强度 / 时长 / 叠层」三个量，但<b>一条词缀只会改动其中一个</b>——由 {@link #aspect} 声明
 * 这个数是哪一个，其余两个一律取 {@link StatusType} 的定义值：
 * <ul>
 *   <li>{@link Aspect#MAGNITUDE}（默认）——数值 = 强度；时长取 {@code status.defaultDurationTicks}，叠层 1</li>
 *   <li>{@link Aspect#DURATION}——数值 = 时长（tick）；强度 1，叠层 1</li>
 *   <li>{@link Aspect#STACKS}——数值 = 叠层数；强度 1，时长取 {@code status.defaultDurationTicks}</li>
 * </ul>
 * 需要「3 秒 + 强度 30 的冰冻」= <b>两条词缀</b>：一条 {@code duration} 一条 {@code magnitude}，
 * 各自独立成一个条目。
 *
 * <p><b>符号语义</b>：状态施加是「施加型」效果，数值须为正——<b>正数 = 施加 / 增强状态</b>；
 * <b>负数与零 = 无效果</b>（状态施加无「反向移除」语义，非正数直接短路跳过）。</p>
 *
 * <h2>数据示例</h2>
 * 对事件目标施加强度 30 的冰冻（时长用冰冻自身定义）：
 * <pre>{@code
 * {"type": "status", "status": "freeze", "amount": 30.0, "target": "trigger_target", "tags": ["hit"]}
 * }</pre>
 * 冰冻持续 3 秒（60 tick）：
 * <pre>{@code
 * {"type": "status", "status": "freeze", "aspect": "duration", "amount": 60,
 *  "target": "trigger_target", "tags": ["hit"]}
 * }</pre>
 * 强度可用 math 综合多个来源，例「最大生命与最大魔力之和的 5%」：
 * <pre>{@code
 * {"type": "status", "status": "freeze",
 *  "amount": "(attribute:coe:max_health + attribute:coe:max_mana) * 0.05", "tags": ["hit"]}
 * }</pre>
 */
public class StatusStatEffect extends AbstractStatEffect {

    /** 本词缀的唯一数值对应状态的哪一个量；其余量取 {@link StatusType} 定义值。 */
    public enum Aspect implements StringRepresentable {
        /** 数值 = 状态强度（默认）。 */
        MAGNITUDE("magnitude"),
        /** 数值 = 状态持续时间（tick）。 */
        DURATION("duration"),
        /** 数值 = 施加叠层数。 */
        STACKS("stacks");

        public static final Codec<Aspect> CODEC = StringRepresentable.fromEnum(Aspect::values);

        private final String name;

        Aspect(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Direction implements StringRepresentable {
        ADD("add"),
        REMOVE("remove");

        public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    @Nullable
    private final StatusType status;
    private final Aspect aspect;
    private final Direction direction;
    private final List<String> kinds;
    private final List<String> statusTags;

    /**
     * 本效果是否<b>由光环授予</b>。为 true 时 {@link StatusEngine#apply} 走7 参重载，
     * 使 {@code effectMul} 额外乘入光环侧倍率 {@code {aura, !taken}}
     * （否则 {@code INC_AURA_MAGNITUDE} / {@code INC_BUFF_MAGNITUDE} 对本效果授予的状态恒乘 1.0）。
     * 仅 presence 光环两个 EFF 置 true；其余构造点一律走默认 false，行为不变。
     */
    private final boolean auraSourced;

    /** 规范构造：身份（状态类型）+ 语义（数值含义）+ 事件标签（布尔表达式）。 */
    public StatusStatEffect(StatusType status, Aspect aspect, GateExpr tags) {
        this(status, aspect, Direction.ADD, List.of(), List.of(), tags, false);
    }

    /** 全参构造：方向（ADD 施加 / REMOVE 移除）+ 状态身份（REMOVE 可为 null）
     *  + 语义 + 大类 / 标签筛选 + 事件标签（布尔表达式）。非光环来源。 */
    public StatusStatEffect(StatusType status, Aspect aspect, Direction direction,
                            List<String> kinds, List<String> statusTags, GateExpr tags) {
        this(status, aspect, direction, kinds, statusTags, tags, false);
    }

    /** 全参构造 + 光环来源标记。仅 presence 光环 EFF 传 {@code auraSourced=true}。 */
    public StatusStatEffect(StatusType status, Aspect aspect, Direction direction,
                            List<String> kinds, List<String> statusTags, GateExpr tags,
                            boolean auraSourced) {
        super(tags);
        this.status = status;
        this.aspect = aspect;
        this.direction = direction;
        this.kinds = kinds.isEmpty() ? List.of() : List.copyOf(kinds);
        this.statusTags = statusTags.isEmpty() ? List.of() : List.copyOf(statusTags);
        this.auraSourced = auraSourced;
    }

    /** 便捷构造：默认数值即强度，作用于事件目标，产出一条完整条目。 */
    public static StatEntry entry(StatusType status, ExpressionCodec.Value amount,
                                  Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(amount, conditions,
                new StatusStatEffect(status, Aspect.MAGNITUDE, GateExpr.of(tags)));
    }

    /**
     * 便捷构造：默认数值即强度，作用于<b>持有者自身（SELF）</b>，产出一条完整条目。
     * 用于「自身获得状态」类效果（如 Archon 授予——点燃敌人者获得 Elemental Archon，
     * 目标是施法者而非事件目标），目标选择器在条目层固定为 SELF，与亚型默认（事件目标）区分。
     */
    public static StatEntry selfEntry(StatusType status, ExpressionCodec.Value amount) {
        return StatEntry.of(amount, List.of(), StatTargets.ofTarget(TargetSelector.SELF),
                new StatusStatEffect(status, Aspect.MAGNITUDE, GateExpr.ALWAYS));
    }

    /** 便捷构造：触发型净化（REMOVE）条目，作用于持有者自身，无条件门控。 */
    public static StatEntry removeEntry(List<String> kinds, List<String> statusTags, Set<String> tags, List<ValueExpr> conditions) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), conditions,
                new StatusStatEffect(null, Aspect.MAGNITUDE, Direction.REMOVE, kinds, statusTags, GateExpr.of(tags)));
    }

    public StatusType status() {
        return status;
    }

    /** 唯一数值的语义。 */
    public Aspect aspect() {
        return aspect;
    }

    /** 施加/移除方向（默认 ADD）。 */
    public Direction direction() {
        return direction;
    }

    /** 移除筛选：状态大类名（大小写无关）。 */
    public List<String> kinds() {
        return kinds;
    }

    /** 移除筛选：状态标签。 */
    public List<String> statusTags() {
        return statusTags;
    }

    /** 是否由光环授予（决定 effectMul 是否乘入 {@code {aura, !taken}} 侧倍率）。 */
    public boolean auraSourced() {
        return auraSourced;
    }

    public static final MapCodec<StatusStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StatusType.CODEC.optionalFieldOf("status").forGetter(e -> Optional.ofNullable(e.status)),
            Aspect.CODEC.optionalFieldOf("aspect", Aspect.MAGNITUDE).forGetter(StatusStatEffect::aspect),
            Direction.CODEC.optionalFieldOf("direction", Direction.ADD).forGetter(StatusStatEffect::direction),
            Codec.STRING.listOf().optionalFieldOf("kinds", List.of()).forGetter(StatusStatEffect::kinds),
            Codec.STRING.listOf().optionalFieldOf("status_tags", List.of()).forGetter(StatusStatEffect::statusTags),
            Codec.BOOL.optionalFieldOf("aura_sourced", false).forGetter(StatusStatEffect::auraSourced),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, (statusOpt, aspect, direction, kinds, statusTags, auraSourced, tags) ->
            new StatusStatEffect(statusOpt.orElse(null), aspect, direction, kinds, statusTags, tags, auraSourced)));

    @Override
    public String type() {
        return "status";
    }

    /** 状态亚型默认作用于事件目标。 */
    @Override
    public StatTargets defaultTargets() {
        return StatTargets.ofTarget(TargetSelector.EVENT_TARGET);
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        if (direction == Direction.REMOVE) {
            removeStatuses(entry, evalCtx, scope);
            return;
        }
        // 唯一数值：按持有者（施加者）上下文求值，再按 aspect 落到对应的量上。
        // 经 scope.evalBase 注入事件名/命中上下文（与 GainCharge/Recoup 同族一致，三遍重检 L1）
        double v = entry.evaluateAmount(scope.evalBase(evalCtx.owner()));
        // 施加型效果：数值须为正（正数 = 施加/增强状态；负数 = 无效果——状态施加无「反向」语义）
        if (v <= 0) return;
        float magnitude = aspect == Aspect.MAGNITUDE ? (float) v : 1.0F;
        int baseDuration = aspect == Aspect.DURATION ? (int) v : durationFallback(entry);
        int stacks = aspect == Aspect.STACKS ? (int) v : 1;
        // Archon 增益时长缩放（语料「X% increased Archon Buff duration」）：对 archon 标签状态的
        // 缺省时长（aspect 非 DURATION 时）经 {archon} 作用域读 ARCHON_BUFF_DURATION 倍率缩放。
        final int duration;
        if (aspect != Aspect.DURATION && status.tags().contains("archon")) {
            duration = (int) Math.max(1, Math.round(baseDuration * StatusEngine.archonBuffDurationMultiplier(evalCtx.owner())));
        } else {
            duration = baseDuration;
        }
        entry.forEachTarget(evalCtx, scope,
                t -> {
                    // 走7 参重载传入 auraSourced：光环授予时 effectMul 才乘入 {aura, !taken} 侧倍率。
                    // 注意必须经此重载才会重算 effectMul——单改 StatusInstance.auraSourced（死存储、零读点）无效。
                    StatusEngine.apply(t, status, evalCtx.owner(), magnitude, duration, stacks, auraSourced);
                    if (status.tags().contains("ailment")) {
                        AilmentHelper.fireStatusCaused(evalCtx.owner(), t, status.id);
                    }
                });
    }

    /**
     * 移除型路径（direction=REMOVE）：按大类（kinds）+ 标签（status_tags）经
     * {@link StatusEngine#clear} 中央出口移除目标身上的状态，移植自原独立净化效果（已并入本类 REMOVE 路径）。
     */
    private void removeStatuses(StatEntry entry, StatEvalContext evalCtx, @Nullable StatContext scope) {
        Set<String> wantedKinds = kinds.stream()
                .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        entry.forEachTarget(evalCtx, scope, t -> {
            if (!(t instanceof LivingEntity who) || !who.hasData(AttachmentRegistry.STATUS_STORE)) return;
            StatusStore store = who.getData(AttachmentRegistry.STATUS_STORE);
            List<StatusType> hit = new ArrayList<>();
            for (StatusType type : StatusType.all()) {
                if (!store.has(type)) continue;
                if (wantedKinds.contains(type.kind.name().toLowerCase(Locale.ROOT))) {
                    hit.add(type);
                } else {
                    for (String tag : statusTags) {
                        if (type.tags().contains(tag)) {
                            hit.add(type);
                            break;
                        }
                    }
                }
            }
            // 先收集再移除：StatusEngine.clear 会改动 store，边遍历边删会漏项
            for (StatusType type : hit) StatusEngine.clear(who, type);
        });
    }

    /**
     * 本次施加的状态时长：<b>只有 {@link StatDuration.Kind#DURATION}（「施加后持续多久」）才覆盖状态自带时长</b>，
     * 其余一律回落 {@code status.defaultDurationTicks}（chill 40t / freeze 80t / wither 80t / onslaught 80t …，
     * 均为 POE2 基础持续量级，已烘焙在状态定义里；def 未显式 {@code duration(...)} 的状态取
     * {@code StatusTypeDef.Builder} 默认值 80t，如 wither 与 onslaught——不可凭印象写具体数值）。
     *
     * <p>为什么 INTERVAL / COOLDOWN 不能当时长：INTERVAL 是「每多久重新施加一次」的<b>节拍</b>、
     * COOLDOWN 是「再次触发前的最短间隔」，二者都<b>不表达本次施加的有效期</b>。把它们当时长用，
     * 会把状态定义里的真值悄悄改掉（实例：父条目 {@code interval:20} 曾让冰缓从 40t 变 20t，
     * 而注释仍写「冰缓时长取 2s（40t）」——注释谎报实际行为）。
     */
    private int durationFallback(StatEntry entry) {
        StatDuration d = entry.duration();
        if (d == null || d.kind() != StatDuration.Kind.DURATION) return status.defaultDurationTicks;
        return d.ticks();
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
     * 本地化效果片段：硬编码 {@code "apply status <id>"} → 模板 {@code stat_effect.apply_status}
     * + 状态名值键 {@code stat.value.status.<id>}（复用 LangProvider#STAT_VALUE_ROWS 已有键）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        if (direction == Direction.REMOVE) {
            List<Component> out = new ArrayList<>();
            for (String k : kinds) out.add(Component.translatable("stat_effect.cleanse", Component.literal(k)));
            for (String tg : statusTags) out.add(Component.translatable("stat_effect.cleanse", Component.literal(tg)));
            return out;
        }
        Component statusName = StatValueKey.of("status", status.id);
        return List.of(Component.translatable("stat_effect.apply_status", statusName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusStatEffect e)) return false;
        return auraSourced == e.auraSourced && aspect == e.aspect && direction == e.direction
                && Objects.equals(status, e.status)
                && Objects.equals(kinds, e.kinds) && Objects.equals(statusTags, e.statusTags)
                && Objects.equals(tags(), e.tags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, aspect, direction, kinds, statusTags, tags(), auraSourced);
    }

    @Override
    public String toString() {
        return "StatusStatEffect{status=" + (status == null ? "null" : status.id)
                + ", aspect=" + aspect + ", direction=" + direction
                + ", kinds=" + kinds + ", statusTags=" + statusTags + ", tags=" + tags()
                + ", auraSourced=" + auraSourced + "}";
    }
}
