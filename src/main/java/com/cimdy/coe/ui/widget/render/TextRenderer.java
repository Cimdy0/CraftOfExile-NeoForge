package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.StateValue;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本渲染器 —— 单行、多行、自动换行、自动缩放、对齐，全在这一个类里。
 *
 * <p>旧库为此拆了三个类：{@code SimpleTextRender}（单行）、{@code MultiLineTextRender}（多行）、
 * {@code TitleRenderer}（带盒子的标题），三者的缩放与对齐算法各写一遍且互相不一致
 * （多行版不支持 fixedScale，单行版不支持行距，标题版干脆不缩放）。
 * 这里统一为「一组行 + 一套排版策略」，单行只是 {@code lines.size() == 1} 的特例。
 *
 * <p>标题那种「边框盒 + 居中文字」不再需要专门的类 —— 用
 * {@code node.with(ColorRenderer...).with(TextRenderer...)} 叠加即可。
 *
 * <pre>{@code
 * // 单行、居中、悬浮变色
 * TextRenderer.of(Component.literal("确定"))
 *     .colorHover(0xFFFFD700);
 *
 * // 多行、左对齐、行距 2
 * TextRenderer.ofLines(line1, line2, line3)
 *     .align(Align.LEFT)
 *     .lineSpacing(2);
 * }</pre>
 */
class TextRenderer implements Renderer, TextRendererI {

    private final List<StateValue<Component>> lines = new ArrayList<>();
    private StateValue<Integer> color = StateValue.of(0xFFFFFFFF);

    private Align align = Align.CENTER;
    private VAlign vAlign = VAlign.MIDDLE;
    private int lineSpacing = 2;

    /** 单行放不下时是否折行（仅当只有一行时生效） */
    private boolean wrap = false;
    /** 放不下时是否自动缩小 */
    private boolean autoScale = true;
    /** &gt;0 时强制使用该缩放，忽略 autoScale */
    private float fixedScale = 0F;
    private boolean shadow = false;

    private TextRenderer() {}

    // ------------------------------------------------------------------
    // 构造
    // ------------------------------------------------------------------

    public static TextRenderer of(Component text) {
        TextRenderer r = new TextRenderer();
        r.lines.add(StateValue.of(text));
        return r;
    }

    public static TextRenderer of(String text) {
        return of(Component.literal(text));
    }

    public static TextRenderer ofLines(Component... texts) {
        TextRenderer r = new TextRenderer();
        for (Component c : texts) r.lines.add(StateValue.of(c));
        return r;
    }

    public static TextRenderer ofLines(List<Component> texts) {
        TextRenderer r = new TextRenderer();
        for (Component c : texts) r.lines.add(StateValue.of(c));
        return r;
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    /** 追加一行。 */
    public TextRenderer line(Component text) {
        lines.add(StateValue.of(text));
        return this;
    }

    /** 追加一行，并指定其悬浮态文本。 */
    public TextRenderer line(Component text, Component hoverText) {
        lines.add(StateValue.of(text).hovered(hoverText));
        return this;
    }

    /** 设置第一行的悬浮态文本（单行场景的便捷写法）。 */
    public TextRenderer hoverText(Component text) {
        if (!lines.isEmpty()) lines.get(0).hovered(text);
        return this;
    }

    /** 替换指定行的常态文本，越界时忽略。 */
    public TextRenderer setLine(int index, Component text) {
        if (index >= 0 && index < lines.size()) lines.get(index).normal(text);
        return this;
    }

    public TextRenderer color(int argb)         { color.normal(argb);   return this; }
    public TextRenderer colorHover(int argb)    { color.hovered(argb);  return this; }
    public TextRenderer colorActive(int argb)   { color.active(argb);   return this; }
    public TextRenderer colorDisabled(int argb) { color.disabled(argb); return this; }

    public TextRenderer align(Align a)      { this.align = a; return this; }
    public TextRenderer vAlign(VAlign a)    { this.vAlign = a; return this; }
    public TextRenderer lineSpacing(int px) { this.lineSpacing = px; return this; }
    public TextRenderer wrap(boolean b)     { this.wrap = b; return this; }
    public TextRenderer autoScale(boolean b){ this.autoScale = b; return this; }
    public TextRenderer shadow(boolean b)   { this.shadow = b; return this; }

    /** 固定缩放，设置后不再自动缩放。传 0 表示取消固定。 */
    public TextRenderer fixedScale(float s) {
        this.fixedScale = s;
        if (s > 0F) this.autoScale = false;
        return this;
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (lines.isEmpty() || w <= 0 || h <= 0) return;

        VisualState s = ctx.state();
        Font font = Draw.font();

        List<Component> resolved = resolve(s);
        if (resolved.isEmpty()) return;

        // 单行且允许折行时，先按可用宽度拆行
        if (wrap && resolved.size() == 1 && font.width(resolved.get(0)) > w) {
            resolved = split(font, resolved.get(0), w);
        }

        int lineHeight = font.lineHeight;
        int maxWidth = 1;
        for (Component c : resolved) maxWidth = Math.max(maxWidth, font.width(c));
        int totalHeight = resolved.size() * lineHeight + (resolved.size() - 1) * lineSpacing;

        float scale = resolveScale(w, h, maxWidth, totalHeight);
        int scaledTotalHeight = Math.round(totalHeight * scale);

        int cursorY = switch (vAlign) {
            case TOP    -> y;
            case BOTTOM -> y + h - scaledTotalHeight;
            case MIDDLE -> y + (h - scaledTotalHeight) / 2;
        };

        int argb = color.get(s);
        if (ctx.alpha() < 1F) argb = fade(argb, ctx.alpha());
        int step = Math.round((lineHeight + lineSpacing) * scale);

        for (Component c : resolved) {
            int scaledWidth = Math.round(font.width(c) * scale);
            int lineX = switch (align) {
                case LEFT   -> x;
                case RIGHT  -> x + w - scaledWidth;
                case CENTER -> x + (w - scaledWidth) / 2;
            };
            if (shadow) {
                Draw.textScaled(g, c, lineX + 1, cursorY + 1, scale, ctx.alpha() < 1F ? fade(0xFF000000, ctx.alpha()) : 0xFF000000);
            }
            Draw.textScaled(g, c, lineX, cursorY, scale, argb);
            cursorY += step;
        }
    }

    /** 把已烘焙的 alpha 再乘一个 0~1 的淡出系数（动画透明度，alpha=1 时原样返回）。 */
    private static int fade(int argb, float a) {
        if (a >= 1F) return argb;
        int baked = (argb >>> 24) & 0xFF;
        int na = (int) (baked * Math.max(0F, Math.min(1F, a)));
        return (argb & 0x00FFFFFF) | (na << 24);
    }

    /** 取各行在当前状态下的文本，并剔除全空行以外的空内容。 */
    private List<Component> resolve(VisualState s) {
        List<Component> out = new ArrayList<>(lines.size());
        boolean anyNonEmpty = false;
        for (StateValue<Component> sv : lines) {
            Component c = sv.get(s);
            if (c == null) c = Component.empty();
            if (!c.getString().isEmpty()) anyNonEmpty = true;
            out.add(c);
        }
        return anyNonEmpty ? out : List.of();
    }

    /** 把过宽的单行按像素宽度折成多行，保留原有样式。 */
    private static List<Component> split(Font font, Component text, int maxWidth) {
        List<FormattedCharSequence> seqs = font.split(text, maxWidth);
        List<Component> out = new ArrayList<>(seqs.size());
        for (FormattedCharSequence seq : seqs) {
            StringBuilder sb = new StringBuilder();
            seq.accept((pos, style, ch) -> {
                sb.appendCodePoint(ch);
                return true;
            });
            // 保留原 Component 的样式（颜色/粗体等），只替换文字内容
            out.add(Component.literal(sb.toString()).setStyle(text.getStyle()));
        }
        return out;
    }

    private float resolveScale(int w, int h, int contentW, int contentH) {
        if (fixedScale > 0F) return fixedScale;
        if (!autoScale) return 1.0F;
        float sw = (float) w / contentW;
        float sh = (float) h / contentH;
        return Math.min(1.0F, Math.min(sw, sh));
    }

    // ------------------------------------------------------------------
    // 度量与拷贝
    // ------------------------------------------------------------------

    @Override
    public int preferredWidth() {
        Font font = Draw.font();
        int max = 0;
        for (StateValue<Component> sv : lines) {
            Component c = sv.base();
            if (c != null) max = Math.max(max, font.width(c));
        }
        return max;
    }

    @Override
    public int preferredHeight() {
        if (lines.isEmpty()) return 0;
        return lines.size() * Draw.lineHeight() + (lines.size() - 1) * lineSpacing;
    }

    @Override
    public Renderer copy() {
        TextRenderer c = new TextRenderer();
        for (StateValue<Component> sv : lines) c.lines.add(sv.copy(Component::copy));
        c.color = color.copy();
        c.align = align;
        c.vAlign = vAlign;
        c.lineSpacing = lineSpacing;
        c.wrap = wrap;
        c.autoScale = autoScale;
        c.fixedScale = fixedScale;
        c.shadow = shadow;
        return c;
    }
}
