package com.cimdy.coe.ui.widget.component.menu;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.base.Scrollable;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.FlexDirection;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Right-click style context menu: a floating popup of labeled actions with
 * optional separators. Distinct from OptionListPopup (a pure list/selector) in
 * that items carry arbitrary Runnables and the menu dismisses on outside click
 * or escape.
 *
 * <pre>{@code
 * ContextMenu.of()
 *     .item(Component.literal("Rename"), this::rename)
 *     .separator()
 *     .item(Component.literal("Delete"), this::delete)
 *     .showAt(mouseX, mouseY, ui.layer());
 * }</pre>
 */
public class ContextMenu extends Window {

    private static final int ROW = 12;

    private final Scrollable list;
    private int maxLabelW = 0;

    public ContextMenu() {
        this.id = "context-menu";
        draggable(false);
        list = scrollBody();
        list.itemGap(1);
        list.scrollBar(false);
        popupStyle();
    }

    public static ContextMenu of() { return new ContextMenu(); }

    /** Add an actionable row. */
    public ContextMenu item(Component label, Runnable action) {
        Button b = Button.of(label).styled().clickSound(true);
        b.flex.height = ROW;
        b.padding(0, 6);
        if (b.labelRenderer() != null) {
            b.labelRenderer().align(Align.LEFT).vAlign(VAlign.MIDDLE).colorHover(Theme.current().textHover());
        }
        b.background(Renderers.color(Theme.current().surfaceTransparent()).backgroundHover(Theme.current().optionHoverBg()));
        b.onClick(() -> {
            if (action != null) action.run();
            close();
        });
        list.add(b);
        maxLabelW = Math.max(maxLabelW, Draw.width(label));
        return this;
    }

    /** Add a non-interactive separator line. */
    public ContextMenu separator() {
        Node sep = new Node();
        sep.flex.height = 1;
        sep.margin(2, 4, 2, 4);
        sep.with(Renderers.color(Theme.current().titleBarBg()));
        list.add(sep);
        return this;
    }

    public boolean isEmpty() { return list.content.children.isEmpty(); }

    /** Neutral popup background + vertical flow. */
    public ContextMenu popupStyle() {
        background(Theme.popup());
        flex.direction = FlexDirection.COLUMN;
        return this;
    }

    private void autoSize() {
        int rows = list.content.children.size();
        flex.height = Math.max(rows * (ROW + 1) + 3, ROW + 4);
        flex.width = Math.min(240, Math.max(90, maxLabelW + 16));
    }

    /** Show at a screen coordinate as a dismissible modal. */
    public ContextMenu showAt(int mouseX, int mouseY, WindowLayer layer) {
        autoSize();
        Minecraft mc = Minecraft.getInstance();
        int sw = (mc != null && mc.getWindow() != null) ? mc.getWindow().getGuiScaledWidth() : mouseX + flex.width + 4;
        int sh = (mc != null && mc.getWindow() != null) ? mc.getWindow().getGuiScaledHeight() : mouseY + flex.height + 4;
        int x = Math.min(mouseX, sw - flex.width - 2);
        int y = Math.min(mouseY, sh - flex.height - 2);
        position(Math.max(2, x), Math.max(2, y));
        layer.add(this);
        layer.bringToFront(this);
        layer.openModal(this);
        return this;
    }

    /** Show anchored below the given node. */
    public ContextMenu showAt(Node anchor, WindowLayer layer) {
        return showAt(anchor.absX, anchor.absY + anchor.height + 1, layer);
    }
}
