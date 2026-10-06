package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.LingeringAreaTracker;

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
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.affix.ModHelper;
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
 * 范围执行器 — 在目标位置搜索范围内实体并点燃 ON_HIT。
 * 同时应用辅助宝石的范围修改效果（半径倍率 / Nova / Cascade）。
 */
public class AreaDeliveryExecutor implements DeliveryExecutor<AreaDelivery> {

    @Override
    public void execute(AreaDelivery delivery, ActionContext context,
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
        // 范围技此前在客户端完全无可见/可闻反馈（无实体 / 无粒子 / 无音效），与「打击真实感」相悖。
        // 在落点以半径发射一圈能量环（显示 AoE 范围）+ 命中时播放撞击音，使视觉/听觉与服务端命中时刻
        // （相位执行即落点时刻，已对齐贯穿时间轴）严格同步。沿用与 Wave 同款的 SOUL_FIRE_FLAME 能量语言。
        spawnAreaBlast(level, center, radius);
        if (!targets.isEmpty()) {
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.7F, 0.8F);
        }

        // 额外目标（Additional Targets）：在范围外、扩展搜索半径内，对最近的 N 个敌人也点燃 ON_HIT。
        // 对应 PoE2 中「技能可命中额外目标」——技能本身与辅助宝石共用同一 DeliveryMod 形状，二者皆生效。
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

        // Lingering 持续时间模式：首脉冲已由上方即时扫描完成，后续脉冲登记到 LingeringAreaTracker，
        // 由 SkillEvent（ServerTickEvent.Post）按维度驱动，每 interval 重新扫描并点燃 ON_HIT（复用同一伤害管线）。
        // 注：Cascade / Additional Targets 仅在首脉冲生效（后续脉冲为纯区域重复检测）。
        if (delivery.duration() instanceof LingeringDuration lingering) {
            // 「Skill Effect Duration」在本引擎统一落到 AILMENT_DURATION（StatEffectIds 设计注释）；
            // 以 {AILMENT, !TAKEN} 作用域读取，使 inc_skill_effect_duration 族（含 freeze per-enemy）生效。
            // 首类「法术时长」（SPELL_DURATION_MULT）：额外读取，使 {spell,storm}/{spell,plant} 的
            // 「X% less Duration」（wildsurge_incantation）在此真实缩短留驻时长（作用域已并入技能标签）。
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
        // 数据层（StatGen，标签 aftershock）：chance_aftershock / _mace_slam / _shapeshift_slam
        //   + inc_aftershock_area（缩放落点脉冲半径）。
        // 触发：技能带 slam 标签（SkillRedGen.SLAM_SKILLS 数据标记）；
        //   mace_slam 变体额外要求施法者持锤（ModHelper.currentEquipment "mace"）；
        //   shapeshift_slam 变体额外要求施法者处于形态（StatusEngine.hasForm）。
        // 落点按聚合 0~1 几率回放一次延迟二次范围脉冲：
        //   复用 LingeringAreaTracker（interval<duration → 恰好单次延迟脉冲，不无限递归——
        //   脉冲经 instance.fireTrigger(ON_HIT) 直接点燃伤害，不再回经本执行器）。
        if (context.skill() != null && caster instanceof LivingEntity lc) {
            Set<String> statTags = context.skill().getSkill()
                    .getStatTags(skillCtx, context.skill().getSkillId());
            if (statTags.contains("slam")) {
                boolean isMace = statTags.contains(GateTag.MACE)
                        || ModHelper.currentEquipment(lc, lc.getMainHandItem(), "mace");
                boolean inForm = StatusEngine.hasForm(lc);
                // 归一：base + mace + form 全部收敛到 AFTERSHOCK_CHANCE 单属性，
                // 按技能实际作用域（{slam} / {mace,slam} / {form,slam}）聚合，等价于原三段相加。
                Set<String> scope = new HashSet<>(List.of("slam"));
                if (isMace) scope.add(GateTag.MACE);
                if (inForm) scope.add(GateTag.FORM);
                double chance = DamageHelper.readInScopePublic(lc, scope, AttributeRegistry.AFTERSHOCK_CHANCE);
                if (chance > 0 && lc.getRandom().nextDouble() < Math.min(chance, 1.0)) {
                    double areaMult = lc.getAttributeValue(AttributeRegistry.AFTERSHOCK_AREA);
                    double ar = radius * areaMult;
                    int delay = 12; // ~0.6s，对齐 POE2 余震延迟
                    LingeringAreaTracker.register(level, caster.getUUID(), center, ar, delay, delay + 2, instance);
                }
            }
        }
    }

    /**
     * EFFECT_AREA（area_of_effect，基 1.0）：读施法者属性缩放范围投递半径。
     *
     * <p><b>作用域 = 当前作用域 ∪ 历史缺省集 {AREA,MELEE,MINION}</b>。并入历史缺省集是兼容底线：
     * 早期全部「范围效果」条目按这三标签聚合登记，去掉会让它们全部失配。
     * <b>并入当前作用域</b>则使按技能类型细分的范围条目（{@code herald}/{@code grenade}/{@code hazard}/
     * {@code totem}/{@code banner}/{@code companion}/{@code offering}/{@code spell}/{@code warcry} …）
     * 得以命中——此前本方法只认硬编码三标签，那批条目虽已接线却<b>永不生效</b>（静默死线）。
     *
     * <p>标签表达式为<b>与</b>语义（作用域须含全部叶子标签），故并集只会更宽松：
     * 既有条目的命中集合只增不减，无回归风险。
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

    /**
     * 范围投递的客户端同步反馈：在落点中心以半径发射一圈能量环（直观显示 AoE 范围边界），
     * 并在中心补一簇粒子强化落点感。由服务端每此广播，所有客户端依自身帧率渲染同一位置 ——
     * 无需任何自定义位置同步包。粒子语言与 Wave 一致的 {@code SOUL_FIRE_FLAME}，保证整套投递视觉统一。
     */
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
}
