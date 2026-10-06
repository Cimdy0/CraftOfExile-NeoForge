package com.cimdy.coe.ui.widget.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 渲染器：把一层「可视内容」画到给定矩形。
 *
 * <p>节点的 {@code visuals} 是一个渲染器列表，按序叠加成最终外观（背景 → 边框 → 图标 → 文字）。
 * 因此不需要为「带标题的边框盒」这类组合再造一个专用渲染器 —— 组合即可。
 *
 * <p>与旧 {@code IResourceRenderer} 的关键区别：
 * <ul>
 *   <li><b>渲染器真的负责渲染</b>。旧库把绘制塞在 {@code IColorRenderer} / {@code ITextRenderer}
 *       的 default 方法里，实现类只「供数」，绘制逻辑和取值逻辑被拆到两个文件，读代码要来回跳；</li>
 *   <li><b>状态从上游给</b>。旧库每个渲染器自己 {@code isHoverArea(mouseX,...)} 算 hover，
 *       同一份判定重复十几遍且与布局坐标耦合；现在统一由 {@link RenderContext#state()} 下发；</li>
 *   <li><b>不反向持有业务类型</b>。需要外部数据一律走构造期注入的 {@code Supplier} / 接口。</li>
 * </ul>
 */
public interface Renderer {

    /**
     * 绘制。
     *
     * @param g   绘制上下文
     * @param x   目标矩形左上角 X（绝对屏幕坐标）
     * @param y   目标矩形左上角 Y（绝对屏幕坐标）
     * @param w   目标矩形宽
     * @param h   目标矩形高
     * @param ctx 本帧上下文（鼠标位置、帧插值、可视状态）
     */
    void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx);

    /**
     * 内容固有宽度；0 表示「没有固有尺寸，完全听布局的」。
     * 供 Flexbox 在未显式指定尺寸时做内容测量。
     */
    default int preferredWidth() { return 0; }

    /** 内容固有高度；0 表示没有固有尺寸。 */
    default int preferredHeight() { return 0; }

    /** 是否含动画（决定是否需要每帧 tick）。 */
    default boolean isAnimated() { return false; }

    /** 动画推进。 */
    default void tick() {}

    /** 深拷贝，供列表项克隆使用。 */
    Renderer copy();

    /**
     * 本渲染器自供的悬浮提示行（无则返回 {@code null}）。
     *
     * <p>默认 {@code null}：大多数渲染器不携带提示。个别渲染器（典型 {@code ItemRenderer} 的物品栈）
     * 覆写它，把内容经宿主节点的 {@code Node.tooltipAt → collectTooltip → WindowLayer} 统一汇入
     * 关键字 tooltip 链路（{@code KeywordTooltipHost}），<b>不再</b>在 {@link #render} 里直调
     * 原版 {@code setTooltipForNextFrame} 绕开该链路。是否 hover 由 {@code collectTooltip} 命中裁决，
     * 此处无需自行判态。
     */
    default List<Component> hoverTooltip() { return null; }
}
