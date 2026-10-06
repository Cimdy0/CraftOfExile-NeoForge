package com.cimdy.coe.effect.custom;

import net.minecraft.world.effect.MobEffectCategory;

public class NoneEffect extends AbstractStateEffect {
    public NoneEffect(MobEffectCategory category, int color) {
        super(category, color, false);
    }
}
