package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.trigger.EntityTriggerTypes;
import com.mojang.serialization.Codec;

import java.util.Map;

/**
 * 实体运动组件 —— 实体每 tick <b>怎么动</b>。
 *
 * <p><b>实体 / 行为拆分的其中一维</b>：宿主类只负责「存在」（同步、存档、渲染 id、阶段引用），
 * 一切<b>行为差异</b>由 {@code coe:entity} 数据里的两组组件决定 ——
 * 本组件管「怎么动」，{@link EntityBehavior} 管「什么时候对谁生效」。
 * 于是新增一种世界物件 = 加一份数据（换个组合），<b>零 Java 改动</b>，
 * 而不是再写一个实体子类把行为烤进 {@code tick()}。
 *
 * <p><b>面向 {@link EntityRuntime} 编程</b>：本接口<b>不认识宿主实体类</b>，
 * 只经运行时契约取用运动原语 —— 否则数据 codec 层反向依赖实体层，拆分即失效。
 *
 * <p><b>禁枚举</b>：运动方式属分类维度，必须走字符串 id + 注册式多态
 * （同 {@code EntityTriggerTypes} / {@code DeliveryTypes}），新增一种运动
 * = 加一个注册实现 + 数据包写一条。
 *
 * <p>已有实现见 {@link EntityMotions}：{@code coe:static}（静止）/ {@code coe:ballistic}（抛射）
 * / {@code coe:external}（不由本层驱动，如原版箭矢）。
 */
public interface EntityMotion {

    Codec<Map<EntityMotion.Type<?>, EntityMotion>> MAP_CODEC = Codec.dispatchedMap(
            EntityMotionTypes.REGISTRY.byNameCodec(), EntityMotion.Type::codec);

    Codec<EntityMotion> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            m -> Map.of(m.getType(), m));

    EntityMotion.Type<?> getType();

    /**
     * 推进一 tick 的运动。
     *
     * @return true 表示「本 tick 之后实体应销毁」（如球撞到方块即消散）
     */
    boolean move(EntityRuntime rt);

    /**
     * 本运动是否需要宿主开启<b>物理碰撞</b>（{@code noPhysics = false}）。
     *
     * <p>由组件声明而非宿主 {@code instanceof} 判定 —— 宿主一旦写
     * {@code motion instanceof Ballistic}，就重新认识了具体组件，拆分即破功。
     */
    default boolean physics() {
        return false;
    }

    /**
     * 实体占位宽度（格）—— 由运动方式决定：静止形态按作用半径铺开（AABB 随半径缩放），
     * 飞行弹体取自身尺寸（否则会拿爆炸半径当身体，撞墙判定荒谬）。
     */
    float width(EntityRuntime rt);

    /**
     * 粒子发射半径（格）—— 视觉足迹也由运动方式决定：
     * 静止形态按作用范围铺开（看得见「这一片都危险」），飞行中只画本体拖尾，
     * 落地后逐步放大到作用半径（给出「要炸了」的预告）。
     *
     * <p><b>只回答「发多大」</b>：发射时机（{@code PARTICLE_INTERVAL}）是宿主的通用调度，
     * 不属任何形态。
     */
    float visualRadius(EntityRuntime rt);

    record Type<T extends EntityMotion>(Codec<T> codec) {
    }
}
