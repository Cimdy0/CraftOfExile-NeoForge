package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.ui.screen.progression.ResearchAllocator;
import com.cimdy.coe.ui.screen.talent.TalentAllocator;

/**
 * 图画笔构造门面：外部一律从本类拿画笔，具体实现类包私有不可见。
 *
 * <p>与 {@code ui.widget.render.Renderers} 同形 —— lib 公开面只暴露能力接口与原语公共用法，
 * 不暴露具体实现。
 */
public final class NodePainters {

    private NodePainters() {}

    /** 默认节点画笔（纯色圆点 / 方块 + 状态描边），不带任何业务假设。 */
    public static <T> NodePainter<T> dot() {
        return new DotNodePainter<>();
    }

    /** 默认连线画笔：两点之间一条直线。 */
    public static <T> EdgePainterI<T> line() {
        return new LineEdgePainter<>();
    }

    /** 研究树节点画笔（业务专属，按三态着色 + 断链标记）。 */
    public static ProgressionNodePainterI progression(ResearchAllocator allocator) {
        return new ProgressionNodePainter(allocator);
    }

    /** 天赋树节点画笔（业务专属，复刻旧 TalentNodeRenderer 视觉语义）。 */
    public static TalentNodePainterI talent(TalentAllocator allocator) {
        return new TalentNodePainter(allocator);
    }
}
