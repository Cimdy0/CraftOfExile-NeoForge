package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.core.VisualState;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.GuiSkin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * 「有贴图就用贴图，没贴图就退回硬渲染」的皮肤渲染器 —— 渐进式贴图化的核心。
 *
 * <p>组件只声明它想要哪张皮肤，本渲染器首帧探测贴图是否存在：
 * <ul>
 *   <li>贴图<b>已存在</b> → 用贴图绘制，尺寸取自布局结果（sprite 模式下九宫格由原版按 mcmeta 处理）；</li>
 *   <li>贴图<b>不存在</b> → 调用 {@link #fallback(Renderer)}，行为与没上贴图时完全一致。</li>
 * </ul>
 * 所以可以先把所有面板都挂上皮肤声明，美术做一张生效一张，做到一半也不会有界面出问题。
 *
 * <h3>相比旧 SkinnedRenderer 的改进</h3>
 * <ul>
 *   <li>状态变体不再只有 hover：自动探测 {@code _hover} / {@code _active} / {@code _disabled}，
 *       探到哪个用哪个，探不到就回落常态贴图（而不是回落 fallback）；</li>
 *   <li>不再硬编码 MODID —— 命名空间由调用方给，或直接传一个 {@link GuiSkin}；</li>
 *   <li>hover 判定来自 {@link RenderContext#state()}，不再自己算鼠标矩形。</li>
 * </ul>
 *
 * <h3>贴图放哪</h3>
 * sprite 模式：{@code assets/<ns>/textures/gui/sprites/<path>.png}，
 * 旁边可放 {@code <name>.png.mcmeta} 声明 {@code nine_slice} / {@code stretch} / {@code tile}，
 * 九宫格拉伸由原版负责，我们一行算法都不写。
 */
class SkinRenderer implements Renderer {

    /** 贴图寻址方式。 */
    public enum Mode {
        /** 走 GUI 图集，支持 mcmeta 的九宫格/拉伸/平铺，尺寸自适应 */
        SPRITE,
        /** 直接贴图文件，按原始尺寸绘制 */
        TEXTURE
    }

    /** TEXTURE 模式下贴图与组件区域的贴合方式（SPRITE 模式由 mcmeta 决定，此项无效）。 */
    public enum Fit {
        /** 原始尺寸，贴左上角 */
        ORIGINAL,
        /** 原始尺寸，区域内居中 */
        CENTER,
        /** 以原始尺寸为单元平铺填满 */
        TILE
    }

    /** 皮肤的状态变体，对应贴图文件名后缀。 */
    private enum Variant {
        NORMAL(""), HOVER("_hover"), ACTIVE("_active"), DISABLED("_disabled");

        final String suffix;
        Variant(String suffix) { this.suffix = suffix; }
    }

    private final GuiSkin skin;
    private final Mode mode;
    /** SPRITE 模式：不含前缀与扩展名；TEXTURE 模式：相对 textures/ 的完整路径（含扩展名） */
    private final String path;

    private Fit fit = Fit.ORIGINAL;
    private int tint = 0xFFFFFFFF;
    private int texWidth = 16;
    private int texHeight = 16;

    private Renderer fallback;

    /** 惰性探测缓存：未探测的 key 不存在 */
    private final Map<Variant, Identifier> resolved = new EnumMap<>(Variant.class);
    private final Map<Variant, Boolean> probed = new EnumMap<>(Variant.class);

    private SkinRenderer(GuiSkin skin, Mode mode, String path) {
        this.skin = skin;
        this.mode = mode;
        this.path = path;
    }

    // ------------------------------------------------------------------
    // 构造
    // ------------------------------------------------------------------

    /** sprite 模式（推荐），对应 {@code textures/gui/sprites/<path>.png}。 */
    public static SkinRenderer sprite(String namespace, String path) {
        return new SkinRenderer(GuiSkin.of(namespace), Mode.SPRITE, path);
    }

    public static SkinRenderer sprite(GuiSkin skin, String path) {
        return new SkinRenderer(skin, Mode.SPRITE, path);
    }

    /** 直接贴图模式，对应 {@code textures/<path>}，按原始尺寸绘制。 */
    public static SkinRenderer texture(String namespace, String path, int texW, int texH) {
        return texture(GuiSkin.of(namespace), path, texW, texH);
    }

    public static SkinRenderer texture(GuiSkin skin, String path, int texW, int texH) {
        SkinRenderer r = new SkinRenderer(skin, Mode.TEXTURE, path);
        r.texWidth = texW;
        r.texHeight = texH;
        return r;
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    /** 贴图缺失时的退路渲染器；不设置则贴图缺失时什么都不画。 */
    public SkinRenderer fallback(Renderer fallback) { this.fallback = fallback; return this; }

    /** TEXTURE 模式下的贴合方式。 */
    public SkinRenderer fit(Fit fit) { this.fit = fit; return this; }

    /** 叠加颜色 ARGB，默认原色。 */
    public SkinRenderer tint(int argb) { this.tint = argb; return this; }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (!probe(Variant.NORMAL)) {
            if (fallback != null) fallback.render(g, x, y, w, h, ctx);
            return;
        }

        Identifier id = resolved.get(pick(ctx.state()));
        if (id == null) return;

        if (mode == Mode.SPRITE) {
            // 尺寸直接用布局结果，九宫格/拉伸交给原版按 mcmeta 处理
            g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h, tint);
        } else {
            drawTexture(g, id, x, y, w, h);
        }
    }

    /** 按状态挑一个已就绪的变体，逐级回落到常态。 */
    private Variant pick(VisualState s) {
        if (s.disabled() && probe(Variant.DISABLED)) return Variant.DISABLED;
        if (s.active() && probe(Variant.ACTIVE)) return Variant.ACTIVE;
        if (s.hovered() && probe(Variant.HOVER)) return Variant.HOVER;
        return Variant.NORMAL;
    }

    private void drawTexture(GuiGraphicsExtractor g, Identifier id, int x, int y, int w, int h) {
        int tw = Math.max(1, texWidth);
        int th = Math.max(1, texHeight);
        switch (fit) {
            case CENTER -> {
                int dx = x + (w - tw) / 2;
                int dy = y + (h - th) / 2;
                g.blit(RenderPipelines.GUI_TEXTURED, id, dx, dy, 0, 0, tw, th, tw, th);
            }
            case TILE -> {
                for (int oy = 0; oy < h; oy += th) {
                    for (int ox = 0; ox < w; ox += tw) {
                        int dw = Math.min(tw, w - ox);
                        int dh = Math.min(th, h - oy);
                        g.blit(RenderPipelines.GUI_TEXTURED, id, x + ox, y + oy, 0, 0, dw, dh, tw, th);
                    }
                }
            }
            default -> g.blit(RenderPipelines.GUI_TEXTURED, id, x, y, 0, 0, tw, th, tw, th);
        }
    }

    // ------------------------------------------------------------------
    // 惰性探测
    // ------------------------------------------------------------------

    private boolean probe(Variant v) {
        Boolean cached = probed.get(v);
        if (cached != null) return cached;

        String p = withSuffix(path, v.suffix);
        boolean found = mode == Mode.SPRITE ? skin.spriteExists(p) : skin.textureExists(p);
        if (found) {
            resolved.put(v, mode == Mode.SPRITE ? skin.sprite(p) : skin.texture(p));
        }
        probed.put(v, found);
        return found;
    }

    /** {@code widget/button} + {@code _hover} → {@code widget/button_hover}；扩展名前插入。 */
    private static String withSuffix(String path, String suffix) {
        if (suffix.isEmpty()) return path;
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path + suffix : path.substring(0, dot) + suffix + path.substring(dot);
    }

    // ------------------------------------------------------------------
    // 框架契约
    // ------------------------------------------------------------------

    @Override
    public int preferredWidth() {
        return mode == Mode.TEXTURE ? texWidth : (fallback != null ? fallback.preferredWidth() : 0);
    }

    @Override
    public int preferredHeight() {
        return mode == Mode.TEXTURE ? texHeight : (fallback != null ? fallback.preferredHeight() : 0);
    }

    @Override
    public boolean isAnimated() {
        return fallback != null && fallback.isAnimated();
    }

    @Override
    public void tick() {
        if (fallback != null) fallback.tick();
    }

    @Override
    public Renderer copy() {
        SkinRenderer r = new SkinRenderer(skin, mode, path);
        r.fit = fit;
        r.tint = tint;
        r.texWidth = texWidth;
        r.texHeight = texHeight;
        r.fallback = fallback == null ? null : fallback.copy();
        // 探测结果可安全复用，省一次 IO
        r.resolved.putAll(resolved);
        r.probed.putAll(probed);
        return r;
    }
}
