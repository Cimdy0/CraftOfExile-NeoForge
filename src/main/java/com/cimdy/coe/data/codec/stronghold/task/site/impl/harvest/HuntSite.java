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
 * 狩猎站点（参考模拟殖民地 Cowboy / 幻兽帕鲁 Gathering）。
 * 以弩箭工作台为标记方块，在设定的工作区域内狩猎动物获取掉落物。
 */
public class HuntSite extends AreaStorageSite {
    public static final Codec<HuntSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, HuntSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.FLETCHING_TABLE);

    public HuntSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                    Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                    int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static HuntSite create(BlockPos workPos) {
        return new HuntSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.hunt"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.hunt.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.HUNT_SITE; }
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
        ).apply(instance, HuntSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), HuntSite::getSitePos,
                maxWorkersStreamCodec(), HuntSite::getMaxWorkers,
                autoAssignStreamCodec(), HuntSite::isAutoAssign,
                priorityStreamCodec(), HuntSite::getPriority,
                workPositionsStreamCodec(), HuntSite::getWorkerPositions,
                posStreamCodec(), HuntSite::getPosA, posStreamCodec(), HuntSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), HuntSite::getMaxStorage,
                StorageSite.storageStreamCodec(), HuntSite::getStorages,
                HuntSite::new
        );
    }
}
