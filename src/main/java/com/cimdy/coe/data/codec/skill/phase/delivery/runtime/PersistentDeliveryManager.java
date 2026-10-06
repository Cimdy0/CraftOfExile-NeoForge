package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BeamDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.WaveDelivery;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillTimeline;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.registry.AttributeRegistry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 常驻投递管理器 — 驱动需要跨 tick 持续命中的投递方式（光束 / 光环）。
 *
 * <p>PoE2 对应：
 * <ul>
 *   <li>Beam（光束）：在 duration  tick 内每 tick 命中光束范围内的敌人（如 Incinerate / Rolling Slam 冲击波）。</li>
 *   <li>Aura（光环）：围绕施法者持续生效，跟随施法者移动，直到技能停止（如 Herald of Ash）。</li>
 * </ul>
 *
 * <p>与投射物（由实体自身生命周期驱动）不同，Beam/Aura 由本管理器在服务端每 tick 统一推进。
 * 通过 {@link #tickAll(ServerLevel)} 调用，由 {@code SkillEvent} 的服务端 tick 驱动。
 */
public class PersistentDeliveryManager {
    /** 剩余 tick 为 -1 表示常驻，直到技能停止（用于光环）。 */
    public static final int INFINITE = -1;

    public static final class Entry {
        final UUID casterId;
        final UUID phaseId;
        final SkillPhaseInstance instance;
        final Delivery delivery;
        final Vec3 origin;
        final Vec3 direction;
        final int interval;
        final ResourceKey<Level> dimension;
        final SupportProfile profile;
        final double speed;
        /** 波前命中去重集：保证每个实体仅被波前命中一次（穿过即生效、不重复）。 */
        final Set<UUID> alreadyHit;
        int remaining;
        int tickCounter = 0;

        public Entry(UUID casterId, UUID phaseId, SkillPhaseInstance instance, Delivery delivery,
              Vec3 origin, Vec3 direction, int interval, int remaining,
              ResourceKey<Level> dimension, SupportProfile profile, double speed) {
            this.casterId = casterId;
            this.phaseId = phaseId;
            this.instance = instance;
            this.delivery = delivery;
            this.origin = origin;
            this.direction = direction;
            this.interval = interval;
            this.remaining = remaining;
            this.dimension = dimension;
            this.profile = profile;
            this.speed = speed;
            this.alreadyHit = new HashSet<>();
        }
    }

    private static final List<Entry> ACTIVE = new CopyOnWriteArrayList<>();

    public static void register(Entry e) { ACTIVE.add(e); }
    public static void unregister(UUID phaseId) {
        ACTIVE.removeIf(e -> {
            if (e.phaseId.equals(phaseId)) {
                // 同步释放阶段实例引用计数，使对应 SkillPhaseInstance 在下一 tick 被正确丢弃
                SkillPhaseInstanceTracker.release(e.casterId, e.phaseId);
                return true;
            }
            return false;
        });
    }

    /** 每个服务端 tick 调用：推进本维度内所有常驻投递。 */
    public static void tickAll(ServerLevel level) {
        for (Entry e : ACTIVE) {
            if (!e.dimension.equals(level.dimension())) continue;
            e.tickCounter++;
            if (e.tickCounter % e.interval != 0) continue;

            // 贯穿时间轴：从所属技能实例拿到施放进度（与客户端动画同源世界时钟），
            // 使「效果视觉 / 音效」随施放进度绽放、与服务端 / 客户端共享同一条时钟
            // （攻击 / 施法速度经 SkillTimeline 已纳入，动画与效果同步伸缩）。
            SkillInstance si = e.instance.skill();
            SkillTimeline tl = (si != null) ? si.getTimeline() : null;
            float castProgress = (tl != null) ? tl.progress(level.getGameTime()) : 1f;

            // ---- Wave：向前推进的球形波前（服务端权威、客户端零同步负担）----
            if (e.delivery instanceof WaveDelivery wave) {
                Vec3 front = e.origin.add(e.direction.scale(e.tickCounter * e.speed));
                SkillContext skillCtx = e.instance.skillContext();
                // hit_test 为 0 时本 tick 波前不命中任何目标（统一 math 表达式门控）
                if (eval(wave.hitTest(), skillCtx) != 0.0) {
                Entity caster = level.getEntity(e.casterId);
                LivingEntity casterLiving = (caster instanceof LivingEntity le) ? le : null;
                // 波前厚度经「范围属性」作用域注入：半径 × EFFECT_AREA（{area} 作用域），
                // 与 Orb/Ball/Wave 等技能投递同源，非存在范围统一走范围属性作用域。
                double areaMult = casterLiving != null
                        ? AilmentHelper.withScope(casterLiving, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA) : 1.0;
                double radius = eval(wave.radius(), skillCtx)
                        * e.profile.radiusMultiplier("wave", skillCtx) * areaMult;
                    List<LivingEntity> targets = ExpressionContext.of(level, caster)
                            .queryEntities(EntityQuery.sphere(front, radius)
                                    .exclude(caster).aliveOnly()
                                    .filter(ent -> !e.alreadyHit.contains(ent.getUUID())));
                    int hits = 0;
                    for (LivingEntity t : targets) {
                        Vec3 tpos = t.position();
                        Vec3 normal = e.origin.subtract(tpos).normalize();
                        float dist = (float) e.origin.distanceTo(tpos);
                        e.instance.fireTrigger(LifecycleTrigger.ON_HIT,
                                HitContext.entityHit(t, tpos, normal, dist));
                        e.alreadyHit.add(t.getUUID());
                        hits++;
                    }
                    // 波前粒子（服务端广播，客户端自动渲染 → 可见的「移动能量墙」）
                    spawnWaveWall(level, front, e.direction, radius, castProgress);
                    if (hits > 0) {
                        level.playSound(null, front.x, front.y, front.z,
                                SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS,
                                0.4F + 0.6F * castProgress, 0.7F);
                    }
                }
                if (e.remaining > 0) {
                    e.remaining--;
                    if (e.remaining == 0) unregister(e.phaseId);
                }
                continue;
            }

            int hits = 0;
            for (LivingEntity t : computeTargets(level, e)) {
                Vec3 tpos = t.position();
                Vec3 normal = e.origin.subtract(tpos).normalize();
                float dist = (float) e.origin.distanceTo(tpos);
                e.instance.fireTrigger(LifecycleTrigger.ON_HIT, HitContext.entityHit(t, tpos, normal, dist));
                hits++;
            }
            // Beam：沿光束线段发射粒子，使此前完全不可见的 Beam 投递（arc / incinerate）在客户端可见；
            // 命中时播放撞击音。服务端广播、客户端零同步负担（沿用 Wave/Area 同款能量语言）。
            if (e.delivery instanceof BeamDelivery beam) {
                double range = eval(beam.range(), e.instance.skillContext());
                spawnBeamLine(level, e.origin, e.direction, range, castProgress);
                if (hits > 0) {
                    level.playSound(null, e.origin.x, e.origin.y, e.origin.z,
                            SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS,
                            0.4F + 0.6F * castProgress, 0.9F);
                }
            }


            if (e.remaining > 0) {
                e.remaining--;
                if (e.remaining == 0) unregister(e.phaseId);
            } else if (e.remaining == INFINITE) {
                if (!isAuraActive(e.casterId)) unregister(e.phaseId);
            }
        }
    }

    /**
     * 在波前平面发射一圈粒子，形成可见、可同步的「移动能量墙」。
     * 由服务端每 tick 广播，所有客户端依自身帧率渲染同一位置 —— 无需任何自定义位置同步包。
     */
    private static void spawnWaveWall(ServerLevel level, Vec3 front, Vec3 dir, double radius, float intensity) {
        // 构造与 dir 垂直的两个正交基向量，绕一圈采样成环
        Vec3 any = Math.abs(dir.y) < 0.99 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 u = dir.cross(any).normalize();
        Vec3 v = dir.cross(u).normalize();
        final int segments = 16;
        // 施放进度越高，能量墙粒子越密（与客户端肢体动画同步绽放）
        int per = 1 + (int) (intensity * 2.0f);
        for (int i = 0; i < segments; i++) {
            double a = (2.0 * Math.PI * i) / segments;
            Vec3 p = front.add(u.scale(Math.cos(a) * radius)).add(v.scale(Math.sin(a) * radius));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, per, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 沿光束线段发射一串粒子，使 Beam 投递在客户端可见（此前完全不可见）。
     * 由服务端每 tick 广播，所有客户端依自身帧率渲染同一位置 —— 无需任何自定义位置同步包。
     */
    private static void spawnBeamLine(ServerLevel level, Vec3 origin, Vec3 dir, double range, float intensity) {
        final int steps = Math.max(2, (int) Math.ceil(range));
        // 施放进度越高，光束粒子越密（与客户端肢体动画同步绽放）
        int per = 1 + (int) (intensity * 2.0f);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = origin.add(dir.scale(range * i / steps));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, per, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 投射物类投递（Orb / Ball / Thrown / Hazard / Remnant / Projectile）的发射音画提示：
     * 在生成点迸发一小簇能量粒子 + 轻响，使此前零音效的投递在客户端有可见 / 可闻的发射反馈
     * （服务端广播，客户端零同步负担）。与 Wave / Beam / Self / Area 同款能量语言（SOUL_FIRE_FLAME + NOTE_BLOCK_BASS）。
     */
    public static void launchCue(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 6, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    /** 光环仅在施法者仍“按住/激活”时持续；松开或停止后由管理器自动移除。 */
    private static boolean isAuraActive(UUID casterId) {
        return com.cimdy.coe.event.skill.SkillEvent.activeSkills.stream().anyMatch(s ->
                s.getContext().getCaster().getUUID().equals(casterId)
                        && s.getContext().isPress());
    }

    private static List<LivingEntity> computeTargets(ServerLevel level, Entry e) {
        // 获取实体统一经 Context 数据基站回调（EntityQuery 球形 / 光束扫描）；
        // 命中分发（获取数值侧：fireTrigger ON_HIT）留在 tickAll。
        Entity caster = level.getEntity(e.casterId);
        ExpressionContext base = ExpressionContext.of(level, caster);
        // 常驻投递跨 tick 求值必须带技能上下文：否则预设声明的 $radius / $range / $width
        // 参数槽（由 SkillRead.vars 注入）读不到，通用光环/光束预设整条为死线。
        SkillContext skillCtx = e.instance.skillContext();
        if (e.delivery instanceof BeamDelivery beam) {
            // 沿光束线段（origin → origin+dir*range）找 width 范围内的实体。宽度受辅助宝石“范围扩大”影响。
            double range = eval(beam.range(), skillCtx);
            LivingEntity casterLiving = (caster instanceof LivingEntity le) ? le : null;
            // 光束宽度经「范围属性」作用域注入：宽度 × EFFECT_AREA（{area} 作用域），
            // 与 Wave/Orb/Ball 等技能投递同源，非存在范围统一走范围属性作用域。
            double areaMult = casterLiving != null
                    ? AilmentHelper.withScope(casterLiving, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA) : 1.0;
            double width = eval(beam.width(), skillCtx) * e.profile.radiusMultiplier("beam", null) * areaMult;
            return base.queryEntities(EntityQuery.beam(e.origin, e.direction, range, width)
                    .aliveOnly().filter(ent -> !ent.getUUID().equals(e.casterId)));
        }
        return List.of();
    }

    /**
     * 带技能上下文求值（context 缺失时退回无上下文求值）。
     *
     * 正常已就位；仅在阶段实例尚未绑定动作上下文的极端时序下为 null，此时字面值仍可求，
     * 而 {@code $} 槽本就无从注入。
     */
    private static float eval(com.cimdy.coe.data.codec.support.math.ExpressionCodec.Value v,
                              SkillContext ctx) {
        return ctx != null ? v.getFloat(ctx) : v.getFloat();
    }
}
