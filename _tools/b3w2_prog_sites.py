#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 2：progress 调用点迁移。

- ProgressionTheme：静态常量 → 转发层方法（与 TalentTheme 同构）
- ProgressionTreeScreen / TechTreeScreen / ResearchTreeScreen：裸字面量 → Theme.current().progressionX()
- ProgressionNodePainter：ProgressionTheme.CONST → ProgressionTheme.const()
"""
import sys, os, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, read_text, write_text

BASE = "src/main/java/com/cimdy/coe/ui/screen/progression/"
THEME_IMPORT = "import com.cimdy.coe.ui.widget.render.Theme;\n"

# ======================================================================
# 1) ProgressionTheme → 转发层
# ======================================================================
THEME_NEW = '''package com.cimdy.coe.ui.screen.progression;
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

    /** 树画布底（原 {@code BACKGROUND}）。 */
    public static int background() { return Theme.current().progressionCanvasBg(); }

    /** 节点边框：锁定（原 = {@code Theme.PANEL}，现在走 {@code panelColor()} 令牌）。 */
    public static int borderLocked() { return Theme.current().progressionBorderLocked(); }
    /** 节点边框：可研究（亮青）。 */
    public static int borderAvailable() { return Theme.current().progressionBorderAvailable(); }
    /** 节点边框：已完成（青绿常亮）。 */
    public static int borderAllocated() { return Theme.current().progressionBorderAllocated(); }
    /** 节点边框：研究进行中（暗青）。 */
    public static int borderAllocatedInactive() { return Theme.current().progressionBorderAllocatedInactive(); }
    /** 节点边框：预览。 */
    public static int borderPreview() { return Theme.current().progressionBorderPreview(); }

    /** 连线暗态。 */
    public static int edgeDim() { return Theme.current().progressionEdgeDim(); }
    /** 连线亮态。 */
    public static int edgeLit() { return Theme.current().progressionEdgeLit(); }
    /** 连线：预览。 */
    public static int edgePreview() { return Theme.current().progressionEdgePreview(); }

    /** 节点填充。 */
    public static int fillNode() { return Theme.current().progressionNodeFill(); }
    /** 节点填充：前置缺失（暗红）。 */
    public static int fillBroken() { return Theme.current().progressionNodeFillBroken(); }

    private ProgressionTheme() {}
}
'''

text, nl = read_text(BASE + "ProgressionTheme.java")
write_text(BASE + "ProgressionTheme.java", THEME_NEW, nl)
print("  ok  ProgressionTheme 重写为转发层")

# ======================================================================
# 2) ProgressionNodePainter：CONST -> const()
# ======================================================================
apply(BASE + "ProgressionNodePainter.java", [
    ("ProgressionTheme.FILL_NODE", "ProgressionTheme.fillNode()"),
    ("ProgressionTheme.FILL_BROKEN", "ProgressionTheme.fillBroken()"),
    ("ProgressionTheme.BORDER_LOCKED", "ProgressionTheme.borderLocked()"),
    ("ProgressionTheme.BORDER_ALLOCATED_INACTIVE", "ProgressionTheme.borderAllocatedInactive()"),
    ("ProgressionTheme.BORDER_ALLOCATED", "ProgressionTheme.borderAllocated()"),
    ("ProgressionTheme.BORDER_AVAILABLE", "ProgressionTheme.borderAvailable()"),
])

# ======================================================================
# 3) ProgressionTreeScreen：外壳
# ======================================================================
PTS = BASE + "ProgressionTreeScreen.java"
apply(PTS, [
    ("package com.cimdy.coe.ui.screen.progression;\n",
     "package com.cimdy.coe.ui.screen.progression;\n" + THEME_IMPORT),
    ("""    /** 全屏背景色（两棵树一致）。 */
    protected static final int BACKGROUND = 0xFF070B0E;

""", ""),
    ("g.fill(0, 0, width, height, BACKGROUND);",
     "g.fill(0, 0, width, height, Theme.current().progressionScreenBg());"),
    ("g.fill(rightPanelX, 0, width, height, 0xCC0A0E14);",
     "g.fill(rightPanelX, 0, width, height, Theme.current().progressionPanelBg());"),
    ("g.fill(rightPanelX - 1, 0, rightPanelX, height, 0xFF2A3340);",
     "g.fill(rightPanelX - 1, 0, rightPanelX, height, Theme.current().progressionPanelBorder());"),
    ("int bg = selected ? a.background() : (hover ? 0xFF24343F : 0xFF141C24);",
     "int bg = selected ? a.background()\n                : (hover ? Theme.current().progressionTabHoverBg() : Theme.current().progressionTabBg());"),
    ("int border = selected ? a.border() : 0xFF3A4652;",
     "int border = selected ? a.border() : Theme.current().progressionTabBorder();"),
    ("selected ? a.text() : 0xFFAABBCC);",
     "selected ? a.text() : Theme.current().progressionTabText());"),
])

# ======================================================================
# 4) TechTreeScreen
# ======================================================================
TTS = BASE + "TechTreeScreen.java"
apply(TTS, [
    ("package com.cimdy.coe.ui.screen.progression;\n",
     "package com.cimdy.coe.ui.screen.progression;\n" + THEME_IMPORT),
    ("""    private static final TabAccent ACCENT = new TabAccent(0xFFFFD24A, 0xFF4A3E1E, 0xFFFFF0C0);

""", ""),
    ("""    @Override
    protected TabAccent accent() {
        return ACCENT;
    }""",
     """    @Override
    protected TabAccent accent() {
        // 不能再是 static final：那会在类加载时冻结，换肤对它无效（与 TalentTheme 同一个坑）
        return new TabAccent(Theme.current().progressionTechAccent(),
                Theme.current().progressionTechAccentBg(),
                Theme.current().progressionTechAccentText());
    }"""),
    ("reached ? 0xFFFFD24A : 0xFF6A6A6A, true);",
     "reached ? Theme.current().progressionTechAccent() : Theme.current().progressionTextDim(), true);"),
    ("reached ? 0xFF4A4230 : 0xFF23272B);",
     "reached ? Theme.current().progressionSectionSepReached() : Theme.current().progressionSectionSepLocked());"),
    ("g.fill(sbX, CONTENT_TOP, sbX + 4, height - 4, 0xFF161C22);",
     "g.fill(sbX, CONTENT_TOP, sbX + 4, height - 4, Theme.current().progressionScrollTrack());"),
    ("g.fill(sbX, knobY, sbX + 4, knobY + knobH, 0xFFFFD24A);",
     "g.fill(sbX, knobY, sbX + 4, knobY + knobH, Theme.current().progressionTechAccent());"),
    ("bg = hover ? 0xFF1E5040 : 0xFF16382E;",
     "bg = hover ? Theme.current().progressionCellUnlockedBgHover() : Theme.current().progressionCellUnlockedBg();"),
    ("border = 0xFF6FE0A0;", "border = Theme.current().progressionBorderAllocated();"),
    ("textColor = 0xFFCCFFEE;", "textColor = Theme.current().progressionCellUnlockedText();"),
    ("bg = hover ? 0xFF1F4A5E : 0xFF14303D;",
     "bg = hover ? Theme.current().progressionCellAvailableBgHover() : Theme.current().progressionCellAvailableBg();"),
    ("border = 0xFF5FD8FF;", "border = Theme.current().progressionAccentCyan();"),
    ("textColor = 0xFFDDF4FF;", "textColor = Theme.current().progressionCellAvailableText();"),
    ("bg = 0xFF14181C;", "bg = Theme.current().progressionCellLockedBg();"),
    ("border = 0xFF33383D;", "border = Theme.current().progressionCellLockedBorder();"),
    ("textColor = 0xFF6A7078;", "textColor = Theme.current().progressionCellLockedText();"),
    ("unlocked ? 0xFF6FE0A0 : (affordable ? 0xFFFFE066 : 0xFFAA5555), true);",
     "unlocked ? Theme.current().progressionBorderAllocated()\n                : (affordable ? Theme.current().progressionCostReady() : Theme.current().progressionCostShort()), true);"),
    ("r[0] + 5, r[1] + r[3] - 11, levelOk ? 0xFF778899 : 0xFFAA5555, true);",
     "r[0] + 5, r[1] + r[3] - 11,\n                levelOk ? Theme.current().progressionLevelOkText() : Theme.current().progressionCostShort(), true);"),
    ('Component.translatable("progression.coe.level", p.level()), 0xFFFFE066);',
     'Component.translatable("progression.coe.level", p.level()), Theme.current().progressionLevelText());'),
    ("0xFFAACCDD)", "Theme.current().progressionInfoText())"),
    ('Component.translatable("progression.coe.tech_points", p.techPoints()), 0xFF5FD8FF);',
     'Component.translatable("progression.coe.tech_points", p.techPoints()),\n                Theme.current().progressionTechPointsText());'),
    ("0xFF9FD8A0)", "Theme.current().progressionBudgetText())"),
    ('Component.translatable("screen.coe.hint.scroll"), 0xFF556677);',
     'Component.translatable("screen.coe.hint.scroll"), Theme.current().progressionHint());'),
    ("g.fill(bx, by, bx + boxW + pad * 2, by + boxH, 0xE0101010);",
     "g.fill(bx, by, bx + boxW + pad * 2, by + boxH, Theme.current().progressionTooltipBg());"),
    ("g.outline(bx, by, boxW + pad * 2, boxH, 0xFF808080);",
     "g.outline(bx, by, boxW + pad * 2, boxH, Theme.current().progressionTooltipBorder());"),
    ("i == 0 ? 0xFFFFD24A : 0xFFCCCCCC, false);",
     "i == 0 ? Theme.current().progressionTechAccent() : Theme.current().progressionTextBody(), false);"),
])

# ======================================================================
# 5) ResearchTreeScreen
# ======================================================================
RTS = BASE + "ResearchTreeScreen.java"
apply(RTS, [
    ("package com.cimdy.coe.ui.screen.progression;\n",
     "package com.cimdy.coe.ui.screen.progression;\n" + THEME_IMPORT),
    ("""    private static final TabAccent ACCENT = new TabAccent(0xFF6FE0A0, 0xFF1E4A44, 0xFFCCFFEE);

""", ""),
    ("""    @Override
    protected TabAccent accent() {
        return ACCENT;
    }""",
     """    @Override
    protected TabAccent accent() {
        // 不能再是 static final：那会在类加载时冻结，换肤对它无效（与 TalentTheme 同一个坑）
        return new TabAccent(Theme.current().progressionResearchAccent(),
                Theme.current().progressionResearchAccentBg(),
                Theme.current().progressionResearchAccentText());
    }"""),
    (".color(ProgressionTheme.EDGE_DIM)", ".color(ProgressionTheme.edgeDim())"),
    (".colorActive(ProgressionTheme.EDGE_LIT)", ".colorActive(ProgressionTheme.edgeLit())"),
    ('Component.translatable("progression.coe.level", p.level()), 0xFFFFE066);',
     'Component.translatable("progression.coe.level", p.level()), Theme.current().progressionLevelText());'),
    ('Component.translatable("progression.coe.research_points", p.researchPoints()), 0xFF6FE0A0);',
     'Component.translatable("progression.coe.research_points", p.researchPoints()),\n                Theme.current().progressionResearchAccent());'),
    ("0xFFAACCDD)", "Theme.current().progressionInfoText())"),
    ('Component.translatable("research.coe.bench.running", name), 0xFF88CCFF);',
     'Component.translatable("research.coe.bench.running", name),\n                Theme.current().progressionBenchRunningText());'),
    ("g.fill(barX, y, barX + barW, y + barH, 0xFF1A2630);",
     "g.fill(barX, y, barX + barW, y + barH, Theme.current().progressionBarTrack());"),
    ("g.fill(barX, y, barX + (int) (barW * progress), y + barH, 0xFF4AC8A0);",
     "g.fill(barX, y, barX + (int) (barW * progress), y + barH, Theme.current().progressionBarFill());"),
    ("g.outline(barX, y, barW, barH, 0xFF5FD8FF);",
     "g.outline(barX, y, barW, barH, Theme.current().progressionBarBorder());"),
    ("barX + barW / 2, y, 0xFFCCFFEE);",
     "barX + barW / 2, y, Theme.current().progressionResearchAccentText());"),
    ("Math.max(0, (duration - bench.ticks()) / 20)), 0xFF99AABB);",
     "Math.max(0, (duration - bench.ticks()) / 20)), Theme.current().progressionTextSecondary());"),
    ('Component.translatable("research.coe.bench.absent"), 0xFFCC8855);',
     'Component.translatable("research.coe.bench.absent"), Theme.current().progressionBenchAbsent());'),
    ('Component.translatable("research.coe.bench.idle"), 0xFF99AABB);',
     'Component.translatable("research.coe.bench.idle"), Theme.current().progressionTextSecondary());'),
    ('Component.translatable("research.coe.bench.hint"), 0xFF667788);',
     'Component.translatable("research.coe.bench.hint"), Theme.current().progressionTextHint());'),
    ('Component.translatable("research.coe.panel.broken", brokenNodes), 0xFFFF6B6B);',
     'Component.translatable("research.coe.panel.broken", brokenNodes),\n                Theme.current().progressionDanger());'),
    ('Component.translatable("screen.coe.hint.pan_zoom"), 0xFF556677);',
     'Component.translatable("screen.coe.hint.pan_zoom"), Theme.current().progressionHint());'),
])

print("\n[prog] 调用点迁移完成")
