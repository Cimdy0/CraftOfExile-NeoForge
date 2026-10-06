package com.cimdy.coe.data.codec.entity;

import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 全局实体修饰器（服务端静态配置，由 datagen 在 {@code data/coe/coe/living/global/*.json} 生成）。
 * 完全归一为纯 Stat 模型：
 * <ul>
 *   <li>{@code stats}——本修饰器贡献的全部 Stat 条目（属性加成 + 转伤等），统一存入实体 {@code STAT_STORE}，
 *       由 {@code StatEngine.fold} 即时聚合（与天赋 / 状态 / 技能同源）；</li>
 *   <li>{@code conditions}——本修饰器生效的判定条件（维度 / 群系 / 实体类型等），
 *       以 {@link ValueExpr} 表达，运行时由 Stat 系统按事件作用域实时门控
 *       （{@code scope} 经 GateExpr#matches / StatEngine#conditionsHold 对任一条件节点求值 0 即判不生效），无需独立的实体谓词通道。</li>
 * </ul>
 * 不设 {@code predicate} / {@code convert} 字段：
 * 条件判定走 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达（同走 Stat 通道）。
 */
public record LivingModifier(
        List<StatEntry> stats,
        List<ValueExpr> conditions
) {
    public static final Codec<LivingModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StatEntry.CODEC.listOf().optionalFieldOf("stats", List.of())
                    .forGetter(LivingModifier::stats),
            ValueExpr.CODEC.listOf().optionalFieldOf("conditions", List.of())
                    .forGetter(LivingModifier::conditions)
    ).apply(instance, LivingModifier::from));

    public static LivingModifier from(List<StatEntry> stats, List<ValueExpr> conditions) {
        return new LivingModifier(stats, conditions);
    }

    public static final LivingModifier EMPTY = new LivingModifier(List.of(), List.of());
}
