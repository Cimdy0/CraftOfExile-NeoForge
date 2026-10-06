package com.cimdy.coe.ui.widget.compose.semantic;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.core.FlexStyle;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;

/**
 * 只读的「标签 : 值」一行（语义部件）。
 *
 * <p>是 {@code CoeUi.settingRow}（标签 + 可交互控件）的<b>展示版</b>：左边标签吃掉剩余宽度，
 * 右边值右对齐。把 {@code StockScreen} 里那个 {@code qtyRow}（标签左 + 数量右、固定区域）之类的
 * 手写行收成一个部件，颜色全部来自 {@link Theme}，不内联 ARGB。
 *
 * <p>值既可以是文字（{@link #value(Component)}），也可以是任意节点（{@link #value(Node)}，
 * 比如塞一个迷你进度条 / 图标）。行高默认 12，{@link #condensed()} 压到 10 并整体缩号，
 * 适配密集的「属性表」场景。
 *
 * <p>需要 1:1 还原某个手写行时：{@link #textScale(float)} 统一改标签/值字号，
 * {@link #autoHeight()} 解锁自然高（默认锁 12px），{@link #gap(int)} / {@link #alignItems(AlignItems)}
 * 对齐间距与交叉轴（默认 4 / CENTER）。全部保留既有默认，不影响 {@link #condensed()}。
 *
 * <p>{@link #gap(int)} 与 {@link #alignItems(AlignItems)} 语义与 {@link Node} 同名方法一致。
 *
 * <pre>{@code
 * KeyValue row = new KeyValue()
 *         .label(Component.literal("已存"))
 *         .value(Component.literal("32"))
 *         .condensed();
 * }</pre>
 */
public class KeyValue extends Node {

    private final TextRendererI labelText;
    private final TextRendererI valueText;
    private final Node labelNode;
    private final Node valueNode;
    private Node customValue;

    /** 未设置文本缩放：沿用渲染器默认字号（保证既有调用方行为不变）。 */
    private static final float SCALE_UNSET = -1f;
    private float scale = SCALE_UNSET;

    private Component labelC = Component.empty();
    private Component valueC = Component.empty();

    public KeyValue() {
        this.id = "key-value";
        row();
        flex.height = 12;
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = 4;

        labelText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().textMuted());
        labelNode = new Node().with(labelText);
        labelNode.flex.grow = 1;

        valueText = Renderers.text(Component.empty())
                .align(Align.RIGHT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().textBright());
        valueNode = new Node().with(valueText);
        valueNode.flex.grow = 1;
        valueNode.flex.alignItems = AlignItems.CENTER;

        add(labelNode);
        add(valueNode);
    }

    public KeyValue label(Component c) {
        this.labelC = (c == null) ? Component.empty() : c;
        labelText.setLine(0, labelC);
        return this;
    }

    /** 设值为文字（清除之前可能的自定义节点）。 */
    public KeyValue value(Component c) {
        this.valueC = (c == null) ? Component.empty() : c;
        if (customValue != null) {
            valueNode.remove(customValue);
            customValue = null;
        }
        valueText.setLine(0, valueC);
        return this;
    }

    /** 设值为任意节点（清除文字值，改用自定义内容，如迷你条 / 图标）。 */
    public KeyValue value(Node n) {
        valueText.setLine(0, Component.empty());
        if (customValue != null) valueNode.remove(customValue);
        customValue = n;
        valueNode.add(n);
        return this;
    }

    /** 标签文字色（默认 {@code textMuted()}）。 */
    public KeyValue labelColor(int argb) { labelText.color(argb); return this; }

    /** 值文字色（默认 {@code textBright()}）。 */
    public KeyValue valueColor(int argb) { valueText.color(argb); return this; }

    /** 压成 10px 行高并整体缩号（0.8），适配密集属性表。等价于 {@code textScale(0.8F)} + 行高 10。 */
    public KeyValue condensed() {
        flex.height = 10;
        return textScale(0.8F);
    }

    /** 统一设置标签与值的文本缩放；不调用则沿用渲染器默认字号。 */
    public KeyValue textScale(float s) {
        this.scale = s;
        labelText.fixedScale(s);
        valueText.fixedScale(s);
        return this;
    }

    /** 不锁行高：恢复「由内容/子节点决定」的自然高（{@link FlexStyle#SIZE_UNSPECIFIED}）。 */
    public KeyValue autoHeight() {
        flex.height = FlexStyle.SIZE_UNSPECIFIED;
        return this;
    }

    /** 主轴间距（标签与值之间），语义同 {@link Node#gap(int)}；默认 4。 */
    public KeyValue gap(int px) {
        flex.gapMain = px;
        flex.gapCross = px;
        return this;
    }

    /** 交叉轴对齐，语义同 {@link Node#alignItems(AlignItems)}；默认 CENTER。 */
    public KeyValue alignItems(AlignItems a) {
        flex.alignItems = a;
        return this;
    }

    @Override
    public KeyValue copy() {
        KeyValue k = new KeyValue();
        // flex 是唯一真源：height（含 autoHeight 的 SIZE_UNSPECIFIED）、alignItems、
        // gapMain/gapCross（gap()）都由 copyFrom 一次性带走，无需重复字段
        k.flex.copyFrom(flex);
        k.id = id;
        k.visible = visible;
        k.state = state;
        if (scale != SCALE_UNSET) k.textScale(scale);
        k.label(labelC);
        if (customValue != null) k.value(customValue.copy());
        else k.value(valueC);
        return k;
    }
}
