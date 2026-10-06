package com.cimdy.coe.data.codec.progression;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家进度运行时状态（存于附件 {@link AttachmentRegistry#PLAYER_PROGRESS}，同步到客户端、死亡保留）。
 *
 * <p>这是整套进度系统的唯一权威状态：等级（1-100，门控科技树/实体上限/区块预算）、
 * 科技点、研究点、已解锁科技、已完成任务与研究、研究台进行中队列、任务进度、已授权工种、已用区块数。
 * 记录类 + 链式 {@code withXxx} 构造，便于游戏逻辑不可变地更新。
 */
public record PlayerProgress(
        int level, long xp, int techPoints, int researchPoints,
        List<Identifier> unlockedTech, List<Identifier> completedQuests,
        List<Identifier> activeResearch, List<Identifier> completedResearch,
        Map<Identifier, List<Integer>> questProgress,
        List<String> unlockedWorkTypes, int chunksUsed) {

    /** 初始：1 级、零进度。 */
    public static final PlayerProgress EMPTY = new PlayerProgress(1, 0, 0, 0,
            List.of(), List.of(), List.of(), List.of(), Map.of(), List.of(), 0);

    public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("level", 1).forGetter(PlayerProgress::level),
            Codec.LONG.optionalFieldOf("xp", 0L).forGetter(PlayerProgress::xp),
            Codec.INT.optionalFieldOf("tech_points", 0).forGetter(PlayerProgress::techPoints),
            Codec.INT.optionalFieldOf("research_points", 0).forGetter(PlayerProgress::researchPoints),
            Identifier.CODEC.listOf().optionalFieldOf("unlocked_tech", List.of()).forGetter(PlayerProgress::unlockedTech),
            Identifier.CODEC.listOf().optionalFieldOf("completed_quests", List.of()).forGetter(PlayerProgress::completedQuests),
            Identifier.CODEC.listOf().optionalFieldOf("active_research", List.of()).forGetter(PlayerProgress::activeResearch),
            Identifier.CODEC.listOf().optionalFieldOf("completed_research", List.of()).forGetter(PlayerProgress::completedResearch),
            Codec.unboundedMap(Identifier.CODEC, Codec.INT.listOf()).optionalFieldOf("quest_progress", Map.of())
                    .forGetter(PlayerProgress::questProgress),
            Codec.STRING.listOf().optionalFieldOf("unlocked_work_types", List.of()).forGetter(PlayerProgress::unlockedWorkTypes),
            Codec.INT.optionalFieldOf("chunks_used", 0).forGetter(PlayerProgress::chunksUsed)
    ).apply(instance, PlayerProgress::new));

    // ---- 附件存取 ----
    public static PlayerProgress get(Player player) {
        return player.getData(AttachmentRegistry.PLAYER_PROGRESS);
    }

    public static void set(Player player, PlayerProgress p) {
        player.setData(AttachmentRegistry.PLAYER_PROGRESS, p);
    }

    // ---- 链式构造（不可变更新） ----
    public PlayerProgress withLevel(int l) {
        return new PlayerProgress(l, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withTechPoints(int t) {
        return new PlayerProgress(level, xp, t, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withResearchPoints(int r) {
        return new PlayerProgress(level, xp, techPoints, r, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withUnlockedTech(List<Identifier> l) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, l, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withCompletedQuests(List<Identifier> l) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, l,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withActiveResearch(List<Identifier> l) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                l, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withCompletedResearch(List<Identifier> l) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                activeResearch, l, questProgress, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withQuestProgress(Map<Identifier, List<Integer>> m) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, m, unlockedWorkTypes, chunksUsed);
    }

    public PlayerProgress withUnlockedWorkTypes(List<String> l) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, l, chunksUsed);
    }

    public PlayerProgress withChunksUsed(int c) {
        return new PlayerProgress(level, xp, techPoints, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, c);
    }

    // ---- 语义化增量操作（避免调用方到处写 new ArrayList） ----

    public PlayerProgress addTech(Identifier id) {
        if (unlockedTech.contains(id)) return this;
        List<Identifier> l = new ArrayList<>(unlockedTech);
        l.add(id);
        return withUnlockedTech(l);
    }

    public PlayerProgress addCompletedQuest(Identifier id) {
        if (completedQuests.contains(id)) return this;
        List<Identifier> l = new ArrayList<>(completedQuests);
        l.add(id);
        return withCompletedQuests(l);
    }

    public PlayerProgress addActiveResearch(Identifier id) {
        if (activeResearch.contains(id)) return this;
        List<Identifier> l = new ArrayList<>(activeResearch);
        l.add(id);
        return withActiveResearch(l);
    }

    public PlayerProgress removeActiveResearch(Identifier id) {
        List<Identifier> l = new ArrayList<>(activeResearch);
        return l.remove(id) ? withActiveResearch(l) : this;
    }

    public PlayerProgress addCompletedResearch(Identifier id) {
        if (completedResearch.contains(id)) return this;
        List<Identifier> l = new ArrayList<>(completedResearch);
        l.add(id);
        return withCompletedResearch(l);
    }

    public PlayerProgress addWorkType(String type) {
        if (unlockedWorkTypes.contains(type)) return this;
        List<String> l = new ArrayList<>(unlockedWorkTypes);
        l.add(type);
        return withUnlockedWorkTypes(l);
    }

    /** 记录某任务第 index 个目标的进度 +delta，返回更新后的状态。 */
    public PlayerProgress advanceObjective(Identifier quest, int index, int objectiveCount, int delta) {
        Map<Identifier, List<Integer>> map = new HashMap<>(questProgress);
        List<Integer> counters = new ArrayList<>(map.getOrDefault(quest, List.of()));
        while (counters.size() < objectiveCount) counters.add(0);
        counters.set(index, counters.get(index) + delta);
        map.put(quest, counters);
        return withQuestProgress(map);
    }

    /** 读取某任务第 index 个目标的当前进度。 */
    public int objectiveProgress(Identifier quest, int index) {
        List<Integer> counters = questProgress.get(quest);
        return counters == null || index >= counters.size() ? 0 : counters.get(index);
    }

    /**
     * 增加经验并结算升级：跨过 {@link #xpToNext(int)} 时升级、科技点 +1，封顶 100 级。
     * 平缓曲线：每级所需 = 100 + (level-1)*50。
     */
    public PlayerProgress addXp(long amount) {
        long nx = xp + amount;
        int nl = level;
        int nt = techPoints;
        long need = xpToNext(nl);
        while (nx >= need && nl < 100) {
            nx -= need;
            nl++;
            nt++;
            need = xpToNext(nl);
        }
        if (nl >= 100) nx = Math.min(nx, need);
        return new PlayerProgress(nl, nx, nt, researchPoints, unlockedTech, completedQuests,
                activeResearch, completedResearch, questProgress, unlockedWorkTypes, chunksUsed);
    }

    /** 升到下一级所需经验（平缓曲线）。 */
    public static long xpToNext(int level) {
        return 100L + (long) (level - 1) * 50L;
    }
}
