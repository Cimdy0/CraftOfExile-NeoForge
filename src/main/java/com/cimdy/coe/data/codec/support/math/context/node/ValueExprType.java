package com.cimdy.coe.data.codec.support.math.context.node;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/**
 * Context 读值节点的类型描述符 —— 一条注册项聚合一个检测的全部序列化面向：
 * <ul>
 *   <li>{@code key}：JSON {@code type} 字段（数据驱动分派键）</li>
 *   <li>{@code clazz}：节点实现类（encode 时按实例类反查）</li>
 *   <li>{@code codec}：节点自身 CODEC（JSON 全量形式）</li>
 *   <li>{@code infix}：节点 → 中缀变量串（math 表达式往返；可为 null 表示无变量简写形式）</li>
 *   <li>{@code resolver}：中缀变量前缀 → 节点（parse 方向；可为 null 表示仅支持 JSON 全量形式）</li>
 * </ul>
 * 注册入口 {@link ValueExprTypes#register}。新增一个检测 = 写一个 {@link ValueExpr} 实现
 * + 一行 register；resolve / toInfix / codec 分派 / encode 反查全部由注册表自动推导，无需再改多处。
 */
public record ValueExprType<T extends ValueExpr>(
        String key,
        Class<T> clazz,
        Codec<T> codec,
        Function<T, String> infix,
        VariableResolver resolver
) {
    /** 中缀变量解析器：按变量类型前缀构造节点；不匹配返回 {@code null}（继续尝试下一注册项）。 */
    @FunctionalInterface
    public interface VariableResolver {
        ValueExpr resolve(String type, Identifier id, double def);
    }

    /** 完整注册项（变量简写 + JSON 全量双形式）。 */
    public static <T extends ValueExpr> ValueExprType<T> of(
            String key, Class<T> clazz, Codec<T> codec,
            Function<T, String> infix, VariableResolver resolver) {
        return new ValueExprType<>(key, clazz, codec, infix, resolver);
    }

    /** 仅 JSON 全量形式（结构复杂、无变量简写的节点，如 select / equipment_match）。 */
    public static <T extends ValueExpr> ValueExprType<T> of(
            String key, Class<T> clazz, Codec<T> codec, Function<T, String> infix) {
        return new ValueExprType<>(key, clazz, codec, infix, null);
    }

    /** 以本类型的 infix 函数反串化节点实例；无 infix 返回 null。 */
    @SuppressWarnings("unchecked")
    String infixOf(ValueExpr node) {
        return infix == null ? null : infix.apply((T) node);
    }
}
