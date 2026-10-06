package com.cimdy.coe.ui.widget.component.panel;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.ColorRendererI;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.dnd.DragRegistry;
import com.cimdy.coe.ui.widget.component.dnd.DragScope;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.BorderStyle;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 窗口：标题栏 + 内容区，可拖动、可关闭，位置由自己说了算。
 *
 * <p>合并了旧库的 {@code AdaptiveWindow} 与 {@code ScrollableWindow} —— 后者只是前者
 * 加了个滚动内容区，却复制了整套窗口代码。这里把「内容区要不要滚动」降级成一个方法
 * {@link #scrollBody()}，不再需要第二个类。
 *
 * <p>窗口是<b>绝对定位</b>的：它不参与父容器的 Flex 排布，位置来自 {@link #position(int, int)}。
 * 内部结构照常用 Flexbox。
 *
 * <p>继承 {@link AnimatedNode}（附录 D.1）：首次以真实尺寸 tick 时播放一次「淡入 + 缩放 0.98→1」
 * 进场动效；未挂动画时 {@link AnimatedNode} 直走 {@code Node} 原渲染路径（零回归）。
 * 纯视觉变换，不改命中区与事件链路。
 *
 * <pre>{@code
 * Window w = Window.of(Component.literal("背包")).styled().closable(true);
 * w.flex.size(180, 120);
 * w.position(20, 20);
 * w.body().add(someContent);
 * }</pre>
 */
public class Window extends AnimatedNode implements DragScope {

    /** 标题栏默认高度。 */
    public static final int TITLE_BAR_HEIGHT = 12;

    private Node titleBar;
    private TextRendererI titleText;
    private Component titleValue = Component.empty();
    private Button closeButton;
    private Node body;
    /** {@code styled()} 装入的底层背景渲染器，便于之后改圆角等配置。 */
    private ColorRendererI windowBg;
    /** 用四角括号边框（旧 ColorBorder 风）替代角点 tick 装饰。 */
    private boolean bracketDecor = false;

    private int posX, posY;
    private boolean draggable = true;
    /** 无标题栏时，是否允许拖窗体任意位置。 */
    private boolean dragAnywhere = false;
    private boolean keepOnScreen = true;
    /** 落在窗口内的点击是否就此吃掉（不再穿透到背后的窗口）。 */
    private boolean consumeClicks = true;

    private Runnable onClose;

    private boolean dragging;

    /** 是否已播过一次进场动效（每个窗口一次；克隆体各自播，不共享）。 */
    private boolean mountAnimated;

    /** 是否已进入退场淡出流程（close 预演中，等待所在 WindowLayer 到点统一拆除）。 */
    private boolean closing;

    /** 本窗口的拖拽作用域注册表（根的子表），惰性创建。 */
    private DragRegistry dragReg;

    // ---- 拖拽缩放 ----
    private boolean resizable = false;
    private int minW = 40, minH = 24;
    private static final int EDGE = 5;
    private enum ResizeMode { NONE, E, S, SE }
    private ResizeMode resizing = ResizeMode.NONE;
    private int resizeW0, resizeH0;
    private int resizeAccumX, resizeAccumY;

    public Window() {
        this.id = "window";
        column();
        body = new Node().column();
        body.id = "window-body";
        body.flex.grow = 1;
        super.add(body);
    }

    public static Window of(Component title) {
        Window w = new Window();
        w.title(title);
        return w;
    }

    public static Window blank() { return new Window(); }

    // ------------------------------------------------------------------
    // 结构
    // ------------------------------------------------------------------

    /** 内容区。往这里加子节点。 */
    public Node body() { return body; }

    /** 把内容区换成滚动容器并返回它（幂等）。 */
    public Scrollable scrollBody() {
        if (body instanceof Scrollable s) return s;
        Scrollable s = new Scrollable();
        s.id = "window-body";
        s.flex.grow = 1;
        for (Node c : java.util.List.copyOf(body.children)) {
            body.remove(c);
            s.add(c);
        }
        super.remove(body);
        body = s;
        super.add(s);
        return s;
    }

    /** 子节点默认加进内容区，不会撞到标题栏。 */
    @Override
    public Node add(Node child) { body.add(child); return this; }

    /**
     * 直接挂到窗口骨架上（与标题栏、内容区平级），不进内容区。
     * 用于底部动作栏、状态栏这类「不该跟着内容滚动」的部件。
     */
    protected Node addChrome(Node child) {
        super.add(child);
        return child;
    }

    public boolean hasTitleBar() { return titleBar != null && titleBar.visible; }

    @Override
    public Node remove(Node child) { body.remove(child); return this; }

    @Override
    public Node clearChildren() { body.clearChildren(); return this; }

    public Window title(Component text) {
        ensureTitleBar();
        this.titleValue = text == null ? Component.empty() : text;
        titleText.setLine(0, titleValue);
        return this;
    }

    public Component title() { return titleValue; }

    public Node titleBar() { ensureTitleBar(); return titleBar; }

    private void ensureTitleBar() {
        if (titleBar != null) return;
        titleBar = new Node().row();
        titleBar.id = "window-title";
        titleBar.flex.height = TITLE_BAR_HEIGHT;
        titleBar.flex.alignItems = AlignItems.CENTER;
        titleBar.padding(0, 3);

        titleText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE);
        Node label = new Node().with(titleText);
        label.flex.grow = 1;
        titleBar.add(label);

        // 标题栏必须排在内容区前面
        children.addFirst(titleBar);
        titleBar.parent = this;
    }

    public Window closable(boolean b) {
        if (b) {
            ensureTitleBar();
            if (closeButton == null) {
                closeButton = Button.of(Component.literal("x"));
                closeButton.id = "window-close";
                closeButton.flex.size(TITLE_BAR_HEIGHT - 2, TITLE_BAR_HEIGHT - 2);
                closeButton.onClick(this::close);
                if (closeButton.labelRenderer() != null) {
                    closeButton.labelRenderer()
                            .color(Theme.current().textMuted())
                            .colorHover(Theme.current().textDanger());
                }
                titleBar.add(closeButton);
            }
            closeButton.visible = true;
        } else if (closeButton != null) {
            closeButton.visible = false;
        }
        return this;
    }

    public Button closeButton() { return closeButton; }

    public Window onClose(Runnable r) { this.onClose = r; return this; }

    public void close() {
        if (closing) return; // 已在退场淡出流程中，幂等（避免重复触发 onClose / 重复注册拆除队列）
        if (onClose != null) onClose.run();
        // 仅当父节点是 WindowLayer 时才分离；挂在其他节点（或在层外手动渲染）时不触碰。
        if (parent instanceof WindowLayer wl) {
            int dur = AnimRegistry.ms(AnimRegistry.Trigger.UNMOUNT);
            if (AnimRegistry.animationsActive() && dur > 0) {
                // close 预演（D.1：Window close 淡出）：先播 alpha 1→0 淡出，到点再由层统一拆除。
                // 淡出期间窗口仍留在 children 且 visible=true（Node.render/contains/isInteractive 均以 visible
                // 为闸门），故输入命中窗口多活 dur ms（≈90ms，标准关窗动画行为，肉眼无感）；纯视觉 alpha，
                // 不改坐标/命中。实际拆除推迟到层的 tick 在「子节点迭代结束后」统一执行，避免迭代中 remove 触发 CME。
                closing = true;
                visible = true; // closeModal 可能已先置 false，淡出期间需保持渲染
                fadeOut(dur);
                wl.beginCloseFade(this, dur);
            } else {
                // 无动画 / reduced-motion：与原行为完全一致——瞬时拆除（静止终帧）。
                visible = false;
                wl.removeWindow(this);
            }
        } else {
            visible = false;
        }
    }

    public void open() { visible = true; }

    // ------------------------------------------------------------------
    // 进场动效（D.1：open 淡入 + 缩放 0.98→1）
    // ------------------------------------------------------------------

    /**
     * 首次以真实尺寸 tick 时播放一次进场动效：alpha 0→1 + scale 0.98→1（时长取
     * {@code AnimRegistry.ms(MOUNT)}，动画关闭 / reduced-motion 时瞬时落到终帧）。
     * 只触发一次；纯视觉变换，不改命中区。{@link #close()} 走「close 预演」：先 fadeOut(UNMOUNT)
     * 再由层到点统一拆除（动画关闭时退化为原瞬时拆除）。
     */
    @Override
    public void tick() {
        if (!mountAnimated && visible && width > 0) {
            mountAnimated = true;
            if (AnimRegistry.animationsActive()) {
                anim().alpha = 0F;
                anim().scale = AnimRegistry.openStartScale();
                fadeIn(AnimRegistry.ms(AnimRegistry.Trigger.MOUNT));
                scaleTo(1F, AnimRegistry.ms(AnimRegistry.Trigger.MOUNT));
            }
        }
        super.tick();
    }

    // ------------------------------------------------------------------
    // 外观
    // ------------------------------------------------------------------

    /** 中性默认皮肤：不透明实色底（圆角）+ 投影 + 四角装饰 + 标题栏分隔条。 */
    public Window styled() {
        ColorRendererI bg = Theme.window();
        this.windowBg = bg;
        if (bracketDecor) bg.borderStyle(BorderStyle.BRACKETS);
        visuals.addFirst(bg);
        visuals.addFirst(Theme.shadow());          // 阴影垫在最底，窗口浮起
        with(Theme.backgroundTexture());           // D.2 皮肤背景纹理（画在底色之上、内容之下）
        if (!bracketDecor) with(Renderers.cornerTicks(2, 4, Theme.current().cornerTicks()));
        if (titleBar != null) {
            titleBar.visuals.addFirst(Renderers.color(Theme.current().titleBarBg()));
        }
        return this;
    }

    /** 取底层背景渲染器（未 {@code styled()} 时为 null）。 */
    public ColorRendererI bgRenderer() { return windowBg; }

    /** 圆角半径（px）。 */
    public Window rounding(int r) { if (windowBg != null) windowBg.rounding(r); return this; }

    /** 叠加一层柔和投影（默认 {@code styled()} 已挂，这里用于自定义投影参数）。 */
    public Window shadow() { visuals.addFirst(Theme.shadow()); return this; }

    /** 叠加一层脉冲发光边框（动态效果，如聚焦窗口强调）。 */
    public Window glow() { with(Theme.glow()); return this; }

    public Window background(Renderer r) {
        visuals.addFirst(r);
        return this;
    }

    // ------------------------------------------------------------------
    // 位置与布局
    // ------------------------------------------------------------------

    public Window position(int x, int y) {
        this.posX = x;
        this.posY = y;
        return this;
    }

    public int posX() { return posX; }
    public int posY() { return posY; }

    /** 在给定区域内居中。 */
    public Window center(int areaW, int areaH) {
        int w = flex.width > 0 ? flex.width : width;
        int h = flex.height > 0 ? flex.height : height;
        return position((areaW - w) / 2, (areaH - h) / 2);
    }

    /**
     * 在当前游戏窗口内居中（尺寸取自实时游戏窗口，等价于 {@code center(this.width, this.height)}）。
     * 屏幕只需写 {@code window.center();} 即可，省掉「(this.width - W) / 2」这类重复居中数学。
     */
    public Window center() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return this;
        return center(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    /** 按自己的 posX/posY 与声明尺寸完成一次布局。窗口层每帧调它。 */
    public void layoutSelf() {
        int w = flex.width > 0 ? flex.width : width;
        int h = flex.height > 0 ? flex.height : height;
        layout(posX, posY, Math.max(0, w), Math.max(0, h));
    }

    public Window draggable(boolean b) { this.draggable = b; return this; }

    /**
     * 本窗口的拖拽作用域注册表：惰性创建一个挂在根（{@code WindowLayer}）注册表下的子表。
     * 窗口内登记的放置区 / 拖拽命中优先于外层，实现「嵌套 DragRegistry」。
     * 若窗口尚未挂到 {@code WindowLayer} 上则返回 {@code null}（登记时调用方会回退到根注册表）。
     */
    @Override
    public DragRegistry dragRegistry() {
        if (dragReg == null) {
            Node r = root();
            if (!(r instanceof WindowLayer wl)) return null;
            dragReg = wl.dragRegistry().createChild(this);
        }
        return dragReg;
    }

    /** 没有标题栏时允许拖窗体任意位置。 */
    public Window dragAnywhere(boolean b) { this.dragAnywhere = b; return this; }

    public Window keepOnScreen(boolean b) { this.keepOnScreen = b; return this; }

    public Window consumeClicks(boolean b) { this.consumeClicks = b; return this; }

    /** 是否允许拖拽右/下缘与右下角缩放窗口（与拖拽移动互不影响）。 */
    public Window resizable(boolean b) { this.resizable = b; return this; }

    // ------------------------------------------------------------------
    // 拖拽
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return consumeClicks;
        // 先判缩放热区：命中就抢走事件，进入缩放；不和新窗口拖拽冲突
        ResizeMode m = resizeModeAt(lx, ly);
        if (m != ResizeMode.NONE) {
            resizing = m;
            resizeW0 = Math.max(0, width);
            resizeH0 = Math.max(0, height);
            resizeAccumX = 0;
            resizeAccumY = 0;
            return true;
        }
        if (draggable && inDragHandle(lx, ly)) {
            dragging = true;
            return true;
        }
        return consumeClicks;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (resizing != ResizeMode.NONE) {
            resizeAccumX += (int) Math.round(dx);
            resizeAccumY += (int) Math.round(dy);
            int w = resizeW0, h = resizeH0;
            if (resizing == ResizeMode.E || resizing == ResizeMode.SE) w = resizeW0 + resizeAccumX;
            if (resizing == ResizeMode.S || resizing == ResizeMode.SE) h = resizeH0 + resizeAccumY;
            w = Math.max(minW, w);
            h = Math.max(minH, h);
            flex.width = w;
            flex.height = h;
            layoutSelf(); // 立即重排，当帧生效，避免拖拽慢一帧
            return true;
        }
        if (dragging) {
            moveBy((int) Math.round(dx), (int) Math.round(dy));
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (resizing != ResizeMode.NONE) {
            resizing = ResizeMode.NONE;
            return true;
        }
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    /** 本地坐标落在哪条缩放热区（右缘 / 下缘 / 右下角）。 */
    private ResizeMode resizeModeAt(double lx, double ly) {
        if (!resizable) return ResizeMode.NONE;
        int w = Math.max(0, width), h = Math.max(0, height);
        // 标题栏整行（含关闭按钮）不触发缩放，否则右上角会被缩放热区抢走点击，
        // 且那一行本就是拖拽手柄，不该同时承担缩放。
        if (titleBar != null && titleBar.visible && ly < titleBar.height) return ResizeMode.NONE;
        boolean onRight  = lx >= w - EDGE && lx <= w;
        boolean onBottom = ly >= h - EDGE && ly <= h;
        if (onRight && onBottom) return ResizeMode.SE;
        if (onRight)  return ResizeMode.E;
        if (onBottom) return ResizeMode.S;
        return ResizeMode.NONE;
    }

    /** 立即平移窗口（同步更新逻辑位置与已算好的绝对坐标，拖拽才不会慢一帧）。 */
    public void moveBy(int dx, int dy) {
        int oldX = posX, oldY = posY;
        posX += dx;
        posY += dy;
        if (keepOnScreen) clampOnScreen();
        translate(posX - oldX, posY - oldY);
    }

    private void clampOnScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int w = Math.max(1, width), h = Math.max(1, height);
        // 至少留一条标题栏在屏幕内，避免窗口被拖到再也点不到的地方
        int keep = Math.max(TITLE_BAR_HEIGHT, 8);
        posX = Math.max(keep - w, Math.min(sw - keep, posX));
        posY = Math.max(0, Math.min(sh - keep, posY));
    }

    private boolean inDragHandle(double lx, double ly) {
        if (titleBar != null && titleBar.visible) {
            return ly >= 0 && ly < titleBar.height;
        }
        return dragAnywhere;
    }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        Window w = new Window();
        copyPropsInto(w);
        w.posX = posX; w.posY = posY;
        w.draggable = draggable;
        w.dragAnywhere = dragAnywhere;
        w.keepOnScreen = keepOnScreen;
        w.consumeClicks = consumeClicks;
        w.resizable = resizable;
        w.minW = minW;
        w.minH = minH;
        w.onClose = onClose;
        if (titleBar != null) w.title(titleValue);
        if (closeButton != null) w.closable(closeButton.visible);
        for (Node c : body.children) w.body.add(c.copy());
        return w;
    }
}
