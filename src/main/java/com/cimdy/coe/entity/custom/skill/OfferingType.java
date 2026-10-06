package com.cimdy.coe.entity.custom.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

/**
 * 奉献类型（PoE2 奉献机制）：每种奉献对应一个<b>伤害型随从「尖刺（Spike）」</b>实体，
 * 以及一份施加给施法者（及其召唤物）的增益状态。
 *
 * <ul>
 *   <li>{@link #BONE} 白骨奉献：你与召唤物获得防御增益。</li>
 *   <li>{@link #PAIN} 痛苦奉献：以尖刺为中心的 6 米光环，召唤物获得增伤。</li>
 *   <li>{@link #SOUL} 灵魂奉献：仅自身获得施法/法术/精魂增益。</li>
 * </ul>
 *
 * <p>实现为 {@link StringRepresentable}，以便经 {@link #CODEC} 在数据层（技能动作）序列化。
 */
public enum OfferingType implements StringRepresentable {
    BONE("bone"),
    PAIN("pain"),
    SOUL("soul");

    /** 序列化名（与技能 JSON 的 {@code offer_type} 字段同源）。 */
    private final String id;

    OfferingType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** 按序列化名解析（忽略大小写）。 */
    public static Optional<OfferingType> byName(String name) {
        if (name == null) return Optional.empty();
        for (OfferingType t : values()) {
            if (t.id.equalsIgnoreCase(name)) return Optional.of(t);
        }
        return Optional.empty();
    }

    /** 数据层编解码（字符串即类型名）。 */
    public static final Codec<OfferingType> CODEC = Codec.STRING.comapFlatMap(
            s -> byName(s).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown offering type: " + s)),
            OfferingType::getSerializedName);
}
