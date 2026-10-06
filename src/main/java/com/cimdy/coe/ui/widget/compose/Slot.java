package com.cimdy.coe.ui.widget.compose;
import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.render.ItemRendererI;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextRendererI;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.render.VAlign;
import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * 物品槽：一个样式化的可点方块，中间显示一件物品、可选角落叠一层文字。
 *
 * <p>把 {@code SkillScreen.slotButton} / {@code StockScreen} 的物品格 / {@code TerminalScreen}
 * 的库存格里「{@code new Button() + 深色底色 + ItemRenderer}」这套重复构造收成一个组件。
 * 仍是 Button（点击 / 左右键分发照常可用），只是把「长什么样」封装好。
 *
 * <pre>{@code
 * Slot s = new Slot().size(18).styledSlot();
 * s.item(someStack);              // 设物品（空则只显槽框）
 * s.overlay(Component.literal("Lv3"));  // 可选角落文字
 * s.onClick((src, btn) -> onPick(btn));
 * }</pre>
 */
public class Slot extends Button {

    /** 可变物品引用：渲染器只读这个 Supplier，更新物品只需改引用，不必重建渲染器。 */
    private ItemStack current = ItemStack.EMPTY;
    private final Supplier<ItemStack> supplier = () -> current;

    private ItemRendererI itemLayer;
    private TextRendererI overlayText;

    public Slot() {
        this.id = "slot";
    }

    public Slot size(int s) {
        flex.size(s, s);
        return this;
    }

    /** 套一套深色槽底 + 细边（与各界面既有的槽外观一致）。 */
    public Slot styledSlot() {
        background(Theme.slot());
        return this;
    }

    /** 设置槽中物品（空栈则只显槽框，不画物品）。 */
    public Slot item(ItemStack stack) {
        return item(() -> stack);
    }

    public Slot item(Supplier<ItemStack> stack) {
        if (itemLayer == null) {
            itemLayer = Renderers.item(supplier).showTooltip(true);
            with(itemLayer);
        }
        ItemStack next = stack.get();
        // put-pop（附录 F 缺口 1：Slot 物品放入 pop 200ms）：内容换成新物品时强调回弹一次，
        // 缩放 1 → 1.08 → 1、回程 EASE_OUT_BACK，时长读 AnimRegistry.ms(VALUE) 令牌。
        // 仅按「物品 + 组件」比对：同物堆叠（仅数量变化）不重复弹；高频连放时新 put 直接
        // pulse 顶掉进行中的旧补间（Animator 既有打断语义，不排队）。纯视觉，不改命中区；
        // hover 边框辉光沿用既有基底（ItemGrid.Cell HoverLerp），此处不重复实现。
        if (next != null && !next.isEmpty() && !ItemStack.isSameItemSameComponents(current, next)) {
            pulse(0.08F, AnimRegistry.ms(AnimRegistry.Trigger.VALUE));
        }
        current = next == null ? ItemStack.EMPTY : next;
        return this;
    }

    /** 角落叠一层文字（默认右下、缩小，适合标等级 / 数量）。 */
    public Slot overlay(Component text) {
        if (overlayText == null) {
            overlayText = Renderers.text(text == null ? Component.empty() : text)
                    .align(Align.RIGHT)
                    .vAlign(VAlign.BOTTOM)
                    .fixedScale(0.5F);
            with(overlayText);
        } else {
            overlayText.setLine(0, text == null ? Component.empty() : text);
        }
        return this;
    }

    @Override
    public Slot reset() {
        super.reset();
        this.current = ItemStack.EMPTY;
        this.itemLayer = null;
        this.overlayText = null;
        return this;
    }
}
