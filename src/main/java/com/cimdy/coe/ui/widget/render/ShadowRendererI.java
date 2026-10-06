package com.cimdy.coe.ui.widget.render;

/**
 * 柔和投影的能力面：在 {@link Renderer} 之上补上链式配置方法。
 *
 * <p>具体实现包私有不可见，外部经 {@link Renderers#shadow(int,int,int,int)} 拿到本接口实例。
 */
public interface ShadowRendererI extends Renderer {

    ShadowRendererI offset(int o);

    ShadowRendererI blur(int b);

    ShadowRendererI radius(int r);

    ShadowRendererI color(int c);
}
