package com.cimdy.coe.data.codec.stronghold.task.impl;

import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.TransportTask;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;

public class EatTask extends BasicTask.ItemTask implements TransportTask{
    public static final Codec<EatTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EatTask> STREAM_CODEC;
    private final BlockPos containerPos;
    private int eatCount;

    public EatTask(ItemCount item, BlockPos containerPos) {
        this(Optional.empty(), item, containerPos, 0);
    }

    public EatTask(Optional<Task> next, ItemCount item, BlockPos containerPos, int eatCount) {
        super(next, item);
        this.containerPos = containerPos;
        this.eatCount = eatCount;
    }

    @Override public boolean validate(ServerLevel level) {
        ResourceHandler<ItemResource> sourceHandler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
        if (sourceHandler == null) return false;
        return StrongholdHelper.countItemsInContainer(sourceHandler, item.resource()) >= item.amount() - eatCount;
    }

    public void addCount(int i) {
        this.eatCount += i;
    }
    public BlockPos getContainerPos() { return containerPos; }
    public int getEatCount() { return eatCount; }
    @Override public int getVolume() { return getItem().amount(); }
    @Override public BlockPos getTargetPositionForEntity(LivingEntity entity) { return containerPos; }
    public ItemResource getResource() { return item.resource(); }
    public int getAmount() { return item.amount() - eatCount; }
    @Override public BlockPos getSourceContainerPos() { return containerPos; }
    @Override public BlockPos getTargetContainerPos() { return null; }  // 无目标容器
    @Override public boolean isBeforePickup() { return true; }
    @Override public double getHungerCostPerTick() {
        return 0.0;
    }
    @Override public boolean canFallback() {
        return false;
    }
    @Override public boolean canBeAssignedTo(LivingEntity entity) {
        return StrongholdHelper.getHunger(entity) <= 0;
    }
    @Override public Type<?> getType() { return TaskTypes.EAT; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() {
        return STREAM_CODEC;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                itemCodec(),
                BlockPos.CODEC.fieldOf("container").forGetter(EatTask::getContainerPos),
                Codec.INT.fieldOf("eat_count").forGetter(EatTask::getEatCount)
        ).apply(instance, EatTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), EatTask::getOptionNext,
                itemStreamCodec(), EatTask::getItem,
                BlockPos.STREAM_CODEC, EatTask::getContainerPos,
                ByteBufCodecs.INT, EatTask::getEatCount,
                EatTask::new
        );
    }
}
