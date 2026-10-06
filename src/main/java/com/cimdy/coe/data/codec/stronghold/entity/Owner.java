package com.cimdy.coe.data.codec.stronghold.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Map;
import java.util.UUID;

public record Owner(UUID uuid, Map<String, Boolean> permissions) {
    public static final Codec<Owner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Owner::uuid),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("permissions")
                    .forGetter(Owner::permissions)
    ).apply(instance, Owner::new));
}