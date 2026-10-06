package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.StateValue;
import com.cimdy.coe.ui.widget.render.TextView;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/**
 * 文本输入框的文字层渲染器：可见文本、占位符、选区高亮、闪烁光标、输入法预编辑。
 *
 * <p>背景与边框不在这里画 —— 那是 {@link ColorRenderer} / {@link SkinRenderer} 的事，
 * 通过节点的 visuals 列表叠加即可。
 *
 * <p>相比旧 {@code InputBoxRenderer}：
 * <ul>
 *   <li>不再由组件每帧调八个 setter 把状态推进来。渲染器通过 {@link TextView} 只读地
 *       <b>拉取</b>组件状态，两边的数据流向唯一，也不会出现「忘了 set 导致画面滞后一帧」；</li>
 *   <li>焦点态取自 {@link RenderContext#state()}，与其它渲染器口径一致；</li>
 *   <li>不再需要构造时传死 {@code widgetHeight}，高度用布局给的实际值。</li>
 * </ul>
 */
class TextFieldRenderer implements Renderer, TextFieldRendererI {

    private static final long BLINK_INTERVAL = 500L;

    private final TextView view;

    private Component placeholder = Component.empty();
    private StateValue<Integer> textColor = StateValue.of(0xFFFFFFFF);
    private int placeholderColor = 0xFFAAAAAA;
    private int selectionColor = 0x40ADD8E6;
    private int padding = 2;

    private long lastBlink = 0L;
    private boolean caretVisible = true;

    public TextFieldRenderer(TextView view) {
        this.view = view;
    }

    public TextFieldRenderer placeholder(Component c)   { this.placeholder = c; return this; }
    public TextFieldRenderer textColor(int argb)        { textColor.normal(argb); return this; }
    public TextFieldRenderer textColorDisabled(int argb){ textColor.disabled(argb); return this; }
    public TextFieldRenderer placeholderColor(int argb) { this.placeholderColor = argb; return this; }
    public TextFieldRenderer selectionColor(int argb)   { this.selectionColor = argb; return this; }
    public TextFieldRenderer padding(int px)            { this.padding = px; return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        Font font = Draw.font();
        int innerWidth = w - padding * 2;
        if (innerWidth <= 0) return;

        boolean focused = ctx.focused();
        if (ctx.hovered() && view.editable()) {
            g.requestCursor(CursorTypes.IBEAM);
        }
        String raw = orEmpty(view.text());
        String preedit = view.preedit();
        int cursor = Draw.clamp(view.cursorPos(), 0, raw.length());

        // 输入法预编辑串插在光标处参与显示
        String display = (preedit != null && focused)
                ? new StringBuilder(raw).insert(cursor, preedit).toString()
                : raw;

        int safeStart = Draw.clamp(view.displayPos(), 0, display.length());
        String visible = font.plainSubstrByWidth(display.substring(safeStart), innerWidth);

        int color = textColor.get(ctx.state());
        String rendered = visible;
        if (display.isEmpty() && preedit == null) {
            color = placeholderColor;
            rendered = placeholder.getString();
        }

        int textX = x + padding;
        int textY = y + (h - font.lineHeight) / 2;

        // 选区高亮（预编辑期间不画）
        if (focused && preedit == null && view.selectionStart() != view.selectionEnd()) {
            drawSelection(g, font, textX, y, h, visible, safeStart);
        }

        Draw.text(g, rendered, textX, textY, color);

        if (preedit != null && focused) {
            drawPreedit(g, font, textX, y, h, visible, safeStart, cursor, preedit, color);
        } else if (focused) {
            drawCaret(g, font, textX, y, h, visible, safeStart, cursor, raw, color);
        } else {
            caretVisible = false;
        }
    }

    private void drawSelection(GuiGraphicsExtractor g, Font font,
                               int textX, int y, int h, String visible, int safeStart) {
        int lo = Math.min(view.selectionStart(), view.selectionEnd()) - safeStart;
        int hi = Math.max(view.selectionStart(), view.selectionEnd()) - safeStart;
        lo = Draw.clamp(lo, 0, visible.length());
        hi = Draw.clamp(hi, 0, visible.length());
        if (lo >= hi) return;
        int x1 = textX + font.width(visible.substring(0, lo));
        int x2 = textX + font.width(visible.substring(0, hi));
        Draw.fill(g, x1, y + 1, x2 - x1, h - 2, selectionColor);
    }

    private void drawPreedit(GuiGraphicsExtractor g, Font font, int textX, int y, int h,
                             String visible, int safeStart, int cursor, String preedit, int color) {
        int start = cursor - safeStart;
        if (start < 0 || start > visible.length()) return;
        int end = Math.min(start + preedit.length(), visible.length());

        int ux1 = textX + font.width(visible.substring(0, start));
        int ux2 = textX + font.width(visible.substring(0, end));
        // 预编辑串下划线
        Draw.fill(g, ux1, y + h - 2, ux2 - ux1, 1, 0xFFAAAAAA);

        int caretRel = start + view.preeditCursor();
        if (caretRel >= 0 && caretRel <= visible.length()) {
            int cx = textX + font.width(visible.substring(0, caretRel));
            Draw.fill(g, cx, y + 2, 1, h - 4, color);
        }
    }

    private void drawCaret(GuiGraphicsExtractor g, Font font, int textX, int y, int h,
                           String visible, int safeStart, int cursor, String raw, int color) {
        long now = Util.getMillis();
        if (now - lastBlink > BLINK_INTERVAL) {
            caretVisible = !caretVisible;
            lastBlink = now;
        }
        if (!caretVisible) return;

        int rel = cursor - safeStart;
        if (rel < 0 || rel > visible.length()) return;

        int cx = textX + font.width(visible.substring(0, rel));
        boolean atEnd = cursor == raw.length() && view.selectionStart() == view.selectionEnd();
        if (atEnd) {
            // 行尾用下划线式光标，避免与文字重叠
            Draw.fill(g, cx, y + h - 2, font.width("W"), 1, color);
        } else {
            Draw.fill(g, cx, y + 2, 1, h - 4, color);
        }
    }

    private static String orEmpty(String s) { return s == null ? "" : s; }

    @Override
    public boolean isAnimated() { return true; }

    @Override
    public int preferredHeight() { return Draw.lineHeight() + 4; }

    @Override
    public Renderer copy() {
        TextFieldRenderer c = new TextFieldRenderer(view);
        c.placeholder = placeholder;
        c.textColor = textColor.copy();
        c.placeholderColor = placeholderColor;
        c.selectionColor = selectionColor;
        c.padding = padding;
        return c;
    }
}
