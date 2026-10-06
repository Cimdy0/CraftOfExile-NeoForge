package com.cimdy.coe.ui.widget.anim;

import com.cimdy.coe.ui.widget.render.StylePreset;
import com.cimdy.coe.ui.widget.render.Theme;

/**
 * 动画注册表（附录 D.1，第 1 层<b>唯一实现点</b>）。
 *
 * <p>六类触发（mount / unmount / hover / pressed / selected / value）的时长 / 强度令牌从
 * {@link Theme#current()} 的动画令牌组读取（皮肤可覆写），这里不写死任何秒表数值；
 * 全模组的补间预算、全局开关与 reduced-motion 退化也收口到这一个点，组件不再各自判断。
 *
 * <p>纪律（D.0）：
 * <ul>
 *   <li>动画只作<b>渲染层参数变化</b>（alpha / offset / scale / glow），不改布局坐标与命中区；</li>
 *   <li>{@link Theme#ANIMATION_ENABLED} 关闭或 MC 辅助设置「减少动态效果」开启时，
 *       所有补间<b>瞬时落到静止终帧</b>（{@link Animator} 经 {@link #animationsActive()} 退化）；</li>
 *   <li>同屏活跃补间 ≤ {@link #MAX_ACTIVE}（§四・五全局预算），超出时新补间直接丢弃（瞬时完成，不排队）；
 *       hover 类由 {@code HoverLerp} 即时 retarget，不累积占预算。</li>
 * </ul>
 */
public final class AnimRegistry {

    private AnimRegistry() {}

    // ------------------------------------------------------------------
    // 六类触发（D.1 表）
    // ------------------------------------------------------------------

    /** 六类动画触发。时长 / 强度令牌读 {@link StylePreset#animMs}。 */
    public enum Trigger {
        /** 进场：淡入 + 上滑 8px，列表项 stagger。 */
        MOUNT,
        /** 退场：淡出 + 下滑 6px。 */
        UNMOUNT,
        /** 悬停：亮度 / 辉光渐变（tooltip 延迟淡入 150ms）。 */
        HOVER,
        /** 按压：缩放 0.94 即时 + 回弹。 */
        PRESSED,
        /** 选中：指示条滑动、卡片边框光晕。 */
        SELECTED,
        /** 数值：进度 / 滑块流动、数值滚动。 */
        VALUE
    }

    // ------------------------------------------------------------------
    // 全局开关 / reduced-motion / 预算
    // ------------------------------------------------------------------

    /** 同屏活跃补间上限（§四・五）。超出按「新补间丢弃、瞬时完成」裁决，绝不排队堆积。 */
    public static final int MAX_ACTIVE = 64;

    private static int activeCount = 0;

    /**
     * 动画是否应当播放（D.0 总闸）。
     * 全局开关开启 <b>且</b> 未启用 reduced-motion 时才播放；否则一律退化到静止终帧。
     */
    public static boolean animationsActive() {
        if (!Theme.ANIMATION_ENABLED) return false;
        return !motionReduced();
    }

    // ------------------------------------------------------------------
    // 令牌读取（组件统一从这里取时长 / 强度，不散落字面量）
    // ------------------------------------------------------------------

    /** 某触发类别的时长（ms），读当前皮肤动画令牌。 */
    public static int ms(Trigger t) {
        return Theme.current().animMs(switch (t) {
            case MOUNT    -> StylePreset.AnimKind.MOUNT;
            case UNMOUNT  -> StylePreset.AnimKind.UNMOUNT;
            case HOVER    -> StylePreset.AnimKind.HOVER;
            case PRESSED  -> StylePreset.AnimKind.PRESSED;
            case SELECTED -> StylePreset.AnimKind.SELECTED;
            case VALUE    -> StylePreset.AnimKind.VALUE;
        });
    }

    /** mount 进场上滑距离（px）。 */
    public static int mountSlidePx() { return Theme.current().animMountSlidePx(); }

    /** unmount 退场下滑距离（px）。 */
    public static int unmountSlidePx() { return Theme.current().animUnmountSlidePx(); }

    /** pressed 按压缩放倍率（<1）。 */
    public static float pressedScale() { return Theme.current().animPressedScale(); }

    /** 窗口进场起始缩放（<1，0.98→1）。 */
    public static float openStartScale() { return Theme.current().animOpenStartScale(); }

    // ------------------------------------------------------------------
    // 全局补间预算（包私有，仅 {@link Animator} 进出账）
    // ------------------------------------------------------------------

    /** 占用一个预算名额；已满则返回 false（调用方应瞬时完成、不起补间）。 */
    static synchronized boolean acquire() {
        if (activeCount >= MAX_ACTIVE) return false;
        activeCount++;
        return true;
    }

    /** 归还一个预算名额。 */
    static synchronized void release() {
        if (activeCount > 0) activeCount--;
    }

    // ------------------------------------------------------------------
    // reduced-motion（MC 辅助设置「减少动态效果」）
    //
    // 经反射解析 Options 上的布尔/OptionInstance 访问器，编译期不绑定具体方法名 ——
    // 任一本版号若没有该选项，解析为空即恒 false（零回归）；命中则整组动画退化为静止终帧。
    // 结果缓存 500ms，避免每帧反射。
    // ------------------------------------------------------------------

    private static java.lang.reflect.Method motionMethod;
    private static boolean motionResolved;
    private static long motionCacheUntil;
    private static boolean motionCached;

    /** MC 辅助设置是否启用「减少动态效果」（无该选项时恒 false）。 */
    public static boolean motionReduced() {
        long now = System.currentTimeMillis();
        if (now < motionCacheUntil) return motionCached;
        motionCacheUntil = now + 500L;
        motionCached = resolveMotionReduced();
        return motionCached;
    }

    private static boolean resolveMotionReduced() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || mc.options == null) return false;
            if (!motionResolved) {
                motionResolved = true;
                for (java.lang.reflect.Method m : mc.options.getClass().getMethods()) {
                    if (m.getParameterCount() != 0) continue;
                    String n = m.getName().toLowerCase(java.util.Locale.ROOT);
                    if (n.contains("reducedmotion") || n.equals("motionreduced") || n.contains("reducemotion")) {
                        motionMethod = m;
                        break;
                    }
                }
            }
            if (motionMethod == null) return false;
            Object v = motionMethod.invoke(mc.options);
            if (v instanceof Boolean b) return b;
            // OptionInstance<Boolean>：再找一个无参 get() 取值（不硬绑定类名，兼容映射差异）
            if (v != null) {
                try {
                    java.lang.reflect.Method g = v.getClass().getMethod("get");
                    Object gv = g.invoke(v);
                    if (gv instanceof Boolean b) return b;
                } catch (ReflectiveOperationException ignored) { /* 非 OptionInstance 形态 */ }
            }
        } catch (Throwable ignored) { /* 任何反射异常一律按「未启用」处理 */ }
        return false;
    }
}
