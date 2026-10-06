#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 2：Paper 预设 toast 配色（正文 1.26:1 完全不可读 → 15.03:1）。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

print("[P0-2] Paper toast 配色")
assert_no_collision(["0xFFF7F7F7", "0xFF8F8F8F", "0xFF0277BD", "0xFF1B7A32", "0xFF8A5A00", "0xFFB3261E"])

apply(PRESET, [
    ("""        @Override public int toastBg()        { return 0xFF323232; }
        @Override public int toastBorder()    { return 0xFF555555; }""",
     """        // Paper 是浅色预设：toast 底必须跟着变浅，否则 text()=0xFF212121 近黑正文
        // 压在 0xF0121212 深底上只有 1.26:1，完全不可读（可访问性 P0-2）
        @Override public int toastBg()        { return 0xFFF7F7F7; }
        @Override public int toastBorder()    { return 0xFF8F8F8F; }"""),
    ("""        @Override public int toastInfo()      { return 0xFF4FC3F7; }
        @Override public int toastSuccess()   { return 0xFF66BB6A; }
        @Override public int toastWarn()      { return 0xFFFFB74D; }
        @Override public int toastError()     { return 0xFFE57373; }""",
     """        // 浅底上语义色改用深色调，保证与 0xFFF7F7F7 底仍有足够对比
        @Override public int toastInfo()      { return 0xFF0277BD; }
        @Override public int toastSuccess()   { return 0xFF1B7A32; }
        @Override public int toastWarn()      { return 0xFF8A5A00; }
        @Override public int toastError()     { return 0xFFB3261E; }"""),
])

print("[P0-2] done")
