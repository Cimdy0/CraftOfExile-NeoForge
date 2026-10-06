package com.cimdy.coe.entity.goal.stronghold.impl;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.entity.goal.stronghold.goal.StrongholdGoal;
import com.cimdy.coe.entity.goal.stronghold.behavior.StrongholdPriority;
import com.cimdy.coe.entity.goal.stronghold.goal.WorkGoalEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class StoreExcessGoal extends StrongholdGoal {
    public static final WorkGoalEntry ENTRY = WorkGoalEntry.of(StrongholdPriority.WORK, StoreExcessGoal::new);

    public StoreExcessGoal(Mob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && !this.hold.holdState().activeTasks().containsKey(this.mob.getStringUUID())
                && !mob.getData(AttachmentRegistry.ENTITY_DATA).stacks().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canUse()
                && !this.hold.holdState().activeTasks().containsKey(this.mob.getStringUUID())
                && !mob.getData(AttachmentRegistry.ENTITY_DATA).stacks().isEmpty();
    }

    @Override
    public void tick() {
        StrongholdEntity data = mob.getData(AttachmentRegistry.ENTITY_DATA);
        if (data.stacks().isEmpty()) return;

        // 简单实现：尝试存放每一个堆
        ItemStack stack = data.stacks().getFirst();
        ItemResource resource = ItemResource.of(stack);

        // 1. 查找终端内是否有需求
        BlockPos target = findDemandTarget(resource);
        if (target == null) {
            // 2. 没有需求，就近找个容器
            target = findNearestContainer();
        }
        if (target == null) return;

        // 注视目标
        mob.getLookControl().setLookAt(Vec3.atCenterOf(target));

        // 移动
        mob.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, 1.0);
        double d = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (d < 6.0D * 6.0D) {  // distanceToSqr 返回平方距离，6.0 格阈值应为 36.0
            // 尝试存入，使用 CapacityHelper
            int leftover = CapacityHelper.insertIntoContainer((ServerLevel) mob.level(), target, stack);
            if (leftover < stack.getCount()) {
                // 成功存入了一部分
                int inserted = stack.getCount() - leftover;
                // 播放音效
                mob.level().playSound(null, target, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.7f, 0.8f);
                // 从背包中移除对应数量
                ItemStack toRemove = stack.copy();
                toRemove.setCount(inserted);
                CapacityHelper.removeItem(mob, toRemove);
            }
        }
    }

    private BlockPos findDemandTarget(ItemResource resource) {
        var demands = hold.holdState().transferResult().promotableDemands();
        for (var demand : demands) {
            if (demand.resource().equals(resource)) {
                return demand.containerPos();
            }
        }
        return null;
    }

    private BlockPos findNearestContainer() {
        BlockPos mobPos = mob.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-10, -10, -10), mobPos.offset(10, 10, 10))) {
            BlockEntity be = mob.level().getBlockEntity(pos);
            if (be != null && be.hasData(AttachmentRegistry.STOCK_SET)) {
                return pos.immutable();
            }
        }
        return null;
    }
}