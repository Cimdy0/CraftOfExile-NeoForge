package com.cimdy.coe.data.codec.support.stat.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * 内联三段选择器（{@code type:"inline"}）——把 {@code target / condition / value} 三把钥匙
 * 平铺在同一对象。由 {@link StatTargetsTypes} 按 {@code type} 分派解码。
 *
 * <p>共享预设即本类内联实例（内容寻址已移除，身份由 StatTargetIds 具名常量承载）
 * 资源，引用能力内聚在 {@link StatTargets} 类型内部（节点自身实现 read 形式），
 * 取代 {@code Either<StatTargets, Identifier>} 外层包裹。
 */
public record StatTargetsInline(@Nullable TargetSelector target,
                                @Nullable TargetSelector conditionTarget,
                                @Nullable TargetSelector valueTarget) implements StatTargets {

    @Override
    public String type() {
        return "inline";
    }

    /** 平铺式 MapCodec：{@code target / condition / value} 与条目其余字段同层（缺省即 null = 持有者自身）。 */
    public static final MapCodec<StatTargetsInline> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TargetSelector.CODEC.optionalFieldOf("target")
                    .forGetter(t -> Optional.ofNullable(t.target())),
            TargetSelector.CODEC.optionalFieldOf("condition")
                    .forGetter(t -> Optional.ofNullable(t.conditionTarget())),
            TargetSelector.CODEC.optionalFieldOf("value")
                    .forGetter(t -> Optional.ofNullable(t.valueTarget()))
    ).apply(i, (target, condTarget, valTarget) ->
            new StatTargetsInline(target.orElse(null), condTarget.orElse(null), valTarget.orElse(null))));

    /** 内联实现对应的完整 Codec（供 {@link StatTargetsTypes} 注册表登记）。 */
    public static final Codec<StatTargetsInline> CODEC = MAP_CODEC.codec();
}
