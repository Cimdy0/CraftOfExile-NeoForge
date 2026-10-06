package com.cimdy.coe.data.codec.talent.data;

import com.cimdy.coe.data.codec.talent.effect.TalentEffectApplier;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 玩家天赋分配状态（存于已同步附件，服务端权威）。
 *
 * <p>{@code version} 记录该分配状态最后校验过的天赋树数据版本。当它与当前
 * {@link TalentTreeModel#version()} 不一致时，由 {@link TalentEffectApplier} 按模型策略做版本迁移
 * （整树重置或保留选择并修正）。新玩家/洗点后的状态以当前模型版本戳记，避免误触发迁移。
 */
public record TalentState(List<TalentAllocation> allocated, int version) {
    public static final Codec<TalentState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TalentAllocation.CODEC.listOf().optionalFieldOf("allocated", List.of())
                    .forGetter(TalentState::allocated),
            Codec.INT.optionalFieldOf("version", 0)
                    .forGetter(TalentState::version)
    ).apply(instance, TalentState::new));

    public static final TalentState EMPTY = new TalentState(List.of(), 0);
}
