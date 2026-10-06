#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 5：radialTextIdle() 的 0xCFCFCF -> 0xFFCFCFCF（alpha=0 缺陷）。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

print("[P0-5] radialTextIdle(): 0xCFCFCF -> 0xFFCFCFCF")
assert_no_collision(["0xFFCFCFCF"])
apply(PRESET, [
    ("""    /** 扇区与枢纽文字（高亮 / 常态）。 */
    public int radialTextHot()    { return 0xFFF2C94C; }
    public int radialTextIdle()   { return 0xCFCFCF; }""",
     """    /** 扇区与枢纽文字（高亮 / 常态）。 */
    public int radialTextHot()    { return 0xFFF2C94C; }
    /**
     * 常态文字色。迁移前这里是 6 位字面量 {@code 0xCFCFCF}，按 ARGB int 解析即
     * {@code 0x00CFCFCF} —— alpha 为 0。同分支的高亮色写的是 {@code 0xFFF2C94C}，
     * 可见作者意图是「不透明白 / 不透明白灰」，只是漏写了 alpha 通道。
     * 这里补成 {@code 0xFFCFCFCF}：若底层渲染对 alpha=0 另有兜底，则视觉零变化；
     * 否则就是修复「文字完全不可见」。两种情况下都是正确值。
     */
    public int radialTextIdle()   { return 0xFFCFCFCF; }"""),
])
print("[P0-5] done")
