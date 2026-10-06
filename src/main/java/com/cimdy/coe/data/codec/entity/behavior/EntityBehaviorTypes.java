package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.trigger.EntityTriggerTypes;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 实体行为组件的分发注册表 —— 写法与 {@code EntityMotionTypes} / {@code EntityTriggerTypes} 对齐。
 *
 * <p>注册名即 JSON 的派发键（如 {@code "pulse"}），一份 {@code coe:entity} 数据恰有一个行为组件。
 */
public class EntityBehaviorTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_behavior");
    public static final ResourceKey<Registry<EntityBehavior.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<EntityBehavior.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(32).create();

    /** 无自主行为：触发由外部系统负责（原版箭矢）。 */
    public static final EntityBehavior.Type<EntityBehaviors.None> NONE =
            register("none", EntityBehaviors.None.CODEC);
    /** 接触触发：目标进入作用半径即生效（危害物 / 残片）。 */
    public static final EntityBehavior.Type<EntityBehaviors.Contact> CONTACT =
            register("contact", EntityBehaviors.Contact.CODEC);
    /** 周期脉冲：每 interval tick 对半径内每个目标各生效一次（Orb / 飞行球）。 */
    public static final EntityBehavior.Type<EntityBehaviors.Pulse> PULSE =
            register("pulse", EntityBehaviors.Pulse.CODEC);
    /** 引信引爆：停住后倒计时结束，对半径内每个目标各生效一次并销毁（手雷）。 */
    public static final EntityBehavior.Type<EntityBehaviors.Detonate> DETONATE =
            register("detonate", EntityBehaviors.Detonate.CODEC);
    /** 光环：每 interval tick 对自带的独立小半径内每个目标各施加一次状态（如日曜球「1 米内点燃」）。 */
    public static final EntityBehavior.Type<EntityBehaviors.Aura> AURA =
            register("aura", EntityBehaviors.Aura.CODEC);

    public static <T extends EntityBehavior> EntityBehavior.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new EntityBehavior.Type<>(codec));
    }
}
