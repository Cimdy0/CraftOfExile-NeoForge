package com.cimdy.coe.data.codec.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Map;

public interface ItemInstance {
    Codec<Map<Type<?>, ItemInstance>> MAP_CODEC = Codec.dispatchedMap(
            ItemInstances.REGISTRY.byNameCodec(),
            Type::codec);

    // 目标 Codec
    Codec<ItemInstance> CODEC = MAP_CODEC.flatXmap(
            map -> {
                if (map.size() != 1) {
                    return DataResult.error(() -> "表达式必须为单键结构");
                }
                return DataResult.success(map.values().iterator().next());
            },
            expr -> {
                ItemInstance.Type<?> type = expr.getType();
                return DataResult.success(ImmutableMap.of(type, expr));
            }
    );

    // 获取表达式的注册类型
    Type<?> getType();

    record Type<T extends ItemInstance>(Codec<T> codec) {
    }
}
