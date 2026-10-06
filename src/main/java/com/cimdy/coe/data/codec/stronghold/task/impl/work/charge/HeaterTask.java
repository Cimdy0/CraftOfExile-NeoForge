package com.cimdy.coe.data.codec.stronghold.task.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.BasicTask;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.Optional;

public class HeaterTask extends BasicTask.Pos {
    public static final Codec<HeaterTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, HeaterTask> STREAM_CODEC;
    public HeaterTask(BlockPos workPos) {
        this(Optional.empty(), workPos);
    }

    public HeaterTask(Optional<Task> next, BlockPos workPos) {
        super(next, workPos);
    }

    @Override
    public boolean validate(ServerLevel level) {
        return level.isLoaded(getWorkPos()) && level.getBlockEntity(getWorkPos()) instanceof AbstractFurnaceBlockEntity;
    }
    @Override
    public boolean canBeAssignedTo(LivingEntity entity) {
        return super.canBeAssignedTo(entity) && entity.getType() == EntityTypes.STRIDER;
    }
    @Override public double getHungerCostPerTick() { return 0.005; } // 轻微消耗饥饿度
    @Override public Type<?> getType() { return TaskTypes.HEATER; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() {
        return STREAM_CODEC;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("pos").forGetter(HeaterTask::getWorkPos)
        ).apply(instance, HeaterTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), HeaterTask::getOptionNext,
                BlockPos.STREAM_CODEC, HeaterTask::getWorkPos,
                HeaterTask::new
        );
    }
}