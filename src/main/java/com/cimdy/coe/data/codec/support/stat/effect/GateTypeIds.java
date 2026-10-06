package com.cimdy.coe.data.codec.support.stat.effect;

/**
 * 门控类型 id 常量。
 *
 * <p>kind 与 StatEngine.gateOf / magnitudeOf 的查找串、以及 StatEffectTypes 的 codec 注册键
 * 三者必须严格相等（裸串精确匹配），故统一收口到本类常量，避免数据包键漂移 / 拼写失配。
 *
 * <p>注意：此处门控 id 与 StatEffectIds.EFF_* 的 path（add_value_attribute_coe_ 前缀）是两个独立命名空间——
 * 前者是 gate 机制的 kind 身份串，后者是 stat 接线 id，故意不同，不要混用。
 */
public final class GateTypeIds {
    public static final String ENERGY_SHIELD_CAPPED_BY_ARMOR = "energy_shield_capped_by_armor";
    public static final String ENERGY_SHIELD_REGEN_DISABLED = "energy_shield_regen_disabled";
    /**
     * 符文护盾（Runic Ward）不充能门控，消费端 {@code ResourceRegistry#isRunicWardRechargeDisabled}
     * （由 {@code LivingTickDriver} 每 tick 充能分支真读）。
     *
     * <p>本 kind 登记于 {@code MarkerStatEffect.KIND_SLOTS}（Slot.GATE），
     * 故可被数据包驱动；若新增门控 kind 遗漏登记，codec 注册循环不会生成对应条目，
     * 门控将永不可能被数据打开。</p>
     */
    public static final String RUNIC_WARD_RECHARGE_DISABLED = "runic_ward_recharge_disabled";

    private GateTypeIds() {
    }
}
