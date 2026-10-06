package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area;

import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BeamDelivery;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * 光束执行器 — 沿施法者朝向发射一束能量，在 duration tick 内每 tick 命中光束范围内敌人。
 * 瞬发光束（duration=0）注册为 1 tick 后自动移除。
 */
public class BeamDeliveryExecutor implements DeliveryExecutor<BeamDelivery> {

    @Override
    public void execute(BeamDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof Entity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();

        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);

        // 「Skill Effect Duration」统一落到 AILMENT_DURATION（StatEffectIds 设计注释）；
        // 以 {AILMENT, !TAKEN} 作用域读取，使 inc_skill_effect_duration 族（含 freeze per-enemy）生效。
        // 时长求值带技能上下文：否则通用光束预设的 $duration 参数槽读不到（$槽死线）。
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;
        // origin：优先用 delivery 覆盖（标量值解释为沿发射方向前移的格数）；否则回退眼睛位置（原行为）。
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);
        int baseDuration = (int) delivery.duration().getFloat(skillCtx);
        // 「Skill Effect Duration」统一落到 AILMENT_DURATION；另叠首类「法术时长」SPELL_DURATION_MULT
        // （{spell,storm}/{spell,plant} 的 X% less Duration 在此真实缩短光束时长，作用域已并入技能标签）。
        double skillEffectDurationMult = 1.0;
        double spellDurationMult = 1.0;
        if (caster instanceof LivingEntity lc) {
            skillEffectDurationMult = AilmentHelper.withScope(lc, Set.of(GateTag.AILMENT, GateTag.not(GateTag.TAKEN)), AttributeRegistry.GROUND_DURATION);
            spellDurationMult = AilmentHelper.spellDurationMultiplier(lc);
        }
        int remaining = baseDuration <= 0 ? 1 : (int) (baseDuration * skillEffectDurationMult * spellDurationMult);
        PersistentDeliveryManager.register(new PersistentDeliveryManager.Entry(
                casterId, phaseId, instance, delivery, origin, direction,
                1, remaining, level.dimension(), SupportProfile.from(context), 0.0));
    }
}
