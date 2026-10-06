package com.cimdy.coe.ui.widget.component.panel;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.HoverLerp;
import com.cimdy.coe.ui.widget.component.dnd.DragRegistry;
import com.cimdy.coe.ui.widget.component.dnd.DragScope;
import com.cimdy.coe.ui.widget.component.dnd.GhostOverlay;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.keyword.KeywordTooltipHost;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 窗口层：Screen 的唯一 UI 根节点。管 z 序、置顶、模态与提示气泡。
 *
 * <p>旧 {@code WindowGroup} 只是「一个窗口列表 + 挨个转发事件」，缺了三件关键能力，
 * 这三件恰好是多窗口界面最容易出 bug 的地方：
 *
 * <ul>
 *   <li><b>z 序与置顶</b>：旧库按添加顺序渲染，点后面的窗口不会浮到前面，
 *       于是重叠时永远只能操作最后添加的那个；</li>
 *   <li><b>模态独占</b>：旧 {@code ModalDialog} 弹出后，背后的窗口照样能点，
 *       甚至能把对话框拖到看不见的地方；</li>
 *   <li><b>提示气泡分层</b>：旧库在渲染器里就地画 tooltip，必然被后画的窗口盖住。
 *       这里等所有窗口画完，再从命中链上取提示统一画在最上层。</li>
 * </ul>
 *
 * <p>{@code children} 的顺序<b>就是</b> z 序：越靠后越上层，与渲染、命中的遍历方向天然一致。
 */
public class WindowLayer extends Node implements DragScope {

    /** 模态遮罩色，0 表示不画遮罩。 */
    private int scrimColor = Theme.current().scrim();
    private Node modal;
    private boolean closeModalOnOutsideClick = true;
    private boolean closeModalOnEscape = true;
    private Runnable onModalClosed;

    private boolean tooltipsEnabled = true;

    /** 正在播放退场淡出的窗口（close 预演）：淡出期间仍留在 children 渲染，到点后在 tick 统一拆除。 */
    private final List<CloseEntry> closing = new ArrayList<>();

    /** tooltip 悬停淡入量（hover 通道 retarget，不累积占 ≤64 预算）；动画关闭时 amount() 瞬到 1。 */
    private final HoverLerp tooltipFade = new HoverLerp(AnimRegistry.ms(AnimRegistry.Trigger.HOVER));

    /** close 预演条目：窗口 + 实际拆除的截止时刻（Util.getMillis()）。 */
    private record CloseEntry(Window window, long deadlineMs) {}

    /** 关键字 tooltip 宿主：数据驱动的关键字系统挂载点（覆盖全 UI 树命中）。 */
    private final KeywordTooltipHost kwHost = new KeywordTooltipHost(this);

    /** 拖拽注册表：登记 DropZone + 持有当前激活拖拽，供 GhostOverlay 绘制（additive，不改动事件流）。 */
    private final DragRegistry dragRegistry = new DragRegistry();

    /** 残影图层：不参与命中（contains 恒 false），在 renderChildren 末尾绘制，永远压在窗口之上。 */
    private final GhostOverlay ghostOverlay;

    public WindowLayer() {
        this.id = "window-layer";
        // 残影图层：永远在最上层绘制，不参与命中（contains 恒 false），不干扰现有事件 / tooltip 命中
        ghostOverlay = new GhostOverlay(dragRegistry);
        add(ghostOverlay);
    }

    /** 暴露关键字宿主，便于业务侧注入自定义数据或查询。 */
    public KeywordTooltipHost keywordHost() { return kwHost; }

    /** 拖拽注册表：业务侧登记 DropZone、查询当前激活拖拽用。 */
    public DragRegistry dragRegistry() { return dragRegistry; }

    // ------------------------------------------------------------------
    // 布局：每个子节点自己决定位置
    // ------------------------------------------------------------------

    /** Screen 每帧调用：把层铺满屏幕，再让各窗口按自己的坐标布局。 */
    public void layoutAll(int screenW, int screenH) {
        this.absX = 0;
        this.absY = 0;
        this.width = screenW;
        this.height = screenH;
        for (Node c : children) {
            if (!c.visible) continue;
            if (c instanceof Window w) {
                w.layoutSelf();
            } else {
                // 非窗口子节点当作铺满全屏的浮层
                c.layout(0, 0, screenW, screenH);
            }
        }
    }

    // ------------------------------------------------------------------
    // z 序
    // ------------------------------------------------------------------

    /** 置顶。 */
    public void bringToFront(Node node) {
        if (node == null || node.parent != this) return;
        int i = children.indexOf(node);
        if (i < 0 || i == children.size() - 1) return;
        children.remove(i);
        children.add(node);
    }

    // ------------------------------------------------------------------
    // 移除：残影图层（ghostOverlay）必须永远留在 children 里，任何移除路径都不得清掉它
    // ------------------------------------------------------------------

    @Override
    public Node remove(Node child) {
        if (child == ghostOverlay) return this; // 残影层永不可被移除
        if (child == modal) modal = null;
        return super.remove(child);
    }

    @Override
    public Node clearChildren() {
        // 清空顶层节点重建 UI 时（WidgetHost.clear → layer.clearChildren），
        // 残影图层必须重新挂回，否则拖拽残影将永久失效。
        super.clearChildren();
        add(ghostOverlay);
        modal = null; // 被清掉的窗口若正是对话中的模态节点，一并丢弃引用，避免 hasModal() 误判锁死输入
        return this;
    }

    // ------------------------------------------------------------------
    // 模态
    // ------------------------------------------------------------------

    /**
     * 以模态方式打开一个节点：置顶、独占输入、背后变暗。
     * 关闭前所有其它节点都收不到任何鼠标/键盘事件。
     */
    public void openModal(Node node) {
        if (node == null) return;
        if (node.parent != this) add(node);
        node.visible = true;
        bringToFront(node);
        modal = node;
    }

    public void closeModal() {
        if (modal == null) return;
        Node m = modal;
        modal = null;
        m.visible = false;
        if (m instanceof Window w) {
            w.close();
        }
        if (onModalClosed != null) onModalClosed.run();
    }

    /**
     * 从层里移除一个窗口（或任意子节点），避免「打开后关闭」的窗口永远留在 {@code children} 中累积。
     * 这是修复内存无界增长的核心：关闭窗口时调用它把节点真正分离出去。
     *
     * <p>永不会移除 {@link #ghostOverlay}（构造函数在第 60 行添加，必须一直留在 children 里）。
     * 若该节点正是对话中的模态节点，会一并清空 {@code modal} 引用，保证事件/遮罩语义不残留。
     */
    public void removeWindow(Node node) {
        if (node == null || node == ghostOverlay) return;
        if (node == modal) modal = null;
        remove(node);
    }

    /**
     * close 预演登记：窗口正在播退场淡出（alpha→0），到 {@code durMs} 后由本层在
     * {@link #renderLayer} 的「渲染完成后」统一 {@link #removeWindow}。
     *
     * <p>行为变化（D.1 close 淡出的代价）：淡出期间窗口仍留在 children 且 {@code visible=true}，
     * 故 {@code contains}/{@code isInteractive} 仍成立——输入命中窗口多活 {@code durMs}（≈90ms）。
     * 推进与拆除刻意落在每帧必经的 {@code renderLayer}（而非 {@code tick}）：部分宿主
     * （如 NodeGraphController）只驱动渲染、不驱动 tick，这样无论宿主是否调 tick 淡出都能跑完并自拆。
     */
    public void beginCloseFade(Window w, int durMs) {
        closing.add(new CloseEntry(w, Util.getMillis() + Math.max(1, durMs)));
    }

    /**
     * 推进正在退场窗口的补间（让 {@code anim.alpha} 每帧更新，淡出真的可见）。
     * 只调 {@code tick()} 采样同一把时钟，不改 children；即便宿主不驱动 {@code layer.tick()} 也生效。
     */
    private void pumpClosing() {
        for (CloseEntry e : closing) e.window().tick();
    }

    /** 到点拆除退场窗口。必须在「子节点渲染迭代结束后」调用，此时改 children 无 CME。 */
    private void sweepClosing() {
        if (closing.isEmpty()) return;
        long now = Util.getMillis();
        for (Iterator<CloseEntry> it = closing.iterator(); it.hasNext(); ) {
            CloseEntry e = it.next();
            if (now >= e.deadlineMs()) {
                it.remove();
                removeWindow(e.window());
            }
        }
    }

    public Node modal() { return modal; }

    public boolean hasModal() { return modal != null && modal.visible; }

    public WindowLayer closeModalOnOutsideClick(boolean b) { this.closeModalOnOutsideClick = b; return this; }

    public WindowLayer closeModalOnEscape(boolean b) { this.closeModalOnEscape = b; return this; }

    public WindowLayer onModalClosed(Runnable r) { this.onModalClosed = r; return this; }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    protected void renderChildren(GuiGraphicsExtractor g, RenderContext ctx, boolean disabled) {
        Node m = hasModal() ? modal : null;
        for (Node c : children) {
            if (c == ghostOverlay) continue; // 残影单独压在所有窗口之上绘制，避免被后加的窗口盖住
            if (c == m && scrimColor != 0) {
                // 遮罩画在模态节点正下方，只挡住它下面的层
                Draw.fill(g, absX, absY, width, height, scrimColor);
            }
            c.render(g, ctx, disabled);
        }
        // 残影永远最上层：跨窗口拖拽时光标在另一窗口之上，残影仍可见
        ghostOverlay.render(g, ctx, disabled);
    }

    /**
     * 渲染整层，并在最上方补上提示气泡。
     * Screen 应该调这个，而不是 {@code render(g, mouseX, mouseY, tick)}。
     */
    public void renderLayer(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        pumpClosing(); // 渲染前推进退场淡出窗口的 alpha（宿主不驱动 tick 时也能淡出）
        // 拖拽中：光标贴近滚动容器边缘时先驱动其滚动（translate 立即生效，本帧即反映）
        if (dragRegistry.activeDrag() != null) dragRegistry.autoScroll(this, mouseX, mouseY);
        render(g, mouseX, mouseY, partialTick);
        sweepClosing(); // 渲染迭代已结束：到点的退场窗口此时拆除，改 children 安全无 CME（置于 tooltip 短路之前）
        // Ctrl 状态同步必须早于 tooltipsEnabled 短路：否则关掉 tooltip 的界面里
        // 宿主收不到「松开」这一帧，ctrlPinned 窗口会残留不塌缩。
        kwHost.setCtrlDown(isCtrlDown());
        if (!tooltipsEnabled) return;

        kwHost.setScreenSize(width, height);
        kwHost.setAltDown(isAltDown());
        kwHost.beginFrame();

        List<Component> lines = collectTooltip(mouseX, mouseY);
        // D.1 Tooltip delay-fade（悬停出现时延迟淡入）：amount() 在动画关闭/reduced-motion 时瞬到 1，
        // 立即全不透明绘制 = 现状零回归；动画开启时前 ~ms(HOVER) amount≈0（延迟不画），随后按 alpha 淡入
        // kwHost 内联盒子。移开即 wantTip=false → 立即消失（按现状行为裁决，不做淡出）。
        boolean wantTip = lines != null && !lines.isEmpty();
        tooltipFade.update(wantTip);
        float ta = wantTip ? tooltipFade.amount() : 0F;
        if (wantTip && ta > 0.02F) {
            boolean handled = kwHost.renderTransient(g, mouseX, mouseY, partialTick, lines, ta);
            if (!handled) {
                // 无关键字命中：回退原生 tooltip（MC 下一帧不透明绘制，无法注入 alpha；延迟已由上方门控提供）
                g.setComponentTooltipForNextFrame(Draw.font(), lines, mouseX, mouseY);
            }
        }
        kwHost.renderPinnedChildren(g, mouseX, mouseY, partialTick);
    }

    /** 每帧轮询取 ALT 实时状态（Node 分发层无键盘释放钩子，故用轮询而非事件）。 */
    private boolean isAltDown() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null) return false;
            return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
        } catch (Exception ignored) {
            return false;
        }
    }

    /** 每帧轮询取 CTRL 实时状态（同 ALT，轮询而非事件）。供关键字 tooltip 的 Ctrl 双形态使用。 */
    private boolean isCtrlDown() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null) return false;
            return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
        } catch (Exception ignored) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // 事件：模态优先，其次置顶再分发
    // ------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;

        if (hasModal()) {
            if (modal.contains(mx, my)) return modal.mouseClicked(mx, my, button);
            if (closeModalOnOutsideClick) closeModal();
            return true; // 无论如何都吞掉，绝不穿透到背后
        }

        // 关键字点击钉住：命中 tooltip 中的关键字则消费点击并钉成持久窗口
        if (kwHost != null && kwHost.handleClick((int) mx, (int) my, button)) return true;

        // 先置顶，再走通用分发，保证被点中的窗口成为最上层
        for (int i = children.size() - 1; i >= 0; i--) {
            Node c = children.get(i);
            if (c.visible && c.contains(mx, my)) {
                bringToFront(c);
                break;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (!visible) return false;
        if (hasModal()) return modal.mouseReleased(mx, my, button);
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (!visible) return false;
        if (hasModal()) return modal.mouseDragged(mx, my, button, dx, dy);
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!visible) return false;
        if (hasModal()) {
            return modal.contains(mx, my) && modal.mouseScrolled(mx, my, sx, sy);
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        if (hasModal()) {
            if (closeModalOnEscape && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeModal();
                return true;
            }
            return modal.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!visible) return false;
        if (hasModal()) return modal.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }
}
