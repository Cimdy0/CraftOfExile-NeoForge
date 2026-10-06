#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 Batch A：widget/component/** 组件 chrome 层。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

# ---- 1) StylePreset：新增 UI 独立色令牌（组件 chrome） ----
ANCHOR = """    public int pagerBarBg()      { return Theme.PAGER_BAR_BG; }

    // ==================================================================
    // 令牌层：间距 / 圆角 / 字号 / 阴影 —— 布局的统一计量单位
"""

NEW_TOKENS = """    public int pagerBarBg()      { return Theme.PAGER_BAR_BG; }

    // ==================================================================
    // UI 独立色令牌（工作流 B3 迁移）
    // ------------------------------------------------------------------
    // 这些色既不是 {@link Theme} 规范常量、又在 UI 里被反复硬编码，换肤时它们
    // 「纹丝不动」正是预设覆盖不全的根因。这里给它们语义化名字与默认值：
    // 默认值 = 迁移前的裸字面量，因此 Dark 预设零视觉回归；其余预设覆盖后，
    // 这批组件才真正跟随换肤。
    // ==================================================================

    /** 纯白高亮文字（徽标 / 强调数值）。 */
    public int textBright()         { return 0xFFFFFFFF; }
    /** 全透明底：只想保留状态色（悬浮/选中）而不要常态底的按钮 / 列表行。 */
    public int surfaceTransparent() { return 0x00000000; }
    /** 模态遮罩（{@code WindowLayer} 的 scrim）；0 表示不画遮罩。 */
    public int scrim()              { return 0x80000000; }
    /** 次级文字（窗口关闭按钮常态等），明度介于 {@code text()} 与 {@code textDisabled()} 之间。 */
    public int textMuted()          { return 0xFFAAAAAA; }
    /** 危险 / 破坏性语义文字（关闭按钮悬浮、删除确认）。 */
    public int textDanger()         { return 0xFFFF6666; }

    // ==================================================================
    // 令牌层：间距 / 圆角 / 字号 / 阴影 —— 布局的统一计量单位
"""

print("[Batch A] StylePreset 新令牌")
assert_no_collision(["0xFFFFFFFF", "0x00000000", "0x80000000", "0xFFAAAAAA", "0xFFFF6666"])
apply(PRESET, [(ANCHOR, NEW_TOKENS)])

# ---- 2) 组件 chrome：裸字面量 -> 令牌 ----
print("[Batch A] widget/component/badge/Badge.java")
apply("src/main/java/com/cimdy/coe/ui/widget/component/badge/Badge.java", [
    (".color(0xFFFFFFFF)", ".color(Theme.current().textBright())"),
])

print("[Batch A] widget/component/list/OptionListPopup.java")
apply("src/main/java/com/cimdy/coe/ui/widget/component/list/OptionListPopup.java", [
    ("ColorRenderer.of(0x00000000)", "ColorRenderer.of(Theme.current().surfaceTransparent())"),
])

print("[Batch A] widget/component/menu/ContextMenu.java")
apply("src/main/java/com/cimdy/coe/ui/widget/component/menu/ContextMenu.java", [
    ("ColorRenderer.of(0x00000000)", "ColorRenderer.of(Theme.current().surfaceTransparent())"),
])

print("[Batch A] widget/component/panel/Window.java")
apply("src/main/java/com/cimdy/coe/ui/widget/component/panel/Window.java", [
    ("closeButton.labelRenderer().color(0xFFAAAAAA).colorHover(0xFFFF6666);",
     "closeButton.labelRenderer()\n                            .color(Theme.current().textMuted())\n                            .colorHover(Theme.current().textDanger());"),
])

print("[Batch A] widget/component/panel/WindowLayer.java")
apply("src/main/java/com/cimdy/coe/ui/widget/component/panel/WindowLayer.java", [
    ("import com.cimdy.coe.ui.widget.render.RenderContext;\n",
     "import com.cimdy.coe.ui.widget.render.RenderContext;\nimport com.cimdy.coe.ui.widget.render.Theme;\n"),
    ("private int scrimColor = 0x80000000;",
     "private int scrimColor = Theme.current().scrim();"),
])

print("[Batch A] done")
