package com.cimdy.coe.data.helper.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * 数据包装义资源的描述符（CODEC 可反序列化自 {@code data/<ns>/resource/*.json}）。
 * 由 {@link ResourceDataLoader} 在资源重载时解析并转为 {@link ResourceEntry}。
 *
 * <p>关键变更：资源<b>不再新建存储</b>，而是经 {@link ResourceSource} 绑定到<b>既有的、可增可减的数值</b>
 * （如状态层数、召唤实体数）。数据包只声明「这个资源键指向哪个既有数据源」，即可零 Java 改动新增资源。
 *
 * <p>字段：
 * <ul>
 *   <li>{@code key} —— 资源键（与 GateTag 同源，如 "ignite_layer"）。</li>
 *   <li>{@code source} —— 既有数据源（status_stacks / attachment）。</li>
 *   <li>{@code max} —— 生成封顶（0 = 不封顶；状态层数仍受状态自身 maxStacks 限制）。</li>
 *   <li>{@code overflow} —— 溢出模式（默认 replace）。</li>
 *   <li>{@code is_float}/{@code fire_gain}/{@code fire_spend} —— 数值/事件语义开关。</li>
 *   <li>{@code regen_per_second} —— 每 tick 自然回复速率（0 = 不回复）。</li>
 * </ul>
 */
public record ResourceDescriptor(
        String key,
        boolean isFloat,
        ResourceSource source,
        OverflowMode overflow,
        boolean fireGain,
        boolean fireSpend,
        double max,
        boolean perSlot,
        double regenPerSecond) {

    public static final Codec<ResourceDescriptor> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("key").forGetter(ResourceDescriptor::key),
            Codec.BOOL.optionalFieldOf("is_float", true).forGetter(ResourceDescriptor::isFloat),
            ResourceSource.CODEC.fieldOf("source").forGetter(ResourceDescriptor::source),
            OverflowMode.CODEC.optionalFieldOf("overflow", OverflowMode.REPLACE).forGetter(ResourceDescriptor::overflow),
            Codec.BOOL.optionalFieldOf("fire_gain", false).forGetter(ResourceDescriptor::fireGain),
            Codec.BOOL.optionalFieldOf("fire_spend", true).forGetter(ResourceDescriptor::fireSpend),
            Codec.DOUBLE.optionalFieldOf("max", 0.0).forGetter(ResourceDescriptor::max),
            Codec.BOOL.optionalFieldOf("per_slot", false).forGetter(ResourceDescriptor::perSlot),
            Codec.DOUBLE.optionalFieldOf("regen_per_second", 0.0).forGetter(ResourceDescriptor::regenPerSecond)
    ).apply(i, ResourceDescriptor::new));

    /** 转注册表条目：用 {@link BoundResourcePool} 经 {@link ResourceSource} 读写既有数据源（不新建存储）。 */
    public ResourceEntry toEntry() {
        ResourceEntry.Builder b = ResourceEntry.builder(key,
                        new BoundResourcePool(source, max, perSlot, regenPerSecond))
                .isFloat(isFloat).overflow(overflow).fireGain(fireGain).fireSpend(fireSpend);
        if (max > 0) b.fixedMax(max);
        return b.build();
    }
}
