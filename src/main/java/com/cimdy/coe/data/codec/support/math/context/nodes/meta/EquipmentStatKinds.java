package com.cimdy.coe.data.codec.support.math.context.nodes.meta;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;

/**
 * 装备读值节点输出种类常量（对应 {@link EquipmentValue} 内部 {@code EQUIPMENT_OUTPUTS} 表的收口 key）。
 * 与模型私有表 key 保持一致，供生成层引用，避免手写字符串漂移。
 */
public final class EquipmentStatKinds {
    private EquipmentStatKinds() {}

    public static final String COUNT = "count";
    public static final String LEVEL = "level";
    public static final String MODS = "mods";
    public static final String ITEM_LEVEL = "item_level";
    public static final String REQUIRE_LEVEL = "require_level";
    public static final String ARMOR = "armor";
    public static final String EVASION = "evasion";
    public static final String ENERGY = "energy";
    public static final String FIRE_RESISTANCE = "fire_resistance";
    public static final String COLD_RESISTANCE = "cold_resistance";
    public static final String LIGHTNING_RESISTANCE = "lightning_resistance";
    public static final String LIGHT_RESISTANCE = "light_resistance";
    public static final String CHAOS_RESISTANCE = "chaos_resistance";
}
