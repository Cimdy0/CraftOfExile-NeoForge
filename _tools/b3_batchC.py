#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 Batch C：screen/radial —— 径向菜单（RadialMenuScreen）。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

ANCHOR = """    /** 连线（hovered）。 */
    public int graphEdgeHover()           { return 0xFF9AC8E8; }
"""
NEW = ANCHOR + """
    // --- 径向菜单（radial menu）---
    /** 径向菜单整屏底色（打开时压暗世界）。 */
    public int radialBackdrop()   { return 0x90000000; }
    /** 中心枢纽填充（有父级 = 「返回」态）。 */
    public int radialHubFill()    { return 0xFF3A4A5A; }
    /** 扇区描边（高亮 / 常态）。 */
    public int radialBorderHot()  { return 0xFFF2C94C; }
    public int radialBorderIdle() { return 0xFF6E7B8B; }
    /** 扇区填充（高亮 / 常态）。 */
    public int radialFillHot()    { return 0xFF2E3A48; }
    public int radialFillIdle()   { return 0xFF1C2530; }
    /** 扇区与枢纽文字（高亮 / 常态）。 */
    public int radialTextHot()    { return 0xFFF2C94C; }
    public int radialTextIdle()   { return 0xCFCFCF; }
"""

print("[Batch C] StylePreset 新令牌")
assert_no_collision(["0x90000000", "0xFF3A4A5A", "0xFFF2C94C", "0xFF6E7B8B",
                     "0xFF2E3A48", "0xFF1C2530", "0xCFCFCF"])
apply(PRESET, [(ANCHOR, NEW)])

print("[Batch C] 预设覆盖")
assert_no_collision([
    "0xA8000000", "0xFF4C5F72", "0xFFFFDC5C", "0xFF8A99AC", "0xFF3D4E60", "0xFF26313E",
    "0x35000000", "0xFFCFD8DC", "0xFFF9A825", "0xFF90A4AE", "0xFFE3F2FD", "0xFFECEFF1", "0xFFF57F17",
    "0xFF263344", "0xFFE8B84B", "0xFF54708C", "0xFF243349", "0xFF161F2C",
])
apply(PRESET, [
    ("""        @Override public int graphNodeBorder()         { return 0xFF9A9A9A; }""",
     """        @Override public int radialBackdrop()   { return 0xA8000000; }
        @Override public int radialHubFill()    { return 0xFF4C5F72; }
        @Override public int radialBorderHot()  { return 0xFFFFDC5C; }
        @Override public int radialBorderIdle() { return 0xFF8A99AC; }
        @Override public int radialFillHot()    { return 0xFF3D4E60; }
        @Override public int radialFillIdle()   { return 0xFF26313E; }
        @Override public int radialTextHot()    { return 0xFFFFDC5C; }
        @Override public int graphNodeBorder()         { return 0xFF9A9A9A; }"""),
    ("""        @Override public int graphNodeBorder()         { return 0xFFB0B0B0; }""",
     """        @Override public int radialBackdrop()   { return 0x35000000; }
        @Override public int radialHubFill()    { return 0xFFCFD8DC; }
        @Override public int radialBorderHot()  { return 0xFFF9A825; }
        @Override public int radialBorderIdle() { return 0xFF90A4AE; }
        @Override public int radialFillHot()    { return 0xFFE3F2FD; }
        @Override public int radialFillIdle()   { return 0xFFECEFF1; }
        @Override public int radialTextHot()    { return 0xFFF57F17; }
        @Override public int graphNodeBorder()         { return 0xFFB0B0B0; }"""),
    ("""        @Override public int graphNodeBorder()         { return 0xFF4A5A72; }""",
     """        @Override public int radialBackdrop()   { return 0x8A000000; }
        @Override public int radialHubFill()    { return 0xFF263344; }
        @Override public int radialBorderHot()  { return 0xFFE8B84B; }
        @Override public int radialBorderIdle() { return 0xFF54708C; }
        @Override public int radialFillHot()    { return 0xFF243349; }
        @Override public int radialFillIdle()   { return 0xFF161F2C; }
        @Override public int radialTextHot()    { return 0xFFE8B84B; }
        @Override public int graphNodeBorder()         { return 0xFF4A5A72; }"""),
])

print("[Batch C] RadialMenuScreen")
R = "src/main/java/com/cimdy/coe/ui/screen/radial/RadialMenuScreen.java"
apply(R, [
    ("gui.fill(0, 0, this.width, this.height, 0x90000000);",
     "gui.fill(0, 0, this.width, this.height, Theme.current().radialBackdrop());"),
    ("fillCircle(gui, cx, cy, innerR - 2, hasParent ? 0xFF3A4A5A : Theme.BORDER_DISABLED);",
     "fillCircle(gui, cx, cy, innerR - 2, hasParent ? Theme.current().radialHubFill() : Theme.BORDER_DISABLED);"),
    ("                cx, cy - 4, 0xCFCFCF);",
     "                cx, cy - 4, Theme.current().radialTextIdle());"),
    ("            int border = hot ? 0xFFF2C94C : 0xFF6E7B8B;\n"
     "            int fill = hot ? 0xFF2E3A48 : 0xFF1C2530;",
     "            int border = hot ? Theme.current().radialBorderHot() : Theme.current().radialBorderIdle();\n"
     "            int fill = hot ? Theme.current().radialFillHot() : Theme.current().radialFillIdle();"),
    ("                    hot ? 0xFFF2C94C : 0xCFCFCF);",
     "                    hot ? Theme.current().radialTextHot() : Theme.current().radialTextIdle());"),
])

print("[Batch C] done")
