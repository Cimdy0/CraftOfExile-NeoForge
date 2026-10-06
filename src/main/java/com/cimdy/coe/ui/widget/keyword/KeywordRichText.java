package com.cimdy.coe.ui.widget.keyword;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * 钉在 HUD 上的关键字窗口正文：把一段<b>已编译</b>的描述（{@link ParsedLine} 列表，
 * 由 {@link KeywordRegistry#compileDescription} 产出）渲染成「关键字高亮文本」，
 * 并把自己最后一帧的布局（含关键字绝对矩形）暴露给 {@link KeywordTooltipHost}，
 * 由宿主统一绘制子级盒子 —— 这样钉住的窗口内也能递归钻取更深的关键字。
 *
 * <p>C.9：正文直接吃 {@link ParsedLine}（片段数组编译结果），引用片段的结构化命中已在编译期登记为
 * 可点跨度；本类不再对原始 Component 二次解析，保证钉窗内的句内引用与瞬态链同源同质量。
 */
public class KeywordRichText extends Node {

    private List<ParsedLine> parsed = List.of();
    private BoxLayout lastLayout;

    public KeywordRichText() { this.id = "kw-richtext"; }

    /** 注入已编译的描述行（渲染期不再二次解析）。 */
    public KeywordRichText parsed(List<ParsedLine> lines) {
        this.parsed = (lines == null) ? List.of() : lines;
        int[] d = KeywordTooltipBox.measureParsed(this.parsed);
        this.flex.size(d[0], d[1]);
        return this;
    }

    /** 供宿主读取本帧布局（子级锚定 + 命中测试）。 */
    public BoxLayout lastLayout() { return lastLayout; }

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        if (parsed.isEmpty()) { lastLayout = null; return; }
        List<ParsedLine> lines = parsed;

        int lineH = Draw.lineHeight();
        int pad = KeywordTooltipBox.PAD;
        BoxLayout box = new BoxLayout(lines);
        box.absX = absX; box.absY = absY;
        int maxW = 0;
        for (ParsedLine p : lines) maxW = Math.max(maxW, p.lineWidth);
        box.width = maxW + pad * 2;
        box.height = lines.size() * lineH + (lines.size() - 1) * KeywordTooltipBox.LINE_GAP + pad * 2;

        for (int i = 0; i < lines.size(); i++) {
            ParsedLine l = lines.get(i);
            int lineY = absY + pad + i * (lineH + KeywordTooltipBox.LINE_GAP);
            Draw.text(g, l.display, absX + pad, lineY, Theme.current().text());
            for (KeywordSpan s : l.spans) {
                int segX = absX + pad + Draw.width(l.text.substring(0, s.charStart));
                int segW = Draw.width(l.text.substring(s.charStart, s.charEnd));
                s.absX = segX; s.absY = lineY; s.w = segW; s.h = lineH;
                box.spans.add(s);
                Draw.text(g, l.text.substring(s.charStart, s.charEnd), segX, lineY, Theme.current().accent());
                Draw.fill(g, segX, s.absY + lineH - 1, segW, 1, Theme.current().accent());
            }
        }
        lastLayout = box;
    }

    @Override
    public Node copy() {
        KeywordRichText n = new KeywordRichText();
        n.parsed = this.parsed;
        return n;
    }
}
