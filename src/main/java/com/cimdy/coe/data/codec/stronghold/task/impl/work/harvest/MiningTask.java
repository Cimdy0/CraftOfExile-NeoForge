package com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest;

import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest.MiningSite;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 采矿任务：在工作区域内开采可被镐开采的方块（石头、矿石等）。
 */
public class MiningTask extends WorkTask {
    public static final Codec<MiningTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningTask> STREAM_CODEC;
    public static final Predicate<BlockState> BLOCK_PREDICATE = MiningSite.SITE_BLOCK_PREDICATE;

    @Override
    public Class<? extends Site> requiredSiteType() {
        return MiningSite.class;
    }

    public MiningTask(BlockPos workPos, int pendingWorkers) {
        this(Optional.empty(), workPos, pendingWorkers);
    }
    public MiningTask(Optional<Task> next, BlockPos workPos, int pendingWorkers) {
        super(next, workPos, pendingWorkers);
    }

    @Override
    public Predicate<BlockState> getPredicate() {
        return BLOCK_PREDICATE;
    }

    public static Task generateTask(ServerLevel level, Site site) {
        AABB area = site.getWorkArea().map(RangeAB::toAABB).orElse(new AABB(site.getSitePos()));
        if (!hasMineable(level, area)) return null;
        if (!hasPickaxeAvailable(level, site)) return null;
        int max = site.getMaxWorkers();
        int current = site.getAssignedWorker();
        int needed = (max <= 0) ? 1 : (max - current);
        if (needed <= 0) return null;
        return new MiningTask(site.getSitePos(), needed);
    }

    public static boolean hasMineable(ServerLevel level, AABB area) {
        return BlockPos.betweenClosedStream(
                        (int) area.minX, (int) area.minY, (int) area.minZ,
                        (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .anyMatch(pos -> isMineable(level.getBlockState(pos)));
    }

    public static boolean isMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static boolean hasPickaxeAvailable(ServerLevel level, Site site) {
        for (BlockPos containerPos : site.getStorages()) {
            if (!level.isLoaded(containerPos)) continue;
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, containerPos, null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (resource.toStack(1).is(net.neoforged.neoforge.common.Tags.Items.MINING_TOOL_TOOLS)) return true;
            }
        }
        return false;
    }

    @Override public WorkTask createSubTask(int workers) {
        return new MiningTask(workPos, workers);
    }
    @Override public Type<?> getType() { return TaskTypes.MINING; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(MiningTask::getWorkPos),
                Codec.INT.fieldOf("pending_workers").forGetter(MiningTask::getPendingWorkers)
        ).apply(instance, MiningTask::new));
        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), MiningTask::getOptionNext,
                BlockPos.STREAM_CODEC, MiningTask::getWorkPos,
                ByteBufCodecs.INT, MiningTask::getPendingWorkers,
                MiningTask::new
        );
    }
}
