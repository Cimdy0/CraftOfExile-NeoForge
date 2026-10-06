package com.cimdy.coe.ui.screen;
import com.cimdy.coe.ui.widget.render.Renderers;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.StylePreset;
import com.cimdy.coe.ui.widget.render.StylePresets;
import com.cimdy.coe.ui.widget.screen.WidgetScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 风格预设选择界面（B2 预设系统）。
 *
 * <p>纯客户端只读浏览，直接 {@code setScreenAndShow} 打开（免 Menu / 网络）。
 * 列出 {@link StylePresets} 中全部已加载预设，每一项带一抹「强调色 / 面板色 / 文字色」色板预览；
 * 点选即 {@link Theme#setPresetById} 应用并即时回写激活 id，当前项以 ▶ 标记。
 */
public class StylePickerScreen extends WidgetScreen {

    public StylePickerScreen() {
        super(Component.translatable("ui.coe.style_picker.title"));
    }

    @Override
    protected void buildUi(WindowLayer root) {
        List<StylePreset> presets = StylePresets.all();
        int h = Math.min(440, 84 + presets.size() * 30);
        Window window = CoeUi.panel(Component.translatable("ui.coe.style_picker.title"), 360, h)
                .onClose(this::onClose)
                .center(this.width, this.height);

        Node list = new Node().column();
        list.flex.gapMain = 6;

        String activeId = StylePresets.active().id();
        for (StylePreset p : presets) {
            boolean active = p.id().equals(activeId);

            Node row = new Node().row();
            row.flex.gapMain = 8;
            row.flex.alignItems = AlignItems.CENTER;

            // 色板预览：强调色 / 面板色 / 文字色（放大到 44×22，便于看清配色比例）
            Node swatch = new Node().row();
            swatch.flex.gapMain = 2;
            swatch.flex.size(44, 22);
            swatch.add(new Node().size(14, 22).with(Renderers.color(p.accent())));
            swatch.add(new Node().size(14, 22).with(Renderers.color(p.panelColor())));
            swatch.add(new Node().size(14, 22).with(Renderers.color(p.text())));
            row.add(swatch);

            Component label = Component.literal((active ? "▶ " : "") + p.displayName());
            Button btn = CoeUi.button(label)
                    .onClick(() -> Theme.setPresetById(p.id()));
            btn.flex.grow = 1;
            row.add(btn);

            list.add(row);
        }

        // 底部说明：切换即时生效，存档需重新打开窗口
        Node note = new Node().with(Renderers.text(Component.translatable("ui.coe.style_picker.note"))
                .align(com.cimdy.coe.ui.widget.render.Align.LEFT)
                .vAlign(com.cimdy.coe.ui.widget.render.VAlign.MIDDLE)
                .color(com.cimdy.coe.ui.widget.render.Theme.TEXT_DISABLED));
        note.flex.height = 14;
        list.add(note);

        window.add(list);
        root.add(window);
    }
}
