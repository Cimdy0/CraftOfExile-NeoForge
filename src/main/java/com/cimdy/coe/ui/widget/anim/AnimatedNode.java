package com.cimdy.coe.ui.widget.anim;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.RenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * 带动画的节点基类：把 {@link NodeAnim} 施加到「自身 + 整棵子树」的渲染上。
 *
 * <p>纯视觉增强 —— 动画不改变布局坐标（absX/absY/width/height 照旧），
 * 命中测试与 tooltip 仍基于布局坐标，输入链路不受影响。
 *
 * <p>用法：组件把 {@code extends Node} 改为 {@code extends AnimatedNode}（零回归：未挂动画时
 * 走与 Node 完全相同的路径），再调用 {@link #fadeTo}/{@link #slideTo}/{@link #scaleTo}/{@link #pulse}
 * 等原语；每帧由 {@link #tick()}（经 {@code Node.tick()} 树递归）推进，渲染采样同一把时钟。
 *
 * <p>透明度沿 {@link RenderContext#alpha()} 下传：子节点的
 * {@code ColorRenderer} / {@code TextRenderer} 会把颜色 alpha 乘以该值（alpha=1 时不改变行为）。
 */
public class AnimatedNode extends Node {

    private NodeAnim anim;

    /** 四个渲染属性各自的补间（alpha / offsetX / offsetY / scale），null = 无动画。 */
    private Animator alphaTween;
    private Animator offsetTweenX;
    private Animator offsetTweenY;
    private Animator scaleTween;

    // ------------------------------------------------------------------
    // 状态访问
    // ------------------------------------------------------------------

    /** 惰性创建动画状态持有器。 */
    public NodeAnim anim() {
        if (anim == null) anim = new NodeAnim();
        return anim;
    }

    public boolean hasAnim() { return anim != null && anim.hasTransform(); }

    // ------------------------------------------------------------------
    // 动画原语（Fade / Slide / Scale / Pulse）
    // ------------------------------------------------------------------

    /** 淡入（alpha 0 → 1）。 */
    public AnimatedNode fadeIn(int ms) { return fadeTo(1F, ms); }

    /** 淡出（alpha 1 → 0）。 */
    public AnimatedNode fadeOut(int ms) { return fadeTo(0F, ms); }

    /** 透明度淡切。 */
    public AnimatedNode fadeTo(float target, int ms) {
        alphaTween = new Animator(anim().alpha, target, ms, Easing.EASE_OUT_CUBIC);
        return this;
    }

    /** 平移（offsetX/offsetY → 目标）。 */
    public AnimatedNode slideTo(int dx, int dy, int ms) {
        offsetTweenX = new Animator(anim().offsetX, dx, ms, Easing.EASE_OUT_CUBIC);
        offsetTweenY = new Animator(anim().offsetY, dy, ms, Easing.EASE_OUT_CUBIC);
        return this;
    }

    /** 缩放到目标倍率。 */
    public AnimatedNode scaleTo(float target, int ms) {
        scaleTween = new Animator(anim().scale, target, ms, Easing.EASE_OUT_CUBIC);
        return this;
    }

    /** 强调脉冲：1 → 1+peak → 1（默认 peak 0.06，300ms 一次，计划时长规范）。 */
    public AnimatedNode pulse() { return pulse(0.06F, 300); }

    public AnimatedNode pulse(float peak, int ms) {
        Animator up = new Animator(anim().scale, 1F + peak, ms / 2, Easing.EASE_OUT_CUBIC);
        scaleTween = up;
        up.onComplete(() -> {
            if (scaleTween == up) {
                scaleTween = new Animator(1F + peak, 1F, ms - ms / 2, Easing.EASE_OUT_BACK);
            }
        });
        return this;
    }

    // ------------------------------------------------------------------
    // 渲染：施加变换
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphicsExtractor g, RenderContext ctx, boolean parentDisabled) {
        if (anim == null || !anim.hasTransform()) {
            super.render(g, ctx, parentDisabled);
            return;
        }
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        float cx = absX + width / 2F, cy = absY + height / 2F;
        pose.translate(cx + anim.offsetX, cy + anim.offsetY);
        pose.scale(anim.scale, anim.scale);
        pose.translate(-cx, -cy);
        float a = Math.max(0F, Math.min(1F, anim.alpha));
        RenderContext c = a >= 1F ? ctx : ctx.withAlpha(ctx.alpha() * a);
        super.render(g, c, parentDisabled);
        pose.popMatrix();
    }

    // ------------------------------------------------------------------
    // tick：推进补间并写回 anim
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        if (alphaTween != null) {
            alphaTween.tick();
            anim().alpha = alphaTween.value();
            if (!alphaTween.isRunning()) alphaTween = null;
        }
        if (offsetTweenX != null) {
            offsetTweenX.tick();
            anim().offsetX = Math.round(offsetTweenX.value());
            if (!offsetTweenX.isRunning()) offsetTweenX = null;
        }
        if (offsetTweenY != null) {
            offsetTweenY.tick();
            anim().offsetY = Math.round(offsetTweenY.value());
            if (!offsetTweenY.isRunning()) offsetTweenY = null;
        }
        if (scaleTween != null) {
            scaleTween.tick();
            anim().scale = scaleTween.value();
            if (!scaleTween.isRunning()) scaleTween = null;
        }
        super.tick();
    }
}
