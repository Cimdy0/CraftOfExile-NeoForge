package com.cimdy.coe.data.codec.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.mod.value.ModPreset;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentRead;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentTemplate;
import com.cimdy.coe.data.codec.item.equipment.EquipmentModPreset;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ItemInstances {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item_instance");
    public static final ResourceKey<Registry<ItemInstance.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<ItemInstance.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final ItemInstance.Type<ItemWeight.Weights> ITEM_WEIGHT =
            register("weight", ItemWeight.Weights.CODEC);

    public static final ItemInstance.Type<ModPreset> ITEM_MOD =
            register("mod", ModPreset.CODEC);

    public static final ItemInstance.Type<EquipmentRead> EQUIPMENT_READ =
            register("equipment", EquipmentRead.CODEC);

    public static final ItemInstance.Type<EquipmentModPreset> MOD_PRESET =
            register("mod_preset", EquipmentModPreset.CODEC);

    public static final ItemInstance.Type<EquipmentTemplate> EQUIPMENT_TEMPLATE =
            register("template", EquipmentTemplate.CODEC);

    private static <T extends ItemInstance> ItemInstance.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(
                REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item_" + name),
                new ItemInstance.Type<>(codec));
    }
}
