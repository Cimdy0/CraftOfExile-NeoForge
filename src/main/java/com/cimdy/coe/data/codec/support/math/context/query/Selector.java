package com.cimdy.coe.data.codec.support.math.context.query;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import com.cimdy.coe.data.codec.support.math.context.OutputMode;

/**
 * 泛型选择管线（方案四核心）：收集 → 过滤 → 排序 → 数量窗口 → 输出分发，对候选域 {@code T} 完全泛化。
 * <p>
 * 设计要点（对应 context_dsl_design.md §3 与方案四收敛结论）：
 * <ul>
 *   <li><b>域解耦</b>：管线只依赖 {@link Domain} 适配器把 {@code T} 变成 {@link Candidate}；
 *       实体域现成（{@link #living}），未来物品 / 方块域各提供一个适配器即可全线复用；</li>
 *   <li><b>零重建热循环</b>：候选作用域切换统一走 {@link #inScope}（{@link ValueScope#swap}
 *       指针交换 + finally 恢复），循环内不重建任何上下文对象；</li>
 *   <li><b>排序键预计算</b>：decorate-sort-undecorate，每候选只求值一次排序键
 *       （比较器内反复 withEntity 重建 + 求值会达 O(n log n) 次）；</li>
 *   <li><b>输出一次分发</b>：{@link OutputMode} 在循环结束后分发一次，不进热循环。</li>
 * </ul>
 */
public final class Selector<T> {
    /** 候选域适配器：把域对象变成统一 {@link Candidate}（新增候选域的唯一接入点）。 */
    @FunctionalInterface
    public interface Domain<T> {
        Candidate adapt(T item);
    }

    private final List<T> items;
    private final Domain<T> domain;

    private Selector(List<T> items, Domain<T> domain) {
        this.items = items;
        this.domain = domain;
    }

    /** 实体域管线（当前唯一内置域）。 */
    public static Selector<LivingEntity> living(List<LivingEntity> items) {
        return new Selector<>(items, Candidate::of);
    }

    /** 任意候选域管线（物品 / 方块等未来域接入口）。 */
    public static <T> Selector<T> of(List<T> items, Domain<T> domain) {
        return new Selector<>(items, domain);
    }

    // ---- 管线阶段 ----

    /** 过滤（就地，AND 语义可多次调用）。 */
    public Selector<T> filter(Predicate<T> where) {
        items.removeIf(where.negate());
        return this;
    }

    /**
     * 排序：{@code key} 在各候选作用域内求值（指针交换），键一次性预计算后排序。
     */
    public Selector<T> sortBy(ValueScope scope, ToDoubleFunction<ValueScope> key, boolean desc) {
        int n = items.size();
        if (n > 1) {
            double[] keys = new double[n];
            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) {
                // 排序键以参照实体（current）为裸读主体、候选（被判断对象）经 candidate./dist() 访问，
                // 与 filter 表达式语义一致（context_dsl_design.md §3.3 / M5 ④）
                keys[i] = scope.evalFilter(domain.adapt(items.get(i)), key);
                order[i] = i;
            }
            java.util.Arrays.sort(order, (a, b) ->
                    desc ? Double.compare(keys[b], keys[a]) : Double.compare(keys[a], keys[b]));
            List<T> sorted = new java.util.ArrayList<>(n);
            for (int i : order) sorted.add(items.get(i));
            items.clear();
            items.addAll(sorted);
        }
        return this;
    }

    /** 数量窗口：截取前 N 个。 */
    public Selector<T> limit(int n) {
        if (n < items.size()) items.subList(n, items.size()).clear();
        return this;
    }

    // ---- 输出（OutputMode 分发，循环后一次）----

    /** {@link OutputMode#COUNT}：匹配数量。 */
    public int count() {
        return items.size();
    }

    /** 输出当前候选集合（过滤 / 排序 / 数量窗口后的结果列表，供消费端直接取用）。 */
    public List<T> result() {
        return items;
    }

    /** {@link OutputMode#SUM}：对每个候选在其作用域内读值累加。 */
    public double sum(ValueScope scope, ToDoubleFunction<ValueScope> reader) {
        double total = 0;
        for (T item : items) total += inScope(scope, item, reader);
        return total;
    }

    /**
     * {@link OutputMode#PICK}：取第 {@code oneBased} 个候选（1-based，越界取末尾）；
     * 空集返回 null（调用方以 null 候选进 {@link #inScope}，读值经 default 兜底）。
     */
    public T pick(int oneBased) {
        if (items.isEmpty()) return null;
        int idx = Math.min(oneBased - 1, items.size() - 1);
        return items.get(Math.max(0, idx));
    }

    /**
     * 候选作用域求值：域适配后委托 {@link ValueScope#evalIn}（唯一原语实现点）。
     * {@code item} 为 null 时以空候选求值（读值节点判空兜底 0 / default）。
     */
    public double inScope(ValueScope scope, T item, ToDoubleFunction<ValueScope> body) {
        return scope.evalIn(item == null ? null : domain.adapt(item), body);
    }
}
