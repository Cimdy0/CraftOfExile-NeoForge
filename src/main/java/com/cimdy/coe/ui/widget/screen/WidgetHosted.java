package com.cimdy.coe.ui.widget.screen;

import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * 节点树宿主接入接口：给「不能换基类」的 Screen 用。
 *
 * <p>{@link WidgetScreen} 是继承式的：换基类就能拿到全部接线。但很多界面已经继承了
 * {@code AbstractContainerScreen}（有物品槽位同步），换基类等于重写整条事件链路。
 * 这个接口把 {@link WidgetHost} 的「渲染 / tick / 事件转发 / 重建」收成一组默认方法，
 * 容器 Screen 只需：
 *
 * <ol>
 *   <li>持有一个 {@code WidgetHost} 字段；</li>
 *   <li>实现 {@link #ui()} 返回它；</li>
 *   <li>在 {@code init()} 里调 {@link #uiRebuild(int, int)}；</li>
 *   <li>把各事件 / 渲染 / tick 钩子改成先调对应的 {@code uiXxx} 转发。</li>
 * </ol>
 *
 * <pre>{@code
 * public class StockScreen extends AbstractContainerScreen<StockMenu> implements WidgetHosted {
 *     private final WidgetHost ui = new WidgetHost();
 *     @Override public WidgetHost ui() { return ui; }
 *
 *     @Override protected void init() { super.init(); uiRebuild(width, height); }
 *     @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
 *         super.extractRenderState(g, mx, my, pt);
 *         uiRender(g, mx, my, pt);
 *     }
 *     @Override public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
 *         return uiMouseClicked(e) || super.mouseClicked(e, dbl);
 *     }
 *     // 其余事件 / tick 同理：uiTick / uiMouseReleased / uiKeyPressed ...
 * }
 * }</pre>
 *
 * 事件转发方法全部返回「是否已被 UI 消费」，接进原版方法时用 {@code || super.xxx(...)} 让
 * 没被 UI 吃掉的事件继续走原版链路（ESC 关界面、槽位交互照常生效）。
 */
public interface WidgetHosted {

    /** 实现方持有并返回自己的 {@link WidgetHost} 实例。 */
    WidgetHost ui();

    // ------------------------------------------------------------------
    // 渲染与驱动
    // ------------------------------------------------------------------

    /** 渲染整层（含最上层的轻提示）。放 {@code extractRenderState} 里。 */
    default void uiRender(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        ui().render(g, mouseX, mouseY, partialTick);
    }

    /** 驱动动画 / 轻提示 / 定时任务。放 {@code tick()} 里。 */
    default void uiTick() { ui().tick(); }

    /** 屏幕尺寸变化（含 {@code init()} 首帧）。 */
    default void uiResize(int screenW, int screenH) { ui().resize(screenW, screenH); }

    // ------------------------------------------------------------------
    // 重建流程
    // ------------------------------------------------------------------

    /** 清空旧界面 → {@link #uiBuild} 重建 → 重新布局。放 {@code init()} 里。 */
    default void uiRebuild(int screenW, int screenH) {
        ui().clear();
        uiBuild(ui().layer());
        ui().resize(screenW, screenH);
    }

    /** 在 {@code root} 上挂窗口；配合 {@link #uiRebuild} 使用，不需要重建流程的可忽略。 */
    default void uiBuild(WindowLayer root) {}

    // ------------------------------------------------------------------
    // 事件转发：返回「是否已被 UI 吃掉」
    // ------------------------------------------------------------------

    default boolean uiMouseClicked(MouseButtonEvent e) { return ui().mouseClicked(e); }

    default boolean uiMouseReleased(MouseButtonEvent e) { return ui().mouseReleased(e); }

    default boolean uiMouseDragged(MouseButtonEvent e, double dx, double dy) {
        return ui().mouseDragged(e, dx, dy);
    }

    default boolean uiMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return ui().mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    default boolean uiKeyPressed(KeyEvent e) { return ui().keyPressed(e); }

    default boolean uiCharTyped(CharacterEvent e) { return ui().charTyped(e); }
}
