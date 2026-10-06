package com.cimdy.coe.data.codec.item.addition;

import com.cimdy.coe.data.codec.item.addition.impl.VeiledAddition;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

/**
 * 词缀贡献<b>接收端</b>（消费端口）—— {@link AdditionType} 实现只依赖本接口，
 * 具体落点（全局 StatStore）由实现方决定。
 *
 * <p>这是消除「{@code instanceof} 具体 StatEffect 子类」判别的结构手段：
 * 消费者不再读取 {@code StatEntry.resolve().effect()} 的运行时类型来猜测语义，
 * 而是由 {@link AdditionType} 自身的实现类<b>多态分派</b>到对应方法。
 *
 * <p>新增一种词缀种类 = 新增一个 {@link AdditionType} 实现类（选择调用哪个端口方法，
 * 或组合多个），核心消费者的 {@code calculateStatEntries} 零改动。
 */
public interface ContributionSink {

    /**
     * 全局：把一条 Stat 条目注入角色 {@code StatStore}（由 {@code StatEngine.fold} 聚合）。
     *
     * @param entry 已完成 roll 值注入的条目
     */
    void global(StatEntry entry);

    /** 本上下文是否已揭示（{@link VeiledAddition} 用）；非隐匿上下文默认 {@code true}。 */
    default boolean revealed() {
        return true;
    }

    /** 标记「本装备存在未揭示的隐匿词缀」→ 装备应视为不可穿戴 / 不可用。 */
    default void markVeiled() {
    }
}
