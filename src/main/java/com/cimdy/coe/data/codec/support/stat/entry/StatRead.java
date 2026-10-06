package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.event.data.LoadEvent;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

/**
 * 通用 Stat 条目：原 INLINE 型 {@code Stat} 已移除，本类一并承载「内联定义」与「引用」两种形态。
 *
 * <ul>
 *   <li><b>ref 非空</b> = 引用形态：仅持 {@code ref}（+ 实例覆盖层 {@code vars}），运行时从
 *       {@link LoadEvent#STAT_ENTRY_MAP} 转发全部行为；内联字段忽略。</li>
 *   <li><b>ref 为空</b> = 内联定义形态：直接持有全部字段，各子字段自理 Read——
 *       数值经 {@link ExpressionCodec.Value}、选择器 / 效果 / 条件经各自 {@code ref} 解析，
 *       Stat 容器不再越界强制每段 READ。</li>
 * </ul>
 *
 * <p>JSON 形态：{@code {"entry":"read","ref":"coe:some_stat","vars":{...}}}（引用）或
 * {@code {"entry":"read","amount":...,"targets":...,"effect":...,"condition":"coe:some_cond"}}
 * （定义，无 ref）。选择器 / 效果 / 条件三段均为外置引用（{@link Identifier}），
 * 访问时经 {@link LoadEvent} 的三段加载表解析；解析缺失即抛错（不静默回落）。
 */
public record StatRead(
        @Nullable Identifier ref,
        Map<String, ExpressionCodec.Value> vars,
        // —— 内联定义字段（ref 空时使用）——
        ExpressionCodec.Value statValue,
        ValueExpr conditionsNode,
        Identifier targetsRef,
        Identifier effectRef,
        Map<String, ExpressionCodec.Value> params,
        GateExpr spillTo,
        GateExpr convertTo,
        @Nullable StatDuration duration,
        @Nullable StatDuration interval,
        @Nullable Identifier conditionRef
) implements StatEntry {

    /**
     * 规范构造：按 ref 是否存在分支归一化。
     * <ul>
     *   <li>定义形态（ref 空）：条件段归一化为非空节点；外溢 / 转换归一化为非空表达式；
     *       未声明选择器归并为 EMPTY 自指向引用。</li>
     *   <li>引用形态（ref 非空）：内联字段忽略（访问走 {@link #resolve()}）。</li>
     * </ul>
     */
    public StatRead {
        if (ref == null) {
            conditionsNode = conditionsNode == null ? ValueExprGroup.EMPTY : conditionsNode;
            spillTo = (spillTo == null || spillTo.isAlwaysTrue()) ? GateExpr.NEVER : spillTo;
            convertTo = (convertTo == null || convertTo.isAlwaysTrue()) ? GateExpr.NEVER : convertTo;
            if (!spillTo.isNever() && !convertTo.isNever()) {
                throw new IllegalArgumentException(
                        "Stat 同时声明 spill_to（并集 / COPY）与 convert_to（替代 / MOVE）——"
                                + "两者门控语义互斥，请只保留其一");
            }
            targetsRef = targetsRef == null ? StatEntry.internTargets(StatTargets.EMPTY) : targetsRef;
        }
    }

    // —— 引用形态便捷构造 ——

    public StatRead(Identifier ref) {
        this(ref, Map.of());
    }

    public StatRead(Identifier ref, Map<String, ExpressionCodec.Value> vars) {
        this(ref, vars, null, null, null, null, Map.of(), GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    // —— 内联定义便捷构造（端口原 Stat 构造器签名：选择器 / 效果为 ref） ——

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    Identifier targetsRef, Identifier effectRef) {
        this(null, Map.of(), statValue, conditionsNode, targetsRef, effectRef, Map.of(),
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    Identifier targetsRef, Identifier effectRef,
                    Map<String, ExpressionCodec.Value> params) {
        this(null, Map.of(), statValue, conditionsNode, targetsRef, effectRef, params,
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    Identifier targetsRef, Identifier effectRef,
                    Map<String, ExpressionCodec.Value> params, GateExpr spillTo) {
        this(null, Map.of(), statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    Identifier targetsRef, Identifier effectRef,
                    Map<String, ExpressionCodec.Value> params, GateExpr spillTo, GateExpr convertTo) {
        this(null, Map.of(), statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, convertTo, null, null, null);
    }

    // —— 内联形态（选择器 / 效果为对象，datagen 接线常用） ——

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    StatTargets targets, StatEffect effect) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions),
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), Map.of(),
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    StatTargets targets, StatEffect effect,
                    Map<String, ExpressionCodec.Value> params) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions),
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    StatTargets targets, StatEffect effect,
                    Map<String, ExpressionCodec.Value> params, GateExpr spillTo) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions),
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                spillTo, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    StatTargets targets, StatEffect effect,
                    Map<String, ExpressionCodec.Value> params, GateExpr spillTo, GateExpr convertTo) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions),
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                spillTo, convertTo, null, null, null);
    }

    // —— 对象形态（conditions 为单 ValueExpr 节点，选择器 / 效果为对象；datagen 接线常用）——
    // 端口原 Stat 的 ValueExpr 条件段重载（LivingModRoller 等走此签名）。

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    StatTargets targets, StatEffect effect) {
        this(null, Map.of(), statValue, conditionsNode,
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), Map.of(),
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    StatTargets targets, StatEffect effect, Map<String, ExpressionCodec.Value> params) {
        this(null, Map.of(), statValue, conditionsNode,
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    StatTargets targets, StatEffect effect, Map<String, ExpressionCodec.Value> params, GateExpr spillTo) {
        this(null, Map.of(), statValue, conditionsNode,
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                spillTo, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, ValueExpr conditionsNode,
                    StatTargets targets, StatEffect effect, Map<String, ExpressionCodec.Value> params,
                    GateExpr spillTo, GateExpr convertTo) {
        this(null, Map.of(), statValue, conditionsNode,
                StatEntry.internTargets(targets), StatEntry.internEffect(effect), params,
                spillTo, convertTo, null, null, null);
    }

    // —— 内联形态（选择器 / 效果为引用，conditions 为 List；端口原 Stat 的 List 条件段重载）——
    // 供 WiringDsl.stat / StatGen.stat 等以具名 targetsRef / effectRef 起手、条件给 List 的接线。

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    Identifier targetsRef, Identifier effectRef) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions), targetsRef, effectRef, Map.of(),
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    Identifier targetsRef, Identifier effectRef, Map<String, ExpressionCodec.Value> params) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions), targetsRef, effectRef, params,
                GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    Identifier targetsRef, Identifier effectRef, Map<String, ExpressionCodec.Value> params,
                    GateExpr spillTo, GateExpr convertTo) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions), targetsRef, effectRef, params,
                spillTo, convertTo, null, null, null);
    }

    // —— 内联形态（选择器为引用、效果为对象）——
    public StatRead(ExpressionCodec.Value statValue, List<ValueExpr> conditions,
                    Identifier targetsRef, StatEffect effect) {
        this(null, Map.of(), statValue, ValueExpr.of(conditions), targetsRef,
                StatEntry.internEffect(effect), Map.of(), GateExpr.NEVER, GateExpr.NEVER, null, null, null);
    }

    // —— 访问器 ——

    @Override
    public ExpressionCodec.Value amount() {
        return ref != null ? resolve().amount() : statValue;
    }

    @Override
    public ValueExpr conditionsNode() {
        return ref != null ? ((StatRead) resolve()).conditionsNode() : conditionsNode;
    }

    @Override
    public List<ValueExpr> conditions() {
        return conditionsNode().asList();
    }

    @Override
    public StatTargets targets() {
        if (ref != null) return resolve().targets();
        StatTargets t = targetsRef == null ? null : LoadEvent.STAT_TARGET_MAP.get(targetsRef);
        return t != null ? t : StatTargets.EMPTY;
    }

    @Override
    public StatEffect effect() {
        if (ref != null) return resolve().effect();
        StatEffect e = effectRef == null ? null : LoadEvent.STAT_EFFECT_MAP.get(effectRef);
        if (e == null) {
            throw new IllegalStateException("Stat 引用了未注册的 effect: " + effectRef);
        }
        return e;
    }

    @Override
    public @Nullable StatCondition condition() {
        if (ref != null) return resolve().condition();
        if (conditionRef == null) return null;
        StatCondition c = LoadEvent.STAT_CONDITION_MAP.get(conditionRef);
        if (c == null) {
            throw new IllegalStateException("Stat 引用了未注册的 condition: " + conditionRef);
        }
        return c;
    }

    @Override
    public Map<String, ExpressionCodec.Value> params() {
        return ref != null ? resolve().params() : params;
    }

    @Override
    public GateExpr spillTo() {
        return ref != null ? resolve().spillTo() : spillTo;
    }

    @Override
    public GateExpr convertTo() {
        return ref != null ? resolve().convertTo() : convertTo;
    }

    @Override
    public @Nullable StatDuration duration() {
        return ref != null ? resolve().duration() : duration;
    }

    @Override
    public @Nullable StatDuration interval() {
        return ref != null ? resolve().interval() : interval;
    }

    @Override
    public Map<String, ExpressionCodec.Value> effectiveParams() {
        if (ref == null || vars.isEmpty()) return params();
        Map<String, ExpressionCodec.Value> merged = new java.util.HashMap<>(params());
        merged.putAll(vars);
        return merged;
    }

    @Override
    public ExpressionContext bindParams(ExpressionContext ec) {
        return ec.withVariables(effectiveParams());
    }

    @Override
    public double evaluateAmount(ExpressionContext ec) {
        return amount().getValue(bindParams(ec));
    }

    /** 从数据生成缓存解析本引用指向的 StatEntry（缺失则报错）。 */
    public StatEntry resolve() {
        StatEntry e = LoadEvent.STAT_ENTRY_MAP.get(this.ref);
        if (e == null) {
            throw new IllegalStateException("StatRead 引用了未注册的 StatEntry: " + this.ref);
        }
        return e;
    }

    /** 内联形态副本：引用形态解析后构造 ref-less 副本（保留参数槽）；定义形态原样返回。 */
    @Override
    public StatEntry toInline() {
        if (ref == null) return this;
        StatRead base = (StatRead) resolve();
        return new StatRead(null, effectiveParams(),
                base.statValue, base.conditionsNode(), base.targetsRef(), base.effectRef(),
                base.params(), base.spillTo(), base.convertTo(),
                base.duration(), base.interval(), base.conditionRef());
    }

    @Override
    public StatRead withDuration(@Nullable StatDuration newDuration) {
        if (ref != null) {
            return (StatRead) toInline().withDuration(newDuration);
        }
        return new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, convertTo, newDuration, interval, conditionRef);
    }

    @Override
    public StatEntry withInterval(@Nullable StatDuration newInterval) {
        return ref != null ? toInline().withInterval(newInterval)
                : new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, convertTo, duration, newInterval, conditionRef);
    }

    @Override
    public StatEntry withSpillTo(GateExpr spill) {
        return ref != null ? toInline().withSpillTo(spill)
                : new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, params,
                spill, convertTo, duration, interval, conditionRef);
    }

    @Override
    public StatEntry withConvertTo(GateExpr to) {
        return ref != null ? toInline().withConvertTo(to)
                : new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, to, duration, interval, conditionRef);
    }

    /** 替换条件引用后的副本（其余段原样保留）；covariant 覆写返回 StatRead。 */
    @Override
    public StatRead withConditionRef(@Nullable Identifier condition) {
        if (ref != null) {
            return (StatRead) toInline().withConditionRef(condition);
        }
        return new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, params,
                spillTo, convertTo, duration, interval, condition);
    }

    /** 内联条件对象 → 具名引用后的 StatRead 副本（covariant 覆写，便于 DSL 链式返回 StatRead）。 */
    @Override
    public StatRead withCondition(@Nullable StatCondition newCondition) {
        return withConditionRef(StatEntry.internCondition(newCondition));
    }

    @Override
    public StatEntry withParams(Map<String, ExpressionCodec.Value> newParams) {
        if (ref != null) {
            if (newParams.isEmpty()) return this;
            Map<String, ExpressionCodec.Value> merged = new java.util.HashMap<>(vars);
            merged.putAll(newParams);
            return new StatRead(ref, merged, null, null, null, null, Map.of(),
                    GateExpr.NEVER, GateExpr.NEVER, null, null, null);
        }
        return new StatRead(ref, vars, statValue, conditionsNode, targetsRef, effectRef, newParams,
                spillTo, convertTo, duration, interval, conditionRef);
    }

    @Override
    public StatEntry withAmount(ExpressionCodec.Value newAmount) {
        return ref != null ? toInline().withAmount(newAmount)
                : new StatRead(ref, vars, newAmount, conditionsNode, targetsRef, effectRef, params,
                spillTo, convertTo, duration, interval, conditionRef);
    }

    @Override
    public StatEntry withEffect(StatEffect newEffect) {
        return ref != null ? toInline().withEffect(newEffect)
                : new StatRead(ref, vars, statValue, conditionsNode, targetsRef,
                StatEntry.internEffect(newEffect), params, spillTo, convertTo,
                duration, interval, conditionRef);
    }

    /**
     * 替换三段选择器后的副本；与 {@link #withAmount} / {@link #withEffect} 同范式，
     * 必须直接透传 {@code conditionRef}——若改走 {@code withCondition(s.condition())}，
     * 会把具名 condition 洗成内存态 anon id，落盘后运行期解析必失败。
     */
    @Override
    public StatEntry withTargets(StatTargets newTargets) {
        return ref != null ? toInline().withTargets(newTargets)
                : new StatRead(ref, vars, statValue, conditionsNode,
                StatEntry.internTargets(newTargets), effectRef, params,
                spillTo, convertTo, duration, interval, conditionRef);
    }

    /** 内联实现对应的完整 Codec（供 {@link StatEntries} 注册表登记）。 */
    public static final MapCodec<StatRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.optionalFieldOf("ref").forGetter(s -> java.util.Optional.ofNullable(s.ref)),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("vars", Map.of()).forGetter(StatRead::vars),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("amount")
                    .forGetter(s -> java.util.Optional.ofNullable(s.statValue)),
            ValueExpr.CODEC.optionalFieldOf("conditions")
                    .forGetter(s -> java.util.Optional.ofNullable(s.conditionsNode)),
            Identifier.CODEC.optionalFieldOf("targets").forGetter(s -> java.util.Optional.ofNullable(s.targetsRef)),
            Identifier.CODEC.optionalFieldOf("effect").forGetter(s -> java.util.Optional.ofNullable(s.effectRef)),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("params", Map.of()).forGetter(StatRead::params),
            GateExpr.CODEC.optionalFieldOf("spill_to", GateExpr.NEVER).forGetter(StatRead::spillTo),
            GateExpr.CODEC.optionalFieldOf("convert_to", GateExpr.NEVER).forGetter(StatRead::convertTo),
            StatDuration.CODEC.optionalFieldOf("duration").forGetter(s -> java.util.Optional.ofNullable(s.duration)),
            StatDuration.CODEC.optionalFieldOf("interval").forGetter(s -> java.util.Optional.ofNullable(s.interval)),
            Identifier.CODEC.optionalFieldOf("condition").forGetter(s -> java.util.Optional.ofNullable(s.conditionRef))
    ).apply(i, (r, v, sv, cond, tgt, eff, p, spill, convert, dur, inter, cref) ->
            new StatRead(r.orElse(null), v, sv.orElse(null), cond.orElse(null),
                    tgt.orElse(null), eff.orElse(null), p, spill, convert,
                    dur.orElse(null), inter.orElse(null), cref.orElse(null))));

    public static final Codec<StatRead> CODEC = MAP_CODEC.codec();

    @Override
    public StatEntry.Type<StatRead> getType() {
        return StatEntries.READ;
    }
}
