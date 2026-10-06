package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntries;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基底词缀 —— 装备固有属性（{@code EquipmentRead} 模板展开）在词缀体系中的表达。

 *
 * <p>phase 3a：基底属性作为 {@link BaseMod} 实例统一收入物品 {@code mods} 列表（与其它词缀同流），不再由
 * 消费端单独从 {@code EquipmentRead} 拉取。本类型直接持有已经展开为引用型 {@link StatEntry} 的基底条目列表
 * （来自 {@code EquipmentRead#baseStatEntries(quality)}），由 {@link EquipmentState} 在构造时按
 * 「基底 + 品质」重新派生并注入，故自身无 roll 语义。</p>
 *
 * <p>名称恒为「基底」（渲染时作为 BASE 段表头）；<b>没有前后缀位置</b>，故不引入 {@link Location}；
 * 不计入前后缀档位。基底属性已由 stat.* 行与 StatEngine 折叠条目展示，tooltip 跳过本类渲染
 * （见 {@code ItemTooltipAssembler#renderEntry}）。</p>
 */
public final class BaseMod implements ModifierEntry {

    /** 基底渲染名翻译键。 */
    public static final String NAME_KEY = "item.coe.mod.base";

    public static final MapCodec<BaseMod> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatEntry.CODEC.listOf().fieldOf("entries").forGetter(BaseMod::entries),
            Codec.INT.fieldOf("level").forGetter(BaseMod::level)
    ).apply(instance, BaseMod::new));

    private final List<StatEntry> entries;
    private final int level;

    public BaseMod(List<StatEntry> entries, int level) {
        this.entries = entries == null ? List.of() : List.copyOf(entries);
        this.level = level;
    }

    /** 基底属性条目（引用型 StatEntry 列表，已按品质缩放）。 */
    public List<StatEntry> entries() {
        return entries;
    }

    @Override
    public ModLayer layer() {
        return ModLayer.BASE;
    }

    @Override
    public Optional<MutableComponent> name() {
        return Optional.of(Component.translatable(NAME_KEY));
    }

    /** 基底无需求等级概念，恒 0（不参与装备需求等级取大）。 */
    @Override
    public int level() {
        return 0;
    }

    /** 基底条目经本类型承载，序列化由 {@link #MAP_CODEC} 负责，不暴露为单条 Addition。 */
    @Override
    public ModifierEntry.Type<BaseMod> getType() {
        return ModifierEntries.BASE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof BaseMod that
                && level == that.level
                && entries.equals(that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries, level);
    }

    @Override
    public String toString() {
        return "BaseMod{entries=" + entries + ", level=" + level + "}";
    }
}
