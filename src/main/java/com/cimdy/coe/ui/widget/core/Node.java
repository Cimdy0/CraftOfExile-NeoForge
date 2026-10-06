package com.cimdy.coe.ui.widget.core;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.attach.Attachment;
import com.cimdy.coe.ui.widget.flex.core.*;
import com.cimdy.coe.ui.widget.flex.enums.*;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 统一节点：UI 树的最小单元，也是所有组件的基类。
 *
 * <p>旧库同一件事有三套机制：{@code AdaptiveWidget} 的子部件、{@code AdaptiveData} 的数据子树、
 * 以及 {@code renders} 渲染器列表 —— 三者各有一套添加/遍历/转发代码，谁包谁没有定论，
 * 事件要在每层手写转发样板。这里收敛成一棵树、四件事：
 *
 * <ul>
 *   <li><b>树</b>：{@link #children} 是唯一的层级结构；</li>
 *   <li><b>布局</b>：{@link #flex} 声明布局意图，{@link #layout} 用 Flexbox 引擎一次算出全树绝对坐标；</li>
 *   <li><b>外观</b>：{@link #visuals} 是按序叠加的渲染器列表，共享同一个 {@link VisualState}；</li>
 *   <li><b>事件</b>：从根单点向下命中分发，容器不需要写任何转发代码。</li>
 * </ul>
 */
public class Node {

    public String id = "node";

    /** 布局样式。 */
    public final FlexStyle flex = new FlexStyle();

    /** 子节点，绘制与命中顺序均为「后加的在上层」。 */
    public final List<Node> children = new ArrayList<>();

    /** 外观层，按序叠加绘制。 */
    public final List<Renderer> visuals = new ArrayList<>();

    /** 附件层（装饰/音效/特效）：可选增强，不改变输入链路，失败静默降级。懒初始化。 */
    private List<Attachment> attachments;

    /** 父节点，由 {@link #add} 维护。 */
    public Node parent;

    /** 自身可视状态（hover 每帧自动算，其余由业务设置）。 */
    public VisualState state = VisualState.NONE;

    public boolean visible = true;

    /** 布局结果：绝对屏幕坐标。 */
    public int absX, absY, width, height;

    public Node() {}

    // ============================ 组装 ============================

    public Node id(String id) { this.id = id; return this; }

    public Node add(Node child) {
        child.parent = this;
        children.add(child);
        return this;
    }

    public Node add(Node... nodes) {
        for (Node n : nodes) add(n);
        return this;
    }

    public Node remove(Node child) {
        if (children.remove(child)) child.parent = null;
        return this;
    }

    public Node clearChildren() {
        for (Node c : children) c.parent = null;
        children.clear();
        return this;
    }

    /** 追加一层外观。 */
    public Node with(Renderer r) { visuals.add(r); return this; }

    /** 清空全部外观层。模板克隆后整体替换外观时用（先清再叠）。 */
    public Node clearVisuals() { visuals.clear(); return this; }

    // ============================ 附件层 ============================
    // 可选增强（Decor/Sfx/Vfx）：不改变输入与数据链路，失败/禁用静默降级；
    // 核心组件无附件也能完整工作。附件不被 copy() 复制（模板克隆后按需重新 attach）。

    /** 挂载一个或多个附件（可叠加）。 */
    public Node attach(Attachment... atts) {
        if (atts == null || atts.length == 0) return this;
        if (attachments == null) attachments = new ArrayList<>(atts.length);
        for (Attachment a : atts) {
            if (a == null) continue;
            attachments.add(a);
            try {
                a.onAttach(this);
            } catch (RuntimeException ignored) { /* 附件挂载失败静默降级 */ }
        }
        return this;
    }

    /** 移除一个附件。 */
    public Node detach(Attachment a) {
        if (attachments != null) attachments.remove(a);
        return this;
    }

    /** 当前挂载的附件列表（无则 null）。 */
    public List<Attachment> attachments() { return attachments; }

    /**
     * 整行复位：清空子节点与外观层。{@link com.cimdy.coe.ui.widget.component.list.ListView}
     * 的行是模板的克隆体、会被反复 {@code bind}，每次重绑前调它把上一轮的残留清理掉，
     * 业务 binder 就能放心「直接叠外观 / 加子节点」而不必先手写 {@code clearVisuals(); styled();}。
     */
    public Node reset() {
        clearChildren();
        clearVisuals();
        return this;
    }

    public Node visible(boolean b) { this.visible = b; return this; }

    public Node disabled(boolean b) { this.state = state.disabled(b); return this; }

    public boolean isDisabled() { return state.disabled(); }

    // ============================ 布局样式便捷方法 ============================
    // 直接改 flex 字段也可以，这些只是让搭界面时少写几行。
    //
    // 注意链式书写顺序：这些方法返回 Node，子类专属 setter 返回子类型。
    // 想在一条链里混用，把布局部分写在 flex 上即可（FlexStyle 自身也是链式的）：
    //     Button b = new Button().label(...).onClick(...);
    //     b.flex.size(60, 20).margin(2);

    public Node row()    { flex.direction = FlexDirection.ROW; return this; }
    public Node column() { flex.direction = FlexDirection.COLUMN; return this; }

    /** 零或正数才接受；负值会被夹成 0，避免「负尺寸」污染 Flex 布局。 */
    public Node size(int w, int h) { flex.width = Math.max(0, w); flex.height = Math.max(0, h); return this; }
    public Node width(int w)  { flex.width = Math.max(0, w); return this; }
    public Node height(int h) { flex.height = Math.max(0, h); return this; }

    public Node grow(float f)   { flex.grow = f; return this; }
    public Node shrink(float f) { flex.shrink = f; return this; }
    public Node basis(int px)   { flex.basis = px; return this; }

    public Node gap(int px) { flex.gapMain = px; flex.gapCross = px; return this; }
    public Node gap(int main, int cross) { flex.gapMain = main; flex.gapCross = cross; return this; }

    public Node padding(int all) { flex.padding.set(all, all, all, all); return this; }
    public Node padding(int vertical, int horizontal) {
        flex.padding.set(vertical, horizontal, vertical, horizontal);
        return this;
    }
    public Node padding(int top, int right, int bottom, int left) {
        flex.padding.set(top, right, bottom, left);
        return this;
    }

    public Node margin(int all) { flex.margin.set(all, all, all, all); return this; }
    public Node margin(int top, int right, int bottom, int left) {
        flex.margin.set(top, right, bottom, left);
        return this;
    }

    public Node justify(JustifyContent j) { flex.justifyContent = j; return this; }
    public Node alignItems(AlignItems a)  { flex.alignItems = a; return this; }
    public Node wrap(FlexWrap w)          { flex.wrap = w; return this; }

    // ============================ 布局 ============================

    /** 以 (x,y) 为绝对原点、在 (availW × availH) 约束内完成整棵子树的布局。 */
    public void layout(int x, int y, int availW, int availH) {
        FlexNode root = toFlex();
        FlexLayout.layout(root, availW, availH);
        applyFlex(root, x, y);
    }

    /**
     * 把本节点（含子树）翻译成 Flex 布局树。
     * 子类可覆写以把自己伪装成叶子 —— {@code Scrollable} 就靠这个让内容脱离父级布局约束。
     */
    protected FlexNode toFlex() {
        FlexNode fn = new FlexNode();
        fn.style.copyFrom(this.flex);
        if (children.isEmpty()) {
            fn.contentSize = intrinsicContentSize();
        } else {
            for (Node c : children) fn.add(c.toFlex());
        }
        return fn;
    }

    /**
     * 叶子节点的固有内容尺寸 —— 取所有外观层里最大的建议尺寸。
     * 这让「文字撑开按钮」这类需求不用手写宽高，返回 null 表示完全听布局的。
     */
    protected FlexNode.ContentSize intrinsicContentSize() {
        if (visuals.isEmpty()) return null;
        int pw = 0, ph = 0;
        for (Renderer r : visuals) {
            pw = Math.max(pw, r.preferredWidth());
            ph = Math.max(ph, r.preferredHeight());
        }
        if (pw <= 0 && ph <= 0) return null;
        return FlexNode.fixed(pw, ph);
    }

    protected void applyFlex(FlexNode fn, int baseX, int baseY) {
        // FlexLayout 把根放在 (0,0)，子节点坐标已是相对根原点的累计值
        this.absX = baseX + fn.x;
        this.absY = baseY + fn.y;
        this.width = fn.width;
        this.height = fn.height;
        for (int i = 0; i < children.size() && i < fn.children.size(); i++) {
            children.get(i).applyFlex(fn.children.get(i), baseX, baseY);
        }
    }

    /** 整棵子树平移。滚动容器用它把已排好版的内容挪到偏移位置，省掉第二次布局。 */
    public void translate(int dx, int dy) {
        if (dx == 0 && dy == 0) return;
        this.absX += dx;
        this.absY += dy;
        for (Node c : children) c.translate(dx, dy);
    }

    /** 内容区左上角 X（扣掉 padding）。 */
    public int contentX() { return absX + flex.padding.left; }

    /** 内容区左上角 Y（扣掉 padding）。 */
    public int contentY() { return absY + flex.padding.top; }

    public int contentWidth()  { return Math.max(0, width - flex.padding.horizontal()); }

    public int contentHeight() { return Math.max(0, height - flex.padding.vertical()); }

    // ============================ 命中测试 ============================

    public boolean contains(double mx, double my) {
        return visible && mx >= absX && mx < absX + width && my >= absY && my < absY + height;
    }

    /** 命中最深的可见节点，用于 tooltip / 焦点定位。 */
    public Node hitTest(double mx, double my) {
        if (!contains(mx, my)) return null;
        for (int i = children.size() - 1; i >= 0; i--) {
            Node hit = children.get(i).hitTest(mx, my);
            if (hit != null) return hit;
        }
        return this;
    }

    // ============================ 提示气泡 ============================

    /**
     * 悬浮提示内容提供者。
     *
     * <p>不在 render 里直接画 —— tooltip 必须画在整棵树的最上层，否则会被后画的兄弟节点盖住
     * （旧库就是在渲染器里就地画，于是提示总被相邻窗口压住）。这里只登记，
     * 由根节点渲染结束后统一 {@link #collectTooltip} 取出再画。
     */
    public Supplier<List<Component>> tooltip;

    public Node tooltip(Supplier<List<Component>> supplier) { this.tooltip = supplier; return this; }

    public Node tooltip(Component... lines) {
        List<Component> fixed = List.of(lines);
        this.tooltip = () -> fixed;
        return this;
    }

    /**
     * 本节点在给定屏幕坐标处的提示内容。
     *
     * <p>默认就是 {@link #tooltip} 供给器的结果 —— 一个节点一条提示。
     * 内部含有大量「非 Node 图元」的组件（典型是图画布：一个节点里有成千上万个圆点）
     * 覆写它，按坐标自行解析出该显示谁的提示。
     */
    protected List<Component> tooltipAt(double mx, double my) {
        // 1) 节点显式供应器优先（业务自定义提示）
        if (tooltip != null) {
            List<Component> lines = tooltip.get();
            if (lines != null && !lines.isEmpty()) return lines;
        }
        // 2) 渲染器自供提示（典型：ItemRenderer 的物品栈 tooltip）——统一汇入关键字链路，
        //    避免渲染器在 render 里直调原版 setTooltipForNextFrame 绕开 KeywordTooltipHost。
        for (Renderer r : visuals) {
            List<Component> lines = r.hoverTooltip();
            if (lines != null && !lines.isEmpty()) return lines;
        }
        return null;
    }

    /** 从命中点向上找最近的、有内容的提示，返回 null 表示不显示。 */
    public List<Component> collectTooltip(double mx, double my) {
        Node hit = hitTest(mx, my);
        while (hit != null) {
            List<Component> lines = hit.tooltipAt(mx, my);
            if (lines != null && !lines.isEmpty()) return lines;
            hit = hit.parent;
        }
        return null;
    }

    // ============================ 渲染 ============================

    /** 从根调用：算状态、画自己、递归子节点。 */
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        render(g, RenderContext.of(mouseX, mouseY, partialTick), false);
    }

    /**
     * 带上下文的渲染入口。公开而非 protected —— 容器类（如 {@code WindowLayer}）
     * 需要在自定义顺序下逐个渲染任意 {@link Node} 子节点，protected 只允许「对自己这一支」调用。
     */
    public void render(GuiGraphicsExtractor g, RenderContext ctx, boolean parentDisabled) {
        if (!visible) return;

        boolean disabled = parentDisabled || state.disabled();
        VisualState effective = state
                .hovered(contains(ctx.mouseX(), ctx.mouseY()))
                .disabled(disabled)
                .normalizeForInteraction();

        renderSelf(g, ctx.with(effective));
        renderChildren(g, ctx, disabled);
    }

    /** 只画自己的外观层。子类想在自身绘制前后插东西就覆写这个。 */
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        for (Renderer r : visuals) {
            r.render(g, absX, absY, width, height, ctx);
        }
        drawFocusRing(g, ctx);
        if (attachments != null) {
            for (Attachment a : attachments) {
                try {
                    a.render(g, absX, absY, width, height, ctx);
                } catch (RuntimeException ignored) { /* 附件失败静默降级，不阻断渲染 */ }
            }
        }
    }

    /**
     * 键盘焦点可视化：聚焦的节点画一圈强调色描边，使方向键 / 回车等键盘操作
     * 「看得见当前焦点在哪」。焦点态由 {@link #focus()} 设置，组件库里只有真正可交互的
     * 节点（按钮 / 滚动条 / 滑杆 / 列表 / 输入框）会被聚焦，故不会误伤普通容器。
     */
    private void drawFocusRing(GuiGraphicsExtractor g, RenderContext ctx) {
        if (!ctx.state().focused() || width <= 0 || height <= 0) return;
        // 预设感知：静态 Theme.ACCENT 不经过 StylePreset，换肤对它无效
        int ring = Theme.current().focusRing();
        Draw.outline(g, absX, absY, width, height, ring);
        // 第二环内缩 2px 形成「双环」：两环之间留 1px 底色间隙，在深底与浅底上都能看清；
        // 节点太小放不下内环时退化为单环，绝不画负尺寸
        if (width > 4 && height > 4) {
            Draw.outline(g, absX + 2, absY + 2, width - 4, height - 4, ring);
        }
    }

    /** 递归画子节点。滚动容器覆写它来压裁剪区。 */
    protected void renderChildren(GuiGraphicsExtractor g, RenderContext ctx, boolean disabled) {
        for (Node c : children) {
            c.render(g, ctx, disabled);
        }
    }

    /** 推进所有动画渲染器。 */
    public void tick() {
        for (Renderer r : visuals) {
            if (r.isAnimated()) r.tick();
        }
        if (attachments != null) {
            for (Attachment a : attachments) {
                try {
                    a.tick(this);
                } catch (RuntimeException ignored) { /* 附件失败静默降级，不阻断整树 tick */ }
            }
        }
        for (Node c : children) c.tick();
    }

    // ============================ 焦点 ============================

    public Node root() {
        Node n = this;
        while (n.parent != null) n = n.parent;
        return n;
    }

    /**
     * 最近的「拖拽作用域」祖先节点（含自身）：实现 {@link com.cimdy.coe.ui.widget.component.dnd.DragScope}
     * 的容器（窗口 / 滚动容器 / 根层）即作用域。用于把放置区 / 拖拽登记到正确的嵌套注册表。
     */
    public Node findDragScopeNode() {
        Node n = this;
        while (n != null) {
            if (n instanceof com.cimdy.coe.ui.widget.component.dnd.DragScope) return n;
            n = n.parent;
        }
        return null;
    }

    /**
     * 本节点所属作用域的拖拽注册表（最近的 {@link com.cimdy.coe.ui.widget.component.dnd.DragScope}
     * 祖先的注册表）。找不到时返回 {@code null}（调用方应回退到根注册表）。
     */
    public com.cimdy.coe.ui.widget.component.dnd.DragRegistry findDragRegistry() {
        Node s = findDragScopeNode();
        if (s instanceof com.cimdy.coe.ui.widget.component.dnd.DragScope ds) {
            return ds.dragRegistry();
        }
        return null;
    }

    /** 独占焦点：清掉全树其它节点的焦点后聚焦自己。 */
    public void focus() {
        root().clearFocusRecursive();
        this.state = state.focused(true);
    }

    public void blur() {
        this.state = state.focused(false);
    }

    private void clearFocusRecursive() {
        if (state.focused()) state = state.focused(false);
        for (Node c : children) c.clearFocusRecursive();
    }

    public boolean isFocused() { return state.focused(); }

    // ============================ 事件分发 ============================
    // 统一「先子后父、后加的先响应、命中即止」，容器无需写任何转发代码

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isInteractive() || !contains(mx, my)) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseClicked(mx, my, button)) return true;
        }
        return onMouseClicked(mx - absX, my - absY, button);
    }

    public boolean mouseReleased(double mx, double my, int button) {
        if (!isInteractive()) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseReleased(mx, my, button)) return true;
        }
        return onMouseReleased(mx - absX, my - absY, button);
    }

    /** 拖拽不做命中限制 —— 按下后即使鼠标移出组件也应继续响应。 */
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (!isInteractive()) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseDragged(mx, my, button, dx, dy)) return true;
        }
        return onMouseDragged(mx - absX, my - absY, button, dx, dy);
    }

    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (!isInteractive() || !contains(mx, my)) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseScrolled(mx, my, scrollX, scrollY)) return true;
        }
        return onMouseScrolled(mx - absX, my - absY, scrollX, scrollY);
    }

    /** 键盘不走命中，只走焦点：优先交给已聚焦的节点。 */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isInteractive()) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return isFocused() && onKeyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!isInteractive()) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).charTyped(codePoint, modifiers)) return true;
        }
        return isFocused() && onCharTyped(codePoint, modifiers);
    }

    private boolean isInteractive() { return visible && !state.disabled(); }

    // ---- 交互钩子，坐标为相对本节点左上角 ----

    protected boolean onMouseClicked(double lx, double ly, int button) { return false; }
    protected boolean onMouseReleased(double lx, double ly, int button) { return false; }
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) { return false; }
    protected boolean onMouseScrolled(double lx, double ly, double sx, double sy) { return false; }
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    protected boolean onCharTyped(char codePoint, int modifiers) { return false; }

    // ============================ 拷贝 ============================

    /** 深拷贝。子类覆写时应先调用 {@code super.copyInto(target)} 再补自己的字段。 */
    public Node copy() {
        Node n = new Node();
        copyInto(n);
        return n;
    }

    protected void copyInto(Node n) {
        copyPropsInto(n);
        for (Node c : children) n.add(c.copy());
    }

    /** 只复制自身属性与外观层，不碰子树。子类需要自定义子树复制策略时用它。 */
    protected void copyPropsInto(Node n) {
        n.id = this.id;
        n.flex.copyFrom(this.flex);
        n.visible = this.visible;
        n.state = this.state;
        n.tooltip = this.tooltip;
        for (Renderer r : visuals) n.visuals.add(r.copy());
    }
}
