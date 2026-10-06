package com.cimdy.coe.ui.widget.component.button;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.ToggleRendererI;

import com.cimdy.coe.ui.widget.component.base.Pressable;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.ToggleStyle;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 开关 / 复选框。一个组件覆盖两种画法：
 *
 * <ul>
 *   <li><b>SWITCH</b>：滑动开关（轨迹 + 滑块，切状态时平滑滑动）；</li>
 *   <li><b>CHECK</b>：勾选框（方盒 + 勾）。</li>
 * </ul>
 *
 * <p>行为来自 {@link Pressable} 的 {@code toggle(true)} 模式：点击翻转 {@code active} 态，
 * 渲染器经 {@code this::isActive} 只读拉取该态，自己不存状态。{@link #styled()} 只负责配色，
 * 不写任何硬编码色值。
 */
public class ToggleWidget extends Pressable {

    /** 画法。 */
    public enum Style { SWITCH, CHECK }

    private Style style = Style.SWITCH;
    private int rounding = 0;
    private final Node switchNode = new Node();
    private final ToggleRendererI visual = Renderers.toggle(this::isActive);
    private TextRendererI labelText;
    private Component labelText0;
    private final Property<Boolean> value = new SimpleObservable<>(false);
    private Consumer<Boolean> onChange;

    public ToggleWidget() {
        this.id = "toggle";
        row();
        flex.alignItems = AlignItems.CENTER;
        switchNode.with(visual);
        add(switchNode);
        applyStyle();
        toggle(true);
        onClick((s, b) -> value.set(isActive()));
    }

    private void applyStyle() {
        if (style == Style.SWITCH) switchNode.flex.size(24, 12);
        else switchNode.flex.size(12, 12);
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    /** 切换画法，并相应调整开关区尺寸。 */
    public ToggleWidget style(Style s) {
        this.style = s;
        visual.style(ToggleStyle.valueOf(s.name()));
        applyStyle();
        return this;
    }

    /** 开关轨道 / 勾选框圆角半径（px）。 */
    public ToggleWidget rounding(int r) {
        this.rounding = Math.max(0, r);
        visual.rounding(this.rounding);
        return this;
    }

    /** 勾选态（也等价 {@link #active}）。 */
    public ToggleWidget checked(boolean b) { active(b); return this; }

    /** 状态变化回调（仅在翻转后触发一次）。 */
    public ToggleWidget onChange(Consumer<Boolean> c) { this.onChange = c; value.addListener((o, n) -> c.accept(n)); return this; }

    /** 在开关右侧加/替换一行文字。 */
    public ToggleWidget label(Component c) {
        this.labelText0 = c == null ? Component.empty() : c;
        if (labelText == null) {
            labelText = Renderers.text(labelText0)
                    .align(Align.LEFT)
                    .vAlign(VAlign.MIDDLE);
            Node ln = new Node();
            ln.flex.grow = 1;
            ln.with(labelText);
            children.add(children.indexOf(switchNode) + 1, ln);
        } else {
            labelText.setLine(0, labelText0);
        }
        return this;
    }

    public ToggleWidget label(String s) { return label(Component.literal(s)); }

    /** 套一套中性默认配色（仅影响可选的文字层）。 */
    public ToggleWidget styled() {
        if (labelText != null) {
            labelText.colorDisabled(Theme.current().textDisabled()).colorHover(Theme.current().textHover());
        }
        return this;
    }

    // ------------------------------------------------------------------
    // 拷贝：重建内部结构，避免克隆出的开关引用到原件的 active 态
    // ------------------------------------------------------------------

    @Override
    public ToggleWidget copy() {
        ToggleWidget t = new ToggleWidget();
        t.flex.copyFrom(flex);
        t.id = id;
        t.visible = visible;
        t.state = state;
        t.style = style;
        t.rounding = rounding;
        t.visual.rounding(rounding);
        t.switchNode.flex.size(switchNode.flex.width, switchNode.flex.height);
        t.visual.style(ToggleStyle.valueOf(style.name()));
        if (onChange != null) t.onChange(onChange);
        if (labelText0 != null) t.label(labelText0);
        return t;
    }
}
