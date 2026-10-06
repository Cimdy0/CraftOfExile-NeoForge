package com.cimdy.coe.entity.goal.stronghold.impl;

import com.cimdy.coe.data.codec.stronghold.task.impl.EatTask;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdBehavior;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.TaskGoal;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class EatGoal extends TaskGoal {
    public static final StrongholdBehavior ENTRY = StrongholdBehavior.of(StrongholdPriority.EAT, EatGoal::new);

    private EatTask eatTask;
    private int eatCount;               // 当前已经吃掉的数量
    private int targetCount;            // 总共需要吃掉的数量
    private int stageTimer;             // 当前阶段计时器
    private EatStage stage = EatStage.APPROACHING;

    private enum EatStage {
        BURPING,        // 当前食物循环结束
        EATING,         // 吃手上的食物
        CLOSING,        // 关闭箱子
        TAKING,         // 从容器取出食物
        OPENING,        // 打开箱子
        APPROACHING     // 走向容器
    }

    public EatGoal(Mob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse() || !(this.firstTask instanceof EatTask task)) {
            return false;
        }
        this.eatTask = task;
        this.eatCount = task.getEatCount();
        this.targetCount = task.getItem().amount();
        return true;
    }

    @Override
    public void start() {
        this.stage = EatStage.APPROACHING;
    }

    @Override
    public void tick() {
        if (eatTask == null) return;

        BlockPos targetPos = eatTask.getContainerPos();

        switch (stage) {
            case APPROACHING -> {
                if (moveToInteractionBlock(targetPos)) {
                    // 开始打开箱子阶段
                    stage = EatStage.OPENING;
                    stageTimer = 10;
                    playOpenSound(targetPos);
                }
            }

            case OPENING -> {
                stageTimer--;
                if (stageTimer <= 0) {
                    // 进入取出物品阶段
                    stage = EatStage.TAKING;
                    stageTimer = 5;
                    // 取出一个食物到手上
                    tryGrabFood(targetPos);
                }
            }

            case TAKING -> {
                stageTimer--;
                if (stageTimer <= 0) {
                    // 进入关闭箱子阶段
                    stage = EatStage.CLOSING;
                    stageTimer = 10;
                    playCloseSound(targetPos);
                }
            }

            case CLOSING -> {
                stageTimer--;
                if (stageTimer <= 0) {
                    // 进入进食阶段
                    stage = EatStage.EATING;
                    stageTimer = 30;
                }
            }

            case EATING -> {
                // 吃食物的动画和音效
                this.mob.swing(InteractionHand.MAIN_HAND);
                if (this.mob.getRandom().nextInt(6) == 0) {
                    playChewingSound();
                }
                if (stageTimer % 5 == 0) {
                    spawnFoodParticles();
                }
                stageTimer--;
                if (stageTimer <= 0) {
                    // 消耗手上的食物
                    consumeHandItem();
                    clearHandItem();
                    eatCount++;
                    this.eatTask.addCount(1);
                    this.updateTask();

                    if (eatCount >= targetCount) {
                        // 进入打嗝阶段
                        stage = EatStage.BURPING;
                        stageTimer = 15;
                        playBurpSound();
                    } else {
                        // 还有食物没吃，开始下一个循环（重新打开箱子）
                        stage = EatStage.OPENING;
                        stageTimer = 10;
                        playOpenSound(targetPos);
                        clearHandItem();
                    }
                }
            }

            case BURPING -> {
                stageTimer--;
                if (stageTimer <= 0) {
                    // 进食完成
                    this.setTaskCompleted();
                }
            }
        }
    }

    // ==================== 食物交互方法 ====================
    private void tryGrabFood(BlockPos pos) {
        ItemResource required = eatTask.getItem().resource();
        ResourceHandler<ItemResource> handler = getContainerHandler(pos);
        if (handler == null) return;

        try (Transaction txn = Transaction.openRoot()) {
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource res = handler.getResource(slot);
                if (res.equals(required)) {
                    int extracted = handler.extract(slot, required, 1, txn);
                    if (extracted > 0) {
                        txn.commit();
                        this.mob.setItemSlot(EquipmentSlot.MAINHAND, required.toStack());
                        playPickupSound();
                        return;
                    }
                }
            }
        }
    }

    private void consumeHandItem() {
        ItemStack heldStack = this.mob.getItemBySlot(EquipmentSlot.MAINHAND);
        if (heldStack.isEmpty()) return;
        FoodProperties food = heldStack.get(DataComponents.FOOD);
        if (food != null) {
            StrongholdHelper.modifyHunger(this.mob, food.nutrition());
        }
    }

    private void clearHandItem() {
        this.mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    // ==================== 音效 ====================

    private void playOpenSound(BlockPos pos) {
        if (!this.mob.level().isClientSide())
            this.mob.level().playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.7F, 1.0F);
    }

    private void playCloseSound(BlockPos pos) {
        if (!this.mob.level().isClientSide())
            this.mob.level().playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.7F, 1.0F);
    }

    private void playPickupSound() {
        if (!this.mob.level().isClientSide())
            this.mob.level().playSound(null, this.mob, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.5F, 1.0F);
    }

    private void playChewingSound() {
        if (!this.mob.level().isClientSide())
            this.mob.level().playSound(null, this.mob, SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL,
                    0.6F, 0.9F + this.mob.getRandom().nextFloat() * 0.2F);
    }

    private void playBurpSound() {
        if (!this.mob.level().isClientSide())
            this.mob.level().playSound(null, this.mob.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL,
                    0.6F, 0.9F + this.mob.getRandom().nextFloat() * 0.2F);
    }

    // ==================== 粒子 ====================

    private void spawnFoodParticles() {
        ItemStack heldStack = this.mob.getItemBySlot(EquipmentSlot.MAINHAND);
        if (heldStack.isEmpty() || !(this.mob.level() instanceof ServerLevel serverLevel)) return;
        ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, heldStack.getItem());
        for (int i = 0; i < 3; i++) {
            double dx = (this.mob.getRandom().nextDouble() - 0.5) * 0.3;
            double dy = this.mob.getRandom().nextDouble() * 0.3;
            double dz = (this.mob.getRandom().nextDouble() - 0.5) * 0.3;
            serverLevel.sendParticles(option,
                    this.mob.getX() + this.mob.getRandom().nextDouble() * 0.6,
                    this.mob.getY() + this.mob.getEyeHeight(),
                    this.mob.getZ() + this.mob.getRandom().nextDouble() * 0.6,
                    1, dx, dy, dz, 0.05);
        }
    }

    // ==================== 容器访问 ====================

    private ResourceHandler<ItemResource> getContainerHandler(BlockPos pos) {
        Level level = mob.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        return level.getCapability(Capabilities.Item.BLOCK, pos, null);
    }

    @Override
    public void stop() {
        super.stop();
        clearHandItem();
    }
}