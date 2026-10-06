package com.cimdy.coe.ui.widget.compose.badge;

import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.StylePreset;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * 状态符号原语（计划 §四·七 A：图标化替代文字，组件清单表第 1 层新增）。
 *
 * <p>原则：<b>状态 / 档位 / 类别能用图形表达就不用文字</b>——本组件本体默认不渲染任何可见文字，
 * 文字仅经 {@link #tooltip(Component...)} 作悬停兜底说明（调用方注入）。
 *
 * <p>形态（全部皮肤驱动，色值一律在渲染时取 {@link Theme#current()} 令牌，不写死、不构造期冻结）：
 * <ul>
 *   <li>{@link #bars(int, int)} —— 档位格（Timberborn 风格，1~5 格横排，≤level 格亮 accent，
 *       其余暗 slotBorder；格子用 {@link Draw#roundRect} 自绘）；</li>
 *   <li>{@link #arrow(Direction)} —— 方向符号 ▲供应 / ▼需求 / ◇仓储；</li>
 *   <li>{@link #tier(int)} —— 等级点 ●◇◆（特殊字符优先，着色走令牌）；</li>
 *   <li>{@link #result(boolean)} —— 结果符 ✓（statusOk）/ ✗（textDanger）；</li>
 *   <li>{@link #pulseDot(int)} —— 脉冲圆点（{@link Draw#disc} 八边形，配合 {@link #firePulse()}）。</li>
 * </ul>
 *
 * <p><b>自绘原语按 Draw 真实能力，不臆造</b>：Draw 没有多边形原语，三角形 / 菱形不用字符兜底，
 * 而是与 {@link Draw#disc} 同一套廉价画法——逐行 {@link Draw#fill} 扫描拼出形状（行数 ≈ 形状高度，
 * 常数代价，小尺寸下肉眼无差别）。等级点 / 结果符才走字体特殊字符。
 *
 * <p><b>动画按 AnimatedNode 真实 API</b>：基类只有一次性 {@code pulse()}（300ms，缩放 1→1.06→1，
 * 对齐计划动画规格表「树节点激活 Pulse 300ms」），<b>没有循环呼吸</b>。因此本组件空闲不空转：
 * 由应用方在状态变化事件里调 {@link #firePulse()} 触发一次强调。透明度沿
 * {@link RenderContext#alpha()} 下传（与 ColorRenderer/TextRenderer 一致），故 Fade 类父动画对本组件同样生效。
 */
public class StatusGlyph extends AnimatedNode {

    /** 方向符号语义：▲供应（流入）/ ▼需求（流出）/ ◇仓储（中转）。 */
    public enum Direction { SUPPLY, DEMAND, STORE }

    /**
     * 档位格亮色语义（C.3 加法式扩展，不改 {@link #bars(int,int)} 原语义）：
     * <ul>
     *   <li>{@link #ACCENT} —— 原行为：亮格取 {@code accent}（蓝，Timberborn 递增式）；</li>
     *   <li>{@link #RED} / {@link #GREEN} —— 分歧式优先级：负档红系（{@code textDanger}）/ 正档绿系（{@code statusOk}）；</li>
     *   <li>{@link #NEUTRAL} —— 零档中性（亮格取 {@code slotBorder}，视觉上等同无强调）。</li>
     * </ul>
     */
    public enum Tint { ACCENT, RED, GREEN, NEUTRAL }

    private enum Kind { BARS, ARROW, TIER, RESULT, DOT }

    // ---- 尺寸常量（小字号行内图标；不引 CoeUi 常量是因为本组件属 widget 库层，不反向依赖 screen 包）----
    /** 档位格：单格宽 / 高 / 格间距（Timberborn 风格细条）。 */
    private static final int CELL_W = 4, CELL_H = 8, CELL_GAP = 1;
    /** 方向符号包围盒。 */
    private static final int ARROW_W = 7, ARROW_H = 8;
    /** 脉冲圆点半径。 */
    private static final int DOT_R = 2;

    private final Kind kind;
    // BARS
    private int level, max;
    private Tint tint = Tint.ACCENT;
    // ARROW
    private Direction direction;
    // TIER
    private int tier;
    // RESULT
    private boolean ok;
    // DOT
    private int color;

    private StatusGlyph(Kind kind) {
        this.kind = kind;
        this.id = "status-glyph";
    }

    // ------------------------------------------------------------------
    // 静态工厂
    // ------------------------------------------------------------------

    /**
     * 档位格（1~5 格）：横排 {@code max} 格，前 {@code level} 格亮（accent），其余暗（slotBorder）。
     * {@code level}/{@code max} 越界自动夹取（max 夹到 1~5，level 夹到 [0, max]）。
     * <p>C.3：等价于 {@link #bars(int, int, Tint)} 传 {@link Tint#ACCENT}，原调用方行为不变。
     */
    public static StatusGlyph bars(int level, int max) {
        return bars(level, max, Tint.ACCENT);
    }

    /**
     * 档位格（C.3 分歧式扩展）：横排 {@code max} 格，前 {@code level} 格按 {@code tint} 语义取色亮，
     * 其余暗（slotBorder）。{@code tint==null} 按 {@link Tint#ACCENT}。
     * 用于站点优先级 -2..2 五档：负档红 / 零档中性 / 正档绿。
     */
    public static StatusGlyph bars(int level, int max, Tint tint) {
        int m = Draw.clamp(max, 1, 5);
        StatusGlyph g = new StatusGlyph(Kind.BARS);
        g.max = m;
        g.level = Draw.clamp(level, 0, m);
        g.tint = tint == null ? Tint.ACCENT : tint;
        g.flex.size(m * CELL_W + (m - 1) * CELL_GAP, CELL_H);
        return g;
    }

    /** 方向符号：▲供应 / ▼需求 / ◇仓储（逐行 fill 自绘三角形 / 菱形，色值随方向取令牌）。 */
    public static StatusGlyph arrow(Direction d) {
        StatusGlyph g = new StatusGlyph(Kind.ARROW);
        g.direction = d == null ? Direction.STORE : d;
        g.flex.size(ARROW_W, ARROW_H);
        return g;
    }

    /**
     * 等级点：tier 1→●（实心）/ 2→◇（空心菱）/ 3→◆（实心菱），走字体特殊字符，
     * 着色取 {@code text()} 令牌。越界自动夹到 1~3。
     */
    public static StatusGlyph tier(int tier) {
        StatusGlyph g = new StatusGlyph(Kind.TIER);
        g.tier = Draw.clamp(tier, 1, 3);
        String s = tierChar(g.tier);
        g.flex.size(Draw.width(s), Draw.lineHeight());
        return g;
    }

    /** 结果符：ok→✓（statusOk 绿），否则 ✗（textDanger 红）。 */
    public static StatusGlyph result(boolean ok) {
        StatusGlyph g = new StatusGlyph(Kind.RESULT);
        g.ok = ok;
        String s = ok ? "✓" : "✗";
        g.flex.size(Draw.width(s), Draw.lineHeight());
        return g;
    }

    /**
     * 脉冲圆点（八边形 {@link Draw#disc}）。{@code color} 为应用方指定的存在色（语义由调用方定）；
     * 强调动画由应用方调 {@link #firePulse()} 触发一次，组件自身不做循环呼吸。
     */
    public static StatusGlyph pulseDot(int color) {
        StatusGlyph g = new StatusGlyph(Kind.DOT);
        g.color = color;
        g.flex.size(DOT_R * 2 + 1, DOT_R * 2 + 1);
        return g;
    }

    // ------------------------------------------------------------------
    // 链式装配（协变覆写，仍可接着链本类方法）
    // ------------------------------------------------------------------

    @Override public StatusGlyph tooltip(Component... lines) { super.tooltip(lines); return this; }

    @Override public StatusGlyph tooltip(Supplier<List<Component>> supplier) { super.tooltip(supplier); return this; }

    /** 触发一次强调脉冲（缩放 1→1.06→1，300ms，计划动画规格）。由应用方在事件回调里调用。 */
    public StatusGlyph firePulse() { pulse(); return this; }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    protected void renderSelf(GuiGraphicsExtractor g, RenderContext ctx) {
        super.renderSelf(g, ctx); // 空 visuals + 焦点环 + 附件（常态皆空，走零回归快路径）
        float a = ctx.alpha();
        switch (kind) {
            case BARS  -> drawBars(g, a);
            case ARROW -> drawArrow(g, a);
            case TIER  -> drawCenteredText(g, tierChar(tier), Theme.current().text(), a);
            case RESULT -> drawCenteredText(g, ok ? "✓" : "✗",
                    ok ? Theme.current().statusOk() : Theme.current().textDanger(), a);
            case DOT   -> Draw.disc(g, absX + width / 2, absY + height / 2, DOT_R, applyAlpha(color, a));
        }
    }

    /** 档位格：亮格按 {@link #tint} 语义实填圆角小条，暗格 slotBorder（半透）实填。
     *  ACCENT 为原 Timberborn 亮度对比；RED/GREEN/NEUTRAL 为 C.3 分歧式优先级色。 */
    private void drawBars(GuiGraphicsExtractor g, float a) {
        StylePreset skin = Theme.current();
        int litColor = switch (tint) {
            case ACCENT  -> skin.accent();
            case RED     -> skin.textDanger();
            case GREEN   -> skin.statusOk();
            case NEUTRAL -> skin.slotBorder();
        };
        int lit = applyAlpha(litColor, a);
        int dark = applyAlpha(skin.slotBorder(), a);
        for (int i = 0; i < max; i++) {
            int x = absX + i * (CELL_W + CELL_GAP);
            Draw.roundRect(g, x, absY, CELL_W, CELL_H, 1, i < level ? lit : dark);
        }
    }

    /** 方向符号：包围盒内居中；STORE 菱形 = 上半三角 + 下半三角拼合。 */
    private void drawArrow(GuiGraphicsExtractor g, float a) {
        int c = applyAlpha(arrowColor(direction), a);
        int x0 = absX + (width - ARROW_W) / 2;
        int y0 = absY + (height - ARROW_H) / 2;
        switch (direction) {
            case SUPPLY -> triangleUp(g, x0, y0, ARROW_W, ARROW_H, c);
            case DEMAND -> triangleDown(g, x0, y0, ARROW_W, ARROW_H, c);
            case STORE -> {
                int half = ARROW_H / 2;
                triangleUp(g, x0, y0, ARROW_W, half, c);
                triangleDown(g, x0, y0 + half, ARROW_W, ARROW_H - half, c);
            }
        }
    }

    /** 字符形态（●◇◆/✓✗）在包围盒内居中绘制，alpha 随 RenderContext 下传。 */
    private void drawCenteredText(GuiGraphicsExtractor g, String s, int color, float a) {
        int sw = Draw.width(s);
        Draw.text(g, s,
                absX + (width - sw) / 2,
                absY + (height - Draw.lineHeight()) / 2,
                applyAlpha(color, a));
    }

    // ------------------------------------------------------------------
    // 自绘原语：逐行 fill 扫描三角形（Draw 无多边形原语，与 Draw.disc 同哲学）
    // ------------------------------------------------------------------

    /** 顶点在上的实心三角：顶行收窄、底行满宽，每行一条 1px fill。 */
    private static void triangleUp(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        for (int i = 0; i < h; i++) {
            int rowW = Math.min(w, Math.round((float) (i + 1) / h * w));
            Draw.fill(g, x + (w - rowW) / 2, y + i, rowW, 1, argb);
        }
    }

    /** 顶点在下的实心三角：顶行满宽、底行收窄。 */
    private static void triangleDown(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        for (int i = 0; i < h; i++) {
            int rowW = Math.min(w, Math.round((float) (h - i) / h * w));
            Draw.fill(g, x + (w - rowW) / 2, y + i, rowW, 1, argb);
        }
    }

    // ------------------------------------------------------------------
    // 令牌取色（方向语义 → 令牌；不写死色值）
    // ------------------------------------------------------------------

    private int arrowColor(Direction d) {
        StylePreset skin = Theme.current();
        return switch (d) {
            case SUPPLY -> skin.accent();        // 流入：强调蓝
            case DEMAND -> skin.textDanger();   // 流出：警示红
            case STORE -> skin.textDisabled();  // 中转：中性暗
        };
    }

    private static String tierChar(int tier) {
        return switch (tier) {
            case 1 -> "●";
            case 2 -> "◇";
            default -> "◆";
        };
    }

    /** 把 RenderContext 的 alpha 乘到 ARGB 上（a>=1 快路径原样返回），与 TextRenderer 的 alpha 语义一致。 */
    private static int applyAlpha(int argb, float a) {
        if (a >= 0.999F) return argb;
        int base = (int) (((argb >>> 24) & 0xFF) * Draw.clamp01(a));
        return (argb & 0x00FFFFFF) | (base << 24);
    }
}
