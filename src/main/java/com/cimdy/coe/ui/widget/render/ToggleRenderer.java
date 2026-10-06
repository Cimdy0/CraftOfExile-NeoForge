package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.ToggleStyle;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Supplier;

/**
 * 开关视觉：把 {@code on/off} 两种状态画成「滑动开关」或「勾选框」。
 *
 * <p>状态只经由构造时注入的 {@link Supplier} 拉取（与 {@code TextFieldRenderer} 只读拉取文本同一套路），
 * 渲染器自身不持有任何业务数据。滑块位置用指数缓动逼近目标，所以切状态时是平滑滑动而非瞬移。
 */
class ToggleRenderer implements Renderer, ToggleRendererI {

    private final Supplier<Boolean> on;
    private ToggleStyle style = ToggleStyle.SWITCH;
    private double knob = 0; // 0=关 1=开，仅 SWITCH 风格用
    private int rounding = 0; // 轨道/勾选框圆角半径

    public ToggleRenderer(Supplier<Boolean> on) {
        this.on = on;
    }

    public ToggleRenderer style(ToggleStyle s) { this.style = s; return this; }

    /** 轨道 / 勾选框圆角半径（px）；0 表示直角。 */
    public ToggleRenderer rounding(int r) { this.rounding = Math.max(0, r); return this; }

    @Override
    public boolean isAnimated() { return true; }

    @Override
    public void tick() {
        double target = (on != null && on.get()) ? 1 : 0;
        knob += (target - knob) * 0.3;
        if (Math.abs(target - knob) < 0.01) knob = target;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        boolean onNow = on != null && on.get();
        if (style == ToggleStyle.CHECK) {
            int bs = Math.min(h - 2, 10);
            int bx = x + 1;
            int by = y + (h - bs) / 2;
            int border = onNow ? Theme.current().accent() : Theme.current().slotBorder();
            if (ctx.state().hovered()) border = Theme.lighten(border, 0.15F);
            if (rounding > 0) {
                Draw.roundRectOutline(g, bx, by, bs, bs, rounding, 1, border);
                Draw.roundRect(g, bx + 1, by + 1, bs - 2, bs - 2, Math.max(0, rounding - 1),
                        onNow ? Theme.current().accent() : 0x20000000);
            } else {
                Draw.outline(g, bx, by, bs, bs, border);
                Draw.fill(g, bx + 1, by + 1, bs - 2, bs - 2, onNow ? Theme.current().accent() : 0x20000000);
            }
            if (onNow) {
                int cx = bx + bs / 2, cy = by + bs / 2;
                Draw.fill(g, cx - (bs / 2 - 1), cy - 1, bs - 2, 2, 0xFFFFFFFF);
                Draw.fill(g, cx - 1, cy - (bs / 2 - 1), 2, bs - 2, 0xFFFFFFFF);
            }
        } else {
            int trackH = Math.min(h - 2, 12);
            int ty = y + (h - trackH) / 2;
            // 轨道底色随 knob（已指数缓动逼近开关态）在 off/on 两色间插值 —— 与滑块滑动同步的 ColorLerp
            int track = Draw.blend(Theme.current().toggleTrackOff(), Theme.current().toggleTrackOn(), (float) knob);
            if (ctx.state().hovered()) track = Theme.lighten(track, 0.12F);
            if (rounding > 0) Draw.roundRect(g, x + 1, ty, w - 2, trackH, rounding, track);
            else Draw.fill(g, x + 1, ty, w - 2, trackH, track);
            int knobR = trackH - 2;
            int kx = x + 2 + (int) ((w - 4 - knobR) * knob);
            Draw.disc(g, kx + knobR / 2, ty + 1 + knobR / 2, knobR / 2, Theme.current().toggleThumb());
        }
    }

    @Override
    public Renderer copy() {
        ToggleRenderer c = new ToggleRenderer(on);
        c.style = style;
        c.knob = knob;
        c.rounding = rounding;
        return c;
    }
}
