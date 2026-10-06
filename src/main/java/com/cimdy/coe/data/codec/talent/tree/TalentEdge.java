package com.cimdy.coe.data.codec.talent.tree;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

/**
 * 统一无向边（内存表示，不序列化）：由节点 {@link TalentNodeDef#links()} 推导，
 * 供自动布局、分配器与渲染统一使用。两端节点 id 无序（无方向）。
 */
public record TalentEdge(String fromId, String toId) {
}
