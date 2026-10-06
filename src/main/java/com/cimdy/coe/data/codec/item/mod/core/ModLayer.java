package com.cimdy.coe.data.codec.item.mod.core;
import com.mojang.serialization.Codec;

/**
 * 词缀层（layer）：词缀在装备上的归属层，决定<b>分类、渲染分组与是否计入前后缀档位</b>。

 *
 * <p>取代原先「用 {@link Location} 同时表达『层』与『前后缀位置』」的混用：
 * {@code Location.PREFIX/SUFFIX} 既是层又是位置，{@code Location.ENCHANT} 却被符文加成与
 * vanilla 附魔共用 —— 语义重叠导致符文加成在 tooltip 上被当成附魔而漏渲染。
 * 现由 {@code ModLayer} 表达「层」，{@link Location} 只保留「前后缀位置」语义（下缀专属）。</p>
 *
 * <p>枚举序即渲染序：{@code BASE → PREFIX → SUFFIX → RUNE → ENCHANT}。</p>
 */
public enum ModLayer {
    /** 基底：装备固有属性（模板展开），名称渲染为「基底」，不计入前后缀档位。 */
    BASE("base"),
    /** 前缀（下缀）。 */
    PREFIX("prefix"),
    /** 后缀（下缀）。 */
    SUFFIX("suffix"),
    /** 符文加成：镶嵌产物，不渲染名称，不计入前后缀档位。 */
    RUNE("rune"),
    /** 原生附魔：vanilla ENCHANTMENTS 共生层。 */
    ENCHANT("enchant");

    public static final Codec<ModLayer> CODEC = Codec.STRING.xmap(ModLayer::byName, ModLayer::getName);

    private final String name;

    ModLayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** 由数据字符串解析层；空 / 未知 -> {@link #PREFIX}（兼容旧存档的 location 字段）。 */
    public static ModLayer byName(String s) {
        if (s == null || s.isEmpty()) {
            return PREFIX;
        }
        return switch (s) {
            case "base" -> BASE;
            case "prefix" -> PREFIX;
            case "suffix" -> SUFFIX;
            case "rune" -> RUNE;
            case "enchant" -> ENCHANT;
            default -> PREFIX;
        };
    }
}
