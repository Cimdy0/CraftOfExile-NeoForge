package com.cimdy.coe.data.codec.skill.input;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class InputModes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_input_mode");
    public static final ResourceKey<Registry<InputMode.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<InputMode.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final InputMode.Type<Click> CLICK = register("click", Click.CODEC);
    public static final InputMode.Type<HoldInput> HOLD = register("hold", HoldInput.CODEC);
    public static final InputMode.Type<Continuous> CONTINUOUS = register("continuous", Continuous.CODEC);
    public static final InputMode.Type<ToggleInput> TOGGLE = register("toggle", ToggleInput.CODEC);

    public static <T extends InputMode> InputMode.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "input_" + name),
                new InputMode.Type<>(codec));
    }
}
