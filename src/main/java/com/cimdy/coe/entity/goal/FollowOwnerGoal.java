package com.cimdy.coe.entity.goal;

import com.cimdy.coe.entity.goal.stronghold.goal.StrongholdGoal;
import com.cimdy.coe.event.world.StrongholdEvent;
import com.cimdy.coe.stronghold.energy.WorkerAIInjector;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 盟友（据点捕获实体里被玩家显式标记为「盟友」的那部分，帕鲁式随行伙伴）的随行 AI：
 * 跟住主人，距离过远时传送追上。
 *
 * <p>背景：盟友不走 {@code WorkerAIInjector.injectStrongholdAI}——那套 AI 里 {@code StrongholdGoal} /
 * {@code WanderingGoal} 会把实体拴在据点范围内，与「随行」语义直接冲突；但也不能什么都不注入，
 * 否则只剩原版 {@code RandomStrollGoal}，盟友会自己溜达直至走丢。本 Goal 填补的正是这一层：
 * 只负责「跟人」，实体的原版目标（战斗 / 游走）与阵营索敌（现由 {@code CampAllianceMixin} 经基类
 * {@code canAttack} 门控，不再依赖 inject 进 targetSelector 的索敌 Goal）全部保留。
 *
 * <p>让位规则（战斗优先）：{@link #canUse()} 与 {@link #canContinueToUse()} 在
 * {@code mob.getTarget() != null} 时一律退出，且本 Goal 只占 MOVE / LOOK 两个控制位。
 * 因此原版 {@code MeleeAttackGoal}（优先级 2–4）与 {@link MobSkillCastGoal}（优先级 1）一旦可用，
 * 会经 {@code GoalSelector} 抢占并停掉本 Goal；战斗目标清空后下一 tick 自动恢复跟随。
 *
 * <p>防走失：距离超过 {@link #TELEPORT_DIST} 时不再寻路，直接传送到主人附近的安全落点
 * （同原版 {@code TamableAnimal}：主人周围随机取点 + {@link PathType#WALKABLE} 校验 + 碰撞盒校验 +
 * 建筑高度校验）。落点全部校验通过才落地，不会把盟友塞进方块或送进虚空。
 *
 * <p>不可达处理：寻路 / 传送连续失败 {@link #MAX_FAILURES} 次后进入 {@link #RETRY_COOLDOWN} tick
 * 冷却，避免在「主人在封闭空间里」这类场景下每 tick 反复起停（不是死循环，只是退避重试）。
 *
 * <p>作用域：仅在服务端构造与生效——{@code WorkerAIInjector.spawnPlayerEntity} 与
 * {@code StrongholdEvent} 两条注入路径都跑在服务端，且 {@code GoalSelector} 本身只在
 * {@code Mob#serverAiStep} 推进。
 */
public class FollowOwnerGoal extends Goal {
    /** 开始跟随的距离（格）：只有超过这个距离才起步，避免贴脸时反复起步/停下。 */
    public static final double FOLLOW_START = 8.0;
    /** 停止跟随的距离（格）：走到这个距离内即交还控制权，由原版游走目标接管。 */
    public static final double FOLLOW_STOP = 3.0;
    /** 传送阈值（格）：超过这个距离直接传送，不再尝试寻路——防止被地形卡住或拉下后永久走失。 */
    public static final double TELEPORT_DIST = 64.0;
    /** 移动速度 = 实体自身移速属性 × 本系数（1.0，仅作调节位，不为此新增属性）。 */
    private static final double SPEED_SCALE = 1.0;

    /** 传送落点尝试上限（与原版 {@code TamableAnimal} 一致）。 */
    private static final int TELEPORT_TRIES = 10;
    /** 传送落点相对主人的水平半径（格）。 */
    private static final int TELEPORT_RADIUS = 3;
    /** 传送落点相对主人的垂直偏移（格）。 */
    private static final int TELEPORT_V_OFFSET = 1;
    /** 重算路径的间隔（tick）；经 {@code adjustedTickDelay} 后实际减半，与原版 FollowOwnerGoal 口径一致。 */
    private static final int RECALC_INTERVAL = 10;
    /** 连续寻路 / 传送失败达此次数后放弃本轮跟随。 */
    private static final int MAX_FAILURES = 3;
    /** 放弃本轮跟随后的退避时长（tick）。 */
    private static final int RETRY_COOLDOWN = 60;

    private final Mob mob;
    private final PathNavigation navigation;
    /** 飞行单位允许落在空中（{@link PathType#OPEN}），地面单位必须落在可站立的 {@link PathType#WALKABLE}。 */
    private final boolean canFly;

    private LivingEntity owner;
    private int timeToRecalcPath;
    private int failures;
    private int cooldown;
    private float oldWaterCost;

    public FollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.navigation = mob.getNavigation();
        this.canFly = this.navigation instanceof FlyingPathNavigation;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (this.failures >= MAX_FAILURES) {
            // 上一轮连续失败：退避一段时间再试，避免不可达时每 tick 起停空转
            this.failures = 0;
            this.cooldown = RETRY_COOLDOWN;
            return false;
        }
        LivingEntity owner = this.resolveOwner();
        if (owner == null) return false;
        if (this.mob.distanceToSqr(owner) <= FOLLOW_START * FOLLOW_START) return false;
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.failures >= MAX_FAILURES) return false;
        LivingEntity owner = this.owner;
        if (owner == null || !owner.isAlive() || owner.level() != this.mob.level()) return false;
        // 有战斗目标时让位；同时 mob 被载具 / 玩家骑乘时不再自己走
        if (this.mob.getTarget() != null || this.mob.isPassenger()) return false;
        if (this.navigation.isDone()) return false;
        return this.mob.distanceToSqr(owner) > FOLLOW_STOP * FOLLOW_STOP;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        // 跟随途中允许涉水：临时抹掉水的寻路惩罚，避免被一条河彻底挡住（原版 FollowOwnerGoal 同款处理）
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        LivingEntity owner = this.owner;
        if (owner == null) return;
        if (--this.timeToRecalcPath > 0) return;
        this.timeToRecalcPath = this.adjustedTickDelay(RECALC_INTERVAL);

        boolean ok;
        if (this.mob.distanceToSqr(owner) > TELEPORT_DIST * TELEPORT_DIST) {
            ok = this.teleportAround(owner);
        } else {
            ok = this.navigation.moveTo(owner,
                    this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_SCALE);
        }
        this.failures = ok ? 0 : this.failures + 1;
    }

    /**
     * 解析当前主人：主人离线 / 跨维度 / 已死亡 / 处于旁观模式，或盟友正被骑乘、已有战斗目标时，
     * 一律返回 {@code null}（视为「此刻不该跟随」）。离线与跨维度都落到同一个 {@code null} 上，
     * 只是一次 {@code UUID → Entity} 的 O(1) 查表，不会空转更不会 NPE。
     */
    private LivingEntity resolveOwner() {
        if (this.mob.isPassenger()) return null;
        if (this.mob.getTarget() != null) return null;
        if (!(OwnerHelper.getOwner(this.mob) instanceof LivingEntity owner)) return null;
        if (!owner.isAlive() || owner.isSpectator()) return null;
        if (owner.level() != this.mob.level()) return null;
        return owner;
    }

    /** 在主人周围随机取一个安全落点并传送过去；十次都找不到就放弃本轮。 */
    private boolean teleportAround(LivingEntity owner) {
        BlockPos center = owner.blockPosition();
        RandomSource random = this.mob.getRandom();
        for (int attempt = 0; attempt < TELEPORT_TRIES; attempt++) {
            int dx = random.nextIntBetweenInclusive(-TELEPORT_RADIUS, TELEPORT_RADIUS);
            int dz = random.nextIntBetweenInclusive(-TELEPORT_RADIUS, TELEPORT_RADIUS);
            // 至少水平留出 2 格，避免直接叠在主人身上
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
            int dy = random.nextIntBetweenInclusive(-TELEPORT_V_OFFSET, TELEPORT_V_OFFSET);
            BlockPos dest = center.offset(dx, dy, dz);
            if (!this.canTeleportTo(dest)) continue;
            this.mob.snapTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5,
                    this.mob.getYRot(), this.mob.getXRot());
            // snapTo 不清零动量与下落距离：若盟友是在坠落途中触发传送（追人跳崖等），
            // 累积的 fallDistance 会一直带到落地的那一 tick 结算，在主人身边凭空摔伤。
            // 此处对齐原版 Entity#teleportTo 的处理。
            this.mob.resetFallDistance();
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.navigation.stop();
            return true;
        }
        return false;
    }

    /**
     * 落点校验：① 在建筑高度内（排除虚空与建筑上限之外，界外取 PathType 不可靠）；
     * ② {@code WALKABLE}（脚下有可站立方块，且已排除水 / 岩浆 / 无支撑的空气）；
     * ③ 盟友碰撞盒平移到落点后不与任何方块相交。
     */
    private boolean canTeleportTo(BlockPos pos) {
        Level level = this.mob.level();
        if (!level.isInsideBuildHeight(pos)) return false;
        PathType type = WalkNodeEvaluator.getPathTypeStatic(this.mob, pos);
        if (type != PathType.WALKABLE && !(this.canFly && type == PathType.OPEN)) return false;
        BlockPos delta = pos.subtract(this.mob.blockPosition());
        return level.noCollision(this.mob, this.mob.getBoundingBox().move(delta));
    }
}
