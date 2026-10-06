package com.cimdy.coe.ui.widget.compose.semantic;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

/**
 * 一排统一样式的按钮（语义部件）。
 *
 * <p>把「底部操作条 / 横向按钮组」这类手写排布收成一个部件：每个按钮都 {@code styled()} 取中性默认皮肤，
 * 高度、间距、是否等宽、横排 / 竖排都可配。屏幕只声明「哪个标签、点下去干嘛」，
 * 不用自己 new Button + 设尺寸 + 加进 row。颜色全部来自 {@link Theme}，无内联 ARGB。
 *
 * <pre>{@code
 * ButtonBar bar = new ButtonBar().equalWidth()
 *         .add(Component.literal("确定"), this::onOk)
 *         .add(Component.literal("取消"), this::onCancel);
 * }</pre>
 */
public class ButtonBar extends Node {

    private int btnHeight = 16;
    private int gap = 2;
    private boolean equal = false;

    public ButtonBar() {
        this.id = "button-bar";
        row();
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = gap;
    }

    /** 改为纵向排列（按钮上下堆叠）。 */
    public ButtonBar vertical() { column(); return this; }

    /** 按钮间距（px）。 */
    public ButtonBar gap(int px) { this.gap = px; flex.gapMain = px; return this; }

    /** 每个按钮统一高度（px）。 */
    public ButtonBar buttonHeight(int px) { this.btnHeight = Math.max(1, px); return this; }

    /** 按钮等宽瓜分整条（适合「确定 / 取消」这类对等操作）。 */
    public ButtonBar equalWidth() { this.equal = true; return this; }

    /** 追加一个「标签 + 点击回调」的统一样式按钮。 */
    public ButtonBar add(Component label, Runnable onClick) {
        Button b = Button.of(label).styled();
        b.flex.height = btnHeight;
        if (equal) b.flex.grow = 1;
        if (onClick != null) b.onClick(onClick);
        add(b);
        return this;
    }

    /** 追加一个已自建的按钮（需要非默认样式 / 图标时）。也会套用本条的身高与等宽规则。 */
    public ButtonBar add(Button b) {
        b.flex.height = btnHeight;
        if (equal) b.flex.grow = 1;
        add(b);
        return this;
    }

    @Override
    public ButtonBar copy() {
        ButtonBar bar = new ButtonBar();
        bar.flex.copyFrom(flex);
        bar.id = id;
        bar.visible = visible;
        bar.state = state;
        bar.btnHeight = btnHeight;
        bar.gap = gap;
        bar.equal = equal;
        for (Node child : children) bar.add(child.copy());
        return bar;
    }
}
