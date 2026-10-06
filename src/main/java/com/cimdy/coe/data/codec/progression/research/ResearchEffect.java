package com.cimdy.coe.data.codec.progression.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 研究节点效果（树状研究树的产出）。
 *
 * <p>两层能力：{@code efficiency} 按工种给效率倍率；{@code policy} 控制工人行为。
 * 数据组合式，整合包作者零代码即可组合「采矿+25% 且 优先采矿」之类效果。
 */
public record ResearchEffect(List<EfficiencyBonus> efficiency, WorkerPolicy policy) {

    public static final ResearchEffect EMPTY = new ResearchEffect(List.of(), WorkerPolicy.EMPTY);

    public static final Codec<ResearchEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EfficiencyBonus.CODEC.listOf().optionalFieldOf("efficiency", List.of()).forGetter(ResearchEffect::efficiency),
            WorkerPolicy.CODEC.optionalFieldOf("policy", WorkerPolicy.EMPTY).forGetter(ResearchEffect::policy)
    ).apply(instance, ResearchEffect::new));

    /** 合并两个研究效果（多个研究节点生效时累加效率、后写入的策略覆盖）。 */
    public ResearchEffect merge(ResearchEffect o) {
        List<EfficiencyBonus> eff = new java.util.ArrayList<>(efficiency);
        eff.addAll(o.efficiency);
        return new ResearchEffect(eff, o.policy == WorkerPolicy.EMPTY ? policy : o.policy);
    }
}
