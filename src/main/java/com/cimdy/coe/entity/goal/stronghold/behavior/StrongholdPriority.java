package com.cimdy.coe.entity.goal.stronghold.behavior;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.event.combat.TargetEvent;

/**
 * 语义化行为优先级常量，替代 int 魔法数字。
 * <p>
 * 优先级数值越小越先被加入目标选择器。
 * 新增自定义优先级可直接使用 int 值（如 {@code 1} 表示介于 EAT 和 WORK 之间）。
 */
public final class StrongholdPriority {
    private StrongholdPriority() {}

    /** 进食（最高优先级，生存需求）。 */
    public static final int EAT    = 0;
    /** 据点防御（阵营入侵时主动出击，优先级高于工作 / 闲逛，让位于进食）。 */
    public static final int DEFEND = 1;
    /**
     * 召唤物 / 盟友的阵营防御优先级：刻意避开 {@link #DEFEND}(1)，因为 {@code TargetEvent} 已在实体入界时
     * 用优先级 1 注入 {@code MobSkillCastGoal}；若同占 1 会被 {@code GoalSelector} 的 TreeSet 去重丢弃其一。
     * 召唤物 AI 在 {@code SpawnAction.spawnMinion} 中清空原版目标后重建，故 2 槽位空闲、稳定生效。
     */
    public static final int MINION_DEFEND = 2;
    /** 搬运任务。 */
    public static final int CARRY  = 2;
    /** 工作类（采矿/农耕/林业等）。推荐新增工种使用此值。 */
    public static final int WORK   = 3;
    /** 闲逛/社交（兜底行为）。 */
    public static final int WANDER = 4;
}
