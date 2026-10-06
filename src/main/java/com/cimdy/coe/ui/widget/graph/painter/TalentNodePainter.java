package com.cimdy.coe.ui.widget.graph.painter;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.client.render.RenderHelper;
import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import com.cimdy.coe.ui.screen.talent.TalentAllocator;
import com.cimdy.coe.ui.screen.talent.TalentTheme;

/**
 * 天赋树节点画笔（业务专属）：复刻旧 {@code TalentNodeRenderer} 的视觉语义。
 *
 * <ul>
 *   <li><b>珠宝插槽</b>：方形；空槽青框、已插珠宝金框 + 物品图标；</li>
 *   <li><b>普通节点</b>：圆 + 类型底色 + 图标（纹理或物品）；</li>
 *   <li><b>状态边框</b>：已点亮=武器组色，休眠=暗武器组色，可点=白，锁定=深灰，悬浮提亮；</li>
 *   <li><b>辐射</b>：被珠宝辐射覆盖的节点加一圈紫框。</li>
 * </ul>
 */
class TalentNodePainter implements NodePainter<TalentNodeDef>, TalentNodePainterI {

    private final TalentAllocator allocator;

    /** 天赋图标纹理统一尺寸（爬取脚本已 resize 到 64x64）。 */
    private static final int ICON_SIZE = 64;
    private static final String ICON_PATH = "textures/gui/talent/icons/";

    /** 搜索命中的节点 id（画黄色聚焦圈）。 */
    private final Set<String> highlighted = new HashSet<>();

    public TalentNodePainter(TalentAllocator allocator) {
        this.allocator = allocator;
    }

    public void setHighlight(Set<String> ids) {
        highlighted.clear();
        if (ids != null) highlighted.addAll(ids);
    }

    @Override
    public void paint(GuiGraphicsExtractor g, GraphNode<TalentNodeDef> node, RenderContext ctx, float zoom) {
        TalentNodeDef def = node.payload;
        if (def == null) return;

        float cx = node.worldX, cy = node.worldY;
        int r = Math.max(1, Math.round(node.radius));
        // 边框厚度按节点尺寸分档（C2 缺口已做，此处补厚度分档）：小节点 1px、keystone 至多 3px，
        // 同时随 zoom 缩放保证任何缩放下都看得见。
        int thick = Math.max(1, Math.min(3, Math.round((r / 14F) / Math.max(1e-4F, zoom))));
        boolean allocated = allocator.isAllocated(node.id);
        boolean active = allocator.isActive(node.id);
        int weaponSet = allocator.weaponSetOf(node.id);
        boolean jewelLit = allocator.isJewelRadiated(node.id);

        // 状态边框色
        int border;
        if (allocated) {
            border = active ? TalentTheme.bindingColor(weaponSet) : TalentTheme.bindingColorDim(weaponSet);
        } else if (allocator.canAllocate(node.id)) {
            border = ctx.hovered() ? Theme.current().textBright() : TalentTheme.borderAvailable();
        } else {
            border = ctx.hovered() ? Theme.current().talentTextDim() : TalentTheme.borderLocked();
        }

        if (def.type().socket()) {
            paintSocket(g, node, cx, cy, r, thick, allocated, border, jewelLit, ctx);
            return;
        }

        // ---- 普通节点：圆 + 图标 ----
        int baseFill = TalentTheme.colorFor(def.type());
        // C4 安全网（UI-ACC-005 §5-Q3）：类型色硬编码于 TalentNodeTypeRegistry、
        // 数据包可覆盖且 lint 扫不到，极端色会让节点在画布上不可见。此处把「已点亮填充」
        // 对画布底的对比度抬到 talentFillSafeFloor() 之上。未分配填充随后仍按 C4 乘 0.70 压暗
        // （它的可见性由边框承担，故不参与钳制 —— 否则会反向削弱 C4 的明暗差）。
        // 当前 7 个内置类型色实测均已达标（4.23–13+），因此本钳制对内置色零视觉变化。
        baseFill = ensureFillContrast(baseFill, Theme.current().talentScreenBg(), Theme.current().talentFillSafeFloor());
        if (!allocated) baseFill = darken(baseFill, 0.70F);   // C4（UI-ACC-004）
        if (ctx.hovered()) baseFill = lighten(baseFill, 0.25F);

        int x = Math.round(cx), y = Math.round(cy);

        // 主体前画的实心八边形外环：装饰圈(keystone/notable) + 珠宝辐射环 + 搜索高亮环。
        // 级联重排 jewelLit +2→+4 / highlight +3→+6（§UI-ACC-005 §1.3）：给 C1 真环让位，
        // 避免三个实心八边形互相 z-fighting（disc 是填充的，大半径会整片盖掉小半径）。
        int fr = TalentTheme.radiusFor(def.type());
        if (def.type().keystone() || fr >= 10) {
            int ring = def.type().keystone() ? Theme.current().talentAccent() : Theme.current().talentKeystoneRingDim();
            Draw.disc(g, x, y, r + thick + 1, ring);
        }
        if (jewelLit && allocated) {
            Draw.disc(g, x, y, r + thick + 4, TalentTheme.jewelLit());
        }
        if (highlighted.contains(node.id)) {
            Draw.disc(g, x, y, r + thick + 6, Theme.current().talentAccent());
        }

        // 主体（已点亮 / 未点亮完全一致）
        Draw.discWithBorder(g, x, y, r, baseFill, border, thick);

        if (allocated) {
            // C1 已点亮外环：用 Draw.ring 画<b>真环</b>，并与主体边框之间留出 gapW 宽的
            // <b>未绘制间隙</b> —— 间隙露出画布（或下层的辐射/高亮/装饰环），
            // 因此「双环」信号与颜色无关，Paper 单墨预设下同样成立（§UI-ACC-005 §1.6 + §4）。
            //
            // 关键修正：不能让外环与主体边框「同色且直接相邻」。此前用 border 色紧贴主体
            // 画环，两段在 [r-thick, r] 与 [r, r+thick+2] 上同色相连，肉眼合并成一条粗边，
            // 双环语义丢失（五个预设全部命中，Paper 因单墨尤其严重）。
            int gapW = thick;                   // 间隙宽（≈1px 屏幕，随 zoom 补偿）
            int ringW = Math.max(1, thick);     // 环宽
            Draw.ring(g, x, y, r + gapW + ringW, r + gapW, border);
        } else if (!allocator.canAllocate(node.id)) {
            // C2 LOCKED 断点环：边框带 [r-thick, r] 上开 4 个 30° 缺口，用 baseFill 回涂
            // （绝不露画布底色，故不会误伤连线/邻节点，§UI-ACC-005 §2）。
            int rIn = r - thick, rOut = r;
            int gap = Math.max(2, (int) Math.round((r - thick * 0.5F) * 2 * Math.sin(Math.toRadians(15))));
            Draw.fill(g, x - gap / 2, y - rOut, gap, rOut - rIn, baseFill);        // N
            Draw.fill(g, x - gap / 2, y + rIn, gap, rOut - rIn, baseFill);         // S
            Draw.fill(g, x - rOut, y - gap / 2, rOut - rIn, gap, baseFill);        // W
            Draw.fill(g, x + rIn, y - gap / 2, rOut - rIn, gap, baseFill);         // E
        }

        drawIcon(g, def, allocator.socketOf(node.id), cx, cy, r);

        if (allocated) {
            // C3 武器组角标（右上角 ASCII 字形，纯形状线索，§UI-ACC-005 §3）：仅 allocated 节点
            String glyph = weaponSetBadge(weaponSet);
            int bxC = Math.round(cx + r * 0.424F);
            int byC = Math.round(cy - r * 0.424F);
            Draw.text(g, glyph, bxC - Draw.width(glyph) / 2, byC - Draw.lineHeight() / 2, Theme.current().talentBadgeText());
        }
    }

    private void paintSocket(GuiGraphicsExtractor g, GraphNode<TalentNodeDef> node,
                             float cx, float cy, int r, int thick,
                             boolean allocated, int border, boolean jewelLit, RenderContext ctx) {
        int left = Math.round(cx - r), top = Math.round(cy - r), size = r * 2;
        ItemStack jewel = allocator.socketOf(node.id);
        boolean filled = !jewel.isEmpty();

        g.fill(left, top, left + size, top + size,
                allocated ? Theme.current().talentNodeBgAllocated() : Theme.current().talentNodeBg());
        int frameColor = filled ? TalentTheme.socketFilled()
                : (allocated ? border : TalentTheme.socketEmpty());
        g.outline(left, top, size, size, frameColor);
        g.outline(left + 2, top + 2, size - 4, size - 4, frameColor);

        if (filled) {
            float box = r * 1.4F;
            RenderHelper.renderItemWithScale(g, jewel,
                    (int) (cx - box / 2F), (int) (cy - box / 2F), box);
        }
        if (ctx.hovered()) {
            g.outline(left - 2, top - 2, size + 4, size + 4, Theme.current().textBright());
        }
        if (jewelLit) {
            g.outline(left - 4, top - 4, size + 8, size + 8, TalentTheme.jewelLit());
        }
    }

    private void drawIcon(GuiGraphicsExtractor g, TalentNodeDef def, ItemStack socketJewel,
                          float cx, float cy, int r) {
        // 插槽已插珠宝 → 物品图标优先
        if (!socketJewel.isEmpty()) {
            float box = r * 1.4F;
            RenderHelper.renderItemWithScale(g, socketJewel,
                    (int) (cx - box / 2F), (int) (cy - box / 2F), box);
            return;
        }
        // 显式物品图标
        if (def.iconItem() != null && !def.iconItem().isEmpty()) {
            float box = r * 1.4F;
            RenderHelper.renderItemWithScale(g, def.iconItem(),
                    (int) (cx - box / 2F), (int) (cy - box / 2F), box);
            return;
        }
        // 纹理图标（POE2 爬取图）
        String icon = def.icon();
        if (icon != null && !icon.isEmpty()) {
            Identifier tex = Identifier.fromNamespaceAndPath("coe", ICON_PATH + icon + ".png");
            float innerD = r * 2F * 0.92F;
            var pose = g.pose();
            pose.pushMatrix();
            try {
                pose.translate(cx - innerD / 2F, cy - innerD / 2F);
                pose.scale(innerD / ICON_SIZE, innerD / ICON_SIZE);
                g.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            } finally {
                pose.popMatrix();
            }
        }
    }

    private static String weaponSetBadge(int set) {
        if (set == 0) return "G";
        if (set == 1) return "I";
        if (set == 2) return "II";
        return "D";
    }

    private static int darken(int color, float ratio) {
        int r = (int) (((color >> 16) & 0xFF) * ratio);
        int g = (int) (((color >> 8) & 0xFF) * ratio);
        int b = (int) ((color & 0xFF) * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int color, float add) {
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1 + add)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1 + add)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + add)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // ===== C4 安全网：填充对比度钳制（UI-ACC-005 §5-Q3） =====

    /**
     * 把填充色对背景色的 WCAG 对比度抬到 {@code floorMilli / 100} 之上（如 {@code 300} = 3.0:1）。
     * 已在阈值内则原样返回（因此内置类型色零视觉变化）；否则朝<b>背离底色</b>的方向逐步调整。
     *
     * <p>方向判据：白可达 3:1 需底色 L ≤ 0.30，黑可达 3:1 需底色 L ≥ 0.10，
     * 故以 0.30 为界选方向，保证单调收敛（迭代上限 64 次，不会死循环）。
     */
    private static int ensureFillContrast(int fill, int bg, int floorMilli) {
        double target = Math.max(1.0, floorMilli / 100.0);
        if (contrastRatio(fill, bg) >= target) return fill;
        boolean toLight = relativeLuminance(bg) < 0.30;
        int c = fill;
        for (int i = 0; i < 64 && contrastRatio(c, bg) < target; i++) {
            int next = shift(c, toLight);
            if (next == c) break;                          // 已到白/黑端点，无法继续改善
            c = next;
        }
        return c;
    }

    /**
     * 朝白色（{@code up}）或黑色推进一档。
     * 用「按剩余距离推进」而非乘法：乘法在近黑端会失效（{@code 5 * 1.08} 取整仍是 5），
     * 恰好兜不住最需要救的极端暗色。
     */
    private static int shift(int color, boolean up) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        if (up) {
            r += (int) ((255 - r) * 0.08F);
            g += (int) ((255 - g) * 0.08F);
            b += (int) ((255 - b) * 0.08F);
        } else {
            r -= (int) (r * 0.08F);
            g -= (int) (g * 0.08F);
            b -= (int) (b * 0.08F);
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** WCAG 相对亮度：sRGB 逐通道线性化后按 0.2126 / 0.7152 / 0.0722 加权。 */
    private static double relativeLuminance(int argb) {
        return 0.2126 * channel((argb >> 16) & 0xFF)
             + 0.7152 * channel((argb >> 8) & 0xFF)
             + 0.0722 * channel(argb & 0xFF);
    }

    private static double channel(int v) {
        double c = v / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    /** WCAG 对比度 {@code (L₁+0.05)/(L₂+0.05)}。 */
    private static double contrastRatio(int a, int b) {
        double la = relativeLuminance(a), lb = relativeLuminance(b);
        double hi = Math.max(la, lb), lo = Math.min(la, lb);
        return (hi + 0.05) / (lo + 0.05);
    }
}
