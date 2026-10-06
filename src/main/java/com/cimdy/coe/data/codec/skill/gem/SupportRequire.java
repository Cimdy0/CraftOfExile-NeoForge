package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 辅助宝石需求 — 专用于辅助宝石（Support Gem）的额外需求类。
 *
 * <p>POE2 机制：辅助宝石本身没有需求（不需要满足属性即可装备 / 链接），
 * 但当辅助宝石链接到技能宝石时，会给该技能宝石增加额外的属性需求
 * （力量 / 敏捷 / 智慧，通常为 +5）。
 *
 * <p>因此本类只持有三个属性字段（无等级），数值使用
 * {@link ExpressionCodec.Value}，并通过 {@link #addToRequire(Require)}
 * 将自身属性累加到技能宝石的 {@link Require} 上。
 */
public record SupportRequire(
        ExpressionCodec.Value strength,
        ExpressionCodec.Value dexterity,
        ExpressionCodec.Value intelligence
) {
    public static final Codec<SupportRequire> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("strength", ExpressionCodec.of(0.0))
                    .forGetter(SupportRequire::strength),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("dexterity", ExpressionCodec.of(0.0))
                    .forGetter(SupportRequire::dexterity),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("intelligence", ExpressionCodec.of(0.0))
                    .forGetter(SupportRequire::intelligence)
    ).apply(instance, SupportRequire::new));

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 将该辅助宝石的额外属性需求累加到基础技能需求上，返回合并后的新需求。
     * <p>多个辅助宝石可叠加（如两个 +5 智慧 -> +10 智慧）。
     * 合并以表达式形式惰性保留，实际数值在施法校验时才求值。
     */
    public Require addToRequire(Require base) {
        return Require.builder()
                .level(base.level())
                .strength(add(base.strength(), strength))
                .dexterity(add(base.dexterity(), dexterity))
                .intelligence(add(base.intelligence(), intelligence))
                .build();
    }

    /** 返回 a + b 的表达式（跳过 0 值以避免无谓包装）。 */
    private static ExpressionCodec.Value add(ExpressionCodec.Value a, ExpressionCodec.Value b) {
        if (isZero(a)) return b;
        if (isZero(b)) return a;
        return new ExpressionCodec.Value(Either.right(
                new ExpressionCodec.BinaryOperation.Add(toExpr(a), toExpr(b))));
    }

    private static boolean isZero(ExpressionCodec.Value v) {
        return v.value().left().map(d -> d == 0.0).orElse(false);
    }

    private static MathExpression toExpr(ExpressionCodec.Value v) {
        Either<Double, MathExpression> e = v.value();
        return e.left().<MathExpression>map(ExpressionCodec.Constant::new).orElse(e.right().get());
    }

    public static class Builder {
        private ExpressionCodec.Value strength = ExpressionCodec.of(0.0);
        private ExpressionCodec.Value dexterity = ExpressionCodec.of(0.0);
        private ExpressionCodec.Value intelligence = ExpressionCodec.of(0.0);

        public Builder strength(ExpressionCodec.Value strength) {
            this.strength = strength;
            return this;
        }

        public Builder dexterity(ExpressionCodec.Value dexterity) {
            this.dexterity = dexterity;
            return this;
        }

        public Builder intelligence(ExpressionCodec.Value intelligence) {
            this.intelligence = intelligence;
            return this;
        }

        public SupportRequire build() {
            return new SupportRequire(strength, dexterity, intelligence);
        }
    }
}
