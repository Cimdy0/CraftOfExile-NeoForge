package com.cimdy.coe.ui.widget.render;

import org.jetbrains.annotations.Nullable;

/** 组件向渲染器暴露的只读文本状态。 */
public interface TextView {
    String text();
    int cursorPos();
    int selectionStart();
    int selectionEnd();
    /** 水平滚动起点：可见文本从原文的第几个字符开始 */
    int displayPos();
    /** 输入法预编辑串，无则返回 null */
    @Nullable default String preedit() { return null; }
    /** 预编辑串内部的光标位置 */
    default int preeditCursor() { return 0; }
    /** 是否可编辑（不可编辑时不显示 IBEAM 光标）。 */
    default boolean editable() { return true; }
}
