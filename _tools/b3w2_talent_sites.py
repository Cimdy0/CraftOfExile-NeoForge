#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UI-B3-002 子项 1：TalentTheme 改成方法转发 + talent 包内所有裸字面量改读令牌。"""
import sys, os, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply

THEME = "import com.cimdy.coe.ui.widget.render.Theme;\n"
BASE = "src/main/java/com/cimdy/coe/ui/"


def T(tok):
    return "Theme.current()." + tok + "()"


# ---------------------------------------------------------------- TalentTheme
OLD_THEME = open(BASE + "screen/talent/TalentTheme.java", encoding="utf-8").read().replace("\r\n", "\n")
NEW_THEME = '''package com.cimdy.coe.ui.screen.talent;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.data.codec.talent.node.TalentNodeType;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;

/**
 * 天赋树视觉色板 —— 现在是 <b>转发层</b>（工作流 B3 第二波）。
 *
 * <p>以前这里是 {@code public static final int} 常量：类加载时即冻结，
 * 而且<b>完全绕开</b> {@link com.cimdy.coe.ui.widget.render.StylePreset}，
 * 于是 {@code Theme.apply(preset)} 对天赋树零效果 —— 这正是「换肤不生效」的两大死角之一。
 * 现在全部改成方法，每次调用都从 {@code Theme.current()} 取，换肤才能真正作用到树上。
 *
 * <p>节点半径/底色按类型（{@link TalentNodeType}），边框与连线按分配状态。
 * 状态语义见 {@link TalentAllocator}：已点亮（金）/ 休眠（灰）/ 可点（绿）/ 锁定（深灰）。
 */
public final class TalentTheme {

    public static int background() { return Theme.current().talentBackground(); }

    public static int borderLocked()    { return Theme.current().talentBorderLocked(); }
    public static int borderAvailable() { return Theme.current().talentBorderAvailable(); }
    public static int borderAllocated() { return Theme.current().talentBorderAllocated(); }
    public static int borderAllocatedInactive() { return Theme.current().talentBorderAllocatedInactive(); }
    public static int borderPreview()   { return Theme.current().talentPreview(); }

    public static int edgeDim()     { return Theme.current().talentEdgeDim(); }
    public static int edgeLit()     { return Theme.current().talentEdgeLit(); }
    public static int edgePreview() { return Theme.current().talentPreview(); }

    /** 珠宝辐射范围圈颜色。 */
    public static int jewelZone() { return Theme.current().talentJewelZone(); }
    /** 被辐射珠宝覆盖的节点外框颜色。 */
    public static int jewelLit()  { return Theme.current().talentJewelLit(); }

    /** 珠宝插槽：空槽青框 / 已插珠宝金框。 */
    public static int socketEmpty()  { return Theme.current().talentSocketEmpty(); }
    public static int socketFilled() { return Theme.current().talentSocketFilled(); }

    /** 武器组边框色（索引 0=全局，1=I，2=II…）。 */
    public static int bindingColor(int weaponSet) {
        return switch (weaponSet) {
            case 0 -> Theme.current().talentBindingGlobal();   // 全局：青
            case 1 -> Theme.current().talentBinding1();        // I：红
            case 2 -> Theme.current().talentBinding2();        // II：绿
            default -> Theme.current().talentBindingDefault(); // 其余：灰蓝
        };
    }

    /** 休眠武器组的暗色版本。 */
    public static int bindingColorDim(int weaponSet) {
        int c = bindingColor(weaponSet);
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        return 0xFF000000 | ((r >> 1) << 16) | ((g >> 1) << 8) | (b >> 1);
    }

    public static int radiusFor(TalentNodeType t) {
        return t.radius();
    }

    public static int colorFor(TalentNodeType t) {
        return t.color();
    }

    private TalentTheme() {}
}
'''
from b3_apply import read_text, write_text
_t, _nl = read_text(BASE + "screen/talent/TalentTheme.java")
write_text(BASE + "screen/talent/TalentTheme.java", NEW_THEME.replace("\n", "\n"), _nl)
print("  ok  TalentTheme -> 转发层")

# ------------------------------------------------------- TalentNodePainter
print("[talent] TalentNodePainter")
apply(BASE + "screen/talent/TalentNodePainter.java", [
    ("import com.cimdy.coe.ui.widget.render.RenderContext;\n",
     "import com.cimdy.coe.ui.widget.render.RenderContext;\n" + THEME),
    ("ctx.hovered() ? 0xFFFFFFFF : TalentTheme.BORDER_AVAILABLE;",
     "ctx.hovered() ? " + T("textBright") + " : TalentTheme.borderAvailable();"),
    ("ctx.hovered() ? 0xFF888888 : TalentTheme.BORDER_LOCKED;",
     "ctx.hovered() ? " + T("talentTextDim") + " : TalentTheme.borderLocked();"),
    ("def.type().keystone() ? 0xFFFFD24A : 0x80C8C8C8;",
     "def.type().keystone() ? " + T("talentAccent") + " : " + T("talentKeystoneRingDim") + ";"),
    ("Draw.disc(g, Math.round(cx), Math.round(cy), r + thick + 2, TalentTheme.JEWEL_LIT);",
     "Draw.disc(g, Math.round(cx), Math.round(cy), r + thick + 2, TalentTheme.jewelLit());"),
    ("Draw.disc(g, Math.round(cx), Math.round(cy), r + thick + 3, 0xFFFFD24A);",
     "Draw.disc(g, Math.round(cx), Math.round(cy), r + thick + 3, " + T("talentAccent") + ");"),
    ("g.fill(left, top, left + size, top + size, allocated ? 0xCC1A2630 : 0xCC101418);",
     "g.fill(left, top, left + size, top + size,\n"
     "                allocated ? " + T("talentNodeBgAllocated") + " : " + T("talentNodeBg") + ");"),
    ("int frameColor = filled ? TalentTheme.SOCKET_FILLED\n"
     "                : (allocated ? border : TalentTheme.SOCKET_EMPTY);",
     "int frameColor = filled ? TalentTheme.socketFilled()\n"
     "                : (allocated ? border : TalentTheme.socketEmpty());"),
    ("g.outline(left - 2, top - 2, size + 4, size + 4, 0xFFFFFFFF);",
     "g.outline(left - 2, top - 2, size + 4, size + 4, " + T("textBright") + ");"),
    ("g.outline(left - 4, top - 4, size + 8, size + 8, TalentTheme.JEWEL_LIT);",
     "g.outline(left - 4, top - 4, size + 8, size + 8, TalentTheme.jewelLit());"),
])

# ------------------------------------------------------- TalentAllocator
print("[talent] TalentAllocator")
apply(BASE + "screen/talent/TalentAllocator.java", [
    ("import com.cimdy.coe.ui.screen.graph.NodeAllocator;\n",
     "import com.cimdy.coe.ui.screen.graph.NodeAllocator;\n" + THEME),
    ("jewelZones.add(new JewelZone(sp[0], sp[1], r, 0x40C77DFF));",
     "jewelZones.add(new JewelZone(sp[0], sp[1], r, " + T("talentJewelZone") + "));"),
])

# ------------------------------------------- AbstractTalentTreeScreen（54 处）
print("[talent] AbstractTalentTreeScreen")
VMAP = {
    "0xFF0A0A0A": "talentScreenBg",
    "0xFFFFD24A": "talentAccent",
    "0xCC0A0E14": "talentPanelBg",
    "0xFF2A3340": "talentPanelBorder",
    "0xFF88FF88": "talentTextPoints",
    "0xFF555555": "talentEdgeDim",
    "0xFF334455": "talentControlHoverBg",
    "0xFF1A2A38": "talentControlBg",
    "0xFF6FE0A0": "talentOk",
    "0xFF88FFAA": "talentTextSummary",
    "0xFFCCCCCC": "talentTextIdle",
    "0xFFB06020": "talentTabActiveBg",
    "0xFF2A4F70": "talentTabBg",
    "0xFF88CCFF": "talentTabBorder",
    "0xFF1A2E40": "talentTabIdleBg",
    "0xFF223040": "talentArrowIdleBg",
    "0xFFFFFFFF": "textBright",
    "0xFFFF4444": "talentNumActive",
    "0xFFEEEEEE": "talentTextNeutral",
    "0xFFFFE066": "talentBorderAllocated",
    "0xFF6E2A2A": "talentDangerHoverBg",
    "0xFF401E1E": "talentDangerBg",
    "0xFFAA4444": "talentDangerBorder",
    "0xFFFF8888": "talentDangerText",
    "0xFF336699": "talentPrimaryHoverBg",
    "0xFFAAEEFF": "talentPrimaryTextHot",
    "0xFF66CCFF": "talentPreview",
    "0xFF333333": "talentControlTrackBg",
    "0xFFE0C060": "talentAccentDim",
    "0xEE0A0E14": "talentSummaryBg",
    "0xFF888888": "talentTextDim",
    "0xFFCCFFCC": "talentTextSoft",
    "0xFF1A1F26": "talentScrollTrackBg",
}
rel = BASE + "screen/talent/AbstractTalentTreeScreen.java"
text, nl = read_text(rel)
before = 0
for lit, tok in VMAP.items():
    # 只在代码行替换（该文件的裸字面量全在代码行，逐值整体替换即可）
    n = len(re.findall(r"\b" + lit + r"\b", text))
    if n:
        before += n
        text = re.sub(r"\b" + lit + r"\b", T(tok), text)
        print(f"    {lit} -> {tok}()  x{n}")
from b3_apply import write_text as _wt
_wt(rel, text, nl)
print(f"  ok  AbstractTalentTreeScreen: {before} 处")
