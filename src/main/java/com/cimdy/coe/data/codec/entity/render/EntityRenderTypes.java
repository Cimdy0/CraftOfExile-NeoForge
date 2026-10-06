package com.cimdy.coe.data.codec.entity.render;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 道具渲染层的分发注册表 —— 写法与 {@code ActionTypes} / {@code DeliveryTypes} 对齐。
 *
 * <p>注册名即 JSON 的派发键（如 {@code "particle_ring"}），多层按 {@code EntityRenderDef.layers}
 * 数组顺序叠加，先写的层在下、后写的层在上（模型层建议置于末尾以免被粒子遮蔽）。
 */
public class EntityRenderTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_render_layer");
    public static final ResourceKey<Registry<EntityRenderLayer.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<EntityRenderLayer.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    /** 水平粒子环：贴地一圈，用于表达「拾取/触发范围」。 */
    public static final EntityRenderLayer.Type<EntityRenderLayers.Ring> RING =
            register("particle_ring", EntityRenderLayers.Ring.CODEC);
    /** 竖直粒子柱：从底部升起，用于表达「驻场/存在感」（战旗旗杆、图腾）。 */
    public static final EntityRenderLayer.Type<EntityRenderLayers.Pillar> PILLAR =
            register("particle_pillar", EntityRenderLayers.Pillar.CODEC);
    /** 粒子球壳：包裹触发体积，用于表达「危害区域/光环边界」。 */
    public static final EntityRenderLayer.Type<EntityRenderLayers.Sphere> SPHERE =
            register("particle_sphere", EntityRenderLayers.Sphere.CODEC);
    /** 单点粒子簇：在实体中心散开，用于表达「火花/余烬」。 */
    public static final EntityRenderLayer.Type<EntityRenderLayers.Point> POINT =
            register("particle_point", EntityRenderLayers.Point.CODEC);
    /** 模型层：客户端绘制贴图立牌（可选十字、可自转），服务端为空实现。 */
    public static final EntityRenderLayer.Type<EntityRenderLayers.Model> MODEL =
            register("model", EntityRenderLayers.Model.CODEC);

    public static <T extends EntityRenderLayer> EntityRenderLayer.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new EntityRenderLayer.Type<>(codec));
    }
}
