package com.cimdy.coe.data.codec.support.stat.entry.describer;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;

import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.query.AmountSourceNamer;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.data.codec.support.value.Value;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * P3 反汇编：把 per-X 表达式数值（如 {@code $value * attribute:coe:strength / $per}、
 * 可选外层 {@code min(..., $cap)}）反解为「每 N 点来源给 X」的可读片段。
 *
 * <p>约 80 条 expression-valued 词缀若无此类会塌缩成纯属性名（无数字），
 * 本类让数字可见。详见 {@code research/stat_translation_plan.md} §P3。
 */
public final class StatAmountDescriber {

    private StatAmountDescriber() {
    }

    /**
     * per-X 反解结果：基准值 / 来源节点 / 每多少（除数） / 封顶值（可空）。
     * <p>基准值的口径取决于调用方的运算（ADD_VALUE 直接作为点数；ADD_MULTIPLIED_* 视作百分比，渲染时 ×100）。
     */
    public record PerXInfo(double baseVal, ValueExpr srcNode, double perVal, @Nullable Double capVal) {
    }

    /**
     * 检测 per-X 表达式形状并在匹配时反解出 {@link PerXInfo}；不匹配返回 null（调用方回落到 [!] amount）。
     * <p>规范形状（{@link com.cimdy.coe.data.codec.support.math.MathParser} 中缀左结合解析得到）：
     * {@code Divide( Multiply( Variable($value), Variable(<src>) ), Variable($per) )}，
     * 可选地再包一层 {@code Min( 上述, Variable($cap) )}（两参数顺序任意）。
     * {@code <src>} 为来源读值节点（AttributeValue / EventHistoryValue …）；退化情形（{@code per("1.0")}）
     * 的常量来源不匹配，回落到 [!] amount。
     */
    public static @Nullable PerXInfo detectPerX(MathExpression expr) {
        MathExpression inner = expr;
        @Nullable Double capVal = null;
        if (inner instanceof ExpressionCodec.BinaryOperation.Min min) {
            MathExpression a = min.left();
            MathExpression b = min.right();
            if (a instanceof ExpressionCodec.Variable v && isDollar(v, "cap")) {
                capVal = v.defaultValue();
                inner = b;
            } else if (b instanceof ExpressionCodec.Variable v && isDollar(v, "cap")) {
                capVal = v.defaultValue();
                inner = a;
            } else {
                return null;
            }
        }
        // Divide( Multiply($value, <src>), $per )
        if (!(inner instanceof ExpressionCodec.BinaryOperation.Divide div)) return null;
        if (!(div.right() instanceof ExpressionCodec.Variable perVar) || !isDollar(perVar, "per")) return null;
        if (!(div.left() instanceof ExpressionCodec.BinaryOperation.Multiply mul)) return null;
        ExpressionCodec.Variable valueVar;
        ValueExpr srcNode;
        if (mul.left() instanceof ExpressionCodec.Variable v && isDollar(v, "value")
                && mul.right() instanceof ExpressionCodec.Variable s) {
            valueVar = v;
            srcNode = s.node();
        } else if (mul.right() instanceof ExpressionCodec.Variable v && isDollar(v, "value")
                && mul.left() instanceof ExpressionCodec.Variable s) {
            valueVar = v;
            srcNode = s.node();
        } else {
            return null;
        }
        double baseVal = valueVar.defaultValue();
        double perVal = perVar.defaultValue();
        return new PerXInfo(baseVal, srcNode, perVal, capVal);
    }

    /** 是否为名为 {@code name} 的本地 {@code $} 变量（VarRef）；MathParser 已剥去前导 '$'。 */
    private static boolean isDollar(ExpressionCodec.Variable v, String name) {
        return v.node() instanceof VarRef vr && vr.name().equals(name);
    }

    /**
     * 用条目参数槽（{@code $value}/$per/$cap}）覆盖默认值：真实 per-X 数据把幅度存在
     * {@code StatEntry.params()}（经 MathParser 解析的 AST 默认值为 0），describe 无运行时上下文，
     * 故此处优先取参数槽里的常量幅度，缺省再回落 AST 默认。
     */
    public static PerXInfo resolveValues(PerXInfo raw, Map<String, ExpressionCodec.Value> params) {
        double base = pick(params, "value", raw.baseVal());
        double per = pick(params, "per", raw.perVal());
        Double cap = raw.capVal();
        if (params.containsKey("cap")) {
            cap = pick(params, "cap", 0.0);
        }
        return new PerXInfo(base, raw.srcNode(), per, cap);
    }

    private static double pick(Map<String, ExpressionCodec.Value> params, String key, double fallback) {
        ExpressionCodec.Value v = params.get(key);
        if (v != null && v.value().left().isPresent()) return v.value().left().get();
        return fallback;
    }

    /** 常量数值（无表达式）→ 返回 null，常量由调用方自行渲染。供测试与调用方判空。 */
    public static @Nullable Component describeFragment(
            ExpressionCodec.Value amount, Locale locale, AttributeModifier.Operation op) {
        return describeFragment(amount, Map.of(), locale, op);
    }

    /** 完整 Modifier 路径：检测 + 参数解析 + 渲染（AST 默认值被参数槽覆盖）。 */
    public static @Nullable Component describeFragment(
            ExpressionCodec.Value amount, Map<String, ExpressionCodec.Value> params,
            Locale locale, AttributeModifier.Operation op) {
        if (amount.value().left().isPresent()) return null;          // 常量：调用方处理
        if (amount.value().right().isEmpty()) return null;
        PerXInfo raw = detectPerX(amount.value().right().get());
        if (raw == null) return null;
        PerXInfo info = resolveValues(raw, params);
        return describeModifier(info, locale, op);
    }

    /**
     * per-X 的「幅度」片段 —— 只含数值本体，<b>不含</b> per 子句也<b>不含</b>属性名（如 {@code "+1%"} / {@code "+5"}）。
     * <p>拆出来是为了让调用方（{@code ModifierStatEffect}）把「数值 + 属性名」与「per 子句」作为
     * 两个独立参数交给 {@code stat.fmt.modifier} 模板，各 locale 才能自行决定语序
     * （英文「+1% Damage per 15 Strength」/ 中文「每 15 力量，伤害 +1%」）。
     */
    public static Component valueFragment(PerXInfo info, Locale locale, AttributeModifier.Operation op) {
        if (op == AttributeModifier.Operation.ADD_VALUE) {
            return Component.literal(StatNumberFormat.formatSigned(info.baseVal(), locale));
        }
        // ADD_MULTIPLIED_BASE / ADD_MULTIPLIED_TOTAL → 百分比口径（×100）。
        return Component.literal(StatNumberFormat.formatSigned(info.baseVal() * 100.0, locale) + "%");
    }

    /**
     * per-X 的「每 N 来源」子句（含可选封顶），如 {@code "per 15 Strength"} / {@code "每 15 力量"}。
     * 与 {@link #valueFragment} 分离，供外层模板按 locale 重排语序。
     *
     * <p><b>P4 值键化</b>：来源名不再注入裸英文 literal，改由
     * {@link AmountSourceNamer#name} 产出可译的 {@code stat.value.*} 组件，
     * 使中文下渲染「每 15 力量」而非「每 15 Strength」；键缺失时
     * {@link StatValueKey} 回落到 humanize 英文，英文输出逐字不变。
     */
    public static Component perClause(PerXInfo info, Locale locale, boolean percentage) {
        Component per = Component.translatable("stat.amount.per",
                Component.literal(StatNumberFormat.format(info.perVal(), locale)),
                AmountSourceNamer.name(info.srcNode()));
        if (info.capVal() == null) return per;
        String capStr = percentage
                ? StatNumberFormat.format(info.capVal() * 100.0, locale) + "%"
                : StatNumberFormat.format(info.capVal(), locale);
        return per.copy().append(Component.literal(" "))
                .append(Component.translatable("stat.amount.up_to", Component.literal(capStr)));
    }

    /** 渲染 Modifier per-X 片段（幅度 + per 子句，不含属性名）。供测试与旧调用点。 */
    public static Component describeModifier(PerXInfo info, Locale locale, AttributeModifier.Operation op) {
        MutableComponent frag = (MutableComponent) valueFragment(info, locale, op);
        frag.append(Component.literal(" "))
                .append(perClause(info, locale, op != AttributeModifier.Operation.ADD_VALUE));
        return frag;
    }

    /** 渲染 Damage per-X 片段（deal %s 主体 + per 子句；无属性名）。 */
    public static Component describeDamage(PerXInfo info, Locale locale) {
        MutableComponent frag = (MutableComponent) Component.translatable("stat_effect.deal",
                Component.literal(StatNumberFormat.format(info.baseVal(), locale)));
        frag.append(Component.literal(" ")).append(perClause(info, locale, false));
        return frag;
    }
}
