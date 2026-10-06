package com.cimdy.coe.ui.widget.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * lib 自带的绘制原语工具箱。
 *
 * <p>存在的意义是<b>切断 lib 对业务包的反向依赖</b> —— 旧渲染器普遍 import
 * {@code com.cimdy.coe.client.render.RenderHelper}，让一个通用 UI 库依赖具体模组的工具类。
 * 这里把用得到的原语内化进来，lib 只依赖原版 GUI API。
 *
 * <p>另外统一了两个旧库里不一致的地方：
 * <ul>
 *   <li>全透明（alpha=0）的颜色直接跳过绘制，省掉大量无效 fill；</li>
 *   <li>缩放绘制不再偷偷加 {@code +2} 的魔法偏移，坐标即所见，需要留白请用 padding 显式表达。</li>
 * </ul>
 */
public final class Draw {

    private Draw() {}

    // ------------------------------------------------------------------
    // 字体度量
    // ------------------------------------------------------------------

    public static Font font() { return Minecraft.getInstance().font; }

    public static int lineHeight() { return font().lineHeight; }

    public static int width(Component c) { return font().width(c); }

    public static int width(String s) { return font().width(s); }

    // ------------------------------------------------------------------
    // 形状
    // ------------------------------------------------------------------

    /** 实心矩形；全透明时跳过。 */
    public static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (isTransparent(argb) || w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, argb);
    }

    /** 1px 外框；全透明时跳过。 */
    public static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (isTransparent(argb) || w <= 0 || h <= 0) return;
        g.outline(x, y, w, h, argb);
    }

    /**
     * 四角直角括号（旧 {@code BorderStyle.CORNERS}）：只画四个角上的 2px 折线，
     * 用于「有边框感但不封闭」的科幻风面板。
     */
    public static void cornerBrackets(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (isTransparent(argb) || w < 2 || h < 2) return;
        // 左上
        g.fill(x, y, x + 1, y + 2, argb);
        g.fill(x, y, x + 2, y + 1, argb);
        // 右上
        g.fill(x + w - 1, y, x + w, y + 2, argb);
        g.fill(x + w - 2, y, x + w, y + 1, argb);
        // 左下
        g.fill(x, y + h - 2, x + 1, y + h, argb);
        g.fill(x, y + h - 1, x + 2, y + h, argb);
        // 右下
        g.fill(x + w - 1, y + h - 2, x + w, y + h, argb);
        g.fill(x + w - 2, y + h - 1, x + w, y + h, argb);
    }

    /**
     * 四角短横线装饰（旧 {@code RenderHelper.drawBorderCorners}）：
     * 在距边 {@code padding} 处，从四角各引出长 {@code length} 的水平线。
     */
    public static void cornerTicks(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                   int padding, int length, int argb) {
        if (isTransparent(argb)) return;
        g.horizontalLine(x + padding, x + padding + length, y + padding, argb);
        g.horizontalLine(x + padding, x + padding + length, y + h - padding, argb);
        g.horizontalLine(x + w - padding, x + w - padding - length, y + padding, argb);
        g.horizontalLine(x + w - padding, x + w - padding - length, y + h - padding, argb);
    }

    /**
     * 近似实心圆 —— 用正八边形拼，只需 3 次 fill。
     *
     * <p>为什么不画真圆：逐行扫描填充一个半径 r 的圆要 {@code 2r+1} 次 fill，
     * 图画布上动辄几千个节点，这个代价直接吃掉帧率。八边形在小尺寸下肉眼几乎无差别，
     * 而代价是常数级。要真圆请用贴图。
     */
    public static void disc(GuiGraphicsExtractor g, int cx, int cy, int r, int argb) {
        if (isTransparent(argb) || r <= 0) return;
        if (r <= 1) {
            g.fill(cx - r, cy - r, cx + r + 1, cy + r + 1, argb);
            return;
        }
        int i = Math.max(1, Math.round(r * 0.4F)); // 四角切掉的量
        g.fill(cx - r, cy - r + i, cx + r, cy + r - i, argb);     // 中间满宽横带
        g.fill(cx - r + i, cy - r, cx + r - i, cy - r + i, argb); // 上收窄带
        g.fill(cx - r + i, cy + r - i, cx + r - i, cy + r, argb); // 下收窄带
    }

    /**
     * 圆环：外圈画 {@code borderArgb} 的实心圆，再在里面套一个 {@code fillArgb} 的实心圆。
     * 因此 {@code fillArgb} 透明时得到的是一个实心边框色圆盘，而不是镂空圆环 —— 这是廉价画法的代价。
     */
    public static void discWithBorder(GuiGraphicsExtractor g, int cx, int cy, int r,
                                      int fillArgb, int borderArgb, int borderThickness) {
        if (r <= 0) return;
        int t = Math.max(1, borderThickness);
        if (!isTransparent(borderArgb)) {
            disc(g, cx, cy, r, borderArgb);
            disc(g, cx, cy, r - t, fillArgb);
        } else {
            disc(g, cx, cy, r, fillArgb);
        }
    }

    /**
     * 八边形<b>环</b>（annulus）：外八边形 − 内八边形，按 y 段逐段画左右两条（≤14 次 fill）。
     *
     * <p>为什么需要它：{@link #disc} 是<b>填充</b>八边形，想画「一圈」只能靠
     * 「先画大圆、再用底色/填充色盖住内部」——那要求已知背后颜色，且不透明覆盖会
     * 连带擦掉下层的连线或邻节点。本方法直接只画环带，<b>不碰内部、不依赖背景色</b>，
     * 因此可以在主体之外安全地留出真正的间隙（C1 双环信号依赖这一点）。
     *
     * <p>内孔是与 {@link #disc} 同构造的<b>八边形</b>（不是方形）：环很薄时方孔会在
     * 对角方向吞掉整条环带。环带四角按八边形切角收窄，与 {@link #disc} 视觉语言一致。
     */
    public static void ring(GuiGraphicsExtractor g, int cx, int cy, int rOuter, int rInner, int argb) {
        if (isTransparent(argb) || rOuter <= 0) return;
        if (rInner <= 0) { disc(g, cx, cy, rOuter, argb); return; }
        if (rInner >= rOuter) return;
        int io = Math.max(1, Math.round(rOuter * 0.4F));   // 外八边形切角
        int ii = Math.max(1, Math.round(rInner * 0.4F));   // 内八边形切角
        int oFlat = Math.max(1, rOuter - io);              // 外八边形「满宽区」半高
        int iFlat = Math.max(1, rInner - ii);              // 内八边形「满宽区」半高

        // 逐 y 段画左右两条：段边界取内外八边形的形状变化处，段内半宽恒定。
        // 这样内孔仍是<b>八边形</b>（而非方形）—— 环很薄时方孔会在对角方向吞掉整条环带。
        int[] ys = {cy - rOuter, cy - oFlat, cy - rInner, cy - iFlat,
                    cy + iFlat, cy + rInner, cy + oFlat, cy + rOuter};
        java.util.Arrays.sort(ys);
        for (int k = 0; k + 1 < ys.length; k++) {
            int y0 = Math.max(ys[k], cy - rOuter);
            int y1 = Math.min(ys[k + 1], cy + rOuter);
            if (y1 <= y0) continue;                        // 去重 / 空段
            // 用<b>段的上下界</b>判定，而不是取中点的 |dy|：
            // disc 的矩形是半开区间 [y, y+h)，故上下两侧边界归属并不对称
            // （ady == rInner 时，上侧仍在内八边形内、下侧已在其外），取中点会判错一格。
            // 外八边形中段为 [cy-oFlat, cy+oFlat)：整段落在其内才满宽。
            boolean oMid = y0 >= cy - oFlat && y1 <= cy + oFlat;
            int wo = oMid ? rOuter : oFlat;
            // 内八边形占据 [cy-rInner, cy+rInner)
            int wi;
            if (y1 <= cy - rInner || y0 >= cy + rInner) {
                wi = 0;                                    // 整段在内八边形之外 → 整段皆环
            } else {
                boolean iMid = y0 >= cy - iFlat && y1 <= cy + iFlat;
                wi = iMid ? rInner : iFlat;
            }
            int w = wo - wi;
            if (w <= 0) continue;
            fill(g, cx - wo, y0, w, y1 - y0, argb);
            fill(g, cx + wi, y0, w, y1 - y0, argb);
        }
    }

    /** 四角 1px 点缀（旧 {@code renderBorderBox} 里那四个角点）。 */
    public static void cornerDots(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (isTransparent(argb) || w < 2 || h < 2) return;
        g.fill(x, y, x + 1, y + 1, argb);
        g.fill(x + w - 1, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + 1, y + h, argb);
        g.fill(x + w - 1, y + h - 1, x + w, y + h, argb);
    }

    // ------------------------------------------------------------------
    // 圆角 / 渐变 / 阴影（全部仅依赖 g.fill，不依赖 vanilla 圆角 API，
    // 保证 lib 在任意 MC 版本上行为一致）
    // ------------------------------------------------------------------

    /**
     * 圆角矩形填充。radius 自动夹到可用范围；0 退化为直角 {@link #fill}。
     *
     * <p>用「中段满宽 + 上下圆角帽逐行收缩」实现，代价是常数级 fill 次数（≈ 2×radius+1），
     * 远比逐行扫描整圆便宜，且小尺寸下肉眼无差别。
     */
    public static void roundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int argb) {
        if (isTransparent(argb) || w <= 0 || h <= 0) return;
        int r = Math.min(radius, Math.min(w, h) / 2);
        if (r <= 0) { fill(g, x, y, w, h, argb); return; }
        fill(g, x, y + r, w, h - 2 * r, argb);                 // 中段满宽
        for (int i = 0; i < r; i++) {                          // 上下圆角帽
            int cut = cornerCut(i, r);
            fill(g, x + cut, y + i, w - 2 * cut, 1, argb);
            fill(g, x + cut, y + h - 1 - i, w - 2 * cut, 1, argb);
        }
    }

    /**
     * 圆角描边（1px 环）：按「外圈圆角矩形 − 内缩圆角矩形」的差集逐行画左右两段，
     * 因此<b>不需要已知背景色就能抠出环</b>，适合做叠加在内容之上的发光边框。
     */
    public static void roundRectOutline(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                        int radius, int thickness, int argb) {
        if (isTransparent(argb) || w <= 0 || h <= 0) return;
        int t = Math.max(1, thickness);
        int r = Math.min(radius, Math.min(w, h) / 2);
        int ri = Math.max(0, r - t);
        int innerH = h - 2 * t;
        for (int i = 0; i < h; i++) {
            int cutOut = cornerCut(i, h, r);
            int xL = x + cutOut, xR = x + w - cutOut;
            int li = i - t;                                     // 在内圈里的局部行
            boolean innerMissing = li < 0 || li >= innerH;
            if (innerMissing) {
                fill(g, xL, y + i, xR - xL, 1, argb);          // 顶/底圆角帽整行都是边
                continue;
            }
            int cutIn = cornerCut(li, ri);
            int ixL = x + t + cutIn, ixR = x + w - t - cutIn;
            if (ixL > xL) fill(g, xL, y + i, ixL - xL, 1, argb);
            if (xR > ixR) fill(g, ixR, y + i, xR - ixR, 1, argb);
        }
    }

    /** 垂直渐变填充：从 {@code top} 到 {@code bottom} 逐行混合。两色相同则退化为实色。 */
    public static void gradientV(GuiGraphicsExtractor g, int x, int y, int w, int h, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        if (top == bottom) { fill(g, x, y, w, h, top); return; }
        int denom = Math.max(1, h - 1);
        for (int i = 0; i < h; i++) {
            fill(g, x, y + i, w, 1, blend(top, bottom, (float) i / denom));
        }
    }

    /**
     * 柔和投影：从矩形出发，按层向外偏移并递减 alpha 叠若干层圆角矩形，模拟模糊阴影。
     * 画在节点下方（偏移向右下），本身是半透，所以不需要已知背景。
     */
    public static void shadow(GuiGraphicsExtractor g, int x, int y, int w, int h,
                              int radius, int blur, int argb) {
        if (isTransparent(argb) || w <= 0 || h <= 0) return;
        int baseA = (argb >>> 24) & 0xFF;
        int rgb = argb & 0x00FFFFFF;
        int layers = Math.max(1, blur);
        for (int i = 1; i <= layers; i++) {
            float f = (float) i / layers;
            int a = (int) (baseA * (1 - f) * (1 - f));
            if (a <= 0) continue;
            int off = (int) Math.round(f * blur);
            roundRect(g, x + off, y + off, w, h, radius, (rgb) | (a << 24));
        }
    }

    /** 圆角帽在某行的横向收缩量（距离圆心 dy 处，弦半宽 = r − √(r²−dy²)）。 */
    private static int cornerCut(int i, int r) {
        if (r <= 0) return 0;
        if (i < r) {
            int dy = r - i;
            return dy >= r ? 0 : (int) Math.round(r - Math.sqrt((long) r * r - (long) dy * dy));
        }
        if (i >= 0 && i < 2 * r) {            // 距底边的对称角
            int dy = i - r;
            return dy >= r ? 0 : (int) Math.round(r - Math.sqrt((long) r * r - (long) dy * dy));
        }
        return 0;
    }

    /** 圆角帽在某行的横向收缩量（给定矩形高 h，自动判上下两个角）。 */
    private static int cornerCut(int i, int h, int r) {
        if (r <= 0) return 0;
        if (i < r) return cornerCut(i, r);
        if (i >= h - r) {
            int dy = i - (h - r);
            return dy >= r ? 0 : (int) Math.round(r - Math.sqrt((long) r * r - (long) dy * dy));
        }
        return 0;
    }

    /** 两色线性插值（t 取 0~1），含 alpha 通道。与 {@link Theme#blend} 同算法，避免 Draw 反向依赖 skin 包。 */
    public static int blend(int a, int b, float t) {
        t = Math.max(0F, Math.min(1F, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        int al = (int) (((a >> 24) & 0xFF) + ((((b >> 24) & 0xFF) - ((a >> 24) & 0xFF)) * t));
        return (Math.max(0, Math.min(255, al)) << 24) | (r << 16) | (g << 8) | bl;
    }

    // ------------------------------------------------------------------
    // 文本
    // ------------------------------------------------------------------

    /** 原始尺寸绘制，(x,y) 为文本左上角。 */
    public static void text(GuiGraphicsExtractor g, Component c, int x, int y, int argb) {
        if (isTransparent(argb)) return;
        g.text(font(), c, x, y, argb, false);
    }

    /** 原始尺寸绘制字符串。 */
    public static void text(GuiGraphicsExtractor g, String s, int x, int y, int argb) {
        if (isTransparent(argb)) return;
        g.text(font(), s, x, y, argb, false);
    }

    /**
     * 缩放绘制，(x,y) 为缩放后文本的左上角（屏幕坐标）。
     * scale &lt;= 0 或 ≈1 时走无缩放快路径。
     */
    public static void textScaled(GuiGraphicsExtractor g, Component c, int x, int y, float scale, int argb) {
        if (isTransparent(argb)) return;
        if (scale <= 0F || Math.abs(scale - 1F) < 1e-4F) {
            text(g, c, x, y, argb);
            return;
        }
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(scale, scale);
        g.text(font(), c, Math.round(x / scale), Math.round(y / scale), argb, false);
        pose.popMatrix();
    }

    /** 缩放居中绘制，cx 为水平中心、y 为缩放后文本顶端。 */
    public static void centeredTextScaled(GuiGraphicsExtractor g, Component c, int cx, int y, float scale, int argb) {
        if (isTransparent(argb)) return;
        if (scale <= 0F || Math.abs(scale - 1F) < 1e-4F) {
            g.centeredText(font(), c, cx, y, argb);
            return;
        }
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(scale, scale);
        g.centeredText(font(), c, Math.round(cx / scale), Math.round(y / scale), argb);
        pose.popMatrix();
    }

    // ------------------------------------------------------------------
    // 物品
    // ------------------------------------------------------------------

    /** 以 {@code box} 为边长绘制物品（原版物品图标基准尺寸为 16）。 */
    public static void item(GuiGraphicsExtractor g, ItemStack stack, int x, int y, float box) {
        if (stack == null || stack.isEmpty() || box <= 0) return;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        float scale = box / 16.0F;
        pose.scale(scale, scale);
        g.item(stack, Math.round(x / scale), Math.round(y / scale));
        pose.popMatrix();
    }

    // ------------------------------------------------------------------
    // 杂项
    // ------------------------------------------------------------------

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public static boolean isTransparent(int argb) {
        return (argb >>> 24) == 0;
    }

    // ------------------------------------------------------------------
    // 数值裁剪（共享原语）
    // ------------------------------------------------------------------

    /** 将 {@code v} 夹到 [lo, hi] 闭区间。 */
    public static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** 将 {@code v} 夹到 [0,1] 闭区间；NaN 视为 0（防御滚动进度等异常输入）。 */
    public static double clamp01(double v) {
        return Double.isNaN(v) ? 0.0 : Math.max(0.0, Math.min(1.0, v));
    }
}
