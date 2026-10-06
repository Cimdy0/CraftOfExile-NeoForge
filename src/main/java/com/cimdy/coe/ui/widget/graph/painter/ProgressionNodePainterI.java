package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/**
 * 研究树节点画笔的可配置能力面：在 {@link NodePainter} 之上补上业务侧需要的断链标记 / 查询方法。
 *
 * <p>具体实现类包私有不可见，外部一律经 {@link NodePainters#progression(ResearchAllocator)} 拿到本接口实例。
 */
public interface ProgressionNodePainterI extends NodePainter<ResearchNodeDef> {

    /** 批量设置「前置缺失」的损坏节点 id。 */
    void setBroken(Set<String> ids);

    /** 标记单个「前置缺失」的节点并附上缺失详情（tooltip 展示）。 */
    void markBroken(String id, List<Component> details);

    /** 取某节点的缺失详情，无则返回 null。 */
    List<Component> brokenDetails(String id);
}
