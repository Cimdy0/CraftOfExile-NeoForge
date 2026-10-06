package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 立方体范围 — 以中心为原点的各向同性半边长（half-extent）。
 */
public record CubeShape(ExpressionCodec.Value halfExtent) implements Shape {
    public static final Codec<CubeShape> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("half_extent", ExpressionCodec.of(3.0))
                    .forGetter(CubeShape::halfExtent)
    ).apply(i, CubeShape::new));

    @Override public Shape.Type<?> getType() { return ShapeTypes.CUBE; }

    /** 代表性半径取半边长。 */
    @Override public double sampleRadius(SkillContext ctx) { return halfExtent().getFloat(ctx); }
}
