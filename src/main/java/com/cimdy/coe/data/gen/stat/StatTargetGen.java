package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.data.codec.support.stat.target.StatTargets;
import com.cimdy.coe.data.codec.support.stat.target.StatTargetsInline;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import net.minecraft.resources.Identifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stat targets 子条目内容注册表（CODEC 工厂：JSON 经 StatTargets 构造器直接构造真实对象，
 * 三段选择器全部引用 TargetSelector 常量，Provider 编码落盘）。
 */
public final class StatTargetGen {
    private StatTargetGen() {}

    public static Map<Identifier, StatTargets> contents() {
        Map<Identifier, StatTargets> m = new LinkedHashMap<>();
        // TGT_ALL_ON_TARGET / TGT_SELF_BY_TARGET / TGT_TARGET_BY_TARGET / TGT_TARGET_ONLY 已于缝#37 移除；
        // inline_minion / inline_ally / inline_totem / inline_presence_* / companion_inline 于 Phase 2b 移除：
        // Target 收敛为 SELF / TRIGGER_TARGET 两态后，随从 / 图腾 / 同伴 / 盟友 / 存在范围
        // 一律改由 Condition（relation / presence / radius）获取实体，这些内联预设不再成立。
        m.put(StatTargetIds.TGT_COND_ON_TARGET, new StatTargetsInline(null, TargetSelector.EVENT_TARGET, null));
        m.put(StatTargetIds.TGT_INLINE_SELF, new StatTargetsInline(TargetSelector.SELF, null, null));
        m.put(StatTargetIds.TGT_INLINE_SELF_TRIGGER_TARGET, new StatTargetsInline(TargetSelector.SELF, TargetSelector.EVENT_TARGET, null));
        m.put(StatTargetIds.TGT_INLINE_TRIGGER_TARGET, new StatTargetsInline(TargetSelector.EVENT_TARGET, TargetSelector.EVENT_TARGET, null));
        m.put(StatTargetIds.TGT_SELF_ONLY, StatTargets.EMPTY);
        return m;
    }

    /**
     * 具名 targets 预设的<b>对象解析</b>：把 {@code StatTargetIds.*} 具名常量解成真实的
     * {@link StatTargets} 对象（接线层全程按对象流转，身份由具名常量保证，无内容哈希）。
     * 未注册即抛错（fail-fast）；交付 {@link com.cimdy.coe.data.codec.support.stat.entry.Stat}
     * 时由 {@link com.cimdy.coe.data.codec.support.stat.entry.StatEntry#internTargets} 登记为引用。
     */
    public static StatTargets targets(Identifier id) {
        StatTargets t = contents().get(id);
        if (t == null) {
            throw new IllegalStateException("未注册的 stat_targets 预设 id: " + id);
        }
        return t;
    }

}