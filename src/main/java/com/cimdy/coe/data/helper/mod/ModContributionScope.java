package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

import java.util.Map;

/**
 * 物品词缀 → StatStore 的<b>消费参数包</b>：每个物品类型（装备 / 药剂 / 护符 / 宝石）提供一个实例，
 * 由 {@link ItemStatContributor} 统一收口注入 / 移除。把「来源前缀 + 持续期 + 额外条目」三参数聚合为命名载体，
 * 使跨物品类型的消费差异收敛为「提供不同 scope」，而非散落各中枢（FlaskHelper / CharmEngine / AttributeLimiterHelper）
 * 的分支逻辑——这正是咒符 / 药剂「独立却交叉」痛点的收口点。
 *
 * <ul>
 *   <li>{@code sourcePrefix}：来源前缀（{@code '#'} 之前部分）。装备 {@code equip:<slot>:<uuid>}、
 *       药剂 {@code flask:<key>:}、护符 {@code charm:<slot>}；脱下 / 到期时按此前缀整体移除。</li>
 *   <li>{@code durationTicks}：持续期（tick）。{@code <=0} 持久（装备），{@code >0} 计时自动过期（药剂 / 护符）。</li>
 *   <li>{@code extra}：额外注入的条目（已按 {@code sourcePrefix} 预键控）。装备 = 移速惩罚；其余通常空。</li>
 * </ul>
 */
public record ModContributionScope(String sourcePrefix, int durationTicks, Map<String, StatEntry> extra) {

    /** 持久来源（装备）：duration 0 + 无额外条目。 */
    public static ModContributionScope persistent(String sourcePrefix) {
        return new ModContributionScope(sourcePrefix, 0, Map.of());
    }

    /** 持久来源 + 额外条目（装备移速惩罚）。 */
    public static ModContributionScope persistent(String sourcePrefix, Map<String, StatEntry> extra) {
        return new ModContributionScope(sourcePrefix, 0, extra);
    }

    /** 计时来源（药剂 / 护符）：duration&gt;0 进 StatStore 计时域，归零自动移除。 */
    public static ModContributionScope timed(String sourcePrefix, int durationTicks) {
        return new ModContributionScope(sourcePrefix, durationTicks, Map.of());
    }

    /** 计时来源 + 额外条目。 */
    public static ModContributionScope timed(String sourcePrefix, int durationTicks, Map<String, StatEntry> extra) {
        return new ModContributionScope(sourcePrefix, durationTicks, extra);
    }
}
