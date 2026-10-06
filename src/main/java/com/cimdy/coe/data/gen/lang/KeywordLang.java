package com.cimdy.coe.data.gen.lang;

import com.cimdy.coe.data.gen.source.KeywordSource;

import java.util.List;

/**
 * 关键字双语翻译键注册。
 *
 * <p>为 {@link KeywordSource} 中的每个关键字注册 {@code coe:keyword.<id>} 的 en + zh 双语：
 * <ul>
 *   <li><b>zh</b>：直接取规范数据源的 {@code title}（展示标题，默认中文）。</li>
 *   <li><b>en</b>：取别名列表中第一个纯 ASCII 项（即英文标题，如 {@code Ailment}）；
 *       无 ASCII 别名时回退到 {@code title}，避免空键。</li>
 * </ul>
 *
 * <p>该键供 {@code KeywordRegistry#resolveText} 解析描述里的 {@code $[id]} 占位符——
 * 把占位符替换为对应关键字的<b>本地化</b>标题（en 区显示英文、zh 区显示中文），实现本地化安全的嵌套引用。
 *
 * <p>数据源与 {@code KeywordProvider} 完全一致（同 {@link KeywordSource}），杜绝键漂移。
 */
public final class KeywordLang {

    private KeywordLang() {
    }

    /** 在 lang 生成期调用：把全部关键字的 {@code coe:keyword.<id>} 双语键注入 {@code L}。 */
    public static void install(LangSupport L) {
        for (KeywordSource.Entry e : KeywordSource.all()) {
            String en = firstAscii(e.aliases);
            if (en == null || en.isEmpty()) en = e.title;
            L.addPair("coe:keyword." + e.id, en, e.title);
        }
    }

    /** 第一个纯 ASCII（英文）别名；找不到返回 null。 */
    private static String firstAscii(List<String> aliases) {
        if (aliases == null) return null;
        for (String a : aliases) {
            if (a != null && a.chars().allMatch(cp -> cp <= 0x7F)) return a;
        }
        return null;
    }
}
