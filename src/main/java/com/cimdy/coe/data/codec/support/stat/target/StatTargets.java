package com.cimdy.coe.data.codec.support.stat.target;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 一条 stat 条目（{@link com.cimdy.coe.data.codec.support.stat.entry.StatEntry}）的
 * <b>三段选择器</b>集合：节点自身即共享预设的载体（与 {@link StatEffect} 同源），
 * 唯一形态 {@link StatTargetsInline}（{@code type:"inline"}）平铺内联三段选择器，
 * 由 {@link StatTargets} 接口统一收口，而非外层 {@code Either<StatTargets, Identifier>} 包裹。
 *
 * <p>「条件 → 数值 → 作用」三阶段各自可指向不同实体，把这三把钥匙聚成一个值对象，
 * 避免条目类被三个同类型字段撑得杂乱，也便于整体填默认值（{@code StatTargetsInline#isEmpty}（该便捷方法已删除））。
 *
 * @param target          作用阶段：本条目「作用于谁」；{@code null} = 持有者自身
 * @param conditionTarget 条件阶段：「被判定实体」；{@code null} = 持有者自身
 * @param valueTarget     数值阶段：「被取值实体」；{@code null} = 持有者自身
 */
public interface StatTargets {
    /** 作用阶段选择器；{@code null} = 持有者自身。 */
    @Nullable
    TargetSelector target();

    /** 条件阶段「被判定实体」；{@code null} = 持有者自身。 */
    @Nullable
    TargetSelector conditionTarget();

    /** 数值阶段「被取值实体」；{@code null} = 持有者自身。 */
    @Nullable
    TargetSelector valueTarget();

    /** 分发类型 id（写入 JSON 的 "type" 字段，用于 Codec 分发）。 */
    String type();

    /** 三段是否全缺省（全部落到持有者自身）。 */
    default boolean isEmpty() {
        return target() == null && conditionTarget() == null && valueTarget() == null;
    }

    /**
     * 目标「片段」：三段选择器融合成的可译短语（P1 仅渲染作用目标 {@code target()}）。
     * <p>规则：{@code target()} 为 null 或源为 {@link TargetSelector.Source#SELF}（默认作用自身）
     * 时不渲染（避免 "to self"）；其余源经融合键 {@code stat.targets.fmt}（"to %s"）输出。
     * <p>limit（取前 N）本阶段不进入短语（见 plan §6 开放点），留待 P2+。
     * 返回空列表 = 无目标片段（装配器据此跳过）。
     */
    default List<Component> describeFragment() {
        TargetSelector t = target();
        if (t == null || t.source() == TargetSelector.Source.SELF) return List.of();
        return List.of(Component.translatable("stat.targets.fmt", t.source().describeName()));
    }

    /**
     * 三段全缺省（全部落到持有者自身）。
     * <p>直接用 {@link StatTargetsInline} 构造，而非引用 {@code StatTargetsInline.EMPTY} 字段——
     * 避免「接口常量 → 实现类常量」的跨类静态初始化环：当本接口先被触发初始化、
     * 又回头读取实现类的常量字段时，实现类可能尚处初始化中、其常量仍为默认 {@code null}。
     */
    StatTargets EMPTY = new StatTargetsInline(null, null, null);

    /** 仅指定作用目标。 */
    static StatTargets ofTarget(@Nullable TargetSelector target) {
        return new StatTargetsInline(target, null, null);
    }

    /** 指定作用目标 + 取值实体（伤害类效果常用：作用于事件目标且按其自身属性取值）。 */
    static StatTargets of(@Nullable TargetSelector target, @Nullable TargetSelector valueTarget) {
        return new StatTargetsInline(target, null, valueTarget);
    }

    /** 分发式 MapCodec：按 {@code type} 字段派发到 {@link StatTargetsTypes} 登记的 inline / read。 */
    MapCodec<StatTargets> MAP_CODEC = StatTargetsTypes.MAP_CODEC;

    /** 独立编解码（整体对象形式，按 {@code type} 分发）：供其作为数据包独立单元被引用。 */
    Codec<StatTargets> CODEC = MAP_CODEC.codec();
}
