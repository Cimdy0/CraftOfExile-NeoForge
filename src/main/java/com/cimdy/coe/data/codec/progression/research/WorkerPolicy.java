package com.cimdy.coe.data.codec.progression.research;

import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskAssigner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 工人行为策略（研究树对「实体行为」的控制）。
 *
 * <p>{@code allow_work_types} 为白名单（空 = 不限制）；{@code priority_bias} 偏置调度优先级
 * （如某工种名）；{@code idle_behavior} 空闲行为（参考值：wander / station / rest）。
 * 由 {@code WorkerAIInjector} 与 {@code TaskAssigner} 消费，决定工人可做的工种与优先级。
 */
public record WorkerPolicy(List<String> allowWorkTypes, String priorityBias, String idleBehavior) {

    public static final WorkerPolicy EMPTY = new WorkerPolicy(List.of(), "", "wander");

    public static final Codec<WorkerPolicy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("allow_work_types", List.of()).forGetter(WorkerPolicy::allowWorkTypes),
            Codec.STRING.optionalFieldOf("priority_bias", "").forGetter(WorkerPolicy::priorityBias),
            Codec.STRING.optionalFieldOf("idle_behavior", "wander").forGetter(WorkerPolicy::idleBehavior)
    ).apply(instance, WorkerPolicy::new));

}
