package com.cimdy.coe.registry;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.effect.custom.NoneEffect;
import com.cimdy.coe.effect.custom.Summon;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CraftOfExile.MODID);

    // Mechanical markers only: used purely for "is this entity holding X" checks, no status semantics.
    // All former status / form / aura effects have been migrated to the Status system.
    public static final Holder<MobEffect> IN_HOLD = MOB_EFFECTS.register(
            "in_hold", () -> new NoneEffect(MobEffectCategory.BENEFICIAL, 0x00FF00));

    public static final Holder<MobEffect> OWNER_HOLD = MOB_EFFECTS.register(
            "owner_hold", () -> new NoneEffect(MobEffectCategory.NEUTRAL, 0x00FF00));

    public static final Holder<MobEffect> SUMMON = MOB_EFFECTS.register(
            "summon", () -> new Summon(0x00FF00));
}
