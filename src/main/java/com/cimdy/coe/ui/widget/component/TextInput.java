package com.cimdy.coe.ui.widget.component;
import com.cimdy.coe.ui.widget.render.Renderers;
import com.cimdy.coe.ui.widget.render.TextFieldRendererI;

import com.cimdy.coe.ui.widget.core.Modifiers;
import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.TextView;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.cimdy.coe.ui.widget.component.assoc.Property;
import com.cimdy.coe.ui.widget.component.assoc.SimpleObservable;

/**
 * 单行文本输入框：完整的文本编辑状态机。
 *
 * <p>状态（文本、光标、选区、水平滚动位置）全部住在这里，
 * 渲染器通过 {@link TextView} <b>只读拉取</b>。
 * 旧 {@code AdaptiveInputBox} 是反过来的 —— 每帧调八个 setter 把状态推给渲染器，
 * 漏推一个就画面滞后，而且两边各存一份必然会不同步。
 *
 * <p>业务相关的过滤（比如「按物品名搜索」）不属于这里。用 {@link #onChange(Consumer)}
 * 把文本抛给外部，由外部去决定怎么过滤列表。
 */
public class TextInput extends Node implements TextView {

    private String text = "";
    private int cursorPos = 0;
    /** 选区的另一端；与 {@link #cursorPos} 相等表示无选区。 */
    private int highlightPos = 0;
    /** 水平滚动：可见文本从原文第几个字符开始。 */
    private int displayPos = 0;

    private int maxLength = 256;
    private int textPadding = 4;
    private boolean editable = true;

    private final Property<String> value = new SimpleObservable<>("");
    private Consumer<String> onChange;
    private Consumer<String> onSubmit;
    /** 值级校验：返回 false 的整串会被拒绝写入。 */
    private Predicate<String> validator;
    /** 字符级过滤：返回 false 的字符不会被输入。 */
    private Predicate<Character> charFilter = c -> c >= 32 && c != 127;

    private String preedit;
    private int preeditCursor;

    /** 撤销栈（与旧 AdaptiveInputBox 的 history 同语义：每步提交一次）。 */
    private final ArrayList<String> history = new ArrayList<>();
    private int historyIndex = -1;

    private TextFieldRendererI textLayer;

    public TextInput() {
        this.id = "text-input";
        flex.height = Draw.lineHeight() + 6;
        this.textLayer = Renderers.textField(this).padding(textPadding);
        with(textLayer);
    }

    /** 中性默认皮肤：深色底 + 聚焦高亮边框。 */
    public TextInput styled() {
        visuals.add(0, Theme.input());
        return this;
    }

    // ------------------------------------------------------------------
    // 配置
    // ------------------------------------------------------------------

    public TextInput placeholder(Component c) { textLayer.placeholder(c); return this; }
    public TextInput placeholder(String s)    { return placeholder(Component.literal(s)); }

    public TextInput maxLength(int n) { this.maxLength = Math.max(1, n); return this; }

    public TextInput editable(boolean b) { this.editable = b; return this; }

    public TextInput onChange(Consumer<String> c) { this.onChange = c; value.addListener((o, n) -> c.accept(n)); return this; }

    public TextInput onSubmit(Consumer<String> c) { this.onSubmit = c; return this; }

    public TextInput validator(Predicate<String> p) { this.validator = p; return this; }

    public TextInput charFilter(Predicate<Character> p) { this.charFilter = p; return this; }

    /** 当前是否可编辑（供文字层决定悬停是否显示 IBEAM 光标）。 */
    public boolean editable() { return this.editable; }

    // ------------------------------------------------------------------
    // 取值 / 赋值
    // ------------------------------------------------------------------

    public String value() { return text; }

    public TextInput value(String v) {
        String s = v == null ? "" : v;
        if (s.length() > maxLength) s = s.substring(0, maxLength);
        if (validator != null && !validator.test(s)) return this;
        boolean changed = !s.equals(text);
        if (changed) pushHistory();
        text = s;
        cursorPos = highlightPos = text.length();
        clampDisplay();
        if (changed) notifyChange();
        return this;
    }

    public boolean isEmpty() { return text.isEmpty(); }

    public void clear() { pushHistory(); value(""); }

    // ------------------------------------------------------------------
    // 撤销 / 重做（对齐旧 AdaptiveInputBox 的 history 行为）
    // ------------------------------------------------------------------

    private void pushHistory() {
        // 与上一帧提交文本相同则跳过，避免连续输入产生大量重复快照
        if (historyIndex >= 0 && historyIndex < history.size()
                && history.get(historyIndex).equals(text)) return;
        while (history.size() > historyIndex + 1) history.remove(history.size() - 1);
        history.add(text);
        historyIndex = history.size() - 1;
    }

    public void undo() {
        if (historyIndex <= 0) return;
        historyIndex--;
        restore(history.get(historyIndex));
    }

    public void redo() {
        if (historyIndex >= history.size() - 1) return;
        historyIndex++;
        restore(history.get(historyIndex));
    }

    private void restore(String t) {
        text = t;
        cursorPos = highlightPos = text.length();
        clampDisplay();
        notifyChange();
    }

    private void notifyChange() {
        value.set(text);
    }

    // ------------------------------------------------------------------
    // TextView：渲染器的只读窗口
    // ------------------------------------------------------------------

    @Override public String text() { return text; }
    @Override public int cursorPos() { return cursorPos; }
    @Override public int selectionStart() { return Math.min(cursorPos, highlightPos); }
    @Override public int selectionEnd() { return Math.max(cursorPos, highlightPos); }
    @Override public int displayPos() { return displayPos; }
    @Override public @Nullable String preedit() { return preedit; }
    @Override public int preeditCursor() { return preeditCursor; }

    // ------------------------------------------------------------------
    // 编辑操作
    // ------------------------------------------------------------------

    public void insert(String s) {
        if (!editable || s == null || s.isEmpty()) return;

        StringBuilder filtered = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (charFilter == null || charFilter.test(c)) filtered.append(c);
        }
        if (filtered.isEmpty()) return;

        int lo = selectionStart(), hi = selectionEnd();
        int room = maxLength - (text.length() - (hi - lo));
        if (room <= 0) return;
        String ins = filtered.length() > room ? filtered.substring(0, room) : filtered.toString();

        String next = new StringBuilder(text).replace(lo, hi, ins).toString();
        if (validator != null && !validator.test(next)) return;

        pushHistory();
        text = next;
        cursorPos = highlightPos = lo + ins.length();
        clampDisplay();
        notifyChange();
    }

    /** 删除若干字符：负数向左（退格），正数向右（Delete）。有选区时优先删选区。 */
    public void delete(int count) {
        if (!editable) return;

        int lo = selectionStart(), hi = selectionEnd();
        if (lo != hi) {
            applyDeletion(lo, hi);
            return;
        }
        if (count < 0) {
            int from = Math.max(0, cursorPos + count);
            applyDeletion(from, cursorPos);
        } else if (count > 0) {
            int to = Math.min(text.length(), cursorPos + count);
            applyDeletion(cursorPos, to);
        }
    }

    /** 按词删除（Ctrl+Backspace / Ctrl+Delete）。 */
    public void deleteWord(boolean forward) {
        if (!editable) return;
        if (selectionStart() != selectionEnd()) { delete(0); return; }
        int target = forward ? wordEnd(cursorPos) : wordStart(cursorPos);
        applyDeletion(Math.min(cursorPos, target), Math.max(cursorPos, target));
    }

    private void applyDeletion(int from, int to) {
        if (from >= to) return;
        pushHistory();
        String next = new StringBuilder(text).delete(from, to).toString();
        if (validator != null && !validator.test(next)) return;
        text = next;
        cursorPos = highlightPos = from;
        clampDisplay();
        notifyChange();
    }

    public void setCursor(int pos, boolean keepSelection) {
        cursorPos = Math.max(0, Math.min(text.length(), pos));
        if (!keepSelection) highlightPos = cursorPos;
        clampDisplay();
    }

    public void selectAll() {
        highlightPos = 0;
        cursorPos = text.length();
        clampDisplay();
    }

    public String selectedText() {
        return text.substring(selectionStart(), selectionEnd());
    }

    private int wordStart(int from) {
        int i = from;
        while (i > 0 && text.charAt(i - 1) == ' ') i--;
        while (i > 0 && text.charAt(i - 1) != ' ') i--;
        return i;
    }

    private int wordEnd(int from) {
        int i = from, n = text.length();
        while (i < n && text.charAt(i) == ' ') i++;
        while (i < n && text.charAt(i) != ' ') i++;
        return i;
    }

    /** 保证光标始终落在可视窗口内（水平滚动）。 */
    private void clampDisplay() {
        int inner = innerWidth();
        if (inner <= 0) { displayPos = 0; return; }

        Font font = Draw.font();
        displayPos = Math.min(displayPos, text.length());
        if (cursorPos < displayPos) {
            displayPos = cursorPos;
        }
        // 光标跑到右边界外时，把窗口往右挪到刚好能显示光标
        while (font.width(text.substring(displayPos, cursorPos)) > inner && displayPos < cursorPos) {
            displayPos++;
        }
        // 内容变短 / 光标左移后，把右侧空出来的位置还回去，避免文本"卡"在半空
        while (displayPos > 0 && font.width(text.substring(displayPos - 1)) <= inner) {
            displayPos--;
        }
    }

    private int innerWidth() { return Math.max(0, width - textPadding * 2); }

    // ------------------------------------------------------------------
    // 事件
    // ------------------------------------------------------------------

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button != 0) return false;
        focus();
        if (!editable) return true;

        Font font = Draw.font();
        String visible = font.plainSubstrByWidth(text.substring(Math.min(displayPos, text.length())), innerWidth());
        int rel = font.plainSubstrByWidth(visible, (int) (lx - textPadding)).length();
        // 鼠标事件不带修饰键掩码，只能轮询实时键盘状态
        setCursor(displayPos + rel, Modifiers.shift());
        return true;
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!editable) return false;
        boolean shift = Modifiers.shift(modifiers);
        boolean ctrl = Modifiers.editShortcut(modifiers);

        if (Modifiers.isSelectAll(keyCode, modifiers)) { selectAll(); return true; }
        if (Modifiers.isCopy(keyCode, modifiers))  { setClipboard(selectedText()); return true; }
        if (Modifiers.isPaste(keyCode, modifiers)) { insert(getClipboard()); return true; }
        if (Modifiers.isCut(keyCode, modifiers))   { setClipboard(selectedText()); delete(0); return true; }

        // 撤销 / 重做：对齐旧 AdaptiveInputBox（Ctrl+Z 撤销，Ctrl+Shift+Z / Ctrl+Y 重做）
        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            if (shift) redo(); else undo();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_Y) { redo(); return true; }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (ctrl) deleteWord(false); else delete(-1);
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (ctrl) deleteWord(true); else delete(1);
                yield true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                setCursor(ctrl ? wordStart(cursorPos) : cursorPos - 1, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                setCursor(ctrl ? wordEnd(cursorPos) : cursorPos + 1, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> { setCursor(0, shift); yield true; }
            case GLFW.GLFW_KEY_END -> { setCursor(text.length(), shift); yield true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (onSubmit != null) onSubmit.accept(text);
                yield true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> { blur(); yield true; }
            default -> false;
        };
    }

    @Override
    protected boolean onCharTyped(char codePoint, int modifiers) {
        if (!editable) return false;
        if (charFilter != null && !charFilter.test(codePoint)) return false;
        insert(String.valueOf(codePoint));
        return true;
    }

    private static void setClipboard(String s) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.keyboardHandler != null && s != null) mc.keyboardHandler.setClipboard(s);
    }

    private static String getClipboard() {
        Minecraft mc = Minecraft.getInstance();
        return (mc == null || mc.keyboardHandler == null) ? "" : mc.keyboardHandler.getClipboard();
    }

    // ------------------------------------------------------------------

    @Override
    public Node copy() {
        // 文本状态与渲染器绑死在实例上，克隆出的是一个「同样配置的空输入框」
        TextInput t = new TextInput();
        t.flex.copyFrom(this.flex);
        t.id = this.id;
        t.visible = this.visible;
        t.maxLength = this.maxLength;
        t.editable = this.editable;
        if (this.onChange != null) t.onChange(this.onChange);
        t.onSubmit = this.onSubmit;
        t.validator = this.validator;
        t.charFilter = this.charFilter;
        return t;
    }
}
