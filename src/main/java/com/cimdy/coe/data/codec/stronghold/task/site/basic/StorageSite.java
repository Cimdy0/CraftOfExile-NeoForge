package com.cimdy.coe.data.codec.stronghold.task.site.basic;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.ActionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.InteractionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.RenderEntry;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteBindingFeedback;
import com.cimdy.coe.client.InputState;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface StorageSite extends Site{
    @Override
    default InteractionResult applyAction(Level level, BlockPos lookingPos, KeyMapping key, boolean shiftKeyDown) {
        InteractionResult result = Site.super.applyAction(level, lookingPos, key, shiftKeyDown);
        if (key == InputState.INTERACT_1) {
            if (containsStorage(level, lookingPos)) {
                SiteBindingFeedback.onStorageUnbound(lookingPos);
                withRemovedStorage(lookingPos);
                result.setAction(ActionResult.TOGGLE_storage);
            } else if (isValidStorage(level, lookingPos) && !isstorageBoundToOtherSite(level, lookingPos, getSitePos())) {
                if(getStorages().size() < getMaxStorage()) {
                    SiteBindingFeedback.onStorageBound(lookingPos);
                    withAddedStorage(lookingPos);
                } else {
                    SiteBindingFeedback.onStorageReplaced(getStorages().getFirst(), lookingPos);
                    withAddedStorageAndRemovedFirst(lookingPos);
                }
                result.setAction(ActionResult.TOGGLE_storage);
            }
        }
        return result.setSite(this);
    }

    @Override
    default InteractionResult getInteractionHint(Level level, BlockPos targetPos) {
        InteractionResult result = Site.super.getInteractionHint(level, targetPos);
        if (containsStorage(level, targetPos)) {
            result.putHint(InputState.INTERACT_1,
                    Component.translatable("hold.site.coe.unbind_storage"));
            result.setAction(ActionResult.TOGGLE_storage);
        } else if (isValidStorage(level, targetPos) && !isstorageBoundToOtherSite(level, targetPos, getSitePos())) {
            if ((getStorages().size() < getMaxStorage())) {
                result.putHint(InputState.INTERACT_1,
                        Component.translatable("hold.site.coe.bind_storage"));
            }else {
                result.putHint(InputState.INTERACT_1,
                        Component.translatable("hold.site.coe.storage_full"));
            }
            result.setAction(ActionResult.TOGGLE_storage);
        }
        return result;
    }

    @Override
    default List<RenderEntry> getRenderEntries(Level level) {
        List<RenderEntry> entries = Site.super.getRenderEntries(level);
        for (BlockPos pos : getStorages()) {
            entries.add(getstorageRenderEntry(level, pos));
        }
        return entries;
    }

    default void withRemovedStorage(BlockPos lookingPos) {
        List<BlockPos> newStorageBlocks = new ArrayList<>(getStorages());
        newStorageBlocks.remove(lookingPos);
        this.setStorages(newStorageBlocks);
    }

    default void withAddedStorage(BlockPos lookingPos) {
        List<BlockPos> newStorageBlocks = new ArrayList<>(getStorages());
        newStorageBlocks.add(lookingPos);
        this.setStorages(newStorageBlocks);
    }

    default void withAddedStorageAndRemovedFirst(BlockPos lookingPos) {
        List<BlockPos> newStorageBlocks = new ArrayList<>(getStorages());
        newStorageBlocks.add(lookingPos);
        newStorageBlocks.removeFirst();
        this.setStorages(newStorageBlocks);
    }

    // 判断容器是否属于本站点（考虑双箱子）
    default boolean containsStorage(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (getStorages().contains(pos)) return true;
        BlockPos paired = getPairedStoragePos(pos, state);
        return paired != null && getStorages().contains(paired);
    }

    // 是否允许绑定容器（双箱子自动处理）
    default boolean canBindStorage(Level level, BlockPos pos) {
        return isValidStorage(level, pos) && !containsStorage(level, pos)
                && !isstorageBoundToOtherSite(level, pos, getSitePos());
    }

    // 根据容器位置和状态生成合适的渲染条目（单箱子方块高亮 / 双箱子AABB高亮）。
    default RenderEntry getstorageRenderEntry(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // 处理双箱子
        if (state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            if (type == ChestType.LEFT || type == ChestType.RIGHT) {
                BlockPos paired = pos.relative(ChestBlock.getConnectedDirection(state));
                // 构造包围两个箱子的AABB
                AABB aabb = new AABB(
                        Math.min(pos.getX(), paired.getX()),
                        Math.min(pos.getY(), paired.getY()),
                        Math.min(pos.getZ(), paired.getZ()),
                        Math.max(pos.getX(), paired.getX()) + 1,
                        Math.max(pos.getY(), paired.getY()) + 1,
                        Math.max(pos.getZ(), paired.getZ()) + 1
                );
                return new RenderEntry.AabbEntry(aabb, getStorageColor());
            }
        }
        // 普通单箱子
        return new RenderEntry.BlockEntry(pos, getStorageColor());
    }

    // 容器有效性检查（拥有 STOCK_SET 或双箱子）
    default boolean isValidStorage(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.hasData(AttachmentRegistry.STOCK_SET)) return true;
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            // 双箱子的一侧，认为有效
            return type == ChestType.LEFT || type == ChestType.RIGHT;
        }
        return false;
    }

    // 辅助：获取双箱子配对坐标
    @Nullable
    static BlockPos getPairedStoragePos(BlockPos pos, BlockState state) {
        if (state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            if (type == ChestType.LEFT || type == ChestType.RIGHT) {
                return pos.relative(ChestBlock.getConnectedDirection(state));
            }
        }
        return null;
    }

    default boolean isstorageBoundToOtherSite(Level level, BlockPos pos, BlockPos selfWorkPos) {
        Stronghold hold = StrongholdHelper.getPosInHold(level, pos);
        if (hold == null) return false;
        for (Site site : hold.holdState().sites()) {
            if (site.getSitePos().equals(selfWorkPos)) continue;
            if (site instanceof StorageSite storageSite) {
                if (storageSite.containsStorage(level, pos)) return true;
            }
        }
        return false;
    }

    default int getStorageColor() {
        return 0xFFFF8800;
    }
    void setMaxStorageBlocks(int maxStorageBlocks);
    void setStorages(List<BlockPos> storages);
    List<BlockPos> getStorages();
    default int getAssignedStorage() {
        return getStorages().size();
    }
    int getMaxStorage();

    static <T extends StorageSite> RecordCodecBuilder<T, Integer> maxStorageBlocksCodec() {
        return Codec.INT.fieldOf("max_storage_blocks").forGetter(T::getMaxStorage);
    }
    static <T extends StorageSite> RecordCodecBuilder<T, List<BlockPos>> storageBlocksCodec() {
        return BlockPos.CODEC.listOf().fieldOf("storage_blocks").forGetter(T::getStorages);
    }
    static <T extends StorageSite> StreamCodec<ByteBuf, Integer> maxStorageBlocksStreamCodec() {
        return ByteBufCodecs.INT;
    }
    static <T extends StorageSite> StreamCodec<ByteBuf, List<BlockPos>> storageStreamCodec() {
        return BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list());
    }
}
