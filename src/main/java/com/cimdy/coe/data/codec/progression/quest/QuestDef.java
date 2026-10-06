package com.cimdy.coe.data.codec.progression.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 玩家任务定义（静态数据，来自数据包 JSON {@code data/<ns>/coe/quest/<id>.json}）。
 *
 * <p>{@code id} 不写进 JSON，而是由文件名注入（见 {@link #withId}），与天赋树节点同风格，
 * 避免数据里重复冗余 id。其余字段全部可选并带默认值，整合包作者只需写关心的内容。
 */
public record QuestDef(Identifier id, String name, List<QuestObjective> objectives,
                       QuestPrerequisite prereq, QuestReward reward, int sortOrder) {

    public static final Codec<QuestDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(QuestDef::name),
            QuestObjective.CODEC.listOf().optionalFieldOf("objectives", List.of()).forGetter(QuestDef::objectives),
            QuestPrerequisite.CODEC.optionalFieldOf("prerequisites", QuestPrerequisite.EMPTY).forGetter(QuestDef::prereq),
            QuestReward.CODEC.optionalFieldOf("rewards", QuestReward.empty()).forGetter(QuestDef::reward),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(QuestDef::sortOrder)
    ).apply(instance, (name, objectives, prereq, reward, sortOrder) ->
            new QuestDef(null, name, objectives, prereq, reward, sortOrder)));

    /**
     * 网络编解码：与 {@link #CODEC} 的唯一差别是<b>显式携带 id</b>。
     * 客户端任务 HUD 需要按 id 读取玩家进度计数，故同步时不能省略 id。
     */
    public static final Codec<QuestDef> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(QuestDef::id),
            Codec.STRING.optionalFieldOf("name", "").forGetter(QuestDef::name),
            QuestObjective.CODEC.listOf().optionalFieldOf("objectives", List.of()).forGetter(QuestDef::objectives),
            QuestPrerequisite.CODEC.optionalFieldOf("prerequisites", QuestPrerequisite.EMPTY).forGetter(QuestDef::prereq),
            QuestReward.CODEC.optionalFieldOf("rewards", QuestReward.empty()).forGetter(QuestDef::reward),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(QuestDef::sortOrder)
    ).apply(instance, QuestDef::new));

    /** 载入时由文件名注入 id（JSON 不含 id 字段）。 */
    public QuestDef withId(Identifier id) {
        return new QuestDef(id, name, objectives, prereq, reward, sortOrder);
    }

    /** 最简工厂：名称 + 目标 + 奖励。 */
    public static QuestDef of(Identifier id, String name, List<QuestObjective> objectives, QuestReward reward) {
        return new QuestDef(id, name, objectives, QuestPrerequisite.EMPTY, reward, 0);
    }
}
