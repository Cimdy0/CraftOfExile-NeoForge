package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.meta.DataSourceContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Map;

public interface MathExpression {
    // 必须惰性初始化，否则 MAP_CODEC 在静态初始化时会触发 MathExpressions 类加载，
    // 而 MathExpressions 注册具体表达式类型（如 Negate）时又会引用 MathExpression.CODEC，
    // 形成静态初始化死循环（CODEC 此时为 null）。
    Codec<Map<Type<?>, MathExpression>> MAP_CODEC = Codec.lazyInitialized(() -> Codec.dispatchedMap(
            MathExpressions.REGISTRY.byNameCodec(),
            Type::codec));

    // 中缀字符串格式：读=解析字符串为 AST，写=AST 序列化为中缀字符串。
    // JSON 中表达式一律为字符串，如 "base + skill.level / per * add"。
    Codec<MathExpression> CODEC = Codec.lazyInitialized(() -> Codec.STRING.flatXmap(
            str -> {
                try {
                    return DataResult.success(MathParser.parse(str));
                } catch (MathParser.ParseException e) {
                    // e.getMessage() 已包含来源 id、表达式原文与出错位置插入符（见 MathParser.ParseException）
                    return DataResult.error(() -> "表达式解析失败: " + e.getMessage());
                }
            },
            expr -> {
                try {
                    return DataResult.success(MathParser.toInfixString(expr));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "表达式序列化失败"
                            + com.cimdy.coe.data.codec.support.meta.DataSourceContext.describe() + ": " + e.getMessage());
                }
            }
    ));

    // 获取表达式的注册类型
    Type<?> getType();

    /**
     * 主求值签名（方案四）：AST 全程传递 {@link ValueScope}（root 数据基站 + current 候选指针）。
     * 选择器 / 谓词候选循环仅做指针交换即可让整棵表达式以候选为当前实体求值，零上下文重建。
     */
    double evaluate(ValueScope scope);

    /** 便捷入口：以 ctx 持有者实体为当前候选组装求值令牌后求值（消费端兼容签名）。 */
    default double evaluate(ExpressionContext context) {
        return evaluate(ValueScope.of(context));
    }

    record Type<T extends MathExpression>(Codec<T> codec) {
    }
}
