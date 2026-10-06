package com.cimdy.coe.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {
    @Accessor("litTimeRemaining")
    void setLitTimeRemaining(int litTimeRemaining);

    @Accessor("litTimeRemaining")
    int getLitTimeRemaining();

    @Accessor("litTotalTime")
    void setLitTotalTime(int litTotalTime);

    @Accessor("litTotalTime")
    int getLitTotalTime();
}
