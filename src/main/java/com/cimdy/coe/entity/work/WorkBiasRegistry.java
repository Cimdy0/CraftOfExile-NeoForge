package com.cimdy.coe.entity.work;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体类型 → 基础工作偏置 的注册表。
 * <p>
 * 为「不同实体类型拥有不同的工作适应度 / 工作速度」提供单一可扩展入口：
 * 任意 {@code EntityType} 都可登记专属 {@link WorkBias}；未登记者统一回退 {@link WorkBias#DEFAULT}。
 * 例：村民基础速度略低，但 {@link WorkProfile} 会再按其职业追加对应工种适应度。
 */
public final class WorkBiasRegistry {
    private static final Map<EntityType<?>, WorkBias> BIASES = new HashMap<>();

    static {
        // 村民：基础移动/作业速度略低，其工种适应度主要由职业挂钩（见 WorkProfile#compute）补强。
        // MC 26.2 起 EntityType 不再提供静态 VILLAGER 字段，改由注册表按标识符取用。
        EntityType<?> villager = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "villager"));
        if (villager != null) {
            register(villager, new WorkBias(0.95, Map.of()));
        }
        // 其它实体类型可按需在此或运行时通过 register(...) 追加差异化偏置。
    }

    private WorkBiasRegistry() {
    }

    public static void register(EntityType<?> type, WorkBias bias) {
        BIASES.put(type, bias);
    }

    public static WorkBias forType(EntityType<?> type) {
        return BIASES.getOrDefault(type, WorkBias.DEFAULT);
    }
}
