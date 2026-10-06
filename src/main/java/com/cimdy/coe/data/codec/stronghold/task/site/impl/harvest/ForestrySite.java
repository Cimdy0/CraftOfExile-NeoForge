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

public class ForestrySite extends AreaStorageSite {
    public static final Codec<ForestrySite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ForestrySite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.CREAKING_HEART);

    public ForestrySite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                           Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                           int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static ForestrySite create(BlockPos workPos) {
        return new ForestrySite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.forestry"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.forestry.description");
    }
    @Override public boolean canSetAssign() {
        return true;
    }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.FORESTRY_SITE; }
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
        ).apply(instance, ForestrySite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), ForestrySite::getSitePos,
                maxWorkersStreamCodec(), ForestrySite::getMaxWorkers,
                autoAssignStreamCodec(), ForestrySite::isAutoAssign,
                priorityStreamCodec(), ForestrySite::getPriority,
                workPositionsStreamCodec(), ForestrySite::getWorkerPositions,
                posStreamCodec(), ForestrySite::getPosA, posStreamCodec(), ForestrySite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), ForestrySite::getMaxStorage,
                StorageSite.storageStreamCodec(), ForestrySite::getStorages,
                ForestrySite::new
        );
    }
}