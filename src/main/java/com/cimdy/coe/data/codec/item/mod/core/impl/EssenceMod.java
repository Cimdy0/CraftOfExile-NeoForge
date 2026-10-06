package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.ModifierEntries;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import java.util.Optional;

/**
 * 精髓词缀 —— 精髓作用产出的「必定出现」词缀，占据一条原生前后缀槽（故继承 {@link AffixMod}）。

 *
 * <p>与普通词缀（{@code CommonMod}）的区别：</p>
 * <ul>
 *   <li><b>名称渲染为「精髓」</b>（而非阶名词）—— 精髓没有 PoE 词缀阶级名；</li>
 *   <li><b>不经候选池抽取</b>，由 {@code EssenceSocketOps} 直接构造；冲突默认由 AdditionType 身份派生
 *       （见 {@link #conflict}），故与普通词缀共享同一冲突命名空间、跨命名空间正确互斥；</li>
 *   <li>min~max 是<b>取值范围</b>（与常规词缀同口径），落槽前随机一次；</li>
 *   <li><b>无需求等级</b>（精髓没有等级概念），{@code level} 恒 0。</li>
 * </ul>
 *
 * <p>冲突：默认空（派生轨）—— 精髓 body 与普通生命词缀复用同一 {@code ModAdditions.ADD_MAX_HEALTH} 常量，
 * 即得同一 AdditionType 身份 → 同一冲突组 → 跨命名空间互斥；同族不同阶亦因复用同一常量而自动覆盖而非叠加。
 * 需要手动隔离时可在数据填 {@code conflict} 命名（路径）走二次判定。</p>
 */
public final class EssenceMod extends AffixMod {

    /** 精髓渲染名翻译键。 */
    public static final String NAME_KEY = "item.coe.mod.essence";

    public static final MapCodec<EssenceMod> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("source").forGetter(EssenceMod::source),
            Addition.CODEC.fieldOf("addition").forGetter(EssenceMod::addition),
            Location.CODEC.fieldOf("location").forGetter(EssenceMod::location),
            Identifier.CODEC.optionalFieldOf("conflict").forGetter(EssenceMod::conflict)
    ).apply(instance, EssenceMod::new));

    private final String source;

    /**
     * @param conflict 冲突路径（手动填写，命名空间限定 {@link Identifier}）；空 {@link Optional} 由 AdditionType 身份派生
     *                 （精髓 body 与普通生命词缀复用同一 {@code ModAdditions.ADD_MAX_HEALTH} 常量 → 同组 → 跨命名空间正确互斥）。
     */
    public EssenceMod(String source, Addition addition, Location location, Optional<Identifier> conflict) {
        super(addition, 0, location, conflict);
        this.source = source;
    }

    /** 来源精髓条目 id（如 {@code perfect_body_essence}），供定位与调试。 */
    public String source() {
        return source;
    }

    @Override
    public Optional<MutableComponent> name() {
        return Optional.of(Component.translatable(NAME_KEY));
    }

    @Override
    public ModifierEntry.Type<EssenceMod> getType() {
        return ModifierEntries.ESSENCE;
    }

    @Override
    public EssenceMod withAddition(Addition addition) {
        return new EssenceMod(source, addition, location(), conflict());
    }

    @Override
    public EssenceMod withLocation(Location location) {
        return new EssenceMod(source, addition(), location, conflict());
    }

    @Override
    public String toString() {
        return "EssenceMod{source=" + source + ", location=" + location() + ", conflict=" + conflict()
                + ", additions=" + additions() + "}";
    }
}
