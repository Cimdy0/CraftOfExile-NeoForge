package com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 引用 Read 模板注入的本地变量（按名查找 {@link ExpressionContext} 的 data map）。
 * <p>
 * 与 {@code ExpressionCodec.Read} 的命名模板引用区分：{@code Read} 查 {@code LoadEvent#EXPRESSION_MAP}
 * （共享命名表达式定义，如 {@code charge_scale}），而本节点查当前表达式求值时由 {@code Read} 注入的
 * 局部变量（如 level_table 模板的 "1"~"40"、"base"、"per"）。
 * 未注入（名字不存在）返回 NaN，交由外层 {@code Variable} 的 defaultValue 兜底（context_dsl_design.md §8）。
 * math 侧对应 {@code $name} 形式（MathParser），解析期即绑定为 VarRef，不再走旧的 String 求值分派。
 */
public record VarRef(String name) implements ValueExpr {
    public static final Codec<VarRef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(VarRef::name)
            ).apply(instance, VarRef::new));

    @Override
    public double evaluate(ValueScope scope) {
        ExpressionContext ctx = scope.root();
        ExpressionCodec.Value v = ctx.getInjectedVar(name);
        return v != null ? ExpressionCodec.getValue(v.value(), ctx) : Double.NaN;
    }

    /** 名词短语：引擎注入变量名 → 值键 {@code stat.value.var.<name>}，键缺失则回落 humanize 英文。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("var", name));
    }
}
