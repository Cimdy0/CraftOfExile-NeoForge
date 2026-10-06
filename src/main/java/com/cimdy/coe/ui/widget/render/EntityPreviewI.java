package com.cimdy.coe.ui.widget.render;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public interface EntityPreviewI extends Renderer {

    EntityPreviewI scale(float s);

    EntityPreviewI followMouse(boolean b);
}
