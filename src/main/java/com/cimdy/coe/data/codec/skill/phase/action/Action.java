package com.cimdy.coe.data.codec.skill.phase.action;

import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Set;

/**
 * 效果动作 — 技能产生的具体效果。
 * 通过 dispatchedMap 实现多态序列化。
 */
public interface Action {
    Codec<Map<Action.Type<?>, Action>> MAP_CODEC = Codec.dispatchedMap(
            ActionTypes.REGISTRY.byNameCodec(),
            Action.Type::codec);

    Codec<Action> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    Action.Type<?> getType();

    void execute(ActionContext context);

    /**
     * 本行为自述的 stat 标签（按需覆写，如伤害行为按伤害类型带元素标签）。
     * 行为执行时会与所属阶段投放形态的 {@code Delivery.statTags()} 合并进入 stat 作用域，
     * 使带标签的天赋加成在该行为的属性读取内即时生效。标签为字符串，可直接对应技能名、元素等任意值。
     */
    default Set<String> statTags() { return Set.of(); }

    record Type<T extends Action>(Codec<T> codec) {}
}
