package com.cimdy.coe.data.codec.support.stat.target;

import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * 三段选择器类型注册表：每种 {@link StatTargets} 对应一个 MapCodec，通过 JSON "type" 字段分发解码。
 * 新增选择器形态只需在此 {@link #register(String, MapCodec)} 即可被数据驱动地解析。
 * 与 {@link StatEffectTypes} / {@code TalentEffectType} 同一注册模式。
 *
 * <p>成员：
 * <ul>
 *   <li>{@code inline} —— {@link StatTargetsInline}：三段选择器平铺内联；</li>
 * </ul>
 */
public final class StatTargetsTypes {
    private static final Map<String, MapCodec<? extends StatTargets>> REGISTRY = new HashMap<>();

    private StatTargetsTypes() {
    }

    public static <T extends StatTargets> void register(String id, MapCodec<T> codec) {
        REGISTRY.put(id, codec);
    }

    /** 未知选择器类型：decode/encode 恒失败，使拼写错误在加载期显式暴露。 */
    private static final MapCodec<StatTargets> UNKNOWN_TYPE = new Codec<StatTargets>() {
        @Override
        public <T> DataResult<T> encode(StatTargets input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "cannot encode unknown stat targets type");
        }

        @Override
        public <T> DataResult<com.mojang.datafixers.util.Pair<StatTargets, T>> decode(
                com.mojang.serialization.DynamicOps<T> ops, T input) {
            return DataResult.error(() -> "unknown stat targets type");
        }
    }.fieldOf("type");

    public static MapCodec<? extends StatTargets> codecFor(String id) {
        MapCodec<? extends StatTargets> codec = REGISTRY.get(id);
        if (codec != null) {
            return codec;
        }
        // HashMap.getOrDefault 在「键存在但值为 null」时返回 null（不会回落 default），
        // 而 KeyDispatchCodec.getCodec 会把 DataResult.success(codecFor(k)) 透传，
        // 导致 elementEncoder 为 null 抛 NPE。注册期若因静态初始化顺序取到 null，
        // 运行期 StatTargetsInline.MAP_CODEC 必然已就绪，直接回退保证 inline 形态可序列化。
        if ("inline".equals(id)) {
            return StatTargetsInline.MAP_CODEC;
        }
        return UNKNOWN_TYPE;
    }

    /** 分发 MapCodec：依据 "type" 字段选择对应选择器，字段与条目其余部分平铺同层。 */
    public static final MapCodec<StatTargets> MAP_CODEC = Codec.STRING.dispatchMap(
            "type",
            StatTargets::type,
            StatTargetsTypes::codecFor
    );

    /** 分发编解码（独立对象形式），供不依附条目的场景使用。 */
    public static final Codec<StatTargets> CODEC = MAP_CODEC.codec();

    static {
        register("inline", StatTargetsInline.MAP_CODEC);
    }
}
