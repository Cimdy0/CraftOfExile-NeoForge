package com.cimdy.coe.ui.widget.keyword;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 一个「关键字」的数据模型：在任意 tooltip 文本里被命中后，弹出它的描述窗口。
 *
 * <p>字段说明：
 * <ul>
 *   <li>{@link #id} —— 唯一标识，同时自动充当翻译键（{@code coe:keyword.<id>}）。</li>
 *   <li>{@link #title} —— 展示用标题（如 {@code Ailment}）。</li>
 *   <li>{@link #aliases} —— 触发匹配的别名集合（默认包含 title）。匹配时大小写不敏感、
 *       且要求词边界，避免把 {@code ail} 误命中 {@code frail}。</li>
 *   <li>{@link #keys} —— 翻译键集合（如 {@code coe:keyword.ailment}）。运行时若某 tooltip 行
 *       本身就是一个 {@code TranslatableComponent} 且其翻译键精确命中，则整行作为该关键字的命中，
 *       这是与「文本子串模糊匹配」并列的<b>精确匹配维度</b>。</li>
 *   <li>{@link #descFragments} —— 描述：<b>片段数组</b>（C.9），每行 = 一串
 *       {@link KeywordFragment}（字面 / {@code @翻译键} / {@code $[关键字引用]}）。
 *       引用片段渲染为可点关键字，形成<b>递归多级</b>窗口（PoE2 第三层断裂 bug 的修复）。</li>
 * </ul>
 *
 * <p>整个系统面向数据驱动：本类只是纯数据，真正的「抓取/维护机制知识」交给
 * {@code data/coe/keywords/<id>.json}（按 id 分文件，见 {@link KeywordRegistry#loadFromResourceManager}）。
 */
public class KeywordEntry {

    public final String id;
    public final Component title;
    public final List<Component> aliases;
    /** 描述：片段行数组（每行 = 一串 {@link KeywordFragment}）。 */
    public final List<List<KeywordFragment>> descFragments;
    /** 小写化后的别名，仅供匹配使用。 */
    final List<String> aliasLower;
    /** 翻译键集合（如 {@code coe:keyword.ailment}），供运行时精确匹配；自动含 {@code coe:keyword.<id>}。 */
    public final List<String> keys;

    private KeywordEntry(Builder b) {
        this.id = b.id;
        this.title = b.title;
        this.aliases = List.copyOf(b.aliases);
        this.descFragments = List.copyOf(b.descLines);

        List<String> al = new ArrayList<>();
        // Locale.ROOT 必须与 KeywordRegistry#find 的折叠方式一致（tr_TR 下 "I"→"ı" 会漏匹配）
        for (Component c : this.aliases) al.add(c.getString().toLowerCase(Locale.ROOT));
        String t = this.title.getString().toLowerCase(Locale.ROOT);
        if (!al.contains(t)) al.add(0, t);
        this.aliasLower = List.copyOf(al);

        List<String> ks = new ArrayList<>(b.keys);
        ks.add("coe:keyword." + this.id); // 自动以 id 充当翻译键，数据可省略 key
        this.keys = List.copyOf(ks);
    }

    /** 小写别名集合，供匹配。 */
    public List<String> aliasStrings() { return aliasLower; }

    /** 翻译键集合（已含自动注入的 {@code coe:keyword.<id>}），供精确匹配。 */
    public List<String> keyStrings() { return keys; }

    /** 描述片段行（递归引用其它关键字）。渲染端经 {@link KeywordRegistry#compileDescription} 编译。 */
    public List<List<KeywordFragment>> descFragments() { return descFragments; }

    public static Builder builder(String id, Component title) {
        return new Builder(id, title);
    }

    public static class Builder {
        final String id;
        final Component title;
        final List<Component> aliases = new ArrayList<>();
        final List<List<KeywordFragment>> descLines = new ArrayList<>();
        final List<String> keys = new ArrayList<>();

        Builder(String id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder alias(Component c) { aliases.add(c); return this; }
        public Builder alias(String s)   { aliases.add(Component.literal(s)); return this; }
        /** 追加一行描述（该行由一串 {@link KeywordFragment} 组成）。 */
        public Builder fragments(List<KeywordFragment> line) {
            if (line != null && !line.isEmpty()) descLines.add(List.copyOf(line));
            return this;
        }
        /** 声明一个翻译键（如 {@code coe:keyword.ailment}），运行时按此键精确匹配 tooltip 的翻译键。 */
        public Builder key(String k)      { keys.add(k); return this; }

        public KeywordEntry build() { return new KeywordEntry(this); }
    }
}
