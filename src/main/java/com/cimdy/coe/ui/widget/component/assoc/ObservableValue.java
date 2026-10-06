package com.cimdy.coe.ui.widget.component.assoc;

/**
 * 可观察值：对外只暴露「读」与「订阅」，写由子接口 {@link Property} 负责。
 *
 * <p>这是关联系统的一等公民底座 —— {@link Association} 与 {@link Binding} 都建立在它之上。
 * 它刻意不绑定任何 UI / 渲染概念，因此同一套数据交互机制既能驱动界面，也能驱动纯逻辑（如对战数值、配置项）。
 *
 * @param <T> 值的类型
 */
public interface ObservableValue<T> {

    /** 当前值。 */
    T get();

    /** 注册一个变化监听器。 */
    void addListener(ChangeListener<? super T> listener);

    /** 移除一个变化监听器。 */
    void removeListener(ChangeListener<? super T> listener);
}
