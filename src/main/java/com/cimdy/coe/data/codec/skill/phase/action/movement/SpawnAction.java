package com.cimdy.coe.data.codec.skill.phase.action.movement;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.data.helper.engine.entity.AdditionalSpawn;
import com.cimdy.coe.data.helper.engine.entity.EntityCategoryLimit;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.data.codec.entity.EntityDefs;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.entity.custom.skill.totem.BallistaTotem;
import com.cimdy.coe.entity.custom.skill.totem.TrapEntity;
import com.cimdy.coe.entity.goal.FactionDefendGoal;
import com.cimdy.coe.entity.goal.FollowOwnerGoal;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.helper.OwnerHelper;
import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 生成实体动作 — 在目标位置生成图腾或召唤物。
 *
 * <ul>
 *   <li><b>图腾</b>（entity_id = coe:skill_totem）：生成 {@link SkillTotem} 驻场施法体，
 *       持有 (casterId, phaseId) 并 retain 阶段实例，周期性点燃阶段的 ON_TICK 触发攻击，
 *       攻击结算完整继承施法者的 SupportProfile（辅助宝石修正全部生效）。</li>
 *   <li><b>召唤物</b>（其他 entity_id，如 minecraft:zombie）：生成生物并标记
 *       {@code Camp.PLAYER} 阵营（与敌对生物相互敌对、不攻击玩家，见 TargetEvent），
 *       duration &gt; 0 时限时存在（到期由 SummonExpiry 检查移除）。</li>
 * </ul>
 */
public record SpawnAction(
        Identifier entityId,
        ExpressionCodec.Value count,
        Optional<ExpressionCodec.Value> radius,
        ExpressionCodec.Value duration,
        ExpressionCodec.Value interval,
        ExpressionCodec.Value attackRange,
        ExpressionCodec.Value health,
        Optional<Boolean> companion
) implements Action {
    /** 召唤物到期时间在实体附件中的键（游戏刻绝对时间）。 */
    public static final String MINION_EXPIRE_KEY = "coe_minion_expire";

    public static final Codec<SpawnAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("entity_id").forGetter(SpawnAction::entityId),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("count", ExpressionCodec.of(1.0))
                    .forGetter(SpawnAction::count),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius").forGetter(SpawnAction::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(200.0))
                    .forGetter(SpawnAction::duration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("interval", ExpressionCodec.of(20.0))
                    .forGetter(SpawnAction::interval),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("attack_range", ExpressionCodec.of(8.0))
                    .forGetter(SpawnAction::attackRange),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("health", ExpressionCodec.of(20.0))
                    .forGetter(SpawnAction::health),
            Codec.BOOL.optionalFieldOf("companion").forGetter(SpawnAction::companion)
    ).apply(i, SpawnAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.SPAWN; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        int n = Math.max(1, (int) count.getFloat(skillCtx));
        float spread = radius.map(v -> v.getFloat(skillCtx)).orElse(0f);
        int lifetime = Math.max(1, (int) duration.getFloat(skillCtx));

        boolean isCompanion = companion.orElse(false);
        // 数量上限池由通用设施解析：已登记类别（图腾族 / 球 / 预留 fissure 等）走各自的池，
        // 未登记的实体（普通生物等）回落到「临时召唤物」池；同伴恒走同伴池。
        // 类别身份全由 Identifier 承载 —— 本类不写任何类别名字符串，新增类别只改登记一行。
        Identifier category = isCompanion ? EntityCategoryLimit.COMPANION
                : EntityCategoryLimit.categoryFor(entityId, EntityCategoryLimit.MINION);

        for (int i = 0; i < n; i++) {
            if (!canPlace(caster, serverLevel, category, isCompanion)) continue;
            spawnOne(serverLevel, caster, context, skillCtx,
                    spawnPos(context.position(), spread, i, n, serverLevel), lifetime, isCompanion);
            // 额外生成掷骰（通用设施：MarkerStatEffect / MarkerStatEffect，按类别标签作用域；
            // 未接线时恒 0，与既有一致。掷骰口径同 EntityBehaviors#detonate 的手雷二次激活）
            int extra = AdditionalSpawn.rollExtra(caster, category, caster.getRandom());
            for (int k = 0; k < extra; k++) {
                if (!canPlace(caster, serverLevel, category, isCompanion)) break;
                spawnOne(serverLevel, caster, context, skillCtx,
                        spawnPos(context.position(), spread, n + k, n + extra, serverLevel), lifetime, isCompanion);
            }
        }
    }

    /** 实体族判定：仅用于选取具体实体类的构造路径（与上限判定解耦，不承载任何类别语义）。 */
    private boolean isTotem() {
        return entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityRegistry.SKILL_TOTEM.get()));
    }

    /** 见 {@link #isTotem()}。 */
    private boolean isBallista() {
        return entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityRegistry.BALLISTA.get()));
    }

    /** 见 {@link #isTotem()}。 */
    private boolean isTrap() {
        return entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityRegistry.TRAP.get()));
    }

    /**
     * 见 {@link #isTotem()}：墙体（def.type == WALL 实体类型）走专属 {@link #spawnWall} 分支。
     *
     * <p>通用 {@link #spawnMinionEntity} 路径<b>不调用</b> {@code SkillEntity.configure()}，
     * 故数据驱动的墙体（其形态由 {@code coe:entity/wall_*} 定义）走该路径将无法加载 EntityDef、行为/碰撞全部失效。
     * 因此墙必须在此被识别并分派到 {@link #spawnWall}（调 configure 加载形态 + retain 阶段）。
     */
    private boolean isWall() {
        Identifier wallType = BuiltInRegistries.ENTITY_TYPE.getKey(EntityRegistry.WALL.get());
        return EntityDefs.get(entityId).map(def -> wallType.equals(def.type())).orElse(false);
    }

    /** 生成墙体实体（{@code coe:wall[_fire|_cold]}）：复用 {@link SkillEntity} 承载，调 configure 加载形态定义并 retain 阶段。 */
    private void spawnWall(ServerLevel level, LivingEntity caster, ActionContext context, SkillContext skillCtx,
                          Vec3 pos, int lifetime) {
        SkillEntity wall = new SkillEntity(EntityRegistry.WALL.get(), level);
        wall.setPos(pos.x, pos.y, pos.z);
        // 碰撞/脉冲半径 = wall_range（SpawnAction.attackRange）；默认 2.0 兜底。
        float r = (float) attackRange.getFloat(skillCtx);
        // configure 加载 coe:wall[_x] 形态（运动=Wall 真实碰撞、行为=pulse 对敌周期点燃 ON_TICK）、按半径算 AABB。
        wall.configure(caster, context.phaseId(), lifetime, r, EntityRead.of(entityId));
        // 阵营=PLAYER：墙体作为友方建筑，脉冲命中敌人的敌我判定以此为前提。
        wall.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
        // retain 阶段实例：驻场期间保证 tracker 可反查（墙销毁时 release）。
        if (context.phaseId() != null) {
            SkillPhaseInstanceTracker.retain(caster.getUUID(), context.phaseId());
        }
        level.addFreshEntity(wall);
        // 墙体计数 +1：归入 WALL 类别上限池（EntityCategoryLimit 按 coe:wall 类型统计）。
        SummonCountHelper.onSummoned(caster);
    }

    /**
     * 本次生成是否放行：先过通用「按实体类别的数量上限」（{@link EntityCategoryLimit#canSpawn}），
     * 同伴再叠加 POE2「不同类型」约束（与数量上限正交）。
     */
    private boolean canPlace(LivingEntity caster, ServerLevel level, Identifier category, boolean isCompanion) {
        if (!EntityCategoryLimit.canSpawn(caster, level, category)) return false;
        return !isCompanion || !hasCompanionOfType(caster, level, companionType());
    }

    /** 按类别生成单个实体：构造路径按实体族分派，数量上限已由 {@link #canPlace} 判定。 */
    private void spawnOne(ServerLevel level, LivingEntity caster, ActionContext context, SkillContext skillCtx,
                          Vec3 pos, int lifetime, boolean isCompanion) {
        if (isTotem()) {
            spawnTotem(level, caster, context, skillCtx, pos, lifetime);
        } else if (isBallista()) {
            spawnBallista(level, caster, context, skillCtx, pos, lifetime);
        } else if (isTrap()) {
            spawnTrap(level, caster, context, skillCtx, pos, lifetime);
        } else if (isWall()) {
            spawnWall(level, caster, context, skillCtx, pos, lifetime);
        } else {
            spawnMinion(level, caster, pos, lifetime, isCompanion);
        }
    }

    /** 目标实体是否为同伴（owner 的 IS_COMPANION 标记召唤物）。 */
    private static boolean isCompanionOf(Entity e, LivingEntity owner) {
        return e instanceof LivingEntity le
                // 原 AttachmentRegistry.IS_COMPANION 已迁入 AdditionData（key=is_companion）
                && AdditionData.getOrDefault(le, AdditionKeys.IS_COMPANION, 0.0) != 0.0
                && owner.equals(OwnerHelper.getOwner(le));
    }

    /**
     * 已在身的同伴里是否存在与 {@code newType} 同种类者（POE2「of different types」）。
     *
     * <p>与数量上限<b>正交</b>：数量由通用设施 {@link EntityCategoryLimit} 的同伴池按
     * {@code MAX_COMPANIONS} 判定，本方法只管「种类唯一」——POE2 同伴按<b>种类</b>唯一（狼 / 熊 / 鸦各一只），
     * 本模组的「种类」自然对应 {@code EntityType}，与 {@code spawnMinionEntity} 由 {@code entityId}
     * 解析出的类型同源，无需额外类型字段。
     *
     * @param newType 本次待召唤同伴的实体类型（由 {@code entityId} 解析）；无法解析时退化为仅按数量判定。
     */
    private static boolean hasCompanionOfType(LivingEntity owner, ServerLevel level, EntityType<?> newType) {
        if (newType == null) return false;
        for (Entity e : level.getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (isCompanionOf(e, owner) && e.getType() == newType) return true;
        }
        return false;
    }

    /** 本动作待召唤实体的类型（用于同伴「不同种类」判重）；无法解析时返回 null，退化为仅按数量判定。 */
    private EntityType<?> companionType() {
        return BuiltInRegistries.ENTITY_TYPE.get(entityId).map(Holder.Reference::value).orElse(null);
    }

    /** 按序均匀散布生成点（单个居中，多个绕圈）。 */
    private static Vec3 spawnPos(Vec3 center, float spread, int index, int total, ServerLevel level) {
        if (spread <= 0 || total <= 1) return center;
        double angle = 2 * Math.PI * index / total;
        return center.add(Math.cos(angle) * spread, 0, Math.sin(angle) * spread);
    }

    private void spawnTotem(ServerLevel level, LivingEntity caster, ActionContext context,
                            SkillContext skillCtx, Vec3 pos, int lifetime) {
        SkillTotem totem = new SkillTotem(EntityRegistry.SKILL_TOTEM.get(), level);
        totem.setPos(pos.x, pos.y, pos.z);
        totem.configure(caster, context.phaseId(), lifetime,
                (int) interval.getFloat(skillCtx),
                attackRange.getFloat(skillCtx),
                health.getFloat(skillCtx));
        // 图腾是 LivingEntity，会被 AttributeEvent 首次 tick 初始化——
        // 预置阵营（PLAYER，与主人同阵营）与 LEVEL 标记：
        //  ① 阵营非 NONE 是 resolvePresenceEnemies(图腾) 的敌我判定前提（图腾作为光环源/承伤判定目标）；
        //  ② 设 LEVEL 使 AttributeEvent 跳过「非玩家实体首次 tick」初始化（否则图腾被误标 MOB 阵营 + 稀有度词缀）。
        totem.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
        totem.setData(AttachmentRegistry.LEVEL, 1);
        // retain 阶段实例：驻场期间保证 tracker 可反查（totem 销毁时 release）
        if (context.phaseId() != null) {
            SkillPhaseInstanceTracker.retain(caster.getUUID(), context.phaseId());
        }
        level.addFreshEntity(totem);
        // 图腾计数 +1：addFreshEntity 之后即代表图腾已成功入世界（事件驱动，无需扫描）
        SummonCountHelper.onTotemSummoned(caster);
    }

    /** 生成箭塔实体（{@code coe:ballista}）：复用 {@link SkillTotem} 装配与周期攻击，仅实体类型不同。 */
    private void spawnBallista(ServerLevel level, LivingEntity caster, ActionContext context,
                               SkillContext skillCtx, Vec3 pos, int lifetime) {
        BallistaTotem ballista = new BallistaTotem(EntityRegistry.BALLISTA.get(), level);
        ballista.setPos(pos.x, pos.y, pos.z);
        ballista.configure(caster, context.phaseId(), lifetime,
                (int) interval.getFloat(skillCtx),
                attackRange.getFloat(skillCtx),
                health.getFloat(skillCtx));
        ballista.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
        ballista.setData(AttachmentRegistry.LEVEL, 1);
        if (context.phaseId() != null) {
            SkillPhaseInstanceTracker.retain(caster.getUUID(), context.phaseId());
        }
        level.addFreshEntity(ballista);
        // 箭塔同属图腾池，计数 +1（与普通图腾一致）。
        SummonCountHelper.onTotemSummoned(caster);
    }

    /** 生成陷阱实体（{@code coe:trap}）：复用 {@link SkillTotem} 装配与周期攻击，仅实体类型不同。 */
    private void spawnTrap(ServerLevel level, LivingEntity caster, ActionContext context,
                           SkillContext skillCtx, Vec3 pos, int lifetime) {
        TrapEntity trap = new TrapEntity(EntityRegistry.TRAP.get(), level);
        trap.setPos(pos.x, pos.y, pos.z);
        trap.configure(caster, context.phaseId(), lifetime,
                (int) interval.getFloat(skillCtx),
                attackRange.getFloat(skillCtx),
                health.getFloat(skillCtx));
        // 陷阱是 LivingEntity（同箭塔），预置阵营（PLAYER，与主人同阵营）与 LEVEL 标记：
        //  ① 阵营非 NONE 是 resolvePresenceEnemies(陷阱) 的敌我判定前提；
        //  ② 设 LEVEL 使 AttributeEvent 跳过「非玩家实体首次 tick」初始化（否则被误标 MOB 阵营 + 稀有度词缀）。
        trap.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
        trap.setData(AttachmentRegistry.LEVEL, 1);
        // retain 阶段实例：驻场期间保证 tracker 可反查（陷阱销毁时 release）
        if (context.phaseId() != null) {
            SkillPhaseInstanceTracker.retain(caster.getUUID(), context.phaseId());
        }
        level.addFreshEntity(trap);
        // 陷阱同属图腾池，计数 +1（与箭塔/普通图腾一致）。
        SummonCountHelper.onTotemSummoned(caster);
    }

    private void spawnMinion(ServerLevel level, LivingEntity caster, Vec3 pos, int lifetime,
                              boolean isCompanion) {
        // 盟友数量上限由 execute 在调用本方法前已校验（canSpawnCompanion）；此处仅做实体装配。
        spawnMinionEntity(level, caster, entityId, pos, isCompanion, lifetime);
    }

    /**
     * 生成召唤物实体（权威实现）：创建实体、写入阵营 / 召唤标记 / 归属 / AI，并加入世界与计数。
     * 初始生成（{@link #spawnMinion}）与复活重生（{@code SummonEvent} 调度）共用，保证两类路径的实体装配完全一致。
     *
     * @param lifetime 限时存在时长（tick），&gt; 0 时写入 {@code MINION_EXPIRE}（按 owner 的 MINION_DURATION 缩放）；
     *                复活重生传 0，使重生体为永久召唤物（与原被击败的永久召唤物一致）。
     */
    public static void spawnMinionEntity(ServerLevel level, LivingEntity owner, Identifier entityId,
                                         Vec3 pos, boolean isCompanion, int lifetime) {
        Optional<Holder.Reference<EntityType<?>>> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (type.isEmpty()) return;
        Entity entity = type.get().value().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) return;
        entity.setPos(pos.x, pos.y, pos.z);
        // 阵营标记：召唤物与敌对生物相互敌对、不攻击玩家阵营（TargetEvent 消费）
        entity.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
        // 召唤物标记：本方法是召唤物生成的单一权威路径，故在此写入即可覆盖全部召唤来源。
        // 缺此写入会让 EntityFlagValue.Flag.SUMMONED 与 presence_count:*.summoned 恒为假/0（死读值）；标记统一收口 AdditionData（key=summoned），与 IS_COMPANION 同构。
        AdditionData.addOrReplaceData(entity, AdditionKeys.SUMMONED, 1.0);
        if (isCompanion) {
            // 盟友=特殊召唤物：标记 IS_COMPANION（解锁 TargetSelector.COMPANION 与 presence_count:*.companion 解析），
            // 并强制为永久召唤物——跳过 MINION_EXPIRE 写入使其 == 0，从而触发 SummonEvent 的死亡复活机制。
            // 原 AttachmentRegistry.IS_COMPANION 已迁入 AdditionData（key=is_companion）
            AdditionData.addOrReplaceData(entity, AdditionKeys.IS_COMPANION, 1.0);
        } else if (lifetime > 0) {
            // INC_MINION_DURATION：在 owner 维度读 MINION_DURATION 倍率（base=1，inc 时长走 ADD_MULTIPLIED_BASE），
            // 仅缩放临时召唤物存在时长，不污染图腾（走 spawnTotem 独立路径）与永久盟友（本分支不进入）。
            double durMul = owner.getAttributeValue(AttributeRegistry.MINION_DURATION);
            entity.setData(AttachmentRegistry.MINION_EXPIRE, level.getGameTime() + (long) (lifetime * durMul));
        }
        // 归属标记：令 TargetSelector.MINION / COMPANION 源以 owner_uuid 解析到本召唤物的主人（与图腾的 caster 同源）。
        if (entity instanceof LivingEntity living) OwnerHelper.setOwner(living, owner);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            // 行为层注入（补全召唤物 AI）：原版召唤物仅注册「索敌玩家」的攻击目标，被 CampAllianceMixin 阵营门控挡下，
            // 对敌方（MOB 阵营）实体实际毫无作为。此处清空原版目标、重建为「战斗召唤」AI：
            //   - FloatGoal(0)：保留基础浮力，避免落水溺亡；
            //   - MobSkillCastGoal：由 TargetEvent 在实体入界时以优先级 1 注入（装配技能的召唤物照常施法）；
            //   - FactionDefendGoal：优先级 2（StrongholdPriority.MINION_DEFEND），阵营感知主动索敌 + 攻速缩放近战，与据点工人/盟友同构；
            //   - 盟友额外 FollowOwnerGoal(5)：帕鲁式随行伙伴（忠实映射 Companion→盟友召唤物）。
            // 优先级刻意避开 1（MobSkillCastGoal 占用槽位），故两者互不冲突、均生效。
            mob.goalSelector.removeAllGoals(g -> true);
            mob.targetSelector.removeAllGoals(g -> true);
            mob.goalSelector.addGoal(0, new FloatGoal(mob));
            if (isCompanion) {
                mob.goalSelector.addGoal(WorkerAIInjector.FOLLOW_PRIORITY, new FollowOwnerGoal(mob));
            }
            mob.goalSelector.addGoal(StrongholdPriority.MINION_DEFEND, new FactionDefendGoal(mob));
        }
        level.addFreshEntity(entity);
        // 召唤物计数 +1：走到此处必然已过 `entity == null` 早退，即实体已成功入世界
        SummonCountHelper.onSummoned(owner);
    }
}
