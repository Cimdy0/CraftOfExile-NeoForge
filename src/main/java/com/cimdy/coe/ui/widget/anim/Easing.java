package com.cimdy.coe.ui.widget.anim;

/**
 * 缓动曲线（全库唯一实现点）。
 *
 * <p>此前 {@code SidePanel} 内部私有一份 {@code easeOutCubic}，这里提升为共享枚举，
 * 动画系统与各组件一律从这里取曲线，不散写第二份。
 */
public enum Easing {

    LINEAR(t -> t),

    EASE_IN_QUAD(t -> t * t),

    EASE_OUT_QUAD(t -> {
        float i = 1F - t;
        return 1F - i * i;
    }),

    EASE_IN_OUT_QUAD(t -> t < 0.5F ? 2F * t * t : 1F - (float) Math.pow(-2F * t + 2F, 2) / 2F),

    /** 提升自 {@code SidePanel}：先快后慢的收尾曲线，面板/滑块类动画默认曲线。 */
    EASE_OUT_CUBIC(t -> {
        float i = 1F - t;
        return 1F - i * i * i;
    }),

    EASE_IN_OUT_CUBIC(t -> t < 0.5F ? 4F * t * t * t : 1F - (float) Math.pow(-2F * t + 2F, 3) / 2F),

    /** 回弹曲线：越过目标再弹回，用于 pop / 强调脉冲。 */
    EASE_OUT_BACK(t -> {
        float c1 = 1.70158F, c3 = c1 + 1F;
        return 1F + c3 * (float) Math.pow(t - 1F, 3) + c1 * (float) Math.pow(t - 1F, 2);
    });

    private final Apply fn;

    Easing(Apply fn) { this.fn = fn; }

    /** 对归一化进度 t∈[0,1] 施加曲线，输出仍是 [0,1]（EASE_OUT_BACK 可能短暂越界）。 */
    public float apply(float t) {
        return fn.apply(Math.max(0F, Math.min(1F, t)));
    }

    @FunctionalInterface
    private interface Apply { float apply(float t); }
}
