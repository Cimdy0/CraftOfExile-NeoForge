package com.cimdy.coe.ui.widget.anim;

import com.cimdy.coe.ui.widget.render.Draw;

/**
 * hover 进入/离开的颜色与缩放过渡持有器 —— 统一替代瞬时 hover 切换。
 *
 * <p>每帧由组件在渲染里喂入当前 hover 态（{@code ctx.state().hovered()}），
 * 内部用 {@link Animator} 让 0..1 的 hover 量平滑趋近（进入/离开各自 100–150ms，
 * 符合计划时长规范），反向立即回弹、不排队。
 *
 * <p>注意：纯背景色 hover 已有 {@code ColorRenderer.smooth(true)} 内置缓动
 * （{@code Theme.button()} 等皮肤默认开启）——本类用于「非背景属性」的过渡
 * （缩放、图标提亮、箭头翻转、文字色），或需要自定义时长的场景；
 * 同一组件上两者不应叠用在同一属性上。
 */
public class HoverLerp {

    /** 计划时长规范：hover 100–150ms。 */
    public static final int DEFAULT_MS = 120;

    private final Animator animator;

    public HoverLerp() { this(DEFAULT_MS); }

    public HoverLerp(int ms) {
        animator = new Animator(0F, 0F, Math.max(1, ms), Easing.EASE_OUT_CUBIC);
    }

    /** 每帧喂入 hover 状态；状态变化时自动 retarget（可中断、立即回弹）。 */
    public void update(boolean hovered) {
        animator.retarget(hovered ? 1F : 0F);
    }

    /** 当前 hover 量 0..1（进入时趋近 1，离开时趋近 0）。 */
    public float amount() { return animator.value(); }

    public boolean isHovered() { return animator.to() >= 1F; }

    // ---- 派生：颜色 / 缩放 ----

    /** 两色按 hover 量混合（alpha 通道一并插值）。 */
    public int color(int base, int hover) { return Draw.blend(base, hover, amount()); }

    /** 缩放 = 基准 + 增量×hover 量（如按钮 hover 1 → 1.03）。 */
    public float scale(float base, float hoverDelta) {
        return base + hoverDelta * amount();
    }
}
