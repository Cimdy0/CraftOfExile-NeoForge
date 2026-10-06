package com.cimdy.coe.data.codec.skill.phase.delivery.area;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 球形范围。
 */
public record SphereShape(ExpressionCodec.Value radius) implements Shape {
    public static final Codec<SphereShape> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", ExpressionCodec.of(3.0))
                    .forGetter(SphereShape::radius)
    ).apply(i, SphereShape::new));

    @Override public Shape.Type<?> getType() { return ShapeTypes.SPHERE; }

    @Override public double sampleRadius(SkillContext ctx) { return radius().getFloat(ctx); }
}
