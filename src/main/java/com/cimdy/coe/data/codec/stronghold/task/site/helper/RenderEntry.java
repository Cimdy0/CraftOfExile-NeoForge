package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public sealed interface RenderEntry {
    /** 方块高亮 (位置, 颜色) */
    record BlockEntry(BlockPos pos, int color) implements RenderEntry {}
    /** AABB 高亮 (区域, 颜色) */
    record AabbEntry(AABB aabb, int color) implements RenderEntry {}
}
