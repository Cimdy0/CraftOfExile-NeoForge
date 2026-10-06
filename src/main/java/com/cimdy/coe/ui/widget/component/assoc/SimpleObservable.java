package com.cimdy.coe.ui.widget.component.assoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link Property} 的最通用实现：存一个值，变化（与旧值不相等时）通知所有监听器。
 *
 * <p>这是关联系统的「电池」—— {@link Association} 在它之上叠加「可控加强」语义，
 * {@link Binding} 在它之上叠加「一对多 / 双向」转发。除非需要传播门控 / 强度 / 变换，
 * 绝大多数可绑定字段直接用它就够了。
 *
 * <p>关于「相等」：{@link #set} 默认用 {@link Objects#equals} 判断新旧值，相等则不通知，
 * 以避免无谓的级联刷新（也避免绑定回环时反复触发）。{@link #update} 用于原地修改可变持有对象，
 * 修改后若与修改前不相等才通知。
 *
 * @param <T> 值的类型
 */
public class SimpleObservable<T> implements Property<T> {

    protected T value;
    private final List<ChangeListener<? super T>> listeners = new ArrayList<>();

    public SimpleObservable(T initial) {
        this.value = initial;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        T old = this.value;
        if (Objects.equals(old, value)) return;
        this.value = value;
        notifyListeners(old, value);
    }

    /**
     * 原地修改当前持有的值（针对可变对象，例如一个 {@code Range} 记录的重用），
     * 修改完成后若与修改前不相等则通知监听者。
     */
    public void update(Consumer<T> mutator) {
        T old = this.value;
        mutator.accept(this.value);
        if (!Objects.equals(old, this.value)) notifyListeners(old, this.value);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        listeners.remove(listener);
    }

    /**
     * 通知所有监听者。先经 {@link #shouldPropagate()} 门控，再经 {@link #derive} 派生最终传播值，
     * 最后逐个回调。子类（如 {@link Association}）无需重写本方法，只需覆写这两个受保护钩子即可
     * 插入「传播门控 / 强度 / 变换」语义——从而避免在子类作用域直接触碰 {@code listeners} 字段
     * （该字段使用父类的类型变量，子类作用域内直接访问会引发通配符类型不匹配）。
     */
    protected void notifyListeners(T oldVal, T newVal) {
        if (!shouldPropagate()) return;
        T derived = derive(oldVal, newVal);
        for (ChangeListener<? super T> l : new ArrayList<>(listeners)) {
            l.changed(oldVal, derived);
        }
    }

    /**
     * 传播门控钩子：返回 {@code false} 时 {@link #notifyListeners} 直接吞咽、不通知任何监听者。
     * 默认 {@code true}（恒传播）。子类如 {@link Association} 可覆写为「启用且强度 &gt; 0」。
     */
    protected boolean shouldPropagate() {
        return true;
    }

    /**
     * 派生传播值钩子：返回最终向外通知的值。默认原样返回 {@code newVal}。
     * 子类如 {@link Association} 可覆写为「先变换、再按强度插值」。
     */
    protected T derive(T oldVal, T newVal) {
        return newVal;
    }
}
