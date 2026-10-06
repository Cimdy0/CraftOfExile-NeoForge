package com.cimdy.coe.entity.custom.skill.totem;

import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 地雷实体（{@code coe:mine}）—— 复用 {@link SkillTotem} 全部驻场施法行为，
 * 仅作为独立实体类型使命中作用域能按实体身份注入 {@code mine} 标签（见 {@link com.cimdy.coe.data.helper.combat.HitScopeHelper}）。
 *
 * <p>与 {@link TrapEntity} 同属「布防 + 邻近触发」一族，但地雷是<b>一次性 proximity 引爆</b>：
 * 落地后经过 {@link #armDelay} 引信期（不触发），布防后每 tick 在 {@code attackRange} 内发现敌人即
 * 点燃本雷技能阶段的 {@code ON_TICK}（朝该敌人结算 {@code DamageAction}），随后<b>一次性消耗</b>。
 *
 * <p>fail-open 占位：当前与陷阱共享同族邻近引爆语义；后续可接入「手动引爆 / 链式引爆 / 延迟引爆」等专属行为，
 * 不影响既有图腾/箭塔/陷阱通道（类名与实体类型独立，作用域标签专属 {@code mine}）。
 */
public class MineEntity extends SkillTotem {
    /** 引信延迟（tick）：落地后至此之前不触发，给「布防」手感。 */
    private int armDelay = 12;

    public MineEntity(EntityType<? extends MineEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 地雷为「一次性邻近引爆」，不沿用 {@link SkillTotem} 的固定周期索敌——
     * 故把攻击间隔置为超大值，使 {@link SkillTotem#tick()} 的周期开火分支
     * （{@code tickCount % attackInterval == 0}）在其存活期内永不命中，
     * 攻击完全由本类的邻近逻辑驱动一次。其余（存活 / 到期 / 血 / 半径）沿用预设。
     */
    @Override
    public void configure(LivingEntity owner, UUID phaseId,
                          int lifetime, int attackInterval, float attackRange, float health) {
        super.configure(owner, phaseId, lifetime, Integer.MAX_VALUE, attackRange, health);
    }

    @Override
    public void tick() {
        // 复用 SkillTotem 的基底 tick（存活计时 / 到期 ON_EXPIRE / 粒子 / 计数回收），
        // 其周期索敌因 configure 时 attackInterval=∞ 而实际不触发；攻击由下方邻近逻辑一次性驱动。
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        // 已到期 / 被打死：super.tick() 已处理 ON_EXPIRE 与回收，不再二次触发
        if (this.isRemoved()) return;
        // 引信未布防：仅驻场，不触发
        if (this.tickCount < this.armDelay) return;

        // 布防后每 tick 邻近索敌：敌人进入攻击半径即触发一次并一次性消耗
        LivingEntity target = findTarget();
        if (target != null) {
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 dir = targetPos.subtract(this.position());
            float distance = (float) dir.length();
            HitContext hit = HitContext.entityHit(target, targetPos,
                    distance > 1e-4 ? dir.normalize() : Vec3.ZERO, distance);
            // 点燃本地雷技能阶段的 ON_TICK（attacker = 本地雷实体），
            // 命中作用域经 HitScopeHelper 叠加 mine 标签 → 点亮 SINGLE-{mine} 效果
            fireTrigger(LifecycleTrigger.ON_TICK, hit);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    targetPos.x, targetPos.y, targetPos.z, 10, 0.25, 0.25, 0.25, 0.05);
            // 一次性消耗（PoE 默认）：触发即销毁，释放 tracker 引用计数
            SummonCountHelper.onMinionRemoved(this);
            this.discard();
        }
    }
}
