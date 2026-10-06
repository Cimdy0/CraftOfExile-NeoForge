package com.cimdy.coe.data.codec.support.math.context.node;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 带归属实体的数值对 —— 取代旧 {@code SplitSink} 旁路传递的「实体-值对应」临时形态。
 * <p>
 * 旧形态下，「哪个值属于哪个实体」这一信息只能靠回调旁路（{@code SplitSink.emit}）从求值管线里
 * 挤出来：求值函数签名只返回 {@code double}，实体-值的对应关系退化成回调参数，既无法与值一起
 * 被返回、也无法被聚合或缓存。本record 把「实体 + 值」收成一等返回值，使对应关系可以正常穿过
 * 函数边界。
 * <p>
 * {@code receiver} 允许为 {@code null}：并非每个数值都天然绑定实体（如纯标量聚合的结果、
 * 与来源无关的常量）。为 {@code null} 时该pair 只贡献数值，不贡献任何实体。
 *
 * @param receiver 该数值归属的实体；无明确归属时为 {@code null}
 * @param value该归属下的数值
 */
public record Weighted(@Nullable LivingEntity receiver, double value) {}