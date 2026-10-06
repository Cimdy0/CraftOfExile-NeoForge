package com.cimdy.coe.ui.widget.compose.inventory;

import com.cimdy.coe.ui.widget.anim.AnimRegistry;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.anim.HoverLerp;
import com.cimdy.coe.ui.widget.compose.item.ObfuscatedTooltip;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 内嵌迷你背包面板：27 槽主背包（3 行 × 9）+ 热键栏（1 行 × 9），共 36 槽，槽位尺寸 {@link Theme#SLOT_SIZE}。
 *
 * <p>这是计划 §二·六「物品栏」在 WidgetScreen 系（无原版菜单容器）里的载体——
 * 供 StockScreen / CutScreen / SiteScreen 内嵌做「容器/背包只读对照」。
 *
 * <p><b>只读对照骨架</b>：{@link #bind(Supplier)} 注入 27+9 个栈的来源，本组件每帧只读它画出来，
 * 不修改任何栈。点击经 {@link #onSlotClick(Consumer)} 把<b>槽位序号</b>上抛——
 * 真实的「取/放」物品移动需要服务端容器交互包（{@code TakePutPacket}，计划六章待办），
 * 包就绪前这里只做渲染 + 回调骨架，不发包、不挪物品。
 *
 * <p><b>槽位序号约定</b>（行优先，自上而下）：主背包 3 行 = 序号 0..26（在上），
 * 热键栏 1 行 = 序号 27..35（在下）。{@code bind} 的 List 须按此顺序排列（不足 36 项时缺槽画空）。
 *
 * <p>动画（规格 §3）：槽位 hover 提亮 100ms（{@link HoverLerp}）。
 * 「拾取图标跟手」的完整拖拽（DnD）暂不实现——见 {@link Cell#onMouseClicked} 处注释。
 */
public class MiniInventory extends Node {

    /** 每行槽数。 */
    public static final int COLS = 9;
    /** 主背包行数。 */
    public static final int MAIN_ROWS = 3;
    /** 热键栏行数。 */
    public static final int HOTBAR_ROWS = 1;
    /** 总槽数 = 27 + 9。 */
    public static final int SLOT_COUNT = (MAIN_ROWS + HOTBAR_ROWS) * COLS;

    private final List<Cell> cells = new ArrayList<>();
    private Supplier<List<ItemStack>> contents = List::of;
    private Consumer<Integer> onSlotClick;
    /** 规格 §3：tooltip 混淆判定谓词，默认恒 false（混淆关闭）。 */
    private Predicate<ItemStack> obfuscateIf = s -> false;
    /** 修复批 5：槽可见性过滤谓词，默认恒 true（全部显示）；不匹配槽只画槽框、不弹 tooltip、不响应点击。 */
    private Predicate<ItemStack> filter = s -> true;

    private MiniInventory() {
        this.id = "mini-inventory";
        column();
        gap(0);
        int cell = Theme.SLOT_SIZE;
        flex.width = COLS * cell;
        flex.grow = 0;
        flex.shrink = 0;

        // 4 行：上 3 行主背包（0..26），底 1 行热键栏（27..35）
        for (int r = 0; r < MAIN_ROWS + HOTBAR_ROWS; r++) {
            Node row = new Node().row().gap(0);
            for (int c = 0; c < COLS; c++) {
                int slotIndex = r * COLS + c;
                Cell cellNode = new Cell(slotIndex);
                cellNode.flex.size(cell, cell);
                row.add(cellNode);
                cells.add(cellNode);
            }
            add(row);
        }
    }

    public static MiniInventory of() {
        return new MiniInventory();
    }

    /**
     * 绑定只读内容来源：返回 27+9 个 {@link ItemStack}（顺序见类注释）。
     * 每帧渲染时实时拉取，内容变化自动反映，无需重建组件。
     */
    public MiniInventory bind(Supplier<List<ItemStack>> contents) {
        this.contents = contents == null ? List::of : contents;
        return this;
    }

    /** 点击某槽时回调其序号（0..35）；真实取/放待 {@code TakePutPacket}。 */
    public MiniInventory onSlotClick(Consumer<Integer> slotClick) {
        this.onSlotClick = slotClick;
        return this;
    }

    /**
     * 加法式：命中谓词的槽，其 hover tooltip 走 {@link ObfuscatedTooltip#obfuscate} 未鉴定样式。
     *
     * <p>混淆判定条件待数据侧确认，默认关闭（恒 false）；传 {@code null} 复位为默认。
     * 不改既有字段 / 签名。
     */
    public MiniInventory obfuscateIf(Predicate<ItemStack> p) {
        this.obfuscateIf = p == null ? (s -> false) : p;
        return this;
    }

    /**
     * 修复批 5：槽可见性过滤（搜索 / 分类复用）。命中谓词的槽照常渲染与交互；
     * 未命中的槽退化为空槽框（不画物品、不弹 tooltip、不响应点击）。
     * 空栈恒视为命中（槽框保持完整网格）。传 {@code null} 复位为全部显示。
     */
    public MiniInventory filter(Predicate<ItemStack> p) {
        this.filter = p == null ? (s -> true) : p;
        return this;
    }

    /** 读第 i 槽的栈；越界或来源给空都回退空栈。 */
    private ItemStack stackAt(int i) {
        List<ItemStack> list = contents.get();
        if (list == null || i < 0 || i >= list.size()) return ItemStack.EMPTY;
        ItemStack s = list.get(i);
        return s == null ? ItemStack.EMPTY : s;
    }

    /**
     * 修复批 6：完整物品 tooltip 行（与 {@code ItemRenderer.hoverTooltip()}/{@code CodexScreen}
     * 同取行口径 {@code stack.getTooltipLines}），异常安全回退单行品名。
     */
    private static List<Component> tooltipLinesOf(ItemStack s) {
        try {
            Player p = Minecraft.getInstance().player;
            return s.getTooltipLines(Item.TooltipContext.EMPTY, p, TooltipFlag.Default.NORMAL);
        } catch (RuntimeException e) {
            return List.of(s.getHoverName());
        }
    }

    // ------------------------------------------------------------------
    // 单个槽：每帧从 supplier 读栈，自绘槽位 + 物品 + hover 提亮 + tooltip + 点击
    // ------------------------------------------------------------------

    private final class Cell extends AnimatedNode {
        private final int slotIndex;
        /** 规格 §3：槽位 hover 提亮 100ms。 */
        private final HoverLerp hover = new HoverLerp(100);
        /** put-pop：上一帧观察到的栈（null = 尚未建档，首帧静默不弹，避免进场齐弹）。 */
        private ItemStack lastSeen;

        Cell(int slotIndex) {
            this.slotIndex = slotIndex;
            this.id = "inv-slot";
            flex.grow = 0;
            flex.shrink = 0;
            // 悬浮名 tooltip：每帧读当前栈，空槽 / 未命中过滤谓词的槽不提示；
            // 完整物品 tooltip 行（修复批 6：与 ItemRenderer.hoverTooltip() 同取行口径，
            // 关键字高亮 + 混淆判定同链路生效；异常安全回退单行品名）；
            // 命中混淆谓词 → 未鉴定样式
            this.tooltip = () -> {
                ItemStack s = stackAt(slotIndex);
                if (s.isEmpty() || !filter.test(s)) return List.of();
                List<Component> lines = tooltipLinesOf(s);
                return obfuscateIf.test(s) ? ObfuscatedTooltip.obfuscate(lines) : lines;
            };
        }

        @Override
        protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
            hover.update(ctx.hovered());

            int slotBg = Theme.current().slotBg();
            float a = hover.amount();
            int bg = a > 0.001F ? Theme.lighten(slotBg, 0.18F * a) : slotBg;
            Draw.fill(g, absX, absY, width, height, bg);
            Draw.outline(g, absX, absY, width, height, Theme.current().slotBorder());

            ItemStack stack = stackAt(slotIndex);

            // put-pop（附录 F 缺口 1，与 Slot 同规则）：镜像内容变化时强调回弹一次。
            // 缩放 1 → 1.08 → 1、回程 EASE_OUT_BACK，时长读 AnimRegistry.ms(VALUE) 令牌；
            // 首帧静默建档（避免打开时 36 槽齐弹），之后内容换成新物品才弹；
            // 高频连放时新 pulse 直接顶掉进行中的旧补间（Animator 既有打断语义，不排队）。
            // 纯视觉缩放，不改命中区；hover 提亮沿用下方 HoverLerp(100)，不重复实现。
            if (lastSeen == null) {
                lastSeen = stack;
            } else if (!sameStack(lastSeen, stack)) {
                lastSeen = stack;
                if (!stack.isEmpty()) pulse(0.08F, AnimRegistry.ms(AnimRegistry.Trigger.VALUE));
            }

            if (stack.isEmpty() || !filter.test(stack)) return;   // 空槽 / 未命中过滤：只显槽框

            int box = Math.max(1, width - 2);
            Draw.item(g, stack, absX + 1, absY + 1, box);

            // 数量角标（对照展示：堆叠数量）
            if (stack.getCount() > 1) {
                Component num = Component.literal(Integer.toString(stack.getCount()));
                // 槽内角标沿用与 ItemGrid 一致的小角标比例（box/20），不另散写缩放
                float scale = box / 20.0F;
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
            ItemStack s = stackAt(slotIndex);
            if (s.isEmpty() || !filter.test(s)) return false;   // 空槽 / 未命中过滤：不响应
            // 取/放骨架：真实物品移动待服务端容器交互包 TakePutPacket 就绪后在此发包。
            // 拾取图标「跟手」（完整拖拽 DnD）暂不实现——当前仅把槽位序号上抛，
            // 由 Screen 决定后续行为（打开取放弹窗 / 高亮等）；拖拽视觉留待 DnD 基建接入。
            if (onSlotClick != null) onSlotClick.accept(slotIndex);
            return true;
        }

        /** 两栈是否视为同一内容（空对空 = 同；一空一非空 = 变；否则按物品 + 组件比对，仅数量变化不算变）。 */
        private static boolean sameStack(ItemStack a, ItemStack b) {
            boolean ae = a.isEmpty(), be = b.isEmpty();
            if (ae || be) return ae == be;
            return ItemStack.isSameItemSameComponents(a, b);
        }
    }
}
