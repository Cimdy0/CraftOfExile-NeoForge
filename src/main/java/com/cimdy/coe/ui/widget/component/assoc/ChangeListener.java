package com.cimdy.coe.ui.widget.component.assoc;

/**
 * 值变化监听器。
 *
 * <p>在「可观察值」{@link ObservableValue} 发生变化时回调，携带旧值与新值。
 * 这是整个关联系统（{@link Association} / {@link Binding}）的最小回调契约，
 * 业务方通常写成 lambda {@code (oldV, newV) -> ...}。
 *
 * @param <T> 被观察的值类型
 */
@FunctionalInterface
public interface ChangeListener<T> {

    /**
     * 值已变化。
     *
     * @param oldVal 变化前的值
     * @param newVal 变化后的值（经 {@link Association} 的 transform / strength 派生后的值）
     */
    void changed(T oldVal, T newVal);
}
