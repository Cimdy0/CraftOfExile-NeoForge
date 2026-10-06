package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.ExprValue;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.query.Candidate;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 过滤节点（{@code type:"filter"}）：把既有 {@link ValueExpr} <b>谓词</b>复用为「只保留谓词成立的实体」。
 * <p>
 * 这是<b>能力复用，不是兼容 shim</b>：既有 55 键 / 30+ 实现类与 486 条 datagen 条件（"被你点燃" /
 * "生命低于 30%" / "稀有度为传奇" …）原样作为过滤器使用，不为新模型重写任何一个值节点。
 *
 * <p><b>求值口径与既有模型完全一致</b>（见 {@code GateExpr#matches / StatEngine#conditionsHold}）：
 * <ul>
 *   <li>{@code ValueScope#current()} = <b>参照者 A</b>（裸读 = 自侧，与既有 stat 求值同一约定）；</li>
 *   <li>被判断的候选挂入 {@code filterCandidate}（{@code candidate.<attr>} 前缀读它）；</li>
 *   <li>{@code StatContext#sourceEntity()} = <b>候选实体</b>（{@code source.<...>} 前缀与
 *       {@code ignited_by_source} 这类「对方 × 自侧」谓词据此求值）。</li>
 * </ul>
 * 且谓词判定沿用既有约定：<b>{@code evaluate != 0} 即真</b>。
 *
 * <p>{@code of} 给出入参实体集（缺省为 {@link ConditionScope#base()}，即事件上下文锁定的目标）。
 *
 * <pre>
 * {"type":"filter","of":{"type":"anchor","anchor":"killed"},
 *  "where":{"type":"ignited_by_source"}}
 * </pre>
 */
public record FilterCondition(
        ValueExpr where,
        Optional<StatCondition> of
) implements StatCondition {

    public static Codec<FilterCondition> codec(Codec<StatCondition> self) {
        return RecordCodecBuilder.create(instance -> instance.group(
                ValueExpr.CODEC.fieldOf("where").forGetter(FilterCondition::where),
                self.optionalFieldOf("of").forGetter(FilterCondition::of)
        ).apply(instance, FilterCondition::new));
    }

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        List<LivingEntity> base = of != null && of.isPresent() ? of.get().resolve(scope) : scope.base();
        if (where == null || base.isEmpty()) return List.of();
        LivingEntity reference = scope.owner();
        if (reference == null) return List.of();

        // 事件作用域：触发型路径下原样注入（标签 / 技能 / 事件名 / 命中上下文），仅把 sourceEntity 换成候选
        StatContext statBase = scope.statContext() != null
                ? scope.statContext()
                : new StatContext(Set.of(), null, null, scope.eventName(), scope.hitContext());
        ExpressionContext ec = statBase.evalBase(reference);
        if (ec == null) return List.of();

        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity candidate : base) {
            if (candidate == null) continue;
            ec.pushStatScope(statBase.withSourceEntity(candidate));
            try {
                ValueScope cs = new ValueScope(reference, ec);
                // 谓词判定沿用既有约定（ConditionBridge 记录的 evaluate != 0 即真），
                // 现统一走 ExprValue.truth这一个落点；候选挂入 filterCandidate（candidate. 前缀）
                if (ExprValue.truth(cs.evalFilter(Candidate.of(candidate), where::evaluate))) {
                    out.add(candidate);
                }
            } finally {
                ec.popStatScope();
            }
        }
        return out;
    }

    /** 便捷构造：过滤入参集（{@link ConditionScope#base()}）。 */
    public static FilterCondition of(ValueExpr where) {
        return new FilterCondition(where, Optional.empty());
    }

    /** 便捷构造：先解析 {@code of} 得到入参集，再过滤。 */
    public static FilterCondition of(ValueExpr where, @Nullable StatCondition of) {
        return new FilterCondition(where, Optional.ofNullable(of));
    }
}
