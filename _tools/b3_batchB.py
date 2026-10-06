#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 Batch B：widget/graph/painter —— 天赋树 / 科技树 / 研究树共用的默认节点与连线画笔。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

# ---- 1) 新令牌 ----
ANCHOR = """    /** 危险 / 破坏性语义文字（关闭按钮悬浮、删除确认）。 */
    public int textDanger()         { return 0xFFFF6666; }
"""
NEW = ANCHOR + """
    // --- 图（graph）画笔：天赋树 / 科技树 / 研究树共用的默认节点与连线色 ---
    /** 节点描边（常态）。 */
    public int graphNodeBorder()          { return 0xFF6A6A6A; }
    /** 搜索命中（focused）时额外套的一圈。 */
    public int graphFocusRing()           { return 0xFF66CCFF; }
    /** 悬浮时额外套的一圈。 */
    public int graphHoverRing()           { return 0xFFFFFFFF; }
    /** 节点填充（active = 已激活 / 已点亮的节点）。 */
    public int graphNodeActiveFill()      { return 0xFF2E5C8A; }
    /** 节点填充（disabled = 未解锁）。 */
    public int graphNodeDisabledFill()    { return 0xFF202020; }
    /** 节点描边（disabled = 未解锁）。 */
    public int graphNodeDisabledBorder()  { return 0xFF303030; }
    /** 连线（hovered）。 */
    public int graphEdgeHover()           { return 0xFF9AC8E8; }
"""

print("[Batch B] StylePreset 新令牌")
assert_no_collision(["0xFF6A6A6A", "0xFF66CCFF", "0xFFFFFFFF", "0xFF2E5C8A",
                     "0xFF202020", "0xFF303030", "0xFF9AC8E8"])
apply(PRESET, [(ANCHOR, NEW)])

# ---- 2) 预设覆盖 ----
print("[Batch B] 预设覆盖")
assert_no_collision([
    "0xFF9A9A9A", "0xFFAADDFF", "0xFF3A76AE", "0xFF2C2C2C", "0xFF404040", "0xFFB8DFF5",
    "0xFFB0B0B0", "0xFF0288D1", "0xFF455A64", "0xFF90CAF9", "0xFFE4E4E4", "0xFFC0C0C0", "0xFF42A5F5",
    "0xFF4A5A72", "0xFF6FA8DC", "0xFFD6E2FF", "0xFF28496E", "0xFF141A26", "0xFF202836", "0xFF7FA8D8",
])
apply(PRESET, [
    ("""        @Override public int scrim()          { return 0xA5000000; }""",
     """        @Override public int graphNodeBorder()         { return 0xFF9A9A9A; }
        @Override public int graphFocusRing()          { return 0xFFAADDFF; }
        @Override public int graphNodeActiveFill()     { return 0xFF3A76AE; }
        @Override public int graphNodeDisabledFill()   { return 0xFF2C2C2C; }
        @Override public int graphNodeDisabledBorder() { return 0xFF404040; }
        @Override public int graphEdgeHover()          { return 0xFFB8DFF5; }
        @Override public int scrim()          { return 0xA5000000; }"""),
    ("""        @Override public int scrim()          { return 0x38000000; }""",
     """        @Override public int graphNodeBorder()         { return 0xFFB0B0B0; }
        @Override public int graphFocusRing()          { return 0xFF0288D1; }
        @Override public int graphHoverRing()          { return 0xFF455A64; }
        @Override public int graphNodeActiveFill()     { return 0xFF90CAF9; }
        @Override public int graphNodeDisabledFill()   { return 0xFFE4E4E4; }
        @Override public int graphNodeDisabledBorder() { return 0xFFC0C0C0; }
        @Override public int graphEdgeHover()          { return 0xFF42A5F5; }
        @Override public int scrim()          { return 0x38000000; }"""),
    ("""        @Override public int scrim()          { return 0x8A000000; }""",
     """        @Override public int graphNodeBorder()         { return 0xFF4A5A72; }
        @Override public int graphFocusRing()          { return 0xFF6FA8DC; }
        @Override public int graphHoverRing()          { return 0xFFD6E2FF; }
        @Override public int graphNodeActiveFill()     { return 0xFF28496E; }
        @Override public int graphNodeDisabledFill()   { return 0xFF141A26; }
        @Override public int graphNodeDisabledBorder() { return 0xFF202836; }
        @Override public int graphEdgeHover()          { return 0xFF7FA8D8; }
        @Override public int scrim()          { return 0x8A000000; }"""),
])

# ---- 3) 替换裸字面量 ----
print("[Batch B] DotNodePainter")
apply("src/main/java/com/cimdy/coe/ui/widget/graph/painter/DotNodePainter.java", [
    ("private final StateValue<Integer> border = StateValue.of(0xFF6A6A6A);",
     "private final StateValue<Integer> border = StateValue.of(Theme.current().graphNodeBorder());"),
    ("private int focusRing = 0xFF66CCFF;",
     "private int focusRing = Theme.current().graphFocusRing();"),
    ("private int hoverRing = 0xFFFFFFFF;",
     "private int hoverRing = Theme.current().graphHoverRing();"),
    ("        fill.hovered(Theme.POPUP_BORDER).active(0xFF2E5C8A).disabled(0xFF202020);\n"
     "        border.hovered(0xFFFFFFFF).active(Theme.ACCENT).disabled(0xFF303030);",
     "        fill.hovered(Theme.POPUP_BORDER)\n"
     "                .active(Theme.current().graphNodeActiveFill())\n"
     "                .disabled(Theme.current().graphNodeDisabledFill());\n"
     "        border.hovered(Theme.current().graphHoverRing())\n"
     "                .active(Theme.ACCENT)\n"
     "                .disabled(Theme.current().graphNodeDisabledBorder());"),
])

print("[Batch B] LineEdgePainter")
apply("src/main/java/com/cimdy/coe/ui/widget/graph/painter/LineEdgePainter.java", [
    ("color.active(Theme.ACCENT).hovered(0xFF9AC8E8).disabled(Theme.BORDER_DISABLED);",
     "color.active(Theme.ACCENT).hovered(Theme.current().graphEdgeHover()).disabled(Theme.BORDER_DISABLED);"),
])

print("[Batch B] done")
