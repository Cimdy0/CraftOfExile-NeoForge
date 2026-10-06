package com.cimdy.coe.ui.widget.component.assoc;

import java.util.Objects;
import java.util.function.Function;

/**
 * 具名「可控加强」关联：在 {@link SimpleObservable} 之上增加<b>传播门控 / 强度 / 变换</b>三重语义。
 *
 * <p>它是 Batch C「组件间数据交互 + 关联可控加强」的核心载体：一个关联带名字、可放进
 * {@link AssociationRegistry} 被任意组件按名订阅，且它的<b>传播行为</b>可被运行时调节：
 *
 * <ul>
 *   <li><b>启用 / 禁用</b>（{@link #setEnabled}）：{@code enabled=false} 时 {@link #set} 仍会存值，
 *       但<b>不再向监听者传播</b>。用作「断开某个联动」的开关。</li>
 *   <li><b>强度</b>（{@link #setStrength}，0..1）：{@code strength==0} 等同禁用；{@code strength==1} 足额传播；
 *       {@code (0,1)} 之间时对派生值做插值（见 {@link #blend}），实现「弱耦合」——值只部分跟随。</li>
 *   <li><b>变换</b>（{@link #setTransform}）：传播前先把新值映射一遍（单位换算、钳制、投影等），再参与强度插值。</li>
 * </ul>
 *
 * <p>传播门控只对「向外通知」生效，对「本地存值」无影响——
 * 因此即使一个关联被禁用，组件本地仍可正常改值、本地仍可见变化（只是不再联动别人）。
 * 这正是 {@code PolymorphicBar#weaken(String)} 演示的行为：拖其中一个，另一个不动，但自己照常动。
 *
 * @param <T> 关联值的类型
 */
public class Association<T> extends SimpleObservable<T> {

    private final String name;
    private boolean enabled = true;
    private double strength = 1.0;
    private Function<T, T> transform = Function.identity();

    public Association(String name, T initial) {
        super(initial);
        this.name = name;
    }

    /** 关联名（用于 {@link AssociationRegistry} 注册与按名查找）。 */
    public String name() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** 开 / 关传播。关闭后 {@link #set} 只存值不通知。 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** 当前强度，0..1。 */
    public double getStrength() {
        return strength;
    }

    /** 设置强度，自动夹到 [0,1]；0 等效于禁用传播。 */
    public void setStrength(double strength) {
        this.strength = Math.max(0.0, Math.min(1.0, strength));
    }

    /** 设置传播前的变换；传 null 视为恒等变换。 */
    public void setTransform(Function<T, T> transform) {
        this.transform = (transform == null) ? Function.identity() : transform;
    }

    @Override
    public void set(T value) {
        T old = this.value;
        boolean same = Objects.equals(old, value);
        // 始终存值：传播门控不影响本地状态
        this.value = value;
        if (same) return;
        notifyListeners(old, value);
    }

    /**
     * 传播门控：禁用或强度为 0 时直接吞咽（不通知任何监听者），对「本地存值」无影响。
     * 这是 {@code PolymorphicBar#weaken} 演示「关闭某个联动」的开关落点。
     */
    @Override
    protected boolean shouldPropagate() {
        return enabled && strength > 0.0;
    }

    /**
     * 派生传播值：先经变换，再按强度插值。禁用 / 强度为 0 已由 {@link #shouldPropagate()} 拦截。
     */
    @Override
    protected T derive(T oldVal, T newVal) {
        T mapped = transform.apply(newVal);
        return blend(oldVal, mapped);
    }

    /**
     * 把（旧值、变换后新值）按当前强度派生出最终传播值。
     * 基类默认忽略强度、直接返回变换后的值（即强度对不可插值的类型无意义）。
     * 需要数值插值的场景请用 {@link DoubleAssociation}。覆写时务必基于 {@link #getStrength()}。
     */
    protected T blend(T oldVal, T mapped) {
        return mapped;
    }
}
