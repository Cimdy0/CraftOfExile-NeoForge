package com.cimdy.coe.ui.widget.component.base;

import com.cimdy.coe.ui.widget.component.dnd.DragRegistry;
import com.cimdy.coe.ui.widget.component.dnd.DragScope;
import com.cimdy.coe.ui.widget.component.list.ScrollBar;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.core.FlexNode;
import com.cimdy.coe.ui.widget.flex.core.FlexStyle;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.component.util.MathUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

/**
 * 滚动容器：一个固定大小的视口 + 一棵可以比它大的内容子树。
 *
 * <h3>为什么要合并重写</h3>
 * 旧库有三份滚动实现（{@code ListableWidget}、{@code ScrollableWindow}、{@code AdaptiveScroll}），
 * 而且<b>滚动量的单位三处都不一样</b>：一处按像素累加、一处按 0~1 比例、
 * 数据键 {@code OFFSET} 又按 0~1 读取。于是同一份数据在不同组件里含义不同，
 * 换个容器滚动距离就错，滚动条位置也对不上。
 *
 * <h3>这里的单位约定（唯一）</h3>
 * <ul>
 *   <li><b>像素是权威单位</b>：{@link #offsetY()} 表示「内容顶端相对视口顶端向上移动了多少像素」，
 *       取值恒为 {@code [0, maxOffsetY()]}；</li>
 *   <li><b>归一化是派生的只读视图</b>：{@link #progressY()} = offset / maxOffset，
 *       只给滚动条这类「按比例摆位置」的消费者用，不作为内部状态存储。</li>
 * </ul>
 *
 * <h3>布局策略</h3>
 * 视口对父级而言是<b>叶子</b>（内容再高也不会把父容器撑开），内容子树用「视口宽 × 无限高」
 * 单独排一次版，然后整体平移到偏移位置。这样内容里照样能用完整 Flexbox。
 */
public class Scrollable extends Node implements DragScope {

    public enum Axis { VERTICAL, HORIZONTAL, BOTH }

    /** 「无限」可用空间。取 2^20 而非 MAX_VALUE，避免下游 padding 相加溢出。 */
    private static final int UNBOUNDED = 1 << 20;

    /**
     * 内容容器。{@code add(...)} 会自动落到这里，一般不用直接碰它；
     * 需要给内容整体设 padding/gap/direction 时用 {@code content.flex}。
     */
    public final Node content = new Node();

    private Axis axis = Axis.VERTICAL;
    private double offsetX, offsetY;
    private int contentW, contentH;
    private int wheelStep = 16;
    private boolean clip = true;

    /** 是否绘制内置滚动条（免去了「手动 new ScrollBar + 定位」的重复接线）。 */
    private boolean showScrollBar = false;
    private ScrollBar.Orientation sbOrientation = ScrollBar.Orientation.VERTICAL;
    private ScrollBar scrollBarNode;
    /** 为滚动条预留的槽位宽度/高度（不参与内容区）。 */
    private int gutterW = 0, gutterH = 0;
    private static final int SB_THICKNESS = 4;

    /** 本滚动容器的拖拽作用域注册表（父作用域 = 最近 DragScope 祖先），惰性创建。 */
    private DragRegistry dragReg;

    public Scrollable() {
        this.id = "scrollable";
        content.id = "scroll-content";
        content.column();
        super.add(content);
    }

    public static Scrollable vertical()   { return new Scrollable().axis(Axis.VERTICAL); }
    public static Scrollable horizontal() { return new Scrollable().axis(Axis.HORIZONTAL).row(); }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public Scrollable axis(Axis a) { this.axis = a; return this; }

    public Axis axis() { return axis; }

    /** 滚轮一格滚动的像素数。 */
    public Scrollable wheelStep(int px) { this.wheelStep = Math.max(1, px); return this; }

    /** 是否裁剪溢出内容（关掉可以做「溢出可见」的浮层）。 */
    public Scrollable clip(boolean b) { this.clip = b; return this; }

    /**
     * 开关内置滚动条。打开后在对应滚动轴旁自动预留槽位、定位并绘制滚动条，
     * 拖拽/点轨道直接驱动本容器 —— 业务侧不必再 {@code new ScrollBar(target)} 并手动摆放。
     */
    public Scrollable scrollBar(boolean b) {
        if (b && scrollBarNode == null) {
            ScrollBar.Orientation o = axis == Axis.HORIZONTAL
                    ? ScrollBar.Orientation.HORIZONTAL : ScrollBar.Orientation.VERTICAL;
            sbOrientation = o;
            scrollBarNode = new ScrollBar(this, o).styled();
            super.add(scrollBarNode); // 直接挂为子节点，不进 content
        }
        this.showScrollBar = b;
        if (!b && scrollBarNode != null) {
            super.remove(scrollBarNode);
            scrollBarNode = null;
        }
        return this;
    }

    /** 指定滚动条方向并开启；会顺带把滚动轴对齐到该方向。 */
    public Scrollable scrollBar(ScrollBar.Orientation o) {
        this.axis = (o == ScrollBar.Orientation.HORIZONTAL) ? Axis.HORIZONTAL : Axis.VERTICAL;
        return scrollBar(true);
    }

    /** 内容区宽度：扣掉 padding 与为滚动条预留的槽位。 */
    @Override
    public int contentWidth()  { return Math.max(0, width - flex.padding.horizontal() - gutterW); }

    /** 内容区高度：扣掉 padding 与为滚动条预留的槽位。 */
    @Override
    public int contentHeight() { return Math.max(0, height - flex.padding.vertical() - gutterH); }

    /** 内容排列方向，等价于 {@code content.flex.direction}。 */
    public Scrollable row()    { content.row(); return this; }
    public Scrollable column() { content.column(); return this; }

    /** 内容项之间的间距。 */
    public Scrollable itemGap(int px) { content.gap(px); return this; }

    /** 内容四周留白。注意这是内容的 padding，不是视口的。 */
    public Scrollable contentPadding(int all) { content.padding(all); return this; }

    // ------------------------------------------------------------------
    // 子节点：一律落进 content
    // ------------------------------------------------------------------

    @Override
    public Node add(Node child) { content.add(child); return this; }

    @Override
    public Node remove(Node child) { content.remove(child); return this; }

    @Override
    public Node clearChildren() { content.clearChildren(); return this; }

    // ------------------------------------------------------------------
    // 布局
    // ------------------------------------------------------------------

    @Override
    protected FlexNode toFlex() {
        // 对父级伪装成叶子：内容多长都不参与父容器的尺寸计算
        FlexNode fn = new FlexNode();
        fn.style.copyFrom(this.flex);
        return fn;
    }

    @Override
    protected void applyFlex(FlexNode fn, int baseX, int baseY) {
        super.applyFlex(fn, baseX, baseY); // fn 无子节点，只会写自身坐标
        layoutContent();
    }

    /** 重排内容子树。增删内容项后若不想等下一帧整树布局，可手动调用。 */
    public void layoutContent() {
        // 先按当前开关算出滚动条槽位，再据此收窄内容区
        gutterW = 0;
        gutterH = 0;
        if (showScrollBar && scrollBarNode != null) {
            if (sbOrientation == ScrollBar.Orientation.VERTICAL) gutterW = SB_THICKNESS + 1;
            else                                                 gutterH = SB_THICKNESS + 1;
        }

        int vw = contentWidth();
        int vh = contentHeight();
        if (vw <= 0 || vh <= 0) {
            contentW = contentH = 0;
            return;
        }

        boolean scrollV = axis != Axis.HORIZONTAL;
        boolean scrollH = axis != Axis.VERTICAL;

        // 不滚动的那个轴锁死为视口尺寸，否则内容会缩成「最宽子项」的宽度
        content.flex.width  = scrollH ? FlexStyle.SIZE_UNSPECIFIED : vw;
        content.flex.height = scrollV ? FlexStyle.SIZE_UNSPECIFIED : vh;

        content.layout(contentX(), contentY(),
                scrollH ? UNBOUNDED : vw,
                scrollV ? UNBOUNDED : vh);

        contentW = content.width;
        contentH = content.height;

        clampOffsets();
        content.translate(-(int) Math.round(offsetX), -(int) Math.round(offsetY));

        positionScrollBar();
    }

    /** 把内置滚动条摆到预留槽位里（每帧布局后调用）。 */
    private void positionScrollBar() {
        if (!showScrollBar || scrollBarNode == null) return;
        int thick = SB_THICKNESS;
        if (sbOrientation == ScrollBar.Orientation.VERTICAL) {
            scrollBarNode.flex.width = thick;
            scrollBarNode.absX = absX + width - flex.padding.right - thick;
            scrollBarNode.absY = contentY();
            scrollBarNode.width = thick;
            scrollBarNode.height = contentHeight();
        } else {
            scrollBarNode.flex.height = thick;
            scrollBarNode.absX = contentX();
            scrollBarNode.absY = absY + height - flex.padding.bottom - thick;
            scrollBarNode.width = contentWidth();
            scrollBarNode.height = thick;
        }
    }

    // ------------------------------------------------------------------
    // 滚动量（像素，权威）
    // ------------------------------------------------------------------

    public double offsetX() { return offsetX; }
    public double offsetY() { return offsetY; }

    public int maxOffsetX() { return Math.max(0, contentW - contentWidth()); }
    public int maxOffsetY() { return Math.max(0, contentH - contentHeight()); }

    public boolean canScrollX() { return maxOffsetX() > 0; }
    public boolean canScrollY() { return maxOffsetY() > 0; }

    /** 滚到指定像素位置（自动夹取范围），内容立即跟着平移。 */
    public Scrollable scrollToX(double px) {
        long before = Math.round(offsetX);
        offsetX = MathUtil.clamp(px, 0, maxOffsetX());
        int d = (int) (Math.round(offsetX) - before);
        if (d != 0) content.translate(-d, 0);
        return this;
    }

    public Scrollable scrollToY(double px) {
        long before = Math.round(offsetY);
        offsetY = MathUtil.clamp(px, 0, maxOffsetY());
        int d = (int) (Math.round(offsetY) - before);
        if (d != 0) content.translate(0, -d);
        return this;
    }

    public Scrollable scrollByX(double px) { return scrollToX(offsetX + px); }
    public Scrollable scrollByY(double px) { return scrollToY(offsetY + px); }

    /**
     * 把偏移夹回合法范围。内容变短（比如列表过滤掉了一批行）后必须调一次，
     * 否则会停在「内容已经没那么长了，却还滚在下面」的空白区。
     */
    private void clampOffsets() {
        offsetX = MathUtil.clamp(offsetX, 0, maxOffsetX());
        offsetY = MathUtil.clamp(offsetY, 0, maxOffsetY());
    }

    public Scrollable scrollToBottom() { return scrollToY(maxOffsetY()); }

    // ------------------------------------------------------------------
    // 归一化视图（派生，只读语义）
    // ------------------------------------------------------------------

    public double progressX() { int m = maxOffsetX(); return m <= 0 ? 0 : offsetX / m; }
    public double progressY() { int m = maxOffsetY(); return m <= 0 ? 0 : offsetY / m; }

    public Scrollable progressX(double p) { return scrollToX(MathUtil.clamp(p, 0, 1) * maxOffsetX()); }
    public Scrollable progressY(double p) { return scrollToY(MathUtil.clamp(p, 0, 1) * maxOffsetY()); }

    /** 可视内容占总内容的比例，滚动条据此决定滑块长度。 */
    public double visibleRatioX() {
        return contentW <= 0 ? 1 : Math.min(1.0, (double) contentWidth() / contentW);
    }

    public double visibleRatioY() {
        return contentH <= 0 ? 1 : Math.min(1.0, (double) contentHeight() / contentH);
    }

    /** 把某个子节点滚进可视区（最小移动量）。 */
    public void scrollIntoView(Node target) {
        if (target == null) return;
        int vy1 = contentY(), vy2 = vy1 + contentHeight();
        if (target.absY < vy1) {
            scrollByY(target.absY - vy1);
        } else if (target.absY + target.height > vy2) {
            scrollByY(target.absY + target.height - vy2);
        }
        int vx1 = contentX(), vx2 = vx1 + contentWidth();
        if (target.absX < vx1) {
            scrollByX(target.absX - vx1);
        } else if (target.absX + target.width > vx2) {
            scrollByX(target.absX + target.width - vx2);
        }
    }

    /**
     * 本滚动容器的拖拽作用域注册表：惰性创建一个子表，其父指针指向<b>最近 DragScope 祖先</b>
     * （所在窗口或根层，而非自身），使容器内登记的放置区命中优先于外层。
     * 若尚未挂到 {@code WindowLayer} 链上则返回 {@code null}（登记时调用方回退到根注册表）。
     */
    @Override
    public DragRegistry dragRegistry() {
        if (dragReg == null) {
            DragRegistry parentReg = null;
            Node n = parent;
            while (n != null) {
                if (n instanceof DragScope ds) { parentReg = ds.dragRegistry(); break; }
                n = n.parent;
            }
            if (parentReg == null) {
                Node r = root();
                if (!(r instanceof WindowLayer wl)) return null;
                parentReg = wl.dragRegistry();
            }
            dragReg = parentReg.createChild(this);
        }
        return dragReg;
    }

    // ------------------------------------------------------------------
    // 交互
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseScrolled(double lx, double ly, double sx, double sy) {
        // 横向容器上滚竖直滚轮，按惯例也应该横向滚动
        if (axis == Axis.HORIZONTAL) {
            double delta = sx != 0 ? sx : sy;
            if (delta == 0 || !canScrollX()) return false;
            scrollByX(-delta * wheelStep);
            return true;
        }

        boolean handled = false;
        if (sy != 0 && canScrollY()) {
            scrollByY(-sy * wheelStep);
            handled = true;
        }
        if (axis == Axis.BOTH && sx != 0 && canScrollX()) {
            scrollByX(-sx * wheelStep);
            handled = true;
        }
        return handled;
    }

    /**
     * 点内容区（空白处）即抢占焦点，使之后方向键能滚动本容器。
     * 命中 Pressable 子节点（如列表行、输入框）时由子节点优先处理，不会抢走它们的焦点。
     */
    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button == 0) focus();
        return false; // 不吞事件，让命中判定的子节点照常响应
    }

    /**
     * 键盘滚动：本容器聚焦时，方向键翻一行、PageUp/Down 翻一屏、Home/End 跳首尾。
     * 纵向容器认上/下，横向容器认左/右；双向容器四向都响应。
     * 若内置滚动条已抢占焦点，会由它先处理，这里不会重复滚动。
     */
    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        boolean v = axis != Axis.HORIZONTAL;
        boolean h = axis != Axis.VERTICAL;
        if (!v && !h) return false;
        int line = Math.max(1, wheelStep);
        int pageV = Math.max(line, (int) (contentHeight() * 0.9));
        int pageH = Math.max(line, (int) (contentWidth() * 0.9));
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:    if (v) scrollByY(-line); return v;
            case GLFW.GLFW_KEY_DOWN:  if (v) scrollByY( line); return v;
            case GLFW.GLFW_KEY_LEFT:  if (h) scrollByX(-line); return h;
            case GLFW.GLFW_KEY_RIGHT: if (h) scrollByX( line); return h;
            case GLFW.GLFW_KEY_PAGE_UP:
                if (v) scrollByY(-pageV); else if (h) scrollByX(-pageH);
                return v || h;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                if (v) scrollByY( pageV); else if (h) scrollByX( pageH);
                return v || h;
            case GLFW.GLFW_KEY_HOME:
                if (v) scrollToY(0); else if (h) scrollToX(0);
                return v || h;
            case GLFW.GLFW_KEY_END:
                if (v) scrollToBottom(); else if (h) scrollToX(maxOffsetX());
                return v || h;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    protected void renderChildren(GuiGraphicsExtractor g, RenderContext ctx, boolean disabled) {
        int x = contentX(), y = contentY(), w = contentWidth(), h = contentHeight();
        if (w > 0 && h > 0) {
            // 鼠标不在视口里时，下传一个「无鼠标」的上下文 ——
            // 否则被裁掉的内容项照样会算出 hover 高亮（旧库的经典 bug）
            RenderContext inner = Draw.inside(ctx.mouseX(), ctx.mouseY(), x, y, w, h)
                    ? ctx : ctx.withoutMouse();
            if (clip) g.enableScissor(x, y, x + w, y + h);
            try {
                content.render(g, inner, disabled);
            } finally {
                if (clip) g.disableScissor();
            }
        }
        // 滚动条画在内容裁剪区之外，避免被视口 scissor 切掉
        if (showScrollBar && scrollBarNode != null && scrollBarNode.visible) {
            scrollBarNode.render(g, ctx, disabled);
        }
    }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        Scrollable s = new Scrollable();
        copyInto(s);
        return s;
    }

    @Override
    protected void copyInto(Node n) {
        copyPropsInto(n);
        // children 里唯一的元素是内容容器本身，不能走父类的通用复制
        if (n instanceof Scrollable s) {
            s.axis = axis;
            s.wheelStep = wheelStep;
            s.clip = clip;
            s.content.flex.copyFrom(content.flex);
            for (Node c : content.children) s.content.add(c.copy());
            // 滚动条是运行期装饰，按原样在新实例上重建
            if (showScrollBar && scrollBarNode != null) s.scrollBar(sbOrientation);
        }
    }
}
