package com.cimdy.coe.data.codec.talent.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * 天赋效果类型注册表：每种效果类型对应一个 MapCodec，通过 JSON "type" 字段分发解码。
 * 新增效果类型只需在此 {@link #register(String, MapCodec)} 即可被数据驱动地解析。
 */
public final class TalentEffectType {
    private static final Map<String, MapCodec<? extends TalentEffect>> REGISTRY = new HashMap<>();

    private TalentEffectType() {}

    public static <T extends TalentEffect> void register(String id, MapCodec<T> codec) {
        REGISTRY.put(id, codec);
    }

    public static MapCodec<? extends TalentEffect> codecFor(String id) {
        return REGISTRY.getOrDefault(id, StatTalentEffect.CODEC);
    }

    /** 分发编解码：依据 "type" 字段选择对应效果；暴露为 Codec 以便 listOf() 使用。 */
    public static final Codec<TalentEffect> CODEC = Codec.STRING.dispatch(
            "type",
            TalentEffect::type,
            TalentEffectType::codecFor
    );

    static {
        register("stat", StatTalentEffect.CODEC);
    }
}
