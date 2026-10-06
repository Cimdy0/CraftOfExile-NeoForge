package com.cimdy.coe.data.codec.skill.phase.action.charge;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 充能球类型 — 流放之路的三种充能球。
 * PoE2 对应：Endurance Charge, Frenzy Charge, Power Charge
 *
 * 每种充能球提供不同的属性加成：
 * - ENDURANCE: 物理伤害减免 + 元素抗性
 * - FRENZY: 攻击/施法速度 + 更多伤害
 * - POWER: 暴击率
 */
public enum ChargeType implements StringRepresentable {
    ENDURANCE("endurance"),
    FRENZY("frenzy"),
    POWER("power");

    public static final Codec<ChargeType> CODEC = StringRepresentable.fromEnum(ChargeType::values);

    private final String name;

    ChargeType(String name) { this.name = name; }

    @Override public String getSerializedName() { return name; }

    /** 默认最大充能球数量。 */
    public int defaultMaxCharges() {
        return 3;
    }

    /** 映射到统一的 StatusEffect 系统。 */
    public StatusType statusType() {
        return switch (this) {
            case ENDURANCE -> StatusDefs.ENDURANCE_CHARGE;
            case FRENZY -> StatusDefs.FRENZY_CHARGE;
            case POWER -> StatusDefs.POWER_CHARGE;
        };
    }
}
