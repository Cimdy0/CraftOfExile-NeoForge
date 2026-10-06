package com.cimdy.coe.data.codec.skill.phase.event;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 技能事件类型枚举。
 *
 * PoE2 新增：
 * - STAGGER: 击晕累积满触发
 * - AILMENT: 异常状态施加/触发
 * - COMBO: 连击步骤
 * - CHARGE: 充能球变化
 * - RESERVATION_CHANGE: 保留资源变化
 */
public enum SkillEventType implements StringRepresentable {
    CAST("cast"),
    HIT("caused_hit"),
    KILL("kill"),
    EXPIRE("expire"),
    CUSTOM("custom"),

    // === PoE2 新增事件 ===
    /** 击晕累积满 */
    STAGGER("stagger"),
    /** 异常状态触发（累积达到阈值） */
    AILMENT("ailment"),
    /** 连击步骤执行 */
    COMBO("combo"),
    /** 充能球变化（生成/消耗/过期） */
    CHARGE_CHANGE("charge_change"),
    /** 保留资源变化 */
    RESERVATION_CHANGE("reservation_change");

    public static final Codec<SkillEventType> CODEC = StringRepresentable.fromEnum(SkillEventType::values);

    private final String name;

    SkillEventType(String name) { this.name = name; }

    @Override
    public String getSerializedName() { return name; }
}
