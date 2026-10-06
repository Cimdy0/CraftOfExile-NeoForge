package com.cimdy.coe.ui.widget.component.panel;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.core.FlexNode;
import com.cimdy.coe.ui.widget.flex.core.FlexStyle;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/**
 * 侧边抽屉：可展开/收起的边栏，带缓动动画。
 *
 * <p>旧 {@code SidePanel} 的展开是「瞬间改宽度」，而且宽度直接写死在组件里。
 * 这里改成时间驱动的插值，并且<b>动画量直接参与布局</b> —— 展开过程中兄弟节点会
 * 平滑地让位，而不是等动画结束才跳一下。
 *
 * <p>做法是覆写 {@link #toFlex()}，把当前插值出来的尺寸写进布局样式，
 * 于是每一帧 Flexbox 看到的都是「此刻的宽度」。
 */
public class SidePanel extends Node {

    public enum Side { LEFT, RIGHT, TOP, BOTTOM }

    private final Side side;
    private final Node content = new Node().column();
    private Button handle;

    private int expandedSize = 100;
    private int collapsedSize = 10;
    private long animMillis = 150;

    private boolean expanded = true;
    private long animStart = 0;
    /** 动画起点尺寸，保证半途反向也能平滑接上。 */
    private int animFrom;

    public SidePanel(Side side) {
        this.side = side;
        this.id = "side-panel";
        content.id = "side-panel-content";
        content.flex.grow = 1;
        if (horizontal()) {
            row();
        } else {
            column();
        }
        super.add(content);
        animFrom = expandedSize;
    }

    public static SidePanel left()   { return new SidePanel(Side.LEFT); }
    public static SidePanel right()  { return new SidePanel(Side.RIGHT); }
    public static SidePanel top()    { return new SidePanel(Side.TOP); }
    public static SidePanel bottom() { return new SidePanel(Side.BOTTOM); }

    private boolean horizontal() { return side == Side.LEFT || side == Side.RIGHT; }

    // ------------------------------------------------------------------
    // 结构
    // ------------------------------------------------------------------

    public Node content() { return content; }

    @Override
    public Node add(Node child) { content.add(child); return this; }

    @Override
    public Node remove(Node child) { content.remove(child); return this; }

    @Override
    public Node clearChildren() { content.clearChildren(); return this; }

    /** 加一个默认的折叠把手（箭头按钮）。 */
    public SidePanel withHandle() {
        if (handle != null) return this;
        handle = new Button().onClick(this::toggle).clickSound(true);
        handle.id = "side-panel-handle";
        handle.background(Theme.handle());
        handle.label(arrowGlyph());
        if (horizontal()) {
            handle.flex.width = collapsedSize;
        } else {
            handle.flex.height = collapsedSize;
        }
        // 把手贴在靠屏幕内侧的那一边：左/上面板在后，右/下面板在前
        if (side == Side.LEFT || side == Side.TOP) {
            super.add(handle);
        } else {
            children.addFirst(handle);
            handle.parent = this;
        }
        return this;
    }

    public Button handle() { return handle; }

    private Component arrowGlyph() {
        boolean out = expanded ^ (side == Side.RIGHT || side == Side.BOTTOM);
        if (horizontal()) return Component.literal(out ? "<" : ">");
        return Component.literal(out ? "^" : "v");
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public SidePanel expandedSize(int px) { this.expandedSize = Math.max(0, px); return this; }

    public SidePanel collapsedSize(int px) { this.collapsedSize = Math.max(0, px); return this; }

    public SidePanel animMillis(long ms) { this.animMillis = Math.max(0, ms); return this; }

    /** 中性默认皮肤。 */
    public SidePanel styled() {
        visuals.addFirst(Theme.panel());
        return this;
    }

    /** 顶部标题条。 */
    public SidePanel title(Component text) {
        Node bar = new Node();
        bar.id = "side-panel-title";
        bar.flex.height = 10;
        bar.with(Renderers.text(text)
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE));
        content.children.addFirst(bar);
        bar.parent = content;
        return this;
    }

    // ------------------------------------------------------------------
    // 展开 / 收起
    // ------------------------------------------------------------------

    public boolean isExpanded() { return expanded; }

    public void toggle() { setExpanded(!expanded); }

    public void setExpanded(boolean b) {
        if (b == expanded) return;
        animFrom = currentSize();
        expanded = b;
        animStart = Util.getMillis();
        if (handle != null) handle.label(arrowGlyph());
    }

    /** 不带动画地直接切换（初始化时用）。 */
    public void setExpandedImmediate(boolean b) {
        expanded = b;
        animStart = 0;
        animFrom = b ? expandedSize : collapsedSize;
        if (handle != null) handle.label(arrowGlyph());
    }

    /** 当前插值出来的主轴尺寸。 */
    public int currentSize() {
        int target = expanded ? expandedSize : collapsedSize;
        if (animStart == 0 || animMillis <= 0) return target;
        long elapsed = Util.getMillis() - animStart;
        if (elapsed >= animMillis) return target;
        float t = elapsed / (float) animMillis;
        t = easeOutCubic(t);
        return Math.round(animFrom + (target - animFrom) * t);
    }

    private static float easeOutCubic(float t) {
        float inv = 1F - t;
        return 1F - inv * inv * inv;
    }

    // ------------------------------------------------------------------
    // 布局：把动画尺寸喂进 Flexbox
    // ------------------------------------------------------------------

    @Override
    protected FlexNode toFlex() {
        FlexNode fn = super.toFlex();
        int size = currentSize();
        if (horizontal()) {
            fn.style.width = size;
        } else {
            fn.style.height = size;
        }
        // 动画期间不让内容把面板撑开
        fn.style.shrink = 0;
        fn.style.basis = FlexStyle.BASIS_AUTO;
        return fn;
    }

    @Override
    protected void renderChildren(net.minecraft.client.gui.GuiGraphicsExtractor g,
                                  com.cimdy.coe.ui.widget.render.RenderContext ctx, boolean disabled) {
        // 收起过程中内容会溢出面板，必须裁掉
        if (width > 0 && height > 0) {
            g.enableScissor(absX, absY, absX + width, absY + height);
            try {
                super.renderChildren(g, ctx, disabled);
            } finally {
                g.disableScissor();
            }
        }
    }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        SidePanel p = new SidePanel(side);
        copyPropsInto(p);
        p.expandedSize = expandedSize;
        p.collapsedSize = collapsedSize;
        p.animMillis = animMillis;
        p.setExpandedImmediate(expanded);
        for (Node c : content.children) p.content.add(c.copy());
        if (handle != null) p.withHandle();
        return p;
    }
}
