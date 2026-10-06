package com.cimdy.coe.ui.widget.component.assoc;

import java.util.HashMap;
import java.util.Map;

/**
 * 具名关联注册表：把 {@link Association} 按名字登记，供任意组件按名订阅 / 共享。
 *
 * <p>组件解耦的关键：屏幕 A 里的 {@code PolymorphicBar} 和屏幕 B 里的另一个
 * {@code PolymorphicBar} 不需要互相持有引用，只要用<b>同一个名字</b>去注册表取关联，
 * 它们的数值就自动联动（见 {@code PolymorphicBar} 的 {@code "bar:shared"} 用法）。
 *
 * <p>当前实现是<b>独立实例</b>：默认提供一个 {@link #global()} 作为演示入口，
 * 业务可按需 new 出独立作用域。后续若要「屏幕级作用域」（切屏自动失效旧关联），
 * 可把某个实例挂到 {@code WidgetHost}，由 WidgetScreen 在其生命周期内创建 / 销毁——
 * 本类无需改动，调用方只换「从哪里拿 registry」即可。
 */
public class AssociationRegistry {

    private final Map<String, Association<?>> map = new HashMap<>();
    private static final AssociationRegistry GLOBAL = new AssociationRegistry();

    private AssociationRegistry() {}

    /** 全局单例：作为「跨屏幕共享关联」的演示入口。 */
    public static AssociationRegistry global() {
        return GLOBAL;
    }

    /**
     * 取出已存在的关联（类型按调用处推断），不存在则创建并登记。
     * 注意：已存在时直接返回原对象，因此<b>同名即同对象</b>——这正是跨组件共享的保证。
     */
    @SuppressWarnings("unchecked")
    public <T> Association<T> getOrCreate(String name, T initial) {
        Association<?> existing = map.get(name);
        if (existing != null) return (Association<T>) existing;
        Association<T> created = new Association<>(name, initial);
        map.put(name, created);
        return created;
    }

    /** 取出 / 创建<b>数值</b>关联（{@link DoubleAssociation}），让强度插值在数值场景生效。 */
    public DoubleAssociation getOrCreateDouble(String name, double initial) {
        Association<?> existing = map.get(name);
        if (existing instanceof DoubleAssociation d) return d;
        DoubleAssociation created = new DoubleAssociation(name, initial);
        map.put(name, created);
        return created;
    }

    /** 直接登记一个已构造好的关联（如 {@code Axis} 自己创建的 range 关联）。 */
    public void register(Association<?> association) {
        map.put(association.name(), association);
    }

    /** 按名取出关联（原始类型）。 */
    public Association<?> get(String name) {
        return map.get(name);
    }

    /** 按名取出关联（调用处指定类型）。 */
    @SuppressWarnings("unchecked")
    public <T> Association<T> getAs(String name) {
        return (Association<T>) map.get(name);
    }

    /** 注销一个关联。 */
    public void remove(String name) {
        map.remove(name);
    }

    /** 是否存在某名字的关联。 */
    public boolean contains(String name) {
        return map.containsKey(name);
    }
}
