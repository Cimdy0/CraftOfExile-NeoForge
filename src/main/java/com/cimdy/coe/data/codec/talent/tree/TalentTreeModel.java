package com.cimdy.coe.data.codec.talent.tree;

import com.cimdy.coe.data.codec.talent.tree.TalentEdge;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import java.util.*;

/**
 * 天赋树静态模型：节点 + 起始节点（可有多个）+ 总点数。
 * 一份模型 = 一棵树（目前仅主树，可含多个天赋起点）。
 *
 * <p>所有节点均携带<b>显式世界坐标</b>（由数据生成器从 POE2 数据库翻译得到），
 * 因此不再需要任何力导向 / 几何迭代布局算法；渲染与分配直接读取 {@link TalentNodeDef#x()} / {@link TalentNodeDef#y()}。
 *
 * <p>连接采用<b>节点中心</b>模型：每个节点通过 {@link TalentNodeDef#links()} 自声明邻居，
 * 因此定义天赋树时只需「声明节点 + 给节点写上 links」，无需任何 from/to 顺序。
 * 统一无向图由 {@link #resolvedEdges()} 基于 links 推导，供分配 / 渲染共用。
 */
public record TalentTreeModel(
        List<TalentNodeDef> nodes,
        List<String> startNodeIds,
        int totalPoints,
        int version,
        boolean resetOnUpdate
) {
    /** 空模型：树数据未从数据包加载时的安全占位（不再回退到代码生成的默认模型）。 */
    public static final TalentTreeModel EMPTY = new TalentTreeModel(List.of(), List.of(), 0, 0, false);

    public TalentNodeDef node(String id) {
        for (TalentNodeDef n : nodes) if (n.id().equals(id)) return n;
        return null;
    }

    /**
     * 统一无向连接图：基于每个节点的 {@link TalentNodeDef#links()} 推导，去重为无向边
     * （同一条边只出现一次）。供分配器、渲染统一使用。
     */
    public List<TalentEdge> resolvedEdges() {
        // 使用 LinkedHashMap/LinkedHashSet 保持节点/links 的声明顺序，使去重结果确定一致。
        Map<String, Set<String>> adj = new java.util.LinkedHashMap<>();
        for (TalentNodeDef n : nodes) {
            for (String l : n.links()) {
                adj.computeIfAbsent(n.id(), k -> new LinkedHashSet<>()).add(l);
                adj.computeIfAbsent(l, k -> new LinkedHashSet<>()).add(n.id());
            }
        }
        List<TalentEdge> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Set<String>> en : adj.entrySet()) {
            for (String to : en.getValue()) {
                String a = en.getKey(), b = to;
                // 无序去重：保证 (a,b) 与 (b,a) 视为同一条边
                String key = a.compareTo(b) <= 0 ? a + "\u0000" + b : b + "\u0000" + a;
                if (seen.add(key)) out.add(new TalentEdge(a, b));
            }
        }
        return out;
    }
}
