package com.cimdy.coe.mixin;

import com.cimdy.coe.event.world.BlockEntityChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Level.class)
public abstract class BlockEntityChangedMixin {
    @Shadow
    @Nullable
    public abstract BlockEntity getBlockEntity(BlockPos pos);

    @Inject(method = "blockEntityChanged", at = @At("HEAD"))
    private void setChanged(BlockPos pos, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new BlockEntityChangedEvent(this.getBlockEntity(pos)));
    }
}
