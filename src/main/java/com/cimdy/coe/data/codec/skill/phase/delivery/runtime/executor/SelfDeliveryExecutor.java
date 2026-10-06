package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 自身投递执行器 — 效果直接作用于施法者自身（Buff / 被动）。
 * ON_CAST 动作已在 {@code SkillPhase.initialize} 中触发；此处再以施法者自身为目标
 * 点燃 ON_HIT，使自指向的 ON_HIT 动作（如自身增益 / 减益）也能生效。
 */
public class SelfDeliveryExecutor implements DeliveryExecutor<SelfDelivery> {

    @Override
    public void execute(SelfDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        Vec3 pos = caster.position();
        HitContext hit = HitContext.entityHit(caster, pos, Vec3.ZERO, 0f);
        // 注册阶段实例：使 ON_HIT 执行期间的动作（如 SpawnAction 生成图腾）可 retain 该实例，
        // 让驻场实体在阶段结束后仍能反查并点燃触发器（继承 SupportProfile）。
        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);
        try {
            instance.fireTrigger(LifecycleTrigger.ON_HIT, hit);
        } finally {
            // 对称释放：若无驻场实体 retain，引用计数归零即移除，避免泄漏
            SkillPhaseInstanceTracker.release(casterId, phaseId);
        }
        // 自施反馈：在施法者身上迸发一圈粒子 + 轻响，使「自身增益 / 诅咒 / 标记」类技能有可见 / 可闻反馈
        // （此前 Self 投递零视觉、零音效）。服务端广播，客户端自动渲染，无需自定义位置包。
        if (caster.level() instanceof ServerLevel sl) {
            Vec3 p = caster.position().add(0.0, caster.getBbHeight() * 0.5, 0.0);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 14, 0.4, 0.6, 0.4, 0.05);
            sl.playSound(null, p.x, p.y, p.z, SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.25F, 1.2F);
        }
    }
}
