package com.cimdy.coe.data.codec.skill.phase.delivery;

import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BallDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BeamDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.WaveDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.HazardDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ThrownDelivery;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class DeliveryTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_delivery");
    public static final ResourceKey<Registry<Delivery.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<Delivery.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final Delivery.Type<ProjectileDelivery> PROJECTILE = register("projectile", ProjectileDelivery.CODEC);
    public static final Delivery.Type<AreaDelivery> AREA = register("area", AreaDelivery.CODEC);
    public static final Delivery.Type<HazardDelivery> HAZARD = register("hazard", HazardDelivery.CODEC);
    public static final Delivery.Type<RemnantDelivery> REMNANT = register("remnant", RemnantDelivery.CODEC);
    public static final Delivery.Type<ThrownDelivery> THROWN = register("thrown", ThrownDelivery.CODEC);
    public static final Delivery.Type<BallDelivery> BALL = register("ball", BallDelivery.CODEC);
    public static final Delivery.Type<OrbDelivery> ORB = register("orb", OrbDelivery.CODEC);
    public static final Delivery.Type<BeamDelivery> BEAM = register("beam", BeamDelivery.CODEC);
    public static final Delivery.Type<WaveDelivery> WAVE = register("wave", WaveDelivery.CODEC);
    public static final Delivery.Type<SelfDelivery> SELF = register("self", SelfDelivery.CODEC);

    public static <T extends Delivery> Delivery.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "delivery_" + name),
                new Delivery.Type<>(name, codec));
    }
}
