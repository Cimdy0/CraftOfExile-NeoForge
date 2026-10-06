package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.FisherSite;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 钓鱼任务：在工作区域内寻找水源，垂钓获取鱼类。
 */
public class FisherTask extends WorkTask {
    public static final Codec<FisherTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FisherTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = FisherSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return FisherSite.class;
    }

    public FisherTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public FisherTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasWater(level, area)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new FisherTask(site.getSitePos(), needed);
    }

    public static boolean hasWater(ServerLevel level, AABB area) {
        return BlockPos.betweenClosedStream(
                        (int) area.minX, (int) area.minY, (int) area.minZ,
                        (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .anyMatch(pos -> level.getFluidState(pos).is(FluidTags.WATER));
    }

    @Override public WorkTask createSubTask(int workers) {
        return new FisherTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.FISHER; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(FisherTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(FisherTask::getPendingWorkers)
        ).apply(instance, FisherTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), FisherTask::getOptionNext,
                BlockPos.STREAM_CODEC, FisherTask::getWorkPos,
                ByteBufCodecs.INT, FisherTask::getPendingWorkers,
                FisherTask::new
        );
    }
}
