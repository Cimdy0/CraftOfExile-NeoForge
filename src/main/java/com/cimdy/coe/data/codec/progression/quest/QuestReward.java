package com.cimdy.coe.data.codec.progression.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 任务奖励。覆盖面广且可组合：经验（驱动升级）、科技点（解锁科技树按钮）、
 * 研究点（研究树），并可直接解锁其它科技/任务节点，便于做分支与长线引导。
 */
public record QuestReward(int xp, int techPoints, int researchPoints,
                          List<Identifier> unlockTech, List<Identifier> unlockQuests) {

    public static final QuestReward EMPTY = new QuestReward(0, 0, 0, List.of(), List.of());

    public static final Codec<QuestReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("xp", 0).forGetter(QuestReward::xp),
            Codec.INT.optionalFieldOf("tech_points", 0).forGetter(QuestReward::techPoints),
            Codec.INT.optionalFieldOf("research_points", 0).forGetter(QuestReward::researchPoints),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_tech", List.of()).forGetter(QuestReward::unlockTech),
            Identifier.CODEC.listOf().optionalFieldOf("unlock_quests", List.of()).forGetter(QuestReward::unlockQuests)
    ).apply(instance, QuestReward::new));

    /** 仅经验/点数的最简奖励。 */
    public static QuestReward of(int xp, int techPoints, int researchPoints) {
        return new QuestReward(xp, techPoints, researchPoints, List.of(), List.of());
    }

    public static QuestReward empty() {
        return EMPTY;
    }
}
