package com.cimdy.coe.ui.widget.graph.core;

/**
 * 图上的一条边，按<b>节点 id</b> 引用两端。
 *
 * <p>之所以存 id 而不是节点引用：图数据经常先建边后填节点（或反过来），
 * 存 id 才允许任意顺序装配，画布用索引表 O(1) 还原两端。缺一端的边直接跳过绘制。
 */
public class GraphEdge {

    public final String fromId, toId;

    /** 业务自定义的边类型标签（如 "major" / "minor"），画笔可据此换画法。画布本身不解读。 */
    public String tag = "";

    public boolean visible = true;

    public GraphEdge(String fromId, String toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    public GraphEdge tag(String t) { this.tag = t == null ? "" : t; return this; }

    public GraphEdge visible(boolean b) { this.visible = b; return this; }
}
