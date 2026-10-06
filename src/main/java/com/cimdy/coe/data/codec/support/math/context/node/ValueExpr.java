package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.stat.entry.describer.StatDescriber;
import com.cimdy.coe.data.gen.lang.LangProvider;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Context DSL 读值节点（context_dsl_design.md §2.1 / §5）。
 * <p>
 * 每个节点在给定 {@link ValueScope} 上求值，统一输出 {@code double}（原始数值，不做比较/判断）：
 * 属性值、效果等级/时长、计时经过 tick、距离、技能实时状态、生命值、选择器聚合等。
 * 判断（>=1 / ==0 / X<=80）完全在 math 侧进行（§0 分工铁律）。
 * <p>
 * 节点实现自带 {@code CODEC}，由 {@link ValueExprTypes#CODEC} 按 {@code type} 字段数据驱动分派，
 * 可由数据包 JSON 声明（数据驱动）；当前 {@code ExpressionCodec.Variable.from} 经由
 * {@link ValueExprTypes#resolve} 工厂按变量类型前缀构造。
 */
public interface ValueExpr {
    /** 数据驱动分派 codec：按 JSON {@code type} 字段 dispatch 到各节点 CODEC（§2.3）。 */
    Codec<ValueExpr> CODEC = ValueExprTypes.CODEC;

    /**
     * <b>列表 → 单节点</b>（复式收口）：把 {@code List<ValueExpr>} 压成<b>一个</b> {@link ValueExpr}，
     * 使持有方（如 {@code Stat} 的条件段）只需一个字段即可表达「零到多条条件」：
     * <ul>
     *   <li>空列表 → {@link ValueExprGroup#EMPTY}（空组，恒真）；</li>
     *   <li>单元素 → 该元素本身（不额外套壳，避免无意义嵌套）；</li>
     *   <li>多元素 → {@link ValueExprGroup}（逻辑与）。</li>
     * </ul>
     * 恒不为 {@code null}，调用方无需判空。
     */
    static ValueExpr of(List<ValueExpr> nodes) {
        if (nodes == null || nodes.isEmpty()) return ValueExprGroup.EMPTY;
        if (nodes.size() == 1) return nodes.get(0);
        return new ValueExprGroup(nodes);
    }

    /**
     * <b>单节点 → 列表</b>：本节点视为「只含自己一项」；若本节点就是
     * {@link ValueExprGroup}，返回其子节点（组在构造时已压平，故任意深度展开都是原子节点）。
     * 与 {@link #of} 互逆，供 {@code StatEntry#conditions()} 这类「接口仍返回 {@code List}」的旧形接口对接。
     */
    default List<ValueExpr> asList() {
        return this instanceof ValueExprGroup group ? group.nodes() : List.of(this);
    }

    /** 在给定 scope（当前实体 + 根 ExpressionContext）上求值，输出 double。 */
    double evaluate(ValueScope scope);

    /**
    * <b>唯一布尔判定口径的节点级入口</b>：在给定 scope 上求值后按「非零即真」判定。
    * <p>
    * 实现内部委托 {@link ExprValue#truth(double)}（全库唯一定义），故凡是把本节点
    * 的求值结果当布尔用的地方（条件分支 / 过滤谓词 / 逻辑与的子项等）都应调用本方法，
    * 而<b>不得</b>自行写 {@code evaluate(scope) != 0} 或 {@code >= 1.0}。
    *
    * @param scope 求值作用域（当前实体 + 根数据基站）
    * @return 求值结果非零则{@code true}
    */
    default boolean test(ValueScope scope) {
        return ExprValue.truth(evaluate(scope));
    }

    /**
     * 把内部 snake_case 标识（状态 id / 事件名 / 模式串）转为「标题化」可读英文，用于条件短语的翻译参数。
     * 例：{@code "low_life" → "Low Life"}、{@code "caused_ailment" → "Caused Ailment"}、{@code "form_bear" → "Form Bear"}。
     * 仅作 en_us 原型占位（非逐字翻译）；逐值翻译键是 P4 范畴。
     */
    public static String humanize(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : raw.split("_")) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    /**
     * 反向拼装翻译（P2）：本节点作为「条件段（何时）」贡献的融合短语片段。
     * <p>
     * 返回<b>自包含</b>的 {@link Component} 分句（含自身连接词，如 "while Ignited" / "on Low Life" /
     * "if you've recently been Hit" / "against Undead"），由外层 {@code StatDescriber.assemble}
     * 收集多条后按 {@code " and "} 连接并拼入词缀行尾。
     * <p>
     * <b>降级铁律（严禁静默丢语义）</b>：未覆写此方法的节点（即本切片未覆盖的条件类型，
     * 如 {@code CompareValue}/{@code Presence*} 等复杂比较）一律回退到 {@code stat.condition.unknown}
     * （en = "[!] condition"），在 tooltip 中<b>可见</b>地标记未翻译，而非什么都不渲染。
     * 覆盖的节点类型各自覆写为可译键（见各节点实现 + {@code data/gen/LangProvider.java}）。
     */
    default List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.unknown"));
    }

    /**
     * 把本节点产出为<b>名词短语</b>（用于 {@link CompareValue} 等操作数位置），与
     * {@link #describeFragment()} 返回的「条件从句」区分（「生命」vs「当生命低于 35% 时」）。
     * <p>
     * <b>降级铁律</b>：未覆写（即本节点无可读名称）时回落到 {@code stat.quantity.unknown}
     * （en = "[?]"），在 tooltip 中<b>可见</b>地标记未命名，而非什么都不渲染。
     * 名词短语通常来自值键（{@code stat.value.<ns>.<id>}）或权威键（{@code attribute.name.<path>}）。
     */
    default List<Component> describeQuantity() {
        return describeQuantity(Locale.ROOT);
    }

    default List<Component> describeQuantity(Locale locale) {
        return List.of(Component.translatable("stat.quantity.unknown"));
    }
}
