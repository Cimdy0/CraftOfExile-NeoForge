package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.helper.engine.ground.VolatilityDetonationManager;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 到最近敌对实体的距离（方块单位）。
 * <p>
 * 作用域：当前被求值实体（LivingEntity）。以该实体为中心、半径内扫描存活且非同盟
 * （{@code !e.isAlliedTo(living)}）的 LivingEntity，取最近者返回其直线距离；无敌人时返回
 * {@link #NO_ENEMY_SENTINEL}（极大哨兵值，表示「最近敌人在无穷远」），使距离门控条件在无敌人时
 * 语义正确：{@code within Xm} 为假、{@code further_than Xm} / 「无敌人在 Xm 内」为真。
 * 敌对判定复用模组既有约定（{@code VolatilityDetonationManager} 同款 {@code !isAlliedTo}）。
 * 枚举半径默认 64 方块，覆盖「within Xm」类阈值需求（精确阈值比较由条件侧 {@code CompareValue} 注入 {@code $value} 完成，本节点只输出原始距离）。
 * <p>
 * JSON 形式 {@code {"type":"dist_nearest_enemy"}} 或 {@code {"type":"dist_nearest_enemy","radius":64.0}}。
 */
public record DistanceToNearestEnemyValue(double radius) implements EntityNode {

    /** 默认扫描半径（方块）：覆盖常见 within 1~3m 等阈值，避免全维度扫描。 */
    public static final double DEFAULT_RADIUS = 64.0;

    /**
     * 无敌人时的哨兵距离（方块）：远大于任何合理 {@code stat_range_m} 阈值（< 100），
     * 令「最近敌人距离」在空场下表现为「无穷远」——{@code within Xm} 为假、{@code further_than Xm} 为真。
     */
    public static final double NO_ENEMY_SENTINEL = 1.0e9;

    public DistanceToNearestEnemyValue() {
        this(DEFAULT_RADIUS);
    }

    public static final Codec<DistanceToNearestEnemyValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("radius", DEFAULT_RADIUS).forGetter(DistanceToNearestEnemyValue::radius)
            ).apply(instance, DistanceToNearestEnemyValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        if (living == null) return 0;
        Level level = living.level();
        if (level == null) return 0;
        Vec3 center = living.position();
        Optional<LivingEntity> nearest = EntityQuery.box(living.getBoundingBox().inflate(radius))
                .exclude(living)
                .aliveOnly()
                .filter(e -> !e.isAlliedTo(living))
                .nearestTo(center)
                .first(level);
        return nearest.map(e -> e.position().distanceTo(center)).orElse(NO_ENEMY_SENTINEL);
    }

    /** 名词短语：值键 {@code stat.value.src.dist_nearest_enemy}（P3/A）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("src", "dist_nearest_enemy"));
    }
}
