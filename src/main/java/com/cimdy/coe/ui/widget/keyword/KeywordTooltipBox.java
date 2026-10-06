package com.cimdy.coe.ui.widget.keyword;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键字 tooltip 盒子的几何与绘制。
 *
 * <p>绘制策略：先以原 {@code Component} 整体画一行（保留原 tooltip 的行内配色），
 * 再把被命中的关键字子串以强调色<b>覆盖重绘 + 下划线</b>，这样非关键字部分维持原配色、
 * 关键字部分高亮且可点。命中测试基于每个关键字的绝对矩形。
 */
public final class KeywordTooltipBox {

    private KeywordTooltipBox() {}

    public static final int PAD = 6;
    public static final int LINE_GAP = 2;
    public static final int RADIUS = 4;

    /** 计算盒子几何（含每个关键字的绝对矩形），不绘制。越界则整体翻转到屏幕内。 */
    public static BoxLayout layout(List<ParsedLine> lines, int anchorX, int anchorY, int screenW, int screenH) {
        if (lines == null || lines.isEmpty()) return null;
        int lineH = Draw.lineHeight();
        int contentH = lines.size() * lineH + (lines.size() - 1) * LINE_GAP;
        int boxH = contentH + PAD * 2;
        int boxW = 0;
        for (ParsedLine l : lines) boxW = Math.max(boxW, l.lineWidth);
        boxW += PAD * 2;

        int x = anchorX, y = anchorY;
        if (x + boxW > screenW) x = Math.max(0, anchorX - boxW);
        if (y + boxH > screenH) y = Math.max(0, screenH - boxH);
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        BoxLayout box = new BoxLayout(lines);
        box.absX = x; box.absY = y; box.width = boxW; box.height = boxH;

        for (int i = 0; i < lines.size(); i++) {
            ParsedLine l = lines.get(i);
            int lineY = y + PAD + i * (lineH + LINE_GAP);
            for (KeywordSpan s : l.spans) {
                int segX = x + PAD + Draw.width(l.text.substring(0, s.charStart));
                int segW = Draw.width(l.text.substring(s.charStart, s.charEnd));
                s.absX = segX; s.absY = lineY; s.w = segW; s.h = lineH;
                box.spans.add(s);
            }
        }
        return box;
    }

    public static void draw(GuiGraphicsExtractor g, BoxLayout box) {
        draw(g, box, 1F);
    }

    /**
     * 绘制盒子，按 {@code alpha}（0..1）整体淡入（WindowLayer tooltip delay-fade）。
     * 纯视觉：几何 / 关键字命中矩形不变；{@code alpha=1} 时逐色短路回原绘制，零回归。
     */
    public static void draw(GuiGraphicsExtractor g, BoxLayout box, float alpha) {
        float a = Math.max(0F, Math.min(1F, alpha));
        Draw.shadow(g, box.absX, box.absY, box.width, box.height, RADIUS, 4, fade(Theme.current().shadowSoft(), a));
        Draw.roundRect(g, box.absX, box.absY, box.width, box.height, RADIUS, fade(Theme.current().popupBg(), a));
        Draw.roundRectOutline(g, box.absX, box.absY, box.width, box.height, RADIUS, 1, fade(Theme.current().popupBorder(), a));

        int lineH = Draw.lineHeight();
        for (int i = 0; i < box.lines.size(); i++) {
            ParsedLine l = box.lines.get(i);
            int lineY = box.absY + PAD + i * (lineH + LINE_GAP);
            Draw.text(g, l.display, box.absX + PAD, lineY, fade(Theme.current().text(), a));
            for (KeywordSpan s : l.spans) {
                String sub = l.text.substring(s.charStart, s.charEnd);
                Draw.text(g, sub, s.absX, s.absY, fade(Theme.current().accent(), a));
                Draw.fill(g, s.absX, s.absY + lineH - 1, s.w, 1, fade(Theme.current().accent(), a));
            }
        }
    }

    /** 仅按系数 a 缩放颜色的 alpha 通道（0=全透明，1=原样）；RGB 保持不变，不向黑色压暗。 */
    private static int fade(int argb, float a) {
        if (a >= 1F) return argb;
        if (a <= 0F) return 0x00000000;
        int al = Math.round(((argb >>> 24) & 0xFF) * a);
        return (argb & 0x00FFFFFF) | (al << 24);
    }

    /** 一组盒子里命中的关键字（含子级盒子），用于点击取字。 */
    public static KeywordSpan hit(List<BoxLayout> boxes, int mx, int my) {
        for (BoxLayout b : boxes) {
            if (mx < b.absX || mx >= b.absX + b.width || my < b.absY || my >= b.absY + b.height) continue;
            for (KeywordSpan s : b.spans) {
                if (mx >= s.absX && mx < s.absX + s.w && my >= s.absY && my < s.absY + s.h) return s;
            }
        }
        return null;
    }

    /** 量算一组<b>已编译</b>行所需的盒子尺寸 [w, h]。 */
    public static int[] measureParsed(List<ParsedLine> parsed) {
        int lineH = Draw.lineHeight();
        int w = 0;
        for (ParsedLine p : parsed) w = Math.max(w, p.lineWidth);
        int h = parsed.isEmpty() ? 0 : (parsed.size() * lineH + (parsed.size() - 1) * LINE_GAP + PAD * 2);
        return new int[]{ w + PAD * 2, h };
    }

    /** 仅宽度（用于子盒越界预判）。 */
    public static int measureW(List<ParsedLine> parsed) {
        int w = 0;
        for (ParsedLine p : parsed) w = Math.max(w, p.lineWidth);
        return w + PAD * 2;
    }
}
