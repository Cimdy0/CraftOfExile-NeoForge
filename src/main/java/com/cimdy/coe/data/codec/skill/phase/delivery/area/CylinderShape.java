package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 圆柱范围 — 半径 + 高度（沿 Y 轴的半高）。
 */
public record CylinderShape(ExpressionCodec.Value radius, ExpressionCodec.Value height) implements Shape {
    public static final Codec<CylinderShape> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", ExpressionCodec.of(3.0))
                    .forGetter(CylinderShape::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("height", ExpressionCodec.of(4.0))
                    .forGetter(CylinderShape::height)
    ).apply(i, CylinderShape::new));

    @Override public Shape.Type<?> getType() { return ShapeTypes.CYLINDER; }

    /** 代表性半径取底面半径。 */
    @Override public double sampleRadius(SkillContext ctx) { return radius().getFloat(ctx); }
}
