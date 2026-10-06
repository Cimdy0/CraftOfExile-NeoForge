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
 * 采矿站点（参考模拟殖民地 Miner / 幻兽帕鲁 Mining）。
 * 以制图台为标记方块（原用锻造台，但锻造台已被数据驱动的配方站点接管），在设定的工作区域内开采石头与矿石。
 */
public class MiningSite extends AreaStorageSite {
    public static final Codec<MiningSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.CARTOGRAPHY_TABLE);

    public MiningSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                      Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                      int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static MiningSite create(BlockPos workPos) {
        return new MiningSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.mining"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.mining.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.MINING_SITE; }
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
        ).apply(instance, MiningSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), MiningSite::getSitePos,
                maxWorkersStreamCodec(), MiningSite::getMaxWorkers,
                autoAssignStreamCodec(), MiningSite::isAutoAssign,
                priorityStreamCodec(), MiningSite::getPriority,
                workPositionsStreamCodec(), MiningSite::getWorkerPositions,
                posStreamCodec(), MiningSite::getPosA, posStreamCodec(), MiningSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), MiningSite::getMaxStorage,
                StorageSite.storageStreamCodec(), MiningSite::getStorages,
                MiningSite::new
        );
    }
}
