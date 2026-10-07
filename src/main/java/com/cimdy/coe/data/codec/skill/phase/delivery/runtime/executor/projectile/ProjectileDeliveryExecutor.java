package com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutor;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryTracker;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.entity.EntitySpawns;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehavior;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.engine.entity.EntityCategoryLimit;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 统一投射物执行器 —— 按 {@link ProjectileDelivery#kind()} 分流到四套运行时行为（D4丙 对称归并）：
 * <ul>
 *   <li>{@code PROJECTILE}：生成 SkillArrow 并注册运行时上下文（飞行 + 碰撞 + 命中），含辅助宝石修改；</li>
 *   <li>{@code THROWN}：生成手雷弹体（抛物线飞出 → 落地 → 引信 → 范围引爆）；</li>
 *   <li>{@code BALL}：生成缓慢飞行、边飞边周期放能的球；</li>
 *   <li>{@code ORB}：在目标位置生成原地驻场、按节奏周期放能的球。</li>
 * </ul>
 */
public class ProjectileDeliveryExecutor implements DeliveryExecutor<ProjectileDelivery> {

    /**
     * 法术技能按几率额外发射投射物：「{X}% chance for Spell Skills to fire {X} additional Projectiles」。
     * <p><b>为什么用 {@code enterMerged}</b>：本执行器运行在 {@code SkillStatScopes.enterAction}
     * 的作用域内（SkillPhase:177 / SkillPhaseInstance:152），该作用域已并入<b>技能自身标签</b>，
     * 故 {@code spell} 应由技能标签提供而非此处硬编码——硬编码会让<b>非法术</b>技能的投射物也吃到本加成。
     * 合并后：法术技能 → 含 spell → EFF（GateExpr {spell, projectile}）命中；非法术 → 不含 → 恒 0。
     * <p>几率为 0 时直接返回，不掷骰（未点该天赋零开销、既有行为完全不变）。
     */
    private static int chanceExtraProjectiles(Entity caster) {
        if (!(caster instanceof LivingEntity living)) return 0;
        try (AutoCloseable ignored = StatScopes.enterMerged(living, StatContext.of(Set.of(GateTag.PROJECTILE), null))) {
            double chance = living.getAttributeValue(AttributeRegistry.ADDITIONAL_PROJECTILE_CHANCE);
            if (chance <= 0 || living.getRandom().nextDouble() >= chance) return 0;
            return (int) living.getAttributeValue(AttributeRegistry.ADDITIONAL_PROJECTILE_COUNT);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * stat/天赋分裂几率：「{X}% chance for Projectiles to Fork」。
     * <p>与 {@link #chanceExtraProjectiles} 同构：{@code enterMerged} 并入当前技能作用域，使 EFF 的
     * GateExpr {@code {projectile}} 命中，且条件（如「近 8 秒造成过近战命中」）在<b>施法者侧</b>实时求值。
     * <p>返回 <b>1</b> 表示本轮发射的投射物获得一次 Fork（命中时经 SkillArrow 的 {@code supportFork>0}
     * 分支分裂出 1 个额外投射物，原投射物继续飞行），<b>0</b> 表示不分裂；与辅助宝石 forkCount 取
     * {@code max} 而非相加，避免已带 Fork 辅助时重复叠加。几率为 0 时直接返回，不掷骰（未点该天赋零开销）。
     */
    private static int chanceFork(Entity caster) {
        if (!(caster instanceof LivingEntity living)) return 0;
        try (AutoCloseable ignored = StatScopes.enterMerged(living, StatContext.of(Set.of(GateTag.PROJECTILE), null))) {
            double chance = living.getAttributeValue(AttributeRegistry.PROJECTILE_FORK_CHANCE);
            if (chance <= 0 || living.getRandom().nextDouble() >= chance) return 0;
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * stat 分裂附加几率：「Projectiles have {X}% chance for an additional Projectile when Forking」。
     * <p>与 {@link #chanceFork} 同构（同样经 {@code enterMerged} 并入 {projectile} 作用域），但语义是
     * <b>条件式</b>的：只有在<b>本轮确定会发生分裂</b>时才掷骰，命中则分裂出的投射物数额外 +1。
     * 因此调用方须先判 {@code fork > 0} 再调用本方法（不 Fork 时零开销，与未点该词条行为一致）。
     */
    private static int chanceForkExtra(Entity caster) {
        if (!(caster instanceof LivingEntity living)) return 0;
        try (AutoCloseable ignored = StatScopes.enterMerged(living, StatContext.of(Set.of(GateTag.PROJECTILE), null))) {
            double chance = living.getAttributeValue(AttributeRegistry.PROJECTILE_FORK_EXTRA_CHANCE);
            if (chance <= 0 || living.getRandom().nextDouble() >= chance) return 0;
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判定技能是否为「lightning 技能」：遍历其阶段树（含事件监听与子阶段），
     * 任一 {@link DamageAction} 的伤害类型为 lightning 即判定为真。
     * <p>lightning 是伤害动作级标签（非技能级），故在此显式遍历阶段树判定，
     * 而非依赖 stat 作用域标签——链接触发点（{@code SkillArrow} 命中时）已无技能作用域，
     * 无法用 {@code enterMerged} 取得 lightning 标签。
     */
    private static boolean isLightningSkill(SkillInstance skill) {
        if (skill == null) return false;
        Skill s = skill.getSkill();
        if (s == null) return false;
        for (SkillPhasePlayer p : s.phases()) {
            if (phaseDealsLightning(p.phase())) return true;
        }
        return false;
    }

    private static boolean phaseDealsLightning(SkillPhase phase) {
        if (phase == null) return false;
        for (TriggeredAction ta : phase.actions()) {
            if (ta.action() instanceof DamageAction da && "lightning".equalsIgnoreCase(da.damageType())) return true;
        }
        if (phase.eventListen().isPresent()
                && phase.eventListen().get().actions().stream()
                .anyMatch(ta -> ta.action() instanceof DamageAction da && "lightning".equalsIgnoreCase(da.damageType()))) {
            return true;
        }
        for (SkillPhasePlayer child : phase.children()) {
            if (phaseDealsLightning(child.phase())) return true;
        }
        return false;
    }

    /** 多投射物之间的扇形展开总角度（弧度）。 */
    private static final double SPREAD_TOTAL = Math.toRadians(20);
    /** 分裂投射物之间的扇形展开总角度（弧度）。 */
    private static final double FORK_SPREAD_TOTAL = Math.toRadians(30);
    /** 圆形发射时，额外投射物相对主方向的锥角（弧度）。 */
    private static final double CONE_ANGLE = Math.toRadians(30);
    /** 弹幕（barrage）：每轮齐射绕垂直轴旋转的角度（弧度），使多轮齐射扩散成弹幕。 */
    private static final double BARAGE_STEP = Math.toRadians(8);

    @Override
    public void execute(ProjectileDelivery delivery, ActionContext context,
                        UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        switch (delivery.kind()) {
            case PROJECTILE -> executeProjectile(delivery, context, casterId, phaseId, instance);
            case THROWN -> executeThrown(delivery, context, casterId, phaseId, instance);
            case BALL -> executeBall(delivery, context, casterId, phaseId, instance);
            case ORB -> executeOrb(delivery, context, casterId, phaseId, instance);
        }
    }

    // ───────────────────────── PROJECTILE ─────────────────────────

    private void executeProjectile(ProjectileDelivery delivery, ActionContext context,
                                    UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof Entity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;
        // origin：优先用 delivery 覆盖（标量值解释为沿发射方向前移的格数）；否则回退眼睛位置（原行为）。
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);
        float speedMult = profile.projectileSpeedMultiplier(skillCtx);
        int extra = profile.additionalProjectiles(skillCtx);
        extra += chanceExtraProjectiles(caster);
        int pierce = profile.pierceCount(skillCtx);
        int chain = profile.chainCount(skillCtx);
        // 「X% chance for Lightning Skills to Chain an additional time」：lightning 技能生成时按几率 +1 链预算。
        // 与 chanceExtraProjectiles 同构：enterMerged 并入 {projectile} 作用域使 EFF（GateExpr {projectile}）命中；
        // lightning 由代码判定（context.skill() 阶段树含 lightning DamageAction），不依赖 stat 作用域标签。
        // 即便 supportChain=0 也会评估：lightning 技能因此可能凭此词缀独立获得一次链。
        if (caster instanceof LivingEntity living && isLightningSkill(context.skill())) {
            try (AutoCloseable ignored = StatScopes.enterMerged(living, StatContext.of(Set.of(GateTag.PROJECTILE), null))) {
                double chance = living.getAttributeValue(AttributeRegistry.CHAIN_CHANCE);
                if (chance > 0 && living.getRandom().nextDouble() < chance) chain += 1;
            } catch (Exception e) {
                // 作用域异常保护：不影响既有链行为
            }
        }
        // 分裂次数：辅助宝石 forkCount 与 stat 分裂几率取 max（chanceFork 命中返回 1，即「分裂成 2 个」）。
        int fork = Math.max(profile.forkCount(skillCtx), chanceFork(caster));
        // 「分裂时 X% 几率额外 1 个投射物」：仅在本轮确定会分裂时掷骰（fork<=0 时零开销，未点该词条无副作用）。
        if (fork > 0) fork += chanceForkExtra(caster);
        boolean circle = profile.fireInCircle(skillCtx);
        boolean returnToCaster = profile.returnToCaster(skillCtx);
        float perSplit = profile.projectileDamageMultiplierPerSplit(skillCtx);
        int barrage = profile.barrageCount(skillCtx);
        int count = 1 + extra;
        int volleys = 1 + barrage;

        // 引用计数 = 全部齐射的投射物总数（弹幕会生成多轮齐射）
        int totalProjectiles = volleys * count;
        SkillPhaseInstanceTracker.register(casterId, phaseId, instance);
        for (int i = 1; i < totalProjectiles; i++) {
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }

        // 速度求值必须带技能上下文：否则预设声明的 $speed 参数槽（SkillRead.vars 注入）读不到。
        float effectiveSpeed = (float) delivery.speed().getFloat(skillCtx) * speedMult;

        // 弹幕：每轮齐射绕垂直轴旋转固定角度，形成扩散弹幕（barrage=0 时退化为单轮，与旧行为一致）
        Vec3 perpAxis = (Math.abs(direction.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0))
                .cross(direction).normalize();
        for (int v = 0; v < volleys; v++) {
            Vec3 volleyDir = rotateAround(direction, perpAxis, v * BARAGE_STEP);
            fireVolley(level, delivery, casterId, phaseId, origin, volleyDir, caster,
                    effectiveSpeed, pierce, chain, fork, returnToCaster, perSplit, count, circle);
        }
        // 发射音画提示（B4：投射物此前零音效）；施放点迸发一小簇能量 + 轻响，客户端自动渲染。
        PersistentDeliveryManager.launchCue(level, origin);
    }

    /**
     * 发射一轮齐射（count 个投射物，扇形或圆形展开）。
     */
    private void fireVolley(ServerLevel level, ProjectileDelivery delivery, UUID casterId, UUID phaseId,
                            Vec3 origin, Vec3 volleyDir, Entity caster,
                            float effectiveSpeed, int pierce, int chain, int fork,
                            boolean returnToCaster, float perSplit, int count, boolean circle) {
        if (count == 1) {
            spawnProjectile(level, delivery, casterId, phaseId, origin, volleyDir, caster,
                    0, effectiveSpeed, pierce, chain, fork, returnToCaster, 1.0f, perSplit);
            return;
        }
        if (circle) {
            // 圆形发射（Fire in a Circle）：所有额外投射物在绕方向轴的圆锥面上均匀分布
            Vec3 axis = Math.abs(volleyDir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 perp = axis.cross(volleyDir).normalize();
            Vec3 coneBase = rotateAround(volleyDir, perp, CONE_ANGLE);
            for (int i = 0; i < count; i++) {
                Vec3 dir = rotateAround(coneBase, volleyDir, 2 * Math.PI * i / count);
                spawnProjectile(level, delivery, casterId, phaseId, origin, dir, caster,
                        0, effectiveSpeed, pierce, chain, fork, returnToCaster, 1.0f, perSplit);
            }
        } else {
            // 扇形展开：以方向为轴，绕垂直轴均匀分布
            Vec3 axis = Math.abs(volleyDir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 perp = axis.cross(volleyDir).normalize();
            double step = SPREAD_TOTAL / (count - 1);
            double start = -SPREAD_TOTAL / 2;
            for (int i = 0; i < count; i++) {
                Vec3 dir = rotateAround(volleyDir, perp, start + step * i);
                spawnProjectile(level, delivery, casterId, phaseId, origin, dir, caster,
                        0, effectiveSpeed, pierce, chain, fork, returnToCaster, 1.0f, perSplit);
            }
        }
    }

    /** 绕任意单位轴旋转向量（Rodrigues 旋转公式）。 */
    private static Vec3 rotateAround(Vec3 v, Vec3 axis, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        Vec3 cross = axis.cross(v);
        double dot = axis.dot(v);
        return v.scale(cos).add(cross.scale(sin)).add(axis.scale(dot * (1 - cos))).normalize();
    }

    /**
     * 生成单个投射物并注册其运行时上下文。
     * @param effectiveSpeed  实际飞行速度（已含辅助宝石速度倍率）
     * @param returnToCaster  命中后是否返回施法者
     * @param damageMultiplier 该投射物携带的伤害倍率（分裂降伤后的值）
     * @param splitDamageMultiplier 每次分裂的降伤比例，供后续 Fork 分裂使用
     */
    public static SkillEntity spawnProjectile(ServerLevel level, ProjectileDelivery delivery, UUID casterId, UUID phaseId,
                                             Vec3 origin, Vec3 direction, Entity caster, int depth,
                                             float effectiveSpeed, int supportPierce, int supportChain, int supportFork,
                                             boolean returnToCaster, float damageMultiplier, float splitDamageMultiplier) {
        // 形态装配（引用 + 覆写渲染 / 名称）：缺失 / 非契约载体则跳过生成，不静默产出无形态的投射物
        EntityRead read = delivery.ref();
        if (!(caster instanceof LivingEntity livingCaster)) return null;
        Entity spawned = EntitySpawns.spawn(level, livingCaster, read, 0, 0, origin, casterId, phaseId);
        if (spawned == null) return null;
        if (!(spawned instanceof SkillEntity projectile)) {
            CraftOfExile.LOGGER.error("Projectile 投递：实体类型 {} 不是 SkillEntity 载体", read.ref());
            return null;
        }
        projectile.setNoGravity(true); // 由 FlightPattern 控制运动，禁用原版重力
        projectile.noPhysics = false;  // 投射物需物理碰撞（BlockHit 反弹 / 命中墙判定），覆盖 configure 按 motion 推导的 noPhysics

        Vec3 velocity = delivery.flight().initialVelocity(direction, effectiveSpeed);
        projectile.setDeltaMovement(velocity);

        DeliveryRuntimeContext ctx = new DeliveryRuntimeContext(
                casterId, phaseId, delivery, origin, direction,
                0, 0, 0, 0, depth, supportPierce, supportChain, supportFork,
                returnToCaster, false, damageMultiplier, splitDamageMultiplier, List.of()
        );
        DeliveryTracker.register(projectile.getUUID(), ctx);
        return projectile;
    }

    /**
     * 在给定位置分裂出 forkCount 个额外投射物（对应 PoE 的 Fork 辅助）。
     * 分裂出的投射物 supportFork 置 0，避免再次分裂导致指数爆炸；
     * 但仍继承穿透 / 连锁 / 返回能力，并应用一次分裂降伤。
     * 每分裂一个都向阶段实例引用计数 +1，
     * 保证阶段在所有分裂投射物销毁前不会提前结束。
     */
    public static void spawnForks(ServerLevel level, ProjectileDelivery delivery, UUID casterId, UUID phaseId,
                                  Vec3 origin, Vec3 baseDir, Entity caster, int depth,
                                  float effectiveSpeed, int supportPierce, int supportChain, int forkCount,
                                  float perSplit, float damageMultiplier) {
        if (forkCount <= 0) return;
        Vec3 axis = Math.abs(baseDir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 perp = axis.cross(baseDir).normalize();
        double step = forkCount > 1 ? FORK_SPREAD_TOTAL / (forkCount - 1) : 0;
        double start = -FORK_SPREAD_TOTAL / 2;
        float childDamage = damageMultiplier * perSplit;
        for (int i = 0; i < forkCount; i++) {
            Vec3 dir = rotateAround(baseDir, perp, start + step * i);
            spawnProjectile(level, delivery, casterId, phaseId, origin, dir, caster, depth,
                    effectiveSpeed, supportPierce, supportChain, 0,
                    false, childDamage, perSplit);
            SkillPhaseInstanceTracker.retain(casterId, phaseId);
        }
    }

    // ───────────────────────── THROWN ─────────────────────────

    /** 投掷物（手雷）扇形展开总角度（弧度），与投射物齐射同口径。 */
    private static final double THROWN_SPREAD_TOTAL = Math.toRadians(20);

    /**
     * 投掷物执行器：生成手雷弹体（抛物线飞出 → 落地 → 引信 → 范围引爆）。
     * 本方法只放弹体，不定义任何效果；效果由技能阶段 actions 在引爆时点燃 ON_HIT。
     */
    private void executeThrown(ProjectileDelivery delivery, ActionContext context,
                               UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);

        // 引爆半径：基础半径 × 辅助宝石/技能自身的 grenade 半径倍率
        double radius = delivery.radius().orElse(ExpressionCodec.of(2.5))
                .getFloat(skillCtx) * profile.radiusMultiplier("grenade", skillCtx);

        // 手雷范围加成：INC_GRENADE_AREA_OF_EFFECT（标签纯 {grenade}，读 EFFECT_AREA）。
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.GRENADE), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));

        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Thrown 投递：未找到实体定义 {}", read.ref());
            return;
        }

        float speed = (float) delivery.speed().getFloat(skillCtx)
                * profile.projectileSpeedMultiplier(skillCtx);
        float arc = (float) delivery.arc().orElse(ExpressionCodec.of(0.35)).getFloat(skillCtx);
        int count = 1 + profile.additionalProjectiles(skillCtx);

        Vec3 axis = Math.abs(direction.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 perp = axis.cross(direction).normalize();
        for (int i = 0; i < count; i++) {
            double angle = count == 1 ? 0.0 : -THROWN_SPREAD_TOTAL / 2 + THROWN_SPREAD_TOTAL * i / (count - 1);
            spawnThrown(level, caster, casterId, phaseId, origin,
                    count == 1 ? direction : rotateAround(direction, perp, angle),
                    (float) radius, lifetimeTicks, speed, arc, read, def);
        }
        PersistentDeliveryManager.launchCue(level, origin);
    }

    /** 生成手雷弹体并装配（owner / 阶段 / 存活上限 / 引爆半径 / 形态）。 */
    private static void spawnThrown(ServerLevel level, LivingEntity caster, UUID casterId, UUID phaseId,
                                    Vec3 origin, Vec3 direction, float radius, int lifetimeTicks,
                                    float speed, float arc, EntityRead read, EntityDef def) {
        Entity thrown = EntitySpawns.spawn(level, caster, read, lifetimeTicks, radius, origin, casterId, phaseId);
        if (thrown == null) return;
        thrown.setDeltaMovement(direction.scale(speed).add(0.0, speed * arc, 0.0));
    }

    // ───────────────────────── BALL ─────────────────────────

    /**
     * 球执行器：生成缓慢飞行、本体不命中、周期放能的球。
     * 本方法只放球，不定义任何效果；效果由技能阶段 actions 经 ON_HIT 点燃。
     */
    private void executeBall(ProjectileDelivery delivery, ActionContext context,
                             UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Vec3 direction = (context.direction() != null && context.direction() != Vec3.ZERO
                ? context.direction()
                : caster.getLookAngle()).normalize();
        Vec3 origin = delivery.origin()
                .map(v -> caster.getEyePosition().add(direction.scale(v.getValue(skillCtx))))
                .orElseGet(caster::getEyePosition);

        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Ball 投递：未找到实体定义 {}", read.ref());
            return;
        }

        // 脉冲半径：基础半径 × 辅助宝石的投射物范围倍率，再经 {projectile} 作用域的 EFFECT_AREA 放大。
        double radius = delivery.radius().orElse(ExpressionCodec.of(1.8)).getFloat(skillCtx)
                * profile.radiusMultiplier("projectile", skillCtx);
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.PROJECTILE), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        int lifetimeTicks = Math.max(1, delivery.lifetime().getInt(skillCtx));
        float speed = (float) delivery.speed().getFloat(skillCtx)
                * profile.projectileSpeedMultiplier(skillCtx);

        spawnBall(level, caster, casterId, phaseId, origin, direction, (float) radius, lifetimeTicks, speed, read, def);
    }

    /** 生成球并装配（owner / 阶段 / 存活上限 / 脉冲半径 / 形态）。 */
    private static void spawnBall(ServerLevel level, LivingEntity caster, UUID casterId, UUID phaseId,
                                  Vec3 origin, Vec3 direction, float radius, int lifetimeTicks,
                                  float speed, EntityRead read, EntityDef def) {
        Entity ball = EntitySpawns.spawn(level, caster, read, lifetimeTicks, radius, origin, casterId, phaseId);
        if (ball == null) return;
        ball.setDeltaMovement(direction.scale(speed));
        PersistentDeliveryManager.launchCue(level, origin);
    }

    // ───────────────────────── ORB ─────────────────────────

    /**
     * Orb 执行器：在目标位置生成原地驻场、按节奏周期放能的球。
     * 本方法只放球，不定义任何效果；效果由技能阶段 actions 经 ON_HIT 点燃。
     */
    private void executeOrb(ProjectileDelivery delivery, ActionContext context,
                            UUID casterId, UUID phaseId, SkillPhaseInstance instance) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel level)) return;

        SupportProfile profile = SupportProfile.from(context);
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;

        // 落点：取上下文位置（瞄准点/地面目标），缺省回退施法者位置。
        Vec3 center = context.position() != null && context.position() != Vec3.ZERO
                ? context.position() : caster.position();

        // 作用半径：基础半径 × 辅助宝石范围倍率 × {area} 作用域的 EFFECT_AREA × 回响加成
        double radius = delivery.radius().orElse(ExpressionCodec.of(3.6)).getFloat(skillCtx)
                * profile.radiusMultiplier("area", skillCtx);
        double areaMult = AilmentHelper.withScope(caster, Set.of(GateTag.AREA), AttributeRegistry.EFFECT_AREA);
        if (instance != null) {
            areaMult += instance.echoAreaInc();
            radius *= instance.echoAreaMore();
        }
        radius *= areaMult;

        // 首类「法术时长」：驻场球存在时长按 SPELL_DURATION_MULT 缩放。
        double spellDurationMult = AilmentHelper.spellDurationMultiplier(caster);
        int lifetimeTicks = Math.max(1, (int) (delivery.lifetime().getInt(skillCtx) * spellDurationMult));
        int pulseTicks = Math.max(0, delivery.pulseInterval().orElse(ExpressionCodec.of(80.0)).getInt(skillCtx));

        EntityRead read = delivery.ref();
        EntityDef def = read.resolve().orElse(null);
        if (def == null) {
            CraftOfExile.LOGGER.error("Orb 投递：未找到实体定义 {}", read.ref());
            return;
        }

        // 数量上限走通用设施「按实体类别的数量上限」：以形态 id（def.type()）为类别。
        if (!EntityCategoryLimit.canSpawn(caster, level, def.type())) return;

        Entity orb = EntitySpawns.spawn(level, caster, read, lifetimeTicks, (float) radius, center, casterId, phaseId);
        if (orb == null) return;
        if (orb instanceof SkillEntity entity && pulseTicks > 0) {
            entity.putParam(EntityBehavior.PARAM_PULSE_INTERVAL, pulseTicks);
        }
        PersistentDeliveryManager.launchCue(level, center);
    }
}
