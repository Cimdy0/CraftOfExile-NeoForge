package com.cimdy.coe.ui.widget.render;

/**
 * 风格预设的运行时上下文。
 *
 * <p>级联解析的「屏幕覆盖」一级需要知道当前正在构建/渲染的是哪个 Screen。
 * Minecraft 的 UI 构建与渲染都在主线程上顺序进行，因此用 {@link ThreadLocal} 承载当前屏幕 id 是
 * 安全的：{@link WidgetScreen} 在 {@code init()}/{@code render()} 时写入、渲染结束后清掉。
 *
 * <p>屏幕 id 默认取 Screen 子类的简单类名；用户配置里 {@code screens.<id>} 的键名即此值。
 */
final class StylePresetContext {

    private static final ThreadLocal<String> SCREEN = new ThreadLocal<>();

    private StylePresetContext() {}

    public static void setScreen(String id) { SCREEN.set(id); }

    public static String currentScreen() { return SCREEN.get(); }
}
