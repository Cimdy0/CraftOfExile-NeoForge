package com.cimdy.coe.data.codec.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 科技节点效果（数据组合式，覆盖面广且高拓展）。
 *
 * <p>不采用密封子类——所有可解锁内容都是可选字段，整合包作者<b>零代码</b>即可组合出
 * 「解锁工种 + 提升实体上限 + 提升区块预算 + 解锁建筑 + 解锁下游研究/任务」等任意组合。
 * 需要全新效果种类（如解锁维度）时，才在此加一个可选字段并在应用器里消费。
 */
public record TechEffect(
        List<String> unlockWorkTypes,
        List<Identifier> unlockBlocks,
        List<Identifier> unlockItems,
        int entityCapAdd,
        int chunkBudgetAdd,
        int siteCapAdd,
        List<Identifier> unlockResearch,
        List<Identifier> unlockQuests) {

    public static final TechEffect EMPTY = new TechEffect(List.of(), List.of(), List.of(), 0, 0, 0, List.of(), List.of());

    public static final Codec<TechEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("unlock_work_types", List.of()).forGetter(TechEffect::unlockWorkTypes),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_blocks", List.of()).forGetter(TechEffect::unlockBlocks),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_items", List.of()).forGetter(TechEffect::unlockItems),
            Codec.INT.optionalFieldOf("entity_cap_add", 0).forGetter(TechEffect::entityCapAdd),
            Codec.INT.optionalFieldOf("chunk_budget_add", 0).forGetter(TechEffect::chunkBudgetAdd),
            Codec.INT.optionalFieldOf("site_cap_add", 0).forGetter(TechEffect::siteCapAdd),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_research", List.of()).forGetter(TechEffect::unlockResearch),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_quests", List.of()).forGetter(TechEffect::unlockQuests)
    ).apply(instance, TechEffect::new));

    /** 合并两个效果（用于把多个已解锁科技节点的效果累加进玩家预算）。 */
    public TechEffect merge(TechEffect o) {
        List<String> wt = new ArrayList<>(unlockWorkTypes);
        wt.addAll(o.unlockWorkTypes);
        List<Identifier> ub = new ArrayList<>(unlockBlocks);
        ub.addAll(o.unlockBlocks);
        List<Identifier> ui = new ArrayList<>(unlockItems);
        ui.addAll(o.unlockItems);
        List<Identifier> ur = new ArrayList<>(unlockResearch);
        ur.addAll(o.unlockResearch);
        List<Identifier> uq = new ArrayList<>(unlockQuests);
        uq.addAll(o.unlockQuests);
        return new TechEffect(wt, ub, ui,
                entityCapAdd + o.entityCapAdd,
                chunkBudgetAdd + o.chunkBudgetAdd,
                siteCapAdd + o.siteCapAdd, ur, uq);
    }
}
