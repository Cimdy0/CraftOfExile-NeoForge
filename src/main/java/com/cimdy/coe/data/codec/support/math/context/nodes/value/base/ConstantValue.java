package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.DistanceValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 常量读值节点：返回固定 {@code double}，供 {@link CompareValue} 等比较节点作常量右操作数
 * （如「距离 ≤ 阈值」条件）。纯函数，无副作用，不依赖 scope。
 *
 * <p>JSON 形式：{@code {"type":"const","value":3.0}}。仅 JSON 全量形式（无中缀简写），
 * 故 {@code infixOf} 为 {@code null}（不会被 toInfix 还原，仅作条件节点被引用）。
 *
 * <p>本节点是「距离簇」引擎扩展（FRONTIER_AUDIT §3.1）的基础设施：解锁
 * {@code CompareValue(DistanceValue.TO_ANCHOR, ConstantValue(threshold), LE)} 形式的距离门控条件。
 */
public record ConstantValue(double value) implements ValueExpr {

    public static final Codec<ConstantValue> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("value").forGetter(ConstantValue::value)
    ).apply(i, ConstantValue::new));

    @Override
    public double evaluate(ValueScope scope) {
        return value;
    }

    /** 名词短语：常数按 locale 格式化（P3/A）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(Component.literal(StatNumberFormat.format(value, locale)));
    }
}
