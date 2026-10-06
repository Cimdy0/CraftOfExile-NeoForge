package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;

import java.util.Set;

/**
 * 天赋树节点画笔的可配置能力面：在 {@link NodePainter} 之上补上搜索高亮方法。
 *
 * <p>具体实现类包私有不可见，外部一律经 {@link NodePainters#talent(TalentAllocator)} 拿到本接口实例。
 */
public interface TalentNodePainterI extends NodePainter<TalentNodeDef> {

    /** 批量设置搜索命中的高亮节点 id（画黄色聚焦圈）。 */
    void setHighlight(Set<String> ids);
}
