#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 3/4：stronghold / screen / widget-keyword 收尾所需的令牌。

shadowSoft 用 raw 角色（不随预设变换）：阴影是叠在任意底色上的黑色 alpha，
在浅色预设里把它也翻亮会变成「白雾」，反而不成其为阴影。
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"


def parse(c):
    return (c >> 24) & 0xFF, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF


def pack(a, r, g, b):
    return (a << 24) | (r << 16) | (g << 8) | b


def mix(c, target, t):
    a, r, g, b = parse(c)
    _, tr, tg, tb = parse(target)
    return pack(a, int(round(r + (tr - r) * t)), int(round(g + (tg - g) * t)), int(round(b + (tb - b) * t)))


def high_contrast(c, role):
    if role == "raw":
        return c
    if role == "surface":
        return mix(c, 0xFF05070A, 0.22)
    if role == "text":
        return mix(c, 0xFFFFFFFF, 0.30)
    if role == "border":
        return mix(c, 0xFFC8D4E0, 0.45)
    return mix(c, 0xFFFFFFFF, 0.26)


def paper(c, role):
    if role == "raw":
        return c
    if role == "surface":
        return mix(c, 0xFFF4F7FA, 0.88)
    if role == "text":
        return mix(c, 0xFF16202A, 0.86)
    if role == "border":
        return mix(c, 0xFF33404E, 0.62)
    return mix(c, 0xFF0A1420, 0.34)


def midnight(c, role):
    if role == "raw":
        return c
    if role == "surface":
        return mix(c, 0xFF0C1420, 0.42)
    if role == "text":
        return mix(c, 0xFFC4D6EC, 0.22)
    if role == "border":
        return mix(c, 0xFF3B5573, 0.45)
    return mix(c, 0xFF4E7BA8, 0.28)


def hx(v):
    return "0x%08X" % (v & 0xFFFFFFFF)


OWN = [
    ("threatTier1",       0xFF8B0000, "accent",  "优先级 / 威胁档 1（深红，符号 <<）"),
    ("threatTier2",       0xFFCD5C00, "accent",  "优先级 / 威胁档 2（橙，符号 <）"),
    ("threatTier3",       0xFF9ACD32, "accent",  "优先级 / 威胁档 3（黄绿，符号 =）"),
    ("threatTier4",       0xFF32CD32, "accent",  "优先级 / 威胁档 4（绿，符号 >）"),
    ("threatTier5",       0xFF006400, "accent",  "优先级 / 威胁档 5（深绿，符号 >>）"),
    ("statusOn",          0xFF00FF00, "accent",  "开关「开」绿 / 资源世界高亮绿"),
    ("statusOffDim",      0xFF9A9A9A, "text",    "开关「关」灰（比 statusOff() 更深一档）"),
    ("placeholderText",   0xFF808080, "text",    "占位提示灰（空槽「+」/ 空列表提示）"),
    ("stockSliderFill",   0x80ADD8E6, "accent",  "库存滑块填充（半透明淡蓝）"),
    ("stockTransitBand",  0x80FF6666, "accent",  "库存容量条：在途段（半透明红）"),
    ("stockStoredBand",   0x8090EE90, "accent",  "库存容量条：已存段（半透明绿）"),
    ("stockQtyLabel",     0xFFB0B0B0, "text",    "库存数量行：标签灰"),
    ("panelSlotFill",     0xFF1E2735, "surface", "面板槽位格子底色（原 AbstractPanelScreen.SLOT_FILL）"),
    ("chargeTrackBg",     0xFF2B2B2B, "surface", "充能条底槽（原 ExtraSlotsScreen.CHARGE_BG）"),
    ("shadowSoft",        0x4D000000, "raw",     "柔和阴影（tooltip 等小卡片；比 shadowColor() 更淡）"),
]

FORWARD = [
    ("statusOff",    "textMuted()",  "text",   "开关「关」灰（= textMuted()）"),
    ("stockQtyValue", "textBright()", "text",  "库存数量行：数值白（= textBright()）"),
]

all_vals = [hx(d) for _, d, _, _ in OWN]
for fn in (high_contrast, paper, midnight):
    all_vals += [hx(fn(d, r)) for _, d, r, _ in OWN]
print(f"[misc] 自有令牌 {len(OWN)}，待校验值 {len(all_vals)} 个")
assert_no_collision(all_vals)

print("\n%-22s %-10s %-10s %-10s %-10s" % ("token", "Dark", "HC", "Paper", "Midnight"))
for name, dark, role, doc in OWN:
    print("%-22s %-10s %-10s %-10s %-10s" % (name, hx(dark)[2:], hx(high_contrast(dark, role))[2:],
                                             hx(paper(dark, role))[2:], hx(midnight(dark, role))[2:]))

lines = ["    // --- 据点（stronghold）/ 通用屏幕收尾：B3 第二波的剩余散字面量 ---"]
for name, dark, role, doc in OWN:
    lines.append(f"    /** {doc}。 */")
    lines.append(f"    public int {name}() {{ return {hx(dark)}; }}")
for name, src, role, doc in FORWARD:
    lines.append(f"    /** {doc}。转发已有令牌，不另抄一份字面量。 */")
    lines.append(f"    public int {name}() {{ return {src}; }}")
block = "\n".join(lines)

ANCHOR = "    // --- 图（graph）画笔：天赋树 / 科技树 / 研究树共用的默认节点与连线色 ---"
apply(PRESET, [(ANCHOR, block + "\n\n" + ANCHOR)])


def overrides(fn):
    return "\n".join(f"        @Override public int {n}() {{ return {hx(fn(d, r))}; }}" for n, d, r, _ in OWN)


for anchor, fn in [
    ("        @Override public int panelColor()      { return 0xFF404040; }", high_contrast),
    ("        @Override public int panelColor()     { return 0xFFF5F5F5; }", paper),
    ("        @Override public int panelColor()     { return 0xFF1B2230; }", midnight),
]:
    apply(PRESET, [(anchor, overrides(fn) + "\n" + anchor)])

print("\n[misc] StylePreset 已写入")
