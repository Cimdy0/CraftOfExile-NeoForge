package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Supplier;

/**
 * 物品图标渲染器：等比缩放居中绘制，可选数量角标与原生 tooltip。
 *
 * <p>数据无关的关键点：物品由 {@link Supplier} 注入而非直接持有。
 * 这样 lib 不需要知道物品从背包、菜单还是某个业务缓存里来，
 * 同时天然支持「每帧取最新值」，不必再走旧库那套 {@code readData(DataCollect, index)} 回调。
 */
class ItemRenderer implements Renderer, ItemRendererI {

    private final Supplier<ItemStack> source;

    private boolean showCount = true;
    private boolean showTooltip = true;

    public ItemRenderer(Supplier<ItemStack> source) {
        this.source = source;
    }

    /** 固定物品的便捷构造。 */
    public static ItemRenderer of(ItemStack stack) {
        return new ItemRenderer(() -> stack);
    }

    public ItemRenderer showCount(boolean b)   { this.showCount = b; return this; }
    public ItemRenderer showTooltip(boolean b) { this.showTooltip = b; return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        ItemStack stack = source.get();
        if (stack == null || stack.isEmpty()) return;

        // 正方形等比：取短边，居中
        int box = Math.min(w, h);
        int ox = x + (w - box) / 2;
        int oy = y + (h - box) / 2;
        Draw.item(g, stack, ox, oy, box);

        if (showCount && stack.getCount() > 1) {
            Component count = Component.literal(String.valueOf(stack.getCount()));
            float scale = box / 20.0F;
            int tw = Math.round(Draw.width(count) * scale);
            int th = Math.round(Draw.lineHeight() * scale);
            Draw.textScaled(g, count, x + w - tw - 1, y + h - th - 1, scale, 0xFFFFFFFF);
        }

        // 注意：此处不再直调 g.setTooltipForNextFrame(stack)。物品 tooltip 行改由
        // {@link #hoverTooltip()} 供应，经 Node.tooltipAt → collectTooltip → WindowLayer
        // 统一汇入 KeywordTooltipHost 链路（命中关键字即高亮/可点，未命中回退原生组件 tooltip）。
    }

    /**
     * 物品完整 tooltip 行（含词缀 / 宝石数据等模组追加行），交宿主节点汇入关键字链路。
     * 取行口径与 {@code CodexScreen#tooltipSummary} / {@code ItemSearch} 一致；玩家上下文缺失或异常时
     * 返回 {@code null}（不显示），绝不吞异常阻断渲染。是否 hover 由 collectTooltip 命中裁决。
     */
    @Override
    public List<Component> hoverTooltip() {
        if (!showTooltip) return null;
        ItemStack stack = source.get();
        if (stack == null || stack.isEmpty()) return null;
        try {
            return stack.getTooltipLines(Item.TooltipContext.EMPTY,
                    Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int preferredWidth() { return 16; }

    @Override
    public int preferredHeight() { return 16; }

    @Override
    public Renderer copy() {
        ItemRenderer c = new ItemRenderer(source);
        c.showCount = showCount;
        c.showTooltip = showTooltip;
        return c;
    }
}
