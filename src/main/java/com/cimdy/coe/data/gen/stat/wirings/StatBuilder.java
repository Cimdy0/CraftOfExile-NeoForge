package com.cimdy.coe.data.gen.stat.wirings;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.StatTargetsInline;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.gen.stat.StatConditionGen;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.gen.stat.StatTargetGen;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 接线建造者：以 effect 起手，按需叠加 target / condition / params，最后 {@link #build()}。
 *
 * <p><b>不可变</b>——每次叠加返回<b>新实例</b>，故一个基础接线可安全派生出多个变体，
 * 这正是「同 effect、同属性、同作用域递增」与「同 target、同条件递增」两种语义分类的
 * 表达方式：
 * <pre>{@code
 * var dmg = StatBuilder.effect(EFF_DAMAGE).self();     // 基础：自身增伤
 * map.put(StatIds.INC_DAMAGE,              dmg.build());
 * map.put(StatIds.INC_DAMAGE_WHILE_IGNITED, dmg.when(COND_IGNITE_STATUS).build());
 * }</pre>
 *
 * <p><b>内联语义（内容寻址已移除）</b>：本类持有的是<b>真实部件对象</b>
 * （{@link StatEffect} / {@link StatTargets} / {@code List<ValueExpr>}），而非路径引用。
 * 以 {@code Identifier} 起手的重载只在<b>边界</b>把 {@code Stat*Ids} 具名常量一次性解成对象
 * （{@link StatEffectGen#effect} / {@link StatTargetGen#targets} / {@link StatConditionGen#conditions}），
 * 之后全程按对象流转——身份可读由其具名常量保证，不再有任何内容哈希参与。
 *
 * <p><b>反向构建</b>：{@link #from(Stat)} 直接取出既有 Stat 的各维度对象，可继续叠加后重建，
 * 用于「在既有接线基础上改条件」。
 */
public final class StatBuilder {

    private final StatEffect effect;
    private final StatTargets target;
    private final List<ValueExpr> conds;
    private final Map<String, ExpressionCodec.Value> params;
    private final ExpressionCodec.Value amount;
    /** 数值阶段「被取值实体」覆写（null = 跟随既有 target 内容）。 */
    private final TargetSelector valueTarget;
    /** 条件阶段「被判定实体」覆写（null = 跟随既有 target 内容）。 */
    private final TargetSelector conditionTarget;
    /** 外溢作用域（null = 不外溢）：本条目数值额外计入的作用域。 */
    private final GateExpr spillTo;
    /** 转换作用域（null = 不转换）：本条目数值改计入（取代）的作用域。 */
    private final GateExpr convertTo;
    /** 时长段（条目第五部分，null = 不计时 / 不冷却 / 不周期）。 */
    private final StatDuration duration;

    private StatBuilder(StatEffect effect, StatTargets target, List<ValueExpr> conds,
                        Map<String, ExpressionCodec.Value> params, ExpressionCodec.Value amount) {
        this(effect, target, conds, params, amount, null, null, null, null);
    }

    private StatBuilder(StatEffect effect, StatTargets target, List<ValueExpr> conds,
                        Map<String, ExpressionCodec.Value> params, ExpressionCodec.Value amount,
                        TargetSelector valueTarget, TargetSelector conditionTarget) {
        this(effect, target, conds, params, amount, valueTarget, conditionTarget, null, null);
    }

    private StatBuilder(StatEffect effect, StatTargets target, List<ValueExpr> conds,
                        Map<String, ExpressionCodec.Value> params, ExpressionCodec.Value amount,
                        TargetSelector valueTarget, TargetSelector conditionTarget, GateExpr spillTo) {
        this(effect, target, conds, params, amount, valueTarget, conditionTarget, spillTo, null);
    }

    private StatBuilder(StatEffect effect, StatTargets target, List<ValueExpr> conds,
                        Map<String, ExpressionCodec.Value> params, ExpressionCodec.Value amount,
                        TargetSelector valueTarget, TargetSelector conditionTarget,
                        GateExpr spillTo, GateExpr convertTo) {
        this(effect, target, conds, params, amount, valueTarget, conditionTarget,
                spillTo, convertTo, null);
    }

    /** 全参构造（含时长段——条目第五部分）。 */
    private StatBuilder(StatEffect effect, StatTargets target, List<ValueExpr> conds,
                        Map<String, ExpressionCodec.Value> params, ExpressionCodec.Value amount,
                        TargetSelector valueTarget, TargetSelector conditionTarget,
                        GateExpr spillTo, GateExpr convertTo, StatDuration duration) {
        this.effect = effect;
        this.target = target;
        this.conds = conds;
        this.params = params;
        this.amount = amount;
        this.valueTarget = valueTarget;
        this.conditionTarget = conditionTarget;
        this.spillTo = spillTo;
        this.convertTo = convertTo;
        this.duration = duration;
    }

    // ===================== 起手：以 effect 构建 =====================

    /** 以具名 effect id 起手（target 缺省为 EMPTY）。 */
    public static StatBuilder effect(Identifier eff) {
        return effect(StatEffectGen.effect(eff));
    }

    /** 以真实 effect 对象起手（target 缺省为 EMPTY）。 */
    public static StatBuilder effect(StatEffect eff) {
        return new StatBuilder(eff, null, List.of(), new LinkedHashMap<>(), ExpressionCodec.VALUE);
    }

    /** 以具名 effect id + target id 起手。 */
    public static StatBuilder effect(Identifier eff, Identifier target) {
        return new StatBuilder(StatEffectGen.effect(eff), StatTargetGen.targets(target),
                List.of(), new LinkedHashMap<>(), ExpressionCodec.VALUE);
    }

    /** 反向构建：直接取用既有 StatEntry 的各维度对象，供继续叠加。 */
    public static StatBuilder from(StatEntry s) {
        return new StatBuilder(s.effect(), s.targets(), s.conditions(),
                new LinkedHashMap<>(s.params()), s.amount(), null, null,
                s.spillTo(), s.convertTo(), s.duration());
    }

    // ===================== 维度叠加（均返回新实例）=====================

    public StatBuilder target(StatTargets t) {
        return new StatBuilder(effect, t, conds, params, amount, valueTarget, conditionTarget,
                spillTo, convertTo, duration);
    }

    public StatBuilder target(Identifier t) {
        return target(StatTargetGen.targets(t));
    }

    /** 自身生效（TGT_SELF_ONLY）——占全部接线约 84%。 */
    public StatBuilder self() {
        return target(StatTargetIds.TGT_SELF_ONLY);
    }

    /** 对目标生效（TGT_COND_ON_TARGET）：条件在目标侧求值。 */
    public StatBuilder onTarget() {
        return target(StatTargetIds.TGT_COND_ON_TARGET);
    }

    /**
     * 数值阶段「被取值实体」覆写：{@code amount} 表达式里的 {@code attribute:...} 等按该实体求值。
     *
     * <p>缺省时数值按持有者求值——对「随从按自身最大生命获得额外 ES」这类须按<b>被作用实体</b>
     * 取值的条目即为错值，故须显式声明。多目标源（{@code MINIONS} 等）在求值阶段由引擎落到
     * 本次实际被作用的那个成员（见 {@code StatEngine#resolveStageEntity}），每个成员各按自身取值。
     */
    public StatBuilder valueTarget(TargetSelector sel) {
        return new StatBuilder(effect, target, conds, params, amount, sel, conditionTarget,
                spillTo, convertTo, duration);
    }

    /** 条件阶段「被判定实体」覆写（多目标源解析规则同 {@link #valueTarget}）。 */
    public StatBuilder conditionTarget(TargetSelector sel) {
        return new StatBuilder(effect, target, conds, params, amount, valueTarget, sel,
                spillTo, convertTo, duration);
    }

    /** 叠加单个具名条件集（可链式多次调用）；条件集内部按逻辑与组合。 */
    public StatBuilder when(Identifier cond) {
        List<ValueExpr> next = new ArrayList<>(conds);
        next.addAll(StatConditionGen.conditions(cond));
        return new StatBuilder(effect, target, List.copyOf(next), params, amount,
                valueTarget, conditionTarget, spillTo, convertTo, duration);
    }

    /** 叠加多个具名条件集。 */
    public StatBuilder when(Identifier... more) {
        List<ValueExpr> next = new ArrayList<>(conds);
        for (Identifier c : more) {
            next.addAll(StatConditionGen.conditions(c));
        }
        return new StatBuilder(effect, target, List.copyOf(next), params, amount,
                valueTarget, conditionTarget, spillTo, convertTo, duration);
    }

    public StatBuilder param(String key, ExpressionCodec.Value val) {
        Map<String, ExpressionCodec.Value> next = new LinkedHashMap<>(params);
        next.put(key, val);
        return new StatBuilder(effect, target, conds, next, amount, valueTarget, conditionTarget,
                spillTo, convertTo, duration);
    }

    /** 参数槽便捷重载：常量默认值（{@code $key} 在 amount / conditions / describe 中的定义期缺省）。 */
    public StatBuilder param(String key, double val) {
        return param(key, ExpressionCodec.of(val));
    }

    // ===================== 数值（amount）=====================

    /**
     * 覆写数值表达式（缺省为 {@code $value}，幅度由实例侧 {@code vars}/roll 注入）。
     *
     * <p>用于「线性缩放」类词条——数值本身是一条表达式而非单一注入量，例如
     * {@code 1% increased Damage per 15 Strength} 的数值 = {@code $value * attribute:coe:strength / $per}。
     * 参与表达式的 {@code $name} 缺省值经 {@link #param} 提供，实例侧仍可覆盖。
     */
    public StatBuilder amount(ExpressionCodec.Value amt) {
        return new StatBuilder(effect, target, conds, params, amt, valueTarget, conditionTarget,
                spillTo, convertTo, duration);
    }

    /**
     * 覆写数值表达式（math 文本形式，经 {@link MathParser} 解析）。
     *
     * <p>语法与 math 系统一致，不发明新语法：{@code $value}、{@code attribute:coe:strength}、
     * {@code min(a, b)}、四则运算。示例：
     * {@code amountExpr("$value * attribute:coe:strength / $per")}。
     */
    public StatBuilder amount(String mathExpr) {
        return amount(ExpressionCodec.of(MathParser.parse(mathExpr)));
    }

    // ===================== 时长段（第五部分）=====================

    /**
     * 声明本条目的时长段：效果「在时间上如何存续 / 复触发」，见 {@link StatDuration.Kind}。
     *
     * <p><b>种类不可混用</b>：{@code DURATION} 是「施加后持续多久」，{@code INTERVAL} 是
     * 「每隔多久重新施加一次」的节拍，{@code COOLDOWN} 是「再次触发前的最短间隔」。
     * 状态施加的<b>有效期</b>只有 {@code DURATION} 一种写法
     * （{@code StatusStatEffect#durationFallback} 只认它，其余回落状态自带默认时长）。
     */
    public StatBuilder duration(StatDuration d) {
        return new StatBuilder(effect, target, conds, params, amount, valueTarget, conditionTarget,
                spillTo, convertTo, d);
    }

    /** 便捷重载：以 {@link StatDuration.Kind#DURATION}（施加后持续 {@code ticks} tick）声明时长。 */
    public StatBuilder duration(int ticks) {
        return duration(StatDuration.of(StatDuration.Kind.DURATION, ticks));
    }

    // ===================== 收尾 =====================

    public StatRead build() {
        if (effect == null) {
            throw new IllegalStateException("StatBuilder: effect is required");
        }
        StatRead s;
        if (spillTo == null && convertTo == null) {
            s = new StatRead(amount, conds, buildTargets(), effect, params);
        } else {
            s = new StatRead(amount, conds, buildTargets(), effect, params,
                    spillTo == null ? GateExpr.NEVER : spillTo,
                    convertTo == null ? GateExpr.NEVER : convertTo);
        }
        // 时长段在条目层收尾时置入：StatRead.withDuration 返回 StatRead。
        return duration == null ? s : s.withDuration(duration);
    }

    // ===================== 外溢作用域（数值额外计入的作用域）=====================

    /**
     * 声明外溢作用域：本条目数值除效果自身 {@code tags} 外，也计入 {@code spill}（并集语义）。
     *
     * <p>用于「X 的增加值也计入 Y」这类<b>纯数值</b>机制——与「转换」（from 扣除 / to 增加）
     * 和「按来源属性折算」（写进 amount 的 math 表达式）都不同：此处条目在两个作用域下
     * <b>各完整生效一次</b>，来源与目标两侧的数值互不扣减。
     *
     * <p>方向性：一元声明，挂在<b>贡献方</b>条目上（「我的数值额外计入这些作用域」），
     * 不存在「A 引 B / B 引 A」的成对关系，故数据形状上无法构成环。
     */
    public StatBuilder spillTo(GateExpr spill) {
        return new StatBuilder(effect, target, conds, params, amount, valueTarget, conditionTarget,
                spill, convertTo, duration);
    }

    /** 变参便捷重载；空变参按「不声明」处理（{@link GateExpr#NEVER}），不得退化为恒真。 */
    public StatBuilder spillTo(String... tags) {
        return spillTo(tags == null || tags.length == 0 ? GateExpr.NEVER : GateExpr.of(List.of(tags)));
    }

    // ===================== 转换作用域（数值改计入的作用域，来源不再计入）=====================

    /**
     * 声明转换作用域：本条目数值<b>取代</b>效果自身 {@code tags} 作用域，只计入 {@code to}。
     *
     * <p>用于「X 的增加值<b>转换为</b> Y」这类<b>纯数值</b>机制——与 {@link #spillTo}（并集：
     * X 与 Y <b>各计一次</b>，X 的贡献不被扣减）语义相反：转换后来源作用域<b>不再计入</b>，
     * 是一次真正的 MOVE 而非 COPY。
     *
     * <p>与 {@link #spillTo} 互斥：同时声明两者在 {@code Stat} 规范构造期即抛错——并集与替代无法
     * 合成单一门控表达式，不做静默取舍。
     *
     * <p>方向性：与外溢同为挂在贡献方条目上的一元声明，聚合侧只做一次门控替换、不递归，
     * 故数据形状上无法构成环。
     */
    public StatBuilder convertTo(GateExpr to) {
        return new StatBuilder(effect, target, conds, params, amount, valueTarget, conditionTarget,
                spillTo, to, duration);
    }

    /** 变参便捷重载；空变参按「不转换」处理（{@link GateExpr#NEVER}），不得退化为恒真。 */
    public StatBuilder convertTo(String... tags) {
        return convertTo(tags == null || tags.length == 0 ? GateExpr.NEVER : GateExpr.of(List.of(tags)));
    }

    /**
     * 三段选择器组装：未覆写 value / condition 时沿用既有 targets 对象（零新增内容）；
     * 任一被覆写则把其内容解出、合并为内联三段选择器。
     */
    private StatTargets buildTargets() {
        if (valueTarget == null && conditionTarget == null) {
            return target == null ? StatTargets.EMPTY : target;
        }
        TargetSelector t = target == null ? null : target.target();
        TargetSelector c = conditionTarget != null ? conditionTarget
                : (target == null ? null : target.conditionTarget());
        TargetSelector v = valueTarget != null ? valueTarget
                : (target == null ? null : target.valueTarget());
        return new StatTargetsInline(t, c, v);
    }
}
