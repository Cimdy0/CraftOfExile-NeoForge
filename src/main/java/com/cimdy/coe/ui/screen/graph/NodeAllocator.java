package com.cimdy.coe.ui.screen.graph;

import java.util.Set;

/**
 * 节点图「分配器」共享契约（A1+A2 架构收敛的一部分）。
 *
 * <p>天赋分配器（{@code TalentAllocator}）与研究分配器（{@code ResearchAllocator}）虽然
 * 内部语义截然不同——天赋有武器组绑定、珠宝辐射、共享总预算；研究不可退点、是只读进度投影——
 * 但它们<b>对外暴露的七条方法形状完全一致</b>：分配态查询、可点判定、乐观预览、权威同步、计数与剩余。
 *
 * <p>本接口只抽象这七条真正共有的形状，<b>不</b>触碰任何一方的专属语义（{@code setActiveSet} /
 * {@code socketOf} / {@code refresh} / {@code defOf} 等仍留在具体类）。{@code NodeGraphController}
 * 借它提供一个共享的 {@code activatable} 接线，避免两棵树各写一遍 {@code n -> allocator.canAllocate(n.id)}。
 * 节点/连线的「状态 → 颜色」映射刻意不进接口——那是两棵树各自 {@code NodePainter} / 主题的职责。
 */
public interface NodeAllocator {

    /** 该节点是否已被分配（含「已点但休眠」）。 */
    boolean isAllocated(String nodeId);

    /** 该节点当前是否点亮（生效）。 */
    boolean isActive(String nodeId);

    /** 该节点当前是否可点（联通性 / 预算 / 前置 全部满足）。 */
    boolean canAllocate(String nodeId);

    /** 乐观预览：{@code allocate=true} 点亮、{@code false} 取消（仅本地即时反馈，权威态由同步覆盖）。 */
    void preview(String nodeId, boolean allocate);

    /** 权威同步：以服务端下发的完整分配集合覆盖本地。 */
    void applySync(Set<String> allocated);

    /** 已分配节点数（天赋 = 总分配；研究 = 已完成）。 */
    int allocatedCount();

    /** 当前可见的剩余点数。 */
    int remainingPoints();
}
