package com.cimdy.coe.data.codec.progression;

import com.cimdy.coe.data.codec.progression.JobCharterDef;
import com.cimdy.coe.data.codec.progression.research.EfficiencyBonus;
import com.cimdy.coe.data.codec.progression.research.ResearchEffect;
import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.data.codec.progression.research.WorkerPolicy;
import com.cimdy.coe.data.codec.progression.TechEffect;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 进度系统服务层：把「数据定义（{@link ProgressionData}）」与「玩家状态（{@link PlayerProgress}）」
 * 结合成可查询的实际能力上限与解锁判定。
 *
 * <p>所有门控（区块预算 / 实体上限 / 站点上限 / 工种授权 / 研究效率）都从这里取值，
 * 保证唯一事实来源；上层（界桩、终端 UI、工人 Goal、网络包）只调用本类，不自行推算。
 */
public final class ProgressionService {

    /** 1 级玩家的初始配额：够搭一条最小生产链，但不够铺开——第一次取舍就在这里发生。 */
    public static final int BASE_CHUNK_BUDGET = 3;
    public static final int BASE_ENTITY_CAP = 3;
    public static final int BASE_SITE_CAP = 2;

    /** 等级派生：每 10 级白送 1 区块 / 1 工人名额，保证不做科技也有缓慢成长。 */
    private static final int LEVEL_PER_CHUNK = 10;
    private static final int LEVEL_PER_ENTITY = 10;
    /** 站点上限成长得更慢——站点是「铺开」的开关，放太快会让据点一步到位失去经营节奏。 */
    private static final int LEVEL_PER_SITE = 20;

    private ProgressionService() {
    }

    // ==================== 科技效果聚合 ====================

    /** 把玩家已解锁的全部科技节点效果累加成一个总效果。 */
    public static TechEffect aggregateTech(PlayerProgress progress) {
        TechEffect total = TechEffect.EMPTY;
        for (Identifier id : progress.unlockedTech()) {
            TechNodeDef def = ProgressionData.TECH_NODES.get(id);
            if (def != null) total = total.merge(def.effect());
        }
        return total;
    }

    /** 把玩家已完成的全部研究节点效果累加成一个总效果。 */
    public static ResearchEffect aggregateResearch(PlayerProgress progress) {
        ResearchEffect total = ResearchEffect.EMPTY;
        for (Identifier id : progress.completedResearch()) {
            ResearchNodeDef def = ProgressionData.RESEARCH_NODES.get(id);
            if (def != null) total = total.merge(def.effect());
        }
        return total;
    }

    // ==================== 能力上限 ====================

    /** 玩家可绑定的基地区块总数。 */
    public static int chunkBudget(PlayerProgress p) {
        return BASE_CHUNK_BUDGET + p.level() / LEVEL_PER_CHUNK + aggregateTech(p).chunkBudgetAdd();
    }

    public static int chunkBudget(Player player) {
        return chunkBudget(PlayerProgress.get(player));
    }

    /** 玩家可在据点安置的工人上限。 */
    public static int entityCap(PlayerProgress p) {
        return BASE_ENTITY_CAP + p.level() / LEVEL_PER_ENTITY + aggregateTech(p).entityCapAdd();
    }

    public static int entityCap(Player player) {
        return entityCap(PlayerProgress.get(player));
    }

    /** 玩家可设置的工作站点上限（与区块 / 工人同构：基础 + 等级派生 + 科技加成）。 */
    public static int siteCap(PlayerProgress p) {
        return BASE_SITE_CAP + p.level() / LEVEL_PER_SITE + aggregateTech(p).siteCapAdd();
    }

    public static int siteCap(Player player) {
        return siteCap(PlayerProgress.get(player));
    }

    /** 剩余可绑定区块数。 */
    public static int remainingChunks(PlayerProgress p) {
        return chunkBudget(p) - p.chunksUsed();
    }

    // ==================== 工种授权 ====================

    /**
     * 工种是否已对该玩家开放。
     *
     * <p>判定顺序：① 数据包里存在对应 {@link JobCharterDef} 授权书时，按其 {@code min_level}
     * 与 {@code requires_tech} 判定；② 无授权书时回退到「科技效果里是否解锁过该工种」；
     * ③ 都没有则视为<b>默认开放</b>——保证未配置数据包的整合包不会被锁死。
     */
    public static boolean isWorkTypeUnlocked(PlayerProgress p, String workType) {
        JobCharterDef charter = findCharter(workType);
        if (charter == null) {
            // ② 无授权书：若有科技节点点名过该工种，就以「是否解锁过」为准；
            //    ③ 无人点名则默认开放，避免未配置数据包的整合包被锁死。
            return !TechGate.isWorkTypeDeclared(workType) || p.unlockedWorkTypes().contains(workType);
        }
        if (p.level() < charter.minLevel()) return false;
        for (Identifier tech : charter.requiresTech())
            if (!p.unlockedTech().contains(tech)) return false;
        // 需要付费解锁的工种，还要求玩家显式解锁过
        if (charter.unlockCost() > 0 && !p.unlockedWorkTypes().contains(workType)) return false;
        return true;
    }

    public static boolean isWorkTypeUnlocked(Player player, String workType) {
        return isWorkTypeUnlocked(PlayerProgress.get(player), workType);
    }

    /** 按工种名查授权书。 */
    public static JobCharterDef findCharter(String workType) {
        for (JobCharterDef def : ProgressionData.JOB_CHARTERS.values())
            if (def.workType().equals(workType)) return def;
        return null;
    }

    // ==================== 研究增益 ====================

    /** 某工种的效率倍率（多个研究节点的加成相乘）。 */
    public static float efficiencyFor(PlayerProgress p, String workType) {
        float mult = 1.0f;
        for (EfficiencyBonus bonus : aggregateResearch(p).efficiency())
            if (bonus.workType().equals(workType) || bonus.workType().equals("*"))
                mult *= bonus.multiplier();
        return mult;
    }

    public static float efficiencyFor(Player player, String workType) {
        return efficiencyFor(PlayerProgress.get(player), workType);
    }

    // ==================== 科技解锁 ====================

    /** 科技节点是否满足解锁条件（等级达标 + 科技点足够 + 未解锁过）。 */
    public static boolean canUnlockTech(PlayerProgress p, TechNodeDef def) {
        return def != null
                && !p.unlockedTech().contains(def.id())
                && p.level() >= def.minLevel()
                && p.techPoints() >= def.cost();
    }

    /**
     * 解锁一个科技节点：扣科技点、写入已解锁列表、把效果里的工种授权同步进玩家状态。
     *
     * @return 是否解锁成功
     */
    public static boolean unlockTech(Player player, Identifier techId) {
        TechNodeDef def = ProgressionData.TECH_NODES.get(techId);
        PlayerProgress p = PlayerProgress.get(player);
        if (!canUnlockTech(p, def)) return false;

        PlayerProgress next = p.withTechPoints(p.techPoints() - def.cost()).addTech(techId);
        for (String wt : def.effect().unlockWorkTypes()) next = next.addWorkType(wt);
        PlayerProgress.set(player, next);

        player.sendOverlayMessage(Component.translatable("tech.coe.unlocked",
                def.name().isEmpty() ? techId.toString() : def.name()));
        return true;
    }

    // ==================== 研究解锁 ====================

    /**
     * 研究节点是否可以开始（科技已放行 + 前置研究全部完成 + 研究点足够 + 未完成也未在研究中）。
     *
     * <p>{@link TechGate#isResearchUnlocked} 是科技树通向研究树的唯一联动通道：
     * 被 {@code unlock_research} 点名的研究，在对应科技解锁前一律不可开始。
     */
    public static boolean canStartResearch(PlayerProgress p, ResearchNodeDef def) {
        if (def == null) return false;
        if (!TechGate.isResearchUnlocked(p, def.id())) return false;
        if (p.completedResearch().contains(def.id()) || p.activeResearch().contains(def.id())) return false;
        if (p.researchPoints() < def.cost()) return false;
        for (Identifier pre : def.prerequisites())
            if (!p.completedResearch().contains(pre)) return false;
        return true;
    }

    /** 扣点并标记研究进行中（实际计时由研究台方块实体推进）。 */
    public static boolean beginResearch(Player player, Identifier researchId) {
        ResearchNodeDef def = ProgressionData.RESEARCH_NODES.get(researchId);
        PlayerProgress p = PlayerProgress.get(player);
        if (!canStartResearch(p, def)) return false;
        PlayerProgress.set(player, p.withResearchPoints(p.researchPoints() - def.cost())
                .addActiveResearch(researchId));
        return true;
    }

    /** 研究台计时结束：移出进行中、写入已完成。 */
    public static void completeResearch(Player player, Identifier researchId) {
        PlayerProgress p = PlayerProgress.get(player);
        PlayerProgress.set(player, p.removeActiveResearch(researchId).addCompletedResearch(researchId));
        ResearchNodeDef def = ProgressionData.RESEARCH_NODES.get(researchId);
        player.sendSystemMessage(Component.translatable("research.coe.completed",
                def == null || def.name().isEmpty() ? researchId.toString() : def.name()));
    }

}
