package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Condition 节点类型注册表（镜像 {@code ValueExprTypes}：单点注册 + {@code type} 分派）。
 * <p>
 * 四个方向全部由注册表推导，不再各自维护平行 if 链：
 * <ol>
 *   <li>{@link #CODEC} decode：JSON {@code type} → 节点 CODEC；</li>
 *   <li>{@link #CODEC} encode：节点实例类 → CODEC + {@code type} 键反查；</li>
 *   <li>{@link #keys()}：已注册 {@code type} 键（数据包校验 / 文档生成）；</li>
 *   <li>{@link #typeOf(StatCondition)}：节点 → 注册项。</li>
 * </ol>
 *
 * <p><b>递归</b>：{@code and / or / not / chain} 的 CODEC 以 {@code Codec.recursive} 的自引用
 * {@code self} 装配（见 {@link #bootstrap}）——子字段即 {@code self} / {@code self.listOf()}，
 * 故嵌套深度不受限，与 {@code GateExpr} 的 sealed {@code And/Or/Not} 同构。
 *
 * <p><b>新增一种判断 = 一个实现类 + {@link #bootstrap} 中一行 register。</b>
 * 外部扩展亦可在首次使用前调用 {@link #register}。
 */
public final class StatConditionTypes {
    private StatConditionTypes() {}

    /** 按 JSON type 键索引（decode 分派）。 */
    private static final Map<String, StatConditionType<?>> BY_KEY = new LinkedHashMap<>();
    /** 按实现类索引（encode 反查）。 */
    private static final Map<Class<?>, StatConditionType<?>> BY_CLASS = new LinkedHashMap<>();
    /** 注册顺序列表（keys() 输出顺序稳定）。 */
    private static final List<StatConditionType<?>> ORDERED = new ArrayList<>();

    private static boolean bootstrapped = false;

    /** 注册一个节点类型；新增判断的唯一登记点。 */
    public static synchronized <T extends StatCondition> void register(StatConditionType<T> type) {
        BY_KEY.put(type.key(), type);
        BY_CLASS.putIfAbsent(type.clazz(), type);
        ORDERED.add(type);
    }

    /**
     * 内置节点注册（懒执行，打破节点 CODEC ↔ 本类的静态初始化环）。
     * 递归节点（{@code filter.of} / {@code and} / {@code or} / {@code not} / {@code chain}）的 CODEC
     * 由 {@code self} 装配 —— {@code self} 即 {@link Codec#recursive} 提供的自引用 codec，故子节点可无限嵌套。
     */
    public static synchronized void bootstrap(Codec<StatCondition> self) {
        if (bootstrapped) return;
        bootstrapped = true;

        // 生成型：命名锚点 → 0/1 元素集（self / trigger / killed / hit / event / source / current）
        register(StatConditionType.of("anchor", AnchorCondition.class, AnchorCondition.CODEC));
        // 生成型：几何范围（XZ 圆柱，阵营过滤 ally/enemy/any），镜像 TargetSelector.resolveRadius*
        register(StatConditionType.of("radius", RadiusCondition.class, RadiusCondition.CODEC));
        // 生成型：存在范围（Event.State.presenceMembers() + 阵营过滤），镜像 TargetSelector.resolvePresence*
        register(StatConditionType.of("presence", PresenceCondition.class, PresenceCondition.CODEC));
        // 生成型：归属关系（minion / totem / companion / ally / owned_by），镜像 TargetSelector.resolve*
        register(StatConditionType.of("relation", RelationCondition.class, RelationCondition.CODEC));
        // 变换型：把既有 ValueExpr 谓词复用为过滤器（能力复用，非兼容 shim）
        register(StatConditionType.of("filter", FilterCondition.class, FilterCondition.codec(self)));
        // 集合运算（递归）：交集 / 并集 / 补集（相对入参集）
        register(StatConditionType.of("and", AndCondition.class, AndCondition.codec(self)));
        register(StatConditionType.of("or", OrCondition.class, OrCondition.codec(self)));
        register(StatConditionType.of("not", NotCondition.class, NotCondition.codec(self)));
        // 多跳原语（递归）：先解析出集合，再逐元素换锚点解析子条件并合并
        register(StatConditionType.of("chain", ChainCondition.class, ChainCondition.codec(self)));
    }

    /** 节点实例 → 注册项（无注册返回 null）。 */
    public static StatConditionType<?> typeOf(StatCondition node) {
        bootstrap(StatCondition.CODEC);
        return node == null ? null : BY_CLASS.get(node.getClass());
    }

    /** 已注册的 {@code type} 键（注册顺序）。 */
    public static List<String> keys() {
        bootstrap(StatCondition.CODEC);
        List<String> out = new ArrayList<>();
        for (StatConditionType<?> t : ORDERED) out.add(t.key());
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Codec<StatCondition> cast(Codec<? extends StatCondition> codec) {
        return (Codec<StatCondition>) codec;
    }

    /**
     * 数据驱动分派 codec（递归）：JSON 对象含 {@code type} 字段，dispatch 到对应节点 CODEC。
     * 由 {@link Codec#recursive} 包裹 —— 递归节点的子字段即本 codec 自身，嵌套深度不受限。
     */
    public static final Codec<StatCondition> CODEC = Codec.lazyInitialized(() ->
            Codec.recursive("stat_condition", self -> {
                bootstrap(self);
                return new DispatchCodec();
            }));

    /** 手工分派 codec：decode 按 {@code type} 字段委托子 codec；encode 在子码结果上补 {@code type} 键。 */
    private static final class DispatchCodec implements Codec<StatCondition> {

        @Override
        public <T> DataResult<Pair<StatCondition, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapRes = ops.getMap(input);
            if (mapRes.result().isEmpty()) {
                return DataResult.error(() -> "StatCondition 期望对象");
            }
            MapLike<T> map = mapRes.result().get();
            T typeVal = map.get(ops.createString("type"));
            if (typeVal == null) {
                return DataResult.error(() -> "StatCondition 缺少 type 字段");
            }
            java.util.Optional<String> type = ops.getStringValue(typeVal).result();
            if (type.isEmpty()) {
                return DataResult.error(() -> "StatCondition type 字段非字符串");
            }
            StatConditionType<?> nodeType = BY_KEY.get(type.get());
            if (nodeType == null) {
                return DataResult.error(() -> "未知 StatCondition 类型: " + type.get());
            }
            return cast(nodeType.codec()).decode(ops, input)
                    .map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(StatCondition node, DynamicOps<T> ops, T prefix) {
            StatConditionType<?> nodeType = BY_CLASS.get(node.getClass());
            if (nodeType == null) {
                return DataResult.error(() -> "无法序列化 StatCondition: " + node.getClass());
            }
            return cast(nodeType.codec()).encode(node, ops, prefix).flatMap(encoded -> {
                Map<T, T> out = new LinkedHashMap<>();
                out.put(ops.createString("type"), ops.createString(nodeType.key()));
                ops.getMap(encoded).result().ifPresent(m ->
                        m.entries().forEach(e -> out.put(e.getFirst(), e.getSecond())));
                return DataResult.success(ops.createMap(out));
            });
        }
    }
}
