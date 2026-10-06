package com.cimdy.coe.data.codec.stronghold.task.site.basic;

import com.cimdy.coe.data.codec.stronghold.task.site.helper.InteractionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.RenderEntry;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AreaStorageSite extends AreaSite implements StorageSite {
    protected int maxStorageBlocks;
    protected List<BlockPos> storageBlocks;

    public AreaStorageSite(
            BlockPos workPos, int maxWorkers, boolean autoAssign, int priority,
            Map<String, Optional<BlockPos>> entityPositions, Optional<BlockPos> posA, Optional<BlockPos> posB,
            int maxStorageBlocks, List<BlockPos> storageBlocks) {
        super(workPos, maxWorkers, autoAssign, priority, entityPositions, posA, posB);
        this.maxStorageBlocks = maxStorageBlocks;
        this.storageBlocks = storageBlocks;
    }

    @Override
    public InteractionResult getInteractionHint(Level level, BlockPos targetPos) {
        InteractionResult result = super.getInteractionHint(level, targetPos);
        if (!result.isChanged()) {
            result.merge(StorageSite.super.getInteractionHint(level, targetPos));
        }
        return result;
    }

    @Override
    public InteractionResult applyAction(Level level, BlockPos lookingPos, KeyMapping key, boolean shiftKeyDown) {
        InteractionResult result = super.applyAction(level, lookingPos, key, shiftKeyDown);
        if (!result.isChanged()) {
            result.merge(StorageSite.super.applyAction(level, lookingPos, key, shiftKeyDown));
        }
        return result;
    }

    @Override
    public List<RenderEntry> getRenderEntries(Level level) {
        List<RenderEntry> result = super.getRenderEntries(level);
        result.addAll(StorageSite.super.getRenderEntries(level));
        return result;
    }

    @Override
    public void setMaxStorageBlocks(int maxStorageBlocks) {
        this.maxStorageBlocks = maxStorageBlocks;
    }
    @Override public int getMaxStorage() {
        return maxStorageBlocks;
    }
    @Override public List<BlockPos> getStorages() {
        return storageBlocks;
    }
    @Override public void setStorages(List<BlockPos> storages) {
        this.storageBlocks = storages;
    }
}
