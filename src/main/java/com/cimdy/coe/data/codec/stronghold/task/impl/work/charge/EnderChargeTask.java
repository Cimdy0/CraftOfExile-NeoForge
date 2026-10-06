package com.cimdy.coe.data.codec.stronghold.task.impl.work.charge;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.process.EnderChargeSite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Predicate;

public class EnderChargeTask extends WorkTask {
    public static final Codec<EnderChargeTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EnderChargeTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = EnderChargeSite.predicate();

    @Override
    public Class<? extends Site> requiredSiteType() {
        return EnderChargeSite.class;
    }
    public EnderChargeTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public EnderChargeTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    /** 末影充能为纯能量类任务，不依赖物品存储容器，跳过物品容量门控。 */
    @Override
    public boolean needsItemStorage() {
        return false;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new EnderChargeTask(site.getSitePos(), needed);
    }

    @Override
    public boolean canBeAssignedTo(LivingEntity entity) {
        return super.canBeAssignedTo(entity) &&
                (entity.getType() == EntityTypes.ENDERMAN || entity.getType() == EntityTypes.ENDERMITE);
    }

    @Override
    public WorkTask createSubTask(int workers) {
        return new EnderChargeTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.ENDER_CHARGE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    @Override public double getHungerCostPerTick() {
        return 0.01;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(EnderChargeTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(EnderChargeTask::getPendingWorkers)
        ).apply(instance, EnderChargeTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), EnderChargeTask::getOptionNext,
                BlockPos.STREAM_CODEC, EnderChargeTask::getWorkPos,
                ByteBufCodecs.INT, EnderChargeTask::getPendingWorkers,
                EnderChargeTask::new
        );
    }
}