package com.cimdy.coe.ui.widget.component.menu;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Top menu bar: a horizontal strip of labeled entries, each opening a dropdown
 * {@link ContextMenu} when clicked. The dropdown resolves its host
 * {@link WindowLayer} from the entry node tree root, so no explicit layer
 * reference is needed at build time.
 */
public class MenuBar extends Node {

    public MenuBar() {
        this.id = "menu-bar";
        row();
        flex.height = 14;
    }

    /** Add a top-level menu entry that builds its dropdown via the callback. */
    public MenuBar menu(Component label, Consumer<ContextMenu> builder) {
        Button b = Button.of(label).styled().clickSound(true);
        b.flex.height = 14;
        b.padding(0, 6);
        b.onClick(() -> {
            ContextMenu m = ContextMenu.of();
            if (builder != null) builder.accept(m);
            if (m.isEmpty()) return;
            WindowLayer wl = findLayer(b);
            if (wl != null) m.showAt(b, wl);
        });
        add(b);
        return this;
    }

    private static WindowLayer findLayer(Node n) {
        Node r = n.root();
        return r instanceof WindowLayer ? (WindowLayer) r : null;
    }
}
