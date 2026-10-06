package com.cimdy.coe.data.codec.skill.phase.delivery.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.BallisticFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.ChainFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.HomingFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.LinearFlight;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.flight.SpiralFlight;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class FlightPatternTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "flight_pattern");
    public static final ResourceKey<Registry<FlightPattern.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<FlightPattern.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    public static final FlightPattern.Type<LinearFlight> LINEAR = register("linear", LinearFlight.CODEC);
    public static final FlightPattern.Type<HomingFlight> HOMING = register("homing", HomingFlight.CODEC);
    public static final FlightPattern.Type<BallisticFlight> BALLISTIC = register("ballistic", BallisticFlight.CODEC);
    public static final FlightPattern.Type<SpiralFlight> SPIRAL = register("spiral", SpiralFlight.CODEC);
    public static final FlightPattern.Type<ChainFlight> CHAIN = register("chain", ChainFlight.CODEC);

    @SuppressWarnings("unchecked")
    public static <T extends FlightPattern> FlightPattern.Type<T> register(String name, MapCodec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "flight_" + name),
                new FlightPattern.Type<>(codec));
    }
}
