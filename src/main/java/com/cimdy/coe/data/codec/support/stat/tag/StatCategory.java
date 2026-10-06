package com.cimdy.coe.data.codec.support.stat.tag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 标签层级条目：一个父类别标签及其子标签集合。
 *
 * <p>父子关系若写死在静态表里，新增一个"元素/异常"类目就必须改 Java 代码重新编译；
 * 故走数据驱动：每个父类别一个 JSON，位于 {@code data/<ns>/coe/stat_category/<parent>.json}，
 * 由 datagen 输出内置默认值，数据包可覆盖或新增。
 *
 * <p>合并语义（多数据包共存时）：
 * <ul>
 *   <li>{@code replace = false}（默认）：子标签集合与已有集合<b>取并集</b>，
 *       便于附属数据包给 {@code elemental} 追加自定义元素而不破坏内置项；</li>
 *   <li>{@code replace = true}：完全<b>替换</b>该父类别的子标签集合，用于需要精确裁剪的场合。</li>
 * </ul>
 *
 * <p>JSON 示例：
 * <pre>{@code
 * {
 *   "parent": "elemental",
 *   "children": ["fire", "cold", "lightning", "ignite", "chill", "freeze", "shock", "electrocuted"],
 *   "replace": false
 * }
 * }</pre>
 */
public record StatCategory(String parent, Set<String> children, boolean replace) {

    /** 子标签集合以有序集合承载：保证 datagen 输出稳定、diff 可读。 */
    private static final Codec<Set<String>> TAG_SET_CODEC = Codec.STRING.listOf().xmap(
            list -> Collections.unmodifiableSet(new LinkedHashSet<>(list)),
            ArrayList::new);

    public static final Codec<StatCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("parent").forGetter(StatCategory::parent),
            TAG_SET_CODEC.fieldOf("children").forGetter(StatCategory::children),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(StatCategory::replace)
    ).apply(instance, StatCategory::new));

    public StatCategory {
        if (parent == null || parent.isBlank())
            throw new IllegalArgumentException("StatCategory.parent 不能为空");
        if (children == null || children.isEmpty())
            throw new IllegalArgumentException("StatCategory.children 不能为空（父类别: " + parent + "）");
        children = Collections.unmodifiableSet(new LinkedHashSet<>(children));
    }

    /** 便捷构造：并集合并语义（数据包可继续追加子标签）。 */
    public static StatCategory of(String parent, Set<String> children) {
        return new StatCategory(parent, children, false);
    }
}
