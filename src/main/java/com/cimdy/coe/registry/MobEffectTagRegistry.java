package com.cimdy.coe.registry;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public class MobEffectTagRegistry {
    public static final TagKey<MobEffect> IS_AILMENT = TagKey.create(Registries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "is_ailment"));
}
