package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.condition.StatCondition;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组合条目：一个词缀 id → N 条 StatEntry 同时生效，共享同一 amount 与同一组 conditions。
 * 语义 = 「这一条 = 这些条一起生效」。用于 PoE 的 hybrid 词缀
 * （Armour and Evasion / Armour and Energy Shield / Evasion and Energy Shield / all Attributes）。
 *
 * <p>JSON 形态（§5.3.2）：
 * <pre>{@code
 * { "entry": "group",
 *   "params": { "value": 0.15 },                       // 组级参数默认值，透传给 members
 *   "members": [
 *     { "entry": "inline", "amount": "$value", "attribute": "minecraft:armor", ... },
 *     { "entry": "inline", "amount": "$value", "attribute": "coe:inc_evasion", ... }
 *   ] }
 * }</pre>
 *
 * <p><b>params 透传（阶段 2 统一参数表）</b>：组 params 为底层默认，摊平时对每个 member 做
 * {@code merge(groupParams, memberParams)} 产出副本（member 同键覆盖组级）——hybrid 共享
 * 「同一个 X」的语义由此天然成立：组 params {@code {value: X}} 摊平后各 member 的 {@code $value}
 * 全部绑到 X。
 *
 * <p><b>下游无感</b>：{@link StatStore} 建索引时调用 {@link #flatten()} 摊平，下游只见摊平结果，
 * {@code StatEngine} 不需要认识 group。
 */
public record StatGroup(
        List<StatEntry> members,
        Map<String, ExpressionCodec.Value> params
) implements StatEntry {

    public StatGroup(List<StatEntry> members) {
        this(members, Map.of());
    }

    public StatGroup {
        // B-2.3/3-2：空 group 无任何可生效成员，属数据错误——构造期显式拒绝（而非 Either.left(null) 静默）
        if (members.isEmpty()) {
            throw new IllegalArgumentException("StatGroup must not be empty (no members to flatten)");
        }
        members = List.copyOf(members);
    }

    public static final MapCodec<StatGroup> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            // 递归引用：members 内可再嵌 inline / ref / assemble（不建议嵌 group，见 flatten 校验）
            Codec.lazyInitialized(() -> StatEntry.CODEC).listOf()
                    .fieldOf("members").forGetter(StatGroup::members),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("params", Map.of()).forGetter(StatGroup::params)
    ).apply(i, StatGroup::new));

    public static final Codec<StatGroup> CODEC = MAP_CODEC.codec();

    @Override
    public StatEntry.Type<StatGroup> getType() {
        return StatEntries.GROUP;
    }

    // ---------------------------------------------------------------- 四段访问（转发第一个 member）

    /** group 自身不定义数值——数值由 members 各自承载；group 不经 {@link StatEngine} 直接求值（先被 flatten）。 */
    @Override
    public ExpressionCodec.Value amount() {
        return members.isEmpty() ? ExpressionCodec.of(0) : members.get(0).amount();
    }

    @Override
    public List<ValueExpr> conditions() {
        return members.isEmpty() ? List.of() : members.get(0).conditions();
    }

    @Override
    public StatCondition condition() {
        return members.isEmpty() ? null : members.get(0).condition();
    }

    @Override
    public StatTargets targets() {
        return members.isEmpty() ? StatTargets.EMPTY : members.get(0).targets();
    }

    @Override
    public StatEffect effect() {
        return members.isEmpty() ? null : members.get(0).effect();
    }

    /** 外溢作用域：转发首成员（同 amount / conditions / targets / effect 的既有转发约定）。
     *  group 摊平后各成员各自携带自身声明，故本方法只服务未摊平路径的读取。 */
    @Override
    public GateExpr spillTo() {
        return members.isEmpty() ? GateExpr.NEVER : members.get(0).spillTo();
    }

    /** 转换作用域：转发首成员（同 amount / conditions / targets / effect 的既有转发约定）。 */
    @Override
    public GateExpr convertTo() {
        return members.isEmpty() ? GateExpr.NEVER : members.get(0).convertTo();
    }

    @Override
    public Map<String, ExpressionCodec.Value> params() {
        return params;
    }

    /**
     * 摊平：group 内不允许再嵌 group（一层足够表达，嵌套只会让索引与调试复杂化）。
     * 每个 member 应用组级 params 透传（member 同键覆盖组级），产出带参副本。
     */
    public List<StatEntry> flatten() {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            if (m instanceof StatGroup) throw new IllegalStateException("stat group must not nest group");
            out.add(applyGroupParams(m));
        }
        return out;
    }

    /**
     * 组合条目重定向：对<b>每个成员</b>分别换目标后重组为 group。
     *
     * <p>必须覆写 {@link StatEntry#withTargets} 的默认实现——默认实现会降级为单条内联 {@link Stat}
     * （取首成员的 amount/effect），从而把 hybrid 词缀（Armour and Evasion 等）的其余成员<b>静默吞掉</b>。
     * 目标重定向只应改「收件人」，不得改变条目结构，故逐成员改写后仍返回 group（组级 params 原样保留）。
     */
    @Override
    public StatGroup withTargets(StatTargets newTargets) {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            out.add(m.withTargets(newTargets));
        }
        return new StatGroup(out, params);
    }

    /** 组合条目实体获取条件：逐成员替换后保持 group。 */
    @Override
    public StatGroup withCondition(StatCondition condition) {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            out.add(m.withCondition(condition));
        }
        return new StatGroup(out, params);
    }

    /** 组合条目实体获取条件（具名 ref）：逐成员替换后保持 group 形态。 */
    @Override
    public StatGroup withConditionRef(net.minecraft.resources.Identifier condition) {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            out.add(m.withConditionRef(condition));
        }
        return new StatGroup(out, params);
    }

    /** 组合条目重定向：逐成员替换外溢作用域后重组（成员各自的外溢声明不被合并）。 */
    @Override
    public StatGroup withSpillTo(GateExpr spill) {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            out.add(m.withSpillTo(spill));
        }
        return new StatGroup(out, params);
    }

    /** 组合条目重定向：逐成员替换转换作用域后重组（成员各自的转换声明不被合并）。 */
    @Override
    public StatGroup withConvertTo(GateExpr to) {
        List<StatEntry> out = new ArrayList<>(members.size());
        for (StatEntry m : members) {
            out.add(m.withConvertTo(to));
        }
        return new StatGroup(out, params);
    }

    /** 组级 params 透传：member 无 params 或参数不足时，补上组级默认值（member 同键覆盖）。 */
    private StatEntry applyGroupParams(StatEntry m) {
        if (params.isEmpty()) return m;
        Map<String, ExpressionCodec.Value> effective = new HashMap<>(params);
        effective.putAll(m.params());   // member 同键覆盖组级
        return m.withParams(effective);
    }

    /** 有效参数：组级 params ⊕ 首个 member 的 params（member 同键覆盖）。 */
    @Override
    public Map<String, ExpressionCodec.Value> effectiveParams() {
        Map<String, ExpressionCodec.Value> effective = new HashMap<>(params);
        if (!members.isEmpty()) effective.putAll(members.get(0).params());
        return effective;
    }

    /** bindParams：透传合并后的参数（组级 + 首个 member 的参数）。防御性实现——
     *  StatStore 构造时 group 已被 flatten（toKeyedMap），StatEngine 不对未 flatten 的 group 求值。 */
    @Override
    public ExpressionContext bindParams(ExpressionContext ec) {
        return ec.withVariables(effectiveParams());
    }
}
