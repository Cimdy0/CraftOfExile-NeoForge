package com.cimdy.coe.data.codec.support.math.context;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.gen.StatDataIO;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 引用型条件节点（{@code type:"read"}）：仅持有路径 {@code ref}，调用时从数据包加载的
 * {@link LoadEvent#STAT_CONDITION_MAP} 取对应 {@code List<ContextNode>}
 * （具名共享条件集，内容寻址落盘为 {@code stat_condition} 资源）并转发求值。
 *
 * <p>与 {@link com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead} /
 * {@link com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead} 同源：引用能力内聚在
 * {@link ContextNode} 类型内部（节点自身实现 read 形式），而非外层 {@code Either<X, Identifier>}
 * 包裹——满足「每个部分都实现 read」的彻头彻尾 read 构造（conditions 段原生可引用）。
 *
 * <p>求值语义：被引用条件集以<b>逻辑与</b>组合（全部通过 → 1.0，任一不通过 → 0.0），
 * 与 {@link com.cimdy.coe.data.codec.support.stat.entry.StatEntry#satisfies} 的
 * 「非零即通过」判定一致。悬空引用在首次求值显式报错（取代静默 null 被吞）。
 *
 * <p>缓存字段类：生成期经 {@link #of} 单独生产、内容命名、去重并缓存为路径键；空集合返回 null。
 * {@link #all()} 暴露去重后的条件集供 Provider 落盘（取代旧 {@code StatPartRegistry} 中心化登记）。
 */
public record ContextNodeRead(Identifier ref) implements ContextNode {

    // 内容去重缓存：规范化 JSON → 路径键 / 已分配文件名 / 内容（键即落盘文件名）
    private static final Map<String, Identifier> JSON_TO_ID = new LinkedHashMap<>();
    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    private static final Map<Identifier, List<ContextNode>> BY_ID = new LinkedHashMap<>();

    /** condition 部件生产者：空集合返回 null；否则登记 + 缓存 + 返回路径引用。 */
    public static ContextNodeRead of(List<ContextNode> conds) {
        if (conds == null || conds.isEmpty()) return null;
        Identifier id = StatDataIO.nameId(JSON_TO_ID, NAMES,
                StatDataIO.canonicalConditionJson(conds), BY_ID, conds, CraftOfExile.MODID);
        return new ContextNodeRead(id);
    }

    /** 去重后的条件集部件（键即落盘文件名）。 */
    public static Map<Identifier, List<ContextNode>> all() {
        return Map.copyOf(BY_ID);
    }

    @Override
    public double evaluate(ContextScope scope) {
        List<ContextNode> list = LoadEvent.STAT_CONDITION_MAP.get(this.ref);
        if (list == null) {
            throw new IllegalStateException("ContextNodeRead 引用了未加载的 stat_condition: " + this.ref);
        }
        for (ContextNode n : list) {
            if (n.evaluate(scope) == 0) return 0.0;
        }
        return 1.0;
    }

    /** 引用型 MapCodec：仅含 {@code ref}。 */
    public static final MapCodec<ContextNodeRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(ContextNodeRead::ref)
    ).apply(i, ContextNodeRead::new));

    /** 引用实现对应的完整 Codec（供 {@link ContextValueTypes} 注册表登记）。 */
    public static final com.mojang.serialization.Codec<ContextNodeRead> CODEC = MAP_CODEC.codec();
}
