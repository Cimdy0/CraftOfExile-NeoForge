package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.math.ComparisonOperator;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatNumberFormat;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 两子节点数值比较节点：对左右子树按各自 scale 缩放后，按 {@link ComparisonOperator} 比较，
 * 命中输出 1.0，否则 0.0。用于「装备头盔护甲 &gt; 闪避」「镶嵌辅助宝石中某色占多数」等门控
 * （无比较节点时此类条件无法表达，只能近似或留缺口）。
 *
 * <p>作为 {@link EntityNode} 求值：以当前候选实体为基准（子节点通常为装备 / 宝石读值节点，
 * 天然针对实体）。两子节点可为任意 {@link ValueExpr}（属性 / 装备 / 宝石 / 嵌套 compare 等）。
 */
public record CompareValue(
        ValueExpr left,
        ValueExpr right,
        double leftScale,
        double rightScale,
        ComparisonOperator operator
) implements EntityNode {

    public static final Codec<CompareValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ValueExpr.CODEC.fieldOf("left").forGetter(CompareValue::left),
            ValueExpr.CODEC.fieldOf("right").forGetter(CompareValue::right),
            Codec.DOUBLE.optionalFieldOf("left_scale", 1.0).forGetter(CompareValue::leftScale),
            Codec.DOUBLE.optionalFieldOf("right_scale", 1.0).forGetter(CompareValue::rightScale),
            ComparisonOperator.CODEC.fieldOf("operator").forGetter(CompareValue::operator)
    ).apply(instance, CompareValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        double a = left.evaluate(scope) * leftScale;
        double b = right.evaluate(scope) * rightScale;
        return operator.compare(a, b) ? 1.0 : 0.0;
    }

    /**
     * 条件从句（P3/A）：把左右子树渲染成名词短语，中间插入比较符值键，组合为
     * {@code stat.condition.compare} = "%1$s %2$s %3$s"（左 / 比较符 / 右）。
     * 两侧各自走 {@link ValueExpr#describeQuantity(Locale)}，未命名节点可见降级到 "[?]"。
     */
    @Override
    public List<Component> describeFragment() {
        Locale loc = ClientLocale.get();
        Component leftQ = left.describeQuantity(loc).get(0);
        Component rightQ = right.describeQuantity(loc).get(0);
        return List.of(Component.translatable("stat.condition.compare", leftQ,
                Component.translatable(cmpKey(operator)), rightQ));
    }

    /** 比较运算符 → 值键（符号本身是 locale 中立的，en/zh 同值）。 */
    private static String cmpKey(ComparisonOperator op) {
        return switch (op) {
            case EQUAL -> "stat.value.cmp.eq";
            case NOT_EQUAL -> "stat.value.cmp.ne";
            case LESS_THAN -> "stat.value.cmp.lt";
            case LESS_THAN_OR_EQUAL -> "stat.value.cmp.le";
            case GREATER_THAN -> "stat.value.cmp.gt";
            case GREATER_THAN_OR_EQUAL -> "stat.value.cmp.ge";
        };
    }
}
