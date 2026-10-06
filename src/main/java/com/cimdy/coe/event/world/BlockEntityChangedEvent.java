package com.cimdy.coe.event.world;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;

public class BlockEntityChangedEvent extends Event {
    private final BlockEntity blockEntity;

    public BlockEntityChangedEvent(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
