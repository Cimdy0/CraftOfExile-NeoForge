package com.cimdy.coe.ui.widget.component.assoc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 绑定：把两个可观察值连起来，并在不需要时干净地断开。
 *
 * <p>这是「子组件 → 父组件」「组件 ↔ 组件」数据交互的接线工具。两种模式：
 *
 * <ul>
 *   <li>{@link #bind}：单向（src → dst），用转换函数 {@code conv} 把源值映射成目标类型；
 *       源变化时写入目标，目标的变化不会回灌源。</li>
 *   <li>{@link #bindBidirectional}：双向（a ↔ b），分别给定 a→b、b→a 两个转换函数；
 *       任一侧变化都会同步到另一侧，且<b>自带重入锁</b>避免回环死循环。</li>
 * </ul>
 *
 * <p>返回的是 {@link Binding} 句柄，持有所有解绑动作；调用 {@link #unbind()} 即可一次性拆除，
 * 适合组件 dispose / 屏幕切换时清理（避免悬挂监听器导致的内存泄漏与幽灵联动）。
 *
 * <p>典型用法（见 {@code PolymorphicBar}）：
 * <pre>{@code
 * Binding b = Binding.bindBidirectional(
 *         parent.value(), bar.value(),
 *         Function.identity(), Function.identity());
 * // 稍后 b.unbind();
 * }</pre>
 */
public final class Binding {

    private final List<Runnable> unbinds = new ArrayList<>();

    private Binding(List<Runnable> unbinds) {
        this.unbinds.addAll(unbinds);
    }

    /** 拆除本绑定登记的全部监听器。幂等：重复调用安全。 */
    public void unbind() {
        for (Runnable r : unbinds) r.run();
        unbinds.clear();
    }

    /** 单向绑定：src 变化时写 dst = conv(src)。dst 的变化不回灌 src。 */
    public static <S, T> Binding bind(ObservableValue<S> src, Property<T> dst, Function<S, T> conv) {
        ChangeListener<S> listener = (oldV, newV) -> dst.set(conv.apply(newV));
        src.addListener(listener);
        List<Runnable> u = new ArrayList<>();
        u.add(() -> src.removeListener(listener));
        return new Binding(u);
    }

    /**
     * 双向绑定：a ↔ b。任一侧变化同步到另一侧，重入锁确保回环时只传播一次，不死循环。
     *
     * @param a2b a 变化 → 写入 b 时的转换
     * @param b2a b 变化 → 写入 a 时的转换
     */
    public static <A, B> Binding bindBidirectional(Property<A> a, Property<B> b,
                                                  Function<A, B> a2b, Function<B, A> b2a) {
        boolean[] lock = {false};
        ChangeListener<A> la = (oldV, newV) -> {
            if (lock[0]) return;
            lock[0] = true;
            try {
                b.set(a2b.apply(newV));
            } finally {
                lock[0] = false;
            }
        };
        ChangeListener<B> lb = (oldV, newV) -> {
            if (lock[0]) return;
            lock[0] = true;
            try {
                a.set(b2a.apply(newV));
            } finally {
                lock[0] = false;
            }
        };
        a.addListener(la);
        b.addListener(lb);
        List<Runnable> u = new ArrayList<>();
        u.add(() -> a.removeListener(la));
        u.add(() -> b.removeListener(lb));
        return new Binding(u);
    }
}
