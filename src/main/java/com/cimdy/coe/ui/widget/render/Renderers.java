package com.cimdy.coe.ui.widget.render;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * 渲染器门面：lib 对外的唯一具体渲染器构造入口。
 *
 * <p>所有具体实现类都是包私有的，外部只通过本类拿 {@code *RendererI} / {@code Renderer} /
 * {@code ValueBarView} 这类能力接口，不直接依赖具体类。
 */
public final class Renderers {

    private Renderers() {}

    // ---- 文本 ----
    public static TextRendererI text(Component text) { return TextRenderer.of(text); }
    public static TextRendererI text(String text) { return TextRenderer.of(text); }
    public static TextRendererI textLines(Component... texts) { return TextRenderer.ofLines(texts); }
    public static TextRendererI textLines(List<Component> texts) { return TextRenderer.ofLines(texts); }

    // ---- 文本输入层 ----
    public static TextFieldRendererI textField(TextView view) { return new TextFieldRenderer(view); }

    // ---- 物品 ----
    public static ItemRendererI item(Supplier<ItemStack> source) { return new ItemRenderer(source); }

    // ---- 生物预览 ----
    public static EntityPreviewI entityPreview(Supplier<LivingEntity> source) { return new EntityPreviewRenderer(source); }

    // ---- 开关 ----
    public static ToggleRendererI toggle(Supplier<Boolean> on) { return new ToggleRenderer(on); }

    // ---- 滚动滑块 ----
    public static ScrollThumbI scrollThumb(Renderer thumb, DoubleSupplier progress) {
        return new ScrollThumbRenderer(thumb, progress);
    }
    public static int thumbLength(int track, double visibleRatio, int minLength, int fixedLength) {
        return ScrollThumbRenderer.thumbLength(track, visibleRatio, minLength, fixedLength);
    }

    // ---- 四角装饰 ----
    public static CornerTicksI cornerTicks(int padding, int length, int argb) {
        return new CornerTicksRenderer(padding, length, argb);
    }

    // ---- 纯色背景 / 边框 ----
    public static ColorRendererI color(int backgroundArgb) { return ColorRenderer.of(backgroundArgb); }
    public static ColorRendererI color(int backgroundArgb, int borderArgb) { return ColorRenderer.of(backgroundArgb, borderArgb); }
    public static ColorRendererI borderOnly(int borderArgb) { return ColorRenderer.borderOnly(borderArgb); }

    // ---- 特效层（返回能力接口，直接 with 到节点） ----
    public static GlowBorderRendererI glow(int color) { return new GlowBorderRenderer(color); }
    public static ShadowRendererI shadow(int offset, int blur, int radius, int argb) { return new ShadowRenderer(offset, blur, radius, argb); }
    public static ShimmerRendererI shimmer(int color) { return new ShimmerRenderer(color); }

    // ---- 数值条 ----
    public static ValueBarView valueBar(Supplier<List<Segment>> segments, Supplier<List<Tick>> ticks) {
        return new ValueBarRenderer(segments, ticks);
    }
}
