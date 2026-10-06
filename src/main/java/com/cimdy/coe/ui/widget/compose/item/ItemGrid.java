package com.cimdy.coe.ui.widget.compose.item;

import com.cimdy.coe.ui.widget.anim.HoverLerp;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.flex.enums.FlexWrap;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.screen.CoeUi;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 通用物品格：一张「固定列数 × 固定格宽」的物品网格。
 *
 * <p>这是计划 §二·六「物品选择/网格」的<b>唯一实现点</b>——存量条目行、切割网格、
 * CodexDB 物品行、{@link com.cimdy.coe.ui.widget.compose.inventory.MiniInventory} 都复用它的
 * 「槽位 + 图标 + 数量角标 + hover tooltip + hover 提亮」渲染原语，不在各屏重画第二遍。
 *
 * <p><b>数据无关</b>：只吃 {@link ItemStack} / {@link Component} / {@link Consumer}，
 * 不认识 Site / Stock / CodexDB 等任何业务字段。Screen 传入条目与回调，本类只负责画与命中。
 *
 * <pre>{@code
 * ItemGrid grid = ItemGrid.of(9, Theme.SLOT_SIZE)
 *         .entries(entries)                 // List<Entry>，可含空栈（画空槽）
 *         .onPick(entry -> openConfig(entry));
 * }</pre>
 *
 * <p>动画（计划四·五 / 规格 §3）：槽位 hover 提亮 100ms，用 {@link HoverLerp} 驱动
 * 槽底向白色提亮（{@link Theme#lighten}），可中断、反向立即回弹。动画只改渲染颜色，
 * 不改 {@code absX/absY/w/h}，命中区与视觉始终一致。
 */
public class ItemGrid extends Node {

    /** 两槽之间不留缝（与 MC 物品栏一致，边框相邻同色不产生双线）。 */
    private static final int GAP = 0;

    private final int cols;
    private final int cellSize;
    private final List<Entry> entries = new ArrayList<>();
    private Consumer<Entry> onPick;
    /** 规格 §3：tooltip 混淆判定谓词，默认恒 false（混淆关闭）。 */
    private Predicate<Entry> obfuscateIf = e -> false;

    private ItemGrid(int cols, int cellSize) {
        this.cols = Math.max(1, cols);
        this.cellSize = Math.max(8, cellSize);
        this.id = "item-grid";
        row();
        wrap(FlexWrap.WRAP);
        gap(GAP);
        // 固定列数：网格自身宽度锁死为 cols*cellSize，wrap 才会恰好每行折回 cols 个
        flex.width = this.cols * this.cellSize;
        flex.grow = 0;
        flex.shrink = 0;
    }

    public static ItemGrid of(int cols, int cellSize) {
        return new ItemGrid(cols, cellSize);
    }

    /** 设置条目（传 {@code null} 视为空列表）；空栈条目仍画一个空槽框。 */
    public ItemGrid entries(List<Entry> entries) {
        this.entries.clear();
        if (entries != null) this.entries.addAll(entries);
        rebuild();
        return this;
    }

    /** 点击某个格（含空槽）时回调该条目。 */
    public ItemGrid onPick(Consumer<Entry> pick) {
        this.onPick = pick;
        return this;
    }

    /**
     * 加法式：命中谓词的条目，其 hover tooltip 走 {@link ObfuscatedTooltip#obfuscate} 未鉴定样式。
     *
     * <p>混淆判定条件待数据侧确认，默认关闭（恒 false）；传 {@code null} 复位为默认。
     * 不改既有字段 / 签名。
     */
    public ItemGrid obfuscateIf(Predicate<Entry> p) {
        this.obfuscateIf = p == null ? (e -> false) : p;
        return this;
    }

    private void rebuild() {
        clearChildren();
        for (Entry e : entries) {
            add(new Cell(e == null ? Entry.EMPTY : e));
        }
    }

    // ------------------------------------------------------------------
    // 单个格子：自绘槽位 + 物品 + 数量角标 + hover 提亮 + tooltip + 点击
    // ------------------------------------------------------------------

    private final class Cell extends Node {
        private final Entry entry;
        /** 规格 §3：槽位 hover 提亮 100ms。 */
        private final HoverLerp hover = new HoverLerp(100);

        Cell(Entry entry) {
            this.entry = entry;
            this.id = "item-cell";
            flex.size(cellSize, cellSize);
            flex.grow = 0;
            flex.shrink = 0;
            if (entry.name() != null) {
                // 命中混淆谓词 → 未鉴定样式；否则原样显悬浮名
                this.tooltip = () -> obfuscateIf.test(entry)
                        ? ObfuscatedTooltip.obfuscate(List.of(entry.name()))
                        : List.of(entry.name());
            }
        }

        @Override
        protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
            // 每帧喂 hover 态：进入/离开各自 100ms，可中断立即回弹
            hover.update(ctx.hovered());

            // 槽底：常态 slotBg，hover 时向白色提亮一档（取色全走 Theme 令牌，不写死色值）
            int slotBg = Theme.current().slotBg();
            float a = hover.amount();
            int bg = a > 0.001F ? Theme.lighten(slotBg, 0.18F * a) : slotBg;
            Draw.fill(g, absX, absY, width, height, bg);
            Draw.outline(g, absX, absY, width, height, Theme.current().slotBorder());

            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty()) return;   // 空条目：只显空槽框

            // 物品图标：内缩 1px 画（18px 槽里画 16px 图标，与原版槽一致）
            int box = Math.max(1, width - 2);
            Draw.item(g, stack, absX + 1, absY + 1, box);

            // 数量角标：SCALE_BADGE 字号，右下
            if (stack.getCount() > 1) {
                Component num = Component.literal(Integer.toString(stack.getCount()));
                float scale = CoeUi.SCALE_BADGE;
                int tw = Math.round(Draw.width(num) * scale);
                int th = Math.round(Draw.lineHeight() * scale);
                Draw.textScaled(g, num,
                        absX + width - tw - 1, absY + height - th - 1,
                        scale, 0xFFFFFFFF);
            }
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (button != 0) return false;   // 仅左键
            if (onPick != null) onPick.accept(entry);
            return true;
        }
    }

    // ------------------------------------------------------------------
    // 条目：物品栈 + 悬浮名（调用方注入，组件不认识业务字段）
    // ------------------------------------------------------------------

    public record Entry(ItemStack stack, Component name) {
        /** 空条目占位（空栈、无名）。 */
        public static final Entry EMPTY = new Entry(ItemStack.EMPTY, null);

        public Entry {
            Objects.requireNonNull(stack, "stack must not be null (use ItemStack.EMPTY)");
        }
    }
}
