package com.cimdy.coe.data.codec.skill.phase.delivery.impl.area;

import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;

import com.cimdy.coe.data.codec.skill.phase.delivery.area.AreaDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.Shape;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.Set;

/**
 * 统一范围投递 —— 以 {@link AreaGeometry} 轴覆盖范围爆发 / 持续危害 / 残留拾取 / 光束 / 波 五种几何（D4丙 对称归并）。
 *
 * <p>五个子形态在记录层共存，运行时由 {@code AreaDeliveryExecutor} 按 {@code geometry} 分流：
 * <ul>
 *   <li>{@link AreaGeometry#AREA}：在目标位置瞬时扫描命中（可叠加留驻区域）；</li>
 *   <li>{@link AreaGeometry#HAZARD}：生成驻场危害物实体，周期触发命中；</li>
 *   <li>{@link AreaGeometry#REMNANT}：生成可拾取残片实体；</li>
 *   <li>{@link AreaGeometry#BEAM}：沿施法者朝向发射一束能量，持续命中；</li>
 *   <li>{@link AreaGeometry#WAVE}：向前推进的球形波前，沿途每实体仅命中一次。</li>
 * </ul>
 *
 * <p>字段按「并集」吸收：{@code shape}/{@code duration}/{@code hitTest} 为区域通用；
 * {@code ref}/{@code lifetime}/{@code count}/{@code maxActive} 仅危害物/残片用；
 * {@code range}/{@code width}/{@code radius}/{@code speed}/{@code origin} 仅光束/波前用。
 * 各字段对不适用的子形态为 {@link Optional#empty()}，由执行器按 {@code geometry} 取默认兜底。
 */
public record AreaDelivery(
        AreaGeometry geometry,
        Shape shape,
        AreaDuration duration,
        ExpressionCodec.Value hitTest,
        Optional<EntityRead> ref,
        Optional<ExpressionCodec.Value> lifetime,
        Optional<ExpressionCodec.Value> count,
        Optional<ExpressionCodec.Value> maxActive,
        Optional<ExpressionCodec.Value> range,
        Optional<ExpressionCodec.Value> width,
        Optional<ExpressionCodec.Value> radius,
        Optional<ExpressionCodec.Value> speed,
        Optional<ExpressionCodec.Value> origin,
        Optional<ExpressionCodec.Value> beamDuration
) implements Delivery {
    public static final Codec<AreaDelivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            AreaGeometry.CODEC.optionalFieldOf("geometry", AreaGeometry.AREA).forGetter(AreaDelivery::geometry),
            Shape.CODEC.optionalFieldOf("shape", new SphereShape(ExpressionCodec.of(3.0f))).forGetter(AreaDelivery::shape),
            AreaDuration.CODEC.optionalFieldOf("duration", new InstantDuration()).forGetter(AreaDelivery::duration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("hit_test", ExpressionCodec.of(1.0))
                    .forGetter(AreaDelivery::hitTest),
            EntityRead.CODEC.optionalFieldOf("ref").forGetter(AreaDelivery::ref),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("lifetime").forGetter(AreaDelivery::lifetime),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("count").forGetter(AreaDelivery::count),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("max_active").forGetter(AreaDelivery::maxActive),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("range").forGetter(AreaDelivery::range),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("width").forGetter(AreaDelivery::width),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius").forGetter(AreaDelivery::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("speed").forGetter(AreaDelivery::speed),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("origin").forGetter(AreaDelivery::origin),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("beam_duration").forGetter(AreaDelivery::beamDuration)
    ).apply(i, AreaDelivery::new));

    @Override public Delivery.Type<?> getType() { return DeliveryTypes.AREA; }

    /** 范围形态：按 {@code geometry} 取对应的 stat 标签。 */
    @Override public Set<String> statTags() {
        return switch (geometry) {
            case AREA -> Set.of("area", "aoe");
            case HAZARD -> EntityRead.statTags(ref.orElse(null), Set.of(GateTag.HAZARD));
            case REMNANT -> EntityRead.statTags(ref.orElse(null), Set.of(GateTag.REMNANT));
            case BEAM -> Set.of("beam");
            case WAVE -> Set.of("wave");
        };
    }

    /** 覆写外观（render）：仅危害物/残片持有实体引用，覆写其 render；其余几何原样返回。 */
    @Override public AreaDelivery withRender(Identifier r) {
        return switch (geometry) {
            case HAZARD, REMNANT -> new AreaDelivery(geometry, shape, duration, hitTest,
                    ref.map(x -> x.withRender(r)), lifetime, count, maxActive,
                    range, width, radius, speed, origin, beamDuration);
            default -> this;
        };
    }
}
