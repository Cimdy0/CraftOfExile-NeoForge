package com.cimdy.coe.data.codec.entity.trigger;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;

/**
 * 道具触发效果 —— 实体被触发时执行的<b>一个效果</b>（点燃阶段 ON_HIT / 发射事件 / 记录事件 / 施加状态 …）。
 *
 * <p>与 {@link EntityRenderLayer} / {@link EntityTriggerTypes} 同构：注册式多态（{@code EntityTriggerTypes}），
 * 多个效果自由组合成 {@link EntityDef} 的 {@code on_trigger} 列表，使「一类实体多重效果」成立——
 * 换效果 = 换一份 {@code prop} 数据，零 Java 改动。
 *
 * <p><b>禁枚举</b>：触发效果种类必须是字符串标签 + 数据驱动注册表（复用本多态范式），
 * 新增一种效果 = 加一个注册实现 + 数据包写一条，不改 Java 分发逻辑。
 */
public interface EntityTrigger {

    Codec<Map<EntityTrigger.Type<?>, EntityTrigger>> MAP_CODEC = Codec.dispatchedMap(
            EntityTriggerTypes.REGISTRY.byNameCodec(), EntityTrigger.Type::codec);

    Codec<EntityTrigger> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            t -> Map.of(t.getType(), t));

    EntityTrigger.Type<?> getType();

    /** 该效果在实体被触发时执行。 */
    void fire(EntityTriggerContext ctx);

    /**
     * 触发上下文：触发瞬间把施放者 / 命中目标 / 实体本身 / 世界 / 阶段 id 一次性打包，
     * 避免各效果重复取用。
     */
    record EntityTriggerContext(LivingEntity caster, LivingEntity target, Entity prop,
                              ServerLevel level, UUID casterId, UUID phaseId) {
    }

    record Type<T extends EntityTrigger>(Codec<T> codec) {
    }
}
