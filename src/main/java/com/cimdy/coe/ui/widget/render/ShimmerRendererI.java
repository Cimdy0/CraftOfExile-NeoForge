package com.cimdy.coe.ui.widget.render;

/**
 * 扫光带的能力面：在 {@link Renderer} 之上补上链式配置方法。
 *
 * <p>具体实现包私有不可见，外部经 {@link Renderers#shimmer(int)} 拿到本接口实例。
 */
public interface ShimmerRendererI extends Renderer {

    ShimmerRendererI color(int c);

    ShimmerRendererI speed(float s);

    ShimmerRendererI band(int b);

    ShimmerRendererI step(int s);

    ShimmerRendererI maxAlpha(float a);

    ShimmerRendererI radius(int r);
}
