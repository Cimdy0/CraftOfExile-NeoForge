package com.cimdy.coe.ui.widget.compose.composite;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 详情面板：一块只读的多行文本区，可被任意来源随时 {@link #show(Component...)} 刷新。
 *
 * <p>终端界面里「悬停实体 → 右侧信息区」这种主从联动，以前是把 {@link TextRenderer}
 * 直接挂在 Window 里、手写 {@code showInfo(List<Component>)} 循环 {@code setLine}。这里把它
 * 收成一个组件：支持任意多行、自动折行、左顶对齐，且内容变更时整体重建文本（不残留旧行）。
 *
 * <p>组件本身不持有任何业务类型 —— 它只认 {@link Component}（MC 文本），所以谁都能往里灌数据。
 */
public class DetailPanel extends Node {

    private final AnimatedNode content = new AnimatedNode();
    private TextRendererI text;

    public DetailPanel() {
        this.id = "detail-panel";
        this.column();
        this.with(Theme.panel());
        this.padding(4);

        content.column();
        content.flex.grow = 1;
        text = defaultText(Component.empty());
        content.with(text);
        add(content);
    }

    /** 用给定行覆盖内容。传空即清空。 */
    public DetailPanel show(Component... lines) {
        return show(List.of(lines));
    }

    public DetailPanel show(List<Component> lines) {
        content.clearVisuals();
        text = defaultText(lines);
        content.with(text);
        // 内容切换淡入 120ms（计划 DetailPanel 换条目淡入）：从透明起渐显，面板底色不动
        content.anim().alpha = 0F;
        content.fadeTo(1F, 120);
        return this;
    }

    public DetailPanel clear() {
        return show(List.of());
    }

    private static TextRendererI defaultText(Component line) {
        return Renderers.textLines(line)
                .align(Align.LEFT)
                .vAlign(VAlign.TOP)
                .lineSpacing(4)
                .wrap(true)
                .autoScale(false);
    }

    private static TextRendererI defaultText(List<Component> lines) {
        return Renderers.textLines(lines)
                .align(Align.LEFT)
                .vAlign(VAlign.TOP)
                .lineSpacing(4)
                .wrap(true)
                .autoScale(false);
    }
}
