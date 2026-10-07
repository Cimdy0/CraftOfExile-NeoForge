package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.LingeringAreaTracker;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.LingeringDuration;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.entity.EntitySpawns;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 统一范围执行器 —— 按 {@link AreaDelivery#geometry()} 分流到五套运行时行为（D4丙 对称归并）：
 * <ul>
 *   <li>{@code AREA}：在目标位置瞬时扫描命中（可叠加留驻区域）；</li>
 *   <li>{@code HAZARD}：生成驻场危害物实体，周期触发命中；</li>
 *   <li>{@code REMNANT}：生成可拾取残片实体；</li>
 *   <li>{@code BEAM}：沿施法者朝向发射一束能量，持续命中；</li>
 *   <li>{@code WAVE}：向前推进的球形波前，沿途每实体仅命中一次。</li>
 * </ul>
 */
public class AreaDeliveryExecutor implements DeliveryExecutor<AreaDelivery> {

    @Override
    public void execute(AreaDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        switch (delivery.geometry()) {
            case AREA -> executeArea(delivery, context, casterId, phaseId, instance);
            case HAZARD -> executeHazard(delivery, context, casterId, phaseId, instance);
            case REMNANT -> executeRemnant(delivery, context, casterId, phaseId, instance);
            case BEAM -> executeBeam(delivery, context, casterId, phaseId, instance);
            case WAVE -> executeWave(delivery, context, casterId, phaseId, instance);
        }
    }

    // ───────────────────────── AREA ─────────────────────────

    private void executeArea(AreaDelivery delivery, ActionContext context,
                             UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof Entity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        // Nova：以施法者为中心释放，而非以目标位置为中心
        Vec3 center = profile.nova(skillCtx) ? caster.position() : (
                context.position() != null && context.position() != Vec3.ZERO
                        ? context.position() : caster.position());

        // 应用辅助宝石的范围半径倍率（Increased / Concentrated Area of Effect）
        double baseRadius = delivery.shape().sampleRadius(skillCtx) * profile.radiusMultiplier("area", skillCtx);
        // EFFECT_AREA（area_of_effect，基 1.0）：范围效果倍率（FOLD-KEEP，{AREA,MELEE,MINION} 作用域聚合）；
        // 读施法者属性缩放投递半径，对齐 POE2「范围效果」全局倍率。
        double effectArea = readEffectArea(caster instanceof LivingEntity le ? le : null);
        // 回响专属范围载荷（EchoMod → SkillPhaseInstance，仅重放的那一次携带，原施放为 0.0 / 1.0）：
        //   inc  —— POE2「Echoes have 40% increased Area of Effect」，increased = 加法，
        //           与 EFFECT_AREA 相加后再乘半径，故与其他 increased 来源正确叠加（而非各自相乘）；
        //   more —— POE2「100% more AoE per time they have Echoed」，more = 乘法，按回响序号递增。
        if (instance != null) effectArea += instance.echoAreaInc();
        double scaledRadius = (effectArea > 0 && effectArea != 1.0) ? baseRadius * effectArea : baseRadius;
        // 一次算定后赋 final：下方 additional-targets 的 lambda 会捕获 radius，非 effectively final 不编译。
        final double radius = instance != null ? scaledRadius * instance.echoAreaMore() : scaledRadius;

        // 获取实体统一经 Context 数据基站回调（EntityQuery 球形扫描）；命中分发（获取数值侧）留在本执行器
        ExpressionContext base = ExpressionContext.of(level, caster);
        List<LivingEntity> targets = base.queryEntities(
                EntityQuery.sphere(center, radius).exclude(caster).aliveOnly());

        for (LivingEntity target : targets) {
            HitContext hit = HitContext.entityHit(target,
                    target.position(),
                    center.subtract(target.position()).normalize(),
                    (float) center.distanceTo(target.position()));
            instance.fireTrigger(LifecycleTrigger.ON_HIT, hit);
        }

        // ---- 客户端同步反馈（服务端广播，零同步负担）----
        spawnAreaBlast(level, center, radius);
        if (!targets.isEmpty()) {
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.7F, 0.8F);
        }

        // 额外目标（Additional Targets）：在范围外、扩展搜索半径内，对最近的 N 个敌人也点燃 ON_HIT。
        int additional = profile.additionalTargets(skillCtx);
        if (additional > 0) {
            double searchR = radius * 3;
            List<LivingEntity> outside = base.queryEntities(
                    EntityQuery.sphere(center, searchR).exclude(caster).aliveOnly()
                            .filter(e -> e.position().distanceTo(center) > radius
                                    && !targets.contains(e))
                            .nearestTo(center).limit(additional));
            for (LivingEntity t : outside) {
                HitContext hit = HitContext.entityHit(t, t.position(),
                        center.subtract(t.position()).normalize(),
                        (float) center.distanceTo(t.position()));
                instance.fireTrigger(LifecycleTrigger.ON_HIT, hit);
            }
        }

        // Cascade：首次命中后，在每个命中目标位置以缩小半径再次触发（不无限递归）
        int cascade = profile.cascadeCount(skillCtx);
        if (cascade > 0) {
            double cr = radius * 0.5;
            for (LivingEntity target : targets) {
                Vec3 c = target.position();
                List<LivingEntity> cTargets = base.queryEntities(
                        EntityQuery.sphere(c, cr).exclude(caster).aliveOnly()
                                .filter(e -> !targets.contains(e)));
                for (LivingEntity ct : cTargets) {
                    HitContext chit = HitContext.entityHit(ct,
                            ct.position(),
                            c.subtract(ct.position()).normalize(),
                            (float) c.distanceTo(ct.position()));
                    instance.fireTrigger(LifecycleTrigger.ON_HIT, chit);
                }
            }
        }

        // Lingering 持续时间模式：首脉冲已由上方即时扫描完成，后续脉冲登记到 LingeringAreaTracker。
        if (delivery.duration() instanceof LingeringDuration lingering) {
            double skillEffectDurationMult = 1.0;
            double spellDurationMult = 1.0;
            if (caster instanceof LivingEntity lc) {
                skillEffectDurationMult = AilmentHelper.withScope(lc, Set.of(GateTag.AILMENT, GateTag.not(GateTag.TAKEN)), AttributeRegistry.GROUND_DURATION);
                spellDurationMult = AilmentHelper.spellDurationMultiplier(lc);
            }
            int durationTicks = Math.max(1, (int) (lingering.duration().getInt(skillCtx) * skillEffectDurationMult * spellDurationMult));
            int intervalTicks = Math.max(1, lingering.tickInterval().getInt(skillCtx));
            LingeringAreaTracker.register(level, caster.getUUID(), center, radius,
                    intervalTicks, durationTicks, instance);
        }

        // ── 余震（Aftershock）生产者 ──
        if (context.skill() != null && caster instanceof LivingEntity lc) {
            Set<String> statTags = context.skill().getSkill()
                    .getStatTags(skillCtx, context.skill().getSkillId());
            if (statTags.contains("slam")) {
                boolean isMace = statTags.contains(GateTag.MACE)
                        || ModHelper.currentEquipment(lc, lc.getMainHandItem(), "mace");
                boolean inForm = StatusEngine.hasForm(lc);
                Set<String> scope = new HashSet<>(List.of("slam"));
                if (isMace) scope.add(GateTag.MACE);
                if (inForm) scope.add(GateTag.FORM);
                double chance = DamageHelper.readInScopePublic(lc, scope, AttributeRegistry.AFTERSHOCK_CHANCE);
                if (chance > 0 && lc.getRandom().nextDouble() < Math.min(chance, 1.0)) {
                    double areaMult = lc.getAttributeValue(AttributeRegistry.AFTERSHOCK_AREA);
                    double ar = radius * areaMult;
                    int delay = 12;
                    LingeringAreaTracker.register(level, caster.getUUID(), center, ar, delay, delay + 2, instance);
                }
            }
        }
    }

    /**
     * EFFECT_AREA（area_of_effect，基 1.0）：读施法者属性缩放范围投递半径。
     */
    private static double readEffectArea(LivingEntity entity) {
        if (entity == null) return 1.0;
        Set<String> tags = new HashSet<>(StatScopes.current(entity).tags());
        tags.add(GateTag.AREA);
        tags.add(GateTag.MELEE);
        tags.add(GateTag.MINION);
        try (var scope = StatScopes.enter(entity, StatContext.of(tags, null))) {
            return entity.getAttributeValue(AttributeRegistry.EFFECT_AREA);
        } catch (Exception ignored) {}
        return 1.0;
    }

    private static void spawnAreaBlast(ServerLevel level, Vec3 center, double radius) {
        final int segments = 16;
        for (int i = 0; i < segments; i++) {
            double a = (2.0 * Math.PI * i) / segments;
            Vec3 p = center.add(Math.cos(a) * radius, 0.1, Math.sin(a) * radius);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 6,
                radius * 0.3, 0.4, radius * 0.3, 0.0);
    }

    // ───────────────────────── HAZARD ─────────────────────────

    /**
     * 危害物执行器：在目标位置生成驻场 Hazard 实体；命中判定由 SkillEntity 周期触发回调 ON_HIT。
     */
    private void executeHazard(AreaDelivery delivery, ActionContext context,
                               UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Vec3 center = profile.nova(skillCtx) ? caster.position() : (
                context.position() != null && context.position() != Vec3.ZERO
                        ? context.position() : caster.position());

        double radius = delivery.shape().sampleRadius(skillCtx) * profile.radiusMultiplier("hazard", skillCtx);
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.HAZARD), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(100.0)).getInt(skillCtx));

        EntityRead read = delivery.ref().orElse(null);
        if (read == null) return;

        int total = Math.max(1, delivery.count().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(1.0)).getInt(skillCtx));
        int maxActive = delivery.maxActive().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(0.0)).getInt(skillCtx);
        int remaining = total;
        if (maxActive > 0) {
            int alive = 0;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof EntityDefCarrier old
                        && old.entityRead() != null
                        && read.ref().equals(old.entityRead().ref())
                        && caster.getUUID().equals(ownerId(old))) {
                    alive++;
                }
            }
            remaining = Math.min(total, maxActive - alive);
        }
        if (remaining <= 0) return;

        double scatter = Math.max(0.5, radius);
        for (int i = 0; i < remaining; i++) {
            Vec3 pos = remaining == 1 ? center : center.add(
                    Math.cos(2 * Math.PI * i / remaining) * scatter, 0,
                    Math.sin(2 * Math.PI * i / remaining) * scatter);
            Entity hazard = EntitySpawns.spawn(serverLevel, caster, read, lifetimeTicks, (float) radius, pos, casterId, phaseId);
            if (hazard == null) return;
        }
        PersistentDeliveryManager.launchCue(serverLevel, center);
    }

    /** 在场危害物的归属者 UUID（无法解析时返回 null，永不匹配）。 */
    private static java.util.UUID ownerId(EntityDefCarrier carrier) {
        return carrier instanceof SkillEntity se && se.getOwner() != null
                ? se.getOwner().getUUID() : null;
    }

    // ───────────────────────── REMNANT ─────────────────────────

    /**
     * 残片执行器：在目标位置生成可拾取的驻场 Remnant 实体；拾取时由 SkillEntity 回调 ON_HIT。
     */
    private void executeRemnant(AreaDelivery delivery, ActionContext context,
                                UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Vec3 center = profile.nova(skillCtx) ? caster.position() : (
                context.position() != null && context.position() != Vec3.ZERO
                        ? context.position() : caster.position());

        double radius = delivery.shape().sampleRadius(skillCtx) * profile.radiusMultiplier("remnant", skillCtx);
        double pickupMult = AilmentHelper.withScope(caster, Set.of(GateTag.REMNANT),
                AttributeRegistry.REMNANTS_COLLECTED_FURTHER_AWAY);
        radius *= pickupMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(200.0)).getInt(skillCtx));

        EntityRead read = delivery.ref().orElse(null);
        if (read == null) return;
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            com.cimdy.coe.CraftOfExile.LOGGER.error("Remnant 投递：未找到实体定义 {}", read.ref());
            return;
        }

        spawnRemnant(serverLevel, center, caster, casterId, phaseId, lifetimeTicks, (float) radius, read, def);
        PersistentDeliveryManager.launchCue(serverLevel, center);

        double extraChance = AilmentHelper.withScope(caster, Set.of(GateTag.REMNANT),
                AttributeRegistry.REMNANT_EXTRA_CHANCE);
        if (extraChance > 0.0 && caster.getRandom().nextDouble() < extraChance) {
            double ang = caster.getRandom().nextDouble() * Math.PI * 2.0;
            double dist = 0.6 + caster.getRandom().nextDouble() * 0.6;
            Vec3 extraPos = center.add(Math.cos(ang) * dist, 0.0, Math.sin(ang) * dist);
            spawnRemnant(serverLevel, extraPos, caster, casterId, phaseId, lifetimeTicks, (float) radius, read, def);
        }
    }

    private static void spawnRemnant(ServerLevel serverLevel, Vec3 pos, LivingEntity caster,
                                     UUID casterId, UUID phaseId, int lifetimeTicks, float radius,
                                     EntityRead read, EntityDef def) {
        EntitySpawns.spawn(serverLevel, caster, read, lifetimeTicks, radius, pos, casterId, phaseId);
    }

    // ───────────────────────── BEAM ─────────────────────────

    /**
     * 光束执行器：沿施法者朝向发射一束能量，在 duration tick 内每 tick 命中光束范围内敌人。
     * 瞬发光束（duration=0）注册为 1 tick 后自动移除。
     */
    private void executeBeam(AreaDelivery delivery, ActionContext context,
                             UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof Entity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();

        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);

        var skillCtx = context.skill() != null ? context.skill().getContext() : null;
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);
        int baseDuration = (int) delivery.beamDuration().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(0.0)).getFloat(skillCtx);
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

    // ───────────────────────── WAVE ─────────────────────────

    /**
     * 波前执行器：沿施法者朝向发射一道向前推进的球形波前，沿途每实体仅命中一次。
     */
    private void executeWave(AreaDelivery delivery, ActionContext context,
                             UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction() : caster.getLookAngle()).normalize();

        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);

        var skillCtx = context.skill() != null ? context.skill().getContext() : null;
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);
        double range = delivery.range().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(10.0)).getFloat(skillCtx);
        double speed = Math.max(0.01, delivery.speed().orElse(com.cimdy.coe.data.codec.support.math.ExpressionCodec.of(0.4)).getFloat(skillCtx));
        int remaining = (int) Math.ceil(range / speed);

        PersistentDeliveryManager.register(new PersistentDeliveryManager.Entry(
                casterId, phaseId, instance, delivery, origin, direction,
                1, remaining, level.dimension(), SupportProfile.from(context), speed));
    }

}
