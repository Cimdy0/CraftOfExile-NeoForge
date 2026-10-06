package com.cimdy.coe.data.codec.stronghold.task.site.impl.process;

import com.cimdy.coe.entity.goal.stronghold.impl.work.misc.EnchantGoal;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.EnchantType;
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
 * 附魔站点（参考模拟殖民地 / 幻兽帕鲁的附魔工作类型）。
 * 以附魔台为标记方块，是「使用附魔书给物品附魔」作业的场所：
 * 工人把附魔书 / 目标物品 / 青金石搬到站点存储，由 {@code EnchantGoal} 消耗三者与末影能量产出附魔物品，
 * 走与堆肥 / 研磨 / 粉碎同构的「需求/供应 → 生成链 → 搬运/附魔/搬运」管线（{@code EnchantType} 承载）。
 */
public class EnchanterSite extends AreaStorageSite {
    public static final Codec<EnchanterSite> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchanterSite> STREAM_CODEC;
    public static final Predicate<BlockState> SITE_BLOCK_PREDICATE = state -> state.is(Blocks.ENCHANTING_TABLE);

    public EnchanterSite(BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
                         Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
                         int max, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB, max, storageBlocks);
    }

    public static EnchanterSite create(BlockPos workPos) {
        return new EnchanterSite(workPos, 1, true, 5, Map.of(),
                Optional.empty(), Optional.empty(), 1, List.of());
    }

    @Override public Component getName() { return Component.translatable("hold.site.coe.enchanter"); }
    @Override public Component getDescription() {
        return Component.translatable("hold.site.coe.enchanter.description");
    }
    @Override public boolean canSetAssign() { return true; }
    @Override public Predicate<BlockState> getBlockPredicate() { return SITE_BLOCK_PREDICATE; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec() { return STREAM_CODEC; }
    @Override public Type<?> getType() { return SiteTypes.ENCHANTER_SITE; }
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
        ).apply(instance, EnchanterSite::new));

        STREAM_CODEC = StreamCodec.composite(
                sitePosStreamCodec(), EnchanterSite::getSitePos,
                maxWorkersStreamCodec(), EnchanterSite::getMaxWorkers,
                autoAssignStreamCodec(), EnchanterSite::isAutoAssign,
                priorityStreamCodec(), EnchanterSite::getPriority,
                workPositionsStreamCodec(), EnchanterSite::getWorkerPositions,
                posStreamCodec(), EnchanterSite::getPosA, posStreamCodec(), EnchanterSite::getPosB,
                StorageSite.maxStorageBlocksStreamCodec(), EnchanterSite::getMaxStorage,
                StorageSite.storageStreamCodec(), EnchanterSite::getStorages,
                EnchanterSite::new
        );
    }
}
