package com.cimdy.coe.ui.screen.radial;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * 径向（轮盘）菜单的节点模型。
 *
 * <ul>
 *   <li>叶子节点（{@link #action}）携带一个 {@link Runnable}，触发后关闭菜单；</li>
 *   <li>分支节点（{@link #submenu}）携带子节点，点击进入下一级子菜单；</li>
 *   <li>子节点支持<b>惰性提供</b>（{@link #submenu(String, Component, ItemStack, Supplier)}），
 *       以便像「天赋树列表」这类完全取决于数据包内容的集合在打开时才解析——
 *       新增一棵树无需任何代码，做到零代码扩展。</li>
 * </ul>
 */
public final class RadialNode {
    public final String id;
    public final Component title;
    public final ItemStack icon;
    public final Runnable action;
    private final List<RadialNode> children;
    private final Supplier<List<RadialNode>> childrenSupplier;

    private RadialNode(String id, Component title, ItemStack icon,
                       List<RadialNode> children, Supplier<List<RadialNode>> childrenSupplier, Runnable action) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.children = children;
        this.childrenSupplier = childrenSupplier;
        this.action = action;
    }

    /** 叶子节点：触发后执行动作并关闭菜单。 */
    public static RadialNode action(String id, Component title, ItemStack icon, Runnable action) {
        return new RadialNode(id, title, icon, null, null, action);
    }

    /** 分支节点：用固定子节点列表构造。 */
    public static RadialNode submenu(String id, Component title, ItemStack icon, List<RadialNode> children) {
        return new RadialNode(id, title, icon, children, null, null);
    }

    /** 分支节点：用惰性子节点提供器构造（打开时才解析）。 */
    public static RadialNode submenu(String id, Component title, ItemStack icon, Supplier<List<RadialNode>> supplier) {
        return new RadialNode(id, title, icon, null, supplier, null);
    }

    public boolean hasChildren() {
        if (children != null) return !children.isEmpty();
        return childrenSupplier != null;
    }

    public List<RadialNode> children() {
        if (children != null) return children;
        if (childrenSupplier != null) {
            List<RadialNode> s = childrenSupplier.get();
            return s == null ? List.of() : s;
        }
        return List.of();
    }
}
