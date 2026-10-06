package com.cimdy.coe.entity.goal.stronghold.impl;

import com.cimdy.coe.stronghold.area.StrongholdArea;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.entity.goal.stronghold.goal.StrongholdGoal;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class WanderingGoal extends StrongholdGoal {
    public static final StrongholdBehavior ENTRY = StrongholdBehavior.of(StrongholdPriority.WANDER, WanderingGoal::new);

    private static final int MAX_WANDER_RANGE = 12;
    private static final int MIN_WANDER_DISTANCE = 3;
    private static final double SPEED_WALK = 0.7;

    private static final int SAN_REGEN_INTERVAL = 60;
    private static final double SAN_REGEN_AMOUNT = 0.5;
    private int sanRegenTimer = 0;

    private static final int MIN_IDLE_TIME = 10;
    private static final int MAX_IDLE_TIME = 30;
    private static final int MIN_LOOK_TIME = 60;
    private static final int MAX_LOOK_TIME = 120;
    private static final int MIN_FOLLOW_TIME = 100;
    private static final int MAX_FOLLOW_TIME = 250;
    private static final int MIN_CHASE_TIME = 80;
    private static final int MAX_CHASE_TIME = 160;

    private final RandomSource random() {
        return this.mob.getRandom();
    }
    private int remainingTime = 0;
    private WanderingState state = WanderingState.IDLE;

    private Vec3 targetPosition = null;
    private BlockPos lookTarget = null;
    private UUID followTargetUUID = null;

    private int stuckTimer = 0;
    private Vec3 lastPosition = null;

    // 用于注视行为：降低随机转头频率，只在注视目标变化时更新
    private int lookChangeTimer = 0;
    private static final int LOOK_CHANGE_INTERVAL = 40;

    private enum WanderingState {
        IDLE, WALKING, LOOKING, FOLLOWING, CHASING
    }

    public WanderingGoal(Mob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !this.hold.holdState().activeTasks().containsKey(this.mob.getStringUUID());
    }

    @Override
    public void start() {
        this.remainingTime = 0;
        this.state = WanderingState.IDLE;
        this.targetPosition = null;
        this.followTargetUUID = null;
        this.stuckTimer = 0;
        this.lastPosition = null;
        this.lookChangeTimer = 0;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (++sanRegenTimer >= SAN_REGEN_INTERVAL) {
            sanRegenTimer = 0;
            StrongholdHelper.modifySan(this.mob, SAN_REGEN_AMOUNT);
        }

        if (state == WanderingState.WALKING || state == WanderingState.FOLLOWING || state == WanderingState.CHASING) {
            Vec3 currentPos = this.mob.position();
            if (lastPosition != null && currentPos.distanceToSqr(lastPosition) < 0.01) {
                stuckTimer++;
            } else {
                stuckTimer = 0;
            }
            lastPosition = currentPos;
            if (stuckTimer > 60) {
                startIdle();
                return;
            }
        } else {
            stuckTimer = 0;
            lastPosition = null;
        }

        if (state == WanderingState.IDLE || state == WanderingState.LOOKING) {
            if (--remainingTime <= 0) {
                selectNextBehavior();
            }
        }

        switch (state) {
            case WALKING -> tickWalking();
            case LOOKING -> tickLooking();
            case FOLLOWING -> tickFollowing();
            case CHASING -> tickChasing();
        }
    }

    private void tickWalking() {
        if (targetPosition == null) { startIdle(); return; }
        PathNavigation nav = this.mob.getNavigation();
        if (nav.isDone() || this.mob.distanceToSqr(targetPosition) < 2.0) {
            startIdle();
        }
    }

    private void tickLooking() {
        if (lookChangeTimer > 0) {
            lookChangeTimer--;
        } else {
            lookChangeTimer = LOOK_CHANGE_INTERVAL;
            if (lookTarget != null && random().nextFloat() < 0.3) {
                lookTarget = null;
            }
        }

        if (lookTarget != null) {
            this.mob.getLookControl().setLookAt(
                    lookTarget.getX() + 0.5, lookTarget.getY() + 0.5, lookTarget.getZ() + 0.5,
                    30.0F, (float) this.mob.getMaxHeadXRot());
        } else {
            this.mob.getLookControl().setLookAt(
                    this.mob.getX() + (random().nextDouble() - 0.5) * 2,
                    this.mob.getEyeY() + (random().nextDouble() - 0.5) * 1,
                    this.mob.getZ() + (random().nextDouble() - 0.5) * 2,
                    10.0F, (float) this.mob.getMaxHeadXRot());
        }
    }

    private void tickFollowing() {
        if (followTargetUUID == null) { startIdle(); return; }
        ServerLevel level = (ServerLevel) this.mob.level();
        Entity target = level.getEntity(followTargetUUID);
        if (target == null || !target.isAlive()) { startIdle(); return; }
        double dist = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (dist < 3.0) {
            if (--remainingTime <= 0) startIdle();
        } else if (dist > 256.0) {
            startIdle();
        } else {
            this.mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
        }
    }

    private void tickChasing() {
        if (!this.mob.isBaby()) { startIdle(); return; }
        if (followTargetUUID == null) { startIdle(); return; }
        ServerLevel level = (ServerLevel) this.mob.level();
        Entity entityTarget = level.getEntity(followTargetUUID);
        if (!(entityTarget instanceof Mob targetMob) || !targetMob.isAlive() || !targetMob.isBaby()) {
            startIdle();
            return;
        }

        double dist = this.mob.distanceToSqr(targetMob.getX(), targetMob.getY(), targetMob.getZ());
        if (dist < 2.0) {
            if (this.mob.onGround()) {
                this.mob.getJumpControl().jump();
            }
            Vec3 away = this.mob.position().subtract(targetMob.position()).normalize().scale(0.3);
            this.mob.getNavigation().moveTo(this.mob.getX() + away.x, this.mob.getY(), this.mob.getZ() + away.z, SPEED_WALK);
        } else if (dist < 64.0) {
            this.mob.getNavigation().moveTo(targetMob.getX(), targetMob.getY(), targetMob.getZ(), SPEED_WALK);
        } else {
            startIdle();
        }
        if (--remainingTime <= 0) startIdle();
    }

    // ---------- 行为概率调整 ----------
    private void selectNextBehavior() {
        float roll = random().nextFloat();
        // 发呆 5%
        if (roll < 0.05) {
            startIdle();
        } else if (roll < 0.45) { // 40% 散步
            startWalking();
        } else if (roll < 0.65) { // 20% 注视
            startLooking();
        } else if (roll < 0.85) { // 20% 跟随
            startFollowing();
        } else { // 15% 追逐（幼体）或散步
            if (this.mob.isBaby() && random().nextFloat() < 0.7) {
                startChasing();
            } else {
                startFollowing();
            }
        }
    }

    private void startIdle() {
        state = WanderingState.IDLE;
        remainingTime = MIN_IDLE_TIME + random().nextInt(MAX_IDLE_TIME - MIN_IDLE_TIME);
        this.mob.getNavigation().stop();
        this.targetPosition = null;
        this.followTargetUUID = null;
        this.stuckTimer = 0;
        this.lastPosition = null;
    }

    private void startWalking() {
        BlockPos center = terminalPos.pos();
        for (int tries = 0; tries < 10; tries++) {
            int dx = random().nextInt(MAX_WANDER_RANGE * 2) - MAX_WANDER_RANGE;
            int dz = random().nextInt(MAX_WANDER_RANGE * 2) - MAX_WANDER_RANGE;
            BlockPos ground = findGround(center.offset(dx, 0, dz));
            if (ground != null && insideHoldArea(ground)) {
                if (Math.sqrt(ground.distSqr(this.mob.blockPosition())) >= MIN_WANDER_DISTANCE) {
                    targetPosition = Vec3.atBottomCenterOf(ground);
                    this.mob.getNavigation().moveTo(targetPosition.x, targetPosition.y, targetPosition.z, SPEED_WALK);
                    state = WanderingState.WALKING;
                    return;
                }
            }
        }
        startIdle();
    }

    private void startLooking() {
        state = WanderingState.LOOKING;
        remainingTime = MIN_LOOK_TIME + random().nextInt(MAX_LOOK_TIME - MIN_LOOK_TIME);
        lookTarget = null;
        lookChangeTimer = 0; // 首次立即设定视线
        // 优先注视同类或容器
        if (random().nextBoolean() && !hold.entities().isEmpty()) {
            ServerLevel serverLevel = (ServerLevel) this.mob.level();
            hold.entities().stream().skip(random().nextInt(hold.entities().size())).findFirst().ifPresent(uuid -> {
                Entity e = serverLevel.getEntity(uuid);
                if (e != null) lookTarget = e.blockPosition();
            });
        }
        if (lookTarget == null && terminalPos != null) {
            for (int i = 0; i < 5; i++) {
                BlockPos pos = terminalPos.pos().offset(random().nextInt(10) - 5, 0, random().nextInt(10) - 5);
                if (this.mob.level().getBlockEntity(pos) != null) { lookTarget = pos; break; }
            }
        }
    }

    private void startFollowing() {
        state = WanderingState.FOLLOWING;
        remainingTime = MIN_FOLLOW_TIME + random().nextInt(MAX_FOLLOW_TIME - MIN_FOLLOW_TIME);
        followTargetUUID = null;
        var group = StrongholdHelper.getHoldGroupAt(this.mob.level(), this.terminalPos.pos());
        if (group != null && random().nextFloat() < 0.6) {
            for (UUID uuid : hold.entities()) {
                if (group.owners().stream().anyMatch(o -> o.uuid().equals(uuid))) {
                    followTargetUUID = uuid;
                    break;
                }
            }
        }
        if (followTargetUUID == null) {
            ServerLevel serverLevel = (ServerLevel) this.mob.level();
            List<UUID> candidates = hold.entities().stream()
                    .filter(uuid -> {
                        var e = serverLevel.getEntity(uuid);
                        return e instanceof Mob && e.getType() == this.mob.getType() && !((Mob) e).isBaby() && !uuid.equals(this.mob.getUUID());
                    }).toList();
            if (!candidates.isEmpty()) followTargetUUID = candidates.get(random().nextInt(candidates.size()));
        }
        if (followTargetUUID == null) startIdle();
    }

    private void startChasing() {
        if (!this.mob.isBaby()) { startIdle(); return; }
        state = WanderingState.CHASING;
        remainingTime = MIN_CHASE_TIME + random().nextInt(MAX_CHASE_TIME - MIN_CHASE_TIME);
        followTargetUUID = null;
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        List<UUID> babies = hold.entities().stream()
                .filter(uuid -> {
                    var e = serverLevel.getEntity(uuid);
                    return e instanceof Mob && e.getType() == this.mob.getType() && ((Mob) e).isBaby() && !uuid.equals(this.mob.getUUID());
                }).toList();
        if (!babies.isEmpty()) followTargetUUID = babies.get(random().nextInt(babies.size()));
        else startIdle();
    }

    private BlockPos findGround(BlockPos pos) {
        Level level = this.mob.level();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int y = pos.getY() + 5; y >= pos.getY() - 5; y--) {
            mut.set(pos.getX(), y, pos.getZ());
            if (level.getBlockState(mut).isSolid() &&
                    level.getBlockState(mut.above()).isAir() &&
                    level.getBlockState(mut.above(2)).isAir()) {
                return mut.immutable();
            }
        }
        return null;
    }

    /** 工人游荡范围 = 据点的不规则绑定区块集合。 */
    private boolean insideHoldArea(BlockPos pos) {
        return com.cimdy.coe.stronghold.area.StrongholdArea.contains(this.hold, this.terminalPos.pos(), pos);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
        this.state = WanderingState.IDLE;
        this.targetPosition = null;
        this.followTargetUUID = null;
        this.stuckTimer = 0;
        this.lastPosition = null;
    }
}