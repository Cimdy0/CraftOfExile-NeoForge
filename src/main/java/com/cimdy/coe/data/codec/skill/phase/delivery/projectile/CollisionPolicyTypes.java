package com.cimdy.coe.data.codec.skill.phase.delivery.projectile;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Bounce;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Explode;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Pierce;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Split;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class CollisionPolicyTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "collision_policy");
    public static final ResourceKey<Registry<CollisionPolicy.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<CollisionPolicy.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    public static final CollisionPolicy.Type<Stop> STOP = register("stop", Stop.CODEC);
    public static final CollisionPolicy.Type<Pierce> PIERCE = register("pierce", Pierce.CODEC);
    public static final CollisionPolicy.Type<Bounce> BOUNCE = register("bounce", Bounce.CODEC);
    public static final CollisionPolicy.Type<Split> SPLIT = register("split", Split.CODEC);
    public static final CollisionPolicy.Type<Explode> EXPLODE = register("explode", Explode.CODEC);

    @SuppressWarnings("unchecked")
    public static <T extends CollisionPolicy> CollisionPolicy.Type<T> register(String name, MapCodec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "collision_" + name),
                new CollisionPolicy.Type<>(codec));
    }
}
