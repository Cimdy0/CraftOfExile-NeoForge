package com.cimdy.coe.data.gen.stat;

import net.minecraft.resources.Identifier;

/**
 * Stat targets 子条目 Ids（路径缓存）
 * 内容寻址子条目路径缓存：与生成产物 <code>stat/targets/&lt;name&gt;.json</code> 1:1，
 * 运行期由 LoadEvent 按路径加载缓存，供对应 read 解析。
 */
public final class StatTargetIds {
    private StatTargetIds() {}
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("coe", path);
    }

    public static final Identifier TGT_COND_ON_TARGET = id("cond_on_target");
    // companion_inline / inline_minion / inline_ally / inline_totem / inline_presence_allies /
    // inline_presence_enemies 于 Phase 2b 移除（归属与范围由 Condition 获取，不再走 Target 预设）。
    public static final Identifier TGT_INLINE_SELF = id("inline_self");
    public static final Identifier TGT_INLINE_SELF_TRIGGER_TARGET = id("inline_self_trigger_target");
    public static final Identifier TGT_INLINE_TRIGGER_TARGET = id("inline_trigger_target");
    public static final Identifier TGT_SELF_ONLY = id("self_only");
}