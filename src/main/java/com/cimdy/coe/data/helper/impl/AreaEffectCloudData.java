package com.cimdy.coe.data.helper.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public record AreaEffectCloudData(
        ParticleOptions particle,
        double duration, double radius,
        double radiusPerTick,
        List<MobEffectInstance> effects
) {
    public static final AreaEffectCloudData EMPTY = new AreaEffectCloudData(ParticleTypes.CLOUD,
            0.0, 0.0, 0.0, List.of());
    public static final Codec<AreaEffectCloudData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(AreaEffectCloudData::particle),
            Codec.DOUBLE.fieldOf("duration").forGetter(AreaEffectCloudData::duration),
            Codec.DOUBLE.fieldOf("range").forGetter(AreaEffectCloudData::radius),
            Codec.DOUBLE.fieldOf("radius_per_tick").forGetter(AreaEffectCloudData::radiusPerTick),
            MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(AreaEffectCloudData::effects)
    ).apply(instance, AreaEffectCloudData::new));
}
