package com.cimdy.coe.data.codec.support.status.registry.ground;

import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;

/**
 * 地面作用域判定（阵营门控）：决定地面效果是否施加于某个目标实体。
 *
 * <p>与 {@link Camp} 同构：地面携带自身阵营（创建时从所有者取），
 * 目标实体也携带阵营（{@code AttachmentRegistry.CAMP}）。门控即比对二者阵营关系。
 */
public enum GroundTargeting implements StringRepresentable {
    /** 影响范围内所有实体（忽略阵营）。 */
    ALL("all"),
    /** 仅影响敌方（目标阵营 ≠ 地面阵营 且非 NONE）。 */
    ENEMIES("enemies"),
    /** 仅影响友方（目标阵营 = 地面阵营 且非 NONE）。 */
    ALLIES("allies");

    public static final Codec<GroundTargeting> CODEC = StringRepresentable.fromEnum(GroundTargeting::values);

    private static final Map<String, GroundTargeting> BY_ID = new HashMap<>();
    static {
        for (GroundTargeting t : values()) BY_ID.put(t.id, t);
    }

    public static GroundTargeting byId(String s) {
        return BY_ID.getOrDefault(s, ENEMIES);
    }

    private final String id;

    GroundTargeting(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** 给定地面阵营与目标阵营，判断地面效果是否施加于该目标。 */
    public boolean affects(Camp groundCamp, Camp targetCamp) {
        return switch (this) {
            case ALL -> true;
            case ENEMIES -> targetCamp != Camp.NONE && targetCamp != groundCamp;
            case ALLIES -> targetCamp == groundCamp && targetCamp != Camp.NONE;
        };
    }
}
