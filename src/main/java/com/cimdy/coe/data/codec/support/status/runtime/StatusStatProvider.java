package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 状态→Stat 贡献钩子。实现「XX 时提高某属性 / 造成某效果」经 Stat 系统落地（而非事件回调）。
 *
 * <p>返回该状态激活期间应注入实体 {@code STAT_STORE} 的 stat 条目（{@link StatEntry}
 * = 数值 + 条件 + 选择器 + 效果）列表。条件「XX 时」由条目自身的 {@code tags} / {@code conditions}
 * 表达（行为发生时经 {@code StatScopes.enter} 带对应 StatContext tag），读属性时即时生效；
 * 条目存在与否本身即「buff 是否激活」的门控。
 *
 * @see StatusStatBridge
 */
@FunctionalInterface
public interface StatusStatProvider {
    List<StatEntry> provide(LivingEntity living, StatusInstance inst);
}
