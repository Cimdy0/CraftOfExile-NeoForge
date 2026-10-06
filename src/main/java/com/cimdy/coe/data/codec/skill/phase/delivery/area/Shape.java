package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Map;

/**
 * 几何形状 — 范围效果的形状。
 */
public interface Shape {
    Registry<Shape.Type<?>> REGISTRY = ShapeTypes.REGISTRY;

    Codec<Map<Shape.Type<?>, Shape>> MAP_CODEC = Codec.dispatchedMap(
            ShapeTypes.REGISTRY.byNameCodec(), Shape.Type::codec);

    Codec<Shape> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    Type<?> getType();

    /**
     * 形状的有效作用半径（标量）— 供范围扫描 / 命中判定 / 实体触发半径装配消费。
     * sphere 直接返回半径；新增几何应避免在每个 executor 里强转/instanceof，
     * 而是直接重写本方法返回代表性半径。默认 0.0 仅作安全兜底（真实形状均重写）。
     */
    default double sampleRadius(SkillContext ctx) {
        return 0.0;
    }

    record Type<T extends Shape>(Codec<T> codec) {}
}

class ShapeTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "delivery_shape");
    public static final ResourceKey<Registry<Shape.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<Shape.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    public static final Shape.Type<SphereShape> SPHERE = register("sphere", SphereShape.CODEC);
    public static final Shape.Type<CylinderShape> CYLINDER = register("cylinder", CylinderShape.CODEC);
    public static final Shape.Type<CubeShape> CUBE = register("cube", CubeShape.CODEC);

    @SuppressWarnings("unchecked")
    public static <T extends Shape> Shape.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "shape_" + name),
                new Shape.Type<>(codec));
    }
}
