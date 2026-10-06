package com.cimdy.coe.data.codec.skill.phase.action.damage;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.world.EntityOperationEvent;
import org.jspecify.annotations.Nullable;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 伤害动作 — 对目标造成伤害，并在击杀时点燃 ON_KILL。
 *
 * <p>伤害以「多类型伤害包」(Map&lt;伤害类型, 数值&gt;) 计算，从而支持：
 * <ul>
 *   <li>附加伤害按各自类型累加（火/电/冰/混沌可同时存在）；</li>
 *   <li>转伤（在倍率前按比例把一种类型迁到另一种）；</li>
 *   <li>全局倍率（EffectMod 的 more，无作用域）；</li>
 *   <li>按投递类型 / 伤害类型作用域、按暴击等条件化修正（EffectMod 的 delivery/damage_type/condition）；</li>
 *   <li>禁伤（suppress 将某类型置 0）；</li>
 *   <li>斩杀（EffectMod 的 execute_threshold：目标生命低于阈值时该类型伤害致命）。</li>
 * </ul>
 * 最终逐类型调用 {@code hurt}，每种类型独立结算（重置无敌帧以保证都生效）。
 *
 * <p>所有「修正效果」统一来自 {@link EffectMod}，投递/效果由同一套作用域逻辑判定，
 * 不再为每种效果单独建类。
 */
public record DamageAction(
        String damageType,
        ExpressionCodec.Value amount,
        Optional<ExpressionCodec.Value> knockback,
        ExpressionCodec.Value canCrit
) implements Action {
    public static final Codec<DamageAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("damage_type").forGetter(DamageAction::damageType),
            ExpressionCodec.VALUE_CODEC.fieldOf("amount").forGetter(DamageAction::amount),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("knockback").forGetter(DamageAction::knockback),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("can_crit", ExpressionCodec.of(0.0))
                    .forGetter(DamageAction::canCrit)
    ).apply(i, DamageAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.DAMAGE; }

    /**
     * 伤害行为自述标签：按基础伤害类型携带元素标签（fire/cold/lightning/chaos/physical，各自独立共存）。
     * ADR-003 阶段1：改查 {@link DamageTypes} 定义表（单一事实来源），未知类型返回空集合（与旧 switch default 一致），
     * 但装载期已由 {@code LoadEvent.validateSkillDamageTypes} 暴露，不再静默失效。
     */
    @Override
    public Set<String> statTags() {
        return DamageTypes.scopeTagsOf(damageType);
    }

    @Override
    public void execute(ActionContext context) {
        if (context.target().isEmpty()) return;
        if (!(context.target().get() instanceof LivingEntity target)) return;
        if (!(context.caster() instanceof Entity caster)) return;
        if (target.isDeadOrDying()) return; // 已死，不重复结算

        SupportProfile profile = SupportProfile.from(context);
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        String deliveryTypeId = resolveDeliveryTypeId(context);
        boolean crit = isCritical(context, profile);
        float hpFrac = target.getMaxHealth() > 0 ? target.getHealth() / target.getMaxHealth() : 0f;

        // 1. 多类型伤害包：基础伤害
        Map<String, Float> packet = new LinkedHashMap<>();
        packet.put(damageType, amount.getFloat(skillCtx));

        // 2. 附加伤害（按各自伤害类型独立累加）
        if (profile.hasAddedDamage()) {
            RandomSource random = caster.level().getRandom();
            for (EffectMod m : profile.effectMods()) {
                if (m.added().isEmpty()) continue;
                var ad = m.added().get();
                String t = ad.damageType();
                float min = ad.min().getFloat(skillCtx);
                float max = ad.max().getFloat(skillCtx);
                float rolled = max <= min ? min : min + random.nextFloat() * (max - min);
                packet.merge(t, rolled, Float::sum);
            }
        }

        // 3. 转伤：按比例把 from 迁到 to（在倍率之前）
        for (EffectMod m : profile.effectMods()) {
            if (m.convert().isEmpty()) continue;
            var c = m.convert().get();
            Float from = packet.get(c.from());
            if (from != null && from > 0) {
                float ratio = (float) c.ratio().getValue();
                float moved = from * ratio;
                packet.merge(c.to(), moved, Float::sum);
                boolean keepOriginal = c.addedAsExtra().map(v -> v.getBool(skillCtx)).orElse(false);
                if (!keepOriginal) {
                    packet.put(c.from(), from - moved);
                }
            }
        }

        // 4. 投射物分裂降伤（每投射物标量，来自 Fork 的 damage_multiplier_per_split）
        float split = context.projectileDamageMultiplier().orElse(1.0f);
        packet.replaceAll((k, v) -> v * split);

        // 4.5 本次施放的临时伤害倍率（充能 effect_per_charge / 连击 effect_multiplier_per_stack 消耗时乘入）
        if (skillCtx != null) {
            float transientMult = skillCtx.getTransientDamageMultiplier();
            if (transientMult != 1f) {
                packet.replaceAll((k, v) -> v * transientMult);
            }
        }

        // 5. 暴击倍率（命中且暴击时，对所有类型生效）
        // 注意：暴击是否发生由上面 isCritical 决定，并通过 SkillDamageSource 透传给 DamageEvent，
        // 由 DamageEvent 统一发布 on_crit 元事件（单一权威源，避免双重暴击判定）。
        if (crit) {
            packet.replaceAll((k, v) -> v * BASE_CRIT_MULTIPLIER * profile.critDamageMultiplier(skillCtx));
        }

        // 6. 作用域/条件化修正：more / less / suppress / execute / penetration / exposure / slow
        List<Exposure> pendingExposures = new ArrayList<>();
        List<Slow> pendingSlows = new ArrayList<>();
        for (EffectMod m : profile.effectMods()) {
            if (m.delivery().isPresent() && !m.delivery().get().equalsIgnoreCase(deliveryTypeId)) continue;
            if (m.condition().isPresent() && m.condition().get().equalsIgnoreCase("crit") && !crit) continue;

            // 抗性穿透：受击方按目标抗性削减伤害，攻击方在此按「穿透后的抗性」预先补偿
            if (m.penetration().isPresent()) {
                var pen = m.penetration().get();
                float p = pen.amount().getFloat(skillCtx);
                if (p > 0) {
                    Set<String> types = pen.damageType().equalsIgnoreCase("elemental")
                            ? Set.of("fire", "cold", "lightning")
                            : Set.of(pen.damageType().toLowerCase(Locale.ROOT));
                    for (String t : types) {
                        Float dmg = packet.get(t);
                        if (dmg == null || dmg <= 0) continue;
                        packet.put(t, dmg * penetrationCompensation(target, t, p));
                    }
                }
            }
            // 元素曝露：命中后施加（结算完伤害再挂 debuff，避免影响本次命中）
            m.exposure().ifPresent(pendingExposures::add);
            // 减速：命中后施加（Maim/Hinder 类非异常 debuff，同 exposure 挂点）
            m.slow().ifPresent(pendingSlows::add);

            if (m.suppress()) {
                if (m.damageType().isPresent()) packet.put(m.damageType().get(), 0f);
                else packet.replaceAll((k, v) -> 0f);
                continue;
            }

            float more = m.more().map(v -> v.getFloat(skillCtx)).orElse(1f);
            float less = m.less().map(v -> v.getFloat(skillCtx)).orElse(1f);
            float mult = more * less;
            if (mult != 1f) {
                if (m.damageType().isPresent()) {
                    for (String key : resolveDamageTargets(m.damageType().get(), deliveryTypeId, packet)) {
                        packet.compute(key, (k, v) -> (v == null ? 0f : v) * mult);
                    }
                } else {
                    packet.replaceAll((k, v) -> v * mult);
                }
            }

            if (m.executeThreshold().isPresent()) {
                float th = m.executeThreshold().get().getFloat(skillCtx);
                if (th >= hpFrac) {
                    float lethal = target.getHealth();
                    if (m.damageType().isPresent()) {
                        String t = m.damageType().get();
                        packet.compute(t, (k, v) -> Math.max(v == null ? 0f : v, lethal));
                    } else {
                        packet.replaceAll((k, v) -> Math.max(v, lethal));
                    }
                }
            }
        }

        // 7. 逐类型结算（重置无敌帧，确保每种伤害都生效）
        for (Map.Entry<String, Float> e : packet.entrySet()) {
            float dmg = e.getValue();
            if (dmg <= 0) continue;
            // 伤害源必须携带施法者（direct=causing=caster），否则 source.getEntity() 为 null，
            // 攻击者作用域不会入栈，攻击侧加成/异常衍生/暴击/命中上下文/ON_KILL 全部失效（死线）。
            DamageSource source = new SkillDamageSource(
                    DamageTypeRegistry.getDamageSource((ServerLevel) caster.level(), e.getKey(), caster),
                    context.skill(), crit, context.phaseId());
            target.invulnerableTime = 0;
            target.hurt(source, dmg);
        }

        // 7.5 命中后施加元素曝露（压低目标对应元素抗性）—— 由 AilmentHelper 统一施加
        if (!pendingExposures.isEmpty() && !target.isDeadOrDying()) {
            for (Exposure exp : pendingExposures) {
                AilmentHelper.applyExposure(target, exp, skillCtx);
            }
        }

        // 7.6 命中后施加减速（SLOW debuff：每级行动速度 -1%）—— 由 AilmentHelper 统一施加
        if (!pendingSlows.isEmpty() && !target.isDeadOrDying()) {
            for (Slow slow : pendingSlows) {
                AilmentHelper.applySlow(target, slow, skillCtx);
            }
        }

        // 7.1 近距命中击退几率（KNOCKBACK_CHANCE，Stat值）：命中近距（≤KNOCKBACK_CLOSE_RANGE）时掷骰，
        // 命中则沿「施法者→目标」水平方向推送目标，与 MoveAction.KNOCKBACK 同构。
        // q-1：chance=Stat值（fold 通道读取），与 DODGE_CHANCE/CHARM_ACTIVATION_CHANCE 同族；
        // 击退「距离倍率」已拆至 KNOCKBACK 属性（EntityOperationEvent.onKnockBack 缩放强度），本处仅承载几率门槛。
        if (!target.isDeadOrDying() && caster instanceof LivingEntity attacker) {
            double dist = attacker.distanceTo(target);
            if (dist <= KNOCKBACK_CLOSE_RANGE) {
                // 绑定命中目标为交互对方（sourceEntity），使 onTarget(COND_BLEED_STATUS_AGE) 类条件可在目标身上求值（死线 #37）
            double chance = readKnockbackChance(attacker, target);
            if (chance > 0 && attacker.level().getRandom().nextFloat() < chance) {
                // 击退力度走技能数据值（knockback 字段），与 KNOCKBACK 距离 Stat（EntityOperationEvent.onKnockBack 缩放）同构；
                // 无数据值时回退 KNOCKBACK_FORCE 常量。去硬编码、使数据值生效。
                double force = knockback.map(v -> (double) v.getFloat(skillCtx)).orElse(KNOCKBACK_FORCE);
                Vec3 dir = target.position().subtract(attacker.position());
                dir = new Vec3(dir.x, 0, dir.z).normalize();
                if (dir.lengthSqr() > 1e-6) {
                    target.push(dir.x * force, 0.2, dir.z * force);
                }
            }
            }
        }

        // 8. 击杀检测 → 由 DamageEvent.LivingDeathEvent 统一发布 on_kill 元事件（单一权威源）。
        //    此处仅触发本技能自身的阶段生命周期（ON_KILL），不影响元宝石跨技能触发。
        if (target.isDeadOrDying()) {
            UUID phaseId = context.phaseId();
            if (phaseId != null) {
                SkillPhaseInstance phase = SkillPhaseInstanceTracker.find(caster.getUUID(), phaseId);
                if (phase != null) {
                    phase.fireTrigger(LifecycleTrigger.ON_KILL);
                }
            }
        }
    }

    /** 暴击基础倍率（PoE2 默认 150%）。 */
    private static final float BASE_CRIT_MULTIPLIER = 1.5f;
    /** 箭矢随飞行距离提升暴击率（GAP-FIX#16，#3）的「米数」阈值：规范 7.0 取自 node_stat_map 模板「after 7 metres」。 */
    private static final double ARROW_TRAVEL_CRIT_METRES = 7.0;

    /** 近距击退判定距离（方块）：对齐 POE2「at close range」近战阈值。 */
    private static final double KNOCKBACK_CLOSE_RANGE = 3.0;
    /** 近距击退触发时的基础推送力度（水平）。 */
    private static final double KNOCKBACK_FORCE = 0.5;

    /**
     * 将 EffectMod 的 damage_type 作用域展开为 packet 中实际要修正的伤害类型 key。
     * 支持聚合类型（elemental=火/冰/雷；all/attack/melee=全部）与投递作用域
     * （projectile/area/beam/aura/self=仅当本次投递匹配时作用于全部伤害类型）。
     * 避免把 "elemental"/"attack" 等当成真实伤害类型落到不存在的 packet key 而变成空操作。
     */
    private static Set<String> resolveDamageTargets(String damageType, String deliveryTypeId, Map<String, Float> packet) {
        String t = damageType.toLowerCase(Locale.ROOT);
        return switch (t) {
            case "elemental" -> Set.of("fire", "cold", "lightning");
            case "all", "attack", "melee" -> new HashSet<>(packet.keySet());
            case "projectile", "area", "beam", "aura", "self" -> {
                if (!deliveryTypeId.equalsIgnoreCase(t)) yield Set.of();
                yield new HashSet<>(packet.keySet());
            }
            default -> Set.of(t);
        };
    }

    /**
     * 抗性穿透补偿倍率。受击方以 {@code 1 - R}（R 为目标抗性，受上限钳制）削减伤害，
     * 攻击方按「穿透后的抗性 R'」预乘 {@code (1 - R') / (1 - R)} 抵消差额。
     * 对齐 POE2 规则：穿透不能把抗性降到 0 以下（原本为负的抗性不受影响）。
     * <p><b>归一化读取</b>：在元素标签作用域内读 {@code RESISTANCES}/{@code MAX_RESISTANCES}
     * （与 {@code DamageTypeConvert.applyReduction} 同一「归一属性+标签」范式），
     * 取代旧的 FIRE/COLD/LIGHT/CHAOS_RESISTANCE + MAX_*_RESISTANCE 8 独立属性。
     */
    private static float penetrationCompensation(LivingEntity target, String type, float pen) {
        String tag = switch (type) {
            case "fire" -> com.cimdy.coe.data.codec.support.stat.tag.GateTag.FIRE;
            case "cold" -> com.cimdy.coe.data.codec.support.stat.tag.GateTag.COLD;
            case "lightning" -> com.cimdy.coe.data.codec.support.stat.tag.GateTag.LIGHTNING;
            case "chaos" -> com.cimdy.coe.data.codec.support.stat.tag.GateTag.CHAOS;
            default -> null;
        };
        if (tag == null) return 1f;
        float r = (float) readInScope(target, Set.of(tag), AttributeRegistry.RESISTANCES);
        float max = (float) readInScope(target, Set.of(tag), AttributeRegistry.MAX_RESISTANCES);
        r = Math.min(r, max);
        if (r <= 0) return 1f; // 抗性非正时穿透无效（POE2 不穿到负）
        float rAfter = Math.max(r - pen, 0f);
        float denom = 1f - r;
        if (denom < 1e-4f) denom = 1e-4f; // 抗性≈100% 时防除零
        return (1f - rAfter) / denom;
    }

    /** 在标签作用域内实时读取某属性（收口至 {@link ActionContext#stat}）。 */
    private static float readInScope(LivingEntity entity, Set<String> tags, Holder<Attribute> attr) {
        return (float) ActionContext.stat(entity, attr, tags, null);
    }

    /**
     * 在 {knockback} 作用域内读 KNOCKBACK_CHANCE（近距命中击退几率，Stat值）。
     *
     * <p><b>为什么需要 {@code against}</b>：「命中流血敌人时有几率击退」这类词缀用
     * {@code onTarget(COND_BLEED_STATUS_AGE, ...)} 接线，其条件必须在<b>命中目标</b>身上求值；
     * 而 2 参 {@code StatContext.of} <b>恒置 {@code sourceEntity=null}</b>，
     * 使 {@code triggerTarget} 恒 null → 条件恒假、条目永不生效（死线 #37）。
     * 此处显式绑定受害者，该类条件方可求值（与 {@code DamagePipeline.readInScopeAgainst} 同一手法：
     * 保留原标签集、仅补 {@code withSourceEntity}，用 {@code enter} 而非 {@code enterMerged} 以免作用域变宽）。
     */
    private static double readKnockbackChance(LivingEntity entity, @Nullable LivingEntity against) {
        return ActionContext.stat(entity, AttributeRegistry.KNOCKBACK_CHANCE, Set.of(GateTag.KNOCKBACK), against);
    }

    /** 解析本次命中来自的投递类型 id（用于按投递作用域的伤害修正）。 */
    private String resolveDeliveryTypeId(ActionContext context) {
        if (context.phaseId() != null && context.caster() != null) {
            SkillPhaseInstance inst = SkillPhaseInstanceTracker.find(context.caster().getUUID(), context.phaseId());
            if (inst != null && inst.getPhase().delivery().isPresent()) {
                return inst.getPhase().delivery().get().getType().id();
            }
        }
        return "none";
    }

    private boolean isCritical(ActionContext context, SupportProfile profile) {
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        if (!canCrit.getBool(skillCtx)) return false;
        double baseCrit = context.skill() != null
                ? context.skill().getSkill().cast().getCrit(skillCtx)
                : 0;
        double chance = baseCrit * profile.critChanceMultiplier(skillCtx);
        // 箭矢随飞行距离提升暴击率（#3 / GAP-FIX#16）：仅投射物投递命中按飞行距离线性提升暴击率，封顶 cap（标记 amount，规范 0.40）/ metres（7.0）。
        // cap 由 INERT 标记 MarkerStatEffect 承载；metres 阈值取自 node_stat_map 模板；对近战/非投射物零副作用（delivery 门控 + cap<=0 早退）。
        if (context.caster() instanceof LivingEntity arrowOwner
                && "projectile".equals(resolveDeliveryTypeId(context))) {
            double arrowCritCap = StatEngine.magnitudeOf(arrowOwner, "arrow_travel_crit", Set.of());
            if (arrowCritCap > 0.0) {
                float travel = context.hitContext().map(h -> h.distance()).orElse(0f);
                double frac = Math.min(travel, ARROW_TRAVEL_CRIT_METRES) / ARROW_TRAVEL_CRIT_METRES;
                chance *= (1.0 + arrowCritCap * frac);
            }
        }
        if (chance <= 0) return false;
        return context.caster().level().getRandom().nextFloat() < chance;
    }
}
