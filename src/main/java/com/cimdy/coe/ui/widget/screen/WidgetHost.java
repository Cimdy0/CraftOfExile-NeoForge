package com.cimdy.coe.ui.widget.screen;

import com.cimdy.coe.ui.widget.component.button.Button;
import com.cimdy.coe.ui.widget.component.panel.Window;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.ui.widget.component.base.Pressable;
import com.cimdy.coe.ui.widget.component.panel.ToastHost;
import com.cimdy.coe.ui.widget.component.panel.WindowLayer;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点树宿主：把 Minecraft 的 Screen 事件模型翻译成本库的节点树调用。
 *
 * <p>这是本库<b>唯一</b>与 {@code net.minecraft.client.gui.screens.Screen} 打交道的地方。
 * 组件层只认 {@code (double mx, double my, int button)} 这类朴素参数，
 * 版本升级时 Mojang 换事件 record（1.26 就把三个 int 参数包成了
 * {@link MouseButtonEvent} / {@link KeyEvent} / {@link CharacterEvent}）只需要改这一个类。
 *
 * <p>宿主是<b>组合</b>而非继承：{@link WidgetScreen} 用它，
 * 已经继承了 {@code AbstractContainerScreen} 的容器界面同样可以直接持有一个实例，
 * 不必为了用新 UI 去换基类。
 *
 * <h2>用法</h2>
 * <pre>
 * private final WidgetHost ui = new WidgetHost();
 *
 * protected void init() {
 *     ui.clear();
 *     ui.add(Window.of(title).position(x, y).size(w, h));
 *     ui.resize(this.width, this.height);
 * }
 * public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
 *     ui.render(g, mx, my, pt);
 * }
 * public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
 *     return ui.mouseClicked(e) || super.mouseClicked(e, dbl);
 * }
 * </pre>
 */
public class WidgetHost {

    private final WindowLayer layer = new WindowLayer();

    /** 轻提示宿主：整层渲染后画在最上层，由本类统一驱动 tick 与渲染。 */
    private final ToastHost toastHost = new ToastHost();

    private int screenW;
    private int screenH;

    /** 每帧重新布局。内容会变的界面（列表增删、文字变长）必须开，默认开。 */
    private boolean layoutEveryFrame = true;

    /** 定时任务与全局快捷键注册表。 */
    private final List<Task> tasks = new ArrayList<>();
    private final List<KeyBindingEntry> keyBindings = new ArrayList<>();

    // ------------------------------------------------------------------
    // 装配
    // ------------------------------------------------------------------

    public WindowLayer layer() { return layer; }

    /** 轻提示宿主：{@code success(...)} / {@code error(...)} / {@code push(...)} 弹非模态提示。 */
    public ToastHost toasts() { return toastHost; }

    /** 加一个顶层节点（通常是 {@code Window}），返回它本身以便继续链式配置。 */
    public <N extends Node> N add(N node) {
        layer.add(node);
        return node;
    }

    public void remove(Node node) { layer.remove(node); }

    /** 清空所有顶层节点。{@code init()} 重建 UI 前调用。 */
    public void clear() { layer.clearChildren(); }

    public WidgetHost layoutEveryFrame(boolean b) { this.layoutEveryFrame = b; return this; }

    // ------------------------------------------------------------------
    // 尺寸与渲染
    // ------------------------------------------------------------------

    /** 屏幕尺寸变化时调用（{@code init()} 里也要调一次）。 */
    public void resize(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        layer.layoutAll(screenW, screenH);
    }

    /** 渲染整层，含最上层的提示气泡；随后在最顶层补画轻提示。 */
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (layoutEveryFrame) layer.layoutAll(screenW, screenH);
        layer.renderLayer(g, mouseX, mouseY, partialTick);
        toastHost.render(g, screenW, screenH, partialTick);
    }

    /** 驱动动画（光标闪烁、侧栏展开）、轻提示生命周期与定时任务。Screen 的 {@code tick()} 里调。 */
    public void tick() {
        layer.tick();
        toastHost.tick();
        tickTasks();
    }

    // ------------------------------------------------------------------
    // 定时任务：依赖宿主每帧 tick，与动画走同一条时间线
    // ------------------------------------------------------------------

    /** 定时任务的取消句柄。 */
    public static final class TaskHandle {
        private Task task;
        private TaskHandle(Task t) { this.task = t; }
        /** 撤销：下一个 tick 起不再触发（已排队的本轮执行不受影响）。 */
        public void cancel() {
            if (task != null) {
                task.cancelled = true;
                task = null;
            }
        }
    }

    /** 内部任务条目：interval=0 表示一次性。 */
    private static final class Task {
        int remaining;
        final int interval;
        final Runnable action;
        boolean cancelled = false;
        Task(int delay, int interval, Runnable action) {
            this.remaining = Math.max(1, delay);
            this.interval = Math.max(0, interval);
            this.action = action;
        }
    }

    /** 每 {@code intervalTicks} 个 tick 执行一次（首次在 interval 之后）。 */
    public TaskHandle every(int intervalTicks, Runnable action) {
        return schedule(intervalTicks, intervalTicks, action);
    }

    /** 延迟 {@code delayTicks} 后执行一次。 */
    public TaskHandle after(int delayTicks, Runnable action) {
        return schedule(delayTicks, 0, action);
    }

    /** 通用：延迟 delayTicks 后首次触发，之后每 intervalTicks 一次（0 = 一次性）。 */
    public TaskHandle schedule(int delayTicks, int intervalTicks, Runnable action) {
        if (action == null) return new TaskHandle(null);
        Task t = new Task(delayTicks, intervalTicks, action);
        tasks.add(t);
        return new TaskHandle(t);
    }

    private void tickTasks() {
        if (tasks.isEmpty()) return;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task t = tasks.get(i);
            if (t.cancelled) {
                tasks.remove(i);
                continue;
            }
            if (--t.remaining > 0) continue;
            try {
                t.action.run();
            } catch (RuntimeException ex) {
                // 单条任务异常不应拖垮整棵 UI 树；记录日志便于排查
                CraftOfExile.LOGGER.error("WidgetHost task action threw an exception", ex);
            }
            if (t.interval > 0) t.remaining = t.interval;
            else tasks.remove(i);
        }
    }

    // ------------------------------------------------------------------
    // 全局快捷键：树没有消费的按键才轮到绑定
    // ------------------------------------------------------------------

    /** 快捷键绑定的解绑句柄。 */
    public static final class KeyBinding {
        private KeyBindingEntry entry;
        private KeyBinding(KeyBindingEntry e) { this.entry = e; }
        public void unbind() {
            if (entry != null) {
                entry.removed = true;
                entry = null;
            }
        }
    }

    private static final class KeyBindingEntry {
        final int keyCode;
        final int modifiers; // 要求按下的修饰键位（GLFW_MOD_*），允许额外修饰键
        final Runnable action;
        boolean removed = false;
        KeyBindingEntry(int keyCode, int modifiers, Runnable action) {
            this.keyCode = keyCode;
            this.modifiers = modifiers;
            this.action = action;
        }
    }

    /** 绑定按键（无修饰键）。返回句柄可 {@code unbind()}。 */
    public KeyBinding bindKey(int keyCode, Runnable action) {
        return bindKey(keyCode, 0, action);
    }

    /**
     * 绑定按键 + 修饰键（如 {@code GLFW.GLFW_KEY_E + GLFW.GLFW_MOD_CONTROL}）。
     * 修饰键按「必须包含」匹配：额外再按别的修饰键也仍会触发。
     */
    public KeyBinding bindKey(int keyCode, int modifiers, Runnable action) {
        if (action == null) return new KeyBinding(null);
        KeyBindingEntry e = new KeyBindingEntry(keyCode, modifiers, action);
        keyBindings.add(e);
        return new KeyBinding(e);
    }

    public void unbind(KeyBinding binding) { if (binding != null) binding.unbind(); }

    /**
     * 把可按压组件的 {@link Pressable#hotkey()} 注册成全局快捷键：按下即等同于点它一次。
     * 返回解绑句柄，丢弃引用也不会泄漏（与 {@link #bindKey} 同源管理）。
     *
     * <pre>{@code
     * Button save = Button.of("保存").hotkey(GLFW.GLFW_KEY_S).onClick(this::save);
     * ui.bind(save);
     * }</pre>
     */
    public KeyBinding bind(Pressable p) {
        if (p == null || !p.hasHotkey()) return new KeyBinding(null);
        return bindKey(p.hotkey(), p.hotkeyModifiers(), () -> p.fire(-1));
    }

    /**
     * 遍历整棵节点树，把其中所有带 {@code hotkey} 的 {@link Pressable} 一次性注册成全局快捷键。
     * 适合「界面建好后统一绑定」的场景：{@code ui.build(); ui.bindAll(rootWindow);}。
     */
    public void bindAll(Node root) {
        if (root == null) return;
        if (root instanceof Pressable p && p.hasHotkey()) bind(p);
        for (Node c : root.children) bindAll(c);
    }

    /** 命中即触发第一个匹配绑定并吞掉按键。 */
    private boolean fireShortcuts(int keyCode, int modifiers) {
        if (keyBindings.isEmpty()) return false;
        for (int i = keyBindings.size() - 1; i >= 0; i--) {
            KeyBindingEntry e = keyBindings.get(i);
            if (e.removed) {
                keyBindings.remove(i);
                continue;
            }
            if (e.keyCode == keyCode && (modifiers & e.modifiers) == e.modifiers) {
                e.action.run();
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // 事件转发：全部返回「是否已被 UI 吃掉」
    // ------------------------------------------------------------------

    public boolean mouseClicked(MouseButtonEvent event) {
        return layer.mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        return layer.mouseReleased(event.x(), event.y(), event.button());
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return layer.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return layer.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 按键先交给节点树（聚焦的输入框会正常吃掉打字键）；树没消费才轮到全局快捷键。
     * 这样快捷键天然不会打断输入。
     */
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(), scancode = event.scancode(), modifiers = event.modifiers();
        if (layer.keyPressed(key, scancode, modifiers)) return true;
        return fireShortcuts(key, modifiers);
    }

    /**
     * {@link CharacterEvent} 在 1.26 里<b>不带修饰键</b>（只有码点），
     * 而文本输入判断修饰键靠的是 {@code keyPressed}，这里传 0 不会丢信息。
     */
    public boolean charTyped(CharacterEvent event) {
        int cp = event.codepoint();
        if (Character.isBmpCodePoint(cp)) {
            return layer.charTyped((char) cp, 0);
        }
        // 补充平面字符拆成代理对逐个送入
        boolean handled = false;
        for (char c : Character.toChars(cp)) {
            handled |= layer.charTyped(c, 0);
        }
        return handled;
    }
}
