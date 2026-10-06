package com.cimdy.coe.ui.widget.compose;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.Animator;
import com.cimdy.coe.ui.widget.anim.Easing;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.AlignItems;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.VAlign;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * 花费行（第 1 层功能组件，唯一实现点 —— 计划一 CostLine）：货币图标 + 数值。
 *
 * <p>充足时数值取 {@code statusOk}（绿），不足时取 {@code textDanger}（红）；状态切换时
 * 颜色 200ms {@code ColorLerp} 平滑过渡，数值 pop 回弹 200ms（计划四·五动画规格表）。
 *
 * <p>数值格式化规则照搬 {@code TeleportScreen#formatCost}：整数直接取整，否则保留一位小数。
 *
 * <p>颜色令牌全部走 {@link Theme#current()}，不写死色值。
 */
public class CostLine extends AnimatedNode {

    /** 状态色缓动 / 数值 pop 时长（计划四·五：CostLine 200ms）。 */
    private static final int MS = 200;

    private final Node iconNode;
    private final TextRendererI valueText;

    /** 0 = 不足（红），1 = 充足（绿）；200ms 插值。 */
    private final Animator stateLerp = new Animator(1F, 1F, MS, Easing.EASE_IN_OUT_CUBIC);
    private boolean affordable = true;

    private CostLine() {
        this.id = "cost-line";
        row();
        flex.height = 12;
        flex.alignItems = AlignItems.CENTER;
        flex.gapMain = 2;

        iconNode = new Node();
        iconNode.id = "cost-icon";
        iconNode.size(16, 16);
        iconNode.visible = false;
        add(iconNode);

        valueText = Renderers.text(Component.literal("0"))
                .align(Align.RIGHT)
                .vAlign(VAlign.MIDDLE)
                .color(Theme.current().statusOk());
        Node valueNode = new Node().with(valueText);
        valueNode.grow(1);
        add(valueNode);
    }

    public static CostLine of() { return new CostLine(); }

    // ------------------------------------------------------------------
    // 数据
    // ------------------------------------------------------------------

    /** 货币图标（传 {@code null} 隐藏图标位）。 */
    public CostLine icon(ItemStack stack) {
        iconNode.visible = stack != null && !stack.isEmpty();
        iconNode.clearVisuals();
        if (iconNode.visible) {
            iconNode.with(Renderers.item(() -> stack).showTooltip(false).showCount(false));
        }
        return this;
    }

    /** 设花费数值（按 formatCost 规则格式化）。 */
    public CostLine cost(double cost) {
        valueText.setLine(0, Component.literal(formatCost(cost)));
        return this;
    }

    /** 设充足 / 不足态；切换时触发 ColorLerp + 数值 pop（200ms）。 */
    public CostLine affordable(boolean ok) {
        boolean changed = (this.affordable != ok);
        this.affordable = ok;
        stateLerp.retarget(ok ? 1F : 0F);
        if (changed) pulse(0.08F, MS);
        return this;
    }

    // ------------------------------------------------------------------
    // 渲染：每帧按插值结果取色（ColorLerp）
    // ------------------------------------------------------------------

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        int okColor = Theme.current().statusOk();
        int badColor = Theme.current().textDanger();
        valueText.color(Draw.blend(badColor, okColor, stateLerp.value()));
        super.renderSelf(g, ctx);
    }

    @Override
    public void tick() {
        stateLerp.tick();
        super.tick();
    }

    // ------------------------------------------------------------------

    /** TeleportScreen#formatCost 规则照搬：整数取整，否则一位小数。 */
    private static String formatCost(double cost) {
        return cost == Math.floor(cost)
                ? String.valueOf((long) cost)
                : String.format(Locale.ROOT, "%.1f", cost);
    }
}
