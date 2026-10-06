package com.cimdy.coe.ui.widget.render;

/**
 * 脉冲发光边框的能力面：在 {@link Renderer} 之上补上链式配置方法。
 *
 * <p>具体实现包私有不可见，外部经 {@link Renderers#glow(int)} 拿到本接口实例。
 */
public interface GlowBorderRendererI extends Renderer {

    GlowBorderRendererI color(int c);

    GlowBorderRendererI speed(float s);

    GlowBorderRendererI maxAlpha(float a);

    GlowBorderRendererI thickness(int t);

    GlowBorderRendererI radius(int r);

    GlowBorderRendererI halo(boolean b);
}
