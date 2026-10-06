package com.cimdy.coe.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AbstractStateEffect extends MobEffect {
    private final boolean tick;

    protected AbstractStateEffect(MobEffectCategory category, int color, boolean tick) {
        super(category, color);
        this.tick = tick;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return tick || duration == 1;
    }
}
