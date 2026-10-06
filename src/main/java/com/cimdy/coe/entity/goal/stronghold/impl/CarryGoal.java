package com.cimdy.coe.entity.goal.stronghold.impl;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.CarryStage;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.TaskGoal;
import com.cimdy.coe.entity.goal.stronghold.helper.interaction.ContainerInteractionHandler;
import com.cimdy.coe.stronghold.transport.TransportService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class CarryGoal extends TaskGoal {
    public static final StrongholdBehavior ENTRY = StrongholdBehavior.of(StrongholdPriority.CARRY, CarryGoal::new);

    protected CarryTask carryTask;
    private CarryStage stage = CarryStage.GO_TO_SOURCE;
    private boolean differentTerminals;
    private boolean remotePickup; // true：实体在目标终端，需远程取货；false：实体在源终端，本地取货

    public CarryGoal(Mob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        if(!super.canUse() || !(this.firstTask instanceof CarryTask carryTask)) return false;
        this.carryTask = carryTask;
        this.stage = carryTask.getStage();
        return true;
    }

    @Override
    public void start() {
        this.differentTerminals = !carryTask.getSourceTerminal().equals(carryTask.getTargetTerminal());
        remotePickup = terminalPos.equals(carryTask.getTargetTerminal());
        if(!this.carryTask.isStarted()) {
            if (differentTerminals && remotePickup) {
                stage = CarryStage.GO_TO_OTHER_TERMINAL;
            } else {
                stage = CarryStage.GO_TO_SOURCE;
            }
            this.carryTask.setStarted(true);
            this.setStage(stage);
        }
    }

    @Override
    public void tick() {
        super.tick();
        ServerLevel level = (ServerLevel) mob.level();
        BlockPos targetPos = getTargetPos();
        if (targetPos == null) return;
        mob.getLookControl().setLookAt(Vec3.atCenterOf(targetPos));
        switch (stage) {
            case GO_TO_SOURCE, GO_TO_TARGET -> handleTransferTick(level, targetPos);
            case GO_TO_OTHER_TERMINAL, RETURN_OTHER_TERMINAL -> handleTeleportTick(level);
        }
    }

    // ========== 容器交互 ==========
    private void handleTransferTick(ServerLevel level, BlockPos targetPos) {
        if (getInteractionState() == ContainerInteractionHandler.InteractionState.IDLE) {
            startContainerInteraction(targetPos, (lvl) -> executeTransferAction(targetPos));
        }
        if (tickContainerInteraction(level)) {
            advanceStage();
        }
    }

    private void executeTransferAction(BlockPos targetPos) {
        if (stage == CarryStage.GO_TO_SOURCE) {
            executeExtract(targetPos);
        } else {
            executeInsert(targetPos);
        }
    }

    // ========== 阶段推进 ==========
    private void advanceStage() {
        CarryStage next = null;
        switch (stage) {
            case GO_TO_SOURCE -> {
                if (differentTerminals) {
                    next = remotePickup ? CarryStage.RETURN_OTHER_TERMINAL : CarryStage.GO_TO_OTHER_TERMINAL;
                } else {
                    next = CarryStage.GO_TO_TARGET;
                }
            }
            case GO_TO_TARGET -> {
                if (differentTerminals) {
                    if (remotePickup) {
                        this.setTaskCompleted();
                        return;
                    } else {
                        next = CarryStage.RETURN_OTHER_TERMINAL;
                    }
                } else {
                    this.setTaskCompleted();
                    return;
                }
            }
        }
        if (next != null) {
            setStage(next);
        }
    }

    private void setStage(CarryStage newStage) {
        this.stage = newStage;
        carryTask.setStage(newStage);
        this.updateTask();
    }

    // ========== 传送阶段 ==========
    private void handleTeleportTick(ServerLevel level) {
        BlockPos targetTerminal = TransportService.terminalToMoveTo(carryTask, stage, terminalPos, remotePickup);
        if (moveToInteractionBlock(targetTerminal)) {
            // 只在第一次传送（GO_TO_OTHER_TERMINAL）时扣除双倍能量
            if (stage == CarryStage.GO_TO_OTHER_TERMINAL) {
                if (!TransportService.tryDeductTeleportEnergy(level, carryTask)) {
                    // 能量不足，终止任务
                    this.setTaskCompleted();
                    return;
                }
            }
            // 执行传送
            teleport(TransportService.teleportDestination(carryTask, stage, remotePickup));

            // 传送后切换阶段
            TransportService.stageAfterTeleport(stage, remotePickup)
                    .ifPresentOrElse(this::setStage, this::setTaskCompleted);
        }
    }

    // ========== 目标位置计算 ==========
    private BlockPos getTargetPos() {
        return switch (stage) {
            case GO_TO_SOURCE -> carryTask.getSourcePos();
            case GO_TO_TARGET -> carryTask.getTargetPos();
            case GO_TO_OTHER_TERMINAL, RETURN_OTHER_TERMINAL ->
                    TransportService.terminalToMoveTo(carryTask, stage, terminalPos, remotePickup);
        };
    }

    private void executeExtract(BlockPos sourcePos) {
        ItemResource required = carryTask.getResource();
        int needAmount = carryTask.getAmount();
        int availableCapacity = CapacityHelper.getRemainingCapacity(mob);
        int canCarry = Math.min(needAmount, availableCapacity);
        if (canCarry <= 0) return;

        ItemStack toExtract = required.toStack(canCarry);
        ItemStack extracted = CapacityHelper.extractFromContainer((ServerLevel) mob.level(), sourcePos, toExtract);
        if (extracted.isEmpty()) return;

        int leftover = CapacityHelper.tryAddItem(mob, extracted);
        if (leftover > 0) {
            ItemStack leftoverStack = extracted.copy();
            leftoverStack.setCount(leftover);
            CapacityHelper.insertIntoContainer((ServerLevel) mob.level(), sourcePos, leftoverStack);
        }

        carryTask.setExtracted(true);
        this.updateTask();

        spawnItemParticles(sourcePos, extracted, 6);
    }

    private void executeInsert(BlockPos targetPos) {
        ItemResource required = carryTask.getResource();
        int totalCarried = getCarriedAmount(required);
        if (totalCarried <= 0) return;

        ItemStack toInsert = CapacityHelper.extractItems(mob, required, totalCarried);
        if (toInsert.isEmpty()) return;

        int leftover = CapacityHelper.insertIntoContainer((ServerLevel) mob.level(), targetPos, toInsert);
        if (leftover > 0) {
            ItemStack leftoverStack = toInsert.copy();
            leftoverStack.setCount(leftover);
            CapacityHelper.addItem(mob, leftoverStack);
        }
        spawnItemParticles(targetPos, toInsert, 6);
    }

    private void teleport(GlobalPos destination) {
        ServerLevel targetLevel = mob.level().getServer().getLevel(destination.dimension());
        if (targetLevel == null) return;
        Vec3 targetVec = new Vec3(
                destination.pos().getX() + 0.5,
                destination.pos().getY() + 1.0,
                destination.pos().getZ() + 0.5);
        TeleportTransition transition = new TeleportTransition(targetLevel, targetVec,
                Vec3.ZERO, mob.getXRot(), mob.getYRot(), TeleportTransition.DO_NOTHING);
        mob.teleport(transition);
    }

    private void spawnItemParticles(BlockPos pos, ItemStack stack, int count) {
        Level level = mob.level();
        if (level instanceof ServerLevel serverLevel) {
            ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, stack.getItem());
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.2;
            double z = pos.getZ() + 0.5;
            for (int i = 0; i < count; i++) {
                double dx = (serverLevel.getRandom().nextDouble() - 0.5) * 0.4;
                double dy = serverLevel.getRandom().nextDouble() * 0.4;
                double dz = (serverLevel.getRandom().nextDouble() - 0.5) * 0.4;
                serverLevel.sendParticles(option, x, y, z, 1, dx, dy, dz, 0.02);
            }
        }
    }
}