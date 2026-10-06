package com.cimdy.coe.data.codec.support.stat.effect.convert;

import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 伤害类型转换（fold 型，非触发）：声明「把一种 POE 伤害类型的部分份额，按 {@link #mode()} 在减免前重路由到另一种」。
 *
 * <p>POE 伤害类型（非原版 DamageType）= {@code physical / chaos / fire / cold / lightning}，
 * 对应 {@code DamageTypeRegistry.DAMAGE_TYPE_TAGS} 的五个槽位。
 *
 * <h2>三种模式</h2>
 * <ul>
 *   <li>{@code REPLACE}（去A取B）：从 {@code from} 扣除 ratio 份额并加到 {@code to}，源类型伤害被替代。</li>
 *   <li>{@code GAIN}（共存 / 也获得B）：保留 {@code from} 全部伤害，额外以 {@code to} 承受 ratio 份额。</li>
 *   <li>{@code NULLIFY}（归零 / 禁止造成）：按 ratio 把 {@code from} 来源槽位直接扣除，<b>不转入任何目标类型</b>。</li>
 * </ul>
 *
 * <h2>目标类型解析（合并原 RandomConvertStatEffect）</h2>
 * 原 {@code RandomConvertStatEffect}（「随机/轮流元素额外伤害」）与本类仅差<b>目标类型是否固定</b>，
 * 现由 {@link ToMode} 统一承载：
 * <ul>
 *   <li>{@link ToMode#FIXED}（默认）：目标类型即 {@link #to()} 常量字段；</li>
 *   <li>{@link ToMode#RANDOM} / {@link ToMode#CYCLE}：目标类型按模式从 {@link #candidates()} 候选列表解析
 *       （{@link #resolvedTo} 委托 {@link StatEngine#resolveRandomTo}，与承伤侧共用同一计数器），输出恒为 GAIN 语义。</li>
 * </ul>
 *
 * <p>数值（转换比例 0~1）是条目唯一数值；本类只携身份（from/to 或 candidates）与语义（mode/toMode）。
 */
public final class DamageConvertStatEffect extends AbstractStatEffect {

    /** 目标类型解析模式（合并原 RandomConvertStatEffect.Mode）。 */
    public enum ToMode implements StringRepresentable {
        FIXED,
        RANDOM,
        CYCLE;

        public static final Codec<ToMode> CODEC = StringRepresentable.fromEnum(ToMode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final MapCodec<DamageConvertStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("from")
                    .forGetter(DamageConvertStatEffect::fromTags),
            Codec.STRING.fieldOf("to").forGetter(DamageConvertStatEffect::to),
            Mode.CODEC.fieldOf("mode").forGetter(DamageConvertStatEffect::mode),
            Codec.BOOL.optionalFieldOf("hits_only", false).forGetter(DamageConvertStatEffect::hitsOnly),
            Codec.BOOL.optionalFieldOf("taken", false).forGetter(DamageConvertStatEffect::taken),
            ToMode.CODEC.optionalFieldOf("to_mode", ToMode.FIXED).forGetter(DamageConvertStatEffect::toMode),
            Codec.STRING.listOf().optionalFieldOf("candidates", List.of()).forGetter(DamageConvertStatEffect::candidates),
            AbstractStatEffect.TAGS_CODEC.optionalFieldOf("from_context", GateExpr.ALWAYS).forGetter(DamageConvertStatEffect::fromContext),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, (from, to, mode, hitsOnly, taken, toMode, candidates, fromContext, tags) ->
            new DamageConvertStatEffect(from, to, mode, hitsOnly, taken, toMode, candidates, fromContext, tags)));

    /** 合法来源/目标类型集合（与 {@code DamageTypeRegistry.DAMAGE_TYPE_TAGS} 一致，全部引用 {@link GateTag}）。 */
    public static final Set<String> VALID_TYPES = Set.of(
            GateTag.PHYSICAL, GateTag.CHAOS, GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING);

    private final Set<String> fromTags;
    private final String to;
    private final Mode mode;
    /** 仅命中(hits)生效、不作用于持续伤害(DoT)的门控。 */
    private final boolean hitsOnly;
    /** 是否同时作用于【承受】伤害（维度B 进项转伤门控）：默认 false = 仅外发；true = 外发与承受均按本规则转类型。 */
    private final boolean taken;
    /** 目标类型解析模式：FIXED 用 {@link #to()}；RANDOM/CYCLE 用 {@link #candidates()} 解析。 */
    private final ToMode toMode;
    /** RANDOM/CYCLE 候选伤害类型（FIXED 时为空）。 */
    private final List<String> candidates;
    /** 命中上下文门控（GateExpr）：缺省恒真 = 不门控。声明（如 {@code ["crit"]} / {@code ["ailment"]}）时
     *  仅当本次命中上下文标签命中才执行转换（参照 Recoup.fromType 的「按条件选基数」范式，轴为命中上下文而非伤害分量）。 */
    private final GateExpr fromContext;

    public DamageConvertStatEffect(Set<String> fromTags, String to, Mode mode, boolean hitsOnly, GateExpr tags) {
        this(fromTags, to, mode, hitsOnly, false, ToMode.FIXED, List.of(), GateExpr.ALWAYS, tags);
    }

    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public DamageConvertStatEffect(Set<String> fromTags, String to, Mode mode, boolean hitsOnly) {
        this(fromTags, to, mode, hitsOnly, false, ToMode.FIXED, List.of(), GateExpr.ALWAYS, GateExpr.ALWAYS);
    }

    /** 无命中上下文门控（fromContext = {@link GateExpr#ALWAYS}）便捷构造。 */
    public DamageConvertStatEffect(Set<String> fromTags, String to, Mode mode, boolean hitsOnly,
                                   ToMode toMode, List<String> candidates, GateExpr tags) {
        this(fromTags, to, mode, hitsOnly, false, toMode, candidates, GateExpr.ALWAYS, tags);
    }

    /** 无命中上下文门控、无事件标签门控便捷构造。 */
    public DamageConvertStatEffect(Set<String> fromTags, String to, Mode mode, boolean hitsOnly,
                                   ToMode toMode, List<String> candidates) {
        this(fromTags, to, mode, hitsOnly, false, toMode, candidates, GateExpr.ALWAYS, GateExpr.ALWAYS);
    }

    public DamageConvertStatEffect(Set<String> fromTags, String to, Mode mode, boolean hitsOnly, boolean taken,
                                   ToMode toMode, List<String> candidates, GateExpr fromContext, GateExpr tags) {
        super(tags);
        for (String f : fromTags)
            if (!VALID_TYPES.contains(f))
                throw new IllegalArgumentException("DamageConvertStatEffect.from 每个元素必须是 GateTag 五伤害类型之一，收到: " + f);
        if (toMode == ToMode.FIXED && mode != Mode.NULLIFY && !VALID_TYPES.contains(to))
            throw new IllegalArgumentException("DamageConvertStatEffect.to 必须是 GateTag 五伤害类型之一（NULLIFY 模式除外），收到: " + to);
        if (toMode != ToMode.FIXED) {
            if (candidates == null || candidates.isEmpty())
                throw new IllegalArgumentException("DamageConvertStatEffect.candidates 至少需要一个候选类型（RANDOM/CYCLE 模式）");
            for (String c : candidates)
                if (!VALID_TYPES.contains(c))
                    throw new IllegalArgumentException("DamageConvertStatEffect.candidates 每个元素必须是 GateTag 五伤害类型之一，收到: " + c);
        }
        this.fromTags = Set.copyOf(fromTags);
        this.to = to;
        this.mode = mode;
        this.hitsOnly = hitsOnly;
        this.taken = taken;
        this.toMode = toMode;
        this.candidates = List.copyOf(candidates);
        this.fromContext = fromContext == null ? GateExpr.ALWAYS : fromContext;
    }

    /** 仅命中生效门控。 */
    public boolean hitsOnly() {
        return hitsOnly;
    }

    /** 是否同时作用于承受伤害（维度B 进项转伤门控）。 */
    public boolean taken() {
        return taken;
    }

    public Set<String> fromTags() {
        return fromTags;
    }

    public String to() {
        return to;
    }

    public Mode mode() {
        return mode;
    }

    /** 目标类型解析模式（FIXED/RANDOM/CYCLE）。 */
    public ToMode toMode() {
        return toMode;
    }

    /** RANDOM/CYCLE 候选伤害类型列表（FIXED 时为空）。 */
    public List<String> candidates() {
        return candidates;
    }

    /** 命中上下文门控（GateExpr），缺省恒真 = 不门控。 */
    public GateExpr fromContext() {
        return fromContext;
    }

    /**
     * 解析目标伤害类型：FIXED 直接返回 {@link #to()}；RANDOM/CYCLE 经 {@link StatEngine#resolveRandomTo}
     * 按候选列表与条目来源键逐次解析（与承伤侧随机转换共用同一计数器）。
     */
    public String resolvedTo(LivingEntity living, String entryKey) {
        if (toMode == ToMode.FIXED) return to;
        return StatEngine.resolveRandomTo(living, entryKey, toMode, candidates);
    }

    /** 转换模式。 */
    public enum Mode implements StringRepresentable {
        REPLACE,
        GAIN,
        /** 归零（禁止造成）：按 ratio 把来源伤害类型从来源槽位直接扣除，不转入任何目标类型。 */
        NULLIFY;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 便捷构造：单来源 from→to 的 REPLACE 转换，ratio 为常量（0~1）。 */
    public static StatEntry entry(String from, String to, double ratio) {
        return entry(Set.of(from), to, Mode.REPLACE, ratio);
    }

    /** 便捷构造：单来源 from→to，指定 mode，ratio 为常量（0~1）。 */
    public static StatEntry entry(String from, String to, Mode mode, double ratio) {
        return entry(Set.of(from), to, mode, ratio);
    }

    /** 便捷构造：组合来源 fromTags→to 的 REPLACE 转换，ratio 为常量（0~1）。 */
    public static StatEntry entry(Set<String> fromTags, String to, double ratio) {
        return entry(fromTags, to, Mode.REPLACE, ratio);
    }

    /** REPLACE 便捷构造（含 taken 进项转伤门控）。 */
    public static StatEntry entry(Set<String> fromTags, String to, double ratio, boolean taken) {
        return entry(fromTags, to, Mode.REPLACE, ratio, false, GateExpr.ALWAYS, taken);
    }

    public static StatEntry entry(Set<String> fromTags, String to, Mode mode, double ratio) {
        return entry(fromTags, to, mode, ratio, false, GateExpr.ALWAYS);
    }

    /**
     * 全参数便捷构造（POE2 严格语义用）：组合来源 fromTags→to，指定 mode / 常量比例 / 是否仅命中 / 作用域标签。
     */
    public static StatEntry entry(Set<String> fromTags, String to, Mode mode, double ratio, boolean hitsOnly, GateExpr tags) {
        return entry(fromTags, to, mode, ratio, hitsOnly, tags, false);
    }

    /** 全参数便捷构造（POE2 严格语义用，含 {@code taken} 进项转伤门控）：组合来源 fromTags→to，指定 mode / 常量比例 / 是否仅命中 / 作用域标签 / 是否承受生效。 */
    public static StatEntry entry(Set<String> fromTags, String to, Mode mode, double ratio, boolean hitsOnly, GateExpr tags, boolean taken) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new DamageConvertStatEffect(fromTags, to, mode, hitsOnly, taken, ToMode.FIXED, List.of(), GateExpr.ALWAYS, tags));
    }

    /** 便捷构造：单来源 from→to，指定 mode，amount 默认 $value 模板。 */
    public static StatEntry entry(String from, String to, Mode mode) {
        return entry(Set.of(from), to, mode, false, GateExpr.ALWAYS);
    }

    /** 便捷构造：组合来源 fromTags→to，指定 mode，amount 默认 $value 模板。 */
    public static StatEntry entry(Set<String> fromTags, String to, Mode mode) {
        return entry(fromTags, to, mode, false, GateExpr.ALWAYS);
    }

    /** 全参数便捷构造（amount 默认 $value 模板）。 */
    public static StatEntry entry(Set<String> fromTags, String to, Mode mode, boolean hitsOnly, GateExpr tags) {
        return StatEntry.of(ExpressionCodec.VALUE, new DamageConvertStatEffect(fromTags, to, mode, hitsOnly, tags));
    }

    /**
     * 便捷构造：组合来源 fromTags，指定 {@link Mode}（含 {@link Mode#NULLIFY} 归零语义，无目标类型）。
     * NULLIFY 模式 {@code to} 留空（构造器对该模式豁免目标类型校验），由消费端按空目标 + NULLIFY 模式归零来源伤害。
     */
    public static StatEntry entry(Set<String> fromTags, Mode mode, double ratio, boolean hitsOnly, GateExpr tags) {
        return entry(fromTags, "", mode, ratio, hitsOnly, tags);
    }

    /** NULLIFY 便捷构造（含 taken 进项转伤门控）。 */
    public static StatEntry entry(Set<String> fromTags, Mode mode, double ratio, boolean hitsOnly, GateExpr tags, boolean taken) {
        return entry(fromTags, "", mode, ratio, hitsOnly, tags, taken);
    }

    /** 便捷构造：NULLIFY 归零（无目标类型），amount 默认 $value 模板（比例由词缀 vars 注入）。 */
    public static StatEntry entry(Set<String> fromTags, Mode mode, boolean hitsOnly, GateExpr tags) {
        return entry(fromTags, "", mode, hitsOnly, tags);
    }

    /**
     * 便捷构造：NULLIFY 归零（带条件门控）。条件全部非 0 时才对持有者生效（读者自判自身状态，机制完全可达）。
     * 用于「受 Archon 影响时归零物理 / 混沌伤害」等忠实门控。
     */
    public static StatEntry entry(Set<String> fromTags, Mode mode, boolean hitsOnly, GateExpr tags, List<ValueExpr> conditions) {
        return StatEntry.of(ExpressionCodec.VALUE, conditions, new DamageConvertStatEffect(fromTags, "", mode, hitsOnly, tags));
    }

    /** 随机/轮流类型便捷构造：目标类型由 {@code toMode} + {@code candidates} 解析（GAIN 共存语义）。 */
    public static StatEntry entry(Set<String> fromTags, ToMode toMode, List<String> candidates,
                                  double ratio, boolean hitsOnly, GateExpr tags) {
        return entry(fromTags, toMode, candidates, ratio, hitsOnly, tags, false);
    }

    /** 随机/轮流类型便捷构造（含 taken 进项转伤门控）。 */
    public static StatEntry entry(Set<String> fromTags, ToMode toMode, List<String> candidates,
                                  double ratio, boolean hitsOnly, GateExpr tags, boolean taken) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new DamageConvertStatEffect(fromTags, "", Mode.GAIN, hitsOnly, taken, toMode, candidates, GateExpr.ALWAYS, tags));
    }

    @Override
    public String type() {
        return "damage_convert";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.DAMAGE_CONVERT;
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
     * 本地化效果片段：硬编码 {@code "physical → fire 0.25"} → 模板
     * {@code stat_effect.damage_convert} / {@code stat_effect.damage_convert_gain}
     * + 来源/目标伤害类型值键 {@code stat.value.damage.<type>} + 比例数值（走 {@link StatNumberFormat}）。
     * RANDOM/CYCLE 模式目标经 {@code stat.value.to_mode.<mode>} 前缀 + 候选类型拼接。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        MutableComponent fromComp = Component.empty();
        List<String> from = List.copyOf(fromTags);
        for (int i = 0; i < from.size(); i++) {
            if (i > 0) fromComp.append("/");
            fromComp.append(StatValueKey.of("damage", from.get(i)));
        }
        Component toComp = resolveToComponent();
        Component amount = Component.literal(StatNumberFormat.format(entry.amount().getValue(), locale));
        String key = mode == Mode.GAIN ? "stat_effect.damage_convert_gain" : "stat_effect.damage_convert";
        return List.of(Component.translatable(key, fromComp, toComp, amount));
    }

    /** 目标伤害类型组件：FIXED → {@code stat.value.damage.<to>}；RANDOM/CYCLE → 前缀 + 候选拼接。 */
    private Component resolveToComponent() {
        if (toMode != ToMode.FIXED) {
            MutableComponent c = Component.translatable("stat.value.to_mode." + toMode.name().toLowerCase(Locale.ROOT));
            c.append("(");
            for (int i = 0; i < candidates.size(); i++) {
                if (i > 0) c.append("/");
                c.append(StatValueKey.of("damage", candidates.get(i)));
            }
            c.append(")");
            return c;
        }
        return StatValueKey.of("damage", to);
    }

    @Override
    public String toString() {
        String target = toMode == ToMode.FIXED ? to
                : toMode.name().toLowerCase(Locale.ROOT) + "(" + String.join("/", candidates) + ")";
        return "DamageConvertStatEffect[" + String.join("/", fromTags) + (mode == Mode.GAIN ? "+" : "→") + target
                + ", toMode=" + toMode + ", tags=" + tags() + "]";
    }
}
