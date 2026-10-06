package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.entity.trigger.EntityTriggerTypes;
import com.mojang.serialization.Codec;

import java.util.Map;

/**
 * 实体行为组件 —— 实体每 tick <b>何时对谁生效</b>（触发判定）。
 *
 * <p>与 {@link EntityMotion}（怎么动）正交：二者组合出全部世界物件形态 ——
 * <pre>{@code
 *   static    + contact  = 危害物 / 残片（等目标走进来）
 *   static    + pulse    = Orb（原地周期放能：风暴漩涡 / 日曜球）
 *   static    + [pulse, detonate] = 复合形态（周期施加 + 到期引爆）
 *   ballistic + detonate = 手雷（飞出去 → 落地 → 引信 → 引爆）
 *   ballistic + pulse    = 球（边飞边放能：天雷之珠）
 *   external  + none     = 原版箭矢（运动与命中皆由外部系统负责）
 * }</pre>
 * 组合是<b>数据</b>的组合，不是子类的组合 —— 这正是「实体只是注册基类，行为依赖数据」的落点。
 *
 * <p><b>一份形态可配多个行为</b>（{@code EntityDef.behavior} 是列表）：宿主不再假设
 * 「一个物件只有一种行为」，否则「周期施加 + 到期引爆」这类复合形态就只能靠再写一个
 * 专属行为来表达（等于把组合烤进 Java）。
 *
 * <p><b>面向 {@link EntityRuntime} 编程</b>：本接口<b>不认识宿主实体类</b>，
 * 只经运行时契约索敌、执行效果、存取按实例分域的键值运行态。
 *
 * <p><b>禁枚举</b>：行为种类属分类维度，走字符串 id + 注册式多态（同 {@code EntityTriggerTypes}）。
 *
 * @see EntityBehaviors
 */
public interface EntityBehavior {

    /** 作用对象：施放者本人（残片被自己捡起）。 */
    String TARGET_SELF = "self";
    /** 作用对象：敌人（危害物 / 手雷 / Orb 的作用目标）。 */
    String TARGET_ENEMY = "enemy";
    /** 作用对象：任意生物（通用兜底）。 */
    String TARGET_ANY = "any";

    /**
     * 运行参数键：脉冲间隔覆写（tick，{@code <= 0} 表示不覆写）。
     *
     * <p>节奏是<b>技能层参数</b>（风暴漩涡 4s / 日曜球 1.4s），由投递装配时注入
     * —— 与「初速度由投递设置」同一口径（皆非形态契约），故走通用 {@link EntityRuntime#param(String)}
     * 而不是在宿主加一个只有脉冲读的专属字段。
     */
    String PARAM_PULSE_INTERVAL = "pulse_interval";

    Codec<Map<EntityBehavior.Type<?>, EntityBehavior>> MAP_CODEC = Codec.dispatchedMap(
            EntityBehaviorTypes.REGISTRY.byNameCodec(), EntityBehavior.Type::codec);

    Codec<EntityBehavior> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            b -> Map.of(b.getType(), b));

    EntityBehavior.Type<?> getType();

    /**
     * 每 tick 的行为判定。
     *
     * @return true 表示「本行为已完成，实体应销毁」（一次性触发完成 / 引信引爆完毕）
     */
    boolean tick(EntityRuntime rt);

    /**
     * 装配时初始化本行为的运行态（如引信装填）。
     *
     * <p><b>只在「新生成」时调用一次</b>：存档重载时运行态由持久化的计时器还原，
     * 不可被重新装填覆盖（否则读档即重置引信）。
     */
    default void init(EntityRuntime rt) {
    }

    record Type<T extends EntityBehavior>(Codec<T> codec) {
    }
}
