#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 1：生成 talent*() 令牌族 + 三个非 Dark 预设的覆盖值。

Dark 默认值 = 迁移前裸字面量 ⇒ Dark 零视觉回归。
其余三个预设用**确定性变换**生成（surface / text / border / accent 四种角色各有规则），
而不是 147 个手挑色值 —— 这样可复现、可审计，也方便 art-director 后续微调。
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

# (令牌名, Dark 默认值, 角色, 中文语义)
TOKENS = [
    # --- 屏幕 chrome ---
    ("talentScreenBg",        0xFF0A0A0A, "surface", "整屏底"),
    ("talentPanelBg",         0xCC0A0E14, "surface", "右侧详情面板底"),
    ("talentPanelBorder",     0xFF2A3340, "border",  "右侧面板左分隔线"),
    ("talentSummaryBg",       0xEE0A0E14, "surface", "悬浮汇总卡底"),
    ("talentControlTrackBg",  0xFF333333, "surface", "通用控件凹槽底"),
    ("talentScrollTrackBg",   0xFF1A1F26, "surface", "滚动条轨道底"),
    # --- 树语义 ---
    ("talentBorderLocked",    0xFF404040, "border",  "节点边框：锁定"),
    ("talentBorderAvailable", 0xFF6FE06F, "accent",  "节点边框：可点"),
    ("talentBorderAllocated", 0xFFFFE066, "accent",  "节点边框：已点亮"),
    ("talentPreview",         0xFF66CCFF, "accent",  "预览态（边框 / 连线 / 文字）"),
    ("talentEdgeDim",         0xFF555555, "border",  "连线暗态 + 次级描边"),
    ("talentEdgeLit",         0xFFC8A04A, "accent",  "连线亮态"),
    ("talentJewelZone",       0x40C77DFF, "accent",  "珠宝辐射范围圈"),
    ("talentJewelLit",        0xFFC77DFF, "accent",  "被辐射节点外框"),
    ("talentSocketEmpty",     0xFF2A6E6E, "accent",  "珠宝插槽：空槽青框"),
    ("talentSocketFilled",    0xFFFFD700, "accent",  "珠宝插槽：已插金框"),
    ("talentBindingGlobal",   0xFF4AC8C8, "accent",  "武器组边框：全局（青）"),
    ("talentBinding1",        0xFFFF6B6B, "accent",  "武器组边框：I（红）"),
    ("talentBinding2",        0xFF6BE06B, "accent",  "武器组边框：II（绿）"),
    ("talentBindingDefault",  0xFFA8B8C8, "accent",  "武器组边框：其余（灰蓝）"),
    # --- 金色强调 ---
    ("talentAccent",          0xFFFFD24A, "accent",  "金色强调：标题 / 页签 / 基点环 / 搜索高亮"),
    ("talentAccentDim",       0xFFE0C060, "accent",  "金色强调（非激活档）"),
    ("talentKeystoneRingDim", 0x80C8C8C8, "border",  "notable 装饰环"),
    # --- 页签 / 控件 ---
    ("talentTabActiveBg",     0xFFB06020, "surface", "页签底：选中"),
    ("talentTabBg",           0xFF2A4F70, "surface", "页签底：常态"),
    ("talentTabBorder",       0xFF88CCFF, "accent",  "页签 / 主按钮边框"),
    ("talentTabIdleBg",       0xFF1A2E40, "surface", "页签 / 按钮底：常态"),
    ("talentArrowIdleBg",     0xFF223040, "surface", "翻页箭头底：常态"),
    ("talentControlBg",       0xFF1A2A38, "surface", "小按钮底：常态"),
    ("talentControlHoverBg",  0xFF334455, "surface", "小按钮底：悬浮"),
    ("talentPrimaryHoverBg",  0xFF336699, "surface", "主按钮底：悬浮"),
    ("talentPrimaryTextHot",  0xFFAAEEFF, "text",    "主按钮文字：悬浮"),
    # --- 删除（危险）---
    ("talentDangerBg",        0xFF401E1E, "surface", "删除按钮底：常态"),
    ("talentDangerHoverBg",   0xFF6E2A2A, "surface", "删除按钮底：悬浮"),
    ("talentDangerBorder",    0xFFAA4444, "accent",  "删除按钮边框"),
    ("talentDangerText",      0xFFFF8888, "accent",  "删除按钮文字"),
    # --- 文字 ---
    ("talentTextIdle",        0xFFCCCCCC, "text",    "文字：常态"),
    ("talentTextDim",         0xFF888888, "text",    "文字：次级"),
    ("talentTextNeutral",     0xFFEEEEEE, "text",    "文字：中性高亮"),
    ("talentTextSoft",        0xFFCCFFCC, "text",    "文字：柔和绿"),
    ("talentTextPoints",      0xFF88FF88, "text",    "文字：点数"),
    ("talentTextSummary",     0xFF88FFAA, "text",    "文字：汇总 / 滚动条滑块"),
    ("talentNumActive",       0xFFFF4444, "accent",  "数字：激活档"),
    ("talentOk",              0xFF6FE0A0, "accent",  "正向态绿（按钮边框 / 面板描边 / 滚动条）"),
    # --- 节点底 ---
    ("talentNodeBg",          0xCC101418, "surface", "插槽底：未点亮"),
    ("talentNodeBgAllocated", 0xCC1A2630, "surface", "插槽底：已点亮"),
]


# 这两个在 TalentTheme 里本来就直接引用 Theme 常量，改成「转发已有令牌」而不是抄一份字面量，
# 既避免与 Theme 常量重复定义，也让它们自动跟随各预设已有的 windowBg()/textDisabled()。
FORWARD = [
    ("talentBackground", "windowBg()", "树画布底（原 TalentTheme.BACKGROUND = Theme.WINDOW_BG）"),
    ("talentBorderAllocatedInactive", "textDisabled()", "节点边框：休眠武器组（原 = Theme.TEXT_DISABLED）"),
]


def parse(c):
    return (c >> 24) & 0xFF, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF


def pack(a, r, g, b):
    return (a << 24) | (r << 16) | (g << 8) | b


def mix(c, target, t):
    """把 c 的 RGB 朝 target 的 RGB 混合 t（alpha 保持不变）。"""
    a, r, g, b = parse(c)
    _, tr, tg, tb = parse(target)
    return pack(a,
                int(round(r + (tr - r) * t)),
                int(round(g + (tg - g) * t)),
                int(round(b + (tb - b) * t)))


# ---- 三个预设的确定性变换 ----
def high_contrast(c, role):
    """提亮 / 拉开对比：底更暗，字与边框更亮。"""
    if role == "surface":
        return mix(c, 0xFF05070A, 0.22)          # 底压暗
    if role == "text":
        return mix(c, 0xFFFFFFFF, 0.30)          # 字提亮
    if role == "border":
        return mix(c, 0xFFC8D4E0, 0.45)          # 描边提亮
    return mix(c, 0xFFFFFFFF, 0.26)              # accent


def paper(c, role):
    """浅色预设：底翻亮、字翻暗、描边加深、强调色压深保证浅底对比。"""
    if role == "surface":
        return mix(c, 0xFFF4F7FA, 0.88)
    if role == "text":
        return mix(c, 0xFF16202A, 0.86)
    if role == "border":
        return mix(c, 0xFF33404E, 0.62)
    return mix(c, 0xFF0A1420, 0.34)              # accent 压深


def midnight(c, role):
    """深蓝变体：整体往「深夜蓝」偏移。"""
    if role == "surface":
        return mix(c, 0xFF0C1420, 0.42)
    if role == "text":
        return mix(c, 0xFFC4D6EC, 0.22)
    if role == "border":
        return mix(c, 0xFF3B5573, 0.45)
    return mix(c, 0xFF4E7BA8, 0.28)              # accent 偏蓝


def hx(v):
    return "0x%08X" % (v & 0xFFFFFFFF)


# ---- 生成 ----
lines = []
lines.append("    // --- 天赋树（talent tree）：原本是 TalentTheme 里的静态常量 + 屏幕内散字面量，")
lines.append("    //     StylePreset 完全触达不到；整族默认值 = 迁移前的值，Dark 零回归 ---")
for name, dark, role, doc in TOKENS:
    lines.append(f"    /** {doc}。 */")
    lines.append(f"    public int {name}() {{ return {hx(dark)}; }}")
lines.append("")
for name, src, doc in FORWARD:
    lines.append(f"    /** {doc}。转发已有令牌，不另抄一份字面量。 */")
    lines.append(f"    public int {name}() {{ return {src}; }}")
talent_block = "\n".join(lines)

# 预设覆盖
def overrides(fn, label):
    out = []
    for name, dark, role, doc in TOKENS:
        v = fn(dark, role)
        out.append(f"        @Override public int {name}() {{ return {hx(v)}; }}")
    return "\n".join(out)

hc_block = overrides(high_contrast, "hc")
pp_block = overrides(paper, "paper")
mn_block = overrides(midnight, "midnight")

# 撞值校验：Dark 默认 + 三套覆盖
all_vals = [hx(d) for _, d, _, _ in TOKENS]
for fn in (high_contrast, paper, midnight):
    all_vals += [hx(fn(d, r)) for _, d, r, _ in TOKENS]
print(f"[talent] 令牌数 {len(TOKENS)}，待校验值 {len(all_vals)} 个")
assert_no_collision(all_vals)

# 打印表格供人工抽查
print("\n%-30s %-12s %-12s %-12s %-12s" % ("token", "Dark", "HighContrast", "Paper", "Midnight"))
for name, dark, role, doc in TOKENS:
    print("%-30s %-12s %-12s %-12s %-12s" % (name, hx(dark)[2:], hx(high_contrast(dark, role))[2:],
                                             hx(paper(dark, role))[2:], hx(midnight(dark, role))[2:]))

ANCHOR = """     * 键盘焦点环色。刻意与 {@code accent()} 的冷蓝<b>色相分离</b>成暖金："""
apply(PRESET, [
    (ANCHOR, talent_block + "\n\n" + ANCHOR),
])

for anchor, block in [
    ("        @Override public int focusRing()       { return 0xFFFFF07A; }", hc_block),
    ("        @Override public int focusRing()       { return 0xFF0A3D91; }", pp_block),
]:
    apply(PRESET, [(anchor, block + "\n" + anchor)])

# Midnight 没有 focusRing 覆盖，改用 gemGold 覆盖行做锚点
apply(PRESET, [("        @Override public int gemGold()           { return 0xFFD8A93A; }",
                mn_block + "\n        @Override public int gemGold()           { return 0xFFD8A93A; }")])

print("\n[talent] StylePreset 已写入")
