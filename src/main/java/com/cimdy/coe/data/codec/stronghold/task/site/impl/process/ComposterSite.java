package com.cimdy.coe.data.codec.stronghold.task.site.impl.process;

import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.AreaStorageSite;
import com.cimdy.coe.data.codec.stronghold.task.site.basic.StorageSite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 堆肥站点（参考模拟殖民地 Composter / 幻兽帕鲁 农业副产）。
 * 以堆肥桶为标记方块，将绑定容器中的有机废料堆肥为骨粉。
 */
public class ComposterSite extends AreaStorageSite {
    public static final Codec<ComposterSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ComposterSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.COMPOSTER);

    public ComposterSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                         Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                         int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static ComposterSite create(BlockPos workPos) {
        return new ComposterSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.compost"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.compost.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.COMPOSTER_SITE; }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                sitePosCodec(),
                maxWorkersCodec(),
                autoAssignCodec(),
                priorityCodec(),
                workerPositionsCodec(),
                posACodec(), posBCodec(),
                StorageSite.maxStorageBlocksCodec(),
                StorageSite.storageBlocksCodec()
        ).apply(instance, ComposterSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), ComposterSite::getSitePos,
                maxWorkersStreamCodec(), ComposterSite::getMaxWorkers,
                autoAssignStreamCodec(), ComposterSite::isAutoAssign,
                priorityStreamCodec(), ComposterSite::getPriority,
                workPositionsStreamCodec(), ComposterSite::getWorkerPositions,
                posStreamCodec(), ComposterSite::getPosA, posStreamCodec(), ComposterSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), ComposterSite::getMaxStorage,
                StorageSite.storageStreamCodec(), ComposterSite::getStorages,
                ComposterSite::new
        );
    }
}
