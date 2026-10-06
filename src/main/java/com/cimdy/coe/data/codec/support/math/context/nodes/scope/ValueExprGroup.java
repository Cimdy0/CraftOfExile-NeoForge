package com.cimdy.coe.data.codec.support.math.context.nodes.scope;

import com.cimdy.coe.data.codec.support.stat.entry.describer.StatDescriber;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 复式条件节点 —— {@link ValueExpr} 的<b>组合（复合）形态</b>。
 *
 * <p>本类存在的意义只有一条：<b>让「一组条件」本身也是一个 {@link ValueExpr}</b>。
 * 数据即 {@code List<ValueExpr>}（下称「子节点」），且子节点允许是本类，
 * 故可任意层嵌套 = 复式（recursive / compound）。于是调用方拿到的永远是一个
 * {@link ValueExpr}，不必再在外面套一层 {@code List<ValueExpr>} 或 {@code List<Identifier>} ——
 * 这正是 {@code Stat} 能把条件段压缩成<b>单个字段</b>的前提。
 *
 * <p><b>语义</b>：子节点之间为<b>逻辑与</b>（全部非 0 才为 1），与 {@code StatEntry#satisfies}
 * 对多条条件「全部通过」的既有口径一致；<b>空组恒真</b>（等价于「无条件的条目」）。
 *
 * <p><b>序列化</b>：JSON 全量形式 {@code {"type":"group","nodes":[…]}}，由
 * {@link ValueExprTypes} 以 {@code group} 键注册；无中缀简写（同 {@code entity_type}）。
 *
 * <p><b>拆回列表</b>：{@link #nodes()} 直接给出子节点；嵌套组在构造时已被压平，
 * 故任意深度的组 {@link #nodes()} 都返回「最内层原子节点」的列表（不含中间层空壳）。
 */
public final class ValueExprGroup implements ValueExpr {

    /** 空组（无条件条目的规范取值）：不可变、可共享。 */
    public static final ValueExprGroup EMPTY = new ValueExprGroup(List.of());

    public static final MapCodec<ValueExprGroup> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ValueExpr.CODEC.listOf().fieldOf("nodes").forGetter(ValueExprGroup::nodes)
    ).apply(i, ValueExprGroup::new));

    public static final Codec<ValueExprGroup> CODEC = MAP_CODEC.codec();

    private final List<ValueExpr> nodes;

    public ValueExprGroup(List<ValueExpr> nodes) {
        this.nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }

    /** 组构造（便捷）。 */
    public static ValueExprGroup of(List<ValueExpr> nodes) {
        return new ValueExprGroup(nodes);
    }

    /** 子节点列表（已去中间层空壳；空组返回空列表）。 */
    public List<ValueExpr> nodes() {
        return nodes;
    }

    /** 是否无条件（空组）。 */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    // ---------------------------------------------------------------- 求值

    /**
     * 逻辑与求值：任一子节点求值结果为 0 即返回 0；全部非 0 返回 1。
     * <b>空组返回 1（恒真）</b>——「无条件」在条件语义下即「永远通过」。
     */
    @Override
    public double evaluate(ValueScope scope) {
        for (ValueExpr node : nodes) {
            // 子项真假走节点级统一入口 test()（内部即 ExprValue.truth）
            if (!node.test(scope)) {
                return 0;
            }
        }
        return 1;
    }

    // ---------------------------------------------------------------- 描述拼接

    /**
     * 条件从句拼接：把各子节点的 {@link #describeFragment} 收集后以 {@code " and "} 连接成
     * 单个自包含分句（与 {@code StatDescriber} 收集多条条件后的连接口径一致）。
     * <b>降级铁律</b>：全部子节点都无可读片段时回落 {@code stat.condition.unknown}（可见地标记未翻译）。
     */
    @Override
    public List<Component> describeFragment() {
        List<Component> parts = new ArrayList<>();
        for (ValueExpr node : nodes) {
            List<Component> fragment = node.describeFragment();
            if (fragment != null && !fragment.isEmpty()) {
                parts.addAll(fragment);
            }
        }
        return parts.isEmpty()
                ? List.of(Component.translatable("stat.condition.unknown"))
                : List.of(join(parts, " and "));
    }

    /** 名词短语拼接（同 {@link #describeFragment}，用于 {@link CompareValue} 等操作数位置）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        List<Component> parts = new ArrayList<>();
        for (ValueExpr node : nodes) {
            List<Component> quantity = node.describeQuantity(locale);
            if (quantity != null && !quantity.isEmpty()) {
                parts.addAll(quantity);
            }
        }
        return parts.isEmpty()
                ? List.of(Component.translatable("stat.quantity.unknown"))
                : List.of(join(parts, " and "));
    }

    /**
     * 以分隔符连接若干分句为单个 {@link Component}（首个元素前不加分隔符）。
     *
     * <p>累加变量须声明为 {@link MutableComponent} 而非 {@link Component}：
     * 后者接口上<b>没有</b>{@code append}，只有 {@code MutableComponent#append} 才有。
     */
    private static Component join(List<Component> parts, String separator) {
        MutableComponent result = null;
        for (Component part : parts) {
            if (result == null) {
                result = part.copy();
            } else {
                result.append(Component.literal(separator)).append(part);
            }
        }
        return result == null ? Component.empty() : result;
    }

    @Override
    public String toString() {
        return "ValueExprGroup" + nodes;
    }
}
