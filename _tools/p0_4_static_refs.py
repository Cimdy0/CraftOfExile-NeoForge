#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 4（续）：ui/ 内其余非 render/ 文件的 Theme.<静态常量> -> Theme.current().getter()。

安全约束：
  * 只改「调用点」，不动 Theme.java 的常量本身（它们是 Dark 基准值且被 lint 索引）。
  * 跳过 widget/render/**（B3 约定：不擅动 render/ 对外契约，留作独立任务）。
  * 跳过 TalentTheme / ProgressionTheme 两个静态色板类：它们的字段是
    public static final，改成 Theme.current() 会在类加载时冻结，反而更糟；
    这两处应连同 talent*/progression* 令牌一起做成「转发层」，属 B3 后续批次。
  * 跳过 ToastHost.ToastStyle 枚举的 4 个构造实参：那是刻意保留的 Dark 基准值。
  * 每个文件先核对「注释内出现次数 == 0」才做整体替换，避免改到 javadoc。
"""
import sys, os, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import read_text, write_text

UI_REL = "src/main/java/com/cimdy/coe/ui"

# Theme 常量 -> StylePreset 预设感知 getter
MAP = {
    "ACCENT": "accent()", "ACCENT_BRIGHT": "accentBright()",
    "PANEL": "panelColor()", "PANEL_DIM": "panelDim()",
    "WINDOW_BG": "windowBg()", "INPUT_BG": "inputBg()",
    "PANEL_SIDE": "panelSide()", "POPUP_BG": "popupBg()", "POPUP_BORDER": "popupBorder()",
    "TRACK": "track()", "SLOT_BG": "slotBg()", "SLOT_BORDER": "slotBorder()",
    "HOVER_BG": "hoverBg()", "ACTIVE_BG": "activeBg()", "PRESSED_BG": "pressedBg()",
    "DISABLED_BG": "disabledBg()", "INPUT_BORDER_HOVER": "inputBorderHover()",
    "BORDER_DISABLED": "borderDisabled()", "HANDLE_BG": "handleBg()", "HANDLE_HOVER": "handleHover()",
    "TITLE_BAR_BG": "titleBarBg()", "CORNER_DOTS": "cornerDots()", "CORNER_TICKS": "cornerTicks()",
    "OPTION_HOVER_BG": "optionHoverBg()", "TEXT_HOVER": "textHover()",
    "TEXT_DISABLED": "textDisabled()", "TEXT": "text()", "PAGER_BAR_BG": "pagerBarBg()",
    "TOGGLE_TRACK_OFF": "toggleTrackOff()", "TOGGLE_TRACK_ON": "toggleTrackOn()",
    "TOGGLE_THUMB": "toggleThumb()", "PROGRESS_TRACK": "progressTrackColor()",
    "PROGRESS_FILL": "progressFill()", "TAB_BG": "tabBgColor()", "TAB_ACTIVE": "tabActive()",
    "TAB_BORDER": "tabBorder()", "TOAST_BG": "toastBg()", "TOAST_BORDER": "toastBorder()",
    "TOAST_INFO": "toastInfo()", "TOAST_SUCCESS": "toastSuccess()",
    "TOAST_WARN": "toastWarn()", "TOAST_ERROR": "toastError()",
    "CELL_BG": "cellBg()", "CELL_BORDER": "cellBorder()", "CELL_SELECTED": "cellSelected()",
}
# 非颜色常量 / 刻意保留，不转换
SKIP = {"SLOT_SIZE"}

TARGETS = [
    "widget/keyword/KeywordTooltipBox.java",
    "widget/keyword/KeywordRichText.java",
    "widget/component/bar/TabBar.java",
    "widget/component/bar/ValueBar.java",
    "widget/component/bar/ProgressBar.java",
    "widget/component/button/Button.java",
    "widget/component/button/ToggleWidget.java",
    "widget/component/Stepper.java",
    "widget/component/menu/ContextMenu.java",
    "widget/component/list/OptionListPopup.java",
    "widget/component/panel/Window.java",
    "widget/component/badge/Badge.java",
    "widget/compose/composite/Collapsible.java",
    "widget/graph/painter/DotNodePainter.java",
    "widget/graph/painter/LineEdgePainter.java",
    "screen/radial/RadialMenuScreen.java",
    "screen/stronghold/TerminalScreen.java",
    "screen/talent/AbstractTalentTreeScreen.java",
]

SR = re.compile(r"\bTheme\.([A-Z][A-Z0-9_]*)\b")
LC = re.compile(r"//.*")
ST = re.compile(r'"(?:\\.|[^"\\])*"')


def code_lines(text):
    """yield (lineno, 去注释后的代码文本)"""
    out, in_block = [], False
    for i, raw in enumerate(text.split("\n"), 1):
        l = raw.rstrip("\r")
        if in_block:
            e = l.find("*/")
            if e == -1:
                continue
            l = l[e + 2:]
            in_block = False
        l = LC.sub("", l)
        l = ST.sub('""', l)
        bi = l.find("/*")
        if bi != -1:
            rest = l[bi + 2:]
            ei = rest.find("*/")
            if ei != -1:
                l = l[:bi] + rest[ei + 2:]
            else:
                l = l[:bi]
                in_block = True
        out.append((i, l))
    return out


total = 0
for rel in TARGETS:
    path = f"{UI_REL}/{rel}"
    text, nl = read_text(path)
    code = dict(code_lines(text))
    code_hits = [m.group(1) for _, l in code.items() for m in SR.finditer(l)]
    raw_hits = [m.group(1) for m in SR.finditer(text)]
    if len(code_hits) != len(raw_hits):
        print(f"  !! SKIP {rel}: 有 {len(raw_hits) - len(code_hits)} 处出现在注释里，需人工处理")
        continue
    if not code_hits:
        print(f"  -- {rel}: 无静态引用")
        continue
    unknown = sorted({c for c in code_hits if c not in MAP and c not in SKIP})
    if unknown:
        print(f"  !! SKIP {rel}: 未映射常量 {unknown}")
        continue
    new_text = text
    for const in sorted({c for c in code_hits if c in MAP}):
        new_text = re.sub(r"\bTheme\." + const + r"\b", "Theme.current()." + MAP[const], new_text)
    n = len([c for c in code_hits if c in MAP])
    write_text(path, new_text, nl)
    total += n
    print(f"  ok  {rel}: {n} 处  " + ", ".join(f"{c}x{code_hits.count(c)}" for c in sorted(set(code_hits))))

print(f"[P0-4] 共转换 {total} 处静态引用")
