package com.cimdy.coe.ui.widget.component.bar;

import com.cimdy.coe.ui.widget.component.assoc.Property;

/**
 * 能力接口：具备「可渲染量程刻度」的组件。
 *
 * <p>这是「功能性组件」思想的第一块基石：<b>基础原语按能力命名与定义</b>，
 * 复合体只认能力、不认具体类。一个组件只要能给出 {@link #range()} 并支持 {@link #vertical()}，
 * 就能被任意复合体当作「刻度」使用——它与「条 / 滑块」毫无耦合。
 *
 * <p>接口只描述能力，不要求 extends Node；复合体持有它后通过接口接线，子组件是否真是节点由实现决定。
 */
public interface ScaleCapable {

    /** 当前量程（可被关联 / 绑定驱动）。 */
    Property<Range> range();

    /** 切换竖直方向。 */
    void vertical();
}
