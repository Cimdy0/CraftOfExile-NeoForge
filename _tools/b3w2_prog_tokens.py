#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 2：progress*() 令牌族 + 共享令牌提升（goldAccent 等）。

1. 把 6 个「天赋树 / 进度树实际上在共用」的色提升为**通用层共享令牌**：
   goldAccent / treePanelBg / treePanelBorder / textBody / statusOk / statusAllocated。
   talent*() 对应令牌改为转发，并删掉它们在各预设里的重复覆盖 ——
   因为共享令牌用同一套确定性变换生成，值完全一致（脚本里逐条断言）。
2. 新增 progression*() 族：默认值 = 迁移前裸字面量 ⇒ Dark 零回归。
"""
import sys, os, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import read_text, write_text, apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"


def parse(c):
    return (c >> 24) & 0xFF, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF


def pack(a, r, g, b):
    return (a << 24) | (r << 16) | (g << 8) | b


def mix(c, target, t):
    a, r, g, b = parse(c)
    _, tr, tg, tb = parse(target)
    return pack(a,
                int(round(r + (tr - r) * t)),
                int(round(g + (tg - g) * t)),
                int(round(b + (tb - b) * t)))


def high_contrast(c, role):
    if role == "surface":
        return mix(c, 0xFF05070A, 0.22)
    if role == "text":
        return mix(c, 0xFFFFFFFF, 0.30)
    if role == "border":
        return mix(c, 0xFFC8D4E0, 0.45)
    return mix(c, 0xFFFFFFFF, 0.26)


def paper(c, role):
    if role == "surface":
        return mix(c, 0xFFF4F7FA, 0.88)
    if role == "text":
        return mix(c, 0xFF16202A, 0.86)
    if role == "border":
        return mix(c, 0xFF33404E, 0.62)
    return mix(c, 0xFF0A1420, 0.34)


def midnight(c, role):
    if role == "surface":
        return mix(c, 0xFF0C1420, 0.42)
    if role == "text":
        return mix(c, 0xFFC4D6EC, 0.22)
    if role == "border":
        return mix(c, 0xFF3B5573, 0.45)
    return mix(c, 0xFF4E7BA8, 0.28)


def hx(v):
    return "0x%08X" % (v & 0xFFFFFFFF)


# ======================================================================
# 1) 共享令牌（从 talent*() 提升）：(名, Dark 值, 角色, 说明)
# ======================================================================
SHARED = [
    ("goldAccent",      0xFFFFD24A, "accent",  "金色强调：天赋树 / 科技树的标题、页签、基点环、预览边框"),
    ("treePanelBg",     0xCC0A0E14, "surface", "树界面右侧信息面板底（天赋树 / 科技树 / 研究树同一套外壳）"),
    ("treePanelBorder", 0xFF2A3340, "border",  "树界面右侧面板左分隔线"),
    ("textBody",        0xFFCCCCCC, "text",    "正文文字（比 text() 暗一档的常态正文 / tooltip 正文）"),
    ("statusOk",        0xFF6FE0A0, "accent",  "正向状态绿：已点亮 / 已完成 / 可解锁"),
    ("statusAllocated", 0xFFFFE066, "accent",  "已点亮高亮黄：等级 / 花费 / 计数强调"),
]

# 对应地把这几个 talent*() 改成转发（值不变，只是来源改为共享令牌）
TALENT_FORWARD = [
    ("talentAccent",        "goldAccent()"),
    ("talentPanelBg",       "treePanelBg()"),
    ("talentPanelBorder",   "treePanelBorder()"),
    ("talentTextIdle",      "textBody()"),
    ("talentOk",            "statusOk()"),
    ("talentBorderAllocated", "statusAllocated()"),
]

# 值守恒断言：共享令牌在各预设的值，必须等于 talent*() 现有覆盖的字面量
EXPECT = {
    "talentPanelBg":         {"hc": 0xCC090C12, "paper": 0xCCD8DBDE, "midnight": 0xCC0B1119},
    "talentPanelBorder":     {"hc": 0xFF717B88, "paper": 0xFF303B49, "midnight": 0xFF324257},
    "talentBorderAllocated": {"hc": 0xFFFFE88E, "paper": 0xFFAC9B4E, "midnight": 0xFFCDC478},
    "talentAccent":          {"hc": 0xFFFFDE79, "paper": 0xFFAC913C, "midnight": 0xFFCDBA64},
    "talentTextIdle":        {"hc": 0xFFDBDBDB, "paper": 0xFF2F3841, "midnight": 0xFFCACED3},
    "talentOk":              {"hc": 0xFF94E8B9, "paper": 0xFF4D9B74, "midnight": 0xFF66C4A2},
}
SHARED_OF = {
    "talentPanelBg": "treePanelBg", "talentPanelBorder": "treePanelBorder",
    "talentBorderAllocated": "statusAllocated", "talentAccent": "goldAccent",
    "talentTextIdle": "textBody", "talentOk": "statusOk",
}

print("[prog] 值守恒校验：共享令牌变换值 vs talent*() 现有覆盖")
for tname, exp in EXPECT.items():
    sname = SHARED_OF[tname]
    dark = dict((n, d) for n, d, _, _ in SHARED)[sname]
    role = dict((n, r) for n, _, r, _ in SHARED)[sname]
    got = {"hc": high_contrast(dark, role), "paper": paper(dark, role), "midnight": midnight(dark, role)}
    for k in exp:
        assert got[k] == exp[k], f"  !! {sname}/{k}: computed {hx(got[k])} != existing {hx(exp[k])}"
    print(f"  ok  {sname:<18} hc={hx(got['hc'])[2:]} paper={hx(got['paper'])[2:]} midnight={hx(got['midnight'])[2:]}")


# ======================================================================
# 2) progression*() 族
# ======================================================================
OWN = [
    # --- 两棵树共用的外壳 ---
    ("progressionScreenBg",            0xFF070B0E, "surface", "整屏底（科技树 / 研究树一致）"),
    ("progressionTabBg",               0xFF141C24, "surface", "页签底：未选中常态"),
    ("progressionTabHoverBg",          0xFF24343F, "surface", "页签底：未选中悬浮"),
    ("progressionTabBorder",           0xFF3A4652, "border",  "页签边框：未选中"),
    ("progressionTabText",             0xFFAABBCC, "text",    "页签文字：未选中"),
    ("progressionInfoText",            0xFFAACCDD, "text",    "面板信息（经验 / 已完成数）"),
    ("progressionHint",                0xFF556677, "text",    "底部操作提示"),
    # --- 科技树（金色系）---
    ("progressionTechAccentBg",        0xFF4A3E1E, "surface", "科技树页签底：选中"),
    ("progressionTechAccentText",      0xFFFFF0C0, "text",    "科技树页签文字：选中"),
    ("progressionSectionSepReached",   0xFF4A4230, "border",  "等级段分隔线：已达成"),
    ("progressionSectionSepLocked",    0xFF23272B, "border",  "等级段分隔线：未达成"),
    ("progressionTextDim",             0xFF6A6A6A, "text",    "未达成段标题灰"),
    ("progressionScrollTrack",         0xFF161C22, "surface", "科技树滚动条轨道"),
    ("progressionCellUnlockedBg",      0xFF16382E, "surface", "单元格底：已解锁"),
    ("progressionCellUnlockedBgHover", 0xFF1E5040, "surface", "单元格底：已解锁·悬浮"),
    ("progressionCellAvailableBg",     0xFF14303D, "surface", "单元格底：可解锁"),
    ("progressionCellAvailableBgHover",0xFF1F4A5E, "surface", "单元格底：可解锁·悬浮"),
    ("progressionCellLockedBg",        0xFF14181C, "surface", "单元格底：锁定"),
    ("progressionCellLockedBorder",    0xFF33383D, "border",  "单元格边框：锁定"),
    ("progressionCellLockedText",      0xFF6A7078, "text",    "单元格文字：锁定"),
    ("progressionCellAvailableText",   0xFFDDF4FF, "text",    "单元格文字：可解锁"),
    ("progressionCostShort",           0xFFAA5555, "accent",  "花费 / 等级不足红"),
    ("progressionLevelOkText",         0xFF778899, "text",    "等级门槛：已达标（次级）"),
    ("progressionTooltipBg",           0xE0101010, "surface", "tooltip 底"),
    ("progressionTooltipBorder",       0xFF808080, "border",  "tooltip 边框"),
    ("progressionBudgetText",          0xFF9FD8A0, "text",    "配额绿（区块 / 实体 / 站点上限）"),
    # --- 研究树（青绿系）---
    ("progressionAccentCyan",          0xFF5FD8FF, "accent",  "青色强调：科技点 / 进度条边框 / 可研究边框"),
    ("progressionResearchAccentBg",    0xFF1E4A44, "surface", "研究树页签底：选中"),
    ("progressionTextSoft",            0xFFCCFFEE, "text",    "柔和青绿文字：页签选中 / 进度百分比 / 已解锁单元格"),
    ("progressionBenchRunningText",    0xFF88CCFF, "text",    "研究台运行中"),
    ("progressionBarTrack",            0xFF1A2630, "surface", "研究进度条轨道"),
    ("progressionEdgeLit",             0xFF4AC8A0, "accent",  "连线亮态 / 进度条填充"),
    ("progressionTextSecondary",       0xFF99AABB, "text",    "次级信息（剩余时间 / 空闲）"),
    ("progressionTextHint",            0xFF667788, "text",    "次级提示"),
    ("progressionBenchAbsent",         0xFFCC8855, "accent",  "无研究台告警橙"),
    ("progressionDanger",              0xFFFF6B6B, "accent",  "数据包损坏红（前置缺失）"),
    # --- 原 ProgressionTheme 常量 ---
    ("progressionCanvasBg",            0xFF0B1116, "surface", "树画布底（原 ProgressionTheme.BACKGROUND）"),
    ("progressionBorderAllocatedInactive", 0xFF2A7A88, "border", "节点边框：研究进行中（暗青）"),
    ("progressionEdgeDim",             0xFF3F4A52, "border",  "连线暗态"),
    ("progressionNodeFill",            0xFF1E3A46, "surface", "节点填充"),
    ("progressionNodeFillBroken",      0xFF5A1E1E, "surface", "节点填充：前置缺失"),
]

# 族内 / 跨族转发：值与上面某个令牌完全相同且角色相同，不另抄一份字面量
FORWARD = [
    ("progressionTechAccent",   "goldAccent()",      "accent", "科技树主色（标题 / 页签 / 段标题 / 滚动条 / tooltip 标题）"),
    ("progressionPanelBg",      "treePanelBg()",     "surface", "右侧面板底"),
    ("progressionPanelBorder",  "treePanelBorder()", "border",  "右侧面板分隔线"),
    ("progressionTextBody",     "textBody()",        "text",    "tooltip 正文"),
    ("progressionResearchAccent", "statusOk()",      "accent",  "研究树主色（已完成 / 已解锁绿）"),
    ("progressionBorderAllocated", "statusOk()",     "accent",  "节点边框：已完成"),
    ("progressionLevelText",    "statusAllocated()", "accent",  "等级数字"),
    ("progressionCostReady",    "statusAllocated()", "accent",  "花费充足高亮"),
    ("progressionBorderPreview", "goldAccent()",     "accent",  "节点边框：预览"),
    ("progressionEdgePreview",  "goldAccent()",      "accent",  "连线：预览"),
    ("progressionBorderAvailable", "progressionAccentCyan()", "accent", "节点边框：可研究"),
    ("progressionBarBorder",    "progressionAccentCyan()",    "accent", "研究进度条边框"),
    ("progressionTechPointsText", "progressionAccentCyan()",  "accent", "科技点数字"),
    ("progressionCellUnlockedText", "progressionTextSoft()",  "text",   "单元格文字：已解锁"),
    ("progressionResearchAccentText", "progressionTextSoft()", "text",  "研究树页签选中文字 / 进度百分比"),
    ("progressionBarFill",      "progressionEdgeLit()", "accent", "研究进度条填充"),
    ("progressionBorderLocked", "panelColor()",     "border",  "节点边框：锁定（原 = Theme.PANEL）"),
]

ALL_ROLE = dict((n, r) for n, _, r, _ in OWN)
for n, src, r, _ in FORWARD:
    ALL_ROLE[n] = r

# ---- 撞值校验（Dark 默认 + 三套覆盖）----
all_vals = [hx(d) for _, d, _, _ in OWN] + [hx(d) for _, d, _, _ in SHARED]
for fn in (high_contrast, paper, midnight):
    all_vals += [hx(fn(d, r)) for _, d, r, _ in OWN]
    all_vals += [hx(fn(d, r)) for _, d, r, _ in SHARED]
print(f"\n[prog] 自有令牌 {len(OWN)} + 共享令牌 {len(SHARED)}，待校验值 {len(all_vals)} 个")
assert_no_collision(all_vals)

# ---- 打印表格供人工抽查 ----
print("\n%-36s %-10s %-10s %-10s %-10s" % ("token", "Dark", "HC", "Paper", "Midnight"))
for name, dark, role, doc in OWN:
    print("%-36s %-10s %-10s %-10s %-10s" % (name, hx(dark)[2:], hx(high_contrast(dark, role))[2:],
                                             hx(paper(dark, role))[2:], hx(midnight(dark, role))[2:]))


# ======================================================================
# 3) 写入
# ======================================================================
def base_lines(items, indent="    "):
    out = []
    for name, dark, role, doc in items:
        out.append(f"{indent}/** {doc}。 */")
        out.append(f"{indent}public int {name}() {{ return {hx(dark)}; }}")
    return out


def fwd_lines(items, indent="    "):
    out = []
    for name, src, role, doc in items:
        out.append(f"{indent}/** {doc}。转发已有令牌，不另抄一份字面量。 */")
        out.append(f"{indent}public int {name}() {{ return {src}; }}")
    return out


shared_block = "\n".join(base_lines(SHARED))
prog_block = "\n".join(
    ["    // --- 进度树（progression）：科技树 / 研究树。原本是 ProgressionTheme 静态常量 + 屏幕内散字面量，",
     "    //     与天赋树刻意不同色系（天赋金橙战斗成长 / 研究青蓝基建投入），但共用同一套外壳 ---"]
    + base_lines(OWN) + [""] + fwd_lines(FORWARD)
)


def overrides(fn, items):
    return "\n".join(f"        @Override public int {n}() {{ return {hx(fn(d, r))}; }}" for n, d, r, _ in items)


def overrides_fwd(fn):
    """转发型令牌不生成覆盖：它们自动跟随被转发令牌的预设值。"""
    return ""


SHARED_ANCHOR = """    /** 危险 / 破坏性语义文字（关闭按钮悬浮、删除确认）。 */
    public int textDanger()         { return 0xFFFF6666; }"""
PROG_ANCHOR = "    // --- 图（graph）画笔：天赋树 / 科技树 / 研究树共用的默认节点与连线色 ---"

apply(PRESET, [(SHARED_ANCHOR, SHARED_ANCHOR + "\n\n" + shared_block)])
apply(PRESET, [(PROG_ANCHOR, prog_block + "\n\n" + PROG_ANCHOR)])

# talent*() 改为转发
for tname, src in TALENT_FORWARD:
    text, nl = read_text(PRESET)
    pat = re.compile(r"    public int " + tname + r"\(\) \{ return 0x[0-9A-F]{8}; \}")
    m = pat.search(text)
    assert m, f"  !! base def not found: {tname}"
    new = f"    public int {tname}() {{ return {src}; }}"
    text = text[:m.start()] + new + text[m.end():]
    write_text(PRESET, text, nl)
    print(f"  ok  base {tname} -> {src}")

# 删掉 talent*() 在三个预设里的冗余覆盖（值已由共享令牌的覆盖等价提供）
for tname, _ in TALENT_FORWARD:
    text, nl = read_text(PRESET)
    pat = re.compile(r"        @Override public int " + tname + r"\(\) \{ return 0x[0-9A-F]{8}; \}\n")
    n = len(pat.findall(text))
    assert n == 3, f"  !! expected 3 overrides of {tname}, found {n}"
    text = pat.sub("", text)
    write_text(PRESET, text, nl)
    print(f"  ok  drop {n} redundant @Override {tname}()")

# 各预设追加：共享令牌覆盖 + progression 自有覆盖
for anchor, fn, label in [
    ("        @Override public int panelColor()      { return 0xFF404040; }", high_contrast, "hc"),
    ("        @Override public int panelColor()     { return 0xFFF5F5F5; }", paper, "paper"),
    ("        @Override public int panelColor()     { return 0xFF1B2230; }", midnight, "midnight"),
]:
    block = overrides(fn, SHARED) + "\n" + overrides(fn, OWN)
    apply(PRESET, [(anchor, block + "\n" + anchor)])

print("\n[prog] StylePreset 已写入")
