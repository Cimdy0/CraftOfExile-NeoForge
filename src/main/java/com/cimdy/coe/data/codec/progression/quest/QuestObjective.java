package com.cimdy.coe.data.codec.progression.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * 单个任务目标。{@link #target()} 语义随 {@link #type()} 变化：
 * KILL→实体 id；GATHER/CRAFT→物品 id；BUILD→方块 id；REACH→维度/结构 id；BIND_CHUNK→忽略。
 */
public record QuestObjective(QuestType type, Identifier target, int count) {

    public static final Codec<QuestObjective> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            QuestType.CODEC.fieldOf("type").forGetter(QuestObjective::type),
            Identifier.CODEC.fieldOf("target").forGetter(QuestObjective::target),
            Codec.INT.optionalFieldOf("count", 1).forGetter(QuestObjective::count)
    ).apply(instance, QuestObjective::new));

    /** 带显式数量。 */
    public static QuestObjective of(QuestType type, Identifier target, int count) {
        return new QuestObjective(type, target, count);
    }

    /** 数量默认为 1。 */
    public static QuestObjective of(QuestType type, Identifier target) {
        return new QuestObjective(type, target, 1);
    }
}
