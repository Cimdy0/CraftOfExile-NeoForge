package com.cimdy.coe.data.codec.item.addition;

import com.cimdy.coe.data.codec.item.addition.impl.GlobalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.LocalAddition;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.addition.impl.VeiledAddition;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link AdditionType} 实现注册表：每种语义对应一个 MapCodec，按 JSON {@code "type"} 字段分发解码。
 *
 * <p>扩展方式（= 用户需求「新增种类 = 新增一个实现类，零核心改动」）：
 * 新增一个 {@link AdditionType} 实现类 + 在下面的 static 块 {@link #register} 一行；
 * 消费者（{@code AttributeApplicator}）与 {@link ContributionSink} 均无需改动。
 */
public final class AdditionTypes {

    private static final Map<String, MapCodec<? extends AdditionType>> REGISTRY = new HashMap<>();

    private AdditionTypes() {
    }

    public static void register(String id, MapCodec<? extends AdditionType> codec) {
        REGISTRY.put(id, codec);
    }

    /** 未知类型：decode/encode 恒失败，使拼写错误在加载期显式暴露（而非静默回退到某一种）。 */
    private static final MapCodec<AdditionType> UNKNOWN_TYPE = new MapCodec<AdditionType>() {
        @Override
        public <T> DataResult<AdditionType> decode(com.mojang.serialization.DynamicOps<T> ops,
                                                   com.mojang.serialization.MapLike<T> input) {
            return DataResult.error(() -> "unknown addition type");
        }

        @Override
        public <T> java.util.stream.Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
            return java.util.stream.Stream.empty();
        }

        @Override
        public <T> com.mojang.serialization.RecordBuilder<T> encode(AdditionType input,
                                                                    com.mojang.serialization.DynamicOps<T> ops,
                                                                    com.mojang.serialization.RecordBuilder<T> prefix) {
            return prefix;
        }
    };

    public static MapCodec<? extends AdditionType> codecFor(String id) {
        return REGISTRY.getOrDefault(id, UNKNOWN_TYPE);
    }

    static {
        register("global", GlobalAddition.MAP_CODEC);
        register("local", LocalAddition.MAP_CODEC);
        register("veiled", VeiledAddition.MAP_CODEC);
        register("multi", MultiAddition.MAP_CODEC);
        register("read", AdditionRead.MAP_CODEC);
    }
}
