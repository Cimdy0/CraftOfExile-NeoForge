package com.cimdy.coe.data.codec.support.math.context.nodes.scope;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.node.ExprValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.math.context.query.Candidate;
import com.cimdy.coe.data.codec.support.math.context.query.Selector;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.OutputMode;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 实体选择器（context_dsl_design.md §3）：统一的实体条件节点，authoring 表面。
 * <p>
 * 方案四重构：本节点不再内联收集 / 过滤 / 排序 / 输出逻辑，而是把 codec 字段翻译为
 * {@link Selector} 泛型管线的一次装配 —— 收集 → 过滤 → 排序 → 数量窗口 → {@link OutputMode} 输出分发。
 * <ul>
 *   <li>无 {@code then} → 聚合读值：{@link OutputMode#COUNT}（匹配数）或
 *       {@link OutputMode#SUM}（对匹配实体 {@code of} 属性累加）；</li>
 *   <li>有 {@code then} → {@link OutputMode#PICK}：挑「一个」候选，指针交换进其作用域，
 *       嵌套体以它为参照求值（零上下文重建）。</li>
 * </ul>
 * 搜索不强制 radius；当前实体即参照参数。筛选统一走 {@code where} filter 表达式（math 变量
 * type:/biome:/dimension:/summoned:/owner:/level:/dist()/candidate. 等，详见 {@code EntityFlagValue} / M5 ④ 解析器；
 * 注：rarity:/camp: 尚未实现，勿在过滤表达式中使用）。
 * 排序仅在声明 {@code sort} 时计算（§11.2），且排序键每候选只求值一次（管线内 decorate-sort-undecorate）。
 * <p>
 * {@code slot} 为预注册实体槽位（由 DamagePipeline.stageTrigger 在每次直接命中时写入）：
 * {@code "hit"}（攻击者侧 = 最近一次直接命中目标）、{@code "hitter"}（受害者侧 = 最近一次直接攻击者）。
 * 无 slot → 全域扫描（EntityQuery.world()）。
 */
public record SelectorNode(
        Optional<MathExpression> where,
        OutputMode mode,
        Optional<Identifier> of,
        Optional<MathExpression> sort,
        Order order,
        Optional<Integer> limit,
        Optional<ValueExpr> then,
        Optional<String> slot
) implements ValueExpr {

    public enum Order { ASC, DESC }

    public static final Codec<SelectorNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MathExpression.CODEC.optionalFieldOf("where")
                    .forGetter(SelectorNode::where),
            Codec.STRING.xmap(s -> OutputMode.valueOf(s.toUpperCase()), OutputMode::name)
                    .optionalFieldOf("mode", OutputMode.COUNT).forGetter(SelectorNode::mode),
            Identifier.CODEC.optionalFieldOf("of").forGetter(SelectorNode::of),
            MathExpression.CODEC.optionalFieldOf("sort").forGetter(SelectorNode::sort),
            Codec.STRING.xmap(s -> Order.valueOf(s.toUpperCase()), Order::name)
                    .optionalFieldOf("order", Order.ASC).forGetter(SelectorNode::order),
            Codec.INT.optionalFieldOf("limit").forGetter(SelectorNode::limit),
            ValueExpr.CODEC.optionalFieldOf("then").forGetter(SelectorNode::then),
            Codec.STRING.optionalFieldOf("slot").forGetter(SelectorNode::slot)
    ).apply(instance, SelectorNode::new));

    @Override
    public double evaluate(ValueScope scope) {
        // 装配管线：收集 → filter 过滤（参照实体 = scope.current()，候选经 candidate./dist() 访问）
        Selector<LivingEntity> selector = Selector.living(collect(scope));
        // 过滤谓词走全库统一口径ExprValue.truth（非零即真）；候选经 evalFilter 挂入 candidate. 前缀
        where.ifPresent(w -> selector.filter(
                c -> ExprValue.truth(scope.evalFilter(Candidate.of(c), w::evaluate))));

        // 排序（仅在声明时计算；键预计算，每候选一次）
        sort.ifPresent(key -> selector.sortBy(scope, key::evaluate, order == Order.DESC));

        // EACH：一对一拆分——逐候选在其作用域求值（then 优先，其次 of 属性，兜底 1.0），
        // 并经 ValueScope.emit 逐对发射 (候选实体, 值)；标量返回和（非拆分上下文即 SUM 语义）
        if (mode == OutputMode.EACH) {
            limit.ifPresent(selector::limit);
            java.util.function.ToDoubleFunction<ValueScope> reader = then.isPresent()
                    ? then.get()::evaluate
                    : of.<java.util.function.ToDoubleFunction<ValueScope>>map(
                            attr -> new AttributeValue(attr)::evaluate)
                        .orElse(s -> 1.0);
            double total = 0;
            for (LivingEntity c : selector.result()) {
                if (c == null) continue;
                double v = selector.inScope(scope, c, reader);
                scope.emit(c, v);
                total += v;
            }
            return total;
        }

        // 输出分发（then 存在 → PICK 优先，兼容旧 JSON 的 mode=count/sum + then 写法）
        if (then.isPresent()) {
            // 无匹配 → chosen 为 null：以空候选进作用域，then 内读值经 default 兜底（§3.2）
            LivingEntity chosen = selector.pick(limit.orElse(1));
            return selector.inScope(scope, chosen, then.get()::evaluate);
        }
        limit.ifPresent(selector::limit);
        if (mode == OutputMode.SUM) {
            Identifier attr = of.orElseThrow(() ->
                    new IllegalStateException("mode=sum 必须提供 of 属性 id"));
            AttributeValue reader = new AttributeValue(attr);
            return selector.sum(scope, reader::evaluate);
        }
        return selector.count();
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("selector", mode.name().toLowerCase()));
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.selector", describeQuantity(ClientLocale.get()).get(0)));
    }

    private List<LivingEntity> collect(ValueScope scope) {
        List<LivingEntity> list = new ArrayList<>();
        if (slot.isPresent()) {
            scope.root().getSlotEntity(slot.get()).ifPresent(list::add);
        } else {
            // 获取实体统一走 Context 数据基站（EntityQuery 全域扫描），读值仍由本节点二次分发
            list.addAll(scope.root().queryEntities(EntityQuery.world()));
        }
        return list;
    }
}
