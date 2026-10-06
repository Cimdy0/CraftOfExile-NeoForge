package com.cimdy.coe.ui.widget.compose.business;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.compose.CostLine;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.JustifyContent;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * 切割预览复合组件：选中宝石后在右侧展示「产物图标 + 花费行列表（成功率/品质/材料）+ 确认按钮」。
 *
 * <p>装配：一列纵排 —— 上方产物大图标（36px），中部 {@link CostLine} 列表（由调用方注入），
 * 底部右对齐确认按钮。未选中宝石时切换为空态提示。
 *
 * <p>纪律（计划〇·边界）：组件只做「数据注入 + 渲染 + 回调上抛」，<b>不发包</b>。
 * 确认按钮由 Screen 注入，Screen 在其 {@code onClick} 里弹 {@code CoeUi.confirm} 防误触并发起切割；
 * 组件本身不持有任何业务类型，产物是 {@link ItemStack}、花费行是 {@link CostLine}。
 *
 * <p>动画（规格 §3）：产物切换时图标品质 pop（缩放 0.94→1.08→1，EASE_OUT_BACK，200ms）。
 *
 * <p>潜在复用方：CutScreen（唯一当前消费者）；未来任何「合成 / 升级 / 转换」类预览面板。
 */
public class CutPreview extends AnimatedNode {

    /** 产物图标尺寸（px）。 */
    private static final int PRODUCT_SIZE = 36;

    public static CutPreview of() { return new CutPreview(); }

    /** 产物图标节点（品质 pop 动画挂这里）。 */
    private final AnimatedNode productIcon = new AnimatedNode();
    /** 花费行容器（成功率/品质/材料）。 */
    private final Node costList = new Node().column();
    /** 底部按钮行（确认按钮右对齐）。 */
    private final Node footer = new Node().row();
    /** 未选中空态。 */
    private final Node emptyState = new Node();
    private final TextRendererI emptyText;

    /** 可变产物引用：渲染器只读这个 Supplier，更新产物无需重建渲染器。 */
    private final ItemStack[] product = { ItemStack.EMPTY };
    private final Supplier<ItemStack> productSupplier = () -> product[0];

    private CutPreview() {
        id("cut-preview");
        column().gap(4);
        padding(4);
        with(Theme.panel());

        // ---- 产物图标 ----
        productIcon.size(PRODUCT_SIZE, PRODUCT_SIZE);
        productIcon.with(Theme.slot());
        productIcon.with(Renderers.item(productSupplier));

        // ---- 花费行列表 ----
        costList.id("cut-costs");
        costList.gap(2);

        // ---- 底部按钮行 ----
        footer.id("cut-footer");
        footer.justify(JustifyContent.FLEX_END);
        footer.grow(1);

        // ---- 空态 ----
        emptyState.id("cut-empty");
        emptyState.height(44);
        emptyText = Renderers.text(Component.translatable("ui.coe.cut_preview.empty"))
                .align(Align.CENTER)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().placeholderText());
        emptyState.with(emptyText);

        add(productIcon);
        add(costList);
        add(footer);
        add(emptyState);

        showEmpty();
    }

    // ------------------------------------------------------------------
    // 数据注入（冻结签名 §2）
    // ------------------------------------------------------------------

    /** 设置预览产物（空栈即回到未选中空态）。 */
    public CutPreview product(ItemStack stack) {
        product[0] = stack == null ? ItemStack.EMPTY : stack;
        boolean has = !product[0].isEmpty();
        productIcon.visible = has;
        costList.visible = has;
        footer.visible = has;
        emptyState.visible = !has;
        if (has) productIcon.pulse(0.08F, 200); // 品质 pop（规格 §3：品质 pop）
        return this;
    }

    /** 注入花费行列表（成功率/品质/材料）；整体替换，传 null/空即清空。 */
    public CutPreview costLines(List<CostLine> lines) {
        costList.clearChildren();
        if (lines != null) {
            for (CostLine l : lines) {
                if (l != null) costList.add(l);
            }
        }
        return this;
    }

    /** 注入确认按钮（Screen 在其 onClick 里弹 CoeUi.confirm 防误触）。传 null 即隐藏底部按钮行。 */
    public CutPreview confirmButton(Button b) {
        footer.clearChildren();
        if (b != null) footer.add(b);
        return this;
    }

    /** 设置未选中空态提示文案（不传则用默认 translatable）。 */
    public CutPreview empty(Component hint) {
        if (hint != null) emptyText.setLine(0, hint);
        showEmpty();
        return this;
    }

    // ------------------------------------------------------------------

    /** 切回空态：隐藏产物/花费/按钮，只留提示。 */
    private void showEmpty() {
        productIcon.visible = false;
        costList.visible = false;
        footer.visible = false;
        emptyState.visible = true;
    }
}
