package com.cimdy.coe.ui.screen.progression;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.ui.widget.graph.painter.EdgePainter;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;

/**
 * 研究树视觉色板 —— 现在是 <b>转发层</b>（工作流 B3 第二波）。
 *
 * <p>以前这里是 {@code public static final int} 常量：类加载时即冻结，
 * 而且<b>完全绕开</b> {@link com.cimdy.coe.ui.widget.render.StylePreset}，
 * 于是 {@code Theme.apply(preset)} 对研究树零效果 —— 与 {@code TalentTheme} 同为
 * 「换肤不生效」的两大死角。现在全部改成方法，每次调用都从 {@code Theme.current()} 取。
 *
 * <p>与天赋树（{@code TalentTheme}）刻意用不同色系：天赋是「战斗成长」走金橙，
 * 研究是「基建投入」走青蓝，玩家扫一眼就知道自己在哪棵树里。
 *
 * <p>状态语义见 {@link ResearchAllocator}：
 * <ul>
 *   <li>{@code ALLOCATED} = 已完成研究（青绿常亮）；</li>
 *   <li>{@code ALLOCATED_INACTIVE} = 研究进行中（暗青，表示投入了但还没到手）；</li>
 *   <li>{@code AVAILABLE} = 前置已满足且研究点够（亮青可点）；</li>
 *   <li>{@code LOCKED} = 前置未满足或点数不足。</li>
 * </ul>
 */
public final class ProgressionTheme {


    /** 节点边框：锁定（原 = {@code Theme.PANEL}，现在走 {@code panelColor()} 令牌）。 */
    public static int borderLocked() { return Theme.current().progressionBorderLocked(); }
    /** 节点边框：可研究（亮青）。 */
    public static int borderAvailable() { return Theme.current().progressionBorderAvailable(); }
    /** 节点边框：已完成（青绿常亮）。 */
    public static int borderAllocated() { return Theme.current().progressionBorderAllocated(); }
    /** 节点边框：研究进行中（暗青）。 */
    public static int borderAllocatedInactive() { return Theme.current().progressionBorderAllocatedInactive(); }

    /** 连线暗态。 */
    public static int edgeDim() { return Theme.current().progressionEdgeDim(); }
    /** 连线亮态。 */
    public static int edgeLit() { return Theme.current().progressionEdgeLit(); }

    /** 节点填充。 */
    public static int fillNode() { return Theme.current().progressionNodeFill(); }
    /** 节点填充：前置缺失（暗红）。 */
    public static int fillBroken() { return Theme.current().progressionNodeFillBroken(); }

    private ProgressionTheme() {}
}
