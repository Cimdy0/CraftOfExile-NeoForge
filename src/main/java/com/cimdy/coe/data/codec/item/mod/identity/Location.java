package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.mojang.serialization.Codec;

/**
 * 词缀<b>落位</b>：一条词缀占宿主的前缀还是后缀。候选池与运行时 {@code CommonMod} 的键类型。
 *
 * <p>取代原 {@code Mods} 记录的字符串常量（prefix/suffix/extra/enchant），使位置成为一等类型。</p>
 *
 * <h2>本枚举不表达「层」，也不含附魔</h2>
 * 只表达<b>下缀位置</b>这一个语义：附魔（vanilla {@code ENCHANTMENTS}）与符文加成都不是词缀，
 * 不占前后缀档位，故<b>不属于本枚举</b> —— 它们的「层」由 {@link com.cimdy.coe.data.codec.item.mod.core.ModLayer}
 * 承担（{@code ModLayer.ENCHANT} / {@code ModLayer.RUNE}），与本枚举的混用正是旧实现里
 * 「符文加成被当成附魔」那一类 bug 的根源（见 {@link com.cimdy.coe.data.codec.item.mod.core.ModLayer}）。
 * 因此本枚举<b>只有两个常量</b>：位置语义收窄后，{@code Location} 不再需要 {@code ENCHANT} 这类占位项。</p>
 *
 * <p>Codec 容忍空串 / 未知串：旧 {@code EquipmentState} 数据中可能残留 {@code location:""}，
 * 统一回落 {@link #PREFIX} 以避免反序列化失败（数据兼容性）。</p>
 */
public enum Location {
    PREFIX("prefix"),
    SUFFIX("suffix");

    public static final Codec<Location> CODEC = Codec.STRING.xmap(
            Location::byName, Location::getName);

    private final String name;

    Location(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** 由数据字符串解析位置；空 / 未知 -> {@link #PREFIX}（兼容旧存档）。 */
    public static Location byName(String s) {
        if (s == null || s.isEmpty()) {
            return PREFIX;
        }
        return s.equals("prefix") ? PREFIX : SUFFIX;
    }
}
