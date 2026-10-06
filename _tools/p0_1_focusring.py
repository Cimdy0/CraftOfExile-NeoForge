#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 1：键盘焦点环 focusRing() 令牌 + Node.java 改预设感知 + 2px 双环。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"
NODE = "src/main/java/com/cimdy/coe/ui/widget/core/Node.java"

# ---- 1) 新令牌 focusRing() ----
ANCHOR = """    /** 危险 / 破坏性语义文字（关闭按钮悬浮、删除确认）。 */
    public int textDanger()         { return 0xFFFF6666; }
"""
NEW = ANCHOR + """
    /**
     * 键盘焦点环色。刻意与 {@code accent()} 的冷蓝<b>色相分离</b>成暖金：
     * accent 同时用于「鼠标悬浮边框」与「选中边框」，若焦点环也用 accent，
     * 键盘焦点与鼠标悬浮在视觉上完全无法区分（可访问性 P0-3）。
     */
    public int focusRing()          { return 0xFFFFD24A; }
"""

print("[P0-1] StylePreset: focusRing() 令牌")
# 注意 0xFFFFD24A 与 gemGold() 默认值相同（值级巧合，非语义耦合），见报告
assert_no_collision(["0xFFFFD24A"])
apply(PRESET, [(ANCHOR, NEW)])

print("[P0-1] 预设覆盖（HighContrast / Paper；Midnight 与 Dark 用基类默认暖金）")
assert_no_collision(["0xFFFFF07A", "0xFF0A3D91"])
apply(PRESET, [
    ("""        @Override public int scrim()          { return 0xA5000000; }""",
     """        @Override public int focusRing()       { return 0xFFFFF07A; }
        @Override public int scrim()          { return 0xA5000000; }"""),
    ("""        @Override public int scrim()          { return 0x38000000; }""",
     """        @Override public int focusRing()       { return 0xFF0A3D91; }
        @Override public int scrim()          { return 0x38000000; }"""),
])

# ---- 2) Node.drawFocusRing：静态 Theme.ACCENT -> 预设感知 + 双环 ----
print("[P0-1] Node.java drawFocusRing")
OLD_RING = """    private void drawFocusRing(GuiGraphicsExtractor g, RenderContext ctx) {
        if (!ctx.state().focused() || width <= 0 || height <= 0) return;
        Draw.outline(g, absX, absY, width, height, Theme.ACCENT);
    }"""
NEW_RING = """    private void drawFocusRing(GuiGraphicsExtractor g, RenderContext ctx) {
        if (!ctx.state().focused() || width <= 0 || height <= 0) return;
        // 预设感知：静态 Theme.ACCENT 不经过 StylePreset，换肤对它无效
        int ring = Theme.current().focusRing();
        Draw.outline(g, absX, absY, width, height, ring);
        // 第二环内缩 2px 形成「双环」：两环之间留 1px 底色间隙，在深底与浅底上都能看清；
        // 节点太小放不下内环时退化为单环，绝不画负尺寸
        if (width > 6 && height > 6) {
            Draw.outline(g, absX + 2, absY + 2, width - 4, height - 4, ring);
        }
    }"""
apply(NODE, [(OLD_RING, NEW_RING)])

print("[P0-1] done")
