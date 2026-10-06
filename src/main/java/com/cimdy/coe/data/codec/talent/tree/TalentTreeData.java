package com.cimdy.coe.data.codec.talent.tree;

import com.cimdy.coe.data.codec.talent.data.TalentData;
import com.cimdy.coe.data.codec.talent.data.TalentState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 单棵天赋树的全部玩家数据，作为 {@link TalentData} 中以树名（treeId）为键的存储单元。
 *
 * <p>玩家在一棵树上的进度<b>只有分配状态本身</b>：可用点数是树模型的静态属性
 * （{@code TalentTreeModel#totalPoints()}），武器组绑定记录在每个 {@code TalentAllocation} 上，
 * 重置花费则按角色等级实时算（{@code TalentRespecCost}）。三者都不是可累积的玩家资源，
 * 因而本记录不再携带 {@code talentPoints} / {@code weaponSetPoints} / {@code regretPoints}
 * 这类恒为 0、全仓零读写的占位字段（POE2 已移除 Orb of Regret，重置 100% 走金币）。
 *
 * <p>新增天赋树只需在 {@link TalentData} 的 Map 中增加一个条目，无需再为每棵树新增独立存储字段。
 */
public record TalentTreeData(
        TalentState state
) {
    public static final Codec<TalentTreeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TalentState.CODEC.fieldOf("state").forGetter(TalentTreeData::state)
    ).apply(instance, TalentTreeData::new));

    public static final TalentTreeData EMPTY = new TalentTreeData(TalentState.EMPTY);
}
