package com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
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

/**
 * 半径内敌对实体<b>计数</b>（方块单位半径）。
 * <p>
 * 与 {@link DistanceToNearestEnemyValue}（只回答「最近敌人有多远」）互补：本节点回答「身边有几个敌人」，
 * 供 PoE2「每个近距离敌人 +X」（{@code per Enemy in Close Range}）线性缩放族与「被包围」阈值判定使用。
 * <p>
 * 作用域：当前被求值实体（LivingEntity）。以该实体位置为球心、{@code radius} 为半径做球形扫描
 * （{@link EntityQuery#sphere}，包围盒粗筛 + 球面精筛），排除自身、仅存活、且非同盟
 * （{@code !e.isAlliedTo(living)}）—— 敌对判定复用模组既有约定（同
 * {@code DistanceToNearestEnemyValue} / {@code VolatilityDetonationManager}）。无敌人时返回 0，
 * 使「每敌 +X」在空场下自然归零（{@code $value * nearby_enemy_count:2 == 0}）。
 * <p>
 * 半径语义按 PoE2 官方关键词：{@code Close Range} = 2 米、{@code Surrounded} = 3 米内 ≥ 5 敌，
 * 故常见取值为 2.0 / 3.0；缺省 {@link #DEFAULT_RADIUS}。
 * <p>
 * JSON 形式 {@code {"type":"nearby_enemy_count"}} 或 {@code {"type":"nearby_enemy_count","radius":2.0}}；
 * 中缀简写 {@code nearby_enemy_count:<radius>}（如 {@code nearby_enemy_count:2}），可直接写在
 * {@code StatBuilder.amount("$value * nearby_enemy_count:2")} 这类表达式中。
 */
public record NearbyEnemyCountValue(double radius) implements EntityNode {

    /** 缺省扫描半径（方块）：取 PoE2 {@code Close Range} 口径 2 米。 */
    public static final double DEFAULT_RADIUS = 2.0;

    public NearbyEnemyCountValue() {
        this(DEFAULT_RADIUS);
    }

    public static final Codec<NearbyEnemyCountValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("radius", DEFAULT_RADIUS).forGetter(NearbyEnemyCountValue::radius)
            ).apply(instance, NearbyEnemyCountValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return count(living, radius);
    }

    /** 名词短语：复用已有值键 {@code stat.value.src.nearby_enemy_count}（P3/A）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(StatValueKey.of("src", "nearby_enemy_count"));
    }

    /**
     * 半径内敌对存活实体数（共享实现：本节点与 {@code LivingStateValue.surrounded} 同源，
     * 保证「每敌缩放」与「被包围门控」用同一套敌对判定，不出现两套口径）。
     */
    public static int count(LivingEntity living, double radius) {
        if (living == null) return 0;
        Level level = living.level();
        if (level == null || level.isClientSide()) return 0;
        Vec3 center = living.position();
        return EntityQuery.sphere(center, radius)
                .exclude(living)
                .aliveOnly()
                .filter(e -> !e.isAlliedTo(living))
                .collect(level)
                .size();
    }
}
