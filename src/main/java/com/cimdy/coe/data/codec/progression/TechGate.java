package com.cimdy.coe.data.codec.progression;

import com.cimdy.coe.data.codec.progression.TechEffect;
import com.cimdy.coe.data.codec.progression.TechNodeDef;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * 科技门控查询层：把 {@link TechEffect} 里那些「解锁某某」的列表字段，
 * 统一翻译成一句可复用的判定——<b>声明即门控</b>。
 *
 * <p>三级语义（与 {@link ProgressionService#isWorkTypeUnlocked} 完全一致，避免两套规则）：
 * <ol>
 *   <li>该资源被<b>任一</b>科技节点声明过，且玩家解锁了其中<b>任一</b>节点 → 开放；</li>
 *   <li>该资源被声明过，但玩家一个都没解锁 → <b>锁定</b>；</li>
 *   <li><b>没有任何</b>科技节点声明过它 → 开放。</li>
 * </ol>
 *
 * <p>第 ③ 条是关键：它保证不配置 {@code tech_node} 数据包的整合包不会被锁死，
 * 整合包作者只要在 JSON 里写一句 {@code "unlock_blocks": [...]}，门控立刻生效，零代码。
 */
public final class TechGate {

    private TechGate() {
    }

    /**
     * 通用判定：按 {@code selector} 取出各科技节点声明的资源列表，判断 {@code value} 是否对玩家开放。
     *
     * @param selector 从 {@link TechEffect} 中取出待检查的列表，如 {@code TechEffect::unlockBlocks}
     */
    private static <T> boolean allows(PlayerProgress p,
                                      Function<TechEffect, ? extends Collection<T>> selector,
                                      T value) {
        boolean declared = false;
        for (Map.Entry<Identifier, TechNodeDef> e : ProgressionData.TECH_NODES.entrySet()) {
            TechNodeDef def = e.getValue();
            if (def == null || !selector.apply(def.effect()).contains(value)) continue;
            declared = true;
            // 多个科技节点可解锁同一资源，任一解锁即放行
            if (p.unlockedTech().contains(e.getKey())) return true;
        }
        return !declared;
    }

    /** 是否有任一科技节点声明过该资源（用于区分「被锁」与「压根不受管」）。 */
    private static <T> boolean declared(Function<TechEffect, ? extends Collection<T>> selector, T value) {
        for (TechNodeDef def : ProgressionData.TECH_NODES.values())
            if (def != null && selector.apply(def.effect()).contains(value)) return true;
        return false;
    }

    // ==================== 工种 ====================

    /** 是否有科技节点声明会解锁该工种（供 {@link ProgressionService#isWorkTypeUnlocked} 的二级回退使用）。 */
    public static boolean isWorkTypeDeclared(String workType) {
        return declared(TechEffect::unlockWorkTypes, workType);
    }

    // ==================== 方块 / 物品 ====================

    /** 该方块是否被科技锁住（未解锁 → 禁止放置）。 */
    public static boolean isBlockLocked(PlayerProgress p, Identifier blockId) {
        return !allows(p, TechEffect::unlockBlocks, blockId);
    }

    public static boolean isBlockLocked(Player player, Identifier blockId) {
        return isBlockLocked(PlayerProgress.get(player), blockId);
    }

    /** 该物品是否被科技锁住（未解锁 → 禁止使用）。 */
    public static boolean isItemLocked(PlayerProgress p, Identifier itemId) {
        return !allows(p, TechEffect::unlockItems, itemId);
    }

    public static boolean isItemLocked(Player player, Identifier itemId) {
        return isItemLocked(PlayerProgress.get(player), itemId);
    }

    // ==================== 跨树联动：科技 → 研究 / 任务 ====================

    /**
     * 该研究节点是否已被科技解锁。
     *
     * <p>这是科技树通向研究树的<b>唯一</b>联动通道：科技节点用 {@code unlock_research}
     * 点名某个研究，该研究在对应科技解锁前不可开始（即便前置研究已完成、研究点足够）。
     */
    public static boolean isResearchUnlocked(PlayerProgress p, Identifier researchId) {
        return allows(p, TechEffect::unlockResearch, researchId);
    }

    /** 该任务是否已被科技解锁（未被任何科技声明的任务不受管）。 */
    public static boolean isQuestUnlocked(PlayerProgress p, Identifier questId) {
        return allows(p, TechEffect::unlockQuests, questId);
    }

    public static boolean isQuestUnlocked(Player player, Identifier questId) {
        return isQuestUnlocked(PlayerProgress.get(player), questId);
    }

}
