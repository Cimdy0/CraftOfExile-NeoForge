package com.cimdy.coe.ui.widget.flex.core;

import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.FlexWrap;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.SidePanel;
import com.cimdy.coe.ui.widget.component.panel.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * 完整 Flexbox 布局引擎（纯算法，零渲染依赖）。
 *
 * <p>用法：构建 {@link FlexNode} 树 → {@link #layout(FlexNode, int, int)} →
 * 读取各节点 {@code x/y/width/height}（相对父节点内容区原点）。
 *
 * <p>支持：row/column 主轴、6 种 justify-content、stretch/start/center/end 交叉轴对齐、
 * nowrap/wrap、flex-grow/shrink/basis、主轴与交叉轴 gap、四边 margin/padding、
 * 以及 border-box 尺寸 hint（width/height）。
 */
public final class FlexLayout {

    private FlexLayout() {}

    public static void layout(FlexNode root, int availableWidth, int availableHeight) {
        measure(root, availableWidth, availableHeight);
        place(root, 0, 0, root.measuredWidth, root.measuredHeight);
    }

    // ============================ 测量 ============================

    private static void measure(FlexNode node, int maxWidth, int maxHeight) {
        FlexStyle s = node.style;
        int padX = s.padding.horizontal();
        int padY = s.padding.vertical();
        int contentMaxW = (maxWidth == FlexStyle.SIZE_UNSPECIFIED) ? FlexStyle.SIZE_UNSPECIFIED : Math.max(0, maxWidth - padX);
        int contentMaxH = (maxHeight == FlexStyle.SIZE_UNSPECIFIED) ? FlexStyle.SIZE_UNSPECIFIED : Math.max(0, maxHeight - padY);

        int contentW, contentH;
        if (node.isLeaf()) {
            int iw = (s.width != FlexStyle.SIZE_UNSPECIFIED) ? s.width
                    : (node.contentSize != null ? node.contentSize.getWidth(contentMaxW) : 0);
            int ih = (s.height != FlexStyle.SIZE_UNSPECIFIED) ? s.height
                    : (node.contentSize != null ? node.contentSize.getHeight(contentMaxH) : 0);
            contentW = iw;
            contentH = ih;
        } else {
            for (FlexNode c : node.children) measure(c, contentMaxW, contentMaxH);
            if (s.direction.isRow()) {
                contentW = 0;
                contentH = 0;
                for (FlexNode c : node.children) contentW += c.measuredWidth;
                contentW += Math.max(0, node.children.size() - 1) * s.gapMain;
                for (FlexNode c : node.children) contentH = Math.max(contentH, c.measuredHeight);
            } else {
                contentW = 0;
                contentH = 0;
                for (FlexNode c : node.children) contentH += c.measuredHeight;
                contentH += Math.max(0, node.children.size() - 1) * s.gapMain;
                for (FlexNode c : node.children) contentW = Math.max(contentW, c.measuredWidth);
            }
        }
        node.measuredWidth = contentW + padX;
        node.measuredHeight = contentH + padY;
        if (s.width != FlexStyle.SIZE_UNSPECIFIED) node.measuredWidth = s.width;
        if (s.height != FlexStyle.SIZE_UNSPECIFIED) node.measuredHeight = s.height;
    }

    // ============================ 布局 ============================

    private static void place(FlexNode node, int ox, int oy, int boxW, int boxH) {
        node.x = ox;
        node.y = oy;
        node.width = boxW;
        node.height = boxH;
        if (node.isLeaf()) return;

        FlexStyle s = node.style;
        boolean row = s.direction.isRow();
        int cx = ox + s.padding.left;
        int cy = oy + s.padding.top;
        int cw = Math.max(0, boxW - s.padding.horizontal());
        int ch = Math.max(0, boxH - s.padding.vertical());

        // 1. 按 wrap 分组为行
        List<List<FlexNode>> lines = new ArrayList<>();
        if (s.wrap == FlexWrap.NOWRAP) {
            lines.add(new ArrayList<>(node.children));
        } else {
            List<FlexNode> line = new ArrayList<>();
            int used = 0;
            for (FlexNode c : node.children) {
                int size = row ? c.measuredWidth : c.measuredHeight;
                int limit = row ? cw : ch;
                if (!line.isEmpty() && used + s.gapMain + size > limit) {
                    lines.add(line);
                    line = new ArrayList<>();
                    used = size;
                } else {
                    if (!line.isEmpty()) used += s.gapMain;
                    used += size;
                    line.add(c);
                }
            }
            if (!line.isEmpty()) lines.add(line);
        }

        // 2. 沿交叉轴堆叠各行（align-content 简化为 FLEX_START）
        //
        // 关键：单行（NOWRAP）容器的交叉轴 stretch/对齐基准必须是「容器交叉内容尺寸」
        // （row 用 ch、column 用 cw），而不是 lineCrossExtent()（= 最宽子项的自然交叉尺寸）。
        // 否则固定尺寸容器（如 Window.flex.size(w,h)）的子节点只会 stretch 到最宽子项宽度，
        // 右侧留白露出半透暗底（观感纯黑）、标题宽度漂移依赖内部文字、列表行被挤窄。
        // 多行（WRAP）时仍按每行自然跨度堆叠，保持换行语义。
        int crossAvail = row ? ch : cw;
        int crossCursor = row ? cy : cx;
        for (List<FlexNode> line : lines) {
            int lineCross = (s.wrap == FlexWrap.NOWRAP)
                    ? crossAvail
                    : lineCrossExtent(line, row);
            int mainAvail = row ? cw : ch;
            int contentMainStart = row ? cx : cy;
            int lineCrossStart = crossCursor;
            layoutLine(line, row, contentMainStart, lineCrossStart, mainAvail, lineCross, s);
            crossCursor += lineCross + s.gapCross;
        }

        // 递归布局孙子节点：3 层+ 的容器嵌套，其直接子节点坐标由上层
        // layoutLine() 算出后，必须继续向下传递，否则更深层节点永远停在 (0,0,0,0)。
        // Scrollable/SidePanel 等通过 toFlex() 伪装成叶子，其 FlexNode 没有 children，自然跳过。
        for (FlexNode c : node.children) {
            if (!c.isLeaf()) place(c, c.x, c.y, c.width, c.height);
        }
    }

    /** 一行在交叉轴上的总跨度。 */
    private static int lineCrossExtent(List<FlexNode> line, boolean row) {
        int max = 0;
        for (FlexNode c : line) {
            int childCross = row ? c.measuredHeight : c.measuredWidth;
            int marginCross = row ? c.style.margin.vertical() : c.style.margin.horizontal();
            max = Math.max(max, childCross + marginCross);
        }
        return max;
    }

    /** 在一行内解析主轴尺寸（grow/shrink）并定位每个子节点。 */
    private static void layoutLine(List<FlexNode> line, boolean row,
                                   int contentMainStart, int lineCrossStart,
                                   int mainAvail, int lineCross, FlexStyle parent) {
        int n = line.size();
        int gap = parent.gapMain;

        // 1. 计算各子节点解析后的主轴尺寸
        int[] resolved = new int[n];
        int totalBasis = 0;
        boolean canGrow = false, canShrink = false;
        for (int i = 0; i < n; i++) {
            FlexNode c = line.get(i);
            int base = (c.style.basis != FlexStyle.BASIS_AUTO)
                    ? c.style.basis
                    : (row ? c.measuredWidth : c.measuredHeight);
            resolved[i] = base;
            totalBasis += base;
            if (c.style.grow > 0) canGrow = true;
            if (c.style.shrink > 0) canShrink = true;
        }
        int free = mainAvail - totalBasis - Math.max(0, n - 1) * gap;
        if (free > 0 && canGrow) {
            float tg = 0;
            for (FlexNode c : line) tg += c.style.grow;
            if (tg > 0) for (int i = 0; i < n; i++) resolved[i] += (int) (free * line.get(i).style.grow / tg);
        } else if (free < 0 && canShrink) {
            float ts = 0;
            for (int i = 0; i < n; i++) ts += line.get(i).style.shrink * resolved[i];
            if (ts > 0) for (int i = 0; i < n; i++)
                resolved[i] -= (int) (-free * line.get(i).style.shrink * resolved[i] / ts);
        }
        for (int i = 0; i < n; i++) resolved[i] = Math.max(0, resolved[i]);

        // 2. justify-content 偏移与额外间距
        int usedMain = 0;
        for (int r : resolved) usedMain += r;
        usedMain += Math.max(0, n - 1) * gap;
        int remaining = mainAvail - usedMain;
        int offset = 0, extraGap = 0;
        switch (parent.justifyContent) {
            case CENTER: offset = remaining / 2; break;
            case FLEX_END: offset = remaining; break;
            case SPACE_BETWEEN: if (n > 1) extraGap = remaining / (n - 1); break;
            case SPACE_AROUND: if (n > 0) { offset = remaining / (2 * n); extraGap = remaining / n; } break;
            case SPACE_EVENLY: if (n > 0) { offset = remaining / (n + 1); extraGap = remaining / (n + 1); } break;
            default: break;
        }

        // 3. 逐子节点定位
        int cursor = 0;
        for (int i = 0; i < n; i++) {
            FlexNode c = line.get(i);
            int marginMainStart = row ? c.style.margin.left : c.style.margin.top;
            int marginCrossStart = row ? c.style.margin.top : c.style.margin.left;
            int marginCross = row ? c.style.margin.vertical() : c.style.margin.horizontal();

            boolean definiteCross = (row ? c.style.height : c.style.width) != FlexStyle.SIZE_UNSPECIFIED;
            int crossSize;
            int crossPos;
            if (parent.alignItems == AlignItems.STRETCH && !definiteCross) {
                crossSize = Math.max(0, lineCross - marginCross);
                crossPos = marginCrossStart;
            } else {
                int childCross = row ? c.measuredHeight : c.measuredWidth;
                if (definiteCross) childCross = row ? c.style.height : c.style.width;
                crossSize = Math.max(0, childCross);
                int rem = lineCross - marginCross - childCross;
                if (rem < 0) rem = 0;
                switch (parent.alignItems) {
                    case CENTER: crossPos = marginCrossStart + rem / 2; break;
                    case FLEX_END: crossPos = marginCrossStart + rem; break;
                    default: crossPos = marginCrossStart;
                }
            }
            crossSize = Math.max(0, crossSize);

            int mainPos = offset + cursor + marginMainStart;
            int mainStart = contentMainStart + mainPos;
            int crossStart = lineCrossStart + crossPos;
            if (row) {
                c.x = mainStart;
                c.y = crossStart;
                c.width = resolved[i];
                c.height = crossSize;
            } else {
                c.x = crossStart;
                c.y = mainStart;
                c.width = crossSize;
                c.height = resolved[i];
            }
            cursor += resolved[i] + gap + extraGap;
        }
    }
}
