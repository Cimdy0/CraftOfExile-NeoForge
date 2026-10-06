package com.cimdy.coe.ui.widget.component.dnd;

import com.cimdy.coe.ui.widget.core.Node;

/**
 * 拖拽作用域：任何想拥有「独立拖拽命名空间」的容器（窗口 / 滚动容器 / 任意嵌套容器）实现本接口，
 * 暴露自己的 {@link DragRegistry} 子表。
 *
 * <p>作用：
 * <ul>
 *   <li>子节点 / 放置区登记到本作用域的注册表，命中测试时<b>优先于外层</b>（同容器 &gt; 父容器 &gt; 全局 &gt; 无关兄弟）；</li>
 *   <li>跨窗口拖拽仍可用 —— 根注册表（挂在 {@code WindowLayer} 上）会 DFS 聚合所有作用域的放置区，
 *       因此松手时即使光标在另一个窗口，也能命中那个窗口的 zone。</li>
 * </ul>
 *
 * <p>「嵌套 DragRegistry」由此实现：{@code Window} 与 {@code Scrollable} 各自持有一个子注册表，
 * 子注册表的父指针指向外层作用域的注册表，最顶层是 {@code WindowLayer} 的根注册表。
 */
public interface DragScope {

    /** 本作用域的拖拽注册表（惰性创建，父指针指向外层作用域的注册表）。 */
    DragRegistry dragRegistry();
}
