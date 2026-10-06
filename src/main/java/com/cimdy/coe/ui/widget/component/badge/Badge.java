package com.cimdy.coe.ui.widget.component.badge;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.ColorRendererI;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

/**
 * Small status badge: a rounded pill with an optional text label, normally
 * overlaid on another component (e.g. a notification count on an icon button).
 *
 * <p>It is a plain {@link Node}, so it can be added as a child and positioned
 * with flex or absolute coordinates like any other widget.</p>
 */
public class Badge extends Node {

    private TextRendererI label;
    private ColorRendererI bg;

    public Badge() {
        this.id = "badge";
        row();
        flex.alignItems = AlignItems.CENTER;
        bg = Renderers.color(Theme.current().toastError()).rounding(6);
        with(bg);
        label = Renderers.text(Component.empty())
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().textBright())
                .autoScale(false);
        with(label);
        padding(1, 4);
        flex.height = 12;
    }

    public static Badge of(Component text) {
        return new Badge().text(text);
    }

    /** Replace the label text. */
    public Badge text(Component c) {
        label.setLine(0, c == null ? Component.empty() : c);
        return this;
    }

    /** Replace the pill background color. */
    public Badge color(int argb) {
        ColorRendererI nb = Renderers.color(argb).rounding(6);
        int i = visuals.indexOf(bg);
        if (i >= 0) visuals.set(i, nb);
        else visuals.add(nb);
        bg = nb;
        return this;
    }

    public Badge rounding(int r) { bg.rounding(r); return this; }

    @Override
    public Badge tooltip(Component... lines) { super.tooltip(lines); return this; }

    @Override
    public Node copy() {
        Badge b = new Badge();
        copyPropsInto(b);
        if (label != null) {
            int i = visuals.indexOf(label);
            if (i >= 0 && i < b.visuals.size() && b.visuals.get(i) instanceof TextRendererI t) b.label = t;
        }
        int j = visuals.indexOf(bg);
        if (j >= 0 && j < b.visuals.size() && b.visuals.get(j) instanceof ColorRendererI c) b.bg = c;
        return b;
    }
}
