package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Map;

/**
 * 范围持续时间模式。
 */
public interface AreaDuration {
    Registry<AreaDuration.Type<?>> REGISTRY = AreaDurationTypes.REGISTRY;

    Codec<Map<AreaDuration.Type<?>, AreaDuration>> MAP_CODEC = Codec.dispatchedMap(
            AreaDurationTypes.REGISTRY.byNameCodec(), AreaDuration.Type::codec);

    Codec<AreaDuration> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    Type<?> getType();

    record Type<T extends AreaDuration>(Codec<T> codec) {}
}

class AreaDurationTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "area_duration");
    public static final ResourceKey<Registry<AreaDuration.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<AreaDuration.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    public static final AreaDuration.Type<InstantDuration> INSTANT = register("instant", InstantDuration.CODEC);
    public static final AreaDuration.Type<LingeringDuration> LINGERING = register("lingering", LingeringDuration.CODEC);

    @SuppressWarnings("unchecked")
    public static <T extends AreaDuration> AreaDuration.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "duration_" + name),
                new AreaDuration.Type<>(codec));
    }
}
