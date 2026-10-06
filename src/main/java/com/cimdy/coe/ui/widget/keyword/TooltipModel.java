package com.cimdy.coe.ui.widget.keyword;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键字 tooltip 的渲染期数据模型（包内共享，非 public 以减少文件数）。
 *
 * <ul>
 *   <li>{@link ParsedLine} —— 一行 tooltip：原文 + 解析出的关键字跨度。</li>
 *   <li>{@link KeywordSpan} —— 一个关键字的字符区间，布局后填入绝对坐标供命中测试。</li>
 *   <li>{@link BoxLayout} —— 一个 tooltip 盒子的几何信息（绝对坐标 + 全部关键字跨度）。</li>
 * </ul>
 */
class ParsedLine {
    /**
     * 绘制用组件：<b>必须与 {@link #text} 一致</b>。无 {@code $[id]} 占位符时就是原组件（保住行内配色），
     * 否则是解析后的字面量文本 —— 若画未解析原文而按 {@code text} 算 span 坐标，会露出裸的 {@code $[id]}
     * 且高亮下划线错位。
     */
    final Component display;
    final String text;
    final List<KeywordSpan> spans;
    final int lineWidth;

    ParsedLine(Component display, String text, List<KeywordSpan> spans, int lineWidth) {
        this.display = display;
        this.text = text;
        this.spans = spans;
        this.lineWidth = lineWidth;
    }
}

class KeywordSpan {
    final KeywordEntry entry;
    final int charStart, charEnd;
    /** 布局后填充的绝对坐标（命中测试用）。 */
    int absX, absY, w, h;

    KeywordSpan(KeywordEntry entry, int charStart, int charEnd) {
        this.entry = entry;
        this.charStart = charStart;
        this.charEnd = charEnd;
    }
}

class BoxLayout {
    int absX, absY, width, height;
    final List<ParsedLine> lines;
    final List<KeywordSpan> spans = new ArrayList<>();

    BoxLayout(List<ParsedLine> lines) { this.lines = lines; }
}

/**
 * 描述片段（C.9：KeywordEntry 描述 = 片段数组，替代旧的「整行字符串内嵌 $[id]」）。
 *
 * <p>片段三种（对齐 poe2db 句内嵌套词条）：
 * <ul>
 *   <li>{@link Type#LITERAL} —— 字面文本，普通色渲染；</li>
 *   <li>{@link Type#TRANSLATABLE} —— {@code @翻译键}，运行时 {@code Component.translatable} 取本地化串；</li>
 *   <li>{@link Type#REF} —— {@code $[关键字id]} 句内引用：渲染为被引关键字的<b>本地化标题</b>，
 *       且是<b>结构化</b>命中（携带 {@link KeywordEntry} 引用，不再仅靠子串命中），恒可点 / 可钻取。</li>
 * </ul>
 *
 * <p>片段串写法（运行时 JSON / datagen 写出共用）：裸串=字面；{@code @key}=翻译键；{@code $[id]}=引用。
 */
final class KeywordFragment {

    enum Type { LITERAL, TRANSLATABLE, REF }

    final Type type;
    /** LITERAL=字面文本；TRANSLATABLE=翻译键（不含 @）；REF=被引关键字 id。 */
    final String value;

    KeywordFragment(Type type, String value) {
        this.type = type;
        this.value = value == null ? "" : value;
    }

    /** 解析片段串：{@code @key}→TRANSLATABLE；{@code $[id]}→REF；其余→LITERAL。 */
    static KeywordFragment of(String s) {
        if (s == null || s.isEmpty()) return new KeywordFragment(Type.LITERAL, "");
        if (s.startsWith("@")) return new KeywordFragment(Type.TRANSLATABLE, s.substring(1));
        if (s.startsWith("$[") && s.endsWith("]") && s.length() >= 3) {
            return new KeywordFragment(Type.REF, s.substring(2, s.length() - 1));
        }
        return new KeywordFragment(Type.LITERAL, s);
    }
}
