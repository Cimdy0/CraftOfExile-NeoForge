#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 3 + 4：Toast 语义标记（形状+字形）+ Dark 语义色亮度差 + ToastHost 改预设感知。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"
TOAST = "src/main/java/com/cimdy/coe/ui/widget/component/panel/ToastHost.java"

# ---- 3a) Dark 语义色拉亮度差（改基类默认，Dark 与 Compact 同时受益） ----
print("[P0-3] StylePreset: toast 语义色拉亮度差")
assert_no_collision(["0xFF6FD3F7", "0xFF9AE89A", "0xFFF0B429", "0xFFE76060"])
apply(PRESET, [
    ("""    public int toastInfo()       { return Theme.TOAST_INFO; }
    public int toastSuccess()    { return Theme.TOAST_SUCCESS; }
    public int toastWarn()       { return Theme.TOAST_WARN; }
    public int toastError()      { return Theme.TOAST_ERROR; }""",
     """    // 这四个语义色刻意<b>不再</b>镜像 Theme.TOAST_*：Theme 那组值在 Dark 底上
    // Success/Error 亮度比只有 1.36，deuteranopia 下 ΔE 4.3 会坍缩成同一种土黄色
    // （可访问性 P0-1）。这里把亮度差拉到 2.31；Dark 与 Compact 同时受益，
    // HighContrast / Paper / Midnight 各自覆盖。
    public int toastInfo()       { return 0xFF6FD3F7; }
    public int toastSuccess()    { return 0xFF9AE89A; }
    public int toastWarn()       { return 0xFFF0B429; }
    public int toastError()      { return 0xFFE76060; }"""),
])

# ---- 3b/4) ToastHost：语义标记 + Theme.XXX 静态引用改 Theme.current() ----
print("[P0-3] ToastHost: import StylePreset")
apply(TOAST, [
    ("import com.cimdy.coe.ui.widget.render.Theme;\n",
     "import com.cimdy.coe.ui.widget.render.StylePreset;\nimport com.cimdy.coe.ui.widget.render.Theme;\n"),
])

print("[P0-3] ToastHost: ToastStyle 加字形 + 预设感知 color()")
apply(TOAST, [
    ("""    /** 提示语义：决定左侧强调条与标题颜色（全部取自 {@link Theme}）。 */
    public enum ToastStyle {
        INFO(Theme.TOAST_INFO),
        SUCCESS(Theme.TOAST_SUCCESS),
        WARN(Theme.TOAST_WARN),
        ERROR(Theme.TOAST_ERROR);

        public final int color;
        ToastStyle(int c) { color = c; }
    }""",
     """    /**
     * 提示语义：决定左侧强调条与标记颜色。
     *
     * <p>{@link #color} 保留为 <b>Dark 基准值</b>（历史公开字段，不随换肤变化）；
     * 需要跟随当前预设时一律用 {@link #color()}。
     *
     * <p>每种语义另有<b>字形</b>与<b>形状</b>两重非颜色编码 —— 仅靠颜色区分在
     * 色盲 / 灰度下不可读（可访问性 P0-1）。字形刻意只用 ASCII：MC 默认位图字体对
     * U+2713 / U+2715 等符号码位不保证有字形，缺字形会渲染成方框，比 ASCII 更糟。
     */
    public enum ToastStyle {
        INFO(Theme.TOAST_INFO, "i"),
        SUCCESS(Theme.TOAST_SUCCESS, "v"),
        WARN(Theme.TOAST_WARN, "!"),
        ERROR(Theme.TOAST_ERROR, "x");

        public final int color;
        private final String glyph;

        ToastStyle(int c, String glyph) { color = c; this.glyph = glyph; }

        /** 语义字形（ASCII，默认字体必定有字形）。 */
        public String glyph() { return glyph; }

        /** 当前风格预设下的语义色（预设感知）。 */
        public int color() {
            StylePreset p = Theme.current();
            return switch (this) {
                case INFO    -> p.toastInfo();
                case SUCCESS -> p.toastSuccess();
                case WARN    -> p.toastWarn();
                case ERROR   -> p.toastError();
            };
        }
    }"""),
])

print("[P0-3] ToastHost: ICON 尺寸 + INDENT")
apply(TOAST, [
    ("""        private static final int INDENT = 8;          // 正文相对卡片左边距（含强调条）""",
     """        private static final int ICON = 11;           // 语义标记边长（形状 + 字形）
        private static final int INDENT = 19;         // 正文左边距 = 强调条 3 + 标记 11 + 间隙 5"""),
])

print("[P0-3] ToastHost: render() 改预设感知 + 画语义标记")
apply(TOAST, [
    ("""        void render(GuiGraphicsExtractor g, int x, int y, int w, float alpha) {
            int h = height();
            Draw.fill(g, x, y, w, h, fade(Theme.TOAST_BG, alpha));
            Draw.outline(g, x, y, w, h, fade(Theme.TOAST_BORDER, alpha));
            Draw.fill(g, x, y, BAR, h, fade(style.color, alpha));

            int tx = x + INDENT;
            int ty = y + PAD;
            if (title != null) {
                Draw.text(g, title, tx, ty, fade(Theme.lighten(Theme.TEXT, 0.12f), alpha));
                ty += Draw.lineHeight() + 3;
            }
            int body = fade(Theme.TEXT, alpha);
            for (Component line : lines) {
                Draw.text(g, line, tx, ty, body);
                ty += Draw.lineHeight();
            }
        }""",
     """        void render(GuiGraphicsExtractor g, int x, int y, int w, float alpha) {
            int h = height();
            // 预设感知：静态 Theme.TOAST_* 不经过 StylePreset，换肤对它们无效
            int bg = fade(Theme.current().toastBg(), alpha);
            Draw.fill(g, x, y, w, h, bg);
            Draw.outline(g, x, y, w, h, fade(Theme.current().toastBorder(), alpha));
            int accent = fade(style.color(), alpha);
            Draw.fill(g, x, y, BAR, h, accent);
            drawMark(g, x + BAR + 2, y + (h - ICON) / 2, accent, bg);

            int tx = x + INDENT;
            int ty = y + PAD;
            if (title != null) {
                Draw.text(g, title, tx, ty, fade(Theme.lighten(Theme.current().text(), 0.12f), alpha));
                ty += Draw.lineHeight() + 3;
            }
            int body = fade(Theme.current().text(), alpha);
            for (Component line : lines) {
                Draw.text(g, line, tx, ty, body);
                ty += Draw.lineHeight();
            }
        }

        /**
         * 语义标记：<b>形状 + 字形</b>双重编码（P0-1 主修 —— 纯调色救不了色觉障碍）。
         * 形状只用 fill / disc 现成图元，不依赖字体码位：
         * 实心圆 = INFO、实心方 = SUCCESS、空心方 = WARN、空心圆 = ERROR。
         */
        private void drawMark(GuiGraphicsExtractor g, int mx, int my, int accent, int bg) {
            int cx = mx + ICON / 2;
            int cy = my + ICON / 2;
            int r = ICON / 2;
            boolean round = style == ToastStyle.INFO || style == ToastStyle.ERROR;
            boolean hollow = style == ToastStyle.WARN || style == ToastStyle.ERROR;
            if (round) Draw.disc(g, cx, cy, r, accent);
            else Draw.fill(g, mx, my, ICON, ICON, accent);
            if (hollow) {
                if (round) Draw.disc(g, cx, cy, r - 2, bg);
                else Draw.fill(g, mx + 2, my + 2, ICON - 4, ICON - 4, bg);
            }
            // 字形取反色：实心底用卡片底色，空心底用语义色，两种形态都保证对比度
            String glyph = style.glyph();
            Draw.text(g, glyph, cx - Draw.width(glyph) / 2, cy - 4, hollow ? accent : bg);
        }"""),
])

print("[P0-3] done")
