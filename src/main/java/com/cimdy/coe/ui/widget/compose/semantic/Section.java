package com.cimdy.coe.ui.widget.compose.semantic;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

/**
 * 带标题的主题卡片容器（语义部件）。
 *
 * <p>把「标题栏 + 隔线 + 主体」这一套被各 Screen 反复手搓的排版收成一个部件：
 * 整个卡片底来自 {@link Theme#current()#panel()}，标题文字与隔线也全部走调色板，
 * 屏幕里从此只写 {@code new Section().title(...).body().add(...)}，不再手写背景 / 边框 / 对齐。
 *
 * <p>典型用法：
 * <pre>{@code
 * Section s = new Section().title(Component.literal("属性"));
 * s.body().add(new KeyValue().label(Component.literal("等级")).value(Component.literal("12")));
 * s.body().add(buttonBar);
 * }</pre>
 *
 * <p>可用 {@link #collapsible()} 让标题栏可点击折叠 / 展开主体（常用于「高级设置」类分组）。
 */
public class Section extends Node {

    private final Node header;
    private final TextRendererI titleText;
    private final Node divider;
    private final Node body;
    private boolean collapsible = false;
    private Component titleC = Component.empty();

    public Section() {
        this.id = "section";
        column();
        // 整张卡片底：面板色 + 圆角边框，全部来自 Theme，零内联 ARGB
        with(Theme.current().panel());

        header = new Node().row();
        header.id = "section-header";
        header.flex.height = 12;
        header.flex.alignItems = AlignItems.CENTER;
        header.padding(0, 4);
        titleText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().text());
        Node titleNode = new Node().with(titleText);
        titleNode.flex.grow = 1;
        header.add(titleNode);

        divider = new Node();
        divider.id = "section-divider";
        divider.flex.height = 1;
        divider.with(Renderers.color(Theme.current().borderDisabled()));

        body = new Node().column();
        body.id = "section-body";
        body.flex.grow = 1;
        body.padding(4);

        add(header);
        add(divider);
        add(body);
    }

    /** 设标题；传 null / 空隐藏整个标题栏（只剩主体）。 */
    public Section title(Component c) {
        this.titleC = (c == null) ? Component.empty() : c;
        boolean has = !titleC.getString().isEmpty();
        titleText.setLine(0, titleC);
        header.visible = has;
        divider.visible = has;
        return this;
    }

    /** 返回主体节点，往里 {@code add} 内容即可。内容随卡片整体换肤。 */
    public Node body() { return body; }

    /** 让标题栏可点击折叠 / 展开主体（默认关闭）。 */
    public Section collapsible() { this.collapsible = true; return this; }

    /** 当前主体是否展开（叠合态下为 false）。 */
    public boolean isExpanded() { return body.visible; }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        // 标题栏命中区：从卡片顶部到隔线为止；仅折叠模式消费这次点击
        if (collapsible && ly >= 0 && ly <= header.height + 1) {
            body.visible = !body.visible;
            return true;
        }
        return false;
    }

    @Override
    public Section copy() {
        Section s = new Section();
        s.flex.copyFrom(flex);
        s.id = id;
        s.visible = visible;
        s.state = state;
        s.collapsible = collapsible;
        s.title(titleC);
        for (Node c : body.children) s.body.add(c.copy());
        return s;
    }
}
