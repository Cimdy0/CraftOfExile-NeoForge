package com.cimdy.coe.registry;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeTagRegistry {
    public static final TagKey<DamageType> IS_AILMENT = TagKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "is_ailment"));

    public static final TagKey<DamageType> BYPASS_ENERGY = TagKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "bypass_energy"));

}
