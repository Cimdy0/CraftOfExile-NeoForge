package com.cimdy.coe.entity.goal;

import com.cimdy.coe.event.combat.TargetEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

/**
 * 嘲讽（Taunt）：当怪物持有 {@link StatusDefs#TAUNT} 状态时，强制将其攻击目标锁定为嘲讽施加者
 * （{@link StatusInstance#source} 记录的 UUID，经 {@code Level#getEntity} 还原）。
 *
 * <p>经 {@code TargetEvent#addTargetGoalEvent} 以最高优先级（targetSelector 槽位 0）注入所有 Mob，
 * 覆盖原版索敌逻辑；taunt 状态消失后 {@link #canUse()} 不成立，怪物退回原版目标选择。
 * 与 {@code CampAllianceMixin}（canAttack 阵营门控）双保险——若嘲讽者同阵营，仍受 canAttack 约束不误伤友方。
 *
 * <p>嘲讽的「施加」由技能 / 词缀数据驱动（施加 taunt 状态并写入 source=施加者），本 Goal 仅负责消费。
 */
public class TauntTargetGoal extends Goal {
    private final Mob mob;
    private LivingEntity taunter;

    public TauntTargetGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        StatusStore store = mob.getData(AttachmentRegistry.STATUS_STORE);
        if (!store.has(StatusDefs.TAUNT)) return false;
        StatusInstance inst = store.get(StatusDefs.TAUNT);
        if (inst == null || inst.source == null) return false;
        Entity e = mob.level().getEntity(inst.source);
        if (!(e instanceof LivingEntity le) || e == mob) return false;
        if (!le.isAlive() || !mob.canAttack(le)) return false;
        this.taunter = le;
        return true;
    }

    @Override
    public void start() {
        if (this.taunter != null) this.mob.setTarget(this.taunter);
    }

    @Override
    public boolean canContinueToUse() {
        return this.taunter != null && this.taunter.isAlive()
                && this.mob.getTarget() == this.taunter
                && mob.getData(AttachmentRegistry.STATUS_STORE).has(StatusDefs.TAUNT);
    }

    @Override
    public void stop() {
        if (this.mob.getTarget() == this.taunter) this.mob.setTarget(null);
        this.taunter = null;
    }
}
