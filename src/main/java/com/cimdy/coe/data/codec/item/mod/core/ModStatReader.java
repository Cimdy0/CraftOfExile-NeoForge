package com.cimdy.coe.data.codec.item.mod.core;


import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.registry.DataComponentRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import com.cimdy.coe.data.codec.support.stat.StatIds;

/**
 * 词缀幅度读取（原 ModStatIds#readRolledPercent，由 id 常量包迁至 mod 包）。
 *
 * <p>本方法不是 id 常量，而是「读物品自身 rolled 词缀幅度」的行为，故归属 mod 包；
 * stat 路径常量统一由 StatIds 承载（ModStatIds 已并入）。
 */
public final class ModStatReader {

    private ModStatReader() {
    }

    /**
     * 读物品自身 rolled 词缀幅度（%% 归一）。
     *
     * <p>药剂/护符的 {@code #%% increased Charges} 与 {@code #%% Chance to gain a Charge on Kill} 是<b>物品自身属性</b>
     * （POE2 语义：提升的是该瓶/该符的充能上限与击杀充能几率，而非全局），故不进 STAT_STORE 聚合，
     * 而是直接从物品 {@code ITEM_STAT_ENTRIES} 里匹配 {@code ref == modStatId} 的 {@link StatRead} 取
     * {@code $value}。这样上限在被动充能/击杀（非饮用窗口）也持续生效，且每条目仅作用本物品（杜绝作用域污染）。
     *
     * <p>幅度归一：roll 值可能以裸百分比（如 30 = +30%%）或分数（0.30）存入，统一按
     * {@code > 1 ? /100 : 原值} 归一成 0.30，供调用方做 {@code 1 + inc} 乘法。
     *
     * @return 归一后的百分比（0.30 表示 +30%%）；未找到返回 0.0。
     */
    public static double readRolledPercent(ItemStack stack, Identifier modStatId) {
        Map<String, StatEntry> mods = stack.get(DataComponentRegistry.ITEM_STAT_ENTRIES);
        if (mods == null) return 0.0;
        for (StatEntry e : mods.values()) {
            if (e instanceof StatRead sr && sr.ref().equals(modStatId)) {
                ExpressionCodec.Value v = sr.vars().get("value");
                if (v != null) {
                    double raw = v.value().left().orElse(0.0);
                    return raw > 1.0 ? raw / 100.0 : raw;
                }
            }
        }
        return 0.0;
    }
}
