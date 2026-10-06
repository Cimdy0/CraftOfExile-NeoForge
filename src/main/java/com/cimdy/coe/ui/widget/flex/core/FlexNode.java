package com.cimdy.coe.ui.widget.flex.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Flex 布局树中的一个节点。纯数据 + 布局结果，不持有任何渲染逻辑。
 *
 * <p>典型用法：构建一棵 {@link FlexNode} 树 → 调用 {@link FlexLayout#layout} →
 * 读取每个节点的 {@link #x}/{@link #y}/{@link #width}/{@link #height} 作为渲染位置。
 */
public class FlexNode {

    public final FlexStyle style = new FlexStyle();
    public final List<FlexNode> children = new ArrayList<>();

    /**
     * 叶子节点的固有内容尺寸提供者（border-box 之外的内容尺寸）。
     * 返回 null 表示「无固有尺寸」（仅靠 width/height hint 或作为伸缩项）。
     */
    public ContentSize contentSize = null;

    /** 测量得到的 border-box 尺寸（measure 阶段填充）。 */
    public int measuredWidth, measuredHeight;
    /** 布局结果：相对父节点内容区原点的 box（place 阶段填充）。 */
    public int x, y, width, height;

    public FlexNode() {}
    public FlexNode(ContentSize contentSize) { this.contentSize = contentSize; }

    public FlexNode add(FlexNode child) { children.add(child); return this; }

    public boolean isLeaf() { return children.isEmpty(); }

    public int contentWidth() { return Math.max(0, width - style.padding.horizontal()); }
    public int contentHeight() { return Math.max(0, height - style.padding.vertical()); }

    /**
     * 内容尺寸提供者。不依赖任何游戏/渲染类型，使布局层保持纯净。
     */
    public interface ContentSize {
        /** 给定最大内容宽度（像素，已扣除 padding；-1 表示无限制），返回固有内容宽度。 */
        int getWidth(int maxWidth);
        /** 给定最大内容高度（-1 表示无限制），返回固有内容高度。 */
        int getHeight(int maxHeight);
    }

    /** 便捷：固定尺寸内容。 */
    public static ContentSize fixed(int w, int h) {
        return new ContentSize() {
            @Override public int getWidth(int maxWidth) { return w; }
            @Override public int getHeight(int maxHeight) { return h; }
        };
    }

    /** 便捷：由函数推导尺寸（例如文本测量）。 */
    public static ContentSize of(BiFunction<Integer, Integer, Integer> w, BiFunction<Integer, Integer, Integer> h) {
        return new ContentSize() {
            @Override public int getWidth(int maxWidth) { return w.apply(maxWidth, -1); }
            @Override public int getHeight(int maxHeight) { return h.apply(-1, maxHeight); }
        };
    }
}
