package com.cimdy.coe.data.codec.item.equipment;

import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record UniquePreset(String lore, List<Addition> additions) {
    public static final Codec<UniquePreset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("lore").forGetter(UniquePreset::lore),
            Addition.CODEC.listOf().fieldOf("additions").forGetter(UniquePreset::additions)
    ).apply(instance, UniquePreset::new));
}