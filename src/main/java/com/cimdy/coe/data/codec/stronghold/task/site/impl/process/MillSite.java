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
 * 研磨站点：以砂轮为标记方块，将绑定容器中的花卉/染料原料研磨为对应染料。
 */
public class MillSite extends AreaStorageSite {
    public static final Codec<MillSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MillSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.GRINDSTONE);

    public MillSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                    Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                    int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static MillSite create(BlockPos workPos) {
        return new MillSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.mill"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.mill.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.MILL_SITE; }
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
        ).apply(instance, MillSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), MillSite::getSitePos,
                maxWorkersStreamCodec(), MillSite::getMaxWorkers,
                autoAssignStreamCodec(), MillSite::isAutoAssign,
                priorityStreamCodec(), MillSite::getPriority,
                workPositionsStreamCodec(), MillSite::getWorkerPositions,
                posStreamCodec(), MillSite::getPosA, posStreamCodec(), MillSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), MillSite::getMaxStorage,
                StorageSite.storageStreamCodec(), MillSite::getStorages,
                MillSite::new
        );
    }
}
