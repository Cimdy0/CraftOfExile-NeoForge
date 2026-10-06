package com.cimdy.coe.data.codec.support.stat.duration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * 时长段类型注册表：每种 {@link StatDuration} 对应一个 MapCodec，按 JSON {@code type} 字段分发。
 * 与 {@link com.cimdy.coe.data.codec.support.stat.target.StatTargetsTypes} 同一注册模式。
 *
 * <p><b>条目层的 {@code duration} 字段走扁平字符串（{@link StatDuration#CODEC}，形如
 * {@code "duration:120"}），不走本派发表</b>；本表供时长作为<b>独立落盘单元</b>
 * （{@code stat/duration/*.json}，被 {@code read:} 引用）时使用。
 *
 * <p>成员：
 * <ul>
 *   <li>{@code fixed} —— {@link StatDurationFixed}：内联三种时间语义；</li>
 *   <li>{@code read} —— {@link StatDurationRead}：引用另一份时长定义。</li>
 * </ul>
 *
 * <p><b>「read」形态的接线状态</b>：解码侧已完整接线——落盘单元由
 * {@link com.cimdy.coe.data.gen.provider.StatDurationProvider} 写到
 * {@code stat/duration/<id>.json}，运行期由 {@code LoadEvent} 的
 * {@code coe/stat/duration} 监听器扫进 {@code LoadEvent#STAT_DURATION_MAP}，
 * 条目的 {@code "duration": "read:coe:<id>"} 经
 * {@link StatDurationRead#resolved()} 查表解析，key 与生成期
 * {@code StatDurationIds.id("<path>")} 字面一致。
 *
 * <p>但<b>当前无产出方</b>：{@link com.cimdy.coe.data.gen.stat.duration.StatDurationGen#contents()}
 * 登记的具名 duration 全是 {@code fixed} 形态（内联 kind + ticks），没有任何条目以
 * {@code read:} 形态引用外置段——工程内既有的时长产出逻辑（{@code StatBuilder#duration(int)}、
 * {@code SkillBanner.buildAuraEntry} 等）全部走内联形态。对称的构造工厂
 * {@link StatDuration#read} 同样<b>零调用方</b>：{@link StatDuration#parse} 遇 {@code read:}
 * 前缀时直接 {@code new StatDurationRead(id)}，不经过它。属<b>预留</b>：机制已通、无消费者，
 * 不为「点亮工厂」去改造既有产出逻辑（那会改变运行时行为）。
 */
public final class StatDurationTypes {
    private static final Map<String, MapCodec<? extends StatDuration>> REGISTRY = new HashMap<>();

    private StatDurationTypes() {
    }

    public static <T extends StatDuration> void register(String id, MapCodec<T> codec) {
        REGISTRY.put(id, codec);
    }

    /** 未知时长类型：decode/encode 恒失败，使拼写错误在加载期显式暴露。 */
    private static final MapCodec<StatDuration> UNKNOWN_TYPE = new Codec<StatDuration>() {
        @Override
        public <T> DataResult<T> encode(StatDuration input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "cannot encode unknown stat duration type");
        }

        @Override
        public <T> DataResult<com.mojang.datafixers.util.Pair<StatDuration, T>> decode(
                com.mojang.serialization.DynamicOps<T> ops, T input) {
            return DataResult.error(() -> "unknown stat duration type");
        }
    }.fieldOf("type");

    public static MapCodec<? extends StatDuration> codecFor(String id) {
        MapCodec<? extends StatDuration> codec = REGISTRY.get(id);
        if (codec != null) return codec;
        // 与 StatTargetsTypes 同：注册期静态初始化顺序可能取到 null，运行期直接回退保证可序列化。
        if ("fixed".equals(id)) return StatDurationFixed.MAP_CODEC;
        if ("read".equals(id)) return StatDurationRead.MAP_CODEC;
        return UNKNOWN_TYPE;
    }

    /**
     * 该 type 是否已注册（{@code register(...)} 过）。
     *
     * <p>供生成期<b>落盘前硬校验</b>用：未注册的 type 会被 {@link #codecFor} 判成
     * {@code UNKNOWN_TYPE}，其 encode/decode 恒失败，只在读取时才炸。外置落盘单元
     * （{@code stat/duration/*.json}）应在 datagen 期就拦下来。
     *
     * <p>与 {@link com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes#isRegistered} 同口径。
     */
    public static boolean isRegistered(String id) {
        return REGISTRY.containsKey(id);
    }

    /** 分发 MapCodec（独立对象形式）。 */
    public static final MapCodec<StatDuration> MAP_CODEC = Codec.STRING.dispatchMap(
            "type",
            StatDuration::type,
            StatDurationTypes::codecFor
    );

    public static final Codec<StatDuration> CODEC = MAP_CODEC.codec();

    static {
        register("fixed", StatDurationFixed.MAP_CODEC);
        register("read", StatDurationRead.MAP_CODEC);
    }
}
