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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.Optional;

public class FEChargeTask extends BasicTask.Pos {
    public static final Codec<FEChargeTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FEChargeTask> STREAM_CODEC;

    public FEChargeTask(BlockPos workPos) {
        this(Optional.empty(), workPos);
    }

    public FEChargeTask(Optional<Task> next, BlockPos workPos) {
        super(next, workPos);
    }

    @Override public boolean validate(ServerLevel level) {
        if (!level.isLoaded(getWorkPos())) return false;
        BlockEntity be = level.getBlockEntity(getWorkPos());
        if (be == null) return false;
        EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, getWorkPos(), null);
        return energy != null && energy.getAmountAsInt() < energy.getCapacityAsInt();
    }

    @Override
    public boolean canBeAssignedTo(LivingEntity entity) {
        return entity instanceof Creeper creeper && creeper.isPowered();
    }

    @Override public Type<?> getType() {
        return TaskTypes.FE_CHARGE;
    }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() {
        return STREAM_CODEC;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(FEChargeTask::getWorkPos)
        ).apply(instance, FEChargeTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), FEChargeTask::getOptionNext,
                BlockPos.STREAM_CODEC, FEChargeTask::getWorkPos,
                FEChargeTask::new
        );
    }
}
