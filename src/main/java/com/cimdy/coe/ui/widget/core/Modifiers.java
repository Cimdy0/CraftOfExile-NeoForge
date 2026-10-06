package com.cimdy.coe.ui.widget.core;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;

/**
 * 修饰键查询。
 *
 * <p>为什么需要这么一个小工具类：1.26.2 把老的 {@code Screen.hasShiftDown()} 这批静态方法拆成了两半 ——
 * 实时键盘状态挪到 {@link Minecraft} 的<b>实例</b>方法上，快捷键判定挪到事件对象
 * （{@code InputState} / {@code MouseButtonEvent} 实现的 {@code InputWithModifiers}）上。
 * 而组件里两种场景都会遇到：
 *
 * <ul>
 *   <li>键盘回调手里有 {@code modifiers} 位掩码 —— 用带参版本，判定和事件严格同步；</li>
 *   <li>鼠标回调（本库的 {@code onMouseClicked} 只传坐标与键位）没有掩码 ——
 *       用无参版本轮询实时键盘状态。</li>
 * </ul>
 *
 * <p>另外 macOS 上编辑类快捷键用的是 ⌘ 而不是 Ctrl，这个平台差异也在这里一并吸收，
 * 组件代码不必各写一遍。
 */
public final class Modifiers {

    private Modifiers() {}

    private static final int KEY_LEFT_SUPER = 343;
    private static final int KEY_RIGHT_SUPER = 347;

    // ------------------------------------------------------------------
    // 实时轮询：没有事件掩码可用时（典型是鼠标点击）
    // ------------------------------------------------------------------

    public static boolean shift() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.hasShiftDown();
    }

    public static boolean control() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.hasControlDown();
    }

    public static boolean alt() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.hasAltDown();
    }

    /** ⌘ / Win 键。 */
    public static boolean superKey() {
        Window w = window();
        return w != null
                && (InputConstants.isKeyDown(w, KEY_LEFT_SUPER) || InputConstants.isKeyDown(w, KEY_RIGHT_SUPER));
    }

    /** 编辑类快捷键的主修饰键：Windows / Linux 上是 Ctrl，macOS 上是 ⌘。 */
    public static boolean editShortcut() {
        return InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? superKey() : control();
    }

    // ------------------------------------------------------------------
    // 位掩码：键盘事件里用，与事件严格同步，不受轮询时序影响
    // ------------------------------------------------------------------

    public static boolean shift(int mods)    { return (mods & InputConstants.MOD_SHIFT) != 0; }
    public static boolean control(int mods)  { return (mods & InputConstants.MOD_CONTROL) != 0; }
    public static boolean alt(int mods)      { return (mods & InputConstants.MOD_ALT) != 0; }
    public static boolean superKey(int mods) { return (mods & InputConstants.MOD_SUPER) != 0; }

    public static boolean editShortcut(int mods) {
        return (mods & InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER) != 0;
    }

    // ------------------------------------------------------------------
    // 文本编辑快捷键（与原版 InputWithModifiers 的判定规则保持一致）
    // ------------------------------------------------------------------

    public static boolean isSelectAll(int keyCode, int mods) { return shortcut(keyCode, mods, InputConstants.KEY_A); }
    public static boolean isCopy(int keyCode, int mods)      { return shortcut(keyCode, mods, InputConstants.KEY_C); }
    public static boolean isPaste(int keyCode, int mods)     { return shortcut(keyCode, mods, InputConstants.KEY_V); }
    public static boolean isCut(int keyCode, int mods)       { return shortcut(keyCode, mods, InputConstants.KEY_X); }

    private static boolean shortcut(int keyCode, int mods, int target) {
        return keyCode == target && editShortcut(mods) && !shift(mods) && !alt(mods);
    }

    private static Window window() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.getWindow();
    }
}
