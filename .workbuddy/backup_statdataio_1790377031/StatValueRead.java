package com.cimdy.coe.data.codec.support.stat.value;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * 引用型数值部件（{@code type:"read"}）——仅持有路径 {@code ref}，调用时从数据包加载的
 * {@link LoadEvent#STAT_VALUE_MAP} 取对应 {@link StatValueInline} 并转发 {@link #value()}，
 * 与 {@link com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead} /
 * {@link com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead} 同源。
 *
 * <p>取代 {@code Either<StatValue, Identifier>} 外层包裹：引用能力内聚在 {@link StatValue}
 * 类型内部（节点自身实现 read 形式），运行期消费方调用 {@link #value()} 时懒委托解析，
 * 无需感知引用形态。悬空引用在首次访问时显式报错（取代静默 null 被吞）。
 */
public record StatValueRead(Identifier ref) implements StatValue {

    @Override
    public String type() {
        return "read";
    }

    /** 从数据包加载的数值部件表解析本引用指向的 {@link StatValueInline}（缺失则报错）。 */
    public StatValue resolve() {
        StatValue v = LoadEvent.STAT_VALUE_MAP.get(this.ref);
        if (v == null) {
            throw new IllegalStateException("StatValueRead 引用了未加载的 stat_value: " + this.ref);
        }
        return v;
    }

    @Override
    public ExpressionCodec.Value value() {
        return resolve().value();
    }

    /** 引用型 MapCodec：仅含 {@code ref}。 */
    public static final MapCodec<StatValueRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(StatValueRead::ref)
    ).apply(i, StatValueRead::new));

    /** 引用实现对应的完整 Codec（供 {@link StatValueTypes} 注册表登记）。 */
    public static final com.mojang.serialization.Codec<StatValueRead> CODEC = MAP_CODEC.codec();
}
