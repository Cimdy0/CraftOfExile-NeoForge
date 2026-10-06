package com.cimdy.coe.ui.widget.component.assoc;

/**
 * 可写可观察值：在 {@link ObservableValue} 之上增加 {@code set}。
 *
 * <p>组件对外暴露的「可绑定字段」几乎都是 {@code Property}：既能被关联系统写入（来自其它组件 / 关联），
 * 也能在变化时通知监听者。{@link SimpleObservable} 是它最通用的实现。
 *
 * @param <T> 值的类型
 */
public interface Property<T> extends ObservableValue<T> {

    /** 设置新值；当新值与旧值「相等」（{@link java.util.Objects#equals}）时实现应跳过通知。 */
    void set(T value);

    /**
     * 便捷监听：值变化时仅以「新值」回调 {@code c}。
     * 等价于 {@code this.addListener((o, n) -> c.accept(n))}，行为不变。
     */
    default void onChange(java.util.function.Consumer<? super T> c) {
        this.addListener((o, n) -> c.accept(n));
    }
}
