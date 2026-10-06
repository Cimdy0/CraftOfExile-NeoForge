package com.cimdy.coe.ui.widget.component.bar;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.assoc.AssociationRegistry;
import com.cimdy.coe.ui.widget.component.assoc.Binding;
import com.cimdy.coe.ui.widget.component.assoc.DoubleAssociation;
import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 复合体：把实现了 {@link ScaleCapable} 的刻度原语与实现了 {@link ValueEditable} 的数值原语
 * 封装进一个 {@link Node}，对外暴露一个<b>具名数值关联</b>，用 {@link Binding} 把原语之间的数据流动
 * 与「关联可控加强」全部接线好。
 *
 * <h3>复合体按能力选择原语</h3>
 * 本类构造时持有的是<b>能力接口</b> {@code ScaleCapable scale} 与 {@code ValueEditable editable}
 * （默认实现分别是 {@link Scale} 与 {@link Slider}），而不是具体的 {@code Scale}/{@code Slider} 类——
 * 这正是「基础原语按能力命名、复合体按能力选择」思想的落地：复合体只认能力、不认具体类，
 * 哪天换成另一个实现了同样能力的原语，这里一行都不用改。
 *
 * <h3>封装</h3>
 * 调用方只看到一个 {@code Bar} 节点：加进 Flexbox 布局、设标题、读 / 写 {@link #value()}。
 * 内部它自己持有「刻度」与「滑块」两个原语，并把它们的字段用绑定接起来：
 * <ul>
 *   <li>{@code value ↔ editable.value()} 双向：拖动滑块的把手改的是原语自身（{@link Slider}），
 *       经原语的同步逻辑写回 {@code value} 属性，再由本绑定写回本组件的 {@code value}——
 *       即「原语 → 复合体」的数据交互；</li>
 *   <li>{@code scale.range() ↔ editable.range()} 双向：改刻度的 min / max 会联动滑块的量程，反之亦然。</li>
 * </ul>
 *
 * <h3>具名关联</h3>
 * 本组件把自己的 {@code value} 建成一个<b>具名 {@link DoubleAssociation}</b>（默认名 {@code "bar:<id>"}），
 * 并登记进 {@link AssociationRegistry#global()}。名字相同即同一对象，因此两个 {@code Bar}
 * 用同一个名字就能共享数值。
 *
 * <h3>最小用法（组件间数据交互）</h3>
 * <pre>{@code
 * // 两个条共享同一命名关联 "bar:shared"：
 * Bar a = new Bar("a", "bar:shared").title(Component.literal("A"));
 * Bar b = new Bar("b", "bar:shared").title(Component.literal("B"));
 * // 拖 a 的把手，b 跟着动（反之亦然）—— 这不靠互相持有引用，只靠「同名关联」。
 * }</pre>
 *
 * <p>所有颜色来自 {@link Theme}，零内联取色。本复合体只在内部{@link Slider}里组合它们，
 * 因此其它屏幕用到的公开 API 不受影响。新 {@link Slider}/{@code Bar} 已覆盖可拖数值条；
 * 只读自动拉取的 {@code ProgressBar} 子类在本次去重中已删除。
 */
public class Bar extends Node {

    /** 复合体按能力持有原语：类型为接口，而非具体类。 */
    private final ScaleCapable scale;
    private final ValueEditable editable;

    /** 本组件对外暴露的具名数值关联（可被其它组件用同一名字订阅 / 共享）。 */
    private final DoubleAssociation value;
    private final List<Binding> bindings = new ArrayList<>();

    private final TextRendererI titleText;
    private final Node titleNode;

    public Bar(String id) {
        this(id, "bar:" + id);
    }

    /**
     * @param id        组件实例 id（用于子节点命名）
     * @param assocName 数值关联名；想跨组件共享就给两个实例传同一个名字（如 {@code "bar:shared"}）
     */
    public Bar(String id, String assocName) {
        this.id = "bar:" + id;
        // 具名数值关联：同名即同对象，构成跨组件共享的基础
        this.value = AssociationRegistry.global().getOrCreateDouble(assocName, 0.0);

        // 复合体按能力选择原语——此处选 Scale + Slider 两个具体实现
        this.scale = new Scale(id);
        this.editable = new Slider("bar:" + id);
        this.editable.range().set(this.scale.range().get());   // 初始把刻度的范围灌给滑块

        // (a) value ↔ editable.value() 双向：对外数值关联 ↔ 内部滑块数值
        bindings.add(Binding.bindBidirectional(this.value, editable.value(),
                Function.identity(), Function.identity()));
        // (b) scale.range() ↔ editable.range() 双向：刻度量程 ↔ 滑块量程
        bindings.add(Binding.bindBidirectional(scale.range(), editable.range(),
                Function.identity(), Function.identity()));

        // 可选标题（默认隐藏）
        this.titleText = Renderers.text(Component.empty())
                .align(Align.LEFT)
                .color(Theme.current().text());
        this.titleNode = new Node().with(titleText);
        titleNode.flex.height = 0;     // 占位 0 高，避免无标题时仍留空隙（FlexLayout 不跳过不可见节点）
        titleNode.visible = false;

        column();
        add(titleNode);
        add((Node) scale);
        add((Node) editable);
    }

    /** 当前数值可绑定字段（同时是具名关联的值）。 */
    public Property<Double> value() {
        return value;
    }

    /** 设标题（显示在最上方）。 */
    public Bar title(Component text) {
        titleText.setLine(0, text);
        titleNode.flex.height = 10;
        titleNode.visible = true;
        return this;
    }

    /** 便捷：设置数值量程（透传到内部刻度与滑块）。 */
    public Bar range(double lo, double hi) {
        scale.range().set(new Range(lo, hi));
        return this;
    }

    /** 切到竖直方向（同时作用刻度与滑块）。 */
    public Bar vertical() {
        scale.vertical();
        editable.vertical();
        return this;
    }

    /** 拆除全部绑定（屏幕销毁 / 组件复用时清理监听，避免幽灵联动）。 */
    public void dispose() {
        for (Binding b : bindings) b.unbind();
        bindings.clear();
    }
}
