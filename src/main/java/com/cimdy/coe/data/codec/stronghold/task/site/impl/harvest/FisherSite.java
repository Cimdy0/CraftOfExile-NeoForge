package com.cimdy.coe.data.codec.stronghold.task.site.impl.harvest;

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
 * 钓鱼站点（参考模拟殖民地 Fisherman / 幻兽帕鲁 水域采集）。
 * 以海晶灯为标记方块（原用木桶，但木桶已被农业站点的 Tags.Blocks.BARRELS 覆盖），在工作区域内水源处获取鱼类。
 */
public class FisherSite extends AreaStorageSite {
    public static final Codec<FisherSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FisherSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.SEA_LANTERN);

    public FisherSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                      Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                      int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static FisherSite create(BlockPos workPos) {
        return new FisherSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.fish"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.fish.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.FISHER_SITE; }
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
        ).apply(instance, FisherSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), FisherSite::getSitePos,
                maxWorkersStreamCodec(), FisherSite::getMaxWorkers,
                autoAssignStreamCodec(), FisherSite::isAutoAssign,
                priorityStreamCodec(), FisherSite::getPriority,
                workPositionsStreamCodec(), FisherSite::getWorkerPositions,
                posStreamCodec(), FisherSite::getPosA, posStreamCodec(), FisherSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), FisherSite::getMaxStorage,
                StorageSite.storageStreamCodec(), FisherSite::getStorages,
                FisherSite::new
        );
    }
}
