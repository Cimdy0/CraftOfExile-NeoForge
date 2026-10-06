package com.cimdy.coe.data.codec.support.math.context;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.query.Candidate;
import com.cimdy.coe.data.codec.support.math.context.query.SplitSink;
import com.cimdy.coe.event.Event;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.ToDoubleFunction;

/**
 * Context 求值令牌（方案四重构）：{@code root} 数据基站固定 + {@code current} 候选指针可交换。
 * <p>
 * 进入选择器 / 谓词候选循环时不再重建 {@link ExpressionContext}，
 * 而是对每个候选只做一次 {@link #swap(Candidate)} 指针交换（零上下文重建），求值后 finally 恢复——
 * 这是「扩展性最大 + 运行期最省」方案的运行期核心。
 * <ul>
 *   <li>{@code root}：参照者（施法者 / Context 持有者）的数据基站，循环内固定；</li>
 *   <li>{@code current}：当前候选（{@link Candidate}），无嵌套时即 root 持有者实体。</li>
 * </ul>
 * 所有实体读值节点默认作用于 {@code current}；事件 / 计时读值经 {@link #event()} /
 * {@link #timerElapsed} 归一解析数据源（current 优先，回落 root），保证候选循环内
 * 读到的是候选自身的事件历史（与候选切换前语义一致）。
 */
public final class ValueScope {
    private final ExpressionContext root;
    private Candidate current;
    /** filter / sort 表达式中被判断对象（候选）；非 filter 上下文时为 null（回落 current）。 */
    private Candidate filterCandidate;
    /** 一对一拆分发射通道（{@link SplitSink}）：由消费端求值前注入，EACH 选择器逐候选发射；null = 不拆分。 */
    private SplitSink splitSink;

    public ValueScope(LivingEntity current, ExpressionContext root) {
        this.root = root;
        this.current = current == null ? null : Candidate.of(current);
        this.filterCandidate = null;
    }

    private ValueScope(Candidate current, ExpressionContext root) {
        this.root = root;
        this.current = current;
        this.filterCandidate = null;
    }

    /** 以 ctx 持有者实体为当前候选组装求值令牌（math 求值入口）。 */
    public static ValueScope of(ExpressionContext ctx) {
        return new ValueScope(ctx.getEntity() instanceof LivingEntity l ? l : null, ctx);
    }

    /** 根数据基站（技能 / 伤害 / 槽位 / 注入变量等共享数据）。 */
    public ExpressionContext root() {
        return root;
    }

    /** 当前候选的实体域视图；候选为空或非实体域时为 null。 */
    public LivingEntity current() {
        return current == null ? null : current.living();
    }

    /** 当前候选（任意域）。 */
    public Candidate candidate() {
        return current;
    }

    /**
     * 候选作用域求值原语（唯一实现点）：指针交换进入候选作用域 → 求值 → finally 恢复。
     * {@link Selector} / {@link ContextCondition} / 谓词 filter 等所有候选循环消费端一律委托本方法，
     * 不得各自手写 swap + try/finally 样板。candidate 为 null 时以空候选求值（读值节点判空兜底）。
     */
    public double evalIn(Candidate candidate, ToDoubleFunction<ValueScope> body) {
        Candidate prev = swap(candidate);
        try {
            return body.applyAsDouble(this);
        } finally {
            swap(prev);
        }
    }

    /**
     * 候选指针交换（零分配、零上下文重建）：返回旧候选，调用方须在 finally 中换回。
     * 常规消费端请直接用 {@link #evalIn}；本方法仅供需要非 double 返回值等特殊形态时兜底。
     */
    public Candidate swap(Candidate next) {
        Candidate prev = this.current;
        this.current = next;
        return prev;
    }

    /**
     * filter / sort 表达式求值原语：以参照实体（{@code current}，即当前层默认实体）保持为当前实体，
     * 把候选（被判断对象）挂入 {@code filterCandidate}，供 {@code candidate.} 前缀与 {@code dist()} 解析访问，
     * 不交换 {@code current}（保证裸读 = 参照实体，符合 context_dsl_design.md §3.4）。finally 还原。
     */
    public double evalFilter(Candidate candidate, ToDoubleFunction<ValueScope> body) {
        Candidate prev = this.filterCandidate;
        this.filterCandidate = candidate;
        try {
            return body.applyAsDouble(this);
        } finally {
            this.filterCandidate = prev;
        }
    }

    /**
     * 被判断对象（候选）：filter 上下文中为本次评估的候选实体，否则回落为当前实体。
     * {@code candidate.} 前缀与 {@code dist()} 据此读取候选，与裸读（{@code current} = 参照实体）区分。
     */
    public Candidate candidateEntity() {
        return filterCandidate != null ? filterCandidate : current;
    }

    /** 换根（Read 模板注入变量等场景）：新 root、保留当前候选指针（拆分通道随之传递）。 */
    public ValueScope withRoot(ExpressionContext newRoot) {
        ValueScope s = new ValueScope(this.current, newRoot);
        s.splitSink = this.splitSink;
        return s;
    }

    /** 注入一对一拆分发射通道（消费端在求值前设置，本次求值内 EACH 选择器逐候选发射）。 */
    public void setSplitSink(SplitSink splitSink) {
        this.splitSink = splitSink;
    }

    /**
     * 统一发射入口（所有 node 通用，无需各自 import {@link SplitSink}）：
     * 把 {@code (实体, 值)} 逐对发往消费端注入的 {@link SplitSink}；未注入（非拆分上下文）时为无操作。
     * <p>
     * 用法示例（任意 node，含嵌套节点）：若本节点在拆分上下文中枚举出候选实体并求得其值，
     * 直接 {@code scope.emit(entity, value)} 即可——调用方 {@code onTrigger} 拿到对子后逐对单独作用。
     */
    public void emit(LivingEntity entity, double value) {
        if (splitSink != null) splitSink.emit(entity, value);
    }

    // ---- 数据源归一解析（current 优先，回落 root）----

    /**
     * 当前候选的近期事件 + 坐标历史：候选即 root 持有者时直接用 root 缓存；
     * 候选被交换（选择器 / 谓词循环）时读候选自身的事件状态。
     */
    public Event.State event() {
        LivingEntity l = current();
        if (l == null || l == root.getEntity()) return root.event();
        return Event.get(l);
    }

    /** 当前候选的事件 timer 经过 tick（候选被交换时读候选自身单例的 timer 存储）。 */
    public double timerElapsed(String name, double def) {
        LivingEntity l = current();
        if (l == null || l == root.getEntity()) return root.getTimerElapsed(name, def);
        return ExpressionContext.get(l).getTimerElapsed(name, def);
    }

    /**
     * 交互对方实体（命中结算中的对侧）：攻击者作用域持受害者，受害者作用域持攻击者；
     * 非命中场景（纯被动读取 / UI 展示）无 StatContext → 返回 {@code null}（调用方据此回落 0 / 不生效）。
     * 统一门面，取代各处 {@code root().currentStatContext().sourceEntity()} 裸隧道。
     */
    public LivingEntity sourceEntity() {
        var sc = root.currentStatContext();
        return sc != null ? sc.sourceEntity() : null;
    }
}
