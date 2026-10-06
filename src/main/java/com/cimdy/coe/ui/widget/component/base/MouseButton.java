package com.cimdy.coe.ui.widget.component.base;

import org.lwjgl.glfw.GLFW;

/**
 * 鼠标键位 —— 把「允许哪些键触发」从裸 GLFW 整数 + 位运算收敛成类型安全的枚举。
 *
 * <p>之前 {@code Pressable.acceptButton(int glfwButton)} 直接吃 GLFW 整型常量，
 * 调用方得自己写 {@code onlyButton(LEFT).acceptButton(RIGHT)} 这种位运算式语义，
 * 传错一个数（比如 32 以外的魔数）编译器完全不会拦。这里用枚举约束取值，
 * 屏蔽底层位掩码，调用点只能写 {@code only(LEFT).accept(RIGHT)}。</p>
 */
public enum MouseButton {

    LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT),
    RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT),
    MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);

    private final int glfw;

    MouseButton(int glfw) { this.glfw = glfw; }

    /** 对应的 GLFW 键号（0/1/2）。 */
    public int glfw() { return glfw; }

    /** 在 {@code Pressable} 内部位掩码中占用的那一位。 */
    public int bit() { return 1 << glfw; }
}
