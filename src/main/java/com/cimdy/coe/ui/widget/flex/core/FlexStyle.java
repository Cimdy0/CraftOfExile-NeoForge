package com.cimdy.coe.ui.widget.flex.core;

import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.enums.FlexDirection;
import com.cimdy.coe.ui.widget.flex.enums.FlexWrap;
import com.cimdy.coe.ui.widget.flex.enums.Insets;
import com.cimdy.coe.ui.widget.flex.enums.JustifyContent;

/**
 * Flex 布局样式。仅描述布局意图，不持有任何渲染状态。
 *
 * <p>坐标约定：封装盒（border-box）尺寸 = 内容尺寸 + padding。
 * {@link #width}/{@link #height} 为 -1 表示「不限定」（由内容/子节点决定）。
 */
public class FlexStyle {

    public FlexDirection direction = FlexDirection.ROW;
    public JustifyContent justifyContent = JustifyContent.FLEX_START;
    public AlignItems alignItems = AlignItems.STRETCH;
    public FlexWrap wrap = FlexWrap.NOWRAP;

    /** flex-grow：剩余主轴的分配权重，0 表示不伸缩。 */
    public float grow = 0f;
    /** flex-shrink：空间不足时的收缩权重，需 >0 才参与收缩。 */
    public float shrink = 1f;
    /** flex-basis（像素，border-box）。AUTO 表示回退到节点自身测量尺寸。 */
    public int basis = BASIS_AUTO;
    public static final int BASIS_AUTO = -1;

    public final Insets margin = new Insets(0);
    public final Insets padding = new Insets(0);

    /** 主轴方向相邻子节点间距。 */
    public int gapMain = 0;
    /** 交叉轴方向相邻行间距（仅在 wrap 时生效）。 */
    public int gapCross = 0;

    /** 不限定为 -1；指定则为 border-box 尺寸。 */
    public int width = SIZE_UNSPECIFIED;
    public int height = SIZE_UNSPECIFIED;
    public static final int SIZE_UNSPECIFIED = -1;

    public FlexStyle() {}

    /** 把 src 的字段复制进本样式（Node 构建 FlexNode 树时复用）。 */
    public void copyFrom(FlexStyle src) {
        this.direction = src.direction;
        this.justifyContent = src.justifyContent;
        this.alignItems = src.alignItems;
        this.wrap = src.wrap;
        this.grow = src.grow;
        this.shrink = src.shrink;
        this.basis = src.basis;
        this.gapMain = src.gapMain;
        this.gapCross = src.gapCross;
        this.width = src.width;
        this.height = src.height;
        this.margin.set(src.margin.top, src.margin.right, src.margin.bottom, src.margin.left);
        this.padding.set(src.padding.top, src.padding.right, src.padding.bottom, src.padding.left);
    }

    /** 返回一份独立的副本。 */
    public FlexStyle copy() {
        FlexStyle s = new FlexStyle();
        s.copyFrom(this);
        return s;
    }

    public FlexStyle direction(FlexDirection d) { this.direction = d; return this; }
    public FlexStyle justifyContent(JustifyContent j) { this.justifyContent = j; return this; }
    public FlexStyle alignItems(AlignItems a) { this.alignItems = a; return this; }
    public FlexStyle wrap(FlexWrap w) { this.wrap = w; return this; }
    public FlexStyle grow(float g) { this.grow = g; return this; }
    public FlexStyle shrink(float s) { this.shrink = s; return this; }
    public FlexStyle basis(int b) { this.basis = b; return this; }
    public FlexStyle gap(int main, int cross) { this.gapMain = main; this.gapCross = cross; return this; }
    public FlexStyle margin(int all) { this.margin.set(all); return this; }
    public FlexStyle margin(int top, int right, int bottom, int left) { this.margin.set(top, right, bottom, left); return this; }
    public FlexStyle padding(int all) { this.padding.set(all); return this; }
    public FlexStyle padding(int top, int right, int bottom, int left) { this.padding.set(top, right, bottom, left); return this; }
    public FlexStyle size(int w, int h) { this.width = w; this.height = h; return this; }

}
