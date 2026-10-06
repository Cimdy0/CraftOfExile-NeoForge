package com.cimdy.coe.data.gen.asset;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.addition.ModAdditions;
import com.cimdy.coe.data.codec.support.meta.Labels;
import com.cimdy.coe.data.codec.vanilla.EnchantModifier;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

import static com.cimdy.coe.data.codec.support.meta.Labels.*;

public class EnchantModifierGen {
    public static void registerEnchantModifier(BootstrapContext<EnchantModifier> bootstrap) {
        bootstrap.register(ResourceKey.create(LoadEvent.ENCHANT_MODIFIER_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "sharpness_provide_physical_damage_inc_local")),
                EnchantModifier.from(
                        Enchantments.SHARPNESS,
                        List.of(
                                Addition.from(ModAdditions.PHYSICAL_DAMAGE_INC_LOCAL, 0.15)
                        ), List.of(ATTACK, PHYSICAL, DAMAGE)));
        bootstrap.register(ResourceKey.create(LoadEvent.ENCHANT_MODIFIER_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_aspect_provide_attack_add_fire_damage")),
                EnchantModifier.from(
                        Enchantments.FIRE_ASPECT,
                        List.of(
                                Addition.from(ModAdditions.ADD_FIRE_DAMAGE, 60)
                        )));
        bootstrap.register(ResourceKey.create(LoadEvent.ENCHANT_MODIFIER_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fire_protection_provide_fire_resistance")),
                EnchantModifier.from(
                        Enchantments.FIRE_PROTECTION,
                        List.of(
                                Addition.from(ModAdditions.ADD_FIRE_RESISTANCE, 0.05)
                        )));
    }

}
