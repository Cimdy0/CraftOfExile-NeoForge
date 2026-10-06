package com.cimdy.coe.entity.goal;

import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * 据点实体的阵营防御：当据点范围内出现「阵营非 NONE 且 ≠ 自身阵营」的入侵实体时主动出击，
 * 平时 {@code canUse} 不成立、不抢占工作 / 随行行为。
 *
 * <p>与 {@code CampAllianceMixin}（基类 {@code canAttack} 门控）双保险：只打敌对阵营、绝不误伤
 * 同阵营 / 中立实体。防御是「增量叠加」而非「替换原 AI」——工人仍跑据点行为、盟友仍随行，
 * 仅在据点遭入侵时临时接管战斗，呼应「据点实体仍需要战斗、不是彻底分离」的设计意图。
 *
 * <p>经 {@code WorkerAIInjector} / 盟友分支按 {@link AdditionKeys#COMBATANT} 条件注入；
 * 设为 false 的实体（专注工作）不获得此防御行为，即帕鲁式「工作 ↔ 战斗」开关。
 */
public class FactionDefendGoal extends Goal {
    /** 防御索敌半径（格）：取原版 follow_range 默认 16，与历史 FactionTargetGoal 一致。 */
    private static final double SEARCH_RANGE = 16.0;
    /** 基础攻击间隔（tick）：对齐原版 MeleeAttackGoal 默认 attackInterval=20（等价于 1 次/秒基准）。 */
    private static final int BASE_ATTACK_INTERVAL = 20;
    /** 攻击速度下限保护：避免 ATTACK_SPEED 为 0/缺失导致冷却为无穷大或除零。 */
    private static final double MIN_ATTACK_SPEED = 0.01;

    private final Mob mob;
    private LivingEntity target;
    /** 近战攻击冷却计时（tick），由实体攻速缩放。 */
    private int attackTimer = 0;

    public FactionDefendGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        Camp selfCamp = mob.getData(AttachmentRegistry.CAMP);
        if (selfCamp == Camp.NONE) return false;
        LivingEntity best = null;
        double bestDist = -1.0;
        AABB box = mob.getBoundingBox().inflate(SEARCH_RANGE);
        for (Entity e : mob.level().getEntities(mob, box)) {
            if (!(e instanceof LivingEntity le) || e == mob) continue;
            Camp c = le.getData(AttachmentRegistry.CAMP);
            if (c == Camp.NONE || c == selfCamp) continue;   // 中立或同阵营不打
            if (!mob.canAttack(le)) continue;                 // 双保险（阵营门控）
            double d = mob.distanceToSqr(le);
            if (best == null || d < bestDist) {
                best = le;
                bestDist = d;
            }
        }
        if (best == null) return false;
        this.target = best;
        return true;
    }

    @Override
    public void start() {
        if (this.target != null) this.mob.setTarget(this.target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive()
                && this.target.level() == this.mob.level()
                && this.mob.getTarget() == this.target
                && this.mob.distanceToSqr(this.target) <= SEARCH_RANGE * SEARCH_RANGE * 4.0;
    }

    @Override
    public void tick() {
        if (this.target == null || !this.target.isAlive()) return;
        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double distSq = this.mob.distanceToSqr(this.target);
        if (distSq > SEARCH_RANGE * SEARCH_RANGE) {
            this.mob.getNavigation().moveTo(this.target, 1.0);
            return;
        }
        this.mob.getNavigation().stop();
        if (this.attackTimer > 0) {
            this.attackTimer--;
            return;
        }
        // 攻速缩放：复用原版 ATTACK_SPEED（CoE 词缀/天赋/形态/随从/召唤物均写入此属性，
        // 原版近战/远程攻击 Goal 已通过 Mixin 同款公式受益）。本自定义近战据此缩放冷却，
        // 使据点防御者真正吃到攻速词条，而非依赖目标无敌帧（那会让攻速恒无效）。
        double speed = this.mob.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)
                ? Math.max(MIN_ATTACK_SPEED, this.mob.getAttributeValue(Attributes.ATTACK_SPEED))
                : 4.0;
        this.attackTimer = Math.max(1, (int) (BASE_ATTACK_INTERVAL / speed));
        this.mob.doHurtTarget((ServerLevel) this.mob.level(), this.target);
    }

    @Override
    public void stop() {
        if (this.mob.getTarget() == this.target) this.mob.setTarget(null);
        this.target = null;
    }
}
