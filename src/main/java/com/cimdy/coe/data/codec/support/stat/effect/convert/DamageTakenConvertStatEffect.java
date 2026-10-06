package com.cimdy.coe.data.codec.support.stat.effect.convert;
import com.cimdy.coe.data.codec.support.stat.effect.base.AbstractStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 承伤转化效果（damage_taken_convert）：在抗性 / 护甲计算之前回调，使某种「减免手段」按给定比例
 * 作用于某些「伤害维度」。
 *
 * <p>与 {@link DamageConvertStatEffect}（伤害类型重路由，作用于伤害源侧）不同，本效果作用于
 * <b>承伤方</b>，扩展减免手段的覆盖范围（例如「护甲也按 X% 作用于火焰伤害」「火抗也按 Y% 作用于物理伤害」）。
 *
 * <p><b>维度全部交给 tag 组合表达</b>：{@code reduction} 为减免手段标签（armor / 具名元素 / resistance）；
 * {@code tags} 为伤害维度标签布尔表达式（决定作用于哪些伤害）；{@code mode} 为 EXTRA（二次减免）/ REROUTE（重路由）。
 *
 * <h2>减免手段解析（合并原 RandomTakenConvertStatEffect）</h2>
 * 原 {@code RandomTakenConvertStatEffect}（「随机元素承伤转换」）与本类仅差<b>减免手段是否固定</b>，
 * 现由 {@link DamageConvertStatEffect.ToMode} 统一承载：
 * <ul>
 *   <li>{@link DamageConvertStatEffect.ToMode#FIXED}（默认）：减免手段即 {@link #reduction()} 常量；</li>
 *   <li>{@link DamageConvertStatEffect.ToMode#RANDOM} / {@link DamageConvertStatEffect.ToMode#CYCLE}：减免手段按模式从
 *       {@link #candidates()} 候选元素解析（{@link #resolvedReduction} 委托 {@link StatEngine#resolveRandomTo}），输出恒为 REROUTE 语义。</li>
 * </ul>
 */
public final class DamageTakenConvertStatEffect extends AbstractStatEffect {

    /** 作用方式：EXTRA=二次减免叠加（REDUCTION 之后）；REROUTE=替代重路由（REDUCTION 之前剥离）。 */
    public enum Mode implements net.minecraft.util.StringRepresentable {
        EXTRA("extra"),
        REROUTE("reroute");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String id;

        Mode(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    /** 已知异常维度标签（取自 {@link GateTag}，用于把异常修饰并入槽位伤害上下文）。 */
    public static final Set<String> AILMENT_TAGS = Set.of(GateTag.BLEED, GateTag.IGNITE, GateTag.POISON);

    /** RANDOM/CYCLE 候选元素（nativeSlot 依赖具名形式；随机承伤转换无物理/混沌目标语义）。 */
    public static final Set<String> VALID_CANDIDATES = Set.of(
            GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.CHAOS);

    private final String reduction;
    private final Mode mode;
    /** 减免手段解析模式：FIXED 用 {@link #reduction()}；RANDOM/CYCLE 用 {@link #candidates()} 解析。 */
    private final DamageConvertStatEffect.ToMode toMode;
    /** RANDOM/CYCLE 候选元素标签（FIXED 时为空）。 */
    private final List<String> candidates;

    public DamageTakenConvertStatEffect(String reduction, Mode mode, GateExpr tags) {
        this(reduction, mode, DamageConvertStatEffect.ToMode.FIXED, List.of(), tags);
    }

    /** 无事件标签门控（{@link GateExpr#ALWAYS}）便捷构造。 */
    public DamageTakenConvertStatEffect(String reduction, Mode mode, DamageConvertStatEffect.ToMode toMode,
                                         List<String> candidates) {
        this(reduction, mode, toMode, candidates, GateExpr.ALWAYS);
    }

    public DamageTakenConvertStatEffect(String reduction, Mode mode, DamageConvertStatEffect.ToMode toMode,
                                         List<String> candidates, GateExpr tags) {
        super(tags);
        if (toMode == DamageConvertStatEffect.ToMode.FIXED) {
            if (!isValidReduction(reduction))
                throw new IllegalArgumentException("DamageTakenConvertStatEffect.reduction 必须是 armor/具名元素/resistance 之一，收到: " + reduction);
        } else {
            if (candidates == null || candidates.isEmpty())
                throw new IllegalArgumentException("DamageTakenConvertStatEffect.candidates 至少需要一个候选元素（RANDOM/CYCLE 模式）");
            for (String c : candidates)
                if (!VALID_CANDIDATES.contains(c))
                    throw new IllegalArgumentException("DamageTakenConvertStatEffect.candidates 每个元素必须是具名元素(fire/cold/lightning/chaos)，收到: " + c);
        }
        this.reduction = reduction;
        this.mode = mode;
        this.toMode = toMode;
        this.candidates = List.copyOf(candidates);
    }

    private static boolean isValidReduction(String reduction) {
        if (GateTag.ARMOR.equals(reduction)) return true;
        if (GateTag.RESISTANCE.equals(reduction)) return true;
        return elementSlot(reduction) >= 0;
    }

    /** 便捷构造：常量转换比例（默认 EXTRA 模式）。 */
    public static StatRead entry(String reduction, double ratio, Set<String> tags) {
        return entry(reduction, Mode.EXTRA, ratio, tags);
    }

    /** 便捷构造：常量转换比例 + 明确作用方式。 */
    public static StatRead entry(String reduction, Mode mode, double ratio, Set<String> tags) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new DamageTakenConvertStatEffect(reduction, mode, GateExpr.of(tags)));
    }

    /** 便捷构造：amount 默认 $value 模板（比例由词缀 vars 注入）。 */
    public static StatRead entry(String reduction, Mode mode, Set<String> tags) {
        return StatEntry.of(ExpressionCodec.VALUE,
                new DamageTakenConvertStatEffect(reduction, mode, GateExpr.of(tags)));
    }

    /** 便捷构造：常量转换比例 + 内联条件。 */
    public static StatRead entry(String reduction, Mode mode, double ratio, Set<String> tags,
                                  List<ValueExpr> conditions) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)), conditions,
                new DamageTakenConvertStatEffect(reduction, mode, GateExpr.of(tags)));
    }

    /** 随机/轮流元素承伤转换便捷构造：减免手段由 {@code toMode} + {@code candidates} 解析（REROUTE 语义，作用于全部伤害维度）。 */
    public static StatRead entry(DamageConvertStatEffect.ToMode toMode, List<String> candidates, double ratio) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(ratio)),
                new DamageTakenConvertStatEffect("", Mode.REROUTE, toMode, candidates, GateExpr.ALWAYS));
    }

    public String reduction() {
        return reduction;
    }

    public Mode mode() {
        return mode;
    }

    /** 减免手段解析模式（FIXED/RANDOM/CYCLE）。 */
    public DamageConvertStatEffect.ToMode toMode() {
        return toMode;
    }

    /** RANDOM/CYCLE 候选元素标签列表（FIXED 时为空）。 */
    public List<String> candidates() {
        return candidates;
    }

    /**
     * 解析减免手段：FIXED 直接返回 {@link #reduction()}；RANDOM/CYCLE 经 {@link StatEngine#resolveRandomTo}
     * 按候选元素列表与条目来源键逐次解析。
     */
    public String resolvedReduction(LivingEntity living, String entryKey) {
        if (toMode == DamageConvertStatEffect.ToMode.FIXED) return reduction;
        return StatEngine.resolveRandomTo(living, entryKey, toMode, candidates);
    }

    /**
     * 减免手段原生槽位（REROUTE 时把剥离的伤害份额移入此槽，由该手段原生减免接管）。
     */
    public static int nativeSlot(String reduction, Set<String> tags) {
        if (GateTag.ARMOR.equals(reduction)) return 0;
        int named = elementSlot(reduction);
        if (named >= 0) return named;
        if (GateTag.RESISTANCE.equals(reduction)) {
            for (String t : tags) {
                int slot = elementSlot(t);
                return slot >= 0 ? slot : 0;
            }
        }
        return 0;
    }

    /** 元素标签 → 伤害槽位；非元素标签返回 -1（便于与「物理槽 0」区分）。 */
    private static int elementSlot(String tag) {
        return switch (tag) {
            case GateTag.FIRE -> 2;
            case GateTag.COLD -> 3;
            case GateTag.LIGHTNING -> 4;
            case GateTag.CHAOS -> 1;
            default -> -1;
        };
    }

    /**
     * 按减免手段 + <b>命中槽位的标签上下文</b>，对 {@code current} 套一次该手段的原生减免公式。
     */
    public static float applyReduction(String reduction, Set<String> tags, LivingEntity living, AdditionData data,
                                       float current, @Nullable LivingEntity against) {
        if (GateTag.ARMOR.equals(reduction)) return DamageTypeConvert.applyArmorReduction(data, current);
        if (elementSlot(reduction) >= 0) return DamageTypeConvert.applyReduction(living, reduction, 0f, current, against);
        if (GateTag.RESISTANCE.equals(reduction)) {
            for (String t : tags) {
                return elementSlot(t) >= 0
                        ? DamageTypeConvert.applyReduction(living, t, 0f, current, against)
                        : current;
            }
        }
        return current;
    }

    public static final MapCodec<DamageTakenConvertStatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("reduction").forGetter(DamageTakenConvertStatEffect::reduction),
            Mode.CODEC.fieldOf("mode").forGetter(DamageTakenConvertStatEffect::mode),
            DamageConvertStatEffect.ToMode.CODEC.optionalFieldOf("to_mode", DamageConvertStatEffect.ToMode.FIXED)
                    .forGetter(DamageTakenConvertStatEffect::toMode),
            Codec.STRING.listOf().optionalFieldOf("candidates", List.of()).forGetter(DamageTakenConvertStatEffect::candidates),
            AbstractStatEffect.TAGS_MAP_CODEC.forGetter(AbstractStatEffect::tags)
    ).apply(i, (reduction, mode, toMode, candidates, tags) ->
            new DamageTakenConvertStatEffect(reduction, mode, toMode, candidates, tags)));

    @Override
    public String type() {
        return "damage_taken_convert";
    }

    @Override
    public boolean isTrigger() {
        return false;
    }

    @Override
    public EffectKind indexKind() {
        return EffectKind.DAMAGE_TAKEN_CONVERT;
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
     * 本地化效果片段：硬编码 {@code "25% reroutes armor for all"} → 模板
     * {@code stat_effect.damage_taken_convert_reroute} / {@code ..._extra}
     * + 百分比数值 + 减免手段值键 {@code stat.value.reduction.<id>}（RANDOM/CYCLE 经 {@code stat.value.to_mode.<mode>} 前缀）
     * + 作用维度（{@code stat.value.scope.all} 或 {@code stat.value.damage.<tag>} 回落 humanize）。
     */
    @Override
    public List<Component> describeFragment(StatEntry entry, Locale locale) {
        Component pct = Component.literal(
                StatNumberFormat.format((int) (entry.amount().getValue() * 100), locale) + "%");
        Component meansComp = resolveMeansComponent();
        Component scopeComp = resolveScopeComponent();
        String key = mode == Mode.REROUTE
                ? "stat_effect.damage_taken_convert_reroute"
                : "stat_effect.damage_taken_convert_extra";
        return List.of(Component.translatable(key, pct, meansComp, scopeComp));
    }

    /** 减免手段组件：FIXED → {@code stat.value.reduction.<id>}；RANDOM/CYCLE → 前缀 + 候选拼接。 */
    private Component resolveMeansComponent() {
        if (toMode != DamageConvertStatEffect.ToMode.FIXED) {
            MutableComponent c = Component.translatable(
                    "stat.value.to_mode." + toMode.name().toLowerCase(Locale.ROOT));
            c.append("(");
            for (int i = 0; i < candidates.size(); i++) {
                if (i > 0) c.append("/");
                c.append(StatValueKey.of("reduction", candidates.get(i)));
            }
            c.append(")");
            return c;
        }
        return StatValueKey.of("reduction", reduction);
    }

    /** 作用维度组件：恒真标签 → {@code stat.value.scope.all}；否则按标签拼接（伤害类型回落 humanize）。 */
    private Component resolveScopeComponent() {
        MutableComponent c = Component.empty();
        if (tags().isAlwaysTrue()) {
            return StatValueKey.of("scope", "all");
        }
        boolean first = true;
        for (String it : tags().leaves()) {
            if (!first) c.append("/");
            first = false;
            c.append(StatValueKey.of("damage", it));
        }
        return c;
    }

    @Override
    public String toString() {
        return "DamageTakenConvertStatEffect[" + reduction + "," + mode + ", toMode=" + toMode + ", tags=" + tags() + "]";
    }
}
