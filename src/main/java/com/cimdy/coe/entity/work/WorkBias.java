package com.cimdy.coe.entity.work;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/**
 * 实体类型的<b>基础工作偏置</b>：决定某类实体默认的工作速度与各工种的初始适应度。
 * <p>
 * 不同实体类型登记不同的 {@code WorkBias}，即为「不同实体类型拥有不同的工作适应度」提供数据来源。
 * 具体数值可在 {@link WorkBiasRegistry} 中按 {@code EntityType} 覆盖；未登记者回退 {@link #DEFAULT}。
 */
public record WorkBias(double baseSpeed, Map<String, Double> aptitude) {
    public static final Codec<WorkBias> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("base_speed", 1.0).forGetter(WorkBias::baseSpeed),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("aptitude", Map.of())
                    .forGetter(WorkBias::aptitude)
    ).apply(i, WorkBias::new));

    /** 未登记实体类型的回退偏置：速度 1.0、无任何工种适应度。 */
    public static final WorkBias DEFAULT = new WorkBias(1.0, Map.of());

    /** 取某工种的初始适应度等级（无记录返回 0）。 */
    public double aptitude(String workType) {
        return aptitude.getOrDefault(workType, 0.0);
    }
}
