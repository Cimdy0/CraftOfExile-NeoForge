package com.cimdy.coe.ui.widget.render;

import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface ItemRendererI extends Renderer {

    ItemRendererI showTooltip(boolean b);

    ItemRendererI showCount(boolean b);
}
