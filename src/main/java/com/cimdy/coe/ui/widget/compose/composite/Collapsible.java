package com.cimdy.coe.ui.widget.compose.composite;

import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Animatable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.flex.core.FlexNode;
import com.cimdy.coe.ui.widget.flex.core.FlexStyle;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 可折叠分区：一条可点击的标题栏 + 一块能展开/收起的内容区，收放带高度缓动。
 *
 * <p>「把一堆设置分组、默认收起来、点标题展开」是长界面的刚需，只靠
 * {@code visible = false} 硬切 —— 没有动画、而且收起时节点仍占位（Flex 不看 visible），
 * 于是下面的内容不会顶上来。这里两件事一起解决：
 *
 * <ul>
 *   <li>内容装在一个<b>伪装成叶子</b>的视口里（同 {@code Scrollable} 的做法），
 *       视口高度就是「自然高度 × 展开进度」，父容器因此会跟着重排；</li>
 *   <li>自然高度每帧用「视口宽 × 无限高」单独量一次，所以内容动态增删也不用手报高度。</li>
 * </ul>
 *
 * <pre>{@code
 * Collapsible sec = new Collapsible().styled()
 *         .title(Component.literal("高级设置"))
 *         .expanded(false);
 * sec.add(someRow);
 * }</pre>
 */
public class Collapsible extends Node {

    /** 「无限」可用空间，与 {@code Scrollable} 同口径。 */
    private static final int UNBOUNDED = 1 << 20;

    private final Button header = new Button();
    private final Viewport viewport = new Viewport();
    private final Animatable anim = new Animatable(0, 0.35);

    private Component title = Component.empty();
    private Component markerExpanded = Component.literal("- ");
    private Component markerCollapsed = Component.literal("+ ");
    private boolean expanded = false;
    private boolean animated = true;
    private final Property<Boolean> value = new SimpleObservable<>(false);
    private Consumer<Boolean> onToggle;

    /** 内容的自然高度，由视口每帧量出。 */
    private int naturalHeight = 0;

    public Collapsible() {
        this.id = "collapsible";
        column();
        flex.alignItems = AlignItems.STRETCH;

        header.id = "collapsible-header";
        header.flex.height = 14;
        header.onClick(() -> setExpanded(!expanded, true));
        refreshHeader();
        if (header.labelRenderer() != null) {
            header.labelRenderer()
                    .align(Align.LEFT)
                    .vAlign(VAlign.MIDDLE);
        }

        viewport.flex.height = 0;
        viewport.visible = false;

        super.add(header);
        super.add(viewport);
    }

    public static Collapsible of(Component title) { return new Collapsible().title(title); }

    // ------------------------------------------------------------------
    // 内容：子节点一律落进折叠区
    // ------------------------------------------------------------------

    @Override public Node add(Node child)   { viewport.add(child); return this; }
    @Override public Node remove(Node child){ viewport.remove(child); return this; }
    @Override public Node clearChildren()   { viewport.clearChildren(); return this; }

    /** 内容容器，需要给内容整体设 gap/padding/direction 时用它。 */
    public Node body() { return viewport.body; }

    /** 标题栏节点，可继续挂 tooltip 或换皮肤。 */
    public Button header() { return header; }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public Collapsible title(Component c) {
        this.title = (c == null) ? Component.empty() : c;
        refreshHeader();
        return this;
    }

    public Collapsible title(String s) { return title(Component.literal(s)); }

    /** 展开/收起的前缀标记，默认 {@code - } / {@code + }。 */
    public Collapsible markers(Component expandedMark, Component collapsedMark) {
        this.markerExpanded = expandedMark;
        this.markerCollapsed = collapsedMark;
        refreshHeader();
        return this;
    }

    /** 关掉缓动，切换即到位。 */
    public Collapsible animated(boolean b) { this.animated = b; return this; }

    /** 展开状态变化回调（仅用户点击或 {@link #toggle()} 时触发，代码直设不触发）。 */
    public Collapsible onToggle(Consumer<Boolean> c) { this.onToggle = c; value.addListener((o, n) -> c.accept(n)); return this; }

    public boolean isExpanded() { return expanded; }

    /** 代码设定展开态，不触发回调；首次设定不做动画，避免开局闪一下。 */
    public Collapsible expanded(boolean b) {
        setExpanded(b, false);
        anim.snap();
        return this;
    }

    /** 切换展开态并触发回调。 */
    public Collapsible toggle() {
        setExpanded(!expanded, true);
        return this;
    }

    private void setExpanded(boolean b, boolean fire) {
        if (expanded == b) return;
        expanded = b;
        anim.target(b ? 1 : 0);
        if (!animated) anim.snap();
        refreshHeader();
        if (fire) value.set(expanded);
    }

    private void refreshHeader() {
        Component mark = expanded ? markerExpanded : markerCollapsed;
        header.label(Component.empty().append(mark).append(title));
    }

    /** 套一套中性默认皮肤：标题栏用按钮皮肤。 */
    public Collapsible styled() {
        header.styled();
        if (header.labelRenderer() != null) {
            header.labelRenderer().colorDisabled(Theme.current().textDisabled()).colorHover(Theme.current().textHover());
        }
        return this;
    }

    // ------------------------------------------------------------------
    // 布局与动画
    // ------------------------------------------------------------------

    @Override
    protected FlexNode toFlex() {
        // 每次排版前按当前动画进度定视口高度：父容器因此会随展开过程连续重排
        int h = (int) Math.round(anim.value() * naturalHeight);
        viewport.flex.height = Math.max(0, h);
        viewport.visible = h > 0;
        return super.toFlex();
    }

    @Override
    public void tick() {
        super.tick();
        anim.tick();
    }

    // ------------------------------------------------------------------

    @Override
    public Collapsible copy() {
        Collapsible c = new Collapsible();
        c.flex.copyFrom(flex);
        c.id = id;
        c.visible = visible;
        c.state = state;
        c.title = title;
        c.markerExpanded = markerExpanded;
        c.markerCollapsed = markerCollapsed;
        c.animated = animated;
        if (onToggle != null) c.onToggle(onToggle);
        c.header.flex.copyFrom(header.flex);
        c.viewport.body.flex.copyFrom(viewport.body.flex);
        List<Node> kids = new ArrayList<>(viewport.body.children);
        for (Node k : kids) c.add(k.copy());
        c.expanded(expanded);
        c.refreshHeader();
        return c;
    }

    // ------------------------------------------------------------------
    // 折叠视口：对父级是叶子，对内容是一块可裁剪的画布
    // ------------------------------------------------------------------

    private class Viewport extends Node {

        private final Node body = new Node();

        Viewport() {
            this.id = "collapsible-body";
            body.id = "collapsible-content";
            body.column();
            super.add(body);
        }

        @Override public Node add(Node child)    { body.add(child); return this; }
        @Override public Node remove(Node child) { body.remove(child); return this; }
        @Override public Node clearChildren()    { body.clearChildren(); return this; }

        /** 对父级伪装成叶子：内容多高都不会把折叠区撑开。 */
        @Override
        protected FlexNode toFlex() {
            FlexNode fn = new FlexNode();
            fn.style.copyFrom(this.flex);
            return fn;
        }

        @Override
        protected void applyFlex(FlexNode fn, int baseX, int baseY) {
            super.applyFlex(fn, baseX, baseY);
            int vw = contentWidth();
            if (vw <= 0) return;
            body.flex.width = vw;
            body.flex.height = FlexStyle.SIZE_UNSPECIFIED;
            body.layout(contentX(), contentY(), vw, UNBOUNDED);
            naturalHeight = body.height;
        }

        @Override
        protected void renderChildren(GuiGraphicsExtractor g, RenderContext ctx, boolean disabled) {
            if (width <= 0 || height <= 0) return;
            // 收放途中内容比视口高，必须裁剪；鼠标不在视口里就下传「无鼠标」上下文，
            // 否则被裁掉的那截照样算 hover
            RenderContext inner = Draw.inside(ctx.mouseX(), ctx.mouseY(), absX, absY, width, height)
                    ? ctx : ctx.withoutMouse();
            g.enableScissor(absX, absY, absX + width, absY + height);
            try {
                body.render(g, inner, disabled);
            } finally {
                g.disableScissor();
            }
        }
    }
}
