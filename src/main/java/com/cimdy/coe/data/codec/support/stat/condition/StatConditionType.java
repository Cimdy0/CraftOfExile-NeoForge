package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;

/**
 * Condition 节点的类型描述符：一条注册项聚合一个「判断」的全部序列化面向。
 * <ul>
 *   <li>{@code key}：JSON {@code type} 字段（数据驱动分派键）；</li>
 *   <li>{@code clazz}：节点实现类（encode 时按实例类反查）；</li>
 *   <li>{@code codec}：节点自身 CODEC（递归节点的 CODEC 由 {@code self} 装配）。</li>
 * </ul>
 * 注册入口 {@link StatConditionTypes#register}。
 * <b>新增一种判断 = 一个 {@link StatCondition} 实现 + bootstrap 中一行 register。</b>
 */
public record StatConditionType<T extends StatCondition>(
        String key,
        Class<T> clazz,
        Codec<T> codec
) {
    public static <T extends StatCondition> StatConditionType<T> of(String key, Class<T> clazz, Codec<T> codec) {
        return new StatConditionType<>(key, clazz, codec);
    }
}
