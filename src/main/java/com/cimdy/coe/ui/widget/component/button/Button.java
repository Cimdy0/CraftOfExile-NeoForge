package com.cimdy.coe.ui.widget.component.button;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.ColorRendererI;

import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.component.base.MouseButton;
import com.cimdy.coe.ui.widget.component.base.Pressable;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * 按钮。行为全部来自 {@link Pressable}，这里只提供「常见外观」的装配捷径。
 *
 * <p>它<b>不预置任何配色</b>：默认造出来是个透明的可点区域。要有外观就显式叠加，
 * 或用 {@link #styled()} 取一套中性默认皮肤。这样做是为了让 lib 不替业务决定视觉风格 ——
 * 旧 {@code AdaptiveButton} 把一套具体配色写死在构造里，换风格只能改库。
 *
 * <pre>{@code
 * Button ok = Button.of(Component.literal("确定"))
 *         .styled()
 *         .onClick(() -> close());
 * ok.flex.size(60, 20);
 * }</pre>
 */
public class Button extends Pressable {

    /** 文字层的引用，方便之后改文案。 */
    private TextRendererI label;
    /** {@code styled()} 装入的底层背景渲染器，便于之后追加圆角/平滑等配置。 */
    private ColorRendererI bgRenderer;

    public Button() {
        this.id = "button";
    }

    // ------------------------------------------------------------------
    // 工厂
    // ------------------------------------------------------------------

    public static Button of(Component text) {
        return new Button().label(text);
    }

    public static Button of(String text) {
        return of(Component.literal(text));
    }

    // ------------------------------------------------------------------
    // 外观装配
    // ------------------------------------------------------------------

    /** 设置/替换文字层，居中显示。 */
    public Button label(Component text) {
        if (label == null) {
            label = Renderers.text(text)
                    .align(Align.CENTER)
                    .vAlign(VAlign.MIDDLE);
            with(label);
        } else {
            label.setLine(0, text);
        }
        return this;
    }

    public Button label(String text) { return label(Component.literal(text)); }

    /** 取文字层做进一步配色/缩放设置；未设过文案时返回 null。 */
    public TextRendererI labelRenderer() { return label; }

    /**
     * 套一套中性默认皮肤（半透明底 + 圆角 + 状态平滑过渡 + 悬浮提亮）。
     * 底色层会插到最底下，不会盖掉已有的文字/图标。
     */
    public Button styled() {
        ColorRendererI bg = Theme.button();
        this.bgRenderer = bg;
        visuals.add(0, bg);
        if (label != null) {
            label.colorDisabled(Theme.current().textDisabled()).colorHover(Theme.current().textHover());
        }
        return this;
    }

    /** 取底层背景渲染器（未 {@code styled()} 时为 null）。 */
    public ColorRendererI bgRenderer() { return bgRenderer; }

    /** 圆角半径（px）。 */
    public Button rounding(int r) { if (bgRenderer != null) bgRenderer.rounding(r); return this; }

    /** 开启 bg/border 色随状态平滑过渡。 */
    public Button smooth() { if (bgRenderer != null) bgRenderer.smooth(true); return this; }

    /** 叠加一层循环扫光带（动态效果）。 */
    public Button shimmer() { with(Theme.shimmer()); return this; }

    /** 叠加一层脉冲发光边框（动态效果，如「可提交」强调）。 */
    public Button glow() { with(Theme.glow()); return this; }

    /** 在最底层插一层背景。 */
    public Button background(Renderer r) {
        visuals.addFirst(r);
        return this;
    }

    /**
     * 整钮复位：清空子节点、外观层，并断开文字层引用，使下一次 {@code label()} 重新建层。
     * {@link com.cimdy.coe.ui.widget.component.list.ListView#clearBeforeBind()} 在每行重绑前调用，
     * 避免「标签只在首绑出现、刷新后消失」这类残留 bug。
     */
    @Override
    public Button reset() {
        super.reset();
        this.label = null;
        return this;
    }

    // ------------------------------------------------------------------
    // 按压缩放反馈（计划四·五：Button 0.94 / 120ms）
    // ------------------------------------------------------------------

    /** 按下时整体微缩，抬起时回弹。纯视觉（AnimatedNode 变换），不改命中区与事件链路。 */
    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        boolean consumed = super.onMouseClicked(lx, ly, button);
        if (consumed) scaleTo(0.94F, 120);
        return consumed;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        boolean r = super.onMouseReleased(lx, ly, button);
        scaleTo(1F, 120);
        return r;
    }

    // ------------------------------------------------------------------
    // 协变覆写：让 Pressable 的 setter 链上还能接着写 Button 的
    // ------------------------------------------------------------------

    @Override public Button onClick(Action a)       { super.onClick(a); return this; }
    @Override public Button onClick(Runnable r)     { super.onClick(r); return this; }
    @Override public Button toggle(boolean b)       { super.toggle(b); return this; }
    @Override public Button active(boolean b)       { super.active(b); return this; }
    @Override public Button clickSound(boolean b)   { super.clickSound(b); return this; }
    @Override public Button accept(MouseButton... b){ super.accept(b); return this; }
    @Override public Button only(MouseButton b)     { super.only(b); return this; }
    @Override public Button acceptAll()             { super.acceptAll(); return this; }

    // ------------------------------------------------------------------
    // 拷贝：ListView 的行是模板的克隆体，这里必须保住按钮身份和文字层引用
    // ------------------------------------------------------------------

    @Override
    public Button copy() {
        Button b = new Button();
        copyInto(b);
        // visuals 已由父类逐个 copy()，把 label / bgRenderer 重新指向克隆出来的那一层，
        // 否则 binder 里调 label(...) 会改到模板上，或 rounding()/smooth() 改到模板渲染器。
        if (label != null) {
            int i = visuals.indexOf(label);
            if (i >= 0 && i < b.visuals.size() && b.visuals.get(i) instanceof TextRendererI t) {
                b.label = t;
            }
        }
        if (bgRenderer != null) {
            int i = visuals.indexOf(bgRenderer);
            if (i >= 0 && i < b.visuals.size() && b.visuals.get(i) instanceof ColorRendererI cr) {
                b.bgRenderer = cr;
            }
        }
        return b;
    }
}
