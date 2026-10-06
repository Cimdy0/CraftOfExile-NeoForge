package com.cimdy.coe.ui.widget.compose;

import com.cimdy.coe.ui.screen.CoeUi;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.ColorRendererI;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;

/**
 * 只读指派行（第 1 层功能组件，唯一实现点 —— 计划一 WorkerRow）：实体名 + 状态小字。
 *
 * <p>纯展示，不可点（不继承 Pressable）；hover 时底色平滑提亮（{@code optionHoverBg}），
 * hover tooltip 由调用方挂。状态小字走 SCALE_META / {@code textMuted}。
 *
 * <p>站点 / 终端任务窗格共用（计划二·六映射表）。
 */
public class WorkerRow extends Node {

    private final TextRendererI nameText;
    private final TextRendererI statusText;

    private WorkerRow() {
        this.id = "worker-row";
        row();
        flex.height = 14;
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = 4;
        padding(0, 2);

        // hover 平滑高亮底（透明常态）
        ColorRendererI hoverBg = Renderers.color(Theme.current().surfaceTransparent())
                .backgroundHover(Theme.current().optionHoverBg())
                .rounding(Theme.current().radiusSm())
                .smooth(true);
        with(hoverBg);

        nameText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().text());
        Node nameNode = new Node().with(nameText);
        nameNode.grow(1);
        add(nameNode);

        statusText = Renderers.text(Component.empty())
                .align(Align.RIGHT)
                .vAlign(VAlign.MIDDLE)
                .fixedScale(CoeUi.SCALE_META)
                .color(Theme.current().textMuted());
        Node statusNode = new Node().with(statusText);
        add(statusNode);
    }

    public static WorkerRow of() { return new WorkerRow(); }

    /** 实体名。 */
    public WorkerRow name(Component c) {
        nameText.setLine(0, c == null ? Component.empty() : c);
        return this;
    }

    /** 右侧状态小字。 */
    public WorkerRow status(Component c) {
        statusText.setLine(0, c == null ? Component.empty() : c);
        return this;
    }

    @Override
    public WorkerRow tooltip(Component... lines) {
        super.tooltip(lines);
        return this;
    }
}
