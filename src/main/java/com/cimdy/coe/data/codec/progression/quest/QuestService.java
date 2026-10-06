package com.cimdy.coe.data.codec.progression.quest;

import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import com.cimdy.coe.event.listener.QuestProgressListener;

/**
 * 玩家任务服务：事件推进 → 目标计数 → 完成结算 → 发奖励。
 *
 * <p>唯一入口是 {@link #onEvent}，由 {@code QuestProgressListener} 在击杀 / 采集 / 合成 /
 * 建造 / 绑定区块等时机调用。任务无需「接取」——只要前置满足就自动激活并计数，
 * 避免玩家在冒险途中还要回城点任务板，符合「专心冒险」的设计目标。
 */
public final class QuestService {

    private QuestService() {
    }

    /**
     * 推进所有活跃任务中与该事件匹配的目标。
     *
     * @param type   事件类型
     * @param target 事件对象 id（实体 / 物品 / 方块 / 维度）
     * @param amount 增量
     */
    public static void onEvent(Player player, QuestType type, Identifier target, int amount) {
        if (player.level().isClientSide() || amount <= 0) return;

        PlayerProgress progress = PlayerProgress.get(player);
        boolean changed = false;
        List<QuestDef> justCompleted = new ArrayList<>();

        for (QuestDef quest : ProgressionData.QUESTS.values()) {
            if (quest.id() == null) continue;
            if (!isActive(progress, quest)) continue;

            List<QuestObjective> objectives = quest.objectives();
            boolean touched = false;
            for (int i = 0; i < objectives.size(); i++) {
                QuestObjective obj = objectives.get(i);
                if (obj.type() != type || !obj.target().equals(target)) continue;
                if (progress.objectiveProgress(quest.id(), i) >= obj.count()) continue;
                progress = progress.advanceObjective(quest.id(), i, objectives.size(), amount);
                touched = true;
            }

            if (touched) {
                changed = true;
                if (isFulfilled(progress, quest)) justCompleted.add(quest);
            }
        }

        if (!changed) return;

        for (QuestDef quest : justCompleted) {
            progress = progress.addCompletedQuest(quest.id());
            progress = applyReward(progress, quest.reward());
            announce(player, quest);
        }
        PlayerProgress.set(player, progress);
    }

    /** 任务是否处于活跃状态：未完成、且前置（等级 / 前置任务 / 前置科技）全部满足。 */
    public static boolean isActive(PlayerProgress p, QuestDef quest) {
        if (quest.id() == null || quest.objectives().isEmpty()) return false;
        if (p.completedQuests().contains(quest.id())) return false;
        QuestPrerequisite pre = quest.prereq();
        if (p.level() < pre.minLevel()) return false;
        for (Identifier q : pre.quests()) if (!p.completedQuests().contains(q)) return false;
        for (Identifier t : pre.tech()) if (!p.unlockedTech().contains(t)) return false;
        return true;
    }

    /** 所有目标是否都已达标。 */
    public static boolean isFulfilled(PlayerProgress p, QuestDef quest) {
        List<QuestObjective> objectives = quest.objectives();
        for (int i = 0; i < objectives.size(); i++)
            if (p.objectiveProgress(quest.id(), i) < objectives.get(i).count()) return false;
        return true;
    }

    /** 发放奖励：经验（驱动升级）、科技点、研究点、直接解锁的科技。 */
    public static PlayerProgress applyReward(PlayerProgress p, QuestReward reward) {
        PlayerProgress next = p;
        if (reward.xp() > 0) next = next.addXp(reward.xp());
        if (reward.techPoints() > 0) next = next.withTechPoints(next.techPoints() + reward.techPoints());
        if (reward.researchPoints() > 0)
            next = next.withResearchPoints(next.researchPoints() + reward.researchPoints());
        for (Identifier tech : reward.unlockTech()) next = next.addTech(tech);
        return next;
    }

    /**
     * 由伤害来源实体解析出「应记功的玩家」。
     *
     * <p>直接命中玩家最简单；若来源是召唤物 / 弹射物的拥有者是玩家，同样记在玩家头上——
     * 否则一个法系或召唤流玩家永远做不完击杀任务。
     */
    public static Player resolveKiller(Entity source) {
        if (source instanceof Player player) return player;
        if (source instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player owner) return owner;
        return null;
    }

    private static void announce(Player player, QuestDef quest) {
        String name = quest.name().isEmpty() ? quest.id().toString() : quest.name();
        player.sendSystemMessage(Component.translatable("quest.coe.completed", name)
                .withStyle(ChatFormatting.GOLD));
    }
}
