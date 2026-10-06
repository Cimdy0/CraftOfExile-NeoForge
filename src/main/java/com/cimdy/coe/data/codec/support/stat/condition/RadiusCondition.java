package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 几何范围节点（{@code type:"radius"}）：以锚点（或作用域中心）为圆心的 XZ 圆柱取实体。
 * <p>
 * 几何与阵营过滤<b>逐字镜像</b>
 * {@link com.cimdy.coe.data.codec.support.stat.target.TargetSelector#resolve(LivingEntity, LivingEntity, Vec3)}
 * 的 {@code RADIUS_*} 分支（{@code dx²+dz² ≤ r²}、{@code y ±64}、排除自身），避免同一套「范围内敌人」
 * 语义在两个模型里漂移。阵营过滤复用既有
 * {@link PresenceCountValue.Relation#matches(LivingEntity, Camp, LivingEntity)}
 * （{@code ally} 含「为你所有」兜底、{@code enemy} 排除你所拥有的实体）。
 *
 * <p>圆心优先级：{@code center} 显式锚点 → 当前锚点（{@code chain} 换锚后的元素）→ 作用域 center → A 的位置。
 * 排除：A 自身与当前锚点（"它附近的敌人"不含它自己）。
 *
 * <pre>
 * {"type":"radius","radius":2.5,"camp":"enemy","center":"current"}
 * </pre>
 */
public record RadiusCondition(
        ExpressionCodec.Value radius,
        PresenceCountValue.Relation camp,
        Optional<ConditionAnchor> center
) implements StatCondition {

    public static final Codec<RadiusCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExpressionCodec.VALUE_CODEC.fieldOf("radius").forGetter(RadiusCondition::radius),
            PresenceCountValue.Relation.CODEC.optionalFieldOf("camp", PresenceCountValue.Relation.ANY)
                    .forGetter(RadiusCondition::camp),
            ConditionAnchor.CODEC.optionalFieldOf("center").forGetter(RadiusCondition::center)
    ).apply(instance, RadiusCondition::new));

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        LivingEntity owner = scope.owner();
        if (owner == null || radius == null) return List.of();
        double r = (double) radius.getFloat(owner);
        if (r <= 0.0) return List.of();
        Vec3 c = scope.centerPosition(center == null ? null : center.map(a -> a.entity(scope)).orElse(null));
        Camp ownerCamp = owner.getData(AttachmentRegistry.CAMP);
        Level level = owner.level();
        AABB box = new AABB(c.x - r, c.y - 64.0, c.z - r, c.x + r, c.y + 64.0, c.z + r);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : level.getEntities(owner, box)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == owner || le == scope.anchor()) continue;
            double dx = le.getX() - c.x;
            double dz = le.getZ() - c.z;
            if (dx * dx + dz * dz > r * r) continue;
            if (camp.matches(owner, ownerCamp, le)) out.add(le);
        }
        return out;
    }

    /** 便捷构造：以当前锚点为圆心（{@code chain} 内的典型用法）。 */
    public static RadiusCondition aroundCurrent(double radius, PresenceCountValue.Relation camp) {
        return new RadiusCondition(ExpressionCodec.of(radius), camp, Optional.of(ConditionAnchor.CURRENT));
    }

    /** 便捷构造：以 A 为圆心。 */
    public static RadiusCondition aroundSelf(double radius, PresenceCountValue.Relation camp) {
        return new RadiusCondition(ExpressionCodec.of(radius), camp, Optional.of(ConditionAnchor.SELF));
    }

    /** 显式圆心锚点（可为 null → 走作用域默认优先级）。 */
    public static RadiusCondition around(@Nullable ConditionAnchor anchor, double radius,
                                         PresenceCountValue.Relation camp) {
        return new RadiusCondition(ExpressionCodec.of(radius), camp, Optional.ofNullable(anchor));
    }
}
