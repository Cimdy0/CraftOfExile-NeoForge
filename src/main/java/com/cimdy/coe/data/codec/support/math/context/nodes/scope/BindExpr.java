package com.cimdy.coe.data.codec.support.math.context.nodes.scope;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.ValueTarget;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.query.Candidate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 实体域绑定组合子（值模型统一内核 · 强复用落点）：把 inner 绑到某个实体域求值。
 * <p>target ∈ {SELF, SOURCE(交互对方), CANDIDATE(filter 候选), SLOT(name)}。
 * 取代 {@code SourceScopeNode} + {@code CandidateScopeNode} + {@code SelectorNode} 内部 per-candidate 逻辑，
 * 单一实现、处处复用——任何「换实体读值」都只写 inner + 选 target，不再为每种组合造专用节点类。</p>
 */
public record BindExpr(ValueTarget target, ValueExpr inner, Optional<String> slot) implements ValueExpr {

    public BindExpr(ValueTarget target, ValueExpr inner) {
        this(target, inner, Optional.empty());
    }

    public static final Codec<BindExpr> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            Codec.STRING.xmap(s -> ValueTarget.valueOf(s.toUpperCase(Locale.ROOT)), Enum::name)
                                    .fieldOf("target").forGetter(BindExpr::target),
                            ValueExpr.CODEC.fieldOf("node").forGetter(BindExpr::inner),
                            Codec.STRING.optionalFieldOf("slot").forGetter(BindExpr::slot))
                    .apply(instance, (t, n, s) -> new BindExpr(t, n, s)));

    @Override
    public double evaluate(ValueScope scope) {
        return switch (target) {
            case SELF -> inner.evaluate(scope);
            case SOURCE -> {
                LivingEntity other = scope.sourceEntity();
                if (other == null) yield 0D;
                yield scope.evalIn(Candidate.of(other), inner::evaluate);
            }
            case CANDIDATE -> scope.evalIn(scope.candidateEntity(), inner::evaluate);
            case SLOT -> {
                Optional<LivingEntity> e = scope.root().getSlotEntity(slot.orElse(""));
                if (e.isEmpty()) yield 0D;
                yield scope.evalIn(Candidate.of(e.get()), inner::evaluate);
            }
        };
    }

    @Override
    public List<Component> describeFragment() {
        return inner.describeFragment();
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return inner.describeQuantity(locale);
    }
}
