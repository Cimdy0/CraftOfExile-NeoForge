package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 可拖拽节点：按下后在移动阈值内即进入拖拽，跟随光标的「残影」由所在 {@link WindowLayer} 的
 * {@link DragRegistry} + {@link GhostOverlay} 统一绘制；松手时由 {@link DragRegistry} 按作用域优先级
 * 命中最近的 {@link DropZone} 并派发回调。
 *
 * <p>设计取舍（与「不重写现有事件流」一致）：
 * <ul>
 *   <li>本类只是 {@link Node} 子类，仅靠 {@code onMouse*} 三个钩子驱动自身拖拽；它返回 {@code true}
 *       占用事件，因此父窗口 / 兄弟节点不会把这次按压误当成自己的拖拽起点；</li>
 *   <li>残影图层挂在 {@link WindowLayer} 上（additive 字段），与现有布局 / 命中 / 渲染解耦；</li>
 *   <li>残影位置每帧由<b>光标屏幕坐标</b>算出（与节点自身布局 / 滚动位移无关），
 *       因此从滚动 / 嵌套容器里发起拖拽时残影始终贴合光标、不会脱钩；</li>
 *   <li>松手命中 zone 后，业务可借 {@link #onDrop}（通知）/ {@link #onDropInto}（真正搬数据）
 *       把底层模型 / 物品真正搬走，而非只画个残影。</li>
 * </ul>
 *
 * <p>注意：被拖的是「残影」—— 节点原地不动，残影跟随光标；是否真移动源节点 / 数据由 zone / 回调决定。
 */
public class Draggable extends Node {

    /** 进入拖拽的移动阈值（px），避免「点一下」被误判成拖。 */
    private static final int DRAG_THRESHOLD = 4;

    private DragRegistry registry;
    private Component dragLabel;
    private Runnable onClick;
    private Consumer<Draggable> onDrop;
    /** 真正搬数据 / 物理重定位的钩子：松手命中 zone 后拿到 (源, 命中 zone)。 */
    private BiConsumer<Draggable, DropZone> onDropInto;
    /** 携带的底层模型 / 物品引用；搬数据时由 onDropInto 读取。 */
    private Object payload;

    private boolean armed = false;
    private boolean dragging = false;
    private int pressButton = -1;
    private double grabOffX, grabOffY;  // 抓取点相对节点左上角
    private int ghostW, ghostH;
    /** 发起拖拽时源节点所属作用域（最近 DragScope 祖先），用于命中优先级。 */
    private Node sourceScope;

    public Draggable() {
        this.id = "draggable";
    }

    /** 拖拽时残影里显示的标签；不设置则只画一个半透矩形。 */
    public Draggable dragLabel(Component c) { this.dragLabel = c; return this; }

    /** 显式注入注册表；不设置则拖拽开始时从根节点（WindowLayer）自动解析。 */
    public Draggable dragRegistry(DragRegistry r) { this.registry = r; return this; }

    /** 单击（未形成拖拽）时触发。 */
    public Draggable onClick(Runnable r) { this.onClick = r; return this; }

    /** 成功落到某个 DropZone 时触发（先于 zone.onDrop，用于纯通知 / 状态更新）。 */
    public Draggable onDrop(Consumer<Draggable> c) { this.onDrop = c; return this; }

    /**
     * 真正搬数据 / 物理重定位的钩子：松手命中 zone 后回调 {@code (源节点, 命中 zone)}。
     * 业务应在这里把底层模型 / 物品搬走（例如从列表移除并插入目标容器）。
     */
    public Draggable onDropInto(BiConsumer<Draggable, DropZone> c) { this.onDropInto = c; return this; }

    /** 携带的底层模型 / 物品引用（搬数据时由 {@link #onDropInto} 读取）。 */
    public Draggable payload(Object o) { this.payload = o; return this; }

    /** 读取携带的模型 / 物品引用。 */
    public Object payload() { return payload; }

    private DragRegistry registry() {
        if (registry != null) return registry;
        Node r = root();
        if (r instanceof WindowLayer wl) registry = wl.dragRegistry();
        return registry;
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        armed = true;
        dragging = false;
        pressButton = button;
        grabOffX = lx;
        grabOffY = ly;
        ghostW = Math.max(1, width);
        ghostH = Math.max(1, height);
        return true; // 吃掉点击，避免父窗口 / 兄弟节点误把这次按压当成自己的拖拽起点
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!armed || button != pressButton) return false;
        DragRegistry reg = registry();
        // 光标屏幕坐标（与节点自身布局 / 滚动无关，残影永不脱钩）
        double mx = absX + lx;
        double my = absY + ly;
        if (!dragging) {
            if (Math.abs(lx - grabOffX) <= DRAG_THRESHOLD && Math.abs(ly - grabOffY) <= DRAG_THRESHOLD) {
                return true; // 仍在上阈值内：继续占用事件，但不进入拖拽
            }
            if (reg == null) { armed = false; return false; }
            dragging = true;
            // 解析源作用域（最近 DragScope 祖先），供作用域优先级命中
            Node scope = this;
            while (scope != null && !(scope instanceof DragScope)) scope = scope.parent;
            sourceScope = scope;
            reg.beginDrag(new DragRegistry.ActiveDrag(this, mx - grabOffX, my - grabOffY, ghostW, ghostH, dragLabel));
        }
        if (reg != null) {
            reg.moveGhost(mx - grabOffX, my - grabOffY);
            DragRegistry.ActiveDrag a = reg.activeDrag();
            if (a != null) {
                a.sourceScope = sourceScope;
                a.hovered = reg.hitTest(mx, my, sourceScope) != null;
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (!armed) return false;
        armed = false;
        if (!dragging) {
            if (onClick != null) onClick.run();
            return true;
        }
        dragging = false;
        DragRegistry reg = registry();
        if (reg != null) {
            double px = absX + lx, py = absY + ly;
            DropZone zone = reg.hitTest(px, py, sourceScope);
            if (zone != null) {
                if (onDrop != null) onDrop.accept(this);
                zone.onDrop(this);
                if (onDropInto != null) onDropInto.accept(this, zone);
            }
            reg.endDrag();
        }
        sourceScope = null;
        return true;
    }
}
