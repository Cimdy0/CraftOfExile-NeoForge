package com.cimdy.coe.ui.widget.attach;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 组件附件：核心功能之外的<b>可选增强</b>（装饰 / 音效 / 特效）。
 *
 * <p>纪律（计划四·六）：
 * <ul>
 *   <li>附件<b>不改变输入与数据链路</b>（Node 事件分发、发包回调一概不动）；</li>
 *   <li>核心组件<b>无附件也能完整工作</b>；附件失败 / 禁用时<b>静默降级</b>
 *       （tick/render 抛异常由 {@code Node} 捕获，不阻断渲染）；</li>
 *   <li>可叠加：一个节点可 {@code attach} 多个附件。</li>
 * </ul>
 */
public interface Attachment {

    /** 挂载到节点时回调（一次）。 */
    default void onAttach(Node owner) {}

    /** 每帧推进（由 {@code Node.tick()} 树递归调用）。 */
    default void tick(Node owner) {}

    /** 每帧绘制，画在节点自身 visuals 之上（叠加层）；坐标为节点矩形（绝对屏幕坐标）。 */
    default void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {}
}
