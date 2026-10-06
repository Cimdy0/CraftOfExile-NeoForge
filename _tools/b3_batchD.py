#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 Batch D：screen/gem —— 宝石切割 / 技能装配界面。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

ANCHOR = """    /** 扇区与枢纽文字（高亮 / 常态）。 */
    public int radialTextHot()    { return 0xFFF2C94C; }
    public int radialTextIdle()   { return 0xCFCFCF; }
"""
NEW = ANCHOR + """
    // --- 宝石（gem）业务界面：切割 / 装配 ---
    /** 宝石金：窗口边框与角点、技能键位文字 —— gem 界面唯一的「品牌色」。 */
    public int gemGold()           { return 0xFFC9A227; }
    /** gem 窗口底（切割界面）。 */
    public int gemWindowBg()       { return 0xE8141A26; }
    /** gem 面板底（装配界面，比窗口更透一档）。 */
    public int gemPanelBg()        { return 0xB8111826; }
    /** gem 卡片 / 视图底。 */
    public int gemViewBg()         { return 0xFF202B40; }
    /** gem 整屏渐变背景（上 / 下）。 */
    public int gemBackdropTop()    { return 0xEE0C1120; }
    public int gemBackdropBottom() { return 0xEE04060B; }
    /** gem 次级文字（等级等）。 */
    public int gemTextMuted()      { return 0xFF9FB4CC; }
    /** gem 正向语义文字（光环已开启等）。 */
    public int gemTextPositive()   { return 0xFF7CFF7C; }
    /** 「不可用」图标的去色遮罩。 */
    public int gemDesaturate()     { return 0xB08C8C8C; }
"""

print("[Batch D] StylePreset 新令牌")
assert_no_collision(["0xFFC9A227", "0xE8141A26", "0xB8111826", "0xFF202B40", "0xEE0C1120",
                     "0xEE04060B", "0xFF9FB4CC", "0xFF7CFF7C", "0xB08C8C8C"])
apply(PRESET, [(ANCHOR, NEW)])

print("[Batch D] 预设覆盖")
assert_no_collision([
    "0xFFFFD54A", "0xEE1E2436", "0xC01E2436", "0xFF2C3A52", "0xEE141C2C", "0xEE080A12",
    "0xFFC3D2E2", "0xFF9EFF9E", "0xB0A8A8A8",
    "0xF2FAFAFA", "0xE8F5F5F5", "0xFFECEFF1", "0xE8ECEFF1", "0xE8DCE1E8", "0xFF546E7A",
    "0xFF2E7D32", "0xB0B0B0B0",
    "0xFFD8A93A", "0xE8121A2A", "0xB80E1420", "0xFF1E2A3C", "0xEE0A1018", "0xEE04080E",
    "0xFF8FA6BF", "0xFF7FD07F", "0xB0556478",
])
apply(PRESET, [
    ("""        @Override public int radialBackdrop()   { return 0xA8000000; }""",
     """        @Override public int gemGold()           { return 0xFFFFD54A; }
        @Override public int gemWindowBg()       { return 0xEE1E2436; }
        @Override public int gemPanelBg()        { return 0xC01E2436; }
        @Override public int gemViewBg()         { return 0xFF2C3A52; }
        @Override public int gemBackdropTop()    { return 0xEE141C2C; }
        @Override public int gemBackdropBottom() { return 0xEE080A12; }
        @Override public int gemTextMuted()      { return 0xFFC3D2E2; }
        @Override public int gemTextPositive()   { return 0xFF9EFF9E; }
        @Override public int gemDesaturate()     { return 0xB0A8A8A8; }
        @Override public int radialBackdrop()   { return 0xA8000000; }"""),
    ("""        @Override public int radialBackdrop()   { return 0x35000000; }""",
     """        @Override public int gemGold()           { return 0xFFF9A825; }
        @Override public int gemWindowBg()       { return 0xF2FAFAFA; }
        @Override public int gemPanelBg()        { return 0xE8F5F5F5; }
        @Override public int gemViewBg()         { return 0xFFECEFF1; }
        @Override public int gemBackdropTop()    { return 0xE8ECEFF1; }
        @Override public int gemBackdropBottom() { return 0xE8DCE1E8; }
        @Override public int gemTextMuted()      { return 0xFF546E7A; }
        @Override public int gemTextPositive()   { return 0xFF2E7D32; }
        @Override public int gemDesaturate()     { return 0xB0B0B0B0; }
        @Override public int radialBackdrop()   { return 0x35000000; }"""),
    ("""        @Override public int radialBackdrop()   { return 0x8A000000; }""",
     """        @Override public int gemGold()           { return 0xFFD8A93A; }
        @Override public int gemWindowBg()       { return 0xE8121A2A; }
        @Override public int gemPanelBg()        { return 0xB80E1420; }
        @Override public int gemViewBg()         { return 0xFF1E2A3C; }
        @Override public int gemBackdropTop()    { return 0xEE0A1018; }
        @Override public int gemBackdropBottom() { return 0xEE04080E; }
        @Override public int gemTextMuted()      { return 0xFF8FA6BF; }
        @Override public int gemTextPositive()   { return 0xFF7FD07F; }
        @Override public int gemDesaturate()     { return 0xB0556478; }
        @Override public int radialBackdrop()   { return 0x8A000000; }"""),
])

print("[Batch D] CutScreen")
CUT = "src/main/java/com/cimdy/coe/ui/screen/gem/CutScreen.java"
apply(CUT, [
    ("import com.cimdy.coe.ui.widget.render.impl.text.TextRenderer;\n",
     "import com.cimdy.coe.ui.widget.render.impl.text.TextRenderer;\nimport com.cimdy.coe.ui.widget.render.Theme;\n"),
    ("window.background(ColorRenderer.of(0xE8141A26, 0xFFC9A227)\n"
     "                .borderStyle(ColorRenderer.BorderStyle.FULL)\n"
     "                .cornerDots(0xFFC9A227));",
     "window.background(ColorRenderer.of(Theme.current().gemWindowBg(), Theme.current().gemGold())\n"
     "                .borderStyle(ColorRenderer.BorderStyle.FULL)\n"
     "                .cornerDots(Theme.current().gemGold()));"),
    ("view.with(ColorRenderer.of(0xFF202B40, 0)\n"
     "                .backgroundHover(blend(accent, 0xFF202B40, 0.35F))",
     "view.with(ColorRenderer.of(Theme.current().gemViewBg(), 0)\n"
     "                .backgroundHover(blend(accent, Theme.current().gemViewBg(), 0.35F))"),
    ("graphics.fillGradient(0, 0, this.width, this.height, 0xEE0C1120, 0xEE04060B);",
     "graphics.fillGradient(0, 0, this.width, this.height,\n"
     "                Theme.current().gemBackdropTop(), Theme.current().gemBackdropBottom());"),
])

print("[Batch D] SkillScreen")
SKILL = "src/main/java/com/cimdy/coe/ui/screen/gem/SkillScreen.java"
apply(SKILL, [
    ("import com.cimdy.coe.ui.widget.render.impl.text.TextRenderer;\n",
     "import com.cimdy.coe.ui.widget.render.impl.text.TextRenderer;\nimport com.cimdy.coe.ui.widget.render.Theme;\n"),
    ("window.background(ColorRenderer.of(0xB8111826, 0xFFC9A227)\n"
     "                .borderStyle(ColorRenderer.BorderStyle.FULL)\n"
     "                .cornerDots(0xFFC9A227));",
     "window.background(ColorRenderer.of(Theme.current().gemPanelBg(), Theme.current().gemGold())\n"
     "                .borderStyle(ColorRenderer.BorderStyle.FULL)\n"
     "                .cornerDots(Theme.current().gemGold()));"),
    ("                .color(0xFF9FB4CC));", "                .color(Theme.current().gemTextMuted()));"),
    ("                .color(0xFF7CFF7C));", "                .color(Theme.current().gemTextPositive()));"),
    ("                .color(0xFFC9A227));", "                .color(Theme.current().gemGold()));"),
])

print("[Batch D] UnusableMark")
UNUSE = "src/main/java/com/cimdy/coe/ui/screen/gem/UnusableMark.java"
apply(UNUSE, [
    ("import com.cimdy.coe.ui.widget.render.Renderer;\n",
     "import com.cimdy.coe.ui.widget.render.Renderer;\nimport com.cimdy.coe.ui.widget.render.Theme;\n"),
    ("    /** 去色遮罩色：与 HUD 的红色「不可施放」遮罩 0x80FF3030 同族，这里用中性灰表达「灰掉」。 */\n"
     "    private static final int DESATURATE = 0xB08C8C8C;",
     "    /** 去色遮罩色：与 HUD 的红色「不可施放」遮罩 0x80FF3030 同族，这里用中性灰表达「灰掉」。\n"
     "     *  B3：改从当前风格预设取（实例字段，随换肤生效）。 */\n"
     "    private final int desaturate = Theme.current().gemDesaturate();"),
    ("Draw.fill(g, x, y, box, box, DESATURATE);", "Draw.fill(g, x, y, box, box, desaturate);"),
])

print("[Batch D] done")
