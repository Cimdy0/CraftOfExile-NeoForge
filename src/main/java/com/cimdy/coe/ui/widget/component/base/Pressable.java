package com.cimdy.coe.ui.widget.component.base;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.anim.AnimatedNode;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

/**
 * 「可按压」行为基类：按下 → 抬起 → 若仍在范围内则触发。
 *
 * <p>旧库里这套逻辑在 {@code AdaptiveButton} 和 {@code AdaptiveWindow}（标题栏按钮）各写了一遍，
 * 且都只在 {@code mouseClicked} 里立刻触发 —— 没有「按下再拖出去取消」的语义，
 * 也没有 pressed 视觉态。这里统一成标准按钮状态机：
 *
 * <ul>
 *   <li>按下时置 {@code pressed}，抬起时清除；</li>
 *   <li>抬起点仍在组件内才算一次点击（拖出去再松开＝取消）；</li>
 *   <li>{@link #toggle} 模式下点击翻转 {@code active}，天然支持 Tab 页 / 开关；</li>
 *   <li>聚焦后可用 Enter/Space 触发，键鼠一致。</li>
 * </ul>
 *
 * <p>不含任何外观。要长什么样，往 {@code visuals} 里叠渲染器。
 *
 * <p>继承 {@link AnimatedNode} 而非 {@code Node}：让按钮类组件能挂按压缩放等原语
 * （{@code scaleTo} 等）。未挂动画时 {@link AnimatedNode} 直走 {@code Node} 原渲染/派发路径，零回归。
 */
public abstract class Pressable extends AnimatedNode {

    /** 点击回调。{@code button} 为 GLFW 鼠标键号，键盘触发时为 -1。 */
    @FunctionalInterface
    public interface Action {
        void run(Pressable source, int button);
    }

    protected Action action;

    /** 允许触发的鼠标键位掩码，默认仅左键。 */
    private int buttonMask = 1 << GLFW.GLFW_MOUSE_BUTTON_LEFT;

    private boolean toggle = false;
    private boolean clickSound = true;

    /** 记录是哪个键按下的，抬起时只认同一个键。 */
    private int pressedButton = -1;

    /**
     * 期望绑定的全局快捷键（GLFW key code）。{@code -1} 表示未设。
     * 仅记录意图，真正注册由宿主（{@link com.cimdy.coe.ui.widget.screen.WidgetHost#bind}）完成，
     * 这样本类保持数据无关、不依赖任何宿主引用。
     */
    private int hotkey = -1;
    private int hotkeyModifiers = 0;

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public Pressable onClick(Action a) { this.action = a; return this; }

    public Pressable onClick(Runnable r) {
        this.action = (src, btn) -> r.run();
        return this;
    }

    /** 追加允许触发的键位（默认已含左键）。 */
    public Pressable accept(MouseButton... buttons) {
        for (MouseButton b : buttons) buttonMask |= b.bit();
        return this;
    }

    /** 只接受指定的键位，覆盖之前所有设置。 */
    public Pressable only(MouseButton button) {
        this.buttonMask = button.bit();
        return this;
    }

    /** 接受全部三个键位。 */
    public Pressable acceptAll() {
        this.buttonMask = (1 << GLFW.GLFW_MOUSE_BUTTON_LEFT)
                | (1 << GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                | (1 << GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        return this;
    }

    /** 开关模式：点击翻转 active 态。 */
    public Pressable toggle(boolean b) { this.toggle = b; return this; }

    public Pressable clickSound(boolean b) { this.clickSound = b; return this; }

    /** 选中态（Tab 页高亮、开关打开）。 */
    public Pressable active(boolean b) { this.state = state.active(b); return this; }

    public boolean isActive() { return state.active(); }

    /**
     * 绑定一个全局快捷键：之后在宿主上调用 {@code host.bind(this)} 即把该键注册成「按下即触发本按钮」。
     * <pre>{@code
     * Button save = Button.of("保存").hotkey(GLFW.GLFW_KEY_S).onClick(this::save);
     * ui.bind(save);   // 此后按 S 等同点击
     * }</pre>
     */
    public Pressable hotkey(int keyCode) { this.hotkey = keyCode; return this; }

    /** 绑定带修饰键的快捷键（如 {@code GLFW.GLFW_KEY_S + GLFW.GLFW_MOD_CONTROL}）。 */
    public Pressable hotkey(int keyCode, int modifiers) {
        this.hotkey = keyCode;
        this.hotkeyModifiers = modifiers;
        return this;
    }

    /** 已登记的快捷键码，{@code -1} 表示未设。 */
    public int hotkey() { return hotkey; }

    public int hotkeyModifiers() { return hotkeyModifiers; }

    public boolean hasHotkey() { return hotkey >= 0; }

    public boolean isPressed() { return state.pressed(); }

    // ------------------------------------------------------------------
    // 状态机
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (!accepts(button)) return false;
        pressedButton = button;
        state = state.pressed(true);
        focus();
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (pressedButton != button) return false;
        pressedButton = -1;
        boolean wasPressed = state.pressed();
        state = state.pressed(false);
        if (!wasPressed) return false;

        // 抬起点滑出组件 = 取消，这是所有原生 UI 的通用约定
        boolean inside = lx >= 0 && lx < width && ly >= 0 && ly < height;
        if (!inside) return true;

        fire(button);
        return true;
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ENTER
                && keyCode != GLFW.GLFW_KEY_SPACE
                && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }
        fire(-1);
        return true;
    }

    /** 直接触发一次点击（供外部代码或快捷键调用）。 */
    public void fire(int button) {
        if (isDisabled()) return;
        if (toggle) state = state.active(!state.active());
        if (clickSound) playClickSound();
        if (action != null) action.run(this, button);
    }

    private boolean accepts(int button) {
        return button >= 0 && button < 32 && (buttonMask & (1 << button)) != 0;
    }

    protected void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    // ------------------------------------------------------------------

    @Override
    protected void copyInto(Node n) {
        super.copyInto(n);
        if (n instanceof Pressable p) {
            p.action = this.action;
            p.buttonMask = this.buttonMask;
            p.toggle = this.toggle;
            p.clickSound = this.clickSound;
            p.hotkey = this.hotkey;
            p.hotkeyModifiers = this.hotkeyModifiers;
        }
    }
}
