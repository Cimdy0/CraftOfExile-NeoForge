package com.cimdy.coe.data.codec.talent.data;

import com.cimdy.coe.data.codec.talent.tree.TalentTreeData;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 玩家全部天赋树数据的统一容器：以树 id（treeId）为键，聚合每棵树的 {@link TalentTreeData}。
 *
 * <p>服务端权威、已同步、死亡保留。新增天赋树只需在 Map 中增加对应条目，
 * 所有读取方统一通过 {@link #get} 按树 id 取用，无需为每棵树新增存储字段。
 *
 * <p><b>键类型</b>：对外一律以 {@link Identifier} 为「方法参数」，命名空间来自本模组（默认 {@code coe}），
 * 与数据包命名空间模型保持一致。但底层 Map 的键采用 {@link String}——因为 NBT / 网络等压缩格式的
 * Map 键必须是字符串，直接以 {@link Identifier} 作键无法序列化。存入时写 {@link Identifier#toString()}
 * 形式（如 {@code coe:main}），取用时由方法参数的 {@link Identifier} 转回同形字符串定位，
 * 遍历时由 {@link #treeIds()} 将键解析回 {@link Identifier}。
 */
public record TalentData(Map<String, TalentTreeData> trees) {
    public static final Codec<TalentData> CODEC = Codec.unboundedMap(Codec.STRING, TalentTreeData.CODEC)
            .xmap(TalentData::new, TalentData::trees);

    public static final TalentData EMPTY = new TalentData(Map.of());

    /** 按树 id（Identifier）取用该树的玩家数据（无记录时返回 {@link TalentTreeData#EMPTY}）。 */
    public TalentTreeData get(Identifier treeId) {
        return trees.getOrDefault(treeId.toString(), TalentTreeData.EMPTY);
    }

    /** 返回替换 / 新增某棵树数据后的新 {@link TalentData}（不可变更新）。 */
    public TalentData with(Identifier treeId, TalentTreeData data) {
        Map<String, TalentTreeData> copy = new HashMap<>(trees);
        copy.put(treeId.toString(), data);
        return new TalentData(copy);
    }

    /** 已存有数据的全部树 id（把底层字符串键解析回 {@link Identifier}），供遍历施加效果等使用。 */
    public Set<Identifier> treeIds() {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (String key : trees.keySet()) {
            ids.add(Identifier.parse(key));
        }
        return ids;
    }
}
