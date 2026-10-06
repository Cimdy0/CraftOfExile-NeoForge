package com.cimdy.coe.data.codec.progression.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 任务前置条件。玩家等级（1-100）、已完成任务、已解锁科技三者任一不满足则任务不激活。
 * 数据组合式，便于整合包作者自由编排长线任务链。
 */
public record QuestPrerequisite(int minLevel, List<Identifier> quests, List<Identifier> tech) {

    public static final QuestPrerequisite EMPTY = new QuestPrerequisite(1, List.of(), List.of());

    public static final Codec<QuestPrerequisite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(QuestPrerequisite::minLevel),
            Identifier.CODEC.listOf().optionalFieldOf("quests", List.of()).forGetter(QuestPrerequisite::quests),
            Identifier.CODEC.listOf().optionalFieldOf("tech", List.of()).forGetter(QuestPrerequisite::tech)
    ).apply(instance, QuestPrerequisite::new));

    public static QuestPrerequisite of(int minLevel, List<Identifier> quests, List<Identifier> tech) {
        return new QuestPrerequisite(minLevel, quests, tech);
    }
}
