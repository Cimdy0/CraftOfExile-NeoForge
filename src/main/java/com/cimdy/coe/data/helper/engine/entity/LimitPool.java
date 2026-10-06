package com.cimdy.coe.data.helper.engine.entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <b>通用上限池</b>（与具体实体类别无关的数量上限设施）。
 *
 * <p>本类提炼自 {@link EntityCategoryLimit} 的「类别上限」语义，但去掉类别 / 实体 / 归属等专属概念，
 * 做成一条<b>完全数据无关</b>的通用链路：任意 {@code key → entry} 都可由本池约束数量上限。
 * 用例涵盖（但不限于）缓存、临时物件登记、按来源的去重配额等。
 *
 * <h3>两条核心维度</h3>
 * <ol>
 *   <li><b>超限策略</b>（{@link Strategy}）：
 *     <ul>
 *       <li>{@link Strategy#REJECT}：已达上限时拒绝新加入（{@link #tryAdd} 返回 {@code false}）；</li>
 *       <li>{@link Strategy#EVICT_OLDEST}：已达上限时移除<b>最旧</b>成员再容纳新成员（始终成功）。</li>
 *     </ul>
 *   </li>
 *   <li><b>豁免来源</b>（{@code Set<String> exemptions}）：标记为该来源（{@code source}）的加入
 *       不受上限约束，常用来表示「系统 / 受信任来源」可绕过配额。</li>
 * </ol>
 *
 * <h3>存储后端</h3>
 * <p>复用 {@link EntityCategoryLimit} 既有的 {@link LinkedHashMap} 习惯：插入序即时序，
 * 既保证 O(1) 查询，又让 {@link Strategy#EVICT_OLDEST} 能以「迭代器首元素」精确取回最旧成员，
 * 无需额外队列。所有读写均 {@code synchronized}，与既有设施一致地规避并发问题。
 *
 * <p><b>无外部新依赖</b>：仅用 JDK 容器；不引入项目未使用的三方库。
 */
public final class LimitPool<K, E> {

    /** 超限策略：达到上限时如何处置新加入请求。 */
    public enum Strategy {
        /** 拒绝：超了不放，{@link #tryAdd} 返回 {@code false}。 */
        REJECT,
        /** 淘汰最旧：超了先移除最旧成员，再容纳新成员（始终成功）。 */
        EVICT_OLDEST
    }

    /** 无上限哨兵：{@link #setMax} 设为此值后等价于「不设限」（对既有条目零行为变化）。 */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    /** 池内容：插入序 = 时序，供 {@link Strategy#EVICT_OLDEST} 取最旧。 */
    private final Map<K, E> entries = new LinkedHashMap<>();

    /** 豁免来源集合：命中 {@code source} 的加入不受上限约束。 */
    private final Set<String> exemptions = new HashSet<>();

    /** 当前上限（{@link #UNLIMITED} 或更小的非负整数）。可被 {@link #setMax} 动态改写。 */
    private int max;

    /** 超限策略（可在运行期经 {@link #setStrategy} 改写）。 */
    private Strategy strategy;

    /** 以给定上限与 {@link Strategy#REJECT} 策略建池。 */
    public LimitPool(int max) {
        this(max, Strategy.REJECT);
    }

    /** 以给定上限与策略建池。 */
    public LimitPool(int max, Strategy strategy) {
        this.max = max;
        this.strategy = strategy == null ? Strategy.REJECT : strategy;
    }

    /**
     * 尝试加入一个成员。
     *
     * @param key    成员键（{@code null} 直接拒绝）
     * @param entry  成员值
     * @param source 来源标记；命中 {@link #exemptions} 时绕过上限
     * @return 是否成功加入（{@link Strategy#REJECT} 且达上限时 {@code false}）
     */
    public synchronized boolean tryAdd(K key, E entry, String source) {
        if (key == null) return false;
        // 豁免来源：不受上限约束，直接放入（已存在则视为续期更新）。
        if (exemptions.contains(source)) {
            entries.put(key, entry);
            return true;
        }
        // 已存在：视为存活续期，更新值但不占新额度、也不改最旧时序。
        if (entries.containsKey(key)) {
            entries.put(key, entry);
            return true;
        }
        int limit = max <= 0 ? UNLIMITED : max;
        if (entries.size() < limit) {
            entries.put(key, entry);
            return true;
        }
        if (strategy == Strategy.EVICT_OLDEST) {
            Iterator<K> it = entries.keySet().iterator();
            if (it.hasNext()) entries.remove(it.next()); // 移除最旧
            entries.put(key, entry);
            return true;
        }
        return false; // REJECT：达上限，拒绝
    }

    /**
     * 强制加入：达上限且策略为 {@link Strategy#REJECT} 时抛异常，否则保证成功。
     *
     * @throws IllegalStateException 当 {@link Strategy#REJECT} 且池已满
     */
    public synchronized void add(K key, E entry, String source) {
        if (!tryAdd(key, entry, source)) {
            throw new IllegalStateException("LimitPool exhausted for key: " + key + " (max=" + max + ")");
        }
    }

    /** 移除指定键的成员，返回被移除的值（不存在则返回 {@code null}）。 */
    public synchronized E remove(K key) {
        return entries.remove(key);
    }

    /** 取指定键的成员（不存在则返回 {@code null}）。 */
    public synchronized E get(K key) {
        return entries.get(key);
    }

    /** 是否含指定键。 */
    public synchronized boolean contains(K key) {
        return entries.containsKey(key);
    }

    /** 当前成员数量。 */
    public synchronized int size() {
        return entries.size();
    }

    /** 改写上限（{@link #UNLIMITED} 或更小的非负整数均可）。 */
    public synchronized void setMax(int max) {
        this.max = max;
    }

    /** 当前上限。 */
    public synchronized int getMax() {
        return max;
    }

    /** 改写超限策略。 */
    public synchronized void setStrategy(Strategy strategy) {
        this.strategy = strategy == null ? Strategy.REJECT : strategy;
    }

    /** 当前超限策略。 */
    public synchronized Strategy getStrategy() {
        return strategy;
    }

    /** 登记一个豁免来源（该来源加入不受上限约束）。 */
    public synchronized void addExemption(String source) {
        if (source != null) exemptions.add(source);
    }

    /** 取消一个豁免来源。 */
    public synchronized void removeExemption(String source) {
        exemptions.remove(source);
    }

    /** 某来源是否豁免上限。 */
    public synchronized boolean isExempt(String source) {
        return exemptions.contains(source);
    }

    /** 清空全部成员（上限 / 策略 / 豁免集合保持不变）。 */
    public synchronized void clear() {
        entries.clear();
    }

    /** 当前全部键的快照（避免外部直接持有内部映射）。 */
    public synchronized Set<K> keys() {
        return new HashSet<>(entries.keySet());
    }
}
