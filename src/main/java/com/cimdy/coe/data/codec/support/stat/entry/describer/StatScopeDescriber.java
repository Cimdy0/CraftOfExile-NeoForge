package com.cimdy.coe.data.codec.support.stat.entry.describer;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * <b>作用域外溢 / 转换从句</b>——把 {@link StatEntry#spillTo()}（COPY）与
 * {@link StatEntry#convertTo()}（MOVE）渲染成一句挂在效果片段末尾的可译从句。
 *
 * <p>两个字段只改「本条目的数值计入哪些作用域」，不改效果本身。若 UI 不显示它们，
 * 一条 spill / convert 条目与其派生自的普通条目文本逐字相同，玩家无从分辨；
 * MOVE 侧更是 outright 说反——文字仍宣称来源属性，而该数值已完全不再计入来源。
 *
 * <p><b>两个从句必须读起来相反</b>（语义相反，不可互换）：
 * <ul>
 *   <li>spill（COPY）= 「also applies to X」——两侧各计一次，来源仍计入；</li>
 *   <li>convert（MOVE）= 「counts as X instead」——只计 X，来源不再计入。</li>
 * </ul>
 */
public final class StatScopeDescriber {

    /** spill（COPY）从句模板：{@code %1$s} 效果行、{@code %2$s} 作用域标签。 */
    public static final String SPILL_KEY = "stat.entry.spill_scope";
    /** convert（MOVE）从句模板：{@code %1$s} 效果行、{@code %2$s} 作用域标签。 */
    public static final String CONVERT_KEY = "stat.entry.convert_scope";

    /** 模板键缺失时的可见英文兜底（{@code %1$s} 效果行、{@code %2$s} 作用域标签）。 */
    private static final String SPILL_FALLBACK = "%1$s (also applies to %2$s)";
    /** 模板键缺失时的可见英文兜底。 */
    private static final String CONVERT_FALLBACK = "%1$s (counts as %2$s instead)";

    private StatScopeDescriber() {
    }

    /**
     * 把作用域从句套到已渲染的效果行上（行内联为 {@code %1$s}）。
     *
     * <p>两个作用域都未声明时原样返回 {@code line}——绝大多数条目走这条零成本返回。
     * 转换优先于外溢命中（两者互斥，构造期已校验），与 {@link StatEntry#effectiveTags()}
     * 的分支次序一致。
     *
     * <p>本方法的入口刻意收 {@link GateExpr} 而非只有 {@link StatEntry}：只渲染效果片段的
     * 调用点（如按聚合身份逐行渲染的物品 tooltip 与天赋面板）手里只有身份上的两个作用域、
     * 没有整条条目——为此伪造一个 {@code StatEntry.of(...)} 会把两个字段一并丢掉，
     * 文本又退回成与来源条目逐字相同。
     */
    public static Component wrap(Component line, GateExpr spillTo, GateExpr convertTo) {
        if (convertTo != null && !convertTo.isNever()) {
            return Component.translatableWithFallback(CONVERT_KEY, CONVERT_FALLBACK, line, tags(convertTo));
        }
        if (spillTo != null && !spillTo.isNever()) {
            return Component.translatableWithFallback(SPILL_KEY, SPILL_FALLBACK, line, tags(spillTo));
        }
        return line;
    }

    /**
     * 作用域表达式 -> 可译标签组件。
     *
     * <p>按 {@link GateExpr} 的布尔树逐节点渲染，不摊平为 {@link GateExpr#leaves()}：
     * 摊平会把 {@code !fire} 与 {@code fire|cold} 渲染成同一串标签，读起来意思相反。
     */
    private static Component tags(GateExpr expr) {
        return node(expr.root());
    }

    private static Component node(GateExpr.Node n) {
        if (n instanceof GateExpr.Leaf leaf) {
            return StatValueKey.of("scope_tag", leaf.tag());
        }
        if (n instanceof GateExpr.Not not) {
            return StatValueKey.not().copy().append(" ").append(node(not.inner()));
        }
        // 与：沿用 stat.condition.join_and（英文 " and " / 中文 " 且 "）。
        // 或：沿用 MarkerStatEffect 的 conditionTags 约定用 "/" 连接（并列备选，非同时成立）。
        boolean or = n instanceof GateExpr.Or;
        List<GateExpr.Node> parts = switch (n) {
            case GateExpr.And a -> a.parts();
            case GateExpr.Or o -> o.parts();
            default -> List.of();
        };
        MutableComponent c = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) c.append(or ? Component.literal("/") : Component.translatable("stat.condition.join_and"));
            c.append(node(parts.get(i)));
        }
        return c;
    }
}
