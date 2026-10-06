package com.cimdy.coe.data.codec.skill.modifier;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.SealMod;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 技能修正类型注册表 — 技能与辅助宝石共享。
 * 键名不带 support_ 前缀（如 coe:delivery_mod），因为二者共用。
 */
public class SkillModifierTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_modifier");
    public static final ResourceKey<Registry<SkillModifier.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<SkillModifier.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final SkillModifier.Type<DeliveryMod> DELIVERY_MOD =
            register("delivery_mod", DeliveryMod.CODEC);
    public static final SkillModifier.Type<EffectMod> EFFECT_MOD =
            register("effect_mod", EffectMod.CODEC);
    public static final SkillModifier.Type<AdditionalActionEffect> ADDITIONAL_ACTION =
            register("additional_action", AdditionalActionEffect.CODEC);
    public static final SkillModifier.Type<EventListenEffect> EVENT_LISTEN =
            register("event_listen", EventListenEffect.CODEC);
    public static final SkillModifier.Type<EchoMod> ECHO_MOD =
            register("echo_mod", EchoMod.CODEC);
    public static final SkillModifier.Type<SealMod> SEAL_MOD =
            register("seal_mod", SealMod.CODEC);

    public static <T extends SkillModifier> SkillModifier.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new SkillModifier.Type<>(codec));
    }
}
