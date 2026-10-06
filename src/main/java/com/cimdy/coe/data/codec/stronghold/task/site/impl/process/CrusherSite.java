package com.cimdy.coe.data.codec.stronghold.task.site.impl.process;

import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertType;
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
 * 粉碎站点（参考模拟殖民地 / 幻兽帕鲁的石料加工）。
 * 以活塞为标记方块（原用炼药锅，但炼药锅已被挤奶站点占用），将绑定容器中的石料逐级粉碎或回收为构成物（均为无原版配方的自定义变换，
 * 由 {@code ConvertType.CRUSHER} 承载，走与堆肥/研磨同构的「需求/供应 → 生成链 → 搬运/转化/搬运」管线）。
 */
public class CrusherSite extends AreaStorageSite {
    public static final Codec<CrusherSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, CrusherSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.PISTON);

    public CrusherSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                      Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                      int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static CrusherSite create(BlockPos workPos) {
        return new CrusherSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.crusher"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.crusher.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.CRUSHER_SITE; }
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
        ).apply(instance, CrusherSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), CrusherSite::getSitePos,
                maxWorkersStreamCodec(), CrusherSite::getMaxWorkers,
                autoAssignStreamCodec(), CrusherSite::isAutoAssign,
                priorityStreamCodec(), CrusherSite::getPriority,
                workPositionsStreamCodec(), CrusherSite::getWorkerPositions,
                posStreamCodec(), CrusherSite::getPosA, posStreamCodec(), CrusherSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), CrusherSite::getMaxStorage,
                StorageSite.storageStreamCodec(), CrusherSite::getStorages,
                CrusherSite::new
        );
    }
}
