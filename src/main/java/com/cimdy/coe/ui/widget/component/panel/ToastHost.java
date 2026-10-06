package com.cimdy.coe.ui.widget.component.panel;

import com.cimdy.coe.ui.widget.component.list.OptionListPopup;
import com.cimdy.coe.ui.widget.core.Animatable;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.StylePreset;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 轻量提示系统（toast / snackbar）。
 *
 * <p>旧库完全没有「轻提示」概念，业务里要么弹个 {@code OptionListPopup} 强占交互，
 * 要么直接往 chat 里塞一行字，前者太重后者太弱。这里补一层<b>非模态、自消失、不抢焦点</b>的提示。
 *
 * <p>设计要点：
 * <ul>
 *   <li>与窗口树<b>解耦</b> —— 由 {@code WidgetHost} 持有一个 {@link ToastHost}，
 *       在整层渲染完之后、最上层绘制，因此永远盖在窗口 / 模态遮罩 / 提示气泡之上；</li>
 *   <li>每条 toast 自管生命周期：{@link Animatable} 负责从右滑入，寿命到了再滑出淡出，
 *       全靠 {@link #tick()} 推进，不需要外部定时器；</li>
 *   <li>数据无关：标题 / 正文都是 {@link Component}，样式只取 {@link Theme} 常量，不引入任何业务类型。</li>
 * </ul>
 *
 * <pre>{@code
 * ui.toasts().success(Component.literal("已保存"));
 * ui.toasts().error(Component.literal("保存失败：磁盘已满"));
 * ui.toasts().push(Component.literal("标题"), List.of(Component.literal("第一行"), Component.literal("第二行")),
 *                  ToastHost.ToastStyle.WARN, 200);
 * }</pre>
 */
public class ToastHost {

    /**
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
        SUCCESS(Theme.TOAST_SUCCESS, "V"),
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
    }

    // ------------------------------------------------------------------
    // 一条 toast
    // ------------------------------------------------------------------

    /** 单条提示：内部状态机（滑入 / 驻留 / 滑出淡出）。 */
    public static final class Toast {
        private static final int FADE = 12;          // 收尾淡出 tick 数
        private static final int ENTER = 6;           // 入场淡入 tick 数
        private static final int PAD = 6;             // 上下内边距
        private static final int BAR = 3;             // 左侧强调条宽度
        private static final int ICON = 13;           // 语义标记边长（形状 + 字形）
        private static final int INDENT = 21;         // 正文左边距 = 强调条 3 + 标记 13 + 间隙 5

        final Component title;                        // 可空
        final List<Component> lines;
        final ToastStyle style;
        final int life;                               // 驻留 tick 数（不含入场）

        private int age = 0;
        private final Animatable slide = new Animatable(0, 0.22); // 0=屏外右侧, 1=归位
        private boolean dismissRequested = false;

        Toast(Component title, List<Component> lines, ToastStyle style, int life) {
            this.title = title;
            this.lines = lines == null ? List.of() : lines;
            this.style = style;
            this.life = life;
        }

        void tick() {
            age++;
            if (dismissRequested) slide.target(0);
            slide.tick();
        }

        float alpha() {
            float entrance = Math.min(1f, age / (float) ENTER);
            float exit;
            if (dismissRequested) exit = (float) slide.value();
            else if (age > life - FADE) exit = Math.max(0f, (life - age) / (float) FADE);
            else exit = 1f;
            return entrance * exit;
        }

        boolean expired() {
            if (dismissRequested) return slide.value() <= 0.02;
            return age >= life;
        }

        int height() {
            int h = PAD * 2;
            if (title != null) h += Draw.lineHeight() + 3;
            h += Math.max(1, lines.size()) * Draw.lineHeight();
            return h;
        }

        void render(GuiGraphicsExtractor g, int x, int y, int w, float alpha) {
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
        }
    }

    // ------------------------------------------------------------------
    // 管理器
    // ------------------------------------------------------------------

    private final List<Toast> toasts = new ArrayList<>();

    private int margin = 12;
    private int gap = 8;
    private int width = 240;
    private int defaultLife = 120; // 20 tick/s → 6 秒

    // ---- 外观配置 ----

    public ToastHost margin(int m) { this.margin = Math.max(0, m); return this; }
    public ToastHost gap(int g)    { this.gap = Math.max(0, g); return this; }
    public ToastHost width(int w)  { this.width = Math.max(40, w); return this; }
    /** 默认驻留时长（tick）。 */
    public ToastHost defaultLife(int ticks) { this.defaultLife = Math.max(1, ticks); return this; }

    // ---- 推入 ----

    public Toast push(Component message) { return push(message, ToastStyle.INFO); }
    public Toast push(Component message, ToastStyle style) { return push(message, style, defaultLife); }
    public Toast push(Component message, ToastStyle style, int lifeTicks) {
        return push(null, List.of(message), style, lifeTicks);
    }
    public Toast push(Component title, List<Component> lines, ToastStyle style) {
        return push(title, lines, style, defaultLife);
    }
    public Toast push(Component title, List<Component> lines, ToastStyle style, int lifeTicks) {
        Toast t = new Toast(title, lines, style, Math.max(1, lifeTicks));
        toasts.add(t);
        return t;
    }

    public Toast info(Component message)    { return push(message, ToastStyle.INFO); }
    public Toast success(Component message) { return push(message, ToastStyle.SUCCESS); }
    public Toast warn(Component message)    { return push(message, ToastStyle.WARN); }
    public Toast error(Component message)   { return push(message, ToastStyle.ERROR); }

    public void clear() { toasts.clear(); }

    public int count() { return toasts.size(); }

    // ---- 驱动 ----

    public void tick() {
        for (Toast t : toasts) t.tick();
        toasts.removeIf(Toast::expired);
    }

    /** 整层渲染完之后调用，画在最上层。 */
    public void render(GuiGraphicsExtractor g, int screenW, int screenH, float partialTick) {
        if (toasts.isEmpty()) return;
        int baseX = screenW - margin - width;
        int y = screenH - margin;
        // 最新的（列表末尾）贴底，旧的向上堆叠
        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            int h = t.height();
            y -= h;
            int dx = (int) Math.round((1 - t.slide.value()) * (width + margin + 6));
            t.render(g, baseX + dx, y, width, t.alpha());
            y -= gap;
        }
    }

    // ------------------------------------------------------------------

    /** 把已烘焙的 alpha 再乘一个 0~1 的淡入淡出系数。 */
    private static int fade(int argb, float a) {
        if (a >= 1f) return argb;
        int baked = (argb >>> 24) & 0xFF;
        int na = (int) (baked * Math.max(0f, Math.min(1f, a)));
        return (argb & 0x00FFFFFF) | (na << 24);
    }
}
