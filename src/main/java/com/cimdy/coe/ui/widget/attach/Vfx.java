package com.cimdy.coe.ui.widget.attach;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 特效附件：<b>屏幕内自绘</b>粒子 / 光晕 / 涟漪（{@code GuiGraphics} 手绘矩形 / 圆环 / 像素点）。
 *
 * <p>纪律（计划四·六）：
 * <ul>
 *   <li><b>不产生世界粒子</b>（不污染存档）—— 全部在 GUI 坐标自绘；</li>
 *   <li>单帧特效数上限（默认 64，可配），超出丢弃新请求；</li>
 *   <li>tick 驱动生命周期（位移 / 半径扩散 / alpha 衰减），渲染读同一把时钟；</li>
 *   <li>可选世界粒子通道默认关闭（本层不实现，避免污染）。</li>
 * </ul>
 */
public final class Vfx implements Attachment {

    /** 单帧特效数上限（计划默认 64）。 */
    public static final int MAX_PER_FRAME = 64;

    /**
     * <b>全局特效总开关（附录 D.4：特效默认关）</b>。默认 {@code false} = 任何屏都不画自绘粒子；
     * Screen 经 {@link AttachmentPresets#vfxFor(AttachmentPresets, AttachmentPresets.Target)}
     * 按屏开启本附件（{@code enabled(true)}）后，仍需本总开关打开才实际渲染。
     * 不新增配置菜单（附件纪律 §四·六#4）——以静态开关 + 常量档位落地。
     */
    public static volatile boolean MASTER_ENABLED = false;

    // ---- 附录 D.4 六特效数量档位（同屏活跃 <10；Screen 装配处只读这些常量）----
    /** window_open 粒子扩散粒数（8–14 粒，取中位 11）。 */
    public static final int WINDOW_OPEN_PARTICLES = 11;
    /** pressed 星点粒数（3–6 粒，取中位 5）。 */
    public static final int PRESSED_STARS = 5;
    /** cut_success 刻印闪光圆环数（环形 1 圈）。 */
    public static final int CUT_SUCCESS_RINGS = 1;
    /** teleport 空间涟漪圈数（2 圈）。 */
    public static final int TELEPORT_RIPPLES = 2;
    /** stock_zero 存量清零警示闪烁（仅 StatusGlyph；闪光短寿命，重复触发即闪烁）。 */
    public static final int STOCK_ZERO_FLASH_TICKS = 6;
    /** skill_swap 武器套切换光带条数（1 条）。 */
    public static final int SKILL_SWAP_BANDS = 1;

    /** 特效形态。 */
    public enum Kind { DOT, RING, RIPPLE, GLOW, BAND }

    private static final class Particle {
        final Kind kind;
        float x, y;          // 相对节点左上角
        float vx, vy;
        float radius;
        final float grow;    // 每 tick 半径增量
        int age = 0;
        final int life;      // tick 数
        final int color;
        final float alphaDecay; // 每 tick alpha 衰减量（0..1 直减）

        Particle(Kind kind, float x, float y, float vx, float vy, float radius, float grow,
                 int life, int color, float alphaDecay) {
            this.kind = kind;
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.radius = radius;
            this.grow = grow;
            this.life = Math.max(1, life);
            this.color = color;
            this.alphaDecay = alphaDecay;
        }

        boolean dead() { return age >= life || radius <= 0F; }

        void tick() {
            age++;
            x += vx; y += vy;
            radius += grow;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private int maxPerFrame = MAX_PER_FRAME;
    private boolean enabled = true;

    private static final RandomSource RAND = RandomSource.create();

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public Vfx cap(int max) { this.maxPerFrame = Math.max(1, max); return this; }

    public Vfx enabled(boolean b) { this.enabled = b; return this; }

    public boolean isEnabled() { return enabled; }

    public int count() { return particles.size(); }

    // ------------------------------------------------------------------
    // 生成（坐标相对节点左上角；tick 后由 render 在节点矩形上绘制）
    // ------------------------------------------------------------------

    /** 像素点迸发：count 个点向随机方向散开，用于绑定成功 / 批量添加成功。 */
    public Vfx burst(float x, float y, int count, int color) {
        if (!enabled || !MASTER_ENABLED) return this;
        for (int i = 0; i < count && particles.size() < maxPerFrame; i++) {
            float ang = RAND.nextFloat() * (float) Math.PI * 2F;
            float spd = 0.4F + RAND.nextFloat() * 0.8F;
            particles.add(new Particle(Kind.DOT, x, y,
                    (float) Math.cos(ang) * spd, (float) Math.sin(ang) * spd,
                    1F + RAND.nextFloat(), 0F, 8 + RAND.nextInt(8), color, 0.08F));
        }
        return this;
    }

    /** 扩散圆环（1 帧扩散 + 淡出）：开启光晕 / 切割冲击波 / 涟漪。 */
    public Vfx ring(float cx, float cy, float r0, float rMax, int life, int color) {
        if (!enabled || !MASTER_ENABLED || particles.size() >= maxPerFrame) return this;
        particles.add(new Particle(Kind.RING, cx, cy, 0F, 0F, r0,
                (rMax - r0) / Math.max(1, life), life, color, 1F / Math.max(1, life)));
        return this;
    }

    /** 光晕：径向淡出的亮圈，用于低能量警告 / 激活微光。 */
    public Vfx glow(float cx, float cy, float radius, int life, int color) {
        if (!enabled || !MASTER_ENABLED || particles.size() >= maxPerFrame) return this;
        particles.add(new Particle(Kind.GLOW, cx, cy, 0F, 0F, radius, 0F, life, color, 1F / Math.max(1, life)));
        return this;
    }

    /** 单点：拖拽拖尾 / 残影。 */
    public Vfx dot(float x, float y, int color, int life) {
        if (!enabled || !MASTER_ENABLED || particles.size() >= maxPerFrame) return this;
        particles.add(new Particle(Kind.DOT, x, y, 0F, 0F, 1F, 0F, life, color, 1F / Math.max(1, life)));
        return this;
    }

    /** 横向光带：从中心向两侧展开的细亮条（宽度 = radius*2，高度 2px），武器套切换等。 */
    public Vfx band(float cx, float cy, float halfWidth, int life, int color) {
        if (!enabled || !MASTER_ENABLED || particles.size() >= maxPerFrame) return this;
        particles.add(new Particle(Kind.BAND, cx, cy, 0F, 0F, halfWidth,
                halfWidth / Math.max(1, life), life, color, 1F / Math.max(1, life)));
        return this;
    }

    // ==================================================================
    // 附录 D.4 六特效（命名工厂；坐标相对挂载节点左上角；数量读上方档位常量）
    // ==================================================================

    /** window_open：窗口开启粒子扩散（8–14 粒主题色 + 1 圈淡入）。 */
    public Vfx windowOpen(float cx, float cy, int color) {
        burst(cx, cy, WINDOW_OPEN_PARTICLES, color);
        ring(cx, cy, 6F, 26F, 14, color);
        return this;
    }

    /** pressed：按压星点（3–6 粒）。 */
    public Vfx pressedStars(float x, float y, int color) {
        return burst(x, y, PRESSED_STARS, color);
    }

    /** cut_success：刻印闪光（环形 1 圈，扩散 + 淡出）。 */
    public Vfx cutSuccess(float cx, float cy, int color) {
        for (int i = 0; i < CUT_SUCCESS_RINGS; i++) {
            ring(cx, cy, 4F, 30F, 16, color);
        }
        return this;
    }

    /** teleport：空间涟漪（2 圈错相扩散）。 */
    public Vfx teleportRipple(float cx, float cy, int color) {
        for (int i = 0; i < TELEPORT_RIPPLES; i++) {
            ring(cx, cy, 4F + i * 6F, 34F + i * 8F, 20 + i * 6, color);
        }
        return this;
    }

    /** stock_zero：存量清零警示闪烁（仅 StatusGlyph；短寿命闪光，重复触发即呈闪烁）。 */
    public Vfx stockZeroFlash(float cx, float cy, int color) {
        return glow(cx, cy, 8F, STOCK_ZERO_FLASH_TICKS, color);
    }

    /** skill_swap：武器套切换光带（1 条横向展开亮带）。 */
    public Vfx skillSwapBand(float cx, float cy, int halfWidth, int color) {
        for (int i = 0; i < SKILL_SWAP_BANDS; i++) {
            band(cx, cy, halfWidth, 12, color);
        }
        return this;
    }

    /** 清空全部特效。 */
    public Vfx clear() { particles.clear(); return this; }

    // ------------------------------------------------------------------
    // Attachment 生命周期
    // ------------------------------------------------------------------

    @Override
    public void tick(Node owner) {
        if (!enabled || !MASTER_ENABLED) return;
        for (Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle p = it.next();
            p.tick();
            if (p.dead()) it.remove();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (!enabled || !MASTER_ENABLED || particles.isEmpty()) return;
        for (Particle p : particles) {
            try {
                int px = x + Math.round(p.x);
                int py = y + Math.round(p.y);
                float f = 1F - p.age / (float) p.life;                 // 剩余生命
                int color = fade(p.color, Math.max(0F, f - p.alphaDecay * p.age));
                switch (p.kind) {
                    case DOT -> Draw.fill(g, px, py, Math.max(1, Math.round(p.radius)), Math.max(1, Math.round(p.radius)), color);
                    case RING -> Draw.ring(g, px, py, Math.max(1, Math.round(p.radius)), Math.max(0, Math.round(p.radius) - 1), color);
                    case GLOW -> Draw.ring(g, px, py, Math.max(1, Math.round(p.radius)), Math.max(0, Math.round(p.radius * 0.4F)), color);
                    case RIPPLE -> Draw.disc(g, px, py, Math.max(1, Math.round(p.radius)), color);
                    case BAND -> Draw.fill(g, px - Math.max(1, Math.round(p.radius)), py,
                            Math.max(2, Math.round(p.radius * 2F)), 2, color);
                }
            } catch (RuntimeException ignored) { /* 特效失败静默降级 */ }
        }
    }

    /** 把已烘焙的 alpha 再乘一个 0~1 的衰减系数。 */
    private static int fade(int argb, float a) {
        if (a >= 1F) return argb;
        int baked = (argb >>> 24) & 0xFF;
        int na = (int) (baked * Math.max(0F, Math.min(1F, a)));
        return (argb & 0x00FFFFFF) | (na << 24);
    }
}
