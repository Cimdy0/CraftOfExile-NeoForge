package com.cimdy.coe.data.codec.progression.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 单工种效率加成。{@code multiplier} 乘到该工种工人的交互耗时 / 产出上，
 * 落地方式参照 {@code ITEM_CAPACITY}：注册属性 → StatEntry 写 StatStore → 工人 Goal 里
 * {@code entity.getAttributeValue(...)} 读取（见研究系统应用器）。
 */
public record EfficiencyBonus(String workType, float multiplier) {

    public static final Codec<EfficiencyBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("work_type").forGetter(EfficiencyBonus::workType),
            Codec.FLOAT.optionalFieldOf("multiplier", 1.0f).forGetter(EfficiencyBonus::multiplier)
    ).apply(instance, EfficiencyBonus::new));

}
