package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.WaveDelivery;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Wave 投递执行器 —— 沿施法者朝向发射一道<b>向前推进的球形波前</b>。
 *
 * <p>复用 {@link PersistentDeliveryManager} 的逐 tick 推进框架（与 Beam 同级）：注册一个 Entry，
 * 每 tick 由 {@code computeTargets} 取 {@code front = origin + direction × (tickCounter × speed)}，
 * 以 {@code front} 为球心、{@code radius} 为半径扫描，仅命中<b>尚未命中过</b>的实体（{@code alreadyHit} 去重集，
 * 保证每个实体仅被命中一次 —— 即「波前扫过即生效、不重复」的语义）。到 {@code range} 尽头（remaining 归零）自动消散。
 *
 * <p><b>客户端同步</b>：波前视觉 / 音效完全由服务端在 {@code PersistentDeliveryManager} 的 wave 分支
 * 每 tick 经 {@code ServerLevel.sendParticles} / {@code playSound} 广播，客户端自动出现，
 * 无需任何自定义位置包 —— 与 Orb / Aura 同范式（服务端权威、客户端零同步负担）。
 *
 * <p><b>效果不在本层定义</b>：波前命中后的伤害 / 状态 / 事件全部由技能阶段自身的 {@code actions}
 * 经 {@code ON_HIT} 触发，本执行器只负责「波从哪出发、推多远、多快、多厚」。
 */
public class WaveDeliveryExecutor implements DeliveryExecutor<WaveDelivery> {

    @Override
    public void execute(WaveDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction() : caster.getLookAngle()).normalize();

        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);

        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        // origin：优先用 delivery 覆盖（标量值解释为沿发射方向前移的格数）；否则回退眼睛位置（原行为）。
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);
        double range = delivery.range().getFloat(skillCtx);
        double speed = Math.max(0.01, delivery.speed().getFloat(skillCtx));
        // 波前推进总 tick 数 = 距离 / 速度，到点即消散（同 Beam 的 remaining 语义）
        int remaining = (int) Math.ceil(range / speed);

        PersistentDeliveryManager.register(new PersistentDeliveryManager.Entry(
                casterId, phaseId, instance, delivery, origin, direction,
                1, remaining, level.dimension(), SupportProfile.from(context), speed));
    }
}
