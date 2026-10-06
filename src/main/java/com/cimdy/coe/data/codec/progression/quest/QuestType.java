package com.cimdy.coe.data.codec.progression.quest;

import com.mojang.serialization.Codec;

import java.util.Locale;

/**
 * 任务目标类型（玩家视角 objective）。
 * 与 stronghold 内部 {@code Task} 完全独立——这里驱动玩家升级与奖励。
 */
public enum QuestType {
    KILL,        // 击杀指定实体
    GATHER,      // 采集/拾取指定物品
    CRAFT,       // 合成指定物品
    REACH,       // 抵达指定维度/结构
    BUILD,       // 放置指定方块
    BIND_CHUNK;  // 绑定一个基地区块

    /** JSON 用小写，代码用大写枚举，互转时统一归一。 */
    public static final Codec<QuestType> CODEC = Codec.STRING.xmap(
            s -> QuestType.valueOf(s.toUpperCase(Locale.ROOT)),
            e -> e.name().toLowerCase(Locale.ROOT));
}
