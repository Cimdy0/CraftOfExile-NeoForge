package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.data.codec.progression.research.ResearchNodeDef;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.*;
import com.cimdy.coe.ui.screen.progression.ProgressionTheme;
import com.cimdy.coe.ui.screen.progression.ResearchAllocator;

/**
 * 研究树节点画笔（业务专属）：按 {@link ResearchAllocator} 的三态着色。
 *
 * <ul>
 *   <li>已完成 → 青绿常亮；</li>
 *   <li>进行中 → 暗青（投入了但没到手）；</li>
 *   <li>可研究 → 亮青可点；</li>
 *   <li>锁定 → 灰；</li>
 *   <li>数据包损坏（前置指向不存在节点）→ 暗红，一眼可辨。</li>
 * </ul>
 */
class ProgressionNodePainter implements NodePainter<ResearchNodeDef>, ProgressionNodePainterI {

    private final ResearchAllocator allocator;
    /** 前置缺失的节点 id → 缺失详情（tooltip 会展示）。 */
    private final Set<String> broken = new HashSet<>();
    private final Map<String, List<Component>> brokenDetails = new HashMap<>();

    public ProgressionNodePainter(ResearchAllocator allocator) {
        this.allocator = allocator;
    }

    public void setBroken(Set<String> ids) {
        broken.clear();
        broken.addAll(ids);
    }

    /** 标记一个「前置缺失」的节点，并附上缺失详情。 */
    public void markBroken(String id, List<Component> details) {
        broken.add(id);
        brokenDetails.put(id, details);
    }

    public List<Component> brokenDetails(String id) {
        return brokenDetails.get(id);
    }

    @Override
    public void paint(GuiGraphicsExtractor g, GraphNode<ResearchNodeDef> node, RenderContext ctx, float zoom) {
        float cx = node.worldX, cy = node.worldY;
        int r = Math.max(1, Math.round(node.radius));
        String id = node.id;

        boolean locked = false;
        int fill = ProgressionTheme.fillNode();
        int border;
        if (broken.contains(id)) {
            fill = ProgressionTheme.fillBroken();
            border = ProgressionTheme.borderLocked();
            locked = true;
        } else if (allocator.isActive(id)) {
            border = ProgressionTheme.borderAllocated();
        } else if (allocator.isAllocated(id)) {
            border = ProgressionTheme.borderAllocatedInactive();
        } else if (allocator.canAllocate(id)) {
            border = ProgressionTheme.borderAvailable();
        } else {
            border = ProgressionTheme.borderLocked();
            locked = true;
        }
        if (ctx.hovered()) border = lighten(border);

        int thick = Math.max(1, Math.round(1f / Math.max(1e-4F, zoom)));
        int xx = Math.round(cx), yy = Math.round(cy);
        Draw.discWithBorder(g, xx, yy, r, fill, border, thick);

        // 锁定/禁用 = 断点环（与天赋树 C2 同形状语言）：边框带四正方向 30° 缺口，
        // 用 fill 回涂，绝不露画布底色，且不误伤连线/邻节点（边先于节点绘制，见 GraphCanvas）。
        if (locked) {
            int rIn = r - thick, rOut = r;
            int gap = Math.max(2, (int) Math.round((r - thick * 0.5F) * 2 * Math.sin(Math.toRadians(15))));
            Draw.fill(g, xx - gap / 2, yy - rOut, gap, rOut - rIn, fill);   // N
            Draw.fill(g, xx - gap / 2, yy + rIn, gap, rOut - rIn, fill);    // S
            Draw.fill(g, xx - rOut, yy - gap / 2, rOut - rIn, gap, fill);   // W
            Draw.fill(g, xx + rIn, yy - gap / 2, rOut - rIn, gap, fill);    // E
        }
    }

    private static int lighten(int color) {
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * 1.4F) + 30);
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * 1.4F) + 30);
        int b = Math.min(255, (int) ((color & 0xFF) * 1.4F) + 30);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
