package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.trigger.EntityTriggerTypes;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 实体运动组件的分发注册表 —— 写法与 {@code EntityTriggerTypes} / {@code DeliveryTypes} 对齐。
 *
 * <p>注册名即 JSON 的派发键（如 {@code "ballistic"}），一份 {@code coe:entity} 数据恰有一个运动组件。
 */
public class EntityMotionTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_motion");
    public static final ResourceKey<Registry<EntityMotion.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<EntityMotion.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(32).create();

    /** 静止：位置恒定（残片 / 危害物 / 球状驻场物件）。 */
    public static final EntityMotion.Type<EntityMotions.Static> STATIC =
            register("static", EntityMotions.Static.CODEC);
    /** 抛射：每 tick 施加重力并只与方块碰撞（投掷物 / 飞行球）。 */
    public static final EntityMotion.Type<EntityMotions.Ballistic> BALLISTIC =
            register("ballistic", EntityMotions.Ballistic.CODEC);
    /** 外部驱动：运动由外部系统负责（原版箭矢 + 投递飞行模式），本层不介入。 */
    public static final EntityMotion.Type<EntityMotions.External> EXTERNAL =
            register("external", EntityMotions.External.CODEC);
    /** 墙体：静止但开启真实碰撞（physical），使承载实体能阻挡技能与移动。 */
    public static final EntityMotion.Type<EntityMotions.Wall> WALL =
            register("wall", EntityMotions.Wall.CODEC);

    public static <T extends EntityMotion> EntityMotion.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new EntityMotion.Type<>(codec));
    }
}
